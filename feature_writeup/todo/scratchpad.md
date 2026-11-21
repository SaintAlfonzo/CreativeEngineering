# Scratchpad

## Prompt scratchpad, dont touch

these effects will be sorted into the solrp ladder. speed, haste, strength, jump boost, regen, resistance, fire resistance, water breathing, invisibility (one of the highest levels), night vision, absorption, satiated (final effect, permanant effect at 'max' diversity, essentially locks diet if no food is eaten, not brewable), glowing, luck, slow falling, dolphin grace (high level permanant), bug pheromones, clinging, fleet-footed, kb resistance, lava vision, poison resistance, long reach, patrol nullifier, phantom bane, allure, emptiness (high), feather feet, raging, sweet heart, stimulation (refreshing and sweetening super), magic resistance, all fruit delight (rename astringent to steady, drop cycling, drop chorus, alienated, healing aura, disgusted, rage aura, suspicious smell), burrowing, clarity, prickly, pungent, magnet, water walker, are included. identify other positive permanant effects i may have missed, break down all of these effects in a list with the description in the scratchpad for me to sort further later

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

- cozy cats
    - ensure stay-follow-wander-guard modes are enabled from in world sit/stand interaction
    - enable gender for cat, but still asexual reproduction
    - update cat food list in book and behavior
        - all raw meat
        - most raw fish (non poisonus)
        - cave carrot meats
        - caviar
    - tooltips on all items
    - check peace-make items like wheat, add feather and foods
    - yarn ball doesnt work
    - merge beds w/ domestic innovation
        - use DI bed as cozy bed, remove cozy bed
    - enable totem of undying behavior reroll
    - do animals eat while unloaded?
    - update book info, rewrite to be more concise/more words per page?

- change campfire textures to not have fire when place unlit is enabled
- underground wood crop
- trash can inventory slots is high priority
    - point at ftb trash can
- mustard?
- display delight
    - allow plated food to be picked up with silk touch, dont allow drinks or placed bowls
    - tooltip on all plated display blocks 'Can be picked up on plate w/ silk touch /n Plate food then serve customers'
    - crouch to pickup empty plates like fd stations
    - plated smore places as 2 items, only shows as 1 
- fake meatloaf to veggie meatloaf
- storage delight cabinets as half cabinets, high priority!
- small manual create mixing basin
- copycat/framed blocks dont render on xaeros minimap/world map
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

- server enabled 'bleed out on ground and get up w/ help' mod
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

- splash texts
    - ISO 8601
    - Free and Open Source

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


## Permanent effects for the Raspberry Pie ladder: breakdown (agent, 2026-11-20, from the owner's list in the prompt block; for the owner to sort)

Source of descriptions: [effects.md](../effects.md) section 2.1 (Brief column and level text). Nothing is built or added to the ladder; this is a sorting list. "Cap" = the pack's `[effects] levelCaps` holds it at level I whatever the source gives. Marks: ladder now = already in `defaultconfigs/solraspberrypie/ladder.json` pool or rungs.


-rp changes
    - ladder is missing tick marker on milestones
        - same color as left/right arrow if past/future milestone


### Stat increases

- Max 20 hearts
- 2x paired 

### Permanant Effect Unlocks

- Speed
- Haste
- Strength
- Regen
- Jump Boost
- Luck
- Invisibility 
    - toggle switch, off by default
    - high lv
    - not unlockable
- Double Jump
    - toggle switch, off by default
    - 2nd highest lv
- Satiated
    - final effect
    - not unlockable

### Unlockable

- short tagline over effect when hovering in rp guis, / means no hover tag (self explanitory)
- unlockables are not slotable once permanant effect of same kind is enabled, grays replaced slot with free respec 


## Low

- Bright
    - Night Vision: See in the dark
        - (toggle switch instead of lv adjust)
    - Glowing: Reveal yourself to nearby players
        - (toggle switch instead of lv adjust)

- Water
    - Water Breathing: /
    - Water Walker: /

## Mid

- Lava
    - Lava Vision: See clearly in lava
    - Lava Walker: /

- Spider
    - Bug Pheromones: Anthropods are neutral
    - Clinging: Climb walls and ceilings
        - Holds climbing as a super

## High

- Fleet-Footed: Faster while in the air
    - Holds feather feet as a super
    - Lower FOV shift when speed is applied







- Speed: Move faster
- Haste: Mine faster
- Burrowing: Mine faster w/ shovels
- Strength: Hit harder
- Regen: Passively heal
- Jump Boost: Jump higher
- Luck: Get lucky



- Poison Immunity: /
    - rename

- Wither Immunity: /
    - high level

- Fire Immunity: /
    - rename





- Slow Falling: /
    - (toggle switch instead of lv adjust)





- Long Reach: Reach further

- Mobs (Mid)
    - Patrol Nullifier: Disable patrols
    - Phantom Bane: Disable phantoms

- Allure: Faster fishing

- Stimulation (Unlock later and replace subeffects from list?)
    - Refreshing: Slowness immunity
    - Sweetening: Mining Fatigue immunity

- Magic Resistance: /

- Appetizing: Eat when full
- Digesting: Overeat to saturation
- Lozenge: Fast eating

- Steady: 
- Shrinking: 

- Leaf Piercing
- Prickly

- Magnet




- Clarity: Blindness/Darkness


### A. Owner's list, grouped

**Vanilla, scaling (levels give real steps)**

| Effect | What it does | Notes |
|---|---|---|
| Speed | +20% walk speed per level | ladder now (unlock I at 2, flat I at 20) |
| Haste | +20% mining speed, +10% attack speed per level | ladder now (I at 5, II at 60) |
| Strength | +3 melee damage per level | ladder now (I at 10, II at 70, III at 90) |
| Jump Boost | +0.1 jump velocity per level (about +0.5 block at I); fall damage starts 1 block later per level | ladder now (I at 7, II at 55) |
| Regeneration | 1 HP every 2.5 s at I, interval halves per level | ladder now (I at 20) |
| Luck | +1 luck per level (loot and fishing) | ladder now (I at 18, II at 100) |

| Fire Resistance | Immune to fire and lava damage | cap I |
| Water Breathing | No drowning | ladder now (14); cap I |
| Invisibility | Invisible; mobs see you from closer (detection x0.07) | owner: one of the highest levels; cap I |
| Night Vision | See in the dark and underwater | ladder now (12); cap I |
| Glowing | Outline visible through walls | owner listed it as positive; it is a tell for other players, not a buff; cap I |
| Slow Falling | Fall slowly, no fall damage | cap I; overlaps Feather Feet (slight) and Sunbird's Blessing (total) |

| Satiated (saturation) | Every tick +1 hunger and +2 saturation per level | owner: final effect at max diversity; essentially locks the diet while no food is eaten; not brewable. The pack currently brews it (5 ticks), so "not brewable" means removing that recipe. The level 1 effect is permanent here, so each tick tops the bar up |

| Bug Pheromones | Arthropods do not target you unless you hit them | cap I |
| Clinging | Walk upside down on ceilings; sneak to drop | cap I |
| Fleet-Footed | +0.2 speed (about 3x walk) while sprinting in the air | ladder now (36); cap I |
| Lava Vision | See through lava | cap I; overlaps Night Vision (slight) |
| Poison Resistance | Clears Poison every tick | cap I; overlaps Recovering (Fruits Delight, Poison and Wither) |

| Long Reach | +3 block reach (blocks only, not attacks) | ladder now (25); max V from a netherite beacon, also felsic and dragon armor |
| Patrol Nullifier | No pillager patrols spawn around you | ladder now (33); cap I |
| Phantom Bane | No phantoms spawn for you | ladder now (22); cap I; also the echo armor set |

| Allure | Fishing rod ticks twice as often | cap I |
| Feather Feet | No fall damage | cap I; also the dragon armor set; overlaps Slow Falling |


| Magic Resistance | -20% magic damage per level, immune at V | max II from potions; also felsic and dragon armor |

| Appetizing | Eat when full | |
| Astringent -> rename "Steady" | Ice and slime do not slide you | rename needs a lang change plus a JEED text pass |
| Brightening | Immune to Blindness and Darkness | overlaps Clarity (Hearth and Harvest, Blindness and Darkness; Clarity also removes them) |
| Digesting | Food over a full bar becomes saturation | |
| Leaf Piercing | Your projectiles pass through leaves | |
| Lozenge | Eat and drink twice as fast (more per level) | |
| Shrinking | Half size while sneaking; crawl through 1-block gaps | ladder now (27) |
| Sliding | Boats slide on land like on ice | |
| Dropped by owner | Cycling, Chorus, Alienating, Heal Aura, Disgusted, Rage Aura, Suspicious Smell | not in the ladder; check they are also gone from foods and the brewing and JEI lists if the owner wants them removed entirely, not just kept out of the ladder |

| Burrowing | +50% mining speed on shovel blocks (max II from a potion) | |
| Clarity | Removes and blocks Blindness and Darkness | |
| Prickly | 1 damage every 0.5 s to mobs touching you; tamed animals spared | |
| Magnet | Items within 5 blocks (+1 per level) picked up at once, or pulled to you when the inventory is full; sneak to pause | max VI from aged wine |
| Water Walker | Walk on water | cap I |


Remove: Agility, Stability
- Harmony needs new source, not adzuki or diet


### C. Open questions for the owner

1. Stimulation: replaces Refreshing and Sweetening (read above), or sits next to them?
2. Lava Walker is in the current ladder but not on the list; keep, drop, or swap for Lava Vision?
3. Knockback Resistance (Alex's Mobs) and Resilience (Quark) are the same effect; confirm Alex's Mobs one only.
4. Satiated: remove its brewing recipe, and does it replace the Nourishment and Digesting rungs or stack?
5. Rename Astringent to Steady: just the display name, or also the id? (Id cannot change without an alias.)
6. Glowing: keep as a rung, or treat as a trade-off effect?
