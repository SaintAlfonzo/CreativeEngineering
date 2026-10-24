# backportedMiners

Miner's Delight 1.4.5 (SammySemicolon, Forge 1.20.1) and 1.1.2 (Forge 1.19.2) merged into one Forge 1.19.2 jar for the
CreativeEngineering pack. Mod id `miners_delight` and package `com.sammy.minersdelight`, so every upstream resource
location, old world and Create Central Kitchen's three mixins still match. Plan, decisions and tests:
`feature_writeup/miners_delight.md`. Licence: GPLv3 (LICENSE).

Miner's Delight is all rights reserved, code and assets. So the jar holds only our own code, written from the mod's
behaviour (not from its decompile), and no upstream file: art, lang, recipes, loot, tags and worldgen are read at load
time from the unmodified upstream jar in `<game dir>/miners_upstream/` (the Vinery donor-jar method). The game stops
with a message if that jar is missing or its sha1 differs from `UpstreamJars`.

Donor: `miners_delight-1.20.1-1.4.5-backport.jar`, sha1 `638438679800711939e7a8029ae3d5340f8c17bd`, from
https://cdn.modrinth.com/data/qMxbM4BQ/versions/p0INUam7/miners_delight-1.20.1-1.4.5-backport.jar

## Build

    ./gradlew :backportedMiners:build --offline          (from the instance root)
    ./gradlew :backportedMiners:installToMods --offline  (game closed; moves the 1.1.2 jar out too)
    python3 development/backportedMiners/tools/check_donor.py

`libs/` holds compile-only copies of FarmersDelight-1.19.2-1.2.4 and jei-1.19.2-forge-11.8.1.1034 from `mods/`
(git-ignored). `checkNoUpstreamResources` fails the build if `src/main/resources` holds a path the donor also has.
`check_donor.py` resolves every id the donor's data names against the port's registry and the last `/dumpall`.

## What the translator changes (upstream/MinersTranslator)

- Drops everything naming a `REMOVED` id (the hot cocoa cup, which only 1.1.2 had).
- Moves the ten cooking recipes under the stray `minersdelight` namespace to `miners_delight`.
- Renames the 1.20 loot condition `minecraft:any_of` to 1.19.2's `minecraft:alternative`.
- Drops the cup conversion row for `farmersdelight:onion_soup` (not in FD 1.19.2; the pack adds a Creamy Onion Soup
  row in `kubejs/data/creativeengineering/cup_conversion/`).
- Drops the five entries of `global_loot_modifiers.json` that have no file upstream.

## What differs from upstream behaviour

- The empty Copper Cup milks only cows and goats (upstream lends a bucket to any mob, which ate fish and the cup).
- No Forge fluid capability on the cups (upstream's bucket wrapper would have turned a cup into a bucket).
- The two no-op brewing steps (long and strong haste + fermented spider eye giving the same potion) are left out.
- The "Served on" tooltip sits on the meal display slot (upstream pointed it at the output slot).
- Feast items are plain block items (FD 1.19.2 has no PlaceableItem; the pack shows placeable tooltips itself).
- Cup conversions are sent to clients, so the recipe book ghost and JEI show cups on a dedicated server too.
