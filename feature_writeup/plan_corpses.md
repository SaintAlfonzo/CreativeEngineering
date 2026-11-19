# Corpse decay and ancient corpses (2026-11-18)

<!-- generated:status_index begin open_work sha1=a9019b2983e8 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 3 open TODO rows (Planned 3), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `corpses #1` | Planned | S | Corpse decay on world time: skeleton after 7 days, empty corpse skeleton after 30 s and despawn after 5 min, shown armor follows contents (Creative Compat) |
| `corpses #2` | Planned | S | Ancient corpses: skeletons on cave and ocean floors, about 1 per 32 chunks, worn lootable armor, loot rolled on first open (Creative Loot, only with Corpse loaded) |
| `corpses #3` | Planned | P | Work out ancient corpse loot tables (cave and ocean dead-player kits, tier check against loot_tiers.md) |
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
  `loot_config.json` tuning layer can reach them). Rate 1 per 32 chunks (ruling 6).
- Name per item 9. Death history (`/corpse`) never lists ancient corpses (no Death record).

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

Agent defaults, unconfirmed (clear items only): the ancient corpse feature is skipped in biomes with no cave floor
(none expected); per-slot armor chance about 40 percent, enchant chance about 5 percent, values in `loot_config.json`;
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

## 6. TODO rows

- `corpses #1`: corpse decay (Creative Compat, items 1 to 4).
- `corpses #2`: ancient corpses (Creative Loot, items 5 to 10).
- `corpses #3`: ancient corpse loot tables (section 5); `corpses #2` ships with placeholder tables until this is done.

## 7. Tests

Prefixes: `CMP-` (creative_compat.md) for decay, `LOOT-` (creative_loot.md) for ancient corpses. Rows written with the
build.

## History

- 2026-11-18: plan written from the scratchpad block and chat; premise checks in section 1; rulings Q1 to Q8 recorded; rows `corpses #1` to `#3`.
