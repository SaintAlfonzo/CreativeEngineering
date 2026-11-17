# damage-types

> damage_types_findings.md (every DamageSource + flags); flag + msgId damage tags, reload listener

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Merged 2026-10-19 from 2 memories (index compaction).

## damage-types-findings

`damage_types_findings.md` (instance root) lists every damage source in the pack with its 1.19.2 flags
(bypassArmor, fire, magic, explosion, projectile, fall, scalesWithDifficulty, damageHelmet, bypassInvul, ...).
1.19.2 has no data-driven damage types, so these are `DamageSource` msgIds found via bytecode (`javap -c`) plus
`death.attack.*` lang keys. SRG setter ids: m_19380_ bypassArmor, m_19381_ bypassInvul, m_19382_ bypassMagic,
m_238403_ bypassEnchantments, m_19383_ setIsFire, m_19389_ setMagic, m_19375_ setExplosion, m_19366_ setProjectile,
m_146708_ setIsFall, m_19386_ scalesWithDifficulty, m_146706_ damageHelmet.

**Why:** the user wanted a categorized list plus "the actual damage type applied in game"; the flags are that.

**How to apply:** reuse the doc for armor/shield/resistance work instead of re-tracing. Key findings: ~23 modded sources
bypass armor (and shields); Create fans/crush, Supplementaries bombs, Farseer scale with difficulty (0 on Peaceful);
IE bullets are not flagged projectile. Statically derived. See [shields](shields.md) and
[magic-resistance-effect](magic-resistance-effect.md).
The doc's section 7 covers the tag layer built on top of it - see [damage-types](damage-types.md).

## damage-tags-layer

Built 2026-09-20 (steps 1+2 of the plan the user approved), made comprehensive + shields fully tag-only 2026-09-24 (user: "every possible damage type... added to my own damage tags... i dont see things like magic" + "meta damage tags, like phys, magic, elemental" + "shields should only use my own damage tags"). 1.19.2 has no damage-type registry, so a "tag" here is a `ResourceLocation` a `DamageSource` belongs to. **Compiled + reobf'd, headless-smoke-tested (156 checks across two harnesses as of the iron-folding pass, 2026-09-24), DEPLOYED to `mods/creativeengineering-1.0.0.jar` 2026-09-24** (game had closed between sessions; the jar also carries [shields](shields.md)'s iron-folding work from the same day) - but still **not yet observed in an actual play session**, no way to launch/play from this environment. Findings/format doc: section 7 of `damage_types_findings.md` (instance root); the source catalog is [damage-types](damage-types.md).

**Files** (`creativeCommon/src/main/java/com/SaintAlfonzo/creativeengineering/damage/`):
- `DamageFlag` - enum of 12 built-in flag tags (`creativeengineering:armor_piercing|fire|magic|explosion|projectile|fall|difficulty_scaled|melee|helmet_damage|bypasses_resistance|bypasses_enchantments|bypasses_invulnerability`), computed live from the `DamageSource` flags; one bit each -> `mask()` used in the cache key because a msgId's flags aren't constant (melee depends on the attacker). Unchanged since 2026-09-20.
- `DamageTags` - public API: `tagsOf(source)`, `is(source, tagId)`, `definedTags()`, `id(path)`. Holds a volatile `DamageTagSnapshot`; before the first data load only flag tags exist.
- `DamageTagSnapshot` - immutable merged generation + per-(msgId,mask) cache (cleared with the snapshot on reload, capped 4096). Membership = flag (if built-in) ∪ msgId exact ∪ `*` glob ∪ `#tag` includes resolved by fixpoint (cycles OK). A data file named like a built-in flag ADDS ids to it (never removes - so a data file can't UN-tag something the live flag already covers, only widen).
- `DamageTagLoader` - `SimplePreparableReloadListener` on `AddReloadListenerEvent` (static `@SubscribeEvent`, registered in `CreativeEngineering` ctor). Reads `data/<ns>/damage_tags/**.json` via `listResourceStacks`, merges all packs low->high priority, honors `"replace": true`. Bad file/entry = logged + skipped. Logs `[damage-tags] loaded N data-defined tag(s)`.
- `DamageTagLogger` - `LivingAttackEvent` at LOWEST + `receiveCanceled`; gated by `[damage_tags] logSeenSources` (`CEConfig.DAMAGE_TAGS`, default false, live). Logs each distinct (msgId, flag mask) once per session; `tags=NONE` = unclaimed by any file (should be rare now).
- `shield/ShieldCategory`/`ShieldBlocking` now read ONLY tags, no exceptions but one (see below) - see [shields](shields.md).

**2026-09-24 data pass - 30 tag files, all validated against the bytecode/lang extraction:**
- Every one of the 12 `DamageFlag` entries now ALSO has an explicit, comprehensive `damage_tags/<name>.json` (magic, fire, explosion, projectile, fall, armor_piercing, melee, helmet_damage, difficulty_scaled, bypasses_resistance, bypasses_enchantments, bypasses_invulnerability) - this is what fixes "I don't see things like magic": before this pass those 12 were pure computed flags with no visible/editable file at all. Deliberately exclude `mob`/`player`/bare `arrow`-in-armor_piercing from these lists since those ids are reused by unrelated mechanics (Quark pickarang, Create Guardian turret, Nock Enough Arrows reinforced arrow) - only the live per-instance flag can tell those apart; listing the bare id would wrongly widen every reuse.
- Two pure META tags, `#include`-only, no ids of their own: `elemental` = fire∪cold∪electric∪chemical, `physical` = melee∪machinery∪piercing∪firearm∪explosion∪projectile. `magic` is a hybrid (explicit vanilla-magic ids + `#include` of psychic + life_drain).
- Fixed a real bug from the first pass: `machinery.json` referenced `#creativeengineering:cutting` but `cutting.json` never existed (dangling include, silently matched nothing, only a log warning) - now `cutting.json` exists (saw/drill/ieSawmill/ieSawblade) and `machinery.json` is a pure meta of blunt+cutting.
- Full roster: armor_piercing, blunt, bypasses_enchantments, bypasses_invulnerability, bypasses_resistance, chemical, cold, cutting, difficulty_scaled, electric, elemental, environmental, explosion, fall, fire, firearm, helmet_damage, life_drain, machinery, magic, melee, misc (Minecolonies internals + Alex's Mobs freddy - administrative/odd, no group), physical, piercing, pneumatic, projectile, psychic, security, suffocation, void.

**Shields are now tag-only, confirmed by inspection:** `ShieldBlocking` no longer calls `source.isBypassArmor()`/`isBypassInvul()` - both are `DamageTags.is(source, DamageFlag.ARMOR_PIERCING/BYPASSES_INVULNERABILITY.tag())`. The ONE remaining direct-source-call exception, deliberate and documented in the class javadoc: the `AbstractArrow` piercing-arrow check, because that's a structural vanilla mechanic (not a kind of damage) that unconditionally bypasses every shield. `ShieldGroup.FIRE` renamed `ELEMENTAL` (toml key `fire`->`elemental`, comments/label auto-follow); `ShieldCategory` now imports `DamageTags` directly (not `DamageFlag`) for its 5 tag bindings, so EXPLOSION/PROJECTILE/MAGIC/ELEMENTAL/MELEE all resolve the same way regardless of whether the underlying tag is flag-derived or pure data. Real, intended behavior changes: lightning/acid/tesla-shock/icicle-cold/copper-poisoning (zero vanilla flags) now shield as ELEMENTAL instead of falling to OTHER/physical; Botania's relic curse now shields as MAGIC (via magic's include of psychic) instead of OTHER.

**Gotchas hit:** `EventPriority` here has no `MONITOR` (use `LOWEST` + `receiveCanceled`). `out.computeIfAbsent(k, ..).add(parse(..))` creates the empty entry BEFORE `parse` throws -> parse first (smoke test caught it: a malformed file was defining an empty tag). In `DamageFlag`'s ctor use `new ResourceLocation(DamageTags.NAMESPACE, path)`, not `DamageTags.id()`, to avoid a class-init cycle. Verified the reobf jar remaps all 12 flag calls to SRG (`m_19376_` isBypassArmor, `m_19384_` isFire, `m_19387_` isMagic, `m_19372_` isExplosion, `m_19360_` isProjectile, `m_146707_` isFall, `m_7986_` scalesWithDifficulty, `m_7640_` getDirectEntity, `m_146705_` isDamageHelmet, `m_19379_` isBypassMagic, `m_238340_` isBypassEnchantments, `m_19378_` isBypassInvul). 2026-09-24: `supplementaries.bomb_explosion` (bare, no `.player` suffix) is a real separate msgId only visible in bytecode, not in any lang key - don't trust a lang-only extraction pass for full coverage, always cross-check bytecode too. A synthetic test `DamageSource` subclass needs to actually call the flag setters that match the real vanilla constant it's standing in for, or a "does the tag agree with the flag" assertion is comparing against the wrong thing, not testing production code - use the real vanilla constant (`DamageSource.OUT_OF_WORLD`, etc.) instead of a synthetic stand-in whenever one exists.

**First real in-game test, 2026-09-24 22:20, crashed** - see [shields](shields.md) for the full writeup (blocking an arrow with a shield hit `NoClassDefFoundError` for `DamageFlag` via `DamageTags.tagsOf`). Traced to a stale/incomplete deployed jar (missing `DamageFlag.class` despite containing its callers), not a bug in this package's source; a newer jar was already rebuilt+redeployed by the time this was diagnosed, but that hasn't been re-tested in game yet.

**Still to do:** per-tag resistance attributes + a `LivingHurtEvent` listener for armor (attributes fixed in Java at startup, membership reloadable; multiply reductions across matching tags, keep first-match priority for shield categories; confirm in bytecode that `LivingHurtEvent` fires before vanilla armor), and a KubeJS `global.damageTags` helper writing the JSON (via `ServerEvents.highPriorityData`) + tooltips. Possible extra: let shield effect filters name any tag, not just `ShieldCategory`/`ShieldGroup` keys. Known limit unchanged: `mob`/`player` can't be told apart by id alone, rely on the live flag.
