# Exposure: cameras and photographs (1.9.19 backport + Polaroid + Expanded)

## 0. At a glance

| Part | Status |
|---|---|
| Exposure 1.9.19 base port (`exposure`): camera, films, lightroom, albums, frames, camera stand, projector | DEPLOYED (in `mods/`, ships with 1.0.3), in-game test pending (EX-11, EX-17 PASSED) |
| Exposure Polaroid 1.1.6 (`exposure_polaroid`): Instant Camera + 4 instant slides | DEPLOYED, in-game test pending |
| Exposure: Expanded 1.0.1 (`exposure_expanded`): 2 lenses, 16 filters, palette / hicap / hires films, 6 instant slides | DEPLOYED, in-game test pending |
| Build-time merge of addon tags and global loot modifiers into the base mod's files | BUILT, offline checks pass |
| Exposure Catalog 1.1.2 (`exposure_catalog`) | not ported, on hold (user, 2026-10-01) |
| Pack customisation (KubeJS recipes, tags, loot rules) | none yet; everything is upstream behaviour |

- **What it is:** our own port of Exposure 1.9.19 and two of its addons from NeoForge 1.21.1 to Forge 1.19.2.
  Everything is in one jar, `mods/exposureBackport-1.9.19-ce-19.2-03.jar` (STAGED for pack 1.0.6; `-02` shipped with 1.0.4), built from
  [development/exposureBackport](../development/exposureBackport/). It replaced Exposure 1.6.0.
- **Files to open:** the port's [README.md](../development/exposureBackport/README.md) (layout, conventions,
  every deviation from upstream), [Exposure.java](../development/exposureBackport/src/main/java/io/github/mortuusars/exposure/Exposure.java)
  (base items and components), [ExposureExpanded.java](../development/exposureBackport/src/expanded/java/dev/titanite/sparkwave/exposureexpanded/ExposureExpanded.java),
  [ExposurePolaroid.java](../development/exposureBackport/src/polaroid/java/io/github/mortuusars/exposure_polaroid/ExposurePolaroid.java).
- **Knobs:** `config/exposure-client.toml`, `config/exposure-common.toml`, `config/exposure_polaroid-client.toml`
  (client and common), and per world `serverconfig/exposure-server.toml` and `serverconfig/exposure_polaroid-server.toml`.
  Nothing is in `defaultconfigs/`, so every world uses the defaults in section 2.
- **Build and install:** `./gradlew :exposureBackport:installToMods --offline` from the instance root. Then
  run `python3 tools/gen_pack_manifest.py` and stage a new pack version with `tools/build_deploy.py` (bump
  `mod_version` once per pack release, never rebuild a released number).
- **Keys:** Camera Controls (`key.exposure.camera_controls`) is unbound, so Sneak is used instead. That is the only key binding.

## 1. Overview

Exposure adds real photography. The camera captures what the player sees onto film. Film is developed by
crafting with potions, or with Create spouts, then printed in a Lightroom with paper and dyes. The prints can be
stacked, kept in albums, hung in frames, copied and aged. Every exposure is a server-side image that clients
download when they need to show it, so photos are shared on a server.

**Camera.** Use raises the viewfinder; Use again (or attack, `viewfinder_attacking`) takes the shot.
Sneak in the viewfinder opens the controls: shutter speed, zoom, self-timer and flash mode. Sneak + Use,
or right click in an inventory, opens the attachments. There are four attachment slots:
- **Film:** `#exposure:film_rolls`.
- **Flash:** `#exposure:flashes` (redstone lamp, IE light bulb).
- **Lens:** `#exposure:lenses`. Without one the focal range is 18-55mm.
- **Filter:** `#exposure:filters`.

Right clicking the camera with one of those items in an inventory swaps it straight in.

**Film to photograph.**
1. Shoot on black-and-white or color film. Each roll holds 16 frames of 320 px.
2. Develop it by crafting:
   - Black-and-white film + a water bottle.
   - Color film + awkward, thick and mundane potions.
   - Or fill it at a Create spout in a sequenced assembly (same potions, 250 mB each).
3. Put the developed film in a Lightroom, at light level 13 or more. Print each frame onto paper using dyes:
   - Black and white: black dye.
   - Color: cyan, magenta, yellow and black dyes.
   - Each print gives XP.
4. **Chromatic:** with Tinted Glass on top of the Lightroom it prints in chromatic mode. Three
   black-and-white frames, shot through the red, green and blue filters, combine into one color photograph.

**Photographs.**
- **Stacking:** right click one photo onto another to make Stacked Photographs (up to 16). Use opens a viewer
  that pages through them. Sneak + Use in hand moves the top photo to the bottom.
- **Albums:** hold photos with a note each, and can be signed (the signed album glints).
- **Frames:** Photograph Frames and Glass Photograph Frames are placed like paintings, and also on floors and
  ceilings. They take the largest size (3x3, 2x2 or 1x1) that fits. A Glow Ink Sac makes a frame glow.
- **Copying and aging:** crafting copies a photo (paper + dyes) or ages it (brown dye or Supplementaries
  antique ink).

**Camera Stand.** A camera placed on it is operated remotely from up to 100 blocks away. Sneak + Use
configures the camera. It has a self-timer, can take redstone-triggered shots, and malfunctions when nobody
is in range to fix it.

**Interplanar Projector.** Rename it to an image URL or a file path, then use it as a camera filter: the
next shot is that image instead of the world. On a server, URLs are limited to an allow-list of domains.
It has a consumed-on-use mode, toggled with right click. A broken projector is repaired with an Eye of Ender.

**Polaroid: Instant Camera.**
- It takes up to 12 instant slides; there is no film or lens slot.
- It has a built-in flash, a 20-40mm range, 240 px frames and a 0.75 crop factor.
- Each shot prints straight into the inventory. The print merges into a photo or photo stack already there,
  otherwise it goes in a free slot or drops at your feet.
- There is a 2 second cooldown between shots.
- Slides come in color and black-and-white, normal and high sensitivity. They give a warm instant-film look:
  contrast, levels, HSB and color balance, plus noise on the high-sensitivity ones.

**Expanded.**
- Two lenses, sixteen filters (vanilla post-shader effects), and four palette films that limit the image
  to a retro console's colors: Game Boy, NES, C64, CGA.
- Hicap films with 32 frames, and hires films at 640 px.
- Instant-slide versions of the palettes, and hires instant slides at 480 px.
- New chest loot.

## 2. Reference

### Items

| Mod | Items |
|---|---|
| `exposure` | camera, camera_stand, black_and_white_film, color_film, high_sensitivity_black_and_white_film, high_sensitivity_color_film (sensitivity 2.0, noise 0.065), developed_black_and_white_film, developed_color_film, photograph, aged_photograph, chromatic_sheet, stacked_photographs, album, signed_album, photograph_frame, glass_photograph_frame, interplanar_projector, broken_interplanar_projector, lightroom |
| `exposure_polaroid` | instant_camera, instant_color_slide, instant_black_and_white_slide, high_sensitivity_instant_color_slide, high_sensitivity_instant_black_and_white_slide |
| `exposure_expanded` | telescopic_lens, panoramic_lens; gameboy_film, nes_film, c64_film, cga_film; hicap_black_and_white_film, hicap_color_film, hires_black_and_white_film, hires_color_film; instant_gameboy_slide, instant_nes_slide, instant_c64_slide, instant_cga_slide, instant_hires_black_and_white_slide, instant_hires_color_slide; 16 filters: antialias, art, bits, blobs, blur, bumpy, color_convolve, deconverge, desaturate, flip, ntsc, pencil, scan_pincushion, sobel, spider, wobble (each `<name>_filter`) |

### Lenses, filters, palettes (datapack registries)

| Kind | Entry | Value |
|---|---|---|
| Lens | none | 18-55mm (`default_focal_range`) |
| Lens | `minecraft:spyglass` | 55-200mm |
| Lens | `exposure_expanded:telescopic_lens` | 200-300mm |
| Lens | `exposure_expanded:panoramic_lens` | 10-18mm |
| Filter | glass pane, 16 stained glass panes | crisp (plain glass), color tints; red/green/blue are the chromatic filters |
| Filter | interplanar projector / broken | projects an image / blue-screen effect |
| Filter | 16 Expanded filters | vanilla 1.19.2 `minecraft:shaders/post/<name>.json` (the old "super secret settings") |
| Palette | `exposure:map_colors`, `exposure:map_colors_plus` | the base palettes |
| Palette | `exposure_expanded:gameboy` / `nes` / `c64` / `cga` | 4 / 55 / 16 / 4 colors (CGA: black, white, magenta, cyan) |

They're Forge datapack registries read from `data/<ns>/exposure/{lens,filter,color_palette}/*.json`, so a datapack
or KubeJS data file can add more. Note that 1.19.2 reads them at world load, not on `/reload`. Check a
palette with `/exposure palette export <id> json <file>`.

### Film and slide numbers

| Item | Frames | Frame size | Look |
|---|---|---|---|
| black_and_white / color film | 16 | 320 px (`default_frame_size`) | plain |
| high sensitivity films | 16 | 320 | sensitivity 2.0 (darker scenes), noise 0.065 |
| Expanded palette films | 16 | 320 | palette `exposure_expanded:<name>` |
| Expanded hicap films | 32 | 320 | plain |
| Expanded hires films | 16 | 640 | plain |
| Polaroid instant slides | 1 each | 240 (`instant_camera.frame_size`) | contrast 0.2, levels (0,135,255,25,255), HSB (0,0.05,0.05), color balance (0.03,0.01,-0.01); high sensitivity adds sensitivity 2.0, noise 0.05 |
| Expanded palette instant slides | 1 | 240 | palette only |
| Expanded hires instant slides | 1 | 480 | the Polaroid look |

### Recipes

- **Base:**
  - Crafting: camera, camera stand, the 4 films, album, lightroom, both frames, interplanar projector.
  - Custom types: `exposure:film_developing` (film + potions), `exposure:photograph_copying` (photo + paper +
    dyes), `exposure:photograph_aging` (photo + `#exposure:photo_agers`), `exposure:component_transferring`
    (broken projector + ender eye).
  - Create: 4 sequenced-assembly developing recipes, which carry a `forge:mod_loaded create` condition.
- **Polaroid:** instant camera (iron, button, glass pane, flash, dropper; upstream's 1.21 crafter became a
  dropper). The slides use paper, dyes, lapis or bone meal, plus prismarine crystals for high sensitivity.
  Each recipe makes 3.
- **Expanded:**
  - Lenses: 2 amethyst shards + a spyglass.
  - Films: gold or iron nuggets and ingots, gunpowder or redstone, dried kelp. The palette films add a
    color item: green dye, red mushroom, emerald, or cyan + magenta dye.
  - Instant slides: dyes + paper, x3.
  - Create developing: 8 recipes.
  - **The filters have no recipes:** survival players only get them from chests.

### Loot (added through Exposure's `exposure:add_table` global loot modifier; off with `[misc] loot_chests = false`)

| Chest | Adds |
|---|---|
| simple_dungeon | photograph; antialias filter (20%) |
| abandoned_mineshaft | 3 photographs; spider filter (20%) |
| stronghold_crossing | 4 aged photographs |
| shipwreck_map | 4 aged photographs, 2 pre-exposed chromatic B&W films, red/green/blue panes |
| village_plains_house | 6 aged photographs; Game Boy film: 2 pre-exposed (creeper, TNT) + 1 blank, 10% each |
| village_desert / savanna / snowy_house | NES / C64 / CGA film: 2 pre-exposed + 1 blank, 10% each |
| village_temple, woodland_mansion, ancient_city, bastion_other, buried_treasure, desert_pyramid, end_city_treasure, igloo_chest, jungle_temple, nether_bridge, pillager_outpost, shipwreck_supply, ruined_portal, underwater_ruin_small | one Expanded filter each, 20% (deconverge, desaturate, sobel, ntsc, pencil, bits, wobble, art, color_convolve, blobs, scan_pincushion x2, flip, blur) |
| trial_chamber_corridor | bumpy filter: **never rolls**, 1.19.2 has no trial chambers |

### Configs (defaults; the pack changes none)

- **Server** (`serverconfig/exposure-server.toml`):
  - `[camera]`: `default_focal_range = "18-55"`, `viewfinder_attacking = true`, `right_click_attachments_screen`,
    `right_click_hotswap`, `timer_attracts_mob_attention` (radius 16).
  - `[camera_stand]`: `working_range = 100`, `out_of_working_range_malfunction = true`.
  - `[capture]`: `default_frame_size = 320`, `projecting_enabled = true`.
  - `[lightroom]`: `lightroom_light_requirement = 13`, dyes per print type, print times (80 / 160 / 120 ticks),
    XP 2 / 4 / 5.
  - `[photographs]`: `stacked_photographs_size = 16`.
  - `[integration]`: `create_deployer_stand_hotswap = true`.
- **Polaroid server:** `[instant_camera] focal_range = "20-40"`, `frame_size = 240`, `slide_capacity = 12`.
- **Common:** `[misc] loot_chests = true`, `signed_album_glint`, `different_developing_potions_colors`.
- **Client:**
  - `[capture.loading]`: URL loading for the projector, `ONLY_ALLOWED_DOMAINS` (imgur, discord, githubusercontent).
  - `[export]`: `export_viewed_photographs = true` saves every photo you view, and that you took, as a PNG
    under `exposures/`, organised by world at 2x size.
  - `[render]`: frame culling distance 64.

### Commands (op level 2)

`/exposure` with `export`, `expose`, `load`, `palette` (export / convert), `show`, `debug ...`;
`/shader` applies a post shader.

### Integrations

- **JEI:** Photograph Printing and Photograph Stacking categories, plus info pages. The pack's JEI category order
  (`configs_edited/root_config/jei/recipe-category-sort-order.ini`) lists both.
- **Create:** spout developing (`FillingBySpoutMixin` carries the film's frames through each step, for any film
  item, Expanded's included). A Deployer can hot-swap camera attachments on a Camera Stand.
- **KubeJS:** `ExposureEvents.frameAdded`, `ExposureEvents.modifyFrameExtraData` (server) and
  `ExposureEvents.addEntityInFrameExtraData`. No pack script uses them yet.

## 3. What the pack changes

1. **The whole mod is a port.** Exposure 1.6.0 (the last 1.19.2 release) was replaced with 1.9.19 so the
   addons could run. It is in-tree, GPLv3, with the upstream MIT notices kept. The port follows upstream file
   for file: same mod ids, packages and class names. A backport layer
   (`io.github.mortuusars.exposure.backport`) supplies the 1.20/1.21 APIs upstream expects. Every deviation
   is listed in the port's README under "Where the port differs from upstream".
2. **Four mods, one jar, one resource pack.**
   - Upstream ships separate jars, and their tags and global loot modifier lists merge across packs.
     In one jar, Expanded's copy of `data/exposure/tags/items/lenses.json` (and 4 other tags plus
     `global_loot_modifiers.json`) would overwrite the base mod's.
   - `build.gradle` merges each addon file into the base mod's copy when the jar is built: list union,
     `replace` OR'd. The addon's own copy is left out.
   - Any other shared path fails the build.
3. **Polaroid fixes.** The Instant Camera is in Exposure's creative tab. Its creative hot-swap sync runs on
   the client only; upstream also runs it on a server, where it would load client classes.
4. **Expanded fixes.**
   - The instant slides always register as real instant slides. Upstream's Polaroid check is inverted, which
     would register them as plain items.
   - The 91 bundled `assets/minecraft/shaders` files aren't shipped. They're byte-identical to 1.19.2's own.
5. **Bugs found in game and fixed (2026-10-01):**
   - **Photo stacks stuck on the top photo** (stacked photographs and Polaroid prints). The viewer compared
     photos by object identity. The port stores item data in NBT, which builds new objects every read, so the
     viewer reset to page 1 every tick. `ItemAndStack` now compares contents.
   - **The Camera Stand's film count only updated when its menu was opened.** Upstream force-resyncs the
     stand's camera after each shot. That flag was lost in the port, because 1.19.2 has no forced entity-data
     set. `Backport.setEntityData` + `SynchedEntityDataAccessor` restore it.

## 4. Limits and decisions

- **No 1.6.0 world compatibility (user decision, 2026-10-01: "i want everything from 1.9 so the addons
  work as easy as possible").** 1.6.0 cameras, film and photographs keep their item ids, but 1.9.19 doesn't
  read their old NBT: expect empty cameras and blank photos. 1.6.0's bare wall photographs (entity
  `exposure:photograph`) no longer exist; 1.9.19 has Photograph Frames instead. Lightroom contents may be
  lost. EX-50 checks a copy of the server world before 1.0.3 goes out.
- **Catalog is on hold** (user, 2026-10-01). Its 1230-line GUI needs a 1.19.2 rewrite. Its source set,
  mixin configs and mods.toml entry exist but are empty.
- **Expanded's filters are loot-only**, as upstream. The bumpy filter's only chest (trial chambers) doesn't
  exist in 1.19.2, so it can't be found. Options: a KubeJS recipe, or adding it to another chest through the
  loot rules ([loot.md](loot.md)). Not done; it's a pack decision.
- **Datapack registries load at world start.** A new lens, filter or palette needs a world rejoin (or server
  restart), not just `/reload`.
- **Projector URL loading** is limited to the allowed domains in `exposure-client.toml` on servers. Files load
  from anywhere unless `multiplayer_file_loading_only_from_game_dir = true`.
- **Stale loot census:** [kubejs/server_scripts/loot/loot_census.md](../kubejs/server_scripts/loot/loot_census.md)
  still lists Exposure 1.6.0's single table. Regenerate it as loot.md describes.
- **Leftovers:**
  - `config/exposure-common-1.toml.bak` is a 1.6.0-era Forge backup.
  - The Exposure-Create Compatibility resource pack is off, because it overrides 1.6.0 camera models and
    would break 1.9.19's.
- **Version:** `1.9.19-ce-19.2-01` shipped with pack 1.0.3 (it was rebuilt in place twice before that: bug
  fixes, Expanded). `-02` is pack 1.0.4: the login-packet null fix, the refmap fix and Exposure Extras. Bump the
  port build once per pack release; rebuilds before a release keep the same number.
- **Rejected:** porting onto 1.6.0, i.e. adding Expanded's lenses and films to the old mod with KubeJS. The
  feasibility study (2026-09-30) found 1.9.19 is a rewrite, and every addon depends on 1.9 internals.
- **Rejected:** shipping each addon as its own resource pack inside the jar. Build-time merging keeps one pack
  and works the same in dev runs.

## 5. File map

- [../development/exposureBackport/README.md](../development/exposureBackport/README.md): port layout, conventions, backport layer, deviations from upstream, porting tools.
- [../development/exposureBackport/build.gradle](../development/exposureBackport/build.gradle): addon source sets, resource merge, refmaps.
- [../development/exposureBackport/gradle.properties](../development/exposureBackport/gradle.properties): `mod_version`, `mixin_configs`, `replaces_jars`.
- [../development/exposureBackport/src/main/](../development/exposureBackport/src/main/): Exposure (`io.github.mortuusars.exposure`); `META-INF/mods.toml` declares all four mods.
- [../development/exposureBackport/src/polaroid/](../development/exposureBackport/src/polaroid/): Polaroid.
- [../development/exposureBackport/src/expanded/](../development/exposureBackport/src/expanded/): Expanded (`dev.titanite.sparkwave.exposureexpanded`).
- [../development/exposureBackport/src/catalog/](../development/exposureBackport/src/catalog/): Catalog (empty).
- [../development/exposureBackport/tools/](../development/exposureBackport/tools/): `port_upstream.py`, `port_data.py`, `hand_ported.txt`, `port_excludes.txt`, `check_jar.py`.
- `../DecompiledMods/exposure*-src/` (git-ignored): the decompiled upstream sources the port is diffed against.
- [../development/exposureBackport/jar_archive/displaced/](../development/exposureBackport/jar_archive/displaced/): Exposure 1.6.0's jar and configs, moved out of `mods/` and `config/`.
- [../mod_sides.md](../mod_sides.md): row `exposure` (both sides).

## 6. Offline checks

- **Build:** `./gradlew :exposureBackport:build --offline` from the instance root. A shared resource path that
  can't be merged fails here.
- **Jar:** `python3 development/exposureBackport/tools/check_jar.py`. It checks:
  - No duplicate paths.
  - Merged tags and loot lists hold both sides.
  - Every registered item has a model and a name (56 items).
  - Every global loot modifier and its table exist.
  - Every filter shader exists, in the jar or in the 1.19.2 client jar.
  
  Last run 2026-10-02: all PASS. INFO lists 11 lang names upstream ships for items it never registers.
- **Server smoke test:** follow the scratch-server recipe: the jar alone, Trash CreEngServer libraries, stdin
  fifo. Then:
  - `/exposure palette export exposure_expanded:cga json t.json` → "Exported palette".
  - Run `/loot spawn 0 -60 0 loot minecraft:chests/simple_dungeon` 40 times, then
    `execute if entity @e[type=item,nbt={Item:{id:"exposure_expanded:antialias_filter"}}]` → passes
    (2026-10-01: count 2).
- **Dev client:** `./gradlew :exposureBackport:runClient` reaches the main menu with no "Unable to load model"
  or missing-texture lines for `exposure*` (2026-10-01).
- **Port tools are no-ops on existing files:**
  `tools/port_upstream.py ../../DecompiledMods/exposure_expanded-neoforge-1.0.1-src src/expanded/java --dry-run`
  reports 0 written.

## 7. In-game tests

Base (camera, film, printing):
- [x] **EX-01 attachments:** craft a camera, Sneak + Use → attachments screen; insert color film, a redstone lamp, a spyglass and a red pane → each shows on the camera.
- [x] **EX-02 shoot:** Use raises the viewfinder, Use again → shutter sound, film counter goes to 1/16.
- [ ] **EX-03 controls:** Sneak in the viewfinder → controls; change shutter speed, zoom with scroll, set the self-timer → the shot fires after the timer.
- [x] **EX-04 lens range:** without a lens the zoom stops at 18-55mm; with a spyglass 55-200mm.
- [ ] **EX-05 flash:** shoot in a dark room with the flash on → the photo is lit.
- [ ] **EX-06 develop by crafting:** color film + awkward, thick, mundane potions → developed color film; B&W film + water bottle → developed B&W film.
- [ ] **EX-07 develop with Create:** run exposed color film through spouts (awkward, thick, mundane, 250 mB each) → developed film that still has its frames in the Lightroom.
- [ ] **EX-08 print B&W:** Lightroom in light 13+, developed B&W film + paper + black dye → photograph, XP; in the dark → "Not enough light".
- [ ] **EX-09 print color:** developed color film + paper + cyan, magenta, yellow, black dyes → color photograph.
- [ ] **EX-10 chromatic:** shoot three B&W frames through red, green, blue panes; Tinted Glass on top of the Lightroom → chromatic mode prints a color photograph.

Photographs:
- [x] **EX-11 stack paging:** stack 3+ photos, Use → the viewer pages with the arrows, A/D and the arrow keys. PASSED 2026-10-01 (after the fix).
- [ ] **EX-12 stack cycling:** Sneak + Use with a stack in hand → the top photo moves to the bottom.
- [ ] **EX-13 album:** put photos in an album with notes, sign it → signed album glints, readable by another player.
  - 2026-10-05 (`-03`): note and title boxes took no keys (1.19.2 screen focus never set the widget's focused flag); fixed in the backport screens. Retest typing/backspace/Enter in a note, Esc to unfocus, right-click to clear, and the signing title box focused on open.
- [ ] **EX-14 frames:** place a Photograph Frame on a wall with 3x3 space → 3x3; in a 1-block gap → 1x1; on the floor and the ceiling; insert a photo; Glow Ink Sac → it glows.
- [ ] **EX-15 glass frame:** place a Glass Photograph Frame and insert a photo → the photo shows; sizes and Glow Ink Sac work as in EX-14.
- [ ] **EX-16 copy and age:** photo + paper + dyes → a copy; photo + brown dye (or antique ink) → aged photograph.

Camera stand and projector:
- [x] **EX-17 stand film count:** a camera on a stand, take shots remotely → the film count updates without opening the menu. PASSED 2026-10-01 (after the fix).
- [ ] **EX-18 stand control:** place a camera on a stand, Use → you look through it; walk past 100 blocks → control stops.
- [ ] **EX-19 projector:** rename an Interplanar Projector to an imgur image URL, use it as the filter, shoot → the photo is that image; the projector is consumed only in consume mode.

Server and sharing:
- [ ] **EX-20 shared photos:** on the dedicated server, player A prints a photo and gives it to player B → B sees the image.
- [ ] **EX-21 persistence:** take photos, restart the server, relog → the photos and the film in cameras are intact.
- [ ] **EX-22 export:** view a photo you took → a PNG appears under `exposures/<world>/`.
- [ ] **EX-23 JEI:** JEI shows Photograph Printing (catalyst Lightroom), Photograph Stacking, and the developing recipes, including the Create ones.
- [ ] **EX-24 base loot:** open fresh village plains house and shipwreck map chests → aged photographs that show images.

Polaroid:
- [ ] **EX-30 instant camera:** craft it, load 12 instant color slides, shoot → the print appears in the inventory and the slide count drops to 11.
- [ ] **EX-31 print merging:** shoot with a photo already in the inventory → they become a stack; with a stack → the print goes on top; with a full inventory → the print drops.
- [ ] **EX-32 cooldown:** shooting twice quickly → the second waits about 2 seconds.
- [ ] **EX-33 slides only:** instant slides don't fit a normal camera's film slot, and film doesn't fit the instant camera.
- [ ] **EX-34 high sensitivity:** a high-sensitivity slide in a dim room → brighter, grainier than a normal slide.

Expanded:
- [ ] **EX-40 lenses:** telescopic lens → zoom 200-300mm; panoramic lens → 10-18mm.
- [ ] **EX-41 filters:** each Expanded filter fits the filter slot and changes the viewfinder; Shift on the item shows its tooltip.
  - Flip lense failed, sky fails to render upside down
- [ ] **EX-42 palette films:** shoot, develop and print each of Game Boy, NES, C64, CGA film → the photo uses only that palette's colors.
- [ ] **EX-43 hicap:** hicap film shows 32 frames on the counter.
- [ ] **EX-44 hires:** a hires photo in a 3x3 frame is visibly sharper than a normal one.
- [ ] **EX-45 instant slides:** palette and hires instant slides load into the Instant Camera and print in their palette / at higher resolution.
- [ ] **EX-46 Create developing:** an exposed Expanded film through spouts → developed film with its frames.
- [ ] **EX-47 Expanded loot:** fresh village houses (plains, desert, savanna, snowy) sometimes hold palette films, some pre-exposed with pictures; a fresh dungeon sometimes has the antialias filter.
- [ ] **EX-48 JEI:** JEI shows the Expanded recipes and the 8 Create developing recipes.

Before deploying 1.0.3:
- [ ] **EX-50 old world:** load a copy of the server world (winecraft) with the new jar → no crash; note what happened to 1.6.0 cameras, photos, wall photographs and lightrooms (expected: blank photos, empty cameras, wall photographs gone).

## 8. History

- 2026-09-30: feasibility study of backporting 1.9.19 and its addons; found 1.9.19 is a rewrite.
- 2026-10-01: user chose a full 1.9.19 port in one jar, without 1.6.0 compatibility. Base mod ported (compiles, dev client to main menu, boots alone and with the pack's server mods); Polaroid ported. Installed in `mods/`, replacing Exposure 1.6.0, and staged in deploy 1.0.3.
- 2026-10-01: fixed in game: photo stacks stuck on the top photo, Camera Stand film count not updating. User confirmed both fixed (EX-11, EX-17 PASSED).
- 2026-10-01: Expanded ported (Java, data via `port_data.py --exclude 'assets/minecraft/shaders/*'`), build-time tag/loot merge added; scratch server and dev client checks passed; reinstalled to `mods/`. Catalog put on hold (user).
- 2026-10-02: this writeup; `tools/check_jar.py` added (all PASS).
- 2026-10-11: `-03` (album note/title text boxes take keys again, EX-13) ships in pack 1.0.5 (staged, not deployed);
  the 1.0.5 removals delete `-02` on both machines.
- 2026-10-14: pack 1.0.5 was never deployed; everything above that was staged for it ships in pack **1.0.6** (STAGED
  2026-10-14, update from 1.0.4, [plan_1.0.6.md](plan_1.0.6.md)), unchanged (`-03`).
