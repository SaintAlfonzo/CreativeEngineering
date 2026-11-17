# release-plans-1-0-3-to-1-0-6

> Release plans 1.0.3, 1.0.4, 1.0.5, 1.0.6 are feature_writeup/plan_*.md; which were deployed: feature_writeup/deploy.md

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Merged 2026-10-19 from 4 memories (index compaction).

## plan-1-0-3

The user asked for a plan only (no implementation) on 2026-09-30. Full design, tests and open questions live in `feature_writeup/plan_1.0.3.md`; indexed under "Release plans" in feature_writeup/README.md.

Key findings: the 465 MB payload is 429 MB of jars; TrashSlot per-screen state is client-local `TrashSlotSaveState.json` (T toggles, `.default.json` is copied in when absent); Loading Protection `protectionDuration` 60, cap 300.

SP done 2026-09-30: protectionDuration 150, backed up both configs_edited. Loading Protection ends protection once the player moves >0.1 block from the login spot, so 150 s is a ceiling. TS findings (plan 5.5): only InventoryScreen/CraftingScreen/chest are enabled by default, T toggles stick per client, backpack offset is from the panel centre so it breaks when the panel size changes (fix: default json with anchorX/Y=1.0, slotX=-28, slotY=0), no inventory slot in creative; handshake race ruled out. Decompile with ~/.gradle/caches/forge_gradle/maven_downloader/net/minecraftforge/forgeflower/1.5.605.9/forgeflower-1.5.605.9.jar.

RD client side investigated 2026-09-30 (plan 3.5a, static only): client packet path, TreeChop client mixin, prediction handler and leaf mixins can't keep removed leaves; targetable phantoms => server never sent the removal. Rubidium 0.6.2c holes: scheduleRebuild drops updates when !isBuilt(), ClonedChunkSectionCache.release unsynchronized. Forge sends no correction on START_DESTROY for air. Waiting on the in-game outline/F3+A check.

**Why:** 1.0.3 focuses on smaller updates, server perf reports and three player-facing bugs.
**How to apply:** build in the plan's order (SP, PR, IM, TS, DP, RD); move each built part into its own writeup and leave a status line in the plan. Related: [deploy-pipeline](deploy-pipeline.md), [perf-profiling-jfr](perf-profiling-jfr.md), memory `feature-writeup-pattern`.

DP and PR status: [plan_1.0.3.md](../plan_1.0.3.md), [deploy.md](../deploy.md), [perf.md](../perf.md).

2026-10-02: the plan doc now covers planned AND done work for the version (user's convention). Section 0.1 "Also in 1.0.3" lists unplanned changes (EX, CB, VJ, CU, CN, NM, FM, WG, CF, DV), found by diffing against deploy/history/1.0.2.json + development/patcherMod/jar_archive/creativeengineering-1.0.2.jar. Keep 0.1 current as more lands.

Status of each release (staged, deployed, dates): the header of its `feature_writeup/plan_1.0.x.md`, [deploy.md](../deploy.md) section 8 and `deploy/DEPLOY_LOG.md`. Status pass 2026-11-16 removed the per-release staging and deploy diary from this note; rules and gotchas stay.

## plan-1-0-4

On 2026-10-02 the user said that everything after the last push (`d791885 "1.0.3 Deployed"`) is 1.0.4. Writeup:
`feature_writeup/plan_1.0.4.md`, which follows the plan doc convention ([release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md)). IDs:
- XK: join-kick fix ([exposure-backport](exposure-backport.md))
- RF: refmap fix ([exposure-backport](exposure-backport.md))
- CB: camera bags moved to exposure_extras ([camera-bag-design](camera-bag-design.md))
- CP: crop parity; HA: hoe harvest areas ([crops-harvest-writeup](crops-harvest-writeup.md), [hoe-harvest-area](hoe-harvest-area.md))
- CG: canned golem dye ([canned-golem-dye](canned-golem-dye.md))
- NM: new mods Blue Flame Burning + Fruits Delight
- CF: config
- VR: version records

**Rule:** bump the in-tree mod versions once per pack release. Rebuilds before a release keep the number. A
post-deploy rebuild under the old name had to be moved to jar_archive/displaced/ as `*-postrelease-dev.jar` (2026-10-02).

Owner rule (2026-10-04): DistantHorizons.toml shipped once in 1.0.4, then classed player in CONFIG_CLASSES (through 1.0.4); DH settings are player config, never push after this.

## plan-1-0-5

**Why:** the deploy flow bumps in-tree mod versions once per release ([release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md) rule).

Gotcha (2026-10-14): build_deploy reads the live tree, so rebuilding an older state ("last stable build without the dev work") needs the newer work split out first; staged payloads can be re-verified against `history/<ver>.json` instead.

## plan-1-0-6

User 2026-10-14: "ship all the changes as 1.0.6. just mark the structure tuning as incomplete in this release. then update deploy and the writeups".

**Why:** structure testing (Phase 5 started 12:45 in another session) was still running; the user chose to ship it mid-test rather than split it out.
**Gotcha:** staging archives the shipped jar into jar_archive/<name>, overwriting an older unshipped build of the same version. Related: [release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md), [deploy-pipeline](deploy-pipeline.md), [plan-7-1-execution](plan-7-1-execution.md), memory `structure-testing-log-method`.
