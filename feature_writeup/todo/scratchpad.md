# Scratchpad

## Prompt scratchpad, dont touch

I am an agent from the food-decisions session (2026-11-17 to 2026-11-18). It ran four rounds of owner rulings on
the food and cooking TODO rows (commits 77f21b1f, 40fefae2, 5c65bb80, 506bc933, 8294ef0f on agent_staging). Goal:
walk the owner through the remaining open food and cooking decisions, showing the possible approaches for each one,
and record every ruling on its row.

Context:
- Rulings sit at the front of a row's decisions field as "decided <date> (owner[, round N]): ...". A decisions field
  that only names a question ("which cheeses", "recipe, uses") is open.
- The list below comes from a filter on the decisions field and has not been checked yet. Some rows may already be
  settled in their details text, a writeup, or a plan.
- Food routing, family rules and past rulings: feature_writeup/food_families.md, the briefs in
  feature_writeup/briefs/ (named in each row's writeup field), and the agent notes index (CLAUDE.md imports it).

Method:
1. Run `python3 tools/session_role.py plan` first.
2. Before presenting a row, run `python3 tools/todo.py show <family> '#<n>'` (the id is two words). Read the writeup it
   links (grep, never whole). Check the dumps in kubejs/exported/ for what already exists: item ids in
   tags/item_tags_by_item.json, recipe ids in recipes/all.csv, tags in tags/item_tags.json. Drop the row if it already
   has a ruling, and correct its premise if the dumps or docs disagree.
3. For each remaining row, work out 2-4 concrete approaches: what each one builds, which existing items or systems it
   reuses, and what it costs (new textures, Java vs KubeJS, overlap with other rows). Put the recommended approach
   first.
4. Present the approaches with AskUserQuestion, 4 questions per call, 2-4 options each, recommended option marked.
   Make each question standalone: put the row id and one line of context in the question text, and the trade-off in
   each option's description. Use multiSelect for "which items" lists, split across questions when there are more
   than 4 candidates. Go one block at a time, in the order below.
5. Record each answer:
   `python3 tools/todo.py set <family> '#<n>' "decisions=decided <today> (owner, round 5): <ruling>; <old decisions text>"`.
   A skipped question stays unrecorded. For a "show me first" answer, do the lookup and re-ask in a later batch. New
   work the owner names becomes a row via `python3 tools/todo.py add --family "<layout.md heading>" ...`, cited in the
   ruling by its new id.
6. Textures in rulings follow the CLAUDE.md texture rule: copies of existing human art or the owner's tools only.
7. After each block: `python3 tools/todo.py lint`, `python3 tools/status_index.py`. Commit the row files you changed
   (by path; other sessions have uncommitted edits in archive.csv, mods_and_ports.csv and possibly others, so
   check `git diff` per file and never stage their hunks), then commit TODO.md separately with [no-stale]. Push
   with `git push origin HEAD:agent_staging`. If the push is not a fast-forward, stop and ask. No changelog bullet
   (rulings only).

Return: the rulings per row id, the rows dropped as already settled (with where the ruling was found), the new
row ids, and the commit hashes.

{represents notes added by owner, take as round 1 answer to open decision}

Open rows, by block:

A. Baking and sweets
- baking/baked-goods #34: every cake as a sequenced assembly: the transition item per cake {incomplete cake, cake base texture}
- baking/baked-goods #36: dessert cookware and recipe treatments: which cookware items
- baking/baked-goods #5: cakes: the dragon egg cake's effects
- baking/baked-goods #14: unobtainable pie slices: add a source or hide, per slice
- baking/baked-goods #2: muffins as a decent food source: which variants
- baking/baked-goods #18, #19, #20: brownie and cupcake, quiche, pizza types: recipes and textures (pizza also oven
  or sequenced assembly)
- baking/sweets #3: cold sweets: which variants
- baking/sweets #7: marshmallow on a stick: bring back to evaluate, then keep or drop {campfire roasting mechanic, initiate 1 block from campfire and move player to fixed distance and angles [side or corner], fire heat speed is based on cursor position on screen, faster at top and near flames, slower at bottom and slight distance away [coals ], different cursor position maps for both angles, soul campfire map is inverted, heat value increases in marshmellow by rate defined by heat map, small threshold between roasted and burnt}
- sweets #8, #9 (smoothie line, brain freeze; ruled 2026-11-18 in plan_smoothies.md): only the items the rows mark
  "Unconfirmed" (Rage Aura in mango smoothies; whether a hit resets the brain freeze count)

B. Ingredients
- dish-merges #1: burger and patty merge: the merge set and what each burger keeps
- dishes/ingredients #9: cheese: which cheeses
- dishes/ingredients #17: tofu from mushrooms: recipe
- dishes/ingredients #27: potato dough: recipe and uses (ties to dishes/meals #33, gnocchi)
- dishes/ingredients #31: glowing sauce: ingredient and uses
- dishes/ingredients #37: new chopped items (onion and which others)
- dishes/ingredients #22: honeycomb as sugar, honey and spread: which recipes
- dishes/ingredients #30: refined gelatin -> gelatin: recipes and rates
- dishes/ingredients #33: cucumber dough -> veggie dough: which veggies

C. Meals
- dishes/meals #13, #14, #15: skewer, patty, salad and wrap food types: recipes and which foods
- dishes/meals #18: cheese in nachos: recipe shape
- dishes/meals #20: feasts in ovens: which feasts
- dishes/meals #52: creamy onion soup: which station makes it
- dishes/meals #66: soup vs stew naming (stew has meat): the final rename list
- dishes/meals #21: meal-specific ingredients audit: keep or open up, per meal
- dishes/meals #24: extra veggie omelette: exact recipe
- dishes/meals #29: fermented veg in uncooked crafts: which crafts

D. Ocean, pickles and ferments
- dishes/ocean #6: caviar use audit: which recipes
- dishes/ocean #10: shrimp trap: block, bait, output
- dishes/ocean #13: fugu poisoning chance: which items count as fugu
- dishes/pickles-and-ferments #5: enzymes from crushed or milled mushrooms: which mushrooms, which machines
- dishes/pickles-and-ferments #13: pickled egg and century egg: check what exists first

E. Drinks
- food/drinks #3: drink merges: which item wins each merge
- food/drinks #18: drink crates: yes or no, which drinks
- food/drugs #1: smoking: the tolerance mechanic

F. Food rules and tuning
- food/seasoning #22: grilling raw meat for the grilled effect, burnt meat: which meats
- food/seasoning #23: sauces on more meat-like foods: the whitelist
- food/types-and-tuning #9: cultured meat has no nutrition: values
- food/types-and-tuning #18: other effects on high-value meals: which effects, which meals
- food/types-and-tuning #20: short effects for seafood: which foods, which effects
- food/types-and-tuning #24: golden food buffs by family: the family -> effect table
- food/types-and-tuning #25: formless foods: which effect each gets
- food/types-and-tuning #13: food families D1 to D4 in the writeup (decision rows, family list, sandwich bread
  shapes, foods without a family)
- food/placeable #6: coaster and small plate compatibility: which mods

G. Kitchen and cans
- cooking/kitchen #11: kitchen decor: source
- cooking/kitchen #16: scythe: areas per tier vs the hoe_harvest tags
- cooking/oven #2: oven crafting pattern (bakeware is hammered): final patterns
- cans/canning #1: can values for meal, pie, feast, drink and hazard cans: the goal per section
- cans/canning #8: canning automation: design

H. Farming and fruit (food-adjacent)
- farming/crops #10: melon and pumpkin growth stages: whether
- farming/crops #25: mini melon crop: recipe and slice count (mini cantaloupe is 4 slices, fruit #3)
- farming/crops #43: produce storage: sack vs crate split
- merges/fruit #2: the apple merge and the green apple source
- farming/composting #2: Fertile Soul Soil (Nulch) recipe: the new inputs
- farming/composting #7: organic waste from cutting recipes: which recipes, fertilizer or not


## Loading Tooltip language, need to read all tooltips for manual overrides

## Idea, Notes, Changes (Human scratchpad, don't write up or add to plans unless directed)

- unbuilt
    - double fridges
    - tooltips chat announce

- small biome o plenty tweaks
    - large lily pads, spawn in biomes at own rate in own systems
        - lotus? flowers too, pink flower in swamp
    - clovers
        - ensure existing 4 leaf clovers are spawnable, investiagate if 3 leaf clovers are availible
    - any web blocks?

- corpses decay to skeleton model after configurable time, decays faster if corpse is empty
    - keeps armor on skeleton?
- randomly spawning corpses underground in world, skeletons

- wire sag adjustment
    - config option for immersive wire system
    - screwdriver on connector loosens/tightens sag of all connected wires on crouch/stand

- tamed cats drop whiskers
    - brewable w/ speed 2 for speed 3 potions

## Tempad/portals WIP dont move

- tempad notes
    - change chronons to [chronitons](https://futurama.fandom.com/wiki/Chronitons)
    - allow knowledge projector recipes to update based on actual recipe
    - change knowledge projector lore (remove TVA)
    - change tempad time to in game time
    - multiple chronometers cancel out chronitons charging
    - add braclet curios slot
    - enlarge knowledge projector screen
    - fix 'hold {X} chronotons' knowledge projector
    - cant remove card from wallet?
    - tooltips for every item, based on knowledge projector
    - timedoor marker
        - name and color fields dont render until clicked, color updates inconsistanly when using color selection screen
    - rename timesteel to temporal steel
        - change recipe to felsic/steel alloy
            - ender pearl or ender grain additive?
    - mark a note to make recipes much much more complex, intermediate steps, unusual craft steps
    - change 
    - potential issues
        - default app doesnt work?
        - only 1 macro option?
        - workstation is locked?
        - cant use empty or marked location card on tempad
    - change knowledge base categories (iron vs steel) to basic vs advanced. make advanced recipes harder and nerf basic storage capacity or etc?
    - use projector in timedoor projector, timedoor projector in tempad
    - swap timedoor projector door projection angle 180 degrees (behind station., not ahead)
    - need to test location boradcaster in multiplayer
    - ensure all apps are added to tempad
    - timeline missing languange in tempad

- tempad in server
    - chronomarker used for mining/resource dimension navigation, more valuable resource dimensions availible w/ own tempad (unlock location cards? wandering trader rarely, or found in end chests?)

## Golden Buffs, WIP Dont move

- golden food buffs
    - all effects and saturation/hunger are identical within family
    - fruit
        - golden: regen 2 5s,  speed 2 30s, nourishment 1 30s
        - gilded: regen 3 20s, speed 3 60s, nourishment 1 60s
    - veggie
        - golden: regen 2 5s,  jump boost 2 30s, nourishment 1 30s
        - gilded: regen 3 20s, jump boost 3 60s, nourishment 1 60s
    - meat/fish
        - golden: absorption 2 60s,  resistance 2 15s, nourishment 1 60s
        - gilded: absorption 4 120s, resistance 4 60s, nourishment 1 120s
    - sweets
        - golden: regen 2 5s,  speed 2 15s, nourishment 1 30s
        - gilded: 
    - meals
        - golden: regen 2 5s,  speed 2 15s, nourishment 1 30s
        - gilded: 

    - exceptions
        - golden carrot: jump boost to night vision (60/120s)

- current gold food buffs

    - Current effects (from kubejs/exported/food/foods.json dump, 2026-11-07). Hunger shown as golden / gilded.

    - Standard golden effects: 
            Regeneration II 5s, Absorption I 120s, Nourishment I 120s

    - Standard gilded (enchanted) effects: 
            Regeneration II 20s, Resistance I 300s, Fire Resistance I 300s, Absorption IV 120s, Nourishment I 240s

    - Every item below has the standard effects unless a note says otherwise.

    - fruit
        - Golden / Gilded Apple (8 / 16)
        - Golden / Gilded Melon Slice (4 / 8)
        - Glistering Melon Slice (4): Nourishment I 120s only (pack item, no gilded form)
        - Golden / Gilded Sweet Berries (4 / 8)
        - Golden / Gilded Glow Berries (4 / 8)
        - Golden / Gilded Chorus Fruit (8 / 16)
    - veggie
        - Golden / Gilded Carrot (6 / 12)
        - Golden / Gilded Beetroot (2 / 4)
        - Golden / Gilded Baked Potato (10 / 20)
        - Golden / Gilded Dried Kelp (2 / 4)
        - Golden Moss Clump (4): Regeneration II 10s, Nourishment I 120s (no absorption)
        - Gilded Moss Clump (6): Absorption III 80s, Regeneration II 15s, Water Breathing III 80s, Nourishment I 240s (not the standard gilded set)
    - meat/fish
        - Golden / Gilded Cooked Beef (16 / 20)
        - Golden / Gilded Cooked Porkchop (16 / 20)
        - Golden / Gilded Cooked Mutton (12 / 20)
        - Golden / Gilded Cooked Salmon (12 / 20)
        - Golden / Gilded Cooked Chicken (12 / 20)
        - Golden / Gilded Cooked Rabbit (10 / 20)
        - Golden / Gilded Cooked Cod (10 / 20)
    - sweets
        - Golden / Gilded Cookie (4 / 8)
        - Golden / Gilded Honey Bottle (12 / 20)
        - Golden / Gilded Pumpkin Pie (4 / 8 in the dump; golden_food.md 3.6 expects 16 / 20 from the pie's input, check whether the dump is stale)
        - Golden Cake slice (Java, GoldenCakeBlock): 4 hunger, Regeneration II 5s, Absorption II 120s
        - Enchanted Golden Cake slice: 8 hunger, Regeneration II 20s, Resistance I 300s, Fire Resistance I 300s, Absorption IV 120s
        - Golden Roll / Golden Roll Slice (Roll Delight): no effects
    - meals/prepared
        - Golden / Gilded Bread (10 / 20)
        - Golden Nutritional Bar (10): Nourishment I 120s only (Miner's Delight, no gilded form)
    - not sorted into a family
        - Golden Coffee Beans (Rustic Delight): no effects
        - Golden / Gilded Milk Bucket: listed with the standard effects but they never apply (milk clears effects, never "eats")
    - NO SOUPS OR STEWS, these are removed. Still present in the dump with the standard effects: Golden / Gilded Beetroot Soup (12 / 20), Mushroom Stew (12 / 20), Rabbit Stew (20 / 20)
    - Canned versions (CE canned goods) carry scaled-down effects: Regeneration II 1.25s, Absorption I 30s, Nourishment I 30s (moss can: Regeneration II 2.5s, Nourishment I 30s; nutritional bar can: Nourishment I 30s). Canned Golden Apples (canned_goods) has no effects in the dump.
