# Dyeing

Block and item dyeing ideas, and the in-world dye and soap audit. Open work: `blocks/dyeing` in [todo/TODO.md](todo/TODO.md).

## 1. Owner ideas (scratchpad 2026-10-26)

- Glow dye item (`blocks/dyeing #4`): an efficient glow-to-dye ratio, and glow plus dye in one step instead of two.
- Related rows: block dyeing and soaping audit (#1, section 2), coloured water dyeing (#2), placeable recoloured string (#3).

## 2. In-world dye and soap audit (2026-10-27, `blocks/dyeing #1`)

Sources: the `/dumpworldjei` dump `kubejs/exported/world_jei/tool_pairs.json` (2026-10-26: 160 colour sets, 466
dye-blocked blocks, 2751 soap pairs), `config/supplementaries-common.toml`, the JEI world tab manual rows
(`kubejs/assets/creativecommon/jei_world/{dyeing,soaping}/manual.json`), and the bytecode of every mod jar whose
block or interaction code reads a dye (`DyeItem`, `DyeColor.getColor(ItemStack)`, `#forge:dyes`) or the soap item.
Block families were found from `kubejs/exported/tags/block_tags_by_block.json` (blocks in no tag are not scanned).
Nothing was changed in game; "verify" marks a result read from code that still needs an in-game check (2.9).

### 2.1 Mechanisms in the pack

| ID | Mechanism | What it does | Where |
|---|---|---|---|
| D1 | Supplementaries `[tweaks.dye_blocks]` | Dye on any member of a Moonlight colour set swaps it for that colour (`BlocksColorAPI.changeColor`, block state copied, block entity not). Skipped for every block with a block entity and `#creativeengineering:no_world_dye` (empty) | `SupplementariesDyeBlocksMixin` |
| D2 | Create's own blocks | Sails (and the sail frame), seats, valve handles, toolboxes: swap block. Belts, nixie tubes, flap displays: colour in the block entity (flap displays also take glow ink) | `SailBlock`, `SeatBlock`, `ValveHandleBlock`, `ToolboxBlock`, `BeltBlock`, `NixieTubeBlock`, `FlapDisplayBlock` |
| D3 | Cooking for Blockheads | Cooking table, counter, corner, hanging corner, fridge, sink recolour (block entity colour) | `BlockDyeableKitchen` |
| D4 | Sign text | Vanilla signs, Farmer's Delight canvas signs (D1 skips them), FramedBlocks framed signs, plus glow ink | vanilla, `AbstractFramedSignBlock` |
| D5 | Other blocks with their own dye | FramedBlocks framed target; Steam Rails smokestacks and buffers / headstocks; PneumaticCraft pneumatic door and aphorism tile; CC: Tweaked turtles; Creative Labels notes and labels | per mod |
| D6 | Entities | Sheep, wolf and cat collars (vanilla); Labels entity, Botania sparks, Breezy hot air balloon | per mod |
| S1 | Supplementaries soap, colour | Colour-set member back to the set's base, or to the white member when the set has no base. Block-entity data copied. `clean_blacklist` = glazed terracotta; beds skipped | `SoapWashableHelper` |
| S2 | Supplementaries soap, metal | Wax off and every oxidation stage back to the first (114 pairs); name parse for exposed / weathered iron and copper families (221 pairs: Immersive Weathering iron, Everything Copper, Dramatic Doors, SecurityCraft copper ...); sticky piston to piston | same |
| S3 | Supplementaries soap, own blocks | Sepia globe to globe; tipped bamboo spikes to plain; blackboard (glow, then wax, then drawing); skull candles; text wipe on signs, sign posts, hanging signs, doormat; Via Romana signs | `IWashable`, text holders |
| S4 | Supplementaries soap, entities | Dyed sheep to white; own wolf's collar to red (other pets only get the bath effect) | `SoapItem.interactWithPet` |
| W1 | Water, not soap | CC: Tweaked turtle: water bucket clears the dye. Create belt: a held water item (bucket, fluid container holding water) appears to clear it (verify) | `TileTurtle`, `BeltBlock` |
| W2 | Create fan washing (bulk) | Wool, stained glass and panes, concrete powder to the undyed block. It adds moss (Create Deco bricks) and rust (pack recipes to Dustrial Decor rusty sheet metal), never removes them | `create:splashing` |
| W3 | Non-soap cleaning tools | Immersive Weathering: shears remove moss (`moss_shearing`), axe scrapes exposed iron; Sooty Chimneys: dirty chimney back to clean (scrape) | per mod |

The vanilla cauldron (washing leather, banners, shulker box items) and crafting-grid dye / soap recipes are items, not
in world, and out of scope.

### 2.2 Dye and soap both work (block, in world)

D1 plus S1 work on every colour set below; a set marked "white" washes to its white member because it has no base.

| Group | Sets |
|---|---|
| Vanilla | wool, carpet, concrete, concrete powder (white); terracotta, stained glass, stained glass pane, candle, candle cake (to undyed) |
| Building | Clayworks terracotta shapes and bricks (9); Quark shingles (4), framed glass and pane; Twigs packed silt, silt shingles (5); IE coloured sheet metal and slab (white); PneumaticCraft plastic bricks and smooth plastic bricks (white); Create D&D padded rubber (5) and velvet, asphalt (white); Design Decor metal plate / sheet shapes (8), wallpapers (3, white); Create Framed stained framed and tiled glass, panes, trapdoor; Create Deco decals (white); CfB kitchen floor (white) |
| Rail and steam | Steam Rails locometal (13 sets, incl. boilers and wrapped variants); Dave's Building steel shapes (17), boilers, buffers, funnels, cow catchers, quilted wool (white) |
| Furniture | Another Furniture curtain, lamp, sofa, stool, tall stool; Redeco fluffy carpet, lamp, ottoman, paper lantern, pet bed, sofa; Interiors chair and floor chair; Handcrafted cushion; Quark stool; Create seats (D2) and sails (D2) (white) |
| Light and misc | Supplementaries candle holders, Supp Squared gold candle holders; Beautify candelabra; Vintage Delight salt lamps; PneumaticCraft wall lamps (2); Compressed Creativity plastic brackets; Chalk marks (white); Coloured Water fluid block (white) and water cauldron; Farmer's Respite and Neapolitan candle cakes (7) |
| Botania | mystical, shiny, double flowers, buried petals, mushrooms, petal blocks (white) |

Other pairs that work both ways: sign text (D4 dye, S3 wipe), Creative Labels notes (own dye, eraser), sheep (vanilla
dye, S4), wolf collar (vanilla dye, S4).

### 2.3 Soap works, in-world dye does not

These colour sets have a block entity, so D1 skips them (the 2026-10-20 fix for sacks losing their contents). S1 still
washes them and copies the block entity.

| Blocks | Dye in world | Soap result |
|---|---|---|
| Banners, wall banners, Supplementaries ceiling banners, flags | none | white (patterns kept) |
| Shulker boxes, Supp Squared sacks, presents, trapped presents | none | undyed |
| Waystones sharestones, Well wells, Twigs silt pots | none | undyed |
| Tinted Campfires | none | vanilla campfire |
| ProjectRed illumar lamps and inverted lamps | none | white |
| Domestication Innovation pet beds | none | white |
| Create Framed stained framed glass doors | none | white |
| Botania floating flowers | none | white |
| Farmer's Delight canvas signs | dye goes to the text (D4) | plain canvas sign |
| Create toolboxes, valve handles | Create's own (D2) | white, see 2.5 |

Bringing in-world dye back for these needs a dye path that carries the block entity over (as S1 does); the owner
asked about shulker boxes, banners and presents in the TODO row.

### 2.4 Dye works, soap does not

| Blocks | Dye | Why no soap | Could soap |
|---|---|---|---|
| Glazed terracotta | D1 | `clean_blacklist`; there is no undyed glazed terracotta | no (no target), keep |
| CfB cooking table, counter, corners, fridge, sink | D3 | colour is a block entity value, not a colour set | yes: back to CfB's default colour |
| Create belts, nixie tubes, flap displays | D2 | own colour field; belts may take water (W1) | yes: clear the colour (nixie back to orange) |
| Steam Rails smokestacks, buffers, headstocks | D5 | own colour field | yes: clear the colour |
| PneumaticCraft pneumatic door, aphorism tile | D5 | own colour field | yes: default colour |
| FramedBlocks framed target | D5 | own colour field | yes: default overlay |
| CC: Tweaked turtles | D5 | washes with a water bucket instead (W1) | optional: soap as a second wash |
| Cat collars | vanilla | S4 handles only own wolves | yes: collar back to red |
| Sign text colour | D4 | soap wipes the whole text, there is no "back to black" | optional |

### 2.5 Wrong or risky results (verify)

1. **Tag-joined members change variant when dyed.** Moonlight colour sets include blocks added through tags. D1 calls
   `changeColor` with no variant check, so dye on these probably turns them into the vanilla coloured block: Fantasy
   Furniture beds (14, no block entity, so D1 does not skip them; dyeing one half of a two-block bed), Fantasy
   Furniture venthyr and bone candles, Buzzier Bees soul candle, Supplementaries soul candle holder, and the Farmer's
   Respite coffee candle cakes in the vanilla candle cake set. Soap leaves them alone (no pair). Likely fix: put
   them in `#creativeengineering:no_world_dye` (DY-01, DY-02).
2. **Valve handles wash to the white handle**, not the undyed copper valve handle (S1 has no base for the set).
3. **Toolboxes wash to the white toolbox**; Create's crafted toolbox is brown.
4. **Rusted iron does not wash, oxidised copper does.** S2 takes exposed and weathered Immersive Weathering iron
   back to clean, but not rusted (11 blocks). Waxed rusted only loses the wax.
5. **Chalk marks wash to a white mark** instead of coming off.
6. Canvas signs are both a colour set (S1) and a text holder (S3); which one soap runs first is not read (DY-04).
7. Create sails: soap gives the white sail; shears already cut any sail back to the sail frame (Create), so no fix needed.

### 2.6 Neither works, both could

Coloured families that are not Moonlight colour sets (the colour sits in the middle of the id, or the set is short).

| Family | Colours | Notes | Candidate |
|---|---|---|---|
| SecurityCraft reinforced wool, carpet, concrete, terracotta, glazed terracotta, stained glass, pane | 16 each | infix naming; reinforced blocks have a block entity (owner) | yes, needs owner carry-over |
| Clayworks chiseled terracotta bricks | 16 | infix naming; undyed `chiseled_terracotta_bricks` exists | yes |
| Create Framed horizontal and vertical stained framed glass and panes | 16 x 4 | infix | yes |
| Glow Ink Plus glow wool and glow carpet | 16 x 2 | infix; soap could also take off the glow (to plain wool) | yes |
| Coloured Water condensed / luminous cauldrons | 16 x 3 | infix | yes |
| Create nixie tubes | 15 + orange | Create's own dye (D2), no soap | soap only (2.4) |
| Create D&D blueprint blocks | 14 | short set | yes if the base exists |
| Create Deco placards | 15 | holds an item, so likely a block entity (not read) | yes, needs carry-over |
| Dave's Building gilded boilers | 14 | short set | yes |
| Dustrial Decor neon lights | 8 | short set | dye within the 8 only |
| Lightman's Currency vending machines, large vending machines, freezers, card displays (13 woods) | 15 each | block entity with stock | no (stock loss risk), or with carry-over |

Not dye families: Quark corundum and crystal lamps (own colours), blossom trees, tulips, Rustic Delight bell peppers.

### 2.7 Soap without dye: unclean states

No block in the pack is bloody. Cracked blocks are damage, charred logs are burnt, and snowy / frosty blocks melt on
their own: none of those are soap cases. The rest:

| State | Blocks | Clean twin | Existing clean | Soap candidate |
|---|---|---|---|---|
| Mossy | vanilla 9 (incl. infested mossy stone bricks), Immersive Weathering 11, Quark 7, Twigs 8, SecurityCraft 8, Botania 6 (livingrock, livingwood, dreamwood), Create Deco 140 (8 brick colours x shapes), Architect's Palette heavy, Philips Ruins, Redeco stepping stones, mossy waystone | all of these | IW shears on its own list (coverage of the mod blocks not checked); none for Create Deco, Botania, SecurityCraft | yes, the main gap; Create Deco is mossed by fan washing with no way back |
| Overgrown | Architect's Palette overgrown algal bricks (5 shapes) | algal bricks | none | yes |
| Rusty | Dustrial Decor 16 (pack fan recipes rust them), Quark rusty iron plate 4, Dramatic Doors rusty doors 4, IW rusted 11 | all | none (IW axe scrapes exposed only) | yes |
| Dirty | Quark dirty glass and pane | glass, pane | none | yes |
| Sooty | Sooty Chimneys dirty chimneys 7 | clean chimney | scraping | optional, as a second way |
| Sandy | Immersive Weathering sandy stone, cobblestone, stone bricks, chiseled (16 shapes) | stone family | IW's own removal not checked | yes |
| Ashen | Supplementaries ashen basalt | basalt | none | yes |
| Muddy | muddy mangrove roots | mangrove roots | none | maybe (gives up the mud) |
| Floral moss | Floral Enchantment flower mossy cobblestone / stone brick (34) | mossy block (flower off) | none | no (decoration, not dirt) |

Not soap cases: Botania mossy apothecary (its own block), sandy waystone (a style, no dirt).

### 2.8 Proposals (for the owner)

| # | Proposal | Work | Size |
|---|---|---|---|
| P1 | Add the 2.5.1 tag-joined members to `#creativeengineering:no_world_dye` | KubeJS tag via `global.tags` | small |
| P2 | Soap on unclean blocks (2.7): mossy, overgrown, rusty, dirty, sandy, ashen, rusted IW iron; a pair list generated from the dump and applied by the same soap hook | patcherMod (soap mixin on `SoapWashableHelper`), generator | medium |
| P3 | Fix soap targets: valve handle to copper, toolbox to brown, chalk mark removed | patcherMod (same hook) | small |
| P4 | Soap clears block-entity colours: CfB units, Create belts / nixie / flap displays, Steam Rails, PneumaticCraft, framed target, cat collars | patcherMod, one adapter per mod | medium |
| P5 | Infix colour families (2.6) as colour sets (Moonlight lets addons register sets) | patcherMod | medium |
| P6 | In-world dye for block-entity sets (2.3) with block-entity carry-over, at least banners, shulker boxes, presents, flags | patcherMod (replace the skip with a copying swap) | medium, needs a dupe check |

### 2.9 In-game checks

- DY-01: dye a placed Fantasy Furniture bed (single and double) with red dye. Expected today (2.5.1): it may turn into
  half a vanilla red bed. Record what happens.
- DY-02: dye a Buzzier Bees soul candle, a Supplementaries soul candle holder and a Farmer's Respite coffee candle cake.
  Record whether the variant is kept.
- DY-03: dye a Create belt, then right-click it with a water bucket (W1). Record whether the colour clears and the
  bucket stays full.
- DY-04: write on a dyed canvas sign, use soap. Record whether the text is wiped, the sign turns plain, or both.
- DY-05: soap a brown Create toolbox holding items, and a red valve handle. Expected: white toolbox, contents kept;
  white valve handle.
- DY-06: shear a Create Deco mossy brick, a Quark mossy cobblestone brick and a SecurityCraft reinforced mossy
  cobblestone. Record which lose their moss (IW shears coverage, 2.7).

## 3. Owner decisions (2026-10-27, `blocks/dyeing #1`)

Owner notes after the audit in section 2. They replace proposals P1 to P6 (2.8) with a build list. Section numbers
below point back to the audit tables. Nothing is built yet.

### 3.1 Disable

| What | Rule |
|---|---|
| Coloured Water interaction | Disable the in-world dye interaction of coloured water (the Coloured Water fluid block and cauldrons, 2.2 "Light and misc"). Coloured water dyeing by fan (`blocks/dyeing #2`) is a separate recipe and stays planned |
| Turtle water bucket washing | Disable W1 for CC: Tweaked turtles (water bucket no longer clears the dye) |
| Banners with more than one colour | In-world dye and in-world soap do nothing on a banner (or wall banner, ceiling banner, flag) that has any pattern layer. Only a plain single-colour banner can be dyed or soaped in world. The crafting-table recipes (banner + dye, banner + soap) are not touched and still work on any banner |
| Botania flowers | Not dyed and not soaped in world: mystical, shiny, double flowers, buried petals, mushrooms, petal blocks, floating flowers (2.2 Botania row, 2.3). Add them to `#creativeengineering:no_world_dye` and to a soap block list (a `no_world_soap` tag, or the existing `clean_blacklist`) |

### 3.2 Port the "soap works" list to dye (P6)

Section 2.3 blocks washed by soap but not dyed in world get in-world dye, with block-entity carry-over (as S1 already
does for soap): shulker boxes, Supp Squared sacks, presents and trapped presents, sharestones, wells, silt pots, Tinted
Campfires, ProjectRed lamps, pet beds, Framed glass doors, single-colour banners and flags, Create toolboxes and valve
handles. Botania floating flowers are excluded (3.1). Multi-colour banners are excluded (3.1). Replace the D1 block-entity
skip in `SupplementariesDyeBlocksMixin` with a copying swap; the canvas sign stays text-dye only (D4).

Needs a dupe check before it ships (a container that drops its contents when swapped): the sack bug of 2026-10-20
(`blocks/dyeing #2` in TODO history) must stay fixed. New check DY-07 (3.8).

### 3.3 Fix soap targets (P3)

| Block | Soap result |
|---|---|
| Create toolbox (any colour) | Brown toolbox (Create's crafted colour), contents kept |
| Create valve handle (any colour) | Copper valve handle |
| Any dyed glass: Create Framed glass and panes, stained glass, Quark framed glass | The clear (undyed) glass, never white |

The glass rule applies to every glass colour set (vanilla stained glass and pane, Quark, Twigs, Create Framed 3.5, and
any set whose undyed member is a glass). In S1 terms: where the set has an undyed glass base, use it; the "white member"
fallback must never pick a glass.

### 3.4 Soap for everything dye reaches (P4)

Add soap to every block in 2.4 "Dye works, soap does not", except sign text:

| Blocks | Soap result |
|---|---|
| CfB cooking table, counter, corners, fridge, sink | CfB default colour |
| Create belts, flap displays | Colour cleared (belt: also drop the W1 water path if it exists) |
| Create nixie tubes | Orange (the undyed nixie tube) |
| Steam Rails smokestacks, buffers, headstocks | Colour cleared |
| PneumaticCraft pneumatic door, aphorism tile | Default colour |
| FramedBlocks framed target | Default overlay |
| CC: Tweaked turtles | Colour cleared (soap becomes the only wash, 3.1) |
| Cat collars (all pets) | Collar back to red |
| Glazed terracotta | No (no undyed target), unchanged |
| Sign text | Excluded: soap keeps wiping the whole text |

One adapter per mod in patcherMod, on the same soap hook as S1 (`SoapWashableHelper`).

### 3.5 New dye and soap families (P5)

Register as colour sets (Moonlight lets addons add sets) or handle with their own adapter, so both dye and soap work:

- Clayworks chiseled terracotta bricks (16, infix; undyed `chiseled_terracotta_bricks` is the base).
- Create Framed stained framed glass: horizontal and vertical, glass and panes (16 x 4, infix). Soap goes to the clear
  framed glass (3.3).
- Glow wool and glow carpet (Glow Ink Plus, 16 x 2, infix). Two-step soap: the first soap removes the colour and keeps the
  glow (coloured glow wool to white glow wool); the second removes the glow (white glow wool to plain white wool). New dye
  interaction: glow ink on wool and carpet makes the glow version of the same colour, and on plain white wool or carpet
  gives white glow wool.
- Create nixie tubes (dye is Create's own D2; soap in 3.4).
- Create D&D blueprint blocks (14, short set), with soap back to the base block where one exists.
- Create Deco placards (15; holds an item, so carry the block entity over).
- Dave's Building gilded boilers (14, short set).
- Dustrial Decor neon lights (8, short set, dye within the 8 only).
- Lightman's Currency blocks (vending machines, large vending machines, freezers, card displays, 13 woods x 15). Block
  entities with stock, so carry-over is mandatory; dupe check DY-07.

Reinforced SecurityCraft and Coloured Water cauldron families (2.6) are not in the owner's notes and stay out.

### 3.6 Soap cleaning for unclean blocks (P2)

Soap turns the block into its clean twin (shape kept: stairs, slabs, walls, vertical slabs). Generate the pair list from
the dump like the existing soap pairs.

| State | Blocks (2.7) | Result |
|---|---|---|
| Mossy | All of them: vanilla, Immersive Weathering, Quark, Twigs, SecurityCraft, Botania livingrock / livingwood / dreamwood, Create Deco (140), Architect's Palette, Philips Ruins, Redeco stepping stones, mossy waystone | Clean twin |
| Overgrown | Architect's Palette overgrown algal bricks (5 shapes) | Algal bricks; also cleared by shears (3.7) |
| Rusty | Dustrial Decor 16, Quark rusty iron plate 4, Dramatic Doors rusty doors 4, IW rusted 11 | Clean twin |
| Dirty | Quark dirty glass and pane | Glass and pane |
| Sooty | Sooty Chimneys dirty chimneys 7 | Clean chimney (scraping stays) |
| Sandy | IW sandy stone family (16 shapes) | Stone family twin |
| Ashen | Supplementaries ashen basalt | Basalt |
| Muddy | Muddy mangrove roots | Mangrove roots (the hoe path in 3.7 also works) |
| Floral | Floral Enchantment flower mossy cobblestone and stone brick (34) | Mossy block, flowers off (shears also work, 3.7) |

Floral and Botania mossy apothecary: the apothecary is its own block and is not a soap case.

### 3.7 Tool interactions

| Tool | Block | Result |
|---|---|---|
| Shears | Overgrown blocks (Architect's Palette) | Clean twin; nothing drops (owner, 2026-10-27) |
| Steel wool | Rusty blocks | Already works for rusty blocks; no change |
| Shovel, right-click | IW sandy blocks | Already built into Immersive Weathering 1.2.10: `Sandy.interactWithPlayer` takes a `ShovelItem` in hand and steps the block down (SANDINESS) with a dig sound and sand particles. Checked 2026-10-27 in the jar's bytecode. No work needed beyond DY-09 and the soap in 3.6. Modded shovels that do not extend `ShovelItem` would not work (not checked) |
| Hoe, right-click | Muddy mangrove roots | Mangrove roots; hoe takes durability (like tilling) |
| Shears | Floral blocks (Floral Enchantment 34) | Mossy cobblestone / stone bricks, flowers off; nothing drops |

Overgrown and floral shears reuse IW's `moss_shearing` approach where it fits (W3), as a tool-action hook in patcherMod.

### 3.8 Plan

| Step | Work | Where |
|---|---|---|
| 1 | Disables (3.1) and the Botania tag entries | KubeJS tag via `global.tags`; patcherMod for coloured water, turtle, banner pattern checks |
| 2 | Soap targets (3.3) | patcherMod soap hook |
| 3 | Dye with block-entity carry-over (3.2) | patcherMod, `SupplementariesDyeBlocksMixin` |
| 4 | Soap adapters (3.4) | patcherMod, one per mod |
| 5 | New families (3.5), glow two-step, glow ink (1 per block) | patcherMod |
| 5b | Crushed glow ink dye, chalk dyeing wool / carpet, glow chalk (3.8a) | KubeJS / patcherMod items, `global.item` keys |
| 6 | Unclean pairs and tool interactions (3.6, 3.7) | patcherMod hook plus generator `tools/*.py` |
| 7 | Regenerate the JEI world tabs (`/dumpworldjei`, `/cdevgen run jei_world`); rows for the new pairs | tools |

Player changelog: add bullets under the dyeing family when each step is built (none yet: nothing built). Banner and glass
behaviour changes (soap target, banner limit) count as player-visible fixes.

### 3.8a Owner answers (2026-10-27, second pass)

- Lightman's blocks: the block entity (stock) is kept; dye and soap only change the colour. Carry-over is a plain
  state swap with the block entity moved across, DY-07 checks the stock.
- Overgrown and floral blocks: shears drop nothing (3.7 updated).
- Glow ink on wool and carpet costs 1 glow ink per block (the dye is used up, not durability).
- New item **crushed glow ink dye** (the glow dye of `blocks/dyeing #4`): made by crushing glow ink; it dyes like a
  normal dye and also gives the glow version in one step (dye + glow in one use, instead of dye then glow ink). Crush
  route and ratio still to set (Create milling / crushing and IE crusher, as for the other pack dyes); one glow ink per
  dye to start.
- Chalk dyes wool and carpet in world (assumed: a chalk item right-click on wool or carpet gives the chalk's colour; the
  Chalk mod's colours match the 16 dyes, one chalk use per block, chalk durability used). To confirm: chalk dyeing
  undyed wool only, or any wool.
- New **glow chalk**: a chalk that draws glowing marks (made from chalk + glow ink; one per colour or the same
  colours as chalk, to set). Glow chalk on wool and carpet gives the glow version of that colour, same as crushed glow
  dye. Glow chalk marks are light level 0..? (to set, probably 7).

Still open: crushed glow dye ratio and recipe; chalk-on-wool limits; glow chalk colours and light level; whether
glow chalk marks wash off with soap like plain marks (assumed yes, to nothing).

### 3.9 In-game checks (add to 2.9)

- DY-07: dye a filled shulker box, sack, present and Lightman's vending machine in world: contents kept, no duplicate on
  breaking. Soap each back.
- DY-08: dye and soap a plain banner in world (works), then a banner with a pattern (nothing happens); both still work in the
  crafting table.
- DY-09: right-click an IW sandy stone brick with a shovel (steps down), and with soap (clean stone).
- DY-10: soap a dyed Create Framed glass, a stained glass and a Quark framed glass: each becomes clear glass, not white.
- DY-11: soap a coloured glow wool: white glow wool; soap again: plain wool; glow ink on wool: glow wool.
- DY-12: dye a Botania flower with dye and soap: nothing. Coloured water: dye interaction off. Turtle with a water bucket:
  dye stays.
- DY-14: crushed glow ink dye on white wool and carpet: glow version in the dye colour. Chalk on wool and carpet dyes it; glow chalk gives the glow version; glow ink costs 1 per block.
- DY-13: shears on overgrown algal bricks and floral mossy cobblestone; hoe on muddy mangrove roots; soap on one of each of
  the 3.6 states.

- 2026-10-28: planned from the scratchpad: water in a cauldron for decor washing recipes, e.g. old and weathered cardboard boxes (TODO `blocks/dyeing #5`). No code changed.
