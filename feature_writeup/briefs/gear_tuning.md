# Brief: gear / tuning

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# gear`, Sub `tuning`; updated 2026-10-31 (brief sweep: rows #14
to #20 added, #2 / #3 moved to Built, line refs re-checked). Reference writeups:
[gear_tuning.md](../gear_tuning.md) (hub: ladder 2.1, stats 2.2 / 2.3, NBT carry 2.7, findings F1 to F13, tests GR-01 to
GR-30), [shields.md](../shields.md) (2.7 recipes, F5 banners, F8 Armor Trims smithing), [armor_effects.md](../armor_effects.md)
(effects on this gear). Code: [end_gear.js](../../kubejs/server_scripts/equipment/end_gear.js),
[tune_tools.js](../../kubejs/startup_scripts/func/tune_tools.js), [tune_armor.js](../../kubejs/startup_scripts/func/tune_armor.js),
[equipmenttags.js](../../kubejs/server_scripts/tags/equipmenttags.js), [neoCraftHelper.js](../../development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js)
(in the patcherMod jar since 2026-10; `cfg` 725-758, `echoAssembly` 1507, `felsicAssembly` 1520, `dragonAssembly` 1531),
patcherMod `CEConfig.java` (`Crafting` 376-438, `DragonGear` 226-240), `mixin/SequencedAssemblyRecipeMixin.java`,
`gear/AirBrakeHandler.java`. New test ids are placeholders (`GR-newN`), numbered when queued; GR-31 to GR-42 below are
this brief's own proposals from 2026-10-21 (not in the writeup or the queue yet).

The "scratchpad 2026-10-19" source text is gone (the scratchpad was emptied into TODO; git history only has the one-line
notes quoted in the rows). The memory notes the rows cite (`config-driven-recipe-values`) are not in the repo; this
brief does not depend on them.

## Scope and order

| Row | Status | Work | Group |
|---|---|---|---|
| #20 Dragon set gives 33 armor, not 36 | Bug | config (`dragonloot-common.toml`) | 0, any time |
| #6 Felsic `potionMb` feeds both fills | Bug | patcherMod + KubeJS | 1 |
| #15 Dragon hoe doesn't consume tree fertilizer | Bug | patcherMod mixin | 1 |
| #4 Gear upgrades keep every NBT | Planned | patcherMod (+ NCH) | 1 |
| #12 End gear recipes configurable | Planned | patcherMod + KubeJS | 1 |
| #10 Fortune on shears | Planned (decided: allow) | patcherMod (+ moreMooblooms) | 1 |
| #5 Untuned dragon bow, crossbow, trident, horse armor | Planned | KubeJS (+ patcherMod) | 2 |
| #7 Small gear findings | Planned | KubeJS | 2 |
| #17 Rebalance the end knife damage | Planned | KubeJS | 2 |
| #14 Armor trims limited to standard armor | Planned | KubeJS tag + patcherMod | 3 (with #16) |
| #16 Audit trimmable armors and trim render cost | Planned | client audit, then #14 | 3 |
| #8 Hardened leather uses | Planned (decided 2026-10-30) | KubeJS | 3 (with #19) |
| #19 Bound leather automated recipe | Planned | KubeJS | 3 |
| #9 PneumaticCraft armor crafts | Planned | KubeJS | 3 |
| #13 Fishing rod chain up to dragon | Planned | KubeJS + Java | 3 |
| #11 Diving gear for every tier | Planned | Java + art | 3 |
| #2, #3, #18 | Testing | see Built, needs play | - |
| #1 | Complete | - | - |

**Facts found on 2026-10-21 that change the rows** (details per row):

- **Armor Trims Backport stores its data under lowercase `trim`**, not `Trim` (verified in `armortrims-1.19.2-1.3.3`,
  `TrimmableItem.TAG_TRIM = "trim"`). Row #4 and gear_tuning.md 2.7 / F5 said `Trim`; a `Trim` entry in the carry list
  would do nothing. The Quark rune is two keys, `quark:RuneAttached` and `quark:RuneColor` (verified, Quark 3.4-418
  `ColorRunesModule`).
- **Two crafting-grid upgrades drop all NBT** (row #4's "which upgrades bypass the mixin"): Iron Fishing Rods' shaped
  rod-in-the-middle recipes, and PneumaticCraft's pneumatic armor (`crafting_shaped_pressurizable` builds the output
  from the result template; only air is summed). Smithing keeps everything, Damage included.
- **DragonLoot's untuned specials** (row #5, verified in `dragonloot-1.1.15`): bow, crossbow and trident durability is
  `67 x dragon_item_durability_multiplier` = 3350 with the pack's `config/dragonloot-common.toml` (50); the dragon bow
  sets arrow base damage to `x1.25 + 1`; the crossbow is a vanilla crossbow; the trident's melee gets
  `+dragon_item_base_damage / 5` (+1.2), its thrown hit is a flat 8 (vanilla's). Horse armor protection is a
  constructor constant, **18** (diamond 11, Simple Netherite Horse Armor 13 from its toml). Enchantability of all
  three ranged items is vanilla's 1.
- **Mining level (row #3, built 2026-10-30: dragon set to 4) is settled by code:** KubeJS `setTier` wraps the tier in a new `MutableToolTier` (verified in
  the KubeJS jar) that is never registered with Forge's `TierSortingRegistry`, so every echo / felsic / dragon tool
  takes Forge's vanilla fallback, where any level 3 or more mines everything. Levels 4 and 5 are the same.
- **The armor cap (row #2, built 2026-10-30 as a cap lift to 50; see #20 for the 33-armor finding) matters less than
  F13 first said:** vanilla `CombatRules.getDamageAfterAbsorb` clamps effective
  armor to 20 anyway (from memory of 1.19.2, not jar-checked). Armor past 20 only keeps you at the 20 clamp against
  bigger hits; 30 already holds the clamp for hits up to about 65 damage with dragon toughness.

**Build groups and order:**

0. **Config, any time:** #20 (two toml values and the `configs_edited` copy; restart).
1. **patcherMod batch (one jar build):** #6 + #12 together (both reshape `CEConfig.Crafting`; do #6's split as part
   of the new piece classes), #4 (carry list, one array), #15 (tree fertilizer refund mixin), #10 (Fortune on shears),
   #7 F8 (`hurtMarked`) if GR-24 shows stutter, #5's enchantability bridge if wanted. Regenerate the `[crafting]` block
   of `config/creativeengineering-common.toml` in the same change.
2. **KubeJS restart batch:** #5 (tune bow / crossbow / trident), #7 F6 / F11, #17 (knife damage), and #12's
   `end_gear.js` side (depends on group 1's jar).
3. **Content rows, each its own session:** #16 audit then #14 (trim allowlist), #8 + #19 hardened leather (dict entries
   first), #9 PneumaticCraft armor, #13 fishing rods (the dragon rod needs Java), #11 diving gear (largest: Java items,
   blocks, block entity, textures).

Dependencies on other families: #13's dragon rod bonus is also `potions/effects #6`; #4 overlaps `gear/shields` (SH-27);
#8 touches `gear/camera-bags` (leather bag) and `gear/archery` (quiver); #14 / #16 build on perf.md 10.3 (the Armor
Trims list cache) and `gear/tuning #18`; #17 pairs with `jei/jei-ordering #8` (knife order); #11 overlaps `worldgen` pillager notes
("end pillagers spawn with diving gear", not a row).

---

## tuning #6: Felsic `potionMb` feeds both fills
Status **BUILT 2026-11-01** (patcherMod built, not installed; steps 1-4 below done, toml in `config/` and `configs_edited/`; test GR-31) · Work: patcherMod / config · id `gear/tuning #6`

### Goal
The config says what it does: either the toml comment is right, or the Long Reach fill and the final fill have
their own amounts.

### Now
- `NCH.felsicAssembly` ([neoCraftHelper.js](../../development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js) 1520-1529) uses `mb`
  for both `create:filling` steps: step 1 `Long Reach` (`{Bottle:"REGULAR",Potion:"creativeengineering:long_reach"}`)
  and step 4 the configured `potion`.
- `CEConfig.Crafting.FelsicPiece` (CEConfig.java 410-437): field `mb`, toml key `potionMb`, comment "mB of potion
  fluid used in the final filling step." (line 430). The same comment is in
  [config/creativeengineering-common.toml](../../config/creativeengineering-common.toml) under every
  `[crafting.felsic_*]` section (10 sections, lines 122-250).
- Real cost a loop: armor 500 + 500 mB, tools and shield 250 + 250 mB.

### Build
Decided (2026-10-29): split. Do it with #12, the classes change anyway.
1. `FelsicPiece`: add `public final ForgeConfigSpec.IntValue reachMb;` toml `reachPotionMb`, comment "mB of Long
   Reach potion fluid used in the first filling step.", range 1-10000, default = the piece's current `mb` (500 armor,
   250 tools / shield) so no recipe changes. Change the `mb` comment to "mB of `potion` fluid used in the last filling
   step."
2. `felsicAssembly(out, base, key, e, potion, mb, loops, hammerBlows, reachMb)`: step 1 uses `reachMb ?? mb`.
3. [end_gear.js](../../kubejs/server_scripts/equipment/end_gear.js) 66-75: add `reachMb` to each `R.cfg` defaults
   object and pass it in the 77-88 bulk rows.
4. Update the toml in `config/` (hand-edit or copy from a dev run; Forge adds missing keys on load but the repo copy is
   what ships).

Comment-only alternative (rejected by the decision): change line 430 to "mB of potion fluid used in each of the two filling steps (Long Reach
first, then `potion`)." and the 10 toml comments. No KubeJS change.

### Ids and data
Java field names are what `R.cfg` reads (`mb`, `reachMb`), toml keys are labels (`potionMb`, `reachPotionMb`)
([gear_tuning.md](../gear_tuning.md) F12).

### Decisions
- Comment or split: decided 2026-10-29, **split**, with defaults equal to today's values.

### Gotchas
- `R.cfg` warns per missing field; ship the jar before the script, or the script logs
  `[NCH.cfg] no CEConfig.CRAFTING.FELSIC_HELMET.reachMb field` and falls back (harmless).
- Recipes need `/reload` after a toml change (the `Crafting` javadoc, CEConfig.java ~373, says so).
- Player-visible only if a default changes; with today's defaults no changelog bullet (dev-only config split).

### Tests
- GR-31: `[crafting.felsic_helmet] reachPotionMb = 100`, `/reload` → log line `CRAFTING.FELSIC_HELMET.reachMb
  overridden: default 500 -> 100`; JEI shows 100 mB Long Reach, 500 mB Magic Resistance. Put it back.

---

## tuning #4: Gear upgrades keep every NBT
Status **Planned** · Work: patcherMod · id `gear/tuning #4` (checked 2026-10-31: carry list unchanged)

### Goal
Every upgrade path keeps the piece's identity: enchantments, name, repair cost, Unbreakable, attribute modifiers,
armor trim, Quark rune glint, banner pattern; and damage by decision.

### Now
- `SequencedAssemblyRecipeMixin.GEAR_NBT_KEYS` (lines 40-42, unchanged 2026-10-31): `Enchantments`, `StoredEnchantments`, `display`,
  `RepairCost`, `Unbreakable`, `AttributeModifiers`. Injects at `RETURN` of Create's `SequencedAssemblyRecipe#advance`
  (`remap = false`), stashes on step 0 as `CEStash`, splices onto the finished item.
- Upgrade paths in the pack and what they keep:

| Path | Recipes | Keeps |
|---|---|---|
| Create sequenced assembly | echo / felsic / dragon gear and shields, air brake, dragon anvil ([end_gear.js](../../kubejs/server_scripts/equipment/end_gear.js)) | the 6 keys above only |
| Vanilla smithing (`UpgradeRecipe.assemble` copies the base's whole tag) | diamond → netherite gear, shield ladder ([shield.js](../../kubejs/server_scripts/equipment/shield.js) 27+), camera bags, Iron Fishing Rods smithing route, DragonLoot `upgraded_dragon_chestplate` (dragon chestplate + elytra, generated in code by `RecipeGenerator`) | everything, Damage too |
| Armor Trims' smithing menu (`SmithingMenuNew.createResult`) | same smithing recipes, template in slot 1 | delegates to `UpgradeRecipe.assemble` (verified), so everything |
| Crafting grid | Iron Fishing Rods `*_fishing_rod_recipe` (rod + 2 ingots, shaped), PneumaticCraft `pneumatic_*` (`crafting_shaped_pressurizable`, output from template, only air summed; verified), PNC `compressed_iron_*` from leather armor (vanilla shaped) | nothing |
| Create Goggles backtanks / goggle helmets | `creategoggles:crafting_nbt` | everything (by design) |

- Tag names (verified): Armor Trims `trim` (compound: `pattern`, `material`), Quark `quark:RuneAttached` (bool) +
  `quark:RuneColor` (ItemStack compound), shield banner `BlockEntityTag`.

### Build
1. patcherMod `SequencedAssemblyRecipeMixin` line 40-42: add `"trim"`, `"quark:RuneAttached"`, `"quark:RuneColor"`,
   `"BlockEntityTag"`, and `"Damage"` if D1 says carry. One array edit. Update the class javadoc.
2. Crafting-grid upgrades: give `NCH.shaped` (NCH line 798) an option to copy NBT from one ingredient. KubeJS 1902.6 has
   `recipe.modifyResult((grid, result) => ...)` with `grid.find(Ingredient)` (verified in the jar):
   ```js
   // NCH.shaped(output, pattern, keys, e, idOverride, damage, cans, keepNbtFrom)
   if (keepNbtFrom) recipe.modifyResult((grid, result) => {
     var src = grid.find(Ingredient.of(keepNbtFrom))
     if (src && !src.isEmpty() && src.hasTag()) result.setTag(src.getTag().copy())
     return result
   })
   ```
   Then re-add the Iron Fishing Rods shaped recipes through it (with #13) and the PNC compressed iron armor (with #9).
3. PneumaticCraft pressurizable armor: `modifyResult` doesn't exist for the custom type. Either replace the four
   recipes with a pressure chamber recipe (#9 decides), or add a patcherMod mixin at `RETURN` of
   `me.desht.pneumaticcraft.api.crafting.ShapedPressurizableRecipe#m_5874_` (`assemble`, SRG name, so `remap` true or
   target `m_5874_` with `remap = false`) that copies the gear keys from the `A` ingredient.
4. Update gear_tuning.md 2.7 and F5, shields.md F5.

### Decisions
- D1: carry damage over: **no** for assembly (an upgrade is a new piece; full durability is a small reward and keeps
  the 2501 → 3350 jumps clean). Smithing already carries it; leave vanilla alone.
- D2: whitelist vs carry everything except `SequencedAssembly` / `CEStash` / `Damage`: **whitelist** (other
  assemblies with NBT inputs exist, e.g. potion fluids are fluids but filled items are not; a blacklist would start
  firing on non-gear recipes).

### Gotchas
- `trim` on a dragon chestplate: Armor Trims only renders on items in its trimmable list; the tag carries anyway.
- `BlockEntityTag` on the incomplete shield is harmless (the stash lives under `CEStash`, not on the item root).
- The incomplete item's own NBT (`SequencedAssembly`) is Create's; never add it to the list.
- `NCH.shaped` already calls `recipe.modifyResult` for the can guard (`_canServings`, NCH 891-915). A second
  `modifyResult` on the same recipe replaces the first, so `keepNbtFrom` must run inside the same callback (one
  combined handler), not as a second call.
- Changelog bullet: upgrades keep trims, rune glint and banners (Gear family).

### Tests
- GR-33: trim a netherite chestplate (Armor Trims), run it to echo → trim kept (`/data get entity @s SelectedItem`
  shows `trim`).
- GR-34: rune a netherite sword (Quark), echo assembly → glint kept.
- SH-27 (update): bannered netherite shield → echo assembly → banner kept.
- GR-35: enchanted iron fishing rod in the grid → gold rod keeps the enchantments (after #13).

---

## tuning #5: Untuned dragon bow, crossbow, trident, horse armor
Status **Planned** · Work: KubeJS · id `gear/tuning #5` (checked 2026-10-31: still untuned, comment block in place)

### Goal
The four specials sit on the ladder like the rest of the dragon gear.

### Now (verified in `dragonloot-1.1.15-1.19.2-forge.jar`, `ItemInit`, `DragonToolMaterial`, item classes)

| Item | Durability | Damage | Ench. | Rarity | Fireproof |
|---|---|---|---|---|---|
| `dragonloot:dragon_bow` (`DragonBowItem extends BowItem`) | 3350 (67 x 50) | arrow base damage x1.25 + 1 (vanilla 2.0 → 3.5) | 1 (BowItem) | common | yes |
| `dragonloot:dragon_crossbow` (`extends CrossbowItem`) | 3350 | vanilla crossbow | 1 | common | yes |
| `dragonloot:dragon_trident` (`DragonTridentItem`) | 3350 | melee vanilla 8 + 1.2 (tooltip ~10.2), thrown flat 8 (`DragonTridentEntity`) | 1 | common | yes |
| `dragonloot:dragon_horse_armor` (`HorseArmorItem(18, "dragon", ...)`) | none | protection 18 | n/a | common | yes |

Pack config [config/dragonloot-common.toml](../../config/dragonloot-common.toml): `dragon_item_durability_multiplier
= 50`, `dragon_item_base_damage = 6.0`, `dragon_tool_enchantability = 25`. Horse armor ladder: leather 3, iron 5,
gold 7, diamond 11, netherite 13 (`netherite_horse_armor-common.toml protectionValue`), dragon 18. The dragon horse
armor is assembled from **diamond** horse armor (end_gear.js 131), skipping netherite.

### Build
1. [tune_tools.js](../../kubejs/startup_scripts/func/tune_tools.js) 137-143 (replace the comment block):
   `tune(i.dragon_bow, { rarity: 'epic' })`, same for crossbow, trident (`damage`, `attackSpeed` only if D2), and
   `tune(i.dragon_horse_armor, { rarity: 'epic' })`. Durability is already 3350, equal to the dragon tools.
2. Enchantability 25 for the three (optional, D1): patcherMod mixin `@Inject(method = "m_6473_", at = HEAD,
   cancellable = true)` on `BowItem`, `CrossbowItem`, `TridentItem` reading a new `CEConfig [item_enchantability]
   overrides` list (`"dragonloot:dragon_bow:25"`), same shape as `ArmorEnchant` (CEConfig.java 533+) and its
   `ArmorEnchantabilityBridge`. Also fixes the Iron Fishing Rods if wanted (they override it themselves, so their
   classes would need the same, see #13).
3. Horse armor protection, if D3 changes it: patcherMod mixin on `HorseArmorItem#m_41368_` (`getProtection`) keyed
   by item id from the same kind of config list. `tuneArmor` can't reach it (not an `ArmorItem`).
4. Dragon horse armor base: switch end_gear.js line 131 (still `i.diamond_horse_armor`, 2026-10-31) to
   `i.netherite_horse_armor` (D4). The dragon horse armor has no durability: its autorepair branch was dropped
   2026-10-28 (GR-21), but it is still listed in `#creativeengineering:dragon_gear` (see #7 F6).

### Decisions
- D1: enchantability 25 for bow / crossbow / trident: **yes** (otherwise the dragon specials are the worst
  enchanting targets in the pack).
- D2: trident damage: **leave** (10.2 melee vs dragon sword 11 is a fair spread). KubeJS `setAttackDamage` on a
  trident is unverified: `DragonTridentItem` overrides `getDefaultAttributeModifiers` and calls `TridentItem`'s, so a
  KubeJS item-level modifier may never show. Check the tooltip after a restart if you try it.
- D3: horse armor 18: **keep** (already a clear step over 13).
- D4: dragon horse armor from netherite horse armor: **yes**, matches every other dragon recipe (felsic or vanilla
  netherite-tier input).

### Gotchas
- `tuneTools` skips `enchantability` on non-tiered items with a warning (tune_tools.js line 33-35).
- Bow and crossbow damage is not an attribute; `tuneTools` `damage` does nothing useful there.
- DragonLoot reads several numbers from its own toml at use time (see #20 for the armor); check that a KubeJS
  `durability` on the ranged items shows in game before trusting it.

### Tests
- GR-36: hover the three ranged items and the horse armor → purple name. Enchanting table at 30 levels with the dragon
  bow (if D1) → enchants comparable to the dragon sword.

---

## tuning #7: Small gear findings
Status **Planned** · Work: KubeJS · id `gear/tuning #7` (F9 done 2026-10-28)

### Goal
Close F6, F8 and the F11 tag question (F9 is done).

### Now and Build (one item each)
- **F9 felsic sword:** DONE 2026-10-28 ([tune_tools.js](../../kubejs/startup_scripts/func/tune_tools.js) line 118 is
  2501; SQ-CM-07 PASS; deployed to the client with 1.1.04).
- **F6 `#creativeengineering:dragon_gear`** ([equipmenttags.js](../../kubejs/server_scripts/tags/equipmenttags.js)
  20-25) holds `i.dragon_anvil`, `i.air_brake` and `i.dragon_horse_armor` (no durability, line 23). Only reader:
  `dragon_autorepair.js` line 40 (`Ingredient.of`), which already skips non-damageable stacks (line 45). Recommend:
  drop all three from the tag (the tag then means "repairs itself"). `/reload`.
- **F8 air brake echo** (`AirBrakeHandler.java` 140-143): the handler runs on both sides and sets
  `player.hurtMarked = true` every braking tick. On the server that makes `ServerEntity` send the player their own
  motion packet every tick (vanilla behaviour, from memory), which can fight client prediction. Fix: only set it when
  the server stalls the player (`stopFallFlying`), or drop the line; the client computes the same velocity itself and
  other players see position updates. Watch GR-24 first; if no stutter, leave it.
- **F11 Alex's Mobs elytra:** the only one in Alex's Mobs 1.21.1 is `alexsmobs:tarantula_hawk_elytra`
  (`ItemTarantulaHawkElytra extends ArmorItem`, overrides `canElytraFly`; verified). Dict key exists
  (`i.tarantula_hawk_elytra`, item_dict.js 10384). The helper mixin already accepts it via `canElytraFly`; adding it to
  `#creativeengineering:elytra_chestplates` would only skip the air-below check for it. Recommend: add it
  (`global.tags.add(e, 'creativeengineering:elytra_chestplates', [i.upgraded_dragon_chestplate, i.tarantula_hawk_elytra])`,
  line 11) if the relaxed takeoff should apply to all non-vanilla gliders, else leave.

### Decisions
- F6: drop. F8: wait for GR-24. F11: **add** (consistent rule: any chestplate that glides takes off like
  the dragon one).

### Gotchas
- `global.tags.remove` only removes direct members; editing the `add` list is simpler here.

### Tests
- GR-23 with the tarantula hawk elytra; JEI tag view of `#creativeengineering:dragon_gear` without anvil, air brake
  and horse armor.

---

## tuning #8: Hardened leather uses
Status **Planned** (decided by the owner 2026-10-30) · Work: KubeJS · id `gear/tuning #8`

### Goal
Hardened leather (or IE ersatz leather) becomes the leather that Sophisticated Backpacks, saddles and leather horse
armor need.

### Now
- The item is **Lucky's Wardrobe** `wardrobe:hardened_leather` (`wardrobe-1.0.3-1.19.2.jar`, checked 2026-10-31).
  Chain: `wardrobe:bound_leather` (`bound_leather_recipe`, shaped `a / aba / a`: 4 string + 1 leather) -> campfire
  cooking 1200 ticks -> `wardrobe:hardened_leather` (`hardened_leather_recipe`). Wardrobe already uses it in its top
  hat, hardened apron, jungle / snowy armor, fletcher's hat, large and travel backpacks.
- **`wardrobe:bound_leather` and `wardrobe:hardened_leather` are still not in item_dict.js** (the wardrobe block at
  item_dict.js 14854-14895 has the armor, hats and backpacks only). No pack script touches the two items.
- Ersatz leather: `i.ersatz_leather` (`immersiveengineering:ersatz_leather`, item_dict.js 1604); the pack's recipe is
  honeycomb + hemp fabric (shapeless and Create deploying, [mob_resources.js](../../kubejs/data/creativeengineering/kubejs/engineering/mob_resources.js) 11-14).
- Targets (verified in jars 2026-10-21): Sophisticated Backpacks `sophisticatedbackpacks:backpack`
  (`i.sophisticatedbackpacks_backpack`; type `sophisticatedbackpacks:basic_backpack`, `L` = `#forge:leather` x4) and
  `sophisticatedbackpacks:upgrade_base` (vanilla shaped, 1 leather); `minecraft:leather_horse_armor`
  (`i.leather_horse_armor`, item_dict.js 2465); saddle `i.saddle` (2742) has no recipe in the pack.
- Other leather users (unchanged by the decision): vanilla leather armor, camera bag ([camera_bag.js](../../kubejs/server_scripts/equipment/camera_bag.js) 12),
  quiver (archery.js 290, already takes leather, hemp fabric or canvas).

### Build
1. item_dict.js wardrobe block: `i.bound_leather = 'wardrobe:bound_leather'`, `i.hardened_leather =
   'wardrobe:hardened_leather'`. Add a tag through `global.tags` in equipmenttags.js:
   `global.tags.add(e, 'creativeengineering:hardened_leathers', [i.hardened_leather, i.ersatz_leather])` and a dict
   key `i.t_hardened_leathers = '#creativeengineering:hardened_leathers'` (one key used by all three recipes).
2. New `kubejs/server_scripts/equipment/leather.js`:
   - Backpack: NCH has no helper for a custom shaped type. Extend NCH with `shapedCustom(type, output, pattern, keys,
     e, idOverride)` (JSON through `_itemJson`, same validation as `shaped`), then
     `R.remove({ id: 'sophisticatedbackpacks:backpack' }, e)` and re-add with `type:
     'sophisticatedbackpacks:basic_backpack'`, Sophisticated Backpacks' own pattern, `L: i.t_hardened_leathers`. Keep the
     type: its serializer gives the backpack its default colours.
   - Saddle (decided shape): `R.shaped(Item.of(i.saddle), ['lll', 'ltl'], { l: i.t_hardened_leathers, t: i.tripwire_hook }, e, 'saddle')`.
   - Leather horse armor (decided shape, old recipe removed): `R.remove({ id: 'minecraft:leather_horse_armor' }, e)`,
     `R.shaped(Item.of(i.leather_horse_armor), ['  l', 'lll', 'l l'], { l: i.t_hardened_leathers }, e, 'leather_horse_armor')`.
3. Tooltip on hardened leather naming its uses (tooltips.js, the pack's line style).
4. Changelog bullets (new family "Hardened leather"): backpack, saddle and horse armor recipes, old horse armor recipe
   removed.

### Ids and data
Recipe ids removed: `sophisticatedbackpacks:backpack`, `minecraft:leather_horse_armor`. New:
`creativeengineering:shaped/saddle`, `creativeengineering:shaped/leather_horse_armor`, the backpack's custom id. Tag
`creativeengineering:hardened_leathers` (new). Items verified in the dict except the two wardrobe ones (to add).

### Decisions
- **"Required in Sophisticated Backpacks": the backpack only, or every SB recipe with leather?** Recommend the base
  backpack only. `upgrade_base` is crafted constantly; the higher backpacks are upgrades of the base one, so they
  inherit the cost.
- **"Tripwire" `t`:** recommend `minecraft:tripwire_hook` (`i.tripwire_hook`, the item; tripwire itself is a block
  placed from string). Ask if string was meant.
- Plain leather in the saddle / horse armor: no (the decision says hardened or ersatz).

### Gotchas
- Hardened leather is campfire-only (60 s each, by hand) until #19 adds a machine route: ship #19 with this row or the
  backpack becomes a chore.
- Leather is common here (rotten flesh to leather); the cost is the string and the time.

### Tests
- **GR-37** (this brief's proposal): JEI on the backpack, saddle and leather horse armor shows only the new recipes,
  each accepting hardened or ersatz leather; the old horse armor recipe is gone; a crafted backpack keeps its default
  colours.

---

## tuning #9: PneumaticCraft armor crafts
Status **Planned** · Work: KubeJS · id `gear/tuning #9` (checked 2026-10-31: pneumatic.js unchanged, no armor recipe edits)

### Goal
Pneumatic armor is made through PneumaticCraft machines, not a plain grid, and keeps the base piece's enchantments.

### Now
- Steel armor (`pneumaticcraft:compressed_iron_*`, renamed "Steel ..." in namechanges.js 118-121): vanilla shaped,
  leather armor piece + compressed iron ingots (in this pack `#forge:ingots/compressed_iron` = IE steel,
  metaltags.js 49). Recipe ids `pneumaticcraft:compressed_iron_helmet` etc.
- Pneumatic armor (`pneumaticcraft:pneumatic_helmet/chestplate/leggings/boots`): type
  `pneumaticcraft:crafting_shaped_pressurizable`, `CPC / CAC / CCC` with air canisters, a printed circuit board and
  the steel piece; drops the steel piece's NBT (see #4).
- PNC is barely edited in the pack ([pneumatic.js](../../kubejs/data/creativeengineering/kubejs/engineering/pneumatic.js): compressed
  iron → steel swap). NCH has `pcPressureChamber` (2341), `pcExplosionCrafting` (2362), `pcThermoPlant` (2381); no
  PNC assembly-line or amadron helper.

### Build (a sketch; D1 picks)
1. Pneumatic armor by pressure chamber: `R.pcPressureChamber([Item.of(i.pneumatic_helmet)], [i.compressed_iron_helmet,
   Item.of(i.printed_circuit_board, 1), Item.of(i.air_canister, 4)], 2.0, e)` per piece (check the helper's input
   format at 2341-2360; dict keys `i.printed_circuit_board` 10021, `i.air_canister` 9845, `i.pneumatic_helmet` 10010,
   `i.compressed_iron_helmet` 9883), remove the four `pneumaticcraft:pneumatic_*` ids.
   Pressure chamber output is built from the template too, so enchantments still drop: either accept (steel armor is
   rarely enchanted) or add the #4 mixin.
2. Steel armor by Create: a sequenced assembly from the leather piece (deploy steel plate x n, press), through
   `R.createSequencedAssembly` with a `transition_items.js` row per piece; NBT then carries via the #4 mixin.
3. Optional upgrade chain: netherite armor + pneumatic module crafts? Out of scope until D1.

### Decisions
- D1 the design: **pressure chamber for pneumatic armor (one machine step, fits PNC), Create assembly for steel
  armor**. Needs your call; the row says only "new, interesting crafts".

### Gotchas
- Pneumatic armor starts empty of air when not made through the pressurizable recipe (the pressurizable type sums
  canister air into the output). Fine for a chamber recipe (the chamber is already pressurised), but say so in a
  tooltip or JEI note.

### Tests
- GR-38: JEI on each pneumatic armor piece shows only the new recipe; craft one in a pressure chamber.

---

## tuning #10: Fortune on shears
Status **Planned** (decided 2026-10-29: allow Fortune on shears) · Work: KubeJS / patcherMod · id `gear/tuning #10`

### Goal
Shears take Fortune at the table and the anvil, and the level does something.

### Now
- Fortune's category is `DIGGER` (`DiggerItem` only); shears (`ShearsItem`, plus Botania manasteel / elementium,
  Everything Copper copper shears) are not diggers, so neither the table nor the anvil offers it.
- What Fortune would do on shears today (from memory of vanilla / Forge 43): **nothing**. Shear-only block drops
  (leaves, grass, vines, cobweb...) are `match_tool` loot that drops the block itself; Forge's `ShearsItem` passes the
  Fortune level to `IForgeShearable.onSheared(..., fortune)` but vanilla sheep, mooshrooms and snow golems ignore it.
- The in-tree More Mooblooms `Moobloom.onSheared(player, item, level, pos, fortune)`
  (development/moreMooblooms/.../entity/Moobloom.java 426-429) also ignores it; it's the one place the pack could use
  the number for free.

- Nothing built (no `ShearsItem` mixin in patcherMod, checked 2026-10-31).

### Build
1. patcherMod mixin on `Enchantment#m_6081_` (`canEnchant(ItemStack)`): return true for `Enchantments.BLOCK_FORTUNE`
   and `stack.getItem() instanceof ShearsItem`. Table: Forge's `canApplyAtEnchantingTable` default calls the
   category check, so also `@Inject` into `ShearsItem` for `canApplyAtEnchantingTable` (IForgeItem default method;
   add the override via mixin).
2. Make it pay: Moobloom `shearInternal` takes `fortune` and rolls extra flower drops (`grownFlowerDrops()` +
   `fortune` extra rolls at 33 %); sheep via a mixin on `Sheep#onSheared` adding `random.nextInt(fortune + 1)` wool.

3. Changelog bullet: shears take Fortune; what it adds (wool, moobloom flowers).
4. Writeup: no shears section exists; add one to gear_tuning.md (or the mooblooms writeup for the flower part).

### Decisions
- D1 allow it: decided 2026-10-29, **yes**.
- D2 what Fortune pays: **recommend sheep wool and moobloom flowers (step 2)**; without it the enchantment is dead.
  Leaves and other shear-only block drops stay as they are (they drop the block itself).

### Tests
- GR-39 (this brief's proposal): enchanting table and anvil offer Fortune on shears; Fortune III shears on a sheep, 20 shears → average wool above 2.

---

## tuning #11: Diving gear for every tier
Status **Planned** · Work: KubeJS / data · id `gear/tuning #11` (checked 2026-10-31: no tier diving items in the dict)

### Goal
Diving helmet, backtank and boots for the echo, felsic and dragon tiers, made by the ladder assemblies.

### Now
- Create 0.5.1.i (verified): `create:copper_*` and `create:netherite_diving_helmet / netherite_backtank /
  netherite_diving_boots`. Classes `DivingHelmetItem(ArmorMaterial, Properties, ResourceLocation)`,
  `BacktankItem(ArmorMaterial, Properties, ResourceLocation, Supplier<BacktankBlockItem>)` (+ `BacktankBlock`,
  `BacktankBlockEntity`, `BacktankRenderer`), `DivingBootsItem`.
- `NetheriteDivingHandler` gives lava vision / fire immunity when the helmet is a `DivingHelmetItem` and the backtank
  is in `#create:pressurized_air_sources`, and each piece is an `ArmorItem` with `isFireResistant()` (verified). So
  fireproof echo / felsic / dragon diving pieces get the netherite perks automatically.
- **Create Goggles** (`creategoggles 0.5.5.g`, in the pack) already does backtanks per vanilla material:
  `creategoggles:leather/chainmail/iron/golden/diamond_backtank` (+ `_placeable`), crafted `creategoggles:crafting_nbt`
  from the chestplate + a copper backtank, its own `CGBacktankBlock` and one `cg_backtank` block entity type listing
  the five blocks (`CGTileEntities`). It's the template for this row. Dict keys exist (item_dict.js ~5440-5460).
- KubeJS can't make these (no backtank builder; needs a block, block entity and renderer).

### Build
1. patcherMod (or a new small mod, `development/`): register per tier T in {echo, felsic, dragon}:
   `T_diving_helmet` (`DivingHelmetItem`), `T_backtank` (`BacktankItem`) + `T_backtank_placeable`
   (`BacktankItem.BacktankBlockItem`) + block (`BacktankBlock` subclass returning own BE type, as `CGBacktankBlock`),
   `T_diving_boots` (`DivingBootsItem`); one `BlockEntityType<BacktankBlockEntity>` with the three blocks; client:
   `BacktankRenderer`, and the armor-layer texture ResourceLocation per tier. Properties `.fireResistant()`.
   Materials: reuse the tier's armor material where reachable (echo `echo_armor`, felsic `felsic`, dragon
   `DragonArmorMaterial`), then set final stats from `tune_armor.js`.
2. Tags: backtanks in `#create:pressurized_air_sources`; helmets in Create's diving helmet handling (by class, no
   tag); add all to `#creativeengineering:dragon_gear` for the dragon set.
3. Recipes in end_gear.js: `R.bulk('echoAssembly' | 'felsicAssembly' | 'dragonAssembly', ...)` from the netherite
   diving pieces, one `transition_items.js` row each (9 rows), config sections via #12.
4. Item dict: `i.echo_diving_helmet = 'creativeengineering:echo_diving_helmet'` etc. (9 items + 3 placeables).
5. Art: 9 item sprites, 3 armor layers, 3 backtank block models (copy Create's `backtank` model, retexture).

### Decisions
- D1 tiers: **echo, felsic, dragon** (the ladder). Vanilla tiers are covered by Create Goggles' backtanks.
- D2 looks: Create-style tanks recoloured per tier (echo teal with the animated glow, felsic, dragon purple).
- D3 stats: diving gear one step under the plain armor of its tier (Create's own diving gear is weak armor).

### Gotchas
- Create's `BacktankItem` hard-codes its BE type lookup through the block; the block subclass must return your type
  (Create Goggles' pattern).
- Mixins on Create are `remap = false`; plain subclassing needs no mixin.
- Big art load; split code and art into two sessions.

### Tests
- GR-40: full echo diving set in lava → netherite-style lava vision and no burning while air lasts. Backtank refills at
  a Create air compressor setup (placed backtank).

---

## tuning #12: End gear recipes configurable
Status **Planned** · Work: KubeJS / config · id `gear/tuning #12` (checked 2026-10-31: only the felsic pieces read CEConfig)

### Goal
Every recipe above netherite reads its numbers from CEConfig through `R.cfg`, like the felsic assemblies.

### Now
- Configurable: the 10 felsic pieces (`CEConfig.Crafting` 376-438, end_gear.js 66-88).
- Hardcoded: echo assembly (end_gear.js 11-22; `echoAssembly` fills 250 mB lava and 250 mB of each potion, 1 loop,
  NCH 1507-1518), felsic ingot (end_gear.js 45-50: 250 mB dragon's breath, 4 loops), dragon assembly (end_gear.js
  114-142; `dragonAssembly` 250 mB dragon's breath hardcoded at NCH 1535, potion mB and loops passed per row).
- The upgraded dragon chestplate stays DragonLoot's smithing recipe (not config).

### Build
1. CEConfig.java `Crafting`: add `EchoPiece(b, section, potionA, potionB, lavaMb, mb, loops)` fields `potionA`,
   `potionB`, `lavaMb`, `mb`, `loops` (toml `potionA`, `potionB`, `lavaMb`, `potionMb`, `loops`), and
   `DragonPiece(b, section, potion, breathMb, mb, loops)` fields `potion`, `breathMb`, `mb`, `loops`. One instance per
   end_gear.js row: `ECHO_SWORD` ... `ECHO_SHIELD` (10), `DRAGON_HELMET` ... `DRAGON_AIR_BRAKE` (16), and
   `FELSIC_INGOT` (`breathMb`, `loops`). Defaults = today's values (table in gear_tuning.md 2.1). Do #6's `reachMb`
   in the same change.
2. NCH: `echoAssembly(out, base, key, e, potionA, potionB, opts)` with `opts = {lavaMb, mb, loops}` (defaults 250 /
   250 / 1); `dragonAssembly(out, base, key, e, potion, mb, loops, breathMb)` (default 250). Old callers keep working.
3. end_gear.js: one `R.cfg(defaults, 'CRAFTING.ECHO_<PIECE>')` per row next to the bulk call, as lines 66-75 do now.
   Rhino has no computed keys; a small loop over `[['helmet', i.echo_helmet, i.netherite_helmet, 'minecraft:night_vision'], ...]`
   keeps it short.
4. Regenerate the `[crafting]` block of `config/creativeengineering-common.toml`.
5. gear_tuning.md: 0 "Knobs" line, 2.1 note, GR-08-style test for one echo and one dragon piece.

### Decisions
- None (row says none). Section names `crafting.echo_<piece>` / `crafting.dragon_<piece>` / `crafting.felsic_ingot`.

### Gotchas
- `R.cfg` compares with `!=`; Java `String` vs JS string compares fine (felsic potion already works).
- A typo'd potion id in the toml makes a filling step no fluid can match; consider logging a warning in end_gear.js
  when `Registry.POTION` lacks the id (NCH has `_effectExists` for effects, not potions; add `_potionExists`).
- Create's config screen needs `SubMenuConfigScreenMixin` + `StringEntry` for string values (already in place for the
  felsic `potion`).

### Tests
- GR-41: `[crafting.dragon_helmet] loops = 3`, `/reload` → log override line; JEI shows 3 loops. Same for
  `[crafting.echo_sword] lavaMb = 500`. Put them back.

---

## tuning #13: Fishing rod chain up to dragon
Status **Planned** · Work: KubeJS / new item · id `gear/tuning #13`

### Goal
One rod ladder: vanilla → iron → gold → diamond → netherite (Iron Fishing Rods) → dragon (new), each step more
durability and enchantability, the dragon rod with its own bonus.

### Now (verified in `Iron Fishing Rods 1.0.0 - 1.19.2.jar`)

| Rod | Durability | Ench. | Repair | Recipes |
|---|---|---|---|---|
| `minecraft:fishing_rod` | 64 | 1 | | vanilla |
| `iron_fishing_rods:iron_fishing_rod` | 270 | 14 | iron ingot | shaped `iron / iron / rod` (`iron_fishing_rod_recipe`) or smithing rod + `iron_fishing_rod_upgrade` |
| `..:gold_fishing_rod` | 34 | 22 | | shaped over the iron rod, or smithing + `gold_fishing_rod_upgrade` |
| `..:diamond_fishing_rod` | 1693 | 10 | | shaped over the gold rod, or smithing + `diamond_fishing_rod_upgrade` |
| `..:netherite_fishing_rod` | 2139 | 15 | netherite ingot | smithing diamond rod + netherite ingot (needs the Armor Trims template, shields.md F8) |

- The `iron_fishing_rods:` ids are now in item_dict.js (14807+, `i.iron_fishing_rod`, `i.iron_fishing_rod_upgrade`,
  ...; checked 2026-10-31). No pack script touches the mod yet. The rods are not fireproof.
- The shaped recipes consume the lower rod and drop its NBT (#4); the smithing ones keep it. The "upgrade" items are
  pure recipe noise.
- Gold is a durability dead end (34) with the best enchantability, so the chain isn't monotonic.
- Fishing mods in the pack: Tactical Fishing, Stardew Fishing (minigame), Book Fishing, Create: Fishing Bobber
  Detector. Check whether Stardew Fishing reads the rod tier before giving the dragon rod a catch bonus.
- KubeJS 1902.6 has no fishing rod builder (verified: only sword / pickaxe / axe / shovel / hoe / shears / armor
  builders), so the dragon rod is Java.

### Build
1. Dict keys: done (item_dict.js 14807+).
2. `kubejs/server_scripts/equipment/fishing.js`: remove the six `*_upgrade_recipe` / `*_upgrading_recipe` and the
   three shaped rod recipes; re-add each step as `R.smithing(i.gold_fishing_rod, i.iron_fishing_rod, i.ingot_gold, e)`
   (keeps NBT), or as `R.shaped` with #4's `keepNbtFrom`. Netherite stays smithing (template, consistent with
   netherite gear).
3. Durability / enchantability: `tuneTools` takes `durability` on any item (`setMaxDamage`), but enchantability is
   hard-coded in each rod class's `getEnchantmentValue`, so it needs #5's `[item_enchantability]` mixin (target
   `FishingRodItem#m_6473_` plus the four Iron Fishing Rods classes, which override it). Proposed: iron 270 / 10,
   gold 400 / 22, diamond 1693 / 12, netherite 2139 / 15 fireproof, dragon 3350 / 25.
4. Dragon rod (patcherMod): `class DragonFishingRodItem extends FishingRodItem`, durability 3350, fireproof, epic,
   repair dragon scale; item model with the `cast` predicate (copy vanilla `fishing_rod.json` / `fishing_rod_cast`);
   recipe `dragonAssembly` from the netherite rod (one `transition_items.js` row, config via #12). Add it to
   `#creativeengineering:dragon_gear` (autorepair).
5. The bonus (D1), as a held effect through `global.toolEffects.held(i.dragon_fishing_rod, ...)` (tool_effects.js)
   if it is an effect, or in the item class if it is fishing logic.

### Decisions
- D1 dragon rod bonus: candidates: built-in Luck of the Sea + Lure (simple, stacks with books up to the vanilla cap),
  lava fishing, a held Luck effect, or an Allurement-style reel that pulls a hooked mob. **Recommend held Luck I plus
  +1 Lure speed in the item class** (`FishingHook` lure speed comes from the enchantment; add it in `use()` like
  vanilla does). Needs your call; also `potions/effects #6`.
- D2 fix gold: **yes**, make it 400 durability so the chain only goes up.

### Gotchas
- `FishingHookRenderer` and `FishingHook` check `canPerformAction(FISHING_ROD_CAST)` in Forge 43 (from memory), so a
  `FishingRodItem` subclass draws its line and keeps its hook; a plain `Item` would not.
- Removing the `*_upgrade` items' recipes leaves the items in JEI; hide them (jei hide list) or leave.

### Tests
- GR-42: JEI chain vanilla → dragon rod, one recipe per step; an enchanted diamond rod keeps its enchantments through
  netherite and dragon; the dragon rod repairs itself (autorepair).

---

## tuning #20: Dragon set gives 33 armor, not 36
Status **FIXED 2026-11-01** (config only: `dragonloot-common.toml` 7 / 12 / 10 / 7, toughness 4.5, copied to `configs_edited/root_config/`; re-run SQ-AC-02) · Work: config · id `gear/tuning #20` (found by scratch r1031, 2026-10-31, SQ-AC-02 FAIL)

### Goal
The full dragon set gives the 36 armor that `tune_armor.js`, the tooltips and gear_tuning.md 2.3 / F13 promise, or the
docs say 33.

### Now
- [tune_armor.js](../../kubejs/startup_scripts/func/tune_armor.js) 108-112: dragon helmet 7, chestplate and upgraded
  chestplate 12, leggings 10, boots 7, toughness 4.5 each, KB 0.3.
- [config/dragonloot-common.toml](../../config/dragonloot-common.toml) `[dragonloot.armor]`: helmet 7, chest **10**,
  leggings **9**, boots 7, `dragon_armor_toughness = 5.0`, `dragon_armor_knockback_resistance = 3`; the same in
  `configs_edited/root_config/dragonloot-common.toml`.
- Why the toml wins (bytecode, `dragonloot-1.1.15-1.19.2-forge.jar`, read 2026-10-31): `DragonArmor` overrides Forge's
  stack-aware `getAttributeModifiers(EquipmentSlot, ItemStack)` and builds armor, toughness and knockback resistance
  from `DragonArmorMaterial` on every call; the material getters (`m_7365_` defense, `m_6651_` toughness, `m_6649_`
  KB) read `ConfigInit.CONFIG` live. So KubeJS's item-level armor / toughness / KB are never used for the four pieces.
  Helmet and boots only match because both sides say 7. **Toughness is the toml's 5.0 x 4 = 20 too, not the 18 the
  writeup states.** Durability (`maxDamage`) and enchantability (25 both) are not affected.
- The cap mixin is fine: SQ-AC-01 and SQ-AC-03 PASS (armor 33 > 30, base 100 clamps to 50).

### Build
1. `config/dragonloot-common.toml`: `dragon_armor_protection_chest = 12`, `dragon_armor_protection_leggings = 10`
   (and `dragon_armor_toughness = 4.5` if D2 says match the tune). Copy to
   `configs_edited/root_config/dragonloot-common.toml`. Restart (the toml is read at load).
2. `tune_armor.js` 108-112: comment that DragonLoot rebuilds armor / toughness / KB from its toml, so those numbers
   only mirror it; or drop them from the dragon lines (keep durability, rarity, fireproof).
3. gear_tuning.md 2.3 and F13 (toughness and where the numbers live); GR-02 expects 36.
4. Changelog bullet (Gear family): Dragon Chestplate armor 12 [10], Dragon Leggings 10 [9] (and toughness if D2).

### Ids and data
No ids. Config keys verified in the installed toml; the client reads its own copy for tooltips, so the deploy ships it.

### Decisions
- D1 toml to 12 / 10 or accept 33: **recommend the toml** (the cap lift to 50 was built for 36; 33 makes the lift
  barely matter).
- D2 toughness 5.0 (live) or 4.5 (tune): **recommend keep 5.0** (20 total is the toughness cap, already what plays)
  and fix the tune and docs to 5.0 instead.

### Gotchas
- Any other DragonLoot armor number set in KubeJS is dead the same way; DragonLoot's tools (`DragonToolMaterial`) may
  read the toml live too (see #5).

### Tests
- SQ-AC-02 re-run (scratch, bot with the full set): `/attribute <bot> minecraft:generic.armor get` -> 36.
- GR-02 in game (expects 36); SQ-AC-04 / CQ-AC-04 HUD in a client.

---

## tuning #15: Dragon hoe doesn't consume tree fertilizer
Status **Bug** · Work: KubeJS / patcherMod · id `gear/tuning #15`

### Goal
Settle what the owner's note means, then make the dragon hoe treat Create's tree fertilizer the way it treats bone
meal.

### Now
- Source: gear_tuning.md "Owner ideas (scratchpad 2026-10-26)": "The dragon hoe does not consume tree fertilizer
  either".
- The dragon hoe's only fertilizer link is the bone meal charm: `#creativeengineering:bonemeal_charms` = `[i.dragon_hoe]`
  ([equipmenttags.js](../../kubejs/server_scripts/tags/equipmenttags.js) 32).
  [BonemealCharmMixin](../../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/BonemealCharmMixin.java)
  injects at `RETURN` of `BoneMealItem#useOn` (`m_6225_`): with a charm in the other hand, `chance` 0.5 refunds the
  bone meal, `damageChance` 0.1 costs the charm 1 durability (`[bonemeal_charm]` in the instance toml, line 1023). So
  with the dragon hoe held, bone meal is often "not consumed".
- Create's `create:tree_fertilizer` (`i.tree_fertilizer`, item_dict.js 5082) is `TreeFertilizerItem extends Item`, not
  `BoneMealItem` (bytecode, Create 0.5.1.i): its own `useOn` grows the sapling, calls `shrink(1)` and returns `SUCCESS`
  on the server. The charm never sees it, so it is always consumed.
- The hoe's area use ([hoe area](../crops.md), `crops.HoeHarvestArea`) harvests and replants; it does not use
  fertilizer items.

### Build (reading A, recommended)
1. `BonemealCharms`: move the refund / damage logic out of the mixin into `static void afterUse(UseOnContext ctx)`.
2. New `mixin/TreeFertilizerCharmMixin`: `@Mixin(value = TreeFertilizerItem.class, remap = false)`, `@Inject(method =
   "m_6225_", at = @At("RETURN"))`; when the result is `SUCCESS` and `!level.isClientSide`, call
   `BonemealCharms.afterUse`. Register in `creativeengineering.mixins.json`; the class is Create's (hard dependency is
   fine, Create is always present).
3. Tooltip / JEED wording of the charm: "bone meal and tree fertilizer". gear_tuning.md and [fixes_compat.md](../fixes_compat.md) FX-12.
4. Changelog bullet: the Dragon Hoe also saves Tree Fertilizer.

### Decisions
- D1 what "either" refers to: **recommend reading A**: like bone meal (the charm's "not consumed" case), the dragon
  hoe should also spare tree fertilizer. Reading B (the hoe's area harvest should spend fertilizer) has nothing to
  attach to. Confirm with the owner.
- D2 other fertilizers (IE fertilizer for the cloche, Botania floral fertilizer): recommend no, tree fertilizer only.

### Gotchas
- Tree fertilizer returns `SUCCESS` on the client too (before the server work); refund on the server only or the
  stack desyncs.
- Keep the catch-all `try` as in `BonemealCharmMixin`.

### Tests
- **GR-new1** (proposed, numbered when queued): dragon hoe in the off hand, 20 tree fertilizer on saplings -> about 10
  left; the hoe loses about 2 durability. Without the hoe: all 20 used.

---

## tuning #14: Armor trims limited to standard armor
Status **Planned** · Work: KubeJS / patcherMod · id `gear/tuning #14` · Do after #16 (the audit names the list)

### Goal
Only standard armor (the pieces that render on the vanilla armor model) can be trimmed; and check how trims look on
what is left.

### Now
- Armor Trims Backport 1.3.3 (`armortrims-1.19.2-1.3.3.jar`): every `ArmorItem` plus its trimmable tags is trimmable.
  The SQ-ATC probe counted **226** (= `kubejs/exported/gear/armor.csv`, 226 rows: Immersive Armors 40, Wardrobe 29,
  vanilla 26, Railways 16, Botania 16, Alex's Mobs 15, Create Goggles 14, PneumaticCraft 8, IE 8, ...).
- `config/armor_trims.toml`: `"Custom Armor Model View" = "NORMAL"` (trims on custom models are usually invisible,
  sometimes "mystery belts"); `["Modpack Maketh Mode"] "Untrimmable Items" = []`. Bytecode (2026-10-31): that list is
  item ids only (no tags; `ArmortrimsApi.getItem(ResourceLocation)`), read by `Config.jeiTrimBlacklist()` and used by
  `SmithingMenuNew` and the JEI recipe maker; active because `Armortrims.isModpack()` is "more than 15 mods". It does
  not shrink `LargeItemLists.getAllTrimmable`, which the item decorator and the untrimming recipe also read.
- patcherMod already wraps that list: `mixin/ArmorTrimsLargeItemListsMixin` + `compat/ArmorTrimsItemCache` (108 lines;
  `putTrimmable` line 65, rebuilt on `TagsUpdatedEvent`), perf.md 10.3.
- Materials already cut to 8 (`gear/tuning #18`, Testing).

### Build
1. Tag through `global.tags` in equipmenttags.js: `creativeengineering:trimmable_armor`, built from whole sets (dict
   keys), per the #16 list.
2. patcherMod `ArmorTrimsItemCache.putTrimmable`: when the tag is non-empty, keep only items in it (one filter before
   caching). That narrows the smithing table, JEI pages, the decorator and untrimming in one place, follows `/reload`,
   and needs no generated id list.
3. Fallback with no Java: write the excluded ids into `"Untrimmable Items"` (generated by a small tool from
   armor.csv, kept in `configs_edited/root_config/armor_trims.toml`). Only blocks the smithing table and JEI.
4. Look check in a client for every kept set (F3+T not needed; trim one piece per set with one material).
5. Writeup: a new Armor Trims section in gear_tuning.md (materials from #18, the allowlist, the cache); changelog
   bullet "Armor trims only fit standard armor sets".

### Decisions
- D1 "standard armor": **recommend the ladder sets plus vanilla-model sets**: leather, chainmail, iron, gold, diamond,
  netherite, copper, echo, felsic, dragon, IE steel, PneumaticCraft steel, Botania sets if they render on the vanilla
  model. Out: hats, mob heads, diving gear, backtanks, goggles, Immersive Armors and other custom models.
- D2 tag filter in the cache (step 2) or the config list (step 3): **recommend the tag filter**.

### Gotchas
- Already-trimmed items outside the list keep their `trim` NBT; check they still render or untrim in the grid.
- The JEI page count (288) drops with the list; CQ-ATC-01's expected count changes.

### Tests
- **GR-new2** (proposed): smithing table refuses a template on an excluded piece (an Immersive Armors chestplate) and
  accepts it on a kept one; JEI trim pages list only kept armor.
- SQ-ATC-02 re-run: trimmable count = the tag's size.

---

## tuning #16: Audit trimmable armors and their trim render cost
Status **Planned** · Work: patcherMod / KubeJS · id `gear/tuning #16` · Feeds #14

### Goal
A list of every trimmable armor with "renders right / wrong / invisible", and a measurement of what trims cost in
client memory, so #14 cuts where it pays.

### Now
- 226 trimmable items (see #14 Now; list in `kubejs/exported/gear/armor.csv`, refreshed by `/dumpall`).
- Client cost candidates (Armor Trims 1.3.3 classes): `model/TrimDecorationBaker` (one baked decoration model per
  item in `customModels`, plus `other`), `model/TrimRenderLayer` (armor-layer trim textures), `util/GetAvgColor`
  (material colours). perf.md 10.2 #3: heap 6.6-9.0 GB of 10 GB while exploring; Armor Trims was not singled out.
- The JEI cost (finding 1, 58 s) is fixed by the cache (perf.md 10.3; CQ-ATC-01 PASS, JEI start 16-19 s); CQ-ATC-02 /
  -03 (trim rendering in a client) not run.

### Build
1. Render audit: a creative client world, armor stands in rows, one trimmed piece per set (one pattern, one material);
   note per set: correct / invisible / wrong. Write the table into the new gear_tuning.md Armor Trims section.
2. Cost: two client heap histograms (`jcmd <pid> GC.class_histogram`, pid via `pgrep -x java`) after loading the same
   world, with and without Armor Trims' decorations (a quick A/B: `"Custom Armor Model View" = "HIDDEN"` or the #14
   filter on a test copy). Count `BakedModel` / `NativeImage` / `DynamicTexture` deltas. Method: perf.md 10.
3. Decide the #14 list from 1 and 2.

### Decisions
- D1 which armors to exclude: from the audit; recommendation in #14 D1.

### Gotchas
- Client checks only; one game JVM at a time (memory `one-game-instance-at-a-time`).

### Tests
- CQ-ATC-02 / -03 (existing, client trim rendering).
- **GR-new3** (proposed): the audit table exists and every kept set shows its trim on an armor stand.

---

## tuning #17: Rebalance the end knife damage
Status **Planned** · Work: KubeJS · id `gear/tuning #17` · Pairs with `jei/jei-ordering #8`

### Goal
The End knives sit above the overworld and nether knives on damage, in step with the gear ladder.

### Now
- Knife line (`kubejs/exported/gear/tools.csv`, attack damage, all attack speed 2): Farmer's Delight flint 2.5, iron
  3.5, golden 1.5, diamond 4.5, netherite 5.5; More Delight wooden 1.5, stone 2.5 (tuned in
  [tune_tools.js](../../kubejs/startup_scripts/func/tune_tools.js) 131-135); Create Things and Misc copper / zinc 4,
  brass 5; Abnormals Delight silver 1.5, necromium 4.5.
- End's Delight (`ends_delight`, a Creative Delight repack member, `development/creativeDelight/members/ends`): end
  stone knife 2.5 (200 durability), purpur knife 2.5 (200), dragon egg shell knife 4 (1250), dragon tooth knife 5.5
  (1561). Dict keys `i.end_stone_knife`, `i.purpur_knife`, `i.dragon_egg_shell_knife`, `i.dragon_tooth_knife`
  (item_dict.js 12065-12072). None is tuned.
- Swords for scale: knife = sword - 2.5 in Farmer's Delight; echo sword 9, felsic 10, dragon 11 (tune_tools.js
  111-125).

### Build
1. tune_tools.js after the More Delight block: an "End's Delight knives" block, `tune(i.<knife>, { damage: N })` per
   D1 (startup script: restart).
2. gear_tuning.md 2.2 (knife rows) and the changelog bullet with old values in brackets.

### Decisions
- D1 which knife and the values: the row says "the end knife". **Recommend treating all four as the End line**:
  end stone and purpur 4.5 (cheap End materials, diamond level), dragon egg shell 6.5 (echo level), dragon tooth 8.5
  (dragon level, knife = sword - 2.5). Owner's call on the numbers.

### Gotchas
- `tuneTools` damage is the total shown in the tooltip (the More Delight lines follow FD's 0.5 knife base); check one
  tooltip after the restart.
- Check the End's Delight knife classes for on-hit extras before raising damage.

### Tests
- **GR-new4** (proposed): tooltips show the new damage; JEI knife order (with `jei/jei-ordering #8`).

---

## tuning #19: Bound leather automated recipe
Status **Planned** · Work: KubeJS · id `gear/tuning #19` · Ship with #8

### Goal
Bound leather (and the hardened leather made from it) can be made by machines, so #8's recipes don't need a campfire
by hand.

### Now
- `wardrobe:bound_leather_recipe` (crafting, 4 string + 1 leather, pattern `a / aba / a`) and
  `wardrobe:hardened_leather_recipe` (campfire cooking 1200 ticks) are the only routes (jar, 2026-10-31). Both items
  lack dict keys (#8 step 1).
- Create's fan "smoking" processes smoking recipes, not campfire cooking, so the hardening step has no machine route.
- Helpers: `NCH.createCompacting` (NCH 1649), `NCH.smoking` (1016), `NCH.createDeploying` (1694).

### Build
1. Bound leather by Create compacting: `R.createCompacting(Item.of(i.bound_leather), [i.leather, Item.of(i.string, 4)], e, 'bound_leather')`
   in #8's `leather.js`.
2. Hardened leather by smoking: `R.smoking(Item.of(i.hardened_leather), i.bound_leather, e, 'hardened_leather', 0.35, 600)`
   (smoker and Create fan smoking; the campfire recipe stays).
3. Changelog bullets under the hardened leather family.

### Decisions
- D1 which machine: **recommend Create compacting** (press + basin, string and leather pressed together); a
  mechanical crafter already runs the shaped recipe, so compacting is the "machine" route.
- D2 also add the smoking step: **yes**, or the chain is still hand-made at the second step.

### Gotchas
- The Wardrobe pattern rows have unequal width (`a`, `aba`, `a`); the dump lists the recipe as loaded, but check it
  crafts in JEI before relying on it.
- `Item.of(i.string, 4)` is fine (a plain item); never `Item.of(<tag key>, n)` (memory `kubejs-api-notes`).

### Tests
- **GR-new5** (proposed): a press over a basin with 1 leather + 4 string -> bound leather; a Create fan smoking bound
  leather -> hardened leather; JEI lists both.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| `gear/tuning #2` | Dragon set over the 30 armor cap | Testing | `ArmorCapMixin`, `[dragon_gear] armorCap` 50, built 2026-10-30 (patcherMod 1.1.04); [gear_tuning.md](../gear_tuning.md) F13. Scratch r1031: SQ-AC-01, -03 PASS, SQ-AC-02 FAIL (33, not 36: `gear/tuning #20`). Open: SQ-AC-04 / GR-02 in a client (C P) |
| `gear/tuning #3` | Mining level 5 probably does nothing | Testing | Every dragon tool `miningLevel: 4` (tune_tools.js 125-129), 2026-10-30; [gear_tuning.md](../gear_tuning.md) F4. Open: GR-05 (echo pickaxe on a `#forge:needs_netherite_tool` block) (S P) |
| `gear/tuning #18` | Armor trims limited to eight materials | Testing | `config/armor_trims.toml` materials: copper, iron, gold, diamond, netherite, echo ingot, felsic ingot, dragon scale (checked 2026-10-31); deployed 1.1.04. Built 2026-10-29; fold into a new gear_tuning.md Armor Trims section (with #14 / #16). Open: smithing table and JEI trim pages in a client (C P) |
| tuning #1 | Gear tuning ladder | Complete | GR-01 to GR-30 never run. gear_tuning.md's "Pending" line said F1 to F13 open; F1, F3, F7, F12 are fixed (corrected). 2.7 / F5 named the Armor Trims tag `Trim`; it is `trim` (corrected). |

## Writeup corrections made

- [gear_tuning.md](../gear_tuning.md) section 0 "Pending": open findings list now excludes the fixed F1, F3, F7, F12;
  added a link to this brief.
- [gear_tuning.md](../gear_tuning.md) 2.7 and F5: Armor Trims tag `Trim` → `trim` (verified in the jar), Quark rune keys
  named.
- [gear_tuning.md](../gear_tuning.md) section 5: NCH assembly helpers' line range `~1061-1110` → `~1159-1207`.
