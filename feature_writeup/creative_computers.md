# Creative Computers: circuits, ComputerCraft, AE2 and control engineering in one mod (reserved)

Written 2026-11-01 from the Creative Split audit ([creative_split.md](creative_split.md) 2.3 and [split/](split/README.md)).
**Status: reserved, not planned.** The rows below ride in Creative Engineering until this mod is planned; Creative
Common never holds them. No source header marks them (owner, 2026-11-01); this list is the marker.

Mod id `creativecomputers`, package `com.SaintAlfonzo.creativecomputers`, config `creativecomputers-common.toml`, script
packs `kubejs_startup/creativecomputers/`, `kubejs/data/creativecomputers/kubejs/`.

## 1. Reserved rows (interim jar: Engineering)

The audit found far less than the plan expected: no Java, no mixins (the AE2 mixins are all tooltip stacks, which are
Interface rows in Common), and 37 script lines.

| Kind | Row | Source |
|---|---|---|
| Server scripts | `engineering/computing.js` (19 lines, ComputerCraft speaker recipe) | split/F_pack_scripts.md |
| Script rows | `engineering/mechanisms.js` 31-48 (redstone mechanism: electron tube and redstone); `components.js` has no circuit row | F section 13 |
| Candidate | `engineering/redstone.js` (256 lines, marked `Engineering?`): the real control-engineering content if this mod is to have any weight | F |
| Not here | AE2 tooltip mixins and `compat/AeTooltipStacks` (Common Interface); AE2 recipe rows live in the generic Engineering files and were not separable by file | split/B_mixins.md |

With under 300 lines even counting `redstone.js`, this mod is below the split's fold threshold; it stays reserved
only because the owner named it, and may be dropped into Engineering for good when the Engineering carve is planned.

## 2. History

- 2026-11-01: reserved from the split audit. Nothing moved.
