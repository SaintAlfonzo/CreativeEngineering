# Log warnings and errors: what shows up every launch

A catalogue of the warnings and errors that appear in every client startup, from any mod: what each message is,
which mods produce it, whether it matters, and how to acknowledge it. Written 2026-10-24 from a real client startup
(`logs/latest.log`, 18071 lines, about 5700 WARN/ERROR lines; the two earlier 24 Oct launches match within a handful of lines).
Section 10 adds a dedicated server startup on 1.2.00 (2026-11-03), split into pack-side and mod-side.

Three files work together:

| File | What it is |
|---|---|
| this file | The explanation: every repeated message, its cause, its verdict, the acknowledge options |
| [log_warnings_lists.md](log_warnings_lists.md) | **Generated**: the full per-mod, per-model lists (which chair, which flag, which plate) for hunting a texture down |
| [log_warnings_ack.txt](log_warnings_ack.txt) | Your acknowledgements: one line per (category, mod) you have decided to ignore; the generator marks those groups ACKNOWLEDGED |

Refresh the lists after a launch: `python3 tools/log_warnings_scan.py` (reads `logs/latest.log`, rewrites the lists file).

**Rule of thumb:** a startup with only what is listed here is healthy. Anything not listed, or a category whose count
jumps, is worth a look. Crashes, `Error occurred while handling event` and `Exception in thread` are never noise.

## 0. At a glance

| # | Message (shortened) | Lines | Cat. key | Biggest sources | Verdict |
|---|---|---|---|---|---|
| 1 | `Exception loading blockstate definition: '<mod>:blockstates/<block>.json' missing model for variant: ...` | 2950 | `blockstate` | redeco 1327, colored_water 1024, displaydelight 552, create_dd 48 | Ignore (A) |
| 2 | `Unable to resolve texture reference: #missing in <model>` | 1302 | `texref` | redeco 941, interiors 306, vintageimprovements 24, alexsabnormalities 12, vinery_backport 10 | Mixed: check in game (B) |
| 3 | `Unable to resolve texture due to reference chain X->X in <model>` | 897 | `chain` | flowerpatch 585, create_crystal_clear 144, gates 132, immersive_weathering 36 | Ignore (C) |
| 4 | `Using missing texture, file <ns>:textures/... not found` (ERROR) | 118 | `missingfile` | supplementaries 37, bedspreads 25, customizableelytra 24, minecraft 9 | Ignore (D) |
| 5 | `Unable to load model: '<model>' referenced from: <block>#...` | 90 | `modelload` | displaydelight 72, supplementaries 6, alexsabnormalities 4, blocky_siege 3 | Mixed (E) |
| 6 | `Leaked resource: '...mtl' loaded from pack: 'Mod Resources'` + `java.lang.Exception` trace | 56 | n/a | immersiveengineering 45, immersive_paintings 10 | Ignore (F) |
| 7 | `Reference map '<x>.refmap.json' for <x>.mixins.json could not be read` | 35 | n/a | about 35 mods | Ignore (G) |
| 8 | `defineId called for: class X from class Y` + `RuntimeException: null` trace | 40 | n/a | MCA 38, babyMobs 2 | Ignore (H) |
| 9 | `Missing sound for event: ...` | 14 | n/a | twigs 10 | Ignore |
| 10 | `DetailArmorBarCompat: '...' is not a registered ArmorItem, skipping` | 12 | n/a | our patcherMod | Ignore (expected) |
| 11 | Mixin conflicts: `Shift.BY`, `@Final should be final`, `@Redirect conflict`, `Static binding violation`, `Method overwrite conflict` | about 30 | n/a | see 4.4 | Ignore |
| 12 | `Error loading class: ... ClassNotFoundException` / `@Mixin target ... was not found` | 8+3 | n/a | mixin plugins probing for optional mods | Ignore |
| 13 | `Failed to process update information` + `HttpConnectTimeoutException` | 7 | n/a | Forge update check | Ignore |
| 14 | `Could not determine mod trust worthiness, Assuming Jar was downloaded from trusted source!` (FATAL) | 3 | n/a | commonality | Ignore; the level is wrong |
| 15 | `Mod file .../fmlcore, javafmllanguage, lowcodelanguage, mclanguage ... is missing mods.toml file` | 4 | n/a | Forge | Ignore |
| 16 | `Assets URL 'union:...' uses unexpected schema` | 2 | n/a | Forge / client jar | Ignore |
| 17 | `ATM Icon Type '...' was registered twice` | 3 | n/a | Lightman's Currency | Ignore |
| 18 | `Registry ...: The object ... has been registered twice for the same name` | 2 | n/a | treechop:chop_wood, ae2:portable_mana_cell | Ignore; watch if it grows |
| 19 | `Registry entry listened ... was not realized!` | 3 | n/a | pollen:custom, etched:bard | Ignore |
| 20 | `Failed to load integration for mekanism` / `powah`; `Successfully loaded integration for create` / `botania` (WARN level) | 4 | n/a | a Create integration mod | Ignore |
| 21 | `Plugin ...GameStagesIntegration does not have required mod gamestages loaded, skipping` | 1 | n/a | KubeJS | Ignore |
| 22 | `Specified invalid page id in system property guideDev.ae2guide.startupPage` (ERROR) | 1 | n/a | AE2 guide | Ignore |
| 23 | `Couldn't parse data file mysticaloaktree:talked193 from mysticaloaktree:tree_wisdom/talked193.json` (ERROR) | 1 | n/a | mysticaloaktree | Ignore; one tree-lore line is lost |
| 24 | `Couldn't get pack info for ... LegacyCoins` + `Custom Resource Pack of 'lightmanscurrency/LegacyCoins failed to load properly!` | 2 | n/a | Lightman's Currency | Ignore |
| 25 | `ResourcePack: ignored non-lowercase namespace: .DS_Store in .../Lower Shield.zip` | 2 | n/a | your resource pack | Fixable (I) |
| 26 | `Configuration file .../philips-ruins-common-configs.toml is not correct. Correcting` | 2 | n/a | Philip's Ruins | Ignore; self-heals |
| 27 | `Failed to find custom wood type twigs:bamboo` | 1 | n/a | Supplementaries / Twigs | Ignore |
| 28 | `[WTHIT] Plugin ...Waystones / CookingForBlockheads ... is using the default namespace` | 2 | n/a | those two mods' WTHIT plugins | Ignore |
| 29 | `Found problematic active MixinExtras instance at ca.fxco.memoryleakfix.mixinextras` + `limited support` | 2 | n/a | memoryleakfix | Ignore |
| 30 | `Config field without expected type, will not validate list content` | 2 | n/a | Forgiving Void | Ignore |
| 31 | `Texture ... limits mip level`; `Model ... deprecated "flip-v"`; `Invalid rotation` model | 5 | n/a | various models | Ignore |

KubeJS has its own set (section 5).

## 1. Reading the numbers

- A line count is not a problem count. Each broken model repeats once per block-state variant, per load pass. The
  redeco shelves are 9 woods at 144 lines each. Use the **unique** column in the generated lists for the real size.
- The log level is the mod author's choice. `ERROR` for a missing flag texture and `FATAL` for "trust worthiness" do
  not mean something is broken.
- The purple/black checkerboard only appears if that exact block state or item is ever shown. Classes A, C, D are
  states that cannot happen or textures that are never drawn. B and E are the ones that can show in game.

## 2. The repeated resource messages in detail

Each class below has: the exact message shape, what the game is doing when it prints it, the cause, and which mods.

### A. Blockstate variant has no model (item 1, 2950 lines, 129 blocks)

```
Exception loading blockstate definition: 'redeco:blockstates/oak_shelf.json' missing model for variant:
'redeco:oak_shelf#east=false,facing=north,north=true,rotating=true,south=true,waterlogged=true,west=false'
```

The game builds every combination of a block's properties and looks each one up in the block's blockstate JSON. A
combination with no entry has no model, so it is reported. The message does not say the block is broken; it says the
JSON does not list that combination.

| Mod | Lines | Blocks | Cause |
|---|---|---|---|
| redeco | 1327 | 18 | Nine `*_shelf` blocks at 144 lines each (acacia, birch, crimson, dark_oak, jungle, mangrove, oak, spruce, warped) plus `*_stepping_stones` x2 each. The shelf blockstate lists `facing/north/south/east/west` variants only; the block also has `rotating` and `waterlogged`, so any state with them set has no entry. Models and the blockstate files exist in the jar; the variants are just incomplete. |
| colored_water | 1024 | about 64 | `*_fluid_block`, `luminous_*_fluid_block`, `condense_*_fluid_block` (16 lines each; 48 and 32 for some luminous ones). Fluid blocks carry a `level` property; the blockstates have no model per level. Fluids draw through their fluid renderer, so these models are never used. |
| displaydelight | 552 | 20+ | `ed_plated_*`, `cd_plated_*`, `df_plated_*`, `plated_*`: plated-food blocks at `stacks=N` values (3 to 5) with no model. See class E; these are the same blocks. |
| create_dd | 48 | 1 | `modular_drill`: some variants unlisted. |
| nock_enough_arrows | 2 | 1 | one block |
| myrtrees | 2 | 1 | one block |

Hunting one down: open the mod's jar, `assets/<mod>/blockstates/<block>.json`, and compare its `variants` keys with
the block's properties. Fix by adding the missing keys through a resource override in `kubejs/assets/<mod>/blockstates/`.
Worth doing only for blocks players place (shelves, plates), not for fluid blocks.

### B. `#missing` texture reference (item 2, 1302 lines, 1316 model names)

```
Unable to resolve texture reference: #missing in redeco:block/light_blue_upholstered_warped_chair_back_6
```

A model has a texture slot (the `#name`), and nothing fills it: the model itself, its parent and its children never
define that variable. The slot renders the checkerboard. `#missingno` is the same message with a different slot name.

| Mod | Lines | What is affected |
|---|---|---|
| redeco | 941 (221 unique) | Fences (`*_plank_fence_east/north/south/west`) and upholstered chairs (`<colour>_upholstered_<wood>_chair_back_<n>`) and `redeco:item` equivalents (16). Example from the jar: `acacia_plank_fence_east` supplies textures `0`, `3` and `particle` to a parent that also reads another slot. Likely a real visual flaw on the fence connections and chair backs. |
| interiors | 306 (306 unique) | `block/chair/<colour>_chair_{none,left,right,both}` and the `_cropped` forms, `block/floor_chair/...` (128), 34 item models. Every colour of chair and floor chair. Likely a visual flaw unless the textures come from a block-entity renderer (not verified). |
| vintageimprovements | 24 | `block/belt_grinder`, 16 of them from the `#missing` slot. Belt grinder block is a block-entity model; check in game. |
| alexsabnormalities | 12 | whale tooth doors (`cachalot_whale_tooth_door_*`), uses `#missingno`. |
| vinery_backport | 10 | `block/wine_box/*` - our own port; this one is ours to fix. |
| veggiesdelight 5, oceanic_delight 5, vinery 4, ae2 3, extendedgears 2, create_things_and_misc 2, create_dd 1, createaddition 1 | | one-off models |

To confirm what is visible, place or hold the block. If it looks right, acknowledge the group.

### C. Self-referencing texture chain (item 3, 897 lines)

```
Unable to resolve texture due to reference chain cross->cross in flowerpatch:block/patch
```

A model says `"cross": "#cross"`: it points a slot at itself. It is the same family of problem as B, except the game
knows it is a loop. These are template/parent models that are meant to be overridden by a child, and are never
rendered on their own.

| Mod | Lines | Models |
|---|---|---|
| flowerpatch | 585 | `block/patch`, `patch2`, `patch3`, `patch4` (the flower patch parents) |
| create_crystal_clear | 144 | `block/encased_cogwheel/block`, `block_top`, `block_bottom`, `block_top_bottom` (slot `particle`) |
| gates | 132 | `block/sensor/basic_detector` (slot `top`) |
| immersive_weathering | 36 | `block/frosty_tinted_cross` (slots `frosty`, `grass`) |

### D. Texture file not found (item 4, 118 lines, ERROR level)

```
Using missing texture, file supplementaries:textures/entity/flags/botania/sword.png not found
```

The model or atlas names a PNG that is not in any loaded pack. Mostly these are patterns generated for an *optional
mod's* content the mod ships entries for.

| Mod | Lines | What |
|---|---|---|
| supplementaries | 37 | flag patterns for mods (`flags/botania/*`, `flags/immersiveengineering/*`, `flags/alexsmobs/brazil`) and globes (`items/globes/globe_leftchaotix`, `globe_drong`) |
| bedspreads | 25 | banner-pattern textures for other mods (`entity/botania/spark_*`, `entity/immersiveengineering/*`) |
| customizableelytra | 24 | `entity/elytra_banner/*` (`hoe`, `hammer`, `wolf_r`, ...) |
| minecraft | 9 | `particle/block/dead_bush`, `item/enchanted_golden_apple` and others |
| create_dd | 6 | `hydraulic_encased_cogwheel_side` and others |
| create | 4 | `chute_block`, `brass_casing_inner` |
| merequester 3, gates 2, dragonloot 2, davebuildingmod 1, oceanic_delight 1, myrtrees 1 (`rubberwood_log_side`), expandeddelight 1, immersiveengineering 1, immersive_weathering 1 | | one-offs |

The first three rows tie to the banner pattern compat in [fixes_compat.md](fixes_compat.md) (2.2): those mods enumerate
every registered pattern and ask for a texture for patterns from mods that never provided one for that item. The
pattern then draws as the checkerboard on that bed or wing only. Check a custom banner on a Bedspreads bed if you care.

### E. Model file not found (item 5, 90 lines, 89 models)

```
Unable to load model: 'displaydelight:block/ctd_plated_rice_ball_6' referenced from: displaydelight:ctd_plated_rice_ball#facing=north,stacks=6: java.io.FileNotFoundException
```

A blockstate points at a model path that does not exist.

| Mod | Lines | Cause |
|---|---|---|
| displaydelight | 72 | The `<prefix>_plated_<dish>` blocks, where `<prefix>` is `ad_` `cd_` `ctd_` `ed_` `df_`: the blockstate lists `stacks=2` to `stacks=6` but the dish ships fewer stack models (for example `ctd_plated_avocado_toast_2` to `_5`, `ad_plated_bison_burger_2`/`_3`). A plate loaded with that many servings has no model. Plausibly visible; see section 3. |
| supplementaries | 6 | |
| alexsabnormalities | 4 | |
| blocky_siege | 3 | `minecraft:cannon`, `minecraft:roaster_base`, `minecraft:brazier`: models resolved in the `minecraft` namespace instead of the mod's. A mod bug; the block (cannon, spewer stand, brazier) renders as the checkerboard, which is likely visible. |
| oldguns, decorative_blocks, create_things_and_misc, createframed, appliedcooking, cookingforblockheads | 1 each | |

### F. Leaked resource (item 6, 56 lines)

```
Leaked resource: 'immersiveengineering:models/block/balloon.mtl' loaded from pack: 'Mod Resources'
java.lang.Exception
	at ...FallbackResourceManager$LeakedResourceWarningInputStream.<init>
	at ...IEOBJLoader.getStream(IEOBJLoader.java:93)
```

A mod opened a resource stream (here OBJ material `.mtl` files) and did not close it before the garbage collector
ran. The `java.lang.Exception` trace is the warning's own stack, printed to show where the stream was opened; it is not
a crash. Sources: immersiveengineering 45, immersive_paintings 10, immersivepetroleum 1. A leak of a few small files, not a perf concern.

### G. Missing refmap (item 7, 35 lines)

```
Reference map 'moyai-forge-refmap.json' for moyai.mixins.json could not be read. If this is a development environment you can ignore this message
```

Mixin could not find the mod's `refmap.json`. In a production pack the mods were built with the refmap folded into the
class names, so mixins still apply. The message's own text says it is safe in a development environment; here it is
harmless too because those mods work. Seen for about 35 mods (labels, moyai, nerb, suppsquared, jerm, myrtrees,
mysticaloaktree, separatedleaves, wares, yungsextras, kubejs_botania, kubejsdelight, design_decor/tfmg, mes, ...). If a
named mod's mixin features visibly do nothing, this is the first line to check.

### H. defineId (item 8, 40 lines)

```
defineId called for: class net.minecraft.world.entity.animal.Squid from class com.SaintAlfonzo.baby_mobs.age.BabyAges
java.lang.RuntimeException: null
	at net.minecraft.network.syncher.SynchedEntityData.m_135353_(SynchedEntityData.java:46)
```

Forge's `SynchedEntityData.defineId` warns when a data parameter is registered for entity class X by a class that
is not X. The `RuntimeException: null` is a stack trace carrier, not a failure.

- babyMobs (2): our own mod registers an age parameter on vanilla `Squid` and `Dolphin` from `BabyAges.init`. Intended.
- MCA (38): `CDataParameter` 32, `CEnumParameter` 6, MCA's generic data-parameter wrappers for `VillagerEntityMCA`. Intended.

## 3. Cases worth looking at in game (the play audit starting list)

These are the entries that could show a visible purple/black block. Do each once and then acknowledge it with the
verdict.

| # | Where | What to do |
|---|---|---|
| 1 | redeco fences (class B) | Place a redeco plank fence and join two: does a side look black? |
| 2 | redeco upholstered chairs (B) and interiors chairs / floor chairs (B) | Place one of each; sit in it |
| 3 | vintageimprovements belt grinder (B) | Place one |
| 4 | Display Delight plates (E) | Fill an `ad_`/`cd_`/`ctd_`/`ed_`/`df_` plate to 2, 3, 4, 5, 6 servings |
| 5 | blocky_siege cannon, spewer stand, brazier (E) | Place them (if obtainable) |
| 6 | redeco shelf, rotating or waterlogged (A) | Place a shelf in water |
| 7 | vinery_backport wine boxes (B) | Place one (our own port) |
| 8 | alexsabnormalities whale tooth door (B) | Place one |
| 9 | Banner patterns on a Bedspreads bed / Customizable Elytra (D) | Apply a pattern from a non-vanilla mod |

Add the rest of this file's "during play" pass (section 7) at the same time.

## 4. Other repeated messages in detail

### 4.1 Forge and launcher level

- `Mod file ... is missing mods.toml file` (4): Forge's own language jars; they are libraries, not mods.
- `Assets URL ... uses unexpected schema` (2): the client jar is on a `union:` virtual file system.
- `Failed to process update information` (7) with `HttpConnectTimeoutException`: the update checker could not reach
  its URL (offline, slow or blocked). Not related to the pack.
- `Could not determine mod trust worthiness` (3 FATAL): the commonality mod ("Loaded 564 BlackListed sources") cannot
  verify a jar and continues. The same level appears on every start.

### 4.2 Registration

- `has been registered twice for the same name` (`treechop:chop_wood` sound, `ae2:portable_mana_cell` menu): a mod
  registers the same id twice; the second wins. If it ever starts naming a different id, treat it as new.
- `Registry entry listened ... was not realized!` (`pollen:custom`, `etched:bard`): a registry listener fired for an
  entry that was never created. Unused.
- `ATM Icon Type ... was registered twice` (3): Lightman's Currency adds the same ATM icon type for each of three entries.

### 4.3 Missing optional mods

Mixin plugins and integrations ask whether a mod is loaded and log when it is not:
`Error loading class: me/shedaniel/rei/...` (REI), `.../sodium/...` (Sodium), `com.mrcrayfish.backpacked...`,
`atomicstryker.multimine...`, `com.teammetallurgy.aquaculture...`, `earth.terrarium.botarium.forge.BotariumForge`,
`net.orcinus.overweightfarming...`, `com.min01.archaeology...`; `@Mixin target ... was not found` for treechop and
create_central_kitchen; `Failed to load integration for mekanism / powah`; `GameStagesIntegration ... does not have required mod gamestages`.
None of those mods is installed; the pack does not need them.

### 4.4 Mixin warnings

`@Inject(@At("INVOKE")) Shift.BY=2 ... exceeds the maximum allowed value: 0` (everycomp LootTableHackMixin, allurement
ItemEntityMixin, better_beacons), `@Final field ... should be final` (better_beacons), `@Redirect conflict. Skipping
securitycraft.mixins.json:camera.ServerPlayerMixin->@Redirect::securitycraft$tick ... already redirected by
railways-common` (the SecurityCraft camera tick redirect is skipped because Railways has it), `Static binding violation:
PRIVATE @Overwrite ... rubidium`, `Method overwrite conflict for setMaxUses` (asynclocator vs MoreJS) and `getPalettes`
(immersiveengineering vs MoreJS), `Found problematic active MixinExtras instance at ca.fxco.memoryleakfix.mixinextras`.
The conflict ones mean one of two mods did not get its mixin applied; the first mod loaded wins. Only worth following
up if the feature of the losing mod is broken (SecurityCraft camera behaviour in a Railways world is the one case here).

### 4.5 Our own mod lines

- `DetailArmorBarCompat: '<id>' is not a registered ArmorItem, skipping` (12): patcherMod's armor-bar compat skipping
  non-armor entries in its list. Expected.

## 5. KubeJS logs (`logs/kubejs/startup.log`, `server.log`, `client.log`)

These are separate from `latest.log` and are where pack-script problems appear.

| # | Message | Log | Cause | Verdict |
|---|---|---|---|---|
| K1 | `[fluid_dict]` / `[block_dict] Tag entry "<key> -> #<tag>" is not validated (tag support not yet implemented)` (about 20) | startup | Our dictionaries do not check tags at that stage | Ignore |
| K2 | `[item_dict] Missing/invalid item IDs:` then a list (35 entries on this run) | startup | Dead keys in `item_dict.js`: IE `jei_bucket_{bioethanol,ethanol,memory_essence,yeast_culture}`, `tmted:melon_juice`/`tomato_sauce`, `more_immersive_wires:rs_{coil,connector,relay}`, `everycomp:{af,fd,hc}/vinery/cherry_{drawer,cabinet}` and others | Real, low priority: prune or leave |
| K3 | `sample_syringe: JEI world rows not added: InternalError: TypeError: redeclaration of var $WorldRows` | startup, `func/sample_syringe.js#71` | The class is loaded twice in one script; the syringe's JEI world rows are not registered | **Real bug of ours**; fix |
| K4 | `Failed to parse recipe '<id>'! Falling back to vanilla: ItemStack 'result' can't be empty / Ingredient can't be empty / Recipe component key 'cookingtime' not found` (24 on the first 24 Oct launch) | server | rusticdelight `cherry_blossom_*` (result empty) and `cooking/*coffee*` (FD recipe key); other mods | Ignore: those recipes do not exist |
| K5 | `Error parsing recipe <id>[<type>]: ... <reason>` | server | `dramaticdoors:tall_*_casing_door` (empty pattern), `design_decor:item_application/{millstones,crushing_wheels}/special_aluminum_*` (`Unknown item design_decor:aluminum_boiler`), `create:kjs_*[create:deploying]` with fluid `minecraft:empty` (4, scripted), `fulleng:block_requester_terminal_from_part`; seen 2026-10-20: `creativeengineering:fermenting/*` Brewin' and Chewin' recipes with `Fluid Ingredient amount must be at least 1` | Mod-side ones: ignore. Ours (`kjs_*` deploying, `fermenting/*`): check if they still appear |
| K6 | `[wild-crops] N overworld biomes are not in wild_crops.json and match no rule: terralith:cave/crystal...` | server | Our `worldgen/_biome_rules.js` | Informational |
| K7 | `[loot-tuning] wares:agreement/wandering_trader/rare_ware: boosts ask for more than the pool has` | server | Our loot tuning | Informational |

`server.log` is rewritten on each world load. The 24 Oct session where the K4/K5 counts came from was overwritten by a
later load while this was written, so re-check K4, K5 against a fresh `server.log` before acting on them.

## 6. Ways to acknowledge a warning you will not fix

From least to most change. Pick per class; the default is 1 and 2.

| # | Option | What it does | Cost / risk | Fits | Where |
|---|---|---|---|---|---|
| 1 | **Acknowledge in the ack file** | A `category namespace # reason` line in [log_warnings_ack.txt](log_warnings_ack.txt); `tools/log_warnings_scan.py` marks that group ACKNOWLEDGED in the lists. Nothing changes in game | None | Any of classes A to E per mod (you can ack redeco shelves and keep hunting interiors) | repo |
| 2 | **Mark a verdict in this file** | Change a row's verdict to "Ignore" / "Fixed" / "Reported" with a date | None | Messages that are not in the categories (items 6 to 31, K-rows) | this file |
| 3 | **Filter the lines out of the log** | A Log4j2 `RegexFilter` (or `MarkerFilter`) so the lines never reach `latest.log` / console. The game still does the same thing. The launcher passes `-Dlog4j.configurationFile=.../assets/log_configs/client-1.19-debug.xml`; copy that XML into the repo, add filters, and point the Java argument at the copy | Edit the launcher's Java arguments (the instance setting is hand-edited, so back it up under `configs_edited/`); filtering hides a real regression of the same shape; filter by exact message prefix only, never by level | The biggest blocks, A to D (about 5400 lines of the 18000), the `Leaked resource` traces, the refmap lines | launcher setting + a repo copy |
| 4 | **Fix with a resource override** | Add the missing blockstate / model / texture file in `kubejs/assets/<mod>/...` (or a pack) | Time per mod; you maintain it across updates | Cases that show in play (section 3) | `kubejs/assets/` |
| 5 | **Fix in the mod's config** | A few mods log because of their own config (Philip's Ruins rewrites an invalid TOML each launch; Forgiving Void list types) | Low | Config warnings only | `config/` + `configs_edited/` |
| 6 | **Patch the mod with a mixin** | Fix the cause from patcherMod | High; version-pinned; needs a [fixes_compat.md](fixes_compat.md) entry | A warning that is a real bug players see (blocky_siege `minecraft:` model path) | patcherMod |
| 7 | **Remove or replace the mod** | The warnings go with the mod | Loses the content | A mod with a visible bug and no better fix | `mods/`, `mod_sides.md` |
| 8 | **Report upstream** | The author fixes it; take the update later | No control over timing | Bugs in many logs: redeco shelf variants, interiors chairs, display delight stack models | the mod's tracker |

What does not work: no setting turns off Mixin conflict messages, Forge's `mods.toml` / trust / assets lines, or the
`defineId` trace; only option 3 hides them.

**Recommended default:** acknowledge by group (option 1), fix only what you can see in play (option 4), and use option 3
only if the log size becomes a problem.

### Log size

`logs/debug.log` reached 129 MB in one start and `latest.log` 3.3 MB. About 5500 of the 18000 `latest.log` lines are
classes A to E, and a large block of INFO lines of the form `Wrote N byte class file ... to /tmp/classDump...` comes from
a mod that dumps classes at startup (not a warning, but the same cost). Filtering these (option 3) is the cheapest way
to shrink both files.

## 7. During play (not yet done)

This list covers startup only. **To do: play audit.** After a long session, and again for a dedicated server,
repeat the scan on `logs/latest.log` and extend this file with a "during play" table:

- which startup lines repeat in play (they should not),
- per-tick or per-chunk spam (the usual cause of very large logs),
- errors that fire on actions (opening a machine screen, placing a structure, a recipe reload, `/reload`),
- the section 3 visible-in-game checks.

Quick pass:

```
grep -E "/(WARN|ERROR)\]" logs/latest.log | sed -E 's/^\[[0-9:]+\] //; s/\[[^]]*\/(WARN|ERROR)\]/[\1]/' \
  | sed -E "s/'[a-z0-9_]+:[^']*'/'ID'/g; s/[a-z0-9_]+:[a-z0-9_\/.]+/ID/g; s/[0-9]+/N/g" | cut -c1-120 | sort | uniq -c | sort -rn | head -60
```

Logged in [todo/scratchpad.md](todo/scratchpad.md) as "LW-01 play audit".

## 8. How this was gathered

```
python3 tools/log_warnings_scan.py                       # lists + totals per mod
grep -c "" logs/latest.log                               # size
grep -E "WARN|ERROR" logs/kubejs/startup.log logs/kubejs/server.log | cut -c1-200
```

The three 24 Oct launches (`logs/2026-10-24-1.log.gz`, `-2.log.gz`, `latest.log`) and the 20 Oct `debug/10_20/kubejs/server.log`
were compared; the WARN/ERROR line counts were 5722, 5722 and 5728 for the three 24 Oct logs.

## 9. Fixable now (small)

| Item | Fix |
|---|---|
| K3 sample syringe `$WorldRows` redeclaration | Load the Java class once in `func/sample_syringe.js` (line 71) |
| `.DS_Store` in `resourcepacks/Lower Shield.zip` (item 25) | `zip -d "resourcepacks/Lower Shield.zip" .DS_Store` (a user file, not done) |
| K2 dead `item_dict` keys | Prune the 35 ids |
| vinery_backport `block/wine_box/*` `#missing` | Our own port; fix the model textures |

## 10. Dedicated server startup, 1.2.00 (2026-11-03)

Source: `debug/11_03/latest.log` (3922 lines, 1149 WARN/ERROR/FATAL lines) and `debug/11_03/debug.log`, a successful
`CreEngServer` start on 1.2.00 loading the existing world (`winecraft`, made on 1.1.x). Load time 110.8 s (ModernFix line);
one `Can't keep up! ... 47 ticks behind` on the first tick (normal after a world load). No `Error occurred while handling
event`, no `Exception in thread`.

A server log has none of the resource classes A to F (no models or textures on a server). In their place are the
world-upgrade lines (10.1), the client-class lines (10.3) and Every Compat (10.4).

**Owner** column: **Pack** = our scripts, data or in-tree mods, or a change we made; **Pack-triggered** = the message
comes from another mod or Forge, but our change or script causes it; **Mod** = another mod alone, or Forge.

### 10.1 At a glance

| # | Message (shortened) | Lines | Owner | Source | Verdict |
|---|---|---|---|---|---|
| S1 | `Registry minecraft:<item/block/fluid>: Object did not get ID it asked for. Name: <id> Expected: N Got: N` | 433 | Pack-triggered | 1.2.00 added and reordered ids (vintagedelight 185, kubejs 53, creativeengineering 58, barbequesdelight 36, cookingforblockheads 38, largemeals 21, roll_delight 20, displaydelight 13, a few FD / Rustic / Nether's) against the world's saved id map | Ignore. Forge remaps by name; numeric ids are only internal. Appears once, on the first load after an update |
| S2 | `Forge Mod Loader detected missing registry entries` / `Missing minecraft:item:` / `There are unidentified mappings in this world` | 1 block (26 items, 2 blocks) | Pack | Ids that 1.2.00 removed (10.2) | Intended. Those items are deleted from the world on load. All are in the 1.2.00 changelog |
| S3 | `Missing data pack mod:creative_delight`, `mod:creative_brewing`, `mod:enders_additions` | 3 | Pack | The world's `level.dat` lists the 1.1.x pack ids; the mods were renamed (no underscore: `creativedelight`, `creativebrewing`) and Ender's additions moved. The new `*_upstream_data` packs (enders, goldenfood, largemeals, oceanic, roll) are "Found new data pack ... loading it automatically" on the next line | Ignore; one time per old world. Gone once the world saves |
| S4 | `Adding duplicate value 'net.minecraftforge.common.world.NoneBiomeModifier@...' to registry` (ERROR) | 69 | Pack + Mod | Every `forge:none` biome modifier after the first. Forge registers the same `NoneBiomeModifier` object once per file and vanilla logs each repeat. 15 are ours (`kubejs/data/campsite_structures` 6, `bushierflowers` 5, `fruitsdelight` 1, the 3 Mooblooms `buzzier_bees` packs); the other 54 are not in any mod jar's data as plain JSON, so they come from mods that return `forge:none` in code when a feature is turned off | Ignore. Harmless: a `forge:none` modifier does nothing by design. The level is wrong |
| S5 | `RuntimeDistCleaner: Attempted to load class <client class> for invalid dist DEDICATED_SERVER` (ERROR): `PoseStack` 36, `AbstractClientPlayer` 15, `HumanoidModel` 6, `RenderType` 4, `ItemTransforms$TransformType` 3, `ClientLevel` 1 | 66 | Pack-triggered | Nearly all fire while KubeJS startup and server scripts run (next to `potions.js`, `golden_values.js`, `toast.js`, `vintage_items.js`, `tags/plantabletags.js`): Rhino reflects every method of a Java class a script touches, and some mod classes have client types in method signatures without `@OnlyIn`. Forge refuses the load, Rhino skips that method | Ignore. Nothing a script calls is lost (the client-only methods cannot run on a server anyway). Only follow up if a script on the server reports a missing method |
| S6 | `EventSubclassTransformer: Could not find parent net/minecraft/client/particle/TextureSheetParticle for class uwu/lopyluna/create_dd/...` + `An error occurred building event handler` + trace | 2 + trace | Mod | Create: Dreams & Desires has a client particle class in common code | Ignore |
| S7 | `EveryCompat: Failed to generate recipe for <module>/<mod>/<block>: recipe template returned null` (ERROR) | 53 (26 ids) | Mod | Every Compat has no template recipe for: myrtrees rubberwood (af, db, dd, fd, sd, tw modules), botania livingwood / dreamwood (dd doors, q posts), upgrade_aquatic driftwood posts, quark bamboo beam | Ignore. Those Every Compat blocks have no crafting recipe; check one in JEI if anyone asks for them |
| S8 | KubeJS `Failed to parse recipe '<id>'! Falling back to vanilla: ...` | 14 | Mod | `dramaticdoors:short_{copper,andesite,brass}_casing_door`, `short_stripped_bamboo_door`, `short_quark_blossom_door` (ingredient empty); `fulleng:part_requester_terminal_from_block`, `create_things_and_misc:schematic_chair`, `copper_scaffolding_craft`, `creategoggles:module_goggle_unit`, `design_decor:stonecutting/metals/iron/{bolt,screw}` (result empty); `hostiledelight:grollrecp`, `grolllrecp` (shapeless recipe written with the wrong key); `create_netherless:soul_sand_from_sequenced_assembly` (transitional item empty) | Ignore: KubeJS falls back to vanilla parsing, which drops them the same way. Updates K4/K5: the 1.2.00 server shows none of ours (no `kjs_*` deploying, no `fermenting/*`, no rusticdelight) |
| S9 | `TagLoader: Couldn't read tag list minecraft:mineable/{hoe,shovel} ... in data pack redeco-1.5.0-1.19.2.jar` + `Not a JSON object: null` trace | 2 + traces | Mod | Redeco ships two malformed tag files | Ignore. Redeco blocks in those two lists mine at hand speed with a hoe/shovel; the rest of the tag loads from other packs |
| S10 | `Couldn't load tag [mekanism:mekasuit_helmet (from creategoggles-...)] as it is missing following references` | 1 | Mod | Create Goggles references Mekanism, not installed | Ignore |
| S11 | `Error setting up the JDA` + `InvalidTokenException: The provided token is invalid!` (ERROR) | 1 + trace | Server config | Lightman's Discord has no valid bot token in the server's config | Ignore, or set the token / remove the server-side jar if the Discord link is not wanted |
| S12 | `Configuration file ./winecraft/serverconfig/oldguns-server.toml is not correct. Correcting` + 4 `Incorrect key ... was corrected from [X] to its default, [X]` | 5 | Mod | Old Guns list values fail Forge's list validation and are rewritten to the same values each start (same pattern as Philip's Ruins, item 26, also present here) | Ignore; self-heals every start |
| S13 | `quark: Non-existant flag elder_prismarine_underground_biome being used` | 1 | Mod | A Quark recipe condition names a flag Quark no longer has; not in our data | Ignore |
| S14 | `@Redirect conflict. Skipping securitycraft.mixins.json:camera.ChunkMapMixin ... already redirected by railways-common` | 1 | Mod | Second SecurityCraft camera redirect lost to Railways (server side of the 4.4 case) | Same as 4.4: only matters if SecurityCraft cameras misbehave near Railways conductors |
| S15 | `[mod names] <modid> is not loaded; '<name>' not applied` (WARN) | 13 | Pack | patcherMod `ModDisplayNames`: display names for client-only mods (xaerolib, xaerozoomout, transition, notenoughanimations, just_enough_beacons, immediatelyfast, entityculling, enchdesc, ctm, create_questing, conditional_mixin, boatview360, betterf3) | Expected on a server. Fixable: log at INFO when the mod is client-only (10.3) |
| S16 | `[golden values] minecraft:pumpkin_pie is not a food` (KubeJS startup WARN) | 2 | Pack | `creativedelight` `goldenfood/golden_values.js#41`: the golden and enchanted golden pumpkin pie copy vanilla Pumpkin Pie's food values, but in the pack Pumpkin Pie has no food properties at that point, so those two keep their own values | Real, low priority (10.3) |
| S17 | `[item_dict] Missing/invalid item IDs:` (23 entries) | 1 block | Pack | Dead keys in `item_dict.js`. Same as K2, now 23 [35]: IE `jei_bucket_{bioethanol,ethanol,memory_essence,yeast_culture}`, `tmted:{melon_juice,tomato_sauce}`, `more_immersive_wires:rs_{coil,connector,relay}`, `everycomp:{af,fd,hc}/vinery/cherry_*`, `dramaticdoors:{short,tall}_vinery_cherry_door`, `vinery:king_danis_wine`, `vinery:mead`, `physics_gun_mod:*` (4), `ricksportalgun:*` (3), and `create_questing:blueprint` (client-only mod, so expected on a server) | Real, low priority: prune (10.3) |
| S18 | `[fluid_dict]` / `[block_dict] Tag entry ... is not validated` | 53 | Pack | Same as K1 | Ignore |
| S19 | `defineId called for: ... Squid / Dolphin from class com.SaintAlfonzo.baby_mobs.age.BabyAges` | 2 | Pack | Baby Mobs (Creative Worldgen member); same as item 8 | Ignore |
| S20 | `defineId called for: ... VillagerEntityMCA / GrimReaperEntity` | 38 | Mod | MCA; same as item 8 | Ignore |
| S21 | Already in section 0 and unchanged: refmaps (31, item 7), mixin optional-mod probes (`Error loading class` 9 and `@Mixin target ... was not found` 4: multimine, backpacked, aquaculture, botarium, sodium, overweightfarming, plus two client classes), `Shift.BY` / overwrite conflicts (11), `mods.toml` (4, item 15), trust worthiness (3 FATAL, item 14), `Assets URL` (2, item 16), update check (3, item 13), ATM icons (3, item 17), registered twice (2, item 18), Advanced Peripherals integrations (4, item 20), GameStages plugin (item 21), Forgiving Void list types (2, item 30), Philip's Ruins config (item 26) | about 80 | Mod | as listed there | Ignore |
| S22 | `KubeJS: Plugin ...BuiltinKubeJSForgeClientPlugin does not load on server side`; `oldguns` banner lines and `Random UUID`; `Distant Horizons: G1 Garbage collector detected` | 5 | Mod | Informational at WARN level | Ignore |

Count by owner (WARN/ERROR/FATAL lines, traces excluded): **Pack** about 90 (S2, S3, S15 to S19 and our 15 of S4);
**Pack-triggered** about 500 (S1 433, S5 66), all harmless; **Mod** about 560. Nothing in this log is a crash risk.

### 10.2 Items the 1.2.00 upgrade deleted from the world (S2)

The world's saved id map had these; 1.2.00 does not register them, so any in chests, inventories or placed are gone.
Every one is a deliberate 1.2.00 removal with a changelog bullet:

| Ids | 1.2.00 changelog bullet |
|---|---|
| `miners_delight:copper_cup` and its 18 `*_cup` items, `delightful:cactus_soup_cup` | "The Copper Cup and every cup ... and the Cactus Soup Cup" |
| `cookingforblockheads:azalea_spice_rack`, `azalea_tool_rack` (item and block) | "The plain Spice Rack, Tool Rack and Fruit Basket ... Woods with no rack of their own make none" |
| `creativeengineering:canned_chips` | "PneumaticCraft's Fries and Canned Chips, with their recipes" |
| `creativeengineering:canned_mhadjeb` | "Canned Mhadjeb" |
| `rusticdelight:pancake`, `rusticdelight:fried_dough` | "Rustic Delight's plain Pancake"; "Fried Dough" |

Unlike the jams, bottles and Fish and Chips (which have Forge remaps to their replacements and do not show here), these have
no replacement, so Forge drops them. If a future release removes items players may be holding, a remap to a near item
(memory `forge-missing-mappings-staging`) avoids the loss.

### 10.3 Pack-side follow-ups (all low priority, none player-visible except the pie values)

| Item | Fix |
|---|---|
| S16 golden pumpkin pie values | `golden_values.js` reads Pumpkin Pie's food after the pack has taken it away. Read a fixed base (or the pie slice) for `pumpkin_pie`, or drop that pair from the table |
| S17 dead `item_dict` keys | Prune the 22 ids for mods not in the pack; keep `create_questing:blueprint` but skip it on a dedicated server |
| S15 `[mod names]` on a server | Skip, or log at INFO, when `FMLEnvironment.dist` is the dedicated server and the mod is client-only in `mod_sides.md` |
| S4 our 15 `forge:none` files | Nothing to fix; the lines are Forge's. Could be removed only by disabling those features another way (config or mixin, as Bushier Flowers already does) |

### 10.4 How this was gathered

```
grep -E "/(WARN|ERROR|FATAL)\]" debug/11_03/latest.log | sed -E 's/^\[[^]]*\] //' \
  | sed -E "s/'[^']*'/'X'/g; s/[0-9]+/N/g" | cut -c1-170 | sort | uniq -c | sort -rn
```

The S4 sources: our files by `grep -rl '"forge:none"'` over `kubejs/data` and `development/`; the mod jars were scanned
for `forge/biome_modifier/*.json` containing `forge:none` (none found). The S5 context: the lines before each
`RuntimeDistCleaner` line in `latest.log`. The S2 list: the `Missing minecraft:item:` block, checked against
`kubejs/assets/creativeengineering/changelog/1.2.00.md`. The server's own `logs/kubejs/*.log` were not included in
`debug/11_03/`, so section 5 (K-rows) is only re-checked through the KubeJS lines that reach `latest.log`.
