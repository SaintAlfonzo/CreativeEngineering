# creativeLabels

**Creative Labels** (`creative_labels`): sticky notes and labels as paper on block faces. One block, `face_paper`,
sits in the air cell in front of a host face and holds, per face, up to five dyeable, writable notes (after
Sticky It! 1.2.1) or one label with an item picture (after Labels 1.10). Text labels on items are kept. Jar:
`creativeLabels-<mod_version>.jar`. GPLv3 (LICENSE.txt).

Spec, decisions and tests: [feature_writeup/sticky_notes.md](../../feature_writeup/sticky_notes.md) (`SN-`);
build brief for the agents: [BRIEF.md](BRIEF.md).

## What it does

| Piece | Where |
|---|---|
| `face_paper` block: six face booleans + waterlogged, no item, carpet-fast, no collision, explosions never remove it (C1), piston DESTROY, nothing can be placed into its cell (Q1) | `block/FacePaperBlock` |
| Per-face content: notes on five spots or one label, floor/ceiling rotation, host inset; synced whole with the update tag; saved `Mask` remaps faces after structure rotation/mirroring (C7) | `block/FacePaperBlockEntity`, `block/FaceContent`, `block/NoteData`, `block/LabelData` |
| Where paper fits: spot geometry, face frames, host rules (deny tag, fence gates and heads take nothing, posts by class or tag, slab halves, the shape test for stairs and everything else), piece boxes, covered-face rule | `block/Spots`, `block/PieceHit` |
| Items: `sticky_note` (`DyeableLeatherItem`, colour in `display.color`, text and special under `CreativeLabels`), `label`, `eraser` (stack 1, crafting remainder) | `item/` |
| Dye mixing (vanilla's leather formula), the four specials, text labels on items (`CreativeLabels.Lines`) | `item/NoteColours`, `item/ItemLabels` |
| Note in hand: right-click in the air opens the note screen (Done writes one note out of the stack); sneak + off-hand item labels it; a written note on the cursor labels a whole slot (`overrideStackedOnOther`, backpacks and shulkers are labelled, not opened); one cursor labelling per player per 5 ticks server side; the eraser on the cursor removes a slot's last line; anvil: item + written note | `item/StickyNoteItem`, `item/EraserItem`, `item/LabelGate`, `item/AnvilLabels` |
| "No room for another label" (bass note + action bar) when a fourth line is tried; labelled items' lines in grey under the name (`ItemTooltipEvent`) | `item/ItemLabels` |
| Note screen: Sticky It's `stickygui.png` text bar and `stickyguidatee.png` date bar, EditBox capped at `noteLength`, Done / Enter sends `NoteTextPacket` (hand or placed mode); the server cuts to one line, strips section codes, caps the length, checks reach and that the note is still there (C6) | `client/NoteScreen`, `client/ClientHooks`, `net/NoteTextPacket` |
| Recipes (Java `CustomRecipe`s; the JSONs are the pack's): `note_dye` N identical notes + 1 dye, `note_special` N plain notes + 1 ingredient, `note_copy` 1 written + 8 blank -> 9, `note_erase` note + eraser | `crafting/` |
| Labelled items give no crafting result while `blockLabelledCrafting` is on: head injections on vanilla's `CraftingMenu.slotChangedCraftingGrid` and on FastWorkbench's `FastBenchUtil.slotChangedCraftingGrid` (FastWorkbench replaces both grids' `slotsChanged`, so in this pack only the second runs); the result slot is cleared and resent to the client | `mixin/CraftingMenuResultMixin`, `mixin/FastBenchResultMixin`, `crafting/LabelledCrafting` |
| JEI: plain note + four specials as entries (subtype on `Special`), example shapeless recipes for 16 dyes, 4 specials, copy and erase under `creative_labels:jei/...` | `compat/jei/CreativeLabelsJei` |
| Item icon: `creative_labels:look` property picks the model override (plain / dyed grey tinted by `display.color` / special icons) | `client/NoteItemColours` |
| Dynamic baked model fed by the block entity's `FaceSnapshot[]` model data; geometry loader `creative_labels:face_paper` | `client/FacePaperModel`, `client/FacePaperModelLoader`, `block/FaceSnapshot` |
| Note shape: Sticky It's entity layer `sticky_it:model_sticky` rendered into a capturing vertex consumer at the first chunk build (fallback constants when missing), scaled into each spot, curl toward the frame's y = 0, winding checked per quad | `client/NoteShape` |
| Label paper: Labels' `labels:block/label` model baked and moved onto each face (a hand-built square when missing) | `client/LabelQuads` |
| Donor art: Sticky It's note textures stitched onto the block atlas by location; greyscale tint base, written variants (pen strokes) and item icons derived in memory into a hidden built-in resource pack | `client/DonorArt`, `client/MemoryPackResources` |
| Block outline of the hovered piece only; block colour handler (tint index = face * 5 + spot) | `client/FacePaperHighlight`, `client/ClientSetup` |
| Label pictures and names (block entity renderer, LABEL faces only): one quad per label with a Moonlight render-to-texture icon (`requestFlatItemStackTexture` undyed, so NBT tints and glint show; `requestTexture` + download + ramp + upload when dyed), ids `creative_labels:labels/<item>/<dye>/<tag hash>` (C3); glow = full-bright; the item's name under a smaller icon within `textDistance`; `shouldRender`/`getViewDistance` from `pictureDistance` | `client/FacePaperRenderer`, `client/LabelLayout`, `client/IconDye` |
| WTHIT: a note's name, icon, colour, text and day; a label shows the host (`getOverride`) plus "Label: item" | `compat/wthit/FacePaperWthit`, `waila_plugins.json` |
| Config `creative_labels-common.toml` (`[notes]`, `[item_labels]`, `[wthit]`) and `creative_labels-client.toml` (`[labels]`) | `CLConfig` |
| Donor jars, gameplay off (3.13): Sticky It's note and eraser and Labels' label answer false from `Item.allowedIn` (out of every creative tab and the search); Sticky It's tooltip handler (`TooltipRenderProcedure.execute`, the `{}` tag and `[Shift]` lines) is cancelled at its head while `sticky_it` is loaded. The pack side hides them from JEI, removes their recipes and re-adds the donor recipes for our items (`kubejs/startup_scripts/_lib/removed_items.js`, `kubejs/server_scripts/other/creative_labels.js`, NCH `creativeLabelsSpecial`) | `compat/DonorItems`, `mixin/ItemHiddenMixin`, `mixin/StickyItTooltipMixin` |

Art comes from the Sticky It! and Labels jars at runtime (both All Rights Reserved: nothing of theirs is in this
jar; they are mandatory dependencies until phase 5 ships our own art).

Pack side (instance root): `tools/build_deploy.py` IN_TREE_MODS, `mod_sides.md`, `instance.json`, patcherMod's
`[mod_names]` default, `kubejs/startup_scripts/_lib/item_dict.js` (`i.cl_sticky_note`, `i.cl_label`, `i.cl_eraser`
and the three donor keys), `removed_items.js` (donor items hidden in JEI), `neoCraftHelper.js`
(`creativeLabelsSpecial`) and `kubejs/server_scripts/other/creative_labels.js` (donor recipes removed; note, eraser
and label recipes; the four special recipes).

Create: `face_paper` is in `create:non_movable`, so a moved host drops its paper. The schematicannon never prints
`face_paper`: a block with no item has an invalid `ItemRequirement`, which the cannon skips (verified in
`SchematicannonBlockEntity`), so nothing to implement there.

## Build

Part of the root Gradle build:

    ./gradlew :creativeLabels:build          # -> build/libs/creativeLabels-<ver>.jar
    ./gradlew :creativeLabels:installToMods  # copy into ../../mods (not while the game runs)

- `libs/` (git-ignored) holds compile-only copies of the JEI and Moonlight jars from `../../mods`; WTHIT comes from
  `../patcherMod/libs`.
- Mixins use Mojang names with a real refmap (`creative_labels.refmap.json`), like backportedVinery.
- `mods.toml` asks for Moonlight `[1.19.2-2.3.0,)`: Moonlight's version string starts with the Minecraft version
  (`1.19.2-2.3.7`), so a bare `[2.3,)` never matches and FML refuses to load (found on the scratch server 2026-10-21).
- Scratch server tests: `tools/creative_labels_test/scratch_server.sh` + `server_probe.js` (`/cltest <group>`), see
  sticky_notes.md 6.1.
- Headless tests: `tools/creative_labels_test/run.sh` from the instance root (spot rules, rotation, NBT round
  trips, colour mixing against vanilla, the fallback note shape placed on every face, the label mapping, the donor
  image maths on the decompiled PNGs).

## Rendering notes (phase 1, to check in game: SN-10)

- Model space of the captured note: the entity faces -z, so x is the viewer's right, y is down, z is away from the
  viewer. The flat 6 x 3 px face is the part at the largest z (the entity's back) and is taken as the paper plane;
  the lip is at the smallest z, 2.76 px toward the viewer. Texture columns 0..6 are sampled by the viewer-facing
  polygons, 6..12 (transparent in the donor files) by their backs; both sides are emitted.
- Face frames (`Spots`) are left-handed except UP (ceilings): there the note's x is mirrored within its spot so the
  placed shape stays a proper rotation of the wall one and its texture reads the same way from below.
- A capture that is not about 6 x 5.35 x 2.76 px, or no `head` part, falls back to the constants of ModelSticky.

## Rendering notes (phase 3, to check in game: label icons)

- Label icons are laid out in a "view frame" (`LabelLayout`): x to the viewer's right, y up as the viewer sees it. On
  walls and floors that is the Spots frame; on ceilings (UP faces) the frame's y is the placing player's facing, which
  is the bottom of their view when looking up, so the view frame flips y there. The icon and name therefore read
  upright to the player who placed the label (notes and the label paper mirror x instead, which a curl and a square
  do not mind).
- Texture orientation follows Labels' `LabelEntityRenderer` on the same Moonlight version: the rendered item
  texture's v = 0 is the bottom of the icon and u runs left to right, so the quad is bottom-left (0,0), bottom-right
  (1,0), top-right (1,1), top-left (0,1), counter-clockwise toward the viewer, normal out of the host. The render
  type is `entityCutoutNoCull`, so a winding error would still show the icon (checked by the headless test anyway).
- Sizes are Labels' numbers: an 8 px icon centred on the 10 px paper, or a 6 px icon moved up with the name fitted
  into 8 x 2 px under it; icon 0.03 px and text 0.06 px in front of the paper (no z-fighting with the chunk mesh).
- The name is drawn with `Font.drawInBatch` through a quaternion built from the view frame (`LabelLayout.fromAxes`):
  font x -> right, font y -> down, font z -> into the host. The quaternion is normalised in double precision because
  Mojang's `Quaternion.normalize()` uses the fast inverse square root and leaves it about 0.3 % short.
- IconDye ramp: light = `DyeColor.getFireworkColor()`, dark = `getTextColor()` when that is at most 85 % as luminous,
  else light scaled by 0.6 (white's text colour is brighter than its firework colour). Pixels are NativeImage's
  packed ABGR ints (R in the low byte); the pure parts are tested, the NativeImage loop is a thin wrapper.

## File map

```
build.gradle, gradle.properties, LICENSE.txt, BRIEF.md, README.md
src/main/java/com/SaintAlfonzo/creative_labels/
  CreativeLabels        @Mod: configs, registry, network, client setup through DistExecutor
  CLConfig              common + client specs, on/num/integer helpers
  CLRegistry            block, block entity, items, recipe serializer register
  CLMixinPlugin         gates mod-targeting mixins on that mod being installed
  block/                FacePaperBlock, FacePaperBlockEntity, FaceContent, FaceSnapshot, NoteData, LabelData, Spots, PieceHit
  item/                 StickyNoteItem, LabelItem, EraserItem, NoteColours, ItemLabels, LabelGate, AnvilLabels
  crafting/             LabelledCrafting (blockLabelledCrafting check for both mixins), NoteGrid (grid sorter), NoteDyeRecipe, NoteSpecialRecipe, NoteCopyRecipe, NoteEraseRecipe
  net/                  CLNetwork (SimpleChannel creative_labels:main), NoteTextPacket (C2S)
  mixin/                CraftingMenuResultMixin, FastBenchResultMixin (@Pseudo, only while fastbench is loaded),
                        ArmorDyeRecipeMixin, ItemHiddenMixin (donor items out of the
                        creative tabs), StickyItTooltipMixin (@Pseudo, by name; only while sticky_it is loaded)
  client/               ClientSetup, ClientHooks, FacePaperModel, FacePaperModelLoader, NoteShape, LabelQuads, DonorArt,
                        MemoryPackResources, FacePaperHighlight, NoteScreen, NoteItemColours,
                        FacePaperRenderer (label icons + names), LabelLayout (view frame), IconDye (dyed-icon ramp)
  compat/wthit/         FacePaperWthit (client plugin, waila_plugins.json)
  compat/jei/           CreativeLabelsJei (@JeiPlugin)
  compat/DonorItems     the donor items hidden from the creative tabs (registry lookup at call time)
src/main/resources/
  META-INF/mods.toml, pack.mcmeta, creative_labels.mixins.json, waila_plugins.json
  assets/creative_labels/lang/en_us.json, blockstates/face_paper.json, models/block/face_paper.json, models/item/*.json
  data/creative_labels/tags/blocks/paper/{posts,deny}.json, data/create/tags/blocks/non_movable.json
```
