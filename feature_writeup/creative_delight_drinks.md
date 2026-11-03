# Creative Delight: juice, tea and coffee rules (owner, 2026-11-01)

Owner decisions for the drink families. **Built 2026-11-02 (section 8), Testing.** It refines TODO `food/drinks #8` (standard rule),
`food/drinks #10` (azalea tea and tea audit), `merges/drinks #1` (dandelion) and `flowers #5` (azalea flower merge).
Item state comes from `kubejs/exported/items_by_mod/` and `fluids.json` (2026-11-01). **Owner answers of 2026-11-02 are folded in
(sections 3, 4, 6, 7); nothing is open except the two review notes in section 7.**

Terms: "pot" = Cooking Pot, "kettle" = the patched Kettle (250 mb per craft), "heated mixing" = Create mixer with a
heater, "unheated mixing" = Create mixer without heat (shapeless craft where there is no mixer), "sugar-like" = the
sugar tag family (sugar, honey, honeycomb as the pack already treats them). Every input that has a slice form is accepted
whole or sliced in every recipe below.

## 1. Fruit juice rule

| Where | Inputs | Output |
|---|---|---|
| Pot | 4 fruit, 1 sugar-like, 1 bottle | 1 juice bottle |
| Kettle | 2 fruit, 250 mb water | 250 mb juice |

Applies to: bayberry, cantaloupe (Fruits Delight hamimelon), chorus fruit, lychee (new), hawberry (new), kiwi, lemon,
mango (new), mangosteen (new), melon, orange, peach (new), pear, carrot, and turnip (Turnip Water is renamed Turnip Juice
and follows the rule).

Exceptions:

| Juice | Pot | Kettle |
|---|---|---|
| Dandelion | 2 dandelion leaves, 2 sugar cane | 1 dandelion leaf, 1 sugar cane, 250 mb water |
| Nautilus | kelp, apple, sweet berries, plated nautilus shell (the nautilus shell is used to plate) | kelp, sweet berries, plated nautilus shell, 250 mb water |
| Pickerelweed, sea grape | 2 sugar, 2 of the ingredient | 1 sugar, 1 of the ingredient |

Not added: prickly pear juice and passion aloe nectar (left as they are).

## 2. Tea

### 2.1 Standard tea

250 mb of the matching juice + any tea leaf (green, yellow, black) + one extra item per tea (default: another fruit).
Applies to every tea not listed in 2.3. Extra item where named: lychee tea = spore blossom, dandelion tea = dandelion.
New standard teas: bayberry, cantaloupe, carrot, lemon, melon, orange, pear, kiwi, turnip. Existing items moving to the
rule: hawberry, mango, mangosteen, peach, lychee cherry (Fruits Delight), dandelion (Respite).

### 2.2 Mixed teas (heated Create mixing)

Every standard tea and every tea in 2.3 can also be made in a heated Create mixer: the base tea cost + one more tea leaf +
one sugar-like. Named: bayberry (new), cantaloupe (new), carrot (new), chorus (new), lychee (spore blossom), dandelion,
hawberry, lemon (new), mango, mangosteen, melon (new), orange (new), peach, pear (new); kiwi and turnip follow as standard
teas.

### 2.3 Tea exceptions

| Tea | Pot | Kettle / mixing | Note |
|---|---|---|---|
| Azalea | 2 flowering azalea (bush or leaves), 2 azalea flowers | kettle: 1 of each, 250 mb green tea; heated mixing: the kettle cost + 1 extra ingredient | Flowers: Twigs' and Immersive Weathering's for now; merge them (`flowers #5`). Better effects. Fixes the missing recipe (`drinks #10`). "Extra ingredient" in mixing = one more of the tea's own pot / kettle ingredient (confirmed 2026-11-02) |
| Lavender | 2 pink flowers (pink tulip, peony, lilac, allium), 2 of (amethyst shard or Quark lavender / Serene blossom leaves) | kettle: 1 of each, 250 mb green tea; heated mixing: the kettle cost + 1 extra ingredient | Better effects |
| Sea pickle (replaces Sea Cucumber Tea) | 2 sea pickles + 1 extra sea pickle | kettle: 2 sea pickles, 250 mb sea grape or pickerelweed juice; heated mixing: the kettle cost + 1 extra ingredient | Better effects. `hostiledelight:seacucumbertea` becomes this tea |
| Chorus tea (was Ender Nectar) | chorus juice recipe + 1 extra ender eye | kettle: 250 mb chorus juice, 1 any tea leaf, 1 ender eye | Rename `delightful:ender_nectar` |
| Chorus fruit milk tea | chorus fruit, grain or dried flower, + 1 extra dried chorus flower | kettle: same item + 250 mb milk | Ender's Delight item |
| Chorus fruit bubble tea (was Bubble Tea) | - | heated mixing: 250 mb chorus fruit milk tea + 1 extra ender pearl grain; or the milk tea crafting recipe with ender pearl grain | Create Central Kitchen already names the fluid `chorus_fruit_bubble_tea` |
| Builder's Tea (Create) | water + tea leaf + milk + 1 extra tea leaf | kettle: 250 mb water, tea leaf, milk | Heated mixing as the other teas (2.2) |

Teas that keep their current crafting recipe: **rose hip ("red"), yellow, black, gamblers (roasted coffee beans, glow
berries), purulent (nether wart, fermented spider eye), chorus flower (ghast tear, dried chorus flower)**. Green, yellow
and black are the tea leaf kinds; green tea is the base and is untouched.

## 3. Lattes and matcha

| Drink | Pot | Other |
|---|---|---|
| Cloud latte | 250 mb milk, cloud in a bottle, cocoa beans + 1 extra cloud in a bottle | Review effect and duration |
| Matcha (plain) | the existing matcha craft + 1 extra matcha powder (confirmed 2026-11-02: powder). Owner 2026-11-02: this is the Matcha Latte, the input drink of Ice Berry Matcha (row below); same item | |
| Matcha latte | honey becomes sugar-like, folded into the pot craft + 1 extra matcha powder | |
| Ice berry matcha (was Berry Matcha Latte) | - | unheated mixing / shapeless: 250 mb matcha, 1 ice or ice cube, 1 of any berry + 1 extra ice and berry |

## 4. Coffee

Every coffee bean used in a drink is the roasted bean (Rustic Delight's coffee is the coffee used; Respite's coffee
already merges into it).

| Coffee | Pot | Kettle |
|---|---|---|
| Coffee | 3 roasted beans | 2 roasted beans, 250 mb water |
| Dark coffee | 6 roasted beans | 2 roasted beans, 250 mb coffee |
| Milk coffee | 3 roasted beans, 1 milk | 250 mb coffee, 1 milk |
| Chocolate, pumpkin, syrup, honey coffee | 3 roasted beans, 1 milk, 1 ingredient | 250 mb milk coffee, 1 ingredient |
| Cherry blossom coffee | 3 roasted beans, 1 milk, sweet blossom and cherry leaves | 250 mb milk coffee, sweet blossom and cherry leaves |

Ingredient per flavour: chocolate = chocolate or cocoa, pumpkin = pumpkin, syrup = Rustic syrup, honey = honey.

### 4.1 Coffee in the Kettle and the flavour chain (owner, 2026-11-02)

The Kettle makes the same milk coffee and flavoured coffees as the pot, by any number of steps. A flavoured coffee is
reached three ways, all giving the same item:

| Route | Kettle | Pot (hand) |
|---|---|---|
| Step by step | coffee tank + milk -> milk coffee; milk coffee tank + additive -> flavoured | 3 roasted beans + 1 milk, then the additive on that |
| Straight from plain coffee | coffee tank + milk + additive -> flavoured (one craft, 5 ingredients counting the beans) | 3 roasted beans + 1 milk + additive (the table in 4) |
| Straight from beans | 2 roasted beans + 250 mb water + milk + additive | same |

Built today (`farmersrespite/kettle.js` section 2): coffee from beans on water, milk coffee from the coffee tank, the flavours only
from the milk coffee tank, dark coffee from the coffee tank. To add: the flavours from the plain coffee tank with milk +
additive in the same craft, and (mixing) the same chain below. Dark coffee keeps its own line (not a flavour base).

### 4.2 Fluids and mixing

- All eight coffees are already registered as fluids with 250 mb <-> bottle pairs (`fluid_dict.js` `f.coffee` is Central
  Kitchen's; the seven others are `creativeengineering:*_coffee`, `kettle.js` BOTTLES). Nothing new to register; the bottle-less
  rule is the existing Spout / Item Drain pair.
- Every coffee gets a **heated** Create mixing recipe (confirmed 2026-11-02). Coffee and dark coffee need +3 roasted beans
  over their kettle cost.
- Coffee + milk mixes to milk coffee. Milk coffee + the additive (honey, chocolate, syrup, pumpkin, sweet blossom and
  cherry leaves) mixes to that flavour; coffee + milk + additive mixes to the flavour straight; plain coffee to flavoured in
  one mixer step is allowed (any number of steps).
- A flavour made from another flavour is not a route: flavours branch off milk coffee or coffee, never off each other.

## 5. Parked

- **Long and strong variants** (Respite's potion-style tiers) of every tea, coffee and juice: pinned until the effects of
  teas, coffees and juices are audited and rules set. New teas do not get long or strong variants before that.
- Effects to design in that audit: lavender, azalea, sea pickle; review cloud latte.
- **Milkshakes: pinned (owner, 2026-11-02) as a completely separate cooking / food family**, not part of the juice, tea
  or coffee rules. The matcha, mango and salmonberry milkshakes wait for that family's own rule (TODO
  `food/drinks #13`).

## 6. Ids (generated 2026-11-02, in line with the family)

Dict key = `i.<key>` in `item_dict.js`; food dict key = `drink_<name>` for teas, `fruit_juice_<name>` for juices (grammar
13.2). Namespace: pack-added items live under `creativeengineering:` (as `canned_*`); third-party ids stay as they are
and only get a new display name. Fluids follow the Respite pattern (`f.<name>_tea`, `creativeengineering:` since the
teas are not Central Kitchen's). Not registered yet: these are the reserved names for the build.

| Drink | Item id | Dict key | Food dict key | Fluid |
|---|---|---|---|---|
| Kiwi tea | `creativeengineering:kiwi_tea` | `kiwi_tea` | `drink_kiwi_tea` | `creativeengineering:kiwi_tea` |
| Turnip tea | `creativeengineering:turnip_tea` | `turnip_tea` | `drink_turnip_tea` | `creativeengineering:turnip_tea` |
| Bayberry tea | `creativeengineering:bayberry_tea` | `bayberry_tea` | `drink_bayberry_tea` | same |
| Cantaloupe tea (Fruits Delight calls it hamimelon) | `creativeengineering:hamimelon_tea` | `hamimelon_tea` | `drink_hamimelon_tea` | same |
| Carrot tea | `creativeengineering:carrot_tea` | `carrot_tea` | `drink_carrot_tea` | same |
| Lemon tea | `creativeengineering:lemon_tea` | `lemon_tea` | `drink_lemon_tea` | same |
| Melon tea | `creativeengineering:melon_tea` | `melon_tea` | `drink_melon_tea` | same |
| Orange tea | `creativeengineering:orange_tea` | `orange_tea` | `drink_orange_tea` | same |
| Pear tea | `creativeengineering:pear_tea` | `pear_tea` | `drink_pear_tea` | same |
| Chorus tea (was Ender Nectar) | `delightful:ender_nectar` (id kept) | `ender_nectar` | `fruit_juice_ender_nectar` -> `drink_chorus_tea` | `creativeengineering:chorus_tea` |
| Lychee juice (new) | `creativeengineering:lychee_juice` | `lychee_juice` | `fruit_juice_lychee` | `creativeengineering:lychee_juice` |
| Hawberry juice (new) | `creativeengineering:hawberry_juice` | `hawberry_juice` | `fruit_juice_hawberry` | same |
| Mango juice (new) | `creativeengineering:mango_juice` | `mango_juice` | `fruit_juice_mango` | same |
| Mangosteen juice (new) | `creativeengineering:mangosteen_juice` | `mangosteen_juice` | `fruit_juice_mangosteen` | same |
| Peach juice (new) | `creativeengineering:peach_juice` | `peach_juice` | `fruit_juice_peach` | same |

Renames only (ids unchanged): Turnip Water -> Turnip Juice (`veggiesdelight:turnip_water`, existing `fruit_juice_`
key to add), Sea Cucumber Tea -> Sea Pickle Tea (`hostiledelight:seacucumbertea`), Bubble Tea -> Chorus Fruit Bubble Tea
(`ends_delight:bubble_tea`), Berry Matcha Latte -> Ice Berry Matcha (`delightful:berry_matcha_latte`). Mixed (heated
mixer) teas reuse the same item ids; no separate item.

## 7. Reviews noted (owner, 2026-11-02)

- **Hot cocoa and apple cider review.** Both stay untouched by these rules; review them against the juice / tea /
  coffee families once the rules are built (TODO `food/drinks #14`).
- **Milkshake family** pinned (section 5, `food/drinks #13`).

## 8. Build (2026-11-02, TODO `food/drinks #15`)

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-DRK-01, -02, -03, -05; rerun pending (batch fault) SQ-DRK-04, -06.

Status **Testing**: in the `mods/` Creative Delight jar of 2026-11-02 14:01; scratch r1102b SQ-DRK-01, -02, -03, -05 PASS, SQ-DRK-04 (Spout half) and -06 batch faults (the drain line and every other -06 check passed; -06 queries `drink_fruits/kiwi`, which does not exist by design) (deep scan 2026-11-02). Not played.

| Part | Where |
|---|---|
| 14 new drink items (9 teas, 5 juices), 17 new fluids (the 14, chorus tea, sea grape juice, pickerelweed juice) | CD `kubejs_startup/creativeengineering/drinks.js`; ids `item_dict.js`, `fluid_dict.js`, food dict keys `food_dict.js`, food type `drink` (`food_types.json`) |
| Every rule in sections 1 to 4 | `kubejs/data/creativedelight/kubejs/creativeengineering/drinks.js`; input tags `drink_tags.js` |
| Superseded recipes taken out | `farmersrespite/kettle.js` (dandelion tea, melon juice, lavender tea, the five Fruits Delight teas; matcha latte takes any sugar-like; chocolate and pumpkin coffee take the tags), `veggiesdelight/recipes.js` (pot), `endersdelight/recipes.js` (pot), `abnormals_delight/upgrade_aquatic.js` (pot), `delightful/recipes.js` (grid berry latte) |
| Renames | client `assets/creativedelight/kubejs/creativeengineering/names.js`; fluid names in CD `fluids.js` |
| Art | `kubejs/assets/creativeengineering/textures/item/<id>.png`: teas recoloured from Farmer's Respite's green tea, juices from Farmer's Delight's melon juice (MIT, `drink_rules_art_notice.txt`) |

Build choices (not decided by the owner; change freely):

- New items: 1 hunger, 0.2 modifier, always drinkable, 32 per stack, bottle back; **no effects** (effects wait for the
  audit, section 5).
- Bayberry juice is Fruits Delight's `bayberry_soup` (already named Bayberry Juice, fluid `fruitsdelight:bayberry_soup`), so it
  needed no new id. Sea grape and pickerelweed juice got pack fluids (`creativeengineering:`) because the Sea Pickle Tea's
  Kettle brews on them.
- Grid recipes of the juices stay (Fruits Delight's, Farmer's Delight's melon juice, Ender's Delight's chorus juice, Oceanic's sea
  grape juice); the rule replaces pot and Kettle recipes only.
- Juices with no mixer recipe of their own (the 5 new ones) mix heated at the Kettle cost (2 fruit + 250 mB water). Existing juice
  mixer recipes are unchanged.
- Fruits Delight's tea pot recipes stay, and so do Slice & Dice's bottle copies of them; the 2.2 fluid mixer recipe is added next to them.
- Mixed teas without a fluid (azalea, sea pickle) mix into the bottle (a glass bottle is one of the mixer inputs).
- Builder's Tea pot uses a water bottle as "water". Chorus fruit milk tea: the pot keeps the milk.
- Coffee pots: Rustic's own recipes already had the rule's counts; only the beans (roasted only) and the chocolate / pumpkin
  inputs change. Heated milk coffee takes 250 mB of milk fluid.

Not built:

- **Plain matcha**: settled 2026-11-02 (owner): plain matcha is the input drink of Ice Berry Matcha, i.e. the Matcha Latte (`delightful:matcha_latte`), which already has the +1 powder. No new item.
- **Kettle "straight from beans" flavours** (beans + water + milk + additive): the Kettle takes two ingredients.
- **Serene blossom leaves** for lavender tea: no such item found; lavender takes amethyst shards and `#forge:lavender` (Quark
  lavender blossom leaves).
- **`flowers #5` item merge**: only the recipe side (`#creativeengineering:azalea_flowers` holds both); the two flower items
  are still separate.
- Display Delight's placed-drink names ("Placed Ender Nectar", "Placed Iced Berry Matcha Latte") are generated: rerun
  `tools/gen_display_delight_names.py` after a `/dumpnames`.

Tests: SQ-DRK-01 to -06, CQ-DRK-01 ([scratch_queue.md](testing/scratch_queue.md)); Bayberry Juice (the renamed `fruitsdelight:bayberry_soup`, TODO `food/drinks #9`): SQ-DRK-07, CQ-DRK-02.

