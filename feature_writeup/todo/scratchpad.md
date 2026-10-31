# Scratchpad

## Idea, Notes, Changes (Human scratchpad, don't write up or add to plans unless directed)


## Low-effort lists (compiled by Claude 2026-10-29 from TODO.md, briefs/ and testing/scratch_queue.md)

Rough picks, ranked by how little each needs; ids are TODO row ids or scratch queue entry ids. Check the row before
starting: a few may have moved on since the last TODO sweep.

### 1. Decisions (brief already has a recommendation: accept or override)

Folded into the TODO rows' Decisions cells 2026-10-30 (`decided (owner, 2026-10-30)`); blank and "more info" cells are still open.

| Row | Question | Brief's recommendation | Decision |
|---|---|---| --- |
| `recipes/metal #6` More gold sources | whether | Small yes: gravel and sand panning at 5 %, after `recipes/metal #3` | find gold pan, sluice model and texture|
| `loot/trader #2` Fewer Wares trades | how many | w = 3 (w = 2 if `loot/trader #3` adds agreements as loot) | 3x less|
| `potions/potions #6` Saturation potion | stronger? | Base amp 5, strong amp 9 (strong = full refill); check PO-31 for buckets first | add strong saturation, each lv is 3 hunger bars (3x saturation 1, up to level 4). acts as instant health for hunger bar |
| `engineering/sawmill-and-components #9` Wire alternatives | whether | Yes, same-class tags first; x2 lower-class recipes only for 3-5 machines | more info |
| `engineering/sawmill-and-components #4` One centre per component | which centre | Copper rod and electrum component | more info |
| `gear/tuning #8` Hardened leather uses | which recipes | Backpack tier 1, leather horse armor, new saddle; not in `#forge:leather` | hardened leather or erstatz leather required in sophisticated backpacks, and new saddle recipe of leatherlike:l tripwire:t "lll" "ltl", new horse armor recipe (remove old) of "  l" "lll" "l l" |
| `farming/crops #8` Watering can | which mod | Own small item; hydration plus a small random-tick chance below bone meal | in bountiful harvest or extra delight, postpone until more large content add |
| `mobs/mooblooms #3` Per-biome shares | retune now? | After the generator (hand edits would be overwritten); 2 % tier after MB-95 |  |
| `baking/sweets #3` Cold sweets variants | which | Custard: sweet berry, strawberry, mango; shaved ice: melon, lemon, peach; popsicle: 5 fruits | |
| `dishes/meals #27` Pineapple pizza | tooltip text | Yours to write; brief says keep it a separate item | name item tropical pizza, tooltip: 'Misnomer, invented in Canada' |
| `dishes/meals #44` Stuffed pumpkin | which recipe stays | (no recommendation; pick one of two) | new recipe, tomato sauce + any raw potato + any bread + any berries + any mushroom + any veggie|
| `loot/trader #4` Rustic pepper seeds | price, rarity | (no recommendation; two numbers) | |
| `structures/earthfill #2` `maxWaterSurplus` | first value | (default is 0.5; one number) | |

### 2. Features to implement (Decisions = none, no jar rebuild, short scope)

| Row | Change | Work |
|---|---|---|
| `ui/names #8` | Rename "Roll of Ender Guardian" to "Ender Guardian Roll" | lang override |
| `ui/names #7` | Every lang entry says Autumn, never Fall | lang / resource pack |
| `engineering/dreams-and-desires #1` | Smoked wood recipe | KubeJS |
| `engineering/dreams-and-desires #3` | Rose quartz sand blasting, 80 % rough / 20 % smooth | KubeJS + patcherMod | Built 2026-10-30, jar not installed, untested (EN-19..21 in scratch_queue) |
| `recipes/engineering #4` | Horse cart crafting recipes | KubeJS |
| `recipes/engineering #5` | Buckets from iron plates; wooden water bucket | KubeJS + patcherMod | Built 2026-10-29, jar not installed, untested (RE-28..30 in scratch_queue) |
| `jei/jei-ordering #3` | Group MCA's items in JEI | KubeJS |
| `creativedelight/members #23` | Storage Delight: mod name tag "(Creative Delight)", JEI wood order | KubeJS / lang |
| `creativedelight/members #24` | Seagrass salad takes 2 sea grass; disable tentacles | KubeJS / data |
| `creativedelight/members #22` | Rustic cutover cleanups: remove Fruit Beignet, follow cotton rename | KubeJS |
| `farming/crops #13` | Every seed (tobacco too) fits in seed pouches | tag |
| `farming/crops #21` | Melon stems do not need fertile soil; check melon-like crops | KubeJS / data |
| `loot/tables #3` | Lower chalk frequency, skew to libraries and underground | KubeJS |
| `loot/tables #4` | Boost crates and sacks in village / farming / cooking houses | KubeJS |
| `mobs/spawns #4` | Lone villagers in plains, hills, fields, meadows | data |
| `food/stack-sizes #2` | Rerun `/dumpstacks` and `tools/stack_sizes.py` | tool |
| `textures/art #5` (Bug) | Coffee and bell pepper crop blocks have no texture | resource pack |
| `textures/art #4` (Bug) | White fridge addition renders with missing texture | resource pack |
| `potions/effects #7` | Check Faraday armor blocks shock damage | check only |

Note: `textures/art #4` / `#5` may already be covered by the 2026-10-27 texture fixes (queue section "Texture fixes,
2026-10-27"); confirm before redoing.

### 3. Tests to verify (cheapest: superflat group A, boot or one command, no player)

Open by a script check against the run logs (no PASS / FAIL found for these ids); confirm in the Run log first.

| Entry | Check |
|---|---|
| SQ-SALT-01 retest | `zq uses salt:salt_block` lists 3 froglight recipes; `zq makes vintagedelight:salt_bucket` and `powdered_sugar_bucket` give 0 fishing recipes |
| SQ-SALT-02, -03 rest | Salting craft and cauldron evaporation (not run 2026-10-28) |
| SQ-CNS-01, SQ-DB-00, SQ-FF-00, SQ-FFB-00, SQ-PR-00 | Boot, grep `latest.log` |
| SQ-LE-01, SQ-MDP-12 | grep `latest.log` (MDP-12 after `/reload`) |
| SQ-LE-04, SQ-LE-05 | Probe potion mixes; probe leatherworker trades (no bedrolls) |
| SQ-PS-02, SQ-DB-02, SQ-CN-05 | Tag dump / `/kubejs tags item ...` |
| SQ-SNF-02, SQ-MDP-13, SQ-CN-02 | Recipe id dump, grep |
| SQ-CDT-04, SQ-CDT-05 | `/cdtest ns / requires / tags / config / mirror` |
| SQ-VTD4-07, SQ-VTD4-08 | `/loottuning`; `/vtdtest fluids` |
| SQ-FF-02, SQ-DDP-04 | `/dumpplaceable` (+ `tools/placeable_catalog.py`) |
| SQ-CDM-04, SQ-CDF-05 | `/dumptrades`; `/dumpcompost` |
| SQ-CN-03, SQ-CN-06 | Composter and villager food probes |
| SQ-RSP-11, -12, -13 | Kettle break / replace, tea bush pot, two food effects (a player or fake player) |
| CQ-DUMP-* (client) | Client-only dumps; one window, unblocks the Vintage rows and UI generators |

### 4. Possibly forgotten (compiled by Claude 2026-10-30; Planned/Bug rows with Decisions = none, no jar; check the row before starting)

Ready now (KubeJS or data):

| Row | Change |
|---|---|
| `engineering/dreams-and-desires #2` | Rubber chain: Myrtrees latex to rubber, D&D sap machine on rubber logs, rubber to belts |
| `engineering/sawmill-and-components #12` | Redstone mechanism: low tier redstone component variety, cheaper low tier recipes |
| `engineering/dreams-and-desires #6` | Painted asphalt: rename, 8 Immersive Petroleum asphalt + 1 dye, soaping recipe back |
| `recipes/engineering #6` | Create add-on machine recipes on components (mechanical arm, helve hammer, centrifuge, curving press, coiler) |
| `recipes/wood #4` | Leather from fermenting bark |
| `ports/cavesandchasmsport #2`, `#3` | Tin out, nickel in; C&C silver to IE silver |
| `dishes/pickles-and-ferments #11` | Vinegar: drop bottle recipe, accept any jam |
| `merges/dishes #11`, `#6`, `#2`, `#1`, `#10` | Drop fish tempura; merge Caesar salads; mashed potatoes split; fish and chips; burger audit and loaded burger |
| `dishes/meals #40` | New sushi roll medleys (hostile, shrimp and sea pickle, egg roll, bell pepper) |
| `food/drinks #10` | Azalea tea (no live recipe, needs Ecologics which is not installed) |
| `food/drinks #1` | Lemonade, pink and blue variants |
| `food/drinks #11` | Re-add Cherry Cream Soda and a fruit soda line (`jeed_providers.js` still lists it) |
| `blocks/dyeing #2` | Coloured water dyeing fan recipe |
| `blocks/new #7` | Mushroom crates |
| `blocks/new #5` | Lit Botania fel pumpkin |
| `farming/crops #22` | Seed bags for more crops |
| `farming/composting #4` | Seed milling to bone meal |
| `loot/tables #6` | MCA items in village houses |
| `loot/tables #12` | Azalea plants in lush loot |
| `mobs/spawns #5` | Allay spawns (pillager cages, mystical biomes, more structures) |
| `mobs/mooblooms #4` | Skylands cows and mooblooms |
| `mobs/pillagers #2` | End pillagers in diving gear |
| `items/jars #3` | More foods in jars |
| `largecontent/mod-additions #2` | Mod family tags |

Probably broken now (quick look first):

| Row | Problem |
|---|---|
| `largecontent/mod-additions #31` | Corn Delight chest loot never loads (`mmlib:add_loot_table`, mmlib not in the pack) |
| `loot/tables #10` | NEA quiver enchanted books still in loot after the 1.0.9 fix |
| `tooling/conventions #7` | `NCH.ieBlueprint` repeated inputs never match (9 IE workbench recipes) |
| `cooking/oven #3` | Muffins may not be cookable |
| `farming/crops #4` | Weeds audit |

Human notes of 2026-10-31 were translated into TODO rows (scratchpad 2026-10-31); this section is kept as history.
