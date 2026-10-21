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
| food | [canning](food_canning.md), [meals](food_meals.md) |
| farming | [composting](farming_composting.md), [trees](farming_trees.md) |
| worldgen | [locate](worldgen_locate.md), [seasons](worldgen_seasons.md) |
