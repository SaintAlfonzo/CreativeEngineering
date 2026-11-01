# blocks / framed-and-decor: implementation brief

Written 2026-10-31 from [TODO](../todo/TODO.md) `## Decor and Framed Blocks` (`framed-and-decor #1`, #2, #3, #7, #8, #10,
#11 Planned; #6 In Progress; #4, #5, #9 Testing). Feature writeups: [flower_pots.md](../flower_pots.md) (test prefix
`POT-`, POT-01 to POT-17), [dyeing.md](../dyeing.md) (`DY-`), [tooltips.md](../tooltips.md) (`TT-`). The decor rows
#3, #7, #8, #10 have no writeup yet: start `decor_blocks.md` with the first one built (prefix `DEC-` proposed). New
tests are placeholders `<PREFIX>-newN` (proposed, numbered when queued).

**Checked 2026-10-31** (jars in `mods/`): FramedBlocks 6.8.6, Copycats+ 2.2.2, Create 0.5.1.i, Create Framed 1.4.5.1,
Fairy Lights 6.0.1, Decorative Blocks 3.0.0, Redeco 1.5.0, Another Furniture 2.1.4, Every Compat 2.5.36, Quark 3.4-418;
the Vinery port in `development/creativeBrewing/members/vinery`. Bytecode read with `javap` where the brief says so.

## Scope and order

| Step | Row | Work | Notes |
|---|---|---|---|
| 1 | `framed-and-decor #6` fixes D3, D6 | KubeJS tags | Smallest open fixes of the audit; the rest wait on owner decisions |
| 2 | `framed-and-decor #2` Copycat pack names | resource pack edit | Client only, no code |
| 3 | `framed-and-decor #1` Flowers on framed / copycat dirt | patcherMod | One mixin per mod family, reuses `CamoSpeed.camo` |
| 4 | `framed-and-decor #8`, `#10` vines | patcherMod + tags | Same `VineBlock` neighbour hook; build together |
| 5 | `framed-and-decor #7` Fairy lights | patcherMod + tags | Independent |
| 6 | `framed-and-decor #11` Random flower offset | patcherMod | Needs a dump first |
| 7 | `framed-and-decor #3` Trapdoor / shutter variants | KubeJS / data | Waits on "which" |

All patcherMod rows can go in one build (patcherMod is at 1.1.04 with 2026-10-31 work not installed yet, flower_pots.md 0).

## Built, needs play

- `framed-and-decor #4` Canvas sign dyeing: `SupplementariesDyeBlocksMixin` skips blocks with a block entity
  (patcherMod 1.1.01, shipped 2026-10-22); [dyeing.md](../dyeing.md) 2.1 D1 / D4 and DY-04. Tiers S C P open.
- `framed-and-decor #5` Botanist workbench as a flower pot (built 2026-10-29, patcherMod 1.1.04);
  [flower_pots.md](../flower_pots.md) 3.1; POT-08, SQ-POT-06 (SQ-POT-06b passed r1031), CQ-POT-02. Tiers S C P open
  (S partly).
- `framed-and-decor #9` Design Decor golden apple craft removed (built 2026-10-23, shipped 1.1.03;
  `kubejs/server_scripts/other/misc_mods.js:58`); no writeup, fold into [recipes_cooking.md](../recipes_cooking.md)
  (golden apples, 2.x / F7). SQ-LE-02 PASSED (2026-10-25 late run: "no Design Decor golden apple"), so tier S looks done;
  P open.

---

## framed-and-decor #1: Flowers on framed / copycat dirt
Status **Planned** · Work: patcherMod · id `blocks/framed-and-decor #1`

### Goal
A flower (and other plants that take dirt or grass) can be planted on a FramedBlocks block, a Create copycat or a
Copycats+ copycat whose camo on the top face is dirt, grass or another soil.

### Now
- Neither mod lets a camo sustain plants: no `canSustainPlant` in any FramedBlocks 6.8.6 class nor in Copycats+ 2.2.2 /
  Create's `CopycatBlock` (checked by grepping the extracted classes). Forge's default `IForgeBlock.canSustainPlant`
  looks at the framed block itself, which is not in `#minecraft:dirt`, so `BushBlock.canSurvive` fails.
- Camo lookup already exists in patcherMod:
  [CamoSpeed.camo(BlockEntity)](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/surface/CamoSpeed.java#L84-L104)
  (FramedBlocks via `FramedCamo`, Copycats+ via `CopycatsPlusCamo`, Create's `CopycatBlockEntity.getMaterial()`).
  FramedBlocks' `FramedBlockEntity.getCamo(Direction)` gives the camo of one face (double blocks).
- Related mixins: `FramedDoubleCamoOrderMixin`, `CopycatMaterialOrderMixin` (same targets, remap = false, SRG names).

### Build
1. Make `CamoSpeed.camo` public, or move it to a small `compat/CamoLookup` with a `camoFacing(be, Direction.UP)` variant
   (FramedBlocks: `getCamo(Direction.UP).getState()`; copycats: the material).
2. Mixins adding `canSustainPlant(BlockState, BlockGetter, BlockPos, Direction, IPlantable)` (Forge method, not
   obfuscated) to `xfacthd.framedblocks.api.block.AbstractFramedBlock`, `com.simibubi.create.content.decoration.copycat.CopycatBlock`
   and Copycats+' base (`CCCopycatBlock` / `MultiStateCopycatBlock`): when `facing == UP`, the top of the block is a
   full face (`state.isFaceSturdy(level, pos, UP)`), and the camo state is in `#creativeengineering:plant_soil_camo`
   (D2), return `camo.canSustainPlant(level, pos, UP, plantable)`; else the Forge default. The method is added by the
   mixin (none of the three targets defines it; mixins may add an override of an interface default).
3. Tag `#creativeengineering:plant_soil_camo` (blocktags.js via `global.tags`): `#minecraft:dirt`, farmland? (D3),
   sand for cacti / sugar cane (D2).
4. Config switch `[blocks] camoSoil = true` in CEConfig (and the shipped toml after the first run).

### Ids and data
Verified: class names and method lists above (javap). New: one tag, one config key.

### Decisions
- **D1 Which blocks.** Recommend every framed / copycat block with a full top face (cube, slab top, stairs top...).
- **D2 Which soils.** Recommend dirt family (flowers, saplings, grass) only; sand and farmland later if asked.
- **D3 Farmland.** Recommend no (a framed farmland can't be tilled or hydrated).

### Gotchas
- A plant placed on a camo dirt must pop off when the camo is removed or changed: neighbour updates fire on camo change
  in FramedBlocks (it calls `level.sendBlockUpdated`, not necessarily `updateNeighbors`); check in game.
- BlockRunner and the sinking render also read camos: no conflict, but keep one lookup.

### Tests
(proposed, numbered when queued)
- POT-new1 framed cube with a grass camo: plant a poppy and an oak sapling on top; remove the camo: the plant breaks.
  Same on a Create copycat panel (flat) and a Copycats+ copycat block with dirt.

---

## framed-and-decor #2: Copycat resource pack keeps the framed block name
Status **Planned** · Work: resource pack · id `blocks/framed-and-decor #2`

### Goal
`resourcepacks/Copycat Framed Block.zip` retextures framed blocks like Create copycats but keeps FramedBlocks' own block
names. The pack is off by default on player clients.

### Now
- The zip (pack_format 8, "Turns the framed blocks to Create's Copycat block!") holds textures and
  `assets/framedblocks/lang/en_us.json` (~300 keys), edited 2026-09-27 to "Framed Copycat ..." (tooltips.md "Renamed",
  lines 104-109); backup in `configs_edited/resourcepacks/Copycat Framed Block.zip` and `.../original/`.
- Shipped to clients as a zip by `build_deploy.py` (resourcepacks → client); players' `options.txt` is not shipped, so
  it is off for them by default. On the dev client it is enabled (`options.txt:40`; listed as incompatible at line 41
  because of the old pack format).
- So players see "Framed Slab", the dev client "Framed Copycat Slab".

### Build
1. Remove `assets/framedblocks/lang/en_us.json` from the zip (names fall back to FramedBlocks'); keep the textures.
2. Copy the new zip to `configs_edited/resourcepacks/` (keep `original/`).
3. tooltips.md "Renamed": replace the "Framed Copycat" bullet with "the pack only retextures; names stay FramedBlocks'".
4. Changelog: none if players never turn it on; one bullet under a catch-all if it is mentioned to players (D1).

### Decisions
- **D1 Keep the gate-door names** the 2026-09-27 edit added? Those keys exist in FramedBlocks' own lang (check
  `unzip -p mods/FramedBlocks-6.8.6.jar assets/framedblocks/lang/en_us.json`); if present, nothing is lost.
  Recommend dropping the whole lang file.

### Tests
(proposed, numbered when queued)
- TT-new1 with the pack on: a framed slab reads "Framed Slab" and looks like a copycat; Create's copycat slab is the only "Copycat Slab".

---

## framed-and-decor #3: Trapdoor and Another Furniture shutter variants
Status **Planned** · Work: KubeJS / data · id `blocks/framed-and-decor #3`

### Goal
More shapes and wood variants of trapdoors and Another Furniture shutters.

### Now (display-names dump, 2026-10-20)
- Shutters: Another Furniture's 9 vanilla woods plus 15 Every Compat ones (`everycomp:af/<mod>/<wood>_shutter`:
  twisted, dreamwood, livingwood, rose, rubber, smoked, spirit, rubberwood, ancient, azalea, bamboo, blossom, driftwood,
  river, cherry). Woods with planks but no shutter: Immersive Weathering charred, Engineer's Decor old industrial wood,
  Interiors seatwood (not wood types Every Compat knows).
- Wood trapdoors missing: Botania dreamwood and livingwood, Myrtrees rubberwood, IW charred (every other modded wood has
  one). Non-wood trapdoors exist from Dave's Building (18), Create Framed (17), Blocky Siege (11), Everything Copper,
  Dustrial Decor, IW, Create Deco, FramedBlocks, Copycats+ (`copycat_trapdoor`, `copycat_iron_trapdoor`).

### Build (once D1 is answered)
1. Wood gaps: KubeJS `StartupEvents.registry('block')` trapdoors are not a KubeJS 1902 block type; register them in
   patcherMod (`TrapDoorBlock` with the wood's sound and `BlockSetType`-less 1.19.2 ctor), items, models from the
   planks texture, loot, `#minecraft:wooden_trapdoors`, recipes `R.shaped(Item.of(id, 2), ['ppp','ppp'], { p: planks })`.
2. Shutters for the three missing woods: Another Furniture's `ShutterBlock` registered by patcherMod under
   `creativeengineering:<wood>_shutter` with AF's model parents, tag `#another_furniture:shutters`, AF-style recipe.
3. Shapes (D2): only after the owner names them.

### Decisions
- **D1 Which.** Recommend the 4 missing wood trapdoors and the 3 missing shutters; nothing else unless named.
- **D2 "Shapes".** Unclear: half-height shutters? glass trapdoors? Ask; recommend none until named.

### Gotchas
- Every Compat already makes AF shutters for modded woods; adding one by hand for a wood it later supports would
  duplicate it. Check Every Compat's config before registering.

### Tests
(proposed, numbered when queued)
- DEC-new1 craft a dreamwood trapdoor and a charred shutter; both open, sound like wood and burn in a furnace.

---

## framed-and-decor #6: Flower potting audit
Status **In Progress** · Work: data / KubeJS · id `blocks/framed-and-decor #6`

### Goal
Know which plants can be potted (done), then: a tooltip label on every flower pot, every pot distinct, pots hold every
flower, and fix the audit's bugs.

### Now
- Audit DONE 2026-10-27 ([flower_pots.md](../flower_pots.md) 0-3, matrix [flower_pots_matrix.md](../flower_pots_matrix.md),
  `/dumppots`, `tools/flower_pot_audit.py`); SQ-POT-01 to -04 ran; SQ-POT-05 (read the 427 mismatches) and CQ-POT-01 open.
- Since then (not in the TODO row's text): flower pot tweaks **built 2026-10-31** (flower_pots.md 3.2: framed pot
  before framing, box plant models, plant lists in `misctags.js:47-56`, Rich Hanging Pot, Large Flower Box, WTHIT
  "Contains", silt pot as soil, Vinery box centred), patcherMod 1.1.04 + Vinery port `-07`, not installed;
  POT-09 to POT-17, SQ-POT-07, SQ-POT-08. Floral large pots hidden 2026-10-31 (3.2.9; D4 decided, replacement in
  `potions/enchanting #7`).
- Bugs still open: F1 (planter box `ClassCastException` on 4 Fungi Delight items: nothing in
  `#another_furniture:planter_box_banned` but `not_in_flower_boxes`, misctags.js:48), F2 (Vinery potted saplings), F4
  (soul planter), F6 (buttercup tag), F3 / F5 / F7 / F11 (gaps).

### Build (by decision, flower_pots.md 4)
1. **D6 (F1):** add `i.cleaned_cap`, `i.cooked_cleaned_cap`, `i.truffle`, `i.truffle_slice` (Fungi Delight dict keys)
   to `another_furniture:planter_box_banned` in misctags.js:48 (`t.add`). Recommended: the tag, not `#forge:mushrooms`.
   Fungi Delight is a CD member: put the line in CD `fungidelight/` tags instead (food routing) if it reads better.
2. **D3:** `t.add(e, 'minecraft:small_flowers', i.buttercup)` (block and item tags). Recommend yes.
3. **F2:** in the Vinery port (now `development/creativeBrewing/members/vinery`, `ObjectRegistry.java`), call
   `((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(sapling id, potted block)` at common setup.
4. **D5:** recommend drop the soul planter (no Nether's Delight soul soil route worth a mixin).
5. **D2 labels:** one tooltip line per holder in `kubejs/client_scripts/tooltips.js` "Flower Pots" (wording list in
   flower_pots.md 4 D2).
6. **D1:** recommend option (b), small flowers and saplings first (Botania mystical / shiny, wild crops): a patcherMod
   or CD registration of `potted_*` blocks with generated models. Large; split into its own row when decided.
7. Rerun `/dumppots` + `tools/flower_pot_audit.py`; flower_pots.md findings marked fixed.

### Decisions
Open with the owner: D1, D2, D3, D5, D6 (flower_pots.md 4). D4 is decided (Floral pots hidden, `potions/enchanting #7`).

### Gotchas
- The planter box tag is read when the box is clicked: a `/reload` is enough for the F1 fix.
- `#minecraft:small_flowers` feeds bee breeding and worldgen tags too (D3 consequence).

### Tests
Existing: POT-01 to POT-17 (flower_pots.md 7), SQ-POT-05, -07, -08, CQ-POT-01. POT-04 covers the F1 fix.

---

## framed-and-decor #7: Fairy lights on non-full blocks
Status **Planned** · Work: patcherMod · id `blocks/framed-and-decor #7`

### Goal
Fairy light fasteners attach to some non-full blocks (fence tops and similar).

### Now (Fairy Lights 6.0.1, javap)
- `FastenerBlock.canSurvive` (`m_7898_`): the block behind must be in `#minecraft:leaves` or have a sturdy face there
  (`isFaceSturdy`). `ConnectionItem.useOn` checks `canSurvive` before placing.
- Fences use a separate fastener entity: `ConnectionItem.isFence` = solid material and `#minecraft:fences`. So vanilla
  and tagged modded fences already work; walls, posts, chains, bars, lattices, stacked logs and fence-like modded blocks
  outside `#minecraft:fences` do not.

### Build
1. Tag `#creativeengineering:fairy_light_posts` (blocktags.js, `global.tags`): `#minecraft:walls`, Supplementaries
   posts, Decorative Blocks / Redeco lattices, `vinery:stackable_log`, chains, iron bars (D1).
2. Mixin on `me.paulf.fairylights.server.item.ConnectionItem.isFence` (static, remap = false): also true for the tag
   (uses the fence-fastener entity, which hangs from the block centre: good for posts and walls).
3. Optional (D2): mixin on `FastenerBlock.canSurvive` to accept `#creativeengineering:fairy_light_faces` for face
   fasteners on non-sturdy faces (top of a slab, a log pile).
4. Guard with `ModList.isLoaded("fairylights")` through the mixin plugin (`CEMixinPlugin`).

### Decisions
- **D1 Which blocks.** Recommend walls, posts, chains, bars, lattices, stacked logs.
- **D2 Face fasteners too?** Recommend no at first: the fence-entity route covers "fence tops and similar".

### Gotchas
- `FenceFastenerEntity` survives only while the block is still a "fence": its own check probably calls `isFence`
  again; the mixin on `isFence` covers both if it is the shared static (verify).

### Tests
(proposed, numbered when queued)
- DEC-new1 connect fairy lights between two cobblestone walls and a lattice; break a wall: the light drops.

---

## framed-and-decor #8: Vines under non-full blocks
Status **Planned** · Work: patcherMod · id `blocks/framed-and-decor #8`

### Goal
Vines can grow on and under non-full blocks such as stacked logs.

### Now
- Vanilla `VineBlock` attaches only to a full face (`isAcceptableNeighbour` → `Block.isFaceFull(collisionShape, dir)`),
  for placing, surviving and spreading.
- `vinery:stackable_log` (Vinery port `StackableLogBlock extends SlabBlock`,
  [StackableLogBlock.java:245-257](../../development/creativeBrewing/members/vinery/src/main/java/net/satisfy/vinery/core/block/StackableLogBlock.java#L245-L257))
  is made of four log boxes: no full face anywhere, so no vine.
- patcherMod already mixes into `VineBlock` ([VineBlockShearMixin.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/VineBlockShearMixin.java),
  sheared vines stop spreading; modded vines subclassing `VineBlock` included).

### Build
1. Tag `#creativeengineering:vine_supports` (blocktags.js): `vinery:stackable_log`, Dave's log piles, lattices (with #10),
   fences? (D1).
2. Mixin on `VineBlock.isAcceptableNeighbour` (static, SRG name from the mappings: look it up in a `mappings.tsrg`
   under `development/*/build/reobfJar/`): return true when the neighbour is in the tag. This covers placing, survival
   and spreading for vanilla and subclassing modded vines.
3. Other vine-like blocks not subclassing `VineBlock` (Quark / IW ivy?) are #10's job.

### Decisions
- **D1 Which blocks.** Recommend stacked logs, log piles, lattices; not fences (vines on fences look floating).

### Tests
(proposed, numbered when queued)
- DEC-new1 a vine placed under and beside a stackable log stays, and spreads down over time.

---

## framed-and-decor #10: All vines in lattices
Status **Planned** · Work: patcherMod / data · id `blocks/framed-and-decor #10`

### Goal
Every vine type can grow on or through lattices. Related: #8 and the Decorative Blocks lattice overlap.

### Now
- Lattices in the pack: `decorative_blocks:lattice` (trapdoor-like states: facing, half, open), Redeco
  `redeco:<wood>_lattice` x9 (state `leaves` 0..2: plain, azalea, flowering azalea, set by Redeco's `LatticeBlock`),
  `framedblocks:framed_lattice_block`, `quark:mud_brick_lattice`, `vinery:grapevine_lattice` (a grape stem, not a
  vine holder).
- No lattice holds vines today; Redeco's takes azalea leaves only.

### Build
1. Through #8's tag: add the lattices to `#creativeengineering:vine_supports`, so vines attach beside and under them.
2. "Through" (D1): a new `leaves`-style state for vines would need Redeco / Decorative Blocks mixins and models per
   lattice; only if D1 asks for it.
3. Modded vines (D2): list the live vine blocks (`#minecraft:climbable` minus ladders, scaffolding) from the tag dump and
   check which subclass `VineBlock`.

### Decisions
- **D1 On or through.** Recommend "on" (attach to the lattice face) first; "through" (vine inside the lattice block) is a
  per-mod model job.
- **D2 Which vines.** Recommend all `VineBlock` subclasses (free with #8) and the cave / weeping / twisting vines only if
  asked.

### Tests
(proposed, numbered when queued)
- DEC-new1 vines on each lattice type (Decorative Blocks, Redeco, framed lattice) stay and spread.

---

## framed-and-decor #11: Random flower block position
Status **Planned** · Work: patcherMod · id `blocks/framed-and-decor #11`

### Goal
Flower blocks that sit dead centre get the random XZ offset vanilla flowers have; the trowel may change it.

### Now
- Vanilla flowers use offset type XZ (1.19.2: `Block.getOffsetType()`, applied by `BlockState.getOffset` in rendering and
  in the flower's own shape). Which modded flowers lack it is not known: no dump records offset types (unverified).
- Quark's `quark:trowel` is the only trowel in the pack (places a random hotbar block; no offset feature).

### Build
1. Dump: add `offsetType` per plant to `/dumppots`' plant list (`kubejs/server_scripts/_lib/dump_flower_pots.js`), or a
   one-off probe; list `#minecraft:flowers` / `#minecraft:small_flowers` blocks with offset NONE.
2. Tag `#creativeengineering:random_offset_plants` from that list.
3. Mixin on `BlockBehaviour.getOffsetType()` (or `BlockStateBase.getOffset`) returning XZ for the tag; check each
   block's `getShape` uses the offset (vanilla `FlowerBlock` does; a modded one that doesn't would have its outline in
   the centre: list them as gotchas or skip them).
4. Trowel (D1).

### Decisions
- **D1 Trowel.** Recommend no trowel change: vanilla offsets come from the position hash, so a "change" would mean a
  stored state. Skip unless the owner wants a fixed position tool.

### Gotchas
- Offset is client render + server shape: changing it only client-side desyncs hitboxes. The mixin must run on both sides.

### Tests
(proposed, numbered when queued)
- POT-new1 a row of the listed flowers: each sits off-centre like a poppy, outline matches the model.
