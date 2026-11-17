# sample-syringe

> creativeengineering:sample_syringe — one empty item + 7 distinct filled per-animal items (not NBT), consumed in cooking recipes to make lab-grown 'culture_*' meat; all 7 animals wired up

Moved from agent memory 2026-11-16 (last edited there 2026-09-12). Repo status docs win over any status line here.

`creativeengineering:sample_syringe` — right-click/attack a sampleable passive mob to swap it
for a distinct filled item id, e.g. `creativeengineering:cow_sample_syringe`. Design is now
**one real item id per animal**, not one item + NBT tag — NBT-based ingredient matching
(`.weakNBT()` etc.) kept silently breaking (see memory `kubejs-api-notes` history), so
identity moved into the item type itself, same relationship as book/written_book/enchanted_book.
All 7 animals are wired: cow, pig, chicken, sheep, rabbit, cod, salmon.

Registration: `startup_scripts/items/sample_syringe.js` (each filled variant uses
`.containerItem('creativeengineering:sample_syringe')` and `.glow(true)` — see
memory `kubejs-itembuilder-glow` for why glow is a builder flag now, not NBT).
Shared logic: `startup_scripts/func/sample_syringe.js` -> `global.sampleSyringe`
(`SAMPLES` map, `isSampleable`, `isFull`, `buildFilledStack`). Handlers (right-click/attack)
live in `server_scripts/engineering/sample_syringe.js`.

**Consuming it in a recipe**: `server_scripts/cooking/cultivated_meat.js` has shapeless recipes
for all 7 filled syringes (+ culture_base + slime -> `creativeengineering:culture_*` meat
precursors for `cooking.js`). Because each filled syringe is its own item id, these recipes
match by plain id — no NBT ingredient tricks needed anymore.
