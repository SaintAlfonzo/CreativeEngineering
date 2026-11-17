# jeed-effect-descriptions

> Just Enough Effect Descriptions (JEED) tooltip coverage — how it sources text, and the placeholder-generator tool for effects it doesn't cover

Moved from agent memory 2026-11-16 (last edited there 2026-09-25). Repo status docs win over any status line here.

`mods/jeed-1.19.2-2.1.4.jar` (Just Enough Effect Descriptions, JEI addon) reads
`effect.<mod>.<path>.description` lang keys and falls back to "No description available"
when absent. It ships its own bundle (`assets/jeed/lang/en_us.json`) with descriptions for
many common addon effects, but only for mods it knows about — most of the 337 mods in this
pack aren't covered.

**tools/gen_effect_descriptions.py** scans every enabled mod jar's lang files plus
`kubejs/assets/**/lang/en_us.json` for effect name keys (`effect.<mod>.<path>`) and diffs
against found description keys. Anything missing gets a `"TODO: write a description for
<Name>."` placeholder written into `kubejs/assets/creativeengineering/lang/en_us.json`
(a virtual resourcepack overlay — namespace doesn't matter, all lang files merge into one
flat table at runtime, so no need to touch JEED's own jar).

Idempotent: re-running only fills still-missing keys or ones still carrying the TODO
marker; hand-written descriptions (marker removed) are left untouched, and placeholders
made obsolete by a mod update (real description now shipped upstream) are auto-pruned.
Re-run after adding/updating mods. New pack-original MobEffects that skip a lang name
entirely (e.g. `creativeengineering:magic_resistance`, set via a hardcoded
`Text.of(...)` display name — see [armor-tool-effects](armor-tool-effects.md) /
[magic-resistance-effect](magic-resistance-effect.md)) must be added to the `EXTRA_EFFECTS` dict at the top of the
script by hand, since they have no lang key for the scanner to find.

2026-10-20: generator only writes/keeps placeholders for REGISTERED effects (kubejs/exported/mob_effects.json), has a
`PORTED` dict (texts from other description mods) and `--sources` (scans jars in git-ignored
tools/.cache/effect_desc_sources/: JEED 1.16-1.21, MEED 8.1.0, Effect Insights, Effect Descriptions, Potion
Descriptions; key suffix `.description` or `.desc`). The overlay also holds hand fixes overriding JEED / Vinery donor
text (KubeJS pack wins). 10 placeholders left. As of 2026-10-19: 17 placeholder rows (10 live, 7 stale), see [effects-writeup](effects-writeup.md). As of 2026-09-24: 47 effects had missing descriptions (mostly `letsdo-vinery`'s ~15
custom effects, `better_beacons`, `brewinandchewin`, Xaero's minimap/worldmap "disable
feature" pseudo-effects, and the one pack-original `creativeengineering:magic_resistance`).
All were given placeholders; real text still needs writing by hand.
