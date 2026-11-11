# Animal breeding item audit (with bees and flowers)

Run 2026-11-10 from [plan_breeding_audit.md](plan_breeding_audit.md). TODO `mobs/tweaks #14` and `mobs/bees #1`.
Method: static. `python3 tools/breeding_audit.py` reads every jar's bytecode (76,637 classes), finds classes under a
vanilla animal class and lists the constants of their `isFood` and goal setup; tags come from the 2026-11-10 dumps
in `kubejs/exported/tags/`. Raw output: `kubejs/exported/breeding_audit.json`. Where the answer is computed at run
time (config, lambda, a tag read through a mixin) the row says **check in game**; one scratch row covers them (SQ-BREED-01).

Not covered: the roster is the 131 animals in `spawns.json` (mobs that can spawn); modded animals that never spawn
naturally are absent. Mobs that are not `Animal` (fish, jellyfish, lobster) are not breedable and are skipped.

## 1. Result in short

- **No pack documentation of breeding exists.** No script, tag description or JEI page mentions breeding items. 61 modded
  animals define their own food; 10 vanilla mixins change breeding or taming. Closing `#14` means one generated page
  (section 6), not changed values.
- **7 findings (F-B1 to F-B7)**, one of them a dead tag. None is a crash.
- Bees: **`Bee` food is the item tag `#minecraft:flowers` (160 items), pollination is the block tag (119 blocks)**; the two
  differ in 59 + 18 entries (section 4).

## 2. Vanilla animals (1.19.2, with the pack's changes)

| Animal | Breeds with | Pack change |
|---|---|---|
| Cow, mooshroom, sheep, goat | wheat | Moobloom and the Fungi Delight mooshrooms: wheat (class `FDMCow`, `Moobloom` have no food of their own) |
| Pig | carrot, potato, beetroot | `blueprint:pig_food` is empty (nothing adds mushrooms or truffles; `#13`) |
| Chicken | wheat, melon, pumpkin, beetroot seeds | `blueprint:chicken_food` adds glow berry and sweet berry pips (Berry Good) |
| Horse, donkey | golden apple, golden carrot | none; Domestication Innovation, Allurement, Supplementaries and Survivality mixin the horse classes (taming, not food) |
| Llama, trader llama | hay bale | Vinery trader mule also takes hay bale (own class, breed goal) |
| Wolf | any meat, rotten flesh included | Quark foxhound: bone and coal (tame), breed goal present |
| Cat, ocelot | cod, salmon | `blueprint:cat_food` / `ocelot_food` empty; Cozy's Improved Cats `cozys-improved-cats:cat_food` = 7 raw meats and fish (its own feeding, not breeding) |
| Fox | `#minecraft:fox_food`: sweet berries, glow berries, plus the pack's salmonberries, raspberries, strawberries, white strawberries (8) | the pack extends the tag |
| Rabbit | carrot, golden carrot, dandelion | Domestication Innovation goal adds hay block (tempt) |
| Panda | bamboo | none |
| Turtle | seagrass | none |
| Axolotl | tropical fish bucket | Domestication Innovation: tropical fish items tame |
| Strider | warped fungus | `blueprint:strider_food` empty |
| Hoglin | crimson fungus | `#13` truffle / hoglin foods not applied |
| Frog | slime ball | Domestication Innovation: `domesticationinnovation:tame_frogs_with` = spider eye |
| Bee | `#minecraft:flowers` | section 4 |
| Parrot, skeleton horse, zombie horse, allay | not bred | `quark:parrot_feed` = beetroot seeds (feed, not breed) |

## 3. Modded animals

Food read from the jar (own `isFood`, or the goal setup when `isFood` is inherited). `tag` = a tag the pack dumps.

| Animal (mod) | Breeds / tames with | Source |
|---|---|---|
| Alligator snapping turtle (AM) | cod | code |
| Anaconda | chicken, cooked chicken | code |
| Anteater | leafcutter ant pupa | code |
| Bald eagle (tame) | rotten flesh; `#alexsmobs:bald_eagle_tameables` = fish oil | code + tag |
| Banana slug | brown mushroom | code |
| Bison | wheat; Ingredient list (dandelion leaf is **not** in it) | code; F-B1 |
| Blue jay | `#alexsmobs:insect_items` (3) | tag |
| Capuchin monkey, jerboa, potoo, rain frog, roadrunner | `#alexsmobs:insect_items` | tag |
| Cockroach | sugar | code |
| Cosmaw (tame) | cosmic cod | code |
| Crocodile, komodo dragon, fly, Tasmanian devil | rotten flesh (devil also any `FoodProperties` meat) | code |
| Crow (AM) | pumpkin seeds (tame); `#alexsmobs:crow_foodstuffs` (47) eaten | code + tag |
| Elephant (tame) | acacia blossom; `#alexsmobs:elephant_foodstuffs` (81) | code + tag |
| Emu | wheat | code |
| Endergrade | chorus fruit | code |
| Flutter | bone meal | code |
| Froststalker | porkchop, cooked porkchop | code |
| Gazelle | wheat, acacia blossom | code |
| Gelada monkey | dead bush | code |
| Gorilla | banana (`isBanana`); `#alexsmobs:gorilla_foodstuffs` (38) | code + tag |
| Grizzly bear (AM) | salmon | code |
| Hummingbird | `#minecraft:flowers` (160 items, all of section 4) | tag; F-B5 |
| Kangaroo, rhinoceros | dead bush, grass | code |
| Laviathan | mosquito larva | code |
| Maned wolf | apple, `allFoods` list | **check in game** |
| Mantis shrimp, mudskipper | lobster tail (cooked), mudskipper also insects | code |
| Mimic octopus | tropical fish | code |
| Moose | dandelion | code |
| Mungus | mungal spores | code |
| Orca | salmon | code |
| Platypus | lobster tails (`#platypus_foodstuffs`, 27) | code + tag |
| Raccoon | bread (`#raccoon_foodstuffs` = eggs, sugar) | code + tag |
| Rattlesnake | any meat | code |
| Seagull | cod | code |
| Seal | lobster tail (`#seal_foodstuffs`, 23) | code + tag |
| Shoebill | `#shoebill_foodstuffs` (21) | tag |
| Skunk | sweet berries | code |
| Snow leopard | moose ribs (cooked) | code |
| Sugar glider | honeycomb | code |
| Tarantula hawk | fermented spider eye | code |
| Terrapin | seagrass | code |
| Tiger | `#alexsmobs:tiger_breedables` = acacia blossom | tag |
| Toucan | egg | code |
| Tusklin | red mushroom | code |
| Warped toad | mosquito larva | code |
| Grizzly bear (Buzzier Bees) | `#buzzier_bees:grizzly_bear_food` = salmon, cooked salmon, salmon bucket | tag |
| Chimpanzee (Neapolitan) | `#neapolitan:chimpanzee_food` = banana bunch | tag |
| Crow (Hearth and Harvest port, CD) | `#creativedelight:crow_food` (43) | tag |
| Duck, goose (Untitled Duck) | own `BREEDING_INGREDIENT` | **check in game** (set in a Forge subclass) |
| Goose (Upgrade Aquatic) | chicken seeds (extends `Chicken`) | inherited |
| Little gingerbread man (Create Confectionery) | sugar | code |
| Crab, Toretoise (Quark) | `Crab.getTemptationItems`, `Toretoise.getGoodFood` | **check in game** |
| Shiba (Quark, disabled) | meat | code |
| Emu, bison, Vinery mule | wheat, wheat, hay bale | code |
| Spectre, leafcutter ant, sunbird, cachalot whale, enderiophage | not breedable by hand (no food) | code |

## 4. Bees and flowers

**Mechanics (verified in jars).** Breeding: `Bee` takes any item in `#minecraft:flowers`. Pollination: a block in the
block tag `#minecraft:flowers`. Botania's `BeeMixin` (`isSpecialFlower`) also accepts block tag `botania:special_flowers`,
so Botania's functional flowers are pollinated without being in the block tag. Immersive Weathering's `BeeMixin` adds
a goal following a flower crown; Buzzier Bees' `BeeEntityMixin` only changes hive entry (sunny effect, weather).
The 2026-10-21 brief said Buzzier Bees' `flower_blacklist` stops pollination; it does not. It is read by the bone meal
handler `BBEvents.renewableFlowers` (bone meal on a flower in a non-listed tag drops another flower); the tag
lists wither rose and every Botania flower (Botania adds them).

| Set | Items | Blocks |
|---|---|---|
| `#minecraft:flowers` | 160 | 119 |
| `#small_flowers` | 122 | 81 |
| `#tall_flowers` | 22 | 22 |

**Item tag only (breeds bees, block not pollinated unless noted):** 59 entries. 52 Botania functional and special flowers
(chibis included; pollinated through the Botania mixin), `alexsmobs:acacia_blossom`, Immersive Weathering azalea flowers
and flower crown, Vintage Delight wild cucumbers, ghost peppers, oats and peanuts (**blocks untagged**, F-B3).
`immersive_weathering:flower_crown` and the leaf-pile items are not flowers and are in the tag (F-B4).

**Block tag only (pollinated, cannot be fed):** 18: the 13 Flower Patch patches (no item), `corn_delight:wild_corn`,
`miners_delight:wild_cave_carrots`, `supplementaries:wild_flax`, `hearthandharvest:four_leaf_clover`,
`twigs:azalea_flowers`. Correct as is, except F-B3.

**Wild crops are flowers on purpose** (Farmer's Delight, Rustic Delight, Veggies, H&H, Delightful): 24 wild items breed
bees. Keep, document (F-B6).

**Plant-like pack blocks in no flower tag** (candidates, need a look at the block class to confirm):
Botania shiny flowers (16 colours), `buzzier_bees:four_leaf_clover` (item only), `vinery:creepers_crush`,
`fireflybushport:firefly_bush` (a `FlowerBlock`, no tags), Floral Enchantment: `dancing_sunflower` and the 17 `*_bush`
variants of vanilla flowers, Upgrade Aquatic pickerelweed (aquatic, leave out), Botania black lotus and blacker lotus
(Botania items, not placed). Floating flowers and the `*_basket` blocks are decoration and stay out.

## 5. Findings

| ID | Finding | Proposal |
|---|---|---|
| F-B1 | Veggies Delight member tag `data/alexmobs/tags/items/bison_breedables.json` uses namespace `alexmobs`; Alex's Mobs uses `alexsmobs` and its bison has a fixed list. Dandelion leaf does not breed bison. Dead tag. | Delete the file, or move to `alexsmobs:` only after checking that the bison reads a tag. Owner call. |
| F-B2 | Four empty Blueprint food tags (`pig_food`, `cat_food`, `ocelot_food`, `strider_food`) while `#13` wants mushrooms and truffles for pigs and hoglins. | Fill in `#13`; one `global.tags` block. |
| F-B3 | Vintage Delight wild cucumbers, ghost peppers, oats, peanuts are in the item flower tag but their blocks are not in the block tag. Bees breed with them and never pollinate them; the other wild crops are in both. | Add the 4 blocks to the block tag; the Vintage member ships its tags as jar data, so edit its `minecraft/tags/blocks` file there. |
| F-B4 | `immersive_weathering:flower_crown` and `flowering_azalea_leaf_pile` / `azalea_flower_pile` are in `#minecraft:flowers` (item). A hat breeds bees. | Owner call; flower crown is wanted by the follow-crown goal. |
| F-B5 | Alex's hummingbird feeds on `#minecraft:flowers`: it eats any flower, wild crops, hats included. | Own tag `creativeengineering:hummingbird_flowers` if F-B4 is fixed, else accept. |
| F-B6 | Wild crops (24) and Botania specials (52) breed bees; nothing tells the player. | One JEI info page on the bee (section 6). |
| F-B7 | Untagged flower-like pack blocks (section 4 list): shiny flowers, four-leaf clover (BB), Creeper's Crush, firefly bush, Floral Enchantment bushes and dancing sunflower. Bees ignore them. | Check each block's class (SQ-BREED-01), then tag block and item in one `global.tags` block in `kubejs/server_scripts/tags/bee_tags.js`. Wither rose stays in, bees pollinate it in vanilla. |

## 6. What closes the TODO rows

- `#14` (items every animal breeds with, JEI documents the options): a generated page, one row per animal (sections 2 and
  3, from this audit plus the live dump). Needs a design call: a Creative Devtools JEI category, or a JEI information
  recipe on each spawn egg. Not built here.
- `#1` (bee flowers): decide F-B3, F-B4, F-B7 (small tag edits, `/reload`).

## 7. Scratch row (queued, not run)

SQ-BREED-01 (in [scratch_queue.md](testing/scratch_queue.md)): add `/dumpbreeding` to Creative Devtools (for each animal
type, `isFood` for every registered item; for bees, block class and both flower tags), run, replace the **check in
game** rows and the F-B7 candidates with measured data.

## 8. Owner decisions applied 2026-11-10

| Decision | Done |
|---|---|
| Dandelions out of bison breeding (F-B1) | The dead Veggies tag `alexmobs:bison_breedables` deleted from the member source (bison breeds on wheat only, verified in bytecode); takes effect with the next Creative Delight build. Just Enough Breeding's bison page already lists wheat only. |
| Pig, cat, ocelot: vanilla tables (F-B2) | `blueprint:pig_food` = carrot, potato, beetroot; `cat_food`, `ocelot_food` = cod, salmon, in [animal_tags.js](../kubejs/server_scripts/tags/animal_tags.js) (`/reload`). Blueprint merges them with vanilla. Strider food left empty (not asked). |
| No wild plant breeds bees (F-B3, F-B6) | Every item with a `wild_` path leaves `#minecraft:flowers`, `#small_flowers`, `#tall_flowers`; also feeds hummingbirds no longer. Mature dandelion (a flower) stays. |
| Wild onion, carrot, potato still pollinate | Only these three wild blocks stay in the block flower tag; every other wild block leaves it. |
| Flower crown and leaf piles out (F-B4, F-B5) | Removed from the item flower tags, so bees and hummingbirds ignore them. |
| Botania special flowers and shiny flowers do not breed bees | `#botania:special_flowers` removed from the item flower tags; shiny flowers were never in a tag. Botania's mixin still lets bees pollinate special flowers. |
| F-B7 blocks need no flower tag | Nothing to do; F-B7 closed. |

### Just Enough Breeding (JEBr 1.4.0)

JEBr keeps a hard-coded table per supported mod and reads `#` tags live, so the bee page (`#minecraft:flowers`), fox, and every
tag-driven animal follow the tag changes above after a `/reload` and a JEI refresh. Checked against the jars: the vanilla,
Alex's Mobs, Quark, Neapolitan and Upgrade Aquatic tables match the pack (one gap class: JEBr lists 1.20 entries, camel and
sniffer, that do not exist in 1.19.2 and show nothing). Pack animals with no JEBr page, now added by
[BreedingPagesJEI](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/jei/BreedingPagesJEI.java)
(Creative Compat, builds JEBr recipes by reflection, skips what is absent):

| Animal | Food |
|---|---|
| Untitled Duck duck | wheat, melon, pumpkin, beetroot seeds (read from the mod's classes) |
| Untitled Duck goose | the same plus kelp and seagrass |
| Buzzier Bees grizzly bear | `#buzzier_bees:grizzly_bear_food` |
| Fungi Delight inky cap cow, morel cow | wheat |
| Cozy's Improved Cats cat | cod, salmon |

Not added: Moobloom (its own pages, [mooblooms.md](mooblooms.md)), crows (taming, not breeding), Quark crab and Toretoise
(food computed at run time, SQ-BREED-01), the little gingerbread man (heals, does not breed).
