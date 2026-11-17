# plan-1-0-8

> 1.0.8 release contents: server crash fix (patcherMod d60b5a99) + worldgen fixes (11_fixes_1_0_8.md); plan in feature_writeup/plan_1.0.8.md, deploy state in deploy.md

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

User 2026-10-19: "deploy the latest build as 1.0.8. do the writeups for features in 1.0.8 and any other missing previous version writeups".

Done: pack manifest regenerated, `build_deploy.py --version 1.0.8` (replaced a stray 18:36 1.0.8 log section). Jars: patcherMod 1.0.8 md5 f136f818, Vinery -02 a3e1164d, Brewin -02 8441a046 (all = build/libs = jar_archive). Removals both sides: BrewinAndChewin-1.19-2.0, backportedBrewin -01, backportedVinery -01, creativeengineering-1.0.7, kubejs/assets/cornmeal.png. Evidence: the user's single-player session 00:53-01:11 on these jars (0 KubeJS errors, `[time]` push + sun migration lines, 14828 advancements left out). Writeups: plan_1.0.7.md (retroactive), plan_1.0.8.md, advancements.md (AV-), fixes_compat 2.20-2.23, README release table + statuses, plan_1.0.6 marked DEPLOYED, history lines in ~15 writeups, TODO.md. Then gradle.properties: patcherMod 1.0.9, ports -03 (not built). Not committed.

Open in plan_1.0.8.md 2: server `[time] preset` (payload ships SINGLEPLAYER to the server; options A by hand / B server override / C follow side), oven JEI [+] error (slot 2 "cannot pickup", since 1.0.7), live-world removals (vinery mead/king_danis_wine/calendar, Brewin jams), advancement progress dropped on save, build_deploy misses two jars with one mod id.

**How to apply:** after the user copies the payload: `--snapshot 1.0.8`. Related: [plan-1-0-7](plan-1-0-7.md), [deploy-pipeline](deploy-pipeline.md), [time-master](time-master.md), [no-advancements](no-advancements.md).
- 2026-10-19 01:39: BLOCKER found by the structure scratch server: patcherMod 1.0.8 does not boot a dedicated server (FML: ClassMetadataNotFoundException net.minecraft.client.Minecraft applying SereneSeasonsClockMixin, common mixin whose onClientTick inject uses Minecraft directly). Added to plan_1.0.8.md 2. Not fixed (user's call).
- 2026-10-19 10:06 (user: fix + "redeployed as 1.0.8"): BLOCKER FIXED (SereneSeasonsClientClockMixin in the client list) + worldgen fixes, writeup feature_writeup/testing/structures/11_fixes_1_0_8.md. mods/ + jar_archive = d60b5a99; crashing f136f818 kept in jar_archive/displaced/. DEPLOYED.json marks set back to 1.0.7 (sha of history/1.0.7.json, the 01:33 1.0.8 log entry removed) so --version 1.0.8 could rebuild. Next: user copies, then `--snapshot 1.0.8`.
- 2026-10-19: deployed and snapshotted 10:20 (both sides); superseded by 1.0.9 at 15:54. Writeups swept to DEPLOYED the same day.
