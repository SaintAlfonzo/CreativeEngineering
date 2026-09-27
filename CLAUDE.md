# CreativeEngineering2 — working notes

## Preferences

- **Centralize recipe and tag creation in the NeoCraftHelper functions.** Recipes go
  through `global.NCH` helpers ([kubejs/startup_scripts/func/neoCraftHelper.js](kubejs/startup_scripts/func/neoCraftHelper.js));
  tags go through `global.tags` ([kubejs/startup_scripts/func/tag_helper.js](kubejs/startup_scripts/func/tag_helper.js)).
  Don't hand-write `e.shaped(...)` / `e.custom(...)` / `event.add(...)` in server scripts
  when a helper covers it — extend the helper instead.
- **Define each item in one location and reference it everywhere else.** Item ids live in
  `global.item` / `item_dict.js` (and block/fluid dicts); server and startup scripts pull
  from those keys rather than repeating literal `namespace:id` strings.
