# dnd-drill-saw-server-crash

> Create: Dreams & Desires Excavation Drill / Deforester Saw crash dedicated servers (client-only sendToServer + CLIENT config read in common BreakEvent code); fixed by 2 patcherMod @Redirect mixins

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

2026-09-26: CreEngServer crashed ("Ticking entity" -> `Attempted to load class net/minecraft/client/Minecraft for invalid dist DEDICATED_SERVER`) the first time a player broke a block. Cause: `create_dd` 1.2c Beta Mid-Dev `ExcavationDrillItem.onBlockDestroyed` (a common BreakEvent listener) runs `DesiresPackets.getChannel().sendToServer(new InvertFunctionPacket())` + `DesiresConfigs.client().invertExcavationDrillFunction.get()` for EVERY real-player block break, before checking the held item. `destroyVein` and `DeforesterSawItem.destroyTree` repeat the same two lines. The CLIENT config object exists on a server but its spec never loads, so `.get()` would throw too. InvertFunctionPacket's server handler just reads and discards the config (useless). JetpackHandler also uses client code but is a `Dist.CLIENT` subscriber, so it's fine.

The follow-up watchdog crash 60s later ("single server tick took 60s") was only fallout: the crashed server stayed alive because of non-daemon DH/AWT threads until the watchdog halted it.

Fix: `mixin/ExcavationDrillItemMixin` (onBlockDestroyed+destroyVein, require=2) + `mixin/DeforesterSawItemMixin` (destroyTree, require=1). Each has static `@Redirect`s on `SimpleChannel#sendToServer` (no-op) and `ConfigBase$ConfigBool#get` (returns FALSE, the config default) when `!FMLEnvironment.dist.isClient()`. SP/LAN behaviour is unchanged. The D&D jar was copied to `patcherMod/libs/Create-DnDesire-1.19.2-1.2c.jar`. Built and deployed to mods/, server_mods/, and the local CreEngServer/mods. **Not boot-tested** (not enough free RAM with the client running). Re-check the call sites if create_dd updates.

Related: [patchermod-coremod](patchermod-coremod.md), [creengserver-dedicated-server](creengserver-dedicated-server.md), memory `debugging-with-latest-log`.

2026-09-29: the external server ran jar 1.0.1 (with these mixins) from 09-27 with players breaking blocks and never hit this crash again, so the fix works in practice. See [server-crash-triage-0929](server-crash-triage-0929.md).
