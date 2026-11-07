# Armor and tool effects: worn, held and on-hit potion effects, the echo set bonus, Magic Resistance

## 0. At a glance

| Part | Status |
|---|---|
| Armor effects framework `global.armorEffects`: `piece()` (one item worn), `set()` (full set worn), `mount()` (horse armor) | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3, live in 1.0.6); no test ids before this writeup |
| Set bonuses on wooden (Immersive Armors), copper, iron, gold, diamond, netherite, felsic, dragon (both chestplates) | DEPLOYED, in-game test pending |
| Dragon horse armor (`mount()`): Regeneration II, Health Boost II, Resistance II, horse walk once per `mountEffectIntervalTicks` | Regeneration I DEPLOYED with 1.0.4; the three level II effects BUILT 2026-10-31 (`potions/effects #4`, startup: restart), scratch server PASSED r1031 (2026-10-31, SQ-EFX-04) |
| Tool effects framework `global.toolEffects`: `onHit()` (to the target) and `held()` (to the wielder) | DEPLOYED, in-game test pending |
| Echo tools on-hit Weakness II + Darkness II; dragon sword / axe / shovel / trident / bow / crossbow on-hit and held effects | DEPLOYED, in-game test pending; dragon bow and crossbow swapped 2026-10-22 (F4); on-hit never applied until F10 (fixed 2026-10-22); built, scratch server PASSED 2026-10-22 for the on-hit half (SQ-GM-02); held effects in game (AE-19, AE-20) |
| Echo armor set bonus swapped from Resistance II + Strength II to Night Vision + Phantom Bane (`EchoArmorEffectProcedureMixin`), tooltip to match (`EchoArmorTooltipMixin`) | DEPLOYED (patcherMod), in-game test pending |
| `creativeengineering:magic_resistance` effect (KubeJS) and its damage cut (`MagicResistanceEffect`, 20% per level on the `creativecommon:magic` tag; Poison / Wither ticks at or below its level cancelled) | Effect DEPLOYED; tag switch and tick cover BUILT 2026-10-31 (`potions/effects #3`, patcherMod 1.1.04), scratch server PASSED r1031 (2026-10-31, SQ-EFX-02, -03) |
| Magic Resistance potions (base, long, strong) and their brewing | DEPLOYED; brewing and reagent belong to the potions writeup (backlog row 4); reagent is a placeholder |
| Tooltip lines for all of the above (`armor_effects_tooltip.js`, `tool_effects_tooltip.js`) | DEPLOYED |

- **Pending:** F4 (dragon bow and crossbow swap, `tool_effects.js`, startup script: restart) built 2026-10-22, unshipped;
  on-hit half scratch server PASSED 2026-10-22 (SQ-GM-02, after F10). AE-01 to AE-24 have never been run. Findings F1 to F10 (section 4) are
  open; F6 needs your call (F4 done 2026-10-22).
- **Files to open:** [func/armor_effects.js](../kubejs/startup_scripts/func/armor_effects.js) (armor data + API),
  [func/tool_effects.js](../kubejs/startup_scripts/func/tool_effects.js) (tool data + API),
  [magic/armor_effects.js](../kubejs/server_scripts/magic/armor_effects.js) and
  [magic/tool_effects.js](../kubejs/server_scripts/magic/tool_effects.js) (apply them),
  [MagicResistanceEffect.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/potion/MagicResistanceEffect.java).
- **Knobs:** the effect lists are code (edit the startup file, restart; `/reload` doesn't re-run startup scripts).
  `config/creativeengineering-common.toml`: `[magic_resistance] reductionPerLevel` (0.2) and
  `[perf] mountEffectIntervalTicks` (100). Both live, no restart.
- **How to see a hidden effect:** framework effects are added with no icon and no particles, so the HUD shows
  nothing. Use `/data get entity @s ActiveEffects` (or on a horse, `/data get entity @e[type=horse,limit=1,sort=nearest] ActiveEffects`).

## 1. Overview

Vanilla 1.19.2 armor and tools carry no potion effects. Create: Deep Dark is the one mod in the pack that does:

- **Echo armor:** a hidden full-set bonus, Resistance II + Strength II. An MCreator procedure
  (`EchoArmorEffectProcedure`) reapplies it every tick at 60 ticks. Each piece's tooltip hard-codes
  "With full set on: Resistance II (infinite) / Strength II (infinite)".
- **Echo sword:** Weakness II + Darkness II (5 s) on hit, with an "On hit, inflicts to target:" tooltip.

The pack generalises that idea into two data-driven KubeJS frameworks, one for armor and one for tools, styled on
CDD's tooltip. The echo armor bonus itself is retargeted in place by two mixins. Magic Resistance is a new effect,
the first one the pack defines.

## 2. Reference

### 2.1 Armor set bonuses (`set()`, every piece of the set must be worn)

All level I (no amplifier given). Tooltip on every piece: "With full set on:", one line per effect, "(infinite)".

| Set | Items | Effects | Kind |
|---|---|---|---|
| Wooden | `immersive_armors:wooden_*` | IE Flammable | drawback |
| Copper | `everythingcopper:copper_*` | IE Conductive | drawback |
| Iron | `minecraft:iron_*` | IE Conductive | drawback (registered twice, F1) |
| Gold | `minecraft:golden_*` | Luck, IE Conductive | |
| Diamond | `minecraft:diamond_*` | Speed | |
| Netherite | `minecraft:netherite_*` | Fire Resistance, Lava Vision (AM), Knockback Resistance (AM) | |
| Echo | `create_deep_dark:echo_armor_*` | Night Vision, Phantom Bane (Better Beacons) | CDD's own procedure, retargeted (2.4) |
| Felsic | `felsic_gear:felsic_*` | Long Reach (Better Beacons), Jump Boost, Magic Resistance | |
| Dragon | `dragonloot:dragon_*`, chest slot either `dragon_chestplate` or `upgraded_dragon_chestplate` | Resistance, Magic Resistance, Fire Resistance, Poison Resistance (AM), Knockback Resistance (AM), Feather Feet (Botania), Long Reach | 7 effects, the cap |

The dragon set is registered twice, once per chestplate, with the same list. The tooltip dedupes, so the shared
pieces show the block once.

No `piece()` entries are registered today.

### 2.2 Mount armor (`mount()`)

| Item | Effect | How |
|---|---|---|
| `dragonloot:dragon_horse_armor` | Regeneration II, Health Boost II, Resistance II | every `mountEffectIntervalTicks` (100) the server walks every entity in the level; each `AbstractHorse` wearing it gets the effect for interval + 40 ticks |

Tooltip "When worn:" on the item.

### 2.3 Tool effects

`onHit` effects go to the target, once per hit, for their own duration. `held` effects go to the wielder every tick
the item is in either hand. Tooltip headers: "On hit, inflicts to target:" (with seconds) and "While held:"
("(infinite)"). Level = amplifier + 1.

| Tool | On hit (to target) | While held (to wielder) |
|---|---|---|
| Echo pickaxe / axe / shovel / hoe (`creativeengineering:echo_*`) | Weakness II 5 s, Darkness II 5 s | |
| Echo sword (`create_deep_dark:echo_sword`) | CDD's own, not the framework | |
| Dragon sword | Weakness III 10 s, Exsanguination II 7.5 s | Soul Cross II (Botania: heal on kill) |
| Dragon axe | Levitation II 3 s, Exsanguination II 7.5 s | Jump Boost III |
| Dragon shovel ("discombobulate") | Nausea II 5 s, Hunger II 5 s, Overencumbered II 5 s, Trippy II 5.5 s, Earthquake II 0.5 s, Fear II 0.5 s | Knockback Resistance II (AM) |
| Dragon trident | Mining Fatigue II 3 s | Slow Falling II |
| Dragon bow | Slowness IV 2.5 s, Glowing I 25 s | Invisibility I |
| Dragon crossbow | Exsanguination II 7.5 s, Weakness III 12.5 s | Resistance II |

`held` specs also carry a `duration`; it is ignored (F3). Hand-written flavour lines for the dragon tools are in
[tooltips.js](../kubejs/client_scripts/tooltips.js) around line 815 (none for the bow or crossbow).

### 2.4 Echo armor set bonus

| | CDD original | Pack |
|---|---|---|
| Effect 1 | Resistance II | Night Vision, at least 300 ticks (CDD's 60 would strobe: Night Vision flashes under about 200) |
| Effect 2 | Strength II | `better_beacons:phantom_bane`, CDD's duration, level I (F7, fixed 2026-10-28; Night Vision too); stays Strength if Better Beacons is missing |
| Tooltip | "Resistance II / Strength II (infinite)" | "Night Vision / Phantom Bane (infinite)" |

The check that all four pieces are worn is CDD's; the mixin only swaps the two `addEffect` arguments.

### 2.5 Magic Resistance

- **Effect:** `creativeengineering:magic_resistance`, beneficial, colour `#8a2be2`, registered in KubeJS.
- **Cut:** a Forge `LivingHurtEvent` listener multiplies damage by `1 - min(1, reductionPerLevel x level)` when the
  source is in the pack's `creativecommon:magic` damage tag (the shields' MAGIC column, [shields.md](shields.md)
  2.9) and the entity has the effect. 20% at I, 40% at II, 100% from V.
- **What counts:** the tag: vanilla's magic flag (poison and Instant Damage, evoker fangs, thorns, the warden's sonic
  boom, dragon's breath clouds, modded sources that set the flag) plus `farseer`, `#psychic` (Botania relics) and
  `#life_drain` (Health Siphon, Supplementaries XP extraction). A datapack edit of `magic.json` and `/reload` changes it.
- **Effect ticks no shield can block** (2026-10-31, owner): a tick from an effect in `[magic_resistance]
  negatedEffectTicks` (default Poison, Wither) deals no damage when its level is at most the Magic Resistance level; it is
  cancelled in `LivingAttackEvent`, before the hurt sound and flash, recognised through `EffectTickContext` (poison ticks
  share Instant Damage's source). A higher level takes the per-level cut (Wither too, though it is not in the tag).
- **Where it comes from:** felsic and dragon set bonuses (level I), the potions (base 3:00, long 8:00, strong
  1:30 at II, from `CommonPotions`), `/effect`. Felsic gear assembly fills with it (see
  [gear_tuning.md](gear_tuning.md)).
- **Effect description:** written 2026-10-31 in [lang/en_us.json](../kubejs/assets/creativeengineering/lang/en_us.json)
  line 13.

### 2.6 Effect spec format

```js
{ effect: 'namespace:id', amplifier: 0, name: 'Display Name', color: 'a', duration: 100, visible: false }
```

- `effect`: a mob effect id (not a potion id). Looked up lazily on first use, because modded effects aren't
  registered yet when startup scripts run. A bad id logs `[armorEffects] effect "..." is not registered` once and
  is skipped.
- `amplifier`: 0 = level I, shown without a numeral.
- `name`: tooltip name. Default: Title Case of the id's path.
- `color`: one legacy format letter. Default: `c` (red) for harmful effects, `a` (green) otherwise.
- `duration`: ticks, required for `onHit` (a spec without it is dropped with a warning), ignored elsewhere.
- `visible: true`: show the HUD icon and particles. Default hidden.

Caps: 7 effects per armor entry, 16 per tool entry; extras are dropped with a warning. Tool calls append, so
several `onHit()` calls on one item add up; armor `piece()` / `mount()` calls replace and `set()` calls add a new set.

## 3. What the pack changes

- **One data file per framework, two consumers each** (the startup-func convention):
  - `startup_scripts/func/armor_effects.js` and `func/tool_effects.js` hold the API and every registration, at
    `// priority: -100` so `global.item` is loaded first.
  - `client_scripts/armor_effects_tooltip.js` and `tool_effects_tooltip.js` draw the lines in `ItemEvents.tooltip`
    as raw `§` strings, because the `{text, color}` convention in `tooltips.js` can't do CDD's two-tone line.
  - `server_scripts/magic/armor_effects.js` and `magic/tool_effects.js` apply them.
- **Armor apply:** `PlayerEvents.tick`, server only. Reads the four armor slots; a `piece()` matches when its item is
  in any slot, a `set()` when every id is worn. Each match is re-added every tick for 40 ticks, not ambient, hidden.
- **Mount apply:** `LevelEvents.tick` every `mountEffectIntervalTicks`; the horse armor is read through
  `entity.getSlot(401)` (`AbstractHorse` armor slot), the public route `/item replace` uses, so no mixin.
- **Tool apply:**
  - `onHit`: `EntityEvents.hurt`. The attacker is `source.getEntity()` (the shooter for arrows), keyed on the
    attacker's **main-hand** item at the moment of damage. One `MobEffectInstance` per spec, normal visibility.
    Chosen over `Item#hurtEnemy` because swords, diggers and hoes handle that method differently.
  - `held`: `PlayerEvents.tick`, both hands, re-added every tick for 40 ticks, hidden.
- **Echo armor:** the framework can only add effects, not cancel CDD's procedure, so echo is deliberately not
  registered. `EchoArmorEffectProcedureMixin` (`@ModifyArg` on `addEffect` in `EchoArmorEffectProcedure.execute`)
  swaps the effects; `EchoArmorTooltipMixin` (`@ModifyConstant` on the two strings in each piece class's
  `appendHoverText`) swaps the lines. Both `remap = false`.
- **Echo tools:** used to be a dedicated `echo_tool_effects.js` plus manual tooltip lines; now four `onHit()` calls.
- **Magic Resistance:** KubeJS registers the effect (no timing problem: it depends on no other mod's effect).
  KubeJS's `EntityEvents.hurt` in this build is read-only, so the damage cut is a Java listener. The potions need a
  Java `DeferredRegister<Potion>` because KubeJS's potion registry callback runs before any mob effect exists.
- **Shared JS scope:** every server script shares one top-level scope in this Rhino build, so the tool file names its
  class `$ToolMobEffectInstance` (a second `const $MobEffectInstance` threw and knocked a file out).

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1.** FIXED 2026-10-19 ([plan_1.0.9.md](plan_1.0.9.md)). **Iron set registered twice** ([func/armor_effects.js](../kubejs/startup_scripts/func/armor_effects.js) lines
  212-220, two identical `set()` calls). The tooltip dedupes and the effect is the same, so nothing visible; Conductive
  is just added twice a tick. Fix: delete one call.
- **F2.** FIXED 2026-10-19 (comment says 7, MAX_EFFECTS). **Stale cap in the header:** the comment says "up to 6 entries"; `MAX_EFFECTS` is 7 and the dragon set uses all
  7. Doc fix.
- **F3. `held()` durations are ignored.** Every held spec has a `duration` (dragon bow Invisibility 60, crossbow
  Resistance 60, ...) but held effects always refresh at 40 ticks. Harmless, misleading. Fix: drop the fields or document them.
- **F4. Dragon bow and crossbow look reversed.** FIXED 2026-10-22 (TODO `potions/effects #2`): on-hit and held swapped in
  `tool_effects.js`, durations and levels kept (Invisibility and Glowing set to level I, their level does nothing).
  The finding before the fix:
  - Bow: the target gets **Invisibility** on hit; the wielder gets **Slowness IV** and **Glowing III** while holding it.
  - Crossbow: the target gets **Resistance II** on hit; the wielder gets **Exsanguination** (bleeding) and **Weakness
    III** while holding it.
  - Possible reading: Slowness on the bow is a deliberate zoom (Slowness narrows the FOV) and the rest are drawbacks;
    or on-hit and held were swapped. Neither item has a flavour line in `tooltips.js`, unlike the other dragon tools.
    The tooltip shows target Resistance in green under "On hit, inflicts to target", which reads oddly either way.
- **F5. On-hit is keyed to the main hand at damage time.**
  - A thrown dragon trident has left the hand, so a thrown hit applies nothing; melee with it works.
  - Arrows apply the bow's or crossbow's on-hit only while it is still in the main hand when the arrow lands (bow in
    the off hand: never).
  - Any damage credited to the wielder counts, e.g. thorns from the wielder's armor.
- **F6. Magic Resistance reads vanilla `isMagic()`, shields read the pack's `magic` damage tag.** FIXED 2026-10-31: Magic Resistance reads the `magic` tag, the widest one, which the shields already read (decided 2026-10-29, TODO `potions/effects #3`). The tag
  ([shields.md](shields.md) 2.9) is vanilla magic plus `psychic` and `life_drain`, so a source can count as magic for
  a shield and not for Magic Resistance. Switching `MagicResistanceEffect` to `DamageTags` would line them up (the
  user asked shields to read only tags). (The stale magic_resistance.js header about "no potion yet" was fixed
  2026-10-19.)
- **F7. Echo Phantom Bane is level II, the tooltip says "Phantom Bane".** The mixin keeps CDD's amplifier 1. Check
  whether Better Beacons scales Phantom Bane with level; if it does, either drop the amplifier to 0 or say "II".
  Checked 2026-10-21 (Better Beacons 1.0.2 jar): its only consumer, `PhantomSpawnerMixin`, tests `hasEffect`, so the
  level does nothing; amplifier 0 is cosmetic ([briefs/potions_effects.md](briefs/potions_effects.md)).
  FIXED 2026-10-28 (owner: Phantom Bane is levelless, like Night Vision): the mixin gives both echo effects amplifier 0,
  and patcherMod `CommonPotions` registers no strong Phantom Bane potion (the glowstone mix is gone from `potions.js`).
  Scratch SQ-CM-07, SQ-CM-08.
- **F8. One effect packet per effect per tick, unmeasured.** Re-adding an effect with a longer duration fires
  `onEffectUpdated`, which syncs the effect to the client. A dragon set plus a held dragon tool is 8 updates a
  player a tick. [perf.md](perf.md) only measured the horse walk. A cheaper version re-adds only when under, say, 20
  ticks remain. Not proposed until a profile shows it.
- **F9. Horse Regeneration level.** BUILT 2026-10-31: Regeneration II, Health Boost II and Resistance II (decided 2026-10-29, TODO `potions/effects #4`). A refresh that extends Health Boost re-runs its modifiers and clamps health to the un-boosted max, so `applyEffects` puts the health back after each pass (bytecode-checked `LivingEntity#onEffectUpdated`, `HealthBoostMobEffect`). Before: level I works, scratch SQ-HT-02 2026-10-28). The old registration named `minecraft:strong_regeneration` (a potion
  id, so it never worked); it was then `minecraft:regeneration` level I ([perf.md](perf.md) 7).
- **F10. No tool on-hit effect ever applied.** FIXED 2026-10-22 (scratch run, SQ-GM-02): `tool_effects.js` called
  `event.getSource().getEntity()`, which throws in this KubeJS (the name is `getActual()`), so every on-hit entry (echo
  tools, dragon tools) did nothing. The sample syringe had the same bug ([engineering.md](engineering.md) 2.5).

### Known limits

- **Night Vision through the framework would strobe:** the 40-tick refresh is under the ~200-tick flashing threshold.
  Any future Night Vision entry needs its own path (the echo mixin uses 300).
- **Players only for armor and held effects:** a mob wearing a full dragon set gets nothing. Horses get `mount()`.
- **Mount effects start late:** newly equipped horse armor works within one interval (5 s by default).
- **Effects never strip:** taking a piece off just lets the 40-tick effect run out.
- **Real potions of the same effect:** vanilla merges the two. A longer or stronger potion keeps its own duration and
  level, but every re-add copies the framework's hidden flags, so the potion's HUD icon disappears while the gear is
  worn.
- **Lazy lookup caches misses:** an effect id that didn't resolve at first use stays skipped until restart.

### Rejected options

- **Echo armor through the framework:** it can add but not cancel CDD's Resistance + Strength, so both pairs would
  stack, and the tooltip would show two blocks. The mixins swap in place.
- **Damage cut in KubeJS:** `EntityEvents.hurt` can't change the amount in this KubeJS build.
- **`Item#hurtEnemy` for on-hit:** per-class overrides (hoes don't override it); the hurt event covers all.
- **Auto-derived effect names:** no client-safe way to get a translated effect name at registration; hence `name`.

## 5. File map

- [startup_scripts/func/armor_effects.js](../kubejs/startup_scripts/func/armor_effects.js): `global.armorEffects` API, set and mount registrations.
- [startup_scripts/func/tool_effects.js](../kubejs/startup_scripts/func/tool_effects.js): `global.toolEffects` API, echo and dragon registrations.
- [server_scripts/magic/armor_effects.js](../kubejs/server_scripts/magic/armor_effects.js): player tick (pieces, sets), level tick (horses).
- [server_scripts/magic/tool_effects.js](../kubejs/server_scripts/magic/tool_effects.js): hurt event (on hit), player tick (held).
- [client_scripts/armor_effects_tooltip.js](../kubejs/client_scripts/armor_effects_tooltip.js), [client_scripts/tool_effects_tooltip.js](../kubejs/client_scripts/tool_effects_tooltip.js): tooltip lines.
- [startup_scripts/magic/magic_resistance.js](../kubejs/startup_scripts/magic/magic_resistance.js): the effect.
- [potion/MagicResistanceEffect.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/potion/MagicResistanceEffect.java): damage cut; [potion/MagicResistancePotions.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/potion/MagicResistancePotions.java): the three potions.
- [mixin/EchoArmorEffectProcedureMixin.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/EchoArmorEffectProcedureMixin.java), [mixin/EchoArmorTooltipMixin.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/EchoArmorTooltipMixin.java): echo set bonus.
- `CEConfig.Magic` (`[magic_resistance]`) and `CEConfig.Perf.mountEffectIntervalTicks` in [CEConfig.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CEConfig.java); listener registered in `CreativeEngineering.java` line 109.
- Related: [potions.js](../kubejs/startup_scripts/cooking/potions.js) lines 68-74 (Magic Resistance brewing), [tooltips.js](../kubejs/client_scripts/tooltips.js) (dragon flavour lines), [tooltips.md](tooltips.md), [gear_tuning.md](gear_tuning.md) (the gear these effects sit on).

## 6. Offline checks

No script yet. Done by hand on 2026-10-18:

- Every `i.<key>` used in the two startup files resolves to an id in `item_dict.js` (no `undefined` registrations).
- Every effect id is a mob effect id, not a potion id (the horse armor bug of 2026-10-03 was exactly that).
- The `remap = false` targets (`EchoArmorEffectProcedure.execute`, `m_7292_`, `m_7373_`) can only be checked against
  the CDD 1.7.0 jar or by a boot; they are in the deployed jar and nothing has logged an injection failure.

A small `tools/effects_check.py` could parse both files and flag duplicate sets, missing durations, caps and ids not
in the dict. Not built.

## 7. In-game test suite

Survival, Normal difficulty. Check hidden effects with `/data get entity @s ActiveEffects`.

- [ ] **AE-01 Set tooltip:** hover any netherite piece → "With full set on:", Fire Resistance, Lava Vision,
  Knockback Resistance, each "(infinite)". Same block on all four pieces.
- [ ] **AE-02 Set needs all four:** wear three netherite pieces → none of the three effects. Add the fourth → all three
  present, no HUD icon, no particles.
- [ ] **AE-03 Set ends:** take one piece off → the effects run out within 2 s.
- [ ] **AE-04 Drawback sets:** full iron set → `immersiveengineering:conductive`; full wooden Immersive Armors set →
  `flammable`. Iron tooltip shows the block once (F1).
- [ ] **AE-05 Gold and diamond:** full gold → Luck + Conductive; full diamond → Speed (walk speed visibly up).
- [ ] **AE-06 Felsic:** full felsic → Long Reach (blocks and mobs reachable from further than usual), Jump Boost, Magic Resistance.
- [ ] **AE-07 Dragon, both chestplates:** full dragon set with `dragon_chestplate` → all 7 effects. Swap in
  `upgraded_dragon_chestplate` → still all 7. Its tooltip shows the block once.
- [ ] **AE-08 Echo bonus:** full echo set → Night Vision (no flicker) and Phantom Bane; no Resistance, no Strength.
- [ ] **AE-09 Echo tooltip:** hover an echo piece → "Night Vision (infinite)" and "Phantom Bane (infinite)", nothing
  about Resistance or Strength, and no second "With full set on" block.
- [ ] **AE-10 Phantom Bane level (F7):** with the echo set, `/data get entity @s ActiveEffects` → note Phantom Bane's
  Amplifier (expected 0 since 2026-10-28, F7).
- [ ] **AE-11 Horse armor:** put dragon horse armor on a tamed horse, wait 5 s; its
  ActiveEffects shows Regeneration, Health Boost and Resistance (Amplifier 1 each); its health does not drop every 5 s. (No repair half: the armor has no durability, 2026-10-28.) Take the armor off → gone within 7 s.
- [ ] **AE-12 Horse interval:** set `[perf] mountEffectIntervalTicks = 20` → newly equipped armor works within 1 s.
  Put it back to 100.
- [ ] **AE-13 Echo tool on hit:** hit a zombie with the echo pickaxe → it gets Weakness II and Darkness II (5 s).
  Tooltip: "On hit, inflicts to target:", both lines with "(5 sec.)". Same for axe, shovel, hoe.
- [ ] **AE-14 Off hand doesn't hit:** echo axe in the off hand, punch a zombie → no effects.
- [ ] **AE-15 Dragon sword:** hit a mob → Weakness III 10 s, Exsanguination II 7.5 s. While held: Soul Cross; killing a
  mob heals you.
- [ ] **AE-16 Dragon axe:** hit → target levitates 3 s and bleeds; while held, Jump Boost III (jumps visibly
  higher).
- [ ] **AE-17 Dragon shovel:** hit a player or mob → all six discombobulate effects; while held, Knockback Resistance.
- [ ] **AE-18 Dragon trident:** melee hit → Mining Fatigue II 3 s; thrown hit → nothing (F5). While held: Slow Falling.
- [ ] **AE-19 Dragon bow (F4)** (on-hit tables and a main-hand hit: scratch server PASSED 2026-10-22, SQ-GM-02, slowness 3 + glowing 0, no Invisibility
  on the target; held effects need a real ticking player: in game): holding it → Invisibility, no Slowness or Glowing on you. Shoot a zombie with the bow
  in the main hand → it gets Slowness IV 2.5 s and Glowing 25 s; tooltip lists Invisibility under held.
- [ ] **AE-20 Dragon crossbow (F4)** (same split as AE-19; on-hit: scratch server PASSED 2026-10-22, SQ-GM-02, exsanguination 1 + weakness 2): holding it → Resistance II, no bleeding or Weakness on you. Shoot a zombie → it
  gets Exsanguination II 7.5 s and Weakness III 12.5 s.
- [ ] **AE-21 Held in either hand:** dragon axe in the off hand → Jump Boost III still applies.
- [ ] **AE-22 Magic Resistance cut:** `/effect give @s creativeengineering:magic_resistance 60 0`, then
  `/effect give @s minecraft:instant_damage 1 0` → 4.8 damage instead of 6 (20%). Amplifier 1 → 3.6.
- [ ] **AE-23 Only magic:** with Magic Resistance, let a zombie hit you → full damage; stand in fire → full damage.
- [ ] **AE-29 Poison and Wither cover:** with Magic Resistance I, `/effect give @s minecraft:poison 10 0` and
  `minecraft:wither 10 0` → no damage, no hurt flash; Poison II → damage at 80%. A raised shield still never blocks them.
- [ ] **AE-30 Tag-driven cut:** with Magic Resistance I, another player's Botania relic damage (2) → 1.6.
- [ ] **AE-24 Live knob:** `[magic_resistance] reductionPerLevel = 0.5` in the config screen → Instant Damage I now
  does 3. Put it back to 0.2.

## 8. History

- By 2026-09-27 (initial git commit): armor effects framework (piece, set, mount), set bonuses, tool effects framework
  replacing `echo_tool_effects.js`, dragon tool effects, Magic Resistance effect and damage cut, potions; shipped with
  1.0.3. The echo set-bonus mixins were in the jar and first committed 2026-10-02.
- 2026-10-03: horse armor effect back on with an interval walk (`mountEffectIntervalTicks`), and its effect id fixed
  (`strong_regeneration` → `regeneration`). [perf.md](perf.md) 7. Shipped with 1.0.4.
- 2026-10-18: this writeup; findings F1 to F9; tests AE-01 to AE-24. No code changed.
- 2026-10-19: F1 fixed, the second iron `set()` deleted ([plan_1.0.9.md](plan_1.0.9.md)).
- 2026-10-19: F2 and the magic_resistance.js part of F6 fixed (comments only; TODO `pack/docs`).
- 2026-10-22: F4 fixed, dragon bow and crossbow on-hit and held swapped; AE-19 and AE-20 rewritten.
- 2026-10-22 (second bulk run): F10 found and fixed (no on-hit tool effect had ever applied); SQ-GM-02 then PASS on
  the scratch server. Held effects of AE-19 / AE-20 stay in game.
- 2026-10-27: horse pass moved to one `ServerEvents.tick`, skipping dimensions without a player ([perf.md](perf.md)
  10.4). Scratch SQ-HT-01 / -02 queued; AE-11 / AE-12 unchanged.
- 2026-10-28: SQ-HT-02 / -03 PASS on the Regeneration half (bot player). The dragon horse armor repair pass in
  `dragon_autorepair.js` was removed: `HorseArmorItem` has no durability, so it never had anything to repair
  ([gear_tuning.md](gear_tuning.md)).
- 2026-10-31: `potions/effects #3` (F6: Magic Resistance on the `magic` tag, Poison / Wither ticks at or below its level
  cancelled, CEConfig `[magic_resistance] negatedEffectTicks`; description written) and `#4` (F9: horse Regeneration II,
  Health Boost II, Resistance II, health kept across refreshes). AE-11 updated, AE-29 / AE-30 added; scratch SQ-EFX-02
  to -04.
