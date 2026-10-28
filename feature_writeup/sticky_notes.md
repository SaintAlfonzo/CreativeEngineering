# Creative Labels: sticky notes and labels as face paper

Plan and build notes for **Creative Labels** (`creative_labels`), a new in-tree mod that replaces two mods' placed entities with one
block, `face_paper`: **sticky notes** (after Sticky It! 1.2.1) and **labels** with an item picture (after Labels 1.10).
It also keeps Sticky It's text labels on items. Until our own art exists, both old jars stay installed as unmodified
art donors with their gameplay off; then they are dropped. Test prefix `SN-`. Tracked in [TODO](todo/TODO.md)
`largecontent/stickynotes`.

## 0. At a glance

| Part | Status |
|---|---|
| Investigation of Sticky It 1.2.1 and Labels 1.10 (both decompiled, every class read; licences read) | done 2026-10-20 (section 2) |
| Creative Labels mod (`development/creativeLabels`, mod id `creative_labels`, GPLv3) | BUILT 2026-10-20 (`creativeLabels-1.0.0.jar`), installed in `mods/` 2026-10-21; [README](../development/creativeLabels/README.md) maps every part to its class |
| Phase 1: `face_paper` block, sticky notes placed by host shape, directional floors and ceilings, per-note dye / open / remove / erase, covered face | BUILT (3.2 to 3.5) |
| Phase 2: note items (leather-style colour in the grid and when placed, 4 specials, recipes), note screen with Sticky It's screen art, text labels on items (inventory, anvil, off hand), eraser, tooltips | BUILT (3.6 to 3.9) |
| Phase 3: labels on `face_paper` (Labels' interactions, real-colour icon, dyed icon, close-only drawing) | BUILT (3.10, 3.11) |
| Phase 4: donor jars with gameplay off (recipes, JEI, Sticky It's tooltip handler) | BUILT (3.13): mod side (creative-tab hiding, tooltip mixin) and pack side (JEI hiding, recipes) |
| Phase 5: our own textures and models, then drop both donor jars | PLANNED, later (3.14) |
| Headless tests | `tools/creative_labels_test/run.sh`: 4 suites, all pass (2026-10-21, section 6) |
| Scratch server tests | 2026-10-21: boots on a dedicated server; 60 checks over 41 SN- tests, all PASSED after 3 fixes (Moonlight range, FastWorkbench crafting path, mixin-package crash); section 6.1 |
| In-game tests | not run yet; first in game: SN-10, SN-12, SN-23, SN-28, SN-29, SN-39, SN-47, SN-48 (client-only), then SN-55 with the crafting table |

- **Files to open:** [development/creativeLabels/README.md](../development/creativeLabels/README.md) (what lives where),
  [BRIEF.md](../development/creativeLabels/BRIEF.md) (the build brief); the decompiled donor mods, `python3 tools/decompile_mod.py mods/<jar> <dir>`; Sticky It
  `client/model/ModelSticky.java`, `client/gui/StickyNoteTextScreen.java`; Labels `LabelEntity.java`,
  `LabelEntityRenderer.java`.
- **Knobs:** `creative_labels-client.toml`: `pictureDistance` (16 blocks), `textDistance` (6 blocks).
  `creative_labels-common.toml`: `noteLength` (32), `anvilCost` (1 level), WTHIT lines on/off.
- **How to measure:** place, dye, open, remove, erase and break notes on each host shape (3.3); punch, shoot and blow
  up papered blocks; JFR ([perf.md](perf.md)) on a wall of 200 labels and 1,000 notes.
- **Open decisions:** none (4.2). Phases 1 to 4 are built; phase 5 is pinned.

## 1. Overview

**The spec (user, 2026-10-20, over several messages):**

- One block, `face_paper`, in a new mod named Creative Labels. A face holds **either** one label **or** notes.
- **Notes:** the same size and curled model as Sticky It's placed note, on every side (floors and ceilings too).
  Up to five per face, spaced out: four corners and the centre. On floors and ceilings the paper faces the way the
  player faced when the first piece went on that face.
- Notes go only on standard shapes: full faces (chests and barrels too, sneaking to place); stairs: bottom and back
  faces, 3 on each side; slabs: 4 on the top or bottom, 2 on each side; fences, walls, bars and rods: the centre spot
  on top and bottom; similar rules for other standard shapes.
- Each note on its own: dye (right-click with a dye, mixed like leather), open (empty-hand right-click), remove
  (sneak + empty-hand right-click), wipe (eraser). All five spots filled: the host's face can't be used.
- Breaking is quick, like carpet. Explosions don't touch the paper; when the host block breaks by any means, notes
  drop with their data and labels drop blank.
- **Note items:** every Sticky It colour is its own variant, shown in JEI like potions, with crafting recipes; the 16
  dye colours plus the leftovers and specials. Stack to 64 without data. Written in hand on an edit screen that uses
  Sticky It's screen textures.
- **Text labels on items** stay: applied in the inventory, in the anvil, or with the off-hand method. The eraser
  takes them off, wipes a note in the crafting grid, and wipes a placed note.
- **Labels:** work like Labels without the entity problems. Sneak + empty hand clears the picture. A dye colours the
  icon, not the paper. Pictures are drawn in real colours (the recommended approach), only up close, configurable
  (16 blocks for pictures, 6 for text).
- **Tooltips:** the label lists every interaction; the note says how to write, that it can be placed, that it can be
  dyed when placed, and that it is added to items in the inventory, one item per line.
- **Recipes:** the current ones for now. **Art:** both mods' models and textures for now; custom art later, then the
  donor jars go. No converter for placed labels; no asking the authors.

## 2. Reference: the two mods today

### 2.1 Sticky It! 1.2.1

MCreator, All Rights Reserved, `both` in `mod_sides.md` since 2026-10-13.

- **Items:** `sticky_it:sticky_note` (stack 16; 4 paper + 3 iron nuggets makes 2), `sticky_it:eraser` (stack 1;
  6 dried kelp).
- **Text labels on items:** the note's anvil name is written onto the off-hand item as `LabelName1-3` with
  sneak + right-click (note in the main hand). Dye in the off hand recolours the note's name with a `§` code. The
  eraser clears the last line by writing `""` and always adds `Label: true`, so an erased item never stacks with clean
  copies again.
- **Placed note:** a `PathfinderMob` (10 health, no AI, no gravity, persistent) with the text, a texture index and the
  real-world date. It is killed by anything its `hurt()` doesn't block, including our TNT shrapnel
  ([tnt_shrapnel.md](tnt_shrapnel.md)), and has no loot table, so it vanishes. A full mob tick per note on the server;
  on the client a 15-layer mob renderer that builds a new model every frame.
- **Note model** (`ModelSticky`, computed from its code): **6 px wide, 5.35 px tall, standing up to 2.76 px off the
  surface**. A 3 px flat face, then folds of 0.94 px at 20 degrees and 1.41 px at 45 degrees, ending in a 1 px lip
  at 90 degrees. Its texture uses a 6x14 px strip of a 16x16 file.
- **Screen:** `textures/screens/stickygui.png` (352x16 text bar) and `stickyguidatee.png` (70x16 date bar) drawn above
  the screen centre, text in dark grey; `sticky_note_text.png` is referenced with a zero size.
- **Tooltip bug:** its `ItemTooltipEvent` handler reads with `getOrCreateTag()`, writing `tag: {}` onto every hovered
  stack. `{}` stacks don't stack with clean ones: the bowl bug of 2026-10-20. No other handler in the pack does it.

**Every Sticky It note texture** (`assets/sticky_it/textures/entities/`; main colour, then how Sticky It uses it):

| File | Main colour | In Sticky It |
|---|---|---|
| `sticky` | #ece44c yellow | index 0; also the base texture drawn under every colour layer |
| `sticky_blue` | #29abd1 | index 1 |
| `sticky_green` | #a3c832 | index 2 |
| `sticky_light_blue` | #90c8b5 (mint) | index 3 |
| `sticky_light_pink` | #e98680 (salmon) | index 4 |
| `sticky_orange` | #dc982f | index 5 |
| `sticky_pink` | #ef5b9d | index 6 |
| `sticky_cyan` | #47a092 | index 7 |
| `sticky_dark_green` | #6c8e4c | index 8 |
| `sticky_lavander` | #7a519f | index 9 |
| `sticky_light_gray` | #c0b0bd (pinkish) | index 10 |
| `sticky_gold` | #d8a43a | index 11, rare; throws a firework spark every second |
| `sticky_creeper` | #46913e, creeper face | index 12, rare |
| `sticky_inverted` | #131bb3 | index 13, rare |
| `sticky_smile` | #ece44c, smiley | index 14, rare |
| `sticky_red` | #c02026 | in the jar, never used |
| `sticky_magenta` | #6c182e (maroon) | in the jar, never used |
| `sticky_purple` | #5c2b86 | in the jar, never used |
| `sticky_itlogo` | 400x400 logo | not a note texture |

Normal spawns roll 0 to 10; 1 % roll 11 to 14.

### 2.2 Labels 1.10

MehVahdJukaar, All Rights Reserved, `both` since 2026-10-18. Uses Moonlight (GPLv3).

- **Item:** `labels:label`. In this pack: 8 paper around Supplementaries' antique ink makes 16.
- **Placed label:** a `HangingEntity` (like item frames), 10/16 square, flush on the host's support shape.
- **Interactions:** sneak + right-click with an item shows it (a copy; not used up); dye recolours it; glow ink sac
  makes it full-bright, ink sac undoes it; feather toggles the item's name; anything else is passed to the host.
- **Picture:** Moonlight's `requestFlatItemTexture(id, item, 16, postProcess)` renders the item into a framebuffer;
  Labels repaints it in a two-colour ink ramp per dye. Paper model and picture drawn every frame.
- **Why they disappear:** any damage kills a hanging entity and drops a blank label (a left-click on the labelled
  chest is enough; also arrows, shrapnel, explosions); a survival check every 100 ticks pops it when the host is moved,
  swapped or no longer solid, or an item frame overlaps it.

### 2.3 Licences

- **Sticky It:** CurseForge "All Rights Reserved unless otherwise explicitly stated", nothing stated; no source.
- **Labels:** CurseForge "All Rights Reserved"; its repo's "Supplementaries Team License" 1.1 forbids public
  distribution "in whole or in part" without written permission; modpacks may use the unmodified jar from
  CurseForge / Modrinth, not bundled; private sharing allowed.

Neither mod's art may go into our jar. Used by reference from the installed jars (3.12) until our own art replaces
it (3.14).

### 2.4 Pack state

- No pack script, tag or config refers to either mod. Local saves: no placed notes or labels.
- The server world `winecraft` has Labels' labels in use. No converter: they are remade by hand.

## 3. Plan

### 3.1 The mod

- `development/creativeLabels`, mod id `creative_labels`, display name "Creative Labels", version 1.0.0, GPLv3 (full
  text in the jar, `mods.toml` `license="GNU GPL3"`), built from the root Gradle build like More Mooblooms
  ([deploy.md](deploy.md) `IN_TREE_MODS`, `mod_sides.md` `both`).
- Dependencies: Moonlight (required; already in the pack); `sticky_it` and `labels` (required while they are the art
  donors, 3.12); WTHIT (optional).
- Ids: `creative_labels:sticky_note`, `creative_labels:label`, `creative_labels:eraser`, `creative_labels:face_paper`
  (block, no item). Item ids into `item_dict.js`; recipes through `global.NCH`.

### 3.2 The `face_paper` block

- **Cell:** a non-solid, no-collision block in the air cell in front of a host face, like glow lichen. State: six
  face booleans and `waterlogged`.
- **Block entity, per face:** `kind` (none / notes / label, **exclusive**), five note slots or one label, and for the
  up and down faces a **rotation** (the player's horizontal facing when the first piece went on that face; kept until
  the face is empty). No ticker; synced with the update tag.
- **Breaking:** hardness 0.1 and no tool, like carpet. Only the piece under the crosshair breaks
  (`onDestroyedByPlayer` removes that piece and keeps the block while anything is left). `RenderHighlightEvent.Block`
  outlines only that piece. Middle-click gives that piece's item (hit-aware `getCloneItemStack`).
- **Drops:** a note drops **with** its colour and text; a label drops **blank** (spec: labels without data), both
  when broken directly and when the host goes.
- **Host gone (any means):** mined, exploded, pushed, swapped, burnt. `updateShape` sees the host face's spots no
  longer fit (3.3) and drops those pieces as above. A face with nothing left clears its boolean; an empty cell turns
  back into air or water.
- **Explosions:** `face_paper` has a very high blast resistance, so explosions never remove paper directly; only a
  host the explosion removes drops its paper. Explosions damage entities before they remove blocks, so the dropped
  notes aren't caught by the same blast.
- **Pistons:** push reaction `DESTROY` (drops as above). **Damage:** none; mobs, projectiles, shrapnel and stray hits
  on the host do nothing.
- **Sneak clicks:** vanilla skips a block's `use` when you sneak holding an item. A `RightClickBlock` handler sets
  `useBlock = ALLOW` when the target is a `face_paper` piece, so sneak + right-click with a dye, eraser or item still
  reaches notes and labels.

### 3.3 Where paper fits: spots by host shape

**Note size and spacing.** Each note is Sticky It's model: 6 px wide, 5.35 px tall, curl up to 2.76 px out. In the
face's own frame (x left to right, y bottom to top, 16 px):

```
 y 16 +---------------------------+
      | TL [0.5-6.5]  TR [9.5-15.5]|   corner rows: y 10.65-16 and 0-5.35
10.65 |                            |   centre:      x 5-11, y 5.33-10.67
      |        C  [5-11]           |   no overlaps; 3 px between corner
 5.35 |                            |   columns, the centre fills the middle
      | BL            BR           |   band, so the spots stay spaced out
  y 0 +---------------------------+
```

The curl hangs at the bottom of each note (towards y 0 on walls; towards the player's facing on floors and ceilings).

**A spot is allowed** when the host fills the spot's whole footprint at the face. "At the face" means at the cell
boundary, or inset by at most 1/16 (chests, ender chests, barrels), plus the shape rules below. Labels need a
five-spot face (its 10x10 px centre square filled).

| Host | Spots |
|---|---|
| Full block face; inset up to 1/16 (chests, barrels) | all 5; a label fits |
| Stairs: bottom face, back face | all 5; a label fits |
| Stairs: each side (L-shaped) | 3: the two bottom corners and the top corner on the back half |
| Stairs: front (the lower riser at the boundary) and top (the back half at the boundary) | 2 each: the two bottom corners of the front, the two corners of the top's back half (in the face's rotation) |
| Slab, top or bottom (the half-height face, drawn 8 px in) | all 5 (no label: labels need a face at the boundary or 1/16 in) |
| Slab, each side (16x8) | 2: the bottom corners of a bottom slab, the top corners of a top slab |
| Double slab | all 5 (a full block) |
| Fences, walls, iron bars, panes, end rods, lightning rods, chains (vertical) | the centre spot on the top and on the bottom (the note overhangs the post); horizontal rods and chains: the centre on each end |
| Anything else (carpets, buttons, ladders, doors, trapdoors, plants, heads, odd modded shapes) | none |

How it is decided: block class first (`StairBlock`, `SlabBlock`, `FenceBlock`, `WallBlock`, `IronBarsBlock`,
`RodBlock`, `ChainBlock`), then tags for modded blocks that don't extend those (`creative_labels:paper/posts`,
`creative_labels:paper/deny`), then the shape test for full or nearly full faces. Spot masks for stairs, slabs and
rotated floors and ceilings are worked out in the face's frame after the face's rotation, so a stair's top-back corner
lands on the right side whatever way the stair faces.

**Covered face:** when all five spots of a five-spot face hold notes, the hit shape becomes the whole face panel; a
right-click that lands between notes does nothing, so the crafting table, furnace or chest behind can't be used
through that face. Faces that can't hold five notes never count as covered (user, 2026-10-20).

### 3.4 Notes, placed

| Action | Result |
|---|---|
| Right-click a host face with a note | placed in the free spot under the click, else the nearest free allowed spot; no free spot, nothing. On blocks with their own use (chests, barrels, furnaces), sneak + right-click places, a plain right-click opens the block. A plain yellow note has a 1 % chance to land as a **gold** note, with Sticky It's firework spark |
| Right-click a note with a dye | **mixes** the dye into the note's colour like leather (an undyed note takes the dye's colour; a dyed one averages with it, 3.6); one dye used unless creative. Special notes (3.6) don't take dye |
| Right-click a note with an empty hand | opens the note screen (3.7) |
| Sneak + right-click a note with an empty hand | removes that note into your inventory with its colour and text (dropped if full). A special note keeps its look only while `keepSpecialColor` is on (default on); off, it comes back as a plain note |
| Right-click a note with the eraser | a written note loses its text; a blank note goes back to plain yellow (colour or special look removed) |
| Right-click a note with another note | places it on the same face (as above) |
| Right-click a note with anything else | passed to the host block, unless the face is covered |
| Left-click | breaks that note quickly; it drops with colour and text |

Special looks come only from crafting (and gold from the 1 % placing chance).

### 3.5 Directional floors and ceilings

Paper on an up or down face turns to the player's horizontal facing when the face's first note or label is placed,
and keeps that rotation until the face is empty. Notes' curls and labels' pictures follow it.

### 3.6 Note items and colours

**Colour like leather** (user, 2026-10-20; replaces the 23 fixed variants):

- One item, `creative_labels:sticky_note`, stack 64. It implements vanilla `DyeableLeatherItem`, so the colour is any
  RGB value in `display.color`, mixed by vanilla's `dyeArmor` formula (average of the old colour and the new dyes,
  kept as bright as the brightest). A plain note has no colour tag and shows Sticky It's yellow paper.
- **Crafting:** any number of identical notes + **one** dye → that many notes with the dye mixed in, text kept
  (`NoteDyeRecipe`, a Java special recipe; its JSON through a new NCH helper). One note + several dyes also works
  through vanilla's own armour-dye recipe, which accepts any `DyeableLeatherItem`. Crafting again mixes again, so
  colours combine over several crafts.
- **Placed:** a dye on a placed note mixes the same way (3.4). This is the main way to colour notes.
- **Specials:** four fixed looks from Sticky It, stored as `tag.Special`, not dyeable:

| Special | Crafted from (plain notes + 1) | Look |
|---|---|---|
| inverted | ink sac | `sticky_inverted` |
| smile | glow berries | `sticky_smile` |
| creeper | gunpowder | `sticky_creeper` |
| gold | gold nugget; also a 1 % chance when a plain note is placed | `sticky_gold`, firework spark when placed and while placed |

  Any number of plain (undyed, non-special) notes + one ingredient. A special note **stays special when broken**
  (left-clicked, or dropped because its host broke). Only a sneak + empty-hand pick-up is governed by
  `keepSpecialColor` (`creative_labels-common.toml`, default on): off, the picked-up note comes back plain.
- **Eraser:** on a written note, wipes the text; on a blank note, returns it to plain yellow (colour and special
  removed). In the grid (note + eraser, the eraser stays as the remainder) and on placed notes alike. Unbreakable,
  stack 1.
- **JEI:** the plain note and the four specials are listed (a subtype interpreter on `Special`); dyed notes are not
  separate entries, like leather armour. Example recipes are registered for JEI: the 16 single-dye crafts and the
  four specials.
- **Stacking:** plain notes stack to 64; notes stack only with identical colour, special and text.

**Colour reevaluation** (what happens to Sticky It's files under dynamic colour):

| File | Fate |
|---|---|
| `sticky` (yellow) | **kept**, by reference: the plain note; also the source of the greyscale tint base |
| `sticky_gold`, `sticky_creeper`, `sticky_smile`, `sticky_inverted` | **kept**, by reference: the four specials |
| `sticky_blue`, `_green`, `_light_blue`, `_light_pink`, `_orange`, `_pink`, `_cyan`, `_dark_green`, `_lavander`, `_light_gray`, `_red`, `_magenta`, `_purple` | **dropped**: any colour now comes from tinting, so fixed colour files aren't needed |
| greyscale note paper (from `sticky`) | **generated** in memory: luminance kept, brightened so a tint shows the true dye colour |
| greyscale item icon (from `sticky_it:item/stickyitem`) | **generated** in memory, tinted through `ItemColors` like leather armour |
| four special item icons | **generated** in memory: the icon recoloured with each special's palette |

No per-colour recolours are generated at all, so the "only where no near colour exists" rule has nothing left to
apply to. For reference, single-dye results against Sticky It's files (redmean distance): light blue 33 (`blue`),
red 38 (`red`), yellow 45 (`sticky`) are near; orange 74, cyan 75, purple 77 (`lavander`), magenta 86 (`pink`),
light gray 90, blue 91, green 97, and the rest 116 or more are not. Swapping in a hand-made file for those three exact
results would make one dye look different from every mix around it, so it isn't planned.

- **Recipes kept** from the donors (spec): note = 4 paper + 3 iron nuggets makes 2; eraser = 6 dried kelp; label = 8
  paper + antique ink makes 16. They now make our items; the donors' own recipes are removed (3.13).
- **Copying a note** (user, 2026-10-20): one written note + 8 blank notes (any colour) in the grid makes 9 copies of
  the written note: its text, colour and special look. A Java special recipe like the others (`NoteCopyRecipe`).

### 3.7 Writing notes: the note screen

- **In hand:** right-click a note in the air (not sneaking) opens the screen; Done writes the text onto **one** note
  from the stack (a C2S packet; the server checks the length, `noteLength` 32). Empty text clears it.
- **Placed:** empty-hand right-click opens the same screen for that note; Done rewrites it.
- **Look:** Sticky It's screen art by reference: `sticky_it:textures/screens/stickygui.png` as the text bar,
  `stickyguidatee.png` as the date bar above it. The text is an edit field on the bar, dark grey as in Sticky It. The
  date bar shows the in-game day the note was placed, and who placed it (D7).

### 3.8 Text labels on items

Kept from Sticky It, with clean NBT: up to 3 lines in `CreativeLabels.Lines` (C5), written only when a line exists;
removing the last line removes the key, and the tag if it's then empty. A note must have text to label anything, and
**labelling uses up the note**, one per line, whichever way it's done.

| Way | How |
|---|---|
| Inventory | a written note on the cursor, right-click the item's slot: the line goes on the **whole stack** (`Item.overrideStackedOnOther` on the carried note; checked in 1.19.2's `AbstractContainerMenu`, no mixin). Works in every container and the creative inventory tab |
| Anvil | item on the left, written note on the right: the output is the item with the line added; costs `anvilCost` (1 level; vanilla won't let an output with cost 0 be taken); one note used (`AnvilUpdateEvent`) |
| Off hand | written note in the main hand, item in the off hand, sneak + right-click in the air (not at a block) |

- **Eraser on items:** the eraser on the cursor, right-click a labelled item's slot: removes its last line.
- **Full:** a fourth line is refused with a bass note and "No room for another label" on the action bar; nothing used.
- **One label per click** (user, 2026-10-20): a right-click drag with a note on the cursor (Mouse Tweaks) must not
  label every slot it crosses. The server allows one cursor labelling per player per 5 ticks; drag clicks arrive
  within a tick or two of each other, so only the first slot is labelled.
- **Labelled items can't be used in crafting** (user, 2026-10-20): while `blockLabelledCrafting` is on
  (`creative_labels-common.toml`, default on), a crafting grid (inventory or crafting table) with any labelled item
  in it gives no result. Erase the labels first. A mixin on the menus' shared result update
  (`CraftingMenu.slotChangedCraftingGrid`, used by both). Machines (furnaces, Create, IE) are not covered.
  FastWorkbench (in the pack) overwrites both grids' `slotsChanged` to queue its own
  `FastBenchUtil.slotChangedCraftingGrid`, so vanilla's is never called; `FastBenchResultMixin` makes the same check
  there and resends the empty result slot (FastWorkbench sends results in its own packet). Found and fixed on the
  scratch server, 2026-10-21 (6.1).
- **Tooltip on a labelled item:** its lines under the name, in grey; no header, no `[Shift]`.

### 3.9 Tooltips

Every line is its own line (spec: newlines for cleanness); no `[Shift]`.

**Sticky note:**

```
<text, in the note's colour, if written>
Right-click in the air to write
Can be placed on blocks
Can be dyed when placed
Right-click an item in your inventory to add this note to it
```

**Label:**

```
<the item it shows, if any>
Place on a block face
Sneak + right-click with an item: show it
Sneak + right-click with an empty hand: clear it
Right-click with a dye: colour the icon
Glow ink sac: glow
Ink sac: stop the glow
Feather: show or hide the name
Anything else uses the block behind
```

**Eraser:**

```
Right-click a labelled item in your inventory: remove its last label
Right-click a placed note: wipe it
Craft with a note: wipe it
On a blank note: back to plain yellow
```

### 3.10 Labels, placed

A label takes a face only where it fits (3.3) and only if the face has no notes.

| Action | Result |
|---|---|
| Right-click a host face with a label | placed (sneak on chests and other usable blocks) |
| Sneak + right-click with an item | shows that item (a one-count copy with its tag, so potions and dyed leather keep their colours); not used up |
| Sneak + right-click with an empty hand | clears the picture |
| Right-click with a dye | colours the **icon** (3.11); one dye used |
| Right-click with a glow ink sac / an ink sac | glow on / off; one used |
| Right-click with a feather | shows or hides the item's name under the picture |
| Anything else, or an empty hand | passed to the host with the hit moved onto its face: chests, barrels and drawers open through the label |
| Left-click | breaks quickly; drops a blank label |

### 3.11 Rendering

| Piece | How | Per-frame cost |
|---|---|---|
| Notes and label paper | one Forge `IDynamicBakedModel` for the block; the block entity hands each face's content to it as `ModelData` (kind, rotation, note slots with colour / special and written flag, host inset). Quads are built per piece and baked into the chunk mesh; a dyed note uses the greyscale base with its RGB baked into the vertex colours (any colour, no texture per colour); plain and special notes use their own textures | none; the section rebuilds when paper changes |
| Note shape | Sticky It's model: at client resource load our code bakes its entity layer `sticky_it:model_sticky` from the game's `EntityModelSet` and captures the polygons; the same shape is used on every side, rotated per face | none |
| Written notes | a few darker pen strokes over the paper, drawn by our code onto each colour in memory | none |
| Label paper | Labels' `labels:block/label` model (10x10 px), registered as an extra model and moved onto each face | none |
| Label icon, undyed | real colours: Moonlight `requestFlatItemStackTexture(id, stack, 16)` (renders the stack, so NBT tints show) in a block-entity renderer | one quad per label within `pictureDistance` |
| Label icon, dyed | Moonlight `requestTexture(id, 16, consumer, false)`: the consumer draws the stack (`drawItem`), reads the pixels (`download` / `getPixels`), maps each pixel's brightness onto a dark-to-light ramp of the dye's colour (our own code), and uploads it. Cached per stack + dye | same quad |
| Glow | full-bright light on the icon quad | none extra |
| Name (feather) | same renderer, within `textDistance` | only near |

**Close-only:** the renderer's `shouldRender(be, cameraPos)` checks `pictureDistance` (default 16) and `getViewDistance()`
returns it, so farther labels show their blank paper from the chunk mesh. Text uses `textDistance` (default 6). Both
are client config.

**Against item frames:** item frames are entities, so they tick (a survival check every 100 ticks), sit in entity
lookups and are synced to players; a label face costs the server nothing. On the client an item frame draws its
frame and a full item model every frame; a label's paper is in the chunk mesh and its icon is one quad, only up close.
Labels cost more in one way: each distinct icon is its own small texture and draw call, where item frames batch on the
block atlas.

### 3.12 Art from the donor jars (until phase 5)

The jars stay installed and unmodified; our code refers to their files by resource location; anything derived is
built in memory at resource load and never written to disk or shipped. The mechanism is the Vinery port's
(an in-memory pack added on `AddPackFindersEvent`, now the shared `BuiltInPacks.addMemory`, [pack_tooling.md](pack_tooling.md) 2.8; `BottleLooks` recolours; [alcohol.md](alcohol.md)
B4.8). Our own code, so it can be copied over.

| Piece | Source | How |
|---|---|---|
| Note shape | `ModelSticky` (code) | baked from their entity layer at runtime (3.11) |
| Note paper, 17 Sticky It colours | `sticky_it:textures/entities/sticky*.png` | entity textures aren't on the block atlas: added with `TextureStitchEvent.Pre`, loaded from their jar |
| Greyscale tint base for dyed notes | from `sticky.png`, brightened greyscale | in-memory pack |
| Special looks | `sticky_gold`, `sticky_creeper`, `sticky_smile`, `sticky_inverted` | stitched like the plain paper |
| Pen strokes | ours | drawn in memory over each colour |
| Note item icons | `sticky_it:item/stickyitem` (yellow) | plain by reference; a greyscale copy tinted by `ItemColors` for dyed notes; 4 special icons recoloured in memory with each special's palette |
| Note screen | `stickygui.png`, `stickyguidatee.png` | referenced |
| Eraser icon | `sticky_it:item/eraser` | our item model's parent / texture reference |
| Label item icon | `labels:item/label` | our item model with `"parent": "labels:item/label"` |
| Label paper | `labels:block/label` + `labels:entity/label` | extra model, referenced |

Heads-up: the deploy payload ships jars directly ([deploy.md](deploy.md)). Labels' licence allows private sharing and
modpacks that download the jar, the same position as the Vinery donor jars; a published pack would download both.

### 3.13 Donor jars, gameplay off (phase 4)

- **Sticky It's tooltip handler** cancelled at its head (a Creative Labels mixin on
  `net.mcreator.sticky_it.procedures.TooltipRenderProcedure.execute`, targeted by name). Ends the `{}` tags (the bowl
  bug, TODO `cooking/kitchen #5`, [kitchen.md](kitchen.md)) and its `[Shift]` text. Can ship ahead of everything else.
  Items already tagged keep their `{}`.
- **Recipes:** every `sticky_it:` and `labels:` recipe removed (KubeJS through NCH).
- **Old items removed from play** (user, 2026-10-20): `sticky_it:sticky_note`, `sticky_it:eraser` and `labels:label` are hidden from JEI and the creative tabs and can't be crafted (their recipes now make our items). They can't be unregistered while the jars are donors; leftover stacks stay inert and vanish with the jars in phase 5.
- **Old placed labels and notes:** still load (the mods are installed) and behave as before until broken; an optional
  one-off `/kill @e[type=labels:label]` and `@e[type=sticky_it:sticky]` clears them.
- Still running: Sticky It's block-break scan, damage handler and tick queue (cheap, idle without placed notes);
  Labels' registrations (its mixin configs are empty).

### 3.14 Phase 5: our own art, then drop the donors

Planned for later (user, 2026-10-20): custom textures and models for the notes (the plain paper, the greyscale tint base, the four specials), the pen strokes,
the note screen, the eraser, the label paper and the item icons. When they exist:

- every donor reference in 3.12 points at our own files; the in-memory pack and the runtime baking of
  `ModelSticky` go;
- `sticky_it` and `labels` come out of the dependencies, `mod_sides.md`, the pack and the deploy (removal list);
- the 3.13 mixin, recipe removals and JEI hiding go with them;
- old Sticky It / Labels entities and items in the server world are dropped by the game on load.

### 3.15 WTHIT

One plugin, per piece under the crosshair: a note shows "Sticky Note", its colour and text; a label uses
`IBlockComponentProvider.getOverride` (WTHIT 5.36.1) to show the host's name and icon, plus "Label: <item>".
`getOverride` swaps the state, not the position, so other mods' lines that read the host's block entity may be missing
while you look at a label.

### 3.16 Effort and order

| Phase | Size |
|---|---|
| 1. Block, block entity, spot rules, dynamic model with Sticky It's shape, per-note actions, WTHIT | large: the bulk; the first dynamic baked model in the pack |
| 2. Note items: leather-style dyeing, specials, JEI subtypes and example recipes, colour and eraser recipes, screen + packet, item labels (3 ways), tooltips, in-memory art pack | medium |
| 3. Labels: interactions, pass-through, icon renderer (real and dyed), distances | medium |
| 4. Donor jars off (the tooltip fix can go first, alone) | small |
| 5. Own art, drop donors | art work, then small |

Ship 1 to 4 together; the tooltip fix ahead if wanted.

### 3.17 Before building: other considerations

Found while reviewing the plan against the pack (2026-10-20). The first group is built in as listed; the second needs
a choice (Q1 to Q6 in 4.2).

**Built in as listed:**

| # | Issue | Handling |
|---|---|---|
| C1 | **Explosion shielding.** A very high blast resistance in an air cell would also block explosion rays and shield the blocks behind the paper | carpet-like resistance for the ray maths; Forge `onBlockExploded` overridden to do nothing, so paper is never removed by the blast itself; the host's removal still drops it (3.2) |
| C2 | **Picture NBT bloat.** A label storing a full copy of a shulker box, backpack or written book puts that whole inventory into chunk data and packets | store the item id plus only render-relevant tags (enchantment glint, potion, dye colour, custom model data); drop `BlockEntityTag`, inventories, pages, backpack data; cap the tag size, else id only |
| C3 | **Icon texture cache growth.** One Moonlight texture per distinct stack + dye; damage values and unique tags multiply them | key the cache on the stripped stack of C2 (no damage); textures freed on resource reload |
| C4 | **Dedicated server.** Screens, the baked model, the icon renderer and Moonlight texture calls are client-only | keep them in client-only classes reached through client event subscribers (the D&D drill crash was this mistake); scratch-server boot test before shipping |
| C5 | **NBT key clashes.** Bare `Text`, `Special`, `CELabels` could collide with other mods' tags | one compound `CreativeLabels {Text, Special}` on notes and `CreativeLabels.Lines` on labelled items; colour stays vanilla's `display.color` |
| C6 | **Text packets.** A client can send anything | server checks: length (`noteLength`), single line, `§` codes stripped, the player within reach, and (for placed notes) the note still there; last write wins if two players edit at once; an open screen closes if its note is removed |
| C7 | **Structure rotation and mirroring.** Templates, villages and schematics rotate blocks | `rotate` / `mirror` move the face booleans and the block entity's face slots and floor rotations together |
| C8 | **Create schematic cannon** (`CreateFastSchematicCannon` is in the pack). A block with no item can't be placed by the cannon, or could dupe | item requirement = the pieces' items (notes with their data, blank labels) |
| C9 | **Slab tops and bottoms** put notes 8 px inside the slab's cell, so the hit shape reaches outside the block's own cell. Legal, and rays from above work, but unusual | test first (SN-05); if a mod chokes on it, slab half-faces fall back to no notes |
| C10 | **Create contraptions.** If a papered cell is glued into a moving contraption, Create renders it without our model data | mark `face_paper` non-movable for Create so a moved host simply drops its paper (3.2) |
| C11 | **Mob spawning, light, occlusion** | not a spawn surface; no light blocking; never culls neighbour faces |
| C12 | **Flowing water.** A waterloggable block stops flowing water like a sign | accepted |
| C13 | **Gold roll and spark** | the 1 % roll on the server; the spark is a client particle from `animateTick` |
| C14 | **Pack chores** | root `settings.gradle`, `IN_TREE_MODS`, `mod_sides.md` row (`both`), `instance.json` user-added entry, pack manifest, GPLv3 file, jar archive; `item_dict.js` keys; lang plus the name audit ([names.md](names.md)); tooltip lines recorded in [tooltips.md](tooltips.md) |
| C15 | **Testing** | headless tests for spots, rotation, colour mixing and NBT; a scratch dedicated server ([deploy.md](deploy.md) tooling) for C4; then the SN- suite in game |

**Settled (user, 2026-10-20):**

| # | Question | Decision |
|---|---|---|
| Q1 | Placing a block into a papered cell | refused; take the paper off first |
| Q2 | Is paper flammable? | no |
| Q3 | Mouse Tweaks right-click drag labelling every slot | **not allowed**: one label per click (3.8); labelling always uses up the note |
| Q4 | Right-clicking a backpack, shulker box or bundle slot with a note on the cursor | labels the container; left-click still puts the note in |
| Q5 | What a label picture keeps (C2) | id plus glint, potion, dye colour, custom model data |
| Q6 | Labelled items in crafting | **blocked**, configurable (`blockLabelledCrafting`, default on; 3.8) |

## 4. Decisions

### 4.1 Settled (user, 2026-10-20)

- New mod Creative Labels; block `face_paper`; one label **or** notes per face.
- Notes: Sticky It's size and curled model on every side; five spots, spaced; directional floors and ceilings.
- Shapes: full faces (sneak on chests and barrels), stairs bottom/back + 3 per side, slabs 4 top/bottom + 2 per side,
  posts centre top and bottom, nothing on non-standard shapes.
- Per-note dye (mixed like leather) / open / remove / eraser wipe; five notes cover the face; faces that can't hold five never cover.
- Carpet-speed breaking; explosions don't touch paper; host broken by any means: notes drop with data, labels blank.
- Colour like leather: any number of notes + 1 dye in the grid, mixes over several crafts, and dye on placed notes; Sticky It's colour files ignored. Specials: inverted (ink sac), smile (glow berries), creeper (gunpowder), gold (gold nugget, and 1 % when a plain note is placed, firework kept); `keepSpecialColor` on by default and only for sneak pick-ups; broken specials stay special. Eraser on a blank note returns it to yellow; eraser unbreakable. No Create mixing recipe (skipped). Stack 64 without data.
- Slabs: 5 on top and bottom; stairs: also 2 on the front and 2 on the top. Posts: fences, walls, iron bars, glass panes, end rods, lightning rods, chains (not fence gates). Old donor items removed from play, recipes kept for ours.
- Note screen with Sticky It's screen art. Item labels kept: inventory, anvil, off hand. Eraser on items, in the grid
  and on placed notes.
- Labels: sneak + empty hand clears the picture; dye colours the icon; real colours; picture 16 and text 6 blocks,
  configurable.
- Current recipes for now; donor art now, own art later (phase 5); no converter; no asking the authors.
- Specials stay special when broken; `keepSpecialColor` only for sneak pick-ups. Smile from glow berries; any number
  of notes + one dye per craft. Copying: 1 written + 8 blank notes makes 9. Labelling uses up the note, one label per
  click; labelled items blocked from crafting (configurable, default on). Q1, Q2, Q4, Q5 as recommended.

### 4.2 Open

Only the colourful ink idea below. O1 to O9, P1 (smile uses glow berries) and P2 (any number of notes + one dye per craft) and Q1 to Q6 are all
settled (4.1, 3.17).

Carried over, recommended and assumed unless changed: D7 (the in-game day and placer on the note screen and in WTHIT),
L6 (Create contraptions: later).

New idea, open (scratchpad 2026-10-21, [TODO](todo/TODO.md) `largecontent/stickynotes #6`): **colourful ink**, crafted
from ancient ink + 8 different dyes. On a label it gives realistic item colours; it also works as a dye, or used in the
world (an item with durability) gives a random dye colour per use. Open: how it sits next to phase 3's undyed icons,
which already show real colours (3.10).

The same scratchpad's sticky note list is this design and needs no new work: own logic in place of the donor mods
(3.1), a block not an entity (3.2, 4.3), several notes per face (3.3), right-click in the inventory (3.8), a short
multi-line tooltip with no shift-expand (3.9), WTHIT on placed paper (3.15). "Disappearing labels" is the hanging
entity dying to any damage (2.2); `face_paper` replaces it (`largecontent/stickynotes #1`, `#3`).

### 4.3 Rejected

- Keeping the entities and hardening them (one mixin): fixes the popping but keeps an entity per piece and two
  systems. Blocks chosen.
- A block-entity renderer for all paper; a finite state model (6 faces x 5 spots x any colour); Labels' item-only
  picture call (loses stack tints); copying either mod's art into our jar (licences).

### 4.4 Limits

- The cell in front of papered faces can't hold another block (a torch, a button); item frames still fit.
- Paper doesn't travel with Create contraptions or pistons; it drops.
- Text isn't drawn on placed notes (6 px wide); pen strokes show a note is written.

## 5. File map

Today: `mods/sticky_it-1.2.1-forge-1.19.2.jar`, `mods/labels-1.19.2-1.10.jar`, `config/labels-client.toml`,
[mod_sides.md](../mod_sides.md) rows, [tools/decompile_mod.py](../tools/decompile_mod.py).

Planned, `development/creativeLabels/src/main/java/.../creativelabels/`:

- `block/`: `FacePaperBlock`, `FacePaperBlockEntity`, `FaceContent`, `NoteSlot`, `LabelData`, `Spots` (spot
  geometry, host-shape rules, rotation; pure logic for headless tests), `FacePaperInteractions` (the
  `RightClickBlock` handler).
- `item/`: `StickyNoteItem`, `LabelItem`, `EraserItem`, `NoteColours` (dye mixing, specials), `ItemLabels` (`CreativeLabels.Lines`
  read/write), `AnvilLabels`.
- `crafting/`: `NoteColourRecipe`, `NoteEraseRecipe`.
- `net/NoteTextPacket`; `client/`: `NoteScreen`, `FacePaperModel` + loader, `LabelIconRenderer`, `IconDye`,
  `FacePaperHighlight`, `Tooltips`, `DonorArt` (in-memory pack, `ModelSticky` capture, atlas stitching).
- `compat/`: `wthit/FacePaperProvider`, `jei/` subtype interpreter; `mixin/StickyItTooltipMixin`.
- Config: `CLConfig` (common + client specs). Resources: lang, item models, `waila_plugins.json`, tags.
- Pack: `item_dict.js` keys, NCH helper for the special recipes, donor recipe removals and JEI hiding.

## 6. Offline checks

- **Decompile:** `python3 tools/decompile_mod.py mods/<jar> <dir>`.
- **Census of a save** (placed notes and labels, text labels, empty `{}` tags): done 2026-10-20 with scratch scripts;
  worth keeping as `tools/nbt_census.py`.
- **Headless tests:** `tools/creative_labels_test/run.sh` from the instance root, after
  `./gradlew :creativeLabels:build` (runs against `build/classes`, no game). Four suites:
  - `SpotsTest`: the host table of 3.3 on real vanilla blocks (full, 1/16 and 2/16 insets, stairs per facing and
    half, slabs, double slab, posts, deny/posts predicates), face frames, floor rotation, piece boxes, hit
    resolution, the covered-face rule, interactions (spot choice, host-gone re-fit, removal, pick-up and eraser).
  - `NbtTest`: note item NBT, item label lines, label picture stripping (C2), face / note / label round trips, the
    Mask remap (C7), colour mixing against vanilla's `dyeArmor`, the note text sanitiser (C6), the four recipe
    matchers on a 3x3 grid.
  - `NoteShapeTest`: the fallback note shape on every face (paper plane, curl direction, winding), the label model
    mapping, DonorArt's image maths on the decompiled donor PNGs.
  - `LabelRenderTest`: the dyed-icon ramp and pixel packing, the label icon / name view frame on every face, the
    text rotation quaternion.
  - 2026-10-21 run: 235 + 142 + 1828 + 394 checks, all pass.

### 6.1 Scratch server (2026-10-21)

A dedicated server on a superflat world, driven from its console; no game client. Scripts:
[tools/creative_labels_test/scratch_server.sh](../tools/creative_labels_test/scratch_server.sh) (`setup OUT`, `boot
OUT`, `cmd OUT "<command>"`, `stop OUT`: every non-client jar of `mods/` by [mod_sides.md](../mod_sides.md), copies of
`kubejs/` and `config/`) and [server_probe.js](../tools/creative_labels_test/server_probe.js) (scratch-only server
script, `/cltest place|actions|host|pistoncheck|items|labels|donors`). Clicks go through
`ServerPlayerGameMode.useItemOn` on Forge fake players, the path a client's right-click packet takes on the server
(RightClickBlock event, the paper's `use`, the item's `useOn`); breaking through `destroyBlock` with the fake player
aimed at the piece; crafting through the real recipe manager and the inventory menu; the anvil through a real
`AnvilMenu`. Results: [testing/creative_labels_scratch_2026-10-21.txt](testing/creative_labels_scratch_2026-10-21.txt).

- **Boot (C4):** the server boots with the mod; no client class is loaded by it (the `invalid dist` errors in the
  log are other mods': the pack's own server shows the same 10 without Creative Labels, and the ones during the run
  come from other mods' recipe classes while the probe walks every recipe).
- **Final run, world cl4: 60 checks, all PASSED.** Covered, server half: SN-01 to SN-09, SN-11, SN-13 to SN-19,
  SN-21, SN-22, SN-24 (gold rate: 4 of 400), SN-25 to SN-27, SN-30 to SN-34, SN-36 to SN-38, SN-40 to SN-45, SN-49,
  SN-53 to SN-55. The host table of 3.3 checked on 20 hosts (3 stair states, 3 slab states, 7 posts, 7 non-standard).
- **Not reachable without a client:** SN-10, SN-12, SN-20, SN-23, SN-28, SN-29, SN-35, SN-39, SN-46 to SN-48,
  SN-50, SN-51 (rendering, screens, JEI, tooltips, WTHIT, hit detection of real rays), and the look of SN-24, SN-36,
  SN-38, SN-40, SN-42. SN-54 checks the 5-tick gate, not a real Mouse Tweaks drag.

Bugs found and fixed on the way (jar rebuilt and reinstalled each time; the headless suites still pass):

| # | Bug | Fix |
|---|---|---|
| 1 | FML refused to load the mod: `moonlight` range `[2.3,)` never matches Moonlight's version `1.19.2-2.3.7` (Maven ordering puts it below 2.3). Client and server alike | `mods.toml`: `[1.19.2-2.3.0,)` (Supplementaries and Labels write theirs the same way) |
| 2 | `blockLabelledCrafting` did nothing in this pack: FastWorkbench overwrites both grids' `slotsChanged` and queues its own result update, so the vanilla method the mixin sat on never runs | `FastBenchResultMixin` on `FastBenchUtil.slotChangedCraftingGrid` (gated on `fastbench`), shared check in `crafting/LabelledCrafting` |
| 3 | The first fix put the shared helper in the mixin package; the server crashed on the first craft (Mixin refuses to load ordinary classes from its package) | helper moved to `crafting/` |

## 7. In-game test suite

Rewritten for the full spec before anything was built (earlier drafts were never run). Boxes are ticked from real
play only; "server half PASSED" notes are from the scratch server (6.1).

Placing and shapes:

- [ ] **SN-01 five spots:** right-click a stone face aiming at each corner and the centre → five notes, spaced, none
  overlapping; a sixth does nothing. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-02 nearest free spot:** click an occupied spot → the note lands on the nearest free spot. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-03 chests and barrels:** plain right-click opens them; sneak + right-click places a note; five notes fit on
  a chest's front. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-04 stairs:** bottom and back faces take five; each side takes three (bottom corners, top corner on the back
  half); the front and the top take two each; for all four stair facings and upside-down stairs. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-05 slabs:** a bottom slab's top takes five; a top slab's bottom takes five; sides take two (the
  half's corners); a double slab takes five. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-06 posts:** fence, wall, iron bars, glass pane, end rod, lightning rod, chain → one note on the centre of
  top and bottom only. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-07 non-standard:** carpet, button, ladder, door, trapdoor, flower, head → no note. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-08 floor direction:** place the first note on a floor facing north, then one facing east → both point north;
  break all, place facing east → east. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-09 notes or label:** a face with notes refuses a label and the other way round; notes on one face and a
  label on another face of the same cell both show. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-10 model:** placed notes have Sticky It's curled shape and size on walls, floors and ceilings.

Per-note actions:

- [ ] **SN-11 dye placed:** red dye on one note → only it turns red, one dye used; then blue → a red-blue mix (the same
  colour as leather dyed red then blue); a special note refuses dye. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-12 open:** empty-hand right-click → the note screen with Sticky It's bars, the text and the placing day; edit,
  Done → WTHIT shows the new text.
- [ ] **SN-13 remove:** sneak + empty-hand right-click → that note in your inventory with colour and text. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-14 eraser on a placed note:** right-click a written red note with the eraser → text gone, still red; again →
  plain yellow. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-15 covered face:** five notes on a crafting table → clicks between them don't open it; remove one → the
  empty spot opens it. Three notes on a stair side or two on its front never block anything. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-16 pass-through:** two notes on a furnace; right-click a note holding a coal → the furnace opens. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-17 break:** left-click one note → breaks about as fast as carpet, drops with its data; the others stay;
  only the hovered note is outlined; middle-click in creative gives that note. Scratch server 2026-10-21: server half PASSED.

Breaking the host, damage:

- [ ] **SN-18 host mined:** mine a block with five notes and a label on another of its faces → notes drop with data,
  the label drops blank. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-19 explosion:** TNT beside a papered wall that survives → no paper lost; TNT that destroys the wall → notes
  drop with data, labels blank, none destroyed by the blast. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-20 shrapnel, arrows, punches:** shoot and punch papered blocks → nothing pops.
- [ ] **SN-21 pistons:** push a papered block → its paper drops as in SN-18. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-22 water:** notes under water keep the water. Scratch server 2026-10-21: server half PASSED.

Note items:

- [ ] **SN-23 JEI:** JEI lists the plain note and the four specials, with example recipes for the 16 dyes and the
  specials.
- [ ] **SN-24 any colour:** notes dyed with each of the 16 dyes, and mixes, look like their leather armour colour when
  placed and as items; place 200 plain notes → about 2 come out gold, with the firework spark. Scratch server 2026-10-21: server half PASSED (gold rate only: 4 of 400; colours need a client).
- [ ] **SN-25 stacking:** 64 plain notes stack; red notes stack with red; a written note doesn't stack with blank ones. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-26 colour in the grid:** 12 plain notes + red dye → 12 red notes; 12 red + blue dye → 12 red-blue mixes;
  the text of a written note survives; 8 plain notes + gunpowder → 8 creeper notes. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-27 eraser in the grid:** red "Iron" + eraser → blank red note, eraser stays; that note + eraser → plain
  yellow. The eraser never breaks. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-28 write in hand:** right-click a stack of notes in the air → screen; "Iron", Done → one written note, the
  rest unchanged.
- [ ] **SN-29 tooltips:** note, label and eraser show the lines of 3.9, each on its own line, no `[Shift]`.

Item labels:

- [ ] **SN-30 inventory:** written note on the cursor, right-click 32 cobblestone in a chest → all 32 show the line;
  one note used. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-31 anvil:** cobblestone left, written note right → output with the line, 1 level; one note used. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-32 off hand:** note in the main hand, sword in the off hand, sneak + right-click in the air → the sword has
  the line. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-33 three lines:** a fourth line is refused, nothing used. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-34 eraser on items:** eraser on the cursor, right-click until no line is left → the stack merges with clean
  cobblestone. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-35 creative tab:** SN-30 and SN-34 in the creative inventory tab.

- [ ] **SN-53 copy:** written red "Iron" + 8 blank notes of mixed colours → 9 red "Iron" notes. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-54 drag:** Mouse Tweaks right-click drag with a written stack of notes across 5 cobblestone slots → only
  the first slot is labelled, one note used. Scratch server 2026-10-21: server half PASSED (5-tick gate; a real drag needs a client).
- [ ] **SN-55 crafting blocked:** a labelled plank in the inventory grid → no result; set `blockLabelledCrafting =
  false` → it crafts, unlabelled output. Scratch server 2026-10-21: server half PASSED (after the FastWorkbench fix).

Labels:

- [ ] **SN-36 place:** a label on a chest front sits flush on its face. Scratch server 2026-10-21: server half PASSED (placement; the look needs a client).
- [ ] **SN-37 picture:** sneak + right-click with a diamond → the diamond in real colours, not used up. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-38 stack tints:** labels showing a healing potion and dyed leather armour → their colours. Scratch server 2026-10-21: server half PASSED (picture NBT kept and stripped; the colours need a client).
- [ ] **SN-39 special renderers:** a chest, a shield, a Create cogwheel → like their inventory icons.
- [ ] **SN-40 dyed icon:** red dye → the icon turns red shades, the paper stays. Scratch server 2026-10-21: server half PASSED (dye stored; the icon needs a client).
- [ ] **SN-41 clear:** sneak + empty hand → picture cleared. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-42 glow, feather:** glow ink sac → full-bright; ink sac → normal; feather → name under the icon. Scratch server 2026-10-21: server half PASSED (flags stored; the look needs a client).
- [ ] **SN-43 open through:** empty-hand right-click the label → the chest opens. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-44 break:** left-click the label → breaks like carpet, drops a blank label. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-45 floor label:** a label on a floor turns to your facing. Scratch server 2026-10-21: server half PASSED.
- [ ] **SN-46 distances:** pictures disappear past 16 blocks (paper stays), names past 6; change both in
  `creative_labels-client.toml` → they follow.
- [ ] **SN-47 WTHIT:** a note → "Sticky Note", colour, text; a label → the chest's name and icon plus "Label: Diamond".

Donor jars and performance:

- [ ] **SN-48 bowls stack:** hover a bowl, put it with fresh bowls → they stack.
- [ ] **SN-49 donors off:** no `sticky_it:` or `labels:` recipe or item in JEI. Scratch server 2026-10-21: server half PASSED (recipes and creative tabs; JEI needs a client).
- [ ] **SN-50 resource reload:** F3+T → notes, labels and icons still right.
- [ ] **SN-51 performance:** 200 labels and 1,000 notes in view → JFR shows the icon renderer as the only per-frame
  cost.
- [ ] **SN-52 phase 5:** with our own art and both donor jars removed, everything above still passes.

## 8. History

- 2026-10-20: Sticky It 1.2.1 investigated after the bowl stacking bug was traced to its tooltip handler; first plan
  (notes as a multiface block, item text labels by cursor right-click).
- 2026-10-20: the user clarified the "disappearing labels" are the Labels mod; Labels 1.10 investigated
  (`HangingEntity` popped by any damage or a failed survival check); labels planned as faces of the same block.
- 2026-10-20: spec round 1: one face-paper block, five notes per face, per-note dye / open / remove, covered face,
  carpet-speed breaking, real-colour pictures investigated (Moonlight `requestFlatItemStackTexture`).
- 2026-10-20: spec round 2: converter dropped; pictures only up close; both mods' art wanted; licences read (no
  redistribution), so donor jars + in-memory derivatives (the Vinery route).
- 2026-10-20: spec round 3 (this version): new mod Creative Labels; notes the size and curled model of Sticky It's
  (measured from `ModelSticky`: 6 x 5.35 px, 2.76 px curl) on every side; directional floors and ceilings; spots by
  host shape (stairs, slabs, posts, inset faces); explosions don't touch paper, host breaks drop notes with data and
  labels blank; 23 note variants in JEI with recipes (Sticky It's colours listed, including three unused textures);
  Sticky It's screen art; item labels by inventory, anvil and off hand; eraser on items, in the grid and on placed
  notes; dyed label icons via Moonlight `requestTexture` + our own recolour; distances 16 / 6, configurable; phase 5
  (own art, drop donors) planned. Test suite rewritten (nothing had run).
- 2026-10-20: spec round 4: colour like leather (any number of notes + 1 dye in the grid, mixes over crafts, dye mixes
  on placed notes, Sticky It's colour files dropped); specials inverted / smile / creeper / gold by crafting, gold also
  1 % on placing, `keepSpecialColor` default on; eraser on a blank note returns plain yellow, unbreakable; slabs 5 on
  top and bottom, stair fronts and tops 2; faces that can't hold five never cover; old donor items removed from play,
  recipes kept; Create mixing recipe considered (needs a `BasinRecipe.apply` hook, results are static) and skipped by
  the user. Colour reevaluation: 5 donor files kept, 13 dropped, greyscale bases and special icons generated.
- 2026-10-20: specials stay special when broken (`keepSpecialColor` only for sneak pick-ups); pre-build review added
  (3.17: C1 to C15 handled, Q1 to Q6 to choose), incl. explosion shielding, picture NBT bloat, dedicated-server
  safety, NBT key namespacing, schematic cannon and slab-top hit shapes.
- 2026-10-20: last choices settled: one label per click (drag labelling blocked, 5-tick server gate), note copying
  (1 written + 8 blank = 9), labelled items blocked from crafting grids (`blockLabelledCrafting`, default on), smile
  from glow berries, one dye per multi-note craft, Q1/Q2/Q4/Q5 as recommended. `CELabels` renamed
  `CreativeLabels.Lines` in 3.8 (C5). SN-53 to SN-55 added. No open decisions.
- 2026-10-20: phases 1 to 4 built (`development/creativeLabels`, `creativeLabels-1.0.0.jar`) with four headless
  suites (`tools/creative_labels_test/run.sh`); pack side wired (item_dict, NCH `creativeLabelsSpecial`,
  `creative_labels.js`, mod_sides, build_deploy).
- 2026-10-21: installed in `mods/`. Scratch dedicated server (6.1): Moonlight range fixed (FML refused the mod),
  `blockLabelledCrafting` extended to FastWorkbench's crafting path (it bypassed the vanilla one), a mixin-package
  crash from that fix fixed; final run 60 checks PASSED. Status table, 6 and 7 updated; TODO `largecontent/stickynotes
  #1-4` to Testing.
