# Shields: tier shields, the folded-in iron shield, and the damage tags they read

## 0. At a glance

| Part | Status |
|---|---|
| Seven tier shield items (copper, gold, diamond, netherite, echo, felsic, dragon) with their own textures | DEPLOYED (patcherMod, shipped since 1.0.3) |
| Vanilla shield folded in as the iron tier (blocking, durability, repair item, tooltip) | DEPLOYED |
| Per-tier reduction for physical / elemental / magic, elemental breakdown (fire, electric, cold, chemical) | DEPLOYED; percentage reduction PASSED in game 2026-09-24 |
| Blocking damage vanilla shields can't (fire, poison, lightning, magic), with per-group facing rules | DEPLOYED, in-game test pending |
| Scaled durability wear per damage group | DEPLOYED, in-game test pending |
| Round-down rule (under 1 damage left = full block) and per-tier knockback threshold | DEPLOYED, in-game test pending (the first knockback attempt was wrong and was replaced) |
| On-block potion effects for holder and attacker (dragon: Regeneration / Poison) | DEPLOYED, in-game test pending |
| Resistance on block from echo up (echo I, felsic II, dragon II, 2 s; `gear/shields #5`) | BUILT 2026-10-22: `ShieldTier` defaults + the instance `creativeengineering-common.toml` (ships in `config/`), patcherMod 1.1.01, DEPLOYED with 1.1.01 (2026-10-22); scratch server PASSED 2026-10-22 (SQ-GM-01); tooltip and a real block in game (SH-44) |
| Shield-disable cooldown (`disableCooldownTicks` = 20), player and mob axes, every shield | player axes DEPLOYED; mob axes BUILT 2026-10-05, in the patcherMod 1.0.5 jar, DEPLOYED with 1.0.6; boots clean, not tested in game |
| Burning and hot-block ticks (fire, campfires, magma, stoves) only blocked by netherite and above (`burn_tick`, `blocksBurnTicks`) | BUILT 2026-10-05, in the patcherMod 1.0.5 jar, DEPLOYED with 1.0.6; boots clean (single player 2026-10-11), not tested in game |
| Thorn ticks (cactus, berry bushes, thorny plants, barbed panes) only blocked by diamond and above (`contact_tick`, `blocksContactTicks`) | BUILT 2026-10-05, in the patcherMod 1.0.5 jar, DEPLOYED with 1.0.6; boots clean (single player 2026-10-11), not tested in game |
| Effect ticks never blocked: poison, wither, copper poisoning, Debilitating Sting, Exsanguination, Ender Flu, Phasing (`unblockableEffectTicks`) | BUILT 2026-10-05, in the patcherMod 1.0.5 jar, DEPLOYED with 1.0.6; boots clean (single player 2026-10-11), not tested in game |
| Banner decoration in the crafting grid for every tier | BUILT 2026-10-05, in the patcherMod 1.0.5 jar, DEPLOYED with 1.0.6; boots clean (single player 2026-10-11), not tested in game |
| Recipe ladder (shaped, smithing, echo / felsic / dragon assembly) | DEPLOYED (KubeJS) |
| Damage tag layer (32 tag files, reload listener, seen-sources log) | DEPLOYED (30 since 1.0.3; `burn_tick`, `contact_tick` built 2026-10-05, DEPLOYED with 1.0.6) |
| Offline checks `tools/test_shields.sh` (`shield_check.py` + `ShieldConfigTest`) | BUILT 2026-10-05, passes (32 checks) |
| Smithing to netherite needs Armor Trims' Netherite Upgrade Smithing Template (F8); JEI's vanilla Smithing category no longer lists template-only recipes (`SmithingTemplateJEI`) | template rule: Armor Trims config, kept; JEI fix BUILT 2026-10-05 20:56, in the patcherMod 1.0.5 jar (DEPLOYED with 1.0.6); SH-43 pending |

- **Pending:** everything from 2026-10-05 was built for pack 1.0.5 (`creativeengineering-1.0.5.jar` md5 68509043),
  which never shipped, and went out with pack 1.0.6 (DEPLOYED 2026-10-14). That jar booted in single player on 2026-10-11 with no `InvalidInjectionException`
  from the three new vanilla-class mixins (`MobShieldDisableMixin`, `MobEffectTickContextMixin`,
  `ShieldDecorationRecipeMixin`). Still to run in game: SH-43, then SH-14, SH-19, SH-33 to SH-42.

- **Files to open:** [ShieldTier.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/ShieldTier.java) (defaults),
  [ShieldBlocking.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/ShieldBlocking.java) (can it block),
  [ShieldBlockHandler.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/ShieldBlockHandler.java) (how much, wear, knockback, effects),
  [shield.js](../kubejs/server_scripts/equipment/shield.js) (recipes).
- **Knobs:** `config/creativeengineering-common.toml`: `[shields]` (enabled, wearThreshold, disableCooldownTicks,
  unblockableEffectTicks), `[shields.facing]`, `[shields.<tier>]` (durability, repairItem, knockbackThreshold, blocksBurnTicks, blocksContactTicks, shielderEffects,
  attackerEffects, `reduction.*`, `elemental_breakdown.*`, `durabilityCost.*`), `[damage_tags] logSeenSources`.
  All live: edit in the toml or Create's config screen, no restart. Damage tag files reload with `/reload`.
- **New keys:** `blocksBurnTicks`, `blocksContactTicks` and `unblockableEffectTicks` are not in the toml until the new jar has run
  once; Forge then writes their defaults.
- **The toml wins.** The live toml is what plays and what ships. The `ShieldTier.java` defaults only fill a
  fresh toml, and they differ (physical copper 40 in code, 20 in the toml). `python3 tools/shield_check.py`
  prints both.
- **How to measure:** F3+H shows durability; hold Shift on a shield for the full stat block; turn on
  `logSeenSources` to see how a damage source is tagged.

## 1. Overview

In vanilla 1.19.2 (Forge 43.3.9) a raised shield blocks a hit only when all of these hold: the hit is not
armour-bypassing, the shield has been up for 5 ticks (0 here: Responsive Shields sets Raise Time to 0), the
hit is not from a piercing arrow, and the source has a position inside the front 180 degrees. A block absorbs
100% of the damage. Fire, poison, magic, lightning and anything without a position can never be blocked.
Wear is `1 + floor(damage)` for hits of 3 or more. An axe hit disables the shield for 100 ticks (5 s). The
blocking player still takes the attacker's knockback.

The pack replaces that with a ladder of eight tiers. Each tier absorbs a percentage of the hit, set separately
for three damage groups (physical, elemental, magic), and can block damage vanilla never could.

## 2. Reference

### 2.1 Tiers (live toml, 2026-10-05)

| Tier | Item | Durability | Physical | Elemental | Magic | KB threshold | Burn / thorn ticks | Repair item | Extras |
|---|---|---|---|---|---|---|---|---|---|
| copper | `creativeequipment:copper_shield` | 242 | 20 | electric 50, rest 0 | 0 | 1 | no / no | copper ingot | |
| gold | `creativeequipment:gold_shield` | 168 | 30 | 30 (blanket) | 30 | 2 | no / no | gold ingot | |
| iron | `minecraft:shield` (renamed "Iron Shield") | 336 | 40 | fire 25, rest 0 | 0 | 3 | no / no | iron ingot (not planks) | |
| diamond | `creativeequipment:diamond_shield` | 1561 | 50 | fire 50, rest 0 | 25 | 4 | no / yes | diamond | rare |
| netherite | `creativeequipment:netherite_shield` | 2031 | 60 | fire 100, rest 0 | 50 | 5 | yes / yes | netherite ingot | rare, fireproof |
| echo | `creativeequipment:echo_shield` | 2124 | 70 | 50 (blanket) | 60 | 6 | yes / yes | echo ingot | epic, fireproof |
| felsic | `creativeequipment:felsic_shield` | 2501 | 80 | 75 (blanket) | 75 | 7 | yes / yes | felsic ingot | epic, fireproof |
| dragon | `creativeequipment:dragon_shield` | 3350 | 100 | 100 (blanket) | 100 | 8 | yes / yes | dragon scale | epic, fireproof; holder gets Regeneration I 5 s, attacker gets Poison I 5 s; dragon-gear autorepair |

Wear is 100% (vanilla) for every tier and group. Global: `wearThreshold = 3.0`, `disableCooldownTicks = 20`,
`unblockableEffectTicks` = poison, wither, `everythingcopper:copper_poisoning`, `alexsmobs:debilitating_sting`,
`alexsmobs:exsanguination`, `alexsmobs:ender_flu`, `endersdelight:phasing`; facing physical `REQUIRED`,
elemental and magic `IF_POSITIONED`. "Burn / thorn ticks" are `blocksBurnTicks` (default from netherite up) and
`blocksContactTicks` (default from diamond up).

### 2.2 How a hit is classified

The hit's damage tags (section 2.4) decide its category; the first match wins, in this order:

| Category | Tag | Group | Example |
|---|---|---|---|
| explosion | `explosion` | physical | creeper, TNT, Supplementaries bomb |
| projectile | `projectile` | physical | arrow, trident, ghast/blaze fireball, potato cannon |
| magic | `magic` | magic | poison, instant damage, evoker fangs, thorns, warden boom, Farseer, Botania relic, health siphon |
| elemental | `elemental` | elemental | burning, lava, campfire, powder snow, icicle, lightning, IE tesla, acid |
| melee | `melee` | physical | zombie, player sword, slap |
| other | none | physical | cactus, falling block, wither effect |

A fireball is in both `projectile` and `fire`; projectile comes first, so it is physical. That is the only
message id in two category tags today (`tools/shield_check.py` section 4).

Elemental hits then pick one kind, first match in the order fire, electric, cold, chemical. That only matters
for a tier in breakdown mode.

### 2.3 Can the shield block it? (`ShieldBlocking.verdict`)

Only for a raised tier shield (a `TierShieldItem` or `minecraft:shield`); for everything else vanilla runs
untouched. In order:

1. Not raised, or the hit is tagged `bypasses_invulnerability` (void, `/kill`): no.
2. Piercing arrow: no (a vanilla rule, not a damage tag).
3. Damage from a ticking effect listed in `unblockableEffectTicks`: no. Recognised by which effect is ticking
   on the holder (`EffectTickContext`), because poison hurts with the same `magic` source as Instant Damage, so
   no tag can tell them apart.
4. A tick gate (`ShieldTickGate`) the tier doesn't have: no. `burn_tick` (fire, campfires, magma, stoves) needs
   `blocksBurnTicks`; `contact_tick` (cactus, berry bushes, thorny plants, barbed panes) needs
   `blocksContactTicks`. Projectiles never count (an ownerless fireball also uses the `onFire` id).
5. The tier's reduction for this hit is 0: no. The hit is ordinary damage, no shield sound.
6. A gated tick the tier does have: yes, from any direction, armour-piercing or not.
7. Physical hit tagged `armor_piercing`: no (vanilla's rule). Elemental and magic ignore this tag, because
   almost all of them carry it.
8. Facing for the group:
   - `REQUIRED`: the source needs a position in front (vanilla).
   - `IF_POSITIONED`: front only when the source has a position; positionless hits (lava, magma, burning,
     lightning) are blocked from any side.
   - `NONE`: any side.

### 2.4 What a block does (`ShieldBlockHandler`)

- **Absorbs** `incoming x reduction%`. If less than 1 damage would get through, it absorbs the whole hit.
  - Zombie (3) on copper (20%): 2.4 gets through.
  - Lava tick (4) on netherite (fire 100%): 0.
  - Magma tick (1) on netherite (fire 100%): 0. On diamond (fire 50%) it isn't attempted at all
    (`blocksBurnTicks = false`).
  - Cactus tick (1) on diamond (physical 50%): 0.5 would get through, under 1, so the whole tick is blocked.
- **Wear** (players only): `(1 + floor(incoming)) x durabilityCost%` on hits of `wearThreshold` or more. The
  fraction is rolled, so 50% averages out exactly. Uses the incoming damage, not the reduced damage. Mobs
  holding shields use vanilla wear (MineColonies guards keep their own bleed-through).
- **Knockback:** if the damage that gets through is under the tier's `knockbackThreshold`, the next
  knockback the holder takes is cancelled. That's the push from the attacker's hit. The attacker's own recoil
  from hitting a shield is still vanilla.
  - Zombie (3) on netherite (60%): 1.2 gets through, under 5, so no push.
  - The same zombie on copper: 2.4 gets through, over 1, so you are pushed.
- **Effects:** `shielderEffects` go to the holder and `attackerEffects` to the living attacker, on every
  blocked hit. Format: `effectId;ticks[;amplifier[;category|category]]`, comma separated. The filter takes
  group names (physical, elemental, magic) or categories (melee, projectile, explosion, other, ...). Bad
  entries log one warning and are skipped.
  Defaults (2026-10-22): echo `minecraft:resistance;40;0`, felsic `minecraft:resistance;40;1`, dragon
  `minecraft:regeneration;100;0, minecraft:resistance;40;1` (attacker: poison 5 s); lower tiers none.

### 2.5 Elemental breakdown

All four `elemental_breakdown` values at -1 means blanket mode: `reduction.elemental` applies to every
elemental hit. Setting any one of them to 0-100 switches the tier to breakdown mode. `reduction.elemental`
is then ignored, and an element left at -1 blocks 0%. Wear is not split.

The tooltip shows one line per non-zero element, in the order cold, chemical, fire, electric. Shift shows
all four, also for blanket tiers.

### 2.6 Tooltip

- **Compact view:** one line per non-zero group, e.g. `Physical: 60%`, coloured white under 40, green
  40-59, blue 60-99, purple 100. Wear shows only when it isn't 100%, e.g. `(50% wear)`. Then the effect
  lists and "Hold [SHIFT] for full stats".
- **Shift or F3+H:** every line including zeros, plus `Fireproof`, `Blocks burning`, `Blocks thorns` and
  `Knockback threshold`.
- The iron shield gets the same lines through `IronShieldTooltip`. See [tooltips.md](tooltips.md).

### 2.7 Recipes

| Output | How |
|---|---|
| Copper shield | shaped `RWR / WPW / RWR`: copper rods, any planks, copper plate |
| Iron shield | same shape with iron rod/plate (vanilla recipe removed), or smithing copper shield + iron ingot |
| Gold shield | same shape with gold rod/plate, or smithing copper shield + gold ingot |
| Diamond shield | smithing iron or gold shield + diamond |
| Netherite shield | smithing diamond shield + netherite ingot |
| Echo shield | echo sequenced assembly from the netherite shield (Darkness + Night Vision) |
| Felsic shield | felsic assembly from the echo shield (250 mB Magic Resistance, 1 loop, 8 blows; `[crafting.felsic_shield]` in `creativeengineering-common.toml` since patcherMod 1.0.9: [gear_tuning.md](gear_tuning.md) F1) |
| Dragon shield | dragon assembly from the felsic shield (250 mB Long Resistance, 5 scales) |

Any tier takes a banner in the crafting grid (shield + banner, like vanilla). The Engineer starter kit
includes a copper shield.

### 2.8 1-damage tick sources

Everything in the pack that hurts in repeated 1-damage ticks (bytecode scan 2026-10-05 of every effect tick,
`entityInside` and `stepOn` in vanilla and all mod jars, plus the entity tick). Under the round-down rule,
any tier that blocks one of these at all blocks it completely, with a shield sound per tick.

| Source (msgId) | From | Shields today |
|---|---|---|
| `onFire` | burning after ignition, every 20 ticks | `burn_tick`: netherite and above |
| `inFire` | fire block (soul fire 2), campfire (soul 2), Tinted Campfires, Decorative Blocks bonfire and brazier, Vinery burning log pile, Salt cauldron | `burn_tick`: netherite and above |
| `hotFloor` | magma block, Immersive Weathering nulch, Immersive Petroleum flarestack | `burn_tick`: netherite and above |
| `farmersdelight.stove`, `endersdelight.endstone_stove` | Farmer's Delight stove, Nether's Delight blackstone stove (soul 2), Ender's Delight endstone stove | `burn_tick`: netherite and above |
| `cactus`, `sweetBerryBush` | cactus; berry bush, Immersive Weathering leaf piles and weeds, Vinery taiga grape bush | `contact_tick`: diamond and above |
| `farmersrespite.witherRoots`, `neapolitan.beanstalkThorns` | Farmer's Respite wither roots, Neapolitan beanstalk thorns | `contact_tick`: diamond and above |
| `barb` | Dustrial Decor barbed pane | `contact_tick`: diamond and above |
| `magic` (poison), `wither` | Poison, Wither effects | never (`unblockableEffectTicks`) |
| `everythingcopper.copper_poisoning*` | Copper Poisoning effect | never (`unblockableEffectTicks`) |
| `magic` | Alex's Mobs Debilitating Sting, Exsanguination, Ender Flu effects | never (`unblockableEffectTicks`) |
| `wasWatered` | Ender's Delight Phasing effect | never (`unblockableEffectTicks`) |
| `freeze` | powder snow, every 40 ticks (5 to freeze-vulnerable mobs) | cold %: gold, echo, felsic, dragon |
| `acid` | PneumaticCraft etching acid | chemical %: gold, echo, felsic, dragon (kept, by request) |
| `inWall`, `starve` | suffocation, hunger | never (armour-piercing physical) |

Other periodic sources that aren't 1 damage: `drown` 2, `create.fan_fire` 2, `lava` 4, `create.fan_lava` 4,
`cramming` 6, Botania relic 2, Upgrade Aquatic guardian spine 3, SecurityCraft fake water 4, Create Addition
barbed wire and Supplementaries bamboo spikes (speed-based).

To change a row: an effect goes in `unblockableEffectTicks`; a block source goes in `burn_tick.json`
(netherite+) or `contact_tick.json` (diamond+); the per-tier switches are `blocksBurnTicks` and
`blocksContactTicks`.

### 2.9 Damage tags

1.19.2 has no damage-type registry, so the pack has its own tags. Each tag is a list of `DamageSource`
message ids in `data/creativecommon/damage_tags/<name>.json`:

- **Entries:** exact ids, `*` globs, and `#creativeengineering:<tag>` includes. Any pack can add to a tag;
  `"replace": true` overrides lower packs.
- **Flag tags:** twelve tags also follow the source's live vanilla flags: `armor_piercing`, `fire`, `magic`,
  `explosion`, `projectile`, `fall`, `melee`, `difficulty_scaled`, `helmet_damage`, `bypasses_resistance`,
  `bypasses_enchantments`, `bypasses_invulnerability`. A file can add ids to them but can't remove what a
  flag sets.
- **Meta tags:** `elemental` = fire + cold + electric + chemical. `physical` = melee + machinery + piercing +
  firearm + explosion + projectile. `magic` = the vanilla magic ids + psychic + life_drain.
- **All 32:** armor_piercing, blunt, burn_tick, bypasses_enchantments, bypasses_invulnerability, bypasses_resistance,
  chemical, cold, contact_tick, cutting, difficulty_scaled, electric, elemental, environmental, explosion, fall, fire,
  firearm, helmet_damage, life_drain, machinery, magic, melee, misc, physical, piercing, pneumatic,
  projectile, psychic, security, suffocation, void.
- **Reclassifying a source:** add its id to the right finer tag. To move a whole kind between shield
  columns, edit the `#include`s in `elemental.json` or `magic.json`. No Java either way.
- **Ids are shared:** `mob`, `player` and the bare `arrow` are used by unrelated mechanics, so they are
  deliberately not listed. Only the live flag tells them apart.
- **Catalogue:** [damage_types_findings.md](../damage_types_findings.md) lists every source in the pack
  with its flags.

## 3. What the pack changes

- **Tier items:** `TierShieldItem extends ShieldItem`, so MineColonies guards accept them.
  - Registered for every tier except iron.
  - Durability and repair item are read from config on every call.
  - Drawing: `TierShieldRenderer` repeats vanilla's shield render (model, banner overlay, glint) with the
    tier's own base and no-pattern sprites (`TierShieldMaterials`, stitched into the block atlas). Textures:
    `kubejs/assets/creativeengineering/textures/entity/shield/<tier>_shield_base[_nopattern].png`.
  - `TierShieldClient` registers the `blocking` model predicate for each item.
- **Iron folded in:** `ShieldTier.of(stack)` maps `minecraft:shield` to the iron tier. That one lookup
  covers blocking, reduction and effects.
  - `VanillaShieldDurabilityMixin` (`Item#getMaxDamage`) and `VanillaShieldRepairMixin`
    (`ShieldItem#isValidRepairItem`) make its durability and repair item config driven.
  - Planks no longer repair it.
  - `namechanges.js` renames it "Iron Shield".
- **Blocking hook:** `LivingEntityShieldMixin` hooks `LivingEntity#isDamageSourceBlocked` at the start and
  asks `ShieldBlocking`. Any exception falls back to vanilla.
- **Block event:** `ShieldBlockHandler` listens to Forge's `ShieldBlockEvent` and `LivingKnockBackEvent`.
- **Disable cooldown (20 ticks), every shield:**
  - Player axes: `ShieldDisableCooldownMixin` swaps the 100 in `Player#disableShield` for
    `disableCooldownTicks`. IE's shock upgrade and the Alex's Mobs void worm shot use the same method.
  - Mob axes: `MobShieldDisableMixin` replaces `Mob#maybeDisableShield`. Vanilla only disabled exactly
    `minecraft:shield` (tier shields were immune), put a fixed 100 on it and never lowered the shield. Now any
    `SHIELD_BLOCK` item, `disableCooldownTicks` on the shield actually raised, and the shield is lowered like
    the player path. The chance stays vanilla's: 25% per hit, +5% per Efficiency level on the mob.
- **Effect ticks:** `MobEffectTickContextMixin` wraps the one call site of every effect's tick
  (`MobEffectInstance#applyEffect` -> `applyEffectTick`), server side only, so `ShieldBlocking` knows when a
  hit comes from a ticking effect and which one.
- **Banners:** `ShieldDecorationRecipeMixin` widens the two `is(Items.SHIELD)` checks in vanilla's
  `ShieldDecorationRecipe` to any `TierShieldItem`.
- **KubeJS:**
  - `item_dict.js` keys `i.ce_<tier>_shield`. The `ce_` prefix is because `i.copper_shield` is Everything
    Copper's removed shield.
  - Recipes: [shield.js](../kubejs/server_scripts/equipment/shield.js) and
    [end_gear.js](../kubejs/server_scripts/equipment/end_gear.js).
  - The dragon shield is in `#creativeequipment:dragon_gear`, so it repairs itself.
  - The echo, felsic and dragon assemblies have transitional items in `transition_items.js`.
- **Damage tags:** the `damage/` package.
  - `DamageTagLoader` (reload listener) and `DamageTagSnapshot` (merge plus a per-(id, flags) cache).
  - `DamageTags` is the API.
  - `DamageTagLogger` logs each new source once when `logSeenSources = true`.

## 4. Limits and decisions

### Findings (2026-10-05)

- ~~**F1. Mobs with axes ignore `disableCooldownTicks`.**~~ Fixed 2026-10-05 (`MobShieldDisableMixin`); it also
  turned out mob axes couldn't disable tier shields at all. SH-19.
- ~~**F2. Stale config comments**~~ (vanilla cooldown is 100, not 20; iron is a tier): fixed 2026-10-05. 20 ticks
  is the intended pack value.
- ~~**F3. Knockback comments describe the old behaviour.**~~ Fixed 2026-10-05.
- ~~**F4. Other stale javadocs**~~ (ladder numbers, "no recipes", "vanilla texture"): fixed 2026-10-05.
- **F5. Banner designs are lost in the assemblies.** Smithing keeps the shield's NBT, banner included. The
  echo, felsic and dragon assemblies carry only enchantments, name, repair cost, Unbreakable and attribute
  modifiers through (`SequencedAssemblyRecipeMixin`). `BlockEntityTag`, which holds the banner design, is not
  carried. Expected from the code, not seen in game; SH-27.
- **F6. Any reduction fully blocks 1-damage ticks.** Because of the round-down rule. Since 2026-10-05 burning, magma
  and stove ticks need `blocksBurnTicks` (netherite and above), thorn ticks need `blocksContactTicks` (diamond and
  above), and every damaging effect tick (poison, wither, copper poisoning, the Alex's Mobs magic effects, Phasing) is
  never blocked. Only powder snow (`freeze`, cold %) and etching acid (`acid`, chemical %, kept by request) are still
  blocked fully by any tier with a non-zero percentage; section 2.8 lists them and how to exclude each.
- ~~**F7. Only the iron shield takes a banner in the crafting grid.**~~ Fixed 2026-10-05
  (`ShieldDecorationRecipeMixin`). SH-33.
- **F8. Diamond to netherite smithing does nothing without a template.** Not a shield bug: Armor Trims
  (`armortrims-1.19.2-1.3.3`) replaces the smithing table (`SmithingMenuNew`), and `config/armor_trims.toml`
  has `"Disable Vanilla Netherite Upgrading" = true`. Its `createResult` (bytecode-read 2026-10-05):
  - Slots: 0 base, 1 template, 2 material.
  - No template in slot 1: any recipe whose material is in `#forge:ingots/netherite` gives no result. This
    applies to every netherite upgrade in the pack, vanilla gear included.
  - Netherite Upgrade Smithing Template in slot 1: the recipe runs when the material is in
    `#forge:ingots/netherite`. The base isn't checked against any list, so the diamond shield works. The
    template is used up (`"Don't Consume Smithing Templates" = false`).
  - JEI showed our KubeJS recipe in vanilla's two-slot smithing layout, without the template, which was
    misleading. Fixed 2026-10-05 by `compat/jei/SmithingTemplateJEI` (+ `ArmorTrimsSmithing`, the only class
    that touches Armor Trims, so the plugin is inert without the mod):
    - Armor Trims 1.3.3's own JEI plugin never filled its pages. It registers its categories as
      `RecipeType(uid, ArmortrimsRecipe.class)` but adds recipes as `RecipeType(uid, IArmortrimsRecipe.class)`.
      JEI 11.8 throws "no recipe category registered for armor_trims:armor_trimming" (`logs/latest.log`), so
      both the Armor Trimming and Item Upgrading pages were always empty.
    - The plugin now adds Armor Trims' trim recipes (its own maker) under the right type. It also builds the
      Item Upgrading entries itself: base + template + material for every smithing recipe whose material
      Armor Trims blocks without a template. Armor Trims' own upgrade maker is not used: it tries every JEI
      item x every template x every item.
    - It then hides exactly those re-shown recipes from the vanilla Smithing page, matched by recipe id.
    - It reads `ArmortrimsApi.upgradeBaseBlockedConditions` / `determiningUpgradeConditions` on every
      reload, so it follows Armor Trims' config.
    - Log lines: `[jei] Armor Trims pages: N trim recipe(s), M template upgrade recipe(s)` and
      `[jei] hid M template-only smithing recipe(s)`.
    - First version (20:13 jar) only hid the recipes on the assumption that Armor Trims' page had them; in
      game the 24 netherite recipes vanished from JEI entirely. Replaced 20:56.
  - Templates come from Armor Trims' bastion loot and its duplication recipe.
  - Decision 2026-10-05: keep the template rule; JEI now matches it (SH-43).
- **Open TODO in `ShieldTier`:** make the knockback thresholds more reasonable, and add a per-tier disable
  cooldown.

### Known limits

- **Unblockable modded sources:** armour-bypassing sources with no elemental or magic tag land in
  physical/other and can't be blocked. Examples: Create saw/crush/drill, dragon breath, the wither effect.
  This matches vanilla.
- **Mixed tags:** a hit tagged both `magic` and `cold` counts as magic only (category order) and never
  reaches the elemental breakdown.
- **Alex's Mobs:** Stradpole and Crocodile attacks skip `hurt()` when the target is blocking. Tiers can't
  touch them; they stay at 100% plus shield wear. This was decided, not missed.
- **Not tier shields:** the IE shield, the Alex's Mobs Shield of the Deep and the Skelewag sword are left on
  vanilla rules. Their wear and blocking are vanilla; only the disable cooldown (player and mob axes) reaches
  them. They take no banner.
- **Burn ticks vs lava:** `burn_tick` has fire, campfires, magma and stoves. Lava and Create fan fire keep
  using the tier's fire percentage.
- **Etching acid** stays on the chemical percentage (gold, echo, felsic, dragon), by request.
- **Effect ticks are matched by the effect ticking on the holder.** Damage an effect tick deals to someone
  else (a modded aura) is not treated as an effect tick for that other entity's shield.
- **Non-player holders:** they get the reduction and effects but vanilla wear.
- **Shield sound:** every block, partial ones included, plays the shield sound and skips the hurt animation.
  That's vanilla's `blocked` flag.
- **Breakdowns:** only elemental has one; magic has none (not requested).

### Rejected options

- **KubeJS items:** they can block (`useAnimation`), but they get no banner/shield renderer and MineColonies
  ignores them. Chose a Java `ShieldItem` subclass.
- **Hooking `LivingHurtEvent` instead of widening `isDamageSourceBlocked`:** the shield sound, the
  `blocked` flag and Forge's `ShieldBlockEvent` would all be lost for fire and magic. Chose the mixin.
- **An eighth item for iron:** chose to fold in the real vanilla shield, so existing shields and loot just
  work.
- **Effects as a toml list:** Create's config screen only edits strings, so they are one string.
- **Per-element wear:** not asked for; wear stays per group.
- **First knockback attempt:** a mixin on `LivingEntity#blockUsingShield` cancelled the attacker's recoil,
  which fires before the holder's push, so the holder was still pushed ("I take kb even on a half-heart
  netherite block"). Replaced by the holder-keyed `LivingKnockBackEvent` cancel.
- **Raw `DamageSource` flag checks in shield code:** the user asked for shields to read only the pack's
  damage tags. Two structural exceptions: piercing arrows, and effect ticks.
- **Poison as a damage tag:** poison ticks and Instant Damage both use vanilla's `magic` source, so a tag
  would catch both. Chose the effect-tick context instead.
- **Mob axe fix as a constant swap only:** would have left tier shields immune to mob axes and the shield
  raised. Chose to replace `Mob#maybeDisableShield` with the player path's behaviour.

## 5. File map

- [shield/ShieldTier.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/ShieldTier.java): tier enum, code defaults, `of(stack)`.
- [shield/ShieldGroup.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/ShieldGroup.java): physical / elemental / magic, default facing, armour-bypass rule.
- [shield/ShieldCategory.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/ShieldCategory.java): tag-based classification, priority order.
- [shield/ElementalKind.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/ElementalKind.java): fire / electric / cold / chemical.
- [shield/ShieldBlocking.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/ShieldBlocking.java): block verdict and facing.
- [shield/ShieldBlockHandler.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/ShieldBlockHandler.java): reduction, round-down, wear, knockback, effects.
- [shield/ShieldEffects.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/ShieldEffects.java): effect string parser.
- [shield/ShieldConfig.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/ShieldConfig.java): null-safe live config reads.
- [shield/ShieldTooltip.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/ShieldTooltip.java), [shield/IronShieldTooltip.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/IronShieldTooltip.java): tooltip lines.
- [shield/TierShieldItem.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/TierShieldItem.java), [shield/ShieldItems.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/ShieldItems.java): items and registration.
- [shield/client/](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/client/): renderer, sprites, blocking predicate.
- [shield/ShieldTickGate.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/ShieldTickGate.java): burn / contact tick gates.
- [compat/jei/SmithingTemplateJEI.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/jei/SmithingTemplateJEI.java), [ArmorTrimsSmithing.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/jei/ArmorTrimsSmithing.java): hide template-only smithing from JEI's vanilla category (F8).
- [shield/EffectTickContext.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/shield/EffectTickContext.java): which effect is ticking on which entity.
- Mixins: [LivingEntityShieldMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/LivingEntityShieldMixin.java), [VanillaShieldDurabilityMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/VanillaShieldDurabilityMixin.java), [VanillaShieldRepairMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/VanillaShieldRepairMixin.java), [ShieldDisableCooldownMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/ShieldDisableCooldownMixin.java), [MobShieldDisableMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/MobShieldDisableMixin.java), [MobEffectTickContextMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/MobEffectTickContextMixin.java), [ShieldDecorationRecipeMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/ShieldDecorationRecipeMixin.java).
- `CEConfig.Shields` / `CEConfig.DamageTagConfig` in [CEConfig.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CEConfig.java).
- [damage/](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/damage/): `DamageFlag`, `DamageTags`, `DamageTagSnapshot`, `DamageTagLoader`, `DamageTagLogger`.
- [damage_tags/](../development/creativeCommon/src/main/resources/data/creativecommon/damage_tags/): the 32 tag files ([burn_tick.json](../development/creativeCommon/src/main/resources/data/creativecommon/damage_tags/burn_tick.json), [contact_tick.json](../development/creativeCommon/src/main/resources/data/creativecommon/damage_tags/contact_tick.json)).
- Models: `development/creativeCommon/src/main/resources/assets/creativeengineering/models/item/<tier>_shield.json`; textures: [kubejs/assets/creativeengineering/textures/entity/shield/](../kubejs/assets/creativeengineering/textures/entity/shield/).
- KubeJS: [shield.js](../kubejs/server_scripts/equipment/shield.js), [end_gear.js](../kubejs/server_scripts/equipment/end_gear.js), [equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js) (dragon_gear), [transition_items.js](../development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/changes/transition_items.js), [namechanges.js](../kubejs/client_scripts/namechanges.js), `item_dict.js`.
- Background: [damage_types_findings.md](../damage_types_findings.md); [tools/shield_check.py](../tools/shield_check.py), [tools/test_shields.sh](../tools/test_shields.sh), [tools/shield_test/](../tools/shield_test/).

## 6. Offline checks

- `tools/test_shields.sh` runs both checks below (needs `./gradlew build` first). 2026-10-05: no problems,
  32/32 Java checks.
- `python3 tools/shield_check.py` reads the live toml, the `ShieldTier.java` defaults and the tag files, and
  prints:
  1. the effective ladder, including both tick gates and `unblockableEffectTicks`;
  2. where the toml differs from the code defaults;
  3. problems: unknown repair items, bad effect strings, dangling `#include`s, a tier with no recipe;
  4. message ids that sit in more than one category tag, and which one wins.

  It exits 1 on a problem. 2026-10-05: no problems, 7 differences (physical column only), 1 shared id
  (`fireball`, physical).
- `ShieldConfigTest` (`tools/shield_test/`): the `[shields]` spec builds; tick-gate defaults follow the ladder
  (burning netherite+, thorns diamond+) and live edits apply; `disableCooldownTicks` = 20; all seven effects in
  the `unblockableEffectTicks` default;
  `EffectTickContext` answers only for the ticking entity and restores nested ticks.
- Bytecode: `javap -c -p` on the Forge client jar for vanilla targets (`Mob`, `Player`, `MobEffectInstance`,
  `ShieldDecorationRecipe`); `javap` on the built jar to confirm the new mixins remap to SRG names (checked
  2026-10-05). Not testable offline: whether the mixins apply (needs a boot).

## 7. In-game test suite

Survival, Normal difficulty. Use `/give @s creativeengineering:<tier>_shield` or `minecraft:shield`, and turn
on F3+H. "Raise" means hold right click.

- [ ] **SH-01 Items and looks:** give all eight shields → each has its own name, rarity colour and texture
  (iron looks vanilla). A bannered iron shield smithed to diamond (SH-26) shows the banner on the diamond
  texture.
- [ ] **SH-02 Raised model:** raise each tier shield in first and third person → raised-arm pose like vanilla.
- [ ] **SH-03 Compact tooltip:** hover the copper shield → `Physical: 20%`, `Electric: 50%`, "Hold [SHIFT]".
  Diamond → Physical 50, Fire 50, Magic 25.
- [ ] **SH-04 Full tooltip:** hold Shift on gold → Physical, Cold/Chemical/Fire/Electric each 30%, Magic 30,
  Fireproof No, Blocks burning No, Blocks thorns No, Knockback threshold 2. Iron gets the same style of lines;
  diamond shows Blocks thorns Yes; netherite shows both Yes.
- [ ] **SH-05 Partial physical block:** copper raised, let a zombie hit you from the front → shield sound,
  about 2.4 damage taken (vanilla shield used to take 0).
- [x] **SH-06 Percentage reduction:** PASSED 2026-09-24 (reduction confirmed working in game).
- [ ] **SH-07 Facing, physical:** copper raised, zombie behind you → full damage, no shield sound.
- [ ] **SH-08 Arrow:** iron raised, skeleton in front → 40% blocked, no crash (the 2026-09-24 copper-arrow crash
  was a stale jar).
- [ ] **SH-09 Piercing arrow:** shoot yourself with a Piercing crossbow bolt via a second player or a pillager
  holding one → never blocked.
- [ ] **SH-10 Fire from any side:** stand in fire with netherite raised, facing any way → no burning damage,
  shield sound on each tick.
- [ ] **SH-11 Lava wear:** netherite raised in lava → no damage; durability drops by about 5 per lava tick
  (4 damage → 1+4).
- [ ] **SH-12 Round-down:** walk into a cactus with diamond raised (physical 50%) → no damage while raised,
  shield sound per tick; lower it → you take damage.
- [ ] **SH-13 Breakdown mode:** copper, `/summon lightning_bolt` next to you → half the damage. Iron, same →
  full damage (electric 0 in breakdown mode).
- [ ] **SH-14 Poison never blocked:** `/effect give @s minecraft:poison 10`, dragon raised → poison hurts,
  no shield sound. (Changed 2026-10-05: gold used to block it.)
- [ ] **SH-15 Unblockable:** dragon raised, `/effect give @s minecraft:wither 5` → wither still hurts
  (armour-piercing physical/other).
- [ ] **SH-16 Knockback suppressed:** netherite raised, zombie hits → no push. Copper raised → pushed.
- [ ] **SH-17 Dragon effects:** dragon raised, let a zombie hit you → you get Regeneration, the zombie gets
  Poison (5 s each).
- [ ] **SH-18 Disable cooldown (player axe):** second player hits your raised shield with an axe → shield
  lowered for about 1 s (`disableCooldownTicks = 20`).
- [ ] **SH-19 Disable cooldown (mob axe):** a vindicator hits your raised diamond shield until it disables it
  (about 1 in 4 hits) → shield drops, about 1 s cooldown, shield-break sound. Repeat with the iron shield.
- [ ] **SH-20 Live durability:** change `[shields.copper].durability` to 50 in the config screen → the copper
  shield's max durability shows 50 without restart. Put it back.
- [ ] **SH-21 Iron durability and repair:** `minecraft:shield` shows max 336; anvil: iron ingot repairs it,
  planks don't.
- [ ] **SH-22 Tier repair:** anvil repairs diamond with a diamond, dragon with a dragon scale; wrong material
  is refused.
- [ ] **SH-23 Breaking:** worn shield breaks on a block → break sound, slot emptied, shield lowered.
- [ ] **SH-24 Master switch:** `[shields] enabled = false` → copper blocks 100% of a zombie hit and none of
  fire (vanilla rules). Set back to true.
- [ ] **SH-25 Recipes:** JEI shows the copper/iron/gold shaped recipes, the smithing chain to netherite, and the
  echo/felsic/dragon assemblies; vanilla's plank shield recipe is gone.
- [ ] **SH-26 Smithing keeps NBT:** put a banner on an iron shield (crafting grid), enchant and name it, smith
  it to diamond → name, enchantments and banner kept.
- [ ] **SH-27 Assembly NBT, F5:** carry the SH-26 shield on to netherite, then run it through echo assembly →
  name and enchantments kept; expected: banner lost.
- [ ] **SH-28 Dragon autorepair:** damage a dragon shield and carry it → durability recovers over time.
- [ ] **SH-29 Damage tag reload:** remove `lightningBolt` from `electric.json` in a datapack override and
  `/reload` → copper no longer blocks lightning. Remove the override.
- [ ] **SH-30 Seen-sources log:** `[damage_tags] logSeenSources = true`, get hurt by fire and a zombie →
  `logs/latest.log` has one `[damage-tags] seen msgId=...` line each with their tags. Turn it off.
- [ ] **SH-31 Fireproof:** throw a netherite shield into lava → it survives; a diamond shield burns.
- [ ] **SH-32 MineColonies guard:** give a guard a diamond shield → it equips and raises it.
- [ ] **SH-33 Banner on every tier:** crafting grid, copper shield + any banner → bannered copper shield; same
  for gold, diamond, netherite, echo, felsic, dragon. The banner shows on the tier texture, and an already
  bannered shield can't take a second banner.
- [ ] **SH-34 Burn ticks below netherite:** walk through a fire block and step out still burning, diamond
  raised → you burn, no shield sound. Stand in a campfire with gold raised → same.
- [ ] **SH-35 Burn ticks from netherite:** on fire with netherite raised → no burning damage, shield sound per
  tick. Same in a campfire, on a magma block and on a lit Farmer's Delight stove. Diamond on magma or a stove
  → full damage, no shield sound.
- [ ] **SH-36 Fireball still blocks:** diamond raised, a dispenser shoots a fire charge at your front → blocked
  as a projectile (physical 50%), shield sound.
- [ ] **SH-37 Wither never blocked:** `/effect give @s minecraft:wither 5`, dragon raised → wither hurts, no
  shield sound.
- [ ] **SH-38 Instant damage still blocks:** gold raised, `/effect give @s minecraft:instant_damage` → 6 damage,
  30% blocked (4.2 taken), shield sound. Only poison and wither ticks are excluded.
- [ ] **SH-39 Thorns below diamond:** iron raised, walk into a cactus and through a sweet berry bush → full
  damage, no shield sound.
- [ ] **SH-40 Barbed pane:** diamond raised, stand in a Dustrial Decor barbed pane → no damage, shield sound per
  tick; gold → damage.
- [ ] **SH-41 Modded effect ticks:** `/effect give @s everythingcopper:copper_poisoning 10` and
  `/effect give @s alexsmobs:exsanguination 10`, dragon raised → both hurt, no shield sound.
- [ ] **SH-42 Netherite smithing (F8):** smithing table, diamond shield + Netherite Upgrade Smithing Template +
  netherite ingot → netherite shield, template used up. Without the template → no result (Armor Trims).
- [ ] **SH-43 JEI shows the template (F8):** look up the netherite shield's recipes (R) → an Item Upgrading
  page with diamond shield, Netherite Upgrade Smithing Template and netherite ingot; no two-slot Smithing page.
  Same for a vanilla netherite sword. Diamond shield from an iron shield (U on the iron shield) still shows on
  the vanilla Smithing page. A trim template (e.g. U on a coast template) shows Armor Trimming pages.
  `logs/latest.log` has both `[jei] Armor Trims pages: ...` and `[jei] hid N ...` lines, and no Armor Trims
  "no recipe category registered" error from our plugin.
- [ ] **SH-44 Resistance on block (echo and up)** (effects: scratch server PASSED 2026-10-22, SQ-GM-01 with `tools/gear_misc_probe.js`, echo amplifier 0, felsic
  and dragon 1, dragon Regeneration and attacker Poison, netherite none, 40 ticks; the tooltip: client): raise an echo shield and let a zombie hit you: Resistance I
  for 2 s. Felsic: Resistance II for 2 s. Dragon: Regeneration I (5 s) and Resistance II (2 s); the zombie gets
  Poison. Netherite and below: no effect. The tooltip "On block, you gain:" lists the same. Defaults in
  `ShieldTier` (patcherMod 1.1.01, 2026-10-22) and in the dev `creativeengineering-common.toml`.

## 8. History

- 2026-09-20: research (shield block path and every mod touching shields); tier shields and the damage tag
  layer built.
- 2026-09-24: FIRE group renamed ELEMENTAL; tags made comprehensive (30 files) and shields read only tags;
  elemental breakdown; iron folded into the vanilla shield with durability and repair mixins; first in-game
  test crashed on a stale jar (rebuilt); percentage reduction confirmed in game; round-down rule; knockback
  threshold (first attempt wrong, replaced the same day).
- 2026-09-26: default ladder rebaked from the tuned toml.
- By 2026-09-27 (initial git commit): own textures per tier, recipes (shield.js, end_gear.js), disable
  cooldown mixin, Shift full-stat tooltip with colour ladder. Shipped in the deployed jar; unchanged since the
  1.0.4 deploy.
- 2026-10-05: this writeup; `tools/shield_check.py`; findings F1 to F7.
- 2026-10-05: mob axes disable every shield with the 20-tick cooldown (`MobShieldDisableMixin`); banners on
  every tier (`ShieldDecorationRecipeMixin`); burn ticks only from netherite up (`burn_tick` tag,
  `blocksBurnTicks`); poison and wither ticks never blocked (`unblockableEffectTicks`,
  `MobEffectTickContextMixin`); stale comments fixed (F1-F4, F7 closed); 1-damage tick list (2.8);
  `tools/test_shields.sh` + `ShieldConfigTest`. Built, not in `mods/`. New tests SH-33 to SH-38; SH-12, SH-14,
  SH-19 rewritten.
- 2026-10-05: all seven tick-damage effects unblockable; magma and stoves moved into `burn_tick` (netherite+);
  new `contact_tick` gate (cactus, berry bushes, wither roots, beanstalk thorns, barbed pane: diamond+,
  `blocksContactTicks`), wither roots and beanstalk thorns tagged `piercing`; `ShieldTickGate` generalises both;
  etching acid left on chemical %. Smithing investigated: F8 (Armor Trims needs the netherite template). Tests
  SH-39 to SH-42; SH-04, SH-12, SH-35 updated. Built, not in `mods/`.
- 2026-10-05: JEI smithing matches Armor Trims' template rule (`SmithingTemplateJEI`, F8 decided: keep the
  template). Jar copied to `mods/` (md5 9a44333c) with the game closed; first boot pending. SH-43.
- 2026-10-05: in game the 20:13 jar hid the 24 netherite smithing recipes from JEI entirely. Cause: Armor Trims'
  JEI pages were always empty (its plugin's recipe-type mismatch, logged). `SmithingTemplateJEI` now fills both
  Armor Trims pages itself and hides only what it re-showed. Jar built 20:56, not yet copied (game running).
- 2026-10-11: ships in pack 1.0.5 (staged, not deployed): the 2026-10-05 changes are all in the staged patcherMod
  1.0.5 jar (md5 68509043; it replaced both 2026-10-05 builds). Single-player boot with it: no mixin errors.
- 2026-10-14: pack 1.0.5 was never deployed; everything above that was staged for it ships in pack **1.0.6** (STAGED
  2026-10-14, update from 1.0.4, [plan_1.0.6.md](plan_1.0.6.md)), unchanged.
- 2026-10-18: felsic shield recipe values corrected to "hardcoded" (no `CRAFTING.FELSIC_SHIELD` section exists); the
  ladder and its assemblies are written up in [gear_tuning.md](gear_tuning.md).
- 2026-10-19: felsic shield values configurable (`CRAFTING.FELSIC_SHIELD`); `DamageFlag` / `DamageTags` / `DamageTagSnapshot` pre-loaded in `commonSetup` against the 2026-09-24 `NoClassDefFoundError` (patcherMod 1.0.9, [plan_1.0.9.md](plan_1.0.9.md)).
- 2026-10-22: Resistance on block for echo (I), felsic and dragon (II), 2 s (`gear/shields #5`): `ShieldTier` defaults and
  the instance toml's `shielderEffects`. patcherMod 1.1.01 in `mods/`, not run. SH-44; scratch probe SQ-GM-01.
- 2026-10-22 (second bulk run): SQ-GM-01 PASS on the scratch server (pillager attackers); the SH-44 tooltip stays in game.
- 2026-11-04: code, mixins, `[shields]` and the shield recipes moved to Creative Equipment (`creativeequipment`, [creative_equipment.md](creative_equipment.md)); paths: `development/creativeEquipment/.../shield/`, `config/creativeequipment-common.toml`, `kubejs/data/creativeequipment/kubejs/equipment/shield.js`. The damage tags stay in patcherMod (Common). Same behaviour.
