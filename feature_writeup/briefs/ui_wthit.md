# ui / wthit: implementation brief

Written 2026-10-24 from [TODO.md](../todo/TODO.md) `# ui`, Sub `wthit` (row 11 from the scratchpad of 2026-10-24; rows 1 to
10 predate this brief). Layout per [README.md](README.md). Feature writeup: [wthit_lines.md](../wthit_lines.md) (test
prefix `WL-`, last id WL-33). The pack's WTHIT code is patcherMod `compat/Wthit*.java`, registered in
`waila_plugins.json`.

## Scope and order

One new row, `wthit #11`.

---

## wthit #11: Silk Touch required icon
Status **Testing** (built 2026-10-28, [wthit_lines.md](../wthit_lines.md) 2.11; rows #9 and #10 built the same day,
sections 2.13 and 2.12) / Work patcherMod / TODO `ui/wthit #11`

Outcome: option (b), the generated tag. The goal's "ore blocks that drop other items" wins over the proposed test below:
iron ore needs Silk Touch to drop itself, so it shows the line. The tests are WL-37 and WL-38 (WL-34 went to the
sheared grass line).

### Goal
WTHIT shows a Silk Touch icon on blocks that only drop themselves with Silk Touch (glass, ice, ore blocks that drop
other items, grass blocks, ...).

### Now
- Plugins to copy from: `WthitBlockModelIcon` (a client icon provider), `WthitStateLines` (lines from
  `assets/creativeengineering/state_lines.json`), `WthitCampfire`, `WthitHive` (server data via `appendServerData`).
- Whether WTHIT's own harvest-tool plugin already marks Silk Touch is unverified (it shows the tool and level icon).
- Loot tables are server data; the client cannot read them. Block tags are synced to clients.

### Build
1. Decide the source of truth: (a) server provider reads the block's loot table (`LootTables`, look for the entry that
   gives the block's own item behind a `match_tool` silk touch predicate and no non-silk entry for it) and sends a
   boolean in `appendServerData`; or (b) a generator tool (`tools/*.py`, in `generators.json`, run with `/cdevgen`) reads
   `kubejs/exported/loot_tables/` and writes a block tag `creativeengineering:needs_silk_touch` through `global.tags`,
   which the client plugin reads.
2. Client component: an icon line "Silk Touch" with the enchanted-book or Silk Touch book item icon next to the name, in a
   new plugin class registered in `waila_plugins.json`; config switch in `CEConfig`.
3. Hide it when the player's tool already has Silk Touch (read the held item, client side).

### Ids and data
Tag name above is proposed. The loot dump (`kubejs/exported/loot_tables/<mod>.json`, refreshed by `/dumpall`) supplies the loot tables.

### Decisions
Runtime loot-table scan (always right, server cost on look) or generated tag (cheap, stale after a mod change). Recommend
the generated tag with a `/cdevgen` module, matching how the pack's other block lists are kept.

### Gotchas
Blocks with several loot pools (leaves: shears or Silk Touch; ores: Fortune) need a rule: show only when no non-Silk-Touch
path gives the block itself. Modded blocks with loot modifiers (global loot modifiers) change drops outside the table.

### Tests (WL-)
- WL-34 (proposed): look at glass, ice, a grass block and an iron ore: glass, ice and grass show the icon, iron ore does not;
  holding a Silk Touch pickaxe hides it.
