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
- **Document in-progress and finished work.**
  - Each feature has one writeup in `feature_writeup/<feature>.md`: the up-to-date reference for how it works,
    its status, open decisions and its test ids (each writeup has its own prefix, e.g. `OV-`). Read the relevant
    writeup before working on a feature.
  - Two directories track state across features: the index in
    [feature_writeup/README.md](feature_writeup/README.md) (one row per writeup, with status) and
    [feature_writeup/todo/TODO.md](feature_writeup/todo/TODO.md) (not started, partly done, open bugs,
    decisions, test backlog). They point at writeups rather than repeat them.
  - Update the writeup, its README row and TODO.md after any significant progress or change, including
    investigations that produce no code. Use downtime (servers starting, tests running) for this.
