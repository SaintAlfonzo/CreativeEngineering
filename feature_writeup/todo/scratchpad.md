# Scratchpad



- todo review (dont move this)
    - finished nested cd jar
    - need to do standalone with integration and standalone w/o

    list {2. Standalone third-party Delight jars, with pack integration
Mod	Licence	State
Ender's Delight 1.2.2	All rights reserved	Planned donor-route member (members #33, 38.1). Nothing built. The old jar stays in mods/ until it is, and the enders_additions member folds into it.
Fruits Delight 0.5.11	All rights reserved	Pack code and the 1.1.3 fixes live in CD folders. It can only be nested by the donor route.
Farmer's Respite 2.0	MIT	CD scripts and the drink rules cover it. A repack is possible.
Corn Delight 1.0.3	MIT	Same as Farmer's Respite.
More Delight	MIT	Same as Farmer's Respite.
Abnormals Delight 4.1.2	Custom (link)	Needs a licence check.
Alex's Delight	MIT	CD script rows only.
Quark Delight	All rights reserved	CD script rows only. Donor route only.
Hostile Delight 0.2.2	AFL 3.0	Plan written (D1–D16), "not yet integrated".
Storage Delight	MIT	Built as repackedStorage, but its own jar and not nested.
3. Standalone, with no CD folder or integration
Chefs Delight (MIT)
Engineers Delight (MPL 2.0)
KubeJSDelight (LGPL 3.0)
4. Cooking-adjacent, not Delight add-ons (stay separate)
Cooking for Blockheads: all rights reserved, so only the blockheads_additions member goes in CD.
Applied Cooking
Golden Food
Create Central Kitchen
Farmer's Structures: has its own donor handling.
Canned Goods
SoL: Apple Pie
Salt and Create Salt}

Planned rows, ready vs. needing a decision
"Ready" means the Decisions cell says "none". That is about 168 of the 421 Planned rows (my count of the table, not a documented figure).

Category	Ready to build	Waiting on a decision
Items and Blocks	34	22
Food Rules	19	11
Interface and JEI	19	10
Farming	14	21
Mods and Ports	10	30
Tech	10	14
Mobs	9	12
Loot and Trade	7	9
Tooling	7	7
Structures	6	18
Dishes and Ingredients	5	12
Alcohol	5	5
Worldgen	4	8
Kitchen and Cooking	3	5
Gear and Combat	3	16
Potions and Effects	3	15
Mechanics	3	12
Drinks and Drugs	2	4
Recipes and Materials	2	14
Creative Delight	2	3
Baking and Sweets	1	3
Creative Mods	1	5

## Writeup plans (add these to writeups and todo)

## Idea, Notes, Changes (Human scratchpad, don't write up or add to plans unless directed)

- download jars to mods folder then move to an upstream folder system
- unify all upstream folders into /upstream

- ensure swap system allows players running unmodified versions of delight, other mods to swap items to new variants. lower priority, swap for every merge and some removes
- manure buildup system
    - required hearth and harvest manure
    - manure can build up as layers if too little space is in pen or manure not cleared consistanly enough
        - manure mobs pathfind to open space to drop manure
        - crammed mobs (touching other mobs, unable to pathfind to 3x3 clear space) drop manure layers instead of items with a chance