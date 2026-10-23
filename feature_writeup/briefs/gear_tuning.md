# Brief: gear / tuning

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# gear`, Sub `tuning`. Reference writeups:
[gear_tuning.md](../gear_tuning.md) (hub: ladder 2.1, stats 2.2 / 2.3, NBT carry 2.7, findings F1 to F13, tests GR-01 to
GR-30), [shields.md](../shields.md) (2.7 recipes, F5 banners, F8 Armor Trims smithing), [armor_effects.md](../armor_effects.md)
(effects on this gear). Code: [end_gear.js](../../kubejs/server_scripts/equipment/end_gear.js),
[tune_tools.js](../../kubejs/startup_scripts/func/tune_tools.js), [tune_armor.js](../../kubejs/startup_scripts/func/tune_armor.js),
[equipmenttags.js](../../kubejs/server_scripts/tags/equipmenttags.js), [neoCraftHelper.js](../../kubejs/startup_scripts/func/neoCraftHelper.js)
(`cfg` 631-664, `echoAssembly` 1174, `felsicAssembly` 1187, `dragonAssembly` 1198), patcherMod `CEConfig.java`
(`Crafting` 296-361), `mixin/SequencedAssemblyRecipeMixin.java`, `gear/AirBrakeHandler.java`.

The "scratchpad 2026-10-19" source text is gone (the scratchpad was emptied into TODO; git history only has the one-line
notes quoted in the rows). The memory notes the rows cite (`config-driven-recipe-values`) are not in the repo; this
brief does not depend on them.

## Scope and order

Open rows: #6 (Bug), #2 to #5, #7 to #13 (Planned). #1 is Complete (table at the end).

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
- **Mining level (row #3) is settled by code:** KubeJS `setTier` wraps the tier in a new `MutableToolTier` (verified in
  the KubeJS jar) that is never registered with Forge's `TierSortingRegistry`, so every echo / felsic / dragon tool
  takes Forge's vanilla fallback, where any level 3 or more mines everything. Levels 4 and 5 are the same.
- **The armor cap (row #2) matters less than F13 says:** vanilla `CombatRules.getDamageAfterAbsorb` clamps effective
  armor to 20 anyway (from memory of 1.19.2, not jar-checked). Armor past 20 only keeps you at the 20 clamp against
  bigger hits; 30 already holds the clamp for hits up to about 65 damage with dragon toughness.

**Build groups and order:**

1. **patcherMod batch (one jar build):** #6 + #12 together (both reshape `CEConfig.Crafting`; do #6's split as part
   of the new piece classes), #4 (carry list, one array), #7 F8 (`hurtMarked`), #5's enchantability bridge if wanted,
   #10 if the answer is yes. Regenerate the `[crafting]` block of `config/creativeengineering-common.toml` in the same
   change.
2. **KubeJS restart batch:** #2 (trim or keep), #3 (dragon `miningLevel` 4, or leave), #5 (tune bow / crossbow /
   trident), #7 F9 / F6 / F11, and #12's `end_gear.js` side (depends on group 1's jar).
3. **Content rows, each its own session:** #8 hardened leather (dict entries first), #9 PneumaticCraft armor, #13
   fishing rods (the dragon rod needs Java), #11 diving gear (largest: Java items, blocks, block entity, textures).
4. Close by decision: #10 (recommend no, or a moobloom-only bonus).

Dependencies on other families: #13's dragon rod bonus is also `potions/effects #6`; #4 overlaps `gear/shields` (SH-27);
#8 touches `gear/camera-bags` (leather bag) and `gear/archery` (quiver); #11 overlaps `worldgen` pillager notes
("end pillagers spawn with diving gear", not a row).

---

## tuning #6: Felsic `potionMb` feeds both fills
Status Bug / Work patcherMod / config / `gear/tuning #6`

### Goal
The config says what it does: either the toml comment is right, or the Long Reach fill and the final fill have
their own amounts.

### Now
- `NCH.felsicAssembly` ([neoCraftHelper.js](../../kubejs/startup_scripts/func/neoCraftHelper.js) 1187-1196) uses `mb`
  for both `create:filling` steps: step 1 `Long Reach` (`{Bottle:"REGULAR",Potion:"creativeengineering:long_reach"}`)
  and step 4 the configured `potion`.
- `CEConfig.Crafting.FelsicPiece` (CEConfig.java 330-360): field `mb`, toml key `potionMb`, comment "mB of potion
  fluid used in the final filling step." (line 350). The same comment is in
  [config/creativeengineering-common.toml](../../config/creativeengineering-common.toml) under every
  `[crafting.felsic_*]` section (lines 117-246).
- Real cost a loop: armor 500 + 500 mB, tools and shield 250 + 250 mB.

### Build
Recommended: split (do it with #12, the classes change anyway).
1. `FelsicPiece`: add `public final ForgeConfigSpec.IntValue reachMb;` toml `reachPotionMb`, comment "mB of Long
   Reach potion fluid used in the first filling step.", range 1-10000, default = the piece's current `mb` (500 armor,
   250 tools / shield) so no recipe changes. Change the `mb` comment to "mB of `potion` fluid used in the last filling
   step."
2. `felsicAssembly(out, base, key, e, potion, mb, loops, hammerBlows, reachMb)`: step 1 uses `reachMb ?? mb`.
3. [end_gear.js](../../kubejs/server_scripts/equipment/end_gear.js) 66-75: add `reachMb` to each `R.cfg` defaults
   object and pass it in the 77-88 bulk rows.
4. Update the toml in `config/` (hand-edit or copy from a dev run; Forge adds missing keys on load but the repo copy is
   what ships).

Comment-only alternative: change line 350 to "mB of potion fluid used in each of the two filling steps (Long Reach
first, then `potion`)." and the 10 toml comments. No KubeJS change.

### Ids and data
Java field names are what `R.cfg` reads (`mb`, `reachMb`), toml keys are labels (`potionMb`, `reachPotionMb`)
([gear_tuning.md](../gear_tuning.md) F12).

### Decisions
- Comment or split: **split**, with defaults equal to today's values.

### Gotchas
- `R.cfg` warns per missing field; ship the jar before the script, or the script logs
  `[NCH.cfg] no CEConfig.CRAFTING.FELSIC_HELMET.reachMb field` and falls back (harmless).
- Recipes need `/reload` after a toml change (the comment block in CEConfig.java 286-295 says so).

### Tests
- GR-31: `[crafting.felsic_helmet] reachPotionMb = 100`, `/reload` → log line `CRAFTING.FELSIC_HELMET.reachMb
  overridden: default 500 -> 100`; JEI shows 100 mB Long Reach, 500 mB Magic Resistance. Put it back.

---

## tuning #2: Dragon set over the 30 armor cap
Status Planned / Work KubeJS / `gear/tuning #2`

### Goal
The dragon set's armor numbers do something, or the overflow is a known, accepted choice.

### Now
- [tune_armor.js](../../kubejs/startup_scripts/func/tune_armor.js) 108-112: dragon 7 / 12 / 10 / 7 = 36 armor,
  toughness 4.5 each (18), KB 0.3. Felsic 24, echo 22, netherite 20.
- `generic.armor` is a `RangedAttribute` with max 30 (vanilla `Attributes`, from memory); toughness max 20. No mod
  in [MODLIST.md](../../pack/MODLIST.md) is a known cap lift (no AttributeFix, no Apotheosis). Detail Armor Bar is
  client display only.
- Damage taken: `CombatRules.getDamageAfterAbsorb`: `f = 2 + toughness/4`; effective armor
  `= clamp(armor - damage/f, armor*0.2, 20)`; damage x `(1 - effective/25)` (from memory). So 20 effective armor is
  the hard ceiling (80 % cut). With dragon toughness 18 (f = 6.5): 30 armor keeps the 20 ceiling for hits up to
  65 damage; 36 would hold it to 104. The extra 6 points only matter for 65+ damage hits, and the attribute cap
  already removes them.

### Build
Recommended: **trim the dragon pieces to 30** so the tooltips stop promising armor that doesn't exist.
1. tune_armor.js 108-112: helmet 6, chestplate and upgraded chestplate 10, leggings 8, boots 6 (30). Keeps the
   felsic → dragon step at every slot (4/9/7/4 → 6/10/8/6).
2. Optional: move the value into toughness (5 each = 20, the toughness cap) so dragon still beats felsic on big hits.
3. Update gear_tuning.md 2.3 and F13, GR-02 (expect 30).

Cap-lift alternative (not recommended): patcherMod, reflection at common setup on `RangedAttribute.maxValue`
(`f_22308_`, SRG) of `Attributes.ARMOR` to e.g. 64, like AttributeFix. Pointless without also touching
`CombatRules`' 20 clamp, which changes the whole game's armor curve.

### Decisions
- Accept, trim, or lift: **trim to 30** (6 / 10 / 8 / 6), toughness to 5.

### Gotchas
- Restart, not `/reload` (`ItemEvents.modification`).
- Mixed sets: a dragon helmet on felsic still adds 6, not 7. Fine.
- The 20 clamp also means felsic (24) and echo (22) already hold the clamp for small hits; worth a line in
  gear_tuning.md 2.3 so later tuning doesn't chase armor points.

### Tests
- GR-02 (update): full dragon set → HUD shows 30.
- GR-32: wearing full dragon, `/attribute @s minecraft:generic.armor get` → 30.

---

## tuning #3: Mining level 5 probably does nothing
Status Planned / Work KubeJS / `gear/tuning #3`

### Goal
Know what the mining levels do, and make the numbers honest.

### Now
- [tune_tools.js](../../kubejs/startup_scripts/func/tune_tools.js) 111-129: echo and felsic `miningLevel: 4`, dragon
  `5`; applied through `item.setTier(t => t.setLevel(n))` (line 71).
- KubeJS `ItemKJS.kjs$setTier` replaces the item's tier with `new MutableToolTier(parent)` (verified in
  `kubejs-forge-1902.6.2-build.73`). `MutableToolTier` has no `getTag()` override and is never registered, so Forge's
  `TierSortingRegistry.isTierSorted` is false for it.
- Forge 43 `isCorrectTierForDrops`: unsorted tier → vanilla check, which only tests `level < 3` against
  `needs_diamond_tool`, `< 2` iron, `< 1` stone (from memory, no Forge jar here). So any level ≥ 3 mines every block,
  including `forge:needs_netherite_tool` and any modded higher-tier tag. Echo, felsic and dragon are identical.
- The pack registers one sorted tier itself: `CETiers.COPPER` between stone and iron
  (`CreativeEngineering.java` 241-246). That is what makes the sorted path live for vanilla tiers.
- The echo tools' base tier (`ItemEvents.toolTierRegistry` in [echo_tools.js](../../kubejs/startup_scripts/items/echo_tools.js))
  is a KubeJS tier, also unsorted.

### Build
Recommended: accept.
1. tune_tools.js 125-129: set dragon `miningLevel: 4` (cosmetic, but `/dumpgear` and any mod that prints the level
   stop showing a meaningless 5). Or leave 5 and note it.
2. gear_tuning.md F4: mark resolved (by code reading) and GR-05 as a confirmation test only.

Register-tiers alternative (not recommended): `TierSortingRegistry.registerTier` for per-item `MutableToolTier`
copies would need registering 15 tier objects created inside `ItemEvents.modification`, after Forge's tier sort; and
only matters if some block needs more than netherite.

### Decisions
- Accept or register: **accept**; change dragon to 4 or leave (either is fine).

### Gotchas
- A mod that reads `tier.getLevel()` directly (not through Forge) would see 5. None known in the pack.

### Tests
- GR-05 (keep): echo pickaxe on a `#forge:needs_netherite_tool` block → drops (expected).

---

## tuning #4: Gear upgrades keep every NBT
Status Planned / Work patcherMod / `gear/tuning #4`

### Goal
Every upgrade path keeps the piece's identity: enchantments, name, repair cost, Unbreakable, attribute modifiers,
armor trim, Quark rune glint, banner pattern; and damage by decision.

### Now
- `SequencedAssemblyRecipeMixin.GEAR_NBT_KEYS` (lines 40-42): `Enchantments`, `StoredEnchantments`, `display`,
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
2. Crafting-grid upgrades: give `NCH.shaped` an option to copy NBT from one ingredient. KubeJS 1902.6 has
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

### Tests
- GR-33: trim a netherite chestplate (Armor Trims), run it to echo → trim kept (`/data get entity @s SelectedItem`
  shows `trim`).
- GR-34: rune a netherite sword (Quark), echo assembly → glint kept.
- SH-27 (update): bannered netherite shield → echo assembly → banner kept.
- GR-35: enchanted iron fishing rod in the grid → gold rod keeps the enchantments (after #13).

---

## tuning #5: Untuned dragon bow, crossbow, trident, horse armor
Status Planned / Work KubeJS / `gear/tuning #5`

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
1. [tune_tools.js](../../kubejs/startup_scripts/func/tune_tools.js) 131-137 (replace the comment block):
   `tune(i.dragon_bow, { rarity: 'epic' })`, same for crossbow, trident (`damage`, `attackSpeed` only if D2), and
   `tune(i.dragon_horse_armor, { rarity: 'epic' })`. Durability is already 3350, equal to the dragon tools.
2. Enchantability 25 for the three (optional, D1): patcherMod mixin `@Inject(method = "m_6473_", at = HEAD,
   cancellable = true)` on `BowItem`, `CrossbowItem`, `TridentItem` reading a new `CEConfig [item_enchantability]
   overrides` list (`"dragonloot:dragon_bow:25"`), same shape as `ArmorEnchant` (CEConfig.java 384-410) and its
   `ArmorEnchantabilityBridge`. Also fixes the Iron Fishing Rods if wanted (they override it themselves, so their
   classes would need the same, see #13).
3. Horse armor protection, if D3 changes it: patcherMod mixin on `HorseArmorItem#m_41368_` (`getProtection`) keyed
   by item id from the same kind of config list. `tuneArmor` can't reach it (not an `ArmorItem`).
4. Dragon horse armor base: switch end_gear.js line 131 to `i.netherite_horse_armor` (D4).

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

### Tests
- GR-36: hover the three ranged items and the horse armor → purple name. Enchanting table at 30 levels with the dragon
  bow (if D1) → enchants comparable to the dragon sword.

---

## tuning #7: Small gear findings
Status Planned / Work KubeJS / `gear/tuning #7`

### Goal
Close F6, F8, F9 and the F11 tag question.

### Now and Build (one item each)
- **F9 felsic sword 2502** ([tune_tools.js](../../kubejs/startup_scripts/func/tune_tools.js) line 118): every other
  felsic tool is 2501. Set 2501 (recommend; no reason found for the 1). Restart.
- **F6 `#creativeengineering:dragon_gear`** ([equipmenttags.js](../../kubejs/server_scripts/tags/equipmenttags.js)
  19-24) holds `i.dragon_anvil` and `i.air_brake`. Only reader: `dragon_autorepair.js` line 44, which already skips
  non-damageable stacks (line 49). Recommend: drop both from the tag (the tag then means "repairs itself"). `/reload`.
- **F8 air brake echo** (`AirBrakeHandler.java` 140-143): the handler runs on both sides and sets
  `player.hurtMarked = true` every braking tick. On the server that makes `ServerEntity` send the player their own
  motion packet every tick (vanilla behaviour, from memory), which can fight client prediction. Fix: only set it when
  the server stalls the player (`stopFallFlying`), or drop the line; the client computes the same velocity itself and
  other players see position updates. Watch GR-24 first; if no stutter, leave it.
- **F11 Alex's Mobs elytra:** the only one in Alex's Mobs 1.21.1 is `alexsmobs:tarantula_hawk_elytra`
  (`ItemTarantulaHawkElytra extends ArmorItem`, overrides `canElytraFly`; verified). Dict key exists
  (`i.tarantula_hawk_elytra`, item_dict.js 10301). The helper mixin already accepts it via `canElytraFly`; adding it to
  `#creativeengineering:elytra_chestplates` would only skip the air-below check for it. Recommend: add it
  (`global.tags.add(e, 'creativeengineering:elytra_chestplates', [i.upgraded_dragon_chestplate, i.tarantula_hawk_elytra])`,
  line 10) if the relaxed takeoff should apply to all non-vanilla gliders, else leave.

### Decisions
- F9: 2501. F6: drop. F8: wait for GR-24. F11: **add** (consistent rule: any chestplate that glides takes off like
  the dragon one).

### Gotchas
- The archery block in equipmenttags.js (45-106) still uses bare `e.add`; that's `gear/archery`, not this row.

### Tests
- GR-01 (felsic sword 2501), GR-23 with the tarantula hawk elytra.

---

## tuning #8: Hardened leather uses
Status Planned / Work KubeJS / `gear/tuning #8`

### Goal
Hardened leather becomes a real intermediate for leather gear (backpacks, leather armor, horse armor, saddles).

### Now
- The item is **Lucky's Wardrobe** `wardrobe:hardened_leather` (verified in `wardrobe-1.0.3-1.19.2.jar`). Chain:
  `wardrobe:bound_leather` (shaped `a / aba / a`, 4 string + 1 leather, `bound_leather_recipe`) → campfire cooking
  1200 ticks → `wardrobe:hardened_leather` (`hardened_leather_recipe`). Wardrobe already uses it in its top hat,
  hardened apron, jungle / snowy armor, fletcher's hat, large and travel backpacks.
- **No `wardrobe:` id is in item_dict.js** and no pack script touches the mod.
- Leather consumers that are candidates (verified in jars): Sophisticated Backpacks `sophisticatedbackpacks:backpack`
  (type `sophisticatedbackpacks:basic_backpack`, `L` = `#forge:leather` x4) and `sophisticatedbackpacks:upgrade_base`
  (vanilla shaped, 1 leather); vanilla `minecraft:leather_helmet/chestplate/leggings/boots`,
  `minecraft:leather_horse_armor`; Immersive Armors `warrior_chestplate`; pack recipes
  [camera_bag.js](../../kubejs/server_scripts/equipment/camera_bag.js) line 18 (leather bag), archery.js 290 (quiver).
  Saddles have no recipe in the pack. PneumaticCraft steel ("compressed iron") armor is made from leather armor
  pieces, so it inherits whatever leather armor costs. Quark's backpack is disabled (`quark-common.toml` `Backpack =
  false`).

### Build
1. item_dict.js: add a `// ===== wardrobe =====` block in the file's per-mod style:
   `i.bound_leather = 'wardrobe:bound_leather'`, `i.hardened_leather = 'wardrobe:hardened_leather'`.
2. Automation: add `R.smoking(Item.of(i.hardened_leather), i.bound_leather, e)` (NCH `smoking`, line 868) so a Create
   fan / smoker can make it (campfire only is 60 s each, hand-placed).
3. Recipes, per D1, in a new `kubejs/server_scripts/equipment/leather.js`:
   - Sophisticated Backpacks backpack: the custom type has no NCH helper and KubeJS `replaceInput` on a modded type is
     unreliable. Extend NCH with `shapedCustom(type, output, pattern, keys, e, idOverride)` that builds the
     `e.custom({type, pattern, key, result})` JSON through `_itemJson` (same validation as `shaped`), then
     `R.remove({id: 'sophisticatedbackpacks:backpack'})` and re-add with `type: 'sophisticatedbackpacks:basic_backpack'`
     and `L: i.hardened_leather`. Keep the type: its serializer is what gives the backpack its default colours.
   - Leather horse armor: `R.remove({id: 'minecraft:leather_horse_armor'})`, `R.shaped` with hardened leather.
   - Saddle: new `R.shaped(Item.of(i.saddle), ['HHH', 'HSH', 'I I'], {H: i.hardened_leather, S: i.string, I: i.nugget_iron}, e, 'saddle')`
     (check the iron nugget key name in the dict).
4. Tooltip on hardened leather naming its uses (tooltips.js, the pack's line style).

### Ids and data
Recipe ids: `sophisticatedbackpacks:backpack`, `sophisticatedbackpacks:upgrade_base`, `minecraft:leather_horse_armor`,
`minecraft:leather_helmet` etc., `immersive_armors:warrior_chestplate`.

### Decisions
- D1 which recipes: **backpack (SB tier 1), leather horse armor, a new saddle recipe**. Leave vanilla leather armor
  on plain leather (it is the starter armor; making it cost 4 string per leather and a minute of cooking hurts the
  early game), unless you want a "hardened leather armor" step, which would need new items.
- D2 put hardened leather in `#forge:leather`: **no** (every leather recipe in every mod would accept it, nothing would
  require it).

### Gotchas
- SB's `upgrade_base` is crafted constantly; leave it on leather.
- Leather is common here (JRFTL rotten flesh to leather, `mob_resources.js` 31); the cost is the string and the time.

### Tests
- GR-37: bound leather in a smoker → hardened leather; JEI lists the new backpack, horse armor and saddle recipes; the
  old leather backpack recipe is gone.

---

## tuning #9: PneumaticCraft armor crafts
Status Planned / Work KubeJS / `gear/tuning #9`

### Goal
Pneumatic armor is made through PneumaticCraft machines, not a plain grid, and keeps the base piece's enchantments.

### Now
- Steel armor (`pneumaticcraft:compressed_iron_*`, renamed "Steel ..." in namechanges.js 115-118): vanilla shaped,
  leather armor piece + compressed iron ingots (in this pack `#forge:ingots/compressed_iron` = IE steel,
  metaltags.js 49). Recipe ids `pneumaticcraft:compressed_iron_helmet` etc.
- Pneumatic armor (`pneumaticcraft:pneumatic_helmet/chestplate/leggings/boots`): type
  `pneumaticcraft:crafting_shaped_pressurizable`, `CPC / CAC / CCC` with air canisters, a printed circuit board and
  the steel piece; drops the steel piece's NBT (see #4).
- PNC is barely edited in the pack ([pneumatic.js](../../kubejs/server_scripts/engineering/pneumatic.js): compressed
  iron → steel swap). NCH has `pcPressureChamber` (1851), `pcExplosionCrafting` (1872), `pcThermoPlant` (1891); no
  PNC assembly-line or amadron helper.

### Build (a sketch; D1 picks)
1. Pneumatic armor by pressure chamber: `R.pcPressureChamber([Item.of(i.pneumatic_helmet)], [i.compressed_iron_helmet,
   Item.of(i.printed_circuit_board, 1), Item.of(i.air_canister, 4)], 2.0, e)` per piece (check the helper's input
   format at 1851-1870 and the dict keys for PCB and canister), remove the four `pneumaticcraft:pneumatic_*` ids.
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
Status Planned / Work KubeJS / patcherMod / `gear/tuning #10`

### Goal
Decide whether shears take Fortune, and if so, make it do something.

### Now
- Fortune's category is `DIGGER` (`DiggerItem` only); shears (`ShearsItem`, plus Botania manasteel / elementium,
  Everything Copper copper shears) are not diggers, so neither the table nor the anvil offers it.
- What Fortune would do on shears today (from memory of vanilla / Forge 43): **nothing**. Shear-only block drops
  (leaves, grass, vines, cobweb...) are `match_tool` loot that drops the block itself; Forge's `ShearsItem` passes the
  Fortune level to `IForgeShearable.onSheared(..., fortune)` but vanilla sheep, mooshrooms and snow golems ignore it.
- The in-tree More Mooblooms `Moobloom.onSheared(player, item, level, pos, fortune)`
  (development/moreMooblooms/.../entity/Moobloom.java 421-425) also ignores it; it's the one place the pack could use
  the number for free.

### Build (only if D1 is yes)
1. patcherMod mixin on `Enchantment#m_6081_` (`canEnchant(ItemStack)`): return true for `Enchantments.BLOCK_FORTUNE`
   and `stack.getItem() instanceof ShearsItem`. Table: Forge's `canApplyAtEnchantingTable` default calls the
   category check, so also `@Inject` into `ShearsItem` for `canApplyAtEnchantingTable` (IForgeItem default method;
   add the override via mixin).
2. Make it pay: Moobloom `shearInternal` takes `fortune` and rolls extra flower drops (`grownFlowerDrops()` +
   `fortune` extra rolls at 33 %); sheep via a mixin on `Sheep#onSheared` adding `random.nextInt(fortune + 1)` wool.

### Decisions
- D1 allow it: **no** unless you want the sheep / moobloom bonus; without step 2 it is a dead enchantment that takes a
  slot.

### Tests
- GR-39 (if built): Fortune III shears on a sheep, 20 shears → average wool above 2.

---

## tuning #11: Diving gear for every tier
Status Planned / Work KubeJS / data / `gear/tuning #11`

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
  the five blocks (`CGTileEntities`). It's the template for this row. Dict keys exist (item_dict.js 5350-5368).
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
Status Planned / Work KubeJS / config / `gear/tuning #12`

### Goal
Every recipe above netherite reads its numbers from CEConfig through `R.cfg`, like the felsic assemblies.

### Now
- Configurable: the 10 felsic pieces (`CEConfig.Crafting` 296-361, end_gear.js 66-88).
- Hardcoded: echo assembly (end_gear.js 11-22; `echoAssembly` fills 250 mB lava and 250 mB of each potion, 1 loop,
  NCH 1174-1185), felsic ingot (end_gear.js 45-50: 250 mB dragon's breath, 4 loops), dragon assembly (end_gear.js
  114-142; `dragonAssembly` 250 mB dragon's breath hardcoded at NCH 1202, potion mB and loops passed per row).
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
Status Planned / Work KubeJS / new item / `gear/tuning #13`

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

- No `iron_fishing_rods:` id is in item_dict.js; no pack script touches the mod. The rods are not fireproof.
- The shaped recipes consume the lower rod and drop its NBT (#4); the smithing ones keep it. The "upgrade" items are
  pure recipe noise.
- Gold is a durability dead end (34) with the best enchantability, so the chain isn't monotonic.
- Fishing mods in the pack: Tactical Fishing, Stardew Fishing (minigame), Book Fishing, Create: Fishing Bobber
  Detector. Check whether Stardew Fishing reads the rod tier before giving the dragon rod a catch bonus.
- KubeJS 1902.6 has no fishing rod builder (verified: only sword / pickaxe / axe / shovel / hoe / shears / armor
  builders), so the dragon rod is Java.

### Build
1. item_dict.js: `// ===== iron_fishing_rods =====` block, `i.iron_fishing_rod` ... `i.netherite_fishing_rod`, and the
   three `*_fishing_rod_upgrade` items.
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

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| tuning #1 | Gear tuning ladder | Complete | GR-01 to GR-30 never run. gear_tuning.md's "Pending" line said F1 to F13 open; F1, F3, F7, F12 are fixed (corrected). 2.7 / F5 named the Armor Trims tag `Trim`; it is `trim` (corrected). |

## Writeup corrections made

- [gear_tuning.md](../gear_tuning.md) section 0 "Pending": open findings list now excludes the fixed F1, F3, F7, F12;
  added a link to this brief.
- [gear_tuning.md](../gear_tuning.md) 2.7 and F5: Armor Trims tag `Trim` → `trim` (verified in the jar), Quark rune keys
  named.
- [gear_tuning.md](../gear_tuning.md) section 5: NCH assembly helpers' line range `~1061-1110` → `~1159-1207`.
