# Mob and block tweaks: bubble column immunity, Bushier Flowers off, feeding-trough retry, block properties, leaf decay, named mobs, villager gates, doors and ladders, monster box vaults

Small, unrelated changes to other mods' mobs and blocks, collected in one file. Armor enchantability overrides, first
listed here in the backlog, are in [gear_tuning.md](gear_tuning.md).

## 0. At a glance

| Part | Status |
|---|---|
| Bubble columns don't push big water mobs (squid, dolphins, turtles, Alex's Mobs whales, sharks, seals, giant squid) | DEPLOYED with 1.0.6 (built 2026-10-02, staged for 1.0.5 as `BC`); in-game test pending |
| Bushier Flowers: no bone-meal growth, no worldgen; its bushes crafted from 4 flowers | DEPLOYED with 1.0.6 (built 2026-10-10); in-game test pending |
| Quark feeding trough: a hungry animal that finds no trough waits 30 s before searching again | DEPLOYED with 1.0.4 (perf fix); scratch server checked the mixin applies, not measured under load |
| Create: Dreams & Desires asphalt: friction and speed factor reset to vanilla (BlockRunner tiers it instead) | DEPLOYED; BR-28 pending |
| Alex's Mobs seagulls never take a lunchbox: not from the inventory, not dropped (patcherMod `AlexsMobsSeagull*Mixin`, every Spice of Life food container; the `seagullStealingBlacklist` config entries from 2026-10-22 stay; [lunchboxes.md](lunchboxes.md)) | Mixins BUILT 2026-10-29 (patcherMod 1.1.04 in `mods/`), not deployed; config list load scratch server PASSED 2026-10-22 (SQ-GM-07); MT-26 in game |
| MCA Comb sets eye colour (11 colours) and eye shape (2) on its hairstyle page, for the player's own look and for a combed villager | BUILT 2026-10-22, DEPLOYED with 1.1.01 (2026-10-22); MT-40 pending in game |
| Leaf decay: natural leaves live up to 12 leaf steps from a log (vanilla 6), decay only after a block next to them changes (never from random ticks), and fall fast once their tree is cut (Fast Leaf Decay ported in, its jar removed) | BUILT 2026-10-19, DEPLOYED with 1.1.01 (2026-10-22, changelog Farming & Nature); scratch server PASSED (MT-13 to MT-16, MT-19 to MT-23, MT-24 decay piles); MT-13 to MT-24 pending in game |
| Named mobs: a naturally spawned farm animal (cow, mooshroom, pig, sheep, chicken, goat) has a 1 in 100 chance of a random name (MCA citizen names); every player sees its death message ("Bessie the Cow was slain by Steve") (patcherMod `mobs.NamedMobs`, `[named_mobs]`, TODO `mobs/tweaks #6`) | BUILT 2026-10-30 (patcherMod 1.1.04 in `mods/`), not deployed; scratch server PASSED r1031 (2026-10-31, SQ-NM-01 to -03); MT-31 to MT-33 pending in game |
| Villagers treat fence gates as walls (open or closed) and path through Dramatic Doors' tall and short wooden doors, opening them (patcherMod `McaVillagerPathGatesMixin`, `WalkNodeEvaluatorVillagerGateMixin`, TODO `mobs/tweaks #15`) | BUILT 2026-10-30 (patcherMod 1.1.04 in `mods/`), not deployed; scratch server PASSED r1031 (2026-10-31, SQ-VG-00 to -03); MT-41 to MT-43 pending in game |
| Villagers path up and down ladders (every block named "ladder" that is climbable; patcherMod `McaVillagerPathLaddersMixin`, `GroundPathNavigationClimbMixin`, `MoveControlVillagerLadderMixin`, tag `#creativeengineering:villager_ladders`) | BUILT 2026-10-30 (patcherMod 1.1.04 in `mods/`), not deployed; scratch server PASSED r1031 (2026-10-31, SQ-VL-00 to -02, up and down a ladder); MT-44 to MT-46 pending in game |
| Quark monster boxes are vaults: the box stays after its wave, ejects one stack from its own loot table once the wave is dead or gone, then re-arms after a 1 hour cooldown; obsidian hardness (diamond pickaxe); breaks only while sneaking, else a chat warning (Lootr's rule); Create drills and saws skip it (patcherMod `mobs.MonsterBoxVault`, `MonsterBoxBlockEntityMixin`, `CreateBreakerMonsterBoxMixin`, TODO `mobs/tweaks #5`) | BUILT 2026-10-30 (patcherMod 1.1.04 in `mods/`, 22:59); scratch server PASSED r1031 (2026-10-31, SQ-MB-01 to -04, -06, -07: stays, one-stack reward, re-arm, Create drill, off switch); SQ-MB-05 hardness client check pending (tags pass; queue says 50, changelog 25); MT-34 to MT-37, MT-47 to MT-50 in game pending |

- **Pending:** the leaf decay changes are built and unshipped (next deploy; the release also drops FastLeafDecay-30.jar,
  now in `disabledmods/`). MT-01 to MT-25 untested in game (BR-28 stays in its writeup). Findings F1 to F7 (section 4);
  F6 (diagonal-only leaves) is a possible follow-up.
- **Files to open:** [BubbleColumnImmunityMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/BubbleColumnImmunityMixin.java),
  [BushierFlowersDisableMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/BushierFlowersDisableMixin.java),
  [FeedingTroughModuleMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/FeedingTroughModuleMixin.java),
  [block_properties.js](../kubejs/startup_scripts/changes/block_properties.js),
  [LeafDecayRange](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/block/LeafDecayRange.java),
  [FastLeafDecayPort](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/block/FastLeafDecayPort.java).
- **Knobs:**
  - `#creativeengineering:bubble_column_immune` entity tag ([entitytags.js](../kubejs/server_scripts/tags/entitytags.js); `/reload`).
  - `[perf] troughMissCooldownTicks` in `config/creativeengineering-common.toml` (600; 0 = off; live).
  - Block properties: startup script, restart.
  - `[leaf_decay] enabled` (true; false = vanilla range and slow random-tick decay, no fast decay),
    `onlyWhenDisturbed` (true), `maxDistance` (12; 6 = vanilla; range 6 to 32), `maxSearchBlocks` (4096),
    `decayMinTicks` / `decayMaxTicks` (4 / 11, Fast Leaf Decay's defaults), same toml. Read on every decay check; edit with the server stopped (an in-place edit of a running scratch server's toml
    was not picked up).
  - `[named_mobs]` (section 2.6): `enabled`, `chance` (0.01), `mobs` (ids or `#tags`), `spawnReasons`
    (NATURAL, CHUNK_GENERATION, STRUCTURE), `announceDeaths`, `fallbackNames`. Read live.
  - `[monster_box]` (section 2.9): `vaultMode` (true; false = Quark's self-breaking box), `cooldownTicks` (72000),
    `rewardTable` (`creativeengineering:misc/monster_box_reward`), `rewardSingleCount` (false), `leashRange` (24),
    `checkIntervalTicks` (20). Read live. Quark's `Enable Extra Loot Table` is false (`quark-common.toml`), so wave
    mobs drop only their own loot.
- **How to measure:** `python3 tools/leaf_path_scan.py` (structure templates, offline) and
  [tools/leaf_path_probe.js](../tools/leaf_path_probe.js) on a scratch server (every tree feature); timing and decay
  checks with [tools/leaf_decay_probe.js](../tools/leaf_decay_probe.js). Section 6.

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
- **Leaf decay:** a natural leaf (not player-placed) keeps a `distance` of 1 to 7, the leaf steps to the nearest log;
  at 7 it decays on a random tick. So vanilla keeps leaves up to 6 steps out, and a cut tree's canopy thins over
  minutes. Fast Leaf Decay (Olafski, FastLeafDecay-30, in the pack until 2026-10-19) gave leaves next to a block that
  turned to air a decay check 4-10 ticks later. 25 of the pack's 265 tree features (mostly Terralith, plus Vinery's
  apple and dark cherry trees) grow leaves 7 to 10 steps out, which fell off the generated trees on their own.

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

### 2.5 Leaf decay range

| | Vanilla | Pack (default) |
|---|---|---|
| Longest leaf path to a log a natural leaf survives | 6 | 12 (`[leaf_decay] maxDistance`, 6 to 32) |
| Player-placed (persistent) leaves | never decay | never decay |
| Leaves whose log is chopped | decay | decay (unless another tree of the same kind is within range, F5) |
| Which leaves carry the path | any leaves (Separated Leaves: only the leaves of the same rule, against that rule's logs) | same: Separated Leaves rules are read and kept |
| Leaves touching the tree only diagonally | decay | decay, at placement (F6) |
| When a natural leaf out of reach decays | any random tick (minutes, at random) | only after a block next to it changes: a leaf whose log was cut is checked `decayMinTicks`-`decayMaxTicks` (4-11) ticks later, a leaf already out of reach is checked at once (`onlyWhenDisturbed`) |
| Untouched tree, leaves 7-12 out | fall off over time | stay; never checked on random ticks |
| Cut tree | canopy thins over minutes (seconds with Fast Leaf Decay) | canopy gone in about a second, in a cascade (Fast Leaf Decay ported) |

Measured 2026-10-19 (scratch server, seed 8675309, `tools/leaf_path_probe.js`): every one of the 310 tree features
placed on a grass pad; 265 grew natural leaves. Longest path per tree: 0-6 for 240 trees, 7 for 19, 8 for 4, 9 for 1,
10 for 1. 283 of 35296 leaves were past 6, none past 12. Structure templates (`tools/leaf_path_scan.py`): 18 place
natural leaves, none past 6.

| Tree feature | Longest path | Leaves past 6 |
|---|---|---|
| `terralith:swamp/ice/small/swamp_small` | 10 | 6 |
| `terralith:cave/jungle/tree_very_large` | 9 | 12 |
| `terralith:snowy/max_spruce_big` | 8 | 27 |
| `terralith:jungle/violet/bending_acacia`, `terralith:savanna/shattered/small/acacia_small`, `terralith:skylands/summer/bending_acacia` | 8 | 2 to 5 |
| 15 more Terralith trees (cherry, tall birch / oak, dark oak, giant jungle top, violet 2x2, giant swamp ...) | 7 | 1 to 36 |
| `vinery:apple`, `apple_variant`, `dark_cherry`, `dark_cherry_variant` | 7 | 11 to 19 |

Paths are random per placement, so one placement per tree is a sample: a tree can reach a little further than listed.
12 leaves 2 steps of margin over the longest seen.

**Leaf piles (2026-10-20, patcherMod 1.1.01).** With Fast Leaf Decay installed, decayed leaves left Immersive
Weathering leaf piles: FLD ran `randomTick`, and IW's `LeavesMixin` calls `LeavesGrowth.decayLeavesPile` right after
its `removeBlock`. The port removes leaves in `tick`, so that hook never fired. `block/LeafDecayPiles` now calls the
same IW method after the port's `removeBlock` (IW's chance `spawn_entity_from_decay` 0.3, its blacklist, a 1-5 layer
pile in the leaf's place that then falls, the falling-leaves particles). Switch: `[leaf_decay] leafPiles` (default
true). How tall a pile must be to slow you: `[leaf_piles] slowFromLayers` (default 4 = IW's own "more than 3"; 9 =
never). `mixin/LeafPileSlowMixin` swaps the 3 in IW's `LeafPileBlock#entityInside`, and the WTHIT Run Speed line
(`surface/StuckInBlock`) uses the same value, so a pile shows a line exactly when it slows. Keep it equal on server and
clients (movement is predicted client-side).

The scratchpad's "restore Immersive Weathering leaf drop on decay" (scratchpad 2026-10-21, listed twice) is this
fix; MT-24 checks it (TODO `farming/trees #1`).

Code check 2026-10-22 (IW 1.2.10 jar disassembled, patcherMod 1.1.01 in `mods/` holds `LeafDecayPiles`): IW has three
leaf pile paths, and all three still run. (1) Decay piles: IW injects into `LeavesBlock#randomTick` after
`removeBlock`. With `onlyWhenDisturbed` our HEAD cancel stops that whole random-tick decay, so IW's hook can't fire
there, and the scheduled-tick decay calls the same `decayLeavesPile` once instead. With `onlyWhenDisturbed = false`
the random-tick decay runs and IW's own hook fires. No path calls it twice. (2) Ground piles under standing trees: IW's
builtin `leaf_piles_from_leaves` growth (chance 0.0005, `reach` 12, `max_pile_height` 3) runs from IW's
`ServerLevelMixin` in `ServerLevel#tickChunk`, outside `LeavesBlock#randomTick`, so our gate never touches it.
(3) Falling-leaf particles: client `animateTick`, untouched. (1) passed live on a scratch server 2026-10-22
(`tools/leaf_pile_probe.js`, patcherMod md5 37bb7049): scheduled-tick decay of 20 lone leaves of 5 types leaves IW piles
(5 of 20 at IW's 0.3, 20 of 20 at 1.0, every type), random ticks decay nothing with `onlyWhenDisturbed`, a felled oak
decays fully with its piles on the ground (none left in the air), `leafPiles = false` leaves none, and with
`onlyWhenDisturbed = false` IW's own random-tick hook makes 20 of 20. Vinery's apple leaves default to persistent
(as upstream 1.5.5); its tree features place them non-persistent, so natural ones decay. (2) is in-game only (MT-24),
because forceloaded chunks get no random ticks.

### 2.6 Named mobs

TODO `mobs/tweaks #6`, design in [briefs/mobs_tweaks.md](briefs/mobs_tweaks.md) tweaks #6 (owner 2026-10-30: a configurable
mob list, farm animals only for now; a large karma loss once karma exists).

- **Roll** (`LivingSpawnEvent.SpecialSpawn`): a mob in `[named_mobs] mobs` with no name, spawned for a reason in
  `spawnReasons`, is marked pending in its persistent data (`creativeengineering:named_mob` = 1) on `chance`. Bred,
  spawn egg, spawner and command mobs never roll. Farm animals mostly spawn with the chunk (CHUNK_GENERATION), so most
  named animals are found in new chunks.
- **Name** (`EntityJoinLevelEvent`, server thread): a pending mob gets `Names.pickCitizenName(Gender)` from MCA (random
  region, random male or female pool), else a random `fallbackNames` entry, and the mark becomes 2. The split exists
  because chunk generation and structure spawns run on worldgen threads and MCA's name pool uses one shared random
  (`API.getRng()`), which is not thread safe. MCA answers "Unnamed" while its lists are empty; that counts as no answer.
- **Death** (`LivingDeathEvent`, lowest priority, cancelled deaths skipped): a mob marked 2 that still has a name
  sends vanilla's death message to every player, its victim argument swapped for `"%s the %s"`
  (`chat.creativeengineering.named_mob.title`), so "Bessie the Cow was slain by Steve". Any cause counts, not only
  player kills. Follows `showDeathMessages`. A tamed mob's owner already gets vanilla's copy and is skipped.
  A name tag rename keeps the mark (the announcement uses the new name).
- **Karma:** the player-kill branch in `NamedMobs.onDeath` holds the hook for `largecontent/karma #1`; nothing happens
  there yet. The karma list in the TODO has the entry (large loss).
- Naming does not make a mob persistent; farm animals (creature category) never despawn anyway. A monster added to
  the list despawns as usual (brief: wanted).

### 2.7 Villager gates and doors

TODO `mobs/tweaks #15` (owner 2026-10-30: gates solid to villagers, Dramatic Doors usable).

- **Cause.** MCA overwrites vanilla villagers (`overwriteOriginalVillagers`) and its villager always paths with its own
  `VillagerNavigation` / `VillagerLandPathNodeMaker`, not `WalkNodeEvaluator`. Its `findNearbyNodeTypes` marks every
  `#minecraft:fence_gates` block WALKABLE_DOOR, open or closed. With the pack's `useSmarterDoorAI: false` the door task is
  vanilla `InteractWithDoor`, which never opens gates, so villagers routed into closed gates and pressed against them,
  and walked through open ones. The node maker's `getCommonNodeType` knows only vanilla `DoorBlock`, so a closed Dramatic
  Doors door was BLOCKED and never on a path. Dramatic Doors' own mixins (door types in `WalkNodeEvaluator`, opening in
  `InteractWithDoor`) did the rest of the job but MCA skipped the first half.
- **Change.** `McaVillagerPathGatesMixin`: `getCommonNodeType` returns FENCE for any gate (tag or `FenceGateBlock`
  subclass, so the Gates mod's metal gates too), and for a block in `#dramaticdoors:tall_doors` / `short_doors`:
  DOOR_OPEN when open, DOOR_WOOD_CLOSED when closed and in the `*_wooden_doors` tag (walkable for a mob that opens doors,
  as for vanilla doors), DOOR_IRON_CLOSED otherwise (metal doors stay shut). The gate-to-WALKABLE_DOOR check in
  `findNearbyNodeTypes` is redirected to false. Opening is Dramatic Doors' `InteractWithDoor` mixin, unchanged; it closes
  the door behind them as vanilla does.
- **Vanilla villagers** (unconverted ones): `WalkNodeEvaluatorVillagerGateMixin` returns FENCE for a gate in the per-block
  lookup when the pathing mob is a `Villager`. Closed gates were already FENCE there; this adds open ones. Other mobs and
  wandering traders keep vanilla.
- **Limits.** Short doors are one block high: only baby villagers fit. With `useSmarterDoorAI` turned on, MCA's own door
  task runs instead of `InteractWithDoor`; it opens vanilla doors and gates only, so Dramatic Doors would be on paths
  but not opened. Gates are FENCE for villagers even when a player leaves them open, by design.

### 2.8 Villager ladders

Owner 2026-10-30, after 2.7: villagers path up and down ladders.

- **Before.** MCA's node maker (2.7) has no vertical moves. A ladder block above the ground was OPEN, so the walker
  dropped through it, and no path went up one. Vanilla's WalkNodeEvaluator has none either.
- **Which blocks.** `#minecraft:climbable` and `#creativeengineering:villager_ladders` both. The pack tag is every block
  with "ladder" in its id (KubeJS [blocktags.js](../kubejs/server_scripts/tags/blocktags.js), `t.addMatching`, `/reload`;
  45 climbable ladders on 2026-10-30: vanilla, Quark, Create, Create Deco, IE, Everything Copper, Framed, Copycats,
  Upgrade Aquatic, Every Compat, Engineer's Decor). Vines, ropes, chains, scaffolding and grab rails stay out.
- **Paths** (`McaVillagerPathLaddersMixin`): a ladder block is WALKABLE (`getLandNodeType`), so a villager can stand on
  any rung and a step off a floor into a ladder shaft lands on the top rung; the ladder block straight above or below a
  node is added as a successor when the villager's box fits there; a villager on a ladder starts its path there
  (MCA's start search walked down to the foot of the ladder). Leaving at the top is the normal one-block step up from
  the top rung, so the ladder must reach the level of the floor it leads to (as for a player who doesn't jump).
- **Moving.** Up is vanilla: a mob in a ladder jumps every tick (the ladder's collision shape is at its feet) and a
  jumping mob on a climbable block climbs. Down needed two changes. `MoveControlVillagerLadderMixin`: a villager on a
  climbable block whose target is over half a block below and within half a block sideways stops jumping and walking,
  so it slides down at 0.15 blocks a tick (walking into the wall behind the ladder would climb). `GroundPathNavigationClimbMixin`:
  a villager on a climbable block counts as able to update its path; vanilla only follows paths on the ground, and in
  the air advances any node it is above in the same column, which on the way down skipped every rung and sent it
  walking off toward the exit while still high up.
- **Limits.** MCA villagers only for the paths; an unconverted vanilla villager keeps vanilla pathing (the two
  movement mixins apply to it, harmlessly). Ladder rungs cost the same as ground. A ladder in mid-air with ground 3 or
  fewer blocks below may be left by stepping off sideways (MCA's normal drop rule).

### 2.9 Quark monster boxes as vaults

Owner 2026-10-30 (TODO `mobs/tweaks #5`, design in [briefs/mobs_tweaks.md](briefs/mobs_tweaks.md) tweaks #5): the box
stays after use with a cooldown, is as hard as obsidian, breaks Lootr's way, and gives a single item when the wave is
dead.

- **Before.** Quark 3.4's box (`MonsterBoxBlockEntity#tick`) growls for 2 s once a player is within 2.5 blocks,
  spawns 5 to 8 mobs from `quark:misc/monster_box_spawns` (zombie, cave spider, witch; spawn type SPAWNER), and
  removes itself. No config kept it. Each wave mob dropped a roll of `quark:misc/monster_box` (which could hold another
  box). Strength 25, no tool needed, no drop.
- **Phases** (`mobs.MonsterBoxVault`, state on the block entity via `MonsterBoxBlockEntityMixin`):
  - ARMED: Quark's own tick. Its `Level#removeBlock` after `spawnMobs` is redirected: the box stays, every mob within
    4 blocks with Quark's `quark:monster_box_spawned` mark and no claim is stamped `creativeengineering:monster_box`
    (the box position) and listed as the wave; then ACTIVE.
  - ACTIVE: once a second (spread per box) the wave is checked. A mob counts as gone when unloaded, dead, or over 24
    blocks from the box. All gone: one roll of the reward table, one random stack of the result pops out of the top
    (ender chest sound, a puff), then COOLDOWN.
  - COOLDOWN: nothing until 72000 game ticks have passed, then ARMED again.
  - Peaceful freezes ACTIVE and COOLDOWN, as Quark's tick does nothing there.
- **Saved and synced.** Phase, cooldown end and wave are written in AutoRegLib's shared NBT, so they save with the box
  and reach clients in the update tag. The mixin adds an update packet (Quark's box had none), sent on each phase
  change. Clients run Quark's tick only while ARMED, so a spent box shows no flames and never plays the local break
  effect.
- **Reward table** [monster_box_reward.json](../kubejs/data/creativeengineering/loot_tables/misc/monster_box_reward.json):
  Quark's `misc/monster_box` with the box itself, TNT and the three infested blocks taken out; 1 pool, 1 roll. Stacks
  keep Quark's counts (raw iron 4 to 10 and so on); `rewardSingleCount = true` cuts the stack to 1.
- **Hardness.** [block_properties.js](../kubejs/startup_scripts/changes/block_properties.js): destroy speed 50,
  blast resistance 1200, requires the right tool; [blocktags.js](../kubejs/server_scripts/tags/blocktags.js):
  `mineable/pickaxe` + `needs_diamond_tool` (exclusive), and `create:non_movable`. Same numbers as obsidian, and
  explosions can't break it.
- **Breaking (Lootr's rule).** A Forge `BreakEvent` listener, as Lootr 0.4.29's `HandleBreak`: a box mined without
  sneaking is not broken and the player gets an aqua chat line ("Breaking the monster box destroys it for good. Sneak
  while mining to break it."); sneaking breaks it, with no drop. The check comes at the end of the mining time, as in
  Lootr. Creative players follow the same rule; fake players are refused without a message.
- **Create.** Drills, saws and their contraption forms break through `BlockHelper.destroyBlockAs` with no player, so no
  `BreakEvent`. `CreateBreakerMonsterBoxMixin` makes `BlockBreakingKineticBlockEntity.isBreakable` false for a box.
- **Every box.** Generated and placed boxes behave alike: with the box gone from the reward table, creative is the
  only other source.
- **Limits.** One reward per wave for whoever is there (no per-player list). Other machine breakers that post no
  `BreakEvent` (not checked beyond Create) could still break a box. `vaultMode = false` returns Quark's behaviour,
  except that the hardness and tags stay (KubeJS).

### 2.10 Giant squid flee distance (added 2026-11-02, TODO `mobs/tweaks #18`)

Alex's Mobs' giant squid runs from cachalot whales: its `AIAvoidWhales` goal starts when any whale is within 50 blocks
and keeps running while that whale is within 60, so squid scatter from whales far out of sight. Owner decision
2026-11-02: squid are the fleeing mob (the whales are not changed). Creative Compat `AmGiantSquidAvoidWhalesMixin`
(ModifyConstant on the goal's `canUse` 50 and `canContinueToUse` 60, gated on `alexsmobs`) reads
`config/creativecompat-common.toml` `[giant_squid]`: `whaleFleeStart` 16 (1 to 64), `whaleFleeStop` 24 (1 to 96, kept
at least the start). Read live each time the goal checks. Vanilla squid have no whale goal (they only flee what hurt
them); orcas and other whales are untouched.

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
- **Leaf decay range:** `LeavesDecayRangeMixin` injects at the head of `LeavesBlock#randomTick` (SRG `m_213898_`). For a
  decaying leaf (not persistent, distance 7: vanilla's `decaying()` test, inlined) it asks `LeafDecayRange.keep`,
  and cancels the decay on true. `keep` walks breadth-first from the leaf through the connected distance-7 leaves
  (loaded chunks only); a leaf `k` steps away with a real distance `d < 7` means a log at `k + d`, kept when that
  is at most `maxDistance`. With `onlyWhenDisturbed` (default) the random-tick decay is cancelled outright instead
  and the search runs only from the scheduled tick (below). Only distance-7 leaves are walked (the leaves nearer a log already carry their distance),
  so a search stays small; it gives up (the leaf decays) after `maxSearchBlocks` leaves. Leaves connect as in vanilla
  (any `LeavesBlock`) unless Separated Leaves has a rule for the leaf: then only that rule's `leaves` set connects
  (read by reflection from `Reload.LEAVES_RULES`, so its datapack reloads apply). Every decay path ends in
  `randomTick`: vanilla random ticks, Fast Leaf Decay (it calls `tick` then `randomTick`), and the fruit leaves that
  override it (Fruits Delight durian / passable / peach, Vinery `FruitLeavesBlock`) all call super. Block states are
  not touched: the `distance` property still stops at 7.
- **Decay timing:** the same mixin injects at the tail of `LeavesBlock#tick` (SRG `m_213897_`), the scheduled tick
  vanilla queues from `updateShape` when a neighbour changes; it has just stored the recomputed distance.
  `LeafDecayRange.afterTick`: still natural and at 7 afterwards? If it was below 7 when the tick started (its log is
  gone) it queues another tick `decayMinTicks`-`decayMaxTicks` later; if it was already 7 it runs `keep` and drops the
  leaf (drops + `removeBlock`, as vanilla's random-tick decay) when no log is in range. A dropped leaf updates its
  neighbours, so a cut canopy cascades. No leaf subclass in the pack overrides `tick` or `updateShape` (jar scan).
- **Fast Leaf Decay port:** `FastLeafDecayPort` (Forge `BlockEvent.NeighborNotifyEvent`): when the changed block is
  now air, each notified neighbour that is a natural `LeavesBlock` gets a vanilla scheduled tick
  `decayMinTicks`-`decayMaxTicks` out (a tick already queued wins). That covers removals that notify neighbours but
  skip shape updates (flag 16). The mod kept its own unbounded list and called `tick` + `randomTick` itself;
  scheduled ticks are saved with the chunk and the decay decision is the shared one above. Every
  `#minecraft:leaves` block in the pack is a `LeavesBlock` (scratch check), so nothing lost fast decay.

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

- **F5. A chopped tree next to a standing one keeps more leaves.** Chopped-tree leaves that join the canopy of a
  standing tree of the same kind (or any leaves without Separated Leaves) survive out to 12 from the standing tree's
  logs instead of 6. Lower `maxDistance` if that shows up as floating clumps.
- **F6. Diagonal-only leaves still decay, right at placement.** 189 leaves on 29 trees (Terralith acacias and palms,
  mangroves, cave jungle trees) touch the rest of the tree only by an edge or corner, so no face path reaches a log;
  vanilla decays them too. `TreeFeature` ends with `StructureTemplate.updateShapeAtEdge` over the new tree, which
  gives every edge leaf one shape update and so one decay check (in worldgen the tick waits in the chunk until it
  loads): those leaves, and any past maxDistance, fall when the tree is placed or its chunk first loads (scratch:
  `swamp_small` lost its 14 floating leaves within 4 s; `max_spruce_big` kept all of them). Everything connected
  within 12 survives that check. Options, not built: let the search step diagonally, or skip the check for leaves
  ticked by that edge pass.
- **F7. Removals with no neighbour notification don't trigger decay** (setBlock flags without 1 and with 16, e.g.
  some contraption moves): the leaves stay until another neighbour changes. Fast Leaf Decay behaved the same. Scratch:
  flags 18 kept all 575 leaves; flags 19 (notify, no shape update) cleared 987 through the ported event.

### Pending review with the mob food overhaul (TODO `tweaks #13`)

- Fungi Delight port (2026-10-23, [fungi_delight.md](fungi_delight.md) 5.1.3): pigs eat brown and red mushrooms, inky
  caps, morels and truffles (`Pig.FOOD_ITEMS`, combined with the Veggies and Vintage additions); truffle-bred litters get
  extra piglets; hoglins breed with truffles; piglins take them. No other pig-food change until the overhaul.

### Known limits

- **Bubble column immunity is by entity type:** a modded mob added later needs a tag line.
- **Bushier Flowers bushes still exist** as blocks and items (crafted, or from old worlds).
- **Leaf decay range covers `LeavesBlock` only:** a modded leaf block that doesn't extend it, or overrides
  `randomTick` without calling super, keeps its own rule. None in the pack today (scan of every jar, 2026-10-19).
- **Fruit leaves at distance 7:** Fruits Delight skips fruit growth on a leaf vanilla calls decaying. Its leaves
  past 6 now live but don't grow fruit.

### Rejected options

- **Removing Bushier Flowers:** its bushes are used decoratively and in dye recipes; only growth and worldgen were the
  problem.
- **Hooking `Entity#onAboveBubbleCol`:** some mobs override it without calling super.
- **Widening the leaf `distance` property past 7:** adds block states to every leaf block. State ids are already
  near the 2^19 chunk-packet limit (ghost blocks), and every client would need the same jar.
- **Recomputing distances further in `updateDistance`:** runs on every neighbour change of every leaf (and is where
  Separated Leaves hooks); the decay-time search only runs for leaves that are about to fall.
- **Keeping every natural leaf of a worldgen tree:** chopped trees would never lose their canopy.
- **A "disturbed" marker per leaf** (to tell worldgen leaves from cut ones): needs a block state or per-chunk
  storage; the scheduled tick is already that signal.
- **Keeping the Fast Leaf Decay jar next to the hooks:** it ran `tick` + `randomTick` from its own list, which the
  random-tick gate turns into a no-op and the tick hook already covers; one place for leaf decay instead.

## 5. File map

- patcherMod mixins: [BubbleColumnImmunityMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/BubbleColumnImmunityMixin.java), [BushierFlowersDisableMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/BushierFlowersDisableMixin.java), [FeedingTroughModuleMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/FeedingTroughModuleMixin.java), [TemptGoalTroughRetryMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/TemptGoalTroughRetryMixin.java); [compat/TroughRetryAccess.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/TroughRetryAccess.java); `CEConfig.Perf.troughMissCooldownTicks`.
- Leaf decay: [LeavesDecayRangeMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/LeavesDecayRangeMixin.java), [block/LeafDecayRange.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/block/LeafDecayRange.java), [block/FastLeafDecayPort.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/block/FastLeafDecayPort.java) (registered in `CreativeEngineering`), `CEConfig.LeafDecay`; tools [leaf_path_scan.py](../tools/leaf_path_scan.py), [leaf_path_probe.js](../tools/leaf_path_probe.js), [leaf_decay_probe.js](../tools/leaf_decay_probe.js). `disabledmods/FastLeafDecay-30.jar` (removed, row gone from [mod_sides.md](../mod_sides.md)).
- MCA eyes: [McaCombEyeColourMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/McaCombEyeColourMixin.java) (client), lang `creativeengineering.mca_eyes.*`; how MCA picks eyes: [briefs/mobs_tweaks.md](briefs/mobs_tweaks.md) tweaks #12.
- Named mobs: [mobs/NamedMobs.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mobs/NamedMobs.java) (registered in `CreativeEngineering`), `CEConfig.NamedMobs`, patcherMod lang `chat.creativeengineering.named_mob.title`.
- Villager gates and doors: [McaVillagerPathGatesMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/McaVillagerPathGatesMixin.java), [WalkNodeEvaluatorVillagerGateMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/WalkNodeEvaluatorVillagerGateMixin.java).
- Villager ladders: [McaVillagerPathLaddersMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/McaVillagerPathLaddersMixin.java), [GroundPathNavigationClimbMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/GroundPathNavigationClimbMixin.java), [MoveControlVillagerLadderMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/MoveControlVillagerLadderMixin.java); tag in [blocktags.js](../kubejs/server_scripts/tags/blocktags.js), key `t_villager_ladders` in `block_dict.js`.
- Monster box vaults: [mobs/MonsterBoxVault.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mobs/MonsterBoxVault.java) (registered in `CreativeEngineering`), [MonsterBoxBlockEntityMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/MonsterBoxBlockEntityMixin.java), [CreateBreakerMonsterBoxMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/CreateBreakerMonsterBoxMixin.java), `CEConfig.MonsterBox`, lang `chat.creativeengineering.monster_box.sneak`, [monster_box_reward.json](../kubejs/data/creativeengineering/loot_tables/misc/monster_box_reward.json), `quark-common.toml` (`Enable Extra Loot Table`, both copies).
- Giant squid flee distance: Creative Compat [AmGiantSquidAvoidWhalesMixin](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/mixin/AmGiantSquidAvoidWhalesMixin.java), `CompatConfig.GiantSquid` (`[giant_squid]`), gate in `CompatMixinPlugin`.
- KubeJS: [entitytags.js](../kubejs/server_scripts/tags/entitytags.js), [flowers.js](../kubejs/server_scripts/other/flowers.js), [block_properties.js](../kubejs/startup_scripts/changes/block_properties.js), `kubejs/data/bushierflowers/forge/biome_modifier/` (5 files).

## 6. Offline checks

- `python3 tools/leaf_path_scan.py`: structure templates with natural leaves and their longest leaf path to a log
  (2026-10-19: 18 templates, none past 6). Rerun after adding structure mods.
- Tree features need a scratch server: [tools/leaf_path_probe.js](../tools/leaf_path_probe.js) (instructions in its
  header; 2026-10-19 results in section 2.5). Rerun after adding tree or biome mods, and raise `maxDistance` if a
  tree comes within 2 of it.
- Decay timing, the port and cost: [tools/leaf_decay_probe.js](../tools/leaf_decay_probe.js) on a scratch server
  (commands in its header; MT-19 to MT-23 were run with it).
- The other parts need the game.

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
- [ ] **MT-13 Leaf line:** at y 250 (or any open air) an oak log with 16 oak leaves in a straight line from it, placed
  as `oak_leaves[persistent=false]` (`/fill`) → within a second leaves 13 to 16 decay (placing them is a change next to
  them), 1 to 12 stay. Scratch server PASSED 2026-10-19 (first with a manual trigger, again with the timing change).
- [ ] **MT-14 Chop:** break the log of MT-13 → the whole line decays. Scratch server PASSED 2026-10-19.
- [ ] **MT-15 Separated Leaves kept:** oak log, oak leaves 1 to 6, then birch leaves 7 to 12, all non-persistent →
  the birch leaves decay (birch doesn't count oak). Scratch server PASSED 2026-10-19.
- [ ] **MT-16 Vanilla setting:** `[leaf_decay] maxDistance = 6` (server stopped), MT-13 again → only leaves 1 to 6
  stay. Scratch server PASSED 2026-10-19. Put it back to 12.
- [ ] **MT-17 Big trees in a new world:** fly through a Terralith cherry grove, snowy spruce or violet jungle and an
  orchard of Vinery apple trees in fresh chunks, wait a few in-game minutes nearby → the canopies stay full (before:
  outer leaves dropped as saplings and apples).
- [ ] **MT-18 Player leaves unchanged:** leaves picked with shears and placed far from any log → never decay.
- [ ] **MT-19 Untouched tree ignores random ticks:** `/gamerule randomTickSpeed 1000` next to a big Terralith spruce or
  a bone-mealed dark oak for a minute → no leaves fall. Put it back to 3. Scratch server PASSED 2026-10-19 (randomTick
  called on every leaf of `max_spruce_big`: 647 -> 647; `swamp_small` 45 -> 45).
- [ ] **MT-20 Fast fall:** cut every log of a big tree (or fell it with TreeChop) → the canopy is gone within a couple
  of seconds, drops on the ground. Scratch server PASSED 2026-10-19 (1351 leaves gone within 12 s of `/fill`ing the
  logs away, most within 1 s).
- [ ] **MT-21 No lag spike:** F3 or `/forge tps` while MT-20 runs → no visible hitch. Scratch server: worst tick 146 ms
  in the second the 109 logs were removed (including the `/fill` commands), mean tick 2.9 ms afterwards.
- [ ] **MT-22 No Fast Leaf Decay jar:** `mods/` has no FastLeafDecay jar; `/forge mods` doesn't list `fastleafdecay`;
  `[leaf_decay] decayMinTicks` / `decayMaxTicks` in the toml. Scratch server PASSED 2026-10-19.
- [ ] **MT-23 Vanilla switch:** `[leaf_decay] enabled = false` (server stopped), MT-20 again → the canopy thins slowly
  over minutes (vanilla, no fast decay). Put it back to true.
- [ ] **MT-24 Leaf piles from decay:** fell an oak in grass → roughly a third of the canopy's leaves leave leaf piles
  that fall to the ground (as with the old Fast Leaf Decay jar). `[leaf_decay] leafPiles = false` → items only.
  Ground piles: stand under an untouched oak for a few in-game days (or `/gamerule randomTickSpeed 300` for a minute,
  then back to 3) → thin leaf piles appear on the grass under its canopy, as before the decay port. Decay piles and
  the switch: scratch server PASSED 2026-10-22; the fall in a real forest, ground piles and particles: in game.
- [ ] **MT-25 Pile slow height:** *Logic half: scratch server PASSED 2026-10-22 (SQ-OM-09: 3 layers no slow, 4 and 5 slow; SQ-OM-14 with 6: 5 no slow, 6 and 7 slow); walking and WTHIT: in game.* default `slowFromLayers = 4`: walk through 3-layer and 4-layer piles → only the
  4-layer one slows you, and WTHIT shows a Run Speed line on the 4-layer one only. Set 6 (game closed), repeat → 4 and
  5 no longer slow or show; 6 and up do. Depth Strider boots still walk through.
- [ ] **MT-26 Seagulls skip lunchboxes** (needs a real player: gulls only target players in the world): all four
  lunchboxes (Lunchbag, Copper, Iron, Netherite), with food inside, anywhere in the inventory on a beach with seagulls
  (no other food): no gull takes one in a few minutes. With a carrot added, a gull still steals the carrot. Drop a
  lunchbox near gulls: none picks it up. Mixins `AlexsMobsSeagullStealMixin` / `AlexsMobsSeagullItemMixin`
  (2026-10-29; the 2026-10-22 config list alone had not stopped them, owner report). Same check as LB-12.
- [ ] **MT-31 Name rolls:** set `[named_mobs] chance = 1.0` (read live), fly into new chunks → every cow, mooshroom,
  pig, sheep, chicken and goat shows a name when looked at; other animals (horses, rabbits) none. Breed two named cows
  and use a cow spawn egg → no names. Leave and come back (chunks unload) → the same names. Set `chance` back to 0.01.
- [ ] **MT-32 Announce:** kill a named cow → one chat line for every player, "<name> the Cow was slain by <you>". A
  named animal killed by lava or a fall → its vanilla message, same form. `/gamerule showDeathMessages false` → no line.
- [ ] **MT-33 Rename and plain names:** rename a named pig with a name tag, kill it → announced with the new name. A
  name-tagged pig that never rolled → no announcement. With MCA's names seen (MT-31), names look like real first names,
  not "Unnamed".
- [ ] **MT-41 Gates are walls:** a pen of fences with a closed oak gate, a villager inside with its job site or bed
  outside the pen (or the reverse) → it never walks into the gate and stays put (or finds another opening). Open the
  gate → it still does not walk through. Same with a Gates mod metal gate. A cow or horse still walks out of the open gate.
- [ ] **MT-42 Tall doors:** a closed house whose only door is a Dramatic Doors tall oak door, the villager's bed inside,
  the villager outside at dusk → it walks to the door, the door opens, it goes in, and the door closes behind it. Same
  out of the house in the morning. A tall iron door instead → it does not path through and does not open it.
- [ ] **MT-43 Short doors and vanilla doors:** a baby villager uses a short oak door (opens it); an adult does not get
  stuck at it. Vanilla oak doors are used as before.
- [ ] **MT-44 Up a ladder:** a 6-block wall with a ladder from the ground to its top, a platform on top with the
  villager's bed (or job site), the villager on the ground → it walks to the ladder, climbs, steps onto the platform.
  Same with a Quark spruce ladder and a Create andesite ladder.
- [ ] **MT-45 Down a ladder:** the reverse (villager on the platform, target on the ground) → it steps into the ladder,
  slides down rung by rung without climbing back up, walks off at the bottom; no fall damage. Also a 1 x 1 shaft
  through a floor with a ladder up to the floor level, both ways.
- [ ] **MT-46 Not ladders:** a vine-covered wall or a scaffolding tower as the only way up → the villager does not path
  up it. A ladder that stops one block short of the platform → not used (expected).
- [ ] **MT-40 Comb eye colour** (client only): hold an MCA Comb, right-click the air. The hairstyle page has `<` `Brown Eyes` `>`
  top right and `Eye Shape 1` top left. Step the colour through all 11 and toggle the shape → the hair previews show
  the new eyes each time. Press Done → your villager-model look keeps the eyes (switch to the villager model in the
  full editor if needed), the comb lost 1 use. Repeat by using the comb on an MCA villager: its eyes change in the
  world, also after a relog. Change the eyes, press Escape → nothing saved, no use lost. Pick a hairstyle after
  changing the eyes → both saved, 2 uses lost. A heterochromia villager shows its fixed two-colour art.

- [ ] **MT-34 Box stays:** find or `/setblock` a `quark:monster_box`, walk up → it growls, mobs spawn, the box stays
  and its flames stop.
- [ ] **MT-35 Reward:** kill the wave → one stack pops out of the box top once (ender chest sound); walking up again
  within the cooldown does nothing.
- [ ] **MT-36 Re-arm:** `[monster_box] cooldownTicks = 200` (game stopped), repeat → 10 s after the reward the
  flames are back and it growls again.
- [ ] **MT-37 Off switch:** `vaultMode = false` → Quark behaviour (the box breaks itself after spawning).
- [ ] **MT-47 Single item:** over several waves, each reward is exactly one stack from the reward table; never a
  monster box, TNT or infested block. Wave mobs drop only their normal loot.
- [ ] **MT-48 Obsidian hardness:** survival, diamond pickaxe → about 9.4 s, same as obsidian next to it; iron pickaxe
  → barely progresses; TNT beside it → the box survives.
- [ ] **MT-49 Sneak to break:** mine it standing → aqua warning in chat, the box stays (it reappears if it vanished
  client side); mine it sneaking → it breaks, nothing drops. Same in creative.
- [ ] **MT-50 Create:** a powered drill against a box, and a drill on a moving contraption → the box doesn't break;
  a contraption doesn't pick the box up.
- [ ] **MT-51 Giant squid flee distance [scratch]:** a giant squid and a cachalot whale summoned 30 blocks apart in deep
  open water → the squid does not flee; whale moved to 12 blocks → it swims away and stops once more than 24 off. With
  `whaleFleeStart = 50`, `whaleFleeStop = 60` it flees at 30 again (Alex's Mobs' own).

## 8. History

- By 2026-09-27: D&D asphalt friction reset ([blockrunner.md](blockrunner.md)).
- By 2026-10-02 (first committed): bubble column immunity (staged for 1.0.5, shipped in 1.0.6).
- 2026-10-03: feeding-trough retry (perf fix, shipped in 1.0.4).
- 2026-10-10: Bushier Flowers growth and worldgen off, 4-flower recipes (shipped in 1.0.6).
- 2026-10-18: this writeup; findings F1 to F4; tests MT-01 to MT-12. No code changed.
- 2026-10-19: leaf decay range ([TODO](todo/TODO.md) `farming/trees #1`): `[leaf_decay]` (default 12), `LeavesDecayRangeMixin`
  + `LeafDecayRange`, patcherMod 1.0.9 rebuilt into `mods/` (not deployed). Every tree feature and template measured
  (longest 10); scratch server: MT-13 to MT-16 passed. Findings F5, F6; tools leaf_path_scan.py, leaf_path_probe.js.
- 2026-10-19 (later): decay only after a neighbour change (`onlyWhenDisturbed`, tick hook) and Fast Leaf Decay ported
  into patcherMod (`FastLeafDecayPort`, `decayMinTicks` / `decayMaxTicks`); FastLeafDecay-30.jar to `disabledmods/`,
  `mod_sides.md` row deleted, `pack/` and `server_mods/` regenerated. Scratch server: MT-13, MT-15, MT-19 to MT-22
  passed; F6 corrected (diagonal leaves fall at placement, `TreeFeature`'s edge shape updates), F7 added.
- 2026-10-20: IW leaf piles from the ported decay (`LeafDecayPiles`, `[leaf_decay] leafPiles`) and configurable pile
  slow height (`[leaf_piles] slowFromLayers`, `LeafPileSlowMixin`, WTHIT line follows). patcherMod 1.1.01 rebuilt into
  `mods/`. MT-24, MT-25 untested.
- 2026-10-22: leaf piles from decay passed on the scratch server (SQ-LP-00 to SQ-LP-06, `tools/leaf_pile_probe.js`,
  patcherMod md5 37bb7049; probe fixed in the run: `scheduleTick` overload, Vinery apple leaves set non-persistent).
  MT-24 ground piles and the fall in a real forest, and MT-25, stay in game.
- 2026-10-22: MCA Comb eye colour and shape (`McaCombEyeColourMixin`, TODO `mobs/tweaks #12`); patcherMod 1.1.01
  rebuilt into `mods/`. MT-40 untested.
- 2026-10-22: seagulls skip lunchboxes (`mobs/tweaks #7`): Alex's Mobs `seagullStealingBlacklist` holds the three
  Spice of Life lunchboxes (config only; the dropped-box mixin not built). MT-26 untested.
- 2026-10-22 (second bulk run): SQ-GM-07 (seagull list) and SQ-OM-09 / SQ-OM-14 (pile slow height, default and 6)
  PASS on the scratch server; MT-25 and MT-26 keep their in-game halves.
- 2026-10-30: named mobs (`mobs/tweaks #6`): `mobs.NamedMobs`, `[named_mobs]` (farm animals, 1 in 100, MCA names),
  death announcements; patcherMod 1.1.04 rebuilt into `mods/`. MT-31 to MT-33 untested, SQ-NM queued.
- 2026-10-30: villager gates and doors (`mobs/tweaks #15`): gates are FENCE and Dramatic Doors doors get door types in
  MCA's node maker (`McaVillagerPathGatesMixin`), open gates FENCE for vanilla villagers
  (`WalkNodeEvaluatorVillagerGateMixin`); patcherMod 1.1.04 rebuilt into `mods/`. MT-41 to MT-43 untested, SQ-VG queued.
- 2026-10-30 (later): villager ladders (section 2.8): `McaVillagerPathLaddersMixin`, `GroundPathNavigationClimbMixin`,
  `MoveControlVillagerLadderMixin`, `#creativeengineering:villager_ladders`; patcherMod 1.1.04 rebuilt into `mods/`.
  MT-44 to MT-46 untested, SQ-VL queued.
- 2026-10-30 (later): Quark monster box vaults (section 2.9, `mobs/tweaks #5`): `mobs.MonsterBoxVault`,
  `MonsterBoxBlockEntityMixin`, `CreateBreakerMonsterBoxMixin`, `[monster_box]`, reward table, obsidian hardness and
  tags, Quark `Enable Extra Loot Table` off. patcherMod 1.1.04 rebuilt into `mods/`. MT-34 to MT-37, MT-47 to
  MT-50 untested, SQ-MB queued.

## Owner ideas (scratchpad 2026-10-26)

- Villagers can't use the tall doors of Dramatic Doors; check the short doors too: `mobs/tweaks #15` (built
  2026-10-30, section 2.7).

- 2026-10-28: planned from the scratchpad: profile and trim Alex's Mobs behavior and tick cost (TODO `mobs/tweaks #17`). No code changed.
- 2026-10-29: seagulls (`mobs/tweaks #7`): owner reported gulls still stealing lunchboxes from inventories with the
  config list in place. Two patcherMod mixins now skip every Spice of Life food container, in the inventory and dropped
  (covers the new Netherite Lunchbox). MT-26 rewritten. [lunchboxes.md](lunchboxes.md).
