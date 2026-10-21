# Brief: worldgen / bee-nests

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# worldgen`, Sub `bee-nests` (row #2; #1 is at Testing).
Feature writeup: [bee_nests.md](../bee_nests.md) (tests `BN-`), report [bee_nests_by_biome.md](../bee_nests_by_biome.md).

## Scope and order

| Row | Status | What is left | Order |
|---|---|---|---|
| bee-nests #2 | Planned | "beehive spawning biomes and frequency: investigate" | **already done** by #1's investigation (2026-10-10). Left: a report refresh, the user's review of the starter rules (#1's open decision), and an optional structure scan |

Recommendation: fold #2 into #1 and delete #2 at the next sweep. If the user meant something #1 doesn't cover (placed
beehive blocks in structures, or bees per nest), keep it with that narrower text (see Decisions).

---

## bee-nests #2: Beehive spawning biomes and frequency

Status Planned · Work data · TODO `worldgen / bee-nests #2` (from mob_spawns.md 9 "Investigate", where it was
written before bee_nests.md existed)

### Goal

Know where wild bees come from and how often, and tune it where it is wrong.

### Now (what #1 already answered)

- Bees have **no natural spawn** in any biome (`/dumpspawns`: `minecraft:bee` in 0 biomes). Every wild bee comes from a
  nest placed by the `minecraft:beehive` tree decorator, 2 to 3 bees each (bee_nests.md 1, 2.1).
  - Buzzier Bees adds no nest or bee spawns: its biome modifiers are moobloom, buttercup, clovers and a cow removal
    (verified in `buzzier_bees-1.19.2-5.0.1.jar`).
- **Where and how often:**
  - before the pack's tuning: bee_nests.md 2.2 (17 of 126 land biomes, about 1 nest per 785 chunks);
  - with the starter rules: bee_nests.md 9 (47 biomes, about 1 per 170).
  - The rules are `kubejs/generation_scripts/worldgen/bee_nests.json`, applied by
    `kubejs/server_scripts/worldgen/bee_nests.js` and patcherMod `worldgen/BeeNests.java`, DEPLOYED with 1.0.6.
- **Stale:** [bee_nests_by_biome.md](../bee_nests_by_biome.md) was last generated 2026-10-14. Its "area %" column and
  the area-weighted totals predate the 2026-10-19 rarity floor. For example lavender forest shows 0.00 %, and
  `biome_config.json` now has 0.15 %. The per-biome "1 in N" values don't depend on area and still hold.
- **Not measured in game:** the starter rules ran once on a small scratch area (4,301 chunks). BN-04 (the ~2,000-chunk
  measurement) is open.

### Build

1. Refresh the report: `python3 tools/gen_bee_nests.py` (or `/cegen run bee_nests`). It needs the instance's `mods/`,
   so run it on the dev machine. Check that section 0's totals move only by the new areas.
2. Run BN-04 on a scratch server:
   - `/dumpbeenests reset`, then `chunky radius 400` in an unexplored area, then `/dumpbeenests`;
   - rerun the generator. Section 2 is then measured.
3. Put the measured table in front of the user for the starter-rule review (bee_nests.md 9 "your review still open";
   it is `bee-nests #1`'s decision). Edit `bee_nests.json` for any changes, rerun step 1.
4. Optional, only if the user meant placed beehive blocks: scan structure templates for `minecraft:beehive` /
   `minecraft:bee_nest` / `buzzier_bees:*_beehive`. Add a temporary pattern to `NETHER_TIERS` in
   `tools/nether_blocks_scan.py:36` or copy its walker; it needs `mods/`. Templates store a nest's bees as block
   entity NBT, so a structure nest can bring bees anywhere.
5. Update mob_spawns.md 9 (done in this pass: the "Investigate" line now points at bee_nests.md) and delete this row.

### Decisions

- **Is #2 the same as #1?** Recommend yes: merge and delete #2. Keep it only if the user names something else:
  - (a) placed beehives in structures (step 4);
  - (b) bees per nest: 2-3 is vanilla `BeehiveDecorator`. Changing it is a mixin on the decorator, next to
    `BeehiveDecoratorBeeNestsMixin`;
  - (c) bees spawning without nests (a `forge:add_spawns` for `minecraft:bee` in flower biomes; not recommended: bees
    without a hive have nowhere to return to, and animals don't despawn).

### Gotchas

- Tuning only reaches newly generated chunks (bee_nests.md 4).
- Distant Horizons generates LOD terrain on the server. Measure without it, as the 2026-10-10 baseline did.

### Tests

- BN-04 is the measurement. No new test unless option (a) or (b) is built.

---

## Built, needs play

| Row | Status | Note |
|---|---|---|
| bee-nests #1 Bee nest tuning with starter rules | Testing | BN-01 to BN-10 open; starter rules await the user's review. bee_nests.md said the rules "ship in 1.0.5 (staged)": fixed to DEPLOYED with 1.0.6 |

## Writeup corrections made

- [bee_nests.md](../bee_nests.md) 0: starter rules "ship as they are in 1.0.5 (staged)" → DEPLOYED with pack 1.0.6; the
  jar note "ships in pack 1.0.5" → shipped with 1.0.6.
- [mob_spawns.md](../mob_spawns.md) 9 "Investigate": the beehive line now says it was investigated and tuned in
  bee_nests.md (2026-10-10) and names the TODO rows.
