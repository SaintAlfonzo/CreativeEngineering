# item-loss-repro

> IM automated repro 2026-09-30 (tools/item_repro/): Sophisticated Backpacks anti-dupe wipes a backpack's contentsUuid on re-open; contents recoverable by old UUID; harness gotchas

Moved from agent memory 2026-11-16 (last edited there 2026-10-01). Repo status docs win over any status line here.

Built 2026-09-30 on request ("do 4", user not confident reproducing by hand). Scratch server + throwaway client + `lagproxy.py` + KubeJS fuzzer/ledger. Results in plan_1.0.3.md section 4.7; logs in tools/item_repro/runs/2026-09-30/.

Finding: the only permanent loss (3 times in ~380 rounds, also once with no lag) is SB 3.20.2's dedupe (`UUIDDeduplicator.checkForDuplicateBackpacksAndRemoveTheirUUID` from the BackpackContainer ctor, identity compare) stripping `contentsUuid` from the real backpack when a backpack screen is re-opened while one is open with clicks in flight. The contents are orphaned in data/sophisticatedbackpacks.dat, and `/give ... sophisticatedbackpacks:backpack{contentsUuid:[I;...]}` restores them. No vanilla click path lost items. Lag mostly causes stale/different-menu views (look-deleted, return on reopen). Fix not built yet; the user has not said whether their losses involve backpacks.

Harness gotchas: KubeJS hides getUUID/getTag/setTag and LocalPlayer.closeContainer (also its SRG name) -> getUuid/getNbt/setNbt/Client.screen.onClose(); java.lang.System is class-filtered and throws an uncatchable error; `/reload` truncates logs/kubejs/server.log (read latest.log); client kubejs/client.log is recreated per launch; resource reload does NOT reload client scripts (restart, ~3 min); a static test player lets thrown items despawn (pull them to the player); `pkill -f "lagproxy..."` kills its own shell.

**Why:** IM was only seen on the dedicated server; this was the first hard evidence.
**How to apply:** ask whether the losses involved backpacks before building the IM monitor; a fix would be a Creative Common mixin on the SB dedupe (skip when it is the same backpack in the open menu's context). Related: [release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md), memory `scratch-server-verify`, memory `client-launch-for-defaults`, memory `rhino-fieldandmethods-gotcha`.
