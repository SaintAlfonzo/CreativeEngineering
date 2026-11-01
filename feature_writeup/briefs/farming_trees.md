# farming / trees: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# farming`, Sub `trees`; swept 2026-10-31 (row 3 added, rows 1 and 2
listed under "Built, needs play"). Feature writeups: [mob_block_tweaks.md](../mob_block_tweaks.md) 2.5 (leaf decay,
test prefix `MT-`), [structure_tuning.md](../structure_tuning.md) 3.25 (deadwood, `ST-`), [crops.md](../crops.md) 2.1
(the lemon bush, `CR-`). Layout: [README.md](README.md).

## Scope and order

| Step | Row | Work | Notes |
|---|---|---|---|
| 1 | `trees #3` Lemon tree yield | CD KubeJS recipes + patcherMod mixin | The only open row. The pick count is hard-coded in Fruits Delight's Java, so the yield half is a mixin, not KubeJS |
| - | `trees #1` Leaf decay | Testing | Needs play, see below |
| - | `trees #2` Acacia stumps and fallen logs | Testing | Needs play; the bush fix is an open decision |

Proposed tests are placeholders (`CR-new1`, ...), numbered when queued.

---

## trees #3: Lemon tree yield
Status **Planned** · Work KubeJS (+ patcherMod, see Now) · id `farming/trees #3`

### Goal
Picking a ripe lemon bush gives 2 to 5 lemons, and cutting a lemon gives 8 slices.

### Now (Fruits Delight 0.5.11, read 2026-10-31)
- `fruitsdelight:lemon_tree` (`i.lemon_tree`, item_dict.js 13243) is a two-high bush, class `DoubleFruitBushBlock`
  (`FDBushes`: blueberry, lemon, cranberry; lemon is the tall one, the class mapping per bush is unverified). Pick at
  age 4 on either half: `use` (`m_6227_`) drops `1 + random.nextInt(2)` = **1 to 2 lemons**, hard-coded (bytecode
  offsets 109-146), then sets both halves back to age 2. Quark's hoe area clicks it as a bush ([crops.md](../crops.md)
  2.1 line 146: "own pick at stage 4, either half: 1-2 lemons (back to 2)").
- Break loot `fruitsdelight:blocks/lemon_tree`: the lower half at age 4 drops 1 lemon (Fortune uniform bonus +1 per
  level); silk touch keeps the bush. Botany Pots `fruitsdelight:botanypots/lemon`: 1 lemon plus 5 % for a second.
- Slices: FD knife `fruitsdelight:lemon_cutting` (`#forge:fruits/lemon` -> 4 lemon slices + 1 lemon seeds), Create
  saw `fruitsdelight:cutting/lemon_slice` (lemon -> 4 slices), and Slice & Dice's copy
  `sliceanddice:cutting/fruitsdelight/lemon_cutting`.
- Dict: `i.lemon` (13241), `i.lemon_slice` (12450), `i.lemon_seeds` (13242), `i.t_fruits_lemon` (1879), `i.any_knife`
  (246). Lemon recipes in the pack: CD
  [fruitsdelight/recipes.js](../../kubejs/data/creative_delight/kubejs/fruitsdelight/recipes.js) (orange jam takes one
  lemon slice, lines 42-45); nothing touches the cutting recipes yet.
- The compile jar `fruitsdelight-0.5.11.jar` is already in `development/patcherMod/libs/`.

### Build
1. **Pick count (patcherMod).** The TODO says KubeJS, but the count is in Java. Mixin on
   `dev.xkmc.fruitsdelight.content.block.DoubleFruitBushBlock` (`remap = false`), `@Redirect` (or `@ModifyArg` on the
   `ItemStack` count) in `m_6227_`: for the lemon bush only (compare the block to `fruitsdelight:lemon_tree`), count =
   2 + `random.nextInt(4)`. Covers the empty hand, Quark's hoe area click and fake players alike. Register it in
   `creativeengineering.mixins.json`; a CEConfig pair `[crop_drops.lemon] pickMin = 2`, `pickMax = 5` if the owner
   wants it tunable.
2. **Slices (KubeJS, CD tree).** In `fruitsdelight/recipes.js`: `R.remove({ id: 'fruitsdelight:lemon_cutting' }, e)` and
   `R.remove({ id: 'fruitsdelight:cutting/lemon_slice' }, e)`; then
   `R.fdCutting(i.t_fruits_lemon, i.any_knife, [Item.of(i.lemon_slice, 8), i.lemon_seeds], e, 'lemon_cutting')` and
   `R.createSaw(Item.of(i.lemon_slice, 8), i.lemon, e, 'lemon_slice')`. `/reload`.
3. Writeups: crops.md 2.1 lemon row (1-2 -> 2-5); changelog bullets (fruit / farming family), e.g. "Lemon bushes give 2
   to 5 lemons when picked [1 to 2]" and "A lemon cuts into 8 Lemon Slices [4]".

### Decisions
- Break loot and Botany Pots: raise them too? Recommendation: no; breaking kills the bush, and the pot is a separate
  balance (keep 1 + 5 %).
- Seeds from the knife: keep 1 lemon seed per cut (recommended, unchanged).

### Gotchas
- KubeJS `BlockEvents.rightClicked` could add 1 to 3 extra lemons without a jar, but Quark's hoe area simulates the
  click on the block (whether that fires the event is unverified), so the hoe path may miss the bonus. The mixin avoids
  the question.
- Slice & Dice builds its cutting copies from the live FD recipes after KubeJS (memory `sliceanddice-manual-only`
  covers the mixer copies; check the cutting copy follows the new recipe, or remove it by id after).
- Doubling slices per lemon halves the lemon cost of every slice recipe (orange jam, juices that take slices); intended.

### Tests
- **CR-new1** (proposed, numbered when queued): 50 picks of a ripe lemon bush: 2 to 5 lemons each, average about 3.5;
  the bush goes back to age 2. Same through an iron hoe area click.
- **CR-new2**: knife on a cutting board and the Create saw: 1 lemon -> 8 Lemon Slices (+ 1 seed on the board); JEI
  shows no 4-slice recipe.

---

## Built, needs play

| Row | Change | Status | Notes |
|---|---|---|---|
| trees #1 | Leaf life distance + decay timing + Fast Leaf Decay port | Testing (S+ P T) | Shipped 1.1.01 (patcherMod), [mob_block_tweaks.md](../mob_block_tweaks.md) 2.5; `[leaf_decay]` in `config/creativeengineering-common.toml` (line 2098 on); scratch server passed, decay piles PASSED 2026-10-22; MT-13 to MT-25 in game (MT-24 leaf piles) |
| trees #2 | Acacia stumps and fallen logs | Testing (S P T) | Shipped 1.1.03 (new chunks only), [structure_tuning.md](../structure_tuning.md) 3.25 (Geophilic stumps and fallen logs x2, six `meta/deadwood_<wood>` in `group/deadwood`); ST-228 to ST-231, SQ-DW-01 to -03 not run. Open: the sunk log is most likely Geophilic's bush (trunk height 0, `dirt_provider` = its log); fix = trunk height 1 + dirt provider dirt, acacia only or all woods (decision); confirm in game first |

### Open decision carried by trees #1 (not a build task yet)
- **F6, diagonal-only leaves fall at placement** ([mob_block_tweaks.md](../mob_block_tweaks.md) 4, F6, with the
  options listed there): 189 leaves on 29 tree features are touched only diagonally and decay right after worldgen
  because `TreeFeature`'s edge shape updates count as a neighbour change. Recommendation: play MT-17 (big trees in new
  chunks) first. Only if the bare corners show in game, pick one of F6's options and file it as a new `trees` row.

## Writeup corrections made

- [mob_block_tweaks.md](../mob_block_tweaks.md) 0 (leaf decay row): "BUILT 2026-10-19 (patcherMod 1.0.9 rebuild, in
  `mods/`, not deployed)" -> "(a patcherMod rebuild after the 1.0.9 deploy, now patcherMod 1.1.01 in `mods/`; not
  deployed)" (2026-10-21; the TODO row now says shipped 1.1.01).
- [mob_block_tweaks.md](../mob_block_tweaks.md) 0 Pending: "MT-01 to MT-23 untested" -> "MT-01 to MT-25 untested"
  (MT-24 / MT-25 exist in 7).
