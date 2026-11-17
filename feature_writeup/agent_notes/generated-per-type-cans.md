# generated-per-type-cans

> tools/gen_can_types.py -> can_types.json drives EVERY can (hand-written raw cans migrated 2026-10-21), Canned Goods orphan values (norecipe), family sprites per section, pot (Cooking Pot) sections

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Built 2026-10-04 (new items and dict keys: a change needs a restart). Sections/multipliers live in SECTIONS/EXPLICIT in `tools/gen_can_types.py` (mirror the Goal comments in canning.js); it reads `kubejs/exported/food/catalog.json` and writes can_types.json, a marked block in item_dict.js, can_servings.json, food_types.json (via food_catalog.write_types).

- One meal = source hunger and sat points x mult, effects copied with duration scaled; "existing" cans are Canned Goods items whose food is overridden in cans.js ItemEvents.modification (setFoodProperties).
- Recipes: canning.js `canSection('x')` loops the json; multi-alternative ingredients repeat the array n times (no tags). Recipe id = can key + suffix.
- Key collisions with Canned Goods keys fall back to `canned_ce_<key>` (e.g. canned_ce_beetroot_soup). Golden cans are new items on purpose (Canned Goods golden cans add code effects).
- 2026-10-13 extension: "meal" sections (fixed meal size, recipe count sized per source by hunger+sat value, target 0.8 of
  the can, max 8; alt inputs sized against the first source, >8 -> 8 items + partial can via recipe "servings" ->
  CanServings NBT output): raw_fruit, raw_meat (shulker), raw_fish, hazard, special. New mult sections meal (pasta),
  pie (opts pie= -> filling + pie_crust -> whole pie), feast (opts input= feast block), drink (powdered milk via
  opts values + clear_effects -> FoodBuilder.eaten). opts code_fx = Canned Goods applies effects in finishUsingItem
  (pufferfish, poisonous potato, spider eye, rotten flesh, honey, chorus) so effects are not copied. GROUP_SPRITE +
  "texture" field (null when kubejs/assets/.../item/<key>.png exists). Stale generated keys are dropped from
  can_servings/food_types on rerun. 381 recipes, node dry-run clean; not in game. Placeholder values in can_parity.md 3.1.
- Not built: salad vinegar additive, suspicious stew can, 6 unobtainable pie slices.
- After restart: /dumpfood then `python3 tools/food_catalog.py --check` (it flags the new cans as not installed until then).

- 2026-10-21: the 26 hand-written raw veggie/fruit/meat cans moved in (section raw_veggie + EXPLICIT rows; `input` opt =
  recipe keys/tags vs catalog value sources; Cactus Flesh `adv=0.5` keeps 4/2+1). Verified identical to the old
  canning.js/cans.js (recipes, ids, values, servings, names, sprites); they gained effects (TODO #4). ANCHOR is now the
  `i.any_bell_pepper` dict line. New opts: adv, servings, fx_drop, pot, norecipe (values only; must NOT add sources to
  `taken` or the soup/golden loops drop pack cans), values.meat. New sections dairy (pot -> R.fdCooking, can in
  container slot), preserve, dessert, cake (canned_cake: 1 cake or 4 slices + sweet). 15 Canned Goods orphan cans
  (apples, beets, golden..., canned_food Meat Meal, hotpot 3 h / 0.5) get family values. GROUP_SPRITE covers every
  section (golden/enchanted key for glow cans); fallback canned_goods:items/canned_food, nothing uses canned_beets now.
  #14 cans built (19 new). After a restart run /dumpfood, food_catalog.py --check (only the 19 new flag until then).

- 2026-10-21 (later, user): Canned Food (+ its butcher BasicItemListing, removed in economy/trades.js via forEachTrades +
  removeIf) and Hot Hot Hotpot REMOVED (removed_items.js; REMOVED list in the script drops their servings). Cactus
  Flesh follows the rule (4/3+1). Corn cuts into 4 kernels; kernels NOT canned. Coffee keeps Wither at 2 s (opt
  fx_ticks). Tart cans = canned_fig/lemon_pie_filling (tarts become pies). Canned Dough = 8 Create dough, food=False
  (not edible, no food type), uncan recipe dropped ("ingredient later"). No partial-can outputs anywhere.
  "Broken values" = AppleSkin saturation (shows points/2: Canned Cake 28 x 4.0 x 2 = 224 -> "112").
- NCH `cans` arg is the can-recipe guard: auto `_manual_only` id (Create's shouldIgnoreInAutomation: mixer, mech
  crafter, JEI) + modifyResult refusing grids with no player (menu without player-inventory slots = IE Assembler, AE2
  assembler); `{ whole: id }` = used up whole, must be full (CanServings.isPartial). ItemStack.getTag is invisible in
  Rhino, so the check calls Creative Common's CanServings.

Related: [multi-serving-cans](multi-serving-cans.md), [food-tuning-system](food-tuning-system.md), memory `centralize-recipes-tags-items`.
