# plan-1-0-8

> 1.0.8 release contents: server crash fix (Creative Common d60b5a99) + worldgen fixes (11_fixes_1_0_8.md); plan in feature_writeup/plan_1.0.8.md, deploy state in deploy.md

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

User 2026-10-19: "deploy the latest build as 1.0.8. do the writeups for features in 1.0.8 and any other missing previous version writeups".

Contents, jars, evidence and deploy state: [plan_1.0.8.md](../plan_1.0.8.md) (header and section 2), [deploy.md](../deploy.md) section 8 (2026-10-19). Status pass 2026-11-16 removed the staging diary here.

Open items from 1.0.8 live in plan_1.0.8.md 2 and TODO `pack/release #2` (server time preset, decided 2026-10-26) / `#3` (first-boot checks); the oven JEI [+] error and the duplicate-mod-id gap were fixed in 1.0.9.

Related: [plan-1-0-7](plan-1-0-7.md), [deploy-pipeline](deploy-pipeline.md), [time-master](time-master.md), [no-advancements](no-advancements.md).
- Gotcha (2026-10-19 blocker): a common-list mixin whose inject references `net.minecraft.client.Minecraft` (SereneSeasonsClockMixin onClientTick) stops a dedicated server boot (FML ClassMetadataNotFoundException); client-only injects go in the client mixin list (fix: SereneSeasonsClientClockMixin).
- To rebuild a version that was already snapshotted, set the DEPLOYED.json marks back to the previous version (its history json sha) and drop the stray log section, then `--version X` (done for 1.0.8 on 2026-10-19).
