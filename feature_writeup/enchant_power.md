# Enchantment power: every block that adds to the enchanting table, audited

## 0. At a glance

| Part | Status |
|---|---|
| Audit of every jar's `getEnchantPowerBonus` override (section 2) | DONE 2026-10-31, read from bytecode of 417 jars |
| Findings F1 to F4 (section 4) | open, need a decision |
| Proposed system (section 5): one block-tag family hooked through patcherMod | proposal, nothing built |

- **Pending:** nothing built. No script or in-tree mod touches enchanting power today.
- **Files to open:** none yet. Easy Magic's config is `config/easymagic-server.toml` (backup in
  `configs_edited/root_config/easymagic-server.toml`).
- **Knobs:** Easy Magic `max_enchanting_power` (15), `lenient_bookshelves` (true). Supplementaries
  `config/supplementaries-common.toml`: `book_power` (1.0), `enchanted_book_power` (1.334), `crystal_enchanting` (3).

## 1. Overview

There is no Apotheosis. Easy Magic (`fuzs.easymagic.world.inventory.ModEnchantmentMenu`) replaces the vanilla
enchanting table menu. It sums Forge's `getEnchantPowerBonus` over the 32 vanilla shelf positions (the 2-high ring
around the table), rounds the float total down, and scales it as `power * 15 / max_enchanting_power`. With the cap at
15 it plays like vanilla.

- Fractions add before rounding: four 0.25 blocks count as one shelf.
- Power is per position, so a small block can match a shelf but never beat it.
- Quark Matrix Enchanting is disabled (`"Matrix Enchanting" = false`), so candle influence does not apply.
- `lenient_bookshelves = true`: blocks without a full collision shape (torches, carpet) do not block shelves behind.
- Every caller goes through `IForgeBlockState.getEnchantPowerBonus` (Easy Magic, Quark, Create's Display Link source),
  so one hook there covers all of them.

## 2. Sources that give power now

Found by scanning every jar for classes declaring `getEnchantPowerBonus`, then reading the value from bytecode.

| Source | Power | Notes |
|---|---|---|
| Vanilla bookshelf | 1 | |
| Quark variant bookshelves (13 woods, incl. ancient, azalea, bamboo, blossom) | 1 | Variant Bookshelves module on |
| Upgrade Aquatic driftwood and river bookshelf | 1 | Blueprint shelf class |
| SecurityCraft reinforced bookshelf | 1 | |
| Dave's Building `book_1`, `book_2` | 1 | recipe: 3 books |
| Beautify `bookstack` | 1 | recipe: 3 books |
| Supplementaries book pile (upright and horizontal) | 0.25 per book, 0.5 per Quark tome, 0.33 per enchanted book (4 books = 1, 4 enchanted = 1.334) | `book_power`, `enchanted_book_power` |
| Supplementaries crystal pedestal (End Crystal on a pedestal) | 3 | `crystal_enchanting` |
| Botania Mana Pylon | 8 | craftable |
| Botania Natura Pylon | 15 | craftable |
| Botania Gaia Pylon | 15 | craftable |
| Mystical Oak Tree Wise Oak | 15 | drops itself with silk touch |
| Create copycats, Copycats+ (incl. multi-part), Supplementaries timber frame | copies the held material's power | a bookshelf-made panel gives 1 |

## 3. Blocks that look like shelves but give 0

| Family | Count | Suggested |
|---|---|---|
| Fantasy Furniture bookshelves (bone skeleton, bone wither, dunmer, necrolord, nordic, royal, venthyr) | 7 | 1 (0.5 each if the footprint fills 2 positions) |
| Storage Delight `*_bookshelf_with_door` (in-tree, `development/repackedStorage`) | 10 | 1, override in its own source |
| Storage Delight `*_drawer_with_books` (in-tree) | 10 | 0.5 |
| Lightman's Currency `bookshelf_trader_*` (vanilla, Quark, BYG, BoP, Tinkers' Construct woods) | about 65 | 1 |
| Dramatic Doors bookshelf doors (tall and short, Macaw's and Many Ideas sets) | 12 | 0.5 per half |
| Fantasy Furniture `book_stack` | 1 | 0.75 |
| Fantasy Furniture `floating_tomes`, `venthyr_tomes` | 2 | 1 |
| Handcrafted `stackable_book` (3 books makes 3) | 1 | 0.25 |

## 4. Findings

- **F1: Wise Oak gives 15.** One silk-touched block fills the whole table. Worst outlier. Proposed 4.
- **F2: Natura and Gaia Pylons give 15, Mana Pylon 8.** One Natura Pylon replaces 15 shelves. Proposed Mana 2,
  Natura 4, Gaia 6.
- **F3: about 100 shelf-like blocks give 0** (section 3), mostly Lightman's Currency trader shelves and Storage Delight.
- **F4: book decorations are balanced.** 3 books for 1 power matches a bookshelf; Supplementaries piles match too. The
  crystal pedestal (3) is fine given an End Crystal's cost. No change.

## 5. Proposed system

1. patcherMod adds a block-tag family, for example `creativeengineering:enchant_power/quarter`, `/half`, `/one`,
   `/four`, filled through `global.tags`.
2. The coremod patches `IForgeBlockState.getEnchantPowerBonus`: return the tag value if the block has one, otherwise
   the block's own value. This serves both new sources and rebalances (Wise Oak, pylons).
3. Storage Delight overrides the method in its own source.
4. New families to consider:
   - Shelves that count their books (Supplementaries item shelf, Handcrafted, Another Furniture and Redeco shelves,
     vanilla lectern: held book 0.5, enchanted book 1). Needs per-mod code reading stored contents; the largest part.
   - Arcane crystals at low values: amethyst cluster 0.25, Geode echo crystal 0.5, Geode diamond crystal 0.5, lapis
     block 0.25.
5. Testing: Create's Display Link can show a table's power.

Open decisions: which families to take, and the pylon and Wise Oak values.

## 6. In-game test suite (proposed, `EP-`)

- [ ] EP-01: ring of 15 vanilla shelves gives the level 30 offer; 14 gives lower.
- [ ] EP-02: Wise Oak and each pylon give the agreed value (Display Link readout).
- [ ] EP-03: each newly tagged family gives its value; fractions add (four quarter blocks = 1).
- [ ] EP-04: a copycat panel of a tagged block copies its power.

## 7. History

- 2026-10-31: audit written. Nothing built.
- 2026-11-04: the enchanting rows now live in Creative Magic ([creative_magic.md](creative_magic.md)); this plan's tags and values go there when built.
