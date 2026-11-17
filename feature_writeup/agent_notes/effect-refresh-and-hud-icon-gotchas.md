# effect-refresh-and-hud-icon-gotchas

> 1.19.2 effect mechanics - re-adding Health Boost clamps health; 5-arg MobEffectInstance hides the HUD icon; Creative Common effect remap/HUD/compound hooks

Moved from agent memory 2026-11-16 (last edited there 2026-10-31). Repo status docs win over any status line here.

- Re-adding an effect with a longer duration fires `LivingEntity#onEffectUpdated(forced=true)`, which runs
  `removeAttributeModifiers` then `addAttributeModifiers`. Health Boost's removal clamps health to the un-boosted max,
  so a periodic refresh (armor/mount frameworks) eats the extra hearts. `server_scripts/magic/armor_effects.js`
  `applyEffects` restores health after each pass (bytecode-checked 2026-10-31).
- `new MobEffectInstance(e, t, a, ambient, visible)` sets showIcon = visible: "no particles" also hides the HUD icon.
  Use the 6-arg ctor `(..., false, false, true)`. The inventory list never checks showIcon (only the HUD does).
- Creative Common hooks (2026-10-31): `potion/EffectRules` + CEConfig `[effects] remap` / `removed` / `forceHudIcon` / `forceHudIconItems` (per item, so armor-given copies stay hidden)
  (mixins on `LivingEntity#addEffect` m_147207_, `Gui#renderEffects` m_93028_); compound part text in
  `potion/CompoundEffects`; Caffeinated -> Phantom Bane in `potion/PhantomBaneRules`.

**Why:** these cost a full investigation each time; status of the features lives in feature_writeup/effects.md 2.5-2.8.
**How to apply:** swap or hide an effect via the config lists, not new mixins; any new refreshed Health Boost source needs
the same health restore. Related: [armor-tool-effects](armor-tool-effects.md), [effects-writeup](effects-writeup.md).
