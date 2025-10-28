{
	description = "A Minecraft Paper plugin that makes every spawn egg throwable for a more fun way to spawn mobs";

	inputs = {
		nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.05";
		flake-utils.url = "github:numtide/flake-utils";
	};

	outputs = {
		nixpkgs,
		flake-utils,
		...
	}: flake-utils.lib.eachDefaultSystem (system:
			let
				pkgs = import nixpkgs {
					inherit system;
				};
			in {
				devShell = pkgs.mkShell {
					buildInputs = with pkgs; [
						git
						gradle
						udev
					];

					shellHook = ''
						export LD_LIBRARY_PATH=${pkgs.udev}/lib:$LD_LIBRARY_PATH
					'';
				};
			});
}
