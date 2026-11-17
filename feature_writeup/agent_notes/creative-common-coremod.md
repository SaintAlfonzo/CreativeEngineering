# creative-common-coremod

> patcherMod (now Creative Common, development/creativeCommon, modid creativecommon since 2026-11-04) is the pack's in-tree core mixin mod; creativeengineering is now the Engineering jar

Moved from agent memory 2026-11-16 (last edited there 2026-09-14). Repo status docs win over any status line here.

**Since 2026-11-04 (Creative Split wave 8, feature_writeup/creative_engineering.md):** patcherMod is **Creative Common**: folder `development/creativeCommon`, Gradle `:creativeCommon`, mod id `creativecommon`, package `com.SaintAlfonzo.creativecommon`, jar `creativecommon-<ver>.jar`, config `creativecommon-common.toml`, mixins `creativecommon.mixins.json`. Namespace of its own data (damage tags, WTHIT/JEI ids, lang keys, `kubejs/assets/creativecommon/` changelog and jei_world, JFR events) is `creativecommon` too since wave 9 (creative_split.md 7.14); ids other jars register stay `creativeengineering`. The mod id `creativeengineering` (and `creativeengineering-common.toml`) belongs to the **Creative Engineering** jar (`development/creativeEngineering`, 2.x). Registered ids keep the `creativeengineering:` namespace (Common registers none). Paths and names below are the pre-swap ones; the mixin and SRG lessons still hold.

`development/patcherMod/` (moved there by the user 2026-10-01; Gradle project still :patcherMod) is a ForgeGradle project (MC 1.19.2 / Forge 43.3.9, `mapping_channel=official`,
modid `creativeengineering`) built into `mods/creativeengineering-<mod_version>.jar` (current version: mod_version in development/patcherMod/gradle.properties; shipped versions: feature_writeup/deploy.md). A 1.0.2 bump on 2026-09-28 was undone at the user's request: they bump the version only when new features ship). Use it when a
change needs real Java / a Mixin — KubeJS and datapacks can't do it.

- **Jar archive (2026-09-28):** `patcherMod/jar_archive/` (git-ignored) holds one jar per version: 1.0.0 (from the old CreEngServer), 1.0.1, `rollback/`. `build` is finalizedBy `archiveJar`, which only ADDS a version not archived yet (dev rebuilds at a released version never replace it). `tools/build_deploy.py` copies the jar it ships into the archive. Before 2026-10-01 a plain `./gradlew build` left `build/libs` != `mods/` because of the manifest timestamp. Jars are reproducible now ([root-gradle-build](root-gradle-build.md)), so a difference means real changes. `patcherMod/LICENSE.txt` was the Forge MDK LGPL notice; it was replaced with GPLv3 on 2026-10-01 (memory `license-gplv3`).
- Build: `cd patcherMod && ./gradlew build --offline` (or `./gradlew :patcherMod:build` from the root; since 2026-10-01 patcherMod is a subproject of the root build, see [root-gradle-build](root-gradle-build.md)), then copy `build/libs/creativeengineering-<ver>.jar` into `mods/`. `build` does NOT copy. `./gradlew :patcherMod:installToMods` (added 2026-10-01, explicit only) builds and swaps it in, moving the old jar to jar_archive/displaced/. A clean `BUILD SUCCESSFUL` only means `build/libs/` was updated; `mods/` keeps the old jar (same file size ballpark, easy to not notice) until you `cp` it over by hand. Forgetting this step reproduces the *exact same* crash/stack trace on relaunch even though the source fix is correct — always verify with `md5sum build/libs/creativeengineering-<ver>.jar mods/creativeengineering-<ver>.jar` (or `javap -p` the specific class) after copying, not just after building. Never overwrite `mods/` while the game process is still running (see memory `debugging-with-latest-log` for a case where that alone caused a hard crash).
- **Version bump:** the version lives ONLY in `patcherMod/gradle.properties` `mod_version` (mods.toml reads `${mod_version}`, jar name follows). Bumping changes the jar filename, so also: remove the old jar from `mods/` (two jars with one modid = boot failure), update the userAdded entry's `name`/`file` in `instance.json` (ATLauncher closed), `mod_sides.md`, the jar name in `.claude/settings*.json` allowlist entries, the `item_physics.js` header comment, README.md, then `tools/gen_pack_manifest.py` + a new deploy version ([deploy-pipeline](deploy-pipeline.md)) so the old jar lands in the removals. The CreEngServer copy is separate and must be swapped by hand ([creengserver-dedicated-server](creengserver-dedicated-server.md)).
- Compile deps: `compileOnly fileTree(dir:'libs', include:'*.jar')` — drop the needed mod jars (and any JiJ deps, e.g. Registrate/flywheel extracted from `META-INF/jarjar/` of the create jar) into `patcherMod/libs/`.
- Mixins: listed in `src/main/resources/creativeengineering.mixins.json`, package `com.SaintAlfonzo.creativeengineering.mixin`. Target third-party classes with `@Mixin(value = X.class, remap = false)` (their own method names are not obfuscated at runtime). To call an inherited protected method, extend the target's superclass in the mixin rather than `@Shadow`-ing an inherited member (the AP can't resolve those).
- **Vanilla-class mixins:** the build generates NO Mixin refmap (no `org.spongepowered.mixin` Gradle plugin). So target MC members by their **SRG name** (`m_xxxxx_` / `f_xxxxx_`) with `remap = false`, and `build.gradle` passes `-AdisableTargetValidator=true` to the compiler so the AP accepts SRG strings against the dev-time Mojmap class. `@Accessor`/`@Shadow` of MC members still fail (validator-independent) — use `@Redirect`/`@Inject` on the method instead. Look SRG names up in `patcherMod/build/reobfJar/mappings.tsrg` or `build/createMcpToSrg/output.tsrg`.
- **Subclassing a Create class that implements an MC interface** (e.g. `ProcessingRecipe implements Recipe`): javac can't see the SRG-named inherited impls as satisfying the Mojmap interface, so re-declare the abstract MC methods (`getType`/`getSerializer`/`getId`/`getResultItem`/`assemble`/`canCraftInDimensions`/`matches`) with Mojmap names + trivial bodies; reobf maps them back onto the parent's slots. Delegate bodies via Create's own un-obfuscated helpers (e.g. `getRollableResultsAsItemStacks()`), never `super.m_xxxxx_()`.
- Setup hooks run from `CreativeEngineering.commonSetup` via `event.enqueueWork(...)`.

Example added 2026-09-05: `fluidbrewing/DragonBreathBrewing` + `MechanicalMixerBlockEntityMixin`
put fluid-reagent potion conversions into Create's Automatic Brewing — see memory `create-automatic-brewing`.

Example added 2026-09-05: `fan/` package + `mixin/SkullBlockEntityMixin` add a
`creativeengineering:dragon_breathing` Create fan-processing type (dragon head facing away
from an Encased Fan = catalyst) — see memory `create-fan-processing-type`.

Example added 2026-09-08: `itemphysic/ItemPhysicSplit` + `mixin/ItemPhysicServerMixin`
split ItemPhysic's single burn bucket into fire-vs-lava and make its float list additive,
driven by KubeJS `global.itemPhysics` — see [itemphysic-fire-lava-split](itemphysic-fire-lava-split.md). First mixin here
to target a CreativeMD mod; needed `ItemPhysic` + `CreativeCore` jars in `libs/`.

2026-09-10: tried + **removed** `mixin/AreaEffectCloudSphereMixin` (`@Redirect`s on
`AreaEffectCloud.tick()` `m_8119_`) — meant to veto victims outside a true 3D sphere and
randomize particle Y, opt-in via `getPersistentData().getBoolean("CESphere")`. Hit two real
Mixin lessons worth keeping even though the mixin itself is gone:
- **`@At` INVOKE target owner must match the receiver's compile-time class in the original
  vanilla source, not wherever the method is declared.** `this.getY()` called from inside
  `AreaEffectCloud` compiles with owner=`AreaEffectCloud` in the constant pool, not `Entity`
  (which merely declares `getY()`) — targeting `Entity` silently matched 0 call sites, no
  error logged anywhere. Verify with `javap -v` on the real class, don't assume from the
  declaring class.
- **`@Redirect` always requires the call's receiver as an explicit first handler parameter,
  even when the receiver is `this`/the mixin's own target class.** A no-arg handler (casting
  `this` internally instead) throws `InvalidInjectionException` at mixin-apply time (game
  boot crash), not a silent no-op.

Ultimately abandoned in favor of a pure-KubeJS stacked-ring dome approach (no Java) — see
[potion-buckets](potion-buckets.md) for the full story and current design.

2026-09-13: `@Redirect`/`@Inject` handler methods must match the **static-ness of the mixin's
target method** (the method being woven into, e.g. `render`), not the static-ness of whatever
call/field the injection point happens to touch. `CustomizableElytraBannerPatternMixin` redirected
a call to a *static* library method (`CustomizableElytraItem.getTextureLocation`) from inside
`BannerCustomizationHandler.render`, which is itself an *instance* method — the handler was
wrongly declared `private static` (matching the redirected call, not the enclosing method) and
threw `InvalidInjectionException: 'static' modifier ... does not match target` at mixin-apply
time (game boot crash). Fix: `javap -p` the real target class to check the enclosing method's
modifier, and match the handler to that. See [banner-pattern-compat-fix](banner-pattern-compat-fix.md) for the full incident,
including the follow-up deploy-step miss above.
- Shipped jar versions: [deploy.md](../deploy.md) section 8 and `deploy/DEPLOY_LOG.md` (status pass 2026-11-16 removed the 1.0.5 to 1.0.8 bump diary here).
- Gotcha: ATLauncher rewrites `instance.json` while it runs, so its userAdded jar entry cannot be updated then; check it after a version bump.
