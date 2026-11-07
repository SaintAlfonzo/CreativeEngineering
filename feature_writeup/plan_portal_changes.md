# Plan: Immersive Portals changes (Nether portal, opaque gun portals, mirrors)

Owner notes of 2026-11-07 (scratchpad "portal changes"). Investigation 2026-11-07 (bytecode of
`immersive-portals-2.3.7-mc1.19.2-forge.jar` and `portalgun-1.19.2-5.0.0.jar`); nothing built. All decisions answered by the owner 2026-11-07 (section 5).
Parent writeup: [immersive_portals.md](immersive_portals.md) 2.3, 2.4. TODO rows `gameplay/tnt-and-portal #17..#25`.

The Portal Gun items of the same notes (names, hotbar desaturation, exit lift, dyeing) are already built:
[plan_portal_gun.md](plan_portal_gun.md), TODO #13..#16. The note "change item model later" stays in the scratchpad.

## 0. At a glance

| # | Change | Cause found | Fix | Home | Decision |
|---|---|---|---|---|---|
| N1 | Ordinary fire lights an Immersive Portals Nether portal; the failure (crying obsidian, soul fire) never happens | IP `@Redirect`s `PortalShape.findEmptyPortalShape` in `BaseFireBlock#onPlace`: when it builds its own portal it returns empty, so Forge's `PortalSpawnEvent` never fires and `PortalFailureHandler` never runs | Mixin: IP's fire hook no longer builds portals; Dark Fire hands a valid frame to IP instead of placing vanilla portal blocks | Creative Magic | none needed |
| N2 | Cut the distance seen through a Nether portal | IP loads and draws the other side up to `indirectLoadingRadiusCap` (8 chunks) | Config: lower the cap | config | D1: 4 chunks |
| N3 | Nether portal tint, and fog on the far side to hide the short distance | IP has an overlay switch (`netherPortalOverlay`, off) that draws the portal block over its portal entity; nothing fogs the far side | Config: overlay on. Client mixin: while IP draws through a Nether portal, fog from 4 blocks out to full at the 4-chunk limit (configurable) | config + Creative Compat (client) | D2: tint on; fog 4 blocks to 64 blocks |
| G5 | Opaque gun portal option | New feature | Client config: gun portals are not rendered through (`Portal#isVisible` false on the client) and draw a flat fill in the portal colour | Creative Compat (client) | D3: off by default |
| M2 | Rename the tinted glass types to "Smooth Tinted Glass" | Design and Decor's three tinted framed glass blocks carry the same names as Create Framed's | Lang override | Creative Compat names.js | D4: Design and Decor set, "Smooth Tinted Glass" |
| M3 | Mirror limits: distance from the player, bounces, view distance inside a mirror | IP has only global knobs (`maxPortalLayer` 5, `portalRenderLimit` 200), shared with the gun | Own client config for mirrors: rendered within 24 blocks, 4 bounces, 4 chunks seen inside a mirror (no fog) | Creative Compat (client) | D5: 24 blocks, 4 bounces, 4 chunks; D7: mirror fog off |
| G6 | Gun portals despawn when the owner is too far away | Portals persist until replaced; `PortalGunRecord` tracks each owner's two portals | Owner tells its portals to dissolve on logout or dimension change; each portal heartbeats (10 s) and dissolves past `despawnDistance` from its owner | Creative Compat | D8: owner message plus heartbeat; distance value open |
| M4 | More glass makes mirrors | `BreakableMirror.isGlass` accepts only `GlassBlock` and `IronBarsBlock` subclasses, so tinted glass (`TintedGlassBlock extends AbstractGlassBlock`) and modded glass fail | Mixin on `isGlass` / `isGlassPane`: also accept a block tag `creativecompat:mirror_glass` (and `_panes`) | Creative Compat + tag helper | D6: sneak + flint and steel on frosty glass; blocks and panes |

(M1, the mirror name, is plan_portal_gun.md item 1, built.)

All IP mixins are gated in the owning mod's mixin plugin on `imm_ptl_core` (and `portalgun` for G5). IP and Portal Gun
jars are already in `development/creativeCommon/libs/` for compiling.

## 1. N1: the failure mechanic with Immersive Portals

Found in `qouteall.imm_ptl.peripheral.mixin.common.portal_generation.MixinAbstractFireBlock`: a `@Redirect` on the
`findEmptyPortalShape` call in `BaseFireBlock#onPlace`. It calls the vanilla method, then, when the fire is next to
obsidian, `IntrinsicPortalGeneration.onFireLitOnObsidian(level, pos)`; if that built a portal it returns
`Optional.empty()`. Forge's `onTrySpawnPortal` only fires `PortalSpawnEvent` for a present shape, so the failure
handler (`creativemagic/dimension/PortalFailureHandler`) is skipped. In `normal` mode IP also accepts any frame shape
(`intrinsicToNether`), against decision "new shapes disabled" (immersive_portals.md 2.3).

Dark Fire (`creativemagic/block/DarkFireBlock#onPlace`) does not go through `BaseFireBlock#onPlace`; it finds a
vanilla `PortalShape` and places vanilla portal blocks, so a Dark Fire portal is a vanilla portal (load screen), not
an IP one.

Fix:

1. `IPFireGateMixin` on `IntrinsicPortalGeneration.onFireLitOnObsidian`, HEAD, cancellable: return false. IP's
   redirect then hands back the vanilla shape, `PortalSpawnEvent` fires, and the failure handler ruins the frame as
   before. Also covers the crouch-ignite path (`onCrouchingPlayerIgnite`, off by config) by the same return.
2. Dark Fire, when IP is loaded and `netherPortalMode` is not `vanilla`: after the vanilla `PortalShape` check passes
   (rectangle, so no new shapes), call IP's generation for that frame through a small bridge class loaded only when
   `imm_ptl_core` is present. A thread-local flag lets the gate in step 1 pass for this one call. If IP returns false,
   fall back to the vanilla portal blocks (today's path).
3. Destination side: IP builds the matching frame itself; the failure handler does not touch it (no fire event).
4. Remove the `PortalFailureHandler` diagnostic log once NP-01..05 pass (existing TODO #4).

Check in build: `onFireLitOnObsidian` is only called from the fire redirect and the crouch path (grep the jar's
constant pools), so returning false has no other effect.

## 2. N2, N3: distance, tint and fog through the Nether portal

- `config/immersive_portals_forge.json`: `indirectLoadingRadiusCap` 8 -> 4 (D1); `netherPortalOverlay`
  false -> true (D2). Copy to `configs_edited/root_config/`. The overlay info (`BreakablePortalEntity$OverlayInfo`:
  block state, opacity, offset) is set when the portal is generated, so portals made before the change keep no tint:
  check in test, and if so note it (relight).
- Fog: client mixin in Creative Compat on `FogRenderer.setupFog` (TAIL) or `RenderSystem.setShaderFogEnd`: when IP
  is rendering a portal layer (`PortalRendering.isRendering()`) and the outermost rendered portal is a
  `NetherPortalEntity`, set linear fog from 4 blocks past the portal plane to full at 64 blocks (the 4-chunk
  limit), so the far side fades out gradually (D2). Distances are measured from the portal plane, not the camera. Uses the far dimension's fog colour (IP already swaps fog context per dimension, `MixinFogRenderer`).
  One `PortalFog` helper picks the settings by the type of the portal being drawn through: Nether portal
  (`NetherPortalEntity`), gun portal (`portalgun:custom_portal`), mirror (`Mirror` / `BreakableMirror`) (D7).
- Client config `config/creativecompat-client.toml` (or the existing Compat config if it has a client section),
  one block per portal type under `[immersive_portals.fog]` (D7):

  | Type | `enabled` | `start` | `end` (blocks) |
  |---|---|---|---|
  | `nether` | true | 4 | 64 |
  | `portalGun` | false | 60 | 64 |
  | `mirror` | false | 60 | 64 |

  `enabled` false leaves IP's normal fog. Keep `nether.end` matched to `indirectLoadingRadiusCap` x 16 (comment in
  the config). Same-dimension portals (gun, mirror) use the current dimension's fog colour.

## 3. G5: opaque gun portals

- Portal Gun's portal is `portalgun:custom_portal`, an IP `Portal` subclass. IP skips drawing through a portal whose
  `isVisible()` is false; teleport does not read it.
- Client mixin on `Portal#isVisible` (or on IP's render-candidate filter) returning false for gun portals when
  `[portalgun] opaquePortals` is on (default off, D3). The gun's own edge overlay still draws; add a flat quad in the portal colour
  (`CustomPortalColors`, read from the entity's synced colour) at the portal plane so the wall does not show.
- Check: the colour reaches the client entity (the gun's renderer already tints, so it should); IP's
  `renderingMergable` grouping does not re-enable the view.

## 4. Mirrors

- **M2 rename.** Design and Decor (`design_decor`) has `tinted_framed_glass`, `horizontal_tinted_framed_glass`,
  `vertical_tinted_framed_glass`, the same three names as Create Framed's. Create: Dreams & Desires has no glass
  blocks, so "D&D" in the note means Design and Decor (owner, D4). `design_decor` set renamed "Smooth Tinted
  Glass", "Horizontal Smooth Tinted Glass", "Vertical Smooth Tinted Glass" via names.js; Create Framed's keep their
  names.
- **M3 limits** (owner: not a performance feature, these are the wanted limits for now). Client config
  `[immersive_portals]`: `mirrorRenderDistance` 24 (blocks from the player; beyond it a mirror is not rendered
  through and shows the glass), `mirrorMaxBounces` 4 (caps the portal layer when every portal in the chain is a
  `Mirror`; IP's global `maxPortalLayer` stays 5 for the gun), `mirrorViewDistance` 64 (4 chunks seen inside a
  mirror, a hard cut, not fog). Mirror fog is off by default (D7); turning it on in the fog config fades the
  reflection instead.
- **M4 glass.** Mixin on `BreakableMirror.isGlass` and `isGlassPane` (RETURN): also true for blocks in
  `creativecompat:mirror_glass` / `creativecompat:mirror_glass_panes`, added through `global.tags`. Members found in
  the dumps: `minecraft:tinted_glass`, `securitycraft:reinforced_tinted_glass` (check it stays unbreakable-safe),
  `design_decor:*tinted_framed_glass` (3), `createframed:*tinted*` (blocks and panes), `quark:framed_glass` and its
  16 dyed kinds, `quark:dirty_glass`, `immersive_weathering:frosty_glass`, `alexsmobs:rainbow_glass`. Check each
  class first; any that already extends `GlassBlock` needs no tag entry.
- **Frosty glass.** Flint and steel on IW frosty glass melts the frost (IW's interaction runs first). D6: sneak +
  flint and steel makes the mirror, plain use still melts. Done by a check in the same mixin chain on
  `MixinFlintAndSteelItem` order, or by an IW-side guard; find which runs first in the build.
- Blocks and panes both make mirrors for every type in the tag that has a pane (D6).

## 3.1 G6: despawn (dissolve) by owner state and distance

Two triggers, both ending in the same "dissolve" call on a gun portal (discard the entity, clear its
`PortalGunRecord` entry so the gun can fire a fresh pair):

1. **Owner message (events).** The owner tells each of their portals to dissolve when they log out
   (`PlayerLoggedOutEvent`) or change dimension (`PlayerChangedDimensionEvent`). The owner looks up their two portals
   from the record (by uuid) and sends each the dissolve message; a portal never has to poll for these cases.
2. **Portal heartbeat (distance).** Each gun portal runs its own heartbeat every `heartbeatSeconds` (default 10 s =
   200 ticks, staggered by entity id so portals do not all tick together). On the beat it finds its owner; if the
   owner is online, in the same dimension and farther than `despawnDistance` (suggest 128 blocks, D8), it dissolves.
   If the owner cannot be found at all (missed logout, crash recovery, world load), it dissolves too, as a backstop.

Implementation: a Creative Compat mixin on `portalgun:custom_portal` `tick` (server side, counter field added by the
mixin; counter and owner uuid saved in NBT so the beat survives a reload) plus a small `PortalGunDissolve` helper
used by both triggers. Check in build: how the record is keyed and whether the portal stores its owner (else add the
owner uuid to the entity NBT at spawn, from the gun's place call), and whether removing the entity fires the gun's
own cleanup so the record is not left dangling.

Config `[portalgun]`: `despawnDistance` (blocks, 0 = never), `heartbeatSeconds` (10). Distance despawn lags by up to
one heartbeat; the owner messages are immediate.

## 5. Decisions (owner, 2026-11-07)

| Id | Question | Decision |
|---|---|---|
| D1 | Chunk radius through Nether portals (`indirectLoadingRadiusCap`, was 8) | 4 chunks |
| D2 | Tint and fog | Tint on; fog configurable, starts 4 blocks out and fades to full at the 4-chunk limit |
| D3 | Opaque gun portals default | Off |
| D4 | Which glass becomes "Smooth Tinted Glass" | Design and Decor's three tinted framed glass |
| D5 | Mirror limits | Rendered within 24 blocks of the player, 4 bounces, 4 chunks seen inside a mirror; not framed as performance |
| D6 | Frosty glass; blocks or panes | Sneak + flint and steel on frosty glass makes a mirror; blocks and panes both work |
| D7 | Fog per portal type | Configurable for each type (Nether portal, gun portal, mirror); on for Nether portals, off for mirrors; gun portals off by default; gun and mirror start at 60 blocks for now (Nether 4); the short, hard fog for mirrors and gun portals is accepted for now, up for evaluation |
| D8 | Gun portal despawn | Owner sends a dissolve message to each portal on logout or dimension change; portals run a heartbeat (10 s default) and dissolve when too far from the owner |
| D9 | Portal state HUD icon, portal failure sound | Later, not planned now (section 7) |

## 7. Later (owner notes, not planned now)

- A HUD icon showing portal state like Portal 2 (which of the two portals is placed). Needs the gun's synced record
  on the client; do after G6.
- A sound when a portal fails to place. Find the gun's failure branch in `PortalGunMod` first.

## 6. Order, build and tests

1. N1 first (gameplay rule broken today), with NP-01..05 from [nether_portal.md](nether_portal.md).
2. N2, N3 config, then the fog helper; M3 reuses it.
3. M4 tag and mixin, M2 lang (no dependency).
4. G5 last (render-only, option); G6 with it (server side, independent).
5. Builds: `:creativeMagic:build --offline` (N1), `:creativeCompat:build --offline` (N3, G5, M2..M4);
   `installToMods` only with the game closed. Tags through `global.tags` (Creative Common only if the tag helper
   needs extending).
6. Scratch queue rows, added when built (prefix `IP-`):
   - SQ-IP-01: flint and steel, fire charge and lightning on a vanilla frame with IP loaded: no portal, frame turns
     to crying obsidian; log shows `PortalSpawnEvent fired`.
   - SQ-IP-02: Dark Fire in a valid rectangle: an IP Nether portal entity (`NetherPortalEntity`) appears, not
     vanilla portal blocks; an L-shaped frame does not light; Nether side frame generated, no failure there.
   - SQ-IP-03: `isGlass` accepts each `mirror_glass` member (flint and steel creates `imm_ptl_core:breakable_mirror`).
   - CQ-IP-01: through a Nether portal: far side ends at 4 chunks, clear for the first 4 blocks then fading to full
     fog at the limit; portal tinted by the overlay; changing `nether.start` / `nether.end` changes the fog; `nether.enabled` false removes it.
   - CQ-IP-02: opaque gun portals: off by default (view through); on: no view through, colour fill matches the dye,
     teleport works.
   - CQ-IP-03: a mirror more than 24 blocks away shows glass, within 24 it reflects; two facing mirrors show 4
     bounces; the reflection stops at 4 chunks with no fog; turning `mirror` fog on in the config fades it;
     `portalGun` fog off by default, on when enabled.
   - SQ-IP-04: place both gun portals; owner logs out: both dissolve at once; owner changes dimension: both
     dissolve at once; record cleared and a new shot places a fresh pair after each.
   - SQ-IP-05: owner walks past `despawnDistance`: portals dissolve within one heartbeat (10 s), not before; set
     `heartbeatSeconds` shorter and see it follow; `despawnDistance` 0 disables the distance case; a portal saved
     with its owner missing dissolves on its first beat; the beat survives a restart.
   - CQ-IP-04: names: Smooth Tinted Glass (Design and Decor only) in JEI and WTHIT; frosty glass: sneak + flint and
     steel makes a mirror, plain use melts the frost; a pane of each tagged type makes a mirror.
7. Changelog (when built), `## Portals` family: ordinary fire no longer lights portals with Immersive Portals (fix,
   past tense); Nether portal view distance 4 chunks, tint and fog; opaque portal option (off); gun portals dissolve when the owner logs out, changes dimension or gets too far away; more glass and panes make
   mirrors, sneak with frosty glass; mirrors render within 24 blocks, 4 bounces; portal fog set per portal type; the renamed glass.
8. Writeup: fold the result into [immersive_portals.md](immersive_portals.md) 2.3, 2.4 and
   [nether_portal.md](nether_portal.md) (IP interplay in section 3).
