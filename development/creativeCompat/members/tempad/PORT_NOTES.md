# Tempad 3.0.4 port: working notes (member `:backportedTempad`)

Decisions: feature_writeup/immersive_portals.md 2.1.1 (TP-D1..TP-D8). Upstream source: github.com/terrarium-earth/Tempad,
branch `1.21.1` (commit cde43055), MIT code / ARR art (Terrarium License v1, see NOTICE-upstream-MIT.txt).
Status 2026-11-07: phases 1 to 4 written and building (`./gradlew :backportedTempad:build --offline`), common/server and
client side (section 5); not run in game yet (section 9). Phase 2 = workstation family + Portal Setup app, location card,
card wallet, location broadcaster, screening device. Phase 3 = timedoor projector (`RudimentaryTempad*`), timedoor marker
and chronomark (`timedoor_marker/*`, spatial anchor item, `markers` location section), metronome, time twister (and its
stacking on the Tempad), Timeline app. Phase 4 = guide (knowledge projector, Knowledge Repository upgrade and app), Curios slots and worn rendering, FTB Teams access, WTHIT plugin (replaces upstream Jade). Optional compat is guarded by ModList checks.

## 1. Package map (upstream Kotlin -> ours)

Package layout and class names follow upstream. Kotlin top-level extensions became static helpers:

| Upstream | Ours |
|---|---|
| `Tempad.kt` (`Tempad`, `String.tempadId`, `Tempad.server`) | `Tempad` (`Tempad.id(path)`, `Tempad.server()`, `LOGGER`, colours) |
| `api/**` | `api/**` (same names). Kotlin operators: `minusAssign` -> `remove`, `plusAssign` -> `add`, `TempadLocations[...]` -> `get`/`getAll` |
| `ItemStack.chronons`, `BlockEntity.chronons` | `ChrononHandler.of(stack / blockEntity)` |
| `ItemStack.upgrades`, `ItemStack.playerAccess` | `UpgradeHandler.of(...)`, `PlayerAccess.of(...)` |
| `ChrononHandler.tooltip`, `ChrononData` (client/tooltip) | `ChrononHandler.tooltip()`, `api/tva_device/ChrononData` (common: items return it) |
| `ChrononHandler.move(...)` top-level fun | `ChrononHandler.move(...)` static |
| `Player.namedGlobalVec3`, `offsetLocation(...)` | `NamedGlobalVec3.of(player)`, `NamedGlobalVec3.offsetLocation(...)` |
| `Player.getPinnedLocation(ctx)` | `FavoriteLocationAttachment.getPinnedLocation(player, ctx)` |
| `common/registries/ModComponents` (data components) | same class: NBT accessors `getX(stack)` / `setX(stack, v)` |
| `common/registries/ModAttachments` (player) | `common/data/TempadPlayerData` (capability) |
| `ModAttachments.playerPoints` (overworld attachment) | `common/location_handlers/PlayerPointsData` (overworld SavedData) |
| `ModAttachments.metronomeEnergy` (overworld attachment) | `common/data/MetronomeData` (overworld SavedData) |
| `ModAttachments.anchorPoints` (overworld attachment) + top-level `getBlockEntity` in `AnchorPointsHandler.kt` | `common/location_handlers/AnchorPointsData` (overworld SavedData) |
| `AnchorPointsHandler.kt` (handler class) | `common/location_handlers/AnchorPointsHandler` (`tempad:markers`) |
| `common/menu/MetronomeMenu.kt` (two classes) | `common/menu/MetronomeMenu` + `MetronomeMenuData` |
| `common/apps/TimelineApp.kt` (two classes) | `common/apps/TimelineApp` + `TimelineData` |
| `Tempad.kt` capability rows for the projector / metronome block entities | their own `getCapability` (`RudimentaryTempadBE`, `MetronomeBe`) |
| `ItemStack.items` (wallet extension) | `WalletInventory.of(stack)` |
| `common/data/LocationDataAttachment.kt` | `common/data/NamedGlobalPosAttachment` (unused, as upstream) |
| `ScreeningDeviceItem.kt` (two classes) | `common/items/ScreeningDeviceItem` + `ScreeningDeviceAccess` |
| `entity.ageUntilAllowedThroughTimedoor` | `TimedoorEntity.get/setAgeUntilAllowedThroughTimedoor(entity)` |
| `common/utils/CodecUtils`, `Extensions` | `common/utils/CodecUtils` (buf + NBT helpers), `BufCodec`, `Color` |
| Resourceful `Color` | `common/utils/Color` (DEFAULT, RAINBOW, ARGB) |
| `ContentMenuProvider` / `MenuContent` | `TempadApp.openMenu` + `AppContent.encode`, `IForgeMenuType` in `ModMenus` |
| `ModNetworking` (Resourceful Network) | `ModNetworking.CHANNEL` (SimpleChannel `tempad:main`, protocol "1"), `sendToServer`, `sendToPlayer`, `sendToAll` |
| `CommonConfig.java` (Resourceful Config) | `common/config/CommonConfig` (ForgeConfigSpec); synced subset `CommonConfigCache` |
| `ClientConfig.java` | `common/config/ClientConfig` (ForgeConfigSpec, CLIENT, registered by `Tempad`) |
| `InteractionHand.getSlot(player)` | `TempadItem.getSlot(hand, player)` |
| `client/TempadClient.kt` (`object`, `@EventBusSubscriber`) | `client/TempadClient` (`init(modBus)` adds the listeners); `renderType` / `timedoorShader` -> `client/entity/TimedoorRenderTypes` |
| `client/screen/tempad/TeleportScreen.kt` `enum Sorting` | `client/screen/tempad/Sorting` (+ `Sorting.Entry` for Kotlin's `Triple`) |
| Olympus widgets (`Widgets.*`, renderers, `LayoutWidget`, `DropdownBuilder`, `ContextMenu`, modals, colour picker, map) | `client/ui` kit (TP-D4): `Button`, `TextBox`, `Label`, `Renderers`, `Dropdown`, `ScrollList`, `DeleteModal`, `ColorPicker`, `MapWidget`, `AppList`; overlays are `Popup` layers of the screen |
| Resourceful Lib `BaseCursorScreen` (marker screens) | `client/ui/KitScreen` (plain Screen hosting the kit's popups and tooltips) |
| `client/ModEnums.kt` (EnumProxy ArmPose) + `RudimentaryTempadClient.kt` | `client/RudimentaryTempadClient` (`ArmPose.create`, IClientItemExtensions via `RudimentaryTempadItem.initializeClient`) |
| `client/screen/TickingScreen.kt` | not ported yet: only the guide screens use it (phase 4) |
| `GuiGraphics.blitSprite` (1.21 GUI atlas) | `client/ui/Sprites.blit` (texture `textures/gui/sprites/<path>.png`, mcmeta stretch / tile / nine_slice) |
| port glue | `forge/` (UpstreamJars, UpstreamTranslator, TempadCapabilities, Teleports, RequiredLocationUpgradeCondition) |
| shared sources | `earth.terrarium.tempad.shared` (BuiltInPacks, DonorJars; generated from development/shared) |

## 2. Saved data

Item NBT (one key per upstream data component, upstream path as the key; absent key = upstream default):

| Key | Type | Default |
|---|---|---|
| `default_app` | string id | `tempad:teleport` |
| `default_macro` | string id | `tempad:default` |
| `chronon_content` | int | 0 |
| `chronon_content_tempad` | int | 0 |
| `chronon_content_time_twister` | int | 0 |
| `enabled` | bool | true |
| `twister_equipped` | bool | false |
| `installed_upgrades` | `{upgrades: [string id]}` | empty |
| `color` | int ARGB, or "default" / "rainbow" | none |
| `owner` | `{id: int[4], name}` | none |
| `portal_offset` | `{leftRight, upDown, forwardBack, angle, isUpright}` | (-2.5, 0, 0, 0, true) |
| `portal_target` | DirectLocation = NamedGlobalVec3 compound; IndirectLocation = `{accessor, info, provider, id}` | none |
| `locked` | bool | true |
| `selected_portal` | IndirectLocation compound (as `portal_target`) | none |
| `wallet_contents` | list of `{slot: int, item: ItemStack NBT}`, empty slots not written; key removed when empty | 18 empty stacks |
| `access_id` | string id (a `PlayerAccessApi` id) | none (= screening off) |
| `twister_data` | compound: the attached time twister stack's own NBT tag (upstream: its DataComponentPatch) | none |
| `anchor_id` | UUID string (UUIDUtil.STRING_CODEC) | none |

`enabled` is read by the location broadcaster (and the Tempad), `portal_target` by the location card, `wallet_contents`
by the card wallet, `access_id` by the screening device, `selected_portal` / `portal_offset` by the Tempad docked in a
workstation (Portal Setup app), `twister_data` by the Tempad (twister stacking), `anchor_id` by the timedoor marker /
chronomark items. The marker items also carry `color`, `owner` and a custom hover name (upstream CUSTOM_NAME); the
projector carries `chronon_content` and `portal_target`; the metronome and twister `chronon_content`.

NamedGlobalVec3 NBT: `{name: component JSON, pos: [x, y, z], dimension, angle: float, color}`.

Player capability `tempad:player_data` (`TempadPlayerData`, saved with the player, copied on every `PlayerEvent.Clone`):
`{pinned_location: {providerId, locationId: int[4]}, travel_history: {"<epoch ms>": {marker?, dimension, pos: [x, y, z]}}}`.

Overworld SavedData `tempad_player_points` (`data/tempad_player_points.dat`): `{"<player uuid>": {"<location uuid>": NamedGlobalVec3}}`.

Overworld SavedData `tempad_metronome_energy` (`data/tempad_metronome_energy.dat`): `{stored: {uuid: int}, positions:
{uuid: [GlobalPos]}}`. Ticked by `Tempad` (`TickEvent.ServerTickEvent` END, every `metronome.generation_rate` ticks)
and filled by metronomes.

Overworld SavedData `tempad_anchor_points` (`data/tempad_anchor_points.dat`): `{"<anchor uuid>": GlobalPos}` (upstream
attachment `anchor_points`).

Block entity `tempad:workstation` NBT (upstream keys): `Items` (ItemStackHandler), `DownloadTime`, `MaxDownloadTime`,
`Left`, `Right`, `Recipe`, `TimedoorId`; plus `owner` {id, name} (port key, written only when set; 6.14).

Block entity `tempad:timedoor_projector`: `Chronons`, `PortalTarget` (LocationGetter compound) (upstream keys); `owner`
{id, name} (port key, only when set). Update tag: `Chronons`, `PortalTarget`.

Block entities `tempad:timedoor_marker`, `tempad:chronomark`: `Locked` (upstream); the attachments as port keys under
their upstream paths: `owner` {id, name}, `color` (Color NBT), `anchor_id` (UUID string; upstream path `id`, renamed 2026-11-07 because `saveWithId` overwrites `id` with the block entity type, TODO `gameplay/tnt-and-portal #11`; a UUID under `id` is still read), `access` (id, default
`tempad:private`), `can_access`, `name` (component JSON), `yoffset` (float), `angle` (int); `owner`, `id`, `name` only
when set. Update tag: `Locked`, `color`.

Block entity `tempad:metronome`: `Owner` {id, name}, `Inventory` (ItemStackHandler), `BootTime` (default 100),
`BootChronons`, `InitialChronons`, `Locked` (upstream keys). Update tag: all of these plus `LocalChronons`,
`LocalCapacity` (owner's pool and capacity, for the client handler).

Timedoor entity NBT (upstream keys): `Age, ClosingTime, TargetAngle, IsGlitching, IsOriginal, LinkedPortalId, Color,
TargetPos [x,y,z], TargetDimension, PlacementSettings {type, x, y, z}`.

## 3. Capabilities (Forge, registered in `forge/TempadCapabilities`)

| Capability | Interface | Attached to (phase 1) |
|---|---|---|
| chronons | `ChrononHandler.CAPABILITY` | tempad (TempadChrononHandler), chronon_cell, chronon_battery, chronometer and chronon_generator (ItemChrononHandler; the last two canInsert=false and absent while capacity is 0), creative_chronometer (InfiniteChrononHandler); phase 3: timedoor_projector (RudimentaryTempad capacity, canExtract=false), time_twister (TimeTwister capacity, canExtract=false), metronome (Metronome capacity) |
| upgrades | `UpgradeHandler.CAPABILITY` | tempad (ItemUpgradeHandler), timedoor_projector (RudimentaryUpgradeHandler) |
| player access | `PlayerAccess.CAPABILITY` | location_broadcaster (`DefaultAccess.Public` while `enabled`), screening_device (`ScreeningDeviceAccess`, none while `access_id` is unset) |
| item handler | `ForgeCapabilities.ITEM_HANDLER` | card_wallet (`WalletInventory`; upstream `Capabilities.ItemHandler.ITEM`) |
| player data | `TempadPlayerData.CAPABILITY` | every Player |

Block-entity queries use the same capabilities (`ChrononHandler.of(blockEntity)` asks the UP side). Block entity
`workstation` provides chronons (WorkstationChrononHandler, only while the docked stack has chronons) and upgrades (the
docked stack's handler) through its own `getCapability`, any side, built per query (no attach event). The projector
and metronome block entities serve their chronons (`BlockChrononContent.projector`, `metronome`; `metronomeClient` only
when no server runs) the same way.

## 4. Network (SimpleChannel `tempad:main`; ids are registration indexes, so later packets go after these)

| # | Packet | Dir | Sent by |
|---|---|---|---|
| 0 | CreateLocationPacket(name, color, ctx) | c2s | New Location screen |
| 1 | DeleteLocationPacket(ctx, providerId, locationId) | c2s | Teleport screen |
| 2 | RedirectAppPacket(AppHolder(appId, ctx, isStationary)) | c2s | app bar of every Tempad screen |
| 3 | OpenAppPacket(appId) | c2s | keybind (new location) |
| 4 | OpenTempadPacket() | c2s | keybind (open Tempad) |
| 5 | OpenTimedoorPacket(providerId, locationId, ctx) | c2s | Teleport screen |
| 6 | SaveSettingsPacket(ctx, defaultApp, defaultMacro) | c2s | Settings screen |
| 7 | SetFavoritePacket(favorite or empty) | c2s | Teleport screen |
| 8 | UpdateTempadLockPacket(locked, ctx) | c2s | lock toggle of the Tempad screens |
| 9 | UseMacroPacket() | c2s | keybind (macro) |
| 10 | RotatePlayerMomentumPacket(angle) | s2c | timedoor, same-dimension teleport |
| 11 | ConfigSyncPacket(...) | s2c | login, config reload |
| 12 | WriteToCardPacket(providerId, locationId, ctx) | c2s | Teleport screen (click the info panel while carrying a card) |
| 13 | SyncPortalSettingsPacket(x, y, z, angle, isVertical, providerId?, id?, ctx) | c2s | Portal Setup screen, on close |
| 14 | BackTrackLocation(time as epoch ms, ctx) | c2s | Timeline screen, Time Twister screen |
| 15 | UpdateAnchorPacket(pos, color, name, access, locked) | c2s | Timedoor Marker screen, on close |
| 16 | UpdateChronomarkPacket(pos, color, name, access id, locked, yOffset, angle) | c2s | Chronomark screen, on close |
| 17 | UpdateMetronomePacket(pos, locked) | c2s | Metronome screen, on close |
| 18 | OpenTimeTwister(history map, ctx) | s2c | time twister use |
| 19 | OpenTimedoorMarker(pos, color, name, canAccess, locked) | s2c | timedoor marker use |
| 20 | OpenChronomark(pos, color, name, access options, access, locked, yOffset, angle) | s2c | chronomark use |

Each group keeps upstream's relative order (12 before 13; c2s back_track_location, update_anchor, update_chronomark,
update_metronome, then s2c open_time_twister, open_timedoor_marker, open_chronomark). Nothing is deferred. History maps
are `writeMap(long epoch ms, HistoricalLocation.write)` (`TimelineData.writeHistory`, shared with OpenTimeTwister). The
s2c handlers run in `client/ClientPacketHandler` through DistExecutor.

## 5. Client side (phases 1 to 3 done 2026-11-07)

Entry: `client/TempadClient.init(modBus)`, called from `Tempad` only on `Dist.CLIENT`.

Done (upstream 3.0.4 behaviour):

- Timedoor: `client/entity/TimedoorRenderer` (box faces + the animated outline on `RenderType.lines()`),
  `TimedoorRenderTypes` (translucent, no cull, particles target), core shader `minecraft:rendertype_timedoor` from the
  upstream jar via `RegisterShadersEvent` (loads unchanged on 1.19.2). With a shader pack in use (`ShaderModBridge`, Iris
  API v0 = Oculus) the faces use generated textures and position_tex_color, as upstream. Oculus is not in `mods/` today.
- Screens: `MenuScreens.register` for teleport, new_location, settings; `AbstractTempadScreen` (frame, app bar,
  close / lock buttons, vertical chronon bar + tooltip, title and clock), `TeleportScreen` (search, sort dropdown saved to
  `tempad-client.toml`, collapsible sections, favourite first, pin / unpin, delete with confirm modal, info panel,
  Teleport), `NewLocationScreen` (map of the player's chunk, dimension and X/Y/Z, colour picker with HSB, hue, eyedropper,
  recent and chat-colour presets, hex input, name max 32, Done only with a name), `SettingsScreen` (default macro / app
  dropdowns, Done saves without closing).
- Widget kit `client/ui` (TP-D4), translated from Olympus 1.0.18 (and Resourceful Lib's SelectionList for the app bar);
  Olympus art and lang come from the Olympus jar nested in the upstream jar (`UpstreamJars.olympus()`, added to
  `tempad_upstream_assets`).
- Tooltips: `ChrononData` -> `ChrononTooltip` (energy bar, shift = exact count), `InstalledUpgradesComponent` ->
  `UpgradesTooltip` (inserted at index 2 for a Tempad with upgrades), portal target lines via `ItemTooltipEvent`.
- Item properties: tempad `in_use`, `attached`, `charge`; `charge` on chronon_cell, chronon_battery, chronometer,
  chronon_generator (4 steps).
- Keybinds (unbound by default, category `category.tempad`): open Tempad, use macro, new location.
- Rainbow colour animated every client tick (`Color.setRainbowValue`).
- `ClientPacketHandler`: rotatePlayerMomentum (unchanged).

Phase 2 (2026-11-07):

- `client/block/WorkstationRenderer` (the docked stack flat on the pad), registered on
  `EntityRenderersEvent.RegisterRenderers`. Render layer comes from `models/block/workstation.json` (`"render_type":
  "cutout"`, read by Forge 43); `workstation_terminal` is solid, as upstream.
- `client/screen/tempad/PortalSetupScreen`: offset column (left/right, up/down, front/back as decimal inputs, angle as an
  integer input, upright toggle; the three offsets have hover descriptions), search box and location list (shares the
  Teleport screen's sort mode and section visibility, sorted by name, selected entry highlighted). Closing it sends
  SyncPortalSettingsPacket (the server clamps offsets to +-5 and stores the selection only for a workstation context).
  Kit additions: `TempadUI.toggle / intInput / floatInput` (Olympus `Widgets.doubleInput` behaviour: typed text kept,
  empty or "-" = 0, unparsable edits refused) and `client/ui/State.map`.
- `client/screen/WalletScreen`: wallet art and slot sprites, 7x7 close button (gray / black / white), black unshadowed
  labels.
- Teleport screen: with a location selected and any item carried, hovering the info panel draws an orange outline and a
  click sends WriteToCardPacket (the server acts only on a location card). `AbstractTempadScreen.popups()` lets the
  subclass skip this while a popup is open.
- Item properties: location_broadcaster `enabled`, screening_device `enabled` (access id set), location_card `written`,
  card_wallet `full` (has any card; upstream name and meaning).
- Not ported: `PlayerPosTooltip` (upstream defines it but nothing registers or returns it).

Phase 3 (2026-11-07):

- `client/screen/tempad/TimelineScreen` (Timeline app, `screen/timeline` art) with `client/widgets/TimelineWidget` and
  `TimelineEntry`: travel history sorted by time on a horizontal scrolling strip (mouse wheel), the entry under the
  centre shows marker + dimension, XYZ and the date (ICU date-time format of the selected language), Teleport sends
  BackTrackLocation; "Tracking" label, the player's name and the player model following the mouse.
- `client/screen/TimeTwisterScreen` (radial menu of the last 8 history entries, newest first; click or keys 1-8 send
  BackTrackLocation; hovered slice grows and shows a tooltip; marker icons from `textures/gui/sprites/marker/`).
- `client/screen/TimedoorMarkerScreen` (name, colour picker + hex input, public access switch, lock and close icons;
  closing sends UpdateAnchorPacket) and `ChronomarkScreen` (access dropdown over the server's options plus "private",
  y offset and angle inputs; sends UpdateChronomarkPacket), on `client/ui/KitScreen`. They keep Olympus' default look
  (Olympus modal, text box, button, switch and list sprites from the nested Olympus jar).
- `client/screen/MetronomeScreen` (`screen/metronome` art, vertical pool bar with `stored/capacity` tooltip, lock and close
  steel buttons; closing sends UpdateMetronomePacket).
- `client/block/SpatialAnchorRenderer` for the timedoor marker and chronomark block entities (the blocks are INVISIBLE and
  drawn only by it, tinted with the synced colour); block colour for timedoor_marker, item colour for timedoor_marker and
  chronomark (stack `color`, else Tempad orange).
- Item property `has_card` on timedoor_projector (`writtenProperty`). The time twister needs none of its own (the Tempad's
  `attached` already exists).
- `client/RudimentaryTempadClient`: the projector arm pose (`HumanoidModel.ArmPose.create("TEMPAD_RUDIMENTARY_TEMPAD",
  true, transformer)`, both arms to the head pitch, off arm pulled in) as IClientItemExtensions.getArmPose, registered by
  `RudimentaryTempadItem.initializeClient` (Forge calls it only on the client; the class is reached through a static
  method typed as the Forge interface, so a dedicated server never loads it).
- Keybind `key.tempad.travel_timeline` (unbound, `category.tempad`) -> OpenAppPacket(`tempad:timeline`).
- `ClientPacketHandler`: openTimeTwister, openTimedoorMarker, openChronomark open the screens above.
- Kit additions: `client/ui/KitScreen`, `Dropdown.withTopRightAlignment` (Olympus OverlayAlignment.TOP_RIGHT: the list
  opens above the button, right edges aligned).

Left for phase 4 (with its items): KnowledgeAppScreen / KnowledgeScreen (guide) and `TickingScreen`, `MultilineTooltip`,
`CraftingRecipeWidget`, the chronon fluid client properties, Curios renderer and Ars Nouveau hook, Jade
`WorkstationComponentProvider`.

### Phase 4 registrations and client (2026-11-07)

Items `tempad:knowledge_projector`, `tempad:knowledge_repository_upgrade`; menu `tempad:guide`; app
`tempad:knowledge_repository`; context type and locator `tempad:curios` (Curios only); access `tempad:ftbteams` (FTB Teams
only); Curios slots by IMC: bracelet (preset, size 1), charm (size 3), belt (preset, size 1); Curios renderers for tempad,
card_wallet, chronon_battery, chronon_cell, chronometer, chronon_generator, creative_chronometer, screening_device,
location_broadcaster; WTHIT plugin `tempad:main` with client toggles `tempad:timedoor`, `anchor`, `rudimentary`,
`metronome`, `workstation`, `chronon` (all on).

## 6. Deviations from upstream behaviour (all forced by 1.19.2 or the phase split)

1. Feature flag `required_location_upgrade` -> config `require_location_upgrade` in tempad-common.toml (default false,
   upstream default). When off, the New Location Upgrade item still exists (1.19.2 has no feature flags) but is hidden
   from the creative tab, and its two recipes are skipped by the condition `tempad:required_location_upgrade`. The value
   is synced to clients with ConfigSyncPacket (upstream clients see the server's enabled features).
2. Config file is a ForgeConfigSpec TOML; Resourceful Config's in-game screen and the ConfigInfo links are gone. No
   ranges (upstream has none) beyond Java int.
3. Config sync sends one packet with every synced value instead of per-entry packets.
4. Travel history: logged on the server only (upstream also logs into an unused client copy); Date keys are restored in
   time order after a reload (upstream's map order after a reload follows CompoundTag hashing). `backtrackTo` uses an
   ITeleporter (no portal sound), as DimensionTransition DO_NOTHING.
5. `age_until_allowed_through_timedoor` is a weak in-memory map instead of an attachment (same lifetime: upstream never
   saves or copies it).
6. Cross-dimension teleport: ITeleporter with PortalInfo(targetPos, velocity, targetAngle, 0) and no portal sound;
   same-level: player position packet with every axis relative (client keeps its velocity) plus the POST_TELEPORT ticket,
   as 1.21's teleportTo(..., RelativeMovement.ALL, ...). `canChangeDimensions()` has no level arguments on 1.19.2.
7. TicketController("tempad:timedoor") -> ForgeChunkManager validation callback for mod id `tempad` (drops any stale
   tickets). Upstream never issues tickets with it; the doors use `TicketType.PORTAL` region tickets, kept as is.
8. Timedoor NBT load accepts a missing/"null" LinkedPortalId (upstream would throw on `UUID.fromString("null")`).
9. Upgrade recipe type is marked special (hidden from the 1.19.2 recipe book) to avoid the unknown-category warning.
10. Player data capability returns a detached dummy for players without the capability (fake players created before
    attach) instead of crashing.
11. Phase split (TP-D7): only phase-1 to phase-3 items, blocks, apps, menus, packets, capabilities and location handlers
    are registered. Visible effects: no knowledge projector / guide, no knowledge repository upgrade, no Curios slot,
    Jade, FTB Teams, Odyssey Allies or Cadmus hooks (phase 4). Since phase 2 the Player Teleport Upgrade recipe loads (it
    needs the broadcaster) and broadcasters / screening devices make players appear in the player list.
12. Upstream bugs fixed (owner decisions 2026-11-07, TODO `gameplay/tnt-and-portal #9`; built 2026-11-07, checks
    SQ-TP-21..25, CQ-TP-12..14; done before SQ-TP / CQ-TP 01..20 ran, at the owner's request, so a FAIL there that
    touches these paths may be the fix rather than the port):
    1. Floor/Vertical placement NBT wrote "x" and "z" from yOffset; each key is now written from its own axis
       (doors saved by an earlier build of the port read back with x = z = y).
    2. `intraDimFail` used the interdimensional key; it uses `entity.tempad.timedoor.fail.intradimensional`. Also
       `intraDimAllFail` used `...fail.interdimensional_all`, a key upstream's lang does not have (raw key shown); it
       uses `...fail.intradimensional_all`.
    3. RotatePlayerMomentumPacket zeroed the player's velocity. `TimedoorEntity.momentumRotation()` gives the angle
       (radians) that turns travel into the entry door (door yaw + 180) into the exit facing (targetAngle); the server
       applies it to every entity on a same-dimension trip and sends it to the player, whose client turns its own
       velocity. Upstream also passed degrees to the radian `Vec3.yRot` and missed the 180, so non-player entities left
       in a wrong direction. Cross-dimension trips keep the velocity unrotated, as upstream.
    4. Re-placing a picked-up marker / chronomark: `SpatialAnchorItem.adoptStoredId` moves the registration from the
       fresh random id to the stack's stored id (removing the random entry). If another standing marker holds that id
       (a copied stack), the new one keeps its fresh id. On Forge, player placement captures block snapshots, so
       `onPlace` may run after `updateCustomBlockEntityTag`; both orders end with the stored id registered once.
    5. Portal Setup number boxes formatted their start value with the system locale; `TempadUI.formatNumber` uses
       Locale.ROOT, no grouping, up to 3 decimals (the format `Double.parseDouble` and the filter accept).
13. Client (TP-D4/TP-D5, forced by 1.19.2 or by Olympus being unavailable):
    - Olympus overlays (dropdown lists, colour picker, eyedropper, delete modal) are layers drawn inside the Tempad
      screen instead of separate screens; they look and close the same (click outside, Escape). A window resize closes
      them (Olympus also drops its overlays on resize).
    - The delete modal darkens the screen without 1.21's blur (1.19.2 has no blur pass).
    - Text boxes use 1.19.2's EditBox for editing; its text has a drop shadow (Olympus draws it flat). Placeholder,
      colours, texture and padding follow Olympus.
    - No mouse cursor shape changes (Resourceful Lib / Olympus cursor screens).
    - Widget tooltips are drawn by the screen (1.19.2 widgets have no tooltip slot); same text and timing.
    - Map player marker: 1.21's `map/decorations/player.png` -> the first 8x8 icon of 1.19.2's `map/map_icons.png`;
      1.21 `canBeReplaced()` -> the block material's replaceable flag in the map's surface search.
    - Rainbow colour: Resourceful Lib's exact animation speed is not known here; the port turns the hue once per 10 s.
    - The colour text box shows `#RRGGBB` (`Color.toString`); `Color.parse` accepts hex, colour names, "rainbow",
      "default", anything else is "default" (Resourceful Lib's parse fallback).
    - The timedoor shader program is built with the POSITION_COLOR_TEX_LIGHTMAP format (upstream passes POSITION_COLOR
      but draws that format; 1.19.2 binds attribute slots from the given format). The timedoor glitch jitter uses the
      level's random (1.19.2 `Entity` does not expose its own). One RenderType per texture is cached instead of one per
      frame (same state).
    - Sorting mode is saved to `tempad-client.toml` at once (`ClientConfig.setSortingMode` saves).
    - Phase 2: Portal Setup's Olympus column / row layouts are placed by hand (same sizes, 2 px gaps); its number boxes
      are EditBox-based like every text box here (shadowed text). The Teleport screen's write-to-card click and outline
      are skipped while a popup layer is open (upstream's overlays are separate screens, so that click never reached the
      Teleport screen). WalletScreen is a plain AbstractContainerScreen (no Resourceful Lib cursor screen) and draws the
      darkened background itself, as 1.21's Screen.render does.
14. Workstation (phase 2):
    - `Properties.of()` -> `Properties.of(Material.STONE, MaterialColor.NONE)` for both blocks (1.19.2 needs a
      material; STONE = solid, not replaceable, normal push reaction like 1.21's defaults; NONE = 1.21's default map
      colour). Mining tool and tier come from upstream's tags.
    - The NeoForge attachment `tempad:owner` on the block entity -> field `WorkstationBE.owner`, saved as `owner`
      {id, name} only when set; load keeps the current value when the key is absent (update packets), as attachment
      deserialisation does.
    - Block capabilities: `WorkstationBE.getCapability` returns upstream's handlers (section 3) instead of
      `RegisterCapabilitiesEvent` providers in `Tempad.kt`.
    - Interaction: 1.21 `useItemOn` + `useWithoutItem` -> one `use()`: server runs the held-item branch, then (main hand
      only, when it passed) the empty-hand branch; client returns SUCCESS, as upstream's client. The child block answers
      the main hand only (it had only `useWithoutItem`). `PASS_TO_DEFAULT_BLOCK_INTERACTION` -> `PASS`.
    - Sounds: `ClientboundSoundPacket(Holder.direct(sound), ...)` -> the 1.19.2 constructor taking the SoundEvent;
      `level.playSound(player, pos, sound, source)` -> volume 1, pitch 1 (the 1.21 overload's values).
    - `MetronomeData` (overworld attachment -> SavedData, section 2), ticked since phase 3 (16.6).
    - `WorkstationTimedoorConfig`: Kotlin `TODO()` -> `UnsupportedOperationException`; nothing calls it (unused upstream
      stub). `Math.clamp` -> `Mth.clamp`. No block codec; `playerWillDestroy` returns void; `getDrops` takes
      `LootContext.Builder`; `getCloneItemStack` is Forge's `BlockGetter` overload; `ServerLevel.entities.get(id)` ->
      `ServerLevel.getEntity(id)`. `WorkstationItem` takes its `Item.Properties` (creative tab).
    - Kept upstream quirks: using an empty workstation with any hand shows "locked"; the child's lock message names the
      child block; `neighborChanged` on either half tests `active` (left or right), so a signal on one half can keep the
      other's flag; the child's `playerWillDestroy` reads `facing` from its neighbour before checking the block;
      finishing an upgrade pops 0-2 dried kelp; `WorkstationChrononHandler` stays bound to the stack docked when it was
      built.
15. Location card, card wallet, broadcaster, screening device (phase 2):
    - `wallet_contents` is stack NBT (section 2) instead of 1.21's ItemContainerContents component. Upstream's client
      wallet menu runs over `WalletInventory(ItemStack.EMPTY)`; writing NBT there would tag the shared EMPTY stack, so
      the write is a no-op (the client copy is never read back).
    - `LocationCardItem.getTooltipImage` returns empty (upstream's `portalTarget as? TooltipComponent` is always null).
    - Screening device language key: 1.21's `ResourceLocation.toLanguageKey("access")` written out as
      `access.<namespace>.<path>`.
    - `NamedGlobalPosAttachment` is ported but, as upstream, registered and used nowhere; its NBT load skips a malformed
      UUID key instead of failing the whole map.
    - `WalletMenu` type uses `IForgeMenuType` (factory ignores the buffer) instead of `new MenuType(...)` with feature
      flags.
    - Kept upstream quirks: screening device cycling uses `currentIndex + 1 % size`; `ScreeningDeviceAccess` passes
      (accessor, owner) to the chosen access, swapped; the wallet "full" model property means "has any card";
      `WriteToCardPacket` does not check that the context is a Tempad, and when the location has no serializable form it
      still swaps the carried card for a blank one (wallet entries have none).

16. Projector, markers, metronome, time twister, Timeline app (phase 3, common / server):
    1. Material: `Properties.of()` -> `Properties.of(Material.STONE, MaterialColor.NONE)` for the projector, both markers
       and the metronome (as 14). Strengths as upstream (3/6, metronome 3/1200).
    2. Interaction: 1.21 `useItemOn` + `useWithoutItem` -> one `use()`: the held-item branch first, then (main hand only,
       when it passed) the empty-hand branch; `PASS_TO_DEFAULT_BLOCK_INTERACTION` -> PASS; the metronome (only
       useWithoutItem upstream) answers the main hand only.
    3. `getDrops(LootContext.Builder)` reads the block entity with `getOptionalParameter` (1.21 `getParameter` throws
       when missing).
    4. Block entity attachments -> fields with the NBT keys of section 2. The synced `color` attachment -> `color` in the
       update tag plus a client block update on every server-side colour change. Every block entity data write also
       marks it changed (marker owner on first use, colour, name, SpatialAnchorItem placement, projector card
       insert/remove, metronome owner on first use); upstream leaves some of these to the next chunk save.
    5. Overworld attachment `anchor_points` -> overworld SavedData `tempad_anchor_points` (PlayerPointsData pattern).
    6. Metronome generation tick: NeoForge `ServerTickEvent.Post` -> Forge `TickEvent.ServerTickEvent` phase END.
    7. `TooltipFlag.hasShiftDown()` (marker item tooltip) -> `Screen.hasShiftDown()` through `DistExecutor` (1.19.2
       tooltip flags carry no shift state).
    8. The three update packets ignore positions in unloaded chunks (`Level.isLoaded`), so a client packet cannot force a
       chunk load; the upstream checks (`mayInteract`, lock / owner) follow unchanged. No distance check (none upstream).
    9. Null safety where upstream would throw: a marker without an anchor id clears a card's target (upstream `id!!`);
       the projector item passes a null chronon handler on (upstream `chronons!!`, only reachable with capacity 0).
    10. `twister_data`: the twister stack's NBT tag instead of a DataComponentPatch (same effect: restored on detach, then
        `chronon_content` set from the Tempad).
    11. Resourceful `ContentMenuProvider` -> `NetworkHooks.openScreen` with the Optional content in the buffer; the
        metronome menu type reads it with `readOptional`.
    12. Resourceful `ConstantColors` (dye recolouring) -> the same RGB ints (alpha 0, as Resourceful's named colours):
        red FF0000, green 008000, blue 0000FF, yellow FFFF00, black 000000, brown A52A2A, cyan 00FFFF, gray 808080,
        light blue = powderblue B0E0E6, light gray D3D3D3, lime 00FF00, magenta FF00FF, orange = Tempad.ORANGE, pink
        FFC0CB, purple 800080, white FFFFFF.
    13. Block codecs (`simpleCodec`) dropped (1.19.2 has none); `Vec3.bottomCenter` / `relative(dir, 1.0)` -> the same
        arithmetic.
    14. Items take `Item.Properties` from `ModItems.props()` with upstream's own settings (`stacksTo(1)` projector,
        metronome; ChrononItem's `stacksTo(1)` for the twister; markers unchanged). Registry order is upstream's
        (creative tab order, app bar order, Teleport section order `default`, `markers`, `player`).
    15. Kept upstream quirks, on purpose (the marker re-place bug that was listed here is fixed: 6.12 item 4):
        - Projector item: the "no location" message is never shown (upstream's `?: { ... }` lambda is never called);
          `useOn` returns PASS after opening a door, so the following `use` tries again (stopped by the 40-tick
          cooldown, except in creative). The projector block opens doors with the owner's GameProfile and needs
          `RudimentaryTempad.capacity` > 0.
        - Marker: right-click with a card rewrites the whole held card stack; dyes are not consumed; the "locked" message
          names the block. Drops always carry a custom name (the block name when none was set), as upstream's
          CUSTOM_NAME.
        - Detaching a twister into an empty slot leaves `twister_data` on the Tempad (overwritten by the next attach).
        - Metronome menu data slots are shorts (vanilla ContainerSetData), so stored / capacity above 32767 (more than
          five metronomes at the default 6000) show wrapped in the screen, as upstream.
          `BlockChrononContent.metronomeClient` is used only when no server runs (`Tempad.server() == null`), so a
          singleplayer client uses the server handler, as upstream.
        - BackTrackLocation always extracts the cost, also in creative (it only skips the check).
17. Phase 3 client (TP-D4/TP-D5):
    - Marker screens: Resourceful Lib's BaseCursorScreen -> `client/ui/KitScreen` (a plain Screen hosting the kit's
      popups; no cursor shapes). Olympus' column (gap 4, including the zero-size spacer), FrameLayout and rows are placed
      by hand with the same sizes; the colour picker overlay and the access dropdown list are popup layers (13).
    - Timeline: 1.21's `renderEntityInInventoryFollowsMouse(box, 50, 0.6)` -> 1.19.2's `renderEntityInInventory` with
      the same centre, feet offset ((bbHeight / 2 + 0.6) x 50 below the box centre), mouse-relative rotation and box
      scissor; the timeline scrolls on the vertical wheel only (1.19.2 has no horizontal scroll delta; upstream adds
      both). Its items take no input, as upstream, so they are not child listeners.
    - Time Twister: an entry without a marker id draws no icon and its tooltip names it "wandering" (upstream's
      `history.marker!!` would crash the client); the screen draws the darkened world first (1.21's Screen.render
      does) and re-enables face culling after its ring. Hovered slice colours above 1.0 wrap in the byte colour exactly
      as in 1.21 (both truncate `(int) (c * 255)` to a byte).
    - Metronome: `menu.data!!` on close -> nothing is sent when the menu has no data (the server always sends it).
    - Projector arm pose: NeoForge EnumProxy (enum extended at load) -> `ArmPose.create` when the client class first
      loads (on item construction, through `initializeClient`).

### 6.18 Phase 4 (compat, handbook)

1. Jade -> WTHIT (the pack has WTHIT 5.36.1, no Jade): same six providers and lines, as WTHIT body lines. Jade's
   ProgressElement -> WTHIT BarComponent (same fraction and "Booting: N%" / "Installing: N%" text; WTHIT's bar art, the
   bar tinted with upstream's text colour, orange for the metronome, dark grey for the workstation, text drawn by WTHIT).
   ChrononElement -> an ITooltipComponent drawing the same 74 x 13 energy bar. Toggle names come from upstream's
   `config.jade.plugin_tempad.*` lang lines, served also as `config.waila.plugin_tempad.*` (section 7). Like upstream,
   every provider reads the client-side block entity (no server data).
2. Curios 5.1 (1.19.2) has no data-driven slots: upstream's `data/tempad/curios/entities/tempad.json` (players: bracelet,
   charm, belt) and `slots/charm.json` (size 3) become IMC `register_type` messages; Curios applies them to players. With
   several requests for a slot Curios keeps the largest size, so charm is at least 3 (a pack config can still change it).
3. CuriosRenderer: upstream's `PlayerModelAccessor` mixin (PlayerModel.slim) is replaced by the player's skin model name
   ("slim"), which is what selects the slim PlayerRenderer; no mixin, `mixin_configs=none` stays. `renderStatic` without
   the level argument (1.19.2 overload; the level is unused for FIXED). Upstream's no-op `model.rightArm.yScale` dropped.
4. Curios locator: upstream calls `orElseThrow()` on the Curios inventory; ours returns "not found" for a player without
   the Curios capability (fake players) instead of throwing during every context lookup. The CuriosContext constructor
   keeps orElseThrow.
5. FTB Teams 1902.2: `FTBTeamsAPIImpl.INSTANCE.manager.getTeamForPlayerID(owner)` -> `FTBTeamsAPI.getPlayerTeam(owner)`;
   null (manager not loaded, unknown player) = no access, as upstream's `getOrNull() ... == true`.
6. Guide widgets (TP-D4): Olympus LayoutWidget/ClearableGridLayout -> `KnowledgeScreen.ContentList` (kit ScrollList +
   upstream's width callbacks, applied before every layout, and rowSpacing); Olympus overscrollX has no visible effect
   (no horizontal scroll) and is not modelled; textarea -> `client/ui/TextArea` (wrap at width / scale, drawn scaled,
   unshadowed, Style-under-cursor click callback); titles and the page title are text-only kit Buttons (the page title
   scrolls an overlong name like other kit text instead of 1.21 StringWidget's drawing); 3-column item grid ->
   `KnowledgeScreen.ItemGrid`. Paragraph scale is upstream's `closestPercent` (0.7 / 0.8 snapped to the GUI scale).
7. Text: GuideLang keeps only keys and arguments; the English is upstream's datagen output read from the upstream jar
   (TP-D1). Upstream's jar lang lacks `chronon_generator_storage`, `chronometer_internal_storage` and
   `metronomes_no_multi_charge`, so those paragraphs show the raw key when they appear (generator / chronometer capacity
   above 0, or `metronome.scale_generation=false`), and `app.tempad.travel_timeline` shows raw too: all as upstream 3.0.4
   (1.19.2 has no translatable fallback).
8. Kept upstream quirks: the Location Card page asks for recipe `tempad:cheap_card` (the recipe is `card_cheap`), so it
   shows no crafting grid; the crafting grid lays the ingredient list into 2x2 when there are 4 or fewer ingredients and
   3x3 otherwise, whatever the recipe's shape; `cell_usage` and `screening_device_usage` pass one argument more than the
   lang uses; the tickers list grows with every page opened while the screen is open; collapsed chapters are remembered
   for the session (static set) and shared by the handheld guide and the app.
9. HandbookItem opens the screen through `DistExecutor` (upstream calls TempadClient directly; same effect, keeps the
   dedicated server from loading client classes) and returns SUCCESS on both sides, as upstream.
10. Not ported, mods not in the pack: Ars Nouveau (`ArsCompat`, timedoor light level), Cadmus (`CadmusCompat`, claim flag
    and game rule `tempad:allowTimedoorsInClaims`), Odyssey Allies (`AlliesAccess`, access ids `tempad:guild` /
    `tempad:party`; their lang lines stay unused), Dark Mode Everywhere (`DarkModeEverywhereCompat`, IMC screen
    blacklist). Upstream has no JEI plugin; JEI shows the crafting recipes as ordinary recipes (the `tempad:upgrade` type has
    no JEI category, as upstream). Patchouli is not used (upstream's guide is its own screen, ported as such).

## 7. Data translation (forge/UpstreamTranslator, TP-D6)

Served as built-in packs `tempad_upstream_assets` / `tempad_upstream_data` (bottom, required). Assets (plus the
`assets/olympus/` files of the Olympus jar nested in the upstream jar, for the screens) pass through
unchanged. Data: folder renames; `c:` -> `forge:` (`UpstreamTranslator.C_TAGS`, checked against kubejs/exported/tags);
`data/c/tags/entity_type/teleporting_not_supported.json` -> `data/forge/tags/entity_types/`; tag references to mapped
`c:` tags made optional; recipes: result `id` -> `item`, `category` dropped, `neoforge:conditions` -> `conditions`
(`forge:mod_loaded`, `forge:not`); dropped: advancements, `data/neoforge/` GLM list, Curios data, and any file naming a
`tempad:` item/block/entity not registered at load (checked against the live registries, so later phases pick their
files up automatically). Tag entries naming unregistered ids are removed; empty tags dropped. Expected phase-1 result:
recipes tempad, chronometer, chronon_battery, chronon_cell, chronon_generator, new_location_upgrade (conditioned),
time_steel_create / _shaped (Create loaded) / _shapeless (no Create), upgrades/new_location (conditioned),
upgrades/player_teleport; item tags batteries, chronon_generators, auto_charge_blacklist (without metronome); entity tags
tempad:teleporting_not_supported, forge:teleporting_not_supported. The debug log lists every dropped file.

Phase 2 adds `C_TAGS` `c:dyes -> forge:dyes`, `c:leathers -> forge:leather`, `c:gems/emerald -> forge:gems/emerald`
(without `c:leathers` the fallback gives `forge:leathers`, which the pack lacks, and the card_wallet recipe fails).
Expected new translated data: recipes `workstation`, `location_card_clean`, `card_cheap`, `card_expensive`,
`card_wallet`, `location_broadcaster`, `screening_device`, `player_teleport_upgrade`; loot table `blocks/workstation`;
`mineable/pickaxe` keeps `tempad:workstation` and `tempad:workstation_child`. Curios tags (`belt`, `charm`) stay dropped
until phase 4.

Phase 3 adds `C_TAGS` `c:storage_blocks/iron -> forge:storage_blocks/iron` (minecraft:iron_block; the fallback gave the
same tag with a warning). The other `c:` tags of the phase-3 recipes were already mapped (checked against
`kubejs/exported/tags/item_tags.json`, 2026-11-07): `c:ender_pearls`, `c:glass_blocks/tinted` (`forge:glass/tinted`),
`c:glass_blocks/colorless` (`forge:glass/colorless`), `c:ingots/iron`, `c:ingots/copper`. Expected new translated data:
recipes `timedoor_projector`, `timedoor_marker`, `timedoor_marker_clean`, `chronomark`, `chronomark_clean`, `metronome`,
`time_twister`; loot tables `blocks/timedoor_marker`, `blocks/chronomark`, `blocks/metronome`,
`blocks/timedoor_projector` (the projector and metronome blocks override getDrops, as upstream, so only the marker
tables are used); `minecraft:mineable/pickaxe` and `minecraft:needs_iron_tool` gain the five blocks;
`tempad:auto_charge_blacklist` gains `tempad:metronome`. Art, blockstates, models, lang, the Timeline app icon and the
metronome GUI come from the upstream jar unchanged.

Phase 4:

- Kept now: `data/curios/tags/item/{belt, bracelet, charm}.json` -> `data/curios/tags/items/` (folder rename like every
  tag; unregistered-entry filter as usual; harmless without Curios). Expected content: belt = tempad, card_wallet;
  bracelet = tempad, chronon_generator, chronometer, creative_chronometer; charm = chronon_battery, chronon_cell,
  chronon_generator, chronometer, creative_chronometer, location_broadcaster, screening_device, card_wallet.
- Still dropped, new reason: `data/tempad/curios/` ("Curios 1.20+ slot data, slots come by IMC").
- Picked up automatically now that the items are registered: recipes `knowledge_projector` (c:ingots/copper,
  c:glass_blocks/colorless, both already mapped), `knowledge_repository_upgrade`, `upgrades/knowledge_repository`.
- Assets: `assets/tempad_static/lang/*.json` gain `config.waila.plugin_tempad.<id>` copies of every
  `config.jade.plugin_tempad.<id>` line (in memory, `aliasWthitConfigNames`; existing keys win).

Lang (port fix, 2026-11-07): Resourceful Lib lets a lang line be a component array. 1.19.2's
`Language.loadFromJson` takes strings only and throws at the first array, so `tempad_static/lang/*.json` stopped
loading at `location.tempad.saved`: the last 13 lines (every timedoor failure message, "location saved", the player
error) showed as raw keys. `UpstreamTranslator.flattenRichLang` turns each array line into the plain text of its parts;
the part colours (orange "Error: ", brown message) are dropped. Check: CQ-TP-12, CQ-TP-15.

## 8. Moving this member

The member uses no other Creative jar (no imports, config, lang or resources from the host; own `@Mod`, config files,
channel and packs; mods.toml needs only forge and minecraft). To ship it standalone or in another bundle, change only:

1. `development/creativeCompat/members/tempad/gradle.properties`: `bundled_in` (remove it for a standalone jar; then
   `./gradlew :backportedTempad:installToMods` works and its `replaces_jars=tempad-forge-*.jar` retires the stock jar).
2. Host `development/creativeCompat/gradle.properties`: drop `:backportedTempad` from `bundle_members` and
   `tempad-forge-*.jar` from `replaces_jars` (and the Tempad lines of the header comment in its build.gradle). The new
   host adds both.
3. `settings.gradle`: the `['backportedTempad': 'tempad']` include block (project dir), if the folder moves.
4. `tools/build_deploy.py` UPSTREAM_DONORS entry `tempad_upstream`: owner mod id (`creativecompat`), label
   ("Tempad (Creative Compat)") and the member path (`creativeCompat/members/tempad`, `earth/terrarium/tempad/forge`).
5. `mod_sides.md`: the `tempad` row (jar column, and the host row if it names the member).
6. README donor table: regenerate with `tools/donor_jars_md.py` (its `OWNER` dict maps a project folder to the mod id
   shown; `creativeCompat` is not in it yet, so the table shows the folder name).
7. feature_writeup/immersive_portals.md TP-D2 (home).

## 9. Checks to queue (scratch-test)

Not run yet: boot with the jar (donor present / missing message), `/give` each item and creative tab content, Tempad
use opens Teleport app, New Location app saves a location (survives restart), timedoor opens / costs 1000 chronons /
cooldown 40 ticks / teleports same and cross dimension / closes after 200 ticks, pinned location + macro (shift-use),
chronometer charging other items, creative chronometer, `require_location_upgrade=true` (app hidden without upgrade,
upgrade item and recipes appear), config sync on a dedicated server, death/respawn keeps pinned location. Most need
the client side first.

Client (phase 1, needs a client): Tempad screen opens with the upstream frame, app bar icons and hover titles, clock,
chronon bar and tooltip; app bar switches Teleport / New Location / Settings; New Location: map of the current chunk
with the player arrow, dimension and X/Y/Z, colour picker (drag in the HSB square and hue bar, eyedropper picks a colour
from the screen, Recents / Chat Colors presets, hex box), Done stays grey until a name is typed, saved location shows in
Teleport with its colour; Teleport: search filters, sort dropdown (Dimension / Alphabetical / Type) is remembered after a
restart, section headers collapse, scrollbar with many locations, select shows dimension and X/Y/Z, pin moves it first and
shows the pin icon, delete asks and removes, Teleport opens the timedoor; timedoor renders (coloured box, white running
light on the outline, opens and closes animated) in Fast and Fancy graphics; Settings: default macro / app dropdowns,
Done saves (shift-use runs the chosen macro, use opens the chosen app); tooltips: chronon bar on the Tempad and cells
(shift shows exact count), upgrades list with icons; item models change with charge and while the Tempad screen is open;
keybinds (bind them first) open the Tempad, run the macro, open New Location; GUI scale 1-4 and a window resize keep the
layout; no errors about tempad / olympus sprites or the rendertype_timedoor shader in latest.log.

Phase 2, workstation: place it (needs room on its left; terminal half appears; facing), break either half (both go,
survival drops the workstation plus any docked Tempad, creative breaking the pad drops nothing), pick-block on the
terminal gives a workstation; dock a Tempad (owner written to it, Tempad drawn flat on the pad), take it back (owner
cleared; other player blocked while locked); terminal opens Portal Setup (or the Tempad's default app if
stationary-capable, e.g. Settings) and sets the block owner on first use; upgrade install (sounds, has_tape model,
smoke, 0-2 kelp, upgrade added; refused when already installed); redstone on either half opens a timedoor at the
configured offset/angle (vertical and floor), costs `cost_to_persist` per second while powered, error message to players
within 5 blocks when it cannot open; Portal Setup screen: offset boxes accept decimals and "-", angle integers, upright
toggle, location list search / sections / select, settings and selection survive closing and a reload; offsets clamped
to +-5; workstation data survives a restart; dedicated server (client classes not loaded server side).

Phase 2, cards and devices: `/give` each of the four items, creative tab order (card, wallet first; broadcaster after
chronon generator; screening device last before creative chronometer); recipes present (card_cheap, card_expensive,
location_card_clean, card_wallet, location_broadcaster, screening_device, player_teleport_upgrade) and no recipe or tag
errors for `tempad`; broadcaster use toggles `enabled` (model changes) and another player with the Player Teleport
upgrade sees the carrier in Teleport (and not when off); screening device use cycles through access ids then off
(action-bar text, tooltip, model), and with `public` the carrier is listed; written card use puts it into a carried
wallet ("Inserted card into Card Wallet") or shows the fail message with no wallet / full wallet; blank card use does
nothing; wallet use opens the 18-slot Wallet screen (art, close button), accepts only written cards, contents survive
relog, `full` model with any card; wallet slots and own wallet cannot be picked up while it is open; right-click a
written card onto the wallet (and the wallet onto a card) in an inventory inserts it; wallet tooltip lists card names
or "empty"; carried wallet's cards appear as the `wallet` section in Teleport and the timedoor opens to them; Teleport
screen: carrying a blank card over the info panel shows the orange outline, click writes the selected location
(`written` model; a stack of 2+ gives one written card back); no outline or write while the sort dropdown or delete
modal is open.

Phase 3, projector: place (faces the player's direction), card in/out by right-click (swap returns the old card,
`has_card` model), shift + empty hand picks it up with its chronons and card, survival break drops it with both, redstone
pulse opens a glitching door after 4 ticks (costs 1000 from its own chronons; "already open" message within 5 blocks),
item use with a card opens a door from the hand, cursor card onto the item and item onto an empty slot moves the card;
holding it raises both arms (first and third person, either hand). Markers: place up / down (landing 1 block in front,
+0.5 or -2), drawn in their colour (block entity renderer; item colour in the inventory), first use sets the owner and
opens the marker screen (modal art, name / colour picker / hex / public switch; chronomark: access dropdown opening
upward, y offset and angle inputs), closing saves name / colour / access / lock, locked blocks other players, dyes
recolour (all 16), card right-click writes an anchor target the Tempad can open, Teleport list shows markers under
"markers" (own, public timedoor marker for others, chronomark access options), chronomark y offset clamped to +-5 and
angle, break drops colour / name / id / owner, owner mismatch blocks placing someone else's marker, data survives
restart; re-placing a picked-up marker (open question, 6.16.15). Metronome: place, screen (art, pool bar and tooltip,
lock toggle saved on close), boot needs 2000 chronons in slots 1-4 then 100 ticks, then the owner's pool fills every 24
ticks, chronons move from slots 1-4 in and to slots 5-8 and neighbouring chronon blocks out (20 per tick), several
metronomes share one pool with summed capacity, break returns a share as the item's chronons, survives restart. Time
twister: use with < 1000 chronons says so; opens the radial history screen (newest first, keys 1-8 and click, hover
tooltip, red numbers for empty slots); backtrack costs 500, 40-tick cooldown, teleports (cross-dimension too) and drops
later entries; right-click stacking onto / off a Tempad keeps its chronons and name (`attached` model); Timeline app
appears only with the twister attached (app bar), scrolls with the wheel, Teleport backtracks the same way; player model
follows the mouse; travel_timeline keybind (bind it first) opens it. Dedicated server: boots and serves the five blocks
and the twister with no client class loaded (marker tooltip path, s2c packets, projector arm pose).

Phase 4:

Server / common: boot with Curios, FTB Teams and WTHIT present (no errors naming tempad compat), and a boot with the
three jars moved out (no ClassNotFound, no tempad:curios context, no tempad:ftbteams access); `/give` knowledge_projector
and knowledge_repository_upgrade, creative tab order (knowledge projector after time steel, upgrade after the player
teleport upgrade); recipes knowledge_projector, knowledge_repository_upgrade, upgrades/knowledge_repository load with no
recipe or tag errors for `tempad`; the debug log lists `data/tempad/curios/...` as dropped and not `data/curios/tags`;
`/data` or the Curios GUI shows bracelet, belt and 3 charm slots; a Tempad in a Curios belt slot opens with the open-Tempad
key and its macro key works, the timedoor costs chronons from that Tempad; chronon cells / chronometer accepted in charm,
card wallet in belt / charm, tempad and chronometer in bracelet; screening device cycles through `tempad:ftbteams` and a
team mate (FTB party) sees the carrier in the player list, a non-member does not; dedicated server: no client class
loaded (handbook use on the server does nothing).

Client: knowledge projector use opens the guide (orange frame 198 x 118, "Knowledge Projector" heading, Introduction
page with the TVA image, credits and the fine-print advisory); chapters Iron / Steel collapse and expand with the
chevron; item buttons select (orange fill) and hover; each page shows its paragraphs, titles, the crafting grid (rotating
tag ingredients every 1.5 s, hover tooltips on ingredients and result) except Location Card; underlined item links jump to
that page (title and selection follow); description scrolls (wheel and scrollbar) and resets to the top on a page change;
GUI scale 1-4 (paragraph text stays sharp) and a window resize keep the page; with the upgrade installed, the Tempad app
bar shows Knowledge Repository (icon from the upstream jar) and opens the same guide inside the Tempad frame; worn items
render on the player in third person (belt Tempad / wallet at the hip, up to 4 charms on the chest, bracelet on the
off-hand arm; slim and wide skins; legs / chest armour offsets); WTHIT: timedoor (closes in N s / closing / target
dimension and X, Y, Z), timedoor marker and chronomark (created by, shift for id), projector (written card's name,
dimension, X/Y/Z or player location), metronome (booting bar, owner), workstation (installing bar, docked Tempad owner),
chronon bar on any chronon block; the six toggles appear in WTHIT's plugin config with readable names.

## 10. Fixes after scratch r1107tp (2026-11-07, owner request; TODO `gameplay/tnt-and-portal #10..#12`)

- #10: `UpstreamTranslator.NON_REGISTRY_KEYS` gains `random_sequence` (a 1.20 loot key holding the table's own id); the
  five block loot tables load again. `workstation_child` keeps no table (the pad drops for both halves).
- #11: marker block entities save the anchor UUID as `anchor_id` (section 2); `SpatialAnchorItem` keeps the id it chose
  when vanilla then loads a ctrl-pick copy's BlockEntityTag.
- #12: `WorkstationBlock.playerWillDestroy` (creative) clears the terminal with UPDATE_ALL | UPDATE_KNOWN_SHAPE |
  UPDATE_SUPPRESS_DROPS, so the pad's updateShape no longer destroys it with drops. Upstream 3.0.4 not compared.
- Checks: SQ-TP-31..39, CQ-TP-16.
