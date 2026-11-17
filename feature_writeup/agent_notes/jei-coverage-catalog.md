# jei-coverage-catalog

> JEI gap catalog (feature_writeup/jei_coverage.md): part 1 crafting done 2026-10-01 via /dumpcraftingaudit + tools/jei_coverage.py; parts 2 (recipe types) and 3 (world interactions) not started

Moved from agent memory 2026-11-16 (last edited there 2026-10-02). Repo status docs win over any status line here.

User asked 2026-10-01 for a catalog of every recipe, recipe type and world interaction JEI doesn't show, starting with crafting. Lives in feature_writeup/jei_coverage.md.

- Part 1 (crafting) DONE: `/dumpcraftingaudit` (kubejs/server_scripts/_lib/dump_crafting_audit.js, also in /dumpall) -> crafting_audit.json + recipe_types.json; `python3 tools/jei_coverage.py` fills the generated block. 69 hidden of 15,572; only pack-made gap is creativeengineering:elytra_duplication.
- JEI 11.8 rule: non-special shown if result non-empty and 1-9 ingredients; isSpecial() hidden unless a plugin extension (class or superclass) or stand-in recipes. Plugin coverage lists (EXTENSIONS/STAND_INS in the tool) came from bytecode, not a running JEI; JC-01..05 spot checks pending.
- Part 2 next: recipe_types.json already exported; need the client-side list of JEI category RecipeTypes to diff.
- KubeJS reflection here: Mojang names only (SRG "not a function"); Recipe.getType() is kjs$getType (serializer id), use RecipeManager.getAllRecipesFor per registry type; Ingredient.getItems() hidden, use getStacks(). See memory `rhino-fieldandmethods-gotcha`.
- Fix kinds: stand-in display recipes need Java (Creative Common JEI plugin); KubeJS JEIEvents can only do info pages.
