# Gear tuning: the netherite → echo → felsic → dragon ladder, stat overrides, autorepair, elytra extras

## 0. At a glance

| Part | Status |
|---|---|
| `global.tuneTools` / `global.tuneArmor`: per-item stat overrides on `ItemEvents.modification`, filled for every echo, felsic and dragon tool and armor piece | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3, live in 1.0.6); no test ids before this writeup |
| Echo-tier tools (pickaxe, axe, shovel, hoe) next to CDD's echo sword; `echo` tool tier | DEPLOYED, in-game test pending |
| Gear ladder recipes: echo / felsic / dragon sequenced assemblies (`NCH.echoAssembly` / `felsicAssembly` / `dragonAssembly`), felsic ingot, original recipes removed | DEPLOYED, in-game test pending |
| Felsic assembly values from config (`[crafting.felsic_<piece>]`, `NCH.cfg`) | DEPLOYED; the felsic shield has no config section (F1) |
| Assembly keeps enchantments, name, repair cost, Unbreakable, attribute modifiers (`SequencedAssemblyRecipeMixin`) | DEPLOYED (patcherMod), in-game test pending |
| Armor enchantability overrides for whole materials (echo 18, felsic 21; `ArmorMaterialEnchantabilityMixin`) | DEPLOYED (patcherMod), in-game test pending |
| Dragon gear autorepair (`#creativeengineering:dragon_gear`, 50 durability every 10 s while worn or held; horse armor too) | DEPLOYED, in-game test pending |
| Dragon scale elytra duplication (`creativeengineering:elytra_duplication`) | DEPLOYED; JEI doesn't show it ([jei_coverage.md](jei_coverage.md), JC-02) |
| Rocket takeoff with the upgraded dragon chestplate (two Easy Elytra Takeoff mixins, `#creativeengineering:elytra_chestplates`) | DEPLOYED (patcherMod), in-game test pending |
| Air brake (hold-to-use elytra brake, stall; `AirBrakeHandler`) | DEPLOYED; the 2026-09-13 "does nothing" bug was fixed; in-game test pending |

- **Pending:** nothing built and unshipped. GR-01 to GR-30 have never been run. Findings F1 to F13 (section 4) are
  open; F4, F5, F10 and F13 need your call.
- **Files to open:** [func/tune_tools.js](../kubejs/startup_scripts/func/tune_tools.js) and
  [func/tune_armor.js](../kubejs/startup_scripts/func/tune_armor.js) (the numbers),
  [end_gear.js](../kubejs/server_scripts/equipment/end_gear.js) (the ladder recipes),
  [dragon_autorepair.js](../kubejs/server_scripts/equipment/dragon_autorepair.js),
  [AirBrakeHandler.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/gear/AirBrakeHandler.java).
- **Knobs:**
  - Stats: code in the two tune files; edit and **restart** (`/reload` doesn't re-run `ItemEvents.modification`).
  - `config/creativeengineering-common.toml`, live through Create's config screen:
    `[dragon_gear.autorepair]` (enabled, intervalTicks 200, amountPerInterval 50), `[air_brake]` (11 keys, 2.9),
    `[armor_enchantability] overrides`, `[crafting.felsic_<piece>]` (potion, potionMb, loops, hammerBlows; needs
    `/reload` to rebuild the recipes).
- **How to measure:** `/dumpgear` writes every armor and tool stat to `kubejs/exported/gear/` (`armor.csv`,
  `armor_sets.csv`, `tools.csv`; not committed). F3+H shows durability.

## 1. Overview

The end-game gear comes from three mods, each with its own stats and recipes:

- **Create: Deep Dark 1.7.0:** echo sword and echo armor, made by Create sequenced assembly from netherite; its own
  `echo` tier (2124 uses, speed 9, +6 damage, enchantability 16, mining level 1).
- **Felsic Gear 1.0.0:** felsic tools and armor, made by smithing; armor material `felsic` (enchantability 18).
- **Enderdragon Loot (DragonLoot) 1.1.15:** dragon tools, armor, bow, crossbow, trident, horse armor and an anvil,
  made by smithing with dragon scales; the `upgraded_dragon_chestplate` glides like an elytra (DragonLoot's own
  mixins; it is a plain `ArmorItem`, not an `ElytraItem`).

The pack turns them into one ladder, netherite → echo → felsic → dragon, all made by sequenced assembly, and tunes
every piece so each step is a clear upgrade. CDD ships only a sword, so the pack adds the other four echo tools.

## 2. Reference

### 2.1 The ladder

| Step | Input | Assembly (one loop) | Loops |
|---|---|---|---|
| Echo | netherite piece | fill 250 mB lava → deploy echo ingot → deploy amethyst dust → vibrate → fill 250 mB Darkness → fill 250 mB Weakness (tools) or Night Vision (armor, shield) | 1 |
| Felsic | echo piece | fill *n* mB Long Reach → deploy felsic ingot → hammer (*b* blows) → fill *n* mB Magic Resistance | from config, 1 |
| Dragon | felsic piece (vanilla bow / crossbow / trident / diamond horse armor / anvil for the specials) | fill 250 mB dragon's breath → deploy dragon scale → laser cut → fill 250 mB lingering potion | per item, below |
| Felsic ingot | netherite sheet | deploy felsic scrap → deploy brass ingot → fill 250 mB dragon's breath → press | 4 |

Felsic *n* / *b*: armor 500 mB, 5 blows; tools 250 mB, 3 blows; shield 250 mB, 8 blows. *n* is used by both fills (F2).

Dragon step (one scale per loop):

| Item | Lingering potion | Loops (scales) |
|---|---|---|
| Helmet | Long Magic Resistance | 2 |
| Chestplate | Long Resistance | 4 |
| Leggings | Long Fire Resistance | 3 |
| Boots | Long Knockback Resistance (AM) | 2 |
| Sword / pickaxe / axe / shovel / hoe | Strong Regeneration | 3 / 3 / 2 / 1 / 1 |
| Crossbow / bow / trident / horse armor | Strong Regeneration | 2 / 3 / 2 / 3 |
| Anvil | Long Resistance | 6 |
| Shield | Long Resistance | 5 |
| Air brake (from a netherite sheet) | Strong Slowness | 2 |

The `upgraded_dragon_chestplate` keeps DragonLoot's own recipe (not removed). Shields have their own ladder, the same
assemblies: [shields.md](shields.md) 2.7.

Removed: CDD's 10 echo sequenced assemblies, Felsic Gear's 9 smithing recipes and its ingot/block recipes, DragonLoot's
14 smithing recipes.

### 2.2 Tool stats (`tune_tools.js`)

Tooltip numbers (bare fist = 1 damage / 4.0 speed). All epic and fireproof.

| Item | Damage | Speed | Mining speed | Durability | Ench. | Mining level |
|---|---|---|---|---|---|---|
| Echo sword (CDD) | 9 | 1.65 | | 2124 | 18 | 4 |
| Echo pickaxe / axe / shovel / hoe | 6 / 12 / 7 / 1 | 1.2 / 1.0 / 1.2 / 4.5 | 10 | 2124 | 18 | 4 |
| Felsic sword | 10 | 1.7 | | **2502** (F9) | 21 | 4 |
| Felsic pickaxe / axe / shovel / hoe | 7 / 14 / 7.5 / 1 | 1.2 / 1.1 / 1.4 / 5.0 | 11 | 2501 | 21 | 4 |
| Dragon sword | 11 | 1.75 | | 3350 | 25 | 5 |
| Dragon pickaxe / axe / shovel / hoe | 8 / 16 / 8 / 1 | 1.2 / 1.2 / 1.6 / 5.5 | 12 | 3350 | 25 | 5 |

Not tuned (DragonLoot defaults): dragon bow, crossbow, trident, horse armor (F10). For comparison, vanilla netherite:
sword 8 / 1.6, pickaxe 6 / 1.2, mining speed 9, 2031 uses, enchantability 15.

### 2.3 Armor stats (`tune_armor.js`)

All epic and fireproof. Armor, toughness and knockback resistance **replace** the piece's value.

| Set | Armor (H / C / L / B) | Toughness | KB res. | Durability (H / C / L / B) | Ench. |
|---|---|---|---|---|---|
| Echo (CDD) | 3.5 / 8.5 / 6.5 / 3.5 | 3.5 | 0.2 | 429 / 624 / 585 / 507 | 18 (mixin) |
| Felsic | 4 / 9 / 7 / 4 | 4 | 0.25 | 572 / 832 / 780 / 676 | 21 (mixin) |
| Dragon (both chestplates) | 7 / 12 / 10 / 7 | 4.5 | 0.3 | 1300 / 1750 / 1600 / 1400 | 25 (DragonLoot) |

Vanilla netherite for comparison: 3 / 8 / 6 / 3, toughness 3, KB 0.1, durability 407 / 592 / 555 / 481,
enchantability 15. The set bonuses on top of these are in [armor_effects.md](armor_effects.md) 2.1.

### 2.4 `tuneTools` / `tuneArmor` spec

```js
global.tuneTools({ 'minecraft:diamond_pickaxe, minecraft:diamond_axe': { durability: 3000, damage: 5 } })
```

| Key | Tools | Armor | Notes |
|---|---|---|---|
| `durability`, `stackSize`, `fireproof`, `rarity` | yes | yes | rarity: common, uncommon, rare, epic |
| `damage`, `attackSpeed` | yes | | tooltip numbers; the helper subtracts 1.0 / 4.0 |
| `miningSpeed` | diggers only | | swords have none |
| `enchantability`, `miningLevel`, `repairWith` | tiered items only | | applied through a per-item copy of the tier |
| `armor`, `toughness`, `knockbackResistance` | | `ArmorItem` only | replace, not add |
| `enchantability`, `repairWith` | | warning, ignored | armor reads these off its `ArmorMaterial`; enchantability goes through `[armor_enchantability]` instead |

A key that doesn't fit the item logs `[tuneTools] <id>: skipped <key>` and the rest still applies. Calls stack. The
calls are unprefixed (`item.setMaxDamage`, not `kjs$setMaxDamage`): the prefixed names don't exist for scripts in this
KubeJS build and silently did nothing.

### 2.5 Echo tools

`ItemEvents.toolTierRegistry` adds tier `echo`: 2124 uses, speed 9, +6 damage, enchantability 18, repair echo ingot,
**mining level 4** (CDD's sword tier is level 1, which would stop an echo pickaxe at iron). The four items
(`creativeengineering:echo_pickaxe/axe/shovel/hoe`) are fireproof, in the Tools tab, with 12-frame animated textures
(`tools/echo_texture.py`). `tune_tools.js` then sets their final numbers (2.2). Their on-hit effects are in
[armor_effects.md](armor_effects.md) 2.3.

### 2.6 Dragon gear autorepair

- **What:** every `intervalTicks` (200), any damaged item in `#creativeengineering:dragon_gear` in a player's four
  armor slots or two hands gets `amountPerInterval` (50) durability back. A horse wearing dragon horse armor too (the
  same once-per-interval walk over the level's entities as the horse armor effect).
- **Not repaired:** items in the inventory, backpacks or curios slots.
- **Tag:** DragonLoot's 14 gear items (both chestplates, bow, crossbow, trident, horse armor), the dragon anvil, the
  dragon shield and the air brake. The anvil and the air brake have no durability, so they're no-ops (F6).
- **Why not Allurement's Reforming:** it would show as a real enchantment, share that mod's rate with real books, and
  stop working without Allurement.

### 2.7 What an assembly keeps

`SequencedAssemblyRecipeMixin` lifts these tags off the input on step 0, carries them on the incomplete item as
`CEStash`, and puts them on the finished item:

`Enchantments`, `StoredEnchantments`, `display` (name, lore, dye), `RepairCost`, `Unbreakable`, `AttributeModifiers`.

Not kept: `Damage` (the output is at full durability), `Trim` (Armor Trims), `BlockEntityTag` (shield banners), any
other mod data (F5). Applies to every sequenced assembly, but only does anything when the input has one of those tags.

### 2.8 Elytra: dragon scale duplication and rocket takeoff

- **Duplication:** crafting grid, any `ElytraItem` + a dragon scale → a fresh vanilla elytra; the original stays in the
  grid untouched (enchantments, durability, Customizable Elytra styling). Any third item breaks the recipe. Quark's
  own version needs `quark:dragon_scale`, which is removed. Recipe id
  `creativeengineering:elytraduplication/dragon_scale_elytra`, type `creativeengineering:elytra_duplication`
  (catalyst and result from the JSON).
- **Rocket takeoff:** Easy Elytra Takeoff 4.2 launches you with a firework only if you wear an `ElytraItem` and nothing
  but air is in the 3x3 layer two blocks under your feet. Two patcherMod mixins:
  - `EasyElytraTakeoffHelperMixin`: "wearing an elytra" also counts any armor stack in
    `#creativeengineering:elytra_chestplates` or for which Forge's `canElytraFly` is true.
  - `EasyElytraTakeoffGroundCheckMixin`: for tagged chestplates the air-below check is skipped, so the rocket works
    from a single glass block or mid-fall. Vanilla elytra keep the stock rule.
  - Tag today: `dragonloot:upgraded_dragon_chestplate` only.

### 2.9 Air brake

`creativeengineering:air_brake`, unstackable, Tools tab, made by dragon assembly (2.1). Hold right click (bow pose)
while gliding. Every tick held, on both the client and the server:

| Key | Default | Effect |
|---|---|---|
| `enabled` | true | master switch |
| `requireFallFlying` | true | only while gliding |
| `horizontalDecelerationPerTick` | 0.05 | flat cut to horizontal speed (blocks/tick) |
| `minHorizontalSpeed` | 0.0 | floor |
| `upwardDecelerationPerTick` | 0.05 | flat cut while climbing |
| `minUpwardSpeed` | 0.0 | floor |
| `terminalDownwardSpeed` | 3.0 | cap on fall speed while braking (0 = off) |
| `downwardSlowFactorPerTick` | 0.02 | 2% of fall speed removed per tick |
| `stallEnabled` | true | drop out of gliding when slow |
| `stallSpeedThreshold` | 0.5 | horizontal speed at or under which you stall |
| `useDurationTicks` | 72000 | max hold, read once at startup (F7) |

From a typical 1.5 blocks/tick glide, the default brake reaches the stall speed in about 20 ticks (1 s).

## 3. What the pack changes

- **Stats at startup:** `tune_tools.js` / `tune_armor.js` (`func/`, `// priority: -100` so `global.item` exists)
  register one `ItemEvents.modification` handler per call. The pre-filled tables started from a `/dumpgear` export and
  were then tuned by hand.
- **Recipes:** `end_gear.js` only calls NCH helpers: `R.bulk('echoAssembly' | 'felsicAssembly' | 'dragonAssembly', ...)`,
  `R.createSequencedAssembly` for the felsic ingot, `R.elytraDuplication`, `R.bulk('remove', ...)`. Each assembly has
  its own transitional item from `transition_items.js` (`R.incomplete(key)`).
- **Felsic config:** `R.cfg(defaults, 'CRAFTING.FELSIC_<PIECE>')` reads `CEConfig.CRAFTING.<PIECE>.{potion, mb, loops,
  blows}` (Java field names) and logs each value that differs from the default. A missing section or field falls back
  to the default with a warning.
- **NBT carry:** `@Inject` at `RETURN` of Create's `SequencedAssemblyRecipe#advance(ItemStack)`, `remap = false`.
- **Armor enchantability:** `ArmorMaterialEnchantabilityMixin` injects at the head of `ArmorItem#getEnchantmentValue`
  (`m_6473_`) and returns the `[armor_enchantability]` value for the item's material name (`echo_armor`, `felsic`).
  Any exception leaves vanilla's value. Covers every piece of a material at once; add a `"name:value"` row for more.
- **Autorepair:** `PlayerEvents.tick` and `LevelEvents.tick` gated on the server tick count; tag membership through
  `Ingredient.of('#...').test(stack)`, because Rhino can't pick between `ItemStack.is(Item)` and `is(TagKey)`.
- **Elytra duplication:** `ElytraDuplicationRecipe extends CustomRecipe` with its serializer registered in
  `CreativeEngineering.java` (line 84). `getRemainingItems` hands the elytra back.
- **Rocket takeoff:** `ElytraChestplates.TAG` / `isWorn` is the one Java place that knows the tag; the tag is filled in
  [equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js) so more chestplates need no rebuild.
- **Air brake:** item in KubeJS (`.useAnimation(BOW)`, `.useDuration`, and a `.use(...) => true` callback, without
  which right click never starts using the item: the 2026-09-13 bug). Velocity in `AirBrakeHandler`, a Forge
  `PlayerTickEvent` (END phase) listener, because elytra flight is client-predicted and a server-only change is
  overwritten by the next movement packet. The stall clears the fall-flying flag through `Entity#setSharedFlag`
  (reflection on the SRG name `m_20115_`; the Mojmap name threw `NoSuchMethodException` at runtime).
- **Tooltips:** flavour lines for the felsic pieces ("Capable of binding to dragon scale"), dragon scale, dragon anvil
  ("Unbreakable!"), dragon tools, the elytra and the air brake are in [tooltips.js](../kubejs/client_scripts/tooltips.js)
  lines 697 and 801-819 ([tooltips.md](tooltips.md)).

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1.** FIXED 2026-10-19 in patcherMod 1.0.9, DEPLOYED with pack 1.0.9 ([plan_1.0.9.md](plan_1.0.9.md)). **The felsic shield has no config section.** [end_gear.js](../kubejs/server_scripts/equipment/end_gear.js) line 75
  asks for `CRAFTING.FELSIC_SHIELD`, which `CEConfig.Crafting` doesn't define, so every `/reload` logs
  `[NCH.cfg] no CEConfig.CRAFTING.FELSIC_SHIELD section` and the hardcoded 250 mB / 1 loop / 8 blows apply.
  [shields.md](shields.md) 2.7 lists it as if it were configurable. Fix: add
  `FELSIC_SHIELD = new FelsicPiece(b, "felsic_shield", magicResistance, 250, 1, 8)`.
- **F2. `potionMb` feeds both fills.** `felsicAssembly` uses the same mB for the Long Reach fill and the Magic
  Resistance fill; the toml comment says "used in the final filling step". A felsic helmet costs 500 + 500 mB a loop.
  Fix the comment, or split it into two keys.
- **F3.** FIXED 2026-10-19 ([plan_1.0.9.md](plan_1.0.9.md)). **Hand-written tag line.** [equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js) line 10 is
  `e.add('creativeengineering:elytra_chestplates', 'dragonloot:upgraded_dragon_chestplate')`, against the
  `global.tags` + item-dict rule. Fix: `global.tags.add(e, 'creativeengineering:elytra_chestplates', [i.upgraded_dragon_chestplate])`.
- **F4. Mining level 5 probably does nothing over 4. Your call.** As far as I know Forge 43 (unverified, no jars in the
  repo): a tier not in `TierSortingRegistry` (KubeJS's per-item tier copies are not) falls back to vanilla's level check,
  and nothing in vanilla needs more than 3; netherite-gated mod blocks (`forge:needs_netherite_tool`) are only checked
  for sorted tiers. So echo, felsic and dragon all mine everything, and dragon's 5 is cosmetic. Check: a block in
  `forge:needs_netherite_tool` with an echo pickaxe (GR-05).
- **F5. What the assembly drops. Your call.** Damage is not carried, so running worn gear through a step also repairs
  it (likely fine: it's a new item). Armor Trims' `Trim` tag and shield banners are lost. Adding `Trim` (and
  `BlockEntityTag`) to the carry list is one line each.
- **F6. Non-damageable items in `#dragon_gear`.** The dragon shield is damageable and should stay. The dragon anvil
  (a block item) and the air brake (no durability) are skipped every pass. Harmless; drop them or leave them.
- **F7.** CONFIRMED and FIXED 2026-10-19: Forge 43's `ConfigValue.get()` returns the default in production before load; the script now reads it on each use ([plan_1.0.9.md](plan_1.0.9.md)). **`useDurationTicks` probably never reaches the item.** `air_brake.js` line 26 reads the config at script load,
  in KubeJS's constructor, before Forge loads common configs; a 1.19 Forge config value read that early returns its
  default in production (from memory of Forge 43; unverified). So a toml change is likely ignored even after a restart.
  Harmless at 72000. Check: GR-28.
- **F8. Server velocity echo while braking, unverified.** The handler sets `hurtMarked` on the server each braking
  tick, which sends the player their own motion each tick. If server and client drift, that could stutter. Watch for
  it in GR-24.
- **F9. Felsic sword durability 2502**, every other felsic tool 2501. Typo or deliberate.
- **F10. Dragon bow, crossbow, trident and horse armor are untuned. Your call.** They keep DragonLoot's numbers (not in
  `/dumpgear`; `tune_tools.js` comment near the end lists what applies). Horse armor protection isn't reachable from
  `tuneArmor` (it's a `HorseArmorItem`).
- **F11. The ground-check mixin is pinned to one jar.** `@ModifyVariable(index = 8)` targets a local slot in Easy
  Elytra Takeoff 4.2's bytecode; any update of that mod needs a recheck (also in `mod_todo.txt` 16). The Alex's Mobs
  items named in `todo.txt` aren't in `#elytra_chestplates`; the helper mixin already accepts any item with
  `canElytraFly`, so only the ground rule would differ for them ([TODO](todo/TODO.md) `gear/tuning #7`; the version pin is `pack/server #7`).
- **F12.** FIXED 2026-10-19 (example uses `mb` / `blows`). **Stale doc in NCH.** The `NCH.cfg` example in
  [neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js) lines 621-626 uses the toml keys
  (`potionMb`, `hammerBlows`); the helper reads Java field names (`mb`, `blows`). Copying the example would fall back
  to defaults with a warning per field.
- **F13. The dragon set is over the armor cap.** 7 + 12 + 10 + 7 = 36 armor, but vanilla's `generic.armor`
  attribute is clamped to 30 and no mod in [MODLIST.md](../pack/MODLIST.md) is known to lift it (no AttributeFix). So
  6 points probably do nothing. Toughness (18 of a 20 cap) is fine. GR-02 shows
  which. Options: accept it, trim the dragon pieces to 30, or add a cap lift.

### Known limits

- **Restart for stats:** `ItemEvents.modification` is a startup event.
- **Armor enchantability and repair item can't be set per item** from KubeJS; enchantability goes per material
  through the mixin; repair items for armor are untouched.
- **Duplication isn't in JEI:** `CustomRecipe` is a special recipe, so JEI ignores it. A stand-in is planned in
  [jei_coverage.md](jei_coverage.md) (JC-02); the dragon scale tooltip says how instead.
- **Duplication output is always vanilla:** a styled or enchanted original stays as it was; the copy is plain.
- **Autorepair needs the item in use:** carried gear in the inventory doesn't repair.
- **Air brake is client-trusting:** the server runs the same math, but the client's movement wins.

### Rejected options

- **Allurement's Reforming for autorepair:** see 2.6.
- **Literal echo-sword mining level 1 for the echo tools:** the pickaxe couldn't mine diamond ore.
- **Air brake in a server script:** overwritten by client prediction; the brake had no visible effect.
- **Echo armor enchantability through `tuneArmor`:** no KubeJS accessor; chose the material-name mixin.

## 5. File map

- [startup_scripts/func/tune_tools.js](../kubejs/startup_scripts/func/tune_tools.js), [startup_scripts/func/tune_armor.js](../kubejs/startup_scripts/func/tune_armor.js): the helpers and the tables.
- [startup_scripts/items/echo_tools.js](../kubejs/startup_scripts/items/echo_tools.js): `echo` tier and the four tools; textures `kubejs/assets/creativeengineering/textures/item/echo_<tool>.png(.mcmeta)`, [tools/echo_texture.py](../tools/echo_texture.py).
- [startup_scripts/items/air_brake.js](../kubejs/startup_scripts/items/air_brake.js): the air brake item.
- [server_scripts/equipment/end_gear.js](../kubejs/server_scripts/equipment/end_gear.js): ladder recipes and removals.
- [server_scripts/equipment/dragon_autorepair.js](../kubejs/server_scripts/equipment/dragon_autorepair.js): autorepair.
- [server_scripts/tags/equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js): `#dragon_gear`, `#elytra_chestplates`.
- [startup_scripts/func/neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js): `cfg` (~line 630), `elytraDuplication`, `echoAssembly`, `felsicAssembly`, `dragonAssembly` (~lines 1061-1110).
- [startup_scripts/changes/transition_items.js](../kubejs/startup_scripts/changes/transition_items.js): the incomplete items.
- [server_scripts/_lib/dump_gear.js](../kubejs/server_scripts/_lib/dump_gear.js): `/dumpgear`.
- patcherMod: [gear/AirBrakeHandler.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/gear/AirBrakeHandler.java), [gear/ElytraDuplicationRecipe.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/gear/ElytraDuplicationRecipe.java), [gear/ElytraDuplicationSerializers.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/gear/ElytraDuplicationSerializers.java), [util/ElytraChestplates.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/util/ElytraChestplates.java), [util/ArmorEnchantabilityBridge.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/util/ArmorEnchantabilityBridge.java).
- Mixins: [SequencedAssemblyRecipeMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/SequencedAssemblyRecipeMixin.java), [ArmorMaterialEnchantabilityMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ArmorMaterialEnchantabilityMixin.java), [EasyElytraTakeoffHelperMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/EasyElytraTakeoffHelperMixin.java), [EasyElytraTakeoffGroundCheckMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/EasyElytraTakeoffGroundCheckMixin.java).
- `CEConfig.DragonGear`, `AirBrake`, `Crafting` (+ `FelsicPiece`), `ArmorEnchant` in [CEConfig.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/CEConfig.java).
- Not here: Customizable Elytra's banner-pattern fix (`CustomizableElytraBannerPatternMixin`, backlog row 10 "banner
  patterns"); effects on this gear ([armor_effects.md](armor_effects.md)); shields ([shields.md](shields.md)).

## 6. Offline checks

No script yet. Done by hand on 2026-10-18:

- Every `i.<key>` in the tune files, `end_gear.js` and `equipmenttags.js` resolves in `item_dict.js`.
- Every `R.cfg` path in `end_gear.js` against `CEConfig.Crafting`: F1 is the only miss.
- `remap = false` targets and the EET local slot can only be checked against the mod jars (none in the repo) or by a
  boot; all four mixins are in the deployed jar with no logged injection failure.

Worth building if this area changes again: a check that parses the tune tables, flags a tier that isn't an upgrade
over the one below it (the F9 kind of slip), and cross-checks `R.cfg` paths against `CEConfig.java`.

## 7. In-game test suite

Survival unless noted. `/dumpgear` after a restart gives every stat for GR-01 to GR-03.

- [ ] **GR-01 Tool stats:** hover the echo, felsic and dragon pickaxe and sword → damage and speed as in 2.2; all epic
  (purple name). `/dumpgear tools` matches the table.
- [ ] **GR-02 Armor stats:** wear a full felsic set → 24 armor on the HUD; each piece's tooltip shows +4 Armor
  Toughness. Full dragon set → note what the HUD shows: 36 if something lifts vanilla's 30 cap, 30 if not (F13).
  The bar draws extra rows past 20 (armor bar fix, backlog row 10).
- [ ] **GR-03 Fireproof:** throw a felsic sword and an echo helmet into lava → both survive.
- [ ] **GR-04 Echo tools exist:** JEI shows Echo Pickaxe, Axe, Shovel, Hoe with animated textures; the echo pickaxe
  mines obsidian and ancient debris.
- [ ] **GR-05 Mining level (F4):** with an echo pickaxe, mine a block from `#forge:needs_netherite_tool` (check
  `/dumpall` tag export for one) → note whether it drops.
- [ ] **GR-06 Echo assembly:** Create assembly line: netherite pickaxe through lava, echo ingot, amethyst dust,
  vibrating, Darkness, Weakness → echo pickaxe. JEI shows the same, and no CDD echo recipe remains.
- [ ] **GR-07 Felsic assembly:** echo helmet through 500 mB Long Reach, felsic ingot, 5 hammer blows, 500 mB Magic
  Resistance → felsic helmet.
- [ ] **GR-08 Felsic config:** `[crafting.felsic_sword] hammerBlows = 6`, `/reload` → log line
  `[NCH.cfg] CRAFTING.FELSIC_SWORD.blows overridden: default 3 -> 6`, JEI shows 6 blows. Put it back.
- [ ] **GR-09 Felsic shield config (F1):** `/reload` → log has `[NCH.cfg] no CEConfig.CRAFTING.FELSIC_SHIELD section`.
  (After the fix: no such line.)
- [ ] **GR-10 Dragon assembly:** felsic chestplate through dragon's breath, dragon scale, laser cutting, lingering Long
  Resistance, 4 loops → dragon chestplate; 4 scales used.
- [ ] **GR-11 Felsic ingot:** netherite sheet, 4 loops of felsic scrap, brass ingot, 250 mB dragon's breath, press →
  felsic ingot. Felsic Gear's own ingot recipe is gone.
- [ ] **GR-12 Removed recipes:** JEI on a dragon sword → only the assembly, no smithing table recipe; same for a felsic
  sword. The upgraded dragon chestplate keeps its recipe.
- [ ] **GR-13 NBT carried:** enchant (Sharpness V) and rename a netherite sword, run it through echo assembly → the echo
  sword has the name and Sharpness V. Carry it on to felsic and dragon → still there.
- [ ] **GR-14 Damage not carried (F5):** a half-broken netherite sword through echo assembly → the echo sword is at full
  durability.
- [ ] **GR-15 Plain inputs unchanged:** a plain netherite sword through echo assembly → plain echo sword with no
  `CEStash` tag (`/data get entity @s SelectedItem`).
- [ ] **GR-16 Armor enchantability:** enchanting table with echo boots vs netherite boots at 30 levels, a few rolls each
  → echo rolls are on average better (18 vs 15). Felsic (21) better still.
- [ ] **GR-17 Enchantability live:** `[armor_enchantability] overrides` → `"echo_armor:40"` → echo boots roll
  noticeably higher enchantments. Put it back.
- [ ] **GR-18 Autorepair worn:** damage a dragon helmet (`/item modify` or fight), wear it → +50 durability every 10 s
  (F3+H).
- [ ] **GR-19 Autorepair held, not carried:** damaged dragon sword in the main hand → repairs; the same sword in a hotbar
  slot not selected → doesn't.
- [ ] **GR-20 Autorepair config:** `[dragon_gear.autorepair] enabled = false` → no repair. `intervalTicks = 20` → once a
  second. Put both back.
- [ ] **GR-21 Horse armor repair:** damaged dragon horse armor on a horse → repairs within 10 s.
- [ ] **GR-22 Elytra duplication:** crafting grid, enchanted elytra (Unbreaking III, part-damaged) + dragon scale → a
  new plain elytra; the enchanted one stays in the grid unchanged; the scale is gone. Elytra + scale + anything else →
  no result.
- [ ] **GR-23 Rocket takeoff, dragon chestplate:** wear `upgraded_dragon_chestplate`, stand on one glass block over a
  drop, use a firework → launch into gliding. Same while falling from a ledge without jumping.
- [ ] **GR-24 Air brake slows:** glide fast, hold right click with the air brake → bow pose; forward speed drops
  steadily; climbing stops; a dive is only trimmed. Watch for stutter (F8).
- [ ] **GR-25 Air brake stall:** keep braking → after about a second you drop out of gliding.
- [ ] **GR-26 Air brake config:** `stallEnabled = false` → braking slows you but you keep gliding.
  `requireFallFlying = false` → holding it on the ground does nothing visible but no error; jumping off a ledge while
  holding it slows the fall slightly. Put both back.
- [ ] **GR-27 Air brake off:** `[air_brake] enabled = false` → holding it changes nothing. Put it back.
- [ ] **GR-28 Use duration (F7):** set `useDurationTicks = 40`, restart, hold the brake while gliding → if it keeps
  braking past 2 s, F7 is confirmed. Put it back.
- [ ] **GR-29 Vanilla elytra rocket rule unchanged:** vanilla elytra, standing on one glass block over a drop, firework
  → stock Easy Elytra Takeoff behaviour (no forced takeoff).
- [ ] **GR-30 Tooltips:** felsic pieces show "Capable of binding to dragon scale"; the air brake, dragon scale and
  dragon anvil show their lines.

## 8. History

- 2026-09-13: air brake did nothing (right click never started using the item); fixed with the `.use(...)` callback.
  Stall effect resolved by SRG name after a `NoSuchMethodException`.
- By 2026-09-27 (initial git commit): tune helpers and tables, echo tools, ladder recipes, felsic config values,
  autorepair, air brake item; shipped with 1.0.3. The Java side (air brake handler, elytra duplication, rocket
  takeoff mixins, assembly NBT mixin, armor enchantability mixin) was in the jar and first committed 2026-10-02.
- 2026-10-18: this writeup; findings F1 to F13; tests GR-01 to GR-30. No code changed.
- 2026-10-19: F1 (`CRAFTING.FELSIC_SHIELD` in CEConfig, patcherMod 1.0.9), F3 (tag helper) and F7 (`useDurationTicks` read per use) fixed ([plan_1.0.9.md](plan_1.0.9.md)). Not run in game; GR-28 now expects a toml change to apply after a config reload.
- 2026-10-19: F12 fixed (NCH.cfg example uses the Java field names).
