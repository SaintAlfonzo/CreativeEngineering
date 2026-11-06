# Placed treated sticks

Immersive Engineering's Treated Stick (`immersiveengineering:stick_treated`) can be placed like Supplementaries'
sticks, and Farmer's Delight tomatoes climb it like they climb Supplementaries' sticks. Built 2026-10-31 in Creative
Delight (CD); test prefix `TSK-`.

## 1. Behaviour

- Right-click a block face with a treated stick (after the clicked block's own use passed, the same order as
  Supplementaries' sticks): places `creativedelight:treated_stick` on that face's axis. Clicking a placed treated
  stick on a new axis adds a crossing stick in the same block (up to three: x, y, z). Sneak places beside it instead.
  Waterloggable, flammable like a placed stick (60/60, none while waterlogged), mined instantly, `mineable/axe`.
- Breaking drops one treated stick per axis; pick-block gives a treated stick. The block has no item of its own.
- Tomatoes:
  - Right-click a bottom FD tomato vine (not on a rope) with a treated stick: it becomes
    `creativedelight:treated_stick_tomatoes` at the same age (Supplementaries' stick behaviour).
  - A tomato vine below a placed treated stick climbs onto it (30% per growth tick or bone meal, at most three vines
    high, only with FD's `enableTomatoVineClimbingTaggedRopes` on), from FD's vine, Supplementaries' stick/rope vines,
    or a treated-stick vine.
  - A vine on a treated stick climbs on into a treated stick, a Supplementaries stick or rope, or any other
    `farmersdelight:ropes` block above.
  - The vine is FD's: same growth, right-click picking and loot (FD's tomato loot table). Since 2026-11-02 it is an
    own-pick crop like every tomato vine: Quark no longer replants it, an area hoe picks it and climbs the column
    ([crops.md](crops.md) 2.3, `farming/crops #29`, CR-101, CR-102). Breaking the
    vine, or the vine dying (no vine or soil below, too dark), leaves the treated stick. A treated-stick vine takes
    crossing sticks on x and z like Supplementaries' stick vines.

## 2. Code and assets

| What | Where |
|---|---|
| Registry, placement and climb helpers | `development/creativeDelight/src/main/java/com/SaintAlfonzo/creativedelight/treatedstick/TreatedSticks.java` |
| Placed stick block (own code; Supplementaries is ARR) | `.../treatedstick/TreatedStickBlock.java` |
| Vine on a treated stick (extends FD `TomatoVineBlock`) | `.../treatedstick/TreatedStickTomatoBlock.java` |
| Placement hook: `Item#useOn` for the IE item, compartment `immersiveengineering` | `.../mixin/immersiveengineering/TreatedStickPlaceMixin.java` |
| Climb hook on FD's vine | `.../mixin/farmersdelight/TomatoClimbTreatedStickMixin.java` |
| Climb hook on Supplementaries' stick/rope vines (`@Pseudo`, `require = 0`) | `.../mixin/supplementaries/SuppTomatoClimbTreatedStickMixin.java` |
| Blockstates, models (parents are Supplementaries' stick and `crop_with_rope` models; vine stages use FD's textures) | CD `src/main/resources/assets/creativedelight/blockstates/treated_stick*.json`, `models/block/treated_stick*.json` |
| Textures (pack level, see 3) | `kubejs/assets/creativedelight/textures/block/treated_stick.png`, `treated_stick_cross.png` |
| Lang (`Treated Stick`, `Tomatoes`) | CD `assets/creativedelight/lang/en_us.json` |
| Axe tag | CD `src/main/resources/data/minecraft/tags/blocks/mineable/axe.json` |
| Dict keys `b.treated_stick`, `b.treated_stick_tomatoes` | `kubejs/startup_scripts/_lib/block_dict.js` |

The blocks are the first registry content of the `creativedelight` mod id itself (not a member): the feature
joins IE, Supplementaries and FD, so no member namespace fits.

## 3. Decisions

- **Own blocks, not Supplementaries' API.** Supplementaries (ARR) registers its own placeable sticks through
  internals; the pack re-implements the stick and stick-vine behaviour instead of calling them, so nothing breaks if
  Supplementaries changes. Its models are used only as parents (by id), as for other ARR mods.
- **Textures:** Supplementaries' `blocks/stick.png` and `stick_cross.png` recoloured to the treated stick item's
  palette (the 4 colours of Updated Engineering's `material_stick_treated.png`, the texture players see) by
  luminance, darkest to darkest. Being derived from an ARR texture, they live in `kubejs/assets/` beside the pack, not
  in the GPL jar (the pack's route for ARR-derived files, `ends_delight.md` 5).
- **Not in `farmersdelight:ropes`.** FD's own climb would turn a tagged block into its rope-logged vine (and leave an
  FD rope behind); the climb hooks handle the treated stick before that check, so the tag is not needed.
- **Mature vine + stick:** the vine's own pick runs first (a click on a ripe vine harvests it); Supplementaries' stick
  turns even a ripe vine into a stick vine. Only immature bottom vines take a treated stick.
- Without Supplementaries the models have no parent (purple-black); the pack always ships it.

## 4. Tests

- [ ] TSK-01 Place treated sticks on the floor, a wall and a ceiling: vertical, and both horizontals. Click a placed
  stick's side with another: crossing stick added; a third axis too. Break: one Treated Stick per axis. Pick-block
  gives a Treated Stick. Place one in water: waterlogged.
- [ ] TSK-02 Right-click a chest, crafting table and a lever with a treated stick: the block opens / toggles, no stick
  placed.
- [ ] TSK-03 Plant tomato seeds on farmland, grow to a vine, right-click it with a treated stick: the vine shows a
  treated stick, keeps its age. Put two treated sticks above and bone meal: the vine climbs, three high at most.
  Pick a ripe upper vine: tomatoes, stage resets. Break an upper vine: stick stays, drops as FD's vine.
- [ ] TSK-04 A vine on a Supplementaries stick with a treated stick above, and a treated-stick vine with a
  Supplementaries stick and a rope above: each climbs onto its own kind (Supplementaries stick vine, rope vine,
  treated stick vine).
- [ ] TSK-05 Break the farmland under a bottom treated-stick vine: the vines above die back to treated sticks.
