# backportedVintage

Vintage Delight 0.1.6 (Ribs, Forge 1.20.1, MIT) ported to Forge 1.19.2 for the CreativeEngineering pack. Mod id
`vintagedelight`, so every upstream resource location still matches. Plan, decisions and tests:
`feature_writeup/vintage_delight.md`. Licence: GPLv3 (LICENSE); the upstream MIT notice is in NOTICE-upstream-MIT.txt.

## Build

    ./gradlew :backportedVintage:build --offline          (from the instance root)
    ./gradlew :backportedVintage:installToMods --offline  (game closed)

`libs/` holds compile-only copies of FarmersDelight-1.19.2-1.2.4 and jei-1.19.2-forge-11.8.1.1034 from `mods/`
(git-ignored). Resources are generated: `python3 tools/port_resources.py` rewrites `src/main/resources/{assets,data}`
from the upstream jar (and three KOTS 0.3.1 files), then lays `src/own/` (pack-owned files) over it, and prints any id the pack's dumps do not know.

## What differs from upstream

- Purged: the gearo berry line, the Dehydrated effect with its potions and brewing hook, the pickled pitcher pod and
  pitcher plant recipe, the Galosphere / Spelunkery recipes, the Diet tags, the 1.20 sniffer tag.
- Not shipped: biome modifiers of the four wild plants (the pack places them), the seven chest-loot GLMs (the tables
  ship unwired).
- From KOTS 0.3.1: one item per mason jar under the block id (no `mason_jar` / `*_mason_jar` items; the empty jar is
  `empty_mason_jar`), one jar block class, Cucumber Dough, the sided Pickling Jar item handler, the ghost pepper soil
  tooltip, the `magicVineConvertsDirtToLushGrass` common config switch.
- Ghost pepper crops grow on `#vintagedelight:ghost_pepper_soil` (soul soil; the pack adds more); wild ghost peppers
  on nylium or that tag; wild oats, peanuts and cucumbers on `#minecraft:dirt`.
- Pack addition (`-10`, TODO `creativedelight/members #32`): `mixed_seeds` is a registered item (`MixedSeedsItem`; upstream
  ships only its texture). Each use plants a random kind from `#vintagedelight:mixed_seeds_plants` (shipped empty; the
  pack fills it). Its model, lang and the empty tag are written by `tools/port_resources.py`. The `-09` grape and
  cherry jam jars are pack-owned files in `src/own/`, which the script lays over its output last (new files copied, tag
  values and lang keys merged), so a rerun keeps them.
- Pack addition (`-15`, TODO `dishes/jams #8`, own GPLv3 code in `jam/`): the pack jam line (jam and jello blocks, bowls
  of jello, jam and melted jello fluids, fruit / jam / jello cauldrons extending Fruits Delight's cauldron block, compile
  only against `libs/fruitsdelight-0.5.11.jar`), four new jam jars and the Nut Milk Jar, and `fd_<fruit>_jam_jar` blocks
  so Fruits Delight's jams place as mason jars. Resources: `tools/gen_pack_jams.py` writes them into `src/own/` from
  `kubejs/assets/mason_jar_new/`; `port_resources.py` sets every mason jar soft fluid to 1 bottle and drops removed bottles.
- The fermenting jar keeps its id and is named Pickling Jar. Recipe type and serializer `vintagedelight:fermenting`.
- 1.19.2 API: Materials, a CreativeModeTab subclass, Recipe without RegistryAccess, JEI 11, PoseStack screens,
  trunk placer on the vanilla registry key.
