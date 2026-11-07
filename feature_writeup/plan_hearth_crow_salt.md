# Hearth and Harvest: crow and salt lick plan (2026-11-05)

Status: PLANNED, nothing built. **Owner answers 2026-11-07 (N24, section 10) close every open call in section 7: crop pecking on, pack corn as crow food, theft limited to one hotbar item and surviving without a nest, dressable scarecrows pinned, slower salt lick wear, salt caves added.** **Owner notes 2026-11-06 (N21) override this plan in three places: nests are dropped totally (P2, C6, C7, C9, section 5 are void), both crows need an in-game evaluation (P1 not final), and the scarecrow accepts any farmer's hat (R1).** See section 9.
stash, the crow feather, crop pecking and the wild and taming mechanics. This **supersedes the D2 default ("drop H&H's crow")
and the D13 default ("no licking")** in [hearth_and_harvest.md](hearth_and_harvest.md) section 5. Findings behind it:
5.3 (D13) and 5.4 (crow on Alex's Mobs). Source: `DecompiledMods/hearthandharvest-1.21.1-1.4.1-src` (MIT, port route allowed;
notice in the member's `NOTICE-upstream-MIT.txt`).

## 0. Decisions taken

| # | Decision | Answer |
|---|---|---|
| P1 | Which crow carries the mechanics | **Alex's Mobs' crow** (`alexsmobs:crow`) by default; H&H's entity not ported **until the eval: both crows get an in-game evaluation (owner 2026-11-06, N21)**, then one stays. Not final |
| P2 | Stash sink | ~~Port H&H's ground nest block~~ **Void (owner 2026-11-06, N21): drop nests totally.** No nest block, stash or nest worldgen |
| P3 | Home | Creative Delight member `development/creativeDelight/members/hearth` (`backportedHearth`), packages `hearth.crow`, `hearth.salt`, `hearth.nest`. Goals attach through `EntityJoinLevelEvent` like `FoxBerryGoals`; AM mixins, if any, go in `creativeDelight/.../mixin/alexsmobs/` |
| P4 | Config | Section `[hearth.crow]` and `[hearth.salt]` in `creativedelight-common.toml`, every feature switchable; copy to `configs_edited/root_config/` |
| P5 | Crow feather and Bad Omen brew | **Add both** (owner 2026-11-05): the `crow_feather` item and the Bad Omen potion brewed from it (C8). Reverses 5.4's "do not add the item" |
| P6 | Crop pecking | **Crows damage crops** (owner 2026-11-05): AM's crow only animates a peck today, so C5 adds the real age reset; default on, `mobGriefing` and the scarecrow protect |
| P7 | Trust system | **Deep dive first** (owner 2026-11-05): see section 8. C1 / C3 / C4 are not to be built until it is written |

Open owner calls are in section 7. Everything else below is the build.

## 1. Design notes that shape the work

- **No mixin needed for the crow state.** Trust and alarm live in a Forge capability attached to `alexsmobs:crow`
  (`AttachCapabilitiesEvent<Entity>`, saved to NBT). Hand-feeding uses `PlayerInteractEvent.EntityInteract`. AM's own thrown-seed taming
  stays and becomes the "dropped food" path of H&H (so it is not changed); trust only governs how close a wild crow lets a
  player come. This replaces the "mixin on `mobInteract`, default off" idea in 5.4 and removes the "changes AM taming" risk.
- **H&H's goals assume its own `CrowMoveControl`.** Rewrite movement on AM's navigation (`getNavigation()`, `getBlockInViewAway`) when porting;
  copy the logic from behaviour, not the call sites.
- **Goal clash.** AM's `CrowAIDepositChests` also fires when a crow holds an item. The steal goal gets a lower priority number than the AM
  deposit goal and sets a `StolenLoot` flag; the deposit goal must not take loot mid-theft (verify with the tests, mixin only if priority is not enough).
- **Perf: Minecraft's POI system, not a registry (2026-11-05 analysis, 1.19.2 code read).** H&H's lick goal reads a 29 x 29 x 7 box (5,887 blocks) per
  animal every 200 ticks (goals run every second tick) whether or not any salt exists. Our own position registry was rejected: it would miss pistons,
  Create contraptions, structures, `/fill` and The Salt's dissolving unless every path is hooked, and needs its own save and chunk-load scan.
  Vanilla's `PoiManager` is already updated on every block state change (`ServerLevel.onBlockStateChange`), saved with the world, and filled when a chunk
  section loads. One POI type covers the unwaxed lick blocks; the animal goal calls `PoiManager.findClosest` (a per-chunk-section lookup, about 50 times cheaper than the scan).
  Waxed blocks are simply not in the type, so waxing removes the block from the index and unwaxing adds it back.
  Catches: (a) one block state can belong to only one POI type, Forge throws at startup if another mod claims `salt:salt_block` (a boot check, S5); (b) salt blocks already placed
  in a section that holds other POI data are invisible until changed (one-time gap in old worlds, accepted).
- **No home area.** `Mob.restrictTo` is not used: salt licks are temporary, vanilla never clears a restriction after a leash, and it is not saved. The goal itself
  keeps animals near the block while one exists and lets go the moment it is gone or waxed.
- **Animals that lick are an allowlist,** not an exclusion list (H&H gives the goal to every `Animal`, which includes bees, parrots, axolotls and AM birds).
- **Standing spot, not the block.** The lick trip paths to an open walkable position next to the block, not into its solid block (avoids partial paths and retries).
- **The Salt's block is not ours.** `salt:salt_block` cannot take H&H's `waxed` property. Stage 0 stays `salt:salt_block`; licking, waxing and restoring on it go through
  `PlayerInteractEvent.RightClickBlock` and the dispenser behaviour; only the worn stages and the waxed variants are new blocks.

## 2. Salt lick

New blocks (7, our class `LickableSaltBlock`): `lightly_licked_salt_block`, `well_licked_salt_block`, `heavily_licked_salt_block`, and a waxed
form of each of the three plus `waxed_salt_block` (waxed stage 0). Same ids as upstream where they exist (textures and lang carry over).

| Step | Work |
|---|---|
| S1 | Blocks, items, loot (drops itself, waxed keeps `waxed`), lang, blockstates. Textures: a tool script (`tools/`) darkens and dents `salt:salt_block`'s texture into the three stages, so they match the pack's salt; the waxed forms reuse them |
| S2 | Player lick: right-click with both hands empty, lick sound, `salt.saltPlayerLickChance` (0.05) to advance one stage; last stage pops 1 `salt:salt` |
| S3 | Wax: honeycomb on any unwaxed lick block (including `salt:salt_block`) waxes it, an axe removes the wax; salt on a worn block restores one stage (by hand and by dispenser) |
| S4 | Animal lick goal `StayNearSaltGoal`, on the animals in the allowlist tag `#creativedelight:salt_lickers` (new, via `global.tags`: cow, sheep, goat, pig, horse, donkey, mule, llama, rabbit, and the AM grazers deer, bison, moose; owner trims). Every 200 ticks (config `salt.saltSearchInterval`) it asks `PoiManager.findClosest` for the nearest unwaxed lick block within `salt.saltAnimalRadius` + 2 (**20** blocks, was 12). While one is within the radius the animal is held there (pull-back path only when it is beyond 20); every `salt.saltLickInterval` (**1200 ticks average (N24-10)**, randomised 900 to 1500, was 500 and originally 9600; first lick staggered between 600 and 1800) it walks to a **standing spot** beside the block (an open walkable position within 1 block, picked with the entity's own path check, not the block position), licks, and the lick has a **1 in 8 chance** (`salt.saltAnimalStageChance` 0.125) to step the block one stage. A failed trip retries after 125 ticks. Skips leashed, riding, sitting and tamed animals; no `restrictTo` |
| S5 | POI type `creativedelight:salt_lick` (`PoiType` registered via `DeferredRegister`, `maxTickets` 0, search distance 1) whose block states are every unwaxed state of `salt:salt_block` and the three worn stages (waxed blocks are left out). Boot check: a clear log line if Forge reports a POI clash. Old worlds: nothing to migrate (see the one-time gap above). Optional later: an accessor limiting the search to the sections within +-3 blocks of height, only if profiling shows the search |
| S6 | JEI / WTHIT: a hint line on the block ("Animals lick this"); no new JEI category |
| S7 | Sound: `lick` from the H&H jar into the member's `sounds.json` |

Open check: The Salt's dissolving and melting rules apply to `salt:salt_block` only; the worn stages are left out of them (simplest). Confirm in testing that a worn stage next to water behaves.
Salt caves stay as decided in 5.3 (swapped for raw rock salt, a separate row, not part of this plan).

## 3. Scarecrow

| Step | Work |
|---|---|
| R1 | Port the H&H `scarecrow` block (MIT; model, texture, loot, recipe) into the member; **it accepts any farmer's hat (owner 2026-11-06): the four pack hats and the H&H hat through one tag, in the recipe and when dressed**; add it to `#alexsmobs:crow_fears` and `#creativedelight:repels_crows` with `global.tags` |
| R2 | `CrowAvoidRepellingBlocksGoal` on AM crows, reading `#creativedelight:repels_crows` (carved pumpkin, jack o'lantern, scarecrow) with `crow.scareRadius` (H&H's wider radius than AM's tag avoid) |
| R3 | Dressable scarecrow (`blocks/new #8`): kept as its own later row; its entity joins the same tag, so this block stays the plain version. No rename needed unless the two look alike |

## 4. Crow

All goals attach to `alexsmobs:crow` only while it is wild (`!isTame()`, not sitting, not a passenger), except where noted. Every item below is one config switch.

| Step | Work | Lines (est.) |
|---|---|---|
| C1 | **Capability** `CrowWariness` (alarm 120 ticks, trust 0 to 200, save to NBT) plus `AttachCapabilitiesEvent` | 90 |
| C2 | **Wild mechanics:** flee goal (players, villagers via `#creativedelight:scary_for_crow`, the alarm source), flock alarm (hurting a wild crow alarms wild crows within 16, squawk), alarm feeds the "five crows" toast only if a trigger replacement exists (advancements are dropped: no trigger) | 140 |
| C3 | **Trust and tempting:** `WaryTemptGoal` for a player holding crow food; comfort distance 4 to 1.5 blocks as trust fills (+1 per tick, +2 sneaking, -5 sprinting). Crow food and tempt items: new tag `#creativedelight:crow_food` = AM `crow_foodstuffs` seeds plus the pack's corn crop and corn kernels (N24-7, no longer waiting on a port) | 120 |
| C4 | **Taming paths on AM:** hand-feed crow food, 1 in 5 (event, wild only); eating dropped crow food heals 1 and 1 in 8 tames the thrower (`EatDroppedFoodGoal`); a stolen item picked up from a player's throw tames 1 in 3 and the item is returned (see C6). AM's thrown-seed taming is left as is | 130 |
| C5 | **Crop pecking:** `EatCropsGoal` (scan 8, peck 40 ticks, resets `age` to 0); needs `mobGriefing` and `crow.eatCrops` (default **on**, as the owner asked; flagged in section 7); protected by scarecrow range, `#alexsmobs:crow_fears`, and a crow-proof corn state when corn exists. Tag `#creativedelight:crow_edible_crops` built through `global.tags` from pack crops (wheat, stems, berries, FD cabbages and tomatoes, raspberry, trellis crops, peanuts, corn) | 100 |
| C6 | **Theft (nest sink void, N21; with no nest only the "fly away and drop" variant remains, decide after the crow eval):** `SeekShinyItemGoal`: scan 10, **N24-8: take 1 item (never more, even from a stack) from the player's hotbar only**, from `#creativedelight:crow_shiny_items` (gems, ingots, nuggets, raw ores, glowstone dust, ender pearl and eye; tools, armour and enchanted items are cut), carry it in the main hand and fly away from the nearest player; the loot survives without a nest (crow does not despawn while carrying, NBT-saved, dropped on death or after `crow.carryTicks`, item entity without despawn timer); full rules in [hearth_and_harvest.md](hearth_and_harvest.md) 5.8. Never while tame; `crow.stealShinyItems` switch | 170 |
| C7 | **Void (nests dropped, N21).** ~~Stash:~~ the nest block (section 5) is the sink; deposit marks it `CrowStash`. Wild-loot nests only count as a stash when marked | in section 5 |
| C8 | **Crow feather:** item `crow_feather` in `#forge:feathers`; AM crow loot swapped to it (0 to 1, looting) through a global loot modifier replacing the vanilla feather; arrows, brush and book and quill need no recipe (tag). Bad Omen potion (awkward + crow feather) added as a Forge `BrewingRecipeRegistry` entry; effect text and the Pungent and Tempting potions stay with N16 | 60 |
| C9 | **Spawning:** keep the pack's `flying_birds` rule; add `LivingSpawnEvent.CheckSpawn` ALLOW for crows near a generated nest or `crow.cropRequirement` crops (the H&H rule, additive so AM spawns do not drop); optional deny mode off | 70 |

## 5. Nest block (the stash sink) -- VOID, nests dropped totally (owner 2026-11-06, N21)

Kept below only as history; do not build.

Port of `NestBlock` (187 lines), `NestBlockEntity` (4 slots, `CrowStash` flag, insert, drops on break), loot table, model and blockstates; `GENERATED`
state marks world-generated nests. Worldgen: ground-nest placed feature plus a Forge `add_features` biome modifier into the `vegetal_decoration` step, in
`#creativedelight:has_crows` biomes (a new biome tag built from the pack's grass biomes where AM crows spawn, not H&H's tag). Raiding a stash: break the nest or open it with
an empty hand (H&H shows the contents); nests are not hopper-able. Tag `#creativedelight:nests` holds the nest (Incubation nests are left out: they have no inventory).

## 6. Build order, files, tests

Order: 1 tags and config (small) -> 2 salt lick S1 to S7 (independent of the crow) -> 3 scarecrow R1, R2 -> 4 nest block -> 5 crow C1 to C9 -> 6 docs and changelog.
Salt and scarecrow can ship first. Build and install: `./gradlew :creativeDelight:build --offline`, then `:creativeDelight:installToMods` with the game closed.
Logic tests in a javac harness for the wariness class and the lick goal's decisions; live checks are queued only (no scratch boot until the owner says so), rows `SQ-HSC-01 ...`.

| Area | Files |
|---|---|
| Member Java | `hearth/salt/*`, `hearth/crow/*` (goals, capability, loot modifier, brew), `hearth/nest/*`, `hearth/block/HHBlocks` (+ scarecrow), `hearth/item/HHItems` (+ crow feather) |
| Data and assets | blockstates, models, textures (generated), lang, sounds, loot tables, biome modifier, placed feature |
| Pack | `global.tags` lines (tags in section 4), `item_dict.js` / `global.item` ids (crow feather, new blocks), config, `configs_edited` copies |
| Docs | this file, [hearth_and_harvest.md](hearth_and_harvest.md) 5.2, 8, 9 (D2, D13 rows), [TODO.md](todo/TODO.md), `mod_sides.md` (no new jar: no row), `scratch_queue.md` |
| Changelog | `unreleased.md`, families `## Salt Lick`, `## Crows` (new content, so bullets are due); run `tools/changelog_lint.py` |

| ID | Test | Pass |
|---|---|---|
| HNH-48 | Right-click a Salt Block with empty hands 40 times | the block steps to lightly, well, heavily licked and then pops 1 salt; about 5 % of licks advance |
| HNH-49 | Honeycomb on a licked block, then an axe | waxed (no lick effect), then unwaxed; the waxed block dropped as an item keeps the wax |
| HNH-50 | Salt on a worn block, and a dispenser facing one | restores one stage each, uses 1 salt |
| HNH-51 | Five cows in a pen around an unwaxed salt block (default 1200-tick average interval, 1 in 8 stage chance, 20-block radius) | cows stay within 20 blocks, walk to a spot beside the block and lick about once a minute each; the block is gone in about 6 minutes (32 min for one cow); a waxed block is ignored; breaking the block frees them at once, with no leftover restriction (check `/data get entity` for no restriction) |
| HNH-52 | A tamed wolf, a leashed cow, a bee and a parrot near a salt block | none is pulled to it (not in `#creativedelight:salt_lickers`, or leashed or tamed) |
| HNH-53 | 200 licker animals in a loaded area, with one salt block and then none (perf) | tick time with the goal within noise of the baseline (spark / profiler); with no salt block the search finds nothing at once |
| HNH-54 | Place a scarecrow next to a field with a crow | the crow avoids the scarecrow's radius, the same as a jack o'lantern |
| HNH-55 | Craft or `/give` the scarecrow | exists, loot returns itself, JEI shows its recipe |
| HNH-56 | Wild AM crow near wheat, `crow.eatCrops` on and mobGriefing on, then off | crops reset to age 0 on, untouched off; the scarecrow protects the field |
| HNH-57 | Throw a diamond near a wild crow | it takes 1, carries it to a nest within 16, waits 1 s, deposits it; a nest gets the `CrowStash` mark |
| HNH-58 | Same with no nest nearby | the crow flies away from the player and drops the item |
| HNH-59 | A tame crow with a diamond on the ground | it does not steal (the AM fetch behaviour is unchanged) |
| HNH-60 | Break a stash nest | the items drop; open it with an empty hand shows the contents |
| HNH-61 | Hold crow food near a wild crow, walk, sneak, then sprint | the crow approaches, comfort distance shrinks as trust rises, sprinting loses trust |
| HNH-62 | Hand-feed a wild crow 20 times; throw food and let it eat; a thrown stolen item | tames about 1 in 5, 1 in 8, and 1 in 3; AM's thrown seed still tames |
| HNH-63 | Hurt a wild crow in a flock | wild crows within 16 flee the attacker for 6 s |
| HNH-64 | Kill an AM crow 40 times with Looting I | drops are crow feather only, 0 to 1 plus looting; the vanilla feather is gone from its table |
| HNH-65 | Brew awkward potion with a crow feather | Bad Omen potion; a vanilla feather does nothing |
| HNH-66 | New world: fly over a grass biome | ground nests generate in the crow biomes; a crow spawns near one even with the rule's other limits unchanged |
| HNH-67 | Reload the world with an alarmed crow and a half-trusting crow | state restored from NBT |
| HNH-68 | A stolen item with an item frame chest of the same item next to it | the crow does not drop the loot into the chest mid-theft (goal clash) |

## 7. Open calls for the owner (all answered 2026-11-07, N24: see section 10)

1. **Crop pecking default.** Planned on, with `mobGriefing` and the scarecrow as protection. Pack farms that were safe become raidable; say if it should default off.
2. **Crow food.** Corn kernels only exist once corn is ported (a later step). Until then crow food is AM's seeds; is that the wanted taming food?
3. **Stolen-item balance.** A wild crow can take dropped gems, ores, tools and armour near a base. Planned shiny list from H&H; tell me which entries to cut (tools and armour are the risky ones).
4. **Dressable scarecrow (R3)** stays separate; confirm it is not meant to replace the H&H block.
5. **Salt lick wear rate.** With the 500-tick interval every animal wears the block one stage about every 25 s, so a block with 5 cows is gone in about 2 minutes (3 stages
   then the pop) against about 40 minutes at H&H's 8 minutes. Confirm 500 is wanted, or tell me whether it is the rate per animal or per block.
6. **Salt caves** are not in this plan (decided as "yes, swapped" in 5.3); say if they should be added here.

## 8. History (see also section 9)

- 2026-11-05: plan written from the owner's direction; nothing built.
- 2026-11-05: salt lick design revised: vanilla POI system instead of a per-level registry, radius 20 and interval 500 ticks, allowlist of licking animals, no home area, standing-spot targeting (sections 1, 2 S4 and S5, tests HNH-51 to HNH-53). Still nothing built.

## 8. Trust deep dive on crows (note, to do)

Owner 2026-11-05: write a deep dive on the crow trust system before any of C1, C3, C4 is built. To cover, from H&H `CrowWariness`, `CrowWaryTemptGoal`,
`CrowFleeEntityGoal`, `CrowEntity.mobInteract` and the config (`Config.java` crow section), against AM's `EntityCrow`:

- Every number: alarm length and range, trust cap and gain / decay per tick, comfort and wary distances, thresholds for fleeing, which entities are scary (`scary_for_crow`), the tame odds on each path (feed 1 in 5, dropped food 1 in 8, thrown pickup 1 in 3).
- How trust interacts with AM: its thrown-seed taming (`seedThrowerID`), `crow_foodstuffs`, scatter and pumpkin fears, and what a tame crow does with trust (nothing in H&H: trust is wild only).
- What a player sees (approach distance, feedback, particles), what is saved to NBT, multiplayer (per player trust or per crow), and the cost of the capability.
- Balance: taming time, interaction with crop pecking (P6), scarecrow range, hostile or neutral play styles; options (trust only changes approach distance; trust gates taming; off by switch).
- A recommendation with a test list, replacing the C1 / C3 / C4 rows here.

## 9. Owner notes 2026-11-06 (N21)

- **Nests dropped totally:** no nest block or entity, loot table, worldgen, `#creativedelight:nests` tag or `hearth.nest` package. Voids P2, C7, section 5, the nest part of C6 and C9, and tests HNH-57 (deposit), HNH-58 (reframe: only the fly-away case), HNH-60, HNH-66 (nest part) and HNH-68. Open: whether theft survives without a stash (decide after the crow eval).
- **Both crows need an in-game eval** (AM's with the mechanics and H&H's) before P1 is final.
- **Scarecrow accepts any farmer's hat** (R1).
- Hats: the Farmer's Hat effect stays on the four pack hats and the H&H hat; main row in [hearth_and_harvest.md](hearth_and_harvest.md) (N21).
- Corn kernels as crow food (C3) stay in, corn husk is now implemented.
- 2026-11-06: recorded; nothing built.

## 10. Owner answers 2026-11-07 (N24)

All six section 7 calls are answered; the details are in [hearth_and_harvest.md](hearth_and_harvest.md) 5.8.

1. **Crop pecking default: on.**
2. **Crow food: the pack's existing corn and corn kernels** (plus AM seeds); used for taming and breeding food.
3. **Stolen items:** one item, from the hotbar only; list generated by tag (tools, armour, enchanted items cut); the item survives without a nest. **Theft stays in** (answers the N21 open question).
4. **Dressable scarecrows (R3): pinned.** A later eval decides whether H&H's scarecrow can be dressed.
5. **Salt lick wear:** cow licks once a minute on average, 1 in 8 licks wears a stage.
6. **Salt caves: added**, then examined in game for a further eval (HNH-87 to HNH-89).

New tests: HNH-87 theft takes exactly 1 item from a stack, from the hotbar only (a main-inventory item and a ground item are never taken); HNH-88 a crow carrying loot does not despawn, and the loot is in its NBT and drops on its death; HNH-89 a salt cave generates, its shell is raw rock salt block, and the cave is logged for eval; HNH-90 corn and corn kernels tempt and tame a wild crow; HNH-91 a pecking-on default: an unprotected crop is pecked, a scarecrow-covered one is not.
