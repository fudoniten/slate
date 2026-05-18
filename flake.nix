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
      in {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [ jdk nodejs clojure git curl docker ];

          shellHook = ''
            echo "Slate development environment loaded"
            echo "Node version: $(node --version)"
            echo "Java version: $(java -version 2>&1 | head -n 1)"
          '';
        };

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

        # Build the slate application
        slateApp = pkgs.stdenv.mkDerivation {
          pname = "slate";
          version = packageJson.version;
          src = ./.;

          buildInputs = [ jdk nodejs pkgs.clojure ];

          buildPhase = ''
            # Set up home for npm/clojure cache
            export HOME=$TMPDIR

            # Install npm dependencies
            npm ci

            # Build the ClojureScript application
            npx shadow-cljs release app
          '';

          installPhase = ''
            # Copy the built application and dependencies
            mkdir -p $out/app
            cp -r resources $out/app/
            cp -r node_modules $out/app/
            cp package.json $out/app/
            cp server.js $out/app/

            # Create a wrapper script
            mkdir -p $out/bin
            cat > $out/bin/slate <<EOF
            #!${pkgs.bash}/bin/bash
            cd $out/app
            exec ${nodejs}/bin/node server.js "\$@"
            EOF
            chmod +x $out/bin/slate
          '';
        };

        packages = {
          default = slateApp;
          slate = slateApp;

          deployContainer = helpers.deployContainers {
            name = "slate";
            repo = "registry.kube.sea.fudo.link";
            tags = [ "latest" versionInfo.versionTag ];
            environmentPackages = with pkgs; [
              nodejs_20
              cacert
              bash
              coreutils
            ];
            verbose = true;
            env = {
              NODE_ENV = "production";
              APP_VERSION = packageJson.version;
              GIT_COMMIT = versionInfo.gitCommit;
              GIT_TIMESTAMP = versionInfo.gitTimestamp;
              VERSION = versionInfo.versionTag;
            };
            entrypoint = [ "${slateApp}/bin/slate" ];
          };
        };

        apps = {
          default = {
            type = "app";
            program = "${slateApp}/bin/slate";
          };

          deployContainer = {
            type = "app";
            program =
              let deployContainer = self.packages.${system}.deployContainer;
              in "${deployContainer}/bin/deployContainers";
          };
        };
      });
}
