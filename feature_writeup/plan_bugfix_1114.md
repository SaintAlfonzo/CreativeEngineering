# Bugfix plan from the 2026-11-11 to 2026-11-14 scratch runs (r1111, r1112, r1113, r1114)

Owner brief 2026-11-14 applied to the unfixed FAILs and findings of
[scratch_run_2026-11-11.md](testing/scratch_run_2026-11-11.md), [-12](testing/scratch_run_2026-11-12.md),
[-13](testing/scratch_run_2026-11-13.md) and [-14](testing/scratch_run_2026-11-14.md) (section 8 of each).
Status: BUILT 2026-11-14, untested in game (per-section status notes below; queue rows written, none run). Power Grid is a
gate jar only (`jar_archive/pwg_gate/`, -18). Code facts below were read 2026-11-14 (paths and lines may drift).

Short names: R = instance root; HH = `development/creativeDelight/members/hearth/src/main/java/com/SaintAlfonzo/creativedelight/hearth`;
KJ = `kubejs/data/creativedelight/kubejs` (copied into the CD jar); CC = `development/creativeCompat/src/main`;
PG = `development/backportedPowerGrid`; dict = `development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/`.

Rebuild map: A = instance `kubejs/data` / config only (`/reload` or restart); B = Creative Delight (`:creativeDelight:`);
C = Creative Common (dicts, helpers); D = Creative Compat; E = Creative Equipment; F = Power Grid port; G = Creative Brewing;
W = Creative Worldgen (`:creativeWorldgen:`).
One jar at a time, never while the game runs. Every item gets its changelog bullet, writeup line and queue rows when built
(CLAUDE.md "Done means"); the row ids below are the existing ones to rerun plus new ones to write.

## 0. Decisions taken in this plan (owner can override)

<!-- generated:status_index begin open_work sha1=3048ffdcb37b -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 6 open TODO rows (Testing 6), 1 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creativecommon/publish #4` | Testing | S | SQ-AZW-01: `[item_dict] Missing` for the block-only Sunflower Stem |
| `creativecommon/publish #5` | Testing | S | SQ-DCM-02: `global.has` read five data namespaces as absent mods |
| `dishes/ingredients #43` | Testing | S+ C | Vegetarian patty renamed Veggie patty; cave carrot makes patties |
| `largecontent/mod-additions #72b` | Testing | S | Syrup to sugar: heated Create mixing recipe with a better yield |
| `largecontent/mod-additions #81` | Testing | S+ C P | Debugify has no config in the instance: every fix runs and MC-121706 crashes a server boot |
| `locate #6` | Testing | S+ P T | Corn maze cannot be located within the 120 s search deadline |
<!-- generated:status_index end open_work -->

Defaults chosen from the brief; each is marked **[default]** where it is used.
1. Seed pouch pattern: fixed 5x5, up to 25 seeds (owner, 2026-11-14, was 3x3); matches the pack's Quark `Shift Range = 5`
   player pattern. (1.1)
2. "The pouch" = `minecraft:bundle` (Quark bundle recipe on). No other seed container in the pack. (1.1)
3. Portal exit lift: one geometry test for every Immersive Portals portal; the saved per-portal flag is ignored and removed
   (portals saved since 2026-11-07 hold wrong values). (4.1)
4. Corn maze 2x (owner, 2026-11-14, was 4x): raise its weight in the `remote` structure group so its share is 2x today's
   (about 845 of the group's total, see 6.3), keeping group spacing. (6.3)
5. Salt lick stages: raw rock salt texture (`salt:block/raw_rock_salt_block`) on every stage; heights 12 / 8 / 4 px for
   lightly / well / heavily licked; waxed stages the same heights. (3.7)
6. Melting (owner, 2026-11-14): a new system melts ice and snow from three source kinds (salt, fire, lava). Packed and blue
   ice step down one tier at a low chance. Defaults inside it:
   - **[default]** Distance is a sphere, measured through any blocks.
   - Torches, lanterns and glowstone count as fire (owner, 2026-11-14); the modded ones are listed in 3.8. Non-flame light
     sources (sea lanterns, shroomlights, redstone torches and lamps, jelly torches, electric lanterns) no longer melt
     anything; a config switch restores vanilla light melting.
   (3.8)
7. Hops textures (owner, 2026-11-14): the white jungle grape art, used in place from the Vinery donor jar. It is not
   copied: Vinery's assets are All Rights Reserved and redistribution was refused. Hops grow on Vinery's
   `grapevine_lattice` like jungle grapes; the Hearth and Harvest lattice is removed. (3.1)
8. Bitten pies: Supplementaries has no pie variant (only `directional_cake`, `double_cake`, `pancake`); pies already count
   through `FdPies`. Only cakes need the fix. (4.3)
9. Block-form food families (SQ-LGM-13): include whole-block ids in the JSON, and check every tag `seasoning_tags.js` derives. (7.4)
10. Power Grid acid vs salt mixing clash (`#82`, owner 2026-11-14): the acid recipe also takes rotten flesh. (8.3)
11. Hops on the lattice (owner, 2026-11-14): Vinery's `grapevine_lattice` takes hops through a hops `GrapeType` in Creative
    Brewing. Hops have no juice, so the stomping pot and JEI skip them. **[default]** Foxes do not eat hops off a lattice. (3.1)

## 1. Seed pouch and bundle in dispensers (B; TODO `largecontent/mod-additions #65`)
Status: DONE 2026-11-14 (cd-a). Quark pickup check: no double count (Quark fills only a held pouch; both shrink the same ground stack).
2026-11-15 (fix-2, r1115): SQ-SPD-08 FAILed in the Nether (the cached fake player is the overworld one and `UseOnContext` took its level); the context now gets the dispenser's level. Rerun SQ-SPD-08b.

### 1.1 Behavior
- Crash cause: HH `gear/QuarkCompat.java:40-47`. The anonymous `DefaultDispenseItemBehavior.execute` calls `dispense(source, pouch)`,
  which binds to the inherited instance method, not the static helper at :87, so it recurses forever.
- Replace with one class, HH `gear/SeedContainerDispense.java`. Register it from `HearthAndHarvestMod.java:68-73` in common setup
  `enqueueWork`, after Quark's `DispensersPlaceBlocks` wrap. Neither item is a BlockItem, so Quark does not wrap them, but
  register late anyway. Base it on `OptionalDispenseItemBehavior`, copying the pattern in HH `machine/CrateBlockItem.java:43`.
  Its `execute` always returns the container stack unchanged in the slot. It never ejects it, success or not, and failure
  only plays the fail click.
- **Planting one seed** (shared by pouch and bundle):
  - Use the seed item's own `useOn` with a use context on the soil block and face UP, the same way Quark's
    `SeedPouchItem.placeSeed` uses `PouchItemUseContext`. Each seed then keeps its own placement rules: farmland crops,
    nether wart, berry bushes, End crops.
  - Accept only stacks in `quark:seed_pouch_holdable` (pouch) or items whose `useOn` succeeds (bundle).
  - Use a fake player (Forge `FakePlayerFactory`) for the context.
- **Quark seed pouch** (`quark:seed_pouch`; NBT `storedItem`, `itemCount`; API `SeedPouchItem.getContents` / `setCount`):
  - The target is the 5x5 centered on the block in front (`front = pos.relative(facing)`):
    - Front block is air (or replaceable): the soil layer is the 5x5 at `front.below()`, and seeds go on top of it.
    - Front block is soil (dirt, farmland, or anything a seed's `useOn` accepts): the soil layer is the 5x5 at `front`,
      and seeds go on the blocks above.
    - Facing up or down: the same two rules on the vertical line. Facing down onto air, the 5x5 is the layer below that
      air. The center column under the dispenser is skipped if blocked.
  - Plant up to 25 seeds, never more than the pouch holds. Decrement `itemCount` once per success. When the count reaches
    0, clear the contents but keep the empty pouch.
  - Return success if at least one planted.
  - The old `canSurvive` + `setBlock` path (QuarkCompat :87-120) goes; the seed's `useOn` replaces it.
- **Bundle** (`minecraft:bundle`, vanilla `Items` list):
  - Take the first entry (vanilla's remove-one order). If it is a seed, plant ONE seed on the single front target (the same
    air/soil rule, center only).
  - Decrement that entry's count, or remove it at 0. Rewrite the vanilla `Items` NBT and the bundle weight stays correct.
  - If the first entry is not a seed, or nothing plants, nothing happens and the bundle stays.
  - Register this regardless of Quark. The vanilla item is always registered. Only its recipe comes from Quark.
- Loose seeds already plant in front through Quark "Dispensers Place Blocks" (BlockItem path). No change.
- `QuarkCompat.init()` keeps its other listeners (Farmer's Hat XP, `onPickup` top-up). Check that `onPickup` does not
  double-count with Quark's own `SeedPouchModule.onItemPickup`; that is a code read only.

### 1.2 Tooltips
- Seed pouch: in `kubejs/assets/creativedelight/kubejs/hearthandharvest/client.js:9-10`, replace "Plants in a dispenser" with:
  - "In a dispenser: plants up to 25 seeds in a 5x5"
  - "Facing air: plants on the blocks below; facing soil: plants on top"
  - "Stays in the dispenser"
- Bundle: one line in the same file, gated on nothing extra: "In a dispenser: plants its first seed; stays in the dispenser".
- Record both in [tooltips.md](tooltips.md).

### 1.3 Verify
- Rerun SQ-HH-1574 and -1575 (crash rows).
- New rows SQ-SPD-01 to -08:
  - pouch facing air over farmland, a 5x5 planted, count -25;
  - pouch facing farmland, the same;
  - pouch with 4 seeds: 4 planted, pouch empty and kept;
  - pouch on stone: nothing, pouch kept;
  - empty pouch: nothing;
  - bundle with wheat seeds plants 1, count -1, NBT intact;
  - bundle with a non-seed first: nothing;
  - nether wart in a pouch on soul sand.
- Client row CQ-SPD-01 for the tooltips.
- Changelog: Seed pouches and bundles family.

## 2. Debugify config (A; TODO `largecontent/mod-additions #81`, `#80`)
- **Status 2026-11-14 (misc): BUILT, untested.** `config/debugify.json` written by the tool (9 fixes on: MC-84873, -200418, -214147, -232869, -223153, -121903, -72151, -206922, -237493; `default_disabled`, `opt_out_updater`), copied to `configs_edited/root_config/`; `mod_sides.md` row `both` (count line recounted); thirdparty_fixes.md status; changelog family Vanilla Bug Fixes. Queue: SQ-TPF-00 and -01 rewritten for the shipped file.

- **No game launch needed.** `config/debugify.json` can be hand-written:
  - Debugify reads it at mixin preload. Missing keys follow `default_disabled`.
  - The format is one `"MC-nnnn": bool` per fix plus `default_disabled`, `opt_out_updater`, `gameplay_fixes_in_multiplayer`.
  - The source is `tools/scratch_query/tpf_debugify_cfg.py`. Mind the jar's typo key `MC-124117` for MC-124177.
- Write it with that tool:
  - `default_disabled: true`, `opt_out_updater: true`.
  - Turn on the `@s1` set minus the ones r1114 failed to reproduce: MC-84873, MC-200418, MC-214147, MC-232869, MC-223153,
    MC-121903, MC-72151, and MC-206922. MC-206922 helped in 3 of 4 runs; it is partial and stays on, with
    [thirdparty_fixes.md](thirdparty_fixes.md) noting the tickCount <= 8 limit.
  - MC-2025 and MC-224729 stay off. MC-121706 is off: it crashes the boot.
  - Also MC-237493 forced on, as the 2026-11-13 scan decided.
- Copy it to `configs_edited/root_config/debugify.json`. Add the `mod_sides.md` row (P7, skill `add-mod`).
- Debugify may rewrite the file at load. The first boot that carries it must diff the file afterwards, and the
  configs_edited copy then follows the rewritten form if only the formatting changed.
- Verify: SQ-TPF-00 (boot, "Enabled N/34" with the expected N), SQ-TPF-01 rewritten (shipped config, no MC-121706 crash),
  SQ-TPF-11 to -17 rerun on the shipped file. The client rows CQ-TPF-31 to -91 stay with the client phase.
- Changelog: one bullet under a "Vanilla bug fixes" family once the file ships.

## 3. Hearth and Harvest (B unless noted)

### 3.1 Hops become a vine, only with Creative Brewing (TODO `#70`, `#71`, `#75`, `#73` item 2)

Status: BUILT 2026-11-14 (cd-b), CD and Brewing installed, not run (tests SQ-HOP-01 to -11, CQ-HOP-01, SQ-HH-304
rewritten; [hearth_and_harvest.md](hearth_and_harvest.md) 4.8, [vinery.md](vinery.md) 3). Deviations: (a) the vine uses
Vinery 1.5.5's `jungle_grapevine_stage0/1/2/stage3_white` models (what the port's jungle vines show), not the 1.3.10
`jungle_grape_vine_*` ones; (b) the wild rule keeps `requires: hearthandharvest` and the feature names the block by id and
places nothing without it (a vinery-gated pack would fail to load with the Hearth and Harvest member switched off);
(c) the H&H "lattice" was the single block `hearthandharvest:hops` (there was no empty H&H lattice); swap in
`data/creativedelight/swaps/hearth_removals.json`, the grape restored by `LegacyLatticeStemBlock.restoreHops`; an age-0
H&H hop loads as an empty lattice; (d) no juice is `Items.AIR` (Vinery's default), not null; (e) the lattice tooltip line
is in the pack's Vinery `tooltips.js`, not the port's Java tooltip; (f) dict key `i.hops_crop` (`hearthandharvest:hops`, the
removed block) is now unused and stale: Common can delete it. Build checks (10.2): the lattice blockstate is a variants file
on age, facing, grape with every grape at every age, so the alias maps hops to the same state with jungle_white (all Vinery
blocks with a grape property); other `getBottle()` callers: the pot (guarded), Grape Stomping JEI (already skipped empty
juice), `/vinery dump grapes` (lists air), no other; foxes eat only from bushes (no change).
- Gate:
  - Java registration of the hops block, `wild_hops` and the hops items (`HHBlocks.java:53-55,91`;
    `HHItems.java:75,80,127,147`) runs only when `ModList.get().isLoaded("vinery")`, following the pattern in
    `members/barbeques/.../PackRawSkewers.java:42-45`.
  - The hops listener `HearthAndHarvestMod.java:89`, the HorseFeed and FarmersHat hops uses (`gear/HorseFeedEvents.java:22`,
    `gear/FarmersHatEvents.java:26`) and the scripts (`KJ/hearthandharvest/recipes.js:71,79`, `tags.js:34,61`,
    `trades.js:12,28`) all get `// requires: vinery` or a split file.
  - Dict keys `item_dict.js:132,138,153,198,216,250` stay. Dicts tolerate absent items; confirm with the dict check (C).
- Vine block: add `HopsVineBlock`, a copy of Vinery's `GrapeVineBlock` behavior (the lattice form follows below)
  (`development/creativeBrewing/members/vinery/src/main/java/net/satisfy/vinery/core/block/GrapeVineBlock.java`):
  - It extends `VineBlock` and clings to a wall face. There is no lattice and no farmland.
  - `AGE_3` and `sterilized` (shears toggle). It grows 1 in 5 per random tick at light 9 or more, via `onCropsGrowPre`.
  - Harvest at age 2 or more drops 0-1 hops (+1 at age 3) and resets to age 1. Bone meal adds +1 age.
  - The planting item is an `ItemNameBlockItem`, like `GrapeBushSeedItem`.
  - Do not extend the Vinery class. CD must not need Brewing classes at load, so copy the logic.
- Lattice: Vinery's `grapevine_lattice` (owner, 2026-11-14; G Creative Brewing, vinery member;
  V = `development/creativeBrewing/members/vinery/src/main/java/net/satisfy/vinery`):
  - How jungle grapes use it: they are a lattice `GrapeType` (`V/core/registry/GrapeTypeRegistry.java:21-22`,
    `lattice = true`). Their seeds plant on `vinery:grapevine_lattice`
    (`V/legacy/block/LegacyLatticeStemBlock.java`, registered at `V/core/registry/ObjectRegistry.java:281`):
    - The lattice mounts on a wall (`canSurvive` needs a full face behind it, :140-162) and needs no farmland.
    - Seeds plant at age 0 (:92-99). It grows at light 9 or more on a random tick, to age 4 (:115-123).
    - Use at age 3 or more harvests and resets to age 1 (:101-106). Shears drop the crop and seeds and reset to 0
      (:81-90). Drops come from `StemBlock.dropGrapes` / `dropGrapeSeeds` (`getFruit()` / `getSeeds()`).
  - Add `GrapeTypeRegistry.HOPS = registerGrapeType("hops", true, false)` statically, so the `GRAPE` block-state
    property (`StemBlock.java:36`) holds it from class load.
    - `setItems` takes suppliers that look up `hearthandharvest:hops` by id (`ForgeRegistries.ITEMS`), as fruit and seeds
      (the H&H hops item is both). There is no Creative Delight class import.
    - With Hearth and Harvest absent, the suppliers return null and the type is inert.
  - The lattice accepts hops: `LegacyLatticeStemBlock.use` (:92) checks `instanceof GrapeBushSeedItem`, which the plain
    H&H hops item is not. Generalize it to a seed lookup, `GrapeTypeRegistry.forSeed(item)`, returning a lattice type.
    The `GrapeBushSeedItem` path keeps working through it.
  - Hops have no juice (`getBottle()` null). Guard every `getBottle`/`getFruit` caller:
    - `V/core/block/GrapevinePotBlock.java:170,196`: stomping hops does nothing.
    - `V/core/compat/jei/port/GrapeStompingCategory.java:53-57`: skip.
    - `V/core/compat/jei/port/GrapePlantingCategory.java:87`: show hops on the lattice.
    - `V/core/mixin/FoxEatBerriesGoalMixin.java:56`: skip hops **[default]**.
    - `V/core/command/VineryCommands.java:271`: list it.
  - Rendering hops on the lattice without touching Vinery's files:
    - Vinery's assets are All Rights Reserved, and Satisfy refused redistribution (note [backported-vinery](agent_notes/backported-vinery.md); alcohol.md
      B3.6). The port reads them from the unmodified donor jar in `vinery_upstream/`. So no Vinery model, texture or
      blockstate is copied or edited.
    - The `vinery:grapevine_lattice` blockstate file belongs to Vinery and has no `grape=hops` variants. A client
      `ModelEvent.ModifyBakingResult` hook in the vinery member's client code (MIT, ours) maps each `grape=hops`
      `ModelResourceLocation` to the baked model of the matching `grape=jungle_white` state.
    - The white grape art is therefore used in place, with no file copied and no `sources.csv` row (nothing derived).
  - Lang: the lattice tooltip (:172) names hops; our own lang key in the port's lang file.
  - Rebuild `:creativeBrewing:` alongside `:creativeDelight:`. Creative Brewing is one package (memory
    `creative-brewing-package-deal`).
- Remove the Hearth and Harvest lattice (owner):
  - Remove the lattice block(s) and `HopsLatticeBlock`, with their item, recipe, blockstates, models, textures, loot
    tables and lang (`HHBlocks.java:53-55`; find the empty-lattice block beside it at build).
  - Also the `onRightClickBlock` hops planting at `HearthAndHarvestMod.java:89`, and the dict keys and tags that name the
    lattice (grep `lattice` in the hearth member, `KJ/hearthandharvest/` and `item_dict.js`).
  - Old worlds: swap entries `hearthandharvest:<lattice> -> vinery:grapevine_lattice` and the hops-on-lattice block to
    `vinery:grapevine_lattice[grape=hops]`, keeping the age where it lines up.
    - An H&H lattice standing on farmland with no wall behind it pops off and drops a Vinery lattice item: no item loss.
    - Test on a fresh old-id save (note [forge-missing-mappings-staging](agent_notes/forge-missing-mappings-staging.md)).
- Models and textures for the wild vine (`hearthandharvest:hops_vine`, our block):
  - We write our own blockstate. Its multipart cases point at Vinery's models by id
    (`vinery:block/jungle_grape_vine_stage0`, `_stage1`, `_white_stage2`, `_white_stage3`).
  - Those models and the white grape textures load from the donor jar in place. Nothing is copied, so there is no
    `sources.csv` row.
  - Hops only exist when Vinery is loaded (gate above), so the referenced models are always present.
  - The old `hops_vine_stage0-3.png` and the H&H lattice textures retire with their ledger rows. The hops item keeps its
    H&H texture.
- Retires: the H&H lattice planting path, the farmland rule `hopsRequireFarmland` (`HHConfig.java:40`; keep the key as
  ignored and say so in the config comment), TODO `#75`, the hops half of `#70`, and `#73` item 2.
- Wild hops (SQ-HH-304; the old cause was the `MOTION_BLOCKING` canopy heightmap plus rarity 120):
  - Replace `KJ/../worldgen/{configured,placed}_feature/wild/wild_hops.json` with a vine feature modeled on Vinery
    `world/feature/JungleGrapeFeature.java`, hanging hops vines on tree trunks.
  - Where (owner): every temperate forest biome. The rule `wild_hops` in `kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.json:155`
    already uses `temperate grassy forest`, which resolves to 18 biomes (vanilla forests plus Terralith;
    `crops_by_biome.md:264`). Keep it.
  - Rate (owner: rarer than a typical biome crop): wild crops run 1 in 30 to 64 chunks. Hops use rarity 1 in 96 chunks,
    and one tree tried per hit.
  - Change the rule's `"requires"` from `hearthandharvest` to `vinery`. Regenerate `crops_by_biome.md`
    (`tools/gen_crop_worldgen_table.py`).
- Verify:
  - New SQ-HOP-01 to -06:
    - no hops ids without the Brewing jar (fold into SQ-DEP-02);
    - plant on a log face;
    - growth to age 3;
    - harvest yield and reset;
    - shears sterilize;
    - wild vines in a located forest box (count per 256 chunks, below the typical crop's count).
  - New SQ-HOP-07 to -10:
    - a Vinery lattice on a wall takes hops at age 0;
    - growth at light 9 or more;
    - harvest at age 3 or more drops hops and resets to 1, and shears reset to 0;
    - hops in a stomping pot do nothing, jungle grapes on the lattice still work, and `hearthandharvest:*lattice*` ids are
      gone.
  - New SQ-HOP-11: an old save with H&H lattices and hops loads them as Vinery lattices.
  - Client CQ-HOP-01: the vine and lattice stages show the white grape art; JEI shows hops in Grape Planting and not in
    Stomping.
  - Rerun SQ-HH-304 rewritten. Retire SQ-HH-1547, -1548, -1555 and -1555h, with a note in the queue.

### 3.2 Drop H&H tilled end stone (TODO `#70` first half; C + B)

Status: dict part DONE 2026-11-14 (common): `item_dict.js` `i.tilled_end_stone = 'hearthandharvest:tilled_end_stone'` deleted; the endercrop item line and `block_dict.js` `b.tilled_end_stone` kept. The rest BUILT 2026-11-14 (cd-b), CD installed, not run (SQ-HH-1546 rewritten, -1546b, -1546c). Build check (10.2): Ender Crop tills in `BlockToolModificationEvent` (HOE_TILL, `ForgeEventHandlers.onUseHoe`, returns early when canceled); gated by `TillingEvents.onToolModification` at HIGHEST, no RightClickBlock handler needed. Ender Crop's `endstoneNeedsUnbreaking` set to false (owner to confirm: the netherite rule replaces its Unbreaking rule). The tooltip is on end stone and Ender Crop's tilled end stone: the dict has no `endercrop:ender_seeds` key (Common could add `i.ender_seeds`). `HHTags.GROWS_ON_TILLED_END_STONE` deleted (nothing reads it).
- Remove the H&H block and its pieces:
  - Code: `HHBlocks.java:58`, `HHItems.java:174`, `block/TilledEndStoneBlock.java`.
  - Assets: the blockstate, models, textures and loot table under `members/hearth/src/main/resources`.
  - Lang: `en_us.json:207`.
- Point the end stone hoe branch `gear/TillingEvents.java:91-92` at `endercrop:tilled_end_stone` when Ender Crop is
  loaded. Otherwise give no H&H conversion.
- End stone tills only with a netherite or better hoe (owner, 2026-11-14):
  - "Netherite or better" = the hoe is a `TieredItem` whose tier sorts at or above `Tiers.NETHERITE` in Forge's
    `TierSortingRegistry`. That catches modded tiers above netherite.
  - Ender Crop tills end stone itself, with any hoe (`io/github/drmanganese/endercrop/HoeHelper`, `event/ForgeEventHandlers`
    in `mods/endercrop-1.19.2-1.7.0-beta.jar`). That path also needs the gate:
    - a `RightClickBlock` handler at HIGHEST priority cancels the event, with result PASS, for end stone with a lower hoe;
    - also check `BlockEvent.BlockToolModificationEvent` (`HOE_TILL`) in case Ender Crop uses it; read its class at build.
  - The H&H area hoe (`TillingEvents` area till) skips end stone under the same rule.
  - Tooltip on Ender Crop's seeds and end stone, or a JEI info page: "Till end stone with a netherite hoe". Record it in
    [tooltips.md](tooltips.md).
  - Verify: new SQ-HH-1546c. An iron and a diamond hoe do nothing on end stone; a netherite hoe makes Ender Crop tilled
    end stone. The area till behaves the same.
- Repoint `HHTags.java:15` `GROWS_ON_TILLED_END_STONE` (or the tag JSON) at the Ender Crop block.
- Dict: delete `item_dict.js:131`. Keep `:12605` (endercrop) and `block_dict.js:208`.
- Tags: `KJ/hearthandharvest/tags.js:60` mineable/pickaxe now targets the Ender Crop block, guarded `// requires: endercrop`.
  Check `kubejs/server_scripts/tags/blocktags.js:374,475`.
- Swap entry `hearthandharvest:tilled_end_stone -> endercrop:tilled_end_stone` in `swap_merges.js`, plus a fresh old-id save
  test (note [forge-missing-mappings-staging](agent_notes/forge-missing-mappings-staging.md)).
- Docs:
  - Retire `texture_sources/ledger.csv:977-978`.
  - Update `hearth_and_harvest.md` J75 and `tools/blockrunner_untagged_blocks.json:528`.
- Verify: SQ-HH-1546 rewritten (Ender Crop tilled end stone pickaxe-mineable, End crops plant). New SQ-HH-1546b (old save
  with the H&H block loads as Ender Crop).

### 3.3 Mums tags (TODO `#71` first half)
Status: DONE 2026-11-14 (cd-a).
- `KJ/hearthandharvest/tags.js:37` (items):
  - Add `minecraft:small_flowers`, `vinery:small_flower` (guard: vinery), `another_furniture:planter_box_placeables`
    and `creativecompat:flower_box_plants`.
- `:49` (blocks):
  - Add `flowerpatch:bonemeal_able_flowers`, `terralith:plants` and `c:ignoredblocks`.
- Every tag through `global.tags` with `has.mod` guards per namespace.
- Verify: SQ-HH-309 rerun, plus the block tags.

### 3.4 Syrup to sugar (TODO `#72`; B rustic member)
Status: DONE 2026-11-14 (cd-a). Dropped through the Rustic bake (`DROPPED_FILES` in bake_upstream.py), not a hand delete. `#72b` row added.
- Delete `members/rustic/src/main/resources/data/rusticdelight/recipes/sugar_from_syrup.json`. Clean-build the member
  (note [repack-stale-resources](agent_notes/repack-stale-resources.md)). The CD recipe `KJ/hearthandharvest/recipes.js:174` (4 sugar) stays.
- Later, a separate step, not in this run: add a heated Create mixing recipe with a better yield (`R.createMixing(..., 'heated')`,
  the pattern at `recipes.js:173`). The owner picks the value. TODO row to add: `#72b`.
- Verify: SQ-HH-1593 rerun (only the 4-sugar recipe).
- `#72b` built 2026-11-17 (lane F2, plan_exec_1117): owner ruling 8 sugar per syrup bottle, bottle returned; Cooking Pot
  recipe `creativedelight:farmersdelightcooking/sugar_from_syrup` (Slice & Dice mirrors it to heated mixing); the
  4-sugar hand craft kept. Syrup fluid `creativedelight:syrup` (CD startup `fluids.js`, tag `forge:fluid/syrup`) with
  heated sap -> syrup fluid (1000 -> 250 mB), syrup fluid -> 8 sugar, bottle <-> 250 mB; inert until Creative Common's
  fluid dict has `f.syrup`. Textures: Hearth and Harvest 1.4.1 `block/fluid/syrup_still` / `_flow`, exact copies
  (`tools/port_hearth_assets.py`). Queue SQ-HH-1613 to -1616 (rows/HH.qb section 4).

### 3.5 Crow feeding and breeding (TODO `#66`, `#67`)
Status: DONE 2026-11-14 (cd-a). A hurt tame crow heals 2 health per food; JEI page on the crow spawn egg.
- Crow foods (owner, 2026-11-14), defined in one place:
  - **Crow food** = item tag `creativedelight:crow_food`
    (`kubejs/data/creativedelight/kubejs/hearthandharvest/crow_salt_tags.js:12`): `#forge:seeds` plus the corn tags.
  - It resolves to 43 items in the 2026-11-13 dump:
    - every seed and pip in the pack (vanilla wheat, beetroot, melon and pumpkin seeds; the Farmer's Delight, Rustic,
      Veggies, Vintage, Fruits, Vinery grape, Neapolitan, Berry Good and Delightful seeds and pips; rice; coffee beans;
      Ender seeds; tobacco seeds);
    - plus `corn_delight:corn` and its seeds.
    - Not in it: Alex's Mobs' own crow food tag `alexsmobs:crow_foodstuffs`, which adds eggs and sugar. That is a
      different crow and is untouched.
  - **Uses:**
    - Wild crow hand feeding (only with `handFeeding` on) eats one crow food. Each feed is a taming roll.
    - A tame crow eats crow food to heal.
    - **Breeding food** = corn only (`corn_delight:corn`, its seeds) while `cornBreeding` is on (default). With it off,
      any crow food breeds.
  - Writeup: the definition goes in `hearth_and_harvest.md` (crow section) and the JEI info page for the crow.
    Tooltip: none.
- `crow/CrowEntity.java:207-231` `mobInteract`:
  - Wild crow plus crow food: hand feeding only when `handFeeding` is true (`CrowConfig.java:42`). Otherwise PASS, with
    no eat and no love.
  - Tame owned crow plus food:
    - With `cornBreeding` on (`:44`), only corn starts love (the rule in `CrowEvents.java:110`). Any crow food heals.
    - With `cornBreeding` off, any `crow_food` starts love. This mirrors the H&H upstream reading; check the config
      comment at build.
- `registerGoals` (`:91-106`): add `BreedGoal(this, 1.0)` at the vanilla animal priority.
- Verify: SQ-HH-434 and -440 rerun. New SQ-HH-440b: cornBreeding off breeds with another crow food.

### 3.6 Horseshoe (TODO `#69`)
Status: DONE 2026-11-14 (cd-a). Unshoe item rule: empty hand, shears or a horseshoe.
- Move sneak-unshoe out of `gear/HorseshoeItem.java:27-54` into an `EntityInteract` handler. It runs before the horse
  opens its inventory: cancel and return SUCCESS when a shod horse is sneak-clicked with an empty hand or shears (keep the
  current item rule).
- `gear/HorseshoeEvents.java:73-80` `onAttack`: compare `source.getMsgId()` with `"cactus"` and `"sweetBerryBush"`, not
  identity.
- Verify: SQ-HH-1505 and -1507 rerun, with the shod state re-read on the same boot.

### 3.7 Salt lick: edge bug and layered stages (TODO `#76`; B + texture rule)
Status: DONE 2026-11-14 (cd-a). Rows renamed: SQ-HH-483/-486/-487 were taken, so SQ-SLK-01 to -04 and CQ-SLK-01. Layers: partial stages cannot hold a layer (not a full top face), so the refill is `LickableSaltBlock.neighborChanged`, and Vintage `SaltLayerBlock.onPlace` notifies the block below. Old stage textures deleted, ledger rerun.
2026-11-15 (fix-2, r1115): SQ-SLK-01b, a cow beside the block never licked (block-position distance > 2 where navigation stopped); `closeEnough` also accepts the body within 1 block of the block. Rerun SQ-SLK-01c.
- Edge bug: `salt/StayNearSaltGoal.java:182-202` `findTrip` needs a full path (`canReach`), but cow FOLLOW_RANGE (16) is
  shorter than `animalRadius` (20).
  - Fix: when the salt is farther than follow range minus 2, accept a partial path toward it and re-plan on arrival.
  - The same retry budget (`RETRY_TICKS` :28) stays as the stop.
- Layered stages:
  - The stages are separate blocks (`salt/HHSaltRegistry.java:37-43`; chain `salt/SaltLick.java:23-26,96-108`).
  - Give `LickableSaltBlock` a per-stage `getShape`/collision: 12/8/4 px **[default]**. Waxed stages get the same height
    as their unwaxed twin.
  - Models parent vanilla `block/snow_height12`, `/8`, `/4` with texture `salt:block/raw_rock_salt_block` on every face
    of every stage **[default]**. This is an existing upstream texture referenced in place, not a copy. Item models
    follow.
  - The three `*_licked_salt_block.png` become unused. Leave the files, retire their `ledger.csv` rows, or delete them
    and the rows.
  - No texture is drawn or edited. If a stage needs a copied file, it gets a `sources.csv` row.
- Verify:
  - SQ-HH-482 rerun (cow at 21 blocks makes a trip).
  - New SQ-HH-483: cow at 18.
  - New SQ-HH-486: each stage's collision height.
  - Client CQ-HH-486: the stages look like rock salt layers, no tint.
  - Also rerun SQ-HH-1534 (single till, INCONCLUSIVE r1112).
- Salt layers refill a licked block (owner, 2026-11-14):
  - Placing a Vintage Delight salt layer (`vintagedelight:salt`, `members/vintage/.../block/custom/SaltLayerBlock.java`)
    on top of a licked stage drops it into the lick: the layer is consumed and the block goes back one stage (heavily ->
    well -> lightly -> `salt:salt_block`).
  - **[default]** A waxed stage keeps its wax.
  - Hook it in `SaltLayerBlock.onPlace` (our port code): if the block below is a `LickableSaltBlock` stage above 0, set
    that block one stage back, and remove the layer or reduce `layers` by 1. That covers players, dispensers and falling
    layers alike.
  - **[default]** One layer restores one stage. A multi-layer placement repeats until the lick is full, and any layers
    left stay on top of the full block.
  - On a full `salt:salt_block` a layer places normally. This keeps SQ-HH-474's dust behaviour: dust on an unworn block
    places a layer, which is answered, so `#73` item 7 closes.
  - Verify: new SQ-HH-487. A layer on a heavily licked block gives well licked, with no layer left. Three layers on
    heavily licked give a full block with no layer. A layer on a full block stays a layer. A waxed stage stays waxed.

### 3.8 Melting system: salt, fire and lava (TODO `creativedelight/members #56`; C Creative Common, B salt member)
Salt member (B) part: DONE 2026-11-14 (cd-a). `tryMeltFromBlock` always false (also covers CD `SaltBlockTicks`), salt item click calls Common `isTarget`/`meltStep` by reflection. Stage ids unchanged.

Status: DONE 2026-11-14 (common) for the Creative Common part: `melting/` package, `[melting]` config, tags script, three mixins, built and installed; writeup [melting.md](melting.md); queue SQ-MELT-01 to -10, -P1, SQ-SALT2-06 (rewritten), -06b. The salt member bullet is agent cd-a's (Creative Delight). r1113 Nether ice: Immersive Weathering's `ice_icicle_and_melt` growth, left on (melting.md 6). Fire-tag light levels: not in any dump; logged at boot and checked by SQ-MELT-10. `[melting]` is added by Forge on the next boot (defaults = pack values, no `configs_edited` copy).

New system, owner 2026-11-14. It replaces the salt member's block-tick melting and the plan's earlier stage-chain-only fix.

- Why the old one failed: `salt:salt_block` is a `SaltSandBlock` copied from sand with no `randomTicks()`, so
  `ISaltBlock.onSaltRandomTick` -> `tryMeltFromBlock` never runs.
  - Trace why Nether ice still melted (r1113), to make sure no second path stays live.
- Owner: Creative Common, new package `melting/`. It works without the Salt mod, and the salt source is a tag. Config
  `[melting]` in `creativecommon-common.toml` plus its `configs_edited/root_config/` copy.
- Source tags (block tags, `creativecommon:melting/*`, written through `global.tags` with `has.mod` guards):
  - `salt`:
    - `salt:salt_block`, `salt:raw_rock_salt_block`;
    - the Vintage Delight salt blocks;
    - the Hearth and Harvest licked and waxed salt stages (3.7).
  - `fire` (owner: magma blocks, campfires, fire blocks, torches, lanterns and glowstone count as fire):
    - `#minecraft:fire` (fire, soul fire);
    - `#minecraft:campfires`, only while `lit=true`;
    - `minecraft:magma_block`;
    - `minecraft:torch`, `wall_torch`, `soul_torch`, `soul_wall_torch`;
    - `minecraft:lantern`, `soul_lantern`, `jack_o_lantern` (owner: lanterns count) **[default: jack o'lantern counts]**;
    - `minecraft:glowstone`.
    - Any block in the tag that has a `lit` property counts only while `lit=true`.
    - Left out: redstone torches (light 7, redstone builds), `sea_lantern`.
    - Modded torches and lanterns: 131 ids in `kubejs/exported/tags/block_tags_by_block.json` (2026-11-13 dump) match
      torch / lantern / sconce / brazier / candelabra. Split by whether a flame gives the light. Each mod's lines are
      guarded by `has.mod`.

      | Mod | Counts as fire | Left out (not a flame) |
      |---|---|---|
      | supplementaries (19) | all sconces (`sconce`, `_wall`, `_soul`, `_ender`, `_glow`, `_green`, `_nether_brass`, `_lever`), `brass/copper/crimson/lead/silver_lantern`, `wall_lantern` | |
      | everythingcopper (16) | all 16 copper and copper soul lanterns, every oxidation and waxed stage | |
      | redeco (18) | 16 dyed paper lanterns, `stone_lantern`, `deepslate_lantern` | |
      | upgrade_aquatic (17) | `tooth_lantern` | 16 jelly torches (bioluminescent) |
      | framedblocks (6) | `framed_torch`, `_wall_torch`, `framed_soul_torch`, `_wall_torch` | `framed_redstone_torch`, `_wall_torch` |
      | fairylights (7) | `candle_lantern`, `oil_lantern`, `paper_lantern`, `jack_o_lantern` | `orb_lantern`; `candle_lantern_light`, `oil_lantern_light` (light helper blocks) |
      | architects_palette (6) | `nether_brass_torch`, `_wall_torch`, `nether_brass_lantern`, `glowstone_cage_lantern` (glowstone) | `algal_cage_lantern`, `redstone_cage_lantern` |
      | twigs (5) | all 5 paper lanterns | |
      | quark (4) | `blaze_lantern`, `paper_lantern`, `paper_lantern_sakura`, `duskbound_lantern` **[default: counts]** | |
      | dustrial_decor (4) | `large_lantern`, `large_soul_lantern` | `redstone_lantern`, `large_redstone_lantern` |
      | alexsabnormalities (3) | the three tooth lanterns | |
      | securitycraft (3) | `reinforced_lantern`, `reinforced_soul_lantern` | `reinforced_sea_lantern` |
      | immersiveengineering (2) | `lantern` | `electric_lantern` |
      | decorative_blocks (2) | `brazier`, `soul_brazier` (while lit) | |
      | blocky_siege (2) | `brazier_on` | `brazier_off` |
      | nethersdelight (2) | `propelplant_torch`, `_wall_torch` | |
      | fantasyfurniture (2) | `necrolord_candelabra`, `royal_candelabra` (while lit) | |
      | domesticationinnovation (1) | `wayward_lantern` | |
      | botania (1) | | `animated_torch` (redstone) |
      | philipsruins (1) | | `frozen_sea_lantern` |

    - The build re-greps a fresh `/dumpall` for ids added since 2026-11-13. The same name match also turns up any other
      flame block (candles are left out **[default]**; their light is 3 per candle).
    - Every fire-tag block must give light 4 or more, so the light check (below) still sees it at 3 blocks. Any lower one
      (magma, light 3) joins magma's source-driven path.
  - `lava`: any block whose fluid state is `#minecraft:lava` (source and flowing), plus `minecraft:lava_cauldron`
    **[default]**.
- Target tags and results:
  - `creativecommon:melting/meltable` (owner: ice, snow, powder snow, snow layers):
    - `minecraft:ice` and `frosted_ice` become water (air in an `ultraWarm` dimension, or when the place-water option is
      off).
    - `snow_block`, `powder_snow` and `snow` (layers) become air.
  - `creativecommon:melting/meltable_slow` (owner: packed and blue ice, one tier down):
    - `blue_ice` -> `packed_ice`, `packed_ice` -> `ice`.
- Ranges (owner):

  | Source | Meltable | Packed and blue ice (low chance) |
  |---|---|---|
  | salt | adjacent (6 faces) | adjacent |
  | fire | within 3 blocks | adjacent |
  | lava | within 4 blocks | within 2 blocks |

  - "Within N" = Euclidean distance ≤ N, through any blocks **[default]**.
  - Config keys: `saltRange`, `fireRange`, `lavaRange`, `slowFireRange`, `slowLavaRange`.
- Rates: one roll per random tick of the target.
  - `meltChance` **[default 0.4]**, the current Salt value.
  - `slowMeltChance` **[default 0.05]** (owner: low).
  - Roughly: at the default randomTickSpeed a block random-ticks about every 68 s. Ice next to a source melts in about
    3 min on average; a packed ice tier takes about 23 min.
- Ticking and cost:
  - Ice and snow layers already random-tick (vanilla light melting).
  - Turn on random ticks for `snow_block`, `powder_snow`, `packed_ice` and `blue_ice`, the same way
    `src/main/java/com/SaintAlfonzo/creativedelight/salt/SaltBlockTicks.java:68-76` does (field `f_60445_`).
  - Each target random tick does a cheap check first, then a scan:
    1. The 6 neighbors, for salt and fire (adjacent cases).
    2. Only if block light at the target is above 0: a sphere scan of radius `max(fireRange, lavaRange)`, stopping at
       the first hit. Fire, soul fire, campfires, torches (soul torch 10), lanterns, glowstone and lava all give light
       10 or more, so the light check never hides them within range.
  - Magma gives light 3, so it can sit dark 3 blocks away. Magma blocks therefore get random ticks too, and melt
    targets in their own 3-block sphere (source-driven). The same melt rule and chances apply.
  - Glaciers, frozen oceans and ice spikes then cost only the neighbor check per tick (dark, no sources).
  - Perf row: SQ-MELT-P1 with the perf suite in an ice spikes box (TPS and MSPT against a control).
- Vanilla light melting: **[default]** off. Torches, lanterns and glowstone melt through the fire rule instead (3 blocks;
  vanilla was light-based). Sea lanterns, shroomlights and the other non-flame lights stop melting ice and snow layers.
  - This takes a mixin on `IceBlock.randomTick` and `SnowLayerBlock.randomTick` light checks, behind the config switch
    `vanillaLightMelting = false`.
  - Serene Seasons' seasonal melt is not touched.
- Salt item click (`SaltItemMeltingEnabled`): stays. One stage per click for both tiers, always succeeding.
- Salt member (B): its block random-tick melt (`Melting.java`, `[salt.Melting] SaltBlockMeltingEnabled`/`Chance`) is
  switched off and its config comment points at `[melting]`, so nothing double-fires. `Melting.meltedState` is reused or
  replaced by Common's result table.
- Nether snow melts at once:
  - Hook `BlockEvent.EntityPlaceEvent` plus a `SnowLayerBlock`/`SnowBlock` `onPlace` check (mixin, or a neighbor-update
    handler). In any `ultraWarm` dimension, snow layers, snow blocks and powder snow become air when placed. This covers
    players, dispensers, snow golem trails and commands.
  - Decide powder snow at build. **[default]** It melts too.
- Before this system (determined 2026-11-14, for the writeup's history): nothing in the pack melted by heat. Vanilla
  melts ice above block light 10 and snow layers above 11 (about 4 and 3 blocks from lava). Magma (light 3) melted
  nothing, and packed ice, blue ice, snow blocks and powder snow never melted.
- Writeup: new `feature_writeup/melting.md` (rules, tags, config, tests), with a cross-link from `salt.md`. Changelog
  family "Melting".
- Verify (rerun SQ-SALT2-06 rewritten; new SQ-MELT rows, randomTickSpeed raised in the batch):
  - SQ-MELT-01: Salt Block beside ice, snow block, powder snow and snow layers -> water / air / air / air.
  - SQ-MELT-02: fire at distance 3 melts ice and snow; at 4 it does not. The same for soul fire, a lit campfire, a lit
    soul campfire and magma. An unlit campfire does nothing.
  - SQ-MELT-03: lava at distance 4 melts; at 5 it does not. Flowing lava counts.
  - SQ-MELT-04: blue ice -> packed -> ice -> water beside salt, at the slow rate (count steps over N ticks against
    `slowMeltChance`). Packed ice beside fire steps down; at 2 from fire it does not. At 2 from lava it steps down; at
    3 it does not.
  - SQ-MELT-05: in the Nether, ice beside salt -> air.
  - SQ-MELT-06: at distance 3, a torch, a soul torch, a lantern, a Supplementaries sconce, a copper lantern and
    glowstone melt ice and snow; at 4 they do not.
    - A sea lantern, a redstone torch and a jelly torch beside ice do nothing (`vanillaLightMelting = false`).
    - An unlit brazier does nothing.
    - With the switch on, vanilla light melt is back.
  - SQ-MELT-10: tag check. Every id in the 3.8 table resolves, and each fire-tag block's light is 4 or more (or it is
    on the source-driven list).
  - SQ-MELT-07: magma 3 blocks away in a sealed dark room melts ice (source-driven path).
  - SQ-MELT-08: the salt item click steps blue ice one tier per click.
  - SQ-MELT-09: without the Salt and Vintage jars (Common + Compat set), fire and lava melting work and there is no
    error.
  - SQ-MELT-P1: perf (above).
  - New SQ-SALT2-06b: snow placed in the Nether is air at once (place, `setblock`, dispenser).

### 3.9 Corn maze locate: see 6.3.

### 3.10 Stomping basin: valid items only, and every Vinery stomping (B hearth member + A scripts; was TODO `#73` item 1)

Status: BUILT 2026-11-14 (cd-b), CD installed, not run (SQ-HH-266 rerun, -266b, -266c). Deviation: the container is
`vinery:wine_bottle` (`i.wine_bottle`), not a glass bottle: `GrapevinePotBlock.takeJuice` takes a wine bottle. The recipes
have four ingredients, so only a 2x2 basin group can stomp them. Build check (10.2): the donor jars hold no recipe-based
stomping (their recipe types are fermenting, apple mashing for the Apple Press, crafting). The grape list is
`GrapeTypeRegistry` (8 types with juice), written out in `hearthandharvest/vinery_stomping.js`.
- Today, `machine/StompingBasinBlockEntity.java:121-135` `insert` takes any item; an empty bucket gets swallowed
  (SQ-HH-266). The item handler behind its capability (`:70`, ItemStackHandler at `:346`) does the same for hoppers.
- Valid items (owner, 2026-11-14): only items that appear as an ingredient in some `hearthandharvest:stomping` recipe.
  - Build a set of valid items from every stomping recipe's ingredients when recipes load (`RecipesUpdatedEvent` and the
    server reload listener). `insert` returns the stack untouched, and the handler's `isItemValid` returns false, for
    anything else.
  - A right-click with an invalid item passes through, so the item's own use runs.
- Mirror all Vinery stomping (owner):
  - The Vinery pot (`V/core/block/GrapevinePotBlock.java`) turns 3 grapes of one type (`GRAPES_PER_BOTTLE = 3`, :53) into
    one juice bottle of that type, taken with an empty glass bottle (`takeJuice`, :168-190).
  - The juice is `GrapeType.getBottle()`, for every type in `GrapeTypeRegistry` with fruit and bottle:
    red/white, savanna, taiga and jungle red/white. Hops are skipped (no juice).
  - Add one basin recipe per grape type, through `R.hhStomping(null, [{ id: fruit, count: 3 }, i.glass_bottle], e, id,
    juiceBottle)` (helper at `neoCraftHelper.js:1511`, item-output form). Put them in a new server script with
    `// requires: vinery`; ids and dict keys from `item_dict.js`, not literals.
  - At build, read the grape list from the registry (or a dump), so a later grape type is not missed.
  - If any Vinery recipe-based stomping exists beyond the pot (a `vinery:` stomping recipe type in the donor data), it
    is mirrored the same way; grep the donor jar's data at build.
- JEI: the basin category (`machine/client/StompingCategory.java`) shows the new recipes with no extra code.
- Verify:
  - Rerun SQ-HH-266: an empty bucket and dirt are refused, and a hopper does not insert them.
  - New SQ-HH-266b: 3 white grapes + 1 glass bottle stomped give 1 white grape juice. The same for one savanna and one
    jungle type.
  - New SQ-HH-266c: hops refused.
- Changelog: under a Stomping Basin family.

## 4. Pack fixes outside Hearth and Harvest

### 4.1 Portal Gun exit lift, applied globally (D; TODO `gameplay/tnt-and-portal #15`)
- **Status 2026-11-14 (misc): BUILT, Creative Compat installed, untested.** `mixin/IPExitLiftMixin` (client list, gated `IMM_PTL`): HEAD inject keeps the portal, `@Redirect` of the GETFIELD returns `getContentDirection().y > 0.5` only (the saved flag is ignored, decision 0.3); old class, mixin, json entry and plugin entry deleted; `javap` shows `f_82480_`. plan_portal_gun.md 3, D3 and the file table rewritten. Queue: SQ-PG-01 split into -01a (server), -01b, -01c (scratch client), CQ-PG-03 feel.
- Today: `PortalGunExitLift.java:20` rotates the already rotated content direction, and the mixin misses portals linked in
  `PortalGunItem`.
- New: one client mixin on Immersive Portals `ClientTeleportationManager.teleportPlayer`, in the `client` list of
  `CC/resources/creativecompat.mixins.json:58` and gated on `IMM_PTL` (`CompatMixinPlugin.java:27`).
  - It runs IP's `adjustPlayerPosition` only when `portal.getContentDirection().y > 0.5` (the exit portal is on a floor).
  - Plain Mixin: `@Inject` at HEAD stores `teleportation.portal()`; `@Redirect` on the `adjustPositionAfterTeleport`
    GETFIELD (bytecode 318-329) returns `field && floorExit`. No MixinExtras, because CC does not build with it.
  - Applies to every IP portal: nether frames, Portal Gun, End, Immersive Portals' own.
- Delete:
  - `portalgun/PortalGunExitLift.java`
  - `mixin/PortalGunExitLiftMixin.java`
  - its entry at `creativecompat.mixins.json:52`
  - `CompatMixinPlugin.java:85-86`
  - The saved per-portal flag is ignored **[default]**. Stale NBT on old portals is harmless.
- Docs: [plan_portal_gun.md](plan_portal_gun.md) section 3 (78-110), D3 (157), the row at 198, and "Not chosen" (c) at 103.
- Verify: SQ-PG-01a and -01b rewritten.
  - The lift is decided by geometry for wall->wall (no lift), wall->floor (lift) and floor->wall.
  - The same after a restart.
  - New SQ-PG-01c: a nether frame portal into a wall-facing exit gets no lift.
  - Client CQ-PG for the feel.

### 4.2 Golden tools, armor and food craftable with rose gold (A; TODO `recipes/metal #7` closes as a decision)
- **Status 2026-11-14 (misc): BUILT (`/reload`), untested.** `goldOnly` is now the raw forms list (ore, raw, crushed, dust, nugget, ingot, block, bars, plate, rod, wire, their storage blocks and coils) for gold and rose gold; header rewritten. SQ-GAP-04 and SQ-T1-04 rewritten, TODO `#3` decision and `#7` closed, brief and recipes_metal.md F5 flipped. Changelog under Rose Gold.
- Tools and armor already accept rose gold, and food takes any gold nugget, so the r1113 "FAIL" is the wanted behaviour.
- Make it explicit:
  - `kubejs/data/creativeengineering/kubejs/metal/rose_gold_interchange.js:12-19`: limit `goldOnly` to the raw gold forms
    (block, ingot, nugget, bars, dust, plate, rod, wire). Dict-keyed golden items then also switch to the tag.
  - Rewrite the header comment at 4-6.
- Flip the docs:
  - SQ-GAP-04 (`scratch_queue.md:5286`)
  - SQ-T1-04's second group (`:5258`)
  - TODO `recipes/metal #3` decision
  - `briefs/recipes_metal.md:183`
- Verify: SQ-GAP-04 rewritten; the golden apple, a golden pickaxe, a golden chestplate and a golden carrot each craft
  with rose gold.
- Changelog: under Rose gold, if not already stated.

### 4.3 Bitten cakes count as cake slices (B; TODO `food/types-and-tuning #39`)
Status: DONE 2026-11-14 (cd-a).
- `kubejs/generation_scripts/food/placed_food_slices.json`: add `"supplementaries:directional_cake": "farmersdelight:cake_slice"`.
  - Check `supplementaries:double_cake` (a cake on a cake). **[default]** It maps to cake_slice.
- CD `food/PlacedFoodSol.java:96-104`: add a branch for cake or candle cake becoming `directional_cake` on the first bite.
  It records the slice for Spice of Life and the Food Dictionary, with `applePie = true`. Today the bite falls to the
  `return` at :103.
- Pies: no change **[default]** (`FdPies`, 122-129).
- Verify: SQ-FDI-03 rerun. Both books count `cake_slice` after the first bite and every later bite. New SQ-FDI-03b: a
  double cake.

### 4.4 Cave carrot patties (A; TODO `dishes/ingredients #43`)
- **Status 2026-11-14 (misc): fixed in source**, ships with the next Creative Delight build (cd-b). Both cutting recipes use `Item.of(..., 2)`. SQ-OB-05 row noted. No new changelog bullet (the unreleased bullet already states it).
- Not yet fixed in source. In `KJ/veggiesdelight/recipes.js:98-99`:
  - Use `Item.of(fd.veggie_vegetarian_patty, 2)` (raw cave carrot) and `Item.of(fd.veggie_cooked_vegetarian_patty, 2)`
    (baked).
  - `n()` at :45 stays for `ceBaking` at :48.
  - The ratio stays 1 -> 2.
  - The Slice & Dice copies follow.
- Verify: SQ-OB-05 rerun.

### 4.5 Felsic scrap 1-2 (E; TODO `gear/tuning #23`)
- **Status 2026-11-14 (misc): DONE.** Comment says 1-2, SQ-CBP-11 row and creative_equipment.md fixed; Creative Equipment rebuilt and installed (the comment is a jar resource). No changelog.
- The loot table already rolls 1-2 (`development/creativeEquipment/.../felsic_gear/loot_tables/blocks/felsic_block.json:32`).
  Fix its `__comment` (line 2), the SQ-CBP-11 row (`scratch_queue.md:4779`), and the felsic writeup.
- Changelog: none; no behavior change.

### 4.6 Barbeque's Delight basin eats the stick (B; TODO `dishes/meals #67`)
Status: DONE 2026-11-14 (cd-a). The exact loss path was not proven by reading; a stick is now never stored and passes without a recipe.
2026-11-15 (fix-2, r1115): SQ-BBQW-02s FAILed (PASS let Supplementaries place the stick on the basin); the tool branch now returns CONSUME. Rerun SQ-BBQW-02t.
- Not in the owner brief by name, but it is an item-loss FAIL. Fix with the batch: on a one-beef basin the stick
  interaction must return PASS and keep the stick.
- Verify: SQ-BBQW-02s rerun.

## 5. Guards for every feature that relies on another mod (A + B + C)

### 5.1 Known failures
CD items #57, #58: DONE 2026-11-14 (cd-a), through a new `packs/mods/<modid>/` data pack per mod (`CompartmentPacks.addModData`).

Status: SQ-DCM-02 part DONE 2026-11-14 (common): `func/has.js` `global.has._modOf` namespace -> mod id map (the five below plus rare-ice -> rare_ice); TODO `creativecommon/publish #5`. The other bullets are B / A.
- `creativedelight/members #57`: H&H loot modifier `hearthandharvest:cherries_from_cherry_leaves` names `vinery:*`.
  Gate it on vinery (Forge `forge:mod_loaded` condition on the GLM entry, or move it to the `packs/` split).
- `creativedelight/members #58`: the Display dish `placed_corn_popsicle` block, its loot table and its models gate on
  corn_delight. Use the Java `isLoaded` check in `src/.../compat/DisplayDelightDishes.java:48`, plus the loot table under
  the corn_delight pack split.
- `creativedelight/framework #16`: delete the ungated instance copies
  `kubejs/data/creativedelight/worldgen/{configured,placed}_feature/wild/*.json` that the jar's `packs/worldgen/<mod>`
  already owns. Diff each against the jar copy first, and keep only files the jar lacks.
- **Status 2026-11-14 (misc) for #16 and #17:** #16 not deleted: the instance `kubejs/data/creativedelight/worldgen/` is the CD build's split source (`splitWorldgen`), dev-only and never deployed, so deleting it breaks the next CD build. Instead `prep_srv.sh` drops it from scratch servers (as the deploy does); moving the source out of `kubejs/data` is left to the CD owner. #17 done (16 rules, apple left). Queue: SQ-CDW-01 note, new SQ-CDW-03b.
- `creativedelight/framework #17`: add `"requires": "fruitsdelight"` to the 16 Fruits Delight rules in `wild_crops.json`.
  `requires` takes one mod; if a rule ever needs two, extend `_biome_rules.js:164` to accept a list. The hops rule (3.1)
  needs only `vinery`.
- SQ-DCM-02 (no TODO row; add one): `global.has.mod(ns)` (`.../func/has.js:39`) treats a data namespace as a mod id. Five
  installed mods are wrongly skipped:
  - towns_and_towers/t_and_t
  - hopo/hopour
  - unwrecked_ships/unwrecked_ships_plus
  - yuushya/yuushya_modelling
  - cozys-improved-cats/cozys_improved_cats
  Fix: a namespace -> mod id map in has.js, or fall back to "the namespace has registry entries". Used by
  `tag_helper.js:86` and `neoCraftHelper.js:422-433` (C).

### 5.2 Audit for the rest
**Status 2026-11-14 (audit): BUILT and installed 23:30, not booted.** `tools/optional_ref_audit.py` (plain lint, no
dumps). First pass 1258 references in 736 files; fixed 269 files (31 loot tables to `packs/mods/<mod>/` or one-item
sub-tables, 4 dishes `requires`, 238 recipes `forge:mod_loaded`); false positives handled by explicit rules and 7
allowlist rows; deferred 53 java-soft and 124 asset files and 13 pack-level references (TODO `creativesplit/deps #27`).
List: [creative_split.md](creative_split.md) 7.21. SQ-DEP-05 was taken (campfire rain), so the drop-one row is
SQ-DEP-09; reruns SQ-DEP-02, SQ-MIN-03, SQ-CDW-01, SQ-CDW-03b in the queue section "SQ-DEP optional-mod guards".
- New tool `tools/optional_ref_audit.py` (listed in `generators.json` only if it reads dumps; else a plain lint):
  - For every data and asset JSON in our jars' sources and in `kubejs/data` / `kubejs/assets`, collect referenced
    namespaces (ids in `item`, `block`, `tag`, `feature`, `entity`, etc.).
  - Look up the owning mod and the jar's declared mandatory dependencies (mods.toml).
  - Flag every reference to an optional mod that sits outside a guard. The guards are a `packs/<mod>` split, a
    `forge:mod_loaded` condition, `// requires:`, a `global.CD.on`/`has.mod` check, or a Java `isLoaded`.
  - Also flag Java `Registry` lookups of foreign ids outside an `isLoaded` check.
- Fix each flagged file with the same guard kinds. Record the list in [creative_split.md](creative_split.md), which
  already covers jar independence.
- Verify by boot sets, extending the r1114 phase 3 drop-one boots:
  - Rerun SQ-DEP-02, SQ-MIN-03, SQ-CDW-01 and SQ-CDW-03b.
  - New SQ-DEP-05: one drop-one boot per optional mod the audit lists as guarded. Pass = "Done", no datapack, loot or
    registry error naming it.

## 6. Worldgen

### 6.1 Rubber tree (B; TODO `farming/wild-crops #8`; lowest priority, test last)
Status: DONE 2026-11-14 (cd-a). Not proven; SQ-CDW-02 rerun last.
- Suspect (not proven): placement on the `OCEAN_FLOOR` heightmap plus a `would_survive` sapling under the jungle canopy.
- Fix:
  - Use the `MOTION_BLOCKING_NO_LEAVES` heightmap and drop or relax the `would_survive` filter, which Myrtrees' own
    feature does not use (compare at build).
  - Keep the rule's removal of Myrtrees' placement.
- Verify: SQ-CDW-02 rerun last in the run. Pass = rubber logs in a located jungle box.

### 6.2 Wild hops: see 3.1.

### 6.3 Corn maze at 2x (A; TODO `locate #6`; owner 2026-11-14, was 4x)
- **Status 2026-11-14 (misc): BUILT, untested.** The group total is 7233 today (not 7901), so the weight is 850 (400/7233 -> 850/7683); `gen_structure_groups.py --check` refreshed the estimates (1 per 72 -> 52 chunks) offline; also in the rebuilt Creative Worldgen jar. SQ-HH-328 rewritten (three seeds). Changelog under Hearth and Harvest.
- The pack places the maze from `kubejs/generation_scripts/worldgen/structure_groups.json`, in the `remote` group (line
  1011, spacing 24 / separation 12). The maze entry is at 1440-1443 with weight 400 of about 7901.
- Set the weight so its share is 2x: w / (7501 + w) = 2 x 400 / 7901 gives w ≈ 845. The other remote members each lose
  about 5 % of their share. Regenerate with `/cdevgen`.
- Leave `searchTimeoutSeconds` at 120.
- Limit: r1114 needed 424 s and 1681 candidates to reach a maze from spawn at 1x. 2x should roughly halve that, so it
  could still miss the 120 s deadline from some spawns. The rerun records the time.
- Verify: SQ-HH-328 rerun with the shipped 120 s deadline on three seeds; record the locate time and candidates for each.
  Pass = found on at least two of three. If not, the owner picks the next step: a longer deadline or a higher rate.
- Changelog: "Corn mazes are twice as common".

### 6.4 Visceral Heap: all flesh, no netherrack (W Creative Worldgen `:creativeWorldgen:`, output `packs/biomesoplenty/data/minecraft/worldgen/noise_settings/nether.json`; owner 2026-11-14; was TODO `#73` item 8)
- **Status 2026-11-14 (misc): BUILT, Creative Worldgen installed, untested.** Generator run offline. The Heap rule now ends with unconditional flesh and sits at index 2, ahead of vanilla's netherrack band under the roof (otherwise the top 5 layers would stay netherrack). Carver tag in `kubejs/data/creativeworldgen/kubejs/tags/visceral_heap.js` through `global.tags`, with a literal tag id (block_dict.js has no key; Common's owner can add `b.t_nether_carver_replaceables`). SQ-VH-06 rewritten, -06b, -06c added. Changelog: the Visceral Heap bullet rewritten (made entirely of flesh, no Nether ores).
- Today (SQ-VH-06, r1112): flesh covers 62 % of floors and 65 % of ceilings (porous flesh +4 %). Netherrack is 10 % /
  16 %, and Better Fortresses bricks the rest.
  - Cause: the Heap surface rule (`tools/gen_visceral_heap.py`, [plan_visceral_heap.md](plan_visceral_heap.md) step 3)
    only sets the top floor and ceiling layer (`stone_depth` offset 0). Cave walls and everything below stay the
    Nether's default block, netherrack.
- Change the generator so the Heap's rule sequence ends with an unconditional `biomesoplenty:flesh` block rule. Every
  default-block position in the Heap becomes flesh at any depth. The floor/ceiling rules stay first.
  - Porous flesh and BOP's other Heap features (tendons, eyebulbs, flesh balls) still place on top.
  - Regenerate `noise_settings/nether.json` with `/cdevgen`.
- Carvers run after surface rules and carve only `#minecraft:nether_carver_replaceables`. Without flesh in that tag the
  Heap would have no caves.
  - Add `biomesoplenty:flesh` and `porous_flesh` to `#minecraft:nether_carver_replaceables` through `global.tags`.
- Ores: Nether ore features target `#minecraft:base_stone_nether`, which flesh is not in. So quartz, gold, ancient debris,
  magma, blackstone and gravel blobs stop placing inside the Heap. **[default]** Keep it that way: "no netherrack",
  flesh only.
  - The alternative is to add flesh to `base_stone_nether`, which puts ores inside flesh.
- Structures (Better Fortresses, bastions) are not surface; their bricks stay.
- Verify:
  - SQ-VH-06 rewritten: 0 netherrack in a Heap column sample, cave walls included. Floors and ceilings are flesh or
    porous flesh at 95 % or more; the rest is structure blocks.
  - New SQ-VH-06b: caves still carve inside the Heap.
  - New SQ-VH-06c: neighboring Nether biomes are unchanged.
  - Client CQ-VH: the look.
- Changelog: "The Visceral Heap is made entirely of flesh".

## 7. FAILs that had no TODO row (add rows when built)

### 7.1 SQ-AZW-01 (C)

Status: DONE 2026-11-14 (common): `b.sunflower_stem` in `block_dict.js`, the item_dict line gone; TODO `creativecommon/publish #4`; rerun SQ-AZW-01.
- Move `hearthandharvest:sunflower_stem` from `item_dict.js:133` to `block_dict.js` (block only, `HHBlocks.java:56`).
  No script reads `i.sunflower_stem`.

### 7.2 SQ-DCM-02: in 5.1.

### 7.3 SQ-CBW-03 (B farmers member)
Status: DONE 2026-11-14 (cd-a). TODO row `creativedelight/members #59`.
- `FDBackportEvents.java:56` returns early on a cutting board, so Delightful's NORMAL handler places its pie block.
- Fix: on a `CuttingBoardBlock` target, cancel the pie placement (`event.setCanceled(true)`, result PASS) so the board's
  own use runs.
- Verify: SQ-CBW-03 rerun.

### 7.4 SQ-LGM-13 (A tools)
- **Status 2026-11-14 (misc): BUILT, untested.** Whole blocks go in the JSON; regenerated offline (75 ids added, all registered items; the current dumps also moved some cans and dropped stale ids). `seasoning_tags.js` checked: no seasoning lists a family, family tags fine on block items; only its comment changed. TODO `food/types-and-tuning #40`. SQ-LGM-13 row extended.
- `tools/food_families.py:1536` drops rows with `food is None`. Block rows get `food=None` (:552), so every `*/whole`
  family is empty.
- Fix **[default]**: include whole-block ids in the JSON.
- Check `seasoning_tags.js`: every item tag it makes from a family must be wanted on the block item. Exclude whole-block
  families from any tag where that is wrong.
- Rerun the generator with `/cdevgen`.
- Verify: SQ-LGM-13 rerun, plus a diff of the tags it generates.

## 8. Power Grid port (F; next free B number B36 in `PG/PORT_NOTES.md`)

Upstream 0.6.2 source fetched 2026-11-14 (tag `v0.6.2`, commit d681ac986a, Apache-2.0) to
`DecompiledMods/powergrid-0.6.2-source/` (git-ignored; U = its `src/main/java/org/patryk3211/powergrid`). The comparison
below was read from source, with no game run.

### 8.1 Ceiling tile drops (TODO `#79`, SQ-PWG-91): deliberate deviation B36
- **Status 2026-11-14 (agent pwg): built**, B36 in PORT_NOTES.md 25, jar `jar_archive/pwg_gate/backportedPowerGrid-0.6.2-ce-19.2-18.jar` (not in `mods/`); SQ-PWG-91 rewritten, not run.
- Upstream has the same bug: the four tables match the port byte for byte. Upstream datagen is at
  `U/collections/ModdedBlocks.java:618-623` and `:996-1001`: one pool, rolls 1, two entries, so one is picked. No
  `getDrops` or break handler adds the other.
- The owner wants both dropped, so this deviates from upstream on purpose.
  - Split `PG/src/main/resources/data/powergrid/loot_tables/blocks/ceiling_tile_{lamp,connector,junction,solar}.json`
    into two pools (tile; attachment), each rolls 1 with `survives_explosion`.
  - Record it as B36 in PORT_NOTES ("upstream drops one").
- B32's bulb add (`CeilingTileLampBlock.java:96-109`) appends to `super.getDrops`, so it keeps working with no double
  drop.
- Verify: SQ-PWG-91: all four drop tile + attachment by hand; wrench path unchanged.

### 8.2 Wire connector drop (B35, TODO `#78`): deliberate deviation B37
- **Status 2026-11-14 (agent pwg): built**, B37 on `AbstractConnectorBlock` (wire and heavy connector; support = sturdy face center or any Power Grid electric block; placement on an unsupporting face is now refused; cord junction not changed), jar -18; new SQ-PWG-97, not run.
- Upstream is the same: no `canSurvive`/`updateShape` in `U/electricity/wireconnector/` or its `DirectionalElectricBlock`
  parent. Registration at `U/collections/ModdedBlocks.java:240-246` is stone + `pickaxeOnly()` + default loot, and the
  port matches.
- Two symptoms:
  - A connector whose support is removed floats.
  - Stone needs the correct tool, so a hand break drops nothing.
- Fix, owner brief:
  - Add `canSurvive` on the attached face plus `updateShape`, copying upstream's own pattern in
    `U/electricity/battery/PotatoBatteryBlock.java:77-85`. A connector whose support goes breaks and drops itself; its
    wires detach and drop as on a hand break.
  - **[default]** Keep pickaxe-only (it is stone); the floating case was the bug.
- Record it as B37 ("upstream floats").
- Verify: new SQ-PWG-97: remove the support, the connector drops, wires drop their items, the network re-solves.

### 8.3 Open questions, compared with upstream
- **Status 2026-11-14 (agent pwg): done, not run.** `#85` built as B38 (jar -18); `#82` pack script `kubejs/server_scripts/other/power_grid.js` (acid fluid has no `fluid_dict` key: the script falls back to the literal `powergrid:acid` until Creative Common adds `f.acid`); `#83`, `#84`, `#86`, `#87` no code. Rows: SQ-PWG-06, -43, -80, -82 rewritten; new SQ-PWG-80b, -54r, CQ-PWG-66. No changelog bullet (Power Grid not shipped to players; bullets come with stage 5).
- `#83` arm leaves parts loose on the belt: **matches upstream**.
  - Upstream `forge/.../mixin/forge/ArmInteractionPointMixin.java:82-129` falls back to Create's normal belt insert
    whenever no incomplete circuit sits in the center window. That is the case before the circuit arrives and after it
    becomes a board, so the arm drops what it carries.
  - The port copy is identical; B25 only touches the lock scan.
  - Mirror: no change. SQ-PWG-82 becomes PASS-as-upstream (assembly completes; loose parts are upstream behavior). Row
    text rewritten.
- `#84` thyratron conducts at grid -30 V: **matches upstream**.
  - `U/electricity/sim/special/ThyratronWire.java:50-74` checks the grid only while unlit (`strike = max(Vh, Vb - mu*Vg)`).
    Once struck it latches while `I >= holdingCurrent`, like a real thyratron.
  - The saved `LIT` state is restored (`U/circuits/components/ThyratronComponent.java:119`). The port is identical.
  - Mirror: no change. The fault is in the row: SQ-PWG-06 sets the grid to -30 V before the heater and anode come up,
    or breaks the anode current first, then reads.
- `#85` servo speed: **no upstream baseline**.
  - Upstream does not move at all with an unpowered coil (it commits at speed 0). B9 fixed that.
  - B9 also resets the coil to idle (64 ohm) after each move, and commits at the first lazyTick window averaging 1 RPM
    or more, often a partly powered one. Upstream locks speed from a full window (`U/kinetics/servo/ServoBlockEntity.java:85-138`).
  - **[default]** Extend B9 so the move commits only after one full powered averaging window. Upstream's speed formula
    then applies once power is there. Keep the idle reset.
  - Verify: SQ-PWG-43 against -08, with the speed within 20 % of -08.
- `#86` display board `Speed:0.0f`: **not Power Grid**.
  - `SpeedScrollValueBehaviour.java:62` is only a clipboard key. The text is Create's own display board `Speed` tag: the
    board gets no rotation from the motor and shaft.
  - Mirror: no Power Grid change. CQ-PWG-66 checks the Create 0.5.1 drive by hand (motor -> shaft -> board). If Create
    needs a different hookup, the row text changes.
- `#87` solar contraption after restart: **matches upstream**.
  - Re-attachment is not stored in NBT. `movedContraption` is set again when the contraption entity ticks and calls
    `attach` (`U/electricity/solarpanel/SolarPanelBearingBlockEntity.java:448-478`). The port only changed vector types.
  - Mirror: no change. New SQ-PWG-54r: restart, wait 100 ticks in an entity-ticking chunk, then read the known-good
    X 1340 contraption.
- `#82` acid vs heated salt mixing (owner, 2026-11-14): the acid recipe also takes rotten flesh.
  - Upstream `PG/src/main/resources/data/powergrid/recipes/mixing/acid.json` is heated: redstone + blaze powder + 250 mB
    water -> 250 mB acid.
  - Make it a pack change, not a port change: a new server script guarded on `powergrid` removes
    `powergrid:mixing/acid` and re-adds it through `R.createMixing(..., 'heated')` (`neoCraftHelper.js:2104`), with
    `minecraft:rotten_flesh` (dict key) as a fourth input. The port jar stays as upstream.
  - Limit: water poured into a heated basin before any item still becomes salt, because the salt recipe needs only water.
    Rotten flesh separates the recipes in JEI and in item-first order. It does not stop water-first.
  - Verify: SQ-PWG-80 rewritten (acid needs the flesh; items first, then water, gives acid). New SQ-PWG-80b: water first,
    to record what happens.
  - Changelog: under Power Grid when it ships.
- Every change gets a B entry and a gate jar in `jar_archive/pwg_gate/`, never `mods/` (note [power-grid-port](agent_notes/power-grid-port.md)).

## 9. Order and tests

1. B (Creative Delight): sections 1, 3 (except 3.2's dict part), 4.3, 4.6, 5.1 (CD parts), 6.1, 7.3. One build, install
   at a stop.
2. C (Creative Common): 3.2 dict, 3.8 melting system, 5.1 has.js, 7.1. Rebuild before B is booted, since B's scripts
   read the dicts and the salt member's melt switch-off assumes Common's system.
3. G (Creative Brewing): 3.1 hops `GrapeType` and lattice, built and installed with B (hops ids come from B).
4. D (Compat): 4.1. E (Equipment): 4.5 comment. W (Worldgen): 6.4. A: 2, 4.2, 4.4, 6.3, 7.4, 5.1 instance files, 3.10 scripts.
5. F (Power Grid): 8.1 (B36), 8.2 (B37), 8.3 servo (B9 extension); the other 8.3 items are row rewrites only.
6. 5.2 audit after 1 to 4, then its fixes.
7. Scratch queue (skill `scratch-test`; rows written in the build session, run only when the owner says): every row named
   above. Rubber tree SQ-CDW-02 goes last.

Scratch run r1115 (2026-11-15, [scratch_run_2026-11-15.md](testing/scratch_run_2026-11-15.md) 8; findings routing in section 9 there): sections 1 (SQ-SPD-01 to -07, SQ-HH-1574, -1575; SQ-SPD-08 Nether pouch fixed by fix-2, rerun -08b PASS), 2 (SQ-TPF-00, -01), 3.2 (SQ-HH-1546 to -1546c), 3.3 to 3.6, 3.7 (SQ-SLK-02 to -04; SQ-SLK-01 reach fixed by fix-2, -01c PASS), 3.10 (SQ-HH-266 to -266c), 4.2, 4.3, 4.4, 4.5, 4.6 (SQ-BBQW-02s fixed by fix-2: CONSUME, -02t PASS), 5 (SQ-DEP, 7.21 of creative_split.md), 6.3 (SQ-HH-328), 6.4 (SQ-VH-06 to -06c), 7.1 to 7.4, 8 PASS. FAIL for the owner: 3.1 hops (SQ-HOP-04 stick on an age-1 vine, -05 Compat shearing (pass-through session), -06 no wild hops), 3.8 (SQ-MELT-08 no per-click salt melt, -10 fire-tag lantern), 4.1 (SQ-PG-01c client crash at a Flint and Terrasteel portal, URGENT; SQ-PG-01b stuck in the ground after floor-to-wall), 8 (SQ-PWG-82).

## 10. Open items for the owner (breakdown 2026-11-14, owner answers the same day)

### 10.1 Decisions
Owner 2026-11-14: every default in this plan is accepted unless changed below.
- Changed by the owner:
  - corn maze 2x (6.3);
  - lanterns count as fire, with the modded list in 3.8;
  - end stone tills only with a netherite or better hoe (3.2);
  - crow foods defined (3.5);
  - salt layers refill a licked block (3.7);
  - the stomping basin takes valid items only and mirrors Vinery stomping (3.10);
  - the Visceral Heap is all flesh (6.4);
  - Power Grid source fetched (8).
- Defaults added in this round, accepted unless changed:
  - jack o'lanterns count as fire; Quark duskbound lantern counts; candles do not (3.8);
  - a waxed lick stays waxed when refilled; one layer restores one stage (3.7);
  - no ores inside the Heap (6.4);
  - wire connectors stay pickaxe-only (8.2);
  - the servo commits after a full powered window (8.3).

### 10.2 Partial fixes and known limits
- **Debugify:** one fix only works part of the time, and three areas are unsettled:
  - MC-206922 (lightning destroys items) is partial: Debugify spares only items younger than 8 ticks. It stays on.
  - MC-2025 and MC-224729 could not be reproduced and stay off.
  - The client fixes (CQ-TPF-31 to -91) are untested.
  - The hand-written file may be rewritten on first load; that boot checks it.
- **Acid vs salt mixing:** water poured into a heated basin before any item still turns to salt.
- **Corn maze at 2x** may still miss the 120 s locate deadline from some spawns (6.3); the rerun records the times.
- **Power Grid:**
  - 8.1 and 8.2 deviate from upstream on purpose (B36, B37).
  - Arm loose parts, thyratron latching and solar re-attach are upstream behavior, kept.
  - The display board reading is a Create hookup question, settled by hand (CQ-PWG-66).
- **Rubber tree:** the cause is a suspicion, not proven, and may need a second pass.
- **Build-time checks:**
  - the Vinery lattice blockstate layout for the hops render hook;
  - other `getBottle()` callers;
  - Ender Crop's tilling path, event or tool modification;
  - any Vinery recipe-based stomping in the donor data;
  - the light level of every fire-tag block;
  - the Nether ice melt path seen in r1113;
  - Quark's and our seed pouch pickup handlers.
- **Syrup to sugar:** the heated recipe is a later step; the owner picks the yield (`#72b`).

### 10.3 Open from the runs but not in this plan
- **URGENT:** with 420 or more jars in `mods/`, the client dies at window creation (TODO `pack/server #39`).
- TODO `#73` items still open:
  - (3) The crow nest is not Clearable, so `/fill` spills it.
  - (4) A watering can on a ripe crop harvests it.
  - (5) Crow tree nest rate.
  - (6) The salt caves biome read.
  - Items 1, 2, 7 and 8 are answered by 3.10, 3.1, 3.7 and 6.4.
- **INCONCLUSIVE rows:** SQ-HH-1534 (single till; rerun in 3.7), SQ-FCK-08b, SQ-RD-10, SQ-WO-34 (raise
  `fs.inotify.max_user_instances`).
- **Stale row:** SQ-PST-12; the owner confirms the jungle grass numbers.
- **Owner hand steps:** CQ-PWG-22 to -29.
