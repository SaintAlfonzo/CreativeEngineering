# effects-writeup

> feature_writeup/effects.md (EF-) - one table of all 140 mob effects by mod; tools/effect_audit.py checks names/descriptions/arrow names offline

Moved from agent memory 2026-11-16 (last edited there 2026-10-19). Repo status docs win over any status line here.

Written 2026-10-19 at the user's request ("effect writeup, starting with vanilla and going through each mod").
The first section is ONE table (mod as a column, the user asked for one table): effect, mod, Brew, Arrow (tipped arrow
has its own name), Super (only works as other effects combined, or vanilla plus extra), Fluid (JEI create:potion),
Desc (JEED text Y / N / Partial / Wrong, PROPOSED replacement text when not Y), Food, Subset, Overlaps
(slight/total), Brief, Detailed. Behaviour was read from decompiled jars (scratch subset decompile of
effect/potion/brew/event classes) and the Vinery/Brewin port sources, not guessed.

- `python3 tools/effect_audit.py` (`--json` for the table): names incl. AM's `alexsmobs.potion.*` keys, JEED
  descriptions, TODO placeholders, stale placeholders, unnamed potion/arrow keys. `EFFECT_OVERRIDES` drops the 3
  Vinery effects the port removed until /dumppotions is rerun.
- JEED shows every effect in JEI except `hidden_effects` in config/jeed-client.toml (Sunny).
- Exports from 2026-10-18 predate the port swap; rerun /dumppotions + /dumpfood.

2026-10-20 (ui/effects TODO rows, built):
- `kubejs/server_scripts/other/jeed_providers.js` = ALL jeed:effect_provider recipes (Vinery wine loop moved there from
  alcohol.js) + one jeed:potion_provider (potion buckets/bundles, IE bullet, PNC ammo). JEED itself already lists
  FoodProperties foods, flower stews, beacon, potions (EffectInfo.buildStaticCache), so providers are only for mobs
  (spawn eggs), blocks/fluids/gear, code-applied food, armor/tool registries, Botania brews (forge:partial_nbt brewKey),
  Xaero switches -> command block. NCH jeedProvider takes NBT, drops missing ids, skips unregistered effects.
- effect_audit.py `no JEI source` check (reads effect id literals in jeed_providers.js + registries + wine config).
- Category audit = effects.md 2.4; overrides in CEConfig `[effects] categoryOverrides` (patcherMod
  potion/EffectCategories + MobEffectCategoryMixin on getCategory m_19483_ / isBeneficial m_19486_). Only red vs blue
  is visible (NEUTRAL renders blue), so only colour-wrong effects were overridden (6).
- User: "leave TODO for any effect description missing" -> 10 placeholders stay until they write text.

Related: [jeed-effect-descriptions](jeed-effect-descriptions.md), memory `feature-writeup-pattern`, [am-potion-brewing-fix](am-potion-brewing-fix.md), [backported-vinery](backported-vinery.md).
