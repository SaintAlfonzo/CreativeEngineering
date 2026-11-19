# Corpse decay and ancient corpses (2026-11-18)

<!-- generated:status_index begin open_work sha1=4a75c749e97f -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 7 open TODO rows (Planned 7), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `corpses #1` | Planned | S | Corpse decay on world time: skeleton after 7 days, empty corpse skeleton after 30 s and despawn after 5 min, shown armor follows contents (Creative Compat) |
| `corpses #2` | Planned | S | Ancient corpses: skeletons on 3x3 flat cave floors (below y48, about 80% below y16) and ocean floors (cold and deep more common), about 1 per 32 chunks, loot rolled on first open (Creative Loot, only with Corpse loaded) |
| `corpses #3` | Planned | P | Work out ancient corpse loot tables (cave and ocean dead-player kits, tier check against loot_tiers.md) |
| `corpses #4` | Planned | S | Cosmetic armor and curios go into the corpse; corpse screen in sections by inventory location; cosmetic armor and visible curios shown on the corpse model |
| `corpses #5` | Planned | S | Ancient corpse armor rolls: equip chains, 56/35/15/5 chances, material weights, oxidized or waxed copper, dyed leather colors (all configurable) |
| `corpses #6` | Planned | S | Nether corpses wither (skeleton then wither skeleton corpse), End corpses keep the player model (Creative Compat) |
| `corpses #7` | Planned | S | Visceral Heap Ancient Remains: empty breakable skeletons dropping bones, bone chunks, cracked bones and sometimes a skull |
<!-- generated:status_index end open_work -->

Status: **planned, rulings in** (section 4); loot contents still to work out (section 5). Status lives in the TODO rows (section 6).

Source: owner notes in `todo/scratchpad.md` (2026-11-18) and chat 2026-11-18:

> - corpses decay to skeleton model after configurable time, decays faster if corpse is empty
>     - keeps armor on skeleton?
> - randomly spawning corpses underground in world, skeletons

Chat additions (owner, 2026-11-18): the corpse decay goes in Creative Compat; the random corpses go in Creative Loot
and only when the Corpse mod is loaded; they spawn in caves or on the ocean floor with loot suited to a "dead player";
they are named "Ancient Corpse", not "Corpse of <player>". Add a note to work out the loot (section 5).

## 1. What the Corpse mod does today (premise check)

Jar: `corpse-forge-1.19.2-1.0.17.jar` (henkelmax, both sides; mod_sides.md). Read from the bytecode
(`de/maxhenkel/corpse/entities/CorpseEntity`) and `serverconfig/corpse-server.toml`:

- **Skeleton stage exists already.** `CorpseEntity.tick()` increments a private `age` each server tick and sets the
  synced `SKELETON` flag when `age >= skeleton_time` (config `[corpse] skeleton_time`, default and pack value
  `72000` ticks). The renderer then draws `DummySkeleton` instead of the player model.
- **The clock counts loaded ticks only.** `age` advances only while the chunk ticks, so a corpse in an unloaded area
  never decays. At 72000 ticks it is 1 hour of loaded time, not world time.
- **Empty corpses vanish.** When the corpse is empty, `emptyAge` is stamped and the entity is discarded
  `[corpse.despawn] time = 600` ticks (30 s) later. With today's values an emptied corpse never reaches the
  skeleton stage, so "decays faster if empty" needs the empty despawn changed too (item 3).
- `force_time = -1`: full corpses never despawn.
- **The skeleton already wears armor.** `show_equipment = true`; `DummySkeleton` gets the corpse's equipment list and
  sets each slot. That list is a **snapshot taken at death** (`setEquipment` is only called on creation and NBT load),
  so armor taken out of the corpse still shows on it (item 4).
- Name: `getDisplayName()` returns `entity.corpse.corpse_of` ("Corpse of %s") when the stored name is set, else
  "Corpse". WTHIT shows the same name (wthit_lines.md).
- Access: `only_owner = false`, so anyone can open any corpse, ancient ones included.

## 2. Items (intake)

| # | Item (owner's words) | Class | Notes |
|---|---|---|---|
| 1 | "corpses decay to skeleton model after configurable time" | premise | Done upstream (section 1). Open: which clock and what default time (Q1, Q2). |
| 2 | "decays faster if corpse is empty" | open | Empty corpses despawn after 30 s today. Open: what happens to an empty corpse (Q3). |
| 3 | "keeps armor on skeleton?" | premise | Done upstream. Open: should the shown armor follow what is still in the corpse (Q4). |
| 4 | corpse decay lives in Creative Compat | clear | Mixin on `CorpseEntity`, gated by `CompatMixinPlugin` on mod id `corpse`. |
| 5 | "randomly spawning corpses underground in world, skeletons" | open | Spawn as skeletons. Rate, depth and placement (Q6). |
| 6 | random corpses live in Creative Loot, only if Corpse is loaded | clear | Creative Loot registers nothing today (creative_loot.md section 0); this adds its first registration (a worldgen feature and biome modifier). |
| 7 | caves or the bottom of the ocean | clear | Two placements: cave floor (air above, solid below, no sky), ocean floor in ocean biomes. |
| 8 | "appropriate loot for a 'dead player'" | open | Contents to be worked out (section 5). Open now: how the loot is rolled and shared (Q5), whether they wear armor (Q7). |
| 9 | "Ancient Corpse" instead of "Corpse of <player>" | clear | Marker tag on the entity; Compat's mixin returns "Ancient Corpse" from `getDisplayName()`; lang key in Creative Loot. |
| 10 | what happens to a looted ancient corpse | open | Not in the notes; follows Q3 unless the owner wants a different rule (Q8). |

## 3. Design (pending rulings)

### 3.1 Corpse decay (Creative Compat)

- Mixin `CorpseDecayMixin` into `CorpseEntity.tick()` (server side), plus save/load of the extra fields. Only applied when
  `corpse` is loaded (`CompatMixinPlugin`). The Corpse jar goes into `development/creativeCommon/libs/` (compileOnly,
  git-ignored, SRG-named, as the other targets).
- Skeleton time on world time (stored death game time, compared each tick and on load), 7 days; empty rule 30 s to skeleton, 5 min to despawn. New keys in `creativecompat-common.toml`, section `[corpse]`:
  skeleton time, empty multiplier or empty skeleton time, empty despawn time. Upstream `skeleton_time` and
  `despawn.time` are left as they are; creative_compat.md lists which upstream keys Compat overrides.
- Shown armor follows contents (ruling 4): the mixin re-syncs `EQUIPMENT` from the corpse's armor and hand slots
  when its inventory changes.

### 3.2 Ancient corpses (Creative Loot)

- A worldgen feature `creativeloot:ancient_corpse` with two placed features (`cave`, `ocean_floor`), added by a Forge
  biome modifier wrapped in a `forge:mod_loaded corpse` condition. Feature registration runs only when
  `ModList.isLoaded("corpse")`; the class touching `CorpseEntity` is loaded only then.
- The feature spawns a `CorpseEntity` with the skeleton flag set, no owner UUID, the ancient marker tag and an unrolled loot table id (rolled on first open, ruling 5) and its armor
  (ruling 7) already in the armor slots, so it shows on the model. Only new chunks get them (worldgen); existing explored areas get none.
- Loot tables `creativeloot:corpse/cave` and `creativeloot:corpse/ocean` (data in Creative Loot, so the LootJS and
  `loot_config.json` tuning layer can reach them). Rate about 1 per 32 chunks (ruling 6), spread by height (ruling 12) and ocean type (ruling 13), flat check ruling 11.
- Name per item 9. Death history (`/corpse`) never lists ancient corpses (no Death record).

### 3.3 Note: cosmetic armor and curios in the corpse (owner, chat 2026-11-18)

> ensure all cosmetic armor and curios items transfer to the corpse appropriately, and automatically show the
> contents of each of these inventories

- **Curios:** Corpse Curios Compat 2.2.2 (`CorpseContainerMixin`) already collects curios into the corpse and puts
  them back into curio slots on Transfer Items (`blacklisted_items = []`). To check: every curio slot type in the pack
  (Botania, Charm of Undying, Elytra Slot, the trinket case), cosmetic curio slots, and that nothing lands in
  "Additional Items" only because a slot type is missing.
- **Cosmetic armor:** conflicts with FX-28 (owner, 2026-10-22, fixes_compat.md): `CosArmorKeepThroughDeath = true`
  keeps cosmetic armor on the player, so it never reaches the corpse. Cosmetic Armor x Corpse Compat 1.0.0 puts it back
  in the cosmetic slots when the keep option is off. Ruling 9: into the corpse; set `CosArmorKeepThroughDeath = false`
  (copy to `configs_edited/`), replacing FX-28.
- **Show the contents (ruling 10):** the corpse screen is laid out by where each item was on the player: hotbar, main
  inventory, armor, offhand, curios (by slot type), cosmetic armor, all visible at once with labels; no Additional
  Items button for these. The corpse and skeleton model also wear the cosmetic armor over the real armor and render
  visible curios (back, belt, elytra slot), as the player looked. Shown items follow contents (ruling 4 applies).
  Needs per-item origin slots stored at death (the Corpse mod keeps a main list and an additional list only), and a
  client screen replacement for `CorpseInventoryScreen`; the model part reuses Curios' and Cosmetic Armor's render
  layers on the dummy entities if they accept them. Look-check with a screenshot before polishing (intake section 5).
- Checks with the decay build: an item in each inventory survives skeleton stage, decay and the 5 min empty despawn
  (a corpse holding only curios or cosmetic items is not "empty"); Transfer Items puts each item back in its own slot.

### 3.4 Placement, armor rolls and dimensions (owner, chat 2026-11-18)

Owner notes, split:

| # | Item (owner's words) | Class |
|---|---|---|
| 11 | "only spawn in flat areas with no overhangs" | open (Q12: footprint and headroom) |
| 12 | "only spawn below y48 (very rare at that height), slowly become more common as they approach y0, and more common below y0 approaching y-64 where most corpses are below y32" | open (Q9: y32 or y-32; Q10: curve) |
| 13 | ocean floor corpses vs the y48 cap | open (Q11; most ocean floor is above y48) |
| 14 | "copper armor on corpses should always be fully oxidized (or waxed? can they be waxed?)" | open (Q13). Premise: yes, Everything Is Copper armor stores `state` (unaffected, exposed, weathered, oxidized) and a `waxed` flag in item NBT, with its own armor textures per stage |
| 15 | "subsequent roll mechanic for armor; equip order chains ... (boots-leg-chest-helmet, helmet-chest-boots-leg, chest-boots-legs-helmet), more common first roll but less common on each subsequent roll" | open (Q14: chances). Mechanic clear: pick one chain at random, roll slot 1; on success roll slot 2 at a lower chance, and so on; the first failure stops |
| 16 | "the first 2 material types are random but any after are 1 of those 2" | clear: pieces 1 and 2 roll materials by weight; pieces 3 and 4 pick one of those two (50/50) |
| 17 | "dyed leather armor is 50% the same color and 50% 2 very different colors" | open (Q16: color source; Q17: how the two colors split) |
| 18 | "leather dyed-leather and copper armor common while iron and chain are rarer" | open (Q15: wood, ruled in ruling 7, is not in this list) |
| 19 | "all of these features and values are configurable" | clear: every chance, weight, chain list, y curve point, footprint and timer in `loot_config.json` (Loot) or `creativecompat-common.toml` (Compat) |
| 20 | "similar tool system, and a chance to hold a tool or inventory item in hands" | note only (section 5b), not designed now |
| 21 | Nether: none spawn; player corpses "turn to skeletons on empty in 15 seconds", "wither to a wither skeleton corpse after 30 seconds, and decays after 5 minutes" | open (Q18: full Nether corpses; Q19: what the 5 min counts from) |
| 22 | wither skeleton look "(shortened? palette swapped skeleton texture?)" | open (Q20). Texture rule: vanilla `wither_skeleton.png` is human art and may be copied; a palette swap needs the owner's source and palette |
| 23 | "the flesh heap nether biome should uncommonly spawn empty skeletons that dont wither or despawn, but can be broken" | open (Q21: rate, name, drops). Biome: Biomes O' Plenty `biomesoplenty:visceral_heap`. Premise: corpses cannot be broken by players today (`hurt()` only reacts to lava, off in the pack), so breaking is new |
| 24 | "player corpses in the end never turn to skeletons and remain the player model until they despawn" | open (Q22: when a full End corpse despawns; `force_time = -1` means never) |

Design holes the questions close are listed with each item; rulings go in section 4.

## 4. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| 1 | skeleton time clock (Q1) | open | World time since death; also decays while the chunk is unloaded, caught up when it loads | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 2 | default skeleton time (Q2) | open | 7 in-game days (168000 ticks), configurable | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 3 | empty corpse rule (Q3) | open | Skeleton 30 s after it becomes empty, despawn 5 min after it became empty (both configurable). "5 min from emptied" confirmed in Q8 | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 4 | armor shown after looting (Q4) | open | Follow contents: the model shows only armor and held items still in the corpse | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 5 | how ancient loot is rolled and shared (Q5) | open | Rolled on first open, shared (first opener takes it) | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 6 | spawn rate and placement (Q6) | open | About 1 per 32 chunks, configurable in `loot_config.json`; cave floors and ocean floors | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 7 | armor on ancient corpses (Q7) | open | Worn (damaged) wood (Immersive Armors `wooden_*`), leather (also dyed), chainmail, copper (Everything Is Copper `copper_*`), iron; rarely enchanted; lootable, stops showing when taken out | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 8 | looted ancient corpse (Q8) | open | Same as player corpses: despawns 5 min after it became empty | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 9 | cosmetic armor into the corpse (conflict with FX-28, fixes_compat.md 2026-10-22) | conflict | Into the corpse: keep option off; replaces FX-28 | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 10 | "automatically show the contents of each of these inventories" | open | Corpse screen in sections by inventory location; cosmetic armor and visible curios also shown on the corpse model | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 11 | "only spawn in flat areas with no overhangs" (Q12) | open | 3x3 floor of full solid blocks at one height, at least 3 blocks of air (water on the ocean floor) above all 9 | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 12 | height spread (Q9, Q10) | open | Cave corpses only below y48 (very rare there); target about 80% below y16 (flexible); tune the curve for the largest possible rate gap between y-64 and y32. Build tunes the curve points and reports the resulting shares | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 13 | ocean floor corpses (Q11) | open | On the ocean floor in clear space (same 3x3 check, water above); no y cap. Rate ratios warm 1 : cold 3, shallow 1 : deep 6 | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 14 | copper oxidation and wax (Q13, Q13b) | open | Unwaxed copper is always fully oxidized. 10% of copper pieces are waxed; a waxed piece takes any state but fully oxidized (unaffected, exposed, weathered), with a flat roll penalty on unaffected. Waxed states follow the dye rule: 50% all the same state, 50% two different states | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 15 | armor roll chances (Q14) | open | 56 / 35 / 15 / 5 % along the chosen chain; breakdown below | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 16 | material weights (Q15) | open | Wood, leather, dyed leather, copper 25 each; chain 8, iron 8 (ruling 7 kept: rarely enchanted, worn) | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 17 | dyed leather colors (Q16, Q17) | open | Any RGB. 50% all pieces one color, 50% two colors far apart in RGB distance (not opposite hues) | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 18 | full Nether corpse (Q18) | open | Skeleton 1 in-game day after death, wither skeleton 1 day after that (world time), never despawns while it holds items | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 19 | empty Nether corpse (Q19) | open | Skeleton 15 s after emptied, wither skeleton 30 s after that, gone 5 min after emptied | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 20 | wither skeleton corpse look (Q20) | open | Vanilla `wither_skeleton.png` (copied human art; sources.csv row) on the normal skeleton model and size | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 21 | Visceral Heap skeletons (Q21) | open | About 1 per 8 chunks in `biomesoplenty:visceral_heap`, same 3x3 check, any height; plain skeleton, empty, never wither or despawn; named "Ancient Remains" | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 22 | breaking (Q21b) | open | Only Visceral Heap skeletons can be broken; drops bones, bone chunk and cracked bones (Philip's Ruins), and a skeleton skull 25% of the time | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 23 | End corpses (Q22) | open | Never turn skeleton, keep the player model; never despawn while full, gone 5 min after emptied | 2026-11-18 | AskUserQuestion 2026-11-18 |

Armor rolls (ruling 15), share of ancient corpses by pieces worn:

| Pieces | Chance |
|---|---|
| 0 | 44.0% |
| 1 | 36.4% (0.56 x 0.65) |
| 2 | 16.7% (0.56 x 0.35 x 0.85) |
| 3 | 2.79% (0.56 x 0.35 x 0.15 x 0.95) |
| 4 (full set) | 0.0147%, about 1 in 6800 (0.56 x 0.35 x 0.15 x 0.05) |

A full set of one material needs pieces 1 and 2 to roll the same material (pieces 3 and 4 then copy it): at the
weights in ruling 16 that is 19.5% of full sets, about 1 in 35000 corpses for any one-material set and about 1 in 1.4
million for full iron or full chain.

Agent defaults, unconfirmed (clear items only): the ancient corpse feature is skipped in biomes with no cave floor
(none expected); enchant chance about 5 percent per piece; RGB "far apart" = distance of at least 200 (of 441); unaffected-copper penalty halves its weight; lukewarm and warm oceans count as warm, cold and frozen as cold; Visceral Heap drops 1 to 3 bones, 0 to 1 bone chunk, 0 to 2 cracked bones; `philipsruins:cracked_bone` gets a dict key (only `bone_chunk` has one), values in `loot_config.json`;
ocean corpses only in `#minecraft:is_ocean` biomes (not rivers); cave corpses below the sea level only.

## 5. Note: ancient corpse loot (to work out)

Determine the loot for ancient corpses, a "dead player" kit: what an explorer would carry when they died there.
To work out with the owner before the loot tables are filled:
- Cave kit candidates: torches, a worn pick or shovel, food (rations, a can), rope or ladders, a little ore or ingots,
  a map, coins (Lightman's Currency), a damaged weapon.
- Ocean kit candidates: a worn sword (no trident), a compass or map, fish or rations, a bucket, maybe a
  water-breathing item; no tool that makes the ocean trivial.
- Tier by depth? (deeper caves, better kit), and check against loot_tiers.md and the existing chest tiers so ancient
  corpses do not out-loot dungeons.
- Durability: worn items (random damage), no enchantments or light ones.
- Use `loot_census.py` afterwards to check the new tables against the rest.

### 5b. Note: corpse tools and held items (to design later)

Ancient corpses get a tool system like the armor rolls (worn tools by material, configurable), and a chance to hold
a tool or an inventory item in the main hand or offhand (shown on the model, lootable, follows contents).

## 6. TODO rows

- `corpses #1`: corpse decay (Creative Compat, items 1 to 4).
- `corpses #2`: ancient corpses (Creative Loot, items 5 to 10).
- `corpses #4`: cosmetic armor and curios into the corpse, sectioned corpse screen, cosmetic and curio rendering (section 3.3).
- `corpses #5`: ancient corpse armor rolls (chains, chances, materials, copper state and wax, dyed leather), rulings 14 to 17.
- `corpses #6`: Nether and End corpse rules, wither skeleton corpse, rulings 18 to 20 and 23 (Creative Compat).
- `corpses #7`: Visceral Heap "Ancient Remains", breakable with bone drops, rulings 21 and 22.
- `corpses #3`: ancient corpse loot tables (section 5); `corpses #2` ships with placeholder tables until this is done.

## 7. Tests

Prefixes: `CMP-` (creative_compat.md) for decay, `LOOT-` (creative_loot.md) for ancient corpses. Rows written with the
build.

## History

- 2026-11-18: plan written from the scratchpad block and chat; premise checks in section 1; rulings Q1 to Q8 recorded; rows `corpses #1` to `#3`.
- 2026-11-18: section 3.4 added (placement, armor rolls, copper, dye, Nether, End, Visceral Heap); rulings 11 to 23; rows `corpses #5` to `#7`; `corpses #2` narrowed to placement.
- 2026-11-18: section 3.3 added (cosmetic armor and curios); rulings 9 and 10; row `corpses #4`.
