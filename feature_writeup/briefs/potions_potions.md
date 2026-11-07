# Brief: potions / potions

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# potions`, Sub `potions` (rows 2 to 9 open, row 1 Complete). Feature
writeups: [potions.md](../potions.md) (test prefix `PO-`, last PO-26) and [effects.md](../effects.md) (prefix `EF-`).
Main code: [potions.js](../../kubejs/startup_scripts/cooking/potions.js) (all potions and mixes) and patcherMod
`fluidbrewing/` + `potion/` (`development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/`, shortened
to `CE/` below).

Jar facts checked 2026-10-21 by decompiling: buzzier_bees 5.0.1 (`BBMobEffects`), blueprint 6.2.0 (`DataUtil.addMix`),
morejs 0.3.0 (`PotionBrewingRegisterEvent*`, `MoreJSForge`), alexsmobs 1.21.1 (`AMEffectRegistry`, `EffectFleetFooted`,
`EffectOiled`, `EffectOrcaMight`), Quark 3.4-418 (`CrabsModule`), Farmer's Respite 2.0 (`CaffeinatedEffect`),
Rustic Delight 1.3.0 (`ModPotions`, `ModBrewingRecipe`), IE 9.2.4 (`EventHandler.onLivingHurt`). Vanilla facts
(Saturation is an instant effect) are from memory and marked so.

## Scope and order

| Step | Rows | What | Work | Why this order |
|---|---|---|---|---|
| 1 | #5 | Delete the 3 duplicate Luck mixes | KubeJS, 3 lines | Bug, trivial, no decision |
| 2 | #4, #6 | Reagent and strength retune in `CUSTOM_POTIONS` / `CB_POTION_MIXES` | KubeJS | Same file as step 1, needs only your picks |
| 3 | #2, #3 | Batch sizes and the Refinery shortcut | patcherMod (one rebuild) | Both touch `Lingerification.java`; do in one build |
| 4 | #8 | Overlap trims: Quark Resilience brew off (config), Caffeinated mixin | config + patcherMod | Can ride the step 3 build |
| 5 | #7 | New potions for modded effects | patcherMod class + KubeJS mixes + lang | Largest; reuses the step 3 build if decided by then |
| 6 | #9 | Shock effects: no merge needed; optional Conductive extension | none / patcherMod | Answer is "nothing to merge"; close or extend |

Batch one patcherMod build for #2, #3, #8 (Caffeinated) and #7. Everything in `potions.js` is startup: restart, not
`/reload`.

Related rows:
- `tooling/conventions #1` (raw `e.custom` in `potion_bottling.js` / `tipped_arrows.js`; `NCH.ieBottling` needs NBT
  results first, see [potions.md](../potions.md) F6).
- `tooling/patchermod-cleanup #1` (`TEMP diagnostic` in `PotionFluidHandlerMixin` line 62, potions.md F4).
- `alcohol/decisions #13` (Brewin' / Vinery effects stay unbrewable) bounds #7.
- `gear/tuning #6` (felsic `potionMb`) and the felsic / dragon assemblies consume `creativeengineering:magic_resistance`
  and `alexsmobs:long_knockback_resistance` fluids, so #4 and #8 change gear costs.
- `potions/effects #7` (Faraday armor shock check) sits next to #9.

---

## potions #5: Luck brewed twice
Status **Bug** · Work: KubeJS · id `potions/potions #5`

### Goal
Only add the brewing mixes Buzzier Bees doesn't already have.

### Now
- `potions.js` lines 87-93, `LUCK_POTION_MIXES`:
  1. `four_leaf_clover` + `minecraft:awkward` → `minecraft:luck`
  2. `redstone` + `minecraft:luck` → `buzzier_bees:long_luck`
  3. `buzzier_bees:silverfish_bottle` + `minecraft:awkward` → `buzzier_bees:unluck`
  4. `redstone` + `buzzier_bees:unluck` → `buzzier_bees:long_unluck`
- Buzzier Bees 5.0.1 `BBMobEffects.registerRecipes()` (run from `BuzzierBees.commonSetup` `enqueueWork`) adds, through
  Blueprint `DataUtil.addMix` → `PotionBrewing.addMix` (`m_43513_`), i.e. real item-agnostic `Mix` entries (verified):
  awkward + clover → luck; luck + redstone → long_luck; luck + glowstone → strong_luck; luck + fermented spider eye →
  unluck; unluck + redstone → long_unluck; unluck + glowstone → strong_unluck; long_luck / strong_luck + fermented
  spider eye → long_unluck / strong_unluck.
- So pack rows 1, 2 **and 4** are exact duplicates (the TODO row and effects.md F6 said rows 1 and 2; F6 corrected).
  Only row 3 (silverfish bottle → Bad Luck) is new.
- Duplicate `Mix` entries are not caught by `PotionMixDedup` (it only drops Forge `BrewingRecipe`s a `Mix` covers), so
  each duplicate becomes two Create Automatic Brewing recipes per bottle type and two IE Mixer recipes.
- The comment at `potions.js` lines 80-84 says there is no "Luck II" in vanilla or buzzier_bees; wrong,
  `buzzier_bees:strong_luck` / `strong_unluck` exist.

### Build
1. `potions.js`: cut `LUCK_POTION_MIXES` to row 3 only; rename it (e.g. `UNLUCK_POTION_MIXES`) or keep the name.
2. Rewrite the comment above it (lines 80-86): Buzzier Bees brews Luck / Bad Luck with long and strong steps itself;
   the pack adds only the silverfish bottle base. Move the `//TODO: give better/magic brewing ingredient` to #4's code
   if #4 keeps it open.
3. While there: use dict keys (`global.item.silverfish_bottle`, `global.item.redstone`) instead of literals (CLAUDE.md
   rule; both keys exist, `item_dict.js` lines 11846 and 1430).

### Ids and data
`buzzier_bees:silverfish_bottle` (`i.silverfish_bottle`), `buzzier_bees:unluck` / `long_unluck` (verified, jar).

### Decisions
None.

### Gotchas
- The log count `[CE potions] added N item-agnostic potion mixes` drops by 3; update any note that quotes N.

### Tests
- **PO-27 Luck once:** JEI → Potion of Luck → R: one brewing recipe per route; Create Automatic Brewing page shows one
  awkward + clover → Luck per bottle type (EF-05 covers the same).
- **PO-28 Bad Luck from silverfish:** awkward + silverfish bottle → Potion of Bad Luck; + redstone → long (BB's step).
- PO-25 stays valid.

---

## potions #4: Placeholder reagents
Status **Planned** · Work: KubeJS · id `potions/potions #4`

### Goal
Replace the two placeholder reagents with something that fits: Magic Resistance (amethyst shard) and Luck (four-leaf
clover, `TODO` comment at `potions.js` line 86).

### Now
- Magic Resistance: `CB_POTION_MIXES` line 72, `['minecraft:amethyst_shard', 'minecraft:thick', 'creativeengineering:magic_resistance']`,
  then redstone / glowstone (73-74). Potions registered in patcherMod `CE/potion/MagicResistancePotions.java`
  (3:00 / 8:00 / 1:30 II), effect in `MagicResistanceEffect.java`, tuning in CEConfig `[magic_resistance]`
  ([armor_effects.md](../armor_effects.md) 2.5).
- The Magic Resistance fluid is consumed by every felsic sequenced assembly (CEConfig `[crafting] felsic_*` `potion` =
  `creativeengineering:magic_resistance`, 250-500 mB each, `CEConfig.java` lines 309-321), so the reagent sets the
  felsic gear cost.
- Luck: after #5 the pack adds no Luck brew; the clover is Buzzier Bees' own reagent. Changing it means removing BB's
  `Mix` and adding a new one.

### Build
1. Magic Resistance: change the reagent in line 72 to the chosen item via its dict key.
2. Luck (only if you want a different reagent): in `MoreJSEvents.registerPotionBrewing`, before the adds:
   ```js
   event.removeByPotion(potion('minecraft:awkward'), Ingredient.of(global.item.four_leaf_clover), potion('minecraft:luck'))
   mix(NEW_REAGENT, 'minecraft:awkward', 'minecraft:luck')
   ```
   `removeByPotion(Potion|null, Ingredient|null, Potion|null)` exists in MoreJS 0.3.0 and edits
   `PotionBrewing.POTION_MIXES` (verified). MoreJS posts the event in `FMLClientSetupEvent` /
   `FMLDedicatedServerSetupEvent` (verified, `MoreJSForge`), which is after BB's common-setup `enqueueWork`, so BB's
   mix is there to remove. (The `potions.js` comment at line 206 says MoreJS runs in common setup; it doesn't.)
3. Delete the `TODO` comment once settled.

### Ids and data
- Current: `minecraft:amethyst_shard` (`i.amethyst_shard`), `buzzier_bees:four_leaf_clover` (`i.four_leaf_clover`).
- Candidates already in `item_dict.js`: `botania:mana_powder` (`i.mana_powder`), `botania:pixie_dust`
  (`i.pixie_dust`), `geode:galcite` (`i.galcite`), `minecraft:ghast_tear`, `minecraft:shulker_shell`. Geode+'s
  wrappist and celestite shards are taken (Long Reach, Phantom Bane).

### Decisions
- **Magic Resistance reagent.** Recommend `botania:mana_powder`: "magic", renewable from a mana pool, mid-game, not
  used by another potion. Note it raises felsic gear cost slightly.
- **Luck reagent.** Recommend keeping Buzzier Bees' four-leaf clover (it is the thematic luck item and BB's own design)
  and just deleting the `TODO`. If you want it harder, `botania:pixie_dust`.

### Gotchas
- The felsic assemblies read the potion id, not the reagent, so nothing else changes.
- JEI and Create Automatic Brewing are rebuilt only at startup.

### Tests
- **PO-29 Magic Resistance reagent:** thick + new reagent → Magic Resistance on the stand, Create mixer and IE Mixer;
  amethyst shard no longer brews it. Update PO-24 to the new reagent.

---

## potions #6: Saturation potion strength
Status **Planned** · Work: KubeJS · id `potions/potions #6`

### Goal
Decide how much a Potion of Saturation should feed, and give it a strong step if wanted.

### Now
- `CUSTOM_POTIONS.saturation` (`potions.js` line 43): `dur: 5` ticks, amplifier 0, thick + golden carrot, no long or
  strong.
- **From memory (vanilla 1.19.2, not checked against a jar):** `minecraft:saturation` is an `InstantenousMobEffect`.
  A drunk potion applies instant effects once through `applyInstantenousEffect`, ignoring duration, and Saturation's
  tick is `FoodData.eat(amplifier + 1, 1.0F)`. So the potion gives **+1 hunger, +2 saturation**, whatever `dur` says.
  A golden carrot eaten gives 6 / 14.4, so the brew is worth much less than its reagent. Duration only matters for
  `effect`-command or food-applied Saturation, not potions.
- Splash scales instant effects by distance; lingering clouds and the pack's potion buckets re-apply instant effects
  every reapplication tick while you stand in them, so a Saturation bucket can feed a lot more
  ([potion_buckets.md](../potion_buckets.md) Known limits, "effects stack per ring").
- `CUSTOM_POTIONS` already supports an amplifier: `amp` (line 149, `p.amp || 0`) and `strong: [dur, amp]`.

### Build
1. Confirm the instant behaviour in game first (test PO-30 below).
2. Set the amplifier, e.g. `saturation: { effect: 'minecraft:saturation', dur: 1, amp: 5, strong: [1, 9], base: 'thick', reagent: global.item.golden_carrot }`
   (amp 5 → 6 hunger / 12 saturation, about a golden carrot; strong amp 9 → 10 / 20, a full bar). No `long`: duration
   doesn't matter.
3. Update potions.md 2.1 (Saturation row) and F8.

### Ids and data
`minecraft:saturation` (`global.effect.saturation`), `minecraft:golden_carrot` (`i.golden_carrot`, line 2296).

### Decisions
- **Strength.** Recommend base amp 5 and strong amp 9 as above: the potion then matches the reagent and the strong
  one is a full refill, which is the point of brewing it. Keep it if a weak "snack" potion is intended.
- **Potion buckets / lingering re-dosing.** Recommend checking PO-31 before shipping; if a Saturation bucket refills
  a whole party many times, leave the bucket as is (it's a feature) or exclude instant effects from re-application in
  `potion_area.js` (separate change, `potions/potion-buckets`).

### Gotchas
- Tipped arrows give 1/8 duration but instant effects ignore duration, so Arrows of Saturation feed what they hit.
- AppleSkin shows the restore amount on the potion only if it reads effects; verify with the hunger bar instead.

### Tests
- **PO-30 Saturation is instant:** at 10 hunger, drink a base Potion of Saturation with AppleSkin → hunger +amp+1 at
  once, no lingering effect icon.
- **PO-31 Saturation cloud:** lingering Saturation potion and lingering Saturation bucket on yourself at low hunger;
  record how much they restore.

---

## potions #2: Batch sizes differ between machines
Status **Planned** · Work: patcherMod · id `potions/potions #2`

### Goal
Settle one rule for potion batch sizes, especially the dragon's breath exchange rate into lingering.

### Now
| Machine | Batch | Where (`CE/` unless noted) |
|---|---|---|
| Create Automatic Brewing (every mix) | 1000 mB | Create `PotionMixingRecipes.createRecipes()` (Create's own, hardcoded) |
| Create mixer, splash + 250 mB dragon's breath fluid → lingering, heated | 1000 mB | `fluidbrewing/DragonBreathBrewing.java` `AMOUNT` line 56, `DRAGON_BREATH_AMOUNT` line 59 |
| IE Mixer, every mix (regular, splash, lingering) | 1500 mB | `mixin/PotionRecipeGeneratorsMixin.java` `CREATIVEENGINEERING$AMOUNT` line 73 |
| IE Mixer, conversion into lingering (splash + dragon's breath item) | 1000 mB | same file, `LINGERING_AMOUNT` line 80, used at lines 211-214 |
| IE Refinery Lingerification, regular or splash + 250 mB `#forge:fluid/dragon_breath` | 1500 mB | `fluidbrewing/Lingerification.java` `POTION_AMOUNT` line 63, `DRAGON_BREATH_AMOUNT` line 66, `ENERGY` 120 line 69 |
| Lava Vision base (IE / Create) | 1500 / 1000 mB lava | `PotionRecipeGeneratorsMixin` `addLavaVision`; [lava_vision.js](../../kubejs/server_scripts/magic/lava_vision.js) |

The dragon's breath rate is the inconsistent part: 250 mB → 1000 mB lingering (Create), 1 bottle → 1000 mB (IE Mixer),
250 mB → 1500 mB (Refinery). The IE Mixer comment (lines 75-79) says lingering runs at 1000 "so Create / IE stay in
step". The `potions.js` header (line 19) still says the IE Mixer runs at "1000 mB"; stale code comment.

### Build
Recommended version (see Decisions):
1. `Lingerification.java` line 63: `POTION_AMOUNT = 1000`. JEI's Lingerification page and recipes regenerate at the
   next `/reload` (generated list), but the class is in the jar, so rebuild patcherMod.
2. Optional, if you want knobs: add a CEConfig COMMON section `[potions]` (new `Potions` class in `CEConfig.java`, next
   to `Effects` at line 2631, registered with the others at lines 36-112):
   `ieMixerMb` (1500), `ieMixerLingeringMb` (1000), `lingerificationMb` (1000), `lingerificationDragonBreathMb` (250),
   `lingerificationEnergy` (120), `createDragonBreathMb` (250). Replace the `static final` constants with reads of
   `CEConfig.POTIONS.<key>.get()` inside the methods. IE Mixer and Refinery lists rebuild on `/reload`;
   `DragonBreathBrewing.build()` runs once in common setup (`CreativeEngineering.java` line 287), so its value needs a
   restart. Create Automatic Brewing's 1000 is Create's own and stays.
3. Fix the `potions.js` line 19 comment (1500, not 1000) and update potions.md 2.4 and F1.

### Ids and data
`create:potion` with `Bottle` / `Potion` NBT; `create_central_kitchen:dragon_breath` (fluid, Create mixer) and
`#forge:fluid/dragon_breath` (Refinery); `minecraft:dragon_breath` item (IE Mixer).

### Decisions
- **One size or not.** Recommend: one exchange rate for lingering everywhere (250 mB fluid or 1 bottle → 1000 mB),
  i.e. only Lingerification drops to 1000; keep the IE Mixer's 1500 mB for regular and splash brewing as the
  multiblock's throughput perk. Alternative: everything 1000 (IE stock), which loses that perk.
- **Config knobs.** Recommend skipping them unless you expect to retune; the constants are one line each.

### Gotchas
- Mixin classes can't have non-private static fields; keep config reads inside the injected methods.
- `FluidTagInputMixin` matches NBT as a subset, so changing amounts doesn't affect matching.

### Tests
- **PO-32 Lingerification rate:** IE Refinery, 1000 mB splash Resistance + 250 mB dragon's breath → 1000 mB lingering
  Resistance (update PO-17).
- PO-11, PO-14 unchanged.

---

## potions #3: Refinery regular → lingering shortcut
Status **Planned** · Work: patcherMod · id `potions/potions #3`

### Goal
Decide whether the IE Refinery may turn a regular potion straight into lingering (every other machine needs splash
first, vanilla's rule).

### Now
`Lingerification.generate()` adds two recipes per potion: `out.add(build(potion, BottleType.REGULAR, slug + "_from_regular"))`
(line 104) and the splash one (line 105). Recipe ids `creativeengineering:lingerification/<potion>_from_regular`.

### Build
- Keep: nothing to build; say so in potions.md 2.4 / F2 and close.
- Drop: delete line 104, update the class javadoc (lines 26-31, "for both the REGULAR and the SPLASH bottle type"), and
  potions.md 2.4 / PO-17. Optional config switch `[potions] lingerificationFromRegular` (default as decided) if #2 adds
  the section.

### Ids and data
None new.

### Decisions
- **Keep or drop.** Recommend **keep**: it saves only one gunpowder step, the Refinery costs power (120 RF/t) and an IE
  multiblock, and it gives the Refinery a reason to exist next to the Mixer. Drop if you want vanilla's rule strictly.

### Gotchas
- Halves the Lingerification JEI page if dropped; nothing else reads those ids.

### Tests
- Keep: PO-17 as is. Drop: **PO-33** Refinery with regular Resistance + dragon's breath → nothing; splash still works.

---

## potions #8: Total-overlap effect groups
Status **Planned** · Work: KubeJS / config / patcherMod · id `potions/potions #8`

### Goal
Of the 12 total-overlap groups ([effects.md](../effects.md) 2.3, F5), trim the two that cost something; leave the rest.

### Now
- **Knockback resistance:** `alexsmobs:knockback_resistance` (AM, bear fur, `AMEffectRegistry.init`, Forge
  `BrewingRecipe`s, plus the pack's item-agnostic long / strong mixes at `potions.js` lines 112 and 118) and
  `quark:resilience` (Quark `CrabsModule`: `QuarkEffect` +0.5 knockback resistance per level, brewed from
  `quark:crab_shell` through `BrewingHandler.addPotionMix("crab_brewing", ...)`). Same attribute, same amount
  (verified both). The pack itself uses AM's: dragon boots assembly fluid `alexsmobs:long_knockback_resistance`
  (`end_gear.js` line 119), dragon shovel held effect (`tool_effects.js` line 145), armor sets (`armor_effects.js`
  lines 232, 254). Nothing in KubeJS references `quark:crab_shell` or `quark:resilience`.
- Quark config `[mobs.crabs]` (`config/quark-common.toml` line 1415 and the identical
  `configs_edited/root_config/quark-common.toml`): `"Enable Brewing" = true`, `"Resilience Required For All Effects" = true`.
  In the jar, `enableBrewing` is the `crab_brewing` flag and also gates the `crab_shell` item (`setCondition`); the
  second key adds Resilience to "How Did We Get Here?" (and "A Furious Cocktail" when brewing is on).
- **Phantoms:** `farmersrespite:caffeinated`. `CaffeinatedEffect.applyEffectTick` (`m_6742_`) sets the player's
  `TIME_SINCE_REST` stat to `-(24000 * (amp + 1))` **every tick** (verified). Phantoms spawn past 72000 ticks
  (3 days), so after Caffeinated I ends you are phantom-free for 96000 more ticks (4 days): stronger than
  `better_beacons:phantom_bane`, which only works while active. Caffeinated comes from 10 teas and coffees
  (effects.md 2.1), cheap; Phantom Bane is a brewed potion and the echo armor set bonus.
- The other 10 groups (wall climbing, random teleport, immobile, Stimulation, poison immunity, Jellie, Double Jump,
  floating on water, Conduit Power, Xaero switches) are food-only, mob-only or vanilla: no extra potion line, no cost.

### Build
1. **Resilience:** in both `config/quark-common.toml` and `configs_edited/root_config/quark-common.toml`, `[mobs.crabs]`:
   `"Enable Brewing" = false` and `"Resilience Required For All Effects" = false` (otherwise "How Did We Get Here?"
   asks for an effect nobody can get). This also hides `quark:crab_shell` (no pack use). Restart.
2. **Caffeinated:** new patcherMod mixin `CaffeinatedEffectMixin`, `@Mixin(targets = "umpaz.farmersrespite.common.effect.CaffeinatedEffect", remap = false)`,
   `@Inject(method = "m_6742_", at = @At("HEAD"), cancellable = true)` (SRG name, as `BonemealCharmMixin` does for
   `m_6225_`). Replace the reset with "pause": for a `ServerPlayer`, read
   `getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST))` and write it back minus 1 (floor 0) so the
   per-tick increment is cancelled while caffeinated, then `ci.cancel()`. The speed and attack-speed attribute
   modifiers in the constructor stay. Register in `creativeengineering.mixins.json`. Optional CEConfig `[effects]`
   switch `caffeinatedPausesRest` (default true) so it can be turned back.
3. Update effects.md 2.3 / F5 and its Caffeinated description (JEED lang overlay, `kubejs/assets/creativeengineering/lang/en_us.json`:
   "keeps you from getting tired while it lasts" instead of a reset).

### Ids and data
`quark:resilience`, `quark:crab_shell`, `alexsmobs:knockback_resistance` (+ `long_` / `strong_`),
`farmersrespite:caffeinated`, `better_beacons:phantom_bane`. All verified in jars. None is in `effect_dict.js`
(vanilla only); add `ef.knockback_resistance = 'alexsmobs:knockback_resistance'` etc. under a new
`// ##### Modded #####` block if new code references them.

### Decisions
- **Which knockback line.** Recommend keeping AM's (the pack's gear already uses it, and the pack re-added its
  modifiers for every machine) and turning Quark's brew off by config.
- **Caffeinated.** Recommend "pause" (no carry-over, no phantoms only while caffeinated, Phantom Bane keeps a purpose
  as the potion version). Alternatives: cancel the stat change entirely (Caffeinated becomes speed only), or accept.
- **Restfulness** (Upgrade Aquatic, brewable, resets the counter once) is listed as `slight`; recommend leave.

### Gotchas
- Quark's `crab_brewing` flag also gates the crab's shell drop (`data/quark/loot_tables/entities/crab.json`, pool
  `shell` has a `quark:flag` condition; verified), so no disabled item drops. Check JEI that nothing else shows the
  shell.
- `Stats.TIME_SINCE_REST` increments in `ServerPlayer` / `Player.tick` only while awake; test with `/time add` and
  `/stats`-style checks (`/scoreboard objectives add rest minecraft.custom:minecraft.time_since_rest`).
- Existing worlds: players already caffeinated keep a big negative stat from before the change; harmless.

### Tests
- **PO-34 Resilience gone:** JEI shows no Potion of Stability / Resilience recipe; "How Did We Get Here?" criteria no
  longer list Resilience; Knockback Resistance still brews (bear fur).
- **PO-35 Caffeinated pauses rest:** scoreboard on `minecraft.time_since_rest`; drink coffee → value stops rising; when
  the effect ends it resumes from the same value (not negative).

---

## potions #7: Potions for unbrewable effects
Status **Planned** · Work: patcherMod + KubeJS · id `potions/potions #7`

### Goal
Give a potion line to the beneficial modded effects that have none, within the exclusions: Brewin' and Vinery effects
stay unbrewable (`alcohol/decisions #13`), Fruits Delight stays food-only for now.

### Now
- Candidates from effects.md F12, after exclusions:

| Effect | Mod | What it does (from the jar) | Brewable now |
|---|---|---|---|
| Fleet-Footed `alexsmobs:fleet_footed` | Alex's Mobs | +0.2 movement speed while sprint-jumping (flat, amplifier unused) | no |
| Orca's Might `alexsmobs:orcas_might` | Alex's Mobs | +3 attack speed per level (ADDITION modifier) | no |
| Oiled `alexsmobs:oiled` | Alex's Mobs | in water or rain: pushed up (sneaking: fall distance reset instead); amplifier unused | no (Fish Oil food) |
| Agility `neapolitan:agility` | Neapolitan | climb any wall | no (bananas) |
| Allure `botania:allure` | Botania | fishing lure | Botanical Brewery only |

- Excluded by `alcohol/decisions #13`: Climbing, Water Walker, Lava Walker, Magnet, Experience (Vinery port).
- **Haste is done:** Rustic Delight registers `rusticdelight:haste` / `long_haste` / `strong_haste` and brews them with
  its own `ModBrewingRecipe` (an `IBrewingRecipe`, invisible to Create's and IE's generators; verified). The pack
  re-adds the three steps as `Mix` entries (`RD_POTION_MIXES`, `potions.js` lines 101-105), PO-07 tests it. Nothing to
  build; tick that part of the row.
- Mechanism: KubeJS's `StartupEvents.registry('potion')` runs before modded effects exist (header comment lines
  129-137), so modded-effect potions are Forge `DeferredRegister<Potion>` in patcherMod, as `CE/potion/BetterBeaconsPotions.java`
  (with a `ModList` guard) and `MagicResistancePotions.java`, registered at `CreativeEngineering.java` lines 85-86;
  brewing mixes go in `CB_POTION_MIXES` in `potions.js`.
- Potion names live in `kubejs/assets/kubejs/lang/en_us.json` (`item.minecraft.potion.effect.<path>`, `splash_potion`,
  `lingering_potion`, `tipped_arrow` keys, e.g. lines 35-40, 74-79, 113-118 for Long Reach / Magic Resistance).

### Build
1. patcherMod: new `CE/potion/ModdedEffectPotions.java`, generalising `BetterBeaconsPotions`: a table of
   `{name, modid, effectPath, base, long, strongDur, strongAmp}`; in `register(IEventBus)`, for each row whose
   `ModList.get().isLoaded(modid)`, call `POTIONS.register(name, supplier)` (the supplier looks the effect up at
   `RegisterEvent<Potion>` time, after every mod's effects exist). Register the class next to lines 85-86.
2. `potions.js`: add the base / lengthen (/ amplify) rows to `CB_POTION_MIXES` (or a new `MODDED_POTION_MIXES`), using
   dict keys for reagents. Everything else (Create, IE Mixer, Lingerification, bottling, buckets, tipped arrows,
   JEI fluid list) follows automatically from the registry.
3. Lang: 4 keys per potion id (potion, splash, lingering, tipped arrow) for base, long, strong in
   `kubejs/assets/kubejs/lang/en_us.json`. Run `tools/effect_audit.py` to check names.
4. effects.md 2.1 / 2.2: set Brew = `Y (pack)` and Arrow for the new ones.

Sketch rows (durations follow the other modded potions, 3:00 / 8:00 / 1:30 II):

| Potion id | Effect | Base + reagent | Long | Strong |
|---|---|---|---|---|
| `creativeengineering:fleet_footed` | `alexsmobs:fleet_footed` | awkward + `alexsmobs:roadrunner_feather` (`i.roadrunner_feather`) | yes | no (amp unused) |
| `creativeengineering:orcas_might` | `alexsmobs:orcas_might` | awkward + `alexsmobs:shark_tooth` (`i.shark_tooth`) | yes | no (+6 attack speed is too much) |
| `creativeengineering:oiled` | `alexsmobs:oiled` | awkward + `alexsmobs:fish_oil` (`i.fish_oil`) | yes | no |
| `creativeengineering:agility` | `neapolitan:agility` | awkward + `neapolitan:dried_banana` (`i.dried_banana`) | yes | no (check amp use) |

### Ids and data
Effect ids verified in the AM jar (`AMEffectRegistry`) and effects.md 2.1 (Neapolitan, Botania). Reagent items are in
`item_dict.js` (lines 655, 1364, 116, 12412). Potion ids are new, `creativeengineering:` namespace. No potion dict
exists; potion ids stay in `potions.js` / the Java class (recommend not adding one for four ids).

### Decisions
- **Which, if any.** Recommend Fleet-Footed, Oiled and Agility (movement utilities with no potion and only food or
  mob sources) and Orca's Might without a strong step. Skip Allure: Botania's Brewery already makes it and
  `gear/tuning #13` may want a lure bonus for the dragon fishing rod.
- **Reagents.** As in the sketch; each is the effect's own source item where one exists.
- **Fruits Delight end-game potion base** (row text): out of scope; leave it on the row as an idea.

### Gotchas
- `EffectFleetFooted` keeps `lastDuration` / `removeEffectAfter` as fields on the one shared effect object, so two
  entities with the effect interfere; a potion makes that more common (splash on several mobs). Check in MP.
- Potion paths become translation keys (`item.minecraft.potion.effect.<path>`); `oiled` / `agility` must not collide
  with another mod's potion path (none found in the effects table; recheck `/dumppotions` after).
- `PotionMixDedup` and the IE `EMPTY` skip need nothing new: these are plain `Mix` entries from real potions.

### Tests
- **PO-36 New potions brew:** each base brew on the stand, Create mixer and IE Mixer; long version with redstone.
- **PO-37 New potions everywhere:** splash, lingering, tipped arrow and potion bucket of each show a name (no raw key)
  and give the effect.

---

## potions #9: Shock and electric effects
Status **Planned** · Work: none / patcherMod · id `potions/potions #9`

### Goal
Find out whether several shock / electric effects exist and merge duplicates.

### Now
From the 140-effect table (effects.md 2.1) and the jars:
- **Shocked** `createaddition:shocking` (Create Crafts & Additions tesla coil): stops movement. The only shock effect.
  Its overlap group is "immobile" (Scared Still, Power Outage), not electric.
- **Conductive** `immersiveengineering:conductive`: a vulnerability, not a shock. IE `EventHandler.onLivingHurt`
  multiplies damage by `1.5 + 0.5 * amp²` only for `"flux"` damage, `razorShock` and `IEDamageSources.ElectricDamageSource`
  (verified). The pack puts it on copper, iron and gold armor (`armor_effects.js` lines 207-220).
- **Stunned** `immersiveengineering:stunned`: stops endermen teleporting; not electric.
- **Power Outage** `alexsmobs:power_down`: April Fools freeze; electric only by name.
- The pack's damage tag `creativeengineering:electric` (`development/creativeCommon/src/main/resources/data/creativeengineering/damage_tags/electric.json`)
  lists IE tesla / wire / razor shock, CCA `tesla_coil`, SecurityCraft electricity / taser and `lightningBolt`.

So there is nothing to merge.

### Build
- Close the row (no duplicates). Optional extension, if you want copper / iron / gold armor's Conductive to mean
  something outside IE: a patcherMod `LivingHurtEvent` handler that applies IE's multiplier for any source in
  `DamageTags.is(source, DamageTags.id("electric"))` that IE doesn't already handle (skip `ieTesla*`, `ieWireShock`,
  `ieRazorShock` to avoid double-dipping). Put the multiplier behind CEConfig `[effects] conductiveAllElectric`.

### Ids and data
Effect ids verified (IE jar, effects.md table). Damage tag file above.

### Decisions
- **Which survives.** None needed. Recommend closing; the Conductive extension is a separate design call (lightning and
  CCA tesla coils would hit metal-armored players 1.5× harder).

### Gotchas
- `potions/effects #7` (Faraday armor shock check) needs the same damage-source list; reuse it.

### Tests
- Only for the extension: **PO-38** iron armor, stand by an active CCA tesla coil → damage 1.5× the unarmored-by-iron
  baseline.

---

## Built, needs play

| Row | Status | Note |
|---|---|---|
| potions #1 Potions and brewing | Complete | PO-01 to PO-26 never run. Stale bits found (fixed in potions.md unless noted): `createEmptying` exists (F6), common-setup lines are 287-288, Luck duplicates are rows 1, 2 and 4. Stale code comments, not edited: `potions.js` line 19 (IE Mixer "1000 mB", it is 1500), lines 80-84 ("no Luck II in buzzier_bees"), line 206 (MoreJS "FMLCommonSetupEvent"; it fires in client / dedicated-server setup) |

## Writeup corrections made

- [effects.md](../effects.md) F6: Buzzier Bees also lengthens / amplifies Bad Luck, so pack rows 1, 2 **and 4** are
  duplicates (jar-verified); added the verification note.
- [potions.md](../potions.md) 2.3 (Buzzier Bees row): "first two rows are duplicates" → rows 1, 2 and 4.
- [potions.md](../potions.md) F6: `createEmptying` is no longer missing (line 1449); noted that `ieBottling` drops
  result NBT.
- [potions.md](../potions.md) section 5: `CreativeEngineering.java` lines 262-263 → 287-288.
