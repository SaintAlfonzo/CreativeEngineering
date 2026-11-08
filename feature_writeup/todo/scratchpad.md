# Scratchpad

## Prompt scratchpad, dont touch

build a plan for these changes. weeds should not grow naturally on rich soil/farmland and fertile soil, but allow hand planting. bushier flowers never generate in world. swap rich and fertile dandelion grow times. add the copper pot tooltip. oatmeal can take sugar as an additive. magic peanut should already be 0.05%. id like 3 rich soil used to craft a fertile soil. boost glow lichen spawn rate by 3x, and make spawned patches bigger. restore azalea wood, the removal was hasty and unneeded. honeycomb shearing yield should be 3x. 

## Writeup plans (add these to writeups and todo)

## Idea, Notes, Changes (Human scratchpad, don't write up or add to plans unless directed)

- warning/error acknowledger
    - some warnings or errors are expected under certain circumstance, some are known but not fixed yet
    - a system should allow porting error and warning logs to an 'aknowledged' and 'in progress' file, and the remaining to latest.log. a small note should print in latest.log saying where the event was sent to instead of printed, but the goal is to make standard log output more readable 

- tempad notes
    - change chronons to [chronotons ](https://futurama.fandom.com/wiki/Chronitons)
    - allow knowledge projector recipes to update based on actual recipe
    - change knowledge projector lore (remove TVA)
    - fix 'hold {X} chronotons' knowledge projector
    - tooltips for every item, based on knowledge projector
    - replace time steel recipes w/ electrum
        - only electrum on location card
    - mark a note to make recipes much much more complex, intermediate steps, unusual craft steps
    - default app doesnt work
    - only 1 macro option?
    - cant use empty or marked location card on tempad
    - change knowledge base categories (iron vs steel) to basic vs advanced. advanced recipes are a good deal harder
    - use projector in timedoor projector, timedoor projector in tempad
    - workstation is locked?
    - swap timedoor projector door projection angle 180 degrees (behind station., not ahead)
    - need to test location boradcaster in multiplayer
    - ensure all apps are added to tempad
    - timeline missing languange in tempad

- tempad in server
    - chronomarker used for mining/resource dimension navigation, more valuable resource dimensions availible w/ own tempad (unlock location cards?)

- weathered cardboard box is toot small, 4 faces on each block face, doesnt wrap correctly

- public pack notes
    - wild food in nearly every biome
    - botany pots, very nerfed, useful for flowers and low usage crops
        - tooltip, 'Useful for flowers, not great for crops'


- golden food buffs
    - all effects and saturation/hunger are identical within family
    - fruit
        - golden: regen 2 5s,  speed 2 15s, nourishment 1 30s
        - gilded: regen 2 20s, speed 2 60s, nourishment 1 60s
    - veggie
        - golden: regen 2 5s,  jump boost 2 15s, nourishment 1 30s
        - gilded: regen 2 20s, jump boost 2 60s, nourishment 1 60s
    - meat/fish
        - golden: regen 2 5s,  speed 2 15s, nourishment 1 30s
        - gilded: 
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
