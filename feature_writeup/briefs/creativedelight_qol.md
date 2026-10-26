# creativedelight / qol briefs

Written 2026-10-25. Every QoL feature gets a config switch (framework #5), default on, and keeps its marked `[QoL-n]` writeup. Rows 2, 4, 5 are scripts only
(no Java) and can go first; row 1 and 6 need framework #8.

**Built 2026-10-25: rows 1, 2, 4, 5, 6** (creative_delight.md 22, Testing, not installed); row 3 stays pinned. The
scratch checks are SQ-CDQ-01 to -08.

## qol #1: QoL-1 Remainder buffer
Status Testing (built 2026-10-25) / patcherMod + new jar. As built: pot and kettle mixins in CD (`farmersdelight`,
`farmersrespite` compartments); the oven stays in patcherMod (it is the pack's own block, so there is no CfB hook to move)
### Now
patcherMod `cooking/Remainder*.java` (`RemainderBuffer`, `RemainderBufferHolder`, `RemainderBufferStorage`, `RemainderOutputHandler`, `RemainderSlot`), mixins `CookingPotRemainderMixin`, `CookingPotMenuRemainderMixin`, `KettleRemainderMixin`, `KettleMenuRemainderMixin`, client `CookingPotScreenRemainderMixin`, `KettleScreenRemainderMixin`; the oven half in `oven/`.
### Build
1. Decide where the buffer classes live (decided: patcherMod). 2. CD compartment `farmersdelight` takes the pot and kettle mixins; compartment `cooking_for_blockheads` the oven half. 3. CD mixin json + refmap; remove patcherMod's entries in the same release. 4. Switch `qol.remainderBuffer` disables the mixins (not the storage classes).
### Decisions
The Brewin' keg and the Pickling Jar (vintageport #4, #16) reuse the buffer idea natively. Recommendation: the storage/API classes stay in patcherMod as shared API, only the Farmer's Delight and oven hooks move to CD.
### Tests
CDQ-01: D1 tests of remainder_buffer.md unchanged (bottle, bowl, bucket stay and stall when full); CDQ-02: switch off restores vanilla FD behavior; CDQ-03: CD boots without Cooking for Blockheads.

## qol #2: QoL-2 Placeable-food tooltips
Status Testing (built 2026-10-25; only the tooltip script moved, the tags and `/dumpplaceable` stay in the pack) / KubeJS. Move the tooltip script (`client_scripts/tooltips.js` section) and the `/dumpplaceable` data reader to `assets/creative_delight/kubejs/`; lines for non-member foods in `// requires:` files. Test CDQ-04: tooltips unchanged (client).

## qol #3: QoL-3 Food types and saturation word
Status Pinned / KubeJS. Rules still in development ([food_families.md](../food_families.md), food/types-and-tuning #13). Do after framework #4 so tables are keyed by `f`. Startup tables load through the jar startup loader.

## qol #4: QoL-4 Compost rows
Status Testing (built 2026-10-25, Farmer's, Fungi, Veggies and Vintage rows) / KubeJS. Member rows leave `compost_rules.js` for a CD startup script calling the compost helper in patcherMod's jar (`kubejs_startup/creativeengineering/func/compost.js`). Test CDQ-05: `compost` tier of each moved food unchanged (headless compare of the table).

## qol #5: QoL-5 Heatless routes and grid-meal moves
Status Testing (built 2026-10-25: owner chose to move the Veggies, Vintage and Fungi blocks now) / KubeJS. Travels with members #2 to #5 (the `_manual_only` moves and heatless pot routes are in each member's cooking file). This row adds the `qol.manualOnlyRoutes` switch: off leaves the member's own recipes and skips the pack's moves. Test CDQ-06: switch off, grid recipes of the 12 Veggies meals present again.

## qol #6: QoL-6 WTHIT crop line and JEI info pages
Status Testing (built 2026-10-25) / new jar + KubeJS. WTHIT crop-growth line (writeup `wthit-crop-growth-line`, patcherMod `crop_growth.json`) as a CD `wthit` compartment; JEI info pages (Fungi first) as client scripts `// requires: jei`. Switches `qol.wthitCropLine`, `qol.jeiInfoPages`. Tests CDQ-07 (line on member crops), CDQ-08 (info pages present, absent without JEI).
