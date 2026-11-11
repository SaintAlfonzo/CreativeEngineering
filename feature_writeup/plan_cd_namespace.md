# Plan: Creative Delight content moves to the `creativedelight` namespace (2026-11-09)

Status: BUILT 2026-11-10 (steps 1 to 7 and 9; step 8 needs `/cdevgen` and the client dumps). Scratch r1110: SQ-CDNS-01 to -04, -06 to -11 PASS, -05 recorded; SQ-CDNS-12 and CQ-CDNS-01 (client) open. Jars in `jar_archive/cdns_full/`, not installed (owner decides). TODO rows `creativesplit/ids #24` (this move) and `#25` (the other jars).
Owner request after the wooden bucket moved to `creativedelight:wooden_*` (TODO `largecontent/mod-additions #58`, the
first id moved this way).

## 0. The rule (owner, 2026-11-09)

**Content is registered under the id of the mod it belongs to.** This replaces rule 1 of
[creative_split.md](creative_split.md) section 3 ("ids stay on `creativeengineering` whichever jar registers them").
Registered content, tags and data ids all follow the owning jar. Saves carry over through the swap lists, and the
swapper (section 3) is extended so that no item, block or block data is lost when an id moves. This plan does Creative
Delight; the other jars (Equipment, Magic, Worldgen, Compat and the rest that still register `creativeengineering:`
content they own) follow the same method under `creativesplit/ids #25`, once this one has passed its tests.

## 1. Scope (inventory 2026-11-09)

From the code, not the dumps. "Conditional" ids register only when their source mod or CD compartment is on; counts
are the maximum.

### 1.1 Registered content, Java (`src/main/java/com/SaintAlfonzo/creativedelight/`)

| Source | Registry | Ids |
|---|---|---|
| `oven/OvenRegistry` | block, item, block entity, menu | `oven` (one of each) |
| `oven/OvenRegistry` | recipe serializer, recipe type | `baking` |
| `food/PowderedSugarRegistry` | block, item | `powdered_sugar_block`, `powdered_sugar_bucket` |
| `compat/DisplayDelightDishBlocks` (conditional: Display Delight) | block, no item | 11 `placed_*` from `resources/creativeengineering/displaydelight_dishes.json` |
| `lunchbox/LunchboxRecipes` | recipe serializer | `lunchbox_shaped`, `lunchbox_upgrade`, `lunchbox_skin`, `lunchbox_dye`, `lunchbox_soap` |
| `food/SeasoningRecipe` | recipe serializer | `seasoning` |
| `loot/BiomeTagCondition` | loot condition type (vanilla registry) | `biome_tag` |
| `compat/KubeJSCanAction` | KubeJS ingredient action (not a registry) | `can_serving` |
| `cooking/RemainderBufferStorage` | capability id, saved in block entity NBT | `remainder_buffer` |
| `compat/cookingforblockheads/kitchen/CornerStorage` | capability id, saved in block entity NBT | `corner_storage` |

Already outside the namespace: TreatedSticks, the CfB racks and the Incubation nests (`creativedelight`), the lunchbox
items (`solapplepie`), `VillageFarmCrops` (`creative_delight`, with its own load alias), every member's registries (their
source mod's namespace), all CD fluids (`creativedelight`, via `fluid_dict.js`).

### 1.2 Registered content, KubeJS startup scripts in the jar (`src/main/resources/kubejs_startup/`)

Ids come from `global.item` keys in Creative Common `_lib/item_dict.js`, except the syringes (literal ids).

| Script | Items | Blocks (each with a block item) |
|---|---|---|
| `creativedelight/cans.js` (fed by the generated `<mod>/can_types.js`; conditional per mod) | 313 `canned_<food>` | |
| `creativedelight/drinks.js` | 14 teas and juices | |
| `creativedelight/cultures.js` | 7 `culture_*` | 7 `*_culture_block` |
| `creativedelight/sample_syringe_items.js` (literal ids) | 8 `*sample_syringe` | |
| `creativedelight/baking_ingredients.js` | `frosting`, `cookie_dough`, `powdered_sugar` | 6 dough blocks |
| `creativedelight/bakeware.js` | `baking_sheet`, `cake_pan`, `pie_tin`, `muffin_tin`, `mortar_and_pestle` | |
| `creativedelight/cornmeal.js` | `cornmeal` | |
| `corn_delight/corn_items.js` | `corncob`, `corn_popsicle`, `cob_pipe` | |
| `rusticdelight/glowing_calamari.js` | `glowing_calamari`, `glowing_calamari_slice`, `glowing_calamari_roll` | |
| `vintagedelight/vintage_items.js`, `veggiesdelight/veggies_items.js`, `moredelight/toast.js`, `farmersdelight/ocean_dishes.js` | `pickled_sea_pickle`, `sliced_sweet_potato`, `cheese_toast`, `kelp_encrusted_cod` | |

The 34 Canned Goods cans marked `existing` in `can_types` are Canned Goods' own items and do not move.

### 1.3 Tags and data ids CD owns

Not saved in worlds, so they need no swap entries, but every reference must move in the same change.

- **Tags.** `item_dict.js` and its siblings hold 140 `#creativeengineering:` tag keys shared by every jar. The ones CD
  owns include `food_type/*` (35 keys), `smokable/*`, `jar_family/*`, `plantable/*`, `bakeware/*`, `sweeteners/*`,
  `food_family/*`, `field_salad/*`, `drink_fruits/*`, `cans/*` and `cans/servings/<n>` (`CanServings.NAMESPACE`),
  `placeable_on_coaster`, `soup_veggie_cooked`, `veggie_noodle_veg`, `whole_raw_fish`, `spices`. Owner of each tag =
  the jar whose scripts or data fill it. Step 1 classifies all 140 and lists the ones that move; shared ones (filled by
  two jars) stay with the jar that defines what the tag means, and the other adds to it under the new name.
- **Data ids.** Loot tables `creativeengineering:fishing_net/*` (used with `biome_tag`), the 13 block loot tables, the
  59 Supplementaries soft fluids, and any CD recipe id still under `creativeengineering` (the CD scripts already write
  `creativedelight:<type>/<suffix>`; step 1 confirms none are left).
- **Lang keys.** `block.creativeengineering.placed_*` in `assets/creativedelight/lang/en_us.json`, and any
  `item.creativeengineering.*` / `block.creativeengineering.*` key for a moved id in the KubeJS lang or name scripts.

### 1.4 Totals

376 items, 26 blocks, 1 block entity, 1 menu, 7 recipe serializers, 1 recipe type, 1 loot condition, 1 KubeJS action,
2 capability ids; plus the tag and data ids that step 1 counts.

### 1.5 Files that carry the ids

- CD jar resources: `assets/creativeengineering/` (26 blockstates, 57 models, 90 textures),
  `data/creativeengineering/loot_tables/` and `moonlight/soft_fluids/`, `resources/creativeengineering/displaydelight_dishes.json`,
  the lang file.
- `item_dict.js`, `block_dict.js` and siblings (619 `creativeengineering:` values in `item_dict.js`; only CD-owned
  ones change).
- Scripts: `kubejs/data/creativedelight/**`, `kubejs/assets/creativedelight/**`, and pack scripts elsewhere that name
  a CD id or tag directly.
- Generated data keyed by id: `kubejs/generation_scripts/food/*`, `physics/`, `stacks/`, the `can_types.js` files, and
  the generators that write them (`tools/food_dict.py`, `food_catalog.py`, `food_families.py`, `can_audit.py`,
  `gen_comfort_nourishment.py` and any others the grep finds).
- Other jars: Creative Common (3 source files name CD ids), Compat; `config/` (one FTB Quests file and any others).
- Probes and tests: `tools/kitchen_probe/`, `tools/remainder_buffer_test/`, `tools/changelog_claims_probe.js`.

## 2. Decisions

**D1. The rule** (section 0): decided by the owner 2026-11-09.

**D2. Capability ids** `remainder_buffer` and `corner_storage` move to `creativedelight:`. The swapper copies their
saved block data from the old key to the new one (section 3, `capabilities`). Decided by the owner 2026-11-09.

**D3. Tags** move with the content in this change (section 1.3). Decided by the owner 2026-11-09.

**D4. Recipe types, serializers, the loot condition and the KubeJS action** are renamed to `creativedelight:` with
**no aliases**: every in-pack reference (recipe JSON `type`, KubeJS recipes, loot tables, the `can_serving` action
name) moves in the same change. Decided by the owner 2026-11-09. Consequence: an external datapack naming the old ids
stops loading those recipes or loot entries; step 7 greps the world `datapacks/` and `kubejs/` folders for old ids so
nothing in the pack itself is left behind.

**D5. Smokable and plantable tags** go to `creativecommon:` (owner 2026-11-10), their fillers to
`kubejs/data/creativecommon/kubejs/tags/`. Section 1.3 listed them as Creative Delight's, but neither is filled by it.

**D6. Seasoning ids move to `creativedelight:` with no swapper (owner, 2026-11-10).** `creativeengineering:cheese`, `onion`,
`garlic`, `sugar`, `void_pepper` are stored as text in food NBT (`Seasonings`). The owner does not want an NBT alias:
food seasoned before the move loses its seasoning. Work and tests: [namespace_move.md](namespace_move.md) section 1.

### Found while building (2026-11-10)

- Forge's snapshot load reads registered names only (`ForgeRegistry.getIDRaw`), so permanent aliases do not hide a missing
  id. Forge rebuilds ACTIVE's aliases on every freeze and injection and fires `IdMappingEvent` after each; `SwapRemapper`
  re-adds them there. Block entity types are in no snapshot, so this is the alias that keeps the oven's block entity.
- `NCH.modId` stays `creativeengineering` for Engineering's types; the seasoning, lunchbox and `can_serving` types it
  composed are literals now.
- The probe is `tools/scratch_query/cdns_probe.py`, offline on the stopped save (KubeJS cannot open `data/*.dat`).
- SQ-CDNS-03 needs the full build: the old Delight jar attaches capabilities under the old key.
- A mixin on `CapabilityProvider` (superclass of `Level`, `Entity`, `ItemStack`) crashed the server with `IncompatibleClassChangeError` in `Level` on the first item tick; the capability key rename hooks the final `CapabilityDispatcher` instead.
- `/give` and other typed commands resolve items through vanilla holders and ignore Forge aliases (SQ-CDNS-02, r1110).
- Forge fires the freeze's `IdMappingEvent` before its event bus starts, so a fresh world got no aliases; `SwapRemapper` also installs them on `ServerAboutToStartEvent`.
- A `merges` array names live items, not history: a purged id that moved must be rewritten there (`canned_hamimelon`, SQ-CDNS-07). `replaces` arrays are the history and keep the old ids.
- The JEI seasoning and can page filters compared recipe ids with the old namespace and never matched; they match now.

## 3. Swapper changes (Creative Common `swap/`)

What the swapper does today: `replaces` entries remap missing registry ids on a world load (`SwapRemapper`, Forge
`MissingMappingsEvent`; Forge then aliases old to new for that session, so chunk palettes, inventories, block entities
and dropped stacks load under the new id); `removed` ignores ids; `merges` rewrite stacks of two live items in the
world (`ItemMerges`).

Additions, so that the id move loses nothing:

1. **Permanent aliases for `replaces`.** Today the old-to-new alias exists only when a world's registry snapshot still
   names the old id. Stacks that come from anywhere else (player files of another world, structure NBT,
   item NBT in configs, a server's registry sync to a client; typed commands such as `/give` do not read aliases and need the new id) would not resolve. After registries freeze, the swapper
   adds every `replaces` pair whose survivor is registered as a Forge alias in the ACTIVE registry, with the existing
   cycle guard (`dropAliasFromSurvivor`). This applies only to the swap lists' own renames, not to D4's data ids.
2. **`capabilities` section.** A swap file may list
   `"capabilities": { "creativedelight:corner_storage": ["creativeengineering:corner_storage"] }`. A mixin on
   `CapabilityProvider.deserializeCaps` (base of block entities, entities, item stacks, chunks) renames each old key in
   the `ForgeCaps` tag to its new key before Forge hands the tag to the providers, unless the new key is already
   there. The corner storage and remainder buffer contents load under the new id and save under it from then on.
3. **Load report.** On every world load and every chunk load in the first 10 minutes, count stacks and blocks still
   naming an old id after aliasing (`ItemStack.of` returning air for an id the lists name, a palette entry that resolves
   to air). Log one `[swaps] unresolved` line per id with a count. Zero is the pass condition in the tests; any count
   is item loss to fix before release.
4. **Generated swap file.** `data/creativedelight/swaps/cd_namespace.json`, written by step 1 (about 410 `replaces`
   entries and 2 `capabilities` entries), never by hand. Like every swap list it is never pruned.
5. **What the swapper cannot reach** (stated in the changelog): player-side files outside the world (JEI bookmarks,
   client configs naming an id) and stats keyed by item id.

## 4. Steps

1. **Id list tool.** `tools/cd_namespace_ids.py` builds the moved-id list from the code (Java register calls,
   `displaydelight_dishes.json`, the startup scripts' dict keys, every `can_types.js` row not marked `existing`, the
   syringe literals), classifies the 140 tag keys by owner (section 1.3), finds CD's data ids, asserts the 1.4 counts,
   checks that no new id clashes with an existing `creativedelight:` id (the 65 items in
   `kubejs/exported/items_by_mod/creativedelight.json`, plus blocks, fluids, tags), and writes the swap file plus a
   plain list for step 5.
2. **Swapper** (section 3): permanent aliases, `capabilities`, the load report; then build Creative Common and run the
   section 5 swapper tests on the wooden bucket swap alone (the smallest real swap, already built) before the large one.
3. **Java.** Every CD `DeferredRegister` on `CreativeDelight.NS` switches to `MOD_ID`; D4 ids renamed outright; the
   capability ids renamed; literal `creativeengineering` ResourceLocations for moved ids (`CornDelightCompat.COB_PIPE`,
   `CanServings.NAMESPACE`) changed. `NS` is then removed.
4. **Startup scripts and dicts.** Dict values for moved items, blocks and tags to `creativedelight:`; the syringe
   literals; the can generator's namespace, then regenerate the `can_types.js` files.
5. **Rewrite references.** `tools/cd_namespace_move.py` (one-off, dry run first, per-file counts) replaces
   `creativeengineering:<id>` and `#creativeengineering:<tag>` with `creativedelight:` for exactly the step 1 list,
   over an allow-list: CD `src` and members, `kubejs/data`, `kubejs/assets`, `kubejs/server_scripts`,
   `kubejs/client_scripts`, `kubejs/startup_scripts`, `kubejs/generation_scripts`, `config/`, `tools/`, Common and
   Compat sources. Never `kubejs/exported/` (regenerated), released changelog files, `jar_archive/`,
   `moved_code_backup/`, writeup history lines. Hand-edited configs are copied to `configs_edited/`.
6. **Resources.** Move `assets/creativeengineering/{blockstates,models,textures}` and
   `data/creativeengineering/{loot_tables,moonlight}` in the CD jar to `creativedelight`, rewriting model and
   blockstate texture paths and soft fluid item ids; rename the lang keys; move `displaydelight_dishes.json` with its
   loader path. Clean-build CD (memory `repack-stale-resources`).
7. **Build and lint.** `:creativeDelight:`, `:creativeCommon:`, `:creativeCompat:` builds; `tools/id_owner_lint.py`
   gains a check that no CD register call, tag or data id uses `creativeengineering`; grep the scratch world's
   `datapacks/` and the pack for old D4 ids; `python3 tools/changelog_lint.py`.
8. **Regenerate.** In game: `/dumpall`, then `/cdevgen`; confirm `kubejs/exported/items_by_mod/creativeengineering.json`
   and the tag dumps no longer list any moved id.
9. **Docs.** creative_split.md 3 rule 1 rewritten to section 0's rule; plan_owner_ids.md section 4 (capability ids
   no longer kept); creative_delight.md; oven.md, cans.md and the other writeups that cite ids.

## 5. Scratch tests (rows to add to scratch_queue.md at build time)

Two kinds: the swapper itself (run first on the wooden bucket swap, then on the full move), and the move. The swapper
tests need a **fixture save made on the old jars**: a scratch server running the currently installed jars builds it
from a `.qb` batch, the save is copied before any new jar touches it (a save opened once without the swap list loses
the old ids from its snapshot for good: memory `forge-missing-mappings-staging`), and every test boots its own copy.

### 5.1 Fixture (old jars)

A batch `tools/scratch_query/batches/<run>/cdns_fixture.qb` places and fills, in one forceloaded area, with at least
one moved item of each kind (a can, a culture, a syringe, the oven item, a dough block item, cornmeal, a wooden water
bucket for the bucket swap) in each holder:

- Blocks: every moved block placed (oven with input, output and fuel, culture blocks, dough blocks, powdered sugar
  block, Display Delight dishes when present).
- Vanilla holders: chest, barrel, shulker box (and a shulker box item inside a chest), hopper, dropper, furnace,
  brewing stand, ender chest, item frame, armor stand, dropped item entity, chest minecart, a mob holding one, a
  villager inventory.
- Player: inventory, armor and offhand, an offline player's `.dat` (second fake player logged out), Curios slots.
- Pack holders: a backpack (item and placed), a Sophisticated Storage chest, a Create toolbox, depot and belt item, an
  AE2 drive cell and an ME chest, a Lootr chest opened by one player, a Lightman's Currency trader with a moved item
  for sale, a CfB corner with contents (capability data), a remainder buffer holding remainders (capability data).
- A probe script `tools/scratch_query/cdns_probe.js` (server, run by command) writes
  `cdns_probe.json`: per holder, per id, the count found. The fixture run stores the old-jar probe as the baseline.

### 5.2 Rows

| ID | Kind | What | Pass |
|---|---|---|---|
| SQ-CDNS-01 | swapper | Boot a fixture copy holding the wooden bucket swap with the new Creative Common only (step 2) | `[swaps]` remaps `wooden_bucket` and `wooden_water_bucket`; probe counts equal the baseline under the new ids; `[swaps] unresolved` count 0 |
| SQ-CDNS-02 | swapper | Same boot: a `/data` merge naming the old id | Reads back `creativedelight:wooden_bucket` (permanent alias); `/give` with the old id is out of scope (3.1) |
| SQ-CDNS-03 | swapper | Fixture copy with a CfB corner and a remainder buffer, `capabilities` entries on | Contents present after load; after `/save-all` the block entity NBT holds only the new key; a second boot still has them |
| SQ-CDNS-04 | swapper | Second and third boot of the same copy | No `[swaps]` remap lines (already saved under new ids); probe unchanged; no `unresolved` |
| SQ-CDNS-05 | swapper | Reverse check: a copy saved with the new ids, booted with the old jars (rollback) | RECORD what is lost; the release notes must say a rollback is not supported if anything is |
| SQ-CDNS-06 | swapper | Swap list with a survivor that is not registered (a can of a removed mod) | Logged `left missing`, boot continues, no crash |
| SQ-CDNS-07 | move | Fresh world, new jars: grep `logs/latest.log` and `logs/kubejs/*.log` for errors and for any moved id or tag under `creativeengineering` | Clean boot; nothing found |
| SQ-CDNS-08 | move | `/kubejs dump_registry` for items, blocks, block entity types, menus, recipe serializers and types; tag dump | Every step 1 id and tag is under `creativedelight`; counts match 1.4 for the mods present |
| SQ-CDNS-09 | move | Boot the full fixture copy with every new jar | Probe counts equal the baseline holder by holder; `unresolved` 0; every moved block still in place (probe block census) |
| SQ-CDNS-10 | move | Recipes and loot: oven baking, seasoning, lunchbox recipes, a KubeJS recipe with `can_serving`, fishing net loot with `biome_tag`, block loot of each moved block, tag-driven recipes (`food_type`, `bakeware`, `cans/servings`) | All load and work; no recipe output or input is air (`/kubejs dump_recipes` grep for `minecraft:air` in CD recipes) |
| SQ-CDNS-11 | move | Supplementaries soft fluids, Display Delight dish placement, AppleSkin and food-type tooltips via the probe | Jars and cans pour; dishes place; food types resolve |
| SQ-CDNS-12 | move | Dedicated server with the new jars, a client with the new jars joins; then a client with the old CD jar tries to join | New client joins and sees the items; old client is refused with the mod mismatch message, no crash |
| CQ-CDNS-01 | client | Textures and models of the moved items and blocks, names, JEI mod name, the CfB corner and oven GUIs on the fixture copy | No purple-black; names unchanged; JEI shows Creative Delight; GUIs show the old contents |

## 6. Changelog (when built)

Catch-all bullets: Creative Delight items and blocks are listed under Creative Delight [Creative Engineering]; existing
worlds keep them, including stored kitchen corner contents. Player files outside the world (JEI bookmarks and similar)
lose the moved items. Datapacks that use the old Creative Delight recipe types need updating.

## 7. Risks

- A missed reference turns a recipe, loot entry or tag member into air silently: step 5's dry run, SQ-CDNS-07, 08 and
  10 and the `items_by_mod` check catch most of it.
- The swap list is large (about 410 entries): generated by step 1, never by hand.
- Rollback is not supported: SQ-CDNS-05 (r1110, a save last written with the wooden bucket swap, booted on the older jars) lost every moved stack, with no crash. The full move is expected to lose all 388 ids the same way.
- A mod that stores item ids as plain strings outside `ItemStack` NBT and compares them literally would not follow the
  alias: the fixture covers the pack's storage mods, and the load report names anything left.
- The server must take the new CD and Common jars in the same deploy as the clients; the swap file ships inside the CD
  jar and the swapper inside Common.
