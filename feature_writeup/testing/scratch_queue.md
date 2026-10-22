# Scratch test queue

Live checks waiting for a bulk scratch-server session. A bulk session groups the entries by world group, runs each
group on one server (`tools/creative_labels_test/scratch_server.sh`, memory `scratch-server-verify`), then marks
each entry PASS / FAIL here and moves the result into the feature's writeup. One game JVM at a time: check
`pgrep -c java` is 0 before booting (never `pgrep -af`).

Status word for a feature with entries here: "built, scratch test queued" (legend in
[../README.md](../README.md)).

Entry fields: ID, feature, target state, world group, commands, expected log, pass / fail, if it fails.

## World groups

| Group | Setup |
|---|---|
| A. superflat | `scratch_server.sh setup OUT` as is (superflat creative) |
| B. fresh normal world | `scratch_server.sh setup OUT`, then in `OUT/srv/server.properties` set `level-type=minecraft\:normal` and `max-tick-time=-1` before `boot`; a new level-name every run |

## Queue

### SQ-MG: item merges 2.6 (apple cider chain) and 2.7 (Froststone)

Feature: [../item_merges.md](../item_merges.md) 2.6, 2.7. Target: patcherMod `creativeengineering-1.1.01.jar` md5
26cf3425 (built 2026-10-22, in `mods/`), KubeJS working tree of 2026-10-22. Probe:
[tools/item_merges_probe/merge_probe.js](../../tools/item_merges_probe/merge_probe.js), copied before `boot` to
`OUT/srv/kubejs/server_scripts/zz_mg_probe.js`. It runs once at load and on `/mgtest`; every result is a
`[mgtest] PASS|FAIL ...` line in `logs/latest.log` (read with `grep '\[mgtest\]'`).

Client-only checks stay in game (no scratch server can show them): MG-16, MG-17, MG-20 (JEI tooltips), MG-21 names.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-MG-00 | A, B | boot | `Done (`; no `Mixin apply failed` / `InvalidInjectionException` naming `QuarkPermafrostStyleMixin` (common side; `JeiFluidHelperTooltipMixin` is client and is not applied on a server); no `Error occurred while handling event` from `cooking.js`, `item_merges.js`, `wandering_trades.js`; no `ieMixer` warning in `logs/kubejs/server.log` | Mixin: the target methods are `fillFloor` / `fillCeiling` on `vazkii.quark.content.world.undergroundstyle.base.BasicUndergroundStyle` (Quark-3.4-418), descriptor `(UndergroundStyleGenerator$Context, BlockPos, BlockState)V`. Scripts: `NCH.ieMixer` in `startup_scripts/func/neoCraftHelper.js`, the "Apple cider" block in `server_scripts/cooking/cooking.js` |
| SQ-MG-01 | A | none (load run) or `/mgtest` | PASS for the 5 ids `creativeengineering:createmixing/{long,strong}_apple_cider`, `creativeengineering:iemixer/{apple,long_apple,strong_apple}_cider`, and exactly one bottling recipe per cider item | Missing IE mixer recipe = IE rejected the json: the input fluid must be `{tag, amount}` (IE `FluidTagInput` reads only `tag`), items as `base_ingredient` + `count`. Compare against `data/tmted/recipes/mixer/apple_cider.json` in Engineers Delight. Two bottling hits for FD cider = tmted's removal didn't run |
| SQ-MG-02 | A | same | PASS: tmted's two cider recipes absent, `create_central_kitchen:mixing/apple_cider` present | `R.remove` ids in `cooking.js` |
| SQ-MG-03 | A | same | PASS: base tag holds only the base fluid; long and strong tags hold theirs | `server_scripts/tags/fluidtags.js` rows, `f.long_apple_cider` / `f.strong_apple_cider` in `startup_scripts/_lib/fluid_dict.js` |
| SQ-MG-04 | A | same | PASS: `creativeengineering:shapeless/froststone` makes 2 `quark:permafrost` | `server_scripts/other/item_merges.js`, dict keys `i.froststone*` (renamed from `i.permafrost*`, `item_dict.js` ~3319) |
| SQ-MG-05 | A | `/dumptrades 200`, then `grep -c 'fruitsdelight:apple_sapling' OUT/srv/kubejs/exported/villager_trades.json` | 0; other `fruitsdelight:*_sapling` still present in the `wandering_winemaker` pool | `economy/wandering_trades.js` Winemaker list; MoreJS rebuilds from a backup on `/reload`, so a stale pool means the script errored (SQ-MG-00) |
| SQ-MG-06 | B | `/locate biome minecraft:frozen_peaks` (say X Z); `/forceload add <X-32> <Z-32> <X+32> <Z+32>`; wait 10 s; `/mgpf X Z`; repeat at 2 more frozen peaks sites | `[mgtest] SQ-MG-06 ... quark 0, immersive_weathering N` with N > 0 at least once over the 3 sites (the cave style has rarity 2, so a site with 0 / 0 is no style there, not a fail) | `quark > 0`: the swap didn't run. Check `[worldgen.systems] permafrostMerge = true` in `OUT/srv/config/creativeengineering-common.toml`, and that Quark's module is on (`config/quark-common.toml` ~814-842). Never probe chunks before `forceload` (Rhino deadlock, memory `scratch-server-verify`) |
| SQ-MG-07 | B | after SQ-MG-06: set `permafrostMerge = false` in the srv toml, restart (live edits are not reloaded), forceload a new frozen peaks site, `/mgpf` | `quark N, immersive_weathering` only from IW's own surface patches | Off branch of `QuarkPermafrostStyleMixin.creativeengineering$swap` |

After the run: write the results into item_merges.md section 4 (MG-15, MG-19, MG-21 recipe parts, MG-22), set the
TODO rows `drinks #3` and `biomes #5`, and remove the passed entries from this queue.

### SQ-MG (core): item merges 2.1 to 2.5 (banana, milk bottle, chocolate cake, diced potatoes, rocky dirt)

Feature: [../item_merges.md](../item_merges.md) 2.1 to 2.5 (MG-01 to MG-14). Target: KubeJS working tree of 2026-10-22
(`startup_scripts/_lib/item_merges.js`, `server_scripts/other/item_merges.js`, `client_scripts/namechanges.js`); no
jar. Same boot as the SQ-MG entries above (group A; SQ-MG-00 covers the load errors). Probe:
[tools/item_merges_probe/merge_core_probe.js](../../tools/item_merges_probe/merge_core_probe.js), copied before `boot`
to `OUT/srv/kubejs/server_scripts/zz_mgcore_probe.js` (it can sit next to `zz_mg_probe.js`). Every result is a
`[mgcore] PASS|FAIL ...` line, then `[mgcore] done N pass, M fail` (33 checks).

Client or real-player checks stay in game: MG-01 (JEI hiding), MG-02 (inventory swap: the KubeJS inventory listener
is only attached to real players), MG-04 (monkey), MG-05 (leaves), MG-07 (milk cauldron), MG-13 and the MG-14 names.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-MG-10 | A | boot | `[itemMerges] 5 merges` in `logs/kubejs/startup.log`; no `[jeiRemove] <purged id> -> n recipe(s)` line for the five purged ids in `logs/kubejs/server.log`; no `Error occurred while handling event` from `item_merges.js` | A `[jeiRemove]` line names the recipe still using a purged item: add a removal or swap in `server_scripts/other/item_merges.js`. Startup errors: a dict key in `item_merges.js` resolving to undefined (`i.neapolitan_milk_bottle`, `i.decorative_blocks_rocky_dirt` in `item_dict.js`) |
| SQ-MG-11 | A | `/mgcore` | `merge list`, 5 `purged` lines (in `global.removedItems`, 0 item tags), `no recipe names a purged item`, 9 `removed` lines | Tags left on a purged id: `recipe_scan.js` tag stripping (priority -10000) ran before a script that re-added the tag. A recipe hit: `NCH.replaceInput` / `replaceOutput` don't reach that recipe type; remove it by id |
| SQ-MG-12 | A | same | banana: both tag lines, `eating a banana leaves a peel` (peels 1), `elephant chest` alexsmobs 0 and neapolitan > 0 over 300 rolls | Peel: `ItemEvents.foodEaten` in `item_merges.js` (the probe fires it through `finishUsingItem` + `ForgeEventFactory.onItemUseFinish`, as `tools/kitchen_probe/serve_items_probe.js`). Elephant chest: the LootJS `replaceLoot` modifier (`logName CE/item_merges`); neapolitan 0 with alexsmobs 0 is a probe miss (the table rolls bananas at weight 5): rerun |
| SQ-MG-13 | A | same | milk: tag line, bucket + 3 glass bottles -> 3 `farmersdelight:milk_bottle`, bucket + 3 FD bottles -> milk bucket | Bottles out as `neapolitan:milk_bottle`: the output swap missed `neapolitan:milk/milk_bucket_from_bottles`. No bucket recipe: FD's bottle missing from `#forge:bottles/milk` (`ServerEvents.tags` in `item_merges.js`) |
| SQ-MG-14 | A | same | cake: knife recipe 7 slices from `createaddition:chocolate_cake`, the CC&A block in `abnormals_delight:drops_chocolate_cake_slice`, 7 slices -> CC&A cake; potatoes: tag line and Rustic's cutting kept | Knife: `R.fdCutting(..., 'chocolate_cake')`. Slices -> cake making nothing: Abnormals Delight's `chocolate_cake_from_slices` output swap, or the cake rework (item_merges.md 2.3) removed it on purpose: check 2.3 before calling it a fail |
| SQ-MG-15 | A | same | rocky dirt: dirt + 2 pebbles -> 4 `davebuildingmod:rocky_dirt`, coarse -> 8; 2 dirt + 2 cobblestone -> 4 `twigs:rocky_dirt`, coarse -> 8; 3 Pebble Dirt -> 6 slabs | `via none`: the NCH ids `shaped/pebble_dirt*`, `shapeless/rocky_dirt*` did not load (grep `[NCH]`). The wrong block out: the swapped recipes in `item_merges.js` (Pebble Dirt is Dave's id, Rocky Dirt is Twigs') |

After the run: write the results into item_merges.md section 4 (MG-03, MG-06, MG-08 to MG-12, MG-14 recipe parts), and
remove the passed entries here. Then the in-game list above.

### SQ-TC: Trinket Case any item, Quark sort / search (fixes_compat 2.25)

Feature: [../fixes_compat.md](../fixes_compat.md) 2.25 (FX-21 to FX-24). Target: patcherMod
`creativeengineering-1.1.01.jar` sha1 6f5628e728 (built 2026-10-22, in `mods/`), Botania 440.2, Quark 3.4-418,
`config/quark-common.toml` Allowed Screens with `vazkii.botania.client.gui.box.BaubleBoxGui`. Probe:
[tools/trinket_case_probe.js](../../tools/trinket_case_probe.js), copied before `boot` to
`OUT/srv/kubejs/server_scripts/zz_tc_probe.js`. Run `/tcprobe`; results are `[tcprobe] PASS|FAIL ...` lines in
`logs/latest.log`, ending `[tcprobe] done: N fail`. It reads `[botania] trinketCaseAnyItem` and checks against it.

Client-only, stay in game: Quark buttons, search bar and sort keybind in the case screen (rest of FX-22), clicking a
stack onto the case item (rest of FX-21), trinket quick-equip (FX-23).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-TC-00 | A | boot | `Done (`; no `Mixin apply failed` / `InvalidInjectionException` naming `TrinketCaseInventoryMixin`, `TrinketCaseSlotMixin` or `TrinketCaseMenuMixin` (all three common side) | The `$1` anonymous class names or SRG names (`m_7013_` canPlaceItem, `m_5857_` mayPlace, `m_7648_` quickMoveStack) moved: re-decompile `BaubleBoxItem`, `BaubleBoxContainer` (vineflower `~/.cache/ce-tools/vineflower-1.10.1.jar`) |
| SQ-TC-01 | A | `/tcprobe` | `trinketCaseAnyItem = true`; canPlaceItem and slot mayPlace true for cobblestone, diamond sword, bread, aura ring, dirt rod, false for shulker box and Trinket Case; menu `IQuarkButtonAllowed`, `InventoryTransferHandler.accepts` true; shift-click moves 64 cobblestone in; sort keeps 50 bread / 80 cobblestone / 1 ring / 1 sword, packed to the front, case NBT reads back 132; `done: 0 fail` | `rules` = `TrinketCaseInventoryMixin` / `compat/TrinketCaseRules`; `slots` = `TrinketCaseSlotMixin`, or the interface on `TrinketCaseMenuMixin`; `shift` = its `@Redirect`; `sort` counts = `compat/QuarkSafeSort` over an `InvWrapper` of Botania's `ItemBackedInventory`. A Rhino `ERROR` line is usually a probe name (`containerMenu`, `Inventory.selected`): fix the probe |
| SQ-TC-02 | A | after SQ-TC-01: set `trinketCaseAnyItem = false` in `OUT/srv/config/creativeengineering-common.toml`, restart (live edits are not reloaded), `/tcprobe` | `trinketCaseAnyItem = false`; only aura ring and dirt rod accepted; menu still `IQuarkButtonAllowed`; shift-click moves nothing; `sort skipped`; `done: 0 fail` | A non-trinket accepted = a mixin ignoring `CEConfig.BOTANIA.trinketCaseAnyItem`; a trinket refused = the HEAD injects cancel when they shouldn't |

After the run: write the results into fixes_compat.md section 7 (FX-21, FX-22 server half, FX-24), set TODO row
`magic #3`, and remove the passed entries from this queue.

### SQ-BK: baking overhaul (oven.md 2.6)

Feature: [../oven.md](../oven.md) 2.6 (OV-25 to OV-31, OV-33's recipe): every cake, pie, cookie, pie crust and cake base recipe of
every mod replaced by the pack's (shared bases + 2 flavour), new Frosting and Cookie Dough items, pot pie fillings,
crust + filling in the oven and by hand, no Create / IE recipe for the bases, the oven refusing opened cans. Target:
patcherMod `creativeengineering-1.1.01.jar` md5 64a97553 (rebuilt 2026-10-22 01:02, in `mods/`; holds the
`BakingRecipe` opened-can check), KubeJS working tree of 2026-10-22 (`baking.js`, `cooking.js`, `canning.js`,
`startup_scripts/cooking/baking_ingredients.js`, `item_dict.js` keys `frosting`, `cookie_dough`). Probe:
[tools/baking_probe/baking_probe.js](../../tools/baking_probe/baking_probe.js), copied before `boot` to
`OUT/srv/kubejs/server_scripts/zz_bk_probe.js`. Run `/bktest`; results are `[bktest] PASS|FAIL ...` lines in
`logs/latest.log` (plus `recipes for <item>: ...` listings), ending `[bktest] done: N PASS, M FAIL`.

Client-only, stay in game: JEI pages and the placeholder textures (frosting = cotton boll, cookie dough =
gingerdough), crafting by hand as a real player (opened can refused in the grid, bottles and buckets back), the oven
GUI and leftover slots: OV-25 to OV-31. The Sweet Berry Pie (cheesecake) can and names are OV-32's.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-BK-00 | A | boot | `Done (`; no `Error occurred while handling event` from `baking.js`, `cooking.js`, `canning.js`; no `[NCH]` warning from a `ceBaking`, `shapeless` or `fdCooking` call in `baking.js`; no `over 6 slots, dropped` line; no missing-item error for `creativeengineering:frosting` / `cookie_dough` | A `[NCH]` warning names the bad ingredient (a dict key resolving to undefined). Item errors: `baking_ingredients.js` or the two `item_dict.js` lines next to `cornmeal` |
| SQ-BK-01 | A | `/bktest` | `PASS item ...` for both new items | Same as SQ-BK-00 item errors; startup scripts only run at boot |
| SQ-BK-02 | A | same | `PASS audit: no other mod recipe ...` (0 left). Every `recipes for <item>:` line lists only `creativeengineering:*` and `*_from_slices` ids | Each leftover has its own `FAIL leftover recipe <id> (<type>)` line: add the item to `REMOVED_OUTPUTS` in `baking.js`, or the recipe is added after KubeJS (datapack). The removal filter is `{ output, not: [{ id: /_from_slices$/ }, { mod: 'creativeengineering' }] }` (a `not` list is an OR in KubeJS 6 `RecipeFilter.of`); a surviving old pack recipe lives in `cooking.js` / `canning.js` |
| SQ-BK-03 | A | same | `PASS ids: X of X expected recipes loaded` and `PASS ids: crust compacting recipe gone` | A `FAIL missing recipe` id: grep the load log for that id's `[NCH]` warning. Durian / pineapple ids: TODO `food/baked-goods #12` may have reworked them; check `baking.js` PIES before calling it a regression |
| SQ-BK-04 | A | same | every `automation:` line PASS (no `create:` / `immersiveengineering:` type for a cake base, baked base, crust, frosting or cookie dough; Create `AllRecipeTypes.shouldIgnoreInAutomation` true for each crafting one) | A crafting id without `_manual_only` (idOverride in `baking.js`), or another mod's machine recipe that SQ-BK-02 also flags |
| SQ-BK-05 | A | same | the 4 `oven:` lines PASS: apple pie from crust + 2 apples + sugar + honey bottle; crust + full apple filling can; NOT crust + a can at max-1 servings (probe logs `opened partial = true`); NOT without the pie tin | Opened can matching: the jar in `mods/` lacks the check (`javap -c` on `oven/BakingRecipe.class` must show `CanServings.isPartial`), or `withServings` did not mark it partial. Plain pie failing: `BakingRecipe.slotCounts`, or `[cooking] ovenIngredientCounts = true` in the srv toml |
| SQ-BK-06 | A | same | `PASS spout: chocolate cake filling recipe loaded` (OV-33's recipe half); `recipes for chocolate_cake:` lists `creativeengineering:createfilling/chocolate_cake_spout` | Missing: an `[NCH]` warning from `createFilling chocolate_cake_spout` in the load log (dict key `chocolate` in `fluid_dict.js` or `cake_base_baked`). Wrong type: the id was taken by another recipe. The Spout run itself stays in game (OV-33) |

A probe `ERROR` line is the probe itself (Rhino / API name: `rm.byKey` returns an Optional, `getResultItem` throws on
some special recipes and is skipped), not the feature.

### Restored 2026-10-22 (overwritten in a write race)

Several sessions created this file within two minutes on 2026-10-22; these four entries were lost and restored from their sessions' transcripts. Their first IDs collided (two SQ-01), so each now has a prefix like the sections above: SQ-GF-01 (was SQ-01, gold foods), SQ-CL-01 (was SQ-02, changelog claims), SQ-CF-01 (was SQ-03, server configs), SQ-SB-01 (was SQ-01, sweet berry pie can). Contents unchanged. The baking overhaul's entry (was SQ-02) was re-added by its session as SQ-BK above.

### SQ-GF-01 (group A) Gold foods accept rose gold (2026-10-22)

- **Feature:** TODO `food/meals #9`; [recipes_cooking.md](../recipes_cooking.md) F5, RC-70. User decision 2026-10-22:
  rose gold counts for every gold food; the machine counts stay 6 (mixer) and 5 (arc) of the grid's 8.
- **State:** `kubejs/server_scripts/cooking/golden.js` using `any_nugget_gold` / `any_ingot_gold` / `any_block_gold`.
- **Run:** boot, check `latest.log` for `Golden food starting` / `Golden food done` and no `Error occurred while handling
  event`. Then in a creative session: craft a golden carrot from 8 pack rose gold nuggets + carrot; mixer recipe with 6
  rose gold nuggets (heated); arc furnace recipe with 5 MCA rose gold ingots on an apple; sequenced assembly with a
  rose gold block.
- **Pass:** all four produce the golden food. Same four with vanilla gold also work.
- **Fail notes:** if rose gold is refused, the tags do not hold it (`metaltags.js`, MCA tags) - `/dumptags` then grep
  `forge:nuggets/gold`. A script error shows as `golden.js` in the log.

### SQ-CL-01 (group A) Changelog claims fixed in the 2026-10-22 audit

Spot-check the bullets that were wrong or new, by JEI recipe page or crafting:

| Claim in `unreleased.md` | Check | Expected |
|---|---|---|
| Pie from a pie filling can | Oven (pie tin): Pie Crust + full Canned Apple Pie Filling; same with an opened can | pie from the full can only |
| Pie fillings | Cooking Pot: 2 apples + 2 sugar, empty can in the bowl slot | Canned Apple Pie Filling |
| Can servings hand-only | Create mixer / mechanical crafter with a can recipe | refused; JEI hides Create tab for it |
| Mortar corn | Mortar and Pestle: 2 Maize, then 2 Wild Corn | 3 Cornmeal each (average) |
| Raw Rock Salt | by hand, Create crushing, milling | 4 Salt each |
| Raw Tortilla | 3 cornmeal over water bottle (bottle returns); fan washing; Bottler 250 mB | tortilla(s) as written |
| Canned Dough / Shulker / White Strawberries | give/eat | 8 servings not edible; 2 hunger; 7 hunger |

- **Fail notes:** the changelog was checked against code, not the game. A mismatch means fix the bullet (or the recipe)
  and rerun `python3 tools/changelog_lint.py`.

### SQ-CF-01 (group B; the config copy happens only when a world is created) New server configs reach a new world (2026-10-22)

- **Feature:** Spice of Life, Create, Sophisticated Backpacks, Stardew Fishing server configs
  (`configs_edited/server_config/`, shipped through `defaultconfigs/` and `world_serverconfig/`).
- **Run:** create a new world on a scratch server, then grep `world/serverconfig/` for the values.
- **Expected:** `solapplepie-server.toml`: `minFoodsToActivate = 10`, `resetOnDeath = true`, `queueSize = 256`;
  `create-server.toml`: `maxSchematics = 256`; `sophisticatedbackpacks-server.toml`: netherite `upgradeSlotCount = 8`,
  `lootingChanceIncreasePerLevel = 0.05`; `stardew_fishing-server.toml`: multipliers 2.0 / 3.0 / 5.0.
- **Then in game:** eat 9 different foods (no bonus), the 10th activates the tracker; die, history resets.
- **Fail notes:** an invalid line in the Spice of Life benefits (`attribute,generic.speed,2`, TODO
  `types-and-tuning #4`) logs a parse error on boot; the tier bonuses are WIP, so an error there is known, not new.
  Existing worlds keep their own copy; the deploy README's "Existing worlds" step covers those.

### SQ-SB-01 Sweet Berry Pie filling can (group A)

Feature: Farmer's Delight's cheesecake renamed Sweet Berry Pie and given a pie filling can like the other pies
([oven.md](../oven.md) 2.6, OV-32; [can_parity.md](../can_parity.md) 2.13, CP-26; TODO `food/baked-goods #12`, `#14`
for the follow-ups). Changed files: `tools/gen_can_types.py` (the can row) and its outputs (`can_types.json`,
`item_dict.js` generated block, `can_servings.json`, `food_types.json`), `server_scripts/cooking/baking.js` (PIES row),
`client_scripts/namechanges.js` (renames), `kubejs/assets/displaydelight/lang/en_us.json` (regenerated).

Probe: `tools/kitchen_probe/sweet_berry_pie_probe.js` (copy to `kubejs/server_scripts/zz_sbp_probe.js` on the scratch
server). Commands:

    sbptest

Expected in `logs/latest.log`: no `[canning]` or `[NCH]` warning naming `sweet_berry` during load, then 10
`[sbptest] PASS` lines and `[sbptest] done 10 pass, 0 fail`:

| Check | Pass when |
|---|---|
| can registered | `creativeengineering:canned_sweet_berry_pie_filling` exists |
| can food | 3 hunger, 0.3 saturation modifier per serving |
| can effect | exactly `minecraft:speed 600` |
| can servings | max 4 |
| 4 slices + empty can | crafting gives 1 filling can |
| full can + crust by hand | crafting gives `farmersdelight:sweet_berry_cheesecake` |
| opened can + crust by hand gives nothing | a can left at 2 servings gives no result |
| pot filling | one `farmersdelight:cooking` recipe makes the can, sweet berries among the inputs |
| oven pies | two `creativeengineering:baking` recipes make the pie, one of them from the can |
| no other sweet berry pie | no other registered item id is a sweet berry pie / tart / cheesecake |

Not covered by the server (client lang): the names. Offline check, already passing when queued:
`python3 tools/gen_display_delight_names.py --check` (exit 0) and
`python3 -c "import sys; sys.path.insert(0,'tools'); import lang_index as l; print(l.script_langs()['block.farmersdelight.sweet_berry_cheesecake'])"`
(prints `Sweet Berry Pie`). In the client: OV-32's name list (whole, slice, incomplete, both Display Delight plates)
and the oven baking the pie from a full can.

Failure notes:
- `can registered` FAIL: the startup item list did not pick up the new can. Check `startup_scripts/items/cans.js`
  reads `canned_sweet_berry_pie_filling` from `kubejs/generation_scripts/food/can_types.json` and the item_dict
  generated block has the key; a full restart is needed after a generator run.
- `4 slices + empty can` FAIL: canning.js threw on the recipe inputs (grep `[canning]`); the source key
  `sweet_berry_cheesecake_slice` must be in item_dict.
- `full can + crust` FAIL with recipe `none`: canning.js skips the hand craft when the can's `pie` key is missing from
  item_dict (warns `[canning] ... _pie skipped`). FAIL with a result on the opened can: the NCH `whole` guard
  (`neoCraftHelper.js` `_canGuard`, patcherMod `CanServings.isPartial`).
- `pot filling` / `oven pies` FAIL: `baking.js` PIES row `['sweet_berry_cheesecake', i.sweet_berries,
  'canned_sweet_berry_pie_filling']`; `R.fdCooking` / `R.ceBaking` ids end in `canned_sweet_berry_pie_filling_pot` and
  `sweet_berry_cheesecake_from_filling`.
- `no other sweet berry pie` FAIL: a mod added since 2026-10-22 registers one; rename or hide it (the user wants one
  sweet berry pie).
- A probe `ERROR` line: Rhino / API mismatch in the probe itself (same patterns as `tools/kitchen_probe/mortar_probe.js`),
  not the feature.

### SQ-LP: Immersive Weathering leaf piles with the pack's leaf decay (mob_block_tweaks 2.5, MT-24) (2026-10-22)

Feature: [../mob_block_tweaks.md](../mob_block_tweaks.md) 2.5 "Leaf piles" and its 2026-10-22 code check. Target:
patcherMod `creativeengineering-1.1.01.jar` (md5 starts 64a97553, in `mods/`), Immersive Weathering 1.2.10. Group A.
Probe: [tools/leaf_pile_probe.js](../../tools/leaf_pile_probe.js), copied before `boot` to
`OUT/srv/kubejs/server_scripts/zz_lp_probe.js`. Don't load `tools/leaf_decay_probe.js` with it. After boot:
`forceload add 288 288 351 351`, wait 5 s. Read results with `grep '\[zlp\]' OUT/srv/logs/latest.log`. Each line ends
with `cfg {...}`, the live config it ran under; check that first. Toml edits go in `OUT/srv/config/` with the server
stopped (live edits are not reloaded), then `boot` again on the same world.

| ID | Config (srv) | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-LP-00 | defaults | boot | `Done (`; no `Mixin apply failed` naming `LeavesDecayRangeMixin` or IW `LeavesMixin`; no `[zlp] ERROR` | `ERROR ... is not a LeavesBlock`: the fruit leaf id changed, so fix `ZLP_TYPES` in the probe (not a feature fail) |
| SQ-LP-01 | defaults (`leafPiles` true, `onlyWhenDisturbed` true, IW `spawn_entity_from_decay` 0.3) | `zlp tick` | `tick: leaves 20 -> 0, piles` 1 to 12 (binomial 20 x 0.3, mean 6), layers 1-5 each | `leaves 20 -> 20`: the scheduled tick did not decay (LeafDecayRange.afterTick, chunk not forceloaded). Piles 0: `LeafDecayPiles.afterDecay` not reached; check `IW_LOADED` and the `leafPiles` value in `cfg` |
| SQ-LP-02 | defaults | `zlp rt` | `randomTick: leaves 20 -> 20, piles 0` (random-tick decay is gated, so IW's random-tick hook stays idle) | Leaves gone: `cancelRandomDecay` not applied (mixin `m_213898_` HEAD) |
| SQ-LP-03 | defaults | `zlp tree`, wait 15 s | `tree after 300 ticks`: `logs 0`, `leaves 0`, `pilesGround` > 0, `pilesAir` 0 (piles fall) | `leaves` > 0: decay cascade stalled (MT-20 path). `pilesAir` > 0: IW piles are not falling blocks in this version, so drop "then falls" from the writeup and MT-24 |
| SQ-LP-04 | IW `immersive_weathering-common.toml` `[leaf_piles] spawn_entity_from_decay = 1.0` | `zlp tick` | `leaves 20 -> 0, piles 20`, all 5 types in `by type` | Fewer than 20 at chance 1.0: a type is on IW's blacklist or has no pile (`WeatheringHelper.getFallenLeafPile`, the two apple leaves are the suspects). Note which, but it's not a pack bug |
| SQ-LP-05 | chance 1.0 and CE `[leaf_decay] leafPiles = false` | `zlp tick` | `leaves 20 -> 0, piles 0` | Piles appear: the switch isn't read (`LeafDecayPiles.afterDecay`) |
| SQ-LP-06 | chance 1.0, `leafPiles = true`, `onlyWhenDisturbed = false` | `zlp rt`, then `zlp tick` | `randomTick: leaves 20 -> 0, piles 20` (IW's own hook in vanilla's path), then `tick: leaves 20 -> 0, piles 20` | rt piles 0: our HEAD inject broke IW's `randomTick` injection (look for `LeavesMixin` in the mixin log) |

Not covered (in game, MT-24): ground piles under standing trees (IW `leaf_piles_from_leaves`, random-tick driven;
forceloaded chunks with no player get no random ticks) and the decay particles (client). After the run: put the
toml values back, then record PASS/FAIL in mob_block_tweaks.md MT-24 ("scratch PASSED <date>" for the decay part) and
remove the passed entries here.
