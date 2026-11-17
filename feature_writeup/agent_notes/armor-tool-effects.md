# armor-tool-effects

> global.armorEffects piece/set/mount (horse armor slot 401), global.toolEffects onHit/held, bonemeal charm

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Merged 2026-10-19 from 4 memories (index compaction).

## armor-effects-framework

Built 2026-09-12 in response to "a framework to add arbitrary effects to armor, tooltip matching
the echo armor effect tooltip, set bonuses marked as such, up to 6 effects per piece/set."

**Three files, split by which KubeJS context each piece has to run in** (mirrors
[potion-buckets](potion-buckets.md)'s potion_area.js/func + server_scripts split):
- `startup_scripts/func/armor_effects.js` — the actual `global.armorEffects` API (`.piece(itemId,
  effects)` / `.set(itemIds, effects)`) plus shared formatting helpers. Pure data + logic, no
  event registration of its own. Effect ids are resolved **lazily** (first real use, cached),
  never at registration time — same reasoning as potion_area.js's `mobEffect()` helper and
  potions.js's header comment: KubeJS startup scripts run before other mods' `MobEffect`
  registries are populated, so an eager lookup here would silently fail for anything modded.
- `client_scripts/armor_effects_tooltip.js` — `ItemEvents.tooltip`, reads `PIECES`/`SETS` and
  calls `e.add(itemId, [...rawLines])`.
- `server_scripts/magic/armor_effects.js` — `PlayerEvents.tick`, checks
  `player.getItemBySlot(HEAD/CHEST/LEGS/FEET)` against the registry every tick and reapplies a
  short (40-tick), `ambient=false, visible=false` `MobEffectInstance` for every matching spec —
  same "invisible HUD icon, tooltip is the real indicator" + "reapply every tick so it never
  expires" idiom as CDD's own `EchoArmorEffectProcedure` (60-tick reapply, see
  [echo-tier-gear](echo-tier-gear.md)).

**Why tooltip lines go through `ItemEvents.tooltip`, not `ItemEvents.modification`:**
decompiled this build's jar and confirmed the modification event's runtime `Item` (`ItemKJS`/
`ModifiableItemKJS`) has NO tooltip accessor whatsoever — see memory `tune-tools-func`'s new note.
`ItemEvents.tooltip` (client-side, display-time) is the only mechanism that can add lines to an
arbitrary item regardless of who registered it, which is required here since armor pieces may be
vanilla/other-mod items, not just our own. Tooltip lines are **raw pre-formatted strings**
(`'§7- §aNight Vision §7(infinite)'`), not `client_scripts/tooltips.js`'s own `{text,color}`
object convention — that convention is single-color-per-line and can't reproduce the two-tone
"gray dash + colored name + gray duration" CDD style; `e.add()` accepts a plain string (or array
of strings) directly, proven by this same file's own `addTooltips()` helper already passing a
raw array through.

**Tooltip header text is copied verbatim from CDD**: set bonuses get `"§7With full set on:"` —
the exact string still present in `EchoArmorItem`'s own tooltip (patcherMod's
`EchoArmorTooltipMixin` only swapped the two effect-name constants, not this header — see
[echo-tier-gear](echo-tier-gear.md)). Per-piece (non-set) effects get `"§7When worn:"` (author's choice —
CDD has no single-piece example to copy, since all 4 echo pieces only ever grant the FULL-set
bonus, never anything per-piece).

**API shape:** `effects` array, max 6 (`MAX_EFFECTS`), each
`{ effect: 'ns:id', amplifier?: 0, name?: 'Display Name', color?: 'a' }` — `color` is a single
legacy-format-code letter (not a `col`-map word), defaulting to `'a'` (green) for
BENEFICIAL/NEUTRAL and `'c'` (red) for HARMFUL, via `MobEffect#getCategory()`, same red/green
split as [echo-tier-gear](echo-tier-gear.md)'s tool-hit-debuff-lines-vs-armor-buff-lines convention. `name`
has no auto-translation lookup (no client-safe way to resolve an arbitrary effect's localized
name at either registration or tooltip-build time) — falls back to a naive Title Case of the
effect id's path if omitted; an explicit name matching vanilla's real wording is recommended.

**Set bonuses actually registered (2026-09-12), bottom of armor_effects.js** (needed
`// priority: -100` added to the file once it started reading `global.item` at top level — same
load-order gotcha as tune_tools.js/tune_armor.js):
- gold (`golden_helmet/chestplate/leggings/boots`) -> Luck.
- netherite -> Fire Resistance + `alexsmobs:knockback_resistance` (vanilla has no "knockback
  resistance" MobEffect at all — Alex's Mobs does, and it's already a live dependency of this
  pack per `am-potion-brewing-fix`/potions.js's `AM_POTION_MIXES`).
- felsic (`felsic_gear:felsic_*`) -> `better_beacons:long_reach` (real effect, see
  `CEPotions.java` / potions.js) + Jump Boost.
- dragon (`dragonloot:dragon_*`) -> Resistance + Fire Resistance + `alexsmobs:poison_resistance` +
  `alexsmobs:knockback_resistance`. Registered as **two** `.set()` calls sharing one
  `dragonEffects` array — once with `dragon_chestplate`, once with `upgraded_dragon_chestplate`
  (the elytra-capable variant, see [dragon-gear-notes](dragon-gear-notes.md)) — since a player wears one
  chestplate or the other, never both, and `.set()` needs an exact id list. This means
  helmet/leggings/boots each belong to TWO registered `SETS` entries; without a fix the tooltip
  script would append the identical "With full set on:" block to them twice. Fixed by
  deduplicating in `armor_effects_tooltip.js`: track `itemId + '|' + renderedLinesJoined` in an
  `added` map and skip a repeat `e.add()` for the same exact block on the same item. The
  `PlayerEvents.tick` side needed no equivalent fix — a player can only satisfy one of the two
  chestplate-specific groups at a time, so effects never double-apply.
- **Echo armor deliberately NOT migrated** to this framework, despite being the literal
  inspiration for it and the user explicitly asking to consider replacing it. Judgment call, not
  a preference gap: `EchoArmorEffectProcedure` is Create: Deep Dark's own hardcoded MCreator
  listener that unconditionally applies Resistance+Strength — a `PlayerEvents.tick` handler has
  no way to CANCEL that, only stack more effects on top of it, so registering echo here would
  give Night Vision+Phantom Bane ON TOP OF (not instead of) the original Resistance+Strength.
  Same problem for the tooltip: CDD's item already bakes its own "With full set on:" block into
  `appendHoverText` (corrected in place by `EchoArmorTooltipMixin`) — a second block from
  `ItemEvents.tooltip` would duplicate it underneath rather than replace it. The mixin pair can
  REDIRECT an existing effect/tooltip; this framework can only ADD a new one — for echo the
  mixins have to stay. Documented inline in armor_effects.js so it doesn't read as an oversight.

All effect ids chosen (`luck`, `fire_resistance`, `knockback_resistance`, `long_reach`,
`jump_boost`, `resistance`, `poison_resistance`) happen to auto-title-case to their correct
vanilla/AM display name via the framework's fallback (`titleCase(id.split(':').pop())`), so none
of these needed an explicit `name` override — only worth revisiting if a future effect's id
doesn't read naturally that way (e.g. multi-concept ids).

Nothing in-game confirmed yet: can't launch the modpack from this environment. Things worth
double-checking on first real load: `player.getItemBySlot(...)` Rhino visibility (untested — no
prior use of `EquipmentSlot`/`getItemBySlot` anywhere else in this pack), that
`e.add(id, arrayOfRawStrings)` actually renders the embedded §-codes as two-tone (inferred from
the API accepting a generic `Object` per line, not directly proven for raw strings specifically —
only proven for `{text,color}` objects in tooltips.js), and that the dragon dedupe actually
produces one clean block rather than none/wrong content.

## armor-effects-mount-support

Added `global.armorEffects.mount(itemId, effects)` to [armor-tool-effects](armor-tool-effects.md)
(startup_scripts/func/armor_effects.js) so the same piece()/set() data shape and tooltip
rendering can cover equipment worn by a non-player mob, not just player armor slots.

**Why a separate path was needed:** dragon_horse_armor (dragonloot:dragon_horse_armor) is
a HorseArmorItem worn by AbstractHorse in its own Container (armor at Container index 401
= EQUIPMENT_SLOT_OFFSET 400 + INV_SLOT_ARMOR 1, read via the public `getSlot(int).get()` —
there's no public getArmor()-style getter, the backing `inventory` field is protected).
That's a completely different mechanism from EquipmentSlot.HEAD/CHEST/LEGS/FEET, so
server_scripts/magic/armor_effects.js's existing PlayerEvents.tick (which only calls
player.getItemBySlot on those 4 slots) can never see it. tune_armor.js already hit this
same wall for stat tuning — see its own comment at the dragon_horse_armor line.

**How it works now:**
- `client_scripts/armor_effects_tooltip.js` loops `AE.MOUNTS` the same way it loops
  `AE.PIECES` — tooltip rendering doesn't care who wears the item, so no new code path
  was needed there beyond one more Object.keys().forEach.
- `server_scripts/magic/armor_effects.js` gained a second handler, `LevelEvents.tick`,
  that walks `level.getAllEntities()`, filters `instanceof AbstractHorse`, reads the
  armor stack via `entity.getSlot(401).get()`, and applies effects the same way
  `applyEffects()` already did for players (renamed param `player` -> `target` since it's
  now called with either a Player or an AbstractHorse).
- dragon_horse_armor is registered with `minecraft:regeneration` (amplifier 0). UPDATE 2026-10-03: it was registered as `minecraft:strong_regeneration` (a potion id) until then, so it never worked; and the per-tick walk was disabled for perf, now back as a pass every `perf.mountEffectIntervalTicks` (100) ticks. See [perf-known-issues-fixes](perf-known-issues-fixes.md).

**How to apply:** any other mob-worn (not player-worn) equipment — future horse/wolf/
llama armor — should go through `.mount()`, not `.piece()`. If a future case needs a
mob type without a `getSlot()`-style public accessor, that's a strong signal to check
patcherMod mixins instead, per memory `centralize-recipes-tags-items` preferring KubeJS-first
but falling back to Java when reflection can't reach a protected field.

## tool-effects-framework

Added `global.toolEffects` (startup_scripts/func/tool_effects.js), a sibling to
[armor-tool-effects](armor-tool-effects.md) for potion effects granted by a TOOL rather than armor.
Reuses that framework's private effect-spec helpers
(`global.armorEffects._resolveEffect` / `_describe`) since they're fully generic (lazy
MobEffect lookup, name/color/roman-numeral formatting) — no duplicate logic.

**Two registration calls, same file structure as armor_effects.js (data + API in the
func/ file, consumers split out):**
- `global.toolEffects.onHit(itemId, effects)` — applies to whatever the wielder hits in
  melee, once per hit, via `EntityEvents.hurt` keyed to the attacker's main-hand item id.
  Each effect spec needs a `duration` (ticks) — REQUIRED, unlike every other effects list
  in this codebase, since onHit is a one-shot MobEffectInstance with no tick to refresh
  it from. Tooltip: "On hit, inflicts to target:".
- `global.toolEffects.held(itemId, effects)` — applies continuously to the wielder while
  the item is in either hand, via `PlayerEvents.tick` (checks both
  `getMainHandItem()`/`getOffhandItem()`), same reapply-every-40-ticks "(infinite)" idiom
  as armor_effects.js's PlayerEvents.tick — and literally reuses its tooltip builder
  (`global.armorEffects._buildTooltipLines('While held', effects)`) since the semantics
  are identical to armor's "when worn". Tooltip: "While held:".

Consumers: `client_scripts/tool_effects_tooltip.js` (tooltip), `server_scripts/magic/
tool_effects.js` (the actual EntityEvents.hurt + PlayerEvents.tick logic).

**Migrated the echo tools onto it**: the 4 echo_pickaxe/axe/shovel/hoe on-hit Weakness II
+ Darkness II (5 sec, matching create_deep_dark:echo_sword) used to be hardcoded in a
one-off `server_scripts/magic/echo_tool_effects.js` + manual `.tooltip()` lines in
`startup_scripts/items/echo_tools.js` ([echo-tier-gear](echo-tier-gear.md)). That file is deleted; the
same effect is now `global.toolEffects.onHit(...)` calls at the bottom of
tool_effects.js, and echo_tools.js just has an explanatory comment instead of literal
tooltip text. Unlike echo ARMOR's set bonus ([echo-tier-gear](echo-tier-gear.md)), this migration
was safe because the on-hit tool effect was always our own KubeJS code, never CDD's
hardcoded mixin logic — nothing to conflict with.

**How to apply:** any new "X on hit" or "effect while holding Y" request should go
through `toolEffects.onHit()`/`.held()` rather than a new one-off EntityEvents.hurt/
PlayerEvents.tick handler — see the echoOnHit registration at the bottom of
tool_effects.js for the exact call shape.

## bonemeal-charm-tool-effect

Added a generalized version of Lets Do: Vinery's Winemaker-armor "infinite bonemeal" trick
(`satisfyu.vinery.mixin.BoneMealItemMixin`, decompiled with CFR from `letsdo-vinery-forge-1.3.10.jar`)
that fires from a single held item instead of a 4-piece armor set.

**Mechanism (both Vinery's and ours):** `BoneMealItem#useOn` already consumed one bone meal
and decided to return `InteractionResult.CONSUME` before a `@Inject(at = "RETURN")` mixin
fires. On a probability roll, the mixin calls `ItemStack#grow(1)` on the just-used stack to
refund it — pure post-hoc refund, no vanilla bonemeal logic touched. A second independent
roll damages the "responsible" item as the cost (Vinery: all 4 armor pieces; ours: the charm).

**Our implementation** (2026-09-24):
- `patcherMod/.../mixin/BonemealCharmMixin.java` — targets `BoneMealItem`, SRG method id
  `m_6225_` (= `useOn`, looked up in `patcherMod/build/reobfJar/mappings.tsrg`) with
  `remap = false`, same idiom as `LivingEntityShieldMixin`/`VanillaShieldDurabilityMixin`
  ("no refmap; the pack only runs reobfuscated" — see [patchermod-coremod](patchermod-coremod.md)). Since bone
  meal occupies whichever hand triggered `useOn` (`context.getHand()`), the charm is checked
  in the OTHER hand.
- `patcherMod/.../util/BonemealCharms.java` — holds the `TagKey<Item>` for
  `#creativeengineering:bonemeal_charms`, mirrors `ElytraChestplates.java`'s pattern exactly.
- `CEConfig.BonemealCharm` — `enabled`/`chance`/`damageChance`/`damagePerUse`, same
  category-class pattern as `CEConfig.Shrapnel`.
- KubeJS: tag membership added via `global.tags.add(e, 'creativeengineering:bonemeal_charms', [i.dragon_hoe])`
  in `kubejs/server_scripts/tags/equipmenttags.js`; tooltip line added to `dragon_hoe` in
  `kubejs/client_scripts/tooltips.js`'s existing End Gear `addTooltips(e, ...)` block (matches
  the other dragon tools' one-line-per-effect style).
- Registered in `creativeengineering.mixins.json`'s `mixins` (server-independent) list.

**How to extend:** to add the charm effect to another item, just add it to the
`bonemeal_charms` tag call above — no rebuild needed (tag is data-driven, reload-able).

Status and in-game tests: [armor_effects.md](../armor_effects.md) section 0.
