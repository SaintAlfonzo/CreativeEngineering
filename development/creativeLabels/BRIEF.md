# Creative Labels: build brief (phases 1 to 4)

Working notes for the agents building the mod. The spec is `feature_writeup/sticky_notes.md` (read sections 1, 3 and
4 in full before writing code; section 3.17 lists handled issues C1 to C15 and the settled questions Q1 to Q6). This
file pins the technical decisions so every stage agrees. Dated 2026-10-20.

## 0. Rules that apply to everything

- No emoji anywhere in files. Status words only. Comments in plain English.
- The user's own work is GPLv3: copy `development/moreMooblooms/LICENSE.txt` into `development/creativeLabels/`.
- Never run the game client. The game is not running now, but re-check before touching `mods/`:
  `ps -eo pid=,etime=,comm=,args= | awk '$3=="java" && /fml\.forgeVersion/ {print $1, $2}'` (never `pgrep -af` or
  `ps -f`: the launcher command line holds the account token).
- Do not commit. Do not run `installToMods` (the orchestrator does that at the end).
- Build: from the instance root, `./gradlew :creativeLabels:build --offline` (first build may need the network for
  the mixin AP / mixinextras artefacts; drop `--offline` if it fails on dependency resolution). The jar lands in
  `development/creativeLabels/build/libs/creativeLabels-1.0.0.jar`. A stage is not done until this passes.
- Headless tests: `tools/creative_labels_test/run.sh` (created in the scaffold stage, see 6).
- Donor art must not be copied into `src/`: the Sticky It! and Labels jars are All Rights Reserved. Our code reads
  their files at runtime from the installed jars (`ModList.get().getModFileById("sticky_it").getFile().findResource(...)`)
  and builds derived images in memory. Nothing derived is written to disk.
- Decompiled references (Mojang names): `DecompiledMods/sticky_it-1.2.1/`, `DecompiledMods/labels-1.19.2-1.10/`,
  `DecompiledMods/moonlight-1.19.2-2.3.7/`, `DecompiledMods/create-1.19.2-0.5.1.i/`. Mapped Forge jar for javap:
  `forge-1.19.2-43.3.9_mapped_official_1.19.2.jar` (instance root).
- Reference mods in tree: `development/moreMooblooms` (small in-tree mod: main class, config, mixin plugin,
  built-in packs, JEI plugin, client setup through DistExecutor), `development/backportedVinery`
  (`forge/upstream/MemoryPackResources` + `UpstreamPacks`: in-memory resource pack; `forge/compat/wthit/`: a WTHIT
  plugin outside patcherMod; `build.gradle`: `ce.forge-mod-refmap` with a real refmap, `fg.deobf('local:...')` for
  jars in `libs/`), `development/patcherMod` (`waila_plugins.json`, mixins into other mods by name).

## 1. Identity and build

| Item | Value |
|---|---|
| Folder / Gradle project | `development/creativeLabels` / `:creativeLabels` (add to root `settings.gradle` list) |
| mod id / name / version | `creative_labels` / `Creative Labels` / `1.0.0` |
| jar | `creativeLabels-1.0.0.jar` (`jar_name=creativeLabels`) |
| group / package | `com.SaintAlfonzo.creative_labels` |
| licence | `mod_license=GNU GPL3`, LICENSE.txt in the folder |
| build plugin | `ce.forge-mod-refmap` (mixins in Mojang names, refmap `creative_labels.refmap.json`, config `creative_labels.mixins.json`, `mixin_configs=creative_labels.mixins.json` in gradle.properties); copy the refmap-output and refmap-backstop task blocks from backportedVinery's build.gradle; no access transformer unless needed |
| libs (git-ignored `libs/`, copied from `mods/`) | `jei-1.19.2-forge-11.8.1.1034.jar` (compileOnly fg.deobf, flatDir `local:jei-1.19.2-forge:11.8.1.1034`), `moonlight-1.19.2-2.3.7-forge.jar` (compileOnly fg.deobf, `local:moonlight-1.19.2:2.3.7-forge`), WTHIT via `files('../patcherMod/libs/wthit-1.19.2-forge-5.36.1.jar')`. No compile dependency on sticky_it, labels or Create |
| mods.toml dependencies | forge, minecraft (as moreMooblooms); `moonlight` mandatory `[1.19.2-2.3.0,)` (Moonlight versions start with the MC version; a bare `[2.3,)` never matches `1.19.2-2.3.7`); `sticky_it` mandatory `*` and `labels` mandatory `*` (art donors until phase 5); `jei`, `wthit` optional. ordering NONE, side BOTH |
| Forge APIs available (verified) | `IDynamicBakedModel`, `ModelData`/`ModelProperty`, `QuadBakingVertexConsumer`, `TextureStitchEvent.Pre`, `RenderHighlightEvent.Block`, `ModelEvent.RegisterAdditional`, `RegisterClientReloadListenersEvent` |

Registry ids: block `creative_labels:face_paper` (no item), items `creative_labels:sticky_note`,
`creative_labels:label`, `creative_labels:eraser`, block entity `creative_labels:face_paper`, recipe serializers
`creative_labels:note_dye`, `note_special`, `note_copy`, `note_erase`, network channel `creative_labels:main`.

## 2. Package layout (`com.SaintAlfonzo.creative_labels`)

| Package | Classes | Stage |
|---|---|---|
| root | `CreativeLabels` (@Mod, registers everything, DistExecutor for client), `CLConfig` (common + client specs), `CLRegistry` (DeferredRegisters: block, BE type, items, recipe serializers), `CLMixinPlugin` (gates `StickyItTooltipMixin` on sticky_it being loaded; everything else always) | scaffold |
| `block` | `FacePaperBlock`, `FacePaperBlockEntity`, `FaceContent`, `NoteData`, `LabelData`, `Spots` (pure geometry + host rules), `PieceHit` (which face/spot a hit lands on), `FacePaperInteractions` (RightClickBlock handler, placement helpers) | scaffold (data model + Spots + registration), phase 1 (behaviour) |
| `item` | `StickyNoteItem`, `LabelItem`, `EraserItem`, `NoteColours` (dye mixing, specials, greyscale helpers used by both sides), `ItemLabels` (`CreativeLabels.Lines` read/write), `AnvilLabels` (AnvilUpdateEvent), `LabelGate` (one label per 5 ticks per player) | scaffold (classes with NBT helpers), phase 2 (behaviour) |
| `crafting` | `NoteDyeRecipe`, `NoteSpecialRecipe`, `NoteCopyRecipe`, `NoteEraseRecipe` (all `CustomRecipe` with `SimpleRecipeSerializer`) | phase 2 |
| `net` | `CLNetwork` (SimpleChannel), `NoteTextPacket` (C2S) | scaffold (channel), phase 2 (packet) |
| `client` | `ClientSetup` (all client registration; only reached through DistExecutor), `FacePaperModel` + `FacePaperModelLoader` (dynamic baked model, geometry loader registered on `ModelEvent.RegisterGeometryLoaders`), `NoteShape` (Sticky It's curled polygons captured at runtime, fallback constants), `DonorArt` (in-memory resource pack + atlas stitching + generated images), `FacePaperHighlight` (RenderHighlightEvent.Block), `FacePaperRenderer` (BlockEntityRenderer: label icons and names only), `IconDye` (dyed-icon ramp), `NoteScreen`, `NoteItemColours` (ItemColors + item property overrides) | phase 1 (model, art, highlight), phase 2 (screen, item colours), phase 3 (renderer, icon dye) |
| `compat` | `wthit/FacePaperWthit` (client plugin), `jei/CreativeLabelsJei` (subtypes, example recipes), `DonorItems` (which old items are hidden) | phase 1 (WTHIT notes), phase 2 (JEI), phase 3 (WTHIT labels), phase 4 (hiding) |
| `mixin` | `CraftingMenuResultMixin` (blockLabelledCrafting), `ItemHiddenMixin` (old donor items out of creative tabs), `StickyItTooltipMixin` (@Pseudo, targets `net.mcreator.sticky_it.procedures.TooltipRenderProcedure`, cancels the private static `execute(Event, ItemStack, List)` at HEAD, `remap = false`) | phases 2, 4 |

Resources: `META-INF/mods.toml`, `pack.mcmeta`, `creative_labels.mixins.json`, `waila_plugins.json`,
`assets/creative_labels/lang/en_us.json`, `assets/creative_labels/models/item/*.json` and `block/face_paper.json`
(the custom loader), `blockstates/face_paper.json`, `data/creative_labels/tags/blocks/paper/posts.json` and
`paper/deny.json` (defaults; the pack adds more through `global.tags`), `data/create/tags/blocks/non_movable.json`
(`creative_labels:face_paper`, C10).

## 3. Pinned design: the block

- **Cell and faces.** `face_paper` sits in the air cell in front of the host face. State: `PipeBlock.PROPERTY_BY_DIRECTION`
  booleans + `WATERLOGGED`. Face `d` of a cell = paper on the host block at `pos.relative(d)`; the paper is seen from
  `d.getOpposite()`. `DOWN` = notes on a floor (host below), `UP` = notes on a ceiling (host above).
- **Block properties.** `Material.CLOTH_DECORATION`, strength 0.1 (carpet), `noCollission`, `noOcclusion`,
  `SoundType.WOOL`, not flammable (Q2), not a spawn surface, no light blocking, `isSuffocating`/`isViewBlocking` false,
  `getPistonPushReaction` DESTROY, `getExplosionResistance` like carpet (0.1) but `onBlockExploded` does nothing
  (C1) and `dropFromExplosion` false. `canBeReplaced` false (Q1: nothing can be placed into a papered cell).
- **Block entity** `FacePaperBlockEntity`: `FaceContent[6]` (index = Direction ordinal). `FaceContent`: `Kind kind`
  (NONE, NOTES, LABEL), `NoteData[5] notes` (index = spot: 0 TL, 1 TR, 2 C, 3 BL, 4 BR; null = empty),
  `LabelData label`, `Direction rotation` (horizontal; only meaningful for UP and DOWN; set when the first piece goes
  on the face, cleared when the face empties), `float inset` (0 to 0.5 blocks: how far INTO the host cell the paper
  surface sits: 0 full face, 1/16 chests, 0.5 slab halves). `NoteData`: `int color` (-1 = plain yellow, else RGB),
  `String special` (null, "inverted", "smile", "creeper", "gold"), `String text` (may be empty), `long day` (in-game day
  placed), `String placer`. `LabelData`: `ItemStack picture` (stripped per C2/Q5: id + count 1 + only `Enchantments`
  (presence -> glint), `Potion`, `display.color`, `CustomModelData`), `DyeColor dye` (null), `boolean glow`,
  `boolean showName`. NBT under compound keys `Faces` -> per-direction compound; notes carry their item NBT shape so
  `NoteData.toItem()` / `fromItem()` are trivial. Synced with `getUpdateTag`/`handleUpdateTag`/`getUpdatePacket`
  (ClientboundBlockEntityDataPacket); on the client after a sync: `requestModelDataUpdate()` and
  `level.sendBlockUpdated(pos, state, state, 8)` so the chunk section rebuilds. `getModelData()` exposes an immutable
  `FaceSnapshot[]` under `FacePaperModel.FACES`. No ticker.
- **Rotation of BE data** (C7): the BE also saves `Mask` (the six booleans it was saved with). On `load`, if `Mask`
  differs from the block state's booleans, find the Rotation (NONE/90/180/270) or Mirror that maps the saved mask onto
  the state's and remap the faces and floor rotations; ambiguous masks keep identity. `FacePaperBlock.rotate`/`mirror`
  move the booleans.
- **Item NBT.** Note colour = vanilla `display.color` (StickyNoteItem implements `DyeableLeatherItem`). Everything
  else under one compound `CreativeLabels`: notes `{Text, Special}`; labelled items `{Lines: [str...]}` (C5). Write
  keys only when non-empty; remove `CreativeLabels` when empty and the tag when it becomes empty, so clean stacks
  stack again.
- **Spots** (`Spots`, pure logic, testable without MC bootstrap except `VoxelShape`): the five rectangles in the
  face frame (px, x right, y up): TL [0.5,6.5]x[10.65,16], TR [9.5,15.5]x[10.65,16], C [5,11]x[5.33,10.67],
  BL [0.5,6.5]x[0,5.35], BR [9.5,15.5]x[0,5.35]. Face frame for each Direction: wall faces: x along the wall as seen
  by a viewer in front, y up. Floors and ceilings: y = the face rotation (the player's facing at first placement),
  x to its right as seen by that player. Host rules, in this order: (1) tag `creative_labels:paper/deny` -> none;
  (2) class `StairBlock` (STRAIGHT only; corner shapes fall through to the shape test), `SlabBlock`,
  `FenceBlock`/`WallBlock`/`IronBarsBlock` (panes too)/`EndRodBlock`/`LightningRodBlock`/`ChainBlock` or tag
  `creative_labels:paper/posts` -> the table in sticky_notes.md 3.3 (posts: centre spot only on UP/DOWN faces, and
  on the two end faces of a horizontal rod/chain); (3) shape test: the host's `getShape` AABBs that reach within
  1/16 of the face plane must cover every one of 9 sample points of the spot (corners, edge midpoints, centre),
  inset = the shallowest such depth; a face is "five-spot" when all five pass at inset <= 1/16. Slab tops/bottoms:
  all five at inset 0.5 (C9, test early). Stairs sides: 3; stairs front/top: 2 (sticky_notes.md 3.3 says which).
  Labels need a five-spot face at inset <= 1/16.
- **Shapes and hits.** `getShape` = union of every piece's box (note: 6 x 5.35 px, 2.76 px out of the paper surface;
  label: 10x10 px, 0.175 px thick) placed at the face's inset; boxes may extend past the cell for inset faces (legal).
  Covered face (3.3): when a five-spot face has all five notes, that face's hit box is the whole 16x16 panel.
  `PieceHit.resolve(state, be, hitLocation)` -> face + spot (or label, or panel); nearest piece when between.
- **Breaking.** Only the hovered piece: override `onDestroyedByPlayer` (both sides): resolve the piece from the
  player's current pick, remove it, spawn its drop (server), keep the block and return false while content remains;
  last piece -> super. `getDrops(state, LootContext.Builder)` returns every remaining piece's item (uses the
  `BLOCK_ENTITY` param) so pistons/explosions of the cell itself drop everything. `getCloneItemStack` is hit-aware.
  Notes drop with colour, special and text (3.6: specials stay special when broken); labels drop blank.
- **Host gone.** `updateShape`: when the neighbour on face `d` changes, recompute `Spots` for that face; every piece
  whose spot no longer fits drops (server only; `LevelAccessor` -> `instanceof ServerLevel`); an empty face clears
  its boolean; an empty cell becomes air or water. Also check in `neighborChanged`.
- **Sneak clicks.** `PlayerInteractEvent.RightClickBlock` handler: when the hit block is `face_paper`, set
  `event.setUseBlock(Event.Result.ALLOW)` so sneak + item still reaches `use`.
- **Placement** (items' `useOn`): target cell = clicked block if it is `face_paper`, else `clickedPos.relative(face)`
  (must be air, water or `face_paper`); host = cell.relative(faceDir) where faceDir = the clicked face's opposite
  when clicking a host, or the resolved piece's face when clicking paper. Spot under the hit if free and allowed,
  else nearest free allowed spot, else nothing (`PASS`). Chests/barrels: vanilla skips `use` when sneaking with an
  item, so sneak + right-click reaches `useOn` and places; a plain right-click opens the block (nothing to do).
  First piece on an UP/DOWN face stores `player.getDirection()` as the face rotation. Plain yellow note placed:
  1 % (`goldChance`) becomes gold, server roll (C13).
- **`use` on a note** (table 3.4): dye -> mix (`NoteColours.mix`, vanilla `DyeableLeatherItem.dyeArmor` maths),
  one dye used unless creative, specials refuse; empty hand -> open `NoteScreen` (client side; server does nothing
  until the packet); sneak + empty hand -> pick up into inventory (`keepSpecialColor` off -> plain); eraser -> text
  first, then colour/special -> plain; another note -> place; anything else -> forward to the host
  (`hostState.use(level, player, hand, new BlockHitResult(loc, faceDir.getOpposite(), hostPos, false))`) unless the
  face is covered (then `CONSUME`, nothing happens). Left-click = break (above).
- **Gold spark**: `animateTick` spawns `ParticleTypes.FIREWORK` from each gold note about once a second.
- **Create**: `data/create/tags/blocks/non_movable.json`. The schematicannon skips blocks whose `ItemRequirement`
  is invalid (verified in `SchematicannonBlockEntity`: `!requirement.isInvalid()` gates placement), so a block with
  no item is simply never printed: nothing to implement, note it in the README.

## 4. Pinned design: rendering (client only)

- **Dynamic model.** `FacePaperModel implements IDynamicBakedModel`; `getQuads(state, side, rand, ModelData, layer)`
  builds quads only for `side == null` from the `FACES` snapshot; `useAmbientOcclusion` false; `isGui3d` false;
  particle icon = the plain note sprite; render type cutout. Quads are built with `QuadBakingVertexConsumer` (or by
  filling the int array directly): position, colour white, UV from the sprite, normal, `tintIndex` =
  `face.ordinal() * 5 + spot` for a dyed note and -1 otherwise. Chunk builds run on worker threads: everything the
  model reads must be immutable or thread-safe. Register through a geometry loader (`ModelEvent.RegisterGeometryLoaders`,
  id `creative_labels:face_paper`) referenced from `models/block/face_paper.json` `{"loader": "creative_labels:face_paper"}`.
- **Block colours.** `RegisterColorHandlersEvent.Block`: tint index -> the BE at pos -> that note's RGB (plain
  tint = white, since plain notes use the untinted yellow sprite).
- **Note shape** (`NoteShape`): capture Sticky It's polygons at runtime: `Minecraft.getInstance().getEntityModels()
  .bakeLayer(new ModelLayerLocation(new ResourceLocation("sticky_it", "model_sticky"), "main"))`, then render the
  `head` part into a capturing `VertexConsumer` (records position, u, v, normal) with an identity `PoseStack`.
  Normalise: the flat 6 x 3 px face lies on the paper plane, the whole shape spans 6 px wide, 5.35 px tall, up to
  2.76 px out; map into a spot rectangle in the face frame, lip at the bottom (towards y = 0 on walls; towards the
  rotation's opposite on floors/ceilings, i.e. the curl hangs toward the player who placed it). Fallback when the
  layer is missing: constants (a 3 px flat face, folds of 0.94 px at 20 degrees and 1.41 px at 45 degrees, a 1 px
  lip at 90 degrees). Cache the captured polygons (synchronized lazy init), clear on resource reload.
- **Sprites.** `TextureStitchEvent.Pre` on `TextureAtlas.LOCATION_BLOCKS`: add `sticky_it:entities/sticky`,
  `sticky_it:entities/sticky_gold`, `_creeper`, `_inverted`, `_smile` (they exist in the donor jar as
  `textures/entities/*.png`), plus our generated `creative_labels:generated/note_grey` (greyscale, brightened so a
  tint shows the true dye colour), `creative_labels:generated/note_written` and the four written variants
  (pen strokes drawn over each look), `creative_labels:generated/note_grey_written`. Generated images come from
  `DonorArt`'s in-memory pack (a `MemoryPackResources` like Vinery's, added on `AddPackFindersEvent` for
  CLIENT_RESOURCES at `Pack.Position.BOTTOM`, built lazily from the donor jar's bytes). The label paper uses Labels'
  `labels:block/label` model (register with `ModelEvent.RegisterAdditional`, draw its quads moved onto the face) and
  its `labels:entity/label` texture (already on the block atlas because that model references it).
- **Item icons.** Note item model: `item/generated` with overrides on an item property `creative_labels:look`
  (0 plain -> `sticky_it:item/stickyitem` untinted; 1 dyed -> `creative_labels:generated/note_item_grey` tinted by
  `ItemColors` with `display.color`; 2..5 specials -> `creative_labels:generated/note_item_<special>`, the donor icon
  recoloured in memory with that special's palette). Eraser: `item/generated` with `sticky_it:item/eraser`. Label:
  `{"parent": "labels:item/label"}`.
- **Highlight.** `RenderHighlightEvent.Block`: for `face_paper`, cancel the vanilla outline and draw only the hovered
  piece's box.
- **Label icons** (`FacePaperRenderer`, a `BlockEntityRenderer`): draws, per LABEL face within `pictureDistance`,
  one quad with the picture texture: undyed = `RenderedTexturesManager.requestFlatItemStackTexture(id, stack, 16)`
  (stack NBT tints show); dyed = `requestTexture(id, 16, tex -> { drawItem(tex, stack); tex.download(); IconDye.apply(tex.getPixels(), dye); tex.upload(); }, false)`
  (read `DecompiledMods/moonlight-1.19.2-2.3.7/.../texture_renderer/` first; `download()` reads the framebuffer into
  `getPixels()`, `upload()` pushes the NativeImage back). Cache key = stripped stack + dye; ids under
  `creative_labels:labels/<hash>`; `RenderedTexturesManager.clearCache()` happens on reload by Moonlight itself.
  Glow = full-bright light. Name (feather) with `Font` within `textDistance`. `shouldRender(be, cameraPos)` checks
  `pictureDistance`; `getViewDistance()` returns it; the renderer draws nothing for NOTES faces.
- **IconDye ramp**: luminance of each opaque pixel -> lerp(dark, light) where light = `DyeColor.getFireworkColor()`
  and dark = `DyeColor.getTextColor()` (if equal, darken light by 0.6). Our own code (not Labels' palette classes).
- **Note screen** (`NoteScreen`): Sticky It's `textures/screens/stickygui.png` (352x16) as the text bar and
  `stickyguidatee.png` (70x16) as the date bar above it, drawn centred above the screen centre; an `EditBox` on the
  text bar, dark grey text, max `noteLength`; "Done" sends `NoteTextPacket`. Date bar text: "Day N, <placer>".

## 5. Pinned design: items, recipes, labels on items (phase 2) and labels on paper (phase 3)

- `StickyNoteItem`: stack 64, `DyeableLeatherItem`; `use` in the air: sneaking with an off-hand item -> label it
  (server), else open the screen (client). `useOn` -> placement. `overrideStackedOnOther(stack, slot, action, player)`:
  `ClickAction.SECONDARY` with a written note on the cursor over a non-note, non-empty slot -> `ItemLabels.add(slotStack, text)`
  on the whole stack, note count -1, sound, return true (works in every container incl. the creative tab; Q4:
  backpacks and shulkers are labelled, not opened). `LabelGate`: server side, one cursor labelling per player per 5
  ticks (drops later clicks silently; the server's slot correction resyncs the client).
- `EraserItem`: stack 1, unbreakable (`isDamageable` false), `hasCraftingRemainingItem` true returning itself;
  `overrideStackedOnOther` SECONDARY over a labelled item -> remove the last line.
- `AnvilLabels` (`AnvilUpdateEvent`): left = any item, right = written note -> output with the line, cost
  `anvilCost`, materialCost 1.
- Full (3 lines): refuse with `SoundEvents.NOTE_BLOCK_BASS` and action-bar
  `creative_labels.label.full` ("No room for another label").
- `NoteColours.mix(int oldColor /* -1 none */, DyeColor... dyes)`: vanilla's `DyeableLeatherItem.dyeArmor` maths
  (average, scaled to the brightest). Specials: `NoteColours.SPECIALS` = inverted (ink sac), smile (glow berries),
  creeper (gunpowder), gold (gold nugget).
- Recipes (all `CustomRecipe`, matched in `matches`, output in `assemble`, `canCraftInDimensions` any size):
  `note_dye` N identical notes (same colour/special/text, not special) + exactly 1 dye -> N notes mixed, text kept;
  `note_special` N plain notes (no colour, no special, text allowed and kept) + exactly 1 special ingredient -> N
  specials; `note_copy` 1 written note + exactly 8 blank notes (any colour/special) -> 9 copies of the written one;
  `note_erase` 1 note + 1 eraser -> the note with text wiped, or if blank: colour and special wiped (eraser stays as
  the remainder). The recipe JSONs are NOT in the jar: the pack adds them through a new NCH helper (phase 4).
- JEI plugin: subtype interpreter on `CreativeLabels.Special` (dyed notes are not separate entries, like leather);
  example crafting recipes (16 single-dye, 4 specials, the copy, the erase) registered via `IRecipeRegistration.addRecipes(RecipeTypes.CRAFTING, ...)`
  as `ShapelessRecipe` instances with ids under `creative_labels:jei/...`. Info pages optional.
- Tooltips: `appendHoverText` per sticky_notes.md 3.9, one lang key per line (`creative_labels.tooltip.note.1` ...),
  grey; a labelled item's lines come from an `ItemTooltipEvent` handler (grey, under the name, no header).
- `CraftingMenuResultMixin`: `@Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"), cancellable = true)` on
  `CraftingMenu`; when `blockLabelledCrafting` and any grid stack has `CreativeLabels.Lines`: set the result slot
  empty, send `ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), 0, EMPTY)` to the
  `ServerPlayer`, cancel.
- Labels on paper (3.10): `LabelItem.useOn` places on a five-spot face with no notes (label drops blank when
  broken). `use` on a LABEL face: sneak + item -> picture = stripped copy (not used up); sneak + empty hand -> clear;
  dye -> `dye` (one used); glow ink sac / ink sac -> glow on / off (one used); feather -> toggle `showName`; anything
  else or empty hand -> forward to the host with the hit moved onto its face (chests open through the label).
- WTHIT (`waila_plugins.json`, `creative_labels:face_paper`, client entrypoint, `side: "client"`): a note under the
  crosshair -> "Sticky Note", its colour (name of the nearest DyeColor or the hex) or special, its text, "Day N,
  <placer>"; a label -> `getOverride` returns the host's state (host name and icon) + body line "Label: <item name>".
  Knob: `CLConfig.WTHIT_LINES`.

## 6. Pinned design: config, lang, tests, pack chores

- `creative_labels-common.toml`: `[notes] noteLength=32 (1..256), keepSpecialColor=true, goldChance=0.01 (0..1)`;
  `[item_labels] anvilCost=1 (0..39), blockLabelledCrafting=true`; `[wthit] lines=true`.
  `creative_labels-client.toml`: `[labels] pictureDistance=16.0 (0..64), textDistance=6.0 (0..64)`. Read through
  `CLConfig.on/num/integer` helpers that fall back to defaults when the config isn't loaded (as MMConfig).
- Lang (`en_us.json`): `block.creative_labels.face_paper` "Face Paper", `item.creative_labels.sticky_note` "Sticky Note",
  `item.creative_labels.label` "Label", `item.creative_labels.eraser` "Eraser", tooltip lines (3.9), screen keys
  (`creative_labels.screen.note.done` "Done", `.day` "Day %s, %s"), `creative_labels.label.full`, WTHIT keys,
  JEI subtype names `creative_labels.special.<name>` ("Inverted Sticky Note", "Smiley Sticky Note",
  "Creeper Sticky Note", "Gold Sticky Note"; the item's `getName` uses them when `Special` is set).
- Headless tests `tools/creative_labels_test/`: `SpotsTest.java` + `NbtTest.java` + `run.sh` (classpath from
  `tools/java_test_cp.sh` with `development/creativeLabels/build/classes/java/main` added; `javac -proc:none`; the
  `FMLEnvironment` stub trick from patcherMod's tests if config values are touched). Cover: the host table (full,
  1/16 inset, stairs per facing and half, slabs top/bottom/sides, double slab, posts, deny tag path by a fake
  predicate), floor rotation of spots, covered-face rule, NBT round trips (display.color, CreativeLabels.Text /
  Special / Lines, tag removed when empty), colour mixing against vanilla's formula, the four recipe matchers if
  constructible without registries (else skip and say so).
- Pack chores (phase 4 agent): root `settings.gradle` include (scaffold does this); `tools/build_deploy.py`
  `IN_TREE_MODS` row `('creativeLabels', 'creativeLabels', True)`; `mod_sides.md` row (`both`, "Creative Labels",
  `creative_labels`, jar, note) + update the two donor rows' notes ("art donor for Creative Labels, gameplay off")
  + the "Current count" line; `development/patcherMod/.../CEConfig.java` `[mod_names]` default list entry
  `creative_labels=Creative Labels (Alfonzo)` (alphabetical place); `instance.json` user-added mods entry (copy the
  moreMooblooms block shape, name/file = `creativeLabels-1.0.0.jar`); `python3 tools/gen_pack_manifest.py` AFTER
  the jar is in `mods/` (the orchestrator runs it; do not run it before). KubeJS: `kubejs/startup_scripts/_lib/item_dict.js`
  keys before `// Validation`: `i.cl_sticky_note`, `i.cl_label`, `i.cl_eraser` under a `// ===== creative_labels =====`
  header, and `i.sticky_it_sticky_note`, `i.sticky_it_eraser`, `i.labels_label` if absent (check: `sticky_it` may
  already be mapped; grep first); `kubejs/startup_scripts/_lib/removed_items.js`: add the three donor items (JEI hide
  + red tooltip); `kubejs/startup_scripts/func/neoCraftHelper.js`: a helper `creativeLabelsSpecial(type, e, idOverride)`
  emitting `e.custom({type: 'creative_labels:' + type}).id(...)`; `kubejs/server_scripts/other/creative_labels.js`:
  `R.remove({mod: 'sticky_it'}, e)`, `R.remove({mod: 'labels'}, e)`, `R.shaped` for the note (4 paper + 3 iron
  nuggets -> 2, pattern ` aa`/`bba`/`bb `), eraser (6 dried kelp, ` aa`/`aaa`/`aa `), label (8 paper around
  `supplementaries:antique_ink` -> 16), the four special recipes through the new helper. Rhino rules: `var` in loop
  bodies, unique top-level names, read `global.item` as `var i = global.item`, no `const` at top level with a
  name used elsewhere.

## 7. Stage checklist

Every build stage: read this file and the spec sections it names; build; run the headless tests; leave the tree
compiling; return a short report (files touched, what is not done, anything the spec left unclear and what was
chosen).
