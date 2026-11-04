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

## Idea, Notes, Changes (Human scratchpad, don't write up or add to plans unless directed)

- lower corncob cutting recipe (2 kernels), add mortar recipe (3 kernels), and grinding (create mill, crush, ie crush, 4 kernels + 0.5 cornmeal)
- curios face slot
    - missing texture, use standard helmet icon
    - allow smoking pipe in slot
- no strawberry storage block
- all berries take bag/sacks, other fruits/veggies take crates?
- book writing animation? not enough animations?

- new jam fluids and blocks displays incorrectly in jei, both square textures and doesnt fold around, register like other jams
    - swap new jams to fruit colors. 
- allow animals to eat thatch, apply gravity, implement w/ layers and thatch drawing on top?
- missed honey jar retexture
- swap empty mason jar texture to fruit delight icon

- grass crop seasons missing language
- 'Can be eaten w/ chopsticks' to foods that are eligible
- rename redeco jar to empty terrarium
- restore azalea wood? hasty removal
- expand wood coverage to all woods automatically? (creative delight basket, rack, etc) investigate everycompat
- paint polished stone textures on stone cutting boards, not palette swap (overlay texture w/o changing shape)
- cosmetic curios back slot
- change grass offseason to only winter (currently has cold grasses)

- seasoning/bbq
    - season icon location by item, family? overlap is consistant problem
    - make offhand slot clearer 
        - adjust
    - allow grilling raw meat for grilled effect
    - allow grabbing 1 item from ingredient basin with full hand
    - allow multiple ingredient types for skewer (3 cabbage leaf type, 2 potato type)
    - allow any item in offhand/in basin
    - jei tab needs name
    - retexture cumin, pepper, and chili powder to dusts
    - seasonings not in trade jei
    - sauced icon missing, add new shape
        - fall back to salt icon
    - allow sauce on more meat like foods, whitelist
    - keep tray, new wood variants?
    - ingredient basin wood variants
    - change 'Burnt!!' wthit tooltip to 'Burnt'
    - show grilled icon in jei grilling
    - use effect chance more in skewers
    - grilled icon/line missing
        - fall back to salt icon