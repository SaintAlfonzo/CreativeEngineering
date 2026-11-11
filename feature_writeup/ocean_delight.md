# Ocean's Delight and Oceanic Delight

**Nothing integrated except the fishing net, the caviar names and the Oceanic item dictionary block** (section 5; `item_dict.js`
section "oceanic_delight (47)", TODO `dishes/ocean #1` and `#5`). Since 2026-10-29 all tentacles are removed and dishes take
calamari (shipped 1.1.05; TODO `creativedelight/members #24`; SQ-CM-32 FAILED in scratch r1031 2026-10-31, the fix PASSED r1101 with SQ-CM-35 and is unreleased, section 5.5), so the one-tentacle-tag decision (`mod-additions #5`) is moot.
Ocean's Delight (`oceansdelight-1.0.2-1.19.2.jar`, 22 items) and Oceanic Delight (`oceanic_delight-1.0.1-forge-1.19.2.jar`,
47 items) were dropped into `mods/` on 2026-10-23. Both are native 1.19.2 builds (no port). Decision rows:
`largecontent/mod-additions #5` and the `dishes/ocean` rows in [todo/TODO.md](todo/TODO.md); plans:
[briefs/dishes_ocean.md](briefs/dishes_ocean.md), [briefs/largecontent_mod-additions.md](briefs/largecontent_mod-additions.md).
Boot check `SQ-AD-00`; proposed tests `DA-01` to `DA-08` live in the dishes_ocean brief.

Split out of the former combined add-ons survey (survey written 2026-10-23, planned rows added 2026-10-24).
"Pack" means everything installed in `mods/` plus the Veggies and Vintage ports. Item lists were read from the jars' `assets/*/models/item`, recipes and loot modifiers from `data/`.

Related pages: [ends_delight.md](ends_delight.md), [hostile_delight.md](hostile_delight.md) (guardian food), [miners_delight.md](miners_delight.md) (squid, tentacles).

**Licence:** Ocean's Delight is MIT; Oceanic Delight is All Rights Reserved (owner-supplied 2026-10-25). Oceanic art and code
cannot be copied; its item ids and facts are only referenced (the item dictionary block holds ids, not upstream files).

## 0. At a glance

| Mod | Jar | Version | Items | Loot / worldgen | Verdict so far |
|---|---|---|---|---|---|
| Oceanic Delight | `oceanic_delight-1.0.1-forge-1.19.2.jar` | 1.0.1 | 47 | Squid / elder guardian / salmon drops; wild sea grape worldgen; fishing net | Bigger crop-and-shrimp mod; see section 1 |
| Ocean's Delight | `oceansdelight-1.0.2-1.19.2.jar` | 1.0.2 | 22 | Squid / guardian / elder guardian knife drops | Small guardian-and-fugu mod, most overlapped |

Oceanic Delight lists AppleSkin, JEED, JEI and "delight" as dependency entries; check whether they are mandatory before relying on them.

## 1. Ocean's Delight and Oceanic Delight vs each other and the pack

Batch sibling (not installed): [carnivore.md](archive/carnivore.md) section 2 covers Carnivore's guardian meat, salmon roe, squid and whale meat against these mods.

The pack's own seafood content before these jars is thin: Farmer's Delight (`squid_ink_pasta`, `kelp_roll`, fish
rolls, cooked fish dishes), Miner's Delight 1.1.2 (squid, glow squid, tentacles, stuffed squid; see §4), Alex's Mobs
(`shrimp_fried_rice`, `lost_tentacle`), Upgrade Aquatic (nautilus, squid buckets), Quark (sea pickle potted only),
Canned Goods (canned pufferfish). No other mod adds shrimp, sea grapes, caviar, fugu or guardian food.

| Theme | Ocean's Delight (22 items) | Oceanic Delight (47 items) | Existing | Overlap |
|---|---|---|---|---|
| Squid / tentacles | `tentacles`, `cut_tentacles`, `tentacle_on_a_stick`, `baked_tentacle_on_a_stick`, `squid_rings` | `squid_tentacles` (tag `forge:squid_tentacles`), `grilled_squid_tentacles`, `glow_squid_tentacles`, `squid_salad`, `squid_and_pickles`, `glow_squid_stew` | Miner's Delight 1.1.2 `tentacles`, `baked_tentacles`, `stuffed_squid`, `bowl_of_stuffed_squid`, `glow_squid`, `squid` | **Three separate raw tentacle items** (`oceansdelight:tentacles`, `oceanic_delight:squid_tentacles`, `miners_delight:tentacles`) plus three squid-drop loot modifiers. Needs one shared tag and a decision on which item a squid drops |
| Sea pickle | `braised_sea_pickle` | `sea_pickle_roll`, `sea_pickle_slices`, `sea_pickle_crate`, `squid_and_pickles`, tag `forge:sea_pickles` | Hostile Delight `preparedseapickle`; Unusual Delight `braised_sea_pickle` (1.20.1) | Ocean's `braised_sea_pickle` has the same name as Unusual Delight's |
| Guardian | `guardian`, `guardian_tail`, `guardian_soup`, `elder_guardian_*`, `fugu_roll`, `cabbage_wrapped_elder_guardian` | `elder_guardian_eye`, `pasta_with_eyeball` | Hostile Delight guardian line (about 12 items) | Guardian food exists in three mods |
| Fugu / pufferfish | `fugu_roll`, `fugu_slice` | `plate_of_stuffed_pufferfish`, `stuffed_pufferfish` | Canned Goods `canned_pufferfishs` | Two mods each cook pufferfish; Canned Goods only cans it |
| Stuffed fish | `stuffed_cod`, `cooked_stuffed_cod` | `stuffed_pufferfish` | Miner's Delight `bowl_of_stuffed_squid`, `baked_cod_stew_cup` | Same idea, different fish |
| Takoyaki | none | `takoyaki` | Miner's Delight 1.4.5 adds `takoyaki` (1.21.1 only) | Only if the newer Miner's Delight is ported |
| Unique to Ocean's | `seagrass_salad`, `honey_fried_kelp` | | | Kelp overlaps Farmer's Delight `kelp_roll` only thematically |
| Unique to Oceanic | | shrimp line (`shrimp`, `fried_shrimp`, `shrimp_rice`, `shrimp_roll`, `shrimp_stew`, `shrimp_chips`, `shrimp_slices`), fish eggs / caviar (`salmon_eggs`, `ancient_fish_eggs`, `caviar_toast`, `fish_egg_*`), sea grape crop + wild form (worldgen `patch_wild_sea_grape`), `paella`, `egg_roll`, `sponge_cake`, `nautilus_juice`, `fishing_net` | Alex's Mobs `shrimp_fried_rice` | Shrimp has no source mob in the pack: check where shrimp drop (loot modifier or fishing net) |

Between the two ocean mods: both turn a squid into tentacles, both cut elder guardians (Ocean's with a knife drop via
`farmersdelight:add_item`, Oceanic an eye), and both make a sea pickle dish; the rest is complementary (Oceanic is the
bigger, crop-and-shrimp mod; Ocean's is a small guardian-and-fugu mod). Oceanic Delight adds a wild crop, so it needs
the same treatment the pack's other wild crops got (`vegetation worldgen`, biome fit, the wild crop dictionary).

Open decision: keep both and unify (one squid-tentacle tag across Miner's, Ocean's, Oceanic; one of the three squid
drop modifiers wins), or drop Ocean's Delight (smallest, most overlapped).

## 2. Integration checklist (ocean rows of `mod-additions #5` / `#9`)

## 3. Integration work common to all five (checklist for `mod-additions #4`)

| `mod_sides.md` rows | One row per jar: all five are `both`, none has a library dependency beyond Farmer's Delight (Oceanic: check AppleSkin / JEED / JEI entries) |
| Tag unification | Squid tentacles, shulker meat, sea pickles, shrimp, chorus pie slices; add through `global.tags` |
| Food tuning | Run the new items through the food-tuning rules and the saturation word; the pack's `food_types.json` generator reads `/dumpall` output |
| Cans | Decide which new foods get a can type (`gen_can_types.py`) |
| Stacks / compost | Stack-size policy and compost values for the new crops (sea grape, caviar crates) |
| JEI / names | JEI coverage and missing-names audit |
| Loot | Several loot modifiers target the same mobs (squid x3, shulker x2, enderman x2); check the loot system and the mob-drop rules |
| Worldgen | Wild sea grape (Oceanic): biome/ocean placement through the wild crop framework |
| Advancements | None of these mods may use advancements for unlocks (pack drops them) |
| Done docs | Writeup, test list, changelog bullet (`unreleased.md`) on each integration step |

## 3. Planned (scratchpad 2026-10-24)

Rows added to [TODO](todo/TODO.md) on 2026-10-24.

**Ocean's Delight and Oceanic Delight (section 2; `largecontent/mod-additions #5`)**
- [TODO](todo/TODO.md) `dishes/ocean #1`: fishing net with a cooldown, tooltips and a loot table, drops by biome (shrimp in the
  ocean, boosted; salmon eggs in rivers and lakes). The net is Oceanic Delight's `fishing_net` (the row says Ocean's), with two
  gameplay loot tables today. Investigate the mechanic first.
- [TODO](todo/TODO.md) `dishes/ocean #2`: fish egg roll accepts fish eggs. Open: salmon and ancient fish eggs only, or chicken eggs too.
- [TODO](todo/TODO.md) `dishes/ocean #3`: keep the squid and glow squid items (Miner's Delight); a cut squid gives calamari and ink
  sacs; add glowing calamari, cooked glow calamari and a glow calamari roll. Open: drop rates.
- [TODO](todo/TODO.md) `dishes/ocean #4`: remove the tentacle items of the three mods; any tentacle cuts to calamari. This answers
  the open decision "one squid-tentacle tag" in section 2 (calamari survives).
- [TODO](todo/TODO.md) `dishes/ocean #5`: salmon eggs renamed caviar, with a caviar roll, a caviar noodle soup and caviar in the sea
  pickle roll (the roll and soup already exist as Fish Egg Roll and Fish Egg Noodle Soup).
- [TODO](todo/TODO.md) `dishes/pickles-and-ferments #4`: braised sea pickle (Ocean's Delight) also takes a sliced sea pickle.

**Rolls**
- [TODO](todo/TODO.md) `largecontent/mod-additions #14`: when a rolls Delight mod is added, any roll works in any roll recipe across
  the mods and rolls become placeable blocks. Open: which mod, and whether it is added at all.

## 3.1 Newer jars reviewed (2026-10-25)

Plans only; nothing here is built. The newer jars sit in `DecompiledMods/` as references and are **not** 1.19.2 builds, so
nothing is installed from them: `oceanic_delight-1.0.4-forge-1.20.1.jar` (installed is 1.0.1) and
`oceansdelight-neoforge-1.0.4-1.21.1.jar` (installed is 1.0.2). Both are MIT. Anything adopted is rebuilt in the pack
(KubeJS, or a pack-namespace item), same behaviour, no old-version conversions. The diff was made against the installed jars.

**Unchanged in the newer jars (no work):** Oceanic's fishing net recipe and loot (so section 5.1 stands), caviar toast,
sea grape / sea pickle crates and unpacking, sea grape juice, sponge cake, seafood skewer, sea salad, caesar salad.
Ocean's: the guardian, fugu, elder guardian and stuffed cod recipes only changed form (conditional wrappers dropped,
1.21 tag names); same inputs and outputs.

### Decided 2026-10-25

| Change in the newer jars | Decision | Plan |
|---|---|---|
| Ocean's **Kelp Encrusted Cod** (new): cooking pot, cod slice + egg + kelp | Add it | New item in the pack namespace with the item key in `item_dict.js`; recipe through `global.NCH` (pot, `experience` 0.35). The mod registers it with the stuffed cod food (3 hunger, 0.2) although it defines a separate "kelp crusted cod" value (5 hunger, 0.6): use 5 / 0.6, then let food tuning set the final value. Needs a texture (take it from the 1.0.4 jar, MIT) and a name entry; food type tag, can type, JEI and tooltip per the section 3 checklist |
| Oceanic **JEED Nourishment** list: adds Fish Egg Noodle Soup, Globular Rice, Glow Squid Stew, Shrimp Stew (9 foods, was 5), plus 11 tooltip lines (Nourishment 3:00 / 5:00 on 8 foods, Instant Breath on Sea Grape and Squid Salad) | Update the JEED info | Add the 4 foods to Nourishment in [jeed_providers.js](../kubejs/server_scripts/other/jeed_providers.js) (the pack's place for code-applied food), or override the mod's `jeed_nourishment` provider with the 9 foods if that is simpler; durations come from the 1.0.4 lang lines, check each against the installed item's real effect in game. Tooltips go through the pack's tooltip system ([tooltips.md](tooltips.md)), not the mod's lang keys; JEED text follows [effects.md](effects.md) |
| Oceanic **wild sea grape**: patch tries 32 to 64, spread 5 to 6 | Fold into the pack's own crop placement system | Add a sea grape rule to `wild_crops.json` ([briefs/farming_wild-crops.md](briefs/farming_wild-crops.md), applier `wild_crops.js`) with a density target in the generator's table; use the new 64 / 6 patch as the starting feature, then set density through the pack's placed copy, not the mod's own feature. Remove the mod's own wild sea grape placement so it does not generate twice. Ocean biome fit (warm / cold) to decide in the rule |
| Oceanic **sea grape plant** mineable with a hoe (the fruit already was) | Allow | Add `oceanic_delight:sea_grape_plant` to `#minecraft:mineable/hoe` through `global.tags` (block tags) |

### Not mentioned, needs a decision

| Change in the newer jars | Why it needs a decision | Recommendation |
|---|---|---|
| Ocean's **Stuffed Squid / Cooked Stuffed Squid** (new: tentacles + rice + kelp; furnace / smoker / campfire; Cooked gives Comfort 1:00) | Miner's Delight already has `stuffed_squid` and `bowl_of_stuffed_squid`, and `dishes/ocean #4` removes the tentacle items | Skip; revisit only after `dishes/ocean #4` (the recipe's tentacle input would be calamari) |
| Ocean's **glow squid drops tentacles when cut with a knife** (new loot modifier) | A 4th tentacle source, against `dishes/ocean #4` and `#3` (glow squid cuts to glow calamari) | Skip; glow squid stays on the `#3` calamari plan |
| Oceanic **`forge:tentacles` tag** (new, covers squid and glow squid tentacles); 4 recipes now take it | Only matters if the tentacle items stay | None until `dishes/ocean #4` is decided; if the tentacles are removed, repoint those recipes to calamari instead of adopting the tag |
| Oceanic **fishing net in `#minecraft:pickaxes`** | Other mods treat a pickaxe-tag item as a mining tool | Do not adopt; the net is a search item (section 5.1) |
| Ocean's smelting / smoking / campfire recipes are plain (not conditional) | None | No work |

### Proposed checks (queue when built)

| ID | Check |
|---|---|
| DA-12 | Kelp Encrusted Cod: pot recipe with cod slice, egg and kelp; item name, food values, tooltip and JEI |
| DA-13 | JEED shows Nourishment on all 9 foods with the right duration |
| DA-14 | Wild sea grape generates once (no duplicate patches) at the rule's density in a fresh ocean; scratch run |
| DA-15 | A hoe breaks a sea grape plant and its fruit |

Changelog: no bullet yet (plans only). The bullets come with the build.

## 4. Status

| Item | Status |
|---|---|
| Newer-jar review (section 3.1) | Written 2026-10-25; four changes decided; Kelp Encrusted Cod BUILT 2026-11-02 (section 7, SQ-FCB-00 / -01 PASSED r1102b), the other three not built; TODO rows proposed 2026-10-30: `dishes/ocean #7` (Kelp Encrusted Cod), `#8` (JEED Nourishment list), `farming/wild-crops #6` (sea grape placement), `farming/crops #27` (sea grape plant hoe-mineable) |
| Overlap survey (section 1) | Written 2026-10-23 |
| Integration | Partly started (fishing net, item dictionary); rest is `largecontent/mod-additions #5` and `dishes/ocean` |
| Fishing net, caviar names, squid chain (section 5) | DEPLOYED 1.1.03 (server and players, 2026-10-26); SQ-FN-01, SQ-FN-04 and the squid half SQ-CDB-03, -04 PASSED 2026-10-25; SQ-CDB-05 (glowing calamari recipes, server half) PASSED r1102b; SQ-FN-03 LOOK in r1102b (junk share to judge), SQ-FN-02 (biome loot, group B) not run (deep scan 2026-11-02) |
| Oceanic donor route (section 5.6) | BUILT 2026-11-01 (`portedOceanic` -02 in the CD jar; in `mods/` since the CD jar of 11-02 14:01, not played); needs `oceanic_upstream/` on server and clients; SQ-DNR-O-01 to -05 queued, not run (not in r1102b's scope) |
| Tentacles removed, seagrass salad (section 5.5) | Shipped 1.1.05; SQ-CM-31 PASSED r1031; SQ-CM-32 FAILED r1031 (calamari dishes missing), fix PASSED r1101 (SQ-CM-32, SQ-CM-35), unreleased |
| Test list | Section 6 (DA-01 to DA-03, DA-08 to DA-11); `DA-04` to `DA-07` stay proposed in the dishes_ocean brief; boot check `SQ-AD-00` |

## 5. Built

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): rerun pending (batch fault) SQ-FN-03.
- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-CDM2-01, -03, -04; SQ-CDM2-02 OPEN: no loaded-ground check (91 sand blocks in the box), not a valid sample; hand-check.

### 5.1 Fishing net (2026-10-25, TODO `dishes/ocean #1`)

**Mechanic (decompiled, Oceanic Delight 1.0.1 `FishingNetExtractingProcedure`).** Using the net on a block runs the
procedure on the clicked block. Any use sets a 10 tick cooldown; a successful search sets 50 ticks (2.5 s) and costs one
durability (92 uses; none in creative). Kelp: only a kelp stalk with more kelp above it; 20 % of searches break the stalk,
and 20 % of those spawn a pufferfish. Tall seagrass: 5 % of searches break it. Each search rolls one gameplay loot table
with the block's position as the loot origin. The existing 2.5 s cooldown is the row's cooldown. Oceanic 1.0.4 (in
`DecompiledMods/`) changes nothing here.

**What the pack changes.**
- Blocks: patcherMod `OceanicDelightFishingNetMixin` redirects the procedure's four `getBlock()` reads. A block in
  `#creativedelight:fishing_net/kelp` reads as a kelp stalk, a block in `#creativedelight:fishing_net/sea_grass`
  as tall seagrass. Kelp tag: vanilla kelp and Upgrade Aquatic's ochre, polar, thorny and tongue kelp (top and stalk
  blocks). Sea grass tag: seagrass, tall seagrass, wild sea grape, the sea grape crop, blue and purple pickerelweed (short
  and tall). The "kelp above" rule reads through the same tags. Tags: `server_scripts/tags/loottags.js`.
- Biomes: 1.19.2 `location_check` takes one biome id, so patcherMod adds the loot condition
  `creativeengineering:biome_tag` (`loot/BiomeTagCondition`, reads the loot origin). Biome tags (loottags.js):
  `fishing_net/warm_ocean` (warm, lukewarm, deep lukewarm, Terralith's deep warm), `fishing_net/shrimp_ocean` (those plus
  ocean and deep ocean), `fishing_net/ocean` (`#minecraft:is_ocean` plus deep warm, which Terralith leaves out of it),
  `fishing_net/river` (`#minecraft:is_river` plus Terralith's warm river).
- Tables (`kubejs/data/oceanic_delight/loot_tables/gameplay/`, whole-table overrides; a weight counts only where its
  condition passes):

| Entry | Kelp table | Sea grass table | Where |
|---|---|---|---|
| Nothing | 5 | 2 | everywhere |
| Clay ball | 10 | 5 | everywhere |
| Kelp | 6 | none | everywhere |
| Scute | none | 1 (only 10 % of rolls) | everywhere |
| Shrimp | 4 (was 2 everywhere) | 4 (was 3 everywhere) | `shrimp_ocean` |
| Caviar | 3 (was 1 everywhere) | 3 (was 2 everywhere) | `river` |
| Coral (`creativeengineering:gameplay/fishing_net/coral`) | 3 | 2 | `warm_ocean` |

- Extra pools in both tables: `creativeengineering:gameplay/fishing_net/ocean_treasure` in `fishing_net/ocean` (nautilus
  shell 2 %, heart of the sea 0.2 %, rolled apart), and 5 % of searches anywhere roll `.../fishing_net/junk`. Coral is its
  own table so the corals share one weight (a `tag` entry with `expand` gives every member the full weight); it picks from
  `#creativedelight:fishing_net/coral`: the 15 live coral types (5 vanilla, 10 Upgrade Aquatic), plant or fan, through
  Upgrade Aquatic's per-type tags. Dead and prismarine corals stay out.
- Junk = the pack's loaded vanilla fishing junk table minus: lily pad (a surface plant), water bottle (fishing joke; the net
  fills no bottles), damaged fishing rod (a tool that rivals the net), 10 ink sacs (joke stack; squids drop ink), bamboo
  (jungle-only filler), both pickerelweeds (now net blocks). Kept at vanilla weights: damaged leather boots 10, leather 10,
  bone 10, bowl 10, tripwire hook 10, rotten flesh 10, string 5, stick 5, and Upgrade Aquatic's driftwood log 10 as 1 log
  (was 5). Enchanted books are fishing treasure, not junk, so none.
- Tooltip (`client_scripts/tooltips.js`) and the shrimp JEI page (`namechanges.js`) name the blocks and the biome drops.
- Rates: kelp in a warm ocean gives shrimp 4 / 28 = 14 % (was 8 %), coral 11 %; sea grass in a river gives caviar
  3 / 10 = 30 % (was 15 %). One net (92 searches) in an ocean averages 1.8 nautilus shells and 0.18 hearts of the sea.
- The chest-loot rules (`loot/loot_config.json`) only touch chests, so they do not change these tables.

**Limits.** Seagrass and pickerelweed out of water also count (the procedure does not check for water). The top kelp block
still does nothing. Lakes have no biome of their own, so caviar comes from rivers only (the row said rivers and lakes).
Junk drops everywhere, as in fishing (user's wording could also mean oceans only; one condition to move).

### 5.2 Caviar names (2026-10-25, TODO `dishes/ocean #5`)

`client_scripts/namechanges.js`: Salmon Eggs -> Caviar, Fish Egg Roll -> Caviar Roll, Fish Egg Roll Slice -> Caviar Roll
Slice, Fish Egg Noodle Soup -> Caviar Noodle Soup; ids unchanged (dict key `caviar` for `oceanic_delight:salmon_eggs`). The
salmon eggs JEI page reads as caviar and names both sources (knife-killed salmon, the net in rivers). Ancient Fish Eggs keep
their name. Caviar in the sea pickle roll was deferred by the user to the caviar use audit (TODO `dishes/ocean #6`).

### 5.3 Squid chain and glow calamari (2026-10-25, TODO `dishes/ocean #3`)

- **Drops** (CD `miners_delight/loot.js`, LootJS, which runs after the mods' loot modifiers): a squid drops one
  Miner's Delight squid item every kill, a glow squid one glow squid item; their ink sacs, Rustic's calamari and the
  tentacles of Miner's, Ocean's and Oceanic are taken off the mob (the tentacle items are removed since 2026-10-29, `dishes/ocean #4`, below).
- **Cuts** (CD `miners_delight/recipes.js`, Miner's tentacle cuts removed): squid -> calamari 1 + 50 % 1, ink sac 1 +
  50 % 2 (Rustic's 1-2 calamari drop, Miner's own ink); glow squid -> the same in glow calamari and glow ink sacs; baked
  squid -> cooked calamari 1 + 50 % 1.
- **Glowing calamari line** (`creativeengineering:` glowing_calamari, glowing_calamari_slice, glowing_calamari_roll; CD
  `rusticdelight/glowing_calamari.js`): Rustic's calamari, slice and roll sprites palette-swapped (`textureswapper/palette_swap.py`)
  to the glow ink sac (the roll's meat swapped, Rustic's rice layer put back on top); notice
  `glowing_calamari_art_notice.txt` beside them. Values as the plain line plus Glowing 30 s. No cooked glowing items
  (owner, 2026-10-25): glowing calamari and its slice cook into Rustic's Cooked Calamari and Cooked Calamari Slice (cooking
  trio, CD `rusticdelight/recipes.js`). Knife cut 2 slices + bone meal; roll from 2 raw slices + cooked rice -> 2. In
  `#forge:raw_fishes/calamari`, so Fried Calamari, Calamari Soup and the plain roll take it.
- Open: Rustic's licence file (the sprites are recolours of its art); glowing cans (need `/dumpfood`, then the can generator).

### 5.4 Braised sea pickle from slices (2026-10-25, TODO `dishes/pickles-and-ferments #4`)

CD `oceansdelight/recipes.js`: a second pot recipe, 2 Sea Pickle Slices + 2 brown mushrooms + bowl (Ocean's own
one-pickle recipe stays). Only with Oceanic Delight on (the slices are its item).

### 5.6 Oceanic Delight on the donor route (2026-11-01, TODO `creativedelight/framework #3`, `members #6`)

Owner 2026-11-01 (creative_delight.md 33.1): Oceanic Delight becomes a donor jar, Miner's route. The repack
(`repackedOceanic`, upstream classes and files plus the fishing net mixin) is gone from the Creative Delight jar. In its place
the member `portedOceanic` (`development/creativeDelight/members/oceanic`, `1.0.1-ce-19.2-02`, mod id `oceanic_delight`,
display name "Oceanic Delight (Creative Delight)", GPLv3) holds only our own code, written from the mod's behaviour. Art and
data are read at load from the unmodified upstream jar:

- **Donor:** `oceanic_upstream/oceanic_delight-1.0.1-forge-1.19.2.jar`, sha1 `91fd8842a5bf9b55c2d9fb39f721129bc6cae1b5`, from
  https://cdn.modrinth.com/data/O1qboAbB/versions/9JkMCuAa/oceanic_delight-1.0.1-forge-1.19.2.jar (the same file the repack
  used). Pinned in `upstream/UpstreamJars`; the game stops with a message naming the folder, the file and the URL when it is
  missing or different. Shipped through `UPSTREAM_DONORS` in `tools/build_deploy.py` (server and every client).
- **Packs:** `oceanic_upstream_assets` / `oceanic_upstream_data`, built in, always on, at the bottom (`upstream/OceanicTranslator`).
  Upstream is a native 1.19.2 build with the same ids, so its files are used as they are, except: the English lang gains our
  two keys (`message.oceanic_delight.needs_bowl`, `tooltip.oceanic_delight.effect`); advancements are left out (the pack drops
  them anyway); the two upstream fishing net tables are left out (the net rolls the pack's tables, below).
- **Ids:** the 47 items, 8 blocks (same block state properties), Instant Breath, the three Mining Fatigue potions and the loot
  modifier type `oceanic_delight:oceanic_delight_loot_modifier` keep upstream's ids, so old worlds and upstream's data load
  unchanged. `tools/check_donor.py` (in the member) compares them with the last dump and the donor (0 problems 2026-11-01).
- **Behaviour kept:** food values, stack sizes, rarities and eating times; containers (bowl, glass bottle, stick, nautilus
  shell); Comfort / Nourishment 3:00 or 5:00 on the eight dishes (dropped from the code 2026-11-02, section 8: the pack's Comfort / Nourishment rule sets them now), Instant Breath on Sea Grape, Sea Grape Juice and Squid Salad,
  and their blue tooltip lines; the sea grape crop (grows 1 tick in 10 into still water above, the third plant tops out with a
  fruit, bone meal adds a plant), wild sea grape, the two placed dishes (bowl needed, four servings; the paella pan stays empty
  and is picked up by using it), the sponge cake (knife cuts a slice, else eat a slice: 3 hunger, 1 saturation, Water Breathing
  0:30), the crates; the fishing net (5.1: same cooldowns, durability, break and pufferfish chances, attributes); compost
  chances; the Mining Fatigue brewing steps; the wandering trader's Salmon Eggs; the six JEI information pages.
- **Fishing net, now our own code** (`content/FishingNetItem`): it reads `#creativedelight:fishing_net/kelp` and
  `/sea_grass` directly (the mixin and its `getBlock` redirect are gone) and rolls `creativedelight:gameplay/fishing_net/kelp`
  and `/sea_grass`. Those tables, with their `coral`, `ocean_treasure` and `junk` sub-tables, moved out of the pack
  (`kubejs/data/oceanic_delight/loot_tables/gameplay/`, `kubejs/data/creativeengineering/loot_tables/gameplay/fishing_net/`) to
  `kubejs/data/creativedelight/loot_tables/gameplay/fishing_net/`, shipped in the CD jar (`dataTrees`). Contents unchanged
  except the sub-table ids (`creativedelight:` for `creativeengineering:`). The tag fill is in CD
  `oceanic_delight/fishing_net_tags.js` (moved earlier the same day). So `members #6`'s move is complete.
- **Small differences** (none a player should see): the extra effect goes to the eater (upstream ran `/effect give @p`,
  the nearest player); a paella or stuffed pufferfish placed without properties starts full (upstream started empty unless
  placed by a player); the net sound plays for everyone near (upstream: the user only); the sponge cake caps hunger at 20
  (upstream could pass it); a net used as a crafting tool keeps its enchantments; the "Why'd you hit me" advancement is not
  granted (the pack drops it); Mining Fatigue brewing is three Forge recipes per bottle type (JEI lists them itself).

**Integration pass (`members #6`, second half).** Squid drops: settled and built (one squid item per kill, tentacles
filtered out; CD `miners_delight/loot.js`, `dishes/ocean #3`, `#4`). Takoyaki: settled and built (raw calamari, 5.5).
**Guardian food** (Oceanic Elder Guardian Eye against Ocean's and Hostile Delight's guardian lines) has no decision in
`mod-additions #5`: open, nothing built.

Tests: SQ-DNR-O-01 to -05 (scratch queue). Old-world check: a 1.1.03+ world with every Oceanic block placed.

## 6. In-game tests

| ID | Setup | Action | Expected |
|---|---|---|---|
| DA-01 | Survival, a fishing net, kelp in a warm ocean and in a cold ocean | Search each about 30 times | Warm: shrimp and coral (plant or fan) drop; cold: no shrimp, no coral; clay and kelp in both |
| DA-02 | Kelp or seagrass in a river | Search about 20 times | Caviar drops; no shrimp, no coral |
| DA-03 | Hover the net; search twice quickly | Read the tooltip; watch the cooldown | Three tooltip lines (blocks, biome drops, ocean treasure); 2.5 s cooldown after a search |
| DA-05 | Survival, a sword and a knife | Kill squids with each; cut a squid item on a board | One squid item per kill, no ink or calamari; the cut gives calamari and ink sacs |
| DA-06 | Glow squids | Kill one; cut the item; cut, cook and roll the glowing calamari | Glow squid item; glowing calamari and glow ink sacs; slices, Cooked Calamari from cooking, and 2 rolls |
| VTD-58 | A cooking pot | 2 sea pickle slices + 2 brown mushrooms + a bowl | Braised sea pickle |
| DA-08 | JEI | Look up Caviar, the roll, the slice, the noodle soup; open the Caviar and Shrimp info pages | Names read Caviar, Caviar Roll, Caviar Roll Slice, Caviar Noodle Soup; Ancient Fish Eggs unchanged; the pages name the knife, the net and the biomes |
| DA-09 | Upgrade Aquatic kelp (any colour) with kelp above; tall seagrass; wild sea grape; pickerelweed | Search each | Kelps give the kelp table (kelp item, clay); the others the sea grass table (no kelp item, scute possible) |
| DA-10 | A stack of nets, an ocean | Search about 200 times | A few nautilus shells; a heart of the sea is rare; junk about 1 in 20; never a lily pad, water bottle, rod or ink sacs |
| DA-11 | `latest.log` after start and `/reload` | grep `fishing_net`, `biome_tag`, `oceanic_upstream` | No loot table parse error; both upstream packs listed (the mixin is gone since 5.6) |


### 5.5 Tentacles removed, seagrass salad (2026-10-29, TODO `dishes/ocean #4`, `creativedelight/members #24`)

- The ten tentacle items of Miner's (`tentacles`, `baked_tentacles`, `tentacles_on_a_stick`), Ocean's (`tentacles`, `cut_tentacles`,
  `tentacle_on_a_stick`, `baked_tentacle_on_a_stick`) and Oceanic (`squid_tentacles`, `glow_squid_tentacles`, `grilled_squid_tentacles`)
  are in `removed_items.js` (hidden, tags stripped). Their recipes are removed by output and input (CD `oceansdelight/recipes.js`,
  `miners_delight/recipes.js`). Hostile Delight's ghast tentacle and Alex's Mobs' lost tentacle are not touched.
- Dishes: Squid Rings takes raw calamari in place of cut tentacles; Glow Squid Stew raw calamari (the tag holds glowing and every slice); Sea Pickle Roll, Shrimp Rice,
  Squid Salad and Takoyaki raw calamari (were `#forge:squid_tentacles`); Seafood Skewer and Squid and Pickles cooked calamari (the grilled tentacle slot).
- Fix 2026-10-31 (SQ-CM-32 FAILED in scratch r1031): the removal by input ran before the swaps and took Squid Rings,
  Glow Squid Stew, Seafood Skewer and Squid and Pickles; it now runs after them. The tentacle tags are empty once the items
  are stripped, so a `replaceInput` on them cannot match (NCH error at boot); instead raw calamari is added to
  `#forge:squid_tentacles` (Oceanic's four dishes) and `#forge:foods/tentacles` (Miner's Takoyaki, two slots) in CD
  `oceansdelight/tags.js`. Rebuilt and installed 2026-10-31 12:32; recheck SQ-CM-32 and SQ-CM-35.
- Seagrass Salad: 2 seagrass + bowl.
- Not done: generated files still list the removed items (`food_types.json`, the can and compost outputs); the generators drop them on the next `/dumpall` and `/cdevgen` run.

## 7. Built 2026-11-02 (food and cooking batch, [plan_food_cooking_batch.md](plan_food_cooking_batch.md))

- Kelp Encrusted Cod (3.1, TODO `dishes/ocean #7`): `creativeengineering:kelp_encrusted_cod`, CD startup
  `farmersdelight/ocean_dishes.js`, 5 / 0.6, pot cod slice + any egg + kelp, 0.35 xp, 200 ticks; sprite from the 1.0.4
  jar (MIT, notice beside it), food type meal. DA-12; SQ-FCB-00, -01.
- Squid drops (TODO `dishes/ocean #9`): CD `miners_delight/loot.js` now removes every drop of a squid or glow squid and
  adds one squid item, whatever the weapon; no longer only while Rustic Delight is on. SQ-FCB-04.
- Scratch r1102b (2026-11-02): SQ-FCB-00, -01 and -04 (50 kills each of squid and glow squid, knife and bare) PASSED; CQ-FCB-02 (cod sprite, JEI) waits for the client round. Not played.

## 8. Built 2026-11-03 (Creative Delight members run, plan_creative_delight.md L: `farming/crops #27`, `farming/wild-crops #6`, `dishes/ocean #8`)

- **Sea grape plant and the hoe** (`farming/crops #27`, DA-15): CD `oceanic_delight/tags.js` puts `oceanic_delight:sea_grape_plant`
  in `#minecraft:mineable/hoe` through `global.tags.mineable` (block tag; the fruit already was). `/reload`.
- **Wild sea grape under the pack's placement** (`farming/wild-crops #6`, DA-14): rule `sea_grape` in `wild_crops.json` (`add` the
  mod's three oceans: warm, lukewarm, deep lukewarm; `requires` oceanic_delight), pack features
  `creativedelight:wild/sea_grape` (configured: the 1.0.1 patch rewritten with the newer jar's 64 tries, spread 6, was 32 / 5;
  placed: 1 in 30 chunks as the mod's, OCEAN_FLOOR_WG, no count), the mod's `patch_wild_sea_grape_biome_modifier` replaced by
  `forge:none`. Starting density 4.2 patch attempts per 1000 overworld chunks (the mod's own rate; the bigger patch makes each
  attempt denser); a tuning value for `farming/wild-crops #1`. `tools/gen_crop_worldgen_table.py` now reads the Oceanic donor
  jar and counts ocean area for sea-floor features; `tools/test_wild_crops.sh` passes (its Rhino harness pointed at the moved
  `_biome_rules.js`). New chunks only, rejoin.
- **Comfort / Nourishment on the Oceanic dishes** (`dishes/ocean #8`, DA-13): rechecked against the rule (comfort_nourishment.md 5,
  11). The port applied Comfort / Nourishment in code (`OceanicFood` bonus), outside the rule, and its bowls hand back the bowl
  in code, so the dump showed no container and the rule never ruled them. Built: the nine Comfort / Nourishment bonuses removed
  from `OceanicItems` (Instant Breath kept); `tools/gen_comfort_nourishment.py` treats the 13 Oceanic bowl foods as bowls
  (`CODE_CONTAINERS`) and the paella and stuffed pufferfish servings as feast servings (`CODE_FEASTS`). The effects are now
  FoodProperties, so JEED lists them by itself (no `jeed_providers.js` row needed). Result (dump of 10-30):

| Food | Was (code) | Now (rule) |
|---|---|---|
| Fish Egg (Caviar) Noodle Soup | Comfort 5:00 | Comfort 4:00 |
| Globular Rice | Comfort 5:00 | Nourishment 4:00 |
| Glow Squid Stew | Comfort 3:00 | Comfort 2:00 |
| Shrimp Stew | Comfort 5:00 | Nourishment 4:00 |
| Pasta with Eyeball | Nourishment 5:00 | Nourishment 4:00 |
| Squid and Pickles | Nourishment 3:00 | Nourishment 4:00 |
| Shrimp Rice | Nourishment 3:00 | Nourishment 4:00 |
| Bowl of Paella | Nourishment 5:00 | Comfort 4:00 |
| Plate of Stuffed Pufferfish | Nourishment 5:00 | Comfort 4:00 |
| Squid Salad | none | Comfort 1:00 |
| Sea Salad | none | none |
| Potato Salad, Caesar Salad | none | merged away (item_merges.md 2.15, 2.17) |

  The newer jar's 11 tooltip lines are not adopted (the pack's tooltips show the food's real effects). Restart (startup data and
  the member jar).

Install: `portedOceanic-1.0.1-ce-19.2-02` and the CD scripts are in the CD jar installed 2026-11-03 00:08; not booted (last boot
2026-10-31). Scratch rows (queue section SQ-CDJ7): SQ-CDM2-01 (DA-15), SQ-CDM2-02 (DA-14), SQ-CDM2-03 (DA-13), SQ-CDM2-04
(potato salad merge, item_merges.md 2.17); client row CQ-CDM2-01 (Oceanic dish tooltip).


## 9. Planned (scratchpad 2026-11-04)

Owner notes for both mods, together with the Hostile Delight notes ([hostile_delight.md](hostile_delight.md) 5.1). None built
(checked 2026-11-04: Hostile Delight's own guardian loot table is still live, no guardian, fugu or guardian soup code in the pack).

- **Guardian Shell** ([TODO](todo/TODO.md) `dishes/ocean #11`): guardians drop Ocean's whole guardian item, renamed Guardian
  Shell; Prismarine Shards and Crystals still drop. Cutting the shell gives 1 to 3 raw guardian fillets and a chance of a
  Prismarine Shard. Replaces D3's "3 fillets" in hostile_delight.md 5.
- **Guardian Soup** (`dishes/ocean #12`): Ocean's soup only (Hostile's removed), from a Guardian Shell, 2 guardian fillets, a
  tomato, an onion and an Elder Guardian Eye.
- **Fugu** (`dishes/ocean #13`): eating any fugu food has a 0.5% chance of Wither III and Poison III for 10 s.
- **Elder guardian chunk** (`dishes/ocean #14`, open): a source for the elder guardian chunk; the mob-drop JEI tab updated with it.
