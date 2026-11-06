# backportedFungi

Fungi Delight 1.3 (Eggmain, Forge 1.20.1, MIT) ported to Forge 1.19.2 for the CreativeEngineering pack. Mod id
`fungidelight`, so every upstream resource location still matches. Plan, decisions and tests:
`feature_writeup/fungi_delight.md` (section 5.1 is the owner plan). Licence: GPLv3 (LICENSE); the upstream MIT notice
is in NOTICE-upstream-MIT.txt.

## Build

    ./gradlew :backportedFungi:build --offline          (from the instance root)
    ./gradlew :backportedFungi:installToMods --offline  (game closed)

`libs/` holds a compile-only copy of FarmersDelight-1.19.2-1.2.4 from `mods/` (git-ignored). Resources are generated:
`python3 tools/port_resources.py` rewrites `src/main/resources/{assets,data}` from the upstream jar
(`~/.local/share/atlauncher/downloads/FungiDelight-1.20.1-1.3.jar`) and prints any recipe id the pack's dumps do not know.

## What differs from upstream

- No config file. Every value upstream read from `fungidelight-common.toml` is its shipped default, hard-coded: all
  switches on, pig digging chance 2.5e-4 per tick, truffle twins on. The configurable Weak Stomach item list is gone.
- Dropped: the sniffer mixin, `minecraft:sniffer_food` and the sniffer config; `ConfigurableRarityFilter` and its
  placement modifier type; the Botany Pots recipes; all advancements (including upstream's override of the vanilla
  suspicious stew recipe advancement); the compost hook (the pack's compost engine sets the values); the 1.20-only
  block tags (`enchantment_power_transmitter`, `replaceable`, `replaceable_by_trees`, `sword_efficient`); the
  Autumnity and Spawn tags (mods not in the pack); the biome tags `has_inky_caps` / `has_morels` / `has_truffles`
  (only the biome modifiers used them); upstream's `#forge:alcohol` tag file (78 ids, nothing here reads it).
- Not shipped: the 14 biome modifiers and the 12 placed features (the pack places mushrooms, rooted dirt, truffle dirt
  and the cows itself). The 8 configured features ship under their upstream ids for the pack's placed features:
  `huge_inky_cap_mushroom`, `huge_morel_mushroom`, `patch_inky_cap_mushroom`, `patch_inky_cap_mushroom_colony`,
  `patch_morel_mushroom`, `patch_morel_mushroom_colony`, `rooted_dirt_blob`, `truffle_dirt_ore`.
- Fix: `patch_inky_cap_mushroom` places on `#fungidelight:inky_cap_placeable_on` (upstream read the morel tag, so the
  inky cap tag with sculk was never used).
- Mooshroom breeding: upstream's `MushroomCowMixin` and `FDMCow`'s `canMate` / `getBreedOffspring` overrides are gone;
  patcherMod's mixin on `MushroomCow` decides partners and calf type for every mooshroom kind. Until that lands, two
  Fungi cows breed with each other (same class) and give a vanilla mooshroom calf, and they do not breed with
  vanilla mooshrooms. Kept: Fungi cows ignore lightning, shear into a cow and 5 of their mushroom, their renderer
  and mushroom layer, entity ids `fungidelight:inky_cap_cow` / `morel_cow`, the mooshroom spawn rule.
- Pig digging asks `ForgeEventFactory.getMobGriefingEvent` before it starts and again before it changes the block;
  when denied the pig neither digs nor turns the block into coarse dirt (upstream ignored `mobGriefing`). The loot
  roll uses the chest parameter set the `gameplay/pig_digging` table declares (upstream passed ORIGIN to the barter set,
  which does not allow it).
- Inky Goo Veil grows to at most 16 segments (`InkyGooVeilBlock.MAX_LENGTH`), for random ticks and bone meal alike;
  bone meal is not accepted on a chain at the cap. Upstream had no cap.
- Weak Stomach: the alcohol side reads `#creativeengineering:food_type/drink/alcohol` (the pack's tag) instead of
  `#forge:alcohol`; any non-water potion still triggers the reaction; `#fungidelight:alcohol_reaction` ships as upstream
  (durian entries already `required: false`).
- Burrowing potions are vanilla `PotionBrewing` mixes (awkward + truffle, + redstone, + glowstone), registered in
  common setup, not Forge `BrewingRecipe`s, so Create's mixer and the pack's potion systems see them.
- Suspicious stews: new recipe type `fungidelight:effect_stew` (shapeless; JSON `"effect"` registry id and `"duration"`
  in ticks, written at craft time with `SuspiciousStewItem.saveMobEffect`; JEI shows the effect). Upstream stored
  numeric `EffectId`s, which are registry slots: inky cap 15 (Blindness in vanilla order), morel 23 (Saturation),
  truffle 35 (no vanilla effect; a modded slot). The lang and the advancement text give no other hint. The port
  follows the owner plan instead: inky cap Luck, morel Comfort (`farmersdelight:comfort`), truffle Haste, 160 ticks
  each (upstream's duration), to review after play.
- Access transformer: only `Pig.FOOD_ITEMS` from upstream (Chicken, Villager, Parrot and structure pool lines were
  unused), plus `PotionBrewing.addMix`.
- Recipes lose the 1.20 `category` field; loot tables lose `random_sequence`; the 1.20 loot condition `any_of` (Inky
  Goo Veil) is 1.19.2's `alternative`; the creative tab key is `itemGroup.fungidelight_tab`.
- mods.toml declares the dependencies under `fungidelight` (the upstream 1.20.1 file put them under `veggiesdelight`).
- 1.19.2 API: Materials (`getMaterial().isReplaceable()` for 1.20's `canBeReplaced()`), a CreativeModeTab subclass,
  `MushroomBlock` with a holder supplier that reads the data-driven huge mushroom feature from the server registry,
  `new SoundEvent`, PoseStack `Vector3f` rotations, `LootContext.Builder`.
- Kept as upstream: villager trades (farmer level 3, wandering trader), the 6 sounds, tooltips, both effects, the
  Hoglin mixin, wolf truffle hunt, pig foods (a `CompoundIngredient` wrap that combines with the current value), truffle
  twins, Create milling recipes (gated on `forge:mod_loaded`).
