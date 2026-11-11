# Plan: animal breeding item audit (with bees and flowers)

Status: EXECUTED 2026-11-10 (static pass; report [breeding_audit.md](breeding_audit.md); live check queued as SQ-BREED-01). Written 2026-11-10. Closes TODO `mobs/tweaks #14` (audit part) and `mobs/bees #1` (audit part). Overlaps `mobs/tweaks #13`
(mob food overhaul) and the animal feed note in [hearth_and_harvest.md](hearth_and_harvest.md) (N21). Bee brief:
[briefs/mobs_bees.md](briefs/mobs_bees.md). Result report: [breeding_audit.md](breeding_audit.md).

## Goal

1. For every breedable animal in the pack (vanilla and modded): the items that breed it, where that list comes from
   (hard-coded `isFood`, an item tag, a mod config), and whether the pack documents it (JEI, tags).
2. For bees: which items breed them, which blocks they pollinate, and which pack flowers sit outside `#minecraft:flowers`.
3. Findings and proposed fixes only. No tag or recipe change here; those are owner calls and go in the TODO.

## Method

The game is running, so nothing needing a restart or a jar swap is used. Offline only:

| Step | What | Source |
|---|---|---|
| 1 | Entity roster: every entity type with its mod, from the spawn dump and entity tags | `kubejs/exported/spawns.json`, `tags/entity_type_tags*.json` |
| 2 | Class hierarchy of every jar in `mods/`; keep classes descending from vanilla `Animal` (and `Bee`) | `tools/breeding_audit.py` (own class-file reader) |
| 3 | Per Animal class: the methods that decide food (`isFood` = `m_6898_`), the constants they touch (Items fields, tags, `Ingredient.of`), and the ctor/goal constants for temptation | same tool, `javap -c` on the hits |
| 4 | Item tags that name breeding or food sets (`*_breedables`, `*_food`, `*foodstuffs`, `snail_breeding_items`) | `tags/item_tags.json` |
| 5 | Bee flowers: `#minecraft:flowers` and `#small_flowers` / `#tall_flowers` members (item and block) against every pack block that is a flower by name or by tag | tag dumps, `items_by_mod` |
| 6 | Pack-side coverage: what scripts already touch these tags or add JEI info | grep `kubejs/` and `development/` |
| 7 | Report with a row per animal: breed items, source, notes, findings F-n; open items to TODO | `breeding_audit.md` |

Static analysis is approximate where food is computed (config lists, lambdas); those rows are marked `check in game` and
a scratch row is queued instead of run (SQ-BREED-01).

## Outputs

- `tools/breeding_audit.py`, generated table in [breeding_audit.md](breeding_audit.md).
- TODO rows `mobs/tweaks #14` and `mobs/bees #1` updated; scratch queue row for the live `/dumpbreeding` check.
- No changelog bullet (report only, no player-visible change).
