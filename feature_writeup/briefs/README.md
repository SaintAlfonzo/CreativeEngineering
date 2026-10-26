# Implementation briefs

One file per TODO subfamily (`<family>_<sub>.md`), written 2026-10-21 from a scan of
[todo/TODO.md](../todo/TODO.md). Each open row (Bug, Planned, Pinned, In Progress) gets a section a later session can
read and start building from: what exists now (files, lines, ids, configs), what to build step by step, the ids and
mod data it needs (checked against the mod jars where the brief says so), open decisions with a recommendation,
gotchas, and the tests to add. Rows at Testing or Complete are only listed (they need play, not building).

The feature writeups stay the reference for how a built feature works; a brief is the plan for the work still to do,
and is superseded by the writeup once the row is built (fold what is still true into the writeup, then delete the
brief's section). The TODO rows link their brief in the Writeup column.

## Section layout

```
## <sub> #<n>: <change>
Status / Work / TODO id line
### Goal
### Now            what exists, with file:line refs and ids
### Build          ordered steps: files to touch, helpers to use or extend, sketch code where it helps
### Ids and data   items, tags, recipe ids, fluids, which mod provides them (verified / unverified)
### Decisions      open questions, each with a recommendation
### Gotchas
### Tests          proposed test ids in the feature writeup's prefix
```

Conventions every brief follows (from [CLAUDE.md](../../CLAUDE.md)): recipes through the `global.NCH` helpers
(`kubejs/startup_scripts/func/neoCraftHelper.js`), tags through `global.tags`
(`kubejs/startup_scripts/func/tag_helper.js`), item / block / fluid ids from the dictionaries in
`kubejs/startup_scripts/_lib/`; when a helper doesn't cover a case, the brief says which helper to extend.

## Index

| Family | Briefs |
|---|---|
| food | [drinks](food_drinks.md), [drugs](food_drugs.md), [placeable](food_placeable.md), [seasoning](food_seasoning.md), [stack-sizes](food_stack-sizes.md), [types-and-tuning](food_types-and-tuning.md) |
| cans | [canning](cans_canning.md) |
| cooking | [kitchen](cooking_kitchen.md), [oven](cooking_oven.md) |
| baking | [baked-goods](baking_baked-goods.md), [sweets](baking_sweets.md) |
| dishes | [ingredients](dishes_ingredients.md), [meals](dishes_meals.md), [jams](dishes_jams.md), [ocean](dishes_ocean.md), [pickles-and-ferments](dishes_pickles-and-ferments.md) |
| farming | [composting](farming_composting.md), [crops](farming_crops.md), [trees](farming_trees.md), [wild-crops](farming_wild-crops.md) |
| worldgen | [bee-nests](worldgen_bee-nests.md), [biomes/netherblocks](worldgen_biomes_netherblocks.md), [biomes](worldgen_biomes.md), [flowers](worldgen_flowers.md), [locate](worldgen_locate.md), [seasons](worldgen_seasons.md) |
| structures | [contents](structures_contents.md), [earthfill](structures_earthfill.md), [metas](structures_metas.md), [netherblockswap](structures_netherblockswap.md), [new](structures_new.md), [rates-and-groups](structures_rates-and-groups.md), [terrain](structures_terrain.md), [testing](structures_testing.md) |
| mobs | [bees](mobs_bees.md), [mooblooms](mobs_mooblooms.md), [nests](mobs_nests.md), [pillagers](mobs_pillagers.md), [spawns](mobs_spawns.md), [tweaks](mobs_tweaks.md) |
| loot | [lootjs-layer](loot_lootjs-layer.md), [lootr](loot_lootr.md), [tables](loot_tables.md), [trader](loot_trader.md) |
| gear | [archery](gear_archery.md), [camera-bags](gear_camera-bags.md), [echo](gear_echo.md), [shields](gear_shields.md), [tuning](gear_tuning.md) |
| potions | [effects](potions_effects.md), [enchanting](potions_enchanting.md), [potion-buckets](potions_potion-buckets.md), [potions](potions_potions.md) |
| recipes | [cooking](recipes_cooking.md), [engineering](recipes_engineering.md), [magic](recipes_magic.md), [materials](recipes_materials.md), [metal](recipes_metal.md), [progression](recipes_progression.md), [wood](recipes_wood.md) |
| engineering | [dreams-and-desires](engineering_dreams-and-desires.md), [sawmill-and-components](engineering_sawmill-and-components.md) |
| gameplay | [blockrunner](gameplay_blockrunner.md), [time](gameplay_time.md), [misc](gameplay_misc.md) |
| items | [item-physics](items_item-physics.md), [jars](items_jars.md) |
| ui | [effects](ui_effects.md), [names](ui_names.md), [tooltips](ui_tooltips.md), [wthit](ui_wthit.md) |
| alcohol | [pack](alcohol_pack.md), [vineryport](alcohol_vineryport.md) |
| blocks | [new](blocks_new.md) |
| jei | [jei-coverage](jei_jei-coverage.md) |
| largecontent | [mod-additions](largecontent_mod-additions.md) |
| ports | [veggiesport](ports_veggiesport.md), [vintageport](ports_vintageport.md) |
| creativedelight | [framework](creativedelight_framework.md), [members](creativedelight_members.md), [qol](creativedelight_qol.md), [swappers](creativedelight_swappers.md) |
