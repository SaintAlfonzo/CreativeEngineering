# Flesh recipes plan (2026-11-10)

Status: BUILT 2026-11-10, jars NOT installed (game open); tests SQ-FLR-01 to -06, CQ-FLR-01 to -03 (visceral_heap.md VH-16 to
VH-21, engineering.md EN-22 to EN-24). Build notes in section 8. Source: scratchpad "To Execute", Biomes O' Plenty flesh recipes. Builds on
[plan_visceral_heap.md](plan_visceral_heap.md) and [visceral_heap.md](visceral_heap.md).

Goal: every Visceral Heap block has a use, flesh and porous flesh are crushed into drops, and blood and flesh can be
cultivated without a free loop.

## 1. Audit (2026-11-10)

BOP ships no recipes for any Heap block; the pack has none either.

| Block | Obtained | Use today | After this plan |
|---|---|---|---|
| Flesh | mined (drops itself) | Botany Pots soil, axe-mineable, spongy in Block Runner | crush, blood, cultivation |
| Porous flesh | mined (drops itself) | Botany Pots soil, very sticky in Block Runner | crush, blood, cultivation |
| Flesh tendons, strand | shears or silk touch; grow like vines (growing-plant head block); Botany Pots crop | none | climbable, blood, porous cultivation |
| Hair | shears; Botany Pots crop | none | crush to string |
| Pus bubble | silk touch only | none | crush to slimeball |
| Eyebulb | silk touch only | none | eye drop of both crushes |
| Blood | fluid and bucket | none | cultivation and mixing |

Tendons already grow, so no growth mechanic is added. Porous flesh does not grow tendons.

## 2. Decisions (owner, 2026-11-10)

1. Tendons and the strand block are climbable (plain ladder speed).
2. Eyebulb is in the eye drop pool of both crushes.
3. Porous flesh drops 1.5 meat and 1 eye (misc stays 0.5, no bone).
4. Crushing a pus bubble gives a slimeball at 25%; crushing hair gives string at 50%. Nothing drops on a failed roll.
5. Porous flesh cultivation takes 250 mb blood (was 1000 mb).
6. Cultivation oil is any `#forge:plantoil` fluid (IE plant oil, Create Crafts & Additions seed oil, PneumaticCraft vegetable oil).
7. Pus bubble and hair do not join the compost waste tag. No mash, paste or organic waste items, ever.
8. Crushing uses crushing wheels plus a millstone mirror.
9. The eyebulb item keeps BOP's placement rule (tested later).
10. Verify the drop item ids (rotten ham, bone chunk, broken bone) and the Botany Pots soil data in build step 1.

## 3. Drop pools

A crush rolls 4 pools (meat, bone, eye, misc). Each pool's number is its **amount per crush**: the whole part is that many
guaranteed picks, the fraction is the chance of one more pick. Each pick takes exactly one item from that pool, by weight.
So flesh gives 1 meat item always, a bone item 75% of the time, an eye item 25% and a misc item 25%; porous flesh gives 1
meat item always plus a second 50% of the time, 1 eye item always, and a misc item 50% of the time.

The per-item numbers below are overall chances per crush (pool amount times the item's weight), so they sum to the pool
amount and are what JEI shows. Rare items get small weights. Item ids come from the dicts (section 5).

**Create cannot do this today.** Create rolls every output independently, so a pool would give none or several items. The
pack's `exclusive_results` flag (creativeEngineering `fan/ExclusiveResults`, applied to any Create processing recipe,
crushing and milling included) gives one pick per recipe, but only one group. Build step 3 patches Create to support several pools, including the JEI pages.

| Pool | Flesh | Porous flesh | Items (flesh chance / porous chance) |
|---|---|---|---|
| meat | 1.0 | 1.5 | rotten flesh .35/.525, rotten ham .20/.30, leather .15/.225, spider leg .10/.15, hoglin ear .10/.15, hoglin loin .10/.15 |
| bone | 0.75 | 0 | bone .30, bone meal .25, broken bone .10, bone chunk .08, bone serpent tooth .02 |
| eye | 0.25 | 1.0 | spider eye .12/.50, eyebulb .08/.30, fermented spider eye .05/.20 |
| misc | 0.25 | 0.5 | pus bubble .08/.17, flesh tendons .09/.18, hair .08/.15 |

Value check: items per crush are 2.25 (flesh) and 3.0 (porous). Weighting eye as 2 and the rest as 1 gives 2.5 and 4.0, so
porous flesh is worth about 1.6 times flesh. Porous costs about 5 flesh to make (section 4), so crushing it is never a gain.

## 4. Recipes

Crush (Create crushing wheels; Create millstone mirror if the helper allows):

- Flesh and porous flesh into the pools in section 3.
- Pus bubble into a slimeball at 25%.
- Hair into string at 50%.

Cultivation:

- 2 porous flesh + 2 flesh + 1000 mb `#forge:plantoil` = 2 porous flesh + 3 flesh.
- 250 mb blood + 2 flesh tendons + 1 flesh = 1 porous flesh.

Blood (heated mixing):

- flesh: 0.25 bone and 250 mb blood.
- porous flesh: 500 mb blood.
- flesh tendons: 250 mb blood.

Costs, with 250 mb blood = 1 flesh of blood (a heated flesh or tendon returns 250 mb):

| Item | Cost in flesh-equivalents | Returns when heated | Returns when crushed |
|---|---|---|---|
| Flesh | 1 (reference) | 250 mb = 1 | 2.25 items |
| Flesh tendon | about 1 (heats to 250 mb; grows and shears free) | 250 mb = 1 | n/a |
| Porous flesh | 1 flesh + 250 mb + 2 tendons = 4 (was 8) | 500 mb = 2 | 3.0 items (4.0 rarity-weighted) |

- **No loop:** making a porous flesh costs 4 equivalents and heating it returns 2, so blood always falls. Crushing 4 flesh
  gives 9 items against 3 from the porous flesh.
- **Porous value:** worth about 1.6 times flesh by drops but costs 4 times as much, so it is a premium block, not a bulk
  one. In practice tendons are renewable (vines, pots, shears), so the real cost is 1 flesh and about 3 tendons.
- **The flesh farm is the lever:** the 2 porous flesh in it are a catalyst and are not consumed, so 1000 mb plant oil
  nets +1 flesh (2.25 items, 250 mb of blood). IE squeezes about 120 mb from one seed, so about 8 seeds make 1 flesh. That
  is cheap meat (and leather, hoglin loin, rotten ham). The yield was cut from 4 to 3 flesh on 2026-11-10; adjust the
  oil amount after scratch testing.

## 5. Build steps

1. Common dicts (`item_dict.js`, `block_dict.js`, `fluid_dict.js`): add porous flesh item, pus bubble, hair, eyebulb,
   flesh tendons item, blood and blood bucket, and any drop item missing (bone chunk, broken bone, rotten ham). Rebuild and
   install Creative Common.
2. Climbable: add `minecraft:climbable` for flesh tendons and strand through `global.tags` (Heap tag script,
   `kubejs/data/creativeworldgen/kubejs/tags/visceral_heap.js`). Check the villager ladder tag is not wanted.
3. **Patch Create for pools** (creativeEngineering, extends `fan/ExclusiveResults`; Create 0.5.1 facts read from the jar):
   - **Recipe data:** a `result_pools` key, a list of lists of output indices. Outputs in no pool keep Create's independent
     roll, so the pus bubble (25%) and hair (50%) crushes are plain chance outputs.
   - **Roll:** per processed item and per pool, `floor(sum of chances)` picks plus one more at the fractional chance, each
     pick one item weighted by its chance. `rollResults` is already patched by `ProcessingRecipeMixin`; the crushing wheels and
     the millstone both call it.
   - **Parse and sync:** read the key in `ProcessingRecipeSerializerMixin` (JSON) and also write and read it in the network
     path. The existing exclusive flag is server-only because JEI only needed the chances, but JEI now needs the pools on the
     client.
   - **Output limit:** crushing allows 7 outputs and a flesh crush has 17 distinct items. Create only warns on `validate`, but
     patch `getMaxOutputCount` (crushing and milling) up to 24 so the log stays clean. Check milling's own limit first.
   - **JEI (Create's `CrushingCategory` and `MillingCategory`):** today `setRecipe` makes one slot per output, centred in a
     row. Patch it so each pool is one slot holding all its items (JEI cycles a slot's list, so the pool rotates through its
     items like a tag ingredient), and each non-pool output keeps its own slot. Slots are ordered meat, bone, eye, misc, then
     the rest. The tooltip on the shown item keeps Create's chance line (its overall chance per crush, as listed in section
     3) and adds the pool's amount ("1 pick from this group", "75% chance of 1 pick"). Because the slot holds every item,
     looking up any item in JEI still finds the recipe. Gate the mixin on JEI and Create being loaded, client only.
   - **Helper:** NCH option `{ pools: [[0,1,2],[3,4]] }` on `createCrushing` and `createMilling` writes the key.
   - Needs a creativeEngineering rebuild and restart.
4. Add `f.t_plantoil = '#forge:plantoil'` to `fluid_dict.js`. NCH accepts fluid tags but warns that it does not validate them. Helpers: confirm `createCrushing` takes chance outputs and `createMixing` takes heat plus fluid in and out; extend NCH if
   not. No hand-written events.
5. Recipe script under `kubejs/data/creativeworldgen/kubejs/` with `// requires: biomesoplenty`, using NCH.
6. Docs: update [visceral_heap.md](visceral_heap.md), the README index and TODO, add a changelog bullet under a Visceral
   Heap family and run `python3 tools/changelog_lint.py`, add scratch queue rows.

## 6. Deferred: item physics for the Heap items

Owner 2026-11-10: leave this until item physics fluids are implemented. Nothing in this plan depends on it.

Item physics decides whether a dropped item floats or sinks in water and burns or survives in fire and lava
([item_physics.md](item_physics.md)). Today 89% of modded items are in no tag and sink and burn. The proposed 21 families
(section 9 there) are written down but not applied. The Heap items are not in the families file at all, so they currently
fall to the default.

What it changes in this plan: crushing happens at a wheel, so the drops land beside it, usually not in a fluid. It matters
for (a) items dropped near lava in the Nether, where everything burns, and (b) items dropped in the blood lakes (blood is a
fluid, so floating or sinking applies there).

Options for the Heap items (flesh, porous flesh, tendons, hair, pus bubble, eyebulb, and the drops):

| Option | Effect | Cost |
|---|---|---|
| A. Do nothing (default) | all sink and burn; when the families ship they become `organic` (float, burn) | none now |
| B. Put them in the `organic` family only | float in water and blood, burn in fire and lava | needs the families to be built first |
| C. Fireproof the Heap items | flesh, hair and bubbles survive lava, so a farm next to lava is safe | thematically odd; one tag line, `/reload` |

Default is A until item physics fluids are implemented; then pick again.

## 7. Tests (to add to the scratch queue)

- Both tendon blocks climb up and down; a player can stand on a strand.
- Crush 200 flesh and 200 porous flesh: per crush, meat is always 1 item (flesh) or 1 to 2 items (porous), never more than one
  item per pick, and each pool total and item split matches section 3 within sampling error.
- JEI crushing page for flesh: 4 slots (meat, bone, eye, misc), each cycling through its items, tooltip chance per item matches
  section 3; the pus bubble and hair pages are unchanged; searching any pool item (for example hoglin ear) finds the recipe.
- A recipe with no `result_pools` still rolls independently (existing sanding and fan recipes unchanged).
- Crush pus bubbles and hair: about 25% slimeball and 50% string.
- Both cultivation recipes and the three blood mixes run once and give the stated amounts.
- Eyebulb drops and can be placed as the drop item.

## 8. Build notes (2026-11-10)

- Ids checked (step 1): rotten ham is Hostile Delight's `hostiledelight:rottenham` (already `i.rottenham`); bone chunk and
  broken bone are Philip's Ruins (`philipsruins:`). The Botany Pots soil data was not rechecked.
- Fluid tag input (step 4): kubejs-create reads an object with a `fluidTag` key as Create's tag ingredient. New
  `NCH.createFluidTag(f.t_plantoil, 1000)` builds it; `_resolveFluidAmount` reads the key, so NCH checks it like any tag.
- Create pools (step 3) as planned, plus two findings: the millstone limit is 4 (raised to 24 with crushing), and the crushing
  wheel controller copies a whole stack's merged results into 15 output slots and drops the rest, so a 64 stack of flesh
  (17 possible items) could lose stacks. `ProcessingInventoryMixin` grows Create's `ProcessingInventory` to 32 slots
  (crushing wheel and saw), also for blocks saved before. Pools travel in the recipe sync packet after Create's data in the
  final `toNetwork` / `fromNetwork`. Details: [engineering.md](engineering.md) "Result pools".
- Cultivation is unheated mixing (the plan names heat only for the blood recipes).
- Villager ladder tag not added: villagers keep off the Heap.
