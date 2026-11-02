# Creative Delight: juice, tea and coffee rules (owner, 2026-11-01)

Owner decisions for the drink families. Nothing here is built. It refines TODO `food/drinks #8` (standard rule),
`food/drinks #10` (azalea tea and tea audit), `merges/drinks #1` (dandelion) and `flowers #5` (azalea flower merge).
Item state comes from `kubejs/exported/items_by_mod/` and `fluids.json` (2026-11-01). Section 6 lists what is still open.

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
| Azalea | 2 flowering azalea (bush or leaves), 2 azalea flowers | kettle: 1 of each, 250 mb green tea; heated mixing: the kettle cost + 1 extra ingredient | Flowers: Twigs' and Immersive Weathering's for now; merge them (`flowers #5`). Better effects. Fixes the missing recipe (`drinks #10`) |
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
| Matcha (plain) | the existing matcha craft + 1 extra matcha powder | |
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

### 4.1 Fluids and mixing

- Every coffee (coffee, dark, milk, chocolate, pumpkin, syrup, honey, cherry blossom) is registered as a fluid, with a
  fluid recipe wherever the bottle-less form applies (no bottle in the pot or kettle craft gives the fluid).
- Every coffee gets a mixing recipe. Coffee and dark coffee need +3 roasted beans over their kettle cost.
- Coffee + milk mixes to milk coffee.
- Milk coffee + the additive (honey, chocolate, syrup, pumpkin, sweet blossom and cherry leaves) mixes to that derivative.
- A derivative can also be made from another milk coffee derivative... read as: derivative + milk coffee + the specific
  additive mixes to the same derivative (confirm).

## 5. Parked

- **Long and strong variants** (Respite's potion-style tiers) of every tea, coffee and juice: pinned until the effects of
  teas, coffees and juices are audited and rules set. New teas do not get long or strong variants before that.
- Effects to design in that audit: lavender, azalea, sea pickle; review cloud latte.

## 6. Open

- Plain matcha: "extra matcha" read as matcha powder; the milkshake still has no rule.
- Azalea, lavender, sea pickle mixing: "an extra ingredient" read as one more of the tea's own pot/kettle ingredient on top
  of the kettle cost (not the 2.2 tea leaf and sugar-like); confirm.
- Coffee mixing: "+3 coffee beans" read as added to the kettle cost; the "coffee milk" derivative line (4.1) read as milk
  coffee; heated or unheated mixing for coffee not stated (assumed heated like tea).
- Kiwi and turnip teas need item ids; coffee fluids need ids and a bottle rule (what the bottle-less recipe is for
  Rustic's bottle-based coffees).
- Hot cocoa, apple cider, mango and salmonberry milkshake and the matcha milkshake are untouched.
