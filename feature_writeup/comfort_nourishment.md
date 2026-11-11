# Comfort vs Nourishment audit

Audit of which foods give Farmer's Delight's *Comfort* and which give *Nourishment*, and whether a food's hunger or
saturation value decides between them. Written so the owner can set pack rules (section 5). R5 is built (section 10).

## 0. At a glance

| Part | Status |
|---|---|
| Audit (upstream 1.19.2 values, before the delight backports) | DONE 2026-10-26 |
| Pack rule for Comfort / Nourishment | DECIDED 2026-11-02 (section 5, answers below the decision list); meat rice and stew cutoff 10; not built |
| Ingredient rule R5 (inputs' hunger and count decide the effect; bowls, mason jars, shulker bowls, copper cups) | RULES SET by owner 2026-10-27 (section 9): Comfort input hunger 5+ / 3+ inputs, Nourishment input hunger 8+ / 4+ inputs and output hunger 10+, an effect-less bowl at 10 or more output hunger gets Comfort, two lengths by output hunger (120 s / 240 s); non-bowl forms settled (appendix C); decisions CN-D6, CN-D9 still open |
| Applying the rule | BUILT 2026-10-27 (section 10): generator + Creative Delight startup script, CD jar in `mods/`; CN-D6 and CN-D9 taken as recommended; owner rules 4 and 5 (golden, 60 s light Comfort) added; tests CNS-01 to CNS-08; in the 1.1.04 client payload (2026-10-29), server 1.1.05. **BROKEN (deep scan 2026-11-02):** scratch r1102b SQ-CNS-01 PASS, SQ-CNS-02 batch fault (wrong beef stew id; the other 7 match), SQ-RD-22 FAIL (no Rustic Delight food in `comfort_nourishment.json`, so Rustic meals lose their Nourishment) and SQ-FDB-04 FAIL (no rule for Gleaming Salad); SQ-PSU-09 open (vinegar jar set to none). The 2026-11-02 21:09 regeneration (copper cup rule dropped, 264 set) fixes neither. Cans: `/dumpfood` refreshed 2026-10-30 00:42 by `/dumpall`, `gen_can_types.py` rerun 2026-11-01 (120 cans moved to the rule; CNS-08 ready). TODO `food/types-and-tuning #15` |

**Answer:** there is **no hunger or saturation cutoff**. In Farmer's Delight 1.2.4 the two effects use the same value
tiers (Chicken Soup and Roast Chicken are both 14 / 0.75 for 300 s). The values pick the **duration**; the **dish form**
picks the effect: soups, stews, broths and warm bowls give Comfort, plated meals give Nourishment. Add-ons follow that
form rule loosely (81 %); Miner's Delight 1.1.2 breaks it most.

Source: `kubejs/exported/food/foods.json` (`/dumpfood` of 2026-10-24 03:39, 1437 foods). Script: scratchpad, not kept;
it is about 40 lines of Python over that file (section 6).

## 1. What the two effects do (1.19.2, FD 1.2.4)

- **Comfort**: heals half a heart every 4 s while saturation is empty and the player has no Regeneration. It replaces
  saturation healing for a hungry player. ([effects.md](effects.md) row Comfort.)
- **Nourishment**: hunger does not drain, except the drain from saturation healing on a nearly full bar. It keeps the
  bar where it is.

They overlap little: Comfort is for a player who is already low, Nourishment keeps a player from getting low.

## 2. Scope

- **Audit set (130 foods):** every food in the dump that gives exactly one of the two effects, from mods running their
  own upstream 1.19.2 values: Farmer's Delight 1.2.4 (repack, values unchanged), Abnormals, Alex's, Corn, Create Central
  Kitchen, Delightful, Ender's 1.2.2, End's, Farmer's Respite, Fruits, Miner's 1.1.2 (the dump predates the 1.4.5 port
  install of 2026-10-25), More, Nether's 3.1, Ocean's, Rustic 1.19.2 (the dump predates the 1.7.0 port).
  Pack overrides (`cooking/food_values.js`, `minecraft/food_values.js`) touch neither effect, so the dump equals upstream.
- **Left out:** the ports with 1.20-era values (Brewin' 4.5.0, Veggies 1.9.2, Vintage, Fungi; 64 foods, kept for
  comparison in 4.4 and A.2) and the pack's 52 cans (copies of their source food).

## 3. Cutoff tests

### 3.1 Spread

| Set | Effect | Foods | Hunger min / median / max | Modifier min / median / max | Seconds min / median / max |
|---|---|---:|---|---|---|
| Audit set | Comfort | 43 | 0 / 8 / 16 | 0 / 0.6 / 1.0 | 10 / 180 / 300 |
| Audit set | Nourishment | 87 | 0 / 12 / 18 | 0 / 0.75 / 1.1 | 10 / 180 / 300 |
| FD only | Comfort | 10 | 6 / 13 / 14 | 0.4 / 0.75 / 0.8 | 30 / 240 / 300 |
| FD only | Nourishment | 14 | 10 / 14 / 14 | 0.6 / 0.75 / 0.8 | 60 / 240 / 300 |

Comfort's median is lower only because cups, teas and cake slices pull it down. The ranges cover each other.

### 3.2 Best single threshold

Each value was tried as "Comfort below X, Nourishment from X up" (and the reverse) at every X in the data. Baseline:
calling every food Nourishment is already right for 87 of 130 (67 %).

| Value | Best split | Right (audit set) | Right (FD only, baseline 14 / 24) |
|---|---|---:|---:|
| Hunger | Comfort below 9 | 91 / 130 (70 %) | 16 / 24 (below 10) |
| Saturation points | Comfort below 6.3 | 91 / 130 (70 %) | 16 / 24 (below 12) |
| Modifier | none useful | 89 / 130 | 15 / 24 |
| Duration | none useful | 87 / 130 | 15 / 24 |

No threshold beats the baseline by more than 3 points. **Ten value pairs carry both effects**, among them 12 / 0.8,
14 / 0.75, 14 / 0.8, 10 / 0.6, 8 / 0.6 and 6 / 0.4. A value cutoff cannot reproduce upstream.

### 3.3 Is there a cutoff for getting either effect at all?

No. Hearty bowls with neither effect: Cooked Whole Crab 15, Shrimp-Fried Rice 12 / 1.0, Mashed Potatoes (Corn Delight)
12 / 0.8, Diced Potatoes with Beef 10 / 0.8, Rabbit Stew 10 / 0.6, Cavern Explorer Stew 10 / 0.7, Miso with Bamboo
Sprouts 10 / 0.7, Mushroom Stew and Beetroot Soup 6 / 0.6. Light foods with one: Cake slices 1 / 0.1 (10 s), Moss
1 / 0.05, teas at 0 hunger.

## 4. What does decide it

### 4.1 Farmer's Delight 1.2.4 (the reference design)

| Hunger / mod | Seconds | Comfort | Nourishment |
|---|---:|---|---|
| 6 / 0.4 | 30 | Cooked Rice |  |
| 8 / 0.7 | 60 | Bone Broth |  |
| 10 / 0.6 | 60 |  | Bacon and Eggs, Ratatouille |
| 12 / 0.8 | 180 | Beef Stew, Fish Stew, Vegetable Soup | Mushroom Rice, Pasta with Meatballs, Pasta with Mutton Chop, Steak and Potatoes |
| 14 / 0.75 | 300 | Baked Cod Stew, Chicken Soup, Noodle Soup, Pumpkin Soup, Egg Fried Rice | Honey Glazed Ham, Roast Chicken, Shepherd's Pie, Stuffed Pumpkin, Roasted Mutton Chops, Squid Ink Pasta, Vegetable Noodles; Grilled Salmon (180 s) |

- **Effect = form.** Comfort: soups, stews, broth, plain and fried rice. Nourishment: plated meals, pasta, roasts.
  Noodle Soup is Comfort, Vegetable Noodles is Nourishment, at identical values.
- **Duration = value tier.** 6 hunger 30 s, 8 to 10 hunger 60 s, 12 hunger 180 s, 14 hunger 300 s, for both effects
  (one exception: Grilled Salmon). Add-ons follow this tier exactly for only 54 of 123 foods; most stay within one step.

### 4.2 Form rule across the add-ons

Rule tested: the name says soup, stew, broth, tea, drink, congee, chili, oatmeal, oats, porridge, curry or sinigang ->
Comfort, anything else -> Nourishment. Right for **105 / 130 (81 %)**; FD 22 / 24 (misses: Cooked Rice, Egg Fried Rice).
Adding "rice" to the Comfort words fixes FD's two but breaks Mushroom Rice and More Delight's three cooked-rice plates.

| Mod | Form rule right | Notes on the misses |
|---|---:|---|
| Abnormals, Ender's, Farmer's Respite, More, Nether's, Rustic | all |  |
| Delightful | 8 / 9 | Azalea Tea gives Nourishment (Lavender Tea gives Comfort) |
| Corn | 4 / 5 | Creamed Corn (bowl, Comfort) |
| End's | 9 / 12 | Ender Noodle Comfort; Enderman Gristle Stew and Dragon Meat Stew Nourishment |
| Fruits | 4 / 8 | Pear with Rock Sugar, Mangosteen Cake, Pineapple Fried Rice Comfort; Fig Chicken Stew Nourishment |
| Alex's | 2 / 4 | Kangaroo Stew, Acacia Blossom Soup Nourishment |
| Ocean's | 1 / 2 | Cabbage Wrapped Elder Guardian Comfort (15 / 1.0, 30 s) |
| Central Kitchen | 1 / 2 | Yucca Cake slice Comfort, Pumpkin Cake slice Nourishment |
| **Miner's 1.1.2** | **18 / 28** | Its own rule: a cup of an FD soup copies FD (Comfort); its own soups (Bat, Cave, Insect) and the cups of the **vanilla** soups (Mushroom, Beetroot, Rabbit) and Onion Soup get Nourishment |

So upstream has one design (FD's) and add-ons that mostly copy it; the misses are mod authors' choices, not a hidden
value rule. Meat-heavy stews (kangaroo, dragon, gristle, fig chicken) lean Nourishment, which matches FD 1.3's later move.

### 4.3 The vanilla soups have neither

Mushroom Stew, Beetroot Soup and Rabbit Stew give nothing, but Miner's cups of them give Nourishment. Any form rule
would make them Comfort; that is a choice (CN-D4).

### 4.4 Ported (1.20-era) foods, for comparison

FD 1.3 and Brewin' 4.5 moved soups to Nourishment ([farmers_delight.md](farmers_delight.md) 2.5): Brewin's Creamy Onion
Soup is Nourishment. Veggies 1.9.2 (port) keeps 9 Comfort foods (soups, stews, coleslaw, mashed potatoes, pancakes).
Vintage gives Comfort to jams, apple sauce, oatmeal, overnight oats and its soups, and Nourishment to pickles, pizza,
pasta and burgers; Relish gives **both**. Fungi: Creamy Morel Soup Comfort, the rest Nourishment.
Decision 2.5 A (2026-10-25) already keeps Comfort on FD's soups, so the pack follows the 1.19.2 design.

## 5. Rule options for the owner

| Option | Rule | Changes | For | Against |
|---|---|---|---|---|
| **R1 Form rule** (recommended) | Comfort: soup, stew, broth, chowder, congee, porridge, oats, chili, curry, plain/fried rice in a bowl, warm drinks and teas. Nourishment: plated meals, pasta, roasts, sandwiches, burgers, bars, salads | 25 upstream foods (A.1, column "Form rule says"), plus the ports to taste | FD's own design, matches decision 2.5 A, explainable in one line on a tooltip or JEI | needs per-food overrides; meat stews may feel like meals |
| R2 Value cutoff | e.g. hunger 9 or less Comfort, 10 and more Nourishment | about 40 foods | one number, automatic for new foods | every FD soup (12 to 14 hunger) becomes Nourishment, reversing decision 2.5 A; light plates become Comfort |
| R3 Status quo | keep upstream, fix only outliers (Miner's vanilla-soup cups, Azalea vs Lavender Tea, cake slices) | under 10 | least work | no rule to point new foods at |
| R5 Ingredient rule (section 9) | Comfort: inputs worth 5+ hunger in 3+ slots, or no effect and output hunger 10+; Nourishment: 8+ hunger in 4+ slots and output hunger 10+; lengths 120 s (output under 10) / 240 s (10 up); bowls only | 84 of 191 bowl foods | automatic for every new bowl, no names or overrides | breaks the soup / plate split; thin soups lose Comfort |
| R4 Form + value hybrid | R1 for which effect, FD's tier for duration (section 4.1) | R1 plus durations for about 70 foods | consistent lengths pack-wide | biggest change; durations are also a balance knob for food.md D1-D6 |

Decisions:

- **CN-D1** Which option (R1 / R2 / R3 / R4). Recommended R1, durations left as upstream until the tuning layer
  (food.md 4.3) can apply R4's tiers as one setting.
- **CN-D2** Rice: Comfort as in FD (Cooked Rice, Egg Fried Rice), or Nourishment as in Mushroom Rice and More Delight's
  rice plates? Recommended: plain and fried rice Comfort, rice topped with meat or mushrooms Nourishment.
- **CN-D3** Meat-heavy stews (Kangaroo, Dragon Meat, Enderman Gristle, Fig Chicken): Comfort by form, or stay
  Nourishment? Recommended Comfort, so "bowl you drink from" is the only test.
- **CN-D4** Vanilla Mushroom Stew, Beetroot Soup, Rabbit Stew: add a short Comfort (FD's 6-hunger tier, 30 s) or leave
  them bare? If added, Miner's cups of them follow.
- **CN-D5** Foods that give both (Vintage Relish bottle and jar) and non-meal items (cake slices, jams, Moss): keep, pick
  one, or drop the effect?

**Owner answers 2026-11-02:**

- **CN-D1** R1 form rule, with **bars and salads moved to Comfort** (the R1 Nourishment list loses them).
- **CN-D2 and CN-D3** (rice, meat stews) default to **Comfort**, with a **nutrition cutoff** above which they become **Nourishment** (applies to meat rices and stews). **The cutoff is 10** (the food's own hunger): 9 or less Comfort, 10 or more Nourishment, for meat rices and meat stews only.
- **CN-D4** Vanilla Mushroom Stew, Beetroot Soup and Rabbit Stew **get the short Comfort**. Miner's Delight cups are being removed, so nothing is added for them.
- **CN-D5** The effect is **dropped from Vintage Relish** (bottle and jar), **cake slices and jams give Comfort only**, **Moss gives neither**.
- Applying this is TODO `textures/food #5`; the R5 numbers (CN-D6 to CN-D9) stay as built.

Applying any option: one `food_values.js`-style list in Creative Delight's tree (all food
code goes in CD), then `gen_can_types.py` so cans copy the new effects, and a changelog bullet.

## 6. File map and method

- Data: [foods.json](../kubejs/exported/food/foods.json) (fields `effects[].effect`, `nutrition`, `saturationModifier`,
  `remainder`, `tags`). Refresh with `/dumpfood` (part of `/dumpall`).
- Generated catalog with every food's effects: [food_catalog.md](food_catalog.md).
- Related decisions: [farmers_delight.md](farmers_delight.md) 2.5 (keep Comfort), [food.md](food.md) 4.3 (tuning layer
  D1-D6), [effects.md](effects.md) (effect texts).
- Method to repeat: load `foods.json`, keep foods with exactly one of `farmersdelight:comfort` /
  `farmersdelight:nourishment`, drop the port mods and `creativeengineering`, then for hunger, modifier, saturation and
  seconds try every observed value as a split and count matches; separately test the name regex of 4.2.
- `comfort_nourishment.js` reads `kubejs/generation_scripts/food/comfort_nourishment.json` from the instance, not the CD
  jar: when the file is missing it logs a warning and changes nothing (was a TypeError in `ItemEvents.modification`,
  found 2026-11-06 in the minimal CEPortTesting instance).

## 7. Tests

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-CNS-01; rerun pending (batch fault) SQ-CNS-02.
- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-CNS-03, -02.

IDs are `CNS-` (`CN-` is taken by Corn Delight and the can tests). Restart with the 2026-10-27 CD jar first. Hover the
food (effect line and length in the tooltip), eat it, check the inventory screen.

| ID | Check | Pass |
|---|---|---|
| CNS-01 | `logs/latest.log` after boot | `[comfort_nourishment] 269 ruled foods set, N bottles stripped, M other foods lost Nourishment`, no `could not read` or `no item_dict key` line |
| CNS-02 | Noodle Soup, Plate of Roast Chicken | Nourishment 4:00, no Comfort |
| CNS-03 | Beef Stew, Baked Cod Stew | Comfort 4:00 |
| CNS-04 | Bone Broth, Cooked Rice | neither effect |
| CNS-05 | Dog Food, Rabbit Stew Cup, Slice of Yucca Cake | Comfort 1:00 |
| CNS-06 | Golden Carrot; Enchanted Golden Apple | Nourishment 2:00; Nourishment 4:00 next to its vanilla effects |
| CNS-07 | Chunkwich, Pickle; Azalea Tea, Relish Bottle | no Nourishment (other effects kept); no Comfort or Nourishment |
| CNS-08 | after `/dumpfood` + `python3 tools/gen_can_types.py` + restart: Canned Noodle Soup, Canned Bone Broth | Nourishment; neither |

Scratch server: SQ-CNS in [testing/scratch_queue.md](testing/scratch_queue.md): SQ-CNS-01 PASS r1102b, SQ-CNS-02 batch fault (rerun with `farmersdelight:beef_stew`), SQ-CNS-03 (cans, CNS-08 server half) not run. Related FAILs r1102b: SQ-RD-22, SQ-FDB-04.

## 8. History

- 2026-11-03 (plan_scratch_bugfix_1103 5.4): `GOLDEN_EXTRA` in the generator rules golden foods outside the golden food
  types; Gleaming Salad is the first (Nourishment 120 s [240 s by hand]); the json regenerated (2 lines: the salad and the strip key
  `vinegar` -> `vinegar_bottle`, since `i.vinegar` now names the jar). The vinegar jar keeps `none` and gets Nausea 10 s from
  CD `vintagedelight/vintage_items.js` (5.5).

- 2026-11-03 (CD members run): `creativedelight/members #43` rechecked: the JSON does carry the Rustic foods, keyed by
  item_dict keys (`calamari_soup`, `potato_salad`, `fried_chicken`, ...), so a grep for `rusticdelight:` finds only
  `sweet_salad` (no key). The rule gives 9 of the 10 Rustic bowls Comfort (Calamari Soup Comfort 240 s; Batter and Bell
  Pepper Soup neither) and none Nourishment: their inputs are under the Nourishment line (input hunger 8). So SQ-RD-22's
  expectation (Nourishment) is wrong under the rule; rerun it expecting Comfort. Oceanic Delight was outside the rule
  (code-applied effects, container in code): generator `CODE_CONTAINERS` / `CODE_FEASTS`, port bonuses removed
  (ocean_delight.md 8). Regenerated: 285 set (was 264): +13 Oceanic, +8 Miner's copper cups at the kept-Comfort 60 s (the
  cups are removed from the pack, so the rows do nothing), 0 other foods lose Nourishment.
- 2026-11-02 (deep scan): status set to BROKEN from the r1102b results (SQ-RD-22, SQ-FDB-04 FAIL); cans follow the rule since the 2026-11-01 can rerun.
- 2026-11-02: owner answered CN-D1 to CN-D5 (section 5): form rule with bars and salads to Comfort, rice and meat stews Comfort with a nutrition cutoff to Nourishment, vanilla soups get Comfort, Relish effect dropped, cake slices and jams Comfort only, Moss neither. Not built.

- 2026-10-27 (build): R5 built (section 10) with CN-D6 / CN-D9 as recommended; owner rules 4 (golden foods
  Nourishment 120 s / 240 s) and 5 (Comfort 60 s at output hunger 7 or less) added to 9.0 during the build.
- 2026-10-27 (latest): the rules were extended to mason jars, shulker bowls and copper cup meals; bottles lose all
  effects; every other food loses Nourishment (appendix C rewritten, C.1 to C.5; CN-D8 settled).
- 2026-10-27 (later): owner added output-hunger rules to R5 (section 9.0): Nourishment needs output hunger 10+, an
  effect-less bowl at 10 or more output hunger gets Comfort, and both effects use two lengths split at 10 output hunger
  (short 120 s, long 240 s, set by the owner; the averages of today's bowls are 140 s / 220 s). Appendix B.1 recomputed: 107 of 191 bowls unchanged.
- 2026-10-27: ingredient rule R5 investigated (section 9, appendices B and C): best fit Comfort 5+ hunger / 3+
  inputs, Nourishment 8+ / 4+, 109 of 191 bowls unchanged. Non-bowl foods with an effect listed in appendix C.
- 2026-10-26: audit done on the 2026-10-24 dump. No value cutoff; FD picks the effect by dish form and the duration by
  value tier. Options R1 to R4 and decisions CN-D1 to CN-D5 written for the owner.

## 9. Ingredient rule (R5, investigated 2026-10-27)

Owner idea: decide the effect from the **recipe inputs**, not the dish form. Bowl meals only for now.

- **Inputs** = the ingredient slots of the recipe that makes the bowl (the bowl itself does not count).
- **Input value** = the total hunger of those inputs. A slot that takes several items (a tag or a list) counts its
  **lowest** hunger food; members that are not food are skipped, and a slot with no food member counts 0.
- Rule: input value at least **Vn** and inputs at least **Cn** -> Nourishment; else input value at least **Vc** and
  inputs at least **Cc** -> Comfort; else neither. Nourishment needs more on both: Vn > Vc and Cn > Cc.

### 9.0 Owner rules (2026-10-27), on top of the input rule

1. **Nourishment needs output hunger 10 or more.** The food's own hunger (what it restores), not the input value. A
   bowl that passes the Nourishment input test (8+ hunger, 4+ inputs) but restores under 10 falls through to the
   Comfort test.
2. **Fallback Comfort.** A bowl that gets no effect from the input rule and restores **10 or more** hunger gets
   Comfort.
3. **Two lengths, both effects.** Output hunger under 10 -> **short 120 s**, 10 and above -> **long 240 s**. The owner
   set these after seeing the averages of today's bowl durations in each band: 141.7 s (18 bowls) and 220.9 s (102
   bowls, median 180 s); the upstream audit set (A.1) gives 144.5 s / 219.3 s. This replaces FD's four-step tier
   (section 4.1) for rule-driven bowls.
4. **Golden foods** (added 2026-10-27, later): every food of the golden food type gives Nourishment 120 s, every
   enchanted golden food Nourishment 240 s. This overrides every other rule (bowls, bottles, no-recipe bowls) and
   replaces Comfort: a golden soup gives Nourishment only.
5. **Light Comfort** (added 2026-10-27, later): a food left with Comfort that restores **7 or less** gets 60 s. This
   covers ruled bowls, jars and cups and the foods outside the ruled forms that keep their Comfort (C.5).

Order of tests, first match wins:

| Test | Result |
|---|---|
| 4+ inputs, input hunger 8+, output hunger 10+ | Nourishment |
| 3+ inputs, input hunger 5+ | Comfort |
| output hunger 10+ | Comfort |
| otherwise | no effect |

Then two overrides (rules 4 and 5 below): a golden food gives Nourishment only (120 s, enchanted 240 s) whatever the
table says, and any Comfort on a food that restores 7 or less lasts 60 s instead of 120 s.

The 9.1 fit below chose only the two input thresholds; the three owner rules were added afterwards and are not fitted.

### 9.1 Fitted numbers

Every Vc, Cc, Vn, Cn combination was scored on the 191 bowl foods that have a recipe (37 Comfort, 83 Nourishment,
71 neither). Score = the mean of the three per-class hit rates, so neither class can win by swallowing the others
(chance 0.33; plain accuracy picks "everything Nourishment", 84 / 120 of the effect foods).

| Rule | Comfort | Nourishment | Score | Comfort kept | Nourishment kept | Neither kept | Same overall |
|---|---|---|---:|---:|---:|---:|---:|
| **Recommended** | hunger 5+, 3+ inputs | hunger 8+, 4+ inputs | 0.567 | 20 / 37 | 46 / 83 | 43 / 71 | 109 / 191 (57 %) |
| Base value 1 (non-food inputs count 1) | hunger 4+, 3+ inputs | hunger 10+, 4+ inputs | 0.579 | 29 / 37 | 37 / 83 | 36 / 71 | 102 / 191 |
| Saturation instead of hunger | saturation 2+, 3+ inputs | saturation 6+, 4+ inputs | 0.583 | 27 / 37 | 39 / 83 | 39 / 71 | 105 / 191 |
| Hunger + saturation | 9+, any count | 12+, 4+ inputs | 0.591 | 18 / 37 | 46 / 83 | 52 / 71 | 116 / 191 |

The variants gain at most 0.024, and hunger is the number players see, so the recommendation stays with hunger and
lowest food value. Upstream 1.19.2 mods only (146 foods, ports left out) give the same picture: 80 / 146 unchanged.

Count spread (all 191): 1 input is 22 neither and 1 Comfort; 3 inputs 14 C / 16 N; 4 inputs 11 C / 34 N; 5 to 6
inputs 9 C / 25 N; 7 to 8 inputs (all feasts) 5 N. Hunger spread: under 6 is 14 C / 16 N / 51 neither; 9 and up is
9 C / 42 N.

### 9.2 What the rules change (input rule plus owner rules 9.0)

| Now \ Rule | Comfort | Nourishment | neither |
|---|---:|---:|---:|
| Comfort (37) | 25 | 6 | 6 |
| Nourishment (83) | 36 | 44 | 3 |
| neither (71) | 31 | 2 | 38 |

107 of 191 bowls keep their effect (56 %; the input rule alone kept 109). The full per-food answer, with the length
each bowl would get, is appendix B.1.

- **Rich soups move to Nourishment (6):** Noodle Soup, Pumpkin Soup, Sinigang, Pickle Soup, Ghostly Chili, Dragon's
  Breath and Chorus Soup. Ender Noodle and Coleslaw now stay Comfort (output hunger 8 and under).
- **Thin soups lose Comfort (6):** Cooked Rice, Bone Broth, Oatmeal, Shulker Soup, Strider Moss Stew, Bell Pepper Soup.
  All restore under 10 and have too little input hunger.
- **Nourishment loses its effect (3):** Pike with Beetroot, Insect Stew, Bat Soup (all restore under 10).
- **Nourishment drops to Comfort (36):** every plate whose input hunger is under 8 or with 3 inputs (pastas, rice
  plates, Fried Chicken, Ratatouille, Shakshouka), plus plates that restore under 10 (Plate of Grilled Shulker, Fake
  Meatloaf, Cheese Pasta, Enderman Gristle Stew), and Grilled Strider, Cave Soup and Fish and Chips (10 hunger, fallback).
- **Bowls with no effect today gain one (33):** 31 Comfort (ice creams, salads, Dog Food, Glowgurt, Takoyaki, diced
  potatoes, Mashed Potatoes, Blazing Chili, Tea Curry) and 2 Nourishment (Neapolitan Ice Cream, vanilla Rabbit Stew).
  Fruit Salad, Cucumber Salad and Adzuki Curry are Comfort, not Nourishment as under the input rule alone. Rule 4 then
  turns the golden soups (Golden Beetroot Soup, Golden Mushroom Stew, Golden Rabbit Stew) into Nourishment 120 s.
- **Rules 4 and 5 (2026-10-27, later)** change the lengths: every golden food is Nourishment 120 s (enchanted 240 s),
  and Comfort on a food restoring 7 or less lasts 60 s (28 foods, e.g. Dog Food, ice creams, jam jars, most soup cups).
  The counts above are from before these two rules; the generator's own tally is in section 10.
- **Rule 2 (fallback Comfort) decides 19 bowls**: 11 that restore 12 to 15 (Stuffed Morels, Squid Ink Pasta, Fried
  Mushrooms, Perch with Mushrooms, Mushroom Rice, Acacia Blossom Soup, Cooked Rice with Chicken Cuts, Seasoned
  Arthropods, Bowl of Guardian Soup, Bowl of Pineapple Fried Rice, Baked Cod Stew) and 8 that restore exactly 10
  (Grilled Strider, Cave Soup, Fish and Chips, Blazing Chili, Tea Curry, Mashed Potatoes, Diced Potatoes with Beef and
  with Chicken Cuts). They would be bare without it.

Why it cannot fit better: the current split follows dish form (section 4), and inputs cut across form. Noodle Soup (4
inputs, 9) is Comfort while Vegetable Noodles (5 inputs, 7) is Nourishment; Beef Stew (3, 6) Comfort next to Pasta with
Mutton Chop (3, 7) Nourishment. Any input rule moves about 40 % of today's bowls. It is a new design, not a model of
the old one.

### 9.3 Method and data

- Data: `foods.json` and `food/recipes.json` of the 2026-10-24 `/dumpfood`, `tags/item_tags.json` of 2026-10-26. The
  dump predates the Miner's 1.4.5 and Rustic 1.7.0 ports (2026-10-25); rerun after the next `/dumpall`.
- Recipe per food: the cooking pot recipe, else shapeless, else shaped crafting (first id). A feast serving uses its
  feast block's recipe whole (Roast Chicken plate = 8 inputs); dividing value by servings scored worse.
- A non-food input that is itself crafted (raw pasta, dough, batter, sauces) takes the value of its cheapest recipe per
  item, two levels deep. This changed almost nothing against counting it 0.
- Bowl meal = hands back `minecraft:bowl`. 22 bowl foods had no recipe found (feasts whose serving link is missing in
  the dump, Ender's own pot recipes); they are listed in B.2 and need a hand check. Cans (52) are left out.
- Script (Python over the three dumps, about 120 lines): session scratchpad, not kept.

### 9.4 Decisions for R5

- **CN-D6** (settled 2026-10-27: owner asked to execute the plan, so the recommended numbers) R5 with Comfort hunger
  5+ and 3+ inputs, Nourishment hunger 8+ and 4+ inputs.
- **CN-D7** (settled by the owner 2026-10-27) Duration: two lengths for both effects, short 120 s below 10 output
  hunger and long 240 s from 10 up (section 9.0), instead of FD's four tiers.
- **CN-D8** (settled by the owner 2026-10-27) Non-bowl foods: the rules apply to mason jars, shulker bowls and copper
  cup meals (appendix C.1 to C.3); bottles lose every effect (C.4); every other food loses Nourishment and keeps
  Comfort (C.5). Cans copy their source food through `gen_can_types.py`, so cans follow the result.
- **CN-D10** (settled by the owner 2026-10-27) Fallback Comfort counts 10 as well: "10 or more".
- **CN-D9** (settled 2026-10-27 as recommended) Base value for non-food inputs: 0, the rule as stated (1 would score
  slightly better and keep Bone Broth, Mushroom Rice and the fried rices in).

Applying R5: a generator in `tools/` writes the per-food effect list from the dumps into `generation_scripts/`, read
by a startup food script in Creative Delight's `creativeengineering/` tree (FoodProperties are startup-time; restart
needed). New bowls then get their effect from the next `/dumpall` + `/cdevgen`.

## 10. Built (2026-10-27)

- **Generator** [gen_comfort_nourishment.py](../tools/gen_comfort_nourishment.py) (`/cdevgen run comfort_nourishment`,
  group `food`): the rules of 9.0 (1 to 5) and appendix C, written to
  `kubejs/generation_scripts/food/comfort_nourishment.json` (`set`: food -> effect and seconds, `keep`: no recipe
  found, `strip`: bottles). `--list` prints every ruled food with inputs, input hunger and recipe. The rules never read
  a food's current Comfort / Nourishment except to find the kept Comfort of rule 5, so the output does not shift after
  the game has applied it.
- **Applier** Creative Delight `kubejs_startup/creativeengineering/comfort_nourishment.js` (priority -100, after the
  other CD food rebuilds): sets `set`, strips `strip`, and removes Nourishment from every other food in the registry
  (new foods too) except cans and `keep`. Restart to apply; shipped through build_deploy's runtime list.
- **Cans**: [gen_can_types.py](../tools/gen_can_types.py) applies the same file to the source food before copying
  effects (70 cans change). Was blocked until the next `/dumpfood` (Pickled Sea Pickle not in the food catalog); rerun
  2026-11-01 (120 cans on the rule, CD member `can_types.js`), so cans follow (deep scan 2026-11-02; CNS-08, SQ-CNS-03).
- (2026-11-02: the copper cups are removed from the pack, `dishes/meals #53`; the cup rule left the generator.) **Copper cups** took their bowl from Miner's own `cup_conversion` data in the donor jar; the Onion Soup Cup's bowl
  is Brewin's Creamy Onion Soup (FD 1.19.2 has no Onion Soup).
- **Check against appendix B / C**: 215 of 216 listed foods match. Glow Ink Pasta differs: the doc lists input hunger
  8 (Nourishment), but the stated rule (lowest food per slot: raw fish 1, pasta 2, glow berries 2, ink sac 0) gives 5,
  so it is Comfort 240 s. Not in the tables (no effect today): Honey Jar restores 10, so the fallback gives it Comfort
  240 s (C's "no jar restores 10" counted only jars with an effect); Nut Mash Jar Comfort 120 s; the shulker-bowl
  Twisted Cereal neither. Appendix B and C were updated to rules 4 and 5 on 2026-10-27 (Rule, Inputs, Input hunger and Seconds rule columns regenerated from the generator).
- **Output (2026-10-24 dump)**: 269 set (118 unchanged), 19 kept for the B.2 hand check (the three enchanted golden
  soups left B.2 through rule 4), 105 bottles, 22 other foods lose Nourishment, 28 Comfort foods at 60 s, 52 golden
  foods. Rerun after the next `/dumpall` (the dump predates the Miner's 1.4.5 and Rustic 1.7.0 ports).
- **Open**: the B.2 hand check (19 bowls keep today's effect); the missing Rustic Delight and Gleaming Salad rules (SQ-RD-22, SQ-FDB-04 FAIL r1102b).

## Appendix: every food in the audit

"Form rule says" shows the form rule's answer where it disagrees with the food's current effect ("same" otherwise).
These are the foods R1 would change.

### A.1 Upstream 1.19.2 foods (the audit set, 130)

| Effect | Hunger | Mod | Sat | Seconds | Hands back | Form rule says | Food | Mod id |
|---|---:|---:|---:|---:|---|---|---|---|
| Comfort | 16 | 0.6 | 19.2 | 180 | bowl | same | Dragon's Breath and Chorus Soup | ends_delight |
| Comfort | 15 | 1 | 30 | 30 | - | Nourishment | Cabbage Wrapped Elder Guardian | oceansdelight |
| Comfort | 15 | 0.9 | 27 | 300 | bowl | same | Coconut Curry | delightful |
| Comfort | 15 | 0.9 | 27 | 300 | bowl | same | Sinigang | delightful |
| Comfort | 15 | 0.8 | 24 | 180 | bowl | same | Bowl of Guardian Soup | oceansdelight |
| Comfort | 14 | 0.8 | 22.4 | 300 | bowl | Nourishment | Bowl of Pineapple Fried Rice | fruitsdelight |
| Comfort | 14 | 0.75 | 21 | 300 | bowl | same | Baked Cod Stew | farmersdelight |
| Comfort | 14 | 0.75 | 21 | 300 | bowl | same | Chicken Soup | farmersdelight |
| Comfort | 14 | 0.75 | 21 | 300 | bowl | Nourishment | Egg Fried Rice | farmersdelight |
| Comfort | 14 | 0.75 | 21 | 300 | bowl | same | Noodle Soup | farmersdelight |
| Comfort | 14 | 0.75 | 21 | 300 | bowl | same | Pumpkin Soup | farmersdelight |
| Comfort | 12 | 0.8 | 19.2 | 180 | bowl | same | Beef Stew | farmersdelight |
| Comfort | 12 | 0.8 | 19.2 | 180 | bowl | same | Carrot Soup | moredelight |
| Comfort | 12 | 0.8 | 19.2 | 180 | bowl | same | Fish Stew | farmersdelight |
| Comfort | 12 | 0.8 | 19.2 | 180 | bowl | same | Vegetable Soup | farmersdelight |
| Comfort | 10 | 0.9 | 18 | 180 | bowl | same | Corn Soup | corn_delight |
| Comfort | 10 | 0.6 | 12 | 180 | bowl | same | Ender Congee | ends_delight |
| Comfort | 9 | 0.6 | 10.8 | 60 | bowl | Nourishment | Mangosteen Cake | fruitsdelight |
| Comfort | 8 | 0.7 | 11.2 | 60 | bowl | same | Bone Broth | farmersdelight |
| Comfort | 8 | 0.6 | 9.6 | 180 | bowl | Nourishment | Ender Noodle | ends_delight |
| Comfort | 8 | 0.6 | 9.6 | 60 | bowl | Nourishment | Pear with Rock Sugar | fruitsdelight |
| Comfort | 8 | 0.6 | 9.6 | 180 | bowl | same | Shulker Soup | ends_delight |
| Comfort | 8 | 0.6 | 9.6 | 120 | bowl | same | Strider Moss Stew | nethersdelight |
| Comfort | 7 | 0.5 | 7 | 180 | bowl | Nourishment | Creamed Corn | corn_delight |
| Comfort | 7 | 0.375 | 5.25 | 150 | copper_cup | same | Baked Cod Stew Cup | miners_delight |
| Comfort | 7 | 0.375 | 5.25 | 150 | copper_cup | same | Chicken Soup Cup | miners_delight |
| Comfort | 7 | 0.375 | 5.25 | 150 | copper_cup | same | Noodle Soup Cup | miners_delight |
| Comfort | 7 | 0.375 | 5.25 | 150 | copper_cup | same | Pumpkin Soup Cup | miners_delight |
| Comfort | 6 | 0.6 | 7.2 | 180 | bowl | same | Bell Pepper Soup | rusticdelight |
| Comfort | 6 | 0.4 | 4.8 | 90 | copper_cup | same | Beef Stew Cup | miners_delight |
| Comfort | 6 | 0.4 | 4.8 | 30 | bowl | Nourishment | Cooked Rice | farmersdelight |
| Comfort | 6 | 0.4 | 4.8 | 90 | copper_cup | same | Fish Stew Cup | miners_delight |
| Comfort | 6 | 0.4 | 4.8 | 90 | copper_cup | same | Vegetable Soup Cup | miners_delight |
| Comfort | 4 | 0.5 | 4 | 300 | shulker_bowl | same | Chorus Stew | endersdelight |
| Comfort | 4 | 0.5 | 4 | 300 | bowl | same | Chorus Stew | endersdelight |
| Comfort | 4 | 0.4 | 3.2 | 300 | shulker_bowl | same | Endermite Stew | endersdelight |
| Comfort | 4 | 0.4 | 3.2 | 300 | bowl | same | Endermite Stew | endersdelight |
| Comfort | 4 | 0.35 | 2.8 | 30 | copper_cup | same | Bone Broth Cup | miners_delight |
| Comfort | 2 | 0.6 | 2.4 | 60 | glass_bottle | same | Creamy Corn Drink | corn_delight |
| Comfort | 1 | 0.1 | 0.2 | 10 | - | Nourishment | Slice of Yucca Cake | create_central_kitchen |
| Comfort | 0 | 0 | 0 | 180 | glass_bottle | same | Dandelion Tea | farmersrespite |
| Comfort | 0 | 0 | 0 | 270 | glass_bottle | same | Dandelion Tea | farmersrespite |
| Comfort | 0 | 0 | 0 | 60 | glass_bottle | same | Lavender Tea | delightful |
| Nourishment | 18 | 0.5 | 18 | 300 | bowl | same | Roasted Dragon Steak | ends_delight |
| Nourishment | 15 | 0.6 | 18 | 180 | - | same | Ender Bamboo Rice | ends_delight |
| Nourishment | 14 | 1.1 | 30.8 | 300 | bowl | same | Glow Ink Pasta | miners_delight |
| Nourishment | 14 | 0.9 | 25.2 | 300 | bowl | same | Passion Fruit Glazed Duck | abnormals_delight |
| Nourishment | 14 | 0.9 | 25.2 | 300 | bowl | same | Perch with Mushrooms | abnormals_delight |
| Nourishment | 14 | 0.9 | 25.2 | 240 | bowl | same | Plate of Stuffed Hoglin Snout | nethersdelight |
| Nourishment | 14 | 0.8 | 22.4 | 180 | bowl | same | Cooked Rice with Beef | moredelight |
| Nourishment | 14 | 0.8 | 22.4 | 180 | bowl | same | Cooked Rice with Chicken Cuts | moredelight |
| Nourishment | 14 | 0.8 | 22.4 | 180 | bowl | same | Cooked Rice with Porkchop | moredelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Bowl of Stuffed Pumpkin | farmersdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Coffee-Braised Beef | rusticdelight |
| Nourishment | 14 | 0.75 | 21 | 180 | bowl | same | Grilled Salmon | farmersdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Plate of Honey Glazed Ham | farmersdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Plate of Roast Chicken | farmersdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Plate of Shepherd's Pie | farmersdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Roasted Mutton Chops | farmersdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Seasoned Arthropods | miners_delight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Squid Ink Pasta | farmersdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Vegetable Noodles | farmersdelight |
| Nourishment | 13 | 0.9 | 23.4 | 180 | - | same | Deluxe Cheeseburger | delightful |
| Nourishment | 12 | 1 | 24 | 300 | bowl | same | Cornbread Stuffing | corn_delight |
| Nourishment | 12 | 1 | 24 | 300 | bowl | same | Nachos | corn_delight |
| Nourishment | 12 | 1 | 24 | 180 | bowl | same | Pasta With Veggieballs | miners_delight |
| Nourishment | 12 | 0.9 | 21.6 | 300 | bowl | same | Seared Venison | abnormals_delight |
| Nourishment | 12 | 0.8 | 19.2 | 300 | bowl | Comfort | Acacia Blossom Soup | alexsdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Bell Pepper Pasta | rusticdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Creamy Pasta with Chicken Cuts | moredelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Creamy Pasta with Ham | moredelight |
| Nourishment | 12 | 0.8 | 19.2 | 300 | bowl | same | Duck Noodles | abnormals_delight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | Comfort | Fig Chicken Stew | fruitsdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Fried Calamari | rusticdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Fried Chicken | rusticdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Fried Mushrooms | rusticdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Lychee Chicken | fruitsdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Mushroom Rice | farmersdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Orange Chicken | fruitsdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Orange Marinated Pork | fruitsdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Pasta with Meatballs | farmersdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Pasta with Mutton Chop | farmersdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Pineapple Marinated Pork | fruitsdelight |
| Nourishment | 12 | 0.8 | 19.2 | 300 | bowl | same | Plate Of Glazed Arachnid Limbs | miners_delight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Steak and Potatoes | farmersdelight |
| Nourishment | 12 | 0.6 | 14.4 | 300 | bowl | same | Bowl of Steamed Dragon Egg | ends_delight |
| Nourishment | 12 | 0.6 | 14.4 | 180 | bowl | same | Vegan Steak And Potatoes | miners_delight |
| Nourishment | 11 | 0.9 | 19.8 | 300 | bowl | same | Venison with Bamboo Shoots | abnormals_delight |
| Nourishment | 11 | 0.3 | 6.6 | 30 | - | same | Chunkwich | delightful |
| Nourishment | 10 | 0.9 | 18 | 180 | bowl | same | Black Cod | farmersrespite |
| Nourishment | 10 | 0.9 | 18 | 120 | bowl | same | Grilled Strider | nethersdelight |
| Nourishment | 10 | 0.8 | 16 | 300 | bowl | same | Bowl Of Stuffed Squid | miners_delight |
| Nourishment | 10 | 0.8 | 16 | 180 | bowl | same | Dune Platter | abnormals_delight |
| Nourishment | 10 | 0.8 | 16 | 60 | - | same | Golden Nutritional Bar | miners_delight |
| Nourishment | 10 | 0.8 | 16 | 300 | bowl | same | Kangaroo Pasta | alexsdelight |
| Nourishment | 10 | 0.8 | 16 | 300 | bowl | Comfort | Kangaroo Stew | alexsdelight |
| Nourishment | 10 | 0.8 | 16 | 300 | bowl | same | Lobster Pasta | alexsdelight |
| Nourishment | 10 | 0.8 | 16 | 180 | bowl | same | Stir Fried Shulker Meat | ends_delight |
| Nourishment | 10 | 0.75 | 15 | 120 | bowl | same | Plate of Stuffed Hoglin Ham | nethersdelight |
| Nourishment | 10 | 0.7 | 14 | 300 | bowl | same | Plate of Dragon Leg with Sauce | ends_delight |
| Nourishment | 10 | 0.6 | 12 | 60 | bowl | same | Bacon and Eggs | farmersdelight |
| Nourishment | 10 | 0.6 | 12 | 300 | bowl | Comfort | Bowl of Dragon Meat Stew | ends_delight |
| Nourishment | 10 | 0.6 | 12 | 60 | bowl | same | Ratatouille | farmersdelight |
| Nourishment | 10 | 0.55 | 11 | 30 | bowl | same | Field Salad | delightful |
| Nourishment | 10 | 0.55 | 11 | 30 | - | same | Nutritional Bar | miners_delight |
| Nourishment | 10 | 0.5 | 10 | 180 | - | same | Nut Butter and Jam Sandwich | delightful |
| Nourishment | 10 | 0.4 | 8 | 180 | bowl | Comfort | Cave Soup | miners_delight |
| Nourishment | 9 | 0.7 | 12.6 | 60 | bowl | same | Pike with Beetroot | abnormals_delight |
| Nourishment | 9 | 0.5 | 9 | 180 | - | same | S'more | delightful |
| Nourishment | 8 | 0.65 | 10.4 | 60 | bowl | same | Plate Of Fake Meatloaf | miners_delight |
| Nourishment | 8 | 0.6 | 9.6 | 300 | bowl | same | Plate of Grilled Shulker | ends_delight |
| Nourishment | 8 | 0.6 | 9.6 | 120 | bowl | same | Plate of Stuffed Hoglin Roast | nethersdelight |
| Nourishment | 8 | 0.45 | 7.2 | 60 | bowl | Comfort | Insect Stew | miners_delight |
| Nourishment | 7 | 0.9 | 12.6 | 300 | bowl | same | Bowl of Stuffed Shulker | endersdelight |
| Nourishment | 7 | 0.7 | 9.8 | 180 | bowl | Comfort | Enderman Gristle Stew | ends_delight |
| Nourishment | 7 | 0.45 | 6.3 | 180 | bowl | Comfort | Bat Soup | miners_delight |
| Nourishment | 6 | 0.4 | 4.8 | 90 | copper_cup | Comfort | Onion Soup Cup | miners_delight |
| Nourishment | 5 | 0.3 | 3 | 150 | copper_cup | Comfort | Rabbit Stew Cup | miners_delight |
| Nourishment | 5 | 0.2 | 2 | 90 | copper_cup | Comfort | Cave Soup Cup | miners_delight |
| Nourishment | 4 | 0.4 | 3.2 | 300 | shulker_bowl | same | Ender Paella | endersdelight |
| Nourishment | 4 | 0.4 | 3.2 | 300 | bowl | same | Ender Paella | endersdelight |
| Nourishment | 4 | 0.4 | 3.2 | 300 | shulker_bowl | same | Pearl Pasta | endersdelight |
| Nourishment | 4 | 0.4 | 3.2 | 300 | bowl | same | Pearl Pasta | endersdelight |
| Nourishment | 4 | 0.225 | 1.8 | 30 | copper_cup | Comfort | Insect Stew Cup | miners_delight |
| Nourishment | 3 | 0.3 | 1.8 | 90 | copper_cup | Comfort | Beetroot Soup Cup | miners_delight |
| Nourishment | 3 | 0.3 | 1.8 | 90 | copper_cup | Comfort | Mushroom Stew Cup | miners_delight |
| Nourishment | 3 | 0.225 | 1.35 | 90 | copper_cup | Comfort | Bat Soup Cup | miners_delight |
| Nourishment | 1 | 0.1 | 0.2 | 10 | - | same | Slice of Pumpkin Cake | create_central_kitchen |
| Nourishment | 1 | 0.05 | 0.1 | 10 | - | same | Moss | miners_delight |
| Nourishment | 0 | 0 | 0 | 60 | glass_bottle | Comfort | Azalea Tea | delightful |

### A.2 Ported foods (1.20-era values, comparison only, 64)

| Effect | Hunger | Mod | Sat | Seconds | Hands back | Form rule says | Food | Mod id |
|---|---:|---:|---:|---:|---|---|---|---|
| Comfort | 14 | 0.8 | 22.4 | 480 | bowl | same | Overnight Oats | vintagedelight |
| Comfort | 14 | 0.75 | 21 | 300 | bowl | Nourishment | Cacciatore | veggiesdelight |
| Comfort | 14 | 0.75 | 21 | 300 | bowl | same | Turnip Beef Stew | veggiesdelight |
| Comfort | 13 | 0.85 | 22.1 | 300 | bowl | same | Pickle Soup | vintagedelight |
| Comfort | 12 | 0.8 | 19.2 | 180 | bowl | same | Garlic Chicken Stew | veggiesdelight |
| Comfort | 12 | 0.8 | 19.2 | 300 | bowl | same | Ghostly Chili | vintagedelight |
| Comfort | 12 | 0.8 | 19.2 | 180 | bowl | Nourishment | Potato Noodles | veggiesdelight |
| Comfort | 12 | 0.8 | 19.2 | 180 | bowl | Nourishment | Sweet Potato Pancakes | veggiesdelight |
| Comfort | 10 | 0.7 | 14 | 30 | bowl | same | Broccoli Soup | veggiesdelight |
| Comfort | 10 | 0.7 | 14 | 30 | bowl | same | Cauliflower Soup | veggiesdelight |
| Comfort | 10 | 0.7 | 14 | 30 | bowl | same | Creamy Morel Soup | fungidelight |
| Comfort | 10 | 0.7 | 14 | 30 | bowl | Nourishment | Mashed Potatoes | veggiesdelight |
| Comfort | 8 | 0.75 | 12 | 300 | bowl | same | Oatmeal | vintagedelight |
| Comfort | 8 | 0.7 | 11.2 | 300 | empty_mason_jar | Nourishment | Apple Sauce Jar | vintagedelight |
| Comfort | 8 | 0.6 | 9.6 | 180 | bowl | Nourishment | Coleslaw | veggiesdelight |
| Comfort | 7 | 0.65 | 9.1 | 300 | empty_mason_jar | Nourishment | Glow Berries Jam Jar | vintagedelight |
| Comfort | 7 | 0.65 | 9.1 | 300 | empty_mason_jar | Nourishment | Sweet Berries Jam Jar | vintagedelight |
| Comfort | 3 | 0.7 | 4.2 | 60 | glass_bottle | Nourishment | Apple Sauce Bottle | vintagedelight |
| Comfort | 2 | 0.6 | 2.4 | 60 | glass_bottle | Nourishment | Glow Berries Jam Bottle | vintagedelight |
| Comfort | 2 | 0.6 | 2.4 | 60 | glass_bottle | Nourishment | Sweet Berries Jam Bottle | vintagedelight |
| both | 7 | 0.7 | 9.8 | 180 | empty_mason_jar | Nourishment | Relish Jar | vintagedelight |
| both | 2 | 0.7 | 2.8 | 30 | glass_bottle | Nourishment | Relish Bottle | vintagedelight |
| Nourishment | 16 | 0.55 | 17.6 | 300 | bowl | same | Horror Lasagna | brewinandchewin |
| Nourishment | 15 | 0.95 | 28.5 | 300 | - | same | The Burger | vintagedelight |
| Nourishment | 14 | 0.9 | 25.2 | 180 | - | same | Stuffed Burrito | vintagedelight |
| Nourishment | 14 | 0.75 | 21 | 420 | bowl | same | Fiery Fondue | brewinandchewin |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Garlic Rice With Cauliflower | veggiesdelight |
| Nourishment | 14 | 0.75 | 21 | 180 | bowl | same | Lasagna Slice | veggiesdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Mutton Chops with Truffle | fungidelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Pasta with Cheese | brewinandchewin |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Pork Marsala With Mushrooms | fungidelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Rabbit with Morels | fungidelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Rice And Vegetables | veggiesdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | bowl | same | Stuffed Zucchinis | veggiesdelight |
| Nourishment | 14 | 0.75 | 21 | 300 | - | same | Turnip Cake | veggiesdelight |
| Nourishment | 12 | 1 | 24 | 420 | bowl | same | Scarlet Pierogi | brewinandchewin |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Cauliflower Kuku | veggiesdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | Comfort | Creamy Onion Soup | brewinandchewin |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Pasta With Broccoli | veggiesdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Roasted Vegetables | veggiesdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Shakshouka | veggiesdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Steak And Broccoli | veggiesdelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Steak With Mushrooms | fungidelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Stuffed Morels | fungidelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Truffle Pasta | fungidelight |
| Nourishment | 12 | 0.8 | 19.2 | 180 | bowl | same | Vegetable Omelet | brewinandchewin |
| Nourishment | 12 | 0.75 | 18 | 300 | bowl | same | Pad Thai | vintagedelight |
| Nourishment | 11 | 0.8 | 17.6 | 300 | - | same | Century Egg | vintagedelight |
| Nourishment | 10 | 0.9 | 18 | 180 | bowl | same | Inky Cap Scrambled Eggs | fungidelight |
| Nourishment | 10 | 0.6 | 12 | 30 | bowl | same | Dandelion Greens And Eggs | veggiesdelight |
| Nourishment | 10 | 0.6 | 12 | 30 | bowl | same | Fish And Chips | veggiesdelight |
| Nourishment | 10 | 0.6 | 12 | 300 | bowl | same | Garlic Baked Cod | veggiesdelight |
| Nourishment | 9 | 0.75 | 13.5 | 180 | bowl | same | Cheese Pasta | vintagedelight |
| Nourishment | 9 | 0.7 | 12.6 | 180 | empty_mason_jar | same | Pepper Jam Jar | vintagedelight |
| Nourishment | 8 | 0.65 | 10.4 | 300 | - | same | Stuffed Bell Pepper | veggiesdelight |
| Nourishment | 6 | 0.65 | 7.8 | 30 | - | same | Pickled Egg | vintagedelight |
| Nourishment | 6 | 0.3 | 3.6 | 30 | - | same | Pickle | vintagedelight |
| Nourishment | 6 | 0.3 | 3.6 | 30 | - | same | Pickled Pepper | vintagedelight |
| Nourishment | 6 | 0.2 | 2.4 | 30 | - | same | Pickled Onion | vintagedelight |
| Nourishment | 5 | 0.75 | 7.5 | 30 | - | same | Meat Pizza Slice | vintagedelight |
| Nourishment | 5 | 0.4 | 4 | 30 | - | same | Pickled Beetroot | vintagedelight |
| Nourishment | 4 | 0.75 | 6 | 30 | - | same | Cheese Pizza Slice | vintagedelight |
| Nourishment | 3 | 0.2 | 1.2 | 180 | empty_mason_jar | same | Vinegar Jar | vintagedelight |
| Nourishment | 1 | 0.2 | 0.4 | 60 | glass_bottle | same | Vinegar Bottle | vintagedelight |
| Nourishment | 1 | 0 | 0 | 120 | tankard | same | Pale Jane | brewinandchewin |
| Nourishment | 1 | 0 | 0 | 60 | tankard | same | Rice Wine | brewinandchewin |

### B. R5 on every bowl meal (recommended numbers)

"Rule" shows the effect under R5 plus the owner rules of 9.0 (bold where it differs from today). Input hunger is the
total hunger of the inputs, output hunger is what the bowl restores. "Seconds rule" is 60 for Comfort at 7 output hunger or less, otherwise 120 below 10 output hunger and
240 from 10 up; "-" means no effect.

#### B.1 Bowl foods with a recipe (191)

| Now | Rule | Inputs | Input hunger | Output hunger | Seconds now | Seconds rule | Food | Mod | Recipe |
|---|---|---:|---:|---:|---:|---:|---|---|---|
| Nourishment | Nourishment | 8 | 56 | 12 | 300 | 240 | Plate Of Glazed Arachnid Limbs | miners_delight | crafting_shapeless (feast, 4 servings) |
| Nourishment | Nourishment | 8 | 36 | 14 | 300 | 240 | Plate of Honey Glazed Ham | farmersdelight | crafting_shapeless (feast, 4 servings) |
| Nourishment | Nourishment | 8 | 29 | 14 | 300 | 240 | Plate of Roast Chicken | farmersdelight | crafting_shapeless (feast, 4 servings) |
| Nourishment | Nourishment | 8 | 27 | 14 | 300 | 240 | Plate of Shepherd's Pie | farmersdelight | crafting_shapeless (feast, 4 servings) |
| Nourishment | **Comfort** | 7 | 17 | 8 | 300 | 120 | Plate of Grilled Shulker | ends_delight | crafting_shaped (feast, 4 servings) |
| Nourishment | Nourishment | 6 | 20 | 12 | 300 | 240 | Bowl of Steamed Dragon Egg | ends_delight | cooking (feast, 4 servings) |
| Nourishment | Nourishment | 6 | 15 | 14 | 180 | 240 | Lasagna Slice | veggiesdelight | cooking (feast, 4 servings) |
| Nourishment | Nourishment | 6 | 14 | 18 | 300 | 240 | Roasted Dragon Steak | ends_delight | crafting_shapeless |
| Nourishment | Nourishment | 6 | 13 | 10 | 30 | 240 | Field Salad | delightful | crafting_shapeless |
| Nourishment | Nourishment | 6 | 13 | 12 | 180 | 240 | Pineapple Marinated Pork | fruitsdelight | cooking |
| Nourishment | Nourishment | 6 | 11 | 10 | 300 | 240 | Bowl of Dragon Meat Stew | ends_delight | cooking (feast, 4 servings) |
| Nourishment | Nourishment | 6 | 11 | 12 | 180 | 240 | Orange Marinated Pork | fruitsdelight | cooking |
| Nourishment | Nourishment | 6 | 11 | 12 | 180 | 240 | Roasted Vegetables | veggiesdelight | cooking |
| Nourishment | Nourishment | 6 | 10 | 12 | 180 | 240 | Lychee Chicken | fruitsdelight | cooking |
| Nourishment | Nourishment | 6 | 9 | 12 | 180 | 240 | Orange Chicken | fruitsdelight | cooking |
| Nourishment | Nourishment | 6 | 8 | 10 | 300 | 240 | Bowl Of Stuffed Squid | miners_delight | cooking (feast, 5 servings) |
| Nourishment | Nourishment | 5 | 17 | 12 | 180 | 240 | Vegetable Omelet | brewinandchewin | cooking |
| Nourishment | Nourishment | 5 | 15 | 16 | 300 | 240 | Horror Lasagna | brewinandchewin | cooking |
| Nourishment | Nourishment | 5 | 14 | 10 | 30 | 240 | Dandelion Greens And Eggs | veggiesdelight | crafting_shapeless |
| Nourishment | Nourishment | 5 | 13 | 12 | 300 | 240 | Duck Noodles | abnormals_delight | crafting_shapeless |
| Nourishment | Nourishment | 5 | 12 | 12 | 300 | 240 | Nachos | corn_delight | cooking (feast, 4 servings) |
| Nourishment | Nourishment | 5 | 11 | 14 | 300 | 240 | Bowl of Stuffed Pumpkin | farmersdelight | cooking (feast, 4 servings) |
| Nourishment | Nourishment | 5 | 11 | 12 | 300 | 240 | Pad Thai | vintagedelight | cooking |
| Nourishment | Nourishment | 5 | 10 | 12 | 420 | 240 | Scarlet Pierogi | brewinandchewin | cooking |
| Nourishment | Nourishment | 5 | 8 | 14 | 300 | 240 | Pork Marsala With Mushrooms | fungidelight | cooking |
| Nourishment | Nourishment | 5 | 8 | 14 | 300 | 240 | Rabbit with Morels | fungidelight | cooking |
| Nourishment | Nourishment | 5 | 8 | 14 | 300 | 240 | Rice And Vegetables | veggiesdelight | cooking |
| Nourishment | Nourishment | 4 | 17 | 12 | 180 | 240 | Steak and Potatoes | farmersdelight | crafting_shaped |
| Nourishment | Nourishment | 4 | 16 | 10 | 60 | 240 | Bacon and Eggs | farmersdelight | crafting_shapeless |
| Nourishment | Nourishment | 4 | 16 | 12 | 180 | 240 | Vegan Steak And Potatoes | miners_delight | crafting_shapeless |
| Nourishment | Nourishment | 4 | 12 | 12 | 300 | 240 | Cornbread Stuffing | corn_delight | cooking |
| Nourishment | Nourishment | 4 | 12 | 12 | 180 | 240 | Creamy Pasta with Ham | moredelight | cooking |
| Nourishment | Nourishment | 4 | 12 | 14 | 300 | 240 | Garlic Rice With Cauliflower | veggiesdelight | crafting_shapeless |
| Nourishment | Nourishment | 4 | 11 | 12 | 180 | 240 | Creamy Onion Soup | brewinandchewin | cooking |
| Nourishment | Nourishment | 4 | 11 | 14 | 300 | 240 | Roasted Mutton Chops | farmersdelight | crafting_shapeless |
| Nourishment | **Comfort** | 4 | 11 | 8 | 60 | 120 | Plate Of Fake Meatloaf | miners_delight | cooking (feast, 4 servings) |
| Nourishment | Nourishment | 4 | 11 | 12 | 180 | 240 | Cauliflower Kuku | veggiesdelight | cooking |
| Nourishment | Nourishment | 4 | 10 | 10 | 180 | 240 | Black Cod | farmersrespite | crafting_shapeless |
| Nourishment | Nourishment | 4 | 9 | 12 | 180 | 240 | Truffle Pasta | fungidelight | cooking |
| Nourishment | Nourishment | 4 | 9 | 10 | 300 | 240 | Garlic Baked Cod | veggiesdelight | crafting_shapeless |
| Nourishment | Nourishment | 4 | 8 | 14 | 300 | 240 | Pasta with Cheese | brewinandchewin | cooking |
| Nourishment | Nourishment | 4 | 8 | 14 | 180 | 240 | Grilled Salmon | farmersdelight | crafting_shapeless |
| Nourishment | Nourishment | 4 | 8 | 12 | 180 | 240 | Steak With Mushrooms | fungidelight | cooking |
| Nourishment | **Comfort** | 4 | 5 | 14 | 300 | 240 | Glow Ink Pasta | miners_delight | cooking |
| Nourishment | Nourishment | 4 | 8 | 12 | 180 | 240 | Creamy Pasta with Chicken Cuts | moredelight | cooking |
| Nourishment | Nourishment | 4 | 8 | 14 | 300 | 240 | Stuffed Zucchinis | veggiesdelight | cooking |
| Nourishment | **Comfort** | 5 | 7 | 14 | 300 | 240 | Vegetable Noodles | farmersdelight | cooking |
| Nourishment | **Comfort** | 4 | 7 | 12 | 180 | 240 | Fried Chicken | rusticdelight | cooking |
| Nourishment | **Comfort** | 4 | 6 | 7 | 180 | 60 | Enderman Gristle Stew | ends_delight | cooking |
| Nourishment | **Comfort** | 4 | 6 | 14 | 300 | 240 | Mutton Chops with Truffle | fungidelight | cooking |
| Nourishment | **Comfort** | 4 | 6 | 14 | 300 | 240 | Coffee-Braised Beef | rusticdelight | cooking |
| Nourishment | **Comfort** | 4 | 6 | 12 | 180 | 240 | Fried Calamari | rusticdelight | cooking |
| Nourishment | **Comfort** | 4 | 6 | 12 | 180 | 240 | Shakshouka | veggiesdelight | cooking |
| Nourishment | **Comfort** | 4 | 5 | 10 | 180 | 240 | Stir Fried Shulker Meat | ends_delight | cooking |
| Nourishment | **Comfort** | 4 | 5 | 10 | 60 | 240 | Ratatouille | farmersdelight | cooking |
| Nourishment | **Comfort** | 4 | 5 | 12 | 180 | 240 | Bell Pepper Pasta | rusticdelight | cooking |
| Nourishment | **Comfort** | 3 | 14 | 10 | 180 | 240 | Inky Cap Scrambled Eggs | fungidelight | crafting_shapeless |
| Nourishment | **Comfort** | 3 | 12 | 12 | 180 | 240 | Steak And Broccoli | veggiesdelight | crafting_shapeless |
| Nourishment | **Comfort** | 3 | 10 | 10 | 300 | 240 | Kangaroo Pasta | alexsdelight | cooking |
| Nourishment | **Comfort** | 3 | 10 | 9 | 180 | 120 | Cheese Pasta | vintagedelight | cooking |
| Nourishment | **Comfort** | 3 | 9 | 12 | 180 | 240 | Pasta With Veggieballs | miners_delight | cooking |
| Nourishment | **Comfort** | 3 | 8 | 10 | 300 | 240 | Kangaroo Stew | alexsdelight | cooking |
| Nourishment | **Comfort** | 3 | 8 | 10 | 300 | 240 | Lobster Pasta | alexsdelight | cooking |
| Nourishment | **Comfort** | 3 | 8 | 12 | 180 | 240 | Pasta with Meatballs | farmersdelight | cooking |
| Nourishment | **Comfort** | 3 | 8 | 12 | 180 | 240 | Pasta With Broccoli | veggiesdelight | cooking |
| Nourishment | **Comfort** | 3 | 7 | 12 | 180 | 240 | Pasta with Mutton Chop | farmersdelight | cooking |
| Nourishment | **Comfort** | 3 | 7 | 12 | 180 | 240 | Fig Chicken Stew | fruitsdelight | cooking |
| Nourishment | **Comfort** | 3 | 6 | 14 | 180 | 240 | Cooked Rice with Porkchop | moredelight | cooking |
| Nourishment | **Comfort** | 3 | 5 | 14 | 180 | 240 | Cooked Rice with Beef | moredelight | cooking |
| Nourishment | **Comfort** | 5 | 4 | 12 | 180 | 240 | Stuffed Morels | fungidelight | cooking |
| Nourishment | **Comfort** | 5 | 2 | 10 | 120 | 240 | Grilled Strider | nethersdelight | cooking |
| Nourishment | **Comfort** | 4 | 4 | 14 | 300 | 240 | Squid Ink Pasta | farmersdelight | cooking |
| Nourishment | **Comfort** | 4 | 4 | 12 | 180 | 240 | Fried Mushrooms | rusticdelight | cooking |
| Nourishment | **Comfort** | 4 | 3 | 10 | 180 | 240 | Cave Soup | miners_delight | cooking |
| Nourishment | **Comfort** | 4 | 2 | 14 | 300 | 240 | Perch with Mushrooms | abnormals_delight | cooking |
| Nourishment | **-** | 4 | 2 | 9 | 60 | - | Pike with Beetroot | abnormals_delight | cooking |
| Nourishment | **Comfort** | 4 | 1 | 12 | 180 | 240 | Mushroom Rice | farmersdelight | cooking |
| Nourishment | **Comfort** | 3 | 4 | 12 | 300 | 240 | Acacia Blossom Soup | alexsdelight | cooking |
| Nourishment | **Comfort** | 3 | 4 | 14 | 180 | 240 | Cooked Rice with Chicken Cuts | moredelight | cooking |
| Nourishment | **Comfort** | 3 | 2 | 10 | 30 | 240 | Fish And Chips | veggiesdelight | cooking |
| Nourishment | **-** | 2 | 10 | 8 | 60 | - | Insect Stew | miners_delight | cooking |
| Nourishment | **Comfort** | 2 | 10 | 14 | 300 | 240 | Seasoned Arthropods | miners_delight | crafting_shapeless |
| Nourishment | **-** | 2 | 2 | 7 | 180 | - | Bat Soup | miners_delight | cooking |
| Comfort | **Nourishment** | 6 | 8 | 15 | 300 | 240 | Sinigang | delightful | cooking |
| Comfort | **Nourishment** | 5 | 14 | 16 | 180 | 240 | Dragon's Breath and Chorus Soup | ends_delight | cooking |
| Comfort | **Nourishment** | 5 | 12 | 13 | 300 | 240 | Pickle Soup | vintagedelight | cooking |
| Comfort | Comfort | 5 | 11 | 8 | 180 | 120 | Ender Noodle | ends_delight | cooking |
| Comfort | **Nourishment** | 5 | 10 | 12 | 300 | 240 | Ghostly Chili | vintagedelight | cooking |
| Comfort | **Nourishment** | 4 | 9 | 14 | 300 | 240 | Noodle Soup | farmersdelight | cooking |
| Comfort | **Nourishment** | 4 | 9 | 14 | 300 | 240 | Pumpkin Soup | farmersdelight | cooking |
| Comfort | Comfort | 4 | 8 | 8 | 180 | 120 | Coleslaw | veggiesdelight | crafting_shapeless |
| Comfort | Comfort | 6 | 6 | 8 | 60 | 120 | Pear with Rock Sugar | fruitsdelight | cooking |
| Comfort | Comfort | 4 | 7 | 10 | 180 | 240 | Corn Soup | corn_delight | cooking |
| Comfort | Comfort | 4 | 6 | 14 | 300 | 240 | Chicken Soup | farmersdelight | cooking |
| Comfort | Comfort | 4 | 6 | 12 | 180 | 240 | Vegetable Soup | farmersdelight | cooking |
| Comfort | Comfort | 4 | 6 | 9 | 60 | 120 | Mangosteen Cake | fruitsdelight | crafting_shapeless |
| Comfort | Comfort | 4 | 5 | 14 | 300 | 240 | Egg Fried Rice | farmersdelight | cooking |
| Comfort | Comfort | 4 | 5 | 10 | 30 | 240 | Creamy Morel Soup | fungidelight | cooking |
| Comfort | Comfort | 3 | 12 | 12 | 180 | 240 | Potato Noodles | veggiesdelight | cooking |
| Comfort | Comfort | 3 | 12 | 12 | 180 | 240 | Sweet Potato Pancakes | veggiesdelight | cooking |
| Comfort | Comfort | 3 | 9 | 12 | 180 | 240 | Carrot Soup | moredelight | cooking |
| Comfort | Comfort | 3 | 8 | 10 | 30 | 240 | Broccoli Soup | veggiesdelight | cooking |
| Comfort | Comfort | 3 | 8 | 12 | 180 | 240 | Garlic Chicken Stew | veggiesdelight | cooking |
| Comfort | Comfort | 3 | 7 | 12 | 180 | 240 | Fish Stew | farmersdelight | cooking |
| Comfort | Comfort | 3 | 7 | 10 | 30 | 240 | Mashed Potatoes | veggiesdelight | cooking |
| Comfort | Comfort | 3 | 7 | 14 | 300 | 240 | Turnip Beef Stew | veggiesdelight | cooking |
| Comfort | Comfort | 3 | 6 | 12 | 180 | 240 | Beef Stew | farmersdelight | cooking |
| Comfort | Comfort | 3 | 6 | 10 | 30 | 240 | Cauliflower Soup | veggiesdelight | cooking |
| Comfort | Comfort | 3 | 5 | 7 | 180 | 60 | Creamed Corn | corn_delight | cooking |
| Comfort | Comfort | 3 | 5 | 10 | 180 | 240 | Ender Congee | ends_delight | cooking |
| Comfort | Comfort | 3 | 5 | 14 | 300 | 240 | Cacciatore | veggiesdelight | cooking |
| Comfort | Comfort | 6 | 4 | 15 | 180 | 240 | Bowl of Guardian Soup | oceansdelight | cooking (feast, 4 servings) |
| Comfort | Comfort | 6 | 1 | 14 | 300 | 240 | Bowl of Pineapple Fried Rice | fruitsdelight | cooking (feast, 4 servings) |
| Comfort | **-** | 5 | 2 | 8 | 120 | - | Strider Moss Stew | nethersdelight | cooking |
| Comfort | **-** | 4 | 4 | 6 | 180 | - | Bell Pepper Soup | rusticdelight | cooking |
| Comfort | Comfort | 4 | 3 | 14 | 300 | 240 | Baked Cod Stew | farmersdelight | cooking |
| Comfort | **-** | 3 | 3 | 8 | 180 | - | Shulker Soup | ends_delight | cooking |
| Comfort | **-** | 2 | 2 | 8 | 60 | - | Bone Broth | farmersdelight | cooking |
| Comfort | **-** | 2 | 2 | 8 | 300 | - | Oatmeal | vintagedelight | cooking |
| Comfort | **-** | 1 | 0 | 6 | 30 | - | Cooked Rice | farmersdelight | cooking |
| - | **Nourishment** | 9 | 10 | 4 | - | 120 | Golden Rabbit Stew | goldenfood | crafting_shaped |
| - | **Comfort** | 6 | 11 | 6 | - | 60 | Fruit Salad | farmersdelight | crafting_shapeless |
| - | **Nourishment** | 6 | 11 | 12 | - | 240 | Neapolitan Ice Cream | neapolitan | crafting_shapeless |
| - | **Comfort** | 4 | 14 | 8 | - | 120 | Cucumber Salad | vintagedelight | crafting_shapeless |
| - | **Nourishment** | 4 | 11 | 10 | - | 240 | Rabbit Stew | minecraft | cooking |
| - | **Comfort** | 4 | 8 | 6 | - | 60 | Adzuki Curry | neapolitan | cooking |
| - | **Nourishment** | 9 | 6 | 4 | - | 120 | Golden Beetroot Soup | goldenfood | crafting_shaped |
| - | **Nourishment** | 9 | 6 | 4 | - | 120 | Golden Mushroom Stew | goldenfood | crafting_shaped |
| - | **Comfort** | 5 | 7 | 10 | - | 240 | End Mixed Salad | ends_delight | crafting_shapeless |
| - | **Comfort** | 5 | 6 | 6 | - | 60 | Maggot Salad | alexsdelight | crafting_shapeless |
| - | **Comfort** | 4 | 7 | 10 | - | 240 | Glowgurt | berry_good | crafting_shapeless |
| - | **Comfort** | 4 | 7 | 12 | - | 240 | Takoyaki | miners_delight | cooking |
| - | **Comfort** | 4 | 7 | 6 | - | 60 | Chicken Salad | moredelight | crafting_shapeless |
| - | **Comfort** | 4 | 7 | 10 | - | 240 | Diced Potatoes with Egg and Tomato | moredelight | cooking |
| - | **Comfort** | 4 | 7 | 6 | - | 60 | Chocolate Ice Cream | neapolitan | crafting_shapeless |
| - | **Comfort** | 4 | 7 | 10 | - | 240 | Cesar Salad | veggiesdelight | crafting_shapeless |
| - | **Comfort** | 4 | 6 | 6 | - | 60 | Adzuki Ice Cream | neapolitan | crafting_shapeless |
| - | **Comfort** | 4 | 6 | 6 | - | 60 | Strawberry Ice Cream | neapolitan | crafting_shapeless |
| - | **Comfort** | 4 | 6 | 8 | - | 120 | Potato Salad | rusticdelight | crafting_shapeless |
| - | **Comfort** | 4 | 5 | 6 | - | 60 | Matcha Ice Cream | delightful | crafting_shapeless |
| - | **Comfort** | 4 | 5 | 6 | - | 60 | Salmonberry Ice Cream | delightful | crafting_shapeless |
| - | **Comfort** | 4 | 5 | 4 | - | 60 | Dog Food | farmersdelight | cooking |
| - | **Comfort** | 4 | 5 | 7 | - | 60 | Truffle Ice Cream | fungidelight | crafting_shapeless |
| - | **Comfort** | 4 | 5 | 6 | - | 60 | Banana Ice Cream | neapolitan | crafting_shapeless |
| - | **Comfort** | 4 | 5 | 6 | - | 60 | Mint Ice Cream | neapolitan | crafting_shapeless |
| - | **Comfort** | 3 | 7 | 9 | - | 120 | Warped Moldy Meat | nethersdelight | crafting_shapeless |
| - | **Comfort** | 3 | 5 | 10 | - | 240 | Diced Potatoes with Porkchop | moredelight | cooking |
| - | **Comfort** | 3 | 5 | 6 | - | 60 | Broccoli Salad | veggiesdelight | crafting_shapeless |
| - | **Comfort** | 6 | 4 | 10 | - | 240 | Blazing Chili | farmersrespite | cooking |
| - | **Comfort** | 6 | 4 | 10 | - | 240 | Tea Curry | farmersrespite | cooking |
| - | - | 5 | 2 | 8 | - | - | Adzuki Stew | neapolitan | cooking |
| - | - | 4 | 4 | 6 | - | - | Vanilla Ice Cream | neapolitan | crafting_shapeless |
| - | - | 4 | 3 | 2 | - | - | Batter | rusticdelight | cooking |
| - | **Comfort** | 3 | 4 | 10 | - | 240 | Diced Potatoes with Beef | moredelight | cooking |
| - | - | 3 | 3 | 6 | - | - | Mixed Salad | farmersdelight | crafting_shapeless |
| - | - | 3 | 3 | 6 | - | - | Scrambled Eggs | incubation | cooking |
| - | - | 3 | 3 | 6 | - | - | Beetroot Soup | minecraft | cooking |
| - | - | 3 | 3 | 5 | - | - | Weird Caviar | miners_delight | crafting_shapeless |
| - | **Comfort** | 3 | 3 | 10 | - | 240 | Diced Potatoes with Chicken Cuts | moredelight | cooking |
| - | **Comfort** | 3 | 3 | 10 | - | 240 | Mashed Potatoes | moredelight | cooking |
| - | - | 3 | 2 | 6 | - | - | Potato Salad | moredelight | cooking |
| - | - | 3 | 1 | 6 | - | - | Vanilla Pudding | neapolitan | crafting_shapeless |
| - | - | 3 | 0 | 5 | - | - | Sopa De Macaco | alexsmobs | crafting_shapeless |
| - | - | 2 | 5 | 8 | - | - | Inky Caps Salad | fungidelight | crafting_shapeless |
| - | - | 2 | 3 | 3 | - | - | Chorus Sauce | ends_delight | cooking |
| - | - | 2 | 3 | 6 | - | - | Turnip Salad | veggiesdelight | crafting_shapeless |
| - | - | 2 | 2 | 4 | - | - | Tomato Sauce | farmersdelight | cooking |
| - | - | 2 | 0 | 5 | - | - | Nether Salad | farmersdelight | crafting_shapeless |
| - | - | 2 | 0 | 6 | - | - | Mushroom Stew | minecraft | cooking |
| - | - | 1 | 8 | 1 | - | - | Apple Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Bayberry Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Blueberry Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Chorus Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Cranberry Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Durian Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Fig Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Glowberry Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Hamimelon Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Hawberry Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Kiwi Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Lemon Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Lychee Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Mango Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Mangosteen Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Melon Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Orange Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Peach Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Pear Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Persimmon Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Pineapple Jello | fruitsdelight | crafting_shapeless |
| - | - | 1 | 8 | 1 | - | - | Sweetberry Jello | fruitsdelight | crafting_shapeless |

#### B.2 Bowl foods with no recipe found (19, hand check)

The three Enchanted Golden soups left this list: rule 4 gives them Nourishment 240 s without a recipe. The rest keep
today's effect until checked.

| Now | Food | Mod |
|---|---|---|
| Nourishment | Dune Platter | abnormals_delight |
| Nourishment | Passion Fruit Glazed Duck | abnormals_delight |
| Nourishment | Seared Venison | abnormals_delight |
| - | Slabdish | abnormals_delight |
| Nourishment | Venison with Bamboo Shoots | abnormals_delight |
| Nourishment | Fiery Fondue | brewinandchewin |
| Comfort | Coconut Curry | delightful |
| Comfort | Chorus Stew | endersdelight |
| Nourishment | Ender Paella | endersdelight |
| Comfort | Endermite Stew | endersdelight |
| Nourishment | Pearl Pasta | endersdelight |
| Nourishment | Bowl of Stuffed Shulker | endersdelight |
| - | Twisted Cereal | endersdelight |
| Nourishment | Plate of Dragon Leg with Sauce | ends_delight |
| - | Mango Salad | fruitsdelight |
| Nourishment | Plate of Stuffed Hoglin Ham | nethersdelight |
| Nourishment | Plate of Stuffed Hoglin Roast | nethersdelight |
| Nourishment | Plate of Stuffed Hoglin Snout | nethersdelight |
| Comfort | Overnight Oats | vintagedelight |

### C. Foods outside the bowl scope (rules applied 2026-10-27)

Owner decision 2026-10-27: the R5 rules (9.0) also apply to **mason jars**, **shulker bowls** and **copper cup
meals**. **Bottles** are being removed from the pack, so every Comfort / Nourishment effect on a bottle is dropped.
**Every other food loses Nourishment** (it keeps Comfort). Cans copy their source food (CN-D8), so the 26 Nourishment
cans follow their source: cans of the foods in C.5 lose it, cans of bowls follow B.1.

How the rules read for these forms:

- **Inputs / input hunger** come from the cooking pot recipe of the food (jars, shulker bowls: the container is not an
  input; sugar and other non-food inputs count 0 hunger but still count as a slot). A **copper cup has no recipe**: it
  is a serving of its bowl, so it takes the **bowl's** inputs and input hunger from B.1 (Onion Soup Cup = Creamy Onion
  Soup; Beetroot Soup, Mushroom Stew and Rabbit Stew cups = the vanilla bowls) and its **own** output hunger.
- Output hunger decides Nourishment (10+) and the length (under 10 -> 120 s, 10 and up -> 240 s). No jar, shulker bowl
  or cup restores 10, so **none of them can be Nourishment, and all of them get 120 s** when they get an effect. The
  fallback Comfort (10+ output hunger) never applies to them either.
- Vinegar Jar has 3 inputs worth 3 hunger and restores 3, so it gets no effect.

#### C.1 Mason jars (6)

| Now | Rule | Output hunger | Inputs | Input hunger | Seconds now | Seconds rule | Hands back | Food | Mod |
|---|---|---:|---:|---:|---:|---:|---|---|---|
| Comfort | Comfort | 8 | 6 | 20 | 300 | 120 | empty_mason_jar | Apple Sauce Jar | vintagedelight |
| Comfort | Comfort | 7 | 6 | 10 | 300 | 60 | empty_mason_jar | Glow Berries Jam Jar | vintagedelight |
| Comfort | Comfort | 7 | 6 | 10 | 300 | 60 | empty_mason_jar | Sweet Berries Jam Jar | vintagedelight |
| Nourishment | **Comfort** | 9 | 6 | 30 | 180 | 120 | empty_mason_jar | Pepper Jam Jar | vintagedelight |
| Nourishment | **-** | 3 | 3 | 3 | 180 | - | empty_mason_jar | Vinegar Jar | vintagedelight |
| both | **Comfort** | 7 | 6 | 30 | - | 60 | empty_mason_jar | Relish Jar | vintagedelight |

Jam jars keep their other effects (Glowing, Strength); only Comfort / Nourishment change. The Relish Jar gave both
effects and now gives Comfort only. Comfort at 7 hunger or less lasts 60 s (rule 5). Two jars had no effect and are not
in the table: Honey Jar (3 inputs worth 18, restores 10) gets Comfort 240 s through the fallback, Nut Mash Jar (6 inputs
worth 20, restores 8) Comfort 120 s.

#### C.2 Shulker bowls (4)

| Now | Rule | Output hunger | Inputs | Input hunger | Seconds now | Seconds rule | Hands back | Food | Mod |
|---|---|---:|---:|---:|---:|---:|---|---|---|
| Comfort | **-** | 4 | 2 | 4 | 300 | - | shulker_bowl | Chorus Stew | endersdelight |
| Comfort | **-** | 4 | 2 | 1 | 300 | - | shulker_bowl | Endermite Stew | endersdelight |
| Nourishment | **-** | 4 | 4 | 1 | 300 | - | shulker_bowl | Ender Paella | endersdelight |
| Nourishment | **-** | 4 | 3 | 2 | 300 | - | shulker_bowl | Pearl Pasta | endersdelight |

All four have too few inputs (2 to 4) and too little input hunger (1 to 4), and restore 4, so none gets an effect.
The bowl versions of Chorus Stew and Endermite Stew (listed without a recipe in B.2) share these recipes and follow the
same result: no effect. Hand-check them with B.2.

#### C.3 Copper cup meals (15)

| Now | Rule | Output hunger | Inputs | Input hunger | Seconds now | Seconds rule | Hands back | Food | Mod |
|---|---|---:|---:|---:|---:|---:|---|---|---|
| Comfort | **-** | 7 | 4 | 3 | 150 | - | copper_cup | Baked Cod Stew Cup | miners_delight |
| Comfort | Comfort | 6 | 3 | 6 | 90 | 60 | copper_cup | Beef Stew Cup | miners_delight |
| Comfort | **-** | 4 | 2 | 2 | 30 | - | copper_cup | Bone Broth Cup | miners_delight |
| Comfort | Comfort | 7 | 4 | 6 | 150 | 60 | copper_cup | Chicken Soup Cup | miners_delight |
| Comfort | Comfort | 6 | 3 | 7 | 90 | 60 | copper_cup | Fish Stew Cup | miners_delight |
| Comfort | Comfort | 7 | 4 | 9 | 150 | 60 | copper_cup | Noodle Soup Cup | miners_delight |
| Comfort | Comfort | 7 | 4 | 9 | 150 | 60 | copper_cup | Pumpkin Soup Cup | miners_delight |
| Comfort | Comfort | 6 | 4 | 6 | 90 | 60 | copper_cup | Vegetable Soup Cup | miners_delight |
| Nourishment | **-** | 3 | 2 | 2 | 90 | - | copper_cup | Bat Soup Cup | miners_delight |
| Nourishment | **-** | 3 | 3 | 3 | 90 | - | copper_cup | Beetroot Soup Cup | miners_delight |
| Nourishment | **-** | 5 | 4 | 3 | 90 | - | copper_cup | Cave Soup Cup | miners_delight |
| Nourishment | **-** | 4 | 2 | 10 | 30 | - | copper_cup | Insect Stew Cup | miners_delight |
| Nourishment | **-** | 3 | 2 | 0 | 90 | - | copper_cup | Mushroom Stew Cup | miners_delight |
| Nourishment | **Comfort** | 6 | 4 | 11 | 90 | 60 | copper_cup | Onion Soup Cup | miners_delight |
| Nourishment | **Comfort** | 5 | 4 | 11 | 150 | 60 | copper_cup | Rabbit Stew Cup | miners_delight |

The empty **Copper Cup** itself is crafted from 3 copper ingots (shaped crafting, recipe `miners_delight:copper_cup`).
It is the cup meals' container, so like the jar and the shulker bowl it is not counted as an input.

Cups are all under 10 output hunger, so Nourishment is impossible and a cup is Comfort (60 s at 7 hunger or less, else 120 s) or bare. Eight cups end
with Comfort (six keep it, Onion Soup and Rabbit Stew switch from Nourishment); seven end bare (Baked Cod Stew and Bone
Broth lose Comfort, five Nourishment cups lose Nourishment).

#### C.4 Bottles (10): every effect removed

| Now | Output hunger | Food | Mod |
|---|---:|---|---|
| Comfort | 2 | Creamy Corn Drink | corn_delight |
| Comfort | 0 | Lavender Tea | delightful |
| Comfort | 0 | Dandelion Tea | farmersrespite |
| Comfort | 0 | Dandelion Tea | farmersrespite |
| Comfort | 3 | Apple Sauce Bottle | vintagedelight |
| Comfort | 2 | Glow Berries Jam Bottle | vintagedelight |
| Comfort | 2 | Sweet Berries Jam Bottle | vintagedelight |
| Nourishment | 0 | Azalea Tea | delightful |
| Nourishment | 1 | Vinegar Bottle | vintagedelight |
| both | 2 | Relish Bottle | vintagedelight |

All ten lose Comfort / Nourishment (7 Comfort, 2 Nourishment, Relish Bottle both). Bottles themselves are to be removed
from the pack.

#### C.5 Every other food: Nourishment removed (25)

| Now | Rule | Output hunger | Hands back | Food | Mod |
|---|---|---:|---|---|---|
| Comfort | Comfort | 1 | - | Slice of Yucca Cake | create_central_kitchen |
| Comfort | Comfort | 15 | - | Cabbage Wrapped Elder Guardian | oceansdelight |
| Nourishment | - | 1 | - | Slice of Pumpkin Cake | create_central_kitchen |
| Nourishment | - | 11 | - | Chunkwich | delightful |
| Nourishment | - | 13 | - | Deluxe Cheeseburger | delightful |
| Nourishment | - | 10 | - | Nut Butter and Jam Sandwich | delightful |
| Nourishment | - | 9 | - | S'more | delightful |
| Nourishment | - | 15 | - | Ender Bamboo Rice | ends_delight |
| Nourishment | Nourishment | 10 | - | Golden Nutritional Bar | miners_delight |
| Nourishment | - | 1 | - | Moss | miners_delight |
| Nourishment | - | 10 | - | Nutritional Bar | miners_delight |
| Nourishment | - | 8 | - | Stuffed Bell Pepper | veggiesdelight |
| Nourishment | - | 14 | - | Turnip Cake | veggiesdelight |
| Nourishment | - | 11 | - | Century Egg | vintagedelight |
| Nourishment | - | 4 | - | Cheese Pizza Slice | vintagedelight |
| Nourishment | - | 5 | - | Meat Pizza Slice | vintagedelight |
| Nourishment | - | 6 | - | Pickle | vintagedelight |
| Nourishment | - | 5 | - | Pickled Beetroot | vintagedelight |
| Nourishment | - | 6 | - | Pickled Egg | vintagedelight |
| Nourishment | - | 6 | - | Pickled Onion | vintagedelight |
| Nourishment | - | 6 | - | Pickled Pepper | vintagedelight |
| Nourishment | - | 14 | - | Stuffed Burrito | vintagedelight |
| Nourishment | - | 15 | - | The Burger | vintagedelight |
| Nourishment | - | 1 | tankard | Pale Jane | brewinandchewin |
| Nourishment | - | 1 | tankard | Rice Wine | brewinandchewin |

23 Nourishment effects dropped (cakes, sandwiches, burgers, bars, pickles, pizza, Century Egg, Moss, the two tankard
drinks). The two Comfort foods (Slice of Yucca Cake, Cabbage Wrapped Elder Guardian) keep Comfort. Slice of Yucca Cake restores 1, so its Comfort lasts 60 s (rule 5); the Elder Guardian wrap keeps its own length.

- 2026-10-28: planned from the scratchpad: other effects on high-value meals, Nourishment being on bowls only (TODO `food/types-and-tuning #18`). No code changed.

## 11. Complete outline of the split (2026-11-02, for the build of `textures/food #5`)

Puts the owner's 2026-11-02 answers (section 5) and the rules built on 2026-10-27 (sections 9, 10) in one ordered list. Nothing is
built from this section yet. Questions A to D are answered (11.4).

### 11.1 Effect choice, first match wins (the form rule is the fallback)

| # | Test | Result | Source |
|---|---|---|---|
| 1 | Bottle (any food in a bottle) | no effect | R5 C.4 |
| 2 | Moss; Vintage Relish (bottle and jar) | no effect | CN-D5 |
| 3 | Golden food / enchanted golden food | Nourishment only, 120 s / 240 s | R5 rule 4 |
| 4 | **Input test (R5, as built):** bowl, mason jar, shulker bowl or copper cup with a recipe: 4+ inputs, input hunger 8+, output 10+ Nourishment; 3+ inputs, input hunger 5+ Comfort; output 10+ Comfort | Nourishment / Comfort, 120 s or 240 s | R5 9.0; any result other than "neither" counts as set |
| 5 | **Form rule, fallback:** nothing above set an effect (R5 said "neither", or the food has no recipe, or it is not a bowl form) -> by food type, 11.6 | Comfort or Nourishment | owner 2026-11-02 (R1, CN-D1 to CN-D5) |
| 6 | Still nothing: formless | no effect, marked `needs_effect` (11.7) | owner 2026-11-02 |

Cans copy their source food (`gen_can_types.py`). Copper cup meals take their bowl's result.

### 11.2 Durations

| Case | Length | Source |
|---|---|---|
| Output hunger under 10 | 120 s | R5 rule 3 |
| Output hunger 10 or more | 240 s | R5 rule 3 |
| Comfort left on a food restoring 7 or less | 60 s | R5 rule 5 |
| Golden / enchanted golden | 120 s / 240 s | R5 rule 4 |
| Short Comfort of row 9 (vanilla soups) | 30 s | CN-D4, owner 2026-11-02 |

### 11.3 What the owner's answers change against what is built

| Built 2026-10-27 | After the answers |
|---|---|
| Nourishment only on bowls, jars, shulker bowls, cups by the input test; 23 other Nourishment effects dropped (C.5: cakes, sandwiches, burgers, bars, pickles, pizza, Century Egg, Moss, tankards) | the form rule decides those foods instead: sandwiches, burgers, pizza slices Nourishment; bars, cake slices, jams Comfort; pickles, Century Egg, tankards, Moss, Relish none (11.7) |
| Bowl effect from recipe inputs (Comfort 5+ hunger / 3+ inputs, Nourishment 8+ / 4+ and output 10+, fallback Comfort at output 10+) | **unchanged**; the form rule only fills in where the input test sets nothing |
| Vanilla soups: no effect (one input) | short Comfort |
| Relish bottle / jar: effect kept | dropped |

### 11.4 Owner answers (2026-11-02, later)

- **A. The form rule is a fallback, not a replacement (owner, 2026-11-02, correcting the first answer).** The input test (R5, as built)
  and the explicit rules come first; the form rule sets the effect type only where none of them did. R5's durations, the golden rule and the
  60 s light-Comfort rule stay. Consequence: a salad or meat stew that the input test already gives Nourishment keeps it; the form rows
  of section 5 (bars and salads Comfort, meat rice and stew cutoff 10) apply only to foods the input test leaves bare.
- **B. Vanilla soups (Mushroom Stew, Beetroot Soup, Rabbit Stew, and their cans and cups) get Comfort 30 s.** The 30 s is for those
  three only; the 60 s rule (Comfort on a food restoring 7 or less) still applies to every other light Comfort food.
- **C. A new food is classified by its food type tag** (`#creativedelight:food_type/...`, `food_types.json`), no named list for the
  type itself. Only the exceptions that a type cannot express (meat rice and stew, plain rice, bars) are a small named list.
- **D. Formless foods lose Comfort and Nourishment** and are all marked to get another effect later (11.7).

### 11.6 Where the form rule acts as the fallback

Dry run over the 226 ruled foods of the 2026-10-24 dump (input test as built; the dump predates the newest ports, the meat test for
rice and stews is by name). The input test sets 163 of them; the form rule fires on the other 63, plus the 21 bowls with no recipe
(B.2, which the input test cannot read), plus foods that are not a bowl form at all.

| Food type / form | Form rule (fallback) | Fires on (of the 63) |
|---|---|---:|
| `sweet` in a bowl or jar (puddings, custards, jam jars) | Comfort | 22 |
| `soup`: soups, broths, porridge, oats, curry, chili | Comfort; vanilla Mushroom Stew, Beetroot Soup, Rabbit Stew 30 s | 14 |
| Rice or stew without meat | Comfort | 10 |
| `salad` | Comfort | 5 |
| `meal` and `meal/feast`: pasta, plated dishes | Nourishment | 4 |
| Rice or stew with meat | own hunger 9 or less Comfort, 10 or more Nourishment | 1 |
| Ingredient jars, sauces, batter, eggs, dog food | none (formless, 11.7) | 7 |
| The 21 bowls with no recipe | by type, as above | 21 |
| Foods that are not a bowl form | by type: `handheld` (sandwich, burger, burrito, pizza slice) Nourishment; bars and `sweet` slices Comfort | see scope below |

Where the input test already set an effect nothing changes: the 56 Nourishment and 107 Comfort bowls it assigns stay. Durations follow
output hunger as in 11.2.

**Scope of the fallback (assumption, 2026-11-02):** it applies to bowl forms and to foods that carry Comfort or Nourishment today. It does not
add an effect to every food of a type: the dump has 178 non-bowl `sweet`, 99 `handheld`, 29 `meal` and 15 `baked` foods that have none, and
a blanket rule would give them all one. A new food gets an effect by its type only if its type is in the table above and it is a bowl form;
widening it to every `handheld` or `meal` food is one more decision.

### 11.7 Formless foods: no Comfort, no Nourishment, marked for a new effect

A food that fits no row of 11.1 loses both effects. Every one is marked `needs_effect` (a list in the generator output, to be read when
the new-effect pass runs; TODO `food/types-and-tuning #25`). Marked as of 2026-11-02:

| Group | Foods |
|---|---|
| Pickles and preserves | Pickle, Pickled Beetroot, Pickled Egg, Pickled Onion, Pickled Pepper (Vintage Delight), Century Egg |
| Sauces, batter and ingredient jars | Tomato Sauce, Chorus Sauce, Batter, Apple Sauce Jar, Nut Mash Jar, Vinegar Jar, Honey Jar |
| Drinks without a form | Pale Jane, Rice Wine (tankards, 1 hunger) |
| Already stripped by owner rule | Moss (neither), Vintage Relish bottle and jar (effect dropped) |
| Not for players | Dog Food |

Foods that the fallback does place (sandwiches, burgers, burritos, pizza slices: Nourishment; bars, cake slices, jams, S'more: Comfort) are
not formless. The new effect for each marked food is a later decision; until then they have no effect.

### 11.5 Build once answered

Extend `tools/gen_comfort_nourishment.py` with the rows above, then `gen_can_types.py` after a fresh `/dumpall` (4 Large Meals soups are
missing from `/dumpfood`); startup applier `comfort_nourishment.js` unchanged in shape; tests CNS-09 and following; changelog bullet;
README and TODO `textures/food #5` / `food/types-and-tuning #15` updated.

