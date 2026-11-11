# Scratch run r1110, 2026-11-10

Runner: session creativeengineering2-49. Owner request: general queue from the top, with on-demand priority for the
session moving Creative Delight content to the `creativedelight` namespace (creativeengineering2-6d,
[plan_cd_namespace.md](../plan_cd_namespace.md)). Priority rows interrupt at the next row boundary.

## 0. Performance capture

Perf session `r1110`, opened 00:13 before the first boot. Report without `--prune` at the end.

## 1. Scope

- Taken first: fixture for SQ-CDNS, then SQ-CDNS rows as the namespace session releases jars.
- Then: general queue, resuming SQ-WO-14 to -37 ([scratch_run_2026-11-09.md](scratch_run_2026-11-09.md) 1). A Creative
  Worldgen jar in the move (`jar_archive/cdns_full/`) means Wise Oak waits until the move is installed or is run on the
  `mods/` jars; recorded when it starts.

## 2. Tools

`tools/scratch_query/cdns_fixture.js`, `cdns_fixture.sh` (fixture builder), `cdns_fixture_manifest.json` (holders),
`cdns_probe.py` (offline census, owner: namespace session), `cdns_step2_run.sh`, `cdns_boot_check.sh`, `cdns_full_run.sh`.
Scratch servers use COPIED jars so the namespace session can run Gradle during a boot. `installToMods` is held while a JVM is up.

## 3. Jars

| Set | Where | Used for |
|---|---|---|
| Old state | `mods/`: Common 11-09 23:00, Compat 17:28, Delight 11-09 00:08, Equipment 17:29 | Fixture, SQ-CDNS-05, control |
| Step 2 | `jar_archive/cdns_step2/`, Common repatched 00:37 | SQ-CDNS-01, 02, 04, 06, kubejs snapshot 00:15 |
| Full | `jar_archive/cdns_full/` 00:24, Common 00:37, Equipment from step 2 | SQ-CDNS-03, 05 source save, 07 to 12, current kubejs |

## 7. Run notes

- 00:13 RUNNER.md claimed. Fixture server on `mods/` jars only (NOSWAP), jars copied.
- 00:15 kubejs snapshot `perf/runs/r1110/kubejs_snapshot_0016/` (no `exported/`, no `generation_scripts/`) before the rewrite.
- 00:18 to 00:23 fixture built by `/cdnsfix` steps, 95 holders, none missing, 378 ids in chests, 12 ids have no item form.
  `perf/runs/r1110/fixture/world_pristine` (hash 76f86b3fd9d8bce0), baseline `fixture/baseline.json`.
- Plan 5.1 deviations: no Sophisticated Storage in the pack (Storage Delight drawer instead); mob holder is an iron golem
  because `spawn-animals=false` discards Animal subclasses silently; villager is `mca:male_villager`, filled after MCA's
  conversion, which loses a vanilla villager's inventory; `cdnsOn` has curios but lost head and offhand, `cdnsOff` the reverse.
- 00:25 to 00:32 first SQ-CDNS step 2 run INVALID: every boot crashed 2 to 3 s after `Done` with
  `IncompatibleClassChangeError: Level.m_186434_ must be Methodref constant`, on the dropped item tick. The runner did
  not hold after `Done`, so the crash read as a normal stop; fixed (25 s hold, crash check).
- 00:34 to 00:40 bisect: full set crashed, `mods/` control clean, repatched Common alone clean. Cause per the namespace
  session: `SwapCapabilityKeysMixin` on `CapabilityProvider`, superclass of `Level`; moved to `CapabilityDispatcher`, 00:37.
  Same patch added the missing `replaces` parse, so aliases exist now.
- 00:40 to 00:50 step 2 rerun, results in section 8.

## 8. Final verdicts

| ID | Result | Evidence |
|---|---|---|
| SQ-CDNS-01 | PASS | `perf/runs/r1110/cdns_step2_r2/`: 2 ids remapped, 286 aliases, load report clean, compare EQUAL |
| SQ-CDNS-02 | PASS (rescoped) | `/data merge` old id reads back `creativedelight:wooden_bucket`. `/give creativeengineering:wooden_bucket` is `Unknown item`; the namespace session took `/give` with an old id out of the row's scope (plan 3.1, 5.2). Owner to confirm |
| SQ-CDNS-03 | PASS | `cdns_full/`: contents in both boots, only `creativedelight:` capability keys |
| SQ-CDNS-04 | PASS | boots 2 and 3: no remap lines, compare EQUAL, load report clean |
| SQ-CDNS-05 | RECORD | Rollback of a step 2 save on `mods/` jars: every wooden bucket and wooden water bucket lost, no crash (`cdns_full/SQ-CDNS-05.cmp`) |
| SQ-CDNS-07 | FAIL | Missed reference: `[NCH] replaceItems[12] old: creativeengineering:canned_hamimelon does not exist`. With the namespace session |
| SQ-CDNS-08 | PASS | 376 items, 26 blocks, 1 block entity, 1 menu under `creativedelight`, none old; recipes clean; 118 of 126 moved tags present |
| SQ-CDNS-09 | PASS (row rewritten) | Compare EQUAL, players clean, no `unresolved`; 42 old-id strings left in undecoded nested and saved data. The namespace session rewrote the Pass text to accept undecoded stacks |
| SQ-CDNS-06 | PASS | `left missing`, boot completes, `unresolved creativeengineering:wooden_water_bucket: 20` |
| SQ-WO-14 | PASS | `perf/runs/r1110wo/`: sleeping at game 1285, awake at 1466. The exact `sleep_until` value was not printed |
| SQ-WO-26 | PASS | no gift at trust 65, gifts after trust 70 and 100 |
| SQ-WO-27 | BATCH-FAULT | 8 `drops none` all on the first tree, which went to sleep after 2 gifts; the batch does not replace a sleeping tree. The other 32 rolls give one id and a listed count, all four lists seen, 0 bad (`wo_judge.py gifts`; its exit 1 is not explained by those counts) |
| SQ-WO-28 | PASS | one gift, single id, last line |
| SQ-WO-29 | PASS | Item Z 299.09 (north), 301.74 (south), not inside the block |
| SQ-WO-30 | PASS | Item NBT holds a relationship with trust 80, no timer keys |
| SQ-WO-32 | OPEN (BATCH-FAULT) | `nope:bad` logged once, oak x3 seen, but the tree slept after 2 gifts, so the totem x3 stacks were never seen. Rerun with the tree replaced when it sleeps |
| SQ-WO-34 | BATCH-FAULT | no config reload line after the file edit, which the row defines as a batch fault, not a gift bug |
| SQ-WO-37 | PASS | strace: 0 lookup hosts, 105 DNS or inet control lines |

SQ-WO steps a2, a3, a4, a5, s totals: PASS 14 from `qrun`, 6 LOOK judged above, 3 FAIL re-judged above. Run on `mods/` jars, NOSWAP, copied jars. Step b (fresh normal world, SQ-WO-35, -36) still to run.

## 9. Stopped 01:47, resume point

Stopped under run default 10: every normal (group B) world crashes or hangs in chunk generation in `CrowEvents.nearGeneratedNest`, on the `mods/` jars (SQ-WO step b, crash `perf/runs/r1110/crow_crash/`) and on the full build (spawn prep hung at 21 percent, 01:19). Superflat rows are unaffected. TODO row in `creativedelight/members`.

| ID | Result | Evidence |
|---|---|---|
| SQ-CDNS-07 rerun | PASS (partial) | `cdns_fresh/`: `replaceItems canned` 0, `690 permanent alias(es)` on a fresh world, no moved id or tag under the old namespace in the log. Boot never reached Done |
| SQ-CDNS-10, -11 | BLOCKED | group B batch `batches/r1110/cdns_10_11.qb` never ran |
| SQ-WO-35 | BLOCKED | crash before the rows finished; `qrun` FAIL is the crash |
| SQ-WO-36 | NOT RUN | crash |

Crow fix built 2026-11-10 (plan_hearth_crow_swap.md: no spawn check scans any more; Delight and Common built, not installed). Once installed, SQ-CDNS-10, -11 and SQ-WO-35, -36 are unblocked; run SQ-HCS-01 first. Next: after a crow fix, rerun `cdns_fresh_run.sh`, `wo_run.sh OUT RES b`. SQ-WO-32 and -34 need batch fixes. Then the rest of the queue (superflat rows do not need the fix). SQ-CDNS-12, CQ-CDNS-01 are client rows.

## 10. On-demand boot after the stop, about 01:50 to 02:00

Namespace session's request, group A (superflat, unaffected by the crow crash), `cdns_fresh_run.sh`, full build with the 01:04 Common and Delight, results `perf/runs/r1110/cdns_fresh_a/`. Short targeted run, no perf session.

| ID | Result | Evidence |
|---|---|---|
| SQ-CDNS-07 | PASS | Reached `Done`; 690 aliases on a fresh world; `replaceItems canned` 0; no moved id or tag under the old namespace in the log. Normal-world boot still blocked by the crow crash |
| SQ-CDNS-10a to -10e | PASS | `qrun`, group A per the rewritten rows. Crafting by hand is in CQ-CDNS-01 |
| SQ-CDNS-11a | PASS | `qrun`. Supplementaries pouring is in CQ-CDNS-01 |
