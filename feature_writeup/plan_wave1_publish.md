# Plan: wave 1 publish (Creative Common 1.3.0, Creative Compat 1.0.0, Raspberry Pie 1.0.0)

<!-- SPDX-License-Identifier: GPL-3.0-only -->

Owner request (chat 2026-11-23): publish Creative Common, Creative Compat and Raspberry Pie; ask the decisions, plan
the rest. Starting point: [publish_readiness.md](publish_readiness.md) (audit 2026-11-22, rulings section 2a). Every
blocker and high finding from that audit has a fix built (rows in Testing). What is left: a few medium items, the new
Raspberry Pie items below, a clean-clone build, one test run on final jars, and release jars from a clean commit.

## 0. Owner rulings

<!-- generated:status_index begin open_work sha1=49d5aea43a13 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 8 open TODO rows (Testing 8), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creative-compat-publish #8` | Testing | S | Weathered Cardboard Box moves from Creative Compat to Creative Engineering before Compat 1.0.0: id creativeengineering:weathered_cardboard_box, Compat ids in saves swapped back, item dict key kept, texture exemption (W2) moves with it |
| `creative-compat-publish #9` | Testing | S | Squid and whales: squid and glow squid flee Alex's Mobs cachalot whales; the giant squid no longer flees whales and fights back when a whale attacks it or comes close (the flee radius config goes) |
| `food-tuning #66` | Testing | S | Drinks: hot cocoa, strong hot cocoa, Farmer's Respite coffee, long coffee and strong coffee, prickly pear juice, strawberry banana smoothie (and their families) carry the drink food type, so Raspberry Pie counts them as drinks |
| `food-tuning #67` | Testing | S | Jams are foods: every jam family member (Vintage jam jars, Fruits Delight jellies, Upgrade Aquatic mulberry jam bottle) stays out of the drink, no-diet and Diet Log excluded tags and counts in the Diet Log; the family stays its own (preserve/jam) |
| `raspberry-pie-publish #21` | Testing | C | Permanent buff icon keeps cycling diamond and effect while the buff is active: find and fix (rule unchanged: cycle until reached, effect alone while active) |
| `raspberry-pie-publish #22` | Testing | C | Steady text: Diet Log tagline 'No slipping; does not trigger plates, tripwires or dripleaf'; full effect description lists all three SteadyRules rules |
| `raspberry-pie-publish #23` | Testing | C | Hover taglines for the 19 ladder effects that have none: agent drafts, owner reviews the list before it is written |
| `raspberry-pie-publish #24` | Testing | S C | Dropping the top effect slot drops every slot: find and fix (a drop should lose only the highest active slot) |
<!-- generated:status_index end open_work -->

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| W1 | PR-CC-12: composter textures recolored with Quark and Architect's Palette plank palettes | open | Accept with credit: NOTICE names the palette sources; no texture change | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W2 | PR-CP-13: weathered cardboard box texture (ad-hoc script, owner-only redo) | open | Superseded by W16 (the box leaves Compat; the exemption follows it to Creative Engineering). Was: ship as is in Compat 1.0.0; dated exemption; stays on art_needed until the owner replaces it | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W3 | PR-CC-13: loading tips, 532 upstream, pack override removes 319 | open | Common's jar removes only the tips about content missing from 1.19.2; the pack override keeps the rest. Widened 2026-11-24 (AskUserQuestion): the 30 tips wrong in 1.19.2 also go in the jar | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W4 | `publish-readiness #3` clean-clone build | open | Before the wave 1 uploads | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W5 | Raspberry Pie low rows `#12`, `#14` to `#20` | open | All in 1.0.0 | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W6 | "permanant effects stop cycling between icon and diamond when enabled" | open | Keep the rule (cycle until reached, effect alone while active); the report is a bug to find (`raspberry-pie-publish #21`) | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W7 | "update steady effect description" | open | Tagline and full effect description both list the SteadyRules rules (`#22`) | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W8 | "all short effect descriptions" | open | "review missing": the agent drafts taglines for the 19 ladder effects without one, the owner reviews before they are written (`#23`). Reviewed 2026-11-23: drafts accepted, Poison Immunity keeps none, Fire Immunity names magma; taglines ship in the jar too (`gen_rp_ladder.py` writes both language files) | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W9 | "dropping top effect slot drops all, fix" | clear | Bug: a drop should lose only the highest active slot (`#24`) | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W10 | Test run timing | open | The executing session runs one scratch run of all wave 1 rows after every code phase is built and committed | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W11 | Common 1.2.0 uploaded jar | open | The archived jar is the uploaded file: sha1 `7d3bad9fdade48155030b2fa5637033280da3679` | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W12 | GitHub Support removal of the 15 old pull request refs | open | Uploads do not wait; the owner files the request in parallel | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W13 | "rp: dropping top effect slot drops all, fix" | clear | Same as W9 (`raspberry-pie-publish #24`); no new row | 2026-11-23 | scratchpad 2026-11-23 |
| W14 | "all jams should be foods" | premise | No jam is drink-tagged (2026-11-19 dump; all carry `ingredient`). Ruling: jams stay their own family; mulberry jam was suggested as a drink, but every jam is a food and goes in the Diet Log (`food-tuning #67`) | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W15 | "hot cocoa, coffes, prickly pear juice, strawberry banana smoothie, and all their families are drinks" | clear | Hot cocoa, strong hot cocoa, Respite coffee, long and strong coffee, prickly pear juice, strawberry banana smoothie get the drink food type; their families are drinks already (`food-tuning #66`) | 2026-11-23 | scratchpad 2026-11-23 |
| W16 | "move weathered cardboard box to creative engineering" | conflict | Undoes the carve move and W2's Compat exemption. Ruling: move before Compat 1.0.0; id `creativeengineering:weathered_cardboard_box`, Compat ids swapped back; the W2 exemption follows the box (`creative-compat-publish #8`) | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W17 | "all vanilla squid should flee whales, giant squid should fight" | conflict | Reverses the giant squid flee tuning (`mobs/tweaks #18`). Ruling: squid and glow squid flee cachalot whales (not orcas); the giant squid stops fleeing and fights back when a whale attacks it or comes close; flee radius config removed (`creative-compat-publish #9`) | 2026-11-23 | AskUserQuestion 2026-11-23 |

Earlier rulings this plan carries: publish_readiness.md 2a (D1 to D10, P1 to P7), release order Common -> Compat ->
Raspberry Pie (`creative-common-publish #9`), Raspberry Pie order polish -> isolation runs -> release jar
(`raspberry-pie-publish #1`, `#7`).

## 1. Coordination

- [plan_ready_now_1123.md](plan_ready_now_1123.md) is being executed by another session (2026-11-23). Its P3 owns
  `creative-compat-publish #7` (PR-CP-11, PR-CP-12) and PR-CC-08, PR-CC-27; its P4 the DR-C2 literals
  (`publish-readiness #6`). Those Common and Compat edits are in the tree, uncommitted. This plan does not repeat them.
- Only one `impl` session at a time (CLAUDE.md). Phases W-P1 onward start after plan_ready_now P3 and P4 are
  committed and that session has left `impl`. W-P0 and the drafting in W-P4 are docs only (`plan`).
- If plan_ready_now P3 does not rewrite creative_compat.md sections 2 to 5 (PR-CP-18), W-P2 does.

## 2. Phases

| Phase | Work | Rows | Owner | Agent type |
|---|---|---|---|---|
| W-P0 | Rulings, rows, ledger sha1 (W11), decisions on existing rows | this plan | this session | docs |
| W-P1 | Common: PR-CC-13 tip remove list (W3: classify the 319 pack removals, version-only subset into the jar's `loading_tips` pack); close PR-CC-12 (W1); rebuild Common | `creative-common-publish #15` | impl session | general-purpose |
| W-P2 | Compat: PR-CP-18 writeup sections 2 to 5 from the code (unless done in plan_ready_now P3); W2 exemption in design_rules.md and `texture_sources/sources.csv` note | `creative-compat-publish #6`, `#7` | impl or doc session | doc-pass |
| W-P3 | Raspberry Pie Java: `#24` drop bug, `#21` icon cycling bug, then `#12`, `#14` to `#19` (`#18`: neutral jar defaults, pack values to the pack config and `configs_edited/`) | `raspberry-pie-publish #12`, `#14` to `#19`, `#21`, `#24` | impl session | general-purpose |
| W-P3b | Compat: box move to Creative Engineering (`#8`, W16; also rebuilds Engineering) and the squid and whale AI (`#9`, W17) | `creative-compat-publish #8`, `#9` | impl session | general-purpose |
| W-P3c | Pack food data (script only, `/reload`): drink food types (`food-tuning #66`, W15), jams kept foods (`#67`, W14); refresh the food dump first | `food-tuning #66`, `#67` | impl session | general-purpose |
| W-P4 | Raspberry Pie text: `#22` Steady wording; `#23` draft the 19 taglines, AskUserQuestion review, then write; `#20` stale writeup lines | `#20`, `#22`, `#23` | plan session drafts, impl writes | doc-pass |
| W-P5 | Clean-clone build (W4): tracked manifest (file, version, sha1, source) and fetch script for every in-tree mod that needs donor or compile jars; verify by cloning into the scratchpad and building Common, Compat and Raspberry Pie offline from the fetched set | `publish-readiness #3` | impl session | general-purpose |
| W-P6 | Commit all wave 1 work; build Common, Compat, Raspberry Pie from the clean commit with checks on (`jarstamp` shows no `dirty`); install | `raspberry-pie-publish #7`, `creative-common-publish #7` | impl session | general-purpose |
| W-P7 | One scratch run on the W-P6 jars (W10): SQ/CQ-CC13, CQ-CMP, the open SQ/CQ-RPS rows (isolation), and the rows of every Testing row in the three families (SQ-CPP-05, SQ-CMP-08, -23 to -25, SQ-RPS-10 to -13, CQ-RPS-09, -12, and the new rows' tests). Fix run blockers in-run; a product fix sends that jar back to W-P6 | `creative-common-publish #8`, `raspberry-pie-publish #1`, `food-tuning #46` | test session | scratch-judge for judging |
| W-P8 | Release (skill `release`): tag `creativecommon-1.3.0` at the W-P6 commit, CurseForge file changelogs, ledger rows with each uploaded jar's sha1; Common, then Compat, then Raspberry Pie | `creative-common-publish #9`, `publish-readiness #4` | owner uploads, agent records | general-purpose |

Each phase: `tools/session_role.py`, rows logged and set to Testing with `tools/todo.py`, queue rows in
`testing/rows/<CODE>.qb`, writeup and changelog bullet (`tools/changelog_lint.py`), `tools/status_index.py`, commit by
path, push to `agent_staging`.

## 3. Owner actions

- File the GitHub Support request for the 15 pull request refs and cached views (W12, `publish-readiness #2`).
- Review the drafted taglines (W-P4): done 2026-11-23.
- Upload the jars in W-P8.
- Later, not blocking: the weathered cardboard box texture (W2).

## 4. History

- 2026-11-23: plan written; rulings W1 to W12.
- 2026-11-23: execution started. Another session held `impl` (owner: this session waits, docs only). W-P4 `#22`, `#23`
  written (Testing; the jar copy of the taglines is built in W-P6); W-P2 and `#20` handed to doc passes. Code phases
  W-P1, W-P3, W-P5 to W-P7 wait for the impl slot.
- 2026-11-23: scratchpad notes taken in (W13 to W17): rows `food-tuning #66`, `#67`, `creative-compat-publish #8`,
  `#9`; phases W-P3b and W-P3c; W2 superseded by W16; `mobs/tweaks #18` superseded by W17.
