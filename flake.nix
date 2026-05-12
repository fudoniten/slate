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

        # Production-ready container image
        packages.deployContainer = pkgs.dockerTools.buildLayeredImage {
          name = "slate";
          tag = "latest";
          maxLayers = 120;

          contents = with pkgs; [ cacert nodejs_20 bash coreutils ];

          config = {
            Env = [
              "NODE_ENV=production"
              "APP_VERSION=${packageJson.version}"
              "GIT_HASH=${self.rev or "dirty"}"
              "GIT_TIMESTAMP=${toString self.lastModified}"
            ];
            WorkingDir = "/app";
            ExposedPorts = { "3000/tcp" = { }; };
            Entrypoint = [ "${nodejs}/bin/node" "server.js" ];
            User = "nobody";
          };

          default = self.packages.${system}.deployContainer;
        };

        apps = {
          deployContainer = {
            type = "app";
            program =
              let deployContainer = self.packages."${system}".deployContainer;
              in "${deployContainer}/bin/deployContainers";
          };
        };
      });
}
