# exposureBackport

Exposure **1.9.19** and its addons **Exposure: Expanded 1.0.1**, **Exposure Polaroid 1.1.6** and
**Exposure Catalog 1.1.2** (NeoForge 1.21.1, all MIT) ported to **Forge 1.19.2**, plus **Exposure Extras**
(`exposure_extras`, this pack's own additions), in one jar: `exposureBackport-<mod_version>.jar`. The base mod's
display name is **Exposure (Backport & Extras)** (`mod_name`).

Status (2026-10-01):
- **Base mod (`exposure`) ported:** all of Exposure 1.9.19 compiles for 1.19.2. The dev client reaches the main menu.
  The reobfuscated jar boots a dedicated server both alone and with the pack's server mods, and the camera stand and
  photograph frame summon and save. Gameplay (capture, lightroom, albums, frames, projector, GUIs) is **not yet tested
  in game**.
- **Polaroid (`exposure_polaroid`) ported:** instant camera and the four instant slides. Recipes and advancements load
  on a dedicated server (which also fixes the base camera stand advancement, which names the instant camera), and the
  dev client loads its models and mixin. Not yet tested in game.
- **Expanded (`exposure_expanded`) ported (2026-10-01):** 2 lenses, 16 filters, the Game Boy/NES/C64/CGA palette
  films, hicap (32-frame) and hires (640px) films, and 6 instant slides (4 palette + 2 hires 480px). On a scratch
  server its palettes load into the registry (`/exposure palette export`) and its dungeon loot rolls through the
  merged global loot modifiers; the dev client loads its models, textures and lang. Not yet tested in game.
- Catalog (`exposure_catalog`) is still empty, and on hold (user, 2026-10-01: not wanted for now).
- **Extras (`exposure_extras`, 2026-10-02):** the leather/iron/netherite camera bags, moved here from patcherMod
  (they were `creativeengineering:*_camera_bag`; a MissingMappingsEvent remap carries old bags over). Slot counts
  are `config/exposure_extras-common.toml` `[camera_bag]`. What a bag accepts is the item tag
  `#exposure_extras:camera_equipment`: defaults in `src/extras/resources/data/exposure_extras/tags/items/`, pack
  adjustments in kubejs `server_scripts/tags/equipmenttags.js`. No recipes in the jar (kubejs
  `equipment/camera_bag.js`). The bag item models and GUI use Spice of Life (`solapplepie`) textures.
- **Installed in `mods/` (2026-10-01)**, replacing stock Exposure 1.6.0 (moved to `jar_archive/displaced/`, along
  with its 1.6.0 `config/exposure-*.toml` files, which were replaced with the 1.9.19 defaults). Shipped in pack 1.0.3.

## Goals

- Port 1.9.19 itself, not a 1.6.0 upgrade. Use 1.6.0 code (already written for 1.19.2) wherever 1.21
  APIs don't exist in 1.19.2, e.g. rendering or `GuiGraphics`.
- No 1.6.0 world compatibility: 1.6.0 photos, cameras and saved exposures are not migrated.
- Keep the upstream mod ids, packages and class names so the addons port with as few changes as possible,
  and each ported file can be diffed against its upstream original.
- The upstream source sets stay a faithful port. Pack recipes, tags and loot go through KubeJS (`global.NCH`,
  `global.tags`, LootJS); Java tweaks to other mods go in patcherMod. New Exposure-related content of our own goes
  in `src/extras` (mod id `exposure_extras`, package `com.SaintAlfonzo.exposure_extras`).

## Version

`mod_version` in `gradle.properties`: `<upstream Exposure version>-ce-<MC 1.19.2>-<port build>`, e.g.
`1.9.19-ce-19.2-01`. All five mods carry the jar's version.

## Layout

One Gradle source set per mod. All five go into the one jar and into the dev runs.

| Source set | Mod id | Upstream | Package |
|---|---|---|---|
| `src/main` | `exposure` | Exposure 1.9.19 | `io.github.mortuusars.exposure` |
| `src/polaroid` | `exposure_polaroid` | Exposure Polaroid 1.1.6 | `io.github.mortuusars.exposure_polaroid` |
| `src/catalog` | `exposure_catalog` | Exposure Catalog 1.1.2 | `io.github.mortuusars.exposure_catalog` |
| `src/expanded` | `exposure_expanded` | Exposure: Expanded 1.0.1 | `dev.titanite.sparkwave.exposureexpanded` |
| `src/extras` | `exposure_extras` | none (this pack's own) | `com.SaintAlfonzo.exposure_extras` |

- **Separation is enforced by the compiler.** Addons compile against `main`, and `expanded` also against
  `polaroid` (upstream's optional instant-slide support). `main` sees no addon code, so the base mod can't
  depend on an addon by accident. Add an allowed edge in `build.gradle` (`addons` map) if one is ever
  really needed.
- **One `mods.toml`** (`src/main/resources/META-INF/`) declares all five mods. Each mod has its own `@Mod`
  class, configs, network channel and resource namespace (`assets/<mod id>/`, `data/<mod id>/` in its
  own source set's `resources/`).
- **Shared resource paths are merged at build time.** The jar is one resource pack, so an addon file at the same
  path as one in `main` would replace it or be hidden by it. Upstream's separate jars rely on tags and global loot
  modifiers merging across packs. `build.gradle` merges each addon's tag / `global_loot_modifiers.json` into
  main's `processResources` output (list union, `replace` OR'd) and leaves it out of the addon's own output; any
  other shared path fails the build. Expanded adds to 5 `exposure:` item tags and the global loot modifier list.
- **Mixins.** Each source set has upstream's mixin configs, renamed from neoforge to forge:
  `exposure-common` / `exposure-forge`, `exposure_polaroid-common` / `-forge`, `exposure_catalog-common` /
  `-forge` and `exposure_expanded`. Each source set gets its own refmap (`<mod id>.refmap.json`).
  - When you add a mixin config, list it in `mixin_configs` in `gradle.properties`, which feeds both the
    jar manifest and the dev runs.
  - Mixins use Mojang names; MixinGradle writes the refmap, so they work in the dev runs and in the game.
    That's different from patcherMod, which has no refmap and uses SRG names.
  - Until a config has mixins, Mixin logs "Reference map ... could not be read". That's harmless.
  - Upstream bundles MixinExtras but doesn't use it. The pack already gets MixinExtras 0.5.4 through
    other mods if a port needs it.

## Upstream -> port conventions

- **Same path for loader-independent code.** `io/github/mortuusars/exposure/X.java` upstream goes to
  `src/main/java/io/github/mortuusars/exposure/X.java`.
- **`neoforge/` becomes `forge/`.** `ExposureNeoForge` becomes `forge/ExposureForge` (the `@Mod` class);
  Expanded's `neoforge/ExampleModNeoForge` becomes `forge/ExposureExpandedForge`.
- **Architectury `@ExpectPlatform` pairs** (`PlatformHelper` + `neoforge/PlatformHelperImpl`, `Register` +
  `RegisterImpl`): keep the common class and put the Forge code directly in it. With only one loader,
  the extra layer isn't needed.
- **1.21 data components** (`Exposure$DataComponents`) become ItemStack NBT on 1.19.2. Keep one
  helper class per component so call sites still read like upstream.
- **Datapack registries** (lenses, filters, color palettes) don't exist as such on 1.19.2. Load them
  with reload listeners from the same `data/<namespace>/<registry>/` paths.
- **Expanded uses Architectury API** (`DeferredRegister`, `RegistrySupplier`). Port it to Forge
  `DeferredRegister` / `RegistryObject`.

## The backport layer (`io.github.mortuusars.exposure.backport`)

Port-only code that gives upstream the 1.20/1.21 APIs it expects, so most upstream files only change their imports:

| Package | What |
|---|---|
| `codec` | `StreamCodec`, `ByteBufCodecs` (+ vanilla `X.STREAM_CODEC` constants), `BackportCodecs` (sized lists, `validate`, codecs for item/location predicates, int bounds, components, lenient SNBT, `getOrThrow`) |
| `component` | `DataComponentType`: 1.20.5 item components stored as stack NBT under their id (`"exposure:camera_film"`), `TYPE.get(stack)` instead of `stack.get(TYPE)`; per-item defaults replace `Item.Properties.component`. `DataComponents` maps the vanilla ones Exposure uses onto `display.Name`, the root tag and `EntityTag` |
| `network` | `CustomPacketPayload` and `PayloadChannel`: every payload over one Forge SimpleChannel, direction-checked, and split/reassembled when over 1.19.2's limits (32 KiB serverbound, 1 MiB clientbound; NeoForge does this for upstream) |
| `gui` | `GuiGraphics` over PoseStack; the 1.20 widget API under vanilla names (`AbstractWidget`, `Button`, `ImageButton`, `WidgetSprites`, `Tooltip`, `Checkbox`, ...); `BackportScreen`/`BackportContainerScreen` with the 1.20.2 render contract. GUI sprites are read from `textures/gui/sprites/<path>.png` |
| `client`, `util` | `DeltaTracker`, `ByIdMap`, `Vector2i`, `TriConsumer`; `Backport` statics for missing vanilla methods (`copyWithCount`, `transmuteCopy`, `setEntityData(data, key, value, force)` via `mixin/SynchedEntityDataAccessor`, ...); `BackportRecipes` reads 1.21 and 1.19.2 recipe JSON |

**Where the port differs from upstream** (beyond API translation):
- Gui/crosshair, first-person photographs, item-frame photographs, mouse scroll/press: upstream mixins became Forge
  events (`forge/event/ForgeClientEvents`, `forge/client`); ForgeGui overrides `Gui.render`, so mixing into it does nothing.
- ServerPlayer/LocalPlayer camera overrides are folded into `mixin/PlayerMixin` (no interface injection on FG5:
  upstream's `player.getActiveExposureCamera()` is `((CameraOperator)player)...` here).
- The anvil 150-character projector name limit is in `ServerGamePacketListenerMixin` (1.19.2 checks length there).
- Lectern page count via `WrittenBookItem.getPageCount`; `exposure:tamed` entity sub-predicate via `EntityPredicateMixin`;
  `exposure:frame` item sub-predicate as a Forge custom item predicate (`{"type": "exposure:frame", "frame": {...}}`).
- No brush in 1.19.2: aging takes the agers only (damageable extra ingredients are damaged, not used up). The
  wildlife archivist advancement drops the camel and sniffer.
- `ItemAndStack.equals` compares stack contents (`ItemStack.matches`), not identity: NBT-backed components decode new
  ItemStack instances on every read, so upstream's identity compare made the photograph screen refresh (and reset to
  the top photo) every tick.
- KubeJS 1902: events are `EntityEventJS` wrappers; the plugin guards against being created once per mod in the jar.
- JEI 11 / Create 0.5.1 APIs; Create recipes carry a `forge:mod_loaded` condition.
- Polaroid: the instant camera recipe's crafter (1.21) is a dropper; the Polaroid items are in Exposure's tab in
  registry order (color slide before black and white); upstream's creative-slot sync on hotswap only runs client side
  (upstream also runs it on a server, where it would load client classes). No network channel (upstream has no packets).
- Expanded:
  - The instant slides always register as `InstantSlideItem`. Upstream's `is_polaroid_loaded` is set to
    `false` when Polaroid *is* present, before the item suppliers run, which would make them plain items.
  - `ExposureExpandedLootModifier` isn't ported: its codec is Exposure's `add_table`, which the loot JSON names anyway.
  - The 91 `assets/minecraft/shaders` files aren't shipped. They're 1.21 re-adds of the removed vanilla
    "super secret settings" shaders, and byte-identical to 1.19.2's own.
  - Create developing needs no Expanded code: the base `FillingBySpoutMixin` carries frames for any `FilmItem`.
- Left out: Jade, Real Camera, Shoulder Surfing (stubs; not in the pack), Easy Anvils compat, the dev-only Bugger overlay.

## Porting tools (`tools/`)

- `port_upstream.py <upstream-src> <java-root> [--refresh]`: copies decompiled upstream sources and applies the
  mechanical rewrites (imports to the backport layer, codecs, components, API renames, decompiler artifacts). Never
  touches files in `hand_ported.txt`; skips everything in `port_excludes.txt` (each entry says why).
- `port_data.py <extracted-upstream-jar> <resources-root> [--exclude GLOB ...] [--refresh]`: assets as is, data converted to 1.19.2
  (folder names, item stacks, `set_nbt`, `forge:partial_nbt`, Create 0.5.1 recipes, advancement icons, 1.19.3+ ids
  dropped and reported via `vanilla_1_19_2_ids.json`).
- Commands used for Expanded (from `development/exposureBackport`):
  `tools/port_upstream.py ../../DecompiledMods/exposure_expanded-neoforge-1.0.1-src src/expanded/java` and
  `tools/port_data.py ../../DecompiledMods/exposure_expanded-neoforge-1.0.1 src/expanded/resources --exclude 'assets/minecraft/shaders/*'`.
- `gen_at.py`: `accesstransformer.official.cfg` (Mojang names) -> `META-INF/accesstransformer.cfg` (SRG).
- `errs.py <gradle-log> [path-filter]`: compact javac error listing.

## Reference sources

Decompiled with **Vineflower 1.10.1** into the instance's `DecompiledMods/` (git-ignored); it keeps generics and
decompiles everything ForgeFlower 1.5 couldn't. The older ForgeFlower output is in `<name>-src-forgeflower/`.

- `exposure-neoforge-1.21.1-1.9.19-src/`
- `exposure_expanded-neoforge-1.0.1-src/`
- `exposure_polaroid-neoforge-1.21.1-1.1.6-src/`
- `exposure_catalog-neoforge-1.1.2-src/`
- `exposure-1.19.2-1.6.0-forge-src/`, the 1.19.2 code to borrow from.

Assets and data are next to them in the extracted `DecompiledMods/<name>/` folders. To redo a decompile:

```
# vineflower-1.10.1.jar from Maven Central (org.vineflower:vineflower), sha1 4f48c5947b21f9ebc743e7c80215ee839d3dc668
cd DecompiledMods/<name> && zip -qr /tmp/in.jar . -i '*.class'
java -jar ~/.cache/ce-tools/vineflower-1.10.1.jar -dgs=1 -rsy=1 /tmp/in.jar ../<name>-src/
```

`DecompiledMods/forge-1.19.2-43.3.9_mapped_official_1.19.2-src/` holds the decompiled 1.19.2 Minecraft + Forge
(Mojang names) to check real 1.19.2 signatures against.

## Building and running

From the instance root (or `cd development/exposureBackport && ../../gradlew ...`):

```
./gradlew :exposureBackport:build               # build/libs/exposureBackport-<ver>.jar
./gradlew :exposureBackport:runClient           # dev client, this mod only, in exposureBackport/run/
./gradlew :exposureBackport:runGameTestServer   # headless boot check, exits by itself
./gradlew :exposureBackport:installToMods       # build + put the jar in ../mods
```

`installToMods` also moves the stock `exposure-1.19.2-*-forge.jar` out of `mods/` (same mod id: two jars
would crash on boot), along with older exposureBackport jars, into `jar_archive/displaced/`. When the
version changes, update the jar name in the `instance.json` userAdded entry and the `mod_sides.md` row
(informational: rows match by mod id `exposure`), regenerate `pack/` (`tools/gen_pack_manifest.py`) and build a
deploy (`tools/build_deploy.py`), which lists the old jar as a removal.

## License and credits

exposureBackport is GPLv3 (`LICENSE`). It ports Exposure, Exposure Polaroid and Exposure Catalog by
mortuusars and Exposure: Expanded by Sparkwave and JohnRamRu, all MIT licensed (per their CurseForge pages).
MIT code may be included in a GPLv3 work as long as its notice stays with it:
`NOTICE-upstream-MIT.txt`. The jar ships both files.
