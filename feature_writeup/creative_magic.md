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

## 0. At a glance

<!-- generated:status_index begin open_work sha1=59e7fa073899 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 11 open TODO rows (Planned 1, Testing 10), 2 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creativesplit/deps #27` | Planned | S C | Audit leftovers: soft Java references, assets, pack files naming Creative jars |
| `creativesplit/audit #1` | Testing | none | Audit patcherMod and the pack folders for the split: carve Engineering, Equipment, Worldgen, Compat, Calendar, Devtools (and CD / CB rows) first, the remainder becomes Creative Common |
| `creativesplit/cdfinish #11` | Testing | S | Finish Creative Delight: campfire from Compat to CD, Nether's Delight repacked as a CD member, Ender's and Cooking for Blockheads leftover rows into CD |
| `creativesplit/deps #26` | Testing | S | Data that names an optional mod sits behind a guard (audit) |
| `creativesplit/ids #23` | Testing | S C | Ids that name the wrong owner jar: event subscriber mod ids, WTHIT plugin and option ids, JEI plugin uids and category ids, network channels; `ce` / `CE` commands, classes, member mod ids and constants outside Engineering |
| `creativesplit/ids #24` | Testing | S C P | Creative Delight content to the `creativedelight` namespace |
| `creativesplit/ids #25` | Testing | S C P | Other jars to their own namespaces |
| `creativesplit/magic #12` | Testing | none | Carve Creative Magic |
| `creativesplit/namespace #13` | Testing | S | Move Creative Common's data to the `creativecommon` namespace; meal tooltips to CD, two fixes to Compat, Curios slots to Equipment |
| `creativesplit/prep #3` | Testing | S | Prep in patcherMod before the first carve: GatedMixinPlugin replaces CEMixinPlugin's accept-all, NCH.cfg per-jar config registration, the Common APIs of creative_split.md 7.2; CD and CB rename |
| `creativesplit/regression #14` | Testing | S C | Feature regression of every Creative jar after the split |
<!-- generated:status_index end open_work -->

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

Scratch run r1114 (2026-11-14, [scratch_run_2026-11-14.md](testing/scratch_run_2026-11-14.md) 8): SQ-CMG-02, -02b PASS (carry lines and kept values; second boot carries nothing).

## 5. History

- 2026-11-01: reserved from the split audit; same day the owner made it a carve of its own (7.6).
- 2026-11-04: carved (wave 6). patcherMod 1.1.09 loses the rows above.
