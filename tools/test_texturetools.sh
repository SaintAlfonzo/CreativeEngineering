#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-only
# Headless tests of the texture tools package (plan_texture_tools_gui.md section 10). Run by tools/check_all.py.
#   T1, T2, T2b parity: texturetools/ core and cli plus the shims (textureswapper/palette_swap.py and siblings)
#   against the frozen pre-move scripts in texturetools/tests/legacy/ (synthetic fixtures in a temp dir; the top-level
#   textureswapper/*.png sprites are read only).
# Later phases add their test_*.py files to texturetools/tests/; every one runs here and must exit 0.
cd "$(dirname "$0")/.." || exit 1
fail=0
for t in texturetools/tests/test_*.py; do
	python3 "$t" || fail=$((fail + 1))
done
echo "texturetools: $fail failed"
[ "$fail" -eq 0 ]
