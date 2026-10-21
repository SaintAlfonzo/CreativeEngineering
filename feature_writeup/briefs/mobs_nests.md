# mobs / nests: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# mobs`, Sub `nests`. Feature writeup: [nests.md](../nests.md)
(test prefix `NE-`, highest id in use NE-14). Mod source: `development/incubationAdditions/` (Incubation Additions
1.0.0, `incubation_additions`, GPLv3). It extends Incubation 3.0.1 (Team Abnormals, **Abnormals License 1.0**, text
in [mooblooms.md](../mooblooms.md) 4.0).

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #2 | License fix before publishing | Planned | small to medium: delete / repoint 2 egg sheets, repaint up to 9 PNGs |
| #3 | Nest variety in spawning | Planned | medium: worldgen JSON in the mod's built-in packs + a pack tag script; the repaint is #2's |

Build order: **#2 then #3, released together as Incubation Additions 1.1.0** (one jar, both sides). #3's "new
textures for the duplicated nests" is the same repaint #2 needs, so paint once. The worldgen half of #3 needs new
chunks to see anything, so test it in a fresh scratch world.

Related: `mobs/spawns #2` / `#3` (where ducks, geese and emus spawn; the nest biome tags should follow them),
`mobs/mooblooms` (same license, same rules: nothing of an Abnormals mod copied into our jars).

---

## nests #2: License fix before publishing
Status Planned / Work new jar / TODO `mobs/nests #2` (nests.md 4)

### Goal
No file in the Incubation Additions jar is a copy or a modified copy of an Incubation file. Referring to Incubation's
models and textures by resource location is fine (a reference is not a copy).

### Now (every PNG in the jar compared with `incubation-1.19.2-3.0.1.jar` on 2026-10-21; md5 and per-pixel)

| Our file (under `src/main/resources/packs/<mod>/assets/incubation_additions/textures/`) | Finding |
|---|---|
| `untitledduckmod/.../block/duck_nest.png`, `block/goose_nest.png` | **identical** to Incubation `block/hay_nest.png` (md5 71219c8f) |
| `untitledduckmod/.../item/duck_nest.png`, `item/goose_nest.png` | **identical** to Incubation `item/hay_nest.png` (md5 2b2721da) |
| `untitledduckmod/.../block/duck_eggs.png` | **identical** to Incubation `block/duck_eggs.png` (md5 a7780129). nests.md called it "yours"; corrected |
| `untitledduckmod/.../block/goose_eggs.png` | **identical** to Incubation `block/turkey_eggs.png` (md5 abb3182d). Same |
| `alexsmobs/.../block/emu_eggs.png` | recolour of Incubation `turkey_eggs.png` (same alpha mask, luminance correlation 0.98): a modified copy |
| `untitledduckmod/.../block/{duck,goose}_egg_crate_{top,side}.png` (4) | 112 of 256 pixels identical to Incubation `chicken_egg_crate_{top,side}.png`: the crate frame is copied, the eggs recoloured |
| `untitledduckmod/.../item/duck_egg.png`, `goose_egg.png` | no match with any Incubation texture: ours |

Models and blockstates: every nest and egg model only **parents** Incubation's (`duck_nest.json` ->
`incubation:block/twig_nest` with a `nest` texture override; `one_duck_egg.json` -> `incubation:block/one_duck_egg`
with an `eggs` override; crates use `cube_bottom_top` with Incubation's `chicken_egg_crate_bottom` by id). So the
TODO's "write the models from scratch or reference Incubation's by resource location" is already done. The
blockstates are our own multipart lists with our ids (same shape as Incubation's `hay_duck_nest.json`, which is
just the standard way to write one); low risk, see Decisions.

### Build
1. **Egg sheets that are exact copies: reference instead of ship.**
   - duck: in the six `models/block/*_duck_egg(s).json`, drop the `eggs` / `particle` override (or point `eggs` at
     `incubation:block/duck_eggs`); delete `textures/block/duck_eggs.png`. The look is unchanged (the files were
     identical).
   - goose: point `eggs` at `incubation:block/turkey_eggs` in the six `*_goose_egg(s).json`; delete
     `goose_eggs.png`. Unchanged look. Or paint a real goose sheet (the user wanted distinct eggs, nests.md 4
     "Egg geometry follows the painted layout").
2. **Emu egg sheet:** repaint from scratch on the turkey layout (UV table in nests.md 2.3), or reference
   `incubation:block/turkey_eggs` and accept turkey-coloured emu eggs. Recommend repaint (emu eggs are dark green in
   Alex's Mobs).
3. **Nest base + item (4 PNGs):** repaint duck and goose as two different nests (this is #3's "new textures for the
   duplicated nests"), 16x16, block sheet read by the twig-nest geometry (u/v 0-16 x 0-3 for the rims, 3-13 for the
   floor). Until painted, a no-copy stopgap is `"nest": "incubation:block/hay_nest"` and
   `"layer0": "incubation:item/hay_nest"` and deleting the PNGs.
4. **Crates (4 PNGs):** repaint the frame from scratch (not traced from the chicken crate), or reference Incubation's
   `chicken_egg_crate_top/side` (then they look like chicken crates). Recommend repaint.
5. Hot-swap first (nests.md 2.3: `kubejs/assets/incubation_additions/textures/...`, F3+T), then copy into the packs
   and delete the `kubejs/assets` copies.
6. Add an offline guard so this doesn't come back: `tools/check_asset_copies.py` (new) that, for each in-tree mod jar
   (`development/*/src/main/resources`), compares every PNG with every PNG in the Abnormals-licensed jars the pack
   ships (Incubation, Buzzier Bees, Caverns and Chasms, ...: the jars whose `mods.toml` license points at
   team-abnormals) by md5 and by alpha mask + pixel overlap, and fails on a match. The jars are not in the repo; take
   them from `mods/` when run on the user's machine.
7. `mod_version=1.1.0` in `development/incubationAdditions/gradle.properties`, `./gradlew :incubationAdditions:build`,
   `installToMods`, check `unzip -l` (nests.md 6), then nests.md 2.3 tables, 4 (close the license bullet), 8 History.

### Ids and data
No id changes. Incubation texture ids used by reference: `incubation:block/duck_eggs`, `incubation:block/turkey_eggs`,
`incubation:block/hay_nest`, `incubation:item/hay_nest`, `incubation:block/twig_nest`,
`incubation:block/chicken_egg_crate_{top,side,bottom}` (all verified present in the jar).

### Decisions
- **Duck/goose egg sheets: reference or repaint?** Reference now (zero visual change, removes 2 copies); repaint
  later only if the user wants goose eggs that differ from turkey eggs.
- **Blockstates:** keep (our ids, mechanical structure). If the user wants zero doubt, generate them from a 10-line
  script instead of hand-copying the shape.
- The user decided the general approach already (TODO Decisions "none (decided)").

### Gotchas
- After a texture becomes a reference, a hot-swap PNG must go to the **Incubation** path
  (`kubejs/assets/incubation/textures/...`) and would also change Incubation's own nests.
- `client/DuckModEggModels` swaps the duck mod's egg item models for our `item/duck_egg` / `goose_egg`: those two
  sprites are ours, leave them.
- The emu nest base/item already reference Incubation's twig nest (fine as is).

### Tests
- **NE-15 No copied assets:** `python3 tools/check_asset_copies.py` passes; `unzip -l` of the 1.1.0 jar has no
  `duck_eggs.png` / `goose_eggs.png` (or repainted ones).
- NE-11 (reword after the repaint: duck and goose nests look different from each other and from the hay nest),
  NE-14 (crates), NE-03 to NE-05 (eggs still render on the right geometry).

---

## nests #3: Nest variety in spawning
Status Planned / Work new jar / data / TODO `mobs/nests #3` (scratchpad 2026-10-21: "nest variety spawning: new
textures for duplicated; nests in goose, duck, emu habitat"; nests.md 4)

### Goal
Duck, goose and emu nests generate in the wild where those birds live, the way Incubation's own chicken nests do.
New nest textures: see #2.

### Now
- **Incubation already has the machinery** (verified by decompiling `incubation-1.19.2-3.0.1.jar`):
  - feature type `incubation:bird_nest` (`BirdNestFeature`), config `{ "state": <BlockState>, "entity_id": <id> }`
    (`NestConfiguration`). At the surface (`WORLD_SURFACE` heightmap) it places `state` with `eggs` = 2-4, only on
    an air block above **`minecraft:grass_block`**, then tries 4 times to spawn `entity_id` within ~4.5 blocks
    (`MobSpawnType.STRUCTURE`, through `SpawnPlacements.checkSpawnRules`, a third of them babies).
  - placed features `incubation:nest_chicken` / `nest_duck` / `nest_turkey`: `rarity_filter` 1 in 32 chunks,
    `in_square`, heightmap, `biome`.
  - biome modifiers `data/incubation/forge/biome_modifier/add_feature/{chicken,duck,turkey}_nest.json`
    (`forge:add_features`, step `vegetal_decoration`) on tags `#incubation:has_feature/<bird>_nest`. Chicken: forest,
    flower forest, birch forests, dark forest, `#is_jungle`, `#is_taiga` (live in this pack: nothing turns it off).
    Duck / turkey: `#environmental:has_animal/duck` / `#autumnity:has_animal/turkey`, optional, empty here.
- Our filled nests are Incubation `BirdNestBlock`s (`WaterfowlNests.java:48-57`, `EmuNests.java:41`) with the
  `eggs` property, so the feature takes them as `state`.
- Habitats in this pack:
  - ducks and geese: the duck mod's own `#untitledduckmod:duck_spawn` / `goose_spawn` (= `#minecraft:is_river`,
    `#c:river`) plus the pack's rules `duck_wetlands` / `goose_wetlands` (swamps, beaches) and `duck_ponds` /
    `goose_ponds` (every grassy land biome, water only) in `mob_spawns.json`, whose biome tags
    `#creativeengineering:mob_spawns/<rule id>` exist at runtime (`mob_spawns.js:282-291`, via `global.tags`).
  - emus: Alex's Mobs config selectors, no tag: `#is_badlands`, `#is_savanna` and 8 Terralith biomes (mob_spawns.md
    10.7 "Emu").
- Data placement pattern: everything for one bird mod goes in that mod's built-in pack (`packs/untitledduckmod/`,
  `packs/alexsmobs/`), loaded only when the bird mod is (nests.md 3, items 6-7).

### Build
1. **Configured + placed features**, in `packs/untitledduckmod/data/incubation_additions/worldgen/`:
   ```json
   // configured_feature/nest_duck.json
   { "type": "incubation:bird_nest",
     "config": { "state": { "Name": "incubation_additions:duck_egg_nest", "Properties": { "eggs": "1" } },
                 "entity_id": "untitledduckmod:duck" } }
   // placed_feature/nest_duck.json
   { "feature": "incubation_additions:nest_duck",
     "placement": [ { "type": "minecraft:rarity_filter", "chance": 32 }, { "type": "minecraft:in_square" },
                    { "type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING" }, { "type": "minecraft:biome" } ] }
   ```
   Same for `nest_goose` (`goose_egg_nest`, `untitledduckmod:goose`) and, in `packs/alexsmobs/`, `nest_emu`
   (`emu_egg_nest`, `alexsmobs:emu`).
2. **Biome tags** `data/incubation_additions/tags/worldgen/biome/has_feature/{duck,goose,emu}_nest.json` in the same
   packs, mod defaults: duck/goose `{"id": "#untitledduckmod:duck_spawn"}` / `goose_spawn`; emu `#minecraft:is_savanna`
   (badlands are mostly sand, where the feature can't place).
3. **Biome modifiers** `data/incubation_additions/forge/biome_modifier/add_feature/{duck,goose,emu}_nest.json`:
   `{"type": "forge:add_features", "biomes": "#incubation_additions:has_feature/duck_nest", "features":
   "incubation_additions:nest_duck", "step": "vegetal_decoration"}`.
4. **Pack side** (habitats follow the pack's spawn rules): extend the tags from a server script, e.g. in
   `kubejs/server_scripts/worldgen/mob_spawns.js`'s `ServerEvents.tags('worldgen/biome')` block or a new
   `worldgen/bird_nests.js`, through `global.tags.add`:
   ```js
   const t = global.tags
   t.add(e, 'incubation_additions:has_feature/duck_nest', ['#creativeengineering:mob_spawns/duck_wetlands'])
   t.add(e, 'incubation_additions:has_feature/goose_nest', ['#creativeengineering:mob_spawns/goose_wetlands'])
   t.add(e, 'incubation_additions:has_feature/emu_nest', ['terralith:arid_highlands', 'terralith:brushland', /* the 10.7 list */])
   ```
   Not `*_ponds`: those match every grassy land biome. Biome ids belong in a dict only if the pack keeps one; it has
   none for biomes (`kubejs/startup_scripts/_lib/` has item/block/fluid/effect dicts), so literals as in
   `mob_spawns.json` are the convention.
5. Rebuild (with #2), new scratch world, look for nests (`/place feature incubation_additions:nest_duck` works on a
   grass block for a quick look).
6. Docs: nests.md 0 (new "Wild nests" row), 2 (a 2.4 "Where nests generate" table), 4 (drop the planned bullet),
   5 file map, 8 History; mob_spawns.md 9 if it lists nests.

### Decisions
- **Spawn birds with the nest?** The feature always tries 4 of `entity_id`. Recommend yes for emus (savanna herds) and
  ducks/geese in swamps. To get nests without birds, `entity_id` must name a non-`Mob` entity (e.g.
  `minecraft:marker`): the feature skips non-Mobs. Never an unknown id: the entity registry is defaulted, so an
  unknown id resolves to `minecraft:pig`.
- **Rarity:** 1 in 32 chunks like Incubation's (recommended start); wetlands are small, so 1 in 16 for duck/goose
  if they turn out too rare.
- **Which biomes:** recommended above (river banks from the duck mod's tag, swamps/beaches from the pack, emu
  savannas). Beaches mostly fail (sand), which is fine.
- **Mod or pack data?** Features and default tags in the mod (they only make sense with the bird mod, and the packs
  gate them); the pack's own habitat choice in KubeJS. Recommended.

### Gotchas
- Grass block only: badlands, sand beaches and snow-covered ground get no nests (the feature checks the block below
  is exactly `grass_block`; SRG `Blocks.f_50440_` in the decompile, read as grass_block from 1.19.2 mappings).
- The birds are spawned during feature placement, before the chunk's light step; Incubation's chickens use the same
  path, so if chickens appear next to wild twig nests in a forest (check first), ducks and emus will too. Emus need
  light above 8 in Alex's Mobs' rule.
- New chunks only, and biome modifiers are read at world load (rejoin after a tag change).
- The `eggs` property is overwritten by the feature (2-4); the value in `state` doesn't matter but must be valid
  (1-6).
- Mob counts: every nest adds up to 4 persistent-ish birds at worldgen; watch the creature cap in wetlands with
  `mob_spawns` rules already adding ducks and geese.

### Tests
- **NE-16 Wild duck/goose nests:** new world, a swamp and a river bank -> now and then a duck or goose nest with 2-4
  eggs on grass, ducks / geese nearby; never in a desert.
- **NE-17 Wild emu nests:** a savanna -> emu nests with 2-4 emu eggs, emus nearby.
- **NE-18 Gating:** Forge + Incubation + this jar + only the duck mod -> no emu nest feature, no errors (`/place
  feature incubation_additions:nest_emu` unknown); the same with only Alex's Mobs for the duck/goose features.

---

## Built, needs play

| Row | Change | Status | Notes |
|---|---|---|---|
| #1 | Incubation Additions 1.0.0 | Testing | DEPLOYED with pack 1.0.6 (2026-10-14). Stale text found: nests.md called the duck and goose egg sheets "yours" (they are Incubation's files, see #2); corrected. NE-11's "nest bases and items look like the hay nest" is true until #2 repaints them |

## Writeup corrections made

- [nests.md](../nests.md) 0 (Textures row): the duck/goose egg sheets are byte-identical copies of Incubation's, the emu sheet a recolour.
- [nests.md](../nests.md) 2.3 Duck nest / Goose nest tables: `duck_eggs.png` / `goose_eggs.png` "(yours, 2026-10-05)" -> byte-identical to Incubation's `duck_eggs.png` / `turkey_eggs.png`.
- [nests.md](../nests.md) 2.3 hotswap note: a recolour of an Incubation sheet is still a modified copy (not for the jar).
- [nests.md](../nests.md) 4 license bullet: the models only parent Incubation's (no copy); added the egg sheets, emu sheet and crate PNGs to the copied list; links this brief.
