# Plan: Yuushya Modelling 2.4.1 backport (phases A and B)

Written 2026-11-25 from the owner's request (chat 2026-11-25) and the report [yuushya_modelling.md](yuushya_modelling.md)
(changes, bugs B1 to B4, tests `YMB-`). Nothing built yet. The fork replaces `yuushya-modelling-1.19.2-forge-2.2.0.jar`
in `mods/` and `server_mods/`. Rows: `mod-integration #29` (phase A), `mod-integration #36` (phase B).

## 0. Intake

<!-- generated:status_index begin open_work sha1=facaaaf1ecc7 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 2 open TODO rows (Planned 2), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `mod-integration #29` | Planned | S C P | Yuushya Modelling 2.4.1 backport phase A: Entity Block options, Item Block, primitives, Color Picker, History List, bugs B1 to B4 |
| `mod-integration #36` | Planned | S C P | Yuushya Modelling 2.4.1 backport phase B: Text Block (font path rewrite for 1.19.2) |
<!-- generated:status_index end open_work -->

| # | Item (owner's words) | Class | Note |
|---|---|---|---|
| 1 | "build a plan to execute phase a and plan b" | clear | Report D1: A = Entity Block options, Item Block, primitives, Color Picker, History List, reload command; B = Text Block. |
| 2 | "keep the mod id" | clear | Report D2: `yuushya_modelling`, item namespace `yuushya`, every id unchanged. |
| 3 | "drop anything unused from the newer version" | premise | The report's D4 called the AnvilCraft-derived render cache unused. False: `NeoItemBlockModel` (in-world path, `pos != null`) hands every placed Item Block to `CustomRenderInstance`, `LevelRendererMixin.callRebuild` feeds `CachedModeClient`, and `CacheableBERenderingPipeline` draws it. It is the Item Block's world renderer. Asked. |
| 4 | "fix all the upstream bugs" | clear | B1 to B4 in the report section 1.5. B4 (the `cube_12` lang key with no item) is fixed by removing the key, which item 3 also covers. |

Truly unused in 2.4.1 (premise check, decompiled source, 2026-11-25): class `item/GetBlockStateItemForge` (the registry
uses `GetBlockStateItem`), class `client/ByteBufferBuilder` (no references), blockstate property
`YuushyaBlockStates.DISTANCE` (declared, on no block), lang key `item.yuushya.cube_12` in 4 languages, item textures
`blueprint`, `pos_trans_tool`, `tool_bundle` (no model uses them; also unused in 2.2), the 1.20-only access transformer
entries, the 1.21 data folders (replaced, B1). `RenderInsideEvent` is used (Forge event subscriber: no suffocation
overlay inside model blocks).

## 1. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| R1 | "execute phase a and plan b" | clear | Both phases; A first, B after A is installed and passes its tests. | 2026-11-25 | chat 2026-11-25 |
| R2 | "keep the mod id" | clear | Mod id `yuushya_modelling`, namespace `yuushya`, all ids, block entity keys and item NBT keys as upstream. | 2026-11-25 | chat 2026-11-25 |
| R3 | "drop anything unused" (render cache) | premise | Keep the cache (it renders Item Blocks); drop only code, properties and keys nothing references. | 2026-11-25 | AskUserQuestion 2026-11-25 |
| R4 | "drop anything unused" (3 unused textures) | clear after preview | Drop `blueprint`, `pos_trans_tool`, `tool_bundle`; update unused_textures.md when the fork is installed. | 2026-11-25 | AskUserQuestion 2026-11-25 (sheet shown) |
| R5 | Upstream recipes (report D5) | open | Keep upstream: Item Block x8, Color Picker, `cube_0`, 31 stonecutter primitives, converted to 1.19.2. | 2026-11-25 | AskUserQuestion 2026-11-25 |
| R6 | Primitives (chibi and anatomy parts) | open | Port all 34. | 2026-11-25 | AskUserQuestion 2026-11-25 |
| R7 | "fix all the upstream bugs" | clear | B1 to B4 fixed (section 3). | 2026-11-25 | chat 2026-11-25 |
| R8 | Folder and jar name (report D3) | clear | `development/backportedYuushyaModelling`, jar `backportedYuushyaModelling-2.4.1-ce-19.2-NN.jar`. | 2026-11-25 | agent default, unconfirmed |
| R9 | New item names (report D6) | clear | Title case through the names.md audit only, like the 2 existing Yuushya rows. | 2026-11-25 | agent default, unconfirmed |
| R10 | History List storage (report D7) | clear | Keep; phase A checks it is held in memory or bounded, not grown on disk. | 2026-11-25 | agent default, unconfirmed |
| R11 | `shape_item` from the 1.21.1 build (report D8) | clear | Not ported; it is not in the 1.20.1 code. | 2026-11-25 | agent default, unconfirmed |

## 2. Approach

- **License route** (skill `port`): upstream is MIT, code and assets. The fork ships code, assets and data in one jar;
  `NOTICE-upstream-MIT.txt` keeps upstream's notice and authors; our changed files are GPL-3.0-only (SPDX header).
  `PORT_NOTES.md` in the project lists every deviation from 2.4.1. Model: `development/backportedTempad` (project
  layout, `gradle.properties` version scheme), except that this fork carries its own art and data (no runtime donor).
- **Source**: the 2.4 source is not public (GitHub stops at 2.2.0). Start from `tools/decompile_mod.py` output of
  `DecompiledMods/yuushya_modelling-1.20.1-2.4.1.jar` (133 files, 15.2k lines). 99 distinct 1.20-only SRG names (713
  uses) stay unmapped there; each becomes its 1.19.2 Mojmap call. 2.2's decompile (`mods/` jar) is the reference for
  every 1.19.2 form the old code already had (`ShowBlockModel` math, `UnbakedModel` signatures, creative tab, screens).
- **Build**: `id 'ce.forge-mod-refmap'` (mixins), Forge 43, no Architectury. Root `settings.gradle` include,
  `installToMods`, `IN_TREE_MODS` in `tools/build_deploy.py`, `replaces_jars` covering the 2.2.0 jar name so the
  install moves it to `jar_archive/displaced/` from both `mods/` and `server_mods/`.
- **One code owner** at a time (memory `orchestrated-port-pattern`): phase agents run one after another, one session
  in `impl`.

## 3. Phase A

Order: each step compiles before the next starts. Text Block code is not in phase A (not registered; its classes stay
out of the source set until B).

| Step | Work | Files (under `src/main/java/com/yuushya/modelling/`) |
|---|---|---|
| A0 | Project skeleton: build.gradle, gradle.properties (`mod_id=yuushya_modelling`, `mod_version=2.4.1-ce-19.2-01`), mods.toml (Forge 43, MC 1.19.2, no Architectury, no optional `yuushya` dep), LICENSE (GPL-3.0), NOTICE-upstream-MIT.txt, PORT_NOTES.md, settings.gradle include, `IN_TREE_MODS` row, `replaces_jars`. | project root |
| A1 | Registries and blocks: `BuiltInRegistries` -> `Registry`, `ResourceLocation.fromNamespaceAndPath` -> constructor, `Properties.of()` -> `Properties.of(Material.METAL)` (2.2 value), drop `forceSolidOn`, creative tabs `modelling` and `primitive` as `CreativeModeTab` subclasses, `BlockShape`, `AbstractTransformBlock` (`shapes`, `enable_ao`, `full_block`), Entity Block and Item Block, block entities, `TransformBlockData` / `TransformItemData`, `YuushyaDataTags`, `ShareUtils`. Drop `DISTANCE` and `GetBlockStateItemForge`. | `registries/`, `block/`, `blockentity/` (not `textblock/`), `utils/` |
| A2 | Network: `SimpleChannel` `yuushya_modelling:main`, block and item packets, `UpdateAOPacket`, `ReloadModelPacket`; text packets wait for B. Protocol version string bumped from 2.2's so a 2.2 client is refused cleanly. | `network/` |
| A3 | Math: JOML -> `com.mojang.math` (`Vector3f`, `Vector4f`, `Quaternion`, `Matrix4f`, `Vector3f.YP.rotationDegrees`); `Vector3d` -> `com.mojang.math.Vector3d` as in 2.2's `TransformData`. NBT layout unchanged (`ShowPos`, `ShowRotation`, `ShowScales`, `BlockState`, `isShown`, `ControlSlot`). | 24 files, mostly `utils/YuushyaUtils`, models, renderers |
| A4 | Models and rendering: `ShowBlockModel` / `NeoShowBlockModel`, `ItemBlockModel` / `NeoItemBlockModel` (`UnbakedModel` 1.19.2 signatures from 2.2), `ModelEvent.ModifyBakingResult` wrapping, `getRenderPasses` for items, BE renderers, `CustomRenderInstance` and the cached pipeline (`client/anvilcraft/rendering/`, `LevelRendererMixin`, `MinecraftMixin`: 1.19.2 `renderLevel` and `updateLevelInEngines` targets). Drop `ByteBufferBuilder`. **B2 fix** here. | `client/`, `blockentity/*/…Render`, `mixin/` |
| A5 | Items: tools (unchanged behavior), `ColorPickerItem`, `HistoryItem` + `HistoryMenu`, 34 primitives (stack 16, rare), `GetBlockStateItem`, `YuushyaDebugStickItem`, Destroy item also on Item Blocks. R10 check of the history list. | `item/`, `gui/history/` |
| A6 | Screens (the bulk, about 7k lines): `GuiGraphics` -> `PoseStack` + `GuiComponent`; `Button.builder` / `Tooltip` -> 1.19.2 `Button` with `OnTooltip`; `AbstractEngraveScreen` / `EngraveScreen`, `ShowBlockScreen` (shape cycle, Fine Tune and Input modes, face count, copy), `ItemBlockScreen` (color, brightness, AO, full-block AO, mirror, custom size, pre-apply), `HistoryScreen`, widgets (`ColorWidget`, sliders, icon lists, transform components). | `gui/` (not `gui/textblock/`, not text widgets) |
| A7 | Commands and events: `/yuushya_reload_model reload all\|blocks\|items` (texts in B), `ClientEvent`, `GuiOverlayEvent`, `RenderInsideEvent`. | `command/`, `event/` |
| A8 | Resources: assets from the 2.4.1 jar minus the 3 dropped textures (R4) and minus Text Block assets; lang without `cube_12` (B4); `pack.mcmeta` format 9; access transformer rebuilt for 1.19.2 (only entries phase A uses); mixin config with `BlockBehaviourMixin` in the common list (**B3 fix**). | `src/main/resources/` |
| A9 | Data, **B1 fix**: `data/yuushya/recipes/` and `loot_tables/` (plural); results `{"item": ...}`, stonecutting `"result": "<id>", "count": n`; no `category`; all 52 recipes; loot `blocks/showblock`, `blocks/itemblock`. Pack rule check: the Shimmering Pearl gold-tag rule still matches. | `src/main/resources/data/` |
| A10 | Checks and install: `./gradlew :backportedYuushyaModelling:build --offline`; headless data check (every recipe and loot JSON parses as 1.19.2 format: a small `tools/` test wired into `check_all.py`, or an existing JSON lint if one covers it); `installToMods`; `/dumpall`; generators (item physics families, stack sizes, names audit). | |
| A11 | Look-check (intake section 5): client capture of the Entity Block screen (all three modes, shape button), the Item Block screen (color mode), the History screen and an Item Block build in the world; owner sees them before test rows are written. | `tools/client_capture/` |
| A12 | Docs and queue: report section 1 status, `SQ-YMB-` / `CQ-YMB-` rows for YMB-01 to -13, mod_sides row (jar, source, `both`), unused_textures.md (R4), names.md rows (R9), stack_sizes.md, toasts.md, loot census, changelog family "Yuushya Modelling", features.csv jars, `status_index.py`. | |

### Bug fixes (all in phase A except where noted)

| Bug | Fix | Test |
|---|---|---|
| B1 1.21 data in the 1.20.1 jar | A9 conversion | YMB-10 |
| B2 item model cache keyed by `ItemStack` identity, never cleared | Key by the transform-data tag's string form (or its hash plus equality on the tag), LRU-bound (256 entries), clear on resource reload (`RegisterClientReloadListenersEvent`); `HashMap` -> concurrent map in `NeoShowBlockModel` | YMB-07 |
| B3 full-block flag client-only | `BlockBehaviourMixin` to the common mixin list; collision and suffocation agree on a dedicated server | YMB-08 |
| B4 `cube_12` lang key without an item | Removed from all 4 lang files | none (lang lint) |

## 4. Phase B: Text Block

Starts after phase A is installed and YMB-01 to YMB-13 pass.

| Step | Work |
|---|---|
| B0 | Register `yuushya:textblock`, its block entity (`textblockentity`), `TextBlockItem`, blockstate and model (`block/blank`). |
| B1 | Text data: `TransformTextData`, `TextTransformType`, `ITransformTextDataInventory`, text packets (`TextLinesPacket`, `TextTransformDataOncePacket`), Engrave text results (`EngraveTextResult*`), reload command `texts`. |
| B2 | Font path, the risk: 1.19.2 has no `GlyphRenderTypes`; `BakedGlyph` holds its normal, see-through and polygon-offset render types itself. Rewrite `GlyphRenderTypesMixin` as a mixin (or accessor) on 1.19.2 `BakedGlyph` / `Font` that swaps in the custom text render type; `TextShader` and `CustomizableTextureState` on 1.19.2 `RenderType.CompositeState`; `FontRenderUtil` on 1.19.2 `Font.renderChar` (mojang `Matrix4f`); font list from `FontManager.fontSets`. Access transformer entries for these, 1.19.2 names. Spike first: one line of text in one font on a placed block, then the rest. |
| B3 | Renderers: `TextBlockEntityRender`, `TextBlockSpecialRender`, `SimpleGeneratedModel` (item backdrop), mirror and cull face. |
| B4 | Screens: `TextBlockScreen`, `StyledMultiLineEditBox` / `StyledMultilineTextField` (1.19.3 multiline field ported to 1.19.2 widgets), `FontList`, `TextIconList`, `TextTransformComponent`. |
| B5 | Assets and lang for the Text Block; look-check capture of the Text Block screen and a placed block with two fonts. |
| B6 | Docs, YMB-14 queue rows, changelog bullet, rebuild and install. |

## 5. Tests

The report's section 3 holds YMB-01 to YMB-14 (owner doc). Phase A: YMB-01 to YMB-13. Phase B: YMB-14 (more text tests
are added to the report when B2's spike shows what the font path supports). Queue rows are written in A12 and B6 after
the look-checks.

## 6. Risks

- **Screens size**: 7k lines through a GUI API change. The 2.2 screens show the 1.19.2 forms of the same widgets;
  new widgets (color picker, icon lists) follow them.
- **Cached render pipeline** targets 1.20 `LevelRenderer` internals; if the 1.19.2 targets do not fit, fall back to
  drawing Item Blocks from their block entity renderer (correct, slower for large builds) and record it in
  PORT_NOTES.md.
- **Text Block font path** (B2): may need a narrower feature set (for example fonts limited to bitmap fonts). The spike
  decides; anything dropped goes to the owner before B continues.
- **Save compatibility**: new blockstate properties load with defaults (No Collision, AO off), block entity and item
  NBT keys are unchanged (YMB-01). No conversions (CLAUDE.md).

## 7. TODO rows

- `mod-integration #29`: phase A (this plan section 3).
- `mod-integration #36`: phase B (section 4).

## History

- 2026-11-25: plan written; rulings R1 to R7 from chat and AskUserQuestion, R8 to R11 agent defaults. The report's D4
  (render cache unused) corrected: the cache is the Item Block renderer and stays.
- 2026-11-25: A0 to A3 built (`./gradlew :backportedYuushyaModelling:build --offline` passes; not installed). Skeleton,
  settings include, `IN_TREE_MODS` row (required=False until A10), `replaces_jars=yuushya-modelling-*.jar`; registries,
  blocks, block entities, transform data, utils, network (protocol `2.4.1-ce-1`), JOML -> `com.mojang.math` in those
  classes. Deviations: the item classes, engrave/history menus and loaders, the reload command and `ClientEvent` (item
  properties only) came in early so the registries compile (A5/A7 review them); `BlockBehaviourMixin` came in with the
  mixin config now (B3, common list); `GetBlockStateItem.renderByItem` dropped with `GetBlockStateItemForge`. The
  plan's "34 primitives" are 32 in the 2.4.1 registry. API map, stubs and phase state: the project's PORT_NOTES.md.
