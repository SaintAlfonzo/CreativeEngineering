# Design rules: code, documentation, writeups and language

Every design rule for how the pack's code, docs and text are written, collected in one place on 2026-11-13 from
CLAUDE.md, the memory rules, the skills and the writeups. This file is the starting point for turning them into hard
rules. Each rule has an id (`DR-<area><n>`) so it can be confirmed, changed or dropped by id. Section 7 lists the
gaps: areas with no rule yet, or where the docs contradict each other. Section 8 says how well each rule is followed,
section 9 lists every violation found (ids `V-NN`, for later fixes), and section 10 adds the unwritten conventions the
2026-11-14 audit found in the code and docs.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=5fd4ce17ff16 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 1 open TODO row (Planned 1), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `tooling/conventions #10` | Planned | none | id_owner_lint: 8 unresolved ids |
<!-- generated:status_index end open_work -->

| Part | Status |
|---|---|
| Collection of existing rules (sections 2 to 6) | Collected 2026-11-13: 63 rules and 7 formatting observations (section 11 lists the sources) |
| Compliance audit (sections 8 and 9) | Audited 2026-11-14 (whole repo, read-only): 62 violations recorded as V-01 to V-62, all Open; 5 high |
| Unwritten conventions (section 10) | Found 2026-11-14: 52 proposed rules, marked Proposed until the owner confirms them |
| Hardening (owner confirms, edits or drops each rule; decides the gaps) | In progress: G1, G2, G3, G5, G11, G12, G13, G16, G19, G20 decided 2026-11-16 (plan_rules_fixes.md D1 to D26); the rest of section 7 open |
| Enforcement | 6 rules are checked by a tool (section 1); `tools/design_rules_audit.py` reports 13 more; the rest rely on review |

**Hardness of each rule** (column H in the tables):

- **Hard**: the owner stated it, and it is written in CLAUDE.md, a skill or a writeup.
- **Checked**: Hard, and a script fails or reports when it is broken.
- **Memory**: the owner stated it, but it is written only in Claude's memory, not in the repo.
- **Convention**: what the files do consistently, never stated as a rule.
- **Proposed**: a convention found by the 2026-11-14 audit (section 10), waiting for the owner to confirm it.

## 1. Tools that enforce rules

| Tool | Rules | What it checks |
|---|---|---|
| `python3 tools/changelog_lint.py` | DR-P5, DR-P9 | changelog file format, labels (New, Changed, Fixed, Removed), icons, frozen deployed files |
| `python3 tools/name_audit.py --check` | DR-P10, DR-P11 | raw lang keys shown as names; Title Case of every displayed name |
| `python3 tools/id_owner_lint.py` | DR-C11 | ids, channels, JEI and WTHIT uids, `ce` names outside Creative Engineering |
| `python3 tools/test_prefix_lint.py` | DR-W3b | prefix rows, defining lines outside the owner doc, duplicate prefixes, duplicate queue ids (fails) |
| `python3 tools/iso_dates.py` / `python3 tools/us_spelling.py` | DR-L4b, DR-L5, DR-L7 | sweep tools (dry run by default, report per file); `--apply` rewrites |
| `python3 tools/texture_provenance.py` | DR-C18 | every derived texture has a `sources.csv` row |
| `python3 tools/design_rules_audit.py` (`-v` lists every hit; name checks to run only some) | DR-L1, DR-L4, DR-L4b, DR-L5, DR-W3b, DR-W9, DR-W11, DR-W16, DR-C1, DR-C2, DR-C10, DR-C14, DR-C19, DR-C21 to DR-C25, G1 | report only (exit 0): emoji, relative and non-ISO dates (DR-L4b), spelling mix, citation form, test prefixes (DR-W3b), path spans, config backups, `mod_sides.md` rows, underscores, license fields, raw recipe and tag calls, literal ids, formatting counts, doc file names |

## 2. Language (all text)

| Id | Rule | H | Source |
|---|---|---|---|
| DR-L1 | No emoji anywhere: code, comments, docs, configs, lang files, commit messages, chat. Arrows, en and em dashes and × are fine. | Hard | CLAUDE.md; memory `no-emojis-in-files` |
| DR-L2 | Status markers are plain words, never symbols: VERIFIED, DEPLOYED, BUILT, BROKEN, PASSED (writeups); Bug, Planned, Pinned, In Progress, Testing, Complete (TODO). | Hard | README.md "Status legend"; note [todo-layout](agent_notes/todo-layout.md) |
| DR-L3 | Abbreviations from the CLAUDE.md glossary (CE, CD, FD, IE ...) are fine in docs and chat; spell them out in player-facing text and changelog bullets. | Hard | CLAUDE.md "Glossary" |
| DR-L4 | Dates are absolute, `YYYY-MM-DD`. Never "yesterday" or "last week" in a file. | Convention | every writeup |
| DR-L4b | ISO 8601 only: dates are `YYYY-MM-DD` everywhere (docs, comments, scripts, tool output, memory, TODO rows, queue rows; commit messages excluded); time stamps `YYYY-MM-DD HH:MM` (24-hour). No year-less `11-14`, no `Nov 14`, no `14/11`. | Checked (report) | plan_rules_fixes.md section 15 (owner, 2026-11-16); `design_rules_audit.py dates` |
| DR-L5 | Owner decisions are cited inline with their date: "(owner, 2026-11-02)"; the word is always "owner", never "user" (D9). | Hard | 279 uses of "owner", 43 older uses of "user" (section 7, G5) |
| DR-L6 | Use in-game names exactly as the game shows them when naming content in prose. | Hard (changelog); Convention (docs) | changelog.md 3.6 |
| DR-L7 | American spelling in all own prose and player text: docs, memory, skills, comments, tool output, `unreleased.md`, own lang values, tooltip and chat text (color, armor, behavior, gray, center, license). Code identifiers follow their API; upstream lang text and deployed changelogs are not changed for spelling alone. Arrows are `->` in docs and test lines; player text adds no arrows. | Checked (report) | plan_rules_fixes.md D20, D21, section 15b (owner, 2026-11-16); `tools/us_spelling.py` |

## 3. Player-facing text

### 3.1 Changelog bullets (full rules: [changelog.md](changelog.md) section 3.6)

| Id | Rule | H | Source |
|---|---|---|---|
| DR-P1 | Write the bullet in the same session that makes the change playable in the dev instance (TODO row reaches Testing), in `kubejs/assets/creativecommon/changelog/unreleased.md`. | Hard | CLAUDE.md "Done means"; changelog.md 3.6 |
| DR-P2 | Group bullets under a feature family heading (`## Lunchboxes`); a lone change goes under a broad catch-all heading and moves into a family when a second change touches it. | Hard | CLAUDE.md |
| DR-P3 | Reader: a player who knows the game and the pack's mods, not its code. A change list, not a guide or a pitch. | Hard | changelog.md 3.6 |
| DR-P4 | Terse, facts only: what changed, where, the numbers. Old value in square brackets after the new one ("4 Corn Kernels [2]"), never "(was 2)". No benefit clauses, tips, reassurance or empty adjectives. One change per bullet; fragments and colons are fine. | Hard | changelog.md 3.6 (owner, 2026-10-21) |
| DR-P5 | Labels: New, Changed, Fixed, Removed. Fixes state the bug in past tense ("The Oven accepted no fuel."). | Checked | changelog.md 3.6 |
| DR-P6 | Never registry ids, file names, config keys, mod internals, test ids or TODO ids. Commands only if players can run them, in backticks. | Hard | changelog.md 3.6 |
| DR-P7 | No bullet for tooling, writeups, refactors, dev-only commands, unnoticeable perf work, or ports that behave the same. | Hard | CLAUDE.md; changelog.md 3.6 |
| DR-P8 | Keep the working list true: rewrite, merge or delete bullets when later work changes them. | Hard | changelog.md 3.6 |
| DR-P9 | A deployed version's file is never edited. Exception: an owner-requested restyle (same facts), its sha1 recorded in `deploy/changelog_restyles.json`. | Checked | changelog.md 3.6 |

### 3.2 Names, tooltips, lang

| Id | Rule | H | Source |
|---|---|---|---|
| DR-P10 | Every shown name is a real name, never a raw key or id. A fix borrows a related name first (the fluid's name for its block, the item's name for its projectile), then falls back to the id in Title Case. | Checked | names.md 11 |
| DR-P11 | Vanilla-style Title Case: every word capitalized except small words mid-name ("Block of Iron", "Flint and Steel"); hyphen and slash parts count as words ("High-Grade"). Exceptions go in `name_rules.json`. | Checked | names.md 11 |
| DR-P12 | Tooltip colors are picked by role, not by look, from the `col.*` legend in `tooltips.js`. | Hard | tooltips.md 2 |
| DR-P13 | Every tooltip change is recorded in [tooltips.md](tooltips.md), cross-linked from its feature's writeup. | Hard | README.md "Keeping them current" |
| DR-P14 | Wording the owner gives is used as given (for example the BlockRunner camo line). | Convention | tooltips.md 3 |
| DR-P15 | Each jar's lang file lives at `assets/<modid>/lang/en_us.json`; Forge 1.19.2 reads only the first jar's copy of a shared path. A resource pack zip outranks KubeJS lang, so fix names in the zip when one sets them. | Hard | creative_split.md rule 11; memory `forge-shared-asset-path`, `resource-pack-lang-priority` |
| DR-P16 | Mod display names follow the pack's form: Brewin' uses "&", the Vinery foundation shows as "Creative Brewing". | Hard | creative_brewing.md 2; names.md 12 |

## 4. Writeups and docs

| Id | Rule | H | Source |
|---|---|---|---|
| DR-W1 | One writeup per feature in `feature_writeup/`, the single up-to-date reference; code and config stay the source of truth for values. Readers: the owner coming back later, and Claude at the start of a session. | Hard | README.md intro |
| DR-W2 | Layout: 0 At a glance, 1 Overview, 2 Reference, 3 What the pack changes, 4 Limits and decisions (with rejected options and why), 5 File map, 6 Offline checks, 7 In-game test suite, 8 History. | Hard | README.md "Layout every writeup follows" |
| DR-W3 | Test ids are `PREFIX-NN`; each test is a checkbox line `- [ ] **PREFIX-NN Name:** setup and action -> expected`, the colon inside the bold and any result note after the colon (D12). Retire an id (strike through, "(moved to X)") rather than renumbering. Prefixes and owners: [testing/test_prefix_index.md](testing/test_prefix_index.md) (a new prefix gets its row first). | Hard | README.md; memory `feature-writeup-pattern` |
| DR-W3b | A new test prefix gets its row in [testing/test_prefix_index.md](testing/test_prefix_index.md) first; `tools/test_prefix_lint.py` must pass (no prefix without a row, no id defined outside its owner doc, no prefix on two rows, no queue id defined twice). | Checked | plan_rules_fixes.md section 2 (owner, 2026-11-16) |
| DR-W4 | Scratch queue rows are a table `ID / Group / Commands / Pass / If it fails`, ids `SQ-<feature>-NN` (server) and `CQ-<feature>-NN` (client), in a dated section at the end of the queue. | Hard | skill `scratch-test` part A |
| DR-W5 | Any change to a feature updates its writeup in the same session: values, file map, tests and a dated History line. | Hard | CLAUDE.md "Done means"; README.md |
| DR-W6 | Feature state (built, deployed, tested, versions) lives in the repo docs: README.md index, TODO.md, `plan_*.md`, deploy.md. Memory holds only rules, gotchas and mechanics. | Hard | CLAUDE.md; memory `memory-vs-docs-policy` |
| DR-W7 | TODO.md: categories with `##` feature families; row id `family/sub #n` never changes; columns Id / Change / Status / Tests / Work / Details / Decisions / Writeup (Tests added after the memory note was written); order by status (Bug, Planned, Pinned, In Progress, Testing, Complete), then Id; Complete rows move to the archive table. Cite rows as "[TODO](todo/TODO.md) `family/sub #n`". | Memory | note [todo-layout](agent_notes/todo-layout.md) (TODO.md header gives part of it) |
| DR-W8 | New ideas go in `todo/scratchpad.md` first; "translate the scratchpad" turns each note into a TODO row. | Hard | README.md intro; note [todo-layout](agent_notes/todo-layout.md) |
| DR-W9 | Links inside docs are relative markdown links (`../kubejs/...`). In chat, files and images are clickable workspace links, never bare paths; images are copied into the repo as one small combined sheet. | Memory | memory `links-not-filepaths` |
| DR-W10 | "Visualize" means markdown tables in the writeup. Never publish claude.ai artifacts. | Hard | CLAUDE.md; memory `no-published-artifacts` |
| DR-W11 | File names: `snake_case.md`; integration reports `<mod_snake>.md`; release and work plans `plan_<name>.md`; briefs `briefs/<family>_<sub>.md`; run docs `testing/scratch_run_<date>.md`. | Convention | skills `integration-report`, `scratch-test`; README.md |
| DR-W12 | Integration reports follow one of the two layouts in the `integration-report` skill (full report, jar upgrade); ports follow the model writeups named in the `port` skill (veggies_delight.md, miners_delight.md). The older integration reports are content examples per section, not layout models (2026-11-17, plan_rules_fixes.md V-62c). | Hard | skills `integration-report`, `port` |
| DR-W13 | Hand-over: name the exact test ids to run in game, any jar still waiting for `mods/`, and whether a changelog bullet was added (or why not). | Hard | CLAUDE.md "Done means" |
| DR-W14 | Every live check a change needs goes into the scratch queue in the same session; scratch servers boot only when the owner says so. | Hard | CLAUDE.md "Build and verify"; skill `scratch-test` |
| DR-W15 | Throwaway checks become `tools/` scripts that a writeup's section 6 can cite; probes and batch files live in `tools/`, never only in the scratchpad. | Memory and Hard | memory `feature-writeup-pattern`; skill `scratch-test` |
| DR-W16 | Hand-edited configs are copied to `configs_edited/root_config/<path>` (or `server_config/`); every new jar gets a `mod_sides.md` row. | Hard | CLAUDE.md |

## 5. Code

### 5.1 Structure

| Id | Rule | H | Source |
|---|---|---|---|
| DR-C1 | Recipes go through `global.NCH`, tags through `global.tags`. No `e.shaped`, `e.custom` or `event.add` in server scripts where a helper covers it; extend the helper instead. | Hard | CLAUDE.md |
| DR-C2 | Each item, block and fluid is defined once in the dicts (`global.item` / `item_dict.js` and siblings in Common's `_lib/`); scripts use the keys, never literal `namespace:id` strings. Only the dict file assigns keys. | Hard | CLAUDE.md; memory `item-dict-mechanics` |
| DR-C3 | Dict entry form, one per line: `i.key = 'ns:path';`, `i.key = '#ns:path';` (tag), `i.key = i.other;` (alias). Key forms: `<form>_<metal>` for material chains (`plate_steel`), bare mod path for machines, `cr_` for colliding Create utilities, `<ns>_<path>` for addon collisions. | Memory | memory `item-dict-mechanics` |
| DR-C4 | One solution per job: no second system beside an existing framework (compost, loot, item merges, food values, `NCH.cfg`, armor and tool effects). | Hard | pack_hygiene.md 1 (group G) |
| DR-C5 | No version gates or fallbacks between in-tree pieces that always ship together. | Hard | pack_hygiene.md (group A) |
| DR-C6 | Food code goes in Creative Delight's trees, one folder per mod; brewing, alcohol and the Winemaker in Creative Brewing; effects and potions in Common; gear rows in Equipment; time and seasons in Creative Calendar. | Memory | memory `creative-delight-routing`; creative_delight.md 956 |
| DR-C7 | Dependencies point down: Common depends on nothing of ours; content jars depend on Common. One owner per file; mixed files are split by row. | Hard | creative_split.md 3, rules 2 and 7 |
| DR-C8 | Jar scripts follow the Creative Delight and Creative Brewing layout. Each jar script pack has its own scope; only `global` crosses. | Hard | creative_split.md rules 5 and 9 |

### 5.2 Naming and ids

| Id | Rule | H | Source |
|---|---|---|---|
| DR-C9 | Content, tags and data ids use the namespace of the mod that owns them (since 2026-11-09), never `creativeengineering` unless the content is Creative Engineering's. | Hard | creative_split.md rule 1; plan_owner_ids.md |
| DR-C10 | No underscore in own mod ids, Java packages, `<modid>-common.toml` names or script-pack folders (`creativecommon`, `creativeworldgen`). | Hard | creative_split.md rule 10; memory `creative-mod-naming` |
| DR-C11 | `CE` / `Ce` class names, `CE_` constants and `ce...` commands only in Creative Engineering; event subscribers, channels, JEI and WTHIT ids name their own jar. | Checked | `tools/id_owner_lint.py` |
| DR-C12 | A moved id gets a swap-list entry in the moving jar's `data/<modid>/swaps/*.json` (never pruned), so saved worlds carry over. | Hard | creative_split.md; memory `creative-mod-naming` |
| DR-C13 | Config keys move with their feature and keep their names and comments. | Hard | creative_split.md rule 3 |

### 5.3 Forbidden patterns

| Id | Rule | H | Source |
|---|---|---|---|
| DR-C14 | No advancements for rewards or unlocks (the pack drops them all at load). No recipe unlocks of any kind (recipe book, advancements, knowledge books). No features that grant items or detect progress through advancements. | Hard | CLAUDE.md; advancements.md (owner, 2026-10-18); plan_rules_fixes.md D23 (owner, 2026-11-16) |
| DR-C15 | Ports and backports: no old-version or cross-mod conversions or remaps. Code from an ARR donor is written from behavior, never translated from a decompile. | Hard | CLAUDE.md; skill `port` |
| DR-C16 | Structure searches go through Async Locator off the server thread, never a synchronous search. | Memory | memory `async-locator-everywhere` |
| DR-C17 | No `getChunkAt`, `getBiome` or `getHeight` on ungenerated land from a KubeJS command; no `r.getType()` while looping all recipes. | Hard | skill `scratch-test` gotchas.md |
| DR-C18 | Textures: human art or the owner's tools only (`palette_swap.py`, `combine_tex.py`, `scale_tex.py`, `upscale_sprites.py`) on a source and palette the owner picked. Never drawn, generated, palette-picked or transformed by an ad-hoc script. Each derived texture gets a `sources.csv` row the same session. | Checked | CLAUDE.md; texture_sources.md 1 |

### 5.4 License

| Id | Rule | H | Source |
|---|---|---|---|
| DR-C19 | Own work is `GPL-3.0-only` (SPDX): full GPL-3 text as the `LICENSE` file, `mod_license=GPL-3.0-only` in `gradle.properties` and so `license="GPL-3.0-only"` in mods.toml (widening to `GPL-3.0-or-later` is a one-line change per jar). Code under another license keeps its notice in a separate file named `NOTICE-<scope>-<licence>.txt` (`NOTICE-upstream-MIT.txt`, `NOTICE-commonmark-BSD.txt`; license `various` when one file credits several licenses, `NOTICE-thirdparty-various.txt`); each jar ships its LICENSE and NOTICE files and the build gate in `ce.forge-mod.gradle` checks them. | Checked | CLAUDE.md; plan_rules_fixes.md D1, section 8 (owner, 2026-11-16); `design_rules_audit.py licence` |
| DR-C20 | Abnormals-licensed and other no-modification mods: our jars carry none of their textures, models or data; reference them by resource location. | Memory | memory `license-gplv3`; mooblooms.md 4.0 |

### 5.5 Formatting and comments

| Id | Rule | H | Source |
|---|---|---|---|
| DR-C21 | Indent is one tab per level in every own source file: Java, JS, Gradle, Python and own hand-written JSON. No spaces as indent; alignment spaces after the indent are allowed. Out of scope: YAML (tabs are illegal), Markdown, game-written configs, generated files (under `kubejs/generation_scripts/`, `kubejs/exported/`, a `build/` folder, or an output listed in `tools/generators.json`) and files baked from an upstream donor. `.editorconfig` at the root sets this for editors. | Hard (from P0; sweep in plan section 16) | plan_rules_fixes.md D3, D7 (owner, 2026-11-16) |
| DR-C22 | No trailing whitespace in own source files; files end with one newline; LF line endings. | Hard | plan_rules_fixes.md D7 (owner, 2026-11-16) |
| DR-C23 | Line length and brace style are free. | Hard | plan_rules_fixes.md D7 (owner, 2026-11-16) |
| DR-C24 | No formatter in the build (no Spotless or Checkstyle); `.editorconfig` and `design_rules_audit.py format` are the tools. | Hard | plan_rules_fixes.md D7 (owner, 2026-11-16) |
| DR-C25 | (merged into DR-C21 and DR-C22, 2026-11-16) | | |
| DR-C26 | Code comments say what the code does now. History, status, dates of past changes and "was X" notes go to the writeup's History, not into comments. File headers stay as they are (no license header rule); Creative Computers files carry no source header (owner, 2026-11-01). | Hard | plan_rules_fixes.md D8 (owner, 2026-11-16) |
| DR-C27 | Commit messages: short free text, mostly lowercase ("power grid backport"); no format. | None |

## 6. Agent and session conduct

| Id | Rule | H | Source |
|---|---|---|---|
| DR-A1 | Agent-to-agent messages: first contact says who the agent is, its overall goal, then the request. No pleasantries. Plain statements: goal, context, request, return format. Delegate with pointers, not retyped content. | Hard | CLAUDE.md "Agent-to-agent messages" |
| DR-A2 | Delegation is provisional: when the other side reports a false premise or finished work, say so in one line and re-split. | Hard | CLAUDE.md |
| DR-A3 | Keep context lean: focused Bash output, grep big docs; a one-line "good point for a new session; status is in <doc>" at about 150k or a natural break. | Hard | CLAUDE.md; memory `token-economy-habits` |
| DR-A4 | Confirm scope before acting on vague or wide requests, especially ones that reach features not under discussion. | Memory | memory `confirm-vague-wide-scope` |
| DR-A5 | Game process command lines only with the access token redacted in the same command. | Hard | CLAUDE.md |

## 7. Gaps and conflicts to decide

The agenda for making these rules hard. Each item names the rule ids it would settle.

- **G1 Spelling** (closed 2026-11-16: DR-L7). The docs mixed British and American spelling: `colour` 608 times, `color` 84 (mostly in code
  names); "capitalization" 2, "capitalization" 5. Pick one for prose (code identifiers follow the language or API).
  **Decided 2026-11-16** ([plan_rules_fixes.md](plan_rules_fixes.md) D20: American; code identifiers follow their API).
- **G2 Code formatting** (closed 2026-11-16: DR-C21 to DR-C24). Indent per language, tabs or spaces, line length, brace style, and
  whether to add an `.editorconfig` (editors apply it with no build change) or a formatter (Spotless rewrites files
  and would need a one-time reformat commit).
  **Decided 2026-11-16** ([plan_rules_fixes.md](plan_rules_fixes.md) D7: tabs as indent in every own source file, Python included; no trailing whitespace; `.editorconfig`; line length and braces free).
- **G3 Comments** (closed 2026-11-16: DR-C26). When a comment or doc comment is required, what it may say (no history or status in
  code comments?), and file headers (license line or none).
  **Decided 2026-11-16** ([plan_rules_fixes.md](plan_rules_fixes.md) D8: comments say what the code does now (DR-C26); history goes to the writeup; no file header rule).
- **G4 Commit messages** (DR-C27). Format and length, or leave free.
- **G5 Decision citations** (DR-L5). "owner" (279 uses) or "user" (43, older): settle on one word and the form
  "(owner, YYYY-MM-DD)".
  **Decided 2026-11-16** ([plan_rules_fixes.md](plan_rules_fixes.md) D9: "owner", form "(owner, YYYY-MM-DD)").
- **G6 Writeup prose.** Only the changelog has written prose rules (DR-P4). Should writeups follow the same
  terse, facts-only style, and who is "you" (README.md and tooltips.md address the owner as "you")?
- **G7 Writeup size.** Some writeups have grown very large (creative_delight.md is 2,124 lines). Set a size at which
  a writeup splits into sub-files, and a cap on dated banner lines at the top of docs (scratch_queue.md has dozens).
  **Decided 2026-11-16** (plan_rules_fixes.md D27: split when a tree-style contents table serves better than one file; no line threshold; banner cap in V-58).
- **G8 Memory-only rules** (DR-W7, DR-W9, DR-C3, DR-C6, DR-C16, DR-C20, DR-A4). Move each into the repo (this file
  or the writeup it belongs to) so it is not lost if memory is cleared.
- **G9 Where rules live.** CLAUDE.md, this file, the skills and the memory all hold rules today, with overlap. Decide
  whether this file becomes the single home (CLAUDE.md keeping only a pointer and the rules needed every turn).
- **G10 Enforcement.** Candidate checks for a docs lint: emoji in any tracked file (DR-L1), relative dates (DR-L4),
  bare file paths instead of links in writeups (DR-W9), writeup section layout (DR-W2), test id format (DR-W3).
  `tools/design_rules_audit.py` (2026-11-14) covers the mechanical half as a report; decide which checks should fail.

Conflicts the 2026-11-14 audit found between a written rule and what the files do:

- **G11 License string** (closed 2026-11-16: DR-C19). The rule says `license="GNU GPL3"`; all 44 jars use `GPL-3.0-only` (SPDX), with
  an "upstream; overlay" form for repacks. Likely fix: change the rule text. Also settle NOTICE file names
  (`NOTICE-upstream-<licence>.txt` for donors, `NOTICE-thirdparty.txt` for libraries).
  **Decided 2026-11-16** ([plan_rules_fixes.md](plan_rules_fixes.md) D1: `GPL-3.0-only`; NOTICE form `NOTICE-<scope>-<licence>.txt` (plan section 8)).
- **G12 Old value in changelog bullets** (closed 2026-11-16: DR-P4). The rule says "(was 2)"; every file since 1.1.03 uses `[2]` (335
  uses), and unreleased.md mixes both.
  **Decided 2026-11-16** ([plan_rules_fixes.md](plan_rules_fixes.md) D10: brackets `[2]`; DR-P4 text to change).
- **G13 Test form** (DR-W3). The README wants ticked checkbox lines; the integration-report skill asks for
  `| ID | Test | Pass |` tables (27 files). Tables cannot be ticked. Also: one form for a retired id, and whether a
  result note may sit between the bold name and the colon (117 lines do this).
  **Decided 2026-11-16** ([plan_rules_fixes.md](plan_rules_fixes.md) D12: checkbox lines, colon inside the bold, notes after the colon; the integration-report table converts).
- **G14 Status words for docs that are not builds** (DR-L2). 52 index rows say reference, audit, plan, research,
  archived and so on; scratch runs write PASS / FAIL / INCONCLUSIVE; findings tables write FIXED / OPEN / DONE.
  Extend the legend or replace them. This file used DONE until 2026-11-14.
- **G15 Split migrations** (DR-C15). Config carry-overs from our own jar split (8 `OLD_FILE` readers) and the camera
  bag id remap move our own data, not another version's. Exempt them, or set a release to remove them.
- **G16 Recipe-book unlocks** (closed 2026-11-16: DR-C14). The Oven calls `awardRecipes` like the furnace and Farmer's Delight. Exempt
  recipe-book parity, or drop it.
  **Decided 2026-11-16** ([plan_rules_fixes.md](plan_rules_fixes.md) D23: no recipe unlocks of any kind; the Oven, Keg and Copper Pot stop awarding, and a Common mixin blocks the rest).
- **G17 JEI and WTHIT in player text** (DR-L3). Players know these mods by the abbreviation (75 and 25 uses in
  deployed changelogs). Exempt them, or spell them out.
- **G18 Writeup layout variants** (DR-W2, DR-W5). 35 writeups add sections after History; integration reports, jar
  upgrades, plans and audits use their own layouts. Name the allowed layouts, say whether appendix sections may
  follow History, and fix the History order (oldest first, appended: 87 files; 11 newest first; 21 mixed).
- **G19 Arrows** (closed 2026-11-16: DR-L7, DR-W3) in test lines: `->` (441) and `→` (895) are mixed, often in one file. Pick one.
  **Decided 2026-11-16** ([plan_rules_fixes.md](plan_rules_fixes.md) D21: `->`).
- **G20 Upstream-id exceptions** (DR-C10, DR-C9). `baby_mobs` and `more_mooblooms` (mod id, package, mixin json,
  config name), the `vinery_backport` lang namespace, Golden Food's `minecraft:` overrides and CE's
  `jrftl:prepared_flesh` are deliberate. List them as named exceptions, or rename what is not saved data.
  **Decided 2026-11-16** ([plan_rules_fixes.md](plan_rules_fixes.md) D11: Baby Mobs and More Mooblooms are ours and are renamed; exceptions are `jrftl:prepared_flesh` and Golden Food's `minecraft:` overrides).

## 8. Compliance by rule (audit 2026-11-14)

Method: `tools/design_rules_audit.py` and the four rule tools (section 1) for the mechanical rules; three read-only
passes for judgement (own Java and build files; the 665 tracked KubeJS scripts; the writeups, skills, TODO, scratch
queue and changelog prose), with the strongest claims re-checked by hand. Upstream code inside ports was checked
only for license handling and DR-C14 / DR-C15. Followed = no or only trivial breaks; Mostly = a few breaks;
Poorly = breaks are common or growing.

| Rule | Result | Notes |
|---|---|---|
| DR-L1 | Followed | No emoji in own files. The 10 hits are third-party text (WTHIT's default symbols in its config, Kiwi's mod name in the generated mod list) and the lint's own regex |
| DR-L2 | Poorly | No symbols, but about 15 status words outside the legend (G14), in README.md, TODO.md and this file |
| DR-L3 | Mostly | Only JEI, WTHIT, IE, AE2 and FTB reach player text (G17) |
| DR-L4 | Mostly | No relative date words (10 hits are all commands or quotes), but 280 dates have no year (`(owner 11-05)`) |
| DR-L5 | Mostly | 339 "(owner, date)" against 74 older "(user, date)" in 31 files |
| DR-L6 | Mostly | unreleased.md lowercases vanilla and mod names next to Title Case ones |
| DR-P1, DR-P8 | Followed | No stale or duplicate bullets found in unreleased.md |
| DR-P2 | Poorly | 5 one-bullet family headings in unreleased.md next to an existing catch-all |
| DR-P3, DR-P4 | Mostly | No flavor words; one missing number, one benefit clause, two reassurance bullets; old-value form differs from the rule (G12) |
| DR-P5 | Mostly | 3 Fixed bullets in present tense; 2 warnings labeled Changed. Lint: 0 errors |
| DR-P6, DR-P7 | Mostly | One "Mod Ids" heading and one datapack bullet are internals |
| DR-P9 | Followed | Lint frozen check passes; 55 over-long bullets are all in deployed files (record only) |
| DR-P10, DR-P11 | Mostly | `name_audit.py --check` reports OUT OF DATE (434 fixes, 13 raw villager professions in the preview) |
| DR-P12 | Poorly | 50 raw-color tooltip lines in 21 files; raw `§` codes in the effects framework; only 13 of 31 tooltip scripts use `global.tooltips` |
| DR-P15 | Mostly | Lang paths are right; one stray `.en_us.json`; 8 members share `data/forge/loot_modifiers/global_loot_modifiers.json` (effect unverified) |
| DR-P16 | Poorly | "Brewin' and Chewin'" in 5 post-rule deployed bullets and 42 writeup uses; Create addons without the colon form |
| DR-W2 | Mostly | About 75 writeups follow 0 to 8; 35 add sections after History; 5 have broken numbering; 2 hold a second document (G18) |
| DR-W3 | Mostly | 2,095 canonical test lines; about 290 off-form; 9 test prefixes used by two writeups each |
| DR-W4 | Mostly | 207 of about 285 queue tables use the documented header; a World column is common |
| DR-W5 | Poorly | 47 writeups have no History; 5 of the spot-checked recent files have History 2 to 6 weeks behind their changes |
| DR-W6 | Mostly | 27 memory files hold feature state; 13 docs have no index row; 51 index status cells are over 400 characters |
| DR-W7 | Mostly | 59 rows out of status order; about 60 status cells outside the six words; bare TODO citations outnumber linked ones 401 to 140 |
| DR-W9 | Poorly (if the rule covers docs) | 1,599 existing repo files are named as code spans, not links, in 259 docs |
| DR-W11 | Mostly | Snake_case throughout; brief subs keep TODO hyphens; run docs use two prefixes; `split/A_java.md` style capitals |
| DR-W12 | Mostly | The three model reports named in the skill do not match the skill's layout; newer reports do |
| DR-W16 | Mostly | 3 jars in `mods/` with no `mod_sides.md` row; 15 backups stale (mostly keys the mods added; real value drift in 4); 1 edited config with no backup; 1 orphan backup |
| DR-C1 | Followed | No shaped, shapeless or cooking calls outside the helpers; raw calls are sequenced-assembly steps (documented exception). Gaps: builder `.tagBlock`, villager trades |
| DR-C2 | Mostly | No dict key assigned outside `_lib/`; literal ids remain in `namechanges.js`, effect ids and CE registrations |
| DR-C3 | Mostly | Entry form followed; 2 conflicting duplicate keys (one is a live bug) and 1 duplicated id |
| DR-C4 | Poorly | pack_hygiene 30 to 35 all still open, and the copies have grown (effect resolvers 5 to 7, config reads 3 to 9) |
| DR-C5 | Mostly | Creative Delight still checks for its own always-bundled members (5 sites); Barbeques falls back for an absent CD |
| DR-C6 | Followed | No food code outside CD; no time or season code outside Calendar |
| DR-C7 | Followed | Common depends on nothing of ours; sideways edges are soft (gated mixins) |
| DR-C8 | Mostly | Packs cross only through `global`; one duplicate top-level function |
| DR-C9 | Mostly | Registrations use the owning id; Creative Compat recipes and some log, ticket and LootJS names still say `creativeengineering` / `CE` |
| DR-C10 | Mostly | Clean for every `creative*` jar; exceptions in G20 |
| DR-C11 | Followed | `id_owner_lint.py`: 0 flagged, 8 unresolved (upstream JEI uids and one channel) |
| DR-C12 | Mostly | Swap lists in every jar that took ids; ExposureExtras keeps a second, Java remap (pack_hygiene 11) |
| DR-C13 | Followed | Carry-overs keep keys and comments |
| DR-C14 | Poorly | No grants, but pack_hygiene 12's dead advancement code and modifiers are all still present |
| DR-C15 | Mostly | Conversions in findings 9 and 10 are gone; split migrations remain (G15) |
| DR-C16 | Followed | No synchronous structure search anywhere |
| DR-C17 | Mostly | Guarded `getType()` in 2 dump scripts |
| DR-C18 | Poorly | Texture ledger: 349 confirm, 27 redo, 4 swap (2,574 ok) |
| DR-C19 | Mostly | Every jar ships the GPL text and NOTICE files (build gate in `ce.forge-mod.gradle`); license string and NOTICE names differ from the rule (G11) |
| DR-C20 | Followed | No Abnormals assets in our jars |
| DR-C21 to DR-C25 | (no rule) | Own Java 1,390 files: 267 tab-indented, 6 mixed, 4,063 lines over 120. KubeJS 678 files: 432 tab-indented, 13 mixed, 1,981 over 120, 211 trailing-space lines. Python 179: spaces only, 1,206 over 120. No CRLF anywhere |
| DR-A1 to DR-A5 | Not audited | Session conduct; no files to check |

## 9. Violations to fix

Every violation found on 2026-11-14, one row per pattern (counts and example locations, not every instance). Status
is Open until fixed; write the fix date and session in the Status cell, and keep the row. Rows whose fix depends on a
gap in section 7 say so. Deployed changelog files are frozen: their breaks are recorded, never fixed (DR-P9).

### 9.1 Code: Java and build

| Id | Rule | Location | Problem | Fix | Sev | Status |
|---|---|---|---|---|---|---|
| V-01 | DR-C14 | `development/creativeBrewing/members/brewin/src/main/java/umpaz/brewinandchewin/forge/event/BnCCommonEvents.java:200-243` | Our port addition scans every block place and right-click near a keg to award an advancement the pack drops (pack_hygiene 12) | Delete `onBlockPlaced`, `onItemUsedOnBlock`, `checkTemperatureAdvancement` | med | Open |
| V-02 | DR-C14 | `development/creativeWorldgen/members/mooblooms/src/main/resources/packs/` (3 advancement modifier JSONs: `bred_all_animals.json` x2, `all_effects.json`) | Dead advancement modifiers | Delete them and their pack switches | low | Open |
| V-03 | DR-C14 | Upstream advancement JSONs in ports: Brewin' 49, Exposure 35, Vintage 11 | Dead data (optional strip from pack_hygiene 12) | Strip, or record as accepted upstream data | low | Open |
| V-04 | DR-C5 | `creativedelight/salt/SaltBlockTicks.java:64`, `compat/jei/SeasoningJEI.java:107`, `compat/DisplayDelightDishes.java:48`, `compat/jei/FeastPlatingJEI.java:36`, `food/PlacedFoodSol.java:117` | `ModList.isLoaded` on members that are always bundled in CD (Farmer's Delight is even mandatory) | Delete the checks and the dead branches | med | Open |
| V-05 | DR-C5 | `creativeDelight/members/barbeques/.../compat/PackSeasonings.java:47-61, 150-160` | Reflection with a "without Creative Delight" fallback its own javadoc calls impossible | Compile against CD (`compileOnly`) or fail loudly | low | Open |
| V-06 | DR-C12, DR-C4 | `development/exposureBackport/src/extras/java/com/SaintAlfonzo/exposureextras/ExposureExtras.java:65-79` | Hand-written `MissingMappingsEvent` remap beside the jar's own swap list (pack_hygiene 11) | Move the ids into `owner_namespace.json` `replaces`; delete the handler (G15) | med | Open |
| V-07 | DR-C4 | `development/creativeCalendar/.../CalendarConfigMigration.java` (171 lines) | Own config migrator beside Common's `swap.ConfigRename` | Use `ConfigRename.carrySections` | low | Open |
| V-08 | DR-P15 | 8 member trees ship `data/forge/loot_modifiers/global_loot_modifiers.json` (exposureBackport x2, brewin, hearth, vintage, veggies, ends, rustic) | Forge may read only the first jar's copy, so other members' loot modifiers may not load | Check in game (loot probe or `/forge` count); if confirmed, merge or give each a unique list. Queue a scratch row | med | Open |
| V-09 | DR-P15 | `development/creativeEngineering/src/main/resources/assets/creativeengineering/lang/.en_us.json` | Stray hidden lang copy ships in the jar | Delete it | low | Open |
| V-10 | DR-C9, DR-C11 | `"[creativeengineering]"` log tags in `creativedelight/compat/DisplayDelightDishes.java:62,71`, `DisplayDelightDishBlocks.java:57`; logger names in `creativeCompat/.../ItemBurnTransform.java:42`, `creativeDevtools/.../StructureCatalogue.java:73`, `MassPlacer.java:95`; ticket `creativeengineering_locate` in `creativeWorldgen/.../LocateLoads.java:51` | Runtime names point at the wrong jar (not saved data) | Use each jar's mod id; add the pattern to `id_owner_lint.py` | low | Open |
| V-11 | DR-C10 | Packages `com.SaintAlfonzo.baby_mobs`, `com.SaintAlfonzo.more_mooblooms`; mixin jsons and default config names `baby_mobs-common.toml`, `more_mooblooms-common.toml`; lang namespace `vinery_backport` | Underscores in own names (mod ids kept on purpose) | G20: name as exceptions, or rename packages and configs | low | Open |
| V-12 | DR-C19 | Every own `gradle.properties` (`mod_license=GPL-3.0-only`) | Rule text says "GNU GPL3" | G11: fix the rule text | med | Fixed 2026-11-17 (DR-C19 text) |
| V-13 | DR-C19 | NOTICE names: `NOTICE-upstream-MIT.txt` (19), `NOTICE-thirdparty.txt`, `NOTICE-vinery-upstream.txt`, `NOTICE-upstream.txt`, `NOTICE`, `NOTICE-commonmark.txt`, `NOTICE-upstream-MIT-enders.txt`; `development/backportedPowerGrid/LICENSE` holds Apache 2.0 | No single naming form; Power Grid's `LICENSE` is the upstream license | G11: settle names; rename Power Grid's file to `NOTICE-upstream-Apache.txt` | low | Fixed 2026-11-17 (NOTICE renames, audit check) |
| V-14 | DR-C15 | `development/creativeDelight/members/miners` (34 files in `com.sammy.minersdelight`) | ARR rewrite keeps the upstream package, unlike the other ARR rewrites | Repackage to `com.SaintAlfonzo.creativedelight.miners`, or record the reason in miners_delight.md | low | Open |
| V-15 | DR-C14 | `creativedelight/oven/OvenBlockEntity.java:365-368` | `awardRecipes` unlocks recipe-book entries | G16 | low | Open |
| V-16 | DR-C7 | `development/creativeDelight/build.gradle:52` (`compileOnly project(':backportedBrewin')`, coaster mixin); `creativeCalendar/.../VineryGrassSlabSeasonMixin.java`, `VintageLushGrassSeasonMixin.java` | Sideways edges (gated, so rule 2 allows them) | Record in creative_split.md, or move each patch into the target jar | low | Open |
| V-17 | DR-W16 | `mod_sides.md` | No row for `mods/debugify-1.1.0-forge-1.19.2.jar`, `mods/creativeLabels-1.0.1.jar`, `mods/backportedPowerGrid-0.6.2-ce-19.2-17.jar` | Add the rows. Note: Power Grid is in `mods/` though the port plan keeps gate jars in `jar_archive/pwg_gate/`; check that is intended | med | Fixed 2026-11-17 (Labels row, build_deploy skips Power Grid, audit covers client_mods/) |
| V-18 | DR-L2, G3 | `patcherMod` 143 times in 70 own Java files and 8 mods.toml files (for example `creativeDevtools/.../mods.toml:25` "becomes creativecommon at the Q1 swap") | Stale name and future-tense plan status in comments | Replace with "Creative Common"; drop the plan wording | low | Open |
| V-19 | G3 | 261 dated history lines in 120 own Java files (for example `CreativeCommon.java:172`) | History in code comments (no rule yet) | Decide in G3 | low | Open |
| V-20 | pack_hygiene 18 | `development/creativeDelight/src/main/resources/data/tmted/recipes/crusher/wheat_flour.json`, `wheat_flour_from_bale.json` | Undocumented `forge:false` overrides | Document, or replace with `NCH.remove` | low | Open |
| V-21 | DR-C21, DR-C22 | 6 Java and 13 JS files mix tab and space indent; 211 JS lines have trailing spaces | Mixed whitespace | Fix after G2 sets the standard | low | Open |

### 9.2 Code: KubeJS scripts

| Id | Rule | Location | Problem | Fix | Sev | Status |
|---|---|---|---|---|---|---|
| V-22 | DR-C2, DR-C3 | `development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js:131` and `:12605`; used at `kubejs/data/creativedelight/kubejs/hearthandharvest/tags.js:60` | `i.tilled_end_stone` assigned twice (Hearth & Harvest, then Ender Crop); the later wins, so H&H's tags.js tags Ender Crop's block as `mineable/pickaxe`. Live bug | Split into `hh_tilled_end_stone` and `endercrop_tilled_end_stone`; fix the use | high | Open |
| V-23 | DR-C2, DR-C3 | `item_dict.js:2163` and `:2212` | `i.t_sea_pickles` assigned twice with different tags; the first is lost (users: `creativedelight/drink_tags.js:12`, `drinks.js:148-151`, `barbequesdelight/skewers.js:82-83`) | One key per tag | med | Open |
| V-24 | DR-C3 | `item_dict.js:132` and `:138`; `:1191-1192` and `:4759-4760` | `hops_crop` and `hops` hold the same id; `spring_lead` / `small_spring_lead` placeholders overwritten later | Keep one key; drop the placeholders | low | Open |
| V-25 | DR-C9 | `kubejs/data/creativecompat/kubejs/other/misc_mods.js:23,24,74,75,83`, `other/flowers.js:11,34`, `other/feature_ports.js:31` | Creative Compat has no `recipes()` wrapper, so about 12 recipe ids are `creativeengineering:` | Add a Compat core with `recipes(fn)` = `NCH.withNamespace('creativecompat', fn)` | med | Open |
| V-26 | DR-C9 | `kubejs/server_scripts/magic/dragon_breathing.js`, `other/creative_labels.js`, `other/botany_pots.js` | Pack-folder recipes for Magic and Labels content default to `creativeengineering:` | Owner call: move into the owning jar, or accept the pack folder as CE | low | Open |
| V-27 | DR-C9, DR-C11 | `.logName('CE/...')`: 17 uses in 11 files (for example `kubejs/data/creativedelight/kubejs/farmersdelight/loot.js:17`, `kubejs/data/creativeworldgen/kubejs/loot/05_tuning.js`) | LootJS names say CE outside Creative Engineering | `CD/`, `CWG/` prefixes; add to `id_owner_lint.py` | low | Open |
| V-28 | DR-C9 | `development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/misc_items.js:26` | Registers `jrftl:prepared_flesh` under a foreign namespace (documented, keeps saves) | G20 | low | Open |
| V-29 | DR-C2 | `kubejs/client_scripts/namechanges.js` (79 of 83 literal ids have keys, pack_hygiene 22); `kubejs/data/creativeengineering/kubejs/engineering/transport.js:117,122`; `kubejs/server_scripts/tags/blocktags.js:61,278,366` | Literal ids where a dict key exists | Use the keys | med | Open |
| V-30 | DR-C2 | Effect ids: `kubejs/data/creativebrewing/kubejs/creativebrewing/jeed_providers.js:42-44` (52), `kubejs/data/creativeequipment/kubejs/equipment/end_gear.js`, `creativeequipment/gear_effects.js` (23), `creativecommon/effects/potions.js` (12) | Literal effect ids that have `global.effect` keys (pack_hygiene 23) | `global.effect.<key>`; have `gen_can_types.py` emit keys | med | Open |
| V-31 | DR-C2 | `kubejs/server_scripts/economy/trades.js:5-10`; 58 `e.create('ns:id')` (for example `creativeengineering/items/components.js`, `misc_items.js`) | A local profession dict outside the dict files; registration by literal while CD registers by key | Move to a dict; pick one registration form | low | Open |
| V-32 | DR-C1 | `kubejs/startup_scripts/items/decor_blocks.js:18-19`, `creativeengineering/items/coil_blocks.js`, `creativecompat/decor_blocks.js` (6 calls) | Builder `.tagBlock(...)` bypasses `global.tags` | Move to the owning tag script | low | Open |
| V-33 | DR-C1 | `kubejs/server_scripts/economy/trades.js` (52 raw `e.addTrade`), CD `salt/`, `barbequesdelight/`, `veggiesdelight/`, `hearthandharvest/`, `corn_delight/` `trades.js` (13) | `global.trades` covers only the Wandering Trader | Extend `trade_helper.js` with a villager helper | low | Open |
| V-34 | DR-C4 | `kubejs/data/creativedelight/kubejs/create/recipes.js:44-45`, `engineering/transport.js:116-125` | Hand item merges (pack_hygiene 30) | `global.addItemMerges` | med | Open |
| V-35 | DR-C4 | 7 private MobEffect resolvers: `creativebrewing/alcohol_drinks.js:51`, `potion_area.js:60`, CD `comfort_nourishment.js:32-33`, `effect_swaps.js:35,67`, `veggiesdelight/veggies_foods.js:8`, `vintagedelight/vintage_items.js:27` | Copies beside `global.armorEffects._resolveEffect` (pack_hygiene 31, grown from 5) | One resolver on `global.effect` | med | Open |
| V-36 | DR-C4 | `creativebrewing/alcohol_drinks.js:47`, CD `fruitsdelight/fruits_foods.js:12`, `vintagedelight/vintage_items.js:23`, `delightful/delightful_foods.js:26` | `FoodProperties$Builder` rebuilds beside the one applier `food_value_copies.js` (pack_hygiene 32) | Absolute-value rows in the applier | med | Open |
| V-37 | DR-C4 | Direct `Java.loadClass('...Config')` in 9 files (for example `creativedelight/soil_yield.js:88`, `creativeworldgen/worldgen/mob_spawns.js:56`, `creativecommon/effects/armor_effects.js:33`) | Config reads beside `NCH.cfg` and the jar cores (pack_hygiene 35, grown from 3) | Getter on `global.<JAR>`, or `NCH.cfg` | low | Open |
| V-38 | DR-C4 | `rusticdelight/crop_drops.js:80`, `creativedelight/soil_yield.js:134`; raw `LootJS.modifiers` in 5 CD loot scripts | Own loot rewrites beside Worldgen's pre-roll engine; no `global.LOOT` id check | Route through `global.CWG.lootRules` / `global.LOOT` | low | Open |
| V-39 | DR-C4 | `kubejs/assets/creativedelight/kubejs/_qol/placeable_tooltip.js:30-44`; `creativeequipment/func/tune_armor.js:57-60`, `func/tune_tools.js:59-62` | Copies of `global.tooltips.removeTooltipKey` and of the tuner half (pack_hygiene 33, 34) | Use the shared ones; factor out | low | Open |
| V-40 | DR-C8 | `kubejs/data/creativedevtools/kubejs/create/metal_test_recipes.js:20`, `wood_test_recipes.js:22` | Two top-level `function buildGrid` in one scope; the later replaces the first | One copy, or IIFE each file | low | Open |
| V-41 | DR-C7 | `kubejs/server_scripts/tags/blocktags.js:598-606` (`creativecompat:` tags), `tags/feature_port_tags.js:9` (`creativeequipment:`), `tags/wthittags.js:13-14` (`creativecommon:`) | The pack folder fills tags a jar's Java reads | Move each to its jar's script pack | low | Open |
| V-42 | DR-C17 | `kubejs/data/creativedevtools/kubejs/dump/dump_food.js:220`, `dump/dump_recipes.js:39,97` | `recipe.getType()` in a loop over all recipes (guarded) | Serializer key, as `dump_crafting_audit.js` does | low | Open |
| V-43 | DR-P12 | 50 lines in 21 files (for example `kubejs/assets/creativedelight/kubejs/ends_delight/tooltips.js:9-15`, `supplementaries/client.js:7-11`, `kubejs/client_scripts/tooltips.js:337-339`, `creativebrewing/potion_buckets.js:54,70`); raw `§7`/`§a` in `creativecommon/effects/armor_effects.js:141-144`, `tool_effects.js:72-77` | Tooltip colors outside the `col.*` legend | `global.tooltips.col.<role>` | med | Open |
| V-44 | DR-P12 | `kubejs/client_scripts/tooltips.js` (159 `col.dark_gray`, Material Information), `creativebrewing/vinery/tooltips.js:38` (`col.lpurple`) | Colors picked by look; the legend has no role for data lines | Add a role key (for example `col.data`) | low | Open |
| V-45 | DR-P3, DR-P6 | `kubejs/client_scripts/tooltips.js:14-34` | A "Test item" palette demo ships to players on White Wool and White Concrete | Delete, or gate behind Devtools | med | Open |
| V-46 | DR-P15 | `kubejs/client_scripts/namechanges.js:10-11, 64`; `creativeengineering/items/misc_items.js:22` | Pack folder names CE items; the transmitter is registered with no display name | Names in CE's registration or lang | low | Open |
| V-47 | DR-L2 | `patcherMod` 192 times in 117 scripts, including player-log text in the 8 jar cores (for example `kubejs/data/creativedelight/kubejs/_core/cd_server.js:12`); stale paths in `kubejs/client_scripts/plantable_tooltip.js:1`, `kubejs/data/creativedevtools/kubejs/_core/cdv_server.js:8` | Old name and paths | Rename to Creative Common; fix the paths | low | Open |
| V-48 | DR-C9 (proposed DR-C45) | About 19 `Platform.isLoaded` calls on CD and CB members inside CD trees (for example `creativedelight/patties.js:14-17`, `kitchen_tags.js:40`, `vintagedelight/vintage_items.js:13,18`) | Bypass the `[members]` switch that `global.CD.on(id)` reads | `global.CD.on(...)` | low | Open |

### 9.3 Docs, writeups and player text

| Id | Rule | Location | Problem | Fix | Sev | Status |
|---|---|---|---|---|---|---|
| V-49 | DR-W3 | `feature_writeup/README.md` index: prefixes `PB-`, `RP-`, `TS-`, `EN-`, `CN-`, `BP-`, `LT-`, `IP-`, `PWG-` each used by two writeups | Test ids collide (for example RP-03 points to 3 files) | Rename the newer prefix in each pair; record the old one | high | Fixed 2026-11-17 (renames applied, test_prefix_lint.py) |
| V-50 | DR-W5 | `feature_writeup/loot.md` (last History 2026-10-03, changed through 2026-11-13; a 2026-11-07 change filed under 2026-09-27), `engineering.md`, `creative_engineering.md`, `creative_delight.md`, `item_physics.md` | History far behind the changes | Add the dated lines from `git log` | high | Open |
| V-51 | DR-W5 | 47 writeups with no History (for example `visceral_heap.md`, `golden_food.md`, `item_merges.md`, `raspberry_pie.md`, `unusual_delight.md`, `ends_delight.md`, `salt.md`, `trade_sync.md`) | No dated record | Add History; add it to the integration-report and jar-upgrade templates | high | Open |
| V-52 | DR-W2 | `feature_writeup/remainder_buffer.md:462` (whole old plan pasted after History), `alcohol.md:1694` (a second document, Part B) | Two documents in one file | Cut or mark superseded; split Part B out | high | Fixed 2026-11-17 (alcohol_part_b.md, plan_remainder_buffer_old.md) |
| V-53 | DR-W2 | `curios_slots.md` (only 0 to 2), `texture_reuse.md:66` (2 then 8), `immersive_portals.md` (no 5), `ends_delight.md` and `ocean_delight.md` (two `## 3.`), `creative_engineering.md` (`## 4.1`) | Missing or broken numbering | Renumber or fill in | med | Open |
| V-54 | DR-W3 | About 290 off-form test lines: no checkbox (`jei_ordering.md:140`, `plan_1.0.3.md`, `plan_visceral_heap.md`, `structure_tuning.md`); no bold name (`vintage_delight.md` 69, `veggies_delight.md:599` 46, `miners_delight.md` 26, 173 in total); colon outside the bold (117 lines, for example `armor_effects.md:295`); 3 retired-id forms; `README.md:107` empty prefix cell | Test line form | Normalize after G13 | low | Open |
| V-55 | DR-W6 | 27 memory files hold feature state (for example `backported-brewin.md`, `plan-1-0-8.md`, `plan-7-1-execution.md`, `campfire-rain.md`) | State in memory | Move state into the writeups; cut memory to the mechanic | med | Open |
| V-56 | DR-W6 | `food_dictionary.md`, `item_physics_audit.md`, `plan_visceral_heap.md`, `plan_hearth_crow_salt.md` and 9 more `plan_*.md` have no README row; 51 index status cells over 400 characters (`README.md:63` is 3,478) | Index gaps; status cells used as logs | Add rows (or a plans table); cap cells at status plus one line | med | Open |
| V-57 | DR-L2, DR-W7 | `todo/TODO.md`: `Built` x24, `Done` x9, `Tested` x2, `Open` x3, `In progress` x2, `Idea`; 13 rows at `:1539-1554` with a sentence in Status; 59 rows out of order | TODO status words and order | Map to the six words; move sentences to Details; re-sort | med | Open |
| V-58 | DR-W4, G7 | `testing/scratch_queue.md`: 78 tables with other headers; about 28 dated banner lines at the top, out of order | Queue format | Add the World column to the rule; normalize; cap banners | low | Open |
| V-59 | DR-L4, DR-L5 | 280 dates with no year (`hearth_and_harvest.md` 108, `README.md` 15, `plan_creative_tips.md` 8); 74 "(user, date)" in 31 files (`todo/TODO.md` 18, `sticky_notes.md` 12) | Date and citation form | Full `YYYY-MM-DD`; "owner" after G5 | low | Fixed 2026-11-17 in docs, memory, skills, TODO rows, queue rows (tools/iso_dates.py, 1,231 dates; 5 number ranges left); code is plan P5 |
| V-60 | DR-L6, DR-P2, DR-P4, DR-P5, DR-P6 | `kubejs/assets/creativecommon/changelog/unreleased.md`: lowercase names `:22, :31, :33, :66, :90-91`; one-bullet families `:95, :104, :114, :132, :143`; missing number `:6`; benefit clause `:153`; reassurance `:126, :130`; present-tense Fixed `:71, :72, :134`; internals `:124, :129`; old-value forms mixed `:41, :145`; "JEI" `:30, :128` | Working changelog breaks the bullet rules | Rewrite those bullets; old-value form after G12, JEI after G17 | med | Fixed 2026-11-17 except "JEI" (waits on G17) |
| V-61 | DR-P16 | `unreleased.md:45` ("Create Confectionery"), `:145` ("Create Deco's"); 42 writeup uses of "Brewin' and Chewin'" | Mod display names not in the pack form | Fix unreleased.md and the writeups (deployed files frozen) | low | Fixed 2026-11-17 (Brewin' & Chewin' in 67 places; "Create Confectionery" is the mod's own display name, kept) |
| V-62 | DR-W11, DR-W12, DR-W16 | `briefs/random_tick_audit.md` (an investigation in briefs/); run docs `scratch_queue_run_*.md` against `scratch_run_*.md`; skill `integration-report` model files (`unusual_delight.md`, `more_delight.md`, `hostile_delight.md`) do not match its layout; 15 stale `configs_edited` backups (real value drift: `creativecompat-common.toml`, `jei/*sort-order.ini`, `waila/plugin_toggle.json5`), `config/waila/waila.json5` edited with no backup, orphan `configs_edited/root_config/salt-common.toml`; `name_audit.py` output OUT OF DATE | Small doc and backup hygiene | Move or rename; make `barbeques_delight.md` the model; refresh backups; rerun `name_audit.py` | low | Fixed 2026-11-17 (V-62a to d; name_audit.py rerun not done) |

## 10. Unwritten conventions found (proposed rules)

Practices the code and docs follow consistently but no doc states, found by the 2026-11-14 audit. Each is Proposed
until the owner confirms, edits or drops it; a confirmed one moves into its section above with H = Hard. Ids continue
each area's numbering. Counts are from 2026-11-14.

### 10.1 Docs and writeups

| Id | Proposed rule | Evidence | Exceptions |
|---|---|---|---|
| DR-W17 | Title line `# <Name>: <subtitle>`; generated docs add "(generated)"; jar-upgrade reports `# <Mod> <new> against the installed <old>` | 126 of 210 titles; 9 jar-upgrade titles | 75 others (for example `bee_nests.md`, `dyeing.md`) |
| DR-W18 | Sections `## N. Title`, subsections `### N.M Title` (no dot after N.M); a letter suffix (3a) for an inserted section | 1,580 and 1,455 uses, 0 with a trailing dot | Letter suffixes in `baby_mobs.md`, `creative_worldgen.md` |
| DR-W19 | At a glance is a table `| Part | Status |` (integration reports: `| Part | Where | Status |`) | 74 of about 105 | About 30 variants |
| DR-W20 | A dated heading puts the date in trailing brackets, optionally with a status word: `### 3.1 Salvaging (BUILT 2026-10-25)` | About 540 dated headings | Scratch queue sections use ", YYYY-MM-DD" (58) |
| DR-W21 | Cross-references are "section N" or a link plus a bare number (`[x.md](x.md) 3.6`); never § | 951 and 584 uses | § 418 times in 5 files (`crops.md`, `vintage_delight.md`, `veggies_delight.md`) |
| DR-W22 | Test id ranges read "XX-01 to XX-04" or "XX-01 to -04" | 647 and 383 uses | ".." 79 times |
| DR-W23 | History is a bullet list `- YYYY-MM-DD: text`, oldest first, appended at the bottom | 117 files use the form, 87 oldest first | Bold dates in 13 files; order mixed in 21 (G18) |
| DR-W24 | A superseded rule keeps its old text: `**Superseded YYYY-MM-DD (who, where):** new rule. Original rule, kept as the record: ...`, or the old text struck through | "Superseded" 61 times in 32 files | Short "(superseded)" notes in most files |
| DR-W25 | Translating the scratchpad adds an unnumbered `## Owner notes (scratchpad YYYY-MM-DD)` at the end of the writeup, each note citing its TODO row | 31 files | 5 numbered |
| DR-W26 | Each writeup numbers its own items: decisions D1.., findings F1.., upstream bugs B1.., owner notes N<batch>-<n>, cited by that id | D 66 files, F 77, B 33 | R1.. and G1.. in a few |
| DR-W27 | A test result goes in brackets after the bold name: `(scratch server PASSED YYYY-MM-DD, SQ-XX-NN)`; tier tags `[scratch]` / `[game]` in the name | 161 and 222 lines | Many suites keep results only in History |
| DR-W28 | Owner decisions are asked in a table `| # | Question | Recommendation | Default if not answered |` | 7 files (for example `hostile_delight.md`, `barbeques_delight.md`) | Older reports use prose lists |
| DR-W29 | Config keys are written in backticks with their section: `` `[worldgen] key` `` | 798 uses | |
| DR-W30 | Prose wraps at about 120 characters; table rows are never wrapped | 91% of 67k prose lines | 6,207 longer, mostly in 3 plan files |
| DR-W31 | Briefs: title `# Brief: <family> / <sub>`, "Written YYYY-MM-DD from [TODO.md] ...", `## Scope and order`, then `## <sub> #n: <change>` with Goal / Now / Build / Ids and data / Decisions / Gotchas / Tests | 48 of 98 titles; layout in `briefs/README.md` | 50 other titles; 11 without Goal |
| DR-W32 | A generated reference's index status reads "reference, regenerate with `python3 tools/<script>.py`"; a new run result starts the cell in bold `**rNNNN YYYY-MM-DD: ...**` | 22 and 27 rows | Some give a date instead |
| DR-W33 | Brief file names keep the TODO sub as written, hyphens included (`briefs/baking_baked-goods.md`); run docs are `testing/scratch_run_<date>[b].md`; structure logs `NN_<name>.scratch.md`; split notes `split/<Letter>_<topic>.md` | All briefs; 20 run docs | 2 `scratch_queue_run_` docs |
| DR-L7 | Numbers: "2 to 5" in changelogs and new prose ("2-5" tolerated in tables); percent with no space ("75%"); multipliers "0.85x" | Changelog 35 to 3, 156 to 21, 39 to 0 | `1.2.00.md:89` "2-3"; "N %" 13 times |
| DR-L8 | Owner decisions in comments, scripts and docs cite "(owner, YYYY-MM-DD)" and TODO row ids `family/sub #n` | 55 in scripts, 130 script files with row ids, 461 Java files citing a writeup | 34 script citations miss the comma or brackets |
| DR-P17 | Changelog family headings carry an icon comment `## Family <!-- icon: ns:item -->`; catch-all headings ("Food & Cooking", "Interface", "Commands") have none | Every family heading in 1.2.02.md; 15 of 18 in unreleased.md | |
| DR-P18 | Every changelog bullet ends with a period | unreleased.md 98 of 98 | 1.2.02.md 80 of 149, 1.2.00.md 16 of 342 (frozen) |

### 10.2 Java and build

| Id | Proposed rule | Evidence | Exceptions |
|---|---|---|---|
| DR-C28 | Each jar's code is in `com.SaintAlfonzo.<modid>`; the root package holds only the main class `<ModName>.java`, the config class and the mixin plugin; one subpackage per feature, plus `mixin/` and `compat/` | 12 of 12 main jars | Mixin plugin in `compat/` (4 jars) or `perf/` (Common); a few root helpers (`CEQTiers`, `CWGResources`) |
| DR-C29 | Members and ARR rewrites live in `com.SaintAlfonzo.<bundle>.<member>`; ports of permissive or GPL code keep the upstream package | golden, roll, largemeals, oceanic, hearth, barbeques, rustic; upstream packages in 9 ports | miners (V-14); `baby_mobs`, `more_mooblooms` |
| DR-C30 | The mod id constant is `MOD_ID`; `NS = MOD_ID` names the namespace of every registered id | 9 main classes, `NS` in 6 | `MODID` in Common, Engineering and the ports; `ID` in 6 |
| DR-C31 | One COMMON config per jar, `<modid>-common.toml`, named explicitly in `registerConfig`; spec built in `static {}`; each section a nested `public static final class` that `push`es, then `pop(); // <section>` | 14 explicit names; 13 of 24 config classes; 92 of 152 pops commented | Default names in 5 small mods |
| DR-C32 | Config keys lowerCamelCase, sections lowercase (snake_case when compound); every value has a `comment(...)` in full sentences, ending "Restart to apply." when it needs one | 453 to 0 keys; 678 comments for 572 values | 6 camelCase sections |
| DR-C33 | Config moved between jars goes through Common's `swap.ConfigRename` (`carryOver` / `carrySections`), called in the constructor before `registerConfig` | 15 files | V-07; ExposureExtras (no Common dependency) |
| DR-C34 | Mixins live in `<base>.mixin` (CD: `mixin/<target modid>/`), are named `<Target><Purpose>Mixin` / `Accessor` / `Invoker`, use SRG names with `remap = false` and no refmap; each jar has `<modid>.mixins.json` (repacks `<modid>.ce.mixins.json`) with a plugin extending `GatedMixinPlugin` | 455 of 455 in `mixin/`; 441 `remap = false` | Labels and Raspberry Pie use Mojang names and a refmap; member configs without a plugin |
| DR-C35 | Optional mods are gated three ways: mixins by the plugin's `NEEDS_MOD` table and a `MOD_RANGE` pinned to the tested minor version; Java by `ModList.isLoaded` before loading a separate class; resources by a `// requires: <modid>` header. In mods.toml the mod is `mandatory=false` with a comment naming what uses it | `CommonMixinPlugin.java:89-100`, `CompatMixinPlugin.java:133`, `CreativeDevtools.java:47` | `Class.forName` reflection in Compat and Worldgen |
| DR-C36 | Event handlers are registered from the main class constructor (`addListener`, `EVENT_BUS.register`), each block headed by a comment naming the feature and its config; `@Mod.EventBusSubscriber` is the exception and names its own jar via the constant | 255 explicit, 40 subscriber files | Subscriber modid as a literal |
| DR-C37 | Logging via `LogUtils.getLogger()`; every message starts with a bracketed tag `[<modid>]` or `[<feature>]`; Worldgen uses named loggers `creativeworldgen/<area>` | 129 files | V-10; Raspberry Pie untagged; 12 files use `LoggerFactory` |
| DR-C38 | Every class has a javadoc saying what it does and why, citing the writeup section and TODO row; no license header in source files | About 1,355 of 1,374; 461 cite a `.md` | About 19 without; 2 with a copyright header |
| DR-C39 | One `SimpleChannel` per feature, named `<modid>:<feature>` (or `<modid>:main`), with a `PROTOCOL` string and exact-match predicates, defined in the feature's class | 5 channels | `TradeSync` (`acceptMissingOr`, on purpose) |
| DR-C40 | Saved names (NBT keys, saved-data names, lang keys) keep their original `creativeengineering` prefix when code moves jars, in a named `private static final String`; registered ids move and get a swap entry | `MonsterBoxVault.java:69-75`, `SeasonClock.java:36`; about 40 lang keys | |
| DR-C41 | Swap lists are generated by `tools/owner_namespace_ids.py` into `data/<modid>/swaps/owner_namespace.json` (`_comment` names the generator and "Never prune"); hand lists sit beside it, one file per reason | 6 jars; 18 hand lists in CD | V-06 |
| DR-C42 | Offline tests live in `tools/<feature>_test/` (plain `javac` or Rhino harness, Mojmap, stubs first on the classpath), run by `tools/test_<feature>.sh` sourcing `tools/java_test_cp.sh`; no mod has a `src/test` | 20 test folders, 14 scripts | Empty upstream `exposureBackport/src/test` |
| DR-C43 | License files sit in the mod folder: GPL text as `LICENSE.txt` (own) or `LICENSE` (ports), upstream terms as `NOTICE-upstream-<licence>.txt`; `ce.forge-mod` copies them into the jar and fails a GPL build without one; `mod_license` is SPDX, "upstream; overlay" for repacks | 34 + 10 license files; `ce.forge-mod.gradle:235-255` | V-13 |
| DR-C44 | ARR donors ship only our code: upstream art and data are read at runtime from the unmodified jar in `<game dir>/<member>_upstream/` through the shared `packs` / `upstream` modules, and a comment block at the top of `gradle.properties` states the license route | golden, roll, largemeals, miners, oceanic, tempad | |

### 10.3 KubeJS scripts

| Id | Proposed rule | Evidence | Exceptions |
|---|---|---|---|
| DR-C45 | Inside a jar tree, members are checked with `global.<JAR>.on(id)`; other mods with `Platform.isLoaded` or `global.has('mod')`, items with `global.has(i.x)` | 144 + 97 + 11 uses | V-48 |
| DR-C46 | Every script opens with a comment header saying what it does and citing its writeup (`<doc>.md <section>`) | 660 of 665; 466 cite a `.md` | 5 old pack-folder scripts |
| DR-C47 | Every file in a jar's script pack starts with `// requires: <own modid>`, plus one `requires` per optional mod it needs; `// priority:` comes first. Standard priorities: 1000 dicts and cores, 900 generated parts, 10 late rows, -100 merges, -10000 removed items | 423 of 426 pack files; 246 priority lines | Common's 12 (always present); 48 put `requires` first |
| DR-C48 | Short aliases bound inside the event callback: `i` item, `R` NCH, `t` tags, `f` fluid, `b` blocks, `ef` effect, `m` misc, `fd` food | 521 inside callbacks | 127 top-level in 59 files; `var item` in 3 |
| DR-C49 | Each Creative jar has one UPPERCASE core global (`global.CD`, `CB`, `CAL`, `CEQ`, `CWG`, `CMG`, `CDV`, `CEN`) in its core script at priority 1000, holding `modId`, `on(member)` and `recipes(fn)`; each server and client pack has a core script that logs an error if the global is missing; other scripts return early without it | 8 jars; 258 CD guards | Compat has none (V-25); core file names vary |
| DR-C50 | Helper and framework globals are camelCase nouns (`global.tooltips`, `trades`, `armorEffects`); only jar cores and engines (`NCH`, `LOOT`) are UPPERCASE | 54 to 12 | 2 UPPER constants |
| DR-C51 | Java classes are bound `var $Name = Java.loadClass(...)`, with a feature prefix at top level (`$CDConfig`) | 171 of 236 | 65 unprefixed |
| DR-C52 | A script with top-level helpers wraps itself in `;(function () { ... })()` and says why (one shared top-level scope) | 86 files | 54 top-level functions (V-40) |
| DR-C53 | Logging `console.<level>('[<module>] message')`; `warn` and `error` only for real problems; no start and finish banners | 234 bracketed calls | 154 others, mostly banners in old helpers |
| DR-C54 | A moved script's header says "Moved from `<old path>` `<date>` (`<doc ref>`), rows unchanged"; the old pack-folder file stays as a one-comment stub pointing to the new home | 245 headers; 5 stubs | Stale stub paths |
| DR-C55 | Generated scripts start `// Generated by tools/<gen>.py ...; do not edit, rerun the generator.` and are never hand-edited; hand rows go in a sibling file that loads later | 155 files | One on line 3 |
| DR-C56 | Recipe id overrides are snake_case suffixes, optional `<family>/` group, `_manual_only` for hand-only routes; NCH adds the namespace and type | 130 `_manual_only` | |
| DR-C57 | Removals sit next to their replacement: `R.remove({ id: ... }, e)` with a one-line why, then the new recipe in the same block; removed items are pushed to `global.removedItems` from the owning member's startup folder | `item_merges.js:25-32`; 13 pushes | |
| DR-C58 | Rhino style: `var` by default, arrow callbacks with the event named `e`, file and folder names snake_case, one folder per mod id in the CD and CB trees, a leading `_` for non-mod folders (`_core`, `_qol`, `_lib`) | 3,639 `var` to 866 `let`/`const`; 459 arrow callbacks to 34 | `neoCraftHelper.js`; `function (event)` in worldgen |

## 11. Sources

- [CLAUDE.md](../CLAUDE.md): Glossary, Preferences, Build and verify, Done means
- [README.md](README.md): intro, Status legend, Layout, Reporting test results, Keeping them current
- [changelog.md](changelog.md) 3.2 and 3.6
- [names.md](names.md) 11 and 12; [tooltips.md](tooltips.md) 2 and 3
- [texture_sources.md](texture_sources.md) 1
- [creative_split.md](creative_split.md) 3 (rules 1 to 11)
- [pack_hygiene.md](pack_hygiene.md) 1 (rules checked, groups A to K)
- [advancements.md](advancements.md); [creative_brewing.md](creative_brewing.md) 2; [creative_delight.md](creative_delight.md) 9, 29
- [todo/TODO.md](todo/TODO.md) header; [testing/scratch_queue.md](testing/scratch_queue.md) header
- Skills: `.claude/skills/` `integration-report`, `port`, `release`, `scratch-test` (and its gotchas.md), `add-mod`
- Tools: `tools/changelog_lint.py`, `tools/name_audit.py`, `tools/id_owner_lint.py`, `tools/texture_provenance.py`
- Claude memory (feedback entries): no-emojis-in-files, feature-writeup-pattern, todo-layout, links-not-filepaths,
  no-published-artifacts, memory-vs-docs-policy, creative-mod-naming, ids-follow-owning-mod, item-dict-mechanics,
  creative-delight-routing, license-gplv3, async-locator-everywhere, confirm-vague-wide-scope, token-economy-habits,
  forge-shared-asset-path, resource-pack-lang-priority
- Code survey (2026-11-13): indentation and line length counted over tracked `development/**/*.java` and `*.js`
- Audit (2026-11-14): `tools/design_rules_audit.py`, the four rule tools, and three read-only review passes (Java and build, KubeJS, docs)

## 12. History

- 2026-11-13: created. Existing rules collected from the sources in section 8; formatting observed from the code;
  gaps G1 to G10 listed for the owner.
- 2026-11-14: compliance audit of the whole repo. Sections 8 (result per rule), 9 (violations V-01 to V-62, all
  Open) and 10 (proposed rules from unwritten conventions) added; gaps G11 to G20 added; DR-W7 columns and DR-C19
  corrected to match the files; `tools/design_rules_audit.py` added (report only).
- 2026-11-16: G2, G3, G5, G11, G12, G13 and G20 decided in [plan_rules_fixes.md](plan_rules_fixes.md) section 0; DR-W3
  points to the new [testing/test_prefix_index.md](testing/test_prefix_index.md).
- 2026-11-16: G1 (American spelling) and G19 (`->`) decided, plan D20, D21.
- 2026-11-16: G16 decided (recipe unlocks removed entirely, plan D23); every V-id scheduled in plan_rules_fixes.md.
- 2026-11-16: P0 rule text (plan_rules_fixes.md): DR-L4b (ISO dates), DR-L7 (American spelling and `->`; the plan called it DR-L6, which already names in-game names), DR-W3b (prefix index first), DR-W3 test line form (D12), DR-L5 owner only (D9), DR-P4 brackets (D10), DR-C14 no recipe unlocks (D23), DR-C19 `GPL-3.0-only` and NOTICE names (D1), section 5.5 rewritten as rules DR-C21 to DR-C26 (D3, D7, D8); G1, G2, G3, G11, G12, G16, G19 closed; `.editorconfig` added.
- 2026-11-17: P1 of plan_rules_fixes.md: V-12, V-13, V-17, V-49, V-52, V-59 (docs), V-60, V-61, V-62 fixed; `tools/test_prefix_lint.py` (DR-W3b), `tools/iso_dates.py` (DR-L4b), `tools/us_spelling.py` (DR-L7) and the NOTICE-name and `client_mods/` checks added to the audit; DR-W12 names the two integration-report layouts.
