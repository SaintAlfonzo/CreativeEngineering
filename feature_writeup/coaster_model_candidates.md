# Coaster model candidates

Status: ANALYSIS 2026-10-26; partly applied 2026-10-29 ([kitchen.md](kitchen.md) 2.3, TODO `cooking/kitchen #21`). Read-only comparison. Open: recoloured models for the Good / Fair matches that would show the wrong colour on a borrowed model.

**2026-10-29 update.** The 56 pack coaster files were gone by 2026-10-27 (the generator could not read Display Delight
1.7.0's multipart blockstates) and the tag was the port's 96. `tools/gen_coaster_models.py` now reads them and every
jar link: 155 Display Delight foods (many of the references and matches below, for example the Farmer's Respite
teas and coffee, now have their own DD model), 26 Vinery wines and 8 Vintage Delight jars from their own block
models, and 19 borrows in its `BORROW` table. Only same-picture matches were borrowed (Farmer's Respite long/strong
drinks, Create Central Kitchen pumpkin pie slice, Enchanted Golden Carrot with glint): the score below ignores hue,
so a Good or Fair match can still show the wrong colour on a borrowed model (golden foods, other teas, cookies).
Those stay out until they get a recoloured model. Tag 304.

## 1. Tag state

`#creativedelight:placeable_on_coaster` (and through it `#brewinandchewin:placeable_on_coaster`) already holds only foods with a coaster model: 152 entries in `kubejs/generation_scripts/food/placeable.json`, 152 coaster model files (96 from the Brewin' port, 56 pack files from `tools/gen_coaster_models.py`). The cut of unmodelled foods was made 2026-10-25 ([food_families.md](food_families.md) 4.4; `tools/placeable_catalog.py` `add_coaster` only adds modelled items). Nothing was left to cut.

Two things found while checking:

- `python3 tools/gen_coaster_models.py --check` reports all 56 pack coaster files as left over and 65 DD-linked foods with no default model (the Display Delight port is mid-change). Regenerating now would delete those 56 files and `placeable_catalog.py` would then drop them from the tag (152 to 96). Do not run the generator until the DD port's block models are back.
- `PLACE_PRODUCE` in `tools/food_families.py` still says every produce food, egg and cheese is in the coaster tag; that is stale and shows in the generated family tables.

## 2. Method

Item sprite of each food in `kubejs/exported/food/foods.json` (1437 foods), resolved through its item model from the pack's kubejs assets, then the mod jars, then vanilla. 1069 foods have a flat item sprite and are not on the tag; each was compared with the 142 tagged foods that have one. Score = half silhouette overlap (alpha, IoU) plus half brightness-pattern correlation inside the shared silhouette; hue is ignored, so colour shifts do not cost anything and shape changes do. Pairs scoring 0.75 or more (354) were checked by eye side by side and the shape mismatches thrown out.

- **Good**: same shape and shading, recolour or small detail change. Safe to reuse the model.
- **Fair**: same container or silhouette, contents differ (bowls, mugs, rolls). Reusable, but check the contents read right.

Not compared: 226 foods without a flat item sprite (block-model cakes, feasts, pies, whole food blocks, dynamic or generated models), and 10 tagged foods whose sprite could not be resolved. Foods scoring under 0.80 are left out unless a close shape match was obvious (cake slices, popsicles, corn dog, golden berries).

Coaster models used as references that matched at least one food: 74 of 152.

## 3. Matches by modelled food

Each heading is a food that already has a coaster model; the list under it is the foods whose sprite looks like it. Counts: 89 Good, 144 Fair, 233 foods in total.

### Ender Nectar (`delightful:ender_nectar`) - 25

| Food | Id | Match |
|---|---|---|
| Sea Grape Juice | `oceanic_delight:sea_grape_juice` | Good 0.94 |
| Chorus Juice | `endersdelight:chorus_juice` | Good 0.92 |
| Coffee | `farmersrespite:coffee` | Good 0.92 |
| Coffee | `farmersrespite:long_coffee` | Good 0.92 |
| Coffee | `farmersrespite:strong_coffee` | Good 0.92 |
| Prickly Pear Juice | `delightful:prickly_pear_juice` | Good 0.91 |
| Sea Pickle Tea | `hostiledelight:seacucumbertea` | Good 0.90 |
| Azalea Tea | `delightful:azalea_tea` | Fair 0.90 |
| Purulent Tea | `farmersrespite:long_purulent_tea` | Fair 0.90 |
| Purulent Tea | `farmersrespite:purulent_tea` | Fair 0.90 |
| Purulent Tea | `farmersrespite:strong_purulent_tea` | Fair 0.90 |
| Yellow Tea | `farmersrespite:long_yellow_tea` | Fair 0.90 |
| Yellow Tea | `farmersrespite:strong_yellow_tea` | Fair 0.90 |
| Yellow Tea | `farmersrespite:yellow_tea` | Fair 0.90 |
| Black Tea | `farmersrespite:black_tea` | Fair 0.90 |
| Black Tea | `farmersrespite:long_black_tea` | Fair 0.90 |
| Black Tea | `farmersrespite:strong_black_tea` | Fair 0.90 |
| Rose Hip Tea | `farmersrespite:long_rose_hip_tea` | Fair 0.89 |
| Rose Hip Tea | `farmersrespite:rose_hip_tea` | Fair 0.89 |
| Rose Hip Tea | `farmersrespite:strong_rose_hip_tea` | Fair 0.89 |
| Chorus Fruit Wine | `ends_delight:chorus_fruit_wine` | Fair 0.88 |
| Hawberry Tea | `fruitsdelight:hawberry_tea` | Fair 0.87 |
| Dandelion Juice | `veggiesdelight:dandelion_juice` | Fair 0.86 |
| Bubble Tea | `ends_delight:bubble_tea` | Fair 0.85 |
| Bayberry Soup | `fruitsdelight:bayberry_soup` | Fair 0.82 |

### Honey Bottle (`minecraft:honey_bottle`) - 12

| Food | Id | Match |
|---|---|---|
| Glow Berries Jam Bottle | `vintagedelight:glow_berry_jam_bottle` | Good 0.98 |
| Apple Sauce Bottle | `vintagedelight:apple_sauce_bottle` | Good 0.94 |
| Fish Oil | `alexsmobs:fish_oil` | Good 0.92 |
| Mana in a Bottle | `botania:mana_bottle` | Fair 0.88 |
| Strawberry Banana Smoothie | `neapolitan:strawberry_banana_smoothie` | Fair 0.88 |
| Enchanted Golden Honey Bottle | `goldenfood:enchanted_golden_honey_bottle` | Fair 0.84 |
| Golden Honey Bottle | `goldenfood:golden_honey_bottle` | Fair 0.84 |
| Pepper Jam Bottle | `vintagedelight:pepper_jam_bottle` | Fair 0.84 |
| Relish Bottle | `vintagedelight:relish_bottle` | Fair 0.83 |
| Sweet Berries Jam Bottle | `vintagedelight:sweet_berry_jam_bottle` | Fair 0.82 |
| Mulberry Jam Bottle | `upgrade_aquatic:mulberry_jam_bottle` | Fair 0.82 |
| Nut Mash Bottle | `vintagedelight:nut_mash_bottle` | Fair 0.81 |

### Mushroom Stew (`minecraft:mushroom_stew`) - 12

| Food | Id | Match |
|---|---|---|
| Ardent Sauce | `hostiledelight:ardentsauce` | Good 0.92 |
| Scrambled Eggs | `incubation:scrambled_eggs` | Fair 0.96 |
| Sopa De Macaco | `alexsmobs:sopa_de_macaco` | Fair 0.89 |
| Enchanted Golden Mushroom Stew | `goldenfood:enchanted_golden_mushroom_stew` | Fair 0.88 |
| Golden Mushroom Stew | `goldenfood:golden_mushroom_stew` | Fair 0.88 |
| Ghastly Ice cream | `hostiledelight:ghastbloodicecream` | Fair 0.87 |
| Potato Salad | `oceanic_delight:potato_salad` | Fair 0.86 |
| Slime Stew | `hostiledelight:slimestew` | Fair 0.84 |
| Aurora soup | `hostiledelight:aurorasoup` | Fair 0.84 |
| Seagrass Salad | `oceansdelight:seagrass_salad` | Fair 0.84 |
| Bowl of Dragon Meat Stew | `ends_delight:dragon_meat_stew` | Fair 0.80 |
| Fish Egg Noodle Soup | `oceanic_delight:fish_egg_noodle_soup` | Fair 0.80 |

### Honey Cookie (`farmersdelight:honey_cookie`) - 12

| Food | Id | Match |
|---|---|---|
| Crawling Cookie | `hostiledelight:mitecookie` | Good 0.93 |
| Vein cookie | `hostiledelight:magmacookie` | Good 0.93 |
| Swelling cookie | `hostiledelight:ghastcookie` | Good 0.92 |
| Lemon Cookie | `fruitsdelight:lemon_cookie` | Good 0.90 |
| Gazing cookie | `hostiledelight:guardiancookie` | Fair 0.90 |
| Persimmon Cookie | `fruitsdelight:persimmon_cookie` | Fair 0.89 |
| Maple Cookie | `abnormals_delight:maple_cookie` | Fair 0.84 |
| Green Tea Cookie | `farmersrespite:green_tea_cookie` | Fair 0.83 |
| Mulberry Cookie | `abnormals_delight:mulberry_cookie` | Fair 0.83 |
| Emitting Cookie | `hostiledelight:blazecookie` | Fair 0.82 |
| Magical Butter Cookie | `hostiledelight:magicbuttercookie` | Fair 0.80 |
| Cherry Cookie | `abnormals_delight:cherry_cookie` | Fair 0.80 |

### Apple Cider (`farmersdelight:apple_cider`) - 10

| Food | Id | Match |
|---|---|---|
| Apple Cider | `farmersrespite:long_apple_cider` | Good 1.00 |
| Apple Cider | `farmersrespite:strong_apple_cider` | Good 1.00 |
| Lavender Tea | `delightful:lavender_tea` | Good 0.91 |
| Green Tea | `farmersrespite:green_tea` | Fair 0.89 |
| Green Tea | `farmersrespite:long_green_tea` | Fair 0.89 |
| Green Tea | `farmersrespite:strong_green_tea` | Fair 0.89 |
| Peach Tea | `fruitsdelight:peach_tea` | Fair 0.86 |
| Mango Tea | `fruitsdelight:mango_tea` | Fair 0.85 |
| Orange Juice | `fruitsdelight:orange_juice` | Fair 0.81 |
| Kiwi Juice | `fruitsdelight:kiwi_juice` | Fair 0.80 |

### Chicken Soup (`farmersdelight:chicken_soup`) - 8

| Food | Id | Match |
|---|---|---|
| Fig Chicken Stew | `fruitsdelight:fig_chicken_stew` | Good 0.99 |
| Guardian Soup | `hostiledelight:guardiansoup` | Good 0.94 |
| Pear with Rock Sugar | `fruitsdelight:pear_with_rock_sugar` | Good 0.94 |
| Cauliflower Soup | `veggiesdelight:cauliflower_soup` | Good 0.92 |
| Broccoli Soup | `veggiesdelight:broccoli_soup` | Good 0.92 |
| Batter | `rusticdelight:batter` | Good 0.92 |
| Carrot Soup | `moredelight:carrot_soup` | Fair 0.89 |
| Shulker Soup | `ends_delight:shulker_soup` | Fair 0.85 |

### Golden Apple (`minecraft:golden_apple`) - 8

| Food | Id | Match |
|---|---|---|
| Honey Apple | `buzzier_bees:honey_apple` | Good 0.92 |
| Blazing Apple | `hostiledelight:blazingapple` | Fair 0.90 |
| Caramel Apple | `create_factory:caramel_apple` | Fair 0.83 |
| Honeyed Apple | `create:honeyed_apple` | Fair 0.83 |
| Ruby Chocolate Apple | `create_factory:ruby_chocolate_apple` | Fair 0.83 |
| Chocolate Apple | `create_factory:chocolate_apple` | Fair 0.82 |
| White Chocolate Apple | `create_factory:white_chocolate_apple` | Fair 0.81 |
| Orange | `fruitsdelight:orange` | Fair 0.81 |

### Creamed Corn (`corn_delight:creamed_corn`) - 8

| Food | Id | Match |
|---|---|---|
| Bowl of Pineapple Fried Rice | `fruitsdelight:bowl_of_pineapple_fried_rice` | Good 0.90 |
| Oatmeal | `vintagedelight:oatmeal` | Fair 0.89 |
| Mashed Potatoes | `veggiesdelight:mashed_potatoes` | Fair 0.87 |
| Bowl Of Paella | `oceanic_delight:bowl_of_paella` | Fair 0.87 |
| Potato Salad | `moredelight:potato_salad` | Fair 0.87 |
| Potato Noodles | `veggiesdelight:potato_noodles` | Fair 0.85 |
| Vanilla Pudding | `neapolitan:vanilla_pudding` | Fair 0.82 |
| Bowl of Steamed Dragon Egg | `ends_delight:steamed_dragon_egg` | Fair 0.80 |

### Slice of Pumpkin Pie (`delightful:pumpkin_pie_slice`) - 6

| Food | Id | Match |
|---|---|---|
| Slice of Pumpkin Pie | `create_central_kitchen:pumpkin_pie_slice` | Good 1.00 |
| Sweet Potato Pie Slice | `veggiesdelight:sweet_potato_pie_slice` | Good 0.95 |
| Slice of Green Apple Pie | `delightful:green_apple_pie_slice` | Good 0.92 |
| Quiche Slice | `brewinandchewin:quiche_slice` | Good 0.91 |
| Slice of Blueberry Pie | `delightful:blueberry_pie_slice` | Fair 0.89 |
| Slice of Gloomgourd Pie | `delightful:gloomgourd_pie_slice` | Fair 0.84 |

### Matcha Ice Cream (`delightful:matcha_ice_cream`) - 6

| Food | Id | Match |
|---|---|---|
| Adzuki Ice Cream | `neapolitan:adzuki_ice_cream` | Good 0.98 |
| Chocolate Ice Cream | `neapolitan:chocolate_ice_cream` | Good 0.92 |
| Strawberry Ice Cream | `neapolitan:strawberry_ice_cream` | Good 0.91 |
| Mint Ice Cream | `neapolitan:mint_ice_cream` | Fair 0.89 |
| Neapolitan Ice Cream | `neapolitan:neapolitan_ice_cream` | Fair 0.87 |
| Truffle Ice Cream | `fungidelight:truffle_ice_cream` | Fair 0.85 |

### Bread (`minecraft:bread`) - 5

| Food | Id | Match |
|---|---|---|
| Jelly Bread | `fruitsdelight:jelly_bread` | Good 1.00 |
| Enchanted Golden Bread | `goldenfood:enchanted_golden_bread` | Good 0.99 |
| Golden Bread | `goldenfood:golden_bread` | Good 0.99 |
| Mulberry Bread | `upgrade_aquatic:mulberry_bread` | Fair 0.88 |
| Glowing Bread | `hostiledelight:glowbread` | Fair 0.86 |

### Chicken Sandwich (`farmersdelight:chicken_sandwich`) - 5

| Food | Id | Match |
|---|---|---|
| Guardian Sandwich | `hostiledelight:guardiansandwich` | Good 0.96 |
| Steak Sandwich | `moredelight:steak_sandwich` | Good 0.95 |
| Porkchop Sandwich | `moredelight:porkchop_sandwich` | Good 0.94 |
| Chicken Sandwich with Egg and Tomato | `moredelight:chicken_sandwich_with_egg_and_tomato` | Fair 0.84 |
| Zucchini Sandwich | `veggiesdelight:zucchini_sandwich` | Fair 0.82 |

### Creamy Corn Drink (`corn_delight:creamy_corn_drink`) - 5

| Food | Id | Match |
|---|---|---|
| Pear Juice | `fruitsdelight:pear_juice` | Good 0.90 |
| Dandelion Tea | `farmersrespite:dandelion_tea` | Fair 0.84 |
| Dandelion Tea | `farmersrespite:long_dandelion_tea` | Fair 0.84 |
| Mangosteen Tea | `fruitsdelight:mangosteen_tea` | Fair 0.83 |
| Lemon Juice | `fruitsdelight:lemon_juice` | Fair 0.82 |

### Sweet Berries (`minecraft:sweet_berries`) - 5

| Food | Id | Match |
|---|---|---|
| Chocolate Glazed Berries | `create:chocolate_glazed_berries` | Fair 0.79 |
| Enchanted Golden Sweet Berries | `goldenfood:enchanted_golden_sweet_berries` | Fair 0.78 |
| Golden Sweet Berries | `goldenfood:golden_sweet_berries` | Fair 0.78 |
| Dark Chocolate Glazed Berries | `create_confectionery:black_chocolate_glazed_berries` | Fair 0.77 |
| Ruby Chocolate Glazed Berries | `create_confectionery:ruby_chocolate_glazed_berries` | Fair 0.76 |

### Potato (`minecraft:potato`) - 4

| Food | Id | Match |
|---|---|---|
| Sweet Potato Dough | `veggiesdelight:sweet_potato_dough` | Fair 0.83 |
| Wheat Dough | `farmersdelight:wheat_dough` | Fair 0.82 |
| Cucumber Dough | `vintagedelight:cucumber_dough` | Fair 0.82 |
| Oat Dough | `vintagedelight:oat_dough` | Fair 0.82 |

### Slice of Cake (`farmersdelight:cake_slice`) - 4

| Food | Id | Match |
|---|---|---|
| Carrot Cake Slice | `veggiesdelight:carrot_cake_slice` | Fair 0.81 |
| Slice of Vanilla Cake | `abnormals_delight:vanilla_cake_slice` | Fair 0.79 |
| Slice of Pumpkin Cake | `create_central_kitchen:pumpkin_cake_slice` | Fair 0.76 |
| Slice of Adzuki Cake | `abnormals_delight:adzuki_cake_slice` | Fair 0.75 |

### Cookie (`minecraft:cookie`) - 3

| Food | Id | Match |
|---|---|---|
| Enchanted Golden Cookie | `goldenfood:enchanted_golden_cookie` | Good 1.00 |
| Golden Cookie | `goldenfood:golden_cookie` | Good 1.00 |
| Biscuit of Totality | `botania:mana_cookie` | Good 0.96 |

### Cooked Chicken (`minecraft:cooked_chicken`) - 3

| Food | Id | Match |
|---|---|---|
| Enchanted Golden Cooked Chicken | `goldenfood:enchanted_golden_cooked_chicken` | Good 0.99 |
| Golden Cooked Chicken | `goldenfood:golden_cooked_chicken` | Good 0.99 |
| Cooked Goose | `untitledduckmod:cooked_goose` | Fair 0.84 |

### Cod Roll (`farmersdelight:cod_roll`) - 3

| Food | Id | Match |
|---|---|---|
| Guardian Roll | `hostiledelight:guardianroll` | Good 0.98 |
| Roll of Elder Guardian | `oceansdelight:elder_guardian_roll` | Fair 0.87 |
| Ghast Roll | `hostiledelight:ghastroll` | Fair 0.82 |

### Salmonberry Ice Cream (`delightful:salmonberry_ice_cream`) - 3

| Food | Id | Match |
|---|---|---|
| Vanilla Ice Cream | `neapolitan:vanilla_ice_cream` | Good 0.95 |
| Banana Ice Cream | `neapolitan:banana_ice_cream` | Fair 0.90 |
| Glowgurt | `berry_good:glowgurt` | Fair 0.85 |

### Bacon Sandwich (`farmersdelight:bacon_sandwich`) - 3

| Food | Id | Match |
|---|---|---|
| Tomato Sandwich | `moredelight:tomato_sandwich` | Good 0.94 |
| PB & J | `vintagedelight:pb_j` | Good 0.93 |
| Egg with Bacon Sandwich | `moredelight:egg_with_bacon_sandwich` | Good 0.92 |

### Cooked Salmon (`minecraft:cooked_salmon`) - 3

| Food | Id | Match |
|---|---|---|
| Enchanted Golden Cooked Salmon | `goldenfood:enchanted_golden_cooked_salmon` | Good 0.92 |
| Golden Cooked Salmon | `goldenfood:golden_cooked_salmon` | Good 0.92 |
| Salted Salmon | `vintagedelight:salted_salmon` | Fair 0.88 |

### Mixed Salad (`farmersdelight:mixed_salad`) - 3

| Food | Id | Match |
|---|---|---|
| Squid Salad | `oceanic_delight:squid_salad` | Fair 0.90 |
| Chicken Salad | `moredelight:chicken_salad` | Fair 0.85 |
| Caesar Salad | `oceanic_delight:caesar_salad` | Fair 0.85 |

### Melon Popsicle (`farmersdelight:melon_popsicle`) - 3

| Food | Id | Match |
|---|---|---|
| Chorus Fruit Popsicle | `ends_delight:chorus_fruit_popsicle` | Fair 0.87 |
| Chocolate Popsicle | `moredelight:chocolate_popsicle` | Fair 0.82 |
| Popsiclime | `hostiledelight:popsiclime` | Fair 0.76 |

### Cooked Porkchop (`minecraft:cooked_porkchop`) - 2

| Food | Id | Match |
|---|---|---|
| Enchanted Golden Cooked Porkchop | `goldenfood:enchanted_golden_cooked_porkchop` | Good 0.99 |
| Golden Cooked Porkchop | `goldenfood:golden_cooked_porkchop` | Good 0.99 |

### Cooked Mutton (`minecraft:cooked_mutton`) - 2

| Food | Id | Match |
|---|---|---|
| Enchanted Golden Cooked Mutton | `goldenfood:enchanted_golden_cooked_mutton` | Good 0.98 |
| Golden Cooked Mutton | `goldenfood:golden_cooked_mutton` | Good 0.98 |

### Cooked Rabbit (`minecraft:cooked_rabbit`) - 2

| Food | Id | Match |
|---|---|---|
| Enchanted Golden Cooked Rabbit | `goldenfood:enchanted_golden_cooked_rabbit` | Good 0.98 |
| Golden Cooked Rabbit | `goldenfood:golden_cooked_rabbit` | Good 0.98 |

### Steak (`minecraft:cooked_beef`) - 2

| Food | Id | Match |
|---|---|---|
| Enchanted Golden Cooked Beef | `goldenfood:enchanted_golden_cooked_beef` | Good 0.97 |
| Golden Cooked Beef | `goldenfood:golden_cooked_beef` | Good 0.97 |

### Pasta with Meatballs (`farmersdelight:pasta_with_meatballs`) - 2

| Food | Id | Match |
|---|---|---|
| Pearl Pasta | `endersdelight:pearl_pasta_wood` | Good 0.97 |
| Pearl Pasta | `endersdelight:pearl_pasta` | Fair 0.89 |

### Cooked Cod (`minecraft:cooked_cod`) - 2

| Food | Id | Match |
|---|---|---|
| Enchanted Golden Cooked Cod | `goldenfood:enchanted_golden_cooked_cod` | Good 0.97 |
| Golden Cooked Cod | `goldenfood:golden_cooked_cod` | Good 0.97 |

### Chorus Fruit (`minecraft:chorus_fruit`) - 2

| Food | Id | Match |
|---|---|---|
| Enchanted Golden Chorus Fruit | `goldenfood:enchanted_golden_chorus_fruit` | Good 0.96 |
| Golden Chorus Fruit | `goldenfood:golden_chorus_fruit` | Good 0.96 |

### Fried Egg (`farmersdelight:fried_egg`) - 2

| Food | Id | Match |
|---|---|---|
| Fried Dragon Egg | `ends_delight:fried_dragon_egg` | Good 0.96 |
| Fried Turtle Egg | `hostiledelight:friedturtleegg` | Good 0.94 |

### Slice of Apple Pie (`farmersdelight:apple_pie_slice`) - 2

| Food | Id | Match |
|---|---|---|
| Slice of Chorus Fruit Pie | `ends_delight:chorus_fruit_pie_slice` | Good 0.95 |
| Slice of Mulberry Pie | `create_central_kitchen:mulberry_pie_slice` | Fair 0.84 |

### Kelp Roll Slice (`farmersdelight:kelp_roll_slice`) - 2

| Food | Id | Match |
|---|---|---|
| Sea Pickle Roll Slice | `oceanic_delight:sea_pickle_roll_slice` | Good 0.95 |
| Fish Egg Roll Slice | `oceanic_delight:fish_egg_roll_slice` | Fair 0.89 |

### Glow Berries (`minecraft:glow_berries`) - 2

| Food | Id | Match |
|---|---|---|
| Enchanted Golden Glow Berries | `goldenfood:enchanted_golden_glow_berries` | Good 0.95 |
| Golden Glow Berries | `goldenfood:golden_glow_berries` | Good 0.95 |

### Kelp Roll (`farmersdelight:kelp_roll`) - 2

| Food | Id | Match |
|---|---|---|
| Sea Pickle Roll | `oceanic_delight:sea_pickle_roll` | Good 0.95 |
| Fish Egg Roll | `oceanic_delight:fish_egg_roll` | Fair 0.85 |

### Mutton Wrap (`farmersdelight:mutton_wrap`) - 2

| Food | Id | Match |
|---|---|---|
| Vegan Wrap | `miners_delight:vegan_wrap` | Good 0.94 |
| Stuffed Burrito | `vintagedelight:stuffed_burrito` | Fair 0.82 |

### Kangaroo Pasta (`alexsdelight:kangaroo_pasta`) - 2

| Food | Id | Match |
|---|---|---|
| Pad Thai | `vintagedelight:pad_thai` | Good 0.93 |
| Pasta With Eyeball | `oceanic_delight:pasta_with_eyeball` | Fair 0.80 |

### Bison Burger (`alexsdelight:bison_burger`) - 2

| Food | Id | Match |
|---|---|---|
| Vegan Hamburger | `miners_delight:vegan_hamburger` | Good 0.93 |
| Cave Hamburger | `miners_delight:cave_hamburger` | Fair 0.84 |

### Beef Stew (`farmersdelight:beef_stew`) - 2

| Food | Id | Match |
|---|---|---|
| Pineapple Marinated Pork | `fruitsdelight:pineapple_marinated_pork` | Good 0.93 |
| Cacciatore | `veggiesdelight:cacciatore` | Fair 0.81 |

### Rabbit Stew (`minecraft:rabbit_stew`) - 2

| Food | Id | Match |
|---|---|---|
| Enchanted Golden Rabbit Stew | `goldenfood:enchanted_golden_rabbit_stew` | Good 0.92 |
| Golden Rabbit Stew | `goldenfood:golden_rabbit_stew` | Good 0.92 |

### Glow Berry Custard (`farmersdelight:glow_berry_custard`) - 2

| Food | Id | Match |
|---|---|---|
| Blueberry Custard | `fruitsdelight:blueberry_custard` | Fair 0.90 |
| Hamimelon Shaved Ice | `fruitsdelight:hamimelon_shaved_ice` | Fair 0.85 |

### Cooked Rice (`farmersdelight:cooked_rice`) - 2

| Food | Id | Match |
|---|---|---|
| Potato Salad | `rusticdelight:potato_salad` | Fair 0.88 |
| Shulker Omelette Mixture | `ends_delight:shulker_omelette_mixture` | Fair 0.82 |

### Beetroot (`minecraft:beetroot`) - 2

| Food | Id | Match |
|---|---|---|
| Enchanted Golden Beetroot | `goldenfood:enchanted_golden_beetroot` | Fair 0.87 |
| Golden Beetroot | `goldenfood:golden_beetroot` | Fair 0.87 |

### Pumpkin Soup (`farmersdelight:pumpkin_soup`) - 2

| Food | Id | Match |
|---|---|---|
| Garlic Chicken Stew | `veggiesdelight:garlic_chicken_stew` | Fair 0.87 |
| Creamy Morel Soup | `fungidelight:creamy_morel_soup` | Fair 0.80 |

### Slice of Chocolate Pie (`farmersdelight:chocolate_pie_slice`) - 2

| Food | Id | Match |
|---|---|---|
| Chorus Pie Slice | `endersdelight:chorus_pie_slice` | Fair 0.87 |
| Slice of Source Berry Pie | `delightful:source_berry_pie_slice` | Fair 0.84 |

### Slice of Salmonberry Pie (`delightful:salmonberry_pie_slice`) - 2

| Food | Id | Match |
|---|---|---|
| Slice of Crimson Berry Pie | `delightful:crimson_berry_pie_slice` | Fair 0.86 |
| Slice of Nightshade Berry Pie | `delightful:nightshade_berry_pie_slice` | Fair 0.83 |

### Bone Broth (`farmersdelight:bone_broth`) - 2

| Food | Id | Match |
|---|---|---|
| Chorus Sauce | `ends_delight:chorus_sauce` | Fair 0.84 |
| Glow Squid Stew | `oceanic_delight:glow_squid_stew` | Fair 0.81 |

### Golden Carrot (`minecraft:golden_carrot`) - 1

| Food | Id | Match |
|---|---|---|
| Enchanted Golden Carrot | `goldenfood:enchanted_golden_carrot` | Good 1.00 |

### Roasted Marshmallow on a Stick (`delightful:cooked_marshmallow_stick`) - 1

| Food | Id | Match |
|---|---|---|
| Marshmallow on a Stick | `delightful:marshmallow_stick` | Good 1.00 |

### Bacon and Eggs (`farmersdelight:bacon_and_eggs`) - 1

| Food | Id | Match |
|---|---|---|
| Suspicious Bacon And Eggs | `hostiledelight:suspiciousbaconandaggs` | Good 0.99 |

### Sweet Berry Cookie (`farmersdelight:sweet_berry_cookie`) - 1

| Food | Id | Match |
|---|---|---|
| Uncanny Cookie | `endersdelight:uncanny_cookies` | Good 0.96 |

### Egg Sandwich (`farmersdelight:egg_sandwich`) - 1

| Food | Id | Match |
|---|---|---|
| Turtle Egg Sandwich | `hostiledelight:turtleeggsandwich` | Good 0.95 |

### Raw Cod (`minecraft:cod`) - 1

| Food | Id | Match |
|---|---|---|
| Salted Cod | `vintagedelight:salted_cod` | Good 0.95 |

### Salmon Roll (`farmersdelight:salmon_roll`) - 1

| Food | Id | Match |
|---|---|---|
| Calamari Roll | `rusticdelight:calamari_roll` | Good 0.93 |

### Grilled Corn (`corn_delight:grilled_corn`) - 1

| Food | Id | Match |
|---|---|---|
| Boiled Corn | `corn_delight:boiled_corn` | Good 0.92 |

### Hamburger (`farmersdelight:hamburger`) - 1

| Food | Id | Match |
|---|---|---|
| Cheese Burger | `vintagedelight:cheese_burger` | Good 0.91 |

### Steak and Potatoes (`farmersdelight:steak_and_potatoes`) - 1

| Food | Id | Match |
|---|---|---|
| Vegan Steak And Potatoes | `miners_delight:vegan_steak_and_potatoes` | Good 0.91 |

### Cabbage Rolls (`farmersdelight:cabbage_rolls`) - 1

| Food | Id | Match |
|---|---|---|
| Spring Rolls | `rusticdelight:spring_rolls` | Good 0.90 |

### Matcha Latte (`delightful:matcha_latte`) - 1

| Food | Id | Match |
|---|---|---|
| Chorus Fruit Milk Tea | `ends_delight:chorus_fruit_milk_tea` | Fair 0.89 |

### Pasta with Mutton Chop (`farmersdelight:pasta_with_mutton_chop`) - 1

| Food | Id | Match |
|---|---|---|
| Bell Pepper Pasta | `rusticdelight:bell_pepper_pasta` | Fair 0.88 |

### Slice of Sweet Berry Pie (`farmersdelight:sweet_berry_cheesecake_slice`) - 1

| Food | Id | Match |
|---|---|---|
| Slice of Cherry Pie | `create_central_kitchen:cherry_pie_slice` | Fair 0.86 |

### Ratatouille (`farmersdelight:ratatouille`) - 1

| Food | Id | Match |
|---|---|---|
| Fried Calamari | `rusticdelight:fried_calamari` | Fair 0.85 |

### Squid Ink Pasta (`farmersdelight:squid_ink_pasta`) - 1

| Food | Id | Match |
|---|---|---|
| Glowing Ink Pasta | `hostiledelight:glowpasta` | Fair 0.84 |

### Raw Chicken (`minecraft:chicken`) - 1

| Food | Id | Match |
|---|---|---|
| Raw Goose | `untitledduckmod:raw_goose` | Fair 0.83 |

### Plate of Shepherd's Pie (`farmersdelight:shepherds_pie`) - 1

| Food | Id | Match |
|---|---|---|
| Sweet Potato Pancakes | `veggiesdelight:sweet_potato_pancakes` | Fair 0.83 |

### Fruit Salad (`farmersdelight:fruit_salad`) - 1

| Food | Id | Match |
|---|---|---|
| Mango Salad | `fruitsdelight:mango_salad` | Fair 0.82 |

### Vegetable Noodles (`farmersdelight:vegetable_noodles`) - 1

| Food | Id | Match |
|---|---|---|
| Cheese Pasta | `vintagedelight:cheese_pasta` | Fair 0.82 |

### Corn Soup (`corn_delight:corn_soup`) - 1

| Food | Id | Match |
|---|---|---|
| Bell Pepper Soup | `rusticdelight:bell_pepper_soup` | Fair 0.81 |

### Mushroom Rice (`farmersdelight:mushroom_rice`) - 1

| Food | Id | Match |
|---|---|---|
| Guardian Sashimi | `hostiledelight:guardiansashimi` | Fair 0.81 |

### Suspicious Stew (`minecraft:suspicious_stew`) - 1

| Food | Id | Match |
|---|---|---|
| Bowl of Guardian Soup | `oceansdelight:bowl_of_guardian_soup` | Fair 0.81 |

### Tomato Sauce (`farmersdelight:tomato_sauce`) - 1

| Food | Id | Match |
|---|---|---|
| Ender Noodle | `ends_delight:ender_noodle` | Fair 0.80 |

### Tomato (`farmersdelight:tomato`) - 1

| Food | Id | Match |
|---|---|---|
| Persimmon | `fruitsdelight:persimmon` | Fair 0.80 |

### Classic Corn Dog (`corn_delight:classic_corn_dog`) - 1

| Food | Id | Match |
|---|---|---|
| Corn Dog | `corn_delight:corn_dog` | Fair 0.78 |

## 4. Next steps

1. Decide the cut-off (Good only, or Good and Fair).
2. A coaster JSON in `assets/<ns>/brewinandchewin/coaster/` names one item, so each accepted food needs its own file pointing at the reference food's model. `gen_coaster_models.py` could take a "borrow model from" table for this instead of hand-written files.
3. Add the accepted foods to the tag (rule in `placeable_catalog.py`, not the json), restart, run the catalog, and add a changelog bullet then.
