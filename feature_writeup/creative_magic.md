# Creative Magic: enchanting, magic recipes and Botania tweaks in one mod

Written 2026-11-01 from the Creative Split audit ([creative_split.md](creative_split.md) 2.3 and [split/](split/README.md)).
**Status: carved 2026-11-04 (wave 6, creative_split.md 7.11), built, installed 2026-11-04 19:17
(`mods/creativemagic-1.0.0.jar`, with patcherMod 1.1.09); nothing booted; scratch SQ-CMG queued.** The full sweep is
[split/H_magic.md](split/H_magic.md). Creative Common never holds these rows.

Written for: the owner and agents working on the split. Own mod `creativemagic` (development/creativeMagic,
`creativemagic-1.0.0.jar`, side both), package `com.SaintAlfonzo.creativemagic`, config `creativemagic-common.toml`,
script packs `kubejs_startup/creativemagic/`, `kubejs/data/creativemagic/kubejs/`, `kubejs/assets/creativemagic/kubejs/`.
Same behaviour as before the carve; every registry id keeps `creativeengineering:`. Potions, effects, alcohol and gear
effects are Creative Brewing's ([creative_brewing.md](creative_brewing.md) 4.2).

## 1. What it holds

- **Java** (7 classes plus 3 new): `block/DarkFireBlock`, `item/FlintAndTerraSteelItem` (was package `items`, the
  TEMPORARY header kept), `dimension/PortalFailureHandler` ([nether_portal.md](nether_portal.md); its
  `@EventBusSubscriber` now names `creativemagic`), `compat/TrinketCaseRules`. New: `CreativeMagic` (main class: its
  own `DeferredRegister`s for blocks and items under namespace `creativeengineering`, registering `dark_fire` and
  `flint_and_terrasteel`; patcherMod's `CreativeEngineering.BLOCKS` holder and `block/CreativeEngineeringBlocks` are
  gone), `CMGConfig`, `MagicMixinPlugin`.
- **Mixins:** `creativemagic.mixins.json`, 7 common: `CycleEnchantmentMixin`, `StockpileEnchantmentMixin`,
  `RecoveryEnchantmentMixin` (gated on `nock_enough_arrows`), `TrinketCaseInventoryMixin`, `TrinketCaseSlotMixin`,
  `TrinketCaseMenuMixin`, `ItemBackedInventoryAccessor` (gated on `botania`).
- **Config:** `[botania]` (`trinketCaseAnyItem`) in `config/creativemagic-common.toml`, carried from the CE file on the
  first boot (`ConfigRename.carrySections`); `configs_edited/root_config/creativemagic-common.toml`.
- **Resources:** Dark Fire blockstate, models and animated textures, Flint and Terrasteel model and texture. The pack
  folder's duplicate Dark Fire textures (`kubejs/assets/creativeengineering/textures/block/dark_fire_*`) were deleted;
  `dark_fire_0.png` there differed from the jar's copy and was the one players saw, so the jar now ships it.
- **Scripts:** startup `cmg_core.js` (`global.CMG`: `modId`, `on()`, `recipes()`); server `magic/enchants.js`,
  `progression.js`, `sculk.js`, `storage.js`, `xp.js` (recipe ids now `creativemagic:<type>/<suffix>`) and
  `tags/fluidtags.js` (the `forge:fluid/xp`, `forge:fluid/hyper_xp` rows and the XP fluid merge); client `names.js`
  (Flint and Terrasteel, Dark Fire), `tooltips.js` (vine ball, Trinket Case), `jei.js` (the red line on books with the
  three NEA enchantments).

## 2. Not in this jar

- `magic/dragon_breathing.js` (sweep H D1): stays in the pack folder for Engineering (`fan/` dragon breathing).
- D3, the liquid XP bridge (`fluid/FluidBridge`, `XpHelperMixin`, `XpPumpUpgradeWrapperMixin`, `XPFluidManagerMixin`,
  `MemoryStickItemMixin`, `PneumaticCraftUtilsMixin`): **settled 2026-11-04 (owner): Creative Engineering**, against
  the audit's Magic lean; moved in wave 8 ([creative_engineering.md](creative_engineering.md)). The XP tag merge and
  `xp.js` stay Magic's.
- D2 (armor enchantability) and D4 (bone meal charm) went to Creative Equipment, per their leans.
- `magic/armor_effects.js`, `tool_effects.js`, `lava_vision.js`, Magic Resistance: Creative Brewing.

## 3. Edges

Magic reads Common (`global.NCH`, `global.item`, `global.fluid`, `global.tags`, `global.tooltips`, `swap/ConfigRename`)
and nothing from a content jar; nothing reads a Magic class. Requires `creativeengineering [1.1.09,)` and
`creativecompat [1.0.0,)`, mandatory AFTER. `tools/trinket_case_probe.js` loads `creativemagic.compat.TrinketCaseRules`
and `CMGConfig`.

## 4. Tests

Scratch SQ-CMG-01 to -05 ([scratch_queue.md](testing/scratch_queue.md)); the older rows still apply: NP-01 to NP-12
(portal), SQ-TC (Trinket Case probe), RA-01 to RA-08.

## 5. History

- 2026-11-01: reserved from the split audit; same day the owner made it a carve of its own (7.6).
- 2026-11-04: carved (wave 6). patcherMod 1.1.09 loses the rows above.
