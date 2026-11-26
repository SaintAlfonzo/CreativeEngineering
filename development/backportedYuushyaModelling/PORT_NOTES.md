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

Decompiled 1.20 code has no `@Override`: a 1.20 override whose 1.19.2 signature differs would compile as a dead new
method. Every ported class was checked by reflection against the 1.19.2 supertypes (no such case in A0 to A3); later
steps should rerun the same check on their classes (a small `OverrideCheck` over `build/classes`, compile classpath
from Gradle).

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
- Not in the source set yet: `client/` (models, render cache, font utils), `*Render` and `*Model` classes, all
  screens and widgets (`gui/**` except the menus, loaders and results), `gui/validate/`, `event/GuiOverlayEvent`,
  `event/RenderInsideEvent`, `LevelRendererMixin`, `MinecraftMixin`, `YuushyaClientNeoForge`, resources (A8) and data
  (A9). The jar is not playable until A8.
