# Namespace moves: what is left after the Creative Delight rename (2026-11-10)

Status: INSTALLED 2026-11-10 21:20 (section 6: steps 0 to 7, every jar; boot and exit PASS b1110; SQ-NSM not run). Rule and method: [plan_cd_namespace.md](plan_cd_namespace.md) section 0
(content, tags and data ids use the namespace of the jar that owns them). TODO row `creativesplit/ids #25` covers the
other jars; this file is its inventory and execution plan.

## 1. Decided: seasonings move, no swapper (owner, 2026-11-10)

Closes D6 of plan_cd_namespace.md. The five seasoning ids move from `creativeengineering:` to `creativedelight:`:
`cheese`, `onion`, `garlic`, `sugar`, `void_pepper`.

- **No NBT read alias and no swapper entry for seasoned food.** Food already seasoned in an existing world keeps the
  old text id in its `Seasonings` NBT and stops showing or applying that seasoning. Accepted by the owner. New and old
  seasoned stacks will not merge.
- Moves with the ids (same change): the `seasonable/*` tags (namespace is derived from the id), the overlay art
  `item/seasoned/<path>` (models in Delight's `assets/creativeengineering/models/item/seasoned/` and the textures, built by
  `textureswapper/seasoning/make_seasoning.py`), `SeasonedItemModel`, the `id:` fields in
  `kubejs_startup/creativedelight/seasonings.js`, `seasoning_tags.js`, `seasoning.js`, the seasoning lang keys,
  `AppleSkinSeasonings`, `SeasoningJEI`, and the `Seasonings` Java defaults.
- Generator and lint carry the old exception and need it removed: `tools/cd_namespace_ids.py:189` (comment and skip) and
  `tools/id_owner_lint.py:281`.
- Test to add (scratch queue): an old seasoned stack from a pre-move world loads without error or log spam, and a fresh
  seasoning craft, tooltip, overlay, eat effect and JEI page all work under `creativedelight:`.

## 2. Scan: what else still names the wrong namespace

Method: grepped `development/` for `creativeengineering:`, `creativecommon:`, `creative_delight:` and `solapplepie` per
jar, read each jar's registry classes, `kubejs_startup/` scripts, `assets/` and `data/` folders, and the id dicts in
Creative Common. Items marked (saved) are stored in worlds, so each needs a swap-list entry; the rest are not.
Counts are from the scan, not exact; step 1 of the work re-counts with `tools/cd_namespace_ids.py`-style tooling.

### 2.1 Registered content (saved) under `creativeengineering:` but owned by another jar

| Owner jar | What | Evidence |
|---|---|---|
| Creative Equipment | 7 tier shields (`ShieldItems`, `CreativeEquipment.NS`); `backpack_skin`, `backpack_skin_clear` and `elytra_duplication` recipe serializers; 16 stepstools; `air_brake`; echo tools (`echo_pickaxe/axe/shovel/hoe`); `arrow_fletching`; `felsic_scrap`; entity textures for the shields | `CreativeEquipment.NS = "creativeengineering"`; `assets/creativeengineering/` in the jar; `item_dict.js` rows 51-82, 5776-5779 |
| Creative Magic | `dark_fire` block, `flint_and_terrasteel` item (`CreativeMagic.NS`) | `CreativeMagic.java:42`; `block_dict.js:9`, `item_dict.js:995` |
| Creative Compat | `shrapnel` entity type, `botanist_pot` block entity type (`CreativeCompat.NS`), `weathered_cardboard_box` block and item, `layered_obsidian`; the painting and banner-pattern output namespace (`PaintingArt.OUT_NAMESPACE`, `BannerPatternArt.OUT_NS`) | Compat `NS` and `OUT_*` constants. Paintings: memory says built ids must not change, so decide before touching. |
| Creative Worldgen | `meta` structure type and 3 piece types, `tune_spawns` and `bee_nests` biome-modifier serializers; data ids `sky_island` structure, structure set and pool, `waystone_ruin_part`, 3 noise settings (`y208void`, `y32plus`, `y40plus`), 12+ placed features under `meta/`, 3 biome modifiers, dimensions (`yellowstone`, `wintersky`, `summersky`, `springsky`, `shield`), `hub_type` dimension type. Dimensions and structure ids are saved in world data. | `CreativeWorldgen.NS`; `data/creativeengineering/` in the jar, plus the `packs/*/data/creativeengineering` overlays |
| Creative Devtools | the world preset | `data/creativeengineering/worldgen/world_preset` |
| Creative Calendar | `crop_seasons` ids and the biome season tags (`SeasonTypes`) built from `CreativeCalendar.NAMESPACE`; the class javadoc still says "ids stay" | `CreativeCalendar.java:22-30` |
| Exposure backport (extras) | `OLD_NAMESPACE = "creativeengineering"` in `ExposureExtras`; camera bags | check whether this is the intended alias for a prior rename |
| Creative Delight | the 5 lunchbox items sit in `solapplepie` (named in plan_cd_namespace.md as "already outside"); 5 item models in `assets/creativeengineering/models/item/` that are not the seasoning overlays | decide: move to `creativedelight:` or keep as a Spice of Life (Apple Pie) add-on id |
| Creative Common | `item_dict.js` still holds 124 `creativeengineering:` values; the ones above are not Engineering's. Common's own registrations (potions, effects) are already `creativecommon:`. | `_lib/item_dict.js`, `block_dict.js` |

Engineering's own stay: components, coils, wires, dust piles, plates, zinc, brass and the other metal rows, `spool`,
`transmitter`, `wood_pellets`, `amethyst_dust` and `amethyst_lens`, `prepared_flesh_block`, `creosote_soaked_sawdust`,
`precision_sawblade` (all registered by Engineering's startup scripts).

### 2.2 Tags the dicts still key under `#creativeengineering:`

Not saved, no swap needed, but every reference moves in the same change.

| Tag | Used by | Likely owner |
|---|---|---|
| `trimmable_armor` | Common's `ArmorTrimsItemCache` | Creative Common or Equipment |
| `vein_undo_denied`, `villager_ladders`, `no_world_dye` | Common mixins | Creative Common |
| `stepstools` | stepstool code | Creative Equipment |
| `loot/chalk`, `loot/golden_food`, `trades/golden_food`, `trades/canned_golden` | loot and trade scripts | Creative Delight (Golden Food member) / Common |
| `railings`, `vertical_planks`, `unsmoked_planks`, `bulb_glass`, `carbon_filament` | recipe scripts | decide per tag |
| Worldgen biome fits, `template_trim/*`, `terrain_adaptation/*` | `StructureBiomeFits`, `TemplateTrim` code, `CreativeWorldgen.NS` | Creative Worldgen |
| Season biome tags | Calendar | Creative Calendar |

Plan_cd_namespace.md step 1 counted 140 shared tag keys; the CD-owned ones moved there, these are the remainder.

### 2.3 Script folders still named for the wrong jar

Not an id, but the same ownership mix-up. `kubejs/data/creativedelight/kubejs/creativedelight/` (30 files),
`kubejs/data/creativebrewing/kubejs/creativebrewing/` (5), and the matching `kubejs/assets/.../creativeengineering/`
folders (2 each). The `creativeengineering` subfolder is Delight's and Brewing's own scripts. The pack's
`kubejs/data/creativeengineering/` also holds the 60 painting JSONs (Compat) and `kubejs/assets/creativeengineering/`
holds 15 shield textures (Equipment) and `layered_obsidian` (Compat).

### 2.4 Mod ids and names that break the naming rule

Rule: own mod ids, packages, config files and script folders carry no underscore ([creative-mod-naming memory]).

- **Creative Labels**: mod id `creative_labels`, package `creative_labels`, assets and data folders, configs
  `creative_labels-client.toml` and `creative_labels-common.toml`. Saved ids (`face_paper`, `sticky_note`, `label`,
  `eraser`, 4 note recipe serializers) would move with it and need swap entries. Largest single item on this list.
- **Exposure extras**: mod id `exposure_extras`, config `exposure_extras-common.toml` (an own addon to a ported mod; check
  it against the rule before renaming).
- **Creative Delight**: `CfbCompartment.MODID = "creativedelight/cookingforblockheads"` has a slash; `VillageFarmCrops`
  keeps `creative_delight:` with a load alias (plan_cd_namespace.md).
- Ports keep upstream ids on purpose (`roll_delight`, `oceanic_delight`, `miners_delight`, `vinery_backport` and the
  rest); not violations.

### 2.5 Config files

`config/creativeengineering-common.toml` still carries the Shields, Stepstool, AirBrake and other sections for Equipment
features (named in the `item_dict.js` comments). Equipment has its own `creativeequipment-common.toml`; check what is
left in the Engineering file and move sections whose code lives elsewhere (copy edits to `configs_edited/`).

### 2.6 Stale comments and exceptions that state the old rule

`CreativeCalendar.java:22`, `CreativeWorldgen.java:57` (both say ids stay `creativeengineering`), the
`item_dict.js` comments that say shields and stepstools are "patcherMod" code, and the allow-list in
`tools/id_owner_lint.py`. Update with the moves.

## 3. Execution order

Every moved saved id gets a swap-list entry (generated, not hand-written) and, for capability or NBT-held ids, the
swapper `capabilities` copy. Each step: build, scratch rows in `feature_writeup/testing/scratch_queue.md`, changelog
bullet only where a player notices, then the next step. Step 0 is a re-count with a generator in the style of
`tools/cd_namespace_ids.py` (new `tools/owner_namespace_ids.py`) so the tables in section 2 become exact lists.

0. **Inventory tool.** Exact id lists per owner jar; also greps world `datapacks/`, `kubejs/` and `config/` (FTB Quests,
   JEI bookmarks, WTHIT, loot) for old ids.
1. **Seasonings** (section 1).
2. **Magic and Equipment** content (2.1 rows): items, blocks, serializers, shield entity textures, stepstool and echo
   rows, `item_dict.js` and `block_dict.js` values, config sections (2.5).
3. **Compat** content: shrapnel, botanist pot, cardboard box, layered obsidian, then **paintings and banner patterns**
   (D-PAINT below).
4. **Lunchboxes** to `creativedelight:` (D-LUNCH), with the Spice of Life item references and the 5 stray item models.
5. **Tags** (2.2): no saves, one change per owner jar, fillers into the owner's `kubejs/data/<ns>/kubejs/tags/`.
6. **Worldgen**: structures, structure sets, pools, noise settings, placed features, biome modifiers, serializers, then
   **dimensions and the dimension type** (D-DIM), plus Devtools' world preset and Calendar's ids.
7. **Creative Labels** rename (D-LABELS), **exposure extras** rename (D-EXPO), script folder renames (2.3), stale
   comments and lint allow-list (2.6).
8. Final: `python3 tools/id_owner_lint.py`, grep for leftover `creativeengineering:` outside Engineering's jar, changelog
   lint, writeups and TODO `creativesplit/ids #25` closed.

## 4. Decisions (owner, 2026-11-10)

All scan findings are to be changed. Previously open questions, answered:

- **D-PAINT: paintings and banner patterns move** to their owner's namespace (Compat). The earlier "built ids must not
  change" rule is reversed for this move: the swap list carries the painting variant ids (saved in painting entities)
  and banner pattern ids (saved in banner NBT, loom items). `PaintingArt.OUT_NAMESPACE` and `BannerPatternArt.OUT_NS`
  change, as do the 60 painting JSONs in `kubejs/data/creativeengineering/paintings/`. Risk: painting variants and
  banner patterns are not registry ids Forge remaps, so the swapper needs entity-NBT and item-NBT rewrites. Verify
  against `SwapRemapper` in step 3 before building; fall back to a data-fixer pass on chunk load if not.
- **D-LUNCH: lunchboxes move** from `solapplepie:` to `creativedelight:` (items, recipe references, skins and dyes,
  tags, JEI rows). Swap list entries for each lunchbox item; contents NBT is untouched.
- **D-DIM: dimensions move** (`yellowstone`, `wintersky`, `summersky`, `springsky`, `shield`, `hub_type`) to
  `creativeworldgen:`. Dimension ids are saved as folder names under the world (`dimensions/<ns>/<path>`) and in
  player data. Needs either a folder rename on world load or an offline migration step plus a swapper hook for
  `Dimension` fields in player and entity NBT. Test on a copy of a real save in step 6; this is the highest-risk step
  and goes last among the content moves.
- **D-LABELS: Creative Labels renames** mod id, package and files to `creativelabels` (assets, data, configs
  `creativelabels-client.toml` and `creativelabels-common.toml`, copied to `configs_edited/`). Saved ids `face_paper`,
  `sticky_note`, `label`, `eraser` and the 4 note serializers get swap entries. Config files: rename on first load or
  ship the renamed file, since the old toml is hand-edited. The `sticky_it` model layer namespace in `NoteShape` is a
  reference to an upstream resource pack, left alone unless it is our own.
- **D-EXPO: exposure extras renames** to a name without an underscore (proposed `exposureextras`; owner may pick
  another). Config `exposure_extras-common.toml` renames with it. The `OLD_NAMESPACE = "creativeengineering"` in
  `ExposureExtras` is the existing camera-bag alias; keep it as the permanent read alias and add the new target.
  Camera bag items and their stored contents must survive (test).
- Players are not asked to do anything; the old ids resolve through the swap lists. Typed commands (`/give`) need the
  new ids. Seasoned food is the one accepted loss (section 1).

## 5. Tests to queue (names reserved, rows written when each step is built)

SQ-NSM-01 seasonings (section 1); -02 Equipment and Magic items in an old save (inventory, chest, dropped, worn
shield and stepstool in Curios, dark fire block in chunks); -03 Compat entities and block entities; -04 old paintings
and banners keep their art; -05 lunchbox with contents; -06 tag-driven recipes still resolve (dump grep for
`minecraft:air`); -07 old save with the old dimensions loads and players return to the right dimension; -08 new world
generates structures and dimensions under the new ids; -09 Creative Labels notes keep text and layers, config values
carry over; -10 exposure extras camera bag contents survive; CQ-NSM-01 client: lang, models and textures for every
moved id, JEI, WTHIT.


## 6. Execution (2026-11-10)

Tools: `tools/owner_namespace_ids.py` (step 0: the owner table, checked against the dicts, the dump and the jar
sources; writes each owner's swap list `data/<ns>/swaps/owner_namespace.json` and `tools/owner_namespace_ids.json`) and
`tools/owner_namespace_move.py` (the rewrite: string ids, Java two-argument `ResourceLocation`s, lang keys, swap-list
survivors, and the resource folder moves by `git mv`; `--left` lists what it left alone). `tools/id_owner_lint.py` now
checks every jar but Engineering for `creativeengineering` registrations, ResourceLocations, namespace constants and
resource folders (`owner-ns`); a line naming `OLD_NAMESPACE`, `OLD_FILE` or `LEGACY_NS` is an intended read alias.

Corrections to the scan (section 2):
- `layered_obsidian` stays Engineering's: Engineering's mixing makes it and its saw and crushers use it. Only its
  registration script sits in the pack's `kubejs/startup_scripts/items/decor_blocks.js`.
- D-LUNCH covers one id. `lunchbag`, `lunchbox` and `golden_lunchbox` are Spice of Life's own items; only
  `netherite_lunchbox` was registered by Creative Delight under `solapplepie:`. It moved to `creativedelight:`.
- The 15 textures in `kubejs/assets/creativeengineering/` were the Canned Golem dye colours (Creative Delight), not
  shield art; they moved to `kubejs/assets/creativedelight/`.
- Section 2.5 was already done: `config/creativeengineering-common.toml` no longer exists.
- Paintings: Immersive Paintings stores the full png id only on the placed entity (the item has none), so D-PAINT is a
  read alias: Compat `mixin/ImmersivePaintingMotiveAliasMixin` rewrites an old `creativeengineering:paintings/...`
  motive to `creativecompat:` when the entity loads. Banner patterns needed no save handling: `BannerPatternArt.OUT_NS`
  only names generated textures.
- Leftovers of plan_cd_namespace.md fixed here: its rewrite missed Java's two-argument form, so Delight member tags
  (`fishing_net/*`, `chopstick_foods`, `food_type/*` in Oceanic, Roll, Fungi, Hearth) still read `creativeengineering`;
  `tools/gen_soup_soft_fluids.py` still wrote to the old folder.

Done per step:
1. Seasonings: ids in `seasonings.js`, the barbeque skewer's garlic, the overlay art to `assets/creativedelight/`,
   `make_seasoning.py` output, lint and generator exceptions removed. UI lang keys (`creativeengineering.seasoning.*`)
   kept: text keys, not ids.
2. Equipment (34 ids: 16 stepstools, 7 tier shields, 4 echo tools, air brake, felsic scrap, arrow fletching, 3 recipe
   serializers, the copper tier name) and Magic (Dark Fire, Flint and Terrasteel): `NS = MOD_ID`, assets and the felsic
   ore packs moved, dicts rewritten, swap lists generated.
3. Compat: weathered cardboard box, shrapnel entity, botanist pot block entity, capabilities `jar_fluid_pipes` and
   `nightstand_storage` (swap `capabilities` entries), loot table `misc/monster_box_reward` (config default and live
   value), models, banner textures, paintings (60 vanilla files to `kubejs/data/creativecompat/paintings/`,
   `gen_immersive_paintings.py` output). NBT key strings (`creativeengineering:monster_box` and the like) kept.
4. Netherite lunchbox (see corrections).
5. Tags: Equipment `stepstools`, `backpack_skins`, `bonemeal_charms`, `elytra_chestplates`, `needs_copper_tool`,
   `dragon_gear`, `bottled_air/*`; Compat `blockrunner_face_up`, `camo_speed`, `sinking`, `vein_undo_denied`,
   `not_in_flower_boxes`, `flower_box_plants`, `no_world_dye`; Common `trimmable_armor`, `trades/golden_food`,
   `trades/canned_golden`; Worldgen `villager_ladders`, `loot/golden_food`, `loot/chalk`, `mob_spawns/*`. Engineering
   keeps `railings`, `vertical_planks`, `unsmoked_planks`, `bulb_glass`, `carbon_filament` (its own recipes).
6. Worldgen (`creativeworldgen:`): 71 data files (11 dimensions, `hub_type`, 3 noise settings, meta placed
   features, sky island structure, set and pools, biome modifiers, the `packs/*` overlays), the `meta` structure and
   piece types, 2 biome modifier codecs (swap list), every tag and config default; Calendar (`creativecalendar:`, seasons,
   snow, crop_seasons tags; no registry entries); Devtools world preset `flower_test`. Old saves, detail in
   [creative_worldgen.md](creative_worldgen.md) "Namespace move": `mixin/ResourceKeyLegacyDimensionMixin` aliases the
   old dimension, dimension type and noise keys wherever a key is read (level.dat, player `Dimension` and
   `SpawnDimension`, Waystones, FTB Chunks, portals, typed `/execute in`); `mixin/LevelStorageAccessDimensionMoveMixin`
   moves `dimensions/creativeengineering/<dim>` to `creativeworldgen/` before anything loads (never merges);
   `ChunkSerializerLegacyStructuresMixin` and `StructureCheckLegacyStructuresMixin` rename structure starts, references
   and piece ids in chunk NBT (`worldgen/LegacyIds`). Hash seeds keep the old strings (`LegacyIds.seed`, the overworld
   `surface_mix/*` names), so old worlds generate the same. Kept: `NamedMobs.KEY`, spawn box keys, the ring cache file
   name, the locate ticket name. Not movable by server code: client per-dimension caches (Xaero maps, Distant Horizons
   LODs). Javac harness 10/10 on `LegacyIds` and `DimensionMove`; the mixins are untested in game (SQ-NSM-07, -08).
7. Creative Labels to `creativelabels` and Exposure extras to `exposureextras` (mod ids, packages, folders, configs
   carried over on first load by each jar, swap lists with `namespaces` rules plus `replaces` for block entity
   aliases); the script folders `kubejs/{data,assets}/creativedelight/kubejs/creativeengineering/` renamed to
   `creativedelight/` and Brewing's to `creativebrewing/` (same load order); path references rewritten.

Built: Common, Equipment, Magic, Compat, Delight, Brewing, Worldgen, Calendar, Devtools, Labels (`creativeLabels-1.0.1.jar`), Exposure
(`exposureBackport-1.9.19-ce-19.2-04.jar`). Creative Delight (`creativedelight-1.1.0.jar`) built 20:10 after the Hearth crow compile error was fixed by its own session. Install all
of them together with the game closed (the swap lists and the dicts must arrive in one go), then run SQ-NSM.

At install (game closed): copy every built jar; then `sed -i 's/creativeengineering:/creativeworldgen:/g' config/creativeworldgen-common.toml`
(left unedited while the game runs because the game watches it; the `configs_edited/` copy is already rewritten and
`LegacyIds.configIds` reads the old values meanwhile). Do not `/reload` the running game: `kubejs/data/creativeworldgen`
and `creativecalendar` already emit the new ids. Left on purpose: `kubejs/data/minecraft/worldgen/noise_settings/*`
`creativeengineering:surface_mix/*` (seed names), generated `*_by_biome.md` tables and JEI-world files (regenerate after
`/dumpall` and `/cdevgen`).

### 6.1 Fixes at install (2026-11-10, scratch b1110)

The rewrite and `id_owner_lint.py` miss ids built by concatenation or template strings, and one folder move nested
the old namespace. Found by the first boots on the installed jars, fixed and rebuilt:
- Worldgen `ResourceKeyLegacyDimensionMixin`: boot crash (NPE), Forge creates a key for a null default key during
  `Registry.<clinit>`; null guard added.
- Equipment `items/echo_tools.js` registered `'creativeengineering:' + name`; now `creativeequipment:`.
- Common `neoCraftHelper.js`: `backpack_skin`, `backpack_skin_clear`, `elytra_duplication` recipe types used
  `${global.NCH.modId}` (Engineering's id); now `creativeequipment:`. `dragon_breathing` stays Engineering's.
- Delight `Lunchboxes.NETHERITE_LUNCHBOX` and `LunchboxItems`' register still used `solapplepie`; now `creativedelight`.
- Devtools world preset `flower_test` sat in `data/creativedevtools/creativeengineering/worldgen/`; moved to
  `data/creativedevtools/worldgen/`.
- `tools/creative_labels_test/` (server probe, javac tests) renamed to the `creativelabels` package and ids.
SQ-NSM fixture: `mods/` now holds the moved jars; build the old save from `deploy/latest/mods/` (1.2.02).
