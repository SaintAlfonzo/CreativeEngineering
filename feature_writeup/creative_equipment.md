# Creative Equipment

Written 2026-11-04 (Creative Split wave 7, [creative_split.md](creative_split.md) 7.6 and 7.12; TODO `creativesplit/equipment #9`).
Status: **carved 2026-11-04, built, installed 2026-11-04 19:17 (`mods/creativeequipment-1.0.0.jar`, with Creative Common 1.1.09 and Creative Magic); nothing booted; scratch SQ-CEQ queued.**

Written for: the owner and agents working on the split. Own mod `creativeequipment` (development/creativeEquipment,
`creativeequipment-1.0.0.jar`, side both): the pack's gear. Same behavior as before the carve; every registry id keeps
`creativeengineering:`. The Magic rows that were to ride here (2.3) went to their own jar, Creative Magic, one wave
earlier ([creative_magic.md](creative_magic.md)).

## 0. At a glance

<!-- generated:status_index begin open_work sha1=46ed302a6862 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 2 open TODO rows (Testing 2), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creativebrewing/publish #23` | Testing | S | Equipment's backpack skin recipe without Sophisticated Backpacks |
| `creativesplit/equipment #9` | Testing | none | Carve Creative Equipment |
<!-- generated:status_index end open_work -->

## 1. What it holds

- **Java** (`com.SaintAlfonzo.creativeequipment`, same subpackages as in Creative Common; 35 classes plus 3 new):
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
  Easy Elytra Takeoff, Everything Copper. The rest target vanilla. The three backpack skin mixins that target client
  classes (item renderer, baked model, worn layer) are in the `client` list since 2026-11-21: in the common list the
  dedicated server tried to load `ItemTransforms$TransformType` (r1120nv SQ-TI-00, plan_scratch_findings_1121 F1).
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
- Event listener order shifts a little: Equipment's registrations run in its constructor, after Creative Common and Compat.
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
  the felsic block loot table in `packs/felsic_gear/` (a data pack added only with Felsic Gear, `compat/ModDataPacks`;
  it drops 1-2 Felsic Scrap before Fortune, owner value 2026-11-14, `gear/tuning #23`; only its comment said 2-4);
  2026-11-06 night: the three Nullscape biome rows moved to `packs/felsic_gear+nullscape/` (a `+` folder needs every
  listed mod), and the felsic ingot assembly in `equipment/end_gear.js` runs only with Felsic Gear and Central Kitchen's
  dragon breath (`global.has`): without Nullscape the pack named its biomes and the world failed to load (r1106spf
  SQ-CBP-08b). Every script of the jar carries `// requires: creativeequipment`. Test SQ-SPFX-04, -05 (both PASS r1106spfx);
  echo tool, air brake and tier shield entity textures in the jar; backpack skin recipes skip without Sophisticated Backpacks.

- 2026-11-04: carved (wave 7). Creative Common 1.1.09 loses 35 classes, 20 mixins, 9 config sections, the shield models and
  8 lang keys; Equipment takes `harvest.js` back from Creative Delight's tree. Installed 19:17 with Creative Magic and
  a clean-built Creative Delight jar (without `harvest.js`).
- 2026-11-16: `BonemealCharmMixin` refunds only vanilla `minecraft:bone_meal` (the dragon hoe charm; `gear/tuning #15`, R7, [plan_small_fixes_1116.md](plan_small_fixes_1116.md) P4; brief [gear_tuning.md](briefs/gear_tuning.md)). Built and installed; no test by ruling.

Lines moved out of code comments (2026-11-17, plan_rules_fixes.md section 17):

- 2026-09-13: <p><b>2026-09-13 bug:</b> the brake did nothing at all on first release (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/gear/AirBrakeHandler.java)
- 2026-09-26: Picked so the current config's non-zero, sub-100 lines split ~1/3 into each of the lower three (9/9/9 as of 2026-09-26); epic is reserved for full 100% blocking (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/shield/ShieldTooltip.java)
- 2026-10-20: Bottled Air port (feature_writeup/feature_porting.md 2.1, jar retired 2026-10-20) (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/item/BottledAir.java)
- 2026-10-29: Regeneration II, Health Boost II, Resistance II (potions/effects #4, decided 2026-10-29) (development/creativeEquipment/src/main/resources/kubejs_startup/creativeequipment/gear_effects.js)
- 2026-11-04: Creative Equipment; moved from kubejs/server_scripts/equipment/dragon_autorepair.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged (kubejs/data/creativeequipment/kubejs/equipment/dragon_autorepair.js)
- 2026-11-04: Creative Equipment; moved from kubejs/server_scripts/equipment/archery.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged (kubejs/data/creativeequipment/kubejs/equipment/archery.js)
- 2026-11-04: Creative Equipment (tool progression; owner, creative_split.md 7.6); moved from Creative Delight's creativeengineering/harvest.js on 2026-11-04 (it came from the pack's server_scripts/other/harvest.js on 2026-11-01), rows unchanged (kubejs/data/creativeequipment/kubejs/other/harvest.js)
- 2026-11-04: creativeEquipment: Creative Equipment (mod id creativeequipment, feature_writeup/creative_equipment.md), carved out of Creative Common on 2026-11-04 (creative_split.md 7.6, wave 7) (development/creativeEquipment/build.gradle)
- 2026-11-04: Creative Equipment; moved from kubejs/server_scripts/tags/equipmenttags.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged (kubejs/data/creativeequipment/kubejs/tags/equipmenttags.js)
- 2026-11-04: Creative Equipment; moved from kubejs/server_scripts/equipment/shield.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged (kubejs/data/creativeequipment/kubejs/equipment/shield.js)
- 2026-11-04: Creative Equipment (feature_writeup/creative_equipment.md): the pack's gear, carved out of Creative Common on 2026-11-04 (creative_split.md 7.6, wave 7) (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/CreativeEquipment.java)
- 2026-11-04: The sections moved out of Creative Common's creativeengineering-common.toml on 2026-11-04 (creative_split.md 7.6, wave 7) with keys, comments, defaults and field names unchanged: {@code [dragon_gear]}, {@code [air_brake]}, {@code [stepstool]}, {@code [crafting]} (the felsic assembly values), {@code [armor_enchantability]}, {@code [shields]}, {@code [bonemeal_charm]}, {@code [bottled_air]} and {@code [backpackSkins]} (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/CEQConfig.java)
- 2026-11-04: Hands, face, feet Curios slots by IMC (compat/CuriosSlots; feature_writeup/curios_slots.md; from Creative Common 2026-11-04) (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/CreativeEquipment.java)
- 2026-11-04: Creative Equipment; moved from kubejs/startup_scripts/ on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged (development/creativeEquipment/src/main/resources/kubejs_startup/creativeequipment/items/air_brake.js)
- 2026-11-04: Creative Equipment: gear tooltips (stepstools, felsic, elytra, air brake, dragon gear); moved from kubejs/client_scripts/tooltips.js on 2026-11-04, rows unchanged (kubejs/assets/creativeequipment/kubejs/tooltips.js)
- 2026-11-04: Creative Equipment; moved from kubejs/startup_scripts/items/end.js on 2026-11-04 (development/creativeEquipment/src/main/resources/kubejs_startup/creativeequipment/items/end_gear.js)
- 2026-11-04: Creative Equipment; moved from kubejs/server_scripts/tags/curiostags.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged (kubejs/data/creativeequipment/kubejs/tags/curiostags.js)
- 2026-11-04: Creative Equipment; moved from kubejs/server_scripts/equipment/camera_bag.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged (kubejs/data/creativeequipment/kubejs/equipment/camera_bag.js)
- 2026-11-04: The mixins moved from Creative Common on 2026-11-04 unchanged apart from their package (SRG names, remap = false, creativeengineering$ member prefixes) (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/EquipmentMixinPlugin.java)
- 2026-11-04: Creative Equipment; moved from kubejs/startup_scripts/ on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged (development/creativeEquipment/src/main/resources/kubejs_startup/creativeequipment/items/stepstools.js)
- 2026-11-04: Curios "Slot:" tooltip line (compat/CuriosSlotTooltip; moved from Creative Common's [tooltips] 2026-11-04) (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/CEQConfig.java)
- 2026-11-04: Creative Equipment: display names; moved from kubejs/client_scripts/namechanges.js on 2026-11-04 (kubejs/assets/creativeequipment/kubejs/names.js)
- 2026-11-04: Creative Equipment; moved from kubejs/server_scripts/equipment/stepstool.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged (kubejs/data/creativeequipment/kubejs/equipment/stepstool.js)
- 2026-11-04: Creative Equipment; moved from kubejs/startup_scripts/ on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged (development/creativeEquipment/src/main/resources/kubejs_startup/creativeequipment/items/echo_tools.js)
- 2026-11-04: Creative Equipment; moved from kubejs/server_scripts/equipment/end_gear.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged (kubejs/data/creativeequipment/kubejs/equipment/end_gear.js)
- 2026-11-04: Creative Equipment; moved from kubejs/server_scripts/other/backpack_skins.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged (kubejs/data/creativeequipment/kubejs/other/backpack_skins.js)
- 2026-11-04: Creative Equipment; moved from kubejs/server_scripts/equipment/bullets.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged (kubejs/data/creativeequipment/kubejs/equipment/bullets.js)
- 2026-11-06: Creative Equipment (plan_cb_polish.md 1, 2026-11-06): JEED effect providers for the gear, moved from Creative Brewing's jeed_providers.js with the gear rows (kubejs/data/creativeequipment/kubejs/equipment/gear_jeed.js)
- 2026-11-06: Taken over from Creative Brewing's creativebrewing-common.toml (plan_cb_polish.md 1, 2026-11-06) (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/CEQConfig.java)
- 2026-11-06: [copper_poisoning] came from Creative Brewing's file (plan_cb_polish.md 1, 2026-11-06), before that Creative Common's (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/CreativeEquipment.java)
- 2026-11-06: Creative Common since 2026-11-06 (creativebrewing: before, creativeengineering: up to 2026-11-04) (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/CEQConfig.java)
- 2026-11-06: Creative Equipment; moved from Creative Engineering's kubejs_startup/creativeengineering/items/misc_items.js on 2026-11-06 (non-engineering content out of Engineering), id unchanged (development/creativeEquipment/src/main/resources/kubejs_startup/creativeequipment/items/archery_items.js)
- 2026-11-06: Gear effect rows (plan_cb_polish.md 1, 2026-11-06): which armor set or tool grants which effect, with the jar that owns the gear (development/creativeEquipment/src/main/resources/kubejs_startup/creativeequipment/gear_effects.js)
- 2026-11-06: Moved from Creative Engineering's changes/transition_items.js on 2026-11-06 (scratch r1106cbp SQ-CBP-08: without Engineering the felsic ingot assembly failed to parse, TODO `creativebrewing/publish #22`) (development/creativeEquipment/src/main/resources/kubejs_startup/creativeequipment/items/end_gear.js)
- 2026-11-06: First folder: {@code felsic_gear} (End felsic ore worldgen and the felsic block loot table, moved from the pack's kubejs/data on 2026-11-06, TODO `creativebrewing/publish #22`) (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/compat/ModDataPacks.java)
- 2026-11-07: [armor_overflow] came from Creative Common's file with the Detail Armor Bar code (2026-11-07), before the 2026-11-04 swap from patcherMod's creativeengineering-common.toml (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/CreativeEquipment.java)
- 2026-11-07: Sections taken over from Creative Common's creativecommon-common.toml (interface carve, 2026-11-07) (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/CEQConfig.java)
- 2026-11-07: Detail Armor Bar overflow row, from Creative Common (2026-11-07) (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/EquipmentMixinPlugin.java)
- 2026-11-07: --- Detail Armor Bar from Creative Common, 2026-11-07 --- armor bar overflow rows and modded armor icons (API plus 2 mixins) (development/creativeEquipment/src/main/resources/META-INF/mods.toml)
- 2026-11-07: from creativecommon-common.toml (interface carve, 2026-11-07) (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/CEQConfig.java)
- 2026-11-07: Tested version ranges (Detail Armor Bar, moved from Creative Common's CommonMixinPlugin 2026-11-07) (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/EquipmentMixinPlugin.java)
- 2026-11-09: The wooden bucket moved to Creative Delight 2026-11-09 (creativedelight:wooden_*; farm/bucket/ there) (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/CreativeEquipment.java)
- 2026-11-10: Ids follow the owner (feature_writeup/namespace_move.md): the shields, the recipe serializers, the copper tier, the stepstool channel, tags and lang keys use this mod's namespace ({@link #NS}); until 2026-11-10 they were {@code creativeengineering}, and the swap list data/creativeequipment/swaps/owner_namespace.json carries old saves (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/CreativeEquipment.java)
- 2026-11-10: The namespace of every id this mod uses (was creativeengineering until the 2026-11-10 namespace move) (development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/CreativeEquipment.java)
- 2026-11-17: The Air Brake did nothing on first release because right-clicking a plain KubeJS item never calls startUsingItem(); the item's .use(...) callback starts the use phase (AirBrakeHandler, items/air_brake.js)
- 2026-11-23: Dragon armor points 9 / 12 / 12 (upgraded) / 11 / 8 = 40 (helmet / chestplate / upgraded chestplate / leggings / boots; was 8 / 13 / 11 / 8) in `config/dragonloot-common.toml`, which `DragonArmorMaterial` reads; `tune_armor.js` mirrors it (`gear-tuning #2`). `jeed_gear_effects.js` writes the Magic Resistance JEED provider from the gear effect rows: the Felsic set and the Dragon set (both chestplates) (`effect-text #1`).
- 2026-11-17: Echo tool on-hit effects were once a dedicated server script plus manual tooltip lines; they are now gear effect rows (gear_effects.js)
