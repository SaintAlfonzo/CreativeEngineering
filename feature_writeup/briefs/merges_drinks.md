# merges / drinks: implementation brief

Written 2026-10-27 from [TODO](../todo/TODO.md) `# merges`, Sub `drinks` (row 1). The plan text (survivors, recipes, names, textures)
is [item_merges.md](../item_merges.md) section 5; rows moved here from other families carry `(was old-id)` in Details and
their former brief section (e.g. `dishes_meals.md`, `recipes_materials.md`, `mobs_tweaks.md`) still holds the code facts.

## How to build any row

1. Add the `[purged, survivor]` pairs with `global.addItemMerges` in their group (item dict keys, never literal ids; food:
   Creative Delight's `<mod>/item_merges.js`, item_merges.md 1.1), then remove the duplicate recipes the swap leaves and
   add the survivor to the tags the purged item was in, in the group's server file (item_merges.md section 1). Recipes go through `global.NCH`, tags through `global.tags`.
2. New names and tooltips go in `client_scripts/namechanges.js` and the lang overrides; recoloured textures come from
   `textureswapper/` (copy the originals first).
3. Add an item_merges.md section 2.x with the built details, queue the live checks in
   [scratch_queue.md](../testing/scratch_queue.md) (`SQ-MG-` ids, next free number), and add a changelog bullet.
4. Merges that touch Creative Delight members (Rustic, Veggies, Vintage, More Delight) belong in the CD trees by mod
   (`creative-delight-routing`), not in the pack folders.

## Open questions

See each row's Decisions column. Status per row is in TODO; rows moved in as In Progress may have earlier partial work, see their old brief section.
