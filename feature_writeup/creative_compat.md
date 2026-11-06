# Creative Compat

Written 2026-11-01 (Creative Split wave 3, [creative_split.md](creative_split.md) 7.6; TODO `creativesplit/compat #5`).
Status: **carved 2026-11-01, installed; scratch SQ-CMP queued, nothing booted.**

Written for: the owner and agents working on the split. Own mod `creativecompat` (development/creativeCompat,
`creativecompat-1.0.0.jar`, side both): the pack's tweaks to third-party mods that have no wider system of their own.
Every other Creative jar requires it (Common <- Compat <- content jars). Same behaviour as before the carve.

## 1. What it holds

- **Java** (`com.SaintAlfonzo.creativecompat`, same subpackages as in patcherMod): `compat/` (banner pattern atlas,
  botanist pot, Polymorph in Sophisticated / vanilla crafting, Supplementaries jar mixing, jar fluid pipes (JarFluidPipes, 2026-11-02), Handcrafted nightstands,
  plant box models, Quark safe sort, sack bundle and scroll, spout gun farmland, Supplementaries flower box, TrashSlot),
  `itemphysic/` (split, burn transform, burn blast damage, Allurement curses),
  `block/` (MossTint, VineShearing), `mobs/MonsterBoxVault`, and the JEI world rows `compat/jei/BurnRows`. The campfire
  code (`campfire/`, `CampfireRainMixin`, `CampfireRows`, `[campfire]`) moved to Creative Delight on 2026-11-04
  (creative_split.md 7.9 step 1).
  Since 2026-11-01 also `cats/` (CatFeather, FeatherAttackGoal, CozyCats: Domestication Innovation's Feather on a
  Stick and Cozy's Improved Cats, [cozy_cats.md](cozy_cats.md) 10) with 5 more gated common mixins (DiFeatherEntity,
  FishingHookFeatherPull, CatCozyCommand, CozyHomeGoalFeather, CozyMessageGender) and a new `[cats]` config table
  (not carried from the old file).
- **Mixins:** `creativecompat.mixins.json`, 32 common + 13 client (2026-11-04; the two client additions are `JetpackItemTooltipMixin`, gated on
  `create_dd`, and the ungated `FallingBlockWaterSplashMixin`, both from Common, member prefix `creativecompat$`), SRG names, `remap = false`, member prefixes still
  `creativeengineering$`. `compat/CompatMixinPlugin` (a `GatedMixinPlugin`) applies each only when its mod is loaded:
  an any-of table plus an all-of table (`NEEDS_ALL`) for the Polymorph pairs (with sophisticatedcore, IE, Pretty Pipes),
  Breezy with Supplementaries, and the Create breaker with Quark. Ungated vanilla targets: own-stack fill, botanist pot
  template, vine shear.
- **Config:** `config/creativecompat-common.toml` (`CompatConfig`): `[leaf_piles]`, `[moss]`, `[vine_shearing]`,
  `[monster_box]`, `[trash_slot]`, keys, comments and defaults unchanged. On the first boot after the
  carve, Common's `ConfigRename.carrySections` copies those tables with their values from
  `creativeengineering-common.toml` into the new file (only when the new file does not exist yet); patcherMod's own
  config correction then drops them from its file. If nothing could be carried, Compat warns about the leftover
  section headers (Calendar's pattern).
- **Resources in the jar** (paths and ids keep the `creativeengineering` namespace): moss tinted models and texture,
  flower box models, the 98 banner pattern textures for Bedspreads and Customizable Elytra, `tags/items/physics/`, 13
  lang keys (MCA eyes, monster box), no painting art (since 2026-11-05 the Immersive Paintings set
  is built at load from the installed jars, `paintings/PaintingPack`, 6.2), the monster box reward loot table, the weathered cardboard box assets.
- **Scripts:** startup `kubejs_startup/creativecompat/` (`item_physics.js`, `decor_blocks.js` weathered cardboard
  box); server `kubejs/data/creativecompat/kubejs/` (coin, dead recipe removal, feature ports, flowers, misc mods,
  starter kit, physics tags, and the Compat rows of curios, feature-port, block and misc tags); client
  `kubejs/assets/creativecompat/kubejs/` (flower pot names, the Misc Mods, Quark, Supplementaries, Framed / Copycat and
  flower pot tooltips). Each tree opens with `_compat_core.js`. The Compat tooltips call the pack's tooltip helpers
  through `global.tooltips` (published by `client_scripts/tooltips.js`), at event time.

## 2. Edges to Common (Common never imports Compat)

| Common reader | Hook Compat fills |
|---|---|
| JEI world tabs (`WorldInteractionJEI`) | `WorldRows.provider(BURNING, ...)` from Compat's constructor when JEI is loaded (CAMPFIRE: Creative Delight since 2026-11-04) |
| `WthitCampfire` | filled by Creative Delight since 2026-11-04 (`campfire/CampfireRainInfo` suppliers); `CampfireRainTicks` accessor stays Common |
| `WthitHolderContents` | `compat/JarContents` kinds / total hooks (fallback: per-slot list) |
| `WthitPotContents` | `compat/PottedPlantHolder`, implemented by the botanist pot block entity |
| `surface/StuckInBlock` (Engineering) | `surface/LeafPileSlow` live reader (default 4) |
| Compat `ItemBurnTransform` -> shrapnel (Engineering) | `util/BurnBursts`, registered by the shrapnel side |

## 3. Left in the pack folder or elsewhere on purpose

- Third-party asset overrides `kubejs/assets/{beautify,supplementaries,lightmanscurrency,wares,prettypipes}`: a mod jar
  cannot reliably sort above the target mod's own jar; they stay in the KubeJS resource pack until a top-position
  built-in pack is set up.
- TrinketCaseRules, its 4 mixins and `[botania]` (Creative Magic, wave 6); salt (Creative Delight, wave 4);
  `layered_obsidian` (Engineering); Botania tooltips; the Vinery flower box tooltip (Creative Brewing).
- Open question: the archery arrow tooltip list moved with Misc Mods; it may belong to Equipment.

## 4. Tests

Scratch SQ-CMP-01 to -08 ([scratch_queue.md](testing/scratch_queue.md)). In game: CMP-01 open a Supplementaries jar
with two potions and read the WTHIT line; CMP-02 (campfire) moved to SQ-CFR / CQ-CFR-01 with the code; CMP-03 TrashSlot
and Polymorph in a Sophisticated crafting upgrade; CMP-04 MCA comb eye colours and a Bedspreads / Elytra banner.

## 5. History

- 2026-11-01: carved (wave 3). patcherMod 1.1.06 loses the classes, mixins, sections and resources; Calendar,
  Devtools, Creative Delight and Creative Brewing require `creativecompat`. Added ModList gates that were missing for
  the spout gun farmland, nightstands, sack scroll, banner atlas and monster box listeners.
- 2026-11-04: campfire rain, Comfort, the Campfire Rules JEI rows and `[campfire]` moved to Creative Delight
  (creative_split.md 7.9 step 1); Compat 33 -> 32 common mixins. Tests SQ-CFR.
- 2026-11-04 (creative_split.md 7.14): from Creative Common, the Create: Dreams & Desires jetpack tooltip null-level
  guard (`JetpackItemTooltipMixin`) and the falling-block water splash (`FallingBlockWaterSplashMixin`): third-party
  and vanilla behaviour fixes, not interface or perf. Common keeps its interface, API and engine/perf rows (2.4).

## 6. Licence audit (2026-11-04, plan_cb_publish.md Step 2)

Audit of `creativecompat-1.0.0.jar` (23:34). Summary row in [creative_brewing.md](creative_brewing.md) 9.2. mods.toml
`GPL-3.0-only`, `creativecompat-LICENSE.txt` present, no NOTICE.

The 950 painting files (475 PNG + 475 JSON from `gen_immersive_paintings.py`; removed 2026-11-05, 6.2): every PNG is another mod's or vanilla's
art upscaled (474 exact after downscaling, one stretched). None is own art; each JSON names the artist.

| Part | Source licence | Verdict |
|---|---|---|
| paintings `rebirth_paintings` (250) | Academic Free License 3.0 (GPL-incompatible per FSF) | fix (notice) or out of the jar |
| paintings `toomanypaintings` (65) | MIT | fix (notice) |
| paintings `darkpaintings` (30) | LGPL 2.1 | fix (notice + licence text) |
| paintings `alexsmobs` (2), `cccbridge` (1, Apache-2.0), `create_things_and_misc` (1, AFL 3.0) | as named | fix (notice) |
| paintings `paintings_pack1` (49) | CC BY-NC 3.0 | remove |
| paintings `handcrafted` (28) | Terrarium Licence | remove |
| paintings `minecraft_style_paintings` (12) | none stated (ARR) | remove |
| paintings `supplementaries` (1) | ARR | remove |
| paintings `buzzier_bees`, `clayworks`, `neapolitan`, `upgrade_aquatic` (6) | Abnormals licence (URL only, not read) | remove unless it allows |
| paintings `minecraft` (30) | Mojang | remove |
| elytra banner and bedspread textures (100) | derived from Botania, IE (Blu's), Alex's Mobs (LGPL), Buzzier Bees banner patterns | fix (check Botania and IE terms, credit) |
| MineColonies banner textures (25) | MineColonies not in the pack; leftover output | remove |
| `weathered_cardboard_box.png` + models | Design and Decor (MIT) recolour | OK: MIT notice added 2026-11-05 |
| `moss_block_tinted.png` | Mojang `moss_block` greyscaled | low ([creative_brewing.md](creative_brewing.md) 9.3) |
| flower box models (3) | reference Beautify / Quark textures, none copied | OK |
| Java, 79 classes (45 mixins), scripts, lang, tags, loot | own | OK |
| `TrashSlotGuiHandlerMixin` `@Overwrite onScreenInit` (about 33 lines) | follows TrashSlot's ARR method | fix (targeted injects) |
| `SupplementariesBreezyCompatMixin` `@Overwrite` (4 lines) | own formula | OK |

Fixes: TODO `licence/compat #1` (paintings), `#2` (banner textures, MineColonies leftovers), `#3` (cardboard notice,
moss, TrashSlot overwrite).

2026-11-05: `NOTICE-thirdparty.txt` added (in the jar as `creativecompat-NOTICE-thirdparty.txt`): credit and licence for
every painting source and banner-pattern source, the Design and Decor MIT notice, and the full MIT, Apache 2.0, AFL 3.0,
LGPL 2.1 and LGPL 3 texts. Credit does not grant redistribution: the remove rows above stand (`licence/compat #1`, `#2`).


### 6.1 Painting options (2026-11-05, for the owner's pick; TODO `licence/compat #1`)

Immersive Paintings reads `data/<ns>/paintings/*.png` as **server data** and sends the images to clients, so whatever
route is picked runs where the datapacks load (the dedicated server, or the integrated server in single player). Every
source mod is installed on both sides ("both" in mod_sides.md), and each jar carries its own `textures/painting/` art, so
Compat can build the set in memory at load from the installed jars (the same idea as CD 40.1, as a data pack). The
dedicated server jar has no vanilla textures, so vanilla paintings can't be read there.

| Source | Paintings | Licence | Ship in a datapack | Build from the installed jar |
|---|---|---|---|---|
| rebirth_paintings | 250 | Academic Free License 3.0 | yes, with notice; keep out of the GPL jar (AFL is GPL-incompatible) | yes |
| toomanypaintings | 65 | MIT | yes, with notice | yes |
| darkpaintings | 30 | LGPL 2.1 | yes, with notice and licence text | yes |
| alexsmobs | 2 | LGPL | yes, with notice | yes |
| cccbridge | 1 | Apache 2.0 | yes, with notice | yes |
| create_things_and_misc | 1 | Academic Free License 3.0 | yes, with notice (not in the GPL jar) | yes |
| paintings_pack1 | 49 | CC BY-NC 3.0 | only non-commercial with credit; CurseForge rewards make it doubtful | yes |
| handcrafted | 28 | Terrarium Licence | no | yes |
| minecraft_style_paintings | 12 | none stated | no | yes |
| supplementaries | 1 | ARR | no | yes |
| buzzier_bees 1, clayworks 1, neapolitan 3, upgrade_aquatic 1 | 6 | Abnormals licence (not read) | no until read | yes |
| minecraft | 30 | Mojang | pack-folder datapack only (low, 9.3 of creative_brewing.md), never a published jar | single player only (no textures in the server jar) |

Totals: 349 can ship with notices; 96 can't ship (49 of them NC-doubtful); all 445 modded ones can be built from the
installed jars, which needs no notices and no licence reading. Vanilla's 30: drop from the set, or keep in a pack-folder
datapack.

### 6.2 Owner pick (2026-11-05; TODO `licence/compat #1`)

Status: **built 2026-11-05, NOT installed** (details and tests in [immersive_paintings.md](immersive_paintings.md)).

- **Modded paintings: built from the installed jars.** No modded painting PNG ships in the Compat jar or the pack.
  At load, Compat scans each installed mod jar's `assets/<ns>/textures/painting/` for registered variants and serves
  them to Immersive Paintings as an in-memory data pack (`data/<ns>/paintings/<id>.png` + json), doing what
  `gen_immersive_paintings.py` does now: upscale to 32 px per block, sizes from the registry, authors from lang /
  DarkPaintings credits / mod name.
- **Extra sources by config.** A list in `creativecompat-common.toml` names more jars or zips to read painting art
  from: painting mods and other jars (for example a resource pack that retextures paintings, or a painting jar not
  installed as a mod). Same layout rule (`assets/<ns>/textures/painting/`); later entries win, as resource packs do.
- **Vanilla's 30: a pack-folder datapack.** Generated pack-side (the generator limited to the `minecraft`
  namespace), never inside a published jar. The dedicated server has no vanilla textures, so it can't be built there.
- Done with it: the 950 files left the Compat jar; `NOTICE-thirdparty.txt` lost the painting credits and the MIT
  (Too Many Paintings), Apache, AFL and LGPL 2.1 texts (only paintings needed them); scratch rows SQ-PA-01 to -04,
  CQ-PA-01, -02. A config-listed source can override art but can't add variants (those need the mod's registry entry).
