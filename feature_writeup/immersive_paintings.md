# Immersive Paintings: every painting variant on the Datapacks page

Test prefix **PA-** · Written **2026-10-19** · Status **DEPLOYED with pack 1.0.6 (2026-10-14); scratch-server verified;
the painting screen has not been checked in game**

## 0. At a glance

| Part | Status |
|---|---|
| `/dumppaintings`: every registered painting variant with its size and whether it is placeable | DEPLOYED with 1.0.6, part of `/dumpall`; last run 2026-10-18 18:31 (475 variants) |
| `tools/gen_immersive_paintings.py`: copies every variant into Immersive Paintings' Datapacks page (`/cegen` id `immersive_paintings`, group `pack`) | in use; output DEPLOYED with 1.0.6 |
| Generated datapack paintings: 475 PNG + JSON pairs in `kubejs/data/creativeengineering/paintings/<ns>/` (4.5 MB), all at 32 px per block | DEPLOYED with 1.0.6. A scratch server loaded all 475 and built thumbnails; PA-01 to PA-07 pending |

- **Files to open:** [tools/gen_immersive_paintings.py](../tools/gen_immersive_paintings.py) (header = the design),
  [dump_paintings.js](../kubejs/server_scripts/_lib/dump_paintings.js), the output folder
  [kubejs/data/creativeengineering/paintings/](../kubejs/data/creativeengineering/paintings/).
- **Knobs:** `TARGET_RES` (32) and the vanilla author table in the generator; `config/immersive_paintings.json`
  (Immersive's own config, left at the mod defaults).
- **Refresh:** after adding or removing a painting mod or a resource pack that retextures paintings: `/dumppaintings`
  (or `/dumpall`), then `python3 tools/gen_immersive_paintings.py` (or `/cegen immersive_paintings`), then `/reload`.
  The generator exits 1 and lists every skipped variant if a texture is missing or a size isn't whole blocks.

## 1. Overview

Immersive Paintings 0.6.7 adds four items: Painting (8 paper around a dye), Graffiti (dyes, gunpowder and a bucket),
and a Glowing version of each (the item + a glow ink sac); the recipes are the jar's own. They are entities, not vanilla paintings. Placing one opens a selection
screen with four pages:

- **Yours / Players:** images players uploaded (from a file, a URL or a screenshot), synced through the server.
- **Datapacks:** paintings loaded from `data/<ns>/paintings/**/<name>.png` with a sibling `<name>.json`
  (`name`, `author`, `width` and `height` in blocks, `resolution` in pixels per block). The mod ships 94 of its own.
- **New Painting:** the upload editor (dither, colours, offsets, zoom, pixel-art switch).

The page opens filtered to resolution 32 and can also filter by width and height and search by name. Frames and
materials are picked on the Frame page.

Vanilla paintings are a different thing: `minecraft:painting` entities with a registered `painting_variant`. The
pack's painting mods (Rebirth Paintings, Too Many Paintings, Painting Pack, DarkPaintings, MC Style Paintings,
Handcrafted, and single variants from Alex's Mobs, Supplementaries, Upgrade Aquatic and others) all add variants,
which Cycle Paintings lets a player step through on a placed vanilla painting. Without this feature none of those
variants appears in Immersive's screen.

## 2. Reference

### 2.1 Variants by mod (2026-10-18 dump)

| Namespace | Variants | Notes |
|---|---:|---|
| `rebirth_paintings` | 250 | |
| `toomanypaintings` | 65 | |
| `paintings_pack1` | 49 | Painting Pack |
| `darkpaintings` | 30 | authors credited by section in its lang file (2.2) |
| `minecraft` | 30 | 4 of them (earth, wind, water, fire) aren't placeable in vanilla |
| `handcrafted` | 28 | none placeable in vanilla (not in `#minecraft:placeable`) |
| `minecraft_style_paintings` | 12 | |
| `neapolitan` | 3 | |
| `alexsmobs` | 2 | |
| `buzzier_bees`, `cccbridge`, `clayworks`, `create_things_and_misc`, `supplementaries`, `upgrade_aquatic` | 1 each | |
| **Total** | **475** | 443 placeable in vanilla, 32 not; all 475 on the Datapacks page |

Dimensional Paintings registers no painting variants (it has its own items), so it isn't in the list.

### 2.2 What the generator writes per variant

- **Id in game:** `creativeengineering:paintings/<ns>/<id>.png`, so the screen's search matches a mod id or a name.
- **Size:** the variant's pixel size / 16, in blocks. A size that isn't whole blocks is skipped and reported.
- **Texture:** `assets/<ns>/textures/painting/<id>.png`, taken from the highest-priority source: the vanilla client
  jar, then `mods/*.jar`, then `kubejs/assets`, then the resource-pack zips enabled in `options.txt`. A retextured
  painting therefore looks the same in Immersive's screen as on a vanilla painting. Animated strips use the first
  frame. A texture off the block grid (Upgrade Aquatic's 65 x 31 sightless painting) is stretched to the variant's
  size, as vanilla does.
- **Resolution:** under 32 px per block is scaled up nearest-neighbour by a whole factor to 32 (pixel art stays
  sharp); larger keeps its own. Today every one ends at 32.
- **Name:** the `painting.<ns>.<id>.title` lang key, else the id in title case.
- **Author:** vanilla: Kristoffer Zetterstrand, except earth, wind, water, fire and wither (Mojang); these are the
  credits vanilla added in 1.21, since 1.19.2 has none. Others: the `painting.<ns>.<id>.author` or `.artist` key,
  then DarkPaintings' section credits (a `<ns>.author.<x>` key followed by that artist's title keys, in file order),
  else the mod's `displayName` from `mods.toml`, else the namespace. "Unknown" and "-" count as missing.

## 3. What the pack changes

1. **Every vanilla-style painting variant is also an Immersive Paintings datapack painting** (2026-10-14). Players
   can put any of them up as an Immersive painting: any size, glowing, framed, without cycling through same-size
   variants on a vanilla painting.
2. **Resolution 32 for all of them.** The Datapacks page opens filtered to 32 px; a 16 px painting would be hidden
   until the player changed the filter. Upscaling by a whole factor keeps them sharp.
3. **Generated data, not hand-kept.** The output folder is wiped and rewritten on every run, so it always matches
   the current mods and resource packs.

Not changed: Immersive Paintings' recipes, config and its own 94 paintings; Cycle Paintings and vanilla painting
placement.

## 4. Limits and decisions

- **Snapshot, not live.** The PNGs are copied at generation time. A new painting mod or resource pack shows up only
  after the refresh in 0.
- **Size in the pack.** 4.5 MB in `kubejs/data`, shipped to both sides (the server serves the datapack paintings;
  thumbnails are built server side).
- **Authors are best effort.** Mods that credit nobody show the mod name (Rebirth Paintings shows "Valentine Love",
  Too Many Paintings "Too Many Paintings!").
- **Kept separate from Cycle Paintings.** Cycle Paintings works on vanilla painting entities; Immersive's own
  selection is left as the mod ships it.
- **Rejected: keeping native resolutions.** 16 px paintings would be hidden behind the default filter.

## 5. File map

- [tools/gen_immersive_paintings.py](../tools/gen_immersive_paintings.py): the generator (uses `tools/lang_index.py`
  for names, the vanilla jar path and the enabled resource packs).
- [kubejs/server_scripts/_lib/dump_paintings.js](../kubejs/server_scripts/_lib/dump_paintings.js): `/dumppaintings`
  -> `kubejs/exported/paintings.json`. In `/dumpall` ([dump_all.js](../kubejs/server_scripts/_lib/dump_all.js)).
- [tools/generators.json](../tools/generators.json): the `immersive_paintings` entry for `/cegen`.
- `kubejs/data/creativeengineering/paintings/<ns>/<id>.png` + `.json`: generated output, do not edit.
- `config/immersive_paintings.json`: the mod's config, defaults.

## 6. Offline checks

- `python3 tools/gen_immersive_paintings.py` prints written / total per namespace and exits 0 when nothing was
  skipped. 2026-10-18: 475 of 475.
- Every output JSON has `resolution` 32 and whole-block `width` / `height`:
  `python3 -c "import json,glob; print({json.load(open(f))['resolution'] for f in glob.glob('kubejs/data/creativeengineering/paintings/**/*.json', recursive=True)})"`
  prints `{32}`.
- A scratch dedicated server (2026-10-14) loaded all 475 as datapack paintings and built their thumbnails.

## 7. In-game tests

- [ ] **PA-01 Datapacks page:** craft an Immersive Painting, place it on a wall → the screen opens; the Datapacks
  page lists the pack's 475 paintings next to Immersive's own 32 px ones (the default filter).
- [ ] **PA-02 search:** type `rebirth` in the search box → only Rebirth Paintings entries; type a painting's name →
  it appears.
- [ ] **PA-03 size filter:** filter width 4, height 4 → the 4 x 4 paintings (vanilla Burning Skull, Pigscene, Pointer
  among them).
- [ ] **PA-04 placed:** pick a 2 x 2 pack painting → it is placed at 2 x 2 and looks like the vanilla variant
  (sharp pixels, no blur).
- [ ] **PA-05 retextured:** with a resource pack that retextures a vanilla painting enabled at generation time →
  Immersive shows the retextured image.
- [ ] **PA-06 authors:** hover a vanilla painting → "By Kristoffer Zetterstrand"; Earth → "By Mojang"; a DarkPaintings
  one → its artist, not the mod name.
- [ ] **PA-07 multiplayer:** on the dedicated server, a second player sees a pack painting another player placed.

## 8. History

- 2026-10-14: built: `/dumppaintings`, `gen_immersive_paintings.py`, 475 paintings at 32 px; scratch server loaded all
  of them. Shipped in pack 1.0.6 (DEPLOYED 2026-10-14).
- 2026-10-18: `/dumppaintings` rerun with `/dumpall` (475, unchanged).
- 2026-10-19: this writeup (TODO `pack/docs`).
