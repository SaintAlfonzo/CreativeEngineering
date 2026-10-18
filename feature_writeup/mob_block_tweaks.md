# Mob and block tweaks: bubble column immunity, Bushier Flowers off, feeding-trough retry, block properties

Small, unrelated changes to other mods' mobs and blocks, collected in one file. Armor enchantability overrides, first
listed here in the backlog, are in [gear_tuning.md](gear_tuning.md).

## 0. At a glance

| Part | Status |
|---|---|
| Bubble columns don't push big water mobs (squid, dolphins, turtles, Alex's Mobs whales, sharks, seals, giant squid) | DEPLOYED with 1.0.6 (built 2026-10-02, staged for 1.0.5 as `BC`); in-game test pending |
| Bushier Flowers: no bone-meal growth, no worldgen; its bushes crafted from 4 flowers | DEPLOYED with 1.0.6 (built 2026-10-10); in-game test pending |
| Quark feeding trough: a hungry animal that finds no trough waits 30 s before searching again | DEPLOYED with 1.0.4 (perf fix); scratch server checked the mixin applies, not measured under load |
| Create: Dreams & Desires asphalt: friction and speed factor reset to vanilla (BlockRunner tiers it instead) | DEPLOYED; BR-28 pending |

- **Pending:** nothing built and unshipped. MT-01 to MT-12 are new (BR-28 stays in its writeup). Findings F1 to F4
  (section 4); none needs a decision.
- **Files to open:** [BubbleColumnImmunityMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/BubbleColumnImmunityMixin.java),
  [BushierFlowersDisableMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/BushierFlowersDisableMixin.java),
  [FeedingTroughModuleMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/FeedingTroughModuleMixin.java),
  [block_properties.js](../kubejs/startup_scripts/changes/block_properties.js).
- **Knobs:**
  - `#creativeengineering:bubble_column_immune` entity tag ([entitytags.js](../kubejs/server_scripts/tags/entitytags.js); `/reload`).
  - `[perf] troughMissCooldownTicks` in `config/creativeengineering-common.toml` (600; 0 = off; live).
  - Block properties: startup script, restart.

## 1. Overview

- **Bubble columns** (soul sand / magma under water) push any entity inside up or down. Big mobs get launched out of
  the water or pinned to the floor. Upgrade Aquatic's nautilus and thrasher already ignore them.
- **Bushier Flowers 0.0.2** turns a vanilla flower into a "grown" bush with bone meal (and pops loose flowers off a
  grown one), and adds grown bushes to worldgen. Flower Patch also uses bone meal on flowers.
- **Quark's feeding trough** lets animals eat from a trough to breed. Quark checks for one from every `TemptGoal`
  update of every animal ready to breed: a POI search (range 10) plus a ray cast, almost every tick when there's no
  trough.
- **D&D asphalt** has its own friction 0.35 (vanilla 0.6) and speed factor 0.001, which stack on top of BlockRunner's
  asphalt tier.

## 2. Reference

### 2.1 Bubble column immunity

Immune (tag `creativeengineering:bubble_column_immune`): `minecraft:squid`, `glow_squid`, `dolphin`, `turtle`;
`alexsmobs:orca`, `cachalot_whale`, `seal`, `giant_squid`, `giant_squid_part`, `hammerhead_shark`, `frilled_shark`.

Both directions: no push up over soul sand, no pull down over magma, and no air-refill/bubble effects from the column
for them. Players, items, boats and every other mob are unchanged.

### 2.2 Bushier Flowers

| Path | Before | Now |
|---|---|---|
| Bone meal on a vanilla flower | grown bush | vanilla bone meal (Flower Patch's behaviour) |
| Bone meal on a grown bush | pops a loose flower | vanilla bone meal |
| Dispenser with bone meal | same as above | vanilla |
| Worldgen | grown bushes in plains, meadow, flower forest, swamp, default | none (5 biome modifiers replaced by `forge:none`) |
| Crafting | | 4 of a flower → its grown bush, shapeless (13 bushes: allium, azure bluet, blue orchid, cornflower, dandelion, lily of the valley, orange / pink / red / white tulip, oxeye daisy, poppy, wither rose) |

Existing bushes, their dye recipes and potted versions stay.

### 2.3 Feeding trough retry

When Quark's trough search comes back with nothing and no real player is tempting the animal, that animal's
`TemptGoal` is stamped "don't search before now + `troughMissCooldownTicks`" (600 = 30 s). Until then the search
returns the previous answer at once. A hit is never stamped, so an animal that has a trough keeps using it at full
speed. Detail and measurements: [perf.md](perf.md) 7.

### 2.4 Block properties

All 16 D&D asphalt colours (`create_dd:<colour>_asphalt_block`): friction 0.6 and speed factor 1.0 (vanilla), jump
factor 1.25 kept. Their walk speed comes from BlockRunner's asphalt tier (1.50), like Immersive Petroleum's asphalt
([blockrunner.md](blockrunner.md)).

## 3. What the pack changes

- **Bubble columns:** `BubbleColumnImmunityMixin` cancels `BubbleColumnBlock#entityInside` (SRG `m_7892_`) at the head
  for tagged entity types. Hooking the block rather than `Entity#onAboveBubbleCol` / `onInsideBubbleColumn` also covers
  mobs that override those without calling super.
- **Bushier Flowers:** `BushierFlowersDisableMixin` empties `ModBlocks.validFlowers` and `grownFlowers` after the class
  initialises, so both of the mod's growth paths miss every check and fall through to vanilla. Worldgen: the mod's 5
  biome modifier files are overridden with `{"type": "forge:none"}` in `kubejs/data/bushierflowers/forge/biome_modifier/`.
  Recipes: [flowers.js](../kubejs/server_scripts/other/flowers.js).
- **Trough:** `FeedingTroughModuleMixin` (head and return of `FeedingTroughModule#temptWithTroughs`) with the stamp on
  `TemptGoal` added by `TemptGoalTroughRetryMixin` through the `compat/TroughRetryAccess` interface.
- **Block properties:** `BlockEvents.modification` in [block_properties.js](../kubejs/startup_scripts/changes/block_properties.js).

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. Entity ids are literals.** `entitytags.js` names the immune mobs as raw ids; there's no entity dictionary
  next to `item_dict.js` / `block_dict.js` / `fluid_dict.js`. Fine for one tag; worth an `entity_dict.js` if more entity
  tags come.
- **F2. Bushier Flowers worldgen is turned off file by file.** Only the 5 modifier files the mod ships today are
  replaced. If an update adds or renames one, its bushes generate again. [flowers.md](flowers.md) lists Bushier
  Flowers as off.
- **F3. Trough: ineligible animals are stamped too** (a baby, one already in love). Its first trough search after
  becoming eligible can wait up to 30 s ([perf.md](perf.md) 7).
- **F4. Not measured:** the trough fix was checked to apply on a scratch server, never measured under load.

### Known limits

- **Bubble column immunity is by entity type:** a modded mob added later needs a tag line.
- **Bushier Flowers bushes still exist** as blocks and items (crafted, or from old worlds).

### Rejected options

- **Removing Bushier Flowers:** its bushes are used decoratively and in dye recipes; only growth and worldgen were the
  problem.
- **Hooking `Entity#onAboveBubbleCol`:** some mobs override it without calling super.

## 5. File map

- patcherMod mixins: [BubbleColumnImmunityMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/BubbleColumnImmunityMixin.java), [BushierFlowersDisableMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/BushierFlowersDisableMixin.java), [FeedingTroughModuleMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/FeedingTroughModuleMixin.java), [TemptGoalTroughRetryMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/TemptGoalTroughRetryMixin.java); [compat/TroughRetryAccess.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/TroughRetryAccess.java); `CEConfig.Perf.troughMissCooldownTicks`.
- KubeJS: [entitytags.js](../kubejs/server_scripts/tags/entitytags.js), [flowers.js](../kubejs/server_scripts/other/flowers.js), [block_properties.js](../kubejs/startup_scripts/changes/block_properties.js), `kubejs/data/bushierflowers/forge/biome_modifier/` (5 files).

## 6. Offline checks

None. All four need the game.

## 7. In-game test suite

- [ ] **MT-01 Squid ignore columns:** soul sand column in a 1 × 1 water pillar, push a squid in → it swims normally,
  not shot up. Same for a dolphin and a turtle.
- [ ] **MT-02 Magma column:** magma column, a glow squid inside → not pulled to the floor.
- [ ] **MT-03 Alex's Mobs:** an orca or hammerhead shark in a column → unaffected.
- [ ] **MT-04 Others still pushed:** a cod, a player and a boat in the soul sand column → pushed up as in vanilla.
- [ ] **MT-05 Tag reload:** remove `minecraft:squid` from the tag in a datapack override, `/reload` → squid pushed
  again. Remove the override.
- [ ] **MT-06 No bush from bone meal:** bone meal on a poppy → vanilla behaviour (or Flower Patch's), no grown poppy.
- [ ] **MT-07 Grown bush, bone meal:** bone meal on an existing grown poppy → no loose poppy popped off.
- [ ] **MT-08 Dispenser:** dispenser with bone meal facing a dandelion → no grown dandelion.
- [ ] **MT-09 No worldgen:** new chunks in plains, meadow, flower forest and swamp → no Bushier Flowers bushes.
- [ ] **MT-10 Craft a bush:** 4 cornflowers in the grid → 1 grown cornflower.
- [ ] **MT-11 Trough still works:** two adult cows, a Quark feeding trough with wheat within 10 blocks, no player
  nearby → they eat from it and breed.
- [ ] **MT-12 Trough off switch:** `[perf] troughMissCooldownTicks = 0` → same as MT-11 (and vanilla-Quark cost). Put
  it back to 600.

## 8. History

- By 2026-09-27: D&D asphalt friction reset ([blockrunner.md](blockrunner.md)).
- By 2026-10-02 (first committed): bubble column immunity (staged for 1.0.5, shipped in 1.0.6).
- 2026-10-03: feeding-trough retry (perf fix, shipped in 1.0.4).
- 2026-10-10: Bushier Flowers growth and worldgen off, 4-flower recipes (shipped in 1.0.6).
- 2026-10-18: this writeup; findings F1 to F4; tests MT-01 to MT-12. No code changed.
