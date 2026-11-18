# Execution order for the open plans (2026-11-17)

Orchestrates the open rows of [plan_post_scratch_1115.md](plan_post_scratch_1115.md) (1115),
[plan_post_scratch_1115b.md](plan_post_scratch_1115b.md) (1115b), [plan_bugfix_1114.md](plan_bugfix_1114.md),
[plan_food_cooking_batch.md](plan_food_cooking_batch.md), [plan_farming_cooking_batch.md](plan_farming_cooking_batch.md),
[plan_meals_rolls_bbq.md](plan_meals_rolls_bbq.md), [plan_rules_fixes.md](plan_rules_fixes.md) and its follow-up.
Design and rulings stay in those plans; this file holds only lanes, order and file ownership. Status lives in the TODO rows.

## 0. Already done or test-only (no build)

- plan_rules_fixes.md and its follow-up: built through P7 and F1 to F7 (committed 2026-11-17). Left: `tooling/design-rules #2`
  (History audit, deferred low), `#3` (patcherMod audit: list written, no identifier left; close at the end), `#4` (palette
  demo off, release-time blocker, not now).
- plan_bugfix_1114.md: built 2026-11-14; `gameplay/tnt-and-portal #15` is fixed by 1115 A1 (Testing row `scratch-1115-follow-ups #1`).
- plan_food_cooking_batch.md: built; only `dishes/meals #40` (Pinned) is left (lane F proposals).
- plan_farming_cooking_batch.md: built except F2 and the two owner items, approved 2026-11-17 (lane M1b, lane F).
- 1115 `#2` AMD portal crash: needs launches on the owner's AMD client (bisect), owner only. `#16` heap spill: look in game, owner only.

## 1. Lanes (one jar has one owner at a time)

| Lane | Jar(s) | Work | Rows file |
|---|---|---|---|
| L1 | Compat | 1115b F1 to F4, G7.4 (walk-through tiers, layer 8 -> snow block, leather boots), 1115b #8, #9, #11 | PS15B.qb |
| L2 | Compat, Devtools | G7.5 all layers fall and stack (salt layers included), G7.9 bamboo leaves, G4c/G4e/G7.13 jump, fall, overlay, thorns, 1115b #7/#10 reload command, #16 | PS15B.qb |
| M1 | Common | G7.1 method (G8), G7.2 magma, G7.3, G7.10 snow by layer, G7.11 saltable tag, G7.15, G6-A freeze; 1115 #9 investigation; perf row queued | PS15.qb, MELT.qb |
| M1b | Common | farming F2 random-tick fixes rows 1 to 12, Floral Enchantment bushes stop ticking | FCK.qb |
| F | Creative Delight, instance kubejs | `dishes/meals #46` / `#57` skewer sequenced assembly, Supplementaries Doormat recipe; read-only proposals for `#72b`, `#58`, `#59`, `#40` | BBQ.qb, FCK.qb |
| M2 | Creative Delight, configs | G2 salt redesign, G7.6 Salt Melt stacks, G7.7 salt JEI, lick layer hooks removed (B5/B7/E4 lick part), 1115b #12, #13, G3 small fixes (tooltip, torches, dragon armor, Rare Ice) | PS15.qb, SALT.qb |
| R | solRaspberryPie | G5 / G7.12 | its own prefix |
| X | Power Grid gate, read-only elsewhere | 1115b #14 arm; 1115b #15 Upgrade Aquatic guard (find the owning jar, report before editing); 1115 #12, #14 investigations | PWG.qb |
| F2 | Creative Delight | food rows after the owner's answers | BBQ.qb, MRD.qb |

## 2. Order

- Wave 1 (parallel, disjoint jars): L1, M1, F, R, X.
- Wave 2: L2 after L1; M1b after M1; M2 after F (both Creative Delight); owner question round on F's proposals.
- Wave 3: F2 after M2 and the owner's answers. Close `tooling/design-rules #3`. Final status_index, commit, hand-over.

## 3. Rules for every lane

- Builds run under one lock: `flock <scratchpad>/gradle.lock ./gradlew :<mod>:build --offline`. Agents build; the
  orchestrator commits each finished lane and installs (`installToMods`, one at a time).
- The orchestrator alone runs `tools/todo.py`, `tools/status_index.py`, edits `status/features.csv` and commits. Agents
  return their TODO log lines and status changes.
- Agents write their own writeups, queue rows (only in the rows files above), and changelog bullets in
  `unreleased.md` (Edit after a fresh read; another lane may have added bullets).
- No scratch server, client or perf boot in this pass.

## History

- 2026-11-17: written; owner rulings: melting method (1115 G8), farming F2 approved with pack defaults, food rows
  `#72b`, `#58`, `#59`, `#40` come back as proposals for one question round.
