# Brief: worldgen / biomes/netherblocks

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# worldgen`, Sub `biomes/netherblocks`. Feature writeup:
[nether_worldgen.md](../nether_worldgen.md) (decision sheet, prefix `NW-`), raw scan
[nether_worldgen_scan.md](../nether_worldgen_scan.md). Tools: `tools/gen_nether_worldgen.py`,
`tools/nether_worldgen_scan.py`, `tools/gen_wild_flowers.py`; rules
`kubejs/generation_scripts/worldgen/nether_worldgen_rules.json`.

## Scope and order

| Row | Status | What is left | Build order |
|---|---|---|---|
| #3 flower changes need a rerun | Planned | make `gen_nether_worldgen.py` rerun (or flag) the flower snapshot | **first**: a small tool change, and #2 is the next run that would hit it |
| #2 frostfire caves (B12) | Planned | decide, then two `features` rules and one generator run | **second** |
| #4 no glowstone in the Overworld | Testing | built 2026-11-02: processor pass in the scan (none puts out glowstone), `tools/block_census.py`; static result clean, no data change; NW-21 (census on a fresh world) queued | done |
| #1 | Testing | NW-01 to NW-05, NW-08, NW-09 | listed at the end |

Related: `structures/netherblockswap #3` (soul lanterns in structures: same lantern choice as B12, decide
together); `engineering/dreams-and-desires #5` (the "D&D glowstone geodes" this brief finds already gone, see #4);
`worldgen/biomes #1` (the 1.0.9 rarity floor that made the frostfire caves reachable).

---

## biomes/netherblocks #3: Flower changes need a rerun

Status Planned · Work tool · TODO `worldgen / biomes/netherblocks #3`

### Goal

A change to a Terralith flower feature through `nether_worldgen_rules.json` can't ship with a stale flower snapshot.

### Now

- `kubejs/server_scripts/worldgen/wild_flowers.js` rebuilds every listed flower feature from the snapshot
  `kubejs/generation_scripts/worldgen/flower_features.json` (written by `tools/gen_wild_flowers.py`; keys `placed`,
  `configured`, `configured_of`, `biomes`; `configured` holds each configured feature's effective JSON, jars then
  `kubejs/data`). So an override written by `gen_nether_worldgen.py` (e.g.
  `kubejs/data/terralith/worldgen/configured_feature/flower/blue/flowers.json`) is ignored in game until
  `gen_wild_flowers.py` reruns (nether_worldgen.md 4, "Flower snapshot order"). The snapshot holds 9
  `terralith:flower/blue` entries today.
- `tools/generators.json` runs `nether_worldgen` before `wild_flowers` (both in groups `core`, `worldgen`), so `/cegen
  run worldgen` or `all` is safe. `/cegen run nether_worldgen` alone, or `python3 tools/gen_nether_worldgen.py` by
  hand, is not. The manifest has no dependency field (`_readme`: "put a generator after anything it reads").
- `gen_nether_worldgen.py` `main()` (:195-268) writes `out` and the manifest; nothing looks at flowers.

### Build

1. In `gen_nether_worldgen.py`, after writing (not on `--dry-run`): load `flower_features.json`, and for every written
   or deleted `kubejs/data/<ns>/worldgen/configured_feature/<path>.json` whose id `<ns>:<path>` is a key of
   `snapshot['configured']` (or of `configured_of` values), compare the new JSON with the snapshot's. If any differs,
   run the flower generator in-process or as a child:
   ```python
   if stale:
       print(f'flower features changed ({", ".join(stale)}): rerunning gen_wild_flowers.py')
       subprocess.run([sys.executable, str(INSTANCE / 'tools/gen_wild_flowers.py')], cwd=INSTANCE, check=True)
   ```
   Add `--no-flowers` to skip it (prints a WARNING line instead).
2. Add `kubejs/generation_scripts/worldgen/flower_features.json` to the `nether_worldgen` entry's `outputs` in
   `tools/generators.json`, so `/cegen` reports the snapshot change, and add a `highlight` regex for the
   "rerunning gen_wild_flowers" line.
3. Optional guard on the other side: a `--check` mode in `gen_wild_flowers.py` that recomputes the snapshot in memory
   and exits non-zero on a difference, called from `tools/test_wild_flowers.sh`.
4. Update nether_worldgen.md 4 ("Flower snapshot order") and 5 to say the generator now does it.

### Decisions

- **Auto-run or warn?** Recommendation: auto-run (it takes seconds and the only failure mode of a warning is
  forgetting it, which is what happened in round 2).

### Gotchas

- `gen_wild_flowers.py` reads the mod jars too: it needs the full instance, same as `gen_nether_worldgen.py`.
- Rerunning the snapshot can pick up unrelated jar changes after a mod update; read its diff.

### Tests

- Offline: change a flower rule in a scratch copy, run `gen_nether_worldgen.py`, check that `flower_features.json`
  changed; run again with nothing changed, check the snapshot is not rewritten.

---

## biomes/netherblocks #2: Frostfire caves (B12)

Status Planned · Work data · TODO `worldgen / biomes/netherblocks #2`

### Goal

No soul fire or soul lanterns generated in Terralith's frostfire caves (or an explicit keep).

### Now

- **The biome now generates.** nether_worldgen.md B12 listed it at 0 % (unreachable in the scan's samples). Since the
  1.0.9 cave floor, `biome_config.json` gives `terralith:cave/frostfire_caves` multiplier 2.48 and 1.196 % of the cave
  layer (corrected in the writeup, see the end). WG-13 checks it in game.
- **Features** (verified in `Terralith_1.19.3_v2.3.12.jar`; biome json `data/terralith/worldgen/biome/cave/frostfire_caves.json`,
  its feature list has `cave/frostfire/frostfire_patch` and `frostfire_ceiling`):
  - `terralith:cave/frostfire/frostfire_patch`: `vegetation_patch` on the floor, ground deepslate / blue ice (2:2),
    `vegetation_chance` 0.05, vegetation = `simple_block` **soul_fire**. Placed 17 per chunk, y bottom to 30.
  - `terralith:cave/frostfire/frostfire_ceiling`: `vegetation_patch` on the ceiling, ground deepslate / blue ice (4:2),
    `vegetation_chance` 0.4; 7 % of the vegetation is a `block_column` down: chain (4 to 50) then one
    `soul_lantern[hanging=true]`. Placed 12 per chunk.
  - The `cave/frostfire_meme/*` copies are not used by any biome (checked every Terralith biome json).
- **The soul fire probably never places.** Vanilla `SimpleBlockFeature` places only if `state.canSurvive(...)`, and
  soul fire survives only on `#minecraft:soul_fire_base_blocks` (soul sand, soul soil). The patch's ground is deepslate
  or blue ice. From knowledge of the 1.19.2 code, not run: a census (NW-20) settles it. The scan counts it because it
  reads features statically.
- `gen_nether_worldgen.py` `rewrite_feature` (:141-166) swaps every placed state `Name` and keeps the properties
  (`{**v, 'Name': to}`), skipping predicate keys (`PREDICATE_KEYS` :138), so `hanging=true` survives; `"remove"` only
  works on weighted-list entries, not on a `simple_block`.

### Build

1. Two `features` rules in `nether_worldgen_rules.json` (after the existing ones):
   ```json
   {"note": "Frostfire caves: ceiling soul lanterns to lanterns (B12)", "feature": "terralith:cave/frostfire/frostfire_ceiling",
    "from": "minecraft:soul_lantern", "to": "minecraft:lantern"},
   {"note": "Frostfire caves: floor soul fire never placed (B12; it can't stand on deepslate or blue ice anyway)",
    "feature": "terralith:cave/frostfire/frostfire_patch", "from": "minecraft:soul_fire", "to": "minecraft:air"}
   ```
   The second rule keeps the blue ice / deepslate floor patches (the biome's look) and makes the "no soul fire" explicit
   whatever the census says. A `simple_block` of air survives anywhere, so it places air on air: harmless.
2. `python3 tools/gen_nether_worldgen.py --dry-run` (every rule must match: 1 state each), then without `--dry-run`.
   Two new files under `kubejs/data/terralith/worldgen/configured_feature/cave/frostfire/`, added to
   `nether_worldgen_manifest.json`. Not flower features, so no snapshot rerun (#3).
3. `python3 tools/nether_worldgen_scan.py`: B12's features drop out of section 2.
4. nether_worldgen.md: B12 Decision cell, section 3 "Built" row, section 0 "Still open", tests.
5. Ship: data, server restart, new chunks only.

### Ids and data

`minecraft:soul_lantern` -> `minecraft:lantern` (same properties `hanging`, `waterlogged`); `minecraft:soul_fire` ->
`minecraft:air`. Feature ids verified in the Terralith jar.

### Decisions

- **Soul fire: drop or fire?** Recommendation: drop (the air rule). Plain fire *would* survive on deepslate and blue
  ice, so "fire" would add burning patches that are not there today.
- **Soul lantern: lantern or keep?** Recommendation: lantern, the same choice as `structures/netherblockswap #3`.

### Tests

- **NW-20 Frostfire census (before and after):** scratch world, `/locate biome terralith:cave/frostfire_caves`, pregen
  about 25 chunks around it; count soul fire, soul lanterns and lanterns with the region census from #4 (or by eye in
  spectator) -> before: soul lanterns on chains, soul fire 0 (expected); after: lanterns on chains, no soul fire.

---

## biomes/netherblocks #4: No glowstone in the Overworld

Status Testing (Build 1 and 2 done 2026-11-02, step 3 is NW-21; nothing found, so step 4 not needed; step 5 in [nether_worldgen.md](../nether_worldgen.md) 0, 1.1, 6, 7) · Work data / patcherMod · TODO `worldgen / biomes/netherblocks #4`

### Goal

Nothing generates glowstone in the Overworld, checked by a scan that covers every source.

### Now

- **Known sources, all handled:**
  - Create: Dreams & Desires' deep layered rock (`create_dd:striated_eroded_overworld`, F7): granite only since
    1.0.9 (`kubejs/data/create_dd/worldgen/configured_feature/striated_eroded_overworld.json`). This is the "D&D
    glowstone geodes" of the row and of `engineering/dreams-and-desires #5`: D&D has **no** geode feature; its only
    glowstone use in worldgen is the layer pattern `DesiresLayerPatterns` (`Blocks.f_50141_`, weight 1, size 1-2;
    decompiled `Create-DnDesire-1.19.2-1.2c.Beta Mid-Dev.jar`, which ships no worldgen json at all).
  - Structure templates: swap rule 21 (glowstone -> `quark:yellow_corundum`), rule 10 (sky village), rule 14
    (Mysticrift mangrove tower -> gold ore). The scan after swaps shows no glowstone left in any live Overworld
    structure (nether_worldgen_scan.md 3).
  - Biome features and code-added features: none places glowstone (nether_worldgen_scan.md 2 and the jar search of
    2026-10-19; Geode+'s Nether geodes are tagged to Nether biomes).
- **Blind spots left:**
  1. **Structure processors** (nether_worldgen.md 4): a `minecraft:rule` processor's `output_state`, or a mod
     processor class, can place glowstone the template doesn't have. The scan does not read `worldgen/processor_list`.
  2. **No real-world check**: every "clean" above is static. The 2026-10-19 scratch counts (nether_worldgen.md 6) were
     for the D&D layer only.

### Build

1. **Processor pass in `tools/nether_worldgen_scan.py`**: walk every `data/*/worldgen/processor_list/*.json` (jars then
   `kubejs/data`, like the other sources); collect `output_state.Name` of `minecraft:rule` processors and any
   `Name` under other processor types; report Nether blocks per processor list and the structures whose pools use it
   (pool element `processors`). Add mod processor classes that hardcode blocks to a hand table, like `JAVA_FEATURES`
   (YUNG's `betterdungeons:nether_block_processor` is one; it only removes Nether blocks). Output a new section in
   nether_worldgen_scan.md.
2. **Region census tool** `tools/block_census.py <world dir> <block id>... [--dim overworld]`: reuse the region/NBT
   reader of `tools/seabed_heights.py` (no dependencies), count the listed blocks per chunk section from each
   section's `block_states` palette (skip sections whose palette lacks them, so it is fast), print totals, the
   chunks with hits and their y. Useful for #2's census too.
3. **Run it**: a scratch world (seed of the earlier runs, 8675309), Chunky radius 1000 around spawn and one in a deep
   cave region, `/forceload` not needed; then `python3 tools/block_census.py <save> minecraft:glowstone
   minecraft:soul_fire minecraft:soul_lantern`. Expected: 0 glowstone in the Overworld. Structures are where a stray
   would hide; with structure density raised (`[worldgen.debug] structureDensity`, soft rule only, structure_tuning.md
   7.4.8) the same area holds more of them.
4. **If something turns up**: a feature -> a `features` / `literal` rule in `nether_worldgen_rules.json`; a template ->
   a swap rule; a processor -> an override of its processor list json in `kubejs/data`. A patcherMod guard (replace
   glowstone in `WorldGenRegion#setBlock` for the Overworld) is the last resort only: it hides the source and costs a
   check on every worldgen block write.
5. Close the row in nether_worldgen.md (a short "Glowstone" line in section 1.1 Findings and the census result in 6).

### Decisions

- None needed up front (the row says none). If the census finds nothing, recommendation: no patcherMod guard.

### Gotchas

- The census only proves the area scanned; structures that are rare (fortified metas, the gothic church) may not be
  in it. The template scan covers those statically; the census covers what the scan can't see.
- Glowstone placed by players or by loot (glowstone dust) is not worldgen; don't count item frames or chests.

### Tests

- **NW-21 Glowstone census:** `block_census.py` on the scratch world -> 0 `minecraft:glowstone` in the Overworld
  region files; the gothic church found in it (NW-11) shows yellow corundum.

---

## Built, needs play

| Row | Status | Tests | Notes |
|---|---|---|---|
| #1 Biome rounds 1 and 2 | Testing | NW-01 to NW-05, NW-08, NW-09 | DEPLOYED with 1.0.9; files match the manifest (7 files in `nether_worldgen_manifest.json`). Nothing stale found |

## Writeup corrections made

- [nether_worldgen.md](../nether_worldgen.md) B12 row: share "0 % (cave)" -> "0 % (cave) at the scan; 1.196 % since
  the 1.0.9 cave floor".
- [nether_worldgen.md](../nether_worldgen.md) section 4, "0 % biomes" bullet: the five biomes now have shares in
  `biome_config.json` (crater 0.15 %, basalt cliffs 0.295 %, caldera 0.24 %, mantle 0.725 %, frostfire 1.196 % of
  caves), so B3, B7, B11 and B12 should generate in new worlds.
- [nether_worldgen.md](../nether_worldgen.md) section 3: "Nothing new yet." removed (also listed in the
  netherblockswap brief).
