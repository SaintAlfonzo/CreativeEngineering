# Pack art: which mods' block and item textures may be used

Audit 2026-11-07 (resource packs added the same day). Question: which installed mods' textures can go into pack art (CurseForge / Modrinth banner,
logo, gallery images, screenshots composed from block and item renders).

Sources: the `license` field in each jar's `META-INF/mods.toml` (all 414 jars in `mods/`, plus the nested Creative
Delight members), the licence files inside the jars, `DecompiledMods/create-1.19.2-0.5.1.i/LICENSE_create-1.19.2`,
and the upstream repositories' LICENSE files fetched 2026-11-07. A `mods.toml` field is the author's one-line claim;
it often does not say what covers the art. **Anything marked "verify" has not been checked against the upstream
repository.** This is a licence reading, not legal advice.

## 1. Verdicts for the three families asked about

| Mod (installed version) | Licence | Art usable for pack art? | Conditions |
|---|---|---|---|
| Create 0.5.1.i (1.19.2) | MIT, whole jar; the LICENSE shipped in the jar has no assets carve-out | **Yes** | Keep the MIT notice and credit "Create, simibubi / The Creators of Create". See the warning below. |
| Immersive Engineering 9.2.4-170 | "Blu's License of Common Sense" (custom) | **Not without permission** | The licence says always ask before copying assets. Only addon mods may reuse and remix art, and only to be addons. A pack banner is not an addon. Ask Blu (BluSunrize) first; credit the artists regardless. |
| Farmer's Delight 1.2.4 (nested in Creative Delight) | MIT | **Yes** | Keep the MIT notice, credit vectorwing. |

Create warning: the 1.21.1 branch of Create (`DecompiledMods/create-1.21.1-6.0.10/LICENSE.md_create-1.21.1`) splits the
licence: `assets/` becomes **All Rights Reserved**, everything else MIT. The 1.19.2 release used here is plain MIT, so
art taken from the 1.19.2 jar is covered, but do not take art from a newer Create jar or repository branch, and do not
read the 1.19.2 grant as carrying forward. Some Create textures may also be third-party (not checked per file).

Immersive Engineering note: its textures include some derived from Engineer's Decor (MIT, stfwi), which has its own
MIT grant (Engineer's Decor is also installed, `engineersdecor`, MIT). Do not use that as a way around IE's terms for
the rest.

### 1.1 Vinery, Brewin' and Serene Seasons (calendar item and other art)

Asked 2026-11-07: which of these may supply art, in particular a calendar item.

| Mod | Licence | Art usable? | Calendar-type art | Notes |
|---|---|---|---|---|
| Brewin' And Chewin' 4.5.0 (Creative Brewing member, `members/brewin`) | MIT upstream (Umpaz, Probleyes, MerchantCalico); port is GPLv3 | **Yes** with the MIT notice in `members/brewin/NOTICE-upstream-MIT.txt` and credit | None. The only book-like texture is the keg recipe book GUI. | One texture, `fd_tray.png`, is Farmer's Delight (MIT). The upstream NeoForge milk textures (LGPL 2.1) are not in the port. |
| Vinery (Let's Do Vinery 1.5.5 and 1.3.10, Creative Brewing member) | Code MIT, everything under `src/main/resources/` All Rights Reserved | **No** without permission from satisfy / Cristelknight | None found | The port ships no upstream textures; they are read at runtime from `vinery_upstream/`, so nothing of Vinery's art is in the repo to reuse. |
| Serene Seasons 8.1.0.24 (Glitchfiend) | All Rights Reserved (`mods.toml`; the GitHub page shows "All rights reserved", no licence grant, no assets carve-out) | **No** without permission from Glitchfiend | The calendar item: `assets/sereneseasons/textures/item/calendar_00` to `calendar_11`, `calendar_null` and `tropical_calendar_*` | The only calendar art among the three, and not usable. |

Result for the calendar item: there is no usable calendar texture in these three. Options: draw an original one (Creative
Calendar is the pack's own mod, so the art is the owner's GPLv3), or ask Glitchfiend for written permission to reuse
Serene Seasons' calendar sprites.

## 2. Delight mods (top-level jars and Creative Delight members)

Creative Delight's own code is GPLv3 (own work, free to use), but the members are repacks and ports of other authors'
mods. The "GPL-3.0-only" in a member's `mods.toml` is the pack's licence on its own changes, not the upstream
author's licence on the art. The upstream column is what governs textures.

### 2.1 Usable with credit and the licence notice

| Mod | Upstream licence | Notes |
|---|---|---|
| Farmer's Delight 1.2.4 | MIT | |
| Ocean's Delight | MIT | |
| End's Delight 2.1 | MIT | |
| Nether's Delight 3.1 | MIT | |
| Delightful 3.4.1 | MIT | |
| Hearth and Harvest 1.4.1 | MIT (assets) | |
| Salt 1.2.4.1, Create: The Salt 1.1.0 | MIT | |
| Farmer's Respite 2.0 | MIT | |
| Brewin' And Chewin' 4.5.0 (Creative Brewing member) | MIT | checked against upstream LICENSE per the writeups |
| Alex's Delight, Chef's Delight, Corn Delight, More Delight, Storage Delight | MIT | `mods.toml` only |
| Engineer's Delight (`tmted`) | MPL-2.0 | file-level copyleft on source; art is fine with credit, verify if you alter it |
| Hostile Delight, Canned Goods 3, Create Confectionery, Create Misc and Things | Academic Free License 3.0 | permissive, needs attribution and the licence text |

### 2.2 Usable only under copyleft terms

| Mod | Licence | Notes |
|---|---|---|
| Barbeque's Delight 1.3.0 | LGPL-2.1 | credit and keep licence notice; derivative art inherits LGPL conditions in the strict reading |
| Display Delight 1.7.0 | LGPLv3 | its tray textures are Farmer's Delight's (MIT) |
| Ports with no upstream licence recorded here: Oceanic Delight, Veggies Delight, Vintage Delight, Fungi Delight, Large Meals | jar says GPL-3.0-only (the pack's own) | **verify** the upstream licence before use |

### 2.3 Do not use without permission (All Rights Reserved or unconfirmed)

| Mod | Licence | Notes |
|---|---|---|
| Fruits Delight 0.5.11 | All rights reserved | |
| Ender's Delight 1.19-1.2.2 (the standalone jar) | All rights reserved | the 1.1.4 content the pack carries has its own MIT notice (see `feature_writeup/ends_delight.md`); use only those |
| Quark Delight | All Rights Reserved | |
| Golden Food 2.0.0 | All rights reserved | |
| Roll Delight 1.3.1 | ARR | |
| Miner's Delight 1.4.5 / 1.1.2 | ARR | |
| Rustic Delight 1.7.0 | not confirmed | treat as ARR until the upstream file is read |
| Vinery (Creative Brewing member) | code MIT, assets and data All Rights Reserved | |
| Abnormals Delight and other Team Abnormals mods (Neapolitan, Buzzier Bees, Berry Good, Clayworks, Incubation, Upgrade Aquatic, Allurement, Boatload, Blueprint) | Abnormals licence (custom, restrictive) | do not use; Incubation also reads NC-style |
| Farmer's Structures | All Rights Reserved | |

### 2.4 Pack's own art

Textures made for this pack (Creative Engineering, Common, Equipment, Magic, Labels, Worldgen, the Delight and
Brewing bundles' own items) are the owner's, GPLv3, free to use. Exceptions are the recolours credited in
`development/*/NOTICE-thirdparty.txt` (for example the powdered sugar block, recoloured from Immersive Weathering,
LGPLv3), which keep their source's terms. Check the notice file before using a recoloured texture.

## 3. Resource packs in `resourcepacks/`

Checked 2026-11-07 against each pack's CurseForge page (licence field and description). The zips carry no licence
files, so the page is the only source. "Enabled" is from `options.txt`.

| Pack | Author | Licence | Enabled | Art usable for pack art? |
|---|---|---|---|---|
| Engineers GUIs 1.18.2 v.01.0 | schaeferd_cz (contributor Pabilo8, from the Immersive Intelligence team) | MIT | yes (flagged incompatible) | **Yes** with the MIT notice and credit. It restyles vanilla, JEI and JER GUIs, so the art is mostly its own, but it imitates IE's look; confirm no IE texture is copied in before using a close-up. |
| Updated Engineering v1.7 | tdstress | All Rights Reserved | yes | **Not for pack art.** The page says "Feel free to include this resource pack in your modpack, just provide credit or a link", which covers shipping it in the pack, not using its art in a banner. It is also a retexture of IE (plus Immersive Posts), so IE's licence sits underneath. Ask tdstress, and Blu for the IE part. |
| Iteratively Engineered 1.5 | Paxilmaniac | MIT | no (installed, not enabled) | **Pack author's grant only.** MIT on the pack, but it is a resprite of IE, so IE's "ask before copying assets" still applies to anything derived from IE art. |
| IE: Reimmersed v1.1 (Forge) | RUBA (RUBABLADADUDI) | All Rights Reserved | no | **No.** Also derived from IE. |

The three IE retexture packs are derivative works of Immersive Engineering's art. A permissive licence from the pack
author does not remove Blu's licence on the underlying design, so treat all of them as needing IE permission, the same
as section 1.

Other packs on disk (not asked about; licences not checked): Create Tweaks Compats, Create-Energistics (AE2 restyle),
Created Pretty Pipes, moromoro's Create style Pretty Pipes, Create style Pretty Fluid Pipes, CreateSophBackpacks,
LCVP Create Style, Copycat Framed Block, Fancy Crops, Fancy Beds, Clear Glass Pack, Modded Omelet, Quark and
SecurityCraft Programmer Art, Lootr Chest Retexture, Waystones 26.2, Eclectic Trove, Golden crit, Low On Fire, Lower
Shield, SHC Enhanced Fire, Squirming Silverfish, IE Breaker Switch Sound, CE2 Iron Armor Fix. Most retexture another
mod's art, so the underlying mod's licence applies as well. Treat as not usable until checked.

## 4. Everything else in `mods/` (declared licence, art not verified)

Classified from `mods.toml`. Counts of the 414 jars excluding the Create, Immersive and Delight families above.

- **Permissive (MIT, Apache, AFL, Unlicense, MPL), about 120 jars.** Usable with notice and credit. Create addons in
  this group: Create D&D (MIT), Create Central Kitchen, Create Enchantment Industry, Create Crafts and Additions,
  Create Framed, Create Applied Kinetics, Create Crystal Clear, Create Factory, Create Power Loader, Create Tweaked
  Controllers, Create Fast Schematic Cannon, Create Goggles, Compressed Creativity, Vintage Improvements, Railways.
  Others: Engineer's Decor, Decorative Blocks, Dramatic Doors, Project Red, Storage Drawers, JEI, Fairy Lights, Chalk,
  Sooty Chimneys, Another Furniture, Design Decor, Yuushya, Astikor Carts.
- **LGPL / GPL, about 90 jars.** Usable if the credit and licence terms are respected: Immersive Weathering (LGPLv3),
  Immersive Armors (GNU), Immersive Paintings (GPL-3.0), Alex's Mobs, PneumaticCraft, Sophisticated Backpacks and
  Core, KubeJS, MCA, Exposure, Moonlight, Comforts, Curios. Compressed
  Engineering (LGPL 2.1) and Full Engineering (LGPL) are IE addons whose own licence applies.
- **Non-commercial or no-derivatives (CC BY-NC-SA, NC-ND, custom), about 19 jars.** Avoid for anything monetised
  (CurseForge projects can earn Points): Patchouli, WTHIT, Legendary Tooltips, Nature's Compass, Explorer's Compass,
  Highlighter, Better Beacons, Handcrafted ("Terrarium Licence"), Campsite Structures, Paintings Pack 1, Incubation.
  The NC-ND ones also forbid edits. `detailab` is declared "MIT Licence" and was caught by the filter by mistake; verify.
- **All Rights Reserved, about 89 jars.** Not usable without permission: Create Deco, Create Compressed, Create Deep
  Dark, Create Questing, Create Guardian Beam Defense, Create Recycle, Create Ultimate Factory, Create Liquid Fuel,
  Immersive Petroleum, Supplementaries and Supplementaries Squared, Twigs, Waystones, Architect's Palette,
  Cooking for Blockheads, Lightman's Currency, FTB Quests / Teams / Library / Chunks, Xaero's maps, Every Compat,
  Copycats+, Serene Seasons, More Mob Variants, Trash Cans, Wardrobe, and the rest.
  Dummmmmmy is code Public Domain, assets ARR.
- **Custom licences, about 31 jars (read before use):** Quark (own LICENSE.md), Botania (Botania License), AE2 and
  AE Bot ("see GitHub"), ComputerCraft (Public License), Slice and Dice, Lootr, Cosmetic Armor Reworked (MMPL),
  Splash Milk (WTFPL).

## 5. How to use this for the art

1. Safest set for a banner or logo: Create (1.19.2 jar), Farmer's Delight and its MIT siblings, the pack's own art.
   These cover most of the pack's look.
2. If IE imagery matters for the pack's identity, ask Blu for permission; the licence invites asking.
3. Put a credits block in the project description (the CurseForge page draft is
   [creative_common_curseforge.md](creative_common_curseforge.md)): "Art includes textures from Create (MIT, Creators
   of Create), Farmer's Delight (MIT, vectorwing) ..." and ship the MIT notice if the art file is distributed on its
   own.
4. Vanilla textures and screenshots including vanilla blocks fall under Mojang's own brand and asset guidelines, not
   any mod's licence.
5. In-game screenshots of ARR mods' blocks are a grey area (they are renders, but still the author's art). Prefer to
   keep them out of the hero image.

## 6. Open items

- Read the upstream LICENSE for: Oceanic, Veggies, Vintage, Fungi, Large Meals, Rustic Delight, Engineer's Delight.
- Licence check for the other resource packs listed in section 3.
- Spot-check Create 0.5.1.i textures for third-party art before a large composite.
- Re-run the audit if mods change; the jar scan is a few lines of Python over `META-INF/mods.toml`.

## 7. Per-mod table (all jars in `mods/`)

Generated 2026-11-07 from each jar's `mods.toml` `license` field and sorted by category. The field is the author's one-line claim and does not always cover the art; a row is a first pass, not a clearance. Rows already given a verdict in sections 1 to 4 (Create, IE, Vinery, Serene Seasons, the ARR Delight mods) take that verdict over this table. Counts: Permissive 144, Copyleft (GPL / LGPL / MPL) 97, Custom, non-commercial or unclear 69, All Rights Reserved or none declared 104.

### 7.1 Permissive (144)

Usable with credit and the licence notice.

| Mod | Jar | Declared licence |
|---|---|---|
| Alex | alexsdelight-1.4.1.jar | MIT License |
| Another Furniture | another_furniture-forge-1.19.2-2.1.4.jar | MIT |
| AppleSkin | appleskin-forge-mc1.19-2.4.2.jar | The Unlicense |
| Applied Cooking | appliedcooking-2.0.3.jar | MIT |
| AstikorCarts | astikorcarts-1.19.2-1.1.2.jar | MIT |
| Async Locator | async-locator-forge-1.19.2-1.3.0.jar | MIT License |
| Bad Packets | badpackets-forge-0.2.3.jar | Apache-2.0 |
| Beautify | beautify-1.4.3.jar | MIT |
| BetterF3 | BetterF3-4.0.1-Forge-1.19.2.jar | MIT |
| Blocky Siege | blocky_siege-6.1.91-1.19.2.jar | Academic Free License v3.0 |
| Bobber Detector | bobberdetector-0.1.9-1.19.2.jar | MIT License |
| Book Fishing | Book Fishing 1.0.0 - 1.19.2.jar | Academic Free License v3.0 |
| Brick Hopper | brickhopper-1.19-1.4.2.1.jar | The MIT License (MIT) |
| Bushier Flowers | bushierflowers-0.0.2-1.19.2.jar | MIT |
| Carry Cats | Carry Cats 2.5 1.19.2.jar | MIT |
| Cat Loaf | catloaf-1.1.1-1.19.x-FORGE.jar | MIT |
| CC:C Bridge | cccbridge-mc1.19.2-forge-v1.5.1.jar | Apache-2.0 |
| Chalk | chalk-1.19.2-1.6.4.jar | MIT |
| Chefs Delight | chefsdelight-1.0.3-forge-1.19.2.jar | MIT License |
| Clear Void | clearvoid-forge-1.3.0.jar | MIT License |
| Clumps | Clumps-forge-1.19.2-9.0.0+14.jar | MIT |
| Compressed Creativity | compressedcreativity-1.19.2-0.1.8.jar | MIT |
| Construction Wand | constructionwand-1.19.2-2.10.jar | MIT License |
| Control Engineering | ControlEngineering-0.2.0.jar | MIT |
| Controlling | Controlling-forge-1.19.2-10.0+7.jar | MIT |
| Cool Rain | coolrain-1.19.2-Forge-1.0.2.jar | MIT |
| Corn Delight | corn_delight-1.0.3-1.19.2.jar | MIT License |
| corpsecurioscompat | corpsecurioscompat-1.18.x-1.20.x-Forge-2.2.2.jar | MIT |
| Cosmetic Armor x Corpse Compat | cosmeticcorpsecompat-1.19.x-1.20.x-Forge-1.0.0.jar | MIT |
| Create | create-1.19.2-0.5.1.i.jar | MIT |
| Create Applied Kinetics | createappliedkinetics-1.4-1.19.2.jar | MIT |
| Create Confectionery | create-confectionery1.19.2_v1.0.9.jar | Academic Free License v3.0 |
| Create Crafts & Additions | createaddition-1.19.2-1.2.3.jar | MIT License, Copyright 2022 MRH0 |
| Create Enchantment Industry | create_enchantment_industry-1.19.2-for-create-0.5.1.f-1.2.9.e.jar | MIT |
| Create Goggles | creategoggles-1.19.2-0.5.5.g-[FORGE].jar | MIT |
| Create Mechanical Extruder | create_mechanical_extruder-1.19.2-1.5.7.e-44.jar | MIT License |
| Create: Central Kitchen | create_central_kitchen-1.19.2-for-create-0.5.1.f-1.3.11.c.jar | MIT |
| Create: Crystal Clear | create_crystal_clear-0.2.1-1.19.2.jar | MIT |
| Create: Design n | design_decor-0.4-1.19.2.jar | MIT |
| Create: Dreams & Desires | Create-DnDesire-1.19.2-1.2c.Beta Mid-Dev.jar | MIT |
| Create: Factory | create_factory-0.0.2-1.19.2.jar | MIT License |
| Create: Fast Schematic Cannon | CreateFastSchematicCannon-2.6.1-0.5.1i-forge-1.19.2.jar | MIT |
| Create: Framed | createframed-1.19.2-1.4.5.1.jar | MIT |
| Create: Gravity | create_gravity-1.19.2-1.2.0-all.jar | MIT License |
| Create: Netherless | create_netherless-1.19.2-1.3.jar | Academic Free License v3.0 |
| Create: Oxidized | create_oxidized-1.19.2-0.1.1.jar | MIT License |
| Create: Power Loader | create_power_loader-1.5.0-mc1.19.2.jar | MIT |
| Create: Steam  | Steam_Rails-1.6.6+forge-mc1.19.2.jar | MIT |
| create: things and misc | create_misc_and_things_ 1.19.2_4.0A.jar | Academic Free License v3.0 |
| Create: Tweaked Controllers | create_tweaked_controllers-1.19.2-1.2.4.jar | MIT |
| Create: Vintage Improvements | vintageimprovements-1.19.2-0.2.0.3.jar | MIT |
| Dave | dbExtended-1.19.2-6.0.1.jar | MIT License |
| Decorative Blocks | Decorative Blocks-forge-1.19.2-3.0.0.jar | MIT |
| Detail Armor Bar | DetailArmorBar-2.6.4+1.19.2-forge.jar | MIT Licence |
| Dimensional Paintings | Dimensional-Paintings-1.19.2-1.4.1.jar | MIT |
| Don | don't void arrows V.1.1-1.19.2.jar | Academic Free License v3.0 |
| Dramatic Doors | DramaticDoors-QuiFabrge-1.19.2-3.2.7.jar | MIT |
| Dreamcatcher | dreamcatcher mod.jar | Academic Free License v3.0 |
| EnderStorage | EnderStorage-1.19.2-2.10.1.181-universal.jar | The MIT License (MIT) |
| Engineer | engineersdecor-1.19.2-forge-1.3.29.jar | MIT |
| Fairy Lights | fairylights-6.0.1-1.19.2.jar | MIT |
| Farmer | FarmersRespite-1.19-2.0.jar | MIT License |
| Fast Workbench | FastWorkbench-1.19.2-7.1.4.jar | MIT License |
| FastFurnace | FastFurnace-1.19.2-7.0.0.jar | MIT License |
| Felsic Gear | Felsic Gear 1.0.0  - 1.19.2.jar | MIT License |
| Ferrite Core | ferritecore-5.0.3-forge.jar | MIT |
| fireflybushport | fireflybush-1.19.2.jar | Academic Free License v3.0 |
| Flower Patch | FlowerPatch-forge-1.19.2-2.0.3.jar | MIT |
| fortuneonneterite i ig z | fortune_on_netherite_1.1.0_forge_1.19.2.jar | Academic Free License v3.0 |
| FTBQuestsOptimizer | FTBQuestsOptimizer-forge-1.2.1-1.19.2.jar | MIT |
| Gates | gates-1.8.0.jar | The MIT License (MIT) |
| GeckoLib | geckolib-forge-1.19-3.1.40.jar | MIT |
| Greek Villager House | greek_villager_house-1.0.0-forge-1.19.2.jar | Academic Free License v3.0 |
| hobbit house  | hobbit_house_-1.0.1-forge-1.19.2.jar | Academic Free License v3.0 |
| Hostile_Delight | Hostile_Delight_0.2.2_1.19.2.jar | Academic Free License v3.0 |
| HT | TreeChop-1.19.2-forge-0.19.1.jar | MIT (https://github.com/hammertater/treechop/blob/main/LICENSE) |
| Ice Boat Nerf | iceboatnerf-1.1.1.jar | MIT |
| Immersive Portals | immersive-portals-2.3.7-mc1.19.2-forge.jar | Apache-2.0 |
| ImprovedPillagerOutpost | ImprovedPillagerOutpost-v5.jar | CC0-1.0 |
| Iron Fishing Rods | Iron Fishing Rods 1.0.0 - 1.19.2.jar | Academic Free License v3.0 |
| It Takes A Pillage | takesapillage-1.0.3-1.19.2.jar | MIT |
| Just Enough Breeding | justenoughbreeding-forge-1.19.2-1.4.0.jar | The MIT License (MIT) |
| Just Enough Immersive Multiblocks | jeimultiblocks-1.19.2-0.0.3.jar | MIT |
| Just Enough Items | jei-1.19.2-forge-11.8.1.1034.jar | The MIT License (MIT) |
| Just Enough Professions (JEP) | JustEnoughProfessions-forge-1.19.2-2.0.2.jar | MIT |
| Just Enough Repair Materials | jerm-1.19.2-1.2.jar | Code: Public Domain |
| Kiwi | Kiwi-1.19.2-forge-8.3.6.jar | MIT |
| Konkrete | konkrete_forge_1.8.0_MC_1.19-1.19.2.jar | Apache-2.0 |
| KubeJS CSV | kubejs_csv-1.18.2~1.20.1-1.0.0.jar | MIT |
| Lava Clear View | LavaClearView-1.19.2-forge-5.0.0.jar | MIT |
| Loading Protection | loadingprotection-1.0.2-forge-1.19.2.jar | MIT |
| MmmMmmMmmmmm | dummmmmmy-1.19.2-1.7.1.jar | code: Public Domain, assets: ARR |
| Mod Name Tooltip | modnametooltip-1.20.1-1.20.0.jar | MIT |
| Moderately Enough Effect Descriptions (MEED) | MEED-1.19.2-7.1.1.jar | Academic Free License v3.0 |
| Monobank | monobank-1.19.2-1.1.3.jar | MIT |
| More Delight | moredelight-25.07.28-1.19-forge.jar | MIT License |
| More Immersive Wires | more-immersive-wires-1.19.2-1.1.3.jar | MIT License https://github.com/tom5454/More-Immersive-Wires/blob/main/LICENSE |
| Mouse Tweaks | MouseTweaks-forge-mc1.19-2.23.jar | BSD-3-Clause |
| Mysterious Mountain Lib | mysterious_mountain_lib-1.2.3-1.19.2.jar | MIT License |
| Netherite Horse Armor | netherite_horse_armor-forge-1.19-1.0.4.jar | MIT License |
| old swamp fisherman house | old_swamp_fisherman_house-1.0.2-forge-1.19.2.jar | Academic Free License v3.0 |
| Placebo | Placebo-1.19.2-7.4.1.jar | MIT License |
| PonderJS | ponderjs-1.19.2-1.2.0.jar | MIT |
| PortableWardrobe | portablewardrobearmorswap-1.0.2-forge-1.19.2.jar | MIT License |
| Portal Gun | portalgun-1.19.2-5.0.0.jar | MIT License |
| Presence Footsteps (Forge) | PresenceFootsteps-1.19.2-1.6.4.1-forge.jar | MIT |
| Pretty Pipes | PrettyPipes-1.13.6.jar | MIT |
| Pretty Pipes: Fluids | ppfluids-1.19.2-2.0.1.jar | MIT License |
| Pretty Rain | Pretty Rain-1.19.2-Forge-1.1.1.jar | MIT |
| ProjectRed Core | ProjectRed-1.19.2-4.19.0-beta+33-core.jar | MIT |
| ProjectRed Fabrication | ProjectRed-1.19.2-4.19.0-beta+33-fabrication.jar | MIT |
| ProjectRed Illumination | ProjectRed-1.19.2-4.19.0-beta+33-illumination.jar | MIT |
| ProjectRed Integration | ProjectRed-1.19.2-4.19.0-beta+33-integration.jar | MIT |
| ProjectRed Transmission | ProjectRed-1.19.2-4.19.0-beta+33-transmission.jar | MIT |
| Rebirth: Paintings | REBIRTH Paintings 5.0.0 Backport 1.19.2.jar | Academic Free License v3.0 |
| Regrowth | Regrowth-1.19-41.1.31.1.jar | MIT License |
| Resourceful Lib | resourcefullib-forge-1.19.2-1.1.24.jar | MIT |
| Responsive Shields | responsiveshields-2.3-mc1.18-19-20.x.jar | MIT |
| Room For Two | roomfortwo-forge-1.19.2-0.2.0.jar | MIT |
| Sculk Transporting | sculktransporting-1.19.2-1.1.jar | MIT |
| SeasonHud | seasonhud-forge-1.19.2-2.0.4.jar | MIT |
| Sebastrn Lib | sebastrnlib-2.0.2.jar | MIT |
| Second Chance Forge | secondchanceforge-1.19-1.5.1.jar | MIT |
| Simple Backups | SimpleBackups-1.19.1-2.1.12.jar | The Apache License, Version 2.0 |
| Simply Houses | SimplyHouses-1.1.3-1.19-forge.jar | MIT |
| Sky Structures | Sky Structures 1.0.0 - 1.19.2.jar | Academic Free License v3.0 |
| Sooty Chimneys | sootychimneys-1.19.2-1.2.1.jar | MIT |
| Stardew Fishing | stardew_fishing-1.19.2-1.3-all.jar | MIT |
| Storage Delight (Creative Delight) | repackedStorage-25.12.09-1.19-forge-ce-19.2-01.jar | MIT (upstream); overlay GNU GPL3 |
| Storage Drawers | StorageDrawers-1.19.2-11.4.1.jar | MIT |
| Subterrestrial | subterrestrial-forge-1.19.2-2.0.0.jar | MIT |
| SwingThroughGrass | swingthroughgrass-1.19.2-1.10.0.jar | MIT |
| Tax Free Levels | TaxFreeLevels-1.4.23-forge-1.20.1.jar | MIT |
| Tectonic | tectonic-forge-1.19-2.3.5a.jar | MIT |
| Too Many Paintings! | toomanypaintings-26.08.16-1.19-forge.jar | MIT |
| Unlit Campfire | unlitcampfire-forge-1.19-1.7.1.0.jar | The MIT License (MIT) |
| Untitled Duck Mod | untitledduckmod-0.6.1-1.19.2-forge.jar | MIT |
| Unwrecked Ships - Plus | Unwrecked Ships Plus 1.19-1.0.4 Forge.jar | MIT |
| Vintage KubeJS | vintage_kubejs-1.19.2-1.0.0rc-2.jar | MIT |
| Wares | wares-1.19.2-1.2.9.jar | MIT |
| Well Mod | well-1.19.2-1.0.0.jar | Apache License 2.0 |
| Witch Mobility | witchmobility-1.3.2-1.19.2.jar | MIT |
| WTHIT Harvestability | wthitharvestability-mc1.19.2-2.1.0.jar | The Unlicense |
| Yuushya Modelling | yuushya-modelling-1.19.2-forge-2.2.0.jar | MIT |

### 7.2 Copyleft (GPL / LGPL / MPL) (97)

Usable if credit and licence terms are kept; release the finished art under GPLv3.

| Mod | Jar | Declared licence |
|---|---|---|
| Architectury | architectury-6.6.92-forge.jar | GNU LGPLv3 |
| Bedspreads | bedspreads-forge-6.0.0+1.19.2.jar | LGPL-3.0-or-later |
| BetterDays | betterdays-1.19.2-3.3.4.3-FORGE.jar | LGPL-3.0-or-later |
| Block Runner | BlockRunner-v4.2.2-1.19.2-Forge.jar | MPL-2.0 |
| BoatView360 | boatview360-v1.0.4-mc1.17-1.20.1-forge.jar | LGPLv3 |
| Bookshelf | Bookshelf-Forge-1.19.2-16.3.20.jar | LGPL V2.1 |
| BotanyPots | BotanyPots-Forge-1.19.2-9.0.44.jar | LGPL V2.1 |
| Breezy | breezy-1.19.2-1.0.8.jar | LGPL-3.0 |
| Caelus API | caelus-forge-1.19.2-3.0.0.6.jar | LGPL-3.0-or-later |
| Charm of Undying | charmofundying-forge-6.2.1+1.19.2.jar | LGPL-3.0-or-later |
| Cherished Worlds | cherishedworlds-forge-6.0.5+1.19.4.jar | LGPL-3.0-or-later |
| CherishedItems | CherishedItems-Forge-1.19.2-1.0.2.jar | LGPL V2.1 |
| Chunky | Chunky-1.3.38.jar | GNU GPLv3 |
| Cloth Config v8 API | cloth-config-8.3.134-forge.jar | GNU LGPLv3 |
| Comforts | comforts-forge-6.0.7+1.19.2.jar | LGPL-3.0-or-later |
| Compressed Engineering | compressedengineering-1.3.5.jar | LGPL 2.1 |
| Cozy | cozys-improved-cats-2.1.0-forge-1.19.2.jar | LGPL-3.0 |
| Create: Bells & Whistles | bellsandwhistles-v0.4.4-1.19.2.jar | GPL-3.0 |
| Create: Dynamic Village | dynamicvillage-v0.4-1.19.2.jar | GPL-3.0 |
| Create: Interiors | interiors-0.5.6+forge-mc1.19.2-build.105.jar | GPL-3.0 |
| Creative Brewing | creativebrewing-1.0.0.jar | GPL-3.0-only |
| Creative Calendar | creativecalendar-1.0.0.jar | GPL-3.0-only |
| Creative Common | creativecommon-1.1.0.jar | GPL-3.0-only |
| Creative Compat | creativecompat-1.0.0.jar | GPL-3.0-only |
| Creative Delight | creativedelight-1.1.0.jar | GPL-3.0-only |
| Creative Devtools | creativedevtools-1.0.0.jar | GPL-3.0-only |
| Creative Engineering | creativeengineering-2.0.0.jar | GPL-3.0-only |
| Creative Equipment | creativeequipment-1.0.0.jar | GPL-3.0-only |
| Creative Labels | creativeLabels-1.0.1.jar | GPL-3.0-only |
| Creative Magic | creativemagic-1.0.0.jar | GPL-3.0-only |
| Creative Worldgen | creativeworldgen-1.0.0.jar | GPL-3.0-only |
| CreativeCore | CreativeCore_FORGE_v2.11.28_mc1.19.2.jar | LGPL-3.0-only |
| Curios API | curios-forge-1.19.2-5.1.6.4.jar | LGPL-3.0-or-later |
| DarkPaintings | DarkPaintings-Forge-1.19.2-13.1.5.jar | LGPL v2.1 |
| DeathKnell | DeathKnell-Forge-1.19.2-6.0.3.jar | LGPL v2.1 |
| Distant Horizons | DistantHorizons-3.0.2-b-1.19.2-fabric-forge.jar | LGPL |
| Do A Barrel Roll | do-a-barrel-roll-2.6.2+1.19.2-forge.jar | GPL-3.0 |
| DragonLoot | dragonloot-1.1.15-1.19.2-forge.jar | GPL-3.0-or-later |
| Easy Magic | EasyMagic-v4.3.3-1.19.2-Forge.jar | MPL-2.0 |
| Elytra Bombing | ElytraBombing-Forge-1.19.2-1.0.0.jar | AGPL v3 |
| Elytra Slot | elytraslot-forge-6.1.2+1.19.2.jar | LGPL-3.0-or-later |
| EnchantmentDescriptions | EnchantmentDescriptions-Forge-1.19.2-13.0.20.jar | LGPL V2.1 |
| Ender Crop | endercrop-1.19.2-1.7.0-beta.jar | GPLv3 https://raw.githubusercontent.com/DrManganese/ender-crop/main/LICENSE |
| Etched | etched-forge-2.3.2.jar | GPLv3 |
| Exposure (Backport & Extras) | exposureBackport-1.9.19-ce-19.2-04.jar | GPL-3.0-only |
| Extended Cogwheels | extendedgears-2.1.1-1.19.2-0.5.1.f-forge.jar | GNU GPLv3 |
| FramedBlocks | FramedBlocks-6.8.6.jar | LGPL v3 |
| Fullblock Energistics | fulleng-forge-1.7.1-1.19.2.jar | LGPLv3.0 |
| Glow Ink Plus Mod | (Forge)GlowInkPlus-1.19.2-1.2.0.jar | GPL-3.0 |
| ImmediatelyFast | ImmediatelyFast-Forge-1.4.0+1.19.2.jar | LGPL-3.0 |
| Immersive Armors | immersive_armors-1.5.6+1.19.2-forge.jar | GNU |
| Immersive Paintings | immersive_paintings-0.6.7+1.19.2-forge.jar | GPL-3.0-only |
| Immersive Weathering | immersive_weathering-1.19.2-1.2.10-forge.jar | LGPLv3 |
| Item Filters | item-filters-forge-1902.2.9-build.51.jar | GNU LGPLv3 |
| ItemPhysic | ItemPhysic_FORGE_v1.6.7_mc1.19.2.jar | LGPL-3.0-only |
| KubeJS | kubejs-forge-1902.6.2-build.73.jar | GNU LGPLv3 |
| KubeJS + CC: Tweaked | kjscc-1902-1.0.2-beta.jar | LGPLv2.1 |
| KubeJS Botania | kubejs_botania-1.3.1-forge.jar | GPLv3 |
| KubeJS Create | kubejs-create-forge-1902.2.4-build.36.jar | GNU LGPLv3 |
| KubeJS Immersive Engineering | kubejs-immersive-engineering-1902.2.3-build.18.jar | GNU LGPLv3 |
| KubeJSDelight | kubejsdelight-1.19.2-1.1.2.jar | GNU LGPL 3.0 |
| Little Contraptions | littlecontraptions-forge-1.19.2.2.jar | GPLv3 |
| Little Logistics | littlelogistics-mc1.19.2-v1.4.0.jar | LGPLv3 |
| Memory Leak Fix | memoryleakfix-forge-1.17+-1.0.0.jar | LGPL-2.1-only |
| Metal Fences | MetalFences-FORGE-1.19.2-1.0.1.jar | LGPL-3.0-only |
| Minecraft Comes Alive | minecraft-comes-alive-7.5.20+1.19.2-universal.jar | GPL-3.0-only |
| ModernFix | modernfix-forge-5.18.1+mc1.19.2.jar | GNU LGPL 3.0 |
| Moonlight Library | moonlight-1.19.2-2.3.7-forge.jar | gplv3 |
| Old Guns Mod | oldguns-1.19.2-3.7.3-47.jar | AGPL-3.0 |
| PneumaticCraft: Repressurized | pneumaticcraft-repressurized-1.19.2-4.3.15-52.jar | GNU GPLv3 |
| Pollen | pollen-forge-2.1.0.jar | LGPLv3 |
| Polymorph | polymorph-forge-0.46.6+1.19.2.jar | LGPL-3.0-or-later |
| Polymorphic Energistics | polyeng-forge-0.1.1-1.19.2.jar | LGPLv3.0 |
| Potion Bundles | potionbundles-1.19.1-1.6.jar | GPLv3 |
| Puzzles Lib | PuzzlesLib-v4.4.3-1.19.2-Forge.jar | MPL-2.0 |
| Rhino | rhino-forge-1902.2.3-build.284.jar | MPL-2.0 |
| Rubidium | rubidium-mc1.19.2-0.6.2c.jar | LGPL-3.0-only |
| Rubidium Extra | rubidium-extra-0.4.19+mc1.19.2-build.105.jar | LGPLv3 |
| Scholar | scholar-1.19.2-1.0.0-forge.jar | GNU GPLv3 |
| SecurityCraft | [1.19.2] SecurityCraft v1.9.6.1.jar | GNU GPL v3.0 https://github.com/Geforce132/SecurityCraft/blob/1.19.1/LICENSE |
| Shulker Box Slot | shulkerboxslot-forge-5.0.0-beta.2+1.19.2.jar | LGPL-3.0-or-later |
| SimpleHats | simplehats-forge-1.19.2-0.3.2.jar | https://github.com/fonnymunkey/SimpleHatsAux/blob/main/LICENSE |
| Sit | sit-1.19-1.3.3.jar | GNU GPL v3.0 https://github.com/bl4ckscor3/Sit/blob/1.19/LICENSE |
| Spice of Life: Apple Pie Edition | solapplepie-1.19.2-2.2.0.jar | LGPL-2.1 |
| Spyglass Improvements | spyglass_improvements-forge-1.5.12+mc1.19+forge.jar | GNU GPL 3.0 |
| WorldEdit | worldedit-mod-7.2.12.jar | GPL |
| XaeroZoomout | XaeroZoomout-Forge-1.19-1.1.0.jar | GPL-3.0-only |
| XP Tome | xptome-1.19-2.1.7.jar | GNU GPL v3.0 https://github.com/bl4ckscor3/XP-Tome/blob/1.19/LICENSE |
| YUNG | YungsApi-1.19.2-Forge-3.8.10.jar | LGPLv3 |
| YUNG | YungsBetterDesertTemples-1.19.2-Forge-2.2.2.jar | LGPLv3 |
| YUNG | YungsBetterDungeons-1.19.2-Forge-3.2.2.jar | LGPLv3 |
| YUNG | YungsBetterEndIsland-1.19.2-Forge-1.0.jar | LGPLv3 |
| YUNG | YungsBetterJungleTemples-1.19.2-Forge-1.0.1.jar | LGPLv3 |
| YUNG | YungsBetterNetherFortresses-1.19.2-Forge-1.0.6.jar | LGPLv3 |
| YUNG | YungsBetterStrongholds-1.19.2-Forge-3.2.0.jar | LGPLv3 |
| YUNG | YungsBetterWitchHuts-1.19.2-Forge-2.1.0.jar | LGPLv3 |
| YUNG | YungsExtras-1.19.2-Forge-3.1.0.jar | LGPLv3 |

### 7.3 Custom, non-commercial or unclear (69)

Read the licence first; avoid for monetised pages; verify.

| Mod | Jar | Declared licence |
|---|---|---|
|  | DustrialDecor-1.3.3-1.19.2.jar | Forge said I needed a license |
| Abnormals Delight | abnormals_delight-1.19.2-4.1.2.jar | https://github.com/team-abnormals/abnormals-delight/blob/main/LICENSE.txt |
| Alex | alexsmobs-1.21.1.jar | GNU LESSER GENERAL PUBLIC LICENSE |
| Allurement | allurement-1.19.2-3.2.1.jar | https://github.com/team-abnormals/allurement/blob/main/LICENSE.txt |
| Applied Botanics | Applied-Botanics-forge-1.4.6.jar | See GitHub repository for details |
| Applied Energistics 2 | appliedenergistics2-forge-12.9.12.jar | See GitHub repository for details |
| AutoRegLib | AutoRegLib-1.8.2-55.jar | https://github.com/Vazkii/AutoRegLib/blob/master/README.md |
| Berry Good | berry_good-1.19.2-6.1.0.jar | https://github.com/team-abnormals/berry-good/blob/main/LICENSE.txt |
| Blueprint | blueprint-1.19.2-6.2.0.jar | https://github.com/team-abnormals/blueprint/blob/main/LICENSE.txt |
| Boat Item View | Boat-Item-View-Forge-1.19.x-0.0.4.jar | https://raw.githubusercontent.com/50ap5ud5/BoatItemView/1.19.x-forge/LICENSE |
| Boatload | boatload-1.19.2-4.2.2.jar | https://github.com/team-abnormals/boatload/blob/main/LICENSE.txt |
| Botania | Botania-1.19.2-440.2-FORGE.jar | Botania License |
| Buzzier Bees | buzzier_bees-1.19.2-5.0.1.jar | https://github.com/team-abnormals/buzzier-bees/blob/main/LICENSE.txt |
| Campsite Structures | campsite_structures-1.19.2-FORGE-1.0.3.jar | Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License |
| CBMultipart | CBMultipart-1.19.2-3.2.0.139-universal.jar | GNU Lesser General Public License v2.1 |
| CC: Tweaked | cc-tweaked-1.19.2-1.101.4.jar | ComputerCraft Public License (https://raw.githubusercontent.com/dan200/ComputerC |
| CERBON | CerbonsBetterBeacons-Forge-1.19.2-1.0.2.jar | Attribution-NonCommercial-ShareAlike 3.0 Unported |
| Citadel | citadel-2.1.4-1.19.jar | GNU LESSER GENERAL PUBLIC LICENSE |
| Clayworks | clayworks-1.19.2-2.1.0.jar | https://github.com/team-abnormals/clayworks/blob/main/LICENSE.txt |
| Cloud Storage | cloudstorage-1.3.4-1.19.2.jar | GNU LESSER GENERAL PUBLIC LICENSE |
| CodeChicken Lib | CodeChickenLib-1.19.2-4.3.2.490-universal.jar | GNU Lesser General Public License v2.1 |
| Connectible Chains | Connectible Chains-forge-1.19.2-2.1.4.jar | GNU Lesser General Public License v3.0 |
| CosmeticArmorReworked | CosmeticArmorReworked-1.19.2-v1a.jar | Minecraft Mod Public License 1.0.1 |
| Create Slice & Dice | sliceanddice-forge-2.4.0.jar | https://github.com/pssbletrngle/sliceanddice/blob/1.19.x/LICENSE.txt |
| Domestication Innovation | domesticationinnovation-1.6.1-1.19.2.jar | GNU LESSER GENERAL PUBLIC LICENSE |
| Dynamic Villager Trades | dynamicvillagertrades-1.2.3-forge-1.19.2.jar | CC-0 |
| Engineers Delight | Engineers Delight 1.19.2 R.1.3.jar | Mozilla Public License 2.0 |
| EntityCulling | entityculling-forge-1.6.1-mc1.19.2.jar | tr7zw Protective License |
| Explorer | ExplorersCompass-1.19.2-1.3.0-forge.jar | CC BY-NC-SA 4.0 |
| Fantasy | fantasyfurniture-1.19.2-6.7.0.jar | GNU General Public License v3.0 |
| Handcrafted | handcrafted-forge-1.19.2-2.0.6.jar | Terrarium Licence |
| Highlighter | Highlighter-1.19.1-1.1.4.jar | CC BY-NC-ND 4.0 |
| Iceberg | Iceberg-1.19.2-forge-1.1.4.jar | CC BY-NC-ND 4.0 |
| Immersive Engineering | ImmersiveEngineering-1.19.2-9.2.4-170.jar | Blu |
| Immersive Posts | ImmersivePosts-1.19.2-4.3.0-8.jar | TwistedGate |
| Incubation | incubation-1.19.2-3.0.1.jar | https://github.com/team-abnormals/incubation/blob/main/LICENSE.txt |
| Jess | FORGE_PaintingPack_1.19.2.jar | Creative Commons Attribution-NonCommercial 3.0 |
| Just Enough Resources | JustEnoughResources-1.19.2-1.2.3.243.jar |  |
| Just Enough Sculk Sensor | jess-1.19.2-forge-1.0.0.jar | Insert License Here |
| JustEnoughBeacons | JustEnoughBeacons-Forge-1.19+-1.1.2.jar | GNU General Public License v3.0 |
| Legendary Tooltips | LegendaryTooltips-1.19.2-forge-1.4.0.jar | CC BY-NC-ND 4.0 |
| LootJS | lootjs-forge-1.19.2-2.9.1.jar | GNU Lesser General Public License v3.0 |
| Lootr | lootr-forge-1.19-0.4.29.76.jar | https://github.com/noobanidus/Lootr/blob/1.16/LICENSE |
| Make Bubbles Pop | make_bubbles_pop-0.3.0-forge-mc1.19-1.19.3.jar | Custom |
| minecraft style paintings | MC style paintings 1.19.2 v2.jar | Not specified |
| Momentum Protection | momentumprotection-1.19.2-0.0.1.jar | GNU Affero General Public License v3.0 |
| Moog | mes-1.3.1-1.19.2-forge.jar | GNU Lesser General Public License v3.0 |
| MoreJS | morejs-forge-1.19.2-0.3.0.jar | GNU Lesser General Public License v3.0 |
| Nature | NaturesCompass-1.19.2-1.10.0-forge.jar | CC BY-NC-SA 4.0 |
| Neapolitan | neapolitan-1.19.2-4.1.0.jar | https://github.com/team-abnormals/neapolitan/blob/main/LICENSE.txt |
| NotEnoughAnimations | notenoughanimations-forge-1.12.2-mc1.19.2.jar | tr7zw Protective License |
| Nullscape | Nullscape_1.19.3_v1.2.2.jar | Stardust Labs License |
| Patchouli | Patchouli-1.19.2-77.jar | CC BY-NC-SA 3.0 |
| Prism | Prism-1.19.1-1.0.2.jar | CC BY-NC-ND 4.0 |
| Quark | Quark-3.4-418.jar | https://github.com/Vazkii/Quark/blob/master/LICENSE.md |
| Quark Engineering | QuarkEngineering.jar | CC BY-NC-SA |
| Quark Oddities | QuarkOddities-1.18.jar | https://github.com/Vazkii/Quark/blob/master/LICENSE.md |
| Rare Ice | rare-ice-0.5.1.jar | Creative Commons Attribution-NonCommercial 4.0 International Public License |
| Revamped Shipwrecks | Revamped Shipwrecks 1.1.0.jar | LicenseRef-Attribution-NonCommercial-ShareAlike-4.0-International- |
| SawmillHouse | sawmillhouse-1.3.0-forge-1.19.2.jar | Not specified |
| Sophisticated Backpacks | sophisticatedbackpacks-1.19.2-3.20.2.1035.jar | GNU General Public License v3.0 |
| Sophisticated Core | sophisticatedcore-1.19.2-0.6.4.730.jar | GNU General Public License v3.0 |
| Splash Milk | splash_milk-1.19.2-1.4.0.jar | WTFPL |
| Tactical Fishing | TacticalFishing-1.19.x2-v1.2.jar | Check Curseforge/Modrinth for details |
| Terralith | Terralith_1.19.3_v2.3.12.jar | Stardust Labs License |
| Towns and Towers | Towns-and-Towers-v.1.10-_FORGE-1.19.2_.jar | https://creativecommons.org/licenses/by-nc-sa/4.0/ |
| Upgrade Aquatic | upgrade_aquatic-1.19.2-5.1.0.jar | https://github.com/team-abnormals/upgrade-aquatic/blob/main/LICENSE.txt |
| Villages & Pillages | villagesandpillages-forge-mc1.19.2-1.0.2.jar | CC BY-NC-ND 4.0 |
| wthit | wthit-1.19.2-forge-5.36.1.jar | CC-BY-NC-SA-4.0 |

### 7.4 All Rights Reserved or none declared (104)

Not usable without permission.

| Mod | Jar | Declared licence |
|---|---|---|
| Advanced Peripherals | AdvancedPeripherals-1.19.2-0.7.37r.jar | All rights reserved |
| Alex | alexsabnormalities-1.3.jar | All rights reserved |
| Architect | Architects-Palette-1.19.2-1.3.5.jar | All Rights Reserved |
| Armor Trims Backport | armortrims-1.19.2-1.3.3.jar | All rights reserved |
| Balm | balm-forge-1.19.2-4.6.0.jar | All rights reserved |
| Better Third Person | BetterThirdPerson-Forge-1.19-1.9.0.jar | All rights reserved |
| Blue Flame Burning | blueflame-1.19.2-0.1.0.2.jar | All rights reserved |
| Breedable Killer Rabbit | breedablekillerrabbit-1.19.2-3.5.jar | All rights reserved |
| Chimes | Chimes-v2.0.1-1.19.2.jar | All rights reserved |
| Collective | collective-1.19.2-7.64.jar | All rights reserved |
| Colored Water | Colored Water-1.19.2-1.3.7.jar | All rights reserved |
| CookingForBlockheads | cookingforblockheads-forge-1.19.2-13.3.4.jar | All rights reserved |
| Corpse | corpse-forge-1.19.2-1.0.17.jar | All rights reserved |
| Create Compact Exp (by NoCube) | nocube's_create_compact_exp_1.0.4_forge_1.19.2.jar | All Rights Reserved |
| Create Compressed | create_compressed-2.0.0-forge-1.19.2.jar | All Rights Reserved |
| Create Deco | createdeco-1.3.3-1.19.2.jar | All rights reserved |
| Create Liquid Fuel | createliquidfuel-2.1.1-1.19.2.jar | All Rights Reserved |
| Create Questing | Create_Questing-FORGE-1.19.2-1.0.0.jar | All Rights Reserved |
| Create Recycling | create_recycle_1.0.2_forge_1.19.2.jar | All Rights Reserved |
| Create: Copycats+ | copycats-2.2.2+mc.1.19.2-forge.jar | All Rights Reserved |
| Create: Deep Dark | create_deep_dark-1.7.0-forge-1.19.2.jar | All Rights Reserved |
| Create: Ultimate Factory | create_ultimate_factory-1.9.0-forge-1.19.2.jar | All Rights Reserved |
| Customizable Elytra | customizableelytra-1.19.0-1.7.4.jar | All rights reserved |
| Cycle Paintings | cyclepaintings-1.19.2-3.5.jar | All rights reserved |
| Durability Tooltip | durabilitytooltip-1.1.6a-forge-mc1.19.jar | All rights reserved |
| Easy Elytra Takeoff | easyelytratakeoff-1.19.2-4.2.jar | All rights reserved |
| Effective (Forge) | effective_fg-1.3.4.jar | All rights reserved |
| Effishiency | effishiency-1.19.2-1.1.1.jar | All rights reserved |
| End Portal | endportal-1.19.2-ver1.2.jar | All rights reserved |
| Ender | endersdelight-1.19-1.2.2.jar | All rights reserved |
| Every Compat | everycomp-1.19.2-2.5.36.jar | All rights reserved |
| Everything is Copper | everythingcopper-1.19.2-2.3.4.jar | All rights reserved |
| FarmersStructures | FarmersStructures-1.0.0-1.19.2.jar | All Rights Reserved |
| Fish On The Line | fishontheline-1.19.2-3.2.jar | All rights reserved |
| Floral Enchantment | Floral Enchantment-1.19.2-1.4.3.jar | All rights reserved |
| Forgiving Void | forgivingvoid-forge-1.19-7.0.0.jar | All rights reserved |
| Friendly Griefing | FriendlyGriefing-1.19-1.0.2.jar | All rights reserved |
| Fruits Delight | fruitsdelight-0.5.11.jar | All rights reserved |
| FTB Chunks | ftb-chunks-forge-1902.4.7-build.420.jar | All Rights Reserved |
| FTB Essentials | ftb-essentials-forge-1902.3.5-build.120.jar | All Rights Reserved |
| FTB Library | ftb-library-forge-1902.4.1-build.236.jar | All Rights Reserved |
| FTB Quests | ftb-quests-forge-1902.5.10-build.497.jar | All Rights Reserved |
| FTB Teams | ftb-teams-forge-1902.2.14-build.123.jar | All Rights Reserved |
| Fuel Goes Here | fuelgoeshere-1.19.2-0.1.0.0.jar | All rights reserved |
| Geode+ | geodeforge-1.1.8-1.19.2.jar | All Rights Reserved |
| Geophilic Reforged | GeophilicReforged-v1.2.0.jar | ARR |
| Golems Are Friends Not Fodder | golemsarefriends-1.19.2-0.1.0.1.jar | All rights reserved |
| Guardian Beam Defense | Create-Guardian-Beam-Defense-1.2.3b.jar | All rights reserved |
| Hearths | hearths-v1.0.0-mc1.19u1.19.3.jar | ARR |
| Heartstone | heartstone-1.19.2-1.1.2.jar | All rights reserved |
| HopoBetterUnderwaterRuins | HopoBetterUnderwaterRuins-[1.19-1.19.3]-1.1.1.jar | All Rights Reserved |
| Ice Prevents Crop Growth | icepreventscropgrowth-1.19.2-3.2.jar | All rights reserved |
| Immersive Petroleum | ImmersivePetroleum-1.19.2-4.2.0-29b.jar | All rights reserved |
| Just Enough Effect Descriptions | jeed-1.19.2-2.1.4.jar | ARR |
| Just Enough Painting Previews | jepp-1.19.2-1.1.0.jar | ARR |
| kotlinforforge-3.12.0-all.jar | ? | (no mods.toml) |
| Labels | labels-1.19.2-1.10.jar | All rights reserved |
| Lightman | lightmanscurrency-1.19.2-2.2.1.3g.jar | All rights reserved |
| Lightmans Discord Integration | lightmansdiscord-1.19.2-0.2.0.3-server.jar | All rights reserved |
| loadingscreentips-1.1.0.jar | ? | (no mods.toml) |
| More Mob Variants | moremobvariants-forge+1.19.2-1.3.0.1.jar | All Rights Reserved |
| Mosaic Masonry | mosaic_masonry-1.1.jar | All rights reserved |
| Moyai | moyai-1.19.2-2.0.1.jar | All rights reserved |
| Myrtrees | myrtrees-forge-1.2.0-build.32.jar | All Rights Reserved |
| Mystical Oak Tree | mysticaloaktree-1.19.2-1.10.jar | All rights reserved |
| Neko | NekosEnchantedBooks-1.19-2.0.3.jar | All rights reserved |
| NetherPortalFix | netherportalfix-forge-1.19-10.0.1.jar | All rights reserved |
| Nock Enough Arrows | nock_enough_arrows-1.19.2-1.2.0.jar | All rights reserved |
| Not Enough Recipe Book | nerb-1.19.2-0.3-FORGE.jar | All Rights Reserved |
| OreExcavation | OreExcavation-1.11.166.jar | All Rights Reserved |
| Panorama | panorama-1.3.0 - 1.19.2.jar | All rights reserved |
| Per-Dimension Game Rules | PerDimensionGameRules-1.19.2-forge-1.2.0.jar | ARR |
| Philip | PhilipsRuins1.19.2-10.8.jar | All Rights Reserved |
| Pierce Arrow | piercearrow-1.19.2-2.0.0.jar | All rights reserved |
| Portable Mobs | portablemobs-1.2.1-forge-mc1.19.jar | All rights reserved |
| Quark Delight | quark_delight_1.0.0_forge_1.19.2.jar | All Rights Reserved |
| Quests Additions | questsadditions-1.19.2-1.4.2.jar | All rights reserved |
| Re:Deco | redeco-1.5.0-1.19.2.jar | All rights reserved |
| restApi | restApi - 0.7 - 1.19.2 - Forge.jar | All Rights Reserved |
| Separated Leaves | separatedleaves-2.4.0-forge-1.19.2.jar | All Rights Reserved |
| Serene Seasons | SereneSeasons-1.19.2-8.1.0.24.jar | All Rights Reserved |
| Smaller Nether Portals | smallernetherportals-1.19.2-3.6.jar | All rights reserved |
| Sticky It! | sticky_it-1.2.1-forge-1.19.2.jar | All Rights Reserved |
| SuperMartijn642 | supermartijn642configlib-1.1.8-forge-mc1.19.jar | All rights reserved |
| SuperMartijn642 | supermartijn642corelib-1.1.21-forge-mc1.19.2.jar | All rights reserved |
| Supplementaries | supplementaries-1.19.2-2.4.20.jar | ARR |
| Supplementaries Squared | suppsquared-1.19.2-1.1.1.jar | ARR |
| Survivality | survivality-1.19.2-1.2.4-forge.jar | All Rights Reserved |
| The Dirty Stuff | the_dirty_stuff-2.1-forge-1.19.2.jar | All Rights Reserved |
| Tinted Campfires | Tinted Campfires-1.19.2-1.2.9.jar | All rights reserved |
| Trade Cycling | trade-cycling-forge-1.19.2-1.0.15.jar | All rights reserved |
| Trash Cans | trashcans-1.0.18-forge-mc1.19.jar | All rights reserved |
| TrashSlot | trashslot-forge-1.19.2-12.1.0.jar | All rights reserved |
| Twigs | twigs-forge-1.19.2-3.0.1.jar | All rights reserved |
| Underground Bunkers | UndergroundBunkers-1.0.4-1.19.2-1.20.4-forge.jar | All Rights Reserved |
| Via Romana | Via_Romana-1.19.2-1.1.3.jar | All Rights Reserved |
| Villagers Sell Animals (by NoCube) | nocube's_villagers_sell_animals_1.2.1_forge_1.19.2.jar | All Rights Reserved |
| Volcanic Caverns | volcanic_caverns-1.19.2-1.2.2.jar | All rights reserved |
| Wardrobe | wardrobe-1.0.3-1.19.2.jar | All Rights Reserved |
| Waystones | waystones-forge-1.19.2-11.4.2.jar | All rights reserved |
| Waystones Teleport Pets | WaystonesTeleportPets-1.19.2-1.19.4--1.2.jar | All rights reserved |
| Waystones2Waypoints2 | xaeros_waystones_compatibility-1.1 - 1.19.2.jar | All rights reserved |
| Xaero | xaerominimap-forge-1.19.2-25.3.10.jar | All Rights Reserved |
| Xaero | xaeroworldmap-forge-1.19.2-1.40.11.jar | All Rights Reserved |
