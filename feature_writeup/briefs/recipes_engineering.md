# Brief: recipes / engineering

Written 2026-10-21 for the open `recipes/engineering` rows of [TODO.md](../todo/TODO.md). Reference writeup:
[recipes_engineering.md](../recipes_engineering.md) (test prefix **RE-**, last used RE-20; new tests below start at
RE-21, take the next free number when you add them). Components and the sawmill are in
[engineering.md](../engineering.md) (`EN-`). Layout: [README.md](README.md).

**Verified against jars** (downloaded and read or decompiled 2026-10-21): Quark 3.4-418 (variant furnace recipes),
Nether's Delight 3.1, Little Logistics 1.4.0 (`steam_locomotive`, `tug`), Create 0.5.1.i (`crushing/amethyst_*`,
`crushing/glowstone`, `crafting/kinetics/{mechanical_arm,rotation_speed_controller,sequenced_gearshift}`,
`FluidIngredient$FluidStackIngredient.testInternal`), Immersive Engineering 9.2.4 (`crusher/amethyst`), PneumaticCraft
4.3.15 (`PressureChamberValveBlockEntity` recipe selection), Create Recycle Everything 1.0.2
(`create_crush_everything:diamond_from_shards`), Vintage Improvements 0.2.0.3 (machine recipes, lang names), Create
Mechanical Extruder 1.5.7.e, AstikorCarts 1.1.2 (all four recipes), Myrtrees 1.2.0 (`WoodenBucketBlock`). Anything
else is marked unverified.

## Scope and order

| Row | Change | Work | Group |
|---|---|---|---|
| engineering #1 | Blast furnace and dispenser recipes | KubeJS | A: quick fixes |
| engineering #2 | Amethyst crushing clash (Bug) | KubeJS | A |
| engineering #3 | Smaller findings (coal blocks, cinder flour, thunder detector NBT, stale comments) | KubeJS | A |
| engineering #5 | Bucket recipes (iron plates; wooden water bucket) | KubeJS + patcherMod or mods/ | B: new crafts |
| engineering #4 | Horse cart recipes | KubeJS | B |
| engineering #6 | Create add-on machine recipes on components; laser cutting blocks into plates | KubeJS | C: big table |

Suggested order: **A** in one `/reload` (all edits in `engineering/` scripts, nothing new registered). Then **B**: the
plate bucket and the carts are small KubeJS edits; the wooden bucket needs a decision (mod or patcherMod item, both
need a restart / jar). **C** last and in one pass, after `engineering/sawmill-and-components #8` (generated component
chains) if that lands first, since #6 consumes components; its laser-cut plates share the metal list with
`recipes/metal #1` ([recipes_metal.md](recipes_metal.md)), build them together. `recipes/progression #1` closes when
the dispenser here and the Explorer's Compass (`recipes/magic #1`) are done.

---

## engineering #1: Blast furnace and dispenser recipes

Status Planned · Work KubeJS · TODO `recipes/engineering #1` (findings F1, F2)

### Goal
The vanilla blast furnace (needed by Little Logistics' locomotive and tug) and the dispenser can be crafted.

### Now
- **Blast furnace: already craftable.** [process.js](../../kubejs/server_scripts/engineering/process.js) lines 8-11
  remove only `minecraft:blast_furnace` ("require deepslate or blackstone recipe"). Quark's
  `quark:building/crafting/furnaces/deepslate_blast_furnace` and `blackstone_blast_furnace` stay: `III / IXI / ###`
  with `minecraft:iron_ingot`, `quark:deepslate_furnace` / `quark:blackstone_furnace`, `minecraft:smooth_stone`,
  condition `quark:flag variant_furnaces` (**jar**), and "Variant Furnaces" = true in both
  `config/quark-common.toml` and `configs_edited/root_config/quark-common.toml` (line 127). Not yet seen in JEI.
- [transport.js](../../kubejs/server_scripts/engineering/transport.js) lines 92-100 swap `i.furnace` → `i.blast_furnace`
  in `littlelogistics:steam_locomotive` and `littlelogistics:tug` (LL recipes use the `minecraft:furnace` item,
  **jar**).
- Nether's Delight's `nethersdelight:blackstone_blast_furnace` (its own block, **jar**) does not satisfy those recipes.
- **Dispenser: no recipe.** [redstone.js](../../kubejs/server_scripts/engineering/redstone.js) lines 57-59 remove
  `minecraft:dispenser` ("must be crafted from dropper") and add nothing. The dropper is rebuilt at lines 52-55 (any
  stone around a Quark chute and redstone, plus a `redstonecomponents` blueprint).

### Build
1. Blast furnace: nothing to add. Update the comment at `process.js` line 10 to name the Quark recipes. Optional (see
   Decisions): swap their iron ingots for plates:
   ```js
   R.bulk('replaceInput', [
     [{ id: 'quark:building/crafting/furnaces/deepslate_blast_furnace' }, i.ingot_iron, i.any_plate_iron, e],
     [{ id: 'quark:building/crafting/furnaces/blackstone_blast_furnace' }, i.ingot_iron, i.any_plate_iron, e],
   ])
   ```
2. Dispenser, in `redstone.js` after line 59, the pack's three routes:
   ```js
   R.shaped(Item.of(i.dispenser, 1), [' B ', 'RDR'], { B: i.bow, D: i.dropper, R: i.redstone }, e, 'dispenser_from_dropper')
   R.createDeploying(Item.of(i.dispenser, 1), [i.dropper, i.bow], e, 'dispenser_deploying')
   R.ieBlueprint(Item.of(i.dispenser, 1), [i.dropper, i.bow, i.redstone], 'redstonecomponents', e, 'dispenser_by_redstonecomponents')
   ```
3. Writeup: F1 and F2 fixed, 2.3 "dispenser crafting removed (F2)" → the new routes; RE-03, RE-08.

### Ids and data
`i.blast_furnace` (dict line 554), `i.dispenser` (1446), `i.dropper` (549), `i.bow` (2072), `i.redstone`,
`i.any_plate_iron` (1766), `i.ingot_iron` (856), `i.deepslate_furnace` (3009), `i.blackstone_furnace` (2898). All in
the dict. Quark recipe ids verified in the jar.

### Decisions
- **Plates in the Quark blast furnace?** Recommend yes (matches "iron ingots → iron plates in the whole of Little
  Logistics" and the press / saw gating); costs nothing to build.
- **Dispenser input: bow or crossbow?** Recommend the bow (vanilla's part, cheap), the dropper carries the stone.

### Gotchas
- `R.remove({ id })` on a mod recipe id never touches another mod's recipe for the same output; that is why the Quark
  ones survived. Don't "fix" this by removing by output.
- `createDeploying` without `keepIngredient` consumes the bow, as intended.

### Tests
- **RE-21 Blast furnace:** JEI `minecraft:blast_furnace` → the two Quark recipes (with plates if step 1's option is
  taken); craft one from a deepslate furnace; JEI locomotive and tug accept it. Replaces RE-03's "note whether".
- **RE-22 Dispenser:** craft a dispenser from a dropper, bow and 2 redstone; deploy a bow onto a dropper; workbench
  `redstonecomponents`. Replaces RE-08.

---

## engineering #2: Amethyst crushing clash (Bug)

Status Bug · Work KubeJS · TODO `recipes/engineering #2` (F3)

### Goal
One amethyst-shard recipe per machine, so JEI shows only what the machine does.

### Now
[dust.js](../../kubejs/server_scripts/engineering/dust.js):

| Line | Helper | Recipe id (generated by `toId`) | Output |
|---|---|---|---|
| 12 | `createCrushing` | `creativeengineering:createcrushing/amethyst_shard_to_amethyst_dust_by_createcrushing` | 75 % amethyst dust |
| 13 | `ieCrushing` | `creativeengineering:iecrushing/amethyst_shard_to_amethyst_dust_by_iecrushing` | amethyst dust + 25 % amethyst dust |
| 103 | `createMilling` | `creativeengineering:createmilling/amethyst_shard_to_glowstone_dust_by_createmilling` | 75 % glowstone dust |
| 104 | `createCrushing` | `creativeengineering:createcrushing/amethyst_shard_to_glowstone_dust_by_createcrushing` | 75 % glowstone dust |
| 105 | `ieCrushing` | `creativeengineering:iecrushing/amethyst_shard_to_glowstone_dust_by_iecrushing` | glowstone dust + 25 % amethyst dust |

- Each of crushing wheels and IE crusher picks the first matching recipe in recipe-map order (unknown which, RE-16);
  the other is dead but shows in JEI.
- `creativeengineering:amethyst_dust` (`i.amethyst_dust`, dict line 30) has **no other source**; it is needed by the
  budding amethyst assembly ([crystal.js](../../kubejs/server_scripts/engineering/crystal.js) line 15). Glowstone dust
  has other sources: cinder flour + night vision (`dust.js` 29-33), soul sandstone in the IE crusher
  (`resource_blocks.js` line 197), Create's glowstone block crushing.
- No mod recipe takes an amethyst shard as crushing input (Create only has `crushing/amethyst_block` and
  `amethyst_cluster` → shards; IE only `crusher/amethyst` for the block, **jars**).

### Build
1. Delete `dust.js` lines 104-105 (the crushing-wheel and IE-crusher glowstone recipes). Keep line 103 (millstone →
   glowstone) and lines 12-13 (wheels and crusher → amethyst dust).
2. Optional, if glowstone from amethyst should stay on the big machines: add it as a secondary instead of a second
   recipe, e.g. line 12 becomes `R.createCrushing([Item.of(i.amethyst_dust, 1).withChance(0.75), Item.of(i.glowstone, 1).withChance(0.25)], i.amethyst_shard, e)`;
   line 13's secondaries become `[{ chance: 0.25, output: Item.of(i.glowstone, 1) }]`.
3. Move the remaining glowstone line under the Amethyst header (or a comment at line 102 "millstone only") so the split
   is visible.
4. Writeup: 2.6 "amethyst shard → glowstone (millstone, crushing wheels, IE crusher; F3)" → millstone only; F3 fixed;
   RE-16 rewritten.

### Ids and data
Ids above (derived from `NCH.toId`; check with `/kubejs` recipe search after the reload if a test needs them).

### Decisions
- **Which output per machine?** Recommend: millstone → glowstone, crushing wheels and IE crusher → amethyst dust (the
  dust has no other source and gates budding amethyst; the millstone is the early machine, glowstone is an early
  need). With step 2 the big machines still give some glowstone.

### Gotchas
- `createCrushing` with an array output generates its id from the first output only; keep the explicit id style the
  file uses (none) or pass an `idOverride` so the id doesn't change when outputs are reordered.

### Tests
- **RE-23 Amethyst split:** JEI uses of amethyst shard → millstone: glowstone; crushing wheels: amethyst dust (+ glowstone
  if step 2); IE crusher: amethyst dust (+ glowstone). Run each machine once. Replaces RE-16.

---

## engineering #3: Smaller findings

Status Planned · Work KubeJS · TODO `recipes/engineering #3` (F4, F5, F8, F9)

### Goal
Fix four small inconsistencies in one pass.

### Now
- **F4 Coal blocks in the pressure chamber** ([crystal.js](../../kubejs/server_scripts/engineering/crystal.js) lines
  33-35): `pcPressureChamber(i.diamond, Item.of(i.coal_block, 9), 8, e)` (id
  `creativeengineering:pcpressurechamber/coal_block_to_diamond_by_pressure`) and `pcPressureChamber(i.diamond_shard,
  Item.of(i.coal_block, 4), 8, e)` (`..._to_diamond_shard_by_pressure`); line 33 also compacts 9 blocks + 250 mB lava
  (superheated) → 1 shard. PNC selection (**decompiled** `PressureChamberValveBlockEntity.checkForRecipeRecalc` /
  `processApplicableRecipes`): every recipe whose ingredients are present is collected in recipe-stream order, and the
  first one with enough pressure runs. With 9+ blocks both match, so which runs is the stream order. Value: 2 shards →
  1 diamond (`create_crush_everything:diamond_from_shards`, **jar**), so the shard route is 8 blocks per diamond, the
  diamond route 9: they are not even consistent.
- **F5 Cinder flour** ([dust.js](../../kubejs/server_scripts/engineering/dust.js) lines 23-39): redstone (23-27),
  glowstone (29-33) and gunpowder (35-39) have shapeless, IE Bottler (flour and pile) and Create filling for the pile
  only; the other seven (sulfur, saltpeter, coke dust, cocoa, matcha, ash, powdered obsidian) also have a single-flour
  `createFilling` (e.g. line 44 `cinder_sulfur_filling`, 25 mB).
- **F9 Thunder detector** ([redstone.js](../../kubejs/server_scripts/engineering/redstone.js) line 226):
  `Fluid.of(f.create_potion, 250, '{Potion:"minecraft:mundane"}')`. Create's fluid match is partial
  (**decompiled** `FluidStackIngredient.testInternal`: `tag.copy().merge(tagToMatch).equals(tag)`), so splash and
  lingering mundane potion fluid match too. Every other potion filling in the pack spells out `Bottle:"REGULAR"`.
- **F8 Stale comments:** `process.js` lines 15-16 (mechanical press "Replace brass w/ gold, more complex recipe"; the
  recipe at 18 is shaft, 2 stiff springs, andesite casing, iron block); `transport.js` lines 69, 71, 73 give the
  vehicle-link assembly steps ids `createdeploying/vehicle_link_step1_nugget`, `_step2_ingot`, `_step3_nugget`, but the
  steps deploy iron wire, an iron nugget, iron wire (the base is the nugget). The ids have no namespace either.

### Build
1. F4, crystal.js line 34: make the diamond recipe consistent with the shard one, `Item.of(i.coal_block, 8)` (8 blocks
   → 1 diamond, 4 → 1 shard = half). Then whichever runs, 8 blocks give one diamond's worth; the clash stops mattering.
2. F5, dust.js: add the three missing single-flour fillings next to their pile lines, same shape as line 44:
   ```js
   R.createFilling(i.redstone, [Fluid.of(f.create_potion, 25, '{Bottle:"REGULAR",Potion:"minecraft:strength"}'), i.cinder_flour], e, 'cinder_redstone_filling')
   R.createFilling(i.glowstone, [Fluid.of(f.create_potion, 25, '{Bottle:"REGULAR",Potion:"minecraft:night_vision"}'), i.cinder_flour], e, 'cinder_glowstone_filling')
   R.createFilling(i.gunpowder, [Fluid.of(f.create_potion, 25, '{Bottle:"REGULAR",Potion:"minecraft:harming"}'), i.cinder_flour], e, 'cinder_gunpowder_filling')
   ```
3. F9, redstone.js line 226: `'{Bottle:"REGULAR",Potion:"minecraft:mundane"}'`. The shaped (line 222) and blueprint
   (229) routes use `i.mundane_potion` (strict `forge:nbt` on `minecraft:potion`), already regular-only.
4. F8: rewrite the press comment ("stiff springs and an iron block instead of brass") and drop the three `.id(...)`
   calls on the sequence steps (sub-recipes inside a sequenced assembly don't need ids; Create serialises them inline).
   If ids are kept, name them `vehicle_link_step{1,2,3}_{wire,nugget,wire}` with the `creativeengineering:` namespace.
5. Writeup: F4, F5, F8, F9 fixed; RE-12 (expect a diamond from 8 blocks); RE-15 add a single-flour filling.

### Ids and data
`i.coal_block` (dict 2128), `i.diamond`, `i.diamond_shard` (`create_crush_everything:diamond_shard`, 5302),
`i.cinder_flour`, `f.create_potion`, `i.mundane_potion` (1363). No new keys.

### Decisions
- **F4: 8 blocks for a diamond, or drop the 9-block recipe?** Recommend 8 (keeps the direct diamond for players who
  don't want shards, and removes the order dependence).
- **F5: add singles or remove the pile-only gap the other way?** Recommend adding the singles (every other resource
  has them; cheap).

### Gotchas
- F6 (local redefinitions of `i.ender_shard`, `i.matcha`, `i.mundane_potion` in `end.js` line 7, `dust.js` line 8,
  `redstone.js` line 9) and F7 (raw `e.custom` vibrating step in `mechanisms.js`) are not in this row; they sit in
  `tooling/conventions #2` and `#1`. Do them in the same pass only if those rows are free.
- PNC's "first applicable" rule also means any future pressure recipe whose input is a subset of another's has
  the same problem; keep inputs value-consistent.

### Tests
- **RE-24 Coal:** 9 coal blocks in a pressure chamber at 8 bar → a diamond (1 block left) or 2 shards (1 left); either
  is one diamond's worth.
- **RE-25 Cinder singles:** spout 25 mB strength / night vision / harming potion onto one cinder flour → 1 redstone /
  glowstone dust / gunpowder.
- **RE-26 Thunder detector NBT:** a spout with splash mundane potion fluid does not advance the thunder detector
  assembly; regular mundane does.

---

## engineering #4: Horse cart recipes

Status Planned · Work KubeJS · TODO `recipes/engineering #4` (scratchpad 2026-10-19)

### Goal
Fold AstikorCarts' four recipes into the pack's progression (iron plates, the three-route pattern where it pays).

### Now
- No pack script touches `astikorcarts`; no dict keys exist for it. The four recipes (**jar**):

  | Recipe id | Pattern | Keys |
  |---|---|---|
  | `astikorcarts:wheel` | `sss / sps / sss` | s `#forge:rods/wooden`, p `#minecraft:planks` |
  | `astikorcarts:animal_cart` | `ppp / ppp / wpw` | p planks, w wheel |
  | `astikorcarts:supply_cart` | `pcp / pcp / wpw` | p planks, c `#forge:chests/wooden`, w wheel |
  | `astikorcarts:plow` | `sss / psp / wpw` | s wooden rods, p planks, w wheel |
- Carts are horse-drawn early transport, before Little Logistics trains (which need the blast furnace and Create
  tanks); the plow tills while pulled.

### Build
1. Dict: add an AstikorCarts block to `item_dict.js` (next to the Little Logistics keys, lines 566-572):
   `i.ac_wheel = 'astikorcarts:wheel'`, `i.ac_animal_cart`, `i.ac_supply_cart`, `i.ac_plow`.
2. In `transport.js` (new section "Horse carts"), recommended changes:
   - Wheel: iron rim. `R.remove({ id: 'astikorcarts:wheel' }, e)`;
     `R.shaped(Item.of(i.ac_wheel, 1), ['SPS', 'P P', 'SPS'], { S: i.any_stick, P: i.any_plate_iron }, e, 'ac_wheel')`
     plus a blueprint `R.ieBlueprint(Item.of(i.ac_wheel, 2), [Item.of(i.any_plate_iron, 2), Item.of(i.stick_treated, 4)], 'railtransport', e, 'ac_wheel_by_railtransport')`.
   - Plow: a real blade. `R.replaceInput({ id: 'astikorcarts:plow' }, i.any_stick, i.any_plate_iron, e)` (the top row
     and centre become plates).
   - Animal cart and supply cart: keep as they are (planks + wheels); the wheel carries the iron cost.
3. Writeup: a short "Horse carts" paragraph in recipes_engineering.md 2.4 and a test.

### Ids and data
`i.any_stick` (`#forge:rods/wooden`, dict 1646), `i.stick_treated` (1645), `i.any_plate_iron`, `i.planks` (1779),
`i.t_chest_wooden` (128). New keys: the four `i.ac_*` above (not in the dict yet; key style follows `i.ll_*`).

### Decisions
- **How deep?** The row says "integrate" with no detail. Recommend the iron-rim wheel and the plow blade only; carts
  stay cheap so they keep their early-game niche. Alternatives: treated wood everywhere (IE-gated, later), or leave all
  four and only add blueprints.

### Gotchas
- `R.replaceInput` on the plow swaps every `#forge:rods/wooden` slot; if only the top row should change, remove and
  rebuild it with `R.shaped` instead.
- Stack sizes: carts are already listed in [stack_sizes.md](../stack_sizes.md) (stack 1); nothing to change.

### Tests
- **RE-27 Carts:** craft a wheel from plates and sticks, then each cart and the plow; JEI wheel shows the
  `railtransport` blueprint.

---

## engineering #5: Bucket recipes

Status Planned · Work KubeJS (+ patcherMod or mods/ for the wooden bucket) · TODO `recipes/engineering #5`
(scratchpad 2026-10-19)

### Goal
Buckets made from iron plates; an early wooden bucket that carries water.

### Now
- `minecraft:bucket` (`i.bucket`, dict 2099) still has vanilla's `minecraft:bucket` recipe (3 iron ingots, `# # / #`);
  no pack script touches it. Furnace Recycle smelts a bucket back to 1 iron ingot
  ([feature_ports.js](../../kubejs/server_scripts/other/feature_ports.js) line 51). The dragon-breathing fan eats
  buckets for shulker shells (`magic/dragon_breathing.js` line 8, `engineering/sawmill-and-components #7`).
- No wooden fluid bucket exists in the pack. `myrtrees:wooden_bucket` is a **block** that collects latex under a tree
  tap (**decompiled** `WoodenBucketBlock.use`: only `latex`), not a fluid container. The mod list has no bucket mod.
- KubeJS 6 can't register a custom-behaviour bucket item (only its own fluids' buckets), so a water-only wooden bucket
  needs Java (patcherMod) or a mod.

### Build
1. Plate bucket, in `engineering/process.js` (or `transport.js`, wherever "iron parts" live; recommend `process.js`):
   ```js
   R.remove({ id: 'minecraft:bucket' }, e)
   R.shaped(Item.of(i.bucket, 1), ['P P', ' P '], { P: i.any_plate_iron }, e, 'bucket_from_plates')
   ```
   Optional third route: a blueprint (`components`, 3 plates → 2 buckets), if the three-route pattern is wanted here.
2. Wooden water bucket, one of:
   - **mods/** (recommended if available): a maintained wooden bucket mod for Forge 1.19.2 (for example cech12's
     "Wooden Bucket" or "Ceramic Bucket"; **unverified**: check CurseForge / Modrinth for a 1.19.2 Forge file, its
     config for a fluid allow-list (water only), and that it breaks on hot fluids). Add it to `pack/manifest.json` /
     MODLIST through the usual tooling, then give its recipe planks / logs via `R.replaceInput` if needed.
   - **patcherMod**: a small `bucket/` package with `WoodenBucketItems` (a `DeferredRegister<Item>` like
     `shield/ShieldItems.java`): `wooden_bucket` (empty) and `wooden_water_bucket`, both `BucketItem` subclasses; the
     empty one only picks up water sources (override `use` to refuse other fluids); the full one returns
     `wooden_bucket` on empty. Override `initCapabilities` with a `FluidBucketWrapper` subclass whose
     `setFluid` returns the wooden items, because Forge's wrapper fills to `fluid.getBucket()` (the iron water bucket).
     Models / textures / lang in the jar; recipe in KubeJS (`R.shaped(i.wooden_bucket, ['P P', ' P '], { P: i.planks })`).
3. Writeup: recipes_engineering.md 2.1 (bucket row) and, for the patcherMod route, a short section in the patcherMod
   writeup the item lands in.

### Ids and data
`i.bucket`, `i.any_plate_iron`, `i.planks`. New keys for the wooden bucket (patcherMod route):
`i.wooden_bucket = 'creativeengineering:wooden_bucket'`, `i.wooden_water_bucket = 'creativeengineering:wooden_water_bucket'`
(do not reuse a `wooden_bucket` key name that suggests the Myrtrees block; that one, if ever added, would be
`i.myrtrees_wooden_bucket`).

### Decisions
- **Mod or patcherMod for the wooden bucket?** Recommend a mod if a 1.19.2 Forge build with a fluid allow-list exists
  (no Java to maintain); otherwise patcherMod.
- **Should the wooden water bucket work in recipes that take `minecraft:water_bucket`?** Recommend no at first (Create
  and FD mostly take water as a fluid, which the capability covers); revisit if a crafting recipe is a pain point.

### Gotchas
- Removing `minecraft:bucket` by id leaves other mods' bucket recipes (if any) alive; check JEI after the reload.
- Plates are 1 ingot each in this pack, so the plate bucket costs the same iron as vanilla; it only adds a press step.

### Tests
- **RE-28 Plate bucket:** JEI `minecraft:bucket` → only the plate recipe (and any mod recipe noted); craft one.
- **RE-29 Wooden bucket:** fill from a water source, place it, refill; it refuses lava; a Create spout / FD cooking pot
  accepts water from it.

---

## engineering #6: Create add-on machine recipes on components

Status Planned · Work KubeJS · TODO `recipes/engineering #6` (F11, scratchpad 2026-10-21)

### Goal
The Create add-on machines cost the pack's components, the laser cutter gets its own assembly, and the laser cutter
cuts metal blocks into plates.

### Now
Current recipes (all **verified in jars**; none is touched by the pack except the vibrating table):

| Machine (dict key) | Recipe id | Now |
|---|---|---|
| Mechanical arm (`i.mechanical_arm`) | `create:crafting/kinetics/mechanical_arm` | `LLA / L / IC`: 3 `#forge:plates/brass`, andesite alloy, precision mechanism, brass casing |
| Helve hammer (`i.helve_hammer`) | `vintageimprovements:mechanical_crafting/helve_hammer` | mech. crafting `' B SS' / 'BLLLC' / 'BB  s'`: 5 `#forge:storage_blocks/iron`, 3 logs, 2 `#vintageimprovements:springs/iron`, andesite casing, shaft |
| Centrifuge (`i.centrifuge`) | `vintageimprovements:craft/centrifuge` | `S S / LsL / SCS`: 4 iron springs, 2 logs, shaft, andesite casing |
| Curving press (`i.curving_press`) | `vintageimprovements:craft/curving_press` | ` S  / ICI`: shaft, 2 iron springs, andesite casing |
| Spring coiling machine (`i.spring_coiling_machine`) | `vintageimprovements:craft/spring_coiling_machine` | `I / WAS / I`: 2 iron ingots, coiling wheel, andesite casing, shaft |
| Compressor (`i.vacuum_chamber`; VI's lang names `vintageimprovements:vacuum_chamber` "Compressor") | `vintageimprovements:craft/vacuum_chamber` | `SCS / APA`: 2 iron springs, andesite casing, 2 andesite alloy, mechanical pump |
| Mechanical extruder (`i.mechanical_extruder`) | `create_mechanical_extruder:mechanical_extruder` | `S / GAG / G`: shaft, andesite casing, 4 `#forge:glass` |
| Laser (`i.laser`) | `vintageimprovements:craft/laser` | `CRC / PBS / QLQ`: 2 cogwheels, redstone block, precision mechanism, brass casing, iron spring, 2 quartz, laser item |
| Laser item (`i.laser_item`) | `vintageimprovements:mechanical_crafting/laser` | mech. crafting: redstone dust, 4 copper plates, electron tube, glowstone dust, red glass |
| Rotation speed controller (`i.rotation_speed_controller`) | `create:crafting/kinetics/rotation_speed_controller` | precision mechanism over brass casing |
| Sequenced gearshift (`i.sequenced_gearshift`) | `create:crafting/kinetics/sequenced_gearshift` | shapeless brass casing, cogwheel, electron tube |
| Vibrating table (`i.vibrating_table`) | pack, `process.js` lines 47-48 | 4 stiff springs, sheetmetal slab, mechanical piston, 2 small weak springs, vibration mechanism (VI's own `craft/vibrating_table` removed) |

- Components ([engineering.md](../engineering.md) 2.3): `i.component_zinc` (Zinc Mechanical), `i.component_brass`
  (Brass Mechanical), `i.component_copper` (Copper Mechanical, fluid), `i.component_iron` / `i.component_steel` (IE
  mechanical), `i.component_netherite` (Netherite Thermal), `i.component_diamond` / `i.component_gold` /
  `i.component_electrum` (electrical), `i.component_electronic` (IE). All in the dict (lines 50-1274, 1655).
- The pack's amethyst lens (`i.amethyst_lens`, `creativeengineering:amethyst_lens`) comes from a 5-loop assembly on an
  amethyst block ([crystal.js](../../kubejs/server_scripts/engineering/crystal.js) lines 19-23).
- Laser cutting in the pack: `R.viLaserCutting(outputs, inputs, e, idOverride, energy, maxChargeRate)`
  ([neoCraftHelper.js](../../kubejs/startup_scripts/func/neoCraftHelper.js) lines 1542-1562); bars use 16000 / 800
  (`metal_building.js`).

### Build
One new section per machine in `engineering/process.js` (or a new `engineering/addon_machines.js`; recommend the new
file, process.js is already long). Proposed recipes (the row fixes the components; the shapes are suggestions):

| Machine | Change | Helper |
|---|---|---|
| Mechanical arm | replace the 3 brass plates with `i.component_zinc`, add 2 `i.springs_stiff`: remove + `R.shaped(i.mechanical_arm, ['ZZA', 'Z S', 'PBS'], {Z: component_zinc, A: andesite_alloy, S: springs_stiff, P: precision_mechanism, B: brass_casing})` | `R.remove`, `R.shaped` |
| Helve hammer | 5 iron blocks → 1 `i.block_netherite` + 4 `i.component_iron` (or steel, Decisions) in the same slots | `R.remove`, `R.createMechanicalCrafting` |
| Centrifuge | logs → `i.component_zinc` | `R.replaceInput({id}, '#minecraft:logs', i.component_zinc, e)` |
| Curving press | iron springs → `i.component_zinc` | `R.replaceInput` |
| Spring coiling machine | iron ingots → `i.component_zinc` | `R.replaceInput({id}, '#forge:ingots/iron', i.component_zinc, e)` |
| Compressor | andesite alloy → `i.component_copper` | `R.replaceInput` |
| Mechanical extruder | 2 of the 4 glass → `i.component_netherite` (rebuild: `S / GNG / N`? see Decisions) | `R.remove`, `R.shaped` |
| Laser item ("laser assembly") | replace the mechanical crafting with a sequenced assembly on `i.amethyst_lens`: deploy `i.electron_tube`, deploy `i.plate_copper`, deploy `i.glowstone`, 2 loops (keeps VI's tube, copper and glowstone); plus `R.ieBlueprint(i.laser_item, [i.amethyst_lens, i.electron_tube, Item.of(i.plate_copper, 2), i.glowstone], 'components', e)` | `R.remove`, `R.createSequencedAssembly` (register `['laser_item']` in `transition_items.js`), `R.ieBlueprint` |
| Laser | redstone block → `i.component_diamond`, quartz → `i.component_brass`, keep precision mechanism and the laser item | `R.remove`, `R.shaped` |
| Rotation speed controller | brass craft ×2 with 2 `i.component_brass` (`B / P / C` + components), plus a brassless one: andesite casing + `i.component_steel` + `i.component_gold` + `i.electron_tube`... ×1 | `R.remove`, `R.shaped` ×2 |
| Sequenced gearshift | shapeless brass casing, cog, electron tube, `i.component_zinc`, `i.component_brass` → ×2 | `R.remove`, `R.shapeless` |
| Vibrating table | check only (see Gotchas) | none |
| Laser cutting blocks → plates | for each metal with a block and a plate: `R.viLaserCutting(Item.of(i['plate_' + m], 12), i['block_' + m], e, m + '_plate_laser_cutting', 16000, 800)` | `R.viLaserCutting` in a loop over the metal list of `recipes/metal #1` |

Steps:
1. Register `['laser_item']` in `startup_scripts/changes/transition_items.js` `SEQUENCED_ASSEMBLIES` (texture
   auto-derives from `i.laser_item`; restart needed).
2. Write the section(s); every removal by exact id from the table.
3. Laser-cut plates: loop over `['copper','iron','gold','zinc','aluminum','lead','silver','nickel','uranium','steel',
   'constantan','electrum','brass','cast_iron','netherite']`, skipping keys equal to `i.na` / `i.none`; share the list
   with `recipes/metal #1`.
4. Writeup: a new 2.x "Add-on machines" table in recipes_engineering.md, F11 marked built; tests below.

### Ids and data
All machine keys exist in the dict: `i.mechanical_arm` 4799, `i.rotation_speed_controller` 4906,
`i.sequenced_gearshift` 4917, `i.centrifuge` 5065, `i.curving_press` 5069, `i.helve_hammer` 5071, `i.laser` 5074,
`i.laser_item` 5075, `i.spring_coiling_machine` 5081, `i.vacuum_chamber` 5088, `i.mechanical_extruder` 5281,
`i.vibrating_table` 544. Parts: `i.precision_mechanism` 1553, `i.brass_casing` 535, `i.andesite_casing` 1546,
`i.andesite_alloy` 1545, `i.shaft` 537, `i.cog` 1547, `i.electron_tube` 1551, `i.mechanical_pump` 186,
`i.block_netherite` 1098, `i.springs_stiff` 1773, `i.amethyst_lens` 31, `i.plate_*` / `i.block_*` (lines 828-1265).
In `replaceInput`, match the recipes' tag ingredients: `i.any_log` (`#minecraft:logs`, dict 585) and `i.any_glass`
(`#forge:glass`, 700) exist; `#vintageimprovements:springs/iron` and `#forge:ingots/iron` have no dict key, so add
`i.t_springs_iron_vi = '#vintageimprovements:springs/iron'` and `i.any_ingot_iron = '#forge:ingots/iron'` (next to
`i.any_ingot_gold`, line 1161) rather than writing the literals.

### Decisions
- **Helve hammer "mechanical components": iron or steel?** Recommend `i.component_steel` (the helve hammer is a
  mid-late machine; iron components are early).
- **Extruder: how many netherite components?** Recommend 2 (4 makes it the most expensive machine in the pack); the
  extruder makes blocks from fluids, it shouldn't cost more than the laser.
- **Laser-cut plate yield:** recommend 12 per block (a 1.33× bonus, the IE crusher's ratio) over 9; it is the
  late-game route. Same decision as `recipes/metal #1`.
- **Brassless RSC "complex components":** recommend steel + gold electrical components + an electron tube on an
  andesite casing, ×1 (expensive, no brass).

### Gotchas
- **Vibrating table check:** the pack's table needs a vibration mechanism, which is a deploying-only assembly on an
  echo shard (`mechanisms.js`), and echo shards come from 4 sculk around an amethyst shard (`crystal.js` line 40). The
  redstone mechanism needs a vibrating step; the mechanical crafter needs the redstone mechanism. No loop, but a player
  must find sculk (deep dark) or grow it on liquid XP before the table; note that in the writeup.
- `sequenced_gearshift` and the laser item use electron tubes; `engineering/sawmill-and-components #11` plans an
  electron-tube tag. If that lands first, use the tag key it adds.
- `R.replaceInput` with a tag string must match the recipe's ingredient exactly as a tag; if JEI still shows the old
  input, the recipe used an item list instead (check the jar JSON quoted above).
- Each machine recipe change is visible in Ponder / guide books that still show the old recipe; acceptable.

### Tests
- **RE-30 Add-on machines:** JEI each machine in the table → the new recipe only (two for the RSC); craft one of each.
- **RE-31 Laser assembly:** run the laser item assembly on an amethyst lens; workbench blueprint too.
- **RE-32 Laser plates:** laser cut an iron block → 12 iron plates (or the chosen count); repeat for aluminium.

---

## Built, needs play

None in this subfamily (no Testing / Complete rows).

## Writeup corrections made

- [recipes_engineering.md](../recipes_engineering.md) at-a-glance row and 2.1 table: blast furnace "no recipe" →
  vanilla recipe removed, Quark's deepslate / blackstone furnace recipes still make it.
- [recipes_engineering.md](../recipes_engineering.md) F1: added the 2026-10-21 recheck with the two Quark recipe ids.
- [recipes_engineering.md](../recipes_engineering.md) F5: redstone, glowstone **and gunpowder** lack the single-flour
  filling (was redstone and glowstone); at-a-glance "9 resources" → 10.
