# todo-layout

TODO rows as data since 2026-11-16 (plan_process_overhaul.md P3.5, rulings D13, D16, D17). The layout history before
that (families, regroups, the archive table) is in [todo/TODO_history.md](../todo/TODO_history.md) and git history.

- **Rows:** `feature_writeup/todo/rows/<category>.csv`, one file per `#` category (22), plus `archive.csv` for rows
  archived before 2026-11-16 without a family. Columns: id, family (the `##` heading), table, status, priority, tests,
  work, change, details, decisions, writeup, feature (features.csv key), plan (`plan_x.md`), source, queue (SQ-/CQ- ids),
  log (dated entries, newest first), updated. Library `tools/todo_rows.py`; schema check `todo.py lint` (check
  `todo_rows` in `check_all`).
- **Edits:** `tools/todo.py` only (`add --family`, `set ID field=value`, `log ID text`, `move ID --family`, `show`,
  `find`). It writes `updated` and the stable order (status rank Bug, Planned, Pinned, In Progress, Testing, Complete;
  then id). Then `python3 tools/status_index.py`.
- **TODO.md is generated** from `todo/layout.md` (headings and prose with `<?todo ...?>` placeholders) and the rows.
  A hand edit FAILs `status_index.py --check` (the first line carries the body's sha1). Prose changes go to layout.md;
  a new family is a new `## heading` plus `<?todo rows HEADING?>` there.
- **Ids (D17):** old ids (`family/sub #n`) never change. A new row takes its `##` family's slug and the next number
  (`salt #1`, `conventions #27`); `todo.py add` assigns it.
- **Details carry no status words.** "Built", "absent", run results go in `log` (`todo.py log`); the jar install
  state is generated per row from its feature's jars. `todo.py lint --changed` FAILs new Details text with them.
- **Priority:** high / normal / low (empty = normal). High rows are listed at the top of TODO.md.
- **Complete rows** stay in their category file; the archive section of TODO.md is a view. Open rows without a family
  (archived by mistake before 2026-11-16) are listed under the archive for the owner to refile.
- **Views from the same rows:** TODO.md, each writeup's and plan's `open_work` block (rows whose feature or Writeup
  cell points at the writeup, or whose `plan` names the plan), and the derived feature status (tools/feature_status.py).
- `todo/scratchpad.md` is the owner's inbox; intake turns notes into rows with `todo.py add --source`.
