# magic-resistance-effect

> creativeengineering:magic_resistance — pack-original MobEffect (KubeJS) + patcherMod LivingHurtEvent listener reducing damage from DamageSource#isMagic() sources, 20%/amplifier level

Moved from agent memory 2026-11-16 (last edited there 2026-09-13). Repo status docs win over any status line here.

Built 2026-09-12. No installed mod already provides this — CoFH Core has a `magic_resistance`
effect ("immune to all magic damage") but CoFH Core isn't in this pack's `mods/` folder (only
showed up because `jeed`, an effect-description mod, bundles lang text for it speculatively).
Installing CoFH Core just for this was rejected in favor of a from-scratch implementation,
partly because CoFH's version is all-or-nothing immunity rather than tunable per amplifier.

**Split, and why:** the MobEffect's EXISTENCE is pure KubeJS
(`kubejs/startup_scripts/magic/magic_resistance.js`, `StartupEvents.registry('mob_effect', ...)`
→ KubeJS's `BasicMobEffect`/`MobEffectBuilder`, builder type string `'basic'`) — this is a
pack-ORIGINAL effect with no cross-mod registry-timing problem (unlike `better_beacons:
long_reach`/`phantom_bane`, which needed patcherMod specifically because KubeJS's early registry
pass can't see another mod's not-yet-fired MobEffect registration — see [echo-tier-gear](echo-tier-gear.md)
/ potions.js's header comment). But the actual DAMAGE REDUCTION needed patcherMod
(`potion/MagicResistanceEffect.java`, `@SubscribeEvent` on `LivingHurtEvent`), because this
build's KubeJS wraps that event read-only: decompiled `LivingEntityHurtEventJS` (the class
backing `EntityEvents.hurt`) and confirmed it exposes only `getDamage()` — no `setAmount()`, no
cancel. This is the same category of constraint that already forced patcherMod for the potion
bucket dispenser and the echo-armor tooltip earlier this session — **KubeJS mutation of event
data is real but inconsistent per-event; always check the actual event-wrapper class in the jar
before assuming a KubeJS event can be used to modify (not just observe) something.**

**Mechanic:** `LivingHurtEvent` listener checks `event.getSource().isMagic()` (a real vanilla
`DamageSource` flag — potion/instant-damage, evoker fangs, conduit, dragon's breath, etc. are
already flagged this way, no new damage category needed) and `entity.getEffect(magicResistance)`.
Reduction = `min(1.0, 0.2 * (amplifier + 1))`, i.e. same 20%-per-level scaling as vanilla
Resistance, full immunity at amplifier 4+ (matches vanilla Resistance's own cap). The `MobEffect`
Java object is looked up lazily and cached on first use inside `MagicResistanceEffect`, not at
class load/mod construction — same reasoning as `potion_area.js`'s `mobEffect()` helper and
`armor_effects.js`'s `resolveEffect()`: both run before KubeJS's own `RegisterEvent<MobEffect>`
has actually fired.

**Not brewable yet** — no "Potion of Magic Resistance". Adding that would need the exact same
real `DeferredRegister<Potion>` dance as `potion/CEPotions.java` (was BetterBeaconsPotions) (referencing our own
`creativeengineering:magic_resistance` by `ResourceLocation` from a Java supplier that only runs
at the real `RegisterEvent<Potion>`, safely after `RegisterEvent<MobEffect>`) — `cooking/
potions.js`'s `CUSTOM_POTIONS` table can't reach it directly for the identical timing reason it
already can't reach `better_beacons` effects. Can still be applied directly (`/effect give`,
[armor-tool-effects](armor-tool-effects.md)'s `.piece()`/`.set()`, etc.) without brewing support.

Color: blueviolet (`0x8a2be2`), category BENEFICIAL. Not yet confirmed in-game (no way to launch
the modpack from this environment) — worth checking the effect actually applies/reduces damage
and that `event.create('creativeengineering:magic_resistance', 'basic')`'s chained
`.displayName(...).beneficial().color(...)` all landed (Rhino resolves by runtime class, not
declared Java return type, so the `.displayName()` call returning a wider `BuilderBase<T>`
before `.beneficial()`/`.color()` should be fine, but unverified live).
