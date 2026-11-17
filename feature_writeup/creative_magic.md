# Creative Magic: enchanting, magic recipes and Botania tweaks in one mod

Written 2026-11-01 from the Creative Split audit ([creative_split.md](creative_split.md) 2.3 and [split/](split/README.md)).
**Status: carved 2026-11-04 (wave 6, creative_split.md 7.11), built, installed 2026-11-04 19:17
(`mods/creativemagic-1.0.0.jar`, with patcherMod 1.1.09); nothing booted; scratch SQ-CMG queued.** The full sweep is
[split/H_magic.md](split/H_magic.md). Creative Common never holds these rows.

Written for: the owner and agents working on the split. Own mod `creativemagic` (development/creativeMagic,
`creativemagic-1.0.0.jar`, side both), package `com.SaintAlfonzo.creativemagic`, config `creativemagic-common.toml`,
script packs `kubejs_startup/creativemagic/`, `kubejs/data/creativemagic/kubejs/`, `kubejs/assets/creativemagic/kubejs/`.
Same behavior as before the carve; every registry id keeps `creativeengineering:`. Potions, effects, alcohol and gear
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

Lines moved out of code comments (2026-11-17, plan_rules_fixes.md section 17):

- 2026-11-04: Creative Magic; moved from kubejs/server_scripts/magic/xp.js on 2026-11-04 (creative_split.md 7.6, wave 6), rows unchanged (kubejs/data/creativemagic/kubejs/magic/xp.js)
- 2026-11-04: Creative Magic: Botania tooltips; moved from kubejs/client_scripts/tooltips.js on 2026-11-04, rows unchanged (kubejs/assets/creativemagic/kubejs/tooltips.js)
- 2026-11-04: The mixins moved from patcherMod on 2026-11-04 unchanged apart from their package (SRG names, remap = false, creativeengineering$ member prefixes) (development/creativeMagic/src/main/java/com/SaintAlfonzo/creativemagic/MagicMixinPlugin.java)
- 2026-11-04: Creative Magic: display names; moved from kubejs/client_scripts/namechanges.js on 2026-11-04 (kubejs/assets/creativemagic/kubejs/names.js)
- 2026-11-04: Creative Magic; moved from kubejs/server_scripts/magic/enchants.js on 2026-11-04 (creative_split.md 7.6, wave 6), rows unchanged (kubejs/data/creativemagic/kubejs/magic/enchants.js)
- 2026-11-04: creativeMagic: Creative Magic (mod id creativemagic, feature_writeup/creative_magic.md), carved out of patcherMod on 2026-11-04 (creative_split.md 7.6, wave 6) (development/creativeMagic/build.gradle)
- 2026-11-04: Creative Magic (feature_writeup/creative_magic.md): enchanting, magic recipes and the Botania tweaks, carved out of patcherMod on 2026-11-04 (creative_split.md 7.6, wave 6) (development/creativeMagic/src/main/java/com/SaintAlfonzo/creativemagic/CreativeMagic.java)
- 2026-11-04: Creative Magic; moved from kubejs/server_scripts/magic/sculk.js on 2026-11-04 (creative_split.md 7.6, wave 6), rows unchanged (kubejs/data/creativemagic/kubejs/magic/sculk.js)
- 2026-11-04: Creative Magic; moved from kubejs/server_scripts/magic/storage.js on 2026-11-04 (creative_split.md 7.6, wave 6), rows unchanged (kubejs/data/creativemagic/kubejs/magic/storage.js)
- 2026-11-04: Creative Magic: the client half of magic/enchants.js and the three Nock Enough Arrows enchantment mixins; moved from kubejs/client_scripts/jeichanges.js on 2026-11-04, rows unchanged (kubejs/assets/creativemagic/kubejs/jei.js)
- 2026-11-04: Creative Magic; moved from kubejs/server_scripts/tags/fluidtags.js on 2026-11-04 (creative_split.md 7.6, wave 6), rows unchanged: the experience fluid tags and the XP fluid merge (kubejs/data/creativemagic/kubejs/tags/fluidtags.js)
- 2026-11-04: Creative Magic; moved from kubejs/server_scripts/magic/progression.js on 2026-11-04 (creative_split.md 7.6, wave 6), rows unchanged (kubejs/data/creativemagic/kubejs/magic/progression.js)
- 2026-11-04: Creative Magic's COMMON config, {@code config/creativemagic-common.toml}. {@code [botania]} moved out of patcherMod's creativeengineering-common.toml on 2026-11-04 (creative_split.md 7.6, wave 6) with its key, comment and default unchanged (development/creativeMagic/src/main/java/com/SaintAlfonzo/creativemagic/CMGConfig.java)
- 2026-11-06: Dark Fire and Flint and Terrasteel with the Nether portal rule (dimension/PortalFailureHandler: ordinary fire no longer opens a portal), the Nock Enough Arrows enchantments taken off tables and trades, the Botania Trinket Case holding any item ({@code [botania]}), and the liquid XP bridge (PneumaticCraft and Sophisticated Core use Create Enchantment Industry's experience fluid; fluid/FluidBridge, from Creative Engineering 2026-11-06) (development/creativeMagic/src/main/java/com/SaintAlfonzo/creativemagic/CreativeMagic.java)
- 2026-11-06: Liquid XP bridge (fluid/FluidBridge), from Creative Engineering 2026-11-06 (development/creativeMagic/src/main/java/com/SaintAlfonzo/creativemagic/MagicMixinPlugin.java)
- 2026-11-07: The gate returns false (no IP portal) and posts the event vanilla would have posted, at the moment the fire lands, so the frame fails before Smaller Nether Portals' delayed flint-and-steel task looks at it (that task fills the frame with portal blocks itself and ignores a cancelled event; found 2026-11-07 in latest.log) (development/creativeMagic/src/main/java/com/SaintAlfonzo/creativemagic/mixin/IPFireGateMixin.java)
- 2026-11-10: Dark Fire and Flint and Terrasteel are registered under this mod's namespace ({@link #NS}); until the 2026-11-10 namespace move (feature_writeup/namespace_move.md) they were {@code creativeengineering}, and the swap list data/creativemagic/swaps/owner_namespace.json carries old saves (development/creativeMagic/src/main/java/com/SaintAlfonzo/creativemagic/CreativeMagic.java)
- 2026-11-10: The namespace of every id this mod registers (was creativeengineering until the 2026-11-10 namespace move) (development/creativeMagic/src/main/java/com/SaintAlfonzo/creativemagic/CreativeMagic.java)
