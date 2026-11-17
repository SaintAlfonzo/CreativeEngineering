# forgiving-void-origin-pull

> Creative Common mixin that pulls Forgiving Void's void-fall respawn 20-25 blocks (CEConfig default, raised from 10-20 on 2026-09-26) toward dimension origin each fall

Moved from agent memory 2026-11-16 (last edited there 2026-09-07). Repo status docs win over any status line here.

Forgiving Void (`forgivingvoid-forge-1.19-7.0.0.jar`, Balm-based) catches a player who
falls out of the world and, in `ForgivingVoid.onPlayerTick`, calls
`ServerPlayer.teleportTo(x, fallingHeight, z)` with the **same X/Z** — enabling infinite
void-fall loops and cross-End travel by void falling.

No config option for horizontal handling (`forgivingvoid-common.toml` only has
trigger distance / damage / fallingHeight / per-dimension toggles).

Fix: [creative-common-coremod](creative-common-coremod.md) mixin `mixin/ForgivingVoidMixin.java` (added to
`creativeengineering.mixins.json` `mixins` list) — `@Redirect` on the
`Lnet/minecraft/server/level/ServerPlayer;m_6021_(DDD)V` INVOKE in `onPlayerTick`
(`remap = false`, SRG target string; handler body calls Mojmap `teleportTo` which reobf
maps back to `m_6021_`). Handler pulls the landing X/Z 20-25 blocks (random) toward
(0,0), snapping to origin if already within one step. Fires once per void fall.

Build deps added to `creativeCommon/libs/`: `forgivingvoid-forge-1.19-7.0.0.jar` +
`balm-forge-1.19.2-4.6.0.jar` (the `@Mixin(value = ForgivingVoid.class)` target must be
on the AP classpath even when referenced only by name).
