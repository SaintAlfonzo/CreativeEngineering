#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-only
# Headless check of the Yuushya Modelling backport's data and lang (plan_yuushya_backport.md A9/A10, bug B1):
# tools/yuushya_data_check.py over development/backportedYuushyaModelling/src/main/resources (1.19.2 recipe and loot
# formats, 52 recipes, registered ids, lang keys). Picked up by tools/check_all.py like every tools/test_*.sh.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
python3 tools/yuushya_data_check.py
