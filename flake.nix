{
  description =
    "Slate - Kubernetes-ready ClojureScript UI service for Pseudovision/Tunarr ecosystem";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    nix-helpers = {
      url = "github:fudoniten/fudo-nix-helpers";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };
  outputs = { self, nixpkgs, flake-utils, nix-helpers }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        helpers = nix-helpers.legacyPackages.${system};
        jdk = pkgs.jdk17;
        nodejs = pkgs.nodejs_20;
        packageJson = builtins.fromJSON (builtins.readFile ./package.json);

        # Pre-fetch Maven/Clojure deps as a fixed-output derivation.
        # outputHash must be updated after any deps.edn change:
        #   1. Set outputHash = pkgs.lib.fakeHash
        #   2. Run: nix build .#packages.x86_64-linux.slate 2>&1 | grep 'got:'
        #   3. Paste the "got:" hash below and rebuild.
        mavenDeps = pkgs.stdenv.mkDerivation {
          name = "slate-maven-deps";
          src = ./.;
          nativeBuildInputs = [ jdk pkgs.clojure ];
          outputHashAlgo = "sha256";
          outputHashMode = "recursive";
          # TODO: replace with the real hash obtained by running the steps above.
          outputHash = pkgs.lib.fakeHash;
          buildPhase = ''
            export HOME=$TMPDIR
            clojure -P
          '';
          installPhase = ''
            cp -r $HOME/.m2 $out
          '';
        };

        # Build the slate application using buildNpmPackage so that npm deps
        # are fetched in a fixed-output derivation rather than at build time.
        # After running `nix run .#update`, run `nix build` and copy the
        # "got:" hash for npmDepsHash here.
        slateApp = pkgs.buildNpmPackage {
          pname = "slate";
          version = packageJson.version;
          src = ./.;

          npmDepsHash = pkgs.lib.trim (builtins.readFile ./npm-hash);

          nativeBuildInputs = [ jdk pkgs.clojure ];

          buildPhase = ''
            runHook preBuild
            export HOME=$TMPDIR
            cp -rL ${mavenDeps} $HOME/.m2
            chmod -R +w $HOME/.m2
            npx shadow-cljs release app
            runHook postBuild
          '';

          installPhase = ''
            runHook preInstall
            mkdir -p $out/app
            cp -r resources $out/app/
            cp -r node_modules $out/app/
            cp package.json $out/app/
            cp server.js $out/app/

            mkdir -p $out/bin
            cat > $out/bin/slate <<EOF
            #!${pkgs.bash}/bin/bash
            cd $out/app
            exec ${nodejs}/bin/node server.js "\$@"
            EOF
            chmod +x $out/bin/slate
            runHook postInstall
          '';
        };

        # Docker-based deploy script. Used by apps.deployContainer when the
        # Nix-native build is unavailable (e.g. while mavenDeps.outputHash
        # is still a placeholder). Requires Docker on PATH.
        deployContainerScript = pkgs.writeShellScript "slate-deploy-docker" ''
          set -euo pipefail
          REGISTRY="registry.kube.sea.fudo.link"
          NAME="slate"
          REPO_ROOT=$(${pkgs.git}/bin/git rev-parse --show-toplevel)
          GIT_HASH=$(${pkgs.git}/bin/git rev-parse HEAD 2>/dev/null || echo "unknown")
          GIT_TIMESTAMP=$(${pkgs.git}/bin/git log -1 --format=%ct 2>/dev/null || echo "unknown")
          VERSION_TAG=$(${pkgs.git}/bin/git log -1 --format=%cd --date=format:'%Y%m%d' 2>/dev/null || echo "dev")

          echo ""
          echo "################################################################"
          echo "# Building Docker image and pushing to $REGISTRY             #"
          echo "################################################################"
          echo ""

          docker build \
            --tag "$REGISTRY/$NAME:latest" \
            --tag "$REGISTRY/$NAME:$VERSION_TAG" \
            "$REPO_ROOT"

          docker push "$REGISTRY/$NAME:latest"
          docker push "$REGISTRY/$NAME:$VERSION_TAG"

          echo ""
          echo "Done! Pushed $REGISTRY/$NAME:latest and $REGISTRY/$NAME:$VERSION_TAG"
        '';

        # Version information (git commit + timestamp)
        versionInfo = let
          gitCommit = self.rev or self.dirtyRev or "unknown";
          gitTimestamp = if self ? lastModified then
            toString self.lastModified
          else
            "unknown";
          versionTag = if self ? lastModified then
            builtins.substring 0 8 gitTimestamp # Use YYYYMMDD
          else
            "dev";
        in { inherit gitCommit gitTimestamp versionTag; };

      in {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [ jdk nodejs clojure git curl docker ];

          shellHook = ''
            echo "Slate development environment loaded"
            echo "Node version: $(node --version)"
            echo "Java version: $(java -version 2>&1 | head -n 1)"
          '';
        };

        packages = {
          default = slateApp;
          slate = slateApp;
          # packages.deployContainer (Nix-native OCI image) is temporarily
          # removed because mavenDeps.outputHash is still a placeholder.
          # Once that hash is computed and filled in, restore it with:
          #   deployContainer = helpers.deployContainers { ... };
        };

        apps = {
          default = {
            type = "app";
            program = "${slateApp}/bin/slate";
          };

          update = {
            type = "app";
            program = let
              updateScript = pkgs.writeShellScript "slate-update" ''
                set -e
                REPO_ROOT=$(${pkgs.git}/bin/git rev-parse --show-toplevel)
                echo "Updating package-lock.json..."
                cd "$REPO_ROOT"
                ${nodejs}/bin/npm install --package-lock-only
                echo ""
                echo "Computing npm deps hash..."
                NPM_HASH=$(${pkgs.prefetch-npm-deps}/bin/prefetch-npm-deps "$REPO_ROOT/package-lock.json")
                printf '%s' "$NPM_HASH" > "$REPO_ROOT/npm-hash"
                echo "npm-hash updated: $NPM_HASH"
                echo ""
                echo "Done! Next step: if Clojure deps changed, recompute the maven hash:"
                echo "  nix build 2>&1 | grep 'got:'"
                echo "  Then update outputHash in the mavenDeps derivation in flake.nix."
                echo ""
                echo "Commit when ready:"
                echo "  git add package-lock.json npm-hash flake.nix && git commit"
              '';
            in "${updateScript}";
          };

          deployContainer = {
            type = "app";
            # Uses Docker (not the Nix-native build) so it works even while
            # mavenDeps.outputHash is still a placeholder. Once that hash is
            # set correctly, packages.deployContainer provides the pure-Nix path.
            program = "${deployContainerScript}";
          };
        };
      });
}
