# Texture sources

Where every pack-owned texture came from, the texture rule, and the textures that break it. One row per texture in
[texture_sources/ledger.tsv](texture_sources/ledger.tsv) (generated); the hand-kept part is
[texture_sources/sources.tsv](texture_sources/sources.tsv). Audit started 2026-11-13.

## 0. At a glance (2026-11-13)

2743 PNGs under `development/**/textures` and `kubejs/assets/**/textures` (build output skipped).

| Status | Count | Meaning |
|---|---|---|
| ok | 2363 | 2022 exact copies of upstream or vanilla art, 136 Mojang 26.1 baby mobs, 203 made with the owner's tools, 2 owner drafts |
| swap | 4 | AI art (or AI-directed): replace with human art. The four echo tools |
| redo | 27 | AI transform of human art: redo with the owner's tools from the recorded source, or replace |
| confirm | 349 | No evidence either way (most predate the session logs): owner to say |

Contact sheets: [swap and redo](texture_sources/sheet_swap_redo.png),
[confirm, edited or unmatched](texture_sources/sheet_confirm_painted.png) (pixels off any clean recolour, or no
source found), [confirm, clean recolours](texture_sources/sheet_confirm_recolour.png) (a human sprite in new
colours, tool unknown).

No texture in the pack is image-model output. The session logs show no freehand drawing into a texture: every AI
hand on a texture was a script that recoloured, shifted, greyed or split a human sprite. Shape-drawing calls in the logs
were preview and contact sheets only.

## 1. The texture rule

- Every texture is human-made: upstream or vanilla art used under its licence, or the owner's own drawing.
- Or it is made from human art by the owner's tools, on a source and palette a human picked:
  `textureswapper/palette_swap.py` (recolour, bulk recolour), `combine_tex.py` (layering), `scale_tex.py` and
  `upscale_sprites.py` (nearest-neighbour scaling). `textureswapper/` is the owner's local folder (git-ignored).
  Scripts that only call these tools count (`make_seasoning.py`, `make_salt.py`, `make_psugar.py`, `gen_jam_art.py`).
- Not allowed: AI-drawn pixels, AI-chosen palettes or ramps, and one-off AI scripts that transform pixels themselves
  (hue shifts, brightness maps, greyscale splits) instead of going through the tools above.
- Every texture that is not a plain upstream copy gets a row in `sources.tsv` in the same session: source file(s) and
  jar, tool, and what it is used for. Rerun `python3 tools/texture_provenance.py` after adding or changing textures.

## 2. Statuses and what to do

| Status | Origin(s) | Action |
|---|---|---|
| ok | upstream, vanilla, mojang-26.1, human, owned-tool | none |
| swap | ai-drawn, or ai-transform the owner wants replaced | new art by the owner, or a fresh owned-tool derivation from a human sprite |
| redo | ai-transform | rerun from the recorded source with `palette_swap.py` / `combine_tex.py`; the result usually looks the same |
| confirm | unknown, mixed | owner says who made it and how; the answer becomes a `sources.tsv` row |

## 3. How the ledger is built

`tools/texture_provenance.py` (about a minute):

1. **Pixel copy.** Every texture in every third-party jar and folder on disk (`mods/`, `server_mods/`, `devmods/`,
   `disabledmods/`, `jar_archive/`, `DecompiledMods/`, `*_upstream/`, member `libs/`, vanilla 1.19.2, resource pack zips;
   about 155k textures; the pack's own jars are skipped) is indexed by RGBA content. Same pixels = `copy`.
2. **Recolour test.** Otherwise a third-party sprite with the same alpha silhouette (for opaque squares, one sharing a
   name word) is compared colour by colour. Every source colour going to exactly one output colour = `recolour`
   (`consistency=1.000`); pixels that break that map = `edited`, with the count in `offmap_px`. No candidate =
   `unmatched`. A match from an unrelated file (`unchanged=0.00`, different name) is a lead, not proof: few-colour
   sources recolour onto almost anything.
3. **Session logs.** Claude Code transcripts (`~/.claude/projects/...`, from 2026-10-14; earlier work has none) are
   scanned for tool calls that name the file: `owned` (called the owner's tools), `paint` (ad-hoc pixel code that
   saved images), `script` (a generator). The current audit's own calls are skipped.
4. **sources.tsv** rows override 1 to 3 (first matching glob wins). Rows it does not cover get an automatic origin:
   copy = upstream/vanilla ok; owned-only evidence = owned-tool ok; paint without owned = ai-transform redo;
   anything else = confirm.

Limits: textures from before 2026-10-14 have no log evidence, so a clean recolour made with `palette_swap.py` and one
made by an AI script look the same; those stay `confirm`. Mojang 26.1 files are not hash-checked (no 26.1 jar on disk).

## 4. Swap: AI art

| Texture | What it is | Source | Note |
|---|---|---|---|
| Creative Equipment `item/echo_axe`, `echo_pickaxe`, `echo_shovel`, `echo_hoe` (+ `.mcmeta`) | 12-frame animated strips | vanilla `diamond_<tool>` shape | Owner flagged. Frame 0 is an exact one-colour-to-one-colour recolour of the vanilla diamond tool, so not freehand, but `tools/echo_texture.py` (AI-written) picked the 12-step glow ramp and which pixels glow. Replace: owner draws frame 0 in the ramp colours (or a `palette_swap.py` of the diamond tool onto a human-picked echo palette), then `echo_texture.py animate` only steps the ramp |

## 5. Redo: AI transforms of human art

| Texture(s) | Source (human) | How it was made | Redo with |
|---|---|---|---|
| Rustic member `block/glowing_calamari_crate_side`, `_top` | Rustic Delight 1.7.0 `calamari_crate_side`, `_top` | ad-hoc brightness remap onto the Glowing Calamari colours (14 to 14 colours, wood unchanged) | `palette_swap.py` with the calamari shades only, glow ink sac palette. Owner flagged; the plain Calamari Crate is an exact Rustic copy (ok) |
| `kubejs/assets/wares/textures/block/*` (cardboard box, package sides) | Wares packages and boxes; Dustrial Decor cardboard colours | `make_cardboard.py` own remap with a tape mask | `palette_swap.py` (`--drop` for the tape) |
| Creative Compat `block/weathered_cardboard_box` | Design and Decor box sheet | `make_cardboard.py` greying | `palette_swap.py` onto a grey palette |
| `kubejs/assets/solapplepie/textures/items/lunchbox_dye/*` (8) | the four lunchbox looks | `gen_lunchbox_looks.py` greyscale split into dye base + overlay | `palette_swap.py` onto a grey ramp for the base; overlay by hand or `combine_tex.py` |
| Canned food: CD `canned_cabbage` (8 px edited), `canned_carrot`; `canned_goods` `canned_fish`, `canned_mushrooms` | Canned Goods can sprites | ad-hoc recolour (2026-10-21) | `palette_swap.py` from the Canned Goods can |
| CD `item/cornmeal` | Create `wheat_flour` | ad-hoc recolour | `palette_swap.py` |
| CD `item/wooden_bucket`, `wooden_water_bucket` | a bucket sprite (nearest matches Extra Delight / Everything Copper buckets, about 57 px off-map) | ad-hoc pixel edits (2026-10-30) | owner check: most likely needs own art |
| CD `block/dough_block/cucumber_dough_block` | another dough block (match is a lead only) | ad-hoc recolour (2026-11-04) | `palette_swap.py` from the dough block the family uses |
| `kubejs/assets/vintagedelight` `item/cucumber_dough`, `cucumber_noodles` | Vintage KOTS `cucumber_dough`; Farmer's Delight `raw_pasta` | ad-hoc recolour | `palette_swap.py` |
| Hearth member `block/sunflower_stem` | a stem sprite (match is a lead only) | ad-hoc recolour (2026-11-09) | `palette_swap.py` |
| `kubejs/assets/beautify` `blocks/hanging_pot` | Beautify `hanging_pot` (36 px changed) | colour-map `putpixel` script | `palette_swap.py --drop`, or `combine_tex.py` |
| Brewin' member `gui/keg` | Brewin' and Chewin' 4.5.0 `gui/keg` (98 % unchanged) | ad-hoc pixel edit of the GUI | owner check: hand edit in an image editor |

The full list with the matched source per file: `ledger.tsv` rows with status `redo`.

## 6. Confirm: owner to say

Grouped by family. Recolour = a clean recolour of the named human sprite (only the tool is unknown); edited / unmatched =
pixels no recolour explains, or no source found.

| Family | Count | Pixel test | Question |
|---|---|---|---|
| CE `item/` components, grits, wires, dusts | 39 | 37 recolour (Immersive Engineering, Updated Engineering pack) | made with `palette_swap.py`? |
| CE `block/coil_block` (20), `dust_pile` (16), `plate_block` (11) | 47 | recolours of IE / Updated Engineering sprites | same |
| Beautify pots (`kubejs/assets/beautify/textures/blocks/pots`) | 50 | edited, 36 px each against Beautify's pot | flowers laid on with `combine_tex.py`, or by hand? |
| Creative Delight `item/` (cans, baking sheet, cake pan, teas, syringe) | 53 | 40 recolour, 11 edited, 2 unmatched | per file; `sample_syringe` has a working copy in `textureswapper/tex/` |
| Canned golem coats (`kubejs/assets/creativedelight/textures/entity/canned_golem`) | 15 | recolours of Canned Goods' golem | tool? |
| Creative Equipment shields | 14 | recolours of vanilla shield; `_nopattern` 2 to 21 px edited; felsic 19 to 21 px | hand-edited? |
| Creative Equipment GUI tints (`gui/*_tint.png`) | 12 | unmatched, 27x9 armour-stripe swatches | who drew them? |
| More Mooblooms coats | 11 | unmatched | README says "drawn for this mod": by the owner? `sunbloom` was recoloured from allium by `make_dandelion_texture.py` |
| Lunchbox looks (copper, iron, netherite + outlines) | 5 | recolours of Spice of Life's lunchbox; outlines unmatched | owner's art? (hand-named working file `ntherite_lunchbox_outline.png`) |
| Farmer's Delight cutting boards (stone, wood) | 11 | recolours of FD's board, stone ones edited | tool? |
| Barbeque's Delight trays (9 woods) | 9 | recolours | tool? |
| CD culture blocks (7), dough blocks (5) | 12 | edited / unmatched | tool? (dough blocks 2026-10-25 logs show both the owner's tools and pixel code) |
| `kubejs/assets/vintagedelight/textures/block` oat stages | 10 | recolours; `oat_stage0` unmatched | tool? |
| Title panoramas (`kubejs/assets/minecraft/textures/gui/title/background`) | 6 | unmatched | owner's screenshots? |
| Smaller: Lightman's Currency coins (4), Vintage Improvements springs (4), Creative Brewing potion buckets (6, `potion_bucket_texture.py` layer split of the vanilla water bucket), Hearth salt licks (3, `generate_salt_lick_textures.py` brightness ratio) and tilled end stone (2), Rustic pancakes (6), Creative Magic dark fire (2), Exposure camera bag (2), Solraspberry `liquid_diet_log`, CE `creosote_soaked_sawdust`, `amethyst_lens`, `layered_obsidian`, `felsic_block`, Create crushed aluminium, arrows | about 45 | mixed | per file; the potion bucket split and salt lick ratio are AI-written transforms, so they are `redo` unless the owner counts a layer split as a combine |

## 7. Derived textures: sources and use

Every owned-tool texture's source, tool and use is in `sources.tsv` (families) and `ledger.tsv` (per file, columns
`source`, `tool`, `used_for`). Families recorded so far:

| Family | Source | Tool | Used for |
|---|---|---|---|
| Seasoned overlays (CD `item/seasoned/*`) | The Salt's salted overlay shape; palettes from Brewin' cheese, FD onion, Veggies garlic, vanilla sugar, Ender's void pepper | `palette_swap.py` (`make_seasoning.py`) | overlay drawn over a seasoned dish |
| Pink salt (`kubejs/assets/salt`, Vintage `block/salt`) | The Salt 1.2.4.1 shapes; Vintage Delight pink salt palette | `palette_swap.py` (`make_salt.py`); lamp side = recolour + upstream wood combined | The Salt's blocks, ores, cauldron, lamp, items; Salt Layer |
| Powdered Sugar Block | vanilla moss block; powdered sugar item palette | `palette_swap.py` (`make_psugar.py`) | Powdered Sugar Block |
| Pack jam line (Vintage member jars, jellos, jam / jello blocks, interiors) | Fruits Delight jar, jam, jello textures; one colour sample per jam | `palette_swap.py` (`gen_jam_art.py`) | jam jars, jellos, jam and jello blocks, mason jar fillings |
| Glowing Calamari items (CD) | Rustic calamari, slice, roll; glow ink sac palette; rice layer combined back | `palette_swap.py` (art notice) | Glowing Calamari, Slice, Roll |
| Cardboard Box and Package items | owner's drafts | by hand | Wares items |

## 8. History

- 2026-11-13: texture rule set by the owner; `tools/texture_provenance.py`, ledger, sources table, sheets. Owner
  flagged the echo tools and the calamari crate: echo tools swap, glowing crate redo, plain crate is upstream.
