{
  inputs = {
    flake-utils.url = "github:numtide/flake-utils";
    lint-utils = {
      url = "github:homotopic/lint-utils";
      inputs.nixpkgs.follows = "nixpkgs";
    };
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    shelpers.url = "gitlab:platonic/shelpers";
  };

  outputs =
    inputs@{ lint-utils, ... }:
      with builtins;
      inputs.flake-utils.lib.eachDefaultSystem
        (system:
        let
          p = inputs.nixpkgs.legacyPackages.${system};
          l = p.lib;

          fs = l.fileset;
          onlyExts = exts: path: fs.toSource {
            root = path;
            fileset = fs.fileFilter (f: foldl' l.or false (map f.hasExt exts)) path;
          };

          lu = lint-utils.linters.${system};
          lu-pkgs = lint-utils.packages.${system};

          inherit (inputs.shelpers.lib p) eval-shelpers shelp;
          shelpers =
            eval-shelpers [
              ({ config, ... }: {
                shelpers."." = {
                  General = {
                    shelp = shelp config;
                  };
                };
              })
            ];
        in
        {
          packages.default = p.stdenv.mkDerivation {
            name = "test";
            src = ./.;
            buildInputs = [ p.jdk p.maven ];
            buildPhase = ''
              mvn package
            '';
            installPhase = ''
              mkdir -p $out/bin
              mv target/lizzie-yzy-hex-shaded.jar $out/bin
            '';

            outputHash = "sha256-2UBXc7Z+FZXrszL0q5IwjwBos69hc8V5V+0UFY52G6s=";
            outputHashMode = "recursive";
          };

          devShells.default = p.mkShell {
            buildInputs = with p; [
              jdk
              maven
            ];

            shellHook = ''
              ${shelpers.functions}
              shelp
            '';
          };

          checks =
            let nixOnly = onlyExts [ "nix" ] ./.; in
            {
              nix-formatting = lu.nixpkgs-fmt { src = nixOnly; };
              nix-dce = lu.deadnix { src = nixOnly; };
              nix-linting = lu.statix { src = nixOnly; };
            };

          inherit (shelpers) apps;

          formatter = lu-pkgs.nixpkgs-fmt;
          shelpers = shelpers.files;
        });
}
