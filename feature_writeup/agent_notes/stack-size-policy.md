# stack-size-policy

> Stack size policy: tools/gen_stack_sizes.py rules -> generation_scripts/stacks/stack_sizes.json -> startup_scripts/changes/stack_sizes.js; status in feature_writeup/stack_sizes.md

Moved from agent memory 2026-11-16 (last edited there 2026-10-05). Repo status docs win over any status line here.

User's stack-size spec (2026-10-05): golden foods 16; bowl meals/soups/salads/feast plates 32; drinks 32; ice cream/pudding/custard 32; pancakes 32; jams/jellies/honey bottles/cooking ingredients/handheld 64; placed cakes + feasts 8; filled buckets 8; special bottles 16; simple hats 8; signs/banners/flags/fairy lights/armor trims/eggs/unused film/stations/etc 64.

Mechanism: rules live in `tools/gen_stack_sizes.py` (first match wins, never reads current stack size so output is stable after the game applies it); it writes `kubejs/generation_scripts/stacks/stack_sizes.json` (size -> item_dict keys, raw ids where no key, 1036 items); `kubejs/startup_scripts/changes/stack_sizes.js` applies on ItemEvents.modification. Replaced the old item_properties.js eggs block (deleted; eggs now come from #forge:eggs in the tool).

**Why:** tags aren't loaded at startup and item_dict lacks simplehats etc., so ids are resolved offline from the /dumpall exports.
**How to apply:** change policy by editing the rules and rerunning, then restart. ItemEvents.modification posts in KubeJS loadComplete, after FD's commonSetup soup override, so the 3 vanilla stews end at 32.

Judgment calls to flag: golden milk bucket = 8 (bucket beats golden), golden honey bottle = 16 (golden beats honey), canned drinks and omelette/shrimp fried rice/caramel popcorn left alone, pies/nachos/popcorn box untouched, fig pudding + pineapple fried rice blocks + B&C pizza + CreateAddition cakes = 8.

Related: [stack-sizes-catalog](stack-sizes-catalog.md), [food-tuning-system](food-tuning-system.md), memory `centralize-recipes-tags-items`.
