# Agent notes status pass (2026-11-16)

One-time pass from [plan_process_overhaul.md](../plan_process_overhaul.md) P1.4 item 5 and the P1.6 follow-up. Every note
in this folder was grepped for status words (built, deployed, installed, shipped, tested, versions, "awaiting").
Reading the matches left 64 notes with a real status-type claim. Each status claim was checked against [README.md](../README.md), [todo/TODO.md](../todo/TODO.md),
the owning writeup or `plan_*.md`, [deploy.md](../deploy.md), `deploy/DEPLOY_LOG.md` and `deploy/DEPLOYED.json`.

Rule used:
- A line that only says where a jar is, what was staged or deployed, or whether a test has run was removed or cut to
  a pointer to the doc that owns the status.
- Mechanics, gotchas, owner rules and dated findings (a crash, a measured result) stay.
- Dated "built on" lines kept as history when they claim nothing about the current state.

## Notes changed (33)

| Note | Removed or changed | Status lives in | Found |
|---|---|---|---|
| [deploy-pipeline](deploy-pipeline.md) | 11 per-version staging and deploy paragraphs (1.0.2 to 1.0.8), the "1.0.2 snapshotted for both sides" sentence; kept the gotchas inside them (no content test before a deploy, stale mods/ jar, gradle copies into jar_archive, DH config class) | `deploy/DEPLOY_LOG.md`, DEPLOYED.json, deploy.md 8, `plan_1.0.x.md` headers | note said 1.0.2 was snapshotted for both sides; DEPLOY_LOG says client only (the note itself later said the entry was edited) |
| [release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md) | staging and deploy diary for 1.0.3 to 1.0.6 (done-lists, blockers, "Next: --snapshot", jar md5s); kept rules (version bump per release, DH player config, live-tree gotcha, jar_archive overwrite gotcha) | `plan_1.0.3.md` to `plan_1.0.6.md` headers, deploy.md 8 | none |
| [plan-1-0-7](plan-1-0-7.md) | contents and jar line to a pointer; "Proposed (not built)" duplicate-mod-id check | plan_1.0.7.md, plan_1.0.9.md row DP | the check was built and DEPLOYED in 1.0.9 |
| [plan-1-0-8](plan-1-0-8.md) | Done paragraph, open list, "--snapshot 1.0.8", deployed line; kept the client-class mixin gotcha and the rebuild-after-snapshot method | plan_1.0.8.md, TODO `pack/release #2`, `#3` | open list was stale: oven JEI and duplicate jars fixed in 1.0.9, `#2` decided 2026-10-26 |
| [plan-1-0-9](plan-1-0-9.md) | "DEPLOYED ... nothing run in game yet"; mods/ jar md5 | plan_1.0.9.md, TODO `pack/release #4` | agrees (`#4` Testing) |
| [miners-delight-backport-plan](miners-delight-backport-plan.md) | "DEPLOYED.json still says 1.1.01" | DEPLOY_LOG (1.1.02 deployed 2026-10-23) | stale: DEPLOYED.json is at 1.2.02 |
| [creative-common-coremod](creative-common-coremod.md) | 1.0.5 to 1.0.8 bump diary; kept the instance.json gotcha | deploy.md 8, DEPLOY_LOG | none |
| [backported-brewin](backported-brewin.md) | `-02` build and install lines; kept the bump-before-rebuild rule | alcohol.md, creative_brewing.md | port is a Creative Brewing member since 2026-10-31 |
| [backported-vinery](backported-vinery.md) | `-02` / `-03` install lines | alcohol.md, creative_brewing.md | none |
| [alcohol-drink-food-values](alcohol-drink-food-values.md) | "Rebuilt as -03" | alcohol.md | none |
| [appleskin-tooltip-coverage](appleskin-tooltip-coverage.md) | jar versions installed | tooltips.md TT-19..30, fixes_compat 2.24 | none |
| [camera-bag-design](camera-bag-design.md) | "in progress", "in mods/, not boot-tested, remap untested" | camera_bags.md (DEPLOYED, R4-04 never run) | agrees |
| [armor-tool-effects](armor-tool-effects.md) | "NOT yet smoke-tested in-game" | armor_effects.md 0 (DEPLOYED, in-game test pending) | agrees |
| [campfire-rain](campfire-rain.md) | "Built into creativeengineering-1.0.9.jar in mods/" | barbeques_delight.md weather row; moved to Creative Delight 2026-11-04 (creative_compat.md) | stale jar name |
| [am-potion-brewing-fix](am-potion-brewing-fix.md) | "rebuilt + deployed" (2) | potions.md 0 | none |
| [bee-nests-system](bee-nests-system.md) | "awaiting user review" | bee_nests.md 0 ("your review still open") | agrees |
| [blockrunner-speed-tiers](blockrunner-speed-tiers.md) | "deployed 2026-09-26" | README BlockRunner row | none |
| [banner-pattern-compat-fix](banner-pattern-compat-fix.md) | status heading "built + deployed, NOT yet verified" | fixes_compat.md 2.2 (DEPLOYED) | agrees |
| [can-parity-worksheet](can-parity-worksheet.md) | "sprites deployed", "no remap yet" | can_parity.md ("no issue, no remap, closed") | note read as open; doc closed it |
| [incubation-nest-internals](incubation-nest-internals.md) | "copied to mods/, shipped in 1.0.2 restage", "installed 1.0.5 jar still has copies" | nests.md | none |
| [canned-golem-dye](canned-golem-dye.md) | "installed to mods/" (2) | cans.md 0 (DEPLOYED with 1.0.4, R4-07) | none (built in the 1.0.3 jar, shipped 1.0.4) |
| [dead-structure-locate-hang](dead-structure-locate-hang.md) | "deployed: the 2026-09-27 jar in mods/" | structure_tuning.md | none |
| [exposure-backport](exposure-backport.md) | "Deploy 1.0.3 staged", per-pack jar line, "not yet installed or deployed", a version-bump line that appeared twice | README Exposure row, exposure.md | note claimed the null fix unshipped; it shipped as `-02` (1.0.4) |
| [structure-groups](structure-groups.md) | 2026-09-27 jar md5 / CreEngServer line; "7.1 NOT BUILT, 12 decisions open" | structure_tuning.md 0, plan-7-1-execution | stale: 7.1 built 2026-10-12 |
| [structure-ground-match](structure-ground-match.md) | deploy jar and rollback copy | structure_tuning.md 3.8 | none |
| [structure-gen-framework](structure-gen-framework.md) | "Built and deployed; NOT yet re-tested in game"; dead link `loot-overhaul-lootjs.md` to `loot-system.md` | structure_tuning.md 0 (VERIFIED 2026-09-26) | stale: doc says VERIFIED |
| [sticky-notes-rebuild](sticky-notes-rebuild.md) | "installed in mods/" | README Creative Labels row | none |
| [rustic-delight-port](rustic-delight-port.md) | "the 1.3.0 jar stays live until CD is installed" | rustic_delight.md | stale: no Rustic jar in `mods/` |
| [server-crash-triage-0929](server-crash-triage-0929.md) | "OPEN" / "fixed-not-deployed" in the summary and headings, "now in mods/" | deploy.md 8 (1.0.2) | summary read as still open; the note's own actions section fixed or removed all three |
| [client-exit-hang-nvidia-gldebug](client-exit-hang-nvidia-gldebug.md) | "Built + deployed to both mods/; NOT yet confirmed" | fixes_compat.md 2.4 (DEPLOYED) | agrees |
| [potion-buckets](potion-buckets.md) | "Ring stack not yet in-game tested" | potion_buckets.md PB-04 (unchecked) | agrees |
| [time-master](time-master.md) | "OPEN gotcha" on the server preset | TODO `pack/release #2` (decided 2026-10-26) | stale: decided |
| [ie-potion-machines](ie-potion-machines.md) | "built + deployed" (5), "needs /reload + restart" | potions.md 0 | none |

## Notes checked and left (31)

Dated build history or findings, no current-state claim; or "installed" means another mod is present, not our jar:
damage-types, shields, magic-resistance-effect, perf-profiling-jfr, locate-writeup, biome-fits-towers,
vegetation-worldgen-system, plan-7-1-execution (run log; status pointer to testing/structures/00_plan.md already in its
description), plan-7-4-new-structure-mods, server-crash-triage-1019, jigsaw-pool-tuning, power-grid-port (agrees with
plan_power_grid.md: P0 to P10 done 2026-11-13), compost-framework, structure-spacing-feasibility, earth-fill-system,
sawblade-recipe-routing, create-config-string-widget, ghost-blocks-null-state-packets, terrain-adaptation-soft-beard,
incubation-additions-mod, loot-system, moobloom-variants, generated-per-type-cans, weather-by-biome,
upstream-jar-distribution-policy, farmers-structures-donor, feature-ports-2026-10-20, jeed-effect-descriptions,
bushier-flowers-disabled, supplementaries-breezy-wind-vane-crash, dnd-drill-saw-server-crash.

## Facts kept in notes that the repo docs lack

- Owner rule: no content test before a deploy, only update the deploy folder ([deploy-pipeline](deploy-pipeline.md)).
  deploy.md does not say it.
- Owner rule: DistantHorizons settings are player config, never pushed after 1.0.4
  ([release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md)). deploy.md does not say it.

## Repo docs that disagree with each other (for the owner, not changed)

- deploy.md section 0 "Deployment snapshot" row says server 1.1.03, client 1.1.04. DEPLOYED.json and DEPLOY_LOG say
  both sides are on 1.2.02 (server 2026-11-09 17:59, client 17:46).
- deploy.md's status header (lines 3 to 9) ends at 1.1.05 and has nothing about 1.2.x.
- deploy.md section 8 has no bullet for these snapshots, which DEPLOY_LOG records:
  - 1.1.01 (2026-10-22 22:39);
  - 1.1.03 (2026-10-26 19:04);
  - 1.1.04 server (2026-10-31 12:42);
  - 1.1.05 (2026-10-31 12:51 / 12:52);
  - 1.1.06 client (2026-10-31 17:58).

  `build_deploy.py --backfill-history` can add them. Only 1.2.00 to 1.2.02 were added this pass.
- TODO `pack/release #7` says the server is on 1.1.03 and the 1.1.04 payload was not copied. DEPLOY_LOG says the server
  got 1.1.04 on 2026-10-31 12:42 and is now on 1.2.02.
- DEPLOYED.json and the DEPLOY_LOG "Deployed" line record the 1.2.00 client snapshot twice (20:40 and 20:55).
