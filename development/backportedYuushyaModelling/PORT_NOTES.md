# Yuushya Modelling 2.4.1 backport: working notes (project `:backportedYuushyaModelling`)

Plan and rulings: [plan_yuushya_backport.md](../../feature_writeup/plan_yuushya_backport.md) (R1 to R11, steps A0 to
A12, B0 to B6). Report: [yuushya_modelling.md](../../feature_writeup/yuushya_modelling.md). Upstream: the released jar
`DecompiledMods/yuushya_modelling-1.20.1-2.4.1.jar` (Forge 47, MIT; the public repository stops at 2.2.0).

## 1. Sources

- Decompiled 2.4.1 (the source of every ported class): `DecompiledMods/yuushya_modelling-1.20.1-2.4.1/` (untracked;
  `python3 tools/decompile_mod.py DecompiledMods/yuushya_modelling-1.20.1-2.4.1.jar <out>` recreates it). 1.20-only
  SRG names (`m_252781_`, `f_256975_`, ...) stay unmapped in that tree: the tool maps 1.19.2 names only.
- Decompiled 2.2.0 (the 1.19.2 reference for old forms: `TransformData`, `ShowBlockModel`, screens):
  `DecompiledMods/yuushya-modelling-1.19.2-forge-2.2.0/`.
- Package and class names follow upstream (`com.yuushya.modelling`, `YuushyaNeoForge` is upstream's Forge entry class
  name). Each file names its upstream class in its header.

## 2. 1.20.1 -> 1.19.2 API map (used so far)

| 1.20.1 (upstream) | 1.19.2 (ours) |
|---|---|
| `org.joml.Vector3f` (public fields `x`, chained setters) | `com.mojang.math.Vector3f` (`x()`, `set(float,float,float)` returns void; `set(double..)` -> cast to float) |
| `org.joml.Vector3d` (`x()`) | `com.mojang.math.Vector3d` (public fields `x`, `set(double,double,double)`) |
| `org.joml.Quaternionf` (fields `x y z w`) | `com.mojang.math.Quaternion` (`i() j() k() r()`, `set(i,j,k,r)`) |
| `Axis.XP/YP/ZP.rotationDegrees` (`f_252529_`/`f_252436_`/`f_252403_` + `m_252977_`), `PoseStack.mulPose` (`m_252781_`) | `Vector3f.XP/YP/ZP.rotationDegrees`, `PoseStack.mulPose` |
| `BuiltInRegistries.BLOCK` / `ITEM` (`f_256975_` / `f_257033_`) | `Registry.BLOCK` / `Registry.ITEM` |
| `Registries.BLOCK_ENTITY_TYPE` / `MENU` in `DeferredRegister.create` (`f_256922_` / `f_256798_`) | `ForgeRegistries.BLOCK_ENTITY_TYPES` / `MENU_TYPES` |
| `ResourceLocation.parse(s)`, `fromNamespaceAndPath(a, b)` | `new ResourceLocation(s)`, `new ResourceLocation(a, b)` |
| `BlockBehaviour.Properties.of()` (`m_284310_`) + `forceSolidOn()` (`m_280606_`) | `Properties.of(Material.METAL)` (2.2 value; METAL is solid, which is what forceSolidOn restored) |
| Registered `CreativeModeTab.builder()` tabs (`Registries.CREATIVE_MODE_TAB`, `f_279569_`) | `CreativeModeTab` subclasses in `GroupRegistry` (eager, `fillItemList` in upstream order) + `Item.Properties.tab(...)` on every item |
| `ItemStack.isItemEnabled(level.enabledFeatures())` (`m_246617_` / `m_246046_`) | removed (1.19.2 has no feature flags: always enabled) |
| `Slot.setByPlayer` (`m_269060_`) | `Slot.set` |
| `NbtUtils.readBlockState(HolderGetter, tag)` (`m_247651_`, `registryAccess().lookupOrThrow`) | `NbtUtils.readBlockState(tag)` |
| `Holder.get()` | `Holder.value()` |
| `SoundEvents.UI_BUTTON_CLICK.value()` (a `Holder`) | `SoundEvents.UI_BUTTON_CLICK` (a `SoundEvent`) |
| `ItemDisplayContext` | `ItemTransforms.TransformType` (client-only in 1.19.2) |
| `serverPlayer.level() instanceof ServerLevel sl` | `ServerPlayer.getLevel()` already returns `ServerLevel`; kept as `(Object)... instanceof ServerLevel sl` |
| `HolderLookup.Provider` parameter of `ITransformDataInventory.saveAdditionalWithoutAir` (unused, never called) | parameter removed |
| `lombok.Generated` annotations (decompiler output) | removed |
| `ModelEvent.ModifyBakingResult` (not in Forge 43) | `ModelEvent.BakingCompleted` (`getModels()` is mutable; 2.2.0 used it) |
| `UnbakedModel.getMaterials(Function)` (void) / `bake(ModelBaker, ...)` | `getMaterials(Function, Set<Pair<String,String>>)` returning `Collection<Material>` / `bake(ModelBakery, ...)` |
| `QuadTransformers.toABGR` / `applyingColor` (Forge 47) | local copies `NeoItemBlockModel.toABGR` / `applyColor` |
| `Vector4f.transform` via `Matrix4f.transform(v)` (`m_252922_`) | `vector4f.transform(stack.last().pose())` |
| `PoseStack.translate(float...)` (`m_252880_`) | `PoseStack.translate(double...)` |
| `new VertexBuffer(Usage.STATIC)`, `drawWithShader` (`m_253207_`) | `new VertexBuffer()`, `drawWithShader` |
| `ItemDisplayContext.NONE` | `ItemTransforms.TransformType.NONE` |
| `Font.drawInBatch(..., DisplayMode.SEE_THROUGH, ...)` (`m_272077_`) | `Font.drawInBatch(..., true, ...)` (seeThrough flag) |
| `Quaternionf().rotationYXZ(-yRot, xRot, 0)` for billboard text | `camera.rotation()` (the same rotation) |
| `BufferUploader.drawWithShader(builder.end())` for axis lines | `RenderSystem.disableTexture()` + `tesselator.end()` (2.2.0 form) |
| AT on `RenderType.mode` / `format()` / `affectsCrumbling` | public `mode()` / `format()` / `affectsCrumbling()` in 1.19.2; AT only for `sortOnUpload` |
| `GuiGraphics` (1.20) | `gui/compat/GuiGraphics`: a thin wrapper of the 1.19.2 `PoseStack` with the same method names (`fill`, `hLine`, `vLine`, `drawString`, `drawCenteredString`, `blit`, `renderItem`, `renderItemDecorations`, `pose`, `bufferSource`, `guiWidth`/`guiHeight`, plus `flush`), built in each `render(PoseStack, ...)` override, so upstream's drawing code stays as written |
| `render(GuiGraphics, ...)` (`m_88315_`), `renderWidget` (`m_87963_`), `renderBackground(GuiGraphics)` (`m_280273_`), `AbstractContainerScreen.renderTooltip(GuiGraphics, x, y)` (`m_280072_`), `updateWidgetNarration` (`m_168797_`), list `Entry.render(GuiGraphics, ...)` | `render(PoseStack, ...)`, `renderButton(PoseStack, ...)`, `renderBackground(PoseStack)`, `renderTooltip(PoseStack, x, y)`, `updateNarration`, `Entry.render(PoseStack, ...)`, all with `@Override` |
| `AbstractWidget.getX()` / `getY()` (`m_252754_` / `m_252907_`) | public fields `x` / `y` |
| `Button.builder(...).bounds(...).tooltip(Tooltip.create(c)).build()` (`m_253074_`, `m_252987_`, `m_257505_`, `m_257550_`, `m_253136_`) | `gui/compat/ButtonUtils.builder` (2.2.0's helper) and `gui/compat/Tooltip.create(c)` returning a `Button.OnTooltip` drawn by the open screen |
| `CycleButton.Builder.withTooltip(v -> Tooltip.create(c))`, `AbstractWidget.setTooltip` (`m_257544_`) | `withTooltip(v -> Tooltip.lines(c))` (1.19.2 `TooltipSupplier` returns lines); `SliderButton` implements `TooltipAccessor`; 1.19.2 draws `TooltipAccessor` tooltips only in options screens, so the Entity Block and Item Block screens call `Tooltip.renderHovered` at the end of `render` |
| `GuiGraphics.renderItem` (pose-aware) | `ItemRenderer.renderAndDecorateFakeItem` inside the model-view stack multiplied by the pose (1.19.2 GUI items ignore the `PoseStack`) |
| `ItemRenderer.render(..., ItemDisplayContext.GUI, ..., guiGraphics.bufferSource(), ...)` | `TransformType.GUI`, then `flush()` (end the batch) and flat-item lighting around it (1.20's `GuiGraphics` did both) |
| `RenderType.TRANSLUCENT` (field) | `RenderType.translucent()` |
| `Quaternionf().rotateXYZ(x, y, z)` / `getEulerAnglesXYZ` | `Quaternion.fromXYZ(x, y, z)` (same order) / `YuushyaUtils.eulerAnglesXYZ` (JOML 1.10's formula; mojang `toXYZ` differs) |
| `RenderGuiOverlayEvent.Pre.getGuiGraphics()` | `new GuiGraphics(event.getPoseStack())` |

Decompiled 1.20 code has no `@Override`: a 1.20 override whose 1.19.2 signature differs would compile as a dead new
method. Every ported class was checked by reflection against the 1.19.2 supertypes (no such case in A0 to A3); later
steps should rerun the same check on their classes (a small `OverrideCheck` over `build/classes`, compile classpath
from Gradle). Rerun after A4 and A5: no dead override (the only hits are generic bridge false positives and the two
upstream `getQuads` helper overloads); every override written in A4/A5 carries `@Override`.

Access transformer: `tools/accesstransformer.official.cfg` (Mojang names, readable) -> `tools/gen_at.py` (copied from
exposureBackport) -> `src/main/resources/META-INF/accesstransformer.cfg` (SRG names, read by Forge at runtime and by
`minecraft { accessTransformer }` in build.gradle at compile time). Only entries the ported code uses; phase B adds its
font and glyph entries.

## 3. Deviations from 2.4.1

- Mod jar: GPL-3.0-only for our changes, `NOTICE-upstream-MIT.txt` keeps upstream's notice. `mods.toml` without the
  1.20 `[[mixins]]` / `[[accessTransformers]]` blocks (Forge 43 reads the manifest `MixinConfigs` attribute).
- Dropped as unused (plan section 0): `item/GetBlockStateItemForge`, the `distance` blockstate property
  (`YuushyaBlockStates.DISTANCE`), and with them `GetBlockStateItem.renderByItem` (its only caller was
  `GetBlockStateItemForge`; it referenced client-only render types from a common class).
- `BlockBehaviourMixin` is in the mixin config's common `mixins` list (upstream: `client`), bug B3.
- `ShowBlock` and `ItemBlock` use `Properties.of(Material.METAL)` without `forceSolidOn` (no such method in 1.19.2;
  METAL is a solid material).
- Network channel `yuushya_modelling:main`: protocol version `2.4.1-ce-1` (upstream `1`), so a 2.2.0 jar on the other
  side is refused at login. Packet ids keep upstream's order; the two Text Block packets' ids (2, 3) are skipped
  until phase B registers them.
- Creative tabs: see the API map. Item order in both tabs is upstream's.
- Item model cache (bug B2): `client/ItemModelCache` replaces both `Map<ItemStack, ...> itemModelCache` fields
  (keyed by stack identity, never cleared, a plain `HashMap` in `NeoShowBlockModel`): keyed by the transform-data
  tag's string form, LRU 256 entries, synchronized (nested render passes reach it from chunk build threads), cleared by
  a client reload listener (`RegisterClientReloadListenersEvent`) on every resource reload.
- Item Block render cache (`client/anvilcraft/rendering`, `LevelRendererMixin`, `MinecraftMixin`): ported, not the
  plan's fallback. The 1.19.2 targets fit: `LevelRenderer.renderLevel` has the same parameters (mojang `Matrix4f`) and
  the same `compileChunks(Camera)` and first `BufferSource.endLastBatch()` calls; `Minecraft.updateLevelInEngines`
  exists. Both mixins are in the `client` list. Changes: null checks on `CacheableBERenderingPipeline.getInstance()`
  (upstream would throw if a frame rendered before the first level was set) and on the local player in the rebuild
  task; `updateLevel` also clears `CachedModeClient` and `CustomRenderInstance` (upstream kept the old level's
  Item Block positions across worlds and dimensions); upstream's Text Block skip in the rebuild task waits for phase B
  (`PHASE-B:`). License: upstream adapted this package from AnvilCraft (LGPL-3.0-or-later); recorded in
  `NOTICE-upstream-MIT.txt`, conveyed under GPL-3.0 (LGPL-3.0 is GPL-3.0 plus permissions, so compatible).
- `YuushyaClientNeoForge`: upstream's block and item color handlers were instance methods in an
  `@EventBusSubscriber` class, which Forge never calls (tinted blocks such as grass and leaves rendered untinted in
  Entity Blocks and their items); static here, as in 2.2.0. `ColorTexture` setup waits for A6 (`TODO(A6)`).
- Color Picker: upstream checked `level instanceof ClientLevel` and called the clipboard from common item code
  (`canAttackBlock` runs on the server too), which a dedicated server cannot load. The pixel read and clipboard live
  in `client/ColorPickerClient`, reached through `DistExecutor` only when `level.isClientSide`. Unchanged upstream
  behavior: the picked color is written to the client's copy of the stack only (no packet), so the tooltip and HUD
  show it until the server next syncs that slot; the clipboard keeps it.
- Chisel and History List screens (`AbstractEngraveScreen`): the decompiled code draws 1.20.2 GUI sprites
  (`container/stonecutter/scroller`, `recipe_selected`, ...) that 1.19.2 does not have; the 1.19.2 form (2.2.0, as the
  vanilla stonecutter) blits the same regions of `stonecutter.png`. The screen draws its dimmed background itself
  (1.19.2 `Screen.render` does not). The hovered recipe shows its item tooltip (upstream's call there is the 1.20 item
  tooltip). `mouseScrolled` takes 1.19.2's three arguments (upstream's four-argument form overrides nothing on 1.20.1
  either, so scrolling the recipe list did nothing there).
- `ColorButton` (unused upstream): constructor without the 1.19.3 `CreateNarration` argument.
- Edit boxes are not ticked (as upstream), so their cursor shows without blinking on 1.19.2.
- History List (plan R10): held in memory only, never written to disk. Upstream fills it on the client whenever an
  Entity Block or Item Block is removed, which includes every client chunk unload, keyed by `<dimension>/<x, y, z>`,
  in plain static `HashMap`s that grew for the whole game session and that the integrated server thread read while the
  render thread wrote. Here each map is a synchronized access-order map capped at 256 entries
  (`HistoryItem.HISTORY_LIMIT`), and `HistoryMenu` reads a copy (`HistoryItem.snapshot`).

- Share codes (A11 fix, build 02): upstream's `ShareUtils` share types (`ShareBlockInformation`, `ShareItemInformation`
  and their nested `ShareData` / `ShareBlockState` / `ItemShareData` / `ShareItemStack`) are Java records, which Gson
  2.10 (1.20.1) handles and Gson 2.8.9 (1.19.2) does not: `fromJson` dies on setting a final field, as an
  `AssertionError` that the callers' `catch (Exception)` misses. It crashed the client on every Entity Block or Item
  Block removal (`setRemoved` feeds the History List, which includes chunk unloads), and would have on applying a
  share code, saving a work and loading Chisel works. Here they are plain final classes with the same field names and
  order, non-final private fields, a private no-arg constructor for Gson and record-style accessors, so the JSON
  (UPPER_CAMEL_CASE keys) is byte-identical to upstream's. `ShareData.transfer` pads copies of the lists (as
  upstream's item form does) instead of the share object's own lists. No other record goes through Gson.
  `tools/test_yuushya_share.sh` (`tools/yuushya_share_test/`, in `check_all.py`) writes the upstream format with
  upstream's records under Gson 2.10.1 and reads and writes it with `ShareUtils` under Gson 2.8.9; it fails on the
  record version with the crash's exception. It compiles `ShareUtils.java` fresh against `build/classes`, so build
  the project once first.
- Button labels (A11 fix): upstream labels five buttons with emoji outside the Basic Multilingual Plane (sunglasses
  and eyes for shown/hidden, outbox, inbox, floppy disk, bar chart). 1.19.2's font ends at U+FFFF
  (`glyph_sizes.bin` is 65536 bytes, `legacy_unicode` pages only), so they drew as boxes. Replaced with glyphs of
  the 1.19.2 bitmap fonts: shown/hidden U+2611 / U+2610 (ballot box with check / empty), copy share code U+2191
  (up arrow), apply share code U+2193 (down arrow), save work U+270E (pencil), face count U+03A3 (sigma); the
  tooltips are unchanged. Every non-ASCII character in `src/main/java` is a `\u` escape (none outside string
  literals); all of them are in the 1.19.2 font.
- Icon lists (A11 fix): 1.20's `AbstractSelectionList` clips entries to the list bounds; 1.19.2's does not, so a
  layer name in the Entity Block list ran into the selected layer's property lines drawn right of the list.
  `BlockStateIconList` and `ItemStackIconList` wrap `render` in `GuiComponent.enableScissor(x0, y0, x1, y1)` (the
  1.20 clip; 2.2.0 had the same overlap).

## 4. Text Block (phase B)

Not registered in phase A: no `textblock` block, item or block entity, no text packets, no `texts` reload literal, no
text share format, no text engrave results. Every removed spot carries a `// PHASE-B:` comment saying what goes back
(`grep -rn PHASE-B src`). The `ReloadType.TEXTS` and `BLOCK_TYPE.TEXT` enum values stay so phase B does not reorder
them.

## 5. Phase state

Status lives in the TODO rows (`mod-integration #29`, `#36`); this section only lists what is in the source set.

- A0 to A3 (2026-11-25): skeleton, registries, blocks, block entities, transform data, utils, items (brought in so the
  registry compiles; A5 reviews them), engrave and history menus and result loaders, network, the reload command,
  `ClientEvent` (item properties and loaders only), `BlockBehaviourMixin`. Stubs left for later steps are marked
  `// TODO(A4)`, `// TODO(A5)`, `// TODO(A6)` (`grep -rn "TODO(A" src`).
- A4 and A5 (2026-11-25): models (`ShowBlockModel`, `ItemBlockModel`, `client/NeoShowBlockModel`,
  `client/NeoItemBlockModel`, `client/ItemModelCache`), block entity renderers (`AbstractTransformBlockEntityRender`,
  `ShowBlockEntityRender`, `ItemBlockEntityRender`, registered in `ClientEvent`), `YuushyaClientNeoForge` (model
  wrapping, color handlers, reload listener), the Item Block render cache and its two mixins, the access transformer,
  `client/ColorPickerClient`, the History List maps; items reviewed (primitives 32, all stack 16 and rare; Destroy item
  and GUI Terminal act on Item Blocks through the shared `AbstractTransformBlock` / `AbstractTransformBlockEntity`
  types, no change needed).
- A6 and A7 (2026-11-25): every screen and widget except Text Block ones (`gui/AbstractEngraveScreen`,
  `gui/engrave/EngraveScreen`, `gui/history/HistoryScreen`, `gui/showblock/` (Entity Block screen, `EditScreen`),
  `gui/itemblock/ItemBlockScreen`, `gui/widget/` (sliders, color widget and texture, icon lists, transform
  components), `gui/validate/`), the `gui/compat/` helpers, `GuiOverlayEvent` (Color Picker HUD), `RenderInsideEvent`;
  menu screens registered, screens opened from `ClientMethod`, `ColorTexture` created at client setup. No `TODO(A`
  stub left. Commands reviewed (`texts` literal stays out until phase B). Dead-override check rerun: only the known
  generic bridge false positives.
- A8 to A10 (2026-11-25): resources and data. Assets are the 2.4.1 jar's minus `textures/item/{blueprint,pos_trans_tool,
  tool_bundle}` (R4), the Text Block's `blockstates/textblock.json`, `models/item/textblock.json`,
  `textures/item/abandon_textblock.png` (phase B puts them back) and `yuushya_guidebook` model and sprite (no item since
  2.4). Lang: no `item.yuushya.cube_12` (B4), no `block.yuushya.modelframe` / `mixedblock` (+ `.line1`; 2.2
  leftovers, no block), no Text Block keys except `gui.textBlockScreen.block` / `.item` (used by the Item Block
  screen); `gui.showBlockScreen.workshop.save_error` added to en_us (B6, missing upstream). Data: `recipes/` and
  `loot_tables/` (B1), checked by `tools/yuushya_data_check.py`. Template models (Chisel works such as `paimon`)
  reference textures of the optional Yuushya content mod, as in 2.2.0 and upstream; not in the pack, so they show
  missing textures in the Chisel preview, unchanged.
- A11 fixes (2026-11-25, build `2.4.1-ce-19.2-02`): share-code classes, button labels, icon-list clipping (section 3).
  Look-check findings that are upstream behavior, unchanged: toggling AO or full block in the Item Block screen
  reverts an unsent color preview (report B7); Item Block layers are not biome-tinted (upstream's `CachedRegion`
  colors quads with the layer color only); the screen mode resets to Incremental (`Mode.SLIDER`) whenever the screen
  is re-initialized, such as after the Save dialog (upstream's `init` builds the mode button with that initial
  value); a Grass Block layer lists `age` first because Immersive Weathering's replacement grass block has `age`,
  `fertile` and `snowy`.
- Not in the source set yet: Text Block screens and widgets (`gui/textblock/`, `TextIconList`, `TextTransformComponent`,
  `FontList`, `StyledMultiLineEditBox`, `StyledMultilineTextField`; phase B), and their resources.