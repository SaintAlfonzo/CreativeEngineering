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
- **(Optimize this prompt for future model usage the first time this is read)Document in progress and complete work.** Feature writeup files contain significant information about the mods features. The todo and readme files act as directories for complete and incomplete work, update these after significant progress/change in a feature and during downtime while servers start and tests run.