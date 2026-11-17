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

DP built 2026-09-30: see [deploy-pipeline](deploy-pipeline.md) and feature_writeup/deploy.md (DP-01..07 pending a real update).
PR built 2026-09-30: see [perf-profiling-jfr](perf-profiling-jfr.md) (report.sh + ingest_report.py, PF-09..12 pending on the real server).

2026-10-02: the plan doc now covers planned AND done work for the version (user's convention). Section 0.1 "Also in 1.0.3" lists unplanned changes (EX, CB, VJ, CU, CN, NM, FM, WG, CF, DV), found by diffing against deploy/history/1.0.2.json + development/patcherMod/jar_archive/creativeengineering-1.0.2.jar. Keep 0.1 current as more lands. Open blockers listed there: 4 new food-mod jars lack mod_sides.md rows, deploy/latest 1.0.3 staging is stale, JEI bookmarks.ini ships.

2026-10-02 evening: 1.0.3 is DEPLOYED (both sides, 15:55, d791885; snapshot recorded after the fact). Post-deploy items (CP, camera bag move) moved out to [release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md).

## plan-1-0-4

On 2026-10-02 the user said that everything after the last push (`d791885 "1.0.3 Deployed"`) is 1.0.4. Writeup:
`feature_writeup/plan_1.0.4.md`, which follows the plan doc convention ([release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md)). IDs:
- XK: join-kick fix ([exposure-backport](exposure-backport.md))
- RF: refmap fix ([exposure-backport](exposure-backport.md))
- CB: camera bags moved to exposure_extras, in progress ([camera-bag-design](camera-bag-design.md))
- CP: crop parity; HA: hoe harvest areas ([crops-harvest-writeup](crops-harvest-writeup.md), [hoe-harvest-area](hoe-harvest-area.md))
- CG: canned golem dye ([canned-golem-dye](canned-golem-dye.md))
- NM: new mods Blue Flame Burning + Fruits Delight
- CF: config
- VR: version records

Done the same day:
- patcherMod mod_version 1.0.4 and exposureBackport `1.9.19-ce-19.2-02`, both rebuilt and installToMods'd.
- The jar_archive 1.0.3 and -01 jars still match history/1.0.3.json. The post-deploy rebuilds went to
  displaced/ as `*-postrelease-dev.jar`.
- `--snapshot 1.0.3 --target both --date 2026-10-02T15:55` was recorded.
- `--version 1.0.4 --dry-run --force`: 26 files, 5.7 MB.

**Blockers before staging:**
- (DONE 2026-10-02) mod_sides rows for blueflame, fruitsdelight, ipo, well; DR data refresh is plan section 5b
- confirm the T&T waystones zip removal
- a boot test with the 1.0.4 jars
- R4-04 old-bag remap check
- commit

**Rule:** bump the in-tree mod versions once per pack release. Rebuilds before a release keep the number. A
post-deploy rebuild under the old name is what made this cleanup necessary.

**2026-10-04 update + staging (user: "update the 1.0.4 writeup ... then deploy to the deploy folder"):** plan_1.0.4.md now
covers EF (earth fill/banks/driftwood), PF (perf throttles), VB (barrel stacking, years_per_effect_level 2), LC (Golden Food book
removed, Lootr fixed containers, loot catalog), dyeable camera bags, WC additions, DH/starterkit config, Dimensional Gravity removed.
`build_deploy.py --version 1.0.4` staged: jars patcherMod md5 55a2e65a (22:51 build; `mods/` had a stale 19:36 one, replaced via
cp + atomic mv because the game was running, never installToMods over a loaded jar) and exposureBackport e54032b6. Preflight fixes:
wild_crops.json = runtime KUBEJS_CLASSES rule; `server/perf/reports/*` EXCLUDEd. NOT committed (130 files dirty), nothing booted on
the final jars. **Next:** user copies payload, then `--snapshot 1.0.4`; offer a scratch-server boot + commit first.

**Deployed:** user had me record `--snapshot 1.0.4` (both, 13:54). Dimensional Gravity mod_sides row dropped. DistantHorizons.toml shipped once in 1.0.4, then classed player in CONFIG_CLASSES (through 1.0.4); user rule: DH settings are player config, never push after this. R4 checks still unrun. Next version 1.0.5.

## plan-1-0-5

User asked 2026-10-05 to "wrap all my changes from 1.0.4 into 1.0.5 and deploy the instance to the deploy folder". Did: patcherMod mod_version 1.0.4 -> 1.0.5 (built + installToMods, game not running), exposureBackport unchanged (-02), `build_deploy.py --version 1.0.5` staged to deploy/latest (22 files, 4.2 MB), plan_1.0.5.md written + README index (1.0.4 row marked DEPLOYED).

Contents: duck/goose nests, stack size policy, generated per-type cans, crop growth WTHIT line, ghost-block (state id overflow) fixes + block_state_sync debug config, Supplementaries/Breezy mixin, data refresh.

**Why:** the deploy flow bumps in-tree mod versions once per release ([release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md) rule).
**How to apply:** not committed (user hasn't asked); after the user copies the payload run `--snapshot 1.0.5`. See [deploy-pipeline](deploy-pipeline.md).

**2026-10-05 later:** exposureBackport bumped to -03 (album text box focus fix) and installed to mods/ after staging. deploy/latest still has no exposure jar, so restage with `--version 1.0.5` before the user copies it.

**2026-10-11 restage (user: "populate my mod side definitions..., populate my changes to be deployed as 1.0.5, update the writeups..., increment my next mod build to be 1.0.6"):**
- mod_sides rows: breedablekillerrabbit server, coolrain both (strict channel), MEED both (jeed recipes), Pretty Rain client; JEED client -> both (registers recipe types both sides). 9 KUBEJS_CLASSES rules (6 runtime, 3 dev).
- `--version 1.0.5` staged: update from 1.0.4, 146 files / 13.7 MB; removals = creativeengineering-1.0.4, exposure -02, item_properties.js. Jars: patcherMod md5 68509043, exposure -03, incubationAdditions + moreMooblooms 1.0.0 (first release).
- Evidence: client booted 14:42 with exactly these jars, world 14:47-15:40, 0 mixin / KubeJS errors, every worldgen system logged its apply line.
- plan_1.0.5.md rewritten (IDs IA..VR, open list incl. WE-25 Cool Rain vs season types, 5 Terralith cave biomes without wild crops, stale stack_sizes.md, dead kubejs/assets/*bloom.png copies); 14 feature writeups + README + deploy.md updated.
- Then patcherMod mod_version -> 1.0.6 (gradle.properties only, not built). Not committed.
**Next:** user copies payload -> `--snapshot 1.0.5`.

**2026-10-11 later:** patcherMod 1.0.6 built and installed via installToMods (snow line + snow seasons only on top of 1.0.5; 1.0.5 jar -> jar_archive/displaced). mod_sides.md updated; instance.json already updated to 1.0.6 by ATLauncher rescan (21:24). Not staged/deployed; no plan_1.0.6.md yet.

**2026-10-14:** user asked to "build the deploy folder with the last stable build, without structure testing changes still in dev". Answer: 1.0.5 as staged 10-11. deploy/latest re-verified (146/146 files match history/1.0.5.json). It was NOT rebuilt: build_deploy reads the live tree, which now holds 7.1-7.4 structure dev (mods/ creativeengineering-1.0.6 = 10-14 dev jar, worldgen json, YUNG toggles, new structure mods). A clean 1.0.6 needs that work split out first.

**2026-10-14 later:** user chose to ship everything as 1.0.6 instead (structure tuning marked INCOMPLETE). 1.0.5 is superseded, never deployed; see [release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md).

## plan-1-0-6

User 2026-10-14: "ship all the changes as 1.0.6. just mark the structure tuning as incomplete in this release. then update deploy and the writeups".

Done: `build_deploy.py --version 1.0.6` (update from 1.0.4; 1.0.5 stays in history as staged-never-deployed, the changelog counts from it). patcherMod 1.0.6 = 12:44 rebuild of the 01:28 source (same code the Phase 3/4 scratch runs used), md5 5acd69dc. 3 KUBEJS_CLASSES rules (meat/nether_block_swaps runtime, nether_swap_rules dev), 4 mod_sides rows, pack manifest regenerated. Writeups: plan_1.0.6.md (IDs R5, SN, CN, IP, NM, ST, CF, VR; 2.1 open structure bugs; 2.2 [worldgen.systems] switches), README index + release table, structure_tuning/locate/biome_fits marked INCOMPLETE in 1.0.6, a 2026-10-14 history line in 14 feature writeups, plan_1.0.5 marked folded into 1.0.6, deploy.md, testing/structures/00_plan.md. deploy/latest/README.md got a hand-added INCOMPLETE + switches block (lost on rebuild).

Then patcherMod `mod_version` -> 1.0.7 (not built), so structure fixes from Phase 5 onward never overwrite the shipped jar. Not committed.

**Why:** structure testing (Phase 5 started 12:45 in another session) was still running; the user chose to ship it mid-test rather than split it out.
**How to apply:** after the user copies the payload: `--snapshot 1.0.6`. Fixes go into 1.0.7. Gotcha: staging archives the shipped jar into jar_archive/<name>, overwriting an older unshipped build of the same version. Related: [release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md), [deploy-pipeline](deploy-pipeline.md), [plan-7-1-execution](plan-7-1-execution.md), memory `structure-testing-log-method`.
