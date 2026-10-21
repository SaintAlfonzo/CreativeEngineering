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
| food | [baked-goods](food_baked-goods.md), [canning](food_canning.md), [drinks](food_drinks.md), [drugs](food_drugs.md), [ingredients](food_ingredients.md), [kitchen](food_kitchen.md), [meals](food_meals.md), [oven](food_oven.md), [placeable](food_placeable.md), [seasoning](food_seasoning.md), [stack-sizes](food_stack-sizes.md), [sweets](food_sweets.md), [types-and-tuning](food_types-and-tuning.md) |
| farming | [composting](farming_composting.md), [crops](farming_crops.md), [trees](farming_trees.md), [wild-crops](farming_wild-crops.md) |
| worldgen | [bee-nests](worldgen_bee-nests.md), [biomes](worldgen_biomes.md), [biomes/netherblocks](worldgen_biomes_netherblocks.md), [flowers](worldgen_flowers.md), [locate](worldgen_locate.md), [seasons](worldgen_seasons.md), [structures/contents](worldgen_structures_contents.md), [structures/earthfill](worldgen_structures_earthfill.md), [structures/metas](worldgen_structures_metas.md), [structures/netherblockswap](worldgen_structures_netherblockswap.md), [structures/new](worldgen_structures_new.md), [structures/rates-and-groups](worldgen_structures_rates-and-groups.md), [structures/terrain](worldgen_structures_terrain.md), [structures/testing](worldgen_structures_testing.md) |
| mobs | [mooblooms](mobs_mooblooms.md), [nests](mobs_nests.md) |
