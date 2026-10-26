# Comfort vs Nourishment audit

Audit of which foods give Farmer's Delight's *Comfort* and which give *Nourishment*, and whether a food's hunger or
saturation value decides between them. Written so the owner can set pack rules (section 5). Nothing is built.

## 0. At a glance

| Part | Status |
|---|---|
| Audit (upstream 1.19.2 values, before the delight backports) | DONE 2026-10-26 |
| Pack rule for Comfort / Nourishment | OPEN: decisions CN-D1 to CN-D5 (section 5) |
| Applying the rule | not started; depends on the decisions |

**Answer:** there is **no hunger or saturation cutoff**. In Farmer's Delight 1.2.4 the two effects use the same value
tiers (Chicken Soup and Roast Chicken are both 14 / 0.75 for 300 s). The values pick the **duration**; the **dish form**
picks the effect: soups, stews, broths and warm bowls give Comfort, plated meals give Nourishment. Add-ons follow that
form rule loosely (81 %); Miner's Delight 1.1.2 breaks it most.

Source: `kubejs/exported/food/foods.json` (`/dumpfood` of 2026-10-24 03:39, 1437 foods). Script: scratchpad, not kept;
it is about 40 lines of Python over that file (section 6).

## 1. What the two effects do (1.19.2, FD 1.2.4)

- **Comfort**: heals half a heart every 4 s while saturation is empty and the player has no Regeneration. It replaces
  saturation healing for a hungry player. ([effects.md](effects.md) row Comfort.)
- **Nourishment**: hunger does not drain, except the drain from saturation healing on a nearly full bar. It keeps the
  bar where it is.

They overlap little: Comfort is for a player who is already low, Nourishment keeps a player from getting low.

## 2. Scope

- **Audit set (130 foods):** every food in the dump that gives exactly one of the two effects, from mods running their
  own upstream 1.19.2 values: Farmer's Delight 1.2.4 (repack, values unchanged), Abnormals, Alex's, Corn, Create Central
  Kitchen, Delightful, Ender's 1.2.2, End's, Farmer's Respite, Fruits, Miner's 1.1.2 (the dump predates the 1.4.5 port
  install of 2026-10-25), More, Nether's 3.1, Ocean's, Rustic 1.19.2 (the dump predates the 1.7.0 port).
  Pack overrides (`cooking/food_values.js`, `minecraft/food_values.js`) touch neither effect, so the dump equals upstream.
- **Left out:** the ports with 1.20-era values (Brewin' 4.5.0, Veggies 1.9.2, Vintage, Fungi; 64 foods, kept for
  comparison in 4.4 and A.2) and the pack's 52 cans (copies of their source food).

## 3. Cutoff tests

### 3.1 Spread

| Set | Effect | Foods | Hunger min / median / max | Modifier min / median / max | Seconds min / median / max |
|---|---|---:|---|---|---|
| Audit set | Comfort | 43 | 0 / 8 / 16 | 0 / 0.6 / 1.0 | 10 / 180 / 300 |
| Audit set | Nourishment | 87 | 0 / 12 / 18 | 0 / 0.75 / 1.1 | 10 / 180 / 300 |
| FD only | Comfort | 10 | 6 / 13 / 14 | 0.4 / 0.75 / 0.8 | 30 / 240 / 300 |
| FD only | Nourishment | 14 | 10 / 14 / 14 | 0.6 / 0.75 / 0.8 | 60 / 240 / 300 |

Comfort's median is lower only because cups, teas and cake slices pull it down. The ranges cover each other.

### 3.2 Best single threshold

Each value was tried as "Comfort below X, Nourishment from X up" (and the reverse) at every X in the data. Baseline:
calling every food Nourishment is already right for 87 of 130 (67 %).

| Value | Best split | Right (audit set) | Right (FD only, baseline 14 / 24) |
|---|---|---:|---:|
| Hunger | Comfort below 9 | 91 / 130 (70 %) | 16 / 24 (below 10) |
| Saturation points | Comfort below 6.3 | 91 / 130 (70 %) | 16 / 24 (below 12) |
| Modifier | none useful | 89 / 130 | 15 / 24 |
| Duration | none useful | 87 / 130 | 15 / 24 |

No threshold beats the baseline by more than 3 points. **Ten value pairs carry both effects**, among them 12 / 0.8,
14 / 0.75, 14 / 0.8, 10 / 0.6, 8 / 0.6 and 6 / 0.4. A value cutoff cannot reproduce upstream.

### 3.3 Is there a cutoff for getting either effect at all?

No. Hearty bowls with neither effect: Cooked Whole Crab 15, Shrimp-Fried Rice 12 / 1.0, Mashed Potatoes (Corn Delight)
12 / 0.8, Diced Potatoes with Beef 10 / 0.8, Rabbit Stew 10 / 0.6, Cavern Explorer Stew 10 / 0.7, Miso with Bamboo
Sprouts 10 / 0.7, Mushroom Stew and Beetroot Soup 6 / 0.6. Light foods with one: Cake slices 1 / 0.1 (10 s), Moss
1 / 0.05, teas at 0 hunger.

## 4. What does decide it

### 4.1 Farmer's Delight 1.2.4 (the reference design)

| Hunger / mod | Seconds | Comfort | Nourishment |
|---|---:|---|---|
| 6 / 0.4 | 30 | Cooked Rice | |
| 8 / 0.7 | 60 | Bone Broth | |
| 10 / 0.6 | 60 | | Bacon and Eggs, Ratatouille |
| 12 / 0.8 | 180 | Beef Stew, Fish Stew, Vegetable Soup | Mushroom Rice, Pasta with Meatballs, Pasta with Mutton Chop, Steak and Potatoes |
| 14 / 0.75 | 300 | Baked Cod Stew, Chicken Soup, Noodle Soup, Pumpkin Soup, Egg Fried Rice | Honey Glazed Ham, Roast Chicken, Shepherd's Pie, Stuffed Pumpkin, Roasted Mutton Chops, Squid Ink Pasta, Vegetable Noodles; Grilled Salmon (180 s) |

- **Effect = form.** Comfort: soups, stews, broth, plain and fried rice. Nourishment: plated meals, pasta, roasts.
  Noodle Soup is Comfort, Vegetable Noodles is Nourishment, at identical values.
- **Duration = value tier.** 6 hunger 30 s, 8 to 10 hunger 60 s, 12 hunger 180 s, 14 hunger 300 s, for both effects
  (one exception: Grilled Salmon). Add-ons follow this tier exactly for only 54 of 123 foods; most stay within one step.

### 4.2 Form rule across the add-ons

Rule tested: the name says soup, stew, broth, tea, drink, congee, chili, oatmeal, oats, porridge, curry or sinigang ->
Comfort, anything else -> Nourishment. Right for **105 / 130 (81 %)**; FD 22 / 24 (misses: Cooked Rice, Egg Fried Rice).
Adding "rice" to the Comfort words fixes FD's two but breaks Mushroom Rice and More Delight's three cooked-rice plates.

| Mod | Form rule right | Notes on the misses |
|---|---:|---|
| Abnormals, Ender's, Farmer's Respite, More, Nether's, Rustic | all | |
| Delightful | 8 / 9 | Azalea Tea gives Nourishment (Lavender Tea gives Comfort) |
| Corn | 4 / 5 | Creamed Corn (bowl, Comfort) |
| End's | 9 / 12 | Ender Noodle Comfort; Enderman Gristle Stew and Dragon Meat Stew Nourishment |
| Fruits | 4 / 8 | Pear with Rock Sugar, Mangosteen Cake, Pineapple Fried Rice Comfort; Fig Chicken Stew Nourishment |
| Alex's | 2 / 4 | Kangaroo Stew, Acacia Blossom Soup Nourishment |
| Ocean's | 1 / 2 | Cabbage Wrapped Elder Guardian Comfort (15 / 1.0, 30 s) |
| Central Kitchen | 1 / 2 | Yucca Cake slice Comfort, Pumpkin Cake slice Nourishment |
| **Miner's 1.1.2** | **18 / 28** | Its own rule: a cup of an FD soup copies FD (Comfort); its own soups (Bat, Cave, Insect) and the cups of the **vanilla** soups (Mushroom, Beetroot, Rabbit) and Onion Soup get Nourishment |

So upstream has one design (FD's) and add-ons that mostly copy it; the misses are mod authors' choices, not a hidden
value rule. Meat-heavy stews (kangaroo, dragon, gristle, fig chicken) lean Nourishment, which matches FD 1.3's later move.

### 4.3 The vanilla soups have neither

Mushroom Stew, Beetroot Soup and Rabbit Stew give nothing, but Miner's cups of them give Nourishment. Any form rule
would make them Comfort; that is a choice (CN-D4).

### 4.4 Ported (1.20-era) foods, for comparison

FD 1.3 and Brewin' 4.5 moved soups to Nourishment ([farmers_delight.md](farmers_delight.md) 2.5): Brewin's Creamy Onion
Soup is Nourishment. Veggies 1.9.2 (port) keeps 9 Comfort foods (soups, stews, coleslaw, mashed potatoes, pancakes).
Vintage gives Comfort to jams, apple sauce, oatmeal, overnight oats and its soups, and Nourishment to pickles, pizza,
pasta and burgers; Relish gives **both**. Fungi: Creamy Morel Soup Comfort, the rest Nourishment.
Decision 2.5 A (2026-10-25) already keeps Comfort on FD's soups, so the pack follows the 1.19.2 design.

## 5. Rule options for the owner

| Option | Rule | Changes | For | Against |
|---|---|---|---|---|
| **R1 Form rule** (recommended) | Comfort: soup, stew, broth, chowder, congee, porridge, oats, chili, curry, plain/fried rice in a bowl, warm drinks and teas. Nourishment: plated meals, pasta, roasts, sandwiches, burgers, bars, salads | 25 upstream foods (A.1, column "Form rule says"), plus the ports to taste | FD's own design, matches decision 2.5 A, explainable in one line on a tooltip or JEI | needs per-food overrides; meat stews may feel like meals |
| R2 Value cutoff | e.g. hunger 9 or less Comfort, 10 and more Nourishment | about 40 foods | one number, automatic for new foods | every FD soup (12 to 14 hunger) becomes Nourishment, reversing decision 2.5 A; light plates become Comfort |
| R3 Status quo | keep upstream, fix only outliers (Miner's vanilla-soup cups, Azalea vs Lavender Tea, cake slices) | under 10 | least work | no rule to point new foods at |
| R4 Form + value hybrid | R1 for which effect, FD's tier for duration (section 4.1) | R1 plus durations for about 70 foods | consistent lengths pack-wide | biggest change; durations are also a balance knob for food.md D1-D6 |

Decisions:

- **CN-D1** Which option (R1 / R2 / R3 / R4). Recommended R1, durations left as upstream until the tuning layer
  (food.md 4.3) can apply R4's tiers as one setting.
- **CN-D2** Rice: Comfort as in FD (Cooked Rice, Egg Fried Rice), or Nourishment as in Mushroom Rice and More Delight's
  rice plates? Recommended: plain and fried rice Comfort, rice topped with meat or mushrooms Nourishment.
- **CN-D3** Meat-heavy stews (Kangaroo, Dragon Meat, Enderman Gristle, Fig Chicken): Comfort by form, or stay
  Nourishment? Recommended Comfort, so "bowl you drink from" is the only test.
- **CN-D4** Vanilla Mushroom Stew, Beetroot Soup, Rabbit Stew: add a short Comfort (FD's 6-hunger tier, 30 s) or leave
  them bare? If added, Miner's cups of them follow.
- **CN-D5** Foods that give both (Vintage Relish bottle and jar) and non-meal items (cake slices, jams, Moss): keep, pick
  one, or drop the effect?

Applying any option: one `food_values.js`-style list in Creative Delight's tree (all food
code goes in CD), then `gen_can_types.py` so cans copy the new effects, and a changelog bullet.

## 6. File map and method

- Data: [foods.json](../kubejs/exported/food/foods.json) (fields `effects[].effect`, `nutrition`, `saturationModifier`,
  `remainder`, `tags`). Refresh with `/dumpfood` (part of `/dumpall`).
- Generated catalog with every food's effects: [food_catalog.md](food_catalog.md).
- Related decisions: [farmers_delight.md](farmers_delight.md) 2.5 (keep Comfort), [food.md](food.md) 4.3 (tuning layer
  D1-D6), [effects.md](effects.md) (effect texts).
- Method to repeat: load `foods.json`, keep foods with exactly one of `farmersdelight:comfort` /
  `farmersdelight:nourishment`, drop the port mods and `creativeengineering`, then for hunger, modifier, saturation and
  seconds try every observed value as a split and count matches; separately test the name regex of 4.2.

## 7. Tests

None until a rule is applied. Then: CN-01 eat one food from each changed group (soup, rice, meat stew, vanilla soup) and
check the effect and duration in the inventory screen and JEI.

## 8. History

- 2026-10-26: audit done on the 2026-10-24 dump. No value cutoff; FD picks the effect by dish form and the duration by
  value tier. Options R1 to R4 and decisions CN-D1 to CN-D5 written for the owner.

## Appendix: every food in the audit

"Form rule says" shows the form rule's answer where it disagrees with the food's current effect ("same" otherwise).
These are the foods R1 would change.

### A.1 Upstream 1.19.2 foods (the audit set, 130)

| Effect | Hunger | Mod | Sat | Seconds | Hands back | Form rule says | Food | Mod id |
|---|---:|---:|---:|---:|---|---|---|---|
| Comfort | 16 | 0.6 | 19.2 | 180 | bowl | same | Dragon's Breath and Chorus Soup | ends_delight |
| Comfort | 15 | 1 | 30 | 30 | - | Nourishment | Cabbage Wrapped Elder Guardian | oceansdelight |
| Comfort | 15 | 0.9 | 27 | 300 | bowl | same | Coconut Curry | delightful |
| Comfort | 15 | 0.9 | 27 | 300 | bowl | same | Sinigang | delightful |
| Comfort | 15 | 0.8 | 24 | 180 | bowl | same | Bowl of Guardian Soup | oceansdelight |
| Comfort | 14 | 0.8 | 22.4 | 300 | bowl | Nourishment | Bowl of Pineapple Fried Rice | fruitsdelight |
| Comfort | 14 | 0.75 | 21 | 300 | bowl | same | Baked Cod Stew | farmersdelight |
| Comfort | 14 | 0.75 | 21 | 300 | bowl | same | Chicken Soup | farmersdelight |
| Comfort | 14 | 0.75 | 21 | 300 | bowl | Nourishment | Egg Fried Rice | farmersdelight |
| Comfort | 14 | 0.75 | 21 | 300 | bowl | same | Noodle Soup | farmersdelight |
| Comfort | 14 | 0.75 | 21 | 300 | bowl | same | Pumpkin Soup | farmersdelight |
| Comfort | 12 | 0.8 | 19.2 | 180 | bowl | same | Beef Stew | farmersdelight |
| Comfort | 12 | 0.8 | 19.2 | 180 | bowl | same | Carrot Soup | moredelight |
| Comfort | 12 | 0.8 | 19.2 | 180 | bowl | same | Fish Stew | farmersdelight |
| Comfort | 12 | 0.8 | 19.2 | 180 | bowl | same | Vegetable Soup | farmersdelight |
| Comfort | 10 | 0.9 | 18 | 180 | bowl | same | Corn Soup | corn_delight |
| Comfort | 10 | 0.6 | 12 | 180 | bowl | same | Ender Congee | ends_delight |
| Comfort | 9 | 0.6 | 10.8 | 60 | bowl | Nourishment | Mangosteen Cake | fruitsdelight |
| Comfort | 8 | 0.7 | 11.2 | 60 | bowl | same | Bone Broth | farmersdelight |
| Comfort | 8 | 0.6 | 9.6 | 180 | bowl | Nourishment | Ender Noodle | ends_delight |
| Comfort | 8 | 0.6 | 9.6 | 60 | bowl | Nourishment | Pear with Rock Sugar | fruitsdelight |
| Comfort | 8 | 0.6 | 9.6 | 180 | bowl | same | Shulker Soup | ends_delight |
| Comfort | 8 | 0.6 | 9.6 | 120 | bowl | same | Strider Moss Stew | nethersdelight |
| Comfort | 7 | 0.5 | 7 | 180 | bowl | Nourishment | Creamed Corn | corn_delight |
| Comfort | 7 | 0.375 | 5.25 | 150 | copper_cup | same | Baked Cod Stew Cup | miners_delight |
| Comfort | 7 | 0.375 | 5.25 | 150 | copper_cup | same | Chicken Soup Cup | miners_delight |
| Comfort | 7 | 0.375 | 5.25 | 150 | copper_cup | same | Noodle Soup Cup | miners_delight |
| Comfort | 7 | 0.375 | 5.25 | 150 | copper_cup | same | Pumpkin Soup Cup | miners_delight |
| Comfort | 6 | 0.6 | 7.2 | 180 | bowl | same | Bell Pepper Soup | rusticdelight |
| Comfort | 6 | 0.4 | 4.8 | 90 | copper_cup | same | Beef Stew Cup | miners_delight |
| Comfort | 6 | 0.4 | 4.8 | 30 | bowl | Nourishment | Cooked Rice | farmersdelight |
| Comfort | 6 | 0.4 | 4.8 | 90 | copper_cup | same | Fish Stew Cup | miners_delight |
| Comfort | 6 | 0.4 | 4.8 | 90 | copper_cup | same | Vegetable Soup Cup | miners_delight |
| Comfort | 4 | 0.5 | 4 | 300 | shulker_bowl | same | Chorus Stew | endersdelight |
| Comfort | 4 | 0.5 | 4 | 300 | bowl | same | Chorus Stew | endersdelight |
| Comfort | 4 | 0.4 | 3.2 | 300 | shulker_bowl | same | Endermite Stew | endersdelight |
| Comfort | 4 | 0.4 | 3.2 | 300 | bowl | same | Endermite Stew | endersdelight |
| Comfort | 4 | 0.35 | 2.8 | 30 | copper_cup | same | Bone Broth Cup | miners_delight |
| Comfort | 2 | 0.6 | 2.4 | 60 | glass_bottle | same | Creamy Corn Drink | corn_delight |
| Comfort | 1 | 0.1 | 0.2 | 10 | - | Nourishment | Slice of Yucca Cake | create_central_kitchen |
| Comfort | 0 | 0 | 0 | 180 | glass_bottle | same | Dandelion Tea | farmersrespite |
| Comfort | 0 | 0 | 0 | 270 | glass_bottle | same | Dandelion Tea | farmersrespite |
| Comfort | 0 | 0 | 0 | 60 | glass_bottle | same | Lavender Tea | delightful |
| Nourishment | 18 | 0.5 | 18 | 300 | bowl | same | Roasted Dragon Steak | ends_delight |
| Nourishment | 15 | 0.6 | 18 | 180 | - | same | Ender Bamboo Rice | ends_delight |
| Nourishment | 14 | 1.1 | 30.8 | 300 | bowl | same | Glow Ink Pasta | miners_delight |
| Nourishment | 14 | 0.9 | 25.2 | 300 | bowl | same | Passion Fruit Glazed Duck | abnormals_delight |
| Nourishment | 14 | 0.9 | 25.2 | 300 | bowl | same | Perch with Mushrooms | abnormals_delight |
| Nourishment | 14 | 0.9 | 25.2 | 240 | bowl | same | Plate of Stuffed Hoglin Snout | nethersdelight |
| Nourishment | 14 | 0.8 | 22.4 | 180 | bowl | same | Cooked Rice with Beef | moredelight |
| Nourishment | 14 | 0.8 | 22.4 | 180 | bowl | same | Cooked Rice with Chicken Cuts | moredelight |
| Nourishment | 14 | 0.8 | 22.4 | 180 | bowl | same | Cooked Rice with Porkchop | moredelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Bowl of Stuffed Pumpkin | farmersdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Coffee-Braised Beef | rusticdelight |
| Nourishment | 14 | 0.75 | 21 | 180 | bowl | same | Grilled Salmon | farmersdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Plate of Honey Glazed Ham | farmersdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Plate of Roast Chicken | farmersdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Plate of Shepherd's Pie | farmersdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Roasted Mutton Chops | farmersdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Seasoned Arthropods | miners_delight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Squid Ink Pasta | farmersdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Vegetable Noodles | farmersdelight |
| Nourishment | 13 | 0.9 | 23.4 | 180 | - | same | Deluxe Cheeseburger | delightful |
| Nourishment | 12 | 1 | 24 | 300 | bowl | same | Cornbread Stuffing | corn_delight |
| Nourishment | 12 | 1 | 24 | 300 | bowl | same | Nachos | corn_delight |
| Nourishment | 12 | 1 | 24 | 180 | bowl | same | Pasta With Veggieballs | miners_delight |
| Nourishment | 12 | 0.9 | 21.6 | 300 | bowl | same | Seared Venison | abnormals_delight |
| Nourishment | 12 | 0.8 | 19.2 | 300 | bowl | Comfort | Acacia Blossom Soup | alexsdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Bell Pepper Pasta | rusticdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Creamy Pasta with Chicken Cuts | moredelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Creamy Pasta with Ham | moredelight |
| Nourishment | 12 | 0.8 | 19.2 | 300 | bowl | same | Duck Noodles | abnormals_delight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | Comfort | Fig Chicken Stew | fruitsdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Fried Calamari | rusticdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Fried Chicken | rusticdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Fried Mushrooms | rusticdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Lychee Chicken | fruitsdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Mushroom Rice | farmersdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Orange Chicken | fruitsdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Orange Marinated Pork | fruitsdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Pasta with Meatballs | farmersdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Pasta with Mutton Chop | farmersdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Pineapple Marinated Pork | fruitsdelight |
| Nourishment | 12 | 0.8 | 19.2 | 300 | bowl | same | Plate Of Glazed Arachnid Limbs | miners_delight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Steak and Potatoes | farmersdelight |
| Nourishment | 12 | 0.6 | 14.4 | 300 | bowl | same | Bowl of Steamed Dragon Egg | ends_delight |
| Nourishment | 12 | 0.6 | 14.4 | 180 | bowl | same | Vegan Steak And Potatoes | miners_delight |
| Nourishment | 11 | 0.9 | 19.8 | 300 | bowl | same | Venison with Bamboo Shoots | abnormals_delight |
| Nourishment | 11 | 0.3 | 6.6 | 30 | - | same | Chunkwich | delightful |
| Nourishment | 10 | 0.9 | 18 | 180 | bowl | same | Black Cod | farmersrespite |
| Nourishment | 10 | 0.9 | 18 | 120 | bowl | same | Grilled Strider | nethersdelight |
| Nourishment | 10 | 0.8 | 16 | 300 | bowl | same | Bowl Of Stuffed Squid | miners_delight |
| Nourishment | 10 | 0.8 | 16 | 180 | bowl | same | Dune Platter | abnormals_delight |
| Nourishment | 10 | 0.8 | 16 | 60 | - | same | Golden Nutritional Bar | miners_delight |
| Nourishment | 10 | 0.8 | 16 | 300 | bowl | same | Kangaroo Pasta | alexsdelight |
| Nourishment | 10 | 0.8 | 16 | 300 | bowl | Comfort | Kangaroo Stew | alexsdelight |
| Nourishment | 10 | 0.8 | 16 | 300 | bowl | same | Lobster Pasta | alexsdelight |
| Nourishment | 10 | 0.8 | 16 | 180 | bowl | same | Stir Fried Shulker Meat | ends_delight |
| Nourishment | 10 | 0.75 | 15 | 120 | bowl | same | Plate of Stuffed Hoglin Ham | nethersdelight |
| Nourishment | 10 | 0.7 | 14 | 300 | bowl | same | Plate of Dragon Leg with Sauce | ends_delight |
| Nourishment | 10 | 0.6 | 12 | 60 | bowl | same | Bacon and Eggs | farmersdelight |
| Nourishment | 10 | 0.6 | 12 | 300 | bowl | Comfort | Bowl of Dragon Meat Stew | ends_delight |
| Nourishment | 10 | 0.6 | 12 | 60 | bowl | same | Ratatouille | farmersdelight |
| Nourishment | 10 | 0.55 | 11 | 30 | bowl | same | Field Salad | delightful |
| Nourishment | 10 | 0.55 | 11 | 30 | - | same | Nutritional Bar | miners_delight |
| Nourishment | 10 | 0.5 | 10 | 180 | - | same | Nut Butter and Jam Sandwich | delightful |
| Nourishment | 10 | 0.4 | 8 | 180 | bowl | Comfort | Cave Soup | miners_delight |
| Nourishment | 9 | 0.7 | 12.6 | 60 | bowl | same | Pike with Beetroot | abnormals_delight |
| Nourishment | 9 | 0.5 | 9 | 180 | - | same | S'more | delightful |
| Nourishment | 8 | 0.65 | 10.4 | 60 | bowl | same | Plate Of Fake Meatloaf | miners_delight |
| Nourishment | 8 | 0.6 | 9.6 | 300 | bowl | same | Plate of Grilled Shulker | ends_delight |
| Nourishment | 8 | 0.6 | 9.6 | 120 | bowl | same | Plate of Stuffed Hoglin Roast | nethersdelight |
| Nourishment | 8 | 0.45 | 7.2 | 60 | bowl | Comfort | Insect Stew | miners_delight |
| Nourishment | 7 | 0.9 | 12.6 | 300 | bowl | same | Bowl of Stuffed Shulker | endersdelight |
| Nourishment | 7 | 0.7 | 9.8 | 180 | bowl | Comfort | Enderman Gristle Stew | ends_delight |
| Nourishment | 7 | 0.45 | 6.3 | 180 | bowl | Comfort | Bat Soup | miners_delight |
| Nourishment | 6 | 0.4 | 4.8 | 90 | copper_cup | Comfort | Onion Soup Cup | miners_delight |
| Nourishment | 5 | 0.3 | 3 | 150 | copper_cup | Comfort | Rabbit Stew Cup | miners_delight |
| Nourishment | 5 | 0.2 | 2 | 90 | copper_cup | Comfort | Cave Soup Cup | miners_delight |
| Nourishment | 4 | 0.4 | 3.2 | 300 | shulker_bowl | same | Ender Paella | endersdelight |
| Nourishment | 4 | 0.4 | 3.2 | 300 | bowl | same | Ender Paella | endersdelight |
| Nourishment | 4 | 0.4 | 3.2 | 300 | shulker_bowl | same | Pearl Pasta | endersdelight |
| Nourishment | 4 | 0.4 | 3.2 | 300 | bowl | same | Pearl Pasta | endersdelight |
| Nourishment | 4 | 0.225 | 1.8 | 30 | copper_cup | Comfort | Insect Stew Cup | miners_delight |
| Nourishment | 3 | 0.3 | 1.8 | 90 | copper_cup | Comfort | Beetroot Soup Cup | miners_delight |
| Nourishment | 3 | 0.3 | 1.8 | 90 | copper_cup | Comfort | Mushroom Stew Cup | miners_delight |
| Nourishment | 3 | 0.225 | 1.35 | 90 | copper_cup | Comfort | Bat Soup Cup | miners_delight |
| Nourishment | 1 | 0.1 | 0.2 | 10 | - | same | Slice of Pumpkin Cake | create_central_kitchen |
| Nourishment | 1 | 0.05 | 0.1 | 10 | - | same | Moss | miners_delight |
| Nourishment | 0 | 0 | 0 | 60 | glass_bottle | Comfort | Azalea Tea | delightful |

### A.2 Ported foods (1.20-era values, comparison only, 64)

| Effect | Hunger | Mod | Sat | Seconds | Hands back | Form rule says | Food | Mod id |
|---|---:|---:|---:|---:|---|---|---|---|
| Comfort | 14 | 0.8 | 22.4 | 480 | bowl | same | Overnight Oats | vintagedelight |
| Comfort | 14 | 0.75 | 21 | 300 | bowl | Nourishment | Cacciatore | veggiesdelight |
| Comfort | 14 | 0.75 | 21 | 300 | bowl | same | Turnip Beef Stew | veggiesdelight |
| Comfort | 13 | 0.85 | 22.1 | 300 | bowl | same | Pickle Soup | vintagedelight |
| Comfort | 12 | 0.8 | 19.2 | 180 | bowl | same | Garlic Chicken Stew | veggiesdelight |
| Comfort | 12 | 0.8 | 19.2 | 300 | bowl | same | Ghostly Chili | vintagedelight |
| Comfort | 12 | 0.8 | 19.2 | 180 | bowl | Nourishment | Potato Noodles | veggiesdelight |
| Comfort | 12 | 0.8 | 19.2 | 180 | bowl | Nourishment | Sweet Potato Pancakes | veggiesdelight |
| Comfort | 10 | 0.7 | 14 | 30 | bowl | same | Broccoli Soup | veggiesdelight |
| Comfort | 10 | 0.7 | 14 | 30 | bowl | same | Cauliflower Soup | veggiesdelight |
| Comfort | 10 | 0.7 | 14 | 30 | bowl | same | Creamy Morel Soup | fungidelight |
| Comfort | 10 | 0.7 | 14 | 30 | bowl | Nourishment | Mashed Potatoes | veggiesdelight |
| Comfort | 8 | 0.75 | 12 | 300 | bowl | same | Oatmeal | vintagedelight |
| Comfort | 8 | 0.7 | 11.2 | 300 | empty_mason_jar | Nourishment | Apple Sauce Jar | vintagedelight |
| Comfort | 8 | 0.6 | 9.6 | 180 | bowl | Nourishment | Coleslaw | veggiesdelight |
| Comfort | 7 | 0.65 | 9.1 | 300 | empty_mason_jar | Nourishment | Glow Berries Jam Jar | vintagedelight |
| Comfort | 7 | 0.65 | 9.1 | 300 | empty_mason_jar | Nourishment | Sweet Berries Jam Jar | vintagedelight |
| Comfort | 3 | 0.7 | 4.2 | 60 | glass_bottle | Nourishment | Apple Sauce Bottle | vintagedelight |
| Comfort | 2 | 0.6 | 2.4 | 60 | glass_bottle | Nourishment | Glow Berries Jam Bottle | vintagedelight |
| Comfort | 2 | 0.6 | 2.4 | 60 | glass_bottle | Nourishment | Sweet Berries Jam Bottle | vintagedelight |
| both | 7 | 0.7 | 9.8 | 180 | empty_mason_jar | Nourishment | Relish Jar | vintagedelight |
| both | 2 | 0.7 | 2.8 | 30 | glass_bottle | Nourishment | Relish Bottle | vintagedelight |
| Nourishment | 16 | 0.55 | 17.6 | 300 | bowl | same | Horror Lasagna | brewinandchewin |
| Nourishment | 15 | 0.95 | 28.5 | 300 | - | same | The Burger | vintagedelight |
| Nourishment | 14 | 0.9 | 25.2 | 180 | - | same | Stuffed Burrito | vintagedelight |
| Nourishment | 14 | 0.75 | 21 | 420 | bowl | same | Fiery Fondue | brewinandchewin |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Garlic Rice With Cauliflower | veggiesdelight |
| Nourishment | 14 | 0.75 | 21 | 180 | bowl | same | Lasagna Slice | veggiesdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Mutton Chops with Truffle | fungidelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Pasta with Cheese | brewinandchewin |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Pork Marsala With Mushrooms | fungidelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Rabbit with Morels | fungidelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Rice And Vegetables | veggiesdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Stuffed Zucchinis | veggiesdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | - | same | Turnip Cake | veggiesdelight |
| Nourishment | 12 | 1 | 24 | 420 | bowl | same | Scarlet Pierogi | brewinandchewin |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Cauliflower Kuku | veggiesdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | Comfort | Creamy Onion Soup | brewinandchewin |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Pasta With Broccoli | veggiesdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Roasted Vegetables | veggiesdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Shakshouka | veggiesdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Steak And Broccoli | veggiesdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Steak With Mushrooms | fungidelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Stuffed Morels | fungidelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Truffle Pasta | fungidelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Vegetable Omelet | brewinandchewin |
| Nourishment | 12 | 0.75 | 18 | 300 | bowl | same | Pad Thai | vintagedelight |
| Nourishment | 11 | 0.8 | 17.6 | 300 | - | same | Century Egg | vintagedelight |
| Nourishment | 10 | 0.9 | 18 | 180 | bowl | same | Inky Cap Scrambled Eggs | fungidelight |
| Nourishment | 10 | 0.6 | 12 | 30 | bowl | same | Dandelion Greens And Eggs | veggiesdelight |
| Nourishment | 10 | 0.6 | 12 | 30 | bowl | same | Fish And Chips | veggiesdelight |
| Nourishment | 10 | 0.6 | 12 | 300 | bowl | same | Garlic Baked Cod | veggiesdelight |
| Nourishment | 9 | 0.75 | 13.5 | 180 | bowl | same | Cheese Pasta | vintagedelight |
| Nourishment | 9 | 0.7 | 12.6 | 180 | empty_mason_jar | same | Pepper Jam Jar | vintagedelight |
| Nourishment | 8 | 0.65 | 10.4 | 300 | - | same | Stuffed Bell Pepper | veggiesdelight |
| Nourishment | 6 | 0.65 | 7.8 | 30 | - | same | Pickled Egg | vintagedelight |
| Nourishment | 6 | 0.3 | 3.6 | 30 | - | same | Pickle | vintagedelight |
| Nourishment | 6 | 0.3 | 3.6 | 30 | - | same | Pickled Pepper | vintagedelight |
| Nourishment | 6 | 0.2 | 2.4 | 30 | - | same | Pickled Onion | vintagedelight |
| Nourishment | 5 | 0.75 | 7.5 | 30 | - | same | Meat Pizza Slice | vintagedelight |
| Nourishment | 5 | 0.4 | 4 | 30 | - | same | Pickled Beetroot | vintagedelight |
| Nourishment | 4 | 0.75 | 6 | 30 | - | same | Cheese Pizza Slice | vintagedelight |
| Nourishment | 3 | 0.2 | 1.2 | 180 | empty_mason_jar | same | Vinegar Jar | vintagedelight |
| Nourishment | 1 | 0.2 | 0.4 | 60 | glass_bottle | same | Vinegar Bottle | vintagedelight |
| Nourishment | 1 | 0 | 0 | 120 | tankard | same | Pale Jane | brewinandchewin |
| Nourishment | 1 | 0 | 0 | 60 | tankard | same | Rice Wine | brewinandchewin |

