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
  `itemphysic/` (split, burn transform, burn blast damage, Allurement curses), `campfire/` (comfort, rain),
  `block/` (MossTint, VineShearing), `mobs/MonsterBoxVault`, and the JEI world rows `compat/jei/BurnRows`,
  `CampfireRows`.
  Since 2026-11-01 also `cats/` (CatFeather, FeatherAttackGoal, CozyCats: Domestication Innovation's Feather on a
  Stick and Cozy's Improved Cats, [cozy_cats.md](cozy_cats.md) 10) with 5 more gated common mixins (DiFeatherEntity,
  FishingHookFeatherPull, CatCozyCommand, CozyHomeGoalFeather, CozyMessageGender) and a new `[cats]` config table
  (not carried from the old file).
- **Mixins:** `creativecompat.mixins.json`, 27 common + 10 client, SRG names, `remap = false`, member prefixes still
  `creativeengineering$`. `compat/CompatMixinPlugin` (a `GatedMixinPlugin`) applies each only when its mod is loaded:
  an any-of table plus an all-of table (`NEEDS_ALL`) for the Polymorph pairs (with sophisticatedcore, IE, Pretty Pipes),
  Breezy with Supplementaries, and the Create breaker with Quark. Ungated vanilla targets: own-stack fill, botanist pot
  template, vine shear, campfire rain.
- **Config:** `config/creativecompat-common.toml` (`CompatConfig`): `[leaf_piles]`, `[moss]`, `[vine_shearing]`,
  `[campfire]`, `[monster_box]`, `[trash_slot]`, keys, comments and defaults unchanged. On the first boot after the
  carve, Common's `ConfigRename.carrySections` copies those tables with their values from
  `creativeengineering-common.toml` into the new file (only when the new file does not exist yet); patcherMod's own
  config correction then drops them from its file. If nothing could be carried, Compat warns about the leftover
  section headers (Calendar's pattern).
- **Resources in the jar** (paths and ids keep the `creativeengineering` namespace): moss tinted models and texture,
  flower box models, the 98 banner pattern textures for Bedspreads and Customizable Elytra, `tags/items/physics/`, 13
  lang keys (MCA eyes, monster box), the Immersive Paintings set (`data/creativeengineering/paintings/`, 950 files,
  written by `tools/gen_immersive_paintings.py`), the monster box reward loot table, the weathered cardboard box assets.
- **Scripts:** startup `kubejs_startup/creativecompat/` (`item_physics.js`, `decor_blocks.js` weathered cardboard
  box); server `kubejs/data/creativecompat/kubejs/` (coin, dead recipe removal, feature ports, flowers, misc mods,
  starter kit, physics tags, and the Compat rows of curios, feature-port, block and misc tags); client
  `kubejs/assets/creativecompat/kubejs/` (flower pot names, the Misc Mods, Quark, Supplementaries, Framed / Copycat and
  flower pot tooltips). Each tree opens with `_compat_core.js`. The Compat tooltips call the pack's tooltip helpers
  through `global.tooltips` (published by `client_scripts/tooltips.js`), at event time.

## 2. Edges to Common (Common never imports Compat)

| Common reader | Hook Compat fills |
|---|---|
| JEI world tabs (`WorldInteractionJEI`) | `WorldRows.provider(BURNING / CAMPFIRE, ...)` from Compat's constructor when JEI is loaded |
| `WthitCampfire` | `campfire/CampfireRainInfo` (rain tags, douses and out-ticks suppliers); `CampfireRainTicks` accessor stays Common |
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
with two potions and read the WTHIT line; CMP-02 rain on a lit campfire (douses, JEI Campfire tab rows); CMP-03 TrashSlot
and Polymorph in a Sophisticated crafting upgrade; CMP-04 MCA comb eye colours and a Bedspreads / Elytra banner.

## 5. History

- 2026-11-01: carved (wave 3). patcherMod 1.1.06 loses the classes, mixins, sections and resources; Calendar,
  Devtools, Creative Delight and Creative Brewing require `creativecompat`. Added ModList gates that were missing for
  the spout gun farmland, nightstands, sack scroll, banner atlas and monster box listeners.
