# Mixed jars (Supplementaries jars holding several kinds)

## 0. At a glance

| Part | Status | Where |
|---|---|---|
| Jar families (tags), every cookie and the families below jarable | BUILT 2026-10-21 | `kubejs/server_scripts/tags/jartags.js`, dict keys `i.t_jar_cookies`, `i.t_jar_family_*` (item_dict, next to `i.candy`) |
| Mixing in a placed jar (one item per slot, filled bottom to top) | BUILT 2026-10-21 | patcherMod `mixin/SupplementariesJarTileMixin`, `mixin/ItemDisplayTileAccessor`, rules in `compat/JarMixing` |
| Layers drawn per item (placed jar, jar item) | BUILT 2026-10-21 | `mixin/SupplementariesJarRendererMixin`, `mixin/SupplementariesJarItemRendererMixin` (client) |
| Jar item tooltip, one line per kind | BUILT 2026-10-21 | `mixin/SupplementariesJarItemTooltipMixin` |
| WTHIT "Contents" line lists kinds | BUILT 2026-10-21 | `compat/WthitHolderContents` ([wthit_lines.md](wthit_lines.md) 2.3) |

Scratch server 2026-10-21: `/jartest` 18 pass, 0 fail (server side: tags, storage, item handler, comparator, old jars,
jar item). patcherMod jar installed in `mods/` 2026-10-21. In game: MJ-01 to MJ-10 pending (rendering, tooltip, WTHIT are
client-only and untested).
TODO `food/baked-goods #11`. Knobs: Supplementaries `[functional.jar]` capacity (12) and jar_cookies; which items
mix = the family tags.

## 1. Overview

A Supplementaries jar holds items from `#supplementaries:cookies`, stacked up as flat layers, normally only one kind
at a time. In this pack:

- every cookie and a set of other small foods go in a jar (the mods only tagged their own few);
- items of one **family** can share a jar: a jar of mixed cookies, of candied apples, of glazed berries ...;
- each layer keeps its own item, in the order put in; taking one out takes the top layer.

## 2. Reference

**Families** (`#creativeengineering:jar_family/<name>`; an item joins a jar when one family holds it and everything
already inside; items in no family still fill a jar on their own):

| Family | Members |
|---|---|
| `cookies` | vanilla, honey, sweet berry, green tea, mulberry, cherry, maple, bayberry, cranberry, lemon, persimmon, golden, enchanted golden cookie; uncanny cookies; Botania's Biscuit of Totality; gingerbread man |
| `candied_apples` | Create honeyed apple; Create Factory caramel, chocolate, dark, ruby and white chocolate apples |
| `candied_berries` | Create chocolate glazed berries; Confectionery dark, caramel, ruby and white chocolate glazed berries; Create Factory honey glazed berries; Neapolitan chocolate strawberries |
| `candies` | Supplementaries candy; Confectionery honey candy, candy cane, and the four shapes each of milk, dark, ruby and white chocolate candy; rock candy, salmonberry gummy (Delightful); mint candies, strawberry bean bonbons, vanilla fudge (Neapolitan); cocoa fudge (Brewin') |
| `marshmallows` | Confectionery marshmallow and its four glazed versions |
| `chocolate_bars` | Create bar of chocolate; Confectionery dark, ruby, white chocolate and caramel bars; Neapolitan chocolate bar, mint chocolate, vanilla chocolate fingers |
| `apples` | apple, golden apple, enchanted golden apple |
| `berries` | sweet and glow berries and their golden / enchanted golden versions; blueberry, cranberry, bayberry, hawberry (Fruits Delight); salmonberries; strawberries, white strawberries; mulberry (Upgrade Aquatic); Vinery cherry and its eight grapes |
| `dried_fruit` | dried persimmon, dried banana |

**Jarable, no family** (fills a jar alone): `moredelight:toast` (its mod's own tag), Create Enchantment Industry's
Hyperexperience bottle.

Examples: cookie, then lemon cookie, then golden cookie: one jar, three layers. Cookie then apple: the apple is
refused. Candy cane then honey candy: mixes. Toast then toast: fine; toast then cookie: refused.

## 3. What the pack changes

- **Tags** (`jartags.js`): the families above, each nested into `#supplementaries:cookies`.
- **Storage** (`SupplementariesJarTileMixin`): the jar has one slot per capacity point (12) instead of one slot
  holding a stack; each slot holds one item (`getMaxStackSize` 1).
  - Accepting (`canPlaceItem`, `canPlaceItemThroughFace`): jar_cookies on, no fluid or mob, room left, the item in
    `#supplementaries:cookies`, and `JarMixing.canMix`. Of the empty slots only the lowest accepts, so hoppers and
    funnels (Forge's unsided `InvWrapper`, which asks only `canPlaceItem`) fill in order and stop at capacity.
  - `addItem` fills the lowest empty slot; `extractItem` takes the top layer; `isFull` counts every slot;
    `resetHolders` clears every slot.
  - Load and save reorder the contents into one item per slot from the bottom: a jar saved before this change
    (one stack of 5 in slot 0) splits into 5 layers, and a gap from a middle extraction closes.
  - Comparator: Supplementaries uses `getRedstoneSignalFromContainer`, which now reads total / capacity.
- **Rendering** (client): the placed jar and the jar item draw each layer's own item, with Supplementaries'
  transforms and per-layer turn.
- **Tooltip** (jar item): one line per kind with its total ("Cookie x3", "Lemon Cookie x2").
- **WTHIT**: one kind keeps "Cookie (5 / 12)"; several read "3 Cookie, 2 Lemon Cookie (5 / 12)".

## 4. Limits and decisions

- **One slot per item, not one slot per run.** Runs (a stack per consecutive kind) would need fewer slots, but Forge's
  item handler inserts up to the slot limit into any slot, so a funnel could overfill the jar past capacity. One
  item per slot caps it naturally and keeps everything in the vanilla `Items` tag (client sync, the jar item's
  `BlockEntityTag` and the tooltip need nothing extra). The brief's `CE_Layers` list tag was dropped for this.
- **Families, not "any jarable item with any other".** Apples among cookies look wrong; families keep jars themed.
  A jar can mix only when one family covers everything in it, so an item in two families cannot bridge them.
- **Taking out:** a hand takes the top layer. Supplementaries' inventory tweak (right-click a jar item on a slot) and
  item handlers take from the slot they ask for; Supplementaries asks slot 0, so that path takes the bottom layer.
  The gap closes on the next save.
- **Eating from a placed jar** (`drink_from_jar`, off in this pack) would eat slot 0, the bottom layer.
- **Left out:** removed items (Buzzier Bees honey apple, Lightman's chocolate coins); fruit that is not small
  (lemons, oranges, peaches, pears, mangoes, kiwis ...: a jar of them reads as a fruit bowl); jams, jellos and cut
  slices (they have plates); `create_confectionery:gingerbread` (a slab piece); the canned cookies (cans); vanilla
  experience bottles (a jar takes those as XP fluid first).
- **Render look:** every layer is drawn as a flat item, so apples and bars stack like coins. Check in game (MJ-06).
- The jar capacity config (12) also sets the slot count. Lowering it drops the layers above the new capacity on load.

## 5. File map

- [kubejs/server_scripts/tags/jartags.js](../kubejs/server_scripts/tags/jartags.js): families
- [kubejs/startup_scripts/_lib/item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js): `i.t_jar_cookies`, `i.t_jar_family_*`
- [compat/JarMixing.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/JarMixing.java): rules
- [mixin/SupplementariesJarTileMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/SupplementariesJarTileMixin.java), [ItemDisplayTileAccessor.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ItemDisplayTileAccessor.java)
- [mixin/SupplementariesJarRendererMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/SupplementariesJarRendererMixin.java), [SupplementariesJarItemRendererMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/SupplementariesJarItemRendererMixin.java)
- [mixin/SupplementariesJarItemTooltipMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/SupplementariesJarItemTooltipMixin.java)
- [compat/WthitHolderContents.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/WthitHolderContents.java)
- [tools/jar_mixing_test/probe.js](../tools/jar_mixing_test/probe.js): scratch-server probe
- Brief: [briefs/food_baked-goods.md](briefs/food_baked-goods.md) #11

## 6. Offline checks

Scratch server: `tools/creative_labels_test/scratch_server.sh setup OUT`, swap `OUT/srv/mods/creativeengineering-*.jar`
for `development/patcherMod/build/libs/`, copy `tools/jar_mixing_test/probe.js` to
`OUT/srv/kubejs/server_scripts/zz_jar_probe.js`, boot, run `/jartest`, grep `[jartest]` in `logs/latest.log`.
Checks J1 (tags), J2 (12 slots of 1), J3-J4 (player mix, refuse, take top), J5-J6 (item handler fill, order,
capacity, comparator), J7 (old one-stack jar splits), J8 (gap closes), J9 (other families, solo items), J10 (jar
item keeps layers). Rendering, tooltip and WTHIT need a client (section 7).

## 7. In-game test suite

- [ ] **MJ-01 Cookies mix:** right-click an empty jar with a cookie, a lemon cookie, a golden cookie → three layers,
  each its own cookie, in that order.
- [ ] **MJ-02 Other family refused:** jar with cookies, right-click with an apple → nothing goes in.
- [ ] **MJ-03 Take top:** empty hand on the MJ-01 jar → you get the golden cookie, then the lemon cookie.
- [ ] **MJ-04 Each family:** a jar each of candied apples (caramel + honeyed apple), candied berries (two glazed
  berries), candies (candy cane + honey candy), marshmallows, chocolate bars, apples, berries (sweet berries + a
  grape), dried fruit → each mixes, and an item from another family is refused.
- [ ] **MJ-05 Every cookie jarable:** each cookie in the cookies row of section 2 goes into an empty jar and stacks to
  12 (was BG-J1 in the brief).
- [ ] **MJ-06 Look:** the MJ-04 jars render layer by layer; apples and bars look acceptable stacked flat.
- [ ] **MJ-07 Jar item:** break the MJ-01 jar → the item shows the three layers in hand and in the inventory, the
  tooltip reads "Cookie x1 / Lemon Cookie x1 / Golden Cookie x1"; place it again → same layers.
- [ ] **MJ-08 WTHIT:** look at a mixed jar → "Contents: 1 Cookie, 1 Lemon Cookie, 1 Golden Cookie (3 / 12)"; a one-kind
  jar → "Contents: Cookie (5 / 12)".
- [ ] **MJ-09 Hopper:** a hopper with 20 mixed cookies above an empty jar → the jar fills to 12 in order; a
  comparator next to it reads 15. An apple in the hopper stays there.
- [ ] **MJ-10 Old jar:** a cookie jar filled before this update (one kind, 5 cookies) still shows 5 and gives 5 back.

## 8. History

- 2026-10-21: built. Cookie tags first (brief step (a)), then families, the slot-per-item jar, renderers, tooltip
  and WTHIT line. Scratch server (Forge 1.19.2 dedicated, the built jar swapped in): every mixin applied, `/jartest`
  J1-J10 18 pass, 0 fail.
