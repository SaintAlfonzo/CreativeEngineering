# Storage Delight 26.10.05 against the installed 25.12.09

**BUILT 2026-10-25 (decisions in section 5); Shipped in 1.1.03 (deployed to server and players 2026-10-26); SQ-STD-01 PASSED 2026-10-26, SQ-STD-02 PASSED r1102b 2026-11-02. The 2026-10-29 23:54 build (name tag, JEI wood order) is the `mods/` copy (identical, deep scan 2026-11-02).** Replaced: `storagedelight-25.12.09-1.19-forge.jar` (`mods/`, `server_mods/`), 1.19.2. Evaluated
2026-10-25 against `devmods/checkNewerVersionForContent/storagedelight-26.10.05-26.3-neoforge.jar` (NeoForge, vanilla
26.3), method in [devmods_evaluation.md](devmods_evaluation.md) section 1. Both jars unpacked in `DecompiledMods/`. Plan
row: `largecontent/mod-additions #33` in [todo/TODO.md](todo/TODO.md). No config file. The pack's scripts do not touch
the mod (no recipe, tag or name change); it is listed only in [creative_delight.md](creative_delight.md) as a
non-member.

**Licence:** MIT in both jars. Code and art can be reused with the notice kept.

**Result in one line:** 94 new blocks, all the same block types as today on new woods and new countertop stones; no new
block type, no behaviour change. 64 of the 94 could exist in the pack (4 new countertops on the 9 current woods, plus
bamboo and cherry sets); pale oak and poplar have no wood here.

## 1. How it was read

- `python3 tools/jar_diff.py --no-classes` (963 files old, 2022 new; 1334 added, 276 removed, 21 changed; en_us lang
  **+51 / -0 / ~0**). Most of the added / removed count is the 1.21 folder renames (`recipes` -> `recipe`,
  `loot_tables` -> `loot_table`, `advancements` -> `advancement`, `tags/blocks` -> `tags/block`) and the new
  `assets/storagedelight/items/` item model definitions.
- Registered ids from the `BlockRegistry` string constants (90 old, 184 new) and the blockstates (91 / 185; the extra
  one is `oak_pantry_cabinet`, an asset with no block, recipe or name in either jar).
- Recipes and loot tables of the 90 shared blocks compared semantically (1.21 bare-string ingredients and `result.id`
  normalised).
- Block entity classes read with `javap -c -p -constants`: every container opens `ChestMenu.threeRows` (27 slots) in
  both. Fuel from `FuelBlockItem` (old) and `data/neoforge/data_maps/item/furnace_fuels.json` (new).
- Pack side: `kubejs/exported/items_by_mod` for the slabs and trapdoors the recipes need, `item_dict.js`,
  [storage_audit.md](storage_audit.md) (Vinery woods).

## 2. Changes and proposed decisions

### 2.1 New blocks (94)

Block types per wood, unchanged: drawer, drawer with books, drawer with door, small drawers, single door cabinet,
bookshelf with door, cabinet with glass doors, glass cabinet, and the countertop cabinets. New in 26.10.05: a plain
`<wood>_cabinet` (only for pale oak and poplar, which Farmer's Delight has no cabinet for; class `CabinetBlock`).

| Group | Count | Recipe needs | In the pack? |
|---|---|---|---|
| Countertops in Polished Blackstone, Polished Diorite, Stone Bricks on the 9 current woods | 27 | vanilla slab (`AAA` stone slab, `C C` trapdoor, `BBB` wood slab) | yes, all vanilla 1.19.2 |
| Countertop in Polished Tuff on the 9 current woods | 9 | `minecraft:polished_tuff_slab` | no vanilla block; Quark `polished_tuff_slab` exists (twigs has only tuff brick slabs) |
| Bamboo set (every type, 6 countertops) | 14 | `minecraft:bamboo_slab`, `bamboo_trapdoor` | Quark `bamboo_planks_slab` and `bamboo_trapdoor`; art matches (average colour 196,176,82 against Quark planks 193,173,80) |
| Cherry set | 14 | `minecraft:cherry_slab`, `cherry_trapdoor` | Vinery `cherry_slab` and `cherry_trapdoor`, but that is the port's dark cherry ([storage_audit.md](storage_audit.md)); Storage Delight's art is vanilla pink cherry (average 224,174,168), so it would not match |
| Pale oak set (incl. plain cabinet) | 15 | `minecraft:pale_oak_*` | no wood |
| Poplar set (incl. plain cabinet) | 15 | `minecraft:poplar_*` (26.x vanilla wood) | no wood |

The installed 25.12.09 jar already carries the bamboo and cherry textures and the en_us names for bamboo, cherry and pale
oak blocks (43 lang keys with no block), but never registers them. New textures for the countertop stones, pale oak and
poplar are in 26.10.05 only.

**Proposed:**
- Countertops in Polished Blackstone, Polished Diorite, Stone Bricks on the 9 woods (27 blocks): carry over.
- Polished Tuff countertops (9) with the Quark slab: owner's call (Quark's polished tuff colour against 26.10.05's art
  not checked).
- Bamboo set (14) on Quark bamboo: carry over; the art is already in the installed jar.
- Cherry set (14): skip, or retexture onto Vinery's dark cherry (owner's call).
- Pale oak and poplar (30): skip, no wood.

**Delivery:** new blocks with a block entity cannot be added by KubeJS on 1.19.2. Two routes, both Java:
1. An in-tree 1.19.2 rebuild of Storage Delight 25.12.09 (MIT, same mod id `storagedelight`, so no `mod_sides.md` id
   change, only the jar name) with the extra registrations, block entity valid-block lists, recipes, loot, axe tag and
   fuel items. Clean, but makes the pack own one more jar. Recommended.
2. patcherMod mixins into `BlockRegistry` / `ItemRegistry` / `EntityTypesRegistry` to append blocks, plus assets and data
   in the resource pack. Avoids a new jar but patches a third mod's registration order.

### 2.2 Shared blocks (90)

| Change | Class | Proposed |
|---|---|---|
| Cabinet with Glass Doors recipe: `forge:glass_panes` -> `c:glass_panes` | data (1.21 tag) | skip: same behaviour |
| All other 81 recipes, all loot tables (drop self, survives explosion), axe tag, 27 slots, names | none | none |
| Fuel: 300 ticks for every overworld-wood block (nether woods none) | value, unchanged | none |
| Creative tab via NeoForge, item model definitions, `pack.mcmeta`, logo removed | code / assets | skip |

## 3. Open questions for the owner

1. Add the new blocks at all (a rebuilt jar, or patcherMod)? If yes, which groups: the 27 vanilla-stone countertops,
   the 9 tuff ones, bamboo, cherry?
2. Cherry: skip, or retexture to Vinery's dark cherry?

## 4. Tests

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-STD-02.

Prefix `STD-`. Needs `repackedStorage-25.12.09-1.19-forge-ce-19.2-01.jar` in `mods/` (and `server_mods/`); install with
`./gradlew :repackedStorage:installToMods` once no game is running (it moves `storagedelight-25.12.09-1.19-forge.jar`
to `development/repackedStorage/jar_archive/displaced/`).

| ID | Check |
|---|---|
| STD-01 | Boot: no registry, model or recipe error mentioning `storagedelight` in `logs/latest.log`; creative tab holds 140 blocks (90 + 50) |
| STD-02 | Craft one countertop of each new stone (Polished Blackstone, Polished Diorite, Stone Bricks, Polished Tuff) on two woods, one nether wood included: slab row + trapdoor + wood slabs; the Tuff one takes a Quark Polished Tuff Slab |
| STD-03 | Craft all 14 bamboo blocks from Quark Bamboo Planks Slabs and Bamboo Trapdoors (countertops with the matching stone slab; glass doors with a glass pane) |
| STD-04 | Place a new countertop cabinet, a bamboo drawer and a bamboo cabinet with glass doors: each opens as 27 slots, keeps its items, drops them (and itself) when broken with an axe, model changes open / closed, in all four facings |
| STD-05 | Tuff countertops show Quark's polished tuff on top; blackstone, diorite and stone bricks show the vanilla texture; no missing-texture purple on any of the 50 |
| STD-06 | A world saved before the jar: the original 90 blocks and their contents are intact after loading with it |
| STD-07 | Wood, bamboo and Oak blocks burn 300 ticks in a furnace; Crimson / Warped ones do not |

Queue: SQ-STD-01, -02 (passed), SQ-STD-03 (STD-07 fuel) and CQ-STD-01 (STD-04, STD-05), the last two added by the deep scan 2026-11-02.


## 5. Owner decisions and build plan (2026-10-25)

Owner's notes: add the Polished Blackstone, Polished Diorite, Stone Bricks and Polished Tuff countertops on the 9
current woods, and every bamboo variant. Nothing else (no cherry, pale oak, poplar, plain cabinet).

**Result: 50 blocks.** 36 countertop cabinets (4 stones x 9 woods) and 14 bamboo blocks (the 8 types: drawer, drawer
with door, drawer with books, small drawers, single door cabinet, cabinet with glass doors, bookshelf with door, glass
cabinet; plus 6 countertops: deepslate, andesite, blackstone, diorite, stone bricks, tuff). Answers section 3: yes to the
27 vanilla-stone countertops, yes to the 9 tuff ones, yes to bamboo, no to cherry.

**Route:** section 2.1 route 1, an in-tree rebuild with the same mod id `storagedelight`. Project
`development/repackedStorage` (plugin `ce.forge-repack`, upstream 25.12.09 unchanged). The overlay replaces
`BlockRegistry`, `ItemRegistry` and `EntityTypesRegistry` (written fresh in Mojmap, same ids for all 90 existing blocks,
registered from the wood / type tables) and `en_us.json` and the axe tag; it adds the models, blockstates, textures
(from the 26.10.05 jar, MIT, notice kept), recipes and loot tables of the 50 blocks. Not a Creative Delight member.

**Ingredients on 1.19.2:** stone slabs `polished_blackstone_slab`, `polished_diorite_slab`, `stone_brick_slab` (vanilla),
`quark:polished_tuff_slab`; bamboo `quark:bamboo_planks_slab` and `quark:bamboo_trapdoor`; glass panes `#forge:glass_panes`
as in the installed recipes. The tuff top face uses `quark:block/polished_tuff` (vanilla has no polished tuff on 1.19.2).
Properties and fuel (300 ticks, overworld woods) as the existing blocks; bamboo burns like them.

**Tests:** `STD-`, section 4. 2026-10-26 scratch run: SQ-STD-01 PASSED (no `storagedelight` error, 140 `storagedelight:` blocks, 140 recipes); SQ-STD-02 (place and use) PASSED r1102b 2026-11-02 (`perf/runs/r1102b/s3/judged.md`: 3 `use SUCCESS`, each drops itself and its contents). STD-01 to STD-07 in game open.
