# Brief: dishes / ocean

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# dishes`, Sub `ocean` (rows 1 to 5, all scratchpad
2026-10-24); rows 6 to 8 added and the built rows condensed 2026-10-31. Layout per [README.md](README.md). Feature
writeup: [ocean_delight.md](../ocean_delight.md) (sections 3.1 newer jars, 5 built, 6 tests; prefix DA-). Ocean's Delight
and Oceanic Delight are Creative Delight members since 2026-10-27 (nested in `creativedelight-1.0.0.jar`, donor jars in
`development/creativeDelight/upstream/`); their pack code is CD `kubejs/data/creativedelight/kubejs/oceansdelight/`
(`recipes.js`, `tags.js`, both `// requires: oceansdelight`, Oceanic parts behind `global.CD.on('oceanic_delight')`). Recipes
through `global.NCH` (`R`), tags through `global.tags`, ids from `item_dict.js` (Oceanic block "oceanic_delight (47)",
around line 14653).

**Verified against jars** (2026-10-31): `development/creativeDelight/upstream/oceanic_delight-1.0.1-forge-1.19.2.jar`,
`oceansdelight-1.0.2-1.19.2.jar`; newer references in `devmods/createiveDelight/` (`oceanic_delight-1.0.4-forge-1.20.1.jar`,
`oceansdelight-neoforge-1.0.4-1.21.1.jar`, both MIT). Recipe uses from `kubejs/exported/food/recipes.json` (dump 2026-10-30).

## Scope and order

| Row | Change | Status | Note |
|---|---|---|---|
| 2 | Fish egg roll accepts eggs | In Progress | one tag + one swap; settle its decision together with row 6 |
| 6 | Caviar use audit | Planned | decides which recipes take caviar (sea pickle roll from row 5) |
| 7 | Kelp Encrusted Cod | Planned | new pack item, decided 2026-10-25 |
| 8 | Oceanic JEED Nourishment list | Planned | premise changed: see the section (installed jar's JEED list is already right) |
| 1, 3, 4, 5 | Fishing net; squid chain; tentacles removed; caviar names | Testing | see Built, needs play |

## ocean #2: Fish egg roll accepts eggs
Status: In Progress / Work: KubeJS / TODO `dishes/ocean #2` ([ocean_delight.md](../ocean_delight.md) 3)

### Goal
Allow fish eggs in the fish egg roll (shown as Caviar Roll) recipe.

### Now
- Nothing built (2026-10-31: no fish egg tag or `fish_egg_roll` swap in CD `oceansdelight/recipes.js` or
  `oceansdelight/tags.js`).
- `oceanic_delight:fish_egg_roll` (`i.fish_egg_roll`) is shaped `a / bbb / ccc`: `oceanic_delight:salmon_eggs` (`i.caviar`),
  3 `farmersdelight:cooked_rice`, 3 `minecraft:dried_kelp` (verified, jar recipe `fish_egg_roll.json`); cut into
  `fish_egg_roll_slice`.
- `oceanic_delight:ancient_fish_eggs` (`i.ancient_fish_eggs`) comes only from buried treasure (loot modifier
  `ancient_fish_eggs_loot_modifier`); no recipe takes it (verified: the dump lists no recipe with it).
- Miner's Delight's `silverfish_eggs` and `weird_caviar` are insect food (insect stew, crunchy bar, sandwich, wrap), not fish.
- Caviar toast already runs on toast (CD `oceansdelight/recipes.js` line 15).

### Build
1. Tag: new dict key `i.t_fish_eggs = '#forge:fish_eggs'` (tag block near `i.t_raw_calamari`, item_dict.js line 1838);
   in CD `oceansdelight/tags.js`, inside the Oceanic branch: `global.tags.add(e, i.t_fish_eggs, [i.caviar, i.ancient_fish_eggs])`.
2. Swap: in CD `oceansdelight/recipes.js` (Oceanic branch) `R.replaceInput({ id: 'oceanic_delight:fish_egg_roll' }, i.caviar,
   i.t_fish_eggs, e)`. Same swap on `caviar_toast` and `fish_egg_noodle_soup` only if row 6 decides they take any fish egg.
3. Changelog bullet under the ocean family (Caviar Roll also takes Ancient Fish Eggs).

### Decisions
- Which eggs (TODO decision): (a) fish eggs only (caviar, ancient fish eggs), (b) chicken eggs too (`#forge:eggs`).
  Recommend (a); chicken eggs already make Oceanic's `egg_roll`.

### Tests
DA-04 (proposed): the Caviar Roll crafts from Ancient Fish Eggs and from Caviar; JEI cycles both.

---

## ocean #6: Caviar use audit
Status: Planned / Work: KubeJS / TODO `dishes/ocean #6` ([ocean_delight.md](../ocean_delight.md) 5.2)

### Goal
Caviar is common since the net drops it in rivers: list where it is used, add it where it fits (the sea pickle roll,
deferred from row 5), and decide which recipes take any fish egg (row 2).

### Now
- Sources: salmon killed with a knife (Oceanic `scavenging_eggs_from_salmon`, 50 %, looting +0.1), shipwreck chests
  (`salmon_eggs_shipwreck_loot_modifier`), the fishing net in rivers (pack table
  `kubejs/data/oceanic_delight/loot_tables/gameplay/fishing_net_on_kelp.json` and `_on_sea_grass.json`, weight 3, biome tag
  `creativeengineering:fishing_net/river`).
- Uses today (dump 2026-10-30): `caviar_toast` (shapeless, on toast), `fish_egg_roll` (row 2), `fish_egg_noodle_soup`
  (pot) and its Slice and Dice mixing copy. Nothing else.
- Sea pickle roll: shaped `RXR / #A#`, `#forge:sea_pickles` x2, dried kelp, cooked rice x2, `#forge:squid_tentacles`
  (raw calamari since 2026-10-31, CD `oceansdelight/tags.js`).
- Other fish rolls that could take a caviar topping: Farmer's Delight `salmon_roll`, `cod_roll`, `kelp_roll`; Oceanic
  `shrimp_roll`, `egg_roll`; Ocean's `fugu_roll`, `elder_guardian_roll`; Rustic `calamari_roll`; pack `glowing_calamari_roll`.

### Build
1. Sea pickle roll: add caviar to the recipe. The pattern is 2 rows, so a third row fits: `R.remove` the Oceanic recipe by id
   and rebuild it with `R.shaped` in CD `oceansdelight/recipes.js` (pattern `RXR / #A# / ' C '`, `C` = `i.caviar` or the row 2
   tag), same output count.
2. For each other recipe the owner picks: a second recipe with caviar (keep the original) through `R.shaped` /
   `R.shapeless`, ids `creativedelight:<type>/<name>_with_caviar`. Do not replace the base recipes.
3. Row 2's tag: decide here whether toast, roll and soup all take `#forge:fish_eggs`.
4. Changelog bullet per recipe change.

### Decisions
- Which recipes (TODO decision): recommend the sea pickle roll only, plus the row 2 tag on all three caviar recipes; a
  caviar variant of the salmon roll is the one other candidate (salmon and its eggs). Leave the other rolls.
- Caviar as an added slot or a swap in the sea pickle roll: recommend added slot (the roll keeps its calamari).

### Gotchas
Oceanic is All Rights Reserved: rebuild recipes in the pack, never copy its data files. The dump's Slice and Dice copies
follow the pot recipe automatically.

### Tests
DA-new1 (proposed, numbered when queued): sea pickle roll crafts with caviar; JEI shows the new pattern. DA-new2: each other
caviar recipe the owner picks crafts; the base recipe still exists.

---

## ocean #7: Kelp Encrusted Cod
Status: Planned / Work: KubeJS / resource pack / TODO `dishes/ocean #7` ([ocean_delight.md](../ocean_delight.md) 3.1)

### Goal
Add Ocean's Delight 1.0.4's Kelp Encrusted Cod as a pack item (decided 2026-10-25).

### Now
- Not built (2026-10-31: no `kelp_encrusted_cod` in `item_dict.js`, CD startup or CD scripts).
- 1.0.4 jar (1.21.1, reference only): recipe `data/oceansdelight/recipe/cooking/cooking/kelp_encrusted_cod.json`:
  `farmersdelight:cooking`, xp 0.35, no container, no cooking time (FD default 200), inputs `farmersdelight:cod_slice`,
  `#c:eggs`, `minecraft:kelp`; texture `assets/oceansdelight/textures/item/kelp_encrusted_cod.png`; name "Kelp Encrusted Cod".
- Pack ids: `i.cod_slice` (item_dict.js 1782), `i.eggs` = `#forge:eggs` (1939), `i.kelp` (1748).
- Pattern for a pack item in a CD member tree: CD `kubejs_startup/rusticdelight/glowing_calamari.js` (StartupEvents.registry,
  `.food(...)`, ids from `global.item`).

### Build
1. Dict: `i.kelp_encrusted_cod = 'creativeengineering:kelp_encrusted_cod'` in the pack-namespace block of `item_dict.js`
   (next to `i.glowing_calamari_roll`, line 1836); `f.dish_kelp_encrusted_cod` in `food_dict.js`.
2. Item: new CD startup script `development/creativeDelight/src/main/resources/kubejs_startup/oceansdelight/kelp_encrusted_cod.js`
   (`// requires: oceansdelight`), `e.create(i.kelp_encrusted_cod).displayName('Kelp Encrusted Cod').food(f => f.hunger(5).saturation(0.6))`.
3. Texture: copy the 1.0.4 png to `kubejs/assets/creativeengineering/textures/item/kelp_encrusted_cod.png` with an MIT
   notice beside it (as `glowing_calamari_art_notice.txt`).
4. Recipe: CD `oceansdelight/recipes.js`: `R.fdCooking([i.cod_slice, i.eggs, i.kelp], i.kelp_encrusted_cod, e,
   'kelp_encrusted_cod', 0.35, 200)` (no container).
5. Follow-ups: `/dumpall`, then `/cdevgen` (food types, cans, Comfort / Nourishment rule, compost); tooltip per
   [tooltips.md](../tooltips.md); food tuning sets the final value.
6. CD rebuild and install (`:creativeDelight:build`, `installToMods`) with the game closed; restart. Changelog bullet.

### Decisions
None open (food 5 / 0.6 to start, decided 2026-10-25). Item namespace: `creativeengineering` per the TODO (Ocean's is MIT
and a member, so its own namespace would also work; keep the decision).

### Gotchas
Rebuild, do not convert: `#c:eggs` is `#forge:eggs` here and 1.21's `result.id` is `item` in 1.19.2. Upstream 1.0.4
registers the item with the stuffed cod food (3 / 0.2) by mistake; use the separate 5 / 0.6 value.

### Tests
DA-12 (proposed in [ocean_delight.md](../ocean_delight.md) 3.1): pot recipe, name, food values, tooltip and JEI.

---

## ocean #8: Oceanic Delight JEED Nourishment list
Status: Planned / Work: KubeJS / TODO `dishes/ocean #8` ([ocean_delight.md](../ocean_delight.md) 3.1)

### Goal
JEED's Comfort and Nourishment pages list the Oceanic foods that really give each effect.

### Now (checked 2026-10-31 in the item classes)
- Oceanic applies its effects in code (MCreator procedures), not as food properties: the dump shows `effects: []` for all
  of them, so neither JEED's own scan nor the pack's Comfort / Nourishment applier (CD `creativeengineering/comfort_nourishment.js`,
  [comfort_nourishment.md](../comfort_nourishment.md) 10) sees them; `kubejs/generation_scripts/food/comfort_nourishment.json`
  has no Oceanic entry.
- **Installed 1.0.1**: Nourishment on Pasta With Eyeball, Paella, Stuffed Pufferfish (5 min), Shrimp Rice, Squid and Pickles
  (3 min); **Comfort** on Caviar Noodle Soup, Globular Rice, Shrimp Stew (5 min), Glow Squid Stew (3 min); Instant Breath
  on Sea Grape, Sea Grape Juice, Squid Salad. Its `jeed_nourishment.json` (5 foods) and `jeed_comfort.json` (4 foods)
  match this exactly.
- **1.0.4** moved the four soups and stews from Comfort to Nourishment in code and dropped `jeed_comfort.json`; that is
  where the "9 foods" list comes from. So the installed JEED info is already correct; the row's plan would make it wrong.

### Build
Depends on the decision:
- (a) Keep 1.0.1 behaviour: nothing to build; close the row.
- (b) Adopt 1.0.4's change: a CD Oceanic member patch (mixin or class override on the four items' `finishUsingItem`
  procedures) to give Nourishment, then add the 4 to Nourishment and remove them from Comfort in
  [jeed_providers.js](../../kubejs/server_scripts/other/jeed_providers.js) (`add('farmersdelight:nourishment', [...])`), or
  override the mod's two provider recipes by id.

### Decisions
- Recommend (a): soups and stews giving Comfort is the pack's form rule (comfort_nourishment.md R1/R5), and 1.0.1 already
  follows it. Separately, decide whether code-applied Oceanic effects should be brought under the R5 rule at all
  (`food/types-and-tuning #15`).

### Tests
DA-13 (proposed, ocean_delight.md 3.1), reworded for (a): JEED lists Nourishment on the 5 and Comfort on the 4 Oceanic
foods, and eating each gives that effect at the listed length.

---

## Built, needs play

- `dishes/ocean #1` Fishing net: cooldown, tooltips, loot by biome: [ocean_delight.md](../ocean_delight.md) 5.1; open: S~ (SQ-FN-02, -03 need group B biomes), C P T (DA-01 to DA-03, DA-09 to DA-11).
- `dishes/ocean #3` Squid and glow squid items, calamari chain: [ocean_delight.md](../ocean_delight.md) 5.3; open: S~ (SQ-CDB-05 JEI), C P (DA-05, DA-06); Rustic's licence file and glowing cans still open.
- `dishes/ocean #4` Remove tentacles; all tentacles to calamari: [ocean_delight.md](../ocean_delight.md) 5.5; open: S (rerun SQ-CM-32, SQ-CM-35 on the 2026-10-31 12:32 CD jar), C P.
- `dishes/ocean #5` Salmon eggs become caviar: [ocean_delight.md](../ocean_delight.md) 5.2; open: C P (DA-08); the sea pickle roll moved to row 6.

Test ids: DA-01 to DA-03, DA-05, DA-06, DA-08 to DA-11 are in [ocean_delight.md](../ocean_delight.md) 6; DA-04 (row 2) stays
proposed here; DA-12, DA-13 are proposed in its 3.1. DA-07 (row 4, no tentacles anywhere) was dropped 2026-10-31:
SQ-CM-31, -32, -35 cover it.
