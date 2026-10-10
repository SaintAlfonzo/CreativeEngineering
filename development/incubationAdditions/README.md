# incubationAdditions

**Incubation Additions** (`incubation_additions`): this pack's additions to Incubation 3.0.1 (Team
Abnormals). Jar: `incubationAdditions-<mod_version>.jar`. GPLv3 (LICENSE.txt).

What it adds (full reference and tests: [feature_writeup/nests.md](../../feature_writeup/nests.md), `NE-`).
Incubation is the only hard dependency; everything else comes in one **content set per bird mod**, switched
on only when that mod is installed. With neither installed the mod does nothing (no blocks, no mixins, no
assets, no data).

| Set (needs) | Content | Code | Resources |
|---|---|---|---|
| Alex's Mobs | emu nest + emu egg nest, emus lay in it | `nest/EmuNests`, `mixin/EntityEmuEggLayerMixin` | `packs/alexsmobs/` |
| Untitled Duck Mod | duck and goose nests + egg nests; duck and goose egg crates; new sprites for the duck mod's egg items | `nest/WaterfowlNests`, `crate/WaterfowlEggCrates`, `client/DuckModEggModels`, `mixin/Duck/GooseEntityEggLayerMixin` | `packs/untitledduckmod/` |
| either | foreign-nest guard (birds skip empty nests with no variant for their egg; stops an Incubation NPE) | `mixin/LayEggInNestGoalMixin` | - |

How the gating works:
- `IncubationAdditions.SETS` lists the sets; only those whose mod is loaded register blocks/items and
  setup work.
- `IncubationAdditionsMixinPlugin` applies each mixin only when a mod it serves is installed.
- `packs/<mod id>/` is a normal resource + data pack (pack.mcmeta, `assets/`, `data/`). `addPacks` adds it
  as a required, hidden built-in pack only for active sets, so recipes, loot tables, tags and models of a
  missing mod are never loaded. The jar's root resources hold no assets or data.
- Old ids: `remapOldIds` maps `creativeengineering:<nest>` blocks and items to `incubation_additions:<nest>`.

Adding a new bird mod: a new `ContentSet` in `IncubationAdditions`, its classes, a `packs/<its mod id>/`
folder, its mixins in `NEEDS_MOD` of the mixin plugin, an optional dependency in mods.toml, its jar in libs/.

## History

Up to pack 1.0.5 all of this lived in patcherMod (`creativeengineering`). It moved here on 2026-10-05,
the same way the camera bags moved out of patcherMod into Exposure Extras.

## Build

Part of the root Gradle build (root `settings.gradle`, shared `ce.forge-mod` convention plugin):

    ./gradlew :incubationAdditions:build          # -> build/libs/incubationAdditions-<ver>.jar

- `libs/` (git-ignored) holds the compile-only jars copied from `../../mods`: incubation, blueprint,
  alexsmobs, citadel, untitledduckmod, geckolib. Re-copy them when those mods update.
- Like patcherMod there is no refmap. Mixins that touch Minecraft members use SRG names with
  `remap = false`; the AP target validator is off.
- Version: `mod_version` in `gradle.properties`.

## Install

Install it **together with** the patcherMod build that no longer has the nests, then restart:

    ./gradlew :patcherMod:installToMods :incubationAdditions:installToMods

If only one of the two is swapped, worlds either get both sets of nests (old patcherMod + this) or
lose their nests (new patcherMod without this). Then:

- add an `incubation_additions` row to `mod_sides.md` (`both`) and an instance.json userAdded entry,
  regenerate `pack/`, and restage the deploy. `tools/build_deploy.py` already lists the project in
  `IN_TREE_MODS`.

## Textures

All assets are in `src/main/resources/packs/<mod id>/assets/incubation_additions/`. To try a texture
without rebuilding, put a PNG with the same path under `kubejs/assets/incubation_additions/` and press F3+T
(per-file paths and egg UV layouts: nests.md 2.3).
