#!/usr/bin/env python3
"""Spice of Life: Apple Pie blacklist from the pack's drink tags (feature_writeup/raspberry_pie.md 7).

Raspberry Pie keeps drinks out of the food diet (its own tag solraspberrypie:drinks, which pulls in the tags below).
Apple Pie's blacklist must list the same items so its tooltip marks them as not counting; this writes it from the
item tag dump so the two never drift. Non-drink entries (lunchboxes) are kept from BASE.

Reads  kubejs/exported/tags/item_tags.json (/dumptags item)
Writes defaultconfigs/solapplepie-server.toml and its backup configs_edited/server_config/solapplepie-server.toml
       (blacklist line only). New worlds only; --worlds also rewrites every saves/*/serverconfig copy.
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TAGS = ROOT / 'kubejs/exported/tags/item_tags.json'
TARGETS = [ROOT / 'defaultconfigs/solapplepie-server.toml', ROOT / 'configs_edited/server_config/solapplepie-server.toml']
# The tags solraspberrypie:drinks reads (keep in step with development/solRaspberryPie/.../tags/items/drinks.json).
DRINK_TAGS = ['creativedelight:food_type/drink', 'farmersdelight:drinks', 'brewinandchewin:fermented_drinks', 'forge:drinks']
DRINK_ITEMS = ['minecraft:potion', 'minecraft:milk_bucket']
# Not drinks: lunchboxes count the food eaten out of them, not the box (lunchboxes.md).
BASE = ['solapplepie:lunchbox', 'solapplepie:lunchbag', 'solapplepie:golden_lunchbox', 'creativedelight:netherite_lunchbox']
LINE = re.compile(r'^(\s*blacklist\s*=\s*)\[.*\]\s*$', re.M)


def main():
    tags = json.loads(TAGS.read_text())
    drinks = set(DRINK_ITEMS)
    for t in DRINK_TAGS:
        drinks.update(x for x in tags.get(t, []) if not x.startswith('#'))
    entries = BASE + sorted(drinks - set(BASE))
    value = '[' + ', '.join(json.dumps(e) for e in entries) + ']'
    targets = list(TARGETS)
    if '--worlds' in sys.argv:
        targets += sorted(ROOT.glob('saves/*/serverconfig/solapplepie-server.toml'))
    changed = 0
    for path in targets:
        text = path.read_text()
        if not LINE.search(text):
            print(f'ERROR no blacklist line in {path.relative_to(ROOT)}')
            return 2
        new = LINE.sub(lambda m: m.group(1) + value, text, count=1)
        if new != text:
            path.write_text(new)
            changed += 1
            print(f'wrote {path.relative_to(ROOT)}')
    print(f'{len(entries)} blacklist entries ({len(entries) - len(BASE)} drinks), {changed} files changed')
    return 0


if __name__ == '__main__':
    sys.exit(main())
