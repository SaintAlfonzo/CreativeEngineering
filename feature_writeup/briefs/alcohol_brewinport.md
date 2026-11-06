# alcohol / brewinport: implementation brief

Written 2026-10-31 from [TODO](../todo/TODO.md) `alcohol/brewinport` (row 4 open, In Progress; rows 1, 2, 5, 6, 7, 8 and the
`#2, #4` dump-reading row are Testing). Layout per [README.md](README.md). Feature writeup: [alcohol.md](../alcohol.md) Part A
(test prefix `AL-`, last id AL-96; AL-97 is proposed in [alcohol_pack.md](alcohol_pack.md)). Bundle:
[creative_brewing.md](../creative_brewing.md) (`CBR-`).

**Where the port lives now (2026-10-31):** `development/creativeBrewing/members/brewin` (Gradle project `:backportedBrewin`,
version `4.5.0-ce-19.2-05`), nested in `creativebrewing-1.0.0.jar`. Build and install only through `:creativeBrewing:`
(a member's own `installToMods` refuses). `mods/creativebrewing-1.0.0.jar` (2026-10-31 16:55) is byte-identical to
`development/creativeBrewing/build/libs/`, and `logs/latest.log` shows it booted 2026-10-31 17:15
(`[creativebrewing] members: [vinery, brewinandchewin]`, no Vinery / Brewin' errors). Older rows that say "`-04` installed,
not yet booted" describe the standalone jar this replaced.

## Scope and order

| Step | Row | Work | Notes |
|---|---|---|---|
| 1 | `brewinport #4` pack-side leftovers | tool run, then close | Most of it is done; only the stack-size generator run on the 2026-10-30 dump is left, and that is `pack/release #5` |
| - | `#1`, `#2`, `#5` to `#8`, `#2, #4` dumps | play | Built, needs play (table at the end) |

---

## brewinport #4: Pack-side leftovers
Status **In Progress** / Work: tool / KubeJS / id `alcohol/brewinport #4`

### Goal
Finish the pack-side changes the 4.5.0 port needs (alcohol.md 3.4): compost, BlockRunner, food types, stack sizes and the
Canned Kimchi effect copy.

### Now (checked 2026-10-31)
- **Compost: done.** The port's chances are rows in Creative Delight
  [brewinandchewin/compost_values.js](../../development/creativeDelight/src/main/resources/kubejs_startup/brewinandchewin/compost_values.js)
  lines 17-19 (kimchi 0.5, quiche and quiche slice 0.85; moved there from the pack in the 2026-10-25 CD move). Pickled
  Pickles has no row any more: it was merged into Vintage's Pickled Sea Pickle (food_types.json lists `pickled_pickles` under
  `excluded`).
- **BlockRunner: done.** `heating_cask` / `ice_crate` are tagged in [blocktags.js](../../kubejs/server_scripts/tags/blocktags.js).
- **Canned Kimchi copy: done.** [can_types.json](../../kubejs/generation_scripts/food/can_types.json) `canned_kimchi`
  (line 5939) has `"effects": []`, and no `satisfaction` string is left anywhere in `kubejs/` outside `exported/`. The row and
  `pack/release #5` still list it as open.
- **Food types: done.** Of the 32 Brewin' foods in the 2026-10-30 `/dumpfood` (`kubejs/exported/food/foods.json`), all have
  item_dict keys; 31 sit in a type in [food_types.json](../../kubejs/generation_scripts/food/food_types.json) and
  `pickled_pickles` is excluded. Untyped Brewin' keys are blocks and tools only (keg, coaster, heating cask, ice crate,
  tankard, cheese wheels, whole quiche / pizza, which are typed through their slices).
- **Stack sizes: not rerun on the new dump.** [stack_sizes.json](../../kubejs/generation_scripts/stacks/stack_sizes.json)
  (generated 2026-10-27) already sizes 28 Brewin' keys (drinks 32, pizza 8, keg 64...); `kimchi`, `jerky`, `kippers`,
  `cocoa_fudge` and the two cheese wedges are in no size list (they keep the mod's size). `gen_stack_sizes.py` on the
  2026-10-30 dump is still queued under `pack/release #5` (CQ-DUMP-04).

### Build
1. Run `python3 tools/gen_stack_sizes.py` on the 2026-10-30 dump (the `pack/release #5` run; one run serves both rows). Diff
   `stack_sizes.json` for `brewinandchewin` keys: the six unlisted foods should land by the generator's rules (kimchi, jerky,
   kippers like the other plain foods). Edit the rules in the tool, never the json.
2. `python3 tools/food_catalog.py --check`: expect no Brewin' food reported as untyped.
3. Close the row (the S tier passed on the scratch server, SQ-OM-03); the P tier is the stack sizes in play.

### Ids and data
No new ids. Item keys are the existing `i.kimchi`, `i.jerky`, `i.kippers`, `i.cocoa_fudge`, `i.flaxen_cheese_wedge`,
`i.scarlet_cheese_wedge` in [item_dict.js](../../kubejs/startup_scripts/_lib/item_dict.js) (lines 1798-1810 and 11834-11874 hold the
Brewin' block).

### Decisions
None open.

### Gotchas
- The stack generator and `food_catalog.py` read `kubejs/exported/`; running them on an older dump drops port items.
- A startup restart applies `stack_sizes.js`; `/reload` does not.

### Tests
- **AL-98 (proposed) Brewin' stack sizes:** kimchi, jerky, kippers, cocoa fudge and a cheese wedge stack to the size the
  generator gives similar foods; a tankard drink stacks to 32; a keg to 64.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| brewinport #1 | Brewin' and Chewin' 4.5.0 port | Testing | alcohol.md Part A; AL-01 to AL-84; open S+ C~ P. Now `-05` nested in Creative Brewing 1.0.0, booted 2026-10-31 17:15 (the row still says `-04` not booted) |
| brewinport #2 | Heat / cold tag audit | Testing | alcohol.md 2.7, 10.13; AL-41, AL-77; open S C~ P. The 2026-10-30 tag dump holds 67 `#brewinandchewin:heat_sources` and 14 `freeze_sources` blocks (snow layers included, by the owner's 10.13 list; AL-41's "a snow layer doesn't count" is stale) |
| brewinport #2, #4 | Reading the 2026-10-30 dumps (`tags/item_tags.json`) | Testing | CQ-DUMP-06. The tag half is read above (#2); the food half is #4's Now. What is left is the stack generator run (#4 Build 1) |
| brewinport #5 | Black and purple tankard slot icon | Testing | alcohol.md 8.9 Q1, 10.16; AL-78; open C P |
| brewinport #6 | Remainder buffer on the keg | Testing | [remainder_buffer.md](../remainder_buffer.md) 3.6, alcohol.md 10.16; AL-79 to AL-81, AL-84; open S~ C P (slot half PASSED SQ-OM-01) |
| brewinport #7 | Brewin' slots and side input | Testing | alcohol.md 10.16; AL-82, AL-83; open S+ C P (SQ-OM-01 PASSED) |
| brewinport #8 | Coasters | Testing | alcohol.md 12; AL-91 to AL-96; open S+ C P (SQ-OM-02 PASSED); 302 foods and drinks since 2026-10-29 |
