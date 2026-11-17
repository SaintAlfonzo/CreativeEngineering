# can-parity-worksheet

> feature_writeup/can_parity.md (CP-) hand-kept per-food can coverage sheet; 2026-10-13 raw can removals, white strawberry can, cactus flesh rename; newcan sprites

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

`feature_writeup/can_parity.md` (written 2026-10-13) is the per-food can coverage worksheet: 18 families, 480 rows,
columns Want / Item / Can / Alt (sliced) / Impl / Texture / Recipe / Food audit / Effects / then Food, Craft sum and
Canned h/s, which stay `later` until food values are settled. Cell words (user switched to marks): x = yes, / = no, n/a (not canned), none (no effects),
later, `-` (awaiting Want), `?`. Hand-kept: the one-off generator was a scratch script, nothing regenerates it.

Same day (from the user's notes in cans.js): removed canned mint, acorn, grape, mite crust, raw ham/chevon/hoglin/
kangaroo/moose/bison/bunfungus (items, dict keys, recipes, servings, food_types, grape tags in cantags.js);
`canned_white_strawberry` split from strawberries; `canned_cactus` -> `canned_cactus_flesh` and delightful:cactus_flesh
renamed "Cactus Flesh" in namechanges.js. gen_can_types.py ANCHOR now the canned_raw_rabbit dict line.
The removed ids and canned_cactus shipped in 1.0.4: no remap (closed as no issue in can_parity.md).

Sprites (10-13): textureswapper/canned/newcan -> 19 CE cans in kubejs/assets/creativeengineering/textures/item (cans.js
drops .texture() for them) + 9 overrides in kubejs/assets/canned_goods/textures/items (fish, cooked_salmon, mushrooms,
+6 unused CG cans). (Until 10-21 every unsprited can pointed at canned_beets; now each borrows its family sprite, see [generated-per-type-cans](generated-per-type-cans.md).) Sheet 5.1 has the map; CP-06.

10-13 (later): all Want x rows built except 6 pie slices with no source in the pack (Delightful blueberry/crimson/green
apple/nightshade, CCK cherry/truffle); 15 implemented Want / cans removed (not shipped, no remap needed); AM banana
dropped. Sheet rows were updated by script from can_types.json; summary counts recomputed by script.

2026-10-20 audit: new families 2.19 Hazard (4 built CG cans + 7 ?), 2.20 Egg and dairy, 2.21 Preserves, 2.22 Desserts,
2.23 Cake, all `?`; still outside (section 3, anchor `outside`): rest of sweet, handheld, dough-type ingredients. Summary
table now recounted by script from the row cells (Want/Impl/Texture/Recipe/Effects = x); user marks undecided rows `?`.

2026-10-21: all 20 buildable Want x rows built (sheet rows filled, CP-15..21 added); summary recounted by script
(the count method reproduces the old table exactly: Want/Impl/Texture/Recipe/Effects cells = x). 6 pie slices still no source.

2026-10-21 audit: `tools/can_audit.py` (imports gen_can_types) fills Food/Craft sum/Canned h/s + Food audit + 2 new
columns (In -> out, Out vs target: 'h% / s%', 100 = equal) in every row with a can (wildcard ids like bell_pepper_*
match by prefix), and regenerates 4.3 between CAN AUDIT markers. Feast/cake input = block servings x serving
(food_blocks.json + FEAST_SERVINGS: stuffed hoglin 11, stuffed shulker 4, cake 7). Rerun after gen_can_types.py.
Sheet tables are now 15 columns (recount script still reads cells 0-9). White Strawberries: meal (7,5) + counts (3,2).

Related: [generated-per-type-cans](generated-per-type-cans.md), [multi-serving-cans](multi-serving-cans.md), memory `feature-writeup-pattern`, [food-tuning-system](food-tuning-system.md).
