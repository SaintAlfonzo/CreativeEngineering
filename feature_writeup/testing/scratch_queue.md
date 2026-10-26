# Scratch test queue

Live checks waiting for a bulk scratch-server session. A bulk session groups the entries by world group, runs each
group on one server (`tools/creative_labels_test/scratch_server.sh`, memory `scratch-server-verify`), then marks
each entry PASS / FAIL here and moves the result into the feature's writeup. One game JVM at a time: check
`pgrep -c java` is 0 before booting (never `pgrep -af`).

Status word for a feature with entries here: "built, scratch test queued"; once its run passes, "built, scratch
server PASSED" (legend in [../README.md](../README.md)).

Refreshed 2026-10-25 against the unreleased changelog: new entries SQ-MDP-05 to -13, SQ-RM, SQ-SK, CQ-NEW-04 to -06;
SQ-FN and the Round 0 / jar-state notes corrected. Every change a player can notice in `unreleased.md` now has a row.

Run results live in the Run log at the bottom, not in the entry rows. Latest: the 2026-10-26 afternoon run (group B nb2: dumps, generators, SQ-SNF, SQ-PF, SQ-INV-01 to -06; perf mixin fix). Before it, the 2026-10-26 run (group A qb1 and one group B
boot nb1, patcherMod 1.1.03 and the 2026-10-25 23:59 jars) passed the Creative Delight boot, Abnormals, Storage, End's server half and
Farmer's Structures placement entries; its "Not run" row lists what is still open (group B worldgen counts and all client rows).
Jars in `mods/` now: patcherMod 1.1.03 (2026-10-26 14:32), CD, Vintage -06, Incubation Additions 1.1.0, repackedStorage; copy them
into the scratch `mods/` before any run.

Entry fields: ID, feature, target state, world group, commands, expected log, pass / fail, if it fails.

## World groups

| Group | Setup |
|---|---|
| A. superflat | `scratch_server.sh setup OUT` as is (superflat creative) |
| B-nether. fresh normal world, Nether visited | As B; worldgen-registry changes need a new level-name; `forceload` the Nether chunks from a console `execute in minecraft:the_nether run forceload add ...` |
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
- Zombies are undead: no Regeneration or Poison on them (use a pillager).
- `/vtdtest` plants its crops at y = -60: group A only (in a normal world that is underground and every light check fails). Adding a placed feature to another biome
  in the same step (scratch biome modifier) crashes spawn generation with a feature order cycle.

## Queue

### Round 0: Fungi Delight port (added 2026-10-23, run first)

Feature: [../fungi_delight.md](../fungi_delight.md) §4.0 / §8 (FGD-), [../potions.md](../potions.md) PO-27, TODO
`ports/fungiport #1`. Target: `backportedFungi-1.3-ce-19.2-01` and patcherMod `creativeengineering-1.1.01` md5 378f7897
(mooshroom family, `brown_chance`, CEConfig `mushroomSpawns`). **Refreshed 2026-10-26:** `backportedFungi-...-01` is in
`mods/` and booted in the 2026-10-26 scratch run (`/fgdtest all` 135/136, the failure is the known truffle pie, TODO `fungiport #3`);
`mods/` holds patcherMod 1.1.03 (built and installed 2026-10-26 14:32), which supersedes `creativeengineering-1.1.01` and 1.1.02 in every row. KubeJS tree as of 2026-10-25 (mushroom_spawns, `_biome_rules.js`, wild_crops `step`, mob_spawns rules, cooking.js Fungi block,
baking.js truffle pie, compost rows, jei_info.js).

Before the run (game closed, `pgrep -c java` is 0): `./gradlew :patcherMod:installToMods --offline` (the Fungi jar is
already installed). **Never boot without the port jar:** `kubejs/data/creativeengineering/worldgen/placed_feature/wild/mushrooms/`
references `fungidelight:*` features and blocks, and a world fails to load without them. The probe exists
(`tools/fungi_probe/fungi_probe.js`, command `/fgdtest <part>`, output `[fgdtest] PASS|FAIL <part> ...`; copy it into
`srv/kubejs/server_scripts/` along with `zcount.js`, `zgoose.js`, `ztrades.js` when a row needs them); rows name the parts, and a
part the probe does not have yet is written during the run. Count worldgen blocks offline from region files (section palettes, the
`nether_blocks_scan.py` pattern) after Chunky pregeneration, not by loading chunks from KubeJS.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-FGD-00 | A | boot | `Done (`; `fungidelight` in the mod list; no mixin error naming `MushroomCowBreedingMixin`, `AnimalCanMateMooshroomMixin`, `HoglinMixin`; no `Unknown item`/`Unknown tag` naming `fungidelight`; `[item_dict]` missing list has no Fungi key; `[mob-spawns]` summary shows `mooshroom_mushroom_fields` matched with 0 changes and the 4 Fungi cow rules matched, none idle; `[mushroom-spawns]` and `[wild-crops]` load lines list the 8 mushroom rules and `rooted_dirt`, `truffle_dirt` (FGD-01, FGD-26 server half) | port README; SRG names in the patcherMod mixins (m_28928_, m_142606_, m_7848_); `_biome_rules.js` priority |
| SQ-FGD-01 | A | `/fgdtest recipes` | Upstream `fungidelight:inky_cap_salad`, `inky_cap_scrambled_eggs`, `truffle_ice_cream` gone; the 3 `_manual_only` grid meals and 7 `*_cooked_manual_only` heatless meals exist with the inputs in `cooking.js`; none in `createmixing`/`createcrafting`; 7 pot meals present; 3 `fungidelight:effect_stew` recipes whose output carries Luck / farmersdelight:comfort / Haste at 160 ticks; `creativeengineering:baking` truffle pie (crust, 2 truffles, 2 sugar); cutting board, crate and smelting recipes present; Create milling present (FGD-20, FGD-22, FGD-25, FGD-34, FGD-38 recipe level) | `cooking.js` Fungi block; `Item.of(tag, 2)` in `R.shapeless`; port `EffectStewRecipe` |
| SQ-FGD-02 | A | `/fgdtest tags` | `#forge:mushrooms/inky_cap` = inky cap, cleaned cap, cooked cap, colony; `/morel` = morel, colony; `/truffle` = truffle, slice; all three in `#forge:mushrooms`; no dish or crate in it; 5 `#forge:storage_blocks/*`; `#forge:alcohol` holds no Fungi-shipped ids; truffle in `#minecraft:piglin_food`; truffle dirt in `#minecraft:dirt` and shovel mineable; inky cap, morel, colonies in the Serene Seasons year-round and unbreakable-infertile tags | `cookingtags.js`; port `port_resources.py` tag output |
| SQ-FGD-03 | A | `/fgdtest compost` | `ComposterBlock.COMPOSTABLES`: inky cap, morel 0.65; truffle, colonies, 5 crates 1.0; huge blocks and edge 0.85; stems and veil 0.65; cooked cap 0.65; cleaned cap, slice 0.5; same after `/reload` (FGD-24) | `compost_values.js` rows |
| SQ-FGD-04 | A | `/fgdtest mobs` | `Pig.FOOD_ITEMS` holds brown, red, inky cap, morel, truffle and the Veggies / Vintage items (`isFood` on a spawned pig); hoglin `isFood(truffle)` true; wolf hunt goal present on a tamed wolf (goal list) (FGD-11, FGD-12 logic) | port `ModEntitiesEvents`, AT |
| SQ-FGD-05 | A | `/fgdtest brewing` | `PotionBrewing` mixes contain awkward+truffle->burrowing, burrowing+redstone->long, +glowstone->strong; `BrewingRecipeRegistry` has no Fungi entry; a brewing stand block entity with awkward + truffle brews Burrowing; Create mixer route listed in the recipe manager for `create:potion` (FGD-18, PO-27 recipe level) | AT on `addMix`; `enqueueWork` order vs `PotionMixDedup` |
| SQ-FGD-06 | A | `/fgdtest blocks` (setblock, bone meal through a fake player or `BoneMealItem.applyBonemeal`) | Bone meal on inky cap and morel on dirt in the dark grows the huge forms; refuses on rich soil; a veil chain under a 20-block overhang stops at 16 and refuses bone meal there; random ticks (`randomTick` x 2000) also stop at 16; inky cap / morel on rich soil becomes a colony after random ticks (FGD-04, FGD-05, FGD-35, FGD-31 block half) | port `InkyGooVeilBlock` `MAX_LENGTH`, `MushroomBlock` holder supplier |
| SQ-FGD-07 | A | `/gamerule mobGriefing true`; 40 adult pigs in a pen floored with podzol and truffle dirt (shipped chance 1 in 4000 ticks per pig); wait 10 min; then `false` and repeat on fresh blocks | true: some podzol becomes coarse dirt, items on the ground, no `Parameters not allowed` / loot error in the log; false: no block changes (FGD-09, FGD-33). If too slow, record and leave for the client round | port pig goal; Friendly Griefing (FP-04) result for pigs |
| SQ-FGD-08 | A | summon `fungidelight:inky_cap_cow` and `morel_cow`, `/summon lightning_bolt` on each; same on a red and a brown `minecraft:mooshroom` | Fungi cows unchanged (type and NBT); red becomes brown, brown becomes red (FGD-27) | `FDMCow.thunderHit`; patcherMod mixins must not touch `thunderHit` |
| SQ-FGD-09 | A | 20 pairs each, `{InLove:600}`, separate pens: red x brown, red x inky, inky x morel, red x red, inky x inky; a moobloom next to an in-love mooshroom | mixed pairs about 50 % each parent's kind; same-kind pairs their own kind (red x red may show a rare brown); moobloom: no calf (FGD-30) | `MooshroomFamily.pickCalf` (headless test passed); a Fungi cow's own breed goal searches only its class, so mixed pairs breed through the vanilla mooshroom's goal |
| SQ-FGD-10 | A | fake player eats a cleaned cap, then a pack alcohol item, then (fresh) a water bottle and a Potion of Swiftness; eat a durian item, then a potion | Weak Stomach after the cap and durian; Nausea II, Poison II, Weakness II for 0:10 on alcohol and potion; nothing on water (FGD-16, FGD-17, FGD-36) | `ModEffectEvents`; `#creativeengineering:food_type/drink/alcohol` contents |
| SQ-FGD-11 | A | restart, roll real farmers to level 3 and a wandering trader (trades are built at start) | farmer level 3 buys the 4 colonies and truffles for 1 emerald; wandering trader sells inky cap, morel, veil (FGD-39 trade half) | port `ModVillagerEvents` |
| SQ-FGD-12 | B (new world) | Chunky radius 600 around a dark forest, a swamp, mushroom fields, a taiga and a birch forest; count offline | inky cap only in dark forest and mushroom fields (plus underground); morel also in all 4 swamps; none at the surface in taiga or birch; dark-forest Fungi singles clearly rarer than brown / red; mushroom fields near upstream (huge inky about 1 in 3 chunks, huge morel 1 in 4); colonies of inky / morel on dirt or podzol (FGD-03, FGD-28 surface) | `[mushroom-spawns]` log lines (skipped, not in tag); CEConfig `mushroomSpawns` read; feature ids; placeable tags |
| SQ-FGD-13 | B, same world | count below y 50 and 13+ under the surface | inky and morel singles on stone, sculk or dirt floors, rare; colonies of all four only over dirt / moss / mycelium-type blocks, never bare stone; none under open sky (FGD-28 caves). Too few: raise the count or scan length (`mushroom_spawns.json`) | a load error naming `environment_scan` or `surface_relative_threshold_filter`: 1.19.2 codec field names |
| SQ-FGD-14 | B, same world | Chunky over a dark forest, a cold forest (taiga, grove) and a conifer biome that is neither; count `minecraft:rooted_dirt` near the surface and `fungidelight:truffle_dirt` | both in the first two, none in the third; truffle dirt sits inside rooted dirt (FGD-32, FGD-07 placement half). Rooted dirt but no truffle dirt: modifiers ran truffle before blob, merge the two rules into one with the features in order | `wild_crops.json` `step`; modifier order |
| SQ-FGD-15 | B, same world | in the same pregen count entities: `minecraft:mooshroom` with `Type:"brown"` vs all, inky cap and morel cows, per biome; then 20 `/summon minecraft:mooshroom` and 20 spawn-egg uses | dark forest about 50 % brown, mushroom fields about 25 %, at least 20 mooshrooms per sample; Fungi cows rare in dark forest, morel cows in swamps, both common in mushroom fields; summoned and egg mooshrooms all red (FGD-29) | `SpawnTuning.brownChance`, `[mob-spawns]` summary; `/dumpspawns` + `gen_spawn_table.py` with no "mod not loaded" |

Client half (round 2, one creative superflat world, after SQ-FGD-00 passes):

| ID | World | Commands | Pass | After |
|---|---|---|---|---|
| CQ-FGD-DUMP | creative superflat | `/dumpall`, `/dumpfood`, `/dumpnames`, `/dumpplaceable` | `fungidelight:` items in the dumps | `food_catalog.py --check`; remove `fungidelight` from `AWAIT_DUMP` and rerun `gen_can_types.py` (truffle, cooked inky cap, truffle pasta, creamy morel soup, inky caps salad cans); TODO `fungiport #2` |
| CQ-FGD-UI | same | JEI on a mushroom, a truffle, a cleaned cap; JEI on Burrowing and Weak Stomach; render both cows and a mixed-pair calf; wolf hunt particles and sounds; pig digging sounds | the 3 info pages; JEED descriptions (FGD-37); cow layers show their mushroom; sounds play (FGD-08, FGD-39) | `jei_info.js`; lang overlay |
| CQ-FGD-PLAY | same | eat each dish, mine truffle dirt with shovel / Fortune / Silk Touch, mill an inky cap, shear a Fungi cow, bowl on one, crates | FGD-06, FGD-07, FGD-13, FGD-14, FGD-19, FGD-21, FGD-22, FGD-25, FGD-40 | values feed `fungiport #2` |

Refilled 2026-10-23 (doc sweep): everything built after the 2026-10-22 second bulk run (last game boot 2026-10-22
23:34, before any of the jars below existed in `mods/`). Entries of the earlier runs are archived in
[scratch_queue_run_2026-10-22b.md](scratch_queue_run_2026-10-22b.md); the probes stay in `tools/`.

Refreshed 2026-10-25: `backportedVeggies-...-02`, `backportedVintage-...-03`, `backportedFungi-...-01`, `backportedMiners-...-01`
and `incubationAdditions-1.1.0` are in `mods/` and loaded in the user's client on 2026-10-25 03:19 (no registry error; Miner's
upstream data 182 files, 15 cup conversions), but **no dedicated-server boot has run with them**, so every row below is still
open. `server_mods/` still holds Veggies -01 and Incubation Additions 1.0.0. Still unbooted: the unintegrated add-on jars
(`ends_delight`, `Hostile_Delight`, `oceanic_delight`, `oceansdelight`; Miner's Delight is now the port). The KubeJS tree holds uncommitted work on top (cooking.js, baking.js, cookingtags.js, item_dict.js,
compost_values.js, can_types.json, food_types.json, wild_crops.json, soup soft fluids, Vinery grape bush `tries`).
Boot-time failures are likely: run SQ-VP-00 first and fix before the rest.

Before the run (game closed, `pgrep -c java` is 0): `./gradlew :patcherMod:installToMods --offline` (source changed since
md5 2d2d74ab); check `mods/` holds the jars above; `diff` the `mods/` set against `server_mods/`
(the scratch server reads `mods/` through `mod_sides.md`, so `server_mods/` staleness is a deploy matter, not a test one).
The Vintage probe exists (`tools/vintage_probe/vintage_probe.js`, command `/vtdtest <part>` or `all`, output
`[vtdtest] PASS|FAIL <part> ...` as `/fdtest` does); copy it into `srv/kubejs/server_scripts/`. A part an entry names that the
probe lacks is written during the run.

### Round 1: scratch server (no client)

#### SQ-VP: Veggies Delight and Vintage Delight ports

Features: [../vintage_delight.md](../vintage_delight.md) 4.0 / 8 (VTD-), [../veggies_delight.md](../veggies_delight.md) 4.0 / 8 (VD-),
[../can_parity.md](../can_parity.md) CP-27 to CP-31, TODO `ports/vintageport #1-#3`, `ports/veggiesport #1`, `food/ingredients #6, #13`.
Client-only halves are in round 2 (CQ-VP).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-VP-00 | A | boot | `Done (`; no `Error loading registry data` / `Not a JSON object` (1.1.02 died here: Vintage placed features used `tag` for FD's `biome_tag`, fixed in -03); no `Error occurred while handling event` from `cooking.js`, `baking.js`, `compost_values.js`, `item_merges.js`, `blocktags.js`; no missing-registry or `Unknown item`/`Unknown tag` line naming `vintagedelight`, `veggiesdelight`; no `dehydrated` anywhere; `[cans] registered 252` (228 + the 24 Veggies cans; the vinegar additive adds recipes, not cans); startup.log shows 134 vintage items, 68 blocks | Port: `development/backportedVintage` (README.md lists changes); data: `tools/port_resources.py` unknown-item report; Rhino: `item_dict.js` keys (134 + 89) |
| SQ-VP-01 | A | boot, grep `latest.log` | No `[NCH]` warning for a missing item/tag in `cooking.js` Meals and `baking.js`; `[mortar]`, `[itemMerges]` and `[wild-crops]` lines unchanged from the 2026-10-22 run except the new rules (9 Veggies + Vintage rules loaded; the 5 Terralith cave biome warning is expected); `[advancements] N left out` counts the two ports' files; `simplehats.json` has chef hat bag weight 0 and equals `configs_edited/root_config/config/simplehats.json` (VTD-33, VTD-34) | `removed_items.js`; grep the key in `OUT/srv/config/` before relying on the copy |
| SQ-VP-02 | A | `/vtdtest recipes` (Vintage), `/ofprobe` (existing, still 214 / 0 first) | Veggies cans CP-27, CP-28, CP-29 recipe level: `Canned Broccoli`, Cauliflower (with florets), Sweet Potato, Turnip, Zucchini (with slices), Baked Sweet Potato, Roasted Zucchini / Cauliflower, 2 pastas, 3 breads, 4 salads, 5 soups, sweet potato pie filling (4 slices, and 2 baked sweet potatoes + 2 sugar in the Cooking Pot) load with `_manual_only` ids; `_vinegar` additive recipe present on all 66 cans of CP-30 (count by id suffix) and takes `i.vinegar`; Vintage cans are NOT present yet (`AWAIT_DUMP`, they wait for CQ-DUMP) | `gen_can_types.py` section configs; `can_types.json`; NCH `cans` guard |
| SQ-VP-03 | A | `/vtdtest cooking` | VTD-48 to VTD-52, recipe level: cheese burger shaped on `vintagedelight` cheese slice with beef or vegetarian patty, deluxe burger shaped, one sequenced assembly each, none in `createmixing`/`createcrafting`; stuffed burrito on `#forge:tortilla`, no bread recipe, one assembly; PB & J and cucumber salad `_manual_only` ids; cheese pasta, pad thai, pickle soup grid routes exist and none in the unheated mixer; oat dough has no smelting/smoking recipe, 3 raw oats -> bread kept; oatmeal cookie only in `creativeengineering:baking`, no grid recipe; Cesar salad takes garlic or cheese, zucchini boat milk or cheese; fajitas wrap has no dough recipe (VD-20, VD-24) | `cooking.js` Meals, `baking.js`; Rhino: `getStacks()`, never `getType()` on all recipes |
| SQ-VP-04 | A | `/vtdtest tags` | `#forge:dough` holds oat dough (not cucumber dough); `#forge:salt` holds salt dust and The Salt's salt; `#vintagedelight:ghost_pepper_soil` = soul soil, rich soul soil, fertile soul soil (renamed Nulch, same id); `#forge:crops/*`, `#forge:seeds/*`, `#forge:storage_blocks/*` for the 4 crops; Rustic roasted bell peppers tag holds the roasted peppers the burrito needs; Brewin' pickled pickles and kimchi no longer have keg recipes (VTD-16), the jar recipes exist (`R.vdPickling`) and take both salts (VTD-11) | `cookingtags.js`, `blocktags.js`, `alcohol.js` |
| SQ-VP-05 | A | `/vtdtest compost` | `ComposterBlock.COMPOSTABLES` holds: wild oats / peanuts / cucumbers / ghost peppers and the 6 Veggies wild crops 0.65; the 3 Vintage crates, the 6 Veggies crates and the oat bag 1.0; pickled egg 0.6, century egg 0.9; a produce and a seed of each port (VD-34). Same table after `/reload` | `compost_values.js` rows (startup reload plus `/reload`) |
| SQ-VP-06 | A | `/vtdtest fluids` (needs `/reload` or a player first, see setup notes) | Soft-fluid map holds 32 soup fluids (the 24 old + pickle soup, ghostly chili, oatmeal + the 5 Veggies soups), `nut_milk`, vinegar, the 8 jar contents, carrot juice, dandelion juice, turnip water, fermented garlic honey; each has an item in and out; carrot juice / dandelion juice / turnip water have their tank fluids as equivalents (a jar of carrot juice fills from a `creativeengineering:carrot_juice` tank); `gen_soup_soft_fluids.py` kept the hand-written drink fluids (VD-14, VD-21, VTD-26, VTD-53 jar half) | `gen_soup_soft_fluids.py`, `/cegen run soup_soft_fluids`; `kubejs/data/creativeengineering/moonlight/soft_fluids/` |
| SQ-VP-07 | A | `/vtdtest fluidsrun` | Chorus-style check for the three new fluids: `creativeengineering:carrot_juice`, `dandelion_juice`, `turnip_water` registered and `#forge:fluid/*` tagged; recipes `createmixing`, `createfilling`, `createemptying`, one `iebottling` per fluid at 250 mB, pot route `_manual_only` with VD's inputs (VD-14) | `fluids.js`, `fluid_dict.js`, `fluidtags.js` |
| SQ-VP-08 | A | `/vtdtest pickling` | Pickling Jar placed with `/setblock`; items inserted through the block entity's item handler from the top, container from a side, result from the bottom; 5 cucumbers + salt -> pickle with both salts; a honey-bottle recipe (fermented garlic honey) runs and the output is 1 jar item; known gap: the bottle is not returned (`vintageport #4`, expected FAIL on remainder, record only); the jar accepts any egg + coal + salt -> century egg after 3 hours (use `/time add` if the recipe counts game time, else record the ticks field) | Port `PicklingJar*`; if inserts do nothing the capability side mapping is wrong (inputs top, container sides, outputs bottom) |
| SQ-VP-09 | A | `/vtdtest crops` | Plant check by `canSurvive`: ghost pepper seeds survive on soul soil, rich soul soil, fertile soul soil; refuse nylium, farmland, dirt, grass (VTD-03); oats and peanuts survive on farmland, Rich Farmland, Fertile Soil (VTD-01); cucumber and Veggies crops likewise (VD-01); a ripe crop right-click (fake player) harvests and drops to age 5 (cucumber, VTD-02), ghost pepper (VTD-03), zucchini back 3 ages (VD-25, VTD-04); vanilla hoe area harvest note only (hoe code is client-ish) | `crops.md` rows (still unwritten), `blocktags.js`, Quark replant interplay |
| SQ-VP-10 | A | `/vtdtest mobs` | VTD-31 / VD-33: `Chicken.FOOD_ITEMS`, `Pig.FOOD_ITEMS`, `Parrot.TAME_FOOD`, `Villager.WANTED_ITEMS` contain both ports' items and the pack's own; `animal.isFood(stack)` true for a chicken with oats / VD seeds, pig with the produce, parrot with VD seeds; Quark troughs read the same; no conflict between the two ports. Static-field reads may be blocked by Rhino, then use `isFood` on spawned entities | `rhino-fieldandmethods-gotcha`; port `ModMobsEvents` |
| SQ-VP-11 | A | `/vtdtest loot`, `/loot spawn` on each Vintage table | VTD-30: none of the 8 Vintage chest tables is in any vanilla or pack chest table (scan `exported/loot` after a server `/dumpall`); `/loot` can still roll them; VD-31: the Veggies injections appear only as `L.add` entries at 0.015 to 0.04; the 6 VD crates are `L.pack`; VD-32 farmer level 1 and wandering trader common pool carry the VD items (trade changes need a restart; roll real villagers) | `loot/07_rules.js`, `trades.js`, `wandering_trades.js` |
| SQ-VP-12 | A | `/vtdtest stacks`, `/vtdtest effects` | VTD-27: no Dehydrated effect, potion, brewing mix or salt-dust awkward potion step; the 6 brews' effects are vanilla ids; VD-18 stacks only after CQ-DUMP (defer) | Port Java; `no dehydrated string` check on the jar |
| SQ-VP-13 | A | `/vtdtest egg` after setting `goldenEggChance` to 1 in `config/incubation_additions-common.toml` and a restart | NE-15, NE-16 logic: a goose (Incubation Additions) with a goose nest in a fenced pen places a Golden Egg block next to itself at lay time (since 2026-10-25; NE-17: never on the goose, never over a torch / flower / rail, falls when undermined) (400-tick window), never walks to the nest, the nest stays empty; with chance 0 geese lay into nests as before; a goose against a wall with open ground within 3 blocks drops on open ground, in a closed 1x1 pen the item where it stands; never a golden egg in a goose nest. A Golden Egg block drops its reward table (VTD-09). Default config key `goldenEggChance` = 1000 (1 in 1000) | `incubationAdditions/.../goldenegg/DropSpot` (logic test 9 / 9 passes headless), `IAConfig`; the config toml appears after the first boot of 1.1.0 |
| SQ-VP-14 | B (new normal world) | `/zfw wild` extended with the 9 rules, then `/ceplace`, `forceload add` sampling | VTD-06 / VD-04 / VD-05: wild oats in cold and temperate grassland; peanuts warm and dry; cucumbers wet / cold-temperate grassy; broccoli, cauliflower, garlic temperate; sweet potato, zucchini warm dry; turnips only where sea beets grow; each places on grass, podzol, Rich Soil, Fertile Soil and Immersive Weathering soils; salt patches on beaches and stony shores; mature dandelion in the 61 biomes of `follows` and nowhere else, more often than wild crops (VD-06). `tools/test_wild_crops.sh` and `test_wild_flowers.sh` already pass offline | `wild_crops.json`, `crops_by_biome.md`, `gen_soil_compat.py`; Vinery grape bush `tries` changed 36 -> 33 (also check the four grape feature JSONs load: no `Failed to load configured feature` line) |
| SQ-VP-15 | B-nether (new world, worldgen registry change) | `forceload add` over a crimson and a warped forest; count blocks | VTD-07: wild ghost peppers generate in crimson and warped forests on nylium and nowhere else in the Nether (`nether_worldgen_rules.json` `literal` modifier); `no feature order cycle` crash is the known risk of adding a placed feature | `gen_nether_worldgen.py`; memory `nether-worldgen-decisions` |
| SQ-VP-16 | B | `/locate` or `/ceplace` a village, plain / desert / savanna / snowy / taiga | VD-30: Veggies depot houses appear in the 5 vanilla village styles at weight 4 x `mod/veggiesdelight` multiplier 0.5, in `pool_config.json` after the first world load; their chests are Lootr chests | `pool_tags.json`, port injection (normal priority, PoolTuner LOWEST) |
| SQ-VP-17 | A | boot | Fertile Soul Soil (renamed Nulch): recipe still crafts (3 netherrack, 2 scales, ash, magma cream, nether wart, blaze powder -> 6; Create mixing 2 netherrack, 1 scale) with the same item id; name is lang only | `sawdust.js` line 83; recipe review is a TODO (`composting #2`), not a test |

#### SQ-MD: Miner's Delight cave carrot worldgen (built 2026-10-24; runs with the merged port jar, which replaced the 1.1.2 jar in `mods/`)

Features: [../miners_delight.md](../miners_delight.md) 4.3, `kubejs/generation_scripts/worldgen/mushroom_spawns.json` rule `cave_carrots`,
placed features `kubejs/data/creativeengineering/worldgen/placed_feature/wild/cave_carrots/`. New world needed (only new chunks change).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-MD-01 | B (new world) | boot; Chunky pregen radius 400; count `miners_delight:wild_cave_carrots` offline by Y (region files, one histogram per 8 layers) and by biome | `[mushroom-spawns] ... 3 mod modifiers off` (FD brown, red, `miners_delight:cave_carrots`) in the log; no error naming `cave_carrots`; **zero blocks above Y 48**; counts per Y layer: core (-24..24) about twice the layers 25..44 and -64..-25, all clearly non-zero; none within 13 blocks of the surface; found in noise and carver caves alike | height cap: lower `max_inclusive` of `high.json` (origin is Y 44 + 1 offset + 3 patch spread); too few: `rarity_filter` chances in the three bands; carver caves empty: the `cave_air` entries in the scan and the predicate |

#### SQ-MDP: Miner's Delight merged backport (built 2026-10-24, jar and `miners_upstream/` in place since 2026-10-25)

Features: [../miners_delight.md](../miners_delight.md) 4.1, tests MND-01 to MND-33 (the rows below are their server halves).
The scratch server needs `backportedMiners-1.4.5-ce-19.2-01.jar` in `mods/` **instead of** `miners_delight-1.19.2-1.1.2.jar`
(same mod id; already so in the instance's `mods/`; the 1.1.2 jar is gone) and `miners_upstream/` (holds
`miners_delight-1.20.1-1.4.5-backport.jar`) next to `mods/`. Copy that folder into the scratch dir too. Rows 05 to 13 added 2026-10-25
to cover the changelog bullets of the Miner's foods, drops and potions; none has run.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-MDP-01 | A | boot | MND-01 server half: `Done (`; `Miner's Delight upstream data: 182 files`; `Cup conversions: 15 bowl foods`; no registry, mixin, recipe, loot table or loot modifier error naming `miners_delight` (in particular none for `scavenging_cooked_*` or `any_of`); CCK applies `CopperPotBlockEntityMixin`, `StickyBasketBlockEntityMixin`, `StickyBasketBlockEntityAccessor` without "target not found" | translator (`MinersTranslator`) or the CCK hook shapes (P6) |
| SQ-MDP-02 | A | boot without `miners_upstream/` | MND-04: startup stops with the message naming `miners_upstream` and the Modrinth link | `UpstreamJars.verifyOrFail` |
| SQ-MDP-03 | A | `/setblock` a lit campfire and a copper pot on it; `/data merge block` the pot's `Inventory` slots 0-3 with `miners_delight:cave_carrot`, `minecraft:red_mushroom`, 2 x `minecraft:brown_mushroom` (the cave soup recipe, 200 ticks, so 160 in the pot); wait 10 s; `/data get block` | MND-10 / MND-12 server half: the meal slot holds 2 `cave_soup_cup` and `Container` is `copper_cup` | `CopperPotBlockEntity.servedResult` or the cup table |
| SQ-MDP-04 | A | `/loot spawn ~ ~ ~ kill @e[type=squid,limit=1]` with and without a knife in the mainhand (a fake player or `/loot ... mainhand`) | MND-30 server half: knifeless squid gives tentacles 1-3; knife adds the squid item | the GLM types (`miners_delight:add_items` / `replace_items`) or `any_of` translation |
| SQ-MDP-05 | A | probe: count registry entries in namespace `miners_delight`; `/give @p` each of `hot_cocoa_cup`, `arthropod`, `cooked_arthropod`, `spider_leg`, `baked_spider_leg`, `moss`, `copper_carrot`, `crunchy_bar`, `nutritional_bar`, `golden_nutritional_bar`, `bat_cookie`, `bat_soup_cup`, `insect_stew_cup`, `bone_broth_cup`, `onion_soup_cup` | MND-02 server half: 69 items minus `hot_cocoa_cup`, which must not exist (changelog: removed); every other id exists; no `Unknown item` line from `item_dict.js` or the loot/recipe scripts naming `miners_delight` | `item_dict.js` keys; the port's drop of the cup (`MinersTranslator` or the item registry) |
| SQ-MDP-06 | A | `/setblock` a lit campfire and a copper pot; `/data merge block` the pot's slots for each recipe in turn: Takoyaki, Bat Soup, Insect Stew, a Bone Broth and Brewin' Creamy Onion Soup (inputs from the recipe JSON in `miners_upstream` data via the Copper Pot recipe type; recipe ids via a probe) | MND-10 / MND-12 / MND-13 server half: takoyaki cooks as a 1.4.5 recipe; Bat Soup, Insect Stew, Bone Broth and Creamy Onion Soup serve 2 cups per bowl (`bat_soup_cup`, `insect_stew_cup`, `bone_broth_cup`, `onion_soup_cup`) with container `copper_cup`; no recipe reads `farmersdelight:onion_soup` (MND-13); other bowl soups (e.g. a Farmer's Delight beef stew) still serve one bowl | cup table (`Cup conversions: 15`), `CopperPotBlockEntity.servedResult`, the `2 per bowl` list in the cup config |
| SQ-MDP-07 | A | fake player (a `/kubejs` probe or `/data merge entity` on a fresh fake): apply Poison II to the player, eat Nutritional Bar, repeat with Golden Nutritional Bar; eat Crunchy Bar, Moss, Copper Carrot, Bat Cookie (note use duration) | MND-21: Poison is removed by both bars and only by them; every food gives its nutrition and saturation (compare `food_values.js` / the food dump); Bat Cookie eats faster than a cookie (16 ticks or the upstream value) | `FoodProperties` translation; `food_values.js` overrides |
| SQ-MDP-08 | A | 300 x `/loot spawn ~ ~ ~ kill @e[type=spider,limit=1]` per case, with the mainhand a knife (`farmersdelight:iron_knife`) vs nothing; summon each spider with `Fire:200` for the burning case; same for `cave_spider` and `silverfish` | MND-30 server half: knife spider/cave spider: 1-3 `arthropod` each time, `spider_leg` about 40 % (spider only), `cobweb` sometimes; burning: `cooked_arthropod` and `baked_spider_leg` instead, no raw; silverfish with knife: 1 `arthropod` always, silverfish eggs about 50 %; without a knife: eggs only, about 5 %, no arthropod; bats and squids as SQ-MDP-04 | the GLM entries (`add_items` / `replace_items`) and their `on_fire` / knife conditions; `kubejs/data` loot overrides |
| SQ-MDP-09 | A | 400 x `/loot spawn ~ ~ ~ loot minecraft:chests/abandoned_mineshaft`; count `miners_delight:copper_carrot` and `miners_delight:cave_carrot` stacks | MND-33 / changelog "Abandoned mineshaft chests can hold Copper Carrots and Cave Carrots": both appear in some rolls (a few per 100); vanilla mineshaft loot (rails, iron, golden apple) unchanged in kind | No pack rule adds these (`loot/07_rules.js` has none): the port's loot modifier (`miners_delight` data in `miners_upstream`) must have reached the table; check the modifier is in `forge:loot_modifiers/global_loot_modifiers` and has a weight under the pack's loot ceilings |
| SQ-MDP-10 | A | `/setblock` each feast (stuffed squid, fake meatloaf, glazed arachnid limbs); right-click serving through a fake player holding a bowl; break one with a knife-less hand; place a sticky basket under a dropper and hopper | MND-20 / MND-14: the three feasts place; each serves its portions to the right container and leaves the leftover; the sticky basket collects an item and moves it with its cooldown; the empty copper cup on water source gives a water cup, on a cow gives a milk cup, on a fish does nothing (MND-16) | feast block entity / container keys in the translator; CCK transfer cooldown (`StickyBasketBlockEntityMixin`) |
| SQ-MDP-11 | A | probe `PotionBrewing.POTION_MIXES` and `/brew` through a brewing stand block entity (awkward + copper carrot) | MND-22: awkward + copper carrot -> `miners_delight:haste`, + redstone -> long, + glowstone -> strong; haste + fermented spider eye -> mining fatigue, and redstone / glowstone on mining fatigue give its long / strong; exactly one mix each. These are the port's own potions (`miners_delight:haste`), separate from Rustic Delight's `rusticdelight:haste` line, which `potions.js` makes item-agnostic (memory `rustic-delight-haste-potions`): both brews work and the two do not share a mix. `potions.js` has no Miner's handling, so record whether the Create mixer, bottler, potion bucket and tipped-arrow routes exist for the Miner's potions (expected: only if the item-agnostic pass reads port potions; a gap goes to TODO `potions/potions`) | the port's `addMix`; `PotionMixDedup`; `potions.js` RD_POTION_MIXES |
| SQ-MDP-12 | A | `/reload`, then check `latest.log` | MND-15 server half: after `/reload` and with a player later, the copper pot's cup table is on the server (the Cup conversions line appears once at boot and not again); no `Error occurred while handling event` naming `miners_delight` in server scripts | `UpstreamJars` reload hook |
| SQ-MDP-13 | A | probe recipe manager for ids containing `miners_delight` | Arthropod, Spider Leg, Bat Cookie, Bat Rolls, Squid Sandwich, Glow Ink Pasta, Vegan Patty / Hamburger / Wrap / Steak And Potatoes / Fake Meatloaf, Cave Hamburger, Tentacles On A Stick each have a recipe (cooking pot, cutting board, smoking, campfire or crafting); no recipe outputs a dropped item; Create mixing / crafting routes exist only where the pack's `cooking.js` adds them | `cooking.js` Miner's block; translator recipe conversion (recipe ids in `miners_upstream` data) |

#### SQ-AD: five unintegrated Delight add-ons (boot only)

Features: [../ends_delight.md](../ends_delight.md), [../ocean_delight.md](../ocean_delight.md), [../miners_delight.md](../miners_delight.md), TODO `ports/mod-additions #4-#12`. Nothing is integrated, so
the only server check is that they load beside everything else.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-AD-00 | A | boot | `ends_delight`, `hostiledelight`, `miners_delight`, `oceanic_delight`, `oceansdelight` all appear in the mod list; `Done (`; no registry, mixin or tag error naming them; recipes load (no `Failed to parse`, except Hostile Delight's `grollrecp` / `grolllrecp`: known upstream bug B1 in [../hostile_delight.md](../hostile_delight.md), removed at integration) | Remove the jar from `mods/` and say so in `mod_sides.md`; Farmer's Delight needs to be present |
| SQ-AD-01 | A | `/dumpall` (recipes, tags, loot) copied into `kubejs/exported/` | Dumps exist for the overlap work (`mod-additions #4-#6, #9`); tag overlap counts for tentacle, chorus, guardian written to the ocean and End pages | none |

#### SQ-FN: Oceanic Delight fishing net (added 2026-10-25)

Feature: [../ocean_delight.md](../ocean_delight.md) 5.1, TODO `dishes/ocean #1`. **Setup changed 2026-10-25:** the fishing net mixin
moved from patcherMod into the Oceanic Delight repack inside Creative Delight (creative_delight.md 14.3 B7). Run with the current
patcherMod and `creative_delight-1.0.0.jar` from `development/*/build/libs/`, `oceanic_delight-*.jar` removed (same run as SQ-CDJ-04).
`/loot spawn` gives the table the spawn position as origin, so the biome rules can be checked without a player.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-FN-01 | A | boot | Log has no `OceanicDelightFishingNetMixin` apply error, no `creativeengineering:biome_tag` / `fishing_net` loot parse error | Mixin: the procedure's `getBlock` descriptor changed (check the jar version). Loot: condition not registered (check `BiomeTagCondition.register` ran) |
| SQ-FN-02 | A (needs the biomes: B or `/execute in` a worldgen world) | tp to a warm ocean, a lukewarm ocean, a plain ocean, a frozen/cold ocean and a river; 500 x `/loot spawn ~ ~ ~ loot oceanic_delight:gameplay/fishing_net_on_kelp` in each, then 500 x `..._on_sea_grass` | Kelp table: shrimp about 14 % in warm oceans (4 of 28), shrimp in the plain ocean, none in the cold ocean or river; caviar (`salmon_eggs`) only in rivers, about 13 % (3 of 24); coral or coral fan only in warm and lukewarm oceans; nautilus shell about 2 % and heart of the sea about 0.2 % in every ocean, never in the river. Sea grass table: caviar about 27-30 % in rivers, shrimp only in non-cold oceans; scute rare. Changelog numbers (shrimp 14 %, caviar 30 % / 13 %) must match | biome tags empty (`/dumptags`), or the condition reads no origin; weights in `fishing_net_on_*.json` |
| SQ-FN-03 | A | 400 x `/loot spawn ~ ~ ~ loot oceanic_delight:gameplay/fishing_net_on_sea_grass` and the same on kelp, in a river and an ocean | Junk about 5 % of rolls (about 20 of 400) in both tables and both biomes, every junk item one of Leather Boots, Leather, Bone, Bowl, Tripwire Hook, Rotten Flesh, String, Stick, Driftwood Log; no lily pad / water bottle / rod / ink sac | `fishing_net/junk.json`, the `random_chance` 0.05 pool |
| SQ-FN-04 | A | `/execute if block ~ ~ ~ #creativeengineering:fishing_net/kelp` (and `/sea_grass`) over `/setblock`ed `minecraft:kelp_plant`, `upgrade_aquatic:ochre_kelp_plant`, `polar_kelp_plant`, `thorny_kelp_plant`, `tongue_kelp_plant`; `minecraft:seagrass`, `minecraft:tall_seagrass`, `oceanic_delight:wild_sea_grape`, the sea grape plant, both pickerelweeds and their tall forms | All kelps in the kelp tag and none in the sea grass tag; the rest in the sea grass tag (changelog: every kelp, Seagrass, Sea Grapes, Pickerelweed) | block dict key typo (startup log `[block_dict] Missing`); `loottags.js` |

#### SQ-RM: removed foods, renames and data checks from the 1.1.03 changelog (added 2026-10-25)

Features: [../food_families.md](../food_families.md), [../ocean_delight.md](../ocean_delight.md), TODO `dishes/meals #19`. Names are
client work (CQ-NEW-04). Server halves only.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-RM-01 | A | boot; probe the item registry and recipe manager for `passion_aloe_nectar`, `cherry_cream_soda`, `dune_platter`, `yucca_gateau_slice`, `fruit_beignet` (the ids in `item_dict.js`) | Each is hidden or removed as `removed_items.js` does for the others (same outcome as an existing removed item); no recipe outputs or consumes any of them; no loot table or trade lists them; `[removed_items]` count rises by 5 and no `Unknown item` line names them | `removed_items.js` rows (lines 337-340); `R.remove` in `cooking.js` line 807 |
| SQ-RM-02 | A | probe recipe manager and loot for the renamed items: `oceanic_delight:salmon_eggs`, `fish_egg_roll`, `fish_egg_roll_slice`, `fish_egg_noodle_soup`, the Fried Rice item (`i.fried_rice` in `item_dict.js`) | Renames are lang only: ids, recipes and loot still resolve (the caviar from the net is the same item as before); the fried rice id is unchanged | a rename done with `item_dict.js` keys changed: stale references show as `[NCH]` or `Unknown item` lines |
| SQ-RM-03 | A | `/reload` and boot; `grep` `server.log` for `Orange Jam`/`orange_jam` recipes; probe `createmixing` and the cooking pot recipes | `orange_jam_from_slices` pot recipe takes 4 Orange Slices + 1 Lemon Slice + sugar; the mixer recipe is heated and takes 100 mB of water, gives 125 mB of jam; the 2-orange recipes still exist (changelog: "stay") | `cooking.js` orange jam rows; `fluids.js` (`f.orange_jam`) |
| SQ-RM-04 | A | probe the Create saw and the Farmer's Delight cutting board for stripping `upgrade_aquatic:driftwood_log` and `driftwood_wood` | Cutting board gives `immersive_weathering:upgrade_aquatic/driftwood_bark` (was Tree Bark); the Create saw has a stripping recipe with the same bark (was none); `abnormals_delight` and `create` compat driftwood cutting recipes absent | `bark.js`; SQ-LE-02 lists the ids |

#### SQ-SK: starter kit join (added 2026-10-25, needs a real player)

Feature: starter kit (`kubejs/server_scripts/other/starter_kit.js`). The script threw on every login in the 2026-10-25 client log
(`Failed to load Java class 'java.nio.file.Files': Class is not allowed by class filter`); the fix moved the tracking file to
`JsonIO` and `java.io.File`. A dedicated server has no player, so this is a server run with the user's client joining.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-SK-01 | A (client joins) | first join of a new player on the scratch server | `server.log` has `[starter-kit] <kit> kit given to <name>` and no `Error occurred while handling event 'PlayerEvents.loggedIn'`; `world/data/starterkit/tracking.json` exists and has the player's UUID under `multiplayer` | another filtered class in the same file: the log names it; `JsonIO.write` path |
| SQ-SK-02 | A | leave, rejoin | No second kit; `[starter-kit]` logs nothing new (tracking read back through `JsonIO.read`); delete the file and rejoin: the kit is given again only if the player lacks the marker tag, and the file is rewritten | `JsonIO.read` returns null on an empty file: the `parsed &&` guard |
| SQ-SK-03 | single-player world (round 2) | create a new world | Kit given once; `tracking.json` keyed by the world folder name under `singleplayer` | key logic for non-dedicated |

#### SQ-RS: removed-item recipe scan (redesigned 2026-10-23)

Features: [../recipe_progression.md](../recipe_progression.md) F2, RP-01; TODO `recipes/progression #2`. patcherMod jar
(new CEConfig key) is in `mods/`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-RS-01 | A | boot with default config; then `/recipescan`; then set `[debug.recipe_scan] runAtLoad = true` in the scratch copy of `creativeengineering-common.toml`, restart, `/reload` once | Default boot: no `[recipe_scan]` or `[jeiRemove]` line. `/recipescan`: chat line with the counts, `[recipe_scan] Scan complete. ... N recipes read, U unreadable` in `logs/kubejs/server.log`, entries end in `(input)` / `(output)`, no merged id (`global.itemMerges` first column) listed. Config on: exactly one scan after `Done (`, none after `/reload`. Note the scan time from log timestamps and N, U | `ServerEvents.loaded` not firing (try the command alone); `stack.id` on Rhino; a large U means `getIngredients` throws for a whole recipe class: log the class |

#### SQ-LE: low-effort TODO batch (added 2026-10-23)

Features: [../potion_buckets.md](../potion_buckets.md) PB-21 to PB-23, [../effects.md](../effects.md) EF-05, EF-12,
EF-20, [../recipes_cooking.md](../recipes_cooking.md) RC-79 to RC-81, [../recipes_wood.md](../recipes_wood.md) RW-21,
RW-22, [../structure_tuning.md](../structure_tuning.md) 3.24 (ST-225 to ST-227); TODO `potions/potion-buckets #4, #5`,
`potions/potions #5`, `recipes/cooking #3`, `recipes/wood #2`, `dishes/meals #17, #19`, `gameplay/misc #10`,
`blocks/framed-and-decor #9`, `ui/effects #6`, `ui/tooltips #6`, `baking/sweets #6`, `structures/rates-and-groups #10`.
All KubeJS / data / config: needs a restart (startup scripts), no jar.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-LE-00 | A | boot | `Done (`; no `Error occurred while handling event` from `potions.js`, `potion_area.js`, `pnc_tooltips.js`, `bark.js`, `misc_mods.js`, `cooking.js`, `potion_bottling.js`, `tipped_arrows.js`; no `[NCH]` missing item / fluid for `i.log_driftwood`, `f.orange_jam`, the bedroll keys; `[jeiRemove] Registered` count up by 22 | the named file |
| SQ-LE-01 | A | grep `latest.log` | `[CE potions] added N item-agnostic potion mixes` (N as before plus 2: the 6 luck rows stay) and MoreJS `removeByPotion` lines for the Buzzier Bees luck mixes; `potion bottling done (N recipes)` and `tipped arrow tipping done` counts lower than the last run's | `potions.js` section 2: MoreJS event timing (fires in client / dedicated setup) |
| SQ-LE-02 | A | recipe probe (`/recipescan`, or a KubeJS `RecipeManager` id dump) | Present: `creativeengineering:farmersdelightcooking/orange_jam_from_slices`, `creativeengineering:createmixing/orange_jam_from_slices`, `farmersdelight:{grilled_salmon,bacon_and_eggs,salmon_roll,cod_roll}`, `creativeengineering:shapeless/crab_bars_manual_only`, driftwood fdCutting / createSaw recipes with `immersive_weathering:upgrade_aquatic/driftwood_bark`. Absent: `rusticdelight:cooking/fruit_beignet` and its Slice & Dice copy, every `upgrade_aquatic:*bedroll` recipe, `design_decor:mechanical_crafting/enchanted_golden_apple`, `abnormals_delight:upgrade_aquatic/cutting/driftwood{,_log}`, `create:cutting/compat/upgrade_aquatic/driftwood{,_log}`, `quarkengineering:sawmill/*/*_stairs`, `creativeengineering:shapeless/{grilled_salmon,bacon_and_eggs,salmon_roll,cod_roll}`, every bucket / arrow recipe for `minecraft_water`, `_awkward`, `_thick`, `_mundane` | the named script |
| SQ-LE-03 | A | `/give` an awkward potion bucket, a Swiftness bucket and a blank bucket to a fake player or run `global.potionArea.spawn` from a probe on each stack | awkward: returns 0 rings, no cloud; Swiftness: rings, Swiftness cloud; blank: Night Vision cloud (PB-22) | `potion_area.js` NBT test (`getOrCreateTag().contains`) |
| SQ-LE-04 | A | probe `PotionBrewing.POTION_MIXES` | Exactly one mix each: awkward + clover -> luck, luck + redstone -> long_luck, luck + glowstone -> strong_luck, unluck + glowstone -> strong_unluck, awkward + silverfish bottle -> unluck, unluck + redstone -> long_unluck; none from luck + fermented spider eye (EF-05) | `removeByPotion` wildcard |
| SQ-LE-05 | A | probe Villager trades for the leatherworker | No bedroll in any leatherworker level (config `Leatherworkers sell bedrolls = false`) | `config/upgrade_aquatic-common.toml` |
| SQ-LE-06 | B (new normal world) | boot; `structure_tag_report.md`; `forceload` / census a desert square (the SQ-ST census probe) | ST-225: `desert_temples` 32 / 20 with the pyramid replacement, no config error; ST-226: no two desert temple / pyramid starts closer than 21 chunks; ST-227: `[placement-priority] dropped towns_and_towers:mimic_desert ... overlaps betterdeserttemples:desert_temple` if any overlap, never the reverse | `structure_groups.json` desert_temples |

Client halves (round 2, any client window): PB-21 JEI, PB-23 survival cooldown, EF-12 / EF-20 JEI, RC-79 to RC-81 JEI and
names, RW-21, RW-22 JEI, glycerol tooltip ("Tastes sweet!" in aqua without Shift, once with Shift), bedrolls and
Fruit Beignet hidden in JEI.

#### Pack seasonings framework (added 2026-10-23)

Feature: [../food.md](../food.md) 3.12, FO-45 to FO-48; TODO `food/seasoning #3`. patcherMod 1.1.02 jar (installed
2026-10-23 23:46, md5 57feef44) + startup / server scripts: restart.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-SN-00 | A | boot | `latest.log` has `[seasoning] defined creativeengineering:cheese`, `...:onion`, `...:garlic` and no `[seasoning] bad definition`; `server.log` has `seasoning done`, no `[NCH] season` warning; no `Parsing error loading recipe creativeengineering:shapeless/season/` | `development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/seasonings.js` load order (needs `global.item`, `global.effect`) or `NCH.season` JSON |
| SQ-SN-01 | A | recipe probe (RecipeManager id dump) | Present, type `creativeengineering:seasoning`: `creativeengineering:shapeless/season/{cheese,onion,garlic,garlic_onion}_on_corn_delight_popcorn` | serializer registration (`SeasoningRecipe.register`) |

#### SQ-SNF: seasonings by food family (added 2026-10-26)

Feature: [../food.md](../food.md) 3.12, FO-53 to FO-56; TODO `food/seasoning #3`, `#8`, `#9`. Needs patcherMod 1.1.03 and
Creative Delight rebuilt 2026-10-26 in `mods/` (not installed yet). Supersedes SQ-SN-01's recipe ids.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-SNF-01 | A | boot + tag probe (`/kubejs dump_tags` or the pack's tag dump) | `creativeengineering:seasonable/cheese`, `/onion`, `/garlic`, `/void_pepper` each hold exactly `corn_delight:popcorn`; no `seasonable/sugar` tag; `creativeengineering:food_family/snack` holds popcorn and tortilla chip, `food_family/fruit` holds `food_family/fruit/whole`'s members; no `[food families] could not read` or `[seasoning] ... has no members` line | `seasoning_tags.js`, `generation_scripts/food/food_families.json` present |
| SQ-SNF-02 | A | recipe probe (RecipeManager id dump) | Present, type `creativeengineering:seasoning`: `creative_delight:shapeless/season/{cheese,onion,garlic,void_pepper,garlic_onion}`; absent: any `..._on_corn_delight_popcorn`, any `sugar` season recipe | `seasoning.js` generation, `NCH.season` with a tag food |
| SQ-SNF-03 | A | craft probe (`SeasoningRecipe.matches` on a test grid, server script) | popcorn + flaxen cheese wedge matches; tortilla chip + cheese wedge and waffle + onion don't; garlic popcorn + onion matches | `Seasonings.accepts`, tag bound on the server |

#### SQ-PS: powdered sugar (added 2026-10-25)

Feature: [../food.md](../food.md) 3.13, FO-50 to FO-53; TODO `dishes/ingredients #16`, `food/seasoning #5`. patcherMod
jar built 2026-10-25 (not in `mods/`; `food/PowderedSugarRegistry`, NCH `viVacuumizing`) + startup / server scripts.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-PS-00 | A | boot | `latest.log` has `[seasoning] defined creativeengineering:sugar`, no registry error for `creativeengineering:powdered_sugar_block` / `_bucket`; `server.log` has `powdered_sugar done`, `seasoning done`, no `[NCH] viVacuumizing` / `foodGrinding` warning | registry (`PowderedSugarRegistry.register` in the mod constructor) or item_dict keys |
| SQ-PS-01 | A | recipe probe (RecipeManager id dump) | Present: `creativeengineering:vivacuumizing/powdered_sugar_bucket` (type `vintageimprovements:vacuumizing`, 4 ingredients: bucket + 3 sugar), `creativeengineering:shapeless/season/sugar_on_create_factory_waffle`, the `sugar_powdered_sugar` crushing / milling / mortar ids, `powdered_sugar_from_bucket`; `corn_delight:caramel_popcorn` still present | `NCH.viVacuumizing` JSON |
| SQ-PS-02 | A | `/kubejs tags item creativeengineering:powdered_sugar` (or tag dump) | Lists `forge:sugar` | `cookingtags.js` |
| SQ-PS-03 | A | `setblock ~ ~ ~2 creativeengineering:powdered_sugar_block`, `tp` a zombie into it, wait 20 s; `data get entity @e[type=zombie,limit=1] TicksFrozen` | Block placed; TicksFrozen 0 | `PowderedSugarBlock.entityInside` still sets the powder snow flag |

| SQ-PS-04 | A | `/kubejs hand` style probe or `give @p creativeengineering:powdered_sugar` | Item exists (it was not registered before 2026-10-25) | `baking_ingredients.js` item registry |

#### SQ-DB: dough blocks (added 2026-10-25)

Feature: [../oven.md](../oven.md) 2.6, OV-35; TODO `baking/baked-goods #27`. Startup + server scripts only (KubeJS).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-DB-00 | A | boot | No KubeJS startup error in `baking_ingredients.js`; `latest.log` has no missing model / texture for `creativeengineering:*dough_block` (client only; server: no registry error) | `.material('sand')` / `.soundType('slime_block')` names, item_dict keys |
| SQ-DB-01 | A | recipe probe (RecipeManager id dump) | 12 shapeless recipes: each of gingerdough, cookie_dough, sweet_potato_dough, oat_dough, cucumber_dough, sourdough 9 -> its block and block -> 9 | `cooking.js` dough block loop, item_dict keys |
| SQ-DB-02 | A | tag dump | Each block in `minecraft:mineable/shovel`, `forge:storage_blocks/<dough>`, `blockrunner:surface/spongy` | `storageblocktags.js`, `blocktags.js` |

#### SQ-TI: pack clock helpers and 1.1.02 client-class boot (added 2026-10-24)

Features: [../time.md](../time.md) 3.10 (TI-30), [../fixes_compat.md](../fixes_compat.md) 2.30 (FX-43 to FX-45).
patcherMod 1.1.02 (in `mods/`) added `XaeroTimeInfoMixin` (client only, `@Pseudo`, `require = 0`), `TimeSettings.clock24` /
`dayNumber` and the client-only `[moss]` config section (`block/MossTint`). The rendering is client work (round 2,
CQ-NEW); the server only has to boot with them and the clock math can be probed.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-TI-00 | A | boot | `Done (`; no mixin error naming `XaeroTimeInfoMixin` (Xaero is not on a dedicated server, so `@Pseudo` must skip it silently); no `ClassNotFound` / `NoClassDefFound` for `MossTint`, `net.minecraft.client` or `xaero`; `creativeengineering-common.toml` has `[moss]` `biomeTint`, `tintStrength` | `creativeengineering.mixins.json` client list vs common list; `MossTint` registration guarded by `FMLEnvironment.dist` |
| SQ-TI-01 | A | probe via KubeJS `Java.loadClass('com.SaintAlfonzo.creativeengineering.time.TimeSettings')` (allowed class?) or a `tools/` probe command: `clock(11800)`, `clock24(11800)`, `dayNumber(0)`, `dayNumber(22299)`, `dayNumber(22300)`, `dayNumber(46299)` with `[time] enabled` true, then false | enabled: `clock` "7:30 pm", `clock24` "19:30"; `dayNumber` is 1 at 0 and steps at the pack sunrise (dayTime 22300 mod 24000, i.e. -1700); enabled false: vanilla clock (`clock24` at 0 is "06:00"). Same numbers as the SeasonHUD "Day x/24" line | `TimeSettings.dayNumber` sunrise maths; TI-30 |

#### SQ-KT: plates and coasters on the Blockhead counter (added 2026-10-24)

Features: [../kitchen.md](../kitchen.md) 2.3 (KT-15, KT-16), TODO `cooking/kitchen #20`. patcherMod 1.1.02 (in `mods/`)
replaced `CuttingBoardSupportMixin` / `BlockStateCuttingBoardOffsetMixin` with `BlockStateCounterTopperMixin` (support +
model offset for every `#creativeengineering:counter_toppers` block) and added the client `CoasterRendererOffsetMixin`.
The tag was renamed `cutting_board_supports` -> `counter_supports`, so the board's own checks (KT-03, KT-05) rerun too.
2026-10-25: the same mixin also hooks `updateShape` (m_60728_) so Display Delight blocks and toppers pop when the block
below goes (KT-17); install the rebuilt 1.1.02 jar first.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-KT-00 | A | boot | `Done (`; no mixin error naming `BlockStateCounterTopperMixin` (the coaster renderer mixin is client only, not loaded here) | mixin target names (`m_60710_`, `m_60728_`, `m_60824_`) |
| SQ-KT-01 | A | copy `tools/kitchen_probe/cfb_kitchen_probe.js` into `srv/kubejs/server_scripts/`, `/kttest` | every `[kttest]` line PASS, in particular KT-03a/b, KT-05a/b/c (the board, unchanged behaviour) and KT-15a-d, KT-16a/b (plates, coaster, plated hamburger, corner; plate over air refused) | KT-15a FAIL with `above: minecraft:air`: tag not filled (`/kubejs` errors in `blocktags.js`, `addMatching` wildcard `displaydelight:*plated_*`); KT-16 FAIL: the per-state cache never saw the tag (`CounterToppers.init` / `TagsUpdatedEvent`) |
| SQ-KT-02 | A | same `/kttest` run | KT-17 PASS for all four (plate and plated hamburger on stone, plate and coaster on a counter): `above: minecraft:air` with drops listed | `above` still the plate: the `updateShape` hook did not apply (mixin error at boot) or `popsUnsupported` is false (namespace check); air but `drops: none`: loot table or `destroyBlock` flags |

#### SQ-KS: KubeJS scripts from packs and jars, calling the pack helpers (added 2026-10-25)

Feature: [../creative_delight.md](../creative_delight.md) 10.2 to 10.4 (Creative Delight scripts in the jar). Spike only, no CD
jar exists yet. Test script (write it in the scratch dir at run time): `ServerEvents.recipes(e => { console.info('[sqks] NCH ' +
typeof global.NCH + ' LOOT ' + typeof global.LOOT); global.NCH.<any shapeless helper>(e, ...) })` with one dict key output and
an `idSuffix` of `sqks_test`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-KS-01 | A | Put the test script at `world/datapacks/sqks/data/sqks/kubejs/test.js` (with a `pack.mcmeta`), boot | `server.log` lists a script pack `sqks` and `[sqks] NCH object LOOT object`; the recipe `creativeengineering:shapeless/sqks_test` exists (`/vtdtest`-style probe or JEI) | `loadFromResources` not reached: check the KubeJS build; `undefined`: `global` not shared, or the pack loaded before the folder pack |
| SQ-KS-02 | A | Same file moved to `kubejs/data/sqks/kubejs/test.js`; edit the log text; `/reload` | New text logged once after `/reload` | `kubejs/data` not served to `loadFromResources`: the dev loop of 10.4 needs another path |
| SQ-KS-03 | A | Both copies present (datapack and `kubejs/data`), different log text | Exactly one `[sqks]` line; record which pack won. Repeat with the jar copy once a CD or patcherMod dev build carries `data/sqks/kubejs/test.js` | Two lines: same-path override does not hold, so 10.4 needs a guard or a different dev path |
| SQ-KS-04 | A | Add a second file headed `// requires: not_a_mod` | Skipped, no error | Header syntax for this KubeJS build |
| SQ-KS-05 | A | Loader built 2026-10-25 (`JarStartupScripts`): covered by SQ-CDT-01 and SQ-CDT-02 (the CD test jar has `kubejs_startup/`) | as SQ-CDT-01 | as SQ-CDT-01 |

#### SQ-CDJ: Creative Delight nested jar (added 2026-10-25)

Feature: [../creative_delight.md](../creative_delight.md) 14.1 to 14.3. **Built 2026-10-25:** `creative_delight-1.0.0.jar`
(`development/creativeDelight/build/libs/`) nests the Ocean's, Oceanic and End's repacks; the four ports are not bundled yet
(SQ-CDJ-05 waits for them). Run with patcherMod from `development/patcherMod/build/libs/` (it holds the helpers now, so the
pack's `kubejs/startup_scripts/func/` no longer has NCH, tags or compost) and with `oceansdelight-*`, `oceanic_delight-*`,
`ends_delight-*` moved out of the scratch `mods/` unless the row says otherwise. Run SQ-CDT in the same boot.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-CDJ-01 | A | CD jar holding only the Ocean's repack in `mods/`, boot | `server.log`: `Found mod file repackedOceans-... of type MOD with provider ...JarInJarDependencyLocator`; `oceansdelight` items exist (`/give`); a world with Ocean's blocks loads with them | `metadata.json` path or format wrong; compare with Create's |
| SQ-CDJ-02 | A | Same, plus the stale `oceansdelight-1.0.2-1.19.2.jar` back in `mods/` (rerun 2026-10-25 rule: the top-level jar still wins, framework #10 only reports it) | Log has `[creative_delight] member oceansdelight is loaded from oceansdelight-1.0.2-1.19.2.jar (version 1.0.2-1.19.2), not from the Creative Delight jar` at ERROR, once; the SQ-CDJ-01 boot (no stale jar) has no such line for any member; `build_deploy.py` reports the duplicate id | No line: the nested-path check (`CreativeDelight.warnIfNotNested`, file system of the mod file) does not tell the two apart; a line in SQ-CDJ-01 too: nested jars are on the default file system after all |
| SQ-CDJ-03 | A | Client: mod list | Ocean's shows "Ocean's Delight (Creative Delight)" and CD as its own entry | Repack `mods.toml` template not applied |
| SQ-CDJ-04 | A | Add the Oceanic repack with the B7 overlay; use a fishing net in a tagged biome | Mixin config `oceanic_delight.ce.mixins.json` applied (no mixin error in the log); the net loot follows the biome tags as with the patcherMod mixin | Refmap or target name; check the overlay was reobfed |
| SQ-CDJ-05 | B | Full CD jar (four ports and three repacks) on a world holding Veggies, Vintage, Fungi and Miner's blocks | All seven mod ids load once; no missing-registry warnings; Vintage, Fungi and Miner's mixin configs applied; suites VD-, VTD-, FGD-, MND- unchanged | Per member: compare with that member's standalone jar |
| SQ-CDJ-06 | A | Same boot as SQ-CDJ-01 (the built jar nests End's too): `/give @p ends_delight:<any item>`; kill an enderman with a knife | End's items exist; the `add_*` / `nd_add_*` loot modifiers still drop; client mod list shows "End's Delight (Creative Delight)" | Compare with the upstream jar: the repack should be byte-identical apart from `mods.toml` and the manifest |
| SQ-CDJ-07 | A | Same boot as SQ-CDJ-01, with `FarmersDelight-1.19.2-1.2.4.jar` also moved out of the scratch `mods/` (the built CD jar nests the FD repack, creative_delight.md 21) | Debug log `Found valid mod file repackedFarmers-...`; no `farmersdelight` missing-dependency error for any of the 24 jars that need it; `farmersdelight.mixins.json` applied (no mixin error; rich soil under a sapling stays rich soil after it grows); patcherMod's FD mixins (remainder buffer) apply; a world with FD blocks (stove, cooking pot with contents, skillet, cutting board) loads with them; `config/farmersdelight-common.toml` still read; `/give @p farmersdelight:cooking_pot` | Mixin missing: the manifest lost `MixinConfigs` (`mixin_configs` in the member's `gradle.properties`); a dependent refuses the version: re-run the range check in creative_delight.md 21.1 |
| SQ-CDJ-08 | A | Client, same jars: mod list, config screen, a cooking pot GUI and its JEI category | FD shows "Farmer's Delight (Creative Delight)" with its logo; its config button opens; JEI cooking and cutting categories present; `creative_delight-common.toml` has no `farmersdelight` switch | Logo or config: the repack `mods.toml` template; a switch: `CDConfig.BASE_MEMBERS` |

#### SQ-CDT: Creative Delight framework suite (added 2026-10-25)

Feature: [../creative_delight.md](../creative_delight.md) 10 (startup loader, helpers in patcherMod's jar, NCH namespace, CD
script trees), tests CDT- in its section 19. Suite and how to load it: [tools/creative_delight_test/README.md](../../tools/creative_delight_test/README.md).
Build `./gradlew :patcherMod:build :creativeDelight:build -Pcd_test_suite --offline`; scratch `mods/`: the patcherMod jar and
`creative_delight-1.0.0-cdtest.jar`, the three upstream member jars removed. Boot once for SQ-CDT-01 to -07, then the rows say what
changes. Not run yet (owner: queue only).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-CDT-01 | A | Boot; `grep "jar-startup\|creative_delight\]\|cdtest\]" logs/kubejs/startup.log` | `[jar-startup] creativeengineering: 3 of 3 files`, `[jar-startup] creative_delight: 2 of 3 files` (the `requires: cdtest_not_a_mod` file skipped), `startup script packs in order: creativeengineering, startup_scripts, ..., creative_delight`; `Neo Craft Helper finished` once; `[creative_delight] core finished, members: oceansdelight, oceanic_delight, ends_delight`; no startup script errors | No `[jar-startup]` line: mixin not applied (`KubeJSStartupJarScriptsMixin`, require = 1 should have stopped boot); folder scripts erroring on `global.NCH` / `global.compost` undefined: patcherMod's pack not at the head |
| SQ-CDT-02 | A | `/cdtest startup` | All PASS: helpers and dicts before the CD pack, NCH apiVersion 1, ns at load `creativeengineering`, compost tiers > 0, requires honoured | Dict missing: the CD pack ran before the folder pack (order in `JarStartupScripts.addPacks`) |
| SQ-CDT-03 | A | Same boot: the pack's own startup and server scripts | `logs/kubejs/startup.log` and `server.log` have no more errors than the last clean boot (compare counts); `/dumpcompost` still writes a table; recipes from `kubejs/server_scripts/cooking/` present (`/cdtest mirror` twins exist) | A pack script used a top-level var of a moved helper (pack scopes are separate): give it a `global` |
| SQ-CDT-04 | A | `/cdtest ns`, `/cdtest requires`, `/cdtest tags`, `/cdtest config` | All PASS; one expected `[NCH] withNamespace failed and was skipped: cdtest deliberate error` line in `server.log` | Ids under `creativeengineering:`: `withNamespace` not used by `global.CD.recipes`; requires FAIL: header syntax in a data-pack script |
| SQ-CDT-05 | A | `/cdtest mirror` | Every `twin of creative_delight:...` PASS (same type, result, ingredients as the pack's recipe); `twins made` PASS; NOTE lists about 30 helpers | A twin differs: that helper builds something from the namespace besides the id; a twin missing: the call threw inside the CD block (grep `[NCH] ... failed`) |
| SQ-CDT-06 | A | `/reload`, then `/cdtest all` | Same results as before the reload (server scripts from the jar reload); the deliberate `[NCH] withNamespace failed` line logged again (`server scripts ready` is a `ServerEvents.loaded` line: start only, not on `/reload`) | Jar server scripts not reloaded: KubeJS keeps the first resource list |
| SQ-CDT-07 | A | Client on the scratch server (Round 2, or note it for CQ): hover a fishing net; F3+T | Tooltip `[cdtest] tooltip from the Creative Delight client tree`; `logs/kubejs/client.log` has `[cdtest] client suite loaded, global.CD object` after F3+T too | Client tree not read from the jar: check `assets/creative_delight/kubejs/` in the jar |
| SQ-CDT-08 | A | Stop; `config/creative_delight-common.toml` `[members] oceanic_delight = false`; boot; `/cdtest ns`, `/cdtest config` | `member row follows CD.on()` PASS with `CD.on(oceanic_delight) false` and no `oceanic_member_row` recipe; Oceanic items still exist | Switch ignored: `CDConfig.member` read before the config loaded at the wrong time |
| SQ-CDT-09 | A | Stop; folder path: normal `creative_delight-1.0.0.jar` in `mods/`, copy the suite's `data` and `assets` into the scratch `kubejs/`; boot; `/cdtest ns`, `/cdtest mirror`, `/cdtest requires`, `/cdtest tags` | Same PASS results as on the jar path (`/cdtest startup` FAILs `suite ran` by design) | The `kubejs/data` dev loop (10.4) does not serve CD's namespace: compare with SQ-KS-02 |
| SQ-CDT-10 | A | Stop; replace the patcherMod jar with the released `creativeengineering-1.1.02.jar` (no loader), keep the CD test jar; boot | `latest.log`: `[creative_delight] this patcherMod (creativeengineering) has no startup script loader`; the pack's own startup scripts fail on missing `global.NCH` (expected: that jar predates the move, so the two jars must ship together) | No CD error line: the class check in `CreativeDelight` |

#### SQ-FD: food dict loads in game (added 2026-10-25)

Feature: [../creative_delight.md](../creative_delight.md) 13.5, test CDT-08. Scratch `mods/`: the live pack's (no jar change; the
three `_lib` files are in `kubejs/startup_scripts/`). Any boot of the pack covers it, so it can ride on another row's boot.

| ID | World | Steps | Pass | Fail means |
|---|---|---|---|---|
| SQ-FD-01 | A | Boot; grep `logs/latest.log` for `food dict:` and `food_grammar.js` / `food_dict.js` errors; `/kubejs eval global.food.veggie_slice_cooked_potato` (or any startup-side print) | No `food dict:` error line; value `rusticdelight:baked_potato_slices` | A thrown check (the line names each bad key) or a script error: a KubeJS API the offline Rhino run did not have |

#### SQ-VTD5: Vintage port -05 (added 2026-10-25)

| ID | World | Steps | Pass | Fail means |
|---|---|---|---|---|
| SQ-VTD5-01 | A | `/setblock` a Cheese Wheel, Cheese Pizza and Meat Pizza at `bites=0` and one at `bites=2`; break each as a fake player with an empty hand; then one at `bites=0` by `/summon tnt` | 4 slices (wheel: `vintagedelight:cheese_slice`, pizzas: their own slice), 2 slices at `bites=2`, nothing from the blast (VTD-54) | Nothing at all: the `-05` jar is not in `mods/`, or `port_resources.py` was not rerun before the build |

#### SQ-VTD6: Vintage port -06 ghost pepper soil, Quark own-pick crops (added 2026-10-25)

Feature: [../vintage_delight.md](../vintage_delight.md) 4.0b, [../crops.md](../crops.md) 2.2. Scratch `mods/`: Vintage port
`-06`, patcherMod 1.1.03 from `build/libs` and the Creative Delight jar of the same tree (the own-pick tag is filled by CD
scripts), Veggies `-03`.

| ID | World | Steps | Pass | Fail means |
|---|---|---|---|---|
| SQ-VTD6-01 | A | `/setblock` farmland, Soul Soil, Rich Soul Soil, Nulch, grass and nylium in a lit row; `/setblock` `vintagedelight:ghost_pepper_crop[age=0]` on each, then a block update next to each; same with `vintagedelight:wild_ghost_peppers` | Crop survives only on the three soul soils; wild survives on those and nylium; the others pop off (VTD-75) | Survives on farmland: the `-06` jar is not loaded, or `canSurvive` is not the override |
| SQ-VTD6-02 | A | Grep `logs/latest.log` for `QuarkOwnPickMixin`; `/kubejs`-dump or `/dumptags` block tag `creativeengineering:crops/own_pick`; as a fake player with an empty hand, right-click ripe `cucumber_crop[age=7]`, `ghost_pepper_crop[age=7]`, `veggiesdelight:zucchini_crop[age=7]` | No mixin apply error; tag holds the three crops; each drops only its fruit (no seeds) and reads age 5, 5, 4 (VTD-76) | Age 0 plus seeds: the tag is empty (CD scripts not loaded) or the mixin did not apply |

#### SQ-VTD4: Vintage port -04, Veggies -03 and the first Creative Delight member scripts (added 2026-10-25)

Feature: [../vintage_delight.md](../vintage_delight.md) 4.0b, [../creative_delight.md](../creative_delight.md) 19.6, [../kitchen.md](../kitchen.md) KT-18.
Scratch `mods/`: `backportedVintage-0.1.6-ce-19.2-04.jar`, `backportedVeggies-1.9.2-ce-19.2-03.jar`, the patcherMod build
with the startup loader and `creative_delight-1.0.0.jar` (rebuilt after this change, so it holds the new trees and
`kubejs_startup/vintagedelight/`). Not run yet (owner: queue only).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-VTD4-01 | A | Boot; `grep "jar-startup" logs/kubejs/startup.log`; `grep -i "creative_delight\]\|vintage_items\|ghost" logs/kubejs/startup.log logs/kubejs/server.log` | `creative_delight: N of N files` counts the new `vintagedelight/vintage_items.js`; no errors from the CD files | Startup file skipped: `// requires: vintagedelight` header or the loader's walk |
| SQ-VTD4-02 | A | `/kubejs recipes` probe or the dumps: recipe ids under `creative_delight:` | `pickling/berserks_brew`, `pickling/leather_from_fermenting` (both with `#forge:salt`), `createcrushing/wild_ghost_pepper`, a milling twin (2 seeds + cyan dye), `iecrushing/wild_ghost_pepper` (3 seeds), `createmixing/slime_block_from_dough_block` (heated); gone: `vintagedelight:fermenting/slime_block_from_fermenting`, `.../berserks_brew`, `.../leather_from_fermenting`, `vintagedelight:salted_cod`, `salted_salmon`, the two upstream wild ghost pepper ids (VTD-65, -66, -71, -72, KT-18) | No `creative_delight:` ids at all: the member files returned early (`global.CD` missing or `[members] vintagedelight = false`) |
| SQ-VTD4-03 | A | Fake player eats `vintagedelight:ghost_charcoal` (or check `getFoodProperties()` in a probe) | Food with nutrition 0, alwaysEat, Wither 100 ticks amp 0, Fire Resistance 3600 ticks (VTD-67) | Not food: `ItemEvents.modification` in the jar startup script did not run |
| SQ-VTD4-04 | A | Place a Pickling Jar; insert garlic + honey bottle (fermented garlic honey) from the top, wait the recipe time; read slot 9 through the bottom handler; break the jar | Output in slot 7, glass bottle in slot 9, extractable from below; breaking drops it (VTD-70) | Bottle missing: `remainderOf` / config `[pickling_jar] remainderBuffer` |
| SQ-VTD4-05 | A | Load a world saved with port `-03` that holds a filled jar | Jar keeps its 9 items, gets the 10th slot, no error in `latest.log` | Handler shrunk: the `Size` widening in `load` |
| SQ-VTD4-06 | A | Break placed Magic Peanut, Smooth Salt Block, Vine Tile as a fake player with an empty hand | Each drops itself (VTD-54) | Table missing in the jar: rerun `port_resources.py` |
| SQ-VTD4-07 | A | `/loottuning` (or the boot log's pack summary) | Packs listed for oat bale, oat bag, peanut / cucumber / ghost pepper crates, three bell pepper crates and the seed bag; adds for the 7 vanilla tables (VTD-74) | Nothing from CD: the loot files did not run after `05_tuning.js` |
| SQ-VTD4-08 | A | Soft fluid map (`/vtdtest fluids`) | `vintagedelight:honey_vd` holds `vintagedelight:honey_jar` only; no apple sauce item maps to it (VTD-73) | |
| SQ-VTD4-09 | A, client | Client: JEI Pickling Jar page; place a carrot cake and eat slices; grow a Magic Peanut tree from a sapling with bone meal | Berserker's Brew reads 20s, leather 4min (VTD-69); the cut face shows the inside texture (VD-41); no golden egg on top (VTD-68) | |

#### SQ-FDB: Farmer's Delight 1.3.4 backports in the FD repack (added 2026-10-25)

Feature: [../farmers_delight.md](../farmers_delight.md) 4. Scratch `mods/`: `creative_delight-1.0.0.jar` (rebuilt
2026-10-25 after the `farmersdelight/` scripts; nests `repackedFarmers-1.19.2-1.2.4-ce-19.2-02`), the patcherMod build with the startup loader, and
`FarmersDelight-1.19.2-1.2.4.jar` moved out (as SQ-CDJ-07). Not run yet (owner: queue only).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-FDB-01 | A | Boot; `grep -i "farmersdelight.ce.mixins\|Mixin apply failed\|farmersdelight-ce" logs/latest.log logs/debug.log`; `ls OUT/srv/config/farmersdelight-ce-common.toml` | No mixin error for `farmersdelight.ce.mixins.json` (4 mixins, `defaultRequire 1`, so a miss is fatal at boot); the toml exists with `[pumpkin_pie] placeAs = "FARMERSDELIGHT"`, `sneakToPlace = false`, `[villagers] farmlandBlocks` = rich soil farmland + mulch block; no "farmlandBlocks ... skipped" warning | Boot crash naming a mixin: the SRG name in that mixin (`m_53329_`, `m_153254_`, `m_5578_`, `m_7203_`); no config: `FarmersDelightInitMixin` did not run |
| SQ-FDB-02 | A | `/give @p farmersdelight:rope_fence 3`, `farmersdelight:rope_fence_gate`, `farmersdelight:gleaming_salad_block`, `farmersdelight:gleaming_salad`, `farmersdelight:pumpkin_pie_slice`; `/setblock ~ ~ ~ farmersdelight:pumpkin_pie` | All exist; `/data get` on a held salad bowl shows max stack 16; KubeJS `item_dict` postInit warns about none of the 5 new keys | Missing: `FDBackportRegistry` not registered (init mixin) |
| SQ-FDB-03 | A | Rope fence row: place 3 rope fences in a line beside an oak fence and a stone block; a rope gate between two rope fences; an oak fence next to the gate; a lead on a rope fence | Rope fences join each other, the gate and the stone face; the oak fence joins neither; collision is 1.5 blocks; a lead ties on; the gate opens, plays the lead-knot sound, closes on redstone off | Oak fence joins: `RopeFenceConnectionMixin` |
| SQ-FDB-04 | A | Place a Gleaming Salad; take 4 servings with a bowl (fake player), then break the leftovers | 4 Bowls of Gleaming Salad, block light 12 to 0 as servings drop; eating one gives Nourishment 6000 ticks, 14 food; leftovers drop a bowl | Light 0: the lightLevel lambda |
| SQ-FDB-05 | A | Hold a vanilla Pumpkin Pie, use it on the top of stone; repeat with `placeAs` set to `DELIGHTFUL`, then `CREATE_CENTRAL_KITCHEN`, then `OFF` (edit the toml, save, wait 2 s) | Places `farmersdelight:pumpkin_pie`, `delightful:pumpkin_pie`, `create_central_kitchen:pumpkin_pie` in turn, no reboot; `OFF` gives what the two mods do alone; never on a Cutting Board; one pie used up in survival; eating from the FD pie or a knife on it gives `farmersdelight:pumpkin_pie_slice` | Config change ignored: Forge's file watcher (try `/reload` is not enough, it is a COMMON config) |
| SQ-FDB-06 | A | Fake player walks on Rich Soil Farmland, then jumps on it from 3 blocks (or `FarmlandTrampleEvent` via a falling armor stand) | Still Rich Soil Farmland (was Dirt) | `onFarmlandTrample` not on the Forge bus |
| SQ-FDB-07 | A | Superflat with a farmer villager (with composter) beside a 5x5 patch of Rich Soil Farmland and one of Fertile Soil (mulch) 4 blocks away, wheat seeds in its inventory; `/data get entity @e[type=villager,limit=1] Brain.memories` after 2 min | `minecraft:secondary_job_site` lists the rich farmland and mulch positions; seeds get planted there; with `farmlandBlocks = []` the memory is empty again (no reboot) | Empty with the defaults: `VillagersTargetFarmlandMixin` redirect missed (it is `require 1`, so a boot crash would show first) |
| SQ-FDB-08 | A | Hold a skillet, stand underwater next to a campfire with raw beef in the other hand, use | Action bar "Can't cook underwater..."; no cooking starts; above water it cooks | |
| SQ-FDB-09 | A | Recipe dump or JEI probe: `creative_delight:` ids `crafting_shaped/rope_fence`, `.../rope_fence_gate`, shapeless `gleaming_salad_block`, `pumpkin_pie_from_fd_slices`, `farmersdelightcutting/salvaging/<wood>_furniture` (9), `.../<wood>_chest_boat` (7), 4 minecarts; `farmersdelight:cutting/<wood>_door|sign|trapdoor` gone; Rabbit Stew takes `#forge:crops/potato`, Vegetable Noodles `#forge:mushrooms`, Dog Food `#creativeengineering:food_type/raw/meat` | All present, each furniture recipe has every part and a 0.75 plank chance | A missing wood part: its dict key (`i.<part>_<wood>`) |
| SQ-FDB-10 | A | Tags: `farmersdelight:cabbage_roll_ingredients` contains `minecraft:beef` and `farmersdelight:tomato`, no `minecraft:egg`; `forge:fences` has `farmersdelight:rope_fence`; block tag `farmersdelight:mineable/knife` has `farmersdelight:pumpkin_pie` | As listed | Egg still in: another script adds it after `farmersdelight/tags.js` |
| SQ-FDB-11 | A | Kill 300 zombies as a fake player with Looting III (or `/loot spawn ~ ~ ~ kill @e[type=zombie,limit=1]` repeatedly with a player killer); break a pumpkin with an iron knife, then with a Silk Touch knife | About 5% of zombie kills drop an Onion (2% + 3x1%); no onion from a non-player kill; knife: 4 Pumpkin Slices, no pumpkin; Silk Touch knife: the pumpkin | Runs without CD too (loot.js needs only FD and LootJS) |
| SQ-FDB-12 | A, client | Client: Gleaming Salad sparkles; rope fence and gate models and item icons; FD pumpkin pie model; Rich Soil Farmland and Fertile Soil tooltips; rice placed on dry land | Sparkle particles near the salad (no missing-texture squares); models match the preview page; tooltips have the "Farmer villagers work it like Farmland" line; rice message "This seed must be planted in shallow water." | Purple-black: a texture not copied into the overlay |
| SQ-FDB-13 | A | `/setblock ~ ~ ~ farmersdelight:hanging_canvas_sign[rotation=5,attached=true,waterlogged=true]` under a stone ceiling; `/setblock` all 16 rotations in a row (`rotation=0..15`, both `attached` values); hold the item and place it under stone, then under a chain, sneaking and not | States accepted with exactly `rotation`, `attached`, `waterlogged`; placed sign faces the player, 16 rotations step evenly (22.5 degrees), V chains when attached, straight chains when not; waterlogged one shows water; no missing-texture squares | Purple-black: `textures/block/hanging_canvas_sign.png`; wrong direction or skewed rotations: element angle signs in `blockstates/hanging_canvas_sign.json` models |
| SQ-FDB-14 | A | Place an attached sign (sneak) under a stone block and break the stone; repeat with an unattached sign; mine a sign with an axe and with a hand | Attached sign pops off and drops 1 item; unattached stays; axe mines it faster; it drops itself | Nothing drops: loot table path; no pop: `canSurvive` |
| SQ-FDB-15 | A | JEI / recipe dump: `creative_delight:crafting_shaped/hanging_canvas_sign`; craft `c c / w#w / w#w` with 2 Chains, 4 oak logs (and another wood), 2 Canvas | 6 Hanging Canvas Signs; recipe visible in JEI with the icon and name "Hanging Canvas Sign"; Farmer's Structures house template places its signs without popping | No recipe: `farmersdelight/recipes.js` load error in `logs/kubejs/server.log` |

#### SQ-FR: Fruits Delight 0.5.11 fixes in Creative Delight (added 2026-10-25)

Feature: [../fruits_delight.md](../fruits_delight.md) 5 (FR-01 to FR-06). Scratch `mods/`: a `creative_delight-1.0.0.jar`
built after 2026-10-25 18:30 (first CD mixin config) with `fruitsdelight-0.5.11.jar` beside it, plus the patcherMod build with
the startup loader; restart (new item_dict keys, startup script). Not run yet (owner: queue only).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-FR-01 | A | Boot; `grep -i "creative_delight.mixins\|Mixin apply failed\|null guard not applied\|fix mixins are off\|fruitsdelight/" logs/latest.log logs/debug.log` | `Done (`; no mixin error, none of the two CD warnings, no `Error occurred while handling event` from `fruitsdelight/recipes.js` or `tags.js` | Manifest lost `MixinConfigs` (`mixin_configs` in CD's `gradle.properties`); warning: Fruits Delight's handler or version changed |
| SQ-FR-02 | A | `/setblock ~1 ~ ~ fruitsdelight:mangosteen_cake`; fake player at full hunger eats one bite (or player with `/effect clear` and hunger 10); `/data get entity @p ActiveEffects` | `farmersdelight:comfort` ~400 ticks and `fruitsdelight:sliding` ~800 ticks, amplifier 0 | Effects at 0 ticks: the redirect did not apply (grep `MangosteenCakeEffectMixin` in debug.log) |
| SQ-FR-03 | A | Startup log line or `/kubejs hand` probe: `Item.of('fruitsdelight:bellini_cocktail').item.foodProperties.effects` | One `fruitsdelight:heal_aura` (200 ticks), Intoxication 1800, Nausea 100; no second Heal Aura | Two Heal Aura entries: `fruits_foods.js` did not run (CD startup loader) |
| SQ-FR-04 | A | Recipe dump: `fruitsdelight:fig_pudding` inputs; tag dump: `forge:jams`, `forge:storage_blocks`, `forge:storage_blocks/peach` (item and block) | Fig Pudding `E` = `#forge:eggs`; `forge:jams` has the 22 Fruits Delight jams beside Delightful's two; the 16 crates in `forge:storage_blocks` with their `/<fruit>` tags, both registries | Unchanged recipe: the `replaceInput` id; empty tags: `global.item.t_forge_jams` key missing (item_dict not reloaded) |
| SQ-FR-05 | A | Same jars with `fruitsdelight-0.5.11.jar` (and the mods that need it) moved out; boot | CD loads; no mixin error or `ClassNotFoundException` for `dev.xkmc.fruitsdelight`; the three Fruits Delight scripts skipped (`requires`) | A compartment class referenced from the main path, or the plugin gate |

#### SQ-RD: Rustic Delight 1.7.0 port in Creative Delight (added 2026-10-25)

Feature: [../rustic_delight.md](../rustic_delight.md) 5 (RD-01 to RD-12). Scratch `mods/`: a `creative_delight-1.0.0.jar`
built after 2026-10-25 (nests `backportedRustic-1.7.0-ce-19.2-01`) with `rusticdelight-forge-1.19.2-1.3.0.jar` moved out,
the patcherMod build with the startup loader, and `rustic_upstream/rusticdelight-forge-1.20.1-1.7.0.jar` copied into the
scratch game dir. Group A: RD-01 to RD-08, RD-13 to RD-25; group B (new level-name): RD-09 to RD-11, RD-26 to RD-28;
client: RD-12, RD-29, RD-30; RD-31 is the instance cutover, last. RD-13 to RD-31 added 2026-10-25 (every test the port
needs). Not run yet (owner: queue only).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-RD-01 | A | Boot; `grep -i "rustic\|Error occurred while handling event\|Couldn't parse\|Failed to load" logs/latest.log` | `Rustic Delight upstream assets/data: N files (M dropped ...)`; no parse error for a `rusticdelight:` recipe, loot table, tag or loot modifier; `Done (` | A parse error names the 1.20 field RusticTranslator misses; game stops: `rustic_upstream/` jar missing or sha1 differs |
| SQ-RD-02 | A | Same boot with `rustic_upstream/` renamed | Boot stops with "reads its textures ... must be in the folder 'rustic_upstream'" and the Modrinth URL | Starts anyway: `UpstreamJars.verifyOrFail` not called |
| SQ-RD-03 | A | Old world from the live pack (1.3.0 blocks: honey pancakes eaten to 3, crates, crops, wild plants, potted wild coffee) | Every block keeps its state (pancakes show 3), no missing-registry warning for `rusticdelight:` | A missing id: compare `RDBlocks` with the 1.3.0 blockstates |
| SQ-RD-04 | A | `/give` honey pancakes, place, right-click 6 times; place again, sneak-right-click with a Honey Pancake 6 times, then once more | 6 Honey Pancakes taken, the 6th removes the plate; stack grows to 12, 13th refused; comparator reads the count | Sneak does nothing: `RDEvents.onRightClickBlock` |
| SQ-RD-05 | A | Place Bell Pepper Medley and Rice Roll Royale; take every serving | Medley: red, yellow, green stuffed peppers, then the bowl breaks with leftovers; Royale: 3 kelp roll slices, green, red, yellow pepper rolls, calamari roll, cherry blossom roll | Wrong item: the serving lists in `PepperMedleyBlock` / `RiceRollRoyaleBlock` |
| SQ-RD-06 | A | Recipe dump (`/dumpall` or JEI): Bell Pepper Pasta, Bell Pepper Soup, Golden Coffee Beans, Honey Coffee, Coffee-Braised Beef, Calamari Roll, brown dye; `rusticdelight:cherry_blossom_ingredients` tag | Pasta and soup take any 3 `#forge:vegetables/bell_pepper`; golden beans from raw or roasted; honey coffee without sugar; beef from any coffee food ingredient; calamari roll from `#forge:raw_fishes/calamari`; brown dye from roasted beans; the cherry tag holds `vinery:cherry_leaves` and `quark:pink_blossom_leaves` | Old recipe: a pack script overrides it (grep the output in `server_scripts/`) |
| SQ-RD-07 | A | Recipe dump: syrup, coffee and cherry blossom cookies, the three cheesecakes, Maple Cookie, Maple Glazed Bacon, Supreme Pizza, the heatless Bell Pepper Pasta | Cookies and cheesecakes only in the oven (cookie dough + 2 flavour -> 12; pie crust + 2 flavour + 2 sweet); Maple Cookie = dough + syrup + honey; bacon = cooked bacon + syrup; pizza = bell pepper, cheese, onion, mushroom, tomato sauce, cooked meat, dough; pasta takes 3 of `#creativeengineering:roasted_bell_pepper` (9 colours) | Missing: `rusticdelight/recipes.js` returned early (CD member switch, `global.CD`) |
| SQ-RD-08 | A | Loot: `/loot spawn ~ ~ ~ loot minecraft:chests/ancient_city` x20, same for `skyvillages:chests/sky_village_normal_house`; `/summon squid` and kill it; Wandering Trader with `/summon wandering_trader` (several) | Dark seeds in some ancient city rolls, Pale seeds in some sky village rolls, never in jungle temples; squid drops 1-2 Calamari; a trader sometimes offers Pale or Dark seeds (5 emeralds, 3 uses) | No seeds: `rusticdelight/loot.js` (lootTuning) or the table ids |
| SQ-RD-09 | B | New world; `/locate biome` a savanna and a jungle, `/tp`, fly around; `/place feature rusticdelight:bell_pepper_block_patch_placed` | Giant peppers only red, yellow, green and only in warm dry grassy biomes; wild bell pepper patches have no Pale or Dark wild peppers | Other colours: `RusticTranslator.basePatch`; wrong biomes: the `giant_bell_pepper` row (`requires: creative_delight`) |
| SQ-RD-10 | B | `/locate structure minecraft:village_savanna`, `/tp`; look at the farms; also a plains village | Log `[creative_delight] village farm crops added to 5 farm processor lists`; some savanna farm crops are cotton; plains farms unchanged (no rule matches) | No line: `VillageFarmCrops.onServerAboutToStart`; reflection failed: the SRG field name `f_74422_` (`farm/VillageFarmCrops.onServerAboutToStart` in the CD jar) |
| SQ-RD-11 | B | Villager trades: farmer and fisherman at novice | Farmer buys cotton, red peppers, coffee beans; fisherman swaps calamari | Missing: `RDEvents.onVillagerTrades` |
| SQ-RD-12 | client | Creative tab, a few items (Pale Bell Pepper Seeds, Pancakes, Coffee Cheesecake, giant pepper), mod list, JEI | Textures and names from the donor; Cotton Boll shows "Cotton", its crate "Cotton Crate"; mod list "Rustic Delight (Creative Delight)"; Brewin pizza "Supreme Pizza"; no Fruit Beignet | Purple-black texture: the client pack (`rustic_upstream_assets`) not added |
| SQ-RD-13 | A | Giant peppers: `/give` 9 `rusticdelight:bell_pepper_slice_red`, craft 3x3; recipe dump for all 9 `bell_pepper_<colour>_block_from_bell_pepper_slice_<colour>` and `cutting/bell_pepper_<colour>_block`; cut one block on a Cutting Board with a knife; place 5, break with an axe, by hand, with Silk Touch, and one by TNT | 9 slices -> 1 giant pepper for all 9 colours; cutting gives exactly 9 slices; breaking drops 1 to 9 slices and never the block (Silk Touch too); axe breaks faster than hand (melon hardness) | No craft: a pack script removes it (grep the id in `server_scripts/`) or `RusticTranslator` dropped it; block drops itself: loot table not loaded |
| SQ-RD-14 | A | Survival chain for giant peppers without worldgen: `/give` each of the 9 peppers; cut each on a Cutting Board with a knife (x20 for chances); roast a slice of each in furnace, smoker, campfire; craft 1 slice of each colour alone in the grid; 2 slices + `farmersdelight:cooked_rice` | Each pepper -> 2 slices + about 10% seed (base colours Bell Pepper Seeds, white / pink / orange Pale, blue / purple / black Dark); every slice roasts in all three; slice -> 1 seed of its family; 2 slices + rice -> 2 rolls of that colour | Wrong seed family: the donor's cutting json; roast missing: smoker / campfire types |
| SQ-RD-15 | A | New crops: on farmland with water, plant Bell Pepper, Pale, Dark, Coffee and Cotton seeds; bone meal each to full; `/data get block` the age; harvest 10 of each (fresh crops) | Ages 0-7 peppers, 0-5 coffee, 0-3 cotton (8 / 6 / 4 stages); Pale harvest white, pink, orange + Pale seeds; Dark blue, purple, black + Dark seeds; base peppers follow the pack roll (about even red / yellow / green, 15% bonus red, `loot/09_crop_drops.js`); coffee beans; cotton | Base roll red-heavy: `09_crop_drops.js` key `rusticdelight:blocks/bell_peppers` no longer matches; Pale or Dark lopsided: upstream table, no pack roll (note it) |
| SQ-RD-16 | A | Wild plants: `/setblock` each wild (bell peppers, pale, dark, coffee, cotton) on grass, dirt, coarse dirt, rooted dirt, podzol, and on sand; break each by hand and with shears; place each in a flower pot | Stays on every `#minecraft:dirt` block, pops off sand; hand gives seeds (wild bell peppers sometimes a red pepper), shears give the plant; potted forms place | Pops off dirt: the wild block's `mayPlaceOn` |
| SQ-RD-17 | A | Pancakes, all 7 stacks (plain, honey, chocolate, cherry blossom, vegetable, pumpkin, coffee): each pot recipe in the dump; `/give` and place each; take 2 servings, break the stack; place another, break it full; eat each serving | Each stack comes from the pot; serving items are plain / honey / chocolate / cherry blossom / vegetable / pumpkin / coffee Pancake; a stack with servings taken drops nothing, an untouched one drops itself (upstream loot); serving effects: honey and chocolate Speed 900, vegetable Haste 600 | Partial stack drops: pancake loot tables; wrong serving: `RDBlocks.pancakes` supplier order |
| SQ-RD-18 | A | Pale and Dark Bell Pepper Medley: dump the 3 feast recipes; place each, take every serving; break one half-eaten | Pale gives white, pink, orange stuffed peppers; Dark blue, purple, black; then the bowl; all 9 stuffed peppers cook in the pot from their colour | Wrong item: `PepperMedleyBlock` serving list |
| SQ-RD-19 | A | Syrup line: dump syrup (bottle + `#rusticdelight:syrup_ingredients` + sugar), `sugar_from_syrup`, Syrup Coffee (from Coffee and from Milk Coffee), Sweet Salad, Syrup Sandwich, Syrup Cookie, Syrup Cheesecake; `/tag` list of `syrup_ingredients`; the pack's maple foods | Apple and beetroot in the tag; syrup -> 3 sugar; Sweet Salad takes honey or syrup; maple foods read syrup (5.2); cookie oven only | Missing item recipe: dropped by the translator (item not registered) |
| SQ-RD-20 | A | Coffees: give a test player (or pillager probe) Poison, Slowness and Weakness, drink each of the 8 coffees (coffee, dark, milk, honey, chocolate, syrup, pumpkin, cherry blossom); dump every coffee recipe | Coffee, Dark, Milk, Honey, Chocolate give Caffeinated at `food_values.js` durations; Milk, Honey, Chocolate, Syrup, Pumpkin, Cherry Blossom clear exactly one effect, plain and Dark none; Syrup, Pumpkin and Cherry Blossom give upstream's Speed and Haste (expected today, `creativedelight/members #11`); every Rustic coffee recipe present: pot coffees from 3 raw or roasted beans, grid coffees from Coffee and from Milk Coffee | Clears more than one or none: `EffectClearingDrinkItem`; a coffee recipe missing: a pack removal (the unification keeps them all) |
| SQ-RD-21 | A | Coffee foods and golden beans: dump Coffee Cookie, Coffee Cheesecake, Coffee Pancakes, Golden Coffee Beans, Coffee-Braised Beef, roasted bean bag both ways, yellow and brown dye | Present, values per 5.3 (golden beans saturation 1.2) | Missing: translator log line for the recipe |
| SQ-RD-22 | A | Fried foods and soup: dump Fried Dough, Fried Dumplings, Fried Fish, Calamari Soup; eat Calamari Soup | Fried ones read `#rusticdelight:cooking_oil` (pack oil included, no Rustic oil pot recipe); soup from calamari tag, potato, onion, milk; Nourishment 3600 | Oil tag empty: `cookingtags.js` |
| SQ-RD-23 | A | Cheesecakes and cherry blossom foods: place each of the 3 cheesecakes, take every slice with a knife; craft slices back to a whole; dump Cherry Blossom Coffee, Pancakes, Cookie, Cheesecake | Slices taken until the plate is gone; slices -> whole; cherry recipes accept `vinery:cherry_leaves` and `quark:pink_blossom_leaves` | Leaves refused: `cherry_blossom_ingredients` tag (RD-06) |
| SQ-RD-24 | A | `/place feature` each on grass in a cleared flat area: `rusticdelight:bell_pepper_block_patch_placed`, the wild bell pepper, coffee and cotton patches (placed ids from `wild_crops.json`) x5 | Giant patch puts red, yellow, green blocks on grass only (never on dirt or sand, never a rare colour); wild patches place their plant; no Pale or Dark wilds in the bell pepper patch | Rare colours: `RusticTranslator.basePatch`; fails to place: rarity filter id or `configurable_rarity_filter` codec |
| SQ-RD-25 | A | After RD-01's boot: `grep -n "rusticdelight\|ERROR" logs/kubejs/startup.log logs/kubejs/server.log`; `/reload` once | No error from `rusticdelight/*.js` (recipes, tags, loot, trades) or `item_dict` lookups for new ids; same after `/reload` | Undefined key: a new id not in the CD dict (one place for items) |
| SQ-RD-26 | B | New world: `/locate biome` jungle, savanna, a warm wet and a warm dry grassy biome; fly 2000 blocks in each | Wild bell peppers and coffee in warm wet, cotton in warm dry grassy, giant peppers about 1 patch per 64 chunks (rare) in warm dry grassy only; nothing from upstream's own biome modifiers (no jungle giant patches) | Upstream modifiers active: translator did not drop `forge/biome_modifier` |
| SQ-RD-27 | B | Every village type: `/locate structure` plains, desert, savanna, snowy, taiga village; look at 3 farms each, list the crops seen | Savanna farms about 25% cotton among vanilla crops; other types vanilla only (today's rules). Record the list per type for the farm audit (`farming/crops #16`) | No change anywhere: RD-10 log line missing |
| SQ-RD-28 | B | Chest loot in a real world: 2 ancient cities, a sky village or loot balloon if found; a jungle temple | Dark seeds possible in ancient city chests, Pale in sky villages; jungle temple has no Rustic seeds or bean swap (translator dropped the chest modifiers) | Seeds in jungle temples: upstream loot modifiers not dropped |
| SQ-RD-29 | client | Models and tooltips: pancake stacks at 1, 6, 12; medleys and Royale at each serving; cheesecake slices; giant peppers (9); crop stages of all 5 crops; potted wilds (5); hover the 5 effect-clearing coffees; WTHIT on each crop | Every state has its donor texture (no purple-black); coffee tooltips show "Clears 1 Effect"; WTHIT crop growth line on the 5 Rustic crops | Missing model: the client pack; no WTHIT line: QoL-6 crop list |
| SQ-RD-30 | client | AppleSkin and JEI: hover bell pepper, slice, golden coffee beans, a pancake serving; JEI uses of a giant pepper and a red slice | Bell pepper 2 / 0.2, slice 1 / 0.1, golden beans saturation 1.2, chocolate and vegetable servings 4 / 0.6; JEI shows the 3x3 craft, the cutting recipe and the roll | Old values: `food_values.js` or a food tuning row overrides them |
| SQ-RD-31 | instance | Cutover (5.7), after RD-01 to RD-30 pass: `installToMods`, check `jar_archive/displaced/` has the 1.3.0 jar; drop the 1.3.0-only lines; boot the instance; `/dumpall`, then the generators | Boot clean (RD-01 grep); `fruit_beignet` nowhere in `server.log`; generators list the about 100 new items (food types, cans, compost, placeable) | 1.3.0 still loads: `replaces_jars`; generator gaps: run order in memory `dumpall-refresh-workflow` |

#### SQ-DF: Delightful repack with the 3.8.1 items in Creative Delight (added 2026-10-25)

Feature: [../delightful.md](../delightful.md) 3, 4 (DF-01 to DF-11). Scratch `mods/`: a `creative_delight-1.0.0.jar`
built after 2026-10-25 20:03 (nests `repackedDelightful-3.4.1-ce-19.2-01`) with `Delightful-1.19.2-3.4.1.jar` moved out,
Neapolitan, Blueprint, Miner's Delight port, AE2 and IE present. Group A only (server); DF-02 and DF-11 are client. Not run
yet (owner: queue only).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-DF-01 | A | Boot; `grep -i "delightful\|Error occurred while handling event\|Mixin apply" logs/latest.log` | `delightful` loads as `3.4.1-ce-19.2-01`; `delightful.ce.mixins.json` applied (no `InvalidInjectionException`); `Done (` | Mixin target not found: the `<init>` of `net.brnbrd.delightful.Delightful` changed; registry error: an id clashes with 3.4.1 |
| SQ-DF-02 | A | `/give @s delightful:cantaloupe_seeds`, `delightful:baklava`, `delightful:fluix_knife`, `delightful:matcha_milkshake`, `delightful:cactus_soup_cup` | all 5 exist | the overlay registers did not run (mixin, or CD member switch off) |
| SQ-DF-03 | A | `/kubejs errors`; grep `server.log` for `creative_delight:` recipe ids of `delightful/recipes.js` (or `/recipes` dump) | no error from `delightful/recipes.js` or `tags.js`; 24 recipes under `creative_delight:` from it | a key undefined: `item_dict.js`; the script returned early: `Item.exists(i.cantaloupe_seeds)` false |
| SQ-DF-04 | A | `/setblock` sand by water, place `cantaloupe_plant` via seeds (`/fill` or a fake player), `/setblock ~ ~ ~ delightful:cantaloupe_plant[age=3]`, break it | drops seeds + Cantaloupe; age 0 drops seeds only | loot table not shipped (path) |
| SQ-DF-05 | A | Neapolitan milk cauldron (`/setblock neapolitan:milk_cauldron[level=3]`), use Matcha Ice Cream on it (fake player or client) | becomes `delightful:matcha_milkshake_cauldron[level=3]` | `DelightfulMilkshakes.registerCauldronInteractions` not run (common setup), or Neapolitan's `milkCauldron` config off |
| SQ-DF-06 | A | `/data get` the tag lists: `/kubejs dump_tags` or `tag list` for `forge:tools/knives`, `forge:cooked_nuts`, `delightful:hot_spices` | the 3 knives; Roasted Acorn + Roasted Peanut; blaze powder + ghost pepper | `delightful/tags.js` returned early |
| SQ-DF-07 | A | Old world with 3.4.1 Delightful blocks (salmonberry bush, cantaloupe, cabinets) | loads, blocks intact | the repack dropped an upstream file (compare `jar_diff.py` against the 3.4.1 jar) |

#### SQ-OPEN: older [scratch]-tagged tests still unchecked, not covered by an entry above (added 2026-10-24)

No new commands: the steps and expected results are in the named test (the writeup is the spec). Several have partial results
in the writeup or in `testing/structures/*.scratch.md`; read those first so a rerun does not start cold. Run only after the
jar set is the current one, and tick in the writeup.

| ID | Group | Tests | Notes |
|---|---|---|---|
| SQ-OPEN-LO | B | [../locate.md](../locate.md) LO-01, LO-03, LO-04, LO-05, LO-13, LO-14, LO-17 | `/celocate` baseline, census 128 with stages A + B, rates, non-blocking loads, config switches, miss reasons, stop label. LO-03/04/14 share one census run |
| SQ-OPEN-FP | B (new player join) | [../feature_porting.md](../feature_porting.md) FP-02, FP-03, FP-05, FP-07, FP-10, FP-12; server halves of FP-11, FP-13, FP-15 already PASSED (SQ-OM) | FP-10 duplicates FP-03: run once, tick both. FP-12 needs a real or fake player beside a lit campfire. FP-05 ice boats is client-feel; only the speed numbers are server |
| SQ-OPEN-ST1 | B | [../structure_tuning.md](../structure_tuning.md) ST-106, ST-114, ST-115, ST-118, ST-120, ST-135, ST-136 | Phase 2 / 6 results are already in the entries (ST-115 and ST-135 FAILED then); recheck against the current jar before blaming code |
| SQ-OPEN-ST2 | B | ST-133, ST-137, ST-142, ST-147 (the `[scratch/game]` ones: the scratch half only) | Ocean geode count by y; way sign text and no-mashup / town village checks need eyes |
| SQ-OPEN-ST3 | B | ST-158, ST-160, ST-176, ST-186, ST-188, ST-191 to ST-198 | Earth-fill `log = ALL` lines, ground match, restart determinism of villages, deep dungeons, drum tower, stone tag |

#### SQ-FF: produce family rules applied (added 2026-10-25)

Feature: [../food_families.md](../food_families.md) 4.4 (FF-01 to FF-08), [../placeable_food.md](../placeable_food.md) 3.
Jar state: the patcherMod jar built 2026-10-25 (Display Delight dishes) and a Creative Delight jar (the recipes are in
its script tree) both in the scratch `mods/`; restart (new item_dict keys, 20 new cans, removed items). Client half
(models, tooltips) is FF-06 to FF-08 in game; the flat coaster items were cut 2026-10-25 (placeable_food.md 3), so FF-0x coaster checks expect only items with a coaster model.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-FF-00 | A | boot, grep `latest.log` | `Done (`; no `[NCH]` warning and no `Error occurred while handling event` from `fruitsdelight/recipes.js`, `hostiledelight/recipes.js`, `miners_delight/recipes.js`, `moredelight/recipes.js`, `abnormals_delight/recipes.js`; no `Display Delight dish ... not installed` warning; no missing-model or loot errors for `creativeengineering:placed_*` | A missing key or tag: check the item_dict line; a dish warning: the item id in `displaydelight_dishes.json` |
| SQ-FF-01 | A | `/dumpall`, copy `recipes.json`, `placeable.json` (food export) and `tags/item_tags.json` into the instance, `python3 tools/food_families.py` | 3.2: every Rule column of `fruit`, `veggie`, `meat`, `fish`, `egg`, `dairy` reads full except `meat` recipe (Maple Glazed Bacon, no source); `egg` placeable 16/16 | A recipe row still short: the member's recipe id in the dump (`creative_delight:` namespace); placeable short: the DD link in `placeable.json` `displayDelight.placed` |
| SQ-FF-02 | A | `/dumpplaceable` | Chat count of Display Delight placed links is 9 higher than the 2026-10-24 dump; the 9 `creativeengineering:placed_*` blocks are in the placed map with their foods | Links missing: `DisplayDelightDishBlocks.onLevelLoad` did not run (server level load) or the food is not in `#displaydelight:displayable` |

#### SQ-CDQ: Creative Delight QoL moves (added 2026-10-25)

Feature: [../creative_delight.md](../creative_delight.md) 22 (CDQ-01 to CDQ-08). Jar state: patcherMod and
`creative_delight-1.0.0.jar` both rebuilt from `development/*/build/libs/` after 2026-10-25 (the pot and kettle hooks,
the WTHIT line and the compost rows left patcherMod for CD); never one without the other. Config switches are in the
scratch server's `config/creative_delight-common.toml [qol]`. Rows marked client run in Round 2.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-CDQ-00 | A | boot, grep `latest.log` | `Done (`; mixin lines apply `creative_delight.mixins.json` with no `InvalidInjectionException` / `@Mixin target ... not found`; no `Error occurred while handling event` from `*/cooking.js`, `<mod>/compost.js` | a `require = 1` miss names the injector: compare with the javap check (22.1) |
| SQ-CDQ-01 | A, client | remainder_buffer.md RB-01 to RB-11 on a cooking pot and a kettle (bottle, bowl, bucket leftovers, full buffer stalls, hopper pulls) | as RB- | patcherMod's old pot mixins still present (two jars mismatched) |
| SQ-CDQ-02 | A | `[qol] remainderBuffer = false`, restart; cook a milk-bottle recipe in a pot and in an oven | pot throws the bottle, oven keeps it | the gate is `RemainderBufferQol` (pot, kettle) and patcherMod `[cooking] remainderBuffer` (oven) |
| SQ-CDQ-03 | A | remove Farmer's Respite from the scratch `mods/`, boot | `Done (`; no mixin error for `farmersrespite.*`; pot buffer still works | `CDMixinPlugin` compartment check |
| SQ-CDQ-04 | A, client | hover a placeable food, a plate food, a coaster drink; then `placeableTooltips = false`, F3+T | lines as before; off: no pack lines, Display Delight's own lines back | script is under `assets/creative_delight/kubejs/_qol/` |
| SQ-CDQ-05 | A | `/dumpcompost`; compare `kubejs/exported/compost/compost.json` with the 2026-10-25 dump for the 101 Farmer's, Fungi, Veggies and Vintage items; then `compost = false`, `/reload`, dump again | on: same chance, source `value`; off: those items `rule` or absent | rows are `values(..., { when })` in the jar startup scripts |
| SQ-CDQ-06 | A | `/dumpall` recipes; grep `creative_delight:shapeless/.*_manual_only` for the Veggies, Vintage, Fungi meals; then `manualOnlyRoutes = false`, `/reload` | on: present, the mods' grid ids absent; off: `veggiesdelight:broccoli_salad`, `vintagedelight:pb_j`, `fungidelight:inky_cap_salad` back | file returns early: check `global.CD.on` for the port |
| SQ-CDQ-07 | A, client | WTHIT on hemp, a salmonberry bush, a Veggies crop; `wthitCropLine = false`, restart | "Growth" lines as before; off: none (WTHIT's own CropBlock lines stay) | rules file is `assets/creative_delight/crop_growth.json` |
| SQ-CDQ-08 | A, client | JEI: truffle, a morel, cleaned cap, info tab; `jeiInfoPages = false`, F3+T | on: the three Fungi pages; off: none | `// requires: jei` header |

#### SQ-CDM: Creative Delight member row moves, members #2 to #5 (added 2026-10-25)

Feature: [../creative_delight.md](../creative_delight.md) 27 (CDM-04 to CDM-07). Jar state: as SQ-CDQ (the cup
conversion file is only in a CD jar built after 2026-10-25 20:40; the scripts also run from the folder tree). Best run
with a `/dumpall` from before the move to compare (`git stash` the six pack files and the CD tree on the scratch copy only).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-CDM-00 | A | boot, `/reload`, grep `latest.log` | no `Error occurred while handling event` from `veggiesdelight/`, `vintagedelight/`, `fungidelight/` scripts | the file named in the error |
| SQ-CDM-01 | A | `/dumpall` recipes; grep `creative_delight:` for `carrot_juice`, `dandelion_juice`, `turnip_water` (pot `_manual_only`, mixing, filling, emptying, bottling), `cauliflower_bread` (baking), `cheese_burger` / `deluxe_burger` (shaped `_manual_only`, sequenced assembly) | each present once; the old `creativeengineering:` twins and `veggiesdelight:cooking/carrot_juice`, `vintagedelight:bread_from_smelting`, `vintagedelight:cheese_burger` absent; recipe count equal to the pre-move dump | `global.CD.on` false for the member, or the file did not load |
| SQ-CDM-02 | A | `/dumptags`: `forge:crops/oat`, `forge:seeds/ghost_pepper`, `forge:storage_blocks/truffle`, `forge:mushrooms/inky_cap`, `forge:dough` | same members as before the move (oat dough in `forge:dough`, colonies in the mushroom tags) | tag script gate |
| SQ-CDM-03 | A | `/loottuning`; open a fresh `village_taiga_house` chest x10 with `/loot` | Veggies crate packs and the 9 table additions listed; cauliflower and seeds turn up at the same rate as before | `veggiesdelight/loot.js` ran before `global.lootTuning` existed |
| SQ-CDM-04 | A | `/dumptrades` | farmer level 1 has the six Veggies produce trades; the Wandering Trader common pool holds the seven Veggies items | MoreJS event in the CD tree: `// requires: morejs` |
| SQ-CDM-05 | A | Copper Pot: cook Brewin' Creamy Onion Soup; grep `Cup conversions:` | the pot serves the Onion Soup cup; log count as before (1 + upstream rows) | `data/creative_delight/cup_conversion/` missing from the jar or folder |

#### SQ-CDF: all pack food and cooking KubeJS moved into Creative Delight (added 2026-10-25)

Feature: [../creative_delight.md](../creative_delight.md) 29 (`creativedelight/members #13`). Jar state: a CD jar built after
this move (the startup half registers the bakeware, dough blocks, cornmeal, pack fluids, generated cans, seasonings and
compost rows from the jar). Never boot a world on the old CD jar with these pack folders: the moved items would be missing.
Compare against a `/dumpall` from before the move (backup tree: `development/creativeDelight/moved_code_backup/2026-10-25b/`).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-CDF-00 | A | boot; grep `logs/kubejs/startup.log` and `latest.log` | no script errors from `kubejs_startup/creativeengineering/`, `*/compost_values.js` or `data/creative_delight/kubejs/`; `[cans] registered N generated cans`, `[food values] 2 foods copied`, `[creative_delight] server scripts ready` | the file named in the error |
| SQ-CDF-01 | A | `/dumpitems` (or JEI): bakeware, frosting, cookie dough, powdered sugar, 6 dough blocks, cornmeal, the 16 pack fluids, the generated cans | all present with their old ids; an old world keeps them in inventories and chests | a startup file did not load from the jar (`JarStartupScripts`) |
| SQ-CDF-02 | A | `/dumpall` recipes; compare with the pre-move dump | the same recipes, `creativeengineering:` ids of the moved rows now `creative_delight:`; total count equal; no `creativeengineering:` twin left | a CD file returned early (gate, `// requires:`, `Platform.isLoaded`) |
| SQ-CDF-03 | A | grep `latest.log` for `[itemMerges]`; JEI on Canned Coffee Berries | two lines (`pack`, `creative_delight`); the can asks for coffee beans, not Farmer's Respite coffee berries | `_core/item_merges.js` ran before `canning.js` (priority) |
| SQ-CDF-04 | A | `/dumptags`: `creativeengineering:food_type/*`, `forge:sugar`, `forge:cooked_meat`, `creativeengineering:soup_veggie_cooked`, bakeware tags, can servings, jar tags | same members as before; removed items (wheat dough, Delightful venison, chocolate coins) in none | `_core/removed_item_tags.js` did not run last |
| SQ-CDF-05 | A | `/dumpcompost` | table equal to the pre-move `kubejs/exported/compost/compost.json` (744 values, 10 categories, 5 tiers) | a `compost_values.js` skipped by its `// requires:` |
| SQ-CDF-06 | A | oven: bake a cake with a cake pan; mortar a catalog entry; season popcorn; Cooking for Blockheads milk jar with a bottle | each works as before | the file for that system under `creativeengineering/` or `cookingforblockheads/` |
| SQ-CDF-07 | A | Brewin' rows left in the pack: craft a quiche from egg solution, cook horror lasagna | still made (pack `cooking.js`; the egg solution fluid comes from CD's `create/recipes.js`) | pack `cooking.js` |
| SQ-CDF-08 | A | `python3 tools/gen_compost_values.py --check` and `python3 tools/creative_delight_test/gen_cdtest_mirror.py --check` after a fresh `/dumpall` | compost check exits 0; the mirror is frozen (README), its stale report is expected | generator rules |

#### SQ-CN: Corn Delight 1.2.11 items and rules (added 2026-10-25)

Feature: [../corn_delight.md](../corn_delight.md) 5 (CN-), TODO `largecontent/mod-additions #24`. Needs the patcherMod 1.1.03
and Creative Delight jars from this session in the scratch `mods/`. Client halves (models, Curios render, right click, JEI):
CN-05, CN-06 in game.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-CN-00 | A | boot, grep `latest.log` | `Done (`; no `Error occurred while handling event` from `corn_delight/*.js`; no `[block_dict] Missing` line for `corn_crop`; `[corn-delight] corn is villager food` once; no mixin error for `MobCobPipeSlotMixin` | the script or mixin named in the error |
| SQ-CN-01 | A | `/kubejs hand` with each, or `/give` | `creativeengineering:corncob`, `corn_popsicle`, `cob_pipe` exist; the pipe stacks to 1 | CD startup `corn_delight/corn_items.js` |
| SQ-CN-02 | A | recipe dump grep `creative_delight:` | `cutting/corncob_kernels` (6 kernels), `crafting_shaped/corn_popsicle` (ice cubes tag), `crafting_shapeless/cob_pipe`, `crafting_shapeless/classic_corn_dog_from_corn_dog`; `createcrushing` and `createmilling` `corn_cornmeal` / `wild_corn_cornmeal` list a 0.1 corncob; IE crusher `corn_cornmeal` lists a 0.1 corncob; mortar `corn_cornmeal` gives 3 cornmeal only | `corn_delight/recipes.js`, `creativeengineering/recipes.js`, `immersiveengineering/recipes.js` |
| SQ-CN-03 | A | `ComposterBlock.COMPOSTABLES` probe | corncob 0.3 | CD `corn_delight/compost.js` |
| SQ-CN-04 | A | `/loot spawn ~ ~ ~ mine <pos of age 7 corn_crop> <knife>` x40, then with an empty hand x20, then on age 3 x20 | knife: about 10 cobs in 40; hand and age 3: none | `corn_delight/loot.js` (`CE/corn_knife_corncob`) |
| SQ-CN-05 | A | tag dump | `#curios:head`, `#curios:mouth`, `#curios:face` hold `creativeengineering:cob_pipe` | `corn_delight/tags.js`, `tags/curiostags.js` |
| SQ-CN-06 | A | probe `Villager.WANTED_ITEMS`, `Villager.FOOD_POINTS` | both hold `corn_delight:corn` (points 1), Veggies items still there | patcherMod `compat/CornDelightCompat` |
| SQ-CN-07 | A | summon a farmer, check its offers at levels 1 and 2; MoreJS wanderer dump | level 1: 24 corn -> 1 emerald; level 2: 8 emeralds -> 8 cornbread, 8 tortillas; wanderer COMMON has corn, RARE has the popcorn box | `corn_delight/trades.js` |

#### SQ-DDP: Display Delight 1.7.0 port (added 2026-10-25)

Feature: [../display_delight.md](../display_delight.md) (DDP-), TODO `largecontent/mod-additions #22`. Jar
`backportedDisplay-1.7.0-ce-19.2-01.jar` in `mods/`, replacing `displaydelight-1.2.0.jar`. Client halves: CQ-DDP.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-DDP-01 | A | boot, grep `latest.log` | `Done (`; no error naming `displaydelight`; no `Failed to set block association`; no missing-registry line; `config/displaydelight-common.toml` rewritten with the Features keys (DDP-01) | Registry error: `DisplayFoods.java` out of step with `display_blocks.json`, rerun `port_resources.py` |
| SQ-DDP-02 | A | `/setblock` one block per source mod (`displaydelight:abd_duck_noodles`, `edd_dragon_meat_stew`, `erd_chorus_stew`, `fr_green_tea`, `md_cave_soup`, `bnc_cheesy_pasta`, `nd_grilled_strider`), then break each as a fake player | Each drops its food (`abnormals_delight:duck_noodles`, ...), none drops the block item; plated `md_plated_vegan_hamburger` drops the food and a `displaydelight:food_plate` (DDP-03) | Block item dropped: the food is not in `#displaydelight:displayable` or the link failed (`md_` must map to `miners_delight`) |
| SQ-DDP-03 | A | Break `creativeengineering:placed_shakshouka` and `placed_omelette` | One food each (loot table only, no second drop) (DDP-09) | Two drops: the `getDrops` early return in `AbstractItemBlock` |
| SQ-DDP-04 | A | `/dumpplaceable`, then `python3 tools/placeable_catalog.py` | About 95 more links than the 2026-10-24 dump; nothing unexpected under "Display Delight gaps" (DDP-12) | Links missing: tag entries in `data/displaydelight/tags/items/` |
| SQ-DDP-05 | A | piston-push a placed `displaydelight:beef_stew` | Breaks and drops (DDP-10) | `getPistonPushReaction` override |

#### SQ-WCD: wild crops in Creative Delight (added 2026-10-25)

Feature: [../creative_delight.md](../creative_delight.md) 24 (WCD-01 to WCD-06). Jar state: patcherMod and
`creative_delight-1.0.0.jar` both rebuilt from `development/*/build/libs/` after the move (the `wildCrops` switch left
CEConfig for CDConfig `[worldgen]`). WCD-05 runs before the CD install, on the jars in `mods/` at the time. Biome tags
and modifiers need a new level-name per boot.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-WCD-01 | B | boot a new level, grep `latest.log` for `[wild-crops]` | `62 crop modifiers added, 49 mod modifiers off, 12 biome-file patches removed` (the Rhino count; mods missing on the scratch server lower it, each named in a `skipped` line); later `crop/biome pairs in 63 crop tags` with no `problems`; the 5 Terralith cave biomes warning names `biome_traits.json` | no `[wild-crops]` line at all: the datapack script tree missed the biome-tag pre-pass or `highPriorityData` (compare SQ-KS-01); move the script back to `server_scripts/` and keep the rules in CD |
| SQ-WCD-02 | B | `/locate biome minecraft:jungle`, tp, forceload 4 chunks and look (or a `/fill ... replace` count): cocoa on jungle trees, melons; `/locate biome minecraft:beach`, same | cocoa and melons in the jungle; wild cabbages on the beach sand | features `creative_delight:wild/*` missing: `data/creative_delight/worldgen/` not in the folder pack or the jar |
| SQ-WCD-03 | B | after WCD-01, grep `latest.log` for `[wild-crops]` lines with `no such biome`, `is not in #creative_delight:wild_crops/` or `could not read` | none | tag pre-pass ran without the rules (readConfig returned null): check the `could not read` line |
| SQ-WCD-04 | B | `[worldgen] wildCrops = false` in `config/creative_delight-common.toml`, new level, boot | `[wild-crops] off in creative_delight-common.toml`; Farmer's wild cabbages back at their mod spawn (beaches) | the switch is read through `global.CD.worldgen` (CD startup script) |
| SQ-WCD-05 | B | before the CD install: boot with the old `creative_delight-1.0.0.jar` in `mods/` and the pack folder trees | same as WCD-01 (rules from the folder copy, switch reads on) | `global.CD.worldgen` guard or the JsonIO path |
| SQ-WCD-06 | B | after the install: delete `kubejs/data/creative_delight/` on the scratch server only, new level, boot | same as WCD-01: rules from the jar copy (`CDResources`), features from the jar | `CDResources.text` returned null: resource path or the build copy of `dataTrees` |
| SQ-WCD-07 | B | `/locate structure minecraft:village_savanna` (savannas are warm dry grassy: `#creative_delight:wild_crops/cotton`), look at the farms | some farm rows are cotton (VillageFarmCrops reads the renamed tag) | the tag namespace in `VillageFarmCrops.rule` |

#### SQ-DW: deadwood patches and doubled stumps (added 2026-10-25)

Feature: [../structure_tuning.md](../structure_tuning.md) 3.25 (ST-228 to ST-231). Data and KubeJS only, no jar change;
new level-name per boot (structure sets and placed features load with the world).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-DW-01 | B | boot a new level, grep `latest.log` for `deadwood` and `[meta-structures]` | no `does not exist` / staging error; `structure_tag_report.md` lists `group/deadwood` (16 / 5) with 6 members | meta not staged: `[worldgen.systems] metaStructures`, or a typo in a placed feature id |
| SQ-DW-02 | B | `/locate biome minecraft:savanna`, tp, `/locate structure creativeengineering:meta/deadwood_acacia`, tp, forceload the 3 x 3 chunks, count `acacia_log` with `/fill ~-20 ~-5 ~-20 ~20 ~10 ~20 minecraft:acacia_log replace minecraft:acacia_log` | found within ~30 chunks; 5+ acacia logs near the spot, no other wood | none found: group biome check (`#geophilic_reforged:wood_type/mc/acacia`); found but empty: every spot failed the ground filters |
| SQ-DW-03 | B | same as 02 for `_spruce` (from a taiga) and `_oak` (from a forest) | found, own wood only | as 02 |

#### SQ-RSP: Farmer's Respite Kettle tank (added 2026-10-25)

Feature: [../farmers_respite.md](../farmers_respite.md) 2 (RSP-01 to RSP-16). Creative Delight `1.0.0` (compartment
`mixin.farmersrespite`) and patcherMod `1.1.03` built 2026-10-25 and installed in `mods/`; copy both to the scratch
server. Full restart (mixins, startup fluids, NCH).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-RSP-01 | A | boot, grep `latest.log` for `KettleTankMixin\|KettleBlockMixin\|KettleRecipeSerializerMixin\|Mixin apply failed\|creative_delight.*Kettle\|farmersrespite:brewing` | no mixin error; no recipe parse error for `farmersrespite:brewing` (the old `rusticdelight:brewing/coffee` may still log once before the script removes it) | the method named in the error: check the SRG name against `javap` of the 2.0 jar |
| SQ-RSP-02 | A | `/kubejs dump_recipes` or grep the recipe dump for type `farmersrespite:brewing`; count, and grep the JSON for `needwater` | 48 recipes (Respite's 9 plain + 8 long + 9 strong, 7 from other mods, berry latte, gambler's tea, 8 Rustic coffees, 5 Fruits Delight teas), all `creative_delight:farmersrespitebrewing/` ids except gambler's tea (`creativeengineering:`), no `needwater` key, no `farmersrespite:brewing/` id left | a mod id still listed: its recipe id changed, fix the `kettle()` row |
| SQ-RSP-03 | A | place Kettle on a lit campfire, `/data get block ~ ~ ~` after: water bucket by right-click; then a Black Tea bottle by right-click | bucket: `water_level` 4 (blockstate), bucket comes back. Tea: refused (no change, GUI opens) | level 3: KettleBlockMixin `<clinit>` ModifyArg not applied |
| SQ-RSP-04 | A | empty the Kettle with 4 glass bottles (right-click): 4 water bottles, level 0; pour 2 Black Tea bottles; `/data get block` | `CETankFluid` = create_central_kitchen:black_tea, level 2; a water bottle then refused | bottle -> drink map: Central Kitchen's `filling/black_tea` / `emptying/black_tea` |
| SQ-RSP-05 | A | with 2 black tea in the tank: 1 honey bottle in an ingredient slot, glass bottle in the plating slot, wait 10 s | one Strong Black Tea in the output, level 1, honey's bottle in a leftover slot | Strong recipe base / matching (`getMatchingRecipe` HEAD) |
| SQ-RSP-06 | A | water in the tank: 2 black tea leaves, wait; `/data get` | Black Tea brewed, level down by 1, `Container` glass bottle | base water check |
| SQ-RSP-07 | A | a glass bottle in an ingredient slot (`/item replace block ~ ~ ~ container.0 with minecraft:glass_bottle 3`), wait 1 s | the 3 bottles move to the leftover slots (WTHIT "Holding: 3 Glass Bottle" or `/data get`), plating slot unchanged | `ce$stowBottles`; `[qol] remainderBuffer` off |
| SQ-RSP-08 | A | Create pump into the Kettle from a tank of water at 16 rpm, then at 256 rpm; a Fluid Tank under the Kettle with a pump pulling | fills in whole 250 mB steps, never above 1000; drains to 0 | K6 handler: `KettleTank.Handler.fill` |
| SQ-RSP-09 | A | Spout over a glass bottle with Milk Coffee fluid (`/fluid` via creative tank), Item Drain with a Milk Coffee bottle; same for Lavender Tea, Matcha Latte, Coffee; Spout over a Copper Cup with water, drain a Water Cup | each gives its drink / 250 mB; cup <-> water | NCH `createBottle` ids `createfilling/<drink>`, fluid registered (`fluids.js`) |
| SQ-RSP-10 | A | dispenser facing a Kettle with tea in it: water bucket; then an empty Kettle: water bucket | first: bucket stays in the dispenser; second: level 4 | `KettleDispenseMixin` method name |
| SQ-RSP-11 | A | break a Kettle with 3 water; place it again | item tooltip line (client, CQ-RSP-01); placed Kettle has level 3 | `writeMeal` RETURN / `setPlacedBy` |
| SQ-RSP-12 | A | place a Flower Pot, right-click with a Wild Tea Bush; break it | pot holds the bush; breaking drops pot + bush | patcherMod `RespitePottedTeaBush` log warning |
| SQ-RSP-13 | A | eat a Slice of Coffee Cake and a Rose Hip Pie Slice, `/effect` list | coffee: Caffeinated 0:30 only; pie: Regeneration 0:20 only | `food_values.js` modification block |
| SQ-RSP-14 | A | a world saved with a 2.0 Kettle at `water_level=3`: load it | Kettle still at 3 water, brews normally | blockstate property range |

#### SQ-INV: TODO investigations a scratch server can answer (added 2026-10-25)

Each entry answers or closes an open TODO row; the result goes into that row (close it, or rewrite it with the finding).
Ordered quickest first: steps 01 to 07 are log reads and single commands on the boot you already need; 08 to 14 are one
dump pass plus offline tools; 15 to 18 are generator and test runs after it. Run the whole group on one server, in order:
the dump pass (SQ-INV-08) must finish before 09 to 18. Group A except SQ-INV-06 (needs the End: B). No client needed.
The `/dumpworldjei` command is a server command (`WorldJeiDump`).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-INV-01 | A | grep `latest.log` after boot for `[time]`, `seasons now follow the sun`, `[advancements]`, `vinery:mead`, `king_danis_wine`, `calendar`, `Missing registry` | TODO `pack/release #3`: `[time]` push line, "seasons now follow the sun" once per dimension, `[advancements] N left out`, missing-registry lines only for the removed ids. Close the row | an unexpected id missing from the registry: note it in the row |
| SQ-INV-02 | A | grep `latest.log` for `item_dict` / `TL-01` tag check lines | TODO `tooling/dictionaries #1`: if every tag key is reported missing, the check runs before tags load (confirmed; move the tag half to `ServerEvents.tags`); if only real gaps, close the row | none |
| SQ-INV-03 | A | grep `latest.log` for the `[CE potions] added N` line and MoreJS `removeByPotion` lines | TODO `potions/potions #5`: N unchanged from PO-27 / PO-28's recorded count; close the row | record the new N in the row |
| SQ-INV-04 | A | grep `latest.log` for `metal base event` | TODO `recipes/metal #4`: print the line's wording and count; decide if it is noise or a real gap | none |
| SQ-INV-05 | A | read the block-state count (log line, or a probe of `Block.BLOCK_STATE_REGISTRY.size()`) | TODO `pack/server #6`: record the count against the 2^20 = 1,048,576 ceiling (761,052 on 10-19); headroom under about 290,000 | over the ceiling: the state-id fix breaks, stop adding block mods |
| SQ-INV-06 | B (End forceloaded) | `execute in minecraft:the_end run locate biome nullscape:crystal_peaks`, then `nullscape:void_barrens`; allow the 120 s timeout | TODO `worldgen/biomes #8` and `#4`: a biome is found (the tuning keeps it) or not found (tuning flips whole biomes out; record the seed and result) | locate times out: repeat with a larger `forceload` square |
| SQ-INV-07 | A | probe recipes of type `farmersdelight:cooking` for muffins (`/kubejs` recipe dump) and check the dough ingredient resolves `#forge:dough` and that Create's dough is in `#forge:dough/wheat` | TODO `cooking/oven #3` (OV-13): a muffin recipe exists and its ingredients match; closes or names the cause | name the failing ingredient in the row |
| SQ-INV-08 | A | `/dumpall`, `/dumptags`, `/dumpfood`, `/dumpstacks`, `/dumpplaceable`, `/dumppotions`, `/dumpworldjei` (the `/dumpall` set runs `/dumpfood`; `/dumpnames` is single-player only) | all write to `kubejs/exported/` without error; `/dumpservermods` needs every jar in `mod_sides.md` | a missing `mod_sides.md` row: add it, rerun |
| SQ-INV-09 | A | after 08: `python3 tools/food_catalog.py`, `gen_stack_sizes.py`, `gen_can_types.py` (empty `AWAIT_DUMP` for `fungidelight` and `vintagedelight`), `effect_audit.py`, `placeable_catalog.py`, `gen_compost_values.py --merge`; follow memory `dumpall-refresh-workflow` for order and hand steps | TODO `pack/release #5`, `food/stack-sizes #2`, `food/placeable #2`, `ports/vintageport #2`, `ports/fungiport #2`, `alcohol/brewinport #4`: generators run clean; Canned Kimchi no longer copies Satisfaction; `canned_mhadjeb` gone and glow calamari cans made (`dishes/meals #32`, `dishes/ocean #3`) | a food in the wrong type band: fix in `food_catalog` precedents |
| SQ-INV-10 | A | after 08: check the `kubejs/exported/` tag dump for `#brewinandchewin:heat_sources` and `freeze_sources` | TODO `alcohol/brewinport #2` (AL-41): both tags hold the expected sources | missing source: edit `blocktags.js` |
| SQ-INV-11 | A | after 08, offline scans of the recipe dump: shapeless recipes that output a pie crust; recipes using vanilla `minecraft:egg`; chicken-cut and fermented-veg usage; users of `createdeco:polished_iron_bars`; amethyst crushing recipes per machine (Create, IE) | TODO `baking/baked-goods #24`, `food/types-and-tuning #6` (IE recipes are not in the dump: list them separately), `recipes/metal #5`, `recipes/engineering #2`: a list of hits per row, written into each row | the dump lacks IE recipes: use `NCH` source grep instead |
| SQ-INV-12 | A | after 08, offline scans of the tag and item dumps: gold tags containing rose gold; flowers and `#minecraft:flowers` vs bee-breeding tags; potted-plant coverage; items with no BlockRunner tier; bottled items without a jar fluid; crops without harvest / replant rows | TODO `recipes/metal #3`, `mobs/bees #1`, `blocks/framed-and-decor #6`, `gameplay/blockrunner #2`, `items/jars #3`, `farming/crops #3`: a list per row | none |
| SQ-INV-13 | A | after 08: count Comfort-type foods in the food dump (57 on 2026-10-25) and list them by food type | TODO `food/types-and-tuning #15`: the list the owner needs to decide the rule | none |
| SQ-INV-14 | A | after 08: list gear upgrade recipes (assembly, smithing, crafting) and mark which go through `SequencedAssemblyRecipeMixin` | TODO `gear/tuning #4`: the upgrade paths that bypass the mixin | none |
| SQ-INV-15 | A | `python3 tools/gen_jei_world.py` / `/cegen run jei_world` after `/dumpworldjei` | TODO `jei/jei-coverage #5`, `#16`: `generated.json` rebuilt from `tool_pairs.json`; Stripping rows one per log | a generator error: fix the tool |
| SQ-INV-16 | A | `/cegen run loot dumps` after 08 | TODO `loot/lootjs-layer #3`: census, `loot_index.json`, `loot_config.json` now include Subterrestrial, Underground Bunkers and eugene (21 tables) | a table missing: check the namespace filter |
| SQ-INV-17 | A | after 16, verify LT-03, LT-04, LT-05, LT-18, LT-19 in the loot index; grep the loot dump for `stockpile`, `recovery`, `cycle` enchanted books; probe a librarian's offers | TODO `loot/tables #7` and `#10`: no coin or PneumaticCraft item in any table; list the tables that still hold the three books | a table with coins: add a ban |
| SQ-INV-18 | A | `/test runall` (or the SUN-counter GameTest) and `python3 tools/time_table.py` | TODO `gameplay/time #3`: both pass; close the row | a failure: the clock mapping needs a fix |

Results: not run yet. Skipped here and left in the TODO: scene and measurement checks (TNT minecart chain, fortified-meta
locate timing, Faraday armor, Vinery stem shearing, End teleport stall, trough load), config-change runs (`avoidCoasts`),
and pregen / debug-build work (`Level.random` caller, watchdog kills).

### Round 2: with a client window

One JVM at a time (memory `one-game-instance-at-a-time`): this round is run with the scratch server stopped. Either a
throwaway client on a single-player world (`tools/item_repro/launch_client.py GAME_DIR`, memory `client-launch-for-defaults`;
fresh `config/`, so client defaults are the pack's) or the user's own game. A single-player world has the integrated
server, so server probes (`/vtdtest`, `/fdtest`) also run there. Rows say what to look at; the full steps are in the
named writeup test. Cheapest order: CQ-DUMP first (everything after the Vintage rows depends on it), then one creative
superflat world for the visual groups, then a normal world for the world-feel groups.

Planned (not built): [../client_capture.md](../client_capture.md) runs the capturable rows unattended and leaves one
cropped PNG per test ID in `test_captures/`; once it exists these rows get a `Capture` column (spec / manual / partial).

#### CQ-DUMP: client-only dumps (unblocks the Vintage rows and the UI generators)

| ID | World | Commands | Pass | After |
|---|---|---|---|---|
| CQ-DUMP-01 | creative superflat | `/dumpall`, then `/dumpfood`, `/dumpcompost`, `/dumpcropgrowth`, `/dumpplaceable`, `/dumpnames` (those not in `/dumpall`) | Files in `kubejs/exported/` newer than the boot; they list `vintagedelight:` items | `python3 tools/food_catalog.py --check` (no unclassified Vintage food, VTD-20, VTD-53); empty `AWAIT_DUMP` in `gen_can_types.py` and rerun, then restart; `gen_stack_sizes.py`, `placeable_catalog.py`, `crop_growth_coverage.py` (0 UNKNOWN), `name_audit.py`, `jei_coverage.py`, BlockRunner tier script, `gen_pack_manifest.py` |
| CQ-DUMP-02 | same, after the rerun | restart; `/ofprobe` and `/vtdtest recipes` again | CP-31 recipe level: the 16 Vintage cans + 4 shared sources load; `[cans] registered` rises accordingly (and the Vintage rows of `can_parity.md` get their numbers, `can_audit.py`) | server-visible, so it can ride the next round-1 run |
| CQ-DUMP-03 | same boot as CQ-DUMP-01 | the dumps `/dumpall` does not run: `/dumpstacks`, `/dumppotions`, `/dumptags`, `/cedumpnames`, `/cedumpicons` (or `[debug.wthit] dumpIconsAtTitle`), `/dumpwinetextures` | Files newer than the boot in `kubejs/exported/` (and the name / icon / wine texture outputs) | `tools/stack_sizes.py` (`food/stack-sizes #2`), `effect_audit.py` and the `/dumppotions` step (`pack/release #5`, `alcohol/brewinport #4`), `name_audit.py` (`ui/names #4`), `wthit_icon_audit.py` (`ui/wthit #2`), the wine texture check (`textures/tools #1`); rerun `/dumptags` for the heat / cold tag audit (`alcohol/brewinport #2`) |
| CQ-DUMP-04 | creative superflat, after CQ-DUMP-01 | `/cegen run loot dumps`, then `/cegen` for the can generators and `soup_soft_fluids`; `python3 tools/food_catalog.py --check`, `gen_can_types.py` (empty `AWAIT_DUMP`: Fungi and Vintage), `can_audit.py` | Loot census and `loot_index.json` list Subterrestrial, Underground Bunkers and eugene (21 tables) (`loot/lootjs-layer #3`); `canned_mhadjeb` gone, glow calamari cans, Canned Pickles with the pickled sea pickle source, Canned Kimchi no longer copies Satisfaction (`dishes/ocean #3`, `dishes/pickles-and-ferments #1`, `dishes/meals #32`, `pack/release #5`); the hunger / saturation findings for the cans, 24 soup fluids still cover every `food_type/soup` item (`cans/canning #3, #14`, `dishes/ingredients #13`) | restart after the can regeneration (CQ-DUMP-02 covers the load check) |
| CQ-DUMP-05 | scratch server, group B normal world with the End | `/dumpall` biomes and `/locate biome nullscape:crystal_peaks` and `nullscape:void_barrens`; rerun the End climate dump | Whether crystal peaks and void barrens exist live (`worldgen/biomes #8`, `#4`) | none |
| CQ-DUMP-06 | any, after CQ-DUMP-01 | read the new `/dumpfood` and recipe dump: Comfort foods per type (57 on 2026-10-25), vanilla egg uses in recipes (rescan, the dump cannot read IE recipes), shapeless pie -> crust recipes, Hostile Mobs effect name, placeable cheese wheels and the Review regex | Numbers and ids for `food/types-and-tuning #15, #6`, `baking/baked-goods #24`, `ui/effects #10`, `food/placeable #2` | none |

#### CQ-VP: Vintage and Veggies, visual and machine runs

| ID | Existing test | What to do and look at |
|---|---|---|
| CQ-VP-01 | VD-40, VTD-45 | Names read the pack's rules (Caesar Salad, Zucchini Seeds, cases, "with", "and"); JEI: every Vintage and Veggies item has a recipe or a use; every Vintage item has a texture (no purple-black) |
| CQ-VP-02 | VD-03, VTD-05 | WTHIT growth line on every Vintage and Veggies crop; WTHIT icons after `wthit_icons.json` regenerates |
| CQ-VP-03 | VD-21, MJ-11, MJ-12, MJ-14, VTD-26, VTD-53 | Soups and the new jar fluids in a Supplementaries jar: each in its own colour close to its texture; 12 bottles fill a jar for bottle soups; WTHIT line on the jar |
| CQ-VP-04 | VTD-10 to VTD-14 | Pickling jar GUI: hopper in from the top, out from the bottom, salt from The Salt and Vintage's salt dust; evaporator beside water grows salt layers, bucket -> 3 dust; cheese mold wheel after a random tick, JEI explains it; Mason jars stack 1 to 4 and render |
| CQ-VP-05 | VTD-25, VD-17 | Placeable tooltips: Vintage jars, pizzas, cheese mold, golden egg; Veggies pies, cake, tray, lasagna, stuffed peppers (after `placeable_catalog.py`) |
| CQ-VP-06 | VTD-08, VTD-08a, VTD-08b, VTD-47 | Magic Peanut about 1 % from ripe peanuts, magic vine tree with Lush Grass and an 80 % Golden Egg; vine tiles only from cutting stripped vine; bone meal on Lush Grass; hoe -> Rich Soil Farmland. Write keep / purge into the writeup (S1) |
| CQ-VP-07 | VTD-02, VTD-03, VTD-04, VD-02 | Replant feel with the real right-click and a hoe over an area (cucumber, ghost pepper, zucchini); note Quark's generic replant interplay, then fill `crops.md` 2.1 rows |
| CQ-VP-08 | VTD-24, CP-27 to CP-30 | Crafting in the grid: a vinegar can (salad + vinegar + can), Veggies cans with the real servings, a canned golden melon slice; JEI on Vinegar Bottle |
| CQ-VP-09 | VTD-21, VTD-48 to VTD-52, VD-13, VD-22, VD-23 | Crafting and JEI for the cooking rules: pizzas in the oven, oat bread chain, burgers / burrito (grid and deployer belt assembly), Cheese Pasta / Pad Thai / Pickle Soup heatless, oatmeal cookies 12 from the oven, cauliflower bread in the oven |
| CQ-VP-10 | VTD-32, VTD-32a, NE-15, NE-16 | Goose golden egg with the chance raised to 1: watch it drop beside the pen wall; default 1 in 1000 not worth waiting for |
| CQ-VP-11 | VTD-33, VTD-35, VTD-40 | Chef hats in 16 colours and Simple Hats' gone from bags and recipes; century egg in the jar; overlap review of `*overlaps w/*` rows in JEI, write keep / merge into `vintage_delight.md` 3.1 |
| CQ-VP-12 | VD-06, VD-07 | Mature dandelion: break drops 1 dandelion + 0 to 3 leaves, shears give the block, bone meal pops another |
| CQ-VP-13 | VD-32, VD-33 | Farmer buys VD produce; wandering trader sells VD seeds, clove, sweet potato, leaf; animal breeding / taming by real right-click |
| CQ-VP-14 | VTD-23 | Brew the 6 custom-effect jar drinks and record how the potion mixers and fluid handle them (record only) |

#### CQ-OLD: client halves left open by earlier runs

Tests already named in their writeups with "in game" as the open half; one pass in one creative world covers them.
Colours / WTHIT / JEI / GUI each need the client.

| ID | Existing tests | What to do and look at |
|---|---|---|
| CQ-OLD-01 | RC-15 to RC-17, RC-55 to RC-69 | Corn / cornmeal names and textures, crushing wheels, millstone, JEI shapes of the heatless meals, burgers, quiche, shepherd's pie, squid ink pasta basin run, dog food, frying |
| CQ-OLD-02 | RC-71 to RC-78 | Chorus juice (JEI, basin / spout runs, Absorption line), pot recipe, berry matcha latte (kettle screen, bottle remainder), milk from Milk Jar / Cow in a Jar with a real player, broth soups, ice cubes with the hammer on the board, egg solution, frosting |
| CQ-OLD-03 | MJ-11, MJ-12, MJ-14, WL-32, WL-33 | Soup colours and WTHIT in jars; WTHIT "Holding:" and "Waiting: leftover slots full" lines on pot, kettle, oven |
| CQ-OLD-04 | KT-01 to KT-06, OV-11, OV-32, OV-33, RB-01 to RB-14 | CfB kitchen: oven fuel and oil, sink, cutting board offset, fridge model, oven JEI [+] without log spam, Sweet Berry Pie names, remainder buffer |
| CQ-OLD-05 | FO-30, FO-31, FO-32, FO-43, SN-48, PL-01 to PL-09 | Seasoning JEI page; Spice of Life in a single-player world (new world values); a pie bite counts for SoL; no `{}` NBT on returned bowls; placeable tooltip lines and coaster line |
| CQ-OLD-06 | AE-01 to AE-24, GR-28, FX-21 to FX-24 | Armor and tool effects in play (the on-hit half passed on the server), air brake `useDurationTicks`, Trinket Case refuses only the open case |
| CQ-OLD-07 | MB-100 to MB-104, MT-25, SN, CP-15 to CP-26 | More Mooblooms JEI, leaf pile slow height walking and WTHIT, can crafts and effects in play |
| CQ-OLD-08 | DP-08, OV | First join of a world with the release's `world_serverconfig` README steps for a single-player save (client half of DP-08) |

#### CQ-NEW: client halves added 2026-10-24

| ID | Existing test | What to do and look at |
|---|---|---|
| CQ-NEW-01 | TI-30, TI-31 | Xaero minimap time line agrees with SeasonHUD (default profile, `/time set 11800`, sunrise step; Nether unchanged); bed clock: sleep at dusk, note whether Better Days' and the hotbar clock turn |
| CQ-NEW-02 | FX-43 to FX-45 | Moss block and carpet follow the biome grass colour; items stay vanilla green in inventory, JEI and hand; `biomeTint = false` (F3+T) vanilla; `tintStrength = 0.5` (F3+A) |
| CQ-NEW-03 | FO-49 | AppleSkin: onion popcorn's hunger bar 2 drumsticks longer than plain; cheese and garlic lines; JEI seasoning page (`SeasoningJEI`) |

| CQ-NEW-04 | names/tooltips (1.1.03) | Salmon Eggs read Caviar, Fish Egg Roll / Slice / Noodle Soup read Caviar Roll / Roll Slice / Noodle Soup, Fried Rice reads Egg Fried Rice, Ocean's / Oceanic / End's Delight read "(Creative Delight)" in mod list, JEI and WTHIT; Drop of Glycerol tooltip "Tastes sweet!" without Shift; Fishing Net tooltip lists the blocks and the drops by biome (SQ-FN numbers) |
| CQ-NEW-05 | kitchen (1.1.03) | Two-high fridge has its own shape with a white top box filling the block; Plates, plated foods and Coasters on a Kitchen Counter and Corner; break the block under a plate: it drops (SQ-KT-02); Miner's Delight redrawn textures (Cave Carrot, Baked Cave Carrot, crops, Wild Cave Carrots, Gossypium, Weird Caviar, Stuffed Squid bowl, Tentacles, Baked Tentacles, Sticky Basket sides) show no purple-black |
| CQ-NEW-06 | potion buckets and Haste | Survival potion buckets have a cooldown; Awkward, Thick, Mundane and Water buckets do nothing and are not used up; Miner's Haste and Mining Fatigue potions brew, show in JEI and JEED text (MND-22) |
| CQ-DDP-01 | DDP-02, DDP-04, DDP-05, DDP-07, DDP-08 | Open a world with 1.2.0 plates and food placed: all intact. Axe on a plated hamburger hides the plate (drops it), a Food Plate restores it; sneak + empty hand takes a stack of 3 cookies; Blazing Chili flames, Hot Cocoa steam; a stew on a fence and on a slab gets a wooden leg, on a Kitchen Counter none |
| CQ-DDP-02 | DDP-06, DDP-11 | Silk touch on a Beef Stew drops "Placed Beef Stew"; JEI and WTHIT on 5 new blocks show "Placed ..." / "Plated ..." names and 3D icons (no purple cube); the Display Delight creative tab lists them |
| CQ-RSP-01 | RSP-09 to RSP-13 | Kettle GUI: the bar has 4 steps in the fluid's colour (water blue, black tea dark) with "Black Tea: 2 / 4" tooltip; fill box (top left) and empty box (bottom left) framed, hint tooltips while empty; Black Tea bottles in the fill box pour one per 0.2 s and the empties land in the empty box; glass bottles put in the empty box flip it and fill from the tank; shift-click a tea goes to the fill box, a stone block goes nowhere new; the pointer marks the active box. Kettle item tooltip "Tank: Water, 3 / 4"; WTHIT "Tank: Black Tea 2 / 4"; JEI Kettle page shows the base fluid in the bar (Strong Black Tea: black tea, one quarter) and lists the Rustic coffees and Fruits Delight teas; Wither Roots death text with and without a mob hitting you |

Anything that fails here is written to the feature's writeup first, then fixed; a pass is ticked in the writeup and the
TODO row's status word changes.

## Run log

### 2026-10-26 afternoon run (group B normal world nb2, seed -8188900049893920259, JFR on)

Jars: `mods/` as of 2026-10-26 15:32 (patcherMod 1.1.03, CD 15:31, Vintage -07), COPIED into the scratch `mods/`; second
boot with patcherMod rebuilt 15:54 (fix 1). Probes: the usual set plus new `tools/seasoning_probe/seasoning_probe.js`
(`/sntest`, SQ-SNF-03) and `/zq states all` (block-state count). Boot clean apart from the known `truffle_pie` NCH error.

Fixes made in the run:

1. **Perf mixins never applied on a dedicated server:** all 9 (`ServerTickPerfMixin` ... `KubeJSEventHandlerContainerMixin`)
   were in the `client` list of `creativeengineering.mixins.json`; `/ceperf status` said no hook had run and 0 handlers
   were wrapped. Moved to `mixins`; patcherMod rebuilt and installed 15:54. Dev / server tooling, no changelog bullet.
2. `food_dict.js`: dropped `fruit_jam_bottle`, `fruit_jam_glow`, `drink_juice_prickly_pear` (Delightful items removed in the
   3.8.1 repack; `tools/food_dict.py` flagged them, no script used them). No changelog bullet.

Dumps and generators: `/dumpall` 23/24 in 318 s (`/dumpservermods` needs `mod_sides.md`, so `tools/dump_server_mods.py` ran
offline: 368 jars). Copied back: recipes, tags, loot_tables, world_jei, structures, biome_data, spawns, villager_trades,
fluids, potions, worldgen/structuregen and the 4 climate dumps (old set in the session scratchpad). All 29 manifest
generators ran from the instance: 27 exit 0, `structure_live_check` warns (only the known empty
`aquacultures_carp_with_all` set; shipwrecks / ruined portals in no set as structure_tuning.md says), `food_dict` warned
before fix 2. Client-only dumps (names, stacks, gear, food, crops, compost, paintings, items by mod) were not copied.

| Entries | Result |
|---|---|
| SQ-SNF-01, -02, -03 | PASS: the 4 seasonable tags hold only popcorn, no `seasonable/sugar` tag; snack and fruit families as expected; the 5 season recipes present, none for sugar or `_on_corn_delight_popcorn`; `/sntest` 5/5 |
| SQ-PF-01, -02, -05 | PASS after fix 1 (320 handlers wrapped, all hooks run; report has ticks, dimensions, `server_scripts:<file>:<line>`, entity / block entity types; 2 reloads keep 320) |
| SQ-PF-03 | Partial: no player, so `spreadplayers` stood in for `/tp`: one 22 s sync load listed with caller `Level#getChunk` and phase |
| SQ-SV-01 | Server half PASS: End armor stand tp to 300016 100 300016 in 6 ms, no "ticks behind", no `r.585.585.mca`. Myalite viaduct needs a player |
| SQ-INV-01 to -06 | Done, results in the TODO rows (`release #3`, `dictionaries #1`, `potions #5`, `metal #4`, `server #6`, `biomes #8`): 770,959 block states; both Nullscape biomes located |
| SQ-INV-08, -15, -16 | Dump pass and generators as above (`jei_world` 3551 rows, loot census 12342 tables) |
| `/fdtest all`, `/cdjtest`, `/fgdtest all`, `/kttest` | 98/98, 23/23, 135/136 (truffle pie), 27/31 (KT-15d, KT-17 x3, as 10-26) |
| `/vtdtest all` | 358/376: the 18 fails are crop-survival rows; the probe plants at y = -60, underground in a normal world (no light). Not a regression: run it on group A |
| Not run | SQ-PF-04 (pregen A/B), SQ-PF-06 and all client rows, SQ-INV-07 and -09 to -14, -17, -18, group B worldgen counts |

### 2026-10-26 run (group A superflat qb1, then group B normal world nb1)

Jars: `mods/` as of 2026-10-25 23:59 (patcherMod 1.1.03, CD 23:49, Vintage -06, Incubation Additions 1.1.0, repackedStorage
-01), COPIED into the scratch `mods/`. KubeJS tree of 2026-10-26 00:06 plus the fixes below. 3 boots of qb1, 1 of nb1.
New probe: `tools/cdb_en_probe/cdb_en_probe.js` (`/cbetest blocks|soils|growth|dragon|anvil|squid|jars|all`; needs
`forceload add 400 400`). Probe fixes: `fdtest` resolves `creativeengineering:` ids to their `creative_delight:` twin (listed
once), `vtdtest` checks Pickled Sea Pickle instead of Brewin's pickled pickles, `/zq recipes` counts past its 80-line print cap.
Setup slips worth knowing: `/cdjtest` takes no argument and needs `forceload add 400 400` (0 gristle otherwise); `place
structure` needs the spot forceloaded first ("That position is not loaded").

Fixes made in the run:

1. **Boot stopped (datapack load):** `creative_delight:wild/chorus_succulent` used feature type
   `ends_delight:chorus_succulent_feature` (from upstream's stale json); the registered id is `ends_delight:chorus_succulent`.
   Fixed in the configured feature and `tools/gen_crop_worldgen_table.py`. Unreleased work, no changelog bullet.
2. `sample_syringe.js#71` `redeclaration of var $WorldRows` (Rhino nested `const`, also in the user's client log): the Sample
   Syringe JEI world rows were never added. `const` -> `let`; after a restart the class loads with no warning. Bullet added.
3. 29 soups had no jar fluid (Miner's cups and bowls, Hostile, Oceanic, End's, Fungi): `gen_soup_soft_fluids.py` only read
   top-level `mods/*.jar`, so the nested CD members and the `*_upstream` donors gave no texture. It now opens jarjar jars and
   donor jars; rerun wrote 29 new fluids (bell pepper soup colour now from Rustic 1.7.0). `/fdtest soupjar` PASS after a
   restart (Moonlight reads soft fluids at world load). Bullet added.
4. `Item.of(i.<tag key>, n)` as a recipe input collapses to the tag's first item (Cake Base wanted 2 Parrot Eggs and plain
   Sugar). 16 grid recipes, 3 Create mixes and 3 IE blueprints fixed: repeated keys for `R.shapeless` / `R.createMixing`,
   `{ id, count }` for `ieBlueprint`; the 4 auto ids that changed are pinned to the old ones. Memory `kubejs-api-notes`. Bullets added.
5. Vintage's fermented spider eye jar recipe took plain sugar only (`replaceInput` does not reach `vintagedelight:fermenting`):
   replaced by `creative_delight:pickling/fermented_spider_eye_from_fermenting` on the plain sweetener tag. Covered by the
   existing sweetener bullet.

| Entries | Result |
|---|---|
| SQ-CDB-01, SQ-EN-01 (server half), SQ-FFH-01/02 (no `foodFamily` / `cd_food.js` error), SQ-STD-01, SQ-FSD-01 | PASS after fix 1. Only NCH error: the known `truffle_pie` (`fungiport #3`). `[itemMerges] 12 merges`; Storage Delight 140 recipes; Farmer's donor 177 files, only Aether / Aquaculture blocks to air, no `Could not translate` |
| `/fgdtest all`, `/vtdtest all`, `/kttest`, `/cdjtest`, `/fdtest all` | fgd 135/136 (truffle pie), vtd 373+ all PASS after the probe update, kt 30/34 (same 4 as 10-25: KT-15d, KT-17 x3), cdj 23/23, fd 97/97 after fixes 3 and the probe alias |
| SQ-CDB-02 | PASS after fix 5 (all four sweetener tags as the row says) |
| SQ-CDB-03, -09, -13; SQ-ABD-01 to -05; SQ-MRD-03 (recipe half) | PASS by `/zq` |
| SQ-CDB-10, -11 | PASS after fix 4 (cacciatore now cycles cooked chicken cuts); `canned_mhadjeb` waits for SQ-CDB-12 |
| SQ-CDB-04, -06, -07 | PASS (`/cbetest squid`, `jars`): one squid / glow squid item per kill, no ink / calamari / tentacles (a knife adds Delightful's animal fat: upstream `fatty_animals` holds squid); lava bottle in and out; milkshakes go in; Splash Milk's bottle shows `moonlight:milk` |
| SQ-EN-02, -03, -04, -06 | PASS (`/cbetest`): crate and stove drop themselves; the 4 plants survive only on end stone / tilled end stone; chorusflame 9-10 vs 16-24 and void pepper 18 vs 28 growth ticks of 100 (tilled faster); dragon bare 23 meat / 8 legs / 5 teeth in 4 kills, knife and machete 34 / 8 / 9; Dragon Tooth Knife repairs with a tooth |
| SQ-FSD-03, -04 (placement), -06 | PASS: all 7 kinds place; carp not locatable, squids at 552 blocks. Chest / frame / pot contents and the cook variety are client checks |
| SQ-FSD-05, -08 | Upstream finding: Farmer's Structures 1.0.6 templates name `minecraft:cook_end_1_pool` / `cook_additions_N_pool` (no namespace) on the back-connectors and on 4 expansion connectors (`cook_additions_2_6/7/10`, `cook_end_1_15`); log `Empty or non-existent pool`, those 4 never expand. Same on 1.21. Not changed: owner decision (rewrite in the donor converter or leave) |
| SQ-EN-07 | Partial: `end_house` found 156 blocks from 1500 0 in a new End; the earth-fill check needs `log = ALL` in the srv toml and a restart (not done) |
| Not run | Group B worldgen counts (SQ-FGD-12 to -15, SQ-WCD, SQ-DW, SQ-VP-14/15, SQ-MD-01, SQ-RD-09 to -11 / -26 to -28, SQ-OPEN-*, SQ-EN-07 rest, SQ-EN-10), fake-player rows of Round 1 not covered by a probe, all client rows (CDB-05/08/12, EN-05/08, CQ-*) |

Other findings (not fixed): Corn Delight 1.0.3's `village_house` / `shipwreck_loot` loot modifiers use `mmlib:add_loot_table`,
not in the pack, so they never load (old: also in 2026-10-24 client logs). `i.spider_eye` is defined 3 times in `item_dict.js`.

### 2026-10-25 late run (group A, current 1.1.03 tree, partial)

Jars: everything in `mods/` as of 22:13 (copied into the scratch `mods/`, not symlinked: a parallel session rebuilt
and installed patcherMod and CD mid-boot once, `NoClassDefFoundError` in RegisterEvent), patcherMod and CD from
`build/libs` with the fixes below. KubeJS tree of 22:24. One superflat world (qa1), 5 boots. New scratch tools:
`tools/scratch_query/zq_probe.js` (`/zq recipes|rdetail|itemtag|blocktag|tagsof|nbt`), `zq_batch.sh` (a file of
console commands, prints the `[zq]` lines), `game_jvms.sh` (java pids that are not Gradle: `pgrep -x java` also
matches the Gradle daemon). `/dumpall` 22/24 ok (climatecaves, servermods fail on superflat). Server stopped at the end.

Fixes made in the run (all in unreleased 1.1.03 work, so no changelog bullet):

1. Boot stopped in KubeJS startup: CD `moredelight/toast.js` called `FoodBuilder.fast()`; KubeJS 1902 has
   `fastToEat()`. Fixed (CD rebuild).
2. Boot stopped again: KubeJS counts upper-case asset file names as startup errors. Renamed
   `corn_delight_art_NOTICE.txt` / `more_delight_art_NOTICE.txt` to `_notice.txt` (docs updated). Keep asset names lower case.
3. `[NCH] withNamespace failed ... redeclaration of var incBbqStick`: the whole CD Farmer's Delight block after the
   barbecue stick was skipped (grilled salmon, rolls, rice medley, mutton chops assemblies...). Nested `const inc*` ->
   `let` in `farmersdelight/recipes.js`, `create/golden.js`, `quarkdelight/recipes.js` (memory `rhino-fieldandmethods-gotcha`).
   After `/reload` the FD sequenced assemblies load.
4. CD Berserker's Brew came out as a plain water bottle: NCH `vdPickling` wrote results through `_itemJson` (no NBT),
   and `_nbtItemJson` read NBT with `getTag()`, which KubeJS hides from scripts (`.nbt` works). `vdPickling` output and
   secondary now use `_nbtItemJson`, which reads `.nbt`. Verified: Strength, Resistance, Wither. No `ieBlueprint` call
   passes an NBT stack, so that helper's recipes are unchanged. patcherMod rebuild + restart.

Probe updates (expectations that went stale with the CD move, not pack bugs): `fungi_probe.js` grid / pot meal ids are
`creative_delight:`, colony Serene Seasons checks dropped (FD's colonies are in neither tag; fungi_delight.md corrected);
`vintage_probe.js` resolves a `creativeengineering:` id to its `creative_delight:` twin, jar bottom side is 3 slots (the
-04 remainder slot), roasted pepper tag holds all 9 colours, Berserker's Brew under `creative_delight:pickling/`.

| Entries | Result |
|---|---|
| SQ-FGD-00, SQ-VP-00, -01, SQ-MDP-01, SQ-AD-00 (as nested CD members), SQ-FN-01, SQ-LE-00, SQ-TI-00, SQ-KT-00, SQ-VTD4-01, SQ-FDB-01, SQ-FR-01, SQ-RD-01, SQ-RD-25, SQ-CDQ-00, SQ-CDM-00, SQ-CDF-00, SQ-CDF-03, SQ-CN-00, SQ-DDP-01, SQ-RSP-01, SQ-FD-01 (log half) | PASS after fixes 1 to 3: `Done (`, no mixin / registry / `Error occurred while handling event` / NCH failure from our code except the known `truffle_pie` (`fungiport #3`). Only noise: redeco's two `Not a JSON object` tag files (also in the client log). Counts that moved since the rows were written: `[cans] registered 272`, `Cup conversions: 16`, Rustic upstream 452 files (8 dropped), `[jar-startup] creative_delight: 57 of 57`, `[CE potions] 58 item-agnostic mixes`, `[jeiRemove] 349`. FDB toml, `[moss]` keys, displaydelight `[Features]`, simplehats chef hat weight 0 all as the rows say |
| SQ-SN-00, SQ-PS-00 | PASS on `[seasoning] defined` cheese / onion / garlic / sugar; the `seasoning done` / `powdered_sugar done` lines no longer exist (the scripts moved into CD and log nothing): rows stale |
| SQ-FGD-01 to -06, -08, -10 (`/fgdtest all`) | PASS 135 / 136; the 1 FAIL is the known truffle pie (`fungiport #3`) |
| SQ-VP-02 to -12 (`/vtdtest all`) | PASS 370 / 375 after fix 4. Remaining FAILs are open TODO rows: ghost pepper survives on farmland (`vintageport #7`), right-click harvest resets cucumber / ghost pepper / zucchini to age 0 (`vintageport #8`) |
| SQ-KT-01, -02 (`/kttest`) | 30 PASS, 4 FAIL: KT-15d plate over air not refused; KT-17 food plate / plated hamburger do not pop when the stone or counter under them is broken (`above` still the plate, no drops). New since the last run: check the `updateShape` mixin and `popsUnsupported` (row's failure notes) |
| SQ-CDJ-01, -06 (`/cdjtest`) | PASS 23 / 23 |
| SQ-SN-01, SQ-PS-01, -02, SQ-DB-01, SQ-VTD4-02, SQ-LE-02, SQ-RM-01, SQ-RM-04 | PASS, ids now `creative_delight:` (seasoning, powdered sugar routes, 12 dough block recipes, pickling brew / leather with `#forge:salt`, ghost pepper crushing 2 / milling 2 / IE 3 seeds, no Vintage slime or salted fish, no bedroll / fruit beignet / Design Decor golden apple, the 4 FD dishes and `crab_bars_manual_only`, driftwood log stripping on the board and the Create saw, no Abnormals driftwood cutting). The five removed foods: no recipe, loot table or trade. Not shown by `/zq`: fluid amounts, heat, secondary outputs (orange jam mixer, slime mixer heat, bark output) |
| Not run | Everything else in Round 1 (fake-player rows, loot rolls, villagers, the B worldgen groups, client rows). Odd finding for a later look: `forge:sugar` also holds `pneumaticcraft:glycerol` |

The scratch `kubejs/exported/` of this run (server-safe dumps) was not copied into the instance.

### 2026-10-25 Creative Delight run (group A, CD focus)

Jars: patcherMod `development/patcherMod/build/libs/creativeengineering-1.1.02.jar` built 15:41 (the startup loader; the
`mods/` copy predates it), `creative_delight-1.0.0.jar` / `-cdtest.jar` rebuilt 16:06 with fix 1, the rest of `mods/` as
found. One superflat world (cdt2), about 12 boots. Probes: `tools/creative_delight_test/cdj_probe.js` (`/cdjtest`, new),
`bench_boot.sh` (new). Setup fix: `scratch_server.sh setup` now links every `*_upstream` folder (the Miner's port refused
to load without `miners_upstream/`).

Fixes made in the run: (1) `cd_core.js` `members()` used `Array.from` on a Java List: Rhino throws
`Invalid JavaScript value of type JavaIteratorWrapper`, `global.CD` was never set and boot stopped; now copied with
`forEach`. (2) The suite's `cdtest_startup.js` called `global.compost.tiers()` as a getter (it is a setter): now calls it
empty and counts `TIERS`.

| Entries | Result |
|---|---|
| SQ-CDT-01 | PASS. `[jar-startup] creativeengineering: 3 of 3`, `creative_delight: 2 of 3`, order `creativeengineering, startup_scripts, creative_delight`; NCH finished once; members line. Startup errors: the pre-existing `truffle_pie` NCH line (also in the client log, `fungiport #3`) and the deliberate `withNamespace` line only |
| SQ-CDT-02, -04, -05 | PASS: startup 7/7, ns 11/11, requires 2/2, tags 1/1, config 5/5, mirror 57/57 (30 helpers) |
| SQ-CDT-03 | PASS: `server.log` 0 errors; `/dumpcompost` 1171 compostable; cooking twins present |
| SQ-CDT-06 | PASS: `/reload` then `/cdtest all` 83/83; row text corrected (`server scripts ready` is start-only) |
| SQ-CDT-08 | PASS: `oceanic_delight = false`: `CD.on` false, member row skipped, Oceanic items still registered |
| SQ-CDT-09 | PASS: folder path ns / mirror / requires / tags all PASS, startup FAIL by design |
| SQ-CDT-10 | PASS, stronger than the row: the CD error line is logged, the pack's startup scripts fail, and KubeJS stops the boot ("startup script syntax errors"). The two jars must ship together; **the instance's `mods/` patcherMod is the old one, so the user's client cannot boot with the current `kubejs/` until the built jar is installed** |
| SQ-KS-01 to -04 | PASS (01 via `/reload` discovery, not a fresh boot): a datapack script sees `global.NCH` / `global.LOOT` and makes `creativeengineering:shapeless/sqks_test`; `kubejs/data` reloads; with both copies one runs and `kubejs/data` wins over the world datapack; the folder `creative_delight/kubejs/_core/cd_server.js` wins over the jar's copy (10.4 dev loop holds); `requires: not_a_mod` skipped |
| SQ-CDJ-01, -06 | PASS (server half): the three repacks load from the jar (debug log `Found valid mod file repacked...`); 22 / 47 / 63 items; End's gristle 3 and 5 from 10 knife kills, 0 from 10 bare-hand kills |
| SQ-CDJ-02 | **FAIL**: the stale `oceansdelight-1.0.2-1.19.2.jar` beside CD wins (`JarSelector ... passed in as source ... Using Mod File: mods/oceansdelight-1.0.2-1.19.2.jar`, `1.0.2-1.19.2-ce-19.2-01 -> 1.0.2-1.19.2`). A top-level jar beats a nested one regardless of version; creative_delight.md 14.1 point 4 corrected. `build_deploy.py` half PASS offline (`nested_modids` reads all three ids, `test_suite_entries` flags the cdtest jar) |
| SQ-FN-01 | PASS: `oceanic_delight.ce.mixins.json` registered and selected, no mixin or `fishing_net` / `biome_tag` loot error |
| SQ-FN-04 | PASS (by tag lookup in `/cdjtest`, not setblock): all 10 kelp blocks only in `fishing_net/kelp`, all 8 sea grass blocks only in `fishing_net/sea_grass` |
| Not run | SQ-CDT-07, SQ-CDJ-03 (client); SQ-CDJ-04 net loot and SQ-FN-02 / -03 (need ocean and river biomes: group B); SQ-CDJ-05 (ports not bundled) |

Performance (boot of the same world, built patcherMod both ways, `bench_boot.sh`): seconds from launch to `Done`, without
CD (upstream Ocean's / Oceanic / End's) 101.5, 110.1, 102.6 (mean 104.7); with the CD jar 112.1, 108.6, 103.4 (mean
108.0). The gap is inside the boot-to-boot spread (about 9 s). KubeJS startup scripts 1.09 to 1.13 s without, 1.15 to
1.27 s with (about +0.05 s: `cd_core.js`); server scripts 0.22 to 0.32 s both ways; recipe event 202 to 248 ms both ways,
same 5327 recipes. No measurable boot cost. Tick cost not measured (CD adds no tick handlers; the fishing net mixin
only moved from patcherMod).

### 2026-10-23 bulk run (group A only)

Jars as found in `mods/` (all current against their sources): `creativeengineering-1.1.02.jar` md5 ba4826bd,
`backportedFungi-1.3-ce-19.2-01`, `backportedVeggies-1.9.2-ce-19.2-02`, `backportedVintage-0.1.6-ce-19.2-03`,
`incubationAdditions-1.1.0`, the five add-on jars. KubeJS working tree of 2026-10-23 plus the fixes below. One group A
world (qa_1792792571), three boots: base; `spawn-animals=true` + `runAtLoad = true` + `goldenEggChance = 1`; the
transition-item fix + `goldenEggChance = 0`. **Group B (SQ-FGD-12 to 15, SQ-VP-14 to 16) and SQ-AD-01 not run yet**:
they stay in the queue above.

Setup note: superflat `scratch_server.sh` sets `spawn-animals=false`, and vanilla then discards every animal on its
next tick (summoned ones too). Anything that needs a live animal for more than one tick (pigs, geese, breeding by AI)
needs `spawn-animals=true` before boot. Forge did not pick up a live edit of `incubation_additions-common.toml`: restart.

New probes and helpers: `tools/fungi_probe/fungi_probe.js` (`/fgdtest`, final 0 fail apart from the findings),
`tools/vintage_probe/vintage_probe.js` (`/vtdtest`, 369 pass / 6 fail, all six findings), scratch-only
`tools/fungi_probe/zcount.js` (`/zcount` box census), `ztrades.js` (`/ztrades <profession> <level> <n>`),
`zgoose.js` (`/zgoose spawn|report`).

| Entries | Result |
|---|---|
| SQ-FGD-00, SQ-VP-00, SQ-VP-01, SQ-VP-17, SQ-AD-00 | PASS. No mixin / registry / unknown-item error for the ports; `[cans] registered 252`; item_dict missing list has no Fungi / Vintage / Veggies key; all 17 spawn rules matched (none idle); 8 mushroom modifiers; no `dehydrated`. The 2 redeco `Not a JSON object` tag errors and corn_delight GLM errors are old noise (also in the client log) |
| SQ-FGD-01 | PASS except **truffle pie**: `create_central_kitchen:truffle_pie` is not an item (CCK registers the pie block, slice and incomplete pie; the whole-pie item comes from Environmental), so `baking/truffle_pie` never loads, also in the client (`fungiport #3`) |
| SQ-FGD-02 | PASS except the colonies in the Serene Seasons tags: the port and fungi_delight.md list only the two mushrooms; the row's wording overreached (record, decide in `fungiport #3`) |
| SQ-FGD-03 to SQ-FGD-06, SQ-FGD-08, SQ-FGD-10, SQ-FGD-11 | PASS. Breeding (by `getBreedOffspring`, 40 per pair): red x brown 24 / 16, red x inky 20 / 20, inky x morel 21 / 19, same-kind pairs 40 / 40, moobloom cannot mate. Trades: farmer L3 buys the 4 colonies and truffle (4 for 1 emerald); wandering trader sells morel, inky cap, veil |
| SQ-FGD-07 | PASS: 40 pigs, mobGriefing true: 19 of 400 blocks dug in about 4.5 min (podzol and truffle dirt to coarse dirt), a truffle, sticks and clay on the ground, no loot error; false: 0 in about 4 min |
| SQ-VP-02 to SQ-VP-12 | PASS except the findings below. Burger and burrito assemblies PASS after fix 2. Queue text corrections: plain can recipes carry no `_manual_only` id (45 plain), the `_vinegar` additive covers 75 cans (all 75 of `can_types.json` load), not 66 |
| SQ-VP-08 | Honey bottle not returned (known `vintageport #4`, record only) |
| SQ-VP-13 | PASS. Chance 1: 3 of 3 geese drop a golden egg item (nest pen: beside the goose, nest empty; 1x1 pen: where it stands; against a wall: 2 blocks out on open ground). Chance 0: the goose lays into the nest (`eggs=1`). Golden Egg block drops 4 items from `vintagedelight:blocks/golden_egg` |
| SQ-RS-01 | PASS after fix 1: default boot no scan; `/recipescan` 29699 read, 0 unreadable, 64 references to 54 removed items, none a merged id; `runAtLoad`: one scan 2.1 s after `Done (`, none after `/reload` |

Findings (TODO rows, not fixed in the run):

- `vintageport #7`: ghost pepper seeds survive on farmland (VTD-03). `GhostPepperBlock` overrides only `mayPlaceOn`;
  Forge's `BushBlock.canSurvive` asks `canSustainPlant` when the block is itself, and farmland sustains crops. Needs a
  `canSurvive` override (or plant type) in the port jar.
- `vintageport #8`: right-click on a ripe cucumber, ghost pepper or zucchini resets it to age 0 with a normal drop,
  not the port's 7 -> 5 (zucchini 7 -> 4) (VTD-02, VTD-03, VD-25, VTD-04): a right-click harvest handler (most likely
  Quark's) runs before the block's `use`. Confirm in the client (CQ-VP-07).
- `vintageport #9`: `vintagedelight:honey_jar` is not a soft-fluid item: the port's `soft_fluids/honey_vd.json` lists
  the apple sauce bottle and jar (upstream copy-paste).
- `vintageport #10`: 2 of 26 jar recipes take only Vintage's salt dust, not `#forge:salt` (VTD-11):
  `vintagedelight:fermenting/berserks_brew`, `leather_from_fermenting`.
- `fungiport #3`: truffle pie (above) and the colony seasons-tag question.

Fixes made in the run:

1. `recipe_scan.js` read inputs with `Ingredient.getItems()` (hidden by KubeJS), so 26949 of 29699 recipes were
   "unreadable"; now `getStacks()`.
2. `transition_items.js`: rows for `cheese_burger`, `deluxe_burger`, `stuffed_burrito`; without them NCH skipped all
   three sequenced assemblies (`[NCH] incomplete failed`). Startup script: restart.

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

- SQ-MND-17 (queued 2026-10-25): stacked copper cups (64): MND-17 in [miners_delight.md](../miners_delight.md). Server part: place a powder snow cup from a stack, empty a water cup from a stack, drink milk cups from a stack; check counts and returned empty cups. The backportedMiners jar is installed in `mods/`.

- SQ-VF (queued 2026-10-25): village farm crops ([crops.md](../crops.md) 2.11), needs the new CD jar installed.

| ID | Where | Test | Pass | Fail points to |
|---|---|---|---|---|
| SQ-VF-01 | B | Boot a fresh world | Log `village farm crops added to N pool elements of 54 listed farm templates`, N at least 54 (pools can hold a template twice), no "failed" | reflection SRG names `f_210411_`/`f_210412_` in `VillageFarmCrops.onServerAboutToStart` |
| SQ-VF-02 | B | `/locate structure` plains, taiga, snowy, desert, savanna village; look at 3 farms each | About a third of the wheat/carrot/potato/beetroot fields are half original crop and half a crop from that climate's table column (crops.md 2.11), cut across the field; the other fields untouched | `process` roll; `CDConfig.worldgen(wildCrops)` |
| SQ-VF-03 | B | Towns & Towers jungle and sparse jungle villages, a Flower Forest village, a sky village over forest and over jungle, Villager Farmer House, Campsite | Jungle farms use the warm wet column, sky villages follow the biome (`auto`); the small structures grow added crops | group `auto`; template ids in `village_farms.json` |
| SQ-VF-04 | B | In those farms, look for popped-off or floating plants (berry bushes, stems, tea, hemp, corn) | Every added plant stands on farmland and stays after a few minutes; berries and stems included; no strawberries, mint, adzuki, blue/cranberries, hamimelon or grapes anywhere | crops that need other ground: remove from the group |
| SQ-VF-05 | B | `wildCrops = false`, new village | Farms are vanilla | the gate in `process` |

## More Delight 26.05.20 (mod-additions #25)

Needs the patcherMod and Creative Delight jars rebuilt and installed. Details: [../more_delight.md](../more_delight.md) 4.

| ID | Group | Action | Expect | Depends on |
|---|---|---|---|---|
| SQ-MRD-01 | B | `/dumpgear` knives | Wooden Knife 1.5 / 2.0, mining speed 2, level 0; Stone Knife 2.5 / 2.0, speed 4, level 1 | tuneTools |
| SQ-MRD-02 | B | `/dumpfood` | Toast, the 5 topped toasts, Caviar Toast and Cheese Toast flagged fast; Cheese Toast 5 hunger, 0.7 | CD `moredelight/toast.js` |
| SQ-MRD-03 | B | `/dumpall` recipe dump, `/dumptags` | `creative_delight:...cheese_toast` shapeless (Toast + Flaxen Cheese Wedge); `forge:sliced_potato` holds Rustic's slices; the four renamed pack tags filled; no use of the plural names | tags |

## Food family recipe helpers (food_families.md 4.5)

Needs the Creative Delight jar rebuilt and installed (`:creativeDelight:build`, `installToMods`).

| ID | Group | Action | Expect | Depends on |
|---|---|---|---|---|
| SQ-FFH-01 | B | Boot, then `/dumpall` and diff the recipe dump against the previous one for cutting, smelting, smoking, campfire and `creativeengineering:baking` ids | Same ids and results as before the change; no `foodFamily` error in `logs/latest.log` | CD `food_family_recipes.js` |
| SQ-FFH-02 | B | Rebuild the CD jar first (`cd_food.js` was edited at 21:29, after the 21:24 build; `:creativeDelight:build`, `installToMods`), boot, then `/dumpall` | No `cd_food.js` parse or `global.CD.food` error in `logs/latest.log`; recipe dump unchanged from SQ-FFH-01 (nothing calls the new helper yet). After the members move to it: same ids and results as before for cutting, cooking trio, `creativeengineering:baking`, grid meals and sequenced assemblies, and no duplicate recipe of a swept output | CD jar; creative_delight.md 30 |

## Storage Delight additions (storage_delight.md section 5)

Needs `repackedStorage-25.12.09-1.19-forge-ce-19.2-01.jar` installed (`:repackedStorage:installToMods`).

| ID | Group | Action | Expect | Depends on |
|---|---|---|---|---|
| SQ-STD-01 | B | Boot a server with the jar and the pack's Quark; `/dumpall` | No `storagedelight` error in `logs/latest.log`; 140 `storagedelight:` blocks, 50 of them new; the 50 recipes load (`storagedelight:` recipe ids for the 36 stone countertops and 14 bamboo blocks) | repackedStorage jar |
| SQ-STD-02 | B | `/give` and place a `storagedelight:bamboo_drawer`, a `storagedelight:oak_cabinet_with_polished_tuff_countertop` and a `storagedelight:warped_cabinet_with_stone_bricks_countertop`; open and close each, break with an axe | Opens as 27 slots, no block entity error, item kept and dropped | repackedStorage jar |

## Abnormals Delight plans (abnormals_delight.md section 3)

Needs the Creative Delight jar rebuilt and installed (`:creativeDelight:build`, `installToMods`).

| ID | Group | Action | Expect | Depends on |
|---|---|---|---|---|
| SQ-ABD-01 | B | Boot, then `/dumpall` and look up `abnormals_delight:upgrade_aquatic/cooked_perch_slice` and `.../cooked_pike_slice` | Both gone; `creative_delight:smelting/cooked_perch_slice` and `.../cooked_pike_slice` present, experience 0.35, cookingtime 200, same input and output; the smoking and campfire recipes unchanged; no error in `logs/latest.log` | CD jar |
| SQ-ABD-02 | B | Same boot: look up the salvaging ids in the dump | `abnormals_delight:upgrade_aquatic/cutting/driftwood_door`, `_sign`, `_trapdoor` gone; `creative_delight:farmersdelightcutting/salvaging/driftwood_furniture` (10 items incl. `abnormals_delight:driftwood_cabinet`, 0.75 driftwood planks, axe), `.../driftwood_chest_boat`, `.../driftwood_furnace_boat`, `.../<wood>_furnace_boat` for the 9 vanilla woods (crimson and warped give `boatload:` boats), `.../crimson_boat`, `.../warped_boat`, `.../crimson_chest_boat`, `.../warped_chest_boat` present; no `fdCutting` warning in `logs/latest.log` | CD jar |
| SQ-ABD-03 | B | Same boot | `abnormals_delight:neapolitan/adzuki_curry` gone; `creative_delight:shapeless/adzuki_curry_cooked_manual_only` takes roasted adzuki beans, dried banana, `#forge:crops/carrot`, pumpkin slice, bowl; `abnormals_delight:neapolitan/cooking/adzuki_curry` unchanged | CD jar |
| SQ-ABD-04 | B | Same boot; `/dumptags` or the tag dump | `#creativeengineering:pickerelweed` = blue + purple; `abnormals_delight:pickerelweed_juice` and `pike_with_beetroot` gone; `creative_delight:farmersdelightcooking/pickerelweed_juice` and `.../pike_with_beetroot` take that tag twice, 1.0 xp, 200 ticks | CD jar |
| SQ-ABD-05 | B | Same boot | `neapolitan:milk/milk_bottles_from_bucket` and `milk_bucket_from_bottles` gone; `creative_delight:shapeless/milk_bottles_from_bucket` (`#forge:buckets/milk` + 4 glass bottles -> 4 `farmersdelight:milk_bottle`) and `.../milk_bucket_from_bottles` (bucket + 4 `#forge:bottles/milk` -> milk bucket) present | CD jar |

## Farmer's Structures donor (farmers_structures.md section 5)

Needs patcherMod rebuilt and installed (`:patcherMod:installToMods`), the Creative Delight jar (hanging canvas sign), and `farmers_upstream/FarmersStructures-1.0.6-1.21.1_neoforge.jar` in the game folder.

| ID | Group | Action | Expect | Depends on |
|---|---|---|---|---|
| SQ-FSD-01 | B | Boot a server; read `logs/latest.log` | One `[farmers-donor] N files from Farmer's Structures 1.0.6 {...}` line; no `Could not translate`; the counts list `blocks mapped minecraft:pink_petals`, no `blocks to air` for a block you expect to exist (rope fence, gleaming salad, hanging canvas sign, basket) | donor jar |
| SQ-FSD-02 | B | Rename the donor jar, boot | One warning that it is missing; the game loads with the 1.0.0 structures only | donor jar |
| SQ-FSD-03 | B | `/place structure farmers_structures:aquacultures_cod` a few times (different spots) | Pond places; fish are cod in some, with pike and perch in others; no empty pond | Upgrade Aquatic |
| SQ-FSD-04 | B | `/place structure` each of `cacaos` (jungle), `fishs_stew`, `ratatouilles`, `squids`, `shepherd_farms` | All place; chests, item frames and cooking pots hold items; the cacao basement piece attaches; ratatouilles and squids sit below ground; the stew hut fish include salmon and pike / perch | |
| SQ-FSD-05 | B | `/place structure farmers_structures:cooks` 10 times | More varied houses than before; rope fences and gates present; azalea flower piles where pink petals were; Vinery cherry leaves; no air holes in walls | |
| SQ-FSD-06 | B | `/locate structure farmers_structures:aquacultures_carp` | Not found (no carp set); `farmers_structures:squids` etc. are locatable in their biomes | |
| SQ-FSD-07 | B | Dump with `/dumpall`; look at `structure_set` for `farmers_structures:*` | Group `remote` holds cod, cacaos, fishs_stew, ratatouilles, shepherd_farms, squids; no `aquacultures_carp` | |
| SQ-FSD-08 | B | Look at the cook house piece with a hanging canvas sign (`cook_end_1_21`, via `/ceplace` or many cooks) | The sign is present, hanging from a ceiling | CD jar |


## Creative Delight food batch, 2026-10-25 (TODO rows in each ID's writeup column)

Needs the Creative Delight jar built 2026-10-25 22:48 (installed in `mods/`). Startup changes (new items, removed items,
the merge): restart. Then `/dumpfood`, `/cegen run` the can generator, restart again for the can changes.

| ID | Group | Action | Expect | Depends on |
|---|---|---|---|---|
| SQ-CDB-01 | A | Boot; `logs/latest.log` | No startup or server script error for `glow_calamari.js`, `veggies_items.js`, `vintage_items.js`, `minecraft/recipes.js`, `miners_delight/*.js`, `oceansdelight/recipes.js`; `[itemMerges]` count one higher | |
| SQ-CDB-02 | A | `/dumpall`; look up the four `#creativeengineering:sweeteners` tags and sugar recipes | Cooking (pumpkin pie, a Fruits Delight jam) takes all five; drinks (`farmersdelight:cooking/hot_cocoa`, a Fruits Delight tea mix, a Create DD milkshake mix) sugar, powdered sugar, honey bottle; Brewin' keg recipes all but glycerol; Botania mana powder, a Pretty Pipes module, the fermented spider eye jar recipe sugar or powdered sugar; powdered sugar and the sugar cube plain sugar (RC-25) | |
| SQ-CDB-03 | A | Same dump: `creative_delight:farmersdelightcutting/squid_to_calamari`, `glow_squid_to_calamari`, `baked_squid_to_calamari`; `miners_delight` squid cuts | The three present (calamari 1 + 50 %, ink 1 + 50 % 2); no Miner's squid-to-tentacles cut | |
| SQ-CDB-04 | A | Summon a squid and a glow squid, kill each with a sword, then with a knife (DA-05, DA-06) | One squid item / glow squid item each kill; no ink sac, calamari or tentacles | |
| SQ-CDB-05 | A | JEI: Glowing Calamari line | 3 items with the palette-swapped sprites (Pickled Sea Pickle too); cut, roll recipes; furnace, smoker and campfire give Cooked Calamari; fried calamari lists glowing calamari | |
| SQ-CDB-06 | A | Fill a jar with a lava bottle, then empty it | Lava goes in and comes back out into a glass bottle | |
| SQ-CDB-07 | A | Fill a jar with a Neapolitan milkshake and with Splash Milk's milk bottle | Each goes in; the milk bottle shows milk | |
| SQ-CDB-08 | A | JEI: Pickled Sea Pickle, Pickled Pickles (VTD-55) | Sea pickle art in pickle shape; Pickling Jar 5 sea pickles + salt -> 5; Pickled Pickles hidden; an old Pickled Pickles stack turns into Pickled Sea Pickles | |
| SQ-CDB-09 | A | Cooking pot: pickle soup with pickled onion, cauliflower, broccoli, beetroot, cabbage leaf (VTD-57); braised sea pickle from 2 slices + 2 brown mushrooms (VTD-58) | Both cook | |
| SQ-CDB-10 | A | JEI: mhadjeb, potato noodle (RC-132, RC-133) | No mhadjeb or uncooked mhadjeb; Gnocchi and Creamy Gnocchi names; knife on sweet potato dough gives 2 gnocchi; Creamy Gnocchi takes milk, 2 gnocchi, onion / garlic / pickled onion | |
| SQ-CDB-11 | A | JEI: Bayberry Juice; heatless cacciatore (RC-D2, RC-136) | The bottle, the fluid and the can read Bayberry Juice; 12 bottles fill a jar (MJ-14); the cacciatore grid recipe cycles cooked chicken cuts | |
| SQ-CDB-12 | A | After `/dumpfood` and `/cegen`: can generator output | No `canned_mhadjeb`; `canned_pickled_pickles` named Canned Pickles from pickle or pickled sea pickle | SQ-CDB-01 |
| SQ-CDB-13 | A | JEI search Bowl of Water; `/give @s botania:water_bowl`, hover it | Not in JEI; the item has no tags | |
| SQ-CDB-14 | client | JEI uses of the empty Sample Syringe (fixed 2026-10-26 run, `sample_syringe.js` `const` -> `let`) | The world page lists each mob and its filled syringe; no `redeclaration of var $WorldRows` in `logs/latest.log` | |
| SQ-CDB-15 | client | Craft Cake Base with 2 chicken eggs, 3 flour and 2 honey bottles; Pie Crust from cornmeal; Cacciatore with cooked chicken cuts | All three craft (fixed 2026-10-26 run, `Item.of(tag, n)`) | |

## End's and Ender's Delight, 2026-10-25 ([ends_delight.md](../ends_delight.md) 5.6)

Needs the CD jar built 2026-10-25 (`repackedEnds -02`, `endersAdditions -01`) and the patcherMod it needs, installed; full
restart (new items, member, worldgen). New End chunks for EN-02, EN-06, EN-10. Then `/dumpall` and `/cegen run worldgen`.

| ID | Group | Action | Expect | Depends on |
|---|---|---|---|---|
| SQ-EN-01 | A | Boot; `logs/latest.log` | `enders_additions` and `ends_delight.ce.mixins.json` load; no missing model / texture for `endersdelight:` plants; no script error in `ends_delight/*.js`, `endersdelight/*.js`; wild-crops log lists the 5 End rules (EN-16) | |
| SQ-EN-02 | A | Break a Chorus Crate and an Endstone Stove (EN-01) | Each drops itself | |
| SQ-EN-03 | A | Place the 4 plants on end stone, tilled end stone, dirt, farmland (EN-03); random tick speed 100 on both soils (EN-04, EN-05) | Only the two soils; faster on tilled; void pepper spreads then fruits, 3 / 1 drops | |
| SQ-EN-04 | A | Kill the Ender Dragon bare-handed, with a knife, with a machete (`/summon`) (EN-07) | 5-7 meat, 2 legs, 1-2 teeth; +2 meat +1 tooth with knife or machete | |
| SQ-EN-05 | A | JEI and tooltips: the 10 drop items; Dragon Tooth Knife; the 5 dishes; Crushed Void Pepper; crate unpack; End's grid meals (EN-08, EN-09, EN-11 to EN-14) | As ends_delight.md 5.6 | SQ-EN-01 |
| SQ-EN-06 | A | Anvil: damaged Dragon Tooth Knife + Dragon Tooth (EN-09) | Repairs | |
| SQ-EN-07 | A | New End world: `/locate structure endersdelight:end_house`, fly the highlands; earth fill log ALL (EN-06, EN-02, EN-10) | Houses only in highlands / midlands, on ground, no chests; plants and succulent wild on end stone | SQ-EN-01 |
| SQ-EN-08 | A | Look at the 25 retextured items; eat Stuffed Shulker with an empty hand (EN-15) | New art; message names the Shulker Bowl | |
| SQ-LR-01 | A | Set `debug.level_random.probe = true`, play a session with Distant Horizons generating and mobs near (Regrowth); `grep LevelRandomProbe logs/latest.log` | No crash; each `[LevelRandomProbe]` entry names the off-thread caller (or none appears, then DH FEATURES mode was not the cause). Reset to false after | |

## Server fixes, 2026-10-26 (TODO `server #3`, `server #6`)

Needs patcherMod 1.1.03 built 2026-10-26 (in `mods/`). SQ-SV-02 needs a client connected to the scratch server.

| ID | Group | Action | Expect | Depends on |
|---|---|---|---|---|
| SQ-SV-01 | A | End: `/execute in minecraft:the_end run tp` an armor stand to 300016 100 300016 (ST-232); then a pearl onto Myalite at a spire viaduct | The tp takes milliseconds, no "ticks behind", no region file at the target; the viaduct warp still works | |
| SQ-SV-02 | client | Blow up a spot with Quark / Every Compat blocks and vanilla blocks (FX-47, FX-13) | Every change shows; no "Error executing task on Client"; `[debug.block_state_sync]` on: no unresolved id | |

## Pack perf events, 2026-10-26 (TODO `server #9`, perf.md 3.6)

Needs patcherMod 1.1.03 rebuilt 2026-10-26 (the perf mixins) and the server booted with the recording: add
`-XX:FlightRecorderOptions=stackdepth=256,repository=perf/jfr-repo -XX:StartFlightRecording=name=ce,settings=perf/ce-server.jfc,disk=true,maxage=30m,maxsize=512m`
to the scratch server's java line and copy `server/perf/ce-server.jfc` to `OUT/srv/perf/`. Grep `latest.log` for
`Perf` / `KubeJSEventHandler` mixin warnings first.

| ID | Group | Action | Expect | Depends on |
|---|---|---|---|---|
| SQ-PF-01 | A | Boot; `/ceperf status` (PF-14) | Recording `ce` running; all 10 events recorded; hooks: all run except possibly chunk wait; KubeJS handlers wrapped > 0; no mixin warning for the 9 perf mixins | |
| SQ-PF-02 | A | After 2 min, `/ceperf dump smoke`; copy the file here, `python3 tools/perf/perf_report.py <file>` (PF-15) | Chat says saved; the report's "Measured by the pack's hooks" has ticks, a row per dimension, handlers as `server_scripts:<file>:<line>`, entity and block entity types | SQ-PF-01 |
| SQ-PF-03 | B | New world: `/tp` 5 times to unexplored land 2000+ blocks apart, then `/ceperf dump tp` and report (PF-16) | Chunk waits lists waits over 1 ms with a caller and phase; totals non-zero | SQ-PF-01 |
| SQ-PF-04 | B | Chunky pregen 2601 chunks with the jfc as is, then with every `creativeengineering.*` set to `enabled=false` (PF-17) | Same time within noise (1:34 in the 2026-09-28 A/B) | SQ-PF-01 |
| SQ-PF-05 | A | `/reload` twice, `/ceperf status` (PF-18) | Wrapped-handler count unchanged; no KubeJS errors | SQ-PF-01 |
| SQ-PF-06 | client | Single player without a recording: play a minute, `/ceperf status` (PF-19) | No errors; "no JFR recording is running"; events listed as not recorded | |
