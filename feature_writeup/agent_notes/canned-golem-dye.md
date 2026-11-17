# canned-golem-dye

> Canned Goods canned golem is dyeable (synced byte "Color", default red) via patcherMod mixins; dyed textures in kubejs/assets

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Built 2026-10-02; status: cans.md section 0 (DEPLOYED with 1.0.4, R4-07).
- `mixin/CannedGolemColorMixin` (common): adds a synced BYTE entity data + NBT "Color" (DyeColor id, default RED) to `net.mcreator.cannedgoods.entity.CannedGolemEntity`; right-click with any dye (`DyeColor.getColor(stack)`, so #forge:dyes/* also work) recolours it, eats one dye unless creative. The entity overrides none of defineSynchedData/save/load/mobInteract, so the mixin adds them as plain overrides (extends PathfinderMob; reobf gives SRG names).
- `mixin/CannedGolemRendererMixin` (client): HEAD inject on the typed `getTextureLocation(CannedGolemEntity)` (keeps its plain name in the jar; `m_5478_` is the bridge).
- `compat/DyeableCannedGolem` interface + texture lookup. Red = the mod's `big_canned_boy.png`; the other 15 come from `kubejs/assets/creativeengineering/textures/entity/canned_golem/<color>.png` (moved there from the user's loose `kubejs/assets/<color>_can.png`).
- `CannedGoods-1.19.2-3.jar` copied into patcherMod/libs. Pattern: [patchermod-coremod](patchermod-coremod.md).

2026-10-02: this ships as patcherMod **1.0.4** (pack 1.0.4), not 1.0.3. See [release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md).
