# Plan: crop yields, second wave

Owner decisions of 2026-11-14 (scratchpad; answers to Q2, Q4, Q6, Q7 and the bush breakdown the same day), on top of the built soil yield system in [crop_yields.md](crop_yields.md).
**BUILT 2026-11-14** except H's adzuki replant (owner pick open) and the pinned / potential items (peanut soil, mini
cantaloupe sand). Creative Delight and Creative Common jars built, not installed; tests SQ-SY-14 to -22 queued (run
SQ-SY-08 to -13 first). Build record and the changes against this plan: section 13. The feature doc is
[crop_yields.md](crop_yields.md) (sections 2.5, 3, 4 rewritten).

Related TODO rows: `farming/crops #24` (nether yield), `#45` (tilled End Stone), `largecontent/mod-additions #35`
(Botany Pots). New rows: `farming/crops #53` to `#57` (section 9).

## 0. At a glance

| # | Part | Decision | Kind | Open |
|---|---|---|---|---|
| A | Fortune never affects crop yield | decided: strip it from all crop drops (melons and cantaloupe left alone) | loot-table edits | none |
| B | Rich Soil and Rich Farmland stay equal (+33%) | decided | rule, no work | none |
| C | Round each crop's base to the nearest 0.5 | decided | script table | farmland drops change or not (Q1) |
| D | Own picks get the soil yield (patch) | decided | Java hook in Creative Delight | exact hook point per crop |
| E | Tomato vines use the soil under the bottom vine | decided | part of D | none |
| F | Ghost pepper own pick fixed | decided | part of D | none |
| G | Seeds on full breaks of cucumber and ghost pepper | decided | data (loot table) | none |
| H | Adzuki soil, bush soils, peanut soil, mini cantaloupe | investigate, then decide | investigation | section 5 |
| I | Nether soils boost like the overworld ones | decided | script + config | default nether soil (audit) |
| J | Propelplant boosts by the soil under the lowest cane | decided | Java hook | none |
| K | Fungus colonies follow their overworld counterparts, on shearing | decided | Java hook | none |
| L | Cave carrot growth soils | investigate | investigation | section 7 |
| M | Botany Pots: +10% Rich, +20% Fertile growth speed, no yield, tooltip | decided | pot soil recipes + tooltip | exact soil recipe ids |
| N | Stale outline (crop_yields.md section 4) | to rewrite | doc | after this plan is accepted |

Order of work: A and B (doc only) -> C (script) -> D/E/F/G (hook + loot data, one Creative Delight build) -> I/J/K
(nether, needs D's hook) -> M (independent) -> H and L (investigations, can run any time, results may add rows).

## 1. Fortune never affects yield (A)

Rule: no soil effect and no base yield in the soil-yield system reads Fortune. Wave 1 already skips Fortune on the soil
pools ("No Fortune on the soil pools", crop_yields.md 5). The owner's rule goes further: "Fortune should not affect crop
yield ever".

- Soil pools: already true. Keep it, state it as a rule instead of a choice.
- **Decided 2026-11-14 (Q3, owner): strip Fortune from the extra drops on all crops.** The crops' own tables carry
  Fortune bonuses (`binomial_with_bonus_count`, `uniform_bonus_count`, `apply_bonus` with `enchantment: fortune`;
  crop_yields.md 1): remove every Fortune term from a crop's produce, so a Fortune tool gives the same as none.
  "All crops" = every crop in the soil yield list (26), the bush group (5.2) and any other crop-like table found by the
  audit below, **except melon, hamimelon, cantaloupe and mini melon: their drops stay exactly as they are** (owner
  2026-11-14, no Fortune strip, no fixed slice count). Fortune still works on ores and everything that is
  not a crop.
- Method: a loot-table edit in the same `genericLootTables` pass: drop `apply_bonus` fortune functions on the produce
  entry; for `binomial_with_bonus_count` with `enchantment: fortune` replace the function by a plain `set_count` of the
  same mean (n = base extra, probability unchanged) with no enchantment term; `limit_count` stays. Same method as
  `rustic/crop_drops.js`. Seeds count as part of the crop (wheat, beetroot, cabbage and others carry the Fortune bonus on
  the seeds): strip those too.
- Audit first (offline, the loot dump): list every block table with a `minecraft:fortune` term whose block is a crop,
  bush, stem fruit or melon, and strip exactly that list; ores, leaves, sea pickle, glowstone and other non-crop
  tables are out.
- Test: a Fortune III hoe or axe on ripe wheat, carrots, a bush gives the same average as no Fortune (section 10); melon blocks still react to Fortune.
- The new own-pick hook (D) never reads Fortune.

## 2. Rich Soil equals Rich Farmland (B)

Owner: equal on purpose. Both stay +33% (Fertile Soil +66%). Remove the "is it intended" item from the open list. The
`richSoilBonus` key stays separate from `richFarmlandBonus` in the config (they only differ if someone edits them);
note in crop_yields.md 2.2 that equality is intended.

## 3. Rounded bases (C)

Owner: round each crop's average production to the closest 0.5 and "reflect this in the yield boosts". Reading used
here: the `base` the script multiplies by the soil bonus becomes the rounded value; the soil bonus is then
`bonus x rounded base`. This makes the boost easy to state per crop and independent of odd means such as 3.71.

| Crop | Mean now | Rounded base | Rich / Rich Farmland extra (x0.33) | Fertile Soil extra (x0.66) |
|---|---|---|---|---|
| wheat, beetroot, cabbage, broccoli, cauliflower, turnip, corn, flax, cucumber, oats | 1.00 | 1.0 | +0.33 | +0.66 |
| tomatoes | 1.50 | 1.5 | +0.50 | +0.99 |
| bell pepper | 1.45 | 1.5 | +0.50 | +0.99 |
| cotton, garlic | 2.00 / 2.14 | 2.0 | +0.66 | +1.32 |
| adzuki | 2.50 | 2.5 | +0.83 | +1.65 |
| sweet potato, zucchini | 2.71 | 2.5 | +0.83 | +1.65 |
| hemp (top) | 2.88 | 3.0 | +0.99 | +1.98 |
| cave carrots | 3.00 | 3.0 | +0.99 | +1.98 |
| coffee | 3.50 | 3.5 | +1.16 | +2.31 |
| carrots, potatoes, onions, peanuts | 3.71 | 3.5 | +1.16 | +2.31 |
| tobacco | 6.71 | 6.5 | +2.15 | +4.29 |

(Ender Crop stays out.) The 2.1 table's soil columns in crop_yields.md are regenerated from these (plain farmland mean
stays as read from the dump; the soil column becomes mean + extra).

- **Q1 (decided yes, 2026-11-14):** the rounding only changes the size of the *extra*; plain farmland drops stay as the mods ship them.
  If you instead want the plain drops tuned to the round value (carrots 3.71 to 3.5), that is a separate table edit on
  14 crops and changes every farm; not planned unless you say so.
- Script change: the 26 `base` values in `soil_yield.js` change; `rolls()` unchanged. A hard-coded base that no
  longer matches the dump gets a check: the CD offline Node test compares each `base` to the loot dump mean and fails
  when they differ by more than 0.5 plus a mod table change (closes the "bases are hard-coded" hole).
- Bonus formula and binomial split stay (crop_yields.md 3).

## 4. Own picks (D, E, F, G)

Problem: a crop picked by its own right-click never rolls its loot table, so soil pools do nothing there
(crop_yields.md 5). Affected: tomato vines (FD budding tomatoes / tomatoes on ropes), Vintage cucumber and ghost
pepper, Veggies zucchini, Nether's Delight propelplant and fungus colonies (K, J), lemon tree already has its own
mixin (`LemonTreePickYieldMixin`) as precedent.

**D. The patch.** One Creative Delight Java hook, in the same style as `LemonTreePickYieldMixin`, that runs after the
block's own pick has decided its drop and adds the soil bonus:

1. Find the soil block for the picked block (the same offsets as the script: 1 below, 2 for rice and hemp; the lowest
   vine for E, the lowest cane for J).
2. Read the soil bonus from the same `CDConfig` `SoilYield` keys and the crop switch (`SoilYield.cropOn`), and the
   crop's rounded base from one shared table. The base table moves out of the JS into a data file both sides read
   (JSON in the CD jar), so the script and the hook cannot disagree. A JS-only fallback is not possible: the pick is a
   Java method that never fires a loot event.
3. Add extra produce with the same rule as the pools: `binomial(ceil(extra), extra / ceil(extra))`, extra = bonus x
   base. Fortune is not read.
4. Per-block hook point (read the port class first; pick the narrowest): the method that spawns the drop
   (`popResource` / `Block.dropResources` call) via `@Redirect` or `@ModifyArg`, as the lemon mixin does.

   | Pick | Class | Soil read |
   |---|---|---|
   | Cucumber, ghost pepper | Vintage port `*CropBlock.use` | block below |
   | Zucchini | Veggies port `*CropBlock.use` | block below |
   | Tomato vines | FD `TomatoVineBlock.use` (and rope / stick variants) | soil under the bottom vine (E) |
   | Propelplant | Nether's Delight berry cane `use` | soil under the lowest cane (J) |
   | Fungus colonies | Nether's Delight colony shearing | soil under the colony (K) |

5. Mixins on third-party classes: remember the `@Overwrite` / `SubscribeEvent` gotcha (memory) and the CapabilityProvider
   one; hook only method bodies, never the provider.

**E. Tomato vines.** A vine on a rope has no soil below it, so the boost comes from the "tomato crop below it": walk
down through vines, ropes and sticks to the bottom budding-tomato block and read the soil under that. Same bonus as a
tomato broken from the bottom vine. Cap the walk (the plant is at most a few blocks; use the existing max height).

**F. Ghost pepper own pick fixed.** "Fix ghost peppers own pick": today the pick works at stage 7 and returns the
plant to stage 5 (crops.md 2.2). The fix is the hook above plus checking that the pick gives the ripe produce (1 ghost
pepper) and the soil bonus on `#vintagedelight:ghost_pepper_soil` blocks (Soul Soil, Nulch, Rich Soul Soil; I). Read the
class first: the owner's wording says the pick is wrong, not just unboosted; if it has a real bug (wrong state, no drop
on a server, soil check), fix it in the same CD change and log it. Test VTD-01 to -03 and VTD-76 plus new rows.

**G. Seeds on full breaks.** Finding F1: Vintage's cucumber and ghost pepper put the seeds bonus on age 3, so a ripe
plant drops no seeds and a half-grown one does. Decision: add the same seed drop to the full-grown break. Data only,
in the same loot pass as the others: append to each table a pool with the ripe `block_state_property` condition and the
seed entry with the existing seed count (cucumber and ghost pepper 1 to 4 seeds as their age-3 pool gives today). Leave
the age-3 pool alone. Decide in the build whether the own pick also drops a seed (it does not now; plan: no, the pick
keeps the plant growing). Track under a TODO row (the old `farming/crops #3` pointer is a lemon row).

## 5. Soils beyond farmland (H, investigation)

### 5.1 What happens to Rich or Fertile Soil under an adzuki harvest

Read from the Neapolitan 4.1.0 bytecode (`AdzukiSproutsBlock.replant`, not yet checked in game): after a mature sprout is
broken, the block below becomes Adzuki Soil only if it is Dirt, Coarse Dirt or Grass Block (three vanilla block checks
in `replant`). Rich Soil, Rich Farmland and Fertile Soil are none of those, so:

- Rich and Fertile Soil are **not deleted**; they stay and no "rich adzuki soil" exists as a block.
- They also never become Adzuki Soil, so a sprout on them does **not** regrow by itself: the player has to replant.
  Adzuki Soil is the only block whose random tick spawns a sprout above it.
- Adzuki Soil itself is a plain block: the soil yield pools (checked by block id) do not see it, so a regrown sprout on
  Adzuki Soil gets no bonus (it is dirt-equivalent, base yield).

**Decided 2026-11-14 (owner, Q4): only existing soils, no new variant blocks.** The "two new blocks" option below is
dropped. What stays open is whether Rich and Fertile Soil regrow adzuki (replant mixin) or not (keep as is). Options
as first written:

| Option | Effect | Cost |
|---|---|---|
| Keep as is | Rich and Fertile stay, adzuki needs replanting there, Adzuki Soil is the free-regrow soil at base yield | none |
| Two new blocks, rich and fertile adzuki soil | A harvest on Rich / Fertile converts to the variant; variant regrows and keeps +33% / +66% | two blocks, models (texture rule: owner's tools / human art only), a replant mixin, a soil list entry |
| Extend `replant` by mixin so Rich / Fertile Soil also regrow, without a new block | Same regrowth, no new blocks | one mixin on Neapolitan, still no new art |

Recommended (still to confirm, one line): the third (mixin, no new blocks): a mature adzuki break on Rich Soil, Rich Farmland or Fertile Soil keeps
the block and replants a sprout (or lets the soil's random tick do it), so the soil bonus stays. Needs the owner's pick.

### 5.2 Bush breakdown (owner asked for the list, 2026-11-14)

All of these regrow by dropping to an earlier stage, so a regrow soil adds nothing; what a soil can add is the yield
bonus on the pick. Every bush pick is an own pick, so each one needs hook D, and none of them is in the soil yield
system today. The "mean" column is what one pick gives as the mods ship it (crops.md 2.1 bush table, loot dump
2026-10-26; "unknown" = not in either, read the class first). Rounded base = mean rounded to 0.5 (section 3 rule).

| Bush | Block | Pick gives | Mean | Rounded base | Soils it accepts today | Notes |
|---|---|---|---|---|---|---|
| Sweet berries | `minecraft:sweet_berry_bush` | 2 to 3 berries at age 3 (vanilla pick: 1 to 2; the table's 2 to 3 applies on break) | 2.5 break / 1.5 pick | 2.5 or 1.5 (decide which path is the base) | dirt, farmland, rich, fertile | Own pick drops fewer than breaking; pick the base per path |
| Strawberries | `neapolitan:strawberry_bush` | strawberry (red or white) at age 6 | 1.0 | 1.0 | all | break gives the berry plus pips |
| Salmonberries | `delightful:salmonberry_bush` | salmonberries from stage 3 | unknown (class) | read | all | table only drops pips |
| Mint | `neapolitan:mint` | mint leaves when grown | unknown (class) | read | all | herb, not a fruit: owner call whether it counts |
| Blueberries, Cranberries | `fruitsdelight:blueberry_bush`, `cranberry_bush` | 1 to 2 berries at age 4 | 1.5 | 1.5 | all | cranberry table has a Fortune bonus: stripped by A |
| Lemon tree | `fruitsdelight:lemon_tree` | 2 to 5 lemons (own mixin) | 3.5 | 3.5 | all | already has `LemonTreePickYieldMixin`; the soil bonus goes into that mixin, not a second one |
| Grape bushes (red, white, savanna, taiga, jungle, 10 blocks) | `vinery:*_grape_bush*` | grapes from stage 2; table drops seeds 2 to 3 at age 3 | unknown (class) | read | dirt, rich, fertile; **not** farmland or Rich Farmland | need a full block below; Fertile Soil not confirmed (CR-13); Creative Brewing member |
| Tea bush | `farmersrespite:tea_bush` | leaves by age plus 2 to 3 sticks, **shears only** | leaves unknown | read | dirt, rich, fertile; farmland refused | the pick is a shear action (like fungus colonies, K) |
| Flower bushes (Floral Enchantment, 17) | `floralench:*_bush` | flowers or vine items | unknown | read | all | decorative; suggest out |

**Decided 2026-11-14 (Q5): in = strawberries, blueberries, cranberries, lemon, sweet berries and salmonberries.**
Out for now: mint, flower bushes, tea, grape bushes (grapes and tea wait on the soil check CR-13 and a class read;
they can be added later by a switch). The bushes are a separate switch group `[crop_drops.soil_yield.bushes]` with
six keys (`strawberry`, `blueberry`, `cranberry`, `lemon`, `sweet_berry`, `salmonberry`), all `true`, same bonuses as
the crops (Rich +33%, Fertile +66%, rounded base). Work per bush:

- Strawberries, blueberries, cranberries, sweet berries, salmonberries: own pick, through hook D.
- Lemon: soil bonus added inside the existing `LemonTreePickYieldMixin`.
- Salmonberries: the mean is not in the dump (the table drops only pips); read `SalmonberryBushBlock` (Delightful)
  for the pick count before the base is set, and use that mean rounded to 0.5.
- Sweet berries: pick the base path: the vanilla own pick (1 to 2, mean 1.5) is what players do, so use the pick
  (base 1.5); breaking the ripe bush uses the table (2 to 3, mean 2.5, base 2.5). Both paths get the bonus with their
  own base (loot pool for the break, hook for the pick). Fortune stripped from the break table (A).
- Strawberries on break drop the berry once at age 6; same split (table pool for break, hook for pick).
- Bushes dropping back to an earlier stage need no soil change; the soil under the bush is the block below.

### 5.3 Peanut soil: pinned

Owner 2026-11-14: pin for later. Peanuts stay a farmland crop with Quark replant and the standard soil yield. No work,
no recommendation until it is picked up again. TODO row `farming/crops #56` keeps it as a pinned sub-item.

### 5.4 Mini cantaloupe and 'rooted sand'

Owner 2026-11-14: "rooted sand" is the appearance of the **future mini cantaloupe sand**, and a **potential block to
add**. So it is a future block, not an existing one (which is why the repo has no trace of it). Recorded as a
potential, not planned work:

- A sand-type soil block for mini cantaloupe with a rooted-looking texture. Falls under the texture rule (human art or
  the owner's tools only; ask for the source art and palette) and under "only allow existing soils" until the owner
  approves a new block.
- Until it is decided: mini cantaloupe stays on its current soils and is not in the soil yield list.
- Before any build: read how mini cantaloupe (Delightful `mini_melon`) is planted (a stem crop, so the soil sits under
  the stem) and which soils it takes today.

## 6. Nether crops (I, J, K)

Rule (owner): the nether farmlands are **Rich Soul Soil** and **Fertile Soul Soil** (Nulch), and they boost like the overworld soils: Rich +33%, Fertile +66%. Default nether soil (soul sand or
netherrack) is the base, no bonus; the audit below decides which one per crop.

**Names (decided 2026-11-14, owner: "soul soil", not "nether soil").** Rich Soul Soil and Fertile Soul Soil. Fertile Soul
Soil is Nulch (`immersive_weathering:nulch_block`), already renamed that way on 2026-10-23, so no rename work: only the
plan and docs stop saying "fertile nether soil" or "nulch" where a player-facing name is meant.

**Config.** Same bonuses as the overworld: new `[crop_drops.soil_yield]` keys `richSoulSoilBonus` (0.33) and
`fertileSoulSoilBonus` (0.66) (the old `nulchTries/Chance` plan keys are dropped), plus per-crop switches in the
`crops` section (`nether_wart`, `ghost_pepper`, `propelplant`, `crimson_fungus_colony`, `warped_fungus_colony`). Default
state: on, like the wave-1 crops (the old plan said off).

**Audit of the default nether soil per crop (owner: soul sand or netherrack may work).** Run before building:

| Crop | Soils today | Default (no bonus) soil to confirm | Better soils |
|---|---|---|---|
| Nether Wart | Soul Sand, Nulch | Soul Sand (vanilla rule) | Fertile Soul Soil +66%; Rich Soul Soil: check if wart accepts it |
| Ghost Pepper | `#vintagedelight:ghost_pepper_soil`: Soul Soil, Nulch, Rich Soul Soil | Soul Soil | Rich Soul Soil +33%, Fertile Soul Soil +66% |
| Propelplant | nylium, netherrack, Rich Soul Soil | netherrack | Rich Soul Soil +33% (Nulch: check) |
| Fungus colonies | nylium, mycelium, Soul Soil, Rich Soul Soil, dirt | netherrack / nylium for the matching fungus | Rich Soul Soil +33%, Fertile Soul Soil +66% if accepted |

For each crop, read the block's `canSurvive` / placement tag and the soil tags, then fill: default soil, accepts Rich
Soul Soil, accepts Nulch. If a crop refuses one of the better soils the plan either skips that soil for that crop or
widens the placement tag (decide per row; widening the tag is data, a mixin is not needed).

**J. Propelplant.** "Boost yield on the block the lowest cane is planted on": a propelplant is a stack of berry canes;
the pearls come from the berry cane pick (own pick). The hook (D) walks down to the lowest cane block and reads the soil
under it; extra produce = the pearls (base of the pearl count, rounded to 0.5, from the dump) times the soil bonus.

**K. Fungus colonies.** Follow the overworld counterparts: the rules are the same as for the mushroom / fungus rows in
the overworld list (the colony's fungi count is the base; a Rich / Fertile soil adds the same share). The extra is rolled
on shearing (own action), and on break through the loot table. "Which age counts as ripe" is answered by the colony's own
shear rule: shearing is only possible when the block allows it, and the bonus rides on that event. Soil: the block under
the colony.

## 7. Cave carrots (L, investigation)

crop_yields.md 5: Cave carrots are a `CropBlock`: farmland soils only; crops.md has no row for them. Owner: investigate
the growth soils. Questions to answer, from the block class and the loot dump:

1. Which blocks can hold it (`canSurvive`, plant tag, light rule)? Does it take deepslate, stone-like cave ground
   instead of farmland? (Wild patches use height, not soil: crops.md 1136.)
2. Does Rich Farmland / Fertile Soil work (it is in the soil yield list today, so they must hold it)?
3. Where does it grow wild and on what block?

Output: a crops.md 2.1 row, and a note here whether the soil yield list should keep it. If the crop accepts non-farmland
cave blocks, name the best soil (analogous to the adzuki / nether soil rule) and add it to the soil list.

## 8. Botany Pots (M)

Owner: pot growth gets +10% (Rich) and +20% (Fertile) speed from those soils, no yield, and a tooltip must say so.

- Soils in pots are `botanypots:soil` recipes with a `growthModifier`. Audit the current rich / fertile soil recipes
  (Rich Soil, Rich Farmland, Fertile Soil; FD / IW blocks) and set their modifiers to 1.10 (Rich Soil and Rich
  Farmland) and 1.20 (Fertile Soil); pot dirt and farmland stay 1.0. Set it from `botany_pots.js` (the existing server
  script) so the numbers are in one place, same helper family as `scaleRecipeInt`.
- No yield change: soil yield pools only fire on world blocks; pots are untouched (crop_yields.md 5 limit). State it.
- Tooltip, English text for the two soil items: "Growth speed in Botany Pots +10%" for Rich Soil and Rich Farmland,
  "+20%" for Fertile Soil; plus a second line "Does not change yield." Add to
  [tooltips.md](tooltips.md) (next TT id) and `kubejs/client_scripts/tooltips.js`. The existing pot tooltip stays.
- Interaction with the x8 crop slow-down: the modifier multiplies on top, so a wheat pot on Fertile Soil takes
  9600 / 1.2 = 8000 ticks.
- Changelog bullet when built (growth speed values, player-visible).

## 9. TODO rows to add

| New id | Title | Covers |
|---|---|---|
| `farming/crops #53` | Soil yield wave 2: rounded bases, no Fortune | A, B, C |
| `farming/crops #54` | Own picks use soil yield | D, E, F, G |
| `farming/crops #55` | Nether soil yield (replaces `#24` outline) | I, J, K; `#24` becomes a pointer to this |
| `farming/crops #56` | Adzuki, bush, peanut and mini cantaloupe soils | H |
| `farming/crops #57` | Cave carrot soils | L |

Botany Pots keeps its own row (`mod-additions #35`) with a line pointing here.

## 10. Tests to write when built (SQ-SY- continues at -14)

- Bushes: six keys on; pick and break of each bush on Rich Farmland / Fertile Soil.
- Rounded base: wheat 1.00 / 1.33 / 1.66; carrots 3.71 / 4.87 / 6.02 (3.71 + 1.16 / + 2.31); sweet potato 2.71 / 3.54 /
  4.36.
- Fortune III on ripe wheat gives the same average as none; no Fortune term on a stripped table (offline loot dump).
- Own pick: cucumber, zucchini, ghost pepper, tomato vine on a rope over Fertile Soil; averages match the table, and a
  Fortune tool changes nothing.
- Ripe cucumber and ghost pepper break drops seeds; half-grown still does.
- Propelplant pearls and fungus colony shearing on Rich Soul Soil and Nulch; default soil unchanged.
- Adzuki break on dirt, Rich Soil, Fertile Soil (result per option picked in Q4).
- Botany pot growth time on Rich and Fertile Soil; tooltip text.
- Offline: base table vs loot dump check, the same table read by script and hook.

## 11. Questions for the owner

| Q | Question | Default if not answered |
|---|---|---|
| Q1 (DECIDED yes) | Rounding changes only the size of the soil extra (plain drops untouched)? | yes |
| Q2 | Names | DECIDED: Rich Soul Soil, Fertile Soul Soil (no rename) |
| Q3 | Fortune | DECIDED: strip Fortune from all crop drops (section 1); melon, hamimelon and cantaloupe drops left alone (owner 2026-11-14) |
| Q4 | Adzuki on Rich / Fertile Soil | DECIDED: existing soils only, no new blocks; confirm replant mixin vs keep as is (default mixin) |
| Q5 | Bushes | DECIDED: strawberries, blueberries, cranberries, lemon, sweet berries, salmonberries (5.2) |
| Q6 | Peanut soil | PINNED for later |
| Q7 | "Rooted sand" | ANSWERED: the look of the future mini cantaloupe sand, a potential new block (5.4); nothing built |

## 13. Build record (2026-11-14)

Built in one session. Where the build differs from the sections above, the build is right:

- **A.** 77 tables (audit list in `soil_yield.json` `fortune_free`; leaves, IW weeds and truffle dirt left out as
  owner calls). `binomial_with_bonus_count` became an additive `set_count` binomial with the same n and p (the
  Fortune-0 roll), not a fixed count; `uniform_bonus_count` dropped. Nether wart's Fortune function carries its own
  condition, kept.
- **C.** The base table moved to Creative Delight `creativedelight/soil_yield.json` (read by the script through
  `SoilYieldPicks.lootBase` and by the pick hook). The offline check is `node tools/soil_yield_check.js` (it runs the
  real script against the dump), not an extension of an existing Node test.
- **D/E/F.** One `@ModifyArg` on each pick's `popResource` call, one mixin per mod package (the CD plugin gates by
  package), all calling `crops.SoilYieldPicks`. Each pick has its own base from what the pick drops, which can differ
  from the break: cucumber and zucchini picks drop 1 to 2 (base 1.5) where the break gives 1 and 2.71. Supplementaries'
  stick and rope tomatoes inherit FD's pick and have rows. Ghost pepper pick: no bug in the class (F); it only lacked
  the soil bonus.
- **G.** Ripe break: 1 + binomial(3, 0.5714) seeds (1 to 4). The age-3 pool gives 2 to 5 (its alternative seed plus
  the bonus pool), not the 1 to 4 section 4 says.
- **5.2 bushes.** Read from the classes: the vanilla sweet berry pick at age 3 gives 2 to 3 like the table (the 1 to 2
  in the table above is the age-2 pick), so one base 2.5 and the age-2 pick gets nothing. Blueberry and cranberry
  breaks drop 1 (base 1.0); picks 1 to 2 (1.5). Strawberry pick 1 to 2 (1.5), break 1 (1.0). Salmonberry: age 4 pick
  2 to 3 (2.5), age 3 pick of 1 not raised; its table drops pips only, so no break pool. Lemon: the pack table drops
  lemons from the lower half only, one pool.
- **6 nether audit.** Wart refuses Rich Soul Soil (Nulch only, +66%); propelplant refuses Nulch (Rich Soul Soil only,
  +33%; no code widening); colonies age only on Rich Soul Soil and cannot be placed on Nulch, so every grown colony
  gets the +33% and the Nulch pool never fires (no tag widened). Colony break base 5 (age 3, not with shears), shear
  pick base 1. Propelplant pearls come only from the pick (berry cane and berry stem, 1 to 2); the walk passes canes
  and stems.
- **L.** Cave carrots: farmland soils only (vanilla, Rich Farmland, Fertile Soil), light 9 to grow; kept in the list.
- **M.** The mod ships Rich Soil x1.10, Rich Farmland x1.25, farmland x1.05 and no Fertile Soil pot soil. Rich Farmland
  goes down to x1.10; Fertile Soil is a new pot soil at x1.20 (categories dirt, farmland); farmland stays x1.05. New
  NCH helpers `setRecipeNumber`, `botanySoil` (Creative Common).
- **Dicts (Creative Common):** `b.strawberry_bush`, `b.blueberry_bush`, `b.cranberry_bush`, `b.lemon_tree`,
  `b.nether_wart_crop`.
- **Open:** H adzuki replant on Rich / Fertile Soil (Q4: mixin or keep as is); grape, tea, mint bushes later.

## 12. Files this will touch

- `kubejs/data/creativedelight/kubejs/creativedelight/soil_yield.js` (bases, nether keys, Fortune strip)
- CD Java: new own-pick hook mixin and the shared base table (JSON); CDConfig `SoilYield` keys
- Vintage member loot tables (seeds on break), `farmersdelight` tomato pick, Nether's Delight picks
- `config/creativedelight-common.toml` and `configs_edited/root_config/` copy
- `kubejs/server_scripts/other/botany_pots.js`, `kubejs/client_scripts/tooltips.js`
- docs: crop_yields.md (2.1, 2.2, 4, 5), crops.md, botany_pots.md, tooltips.md, TODO, README
- changelog bullets in `unreleased.md` when each part is built (not for this plan)
