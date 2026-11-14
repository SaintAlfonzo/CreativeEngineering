# Portals: Tempad, Immersive Portals (limited) and Immersive Portal Gun

Owner notes of 2026-11-06, written down as the plan. Nothing is built or installed yet.

## 0. At a glance

| Part | Status |
|---|---|
| Tempad 1.4.5 loaded cleanly (2026-11-06 session); jar now in `disabledmods/` | superseded by the port |
| Tempad 3.0.4 ported to Forge 1.19.2 (2.1.1), its own jar `backportedTempad` since 2026-11-07 (a Creative Compat member before; planned to fold into Creative Computing); all four phases (core; workstation, cards, wallet, broadcaster, screening device; projector, markers, metronome, time twister; Curios, FTB Teams, WTHIT instead of Jade, handbook as upstream's own screen) | BUILT 2026-11-07, not installed; scratch r1107tp 2026-11-07 ([run doc](testing/scratch_run_2026-11-07.md) 1): server rows PASS 26, FAIL 2 (SQ-TP-08 creative workstation break drops, SQ-TP-25 marker anchor id lost on load), plus all five block loot tables dropped by the translator (TODO `gameplay/tnt-and-portal #10..#12`, all three fixed and built 12:01; scratch r1107tp2 ([run doc](testing/scratch_run_2026-11-07.md) 2): SQ-TP-31..39 PASS, reruns of SQ-TP-24, -25 and the break steps of -08, -13, -14, -15 PASS; CQ-TP-16 not run); SQ-TP-17, -29, CQ-TP-01..15 not run (21..30 / 12..15 added 2026-11-07: bug-fix checks and whole-mod gaps); 5 upstream bugs fixed 2026-11-07 at the owner's request, before testing (TODO `gameplay/tnt-and-portal #9`, PORT_NOTES 6.12), plus 2 lang bugs found while fixing (missing `intradimensional_all` key; array lang lines stopped 1.19.2 loading the end of `tempad_static` lang); WTHIT bar colours approved 2026-11-07 |
| Immersive Portal Gun added, with Immersive Portals as its dependency | PLANNED |
| Immersive Portals limited to the features below | PLANNED |
| Nether portal test of the visual effect and the missing load screen | PLANNED, a trial for stability and performance |

- **Pending:** everything except the interim Tempad jar. `tempad-forge-1.19.2-1.4.5.jar` is in `mods/` and loaded
  without errors on 2026-11-06 (items and the timedoor entity registered, `/give` worked); no Immersive Portals or
  Portal Gun jar is in the pack yet. The client crash on quit that session (2026-11-06 18:54) was Distant Horizons
  (`DhApiWorldUnloadEvent` not found on disconnect), not Tempad: the same crash happened on 2026-10-31, before Tempad
  was installed, and the world had finished saving first.
- **Related:** [nether_portal.md](nether_portal.md) (the failure mechanic), Smaller Nether Portals, NetherPortalFix and
  End Portal, which are in `mods/` and must still work beside Immersive Portals.

## 1. Goal

The main use of Immersive Portals is the Immersive Portal Gun. The second use is a trial with Nether portals, to find
out whether they are stable and how they perform. Everything else Immersive Portals offers is switched off.

## 2. Decisions

### 2.1 Tempad

- Add the mod. The 1.19.2 jar (1.4.5) now loads; the earlier load error did not recur on 2026-11-06.
- **Backport (owner note 2026-11-06):** 1.4.5 is very limited compared with the Tempad wiki, which describes the newer
  releases. Backport the newer feature set to Forge 1.19.2 (skill `port`). First steps: list the features the wiki
  and newer releases have that 1.4.5 lacks; settle the licence route (the 1.19.2 jar's metadata says "All rights
  reserved", so check the upstream repository's licence for each version before choosing a source port or the
  donor-jar method); keep the `tempad` mod id and item ids so existing Tempads in worlds stay valid.
- **Investigation (2026-11-06).** Donor: `DecompiledMods/tempad-1.21.1-3.0.4-all` (NeoForge 1.21.1, compiled Kotlin
  classes only, `mods.toml` says ARR). Real source: github.com/terrarium-earth/Tempad, branch `1.21.1` (= 3.0.4;
  `1.21.x` carries the same version). 190 files, about 14.7k lines, almost all Kotlin.
  - **Licence route: split.** Since the 1.21 rewrite the repo uses "Terrarium License v1": `.kt`/`.java` source and
    `data/` files are MIT; everything else (textures, models, lang, sounds, blockstates) is All Rights Reserved. The
    `1.19.2` (1.4.7) and `1.20.1` (2.3.4) branches are fully ARR. So: port the 3.0.4 source (MIT, GPLv3-compatible,
    keep the MIT notice); load the ARR assets at runtime from the unmodified upstream 3.0.4 jar in `tempad_upstream/`
    (the Vinery `UpstreamJars` method). Never port from the 1.4.x/2.x code.
  - **Libraries:** Olympus (UI, 20 files), Resourceful Lib KT (registry/codec DSL, 10 files), Common Storage Lib
    (2 files) are all MIT but 1.21-only; Resourceful Config 3.x (4 files). None exist for 1.19.2. Compat-only:
    Cadmus, Odyssey Allies, Ars Nouveau, Dark Mode Everywhere (drop); Curios, Jade, FTB Teams, Patchouli, JEI (pack
    has 1.19.2 versions; keep the ones worth it).
  - **1.21 -> 1.19.2 gaps:** data components (`ModComponents`, ~37 files) -> item NBT; NeoForge attachments ->
    capabilities; `CustomPacketPayload`/`StreamCodec` (20 packets) -> `SimpleChannel`; `GuiGraphics` UI (Olympus) ->
    `PoseStack`/`GuiComponent`, so every screen is rewritten, not adapted; `RecipeInput` -> `Container`; `Holder`
    and registry-access APIs; `MapCodec` blocks; 1.21 data formats (`recipe/`, `loot_table/` folder names, item
    component JSON) -> 1.19.2. Feature flag `required_location_upgrade` -> config toggle. Advancements dropped.
  - **Language:** translate to Java rather than keep Kotlin. The pack's KFF is 3.12.0 (Kotlin 1.8); upstream needs
    Kotlin 2.0.20, and the offline Gradle cache has no Kotlin toolchain. Every line is touched by the API gaps above,
    so translation costs little extra and keeps the mod in the pack's Java build (`build-logic/`).
  - **Ids:** 3.0.4 keeps `tempad:tempad`; 1.4.5's `tempad:he_who_remains_tempad` has no 3.0.4 id (its creative
    item is `creative_chronometer`); no remap (no conversions rule; the 1.4.5 jar is in `disabledmods/`, so no world should hold it).
  - **Plan:** in-tree `development/backportedTempad`, mod id `tempad`, in phases: (1) core (Tempad item, timedoor
    entity, locations, teleport/new-location/settings apps, chronons, config); (2) workstation, upgrades, location
    cards, wallet, broadcaster; (3) projector, marker, chronomark, metronome, time twister, screening device;
    (4) compat (Curios, Jade, FTB Teams, JEI) and handbook (Patchouli or own screen). The UI rewrite is the largest
    single cost; a small own widget kit replaces Olympus.

### 2.1.1 Tempad port: approach (decided 2026-11-06)

- **TP-D1 Licence route (owner: "if code is MIT and assets ARR, go the donor jar route").** The CurseForge and GitHub
  copies of Terrarium License v1 agree: `.java`/`.kt` source, `data/` files, build and config files are MIT; all else
  (textures, models, blockstates, lang, sounds, shaders' `.json`) is All Rights Reserved. The CurseForge copy also
  lists `neoforge.mods.toml` as Code. So: Java translated from the MIT source of branch `1.21.1` (3.0.4), with the
  upstream MIT notice in `NOTICE` and per-package headers; the unmodified jar
  `tempad_upstream/tempad-1.21.1-3.0.4-all.jar` (Modrinth gKNwt7xu/T26aJH7E, sha1
  `7a4d6837c6f7a4c9a8f4e7054952df25c148033e`, byte-identical to `DecompiledMods/tempad-1.21.1-3.0.4-all`) is read in
  memory for art (`tempad_upstream_assets`) and data (`tempad_upstream_data`, via `BuiltInPacks.addUpstream`). No
  upstream resource is shipped in our jar (resource guard on). Never port from the 1.4.x / 2.x branches (fully ARR).
- **TP-D2 Home.** Creative Compat becomes a bundle (`ce.forge-bundle`, like Creative Brewing); member
  `development/backportedTempad` (was `development/creativeCompat/members/tempad` until 2026-11-07, when it moved out so Compat ships without a donor jar; TP-D8; planned host: Creative Computing), Gradle `:backportedTempad`, mod id `tempad`, jar `backportedTempad`.
  Ported code keeps package `earth.terrarium.tempad`; backport glue (donor pack, translator) in `...tempad.forge`.
  `replaces_jars` retires `tempad-forge-*.jar`.
- **TP-D3 Java, not Kotlin** (KFF 3.12 = Kotlin 1.8, upstream needs 2.0.20, no Kotlin toolchain offline).
- **TP-D4 Libraries (all MIT, all 1.21-only) are replaced, not depended on:** Olympus -> a small own widget kit in
  `client/ui`; Resourceful Lib KT DSL -> plain Java; Resourceful Config -> `ForgeConfigSpec` (`tempad-common.toml`,
  `tempad-client.toml`, same keys, defaults and ranges); Common Storage Lib -> Forge `IItemHandler`. The pack's
  Resourceful Lib 1.19.2 is not used.
- **TP-D5 1.21 -> 1.19.2 mapping:** data components -> stack NBT, one key per component under its upstream name;
  player attachments -> Forge capabilities (saved, cloned on respawn, synced by packet); payloads -> `SimpleChannel`;
  feature flag `required_location_upgrade` -> common config `requireLocationUpgrade` (default false = upstream default,
  the pack is opt-in); `TicketController` -> `ForgeChunkManager`; `GuiGraphics` -> `PoseStack` + `GuiComponent`.
- **TP-D6 Data translation (in memory, `forge/UpstreamTranslator`):** folder names `recipe`, `loot_table`,
  `tags/item|block|entity_type` -> 1.19.2 plurals; `c:` tags -> the pack's `forge:` tags; result `id` -> `item`;
  drop `category`; drop advancements (pack rule) and the dangling `tempad:tempad_loot_modifier` GLM entry; drop any
  file naming an item not registered yet (phased build).
- **TP-D7 Phase 1 = playable core, as upstream, no tweaks:** Tempad, time steel, chronon cell / battery / generator,
  chronometer, creative chronometer, new-location and player-teleport upgrades and the upgrade recipe; chronon and
  upgrade capabilities; locations, favourites and travel-history data; default and player location handlers; macros;
  timedoor entity, renderer and shader; Teleport, New Location and Settings apps with their screens; config.
  Later: 2 workstation, portal setup, location card, wallet, broadcaster, screening device; 3 timedoor projector,
  marker, chronomark, metronome, time twister + timeline; 4 compat (Curios, FTB Teams, WTHIT in place of upstream's Jade plugin; upstream has no JEI plugin), guide/handbook (upstream's own screen, no Patchouli).
  Tempad behaviour that needs a later item (time-twister stacking, the timeline app) arrives with that item.
- **TP-D8 Portable member (owner 2026-11-06).** The Tempad member must be able to move to its own jar or another bundle
  (likely Creative Engineering once crafting is integrated, or a future computing jar for high-level redstone, advanced
  controls and capabilities) by editing only the host's `bundle_members` / `replaces_jars`, its own `bundled_in`,
  `settings.gradle`, the `tempad_upstream` row in `tools/build_deploy.py` (`UPSTREAM_DONORS`), `mod_sides.md` and the
  README donor table. So: no code, config, lang or resource ties to the host or any Creative jar, no host name in
  strings, its own config, channel, packs and `@Mod`; the host never depends on it. Checklist: member
  `PORT_NOTES.md`, "Moving this member".

### 2.2 Immersive Portal Gun

- Add the mod. It needs Immersive Portals, so that comes in with it.
- Fixes and portal colour dyeing (2026-11-07 owner notes: names, hotbar desaturation, exit lift, dyeing): [plan_portal_gun.md](plan_portal_gun.md),
  TODO `gameplay/tnt-and-portal #13..#16`. All four built in Creative Compat 2026-11-07 (plan section 7); tests
  SQ-PG-01, -02, CQ-PG-01..05.
- Opaque gun portal option: [plan_portal_changes.md](plan_portal_changes.md) G5, TODO #20.

### 2.3 Immersive Portals, limited

| Feature | Decision |
|---|---|
| New portal shapes | Disabled |
| Vanilla portal behaviour | In place |
| The pack's failure mechanic (ordinary ignition fails and ruins the frame, only Dark Fire lights a portal) | In place with Immersive Portals since 2026-11-07: IP's fire hook is gated (Creative Magic `IPFireGateMixin`), Dark Fire builds the IP portal ([nether_portal.md](nether_portal.md) 3) |
| Nether portal | Trial of the visual effect and of the missing load screen |
| Nether portal link mode | `Mode: normal`: a portal only links to an obsidian frame with the exact same shape and orientation |
| End portals | Vanilla behaviour |
| Mirrors | Implemented (enabled). Since 2026-11-07: more glass and panes (tags `creativecompat:mirror_glass`, `_panes`; frosty glass needs sneaking), rendered within 12 blocks, 4 bounces, 32 blocks inside (Creative Compat `[immersive_portals]`); Design and Decor's tinted framed glass named Smooth Tinted Glass |
| Global portals | Excluded |
| Vertical dimension stacking | Disabled |

### 2.4 The Nether trial

Plan of 2026-11-07 for the failure mechanic with IP (found broken: IP's fire hook skipped `PortalSpawnEvent`), view
distance, tint, fog, and the mirror changes: [plan_portal_changes.md](plan_portal_changes.md), TODO #17..#24.
BUILT and installed 2026-11-07 16:44 (Magic, Compat, Common), scratch rows SQ-IP-01..05, CQ-IP-01..04 queued:

- Nether portal: `indirectLoadingRadiusCap` 2 (was 8; 4 until 17:18), view cut `netherViewDistance` 32, `netherPortalOverlay` on (portals made before keep no tint).
- Fog through a portal per type, Creative Compat `[immersive_portals.fog]` (`portals/PortalFog`, client mixin on
  `FogRenderer.setupFog`): Nether on, 8 to 32 blocks past the portal plane (was 4 to 64 until 17:18); gun portals and mirrors on, 16 to 32 (owner, 17:10). View cut: 32 blocks
  inside mirrors (`mirrorViewDistance`) and through gun portals (`portalGunViewDistance`).
- Portal Gun ([plan_portal_gun.md](plan_portal_gun.md) for the earlier fixes): `[portalgun] opaquePortals` (off) skips
  rendering through gun portals and draws the gun's own filled disc; portals dissolve on the owner's logout or
  dimension change, and on a heartbeat (`heartbeatSeconds` 10) when the owner is offline, elsewhere, or past
  `despawnDistance` (128) (`portalgun/PortalGunDissolve`).
- The render filter is IP's `PortalRenderer.shouldSkipRenderingPortal` (`portals/PortalLimits`); teleporting and
  block interaction through portals are untouched.

- Purpose: judge stability and performance, not a permanent commitment. If it is unstable or slow, fall back to the
  vanilla Nether portal and keep Immersive Portals for the Portal Gun and mirrors.
- The trial must check that the failure mechanic still applies (ordinary fire fails, Dark Fire lights), because
  Immersive Portals replaces how Nether portals are created and linked.

## 3. Work

1. Tempad backport (section 2.1): feature gap list, licence route, then the port. The 1.4.5 jar stays in `mods/`
   until the backport replaces it.
2. Pick the Immersive Portals and Portal Gun jars for Forge 1.19.2, check licences and sides, add rows to
   [mod_sides.md](../mod_sides.md) (skill `add-mod`; `integration-report` for overlap with Smaller Nether Portals,
   NetherPortalFix and End Portal).
3. Write the Immersive Portals config for the table in 2.3 and copy it to `configs_edited/root_config/`.
4. Check that `PortalFailureHandler` still fires with Immersive Portals' Nether portal; adapt it if not.
5. Queue the live checks in [scratch_queue.md](testing/scratch_queue.md) (prefix `IP-`): Tempad loads; Portal Gun
   places portals; mirrors work; no new shapes; global portals absent; no vertical stacking; Nether portals link only
   to the same shape and orientation; ordinary ignition fails and Dark Fire lights; End portal is vanilla; Nether
   travel without a load screen; tick time and FPS with Nether portals open.
6. Player changelog bullets once something is built (not added now: nothing player-visible exists yet).

Found by scratch r1106spf (2026-11-06): with Immersive Portals 2.3.7 in `mods/`, a server with Serene Seasons crashed at
world creation (Serene Seasons' ChunkMap access transformer vs IP's private `@Overwrite` of `m_183760_`). Creative Common
now turns on Mixin's `overwrites.conformVisibility` for IP's config, so the overwrite is upgraded to public instead
([creative_engineering.md](creative_engineering.md), "Immersive Portals with Serene Seasons"; the first fix, which set
the method back to private in `preApply`, ran too late and FAILED SQ-SPFX-01 in r1106spfx). Installed 2026-11-07 13:00
after the same crash hit the owner's world join; SQ-SPFX-01 to rerun (TODO `creativesplit/regression #19`). Immersive Portals, Portal Gun and Portal Gels still need their
`mod_sides.md` rows (step 2).

Client crash on launch, 2026-11-06 19:14 and 22:59 (`Initializing game`): Heartstone 1.1.2's `LevelRendererMixin`
`@ModifyArg`s the `renderEntity` call in `LevelRenderer.renderLevel` (to swap in the outline buffer for the bonded
player), while IP's `MixinLevelRenderer.redirectRenderEntity` `@Redirect`s the same call. IP applies first, so
Heartstone's injector (require 1) no longer finds the call it expects and Mixin aborts. Heartstone is ARR, so no
patched copy of it. **Workaround built and installed 2026-11-06 23:11** (the 23:07 build found no config): Creative Common's `CEMixinPlugin` lowers
Heartstone's `LevelRendererMixin` to priority 999 before LevelRenderer loads, so the ModifyArg lands first and IP's
redirect then takes over the same call; the outline is kept ([creative_engineering.md](creative_engineering.md),
"Heartstone with Immersive Portals"). Test CQ-SPFX-02 (client launch).

## 4. Open questions

- Which Immersive Portals build supports Forge 1.19.2 and which Portal Gun build matches it.
- Whether Distant Horizons (in the pack) conflicts with Immersive Portals rendering; the resource pack bench notes say
  DH has crashed other things, so test the pair early.
- Whether Smaller Nether Portals and NetherPortalFix stay once Immersive Portals handles Nether portals.

## 6. Owner notes (scratchpad 2026-11-13, all planned, none built)

TODO `gameplay/tnt-and-portal #27` to `#37`.

- **Naming:** chronons become chronitons ([Futurama](https://futurama.fandom.com/wiki/Chronitons)); fix the projector line "hold {X} chronotons"; timesteel becomes temporal steel, recipe a felsic/steel alloy (ender pearl or ender grain additive?). Tempad recipes overall much more complex, with intermediate and unusual steps.
- **Knowledge projector:** recipes follow the real recipes; no TVA lore; larger screen; a tooltip for every item from its entry; use the projector in the timedoor projector, and that in the Tempad. Categories basic vs advanced instead of iron vs steel (advanced harder, basic lower storage).
- **Mechanics:** Tempad time in game time; several chronometers cancel each other's charging (bug); bracelet Curios slot; timedoor projector door angle 180 degrees (behind the station).
- **Bugs to confirm:** marker name and colour fields not drawn until clicked, colour update inconsistent; default app, single macro option, locked workstation, empty or marked location card on the Tempad, card stuck in the wallet, timeline language entry, every app present in the Tempad, location broadcaster in multiplayer.
- **Server:** chronomarker navigation of mining and resource dimensions; better dimensions need an own Tempad; location cards as unlocks (rare wandering trader, End chests).
- **Portal Gun:** placement must not be blocked by the position of the colour being moved (a blue portal could not move up or down at the same spot).
