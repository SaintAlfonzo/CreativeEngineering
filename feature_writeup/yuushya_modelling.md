# Yuushya Modelling 2.4.1: jar upgrade and backport report

Written 2026-11-25 as an investigation; phase A of the backport is built since then ([plan_yuushya_backport.md](plan_yuushya_backport.md),
status in `mod-integration #29`; the fork replaces the 2.2.0 jar). Before it: `yuushya-modelling-1.19.2-forge-2.2.0.jar` (mod id
`yuushya_modelling`, item namespace `yuushya`, in `mods/` and `server_mods/`). Newer jar:
`DecompiledMods/yuushya_modelling-1.20.1-2.4.1.jar` (Forge 47, 1.20.1, built 2026-04-23); a NeoForge 1.21.1 build of
2.4.1 sits in the launcher downloads folder and was read for comparison only. License MIT (both jars). Modrinth lists
no 1.19.2 build after 2.2.0 (2024-09-20), and the public source (GitHub `XiLaiTL/Yuushya-Modelling`, branch `master`)
stops at 2.2.0, so the 2.4 line is jar-only. A **backport to Forge 1.19.2 is needed**: MIT allows a fork, route
`development/backportedYuushyaModelling` (skill `port`), upstream MIT notice kept, our changes GPL-3.0-only. Short
names in this file: **2.2** (installed) and **2.4** (new). Test prefix `YMB-`.

Scope rule: the backport carries what 2.4 has, adds no recipe or mechanic that is not in the mod or defined by a pack
rule unless the owner asks by name, and fixes upstream bugs.

## 0. How it was read

<!-- generated:status_index begin open_work sha1=facaaaf1ecc7 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 2 open TODO rows (Planned 2), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `mod-integration #29` | Planned | S C P | Yuushya Modelling 2.4.1 backport phase A: Entity Block options, Item Block, primitives, Color Picker, History List, bugs B1 to B4 |
| `mod-integration #36` | Planned | S C P | Yuushya Modelling 2.4.1 backport phase B: Text Block (font path rewrite for 1.19.2) |
<!-- generated:status_index end open_work -->

- `python3 tools/jar_diff.py mods/yuushya-modelling-1.19.2-forge-2.2.0.jar DecompiledMods/yuushya_modelling-1.20.1-2.4.1.jar`:
  253 -> 438 files; 247 added, 62 removed, 61 changed; lang en_us +97 keys, 3 changed.
- `python3 tools/decompile_mod.py` on both jars into the scratchpad: 59 Java files (5.3k lines) in 2.2, 133 (15.2k
  lines) in 2.4. Registries, block properties, NBT keys, mixins, access transformer and screens read from that.
- Data counted by path and recipe `type`. Version history from the Modrinth API; source tree from GitHub.
- Pack state: grep for `yuushya` in `kubejs/`, `development/*/src`, the dicts and `feature_writeup/`. The only script
  reference is the `@yuushya` decorative BlockRunner match in `kubejs/server_scripts/tags/blocktags.js`; `has.js`
  maps the namespace to the mod id. No item_dict entries, no recipe edits. Docs that list Yuushya items:
  [names.md](names.md) (2 title-case fixes), [stack_sizes.md](stack_sizes.md), [item_physics.md](item_physics.md)
  (14 items in family tags), [toasts.md](toasts.md) (Entity Block screen toast), [unused_textures.md](unused_textures.md),
  [recipes_metal.md](recipes_metal.md) (Shimmering Pearl takes the gold tag). Dumps in `kubejs/exported/` are from
  2026-11-17 and know only 2.2.

## 1. Changes

### 1.1 Entity Block (`yuushya:showblock`): new rendering and editing options

The block the pack already has. Its saved data is unchanged: block entity keys `ShowPos`, `ShowRotation`,
`ShowScales`, `BlockState`, `isShown`, `ControlSlot` and item keys `BlockEntityTag` / `TransformData` are the same in
both versions, so existing Entity Blocks load as they are.

| Change | What it does | Pack impact |
|---|---|---|
| Collision shape (`shapes` blockstate, 5 values) | Cycle button in the Entity Block screen: No Collision, Fence (1.5 high post), Bottom Half, Top Half, Block. Default No Collision, which is the 2.2 behavior (`noCollission`). | New player-visible option; old blocks keep no collision. |
| Ambient occlusion (`enable_ao`, `full_block` blockstates) | Per-block AO on/off and "full block AO" (the block counts as a full cube for AO and suffocation). Default off. | Lighting look of builds; old blocks unchanged (defaults off). |
| Fine Tune mode | Third slider mode beside Incremental and Input: no step limit (90.5 degree rotation, 2.05 offset). | Editing option. |
| Face count | Button that counts the rendered faces of the block's layers. | Builder aid for heavy builds. |
| Copy block or blockstate | Button in the layer list copies the selected layer's block or state. | Editing option. |
| Item rendering | A configured Entity Block item draws its layers in hand and inventory (Forge `getRenderPasses`), instead of the plain icon. | Visible change; see B2. |
| Tool tooltips | Destroy item and GUI Terminal also act on Item Blocks. | Lang only. |

Blockstate count goes from 128 (powered x lit x facing) to 2560 (x shapes 5 x AO 2 x full block 2). Loaded blockstates
are cheap in 1.19.2 but each gets a model wrapper at bake (`ModelEvent.ModifyBakingResult` wraps every state); a
startup-time note, not a blocker.

### 1.2 New blocks

| Id | Name | What it is |
|---|---|---|
| `yuushya:itemblock` | Item Block | Like the Entity Block but its layers are item models: per-layer position, rotation, scale, color (RGB picker), brightness, AO and full-block AO, mirror; Incremental, Fine Tune, Input, Color and Custom Size modes; "pre-apply" to all layers. Recipe: quartz + wheat + kelp -> 8. Drops itself. |
| `yuushya:textblock` | Text Block | Renders text lines in the world: font list (every loaded font), color, mirror, cull face, block/item backdrop, per-line transform. Own text shader and glyph render types (mixin on `GlyphRenderTypes`). |

Both keep the Entity Block's tools (Shifter, Rotator, Sizer, Layer-Switcher, GUI Terminal, Destroy item,
Modelling Chisel templates).

### 1.3 New items

| Id | Name | Notes |
|---|---|---|
| `yuushya:color_picker_item` | Color Picker | Right click a block: color to clipboard and stored; left click: stored color to clipboard. Recipe: The Encyclopedia + cauldron. |
| `yuushya:history_item` | History List | Opens a list of model blocks the player broke recently (menu `yuushya:history`); off hand picks Entity or Item Block list. |
| 32 primitives | `cube_0` to `cube_11`, `cylinder_0/1`, `circle_0/1`, `circle_hollow_0/1`, `triangle_0/1`, `chibi_0` to `chibi_6`, `chibi_0_alt`, `anatomy_0` to `anatomy_2`, `anatomy_0_alt` | Model shapes for Item Block layers (cubes, prisms, slab, pillar, cylinders, circles, triangles, chibi figure parts). Stack 16, rare. New creative tab "Yuushya - Modeling Primitive". `cube_0` has a recipe; 31 stonecutting recipes cut the rest from `cube_0`. |

Lang has `item.yuushya.cube_12` (Small Pillar) with no item registered; harmless (B4).

### 1.4 Other code changes

- **Networking**: 8 packets (block, item and text transform data, item stack, text lines, AO update, reload). Forge
  `SimpleChannel` `yuushya_modelling:main` in both versions; both sides need the same jar (unchanged: mod_sides
  `both`).
- **Command** `/yuushya_reload_model reload all|blocks|items|texts`: rebuilds the model blocks' render data.
- **Client mixins** (4): `LevelRendererMixin` and `MinecraftMixin` drive an AnvilCraft-derived cached block entity
  render pipeline (`client/anvilcraft/rendering/`, 5 classes). It is the Item Block's world renderer: the in-world
  path of `NeoItemBlockModel` registers each placed Item Block in `CustomRenderInstance`, and the pipeline draws it
  (corrected 2026-11-25; first read as dormant). `GlyphRenderTypesMixin` serves the Text Block. `BlockBehaviourMixin` makes the Item Block's
  `isCollisionShapeFullBlock` follow `full_block` (B3).
- **Access transformer**: 31 entries, mostly render type, font and glyph internals, several 1.20-only
  (`BakedGlyph.renderTypes`, `Font.filterFishyGlyphs`, the JOML `renderChar`).
- **Removed**: Architectury dependency (2.4 is Forge-only; Architectury stays in the pack for its other users, see
  [mod_sides.md](../mod_sides.md)), the Patchouli guide book and its join advancement (the pack drops advancements
  anyway), the optional `yuushya` content mod dependency.
- **Recipes**: 17 -> 52 (6 shaped, 15 shapeless, 31 stonecutting); the 17 old ids keep their names.
- **Loot**: `blocks/showblock` (was in 2.2) and new `blocks/itemblock` (self drop, survives explosion). No Text Block
  table (it drops via its item code).

### 1.5 Upstream bugs

- **B1. The 1.20.1 jar ships 1.21 data.** Recipes are in `data/yuushya/recipe/` and loot in `data/yuushya/loot_table/`
  (1.21 singular folders; 1.20.1 reads `recipes/` and `loot_tables/`), and results use the 1.21 `{"id": ...}` form.
  On 1.20.1 none of the 52 recipes and neither loot table load (the Entity Block, which had a table in 2.2, would
  drop nothing when mined). Evidence: file paths and JSON in the jar; not run. Fix in the backport: plural folders,
  1.19.2 result form (`"result": {"item": ...}`, stonecutting `"result": "<id>", "count": n`).
- **B2. Unbounded item model cache.** `NeoShowBlockModel` (a `HashMap`) and `NeoItemBlockModel` (a
  `ConcurrentHashMap`) cache a baked wrapper per `ItemStack`. `ItemStack` has no `equals`/`hashCode`, so the key is
  identity: every new stack copy (inventory sync, creative tab, JEI) adds an entry that is never removed. Slow client
  memory growth while configured Entity or Item Block items are on screen. Fix: key by the transform-data tag (or its
  hash), bound the cache, clear it on resource reload. Needs a live check (YMB-07).
- **B3. Full-block flag client-only.** `BlockBehaviourMixin` is in the mixin config's `client` list, so with
  `full_block` on, the client treats the Item Block as a full cube and a dedicated server does not (suffocation,
  `isCollisionShapeFullBlock` users). Plausible desync, from code; fix by moving it to the common list. Live check
  YMB-08.
- **B4.** `item.yuushya.cube_12` lang key without an item. Fix: remove the key (owner 2026-11-25: fix all bugs).
  The 2.2 leftovers `block.yuushya.modelframe` and `block.yuushya.mixedblock` (with their `.line1` keys; no such
  block in either version) went with it.
- **B5. Color handlers never registered.** The block and item color handlers were instance methods in an
  `@EventBusSubscriber` class, which Forge never calls: tinted blocks (grass, leaves) showed untinted inside Entity
  Blocks and their items. Found in A4; fixed with static handlers, as in 2.2.0.
- **B6. Missing lang key.** The Entity Block and Item Block screens show `gui.showBlockScreen.workshop.save_error`
  when saving a work fails; no lang file has it (raw key in the toast). Found in A8; en_us key added.

### 1.6 Backport work (1.20.1 -> 1.19.2)

Route (skill `port`): fork the decompiled 2.4.1 code into `development/backportedYuushyaModelling`, Forge 43,
keep mod id `yuushya_modelling` and every item and block id (saves and the pack's `@yuushya` matches carry over; the
mod id has an underscore, which the own-mod naming rule forbids for new mods, but upstream ids are kept, D2). The
jar replaces 2.2.0 in `mods/` and `server_mods/` (D1). Main porting work, by size:

1. **Screens, about 7k lines, 14 files on `GuiGraphics`**: 1.20 `GuiGraphics`, `Button.builder` / `Tooltip`,
   `MultiLineEditBox`-style widgets (1.19.3+) -> 1.19.2 `PoseStack` + `GuiComponent`, `Button` with `OnTooltip`,
   own multi-line field. `CycleButton` and `AbstractSliderButton` exist in 1.19.2. The bulk of the work.
2. **Math, 24 files on JOML**: `org.joml` vectors, matrices and `Axis` -> `com.mojang.math` `Vector3f`, `Vector4f`,
   `Quaternion`, `Matrix4f`, `Vector3f.YP.rotationDegrees`. 2.2's code is the reference for the old forms.
3. **Text Block font path**: `GlyphRenderTypes` does not exist in 1.19.2 (`BakedGlyph` holds its three render types
   directly), so the glyph mixin, text shader and the font access transformers are rewritten. Biggest risk (D1
   phase B).
4. **Registries and blocks, 8 files**: `BuiltInRegistries` -> `Registry`, `ResourceLocation.fromNamespaceAndPath`
   -> constructor, `Properties.of()` -> `Properties.of(Material.METAL)` (2.2 value), `forceSolidOn` dropped,
   creative tabs as `CreativeModeTab` subclasses, `UnbakedModel.getMaterials` / `bake` to the 1.19.2 signatures (2.2
   has them). `ModelEvent.ModifyBakingResult`, `IForgeBakedModel.getRenderPasses`, `ModelData` and
   `SimpleChannel` exist in Forge 43.
5. **Data**: B1 conversion; `pack.mcmeta` to the 1.19.2 format (15 -> 9); recipe JSON without `category`.
6. **Cached render pipeline and its two mixins**: ported with the Item Block (D4). Dead code dropped: `GetBlockStateItemForge`, `ByteBufferBuilder`, the `distance` property, the `cube_12` key, 3 unused textures.

### 1.7 Fold-in once built

Done in A8 to A12 (2026-11-25): 1, 2 (mod_sides row; Architectury stays for its other users, its abbreviated Required-by list unchanged), 3, 4 (tag
unchanged), 5 (census row waits for `/dumploot`), 6 (names and stack catalogs wait for the dumps; toasts.md row
updated), 7 (changelog family; queue rows after the look-check A11).


1. Install the fork in `mods/` and `server_mods/` (2.2.0 to `jar_archive/displaced/` via `installToMods`), then
   `/dumpall` ([dumpall-refresh-workflow](agent_notes/dumpall-refresh-workflow.md)); the generators below read it.
2. [mod_sides.md](../mod_sides.md): row stays `both`, jar name and source change; drop Yuushya from Architectury's
   "Required by" if listed. Add `backportedYuushyaModelling` to `IN_TREE_MODS` in `tools/build_deploy.py`.
3. Ids: no `item_dict.js` entries unless a pack script starts using them (none planned).
4. Tags: the `@yuushya` decorative match in `blocktags.js` covers the two new blocks (BlockRunner tier); no
   mineable tag upstream (2.2 had none either), so check break speed with a pickaxe in YMB-08.
5. Recipes: upstream data in the jar (B1 converted); no `global.NCH` changes (D5). Loot census row
   (`kubejs/data/creativeloot/kubejs/loot/loot_census.md`) gains `blocks/itemblock`.
6. [names.md](names.md) audit of the 35 new phase A items (D6; 36 with the Text Block); [stack_sizes.md](stack_sizes.md) catalog (primitives 16);
   [item_physics.md](item_physics.md) family tags for the new items (generator rerun); JEI coverage
   ([jei_coverage.md](jei_coverage.md)); [toasts.md](toasts.md) for the new screens' toasts.
7. Release: changelog family "Yuushya Modelling" (new blocks, items, editing options, B1 to B3 fixes), TODO row,
   queue rows `SQ-YMB-` / `CQ-YMB-` from section 3.

## 2. Open questions

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | Scope of the backport. | Phase A: Entity Block options (1.1), Item Block, primitives, Color Picker, History List, reload command, B1 to B3. Phase B: Text Block (font path rewrite). | Phase A, then B. |
| D2 | Keep mod id `yuushya_modelling` and namespace `yuushya` in our fork? | Yes: saves, item NBT and pack matches carry over; it replaces upstream rather than adding a mod. | Yes. |
| D3 | Folder name. | `development/backportedYuushyaModelling` (port skill pattern). | That. |
| D4 | The AnvilCraft-derived cached render pipeline. | Keep: it renders Item Blocks (first read as unused, wrong). | Keep. |
| D5 | Upstream recipes (Item Block 8 from quartz + wheat + kelp, primitives from `cube_0` by stonecutter, Color Picker, History List has none). | Keep as upstream; the gold-tag rule on the Shimmering Pearl recipe stays ([recipes_metal.md](recipes_metal.md)). | Keep. |
| D6 | New item names carry color codes (`§9` tooltip lines; the existing "§CModelling Chisel" style). | Run the [names.md](names.md) audit on the 36 new items (35 in phase A) like the 2 existing rows; no renames beyond title case. | Title case only. |
| D7 | History List keeps a per-player list of broken model blocks with their data. | Keep; check it is not written to disk unbounded during the build. | Keep. |
| D8 | The 1.21.1 build adds one more item (`shape_item`). | Skip; it is not in the 1.20.1 code. | Skip. |

Rulings 2026-11-25 (owner and agent defaults): [plan_yuushya_backport.md](plan_yuushya_backport.md) section 1, R1 to R11.

## 3. Tests

Drafted for the build; queue rows (`SQ-YMB-`, `CQ-YMB-`) are written when it is built.

- [ ] **YMB-01 Old builds load:** a world with 2.2 Entity Blocks (several layers, rotated, scaled, lit) -> with the
  fork every block shows the same layers, has no collision, AO off; no missing-state or NBT errors in the log.
- [ ] **YMB-02 Collision shapes:** cycle each of the 5 shapes on one Entity Block, walk into and onto it -> No
  Collision passes through, Fence blocks at 1.5 high, Bottom/Top Half and Block collide as named; persists after relog.
- [ ] **YMB-03 Fine Tune mode:** set rotation 90.5 and offset 2.05 in Fine Tune -> values render and survive relog on
  a dedicated server.
- [ ] **YMB-04 Input mode and copy:** type values, apply, reset; copy a layer's block and state -> applied values
  match, copy places the same state.
- [ ] **YMB-05 Face count:** press the face count button on a 3-layer block -> a count shows, no lag spike.
- [ ] **YMB-06 Item rendering:** pick up a configured Entity Block (Destroy item) -> hand and inventory show its
  layers; a plain Entity Block item shows the normal icon.
- [ ] **YMB-07 Item model cache (B2):** a hotbar of 9 configured Entity and Item Block items, open and close the
  inventory and JEI for 5 minutes -> client heap after GC does not keep growing (JFR or F3 memory).
- [ ] **YMB-08 Item Block:** place, add 3 item layers, set position, rotation, scale, color, brightness, AO, full-block
  AO, mirror; relog; break with the Destroy item -> everything kept; with full-block AO on, standing inside does
  suffocate on a dedicated server the same as in single player (B3).
- [ ] **YMB-09 Primitives:** stonecutter on `cube_0` -> 31 options; primitives place into an Item Block and render;
  JEI lists the 32 items and the Modeling Primitive tab.
- [ ] **YMB-10 Recipes and loot (B1):** JEI shows all 52 Yuushya recipes; mining an Entity Block and an Item Block by
  hand drops the block.
- [ ] **YMB-11 Color Picker and History List:** right click a block with the Color Picker, left click -> clipboard
  gets the color; break 2 Entity Blocks, open the History List -> both listed and restorable.
- [ ] **YMB-12 Reload command:** `/yuushya_reload_model reload all` -> success message, blocks still render.
- [ ] **YMB-13 Dedicated server:** start a dedicated server with the fork -> no client class loads; a client with the
  fork joins and edits a block that a second client sees updated.
- [ ] **YMB-14 Text Block (phase B):** place, enter two lines, change font, color, mirror, cull -> renders the same
  for a second client; text survives relog.

## 4. History

- 2026-11-25: investigation written, nothing built. 2.4.1 is 1.20.1/1.21.1 only and its source is not public; MIT,
  backport from the decompiled jar.
- 2026-11-25: owner asked for a plan for phases A and B, mod id kept, unused parts dropped, all bugs fixed:
  [plan_yuushya_backport.md](plan_yuushya_backport.md). D4 corrected (the render cache is the Item Block renderer).
- 2026-11-25: phase A resources, data and install (plan A8 to A10): 2.4.1 assets without the three R4 textures and the
  Text Block and guidebook assets, data converted to 1.19.2 (B1), bugs B5 and B6 added, `tools/yuushya_data_check.py`
  (run by `tools/test_yuushya_data.sh` in `check_all.py`). The primitive count is 32, not 34.
