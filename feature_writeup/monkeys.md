# Monkeys and apes

Reference for every monkey and ape mob in the pack, so each can be kept or cut. Decision is open: TODO `merges/mobs #1`
([brief](briefs/mobs_tweaks.md)). Spawn numbers are from [mob_spawns.md](mob_spawns.md) and the configs. Behavior marked
"recalled" comes from memory of the mods, not a readable source.

## Comparison

| | Capuchin Monkey | Gorilla | Gelada Monkey | Chimpanzee |
|---|---|---|---|---|
| Mod / id | Alex's Mobs `alexsmobs:capuchin_monkey` | Alex's Mobs `alexsmobs:gorilla` | Alex's Mobs `alexsmobs:gelada_monkey` | Neapolitan `neapolitan:chimpanzee` |
| Kind | monkey | ape | monkey | ape-like monkey (Neapolitan calls it chimpanzee) |
| Spawn source | biome spawn | biome spawn | biome spawn | banana plant feature, no biome spawn |
| Weight / group | 28, groups 9-16 | 25, groups 7 | 5, groups 9-16 | 25 % chance per banana plant, 12-24 attempts, up to 10 |
| Config | `capuchinMonkeySpawnWeight` | `gorillaSpawnWeight` | `geladaMonkeySpawnWeight` | `Chimpanzee group chance` and 3 more, neapolitan-common.toml |
| Biomes | 9 jungle-type biomes (Jungle, Sparse Jungle, Terralith rocky / tropical / amethyst / jungle mountains), Skylands Summer, Mangrove Swamp | jungle biomes, same set as the capuchin (8 biomes) | 7 highland / meadow / shrubland biomes (plains + plateau, Terralith Highlands, Alpine Highlands, Steppe, Valley Clearing, Hot and Rocky Shrubland, Meadow) | wherever banana plants generate: the 4 dense jungles (common), other warm wet forests (uncommon) (`wild_crops.json`) |
| Spawn rule | leaves, grass, logs or air; light above 8; ground or leaves | leaves, grass, logs or air; light above 8; ground | grass block only; light above 8; ground | with the plant |
| Tameable | yes: `#alexsmobs:bananas`, 1 in 5 | yes: bananas, several feeds; then sit / wander | no | no |
| Tamed role | shoulder pet, follows / stays / wanders, melee and ranged (throws stones / Ancient Dart, recalled) | follows, defends its owner (recalled: strong melee) | none | none |
| Wild behavior | group monkeys, eats bananas | peaceful troop with a Silverback leader (2-3x as strong), chest pounding, babies ride on parents, strong if provoked | neutral herd with a leader; leaders stage mock fights, grooming, resting, lip-flip threat; eats grass and clears it | ambient group: grooming, flips, crying, jumps on bouncy blocks, opens banana bunches, scared by explosions |
| Foods | bananas (taming), eggs heal, maggots breed (favorite) | bananas | wheat (entices) | `#neapolitan:chimpanzee_food` (banana bunch), `chimpanzee_snacks` |
| Drops / extras | occasional banana peel when eating a banana (feeds the Sopa De Macaco dish, Alex's Delight) | loot table (contents not read) | loot table (contents not read) | head block, banner pattern, spawn egg |
| Items | spawn egg, Ancient Dart | spawn egg | spawn egg | spawn egg, Bananarrow (sends chimps after the mob it hits), head, banner pattern |

## World and mod integration

- **Capuchin / Gorilla:** both live in the same jungles and share the Alex's Mobs banana tag. The gorilla breaks blocks
  from `#alexsmobs:gorilla_breakables` (`#minecraft:leaves`, bamboo); the exact use is not confirmed. Both eat Neapolitan
  bananas through `#forge:fruits/banana`.
- **Gorilla:** the Alex's Mobs advancement `tame_gorilla` is dropped by the pack (no advancements).
- **Capuchin:** Cloud Storage balloons are in `#alexsmobs:monkey_target_with_dart`. Alex's Delight adds the peel dish.
- **Gelada:** shares meadow and shrubland with bison, raccoon, crow and Quark's shiba (weight 40). No cross-mod item
  links. Block tag `#alexsmobs:gelada_monkey_grass` lists what it grazes.
- **Chimpanzee:** tied to Neapolitan banana plants, so the pack's banana placement decides where it appears.
  Turning chimps off leaves the bananas. Its banner pattern is in `stack_sizes.json`; none of its items are in
  `item_dict.js` yet (the Bananarrow is).
- **Pack spawn tooling:** capuchin, gorilla and gelada appear in the generated spawn tables; spawn tuning
  (`mob_spawns.json` scale / remove) can adjust them. The chimp is not a biome spawn, so only its config applies.

## Overlap

Capuchin vs chimpanzee (same jungles, same bananas) is the one the brief targets. The gorilla also shares jungle
and bananas with the capuchin but is a large defender, not a duplicate. The gelada is a different habitat.

## Cut options

| Option | Change | Hide | Lost |
|---|---|---|---|
| Drop chimpanzee | `"Chimpanzee group chance" = 0.0` | chimp egg, Bananarrow (and recipe), head, banner pattern | ambient banana-grove life, Bananarrow |
| Drop capuchin | `capuchinMonkeySpawnWeight = 0` (both copies of alexsmobs.toml); raise chimp chance to compensate | capuchin egg, Ancient Dart | shoulder pet, dart, banana peel dish |
| Drop gorilla | `gorillaSpawnWeight = 0` | gorilla egg | the large jungle defender / pet |
| Drop gelada | `geladaMonkeySpawnWeight = 0` | gelada egg | one rare herd animal, no overlap |

Existing mobs stay in the world unless cancelled with `EntityEvents.spawned` (skip tamed with `isTame()`). Verify a
cut with `/dumpspawns` and MT-30. Hand-edited configs also go to `configs_edited/root_config/`.
