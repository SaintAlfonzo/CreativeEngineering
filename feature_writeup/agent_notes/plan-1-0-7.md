# plan-1-0-7

> 1.0.7 release contents: two Brewin jars with one mod id (FML picked port -01, also on the server); plan in feature_writeup/plan_1.0.7.md, deploy state in deploy.md

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

1.0.7 = 15c43a0 (1.0.6 deploy) .. 908e831: structure/locate scratch-run fixes, QuarkSafeSort, remainder buffer, first oven, JEI ordering steps 1-3, Vinery port -01 (replaces 1.3.10 + doapi), Brewin port -01, Exposure -04, Create: The Salt, Storage Labels. patcherMod 1.0.7 sha1 9dd6ae4c (built 10-18 16:27). First staging 18:20 crashed the dedicated server on the Vinery grass slab (debug/10_18/); hotfix restaged 19:29 under the same name.

Gotcha found 2026-10-19: mods/ had BrewinAndChewin-1.19-2.0 AND backportedBrewin -01; build_deploy keys mods by mod id so it reported neither, and both shipped. debug/10_18/debug.log shows FML 43 UniqueModListBuilder "selecting most recent based on version data" -> the port loaded on the server too. Proposed (not built): preflight error for two jars with one mod id.

Related: [plan-1-0-8](plan-1-0-8.md), [deploy-pipeline](deploy-pipeline.md), [backported-brewin](backported-brewin.md).
