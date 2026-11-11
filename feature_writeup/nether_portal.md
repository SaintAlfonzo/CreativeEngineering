# Nether portal ignition: ordinary fire fails, Dark Fire from Flint and Terrasteel opens the portal

## 0. At a glance

| Part | Status |
|---|---|
| Any ordinary portal ignition fails: the portal doesn't form, the fire turns to soul fire, the obsidian frame turns to crying obsidian (`PortalFailureHandler`) | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3, live in 1.0.6); "until confirmed working" diagnostic log still in |
| Dark Fire block (`creativemagic:dark_fire`): lights a frame into a working portal | DEPLOYED |
| Flint and Terrasteel (`creativemagic:flint_and_terrasteel`, 65 uses, flint + terrasteel ingot) places Dark Fire | DEPLOYED, marked **"TEMPORARY testing behavior"** in its own code: no frame check (F1) |

- **Pending:** nothing built and unshipped. NP-01 to NP-12 have never been run (the TODO list says stability isn't
  confirmed). Findings F1 to F6 (section 4) are open; F1 and F2 need your call.
- **Files to open:** [dimension/PortalFailureHandler.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/dimension/PortalFailureHandler.java),
  [block/DarkFireBlock.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/block/DarkFireBlock.java),
  [item/FlintAndTerraSteelItem.java](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/item/FlintAndTerraSteelItem.java).
- **Knobs:** none. The feature has no config switch; turning it off means a jar rebuild.

## 1. Overview

In vanilla 1.19.2 a fire placed inside an obsidian frame (flint and steel, fire charge, dispenser, lightning, spreading
fire) runs `BaseFireBlock#onPlace`: in the Overworld or the Nether it looks for a valid empty frame
(`PortalShape.findEmptyPortalShape`, both axes), asks Forge (`PortalSpawnEvent`, cancellable) and fills it with portal
blocks. Travelling through creates the matching portal on the other side through `PortalForcer`, without that event.
Smaller Nether Portals (in the pack) lowers the minimum frame size.

The pack gates the Nether behind Botania: only Dark Fire, from Flint and Terrasteel, lights a portal, and an
ordinary attempt ruins the frame.

## 2. Reference

### 2.1 Ordinary ignition (fails)

When a valid frame would light with anything but Dark Fire:

1. `PortalSpawnEvent` is cancelled: no portal (both sides).
2. Server side: the frame is found by a flood fill from the fire through air and fire cells in the frame's plane
   (X axis first, then Z; at most 500 interior cells). If the region is fully closed by obsidian, every obsidian block
   touching the interior becomes **crying obsidian**. Corners aren't touched (they're never adjacent to the interior).
   If no closed frame is found on either axis, the obsidian is left alone.
3. The fire becomes **soul fire**, with the fire-extinguish sound.

Crying obsidian isn't obsidian, so the frame can't be lit again; it has to be rebuilt.

This covers every way vanilla lights a frame: flint and steel, fire charge, dispenser, lightning, fire spreading into
the frame, lava lighting the frame interior.

### 2.2 Dark Fire (works)

| | |
|---|---|
| Block | `creativemagic:dark_fire`, a `BaseFireBlock` with 1.0 fire damage, light 5, no collision, instant break, wool sounds |
| Burns things | no (`canBurn` false), never spreads, never goes out by itself |
| Survives | next to any sturdy face (any of six sides, unlike fire's floor rule), else it breaks on the next update |
| Portal | on placement it finds a valid frame (`PortalShape::isValid`, X then Z) and fires `PortalSpawnEvent`; `PortalFailureHandler` sees Dark Fire and creates the portal blocks |
| Tag | in `#minecraft:fire` ([blocktags.js](../kubejs/server_scripts/tags/blocktags.js) line 18), which is what makes portal shapes and the flood fill treat it as interior |
| Textures | `textures/block/dark_fire_0/1.png` (animated), cross model, cutout |

### 2.3 Flint and Terrasteel

- Recipe: shapeless flint + terrasteel ingot ([progression.js](../kubejs/server_scripts/magic/progression.js)).
- 65 uses (vanilla flint and steel has 64), Miscellaneous tab.
- Use on a block: if the space in front of the clicked face is air, places Dark Fire there and takes 1 durability.
  **Any block, anywhere** (F1). Not on non-air spaces.

## 3. What the pack changes

- **One place makes portals:** `PortalFailureHandler` (a Forge event subscriber) is the only code that calls
  `createPortalBlocks()`. `DarkFireBlock#onPlace` only finds the shape and fires the event; it doesn't call
  `super.onPlace` (that ran a second, competing detection pass).
- **Dark Fire isn't `isAir`:** an earlier version overrode `isAir()` to pass the portal interior check, which made it
  invisible (the client skips rendering air). It now relies on the fire tag.
- **Item:** `FlintAndTerraSteelItem extends FlintAndSteelItem` and overrides `useOn` without calling the parent, so
  vanilla's fire placement and portal checks are skipped.
- **With Immersive Portals (2026-11-07, [plan_portal_changes.md](plan_portal_changes.md) N1):** IP redirects
  `PortalShape.findEmptyPortalShape` in `BaseFireBlock#onPlace` and, when its own `onFireLitOnObsidian` built a
  portal, returned an empty shape, so `PortalSpawnEvent` never fired and ordinary fire lit seamless portals.
  Creative Magic's `IPFireGateMixin` makes `onFireLitOnObsidian` (and the crouch-ignite path) return false, so the
  vanilla shape reaches the event and the failure runs as without IP. Dark Fire, after the vanilla rectangle check,
  calls IP's generation through `dimension/IPNetherPortalBridge` (a thread-local pass through the gate): the result is
  an IP portal (`imm_ptl_core:nether_portal_new`) with a generated frame on the other side, not vanilla portal blocks.
  If IP declines (mode `vanilla`/`disabled`, not Overworld or Nether, no frame) the old path places vanilla blocks.
  `PortalFailureHandler` is still the only place that creates vanilla portal blocks. The gate also posts the
  `PortalSpawnEvent` vanilla would have posted (IP's redirect returns an empty shape in `normal` mode), so the frame
  fails when the fire lands.
- **Smaller Nether Portals:** a flint-and-steel click near obsidian queues a task that fills a fire-holding frame
  with vanilla portal blocks and then posts `PortalSpawnEvent` (through Collective, which ignores a cancel). The
  failure handler counts `minecraft:nether_portal` as frame interior, so those frames turn to crying obsidian and the
  portal blocks break with them (2026-11-07).
- **Text:** names in [namechanges.js](../kubejs/client_scripts/namechanges.js) ("Flint and Terrasteel", "Dark Fire").

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. Flint and Terrasteel places Dark Fire on any air block. Your call.** The code calls itself temporary: no
  frame check, so it's also a permanent, unspreading fire and light source anywhere (it damages and ignites mobs
  standing in it). The intended version (per its comment) only places inside a valid obsidian frame. Also: its
  javadoc names a `TerraFireBlock` that no longer exists. DECIDED 2026-10-29: place only on solid blocks; new Dark Fire
  behaviour: faster, reversed animation, no spreading, burns wood blocks very fast with a high ash chance, sustained by
  every fire and soul-fire sustain block (add more magic sustain blocks). Not built (TODO `gameplay/tnt-and-portal #5`).
- **F2. Dark Fire lights portals in any dimension. Your call.** Vanilla only lights portals in the Overworld and the
  Nether; `DarkFireBlock#onPlace` has no such check, so a frame in the End or another dimension opens (and leads to
  the Nether). Add the dimension check, or keep it. DECIDED 2026-10-29: Overworld only. Not built (TODO
  `gameplay/tnt-and-portal #6`).
- **F3. Accidental fire ruins frames.** Lightning or spreading fire reaching a frame (a ruined portal next to lava, a
  storm) turns it to crying obsidian with nobody nearby. Intended or not? DECIDED 2026-10-29: keep, mirrors vanilla
  (TODO `gameplay/tnt-and-portal #7`).
- **F4. The soul fire doesn't stay.** Soul fire needs soul sand or soul soil below; on obsidian it stays only until the
  next block update next to it, then vanishes. The comment says soul fire "is the same flame crying obsidian produces
  when lit", which isn't true in vanilla. Harmless; it reads as a puff.
- **F5. Diagnostic log on every portal attempt** (`[PortalFailureHandler] PortalSpawnEvent fired, state=...`), kept
  "until confirmed working". Remove after NP-01 to NP-05 pass. In [TODO](todo/TODO.md) `gameplay/tnt-and-portal #4`.
- **F6.** FIXED 2026-10-19 (comment names the fire tag). **Stale comment in `DarkFireBlock#onPlace`:** it still says "Dark Fire's isAir() override means PortalShape's
  interior check already accepts it"; the override is gone (the class comment above it explains the fire tag).

### Known limits

- **Destination portals** form normally (vanilla's `PortalForcer` doesn't fire `PortalSpawnEvent`), so the Nether side
  needs no Dark Fire, and the return trip works.
- **Already-lit portals** (in old worlds, or from structures that place portal blocks) keep working; only lighting is
  gated.
- **No config switch:** the gate is always on.

### Rejected options

- **Dark Fire as an `isAir` block:** invisible (3).
- **Dark Fire creating the portal itself:** two code paths could spawn portals; kept to one.

## 5. File map

- patcherMod: [dimension/PortalFailureHandler.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/dimension/PortalFailureHandler.java), [block/DarkFireBlock.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/block/DarkFireBlock.java), [block/CreativeEngineeringBlocks.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/block/CreativeEngineeringBlocks.java) (registration), [item/FlintAndTerraSteelItem.java](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/item/FlintAndTerraSteelItem.java), `item/CreativeEngineeringItems.java` (registration).
- Assets: patcherMod `assets/creativeengineering/{blockstates/dark_fire.json, models/block/dark_fire_0/1.json, models/item/flint_and_terrasteel.json, textures/block/dark_fire_*.png, textures/item/flint_and_terrasteel.png}`. `kubejs/assets/creativeengineering/textures/block/dark_fire_*.png` are different images with the same names; the KubeJS copy is the one the game shows.
- KubeJS: [blocktags.js](../kubejs/server_scripts/tags/blocktags.js) (fire tag), [progression.js](../kubejs/server_scripts/magic/progression.js) (recipe), [namechanges.js](../kubejs/client_scripts/namechanges.js), `item_dict.js` (`i.flint_and_terrasteel`), `block_dict.js` (`b.dark_fire`).

## 6. Offline checks

None. The flood fill could be unit-tested headlessly like the worldgen tools (`tools/java_test_cp.sh`); not built.

## 7. In-game test suite

Survival, Overworld, a fresh obsidian frame (4 × 5, corners optional) each time.

- [ ] **NP-01 Flint and steel fails:** light the frame → no portal, fire-extinguish sound, a soul-fire puff, every
  frame block except corners is now crying obsidian. `latest.log` has the `PortalSpawnEvent fired` line.
- [ ] **NP-02 Fire charge and dispenser fail:** same result with a fire charge and with a dispenser firing one.
- [ ] **NP-03 Dark Fire works:** craft Flint and Terrasteel (flint + terrasteel ingot), use it on the inside bottom of
  the frame → portal forms; the item loses 1 durability.
- [ ] **NP-04 Z axis:** build the frame along the other axis → same result for NP-01 and NP-03.
- [ ] **NP-05 Travel and return:** go through the NP-03 portal → a portal forms in the Nether; walk back through it →
  back in the Overworld.
- [ ] **NP-06 Smaller frame:** the smallest frame Smaller Nether Portals allows → Dark Fire lights it; flint and steel
  ruins it.
- [ ] **NP-07 Big frame:** a 23 × 23 frame (vanilla's largest) → Dark Fire lights it; flint and steel turns it all to
  crying obsidian.
- [ ] **NP-08 Open frame:** a frame with one block missing → flint and steel just lights fire (no portal shape, so
  nothing happens to the obsidian).
- [ ] **NP-09 Dark Fire anywhere (F1):** use Flint and Terrasteel on a dirt floor → Dark Fire stays there, lights the
  area slightly, sets a zombie walking into it on fire, doesn't spread to nearby wood.
- [ ] **NP-10 Dark Fire on a wall:** use it on the side of a stone wall → Dark Fire placed beside the wall stays.
  Break the wall → it disappears.
- [ ] **NP-11 Other dimension (F2):** in the End, build a frame and use Flint and Terrasteel → note whether a portal
  forms and where it leads.
- [ ] **NP-12 Old portal:** a portal lit before the feature (or one in an old world) still works.

## 8. History

- By 2026-09-27 (initial git commit): portal failure handler, Dark Fire (isAir version replaced by the fire tag),
  Flint and Terrasteel (temporary placer); shipped with 1.0.3.
- 2026-10-18: this writeup; findings F1 to F6; tests NP-01 to NP-12. No code changed.
- 2026-10-19: F6 fixed (DarkFireBlock comment; source only, ships with the next patcherMod build).
- 2026-11-04: Dark Fire, Flint and Terrasteel and `PortalFailureHandler` moved to Creative Magic ([creative_magic.md](creative_magic.md)); ids unchanged. Same behaviour.
- 2026-11-07: Immersive Portals interplay fixed (section 3): ordinary fire fails again with IP loaded, Dark Fire builds the IP
  portal. Built and installed 16:44; scratch SQ-IP-01, -02 queued.
