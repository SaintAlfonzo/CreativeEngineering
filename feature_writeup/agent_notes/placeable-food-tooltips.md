# placeable-food-tooltips

> Placeable / on plate / on small plate tooltip lines for foods from #creativeengineering:placeable* tags; /dumpplaceable + tools/placeable_catalog.py; DD links = loot drop AND tag; status in feature_writeup/placeable_food.md (PL-)

Moved from agent memory 2026-11-16 (last edited there 2026-10-05). Repo status docs win over any status line here.

Built 2026-10-05 at the user's request. Everything is in `feature_writeup/placeable_food.md` (PL- tests); generated catalog `feature_writeup/placeable_catalog.md`.

- Pipeline: `/dumpplaceable` (in /dumpall) -> `tools/placeable_catalog.py` (generators.json `placeable_catalog`) -> `generation_scripts/food/placeable.json` (item_dict keys) -> `tags/placeabletags.js` -> `client_scripts/placeable_tooltip.js` (`addAdvancedToAll` + `stack.hasTag(id)`).
- Display Delight only links a food when a registered DD block's first loot drop is that food AND the food has the matching `displaydelight:*` tag (DisplayEvents.onWorldLoad). The DD jar ships 25 loot tables for unregistered blocks (hamburger, bowl, dog_food...). The tool reads the registered names from the DisplayBlocks/PlatedBlocks/SmallPlatedBlocks class constants.
- Sneak-only placement: DD placed, Fruits Delight `FDBlockItem` (jams, mangosteen cake). Vinery bottles place on a plain right click on a block.
- The pack strips DD's and Fruits Delight's own placement lines by translation key. DD config was not changed.
- The catalog can be built from fallback data (`/dumpfood` plus the Display Delight jar) when `/dumpplaceable` has not run; state in the writeup.

Related: [display-delight-icons](display-delight-icons.md), [missing-names-catalog](missing-names-catalog.md), [food-tuning-system](food-tuning-system.md), memory `feature-writeup-pattern`, memory `rhino-fieldandmethods-gotcha`.
