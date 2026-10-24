# ui / effects: implementation brief

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# ui`, Sub `effects` (row 6 Planned, row 1 In Progress; rows 2, 3, 4,
5, 7, 8 Testing). Feature writeup: [effects.md](../effects.md) (test prefix `EF-`, last id EF-19).

## Scope and order

| Group | Rows | Work | Why together |
|---|---|---|---|
| A | `effects #6` (+ the `effects #4` provider fix) | KubeJS: `_lib/item_dict.js`, `_lib/removed_items.js`, `server_scripts/other/jeed_providers.js`, `food_types.json` | One removal pass for the Abnormals Delight items that need a mod the pack doesn't have; the JEED provider list names one of them |
| B | `effects #1` | KubeJS lang overlay, `config/jeed-client.toml`, `tools/gen_effect_descriptions.py` | Text only, waiting on the user's approval of the PROPOSED texts |

Build order: A any time (one restart: item_dict and removed_items are startup scripts); B as soon as the text is approved
(F3+T reloads lang; the JEED config needs a client restart).

Cross-family links:
- `potions/effects #3` ([brief](potions_effects.md)): switching Magic Resistance to the pack's `magic` damage tag changes what
  its description must say; that brief has its own wording. Write the Magic Resistance text with or after it.
- `food/types-and-tuning #10` ([brief](food_types-and-tuning.md)) already removes `passion_fruit_glazed_duck`;
  `baking/baked-goods #5` part c ([brief](baking_baked-goods.md)) already removes `create_central_kitchen:passionfruit_cake_slice`
  (and the aloe / yucca cake slices). `#6` here only needs to check them, as its Details ask: both are handled there.
- `food/placeable` ([brief](food_placeable.md) "not placeable today") and `food/stack-sizes` ([brief](food_stack-sizes.md)
  "drinks with no food value") list `passion_aloe_nectar` and `cherry_cream_soda`; both are unobtainable (below), so
  those lists can drop them.
- Sunny (row 5): with More Mooblooms nothing can give it (verified below); the `mobs/mooblooms` brief
  ([mobs_mooblooms.md](mobs_mooblooms.md)) says the same.

---

## effects #6: Remove Passion Aloe Nectar
Status **Planned** · Work: KubeJS · id `ui/effects #6`

### Goal
Passion Aloe Nectar (and any other item that can only exist with a mod the pack doesn't have) is out of play: hidden,
untagged, flagged if a stack turns up, and not listed as an effect source.

### Now (Abnormals Delight 4.1.2 jar, decompiled `ADItems`, `NectarItem`, `EffectDrinkItem`)
- `abnormals_delight:passion_aloe_nectar` is a `NectarItem` (`EffectDrinkItem(null, props, "atmospheric")`): it gets a
  creative tab only when `atmospheric` is loaded, so it is not in JEI (JEI lists creative-tab items; this is why the user
  saw it hidden). It has **no food value** (no `FoodProperties`; stack_sizes.md 361 has no food type), and its effect is
  Atmospheric's `relief`, which doesn't exist, so drinking it does nothing but return the bottle.
- Its only recipe, `abnormals_delight:passion_aloe_nectar` (`farmersdelight:cooking`: honey bottle, `atmospheric:aloe_leaves`,
  2 `#forge:fruits/passion_fruit`), has `forge:mod_loaded atmospheric`: it never loads. No tag file in the jar names it,
  and no pack script does. It is **not in item_dict** (the row says "remove from item_dict"; there is nothing to remove,
  it needs a key to be listed in `removedItems`).
- The other passion fruit items:
  - `abnormals_delight:passion_fruit_glazed_duck`: compat item for `atmospheric` + `environmental` (no tab), recipe needs
    both. Removal planned in `food/types-and-tuning #10`.
  - `create_central_kitchen:passionfruit_cake_slice`: its cut and craft recipes need `peculiars` (not installed). Removal
    planned in `baking/baked-goods #5` (c). Whether CCK puts it in a creative tab is unverified: check JEI.
- More Abnormals Delight items in the same state (gated on mods not in pack/MODLIST.md):
  - Atmospheric: `dune_platter` (typed `meal`), `yucca_gateau_slice` (typed `sweet`, compost_values.js 699).
  - Environmental: `cherry_cream_soda` (`EffectDrinkItem(SLOW_FALLING, ..., "environmental")`, recipe needs
    `environmental`) — but it is listed as a Slow Falling source in
    [jeed_providers.js](../../kubejs/server_scripts/other/jeed_providers.js) line 106, so JEI shows an item no one can
    make. `cherry_cookie`, duck and venison items: owned by `baking/baked-goods #3` and `food/types-and-tuning #10`.
  - Autumnity: `maple_cookie`, `maple_glazed_bacon`, `escargot`. Leave them: `ports/autumnityport` may bring the
    `autumnity` mod id back, which would make them live.
- Removal path ([removed_items.js](../../kubejs/startup_scripts/_lib/removed_items.js)): `global.removedItems` ->
  hidden in JEI ([jeichanges.js](../../kubejs/client_scripts/jeichanges.js) 4-10), red "REMOVED ITEM: DO NOT USE"
  tooltip (16-23), every tag stripped and every recipe that still names it logged
  ([recipe_scan.js](../../kubejs/server_scripts/_lib/recipe_scan.js) 21-55).

### Build
1. item_dict, in the "JEED effect providers" block (line 13419, next to `i.cherry_cream_soda`), or a new
   `// ===== Abnormals Delight compat items with no source (ui/effects #6) =====` block:
   `i.passion_aloe_nectar = 'abnormals_delight:passion_aloe_nectar';`
2. removed_items.js, a commented group before the closing `];`:
   ```js
   // Abnormals Delight compat items whose mod isn't installed (ui/effects #6): no recipe loads, no creative tab.
   i.passion_aloe_nectar,   // Atmospheric: Relief effect doesn't exist, no food value
   i.cherry_cream_soda,     // Environmental
   i.dune_platter,          // Atmospheric
   i.yucca_gateau_slice,    // Atmospheric
   ```
   (D1 decides the last three.) Keep `passion_fruit_glazed_duck` and the CCK slice in their own rows' changes, or add
   them here too if this lands first and tell those briefs.
3. jeed_providers.js line 106: delete `add('minecraft:slow_falling', i.cherry_cream_soda)` (Red Wine stays the Slow
   Falling drink, from the Vinery wine providers built from `config/vinery-common.toml` `effects`, plus the potions). Do this even if D1 keeps the soda.
4. food_types.json: move `dune_platter` and `yucca_gateau_slice` from `meal` / `sweet` to `excluded` with
   `"removed: needs Atmospheric (ui/effects #6)"`; compost_values.js: drop the `i.yucca_gateau_slice` row (699). Then
   `python3 tools/food_catalog.py --check`.
5. effects.md: F8 -> FIXED with the date; EF-12 rewritten (below); 3 "Coverage" list: Slow Falling's provider list no
   longer has the soda.

### Ids and data
Verified in the Abnormals Delight 4.1.2 and CCK 1.3.11.c jars: item ids, the `atmospheric` / `environmental` /
`peculiars` conditions, `NectarItem` behaviour. Installed-mod check against pack/MODLIST.md (no `atmospheric`,
`environmental`, `peculiars`, `autumnity`).

### Decisions
- **D1 Scope:** recommend removing the four Atmospheric / Environmental items above in this row (same state as the
  nectar, nobody owns them), and leaving the Autumnity three for the Autumnity port to decide.

### Gotchas
- `removedItems` doesn't unregister anything: `/give` still works and the stack shows the REMOVED line. EF-12's
  current wording ("`/give` fails") is wrong for this path.
- item_dict and removed_items.js are startup scripts: restart, not `/reload`.

### Tests (EF-)
- EF-12 (reworded): `/give @s abnormals_delight:passion_aloe_nectar` -> the item has the red REMOVED line; JEI search
  "nectar" / "Passion" -> no Abnormals Delight entries; latest.log `[jeiRemove]` lists no recipe for it.
- EF-20: JEI -> Slow Falling effect page -> Red Wine, no Cherry Cream Soda; `python3 tools/effect_audit.py` reports no
  `no JEI source` for `minecraft:slow_falling`.

---

## effects #1: JEED descriptions for 10 effects
Status **In Progress** · Work: KubeJS · id `ui/effects #1`

### Goal
The 10 effects still on a `TODO: write a description` placeholder get real JEI descriptions, once the user approves
the text (decision "approve the text").

### Now
- [kubejs/assets/creativeengineering/lang/en_us.json](../../kubejs/assets/creativeengineering/lang/en_us.json): 10
  placeholders, lines 4 Long Reach, 5 Patrol Nullifier, 6 Phantom Bane (`effect.better_beacons.*.description`), 11 Magic
  Resistance, 12 Copper Poisoning, 17 Anti-Fire (`immersivepetroleum:anti_fire`, named only by its id: its name key is
  `""` in IP's lang), 25-28 the four Xaero World Map switches (`effect.xaeroworldmap.no_cave_maps[_harmful]`,
  `no_world_map[_harmful]`).
- PROPOSED texts in effects.md 2.1 (Detailed column): lines 117-119 (Better Beacons), 134 (Magic Resistance), 136
  (Copper Poisoning), 165 (Anti-Fire), 202-205 (Xaero World Map; worded like JEED's minimap texts on 194-201).
- Checked against code here: Long Reach is `ForgeMod.REACH_DISTANCE` +3.0 per level, ADDITION (Better Beacons 1.0.2
  `BBEffects`); Patrol Nullifier and Phantom Bane are `hasEffect` checks in `PatrolSpawnerMixin` / `PhantomSpawnerMixin`
  (no level effect); Long Reach and Magic Resistance are on the felsic and dragon sets (armor_effects.js 236-257).
- [gen_effect_descriptions.py](../../tools/gen_effect_descriptions.py) `main()` keeps any value that doesn't start with
  `TODO: write a description` ("real descriptions left untouched"), so hand-written text in the overlay survives reruns.
- Anti-Fire is given for one tick when leaving a fireproof motorboat over lava and is hidden from the HUD, but JEED still
  lists it in JEI, with a blank name (`hidden_effects` in
  [jeed-client.toml](../../config/jeed-client.toml) has only Sunny).

### Build
1. On approval, replace the nine placeholders (all but Anti-Fire, D1) with the 2.1 texts, e.g.
   `"effect.better_beacons.long_reach.description": "Extends your block reach by 3 blocks per level, for placing, breaking and using blocks. Attack range is unchanged. From a beacon, felsic and dragon armor, or a Potion of Long Reach."`
   Magic Resistance: take the wording settled in `potions/effects #3` (the `magic` tag version if that lands).
2. Anti-Fire (D1): add `"immersivepetroleum:anti_fire"` to `hidden_effects` in `config/jeed-client.toml` **and**
   `configs_edited/root_config/jeed-client.toml` (they must stay equal, as for Sunny). Then extend
   `gen_effect_descriptions.py` to read that list (`hidden_effects = [...]`, the same regex as effect_audit.py's `hidden_effects()`, line 123) and
   treat hidden effects like unregistered ones: no new placeholder, and an existing placeholder goes to the `stale` list.
   Rerun it: line 17 disappears.
3. Run `python3 tools/gen_effect_descriptions.py` (expect "placeholders already present: 0") and
   `python3 tools/effect_audit.py` (the placeholder problems gone; 15 problems on 2026-10-20 minus these).
4. effects.md: 2.1 Desc column `N` -> `Y` and the Detailed cells drop "PROPOSED:"; F2 -> FIXED; 0 at-a-glance; EF-02
   reworded (Long Reach shows its text).

### Ids and data
Effect ids above (verified in the overlay and, for Better Beacons, the jar). Effect ids are not in
`global.effect` ([effect_dict.js](../../kubejs/startup_scripts/_lib/effect_dict.js), vanilla only), but lang keys and the
JEED toml take raw ids anyway, so no dict key is needed.

### Decisions
- **D0 Approve the text** (the row's decision): the nine texts in effects.md 2.1. Recommend approving as written, with
  Magic Resistance following `potions/effects #3`.
- **D1 Anti-Fire:** recommend hiding it in JEED (it is a one-tick internal effect with no name) instead of describing
  it. Alternative: keep the PROPOSED text and also give it a name ("Anti-Fire") through name_rules.json `names`.

### Gotchas
- The JEED config is client-side and read at startup: restart after editing `hidden_effects`.
- Don't hand-edit the placeholder text format: the generator recognises placeholders by the `TODO: write a description`
  prefix only.

### Tests (EF-)
- EF-02 (reworded): JEI -> Long Reach -> the new text; Patrol Nullifier, Phantom Bane, Magic Resistance, Copper
  Poisoning and the four World Map switches likewise; JEI effect search shows no blank-named entry (Anti-Fire hidden).

---

## effects #9: Potion bundles do not render in the effect provider
Status **Bug** / Work patcherMod / TODO `ui/effects #9`

### Goal
JEI effect pages (JEED's provider page) show the three potion bundle items with their potion, like the potion buckets.

### Now
- [jeed_providers.js](../../kubejs/server_scripts/other/jeed_providers.js) lines 209-212 pass `i.potion_bundle`,
  `i.splash_potion_bundle`, `i.lingering_potion_bundle` (Potion Bundles 1.6, `com.github.ichhabehunger54.potionbundles`)
  to `R.jeedPotionProvider(..., 'potion_carriers')`: JEED then fills each stack with a `Potion` tag per potion.
- The bundles keep their potion and a `Uses` count through `PotionBundleUtils` (strings in the class; exact NBT keys
  unverified). A bare `{Potion:...}` stack may render as an empty bundle, or be hidden by a JEI filter. Cause unverified.

### Build
1. Reproduce: JEI -> Regeneration; note whether the bundles are missing, blank or show no potion; read `latest.log`
   for JEED warnings.
2. Read `PotionBundleUtils` for the NBT the bundle needs (decompile or `javap -p -c`).
3. Fix by giving JEED fully formed stacks: either extend `NCH.jeedPotionProvider` (neoCraftHelper.js 1872) to accept
   `{item, nbt}` templates with a placeholder, or a patcherMod hook that completes the stack (like the
   `CraftableFirstRecipeLayoutList` style patches).

### Ids and data
`i.potion_bundle`, `i.splash_potion_bundle`, `i.lingering_potion_bundle` (item_dict), mod `potionbundles`.

### Gotchas
The potion buckets use the same list and work (EF-16); change only the bundle entries.

### Tests (EF-)
- EF-21 (proposed): JEI -> Regeneration lists the three bundles with the Regeneration potion shown.

---

## effects #10: Hostile Mobs effect provider
Status **Planned** / Work patcherMod / KubeJS / TODO `ui/effects #10`

### Goal
The "Hostile Mobs" effect's provider page shows one grouped entry (a cycling slot or tag) instead of a long item list,
and the effect gets a description.

### Now
- **Which effect is unverified.** No effect named "Hostile Mobs" is in `kubejs/exported/mob_effects.json` (the dump
  predates the Fungi and Hostile Delight jars) or in Hostile Delight 0.2.2's lang (`hostiledelight:collapsing`,
  `hostiledelight:dioxide` are its effects). Find the key in game (`/effect give @s <tab>` or JEI effect search).
- `jeed_providers.js` writes one `jeed:effect_provider` recipe per effect with an explicit item list
  (`R.jeedProvider`, neoCraftHelper.js 1848); `_jeedProviderJson` (1887) accepts item ids and `{item,nbt}` only, no tags.
- Descriptions live in `kubejs/assets/creativeengineering/lang/en_us.json` (`effect.<ns>.<id>.description`, JEED lang
  overlay; see `effects.md` 3).

### Build
1. Identify the effect id; list what its provider page shows now.
2. Extend `NCH._jeedProviderJson` to accept an item tag (`#ns:tag`) as one entry; check that JEED's recipe codec
   (`jeed:effect_provider`) takes a tag ingredient (unverified; else collapse to a single representative item).
3. Add a tag in tags (via `global.tags`) for the group, point the effect at it, add the description key.

### Decisions
Collapse by tag (one slot that cycles) or by a single stand-in item. Recommend tag, if JEED accepts it.

### Tests (EF-)
- EF-22 (proposed): the effect's page shows one cycling slot; the hover text and description read well.

---

## effects #11: Weak Stomach effect provider
Status **Planned** / Work patcherMod / KubeJS / TODO `ui/effects #11`

### Goal
Weak Stomach (`fungidelight:weak_stomach`, Backported Fungi Delight) has an effect provider page, and its text no longer
says only alcohol causes the reaction.

### Now
- Source (jar `development/backportedFungi`, `ModEffectEvents.onFoodEaten`): eating an item in
  `FungiDelightTags.ALCOHOL_REACTION` gives Weak Stomach for 1200 ticks; while it lasts, eating or drinking an
  `ALCOHOL` tagged item or any potion except water gives Nausea, Poison and Weakness (200 ticks, level 2).
- JEED finds only FoodProperties effects, so no provider exists: [jeed_providers.js](../../kubejs/server_scripts/other/jeed_providers.js)
  has no `fungidelight:weak_stomach` line.
- Existing text mentioning alcohol: `kubejs/assets/creativeengineering/lang/en_us.json` line 30
  (`effect.fungidelight.weak_stomach.description`: "From eating inky caps or durian. Drinking alcohol or any potion
  other than water ...") and [jei_info.js](../../kubejs/client_scripts/jei_info.js) line 15-17 ("Causes Weak Stomach:
  drinking alcohol or a potion ..."). Which line is the "from alcohol" wording the row means is unverified.

### Build
1. `add('fungidelight:weak_stomach', [i.cleaned_cap, i.cooked_cleaned_cap, i.inky_caps_salad, i.inky_cap_scrambled_eggs, ...])`
   in the food and drink section of `jeed_providers.js`; read the real members of `fungidelight:alcohol_reaction` from the
   mod's tag file (and durian, if tagged) instead of hand-listing.
2. Reword the two texts so the cause (the tagged foods) and the trigger (alcohol, any non-water potion) are separate
   sentences.

### Ids and data
Keys `i.cleaned_cap`, `i.inky_caps_salad`, `i.inky_cap_scrambled_eggs` exist (item_dict 13939-13950); durian is
`fruitsdelight:*` (check which durian item is in the tag).

### Tests (EF-)
- EF-23 (proposed): JEI -> Weak Stomach lists the cap foods; the description names the cause and the trigger separately.

---

## Built, needs play

| Row | Change | Status | Notes |
|---|---|---|---|
| effects #2 | Wrong or partial JEED descriptions | Testing | Overlay holds the fixes (Comfort, Soul Cross, Teleport, etc.); EF-17 |
| effects #3 | Merge backported effect descriptions with JEED | Testing | OK as described |
| effects #4 | Effect providers in JEI | Testing | **Bug:** jeed_providers.js 106 lists `cherry_cream_soda` for Slow Falling, an item no one can make (needs Environmental); fixed in `#6` step 3 |
| effects #5 | Sunny hidden from JEI | Testing | **Details stale:** Sunny is not on the HUD from anything. More Mooblooms (in the pack since 1.0.6, both sides) removes BB's moobloom on join, strips `buzzier_bees:buttercup` from every item tag (no Sunny suspicious stew) and, with `disableSunny = true` (`config/more_mooblooms-common.toml` line 62, same in configs_edited), denies `buzzier_bees:sunny` in `MobEffectEvent.Applicable` (`/effect give` too) and strips it on join (`compat/BuzzierBeesSet.java` 125-160; MB-82 passed on a scratch server). The "purge sunny" item (effects.md 7) is therefore done. The JEED hide is belt and braces |
| effects #7 | Effect colour audit | Testing | OK as described (EF-18) |
| effects #8 | Effects reference tests | Testing | EF-01 describes the pre-fix state (raw keys); after `names #2` / `#4` it should expect names. EF-11 and EF-19 are the same check |

## Writeup corrections made

- [effects.md](../effects.md) F7: Sunny no longer shows on the HUD; More Mooblooms' removals and `disableSunny` make
  "purge sunny" done (with the mooblooms.md 4.11 reference).
- [effects.md](../effects.md) 2.1 Sunny row: noted that both sources are gone in this pack and the effect is denied.
- [effects.md](../effects.md) F8: "The drink still feeds you" was wrong (no food value); added why it is out of JEI
  (no creative tab without Atmospheric) and that its recipe never loads.
- [effects.md](../effects.md) 7: "purge sunny" marked done.
