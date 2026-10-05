# 1.0.5: duck and goose nests, stack sizes, generated cans, crop growth line, ghost-block fixes

STAGED 2026-10-05 (not yet copied to the server or clients). 1.0.5 is everything built after the 1.0.4 deploy
(2026-10-04 13:54, commit `334c76b`; `eebaf63` "New nests, stack size changes" is the only commit since and is part
of 1.0.5). There was no plan up front: this file records what went in, what is still open, and where each piece is
written up. It follows [plan_1.0.4.md](plan_1.0.4.md).

## 0. At a glance

| ID | Change | Kind | State | Writeup |
|---|---|---|---|---|
| WN | Duck and goose nests (`WaterfowlNests`): two craftable nest blocks (grass + tall grass, mirrored U shapes), egg-layer mixins for the Untitled Duck and Goose mods, loot tables, blockstates, WTHIT line | feature | BUILT, patcherMod 1.0.5 in `mods/`; scratch-server verified 2026-10-05; not tested in a game world | this file 1 |
| SS | Stack size policy: `tools/gen_stack_sizes.py` rules -> `stack_sizes.json` -> `startup_scripts/changes/stack_sizes.js`; replaces the egg-only `item_properties.js` | feature + tuning | BUILT 2026-10-05, never run in game | [stack_sizes.md](stack_sizes.md) |
| GC | Generated per-type cans (cooked, bread, salad, soup, golden): `tools/gen_can_types.py` -> `can_types.json`, registered by `items/cans.js`, recipes from `canning.js`, can servings and food types regenerated | feature | BUILT, needs a restart, untested | [food.md](food.md) |
| GL | WTHIT growth line for the plants WTHIT skips (62 blocks), `[crop_growth]` config, `/dumpcropgrowth` audit | feature | BUILT 2026-10-04, headless-tested; now in the 1.0.5 jar; CR-59..66 pending | [crops.md](crops.md) 2.9 |
| GB | Ghost blocks on a dedicated server: block-state ids past 2^19 overflowed in vanilla's section packet; null-state packet guards; `[debug.block_state_sync]` diagnostics (off by default) + `tools/diff_block_state_ranges.py` | fix + dev tool | BUILT in the 1.0.5 jar; not seen on a live server yet | [crops.md](crops.md) |
| SB | Supplementaries vs Breezy wind vane crash (API mismatch Breezy 1.0.8 / Supplementaries 2.4.20): `SupplementariesBreezyCompatMixin` | fix | BUILT in the 1.0.5 jar | none yet |
| WG | World gen data refresh: `pool_config.json`, `biome_config.json`, `wild_crops.json`, structure and pool tag reports | data | regenerated; ST- checks from 1.0.4 still pending | [structure_tuning.md](structure_tuning.md) |
| DR | Data: `item_dict.js` / `block_dict.js` keys for the nests and cans, compost values, `blocktags.js`, `misc_mods.js` recipes | data | live | this file 1 |
| CF | Config: `creativeengineering-common.toml` gets `[debug.block_state_sync]`; rare-ice and worldedit properties rewritten; JEI mod sort order; Dimensional Gravity `mod_sides` row dropped | config | live | this file 2 |
| VR | Version records: patcherMod `mod_version` 1.0.5 (exposureBackport unchanged at `-02`), jar archive, `pack/manifest.json` regenerated, two `KUBEJS_CLASSES` rules for the new runtime json files | bookkeeping | DONE 2026-10-05 | [deploy.md](deploy.md) |

## 1. What the payload contains

Update from 1.0.4: 22 files, 4.2 MB (mods 1, kubejs 17, config 4).

- **mods:** `creativeengineering-1.0.5.jar` (md5 8d827557) replaces `-1.0.4`. exposureBackport is unchanged.
- **kubejs:** 4 added (`can_types.json`, `stack_sizes.json`, `dump_crop_growth.js`, `stack_sizes.js`), 13 changed, 1 removed
  (`item_properties.js`, its egg rule is now part of the stack size policy).
- **config:** `creativeengineering-common.toml`, `rare-ice.properties`, `jei/ingredient-list-mod-sort-order.ini`,
  `worldedit/worldedit.properties`. `DistantHorizons.toml` is no longer shipped (player config since 1.0.4).
- **removals:** both sides delete `mods/creativeengineering-1.0.4.jar` and `kubejs/startup_scripts/changes/item_properties.js`.

## 2. Open

- Nothing in 1.0.5 has been played in a world. The jar was built and installed, and the payload staged, without a boot test.
- The 1.0.4 R4- checks and the CR-/ST-/WT-/VN- tests listed in [plan_1.0.4.md](plan_1.0.4.md) are still unrun.
- Duck and goose nests need a restart on the client (new items) and the server.
- After copying the payload: `python3 tools/build_deploy.py --snapshot 1.0.5`.
