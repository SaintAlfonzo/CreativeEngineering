# Mixed jars (Supplementaries jars holding several kinds)

## 0. At a glance

| Part | Status | Where |
|---|---|---|
| Jar families (tags), every cookie and the families below jarable | BUILT 2026-10-21 | `kubejs/data/creativedelight/kubejs/creativeengineering/jar_tags.js`, dict keys `i.t_jar_cookies`, `i.t_jar_family_*` (item_dict, next to `i.candy`) |
| Mixing in a placed jar (one item per slot, filled bottom to top) | BUILT 2026-10-21 | patcherMod `mixin/SupplementariesJarTileMixin`, `mixin/ItemDisplayTileAccessor`, rules in `compat/JarMixing` |
| Layers drawn per item (placed jar, jar item) | BUILT 2026-10-21 | `mixin/SupplementariesJarRendererMixin`, `mixin/SupplementariesJarItemRendererMixin` (client) |
| Jar item tooltip, one line per kind | BUILT 2026-10-21 | `mixin/SupplementariesJarItemTooltipMixin` |
| WTHIT "Contents" line lists kinds | BUILT 2026-10-21 | `compat/WthitHolderContents` ([wthit_lines.md](wthit_lines.md) 2.3) |
| Every soup and stew as a jar soft fluid (section 9) | built, scratch server PASSED 2026-10-22 (SQ-MJ-01, SQ-MJ-02: registry, pour in and out, containers; data, KubeJS tree); in game: colours and WTHIT (MJ-11, MJ-12) | `kubejs/data/creativeengineering/moonlight/soft_fluids/`, `tools/gen_soup_soft_fluids.py` |

Scratch server 2026-10-21: `/jartest` 18 pass, 0 fail (server side: tags, storage, item handler, comparator, old jars,
jar item). patcherMod jar installed in `mods/` 2026-10-21. In game: MJ-01 to MJ-10 pending (rendering, tooltip, WTHIT are
client-only and untested).
TODO `baking/baked-goods #11`. Knobs: Supplementaries `[functional.jar]` capacity (12) and jar_cookies; which items
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

- **Broken soft fluid containers** (2026-10-25, TODO `cooking/kitchen #18`): an audit of all 136 jar fluids (mod jars,
  nested ones and the pack's) for container ids that are not items. Fixed by same-path overrides in `kubejs/data/`
  (above mod data): Moonlight's `lava.json` (the lava bottle's empty `minecraft:bottle` -> `minecraft:glass_bottle`),
  Supplementaries' 7 Neapolitan milkshake and smoothie fluids (`minecraft:<id>` -> `neapolitan:<id>`), and Moonlight's
  `milk.json` (+ Splash Milk's milk bottle). Bayberry Juice's fluid is hand-written now (it left the soup type).
  Bottled foods no jar takes: TODO `items/jars #3`. Tests SQ-CDB-06, -07.

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

### Planned from the scratchpad (2026-10-24)

Brief: [briefs/items_jars.md](briefs/items_jars.md). Nothing here is built.

- [TODO](todo/TODO.md) `items/jars #1`: capacity 60 (config `[functional.jar] capacity`, now 12) with every other jar value scaled to the same ratios;
  raw fruit holds 36 (a per-family cap). Soft fluids and item caps all need scaling; old jars need a conversion. Open: per-family caps.
- [TODO](todo/TODO.md) `items/jars #2`: new contents: all candy mixes, cotton candy, diamond, emerald, redstone (opaque red with black accents, holds 36,
  glows slightly; glowstone and gunpowder follow), coal, charcoal. New families in `jartags.js`, a pile look in the renderer mixins.
- [TODO](todo/TODO.md) `items/jars #3`: more foods: raw and cooked fruit, candy, honeycomb, Hostile Delight cookies, jams, dried vanilla, kelp, dried
  kelp, tobacco leaves, seashells and the lava bottle ([TODO](todo/TODO.md) `cooking/kitchen #18`: Moonlight's lava soft fluid lists
  the lava bottle with an invalid empty item, `minecraft:bottle`, so a datapack override with `glass_bottle` is the probable fix).
  This overturns the "left out" list in section 4 for fruit and jams.
- [TODO](todo/TODO.md) `items/jars #4`: soups and stews take 1 jar space instead of 2 (soft fluid `capacity` and `divider` in the generator); enchanted
  ones glint in the jar. Open: 1 of 12 or 5 of 60 once `#1` lands.
- [TODO](todo/TODO.md) `items/jars #5`: storage for pickled veg: a crate or large jars. Open: recommended a crate.
- [TODO](todo/TODO.md) `items/jars #6`: why hyperxp and toast look like a family: they are in `#supplementaries:cookies` but in no family; the Jar Storage
  tab's last "one kind per jar" row cycles every such item. Reword that row.

## 5. File map

- [kubejs/data/creativedelight/kubejs/creativeengineering/jar_tags.js](../kubejs/data/creativedelight/kubejs/creativeengineering/jar_tags.js): families
- [kubejs/startup_scripts/_lib/item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js): `i.t_jar_cookies`, `i.t_jar_family_*`
- [compat/JarMixing.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/JarMixing.java): rules
- [mixin/SupplementariesJarTileMixin.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/SupplementariesJarTileMixin.java), [ItemDisplayTileAccessor.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/ItemDisplayTileAccessor.java)
- [mixin/SupplementariesJarRendererMixin.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/SupplementariesJarRendererMixin.java), [SupplementariesJarItemRendererMixin.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/SupplementariesJarItemRendererMixin.java)
- [mixin/SupplementariesJarItemTooltipMixin.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/SupplementariesJarItemTooltipMixin.java)
- [compat/WthitHolderContents.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/WthitHolderContents.java)
- [tools/jar_mixing_test/probe.js](../tools/jar_mixing_test/probe.js): scratch-server probe
- Brief: [briefs/baking_baked-goods.md](briefs/baking_baked-goods.md) #11

## 6. Offline checks

Scratch server: `tools/creative_labels_test/scratch_server.sh setup OUT`, swap `OUT/srv/mods/creativeengineering-*.jar`
for `development/creativeCommon/build/libs/`, copy `tools/jar_mixing_test/probe.js` to
`OUT/srv/kubejs/server_scripts/zz_jar_probe.js`, boot, run `/jartest`, grep `[jartest]` in `logs/latest.log`.
Checks J1 (tags), J2 (12 slots of 1), J3-J4 (player mix, refuse, take top), J5-J6 (item handler fill, order,
capacity, comparator), J7 (old one-stack jar splits), J8 (gap closes), J9 (other families, solo items), J10 (jar
item keeps layers). Rendering, tooltip and WTHIT need a client (section 7).

## 7. In-game test suite

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-JFP-03, -04, -05; rerun pending (batch fault) SQ-JFP-01, -02.
- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-JFP-01, -02.

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
- [ ] **MJ-11 Soup in, soup out** (counts: scratch server PASSED 2026-10-22, SQ-MJ-02; client: the colour and the WTHIT line): right-click an empty jar with a bowl of Corn Soup → soup-coloured liquid, one bowl
  back in hand; right-click with an empty bowl → Corn Soup. Six bowls fill it; a seventh stays in hand. WTHIT reads
  "Contents: Corn Soup (2 / 12)" after one bowl.
- [ ] **MJ-12 Every soup** (in and out: scratch server PASSED 2026-10-22, SQ-MJ-01 / SQ-MJ-02, all 24 as themselves; client: the colours): each soup in section 9 goes into an empty jar, and each shows a colour close to its texture
  (none black, purple-black or grey). Golden and Enchanted Golden soups come back out as the same item.
- [x] **MJ-13 Shulker bowl only** (scratch server PASSED 2026-10-22, SQ-MJ-02; nothing client-only): Chorus Stew into a jar, one bowl → the shulker bowl comes back; an empty wooden
  bowl on the jar does nothing; an empty shulker bowl takes the Chorus Stew out and the jar is empty again (2 units in,
  2 out, no extra stew).
- [ ] **MJ-14 Bottle soup** (one bottle: scratch server PASSED 2026-10-22, SQ-MJ-02; 12 bottles: client): a Bayberry Soup bottle pours 1 unit (12 bottles fill a jar); an empty glass bottle takes
  one back.

## 8. History

- 2026-10-24: planned from the scratchpad: capacity 60, new contents, more foods, soup space, pickle storage, the toast / hyperxp row (`items/jars #1` to `#6`). No code changed.
- 2026-10-23: 8 more soup fluids (Veggies, Vintage) and 5 hand-written drink fluids; the generator keeps the
  hand-written ones (its delete pass would have removed `nut_milk.json`). Not run in game (VTD-53).
- 2026-10-22: soups and stews in jars (section 9, TODO `dishes/ingredients #13`): 24 generated soft fluids, own
  colour each. Data only; not tested in game (MJ-11 to MJ-14).
- 2026-10-22 (doc audit): soups in jars not run yet; scratch checks queued (SQ-MJ-01, SQ-MJ-02,
  `tools/food_drinks_probe/food_drinks_probe.js`, `/fdtest soupjar`), colours and WTHIT stay in game.
- 2026-10-22 (second bulk run): SQ-MJ-01, SQ-MJ-02 PASS on the scratch server (24 soups; the first try failed only
  because Moonlight's soft-fluid map is empty before a player joins, probe fixed). MJ-13 done; MJ-11, MJ-12, MJ-14
  client halves stay in game.
- 2026-10-21: built. Cookie tags first (brief step (a)), then families, the slot-per-item jar, renderers, tooltip
  and WTHIT line. Scratch server (Forge 1.19.2 dedicated, the built jar swapped in): every mixin applied, `/jartest`
  J1-J10 18 pass, 0 fail.

## 9. Soups and stews in jars

Supplementaries jars hold liquids as Moonlight *soft fluids*, data files at
`data/<ns>/moonlight/soft_fluids/<name>.json`. A bowl of soup on a jar pours in 2 units (a jar holds 12, so 6 bowls);
an empty bowl on the jar takes one bowl (2 units) back out. In and out move the same amount, so a jar
cannot duplicate soup (a bottle soup is 1 unit both ways). Moonlight and Supplementaries ship 11 soups (beetroot soup, mushroom,
rabbit and suspicious stew; beef stew, baked cod stew, chicken soup, fish stew, pumpkin soup, vegetable soup, sopa de
macaco). The pack adds the other 24, so every item in `#creativeengineering:food_type/soup` and every Golden Food
soup and stew goes in a jar:

- Acacia Blossom Soup, Kangaroo Stew, Adzuki Stew, Bell Pepper Soup, Blazing Chili, Bone Broth, Carrot Soup, Cavern
  Explorer Stew, Miso with Bamboo Sprouts, Corn Soup, Creamy Onion Soup, Fig Chicken Stew, Noodle Soup, Sinigang,
  Strider Moss Stew;
- Chorus Stew and Endermite Stew, shulker bowl only. Ender's Delight's wooden-bowl versions are removed from the pack
  (removed_items.js) and the generator skips every removed item, so a wooden bowl takes nothing out of these jars;
- Bayberry Soup, a bottle: 1 unit per bottle, and Fruits Delight's bayberry soup fluid counts as the same liquid;
- Golden and Enchanted Golden Beetroot Soup, Mushroom Stew, Rabbit Stew (one fluid each);
- 2026-10-23: the 5 Veggies Delight soups and Vintage Delight's Pickle Soup, Ghostly Chili and Oatmeal (32 in all).

Bottled drinks of the two ports are hand-written files in the same folder, kept by the generator (`HAND_WRITTEN`):
Vintage's Nut Milk, Veggies' Carrot Juice, Dandelion Juice and Turnip Water (their tank fluids count as the same
liquid, `equivalent_fluids`) and Fermented Garlic Honey. Vintage's own jar contents ship in its jar.

**Generator:** `python3 tools/gen_soup_soft_fluids.py` (`/cegen run soup_soft_fluids`, reads `/dumpfood`'s
foods.json for each item's empty container) writes one json per uncovered soup and deletes stale ones; the folder is
the tool's, except the `HAND_WRITTEN` drink files. A soup a mod already covers is skipped (its colour stays the mod's). New soups: put the key in the soup
type in food_types.json and rerun. Applies on `/reload`.

**Colour:** each soup its own, decided 2026-10-22 (not one shared soup colour). The tool takes the largest
one-colour patch of the item texture that is not the bowl's colours; `COLOR_OVERRIDE` in the tool fixes the eight
where that picked garnish, shadow or a gold bowl (acacia blossom, bone broth, blazing chili, chorus, endermite,
sinigang, both golden mushroom stews). Contact sheet checked against the textures.

**Limits:** drinking from a placed jar (`drink_from_jar`) is off in this pack, so a jar of soup is storage only. The
curries (Coconut Curry, Tea Curry, Adzuki Curry) and the canned soups are not soups here and stay out. Display
Delight's placed soups are blocks, not bowls.

## 10. Pipes into and out of jars (2026-11-02, TODO `items/jars #7`)

Supplementaries 2.4.20 gives the jar no Forge fluid capability, so a Create pump or pipe ignored it. Creative Compat
`compat/JarFluidPipes` attaches one (Forge `AttachCapabilitiesEvent`, no edit of the Supplementaries jar) to every
Moonlight soft-fluid tank block entity: the jar and the goblet. Any Forge fluid pipe works the same way.

- **Units:** one bottle = 250 mB, so a 12-bottle jar holds 3000 mB.
- **Which fluids:** a Forge fluid goes in when a soft fluid lists it (`equivalent_fluids`): water, lava, milk, honey,
  chocolate, potions (`create:potion`, NBT kept: Potion, Bottle, CustomPotionEffects), tea and the other drinks that
  have a Forge fluid. Soups and drinks with no Forge fluid stay hand-only: a pipe puts nothing in and sees the jar as
  empty.
- **Part bottles:** pipes move a few mB a tick, so up to 249 mB waits in a buffer saved with the block (ForgeCaps, kept
  when the jar is picked up). Pipes see tank + buffer; the jar draws only whole bottles. Pouring a different liquid into
  the emptied jar by hand drops the buffer.
- **Refused:** a jar with items or a mob, the pony jar, or `jar_liquids` off.

Tests (in game; server half in the scratch queue, SQ-JFP-):

- [ ] **MJ-15 Pump in:** Create pump from a water tank into an empty jar → the jar fills to 12 bottles and stops; a
  glass bottle on it gives water bottles.
- [ ] **MJ-16 Pump out:** jar of 12 honey bottles, pump pulling from it into a Fluid Tank → the tank gets 3000 mB of
  honey, the jar empties.
- [ ] **MJ-17 Potion:** Spout or pump of Create potion fluid (Swiftness) into a jar → bottles out are Potions of
  Swiftness; a jar filled with Swiftness by hand also takes the piped Swiftness.
- [ ] **MJ-18 Refused:** jar holding cookies next to a pumping pipe → nothing goes in; jar of soup → a pump pulls
  nothing.
