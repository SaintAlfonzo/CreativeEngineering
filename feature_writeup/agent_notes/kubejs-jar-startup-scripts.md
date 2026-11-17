# kubejs-jar-startup-scripts

> patcherMod's startup loader (kubejs_startup/ in mod jars) and the per-pack scope gotcha that decides what can move into a jar

Moved from agent memory 2026-11-16 (last edited there 2026-10-25). Repo status docs win over any status line here.

Since 2026-10-25 patcherMod loads KubeJS startup scripts from any mod jar's root `kubejs_startup/` folder
(`compat/kubejs/JarStartupScripts`, mixin on `ScriptManager.loadFromDirectory` RETURN). Order: patcherMod's pack
(NCH, tags, compost), then the pack's `startup_scripts/`, then other jars (Creative Delight). Status and tests:
feature_writeup/creative_delight.md 19.

**Gotcha:** KubeJS gives each script pack its own scope. Only `global` is shared, so a top-level `var` in a jar
script is invisible to the pack folder's scripts and vice versa. Before moving a startup script into a jar, grep the
pack for uses of its top-level names. Code that runs before the dicts must read `global.item` at call time.

**Why:** found while moving the helpers (Q26). NCH's top-level `var i = global.item` had to become call-time reads.

**How to apply:** an edit to a moved helper needs a patcherMod rebuild and restart. Offline checks: compile with
the Rhino fork (memory `kubejs-headless-rhino-harness`). Live checks: queue SQ-CDT (memory `scratch-test-now-or-batch`).
