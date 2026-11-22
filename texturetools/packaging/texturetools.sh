#!/bin/sh
# SPDX-License-Identifier: GPL-3.0-only
# Launcher of the packaged texture tools: ./texturetools [gui [--screen NAME] | palette_swap ARGS... | combine_tex ARGS...
#	| paint_tex ARGS... | scale_tex ARGS... | upscale_sprites ARGS... | selftest | --version]
# Works from any folder and through a symlink (e.g. ~/.local/bin/texturetools).
self=$(readlink -f "$0")
here=$(dirname "$self")
export KIVY_NO_ARGS=1
exec "$here/app/texturetools" "$@"
