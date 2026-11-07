# recipes / magic: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# recipes`, Sub `magic`. Feature writeup:
[recipes_magic.md](../recipes_magic.md) (test prefix `RA-`, last id RA-08). The trinket case (#3) is a compat fix and
belongs in [fixes_compat.md](../fixes_compat.md) (prefix `FX-`, last id FX-20). Related writeups:
[potions.md](../potions.md) (the Geode+ shard potions), [engineering.md](../engineering.md) 2.3 (the metal components
#4 copies), [locate.md](../locate.md) and [structure_tuning.md](../structure_tuning.md) (structure searches, #1).

Jars read: Explorer's Compass 1.3.0 (decompiled under `scratchpad/decomp/explorerscompass/`), Nature's Compass 1.10.0,
Geode+ (`geodeforge-1.1.8-1.19.2`), Botania 1.19.2-440.2 (`BaubleBoxItem`, `BaubleBoxContainer`, `EquipmentHandler`,
`CurioIntegration` decompiled under `scratchpad/decomp/botania_box/`), Armor Trims 1.3.3.

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #1 | Explorer's Compass recipe | Planned | tiny KubeJS, one decision |
| #3 | Botania trinket case takes any item | BUILT 2026-10-22 ([fixes_compat.md](../fixes_compat.md) 2.25) | small **patcherMod** (not KubeJS: the check is Java) |
| #4 | Magical components | Planned | medium: new items, textures, NCH Botania helpers, recipes; mostly design |
| #2 | New geode shard uses | Planned | small once #4 is designed. The potion half is already built |

**Findings that change the rows:**

- **#2's potion half is done.** `startup_scripts/cooking/potions.js` 59-65 brews `geode:wrappist_shard` -> Long Reach and
  `geode:celestite_shard` -> Phantom Bane (potions.md 80-81, PO-04). Geode+ adds only those two new shard items. Its
  other "crystals" are cluster blocks of vanilla materials. So the open part is "armor uses", and the Armor Trims mod
  can't take new trim materials (no trim-material data in its jar). The natural sink is #4.
- **#1: the compass is safe to hand out.** Explorer's Compass searches through `ChunkGenerator.getPlacementsForStructure`
  (SRG `m_223138_`), the method the pack's `ChunkGeneratorLocateMixin` filters. So removed and frequency-0 structures
  have no placements and can't hang a search (verified in the decompiled `SearchWorkerManager.createWorkers`).
- **#3 has to be patcherMod.** The case's filter is `EquipmentHandler.isAccessory`: `#botania:rods`, a `BaubleItem`, a
  mana item, or a Curios capability. That is code, and three call sites check it.

Build order: #1 (one `/reload`). #3 rides the next patcherMod build. #4 and #2 together, after a design pass (both
are "what for" decisions), with a restart for the new items.

Related rows: `potions/potions #4` (placeholder reagents: Magic Resistance on amethyst) and `#7` (potions for unbrewable
effects) could use the shards too. `engineering/sawmill-and-components` (the metal components #4 mirrors).
`recipes/progression #1` (Explorer's Compass is one of its "no recipe" items). `tooling/conventions` (dict duplicates
`i.explorers_compass` / `i.explore_compass`, recipes_magic.md F3).

---

## magic #1: Explorer's Compass
Status Planned / Work KubeJS / TODO `recipes/magic #1` ([recipes_magic.md](../recipes_magic.md) F1)

### Goal
Give Explorer's Compass a late recipe next to Nature's Compass (or make it loot only, on purpose and visibly).

### Now
- [progression.js](../../kubejs/server_scripts/magic/progression.js) 10: `R.remove({id: 'explorerscompass:explorers_compass'}, e)`.
  Line 11: `R.replaceInput({ id: 'naturescompass:natures_compass' }, i.any_log, i.terrasteel_ingot, e)`.
- **Verified (jar):** `explorerscompass:explorers_compass` = `WSW / SCS / WSW`, `W` cobweb, `S` cracked stone bricks,
  `C` compass, giving `explorerscompass:explorerscompass`. `naturescompass:natures_compass` = `SLS / LCL / SLS`,
  `S` `#saplings`, `L` `#logs`, `C` compass. After the pack's swap that is 4 saplings + 4 terrasteel ingots + compass.
- Not in any chest: `kubejs/server_scripts/loot/loot_config.json` has no `explorerscompass:*` entry (the generator
  lists every chest item). So today it is creative-only.
- Tooltip: [tooltips.js](../../kubejs/client_scripts/tooltips.js) 551 `[i.explorers_compass, ['Points to any structure!', col.leg]]`.
- Config `config/explorerscompass-common.toml`: `maxRadius = 10000`, `maxSamples = 100000`, `structureBlacklist = []`,
  `allowTeleport = true`, `displayCoordinates = true`.
- **Verified (decompiled):** the GUI lists every registered structure not in `structureBlacklist`
  (`StructureUtils.getAllowedStructureKeys`). The search builds its workers from
  `level.getChunkSource().getGenerator().getPlacementsForStructure(holder, randomState)`, which the pack filters
  (structure_tuning.md "Removed sets and structure searches"). So a removed structure is listed but finds nothing at
  once.
- Dict: `i.explorers_compass` (636) and `i.explore_compass` (1718) are the same id; `i.natures_compass` (637),
  `i.terrasteel_ingot` (1680), `i.cobweb` (2137), `i.cracked_stone_bricks` (2154), `i.compass` (2144).

### Build
1. progression.js, after line 11:
   ```js
   // Explorer's Compass: the mod's ring of cobwebs and cracked stone bricks around a Nature's Compass (so it inherits
   // the terrasteel gate) instead of a plain compass.
   R.shaped(Item.of(i.explorers_compass), ['WSW', 'SCS', 'WSW'],
   	{ W: i.cobweb, S: i.cracked_stone_bricks, C: i.natures_compass }, e, 'explorers_compass')
   ```
   This gives `creativeengineering:shaped/explorers_compass`. Keep the `R.remove` of the mod's own recipe.
2. Optional: fill `structureBlacklist` in `config/explorerscompass-common.toml` with the structures the pack removed,
   so the GUI doesn't list dead entries. Where the list lives: the emptied sets in
   `kubejs/generation_scripts/worldgen/structure_groups.json` (generator `tools/gen_structure_groups.py`). Hand-copying
   them goes stale; if wanted, have the generator write the toml line. Recommendation: skip for now (searches already
   end at once).
3. Docs: recipes_magic.md 2.3 and F1; drop the compass from `recipes/progression #1`.

### Ids and data
Recipe id `creativeengineering:shaped/explorers_compass` (new). Items verified above. Use `i.explorers_compass` and
leave `i.explore_compass` for the dict-duplicate cleanup.

### Decisions
- **Recipe or loot only.** Recommendation: the recipe above. Loot only would need a loot entry (none exists), and the
  structure-heavy pack is where the compass earns its place.
- **Centre item.** Nature's Compass (recommended, reuses the terrasteel gate) or a magical component from #4 once it
  exists (swap `C` then).

### Gotchas
- `R.shaped` validates every key, so the Nature's Compass id must resolve (it does: `naturescompass:naturescompass`).
- Searches for large modded sets with `maxSamples = 100000` run on a worker per placement. The pack's Async Locator
  setup isn't involved (the compass doesn't call `/locate`), so a long search costs server time. Watch the first use.

### Tests
- **RA-07** (existing, expected answer): JEI Explorer's Compass -> one recipe, cobwebs and cracked stone bricks around
  a Nature's Compass.
- **RA-09 Compass search:** use it, pick a common structure (a village) -> points to one; pick a structure the pack
  removed -> "not found" within a few seconds, no tick lag.

---

## magic #3: Botania trinket case takes any item
Status BUILT 2026-10-22 (fixes_compat 2.25, tests FX-21 to FX-24) / Work patcherMod / TODO `recipes/magic #3` (scratchpad 2026-10-19)

### Goal
Botania's Trinket Case (`botania:bauble_box`) accepts any item, not just trinkets, rods and mana items.

### Now
- **Verified (decompiled, Botania 440.2):** three checks, all `EquipmentHandler.instance.isAccessory(stack)`:
  1. `vazkii.botania.common.item.BaubleBoxItem.getInventory(ItemStack)` builds an anonymous `ItemBackedInventory`
     (`BaubleBoxItem$1`) whose `canPlaceItem` (SRG `m_7013_`) returns `isAccessory(stack)`. This one governs
     insertion from outside the GUI: `overrideStackedOnOther` / `overrideOtherStackedOnMe`, i.e. clicking a stack onto
     the case in an inventory.
  2. `vazkii.botania.client.gui.box.BaubleBoxContainer.<init>` adds 24 slots as an anonymous `Slot`
     (`BaubleBoxContainer$1`) whose `mayPlace` (`m_5857_`) returns `isAccessory(stack)`.
  3. `BaubleBoxContainer.quickMoveStack` (`m_7648_`) only shift-clicks a stack into the case if `isAccessory(itemstack1)`.
- `EquipmentHandler.isAccessory` = `stack.is(BotaniaTags.Items.RODS) || item instanceof BaubleItem ||
  findManaItem(stack) != null`. Forge's `CurioIntegration.isAccessory` adds `|| stack.getCapability(CuriosCapability.ITEM).isPresent()`.
  A KubeJS tag (`#botania:rods`) could let chosen items in, but not "any item", and the rods tag has other uses in
  Botania. So: mixin.
- `BaubleBoxItem.SIZE = 24`. The case drops its contents when the item entity is destroyed (`onDestroyed`).
- patcherMod mixins against mod classes: `creativeengineering.mixins.json` (package `...creativeengineering.mixin`,
  e.g. `QuarkSortingHandlerMixin`), mod jars on the compile classpath from `development/creativeCommon/libs/` (copied from
  `mods/`, git-ignored, build.gradle 17). No refmap: vanilla members by SRG name with `remap = false`.

### Build
1. `mixin/BotaniaTrinketCaseMixin.java` (three small mixins, or one per target class):
   ```java
   @Mixin(targets = "vazkii.botania.common.item.BaubleBoxItem$1", remap = false)
   abstract class TrinketCaseInventoryMixin {
       @Inject(method = "m_7013_", at = @At("HEAD"), cancellable = true)
       private void ce$anyItem(int index, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
           if (TrinketCaseRules.allowed(stack)) cir.setReturnValue(true);
       }
   }
   @Mixin(targets = "vazkii.botania.client.gui.box.BaubleBoxContainer$1", remap = false)  // mayPlace, same body
   @Mixin(value = BaubleBoxContainer.class, remap = false)
   // quickMoveStack: @Redirect the isAccessory call
   //   @Redirect(method = "m_7648_", at = @At(value = "INVOKE", target = "Lvazkii/botania/common/handler/EquipmentHandler;isAccessory(Lnet/minecraft/world/item/ItemStack;)Z"))
   ```
   Inject at HEAD and fall through to Botania's own check, so a disabled config gives vanilla Botania behaviour.
2. `compat/TrinketCaseRules.allowed(stack)`: `CEConfig.botania.trinketCaseAnyItem` is on, the stack isn't a trinket
   case itself (`!(stack.getItem() instanceof BaubleBoxItem)`), and the item can go in containers
   (`stack.getItem().canFitInsideContainerItems()`, SRG `m_142095_`, false for shulker boxes). Optionally also a deny
   tag `#creativeengineering:trinket_case_deny` (filled in `kubejs/server_scripts/tags/` through `global.tags.add`).
3. CEConfig: a new section next to `Magic` (CEConfig.java 363), e.g. `[botania] trinketCaseAnyItem = true` with a comment.
   Regenerate / hand-add the toml key in `config/creativeengineering-common.toml`.
4. Register the mixins in `creativeengineering.mixins.json`. Make sure the Botania jar is in `libs/`.
5. Tooltip: add "Holds any item" to `botania:bauble_box` in [tooltips.js](../../kubejs/client_scripts/tooltips.js).
6. Docs: a new [fixes_compat.md](../fixes_compat.md) 2.x "Trinket case holds anything", file map, FX tests.

### Ids and data
`botania:bauble_box` (Trinket Case, lang `item.botania.bauble_box`). Classes and SRG names above (verified).

### Decisions
- **Exclusions.** Recommendation: the case itself and anything with `canFitInsideContainerItems() == false` (shulker
  boxes, and backpacks that opt out). That avoids nesting exploits. No deny tag unless a problem shows up.

### Gotchas
- The anonymous class names (`$1`) are compiler output and can change with a Botania update. The mixin fails to apply,
  and is logged, rather than crashing, if `required` is false for these targets; consider a separate non-required
  mixin config entry or `@Pseudo`.
- `BaubleBoxContainer` lives in Botania's `client.gui` package but is a common menu (it runs on the server). Don't
  mark the mixin client-only.
- Botania's quick-swap of equipped trinkets from the case UI still only equips real trinkets. Any item can sit in the
  case, but only trinkets equip.

### Tests
- **FX-21 Trinket case, any item:** put cobblestone, a sword and a stack of food in a trinket case by clicking, by
  shift-click and by clicking the stack onto the case item in the inventory -> all accepted; a shulker box and a
  second trinket case -> refused.
- **FX-22 Config off:** `trinketCaseAnyItem = false`, `/reload` is not enough (restart) -> only trinkets, rods and
  mana items fit, as in Botania.

---

## magic #4: Magical components
Status Planned / Work KubeJS / TODO `recipes/magic #4` (scratchpad 2026-10-19)

### Goal
A magic counterpart to the pack's metal components ([engineering.md](../engineering.md) 2.3): a few intermediate items
made from magic materials. They gate magic-side recipes the way the metal components gate machines.

### Now
- Metal components: 13 KubeJS items in [items/components.js](../../development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/components.js) 2-14
  (`creativeengineering:component_<metal>`, names "<Metal> <Kind> Component") plus IE's iron and steel. Recipes in
  [engineering/components.js](../../kubejs/data/creativeengineering/kubejs/engineering/components.js): crafting `P P / _C_ / P P`
  (35-51), sequenced assembly with `R.incomplete('component_<m>')` (54-70; transition items in
  `startup_scripts/changes/transition_items.js` 128-131), Engineer's Workbench blueprint `components` (73-...),
  disassembly. Textures `kubejs/assets/creativeengineering/textures/item/component_<m>.png` + models. Tag
  `#forge:components` (`i.any_component`, item_dict 810).
- Magic materials in the dict: `i.ingot_manasteel` / `i.ingot_elementium` / `i.terrasteel_ingot` (1680-1687), their
  nuggets (5688, 5963, 6262), `i.mana_pearl` (5940), `i.mana_diamond` (5932), `i.mana_powder` (5942), `i.livingrock`,
  `i.livingwood`, `i.celestite_shard` / `i.wrappist_shard` (6390, 6455), `i.xp_block` (Create's experience block,
  recipes_magic.md F3).
- No Botania recipe type is used anywhere in the pack, and NCH has no Botania helper. **Verified JSON shapes
  (jar):** `{"type":"botania:mana_infusion","input":{...},"output":{...},"mana":3000}` (optional `catalyst`) and
  `{"type":"botania:runic_altar","ingredients":[...],"mana":5200,"output":{"item":...,"count":2}}`.
- Magic-side recipes that could take them: [xp.js](../../kubejs/server_scripts/magic/xp.js) (disenchanter, rotor,
  printer), [storage.js](../../kubejs/server_scripts/magic/storage.js) (Ender Storage, ender chest, Cloud Storage),
  progression.js (compasses), the trinket case (#3), Create Enchantment Industry, the potion machines.

### Build (proposal; the design is the row's open decision)
1. **Items** (`startup_scripts/items/components.js`, same style):
   ```js
   e.create('creativeengineering:component_manasteel').displayName('Manasteel Arcane Component')
   e.create('creativeengineering:component_elementium').displayName('Elementium Arcane Component')
   e.create('creativeengineering:component_terrasteel').displayName('Terrasteel Arcane Component')
   e.create('creativeengineering:component_celestite').displayName('Celestite Warding Component')   // magic #2
   e.create('creativeengineering:component_wrappist').displayName('Wrappist Warding Component')     // magic #2
   ```
   Dict keys `i.component_manasteel` ... in the component block of item_dict.js (by 1274), plus a tag
   `#creativeengineering:components/arcane` (`i.any_arcane_component`) filled in `tags/metaltags.js` next to
   `#forge:components` through `global.tags.add`. Textures: recolour the metal component sprites.
2. **NCH helpers** (neoCraftHelper.js, after `ieBlueprint` ~1787, same id scheme `creativeengineering:<type>/<id>`):
   ```js
   botaniaManaInfusion(output, input, mana, e, idOverride, catalyst) { ... e.custom({ type: 'botania:mana_infusion', input: _itemJson(input), output: _itemJson(output, result), mana: mana, catalyst? }) ... }
   botaniaRunicAltar(output, inputs, mana, e, idOverride) { ... e.custom({ type: 'botania:runic_altar', ingredients: inputs.map(_itemJson), mana, output }) ... }
   ```
   validated like the others (`_validateAny`), ids `botaniamanainfusion/<id>` and `botaniarunicaltar/<id>`.
3. **Recipes** (new `server_scripts/magic/components.js`), the metal pattern with a Botania route in place of the IE
   blueprint:
   - crafting `P P / _C_ / P P`, keeping the metal rule of 1 ingot per part:

     | Component | P (x4) | C |
     |---|---|---|
     | manasteel | manasteel ingot | mana powder |
     | elementium | elementium ingot | pixie dust |
     | terrasteel | terrasteel nugget (terrasteel is costly) | mana diamond |
     | celestite | celestite shard | manasteel component |
     | wrappist | wrappist shard | elementium component |
   - sequenced assembly: centre as base, deploy 1 part, 3 loops (`R.createSequencedAssembly(..., ['component_<m>'],
     e, 'component_<m>_sequenced_assembly', 3)`), transition rows in transition_items.js.
   - runic altar: 2 parts + centre + a rune at about 5000 mana -> 2 (the efficient magic route, as the blueprint is
     for metals).
4. **Uses** (pick per the decision): disenchanter and printer take an arcane component in place of one experience
   block; Ender Storage pouch / chest / tank a manasteel component; Explorer's Compass centre (#1); the felsic or
   dragon armor assemblies a warding component (that's #2's "armor uses").
5. Docs: a new section in [recipes_magic.md](../recipes_magic.md) (2.5 "Magical components"), the RA tests.

### Ids and data
New: the 5 `creativeengineering:component_*` items (proposed names), tag `creativeengineering:components/arcane`.
Existing: Botania ingots, nuggets, mana items (dict lines above), Geode+ shards (verified in the jar:
`geode:celestite_shard` from swamp / mangrove swamp geodes, `geode:wrappist_shard` from End highlands / midlands
geodes, by `data/geode/tags/worldgen/biome/has_{celestite,wrappist}_geode.json`). `botania:pixie_dust` is
`i.pixie_dust` (item_dict 6124).

### Decisions
- **What they gate.** Recommendation: the XP machines and Ender Storage (magic utility already behind magic
  materials, recipes_magic.md 1), plus #1's compass. Keep gear for the warding pair.
- **How many.** Recommendation: the three Botania tiers plus the two shard ones. Fewer is fine; the shard pair is what
  makes #2 worth doing.
- **Botania route.** Runic altar (recommended: it's the Botania crafting station) or mana infusion (cheaper, one input).

### Gotchas
- New items need a restart and textures. Ship them with a patcherMod or pack build that has a restart anyway.
- Every new sequenced assembly needs its `transition_items.js` row, or the incomplete item is missing at startup.
- Keep the part counts in step with the metal components (1 ingot per part), or the three routes drift as metal F4 did
  ([engineering.md](../engineering.md) F4).

### Tests
- **RA-10 Arcane components:** JEI each new component -> crafting, sequenced assembly and runic altar recipes; craft a
  manasteel component by hand and on the altar.
- **RA-11 Gated recipes:** JEI the disenchanter (or whatever the decision picks) -> needs an arcane component.

---

## magic #2: New geode shards
Status Planned / Work KubeJS / TODO `recipes/magic #2` (scratchpad 2026-10-19)

### Goal
Armor and potion uses for the Geode+ shards.

### Now
- **Verified (jar):** Geode+ 1.1.8 adds two new shard items, `geode:celestite_shard` and `geode:wrappist_shard`
  (`i.celestite_shard`, `i.wrappist_shard`). Its own recipes only turn them into blocks (4 -> `celestite_block` /
  `wrappist_block`) and use wrappist in `wrappist_pedestal` (2 shards, end stone bricks, 2 prismarine shards). The
  other geodes drop vanilla materials (diamond, redstone, quartz, lapis, emerald, glowstone, gold nuggets, ancient
  debris, echo, prismarine).
- **Potions, built:** [potions.js](../../kubejs/startup_scripts/cooking/potions.js) 59-65 (`CB_POTION_MIXES`):
  thick + wrappist -> `creativeengineering:long_reach`, thick + celestite -> `creativeengineering:phantom_bane`, each
  with redstone / glowstone steps; [potions.md](../potions.md) 80-81, test PO-04. Magic Resistance still uses
  amethyst as a placeholder (potions.js 72, `potions/potions #4`).
- **Armor:** Armor Trims 1.3.3 has no data-driven trim materials (only templates and loot modifiers in its jar). The
  shards can't become trim colours without a mixin.

### Build
1. Fold the armor half into #4: the two "warding" components (celestite, wrappist) go into armor recipes. Which armor
   is part of #4's decision. A proposal: celestite into the felsic set's assembly (mid game; Phantom Bane theme) and
   wrappist into the dragon set (End).
2. Optional potion follow-up with `potions/potions #4`: if Magic Resistance needs a non-placeholder reagent, a third
   Geode+ material isn't available. Use a warding component, or leave amethyst. Recommendation: leave it to that row.
3. Docs: potions.md already covers the potions. Add the armor uses to the gear writeup that gets them.

### Ids and data
As above (verified). No new ids beyond #4's.

### Decisions
- **Which armor.** See #4. Recommendation: build #4 first; this row then needs only the gear recipe edits.

### Gotchas
- Celestite only spawns in swamps and mangrove swamps, wrappist only in the End. A recipe that needs many of either is
  a long hunt; keep the counts low.

### Tests
- **RA-12 Shard armor:** JEI the chosen armor piece -> takes a warding component; craft it with celestite from a
  swamp geode.

---

## Built, needs play

None in this subfamily.

## Writeup corrections made

- [recipes_magic.md](../recipes_magic.md) F1: added that the compass is in no chest loot (creative-only today) and that
  its searches go through the pack-filtered placement lookup (decompiled 2026-10-21).
