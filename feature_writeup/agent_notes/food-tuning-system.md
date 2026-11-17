# food-tuning-system

> Food feature - /dumpfood, 31 food types as #creativeengineering:food_type/* tags from generation_scripts/food/food_types.json, tools/food_catalog.py, SoL + Canned Goods findings; writeup and status in feature_writeup/food.md (FO-)

Moved from agent memory 2026-11-16 (last edited there 2026-09-29). Repo status docs win over any status line here.

First 1.0.2 feature (started 2026-09-28). **Everything is in `feature_writeup/food.md` (FO- tests) + generated
`feature_writeup/food_catalog.md`.** This memory keeps pointers and gotchas.

- Pipeline: in-game `/dumpfood` (kubejs/server_scripts/_lib/dump_food.js) -> `python3 tools/food_catalog.py`
  (classifies only foods in no type yet, never moves placed ones; adds item_dict keys in a generated block before
  `// Validation`; writes the catalog) / `--check`.
- Types: `kubejs/generation_scripts/food/food_types.json` = type -> item_dict KEYS (not ids), plus `excluded` and
  `canned` (can key -> source keys). `server_scripts/tags/foodtypetags.js` adds them via `global.tags.addTag` so
  nested types chain to parents. Tag keys `i.t_food_<type_with_underscores>`, `i.t_canned_source`. User asked for the
  tags to build canning recipes later.
- New item_dict keys need a game restart before the tag script sees them.
- Canned Goods ids are crossed (canned_carrots = pumpkin pies, canned_pumpkin_pie = golden carrots); the canned map
  follows content, not id. Its saturation modifiers are absolute values (up to 42): cans dominate Spice of Life.
- Spice of Life jar is really "solsweetpotato": complexity = config list else f(hunger, modifier); diversity =
  sum complexity x (1 - age/32); `ConfigHandler.complexityMap` is a public HashMap rebuilt only at server start
  (planned KubeJS injection). Server config is per world, no defaultconfigs copy yet.
- Running the dump: the client used 10 of 14 GB, so a scratch server wasn't possible; the user ran /reload + /dumpfood.
- 2026-09-28 canning.js (server_scripts/cooking): all Canned Goods food-can + can + can->ingot recipes removed;
  empty cans: stonecut ingot 4, 2 plates 12 (shaped + deployer), IE components blueprint 4 plates 32. User: no food
  canning and no value changes yet ("more to implement on the can side"); crushed can now has no recipe.
- 2026-10-02: `/dumpfood` is now in /dumpall (it was missing, so the export sat at 2026-09-28). Refresh: 856 foods, 227 new typed; the classifier misfiles muffins/tarts/pies/jellos as raw or meal and bottled jellies as drink. Precedents used to fix them: jam/jelly bottles = ingredient, custard/pudding/popsicle/candy-on-stick/jello = sweet, dried/baked fruit = cooked/fruit, cut fruit = raw/fruit, jelly bread/corn dog = handheld.
- 2026-10-20 audit: /dumpfood on a SCRATCH (dedicated) server gives raw lang keys (`item.creativeengineering.canned_x`) and
  pre-resource-pack names for KubeJS/CE items: merge the `name` fields back from a client dump (by id) before running
  food_catalog.py. Then rerun gen_can_types.py too: can effects/values are copied from catalog.json, so a stale catalog
  leaves cans with effects that no longer exist (Brewin port: kimchi `satisfaction`). food_catalog.py now keeps trailing
  `// ...` comments in its item_dict block (it used to drop them on re-sort).
- Open: decisions D1-D6 in food.md 4.3 before building the tuning layer.

Related: memory `centralize-recipes-tags-items`, memory `item-dict-mechanics`, memory `feature-writeup-pattern`, memory `scratch-server-verify`.
