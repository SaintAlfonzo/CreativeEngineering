# Farmer's Delight add-ons added 2026-10-23: overlap investigation

Investigation written 2026-10-23; **nothing integrated**. The five jars were dropped into `mods/` on 2026-10-23 and have
no `mod_sides.md` row, no tags, no recipe rules, no food tuning, no names/JEI pass. This file is the overlap survey
(item lists read from the jars' `assets/*/models/item`, recipes and loot modifiers from `data/`); the decisions are the
rows `largecontent/mod-additions #4` to `#9` in [todo/TODO.md](todo/TODO.md).

"Pack" below means everything installed in `mods/` plus the Veggies and Vintage ports.

## 0. At a glance

| Mod | Jar | Version | Items | Loot / worldgen | Verdict so far |
|---|---|---|---|---|---|
| End's Delight | `ends_delight-1.19.2-2.1.jar` | 2.1 | 64 | Dragon, shulker, enderman, endermite drops (loot modifiers); 1 worldgen file | Overlaps Ender's Delight on chorus and shulker; see §1 |
| Hostile Delight | `Hostile_Delight_0.2.2_1.19.2.jar` | 0.2.2 | 69 | No loot modifiers, no worldgen | Mostly standalone; heavy overlap with Unusual Delight (§5) |
| Miner's Delight | `miners_delight-1.19.2-1.1.2.jar` | 1.1.2 | 37 | Squid, glow squid, bat wing, silverfish eggs drops | **OLD** (§4); 1.4.5 for 1.21.1 is the newer line |
| Oceanic Delight | `oceanic_delight-1.0.1-forge-1.19.2.jar` | 1.0.1 | 47 | Squid / elder guardian / salmon drops; wild sea grape worldgen | See §2 and §3 |
| Ocean's Delight | `oceansdelight-1.0.2-1.19.2.jar` | 1.0.2 | 22 | Squid / guardian / elder guardian knife drops | See §2 and §3 |

All five need Farmer's Delight (Oceanic Delight lists AppleSkin, JEED, JEI and "delight" as dependency entries; check
whether they are mandatory before relying on them). None of them is a port target on 1.19.2: all five are native
1.19.2 builds, so no port is needed for them (the port rule applies only to the 1.20/1.21-only mods, §4 and §5).

## 1. End's Delight vs Ender's Delight (installed: `endersdelight-1.19-1.2.2`)

Both are "End food" add-ons. Neither depends on the other, they do not share ids, and each brings its own stove.

| Theme | Ender's Delight (existing, 28 items) | End's Delight (new, 64 items) | Overlap |
|---|---|---|---|
| Stove | `endstone_stove` | `end_stove` | Duplicate function, two blocks. Pick one or let both exist |
| Chorus | `chorus_crate`, `chorus_juice`, `chorus_pie` + slice, `chorus_stew` | `chorus_fruit_crate`, `chorus_fruit_pie` + slice, `chorus_flower_pie`, `chorus_fruit_milk_tea`, `chorus_fruit_popsicle`, `chorus_fruit_wine`, `chorus_sauce`, `chorus_cookie`, `chorus_fruit_grain`, dried / succulent / flower tea | Direct: crate, pie + slice. Pie slice is the same concept twice. Fruits Delight chorus jam / jello and Create Central Kitchen chorus pies (recipe-only) also exist |
| Shulker | `shulker_filet`, `shulker_mollusk`, `shulker_bowl`, `stuffed_shulker` (+ bowl) | `shulker_meat` + slice, `roasted_shulker_meat` + slice, `grilled_shulker`, `shulker_omelette`, `shulker_soup`, `stir_fried_shulker_meat` | Direct: both turn shulkers into meat and dishes. Needs a shared tag |
| Enderman | `enderman_sight`, `sight_fragment`, `uncanny_cookies` | `enderman_gristle`, `enderman_gristle_stew`, `ender_noodle`, `ender_sausage` | Same mob, different drop; both are knife/loot-modifier drops |
| Endermite | `mite_crust`, `endermite_stew` (+ wood) | `raw_ender_mite_meat`, `dried_endermite_meat` | Same mob, two drops; tag `forge:raw_ender_mite_meat` is End's only |
| Pearl | `pearl_pasta` (+ wood), `ender_paella` (+ wood), `strange_eclair`, `crispy_skewer`, `crawling_sandwich`, `twisted_cereal` | `ender_pearl_grain`, `ender_bamboo_rice`, `ender_congee`, `stuffed_rice_cake` | Rice-style dishes overlap the pearl pasta / paella idea |
| Unique to End's | | Dragon meat line (`raw_dragon_meat`, `roasted_dragon_meat`, `dragon_leg`, `dragon_meat_stew`, steak, smoked leg), dragon egg line (`fried_dragon_egg`, `steamed_dragon_egg`, `liquid_dragon_egg`), dragon tooth / egg shell / end stone / purpur knives, bubble tea, dragon breath soda / soup | Dragon meat has no equivalent in the pack. Dropped by the Ender Dragon when killed with a tool from the `ends_delight:dragon_loot_tools` tag, or with a Nether's Delight machete (the `nd_add_*` modifiers) |
| Unique to Ender's | `shulker_bowl` set, wooden-bowl variants of every stew | | |

Files in End's Delight touch `create` (belt-upright tag) and `forge:tools/knives`; it adds knife tags, so its four
knives join the knife set the pack tunes (`tuneTools`). Its loot modifiers come in two sets (`add_*` and `nd_add_*`),
the `nd_` set being the same drops for Nether's Delight machetes (installed), so machetes can harvest dragon, shulker, endermite and enderman parts too.

Open decision: keep both, keep one, or keep both and merge the duplicated ingredient lines (chorus crate, shulker meat
tags). Recommendation: keep both (little real clash beyond the stove and chorus pie), tag the shared ingredients, and
let the owner pick which chorus pie the pack's fruit/jam recipes use.

## 2. Ocean's Delight and Oceanic Delight vs each other and the pack

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

## 3. Integration work common to all five (checklist for `mod-additions #4`)

| Step | Notes |
|---|---|
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

## 4. Miner's Delight: old (installed) vs new

The installed `miners_delight-1.19.2-1.1.2.jar` is the **OLD** version. The newer line is in
`DecompiledMods/minersdelight-1.21.1-1.4.5.jar` (NeoForge 1.21.1, mod id `minersdelight`, needs Lodestone, Farmer's
Delight). Not a drop-in: different mod id (`minersdelight` vs `miners_delight`), different loader, so a newer
Miner's Delight on 1.19.2 would be a backport in the Veggies / Vintage style, not an update.

| | Old (1.1.2, 1.19.2) | New (1.4.5, 1.21.1) |
|---|---|---|
| Items | 37 | 69 |
| Blocks (blockstates) | 7 | 10 |
| Loot modifiers | 8 (bat wing, squid, glow squid, silverfish eggs, infested blocks) | 9+ (adds spider web, spider, arthropods, cooked squid / bat wing / spider leg) |
| Worldgen | none | 3 files |
| Tags | 1 (milk) | 44 |

Content in the old version that the new one still has: copper pot and cup, cave carrot (+ wild + crate), bat wing,
squid / glow squid / tentacles line, silverfish eggs, weird caviar, noodle / vegetable / beef / chicken / rabbit /
mushroom / beetroot / pumpkin soup cups, milk / water / powder snow cups, sticky basket, gossypium, improvised
barbecue stick, pasta with veggieballs.

Only in the old version: `hot_cocoa_cup` (1).

New in 1.4.5 (33 items): bat line (`bat_cookie`, `bat_rolls`, `bat_soup` + cup), spider and arthropod line
(`spider_leg`, `baked_spider_leg`, `arthropod`, `cooked_arthropod`, `seasoned_arthropods`, `insect_stew` + cup,
`insect_sandwich`, `insect_wrap`, `glazed_arachnid_limbs` + plate), vegan line (`vegan_patty`, `vegan_hamburger`,
`vegan_wrap`, `vegan_steak_and_potatoes`, `fake_meatloaf` + plate), `cave_hamburger`, `copper_carrot`, `crunchy_bar`,
`nutritional_bar`, `golden_nutritional_bar`, `moss`, `onion_soup_cup`, `bone_broth_cup`, `glow_ink_pasta`,
`squid_sandwich`, `takoyaki`, `tentacles_on_a_stick`.

Overlap of the new additions with the pack: onion soup (Veggies Delight port has onions and soups: check), vegan
hamburger / wrap (Farmer's Delight `hamburger`, wraps), `takoyaki` (Oceanic Delight), `tentacles_on_a_stick` (Ocean's
Delight `tentacle_on_a_stick`), `glow_ink_pasta` (Farmer's Delight `squid_ink_pasta`), spider/arthropod meat
(Hostile Delight, Unusual Delight `spider_leg`, `spiderbarbecue`).

## 5. Unusual Delight

Only one build is on disk: `DecompiledMods/unusual_delight-5.0.jar` (mod id `unusual_delight`, Forge 1.20.1,
MCreator-generated, Academic Free License v3.0). There is **no older or 1.19.2 Unusual Delight** in `mods/`,
`disabledmods/` or `DecompiledMods/`, so a current-vs-old comparison is not possible from the files; this is the
version to compare against when one turns up. If the owner meant Hostile Delight as the "old" one, see the table.

| Fact | Value |
|---|---|
| Items | about 306 (by far the biggest) |
| Blocks (blockstates) | 26 |
| Recipes | 345 |
| Advancements | 20 (pack drops all advancements: do not port as an unlock path) |
| Depends on | `minecraft` only in `mods.toml`; recipes also use Farmer's Delight (cooking pot, cutting board, rice) |
| Soft mod content | Ice and Fire (about 33 references: dragon chunks, scales, stymphalian feathers, pixie dust), Alex's Mobs (15: crocodile egg, deathworm, sniffer-like items), Alex's Caves (8: fiddlehead) |
| Installed in pack | Alex's Mobs (the 1.21.1 jar in `mods/`, unusual name), Citadel; **not** Ice and Fire, **not** Alex's Caves |
| Port needed | Yes (1.20.1 only): a Veggies / Vintage style backport of an MCreator mod with hundreds of items |

Overlap with Hostile Delight 0.2.2 (installed): about 24 shared concepts (ids with underscores stripped):
`blazingale`, `blazingapple`, `bonegelatin`, `candiedslime`, `candiedslimeblock`, `carrotmite`, `cookedrottenham`,
`friedturtleegg`, `frozenflesh`, `ghastblood`, `ghastbrew`, `ghastpudding`, `ghastroll`, `ghasttentacle`,
`glowapple`, `glowgummy`, `miteymeal`, `popsiclime`, `preparedseapickle`, `refinedgelatin`, `rottenham`, `slimestew`,
`soda`, `turtleeggsandwich`. Hostile Delight reads as an earlier cousin of Unusual Delight; installing both would give
two of each of these.

Overlap with other installed content: `braised_sea_pickle` (Ocean's Delight), `spider_leg` and spider dishes
(Miner's Delight 1.4.5), dragon loaf / chunk lines (End's Delight dragon meat, if Ice and Fire is present), chorus
honey apple (End's/Ender's chorus), soul / Nether foods (Nether's Delight), wart / strider foods (Nether's Delight).

Considerations: 306 items is larger than every Delight port already done (Veggies, Vintage, Fungi), so the port cost
is of that order or more, and half the mod needs Ice and Fire or Alex's Caves content the pack lacks. The ids of
Unusual Delight and Hostile Delight clash conceptually, not literally (different namespaces).

## 6. Status

| Item | Status |
|---|---|
| Overlap survey (this file) | Written 2026-10-23 |
| Integration of the five jars | Not started; rows `largecontent/mod-additions #4` to `#9` |
| Test list | None yet (nothing integrated) |
