# Cozy's Improved Cats and Domestication Innovation: overlap and integration report

Investigation written 2026-10-30. **Built 2026-11-01 (section 10): owner's cat changes (no gender, no ancient fish,
vanilla fish taming, Cozy owns cat modes, Feather on a Stick lure and hook); not run in game.** The rest of the
fold-in (section 4) is still planned. Cozy's Improved Cats 2.1.0 (`cozys_improved_cats`, registry
namespace `cozys-improved-cats`, author Cozy) is a native Forge 1.19.2 build, put in `mods/` on 2026-10-30
(`cozys-improved-cats-2.1.0-forge-1.19.2.jar`); the game has not booted with it yet. Domestication Innovation 1.6.1
(`domesticationinnovation`, Alex's) has been in the pack since 2024 and had no report; it is covered here because both
mods own pet cats and their systems collide. No port is needed for either. "Cozy" in this file is Cozy's Improved
Cats, "DI" is Domestication Innovation. Test prefix `CIC-`.

**Scope rule:** integration adds no recipe or mechanic that is not in Cozy, DI or defined by a pack rule, unless the
owner asks for it by name. Upstream bugs are fixed (3.6).

**Overlap marker:** every item or system that duplicates or closely mirrors something in the pack carries
`*overlaps w/ <what>*` in the tables.

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Cozy inventory (11 items incl. 2 block items, 2 blocks with block entities, 1 entity, 2 menus, 2 recipe serializers, 1 fishing loot modifier, 1 biome modifier, 45 recipe files, 6 tags, 72 advancement files / 24 criteria, 6 commands, 22 + 2 mixins) | section 3 | reference, from the jar and decompiled code |
| DI inventory (25 items, 18 blocks, 37 enchantments, 1 villager profession, 5 village pet shops, 36 recipes) | 1.2 | already folded in (item_dict, loot, compost, tooltips, BlockRunner, names, stack sizes) |
| Upstream bugs | 3.6 | 4 in Cozy (B1 to B4); none in DI (no `latest.log` errors, all 36 recipes load) |
| Overlap list | section 2 | the main conflicts are pet revival (DI pet bed vs Cozy collar) and pet commands (DI three-mode vs Cozy four-mode); three pet-bed sets |
| Fold-in list in build order | section 4 | planned, 17 steps |
| Decisions for the owner | section 5 | D1 to D10; D2, D5, D9 settled by the owner 2026-11-01 |
| Owner's cat changes | section 10 | built 2026-11-01 (Creative Compat `cats/`, Creative Delight merge, config); not run |
| Tests | sections 8, 10.6 | CIC-01 to CIC-36 drafted, none run; scratch SQ-CAT, client CQ-CAT queued |

- **Jars:** `mods/cozys-improved-cats-2.1.0-forge-1.19.2.jar` (jar-in-jar MixinExtras 0.5.4),
  `mods/domesticationinnovation-1.6.1-1.19.2.jar` (needs Citadel, installed). Both unpacked and decompiled under
  `DecompiledMods/<jar name>/` (`src/`: Cozy 215 classes, DI 118).
- **Licences:** Cozy LGPL-3.0, DI LGPL-3.0. Both GPL-compatible, so a fork is allowed, but nothing here needs one:
  every fix is a patcherMod mixin, config or KubeJS.
- **Pack references:** DI is mapped in `item_dict.js` (25 keys), `loot_config.json` / `loot_index.json` (pet shop
  chest), `creativeDelight/.../domesticationinnovation/compost_values.js`, `client_scripts/tooltips.js` (pet bed
  line), `tags/blocktags.js` (BlockRunner soft list), `jei/order.js`; config tuned and backed up
  (`configs_edited/root_config/domestication-innovation.toml`, identical to the live file: 20 of 37 enchantments off,
  pet shop village weight 17). **Nothing references Cozy**, and it has no `mod_sides.md` row yet.
- **Dumps:** `kubejs/exported/` was refreshed 2026-10-30 00:42, before the Cozy jar (21:51), so `/dumpall` is step 0.
- **Already live once the game boots:** every vanilla `minecraft:cat` that starts being tracked becomes a Cozy cat
  (2.3), the fishing loot modifier and cod / salmon drop pools (3.4), and new cat spawns in the crimson forest and the
  outer End.

## 1. Overview

### 1.1 Cozy's Improved Cats

Cozy turns cats into companions with stats. Its entity `cozys-improved-cats:improved_cat` extends vanilla `Cat`.
Spawns are not replaced: a vanilla cat (exact class `Cat`) is swapped for an improved cat when it starts being tracked
(`event/CatReplacementHandler.java:33`). Tamed cats keep their owner, collar colour, variant, name, age, equipment,
ForgeData and caps (best effort), and are given a class, a gender and a favourite food. Cats then have affinity and bond
levels (1 to 5), 20 to 35 health, hunger, genetics on breeding, a home (Cat Bed) and roaming radius, four modes
(follow / stay / wander / guard), fetching of a Yarn Ball, and one of five classes with an owner ability (Lucky: Luck;
Guardian: Resistance near creepers and phantoms; Naiad: Regeneration; Witchling: random gifts; Dreamer: XP bottles).

Taming takes **Raw Ancient Cod or Salmon** only (1 in 2), not vanilla fish (`allowVanillaTaming=false`). Ancient fish
come from fishing (5% of cod / salmon catches swapped), from killing cod and salmon (50% each) and from level-3
fisherman trades (3 emeralds). When a tamed cat dies, a **Cat Collar** holding its data goes to the owner; using it
revives the cat. Nether-origin cats are fire immune, End cats give slow falling and stop endermen noticing the owner.

### 1.2 Domestication Innovation (installed)

Enchantable Collar Tags for any tamed mob (37 enchantments, 20 of them off in the pack config), three-way pet commands
(follow / wander / stay) on right-click, pet beds as respawn points (a pet that slept in a bed respawns there at
dawn), Wayward Lantern, Command Drum, Feather on a Stick, Deed of Ownership, Rotten Apple, Sinister Carrot, the Animal
Tamer villager and 5 village pet shops. It makes axolotls, horses, foxes, rabbits and frogs tameable.

### 1.3 Facts

| | Cozy | DI |
|---|---|---|
| Mod id / namespace | `cozys_improved_cats` / `cozys-improved-cats` (Forge logs "dangerous alternative prefix") | `domesticationinnovation` |
| Items | 11 (4 fish, collar, training string, treat, yarn ball, guide, 2 block items) | 25 |
| Blocks | 2: `cat_bed` (colour in the item / block entity, 16 colours), `cat_food_bowl` (3 slots) | 18 (16 pet beds, drum, wayward lantern) |
| Entities | `improved_cat` (replaces vanilla cats at tracking) | 2 technical (`giant_bubble`, `highlighted_block`) + projectiles |
| Effects / potions | none (uses vanilla Luck, Regeneration, Resistance, Slow Falling) | none (37 enchantments) |
| Recipes | 45 files: 32 bed (white + 15 colours, 16 recolours), 8 smelting / blasting (4 per fish), 5 crafting | 36 crafting |
| Loot | GLM `ancient_fish_fishing`; cod / salmon entity pools by `LootTableLoadEvent`; no tables replaced | 1 chest (`chests/petshop_chest`, rolled by code), book pools by code |
| Worldgen | biome modifier `dimensional_cats` (crimson forest; End highlands / midlands / barrens) | 5 village pet shops (pool weight 17) |
| Trades | fisherman level 3: ancient cod, ancient salmon | Animal Tamer profession |
| Tags | items `cat_food`, `protected_dropped_food`; blocks `end_cat_support`, `nether_cat_support`; biomes x2 | 7 entity-type, 2 item |
| Advancements | 72 files (26 + 46 recipe unlocks), 24 criteria. The pack drops all advancements; no Cozy unlock reads advancement progress | none |
| Lang | 787 keys | in pack names dump |
| Config | `config/cozys_improved_cats-common.toml` (own TOML, not ForgeConfigSpec, read on both sides, no sync) | `domestication-innovation.toml` (backed up) |
| Commands | `/cozycats mode|home unlink|droppedfood`, `/cozycats uninstall ...` (op 2), `/reloadcozycatsconfig` (op 2) | none |

## 2. Overlap (first)

### 2.1 Items

| Item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Cat Bed (`cat_bed`, 16 colours) *overlaps w/ DI Pet Bed x16, Redeco Pet Bed x16* | DI beds: pet respawn point; Redeco beds: decor | three bed sets, different jobs; Cozy's bed is a cat home and (if `bedRevivalEnabled`) a revival point | keep all three (no merge: different functions); same treatment as DI beds (BlockRunner soft list, tooltip line); D3 |
| Cat Collar (`cat_collar`) *overlaps w/ DI Collar Tag (name)* | Collar Tag holds enchantments; Cat Collar holds a dead cat | similar names, unrelated jobs | keep both; tooltips make the job plain (step 12) |
| Raw / Cooked Ancient Cod and Salmon *overlaps w/ cod, salmon* | vanilla fish, `forge:raw_fishes/cod`, `/salmon` | same food under another id, but the raw ones are the only taming item | no merge (D5); cooked ones join the pack food tags as fish |
| Cat Treat | none (no cat item in the name dump) | not a food: breeding / bond item | keep |
| Cat Food Bowl | none (no pet bowl in the pack) | 3 iron ingots in a row: possible recipe clash | check the crafting audit after `/dumpall` (step 4) |
| Yarn Ball, Training String, Cat Care Guide | none | Yarn Ball `S S / / S S` string: check clash | crafting audit |

The name dump has no other cat-care items. Cat cosmetics (Simple Hats cat hats, Botania cat ears) and the Villagers
Sell Animals Kitten Crate are unrelated items.

### 2.2 Systems

| System | Cozy | Pack / DI | Problem | Proposal |
|---|---|---|---|---|
| **Pet revival** *overlaps w/ DI pet beds* | death marks the cat (`NativeAcceptedDeathMixin`), drops a Cat Collar that revives it | DI `onLivingDie` (`CommonProxy.java:922`) does not cancel the death: if the cat slept in a DI pet bed it files a respawn request | a cat with a DI bed comes back twice (DI respawn at dawn + Cozy collar) | D1: patcherMod skips DI's respawn request for `improved_cat`; DI beds keep working for every other pet |
| **Pet commands** *overlaps w/ DI three-mode commands* | modes follow / stay / wander / guard (GUI, `/cozycats mode`); own interactions run first (`ImprovedCatEntity.java:410`) | empty-hand right-click falls through to vanilla `Cat.mobInteract`, where DI's `CatMixin` swaps sit for its three-mode cycle (`trinary_command_system=true`) | two mode systems on one cat, each with its own saved state | D2: Cozy owns cat modes: patcherMod cancels DI's `CatMixin` interact for `improved_cat`; DI keeps dogs, parrots, foxes and the rest |
| Follow / teleport | `FollowOwnerTeleportMixin` cancels `FollowOwnerGoal.maybeTeleportTo` while fetching | DI `FollowOwnerGoalMixin` (`canUse`, `canContinueToUse`, `canTeleportTo`) for wander / stay; Waystones Teleport Pets | different methods, no mixin clash; DI wander / stay would still apply if D2 keeps DI commands | test (CIC-10) |
| Collar enchantments | none | DI collar tags apply to any tamed mob, so to `improved_cat` | wanted: DI enchants (health boost, fireproof, ...) stack with Cozy stats; Cozy's own health 20 to 35 plus DI Extra Health | keep; test (CIC-11) |
| Baby lock | `AgeableMobGrowthLockMixin`: every `AgeableMob`, `AgeLocked` NBT, `@ModifyArg` x2 on `aiStep` setAge | DI `AgeableMobMixin` `isBaby` HEAD (Curse of Immaturity); Baby Mobs backport | different targets, no clash | none |
| Targeting | `Mob.setTarget` HEAD, `HurtByTargetGoal.start` / `TargetGoal.canContinueToUse` wraps | DI `MobMixin` (`tick`, `pickUpItem`, `playAmbientSound`), `TameableAnimalMixin.isAlliedTo` | no shared target | none |
| Tempt | `TemptGoal.shouldFollow` `@Redirect` on `Ingredient.test` | patcherMod `TemptGoalTroughRetryMixin` adds an interface only | redirect is exclusive, but no other redirect there | none |
| Cat food | tag `cozys-improved-cats:cat_food`: 7 vanilla raw meats + 2 ancient fish | pack raw foods: `creativeengineering:food_type/raw/fish` (40), `/raw/meat` (42) | cats refuse every modded raw fish and meat | D4: add the two pack tags via `global.tags` |
| Taming of cats | ancient fish only | DI pet shop cages and Villagers Sell Animals crates hold vanilla cats | those cats are converted, untamed; fine, but a bought kitten needs ancient fish | JEI info (step 13); D5 |
| Fishing loot | GLM swaps 5% of cod / salmon catches | pack fishing net tables (`kubejs/data/creativeengineering/loot_tables/gameplay/fishing_net`), Oceanic nets | GLM runs on `gameplay/fishing` only; net catches unchanged | none |
| Mob drops | cod / salmon pools at 50% (code, `LootTableLoadEvent`) | [loot.md](loot.md) code-pool census | one ancient fish per two fish killed is a lot next to 5% fishing | D6 |
| Spawns | new `improved_cat` spawns: crimson forest; End highlands / midlands / barrens (weight 1, 1-2, then 5% acceptance) | [mob_spawns.md](mob_spawns.md) rules list | not in the spawn report until `/dumpall` | D7; record in mob_spawns.md |
| Cat variants | copies the vanilla variant | More Mob Variants cat textures; Carry Cats ships Cozy support (`CozyCatsVisualTest`, texture migration) | whether More Mob Variants' variant survives conversion is unchecked | test (CIC-04) |
| Cat Loaf, Carry Cats | `ImprovedCatEntity extends Cat` | both check `instanceof Cat` / TamableAnimal | should work; a cat put down by Carry Cats is converted again on tracking | test (CIC-12, 13) |
| Baby Mobs remodels | own renderer | Baby Mobs D5 (not built) lists cat / ocelot | a future baby cat model would also need Cozy's renderer | note in baby_mobs.md when D5 is built |

### 2.3 Overlap inside the batch

Cozy and DI are the batch. Beyond 2.2: DI's "Allows pets to respawn if killed" pet-bed tooltip
(`tooltips.js:572`) becomes untrue for cats under D1, so the line names the exception.

## 3. New content (Cozy)

### 3.1 Items and blocks

| Id (`cozys-improved-cats:`) | Name | Values | Stack |
|---|---|---|---|
| `ancient_cod`, `ancient_salmon` | Raw Ancient Cod / Salmon | 2 / 0.1; taming item | 64 |
| `ancient_cooked_cod`, `ancient_cooked_salmon` | Cooked Ancient Cod / Salmon | 5 / 0.6 (as vanilla cooked cod), one random roll on eating: Luck 10 s, Regeneration 5 s, Resistance 5 s or 5-9 XP (`AncientFishEffect.java`, `cookedAncientFishEffects`) | 64 |
| `cat_treat` | Cat Treat | not food; breeding / bond (`CatBreeding.treat`) | 64 |
| `yarn_ball` | Yarn Ball | fetch toy | 1 |
| `training_string` | Training String | trains a tamed cat; links a bed; 99 uses (config) | 1 |
| `cat_collar` | Cat Collar | holds a dead cat; use to revive; tooltip lists name, owner, class, deaths, stats | 1 |
| `cat_guidebook` | Cat Care Guide | opens a vanilla book screen | 1 |
| `cat_bed` (block) | Cat Bed | home; GUI; 16 colours by recipe; strength 1.5, wood | 64 |
| `cat_food_bowl` (block) | Cat Food Bowl | 3-slot feeder; strength 2.0, metal | 64 |

Food values are read from the config when items register, so changes need a restart.

### 3.2 Recipes

Cat Treat (wheat + sugar, shapeless, x2), Cat Food Bowl (3 iron ingots), Yarn Ball (4 string), Training String
(ancient cod + ancient salmon + string), Cat Care Guide (book + string), Cat Bed (2 wool + 2 planks, colour by wool;
recolour by dye), smelting and blasting of both ancient fish (1.0 XP). Literal inputs: `minecraft:iron_ingot`,
`minecraft:string`, `minecraft:wheat`, `minecraft:sugar`, `minecraft:book`, `minecraft:*_wool`.

### 3.3 Mechanics outside items

Listed in 1.1. Plus: owner friendly fire blocked; fall damage off (`enableFallDamage=false`); accidental-attack
recovery (shift + wheat); dropped-food protection tag; the Witchling's gifts are hardcoded
(`WitchlingGiftManager.java`). An offline uninstall tool (`/cozycats uninstall`, JVM `-Dcozycats.uninstall.offline.*`)
converts cats back to vanilla and refuses to open a world while its marker file exists (it can `System.exit(2)`).

### 3.4 Loot and trades

Fishing GLM `ancient_fish_fishing` (5% each); cod / salmon entity pools at 50% each (code); fisherman level 3,
3 emeralds, 10 uses each.

### 3.5 Config

`config/cozys_improved_cats-common.toml` (appears after the first boot), sections trading, catStats, behavior,
hunger, genetics, homes, dimensionalCats, toggles, misc, ancientFish, classes, notifs, debug, genderClassChances,
`configVersion=5`. Defaults that matter here: `reviveEnabled=true`, `bedRevivalEnabled=false`,
`allowVanillaTaming=false`, `tameChance=2`, ancient fish fishing 0.05 / drop 0.5, dimensional spawning on at 0.05,
`maxRoamingRadius=32`. It is read on both sides with no sync, so clients need the same file as the server (step 16).

### 3.6 Upstream bugs

| # | Bug | Evidence | Fix |
|---|---|---|---|
| B1 | `NativePetChunkMigration` throws `IllegalArgumentException` inside `EntityStorage.upgradeChunkTag` when an entity chunk's DataVersion is missing or does not match | code (`restoration/NativePetChunkMigration.java`); not seen yet (no boot) | watch the first boot log; patcherMod catch-and-skip if it fires |
| B2 | Dreamer with a full inventory drops 1 XP bottle, not `bottleCount` | `classes/custom/DreamerHandler.java` ~37 | patcherMod: drop the whole count |
| B3 | Witchling gift loot table `gameplay/witchling_gift` declared, never used, not in the jar | code | none (dead constant) |
| B4 | registry under `cozys-improved-cats` while the mod id is `cozys_improved_cats` | Forge "dangerous alternative prefix" warnings expected | none (cosmetic); scripts use the hyphen namespace |

## 4. Fold-in, in build order

0. **Dumps and generators** (memory `dumpall-refresh-workflow`): boot once, `/dumpall`, then the `/cegen` generators.
   Read the first boot log for B1 and the prefix warnings. Add the `mod_sides.md` row (both sides; skill `add-mod`).
1. **Ids:** 11 keys in `item_dict.js` (new `// ===== cozys-improved-cats` block: `ancient_cod`, `ancient_salmon`,
   `ancient_cooked_cod`, `ancient_cooked_salmon`, `cat_treat`, `yarn_ball`, `training_string`, `cat_collar`,
   `cat_guidebook`, `cat_bed`, `cat_food_bowl`; check each key against existing ones) and the block dict.
2. **Tags** via `global.tags`: raw ancient fish into `creativeengineering:food_type/raw/fish` and
   `forge:raw_fishes`; cooked into `forge:cooked_fishes` and the cooked food type; `cat_food` gains the pack raw fish /
   meat tags (D4); `cat_bed` into the bed group where DI's beds sit.
3. **Upstream recipe removals:** none. Literal inputs to tags: wool, planks, string, iron ingot through NCH if the
   recipes are rebuilt (only if the crafting audit shows a clash).
4. **Recipes** by pack rules through `global.NCH` ([recipes_cooking.md](recipes_cooking.md)): the two ancient fish
   join the pack's fish cooking routes as cod / salmon do (smoker, campfire, Create fan if those are pack rules for
   fish). Crafting audit for Cat Food Bowl and Yarn Ball clashes. Food code under
   `development/creativeDelight/src/main/resources/kubejs_startup/cozys-improved-cats/` (memory
   `creative-delight-routing`).
5. **Mob loot** ([loot.md](loot.md)): record the GLM and the two code pools in the census; D6 drop rate (config
   `ancientFish` drop chance, no script).
6. **Item merges:** none (D5).
7. **Potions:** none. **Effects:** none new; JEED unaffected.
8. **Food types and values** (`food_types.json`, [food.md](food.md)): raw fish 2 / 0.1 and cooked 5 / 0.6 against the
   pack's cod rows; the random effect stays code (as AncientFishEffect). Then `tools/food_families.py --check`.
9. **Stack sizes** ([stack_sizes.md](stack_sizes.md)): fish by the fish policy; collar, guide, yarn, string stay 1.
10. **Cans and jars** ([cans.md](cans.md)): cooked ancient fish by the canned-fish rule (D8).
11. **Compost:** by the pack's raw-fish rule; none if raw fish are not composted. Crops, wild crops, seasons: none.
12. **Pack mixins (patcherMod):** D1 skip DI's respawn request for `improved_cat`; D2 cancel DI's cat interact on
    `improved_cat`; B2. Headless harness first.
13. **Names and lang** ([names.md](names.md)): none needed (names are clean). Tooltips
    ([tooltips.md](tooltips.md)): DI pet-bed line names the cat exception (D1); Cat Bed line. JEI info
    ([jei_coverage.md](jei_coverage.md)): taming with ancient fish, revival by collar, the three fish sources.
14. **Worldgen / spawns** ([mob_spawns.md](mob_spawns.md)): D7; record Cozy's biome modifier in the rules section.
15. **Mob and block audit:** Cat Bed into the BlockRunner soft list (`blocktags.js:360`, beside DI beds); mineable
    tags (bed: axe, bowl: pickaxe; check the jar's); Item Physic classes for the 11 items; WTHIT for the bed and bowl.
16. **Configs:** set the D-values in `cozys_improved_cats-common.toml`, back it up to
    `configs_edited/root_config/`; ship it to clients (no sync). DI config: D1 / D2 need no DI change.
17. **Release:** changelog family `## Cats`, TODO row, plan row.

Ordering: step 0 before 1, 2, 8 to 11 and 15 (they need the dumps); 2 before 8 and 10; 12 before tests CIC-06 to
CIC-10; 16 before any player-side test.

## 5. Decisions for the owner

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | Which revival system for cats? | Cozy's collar for cats (it keeps stats); DI pet beds for every other pet; patcherMod skips DI's respawn request for `improved_cat` | as recommended |
| D2 | Which command system for cats? | Cozy's four modes; DI's three-mode right-click cancelled on Cozy cats only | **built 2026-11-01** (10.3) |
| D3 | Three pet-bed sets (DI, Redeco, Cozy) | keep all three: DI = respawn, Cozy = cat home, Redeco = decor | keep |
| D4 | Cats eat only vanilla raw meat and fish | add `food_type/raw/fish` and `/raw/meat` to `cat_food` | add |
| D5 | Ancient fish as a cod / salmon variant | separate items (taming item); raw and cooked join the fish tags so recipes accept them | **owner 2026-11-01: removed, merged into vanilla cod / salmon** (10.2); D6, D8 moot |
| D6 | 50% ancient fish from every cod / salmon kill | lower to 0.1 (fishing stays 0.05) | 0.1 |
| D7 | Cat spawns in the crimson forest and the outer End | keep (they give the Nether / End cat lines) | keep |
| D8 | Cooked ancient fish in cans | treated as cod / salmon in cans (the random effect is lost when canned) | as cod / salmon |
| D9 | `allowVanillaTaming` | keep off (ancient fish is Cozy's design) | **owner 2026-11-01: on** (10.2) |
| D10 | Bed revival (`bedRevivalEnabled`, revival at the Cat Bed when the collar is destroyed) | turn on: a lost collar should not end a cat | on |

## 6. File map (planned)

| File | Change |
|---|---|
| `kubejs/startup_scripts/_lib/item_dict.js`, block dict | step 1 |
| `kubejs/server_scripts/tags/*.js` (through `global.tags`), `blocktags.js` | steps 2, 15 |
| `development/creativeDelight/src/main/resources/kubejs_startup/cozys-improved-cats/` (recipes, compost) | steps 4, 11 |
| `kubejs/generation_scripts/food/food_types.json`, `tools/food_families.py`, `tools/gen_stack_sizes.py`, `gen_can_types.py` | steps 8 to 10 |
| `development/patcherMod`: DI respawn skip, DI cat interact cancel, Dreamer bottles (plus B1 guard if needed) | step 12 |
| `kubejs/client_scripts/tooltips.js`, `jei_info.js` | step 13 |
| `config/cozys_improved_cats-common.toml` + `configs_edited/root_config/` copy | step 16 |
| `mod_sides.md`; docs: this file, loot.md, mob_spawns.md, food.md, stack_sizes.md, cans.md, TODO, README, changelog | each step |

## 7. Offline checks (once built)

- First boot log: no `Failed to parse` naming `cozys-improved-cats`; no B1 exception; prefix warnings only.
- `/dumpall`, then `tools/food_families.py --check` (no unsorted ancient fish) and the crafting audit
  (`kubejs/exported/recipes/crafting_audit.json`) for bowl / yarn clashes.
- Tag dump: `cozys-improved-cats:cat_food` holds the pack raw fish / meat (D4).
- patcherMod headless harness for the D1 / D2 mixins (memory `patchermod-headless-smoke-test`).
- `python3 tools/changelog_lint.py`.

## 8. In-game test suite (drafted)

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-CAT-01, -02, -03, -04, -05.

| ID | Test | Pass |
|---|---|---|
| CIC-01 | Boot with an existing world | no Cozy parse errors; no B1 exception; world opens |
| CIC-02 | Walk to a village with cats | each cat is a Cozy cat (Jade / WTHIT shows the Cozy entity) |
| CIC-03 | Load a tamed vanilla cat from before the install | still owned, same name, collar colour and variant; sitting |
| CIC-04 | Cat with a More Mob Variants texture | same texture after conversion |
| CIC-05 | Tame a stray with vanilla cod, then Raw Ancient Cod | cod refused; ancient cod tames (about 1 in 2) |
| CIC-06 | Tamed cat slept in a DI pet bed, then killed | Cat Collar given; no DI respawn at dawn (D1) |
| CIC-07 | Tamed wolf slept in a DI pet bed, then killed | respawns at the bed at dawn |
| CIC-08 | Use the Cat Collar | cat revived with its stats |
| CIC-09 | Destroy a collar (lava) with a linked Cat Bed | cat respawns at the bed (D10) |
| CIC-10 | Empty-hand right-click a tamed Cozy cat | Cozy behaviour only, no DI "following / wandering / staying" message (D2); a wolf still cycles DI modes |
| CIC-11 | DI collar tag with Extra Health on a Cozy cat | health above the Cozy stat; tag kept on revival |
| CIC-12 | Carry Cats: pick up and put down a Cozy cat | rides the shoulder with its texture; same cat after |
| CIC-13 | Cat Loaf with a Cozy cat | loaf pose works |
| CIC-14 | Feed a Cozy cat a modded raw fish (Aquaculture or pack fish) | accepted (D4) |
| CIC-15 | Fish 100 times | ancient fish about 5% of cod / salmon catches |
| CIC-16 | Kill 20 cod | ancient cod about 2 (D6 at 0.1) |
| CIC-17 | Fisherman level 3 | ancient cod and salmon trades |
| CIC-18 | Eat Cooked Ancient Cod 5 times | one of Luck, Regeneration, Resistance or XP each time; 5 hunger |
| CIC-19 | JEI on Raw Ancient Cod | info page: taming, sources |
| CIC-20 | Cat Bed and Cat Food Bowl | break by the right tool, drop themselves; BlockRunner soft |
| CIC-21 | Crimson forest and End highlands | Nether / End cats appear (rare) |
| CIC-22 | End cat following, look at an enderman | enderman stays calm |
| CIC-23 | Dedicated server with a changed config value on the server only | client matches (configs shipped, step 16) |
| CIC-24 | DI pet shop and Villagers Sell Animals kitten crate | cats converted, untamed, tamable with ancient fish |

## 9. History

- 2026-10-30: investigation written (Cozy jar and decompiled code, DI code and the pack's DI fold-in, overlap search
  against the 2026-10-20 name dump), B1 to B4, D1 to D10. Nothing built.
- 2026-11-01: owner's changes built (section 10): gender off, ancient fish merged into vanilla fish and their sources
  off, vanilla fish tame, D2 (Cozy owns cat modes), feather lure on Cozy cats in Wander / Guard, feather hooks mobs for
  cats to attack with buffs (`[cats]`). Pickup / petting item skipped by the owner. Not run.

## 10. Owner's cat changes (built 2026-11-01)

Asked by the owner 2026-11-01 (scratchpad "improved cats"). Jars `creativecompat-1.0.0.jar` and
`creativedelight-1.0.0.jar` built and installed 2026-11-01 15:58; not run. The owner skipped the petting / shoulder
pickup item: in Carry Cats 2.5 pickup is the `B` key while looking at the cat and Cozy's petting is sneak + empty-hand
use, so they share no input (note: `B` is also Portable Wardrobe "quick swap 5" in the owner's `options.txt`, and
Carry Cats refuses a sitting cat).

### 10.1 No gender

Config `[toggles] genderSystemEnabled = false`: breeding needs no opposite pair, names are gold, class rolls use the
neutral table. Cozy still prints "Gender: Unknown" in the stats chat and the Cat Collar tooltip:
`mixin/CozyMessageGenderMixin` (MessageUtil.send) and `cats/CozyCats.onTooltip` (lowest priority) drop that line.
Three guide pages that mention genders are rewritten in Creative Compat `assets/creativecompat/kubejs/names.js`.
Breeding stays (owner).

### 10.2 No ancient fish

- Item merge (Creative Delight group, [item_merges.md](item_merges.md) 2.12): Raw / Cooked Ancient Cod and Salmon ->
  Cod, Salmon, Cooked Cod, Cooked Salmon (`kubejs_startup/cozys_improved_cats/item_merges.js`); hidden in JEI, tags
  stripped, recipes, loot and inventories swapped. The Training String recipe becomes cod + salmon + string. The 8
  smelting / blasting recipes are removed (CD `data/creativedelight/kubejs/cozys_improved_cats/item_merges.js`).
- Config: `allowVanillaTaming = true` (raw cod and salmon tame, 1 in `tameChance` = 2), `fishingEnabled = false`,
  fishing and drop chances 0.0. Trade uses stay 10: Cozy's validator refuses 0.
- Fisherman trades: no Cozy switch, so `CozyCats.onTrades` (VillagerTradesEvent, lowest priority) removes the
  listings declared in `ModVillagerTrades`.
- Guide pages on taming, ancient fish, cooked fish, kittens, foods and the Training String rewritten (names.js).
- Config backed up to `configs_edited/root_config/cozys_improved_cats-common.toml`.

### 10.3 Cozy owns cat modes (D2), guard

DI's `CatMixin` replaces the owner's sit toggle in `Cat.mobInteract` with its follow / wander / stay cycle. A DI
"stay" sits the cat, which turns Cozy's home goal off (Guard and Wander stop), and DI "wander" fights Cozy's Follow.
`mixin/CatCozyCommandMixin` injects at the same call (first `setOrderedToSit`, m_21839_) with priority 900, so it is
applied before DI's (1000) and runs first: on a Cozy cat it does the vanilla toggle and returns vanilla's result;
other cats keep DI's cycle. A DI command already saved on a Cozy cat is reset to 0 when the cat joins a level
(`CozyCats.clearDiCommand`). Guard itself is Cozy's (Cat Bed GUI: patrol the home radius, warn the owner, glow the
first monster inside); nothing is added to it.

### 10.4 Feather on a Stick lure on Cozy cats

DI's feather lands, picks the nearest ambulatory pet (not sitting, not on DI stay) and paths it to the feather every
tick. On a vanilla cat that works when it is not sitting. On a Cozy cat with a home, Cozy's `CatHomeGoal` (priority 2,
every tick in Stay, Wander and Guard) stopped it or walked it back to the bed. Now:

| Cozy mode | Vanilla equivalent | Lure |
|---|---|---|
| Follow | standing | works (worked before) |
| Wander, Guard | DI wander | works: `mixin/CozyHomeGoalFeatherMixin` makes the home goal give way while the lure pulls this cat (`CatFeather.chasing`, refreshed by the feather's tick) |
| Stay | sitting / DI stay | ignored: `DiFeatherEntityMixin` adds Stay to DI's `isPetAmbulatory` |
| sitting | sitting | ignored (DI) |

### 10.5 Feather on a Stick hooks mobs (`cats/CatFeather`, `[cats]` in `creativecompat-common.toml`)

- `DiFeatherEntityMixin`: DI's feather hits nothing (`canHitEntity` false, empty `onHitEntity`). It now hits the
  mobs `CatFeather.canHook` allows and sticks to them through the fishing hook's own hooked state
  (`FishingHook.setHookedEntity`, via ObfuscationReflectionHelper), so it rides the mob like a hooked bobber.
- Allowed: hostile mobs (`Enemy` and not `NeutralMob`, `hookHostile`, default true); peaceful and neutral mobs
  (`hookNeutralAndPassive`, default false); players other than the caster (`hookPlayers`, default false, independent of
  pvp). Never the caster, the caster's own pets (OwnableEntity owner), armour stands or other non-mobs.
- While hooked, every 10 ticks the caster's tamed cats within 21 blocks of the caster that DI counts as ambulatory
  (plus the Stay rule) take the mob as target. `FeatherAttackGoal` (MeleeAttackGoal, speed 1.3, priority 1, added to
  every cat on the server) chases and claws it while the feather holds it, outranking Cozy's home goal and every
  vanilla cat goal. Unhooked (reeled in, feather gone, mob dead), the goal stops and the cat drops the target.
- A cat's melee hit on a mob held by its owner's feather (LivingHurtEvent, direct entity the cat) gives the cat
  Strength, Resistance and Regeneration at `buffLevel` (default 3 = III) for `buffSeconds` (default 10).
- Reeling in a hooked feather lets go without pulling the mob (`FishingHookFeatherPullMixin`, both sides); it costs 5
  durability as a fishing rod does.
- All cats, not only Cozy's. `featherHooksMobs = false` restores DI's pass-through feather.
- Tooltip line on the feather (Creative Compat `tooltips.js`).

### 10.6 Tests

Scratch server: SQ-CAT-01 to -05; client: CQ-CAT-01 to -10 ([scratch_queue.md](testing/scratch_queue.md)).

| ID | Test | Pass |
|---|---|---|
| CIC-25 | Raw Cod / Salmon on a wild cat | tames (about 1 in 2), no ancient fish hint |
| CIC-26 | Stats chat (Training String) and Cat Collar tooltip | no Gender line |
| CIC-27 | Breed two tamed cats | any pair breeds |
| CIC-28 | `/give` Ancient Cod; JEI search "ancient" | turns into Cod; nothing in JEI |
| CIC-29 | Fisherman level 3 | no ancient fish trades |
| CIC-30 | Empty-hand click a Cozy cat; a wolf | cat sits / stands, no DI message; wolf cycles DI modes |
| CIC-31 | Guard mode with a zombie in the home radius | warning, glow, patrol |
| CIC-32 | Feather on the ground: Follow, Wander, Guard, Stay, sitting | follows in the first three only |
| CIC-33 | Feather cast at a zombie | sticks; cats attack; buffs after the first hit |
| CIC-34 | Reel in while hooked | no pull; cats stop |
| CIC-35 | Feather at cow, wolf, villager, player, own pet | passes through by default; sticks with the two settings on; never own pets |
| CIC-36 | Cat Care Guide pages | no genders, no ancient fish |

CIC-05, CIC-14 to CIC-18 and CIC-24 (ancient fish taming, fishing, drops, trades, cooked effects) are superseded by
CIC-25, CIC-28 and CIC-29. CIC-10 is CIC-30.
