# Creative split: audit inventory (sweeps A to G)

Written 2026-11-01 by the audit run of [creative_split.md](../creative_split.md) section 4. Read-only: no source file
was changed. One file per sweep; the merged summary (dependency graph, sizes, carve order, remainder check) is
creative_split.md section 7.

| Sweep | File | Covers |
|---|---|---|
| A | [A_java.md](A_java.md) | every non-mixin class in patcherMod |
| B | [B_mixins.md](B_mixins.md) | every entry in `creativeengineering.mixins.json` |
| C | [C_config.md](C_config.md) | every `CEConfig` section and its readers |
| D | [D_registration.md](D_registration.md) | `CreativeEngineering.java`, `DeferredRegister` holders, event subscribers, JEI and WTHIT entry points |
| E | [E_resources.md](E_resources.md) | patcherMod resources, `kubejs/data`, `kubejs/assets` |
| F | [F_pack_scripts.md](F_pack_scripts.md) | `kubejs/startup_scripts`, `server_scripts`, `client_scripts` and the globals that cross |
| G | [G_external_edges.md](G_external_edges.md) | CD, CB, own jars, generators, `mod_sides.md` |

## Target vocabulary (use these exact words in the `target` column)

`Engineering`, `Equipment`, `Worldgen`, `Compat`, `Calendar`, `Devtools`, `CD`, `CB`, `Common`, `pack`,
`Magic (Equipment)`, `Computers (Engineering)`. A doubtful row gets `?` after the target and a one-line note.

## Decisions applied in this audit (owner, 2026-11-01)

- Engine fixes and perf are `Common` (Q3 closed).
- The `startup_scripts/_lib` dicts are `Common` (jar `kubejs_startup`); perfBot and clientCapture are `Devtools` (Q4, Q5 closed).
- No source headers are written for Magic or Computers reservations; the marked lists go to the reservation docs.
- Interface code (JEI, WTHIT, tooltips, toasts, GUI plaque, armor bar) is `Common`, except a row that only shows one
  content jar's data, which goes with that jar.
- Fauna (mobs, spawn caps, MCA pathing, bee idle, raid glowing) is `Worldgen`. Troughs, babyMobs, moreMooblooms,
  incubationAdditions are `CD`.
