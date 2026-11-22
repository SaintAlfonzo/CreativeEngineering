# Creative Common 1.3.0 (CurseForge file changelog)

First changelog for this project; covers everything since 1.1.0. Creative jars that need Creative Common now require 1.3.0 or later.

## New

- Wooden composters in spruce, birch, dark oak, jungle, acacia, mangrove, crimson, warped, twisted, ancient, blossom and azalea wood, each crafted from 7 slabs and working like the Composter.
- Melting: salt, fire, lava and magma melt ice and snow; snow melts one layer at a time; packed ice and cryosol melt slowly; ice and snow melt on their own in the Nether and in hot biomes. Blocks salt can melt are marked "Saltable" in their tooltip.
- Glass blocks and panes from every mod break into shards of their color without Silk Touch (needs Quark).
- Effect level caps: effects that do the same at every level are held at level I from every source, and tooltips show level I. The list is in the config.
- Config reload: changes saved to the pack's own config files while the game runs apply after about a second.
- Idle random ticks: blocks that can no longer change on a random tick stop asking for them.
- Sneak and right-click a placed sack to pick it up with its contents.
- Swap lists: permanent aliases for renamed ids, renamed capability keys, and a load report.
- Scripting: shapeless, crushing, milling, fermenting and fertilizer recipe helpers; json recipe edits are tracked as changes; tag scripts for plantable and smokable blocks.

## Changed

- Dictionaries for items, blocks and fluids follow the mod that owns the id; ids moved out of Common are gone from its dictionaries (the Diet Log, Liquid Diet Log and Food Dictionary keys moved to Spice of Life: Raspberry Pie).
- Own text uses American spelling.

## Fixed

- Opening chat with a key no longer types that key into the chat box.
- Several recipe helpers left a recipe unchanged in the output after an edit.
- Mods whose data namespace differs from their mod id were treated as not installed by the dictionaries and tag scripts.

## Requirements

Minecraft 1.19.2, Forge 43 or newer, KubeJS 1902.6.2-build.73 or newer. Both client and server need it.
