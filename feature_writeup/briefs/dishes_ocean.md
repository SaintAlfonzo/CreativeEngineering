# Brief: dishes / ocean

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# dishes`, Sub `ocean` (rows 1 to 5, all scratchpad
2026-10-24). Layout per [README.md](README.md). Feature writeup for this area:
[delight_addons_2026_10.md](../delight_addons_2026_10.md) section 2 (Ocean's Delight, Oceanic Delight and the
squid items of Miner's Delight and Rustic Delight). All recipes through `global.NCH` (`R`) in a new ocean section of
[cooking.js](../../kubejs/server_scripts/cooking/cooking.js), tags through `global.tags`, merges through
`global.itemMerges` ([item_merges.js](../../kubejs/startup_scripts/_lib/item_merges.js)).

**Verified against jars** (2026-10-24, `mods/`): `oceanic_delight-1.0.1`, `oceansdelight-1.0.2`, `miners_delight-1.1.2`,
`rusticdelight-1.3.0`. None of the Ocean's, Oceanic or Miner's Delight ids is in `item_dict.js` yet: run `/dumpall` first
(rows build on the mod-additions integration, `largecontent/mod-additions #5`).

## Scope and order

| Row | Change | Status | Note |
|---|---|---|---|
| 4 | Remove tentacles, all to calamari | Planned | first: it fixes the ingredient set the others use |
| 3 | Squid items, calamari chain | Planned | needs new glow calamari items |
| 5 | Salmon eggs become caviar | Planned | lang and recipes |
| 2 | Fish egg roll accepts eggs | Planned | one recipe |
| 1 | Fishing net | Planned | investigate first |

## ocean #1: Fishing net: cooldown, tooltips, loot by biome
Status: Planned / Work: KubeJS / data / TODO `dishes/ocean #1` ([delight_addons_2026_10.md](../delight_addons_2026_10.md))

### Goal
Give the net a cooldown, tooltips and a loot table; drops by biome: shrimp in the ocean (boosted), salmon eggs in rivers
and lakes. Investigate the mechanic first.

### Now
- The net is `oceanic_delight:fishing_net` (Oceanic Delight, not Ocean's Delight as the row says): shaped
  prismarine crystals, stick, string. The mod is MCreator-generated (`net/mcreator/oceanicdelight`), so the use logic is
  a procedure class; not decompiled yet (`tools/decompile_mod.py`).
- Loot is in two gameplay tables (verified): `oceanic_delight:gameplay/fishing_net_on_kelp` (weights: air 5, shrimp 2,
  salmon eggs 1, clay ball 10, kelp 6) and `.../fishing_net_on_sea_grass` (air 2, shrimp 3, salmon eggs 2, clay 5, scute
  1 at 10 %). Biome is not a condition today. An advancement `hitting_villager_with_net` exists (joke; the pack drops
  advancements).

### Build
1. Decompile the net procedure: how the table is picked (block use on kelp / seagrass), damage, any cooldown.
2. Override the two tables from KubeJS data (`kubejs/data/oceanic_delight/loot_tables/gameplay/...`) with
   `minecraft:location_check` biome predicates (ocean tag -> shrimp weight up; `#minecraft:is_river` and lake biomes -> salmon
   eggs); keep clay and kelp.
3. Cooldown and tooltips: if the procedure has no cooldown, a KubeJS item modification (`ItemEvents`) cannot add one; a
   patcherMod mixin or a `use` event handler is needed (decide after step 1). Tooltip in
   [tooltips.js](../../kubejs/client_scripts/tooltips.js).

### Decisions
Cooldown length (recommend 2 seconds, matching a fishing cast feel); whether biome rules are loot conditions (recommend)
or two net tables per biome group.

### Tests
DA-01 Net on kelp in ocean gives shrimp more often than in a river. DA-02 River gives salmon eggs. DA-03 Cooldown and
tooltip show.

---

## ocean #2: Fish egg roll accepts eggs
Status: Planned / Work: KubeJS / TODO `dishes/ocean #2` ([delight_addons_2026_10.md](../delight_addons_2026_10.md))

### Goal
Allow fish eggs in the fish egg roll recipe.

### Now
`oceanic_delight:fish_egg_roll` (shaped: cooked rice, dried kelp, `oceanic_delight:salmon_eggs`; verified), cut into
`fish_egg_roll_slice`. Other fish egg item: `oceanic_delight:ancient_fish_eggs` (buried treasure loot), which no recipe uses
(*unverified*). `egg_roll` is a separate dish (rice, fried egg, kelp).

### Decisions
"Eggs" is ambiguous: (a) every fish egg item (salmon eggs, ancient fish eggs), (b) also chicken eggs (`#forge:eggs`).
Recommend (a): a tag `forge:fish_eggs` through `global.tags`, then `R.replaceInput` on the recipe; chicken eggs belong to
the egg roll. Related `ocean #5` (salmon eggs become caviar).

### Build
`R.replaceInput({ id: 'oceanic_delight:fish_egg_roll' }, <salmon_eggs>, <tag>, e)` (shaped input swap works on the
recipe id); same swap for `caviar_toast` and the noodle soup if the tag is meant for them.

### Tests
DA-04 Roll crafts from ancient fish eggs.

---

## ocean #3: Squid and glow squid items, calamari chain
Status: Planned / Work: KubeJS / data / TODO `dishes/ocean #3` ([delight_addons_2026_10.md](../delight_addons_2026_10.md))

### Goal
Keep the squid and glow squid items. They drop from squids and glow squids; the raw ink sac chance drops and a cut squid
gives ink sacs. A squid cuts to calamari. Add glowing calamari, cooked glow calamari and the glow calamari roll.

### Now
- The squid items are Miner's Delight's `miners_delight:squid` and `glow_squid` (installed 1.1.2; cut -> 2 `tentacles` + ink
  sac or glow ink sac; baked by smelting / smoking / campfire). Their drop source (loot modifier) was not found in the jar
  data: *unverified*.
- Rustic Delight has the calamari line: `calamari`, `calamari_slice`, `cooked_calamari`, `cooked_calamari_slice`,
  `calamari_roll` (tag `#rusticdelight:calamari_roll_ingredients`), `fried_calamari`; loot modifiers
  `calamari_from_squid` and `calamari_from_glow_squid` (plain calamari from both; config `squids_drop_calamari`).
- Pack keys: `i.calamari`, `i.cooked_calamari`, `i.calamari_slice`, `i.calamari_roll` (item_dict 12502-12512); cans
  `canned_raw_calamari`, `canned_cooked_calamari`.

### Build
1. Squid cut recipe: `R.fdCutting(squid item, knife, calamari + ink sac (chance), e, ...)`; same for the glow squid giving
   glow calamari + glow ink sac. Remove the ink sac drop chance from the raw item's entity loot via `LootJS`
   (see `loot/lootjs-layer`).
2. New items: `glow_calamari`, `cooked_glow_calamari`, `glow_calamari_roll` (startup items; recolour Rustic textures with a
   glow tint; food values copying the plain ones via `food_values.js`); glow squids drop glow calamari instead of calamari
   (LootJS), roll recipe like Rustic's `calamari_roll` with the glow calamari.
3. Dicts, `food_types.json`, can rows (`tools/gen_can_types.py`) and `/cegen`.

### Decisions
Drop rates (the TODO row's decision): recommend squid item 100 % like a mob drop, a cut gives calamari 1 and ink sac
50 %. Whether the glow calamari line is its own can: recommend yes, generated.

### Tests
DA-05 Squid drops its item; knife gives calamari and ink. DA-06 Glow squid chain to the glow roll.

---

## ocean #4: Remove tentacles; all tentacles to calamari
Status: Planned / Work: KubeJS / TODO `dishes/ocean #4` ([delight_addons_2026_10.md](../delight_addons_2026_10.md)); part of the ocean merge (`mod-additions #5`)

### Goal
Remove the tentacle items; any tentacle cuts to calamari.

### Now
Tentacle items today (jars): Miner's Delight `tentacles`, `baked_tentacles`; Ocean's Delight `tentacles`, `cut_tentacles`,
`tentacle_on_a_stick`, `baked_tentacle_on_a_stick`, `squid_rings`; Oceanic Delight `squid_tentacles`, `glow_squid_tentacles`,
`grilled_squid_tentacles` (tag `#forge:squid_tentacles`, read by its dishes). Ocean's drop modifier
`farmersdelight:loot_modifiers/cut_tentacles_squid`; Oceanic `squid_tentacles_loot_modifier`, `glow_squid_tentacles_*`.

### Build
1. `global.itemMerges` entries purging each raw tentacle to `i.calamari` (glow ones to glow calamari after `#3`); the
   merge swaps recipe inputs and outputs and hides them in JEI.
2. Disable the three modules' squid loot modifiers (global loot modifier removal through the loot layer) so nothing drops.
3. `global.tags`: put calamari into `forge:squid_tentacles` so Oceanic's dishes keep working.
4. Cooked tentacle dishes (`tentacle_on_a_stick`, `squid_salad`, `squid_and_pickles`...) take calamari through the tag.

### Decisions
Purge by merge (recommend) or only remove from loot and recipes and leave the items: recommend the merge, which also
rescues old inventories.

### Gotchas
Order: the calamari survivor must exist (Rustic is installed). Ocean's Delight is not integrated yet.

### Tests
DA-07 No tentacle item in JEI or loot; a knife on any squid-family item gives calamari; Oceanic dishes still craft.

---

## ocean #5: Salmon eggs become caviar
Status: Planned / Work: KubeJS / lang / TODO `dishes/ocean #5` ([delight_addons_2026_10.md](../delight_addons_2026_10.md))

### Goal
More uses for salmon eggs: rename to caviar, add a caviar roll and a caviar noodle soup, and caviar in the sea pickle roll.

### Now
`oceanic_delight:salmon_eggs` (drops from salmon, shipwrecks, the net) is used by `caviar_toast`, `fish_egg_roll`,
`fish_egg_noodle_soup` (onion, pasta, bamboo, bowl; verified). So a roll and a noodle soup already exist, named Fish Egg
Roll and Fish Egg Noodle Soup. `sea_pickle_roll` is shaped sea pickles, tentacles, rice, kelp.

### Build
Names in [namechanges.js](../../kubejs/client_scripts/namechanges.js): Salmon Eggs -> Caviar, Fish Egg Roll -> Caviar Roll,
Fish Egg Noodle Soup -> Caviar Noodle Soup (ids stay). New: add caviar to the `sea_pickle_roll` recipe (a swap or an extra
slot).

### Decisions
The row says "add" a roll and a soup; both exist under other names. Recommend renaming them (no new items) and treat the
row's real new work as the sea pickle roll change. What "ancient fish eggs" are called: recommend leave.

### Tests
DA-08 Names read Caviar, Caviar Roll, Caviar Noodle Soup; sea pickle roll takes caviar.

---

## Built, needs play

None: no `ocean` row is at Testing or Complete.

Test ids: DA-01 to DA-08 are proposed for [delight_addons_2026_10.md](../delight_addons_2026_10.md) (its test list is
empty).

## Writeup corrections made

None.
