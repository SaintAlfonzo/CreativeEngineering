# Advancements: switched off pack-wide

The pack loads no advancements at all: vanilla, every mod and every datapack. One CEConfig switch, read by one mixin.
Test prefix `AV-`. The decision and the list of lost rewards were first written up in [alcohol.md](alcohol.md) 10.2;
this file is the reference for the switch.

## 0. At a glance

| Part | Status |
|---|---|
| `[advancements] enabled = false` + `ServerAdvancementManagerMixin` | BUILT 2026-10-18 in patcherMod 1.0.8; DEPLOYED with 1.0.8 (2026-10-19 10:20). Ran in single player 2026-10-19 00:57: "14828 advancements left out", 931 "Ignored advancement" lines for the saved progress (6) |
| Vinery `vintage_perfection` left out of the upstream layer | BUILT in the Vinery port `-02` ([alcohol.md](alcohol.md) 10.2) |
| Replacement sources for lost rewards | Vinery banner: crafting recipe ([alcohol.md](alcohol.md) 10.5). The rest have none (3.2) |
| In-game tests | AV-01 to AV-05 pending |

- **Files to open:** `development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/ServerAdvancementManagerMixin.java`,
  `CEConfig.Advancements`.
- **Knob:** `config/creativeengineering-common.toml` `[advancements] enabled` (default `false`). Read when data loads:
  a change applies on the next `/reload` or world start.
- **How to measure:** `latest.log` line `[advancements] N advancements left out`; the advancement screen (L) is empty.

## 1. Overview

Vanilla builds its advancement tree from every `data/*/advancements/*.json` in the loaded data packs, in
`ServerAdvancementManager.apply` (`m_5787_`). The mixin swaps the JSON map for an empty one at the head of that method
when the switch is off. The tree is then empty, so nothing can show, toast, be awarded or hand out a reward.

The user asked for this on 2026-10-18 ("rip out every advancement"). The pack has no vanilla recipe book either, so
the recipe-unlock advancements did nothing useful.

## 2. Reference

| Case | What happens with the switch off |
|---|---|
| Advancement screen (L) | empty |
| Toasts, chat announcements | none (advancement toasts are also filtered on the client by `[toasts] advancement`, [toasts.md](toasts.md)) |
| `/advancement grant ...` | fails: the id doesn't exist |
| Mod code awarding a criterion | finds no advancement; Brewin's "temperature block near a keg" award checks for null ([alcohol.md](alcohol.md) 10.2) |
| FTB Quests | no quest task uses an advancement (checked 2026-10-18) |
| A player's saved progress (`world/advancements/<uuid>.json`) | vanilla logs `Ignored advancement '...' in progress file ... - it doesn't exist anymore?` per entry and keeps only known ones; the file is rewritten without them on the next save (3.1) |
| `/reload` with the switch changed | the tree is rebuilt from the full map (on) or emptied (off) |

## 3. What the pack changes

### 3.1 One-way effect on existing worlds

Saved progress isn't converted or kept aside. Once a player has joined and their advancement file has been saved
with the switch off, turning it back on starts them from nothing. Stats (`world/stats/`) are separate and untouched.
On the live server this happens the first time each player joins on 1.0.8. A copy of `world/advancements/` taken
before the update is the only way back.

### 3.2 Rewards lost with them

| Reward | From | Other source |
|---|---|---|
| Vinery banner x2 | `vinery:main/wine_somelier` | crafting recipe since 2026-10-18 ([alcohol.md](alcohol.md) 10.5) |
| Lootr trophy | `lootr:100loot` | none |
| MCA books (romance, family, infection, death, blueprint, rose gold, supporters) | MCA advancements | some in MCA villager trades / loot |
| Yuushya guide book on first join | `yuushya:grant_book_on_join` | crafting |
| IE rare / masterwork shader grab bags; IP coker reward | IE / IP advancements | IE loot chests |
| Lightman's Currency chocolate coins (event) | LC event advancements | none |

Pack rule from now on (user, 2026-10-18): don't design features that grant items, unlock recipes or detect
progress through advancements. Use FTB Quests, loot or code.

## 4. Limits and decisions

- **Whole map, not a filter.** A per-namespace or per-id filter was not asked for. The mixin is the place to add one
  if a mod ever needs its advancements back.
- **Saved progress is not preserved** (3.1). Keeping it would need a mixin on `PlayerAdvancements` to carry unknown
  entries through a save. Not built: the user's call was to remove advancements, not pause them.
- **Rejected: datapack overrides** (an empty JSON per advancement). That is 14,828 files to keep in step with every mod
  update, and a missing one would still load.

## 5. File map

- [ServerAdvancementManagerMixin.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/ServerAdvancementManagerMixin.java)
- [CEConfig.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CEConfig.java) `Advancements`
- Vinery port: `UpstreamTranslator` (`vintage_perfection` dropped), [alcohol.md](alcohol.md) 10.2
- Banner recipe: [alcohol.js](../kubejs/server_scripts/cooking/alcohol.js), [alcohol.md](alcohol.md) 10.5

## 6. Offline checks

- `grep "advancements left out" logs/latest.log` after a world start: one line with the count. 2026-10-19 00:57 single
  player: 14,828.
- `grep -c "Ignored advancement" logs/latest.log`: the saved progress entries dropped for the joining player (931 on
  2026-10-19).

## 7. In-game test suite

- [ ] **AV-01 Empty screen:** open the advancement screen (L) → no tabs, no advancements.
- [ ] **AV-02 No toasts:** craft a crafting table, pick up a log, enter the Nether → no toast, no chat line.
- [ ] **AV-03 Grant fails:** `/advancement grant @s only minecraft:story/root` → error, unknown advancement.
- [ ] **AV-04 Switch back on:** set `[advancements] enabled = true`, `/reload` → the screen fills again (progress from
  before the switch is gone, 3.1); set it back to `false`, `/reload` → empty again.
- [ ] **AV-05 Dedicated server:** first join on the updated server → `latest.log` has the "left out" line once per
  data load and "Ignored advancement" lines for the joining player; no error.

## 8. History

- 2026-10-18: built in patcherMod (1.0.8 source) at the user's request, with the Vinery `vintage_perfection` drop and
  the banner crafting recipe ([alcohol.md](alcohol.md) 10.2, 10.5).
- 2026-10-19: this writeup. Single-player evidence from the 00:57 session added (6). STAGED in pack 1.0.8. AV-01 to
  AV-05 added.
- 2026-10-19: DEPLOYED with pack 1.0.8 (10:20, server + clients); the first-boot log check on the live server is TODO `pack/release #3`.
- 2026-10-19: advancement toasts also filtered client-side by `ToastFilterMixin` ([toasts.md](toasts.md)), BUILT.
