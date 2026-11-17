# banner-pattern-compat-fix

> patcherMod fix so modded banner patterns render on bedspreads beds and customizableelytra elytra, not just vanilla ones; status in feature_writeup/fixes_compat.md

Moved from agent memory 2026-11-16 (last edited there 2026-09-14). Repo status docs win over any status line here.

## Status: bug found on first in-game launch, fixed + rebuilt, needs a clean relaunch to confirm

First actual launch after implementation hard-crashed at `[23:00:01]` with a Mixin `InvalidInjectionException: 'static' modifier of handler method does not match target` on
`CustomizableElytraBannerPatternMixin`. Root cause: the `@Redirect` handler
`creativeengineering$fallbackToRealPatternTexture` was declared `private static`, but Mixin
requires an injected handler's static-ness to match its **target method's** static-ness (the
method the mixin is being woven into — `BannerCustomizationHandler.render`, confirmed via
`javap` to be an *instance* method), not the static-ness of the specific call being redirected
inside it (`CustomizableElytraItem.getTextureLocation` is static, which is a red herring — that
doesn't matter here). Fixed by dropping `static` from the handler signature. `-AdisableTargetValidator=true`
(noted below) is exactly why this wasn't caught at compile time — this class of error is
Mixin-apply-time only, confirming the caveat already written into "Verification done" below.
Rebuilt clean (`./gradlew build`, BUILD SUCCESSFUL) — but the first pass mistakenly assumed
`mods/creativeengineering-1.0.0.jar` gets auto-synced from `patcherMod/build/libs/`; there is
**no such gradle task** (`grep -i copy build.gradle` — nothing), and the mods/ jar stayed the
stale pre-fix build even after a successful compile. Relaunching crashed with the exact same
`InvalidInjectionException` again. Caught by `javap -p` on the deployed jar's actual class file
(`private static` still present) vs. the freshly built one (`private`, correctly non-static) —
different md5, different timestamp. **`patcherMod/build/libs/...jar` must be manually `cp`'d over
`mods/creativeengineering-1.0.0.jar` after every rebuild** (game closed) — there is no automatic
deploy step in this project. Copied for real this time, byte-identical confirmed via md5sum, game
was closed throughout. `BedspreadsBannerPatternMixin` had no such static/instance issue (its
target, `renderPatterns`, actually is static, so the handler's `static` was correct there).

General lesson for future patcherMod `@Redirect`/`@Inject` mixins: match the handler's
`static`-ness to the **target method's** modifier (`javap -p` the real target class to check),
not to the modifier of whatever call/field the injection point happens to touch.

## Implementation history (status: fixes_compat.md row 2.2)

`./gradlew build` succeeded, `reobfJar` ran, and every vanilla method/field reference in the new
code was cross-checked byte-for-byte against both the real SRG mapping table
(`srg_to_official_1.19.2.tsrg`) and the actual shipped bedspreads/customizableelytra bytecode —
all match exactly (see "Verification done" below). The built jar was copied over
`mods/creativeengineering-1.0.0.jar`. The only remaining step is launching the game and checking
a modded-pattern banner on a decorated bed and on a customizable elytra.

## The bug (confirmed via decompilation)

bedspreads and customizableelytra each hardcode their own private, hand-curated
banner-pattern-texture lookup instead of using vanilla's real registry-driven resolution — so
only the specific patterns they bundled art for work; any other mod's pattern renders blank (or,
for customizableelytra, risks rendering the *wrong* pattern on a path collision, since it keys by
path only).

- **bedspreads** (`bedspreads-forge-6.0.0+1.19.2.jar`,
  `com.illusivesoulworks.bedspreads.client.DecoratedBedBlockEntityRenderer.renderPatterns`, used
  for both the in-world bed render and the bed item render, which just delegates to it via
  `Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(...)`): builds
  `new ResourceLocation("bedspreads", "entity/" + originNamespace + "/" + path)`. Only ships
  bundled compat copies for `minecraft`, `alexsmobs`, `iceandfire`, `rats` (under
  `assets/bedspreads/textures/entity/<modid>/`). Confirmed these bundled PNGs are plain 64x64
  same-shape relocations of the real pattern art — safe to fully override, no distortion risk.
- **customizableelytra** (`customizableelytra-1.19.0-1.7.4.jar`,
  `com.hidoni.customizableelytra.items.CustomizableElytraItem.getTextureLocation(ResourceKey)`,
  called from `com.hidoni.customizableelytra.util.BannerCustomizationHandler.render`): builds
  `new ResourceLocation("customizableelytra", "entity/elytra_banner(_low)/" + path)`, dropping
  the origin namespace entirely. Ships ~103 bundled PNGs
  (`assets/customizableelytra/textures/entity/elytra_banner/*.png`). Confirmed these are **not**
  plain relocations — custom-shaped for the elytra wing's UV layout (128x64 vs. vanilla's 64x64
  banner sprite, e.g. `creeper.png` 64x64 gray+alpha vanilla vs. 128x64 RGBA elytra version). A
  blanket override would stretch every currently-working pattern, so the fix only substitutes the
  real texture when the curated lookup is actually missing (same
  `sprite.getName() != MissingTextureAtlasSprite.getLocation()` check the mod's own render loop
  already performs, just acted on instead of silently skipping the layer).

This modpack has 6 mods registering their own banner patterns (scanned every mod jar for
`assets/<modid>/textures/entity/banner/`): `alexsmobs`, `botania`, `buzzier_bees`,
`immersiveengineering`, `minecolonies`, `supplementaries`. Bedspreads only special-cased
`alexsmobs`; customizableelytra special-cases none of them by namespace.

## Key technical facts learned (MC 1.19.2 banner texture pipeline)

- `net.minecraft.world.level.block.entity.BannerPattern` is registry-based even in 1.19.2.
  `BannerPattern.location(ResourceKey, boolean isBanner)` =
  `new ResourceLocation(key.location().getNamespace(), "entity/" + (isBanner?"banner":"shield") +
  "/" + key.location().getPath())` — already fully namespace-generic. `Registry.BANNER_PATTERN`
  is the registry accessor (`.registryKeySet()`, `.holders()`).
- **There IS a dedicated banner atlas**: `Sheets.BANNER_SHEET =
  new ResourceLocation("textures/atlas/banner_patterns.png")`, with
  `Sheets.BANNER_MATERIALS`/`getBannerMaterial(ResourceKey)` pre-populating a `Material` for
  every registered pattern at atlas-stitch time. **But neither broken mod uses it** — both
  bedspreads and customizableelytra build their pattern `Material` against
  `TextureAtlas.LOCATION_BLOCKS` instead (confirmed byte-identical to `InventoryMenu.BLOCK_ATLAS`
  — both are literally `new ResourceLocation("textures/atlas/blocks.png")`, cross-referenced in
  decompiled vanilla source: `TextureAtlas.LOCATION_BLOCKS = InventoryMenu.BLOCK_ATLAS`). Their
  curated/bundled textures work only because each mod separately stitches its own curated set
  into the block atlas at texture-reload time (customizableelytra has its own
  `AtlasStitchingHandler` for this, not inspected in detail — not needed once patcherMod stitches
  the generic set itself).
- Consequence: simply resolving the *correct* `<originNamespace>:entity/banner/<path>` location
  is not enough on its own — that sprite lives on `Sheets.BANNER_SHEET`, not
  `TextureAtlas.LOCATION_BLOCKS`, so it wouldn't be found by either mod's existing (unchanged)
  `new Material(TextureAtlas.LOCATION_BLOCKS, ...)` call. Fixed by adding a
  `TextureStitchEvent.Pre` listener (see below) that stitches every registered pattern's real
  texture into the block atlas too, rather than trying to reroute either mod through a second
  atlas.
- Reobfuscation/mapping rules for this project (comment in `patcherMod/build.gradle` ~line 75):
  no Mixin refmap, always runs reobfuscated. Regular Java method/field references in a mixin
  (or any) class body can use clean Mojmap names — `reobfJar` rewrites them to SRG automatically
  (**empirically confirmed**: decompiled the actual built+reobfuscated jar and every vanilla
  reference — `TextureAtlas.location()`→`m_118330_`, `.LOCATION_BLOCKS`→`f_118259_`,
  `Registry.BANNER_PATTERN`→`f_235736_`, `.registryKeySet()`→`m_214010_`,
  `BannerPattern.location(...)`→`m_222697_`, `Material.sprite()`→`m_119204_`,
  `TextureAtlasSprite.getName()`→`m_118413_`, `MissingTextureAtlasSprite.getLocation()`→
  `m_118071_`, `ResourceKey.location()`→`m_135782_`, `ResourceLocation.getNamespace()`→
  `m_135827_`, `.getPath()`→`m_135815_` — all match the SRG names the real shipped
  bedspreads/customizableelytra jars use natively, and all cross-checked directly against
  `srg_to_official_1.19.2.tsrg` at
  `~/.gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/
  1.19.2-20220805.130853/srg_to_official_1.19.2.tsrg`). Mixin annotation string selectors
  (`@Inject(method=...)`, `@At(target=...)`) naming a *vanilla/Forge* method still need SRG form
  + `remap=false` (not covered by reobfJar, opaque strings). Selectors naming a *third-party
  mod's own* method/class use the plain declared name (mods aren't SRG-obfuscated). Class/package
  names are never SRG-renamed, only members.
- `-AdisableTargetValidator=true` (in build.gradle) means the Mixin annotation processor does
  **not** validate `@Mixin`/`@Inject`/`@Redirect` targets at compile time against the dev-mapped
  classes — a wrong method/field name would only surface as a runtime Mixin apply failure, not a
  build error. Compile+reobf success alone doesn't prove the mixins will apply; only an actual
  game launch does. (Confidence here is still high given the byte-for-byte cross-check above, but
  this is the one thing genuinely unverified.)
- Decompiler: `~/.gradle/caches/forge_gradle/maven_downloader/net/minecraftforge/forgeflower/
  1.5.605.9/forgeflower-1.5.605.9.jar` (`java -jar forgeflower.jar <inputDir> <outputDir>`).
  Mapped-official vanilla/Forge class tree (clean names, javap-only, not decompiled to .java) at
  `DecompiledMods/forge-1.19.2-43.3.9_mapped_official_1.19.2/` in the instance root.

## Implementation (final)

1. **`patcherMod/libs/bedspreads-forge-6.0.0+1.19.2.jar`** and
   **`patcherMod/libs/customizableelytra-1.19.0-1.7.4.jar`** — copied in so
   `compileOnly fileTree(dir: 'libs', include: '*.jar')` puts their classes on the compile
   classpath.
2. **`patcherMod/src/main/java/.../compat/BannerPatternAtlasCompat.java`** (new) — client-only
   `@Mod.EventBusSubscriber`, `TextureStitchEvent.Pre` handler: when the stitching atlas is
   `TextureAtlas.LOCATION_BLOCKS`, adds `BannerPattern.location(key, true)` for every
   `Registry.BANNER_PATTERN.registryKeySet()` entry via `event.addSprite(...)`. This is what
   makes the two mixins' fallback locations actually resolvable.
3. **`patcherMod/src/main/java/.../mixin/BedspreadsBannerPatternMixin.java`** (new) — `@Mixin`
   on `DecoratedBedBlockEntityRenderer`, `@Redirect` on the one
   `new ResourceLocation("bedspreads", ...)` call inside `renderPatterns` (a static method), at
   `@At(value="NEW", target="Lnet/minecraft/resources/ResourceLocation;")`. Parses the origin
   namespace/path back out of the string bedspreads already built (`"entity/<ns>/<path>"`) and
   returns `new ResourceLocation(originNamespace, "entity/banner/" + originPath)` — a full,
   unconditional override (safe: bundled textures are plain same-shape relocations).
4. **`patcherMod/src/main/java/.../mixin/CustomizableElytraBannerPatternMixin.java`** (new) —
   `@Mixin` on `BannerCustomizationHandler` (not `CustomizableElytraItem` — needed to stay at the
   call site where the real `ResourceKey<BannerPattern>` is still in scope, since
   customizableelytra's own mangled location drops the origin namespace and can't be reversed).
   `@Redirect`s the `CustomizableElytraItem.getTextureLocation(ResourceKey)` call inside
   `render`: calls through to the original (curated) result, probes it with
   `new Material(TextureAtlas.LOCATION_BLOCKS, curated)` (matching the atlas `render` itself
   binds against), returns it unchanged if present, otherwise falls back to
   `new ResourceLocation(key.getNamespace(), "entity/banner/" + key.getPath())` from the real
   `ResourceKey`. Non-destructive: only ever changes behavior for patterns that were already
   rendering as nothing.
5. **`creativeengineering.mixins.json`** — both new mixins added to the `"client"` array
   (alongside the other client-only rendering mixins).
6. Built (`./gradlew build` from `patcherMod/`), reobfuscation succeeded, and the resulting
   `patcherMod/build/libs/creativeengineering-1.0.0.jar` was copied over
   `mods/creativeengineering-1.0.0.jar` to actually deploy it.

## Verification done vs. still needed

Done: compiles clean, `reobfJar` succeeds, every vanilla member reference in the reobfuscated
jar was decompiled/javap'd and matches the real SRG names (cross-checked against the actual
shipped bedspreads/customizableelytra bytecode AND the canonical SRG mapping table — see above).
`creativeengineering.mixins.json` inside the built jar correctly lists both new mixins under
`"client"`.

Still needed (next session, or whenever the user next launches the pack): actually launch the
game and confirm (a) no Mixin apply crash at startup — check `logs/latest.log` for
"Error occurred while handling event" or a Mixin `ClassNotFoundException`/apply failure near
these three new classes' names; (b) a vanilla-pattern banner on a decorated bed and on a
customizable elytra still renders correctly (regression check); (c) a banner using a pattern from
one of the 6 confirmed modded-pattern mods (easiest to test: `supplementaries` or
`immersiveengineering`, both already installed) now renders on both a decorated bed and a
customizable elytra instead of blank. See memory `debugging-with-latest-log` for the general
log-first-not-static-code-guessing approach this project already follows.

See also [patchermod-coremod](patchermod-coremod.md) for general patcherMod mixin/build conventions this followed.
