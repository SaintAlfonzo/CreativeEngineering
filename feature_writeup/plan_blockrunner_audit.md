# Plan: BlockRunner audit

Status: Built, waiting for the game (2026-11-10). Closes TODO `gameplay/blockrunner #2` and the leftovers of `#5`.

**Progress 2026-11-10:**
- Phase 0: done offline. `/dumpall` was not rerun; the 22:13 items dump and the 00:58 tags dump were used.
- Phase 1: done and built, not installed. Command `/dumpsurface` (not `cedumpsurface`, matches the `dump*` set, in `/dumpall`)
  in Creative Devtools, reading `RunSpeed` in Creative Compat; generator `blockrunner_audit` is a separate entry (one
  entry has one `args` list, and `--audit` must not replace the untagged-list run). Live config keys appear on the
  next start (backup in `configs_edited` updated).
- Found on the way: every `@mod` tag entry was dropped since 2026-11-06 (`has.namespace`), fixed in Common `has.js`.
- Phases 2 and 3: done in one pass (`blocktags.js` "Audit 2026-11-10" block). Sim 13828 ids, 0 overlaps. The name
  scan also hid walkable tables, shelves and lamps; those are tiered too.
- Left, needs the game: install Common + Compat + Devtools, restart, `/dumpsurface`, `--audit` (BR-59..64);
  the outlier decisions (step 4 of phase 1, `#5`); magma (`auditSkip`, untiered until decided).
- Not done here: the Blocky Siege cast iron swap (its own change, see the note below).
Feature doc: [blockrunner.md](blockrunner.md). Earlier design of the tooling: [briefs/gameplay_blockrunner.md](briefs/gameplay_blockrunner.md)
(`#2` Build steps 1-5; this plan keeps that design and reorders the work by recency).

## Goal

1. Every walkable block in the pack has a tier, or is on a reviewed "untiered on purpose" list.
2. Recently added mods are done first.
3. A watcher flags new untiered walkable blocks, so the next mod cannot silently walk at 1.00.

## Where things stand

- Last full re-check: 2026-10-02 (pack 1.0.4). Since then only point fixes: Brewin' (10-19), Vintage Delight port (11-01),
  Visceral Heap flesh (11-10).
- Mods with blocks and no tier work yet (brief, checked 2026-10-31): Botany Pots, Fungi Delight port, Veggies Delight port,
  Miner's Delight port, End's/Oceanic/Ocean's Delight, Ender's additions, Hearth and Harvest port, Cooking for Blockheads
  additions, Hostile Delight, creativedelight itself, Neapolitan ice cream blocks.
- New since 10-31, not yet looked at: everything in 1.2.00 to 1.2.02 (Immersive Portals / Portal Gun, Biomes O' Plenty
  update 11-09, repackedStorage 11-07, Creative Common / Compat / Equipment / Magic / Worldgen / Calendar / Brewing
  carve-outs), plus Rustic Delight fold-in and any block the Creative jars gained when carved.
- Tooling: `tools/blockrunner_scan.py` (regex "could register" filter, no real shapes) and `blockrunner_tag_sim.js`
  exist. `/dumpsurface`, `--audit` and the watcher do not.
- Test ids: BR-58 is the last used; new tests start at BR-59 (the brief's BR-53/54 are taken).

## Phase 0: refresh inputs (no edits)

1. Confirm no game JVM is running (CLAUDE.md redacted `ps` command).
2. In game: `/dumpall` (or `/dumptags block` + `/dumpitemsbymod`) so `kubejs/exported/` matches the installed mods.
3. `node tools/blockrunner_tag_sim.js /tmp/t.json` then `python3 tools/blockrunner_scan.py /tmp/t.json`: get today's untiered
   list per mod. Save the output in the scratchpad; this is the work list for phase 2.
4. Build the mod-recency order: from `deploy/DEPLOY_LOG.md` "Mods added" per release plus jar mtimes in `mods/`.

## Phase 1: watcher tooling (build once, then every later phase is cheap)

Design as in the brief; only changes are listed.

1. `devtools/SurfaceAudit.java` in Creative Common (modelled on `WthitIconDump`): server command `/dumpsurface` writes
   `kubejs/exported/surface_audit.json` with, per block default state: id, collisionTop, tier, override, friction,
   speedFactor, jumpFactor, inside, has item. Expose `RunSpeed.REGISTER_HEIGHT` package-visible. Errors listed per block.
   Check where `RunSpeed` lives now (it is in creativeEngineering per the brief; Common cannot depend on it, so either put
   the command in creativeEngineering devtools or read the tier through the tag only).
2. `blockrunner_scan.py --audit <json>`: list blocks with collisionTop >= 0.2, an item, no tier/override/native factor, not in
   a tier (option B, see below); group per mod; exit 3 if any.
3. `tools/generators.json`: extend `blockrunner_scan` with the new dump, args, `warnExit: [3]`, highlight.
4. Also have the dump flag the friction/jump outliers (friction != 0.6, jumpFactor != 1, speedFactor != 1) on tiered and
   untiered blocks. This closes `#5` ("check the other blocks' speed factors"). Each outlier gets a decision: reset in
   `block_properties.js` (like D&D asphalt) or keep and record it.
5. Build: `./gradlew :<mod>:build --offline`, `installToMods` only after the game is closed.

Decision (2026-11-10): option B. Fill the 1.00 tiers so every walkable block is typed; the watcher flags any walkable
block with no tier, and no baseline file is needed (drop `--write-baseline` and the baseline step in phase 3).
Before building, read how BlockRunner treats a 1.00 multiplier (does it still attach a modifier? Soul Speed, sprint,
FOV) and check that WTHIT `hideNormal` hides 1.00 tiers. Tier enum: `creativeCompat/.../surface/SurfaceTier.java`
(not Common); every new tier needs an enum entry (default speed + description), a `[surface.speed]` key and a
`runner` entry in blocktags.js. The sim needs `has.js` loaded (fixed 2026-11-10).

## Phase 2: audit by recency (tier newcomers)

Work each group in this order. For every group: list its walkable blocks (dump), decide by precedent (blockrunner.md 2 and
the 10-02 / 11-01 History entries), edit `kubejs/server_scripts/tags/blocktags.js` with `t.addMatching`, keys via
`global.item` for single ids (Creative Delight members: keys in their CD item tree), re-run the sim (0 overlaps, no dead
patterns), commit-sized chunks.

| Order | Group | Notes |
|---|---|---|
| 1 | 1.2.00 to 1.2.02 additions: Immersive Portals / Portal Gun, BoP 17.1.2.544, repackedStorage, loading tips (no blocks), Compat/Equipment/Magic/Worldgen/Calendar/Brewing blocks | newest, least checked; BoP update can add terrain blocks |
| 2 | Creative Delight members: Fungi (huge mushrooms, `fungiport #2`), Veggies (`veggiesport #1`), Vintage rest (`vintageport #2` leftovers), Miner's, Rustic, End's/Oceanic/Ocean's, Ender's additions, H&H, CfB additions, Display, Hostile Delight, Neapolitan ice cream blocks (per `baking/sweets` D5, same tier as Delightful's, very_sticky recommended) | the port rows list tiers as a leftover; many are food blocks, confectionery/soft/wood precedents |
| 3 | Creative Brewing: Vinery 1.5.5 port ids (only visible in a game dump) and Brewin' stragglers | |
| 4 | Botany Pots (98 blockstates, mostly functional/untiered), Create: The Salt, Volcanic Caverns, Subterrestrial, Underground Bunkers, Campsite Structures, Firefly Bush, Sticky It! | the brief found no blockstates in most; just confirm against the dump |
| 5 | In-tree mods: creativeEngineering (oven, culture, coils, dust, placed dishes), Magic, Equipment, Labels, Exposure | |
| 6 | Everything else (full sweep, see phase 3) | |

Per-group definition of done: sim 0 overlaps, group's untiered list reviewed, outliers (friction/jump) decided.

## Phase 3: full coverage sweep

Counts below are from the 2026-11-10 sim + scan: about 4,365 untiered, non-thin blocks in 144 mods (keyword buckets,
approximate). Rule: real building blocks get a real tier first (below), then the rest go into the 1.00 buckets.

### Tiers after this phase

| Tier | Speed | Status | Holds | About |
|---|---|---|---|---|
| `machinery` | 1.00 | existing, fill | processing machines, belts, shafts, casings, tanks, pumps, boilers, plus logic and electronics (gates, relays, computers) | 400 |
| `functional` | 1.00 | existing, fill | altars, apothecaries, waystones, sharestones, avatars, anvils and similar | 120 |
| `decorative` | 1.00 | existing, fill | trim, girders, vents, stepping stones, coin piles, trophies, statues | 270 |
| `furniture` | 1.00 | new (storage merged in) | benches, desks, counters, couches, curtains, flags, wallpaper, plus chests, crates, barrels, drawers, cabinets, shulker boxes, sacks, presents | 1,200 |
| `flora` | 1.00 | new | plants and nature props that can register | 300 |
| `confectionery` | 0.85 | existing | placed and plated dishes including plates, jars, bottles, jellies, cakes (the old `dishes` bucket, ~740; decided 2026-11-10: all at 0.85), plus every chocolate block | +740 |

Speed for the new tiers is 1.00 like the reserved ones; the enum entries need a description line each, and the
descriptions of `machinery`, `functional` and `decorative` lose "Not filled yet".

### Decisions on the leftover ~1,400 (real building blocks)

- Chocolate blocks (Neapolitan chocolate bricks, Create Confectionery chocolate brick slabs and the like) go in
  `confectionery`, even where the name says brick.
- Other brick and shingle families (Upgrade Aquatic stone bricks, shingles, and similar) go in `constructed_brick`.
- Pavements (Alex's Abnormalities scute pavements and stairs, similar) go in `constructed_smooth`.
- Cast iron goes in `metal` (Create Deco's cast iron block and the Dustrial Decor one are already or will be there).
- The 71 untiered Framed Blocks belong in `camouflage`; first find out why the sim shows that tier with 2 members.

### Note: Blocky Siege cast iron (needs its own change)

Blocky Siege's cast iron block duplicates Dustrial Decor's `dustrial_decor:cast_iron_block` (`i.cast_iron_block`).
To do, outside the audit itself:

1. Swap every recipe that uses Blocky Siege cast iron (block and its stairs/slabs) for the Dustrial Decor cast iron block.
2. Put the Blocky Siege cast iron texture on top of the Dustrial Decor block (resource override, check
   [resource-pack-lang-priority] and the shared-asset-path memory for jar precedence).
3. Remove `blocky_siege:block_of_cast_iron` (and its variants) from the pack.
4. The Blocky Siege cast iron ids then leave blocktags.js (they are untiered today); add a swap-list row so saves
   convert the old ids. Changelog bullet.
Decided 2026-11-10: Create Deco's `createdeco:cast_iron_block` (`item.block_cast_iron`) stays and is renamed Cast Iron
Plating (client_scripts/namechanges.js, done, changelog bullet under Blocks); Dustrial Decor's Cast Iron Block stays
the plain one. Both are in `metal`.

### Steps

1. Sort flagged blocks by mod, in mod-recency order, then alphabetically for the 1.0.0 base set.
2. Per mod, one wildcard per family into its tier in blocktags.js; blocks that are really thin or never walkable stay out
   by the collision-height rule (the audit dump), not by name.
3. Native speed properties that are not 1 (friction, jump, speedFactor) are fixed in block_properties.js or recorded.
4. Re-run the sim (0 overlaps, no dead patterns) and the audit dump: the watcher must list nothing.
5. Spot-check `camouflage`, `glass`, `sticky`/`very_sticky` membership against current mods (sticky survey in
   blockrunner.md 3.9 already has proposals).

## Phase 4: close out

1. blockrunner.md: History entry (mods checked, what was tiered, counts), section 6 gets the `--audit` mode, file map gets
   `SurfaceAudit`, new tests.
2. Tests (start at BR-59): audit dump content (asphalt tier, soul sand speedFactor 0.4 untiered, carpet collisionTop
   0.0625), `--audit` lists nothing after phase 3, watcher catches a removed tier (remove `item.ice_crate`, expect it
   listed), plus one in-game walk check per new tier assignment worth feeling (food blocks, mushrooms, ice cream).
   Add scratch queue rows in `scratch_queue.md` (do not boot a server unless asked).
3. TODO: `gameplay/blockrunner #2` and `#5` to Testing/Done; sweep the Brewin'/port leftover rows
   (`fungiport #2`, `veggiesport #1`, `vintageport #2`) that this finishes.
4. Changelog bullet in `unreleased.md` under a BlockRunner family if any tier changes a player-visible speed (likely yes);
   run `python3 tools/changelog_lint.py`.
5. Hand-over lists the BR test ids and any jar waiting for `mods/`.

## Risks and notes

- Pure KubeJS tier edits need `/reload` only for blocktags.js, but new `global.item` keys and Common/CD helper changes need a
  rebuild and restart.
- One tier per block: always run the sim; overlaps give a random tier per reload.
- Thin or never-walkable blocks stay untiered by the collision-height rule, which the watcher applies, so they do not reappear.
- Dump `kubejs/exported/` is not in the repo; the tier assignments in blocktags.js are the record.

## Order of work in one line

Phase 0 refresh, Phase 1 tooling, Phase 2 groups 1 to 5 newest first, Phase 3 full sweep into the filled 1.00 tiers (no baseline), Phase 4 docs and tests.
