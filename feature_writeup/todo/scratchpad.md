# Scratchpad

## Test updates

Client-only in-game tests for unreleased 1.1.01 (all work since 1.0.9 shipped, 2026-10-19), newest first. Tests the
scratch server fully covers are left out; where it covers part, the entry says so.

### Sweet Berry Pie and chocolate cake spout (newest)

- [ ] **OV-33 Chocolate cake spout** (recipe loaded: scratch test queued, SQ-BK-06; the Spout run: in game): a Spout with 500 mB chocolate over a Baked Cake Base on a depot gives a
  Chocolate Cake; JEI shows the filling recipe on the chocolate cake.
- [ ] **OV-32 Sweet Berry Pie** (recipes and can values: scratch test queued, SQ-SB-01; names and the oven screen: in game): the cheesecake shows as Sweet Berry Pie (whole, Slice of Sweet Berry Pie, Incomplete
  Sweet Berry Pie, both Display Delight plates; JEI search "cheesecake" by name finds nothing). Cooking Pot: 2 sweet
  berries + 2 sugar + empty can -> Canned Sweet Berry Pie Filling; 4 slices + empty can also craft one. Pie crust +
  full filling bakes a Sweet Berry Pie (pie tin) and crafts one by hand; an opened can does neither. Eating the can:
  4 servings, 3 hunger each, Speed 30 s.
- [ ] **CP-26 sweet berry pie** (scratch test queued, SQ-01): 4 Slice of Sweet Berry Pie + can -> Canned Sweet Berry Pie Filling (4 servings, 3 hunger,
  Speed 30 s); full can + pie crust -> Sweet Berry Pie by hand and in the oven (OV-32).

### Item merges 2.6 / 2.7: JEI tooltips and names

- [ ] **MG-21** JEI search froststone; craft 2 packed ice + 2 stone -> 10 Froststone blocks named as in 2.7; 2 Froststone. No "Permafrost" left on Quark's blocks
- [ ] **MG-20** Hover a green tea fluid in JEI -> Its effect line under the name (side effect of MG-16's hook)
- [ ] **MG-17** Hover the three cider items -> All "Apple Cider" with the same three effect lines
- [ ] **MG-16** Hover the three cider fluids in JEI (ingredient list and a Mixing recipe) -> All "Apple Cider"; under the name "Absorption (1:00)", "Absorption (1:30)", "Absorption II (0:30)" in blue

### Trinket Case

- [ ] **FX-24 Config off:** `trinketCaseAnyItem = false`, restart → only trinkets, rods and mana items fit.

- [ ] **FX-23 Trinket case equip:** a trinket in the case still quick-equips from its screen; cobblestone doesn't.
- [ ] **FX-22 Trinket case sort and search:** open the case with mixed items → Quark's sort button and search bar
  show; sort → items grouped, none lost; the sort keybind works; search dims non-matching slots; the deposit /
  restock buttons move items in and out.
- [ ] **FX-21 Trinket case, any item:** put cobblestone, a sword and a stack of food in a Trinket Case by clicking in
  its screen, by shift-click and by clicking the stack onto the case item in the inventory → all accepted; a shulker
  box and a second Trinket Case → refused. Close and reopen → contents kept.

### Item merges 2.1 to 2.5: real-player checks

- [ ] **MG-14** JEI Pebble Dirt and Rocky Dirt -> Pebble Dirt (Dave's, slab "Pebble Dirt Slab"): 2 dirt + 2 Twigs pebbles crossed -> 4, coarse dirt -> 8. Rocky Dirt (Twigs'): 2 dirt + 2 cobblestone -> 4, coarse dirt -> 8. One Rocky Dirt only, no rocky dirt -> dirt recipe
- [ ] **MG-13** Hover Vinery cider and apple juice -> "Hard Cider"; tooltip "Apple Wine and Hard Cider"
- [ ] **MG-07** Milk cauldron (bucket in), take with a glass bottle -> FD milk bottle
- [ ] **MG-05** Break jungle leaves (20+) -> No bananas
- [ ] **MG-04** Feed / tame a capuchin monkey with a Neapolitan banana -> Accepted
- [ ] **MG-02** `/give @s alexsmobs:banana 5` -> Becomes 5 Neapolitan bananas
- [ ] **MG-01** JEI: search banana, milk bottle, chocolate cake, diced, rocky dirt -> None of the five purged items show

### Baking overhaul

- [ ] **OV-31 Kept:** 7 cake slices -> cake still crafts; golden cookie / cake / pumpkin pie still craft from the
  finished item; muffins and tarts unchanged (OV-20).
- [ ] **OV-30 Cookies:** cookie dough + 2 cocoa beans bakes 12 cookies on the baking sheet; no cookie craft in the grid.
  Cookie dough + 2 Vinery cherries bakes 12 cherry cookies.
- [ ] **OV-29 Opened can:** eat one serving of a pie filling can; crust + that can doesn't start in the oven and
  gives nothing in the grid; a full can does both.
- [ ] **OV-28 Pies:** crust + 2 apples + sugar + honey bottle bakes an apple pie (pie tin), the bottle lands in a
  leftover slot. No apple pie recipe in the crafting grid. Cooking pot: 2 apples + 2 sugar + empty can in the container slot
  -> Canned Apple Pie Filling; crust + filling bakes an apple pie, and also crafts one by hand. Cheesecake: crust + 2
  sweet berries + 2 sugar in the pie tin.
- [ ] **OV-27 Cakes:** baked base + frosting + 2 strawberries crafts a strawberry cake; raw base + frosting + 2
  strawberries bakes one. Baked base + 3 frosting -> cake; baked base + 2 frosting + a cherry -> cake. Honey cake gives
  back 2 bottles.
- [ ] **OV-26 Bases:** raw cake base crafts from 3 flour (or cornmeal) + 2 egg + 2 sugar and from 3 dough + egg + 2
  sugar; the oven (cake pan) bakes a baked base from 2 flour + 2 egg + 2 sugar, from 2 dough + egg + 2 sugar, and from a raw
  base. Pie crust: 3 flour + egg + milk -> 3 and 3 dough + milk -> 3 by hand. None of the bases, crust, frosting or cookie dough
  work in a Create mechanical crafter or mixer, and JEI shows no Create or IE recipe for them. Frosting: sugar + 2 milk
  -> 2. Cookie dough: dough + 2 sugar (gingerdough look).
- [ ] **OV-25 Overhaul boot:** after a restart no KubeJS errors for baking.js; Frosting and Cookie Dough exist (cotton
  boll texture). JEI on any cake, pie or cookie shows only the new recipes plus `*_from_slices`. Uses still list the golden
  and canned recipes.

### What's New screen (changelog.md 7)

- [ ] **CL-12 fresh install:** delete `config/creativeengineering/changelog_seen.txt` → the bubble shows once.
- [ ] **CL-10 pause menu:** in a world, Esc → the book button right of "Back to Game", the bubble too if unread;
  Done returns to the pause menu.
- [ ] **CL-09 in development:** dev instance with bullets in `unreleased.md` → "In development" at the top, no
  popup for it; a payload staged with `--version X` has `X.md` with the date and no `unreleased.md`.
- [ ] **CL-08 resize:** change the window size and GUI scale with the screen open → text rewraps, nothing cut off;
  the bubble stays on screen.
- [ ] **CL-07 reload:** edit a bullet in the dev instance, F3+T, reopen → new text.
- [ ] **CL-06 update brings it back:** apply an update payload → the bubble shows again with the new version's
  number; versions skipped since the marker carry a `*` in the list on that first visit.
- [ ] **CL-05 popup:** with a read marker older than the newest version → the bubble bobs beside the book button
  on the title screen; open the changelog, go back → bubble gone; restart → still gone.
- [ ] **CL-04 links:** click a link → vanilla confirm screen; Yes opens the browser, No returns to the changelog.
- [ ] **CL-03 formatting:** family headings show their icons where given; New / Changed / Fixed / Removed labels
  coloured; bold, italic and backtick text styled; sub-bullets indented; no raw Markdown visible.
- [ ] **CL-02 screen:** press it → version list newest first, the newest selected, text wraps inside the panel,
  scrollbar works with the wheel and by dragging.
- [ ] **CL-01 button:** open the title screen → a book button right of Singleplayer, no overlap with Quark's Q
  button or any other button; tooltip "What's New".

### Leftover slots in the GUI and WTHIT (remainder_buffer.md, wthit_lines.md)

- [ ] **WL-33 Waiting line:** fill both of a pot's leftover slots with different items (cook with a milk bucket, then
  a honey bottle recipe, or use `/data`), then load a recipe that leaves a third kind → red "Waiting: leftover slots
  full"; take one slot out → the red line goes and the pot cooks. Repeat once on the dedicated server.
- [ ] **WL-32 Holding line:** cook hot cocoa with a milk bucket in an FD pot → "Holding: 1 Bucket". Bake a milk-bottle
  recipe in the oven → "Holding: 1 Glass Bottle" (note whether WTHIT's own item line lists the bottle as well). An
  empty pot shows no line.
- [ ] **RB-14 Recycle shows** (patcherMod 1.1.01): watch an FD pot cook a tomato-sauce bowl meal with the plating
  slot empty: the bowl appears in the plating slot for half a second, then the meal is plated. The same with hot
  cocoa and a milk bottle in the pot, and strong green tea in the kettle.
- [ ] **RB-12 Oven leftovers stay visible** (patcherMod 1.1.01): bake blueberry muffins (milk bottle) with the GUI
  open, take the muffins out: the bottle stays in the leftover slot. Close and reopen: still there. Take it with a
  click.

### Ovens: JEI pages and tooltips (oven.md)

- [ ] **OV-23 Other-oven tooltips:** CfB oven shows "Cooks food like a furnace and a smoker"; each of the 7 Fantasy
  Furniture ovens shows "Works like a smoker" (client log: `fantasyfurniture:*/oven -> 7 matches`).
- [ ] **OV-18 Counts on:** set `ovenIngredientCounts = true`, `/reload`: apple pie shows 4 slots (crust, 2 apples,
  sugar, sugar); 1 apple in the slot doesn't start, 2 does and takes exactly 1/2/1/1. Honey cake on 2 honey bottles in one
  slot gives back 2 bottles (leftover slots).
- [ ] **OV-16 Counts off (default):** JEI's apple pie shows 5 single slots (crust, 2 apple, 2 sugar-or-honey); it bakes
  with one item per slot and takes exactly those. The KubeJS log has no `over 6 slots, dropped` line (2.6.1).
- [ ] **OV-11 JEI:** "Baking" page lists the 48 recipes with the oven as catalyst and the bakeware left of the grid;
  counted ingredients show their stack size (apple pie with counts on: crust, 2 apples, sugar, sugar); clicking the GUI arrow opens
  it; "+" moves the counted amounts in.

### Kitchen: looks and screens (kitchen.md)

- [ ] **KT-12 Corner feeds the Cooking Table:** a Cooking Table connected to the corner (through counters) lists
  recipes from ingredients kept only in the corner.
- [ ] **KT-09 Corner GUI:** right-click a Kitchen Corner → a 3-row chest titled "Kitchen Corner".
- [ ] **KT-06 Fridge look:** a 2-tall fridge between two counters: no gaps at the sides, top flush with a full block
  next to it, a candle on top sits on it; the door opens and closes cleanly on both hinge sides; a dyed fridge keeps
  its colour.
- [ ] **KT-05 Board drawn on the worktop:** the board and an item on it sit on the counter top with no gap, on
  counters facing all four ways; set `cuttingBoardOffsetZ = -2`, restart, F3+A → it moves toward the door on each.

### Mixed jars: looks and WTHIT (mixed_jars.md)

- [ ] **MJ-08 WTHIT:** look at a mixed jar → "Contents: 1 Cookie, 1 Lemon Cookie, 1 Golden Cookie (3 / 12)"; a one-kind
  jar → "Contents: Cookie (5 / 12)".
- [ ] **MJ-07 Jar item:** break the MJ-01 jar → the item shows the three layers in hand and in the inventory, the
  tooltip reads "Cookie x1 / Lemon Cookie x1 / Golden Cookie x1"; place it again → same layers.
- [ ] **MJ-06 Look:** the MJ-04 jars render layer by layer; apples and bars look acceptable stacked flat.
- [ ] **MJ-01 Cookies mix:** right-click an empty jar with a cookie, a lemon cookie, a golden cookie → three layers,
  each its own cookie, in that order.

### Cooking recipes in JEI (recipes_cooking.md)

- [ ] **RC-69 Frying:** JEI shows no Thermopneumatic chips or tempura and no plant-oil tempura mixing; names read Fish
  Tempura, Raw Fish Tempura, Fries, Fish and Chips. Raw tempura from a pufferfish + batter. Pot: cooking oil + raw
  tempura -> Fish Tempura; cooking oil + 2 potato slices (Rustic) -> 2 Fries, also with diced potatoes; cooking oil +
  a raw cod + 2 slices with paper in the plate slot -> Fish and Chips; fries + cooked fish +
  paper in the grid -> Fish and Chips; its tooltip reads "The chips are fries. Don't tell the British."
- [ ] **RC-66 Expensive meals:** JEI shows a shapeless grid recipe from cooked ingredients for chorus stew, endermite
  stew, crab pasta, pumpkin soup, pasta with meatballs, dumplings (1), cavern explorer stew, squid ink pasta; none
  runs in an unheated basin. Craft one (pasta with meatballs: 2 beef patties + pasta + tomato sauce).
- [ ] **RC-65 Feast Plating tab:** JEI "Feast Plating" tab lists roast chicken, honey glazed ham, shepherd's pie, rice
  roll medley, stuffed pumpkin, nachos, popcorn box, pineapple fried rice, pizza and stuffed hoglin (2 pages), plus any
  KubeJS Delight feast. Roast chicken: 4 bowls -> 4 roast chicken, leftovers bowl + bone meal; popcorn box: empty
  hand, 4 caramel popcorn, paper "given"; pizza: any hand, 4 slices. Uses (U) on a bowl shows the tab; R on a roast
  chicken plate shows it. One plating in game matches the page. `latest.log` has `[jei] Feast Plating: N page(s)`.
- [ ] **RC-64 Shaped only:** crispy skewer, crawling sandwich and steak and potatoes have only shaped grid recipes.
- [ ] **RC-61 Quiche:** one shaped recipe in JEI cycling bacon / mushroom; one assembly; mushrooms first works.
- [ ] **RC-60 Burgers:** each burger has only a shaped grid recipe; JEI shows no milk cheeseburger; each assembly on
  bread works (the egg burger on a beef patty); a mechanical crafter with a burger pattern does nothing; CCK's bacon,
  chicken and egg sandwich assemblies still work.
- [ ] **RC-55 Heatless meals:** JEI shows the shapeless routes for vegetable soup, vegetable noodles, ratatouille and
  corn soup; each crafts by hand; the same items in a basin under an unheated mixer do nothing.
- [ ] **RC-19 Grinding mirrors:** JEI cornmeal: crushing wheels, Millstone and a mortar and pestle craft for corn,
  crate, kernels (3 -> 1) and kernel bag; JEI wheat flour: the hay bale on the Millstone and on the mortar (-> 13).

### Cans: sprites, AppleSkin, JEI (can_parity.md)

- [ ] **CP-24 manual only:** JEI's Create "Shapeless Mixing" / "Automated Crafting" categories list no `*_pie_manual_only`
  recipe; filling + crust in a basin under a mixer, in a mechanical crafter, or in an IE Assembler makes nothing. In a
  crafting terminal with the player's inventory (AE2) the recipe works. Canning recipes (empty can + food) still run in
  the mixer.
- [ ] **CP-22 removed:** Canned Food and the Hot Hot Hotpot are hidden in JEI; a butcher (any level) never offers Canned
  Food; the butcher's other trades are still there.
- [ ] **CP-21 Canned Goods values:** AppleSkin on Canned Cake shows 4 hunger and about 0.4 saturation (was 28 / 112);
  Canned Apples 5 hunger, 1.5 saturation (3 servings); Canned Suspicious Stew 8 hunger (8 servings, Canned Goods' own
  effects still apply). No Canned Goods can shows more saturation than its family's formula gives. Canned Cake: 1 cake
  + can, or 4 slices + sugar -> 4 fast servings.
- [ ] **CP-19 new cans:** every can of the 2026-10-21 build shows its family sprite (no purple/black) and its JEI
  recipe; startup log `[cans] registered 227 generated cans`; server log has no `[canning] ... skipped` line.

### Food: JEI and tooltips (food.md, placeable_food.md)

- [ ] **FO-30 Seasoning page:** after installing the patcherMod jar and restarting, `logs/latest.log` has
  `[jei] Seasoning page: 2 page(s), moved 1 crafting and 65 mixing recipe(s)` and `[jei] hid 66 seasoning recipe(s)`
  (counts may differ if Create: The Salt recipes are removed). Press U on Salt: a Seasoning tab with two pages
  (crafting table, mechanical mixer); the Crafting tab no longer shows the "food + salt" page. R on Cooked Beef: the
  Mixing tab has no cooked beef + salt mix; the Seasoning pages show only cooked beef -> salted cooked beef.
- [ ] **FO-29 glistering melon:** after a restart, hold a Glistering Melon Slice in survival with a full hunger bar and
  eat it: AppleSkin shows 4 hunger / 4.8 saturation (same as a Golden Melon Slice), no effect is applied. It still
  brews Healing in a brewing stand. `logs/kubejs/startup.log` has `[food values] 2 foods copied` and no warning.
- [ ] **PL-06 Own block:** hover Cake, Apple Pie and Pizza. → "Placeable" each, no "(while sneaking)". Cherry Wine → "Placeable (while sneaking)"; it places only on a sneak right click.

### Wine timing in WTHIT, single player, enchanting (time.md, archery.md)

- [ ] **TI-12 Cellar boost:** wine in a rack with no sky light -> WTHIT and tooltip show x2.0 (x2.5 until 2026-10-19, 3.5), `/wine info` shows the
  shorter time to the next level
- [ ] **TI-07 New single-player world:** set `daysPerSubSeason = 10`, create a world -> 24000 / 10, not Serene Seasons' 24000 / 8 (the pack's own 8 matches Serene Seasons' default, so it can't tell the two apart); set it back to 8
- [ ] **AR-14 Enchantments gone:** enchanting table with a book at level 30, many rolls → never Stockpile, Recovery or
  Cycle; librarians never sell them; an anvil refuses a book of one. `/give` an enchanted book with Cycle → red "REMOVED
  ITEM: DO NOT USE".

### Sticky notes and labels: client halves (sticky_notes.md)

- [ ] **SN-55 crafting blocked:** a labelled plank in the inventory grid → no result; set `blockLabelledCrafting =
  false` → it crafts, unlabelled output. Scratch server 2026-10-21: server half PASSED (after the FastWorkbench fix).
- [ ] **SN-54 drag:** Mouse Tweaks right-click drag with a written stack of notes across 5 cobblestone slots → only
  the first slot is labelled, one note used. Scratch server 2026-10-21: server half PASSED (5-tick gate; a real drag needs a client).
- [ ] **SN-53 copy:** written red "Iron" + 8 blank notes of mixed colours → 9 red "Iron" notes. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-51 performance:** 200 labels and 1,000 notes in view → JFR shows the icon renderer as the only per-frame
  cost.
- [ ] **SN-50 resource reload:** F3+T → notes, labels and icons still right.
- [ ] **SN-49 donors off:** no `sticky_it:` or `labels:` recipe or item in JEI. Scratch server 2026-10-21: server half PASSED (recipes and creative tabs; JEI needs a client).
- [ ] **SN-48 bowls stack:** hover a bowl, put it with fresh bowls → they stack.
- [ ] **SN-47 WTHIT:** a note → "Sticky Note", colour, text; a label → the chest's name and icon plus "Label: Diamond".
- [ ] **SN-46 distances:** pictures disappear past 16 blocks (paper stays), names past 6; change both in
  `creative_labels-client.toml` → they follow.
- [ ] **SN-45 floor label:** a label on a floor turns to your facing. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-44 break:** left-click the label → breaks like carpet, drops a blank label. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-43 open through:** empty-hand right-click the label → the chest opens. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-42 glow, feather:** glow ink sac → full-bright; ink sac → normal; feather → name under the icon. Scratch server 2026-10-21: server half PASSED (flags stored; the look needs a client).
- [ ] **SN-41 clear:** sneak + empty hand → picture cleared. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-40 dyed icon:** red dye → the icon turns red shades, the paper stays. Scratch server 2026-10-21: server half PASSED (dye stored; the icon needs a client).
- [ ] **SN-39 special renderers:** a chest, a shield, a Create cogwheel → like their inventory icons.
- [ ] **SN-38 stack tints:** labels showing a healing potion and dyed leather armour → their colours. Scratch server 2026-10-21: server half PASSED (picture NBT kept and stripped; the colours need a client).
- [ ] **SN-37 picture:** sneak + right-click with a diamond → the diamond in real colours, not used up. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-36 place:** a label on a chest front sits flush on its face. Scratch server 2026-10-21: server half PASSED (placement; the look needs a client).
- [ ] **SN-35 creative tab:** SN-30 and SN-34 in the creative inventory tab.
- [ ] **SN-34 eraser on items:** eraser on the cursor, right-click until no line is left → the stack merges with clean
  cobblestone. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-33 three lines:** a fourth line is refused, nothing used. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-32 off hand:** note in the main hand, sword in the off hand, sneak + right-click in the air → the sword has
  the line. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-31 anvil:** cobblestone left, written note right → output with the line, 1 level; one note used. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-30 inventory:** written note on the cursor, right-click 32 cobblestone in a chest → all 32 show the line;
  one note used. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-29 tooltips:** note, label and eraser show the lines of 3.9, each on its own line, no `[Shift]`.
- [ ] **SN-28 write in hand:** right-click a stack of notes in the air → screen; "Iron", Done → one written note, the
  rest unchanged.
- [ ] **SN-27 eraser in the grid:** red "Iron" + eraser → blank red note, eraser stays; that note + eraser → plain
  yellow. The eraser never breaks. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-26 colour in the grid:** 12 plain notes + red dye → 12 red notes; 12 red + blue dye → 12 red-blue mixes;
  the text of a written note survives; 8 plain notes + gunpowder → 8 creeper notes. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-25 stacking:** 64 plain notes stack; red notes stack with red; a written note doesn't stack with blank ones. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-24 any colour:** notes dyed with each of the 16 dyes, and mixes, look like their leather armour colour when
  placed and as items; place 200 plain notes → about 2 come out gold, with the firework spark. Scratch server 2026-10-21: server half PASSED (gold rate only: 4 of 400; colours need a client).
- [ ] **SN-23 JEI:** JEI lists the plain note and the four specials, with example recipes for the 16 dyes and the
  specials.
- [ ] **SN-22 water:** notes under water keep the water. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-21 pistons:** push a papered block → its paper drops as in SN-18. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-20 shrapnel, arrows, punches:** shoot and punch papered blocks → nothing pops.
- [ ] **SN-19 explosion:** TNT beside a papered wall that survives → no paper lost; TNT that destroys the wall → notes
  drop with data, labels blank, none destroyed by the blast. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-18 host mined:** mine a block with five notes and a label on another of its faces → notes drop with data,
  the label drops blank. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-17 break:** left-click one note → breaks about as fast as carpet, drops with its data; the others stay;
  only the hovered note is outlined; middle-click in creative gives that note. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-16 pass-through:** two notes on a furnace; right-click a note holding a coal → the furnace opens. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-15 covered face:** five notes on a crafting table → clicks between them don't open it; remove one → the
  empty spot opens it. Three notes on a stair side or two on its front never block anything. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-14 eraser on a placed note:** right-click a written red note with the eraser → text gone, still red; again →
  plain yellow. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-13 remove:** sneak + empty-hand right-click → that note in your inventory with colour and text. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-12 open:** empty-hand right-click → the note screen with Sticky It's bars, the text and the placing day; edit,
  Done → WTHIT shows the new text.
- [ ] **SN-11 dye placed:** red dye on one note → only it turns red, one dye used; then blue → a red-blue mix (the same
  colour as leather dyed red then blue); a special note refuses dye. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-10 model:** placed notes have Sticky It's curled shape and size on walls, floors and ceilings.
- [ ] **SN-09 notes or label:** a face with notes refuses a label and the other way round; notes on one face and a
  label on another face of the same cell both show. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-08 floor direction:** place the first note on a floor facing north, then one facing east → both point north;
  break all, place facing east → east. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-07 non-standard:** carpet, button, ladder, door, trapdoor, flower, head → no note. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-06 posts:** fence, wall, iron bars, glass pane, end rod, lightning rod, chain → one note on the centre of
  top and bottom only. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-05 slabs:** a bottom slab's top takes five; a top slab's bottom takes five; sides take two (the
  half's corners); a double slab takes five. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-04 stairs:** bottom and back faces take five; each side takes three (bottom corners, top corner on the back
  half); the front and the top take two each; for all four stair facings and upside-down stairs. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-03 chests and barrels:** plain right-click opens them; sneak + right-click places a note; five notes fit on
  a chest's front. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-02 nearest free spot:** click an occupied spot → the note lands on the nearest free spot. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-01 five spots:** right-click a stone face aiming at each corner and the centre → five notes, spaced, none
  overlapping; a sixth does nothing. Scratch server 2026-10-21: server half PASSED.

### WTHIT lines (wthit_lines.md)

- [ ] **WL-31 Every switch:** set each switch in the section 3 table to false in turn (or all at once, then back) and
  restart: its line or icon goes and the others stay. Expected when off: `iconBlockModel` → empty icon on an item-less
  DD plate; `iconForgePick` → skull pile icon empty; `iconSubstitutes` → powered flywheel icon empty;
  `holderPotionEffects` → no effect lines; `holderSingleName` → goblet "Water (1 / 1)"; `hiveBeesOnTop` → WTHIT's
  "2 Bee" line back and Bees at the end; `[wthit.state_lines] slices` → no Slices on a cake while feasts keep Servings;
  `campfireBurn` / `campfireRain` → that line only; `sheetmetalTank` → the bar only on the two ports; `kitchenStation`
  → no station lines; `remainderBuffer` → no Holding / Waiting lines; `wthitKeg` (brewinandchewin-client.toml) → no keg lines. Set them all back to true.
- [ ] **WL-30 Keg on the server:** WL-29 on the dedicated server.
- [ ] **WL-29 Keg:** an empty keg → "Temperature: Normal" (near a heat source "Warm"/"Hot"). Load a beer recipe at a
  valid temperature → "Fermenting: Beer n%" rising; move it somewhere the recipe can't run (wrong temperature) → red
  "Beer: Must Be ..." (the requirement the recipe book shows).
- [ ] **WL-28 Kitchen station:** an ME Kitchen Station on its own → red "Device Offline"; tied to an ME Security
  Terminal on a grid → green "Device Online" and "Connected to ME Security Terminal at x, y, z".
- [ ] **WL-27 Sheetmetal tank:** build and fill an IE sheetmetal tank → the same fluid bar on every part, including the
  two ports (once each).
- [ ] **WL-26 Rain countdown:** `/weather rain` beside the open-sky campfire → red "Rain: Going out in 8s" counting down,
  then it goes out and the lines disappear.
- [ ] **WL-25 Campfire lines:** a lit campfire on grass → "Burns forever", "Rain: Puts it out"; on a coal block → green
  "Rain: Safe (fuel block below)"; a soul campfire → "Burns forever" only.
- [ ] **WL-24 Others and override:** a respawn anchor with 2 charges → "Charges: 2 / 4"; 3 sea pickles → "Pickles: 3 /
  4". Copy `state_lines.json` to `kubejs/assets/creativeengineering/`, delete the pickle rule, F3+T → the Pickles line
  is gone. Remove the copy.
- [ ] **WL-23 Cauldrons:** a dyed water cauldron two-thirds full → "Level: 2 / 3"; a vanilla water cauldron → only
  WTHIT's fluid bar, no Level line; a Fruits Delight juice cauldron and a powder snow cauldron → Level lines.
- [ ] **WL-22 Eggs:** an Incubation hay nest with 3 eggs → "Eggs: 3 / 6"; a turtle egg cluster → "Eggs" and "Hatching".
- [ ] **WL-21 Cakes and cheese:** a cake with two slices eaten → "Slices: 5 / 7"; an FD apple pie, one slice → "Slices:
  3 / 4"; a Brewin' flaxen cheese wheel, cut twice → "Slices: 2 / 4" and no "Servings" line.
- [ ] **WL-20 Feast:** a Roast Chicken block, take one serving → "Servings: 3 / 4"; Rustic Delight honey pancakes, eat
  two → "Servings: 4 / 6".
- [ ] **WL-19 Hive on the server:** WL-16 on the dedicated server (the count comes from the server half).
- [ ] **WL-18 Hive options:** WTHIT's "Show Beehive Occupants" off → still Bees then Honey. "Show Beehive Honey Level"
  off → Bees only. `[wthit] hiveBees = false` → WTHIT's own lines back (names, "Honey Level: 3 / 5").
- [ ] **WL-17 Empty nest:** a bee nest with no bees → "Bees: 0 / 3" first, then "Honey Level: n / 5".
- [ ] **WL-16 Hive with bees:** a beehive with 2 bees inside (`/setblock ~ ~ ~ minecraft:beehive[honey_level=3]{Bees:[{EntityData:{id:"minecraft:bee"},MinOccupationTicks:100000},{EntityData:{id:"minecraft:bee"},MinOccupationTicks:100000}]}`)
  → "Bees: 2 / 3" first, "Honey Level: 3 / 5" under it, no "2 Bee" line.
- [ ] **WL-15 Cookie jar and cage:** 5 cookies → "Contents: Cookie (5 / 12)". A bee in a jar or cage → "Contents: Bee".
- [ ] **WL-14 Potion effects off:** `[wthit] holderPotionEffects = false` → only the Contents line.
- [ ] **WL-13 Jar potion:** pour Splash Potions of Healing II into a jar → "Contents: Splash Potion of Healing (1 / 12)"
  and a blue "Instant Health II" line under it. A jar of Potions of Regeneration (long) → "Regeneration (1:30)".
  Lingering Potion of Regeneration (long) → "Regeneration (0:22)" (¼). A harmful potion (Poison) → a red line.
- [ ] **WL-12 Goblet:** a goblet of water → "Contents: Water", no numbers.
- [ ] **WL-11 Jar fluid:** 6 bottles of honey in a jar → "Contents: Honey (6 / 12)"; fill to 12 → "(12 / 12)".
- [ ] **WL-10 Forge pick:** a Supplementaries skull pile (two skulls stacked) → the icon is the skull looked at; look at
  the other half → the other skull. A skull with candles → the skull.
- [ ] **WL-09 Substitute item:** look at a D&D powered flywheel (a furnace engine's flywheel while running) → Create's
  flywheel as the icon.
- [ ] **WL-08 Exclude list:** `"colored_water"` in `[wthit] iconExclude`, restart → the dyed cauldron's icon is empty.
  Remove it.
- [ ] **WL-07 Model icon outside DD:** a dyed Colored Water cauldron, an Immersive Posts post, a placed Delightful pie
  → their own models in the icon slot. A placed backpack → its backpack item.
- [ ] **WL-06 Other mods untouched:** a Farmer's Delight pie and a vanilla cake → WTHIT's normal icons.
- [ ] **WL-05 DD icon, modelled item:** a placed Plated Hamburger → its item icon (no purple cube).
- [ ] **WL-04 DD icon, item-less plate:** 3 cookies on a food plate → the icon shows the plate with 3 cookies.
- [ ] **WL-03 Toggles:** turning a plugin off in WTHIT's list removes its lines (spot-check `holder_contents`, `hive`,
  `state_lines`, `campfire`, `brewinandchewin:keg`); turn them back on.
- [ ] **WL-02 Plugin names:** they read Run Speed, Block Model Icons, Crop Growth, Coaster Drinks, Jar Contents, Hive
  Bees and Honey, Servings Slices Eggs and Levels, Campfire Burn and Rain, Sheetmetal Tank Contents, ME Kitchen
  Station, Keg Fermentation; no raw `config.waila.plugin_...` keys.
- [ ] **WL-01 Plugins load:** no WTHIT plugin error in `logs/latest.log`; WTHIT's config screen, Plugins, lists the ten
  `creativeengineering:` plugins and `brewinandchewin:keg`.

### Names in game (names.md)

- [ ] **NM-13 Mod names in game:** hover any Create Deco item in JEI -> blue mod line "Create: Deco"; a Pretty Pipes
  pipe -> "Pretty Pipes"; WTHIT on a Farmer's Delight stove -> "Farmer's Delight"; Mods screen lists "Death Knell",
  "Target Dummy" and "Creative Engineering (Alfonzo)".
- [ ] **NM-12 In game:** JEI search `witchmobility` shows "Goldenwood Broom" (was `item.witchmobility.goldenwood_broom_item`);
  `@floralench lily` shows "Lily of the Valley Bush"; WTHIT on an Upgrade Aquatic wall coral fan shows "Acan Coral
  Fan"; a sconce on a wall shows "Ender Sconce".

### Baby mobs: looks, sounds, config (baby_mobs.md)

- [ ] **BM-07 Config [game]:** `babyModels = false` -> babies render as half-size adults; `squidBabyChance = 1` -> every
  squid after the first of a group is a baby
- [ ] **BM-04 Sounds, drops [game]:** baby sounds are high-pitched; killing a baby drops nothing and no experience
- [ ] **BM-03 Looks [game]:** a baby squid / glow squid / dolphin shows the 26.1 baby shape and texture (stubby body,
  short tentacles; short dolphin with a round head), floating at the right height; the hitbox (F3+B) is small; a glow
  squid baby still glows and dims when hit

### Effects in JEI (effects.md)

- [ ] **EF-19 Sunny stays hidden:** search JEI for "Sunny" → no effect entry (same as EF-11, after the config was
  confirmed).
- [ ] **EF-18 Category colours:** `/give @s create_confectionery:bar_of_white_chocolate`, hover it → Rest is blue.
  Hover the dragon shovel → Sensory Perception and Scared Still are red. In `config/creativeengineering-common.toml`,
  delete the `create_confectionery:rest` line under `[effects]` and save → Rest goes red again with no restart.
- [ ] **EF-17 Fixed descriptions:** JEI → Comfort reads "Heals half a heart every 4 seconds..."; Absorption says
  "hearts", not "hearths"; Teleport says "random safe spot"; Travel Fatigue reads "Prevents fast travel."
- [ ] **EF-16 Potion carriers:** JEI → Regeneration. The potion bucket (three kinds), potion bundles (three kinds),
  the IE potion bullet and PneumaticCraft gun ammo each show filled with a Regeneration potion.
- [ ] **EF-15 JEI item lists:** check `logs/latest.log` for `jeed_providers.js: 83 effect provider recipes` and no
  `jeedProvider` warnings. Then open these pages in JEI:
  - Rest: four Create Confectionery chocolates.
  - Allure: a brew vial, flask, incense stick and blood pendant, all of Allure.
  - Shocked: the Create Crafts & Additions tesla coil.
  - Flammable: the IE and IP fuel buckets, the chemthrower and the four wooden armor pieces.
  - Phantom Bane: the beacon and the four echo armor pieces.
  - Health Boost: the Vinery standard (banner), Solaris and Stal Wine.
  - No Minimap: a command block.

### Food bars in other screens (tooltips.md)

- [ ] **TT-30 AE2:** request a crafting job for bread; hover bread in the crafting plan and in the CPU status table → food bar; in the pattern encoding terminal, put bread in a processing input and hover it → food bar with the amount lines
- [ ] **TT-29 Patchouli:** in any Patchouli book page that shows a food item, hover it → food bar, and the "(Shift for recipe)" line still there
- [ ] **TT-28 Wares:** open a delivery agreement requesting food, hover the requested item → food bar
- [ ] **TT-27 FTB Quests:** hover an item task (no custom title) for a food, a food item reward, and a food item linked in a quest description → food bar on each; hovering a quest button still shows FTB's plain tooltip
- [ ] **TT-26 Pretty Pipes terminal:** store bread in a pipe network, hover it in the item terminal → stored count on the first line and the food bar
- [ ] **TT-25 TrashSlot:** trash a cooked steak, hover the trash slot → food bar
- [ ] **TT-24 Vinery recipe book:** open a Vinery station's recipe book, hover a wine result → food values shown
- [ ] **TT-23 JEI keg recipe:** JEI Fermenting page of beer, hover the result tank and the tankard drawn inside it → fluid text, and the food bar of the drink
- [ ] **TT-22 Keg tank:** fill a keg with beer, hover the tank in the keg GUI → fluid lines plus the tankard's food bar; the recipe book's tank preview the same
- [ ] **TT-21 Central Kitchen guides:** open a Cooking Guide set to a cooking pot meal, hover its result slot → food bar
- [ ] **TT-20 Kettle and Oven:** same in a Farmer's Respite kettle (tea) and in the Oven's finished slot → food bar under the "Served on" line
- [ ] **TT-19 Cooking pot meal slot:** cook a stew in a Farmer's Delight cooking pot with no bowl in the container slot, hover the finished-meal slot → name, "Served on: Bowl" line and AppleSkin's food bar

### Brewing and Vinery: screens, models, tooltips, WTHIT, JEI (alcohol.md)

- [ ] **AL-96 Coaster log (12):** after the restart no "Failed to get model" or "coaster model JSON" errors from Brewin' in `logs/latest.log`
- [ ] **AL-95 Old coaster contents (12):** a coaster that already held a Vinery wine before the change keeps it, draws it flat, and gives it back on a right-click
- [ ] **AL-94 WTHIT on a coaster (12):** look at a coaster with beer, beer, apple -> body "Coaster: 3 / 4", "2x Beer", "Apple"; sneak-right-click with an empty hand (coaster put away) -> "Coaster: 3 / 4 (put away)"; an empty coaster -> "Coaster: 0 / 4"; `[coaster] wthit = false` in `creativeengineering-common.toml` -> no lines
- [ ] **AL-93 Tooltip follows the tag (12):** "Placeable on a coaster" on the Hamburger and on a beer, not on a Vinery wine or a Farmer's Respite tea
- [ ] **AL-92 Display Delight foods on a coaster (12):** put a Hamburger, Bacon and Eggs, a Cake Slice and a Corn Delight Taco on one coaster (plain right-click) -> each drawn with its Display Delight model, about bowl size, none of them a flat item, none sticking far past the tray
- [ ] **AL-91 Coaster takes modelled items only (12):** right-click an empty coaster with a Vinery wine, a Farmer's Respite tea and a stone block -> none goes on; with a beer, an apple and FD hot cocoa -> each goes on with its own model; `[coaster] taggedItemsOnly = false` in `brewinandchewin-common.toml` -> the wine goes on again, drawn flat
- [ ] **AL-88 Tooltip name (11):** Beer, Vodka, the Bellini, Bottle of Wodka and a wine show Quark's grey saturation word as "Intoxicating Drink" (not "Disgusting Snack"); rotten flesh still says "Disgusting Snack"; the effect lines read "Intoxication (m:ss)"; a wine lists its own effect, then Intoxication and Nausea; `[alcohol] quarkLine = false` brings "Disgusting Snack" back
- [ ] **AL-86 Intoxication everywhere (11):** drink Red Wine, the Bellini and Bottle of Wodka (one at a time, after milk) -> the hunger bar turns Intoxicated for 1:30, no Intoxication icon or particles; eat bread -> hunger fills, saturation doesn't
- [ ] **AL-85 Drink food values (11):** hover Beer, Red Wine, the Bellini and Bottle of Wodka -> AppleSkin shows half a rotten (green) shank and no saturation; drink one at full hunger -> it can be drunk, hunger goes up by half a shank, saturation doesn't
- [ ] **AL-82 Slot rules (10.16):** dirt won't go in the ingredient grid or the container slot by hand or shift-click; a tankard goes in the container slot; a honey bottle goes in the fluid slot by shift-click
- [ ] **AL-78 Tankard slot icon (10.16):** open an empty keg -> the container slot shows the grey tankard outline, not black and purple
- [ ] **AL-77 Tooltips (10.13, split 10.18):** furnace, brazier, blaze burner, campfire -> "Heats the surrounding environment" with "Useful for keg brewing and wine casking" on the next line; snow block, ice, permafrost -> "Cools the surrounding environment" and the same second line; keg -> "Sensitive to hot and cold blocks in the environment"; an FD cooking pot on a lit furnace still doesn't cook
- [ ] **VB-92 Rack names (10.18):** JEI search "wine rack": every wood has "Single <Wood> Wine Rack" (1 bottle), "Small <Wood> Wine Rack" (4x) and "Big <Wood> Wine Rack" (9x), cherry included; no "Wine Bottle Storage" or "Dark Cherry" left; WTHIT shows the same names
- [ ] **VB-90 Wine texture dump (10.17):** `/dumpwinetextures` -> chat says how many were written; `kubejs/exported/wine_textures/upstream/block/wine/` holds every wine's textures (Praetorian's too), `generated/block/wine_box/` 8 PNGs (Magnetic's the inverted one), `index.tsv` one line per wine
- [ ] **VB-86 Vinery block lines (10.15):** Grapevine Pot, Apple Press, Cask, Grapevine Stem, Grapevine Lattice, apple / cherry leaves and saplings, Stackable Log and Chorus Wine show their gray (Chorus: light purple) lines, no "TODO" anywhere; `logs/latest.log` has no tooltip errors
- [ ] **VB-85 Capacity lines (10.15):** an oak 9x rack says "Holds 9 wines (small only)", the 4x rack "Holds 4 wines (small or medium)", the single-bottle rack "Holds 1 wine (any size)", the Wine Box "Holds 1 wine (small or medium)" and the sneak line; the aging hint still follows
- [ ] **VB-84 Wine size lines (10.15, reworded 10.18):** hover Red Wine -> "Small bottle: 3 per placed block" and "Fits in Single, Small, and Big Wine Rack, and Wine Box"; Eiswein -> "Medium bottle: 2 per placed block", "Fits in Single and Small Wine Rack, and Wine Box"; Cristel -> "Large bottle: 1 per placed block", "Fits in Single Wine Rack"; each matches what VB-73 shows the racks accept. First run 2026-10-20: TWEAK (the old "Small Wine Bottle Storage, the Wine Rack..." wording was clunky)
- [ ] **VB-80 Warm slows (10.13):** a fresh wine racked by a lit furnace in daylight -> its tooltip's next-level estimate is 4/3 of an unracked one; `/wine info` on it over time -> the age only goes up
- [ ] **VB-79 Old stored wine (10.13):** a wine racked before the update in a cellar still shows "cellar x2.5" (its stored rate) until taken out and put back
- [ ] **VB-78 Rack tooltips (10.13):** every 9x / 4x / single-bottle rack and the wine box -> "Wine aging is sped up by dark and cold environments, slowed by warm ones"; JEI Wine Aging page shows the cold and warm lines
- [ ] **VB-77 Wine tooltip + command (10.13):** the stored wine's tooltip says "In storage: <label> x<rate>" as in VB-76; `/wine storage` says "Storage here: <label>, wine ages at <percent>%" (green above 100, gold below)
- [ ] **VB-76 Storage labels (10.13):** wine put in a rack in daylight -> WTHIT "sunny x1.0"; ringed by snow blocks -> "cold sunny x2.0"; next to a lit furnace -> "warm sunny x0.75"; underground with nothing -> "cellar x2.0"; underground with ice -> "cold cellar x4.0"; underground with a lit furnace -> "warm cellar x1.5"
- [ ] **VB-72 Cherry mid rack window (10.11):** a closed cherry single-bottle rack shows its bottle through two glass panes (same look as the other woods' windows); opening and closing still animates; the Cherry Cabinet's door stays solid
- [ ] **VB-71 Racks merged (10.11):** no `vinery:wine_rack_1/2/3/5` in JEI or `/give`; "Big / Small Cherry Wine Rack" are the 1.5.5 cherry racks; oak and cherry 4x / 9x racks take wines by size (10.12, see VB-73); juice bottles are refused
- [ ] **VB-70 Jungle grapes on the grapevine lattice (10.10):** jungle red / white grape seeds plant on `grapevine_lattice`, grow, show the WTHIT stem line and the Growth % line, and harvest; a chest packed by the loot rules holds an apple / cherry / grape bag, not a crate
- [ ] **VB-69 Cherry names (10.10):** JEI search "dark cherry" finds nothing; planks, log, barrel, cabinet, drawer, racks, boats and the Dramatic Doors short / tall door say "Cherry"; the creative tab side label says "Cherry Wood"
- [ ] **VB-68 Removed blocks gone (10.10):** no `vinery:*_crate`, `barrel`, `stove`, `wood_fired_oven`, `kitchen_sink`, `cherry_chair` / `_table` / `_big_table` / `_shelf` or wood `*_lattice` in JEI, the creative tab or `/give`; the 10 bags, `cherry_barrel`, `cherry_cabinet`, `cherry_drawer`, the 1.3.10 chair / table / big table / shelf and `grapevine_lattice` are there; no log errors naming them
- [ ] **VB-59 Praetorian in a rack (10.8, sizes 10.12):** Praetorian Wine goes into a 4x rack, the wine box and the single-bottle rack, and a 9x rack refuses it (medium since 10.12); lying in a 4x rack or the box it keeps its own model, shrunk inside its slot (no slim one, 10.14); standing in the single-bottle rack it looks like Stal Wine; two stack in one placed block
- [ ] **VB-10 Pot stomping:** 12 red grapes (a 13th refused; half-full look from the 6th), 4 jumps -> 4 red juice, pot empty after; JEI page and WTHIT say 12 grapes / 4 stomps

### Toasts (toasts.md)

- [ ] **TO-07 Dedicated server:** join the updated server → no error; the server log has no `ToastFilterMixin` line
  (client-only mixin).
- [ ] **TO-06 Other toasts still show:** press Ctrl+B twice (narrator toast), finish a long AE2 autocraft, complete an
  FTB quest task → each toast shows.
- [ ] **TO-05 Switch back on:** set `[toasts] tutorial = true`, repeat TO-02 → the movement toast shows; set it back to
  `false`.
- [ ] **TO-04 Fairy Lights:** set `progress = "none"` in `fairylights-client.toml`, pick up a light item → no
  "craft hanging lights" balloon.
- [ ] **TO-03 Exposure hints:** set `attachments_show_info_toast = true` in `exposure-client.toml`, open a camera's
  attachments screen → no hint toast. Develop film in the lightroom → no "develop film" toast.
- [ ] **TO-02 Vanilla tutorial:** set `tutorialStep:movement` in `options.txt`, start a world → no "Move" / "Look
  around" toast; walk, look, punch a tree → no toasts.
- [ ] **TO-01 Config section:** launch → `[toasts]` in `creativeengineering-common.toml` with `tutorial = false`,
  `advancement = false`; no mixin error in `latest.log`.

### Immersive Paintings (immersive_paintings.md)

- [ ] **PA-07 multiplayer:** on the dedicated server, a second player sees a pack painting another player placed.
- [ ] **PA-06 authors:** hover a vanilla painting → "By Kristoffer Zetterstrand"; Earth → "By Mojang"; a DarkPaintings
  one → its artist, not the mod name.
- [ ] **PA-05 retextured:** with a resource pack that retextures a vanilla painting enabled at generation time →
  Immersive shows the retextured image.
- [ ] **PA-04 placed:** pick a 2 x 2 pack painting → it is placed at 2 x 2 and looks like the vanilla variant
  (sharp pixels, no blur).
- [ ] **PA-03 size filter:** filter width 4, height 4 → the 4 x 4 paintings (vanilla Burning Skull, Pigscene, Pointer
  among them).
- [ ] **PA-02 search:** type `rebirth` in the search box → only Rebirth Paintings entries; type a painting's name →
  it appears.
- [ ] **PA-01 Datapacks page:** craft an Immersive Painting, place it on a wall → the screen opens; the Datapacks
  page lists the pack's 475 paintings next to Immersive's own 32 px ones (the default filter).

### Exposure viewfinder (exposure.md)

- [ ] **EX-25 viewfinder and screens (`-04`):** raise the viewfinder, then die and respawn, change dimension, and relog, each with the camera still in hand → after each, open the inventory and a chest and click items → every click moves items, no shutter sound, no shot taken. Also with the viewfinder up: open the inventory (E) and click items → items move, no shot taken.

### Feature ports [game] (feature_porting.md) (oldest)

- [ ] **FP-09 Tier 1 in game [game]:** craft a name tag (leather, paper, 2 string); a fan over water weathers a copper block to exposed; smelt a hopper -> iron ingot, a rail -> nugget, an anvil -> iron block; 8 smart cables around black dye -> 8 black smart cables, 8 black around a water bucket -> 8 fluix; mine copper ore -> 1 raw copper (silk touch: the ore)
- [ ] **FP-01 Death Knell lang [game]:** on the live server, die to a zombie and a fall a few times: the chat shows
  Death Knell's flavour messages ("was bonked by", "fell victim to gravity"), no raw `message.deathknell.*` keys.
  The jar is on both sides since 1.0.7, so raw keys would mean a client missed the update.

## Idea, Notes, Changes (Human scratchpad, don't write up or add to plans unless directed)

- merge potato salad

## Writeup Plans (ToDo-ToDo, agents mark brief note here when a new feature/change requires a writeup, defer to later to save context)

- placeholder
