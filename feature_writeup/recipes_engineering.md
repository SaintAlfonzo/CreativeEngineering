# Engineering recipes: machine gating, mechanisms, redstone, rails, resource stones, dusts

Part of backlog row 20 (recipe progression), split per folder. This one covers `kubejs/server_scripts/engineering/`
except the parts that already have a writeup: components, coils and the sample syringe
([engineering.md](engineering.md)), potion bottling ([potions.md](potions.md)), potion buckets
([potion_buckets.md](potion_buckets.md)) and the archery and bullet blueprints ([archery.md](archery.md)).

## 0. At a glance

| Part | Status |
|---|---|
| Machine gating: deployer on gold, press / saw / drill on IE parts, alloy kiln nether-locked, IE blast furnace on cast iron, Engineer's Workbench on steel | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3); vanilla blast furnace recipe removed, Quark's deepslate / blackstone furnace recipes still make it (F1) |
| PneumaticCraft compressed iron folded into IE steel (steel ingot in `#forge:ingots/compressed_iron`, PNC's block is the steel block) | DEPLOYED |
| Mechanisms: precision on brass, vibration from an echo shard, redstone mechanism by assembly | DEPLOYED |
| Redstone: vanilla, Create, Gates and Project Red parts rebuilt with assembly and blueprint routes; transmitter and redstone links | DEPLOYED |
| Rails and Little Logistics: iron-rod rails, plates, Create tanks, redstone links instead of LL wireless | DEPLOYED |
| Resource stones: Create ore stones by compacting, 3-heat stone generators, crushing tables for 20 stones | DEPLOYED |
| Dusts: cinder flour + potion → 10 resources, amethyst / diamond dust, sulfur, TNT, obsidian | DEPLOYED; amethyst crushing clash fixed 2026-11-01 (F3) |
| Smaller files: crystals, power (Create Crafts & Additions), music, bandage, plant oil, leather from flesh, wool → hemp, seashells, bottles, CC speaker | DEPLOYED |
| Buckets (`process.js`, patcherMod `bucket/`): iron bucket from 3 iron plates; Wooden Bucket (3 planks, water only, stacks to 16) | BUILT 2026-10-29 (patcherMod 1.1.04 rebuilt 2026-10-30, installed 2026-10-31 00:44); RE-28 scratch server PASSED r1031 (2026-10-31, SQ-RE-28); RE-29, RE-30 in game pending; TODO `recipes/engineering #5`, [brief](briefs/recipes_engineering.md) |

- **Pending:** buckets (built 2026-10-29, jar installed 2026-10-31 00:44; RE-28 PASSED r1031 2026-10-31, RE-29, RE-30 in game). Otherwise nothing built and unshipped. RE-01 to RE-20 have never been run. Findings F1 to F10 (section 4); F1 and F2
  need your call; F3 fixed 2026-11-01 (script, `/reload`).
- **Files to open:** [process.js](../kubejs/server_scripts/engineering/process.js) (machine gating),
  [redstone.js](../kubejs/server_scripts/engineering/redstone.js),
  [resource_blocks.js](../kubejs/server_scripts/engineering/resource_blocks.js),
  [dust.js](../kubejs/server_scripts/engineering/dust.js).
- **Knobs:** none. All server scripts (`/reload`).

## 1. Overview

The pack's progression runs through recipes more than through config: a Create machine needs an IE part, an IE
multiblock needs a Nether material, a vanilla block needs a Create component. The `engineering/` folder holds most of
that gating, plus the resource loops (stone generators, ore stones, dusts) that feed it. Almost every replaced recipe
gets three routes: a crafting-grid recipe, a Create sequenced assembly (usually cheaper per item), and an IE Engineer's
Workbench blueprint (usually the best yield).

## 2. Reference

### 2.1 Machine gating (`process.js`, `create_utilities.js`, `pneumatic.js`)

| Item | Pack recipe | What it gates |
|---|---|---|
| Brass hand (Create) | gold plates instead of brass; blueprint `components`: 3 gold plates + andesite alloy | deployers before brass |
| Mechanical press | shaft, 2 stiff springs, andesite casing, iron block | pressing |
| IE sawblade | 4 iron ingots + 4 iron plates (was steel) | |
| Mechanical saw | sawblade over andesite casing, or deploy the blade onto the casing | |
| Mechanical drill | iron drillhead over andesite casing, or deploy it | |
| Mechanical crafter (×2) | 4 gold ingots, redstone mechanism, 2 cogs, crafting table, andesite casing | |
| Vibrating table (Vintage Improvements) | 4 stiff springs, sheetmetal slab, mechanical piston, 2 small weak springs, vibration mechanism | |
| Bearings (windmill, mechanical, clockwork) | turntable instead of the wood slab | |
| Encased chain drive (×3) | 3 chains + andesite casing | |
| Display link | transmitter, brass casing, copper coil block | |
| Fluid valve | pipe + any plate | |
| Rope pulley | any rope instead of wool | |
| IE alloy brick (×2) | 2 polished blackstone bricks + 2 nether bricks; or apply nether bricks to polished blackstone bricks | alloy kiln: Nether first |
| IE cast iron | alloy kiln: iron ingot + coke dust or petcoke dust | |
| IE blast brick (×3) | 4 nether bricks, 4 cast iron, molten nether bricks | IE blast furnace, steel |
| Engineer's Workbench | steel instead of iron | every blueprint |
| Steel (PneumaticCraft) | pressure chamber: iron ingot → steel ingot, iron block → steel block at 6 bar | a second steel route |
| Vanilla blast furnace | vanilla recipe removed; Quark's deepslate / blackstone furnace recipes still make it (F1) | |

Compressed iron is gone as its own material: the PNC recipes making it are removed, IE steel is tagged
`#forge:ingots/compressed_iron` (`metaltags.js`) and the dict's `i.block_steel` is PNC's compressed iron block, so 9
steel ↔ 1 block replaces both mods' storage recipes (IE's slab recipes are pointed at it). The PNC guide book is a book
+ a steel ingot.

### 2.2 Mechanisms (`mechanisms.js`)

| Mechanism | Pack recipe |
|---|---|
| Precision | Create's assembly with a brass plate instead of a gold one; blueprint `components`: 5 cogs, 5 large cogs, 5 iron nuggets, brass plate |
| Vibration (Create Things and Misc) | assembly on an echo shard, 3 loops: rose quartz plate, amethyst shard, cog, redstone; blueprint `redstonecomponents` |
| Redstone (Vintage Improvements) | assembly on a gold plate, 2 loops: copper rod, electron tube, redstone, vibrate, randomizer, rose quartz plate; blueprint |

### 2.3 Redstone (`redstone.js`)

- **Vanilla:** redstone torch (assembly, blueprint ×4); redstone lamp (also from rose quartz; blueprint with rose quartz or
  glowstone block); daylight sensor (assembly on a wood slab with quartz or rose quartz plates, then glass);
  dropper (stone, a Quark wooden chute, redstone); dispenser crafting removed (F2); observer and piston take any stone;
  sticky piston by filling 50 mB slime; hopper blueprint.
- **Create:** rose quartz (heated mixing 6 redstone + quartz → 1; arc furnace 4 redstone; pressure chamber 3 quartz + 9
  redstone → 3 at 3 bar); polished rose quartz by charging (Create Crafts & Additions); rose quartz plate ×2 by saw
  (sawmill version with redstone back is a datapack JSON, [engineering.md](engineering.md)); electron tube (deploy an iron
  plate on polished rose quartz, arc furnace, blueprint ×2); rose quartz lamp (shaped with zinc nuggets, assembly,
  blueprint).
- **Transmitter** (pack item): lightning rod, 3 copper plates, 2 redstone, amethyst shard; assembly; blueprint ×2. Used by
  redstone links (×2 from transmitter + andesite casing, ×4 by assembly or blueprint), the display link and the Etched
  radio.
- **Gates mod:** NAND, NOR, XOR, XNOR and all wireless parts removed; repeater, fast repeater (gold), slow repeater (cast
  iron), comparator, randomizer (prismarine crystal) each get assembly and blueprint routes; rain detector from a daylight
  sensor + lapis + glass; thunder detector from a rain detector + mundane potions + glass.
- **Project Red / CB Microblock:** draw plate, coils, motor, woven cloth, sail and the three saws: recipes removed.
- **Little Logistics wireless:** receiver and transmitter components removed; recipes using them take a redstone link.

### 2.4 Rails and Little Logistics (`transport.js`)

- Rail ×12 from 6 iron rods + a stick (assembly ×8, blueprint `railtransport` ×12); detector, activator, powered and
  Create controller rails ×6 from rails (blueprints ×8).
- Little Logistics: iron ingots → any iron plate in the whole mod; vehicle link from iron wire, 2 from a chain, assembly
  ×6, blueprint; seater car ×4 (planks + iron blocks); fluid car and barge with a Create fluid tank; locomotive and tug
  take a blast furnace (F1); seater barge takes a Create seat; rapid hopper with brass; fluid hopper rebuilt; vessel
  charger with an MV coil, vacuum tubes and redstone blocks.

### 2.5 Resource stones (`resource_blocks.js`)

| Stone | Make | Break down |
|---|---|---|
| Veridium, crimsite, ochrum, asurine (copper, iron, gold, zinc) | compacting 6 nuggets + 2 gravel + 250 mB lava; Vintage extruding over a dust block | Create crush: 52 % crushed ore; IE crush: 5 nuggets |
| Limestone | mixing honey + lava: 1 / 5 / 15 per batch at no heat / heated / superheated; heated mix calcite + bone meal + clay | crush to quartz, lapis, clay; haunt to basalt |
| Scoria | the same with chocolate instead of honey | blaze powder, blaze rod (Create); sulfur (IE) |
| Scorchia (×8) | 8 scoria around coke or petcoke | coke dust, lead and uranium nuggets |
| Stone, cobblestone | water + lava: 1 / 5 / 10 per batch | |
| Basalt | packed ice (blue ice superheated) + soul soil + lava, soul soil returned | blackstone + aluminium, silver, nickel nuggets |
| Andesite | mix cobblestone + diorite: 2 per pair, 32 per 16 heated, 48 superheated | IE: cobblestone |
| Obsidian | mixing water + lava 250 mB each, heated, both used up (the old `create_generators` recipe, kept) | |
| Deepslate | compact cobblestone + lava (8 / 32 / 48 by heat) | IE: deepslate cobblestone; Create: gravel |
| Granite, diorite, terracotta, calcite, dripstone, tuff, magma, netherrack, nether bricks, soul sandstone, blackstone, obsidian, galcite, layered obsidian | | crushing tables (tuff: flint and metal nuggets; nether bricks: 0.5-1 % netherite fragment; soul sandstone: glowstone; obsidian: powdered obsidian) |

The old `create_generators` cobblestone, stone, basalt, limestone and scoria generators (the mod is gone since 2026-10-21), Create Ultimate Factory's ore-stone compacting and crushing, Create Netherless
basalt, soul sand and deepslate recipes, and Botania's granite and calcite conversions are removed. Ash ×16 compacts to
tuff. Stone recipes in Gates, Create Things and Misc, stonecutter, repeater and comparator take any stone.

### 2.6 Dusts (`dust.js`, `end.js`)

- **Cinder flour + potion:** 8 cinder flour + a potion bottle → 8 of a resource (bottle back); IE Bottler 20 mB per flour,
  180 mB per flour pile (×9); Create filling 25 mB per flour, 225 mB per pile.

  | Potion | Resource | Potion | Resource |
  |---|---|---|---|
  | Strength | redstone | Weakness | cocoa powder |
  | Night vision | glowstone | Slow falling | matcha |
  | Harming | gunpowder | Healing | ash (×4) |
  | Long fire resistance | sulfur dust | Resistance (`kubejs:resistance`) | powdered obsidian |
  | Long invisibility | saltpeter | Strong slowness | coke dust |

- **Other dusts:** amethyst shard → amethyst dust; diamond → diamond dust (Create Crafts & Additions' own removed);
  amethyst shard → amethyst dust also on the millstone (no glowstone from shards since 2026-11-01, F3); ender pearl → 2-3 ender shards (Ender's
  Delight).
- **Sulfur:** Vintage Improvements' scoria → sulfur removed; blaze powder → 75 % sulfur. **TNT ×4** from 5 gunpowder + 4
  sulfur (vanilla recipe removed), so TNT needs the Nether.
- **Obsidian:** powdered obsidian block + 2000 mB lava, superheated → layered obsidian; saw → 12 sturdy plates.

### 2.7 Crystals and the smaller files

| File | What |
|---|---|
| `crystal.js` | budding amethyst (assembly on an amethyst block: amethyst dust, 250 mB thick potion, 4 loops); amethyst lens (cut + polish, 5 loops); diamond from 9 coal blocks at 8 bar, diamond shard from 4 at 8 bar or compacting 9 + 250 mB lava superheated (Create Ultimate Factory and PNC coal → diamond removed); echo shard from 4 sculk around an amethyst shard, or assembly |
| `power.js` | Create Crafts & Additions: spools, relay and connectors removed; IE light bulb (carbon ×1, or steel spring ×3); vacuum tube ×3 (nickel plate, bulb glass, copper wire, redstone); capacitor (electrum + constantan plates, electron tube); portable energy interface ×2; modular accumulator from an IE HV accumulator |
| `music.js` | note block with an amethyst shard; jukebox with a note block + diamond; Etched radio with a transmitter; Supplementaries speaker block ×2 from railway casings, filters and a vibration mechanism (its own recipe and Create Things and Misc's brass speaker recipe removed) |
| `computing.js` | CC speaker takes the Supplementaries speaker instead of a note block |
| `health.js` | PNC bandage: canvas or hemp fabric + 4 honey bottles or 4 glycerol; Bottler / Spout with honey (1) or phenolic resin (2) |
| `oil.js` | flax seeds and the 8 grape seed kinds squeeze to 50-60 mB plant oil; PNC crops → vegetable oil removed |
| `mob_resources.js` | jrftl rotten flesh → leather removed; ersatz leather from honeycomb + hemp fabric; rotten flesh + 250 mB creosote → prepared flesh → smoke to leather (and blocks) |
| `fabric.js` | about 50 recipes take hemp fabric as well as wool; frames and paintings also take paper or canvas |
| `vibrating.js` | vibrating sand: four seashells 22.5 % each, nautilus shell 9 %, heart of the sea 1 % |
| `laser_cutting.js` | glass → 3 bottles |

## 3. What the pack changes

- Everything goes through `global.NCH` (`R.remove`, `R.replaceInput`, `R.shaped`, `R.createSequencedAssembly` with
  `R.incomplete(key)`, `R.ieBlueprint(output, inputs, category, e)`, `R.pcPressureChamber`, ...). Ids are generated from
  output, input and process (`creativeengineering:createcrushing/...` and so on) unless an id is given.
- Blueprint items (`blueprints.js`): each category from a Create empty schematic, most also from paper + dye (the
  original IE shape); any blueprint + an empty schematic → 2 copies. IE's molds blueprint recipe is replaced.
- The old commented-out `global.CE` version of `resource_blocks.js` (from line 257) was deleted 2026-10-22 ([TODO](todo/TODO.md) `tooling/conventions #3`).

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. The vanilla blast furnace has no recipe. Your call.** `process.js` removes `minecraft:blast_furnace` with the
  comment "require deepslate or blackstone recipe", but no replacement exists in the scripts or `kubejs/data`. Little
  Logistics' locomotive and tug now need one (`transport.js`). Nether's Delight's blackstone blast furnace is a different
  block. Add a recipe (deepslate or blackstone around a furnace and iron), or point the two LL recipes at another block.
  Check with RE-03. Rechecked 2026-10-21 (Quark 3.4-418 jar): Quark's `quark:building/crafting/furnaces/deepslate_blast_furnace`
  and `blackstone_blast_furnace` (5 iron ingots, a Quark deepslate / blackstone furnace, 3 smooth stone; flag
  `variant_furnaces` on) still make `minecraft:blast_furnace`, which is what the comment asks for; not yet seen in JEI
  ([brief](briefs/recipes_engineering.md)).
- **F2. The dispenser has no recipe. Your call.** `redstone.js` removes it ("must be crafted from dropper") and adds
  nothing. Unless a mod adds dropper → dispenser, dispensers are loot only. Check with RE-08.
- **F3. Amethyst shards had two crushing recipes in each machine. FIXED 2026-11-01** (user call): no glowstone from
  shards. One recipe per machine: millstone and crushing wheels 75 % amethyst dust, IE crusher amethyst dust + 25 %
  amethyst dust (1.25 average). Glowstone keeps its other sources (cinder flour + night vision, soul sandstone, Create's
  glowstone block crushing). Verify with RE-16.
- **F4. Coal blocks in the pressure chamber:** 9 blocks → diamond and 4 blocks → diamond shard at the same pressure.
  With 9 or more in the chamber which one runs depends on PNC's recipe order (not checked). Check with RE-12.
- **F5. Cinder flour routes don't line up.** Redstone, glowstone and gunpowder have no single-flour Create filling (only
  the pile; `dust.js` lines 23-39, rechecked 2026-10-21); the other seven have both. Shapeless gives 1 per flour, the machines 1 per flour too, so the machines only save the
  bottle.
- **F6. Define-once:** `end.js` sets `i.ender_shard` and `dust.js` `i.matcha`, `redstone.js` `i.mundane_potion`; all
  three are already in `item_dict.js` with the same value. Delete the lines.
- **F7. Raw recipe code:** `mechanisms.js` builds its vibrating step with `e.custom(...)` and uses the shared
  `i.incomplete` instead of `R.incomplete(key)`; an `R.viVibrating` step form would fit the helper rule.
- **F8. Comments that no longer match:** the mechanical press comment says "replace brass with gold" (it uses stiff
  springs and an iron block); the vehicle link step ids say `step2_ingot` for a nugget step.
- **F9. Thunder detector filling NBT** omits `Bottle:"REGULAR"`, so splash and lingering mundane potion fluid probably
  match too (Create's fluid NBT match is partial; not checked).
- **F10. Old code:** the commented-out `global.CE` copy in `resource_blocks.js` (230 lines) was deleted 2026-10-22.
- **F11. Create add-on machines on components (scratchpad 2026-10-21).** Wanted: mechanical arm on zinc components and
  springs; helve hammer on a netherite block and mechanical components; centrifuge, curving press and coiler on zinc
  components; compressor on copper components; extruder on netherite components; laser cutter on a lens, precision
  mechanism, diamond and brass components, with a new laser assembly (sequenced assembly / blueprint); rotation speed
  controller on brass components (double output for the brass craft, plus an expensive brassless recipe with complex
  components); sequenced gearshift on zinc and brass components, double output. Check the vibration table recipe. Laser
  cutting metal blocks into plates (user 2026-10-21). In [TODO](todo/TODO.md) `recipes/engineering #6`.

### Known limits

- **JEI clutter:** three routes per item triple the recipe lists; intended.
- **Nether first:** alloy kiln, blast brick, TNT and sulfur all need Nether materials, so steel needs a Nether trip.

### Rejected options

- **One route per item:** the assembly and blueprint routes are the reward for building the machines.

## 5. File map

- [server_scripts/engineering/](../kubejs/server_scripts/engineering/): `process.js`, `create_utilities.js`,
  `pneumatic.js`, `mechanisms.js`, `redstone.js`, `transport.js`, `resource_blocks.js`, `dust.js`, `end.js`,
  `crystal.js`, `power.js`, `music.js`, `computing.js`, `health.js`, `oil.js`, `mob_resources.js`, `fabric.js`,
  `vibrating.js`, `laser_cutting.js`, `blueprints.js`.
- Elsewhere: [engineering.md](engineering.md) (`components.js`, `sample_syringe.js`), [potions.md](potions.md)
  (`potion_bottling.js`), [potion_buckets.md](potion_buckets.md) (`potion_buckets.js`).
- Helpers: [func/neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js); tags:
  [tags/metaltags.js](../kubejs/server_scripts/tags/metaltags.js) (steel as compressed iron).

## 6. Offline checks

None. KubeJS logs a validation error for any helper call naming an item that doesn't exist (`_validateAny`); check
`logs/kubejs/server.log` after `/reload`.

## 7. In-game test suite

Use JEI (recipe and usage views) and a creative test world with Create, IE and PNC machines.

- [ ] **RE-01 Log:** `/reload` → no `_validateAny` errors from engineering scripts in `server.log`.
- [ ] **RE-02 Deployer early:** JEI brass hand → gold plates; no brass needed.
- [ ] **RE-03 Blast furnace (F1):** JEI `minecraft:blast_furnace` → note whether any recipe exists; JEI locomotive → needs
  one.
- [ ] **RE-04 Press, saw, drill:** JEI each → the recipes in 2.1, including deploying the sawblade and drillhead.
- [ ] **RE-05 Nether lock:** JEI alloy brick → only blackstone + nether brick routes; blast brick → cast iron + molten
  nether bricks.
- [ ] **RE-06 Workbench:** JEI Engineer's Workbench → steel.
- [ ] **RE-07 Steel:** pressure chamber at 6 bar with an iron ingot → IE steel; 9 steel → the steel block; a PNC recipe
  that asks for compressed iron accepts steel.
- [ ] **RE-08 Dispenser (F2):** JEI dispenser → note any recipe.
- [ ] **RE-09 Mechanisms:** sequenced assembly of a vibration mechanism from an echo shard runs 3 loops; redstone
  mechanism with a vibrating step completes on a vibrating table.
- [ ] **RE-10 Transmitter and links:** craft a transmitter, then 2 redstone links; Etched radio and display link use it.
- [ ] **RE-11 Rails:** 6 iron rods + stick → 12 rails; workbench `railtransport` blueprint → 12.
- [ ] **RE-12 Coal (F4):** 9 coal blocks in a pressure chamber at 8 bar → note diamond or shard.
- [ ] **RE-13 Stone generator:** mixer with water + lava, unheated / heated / superheated → 1 / 5 / 10 cobblestone per
  batch.
- [ ] **RE-21 Obsidian generator:** heated mixer with 250 mB water + 250 mB lava → 1 obsidian, both fluids used up;
  unheated → nothing.
- [ ] **RE-28 Iron bucket [scratch]:** *PASS r1031 (2026-10-31, SQ-RE-28; JEI look in game).* JEI `minecraft:bucket` → only `shaped/bucket_from_plates` (3 iron plates, V shape).
- [ ] **RE-29 Wooden bucket [scratch]:** craft from 3 planks; scoops and places water only (lava, a cow's milk do nothing);
  placing returns the wooden bucket; texture and names show.
- [ ] **RE-30 Wooden bucket in machines [scratch]:** Create spout / basin / FD cooking pot drain the wooden water bucket
  and return the empty one; it fills from water only.
- [ ] **RE-14 Ore stone:** compact 6 iron nuggets + 2 gravel + 250 mB lava → crimsite; IE crush → 5 iron nuggets.
- [ ] **RE-15 Cinder flour:** 8 cinder flour + a strength potion → 8 redstone and a glass bottle; a pile in the Bottler
  with 180 mB strength → 9 redstone.
- [ ] **RE-16 Amethyst crushing (F3):** JEI uses of an amethyst shard: millstone and crushing wheels each show one
  recipe (75 % amethyst dust), IE crusher one (amethyst dust + 25 % amethyst dust); no glowstone recipe from shards.
- [ ] **RE-17 TNT:** JEI TNT → only gunpowder + sulfur, ×4.
- [ ] **RE-18 Layered obsidian:** superheated mixer, powdered obsidian block + 2000 mB lava → layered obsidian; saw → 12
  sturdy plates.
- [ ] **RE-19 Leather from flesh:** spout 250 mB creosote on rotten flesh → prepared flesh; smoker → leather.
- [ ] **RE-20 Seashells:** sand on a vibrating table → seashells, sometimes a nautilus shell.

## 8. History

- By 2026-09-27 (initial git commit): the folder built, first on `global.CE` (the commented copy in
  `resource_blocks.js`), then on `global.NCH`; shipped with 1.0.3.
- 2026-10-18: this writeup; findings F1 to F10; tests RE-01 to RE-20. No code changed.
- 2026-10-21: `create_generators` removed from the pack; its obsidian recipe is now in `resource_blocks.js`
  (`obsidian_generation_heated`), the 5 `R.remove` lines for its other recipes deleted; test RE-21.
- 2026-10-29: buckets (TODO `recipes/engineering #5`): `process.js` removes `minecraft:bucket`, adds
  `shaped/bucket_from_plates` and `shaped/wooden_bucket`; patcherMod `bucket/WoodenBucketItems` + `WoodenBucketItem`
  (`creativeengineering:wooden_bucket`, `wooden_water_bucket`; water only; cech12's MIT texture). Not done: dispenser,
  cauldron, use as `minecraft:water_bucket` in recipes. Tests RE-28 to RE-30 (scratch queue). Not run in game.
- 2026-10-31: scratchpad notes translated into TODO rows (`engineering/dreams-and-desires #9`, `recipes/engineering #7`). No code changed.
