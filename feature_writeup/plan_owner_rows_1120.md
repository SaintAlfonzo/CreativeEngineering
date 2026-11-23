# Owner row batch (2026-11-20)

<!-- generated:status_index begin open_work sha1=897ca870b228 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 26 open TODO rows (Planned 1, In Progress 1, Testing 24), 4 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `crops #59` | Planned | S | Botany Pots rules |
| `farming/crops #3` | In Progress | S P | Harvest and replant audit for new crops |
| `baking-types-and-bakeware #1` | Testing | SQ-FF-28 | Gingerbread block recipes |
| `baking/baked-goods #39` | Testing | SQ-FF-26 SQ-FF-27 CQ-FF-05 | Baked families: croissant to misc baked, plain donut beside the cider donut |
| `conventions #39` | Testing | SQ-RFX-28 | Creative Compat config comments: British spelling of color (CompatConfig.java lines 112, 1426) |
| `creativebrewing/publish #25` | Testing | S | Delight's IE Bottling dough recipe writes a tag as its result |
| `dishes/ingredients #36` | Testing | S C P | Bone broth as a fluid |
| `dishes/ocean #11` | Testing | S C P | Guardian Shell: guardian drop and cutting |
| `dishes/ocean #12` | Testing | S P | Guardian Soup recipe |
| `farming/composting #5` | Testing | S P | Expand organic compost |
| `farming/composting #6` | Testing | S P | Merge the organic mash compost recipe |
| `farming/crops #36` | Testing | SQ-BOT-06 SQ-BOT-07 CQ-BOT-01 | Every seed in the Botany Pots |
| `gear/tuning #12` | Testing | S | End gear recipes configurable |
| `items/jars #6` | Testing | C | Reword the Jar Storage "one kind per jar" JEI row (hyperxp and toast) |
| `jei/jei-coverage #29` | Testing | C | Maple bacon and cookies missing from JEI |
| `largecontent/mod-additions #46` | Testing | S P | Hostile Delight: Magma and Slime Brownies |
| `largecontent/mod-additions #48` | Testing | S P | Hostile Delight: rotten foods hunger type, not saltable |
| `largecontent/mod-additions #49` | Testing | S P | Hostile Delight: Suspicious Bacon and Eggs assembly |
| `licence/cd #4` | Testing | S | Rewrite the Miner's Delight potted gossypium loot table |
| `loot/tables #18` | Testing | S P | Remove Lexica Botania from loot |
| `loot/trader #2` | Testing | S P | Fewer Wares trades |
| `potions/enchanting #1` | Testing | S P | Enchanted books out of villager trades |
| `recipes/metal #5` | Testing | S P | Polished iron bars stonecutting removed |
| `recipes/wood #1` | Testing | S P | Twisted wood duplicate |
| `rice-egg-and-sushi #1` | Testing | S P | Bird eggs fry into the fried egg; parrot, turtle and reptile eggs do not |
| `tooling/dictionaries #2` | Testing | S | Block, fluid and NCH fluid tags not validated |
<!-- generated:status_index end open_work -->

Status: **G0 to G6 built and installed 2026-11-21, G7 tooling built 2026-11-22** (the Quark list and tag fixes wait for the owner's `/dumpall`; `farming/crops #3` log lists what is left). Per-row state is in the TODO rows. The owner picked 28 TODO rows on 2026-11-20 (chat), mostly small ones. The
premise checks came from three read-only passes on 2026-11-20, and the owner answered the open items in four
AskUserQuestion rounds the same day. Status lives in the TODO rows; this plan holds the rulings, the lanes and the order.

## 1. Intake

| # | Row | Owner's words (2026-11-20) | Class | Premise check (2026-11-20) |
|---|---|---|---|---|
| 1 | `conventions #39` | (row as written) | clear | Holds. `CompatConfig.java:112` and `:1426` spell color the British way. "neighbours" (`:703`, `:727`, `:865`, `:868`, `:902`) is a toml key, a Java field and `PassTier.Neighbours`. |
| 2 | `engineering/sawmill-and-components #6` | (row) | clear | Holds. The lines moved to `kubejs/data/creativeengineering/kubejs/engineering/components.js:20` (`Light?`) and `:24` (`Dense?`). |
| 3 | `creativebrewing/publish #25` | (row) | clear | Holds, plus a second instance. `kubejs/data/creativedelight/kubejs/create/recipes.js:52` and `:54` both take `i.dough` (`#forge:dough/wheat`, `item_dict.js:2129`) as a result. The concrete item is `i.in_dough` (`create:dough`, `item_dict.js:2013`). `_itemJson` (NCH `:603-611`) writes a tag as `{item:"#..."}` even with allowTag false. |
| 4 | `items/jars #6` | (row) | clear | Holds. The text "Only one kind per jar." comes from `tools/jei_world/tab_drops.py:278` (`cookie_rows`), which writes it to `kubejs/assets/creativecommon/jei_world/jar_storage/generated.json`. It is not a lang key. |
| 5 | `jei/jei-coverage #29` | "identify any other jei items improperly hidden" | premise (false) | Not on any hide list. Abnormals Delight gives compat items a creative tab only when their parent mod is installed. Autumnity and Environmental are absent, so the items have no tab and never show in JEI (`jei_ordering.md:250`). Same cause, but with pack recipes: `abnormals_delight:maple_glazed_bacon`, `cherry_cookie`, `duck_fillet`, `cooked_duck_fillet`, `duck_noodles`. `escargot` has a salting recipe and no clear source. `maple_cookie` merges into the Syrup Cookie (`food/types-and-tuning #10`, owner 2026-11-17). No pack-wide "no tab but obtainable" list exists. The Compat JEI order report has never been exported. |
| 6 | `recipes/wood #1` | (row) | clear | Holds. All 7 `immersiveengineering:sawmill/stripped_{oak,spruce,birch,dark_oak,jungle,acacia,mangrove}_log` are still in `kubejs/exported/recipes/all.csv`. The removal list at `kubejs/data/creativeengineering/kubejs/wood/bark.js:90-95` has only crimson and warped. The twisted comment at `:64` is stale. |
| 7 | `recipes/metal #5` | (row) | clear | Holds. Stonecutting is removed at `metal_building.js:13`; there is no polishing recipe. NCH has no Create sandpaper helper (`viPolishing` at NCH `:2708` is a sequenced-assembly step). |
| 8 | `loot/tables #18` | (row) | clear | Holds. `botania:lexicon` is in `inject/simple_dungeon` (weight 20) and `inject/spawn_bonus_chest`. The switch is `kubejs/data/creativeloot/kubejs/loot/loot_config.json:300` (`"remove": false`). |
| 9 | `loot/trader #2` | "8x" | clear | Holds. Today it is `kubejs/server_scripts/economy/wandering_trades.js:19`, weight 20. "8x" supersedes the 2026-10-30 "3x less" ruling (weight about 7). |
| 10 | `pack/release #8` | (row) | open -> ruled | No public notes file exists. `README.md` "Gameplay highlights" (line 31) already mentions wild crops. |
| 11 | `largecontent/mod-additions #48` | "like rotten flesh and rotten apple for all rotten foods, fried turtle egg, suspicious egg and ham (rename green eggs and ham)" | open -> ruled | There is no "hunger" type. Rotten flesh and rotten apple are `hazard` (`kubejs/generation_scripts/food/food_types.json`), and so are Rotten Ham and Cooked Rotten Ham. Rotten Pie and Green Pie are `sweet`, Suspicious Bacon and Eggs is `meal`, and Fried Turtle Egg is `egg`. Salt list: `kubejs/data/creativedelight/kubejs/salt/tags.js:44-45`. |
| 12 | (new row) | "drop turtle and parrot egg from fried egg" / "allow every standard egg but parrot or turtle, like duck and goose, to be fried" | open -> ruled | The fried egg cooks only from `minecraft:egg` (`F.eggCook`, `farmersdelight/recipes.js:299`). `#forge:eggs`, `balm:eggs` and `rusticdelight:raw_cooked_eggs` hold the turtle egg and the 5 Quark parrot eggs, plus the AM crocodile, terrapin and emu eggs and the duck and goose eggs. |
| 13 | `largecontent/mod-additions #49` | (row) | clear | Holds. Upstream `hostiledelight:susbanderecp` is shapeless: 2 `friedturtleegg` + `cookedrottenham` + bowl. The pattern to copy is `farmersdelight/recipes.js:203` `F.assemble`. |
| 14 | `largecontent/mod-additions #46` | "these are gummies now, merge into gummy family" / "magma and slime gummy for names. glow, salmonberry, cantaloupe, matcha gummies exist. identify recipe patterns but dont apply yet" | open -> ruled | Matches the 2026-11-18 ruling on `baking/baked-goods #18` (rename, keep the sprites). There is no gummy family: the gummies sit in `candy/piece` (`tools/food_families.py:120`). `baking/sweets #1` (Gummies) is In Progress with nothing built. |
| 15 | `dishes/ocean #11` | (row) | open -> ruled | Holds. The item is `oceansdelight:guardian` and has no dict key. Hostile replaces `minecraft:entities/guardian` outright (2 rolls of fillet 1-3, shard 1-4, crystals 0-2). Ocean's adds its item through the GLM `farmersdelight:guardian_drop`. Cutting is `oceansdelight:cutting/guardian` -> `guardian_tail` + bone meal. |
| 16 | `dishes/ocean #12` | (row) | clear | Holds. The current recipe is the upstream pot (guardian + onion + 2 eggs + 2 tomatoes, bowl) -> the `guardian_soup` feast block. The Elder Guardian Eye is `oceanic_delight:elder_guardian_eye` (`i.elder_guardian_eye`). |
| 17 | `farming/composting #5` | "define dirt slot" | open -> ruled | Holds. Rot slot = `#forge:mulch/waste` (`compost_tags.js:11`: rotten flesh, spider eye, poisonous potato). Dirt slot = `minecraft:dirt` only (`kubejs/data/creativeengineering/kubejs/wood/sawdust.js:71-75`). |
| 18 | `farming/composting #6` | (row) | clear | Holds. `vintagedelight:organic_mash` is a vinegar by-product (`vintagedelight/jams.js:119-120`). The member has two organic compost recipes (rotten flesh mash, tree bark mash) plus the tree fertilizer. Mash is not compostable (missing from `kubejs/exported/compost/compost.json` and the CD vintage `compost.js`). |
| 19 | `baking/baked-goods #39` | "scone, croissant, buns, identify others" / "croissant, scone, adzuki bun, cider donut, honey and mulberry bread (glow bread not seen?), strange eclair, waffle in misc baked, gingerbread is dough->bread" | open -> ruled | There is no misc baked family. Croissant is in `bread/loaf`; the scones, adzuki bun, eclair and waffle are in `dessert/pastry`; cider donut is in `candy/piece`; honey, mulberry and glow bread and gingerbread are in `dessert/other`. There are no plain buns, bagels, pretzels or biscuits. Glow Bread exists (`hostiledelight:gbread`, shapeless, 4 ingredients). No plain donut exists. |
| 20 | `gear/tuning #12` | (row) | premise (corrected) | The config is `CEQConfig.java` in Creative Equipment (`Crafting` at 251, `FelsicPiece` at 285), not CEConfig. Only the 10 felsic pieces use `R.cfg` (`end_gear.js:73-82`). Left to convert: echo `:14-25` (10), felsic ingot `:52-57` (1), dragon `:121+` (16). `echoAssembly` (NCH `:2041`) and `dragonAssembly` (`:2070`) hardcode their amounts. |
| 21 | `mobs/tweaks #4` | (row) | clear | Holds. There are 11 literals in `kubejs/data/creativeworldgen/kubejs/tags/entitytags.js`. The 8 SAMPLES keys are in CD `kubejs_startup/creativedelight/sample_syringe.js:23-30`. Template: Common `_lib/profession_dict.js`. |
| 22 | `tooling/dictionaries #2` | (row) | clear | Holds. Common `_lib/block_dict.js:356` and `_lib/fluid_dict.js:310` skip tags, and NCH `:526` `_checkFluid` warns "not validated". The item check (`item_dict.js` ~14893, postInit) likely runs before tags are bound. |
| 23 | `licence/cd #4` | (row) | clear | Holds. `kubejs/data/miners_delight/loot_tables/blocks/potted_gossypium.json` is still there; the member ships no loot tables. |
| 24 | `largecontent/mod-additions #2` | "skip" | ruled: skip | Nothing exists and no consumer is named. |
| 25 | `farming/crops #36` | "nether and end crops can only grow in botany pots in the nether and end, with the correct soil. ensure the seed, produce, and colonies should grow in the pots w/ the correct soil" | open -> ruled | 41 plantables have no pot recipe: 29 `#forge:seeds` plus produce, colonies and End plants (list in section 3, G2). The pot recipes are 1,246 from the BotanyPots jar, 18 from fruitsdelight and 1 pack file. `kubejs/data/creativecompat/kubejs/other/botany_pots.js` only scales growth. Botany Pots 9 has no tree crops. No dimension gate was found (check at build). |
| 26 | `farming/crops #3` | (row) | clear | Holds. Quark SimpleHarvest (`config/quark-common.toml:780`, `:783`). The build is specified in `briefs/farming_crops.md:49-125` (`/dumpcropharvest`, `tools/crop_harvest_coverage.py`, `crop_harvest_rules.json`); none of it exists. |
| 27 | `fruit #4` | (row) | clear | `#forge:fruits` has 27 members and `#forge:berries` 10 (28 unique; `vinery:cherry` is in berries only). The families live in `tools/food_families.py` (~61-165), which records members, not the fruit each uses. |
| 28 | `dishes/ingredients #36` | (row) | open -> ruled | Bone broth is already a Moonlight soft fluid (`development/creativeDelight/src/main/resources/data/creativedelight/moonlight/soft_fluids/bone_broth.json`), not a Forge fluid. It is used in 14 recipes through `soupCraft` (`cd_food.js:185`) and in a can. |
| 29 | `potions/enchanting #1` | "LC banker Money Mending, Domestication pet books, define the quark tome trade" | open -> ruled | Vanilla and MCA trades: `kubejs/server_scripts/economy/trades.js` (MoreJS). LC cashier: `config/trades/lightmanscurrency/custom_cashier_trades.json` lines 4, 24, 57, 210. Quark: `config/quark-common.toml:512` "Librarians Exchange Ancient Tomes" = true. |

## 2. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| R1 | Rotten foods "like rotten flesh and rotten apple" | open | Rotten Ham, Cooked Rotten Ham, Rotten Pie, Green Pie, Fried Turtle Egg and Suspicious Bacon and Eggs are all `hazard`. Each gets a 20% chance of Poison II for 5 s. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| R2 | "suspicious egg and ham (rename green eggs and ham)" | clear | `hostiledelight:suspiciousbaconandaggs` is renamed Green Eggs and Ham (lang only; the id stays). | 2026-11-20 | AskUserQuestion 2026-11-20 |
| R3 | Not saltable | clear | Rotten Pie, Green Pie and Green Eggs and Ham leave the salt list (row #48). | 2026-11-04 | row #48 |
| R4 | Frying eggs | open | Bird eggs (chicken, duck, goose and the other bird eggs in `#forge:eggs`; parrot eggs excluded as too small, owner, 2026-11-22) fry into the Farmer's Delight fried egg. Parrot, turtle, crocodile and terrapin eggs don't, and emu keeps only its Boiled Emu Egg. | 2026-11-20 | AskUserQuestion 2026-11-20 (two rounds) |
| R5 | Magma and Slime Brownies are gummies | open | They are renamed Magma Gummy and Slime Gummy. A `candy/gummy` family is added with these two and the Glow, Salmonberry, Cantaloupe and Matcha gummies. The build writes down the recipe patterns the six use today and leaves them as they are; the gummy recipe pattern is applied later. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| R6 | Guardian fillet | open | Hostile's `guardianfillet` is the one raw guardian fillet. Ocean's `guardian_tail` merges onto it, with a save swap. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| R7 | "define dirt slot" | open | New tag `#creativedelight:compost_dirt`: dirt, coarse dirt, rooted dirt, podzol, moss block, mud, IW humus and silt. No grass block or mycelium (silk-touch only), no rich soil (an output tier). | 2026-11-20 | AskUserQuestion 2026-11-20 |
| R8 | Misc baked family | open | New family `baked/misc` with croissant, the raspberry and strawberry scones, adzuki bun, cider donut, honey bread, mulberry bread, Glow Bread, strange eclair and the H&H waffle. | 2026-11-20 | AskUserQuestion 2026-11-20 (two rounds) |
| R9 | "gingerbread is dough->bread" | clear | Gingerbread moves to the bread family and takes a dough -> bread oven route. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| R10 | Bone broth | open | It becomes a Forge fluid (no bucket, no block). Create fills and empties 250 mB per bowl, and the `soupCraft` recipes take it as a spout or basin fluid. The soft fluid stays for jars. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| R11 | Botany Pots | open | Every seed, planted produce and colony grows in a pot on its correct soil. Nether crops grow only in the Nether, and End crops only in the End. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| R12 | Hidden JEI items | open | A Creative Common mixin gives orphaned items (no creative tab) a tab, which fixes JEI, creative mode and search. A dump lists every no-tab item that has a recipe or loot. Maple bacon, cherry cookie, duck fillet, cooked duck fillet and duck noodles are shown. The maple cookie is left to its merge, and escargot stays hidden. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| R13 | Mod family tags | open | Skip; `largecontent/mod-additions #2` leaves this batch. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| R14 | Other enchanted books | open | Remove the Lightman's Currency banker Money Mending book and Domestication Innovation's pet enchant books from trades. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| R15 | Quark tome trade | open | Remove it (`Librarians Exchange Ancient Tomes` = false). Over-leveled books then come only from the anvil route. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| R16 | Public pack notes | open | Add both points to `README.md` "Gameplay highlights". | 2026-11-20 | AskUserQuestion 2026-11-20 |
| R17 | Wares trader "8x" | clear | The Wares delivery agreement weight goes from 20 to 8. This replaces the 2026-10-30 "3x less" ruling. | 2026-11-20 | chat 2026-11-20 |
| R18 | Owner rows D1-D19 | clear | All confirmed, with one change: bird eggs exclude parrot eggs (too small) (owner, 2026-11-22) | 2026-11-22 | AskUserQuestion 2026-11-22 |
| R19 | Gingerbread (R9 follow-up) | open | 9 gingerbread <-> 1 gingerbread block; the gingerdough block smokes or bakes (smoker and oven, as gingerbread does) into a gingerbread block; other recipes unchanged (owner, 2026-11-22) | 2026-11-22 | AskUserQuestion 2026-11-22 |
| R20 | Botany Pots (R11 follow-up) | open | Dimension soil gating: vanilla fungus and roots need Nether soils, Ender Crop needs End soil; propelplant pot drops fixed; Mixed Seeds not pottable; glow berries, hanging Nether roots and any vines not plantable in pots; no water plants (owner, 2026-11-22) | 2026-11-22 | AskUserQuestion 2026-11-22 |
| R21 | Propelplant pot drops (R20 follow-up) | clear | The cane back at 5% (was 25%), pearls 1 to 2, confirmed (owner, 2026-11-22) | 2026-11-22 | coordinator relay 2026-11-22 |
| D1 | `neighbours` config key | clear | Keep the key, field and enum (renaming breaks user tomls). Fix only the two "colour" comments. SQ-RFX-28's grep skips the key. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D2 | Component comments | clear | `Light?` -> `Light`, `Dense?` -> `Heavy`, per the 2026-10-29 ruling. No joke tooltip. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D3 | Tag as a recipe result | clear | Both dough results become `i.in_dough`. `_itemJson` warns and refuses a tag when `allowTag` is false, which covers every result slot. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D4 | Polishing route | clear | New NCH helper `createSandpaperPolishing` next to `createSplashing`: iron bars -> polished iron bars. Extends the helper rule rather than writing a raw event. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D5 | Lexica Botania | clear | Removed from every table, spawn bonus chest included, as the row says. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D6 | Already-spawned villagers | clear | Leave their existing book offers. Only new trade rolls lose books. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D7 | Gossypium loot | clear | Hand-written: one pool, two entries (flower pot, gossypium), each surviving explosions. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D8 | End gear config | clear | `EchoPiece` and `DragonPiece` classes in `CEQConfig`, plus the felsic ingot fluid amount, per the gear tuning brief. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D9 | Dict tag check | clear | A server-side check after tags load (lowest-priority `ServerEvents.tags` capture, like creativeloot `95_tag_capture.js`), which logs dict tags that are empty or unknown. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D10 | Entity dictionary | clear | Build it now (the owner listed the row): Common `_lib/entity_dict.js`, `global.entity`, used by `entitytags.js` and `sample_syringe.js`. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D11 | Green Eggs and Ham assembly | clear | `F.assemble(i.bowl, [i.cookedrottenham, i.friedturtleegg], result, 2)`, copying FD's bacon and eggs. The upstream shapeless recipe stays, as FD's does. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D12 | Guardian drops | clear | Hostile's guardian table keeps its shard and crystal counts. The fillet entry is replaced by 1 Guardian Shell, and Ocean's `guardian_drop` GLM is turned off so there is no double drop. Cutting the shell: 1-3 fillets plus a 30% Prismarine Shard. Elder guardians are untouched. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D13 | Guardian Soup | clear | FD pot: Guardian Shell, 2 fillets, tomato, onion, Elder Guardian Eye, bowl -> the existing feast block. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D14 | Mash compost recipes | clear | Remove the member's two organic compost recipes (rotten flesh mash, tree bark mash) and add organic mash to `#forge:mulch/waste` and the composter. The tree fertilizer stays. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D15 | Plain donut | open | Add now with the cider donut texture as a placeholder (owner, 2026-11-22; replaces the left-out default); art in [art_needed_2026-11-22.csv](texture_sources/art_needed_2026-11-22.csv) | 2026-11-22 | AskUserQuestion 2026-11-22 |
| D16 | Jar row wording | clear | "These don't mix: one kind per jar." | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D17 | Harvest audit | clear | A separate `/dumpcropharvest` that reuses the growth dump's block walk, per the brief. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D18 | Fruit audit | clear | A fruit x family matrix generator in `tools/food_families.py` (from recipe ingredients), written into `fruit.md`. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |
| D19 | Rustic cooked eggs | clear | `rusticdelight:raw_cooked_eggs` follows R4: bird eggs only. | 2026-11-20 | agent default; confirmed (owner, 2026-11-22) |

## 3. Lanes and order

One jar has one owner at a time (as in plan_exec_1117.md). Each phase is built, gets its scratch queue rows and its
changelog bullet, and is committed before the next phase starts.

### G0. Pack folder, /reload and config (no jar)
- `loot/trader #2`: `wandering_trades.js:19` weight 20 -> 8 (R17). Update `wandering_trader.md` 4.1.
- `potions/enchanting #1`: `trades.js` removes `EnchantBookForEmeralds` from the vanilla and MCA professions. Also remove
  the LC cashier book entries and the banker Money Mending book, Domestication pet books (R14), and the Quark tome
  exchange (R15). Back up each config to `configs_edited/root_config/`. Check that MCA uses the vanilla tables, as
  the brief says.
- `licence/cd #4`: rewrite `potted_gossypium.json` (D7).
- `pack/release #8`: README highlights (R16). Docs only.

### G1. Creative Common (rebuild, restart)
- `creativebrewing/publish #25` guard half: `_itemJson` refuses a tag result (D3).
- `recipes/metal #5` helper: `createSandpaperPolishing` (D4).
- `mobs/tweaks #4`: `_lib/entity_dict.js` (D10).
- `tooling/dictionaries #2`: post-tag validation (D9).
- `jei/jei-coverage #29`: orphan creative tab mixin plus a no-tab dump (R12), and a generator that lists the dump
  against `crafting_audit.json` and the loot dumps. Add the result to `jei_coverage.md`.
- `items/jars #6`: `tab_drops.py:278` wording (D16), then regenerate `jei_world` (it sits in Common's assets).
- `gear/tuning #12` NCH half: `echoAssembly` / `dragonAssembly` take their amounts as arguments.
- Bone broth fluid key in `fluid_dict.js` (R10).

### G2. Creative Delight (rebuild, restart)
- `creativebrewing/publish #25` swap half: `create/recipes.js:52,54` -> `i.in_dough`.
- `largecontent/mod-additions #48`: R1, R2, R3. Types go through `food_types.json` and `food_catalog.py`; the effect
  goes in CD `hostiledelight/hostile_items.js`.
- `largecontent/mod-additions #49`: D11, with the renamed result.
- New row, frying eggs: `#creativedelight:eggs/fryable` (bird eggs), `F.eggCook` from the tag, and
  `raw_cooked_eggs` trimmed (R4, D19). Fried Turtle Egg keeps its own recipe.
- `largecontent/mod-additions #46`: rename to Magma and Slime Gummy, plus the `candy/gummy` family in
  `food_families.py` (R5). Record the six gummies' recipe patterns in `food_families.md` without applying them.
- `dishes/ocean #11` and `#12`: dict key `i.guardian_shell` (Common), names, loot, cutting, the tail -> fillet merge
  and its swap (R6, D12), and the soup (D13).
- `farming/composting #5` and `#6`: the `compost_dirt` tag in `sawdust.js` (this is Engineering's file: do it in G3), mash
  in `mulch/waste`, the composter entry and the member recipe removals (R7, D14).
- `baking/baked-goods #39`: `baked/misc` and gingerbread to bread (R8, R9), then regenerate the families and catalog.
- `dishes/ingredients #36`: CD `fluids.js` fluid, fill and empty, and `soupCraft` fluid inputs (R10).
- `fruit #4`: the matrix generator (D18). A tool, but it reads the families G2 changes.

### G3. Creative Engineering (rebuild)
- `engineering/sawmill-and-components #6` (D2), `recipes/wood #1` (the 7 removals plus the twisted comment),
  `recipes/metal #5` (the recipe with the G1 helper), and the `farming/composting #5` dirt slot in `wood/sawdust.js`.

### G4. Creative Equipment (rebuild)
- `gear/tuning #12`: `CEQConfig` classes and `end_gear.js` (D8). Needs G1.

### G5. Creative Compat (rebuild)
- `conventions #39` (D1).
- `farming/crops #36`: a generator over the dicts writes `kubejs/data/creativecompat/recipes/botanypots/` (seed or
  produce, soil category, drops) for all 41 (R11). Nether and End crops only grow in their own dimension; if Botany
  Pots 9 can't gate that, a Compat mixin on the pot's growth tick checks the level's dimension. Look for an existing
  gate first.

### G6. Creative Loot and Creative Worldgen (rebuild)
- `loot/tables #18`: `loot_config.json:300` remove = true (D5). Check that `tools/gen_loot_config.py` keeps the flag.
- `mobs/tweaks #4` user side: `entitytags.js` reads `global.entity`.

### G7. Tooling
- `farming/crops #3`: `/dumpcropharvest`, `tools/crop_harvest_coverage.py`, and `crop_harvest_rules.json` (D17),
  then fix the gaps the report shows (Quark harvestable list, tags).

Order: G0, then G1 (the shared helpers), then G2 to G6 one at a time, and G7 last (its report reads the new crops).
Mark `largecontent/mod-additions #2` as skipped (R13).

## 4. Done per phase

- Scratch queue rows per row in `feature_writeup/testing/rows/<CODE>.qb`, prefixes from `test_prefix_index.md`.
- Player changelog bullets for: trader weight, enchanted books, Lexica, polished iron bars, rotten foods and Green Eggs
  and Ham, frying eggs, gummies, Guardian Shell and soup, compost, baked/misc (if visible in JEI groups), bone broth
  fluid, Botany Pots crops and dimension rule, the 5 newly shown JEI items, and End gear config. No bullet for
  conventions #39, the component comments, #25 (the full pack never hit it), the gossypium loot rewrite, entity dict, dict
  check, fruit audit, harvest audit tool, or pack notes.
- Writeups touched: hostile_delight.md 5.1, ocean_delight.md 9, composting.md, food_families.md, oven.md (gingerbread),
  wandering_trader.md, loot.md 10, jei_coverage.md, crops.md, fruit.md, mob_block_tweaks.md, pack_tooling.md,
  miners_delight.md, engineering.md, recipes_wood.md, mixed_jars.md, the enchanting brief.

## History

- 2026-11-20: written from the owner's list and four AskUserQuestion rounds. Nothing built.
- 2026-11-21: G0 to G6 built, installed and committed by phase agents (commits 5d44d014 to 322149de); live checks queued. G7 paused by the owner before it started. Open owner questions: gingerbread furnace and smoker recipes, water plants in Botany Pots, gating vanilla fungus, roots, vines and the Ender Crop, Mixed Seeds and propelplant pot drops, plain donut art (D15).
- 2026-11-22: G7 tooling built (`/dumpcropharvest` in Creative Devtools and `/dumpall`, `tools/crop_harvest_coverage.py`, `tools/crop_harvest_rules.json`); Devtools jar installed; live row SQ-CDV-09. The gap fixes need the owner's in-game dump.
