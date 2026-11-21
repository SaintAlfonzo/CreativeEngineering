# Scratchpad

## Prompt scratchpad, dont touch

speed, haste, strength, jump boost, regen, resistance, fire resistance, water breathing, invisibility (one of the highest levels), night vision, absorption, satiated (final effect, permanant effect at 'max' diversity, essentially locks diet if no food is eaten, not brewable), glowing, luck, slow falling, dolphin grace (high level permanant), bug pheromones, clinging, fleet-footed, kb resistance, lava vision, poison resistance, long reach, patrol nullifier, phantom bane, allure, emptiness (high), feather feet, raging, sweet heart, stimulation (refreshing and sweetening super), magic resistance, all fruit delight (rename astringent to steady, 

I am an agent picking up the food-decisions walkthrough (round 6, started 2026-11-19; the prior agent committed c5af0057 on agent_staging). Goal: walk the owner through the remaining open food and cooking TODO decisions with AskUserQuestion, and record every ruling on its row.

Read the previous agent's brief in full: the owner's task prompt "food-decisions session round 6" is reproduced in this repo's session scratchpad if kept; otherwise use the method below.

Method: run python3 tools/session_role.py plan. Before each row, run python3 tools/todo.py show <family> '#<n>' (two shell words), read its brief section in feature_writeup/briefs/ (each has a "Decisions" recommendation), and check kubejs/exported/ dumps. Drop rows that already carry a ruling. Present 2-4 approaches per row, recommended first, 4 questions per call, standalone question text with the row id. Record with python3 tools/todo.py set <family> '#<n>' "decisions=decided <today> (owner, round 6): <ruling>; <old text>". New work becomes a row via todo.py add. After each block: todo.py lint, status_index.py, commit row files by path (other sessions have uncommitted edits), push with git push origin HEAD:agent_staging; stop and ask if it is not a fast-forward. No changelog bullet.

Already done in round 6: dishes/ingredients #9, #17, #27 recorded; #33 dropped (settled by R17).

Still open, in order:

Block B remainder: dishes/ingredients #31 (glowing sauce: add glow berries / new fluid / drop; seasoning #27 already made it a skewer sauce), #37 (which chopped items: beetroot, carrot, tomato, apple; onion vs prepared onion), #22 (honeycomb: 2-sugar heated mixer, toast spread, honey slot), #30 (gelatin: machine route / one-step / rates).
Block C: dishes/meals #13 (recommend the type now, recipes with #46), #14 (recommend a tag, every generic patty recipe), #15, #18 (recommend replacing milk with #forge:cheese, pot only), #20 (recommend every feast with a cooked main, baking sheet), #66, #21, #24, #29.
Blocks D to J as listed in the original prompt: ocean and pickles, drinks, food rules and tuning, kitchen and cans, farming, then the two follow-up blocks from the 2026-11-19 four-plan run (food batch; diet books, corpses, scratch driver). Record block I and J rulings in the owning plan's rulings table as well as the row.
Last: dish-merges #1 (burger and patty merge). The owner deferred it to the end of the queue. The merge options are: merge duplicates and keep unique meats; patty tag only; one burger per archetype; or show recipes, values and sprites first. Ask it last, and ask it together with meals #14 (the patty tag decision depends on it).
Settled, do not re-ask: every round-5 Block A row (baking/baked-goods #2, #5, #14, #18, #19, #20, #34, #36; baking/sweets #3, #7; sweets #8, #9), dishes/ingredients #33, dishes/meals #52, dishes/ocean #13, and the three rows recorded above.

## Idea, Notes, Changes (Human scratchpad, don't write up or add to plans unless directed)

- bop flesh and porus flesh burn to rotten flesh block on contact w/ lava
- parcool mod
    - optional, dont add now
    - enable certain moves with curios item equiped?
- expand waystone selection page size (check decompiled)
- sophisticated backpack crafting table restock feature from newer version
- peppercorn? nether crop?
- dragon armor/other armor effects reapply to 40 second length at 10 second remaining
- pickle aging
    - 3 pickles (any kind) + vinegar and salt in jar
    - long recipe, several days
    - adds the 'mature' flavor, +2 sat for now

- cheese aging
    - place and wait to age
    - aged flavor doesnt transfer to recipes
    - required for some recipes?
- raw pufferfish has same poison chance as raw sliced, 25% on all raw with tiny chance on all cooked
    - new effect and custom damage type toxin, wither w/ poison heart color, slowness 4, nausea
    - custom death messages from toxin
    - other toxin sources?
    - toxin antidote?
- chopstick use jei screen
- armor added by non equiped means (frost armor for example) should get a new texture, swap color to pure white?
- cant place sweet potato pancakes?
- remove canned stuffed bellpeppers
- raspberry pie as its own mod, ships apple pie and carrot as submods, publish soon

- diversity
    - smelting adds small bonus, useful for small value foods but not large
    - seasoning doesnt affect
    - ensure apple pie calculation rules are followed for all foods


- even more rp
    - pull ladder and milestone horizontal bounds in another 10%
    - increase 32 meal icon size by 20%
    - milestone text color for active buffs (effect slots, permanant effects, hearts, resistance, etc) show in green 
    - permanant buffs also alternate between diamond shard and effect on milestones and ladder before unlocked

- more rasp pie
    - no hover renders on first page
    - shrink page width 25%
    - pull in horizontal text bounds of ladder and milestones page 25% to prevent overlap

- rasp pie (intake 2026-11-19 -> plan_diet_books_r2.md)
    - merge raspberry pie changelog lines
    - expand food log horizontally by 50%
    - shrink text size of 'current diversity' and 'need x foods' text on first page, keep both on their own line w/ no overflow
    - diet log freezes for short time on each open, carrot dict does on first open but not after. caching needed? other issue?
    - liquid diet log should also pause game in sp
    - food total shouldnt show up on 30 meal hover if its the same as this meal
    - hover order is food total(only present if different from hovered food meal value), this meal(crossed out if food total is present)
    - show food diversity tag on food items between shanks and meal description (hearty meal, snack)
    - queue unlock messages fired when food eaten threshold is not passed
    - diversity value food icon ONLY pulls from carrot food dict history, otherwise a pufferfish
        - pick food closest in value to current diversity scaled to food values (eating many low value foods to get a higer diversity is still shown as a low value food, eating mid diversity meals and getting high diversity still shows mid diversity meals)
        - never pick drinks
    - last 30 meals label should be centered
    - change last meals to 8 columns by 4 rows, 32 last meals count
    - include pancakes (treat as single pancake) and all other in world eaten foods in diet calcs 
    - item tags in game show diversity value and contribution when present
    - sort carrot options for highest food, saturation, diversity
        - applied to uneaten, old 'same as uneaten' renamed to custom and applied to uneaten too, not shown if not configured (mark note to configure)
- move food diversity tag on food items between shanks and meal description (hearty meal, snack) (intake 2026-11-19 -> plan_diet_books_r2.md)
- move permanant effects (from armor, charm, beacon, etc) to the right of the timed effects in a new column. dont push over if only permanant effects are applied (intake 2026-11-19 -> plan_diet_books_r2.md)
- appleskin (intake 2026-11-19 -> plan_diet_books_r2.md)
    - a hunger shank shows below food name, hunger shank and saturation shown at bottom of item
    - move hunger+saturation to top, remove duplicate
- double mod label on item when eaten w/ chopsticks tooltip is shown? (intake 2026-11-19 -> plan_diet_books_r2.md)


- armor bars should fill bottom row first, then top
- 2nd+ row of armor doesnt show grayed missing armor slots
- increase jei effect text box max size
- fruit tree leaf layers should be unripe texture, currently ripe
- inside layers of leaves only render when head is inside leaves


- loot tables
    - magic (common, rare), these are base tables not village or witch chests
        - village
        - witch
        - pillage
        - old
        - nether
        - underground
        - water

- apply food log stretch
- dust blocks cause dust particles colored like block when walking, check ash implementation, mirror to all dust
- leaf layers are always transparent inside and out (past ~6 layer currently renders solid)
- leaf overlay
    - misunderstood, remove on player overlay
    - i meant when the players head is inside of leaf layers, the player currently doesnt see those leaves. render the outside texture on the inside of the block as well so leaf cube needs to be seen through, including partial layers.
    - break down what you think i mean before implementing

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
