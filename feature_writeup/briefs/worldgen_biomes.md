# Brief: worldgen / biomes

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# worldgen`, Sub `biomes` (rows #2 to #7; #1 is at Testing).
`biomes/netherblocks` is a separate subfamily and is not covered here. Feature writeup for the tuning rows:
[biome_tuning.md](../biome_tuning.md) (tests `WG-`). The solver is patcherMod
`development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/BiomeLayoutTuner.java` (written
`BiomeLayoutTuner.java` below). Its config is `kubejs/generation_scripts/worldgen/biome_config.json` (written
`biome_config.json`).

## Scope and order

| Row | Status | What is left | Order |
|---|---|---|---|
| biomes #4 | Planned | find out why the live End never has crystal peaks (and void barrens only once) | **first**. It is a bug lead, not just a rough number (see "the finding" in #4), and it decides #2's End half |
| biomes #2 | Planned | settle the unreachable targets: Nether + Overworld accept or set reachable values; End by #4's result | after #4; a 10-minute config edit once decided |
| biomes #3 | Planned | skylands multiplier 0.15: keep or raise | any time; one number, user decision |
| biomes #5 | Testing | permafrost merge built 2026-10-22, scratch server PASSED 2026-10-22 (SQ-MG, [item_merges.md](../item_merges.md) 2.7, MG-21, MG-22); left: MG names in game, PF-01 | done; in-game checks only |
| biomes #6 | Planned | leafless dead trees, grouped, some in shallow pools | independent; data + one `wild_crops.json` entry. Ties in with `structures/contents #2` (driftwood) |
| biomes #7 | Planned | small "texture not found" area low in the End | last; new blocks + a hand-built template. Uses the new-structure pipeline of `structures/new` |

#2, #3 and #4 are all `biome_config.json` edits, so do them in one pass with one `/reload`. They all apply to **new
worlds only** (biome_tuning.md 4). #5 and #6 change new chunks of any world. #7 is a structure, so it reaches new End
chunks of the live world too.

---

## biomes #4: End model agreement and one seed

Status Planned · Work tool · TODO `worldgen / biomes #4` (biome_tuning.md F4, F5)

### Goal

Find out whether the End results in `biome_config.json` describe the End that new worlds actually get. Then decide
whether to keep tuning the End.

### Now

- The End edits are `biome_config.json` lines 1259-1283: `nullscape:shadowlands` x0.5, `void_barrens` x0.75,
  `crystal_peaks` x0.25. The current file says agreement **80.128 %** (End samples 2026-10-18T23:32, 7,050 points:
  3,000 surface + 4,050 underground). The writeup and the TODO row still said 70 % (fixed in the writeup, see below).
- The baseline End source is Nullscape's `multi_noise`, 7 entries
  (`kubejs/generation_scripts/worldgen/biome_baselines/minecraft__the_end.json`). `minecraft:end_highlands` has the
  full box on every axis with offset 0.5. Shadowlands (0.49), void barrens (0.49) and the two crystal peaks entries
  (0.485) sit inside it. Verified from the baseline file.
- The live world is wrapped by Blueprint's `ModdedBiomeSource` (dump field `biome_source`). The dump reads climate
  and the live biome at the same quart through the same sampler (`kubejs/server_scripts/_lib/dump_climate.js:84-85`).

### The finding (offline, from the shipped dumps)

I recomputed the baseline model against the 2026-10-18 End dump in Python. Fitness = squared distance outside each
box + offset², quantized x10,000, the same as `BiomeLayoutTuner`.

| Live biome | Model biome (baseline) | Points |
|---|---|---|
| end_highlands | end_highlands | 4,530 |
| end_highlands | **shadowlands** | 657 |
| end_highlands | **crystal_peaks** | 380 |
| end_highlands | **void_barrens** | 364 |
| shadowlands, small islands, the_end | same | 1,119 |

Every one of the 1,401 disagreements is a point the live world calls end_highlands. **The live world has no crystal
peaks and no void barrens at all.** The tuned model expects 1.667 % and 2.767 % of the surface (about 50 and 83
points). The older dumps in git show the same pattern. Each live End held at most one of the three tuned Nullscape
biomes:

| Dump (git) | Live biomes besides highlands / small islands / the_end |
|---|---|
| 2026-10-03 (`d993d01`) | shadowlands only |
| 2026-10-10 (`52b2c63`) | void barrens only |
| 2026-10-14 (`15c43a0`) | none |
| 2026-10-18 (current) | shadowlands only |

The same check on the Nether dump matches the **tuned** results exactly: nether wastes 7,025 / 15,000 = 46.833 %, and
so on. So the dump worlds were created with the tuned layout, and the Nether model is right.

Likely cause: in the End every tuned box lies inside end_highlands' full box. Inside it, a biome's margin is a
constant offset gap (0.5² - 0.49² = 0.0099 for void barrens, 0.5² - 0.485² = 0.0148 for crystal peaks). Every
interior sample sits on one plateau, so a penalty either keeps the whole interior or removes all of it. This is the
"all-or-nothing" note on both biomes. Each world gets the solve of whatever samples existed when it was created, so
small sample changes flip whole biomes in or out. Not proven: the model on the *current* samples still says crystal
peaks keep 1.667 %. **The live server's End may be missing crystal peaks and void barrens.** That also removes
`creativeengineering:felsic_ore_crystal_peaks` / `_void_barrens`
(`kubejs/data/creativeengineering/forge/biome_modifier/felsic_ore_*.json`) from that End.

### Build

1. **Check the live world first** (decides everything else). On the deployed server, in the End:
   `/locate biome nullscape:crystal_peaks`, `/locate biome nullscape:void_barrens`, `/locate biome nullscape:shadowlands`.
   Record what is found. The server's `latest.log` from world creation (if kept) has the
   `[biome-tuning] ... live world matches the TUNED layout` / original line from `BiomeLayoutTuner.java:439-441`.
2. **Scratch A/B** (scratch server, same seed `7379340783510277000`):
   - world A with `[worldgen] biomeLayout = false`, run `/dumpclimate 3000`;
   - world B with it on, run `/dumpclimate 3000`.
   Run the confusion script below on both. A should agree near 100 % (if not, the model itself is wrong for the End:
   look at Blueprint's slice noise). B shows what the tuned End really is.
3. Add the confusion table to the tuner's output so this never needs a scratch script again.
   - In `BiomeLayoutTuner.java` around `:398-409`, where `agreeNew` is counted, also count `(live, model)` pairs per
     layer.
   - Write the top mismatches into the dimension's stats, e.g. `"mismatch": {"end_highlands->crystal_peaks": 380}`,
     next to `model_agreement_pct` (`:437`).
   - Log the line for a biome the live world never has but the model gives over 0.5 %.
4. **Decision** (below), then edit `biome_config.json`, `/reload`, new world, repeat step 2 B.
5. **F5 (one seed):** after the decision, one `/dumpclimate 3000` on a second seed per dimension. Compare
   `share_pct` per biome (expect within the sampling noise, about ±0.1 % at 3,000 columns). If it holds, mark F5 closed
   in biome_tuning.md. Don't ship the second seed's samples.

Confusion sketch (as run for this brief; inputs are the shipped files):

```python
d = json.load(gzip.open('climate/minecraft__the_end.json.gz'))      # fields: layer, 6 axes, live_biome
ents = json.load(open('biome_baselines/minecraft__the_end.json'))['generator']['biome_source']['biomes']
# per point: model = argmin over entries of sum(max(0, lo*1e4 - v, v - hi*1e4)**2) + (offset*1e4)**2
# Counter((layer, d['biomes'][p[-1]], model)) -> table above
```

### Decisions

- **Keep tuning the End?** Recommend: set all three End multipliers back to 1 (no End override is written then) unless
  step 2 B shows the tuned End keeps all three biomes. A multiplier can't scale an all-or-nothing biome. If the user
  still wants shadowlands rarer, the only exact tool is a new config field that shrinks a box (e.g. a lower
  `humidity` max for shadowlands' one entry) instead of moving an offset. Don't build that unless asked.
- **Second seed:** recommend yes, one dump each, not shipped.

### Gotchas

- Biome layouts are stored in level.dat at creation. None of this changes the live world. If step 1 shows missing
  biomes there, the only fixes are a new End (delete `DIM1` on the server; the End is small and rarely built in) or a
  structure / feature workaround. Ask the user before suggesting a DIM1 reset.
- `/dumpclimate` writes all three dimensions. Run it on the scratch server, not the live one, and only commit the
  samples that are meant to ship.
- `ModdedBiomeSource` (Blueprint) can put a "slice" of other biomes in. None was seen in the dumps (only Nullscape and
  vanilla End biomes appear).

### Tests (prefix WG-)

- WG-14 End biomes present: in a world created after the decision, `/locate biome` each of
  `nullscape:crystal_peaks`, `void_barrens`, `shadowlands` → all found.
- WG-15 Confusion stats: `/reload` → `biome_config.json` End stats list the mismatch pairs. With End multipliers at 1,
  agreement is above 95 %.

---

## biomes #2: Five unreachable targets

Status Planned · Work data · TODO `worldgen / biomes #2` (biome_tuning.md F1)

### Goal

Make `biome_config.json` say what actually happens. Either accept the solver notes, or set multipliers the solver can
reach.

### Now

The TODO row's list is from 2026-10-18. The current file (`biome_config.json`, notes written by
`BiomeLayoutTuner.java:423-426`) has **14** "aimed for" notes:

| Dimension | Biome | Mult. | Share → result (aimed) |
|---|---|---|---|
| Nether | nether_wastes | 2 | 34.9 → 46.833 (69.8) |
| Nether | soul_sand_valley | 1.25 | 17.4 → 18.933 (21.753) |
| End | crystal_peaks | 0.25 | 3.067 → 1.667 (0.767) |
| End | void_barrens | 0.75 | 3.967 → 2.767 (2.967) |
| Overworld | mushroom_fields | 3.75 | 0.04 → 0.06 (0.15) |
| Overworld | cave/mantle_caves | 1.61 | 0.746 → 0.725 (1.203) |
| Overworld | caldera | target 0.3 | 0 → 0.24 |
| Overworld | volcanic_peaks | 1.5 | 0.205 → 0.27 (0.31) |
| Overworld | lush_desert, desert_oasis, ashen_savanna, eroded_badlands, taiga, alpine_highlands | floor / donor values | within 0.01 to 0.05 % of the aim |

- The clamp is `PMAX = 0.6² x Q²` (`BiomeLayoutTuner.java:69`, applied at `:377`). The Nether's vanilla preset has
  zero-size boxes (points) and nether wastes has offset 0. It can't win more than its nearest neighbours lose within
  the clamp.
- The Overworld multipliers are owned by `tools/biome_rarity_floor.py` (`:8-16`, keep list `:30`, `:50`). It resets any
  multiplier it doesn't need to 1, so hand edits to its biomes are overwritten at the next run.

### Build

1. Wait for #4's decision for the End rows.
2. Nether: either leave it as is (the live world already matches the results exactly, see #4), or set
   `nether_wastes` 1.34 and `soul_sand_valley` 1.088 (= result / share) so the notes go away.
3. Overworld: leave them. The near misses are the solver's resolution. For mushroom fields, mantle caves and caldera
   the notes already say why. If the user wants the file clean, add a `--settle` option to `biome_rarity_floor.py`.
   It would read the last solve's `result_pct` and, for every noted biome, write `multiplier = result / share`
   (or `target_pct = result`). Run it after a `/reload`.
4. `/reload` → check the notes in `biome_config.json`; update biome_tuning.md 2.1 and F1.

### Decisions

- **Accept or set reachable values?** Recommend: accept for the Overworld (tool-owned, and close); set reachable values
  for the Nether (two numbers, so the file is honest); End per #4.

### Gotchas

- Raising the Nether clamp (PMAX) would let nether wastes grow further. It would also let every other biome's penalty
  go further, so redo all three dimensions. Not recommended for a cosmetic note.
- New worlds only.

### Tests

- WG-01 covers it (solve on reload, notes updated). No new test.

---

## biomes #3: Skylands at 15 %

Status Planned · Work data · TODO `worldgen / biomes #3` (biome_tuning.md F3)

### Goal

Decide whether the four Terralith skylands stay at 0.15x, given how many features point at them.

### Now

- `biome_config.json:1017-1048`: `terralith:skylands_{autumn,spring,summer,winter}` multiplier 0.15. The share is
  0.225 / 0.065 / 0.08 / 0.225 % and the result 0.03 / 0.01 / 0.01 / 0.035 %: together **0.085 % of the surface** (stock
  0.595 %). The 0.15 is in the initial commit, with no recorded reason. `tools/biome_rarity_floor.py:16,50` skips the
  skylands "on purpose".
- Pack features that target them:
  - permanent seasons, `kubejs/generation_scripts/worldgen/season_types.json:149-160`;
  - moobloom rules, skylands summer 50 % and spring 75 % of cows (`kubejs/data/more_mooblooms/moobloom_spawns/creativeengineering.json`)
    plus cows added to skylands spring (`kubejs/data/creativeengineering/forge/biome_modifier/moobloom_cows.json`);
  - chickens in every skylands biome (`kubejs/generation_scripts/worldgen/mob_spawns.json:121`);
  - bee nests `bloom_woods` (`bee_nests.json`);
  - wild crops (pumpkin = skylands autumn, `wild_crops.json:151-154, 214, 232`);
  - wild-flower palettes (`wild_flowers.json:184-187`).
- mob_spawns.md 9 "Later" also asks for mooblooms in the spring to autumn skylands, at 100 % with an even split.

### Build

1. Ask the user (decision below).
2. If raising: set the four multipliers in `biome_config.json`. Remove `'skylands'` from the keep set in
   `tools/biome_rarity_floor.py:50` only if they should follow the floor instead (0.15 % each).
3. `/reload`, new world, `/locate biome terralith:skylands_spring` (WG-06), and rerun the generators whose reports
   weight by area: `/cegen run worldgen` (bee nests, flowers, spawns, structures by biome).

### Decisions

- **0.15, the floor, or stock?** Recommend **1.0 (stock, 0.6 % of the surface together)**. Several systems were built
  for these biomes, and at 0.085 % a player may never see one. The rarity floor would give 0.15 % each (0.6 % total,
  about the same) but makes the tool own them. Keep 0.15 only if the user cut them on purpose (performance or looks).

### Gotchas

- New worlds only. Structures never fit the skylands (biome_fits.md), so no structure counts change.
- The rarity floor's donors (x0.736) were computed without the skylands' extra area. Rerun
  `biome_rarity_floor.py --dry-run` after the change to see if any donor moves.

### Tests

- WG-06 already covers it (note the distance). No new test.

---

## biomes #5: Permafrost merge

Status Testing (built 2026-10-22; scratch server PASSED 2026-10-22, SQ-MG-00 to SQ-MG-07; see [item_merges.md](../item_merges.md) 2.7) · Work patcherMod + KubeJS + config · TODO `worldgen / biomes #5`

### Goal

One permafrost in the world: Immersive Weathering's. Quark's block keeps existing (bricks, structures) under a new
name and gets a craft. The TODO row says IW's "damages on touch". That is not true with the current config (see
Now): check it before doing anything about it.

### Now (verified against the jars: Quark-3.4-418, immersive_weathering-1.19.2-1.2.10-forge)

- **Where Quark's permafrost generates.** It is not an ore or a cluster. It is an **underground style**
  (`vazkii.quark.content.world.module.PermafrostModule` → `undergroundstyle.PermafrostStyle`):
  - In `minecraft:frozen_peaks` caves between y 105 and 140 (`config/quark-common.toml:814-842`: rarity 2,
    horizontal size 100, vertical 30), it restyles cave surfaces. Floors and ceilings become `quark:permafrost`, and
    walls become packed ice (`PermafrostStyle()` passes the same vanilla block for all three, then `setBlock` sets
    floor and ceiling).
  - On 1.5 % of floor blocks it grows a 3 to 5 high pillar of permafrost (`PermafrostStyle.fillFloor`).
  - The generator is Java (`AbstractUndergroundStyleModule.setup` → `WorldGenHandler.addGenerator(...,
    UNDERGROUND_DECORATION)`), so a datapack can't touch it.
  - `BasicUndergroundStyle.floorState` / `ceilingState` are **public, non-final** fields, and
    `AbstractUndergroundStyleModule.generationSettings` is a public field. Its `biomeObj` is the style.
- **Where IW's permafrost generates.** Placed feature `immersive_weathering:cryosol_patch` → configured
  `immersive_weathering:permafrost`. It is a `minecraft:ore`, size 64:
  - stone, dirt and snow_block become `immersive_weathering:permafrost`; grass_block becomes `cryosol`;
  - placement: count 3 + noise count, at the heightmap, so it shows at the surface;
  - biome modifier `immersive_weathering:icicles` (step `top_layer_modification`) puts it in
    `#immersive_weathering:icy`: `#forge:is_snowy`, `#c:icy` and nine vanilla cold biomes.
- **"Damages on touch".** `PermafrostBlock.stepOn` (`m_141947_`) applies freezing only when
  `CommonConfigs.ENABLE_FREEZING_PERMAFROST` is on. The pack has `enable_permafrost_freezing = false`
  (`config/immersive_weathering-common.toml:45`, unchanged since the initial commit; not in `configs_edited/`). Frost
  blocks don't freeze either (`FrostBlock` has no entity hook). So with the shipped config, IW permafrost doesn't
  hurt anyone. Either the server runs a different config, or what the user saw was powder snow. **Test it in game
  first** (test below).
- Ids already in the dicts:
  - `kubejs/startup_scripts/_lib/item_dict.js`: `i.permafrost` = `quark:permafrost` (`:3292`, and the 10 variants at
    `:3293-3301`), `i.immersive_weathering_permafrost` (`:10678`), `i.cryosol` (`:10628`);
  - `kubejs/startup_scripts/_lib/block_dict.js:65-66`: `b.iw_permafrost`, `b.iw_cryosol`.
- Tags that name them: `kubejs/server_scripts/tags/blocktags.js:54` (keg freeze sources, IW only), `:285` (Block
  Runner natural smooth: `quark:permafrost*`), `:330` (Block Runner loose ground: IW permafrost).
- Recipes:
  - IW: `immersive_weathering:permafrost` = shapeless 2 packed ice + 2 dirt → 2;
  - Quark: no recipe makes `quark:permafrost` itself, only its bricks, slabs, stairs and walls (crafting and
    stonecutting under `data/quark/recipes/world/`).
- Renaming precedent: `kubejs/client_scripts/namechanges.js:137-147` renames Quark limestone to "Gabbro" with
  `e.renameItem(...)`.

### Build

1. **Generation swap (patcherMod).** New class `compat/QuarkPermafrostSwap.java`:
   - On `FMLCommonSetupEvent.enqueueWork`, if both `quark` and `immersive_weathering` are loaded and the new CEConfig
     `[worldgen.systems] permafrostMerge` (default true) is on, find the Quark module instance.
   - Then set `style.floorState = style.ceilingState = IW permafrost default state`.
   - Reapply on `ModConfigEvent.Reloading` for Quark's config, in case Quark rebuilds `generationSettings`.
   - Unverified: the module lookup API. In Quark 3.4, look for `ModuleLoader.INSTANCE.getModuleInstance(Class)` in
     `vazkii.quark.base.module.ModuleLoader`; decompile and confirm before writing.
   - Fallback without that lookup: a `@Mixin(PermafrostStyle.class)` with `@Inject` at HEAD of `fillFloor` and
     `fillCeiling` (the latter is inherited from `BasicUndergroundStyle`, so target that class and check
     `this instanceof PermafrostStyle`) that swaps the field before the call.
   ```java
   // sketch
   BasicUndergroundStyle s = (BasicUndergroundStyle) module.generationSettings.biomeObj;
   BlockState iw = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("immersive_weathering", "permafrost")).defaultBlockState();
   s.floorState = iw; s.ceilingState = iw;
   ```
   No config route exists for this: Quark's config can only turn the style off (biome list), not change its block.
2. **Rename Quark's family** in `namechanges.js`, next to the Gabbro block. Use dict keys, not literals (CLAUDE.md):
   `e.renameItem(item.permafrost, 'Frost Stone')`, `item.permafrost_bricks` → 'Frost Stone Bricks', and so on for all
   11 ids in `item_dict.js:3292-3301`.
   - Consider renaming the dict keys too (`i.frost_stone = 'quark:permafrost'`, ...) so `i.permafrost` no longer
     means Quark's. Nothing in `kubejs/` uses `item.permafrost*` yet (grep 2026-10-21), so the rename is free now.
     Add `i.permafrost` → IW only if a later script needs it.
   - Then run `tools/name_audit.py` (names.md).
3. **Recipe** in a server recipe script (wood/stone crafts: `kubejs/server_scripts/` stone section; pick the file
   that holds other Quark stone changes): `global.NCH.shapeless(Item.of(item.permafrost, 2),
   [item.packed_ice, item.packed_ice, item.stone, item.stone], e, 'frost_stone')`. This mirrors IW's 2+2 → 2.
   `item.packed_ice` and `item.stone` exist (`item_dict.js:1338, 1314`).
   Optional second recipe: IW permafrost + stone → Quark's (`'frost_stone_from_permafrost'`).
4. **Tags:** none needed. The Block Runner tiers already cover both blocks (`blocktags.js:285, 330`).
5. **Mirror config:** none (the swap is in code). The new CEConfig key needs patcherMod `CEConfig.java`
   `[worldgen.systems]`, next to `wildFlowers` / `beeNests`.
6. Structures keep `quark:permafrost` (the TODO row asks for that). To know which ones hold it, add a temporary pattern
   `(9, 'quark permafrost', r'quark:permafrost')` to `NETHER_TIERS` in `tools/nether_blocks_scan.py:36`, or run a
   one-off copy. It needs the instance's `mods/`, so run it on the dev machine. Record the result in the
   writeup, then remove the pattern.

### Ids and data

`quark:permafrost` (+ `_slab`, `_stairs`, `_vertical_slab`, `_wall`, `_bricks`, `_bricks_slab|stairs|vertical_slab|wall`),
`immersive_weathering:permafrost` (state `dirty=false`), `immersive_weathering:cryosol`, `minecraft:packed_ice`,
`minecraft:stone`. All verified in the jars.

### Decisions

- **New name for Quark's block.** Recommend "Frost Stone" (short, says stone, doesn't clash with IW). Alternatives:
  "Rimestone", "Glacial Stone".
- **Turn IW freezing on?** The row reads as if the damage is wanted gone; it is already off. Recommend leaving it off,
  and confirming in game.
- **Keep Quark's walls as packed ice?** Recommend yes (only floor and ceiling change).

### Gotchas

- IW permafrost's `dirty` state updates from the block above. A floor of it under dirt turns dirty, which is fine.
- `PermafrostStyle.fillFloor` pillars use `floorState`, so they become IW permafrost too.
- Block Runner: Quark's block is a "natural smooth" tier and IW's is "loose ground", so running speed in those caves
  changes slightly.
- Old chunks keep Quark's blocks.

### Tests (new writeup section; suggest prefix `PF-` in a short `permafrost.md`, or add to mob_block_tweaks.md as `MT-`)

- PF-01 Freezing: stand on `immersive_weathering:permafrost` for 30 s in survival → no freezing (config off). If it
  does freeze, the server config differs: compare the server's `config/immersive_weathering-common.toml`.
- PF-02 Generation: new frozen peaks chunks, caves at y 105-140 → IW permafrost floors and ceilings, packed ice walls,
  no `quark:permafrost`.
- PF-03 Names: JEI shows "Froststone" and its 10 variants (name as built; the plan said "Frost Stone").
- PF-04 Recipe: 2 packed ice + 2 stone → 2 Frost Stone.

---

## biomes #6: More dead trees

Status Planned · Work data · TODO `worldgen / biomes #6` (scratchpad 2026-10-21)

### Goal

Leafless dead trees, usually branched (not a single log column), often in groups, some standing in small shallow
stagnant pools.

### Now (verified against Terralith_1.19.3_v2.3.12 and Immersive Weathering 1.2.10)

- No mod in the pack ships a leafless standing tree.
  - Scanning every Terralith `minecraft:tree` configured feature found only part-leaf ones: the shattered savanna
    oaks / acacias (`terralith:savanna/shattered/*`, foliage up to 24:16 air:leaves) and a "lava tree" joke.
  - Terralith's `birch/fallen_tree` is a fallen log, dead bushes are bushes, and Upgrade Aquatic driftwood is a log.
- **Leafless works in data.** `terralith:volcano/lava_tree_meme` is a `minecraft:tree` with
  `random_spread_foliage_placer` (`leaf_placement_attempts: 0`) and an air foliage provider.
- Logs available: vanilla logs, stripped logs, `immersive_weathering:charred_log` (`i.charred_log`,
  `item_dict.js:10586`) and `immersive_weathering:rotten_log`.
- Vehicle for "which biomes": `wild_crops.json` already places pack tree features by trait (the `rubber` entry,
  `wild_crops.json:403-408` → `kubejs/data/creativeengineering/worldgen/{configured,placed}_feature/wild/rubber_tree.json`).
  `wild_crops.js` writes the tag and modifier, `tools/gen_crop_worldgen_table.py` reports it, and
  `tools/test_wild_crops.sh` tests it. Traits: swamp (4 biomes), savanna (11), badlands (8), dry (32), muddy (2).
- Bee nests: `BeeNests.java:150-161` skips trees without leaves, so dead trees never get nests.

### Build

1. **Configured features** in `kubejs/data/creativeengineering/worldgen/configured_feature/wild/dead_tree/`:
   - `oak_branched.json`: `minecraft:tree`, `trunk_placer` `minecraft:fancy_trunk_placer` (base 5, rand 2/2: the
     limbs are logs, so you get branches); `foliage_placer` `minecraft:random_spread_foliage_placer` radius 0,
     `leaf_placement_attempts` 0; `foliage_provider` air; `trunk_provider` weighted oak log 3 : stripped oak log 1;
     `decorators` []; `ignore_vines` true; `dirt_provider` dirt.
   - `forked.json`: same, with `minecraft:forking_trunk_placer` (acacia shape) and spruce/dark oak logs. Gives a
     second silhouette.
   - `charred.json`: `fancy_trunk_placer` with `immersive_weathering:charred_log` for ashen and volcanic biomes.
   - `group.json`: `minecraft:random_patch`, `tries` 4, `xz_spread` 5, `y_spread` 1, feature = a
     `minecraft:simple_random_selector` over the placed trunks (each with a `would_survive` oak sapling filter).
   - `pool.json`: `minecraft:waterlogged_vegetation_patch` (vanilla uses it for `clay_pool_with_dripleaves`).
     `replaceable` `#minecraft:dirt`, `ground_state` mud (or `immersive_weathering:silt`), depth 1, `xz_radius` 2-3,
     `vegetation_chance` 0.08, `vegetation_feature` = the dead tree. The surface layer becomes water, so the trees
     stand in about one block of water.
2. **Placed features** next to them (`placed_feature/wild/dead_tree/*.json`), like `wild/rubber_tree.json`:
   `rarity_filter`, `in_square`, `heightmap` `WORLD_SURFACE_WG` (pools) or `OCEAN_FLOOR` +
   `surface_water_depth_filter` 0 (dry ones), `biome`.
3. **Where** (`wild_crops.json`, new entries in the same `crops` object; the label says "Dead trees", not a crop):
   - `dead_trees_swamp`: `["swamp !cold"]`, features `[pool, group]`, rarity 1 in 6;
   - `dead_trees_dry`: `["warm dry savanna", "badlands grassy"]`, `[group]`, 1 in 12;
   - `dead_trees_ash`: `add: ["terralith:ashen_savanna", "terralith:volcanic_peaks", "terralith:caldera"]`,
     `[charred]`, 1 in 4;
   - `dead_trees_cold`: `["cold forest !snowy"]`, `[oak_branched]`, 1 in 20.
   `wild_crops.js:152-170` treats `disable_modifiers` and `remove_features` as optional, so a `features`-only entry
   works (checked). Check `crops_by_biome.md`'s generator doesn't assume a crop item; it may need a "tree"
   flag so the report puts them in their own table.
4. `python3 tools/gen_crop_worldgen_table.py`, `bash tools/test_wild_crops.sh`, `python3 tools/check_worldgen_json.py`.
5. Scratch server: `/place feature creativeengineering:wild/dead_tree/pool` (and the others) on a swamp and a savanna
   pad. Check the pool's tree actually stands in the water. If the trunk is blocked by the water block, use
   `minecraft:surface_water_depth_filter` max 1 placement on a plain tree instead of the vegetation patch (the vanilla
   swamp-oak method).

### Decisions

- **Source: mod features or own templates?** Recommend **own data features** (above). No mod ships one, and JSON trees
  are cheap and tunable. Hand-built NBT templates (structures) only if the user wants specific shapes; they would go
  through the structure pipeline.
- **Which wood?** Recommend oak / stripped oak (bleached look), spruce / dark oak for the forked ones, IW charred log in
  ash biomes. Not driftwood: `structures/contents #2` removes driftwood logs.
- **Replace the land driftwood?** `kubejs/data/creativeengineering/forge/biome_modifier/driftwood_land_{swamp,beach,extra}.json`
  put Upgrade Aquatic driftwood logs on swamp and beach land. When `structures/contents #2` removes driftwood logs,
  point `driftwood_land_swamp` at `creativeengineering:wild/dead_tree/group` instead. Recommend doing it then.

### Gotchas

- Feature order: add the new placed features only through the one `forge:add_features` modifier per entry (as
  wild_crops.js does), never into a biome JSON. Two biomes listing shared features in different orders crash world load.
- The pool's mud/water replaces dirt only (`replaceable`). On sand or terracotta (badlands) it does nothing, so pools
  go in swamps only.
- `TreeChop` / leaf decay: no leaves, so nothing decays. Chopping works as for any log.
- `would_survive` with an oak sapling rejects mud in pools. Use no survive filter on the pool's inner tree (the patch
  only runs on dirt anyway).

### Tests (crops.md prefix is `CR-`; or a short new section in crops.md)

- CR-xx Dead trees: new swamp chunks → leafless branched trees in small groups, some in one-deep pools; savanna and
  badlands → occasional groups; ashen savanna → charred trunks. No floating trees, no nests.

---

## biomes #7: "Texture not found" End biome

Status Planned · Work data / resource pack · TODO `worldgen / biomes #7` (scratchpad 2026-10-21)

### Goal

A small, rare joke area low in the End: magenta and black "missing texture" blocks, with missing-texture trees, sugar
cane, grass, flowers and fluid.

### Now

- Nothing exists. The End is Nullscape (`noise_settings/end.json`: `min_y` 0, height 288; verified in the jar), with
  islands down to low y.
- KubeJS 1902.6.2 (verified in the jar): `BlockBuilder` has `renderType`, `noCollision`, `notSolid`, `box`, `model`,
  `textureAll`, `tagBlock`. There is no cross-plant builder and no pillar builder. `FluidBuilder` has `stillTexture`,
  `flowingTexture`, `bucketColor`, `noBucket`. The pack registers blocks in `kubejs/startup_scripts/items/*.js`
  (e.g. `decor_blocks.js`) with hand-written models under `kubejs/assets/creativeengineering/`. No fluid is
  registered through KubeJS yet.

### Build

1. **Blocks** (`kubejs/startup_scripts/items/missing_blocks.js`, new; ids in `block_dict.js` / `item_dict.js` under
   `missing_*` keys):
   - `creativeengineering:missing_block` (end-stone stats). A log isn't needed: the texture is the same on every face,
     so `missing_block` serves as "log" in the trees.
   - `missing_leaves`: `notSolid`, `renderType('cutout')`, the same texture with holes.
   - `missing_grass` and `missing_flower`: `noCollision`, `notSolid`, `renderType('cutout')`, a model with parent
     `minecraft:block/cross`.
   - `missing_sugar_cane`: like the grass, taller texture. No growth (decor only).
   - Textures: 16x16 PNGs, 2x2 magenta `#F800F8` / black checks (the vanilla look), under
     `kubejs/assets/creativeengineering/textures/block/`. Ship real files. A real missing texture would spam the log
     and look different per atlas.
   - Fluid `creativeengineering:missing_fluid` (`StartupEvents.registry('fluid', ...)`, `stillTexture`/`flowingTexture`
     pointing at an animated checker). Add it to `fluid_dict.js`.
2. **Template.** The user builds one or two small islands (about 15x15) with a structure block, from these blocks:
   - pieces: a missing-texture tree, cane by a pool of the fluid, grass and flowers;
   - save as `creativeengineering:end/missing_island_1` in `kubejs/data/creativeengineering/structures/end/`.
3. **Structure** (data, the pipeline in [structure_tuning.md](../structure_tuning.md) 7.4.7; same steps as
   `structures/new #1-3`):
   - `worldgen/structure/end/missing_island.json`: `minecraft:jigsaw`, one-piece pool,
     `start_height` `{"absolute": 30}` to `uniform 20-50`, `project_start_to_heightmap` absent, `terrain_adaptation`
     `none`, biomes `#minecraft:is_end` without `minecraft:the_end`;
   - a structure set with `random_spread` spacing about 60, separation 20.
   - Then `gen_structure_config.py`, a `mod/creativeengineering` / `type/*` tag, `gen_structure_groups.py`, and it
     stays standalone (not in a group).
4. JEI: hide nothing (the blocks are a curiosity). Give them names in `namechanges.js` ("Missing Texture Block" and
   so on), or leave the raw keys as a joke. Ask.

### Decisions

- **Biome or structure?** Recommend **structure**. A new End biome needs an entry in the End's `multi_noise` list and
  a surface rule in Nullscape's `end.json`. It applies to new worlds only (level.dat), so it would never appear on the
  live server, and the End boxes are all-or-nothing (#4). A structure is small, rare, tunable, and appears in new End
  chunks of the live world.
- **Names:** raw `block.creativeengineering.missing_block` keys as part of the joke, or real names? Recommend real
  names ("Missing Texture Block"), so WTHIT and JEI don't look broken.

### Gotchas

- Anything with "missing" textures sets off the pack's audits: `client_scripts/texture_tool.js` (placeholder hunt) and
  `tools/missing_names.py`. Add the ids to their ignore lists if they flag them.
- Fluids from KubeJS need a bucket (or `noBucket`), and the fluid tag `minecraft:water` must NOT include it (or it
  would behave as water).
- Low y in the End is in the void band. The structure must carry its own ground (`terrain_adaptation` none, no
  projection).

### Tests (structure_tuning.md prefix `ST-`)

- ST-xx Missing island: `/locate structure creativeengineering:end/missing_island` in the End → found. The island
  floats low, with all the block kinds present and the fluid flowing in its pool.

---

## Built, needs play

| Row | Status | Note |
|---|---|---|
| biomes #1 Rarity floor, `target_pct`, 20,000-column and cave samples | Testing | WG-09 to WG-13 open; WG-01 to WG-08 never run. biome_tuning.md had stale End / Nether stats (fixed below) |

## Writeup corrections made

- [biome_tuning.md](../biome_tuning.md):
  - 2.1 End rows updated to the current file (shadowlands 18.933 → 9.633 %, void barrens 3.967 → 2.767 % aimed
    2.967, crystal peaks 3.067 → 1.667 % aimed 0.767); Nether crimson / warped results to the file's values;
    volcanic peaks result 0.25 → 0.27 %.
  - 2.1 samples paragraph: model agreement Nether 86.7 → 86.4 %, End 70.0 → 80.1 %, plus a note that the End's
    Nullscape biomes are now measured on the surface layer (the 2026-10-14 file measured them underground).
  - F1: End numbers to the current file, plus a note on the 10 Overworld notes.
  - F4: 80 % (was 70 %), Overworld 96.6 % (was 99 %), and a pointer to the #4 lead in this brief.
