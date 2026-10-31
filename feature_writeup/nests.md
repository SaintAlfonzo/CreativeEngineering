# Nests and egg crates (Incubation Additions: emus, ducks and geese)

Written 2026-10-05. Test prefix `NE-`.

## 0. At a glance

| Part | Status |
|---|---|
| Own mod: **Incubation Additions** (`incubation_additions`, [development/incubationAdditions](../development/incubationAdditions/README.md), jar `incubationAdditions-1.0.0.jar`) | BUILT 2026-10-05, in `mods/` with patcherMod 1.0.5 and DEPLOYED with pack 1.0.6 (2026-10-14, first time it ships); booted in single player 2026-10-11 without errors. Scratch server: old `creativeengineering:` nests remap, emu/duck/goose all lay into their nests |
| Per-mod gating | BUILT 2026-10-05, scratch servers: Incubation only -> nothing registered, no extra packs; + duck mod -> duck/goose nests + crates + duck pack only; + Alex's Mobs -> emu nest + emu pack only; full pack -> both packs, all 7 recipes |
| Duck and goose egg crates (`WaterfowlEggCrates`) | BUILT 2026-10-05, scratch-server verified (blocks, loot, axe tag, recipes load); not seen in game |
| New sprites for the duck mod's duck/goose egg items (`client/DuckModEggModels`) | BUILT 2026-10-05, client side NOT run yet (NE-13) |
| Emu nest (`EmuNests`, Alex's Mobs emus) | DEPLOYED in patcherMod 1.0.2 (old id `creativeengineering:emu_nest`), never tested in game |
| Duck and goose nests (`WaterfowlNests`, Untitled Duck Mod) | moved to Incubation Additions (`incubation_additions:duck_nest` / `goose_nest`); DEPLOYED with pack 1.0.6 (2026-10-14) (the 2026-10-05 staging that had them under the old patcherMod ids was rebuilt before it went out, so the old duck/goose ids never reached a player); scratch-server verified 2026-10-05 |
| Foreign-nest NPE guard (`LayEggInNestGoalMixin`) | DEPLOYED with the emu nest (1.0.2) |
| Goose golden egg (`goldenegg/`, 3.11; [vintage_delight.md](vintage_delight.md) §2.5) | BUILT 2026-10-23 in 1.1.0 (`DropSpot` logic test 9/9 in plain Java), `incubationAdditions-1.1.0.jar` in `mods/`, DEPLOYED with 1.1.02 (1.1.02 changelog Mobs & Animals); scratch SQ-VP-13 PASSED (NE-15, NE-16 logic); NE-15 to NE-17 in game |
| Textures | emu, duck and goose eggs have their own sheets (but the duck and goose sheets are byte-identical copies of Incubation's `duck_eggs.png` / `turkey_eggs.png` and the emu sheet is a recolour of `turkey_eggs.png`, checked 2026-10-21: see 4); duck/goose nest base + item textures are swappable copies of the hay nest; the emu nest base + item still use Incubation's twig nest. All in the Incubation Additions jar; override from `kubejs/assets/incubation_additions/`. See 2.3 |

**Files to open first:** [EmuNests.java](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/nest/EmuNests.java),
[WaterfowlNests.java](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/nest/WaterfowlNests.java),
[LayEggInNestGoalMixin.java](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/mixin/LayEggInNestGoalMixin.java),
[IncubationAdditions.java](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/IncubationAdditions.java) (gating, packs).

**Knobs:** `config/incubation_additions-common.toml`, `[goose] goldenEggChance` (1 in N, default 1000, 0 = off; since
1.1.0). No CEConfig section. Egg timers are the mobs' own (6000 to 12000 ticks).

**How to measure:** put a nest in a pen with the bird, wait one lay cycle (5 to 10 minutes), check the
nest fills instead of an egg dropping. A hopper under the nest pulls the eggs out.

## 1. Overview: how Incubation nests work

Incubation 3.0.1 (Team Abnormals) adds the hay nest and twig nest. Out of the box:

- A bird that implements Incubation's `EggLayer` interface gets `LayEggInNestGoal` when it joins the
  world (priority 3, search range 16). Vanilla chickens get the interface through Incubation's own mixin.
- In the last 400 ticks before its egg timer runs out, the bird walks to a suitable nest. If it gets
  there, it lays into the nest and resets its timer, so the normal egg drop is skipped. If it doesn't
  make it in time, the egg drops on the ground as usual.
- An empty nest (`EmptyNestBlock`) holds a map of egg item to filled nest. Laying swaps the empty
  nest for the filled nest (`BirdNestBlock`), which has an `eggs` state from 1 to 6.
- Right-click a filled nest to take one egg. A comparator reads the egg count.
- A filled nest pushes one egg per tick into an unlocked hopper directly below it. That is the
  automation path.
- The block entity type accepts every `BirdNestBlock` registered by any mod, so our nests get the
  hopper push for free.

The hay and twig nests only map chicken, duck and turkey eggs. Duck and turkey eggs come from
Environmental and Autumnity, which are not in the pack, so in stock form only chickens use nests.

## 2. Reference

### 2.1 Blocks, items and recipes

| Nest | Bird (mod) | Empty nest block (has item) | Filled nest block (no item) | Egg item | Recipe |
|---|---|---|---|---|---|
| Emu | Emu (Alex's Mobs) | `incubation_additions:emu_nest` | `incubation_additions:emu_egg_nest` | `alexsmobs:emu_egg` | `B B` / `BBB`, B = `#immersive_weathering:bark` |
| Duck | Duck (Untitled Duck Mod) | `incubation_additions:duck_nest` | `incubation_additions:duck_egg_nest` | `untitledduckmod:duck_egg` | `T T` / `GGG`, G = grass, T = tall grass |
| Goose | Goose (Untitled Duck Mod) | `incubation_additions:goose_nest` | `incubation_additions:goose_egg_nest` | `untitledduckmod:goose_egg` | `G G` / `TTT`, G = grass, T = tall grass |

Recipes ship in the mod (`packs/<mod id>/data/incubation_additions/recipes/`), not KubeJS.
For comparison, the pack's twig nest recipe (KubeJS `misc_mods.js`) is `T T` / `TTT` from twigs or any wooden rod.
The duck and goose recipes use the same two ingredients in swapped rows, so the U shapes stay distinct.

Other properties:

| | Emu | Duck | Goose |
|---|---|---|---|
| Block properties | Incubation `TWIG_NEST` | Incubation `HAY_NEST` | Incubation `HAY_NEST` |
| Flammability | 60 / 20 | 60 / 20 | 60 / 20 |
| Mined by | hoe (`incubation:bird_nests`) | hoe | hoe |
| Drop when broken | nest with shears, else 1-3 oak bark (like twig nest -> sticks), through the item tag `incubation_additions:emu_nest_drops` so the table still loads without Immersive Weathering (then: nothing) | the empty nest | the empty nest |
| Compost chance (empty nest item) | 0.65 (pack, KubeJS compost) | 0.65 | 0.65 |
| Creative tab | Decorations | Decorations | Decorations |
| Lay sound | chicken egg | duck mod's duck egg sound | duck mod's goose egg sound |

Egg crates (Untitled Duck Mod only), the same as Incubation's chicken egg crate:

| Crate | Recipe | Back to eggs | Properties |
|---|---|---|---|
| `incubation_additions:duck_egg_crate` | 9 `untitledduckmod:duck_egg` (3x3) | crate -> 9 duck eggs (shapeless) | copy of the chicken egg crate (wood, strength 1.5, wood sound), axe-mineable, flammable 5 / 20, drops itself, Decorations tab |
| `incubation_additions:goose_egg_crate` | 9 `untitledduckmod:goose_egg` (3x3) | crate -> 9 goose eggs | same |

No other recipe in the pack's jars uses 9 duck or goose eggs (checked 2026-10-05).

### 2.2 Which bird uses which nest

| Bird | Hay nest | Twig nest | Emu nest | Duck nest | Goose nest |
|---|---|---|---|---|---|
| Chicken | yes | yes | no | no | no |
| Emu | no | no | yes | no | no |
| Duck (Untitled Duck Mod) | no (*) | no (*) | no | yes | no |
| Goose | no | no | no | no | yes |

(*) Hay and twig nests do have a duck variant, but it is mapped to Environmental's duck egg, not
`untitledduckmod:duck_egg`, so these ducks skip them.

### 2.3 Textures, per nest (for hotswapping)

All texture paths below are inside a resource pack root, so `incubation:block/hay_nest` means
`assets/incubation/textures/block/hay_nest.png`. All textures are 16x16.

**Emu nest**

| Part | Texture in use | Referenced from |
|---|---|---|
| Nest base (empty and filled) | `incubation:block/twig_nest`, in `mods/incubation-1.19.2-3.0.1.jar` | blockstates [emu_nest.json](../development/incubationAdditions/src/main/resources/packs/alexsmobs/assets/incubation_additions/blockstates/emu_nest.json) and [emu_egg_nest.json](../development/incubationAdditions/src/main/resources/packs/alexsmobs/assets/incubation_additions/blockstates/emu_egg_nest.json), model `incubation:block/twig_nest` |
| Nest item sprite | `incubation:item/twig_nest` (Incubation jar) | [models/item/emu_nest.json](../development/incubationAdditions/src/main/resources/packs/alexsmobs/assets/incubation_additions/models/item/emu_nest.json) |
| Eggs | **ours:** [textures/block/emu_eggs.png](../development/incubationAdditions/src/main/resources/packs/alexsmobs/assets/incubation_additions/textures/block/emu_eggs.png) (recoloured turkey eggs) | [models/block/one_emu_egg.json](../development/incubationAdditions/src/main/resources/packs/alexsmobs/assets/incubation_additions/models/block/one_emu_egg.json) to `six_emu_eggs.json`, each parented on `incubation:block/*_turkey_egg(s)` |

**Duck nest** (files in the Incubation Additions jar, `assets/incubation_additions/`)

| Part | Texture file (swap this) | Referenced from |
|---|---|---|
| Nest base (empty and filled) | [textures/block/duck_nest.png](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/textures/block/duck_nest.png) (copy of Incubation's hay nest) | [models/block/duck_nest.json](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/models/block/duck_nest.json) (twig-nest geometry), used by blockstates [duck_nest.json](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/blockstates/duck_nest.json) and [duck_egg_nest.json](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/blockstates/duck_egg_nest.json) |
| Nest item sprite | [textures/item/duck_nest.png](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/textures/item/duck_nest.png) (copy of Incubation's hay nest item) | [models/item/duck_nest.json](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/models/item/duck_nest.json) |
| Eggs | [textures/block/duck_eggs.png](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/textures/block/duck_eggs.png) (byte-identical to Incubation's `duck_eggs.png`, md5 checked 2026-10-21) | [models/block/one_duck_egg.json](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/models/block/one_duck_egg.json) to `six_duck_eggs.json`, each parented on `incubation:block/*_duck_egg(s)` |

**Goose nest** (files in the Incubation Additions jar, `assets/incubation_additions/`)

| Part | Texture file (swap this) | Referenced from |
|---|---|---|
| Nest base (empty and filled) | [textures/block/goose_nest.png](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/textures/block/goose_nest.png) (copy of Incubation's hay nest) | [models/block/goose_nest.json](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/models/block/goose_nest.json) (twig-nest geometry), used by blockstates [goose_nest.json](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/blockstates/goose_nest.json) and [goose_egg_nest.json](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/blockstates/goose_egg_nest.json) |
| Nest item sprite | [textures/item/goose_nest.png](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/textures/item/goose_nest.png) (copy of Incubation's hay nest item) | [models/item/goose_nest.json](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/models/item/goose_nest.json) |
| Eggs | [textures/block/goose_eggs.png](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/textures/block/goose_eggs.png) (byte-identical to Incubation's `turkey_eggs.png`, md5 checked 2026-10-21) | [models/block/one_goose_egg.json](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/models/block/one_goose_egg.json) to `six_goose_eggs.json`, each parented on `incubation:block/*_turkey_egg(s)` |

**Egg crates and the duck mod's egg items** (Untitled Duck Mod pack)

| Part | Texture file (swap this) | Referenced from |
|---|---|---|
| Duck egg crate top / side | [duck_egg_crate_top.png](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/textures/block/duck_egg_crate_top.png), [duck_egg_crate_side.png](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/textures/block/duck_egg_crate_side.png); bottom is Incubation's `chicken_egg_crate_bottom` | [models/block/duck_egg_crate.json](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/models/block/duck_egg_crate.json) (`cube_bottom_top`) |
| Goose egg crate top / side | [goose_egg_crate_top.png](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/textures/block/goose_egg_crate_top.png), [goose_egg_crate_side.png](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/textures/block/goose_egg_crate_side.png); same bottom | [models/block/goose_egg_crate.json](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/models/block/goose_egg_crate.json) |
| `untitledduckmod:duck_egg` item | [textures/item/duck_egg.png](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/textures/item/duck_egg.png) (yours, 2026-10-05) | [models/item/duck_egg.json](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/models/item/duck_egg.json), swapped in for the duck mod's model by `client/DuckModEggModels` |
| `untitledduckmod:goose_egg` item | [textures/item/goose_egg.png](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/textures/item/goose_egg.png) (yours, 2026-10-05) | [models/item/goose_egg.json](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/assets/incubation_additions/models/item/goose_egg.json), same swap |

All of these files live in `development/incubationAdditions/src/main/resources/packs/<mod id>/assets/incubation_additions/`
(`alexsmobs` for the emu, `untitledduckmod` for the rest) and ship inside the jar. Each folder is a
built-in pack that only loads when its mod is installed.

**Egg texture layout.** The egg models only read part of the 16x16 egg sheet. Paint into these
regions (u, v from the top left, in pixels):

| Egg geometry | Big egg side | Big egg top | Small egg side | Small egg top |
|---|---|---|---|---|
| Chicken (unused by our nests) | 0,0 to 4,6 | 4,0 to 8,4 | 0,6 to 3,10 | 3,6 to 6,9 |
| Turkey (emu, goose) | 0,0 to 4,6 | 4,0 to 8,4 | 0,6 to 3,11 | 3,6 to 6,9 |
| Incubation duck models (duck) | 0,0 to 4,7 | 4,0 to 8,4 | 0,7 to 3,12 | 3,7 to 6,10 |

The easiest start is to copy `chicken_eggs.png` / `turkey_eggs.png` / `duck_eggs.png` out of the
Incubation jar and recolour it, the way `emu_eggs.png` was made. A recolour is still a modified copy under the
Abnormals License: fine for a private test in `kubejs/assets/`, not for the jar (section 4).

**How to hotswap.** KubeJS's `kubejs/assets/` is loaded as a resource pack above every mod jar, so a
PNG at the same path there replaces the jar's copy and reloads with F3+T. No model edits needed:

1. *Any of our textures* (emu/duck/goose egg sheets, duck/goose nest base and item, crate top/side,
   duck/goose egg item sprites): put the new PNG at
   `kubejs/assets/incubation_additions/textures/<block|item>/<name>.png`, same name as in the tables.
   Keep an egg sheet on the layout its geometry reads (duck = Incubation duck layout, goose and emu =
   turkey). Want a different egg geometry? Change the six `parent` lines of that bird's egg models.
2. *Emu nest base or item:* it still points at Incubation's `twig_nest` textures. Copy the duck
   pattern in the mod's resources: `textures/block/emu_nest.png`, `models/block/emu_nest.json` with
   the `nest` texture override, the two emu blockstates repointed, and an item model.
3. Once a texture is final, copy it into the right `packs/<mod id>/assets/` folder and rebuild
   (`./gradlew :incubationAdditions:installToMods`), then delete the `kubejs/assets/` copy.
4. Resource-pack zips in `resourcepacks/` beat `kubejs/assets/` (see the lang-priority note in
   memory); none touch these paths as of 2026-10-05.

## 3. What the pack changes

1. **Emus lay in nests.** [EntityEmuEggLayerMixin](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/mixin/EntityEmuEggLayerMixin.java)
   makes `EntityEmu` an `EggLayer`. It only exposes the emu's existing `timeUntilNextEgg` timer, which
   already works exactly like the chicken's (6000 + rand(6000), saved as NBT `EggLayTime`).
2. **Ducks and geese lay in nests.** [DuckEntityEggLayerMixin](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/mixin/DuckEntityEggLayerMixin.java)
   and [GooseEntityEggLayerMixin](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/mixin/GooseEntityEggLayerMixin.java)
   do the same with the duck mod's private `eggLayTime` (NBT `duckEggLayTime` / `gooseEggLayTime`).
3. **Three new nest pairs** (empty + filled) registered in `EmuNests` and `WaterfowlNests`. The
   filled nest's constructor adds itself to its empty nest's egg map, so nothing else is needed to
   link them.
4. **Foreign-nest NPE guard.** Incubation's goal accepts any empty nest as a target, then crashes
   when the nest has no variant for the bird's egg. [LayEggInNestGoalMixin](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/mixin/LayEggInNestGoalMixin.java)
   rejects those nests in `isValidTarget`, which also gives the "each bird only uses its own nest"
   rule in 2.2.
5. **Own mod since 2026-10-05.** Everything above lived in patcherMod (`creativeengineering:` ids) up
   to 1.0.5. `IncubationAdditions.remapOldIds` (a `MissingMappingsEvent` handler, same as Exposure
   Extras does for the camera bags) loads nests and nest items saved under the old ids as the
   `incubation_additions:` ones, egg counts included.
6. **One content set per bird mod.** Incubation is the only hard dependency. `IncubationAdditions`
   keeps a list of sets (Alex's Mobs: emu nest; Untitled Duck Mod: duck/goose nests, egg crates, egg
   item sprites). A set whose mod is missing registers nothing, and `IncubationAdditionsMixinPlugin`
   skips its mixins; `LayEggInNestGoalMixin` applies only when at least one set is on. With neither
   mod the mod does nothing at all.
7. **Per-mod built-in packs.** A set's assets, recipes, loot tables and tags live in
   `packs/<mod id>/`, not the jar's root resources. `addPacks` (Forge `AddPackFindersEvent`) adds that
   folder as a required, hidden built-in pack, for both client resources and server data, only for
   active sets. On an existing world the pack is switched on automatically (`datapack list` shows
   `incubation_additions:<mod id> (built-in)`). Tags: the nests join `incubation:bird_nests`
   (hoe-mineable), the crates `minecraft:mineable/axe`.
8. **Egg crates** (`crate/WaterfowlEggCrates`): duck and goose versions of Incubation's chicken egg
   crate, properties copied from it.
9. **Duck mod egg item sprites** (`client/DuckModEggModels`, client only): the duck mod's
   `duck_egg` / `goose_egg` inventory models are replaced at model bake (`ModelEvent.BakingCompleted`)
   by `incubation_additions:item/duck_egg` / `goose_egg`.
10. **KubeJS keeps only pack-level bits:** `i.*` / `b.*` keys for the nests and crates in the dicts,
   compost values. The nest recipes (`misc_mods.js`) and the `bird_nests` tag (`blocktags.js`) moved
   into the mod.
11. **Goose golden egg** (1.1.0, Untitled Duck Mod set, only while Vintage Delight is loaded and
   `vintagedelight:golden_egg` exists). [GooseEntityEggLayerMixin](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/mixin/GooseEntityEggLayerMixin.java):
   at the head of `aiStep`, on the first tick the egg timer is 400 or less (the nest goal's window, before
   the goals run that tick), it rolls `goldenEggChance` once per egg. A win sets a "golden egg due" flag:
   `LayEggInNestGoalMixin` makes `canEggBeLaid` false for that goose, so it never starts or continues a nest
   walk, and the `spawnAtLocation` call at 0 is redirected to `goldenegg.GoldenGooseEgg.lay`, which lays the
   Golden Egg instead of the goose egg: since 2026-10-25 as a placed block (the backported Golden Egg block falls
   like sand), at the spot `DropSpot.pickBlock` picks, never the goose's own block, and only where the block there
   is replaceable (no torch, flower or rail is overwritten). With no such spot (a closed 1x1 pen) it drops as the
   item, as before. The goose's own sound before it and its 6000 + rand(6000)
   reset after it are unchanged. Drop spot (`goldenegg.DropSpot`): standable spots within 3 blocks (circle,
   one block up or down, nearest first) in the goose's line of sight, scored by solid blocks among the 8
   neighbours at foot and head height; the first with 0 wins, else the lowest if it beats the goose's own
   spot, else where it stands (item drop). `pickBlock` is the same without the goose's own spot: the first
   with 0, else the lowest, else none. Not saved: a goose reloaded inside the 400-tick window rolls again. If the item
   is gone at drop time the normal egg drops.

## 4. Limits and decisions

- **License issue (found 2026-10-05, open).** Incubation is under Team Abnormals' **Abnormals License 1.0** (its
  `mods.toml` links `team-abnormals/incubation/.../LICENSE.txt`; the text is quoted in
  [mooblooms.md](mooblooms.md) 4.0). That license forbids publishing or distributing copies or modified copies of
  the software "and/or its associated files". Our GPLv3 jar currently ships the duck and goose nest textures as
  **copies of Incubation's hay nest** (block and item, 4 PNGs). The nest models only parent
  `incubation:block/twig_nest` (a reference, not a copy; checked 2026-10-21). Also copied (checked 2026-10-21): the
  duck and goose egg sheets (identical to Incubation's `duck_eggs.png` / `turkey_eggs.png`), the emu egg sheet (a
  recolour of `turkey_eggs.png`) and the 4 egg crate top/side PNGs (112 of 256 pixels identical to the chicken egg
  crate's). Plan: [briefs/mobs_nests.md](briefs/mobs_nests.md) #2. Fix before publishing the jar anywhere:
  - repaint the 4 PNGs (already planned: "placeholder nest looks"), and
  - either write the models from scratch or point them at Incubation's own model/texture by resource location
    (a reference is not a copy).
  Referring to Incubation's ids and textures by resource location, as the emu nest does with the twig nest, is
  fine. Nothing changed yet.
- **Placeholder nest looks.** The duck and goose nest textures are still copies of the hay nest, and
  the emu nest uses the twig nest, so the bases look alike until those PNGs are repainted. The
  eggs are distinct: emu, duck and goose each have their own sheet.
- **Nest variety in spawning (planned, scratchpad 2026-10-21).** New textures for the duplicated nests (above), and
  nests generating in goose, duck and emu habitats. TODO `mobs/nests #3`.
- **Egg geometry follows the painted layout.** The duck sheet is painted to Incubation's duck-egg
  UVs, so duck nests use the `*_duck_eggs` models; the goose sheet fits the turkey UVs, so geese get
  the larger turkey eggs (same as emus).
- **Own mod rather than patcherMod,** like Exposure Extras: the nests are a self-contained addon to
  one mod, with their own deps and mixins. The cost is one more jar to ship, and the id change,
  which the remap covers.
- **Assets live in the jar, overrides in `kubejs/assets/`.** One definition for every model and
  texture; a hotswap is a single PNG with the same path, no model or blockstate copies.
- **Packs per mod instead of `forge:mod_loaded` conditions.** Conditions only cover recipes (and
  loot modifiers); loot tables and tags that name missing blocks log errors, and assets would still
  load. A whole pack per mod makes "mod missing" mean "nothing loaded".
- **Duck mod egg sprites by model swap, not by shipping `assets/untitledduckmod/` files.** Inside
  Forge's `mod_resources` the duck mod's own jar wins that path; putting our pack on top would also put
  it above `kubejs/assets` (KubeJS inserts its pack below the first zip resource pack), which would
  break the hotswap route. Our packs sit at the bottom and only contain our namespace.
- **Ducks don't use hay nests.** Rejected remapping the hay nest's duck variant to the duck mod's
  egg: the map is keyed by egg item and owned by Incubation, so it would need another mixin for no
  real gain over the dedicated nest.
- **Separate nests per bird** instead of one shared waterfowl nest: one empty nest can map several
  eggs, but then a duck nest could fill with goose eggs, and hopper sorting gets harder.
- **Emu nest drops bark, not itself,** unless sheared. This mirrors Incubation's twig nest
  (sticks unless sheared). The duck/goose nests follow the hay nest and always drop themselves.
- Breaking a filled nest returns the empty nest per the loot tables; whether the remaining eggs pop
  out comes from Incubation's `BirdNestBlock` (checked by NE-08, not read from source).

## 5. File map

| File | Role |
|---|---|
| [nest/EmuNests.java](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/nest/EmuNests.java) | emu nest blocks + item, flammability |
| [nest/WaterfowlNests.java](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/nest/WaterfowlNests.java) | duck + goose nest blocks + items, flammability |
| [mixin/EntityEmuEggLayerMixin.java](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/mixin/EntityEmuEggLayerMixin.java) | emu as EggLayer |
| [mixin/DuckEntityEggLayerMixin.java](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/mixin/DuckEntityEggLayerMixin.java) | duck as EggLayer |
| [mixin/GooseEntityEggLayerMixin.java](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/mixin/GooseEntityEggLayerMixin.java) | goose as EggLayer |
| [mixin/LayEggInNestGoalMixin.java](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/mixin/LayEggInNestGoalMixin.java) | skip nests with no variant for the bird's egg |
| [IncubationAdditions.java](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/IncubationAdditions.java) | mod class: content sets per bird mod, built-in packs, flammability, old-id remap |
| [IncubationAdditionsMixinPlugin.java](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/IncubationAdditionsMixinPlugin.java) | applies each mixin only when the mod it serves is installed |
| [crate/WaterfowlEggCrates.java](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/crate/WaterfowlEggCrates.java) | duck + goose egg crates |
| [client/DuckModEggModels.java](../development/incubationAdditions/src/main/java/com/SaintAlfonzo/incubation_additions/client/DuckModEggModels.java) | duck mod egg item model swap (client) |
| [mods.toml](../development/incubationAdditions/src/main/resources/META-INF/mods.toml), [gradle.properties](../development/incubationAdditions/gradle.properties), [README.md](../development/incubationAdditions/README.md) | mod metadata and dependencies, version, build notes |
| [packs/alexsmobs/](../development/incubationAdditions/src/main/resources/packs/alexsmobs/) | emu pack: assets, recipe, loot tables, `bird_nests` tag, `emu_nest_drops` item tag |
| [packs/untitledduckmod/](../development/incubationAdditions/src/main/resources/packs/untitledduckmod/) | duck/goose pack: assets (nests, crates, egg item sprites), recipes, loot tables, `bird_nests` + `mineable/axe` tags |
| [kubejs/server_scripts/other/misc_mods.js](../kubejs/server_scripts/other/misc_mods.js) | twig nest recipe (the others moved to the mod) |
| [kubejs/startup_scripts/compost/compost_values.js](../kubejs/startup_scripts/compost/compost_values.js) | 0.65 for all nests |
| [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js), [block_dict.js](../kubejs/startup_scripts/_lib/block_dict.js) | ids |

## 6. Offline checks

- `unzip -l development/incubationAdditions/build/libs/incubationAdditions-1.0.0.jar`: no root
  `assets/` or `data/`, everything under `packs/alexsmobs/` and `packs/untitledduckmod/`; the rebuilt
  patcherMod jar has no nest or EggLayer entries (2026-10-05).
- Scratch server, move check 2026-10-05: world saved with the old patcherMod 1.0.5 (an emu nest, a
  duck nest with 3 eggs, a goose nest with 5, a chest of all three nest items), then booted with the
  new patcherMod + Incubation Additions: all came back as `incubation_additions:` with the same egg
  counts and item counts. In the same world a duck and a goose laid into their nests (3 -> 4, 5 -> 6)
  and an emu alone laid into a fresh emu nest. (With a goose in the pen, a dropped emu egg gets
  picked up by the goose.)
- Gating, minimal scratch servers 2026-10-05 (Forge + Incubation + Blueprint + this jar):
  - nothing else: no `incubation_additions` blocks (`setblock` -> unknown block), no extra packs.
  - + Untitled Duck Mod: pack `incubation_additions:untitledduckmod` on; duck/goose nests and both
    crates place; nest in `#incubation:bird_nests`, crate in `#minecraft:mineable/axe`; crate and nest
    loot tables drop themselves; emu nest unknown.
  - + Alex's Mobs: pack `incubation_additions:alexsmobs` on; emu nest places and is tagged; duck
    content unknown; emu nest loot drops nothing (no Immersive Weathering there), no loot error.
- Full pack 2026-10-05 (existing scratch world): both packs auto-enabled; `/dumprecipes
  incubation_additions` lists all 7 recipes; emu nest loot drops oak bark.
- Scratch-server run 2026-10-05: a duck laid in a duck nest, a goose in a goose nest (both reached
  1 egg, timers reset), and a chicken next to both ignored them and laid normally.

## 7. In-game test suite

- [ ] **NE-01 emu nest recipe:** 5 bark in a U in a crafting table -> 1 Emu Nest, twig-nest sprite.
- [ ] **NE-02 duck/goose recipes:** `T T`/`GGG` -> Duck Nest; `G G`/`TTT` -> Goose Nest (G grass, T tall grass).
- [ ] **NE-03 emu lays in nest:** fence an adult emu with an emu nest, wait up to 10 min -> nest shows 1 emu egg (our recoloured texture), no egg on the ground.
- [ ] **NE-04 duck lays in nest:** same with a duck and a duck nest -> Duck Egg Nest with 1 egg.
- [ ] **NE-05 goose lays in nest:** same with a goose and a goose nest.
- [ ] **NE-06 birds keep to their own nest:** one pen with a chicken, a duck and a goose plus a hay, duck and goose nest -> each bird only fills its own nest; no crash in `logs/latest.log`.
- [ ] **NE-07 hopper pulls eggs:** hopper under a filled nest -> eggs move into the hopper one at a time, nest goes back to empty.
- [ ] **NE-08 break and take:** right-click a filled nest -> 1 egg; break it with a hoe -> empty nest (emu: bark unless sheared). Note whether leftover eggs drop.
- [ ] **NE-09 nests burn and compost:** fire next to a nest spreads to it; an empty nest in a composter has a 65 % chance to add a layer.
- [ ] **NE-10 hotswap:** place a test PNG at `kubejs/assets/incubation_additions/textures/block/emu_eggs.png`, F3+T -> emu eggs change, chicken and turkey eggs don't.
- [ ] **NE-11 duck/goose look:** fill a duck and a goose nest -> each shows its own egg texture (duck eggs on the duck shape, goose eggs as larger turkey-shaped eggs); nest bases and items look like the hay nest; no missing-texture checkerboard.
- [ ] **NE-12 old world:** load a world that has an emu nest from 1.0.2-1.0.4 (or duck/goose nests from a
  1.0.5 test) after installing Incubation Additions -> the nests are still there, no "missing registry
  entries" screen.
- [ ] **NE-13 duck mod egg sprites:** hold a duck egg and a goose egg -> the new teal duck egg and pale pink goose egg sprites, not the duck mod's originals; also when thrown (thrown eggs use the item model).
- [ ] **NE-14 egg crates:** 9 duck eggs -> Duck Egg Crate, crate -> 9 duck eggs; same for goose; crates show the new top/side textures with the chicken crate's bottom, mine fastest with an axe, drop themselves.

- [ ] **NE-15 goose golden egg** (VTD-32): set `goldenEggChance` to 1, pen a goose with a goose nest -> it places a Golden Egg block beside itself at lay time, never walks to the nest, the nest stays empty. Set it to 0 -> geese lay into nests as before. Without Vintage Delight nothing changes.
- [ ] **NE-16 golden egg away from walls** (VTD-32a): chance 1; a goose against a wall or in a corner with open ground within 3 blocks places the egg block on the open ground; in a closed 1x1 pen it drops the item where it stands; never on the far side of a fence.
- [ ] **NE-17 golden egg block placement**: chance 1; the egg block never lands on the goose, never replaces a torch, flower or rail (a free spot with one is skipped), and falls like sand if the block under it is broken. Breaking it gives its loot table.

## 8. History

- 2026-09-29: emu nest built (EmuNests, emu EggLayer mixin, foreign-nest guard). Shipped in the
  1.0.2 restage 2026-09-30.
- 2026-10-05: duck and goose nests built (WaterfowlNests, two EggLayer mixins), scratch-server
  verified, in `mods/` as patcherMod 1.0.5, staged for 1.0.5.
- 2026-10-05: this writeup, including the per-nest texture locations and the hotswap route.
- 2026-10-05: duck and goose egg textures wired up (duck on Incubation duck-egg geometry, goose on
  turkey geometry); duck/goose nest base and item textures duplicated from the hay nest; all duck/goose
  assets moved to `kubejs/assets/creativeengineering/` and removed from the patcherMod source. NE-11 added.
- 2026-10-05: moved everything to its own mod, Incubation Additions (`development/incubationAdditions`,
  `incubation_additions`, 1.0.0) with an old-id remap; assets back in the jar. patcherMod lost the nest
  package, the 4 mixins, their mods.toml dependencies and lang. Built, scratch-server verified, not in
  `mods/`. NE-12 added.
- 2026-10-05: per-mod gating (content sets + built-in packs per bird mod, mixin plugin covers all 4
  mixins), duck and goose egg crates, new sprites for the duck mod's egg items (model swap), nest
  recipes and `bird_nests` tag moved from KubeJS into the mod. The user's new nest egg sheets were
  identical to the ones already in place. Scratch-server verified; NE-13, NE-14 added.
- 2026-10-05: license note added (section 4): the hay-nest texture copies, and possibly the twig-nest model geometry, in the Incubation Additions jar conflict with the Abnormals License; repaint or reference instead. Not changed yet.
- 2026-10-11: ships in pack 1.0.5 (staged, not deployed): `incubationAdditions-1.0.0.jar` is new in the payload next to
  patcherMod 1.0.5 (which no longer has the nests), so both machines get the pair together. Only the emu nest was
  ever deployed under its old id (1.0.2), so the remap matters for emu nests already placed in the world (NE-12).
  Both jars booted in single player 2026-10-11 without errors.
- 2026-10-14: pack 1.0.5 was never deployed; everything above that was staged for it ships in pack **1.0.6** (STAGED
  2026-10-14, update from 1.0.4, [plan_1.0.6.md](plan_1.0.6.md)), unchanged.
- 2026-10-23: 1.1.0, goose golden egg for the Vintage Delight port (3.11, config `goldenEggChance`). Built,
  `DropSpot` logic test 9/9; not in `mods/`. NE-15, NE-16 added.
- 2026-10-25: the goose lays the Golden Egg as a block (`GoldenGooseEgg.lay`, `DropSpot.pickBlock`); item only
  in a closed 1x1 pen. Built (`:incubationAdditions:build` passes, `pickBlock` checked in plain Java: open ground
  gives the next block over); not in `mods/`, not run in game. NE-15, NE-16 updated, NE-17 added.
