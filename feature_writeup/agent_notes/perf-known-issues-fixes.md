# perf-known-issues-fixes

> 2026-10-03 fixes for the 5 perf.md section 7 hot spots: Creative Common [perf] CEConfig throttles (trough cooldown, Serene Seasons melt, Create Gravity dimensions), Fruits Delight bytecode patch via mixin config plugin, horse armor pass

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Built 2026-10-03 (Creative Common 1.0.4 jar in build/libs). Details + dials: feature_writeup/perf.md section 7. Mixins: FeedingTroughModuleMixin + TemptGoalTroughRetryMixin (Quark), SereneSeasonsMeltThrottleMixin, CreateGravityDimensionMixin, FruitsDelightLeafCheckMixin (moved to Creative Delight's fruitsdelight compartment 2026-10-27); CEConfig `[perf]`. The user said "7 known issues" but the writeup lists 5; I did the 5.

**Lessons worth keeping:**
- Mixin will NOT inject into another mixin's merged `handler$...` method, even with a regex selector and a higher priority (silently skipped under require=0; verified in the .mixin.out export). To change another mod's mixin handler, use an `IMixinConfigPlugin.postApply` ASM edit (since 2026-10-27 only CD's `compat/CDMixinPlugin` does this, as an anchor -> edit table; Creative Common has no mixin plugin any more) on an empty anchor mixin with priority 5000. Keep the edit stack-neutral so no stack-map frames change.
- Third-party libs for compile go in `development/creativeCommon/libs/` (git-ignored): added SereneSeasons, create_gravity, fruitsdelight.
- Verify mixin application with a scratch server and `-Dmixin.debug.export=true`, then `javap -p -c -cp .mixin.out/class <class>` (memory `scratch-server-verify`). The session scratchpad can be wiped mid-task, rebuild srv/ from the recipe.
- `minecraft:strong_regeneration` is a potion id; the dragon horse armor mount() used it so it never applied an effect (now `minecraft:regeneration`, amplifier 0). Check [armor-tool-effects](armor-tool-effects.md).
- Snow Under Trees was removed from the pack in 1.0.2.

**How to apply:** deploy needs `installToMods` / copy of the 1.0.4 jar ([patchermod-coremod](patchermod-coremod.md), [deploy-pipeline](deploy-pipeline.md)). Under real load, re-profile with [perf-profiling-jfr](perf-profiling-jfr.md) to confirm the shares dropped.

**First in-game profile 2026-10-03 (runs sp-20261003-1711/-172709/-193542):** the three runs all used the OLD mods/ jar (302 classes, 14:51, no perf mixins), so the Java throttles were never measured. The new horse-armor script ran against it and threw `CEConfig has no public ... "PERF"` every level tick (257k errors in latest.log, 313 MB debug.log); Rhino's failed property lookup falls into `ClassLoader.loadClass` = ~1.3% of a core, ~75% of KubeJS levelPostTick. The script's `$ArmorFxCEConfig ?` guard covers a missing class, not a missing field. Bigger problem than any mod: 8 GB heap with 6.7-8.1 GB live (DH + worldgen) -> G1Full death spiral at the end of each run (ticks ~195 ms, 30-44 s GC pause per 2 min). Deploy the jar, then re-profile.
