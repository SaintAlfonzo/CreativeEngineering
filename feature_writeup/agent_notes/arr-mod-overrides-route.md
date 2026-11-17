# arr-mod-overrides-route

> How to change art/content of an ARR mod that is not a CD member (Ender's Delight, Cooking for Blockheads): new ids in a CD 'additions' member under its namespace; never ship a modified copy of its files

Moved from agent memory 2026-11-16 (last edited there 2026-10-26). Repo status docs win over any status line here.

Ender's Delight 1.2.2 is ARR, so it is not a Creative Delight member. Its MIT 1.1.4 content lives in the CD member
`enders_additions` (development/creativeDelight/members/enders/), which registers new ids under `endersdelight:` and has
a build guard (`checkNoArrResources`) that fails on any path the ARR jar also ships. Overrides of the ARR jar's own
files (the 12 retextures) go in `kubejs/assets/endersdelight/...` with an MIT notice, because the KubeJS pack sits above
mod jars and mod-jar load order is not reliable. Lang keys: `ClientEvents.lang` with the 3-argument `e.add(ns, ...)`.

**Why:** built 2026-10-25 (feature_writeup/ends_delight.md 5).
**How to apply:** same route for any other ARR mod the pack extends. The wild-crops system can also place features
in End / Nether biomes through a rule's `add` list ("where": []). See memory `creative-delight-routing`.

**Cooking for Blockheads (2026-10-31):** same route, member `blockheads_additions` (development/creativeDelight/members/blockheads/,
kitchen.md 9). Its author's permissions page forbids rehosting AND publishing modified files, so a kubejs/assets copy of
a CfB model with edits is not allowed either (the old fridge override was replaced by a baked-model wrapper in code).
New models may name CfB models as parents / its textures by id. Put member lang under the member's own namespace
folder (lang loads from every namespace) so `checkNoArrResources` never sees a shared path.
