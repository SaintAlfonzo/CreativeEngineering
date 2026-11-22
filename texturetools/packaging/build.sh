#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-only
# Build the Linux bundle of the texture tools (plan_texture_tools_gui.md P9, rulings R1 and R2).
#	texturetools/packaging/build.sh [--clean] [--no-appimage]
# 1. venv in packaging/build/venv with texturetools/requirements.txt + requirements-build.txt (downloads from PyPI
#    on the first run; set TT_VENV to reuse another venv)
# 2. PyInstaller onedir (texturetools.spec) -> packaging/dist/texturetools/
# 3. tarball packaging/dist/texturetools-<version>-linux-x86_64.tar.gz: launcher, README.md, LICENSE, app/
# 4. listing check: no pack art, generated textures or jars (only Kivy's own theme images may be images)
# 5. AppImage only when appimagetool is on PATH (optional, offline)
# packaging/build and packaging/dist are git-ignored. Kivy writes its home to packaging/build/kivyhome, not ~/.kivy.
set -euo pipefail
HERE=$(cd "$(dirname "$0")" && pwd)
ROOT=$(cd "$HERE/../.." && pwd)
BUILD="$HERE/build"
DIST="$HERE/dist"
VENV=${TT_VENV:-$BUILD/venv}
APPIMAGE=1
for a in "$@"; do
	case "$a" in
		--clean) rm -rf "$BUILD" "$DIST" ;;
		--no-appimage) APPIMAGE=0 ;;
		*) echo "unknown option $a"; exit 2 ;;
	esac
done
VERSION=$(sed -n 's/^__version__ = "\(.*\)"/\1/p' "$ROOT/texturetools/__init__.py")
ARCH=$(uname -m)
NAME="texturetools-$VERSION-linux-$ARCH"
mkdir -p "$BUILD" "$DIST"

if [ ! -x "$VENV/bin/python" ]; then
	python3 -m venv "$VENV"
	"$VENV/bin/pip" install -q --upgrade pip
fi
"$VENV/bin/pip" install -q -r "$ROOT/texturetools/requirements.txt" -r "$HERE/requirements-build.txt"

export KIVY_HOME="$BUILD/kivyhome" KIVY_NO_CONSOLELOG=1 KIVY_NO_ARGS=1
cd "$HERE"
# Kivy's PyInstaller hooks open a window while analysing: give them a private X server, never the desktop
XVFB=()
if command -v xvfb-run >/dev/null 2>&1; then
	XVFB=(env -u WAYLAND_DISPLAY xvfb-run -a)
else
	export SDL_VIDEODRIVER=offscreen
fi
"${XVFB[@]}" "$VENV/bin/pyinstaller" --noconfirm --log-level WARN --distpath "$DIST" --workpath "$BUILD/pyi" texturetools.spec

STAGE="$BUILD/stage/$NAME"
rm -rf "$BUILD/stage"
mkdir -p "$STAGE"
cp -a "$DIST/texturetools" "$STAGE/app"
cp "$HERE/texturetools.sh" "$STAGE/texturetools"
cp "$HERE/README.md" "$HERE/LICENSE" "$STAGE/"
chmod +x "$STAGE/texturetools"
TAR="$DIST/$NAME.tar.gz"
tar -C "$BUILD/stage" -czf "$TAR" "$NAME"

# listing check: images only from Kivy's own data, no jars, no repo content folders
bad=$(tar -tzf "$TAR" | grep -E '\.jar$|/(kubejs|textureswapper|resourcepacks|generation_scripts|legacy)/' || true)
img=$(tar -tzf "$TAR" | grep -iE '\.(png|jpe?g|gif|tga|webp|bmp|xcf|ase|aseprite)$' \
	| grep -vE "^$NAME/app/_internal/kivy_install/data/" || true)
if [ -n "$bad$img" ]; then
	echo "tarball holds files that must not ship:"
	printf '%s\n%s\n' "$bad" "$img" | sed '/^$/d' | head -20
	exit 1
fi

if [ "$APPIMAGE" = 1 ] && command -v appimagetool >/dev/null 2>&1; then
	APPDIR="$BUILD/AppDir"
	rm -rf "$APPDIR"
	mkdir -p "$APPDIR"
	cp -a "$STAGE/." "$APPDIR/"
	printf '#!/bin/sh\nexec "$(dirname "$(readlink -f "$0")")/texturetools" "$@"\n' > "$APPDIR/AppRun"
	chmod +x "$APPDIR/AppRun"
	printf '[Desktop Entry]\nType=Application\nName=Texture Tools\nExec=texturetools\nIcon=texturetools\nCategories=Graphics;\nTerminal=false\n' \
		> "$APPDIR/texturetools.desktop"
	cp "$APPDIR/app/_internal/kivy_install/data/logo/kivy-icon-256.png" "$APPDIR/texturetools.png"
	ARCH="$ARCH" appimagetool --no-appstream "$APPDIR" "$DIST/$NAME.AppImage"
	echo "AppImage: $DIST/$NAME.AppImage"
else
	echo "AppImage: skipped (appimagetool not on PATH or --no-appimage)"
fi

echo "bundle: $(du -sh "$DIST/texturetools" | cut -f1)  $DIST/texturetools"
echo "tarball: $(du -h "$TAR" | cut -f1)  $TAR"
