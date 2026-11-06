# Brief: worldgen / flowers

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# worldgen`, Sub `flowers` (rows #2 and #3; #1 is at
Testing). Feature writeups: [flowers.md](../flowers.md) (tests `FL-`), [mooblooms.md](../mooblooms.md) 2.8 / 4.9 / 7
(tests `MB-`), [mob_block_tweaks.md](../mob_block_tweaks.md) F2 (tests `MT-`).

## Scope and order

| Row | Status | What is left | Order |
|---|---|---|---|
| flowers #3 | Planned | Bushier Flowers worldgen off by a list of 5 files; nothing notices a 6th | **first**: a small offline check, one session |
| flowers #2 | Planned | moobloom spawn rules are a hand translation of the flower inventory | second: a generator (`tools/gen_moobloom_spawns.py`) that writes the rule file and the 2.8 table, with the hand-set flower-biome splits kept in a small JSON |

The two don't depend on each other. #2 is the bigger one. It also needs a refresh now, whether or not it is automated:
the 2.8 table and the rules predate the 2026-10-19 biome rarity floor and the moonlight flower fix (see #2 Now).

---

## flowers #3: Bushier Flowers worldgen off file by file

Status Planned · Work data · TODO `worldgen / flowers #3` (mob_block_tweaks.md F2)

### Goal

If a Bushier Flowers update (or a different jar) ships a biome modifier that the pack doesn't override, the offline
checks fail instead of bushes quietly coming back.

### Now (verified against `bushierflowers-0.0.2-1.19.2.jar`)

- The jar ships exactly 5 biome modifiers, `data/bushierflowers/forge/biome_modifier/add_grown_flower_{default,
  flower_forest,meadow,plain,swamp}.json`. Each is `forge:add_features` of a **Java-registered** placed feature, e.g.
  `bushierflowers:grown_flower_plain_placed` (`net/Pandarix/bushierflowers/world/feature/ModPlacedFeatures`). There is
  no worldgen JSON in the jar.
- The pack overrides all 5 with `{"type": "forge:none"}` in `kubejs/data/bushierflowers/forge/biome_modifier/` (same
  file names). Growth is off by `development/creativeCommon/.../mixin/BushierFlowersDisableMixin.java`.
- The pack pins `bushierflowers-0.0.2-1.19.2.jar` (`pack/manifest.json`, `pack/MODLIST.md`). So an "update" only
  happens when someone changes the manifest.
- The same pattern (a mod modifier replaced by `forge:none`) is used at runtime by `wild_flowers.js` (Botania, Buzzier
  Bees clovers) and `wild_crops.js` (44 crop modifiers). Those name their modifiers in JSON and check at world load.
  `tools/gen_wild_flowers.py` has a hard-coded `PACK_OFF_MODIFIERS` set for one built-in-pack case.
- The tools read the instance's `mods/*.jar` (e.g. `tools/gen_crop_worldgen_table.py:39, 242`). `mods/` is not in the
  repo, so this check runs on the dev machine.

### Build

1. Add a check to `tools/check_worldgen_json.py`. It already runs as step 9 of `tools/test_worldgen_tools.sh` and is
   pure Python:
   - for each jar in `mods/` whose name starts with `bushierflowers`, list `data/*/forge/biome_modifier/*.json` (zipfile);
   - for each one, require `kubejs/data/<ns>/forge/biome_modifier/<name>.json` to exist with `type == "forge:none"`;
   - print `FAIL <jar>: biome modifier <ns>:<name> is not switched off (kubejs/data/... missing)` and count it as
     bad. Skip quietly (with one info line) when `mods/` is absent, so the repo-only CI doesn't fail.
2. Make it a small table rather than one mod: `DISABLED_MODS = {'bushierflowers': 'every biome modifier'}` at the
   top. Then a later "turn all of mod X's worldgen off" reuses it.
3. Update mob_block_tweaks.md F2: "checked by `check_worldgen_json.py`". Change the row to Complete once the check
   is in and passing.

Sketch:

```python
for jar in glob.glob('mods/*.jar'):
    mod = next((m for m in DISABLED_MODS if os.path.basename(jar).startswith(m)), None)
    if not mod: continue
    with zipfile.ZipFile(jar) as z:
        for n in z.namelist():
            m = re.match(r'data/([^/]+)/forge/biome_modifier/(.+\.json)$', n)
            if not m: continue
            f = f'kubejs/data/{m[1]}/forge/biome_modifier/{m[2]}'
            ok = os.path.exists(f) and json.load(open(f)).get('type') == 'forge:none'
            if not ok: print(f'FAIL {jar}: {m[1]}:{m[2][:-5]} not switched off ({f})'); bad += 1
```

### Decisions

- **Check, or remove the features at runtime?** The only fully automatic alternative is runtime: a patcherMod hook
  that drops every `forge:add_features` modifier from namespace `bushierflowers` when Forge runs biome modifiers
  (`ServerLifecycleHooks` / `ModifiableBiomeInfo` in Forge 43). That is more code and a mixin into Forge for a pinned
  0.0.2 mod. Recommend the offline check.

### Gotchas

- `forge:none` overrides need the exact file path. A renamed file in an update is a new modifier plus a dead override;
  the check catches the new one but not the dead one (harmless).
- Buzzier Bees and Botania modifiers are handled at runtime by `wild_flowers.js` and need no entry here.

### Tests

- Offline: `bash tools/test_worldgen_tools.sh` step 9 passes. Temporarily delete one override → step 9 fails naming it.
- MT-09 (no worldgen) stays the in-game test.

---

## flowers #2: Moobloom rules follow flowers by hand

Status Planned · Work tool · TODO `worldgen / flowers #2` (flowers.md 4; mooblooms.md 2.8, 4.9, 7)

### Goal

After a `wild_flowers.json` change, a worldgen mod update or biome tuning, one generator run regenerates the 2.8 flower
table and the moobloom spawn rules. Only the user's hand-set choices stay hand-set.

### Now

- **The rules.** `kubejs/data/more_mooblooms/moobloom_spawns/creativeengineering.json`: 36 rules, exact biome ids, last
  changed 2026-10-14 (`15c43a0`). Shape: `{"biomes": [...], "replace": [{"variant": "more_mooblooms:<v>", "percent":
  n}, ...]}`. The witherbloom rule lives in the mod's switch pack. The lush caves azaleabloom rule is also in this file.
- **How they were made** (mooblooms.md 4.9, 2026-10-08):
  1. Per biome, take the 16 pool flowers' patches per chunk from `tools/gen_wild_flowers.py`'s
     `inventory(world, cfg, all_traits, snap, after=True)` (`gen_wild_flowers.py:505-559`).
  2. Tier the biome by the sum: flower biome ≥ 1.0 (plus `flower_forest`, `sunflower_plains` by name), flowery 0.5 to
     1.0 (25 %), some flowers 0.25 to 0.5 (2 %), under 0.25 no rule.
  3. Variant mix = each variant's share of the biome's pool flowers. A shared flower is split by `feed_weight`
     (dandelion 9:1 moobloom:sunbloom, pink tulip 1:1 onionbloom:peonybloom). Drop shares under 5 %, rescale to the
     tier percent, round. Biomes with the same mix share one rule.
  4. Flower biomes: the user's bloom % and split table (mooblooms.md 4.9 "Flower biomes", 15 rows, retuned
     2026-10-11) → percent = bloom % x split / split total. Even splits = the 9 flower variants.
  5. Cows: a biome needs cows; meadow, blooming plateau and skylands spring get them from
     `kubejs/data/creativeengineering/forge/biome_modifier/moobloom_cows.json`.
  The script that did steps 1-3 was a scratch script and was not kept (mooblooms.md 6 / 7).
- **Flower → variant map**: each variant's `flowers` pool in
  `development/moreMooblooms/src/main/resources/data/more_mooblooms/moobloom_variants/<id>.json` (11 variants,
  generated by `tools/gen_moobloom_variants.py` from the mooblooms.md 4.4 table), with `feed_weight` per entry.
  `tools/check_moobloom_data.py` already loads them (mod data + switch packs + kubejs/data on top).
- **Cows per biome**: `tools/gen_spawn_table.py --mob minecraft:cow` (reads `kubejs/exported/spawns.json` from
  `/dumpspawns`, applies `mob_spawns.json`).
- **What is stale now:**
  - The 2.8 "Area %" column is from before the 2026-10-19 rarity floor (e.g. lavender forest 0.03 → 0.15 %,
    skylands spring <0.03 → 0.01 %).
  - The moonlight blue flower patch was converted on 2026-10-19 (nether_worldgen.md B8: roots and sprouts out, so a
    bigger flower share). `flowers_by_biome.md` was regenerated then; mooblooms 2.8 was not.
  - The "about 10.4 mooblooms per 100 cows" figure is area-weighted, so it moved too.

### Build

1. **Inputs file** `kubejs/generation_scripts/worldgen/moobloom_spawn_tiers.json` (new; the user edits it):
   ```json
   { "tiers": [ {"min": 1.0, "percent": "flower"}, {"min": 0.5, "percent": 25}, {"min": 0.25, "percent": 2} ],
     "flower_biomes_by_name": ["minecraft:flower_forest", "minecraft:sunflower_plains"],
     "min_share": 5,
     "flower_biomes": {
       "minecraft:old_growth_birch_forest": { "bloom": 35, "split": {"peonybloom": 58, "lilybloom": 18, "bluebloom": 16, "onionbloom": 8} },
       "terralith:blooming_valley": { "bloom": 100, "split": "even" },
       "...": "the other 13 rows of the mooblooms.md 4.9 flower-biome table"
     },
     "extra_rules": [ { "biomes": ["minecraft:lush_caves"], "replace": [{"variant": "more_mooblooms:azaleabloom", "percent": 100}] } ] }
   ```
   A flower biome with no `split` uses the computed mix.
2. **Generator** `tools/gen_moobloom_spawns.py` (new):
   - imports `gen_wild_flowers as W` and builds the "after" inventory exactly as `W.main()` does (`load_world`,
     `build_snapshot`, `inventory(..., after=True)`);
   - loads the variant pools the way `check_moobloom_data.py` does (factor that loader into a function both can import);
   - computes per biome (with cows: from `kubejs/exported/spawns.json`, or the biome files' spawners plus
     `moobloom_cows.json` when no dump exists) the pool-flower sum, tier, mix, and the rule;
   - groups biomes with identical rules and writes `creativeengineering.json` (sorted, stable output so `git diff` is
     readable);
   - writes the 2.8 tables to a generated `feature_writeup/mooblooms_by_biome.md` (header "GENERATED, do not edit",
     like `bee_nests_by_biome.md`). mooblooms.md 2.8 then shrinks to "how to read" plus a link. Area % comes from
     `biome_config.json` `result_pct`.
   - `--check`: exit 1 if the rule file would change. Print the total "mooblooms per 100 cows" and the per-variant
     shares (4.9 "In numbers").
3. **Wire it in**: `tools/generators.json` entry `moobloom_spawns` after `wild_flowers` (it reads the snapshot), groups
   `core`, `worldgen`, outputs the rule file + the md, apply "`/reload`; only new spawns change". Add
   `python3 tools/gen_moobloom_spawns.py --check` to `tools/test_wild_flowers.sh` as a last step, so a flower change
   without a rule refresh fails the test.
4. **First run**: generate, diff against today's file. Differences should be explainable by the 2026-10-19 changes only.
   Show the user the changed rows before committing. Then `python3 tools/check_moobloom_data.py`.
5. Docs: flowers.md 4 "Mooblooms follow these flowers" → name the generator. mooblooms.md 4.9 "Retuning by hand" and
   section 7 "Open" → the generator; the flower-biome table points at `moobloom_spawn_tiers.json`.

### Ids and data

Variant ids `more_mooblooms:{moobloom,sunbloom,rosebloom,peonybloom,onionbloom,lilybloom,bluebloom,orchidbloom,oxeyebloom,azaleabloom,witherbloom}`
(the 11 files above). Pool flowers: the 16 in mooblooms.md 2.8.

### Decisions

- **Automate or keep manual?** Recommend automate (above). The hand step has already gone stale once (2026-10-19) and
  `gen_wild_flowers.py` has every input.
- **Where the user's splits live:** in `moobloom_spawn_tiers.json` (recommended) rather than parsed from the mooblooms.md
  table. Parsing markdown works (`gen_moobloom_variants.py` does it), but a JSON is less fragile for numbers.
- **Skylands:** `worldgen/biomes #3` may raise the skylands. mob_spawns.md 9 "Later" asks for mooblooms in the spring
  to autumn skylands at 100 % with an even split. With the generator, that is three `flower_biomes` entries.

### Gotchas

- Rounding: the current file rounds to 2 decimals and scales lavender's 106 split to 75 %. Do the same (normalize the
  split, then x bloom %).
- `check_moobloom_data.py` checks biome ids against `kubejs/exported/biome_data.json` (a dump). The generator should
  only use biomes from `wild_crops.json`'s table, to avoid naming cave or ocean biomes.
- Cows: without a fresh `/dumpspawns`, a biome that gained or lost cows since the last dump is wrong. Print a warning
  when the dump is older than `mob_spawns.json`.

### Tests

- Offline: `gen_moobloom_spawns.py --check` clean after a run; `test_wild_flowers.sh` fails when `wild_flowers.json`
  changes and the rules weren't regenerated (try it by changing one rate).
- In game: MB-36 / MB-38 / MB-96 / MB-97 (rule load and spawns) still apply; the load line should say the same rule
  count the generator printed.

---

## Built, needs play

| Row | Status | Note |
|---|---|---|
| flowers #1 Wild flowers system and first tuning pass | Testing | FL-01 to FL-14 open. flowers.md said the CEConfig switch "ships in 1.0.5"; fixed to DEPLOYED with 1.0.6 |

## Writeup corrections made

- [flowers.md](../flowers.md) 0: the `wildFlowers` switch row: "ships in 1.0.5" → DEPLOYED with pack 1.0.6 (1.0.5 was
  never deployed).
- [mooblooms.md](../mooblooms.md) 7: "the two approaches written up 2026-10-08, History" pointed at nothing (History
  has no such write-up). Now points here.
