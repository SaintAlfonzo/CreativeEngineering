---
name: intake
description: Turn owner notes (todo/scratchpad.md blocks, "owner notes" sections, scratch run findings) into a plan - split, classify, ask open items before building, record rulings. Use whenever owner notes become a plan_*.md or plan rows.
---

# Design intake

Why: owner decisions are collected before an agent builds, not after (plan_process_overhaul.md P1.3; review finding 6:
about 6 owner reversals in the 11-10 to 11-16 week). Measure: owner reversals per plan.

## 1. Split

Copy the notes into the plan's intake table, one item per line, quoting the owner's words. A bullet with two asks is
two items. Keep the owner's nesting as the item's context, not as extra items.

## 2. Classify every item

| Class | Meaning | Action |
|---|---|---|
| clear | one reasonable reading | build |
| open | two or more readings with different results | ask before building anything that depends on it |
| conflict | contradicts an earlier owner ruling (cite plan + row, or CLAUDE.md line) | ask |
| premise | assumes something about the current state ("X still does Y", "add Z" when Z may exist) | check the code, config and dumps first; a false premise becomes an open item with what you found |

Premise checks catch "already done" and "works differently than assumed". Grep the code, `kubejs/exported/` dumps and
`logs/latest.log`; cite file:line in the table.

## 3. Ask

- Every open and conflict item, before building anything that depends on it. AskUserQuestion, up to 4 questions per
  call, concrete options, the recommended one first with "(Recommended)".
- Each question stands alone: name the item and what you found; the owner may see only the dialog.
- Visual items (screens, books, HUD, textures, models, layout) get a preview: an ASCII layout, a screenshot of the
  current state (`tools/client_capture/run_captures.py`, client_capture.md), or a mock in the session scratchpad.
  Textures follow the CLAUDE.md texture rule: never drawn or generated; ask for source and palette.
- Items independent of the open ones may be built meanwhile.

## 4. Record

The plan gets an "Owner rulings" table:

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|

- Source: `AskUserQuestion 2026-11-16`, `chat 2026-11-16`, or the earlier plan row for a conflict.
- A rule the agent chose itself is written `agent default, unconfirmed` and listed in the hand-over. Building on an
  unconfirmed default is allowed only for *clear* items.
- Later owner changes edit the row and add a History line; never delete a ruling.

## 4b. TODO rows

Each item that becomes work is a TODO row, written with the tool, never into TODO.md (it is generated):
`python3 tools/todo.py add --family "<## family>" --change "..." --tests "S P" --source "scratchpad 2026-11-16"
--plan plan_x.md [--priority high]`, then `python3 tools/status_index.py`. An item that changes an existing row:
`todo.py set ID field=value` or `todo.py log ID "..."`. The plan lists the row ids; its Open work block is generated.

## 5. Look-check for UI and visual items

The first build of a screen, book, HUD element, texture use or model is a look-check: capture a screenshot with the client
capture and show it to the owner (a clickable workspace link) before writing test rows or polishing.

## 6. Mark the source

Mark the scratchpad block as taken in, on its heading line: `(intake 2026-11-16 -> plan_x.md)`, so it is not
processed twice. The "Prompt scratchpad, dont touch" section stays untouched.

## Hand-over

List: items built, items asked and their rulings, `agent default, unconfirmed` rows, items still waiting on the owner.
