# Farmer's Structures 1.0.6 against the installed 1.0.0

**BUILT 2026-10-25, Shipped in 1.1.03 (deployed to server and players 2026-10-26); scratch SQ-FSD-01, -03, -04, -06 PASSED 2026-10-26, -07, -08 PASSED r1102b 2026-11-02, **SQ-FSD-02 FAILED r1102b** (without the donor the world does not load; section 4); not run in game.** Installed: `FarmersStructures-1.0.0-1.19.2.jar` (`mods/`, `server_mods/`), mod id
`farmers_structures`, data only (one class). The 1.0.6 NeoForge jar is a resource donor (section 5): all 6 new Overworld
structures, the cod pond and the grown cook house pools are served from it, converted in memory. Evaluated against
`devmods/checkNewerVersionForContent/FarmersStructures-1.0.6-1.21.1_neoforge.jar` (1.21.1 / NeoForge), method in
[devmods_evaluation.md](devmods_evaluation.md) section 1. Both jars unpacked in `DecompiledMods/`. Plan row:
`largecontent/mod-additions #27` in [todo/TODO.md](todo/TODO.md). How the pack runs these structures today:
[structure_tuning.md](structure_tuning.md) (group `remote`, `meta/farmers_camp`), [biome_fits.md](biome_fits.md).

**Licence:** All Rights Reserved (owner-supplied 2026-10-25; both jars agree). The templates are the content, so carrying any of them over means
copying ARR files into the pack (see open question 1).

**Result in one line:** 11 new structure sets (6 are for dimension mods the pack does not have), the carp pond replaced
by a cod pond, the cook house pool grown from 33 to 78 templates. No item, no lang, no code. Almost every new template is
a 1.21 save, so its chest, item frame and cooking pot contents load empty on 1.19.2 unless converted.

## 1. How it was read

- `python3 tools/jar_diff.py` (152 files added, 57 removed, 20 changed; no lang). 50 of the removed files are the
  1.19.2 `structures/` folder, which 1.21 renamed to `structure/`: the templates themselves were compared by name.
- Templates read with `tools/audit_loot_containers.py`'s NBT reader (palette, block entities, entities, loot tables,
  DataVersion). Block ids checked against `kubejs/exported/items_by_mod`, the block tag dump and the blocks 1.0.0's
  templates already place. Biome tags resolved with `gen_structure_groups.resolve_biome_tag` against the pack's
  Overworld biomes. Scripts in the session scratchpad, not kept.
- Structure, structure set, pool and processor jsons diffed with `json.tool --sort-keys`.

## 2. Changes and proposed decisions

### 2.1 New structures for the Overworld

| Structure | Biomes here (live count) | What it is | Proposed |
|---|---|---|---|
| `aquacultures_cod` | plains, savanna, forests (19) | the carp pond rebuilt with 7 vanilla cod and a chicken; same salt as the removed `aquacultures_carp` | **carry over in place of the carp pond.** The installed carp pond's 9 `aquaculture:carp` and its fish mount need Aquaculture (not installed), so today the pond spawns with no fish |
| `cacaos` | jungles (8) | jungle cocoa farm, 28 x 48 x 21, with a basement piece from its own pool; 3 cows | owner's call; fits `land/jungle` |
| `fishs_stew` | snowy biomes (22) | snowy fishing hut with salmon | owner's call |
| `ratatouilles` | swamps (4) | stone brick cellar kitchen, starts 55 blocks below the surface | owner's call; buried, so needs the underground step in the group sim |
| `squids` | taigas, dark forest (14, 2 of them Terralith cave biomes) | stone brick squid tank, 40 blocks below the surface, 9 squid | owner's call; the start is checked at depth, so cave biomes decide where it can start |
| `shepherd_farms` | the cook house biomes (10) | large sheep farm, 38 x 26 x 30, shepherd chest; upstream spacing 141 / 121 (rare) | owner's call |

All six would join group `remote` beside the existing Farmer's members (cooks, cooks_moss, three ponds, sushi stall)
and the `meta/farmers_camp` anchor tag. The group keeps its spacing, so new members take slots from the old ones.

### 2.2 New structures for other dimensions

`afterdark_farms` (The Afterdark), `deeper_farms` (Deeper and Darker), `flesh_farms` (`ihfr_fo`), `honey_cake_farms`
(The Bumblezone), `undergardens` (The Undergarden): none of those mods is installed, every biome entry is optional, so
they never spawn. **Skip.** Same reason the pack already empties `aethers_with_all` and `undergardens_sea_with_all`.
(`undergardens_with_all`'s salt 9946924945 is also past the int range.)

### 2.3 Cook houses (existing `cooks` structure)

| Change | Proposed |
|---|---|
| Pools grow: start 4 -> 7, additions 5 / 4 / 5 / 5 -> 7 / 10 / 9 / 14, ends 10 -> 31 templates (45 new). 3 old templates changed slightly (`cook_additions_2_1`, `4_4`, `cook_end_1_2`), 33 unchanged | owner's call; the most visible gain (more varied farmer houses), but the largest conversion job |
| `random_crates` processor: moss -> dirt 30%; obsidian placeholders become cobblestone or iron / gold / copper / lapis / diamond ore (a small mine in a new template) | carry over with the new templates (they rely on it) |
| Cherry Grove houses use a new `moss_random_crates` processor | skip: the pack has no Cherry Grove, so `cooks_moss` never spawns here (it still holds weight 100 in `remote`, see 2.6) |

### 2.4 1.21 format: what a carry-over must convert

83 of the 91 Overworld templates are 1.21 saves (DataVersion 3953 / 3955); 8 are 1.20.1 saves (3465), the same format
as everything 1.0.0 ships. `tools/port_structure_jars.py` refuses DataVersion 3837 and up.

- **Items:** 394 stacks use the 1.20.5 format (lowercase `count`), which 1.19.2 reads as empty: chests with fixed
  contents (119), item frames (118), cooking pots (55), furnaces, barrels, cutting boards, baskets, hoppers, a skillet.
  Converting means `count` -> `Count` (byte); only 3 components occur (`repair_cost`, `enchantments`,
  `stored_enchantments`, all on one enchanted book). Chests with a `LootTable` are unaffected (village butcher 11,
  plains house 4, fisher 4, shepherd 1).
- **Entities:** drop the `attributes`, `variant` and `neoforge:*` keys (defaults apply).
- **Blocks with no 1.19.2 equivalent** (lands as air), with the remap that fits the script's existing table:

| Block | Count | Remap |
|---|---|---|
| `farmersdelight:rope_fence` / `rope_fence_gate` | 158 / 6 | target of the planned FD rope fence ([farmers_delight.md](farmers_delight.md), "Rope Fence and Gate"); until then oak fence |
| `minecraft:pink_petals` | 76 | dropped (already in the script) |
| `minecraft:short_grass` | 31 | `minecraft:grass` (already in the script) |
| `minecraft:cherry_leaves`, `cherry_sapling` | 26 | `vinery:cherry_*` (already in the script) |
| `minecraft:tuff_brick_slab` | 9 | `quark:tuff_bricks_slab` |
| `minecraft:bamboo_block`, `stripped_bamboo_block` | 7 / 11 | `quark:bamboo_block`, `quark:stripped_bamboo_block` |
| `farmersdelight:wooden_basket`, `bamboo_basket` (FD 1.3 names) | 11 | `farmersdelight:basket` |
| `minecraft:decorated_pot`, `suspicious_sand` | 3 / 3 | urn, sand (already in the script) |
| `farmersdelight:hanging_canvas_sign` | 1 | lantern, like the mangrove hanging sign rule |
| `farmersdelight:gleaming_salad_block` | 1 | the FD Gleaming Salad if it is added, else air |

Delivery: extend `port_structure_jars.py` with an item / entity downgrade for DataVersion 3837+ and the remaps above,
then the usual chain (`gen_structure_config.py`, tags, `gen_structure_groups.py`), as in
[structure_tuning.md](structure_tuning.md). The installed jar stays; new files go under `kubejs/data/farmers_structures/`
and override the jar's pools by id.

### 2.5 Values and other

| Change | Proposed |
|---|---|
| Structure set spacing: sushi 35 / 20 -> 54 / 24, `cooks_nether_2` separation 22 -> 28, cod pond 78 / 38 (carp was 48 / 31) | skip: the pack sets every rate through groups and standalone entries |
| `liquid_settings: ignore_waterlogging` on cooks and cooks_moss (1.20.6 field) | skip (1.19.2 has no such field) |
| Biome tags gain `#c:` entries; `is_undergarden_sea_spawn` narrowed | skip (no `c:` tags on 1.19.2 Forge; undergarden emptied) |
| Unused `less_chest` processor (removes 30% of chests) | skip (no pool uses it) |

### 2.6 Found on the installed version

- The carp pond spawns with no fish (2.1). Fixed by the cod pond carry-over, or by a template edit alone.
- 1.0.0's templates are 1.20.1 saves (DataVersion 3465) and already lose 41 Pink Petals, 6 Stripped Bamboo Blocks and 4
  Aquaculture blocks (worm farm, tackle box) to air. The remaps in 2.4 would fix those too if 1.0.0's templates go
  through the script.
- `cooks_moss` lists only `minecraft:cherry_grove`, which 1.19.2 does not have: it never spawns, but has weight 100 in
  `remote`. Harmless (a slot it would win falls through to the other members), but it could be emptied like the
  Aether set, or given Cherry Grove-like biomes (owner's call).

## 3. Owner decisions (2026-10-25)

1. Licence: the jar is a donor, like Vinery. Nothing is copied into `kubejs/data`; the unmodified jar sits in
   `farmers_upstream/` and is converted in memory (section 5).
2. Cod pond replaces the carp pond: yes. Pike and perch (Upgrade Aquatic) may take cod places, and the stew hut's salmon places.
3. Add cacaos, fish stews, ratatouilles, squids and shepherd farms, all in group `remote`.
4. The 45 new cook house pieces: yes, through the same conversion.
5. Rope fence: the pack's FD rope fence and gate. Pink petals -> Immersive Weathering azalea flowers, cherry -> Vinery cherry,
   bamboo blocks -> Quark bamboo blocks, wooden / bamboo basket -> the FD basket, Gleaming Salad -> the pack's feast block,
   hanging canvas sign -> the new `farmersdelight:hanging_canvas_sign` ([farmers_delight.md](farmers_delight.md)).

## 4. Tests

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-FSD-07, -08; FAIL SQ-FSD-02: without `farmers_upstream` the world does not load: 6 unbound `farmers_structures:` structures.

Section "Farmer's Structures donor" of [testing/scratch_queue.md](testing/scratch_queue.md) (SQ-FSD-01 to 08). 2026-10-26 scratch run: SQ-FSD-01, -03, -04 (placement) and -06 PASSED (donor 177 files, all 7 kinds place, carp not locatable, squids at 552 blocks). SQ-FSD-05 and -08 found that 4 cook house expansion connectors and the back-connectors name `minecraft:cook_*_pool` (no namespace): `Empty or non-existent pool`, those 4 never expand (same upstream on 1.21); TODO `largecontent/mod-additions #30` (owner decision). r1102b 2026-11-02 (`perf/runs/r1102b/s3/judged.md`, deep scan): SQ-FSD-07 (sets in the dump) and -08 (hanging canvas sign) PASS. **SQ-FSD-02 FAIL:** with the donor jar renamed the one warning is logged, but the world does not load (`Unbound values in registry worldgen/structure: [farmers_structures:aquacultures_cod, cacaos, fishs_stew, ratatouilles, shepherd_farms, squids]`): something outside the donor pack still names the six 1.0.6-only structures (candidates: `structure_tags.json`, `structure_groups.json`, `structure_config.json` and the patcherMod readers), so the "runs on 1.0.0 data" fallback does not hold. The test world was made with the donor: confirm on a fresh world before fixing. Still open: that, and the client checks (chest / frame / pot contents, cook variety): CQ-FSD-01 (deep scan 2026-11-02).
Needs `farmers_upstream/` in the game folder (and on the server) and the Creative Delight jar for the sign; both jars shipped in 1.1.03.

## 5. How it is built

- **Reader:** patcherMod `worldgen/donor/` ([FarmersDonor](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/donor/FarmersDonor.java),
  [FarmersTranslator](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/donor/FarmersTranslator.java),
  `UpstreamJars` pins the sha1). A built-in server data pack `farmers_structures_donor` at the top of the data packs (below
  KubeJS) over the installed 1.0.0 mod pack. A missing or different jar adds nothing and logs one warning.
- **What it converts:** `structure/` -> `structures/`, `tags/block` -> `tags/blocks`; templates: item `count` -> `Count`
  plus the 4 components 1.19.2 has, blocks via the table in 2.4 (unknown ones become air), entities of absent mods and 1.21
  keys stripped, unknown items dropped; `liquid_settings` removed from structures. Foreign-dimension structures, sets,
  pools and templates (Aether, Undergarden, Afterdark, Deeper and Darker, flesh, Bumblezone) are not served.
- **Fish:** the cod pond and the stew hut pools get 3 extra pieces each: every second fish a pike, a perch, or alternating
  pike / perch (1 in 4 ponds keeps cod only). Needs Upgrade Aquatic.
- **Carp:** the donor serves an empty `aquacultures_carp_with_all`, and the pack tags no longer name the carp pond.
- **Pack data:** land fits in `structure_tags.json` (cacaos `land/jungle`, fishs_stew `land/snowy`, ratatouilles `land/swamp`,
  squids `land/taiga` + `land/dark_forest`, shepherd farms and the cod pond with the old cook / carp fits), the first four plus
  shepherds in `meta/farmers_structures`, ratatouilles and squids in `type/underground` and exempt from earth fill.
  `gen_structure_config.py` reads the donor jar (its sets and structures replace the installed jar's, foreign ones excluded);
  `gen_structure_groups.py` fitted the new members into `remote`.
- **The underground step in the group sim:** the sim and the fitter judge a structure only by the surface biomes of its fit
  tag (`surface_mean` counts `layer: surface` biomes). Ratatouilles start at surface -55 and squids at -40, and a jigsaw start
  is biome-checked where the start y lies, not at the surface. At those depths the biome is nearly always the surface biome
  continuing down (cave biomes only in pockets, Terralith's deep ones start lower, hence `land/any/deep` for -50 to -30
  absolute), so they use the surface fits and their rates are right to within the cave pockets. A real underground step
  would sample the 3D biome at the start y and use `land/any/deep`-like cave fits for starts below about y 0; it is not
  built, since these two don't need it. They are `type/underground` so the sea check lets them sit under the seabed.
