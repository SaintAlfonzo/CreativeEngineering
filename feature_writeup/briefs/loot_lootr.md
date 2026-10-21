# Brief: loot / lootr

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# loot`, Sub `lootr`. Reference writeups:
[loot.md](../loot.md) 3.6 and 7, [loot_catalog/handplaced.md](../loot_catalog/handplaced.md) (parts A and C),
[loot_catalog/by_structure.md](../loot_catalog/by_structure.md), [structure_tuning.md](../structure_tuning.md) 3.6
(container swaps). Code / data: [config/lootr-common.toml](../../config/lootr-common.toml),
[loot_container_swaps.json](../../kubejs/generation_scripts/worldgen/loot_container_swaps.json),
[loot_container_audit.md](../../kubejs/generation_scripts/worldgen/loot_container_audit.md),
patcherMod `worldgen/LootContainerSwaps.java` + `mixin/StructureTemplateLootSwapMixin.java`,
[tools/lootr_fixed_containers.py](../../tools/lootr_fixed_containers.py), [tools/port_structure_jars.py](../../tools/port_structure_jars.py),
[config/quark-common.toml](../../config/quark-common.toml) `[building.variant_chests]`.

## Scope and order

Open rows: #1, #2 (both Planned). No Testing / Complete rows in this sub.

**State on 2026-10-21 that changes the rows** (verified against the jars where marked):

- **Lootr 0.4.29.76 has no item frames** (verified: no frame entity or class in the jar; only chests, trapped
  chests, barrels, shulkers, the inventory block and the chest minecart). `convert_elytras = true` only turns the End
  ship's elytra frame into a chest. "Item frame integration" is therefore new patcherMod code, not a config switch.
- **The resource pack already skins most Lootr textures** (verified, `LootrChestRetexture_v1.1_1.19.zip`, load order 22):
  `chest`, `chest_opened`, `shulker`, `shulker_opened` and the barrel set. It does **not** cover the trapped chest
  (`lootr:chest_trapped`, `chest_trapped_opened`, which `LootrChestBlockRenderer` uses for every Lootr trapped chest),
  and its `block/opened_barrel_top_opent.png` is misspelled, so the opened barrel's open-lid top
  (`opened_barrel_top_open`) stays Lootr's default. Shulkers do **not** need new art.
- **Quark's Lootr chests are Quark's own blocks** (verified, Quark 3.4-418): `quark:lootr_<type>_chest` and
  `quark:lootr_<type>_trapped_chest` for 16 types (oak, spruce, birch, jungle, acacia, dark_oak, mangrove, crimson,
  warped, azalea, blossom, ancient, bamboo, prismarine, purpur, nether_brick). Quark's `ConfigManagerMixin` rewrites
  Lootr's `addSafeReplacement` so a Quark chest converts to its Lootr variant, and Quark's "Replace Worldgen Chests"
  (on) first swaps vanilla chests in every structure in `#quark:<type>_chest_structures` (villages, igloos ...) for
  Quark chests. Those render with `quark:model/chest/<type>/lootr_normal|lootr_opened|lootr_trap|lootr_trap_opened`
  (`LootrVariantChestRenderer`), so the resource pack never reaches them.
- **The `eugene` port still has 6 hand-filled containers**: `python3 tools/lootr_fixed_containers.py --dry-run`
  (run 2026-10-21) -> "would convert 6 containers in 3 templates -> 5 loot tables". The tool wasn't re-run after the
  camps_castles_carriages port.

**Build groups and order:**

1. **#1 step 1** (eugene, one tool run) any time; then `loot/lootjs-layer #3` regenerates the catalog.
2. **#2a textures** (Quark override + trapped chest + barrel typo): client-only, small; do with the next patcherMod
   or kubejs/assets change.
3. **#1 steps 2-4** (jar templates with fixed contents, the verse chest): after `loot/lootjs-layer #3` (fresh audit),
   alongside `loot/tables #1 / #2` (which may replace some fixed chests with themed tables instead).
4. **#2b item frames:** last; its own patcherMod feature with a decision first.

---

## lootr #1: Missed Lootr chests and fixed-loot chests
Status Planned / Work data / `loot/lootr #1`

### Goal
Every loot-like container a structure places is per-player: containers with a loot table that Lootr skips, and
hand-filled chests / barrels (fixed contents, no table, so shared). The verse chest keeps its fixed contents but
becomes per-player.

### Now
- Lootr converts natively: chest, trapped chest, barrel, shulker boxes, Quark chests (`convert_quark`), the
  `forge:chests/wooden` and `forge:chests/trapped` tags. Swaps (patcherMod, `processBlockInfos` RETURN) turn IE
  crates, FD cabinets / baskets into barrels and PNC reinforced chests into chests.
- [loot_container_audit.md](../../kubejs/generation_scripts/worldgen/loot_container_audit.md) (2026-10-04) "NOT
  converted" with a table: 12 `minecraft:dispenser` (Unwrecked Ships, `jungle_temple_dispenser` arrows and
  `unwrecked_ships:dispensers/fire_dispenser`), 13 `minecraft:dropper` (Nullscape phantom hives,
  `nullscape:phantom_nest`), 10 `monobank:monobank` (village vaults). Stale for the 1.0.6 structure mods.
- [handplaced.md](../loot_catalog/handplaced.md) part A (2026-10-14): 163 chest / barrel / trapped chest containers
  with fixed contents, in jar templates (104 templates, 3 in the disabled Farmer's Structures aether set) plus the 6
  in `eugene`. Biggest sources: Philip's Ruins 61, Towns & Towers 38, Farmer's Structures 17, Old Swamp Fisherman
  House 10, Sawmill House 9, Terralith 6, vanilla 5 (mansion rooms, an ancient city chest with a golden apple). Part C:
  151 already converted to `<ns>:chests/fixed/<hash>` for the ported `kubejs/data` structure mods.
- Verse chest: `philipsruins:ancient_tower_8`, chest at (6, 2, 5): `minecraft:writable_book` with page
  `"        \n        John 8:32"` + `minecraft:golden_carrot` (structure `philipsruins:ancient_towers`, freq 0.863).
  The row says "written book"; the template holds a book and quill.
- `lootr_fixed_containers.py` only walks `kubejs/data/*/structures/**.nbt`; jar templates are out of its reach.

### Build
1. **eugene:** `python3 tools/lootr_fixed_containers.py` -> 6 containers in `eugene:plains_undead_castle`,
   `eugene:red_roof`, `eugene:ruined_drum_tower` get `eugene:chests/fixed/<hash>` tables. Review the hauls first
   (D2): the drum tower has a chest of 54 fireworks, one of 27 books and two of 27 gold ingots each; per player that
   is 54 gold ingots a visit.
2. **Jar templates, datapack override (recommended, D1):** add a `--from-jars <mods dir>` mode to
   `lootr_fixed_containers.py`: for each jar template with a chest / trapped chest / barrel that has `Items` and no
   `LootTable` (reuse `loot_breakdown_scan.py`'s template walk), copy the `.nbt` to
   `kubejs/data/<ns>/structures/<path>.nbt` (same path = datapack override of the jar's template), then run the
   existing conversion on it. Skip templates of structures disabled in `structure_groups.json` (handplaced marks them
   **DISABLED**). Record the copied templates in a manifest (`tools/.cache/` or a JSON next to the swaps) so a mod
   update can re-copy them.
3. **Verse chest:** comes out of step 2 as a `philipsruins:chests/fixed/<hash>` table. If D3 says written book, edit
   that table's `set_nbt` to `{title:"John 8:32",author:"...",pages:['"..."'],resolved:1b}` on
   `minecraft:written_book` (and keep the golden carrot).
4. **Containers with tables Lootr skips (D4):** dispensers stay (traps, not loot). Nullscape droppers: optional swap
   `"minecraft:dropper": "minecraft:barrel"` in `loot_container_swaps.json` would also hit any other dropper placed
   with a table (only these 13 today); it changes their look. Monobank: keep (its lock mechanic is the point).
5. **Placed-loot pass:** after `loot/lootjs-layer #3`, re-run `python3 tools/audit_loot_containers.py` and walk the
   new structure mods' rows in `by_structure.md` (Subterrestrial, Underground Bunkers, Campsite Structures, Volcanic
   Caverns) for unconverted blocks; add swaps as needed.
6. Regenerate the catalog (`loot_breakdown.py`) so part A shrinks and part C grows; update loot.md 3.6.

### Ids and data
- Tables written: `<ns>:chests/fixed/<sha1[:10]>` of the table JSON (identical contents share a table), type
  `minecraft:chest`, one pool per stack (`set_count`, `set_nbt`).
- Override path rule: `kubejs/data/<ns>/structures/<path>.nbt` overrides `data/<ns>/structures/<path>.nbt` of the
  jar (vanilla included: `minecraft:woodland_mansion/1x1_b5` etc.).

### Decisions
- **D1 override templates vs runtime conversion in `LootContainerSwaps`:** runtime would need a content key computed
  identically in Java and Python (no template id reaches `processBlockInfos`). Recommend the datapack override: data
  only, visible to every offline tool, same path as the 151 already converted.
- **D2 big fixed hauls:** per-player copies multiply them. Recommend converting all, then trimming outliers (stacks
  of ingots, gems, 27-slot fills) or pointing them at a themed table from `loot/tables #1`.
- **D3 verse item:** keep the writable book (as built) or a signed written book. Recommend a written book titled
  "John 8:32" (the row's wording; a book and quill reads as unfinished).
- **D4 droppers / dispensers / monobank:** recommend leave all three.

### Gotchas
- Overriding a jar template means a mod update won't reach it; the manifest in step 2 is the re-copy list.
- Towns & Towers and Terralith templates also go through the pack's ground-matching and earth-fill hooks
  (structure_tuning.md); an override is fine for those (they act on placement), but re-test one village.
- Only newly generated chunks change; existing Lootr containers keep their table.
- The scan sees `Items` in templates only; containers filled by processors or code (YUNG's) are already tables.

### Tests
- **LT-32 eugene fixed:** find a `eugene:ruined_drum_tower` in a new world, open the gold chest with two players ->
  each gets the contents; Lootr's chest texture shows.
- **LT-33 verse chest:** `/locate structure philipsruins:ancient_towers`, find the tower variant with the chest at
  (6, 2, 5) -> Lootr chest, the verse book + golden carrot, per player.
- **LT-34 audit clean:** `python3 tools/audit_loot_containers.py` -> no "NOT converted" rows except those D4 keeps.

---

## lootr #2: Lootr item frames and textures
Status Planned / Work resource pack / patcherMod / `loot/lootr #2`

### Goal
Every Lootr container shows the resource pack's Create-style skin (including Quark's variants and trapped chests),
and (separately) item frames that structures place with an item give each player their own copy.

### Now
- Resource pack `LootrChestRetexture_v1.1_1.19.zip` (enabled, order 22), files verified (sha1 89baa371...):
  `assets/lootr/textures/{chest,chest_opened,chest_old,chest_partial,shulker,shulker_opened}.png` and
  `textures/block/{barrel_bottom,barrel_side,barrel_top,barrel_top_open,opened_barrel_bottom,opened_barrel_side,opened_barrel_top,opened_barrel_top_opent}.png`.
  `chest_old` / `chest_partial` match nothing Lootr 0.4.29 loads (it reads `old_chest`, `old_chest_opened`).
- Lootr 0.4.29 sprites (verified, `LootrChestBlockRenderer` / `LootrShulkerBlockRenderer`): `lootr:chest`,
  `chest_opened`, `chest_trapped`, `chest_trapped_opened`, `old_*`, `shulker`, `shulker_opened` on the chest and
  shulker sheets. `lootr-client.toml`: `vanilla_textures = false`, `old_textures = false`.
- Quark (verified): `LootrVariantChestRenderer.getMaterial` returns `quark:model/chest/<type>/lootr_normal` /
  `lootr_opened` (trapped: `lootr_trap` / `lootr_trap_opened`) per block; with Lootr's `vanilla_textures` it returns
  the plain Quark chest. `quark-common.toml` lines 218-227: `"Replace Worldgen Chests" = true`.
- Item frames: none in Lootr 0.4.29. Structure item frames in this pack come from templates (entities) and YUNG's
  processors (`config/better*/forge-1_19/itemframes.json`, chance per item, loot.md 2.9).

### Build
**2a, textures (client side; kubejs/assets is shipped to clients):**
1. Quark variants, pick D1. Recommended: a patcherMod client mixin, no copied art:
   ```java
   @Pseudo @Mixin(targets = "vazkii.quark.integration.lootr.client.LootrVariantChestRenderer", remap = false)
   abstract class QuarkLootrChestSkinMixin {
     @Inject(method = "getMaterial", at = @At("RETURN"), cancellable = true)
     private void ce$lootrSkin(@Coerce Object tile, ChestType type, CallbackInfoReturnable<Material> cir) {
       // tile is LootrVariantChestBlockEntity: opened? -> MATERIAL2 / MATERIAL4, else MATERIAL / MATERIAL3,
       // trapped = tile's block is LootrVariantTrappedChestBlock; skip when ConfigManager.isVanillaTextures()
       // use noobanidus.mods.lootr.client.block.LootrChestBlockRenderer.MATERIAL..MATERIAL4
     }
   }
   ```
   Simpler: compare the returned Material's texture path (`.../lootr_opened` or `lootr_trap_opened` -> opened) and
   map to Lootr's four materials, so the mixin needs no Quark types. Gate it behind a CEConfig client key
   (`[lootr] quarkChestsUseLootrSkin = true`). Needs Quark and Lootr as compile-only deps (`libs/`), `@Pseudo` so it
   is skipped without Quark.
2. Trapped chest: draw `chest_trapped.png` / `chest_trapped_opened.png` (64x64, vanilla chest UV layout) from the
   pack's `chest.png` / `chest_opened.png` with a trap mark, and put them in
   `kubejs/assets/lootr/textures/` (or a small pack-own resource pack). The art is the user's (D2).
3. Barrel typo: copy the pack's `opened_barrel_top_opent.png` to
   `kubejs/assets/lootr/textures/block/opened_barrel_top_open.png`.
4. Check the Lootr chest minecart and the held item (`LootrChestItemRenderer`) pick up the same sprites (unverified;
   look in game).

**2b, item frames (patcherMod, after D3):**
1. Mark structure frames: mixin into `StructureTemplate` entity placement (Mojang `placeEntities`; take the SRG name
   from the mapped jar, the pack's mixins use SRG names with `remap = false`) and tag every `ItemFrame` / glow frame
   that is created with an item: `persistentData.putBoolean("creativeengineering:loot_frame", true)` and an empty
   `looters` UUID list. YUNG's processors edit the entity NBT before that call, so their frames are covered.
2. Per-player take: mixin `ItemFrame.hurt` (Mojang name; SRG `m_6469_` is `Entity.hurt`) at HEAD: if tagged and the
   attacker is a player not in `looters`, give that player a copy of the item, add the UUID, cancel (frame keeps its
   item); a player already listed gets nothing (or the frame breaks normally for creative). Rotating (`interact`)
   stays as is.
3. Optional: client hint, e.g. a WTHIT line "Loot frame: take once" via the pack's WTHIT framework (wthit_lines.md).

### Ids and data
- Texture targets: `lootr:chest_trapped`, `lootr:chest_trapped_opened`, `lootr:block/opened_barrel_top_open`;
  Quark sprites `quark:model/chest/<type>/lootr_{normal,opened,trap,trap_opened}` for the 16 types listed above
  (the jar also ships a `cherry` folder with no block).
- Structure tags that make Quark swap chests: `quark:<type>_chest_structures` (16 files in the jar; the pack adds none).

### Decisions
- **D1 Quark chests:** (a) patcherMod mixin, as above (recommended: no copied third-party art, follows Lootr's own
  texture switches); (b) copy the pack's two chest PNGs over the 64 Quark `lootr_*` sprites in `kubejs/assets/quark/`
  (no code, but redistributes the pack's art 32 times and ignores `vanilla_textures`); (c) `"Replace Worldgen Chests"
  = false` in quark-common.toml (structures then place vanilla chests, which Lootr skins via the pack; loses Quark's
  wood-matched chests after looting; Quark chests placed directly by templates still convert to Quark variants).
- **D2 trapped-chest art:** new texture (recommended) or point Lootr's trapped sprite at the plain chest (no visual
  warning that it is trapped; not recommended).
- **D3 item frames:** worth a patcherMod feature? Recommend yes but small: only frames placed by structure templates,
  take-once per player, no GUI. Confirm with the user which frames matter (YUNG's stronghold / fortress / desert
  temple frames are the main source).

### Gotchas
- Chest sprites live on the chest sheet (`Sheets.CHEST_SHEET`); a texture in `kubejs/assets` must keep the 64x64
  chest layout. Quark's sprites are added in its `TextureStitchEvent.Pre`; Lootr's are referenced by Material and
  stitched by the vanilla chest sheet loader.
- Resource-pack priority: file packs (the Lootr retexture, order 22) sit above KubeJS's assets; a file named the same
  in both comes from the file pack. The new trapped / barrel files exist only in kubejs/assets, so no clash.
- Item frames are entities: a frame broken by an explosion or arrow would drop its item for everyone; the mixin
  must also cover `hurt` from non-player sources (cancel for tagged frames).

### Tests
- **LT-35 Quark skin:** in a new world, a taiga or snowy village chest (Quark spruce chest) -> unopened and opened
  look match a plain Lootr chest with the pack's skin; same for a trapped one.
- **LT-36 trapped skin:** `/setblock ~ ~ ~ lootr:lootr_trapped_chest` (or a YUNG's desert temple trap chest) -> new
  trapped texture, opened variant after opening.
- **LT-37 barrel lid:** open a Lootr barrel facing up -> opened-lid top uses the pack's art.
- **LT-38 loot frame:** two players punch the same YUNG's stronghold item frame -> each gets the item once, the frame
  keeps it; a second punch gives nothing.

---

## Writeup corrections made

- [loot.md](../loot.md) 7: the verse chest is `philipsruins:ancient_tower_8` and holds a writable book; handplaced
  inventory regenerated 2026-10-14 (was dated 2026-10-03).
