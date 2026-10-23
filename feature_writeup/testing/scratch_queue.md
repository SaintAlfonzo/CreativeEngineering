# Scratch test queue

Live checks waiting for a bulk scratch-server session. A bulk session groups the entries by world group, runs each
group on one server (`tools/creative_labels_test/scratch_server.sh`, memory `scratch-server-verify`), then marks
each entry PASS / FAIL here and moves the result into the feature's writeup. One game JVM at a time: check
`pgrep -c java` is 0 before booting (never `pgrep -af`).

Status word for a feature with entries here: "built, scratch test queued"; once its run passes, "built, scratch
server PASSED" (legend in [../README.md](../README.md)).

Entry fields: ID, feature, target state, world group, commands, expected log, pass / fail, if it fails.

## World groups

| Group | Setup |
|---|---|
| A. superflat | `scratch_server.sh setup OUT` as is (superflat creative) |
| B. fresh normal world | `scratch_server.sh setup OUT`, then in `OUT/srv/server.properties` set `level-type=minecraft\:normal` and `generate-structures=true` (`max-tick-time=-1` is already set) before `boot`; a new level-name every run |

Setup notes (from the 2026-10-22 run):

- A config key added in the current jar may be missing from the instance's toml (Forge adds it with its default at
  load). `grep` the key in `OUT/srv/config/` before relying on a `sed`; add the line under its section if it's absent.
- Server configs that ship through `defaultconfigs/` are merged from `configs_edited/server_config/` by
  `tools/build_deploy.py`. To test what a new player world gets, copy `configs_edited/server_config/*.toml` into
  `OUT/srv/defaultconfigs/` before the first boot of a new level.
- After a restart, wait about 30 s after `forceload add` before scheduling block ticks there; 5 s was too short.
- Probes: never call `getType()` on every recipe, use `Ingredient.getStacks()` (not `getItems()`), compare boxed
  config Booleans as text. Details in memory `rhino-fieldandmethods-gotcha`.
- `/dumpall` on a scratch server: recipes, tags, loot, structures, biomes, spawns and trades are good to copy into
  the instance's `kubejs/exported/`. Names, stacks, mob effects, gear, food, crops, compost, paintings and items by
  mod need the client (no mod lang files or client-only mods on a dedicated server).

Setup notes (from the 2026-10-22 second run; Rhino names in memory `rhino-fieldandmethods-gotcha`):

- No player on a dedicated server: Moonlight's soft-fluid item map stays empty until a player joins or `/reload`
  (`/fdtest soupjar` fills it itself now). Villager trades are built at server start: trade script changes need a
  restart. Probe blocks reuse fixed spots: clear them first in a world that ran a probe before.
- KubeJS hides or renames some vanilla methods: `DamageSource.getEntity()` is `getActual()`, `hurt()` is reached
  through `attack()`, `ItemStack.setTag` / `hasTag` / `Entity.getUUID` are hidden (use `Item.of(id, n, nbt)`, `.nbt`,
  `getGameProfile().getId()`), `ItemStack.enchant()` returns a copy, `level.dimension` is a property. A recipe
  wrapper's `getType()` is the serializer id (`minecraft:crafting_shaped`).
- Uncaught Java errors (no JS `catch`): reading `VillagerTrades` (roll real villagers instead), a `const` inside a
  nested block that runs twice (use `let`). `ConfigTracker` is blocked by KubeJS's class filter (read the mod's own
  config statics).
- Zombies are undead: no Regeneration or Poison on them (use a pillager). Adding a placed feature to another biome
  in the same step (scratch biome modifier) crashes spawn generation with a feature order cycle.

## Queue

Empty: every entry below the 2026-10-22 refill ran in the 2026-10-22 second bulk run (results in the run log). The
entries as they were queued (commands, pass criteria, failure notes) are archived in
[scratch_queue_run_2026-10-22b.md](scratch_queue_run_2026-10-22b.md); the five probes stay in `tools/` for reruns.

## Run log

### 2026-10-22 second bulk run

patcherMod `creativeengineering-1.1.01.jar` installed at md5 4daad5bb, rebuilt in the run to ac3dd59c (stem fruit) and
a4d48336 (prismarine geodes), the last one in `mods/`; `moreMooblooms-1.1.0.jar` (c2df0ab6) installed in place of 1.0.0;
KubeJS working tree of 2026-10-22 plus the fixes below. Worlds: sqA3 (group A, three boots: base with the test
`[stem_fruit]` values, the SQ-GM-05 + SQ-OM-14 flips together, SQ-OM-15), sqB3 and sqB5 (group B), sqOM2 (group A,
SQ-OM-18 and a clean rerun of every probe after the last fix: ofprobe 214 / 0, fdtest 98 / 0, gmprobe 62 / 0,
omprobe 86 / 0, kotest 4 / 0). Every entry PASS:

| Entries | Result |
|---|---|
| SQ-DR-00 to SQ-DR-03, SQ-FL-01, SQ-FL-02, SQ-IN-01, SQ-IN-02, SQ-SOL-01 | PASS on the first run |
| SQ-MJ-01, SQ-MJ-02 | PASS (24 soups; the first run failed only because the soft-fluid map is empty before a player joins) |
| SQ-CO-01, SQ-CO-02 | PASS after fixes 1 and 2 (gambler's tea, Canned Coffee Berries and a mortar craft still named Respite's coffee berries) |
| SQ-GM-00 to SQ-GM-10 | PASS; SQ-GM-02 after fix 3 (no tool on-hit effect ever applied), SQ-GM-05 shows the two expected flips |
| SQ-SF-00 to SQ-SF-05 | PASS (17 / 0 after fix 5) |
| SQ-WC-00 to SQ-WC-05, SQ-CD-00 to SQ-CD-02, SQ-MB-00 to SQ-MB-03 | PASS. `[wild-crops]` also warns that 5 Terralith cave biomes are not in `wild_crops.json` (no crops there; not an error) |
| SQ-MB-04 | PASS through the holder: UA's `trees_river` grows the override (90 river leaves, 21 oak logs, 0 river logs). Worldgen itself gave no river tree at three rivers: the feature needs dry land inside the river biome and the samples had none (0 dry columns in 25 x 25) |
| SQ-GE-00 to SQ-GE-04 | PASS; SQ-GE-03 after fix 6 (prismarine geodes were moved too). 23 of 24 ocean geodes moved, tops 10+ blocks under the floor; 32 land geodes kept their height; system off: 0 moved |
| SQ-OF-00 to SQ-OF-12 | PASS (`[cans] registered 228`); SQ-OF-03's trade half after fix 4 (butchers still sold Canned Food: 24 of 240 level 2 offers, now 0) |
| SQ-OM-00 to SQ-OM-18 | PASS. SQ-OM-08: 2 of 8 placed villages have a winery. SQ-OM-14: weight 20, 6-layer piles, no Glowing, 1200 of 1200 baby squid. SQ-OM-15: no `[vinery-houses]` line, template unknown. SQ-OM-16: the Cherry Cream Soda bug stays open (no recipe makes it). SQ-OM-17: 8 `world_serverconfig` files equal to their sources, update build, both README steps name the changed files (after fix 8). SQ-OM-18: mod defaults 2000 / 160 / 2000, then 0 / -1 / 0 from the world's serverconfig |

Fixes made in the run (pack and jar):

1. `R.replaceInput` / `R.replaceOutput` (NCH) take `includeAdded`: KubeJS 1902.6 swaps only recipes loaded from data,
   never script-added ones. Item merges pass it (`[itemMerges] 2 swaps in script-added recipes`: the two Canned Coffee
   Berries recipes). Opt-in because a swap replaces the whole matching slot (sugar -> `#forge:sugar` turned a
   `[sugar, honey]` slot into the tag alone when it ran on added recipes).
2. `item_merges.js`: Respite's kettle has no KubeJS schema, so gambler's tea is removed and rebuilt with `R.frBrewing`
   on Rustic's beans. `mortar.js` skips catalog entries naming a purged item (`[mortar] 6 of 7`).
3. `tool_effects.js` and `sample_syringe.js`: `event.getSource().getEntity()` threw (KubeJS name `getActual()`), so no
   on-hit tool effect and no syringe hit sample ever happened.
4. `trades.js`: MoreJS 0.3.0 reads `[1, 5]` as level 1 only (both ends from the first entry); every level range is a
   real `IntRange(1, 5)` now, so the cleric, chef and engineer removals and the Canned Food filter reach levels 2 to 5.
5. patcherMod `StemExtraFruit`: grows only the stem's own fruit (Delightful's slices and cantaloupe extend the vanilla
   blocks and report the vanilla stem; re-pointing only knew the real fruit).
6. patcherMod `OceanGeodes.untouched` checks the alternate inner layer too (Geode Plus' prismarine geode keeps its
   budding prismarine there; the inner layer is plain prismarine).
7. `jeed_providers.js` skips Xaero's map effects when the mod is not loaded (12 `[NCH] jeedProvider failed` errors on
   every dedicated server boot).
8. `tools/build_deploy.py`: runtime rules for `mortar_catalog.json` and `placed_food_slices.json` (the release
   preflight refused to build without them). New `tools/changelog_test/scratch_deploy_build.py` runs the release
   build into a scratch folder (SQ-OM-17).

Probe fixes (all in `tools/`): `food_drinks_probe.js` (fills Moonlight's map, clears jar spots), `gear_misc_probe.js`
(pillagers for shields, `attack`, dimension key, game-profile id, sack NBT), `farm_worldgen_probe.js` (real stem fruits,
prismarine by alternate layer, only blocks a placement added, reach check against the floor, new `zfw rivertrees R`),
`older_food_probe.js` (inedible count, grid type ids, can recipes not meals, quiche reassembly, purged keys, butcher
trades from real villagers), `older_misc_probe.js` (enchant copy, `attack`, Xaero skip, Unlit Campfire statics, `let`).

### 2026-10-22 bulk run

patcherMod `creativeengineering-1.1.01.jar` md5 37bb7049 (rebuilt in this run for the Trinket Case change), KubeJS
working tree of 2026-10-22. Group A (worlds sqA1, sqA2) and group B (sqB1, sqB2). All passed; results are in the
writeups:

| Entries | Result | Recorded in |
|---|---|---|
| SQ-MG-00 to SQ-MG-07, SQ-MG-10 to SQ-MG-15 | PASS (the SQ-MG-10 `[jeiRemove]` lines were scan false positives, fixed in `recipe_scan.js`) | [item_merges.md](../item_merges.md) 4.1 |
| SQ-TC-00 to SQ-TC-02 | PASS after a change: shulker boxes were accepted through Botania's curio check; user decision: any item, only the open case refused | [fixes_compat.md](../fixes_compat.md) 2.25, FX-21 to FX-24 |
| SQ-BK-00 to SQ-BK-06 | PASS | [oven.md](../oven.md) 0, 8 |
| SQ-SB-01 | PASS, 10 of 10 | [oven.md](../oven.md) OV-32, [can_parity.md](../can_parity.md) CP-26 |
| SQ-GF-01 | PASS at recipe level (`tools/golden_food_probe.js`); crafting in game (RC-70) | [recipes_cooking.md](../recipes_cooking.md) RC-70 |
| SQ-CL-01 | PASS for Canned Dough / Shulker / White Strawberries values, mortar corn, rock salt (hand, crushing, milling, mortar: 4), raw tortilla (grid, fan, bottler), pie filling crafts manual only (`tools/changelog_claims_probe.js`). Not checked: heated-mixing salt chance, cauldron salt bonus (in game) | this log |
| SQ-CF-01 | PASS with the deploy's `defaultconfigs` (every value in the new world). Spice of Life warns for 4 Large Meals `complexityUnparsed` entries (placed meals are not food items, so the entries do nothing) | this log |
| SQ-LP-00 to SQ-LP-06 | PASS | [mob_block_tweaks.md](../mob_block_tweaks.md) 2.5, MT-24 |

Probe fixes made in the run (all in `tools/`): `merge_probe.js` (getType crash, namespaced ids), `merge_core_probe.js`
(getStacks, tag check), `sweet_berry_pie_probe.js` (getType, getStacks), `baking_probe.js` (oven slots follow
`ovenIngredientCounts`), `trinket_case_probe.js` (new rules, boxed Boolean), `leaf_pile_probe.js` (scheduleTick
overload, Vinery leaves set non-persistent). New: `golden_food_probe.js`, `changelog_claims_probe.js`.
