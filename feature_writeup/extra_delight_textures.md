# Extra Delight: texture comparison plan

Plan only, nothing generated. Written 2026-11-09 (ruling R5 in [extra_delight.md](extra_delight.md)). Keep this file and
its output folder: they are the permanent record of how ED textures compare to the pack textures that replace them.

## Purpose

ED is applied on top of the pack (installed mods win shared items, extra_delight.md 2.6), and under R1 / R3 every station and
all furniture and decor is registered for evaluation. Before anything is kept or dropped, you want to see ED's art next to the
art the pack already has, **by group**, so a drop or a keep is a visual decision and not a name match.

## Output

| Path | What |
|---|---|
| `feature_writeup/extra_delight_textures/<group>.png` | one contact sheet per group (list below), committed |
| `feature_writeup/extra_delight_textures/pairs.tsv` | the pair list the sheets are built from: group, ED id, ED texture, pack id, pack texture, ED decision, verdict (empty until reviewed) |
| `feature_writeup/extra_delight_textures/README.md` | generated index: group, sheet, pair count, date, jars and pack versions used |

Each sheet is a grid; a row is one pair: **ED texture on the left, pack texture on the right**, both scaled with nearest
neighbour (4x for 16px, 2x above 32px, same pixel scale left and right), the two ids under them, and a coloured edge
(green = ED planned to be kept, red = ED dropped, amber = eval). Items with no pack counterpart (new content, 3.1) are
**not** in the sheets except where a group says so. Sheets stay under 2000 px wide so they open in an editor preview.

## Groups

One sheet per group; the pair sources are the 2.1 / 2.3 / 2.6 tables.

| # | Group | ED side | Pack side |
|---|---|---|---|
| 1 | Crops and produce | corn, seeds, cob, wild corn; cucumber; peanut; garlic; mint; coffee cherry, beans, ground; lemon, orange, apple; sunflower seeds; cactus; salt | Corn Delight, Vintage, Veggies, Neapolitan, Respite, Fruits Delight, H&H, Delightful |
| 2 | Trees, saplings, leaves | apple, lemon, orange (dropped), lime, grapefruit, hazelnut, cinnamon (kept) | Fruits Delight and Vinery trees |
| 3 | Crates and sacks | egg, cucumber, garlic, berry, lemon, orange, corn crates | Incubation, Vintage, Veggies, Quark sacks, Fruits Delight |
| 4 | Staples and ingredients | cornmeal, butter, cheese, chili powder, BBQ sauce (jar), roasted garlic, corn husk, frosting, pasta shapes | pack cornmeal, H&H butter, Vintage cheese, Barbeque's, Veggies, `creativeengineering:frosting`, FD raw pasta |
| 5 | Dishes dropped or merged | potato soup, rice pudding, sweet berry custard, rack of lamb, bacon egg sandwich, mac and cheese, glazed carrots, trail mix, curry, fudge, french fries, stuffing | Large Meals, More Delight, H&H, Neapolitan, Brewin', Ender's, Corn Delight |
| 6 | Chocolate, candy, frozen | chocolate bars, truffles, dipped, marshmallow, ice cream, milkshakes, popsicles | Create Confectionery, Neapolitan, More Delight |
| 7 | Fluids and buckets | the 35 fluids that map to an existing fluid (juice, oil, vinegar, broth, jam, ketchup, mayo, milkshake, chocolate syrups, honey and similar) | pack fluids (alcohol.md 4.1, Central Kitchen, potion buckets) |
| 8 | Workstations | oven, 8 pans, mortar, juicer, melting pot, vat, evaporator, keg, mixing bowl | pack oven and bakeware, `foodGrinding` mortar, Apple Press, Create mixer, Vintage fermenting jar and evaporator, Brewin' / H&H keg |
| 9 | Furniture: cabinets, stools, kitchen | half cabinets (13 woods + cinnamon + fruit), step stools, sinks, counters, spice racks, food displays, knife blocks | H&H half cabinets, `creativeengineering:*_stepstool` |
| 10 | Decor | wallpapers (one wood, 16 colours each), bowls, boxes, gingham, ribbons, wreaths | `design_decor` wavy wallpaper, `creativedelight:*_fruit_basket` |
| 11 | Effects | Pickled, Pucker Up, Sunshine | H&H Clarity, Fruits Delight Brightening |
| 12 | Cornfield | corn husk doll, dried corn fence, corn items in the confiscate tag | H&H corn husk, Corn Delight |

Groups 9 and 10 compare **block** art and need a model render, not a flat texture; see Method.

## Method

1. **Pair list first.** A script (`tools/extra_delight_texture_pairs.py`, listed in `tools/generators.json` once it exists) reads
   the 2.1 / 2.3 / 2.6 table rows from a small hand-kept map (`tools/extra_delight_texture_map.json`: group, ED id, pack id) and
   writes `pairs.tsv`. Ids, not names: the map is the single place a pair is declared.
2. **Texture lookup.** ED textures from `DecompiledMods/extradelight-2.6.6/assets/extradelight/textures/{item,block}`. Pack
   textures **as the player sees them**: resource pack folders and zips under `resourcepacks/` first, then the mod jars,
   following the real load order (`resourcepacks/` priority, memory `resource-pack-bench`); `kubejs/assets` generated models
   outrank jars (`.model()` still generates a block model, memory `kubejs-generated-assets-outrank-jars`), so an item whose
   model is KubeJS-made is read from its model JSON, not the jar. An id with no flat texture falls to step 3.
3. **Block and model art.** For furniture, wallpapers, stations and anything with a multi-part model, render a 3D preview
   from the item model using the existing tooling in `tools/` where it covers it; otherwise the pair gets an explicit
   "needs in-game screenshot" placeholder and a scratch-queue row (skill `scratch-test`), never a guessed flat texture.
4. **Sheet build.** `tools/texture_audit.py --sheet` already builds a Pillow contact sheet from a "namespace textures/path"
   list; the compare mode is a sibling (`--compare PAIRS OUT`) that draws two columns per row instead of one grid cell.
   Reuse its texture index so there is one indexing pass, not two.
5. **Record.** After you review a sheet, the verdict goes into `pairs.tsv` (keep ED / keep pack / keep both / drop both) and
   the decision lands in extra_delight.md (the D row it belongs to). The sheet is regenerated, never edited by hand.
6. **Refresh.** Regenerate when ED's art is replaced, the pack's textures change, or a resource pack changes the pack side. The
   README stamps the date and what it read.

## Order of work

1. Write the map JSON from the overlap tables (about 120 + 25 ids; the dishes and crates dominate).
2. Build groups 1 to 7 and 11 (flat textures only), review.
3. Groups 8, 9, 10, 12 (model renders; screenshot rows in `scratch_queue.md` if the render tool cannot do them).
4. Fold verdicts into the decisions.

Cost: step 2 is one script and one Pillow dependency (9.0.1 is installed); step 3 is the unknown, which is why those groups
come last.

## Not covered

New content with no pack counterpart (the roughly 520 kept ids) is not compared: there is nothing to put beside it. A
"new items" sheet per line is a separate, optional step if you want to see the kept art too.
