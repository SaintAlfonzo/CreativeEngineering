# exposure-backport

> development/exposureBackport: 1.9.19 feasibility, the mod, login-packet null kick, addon refmap crash

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Merged 2026-10-19 from 4 memories (index compaction).

## exposure-backport-mod

Scaffold built 2026-10-01; **base mod (`exposure`) ported the same day** (2nd session). The user moved both in-tree mods to `development/` (root settings.gradle sets projectDir; .gitignore allow-list (/development/*/build etc.), tools/build_deploy.py IN_TREE_MODS (DEVELOPMENT dir), perf scripts, pattern-compat generators, java_test_cp.sh and doc links were repointed 2026-10-01 at the user's request; todo/ notes left as historical).

**Base port state (2026-10-01):** all 1.9.19 code compiles; dev client reaches main menu (no mixin/model errors); reobf jar boots a scratch dedicated server alone and with the pack's 339 server mods (KubeJS plugin loads, datapack registries work, camera stand/frame summon+save). NOT tested in game: capture, lightroom, albums, frames, projector, GUIs - ask the user to test via `./gradlew :exposureBackport:runClient`. 

**Polaroid ported 2026-10-01** (same tools, run with the source set as dest; its hand-ported files are in hand_ported.txt under an exposure_polaroid header): boots on a scratch server (recipes/advancements load, which also fixed base camera_stand advancement) and dev client (models + its ItemRendererMixin apply). Crafter in its recipe -> dropper via port_data.py ITEM_SUBSTITUTES (shaped keys now warn on missing 1.19.2 items). No network channel (upstream has no packets). Next: user in-game tests of Expanded/Polaroid/base; Catalog on hold.

**How it was ported** (details in exposureBackport/README.md): Vineflower decompile + `tools/port_upstream.py` (rule-based rewrites; `hand_ported.txt` protects hand edits, `port_excludes.txt` lists dropped upstream files) + `tools/port_data.py` (data -> 1.19.2) + a `backport` package (StreamCodec, NBT-backed DataComponentType, PayloadChannel with packet splitting, GuiGraphics + 1.20 widget API, screen bridge classes). Lessons: never touch client classes from common code even as a method argument type (verifier loads LocalPlayer on servers - happened in Backport.getTooltipFromItem); KubeJS 1902 instantiates a plugin once per mod in the jar (guard); ForgeGui overrides Gui.render (use events); 1.19.2 Mixin can't inject into interface methods.

**Expanded ported 2026-10-01** (user: "continue with expanded, i dont want catalog at this point"):
- What was checked: the scratch server (palette export, dungeon loot via the merged global loot modifiers) and the dev client (no model/texture errors).
- What wasn't: anything in game, e.g. palette capture, filters on the camera, hicap/hires films, instant slides, Create developing.
- **Build-time resource merge** in build.gradle: addon tags + global_loot_modifiers.json are merged into main's processResources output, and any other shared path fails the build. One jar = one resource pack, so these would otherwise collide.
- Upstream bug fixed: `is_polaroid_loaded` is inverted, so upstream would register the instant slides as plain items.
- Shaders were dropped (byte-identical to 1.19.2 vanilla). port_data.py has `--exclude`.
- The jar is still version -01 (1.0.3 undistributed). Reinstalled to mods/ and pack/ regenerated.
- The user confirmed the photo-stack and stand film-count fixes in game the same day.

**In-game bugs fixed 2026-10-01 (jar reinstalled to mods/ as same -01 version; deploy/latest 1.0.3 copy is STALE until restaged with `--version 1.0.3`):**
- Photo/polaroid stack screen stuck on the top photo. Cause: `ItemAndStack.equals` compared stacks by identity, and NBT-backed components decode new stacks on every read, so `PhotographScreen.tick` refreshed and called `setPage(0)` every tick. Fix: `ItemStack.matches` (ItemAndStack added to hand_ported.txt).
- Camera stand film count went stale until the menu was opened. Cause: `port_upstream.py` stripped `force` from `entityData.set(..., true)`, and 1.19.2's set() skips a stack mutated in place (same instance). Fix: `Backport.setEntityData(data, key, value, force)` + `mixin/SynchedEntityDataAccessor`; the tool rule now emits that call.
- General lesson: watch for anything upstream compares by identity on component-derived objects, and for 1.20+ `force`/overload args the port tool dropped.

**User decisions (2026-10-01):**
- The mod is called exposureBackport, with jar `exposureBackport-<ver>.jar` and version `1.9.19-ce-19.2-01` (upstream ver - ce - MC 19.2 - port build).
- It is one jar with all three addons inside.
- **No 1.6.0 world compatibility**: "i want everything from 1.9 so the addons work as easy as possible".

**Design I chose (flagged to the user):**
- Mod ids stay upstream: `exposure`, `exposure_expanded`, `exposure_polaroid`, `exposure_catalog`. They are 4 `memory `mods`` in one mods.toml, each with its own `@Mod` class.
- Packages and class names stay upstream (`io.github.mortuusars.*`, Expanded `dev.titanite.sparkwave.exposureexpanded`). `neoforge/` becomes `forge/`.
- Each addon is a Gradle source set (src/expanded, src/polaroid, src/catalog) compiled against main, so main can't import addon code. Allowed edge: expanded -> polaroid.
- MixinGradle refmaps are per source set. Mixin configs use upstream names (neoforge -> forge) and are listed in `mixin_configs` in gradle.properties, which feeds both the manifest and the dev runs.
- Probe mixins proved that dev runs apply mixins and that the refmaps map to SRG; the probes were removed afterwards.
- `replaces_jars=exposure-1.19.2-*-forge.jar`, so installToMods moves stock Exposure out of mods/.

**Reference sources:** decompiled into `DecompiledMods/<name>-src/` (git-ignored): 1.9.19 (439 java files), expanded (7), polaroid (31), catalog (48), 1.6.0 (247).
- Upstream doesn't use MixinExtras (it is only bundled as a nested jar); the pack has 0.5.4 via other mods.
- Conventions and the redo-decompile command are in exposureBackport/README.md.

**Installed 2026-10-01** (user: "put the jar in the server mods and update server side, docs"): installToMods moved exposure-1.19.2-1.6.0-forge.jar + the 1.6.0 config/exposure-*.toml (replaced by 1.9.19 defaults from the dev run) + an instance.json backup to development/exposureBackport/jar_archive/displaced/. mod_sides row = mod id exposure (first memory `mods` id; tools match on it), instance.json userAdded entry, pack/ regenerated, IN_TREE_MODS marks it required. Exposure-Create Compatibility resource pack is off (it overrides 1.6.0 camera models; would break 1.9.19's).

**License:**
- GPLv3 (`mod_license=GNU GPL3`, LICENSE = GPL-3 text).
- The upstream MIT notice for mortuusars and Sparkwave/JohnRamRu is in `NOTICE-upstream-MIT.txt`; MIT was confirmed on all four CurseForge pages.
- Both files ship in the jar.

Feature verdicts and 1.21 -> 1.19 gaps: [exposure-backport](exposure-backport.md). Build layout: [root-gradle-build](root-gradle-build.md).

Writeup: feature_writeup/exposure.md (EX- tests; EX-11/EX-17 passed; EX-50 old-world check before deploying 1.0.3). Jar checks: `python3 development/exposureBackport/tools/check_jar.py`. Open pack decision: the bumpy filter is unobtainable (its only loot chest, trial chambers, isn't in 1.19.2).

`-02` added the null-kick + refmap fixes and the fifth mod `exposure_extras` (camera bags, [camera-bag-design](camera-bag-design.md)); display name "Exposure (Backport & Extras)". Which jar shipped in which pack: README index Exposure row and [exposure.md](../exposure.md).

**2026-10-05: album text boxes couldn't be typed in (fixed, `-03`, in mods/).**
- Cause: 1.19.2's `AbstractContainerEventHandler.setFocused(listener)` only stores the field. 1.20+ also calls `widget.setFocused(true/false)`. Upstream `TextBox` gates `keyPressed`/`charTyped` on `isFocused()`, so the album note boxes and the signing title box dropped every key.
- A second trap: 1.19.2's `setInitialFocus` calls `setFocused` and then `changeFocus(true)`, which toggles the flag back off.
- Fix: `backport/gui/AbstractWidget.moveFocus` + `setFocused`/`setInitialFocus` overrides in BackportScreen and BackportContainerScreen.
- The flag is set only for `TextBox`. Buttons and photo slots draw highlighted while `isHoveredOrFocused()`, so flagging them would leave them lit after a click.
- 1.0.5 was staged with `-02`, so it needs restaging with `--version 1.0.5` to ship this.
- General lesson: any upstream widget relying on 1.20 focus propagation needs this treatment.

**2026-10-14: stale viewfinder could eat clicks (fixed, `-04`, in mods/ + instance.json).**
- Found while triaging a player's "mouse clicks stopped working" report (debug/latest.log). It is NOT proven to be that player's cause: on a server it only blocks left clicks with viewfinder_attacking=false or in selfie mode.
- Bugs: `MouseButton.Pre` fires before screens get the click, so a viewfinder cancelled clicks inside GUIs too. `CameraClient.activeViewfinder` is static and was never cleared on logout, respawn or dimension change. The client tick's "camera inactive -> remove" check went through getActiveExposureCamera(), which already returns null for inactive cameras, so it never fired.
- Fixes: `MouseHandler.buttonPressed` requires `screen == null` (MouseHandler added to hand_ported.txt). PlayerMixin checks the raw field. `PlayerClientHooks.onLocalPlayerTick` drops the viewfinder when the ticking player has no camera. A LoggingOut handler clears it too.
- Build trap seen again: the polaroid refmap had vanished from build/tmp and the jar backstop failed the build. Fix: `./gradlew :exposureBackport:compilePolaroidJava --rerun`.

## exposure-backport-feasibility

Assessment (2026-09-30). **Superseded in scope on 2026-10-01:** the user chose a full 1.9.19 port with all addons in one jar, with no 1.6.0 compatibility. See [exposure-backport](exposure-backport.md). The per-feature notes below still describe the 1.21 -> 1.19 gaps. The user dropped four 1.21.1 jars, extracted as **.class only** (not source), into
`DecompiledMods/`: exposure-neoforge-1.21.1-1.9.19, exposure_catalog 1.1.2, exposure_expanded 1.0.1,
exposure_polaroid 1.1.6. All MIT. Decompile with ForgeFlower:
`~/.gradle/caches/forge_gradle/maven_downloader/net/minecraftforge/forgeflower/1.5.605.9/forgeflower-1.5.605.9.jar`
(zip the class dir, then `java -jar ff.jar -dgs=1 -rsy=1 in.jar outdir/`). The output went to the session scratchpad, so it is gone now.

**Base verdict:** 1.9.19 is NOT "roughly the same" as 1.6.0. It is a rewrite (data components, datapack registries for
lens/filter/color_palette, camera abstraction with Attachment/Shutter/CameraHolder) and adds Camera Stand (~3.3k lines),
Photograph Frames 1-3 size/glass/glow (~860), Interplanar Projector (URL/file image), high-sensitivity films, FilmStyle
(contrast/levels/HSB/color balance/noise, ~950 lines of pure pixel math), color palettes + dithering (~570), self-timer,
rename menu, 12 new advancements, and loot injection. 1.9.19 REMOVED bare-photograph wall hanging (1.6.0 PhotographEntity),
which matters for existing worlds. None of the addons can drop in: every one depends on 1.9.x internals.

**Per-mod verdicts:**
- **Expanded (easy, best value):** the 2 lenses (telescopic 200-300mm, panoramic 10-18mm, within 1.6.0's 10-300mm limit) and 16 filters need no Java (items, lens and filter JSON files, tags).
  - The hicap (32-frame) and hires (640px) films only need film items carrying `FrameCount` / `FrameSize` NBT.
  - The Game Boy/NES/C64/CGA palette films are medium: about 4 mixins plus a palette loader. Port 1.9.19's `client/capture/palettizer`.
  - Its 26 post + 65 program shaders are redundant on 1.19.2.
  - Its Create sequenced-assembly recipes are covered by 1.6.0's spout-developing config.
  - Loot goes through LootJS ([loot-system](loot-system.md)). The 6 instant slides only make sense with Polaroid.
- **Polaroid (medium, ~1750 lines upstream):**
  - Instant Camera = a patcherMod `CameraItem` subclass with one slide slot and no attachments menu. Its stats: built-in flash, 20-40mm, 240px, crop 0.75, 40-tick cooldown; it prints a photo into the inventory (merging into stacked photos) and consumes a slide.
  - The slides need a film-style capture component: contrast 0.2, levels (0,135,255,25,255), HSB (0,0.05,0.05), color balance (0.03,0.01,-0.01); the high-sensitivity slides add sensitivity 2.0 and noise 0.05.
  - Slides must be blocked from the normal camera's film slot and from Create spout developing.
  - The custom viewfinder (exposure slider, slide counter) is optional and needs mixins, because 1.6.0's viewfinder is static.
  - Skip the 3D in-hand poses and camera-stand support.
- **Catalog (~3200 lines):**
  - Server side is medium: a cache and 54px thumbnails, plus query, delete and close packets. Needs a delete added to the 1.6.0 storage (the file and the DimensionDataStorage cache).
  - `CatalogScreen` (1230 lines, 1.20+ GUI APIs) is a full rewrite.
  - A cheaper alternative is op commands (`/exposure list`, `/exposure delete`).

**Other 1.9.19 base features:**
- Easy: film styles and high-sensitivity films (shared with Polaroid); loot.
- Medium: palettes (shared with Expanded); the Interplanar Projector (1.6.0 already has `/exposure load <size> <path> <id>` for files; URL loading needs a domain allowlist); Photograph Frames (start from 1.6.0's `PhotographEntity`); self-timer.
- Advancements: most can use 1.6.0's FILM_FRAME_EXPOSED trigger with its entities-in-frame predicate; the "printed", Enderman and projector ones need new triggers.
- Camera Stand: hard, not recommended.

**Suggested order (from before the full-port decision):**
1. Expanded lenses, filters, hicap and hires films via KubeJS (item_dict/NCH/global.tags, see memory `centralize-recipes-tags-items`).
2. The film-style capture component in patcherMod.
3. Exact palettes.
4. The Instant Camera.
5. Frames and the projector, if wanted.
6. Catalog last, or as commands.

**1.6.0 hook points (verified in source):**
- `CameraItem.createCapture(Player, ItemStack, String, boolean)` (protected) builds the components and converter for every shot. Inject here for palette/film-style/sensitivity.
- `ICaptureComponent.modifyImage/modifyPixel`; `Capture.setConverter(IImageToMapColorsConverter)`, `setSize`, `cropFactor`.
- Pixels are map-color bytes. The byte -> color lookup is `MaterialColor.getColorFromPackedId` at `ExposureImage.getPixelABGR`, `ExposureExporter` (~l.136) and `ChromaticSheetItem` (~l.167). `ExposureSavedData.properties` (CompoundTag) round-trips client<->server, so it can store a palette id.
- Map palette approximates retro palettes poorly (CGA max err 87, NES 105), so exact palettes need the properties + redirect approach.
- `IFilmItem` already honors NBT `FrameCount` / `FrameSize` (1-2048), so hicap/hires films need no mixin.
- Lenses: `data/exposure/lenses/*.json` `{item, focal_range}`. Filters: CLIENT `assets/exposure/filters/*.json` `{item, shader}`. Both loaders accept ONLY namespace `exposure`. Slots gate on `#exposure:lenses` / `#exposure:filters`. Expanded's 16 filters use vanilla post shaders that exist byte-identical in 1.19.2.
- Camera attachments store the full ItemStack (count kept), so a 12-slide Instant Camera magazine works. A photograph = `exposure:photograph` with tag = frame compound (LightroomBlockEntity ~l.526).
- Almost all checks are `instanceof CameraItem`. Only `ItemRendererMixin` (GUI model) and the `camera_state` ItemProperties registration are id-specific.
- Server storage has `getAllIds()`/`getOrQuery()`/`put()`, but no delete. Catalog needs that, and a 1.19.2 GUI rewrite (1.21 GuiGraphics/sprites/Tooltip).
- KubeJS 1902 has `StartupEvents.registry(...).createCustom(id, supplier)`, so Java-class items can be registered from JS.

Subclassing Exposure classes from patcherMod hits the SRG/Mojmap libs split (see [patchermod-coremod](patchermod-coremod.md)); consider
`fg.deobf` for the exposure jar.

## exposure-login-packet-null-kick

On 2026-10-02, after the 1.0.3 deploy, every player was kicked when joining CreEngServer. The kick message was "Internal Exception: io.netty.handler.codec.EncoderException: NullPointerException: Cannot invoke JsonElement.getAsJsonPrimitive() because input is null".
- The stack trace is in the **server** latest.log, under "Error receiving packet 37" (ClientboundLoginPacket). The client log shows nothing.

**Cause:** exposureBackport syncs three datapack registries (color_palette, lens, filter). Lens and Filter use `BackportCodecs.ITEM_PREDICATE`, which is `jsonBacked` on vanilla `ItemPredicate.serializeToJson`.
- That serializer writes `"count": null`, `"durability": null` and `"nbt": null`.
- JsonOps passes JsonNull map values on as Java null, so converting the value to NbtOps for the login packet throws an NPE.
- Single player and the scratch-server boot never encode that packet, which is why only a real client joining a dedicated server hit it.

**Fix (2026-10-02):** `jsonBacked` now runs `BackportCodecs.withoutNulls` on the encoded JSON, and a whole-value null becomes `{}`. A headless test (scratchpad PredTest, mapped classpath via tools/java_test_cp.sh plus exposureBackport build/classes) showed the old path throwing the NPE and the new path round-tripping through NBT bytes.
- Shipped as `-02` (pack 1.0.4).

**How to apply:** any Codec built on vanilla 1.19.2 `serializeToJson` output (predicates, MinMaxBounds) that ends up in a synced registry or packet must strip JSON nulls. To test a networked change, have a real client join a dedicated server.

Related: [exposure-backport](exposure-backport.md), [recipe-book-type-desync](recipe-book-type-desync.md), [deploy-pipeline](deploy-pipeline.md).

## exposure-addon-refmap-missing

2026-10-02: real (non-dev) clients crashed at startup with exposureBackport 1.0.3.
- **Crash report:** NoClassDefFoundError RenderType.
- **latest.log:** "Mixin apply failed exposure_polaroid-common.mixins.json:client.ItemRendererMixin ... @ModifyVariable could not find any targets matching 'render(...)'", plus "Reference map 'exposure_polaroid.refmap.json' ... could not be read".

**Cause:**
- MixinGradle's addMixinsToJar packs every source set's refmap from `build/tmp/<compileTask>/`, and silently skips any that is missing.
- That file is not a Gradle-tracked output, so once it vanished while compilePolaroidJava stayed UP-TO-DATE, every later jar shipped without it.
- Dev runs use Mojang names, so they never show the problem; servers never load ItemRenderer.
- The expanded and catalog configs have no mixins, so their "could not be read" warnings are harmless.

**Fix in development/exposureBackport/build.gradle:**
- Each mixin source set's compile task declares `<task>-refmap.json` as an output, so a missing refmap now re-runs the compile.
- `jar` doLast fails the build if a mixin config that lists mixins has no refmap in the jar.
- Both were verified by deleting the refmap (the compile re-ran) and running with `-x compilePolaroidJava` (the build failed).
- A throwaway client (memory `client-launch-for-defaults`) with the rebuilt jar reached the main menu with no mixin errors.

**Shipping rule:** a rebuilt jar needs a version bump, installToMods and a deploy, together with [exposure-backport](exposure-backport.md). Current jar state: feature_writeup/exposure.md and deploy.md.

**How to apply:** when a client says "mixin could not find any targets" for an in-tree mod, first check that the jar actually contains the refmap named in the mixin config.

Related: [exposure-backport](exposure-backport.md), [root-gradle-build](root-gradle-build.md).
