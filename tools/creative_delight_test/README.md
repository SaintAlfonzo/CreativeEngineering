# Creative Delight scratch test suite

Boilerplate KubeJS suite that runs the pack's existing cooking helper calls from Creative Delight's script trees,
through the helpers that moved into patcherMod's jar (feature_writeup/creative_delight.md 10). Scratch servers
only: never copy it into the pack's `kubejs/` and never ship a jar built with it. Queue entries: SQ-CDT in
[../../feature_writeup/testing/scratch_queue.md](../../feature_writeup/testing/scratch_queue.md); tests CDT- in
creative_delight.md 19.

## Layout (`suite/`, same paths as in the jar)

| File | Checks |
|---|---|
| `kubejs_startup/creativedelight/_cdtest/cdtest_startup.js` | Startup loader: helpers (patcherMod pack) and dicts (pack folder) exist before the CD pack; `cd_core.js` first by priority; compost helper callable at startup top level |
| `kubejs_startup/creativedelight/_cdtest/cdtest_startup_absent.js` | `// requires:` honoured by the startup loader |
| `data/creativedelight/kubejs/_cdtest/cdtest_cooking_mirror.js` | GENERATED: real single-line helper calls from `kubejs/server_scripts/cooking/`, unchanged, inside `global.CD.recipes()`; each recipe is the `creativedelight:` twin of the pack's `creativeengineering:` recipe |
| `data/creativedelight/kubejs/_cdtest/cdtest_namespace.js` | `ns()` / `withNamespace()` scoping, nesting, restore after an error; member switch; pack server globals before CD scripts |
| `data/creativedelight/kubejs/_cdtest/cdtest_requires_absent.js`, `cdtest_requires_present.js` | Compartment headers in the CD server tree |
| `data/creativedelight/kubejs/_cdtest/cdtest_tags.js` | `global.tags` from a CD script |
| `data/creativedelight/kubejs/_cdtest/cdtest_probe.js` | `/cdtest <startup / ns / mirror / requires / tags / config / all>`: `[cdtest] PASS/FAIL` lines, then `[cdtest] done` |
| `assets/creativedelight/kubejs/_cdtest/cdtest_client.js` | Client tree: a log line and a tooltip on the fishing net |

Outside `suite/` (scratch-only, not in any jar):

- `cdj_probe.js`: copy into the scratch `kubejs/server_scripts/` as `zz_cdj_probe.js`; `/cdjtest` checks the nested
  members' items, the fishing net block tags (SQ-FN-04) and End's knife-kill gristle (SQ-CDJ-01, -06). Needs
  `forceload add 400 400` first.
- `bench_boot.sh OUT ROUNDS`: alternates boots without CD (upstream member jars) and with the CD jar on one scratch
  world, one line per boot (seconds to `Done`, KubeJS script and recipe times).

The mirror is frozen as of 2026-10-25: the pack's cooking scripts moved into the CD tree itself (creative_delight.md 29),
so `server_scripts/cooking/` has no twins left to compare against and `--check` reports it stale by design. Do not
regenerate it; the file stays as the record of SQ-CDT-05's 57/57 pass. `python3 tools/creative_delight_test/gen_cdtest_mirror.py`
(`--check` exits 1 when stale; `--per-helper N`, default 2).

## Two ways to load it

- **Jar path** (the real one): `./gradlew :creativeDelight:build -Pcd_test_suite --offline` writes
  `development/creativeDelight/build/libs/creativedelight-<v>-cdtest.jar` (all three script types inside; not
  archived, `installToMods` refuses it, `build_deploy.py` refuses a jar holding `_cdtest/`). Copy that jar and the
  patcherMod jar from `development/patcherMod/build/libs/` into the scratch server's `mods/`, with the upstream
  `oceansdelight-*`, `oceanic_delight-*` and `ends_delight-*` jars removed.
- **Folder path** (the dev loop, 10.4): with the normal CD jar in the scratch `mods/`, copy `suite/data` and
  `suite/assets` into the scratch server's `kubejs/` (they land at `kubejs/data/creativedelight/kubejs/_cdtest/`
  etc.). The startup half cannot load from a folder there (only the jar or `startup_scripts/`), so `/cdtest startup`
  FAILs "suite ran" on this path by design; run the other parts.
