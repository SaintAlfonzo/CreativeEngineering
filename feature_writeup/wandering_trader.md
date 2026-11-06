# Wandering Trader and Wandering Winemaker: spawning, offers, stock

## 0. At a glance

| Part | Status |
|---|---|
| Spawn timers in server ticks, Wandering Trader and Winemaker independent (`traderIntervalTicks`, `winemakerIntervalTicks`, both 96000) | DEPLOYED with 1.0.4 (2026-10-04); built 2026-10-03, patcherMod 1.0.4 (md5 37ce1023). Scratch dedicated server: level.dat `WanderingTraderSpawnDelay 96000`; the Winemaker spawner's saved wait counted 96000 to 94800 after one step. A natural spawn with a player present is not tested yet (WT-16) |
| 25 common + 5 rare offers (`commonTrades`, `rareTrades`; vanilla 5 + 1) | BUILT, same jar; 24 summoned traders all had 31 offers (quill + 25 + 5), no rare item twice |
| Stock (removals, Wares agreements x20, golden food listings, new saplings/seeds/plants/blocks/mob buckets, Winemaker additions, no Nether/End items) | DEPLOYED with 1.0.4 (2026-10-04); built 2026-10-03: KubeJS [wandering_trades.js](../kubejs/server_scripts/economy/wandering_trades.js) through the new `global.trades` helper. Scratch server: common 205 entries, rare 39, Winemaker 35, same after `/reload` |
| Vinery's own Winemaker swap off (`wine_trader_chance` 0) | config edited + backed up in `configs_edited/` (instance and CreEngServer) |
| Catalog of every offer + gap lists | regenerated 2026-10-03 from the new pools: [wandering_trader_catalog.md](wandering_trader_catalog.md); every category now has 0 missing (sold, or excluded with a reason) |

**Knobs** (`config/creativeengineering-common.toml` `[wandering_trader]`, live-editable from Create's config screen):

| Key | Default | Vanilla | Meaning |
|---|---|---|---|
| `enabled` | true | | off = vanilla trader (24000 ticks, 5 + 1 offers) |
| `traderIntervalTicks` | 96000 | 24000 | server ticks between Wandering Trader spawn rolls (96000 = 80 minutes) |
| `commonTrades` | 25 | 5 | offers drawn from the common pool, each a different entry |
| `rareTrades` | 5 | 1 | offers drawn from the rare pool, each a different item |

The Winemaker's spawner moved into the Vinery port on 2026-10-27 ([pack_hygiene.md](pack_hygiene.md) finding 39):
`config/vinery-common.toml` `[trader]` `ownSpawner` (true; was CEConfig `winemakerEnabled`), `ownSpawnerInterval`
(96000; was `winemakerIntervalTicks`), `spawnChance` (0, 1.5.5's swap), `spawnWithMules` (true), `spawnDelay` (48000,
the stay). It no longer depends on `[wandering_trader] enabled`.

What the pools hold is not a config value: it is [wandering_trades.js](../kubejs/server_scripts/economy/wandering_trades.js)
(edit, then `/reload`).

**How to measure:** `/summon minecraft:wandering_trader ~ ~ ~ {NoAI:1b}` then
`/data get entity @e[type=minecraft:wandering_trader,limit=1,sort=nearest] Offers.Recipes` (reading the data rolls the
offers). Trader wait: `WanderingTraderSpawnDelay` in level.dat. Winemaker wait: `SpawnDelay` in
`<world>/data/vinery_winemaker_spawner.dat`. Pool sizes: the `[wandering trades] common N, rare N,
winemaker N` line in `logs/kubejs/server.log`. Catalog: `/dumptrades`, then `python3 tools/wandering_trader_report.py`.

## 1. Overview

**Spawning (vanilla 1.19.2, `WanderingTraderSpawner`, overworld only).** Every 1200 ticks the spawner counts down a
wait (vanilla 24000 ticks). At 0 it rolls: the chance is 25%, then 50%, then 75% on each roll that has not produced
a trader, back to 25% after a spawn. A roll that passes still needs 1 in 10 and a free spot within 48 blocks of a
random player (or of a meeting-point bell near them). The trader brings two llamas and leaves after 48000 ticks.
The wait is saved in level.dat. Custom spawners only tick while `doMobSpawning` is on, and the trader spawner also
needs `doTraderSpawning`.

**Ticks, not days.** The spawner counts server ticks (20 per second). It ignores the time of day, so Better Days
(day at 0.125x speed in this pack, so an in-game day is about 127000 ticks, not 24000), sleeping and `/time` do not
change it. The intervals are set and described in ticks and real minutes for that reason.

**Vinery's Wandering Winemaker.** Out of the box Vinery's `WanderingTraderManagerMixin` turns `wine_trader_chance`
percent (50 by default) of Wandering Trader spawns into a Winemaker, so the two shared one roll. The pack sets that
to 0 and spawns the Winemaker from its own spawner instead (3).

**Offers.** Forge builds `VillagerTrades.WANDERING_TRADER_TRADES` from `WandererTradesEvent` at server start:
pool 1 (common) and pool 2 (rare). MoreJS then fires `MoreJSEvents.wandererTrades` on the finished pools (again on
every `/reload`, starting from the untouched pools), which is where the pack edits them. The trader draws its offers
the first time they are needed. Quark adds a Pathfinder's Quill as a forced first offer, outside both pools, on
every Wandering Trader and also on the Winemaker (it is a Wandering Trader subclass). The Winemaker draws 8 from
its own pool (`WanderingWinemakerEntity.TRADES`).

## 2. Reference

### 2.1 Spawn interval

About 14.1 rolls per spawn on average (the 25/50/75% climb, then 1 in 10), counting only server time with
`doMobSpawning` on and a player online, and assuming a free spot is found:

| | Roll interval | Average between spawns |
|---|---|---|
| Vanilla Wandering Trader | 24000 ticks (20 min) | about 4.7 hours |
| Pack before 2026-10-03 (one roll shared, half Winemakers) | 96000 ticks | trader about 37.7 hours, Winemaker about 37.7 hours |
| **Pack now: Wandering Trader** | 96000 ticks (80 min) | about 18.8 hours |
| **Pack now: Winemaker (independent)** | 96000 ticks (80 min) | about 18.8 hours |

Each kept the 96000-tick roll, so each now spawns twice as often as it did while they shared one roll. For the
earlier per-type rate set both intervals to 192000.

### 2.2 Pools now

From `/dumptrades` on the scratch server, 2026-10-03:

| Pool | Entries | Distinct listings | Drawn per trader | Chance a weight-1 entry is on a trader |
|---|---|---|---|---|
| Common | 205 | 167 | 25 | 12.2% |
| Rare | 39 | 37 | 5 | about 12.8% |
| Winemaker | 35 | 35 | 8 | 22.9% |

Weight: Wares' two delivery agreement listings are in the common pool 20 times each (40 of 205 entries), so a
trader carries about 4.9 agreements (scratch server: 1 to 9, average 4.9). Supplementaries registers its globe and
flax seeds twice in the rare pool; the rare picker skips an item already on the trader, so it never shows twice.

### 2.3 What changed in the stock

All in [wandering_trades.js](../kubejs/server_scripts/economy/wandering_trades.js); item ids from `item_dict`.

| Change | Items | Pool, price |
|---|---|---|
| Removed | CC: Tweaked advanced turtle, Advanced Peripherals peripheral casing, Lightman's Currency ATM / cash register / terminal / display case / armor display | both pools |
| Removed (Nether) | glowstone; Architect's Palette sunstone (basalt) and moonstone (sunstone warped in a portal) | common |
| Moved, weight 20 each | Wares sealed delivery agreement: regular (6 emeralds) and the former rare one (12) | rare to common |
| Saplings | azalea, flowering azalea, the 6 Quark blossom saplings, Upgrade Aquatic river sapling, Myrtrees rubberwood sapling | common, 5 emeralds, 8 uses |
| Saplings | Quark ancient sapling (ancient cities only) | rare, 8, 3 uses |
| Tall flowers | lilac, peony, rose bush, sunflower, Upgrade Aquatic flowering rush | common, 1, 8 uses |
| Seeds, crop starters | corn kernels, tea seeds, blueberry, cranberry, hamimelon seeds, lemon seeds, sweet and glow berry pips, carrot, potato, cocoa beans, bamboo, hemp seeds, mulberry | common, 1, 12 uses |
| Plants | grass, tall grass, large fern, dead bush, seagrass, moss carpet, glow lichen, hanging roots, big dripleaf, glow shroom, ivy, moss clump, frosty grass, frosty fern | common, 1, 12 uses |
| Plants | spore blossom, four-leaf clover | common, 3, 4 uses |
| Natural blocks | coarse dirt x8; clay, mud, ice, snow block, calcite, dripstone block x4; grass block x2; mycelium x2 (3 emeralds) | common |
| Natural blocks | sponge (no renewable source) | rare, 8, 3 uses |
| Random golden food | one random member of `#creativeengineering:trades/golden_food` per trader: golden apple, golden carrot, Golden Food's plain golden foods, no golden chorus fruit | rare, 6, 3 uses |
| Random canned golden | one random member of `#creativeengineering:trades/canned_golden` (canned golden apples, canned golden carrot) | rare, 12, 2 uses |
| Mob buckets | cod, salmon, tadpole, squid | rare, 3, 4 uses |
| Mob buckets | glow squid, jellyfish, perch, crab, axolotl, terrapin, small / medium / large catfish, mudskipper, lobster, flying fish | rare, 5, 4 uses |
| Winemaker | 12 Fruits Delight tree saplings (5 emeralds; the apple sapling dropped 2026-10-22, Vinery's apple tree is the pack's) and the pineapple sapling (3); white jungle grape seeds (1; Vinery's own slot sells the grape) | Winemaker pool, 8 uses |

**Never sold, by rule (sequence breaking):** nether wart, chorus flower, Nether's Delight propelplant cane, Farmer's
Respite coffee beans (its wild coffee only grew in the Nether, and `wild_crops.json` has switched that spawn off since
1.0.4, so the beans have no source at all: crops.md V18), Architect's Palette twisted sapling, crimson and
warped fungus and roots, nether sprouts, twisting and weeping vines, nylium, soul soil, glowstone, sunstone,
moonstone, golden chorus fruit, cosmic cod (End) and stradpole (Nether) buckets. The report script's `EXCLUDE`
table keeps this list so the gap report never proposes them.

**Other choices:** Berry Good makes sweet and glow berries unplantable (`berry_good-common.toml`), so the pips are
sold, not the berries. Comb jelly, devil's hole pupfish, frilled shark, mimic octopus and platypus buckets were not
on the list. Prices follow the nearest existing offer (vanilla saplings 5, seeds and small plants 1, fish buckets 5).

### 2.4 Full catalog

[wandering_trader_catalog.md](wandering_trader_catalog.md): every entry of the three pools with price, uses and
weight, then the per-category gap check. Regenerate after any stock change: `/dumptrades` then
`python3 tools/wandering_trader_report.py`.

## 3. What the pack changes (mechanics)

- **Trader wait:** `WanderingTraderSpawnerMixin` swaps the `sipush 24000` in `tick` (`m_7995_`) and in the
  constructor (a new world's first wait) for `traderIntervalTicks`. Bytecode-checked: those are the only two.
- **Winemaker spawner** (in the Vinery port since 2026-10-27, `forge/WinemakerSpawner`; before that patcherMod
  `trader/WinemakerSpawner`): copies vanilla's spawner on `LevelTickEvent` (overworld, `doMobSpawning`, `doTraderSpawning`): 1200-tick step,
  25/50/75%, 1 in 10, spot near a random player or meeting point, two leashed `vinery:mule`s like Vinery's own code,
  48000-tick stay (`spawnWithMules`, `spawnDelay`). Its wait and chance are saved per world in
  `data/vinery_winemaker_spawner.dat` (was `creativeengineering_winemaker_spawner.dat`; not carried over, so an
  existing world waits one full interval first). `[trader] spawnChance` above 0 still adds 1.5.5's swap on top; the
  patcherMod start-up warning for that is gone.
- **Offer counts:** `WanderingTraderTradesMixin` swaps the `iconst_5` in `updateTrades` (`m_7604_`) for
  `commonTrades`, and at TAIL adds `rareTrades - 1` rare offers after vanilla's one, in random order, skipping any
  item already on the trader.
- **Stock:** `global.trades` ([trade_helper.js](../kubejs/startup_scripts/func/trade_helper.js)) inside
  `MoreJSEvents.wandererTrades`:
  - `sell(e, pool, items, {emeralds, count, maxUses, weight})`
  - `sellFromTag(e, pool, tag, opts)`: one random tag member per roll (Java `TagItemListing`)
  - `remove(e, pools, matchers)` / `move(e, pools, matchers, toPool, weight)`: find listings by what they sell,
    `'ns:id'` or `'@ns'`. The Java side samples each listing once with no entity and catches every Throwable, since
    some mod listings throw without an entity.
  - `winemakerReset()` / `winemakerSell(items, opts)`: Vinery's static pool, original array + extras, reset first
    on every run so `/reload` never stacks copies.

## 4. Limits and decisions

### 4.1 Decisions

- **Rolls stay vanilla, only the wait is set.** The rate is then exactly vanilla x (24000 / interval), and spawns
  stay as streaky as vanilla. A fixed "every N ticks" spawn was not built.
- **Independent spawners, each at the old 96000.** Read as keeping the 0.25x setting for each type, which doubles
  how often each appears compared with the shared roll (2.1). 192000 gives the old per-type rate.
- **Vinery's swap off through its config, not a mixin.** Its injection is merged into vanilla's `spawn` at runtime
  and can't be targeted cleanly; a config value is reliable, and the startup warning covers a reset config.
- **Removals by sampled result, not MoreJS filters.** MoreJS's `removeTrades` only matches vanilla and Forge
  listing classes; CC/AP's `VillagerTrade` and Lightman's `SimpleTrade` are neither.
- **Wares: both listings x20.** "Move it to the common pool" read as the rare agreement moving, "20x" applied to
  each. That is about 4.9 agreements per trader; `weight` in `t.move` changes it.
- **Golden food tag of its own.** `#creativeengineering:loot/golden_food` includes golden chorus fruit (End), so
  trades use `#creativeengineering:trades/golden_food` (same set minus it), in
  [tradetags.js](../kubejs/server_scripts/tags/tradetags.js).
- **Ancient sapling and sponge in the rare pool** at 8 emeralds: both have no other renewable source.
- **Winemaker still draws 8.** Its pool grew from 20 to 35; a count knob was not asked for.

### 4.2 Limits

- `rareTrades` minimum 1: vanilla's own rare pick is inline code.
- 31 offers is a long list in the trade GUI (it scrolls, 7 rows at a time).
- A random-tag offer is decided when the trader rolls its offers; the trade screen shows the picked item, not the
  tag.
- `/dumptrades` doesn't see Quark's quill (it isn't in any pool).
- The Winemaker spawner was proven to count down on the scratch server, but a real spawn needs a player (WT-16).

## 5. File map

| File | Role |
|---|---|
| [CEConfig.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CEConfig.java) | `[wandering_trader]` |
| [trader/WanderingTraderTweaks.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/trader/WanderingTraderTweaks.java) | trader wait, common count, extra rare offers |
| [backportedVinery forge/WinemakerSpawner.java](../development/backportedVinery/src/main/java/net/satisfy/vinery/forge/WinemakerSpawner.java) | Winemaker spawner + saved data (moved from patcherMod 2026-10-27) |
| [trader/TradeListings.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/trader/TradeListings.java) | listing builders, find-by-result |
| [trader/TagItemListing.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/trader/TagItemListing.java) | random tag member listing |
| [trader/WinemakerTrades.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/trader/WinemakerTrades.java) | Winemaker pool extras (reflection, no Vinery compile dependency) |
| [mixin/WanderingTraderSpawnerMixin.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/WanderingTraderSpawnerMixin.java) | 24000 constant |
| [mixin/WanderingTraderTradesMixin.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/WanderingTraderTradesMixin.java) | 5 constant + TAIL rare injection |
| [startup_scripts/func/trade_helper.js](../kubejs/startup_scripts/func/trade_helper.js) | `global.trades` |
| [server_scripts/economy/wandering_trades.js](../kubejs/server_scripts/economy/wandering_trades.js) | the stock |
| [server_scripts/tags/tradetags.js](../kubejs/server_scripts/tags/tradetags.js) | `trades/golden_food`, `trades/canned_golden` |
| [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js) | new keys: `rubberwood_sapling`, 4 Upgrade Aquatic buckets, `sealed_delivery_agreement`, `t_trades_*` |
| [villager_trades_dump.js](../kubejs/server_scripts/_lib/villager_trades_dump.js) | `/dumptrades`, now with the Winemaker pool |
| [tools/wandering_trader_report.py](../tools/wandering_trader_report.py) | catalog + gap check, `EXCLUDE` reasons |
| `config/vinery/config.json5` | `wine_trader_chance` 0 (backup in `configs_edited/root_config/vinery/`) |

## 6. Offline checks

- Build + reobf names (`javap`), done 2026-10-03.
- Scratch dedicated server (memory scratch-server-verify), done 2026-10-03:
  - pools logged `common 205, rare 39, winemaker 35`, the same after `/reload`
  - all 9 removals hit (the counts match exactly)
  - 24 traders: 31 offers each, no removed or Nether item, no rare item twice, 1 to 9 agreements (average 4.9)
  - 8 Winemakers: 9 offers each (quill + 8), all 14 new items seen
  - golden food listing: 20 different foods in 48 sample rolls, no chorus fruit
  - level.dat `WanderingTraderSpawnDelay 96000`; Winemaker `SpawnDelay` 94800 after one step
  - no exceptions from creativeengineering in latest.log
- `python3 tools/wandering_trader_report.py`: every category 0 missing.

## 7. In-game test suite

- [ ] **WT-01 config:** Create's config screen for Creative Engineering → a Wandering Trader section with
  `enabled`, `traderIntervalTicks` 96000, `commonTrades` 25, `rareTrades` 5, `winemakerEnabled`,
  `winemakerIntervalTicks` 96000.
- [ ] **WT-02 offer count:** summon a trader, open it → 31 offers: the Pathfinder's Quill first, 25 common, 5 rare.
- [ ] **WT-03 no repeats:** in WT-02's list → no rare item twice; common items repeat only for Wares agreements.
- [ ] **WT-04 rare count live:** set `rareTrades` to 2, summon a new trader → 28 offers. Set it back to 5.
- [ ] **WT-05 off switch:** set `enabled` false, summon a trader → 7 offers (quill + 5 + 1). Turn it back on.
- [ ] ~~**WT-06 Winemaker unchanged**~~: retired 2026-10-03, the Winemaker's pool changed; see WT-09.
- [ ] **WT-07 trader wait (new world):** create a world, `/save-all`, read level.dat →
  `WanderingTraderSpawnDelay` is 96000 or a little less.
- [ ] **WT-08 trader wait (existing world):** in the dev world, after the current wait runs out, it resets to
  96000, not 24000.
- [ ] **WT-09 Winemaker offers:** use a Wandering Winemaker spawn egg a few times → 9 offers each (quill + 8);
  across them Fruits Delight saplings and white jungle grape seeds turn up.
- [ ] **WT-10 Winemaker wait saved:** play 2 minutes, `/save-all` → `<world>/data/vinery_winemaker_spawner.dat` (before 2026-10-27: `creativeengineering_winemaker_spawner.dat`)
  exists with `SpawnDelay` below 96000.
- [ ] **WT-11 no Vinery swap:** `latest.log` has no `[trades] ... wine_trader_chance` warning at server start.
- [ ] **WT-12 removed stock:** across several summoned traders → no CC: Tweaked, Advanced Peripherals or Lightman's
  Currency item, no glowstone, sunstone or moonstone.
- [ ] **WT-13 delivery agreements:** across several traders → usually 3 to 7 Sealed Delivery Agreements each, some at
  6 emeralds and some at 12.
- [ ] **WT-14 golden listings:** across several traders → rare offers include a golden food (6 emeralds) and a
  canned golden food (12), with different golden foods on different traders.
- [ ] **WT-15 reload:** `/reload`, then check `logs/kubejs/server.log` → the `[wandering trades]` line shows the
  same counts as at startup.
- [ ] **WT-16 natural spawns:** in a test world set both intervals to 1200, stand near a village with
  `doMobSpawning` on and wait 20 to 30 minutes → both a Wandering Trader (with llamas) and a Winemaker (with two
  mules) arrive on their own, separately. Set the intervals back to 96000.
- [ ] **WT-17 no Fruits Delight apple sapling:** `/reload`, then summon several Winemakers → Fruits Delight
  saplings on offer, never `fruitsdelight:apple_sapling`. Scratch server PASSED 2026-10-22 (SQ-MG-05).

## 8. History

- 2026-10-03: written. Spawn rate multiplier (0.25) and offer counts (20 common, 1 rare) added to patcherMod 1.0.4;
  catalog and gap analysis from the 2026-10-02 dumps.
- 2026-10-03 (later): rework on request. Spawning set in server ticks (`traderIntervalTicks` replaces
  `spawnRateMultiplier`, same 96000); the Winemaker gets its own spawner (`winemakerIntervalTicks` 96000) and
  Vinery's `wine_trader_chance` goes to 0. Offers 25 common + 5 rare (5x vanilla). Stock rebuilt in KubeJS
  through a new `global.trades` helper: CC/AP/Lightman's and Nether items removed, Wares agreements x20 into common,
  random golden food / canned golden rare listings, the remaining saplings, tall flowers, seeds, plants, natural
  blocks and 16 mob buckets added, Fruits Delight saplings + white jungle grape seeds to the Winemaker. `/dumptrades`
  now dumps the Winemaker pool; the report shows weights and counts both traders. Jar md5 37ce1023, checked on a
  scratch dedicated server. WT-06 retired, WT-09 to WT-16 added.
- 2026-10-22: Fruits Delight's apple sapling removed from the Winemaker (Vinery's apple tree is the pack's); WT-17.
- 2026-10-27: the Winemaker spawner moved from patcherMod into the Vinery port (pack_hygiene.md finding 39). Same
  rolls, interval, mules and stay; keys now `vinery-common.toml` `[trader] ownSpawner` / `ownSpawnerInterval`; CEConfig
  `winemakerEnabled` / `winemakerIntervalTicks` and the regex check of the Vinery toml removed. New saved-data name.

## Owner ideas (scratchpad 2026-10-26)

- Rare Rustic pepper seed offer: `loot/trader #4`. Built with the Rustic port and live since 1.1.03: Pale or Dark seeds, 5 emeralds, 3 uses ([rustic_delight.md](rustic_delight.md) 5.4).
