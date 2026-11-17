# supplementaries-breezy-wind-vane-crash

> Supplementaries 2.4.20 wind vane crashes clients with Breezy 1.0.8 (NoClassDefFoundError coda.breezy); fixed by SupplementariesBreezyCompatMixin in Creative Common

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Crash 2026-10-04 14:24 (debug/crash-2026-10-04_14.24.57-client.txt): Supplementaries 2.4.20's `BreezyCompatImpl.getWindDirection` references `coda.breezy.networking.BreezyNetworking` / `WindDirectionSavedData` (old Breezy), but the pack ships Breezy 1.0.8 (`codyhuh.breezy.common.network.NewWindSavedData`, wind angle in degrees, `getWindAtHeight(y, level)`). First wind vane to tick on a client kills the game. Breezy has been in the pack since 1.0.1; no Supplementaries config turns the compat off.

Fix: `mixin/SupplementariesBreezyCompatMixin` (@Overwrite, targets by string) in Creative Common; needs breezy + supplementaries jars in `creativeCommon/libs/` (the AP must see the target class). Built into jar 1.0.4 (same version as deployed, so hash differs from mods/), NOT installed or deployed as of 2026-10-04. Vane heading is yaw from Breezy's (cos a, sin a) vector; a possible 180 degree offset vs the old behavior is untested in game.

**How to apply:** if wind vanes point the wrong way, flip the sign in the atan2. Related: [creative-common-coremod](creative-common-coremod.md), [deploy-pipeline](deploy-pipeline.md).
