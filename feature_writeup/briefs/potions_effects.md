# potions / effects: implementation brief

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# potions`, Sub `effects` (rows 2 to 7 open, row 1 Complete).
Feature writeup: [armor_effects.md](../armor_effects.md) (test prefix `AE-`, last id AE-24).

## Scope and order

| Group | Rows | Work | Why together |
|---|---|---|---|
| A | `effects #5` (F3, F5 parts), `effects #2`, `effects #6` (pickaxe), `effects #4` | KubeJS: `startup_scripts/func/tool_effects.js`, `func/armor_effects.js`, `server_scripts/magic/tool_effects.js`, `client_scripts/tool_effects_tooltip.js`, `client_scripts/tooltips.js` | All the same four files. #2 is only worth doing after the F5 projectile fix: the swapped bow / crossbow on-hit effects are what F5 breaks |
| B | `effects #3`, `effects #5` (F7 part) | patcherMod: `potion/MagicResistanceEffect.java`, `mixin/EchoArmorEffectProcedureMixin.java` | Two one-line Java changes; ride the next patcherMod build with the shield rows ([gear_shields.md](gear_shields.md)) |
| C | `effects #7` | none (in-game check), docs | Answered from the IE jar below; needs one in-game confirmation |
| D | `effects #5` (F8 part) | measurement only | Profile first; no change proposed until a profile shows a cost |

Build order: A (one session), then B with the next patcherMod build, C whenever someone is in game, D only with a
profile in hand.

**Already done, found during this scan:** the dragon hoe half of `effects #6` exists. `dragonloot:dragon_hoe` is the only
member of `#creativeengineering:bonemeal_charms` ([equipmenttags.js:31](../../kubejs/server_scripts/tags/equipmenttags.js)),
so `BonemealCharmMixin` refunds bone meal used with the hoe in the other hand (`[bonemeal_charm]` `chance` 0.5,
`damageChance` 0.1, `damagePerUse` 1, all live config), tooltip at [tooltips.js:820](../../kubejs/client_scripts/tooltips.js).
That is exactly "a Vinery bone-meal buff with a configurable durability cost" (it was modelled on Vinery's Winemaker set
mixin). Shipped since the initial commit; documented in [fixes_compat.md](../fixes_compat.md) 2.9, test FX-12. Only the
pickaxe is left in `effects #6`.

Related rows elsewhere: `gear/tuning #13` (the dragon fishing rod's bonus), `potions/potions #9` (shock / electric effects,
touches `effects #7`), `ui/effects #1` (the `TODO` effect descriptions, incl. Magic Resistance).

Conventions for every change here: effect ids go through `global.effect`
([effect_dict.js](../../kubejs/startup_scripts/_lib/effect_dict.js), priority 1000, so it is loaded before the
`priority: -100` func files). It has vanilla effects only today. When a line is touched, use `ef.<key>` for the vanilla
ones and add the modded ones in a new `// ##### Modded #####` section, keyed by plain path where it doesn't collide
(`ef.exsanguination = 'alexsmobs:exsanguination'`, `ef.soul_cross = 'botania:soul_cross'`,
`ef.knockback_resistance = 'alexsmobs:knockback_resistance'`, `ef.magic_resistance = 'creativeengineering:magic_resistance'`,
`ef.phantom_bane = 'better_beacons:phantom_bane'` ...). Don't convert the untouched registrations in the same pass unless
it's cheap. The whole file is 30 lines.

---

## effects #5: Small effect findings
Status **Planned** · Work: KubeJS (F3, F5), patcherMod (F7), none (F8) · id `potions/effects #5`

### Goal
Close armor_effects.md F3, F5, F7 and F8.

### Now
- **F3:** `held()` specs carry a `duration` that is never read. [server_scripts/magic/tool_effects.js:72](../../kubejs/server_scripts/magic/tool_effects.js)
  always uses `HELD_REFRESH_TICKS` (40). Affected: [func/tool_effects.js](../../kubejs/startup_scripts/func/tool_effects.js)
  lines 138, 142, 145, 148, 151, 152, 155, 156.
  Note: the bow and crossbow "held" specs have on-hit-looking durations (Glowing 500, Weakness 250, Exsanguination 150).
  That supports `effects #2`'s reading that on-hit and held were swapped.
- **F5:** `EntityEvents.hurt` (tool_effects.js:26-47) keys on `source.getEntity().getMainHandItem()` at damage time:
  - a thrown dragon trident applies nothing (the hand is empty or holds something else);
  - an arrow applies the bow's effects only if the bow is still in the **main** hand when it lands (bow in the off hand: never);
  - any damage credited to the wielder counts, e.g. thorns from the wielder's armor, or a TNT the player lit.
- **F7:** `EchoArmorEffectProcedureMixin` keeps CDD's amplifier 1, so Phantom Bane is level II.
  **Verified 2026-10-21 against `CerbonsBetterBeacons-Forge-1.19.2-1.0.2.jar`:** the only consumer,
  `PhantomSpawnerMixin.better_beacons_preventPhantomsSpawn`, checks `player.hasEffect(PHANTOM_BANE)` and nothing else.
  The level does nothing; only the inventory display ("Phantom Bane II") is off.
- **F8:** every framework effect is re-added every tick (`addEffect` with 40 ticks), which syncs it to the client each
  time. Unmeasured.

### Build
1. **F3 (KubeJS, startup):** delete the `duration:` fields from every `held()` call that survives `effects #2`. In
   `held()` (tool_effects.js:96-99), warn once when a spec has `duration`: `'[toolEffects] held ' + itemId + ': duration
   is ignored (held effects refresh every tick)'`. Update the header comment (lines 31-32) to say so.
2. **F5 (KubeJS, server), projectile attribution.** Make the on-hit item "the item that fired or threw the projectile,
   else the melee weapon":
   - New handler in `server_scripts/magic/tool_effects.js`:
     ```js
     const $ToolAbstractArrow = Java.loadClass('net.minecraft.world.entity.projectile.AbstractArrow')
     EntityEvents.spawned(event => {
       var ent = event.getEntity()
       if (!(ent instanceof $ToolAbstractArrow) || event.getLevel().isClientSide()) return
       var pd = ent.getPersistentData()
       if (pd.contains('ce_launcher')) return            // re-joined from disk, keep the original
       var owner = ent.getOwner()
       if (!owner || !owner.getMainHandItem) return
       var TE = global.toolEffects
       // bow: still the use item during releaseUsing; crossbow: in either hand when it fires
       var cands = [owner.getUseItem(), owner.getMainHandItem(), owner.getOffhandItem()]
       for (var k = 0; k < cands.length; k++) {
         var id = String(cands[k].getItem().getId())
         if (TE.ON_HIT[id]) { pd.putString('ce_launcher', id); return }
       }
     })
     ```
     `EntityEvents.spawned` (`EntityJoinLevelEvent`), `getPersistentData()` and `entity.getNbt()` exist in
     KubeJS 1902.6.2-build.73 (jar checked: `EntityEvents.SPAWNED`, `EntitySpawnedEventJS`, `EntityKJS.kjs$getNbt`).
     `ThrownTrident` extends `AbstractArrow`, so this tags tridents too (the trident is still in the hand when it spawns:
     `TridentItem#releaseUsing` adds the entity before it removes the item, vanilla code from memory).
   - In `EntityEvents.hurt`, pick the item id in this order:
     1. `source.getDirectEntity()` has `persistentData.ce_launcher` → that id;
     2. the direct entity is a `ThrownTrident` without the tag (thrown before the update) → `direct.getNbt().getCompound('Trident').getString('id')`;
     3. the direct entity **is** the attacker (a melee hit) → the attacker's main-hand item (today's rule);
     4. otherwise → nothing. That drops thorns, explosions and other credited damage.
   - Keep the `$Tool...` prefix on new top-level `const`s: all server scripts share one scope (armor_effects.md 3).
3. **F7 (patcherMod):** in `EchoArmorEffectProcedureMixin.java` line 64 pass `0` instead of `original.getAmplifier()`
   for Phantom Bane. Optionally line 56 too: Night Vision has no levels. The tooltip already says "Phantom Bane", so no
   tooltip change. Rewrite F7 in armor_effects.md as fixed and update AE-10 (expect Amplifier 0).
4. **F8:** no code. If a spark / JFR profile ([perf.md](../perf.md) 3) ever shows `ClientboundUpdateMobEffectPacket`
   from these ticks, change both appliers to re-add only when `player.getEffect(me)` is missing or has under 20 ticks
   left. (`addEffect` with a longer duration is what fires `onEffectUpdated` and the packet.) Until then, close F8 as
   "watch".

### Ids and data
- `minecraft:trident`, `dragonloot:dragon_trident` (`i.dragon_trident`), `dragonloot:dragon_bow`, `dragonloot:dragon_crossbow`,
  all in item_dict (6311-6325).
- Persistent-data key `ce_launcher` (string, item id). New; namespaced by prefix like the pack's other KubeJS keys.
- `better_beacons:phantom_bane`: verified binary (above).

### Decisions
- **F7, amplifier 0 or "II":** verified that the level does nothing. **Recommend amplifier 0** (one constant in the mixin).
  The TODO row's decision is then answered.
- **F5, should off-hand bows count?** With launcher tagging, a bow fired from the off hand is the use item and counts.
  **Recommend yes**: the item that fired the arrow is what the tooltip promises.
- **F5, should a dispenser-fired arrow count?** It has no owner, so no. Keep.

### Gotchas
- `AbstractArrow#getOwner()` is null client side and for dispensers; guard it.
- A crossbow loaded with fireworks fires a `FireworkRocketEntity`, not an arrow. No on-hit, same as today.
- Multishot spawns three arrows. All three are tagged and each applies the effects: the same as three bow hits.
- The `hurt` event also fires for blocked or zero-damage hits (e.g. against a raised shield), as it does today.
- Don't strip `duration` from `onHit()` specs: there it is required (tool_effects.js:86-92).

### Tests (new ids in armor_effects.md 7)
- **AE-25 Thrown trident:** throw the dragon trident at a zombie → Mining Fatigue II 3 s (AE-18's "thrown → nothing" changes).
- **AE-26 Bow swapped hands:** shoot the dragon bow, switch to a sword before the arrow lands → the bow's on-hit still applies.
  Off-hand bow → applies.
- **AE-27 Thorns doesn't count:** wear Thorns armor, hold the dragon sword, let a zombie hit you → the zombie gets thorns
  damage but no Weakness / Exsanguination.
- **AE-28 Held duration warning:** `logs/kubejs/startup.log` has no `duration is ignored` warning (all fields removed).
- AE-10 changes to expect Amplifier 0.

---

## effects #2: Dragon bow and crossbow look reversed
Status **Testing** (built 2026-10-22, AE-19, AE-20) · Work: KubeJS · id `potions/effects #2` · Depends on `effects #5` F5 (build in the same session)

### Goal
Scratchpad 2026-10-19: bow **held** Invisibility, bow **inflicts** Slowness and Glowing; crossbow **held** Resistance,
crossbow **inflicts** bleeding (Exsanguination) and Weakness.

### Now
[func/tool_effects.js:150-156](../../kubejs/startup_scripts/func/tool_effects.js):

| Item | onHit today | held today |
|---|---|---|
| `i.dragon_bow` | Invisibility II, 60 t | Slowness IV (`duration: 50`), Glowing III (`duration: 500`) |
| `i.dragon_crossbow` | Resistance II, 60 t | Exsanguination II (`duration: 150`), Weakness III (`duration: 250`) |

No flavour lines for either in [tooltips.js](../../kubejs/client_scripts/tooltips.js) (the dragon block ends at 815-820).

### Build
1. Replace lines 150-156:
   ```js
   global.toolEffects.held(i.dragon_bow,  { effect: ef.invisibility })
   global.toolEffects.onHit(i.dragon_bow, [
     { effect: ef.slowness, amplifier: 3, duration: 50 },
     { effect: ef.glowing,  amplifier: 0, duration: 500 },
   ])
   global.toolEffects.held(i.dragon_crossbow,  { effect: ef.resistance, amplifier: 1 })
   global.toolEffects.onHit(i.dragon_crossbow, [
     { effect: ef.exsanguination, amplifier: 1, duration: 150 },   // add ef.exsanguination to effect_dict (Modded)
     { effect: ef.weakness,       amplifier: 2, duration: 250 },
   ])
   ```
   with `var ef = global.effect` next to `var i = global.item` (line 107). The durations are the ones already written on
   today's held specs. Glowing has no levels, so amplifier 0 (today's III only changes the numeral).
2. Add flavour lines to the dragon block in tooltips.js after line 819, same `col.epic` style:
   `[i.dragon_bow, ['Slow and reveal your target', col.epic], ['Turns you invisible while held', col.epic]]`,
   `[i.dragon_crossbow, ['Weaken and bleed your target', col.epic], ['Hardens you while held', col.epic]]`.
3. Update armor_effects.md 2.3 table, F4 (fixed, with the date), AE-19 and AE-20.

### Ids and data
`minecraft:invisibility`, `minecraft:slowness`, `minecraft:glowing`, `minecraft:resistance`, `minecraft:weakness` (all in
effect_dict); `alexsmobs:exsanguination` (Alex's Mobs 1.21.1, already resolved by the framework today, not in the dict).

### Decisions
- None on direction (the row says "swap"). **Slowness IV for 2.5 s** on a target is close to a root. Keep the numbers as
  written and retune after play.
- **Held Invisibility hides the player only:** armor and the held bow stay visible (vanilla). Hidden (no particles)
  because the framework adds held effects with `visible: false`. Fine.

### Gotchas
- Resistance II held stacks with the dragon set's Resistance I by vanilla merge (the higher amplifier wins). So a full
  dragon set plus the crossbow gives Resistance II, 40% damage cut. Note it in gear_tuning.md if the ladder table lists it.
- A held effect refreshes at 40 ticks, so Invisibility outlasts switching away by up to 2 s.
- Without F5 the bow's new on-hit only lands while the bow is still in the main hand. Build F5 first or in the same commit.

### Tests
Rewrite **AE-19** (bow: shoot a zombie → Slowness IV 2.5 s, Glowing 25 s; holding it → `/data get entity @s ActiveEffects`
shows Invisibility, hidden) and **AE-20** (crossbow: target gets Exsanguination II 7.5 s and Weakness III 12.5 s; holding it
gives Resistance II).

---

## effects #3: Magic Resistance vs the `magic` damage tag
Status **Planned** · Work: patcherMod · id `potions/effects #3`

### Goal
Magic Resistance cuts the same set of sources the shields' MAGIC column blocks.

### Now
- [MagicResistanceEffect.java:56](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/potion/MagicResistanceEffect.java):
  `if (me == null || !event.getSource().isMagic()) return;` (Forge `LivingHurtEvent`, registered at `CreativeEngineering.java:109`).
- The pack tag `creativeengineering:magic` ([damage_tags/magic.json](../../development/patcherMod/src/main/resources/data/creativeengineering/damage_tags/magic.json))
  is the live `isMagic()` flag (`DamageFlag.MAGIC`, a built-in flag tag computed per hit) **plus** the ids `magic`,
  `indirectMagic`, `thorns`, `sonic_boom`, `farseer`, plus `#psychic` (`farseer`, `botania-relic`) and `#life_drain`
  (`health_siphon`, `supplementaries.xp_extracting`). It is a strict superset of `isMagic()`.
- `DamageTags.is(source, tag)` ([damage/DamageTags.java:47](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/damage/DamageTags.java))
  is thread-safe and cached per (msgId, flag mask). It is preloaded in `commonSetup` (`CreativeEngineering.java:278`)
  against the old `NoClassDefFoundError`.

### Build
1. In `MagicResistanceEffect`:
   ```java
   private static final ResourceLocation MAGIC_TAG = DamageTags.id("magic");
   ...
   if (me == null || !DamageTags.is(event.getSource(), MAGIC_TAG)) return;
   ```
   Update the class javadoc (lines 26-29), which describes `isMagic()`.
2. Docs: armor_effects.md 2.5 "What counts" (now the `magic` tag: vanilla magic + Botania relic + health siphon + XP
   extraction), F6 (fixed), and finding 4 in [damage_types_findings.md](../../damage_types_findings.md) 5. The effect
   description in [lang/en_us.json:11](../../kubejs/assets/creativeengineering/lang/en_us.json) is still `TODO`
   (`ui/effects #1`). Write it here: "Reduces magic damage (potions, poison, evoker fangs, thorns, sonic booms, curses and
   life drain) by 20% per level."

### Ids and data
Tag `creativeengineering:magic` (data, reloadable). Config `[magic_resistance] reductionPerLevel` (0.2) unchanged.

### Decisions
- **Switch or keep:** **recommend switch.** Players and the user think in the pack's tags, and the tag can then be tuned
  with `/reload` and no Java. Cost: the magic set gets slightly wider (four message ids).
- **Should `chemical` (acid, copper poisoning) count?** No: it is in the shields' ELEMENTAL column. Keep the tag as is.

### Gotchas
- Poison ticks are `magic` and stay cut, as today. Shields never block them (`unblockableEffectTicks`); Magic Resistance
  still does. Intended.
- Before the first data load only the flag tags exist ([DamageTags.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/damage/DamageTags.java) javadoc),
  so a hit during world load counts flag-only. Harmless.
- `/reload` with a datapack that edits `magic.json` changes Magic Resistance too. Worth a sentence in shields.md 2.9.

### Tests
- **AE-29 Tag-driven cut:** with Magic Resistance I, hold another player's Botania relic (`botania-relic`, 2 damage) →
  1.6 per tick instead of 2. AE-22 (Instant Damage) unchanged.
- **AE-30 Reload:** a datapack override of `magic.json` without `indirectMagic`, `/reload`, a splash Instant Damage potion
  thrown by a witch → full damage. Remove the override.

---

## effects #4: Horse Regeneration level
Status **Planned** · Work: KubeJS · id `potions/effects #4`

### Goal
Settle the dragon horse armor's Regeneration level.

### Now
[func/armor_effects.js:265-267](../../kubejs/startup_scripts/func/armor_effects.js): `mount(i.dragon_horse_armor, [{ effect: 'minecraft:regeneration' }])`,
level I. The original registration named `minecraft:strong_regeneration`, which is a potion id (the strong potion is
Regeneration II). So the first author most likely meant **II**. Applied every `[perf] mountEffectIntervalTicks` (100) for
interval + 40 ticks.

### Build
One line, if II: `{ effect: ef.regeneration, amplifier: 1 }`. The tooltip picks up "Regeneration II" by itself
(`describe()` adds the numeral). Then update armor_effects.md 2.2 and F9, [perf.md](../perf.md) 7 ("undecided"), and AE-11
(expect Amplifier 1).

### Decisions
- **I or II:** **recommend II.** It matches the `strong_regeneration` intent. It is end-game armor, a horse has 15-30 HP,
  and Regeneration I (1 HP per 2.5 s) barely shows on a horse that is being hit. II heals 1 HP per 1.25 s.

### Gotchas
The interval applies the effect fresh every 5 s with a 7 s duration. Regeneration's heal timer depends on the remaining
duration (`isDurationEffectTick`: `duration % (50 >> amp) == 0`), so the re-adds don't skip heals. Vanilla rule from memory.

### Tests
AE-11 updated: Amplifier 1.

---

## effects #6: Dragon bonus on nearly every piece
Status **Planned** · Work: KubeJS · id `potions/effects #6`

### Goal
Every dragon piece has its own bonus. The row lists the hoe (bone-meal buff with a durability cost), the pickaxe (Haste
unless crouching), the bow and crossbow (`effects #2`) and the fishing rod (`gear/tuning #13`).

### Now
| Piece | Bonus today |
|---|---|
| sword, axe, shovel, trident | on-hit + held ([tool_effects.js:136-148](../../kubejs/startup_scripts/func/tool_effects.js)) |
| bow, crossbow | on-hit + held, being swapped (`effects #2`) |
| **hoe** | **done:** bonemeal charm, see "Already done" above |
| **pickaxe** | none (`i.dragon_pickaxe`, `tune_tools.js:126`: mining speed 12, level 5) |
| helmet / chestplate / upgraded chestplate / leggings / boots | the 7-effect set bonus only (armor_effects.js:249-259) |
| shield | on-block Regeneration / Poison (shields.md 2.1) |
| horse armor | Regeneration (`effects #4`) |
| anvil | unbreakable |
| fishing rod | no dragon rod exists (`gear/tuning #13`) |

### Build (pickaxe)
1. **Framework (startup, tool_effects.js):** accept two optional `held` spec fields:
   - `unlessCrouching: true`: not applied while `player.isCrouching()`;
   - `refreshTicks: n`: the duration re-added each tick, default `HELD_REFRESH_TICKS` (40).

   Document both in the header (lines 25-32).
2. **Server (magic/tool_effects.js:66-74):**
   ```js
   effects.forEach(spec => {
     if (spec.unlessCrouching && player.isCrouching()) return
     var me = global.armorEffects._resolveEffect(spec.effect)
     if (!me) return
     player.addEffect(new $ToolMobEffectInstance(me, spec.refreshTicks || HELD_REFRESH_TICKS, spec.amplifier || 0, false, spec.visible === true))
   })
   ```
3. **Registration:** `global.toolEffects.held(i.dragon_pickaxe, { effect: ef.haste, amplifier: 1, unlessCrouching: true, refreshTicks: 5 })`.
   With a 5-tick refresh, Haste is gone a quarter second after you crouch, and nothing has to be removed.
4. **Tooltip:** in `armor_effects.js` `buildTooltipLines` (line 137-144), append `spec.note` when present:
   `'§7- §' + d.color + d.name + ' §7(infinite' + (spec.note ? ', ' + spec.note : '') + ')'`. Set
   `note: 'not while sneaking'` on the pickaxe spec. Add a flavour line in tooltips.js:
   `[i.dragon_pickaxe, ['Mines faster unless sneaking', col.epic]]`.
5. Docs: armor_effects.md 2.3 (pickaxe row; also a hoe row pointing at fixes_compat.md 2.9), 2.6 (the new spec fields).

### Ids and data
`minecraft:haste` (`ef.haste`), `dragonloot:dragon_pickaxe` (`i.dragon_pickaxe`, item_dict:6320).

### Decisions
- **Cancel on crouch vs a short Haste while standing:** **recommend the short refresh (5 ticks).** Removing the effect on
  crouch (`player.removeEffect(haste)`) would also strip a beacon's or a potion's Haste, because vanilla merges them into
  one instance. A 5-tick refresh needs no removal and lags 0.25 s.
- **Haste level:** **recommend II** (+40% dig speed). Haste is hidden, so a Haste II beacon plus the pickaxe stays at II.
- **Per-piece armor bonuses:** the five armor items share one 7-effect set, which is the framework cap
  (`MAX_EFFECTS` 7). **Recommend not giving armor pieces their own bonus**: the set is the armor's identity. Scope the row
  to the pickaxe, then close it once `effects #2` and `gear/tuning #13` are done.

### Gotchas
- Crouching is also how players mine carefully at edges. That is the point (precise mining without Haste overshooting).
- Haste hides the vanilla mining-speed tooltip gain; the flavour line covers it.
- Every held effect is one client packet per tick (F8). A 5-tick refresh doesn't change that.

### Tests
- **AE-31 Dragon pickaxe Haste:** hold it → Haste II in `ActiveEffects` (hidden); crouch → gone within 0.5 s; stand →
  back. Tooltip: "Haste II (infinite, not while sneaking)".
- FX-12 already covers the hoe.

---

## effects #7: Faraday armor shock check
Status **Planned** · Work: none · id `potions/effects #7`

### Goal
Know whether the IE Faraday suit protects against shock and electric damage in this pack.

### Now (verified 2026-10-21 against `ImmersiveEngineering-1.19.2-9.2.4-170.jar`, decompiled)
- `FaradaySuitItem.onStrike` only acts on `IEDamageSources.ElectricDamageSource`. It is called by
  `IElectricEquipment.applyToEntity` from `ElectricDamageSource.apply` once per strike.
  - If the source's `ElectricSource.level < 1.75`: each worn piece sets a bit; with **all four pieces** (mask 15) the
    damage is set to 0. With fewer than four pieces: no reduction at all.
  - If `level >= 1.75`: damage x1.2, and each piece takes 2 durability (a piece that breaks is removed).
- Which IE sources are `ElectricDamageSource`, and their levels:
  - wire shock (`ieWireShock`): copper 0.5, electrum 1.0, steel 1.5 (`IEWireTypes.ShockingWire`: `0.5 * (1 + ordinal)`).
    **Blocked by a full suit.**
  - Tesla coil (`ieTesla`, `causeTeslaDamage`): low power 0.25 (blocked), high power 2.0 (x1.2 and wear).
- **Not covered by the suit:** `ieRazorShock` (a plain `IEDamageSource`), lightning (`lightningBolt`), Create Additions'
  `tesla_coil`, SecurityCraft `securitycraft.electricity` / `.taser`, Alex's Mobs and other modded shocks. All of them are
  in the pack's `electric` damage tag ([electric.json](../../development/patcherMod/src/main/resources/data/creativeengineering/damage_tags/electric.json)),
  which only the shields read.
- The pack doesn't touch the suit: no script references `i.armor_faraday_*` (item_dict 3805-3808).
- Unverified: how IE's Conductive effect (the pack's copper / iron / gold set drawback) scales electric damage. It is
  applied outside `FaradaySuitItem`. Check `IEPotions` / IE's event handler if it matters.

### Build
Nothing to build to answer the row. Write the facts above into armor_effects.md as a short "2.7 Faraday suit (IE,
unchanged)" subsection, then run AE-32 / AE-33.

### Decisions
- **Extend the suit to the pack's `electric` tag** (lightning, Create Additions tesla, SecurityCraft, razor shock)? That
  would be a patcherMod `LivingHurtEvent` listener: four `armor_faraday_*` pieces worn and
  `DamageTags.is(source, DamageTags.id("electric"))` → cancel, or scale like IE's two bands. **Recommend: not now.**
  Record the answer, and open a row only if the user wants lightning immunity from a cheap IE suit. Pairs with
  `potions/potions #9` (shock effects).

### Gotchas
A partial suit gives **nothing** against low-level shocks (all-or-nothing mask). Players expect per-piece protection.
Worth a tooltip line if the row is extended.

### Tests
- **AE-32 Faraday vs wires:** full suit, walk into a live uninsulated HV (steel) wire span → no damage. Take one piece
  off → full damage.
- **AE-33 Faraday vs tesla:** full suit next to a high-power IE Tesla coil → hurt (1.2x) and the suit loses durability; low
  power → no damage.

---

## Built, needs play

| Row | Change | Status | Notes |
|---|---|---|---|
| effects #1 | Armor and tool effects | Complete | Code matches armor_effects.md: F1 and F2 fixed 2026-10-19 (one iron `set()` at armor_effects.js:212-215, comment says 7). AE-01 to AE-24 still unticked; this brief proposes AE-25 to AE-33 |

## Writeup corrections made

- [armor_effects.md](../armor_effects.md) F7: added the Better Beacons check (Phantom Bane is only tested with
  `hasEffect`, so its level does nothing).
- [armor_effects.md](../armor_effects.md) 2.5: the Magic Resistance description `TODO` is on line 11 of `lang/en_us.json`,
  not line 8.
