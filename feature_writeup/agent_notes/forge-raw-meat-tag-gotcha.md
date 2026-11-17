# forge-raw-meat-tag-gotcha

> In this pack #forge:raw_meat holds only Miner's Delight arthropod + spider leg; use #creativeengineering:food_type/raw/meat (i.t_food_raw_meat) for \"any raw meat\

Moved from agent memory 2026-11-16 (last edited there 2026-10-25). Repo status docs win over any status line here.

`#forge:raw_meat` in the 2026-10-25 tag dump has 2 entries (miners_delight:arthropod, spider_leg). Newer upstream
recipes (FD 1.3.4 Dog Food, Cabbage Rolls) name it expecting beef/pork/chicken/mutton/rabbit, so a literal port breaks
the recipe. The pack's equivalent is `#creativeengineering:food_type/raw/meat` (30 meats, `i.t_food_raw_meat`), and
`food_type/raw/fish` (`i.t_food_raw_fish`) for fish.

**Why:** found during the FD 1.3.4 backport (feature_writeup/farmers_delight.md 4.3).
**How to apply:** before porting any recipe that names a forge tag, check `kubejs/exported/tags/item_tags.json` for its
size; prefer the pack food-type tags. See memory `item-dict-mechanics`.
