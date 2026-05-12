{
  description = "Slate - Kubernetes-ready ClojureScript UI service for Pseudovision/Tunarr ecosystem";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        jdk = pkgs.jdk17;
        nodejs = pkgs.nodejs_20;
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            jdk
            nodejs
            clojure
            git
            curl
            docker
          ];

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

          contents = with pkgs; [
            cacert
            nodejs_20
            bash
            coreutils
          ];

          config = {
            Env = [
              "NODE_ENV=production"
            ];
            WorkingDir = "/app";
            ExposedPorts = {
              "3000/tcp" = { };
            };
            Entrypoint = [ "${nodejs}/bin/node" "resources/public/js/main.js" ];
            User = "nobody";
          };

          extraCommands = ''
            mkdir -p app/resources/public/js
          '';
        };

        defaultPackage = self.packages.${system}.deployContainer;
      }
    );
}
