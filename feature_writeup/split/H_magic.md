# Sweep H: Creative Magic

Read-only sweep for [creative_split.md](../creative_split.md) 7.6, written 2026-11-01. Magic (`creativemagic`) is carved in
this split. Starting point: [creative_magic.md](../creative_magic.md), the 7.6 owner rows (Dark Fire, Flint and
Terrasteel, `dimension/PortalFailureHandler`, `[botania]`, `TrinketCaseRules`), sweeps A to G, then a fresh search of
patcherMod Java and `creativeengineering.mixins.json`, `CEConfig`, `kubejs/startup_scripts`, `server_scripts`,
`client_scripts`, `kubejs/data`, `kubejs/assets` and the installed jars. Line numbers were read from the working tree on
2026-11-01 and can move while Devtools and Calendar are carved.

Target words follow [README.md](README.md). Confidence: H = clearly Magic, M = Magic by owner row or by cohesion, L = see
the doubtful table (section 8). Only H and M rows are counted in section 9.

Decisions this sweep rests on:

- 7.6 puts `fan/` dragon breathing wholly in Engineering. `magic/dragon_breathing.js` (25 lines) therefore leaves the
  Magic list in creative_magic.md and goes with the `fan/` type (section 8, row D1).
- Potions, effects, gear effects, Magic Resistance and lava vision stay CB (section 7).
- Easy Magic, Supplementaries and Quark enchanting settings are third-party config in the pack, not jar content.

## 1. Java (patcherMod)

Root is `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/`.

| Path | Lines | Why Magic | Planned target now | Cross-jar edges | Conf |
|---|---|---|---|---|---|
| `block/DarkFireBlock.java` | 115 | Dark Fire block, the one flame that opens a Nether portal | Engineering (A: `block/`), Worldgen ? (E) | Reads vanilla `PortalShape`, fires `PortalSpawnEvent`; called by `FlintAndTerraSteelItem` and `PortalFailureHandler` (both Magic). No Common or content calls | H |
| `block/CreativeEngineeringBlocks.java` | 28 | Registers only `DARK_FIRE` | Engineering | Writes to `CreativeEngineering.BLOCKS` (Common holder, `CreativeEngineering.java` 65-66); only reader of that holder. Magic needs its own `DeferredRegister<Block>` keeping namespace `creativeengineering` (rule 3.1) | H |
| `item/FlintAndTerraSteelItem.java` | 56 | Places Dark Fire; headed TEMPORARY | Engineering (A), Magic (Equipment) (creative_magic.md) | Reads `CreativeEngineeringBlocks.DARK_FIRE` (Magic). Recipe is `magic/progression.js`; id in Common `item_dict.js` | H |
| `item/CreativeEngineeringItems.java` | 30-31 of 40 | Registers `flint_and_terrasteel`; lines 1-29 (sawblade, sawdust) stay Engineering | Engineering | Mixed holder: split into an Engineering holder and a Magic holder, both namespace `creativeengineering`. Register call at `CreativeEngineering.java` 81 | H |
| `dimension/PortalFailureHandler.java` | 181 | Ordinary Nether portal ignition fails, frame becomes crying obsidian, Dark Fire succeeds | Worldgen (A), Magic (owner 7.6) | Reads `DARK_FIRE` (now same jar: the Worldgen to Engineering edge in A section 4 disappears). `@Mod.EventBusSubscriber(modid = "creativeengineering")` must change to the Magic mod id. Vanilla and Forge classes only; no `CEConfig` read | H |
| `compat/TrinketCaseRules.java` | 29 | Botania Trinket Case accepts any item | Compat (A, C) | Reads `CEConfig.BOTANIA` (moves with it); read by the three `TrinketCase*Mixin`; `ItemBackedInventoryAccessor` | M (owner 7.6) |
| `CreativeEngineering.java` rows 7, 65-66, 81-82 | 5 | Import of `CreativeEngineeringBlocks`, the `BLOCKS` holder and its two register calls | Common (D) | Edit only: the holder and the two `register` calls move to Magic's main class; the `items.CreativeEngineeringItems` import stays for the Engineering half | M |

Subtotal: 7 rows, 416 lines (the registration row is an edit, not a class).

## 2. Mixins (`creativeengineering.mixins.json`)

| Entry (java line count) | json line | Why Magic | Planned target now | Cross-jar edges | Conf |
|---|---|---|---|---|---|
| `CycleEnchantmentMixin` (29) | 43 | Makes NEA Cycle undiscoverable, untradeable, off tables and books | Magic (Equipment) | Hard `nock_enough_arrows` (third-party; Compat gate). No pack calls. Client twin: `jeichanges.js` 25-56 (section 5) | H |
| `StockpileEnchantmentMixin` (29) | 44 | same, Stockpile | Magic (Equipment) | same | H |
| `RecoveryEnchantmentMixin` (29) | 57 | same, Recovery | Magic (Equipment) | same | H |
| `TrinketCaseInventoryMixin` (23) | 200 | Botania Trinket Case inventory accepts any item | Compat | Hard `botania`; calls `TrinketCaseRules` and `ItemBackedInventoryAccessor` | M |
| `TrinketCaseSlotMixin` (20) | 201 | Trinket Case slot rule | Compat | same | M |
| `TrinketCaseMenuMixin` (30) | 202 | Trinket Case shift-click rule | Compat | same | M |
| `ItemBackedInventoryAccessor` (13) | 203 | Accessor Botania's `ItemBackedInventory.stack` for the open-case check | Compat | Hard `botania`; read by `TrinketCaseRules` | M |

Subtotal: 7 rows, 173 lines. The enchantment mixins target NEA, an archery mod. Archery itself stays Equipment
(`NeaWhistleGoalSkipMixin`, `NockRecipeBookSync`); only the three enchantment rows are Magic.

## 3. Config sections (`CEConfig.java`, `config/creativeengineering-common.toml`)

| Section | Lines | Why Magic | Planned target now | Cross-jar edges | Conf |
|---|---|---|---|---|---|
| `[botania]` (`Botania` class 519-536, field line 44, init line 97) | 18 + 2 | One key `trinketCaseAnyItem` for the Trinket Case | Compat? (C) | Readers: `compat/TrinketCaseRules` only. Moves to a new `creativemagic-common.toml`; the key leaves `creativeengineering-common.toml` line 300 and `configs_edited/root_config/` (players lose the old value once; default true). New spec needs the `COMMON_SPEC` guard of 7.3 step 0 | M (owner 7.6) |

Dark Fire, Flint and Terrasteel and the portal rule have no config keys (nether_portal.md "Knobs: none").
`[armor_enchantability]` is doubtful (section 8, row D2).

## 4. Server scripts (`kubejs/server_scripts/`)

| Path (rows) | Lines | Why Magic | Planned target now | Cross-jar edges | Conf |
|---|---|---|---|---|---|
| `magic/xp.js` | 23 | Create Enchantment Industry disenchanter, rotor and printer on experience blocks; removes Compressed Creativity memory essence | Magic (Equipment) | `global.NCH`, `global.item` (Common). Items are third-party | H |
| `magic/sculk.js` | 17 | Sculk doubled by mixing with liquid or hyper XP | Magic (Equipment) | NCH, `global.fluid`. Sculk feeds echo shards in `engineering/crystal.js` 40-45 (Engineering reads vanilla `sculk`, not a Magic item) | H |
| `magic/storage.js` | 42 | Ender chest behind a Botania third eye; Cloud Storage and Ender Storage recipes | Magic (Equipment) | NCH incl. `NCH.enderStorage` (already a helper). Uses IE `hemp_fabric` (third-party) | H |
| `magic/progression.js` | 14 | Flint and Terrasteel recipe; Nature's Compass needs terrasteel; Explorer's Compass removed | Magic (Equipment) | `i.flint_and_terrasteel` (Magic item, id in Common dict) and `i.terrasteel_ingot` (Botania) | H |
| `magic/enchants.js` | 13 | MoreJS filters drop the three NEA enchantments from tables and librarian books | Magic (Equipment) | MoreJS (third-party). Six raw calls; MoreJS events are not recipes, so NCH does not cover them | H |
| `tags/fluidtags.js` 79-80 | 2 | `forge:fluid/xp` and `forge:fluid/hyper_xp` | CB (F: lines 75-84 "magic/potion fluids") | `global.tags` (Common), `global.fluid`. F assigned the whole 75-84 block to CB, but these two lines are XP, not potion | M (new) |
| `tags/fluidtags.js` 86-88 | 3 | XP fluid merge: Create's Liquid Experience is the only `#forge:experience` | Magic (Equipment) | same. F wrote 86-90; 89-90 are blank in the current file | H |

Subtotal: 7 rows, 114 lines (109 in `magic/`, 5 in `fluidtags.js`). creative_magic.md says 146 lines for the `magic/`
rows; the five files (not counting dragon breathing) are 109, with dragon breathing 134.

## 5. Client scripts (`kubejs/client_scripts/`)

| Path (rows) | Lines | Why Magic | Planned target now | Cross-jar edges | Conf |
|---|---|---|---|---|---|
| `tooltips.js` 808-814 | 7 | Botania section: vine ball, Trinket Case tooltip ("Holds any item; only trinkets equip from it") | Magic (Equipment) | `addTooltips` helper from the same file (Common): Magic client script needs the helper through a Common global | H |
| `jeichanges.js` 25-56 | 32 | Red "REMOVED ITEM" tooltip on enchanted books holding the three NEA enchantments; the client half of `enchants.js` and the three mixins | Common (F: whole file) | Reads nothing from Common (only `unwantedEnchants` literal). Lines 1-23 and 58 stay Common | M (new) |
| `namechanges.js` 29, 31 | 2 | Lang names for `flint_and_terrasteel` and `dark_fire` | Common (F rows 62-227) | Lang only; `e.add` keys keep the `creativeengineering` namespace | H (new) |

Subtotal: 3 rows, 41 lines.

## 6. Resources

Pack resources live in `development/patcherMod/src/main/resources/assets/creativeengineering/` (PM) and
`kubejs/assets/creativeengineering/` (KJ).

| Path | Files / lines | Why Magic | Planned target now | Cross-jar edges | Conf |
|---|---|---|---|---|---|
| PM `blockstates/dark_fire.json` | 1 / 14 | Dark Fire blockstate | Worldgen ? (E) | Block id stays `creativeengineering:dark_fire` | H |
| PM `models/block/dark_fire_0.json`, `dark_fire_1.json` | 2 / 16 | Dark Fire models | Worldgen ? (E) | none | H |
| PM `models/item/flint_and_terrasteel.json` | 1 / 6 | Item model | Worldgen ? (E) | none | H |
| PM `textures/block/dark_fire_0.png`, `dark_fire_1.png` and two `.mcmeta` | 4 | Dark Fire textures (animated) | Worldgen ? (E) | none | H |
| PM `textures/item/flint_and_terrasteel.png` | 1 | Item texture | Worldgen ? (E) | none | H |
| KJ `textures/block/dark_fire_0.png`, `dark_fire_1.png` and two `.mcmeta` | 4 | Duplicates of the PM textures (E row 204: one copy should go) | Worldgen ? | Delete the KJ copy at the carve; the Magic jar ships the PM files | M |
| Docs: `nether_portal.md`, `enchant_power.md` (98), `recipes_magic.md` (112), `briefs/recipes_magic.md`, `briefs/potions_enchanting.md`; tests RA-01 to RA-08, NP-01 to NP-12, EP-01 to EP-04 | 5 docs | Writeups and test ids for the rows above. `recipes_magic.md` keeps its pointer lines to `armor_effects.md` and `potions.md` (CB) | feature_writeup | `archery.md` keeps its NEA enchantment paragraph (lines 12, 117-123) and links to Magic; `briefs/gameplay_tnt-and-portal.md` rows #5 and #6 (Flint and Terrasteel, Dark Fire in any dimension) belong to Magic | M |

Subtotal: 6 resource rows (21 files in PM and KJ) plus the docs row.

Nothing for Magic under `kubejs/data` (the `ancient_enchantment_temple` data is a Worldgen structure, the Terralith
`enchanted` features are Worldgen) and nothing under `kubejs/startup_scripts` (the only `startup_scripts/magic/` file is
`magic_resistance.js`, CB). `kubejs_startup/creativemagic/` would be empty at the carve; do not create it until needed.

## 7. CB, not Magic (listed so nobody claims them)

| Row | Why it looks magic | Owner |
|---|---|---|
| `server_scripts/magic/armor_effects.js` (108), `tool_effects.js` (75), `lava_vision.js` (27) | folder is named `magic/` | CB (creative_brewing.md 4.2) |
| `startup_scripts/magic/magic_resistance.js` (33), `potion/MagicResistanceEffect` (122), `[magic_resistance]`, `textures/mob_effect/magic_resistance.png` | Magic Resistance effect | CB |
| `startup_scripts/func/armor_effects.js` (269), `func/tool_effects.js`, `client_scripts/armor_effects_tooltip.js`, `tool_effects_tooltip.js` | gear effects | CB |
| `server_scripts/other/jeed_providers.js` (Botania brews 133-216) | Botania brew effect descriptions | CB |
| `engineering/potion_bottling.js`, `PotionRows`, Potion* mixins, `PotionPagesJEI` | potions | CB / Common |
| `kubejs/assets/.../lang` `effect.botania.*.description` (3 keys) | Botania brew text | CB |
| `damage/DamageTags` | magic damage groups | Equipment (Common API) |
| `shield/ShieldGroup` (physical/elemental/magic) | "magic" damage group | Equipment |

## 8. Doubtful rows (owner decision)

| Id | Row | Lines | Looks magic because | Current target | Lean | Reason for the lean |
|---|---|---|---|---|---|---|
| D1 | `magic/dragon_breathing.js` | 25 | Sits in `magic/`, dragon breath | creative_magic.md: Magic; 7.6: Engineering (`fan/` dragon breathing "all Engineering") | Engineering | 7.6 is explicit and uses `NCH.dragonBreathing`, the `fan/` type; remove from the Magic list |
| D2 | `util/ArmorEnchantabilityBridge` (31), `mixin/ArmorMaterialEnchantabilityMixin` (48), `[armor_enchantability]` (CEConfig 537-567) | 79 + 31 | Enchantability | Equipment; 7.6 "unless the Magic sweep claims the bridge" | Equipment | Only two rows (echo 18, felsic 21); it is the Java half of `tune_armor.js` and `tune_tools.js` enchantability (Equipment). Splitting gear stats across jars costs more than it buys |
| D3 | `fluid/FluidBridge` (93) plus `XpHelperMixin` (27), `XpPumpUpgradeWrapperMixin` (43), `XPFluidManagerMixin` (43), `MemoryStickItemMixin` (106), `PneumaticCraftUtilsMixin` (47) | 359 | One feature: liquid XP merged onto Create Enchantment Industry's fluid; the tag merge (`fluidtags.js` 86-88) and `xp.js` already go to Magic | Engineering (B, A) | Magic | Same feature as three Magic rows; leaving it splits the XP merge over two jars. Cost: hard deps `pneumaticcraft`, `sophisticatedcore` become Compat-gated mixins in Magic; no content-jar edge. Otherwise Compat (third-party mixins only), never Engineering |
| D4 | `BonemealCharmMixin` (71), `util/BonemealCharms` (25), `[bonemeal_charm]` (627-659) | 96 + 33 | Charm, generalised Winemaker trick (B notes "could be Magic") | Equipment ? | Equipment | A held tool effect for the dragon hoe; no magic system behind it |
| D5 | `fabric.js` 59-62, 77-80, 96-97 (Botania picture-frame, holy cloak, invisibility cloak, flower bag wool swaps) | 8 | Botania recipes | Engineering (IE fabric) | Engineering | They exist to accept hemp fabric in a wool sweep; the owner of the wool sweep is Engineering |
| D6 | `resource_blocks.js` 106-108, 238-239 (remove Botania mana infusion granite and calcite recipes) | 5 | Botania recipes | Engineering | Engineering | Stone production chain; the removals serve Create recipes |
| D7 | `metal_base_materials.js` `xp_nugget` ore outputs (lines 261-310 area) | ~25 | XP | Engineering | Engineering | Ore processing byproduct, not magic |
| D8 | `tooltips.js` 417-418 (book power), 735 (crystal pedestal power), 397 and 719 (Stasis) | 4 | Enchanting power text | Common / Compat | Leave, move to Magic only when [enchant_power.md](../enchant_power.md) is built | Today they describe Supplementaries and vanilla books |
| D9 | `blocktags.js` Botania lines (61, 179-182, 226, 255, 267, 277, 286, 310, 363), `curiostags.js` Botania masks, `wild_flowers.js` Botania colours, `jei/order.js` Botania list, `item_dict.js` Botania ids | many | Botania | Common / Compat / Worldgen | Leave | Data about third-party blocks inside shared tag, dict and order files |
| D10 | `development/patcherMod/src/main/resources/working/` (`FluidMemoryEssence`, `MemoryStickItem`, `xpfluidbridge/`, `xpfluidbridge.zip`) | 4 items | Parked XP code (E line 123 "?") | ? | Follows D3 | If D3 goes to Magic these are its scratch; otherwise delete |
| D11 | Future enchant-power system (enchant_power.md section 5: coremod patch of `IForgeBlockState.getEnchantPowerBonus`, tag family `enchant_power/*`) | not built | Enchanting | not assigned | Magic for the tags and values; the coremod patch lives in Common (the patcherMod coremod is Common) | Needs a Common transformer hook that Magic fills with its tag lookup; flag as the only planned edge into Common beyond the current APIs |

## 9. Totals (H and M rows only, no doubtful rows)

| Kind | Rows | Lines |
|---|---|---|
| Java (including the registration edit) | 7 | 416 |
| Mixins | 7 | 173 |
| Config sections | 1 | 20 |
| Server scripts | 7 | 114 |
| Client scripts | 3 | 41 |
| Resources (JSON and PNG, PM and KJ) | 6 | 21 files / ~46 JSON lines |
| **Total code and script** | **25** | **764** |

If D3 is accepted, add 7 rows (6 classes) and 359 lines.

Rows new compared with creative_magic.md: `DarkFireBlock`, `CreativeEngineeringBlocks`, the `CreativeEngineeringItems`
half, `PortalFailureHandler`, the `CreativeEngineering.java` registration rows, `TrinketCaseRules` and its 4 mixins
(listed there as "Open"), `[botania]` (Open), `fluidtags.js` 79-80, `jeichanges.js` 25-56, `namechanges.js` 29 and 31,
the Dark Fire and Flint and Terrasteel assets, `nether_portal.md` and tests NP-01 to NP-12. Row removed:
`dragon_breathing.js` (D1).

## 10. Edges that bear on the dependency rule

- Magic reads Common (`global.NCH`, `global.item`, `global.fluid`, `global.tags`, `addTooltips`) and Compat (gated
  mixins on `botania` and `nock_enough_arrows`). No row calls Calendar, CD, CB, Worldgen, Equipment or Engineering.
- Nothing in another jar reads a Magic class. The only outside references to Magic content are strings: the id in
  Common's `item_dict.js` (`i.flint_and_terrasteel`), the lang keys in `namechanges.js`, and the generated
  `generation_scripts/physics/item_physics_families.json`.
- The one structural change: Dark Fire and Flint and Terrasteel register into `CreativeEngineering.BLOCKS` and the
  shared `CreativeEngineeringItems` holder (Common/Engineering today). Magic needs its own `DeferredRegister`s with
  namespace `creativeengineering`; ids stay (rule 3.1).
- `PortalFailureHandler` leaves the Worldgen to Engineering edge listed in A section 4; with Magic it is a same-jar
  read.
- D11 (future enchant power) would need a Common hook; D3 would need Compat gating only.

## 11. Installed magic and enchanting mods

From `mods/` (418 jars). Pack tweak means a script, mixin, config edit or recipe change in this repo.

With pack tweaks (already covered above): Botania 440.2, Create Enchantment Industry 1.2.9.e, Easy Magic 4.3.3
(`configs_edited/root_config/easymagic-server.toml`; enchant_power.md audit only), EnderStorage 2.10.1 (recipes,
`EnderStorage.cfg`), Cloud Storage 1.3.4 (recipes), Nock Enough Arrows 1.2.0 (enchantments, recipes, config; archery is
Equipment), Allurement 3.2.1 (hand-edited `allurement-common.toml`, no scripts), Sculk Transporting 1.1 (sculk
recipes use vanilla sculk only), Floral Enchantment 1.4.3 (large pot hidden in `other/flowers.js`), Supplementaries and
Quark enchanting settings (third-party config), Dragonloot 1.1.15 (Equipment, dragon gear).

No pack tweaks yet: KubeJS Botania 1.3.1 (no `BotaniaEvents` use), Applied Botanics 1.4.6, Patchouli 77 (Botania book; only
`PatchouliBookTooltipMixin`, Common), XP Tome 2.1.7 (briefs `potions_enchanting #2` planned), Mystical Oak Tree 1.10
(Wise Oak gives enchant power 15; enchant_power.md F1 proposes 4), Neko's Enchanted Books 2.0.3 (client), Enchantment
Descriptions 13.0.20 (client), Fortune on Netherite 1.1.0, Witch Mobility 1.3.2, Golems Are Friends Not Fodder 0.1.0.1,
End Portal 1.2, NetherPortalFix 10.0.1, Smaller Nether Portals 3.6 (shapes the portal sizes that `PortalFailureHandler`
checks; no config edit), Better Beacons / Just Enough Beacons, Just Enough Sculk Sensor, Charm of Undying 6.2.1.
