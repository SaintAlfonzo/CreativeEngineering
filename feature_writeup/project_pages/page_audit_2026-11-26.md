# Project page audit, 2026-11-26

Checked from source; nothing booted. Verdict: none of the three pages is up to date. "Page N" is a line number.

## Code problems on a standalone install (decide: fix the jar or change the page)

1. Common: the 12 vanilla-effect potions have no item names in the jar (only Magic Resistance has them; the others are in
   `kubejs/assets/kubejs/lang/en_us.json`). Confirmed from the jar's lang file.
2. Compat: block walking speeds need BlockRunner, and the surface tier tags are filled only by the pack's
   `server_scripts/tags/blocktags.js`. Sinking, camouflage and sticky tiers depend on the same tags (pages 3, 20, 32 and the
   file changelog).
3. Compat: `#creativecompat:vein_undo_denied` is filled only by the pack script, so a standalone vein undo refuses only
   block entities (page 76).
4. Compat: the jar's assets script rewrites the Cozy guide with pack-only facts; two fisherman trades are removed whenever
   Cozy is loaded, not only with its gender system off (page 98).
5. Raspberry Pie: bundled Carrot Edition keeps its own heart milestones and effects standalone; only the pack disables
   them. Document or disable.
6. Common: Fire Resistance is renamed "Fire Immunity" by the jar unconditionally (also two conditional renames); no page
   says so. `forceHudIcon` works only with Creative Compat. The WTHIT tag script ships without its block list.

## Creative Common page

- 51: first food in a row has no brain freeze roll; the second within 60 s is the first 20%.
- 33: JEI ordering helper belongs to Compat (also in `mod_description`); absent-mod summary is one line per mod per kind.
- 87: `[changelog]` is in the common config file, not client. 119: "most other features have a switch" is false.
- Version brackets wider than the code gates: Better Beacons [1.0.2,1.1), Immersive Weathering [1.2.10,1.3), WorldEdit
  [7.2.12,7.3), Quark [3.4-418,3.5), Armor Trims [1.3.3,1.4), Immersive Portals [2.3.7,2.4), Heartstone [1.1.2,1.2).
- 105: the launch crash fix runs by default only on Linux with 300 or more mod files (flag
  `creativecommon.closeStaleJars`). 41: config watcher covers the Creative mods' configs. 109: holder fix is an id check.
- Omitted: effect renames, all fluids listed in JEI, swapper config, JVM flags, modder helpers (`NCH.cfg`, trades, new
  dictionaries, swap list keys, tags), more idle-tick plants.
- Format: no Default column, "Settings:" instead of "Section:", shorthand names, benefit clauses, no screenshot comments.

## Creative Compat page

- 36: sawdust, ash, fodder and sand layers are stood on, not walked through; thorn chakrams and pickarangs catch in webs.
- 33: unframed Framed Flower Pots can take a plant (lines 32 and 33 also repeat camo order).
- 36: dust key is `[surface.passthrough.layers] dustParticles`.
- 80 silt pots with Rooted Dirt take every plant. 84 nightstand storage is new (27 slots). 88 adds text fields to config
  screens. 102 villagers avoid deep leaf piles; comb gains eye color and shape controls.
- 98: cats attack the mob hooked by their owner's feather; needs only Domestication Innovation.
- 62 framed glass and self-dropping glass excluded. 72 jar mixing needs a registered family (Creative Delight). 100/106
  cosmetic armor needs two named mods; emptied corpses still vanish in the End. 136 JEED compound lines. 140/152 WTHIT
  shows sheared state, switches are in several sections. 146 dimming option turns dimming off. 20 overstates version
  gating. 54 salt needs another mod; Snow! Real Magic overrides the light melting switch. 3: about 40 more mods, not 50.
- Omitted: leaf pile pricking, bamboo leaf drops, more layer behaviors, campfires burning items, salt tooltip, mirrors from
  more glass, JEI pages, tooltips and renames, recipe and tag changes, vine shearing, more WTHIT lines.
- Format: brackets or versions missing on many lines, license line must name third-party licenses, settings table lacks
  several sections and a Default column, screenshot comments placement.

## Raspberry Pie page

- 23: vanilla foods reach about 56, not 50. 31: aged-out foods leave memory; changing a covered or replaced slot is free;
  death also resets the food counters; `/solrp pause` only stops refreshing.
- 41 only locked unlocks and unreached buffs alternate. 47 drinks come from potion, milk and four listed tags. 51 sort
  lists differ for eaten and uneaten. 61 (and changelog 9) diversity files load only from mod jars. 65 reset clears much
  more; commands take several players. 69 diversity line does not need AppleSkin; Food Dictionary sorting needs Carrot
  exactly 1.14.0. 75 `minContribution` is only an info note.
- Omitted: Carrot hearts, Food Dictionary recipe, lunchboxes, default ladder amounts, action-bar messages, confirm panel,
  movable inventory bar, other Diet Log changes, milk not clearing diet effects, config file names.
- Format: heading grammar (line 15), "Optional mods" split by dependency, command spoiler layout, missing optional-mods
  line under Requirements.
