# Scratchpad

## Idea, Notes, Changes (Human scratchpad, don't write up or add to plans unless directed)

- add SOL carrot just for food dictionary?
- readd cherry cream soda, determine removed recipe
  - many fruit sodas?
  - most glass bottles
  - some cans, dyes, cheap and worse

## Oldest Unrun Tests

The 47 oldest unchecked tests in the writeups (by last git change, all 2026-09-27; copies, the originals stay in their writeups and get ticked there).

### BlockRunner (blockrunner.md)

- [x] **BR-04 Slow tiers:** `M` on sand → **−0.015**; on wool → **−0.012**; on a honey block → **−0.055** (jumping on honey is still damped).
- [x] **BR-05 1.00 tiers:** `M` on glass and on grass → "no modifier". `T blockrunner:surface/glass` on glass passes.
- [ ] **BR-08 Mobs too:** walk a horse onto asphalt, then `/attribute @e[type=minecraft:horse,limit=1,sort=nearest] minecraft:generic.movement_speed modifier value get 23237052-61ad-11eb-ae93-0242ac130002` → half the horse's base speed (a positive number).
- [ ] **BR-09 Soul sand untouched:** `M` on soul sand → "no modifier". You are still slowed, and Soul Speed boots still cancel it.
- [ ] **BR-10 Thin blocks don't register:** put a carpet on concrete and stand on it. `M` → **+0.04** (the concrete's value).
- [ ] **BR-18 Survives reloads:** redo BR-17's end state (oak first, concrete second). Relog or reload the chunk → still **+0.04**.
- [ ] **BR-19 Newest wins (Copycats+):** a Copycats+ copycat slab doubled up: wool first, concrete second → **+0.04**. Swap the order on a fresh one → **−0.012**.
- [ ] **BR-21 copyCamo off:** set `[surface.camouflage] copyCamo = false` and save (live reload). Step off and back onto the concrete-camo frame from BR-15 → "no modifier". Set it back to `true`. (The tooltip half is [TT-05](tooltips.md).)
- [ ] **BR-25 No sink where it shouldn't:** carpet on wool, a thatch slab, a honey block (keeps its own real sink), mid-jump over sand → no extra sink.
- [ ] **BR-26 Off switch:** `depthPixels = 0` → nothing sinks. Set it back to `2` afterwards.
- [ ] **BR-30 Frames sink like their camo:** a framed cube camo'd with wool → you sink on it (camera and model). Re-camo with stone → no sink. An empty frame → no sink.
- [ ] **BR-31 Hybrids use the newest camo:** framed double slab, stone first, then wool → sinks. On a fresh one, wool first, then stone → no sink.
- [ ] **BR-32 Non-full frames don't sink:** a single framed slab camo'd with wool → no sink.
- [ ] **BR-33 Independent switch:** set `[surface.sinking] copyCamo = false` → the wool frame from BR-30 no longer sinks, but `M` on it still reads wool's **−0.012** (walk speed follows `[surface.camouflage]`). Set it back to `true`.
- [ ] **BR-39 Section is there:** open Create's Configs screen for Creative Engineering, Common → a **surface** folder holding speed, sticky, camouflage and sinking. `speed` lists all 26 tiers, each showing its description. No `block_sinking` or `camouflage_speed` left at the top level.
- [ ] **BR-41 Live edit:** in the Configs screen set `surface.speed.asphalt` to 2.0 and save. Step off and back onto asphalt → `M` **+0.1**, and the asphalt item's tooltip reads "Speed Multiplier: 2.0". Set it back to 1.5.
- [ ] **BR-42 Sticky switch:** set `[surface.sticky] faceUpOnly = false`. Step onto a sideways sticky piston → **−0.035**. Set it back to `true` → "no modifier" after stepping off and on.
- [ ] **BR-43 Hybrid wording:** framed slab, framed panel, a framed double slab, a Copycats+ copycat slab → "Speed Multiplier: matches its camo (newest camo sets walk behaviors for hybrid blocks)". Framed cube → "…matches its camo". (Needs `[surface.camouflage] copyCamo = true`; with false every frame reads "1.0, no matter its camo".)
- [ ] **BR-44 Line shows:** look at IP asphalt → "Run Speed: 1.50× (Asphalt)", number green. Sand → "0.85× (Dust)", red. Stone → "1.05× (Natural Smooth)". Glass and grass → no line (1.00, `hideNormal`).
- [ ] **BR-45 Frames and sticky:** a framed cube camo'd with concrete → "1.40× (Concrete, from camo)". A sticky piston facing up → "0.65× (Sticky)". Facing sideways → no line; with `hideNormal = false` → "1.00× (Sticky, sticky face not up)".
- [ ] **BR-46 Live and configurable:** set `[surface.speed] asphalt = 2.0` and save → the asphalt line reads 2.00× without relooking. `format = PERCENT` → "+100%". `showTier = false` → number only. `enabled = false` → no line. Put everything back.
- [ ] **BR-28 D&D asphalt matches IP asphalt:** lay a strip alternating IP asphalt and D&D asphalt, and walk (then sprint) along it. → `M` reads **+0.05** on both. The pace doesn't change between them; before the fix you'd shoot ahead on the D&D blocks and slide when stopping.
- [ ] **BR-29 D&D asphalt is walkable if untiered** (optional): temporarily remove `create_dd:*_asphalt_block` from the asphalt tier and `/reload` → normal walking speed, not frozen. Put it back afterwards.
- [ ] **BR-34 Modded tiers:** `M` on a Create Deco brick → **+0.025**; a Dustrial Decor cinder block → **+0.04**; an Engineer's Decor steel catwalk → **+0.03**; Railways locometal → **+0.025**; an Architect's Palette oak board → **+0.01**.
- [ ] **BR-35 Fence tops and beds:** stand on top of an oak fence → **+0.01** (wood). On a bed → **−0.012** (soft), and with `depthPixels = 8` you don't sink into it.
- [ ] **BR-36 Gourds don't sink:** `M` on a pumpkin → **−0.015** (confectionery). With `depthPixels = 8` no sink; a froglight next to it (soft, full block) does sink.
- [ ] **BR-37 Blocks with no tags:** a froglight → **−0.012**; D&D padded rubber → **−0.012**; tilled end stone → **−0.02**. These only work because the scan found them.
- [ ] **BR-38 SecurityCraft copies:** reinforced stone and a stone mine → **+0.005** (same as stone); reinforced concrete → **+0.04**; a reinforced sticky piston facing up → **−0.035**, sideways → "no modifier".
- [ ] **BR-27 Server parity:** with the new patcherMod jar on the server, repeat BR-11, BR-15 and BR-17 while connected. → Same values, and no rubber-banding when walking across camo'd frames.

### Loot (loot.md)

- [ ] **LT-03 PNC removed pre-roll:** `/loot give @s loot pneumaticcraft:chests/mechanic_house` 5x -> nothing at
  all (the table was 100% PNC).
- [ ] **LT-04 PNC GLM path:** `/loot give @s loot minecraft:chests/simple_dungeon` 20x -> no PneumaticCraft items;
  vanilla dungeon loot, Quark tomes and runes and Botania items still show up.
- [ ] **LT-08 Pattern with except on injected items:** add `"@quark": {"remove": true, "except": ["quark:ancient_tome"]}`,
  `/reload` -> 30 rolls of `minecraft:chests/simple_dungeon` show ancient tomes but never blank runes.
- [ ] **LT-09 Slack modes:** set `minecraft:bread` to `"frequency": 0.5`, `/reload`, `/dumploot` -> in
  `minecraft.json`, `chests/village/village_plains_house` has no new `minecraft:empty` entry and the other items'
  weights grew. Then `"slack": "empty"`, `/reload`, `/dumploot` -> `/loottuning` says slack=empty, the table gained a
  `minecraft:empty` entry, and the other items' shares are unchanged.
- [ ] **LT-12 Lootr timing:** with LT-07's rule active, open a never-opened village chest in fresh chunks -> no bread.
  A chest you opened before the `/reload` keeps its old contents.

### Tooltips (tooltips.md)

- [ ] **TT-01 IP asphalt:** hover Asphalt Concrete, its slab and its stairs. → No "Increases walking speed" line on any; each still shows "Speed Multiplier: 1.5".
- [ ] **TT-02 Crushed piles:** hover a Create Compressed crushed aluminium pile. → Name only.
- [ ] **TT-03 Speed values:** asphalt (IP and D&D) → "Speed Multiplier: 1.5"; glass → "Speed Multiplier: 1.0"; stone → "Speed Multiplier: 1.05". *(was BR-06)*
- [ ] **TT-04 Camo lines** (needs the rebuilt patcherMod jar): framed cube → "Speed Multiplier: matches its camo"; framed slab, framed panel, any framed double item, Copycats+ copycat slab or byte panel → "…matches its camo (newest camo sets walk behaviors for hybrid blocks)". Needs `[surface.camouflage] copyCamo = true`. *(was BR-20)*
- [ ] **TT-05 Camo off** (needs the rebuilt patcherMod jar): set `[surface.camouflage] copyCamo = false` and save. Hover a framed cube → "Speed Multiplier: 1.0, no matter its camo". Set it back to `true`. *(was the tooltip half of BR-21)*
- [ ] **TT-06 Patterns:** a Quark hollow log → "Can be crawled through"; any Create/Quark ladder → the ladder line.
- [ ] **TT-07 Colours:** Bottled Cloud's line is dark blue (rare); an IE copper wire's solder line is gold (eng).
- [ ] **TT-08 NBT line:** an unprogrammed Precision Sawblade → "Unprogrammed — blueprint with a shape to set a profile".
- [ ] **TT-10 Names:** search JEI for "copycat slab". → "Framed Copycat Slab" (FramedBlocks) and "Copycat Slab" (Copycats+) show as two distinct names. A placed Framed Copycat Chest's GUI title reads "Framed Copycat Chest", and the two gate doors are "Framed Copycat Gate" / "Framed Copycat Iron Gate".
- [ ] **TT-11 Schematic lines:** framed cube → gray "Create schematics won't save the block inside it"; Framed Hammer → no such line; Copycats+ copycat block and Create copycat step → gray "Create schematics save the block inside it".
- [ ] **TT-12 Hybrid lines:** framed slab and framed panel → "Place a second one in the same space to make a hybrid block, one camo per half"; Framed Copycat Double Corner → "Hybrid block: each part holds its own camo"; framed cube → neither line.
- [ ] **TT-13 Schematic claim holds:** save a Create schematic over a camo'd framed cube and a copycat block, then print it. → The framed cube comes out empty; the copycat keeps its material.
- [ ] **TT-09 No dead patterns:** search `logs/latest.log` for `[addTooltipPattern]`. → Every line reports more than 0 matches.

## Client tests to run in game (top of file since the 2026-10-22 doc sweep)

Client-only in-game tests for unreleased 1.1.01 (all work since 1.0.9 shipped, 2026-10-19), newest first. Tests the
scratch server fully covers are left out; where it covers part, the entry says so (the `SQ-` ids are in
[testing/scratch_queue.md](../testing/scratch_queue.md)). Before testing: install the `build/libs` patcherMod jar (extra
stem fruit is only there) and More Mooblooms 1.1.0, both with the game closed; startup scripts changed, so restart.

### 1.1.02 and later work (added 2026-10-23, newest first; client halves only)

Covers the 1.1.02 commit (Vintage Delight, Fungi Delight, misc delights) and the uncommitted work after it. Full lists:
[vintage_delight.md](../vintage_delight.md) section 8 (VTD), [fungi_delight.md](../fungi_delight.md) section 8 (FGD). Startup
scripts and the removed-items list changed: restart the client, and `/reload` is not enough.

#### Uncommitted after 1.1.02 (no writeup ids yet)

- [-] **N-01 Egg Fried Rice name:** Farmer's Delight's Fried Rice reads "Egg Fried Rice" in the inventory, JEI and WTHIT; the
  Display Delight placed copy reads "Placed Egg Fried Rice".
- [-] **N-02 Glycerol tooltip:** hover Glycerol with no Shift: the aqua line "Tastes sweet!" shows and the "Hold Shift" hint is gone.
  Other PneumaticCraft items still show "Hold Shift"; the Liquid Experience Bucket tooltip still reads "Experience in liquid form."
- [-] **N-03 Bedrolls gone:** search JEI and every creative tab for "bedroll": nothing. No Leatherworker offers one (check a few).
- [-] **N-04 Abnormals Delight leftovers gone:** JEI and creative: no Passion Aloe Nectar, Cherry Cream Soda, Dune Platter,
  Yucca Gateau Slice, Fruit Beignet; no Fruit Beignet recipe page.
- [ ] **N-05 Luck brewing in JEI:** Potion of Luck and Potion of Bad Luck (and their long forms) each list one pack route
  (clover or silverfish bottle on awkward, redstone to lengthen); no Buzzier Bees duplicate, no strong or corruption route
  (compare EF-05 in [effects.md](../effects.md)).
- [ ] **N-06 No-effect potions:** JEI shows no bucket or tipped-arrow recipe for awkward, thick, mundane or water; bottling
  recipes for awkward, thick and mundane remain. Right-click an awkward potion bucket on the ground: nothing spawns and the bucket is kept.
  An effect bucket (e.g. Speed) still spawns rings, becomes an empty bucket and goes on a 1 second cooldown.
- [ ] **N-07 Driftwood bark:** JEI: driftwood log and wood stripping on the cutting board give Immersive Weathering's driftwood bark
  (not Abnormals Delight's); Create cutting and the pack sawmill still list their driftwood recipes.
- [ ] **N-08 Quark Engineering stairs:** the sawmill recipes for the quark trio (blossom, ancient, azalea) stairs in JEI are the
  pack's only ones; the bookshelf recipes remain.
- [ ] **N-09 Food types:** Cheese Pasta and the sweet entries changed in `food_types.json`: hover them, the type line and AppleSkin
  values match their neighbours (spot check three).

#### Vintage Delight (vintage_delight.md section 8)

- [ ] **VTD-25 Placed tooltips:** placed jars, pizzas and the cheese mold show the placeable tooltip.
- [ ] **VTD-03 Seed tooltip:** the Ghost Pepper Seeds tooltip names Soul Soil, Rich Soul Soil and Fertile Soul Soil. Nulch reads "Fertile Soul Soil".
- [ ] **VTD-05 WTHIT growth line** on every Vintage crop; **VTD-14** mason jars: up to 4 on a block, look right placed.
- [ ] **VTD-13 Cheese mold** JEI explanation page; **VTD-45** every Vintage item in JEI has a recipe or a use, names are sensible.
- [ ] **VTD-33 Chef's hats:** 16 colours exist and render on the head; Simple Hats' chef's hat is gone from hat bags.
- [ ] **VTD-26 Supplementaries jar:** vinegar, nut milk and the jar fillings show their colour in a Supplementaries jar.
- [ ] **VTD-48/49 Burgers and burrito:** the sequenced-assembly JEI pages show the steps in order.

#### Fungi Delight (fungi_delight.md section 8)

- [ ] **FGD-03 Mushroom fields:** colonies, both huge mushrooms and the Inky Cap and Morel cows render (the cow cap layer included).
- [ ] **FGD-04 / FGD-35 Inky Goo Veil:** bone meal on an inky cap in low light grows a huge form and the veil hangs under it; the
  veil stops at 16 segments and looks right (no missing texture).
- [/] **FGD-16 Weak Stomach:** the effect icon and name show in the HUD and the inventory after eating a Cleaned Inky Cap.
  - remove 'from' language, add to effect provider
- [/] **FGD-18 Burrowing:** the effect name, icon and potion item tint render; the potion shows in JEI.
- [ ] **FGD-20 / FGD-31 JEI:** all 7 pot meals and 3 grid meals; the mushroom info page lists the four mushrooms.
- [ ] **FGD-26 Join check:** a client joining the dedicated server sees no missing-class or registry mismatch message.

### Recently added tests (not yet in a section above; collected from the writeups, 2026-10-22)

#### Backpack skins (backpack_skins.md)

- [ ] **BP-01 Smithing:** smith a Sophisticated Backpack with a Basket: a skinned backpack with contents and upgrades intact; a non-block or untagged item is refused.
- [ ] **BP-02 Clear:** craft the skinned backpack alone: the skin is gone, contents kept.
- [ ] **BP-03 Held and dropped:** held, in the inventory, dropped and in an item frame the backpack shows the basket.
- [ ] **BP-04 Placed:** placed it shows the basket, the hitbox is the basket's, it faces the backpack's facing; pick it up and the skin is kept; no upgrade, tank or display drawing.
- [ ] **BP-05 Equip switch:** switch off (default): chest slot, shift-click and Curios slot refuse the skinned backpack; switch on: both accept it and the basket is drawn on the back.

#### Backpack and terminal Polymorph choice (fixes_compat.md)

- [ ] **FX-35 Backpack Polymorph choice** (in game): backpack with a crafting upgrade, open its tab, fill the grid with
  a recipe that has two outputs (e.g. a conflicting plank or slab recipe; JEI shows both). The Polymorph button
  shows right of the result slot; pick the other output: the result slot changes, taking it gives that item and
  uses the grid as that recipe does. Shift-click crafts the picked one repeatedly.
- [ ] **FX-36 Button follows the tab** (in game): close the crafting tab: the button is gone and its spot takes no
  clicks. Reopen: button back next to the result slot. With an unambiguous recipe the button stays hidden.
- [ ] **FX-37 Reopen and second upgrade** (in game): leave a conflicting recipe in the grid, close and reopen the
  backpack: button and list come back without touching the grid. With two crafting upgrades holding different
  conflicting grids, switching tabs shows each grid's own list.
- [ ] **FX-38 No regressions** (in game): normal crafting in the upgrade (including a recipe with a remainder, e.g. a
  bucket recipe), the vanilla crafting table's Polymorph button, and JEI transfer into the upgrade grid all work.
- [ ] **FX-39 Engineer's Crafting Table choice** (in game): fill the IE Engineer's Crafting Table grid with a recipe
  that has two outputs (JEI shows both). The Polymorph button shows right of the result frame, clear of the title;
  pick the other output: the result changes, taking it gives that item. Shift-click crafts the picked one repeatedly.
  Close and reopen with the grid still filled: the button and list come back without touching the grid.
- [ ] **FX-40 Crafting Terminal choice** (in game): same in a Pretty Pipes Crafting Terminal (needs a pipe network).
  The button sits right of the result frame and covers no grid slot or the Request button. Reopen with the grid
  filled: the list comes back.
- [ ] **FX-41 Shared terminal** (in game, two players): both open one Crafting Terminal with a conflicting grid; each
  picks a different output: each sees and takes their own pick.
- [ ] **FX-42 No regressions** (in game): plain crafting in both (no button with an unambiguous recipe), the vanilla
  crafting table's Polymorph button, JEI transfer into both grids, Crafting Terminal ghost items and Request button.

#### JEI cycling pages, Tipped Arrows, Potion Buckets, Canning (jei_coverage.md)

- [ ] **JC-06 log:** after joining a world, latest.log has `[jei cycle] N crafting pages, N variants` and
  `[jei cycle] hid N AE2 facades`; note any recipe named in `[jei cycle] no candidate grid matched` (expected: none,
  or only `immersiveengineering:revolver_cycle`).
- [ ] **JC-07 elytra duplication:** uses of the dragon scale -> elytra + dragon scale page, result an elytra (the original is kept in the grid).
- [ ] **JC-08 repairs:** uses of a diamond sword -> vanilla repair page (two worn swords, one enchanted -> enchantments
  gone), Mimicream page (sword ringed by 8 Mimicream; the enchanted one loses Mending); recipes of an IE hammer -> IE repair page.
- [ ] **JC-09 dyeing:** recipes of leather boots -> one page cycling 16 colours then two- and three-dye mixes; same for a
  balloon arrow and a balloon. Uses of red dye -> each page shows only its red variant.
- [ ] **JC-10 bedspreads:** uses of a red bed -> bed + patterned banner page; recipes of the patterned bed -> remove-pattern page.
- [ ] **JC-11 Botania:** uses of Resolute Ivy, Phantom Ink, Spellbinding Cloth, Life Essence (shard level 0 to 19),
  a lens (dye, mana blaster load/unload, split composite), a brew vial (merge), a cosmetic bauble (attach/remove),
  Black Hole Talisman (extract): each has a page.
- [ ] **JC-12 glass wind bell:** uses of the bell -> dye above / bell / dye below, cycling base x trim.
- [ ] **JC-13 CC:** uses of a computer -> colour page (16 + mixes) and sponge page; uses of paper -> disk page; uses of
  a printed page -> printout page.
- [ ] **JC-14 wand, elytra, ender storage:** uses of the angel core -> wand pages; uses of an elytra -> dye, banner,
  hide cape, glow, split pages (wing combination from the wing); uses of an ender chest -> dye in each of the three top slots.
- [ ] **JC-15 fairy lights / routes / arrows / powder horn:** uses of a fairy light -> copy colour; uses of a tug route
  -> route copy with 1-3 blanks; uses of a paint arrow -> dye page; uses of gunpowder -> powder horn refill page.
- [ ] **JC-16 IE:** uses of earmuffs (helmets cycle), powerpack (chestplates), flare cartridge (dyes), potion cartridge
  (potions), speedloader (bullets), jerrycan (drill / chemthrower refill), a shader (back to its bag).
- [ ] **JC-17 Quark and drawers:** recipes of a chest -> mixed-wood page with each plank slot cycling; uses of a drawer
  upgrade -> drawers cycling.
- [ ] **JC-18 vanilla copies and fireworks:** uses of a written book (1-8 copies), a filled map (1-8 copies), a
  patterned banner (copy); recipes of a firework star (shape x effect x colour), a rocket (flight 1-3, with stars).
- [ ] **JC-19 AE2:** search `facade` -> nothing in the item list; uses of the cable anchor -> facade page cycling blocks.
- [ ] **JC-20 load time:** world join is not noticeably slower; latest.log `[jei cycle] built in N ms` (expect well under 2000).
- [ ] **JC-21 log:** after joining, latest.log has `[jei] Tipped Arrows: N page(s); Potion Buckets: N page(s); moved N
  recipe(s)` (expect about 119 and 357 pages), `[jei] Canning: N recipe(s); Using Cans: N recipe(s)`, and the two
  `[jei] hid N ...` lines; no `couldn't build` warning.
- [ ] **JC-22 Tipped Arrows:** recipes of a Tipped Arrow of Healing -> Tipped Arrows tab, one page: crafting table |
  8 arrows + lingering healing potion -> 8 arrows + glass bottle; spout | arrow + 25 mB healing -> arrow; Bottling
  Machine | arrow + 20 mB -> arrow. Hovering the fluid shows the potion's effect lines. The Crafting, Spout Filling
  and Bottling Machine tabs no longer list tipped arrows.
- [ ] **JC-23 Potion Buckets:** recipes of a Potion Bucket -> Potion Buckets tab; each page has five rows: bucket + 4
  potions -> potion bucket + 4 glass bottles; potion bucket + 4 glass bottles -> 4 potions + bucket; spout; item
  drain (potion bucket -> bucket + 1000 mB); Bottling Machine. Uses of `create:potion` fluid also lead here.
  Crafting / Spout Filling / Item Draining / Bottling Machine no longer list potion bucket recipes.
- [ ] **JC-24 tab icons:** Canning icon = empty can with a small crafting table on its lower right, drawn like
  Create's Automated Shaped Crafting icon; Using Cans = crushed can with the same table.
- [ ] **JC-25 Canning / Using Cans:** recipes of Canned Beef -> Canning tab, crafting grid, + button fills a crafting
  table. Uses of Canned Apple Pie Filling -> Using Cans tab (filling + pie crust -> pie), plus any burger or stew
  that takes a can. The Crafting tab no longer shows either set. Seasoning crafts still on the Seasoning tab.
- [ ] **JC-26 switches:** set `tippedArrowCategory`, `potionBucketCategory`, `canCategories` to false in
  `config/creativeengineering-common.toml` `[jei]`, rejoin: the four tabs are gone and the recipes are back on their
  old tabs (tipped arrows on Crafting again).
- [ ] **JC-27 load time:** world join not noticeably slower than before.

#### JEI world tabs (jei_world_tabs.md)

- [ ] **JW-01 Dump:** `/dumpworldjei` -> "2/2 parts"; `kubejs/exported/world_jei/tool_pairs.json` has no `_fixture` and
  `"errors": 0`; no door or block appeared at the world bottom under spawn. Then `/cegen run jei_world`: no WARNING
  lines; F3+T and rejoin.
- [ ] **JW-02 Log:** `latest.log` has `[jei_world] N rows in 19 tabs` and no `[jei_world]` warnings, and no
  `sample_syringe: JEI world rows not added`.
- [ ] **JW-03 Weathering:** uses of Brain Coral Block -> Weathering and Growth page: fan 77%, coral 21%, wet sponge 2%,
  Warm Ocean in the input tooltip. Uses of Oak Leaves -> leaf-pile row cycling leaves and piles together.
- [ ] **JW-04 Lightning:** uses of Lightning Rod -> Lightning Conversion: four copper rows, sand -> vitrified sand with
  40% fulgurite.
- [ ] **JW-05 Lava:** uses of Lava Bucket -> Lava Generation: andesite (water beside plus diorite), Create honey ->
  limestone, a DnD milkshake row.
- [ ] **JW-06 Concrete and Salt:** recipes for IE Concrete -> 2 Concrete Hardening rows. Uses of Salt Block -> 7 Salt rows,
  the melted-away output an empty slot whose tooltip reads "Nothing" (no barrier).
- [ ] **JW-07 Stripping:** uses of Oak Log -> Stripping: Stripped Oak Log plus Oak Bark.
- [ ] **JW-08 Waxing:** recipes for Exposed Cut Iron -> steel wool only, no axe; Weathered Cut Iron -> axe and steel
  wool. Uses of Honeycomb -> copper, Create copper and IW iron waxing.
- [ ] **JW-09 Dyeing and soap:** uses of Red Dye -> Red Wool / Glass / Terracotta pages, no sack or shulker box page, one
  Create sail page and one seat page. Uses of Soap -> terracotta from coloured terracotta (no glazed), sticky piston ->
  piston.
- [ ] **JW-10 Jars:** uses of Honey Bottle -> Jar Storage "1 of 12" with honey 250 mB, and Jar Fluids pouring a honey jar
  into a glass bottle. Filled jars draw their liquid in colour, not black: water blue, honey golden, lava orange. Uses of Cookie -> the cookie family row. Uses of Faucet -> beehive
  and water cauldron rows.
- [ ] **JW-11 Drops:** Knife and Machete Drops for a pig -> ham 50%, not burning, looting note, knife and machete. Uses of
  Grass -> 3 Grass Drops rows (hand: seeds 12.5%, hemp 10%; knife: straw 20%; shears: grass).
- [ ] **JW-12 Mobs:** Sample Syringe tab 7 rows, the cow row cycles cow and moobloom eggs. Mob Bottling: axolotl and
  Alex's Mobs buckets, stradpole with a lava bucket. Mob Transformations: the creeper output shows Charged.
- [ ] **JW-13 Burning and campfires:** Burning: glass -> dirty glass 100%, wood -> ash 25%, the ash row. Campfire Rules:
  the rain row says 8 s, the coal block row says it stays lit. The comfort row's output is the Comfort effect icon (no
  name tag), tooltip "Comfort" plus the seconds; the shovel row reads "Right-click with a shovel: puts it out."
- [ ] **JW-14 Budding:** AE2 flawless budding with Silk Touch shows the flawed block; the Geode Forge quartz row cycles
  basalt and blackstone.
- [ ] **JW-15 Moobloom Conversion:** uses of Dandelion -> moobloom 90% / sunbloom 10% shares in the hover.
- [ ] **JW-16 Grape Planting:** U on Red Grape Seeds opens Grape Planting; taiga page shows the soil line; jungle pages
  show the vine and lattice. Bush pages read "Plantable on any solid block, grows slowly at light level 10 or more",
  stem pages "grows quickly at light level 9 or more"; no percentages, no line cut off.
- [ ] **JW-18 Notes and arrow:** every world tab uses Feast Plating's arrow. Page through Stripping (oak log with bark),
  Soap Washing (wolf), Jar Fluids (faucet rows), Mob Transformations (zombie villager, MCA villager), Knife and Machete
  Drops (a pig), Grass Drops, Lightning, Lava Generation, Block Dyeing (Create sail): no note runs past its third line.
  If one does, hovering the notes shows the whole text.
- [ ] **JW-17 Dedicated server:** join CreEngServer: the tabs show the same rows as single player.

#### Hedge recipe (recipes_wood.md)

- [ ] **RW-20 Hedge recipe [scratch server PASSED 2026-10-22, SQ-MB-00, one recipe, oak log, x2 / game]:** JEI shows one Mulberry Hedge recipe: Mulberry Leaves over an
  Oak Log -> 2; `upgrade_aquatic:river_hedge` (the old recipe id) is gone.

### Drinks and cooking fluids (recipes_cooking.md) (newest, 2026-10-22 doc sweep)

- [ ] **RC-71 Chorus juice fluid** (recipe ids: SQ-DR-01): JEI shows the Chorus Juice fluid with an Absorption line. Heated
  basin: chorus fruit + sugar + 250 mB water -> 250 mB; spout fills a bottle, drain empties one, Bottling Machine fills one.
- [ ] **RC-72 Chorus juice pot:** Cooking Pot, 2 chorus fruit + sugar with bottles -> 2 Chorus Juice; not in a basin as a
  Slice & Dice recipe.
- [ ] **RC-73 Berry matcha latte:** JEI has no recipe that takes a glass bottle plus a latte. Kettle: matcha latte + sweet
  berries (no water) with a glass bottle -> berry matcha latte, the bottle goes to the leftover slot. Grid: latte + berries
  + ice -> berry latte and one glass bottle back.
- [ ] **RC-74 Milk from jars** (tank part: SQ-DR-03): glass bottle on a Cow in a Jar with 250 mB or more -> FD milk bottle,
  bottle fill sound; on an empty Milk Jar nothing happens. Milk bottle on a Milk Jar adds 250 mB (refused when full).
  Buckets still fill and empty both jars. Creative keeps the bottle.
- [ ] **RC-75 Broth soups and pastas:** grid: bone broth + 2 cooked kangaroo + carrot + beetroot -> kangaroo stew; bone broth
  + milk + 3 carrots -> carrot soup; 2 cooked lobster tails + pasta + tomato sauce -> lobster pasta; 2 cooked duck + pasta +
  carrot + baked potato + bowl -> duck noodles. The kangaroo stew items in an unheated basin do nothing.
- [ ] **RC-76 Ice cubes:** JEI shows no pickaxe ice cutting. Hammer on ice on a cutting board -> 4 ice cubes (hammer loses
  1); hammer + ice in the grid -> 4; IE crusher -> 3. Vanilla ice crafts a Neapolitan ice cream; a melon popsicle takes ice
  cubes; an ice cream crafts in the CfB crafting book with ice only from a fridge with the ice unit. 9 cubes still make 1 ice.
- [ ] **RC-77 Egg solution:** after a restart JEI shows the Egg Solution fluid. Unheated basin: egg + 250 mB milk -> 500 mB;
  3 flour + 250 mB -> 6 dough; 2 cornmeal + 250 mB -> 3 cornbread batter. Heated: sugar + flour + 250 mB -> 5 pancakes; bowl +
  750 mB -> scrambled eggs. Quiche assembly in JEI has a second recipe with one egg solution spout step; run it on a pie
  crust -> quiche.
- [ ] **RC-78 Frosting fluid:** heated basin sugar + 250 mB milk -> 500 mB frosting; compactor over a basin with 250 mB -> 1
  Frosting item; spout 750 mB on a Baked Cake Base -> Cake. JEI lists the Frosting fluid next to the item.

### Soups in jars: looks and WTHIT (mixed_jars.md)

- [ ] **MJ-11 Soup look:** a bowl of Corn Soup in an empty jar shows a soup-coloured liquid; WTHIT reads "Contents: Corn
  Soup (2 / 12)".
- [ ] **MJ-12 Soup colours:** each soup in section 9 shows a colour close to its texture in the jar (none black,
  purple-black or grey).
- [ ] **MJ-14 Bottle soup fill:** 12 Bayberry Soup bottles fill a jar.

### Spice of Life: placed food and new worlds (food.md)

- [ ] **FO-43 Placed pies and cakes count for Spice of Life:** in survival, hungry, bite a placed Apple Pie, a Chocolate Cake
  (Create Crafts & Additions'), a Strawberry Cake, a Golden Cake and a vanilla candle cake (one bite each); the food book
  lists Slice of Apple Pie, Chocolate Cake Slice (Abnormals Delight's), Strawberry Cake Slice and Slice of Cake. A knife on a
  placed pie drops a slice and adds nothing until the slice is eaten. A Create deployer biting a cake adds nothing. No
  `PlacedFoodSol` warning in `logs/latest.log`.
- [ ] **FO-32 Spice of Life defaults, single player** (content passed on the scratch server, SQ-CF-01): a new single-player
  world's `serverconfig/solapplepie-server.toml` has `minFoodsToActivate = 10`, `resetOnDeath = true`, `queueSize = 256`.

### Coffee merge and chocolate cake (item_merges.md 2.8)

- [ ] **MG-23 Coffee in JEI:** search coffee: no Farmer's Respite coffee items or kettle coffee recipes; Rustic's Coffee,
  Milk / Honey / Chocolate / Dark Coffee, beans, bag, wild coffee all there.
- [ ] **MG-24 Coffee effects:** hover and drink Rustic's Coffee, Milk Coffee, Honey Coffee, Chocolate Coffee, Dark Coffee:
  Caffeinated 1:30, 1:00, 0:45, 0:45, Caffeinated II 0:45; no Speed or Haste. Milk / chocolate coffee still clear one effect.
- [ ] **MG-25 Coffee swap:** `/give @s farmersrespite:coffee_berries 3`, `coffee_beans 3`, `strong_coffee 1`, `long_coffee 1`
  -> 3 Coffee Beans, 3 Roasted Coffee Beans, 1 Dark Coffee, 1 Coffee (Rustic's).
- [ ] **MG-26 Coffee recipes:** JEI Coffee Cake and Blazing Chili take Roasted Coffee Beans; Gambler's Tea (kettle) and Canned
  Coffee Berries take Coffee Beans; no beans -> red dye.
- [ ] **MG-27 Coffee fluids:** JEI uses of Rustic's Coffee and Dark Coffee in a Spout / Item Drain: no Central Kitchen coffee
  fluid recipe.
- [ ] **MG-28 Old coffee bush:** break a placed FR coffee bush at stage 2 (`/setblock`): Rustic's Coffee Beans drop, no FR
  berries.
- [ ] **MG-09 Chocolate cake in JEI:** CC&A cake with baked base + frosting + 2 chocolate bars by hand, 7 slices, oven, and
  the pack's spout (baked base + 500 mB chocolate); no other spout.

### Shields (shields.md)

- [ ] **SH-44 Resistance on block** (effects: scratch SQ-GM-01; tooltip here): Shift-hover echo / felsic / dragon shields ->
  "On block, you gain:" lists Resistance I / II / II for 2 s; dragon also Regeneration I.

### Dragon bow and crossbow (armor_effects.md)

- [ ] **AE-19 Dragon bow, held** (on-hit: scratch SQ-GM-02): hold it -> Invisibility on you, no Slowness or Glowing;
  tooltip lists Invisibility under "While held".
- [ ] **AE-20 Dragon crossbow, held** (on-hit: scratch SQ-GM-02): hold it -> Resistance II on you, no Exsanguination or
  Weakness.

### Camera bags and archery (camera_bags.md, archery.md)

- [ ] **CB-11 Flashes** (accept rule: scratch SQ-GM-03): click a redstone lamp and an IE light bulb into a bag's screen
  -> both go in.
- [ ] **AR-07 Explosive arrow JEI** (recipes: scratch SQ-GM-04): JEI shows the shaft slot as `#forge:arrow_shaft/blazing`,
  at the fletching table and on the archery blueprint.
- [ ] **AR-13 Dragon scale bullets workbench** (recipe: scratch SQ-GM-04): Engineer's Workbench with the bullet blueprint
  lists the dragon scale recipe; 64 casings + 32 gunpowder + a dragon scale -> 64 armor-piercing cartridges.

### More Mooblooms 1.1 JEI (mooblooms.md 5; install moreMooblooms-1.1.0 into mods/ first)

- [ ] **MB-100 JEI shearing:** JEI uses of shears -> "Moobloom Shearing", 11 pages; rosebloom shows rose bush 1.5, poppy 1.2, red tulip 0.3 per shear; witherbloom wither rose 0.3; the rendered moobloom cycles through every pool flower and turns with the mouse; the cow egg slot appears (and is gone with `shearBareToCow = false` after a restart)
- [ ] **MB-101 JEI breeding outputs:** rosebloom egg -> pages for rosebloom + rosebloom -> rosebloom 100%, rosebloom + lilybloom -> peonybloom 35% / random 65%, the random-calf page (no witherbloom); each parent shows its food cycling; moobloom + moobloom -> sunbloom 10% / moobloom 90%
- [ ] **MB-102 Info text:** each egg's info page reads in a few short lines, numbers match the config
- [ ] **MB-103 No BB moobloom in JEI:** JEI search "moobloom" and BB's egg via recipe lookup -> no Just Enough Breeding page for Buzzier Bees' moobloom

### Mob tweaks (mob_block_tweaks.md)

- [ ] **MT-26 Seagulls skip lunchboxes** (list load: scratch SQ-GM-07): beach with gulls, the three lunchboxes in the
  hotbar -> never taken; add a carrot -> the carrot is stolen.
- [ ] **MT-40 Comb eye colour** (client only): hold an MCA Comb, right-click the air. The hairstyle page has `<` `Brown Eyes` `>`
  top right and `Eye Shape 1` top left. Step the colour through all 11 and toggle the shape → the hair previews show
  the new eyes each time. Press Done → your villager-model look keeps the eyes (switch to the villager model in the
  full editor if needed), the comb lost 1 use. Repeat by using the comb on an MCA villager: its eyes change in the
  world, also after a relog. Change the eyes, press Escape → nothing saved, no use lost. Pick a hairstyle after
  changing the eyes → both saved, 2 uses lost. A heterochromia villager shows its fixed two-colour art.

### Sacks as bundles (storage_audit.md 6; logic: scratch SQ-GM-10)

- [ ] **SB-01 Scroll:** sack with 3 kinds in the inventory. Scroll down over it: the Quark tooltip's first cell
  becomes the 2nd kind; scroll up returns it. No item moves to another inventory (Mouse Tweaks).
- [ ] **SB-02 Single stack:** sack with one stack: scrolling over it does nothing to it (Mouse Tweaks may act as usual).
- [ ] **SB-03 Take stack:** empty cursor, right-click the sack: the selected stack is on the cursor, the rest stay.
  Close and reopen the inventory: same contents (server agrees).
- [ ] **SB-04 Empty sack:** right-click an empty sack: it is picked up. Empty one with SB-03 until empty: it has no
  contents tooltip and no tag (F3+H advanced tooltip shows no NBT line).
- [ ] **SB-05 Place 1:** carry a sack, right-click an empty slot: 1 of the selected item lands there.
- [ ] **SB-06 Insert:** right-click a stack onto a sack: it goes in; a 17th kind is refused (16 slots).
- [ ] **SB-07 Chest:** SB-01 and SB-03 with the sack in a chest slot, and in a backpack.
- [ ] **SB-08 Creative:** in creative mode the pack opens the survival inventory (`CreativeMenuDisableMixin`, fixes_compat
  2.19), so the creative-tab branch of `SackBundleScroll` is unused: SB-01 and SB-03 in creative work as in survival
  (client).
- [ ] **SB-09 World:** place the sack, right-click it: its screen opens with the rotated order. Dedicated server:
  repeat SB-01 and SB-03 (packet and mixin on both sides).

### Config, chalk, Ore Excavation undo (fixes_compat.md 2.26, 2.27)

- [ ] **FX-25 JEI tooltip search needs `$`** (client only): in JEI type `edible`: no items match by tooltip text alone; `$edible`
  matches. Name search still works without a prefix. `jei-client.ini TooltipSearchMode = REQUIRE_PREFIX`
  (2026-10-22).
- [ ] **FX-26 Breezy wind particles** (client only): above y 83 in the open (sea level + 20), wind particles show about 3 times as
  often as before (`breezy-client.toml` frequencies x3: 0.015 / 0.03 / 0.045, 2026-10-22).
- [ ] **FX-27 Chalk symbols unlocked** (the server's unlock list: scratch SQ-GM-06; the symbol screen and drawing:
  client): a new player with no advancements opens the chalk symbol menu (sneak-use
  chalk): Skull, Cross, Heart, Check, Pickaxe and House are all selectable and draw. `chalk-common.toml` every
  `*UnlockAdvancement = ""` (2026-10-22).
- [ ] **FX-28 Cosmetic armor kept on death** (real player death, in game): wear a cosmetic helmet, die: it is still in the cosmetic slot after
  respawn, not in the corpse. Then set `CosArmorKeepThroughDeath = false`, restart, die: it is in the corpse and goes
  back to the cosmetic slot when the corpse is opened (no config change shipped; checks the existing setup).
- [ ] **FX-29 Drops into the inventory** (vein mining needs the client keybind: in game; Auto Pickup on is checked by
  SQ-GM-08): vein-mine 20 stone or dirt with room in the inventory: every drop is in the
  inventory, none on the ground. With a full inventory: the drops lie at your feet.
- [ ] **FX-30 Undo works** (in game): right after FX-29 (inventory case), `/undo_excavation`: the blocks come back and the
  drops leave the inventory; message "Excavation undone" (OE's text).
- [ ] **FX-31 Cooldown** (rule: SQ-GM-08; the real command: in game): vein-mine twice, undo, undo again at once: refused, "You can undo another vein mine in 5 s"
  (or 4). After 5 s it works. A refused undo (FX-32) starts no cooldown.
- [ ] **FX-32 Denied blocks** (rule and message per block: SQ-GM-08; the real command: in game): vein-mine iron ore (or coal ore, deepslate ores), undo: refused, "This vein mine can't
  be undone: it broke Iron Ore." Same with a few Blocks of Iron, budding amethyst (silk-touch-free mining, it
  drops nothing), and two chests side by side.
- [ ] **FX-33 XP once** (in game; SQ-GM-08 only checks the offline-player guard on the same restore): note your XP, vein-mine sculk without Silk Touch (not denied, drops XP; ores are denied),
  note the gain, undo: your XP drops by that gain once, not more (a large vein mine restores over several ticks).

### Extra stem fruit and crop drops (crops.md; install the build/libs patcherMod jar first)

- [ ] **CR-72 Re-point (look)** (logic: scratch SQ-SF-04): on a stem with 3 or 4 melons break the melon the stem bends
  toward -> the stem model bends to another melon.
- [ ] **CR-73 Wait holds at defaults** (1-minute wait: scratch SQ-SF-02): defaults 10 / 20 / 40, `randomTickSpeed` 100 -> no
  2nd melon for about 10 minutes, then one.
- [ ] **CR-76 Seasons**: `/season set winter`, a melon stem with one melon, `randomTickSpeed` 100 -> note whether extra
  melons still grow (expected today: yes, bug `farming/crops #12`).
- [ ] **CR-77 Saved wait across restart**: stem with one melon, defaults; restart the world after 5 minutes -> the 2nd melon
  comes about 5 minutes later.
- [ ] **CR-70 Pepper config**: `[crop_drops.bell_peppers]` `redWeight = 6`, `redBonusChance = 0.0`, `/reload` -> log
  `weights 6:1:1`; mostly red again; restore.

### Wild crops batch (crops.md)

- [ ] **CR-83 Tea spread** (biome lists: scratch SQ-WC-01): new chunks of `terralith:lush_valley`, `sakura_grove`,
  `orchid_swamp`, `tropical_jungle` -> wild tea bushes; `plains` -> none.
- [ ] **CR-86 Bone meal in savanna** (flower lists: scratch SQ-WC-04): bone-meal a large savanna grass area -> flowers only;
  new savanna / jungle chunks still have wild cotton, pepper and coffee patches.
- [ ] **CR-87 Grapes in Terralith** (biome lists: scratch SQ-WC-02): `lavender_forest` red and white bushes, `shrubland`
  savanna bushes, `shield` taiga bushes, `tropical_jungle` / `mangrove_swamp` vines on trunks; none in `snowy_taiga`,
  `dark_forest`.
- [ ] **CR-88 White strawberries tooltip** (ripening: scratch SQ-WC-05): the White Strawberries tooltip names Y 192.

### River wood removed, Mulberry tree (recipes_wood.md 2.6)

- [ ] **RW-15 River set gone** (recipes: scratch SQ-MB-00): JEI search `river` -> only the Mulberry leaves, sapling, leaf
  pile, carpet, hedge and IW leaf pile.
- [ ] **RW-18 Inventory swap** (block drops: scratch SQ-MB-03): `/give @s upgrade_aquatic:river_planks 4` -> 4 oak planks
  in the inventory; Every Compat `everycomp:hc/upgrade_aquatic/river_chair` -> Handcrafted oak chair.
- [ ] **RW-19 Mulberry names**: leaves, sapling, potted sapling, leaf pile, carpet, hedge and IW leaf pile all named
  Mulberry in hand, JEI and WTHIT.

### Ocean geodes (structure_tuning.md)

- [ ] **ST-133 Ocean floor look** (placement: scratch SQ-GE): fly over a new deep ocean in spectator -> almost no amethyst /
  calcite shells on the sea floor; prismarine geodes still on it.

### Sweet Berry Pie and chocolate cake spout

- [ ] **OV-33 Chocolate cake spout** (recipe loaded: scratch server PASSED 2026-10-22; the Spout run: in game): a Spout with 500 mB chocolate over a Baked Cake Base on a depot gives a
  Chocolate Cake; JEI shows the filling recipe on the chocolate cake.
- [ ] **OV-32 Sweet Berry Pie** (recipes and can values: scratch server PASSED 2026-10-22; names and the oven screen: in game): the cheesecake shows as Sweet Berry Pie (whole, Slice of Sweet Berry Pie, Incomplete
  Sweet Berry Pie, both Display Delight plates; JEI search "cheesecake" by name finds nothing). Cooking Pot: 2 sweet
  berries + 2 sugar + empty can -> Canned Sweet Berry Pie Filling; 4 slices + empty can also craft one. Pie crust +
  full filling bakes a Sweet Berry Pie (pie tin) and crafts one by hand; an opened can does neither. Eating the can:
  4 servings, 3 hunger each, Speed 30 s.
- [ ] **CP-26 sweet berry pie** (recipes and can values: scratch server PASSED 2026-10-22; names: in game): 4 Slice of Sweet Berry Pie + can -> Canned Sweet Berry Pie Filling (4 servings, 3 hunger,
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
- [ ] **CP-21 Canned Goods values:** AppleSkin on Canned Cake shows 4 hunger and about 0.7 saturation (was 28 / 112;
  saturation as shown = hunger x modifier x 2); Canned Apples 5 hunger, 3.0 saturation (3 servings); Canned Suspicious Stew 8 hunger (8 servings, Canned Goods' own
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
### Log warnings (log_warnings.md)

- [ ] **LW-01 Play audit [game]:** after a long play session and a dedicated-server run, repeat the warning scan on `logs/latest.log` and add a "during play" table to [log_warnings.md](../log_warnings.md) (section 7); also place the section 3 blocks (redeco fences/chairs, interiors chairs, belt grinder, Display Delight plates at 2 to 6 servings, wine boxes) and acknowledge or fix each.
