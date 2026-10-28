# blocks / new: implementation brief

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# blocks`, Sub `new` (rows 11 and 12, from the scratchpad of
2026-10-24; rows 1 to 10 predate this brief and have no section yet). Layout per [README.md](README.md). Related writeups:
[structure_tuning.md](../structure_tuning.md) (rare ice), [recipes_materials.md](recipes_materials.md) (the brief; bale recipes),
[loot.md](../loot.md).

## Scope and order

Two independent rows. #11 is a small new block, #12 is a data and config change for the Rare Ice mod.

---

## new #11: Flower bale
Status **Planned** / Work KubeJS / resource pack / TODO `blocks/new #11`

### Goal
A flower bale block beside the straw, rice and hay bales: a decorative (or compostable) block made of flowers.

### Now
- Existing bales: `i.straw_bale` (`farmersdelight:straw_bale`), `i.rice_bale`, `i.hay_bale` (`minecraft:hay_block`), `i.oat_bale`
  (`vintagedelight:oat_bale`) in item_dict. There is no grass bale yet; `merges/materials #2` makes the flax bale a grass bale
  (misc_mods.js line 25-27 swaps `i.flax_block` for rice and straw bales). Pack-own blocks are registered in
  [decor_blocks.js](../../kubejs/startup_scripts/items/decor_blocks.js) (`StartupEvents.registry('block')`, hand-written
  blockstate and models under `kubejs/assets/creativeengineering/`, texture `creativeengineering:block/<name>`).
- Compost chances: [compost_values.js](../../kubejs/startup_scripts/compost/compost_values.js) (`i.rice_bale` 0.85, line 652).
  Bale family tags: `blocktags.js` line 362-364 (sacks, bags and bales).

### Build
1. Register `creativeengineering:flower_bale` in decor_blocks.js (hay-like material, `mineable/hoe`), model with an
   axis rotation like the hay block, a new texture (a resource pack job; recolour of the hay texture with petals).
2. Recipe through `global.NCH`: 9 flowers in a square -> 1 bale (and the reverse 1 -> 9); the flower input is a tag
   (`#minecraft:small_flowers`; the pack's own flower tag if the wild flowers add modded ones).
3. `global.item` key, lang name, compost value (a bale of flowers: about 0.85), `blocktags.js` entry beside the other bales.

### Decisions
Recipe shape and whether it is decorative only or also a compost / fuel block. Recommend a 3x3 craft with reverse, compost
0.85, no other use.

### Gotchas
Mixed flowers in one craft: output is the same block, so a tag recipe is fine; bees and Bushier Flowers tags are unaffected.

### Tests
- BL-01 (proposed, new prefix `BL-`): craft, uncraft, place on each axis, compost it, break with a hoe.

---

## new #12: Rare ice: hide it, make it configurable
Status **Planned** / Work KubeJS / config / TODO `blocks/new #12`

### Goal
Rare Ice is harder to stumble on (its visible sources removed) and its generation can be configured.

### Now
- Mod `rare-ice-0.5.1.jar`: block `rare-ice:rare_ice` with **no item** (the loot table gives `minecraft:ice` with Silk Touch),
  a block entity holding an item, a world feature `rare-ice:rare_ice` added by `data/rare-ice/forge/biome_modifier/add_rare_ice.json`
  (`#minecraft:is_overworld`, step `underground_ores`), a chest loot table `chests/rare_ice`. Its block tag `minecraft:ice`
  adds `rare-ice:rare_ice`.
- **Generation is already configurable:** `config/rare-ice.properties` has `probabilityOfRareIce=3` and
  `allowInsertingItemsToIce=true`. The scale of the 3 is unverified (read `RareIceFeature`).
- Visible sources: the block sits in `#minecraft:ice`, so it appears in JEI block-tag cycling (the world tabs show it as a
  renamed Barrier, `jei/jei-coverage #19`), WTHIT lists it, and `b.t_ice` (block_dict line 86) includes it. Loot lists it in
  the dump ([loot.md](../loot.md) 172, untyped).

### Build
1. Decide "hidden" (Decisions). Cheapest: leave generation, remove it from JEI cycling (the tab generator skips
   `rare-ice:*`, `tools/jei_world/` modules) and from `removed_items`-style hiding (it has no item, so nothing to hide in JEI).
2. Config: document `probabilityOfRareIce` in a `configs_edited/root_config/config/rare-ice.properties` copy and in the
   writeup; if per-biome control is wanted, override the biome modifier at `kubejs/data/rare-ice/forge/biome_modifier/add_rare_ice.json`
   with a narrower `biomes` list (a datapack override of the same path).
3. Optional: a worldgen config key in `CEConfig` `[worldgen]` if rarity should sit with the pack's other worldgen keys.

### Decisions
- What "hidden" means: out of JEI and tag listings, rarer, deeper, or only in cold biomes. Recommend: rarer through the
  existing property plus a biome-modifier override limiting it to cold biomes; remove it from the JEI cycling.
- Config location: the mod's own file (exists) or a pack key. Recommend the mod's own file.

### Gotchas
Removing the block from `#minecraft:ice` would stop it counting as ice (melting, frost walker, mineable rules): do not.

### Tests (ST-)
- ST-228 (proposed): `probabilityOfRareIce = 0` and a new world: no rare ice in a 10 x 10 chunk dig; the default frequency
  unchanged by the override file.
