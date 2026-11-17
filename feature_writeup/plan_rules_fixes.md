# Rules violation fix plan (design_rules.md section 9, V-01 to V-62)

Owner brief 2026-11-16 applied to the 2026-11-14 compliance audit in [design_rules.md](design_rules.md) section 9.
Status: P0 and P1 built 2026-11-17 (rule text, prefix lint and renames, docs moves, licence names, mod sides, date and
spelling sweeps of the docs; owner list below); P2 (Creative Common) built 2026-11-17; P2b to P7 planned. Code facts below were read
2026-11-16 (paths and lines drift; re-read before editing).

Short names: R = instance root; dict = `development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/`;
KJ = `kubejs/data/creativedelight/kubejs`. Rebuild map as in [plan_bugfix_1114.md](plan_bugfix_1114.md): A = instance
`kubejs/` or config only; B = Creative Delight; C = Creative Common; D = Creative Compat; G = Creative Brewing; W = Creative Worldgen;
E = Creative Equipment. One jar at a time, never while the game runs. Every functional item gets scratch queue rows
(queue only, none run) and a writeup History line; player-visible items get a bullet in `unreleased.md`.

## 0. Decisions

<!-- generated:status_index begin open_work sha1=45ddc89f6f81 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 23 open TODO rows (Planned 10, In Progress 10, Testing 3), 4 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `conventions #22` | Planned | none | Brewin' advancement scan |
| `conventions #26` | Planned | none | Delight loot through Creative Loot |
| `conventions #28` | Planned | none | Sideways edges and what the dependencies simplify |
| `conventions #29` | Planned | none | patcherMod to Creative Common in text |
| `conventions #30` | Planned | none | Underscores in own names, touches saves |
| `conventions #34` | Planned | none | Dated history out of code |
| `conventions #35` | Planned | none | Memory cleanup todo |
| `conventions #36` | Planned | none | History audit, low priority, marked |
| `conventions #37` | Planned | none | Docs and writeup form, last |
| `tooling/conventions #11` | Planned | S | Rules violation fixes (design_rules V-01 to V-62) |
| `conventions #15` | In Progress | SQ-LOOT-02 | Global loot modifiers |
| `conventions #17` | In Progress | none | Registration and namespace |
| `conventions #18` | In Progress | none | Dict keys for literals and the lints |
| `conventions #23` | In Progress | none | Member checks, remaps, helper copies |
| `conventions #24` | In Progress | none | Small fixes (added 2026-11-16) |
| `conventions #25` | In Progress | none | Villager trade helper |
| `conventions #27` | In Progress | SQ-AV-01, CQ-AV-01, CQ-AV-02 | No recipe unlocks |
| `conventions #31` | In Progress | none | ISO 8601 dates only |
| `conventions #32` | In Progress | CQ-TT-01 to -03 | American spelling and arrows |
| `conventions #33` | In Progress | none | Tab indent only and the rule |
| `conventions #14` | Testing | SQ-DRK-08 | Tilled end stone and sea pickles |
| `conventions #16` | Testing | SQ-LOOT-01 to -06 | Split Creative Loot out of Creative Worldgen |
| `conventions #20` | Testing | CQ-TT-04, CQ-TT-05 | Test-item palette switch |
<!-- generated:status_index end open_work -->

Owner 2026-11-16 unless marked. D7 to D19 come from the review of this plan on 2026-11-16, D20 to D26 from the owner's answers the same day.

1. **D1 License string**: `GPL-3.0-only` confirmed (can be widened later); change the rule text, not the jars (section 8).
2. **D2 Loot modifiers**: see D14.
3. **D3 Tab scope**: own source files only: Java, JS, Gradle, Python, own hand-written JSON. Excluded: YAML (tabs are
   illegal), Markdown, game-written configs, generated files (D3 test: section 16), MIT/ARR files baked from an upstream
   donor. Alignment spaces after the indent are allowed; spaces as indent are not.
4. **D4 Underscore scope**: own jars and members, including the backports' mod ids (`baby_mobs`, `more_mooblooms`:
   they are our jars, see D11). Upstream mods that we only patch keep their names (`oceanic_delight`; `jrftl:prepared_flesh` stays as
   registered, V-28 is a different rule).
5. **D5 Year-less dates**: every year-less date in the repo is 2026 (the repo starts 2026-09-27, today is 2026-11-16).
   A year-less date later than the file's last commit is impossible and goes to the owner list, never guessed. 2027
   starts in about 45 days; after 2026-12-31 a year-less date is ambiguous, so the ISO rule and its lint go in at P0
   (report mode) and the doc sweep finishes in P1, before 2027-01-01 (section 15).
6. **D6** Deployed changelog files stay frozen (DR-P9) for dates, names and tabs.
7. **D7 Code formatting (G2)**: tabs as indent in every D3 file, Python included (179 files, all spaces today); no trailing
   whitespace; `.editorconfig` added. Line length and brace style stay free.
8. **D8 Comments (G3)**: new rule DR-C26, code comments say what the code does now; history and status go to the writeup
   History (section 17). File headers stay as they are (no license header rule).
9. **D9 Citations (G5)**: one word, "owner", in the form "(owner, YYYY-MM-DD)"; "user" citations convert in the section 15 sweep.
10. **D10 Old value in changelog bullets (G12)**: brackets, `[2]`, as CLAUDE.md and every file since 1.1.03 do. DR-P4's
    "(was 2)" text is rewritten; V-60 converts the `unreleased.md` stragglers.
11. **D11 Upstream-id exceptions (G20)**: Baby Mobs and More Mooblooms are our backports, so their mod ids, packages, mixin
    jsons and config names are renamed (section 14). The named exceptions are only `jrftl:prepared_flesh` (saves) and
    Golden Food's `minecraft:` overrides.
12. **D12 Test line form (G13)**: checkbox lines `- [ ] **PREFIX-NN Name:** setup and action -> expected`, colon inside
    the bold, a result note after the colon, never between the name and the colon. Retired ids are struck through with
    "(moved to X)". The integration-report skill's `| ID | Test | Pass |` table becomes checkbox lines too (section 12b V-62c).
13. **D13 Loot home**: a new **Creative Loot** jar (mod id `creativeloot`) is split out of Creative Worldgen (owner
    2026-11-16, section 5b). It owns the loot rule engine, the loot scripts and the global loot modifier merge.
14. **D14 Global loot modifier list**: design for the worst case. Assume Forge reads only one jar's
    `data/forge/loot_modifiers/global_loot_modifiers.json` and drops the rest (section 5). No test decides it.
15. **D15 Ids may change for consistency**: recipe ids, LootJS log names and registration ids that move into their owner
    namespace may change. Registered items, blocks and entities keep save safety through `owner_namespace.json` swaps;
    recipe ids need no save carry, only their references updated (section 6).
16. **D16 Palette demo**: stays on in the owner's config for now; the switch exists with default off, and TODO
    `tooling/design-rules #4` turns it off before the next release that reaches players (section 9).
17. **D17 Member checks**: code that asks "is this member here" asks the member switch (`CDConfig.member`,
    `CBConfig.member`, `Compartments.on`, `global.CD.on`, `global.CB.on`), not the mod list. `ModList` and
    `Platform.isLoaded` stay for optional third-party mods; checks on mandatory mods (Farmer's Delight) are deleted (section 12, 12b V-48).
18. **D18 patcherMod text**: this plan changes comments, log text, paths and docs only. Java identifiers, mixin names and
    config keys that still say patcherMod are listed, not changed; TODO `tooling/design-rules #3` is the later rename audit (section 13).
19. **D19 Prefix renames**: the 10 renames in [testing/test_prefix_index.md](testing/test_prefix_index.md) section 2 are set
    (older doc keeps the prefix). The owner can override a name before the rename tool runs.
20. **D20 Spelling (G1)**: American spelling in all own prose and player text: docs, memory, skills, comments, tool output,
    `unreleased.md`, own lang files, tooltip and chat text (color, armor, behavior, gray, center, license, normalize, organize).
    Code identifiers follow their API (`colour` in an upstream class name stays). Upstream lang text from a repack or a
    donor is not overridden for spelling alone; deployed changelogs stay frozen (D6). Sweep in section 15b.
21. **D21 Arrows (G19)**: `->` in test lines and docs; `→` (895 uses) converts in the V-54 pass and the section 15b sweep.
    Player text keeps whatever the line already needs (no arrows are added to tooltips or lang).
22. **D22 Namespace binding**: confirmed (owner, 2026-11-16): every script binds the recipe helper to its folder's
    namespace with `global.NCH.forPack(ns)`, and registrations with `global.reg.forPack(ns)` (section 6).
23. **D23 Recipe unlocks (V-15, G16)**: removed entirely, pack-wide (section 12e). DR-C14 becomes "no recipe unlocks
    of any kind"; G16 closed.
24. **D24 Sideways edges (V-16)**: recorded in creative_split.md, and every simplification the existing mandatory
    dependencies allow is carried out (section 12f): Brewing and Delight both require Calendar, and Brewing requires
    Delight, so code in the depended-on jar that reaches into the depending jar moves to the depending jar.
25. **D25 Loot dependency**: Creative Delight and Creative Brewing declare Creative Loot as a mandatory dependency
    (`ordering="AFTER"`). Creative Loot requires only Common and Compat, so no cycle; Worldgen requires it too. This lets section 5 (GlmMerge) and
    V-38 (section 12d) call Creative Loot directly with no guards. Exposure stays independent (its fragment is read when
    Creative Loot is present; recorded in creative_split.md).
26. **D26 The other unscheduled violations**: V-05, V-14, V-32, V-33, V-38, V-41 take the recommended fix (owner
    2026-11-16): sections 12b, 12c, 12d.
27. **D27 Writeup split (G7)**: a writeup splits when its features are more useful to reach from a tree-style table of
    contents than side by side in one file: a parent doc keeps the overview and a linked contents table, each feature
    gets its own file (like `testing/structures/`). No line-count threshold. Not scheduled in this plan; the audit
    lists candidates (creative_delight.md, 2,124 lines) for the owner to judge by that test.

28. **D28 V-26**: all three files move (owner, 2026-11-16): dragon_breathing to Magic, creative_labels to Labels (with the
    build copy step), botany_pots to Compat (section 6).

Still open (owner):



## 1. Order of work

| Phase | Work (sections) | Rebuild | Why here |
|---|---|---|---|
| P0 Decisions and rule text | 0; rule text for DR-W3b, DR-L4b, DR-C19, DR-C21, DR-C26, DR-P4; prefix index (2, written); `.editorconfig`; date and prefix lints in report mode | none | Every later step writes text under these rules; the date lint must run before 2027 starts |
| P1 Docs and hygiene | 2 (prefix lint, rename run); 3; 10; 12b docs rows (V-60, V-61, V-62a to d); 15 doc pass (docs, memory, skills); 15b spelling and arrows, docs and player text | A | No game risk. Renamed prefixes are needed before any new test is written in P2 to P4. The date sweep must end before 2027-01-01 |
| P2 Creative Common | 4 (dict lines, dict lint); 6 (`forPack`, `reg`, log names, lint report); 7 (missing dict keys); 9; 12 (resolver, `col` roles, absolute food rows); 12b V-37 getters; 12c villager trade helper; 12e pack-wide unlock block | C, once | Every user jar calls these helpers; one Common rebuild, then users |
| P2b Creative Loot split | 5b: new jar, engine and loot scripts moved out of Worldgen, GlmMerge (section 5) written in it | Loot, W, once | Delight, Brewing and Worldgen depend on it; must exist before P3 |
| P3 Users of Common | Creative Loot first (installed in P2b), then Worldgen's 6, 7, 12, 12b rows. Then B, G, D, E, Exposure, Devtools, pack folder: 6, 7, 11, 12 (V-04 member checks, V-34 to V-36, V-43, V-44), 12b (V-02, V-03, V-05, V-09, V-10, V-14, V-20, V-32, V-39, V-40, V-41, V-42, V-46, V-48), 12c trade conversions, 12e own block entities, 12f (Calendar, Delight, Brewing edges), 5 (drop member list files), then 12d (loot routing, last: it touches loot in several jars and needs GlmMerge in place) | W, then Calendar, B, G, D, E, exposure, Devtools | Worldgen's merge must be installed before any member drops its list file, or those modifiers stop loading |
| P4 Renames that touch saves | 14 (with V-07); 12 V-06 Exposure remap | W, Calendar, Exposure | Both need an old-id save test; one test world covers both |
| P5 Code sweeps, one commit each, in this order | 17 (history out of code; extracted lines written ISO) -> 15 and 15b code pass (dates and spelling left in comments and strings) -> 13 (patcherMod text) -> 16 (tabs, last) | all, build only | 17 deletes most dated comments, so 15 touches code once; 13 before 16 keeps the tab commit pure whitespace; 16 last isolates blame and allows the byte-identical class check |
| P6 Docs form | 19b | A | Test ids and anchors are final only after P1 renames and P5 history moves |
| P7 Close | 18, 21 | A | Memory state is moved once the writeups are current |

Section 12b rows are done in the phase of the jar they touch (listed above), not as a separate phase.

## 2. Test prefix index (V-49)

- **Built 2026-11-16**: [testing/test_prefix_index.md](testing/test_prefix_index.md). Section 0 is the procedure for
  a new test or prefix; section 1 the id types (writeup test, `SQ-`/`CQ-` queue rows, structure series, `DR-`, `V-`,
  `G`, TODO rows, doc-local `D1`/`B1`/`P1`); section 2 the 10 renames; section 3 the 151 writeup prefixes with owner doc,
  queue series and seed docs; section 4 the 289 queue series with no writeup prefix of their own (40 with no owner yet).
- Pointers added the same day: CLAUDE.md "Done means", README index intro, DR-W3, skills `integration-report` and `scratch-test`.
- Findings beyond the audit's nine: the queue reuses colliding codes too (`SQ-EN` is Ends Delight and enchanting,
  `CQ-RP` the resource pack bench and Raspberry Pie, `SQ-IP` Item Physics and Immersive Portals, `SQ-CN` is Corn Delight
  while `CN-` is Cans). `PWG-`, `NP-`, `RA-` were README references, not collisions: their README cells become references.
- To build (P1):
  - `tools/test_prefix_lint.py` reads the index tables and fails when (a) a defining id's prefix has no row, (b) an
    id is defined (`**PREFIX-NN**`, `| PREFIX-NN |`) outside its owner doc, allowing the queue, run docs, briefs and plans
    as quoting docs, (c) two rows share a prefix, (d) a queue id is defined twice (a "(rerun)" suffix is not a second definition).
    Add it to `tools/design_rules_audit.py`; it is a docs check, not a `/cdevgen` generator, so it does not go in `generators.json`.
  - `tools/test_prefix_rename.py` (dry run by default) applies section 2 of the index: defining lines, references in
    writeups, plans, TODO, README, `briefs/`, the queue (by section heading, since one queue code covers two features),
    the `.scratch.md` mirrors and `tools/scratch_query/`. Run docs keep old ids with "(now X)" at the first quote.
  - Rule DR-W3b: "A new test prefix gets its index row first; the lint must pass."
- Verify: lint passes; `grep` finds no id with two defining lines; the scratch query tool still resolves queue rows.

## 3. Two documents in one file (V-52)

- `remainder_buffer.md:462` (old plan pasted after History): move the pasted plan to `plan_remainder_buffer_old.md`
  with a "superseded by remainder_buffer.md" line at the top; keep the writeup to History and below.
- `alcohol.md:1694` (Part B): move Part B to `alcohol_part_b.md`, own sections 0 to 6, linked from `alcohol.md` and the
  README row; renumber test ids only if they collide (section 2 lint decides).
- Add both to README; fix links in other docs found by `grep`. Docs-only, no build.

## 4. Tilled end stone and sea pickles (V-22, V-23, V-24)

**Tilled end stone** is already built: [plan_bugfix_1114.md](plan_bugfix_1114.md) section 3.2 (2026-11-14) deleted the H&H
block, the dict line and kept Ender Crop's. State now: `item_dict.js:12603` and `block_dict.js:225` hold only
`endercrop:tilled_end_stone`; the swap `hearthandharvest:tilled_end_stone -> endercrop:tilled_end_stone` is in
`hearth_removals.json`. Remaining propagation, to check and finish:
- `kubejs/server_scripts/tags/blocktags.js:374` uses `item.tilled_end_stone` as a block (use `b.`), `:476` uses the wildcard `*:tilled_end_stone`: use `b.tilled_end_stone`.
- `endersdelight/additions.js:67`, `hearthandharvest/tags.js:74`: confirm they use the block dict and the endercrop guard.
- Generated data: `kubejs/generation_scripts/physics/item_physics_families.json` carries `endercrop:tilled_end_stone` (fine);
  regenerate with `/cdevgen` if the hearth row exists anywhere else. `tools/blockrunner_untagged_blocks.json:528`,
  `hearth_and_harvest.md` J75 and `texture_sources/ledger.csv:977-978` per the 1114 plan.
- Mark V-22 fixed (date, session) once SQ-HH-1546 and 1546b have run; until then "Built, untested".

**Sea pickles**: `item_dict.js:2161` says `#forge:sea_pickles`, `:2210` says `#creativedelight:sea_pickles`. The later
assignment wins, so every consumer already reads `#creativedelight:sea_pickles` and `drink_tags.js:12` fills exactly that
tag. The forge tag line is dead. Unify on `#creativedelight:sea_pickles`:
- Delete `:2161`; keep `:2210` (move the comment to say whole or sliced).
- Check that no script or datapack reads `#forge:sea_pickles` literally (`grep` in `kubejs/`, `development/*/src`, `data/`);
  if one does, it moves to the key. Do not add the pickles to `forge:sea_pickles`: that tag is not ours.
- Dict change: C rebuild. Verify with the existing sea pickle tea rows (`sea_pickle_tea_*`, skewers).
- Same pass for V-24: `hops` / `hops_crop` keep one key; delete the `spring_lead` / `small_spring_lead` placeholders
  overwritten later (`item_dict.js:1191-1192` vs `:4759-4760`). The duplicate-key check is in section 7.

## 5. Global loot modifiers (V-08)

Facts: 9 files ship `data/forge/loot_modifiers/global_loot_modifiers.json`: Exposure (main and `expanded` flavors),
Creative Delight members hearth, veggies, rustic, ends, vintage, the Vinery pack inside Creative Delight
(`packs/mods/vinery/`), and Brewin' in Creative Brewing. Third-party jars in `mods/` ship their own copies too.
Creative Delight, Brewing and Exposure depend on Common, not on Worldgen or Creative Loot (until D25).

Design (D13, D14: assume only one copy of the list is read, and we cannot choose which):
1. **No member ships the Forge list file.** Each member moves its `entries` to a fragment
   `data/<member ns>/glm_entries.json` (`{"entries": ["ns:name", ...]}`). The modifier definitions stay where they are
   (`data/<ns>/loot_modifiers/<name>.json`); Forge resolves an entry `ns:name` to that path from any jar.
2. **Creative Loot owns the merge** (`GlmMerge.java`, D13). After Forge's `LootModifierManager` has built its list
   on each data reload, GlmMerge adds every entry from (a) every copy of the Forge list in the resource stack
   (`ResourceManager.getResourceStack`, so third-party copies that lost the race still load) and (b) every
   `glm_entries.json` fragment. It skips entries whose serializer is not registered and entries from a member whose switch
   is off (D17), drops duplicates by entry id, and logs one line: `[GlmMerge] N entries (lists: ..., fragments: ...)`.
   The result does not depend on which copy Forge read.
3. Creative Loot ships no list of its own (no modifiers today; if it gets one, it uses a fragment as well).
4. **Dependency** (D25): Creative Delight and Creative Brewing require Worldgen in `mods.toml`; Exposure's fragment is
   read when Worldgen is present (soft edge, recorded in [creative_split.md](creative_split.md)). Mixin target and reload hook: read Forge
   1.19.2 `LootModifierManager` once before writing (the merge must run after Forge's `apply`, on the same reload).
5. **Order**: Creative Loot with GlmMerge is built and installed first (P3); the member list files are removed after, in the
   same install window, so no modifier is lost in between.
6. **Lint**: no own jar ships `data/forge/loot_modifiers/global_loot_modifiers.json`; every fragment entry has a
   definition file. Added to `design_rules_audit.py`.
- Queue row: boot, grep the GlmMerge line; the count equals the sum of the fragments plus the third-party entries; spot
  loot probes for one modifier per member (hearth, veggies, rustic, ends, vintage, vinery, brewin, exposure) and one
  third-party one (Farmer's Delight).
- Changelog: none if every modifier loads before and after. If the probe shows modifiers that were missing until now
  (the first-only case was true), a "Fixed" bullet names them.
- The jar itself: section 5b.

## 5b. Split Creative Loot out of Creative Worldgen (D13)

Facts read 2026-11-16: Worldgen's loot lives in `kubejs/data/creativeworldgen/kubejs/loot/` (`00_lib.js` to `99_debug.js`:
tuning, dead maps, rules, leaf apples, drops, removals, frequency, redistribute, targeted, tag capture, `loot_config.json`,
`loot_index.json`, `loot_census.md`), the engine API `global.CWG.lootRules` in `cwg_core.js`, and an empty Java `loot/`
package. 12 script files across the pack use `lootRules` or `global.LOOT`. Loot is not worldgen: only chest loot rules
touch structures.
1. New project `development/creativeLoot` (settings.gradle include, `ce.forge-mod` plugin, `mods.toml` requiring Common
   and Compat, mod id `creativeloot`, GPL-3.0-only, NOTICE and sides rows, `build_deploy.py` required entry, README row,
   writeup `creative_loot.md`, test prefix `LOOT-` added to the prefix index first; check `LT-`/`LTP-` stay as they are).
2. Move the loot script tree to `kubejs/data/creativeloot/kubejs/` with its build copy step (as Magic's), the data files
   (`loot_config.json`, `loot_index.json`, census) to the matching generated-data path, and the Java `loot/` package.
3. API rename: `global.CWG.lootRules` becomes `global.LOOT.rules` (one global, `global.LOOT` already exists as the id
   check); a deprecated alias on `global.CWG.lootRules` is not kept (all 12 callers are ours and updated in the same change).
   Config section `[loot]` carries over with `ConfigRename.carrySections` from the Worldgen config. Generators in
   `tools/generators.json` that read or write the loot files get their paths updated.
   **Migrate with the split: `tools/loot_tuning_sim.js`** (TODO `tooling/generators-and-tests #9`). It still runs the
   pre-wave-5 paths `kubejs/server_scripts/loot/05_tuning.js`, `07_rules.js`, `95_tag_capture.js` (lines 73-76), gone
   since the move into Worldgen; it is repointed once, at the Creative Loot tree, in this step (and `09_drops.js` added),
   not first at Worldgen.
4. Worldgen drops the loot tree and requires Creative Loot (chest loot rules for structures stay data in Worldgen's own
   namespace, registered through `LOOT.rules`).
5. Order inside P2b: create the jar and move files with the game closed; Worldgen and Loot build and install together
   (neither runs alone); recipe, loot and tag dumps before and after must be identical.
6. Queue rows: boot log shows the loot engine banner once; `/dumpall` loot dump diff empty; one chest loot probe in a
   structure; the config values survived. Changelog: none (same behavior).

## 6. Registration and namespace (V-25, V-26, V-27, V-28, V-31 and "no jar registers wrongly")

How it works today: `NCH.withNamespace(ns, fn)` sets the namespace of recipe ids for the calls inside `fn` (default
`creativeengineering`). Scripts alias the helper per file: `var R = global.NCH` (91 files) or `const R = global.NCH` (43).
Recipe callbacks run at event time, long after the file loaded, so the helper cannot tell which script pack is calling;
the namespace has to be bound when the alias is made. Registered items and blocks take a literal `ns:id`
(58 `e.create('ns:id')`), LootJS `logName` strings are free text (`CE/corn_knife_corncob` in Creative Delight), and tags
take literal ids.

Design: **namespace follows the script's folder, bound once per file, with an explicit override.**
1. Common adds `global.NCH.forPack(ns)`: returns an object with every recipe and remove helper of NCH, each running
   inside `withNamespace(ns, ...)`, plus `logName(name)` returning `<PACK>/<name>` for LootJS. Each script's alias becomes
   `var R = global.NCH.forPack('creativedelight')`. The pack folder (`kubejs/server_scripts/`) keeps `global.NCH`
   (namespace `creativeengineering`, the pack's own). `R.withNamespace(other, fn)` stays as the recorded override.
2. Startup: Common adds `global.reg.forPack(ns)` with `item(e, id)`, `block`, `fluid`, `effect`. A bare id registers
   `<ns>:id`; a full id `other:id` is an override and must be in the allowlist. CE and CD use it (V-31), and the profession
   list in `misc_items.js` moves to a `profession_dict.js` beside the item dict.
3. The alias rewrite is done by `tools/literal_id_swap.py --aliases` (section 7), which reads the namespace from the folder
   (`kubejs/data/<ns>/kubejs/`, `kubejs_startup/<ns>/`, a member's `kubejs_startup/<member>/` maps to its bundle's namespace).
4. **Lint** (extends `tools/id_owner_lint.py`, in `design_rules_audit.py`): per script file, owner namespace from the
   folder; fail on (a) a registration id in another namespace not in `tools/namespace_allowlist.tsv` (id, reason),
   (b) `global.NCH` recipe calls or a `forPack` argument that differs from the folder, outside the pack folder,
   (c) a `logName` not made by `R.logName` or not starting with the owner's prefix, (d) in Java,
   `DeferredRegister.create(..., MODID)` with another mod's id. The allowlist starts with `jrftl:prepared_flesh` and
   Golden Food's `minecraft:` overrides (D11).
5. **Audit first**: run the lint in report mode over every jar and the pack folder; its list is the "no jar registers
   wrongly" deliverable. Fix each hit or allowlist it with a reason.

Per violation:
- **V-25** Compat recipes in `misc_items.js`, `flowers.js`, `feature_ports.js` (about 12) use `forPack('creativecompat')`;
  their ids change from `creativeengineering:` to `creativecompat:` (D15).
- **V-26** (D28): `magic/dragon_breathing.js` moves to `kubejs/data/creativemagic/kubejs/` (Magic's pack);
  `other/creative_labels.js` to a new `kubejs/data/creativelabels/kubejs/` (check the Labels jar copies server scripts
  like the others; if not, add the copy to its build); `other/botany_pots.js` to Creative Compat's pack (third-party tweaks
  are Compat's). Ids change to the new namespace.
  V-26 per file (facts read 2026-11-16):
  - `magic/dragon_breathing.js` (25 lines): 4 or 5 `R.dragonBreathing` rows using the dict. The helper lives in Common's
    NCH. Move: clean, no dependencies. Becomes `kubejs/data/creativemagic/kubejs/` (the Magic jar already copies that tree);
    recipe ids change to `creativemagic:`.
  - `other/creative_labels.js` (40 lines): 3 recipes plus the four `creativeLabelsSpecial` ones, all making Labels items.
    Move is right but needs build work: `development/creativeLabels/build.gradle` has no script-tree copy (Magic's does),
    so add the same copy step, the `kubejs/data/creativelabels/kubejs/` folder, the jar-has-scripts check, and a
    `mods.toml` dependency on Common (and KubeJS). Ids change to `creativelabels:`.
  - `other/botany_pots.js` (66 lines, `// requires: botanypots`): not a recipe set but a growth pass over every loaded pot
    crop recipe, plus `global.BOTANY_*` constants that `client_scripts/tooltips.js` reads. Move to Compat's pack (third-party
    tweak). The globals stay global, so the tooltip still works, but Compat's server pack must load before the client
    script reads them (client reads at tooltip time, so safe). Recipe ids unaffected (it edits, not adds) except the
    soil recipe it adds, which becomes `creativecompat:`.
- **V-27**: LootJS names `CE/...` in Creative Delight and Worldgen scripts become `CD/...`, `CWG/...` through `R.logName`.
- **V-28**: allowlisted (D11).
- **V-31**: `reg.forPack` for CE and CD registrations.
- **Recipe id changes** (D15): before the move, `grep` every moved id in `NCH.remove`/`e.remove({id:...})` calls, JEI
  hide lists, `tools/` generators, quest files (`config/ftbquests/`), the recipe dump baselines and the docs; update each
  in the same change. The `/dumpall` recipe count must stay equal; the id diff must equal the move list exactly.
- Changelog: none (no player-visible change; recipe-book "new recipe" marks may reappear once: written in the writeup).
- Queue rows: one per jar, recipe count before and after `/dumpall`, and the id diff against the move list.

## 7. Dict keys for literals (V-29, V-30, V-31, V-24) and the lints

- Swap list: `namechanges.js` (79 of 83), `transport.js:117,122`, `blocktags.js:61,278,366`, effect ids (V-30: `jeed_providers.js`
  52, `end_gear.js`, `gear_effects.js` 23, `potions.js` 12), `gen_can_types.py` emits `global.effect` keys.
- Tool `tools/literal_id_swap.py` (dry-run by default): reads the dicts, finds quoted `ns:id` literals in scripts whose id
  has a dict key, rewrites to the key using the name the file already binds for that dict (`item.`, `i.`, `b.`,
  `global.item.` when the file binds none). It skips ids with no key, strings that are not ids (tooltip text, comments,
  lang), `e.create(...)` registrations (section 6) and `tools/` Python. After the run: `/reload` plus `/dumpall` and a
  recipe/tag count diff must be zero.
- Where an id has no key and is used 2+ times, the swap tool lists it and adds the key to the dict (C rebuild). Ids used
  once may stay literal (one-off) when they are a foreign-mod string in a guarded block; the lint allows that with
  `// requires: <mod>`.
- Dict lint: duplicate key assignments in a dict file fail (catches V-22, V-23, V-24 for good); add to `design_rules_audit.py`.

## 8. License string (V-12, V-13)

- **GNU GPL3 (free text) vs `GPL-3.0-only` (SPDX)**: both name the GPL version 3 licence. `GPL-3.0-only` is the SPDX
  identifier, unambiguous and machine-readable: GPL 3 only, not "or any later version" (`GPL-3.0-or-later`).
  "GNU GPL3" is only a label with no "only/or later" statement. All 44 jars already use the SPDX form and ship the full
  GPL-3 text, so the license content is identical; only the rule text is wrong.
- Fix: DR-C19 and CLAUDE.md "Own work is GPLv3" say "GPL-3.0-only (SPDX)"; mods.toml `license=` takes the same string;
  `design_rules_audit.py` checks it. Widening to or-later later is a one-line change in each `gradle.properties` (owner 2026-11-16: only for now).
- V-13: NOTICE naming: one form `NOTICE-<scope>-<licence>.txt` (for example `NOTICE-upstream-MIT.txt`); rename the 7 odd names; Power Grid's
  `LICENSE` (Apache 2.0, upstream) becomes `NOTICE-upstream-Apache.txt`. The build gate in `ce.forge-mod.gradle` reads
  the new names; update it in the same change.

## 9. Test-item palette switch (V-45)

- Config key `[tooltips] paletteDemo` in `CommonConfig` (C rebuild), default `false`, read by
  `kubejs/client_scripts/tooltips.js:14-34` through `NCH.cfg`; the script falls back to false when the key is missing.
- The owner's live `config/creativecommon-common.toml` sets `paletteDemo = true` for now (D16); copy it to
  `configs_edited/root_config/creativecommon-common.toml`. `build_deploy.py` ships `config/` as it is, so a release built now
  would ship the demo on: TODO `tooling/design-rules #4` sets it to false before the next release that reaches players
  (and the release skill's pre-flight reads open TODO rows marked "Blocks the next release").
- Changelog: none while the shipped value is on. When `#4` turns it off, that change gets the bullet "White Wool and White
  Concrete no longer show the color palette test tooltips". Writeup: tooltips.md. Queue row: key false hides, true shows.

## 10. Mod sides (V-17)

- Add rows in `mod_sides.md` for `debugify-1.1.0-forge-1.19.2.jar`, `creativeLabels-1.0.1.jar` and
  `backportedPowerGrid-0.6.2-ce-19.2-18.jar`. The audit named `-17`; the file in `mods/` is now `-18`.
  Sides come from each jar's `mods.toml` (`displayTest` / `side` fields) and the owner's pattern of both-sides unless
  the jar is client-only; read before writing. Debugify is both-sides (server fixes plus client fixes).
- Power Grid `-18` stays in `mods/` (owner, 2026-11-16): it is loaded for development testing, to exercise the port in an
  unstable environment, and is not distributed until finished. The sides row says so. `build_deploy.py:153-155` already
  marks it not required; its comment ("not in mods/ during the port") is stale: rewrite it to "in mods/ for development
  testing, never staged until energy_advanced.md stage 5", and check the staging code skips a non-required jar found in
  `mods/` (fix it if it does not). The `power-grid-port` memory rule (gate jars only from `jar_archive/pwg_gate/`) is
  updated in the memory cleanup (section 18).
- Add a check to `design_rules_audit.py`: every jar in `mods/` and `client_mods/` has a sides row.

## 11. Brewin' advancement scan (V-01)

- Delete `onBlockPlaced`, `onItemUsedOnBlock` and `checkTemperatureAdvancement` in
  `members/brewin/.../BnCCommonEvents.java:200-243` and the registrations/imports that only they used.
- Check nothing else calls the temperature helper; the keg temperature logic itself stays.
- G rebuild (`:creativeBrewing:`). No player-visible change (advancements are dropped by the pack): no changelog bullet.
- Queue row: a keg near heat and cold still works, no per-place scan cost (tick profile unchanged).
- V-02 and V-03 (the same rule) are in section 12b.

## 12. Member checks, remaps, helper copies (V-04, V-06, V-34, V-35, V-36, V-43, V-44)

- **V-04** (D17): every `ModList.get().isLoaded` in Creative Delight and Brewing code is sorted into three kinds:
  - a nested member (for example `salt`, `displaydelight`): replace with `CDConfig.member(id)` / `CBConfig.member(id)`
    (true when the member is present and its `[members]` switch is on), or `Compartments.on(id)` for a compartment;
  - a mandatory mod (Farmer's Delight): delete the check and the dead branch;
  - an optional third-party mod (`solapplepie`, `solcarrot`): keep `ModList`.
  Known sites: `SaltBlockTicks.java:64`, `SeasoningJEI.java:107`, `DisplayDelightDishes.java:48,70` (the JSON `requires`
  field: member check when the id is a member, else `ModList`), `FeastPlatingJEI.java:36`, `PlacedFoodSol.java:112-123`.
  Grep the whole of both bundles and their members for the rest. B, G rebuild. Queue row: switch one member off
  (`salt`), boot, its JEI category and block ticks are gone, no error.
- **V-06** (P4): move the ids of the hand `MissingMappingsEvent` in `ExposureExtras.java:65-79` into the jar's
  `owner_namespace.json` `replaces`; delete the handler and its registration. Test on a fresh old-id save (memory
  `forge-missing-mappings-staging`), in the same old-id world as section 14. Rebuild the Exposure jar.
- **V-34**: the two hand item merges (`create/recipes.js:44-45`, `engineering/transport.js:116-125`) go through
  `global.addItemMerges`.
- **V-35**: one `global.effect.resolve(idOrKey)` in Common; delete the 7 private resolvers
  (`alcohol_drinks.js:51`, `potion_area.js:60`, `comfort_nourishment.js:32-33`, `effect_swaps.js:35,67`, `veggies_foods.js:8`,
  `vintage_items.js:27`) and the copy beside `global.armorEffects._resolveEffect`, which now calls it.
- **V-36**: absolute-value rows in `food_value_copies.js` (the applier) for `alcohol_drinks.js:47`, `fruits_foods.js:12`,
  `vintage_items.js:23`, `delightful_foods.js:26`; delete the `FoodProperties$Builder` rebuilds. The row format gets one new
  field (absolute) documented in food_tuning writeup. Verify: `/dumpall` food dump diff is zero.
- **V-43, V-44**: add `global.tooltips.col` roles for every color in use (`col.data` for the Material Information lines that
  are `dark_gray` by look; any `lpurple` use gets a named role). Replace the 50 literal-color lines in 21 files and the raw
  section-sign codes in `armor_effects.js:141-144`, `tool_effects.js:72-77`. Add a `col.*` lint (raw `§`, bare `Text.color`,
  `'§'` literals) to the audit. Tooltip appearance must not change: client check by screenshot of 6 tooltips before and after.
- Sequence: Common first (resolver, `col` roles, absolute-row support), then each user jar. Each is its own
  `design_rules.md` status row.

## 12b. Small fixes (added 2026-11-16)

Owner brief 2026-11-16: all of group A, V-37, V-39 and V-42 from group B, all of group D (V-20, V-46), all of group F (V-62).
Group E is section 19b (last). Each is one change, done in the phase of the jar it touches (section 1).

| Id | Fix | Rebuild |
|---|---|---|
| V-02 | Delete the 3 dead advancement modifier JSONs in the mooblooms pack and their pack switches | W |
| V-03 | Remove the upstream advancement JSONs in Brewin' (49), Exposure (35) and Vintage (11). Where a member is a repack or bakes from an upstream donor (Vintage; Brewin' if its files are baked), deleting the files does not hold: add `data/*/advancements/**` to that member's repack exclude list or the bake step's drop list, then clean-build (memory `repack-stale-resources`). In-tree files (Exposure) are deleted | G, B, exposure |
| V-05 | `PackSeasonings.java` (Barbeques member): compile against Creative Delight directly (it is inside the CD jar) and delete the reflection and the "without Creative Delight" fallback (`:47-61`, `:150-160`) | B |
| V-09 | Delete `assets/creativeengineering/lang/.en_us.json` | CE |
| V-14 | Repackage the Miner's member from `com.sammy.minersdelight` to `com.SaintAlfonzo.creativedelight.miners` (34 files, its mixin json, `mods.toml` entry class, any reflection strings); registry ids unchanged, so no save risk. Record in miners_delight.md | B |
| V-32 | Move the 6 builder `.tagBlock(...)` calls (`startup_scripts/items/decor_blocks.js:18-19`, `creativeengineering/items/coil_blocks.js`, `creativecompat/decor_blocks.js`) into the owning tag scripts through `global.tags`; `/dumpall` tag diff empty | A, CE, D |
| V-41 | Move the tag fills a jar's Java reads out of the pack folder: `creativecompat:` tags in `tags/blocktags.js:598-606` to Compat's script pack, the `creativeequipment:` tag in `tags/feature_port_tags.js:9` to Equipment's, the `creativecommon:` WTHIT tags in `tags/wthittags.js:13-14` to Common's; `/dumpall` tag diff empty | D, E, C |
| V-10 | Log tags, logger names and the `creativeengineering_locate` ticket name become the owning jar's name (`DisplayDelightDishes.java:62,71`, `DisplayDelightDishBlocks.java:57`, `ItemBurnTransform.java:42`, `StructureCatalogue.java:73`, `MassPlacer.java:95`, `LocateLoads.java:51`); check no log grep or tool matches the old text | B, D, Devtools, W |
| V-40 | One `buildGrid` in Devtools (`metal_test_recipes.js`, `wood_test_recipes.js`): a shared copy in Devtools' `_lib/` | A |
| V-48 | The about 19 `Platform.isLoaded` calls on members use the member switch (D17): `global.CD.on(...)` for Creative Delight members, `global.CB.on(...)` for Creative Brewing members (`vinery`, `brewinandchewin`); third-party mods keep `Platform.isLoaded` | B, G |
| V-60 | Fix the `unreleased.md` breaks (lowercase names, one-bullet families, missing number, benefit clause, reassurance, present-tense Fixed, internals, old-value forms to brackets (D10), "JEI"), then `python3 tools/changelog_lint.py` | A |
| V-61 | Mod display names in the pack form in `unreleased.md` and the writeups ("Brewin' and Chewin'" in 42 places); deployed files frozen | A |
| V-37 | Config reads through a getter on `global.<JAR>` or `NCH.cfg`, replacing direct `Java.loadClass('...Config')` in 9 files; add the getters in Common first | C then users |
| V-39 | `placeable_tooltip.js:30-44` uses `global.tooltips.removeTooltipKey`; `tune_armor.js:57-60` and `tune_tools.js:59-62` share one tuner function in `creativeequipment/func/` | B, E |
| V-42 | `dump_food.js:220`, `dump_recipes.js:39,97` read the serializer key, as `dump_crafting_audit.js` does; `/dumpall` output diff must be empty | A |
| V-20 | Replace the two `forge:false` overrides (`data/tmted/recipes/crusher/wheat_flour.json`, `wheat_flour_from_bale.json`) with `NCH.remove` in the owning script; if the removal cannot be written, document the override in `pack_hygiene.md` | B |
| V-46 | Item names out of the pack folder (`namechanges.js:10-11,64`) into the owning registration or lang; give the transmitter (`misc_items.js:22`) a display name; lang under the owning jar's namespace | CE |
| V-62a | Move `briefs/random_tick_audit.md` into `feature_writeup/random_tick_audit.md` (an investigation writeup) with a README row | A |
| V-62b | Run docs: rename the two `testing/scratch_queue_run_2026-10-22b.md` and `scratch_queue_run_2026-10-28.md` to `scratch_run_2026-10-22b.md` and `scratch_run_2026-10-28.md` (17 files and the scratch-test skill already use `scratch_run_<date>.md`; both new names are free); fix links (`grep -rl scratch_queue_run_`) | A |
| V-62c | integration-report layout (see below) | A |
| V-62d | Refresh the 15 stale `configs_edited` backups from the live files; real drift: `creativecompat-common.toml`, `jei/*sort-order.ini`, `waila/plugin_toggle.json5`; back up `config/waila/waila.json5`; delete the orphan `configs_edited/root_config/salt-common.toml` after checking no live `salt-common.toml` exists. Do the config drift first; no game needed | A |

**V-62c in detail.** The skill (`.claude/skills/integration-report/SKILL.md` section 1) prescribes: title, opening
paragraph, 0 At a glance, 1 Overview and Facts, 2 Overlap, 3 New content, 4 Fold-in, 5 Decisions, 6 File map,
7 Offline checks, 8 In-game tests (a `| ID | Test | Pass |` table), 9 History. Its three "models" each differ:
`hostile_delight.md` has all ten sections but puts the full inventory (Part 1) before Overlap (Part 2);
`unusual_delight.md` has At a glance, Overlap, New content, Decisions, Tests but no Overview, File map, Offline checks or
History; `more_delight.md` is a different kind of report (a newer jar against an installed one: How it was read, Changes,
Open questions, Tests, Built). Fix (the obvious one, so the owner does not need to pick):
- The skill's layout stays the rule, with two named variants (G18): **full report** (the ten sections) and **jar
  upgrade** (more_delight's five sections). Section 8 becomes checkbox lines (D12) instead of the table.
- The three files are historical reports; they are not rewritten. The skill stops calling them layout models and cites
  them per section as content examples ("Overlap table: unusual_delight.md section 3"); the next report written to the
  layout becomes the full-report model. `hostile_delight.md` and `unusual_delight.md` get a History section (DR-W5)
  and their test tables convert in the V-54 pass.

Queue rows: V-03 and V-20 (recipes and loot unchanged: `/dumpall` diff), V-48 (member switch off removes the feature), V-46 (names show).
Changelog: V-46 only if a shown name changes; V-60/V-61 are changelog edits themselves.

## 12c. Villager trade helper (V-33)

- Common: `trade_helper.js` gets `global.trades.villager(e, profession, level, { buy, buy2, sell, uses, xp, priceMultiplier })`,
  the villager counterpart of the Wandering Trader helper, with dict keys accepted for every item and the same id checks.
- Convert the 52 raw `e.addTrade` calls in `kubejs/server_scripts/economy/trades.js` and the 13 in the Creative Delight
  `trades.js` files (`salt/`, `barbequesdelight/`, `veggiesdelight/`, `hearthandharvest/`, `corn_delight/`). Lint: raw
  `e.addTrade` outside the helper fails.
- Verify: `/dumptrades` before and after, diff empty. C, then A and B.

## 12d. Delight loot through Creative Loot (V-38)

- Route Creative Delight's own loot rewrites (`rusticdelight/crop_drops.js:80`, `creativedelight/soil_yield.js:134`, and the
  raw `LootJS.modifiers` blocks in 5 CD loot scripts) through Creative Loot's engine (`global.LOOT.rules` for table rules and the id checks). D25 makes the call safe without guards.
- If a rule shape is missing from the engine (block drops by tool or by soil, for example), add it to Creative Loot's engine
  first; the CD script only declares rules. Log names come from `R.logName` (section 6).
- Verify: loot probes for each moved rule (same drops before and after); `/dumpall` loot dump diff empty. W, then B.
- Afterwards every own loot rule is in Creative Loot's one engine.

## 12e. No recipe unlocks (V-15, D23)

- Pack-wide: a Common mixin makes `ServerPlayer.awardRecipes(Collection)` and `awardRecipesByKey(ResourceLocation[])`
  do nothing. This covers vanilla furnaces, smokers, blast furnaces, the crafting table, Farmer's Delight, the knowledge
  book and every mod that calls them. `/recipe give` stops working too (no use in this pack: JEI is the recipe viewer).
  Experience from cooking is not touched: furnaces pop XP before they award recipes, and the award call becomes a no-op.
- Own code: delete the award calls and the used-recipe bookkeeping kept only for them, but keep the XP pop:
  `OvenBlockEntity.java:365-368` and `OvenResultSlot.java:53` (Creative Delight), the Keg (`KegBlockEntity.java:963-979`,
  `KegBlock.java:79`, Brewin' member), the Copper Pot (`CopperPotBlockEntity.java:535-549`, `CopperPotResultSlot.java:51`,
  Miner's member). Central Kitchen injects at `setRecipeUsed` on the Copper Pot (comment at `:61`): keep the method,
  drop only the award.
- Rule DR-C14 text: "No recipe unlocks of any kind (recipe book, advancements, knowledge books)". design_rules.md G16 closed.
- Player-visible: the recipe book no longer gains entries and no "new recipe" toasts show. Changelog bullet: "Recipes
  are no longer unlocked in the recipe book by crafting or cooking". Entries already in a save stay.
- Queue rows: smelt iron ore in a furnace, cook in the Oven, the Keg and the Copper Pot: XP drops as before, the recipe
  book gains nothing, no toast. C, B, G rebuild.

## 12f. Sideways edges and what the dependencies simplify (V-16, D24)

Brewing and Delight both require Calendar (`mandatory=true`), and Brewing requires Delight. Code in a depended-on jar that
reaches into a depending jar moves to the depending jar, which can call the other directly:
1. **Vinery grass slab season** (`creativeCalendar/.../VineryGrassSlabSeasonMixin.java`, a `@Pseudo` mixin gated by
   `CalendarMixinPlugin`): the Vinery port is our source, so `SpreadableGrassSlabBlock.randomTick` in
   `members/vinery` calls `GrassSeasonHook.refuse(...)` directly. Delete the mixin, its plugin entry and its mixin json line.
2. **Vintage lush grass season** (`VintageLushGrassSeasonMixin.java`): the Vintage member's source is in-tree
   (`members/vintage/src/main/java/net/ribs/vintagedelight`); its lush grass block's random tick calls the hook directly.
   Same deletions in Calendar.
3. **Vintage clock reflection** (`creativeCalendar/.../time/TimeDate.java:211-228`, `Class.forName("net.satisfy.vinery.core.wine.VintageClock")`):
   Calendar gets a small hook, `WineTime.onDateMoved(Consumer)` style, that the Vinery port's `PackTime` registers at setup;
   `/ccaltime date` calls the registered listeners. No Vinery class name left in Calendar.
4. **Coaster offset** (`creativeDelight/.../mixin/brewinandchewin/CoasterRendererOffsetMixin.java`, needing
   `compileOnly project(':backportedBrewin')` in `creativeDelight/build.gradle:52`): Brewing requires Delight, so the change
   goes into the Brewin' member's coaster renderer source directly; delete the mixin and the compileOnly line.
5. After the moves, `grep` both bundles and Calendar for the other jar's packages and mod ids (`brewinandchewin` in
   `FeastPlatingCategory.java`, for example) and apply the same rule to each hit; a hit that is a plain optional mod check
   stays.
- Record the edges that remain (Exposure's loot fragment, D25; anything step 5 keeps) in [creative_split.md](creative_split.md).
- Verify: the queue rows of the crop-season tests for the two grass blocks (SQ-PST-08 to -11b family) rerun; `/ccaltime date`
  backward moves the vintage clock; the coaster renders offset. Calendar, B, G rebuild, Calendar first.
- No changelog bullet (same behavior).

## 13. patcherMod to Creative Common in text (V-18, V-47)

- Tool `tools/rename_patchermod.py` (dry-run): replaces `patcherMod` with "Creative Common" in comments, log text and
  docs, and with `creativeCommon` in file paths (`plantable_tooltip.js:1`, `cdv_server.js:8`), across the Java files, the
  8 `mods.toml` comment blocks and the scripts. Rewrites "becomes creativecommon at the Q1 swap" as plain present tense.
- **Not changed (D18)**: Java identifiers (class, field, method, mixin and config names), JSON keys and strings read by
  code. The tool lists every such hit (file, line, kind) into the report for TODO `tooling/design-rules #3`, the later
  rename audit.
- Skip list (stays as written): `CLAUDE.md` glossary and namespace swap history, `namespace_move.md`, memory `patchermod-*`
  files and their index (renamed in the memory cleanup, section 18), frozen deployed changelogs, `jar_archive/`, quoted old
  log lines in run docs.
- Verify: `grep -rn patcherMod` leaves only the skip list and the identifier list; build all; one boot to check the log tag text.

## 14. Underscores in own names (V-11), touches saves

Scope (D4, D11): packages `com.SaintAlfonzo.baby_mobs`, `com.SaintAlfonzo.more_mooblooms`; their mixin jsons; default
configs `baby_mobs-common.toml`, `more_mooblooms-common.toml`; lang namespace `vinery_backport`; mod ids `baby_mobs` and
`more_mooblooms`.
- New names: `babymobs`, `moremooblooms`, `vinerybackport` (memory `creative-mod-naming`: no underscore in own ids).
- Save safety (memory `ids-follow-owning-mod`, `forge-missing-mappings-staging`): entity types, items, blocks and spawn eggs
  under the old namespace must load. Add `owner_namespace.json` swap entries and the `ConfigRename.carrySections` copy of
  the old config file; lang keys move with the namespace (memory `forge-shared-asset-path`: lang goes under
  `assets/<modid>/`). Also update every reference to the old ids: item dict keys' values, tags, loot tables, biome
  modifiers, spawn rules, KubeJS scripts, `mod_sides.md`, `build_deploy.py`, the Worldgen member list and the docs.
- Tests on an old-id save (shared with V-06): spawn eggs, existing mooblooms and baby mobs, configs carried.
- V-07 joins here: Calendar's own config migrator swaps to `ConfigRename.carrySections`.
- Changelog: only if a player-visible name or value changes (config file renamed: Common's swap carries values, no bullet;
  state this in the hand-over).

## 15. ISO 8601 dates only (V-59 and others)

- New rule DR-L4b: dates are `YYYY-MM-DD` everywhere (docs, comments, scripts, tool output, memory; commit messages
  excluded). Time stamps `YYYY-MM-DD HH:MM` (24-hour). No `11-14`, no `Nov 14`, no `14/11`. Add to design_rules.md,
  CLAUDE.md "Preferences" (one line), memory.
- Tool `tools/iso_dates.py` (dry-run, writes a report of every change and every unresolved one): handles `MM-DD`,
  `Month D`, `D Month`, `M/D`, `YYYY.MM.DD` and the `(user, date)` citation form (becomes "(owner, YYYY-MM-DD)", D9).
  Year from D5 (2026; later than the file's last commit = owner list). Skips: version strings (`25.12.09` upstream),
  deployed changelog files, `exported/`, `logs/`, `jar_archive/`, code that is date logic (calendar mod), commit hashes,
  quoted upstream text.
- Lint in `design_rules_audit.py` (report mode at P0, failing from P1 end): year-less `MM-DD`, month-name dates, citation form.
- **Deadline 2026-12-31** for the doc pass (P1: docs, memory, skills, TODO, queue). The code pass runs in P5 after
  section 17, which removes most dated code comments and writes the extracted lines in ISO itself.

## 15b. American spelling and arrows (D20, D21)

- Rule: DR-L7 (new; DR-L6 is the in-game names rule) "American spelling in prose and player text; `->` for arrows". design_rules.md G1 and G19 closed.
- Tool `tools/us_spelling.py` (dry-run, report per file): a word list of British to American forms (`colour`, `armour`,
  `behaviour`, `grey`, `centre`, `metre`, `litre`, `licence` as a noun, `favour`, `flavour`, `honour`, -ise/-isation verbs, `catalogue`,
  `programme`, `travelled`) applied to prose only. Skips code identifiers, quoted ids (`ns:id`, config keys, file paths),
  upstream text (repack and donor files, quoted upstream lines), deployed changelogs and `exported/`. Lang files: only
  own namespaces' `en_us.json` values, never keys. Also rewrites `→` to `->` outside player text.
- Order: docs, memory, skills and own lang in P1 (with the date pass, one commit each); code comments and strings in P5
  after section 17. Lint in `design_rules_audit.py` (the word list) from P1 end.
- Player-visible: own lang and tooltip text change (for example `colour` 12 times, `behaviour`, `grey` in own lang
  today). Changelog bullet under a catch-all family when it ships: "Item names and tooltips use American spelling
  (color, armor, gray)". Queue row: none needed (text only); a client look at 5 changed tooltips.

## 16. Tab indent only (V-21) and the rule

- Rule DR-C21 rewritten (D7): indent is one tab per level in every own source file of D3 scope; no spaces as indent; no
  trailing whitespace (DR-C22); mid-line alignment spaces allowed. Remove the per-language indent table in design_rules.md
  and gap G2.
- Tool `tools/tab_indent.py` (dry-run, per language): converts leading space runs to tabs using the file's detected unit
  (4 for Java and Python, 2 or 4 for JS), keeps continuation alignment as tabs plus residual spaces, never touches string
  literals, text blocks, block comments' ` * ` columns (aligned with one space after the tab) or lines inside multi-line
  strings. Also strips trailing whitespace outside string literals. Idempotent; checks parse equality (`javac` class diff
  for Java, Rhino parse for JS, `python3 -m py_compile` plus `ast.dump` equality for Python); a file that fails is
  skipped and listed.
- **Generated files** (D3): a file is generated when it is under `kubejs/generation_scripts/`, `kubejs/exported/`, a
  `build/` folder, or is an output path listed in `tools/generators.json`. The tool and the lint skip these; the
  generators keep their own JSON style.
- Lint: `tools/design_rules_audit.py` fails on a leading space or a space/tab mix in scope files. `.editorconfig` (added
  at P0) has `indent_style = tab` with `[*.{yml,yaml,md}]` exempt.
- Run last, one commit per language, so blame noise is isolated and a revert is easy. All jars rebuild once; compare each
  jar's class files before and after (byte-identical for Java: indentation does not change bytecode or line numbers).

## 17. Dated history out of code (V-19, G3)

- Rule DR-C26 (D8): code comments describe what the code does now, never when or why it changed; dated history lives in
  the writeup History (DR-W5). Status words ("will become", "planned for") are also out.
- Tool `tools/history_extract.py` (dry-run): lists every comment line containing a date or a "was/used to/changed/moved"
  phrase (261 dated lines in 120 Java files, plus JS). Maps each file to its writeup through a new
  `tools/writeup_owner.json` (source folder prefix -> writeup), built once from the README index and the jar folders;
  `design_rules_audit.py` warns on a source folder with no entry. The report is one table per writeup: file, line, text,
  proposed History line. The extracted lines are appended to the writeup History as `- YYYY-MM-DD: <text> (file)`, in
  ISO (D5); the comment lines are deleted; a comment that carries a still-needed rule (not history) is rewritten in
  present tense instead, and the report marks those for owner review.
- This also seeds V-50/V-51 (section 19) with real dated lines.
- Lint: `design_rules_audit.py` fails on a date in a code comment.

## 18. Memory cleanup todo (V-55)

TODO row: `tooling/design-rules #1`. Work: 27 memory files hold feature state (for example `backported-brewin.md`,
`plan-1-0-8.md`, `plan-7-1-execution.md`, `campfire-rain.md`): move the state into the writeups (the README says status
lives in the repo, memory holds rules, gotchas, mechanics), cut each file to the mechanic or delete it, fix the
index line, and rename `patchermod-*` memory ids per section 13. Memory dates go to ISO in the section 15 doc pass.
Update `power-grid-port` (Power Grid now in `mods/` for development testing, section 10). Low priority, docs/memory only;
P7, so the state moved is current.

## 19. History audit (V-50, V-51), low priority, marked

TODO row: `tooling/design-rules #2` "History audit". Scope: 5 writeups with History far behind (`loot.md`,
`engineering.md`, `creative_engineering.md`, `creative_delight.md`, `item_physics.md`) and 47 with none. Not scheduled in
this plan. Marker: design_rules.md V-50 and V-51 status becomes "Deferred (low), tracked as TODO tooling/design-rules #2";
`tools/design_rules_audit.py` keeps counting writeups without History so the number is visible. The section 17 extraction fills part
of it for free. Add History to the integration-report and jar-upgrade layouts when the audit runs.

## 19b. Docs and writeup form (V-53, V-54, V-56, V-57, V-58), last

Runs in P6, after the P1 prefix renames and the P5 sweeps, because renumbering and normalizing move test-id anchors.
Docs only, no build. Uses D12 (test line form, G13), D21 (arrows, G19) and G7's banner cap (below); G7's writeup size
split (O3) is not needed here.

| Id | Fix |
|---|---|
| V-53 | Renumber or fill sections in `curios_slots.md`, `texture_reuse.md:66`, `immersive_portals.md`, `ends_delight.md`, `ocean_delight.md`, `creative_engineering.md` (`## 4.1`); keep a "(was N)" note where a test line quotes a section |
| V-54 | `tools/test_line_fix.py` (dry-run): normalizes about 290 test lines to the D12 form (checkbox, bold name, colon inside the bold, result notes after the colon, retired-id form, arrows per D21) and converts the integration reports' `| ID | Test | Pass |` tables; ambiguous lines are listed, not changed. Lint added to the audit |
| V-56 | README rows for the 13 unindexed docs (or a plans table); cap status cells at status plus one line, moving the log text to the writeup History (`README.md:63` is 3,478 characters) |
| V-57 | TODO: map status words to the six words (`Built` x24, `Done` x9, `Tested` x2, `Open` x3, `In progress` x2, `Idea`), move the sentences at `:1539-1554` to Details, re-sort the 59 rows out of order; lint added |
| V-58 | Queue (DR-W4): add the World column to the rule text (it is common already) and normalize the 78 other-header tables; banner cap (G7 second half): the queue's top keeps the 5 newest dated banner lines, older ones move to the run doc of their date |

## 20. Coverage of the 62 violations

| Handled in this plan | Ids |
|---|---|
| Sections 2 to 4 | V-49, V-52, V-22, V-23, V-24 |
| Sections 5 to 8 | V-08, V-25, V-26, V-27, V-28 (allowlist), V-29, V-30, V-31, V-12, V-13 |
| Sections 9 to 12 | V-45, V-17, V-01, V-04, V-06, V-34, V-35, V-36, V-43, V-44 |
| Sections 13 to 17 | V-18, V-47, V-11, V-07, V-59, V-21, V-19 |
| Sections 18 and 19 | V-55, V-50, V-51 (deferred) |
| Section 12b | V-02, V-03, V-05, V-09, V-10, V-14, V-20, V-32, V-37, V-39, V-40, V-41, V-42, V-46, V-48, V-60, V-61, V-62 |
| Sections 12c to 12f | V-33, V-38, V-15, V-16 |
| Section 19b (last) | V-53, V-54, V-56, V-57, V-58 |

Every violation is scheduled (owner 2026-11-16: D23 to D26).

## Owner list (P1)

Items the P1 agent did not guess (2026-11-17). Nothing here blocks P2.

1. **Queue ids defined twice** (`tools/test_prefix_lint.py` check d, accepted in `tools/check_known.csv` until settled):
   20 ids have two `## ID` rows in `testing/rows/*.qb`, each in a different section (a rerun or a second batch that reused
   the id): `SQ-VTD8-01` (CDJ.qb:385, CDJ.qb:205), `SQ-DEP-02` (DEP.qb:201, DEP.qb:25), `SQ-CNS-03` (FO.qb:317, CNS.qb:55), `SQ-HH-304` (HOP.qb:306, HH.qb:6012), `SQ-HH-1546` (HOP.qb:329, HH.qb:10265), `SQ-CDW-01` (MIN.qb:18, DEP.qb:233), `SQ-CDW-02` (MIN.qb:33, HH.qb:11825), `SQ-CDW-03b` (MIN.qb:72, DEP.qb:240), `SQ-MIN-03` (MIN.qb:122, DEP.qb:225), `SQ-PWG-07` (PWG.qb:1725, PWG.qb:313), `SQ-PWG-06` (PWG.qb:3456, PWG.qb:132), `SQ-FGD-14` (RD.qb:226, CDJ.qb:425), `SQ-MDP-13` (RD.qb:870, CDJ.qb:318), `SQ-MDP-14` (RD.qb:900, CDJ.qb:332), `SQ-MDP-16` (RD.qb:962, CDJ.qb:339), `SQ-CNS-01` (RD.qb:1712, CNS.qb:16), `SQ-CNS-02` (RD.qb:1725, CNS.qb:28), `SQ-CDJ-05` (RD.qb:2071, CDJ.qb:305), `SQ-DM-03` (TSK.qb:110, SB.qb:233), `SQ-RW-28` (VS.qb:74, GF31.qb:72). Proposal: the later row gets a letter suffix (`SQ-CNS-01b`) or `(rerun)`; the
   scratch-test owner session decides, since the rows and their verdict history are theirs.
2. **Number ranges the date tool reports** (not dates, left as written): `briefs/recipes_cooking.md` "cooking.js 12-13",
   `plan_1.0.3.md` "12-16 rounds", `split/F_pack_scripts.md` "- 12-18:" (line numbers), `testing/structures/02_new_mods_tests.md`
   "30 and 12-15", `todo/rows/kitchen_and_cooking.csv` "12-14 (moved from cooking...)" (line numbers). No impossible
   dates were found (D5): every year-less date that is a date resolved to 2026 on or before its file's last change.
3. **V-61 "Create Confectionery"**: the jar's own display name is "Create Confectionery", so `unreleased.md` was not
   changed; confirm, or name the pack form if there is one.
4. **V-60 "JEI" in `unreleased.md`**: left as is until G17 (JEI and WTHIT in player text) is decided.
5. **NOTICE name for files that credit several licences**: `NOTICE-thirdparty-various.txt` (Common, Compat, Delight);
   Equipment's credits only MIT (`NOTICE-thirdparty-MIT.txt`). Rename if another word is wanted.
6. **Generated docs and the date rule**: the date sweep converted dates in generated docs (for example
   `item_physics_audit.md`, `stack_sizes.md`); their generators still print year-less dates and will write them back on
   the next run. The generators are code: P5 section 15 code pass. The spelling sweep skipped generated docs.
7. **Rows files keep their names**: `EN.qb` holds `SQ-EDL-` rows, `LT.qb` `CQ-LTP-`, `PB.qb` `SQ-PFS-`, `RP.qb` `CQ-RPB-` and
   the Raspberry Pie `SQ-RPI-` / `CQ-RPI-`, `IP.qb` section 2 `SQ-PRT-` / `CQ-PRT-`. Renaming a rows file renames its
   generated queue blocks; left for the scratch-test owner.
8. **Alcohol Part B numbering**: `alcohol_part_b.md` keeps the old B-section numbers without the B (0 to 9, `7.14`), not
   a new 0 to 6 layout; references in other docs were rewritten to `alcohol_part_b.md 7.14`. Section numbers 10 to 13
   in it still point to alcohol.md (noted at its top).

## 21. Done means for this plan

- design_rules.md section 9 status cell per id: fix date and "Built, untested" until the queue row passes.
- New and updated lints registered in design_rules.md section 1; `tools/design_rules_audit.py` clean at the end.
- Writeups updated for each touched feature (tooltips.md, food_tuning, creative_common, creative_split.md, mod_sides.md),
  History lines added.
- Changelog bullets: section 12e (no recipe unlocks); section 15b (American spelling in own lang and tooltips); section 5 only if the probe finds modifiers that were missing; section 9 when TODO
  `tooling/design-rules #4` turns the demo off; V-46 if a shown name changes. Run `python3 tools/changelog_lint.py`.
- Queue rows for: sea pickle tea, palette switch, GlmMerge count and loot probes, member switch off (`salt`), recipe unlocks (12e), trade dump diff (12c), loot probes for moved rules (12d), grass seasons and vintage clock (12f), Exposure
  and underscore-rename old-id save, namespace recipe counts and id diff, food dump diff, tooltip screenshots.
- Hand-over lists the exact test ids and which jars wait to be copied to `mods/`.

## History

- 2026-11-16: section 5b step 3 names `tools/loot_tuning_sim.js` as a component migrated in the split (owner, TODO `tooling/generators-and-tests #9`).
- 2026-11-16: Creative Loot split out of Worldgen added (D13 changed, section 5b, phase P2b; D25 now names Creative Loot); V-26 all moved (D28); O5 closed.
- 2026-11-16: D27 (writeup split by tree-TOC usefulness, not size); V-26 broken down per file in section 6.
- 2026-11-16: owner answers: V-05, V-14, V-32, V-33, V-38, V-41 scheduled with the recommended fix; recipe unlocks
  removed pack-wide (12e); sideways edges recorded and the Calendar, Delight and Brewing dependencies used to move code
  into the depending jar (12f); Delight and Brewing require Worldgen (D25). Sections 12c to 12f added; no violation unscheduled.
- 2026-11-16: review pass. Decisions D7 to D19 and open list O1 to O5 added (O1, O2 then settled as D20, D21; D22 confirmed section 6); phase order rewritten with reasons (Worldgen
  before members for loot, date pass before 2027); test prefix index built ([testing/test_prefix_index.md](testing/test_prefix_index.md),
  10 renames) and pointed to from CLAUDE.md, README, DR-W3 and two skills; section 5 redesigned for a first-only list
  (Worldgen GlmMerge); section 6 bound helper `forPack`; V-04 and V-48 use member switches; V-03 repack route;
  V-62b and V-62c settled; section 13 limited to text (identifier audit TODO `tooling/design-rules #3`); palette demo
  stays on until TODO `tooling/design-rules #4`; section 19b gap references fixed (G13, not G1; World column is DR-W4).
- 2026-11-16: section 12b and 19b added (small fixes, docs form last); Power Grid stays in mods/, license GPL-3.0-only confirmed.
- 2026-11-16: plan written from the owner brief and the 2026-11-14 audit. Section 4 tilled end stone found already built (plan_bugfix_1114 3.2).
- 2026-11-17: P0 and P1 built (P1 agent): rule text (DR-L7 for spelling, since DR-L6 was taken), `.editorconfig`; sections 2, 3, 8, 10, 12b docs rows, 15 and 15b doc passes; "Owner list (P1)" added. Mechanical sweep commits carry `[no-stale]`, which `qrowlint.py` now skips when it dates a source change.
- 2026-11-17: P2 built (P2 agent), Creative Common installed: section 4 dict lines and tilled end stone tags, `tools/dict_lint.py`; section 6 `NCH.forPack` with `logName`, `global.reg.forPack`, `profession_dict.js`, `tools/namespace_lint.py` in report mode (written to [namespace_report.md](namespace_report.md); a separate tool, not inside `id_owner_lint.py`, so the report mode does not fail check_all), allowlist as `tools/namespace_allowlist.csv` (CSV rule, not the TSV named in 6.4); section 7 `tools/literal_id_swap.py` and 44 missing keys; section 9 `paletteDemo`; section 12 `global.effect.resolve`, `col` roles and `global.tooltips.code`, absolute food rows (the applier is Creative Delight's `food_value_copies.js`, not Common's: it ships with the P3 Delight rebuild); 12b V-37 `NCH.cfgClass` / `cfgValue`; 12c `global.trades.villager` with an audit report (the failing lint waits for the P3 conversions); 12e `NoRecipeUnlockMixin`. Queue SQ-DRK-08, CQ-TT-04, -05, SQ-AV-01, CQ-AV-01, -02.
- 2026-11-17: P2b built (P2b agent), `creativeloot-1.0.0.jar` and `creativeworldgen-1.0.0.jar` installed together: section 5b steps 1 to 4 ([creative_loot.md](creative_loot.md)), `global.LOOT.rules` with the 4 Creative Delight callers edited in the pack folder (they ship with the P3 Delight rebuild, which also adds the D25 `creativeloot` dependency to Delight and Brewing), `tools/loot_tuning_sim.js` migrated and passing; section 5 GlmMerge written (member list files stay until P3; members register `GlmMerge.gate` for their switches then). Findings: Forge 1.19.2's `LootModifierManager` already reads every copy of the list (`getResourceStack`), so only a `"replace": true` copy can drop entries; Worldgen had no `[loot]` toml section, so `ConfigRename` had nothing to carry; `loottags.js` moved with the tree but its tag ids stay `creativeworldgen:loot/*` (renaming them is a Common dict edit, left for section 6 in P3). Queue SQ-LOOT-01 to -06 (SQ-CWG-05 superseded).
