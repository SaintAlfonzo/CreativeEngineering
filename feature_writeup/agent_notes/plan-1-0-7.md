# plan-1-0-7

> 1.0.7 release contents: two Brewin jars with one mod id (FML picked port -01, also on the server); plan in feature_writeup/plan_1.0.7.md, deploy state in deploy.md

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Contents, jars and deploy state: [plan_1.0.7.md](../plan_1.0.7.md), [deploy.md](../deploy.md) section 8 (2026-10-18). First staging crashed the dedicated server on the Vinery grass slab ([backported-vinery](backported-vinery.md)); a hotfix may be restaged under the same name only while the version is not yet snapshotted.

Gotcha found 2026-10-19: mods/ had BrewinAndChewin-1.19-2.0 AND backportedBrewin -01; build_deploy keys mods by mod id so it reported neither, and both shipped. debug/10_18/debug.log shows FML 43 UniqueModListBuilder "selecting most recent based on version data" -> the port loaded on the server too. Fixed in 1.0.9: preflight error for two jars with one mod id ([plan_1.0.9.md](../plan_1.0.9.md) row DP).

Related: [plan-1-0-8](plan-1-0-8.md), [deploy-pipeline](deploy-pipeline.md), [backported-brewin](backported-brewin.md).
