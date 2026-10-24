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
- Zombies are undead: no Regeneration or Poison on them (use a pillager). Adding a placed feature to another biome
  in the same step (scratch biome modifier) crashes spawn generation with a feature order cycle.

## Queue

### Round 0: Fungi Delight port (added 2026-10-23, run first)

Feature: [../fungi_delight.md](../fungi_delight.md) §4.0 / §8 (FGD-), [../potions.md](../potions.md) PO-27, TODO
`ports/fungiport #1`. Target: `backportedFungi-1.3-ce-19.2-01` and patcherMod `creativeengineering-1.1.01` md5 378f7897
(mooshroom family, `brown_chance`, CEConfig `mushroomSpawns`), both built 2026-10-23 and **not in `mods/`**; KubeJS
tree as of 2026-10-23 (mushroom_spawns, `_biome_rules.js`, wild_crops `step`, mob_spawns rules, cooking.js Fungi block,
baking.js truffle pie, compost rows, jei_info.js).

Before the run (game closed, `pgrep -c java` is 0): `./gradlew :backportedFungi:installToMods :patcherMod:installToMods
--offline`. **Never boot without the port jar:** `kubejs/data/creativeengineering/worldgen/placed_feature/wild/mushrooms/`
references `fungidelight:*` features and blocks, and a world fails to load without them. No probe exists yet: write
`tools/fungi_probe/fungi_probe.js` (command `/fgdtest <part>`, output `[fgdtest] PASS|FAIL <part> ...`, the `/fdtest`
style) during the run; rows name the parts. Count worldgen blocks offline from region files (section palettes, the
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

None of the jars below has ever booted: `backportedVeggies-1.9.2-ce-19.2-02`, `backportedVintage-0.1.6-ce-19.2-03`,
`incubationAdditions-1.1.0` (goose golden egg; `server_mods/` still holds Veggies -01 and Incubation Additions 1.0.0),
and the five unintegrated add-on jars (`ends_delight`, `Hostile_Delight`, `miners_delight`, `oceanic_delight`,
`oceansdelight`). The KubeJS tree holds uncommitted work on top (cooking.js, baking.js, cookingtags.js, item_dict.js,
compost_values.js, can_types.json, food_types.json, wild_crops.json, soup soft fluids, Vinery grape bush `tries`).
Boot-time failures are likely: run SQ-VP-00 first and fix before the rest.

Before the run (game closed, `pgrep -c java` is 0): `./gradlew :patcherMod:installToMods --offline` only if the
source changed since md5 a4d48336; check `mods/` holds the jars above; `diff` the `mods/` set against `server_mods/`
(the scratch server reads `mods/` through `mod_sides.md`, so `server_mods/` staleness is a deploy matter, not a test one).
No probe exists for the Vintage port yet: write `tools/vintage_probe/vintage_probe.js` (command `/vtdtest <part>`,
output `[vtdtest] PASS|FAIL <part> ...` as `/fdtest` does, copy of its style) during the run; the entries name the parts.

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
| SQ-VP-13 | A | `/vtdtest egg` after setting `goldenEggChance` to 1 in `config/incubation_additions-common.toml` and a restart | NE-15, NE-16 logic: a goose (Incubation Additions) with a goose nest in a fenced pen drops a Golden Egg item on the ground at lay time (400-tick window), never walks to the nest, the nest stays empty; with chance 0 geese lay into nests as before; a goose against a wall with open ground within 3 blocks drops on open ground, in a closed 1x1 pen where it stands; never a golden egg in a goose nest. A Golden Egg block drops its reward table (VTD-09). Default config key `goldenEggChance` = 1000 (1 in 1000) | `incubationAdditions/.../goldenegg/DropSpot` (logic test 9 / 9 passes headless), `IAConfig`; the config toml appears after the first boot of 1.1.0 |
| SQ-VP-14 | B (new normal world) | `/zfw wild` extended with the 9 rules, then `/ceplace`, `forceload add` sampling | VTD-06 / VD-04 / VD-05: wild oats in cold and temperate grassland; peanuts warm and dry; cucumbers wet / cold-temperate grassy; broccoli, cauliflower, garlic temperate; sweet potato, zucchini warm dry; turnips only where sea beets grow; each places on grass, podzol, Rich Soil, Fertile Soil and Immersive Weathering soils; salt patches on beaches and stony shores; mature dandelion in the 61 biomes of `follows` and nowhere else, more often than wild crops (VD-06). `tools/test_wild_crops.sh` and `test_wild_flowers.sh` already pass offline | `wild_crops.json`, `crops_by_biome.md`, `gen_soil_compat.py`; Vinery grape bush `tries` changed 36 -> 33 (also check the four grape feature JSONs load: no `Failed to load configured feature` line) |
| SQ-VP-15 | B-nether (new world, worldgen registry change) | `forceload add` over a crimson and a warped forest; count blocks | VTD-07: wild ghost peppers generate in crimson and warped forests on nylium and nowhere else in the Nether (`nether_worldgen_rules.json` `literal` modifier); `no feature order cycle` crash is the known risk of adding a placed feature | `gen_nether_worldgen.py`; memory `nether-worldgen-decisions` |
| SQ-VP-16 | B | `/locate` or `/ceplace` a village, plain / desert / savanna / snowy / taiga | VD-30: Veggies depot houses appear in the 5 vanilla village styles at weight 4 x `mod/veggiesdelight` multiplier 0.5, in `pool_config.json` after the first world load; their chests are Lootr chests | `pool_tags.json`, port injection (normal priority, PoolTuner LOWEST) |
| SQ-VP-17 | A | boot | Fertile Soul Soil (renamed Nulch): recipe still crafts (3 netherrack, 2 scales, ash, magma cream, nether wart, blaze powder -> 6; Create mixing 2 netherrack, 1 scale) with the same item id; name is lang only | `sawdust.js` line 83; recipe review is a TODO (`composting #2`), not a test |

#### SQ-MD: Miner's Delight cave carrot worldgen (built 2026-10-24, works with the 1.1.2 jar)

Features: [../miners_delight.md](../miners_delight.md) 4.3, `kubejs/generation_scripts/worldgen/mushroom_spawns.json` rule `cave_carrots`,
placed features `kubejs/data/creativeengineering/worldgen/placed_feature/wild/cave_carrots/`. New world needed (only new chunks change).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-MD-01 | B (new world) | boot; Chunky pregen radius 400; count `miners_delight:wild_cave_carrots` offline by Y (region files, one histogram per 8 layers) and by biome | `[mushroom-spawns] ... 3 mod modifiers off` (FD brown, red, `miners_delight:cave_carrots`) in the log; no error naming `cave_carrots`; **zero blocks above Y 48**; counts per Y layer: core (-24..24) about twice the layers 25..44 and -64..-25, all clearly non-zero; none within 13 blocks of the surface; found in noise and carver caves alike | height cap: lower `max_inclusive` of `high.json` (origin is Y 44 + 1 offset + 3 patch spread); too few: `rarity_filter` chances in the three bands; carver caves empty: the `cave_air` entries in the scan and the predicate |

#### SQ-MDP: Miner's Delight merged backport (built 2026-10-24, jar not yet in `mods/`)

Features: [../miners_delight.md](../miners_delight.md) 4.1, `development/backportedMiners`. The scratch server needs
`backportedMiners-1.4.5-ce-19.2-01.jar` in `mods/` **instead of** `miners_delight-1.19.2-1.1.2.jar` (same mod id; take it
from `development/backportedMiners/build/libs/` if not installed yet) and `miners_upstream/` next to `mods/`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-MDP-01 | A | boot | MND-01 server half: `Done (`; `Miner's Delight upstream data: 182 files`; `Cup conversions: 15 bowl foods`; no registry, mixin, recipe, loot table or loot modifier error naming `miners_delight` (in particular none for `scavenging_cooked_*` or `any_of`); CCK applies `CopperPotBlockEntityMixin`, `StickyBasketBlockEntityMixin`, `StickyBasketBlockEntityAccessor` without "target not found" | translator (`MinersTranslator`) or the CCK hook shapes (P6) |
| SQ-MDP-02 | A | boot without `miners_upstream/` | MND-04: startup stops with the message naming `miners_upstream` and the Modrinth link | `UpstreamJars.verifyOrFail` |
| SQ-MDP-03 | A | `/setblock` a lit campfire and a copper pot on it; `/data merge block` the pot's `Inventory` slots 0-3 with `miners_delight:cave_carrot`, `minecraft:red_mushroom`, 2 x `minecraft:brown_mushroom` (the cave soup recipe, 200 ticks, so 160 in the pot); wait 10 s; `/data get block` | MND-10 / MND-12 server half: the meal slot holds 2 `cave_soup_cup` and `Container` is `copper_cup` | `CopperPotBlockEntity.servedResult` or the cup table |
| SQ-MDP-04 | A | `/loot spawn ~ ~ ~ kill @e[type=squid,limit=1]` with and without a knife in the mainhand (a fake player or `/loot ... mainhand`) | MND-30 server half: knifeless squid gives tentacles 1-3; knife adds the squid item | the GLM types (`miners_delight:add_items` / `replace_items`) or `any_of` translation |

#### SQ-AD: five unintegrated Delight add-ons (boot only)

Features: [../delight_addons_2026_10.md](../delight_addons_2026_10.md), TODO `ports/mod-additions #4-#12`. Nothing is integrated, so
the only server check is that they load beside everything else.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-AD-00 | A | boot | `ends_delight`, `hostiledelight`, `miners_delight`, `oceanic_delight`, `oceansdelight` all appear in the mod list; `Done (`; no registry, mixin or tag error naming them; recipes load (no `Failed to parse`, except Hostile Delight's `grollrecp` / `grolllrecp`: known upstream bug B1 in [../hostile_delight.md](../hostile_delight.md), removed at integration) | Remove the jar from `mods/` and say so in `mod_sides.md`; Farmer's Delight needs to be present |
| SQ-AD-01 | A | `/dumpall` (recipes, tags, loot) copied into `kubejs/exported/` | Dumps exist for the overlap work (`mod-additions #4-#6, #9`); tag overlap counts for tentacle, chorus, guardian written to `delight_addons_2026_10.md` section 3 | none |

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
| SQ-SN-00 | A | boot | `latest.log` has `[seasoning] defined creativeengineering:cheese`, `...:onion`, `...:garlic` and no `[seasoning] bad definition`; `server.log` has `seasoning done`, no `[NCH] season` warning; no `Parsing error loading recipe creativeengineering:shapeless/season/` | `startup_scripts/cooking/seasonings.js` load order (needs `global.item`, `global.effect`) or `NCH.season` JSON |
| SQ-SN-01 | A | recipe probe (RecipeManager id dump) | Present, type `creativeengineering:seasoning`: `creativeengineering:shapeless/season/{cheese,onion,garlic,garlic_onion}_on_corn_delight_popcorn` | serializer registration (`SeasoningRecipe.register`) |

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

Anything that fails here is written to the feature's writeup first, then fixed; a pass is ticked in the writeup and the
TODO row's status word changes.

## Run log

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
