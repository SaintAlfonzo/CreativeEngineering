# Creative Equipment

Written 2026-11-04 (Creative Split wave 7, [creative_split.md](creative_split.md) 7.6 and 7.12; TODO `creativesplit/equipment #9`).
Status: **carved 2026-11-04, built, installed 2026-11-04 19:17 (`mods/creativeequipment-1.0.0.jar`, with patcherMod 1.1.09 and Creative Magic); nothing booted; scratch SQ-CEQ queued.**

Written for: the owner and agents working on the split. Own mod `creativeequipment` (development/creativeEquipment,
`creativeequipment-1.0.0.jar`, side both): the pack's gear. Same behaviour as before the carve; every registry id keeps
`creativeengineering:`. The Magic rows that were to ride here (2.3) went to their own jar, Creative Magic, one wave
earlier ([creative_magic.md](creative_magic.md)).

## 1. What it holds

- **Java** (`com.SaintAlfonzo.creativeequipment`, same subpackages as in patcherMod; 35 classes plus 3 new):
  - `shield/` (tier shields, all but `EffectTickContext`) with `shield/client/`; `stepstool/` with `stepstool/client/`
    (the `creativeequipment:stepstool` network channel, renamed 2026-11-07, plan_owner_ids.md); `backpackskin/` (7); `gear/` (air brake,
    elytra duplication and its serializer); `item/BottledAir`; `CEQTiers` (copper tool tier, registered in this
    constructor); `util/ArmorEnchantabilityBridge`, `BonemealCharms`, `ElytraChestplates`;
    `compat/NockRecipeBookSync` (still needed: it fixes the RecipeBookType count in the recipe packet, which the
    recipe book's removal from the UI does not touch).
  - New: `CreativeEquipment` (main class, every registration that left `CreativeEngineering.java`, same order:
    serializers, shield items, backpack skins, stepstool channel and Curios listeners, air brake, Bottled Air, shield
    listeners, copper tier; client: shield item properties, stepstool key and client tick), `CEQConfig`,
    `EquipmentMixinPlugin` (a `GatedMixinPlugin`).
- **Mixins:** `creativeequipment.mixins.json`, 20 common, none client, SRG with `remap = false`, member prefixes still
  `creativeengineering$`: the 20 sweep B Equipment rows (stepstool 2, shields 6, backpack skins 6, Easy Elytra Takeoff 2,
  copper pickaxe, armor enchantability, armor cap, bone meal charm). Gated on their mod: Sophisticated Backpacks,
  Easy Elytra Takeoff, Everything Copper. The rest target vanilla.
- **Config:** `config/creativeequipment-common.toml` (`CEQConfig`): `[dragon_gear]`, `[air_brake]`, `[stepstool]`,
  `[crafting]` (felsic assembly values), `[armor_enchantability]`, `[shields]` with every subtable, `[bonemeal_charm]`,
  `[bottled_air]`, `[backpackSkins]`. Keys, comments, defaults and field names unchanged. First boot: Common's
  `ConfigRename.carrySections` copies the tables with the player's values from `creativeengineering-common.toml` (only
  when the new file does not exist); Forge then drops them from the CE file. NCH finds `CRAFTING.FELSIC_<PIECE>`
  through `global.NCH.registerConfig('creativeequipment', ...CEQConfig)` (startup `ceq_core.js`). The edited copy is
  `configs_edited/root_config/creativeequipment-common.toml`.
- **Resources in the jar** (same paths, `creativeengineering` namespace): the seven tier shield item models; lang
  `assets/creativeequipment/lang/en_us.json` (the shield names and the Step Up key, keys unchanged). The shield
  entity textures and the KubeJS item textures stay in `kubejs/assets/creativeengineering/` (pack resources).
- **Scripts:**
  - Startup `kubejs_startup/creativeequipment/`: `ceq_core.js` (`global.CEQ`: `modId`, `on()`, `recipes()`),
    `func/tune_tools.js`, `func/tune_armor.js` (`global.tuneTools`, `global.tuneArmor`), `items/air_brake.js`,
    `echo_tools.js`, `stepstools.js`, `end_gear.js` (Felsic Scrap, split out of the pack's `items/end.js`). The
    amethyst dust and lens, which sat at the end of `echo_tools.js`, stay in the pack as `items/components_amethyst.js`
    (Engineering rows).
  - Server `kubejs/data/creativeequipment/kubejs/`: `equipment/` (archery, bullets, camera bag, dragon autorepair,
    End gear, shield, stepstool), `other/backpack_skins.js`, `other/harvest.js` (out of CD's tree, owner row),
    `tags/equipmenttags.js`, `tags/curiostags.js` (the stepstool feet rows of the pack's file); opened by
    `_equipment_core.js`. Recipe files run inside `global.CEQ.recipes`, so their ids are now
    `creativeequipment:<type>/<suffix>` (no script, quest or JEI order names the old ids).
  - Client `kubejs/assets/creativeequipment/kubejs/`: `tooltips.js` (stepstools, felsic, elytra, air brake, dragon
    gear lines of the pack's `tooltips.js`), `names.js` (Iron Shield).

## 2. Edges

- **Common keeps the damage tag framework** (`damage/`, all five classes, and `[damage_tags]`) and
  `shield/EffectTickContext`: Creative Brewing's Magic Resistance reads `DamageTags` and `EffectTickContext`, so they
  are Common APIs (7.6 "DamageTags becomes a Common API"; the other four `damage/` classes are its loader, snapshot,
  flags and debug logger and stay with it). Equipment imports them downward.
- Common never imports Equipment: after the move only `CreativeEngineering.java` and `CEConfig` named moved classes,
  and both were trimmed. `compat/CuriosSlots` (the feet slot itself) and `CuriosSlotTooltip` stay Common.
- `perf/WhistleArrowTracker` and `NeaWhistleGoalSkipMixin` stay Common (perf).
- No content jar calls Equipment. CB's echo tool effects read item ids only.
- Requires `creativeengineering [1.1.09,)` and `creativecompat [1.0.0,)`, mandatory AFTER.

## 3. Known effects of the move

- Recipe ids of the moved recipe scripts change namespace (`creativeequipment:`); item, tag and config ids do not.
- Event listener order shifts a little: Equipment's registrations run in its constructor, after patcherMod and Compat.
  The copper tier is sorted between stone and iron the same way.
- Tooltip lines from the moved client rows are added after the pack's own lines for the same item.
- `tools/shield_test/` (the javac harness for `ShieldConfig`) still uses the old package and needs a path update
  before its next run; `tools/shield_check.py`, `gear_misc_probe.js` and `older_misc_probe.js` (Bottled Air) were
  updated.

## 4. Tests

Scratch SQ-CEQ-01 to -08 ([scratch_queue.md](testing/scratch_queue.md)). In game (client rows pinned until wave 8+,
creative_split.md 7.8): CQ-CEQ-01 shields, stepstool, backpack skin and air brake on a client.

## 5. History

- 2026-11-06 (plan_cb_polish.md 1, built, not installed): every armor and tool effect row (`kubejs_startup/creativeequipment/gear_effects.js`),
  their JEED providers (`equipment/gear_jeed.js`), copper poisoning (`effect/CopperPoisoningHandler`,
  `CopperPoisoningEffectMixin`, `[copper_poisoning]` carried over from Creative Brewing's config) and the Create: Deep
  Dark echo armor mixins came from Creative Brewing; `create_deep_dark` is an optional dependency. The felsic recipes
  ask for `creativecommon:magic_resistance` (Common registers it). Tests SQ-CBP-08, -09, CQ-CBP-01.
  Scratch r1106cbp 2026-11-06: SQ-CBP-09 PASS; SQ-CBP-08 FAIL (no leaks, echo rows and potions fine; felsic ingot assembly
  needs Engineering's incomplete items, TODO `creativebrewing/publish #22`; backpack skin recipe needs Sophisticated Backpacks, `#23`).
  2026-11-06 18:29: felsic transition items (11 rows) and the Felsic Scrap model and texture moved into Equipment
  (`items/end_gear.js`, `assets/creativeengineering/`); ids unchanged. Tests SQ-CBP-08b, -11, CQ-CBP-02.
  18:35: all end gear (echo, felsic, dragon) and air brake transition rows (37) in `items/end_gear.js`; felsic ore worldgen and
  the felsic block loot table in `packs/felsic_gear/` (a data pack added only with Felsic Gear, `compat/ModDataPacks`);
  2026-11-06 night: the three Nullscape biome rows moved to `packs/felsic_gear+nullscape/` (a `+` folder needs every
  listed mod), and the felsic ingot assembly in `equipment/end_gear.js` runs only with Felsic Gear and Central Kitchen's
  dragon breath (`global.has`): without Nullscape the pack named its biomes and the world failed to load (r1106spf
  SQ-CBP-08b). Every script of the jar carries `// requires: creativeequipment`. Test SQ-SPFX-04, -05 (both PASS r1106spfx);
  echo tool, air brake and tier shield entity textures in the jar; backpack skin recipes skip without Sophisticated Backpacks.

- 2026-11-04: carved (wave 7). patcherMod 1.1.09 loses 35 classes, 20 mixins, 9 config sections, the shield models and
  8 lang keys; Equipment takes `harvest.js` back from Creative Delight's tree. Installed 19:17 with Creative Magic and
  a clean-built Creative Delight jar (without `harvest.js`).
