# gear / shields: implementation brief

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# gear`, Sub `shields` (rows 2 to 5 open, row 1 Complete).
Feature writeup: [shields.md](../shields.md) (test prefix `SH-`, last id SH-43).

## Scope and order

| Step | Row | Work | Notes |
|---|---|---|---|
| 1 | `shields #4` damage-type sanity check | tool (`tools/shield_check.py`), then data | Do it first: it prints the per-source table the other rows tune against, and it may move ids between tags |
| 2 | `shields #3` 1-damage ticks | patcherMod + data | Most of the row is already done (see below); only powder snow is left, plus the acid decision |
| 3 | `shields #2` knockback thresholds + per-tier disable cooldown | patcherMod + config | The only real Java work: new per-tier keys, both disable mixins, tooltip, `ShieldConfigTest` |
| 4 | `shields #5` Resistance while blocking | **config only** (recommended) | `shielderEffects` already does this. Can ship any time, even before 1-3 |

Steps 2 and 3 go into one patcherMod build together with `gear/effects #3` and the F7 mixin change
([gear_effects.md](gear_effects.md), group B). Run `tools/test_shields.sh` (needs `./gradlew build`) after each.

Related rows: `gear/tuning #4` (assemblies drop the shield's banner, shields.md F5, SH-27). It touches
`SequencedAssemblyRecipeMixin`, not shield code, and is briefed under tuning.

Conventions that matter here:
- **The toml wins** (shields.md 0). Every default change in `ShieldTier.java` must also be written into the shipped
  [config/creativeengineering-common.toml](../../config/creativeengineering-common.toml) `[shields.<tier>]`, or it never plays.
  New keys appear in the toml only after the new jar has run once (Forge writes the defaults). Run the jar, then edit
  the values, then commit the toml.
- Shields read damage **tags** only (no raw `DamageSource` flag checks), except for piercing arrows and effect ticks.

---

## shields #4: Shield damage-type sanity check
Status **Planned** · Work: none / patcherMod (really: tool + possibly data) · id `gear/shields #4`

### Goal
For every damage type in the pack, check what each tier blocks (and what it doesn't) against the intended ladder,
and fix the misfits.

### Now
- Classification lives in data: 32 tag files in
  [damage_tags/](../../development/patcherMod/src/main/resources/data/creativeengineering/damage_tags/). The category
  order is fixed in [ShieldCategory.java:37-43](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/shield/ShieldCategory.java):
  explosion, projectile, magic, elemental, melee, then other (physical).
- The verdict is `ShieldBlocking.verdict` ([ShieldBlocking.java:42-77](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/shield/ShieldBlocking.java)):
  invulnerability-bypass, piercing arrow, unblockable effect tick, tick gate, 0% reduction, armour-piercing physical,
  then facing.
- [tools/shield_check.py](../../tools/shield_check.py) (187 lines) already loads the live toml, the `ShieldTier.java`
  defaults and every tag with `#include`s resolved (`load_tags`, `members`). Section 4 lists only ids in more than one
  category tag. There is no per-id table.
- The source catalogue with live flags is [damage_types_findings.md](../../damage_types_findings.md) sections 3-4
  (~127 message ids). Every flag tag also has an explicit json listing the known ids (since 2026-09-24), so a static
  classification is possible for everything except `mob`, `player` and the bare `arrow`, which only the live flag tells
  apart.
- Live tier values (toml, 2026-10-21, matches shields.md 2.1): physical 20/30/40/50/60/70/80/100; magic 0/30/0/25/50/60/75/100;
  elemental copper electric 50, iron fire 25, diamond fire 50, netherite fire 100, gold 30 / echo 50 / felsic 75 / dragon
  100 blanket.

### Build
1. **`shield_check.py --matrix [--md]`** (new section 5, reuses `load_tags` / `members` / `effective_reductions`):
   - Rows: every message id in any resolved tag, plus the ids in `damage_types_findings.md` 3-4 that are in none
     (parse the backticked ids out of its tables; flag them `untagged`).
   - Columns: category (first match in `CATEGORIES`, else `other`), group, elemental kind (first of fire / electric /
     cold / chemical), gate (`burn_tick` / `contact_tick` membership, not for `projectile` ids), `armor_piercing`
     membership. Then per tier one cell: `%` blocked, or the reason it isn't attempted: `AP` (physical + armour-piercing),
     `gate`, `0`, `eff` (an effect tick in `unblockableEffectTicks`; mark the msgIds of the seven effects by hand: `magic`
     for poison and the AM ones, `wither`, `everythingcopper.copper_poisoning*`, `wasWatered`).
   - `--md` prints a markdown table to paste into shields.md as a new section "2.10 What each tier blocks (generated)".
     Keep the command line in the section header so it can be regenerated.
2. **Anomaly pass** printed after the matrix (all warnings, no exit code):
   - physical-group ids that are `AP` for **every** tier, i.e. no shield can ever block them (expected: Create saw / crush /
     drill, `dragonBreath`, `wither`, `inWall`, `starve`; anything else is a candidate to retag);
   - ids that a cheap tier (copper, gold, iron) blocks **fully** (100% or 1-damage round-down) but diamond doesn't. The
     ladder should be monotonic per id; gold's blanket 30% vs diamond's 0 cold / chemical / electric is the known case;
   - `untagged` catalogue ids (they fall to `other` / physical by default).
3. **Review with the user**, then fix by data only: add ids to finer tags, or move `#include`s in `elemental.json` /
   `magic.json` (shields.md 2.9: no Java either way). Record each decision in shields.md 4 as findings F9+.
4. **In game** for the dynamic ids: `[damage_tags] logSeenSources = true`, play a session (zombie, skeleton, player
   melee, a wolf, Quark pickarang, Create Guardian turret), then grep `logs/latest.log` for `[damage-tags] seen` and
   compare the `tags=` lists to the matrix. `tags=NONE` lines are untagged sources to add.

### Ids and data
No new ids. Output only, unless the review moves tag entries.

### Decisions
- **Where the matrix lives:** **recommend generated into shields.md 2.10** (regenerable, so not a hand table that rots).
- **Monotonic ladder:** should a higher tier always block at least what a lower one does, per element? Gold's blanket 30%
  beats diamond and netherite on cold, electric and chemical today (by design: gold is the "magic metal"). **Ask the user;**
  the anomaly pass lists every inversion.

### Gotchas
- Static view only: modded sources that set `isMagic()` / `isFire()` at runtime but aren't listed in the matching json
  don't show. That's what the `logSeenSources` pass is for.
- The fireball is in `projectile` and `fire`; projectile wins (shields.md 2.2). Keep that special case in the output.
- `shield_check.py` needs the `toml` module (already imported) and the live toml with a `[shields]` section.

### Tests
- `tools/test_shields.sh` still passes (the new section is print-only).
- **SH-44 Matrix spot check:** pick three rows from 2.10 (a lava tick on netherite, a lightning bolt on copper, a Create saw
  on dragon) and confirm each in game.

---

## shields #3: 1-damage ticks fully blocked
Status **Planned** · Work: patcherMod / data · id `gear/shields #3`

### Goal
Under the round-down rule (under 1 damage left = full block), any tier with a non-zero percentage blocks a 1-damage tick
completely, with a shield sound each tick. Decide which of these ticks to exclude.

### Now (the row is mostly stale)
The row lists magma, stoves, powder snow, acid, copper poisoning and modded magic ticks. Since the second 2026-10-05
build (DEPLOYED with 1.0.6, now live in 1.0.9):
- **magma (`hotFloor`), stoves (`farmersdelight.stove`, `endersdelight.endstone_stove`):** in
  [burn_tick.json](../../development/patcherMod/src/main/resources/data/creativeengineering/damage_tags/burn_tick.json), so they are
  only attempted by tiers with `blocksBurnTicks` (netherite and above).
- **copper poisoning and the Alex's Mobs magic ticks** (Debilitating Sting, Exsanguination, Ender Flu), plus poison,
  wither and Phasing: never blocked (`[shields] unblockableEffectTicks`,
  [ShieldConfig.java:49-51](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/shield/ShieldConfig.java)).
- **cactus, berry bushes, wither roots, beanstalk thorns, barbed panes:** `contact_tick`, diamond and above.

What is actually left (shields.md 2.8):

| Source | msgId | Tag | Blocked fully by today |
|---|---|---|---|
| powder snow, every 40 ticks | `freeze` | `cold` | gold (blanket 30), echo, felsic, dragon |
| PneumaticCraft etching acid | `acid` | `chemical` | gold, echo, felsic, dragon; **kept by request** (shields.md 4, known limits) |

`ieAcid` is in `chemical` too, but no 1-damage IE acid tick was found in the 2026-10-05 scan.

### Build (if powder snow should be excluded below echo)
Add a third tick gate. It's the existing pattern: [ShieldTickGate.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/shield/ShieldTickGate.java)
generalises burn and contact.
1. `ShieldTickGate`: `FREEZE("freeze_tick", "blocksFreezeTicks", "Blocks freezing")`.
2. New data file `damage_tags/freeze_tick.json`: `{"description": "Freezing ticks: powder snow ...", "values": ["freeze"]}`.
   Not `icicle` / `fallingIcicle` (one-off hits).
3. `ShieldTier.defaultBlocksTicks` (ShieldTier.java:191-196): `case FREEZE -> ordinal() >= ECHO.ordinal();`. Both this
   switch and the comment switch in `CEConfig.Shields.TierCfg` (CEConfig.java:1537-1546) are exhaustive switch
   expressions, so the build fails until both have the new case. Comment text: "Whether this tier may block powder-snow
   freezing ticks at all: the creativeengineering:freeze_tick damage tag. Default: echo and above."
4. The tooltip (ShieldTooltip.java:72-75) and the toml loop (CEConfig.java:1550) iterate `ShieldTickGate.values()`, so
   they need nothing.
5. `tools/shield_test/.../ShieldConfigTest.java:36-37`: add the FREEZE default check. `shield_check.py`: print the third
   gate column (line ~135, beside burn / thorn).
6. After the first boot, set `blocksFreezeTicks` in the shipped toml (defaults are fine).
7. Docs: shields.md 2.1 (gate column), 2.3 step 4, 2.8 table row, F6 (closed), file map.

### Ids and data
Damage tag `creativeengineering:freeze_tick` (new). Config `[shields.<tier>] blocksFreezeTicks` (new, 8 keys).

### Decisions
- **Exclude powder snow below echo?** It drops gold, the one cheap tier that blocks it. **Recommend yes, via the gate**:
  gold blocking powder snow for free, with a shield sound every 2 s, reads as a bug. Netherite and diamond have cold 0
  anyway, so "echo and up" is the honest default.
- **Acid:** kept on chemical % by an earlier request. **Recommend keep**, close it in this row.
- **Alternative with no Java:** put `freeze` in `contact_tick.json`. Gold (gate off) would stop blocking it and echo+
  would keep it, but the tooltip would call it "Blocks thorns". Not recommended.

### Gotchas
- `ShieldTickGate.of` returns the **first** gate whose tag the hit carries (BURN, CONTACT, then FREEZE). Don't put one msgId
  in two gate tags.
- A gated tick ignores facing and armour-piercing (ShieldBlocking.java:69-71). Powder snow has no position anyway.

### Tests
- **SH-45 Powder snow below echo:** stand in powder snow with gold raised → you freeze and take damage, no shield sound.
- **SH-46 Powder snow from echo:** echo raised in powder snow → no freeze damage, shield sound every 2 s. Shift tooltip:
  "Blocks freezing: Yes" on echo, "No" on gold.

---

## shields #2: Knockback thresholds and per-tier disable cooldown
Status **Planned** · Work: patcherMod · id `gear/shields #2`

### Goal
The `TODO` at [ShieldTier.java:46](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/shield/ShieldTier.java):
"adjust kbthreshold to be more reasonable, add variable shield cooldown to each shield".

### Now
- **Knockback:** `ShieldBlockHandler.onShieldBlock` ([ShieldBlockHandler.java:80-82](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/shield/ShieldBlockHandler.java))
  flags the holder when `remaining < knockbackThreshold(tier)`. `onKnockback` (103-109) then cancels the holder's next
  `LivingKnockBackEvent`. Binary: a full push or none. Thresholds are 1, 2, 3, 4, 5, 6, 7, 8 (copper → dragon), the same in
  code and toml.
- **Why it reads as unreasonable:** the threshold rises up the ladder while the damage that gets through falls, so the two
  stack. Remaining damage per reference hit (Normal difficulty, physical %; push = remaining ≥ threshold):

  | Tier (phys %, KB) | zombie 3 | vindicator 13 | ravager 12 | warden 30 |
  |---|---|---|---|---|
  | copper (20, 1) | 2.4 push | 10.4 push | 9.6 push | 24 push |
  | iron (40, 3) | 1.8 no | 7.8 push | 7.2 push | 18 push |
  | diamond (50, 4) | 1.5 no | 6.5 push | 6 push | 15 push |
  | netherite (60, 5) | 1.2 no | 5.2 push | 4.8 **no** | 12 push |
  | echo (70, 6) | 0.9→0 no | 3.9 no | 3.6 no | 9 push |
  | felsic (80, 7) | 0.6→0 no | 2.6 no | 2.4 no | 6 **no** |
  | dragon (100, 8) | 0 no | 0 no | 0 no | 0 no |

  (The mob damage numbers are vanilla values from memory. Check one in game before tuning on them.) From felsic up nothing
  pushes, warden included. Dragon's 8 does nothing because 100% physical leaves 0.
- **Disable cooldown:** one global `[shields] disableCooldownTicks` = 20 (CEConfig.java:1471-1477), read by
  `ShieldConfig.disableCooldownTicks()` (ShieldConfig.java:40-46) from two mixins:
  - [ShieldDisableCooldownMixin](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ShieldDisableCooldownMixin.java):
    `@ModifyConstant` on the `100` in `Player#disableShield` (`m_36384_`). The shield isn't a parameter, but
    `player.getUseItem()` is still the raised shield at that point (vanilla calls `addCooldown(getUseItem().getItem(), 100)`
    before `stopUsingItem()`).
  - [MobShieldDisableMixin](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/MobShieldDisableMixin.java):
    line 46, `shield` is a parameter.

### Build
1. **Per-tier cooldown:**
   - `CEConfig.Shields.TierCfg` (CEConfig.java:1508+): `disableCooldownTicks = b.comment("Ticks this shield stays
     unusable after an axe disables it. -1 = use [shields].disableCooldownTicks.").defineInRange("disableCooldownTicks",
     t.defaultDisableCooldownTicks(), -1, 12000);`
   - `ShieldTier`: a `defaultDisableCooldownTicks()` returning -1 for every tier. Adding a 10th constructor argument to
     every overload is noisy, so derive it in a switch, the way `defaultBlocksTicks` does.
   - `ShieldConfig.disableCooldownTicks(ItemStack shield)`: `ShieldTier t = ShieldTier.of(shield)`; when `t != null` and
     the tier value is ≥ 0, return it; else the global value. Keep the no-arg version for the global value.
   - `ShieldDisableCooldownMixin`: `Player self = (Player) (Object) this; return ShieldConfig.disableCooldownTicks(self.getUseItem());`
     (the `try`/`catch` fallback stays).
   - `MobShieldDisableMixin:46`: `ShieldConfig.disableCooldownTicks(shield)`.
   - Tooltip, full view (ShieldTooltip.java after line 77): `§7Disable cooldown: §f1.0 s`.
2. **Knockback** (see Decisions for the values):
   - New per-tier key `knockbackScale` (IntValue 0-100, default 100): the share of the push left after a block that lands
     at or above the threshold. Or the alternative mode: scale by `remaining / incoming`.
   - `ShieldBlockHandler`: replace `suppressKnockbackHolder` with a holder plus a `float knockbackFactor` (0 = cancel).
     `onKnockback`: if the factor is 0, `setCanceled(true)`; else `event.setStrength(event.getStrength() * factor)`. It
     keeps the same self-clearing rule. `LivingKnockBackEvent#setStrength` exists in Forge 43 (from memory; it is the
     event's documented setter).
   - Update the code defaults in the `ShieldTier` enum rows (lines 51-59) and the toml.
3. Remove the `TODO` on line 46 and fix the "Placeholder ladder" javadoc at ShieldTier.java:175-179.
4. `ShieldConfigTest`: per-tier cooldown falls back to the global value at -1, and a tier value wins. `shield_check.py`:
   show `kb` and `cd` per tier.
5. Docs: shields.md 2.1 (columns), 2.4 Knockback, 3 Disable cooldown, 4 (close the open TODO bullet at line 321).

### Ids and data
Config `[shields.<tier>] disableCooldownTicks` (new, -1 default) and `[shields.<tier>] knockbackScale` (new). No item or tag ids.

### Decisions
- **Threshold values:** **recommend a flatter ladder**, so that only "trivial after reduction" hits stop the push:
  copper 1, gold 1, iron 1.5, diamond 2, netherite 2.5, echo 3, felsic 3.5, dragon 4. With the table above: netherite stops
  zombies but not ravagers, felsic stops ravagers and vindicators but not the warden. Dragon never pushes while its
  physical stays 100%.
- **Scaled push above the threshold:** **recommend `knockbackScale` = the remaining share** (push x remaining / incoming).
  It follows the reduction with no new number per tier: a 60% block gives a 40% push. If the user prefers a knob, use the
  per-tier percentage. Ask; default to remaining share.
- **Per-tier cooldown values:** the TODO only asks for the knob. **Recommend** copper 40, gold 30, iron 20, diamond 20,
  netherite 15, echo 10, felsic 10, dragon 5 (ticks). The better shield is harder to keep down. Keep the global 20 for
  non-tier shields (IE, Alex's Mobs).

### Gotchas
- `ItemCooldowns` is keyed by **Item**, so two copper shields share a cooldown, as in vanilla.
- The IE shield's shock upgrade and the Alex's Mobs void-worm shot also call `Player#disableShield`. With tier lookup
  they get the raised shield's tier value, which is right.
- The knockback cancel is keyed to the holder instance and self-clears. Keep that: a scaled factor must not leak into a
  later unrelated knockback (always reset the holder at the start of `onShieldBlock`, as line 51 does today).
- Toml first: run once, then set values (the Conventions above).

### Tests
- **SH-47 Per-tier cooldown:** `[shields.dragon] disableCooldownTicks = 5`, a vindicator disables your dragon shield → back
  up in 0.25 s; copper (40) → 2 s; `-1` → the global 20.
- **SH-48 Scaled push:** diamond raised, a zombie hit above the threshold → visibly shorter push than with no shield; SH-16
  re-run with the new thresholds.

---

## shields #5: Resistance while blocking with echo and up
Status **Planned** · Work: patcherMod (per the row; config is enough) · id `gear/shields #5`

### Goal
A short Resistance for the holder while blocking with the echo, felsic and dragon shields.

### Now
`[shields.<tier>] shielderEffects` already gives the holder effects on every blocked hit, live, with a category filter.
Format: `effectId;ticks[;amplifier[;category|...]]` ([ShieldEffects.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/shield/ShieldEffects.java),
CEConfig.java:1553-1562). Today only dragon uses it (`minecraft:regeneration;100;0`). Echo and felsic are `""`.

### Build (config only, recommended)
1. Shipped toml:
   - `[shields.echo] shielderEffects = "minecraft:resistance;40;0"`
   - `[shields.felsic] shielderEffects = "minecraft:resistance;60;0"`
   - `[shields.dragon] shielderEffects = "minecraft:regeneration;100;0, minecraft:resistance;60;1"`
2. Mirror the defaults in the `ShieldTier` enum. ECHO and FELSIC (lines 56-57) use the 9-argument blanket constructor;
   switch them to the 11-argument one (`..., 6, "minecraft:resistance;40;0", "")`) so a fresh toml matches.
   `shield_check.py` validates the strings (`check_effects`).
3. The tooltip shows "On block, you gain:" by itself (ShieldTooltip.java:79). shields.md 2.1 Extras column and 2.4.

### Ids and data
`minecraft:resistance` (`ef.resistance`); no new ids.

### Decisions
- **"While blocking" = on each blocked hit, or the whole time the shield is up?** On-hit is what the existing knob does,
  and it matches "short Resistance". **Recommend on-hit.** If the user wants it while raised, add
  `[shields.<tier>] raisedEffects` (same format) applied from a `LivingTickEvent` handler every 10 ticks when
  `entity.isBlocking()` and `ShieldTier.of(getUseItem())` is set: about 30 lines in a new `shield/ShieldRaisedEffects.java`.
- **Level and length:** the numbers above (echo I 2 s, felsic I 3 s, dragon II 3 s). **Recommend** them as a start.

### Gotchas
- **Resistance applies to the same hit.** `ShieldBlockEvent` fires inside `LivingEntity#hurt` before `actuallyHurt`,
  which applies Resistance to what the shield let through (vanilla order, from memory). So echo's 70% physical block plus
  Resistance I lets through 30% x 0.8 = 24%: effectively a stronger shield. Say so in the tooltip / writeup, or apply it
  next tick if the user doesn't want it.
- Dragon physical is 100%, so its Resistance only helps elemental / magic remainders and hits from behind in the 3 s window.
- Effects are added visible (with particles), unlike the gear framework's hidden ones.

### Tests
- **SH-49 Echo Resistance:** echo raised, a zombie hits → `ActiveEffects` shows Resistance (Amplifier 0, ~40 ticks);
  felsic → 60; dragon → Resistance 1 and Regeneration.

---

## Built, needs play

| Row | Change | Status | Notes |
|---|---|---|---|
| shields #1 | Seven tier shields | Complete | Code matches shields.md (live toml = 2.1 table, 2026-10-21). SH-01 to SH-43 open except SH-06 |

## Writeup corrections made

- [shields.md](../shields.md) 4 F6: the stale list (magma, stoves, copper poisoning, modded magic ticks are now gated or
  unblockable) cut down to what is left: powder snow and etching acid.
- [shields.md](../shields.md) 0, damage-tag row: `burn_tick` / `contact_tick` "BUILT 2026-10-05" → shipped with 1.0.6.
