# rustic-delight-haste-potions

> Haste potions are Rustic Delight's (rusticdelight:haste/long_/strong_), made item-agnostic via RD_POTION_MIXES in potions.js; kubejs:haste + golden moss recipe removed 2026-09-26

Moved from agent memory 2026-11-16 (last edited there 2026-09-26). Repo status docs win over any status line here.

The pack's only Haste potion line is Rustic Delight's `rusticdelight:haste` / `long_haste` / `strong_haste` (awkward + golden coffee beans, redstone, glowstone). `kubejs:haste` (thick + immersive_weathering:golden_moss_clump) was removed from CUSTOM_POTIONS on 2026-09-26 at the user's request because two "Potion of Haste" lines existed.

RD brews via its own `ModBrewingRecipe implements IBrewingRecipe`, NOT Forge `BrewingRecipe`. Create's PotionMixingRecipes and IE's PotionHelper only read `instanceof BrewingRecipe`, so RD's recipes are invisible to every mixer. Creative Common PotionMixDedup also only dedups `BrewingRecipe`, but that doesn't matter since the mixers never see RD's recipes. Fix: `RD_POTION_MIXES` in kubejs/startup_scripts/cooking/potions.js re-adds all 3 steps as PotionBrewing.Mix (same pattern as [am-potion-brewing-fix](am-potion-brewing-fix.md)).

**Why:** a mod potion that seems to work in the brewing stand can still be missing from the mixers and from splash/lingering. Check the recipe class before assuming it works.
**How to apply:** for any new mod potion, check whether its brewing class is a real `BrewingRecipe`. If it isn't, add item-agnostic Mix rows. Bottling, tipped arrows, JEI fluids and Lingerification all walk the potion registry, so they need no changes.
