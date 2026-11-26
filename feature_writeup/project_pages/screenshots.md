# Screenshot list: Common, Compat, Raspberry Pie (2026-11-26)

Setups come from reading the code and dumps; none were run in game. Common and Compat shots need Creative Common,
KubeJS, Rhino and Architectury. Use a creative world with cheats unless a shot says otherwise.

## Spice of Life: Raspberry Pie Edition (only its own jar)

New world. The bundled Carrot Edition adds bonus hearts after 5 unique foods unless its config turns them off.

| # | Shot | Page section | Setup and what must show | Extra mods |
|---|---|---|---|---|
| RP-1 | Diet Ladder | Intro | `/give @s solapplepie:food_book`, `/solrp diversity set @s 22`, open page 2: 22.00 now and highest, progress bar, icons, key | - |
| RP-2 | Meals overview | Diet Log | `/solrp reset @s`, `/effect give @s minecraft:hunger 60 255`, eat 32 different vanilla foods (no `diversity set`); page 1, hover one meal | - |
| RP-3 | Milestones | Bonuses | `/solrp diversity set @s 22`, page 3: reached, struck-through and gray rows | - |
| RP-4 | Effect screen | Effect slots | Set 50, press P, fill Night Vision, Water Breathing, Fire Resistance, Jump Boost; set 30, press P: three filled slots, fourth grayed | - |
| RP-5 | Change confirmation | Costs | After eating foods to diversity 9 or more: P, slot 1, Change to another unlock: before and after diversity, aged foods, Confirm | - |
| RP-6 | Inventory bar | Effect slots | State of RP-4, inventory open, recipe book closed, hover a bar icon | - |
| RP-7 | Liquid Diet Log | Drinks | `/give @s solraspberrypie:liquid_diet_log`, drink milk and a potion, open the book (all potions count as one entry) | - |
| RP-8 | Food Dictionary | Food Dictionary | After RP-2: `/give @s solcarrot:food_book`, eaten foods page, hover the sort button | - |
| RP-9 | Food tooltip | Food Dictionary | Hover an eaten golden carrot: "Diversity: X (contributes Y)" | AppleSkin 2.4.2 (optional) |
| RP-10 | Recipes (optional) | Diet Log | JEI recipes of both books | JEI |

## Creative Common

| # | Shot | Page section | Setup and what must show | Extra mods |
|---|---|---|---|---|
| CC-1 | Two-column effects | Screens and lists | Beacon with Speed on iron blocks; `/effect give @s minecraft:night_vision 300`, `/effect give @s creativecommon:magic_resistance 300 1`; survival, wide window, inventory | - |
| CC-2 | New effects | New effects | `/effect give` magic_resistance, wither_immunity, brain_freeze (`creativecommon:`); inventory icons and names | JEED (optional) |
| CC-3 | Brain freeze | New effects | `[brain_freeze] chanceStep = 1.0`; drink two `creativedelight:glowberry_smoothie` within 60 s: frost overlay and message | Creative Delight set |
| CC-4 | Potions | Potions | After the potion-name fix: `/give @s minecraft:potion{Potion:"creativecommon:magic_resistance"}` and resistance, health_boost, levitation, darkness | JEI (optional) |
| CC-5 | Level caps | Potions | night_vision at amplifier 4, fire_resistance at 3: no level numerals (shows as "Fire Immunity") | - |
| CC-6 | Mod display names | Screens and lists | `[mod_names] overrides = ["kubejs=Pack Scripts"]`, Mods screen | - |
| CC-7 | Survival inventory in creative | Creative menu | `[creative_menu] disabled = true`, creative mode, inventory | JEI |
| CC-8 | Leaf Piercing | Effect fixes | Oak leaf wall, `/effect give @s fruitsdelight:leaf_piercing 300`, shoot arrows through, F1 | Fruits Delight |
| CC-9 | Config in game | Pack settings | Create config screen, Creative Common, effects | Create, Creative Compat |

The two existing changelog viewer and tip bar images need retaking only if those screens changed.

## Creative Compat

| # | Shot | Page section | Setup and what must show | Extra mods |
|---|---|---|---|---|
| CMP-1 | Corpse screen | Corpses | Wear iron chestplate, a ghost mask hat in the Face slot and a cosmetic armor hat; keepInventory off, `/kill @s`, open the corpse | Corpse, Curios, Simple Hats, Cosmetic Armor Reworked, Cosmetic Armor x Corpse Compat |
| CMP-2 | WTHIT jar | WTHIT | Place `supplementaries:jar`, fill with honey bottles: contents line | WTHIT, Supplementaries |
| CMP-3 | Two pots in one | Flower pots | Flower pot with poppy, right-click with a second pot, plant a blue orchid | - |
| CMP-4 | Deep snow | Walking through | `/fill ~-4 ~ ~-4 ~4 ~ ~4 minecraft:snow[layers=6]`, third person, player sunk and a rabbit on top | - |
| CMP-5 | Melting ring | Melting | Snow layers around a magma block, `/gamerule randomTickSpeed 30`, wait for the bare circle | - |
| CMP-6 | Snowy blocks | Melting | Right-click oak stairs, a fence and a poppy with snow; WTHIT on the stairs | Snow! Real Magic, WTHIT |
| CMP-7 | Biome moss | Blocks and plants | Same moss patch in badlands and swamp, side by side | - |
| CMP-8 | Trash can | Crafting, sorting and trash | `/give @s trashcans:item_trash_can`, open with items inside | Trash Cans 1.0.18 |
| CMP-9 | Squid and whale | Animals | Deep water, summon `alexsmobs:giant_squid` and `alexsmobs:cachalot_whale` close together (may take retries) | Alex's Mobs |
| CMP-10 | Tinted glass mirror | Portals | Tinted glass wall, right-click with flint and steel | Immersive Portals |
