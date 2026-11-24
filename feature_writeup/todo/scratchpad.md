# Scratchpad

## Open plans, dont touch

feature_writeup/plan_wave1_publish.md
feature_writeup/plan_wave2_publish.md

## Prompt scratchpad, dont touch

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

- rp tweaks
    - commands to set diversity to specific level for player
    - permanant effects stop cycling between icon and diamond when enabled
    - update steady effect description
    - all short effect descriptions  
    - dropping top effect slot drops all, fix (intake 2026-11-23 -> plan_wave1_publish.md)
- all jams should be foods (intake 2026-11-23 -> plan_wave1_publish.md)
- hot cocoa, coffes, prickly pear juice, strawberry banana smoothie, and all their families are drinks (intake 2026-11-23 -> plan_wave1_publish.md)

- move weathered cardboard box to creative engineering (intake 2026-11-23 -> plan_wave1_publish.md)

- all vanilla squid should flee whales, giant squid should fight (intake 2026-11-23 -> plan_wave1_publish.md)

- little joys mod
    - extra random rewards
    - port or recreation

- Farming for Blockheads
    - only for shipping bin, disable the rest

- steady still prevents slime slowdown
    - expand to prevent all block surface type slowdown or remove immunity

- industrial oven multiblock?
    - smoking or baking
    - 2x2 or 3x3
        - changes i/o capacity 
        - larger oven give speed buff but shouldnt compensate for size increase (larger oven is faster but less space efficient, dense oven setups would use 2x2, two 2x2 should process faster than one 3x3)

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

# Raspberry Pie

## Permanent effects for the Raspberry Pie ladder: breakdown (agent, 2026-11-20, from the owner's list in the prompt block; for the owner to sort)

Source of descriptions: [effects.md](../effects.md) section 2.1 (Brief column and level text). Nothing is built or added to the ladder; this is a sorting list. "Cap" = the pack's `[effects] levelCaps` holds it at level I whatever the source gives. Marks: ladder now = already in `defaultconfigs/solraspberrypie/ladder.json` pool or rungs.


-rp changes
    - ladder is missing tick marker on milestones
        - same color as left/right arrow if past/future milestone
    - ladder is built by 3 subladders, stat increases, permanant, and unlocked effects. defined below
    - ~120 max diversity?
        - >40 is mid, >80 is high



### Stat increases

- Max 20 hearts, 10 milestones evenly spread between min and max (max is satiated, last heart is unlocked before at same distance to first heart unlock from 0)
- 3x paired toughness and kb resistance milestones, at 1/6,1/2,5/6 max diversity. 1 tough, .1 kbr
- NO movement speed increases

### Permanant Effect Unlocks

- Luck 1
- Speed 1
- Night Vision
    - Toggle
- Phantom Bane
    - Toggle
- Speed 2
- Regen 1
- Long Reach 1
- Strength 1
- Invisibility 
    - toggle switch, off by default
    - 3rd highest lv
- Double Jump
    - toggle switch, off by default
    - 2nd highest lv
- Luck 2
- Satiated
    - final effect

### Unlockable

- short tagline over effect when hovering in rp guis, / means no hover tag (self explanitory)
- unlockables are not slotable once permanant effect of same kind is enabled, grays replaced slot with free respec 

~ 40 unlock slots, evenly spread along diversity ladder

- Brightening
    - Night Vision: See in the dark
        - (toggle switch instead of lv adjust)
    - Glowing: Reveal yourself to nearby players
        - (toggle switch instead of lv adjust)

- Leaf Piercing: Projectiles pass through leaves
    - Spawn leaf particles of leaf block trailing after an arrow, leaf poof at block enter point 

- Speed 1: Move faster

- Allure: Faster fishing

- Immunity
    - Refreshing: Slowness immunity
        - Removed when Stimulation unlocked
    - Sweetening: Mining Fatigue immunity
        - Removed when Stimulation unlocked

- Steady: Not affected by slippery blocks
    - Change to stop all slip effects, ices and soap
    - Toggle
    - Prevent dripleaf and pressure plates from being affected, add to jei description

- Dig
    - Haste 1: Mine faster
    - Burrowing 1: Mine very fast with shovel

- Water
    - Water Breathing: /
    - Water Walker: /

- Prickly 1: Touch harms nearby creatures, tamed exempt

- Luck 1: Get lucky

- Jump Boost 1: Jump higher

- Lava
    - Lava Vision: See clearly in lava
    - Lava Walker: /
        - Act like magma blocks when walking w/o fire immunity, crouch to fall into lava

- Climbing: Climb walls
    - Removed when clinging unlocked

- Mobs
    - Patrol Nullifier: Disable patrols
    - Phantom Bane: Disable phantoms

- Regen 1: Passively heal

- Poison Immunity: /
    - rename

- Shrinking: Crawl in 1-block gaps while crouched

- Spider
    - Bug Pheromones: Anthropods are neutral
    - Clinging: Climb walls and ceilings
        - Holds climbing as a super

- Fire Immunity: /
    - rename
    - Drown in lava w/o, mostly cosmetic

- Strength 1: Hit harder

- Immunity
    - Clarity: Immunity to Blindness and Darkness
    - Stimulation: Slowness and Mining Fatigue immunity
        - Refreshing: Slowness immunity
            - removes when unlocked
        - Sweetening: Mining Fatigue immunity
            - removes when unlocked

- Wither Immunity: /

- Speed 2: Move faster

- Luck 2: Get lucky

- Long Reach 1: Reach farther

- Slow Falling: /
    - (toggle switch instead of lv adjust)

- Magic Resistance 1: /

- Prickly 2: Touch harms nearby creatures, tamed and passive exempt (at lv 2)
    - add to jei

- Dig
    - Haste 2: Mine faster
    - Burrowing 2: Mine very fast with shovel

- Magnet: Attract items when standing
    - (toggle switch instead of lv adjust)

- Jump Boost 2: Jump higher

- Recovering: Poison and Wither immunity
    - Disables sub-effects when unlocked
        - Poison Immunity: /
        - Wither Immunity: /

- Long Reach 2: Reach farther

- Fleet-Footed: Faster while in the air
    - Holds feather feet as a super
    - Lower FOV shift when speed is applied

- Strength 2: Hit harder

- Eating
    - Appetizing: Eat when full
    - Digesting: Overeat to gain saturation
    - Lozenge: Eat food fast

- Regen 2: Passively heal

- Long Reach 3: Reach farther

- Luck 3: Get lucky



- Remove: Agility, Stability, Brightening
- Make emptiness a super of patrol nullifier and phantom bane
- Harmony needs new source, not adzuki or diet ladder
    - Applied by a passive mob? Koi in newer updates, do audit of similar rare passive mobs
    - Applies emptiness (subsubeffects!) and hostile mob flee

### C. Open questions for the owner

1. Stimulation: replaces Refreshing and Sweetening (read above), or sits next to them? replace when unlocked
2. Lava Walker is in the current ladder but not on the list; keep, drop, or swap for Lava Vision? keep both in their slot
3. Knockback Resistance (Alex's Mobs) and Resilience (Quark) are the same effect; confirm Alex's Mobs one only. drop resilience, confirm
4. Satiated: remove its brewing recipe, and does it replace the Nourishment and Digesting rungs or stack? permanant buff, stack
5. Rename Astringent to Steady: just the display name, or also the id? (Id cannot change without an alias.) name
6. Glowing: keep as a rung, or treat as a trade-off effect? keep
