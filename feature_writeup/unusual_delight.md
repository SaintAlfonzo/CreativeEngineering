# Unusual Delight: overlap analysis

Survey for TODO `largecontent/mod-additions #8`. Written 2026-10-30 from `devmods/createiveDelight/unusual_delight-5.0.jar`
(data, lang and bytecode read; nothing built, nothing installed). The earlier summary is
[hostile_delight.md](hostile_delight.md) section 3.4; this file replaces it as the reference.

## 0. At a glance

| Bucket | Items | Section |
|---|---|---|
| Blocked: needs Ice and Fire (not in the pack) | 35 | 2.1 |
| Blocked: needs Alex's Caves (not in the pack) | 9 | 2.2 |
| Blocked: needs 1.20+ vanilla content (sniffer, torchflower, pitcher plant) | 11 | 2.3 |
| Blocked: no source at all (creative tab only, unfinished upstream) | 9 | 2.4 |
| Overlap: same concept as Hostile Delight (installed, integration planned) | 35 | 3.1 |
| Overlap: same item or concept in another installed mod | 30 | 3.2 |
| New content the pack can build today | 177 | 4 |
| **Total** (registered items, block items and eaten-state blocks included) | **306** | |

Read: 64 items (21 %) cannot exist in the pack without new mods, 65 (21 %) duplicate something, 177 (58 %) are new.
Of the new ones, almost all land in a pack system that already has rules (baking, ice cream, salt, sweeteners, drinks,
jars, crates, loot), so very little of it can go in as shipped (section 5).

## 1. The mod

| Fact | Value |
|---|---|
| Id, version, loader | `unusual_delight` 5.0, Forge 1.20.1 only, MCreator-generated |
| Licence | Academic Free License 3.0 (same as Hostile Delight): not GPL-compatible |
| Registry | 306 items (26 of them blocks or block states), 14 mob effects, 2 creative tabs, villager trades, fuels |
| Data | 345 recipes (crafting, smelting / smoking / campfire, FD `cooking` and `cutting`, one blasting), 20 advancements, 12 tag files, 3 vanilla loot tables |
| Mandatory deps | `minecraft` only in `mods.toml`, but about 60 recipes use Farmer's Delight (pot, board, rice, cabbage, tomato sauce, wheat dough) |
| Optional content | Ice and Fire (29 ids), Alex's Mobs (11 ids, all present in the pack), Alex's Caves (5 ids) |
| Code | about 150 on-eat procedures (the effects of most foods live in code, not food properties), a kill-event drop procedure, block click procedures |

### 1.1 How raw materials enter the game

| Source | Gives | Pack note |
|---|---|---|
| Kill-event procedure (code, not loot tables) | spider: Spider Leg, Egg Sac; cave spider: Egg Sac, Spider Leg; zombified piglin and zoglin: Raw Zorkchop; drowned: Soggy Flesh; endermite: Raw Mite Meat; silverfish: Silver Cube; warden: Warding Soul; snow golem: Living Snowball; strider: Cold Strider Shank | Pack rule is loot through LootJS / modifiers (CD `miners_delight/loot.js` pattern); code drops bypass looting, Lootr, loot audits |
| Kill procedure (`Soulearn`) | Soul, from killed mobs | Same |
| Whole-table replacement | `minecraft:entities/ghast` (Ghast Tentacle + gunpowder), `phantom` (Raw Phantom Meat), `sniffer` | Hostile Delight already replaces the ghast table (hostile_delight.md D5: undo); two mods would fight over it |
| Right-click procedures | bottle on chorus flower: Chorus Honey Bottle; rotten flesh on powder snow / ice: Frozen Flesh | Code |
| Knife on the board | Sculk Chunk (sculk), Shroomlight Slice, Wart Kernels, Prepared Sea Pickle, Rotten Ham (rotten flesh), Chunk of Glacier (blue ice), salt (quartz) | Easy to carry over through NCH |
| Villager trades | Salt, Sugar Cube, Ink Bread, Glow Ink Bread, Plain Cookie, Refined Gelatin | Check against `villager_trades.json` policy |

### 1.2 Effects (all 14 are new to the pack; none exist under any id)

Vitality, Soul Satisfy, Soul Ember (damage over time), Haunted (damage over time), Spiked (damage over time), Sunburn
(sets on fire), Frostbitten (Slowness, self-renewing), Sculked (strips Night Vision and similar, applies Darkness),
Infested (spawns spiders, cave spiders or silverfish when it ends), Brilliance, Winter's Bless, and the three
dragon "aspects" (Ember, Crystal, Lightning: Ice and Fire foods only, so blocked with 2.1). Every one needs code,
a JEED description (`ui/effects #1`) and a place in the effect overlap groups (`potions/potions #8`). Infested has the same
name as 1.21's vanilla effect; Frostbitten sits close to powder-snow freezing.

### 1.3 Upstream bugs found

- `minecraft:nether_quartz` (does not exist; should be `quartz`) in the Ghast Bloodade pot recipe and Gorgon Steam:
  the bloodade pot route fails at load.
- Dried Berry's second recipe takes `minecraft:sweet_berry_bush` (a block with no item): never craftable.
- Soul Roll's recipe asks for `unusual_delight:frost_sugar` (the item is `frostsugar`): fails at load.
- Blazing Salt has a self-multiplying recipe (1 Blazing Salt + blaze powder + glowstone dust -> 2) beside the real one.
- Names: "Bacon And EGG", "Neturalized Meal", "Silmy Acid Bottle", "Trillo Roll", effect "Soul_Ember", id `iced_macarong`.

## 2. Blocked content (64 items)

### 2.1 Ice and Fire (35)

Dragon chunks (fire, ice, lightning, mixed) and everything made from them: Dragon Loaf (placed, 3 states + bowl),
Neutralized Meal, Charging Curry, Stir Fried Lightning Dragon, Draco Pasta, Everfrost Fudge, Smoldering Cheesewheel and
Lavapool Tart, Dragonscale Crumble, Dragon Ice Cream and All-Seeing Ice Cream; deathworm foods (Grub, Risotto, two fried
eggs, Tongue Sandwich); myrmex foods (Desert Fry, Jungle Roast); Gorgon Steam, Siren Schnapps, Wing Stew, Copper Feather
Soup, Talon Powder, Scale Chip, Witherbone Broth, Nemo Muffin (cyclops eye), Lightning Sourdough and Lightning Cheesecake
(lightning dragon blood).

Two of these could be rerouted onto installed meats if wanted: dragon foods onto End's Delight `raw_dragon_meat` (End's
already owns dragon meat, cuts, steak, leg), and the ice-cream pair once dragon ice exists. Otherwise skip.

### 2.2 Alex's Caves (9)

Amber Apple, Amber Cookie, Ambercookiocity (amber, amber curiosity), Bone Marrow and Cooked Bone Marrow (heavy bone),
Fiddlehead Cookie, Raw / Cooked Trilocaris Meat and Trilo Roll. Skip, or reroute Bone Marrow onto plain bones (Miner's
Delight already has bone broth).

### 2.3 1.20+ vanilla (11)

Sniffer: Raw / Cooked Sniffer Meat, Fried Sniffer Egg, Sniffer Sizzle, Steak and Pod (also torchflower and pitcher pod),
Bacon And Egg (fried sniffer egg). Plants: Diced Torchflower, Diced Pitcher Plant, Nightmare Jam and Pearl Jam (pitcher
plant), Silmy Acid. No sniffer, torchflower or pitcher plant in the pack. Sculked Egg also has a sniffer-egg recipe, but
its egg and turtle-egg recipes work, so it is in section 4.

### 2.4 No source (9)

Hungrymaker, Ender Pearlsicle, Enchanted Honeycomb (has a use procedure, no recipe), Foul Fish Stew, Bony Fish Soup,
Expired Patty, Cooked Expired Patty, Ghastew, Dino Kebab (placed block, no item recipe). Upstream left these unfinished.
Port only if a recipe is designed for them.

## 3. Overlaps (65 items)

### 3.1 Hostile Delight (35)

Hostile Delight 0.2.2 is installed and its integration is planned, not built (hostile_delight.md, `mod-additions #6`). It
reads as an older branch of the same idea; 35 Unusual items are the same food under a new id:

| Unusual | Hostile | Note |
|---|---|---|
| Raw / Cooked Mite Meat, Carrotmite, Mitey Meal, Magic Butter, Mite Skewer's meat | `rawendermite`, `cookendermite`, `carrotmite`, `miteymeal`, `magicalbutter` | Endermite meat is already in three mods (End's `raw_ender_mite_meat`, Ender's `mite_crust`, Hostile); hostile_delight.md D2 keeps End's. Unusual would be the fourth |
| Ghast Tentacle, Ghast Blood, Ghast Brew, Ghast Pudding, Ghast Roll, Ghast Cream | `ghasttentacle`, `ghastblood`, `ghastbrew`, `ghastpudding`, `ghastroll`, `ghastbloodicecream` | Both replace the ghast loot table |
| Glow Gummy (+2 eaten states), Glow Ink Sauce, Glow Ink Bread, Glow Apple | `glowgummy`, `glowingsauce`, `glowbread`, `glowapple` | Glow Ink Sauce also overlaps Miner's `glow_ink_pasta` chain |
| Candied Slime (+block), Slime Stew, Slime Syrup, PopsiClime, Magma Gummy | `candiedslime`, `candiedslimeblock`, `slimestew`, `silmesyrup`, `popsiclime`, `magmajelly` | |
| Bone Gelatin, Refined Gelatin | `bonegelatin`, `refinedgelatin` | Also Nether's `magma_gelatin`, Quark Delight's sweet gelatine |
| Rotten Ham, Cooked Rotten Ham | `rottenham`, `cookedrottenham` | |
| Fried Turtle Egg, Turtle Egg Sandwich | `friedturtleegg`, `turtleeggsandwich` | |
| Frozen Flesh | `frozen_flesh` | |
| Blazing Apple, Blazing Ale, Acid Soda | `blazingapple`, `blazingale`, `soda` | D15 renames Hostile's Soda; Unusual's is also made from dragon's breath, like End's Dragon Breath Soda |
| Prepared Sea Pickle, Sea Pickle Tea | `preparedseapickle`, `seacucumbertea` | Also Oceanic `sea_pickle_slices`, pack `pickled_sea_pickle` (D14) |

Hostile's D1 already said: integrate Hostile now, and any later Unusual port merges onto Hostile's items. That holds:
these 35 should not be ported as new items. Unusual's versions bring only recipe or value ideas.

### 3.2 Other installed mods (30)

| Unusual | Pack item(s) | Fold |
|---|---|---|
| Salt, Salt Bag | merged pack salt (item_merges.md 2.9, all salt is Vintage pink), `salt:salt_block` | Do not add; `#forge:any_salt` recipes take the pack salt tag. Salt from quartz and from smelting Soggy Flesh: decide |
| Salted Fish | Vintage `salted_cod`, `salted_salmon`, Salt's salting | Salting system (`food/seasoning`) already covers it |
| Sugar Cube | `supplementaries:sugar_cube` (9 sugar) | Use Supplementaries' |
| Spider Leg | `miners_delight:spider_leg`, `baked_spider_leg` | Use Miner's; Unusual's leg drop dropped |
| Spider Barbecue, Arthropod Barbecue | Miner's `improvised_barbecue_stick`, `arthropod` line | Close concept; skip or keep one as a recipe on Miner's items |
| Silver Cube, Cooked Silver Cube | Miner's `silverfish_eggs` | Second silverfish drop; pick one |
| Braised Sea Pickle | `oceansdelight:braised_sea_pickle` (same id path) | Use Ocean's |
| Apple Juice, Melon Juice | `vinery:apple_juice`, `farmersdelight:melon_juice` | Use theirs (juice family, `merges/drinks #1`) |
| Cherry Blossom Cookie | `rusticdelight:cherry_blossom_cookie` | Use Rustic's (Unusual's needs pink petals anyway) |
| Strider Shank, Cold Strider Shank, Chopped (Cold) Strider, two Strider Jerkies | Nether's Delight `strider_slice`, `ground_strider`, `grilled_strider`; Brewin' `jerky` | Strider meat is Nether's Delight's; jerkies could be recipes on `strider_slice` |
| Ember Kimchi | Brewin' and Vintage `kimchi` | Variant; keep only if the kimchi family wants a nether variant |
| Warped Vinegar | Vintage `vinegar_bottle` / `vinegar_jar` | Variant of an existing ingredient; prefer one vinegar |
| Cane Syrup | Rustic `syrup` | Second syrup |
| Blue Cheese | Brewin' cheese wheels (flaxen, scarlet) | New cheese from sculk + salt: fits Brewin's aging if kept |
| Rotten Flesh Crate | Architect's Palette `rotten_flesh_block`, planned pack prepared flesh block (item_merges.md 5.5) | Skip |
| Zombie Zerky | `jrftl:prepared_flesh` (rotten flesh made usable) | Same role; skip |
| Scarlet Salad, Cyan Salad | `farmersdelight:nether_salad` (crimson + warped fungus) | Split versions of FD's salad; skip or keep as variants |
| Cosmic Cod Slice, Flying Filet (raw and cooked) | `canned_flying_fish`, Alex's Delight cuts pattern | Alex's Mobs fish have no slices yet; low-risk adds, but they follow the slices rule of the fish family |

## 4. New content (177 items)

Grouped by theme. "Lands in" names the pack system that must take it before it can ship.

| Theme | Items | Lands in |
|---|---|---|
| **Sculk** (13) | Sculk Chunk, Sculked Egg, Spectral Egg, Sculk Cookie / Candy / Donut / Pudding, Sculk And Egg, Deep Skewer, Silent Bread, Infected Apple, Charged Lasagna, Warding Soul | New food family; Sculked effect; baking (cookie, donut) |
| **Soul** (14) | Soul, Soul Sack, Soul Stew / Tonic / Wine / Roll / Dango / Jelly Bottle / Parfait / Sand Cookie, Glazed Soul, Depressive Soul, Soulburn Soda, Soul Satisfy and Soul Ember effects | New drop (loot), drinks, jars, effects |
| **Pale / ghast** (10) | Pale Apple / Biscuit / Biscuit Stack / Bread / Breakfast / Dumplings / Mash / Pop, Ghast Toast, Ghast Bloodade | Built on Hostile's ghast tentacle and blood after 3.1; `#forge:pale` tag |
| **Frost** (27) | Chunk of Glacier, Frostsugar, Fried Ice, Frozen Apple, Frozen Ham, Icy Bread, Iceberg / Snowball / Snowflake / Snowman Cookie, Iced Macaroon, Cold Pudding, Ice Spikes In The Bowl, Snoburger, Snow Head (placed feast, 5 states + bowl), Snowman Slush, Taiga Fresca, Tundra Skewer, Glacier Tequila, Spiked Dumpling, Chilled Dragon's Breath, Popice, Mossicle, Phantomsicle, PopShroomlight, Living Snowball | Ice cubes (`#forge:ice_cubes`, `dishes/ingredients #5`), cold sweets / popsicles (`baking/sweets #3`), feasts, Frostbitten and Winter's Bless effects |
| **Blaze and magma** (15) | Ardent Coal (also a fuel), Blazing Salt, Blazing Whipped Cream, Blazing Ice Cream, Netherpolitan Ice Cream, Cool Magma Cream, Hot Creamsoup, Magma Mimic, Cream-Coated Bread, Creamy Nectar, Sizzling Oil Bottle, Searing / Stacked Stroopwafel, Smoldering Steak, Volcanic Feast, Magma Cream Crate | Seasoning (Blazing Salt as a seasoning?), ice cream rework, crates policy, fuels |
| **Nether plants** (17) | Wart Kernels, Candied Wart, Wart Bowl, Pickled Wart Bowl, Scarlet Jelly, Scarlet Tea, Wart Cookie, Tricolor Salad, Sprout Salad, WarpBurger, Crimson Bread, Crimson Donut, Shroomlight Slice / Cookie, Golden / Pickled Shroomlight Slice, Piglin Pudding, Red Plate, Striding Stew | Nether's Delight sits next to this; pickles family; baking |
| **Undead and rot** (14) | Raw / Cooked Zorkchop, Zombified Bacon (raw and cooked), Rotten Sausage, RotDog, Rotburger, Moldy Bread, Gnawed Skewer, Flesh Donut, Ghoulash, Soggy Flesh (+crate), Stinky Coffee, Pickled Poisonous Potato, Green Bacon And Eggs | New drops (zombified piglin, zoglin, drowned); rotten flesh line (prepared flesh merge); coffee merge (item_merges.md 2.8) |
| **Arthropod** (5) | Egg Sac (also places a cave spider), Spider Bun, Spider Mash, Spider Patty, Mite Skewer | On Miner's spider leg and End's mite meat after 3.1 / 3.2 |
| **Phantom** (2) | Raw / Cooked Phantom Meat | New meat; phantom table is replaced whole upstream (pack: modifier) |
| **Apples and juices** (9) | Amethyst Apple, Glowden Apple, Golden Apple Juice, Enchanted Golden Apple Juice, Chorus Honey Apple, Chorus Honey Bottle, Slime Toffee Apple, Candied Berry, Dried Berry | Golden food family (`goldenfood`), juice family, End's / Ender's chorus lines, rock candy amethyst decision (`baking/sweets #2`) |
| **Baking** (16) | Plain Cookie, Lapis / Clover / Glow Berry / Slime / Candied Slime Cookie, Allium / Cornflower / Dandelion / Lily Of The Valley Cake, Pumpkin Bread, Bowl of Pumpkin Mousse, Pickle Pie, Ink Bread, Seeking Bread, Slime-Coated Bread, Sandwichsandwich, Ocean Bread, Starry Sandwich | Oven only, flour not wheat (`cooking/oven #6`); filled breads clash with `baking/baked-goods #33` |
| **Sugar and honey materials** (9) | Sugar Bag, Rough / Refined Sugar Glass (+panes), Honey Glass (+pane), Empty Honeycomb Block, Glowing Nectar | Building blocks: BlockRunner tier, Sweeteners (`dishes/ingredients #3`), crates and bags policy |
| **Misc** (17) | Ink Sauce, Starry Jam, Pond In A Bowl, Fried Bamboo, Diced Cactus, Cactus Ale, Hairy Feast, Wither Rose Tea, Mungal Spore Cookie, Mysterious Worm Cocktail, Cut Lost Tentacle, Roast Tentacle, Fish Blob, Fried Crocodile Egg, Supreme Skewer | Alex's Mobs items (all present): Cut Lost Tentacle, Fish Blob, Fried Crocodile Egg, Supreme Skewer, worm and spore foods. Cactus line overlaps Delightful's cactus flesh / steak |

(Counts per theme are approximate where an item fits two themes; the bucket total of 177 is exact.)

## 5. What would need folding in

In the order a port would hit them. Each line names the pack rule that applies.

1. **Licence and form.** AFL 3.0 cannot go into the GPLv3 Creative Delight jar. Hostile is handled as an untouched jar plus
   KubeJS / patcherMod. Unusual is 1.20.1 only, so there is no untouched jar to install: either an AFL-licensed backport
   jar kept separate (its own `mods/` entry and notice), or chosen items re-made as pack items with our own code and the
   art under an AFL notice (as `glowing_calamari_art_notice.txt`). Decision U1.
2. **Hostile first.** Section 3.1 means the Hostile integration (`mod-additions #6`) should be built before any Unusual
   work, and Unusual's 35 duplicates are then dropped, not merged.
3. **Item ids and helpers.** Every kept item goes into `item_dict.js` / `global.item`; every recipe through NCH,
   tags through `global.tags`. None of Unusual's 345 recipe JSONs carry over as files.
4. **Loot.** The kill-event drops (9 mobs) and the whole-table ghast / phantom / sniffer replacements become LootJS
   modifiers (CD `miners_delight/loot.js` pattern), with knife / looting rules like the squid chain
   (ocean_delight.md 5.3). Mob overlaps to settle: endermite (now four items), spider (Miner's), silverfish (Miner's),
   strider (Nether's), ghast (Hostile), drowned / zombie flesh (rotten flesh line).
5. **Salt, sugar, ice.** `#forge:any_salt` -> pack salt; `#minecraft:any_sugar` -> the sweetener tiers;
   `#forge:freeeze_materials` (sic) -> `#forge:ice_cubes`. Blazing Salt either joins the seasoning system as a new
   seasoning (overlay icon, AppleSkin line, `food/seasoning #9`) or stays an ingredient.
6. **Baking overhaul.** 16+ cookies, cakes, breads, the stroopwafels and biscuits: oven recipes with flour, no grid route.
   The filled breads (Ink, Glow Ink, Slime-Coated, Cream-Coated, Crimson, Pale, Silent, Seeking) contradict
   `baking/baked-goods #33` (filled breads dropped from the bread family). Decision U4.
7. **Sweets families.** Ice creams into the ice cream rework (`baking/sweets #4`: fluid per flavour, block per flavour);
   popsicles into the cold sweets family (`#3`); gummies (`#1`); Amethyst Apple vs the amethyst rock candy (`#2`).
8. **Drinks.** Ales, Soul Wine, Glacier Tequila, Siren Schnapps, Cactus Ale: alcohol belongs to the reserved Creative
   Brewing line (memory: CD routing), and Brewin' intoxication rules apply if they are alcoholic. Teas and juices into
   the tea / juice families (`merges/drinks #1`). Bottles: stack-size special bottle policy.
9. **Jars and crates.** Jams and jellies (Starry Jam, Soul Jelly) into the jars list (`items/jars #3`); Sugar Bag,
   Salt Bag, Soul Sack, crates into the crates-and-bags rules (item_merges.md 5.5).
10. **Placed foods.** Snow Head (5 bites), Glow Gummy (3 bites), Dragon Loaf, Dino Kebab: feast / placeable rules,
    Display Delight, WTHIT.
11. **Effects.** 14 effects: code, JEED text, overlap groups; and about 150 on-eat procedures, which hostile_delight.md
    D10 would turn into food properties where the effect is plain.
12. **Common food pass.** Food tuning values, stack sizes, cans (generator), compost, AppleSkin, names, JEI pages.
13. **Advancements.** 20, dropped (pack rule), including the salt trade one.
14. **Blocks.** Honey glass, sugar glass, candied slime block, crates: BlockRunner tiers, mineable tags, item physics.

## 6. Decisions for the owner

| # | Question | Recommendation |
|---|---|---|
| U1 | Port at all, and in which form? | Not as a whole mod. Cherry-pick: re-make the chosen new items as pack items (KubeJS items, NCH recipes, LootJS drops), art under an AFL notice. A full backport carries 64 dead items and 35 duplicates |
| U2 | Which themes to take? | Strongest fits with the pack and no overlap: Sculk, Soul, Frost (into ice cubes / cold sweets), Phantom meat, Undead (zorkchop, soggy flesh), Alex's Mobs fish cuts. Weakest: Pale (rests on ghast tentacle), Nether plants (crowds Nether's Delight), single-gimmick drinks |
| U3 | Hostile vs Unusual for the 35 shared foods | Hostile's ids (installed, native 1.19.2); Unusual's recipes and values only as reference |
| U4 | Filled breads | Follow `baking/baked-goods #33`: drop them, or turn the good ones into sandwiches |
| U5 | Ice and Fire / Alex's Caves content | Skip. Optional: dragon foods rerouted onto End's dragon meat |
| U6 | New mob drops (soul from every kill, warden soul, snow golem snowball, drowned flesh, phantom meat) | Take as LootJS modifiers; soul on every kill is the one to question (inventory spam) |
| U7 | Second salt source (quartz on the board, smelted soggy flesh) | Skip; salt already has a full chain |

## 7. Tests

None (survey only). Test IDs get drafted with the first build, prefix `UD-`.
