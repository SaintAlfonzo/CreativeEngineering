# Creative Delight: Delight add-on backports and native builds in one jar

Written 2026-10-25, scope revised 2026-10-25 (owner). **Status: framework built, scratch test queued (2026-10-25, section 19):**
startup loader and helpers in patcherMod, NCH namespace option, repack and bundle build, the Farmer's Delight / Ocean's /
Oceanic / End's repacks nested in `creative_delight-1.0.0.jar`, Oceanic fishing net overlay; FD staged for the 1.3.4
backports (section 21). Not installed in `mods/`. The four ports, the QoL moves, the
member script rows, the tool and misc dicts and swappers are not done (section 19.3); the food dict is built (13.5, 2026-10-25). Goal (owner):
fold the Delight add-ons the pack owns into a single Forge 1.19.2 jar, **Creative Delight**, "multiple jars in a trenchcoat".
Members: four ports from `development/backported*` (Veggies, Vintage, Fungi, Miner's), four third-party native 1.19.2
Delight jars (End's, Ender's, Ocean's, Oceanic), and the Delight-specific pack Java that today sits in patcherMod.
**Backported Brewin and Backported Vinery are out of this plan for now** (parked, section 7). This file compiles what
changed in each port (read from the writeups, the `gradle.properties` / `mods.toml` of each project, and `mod_sides.md`),
what KubeJS and patcherMod do to each Delight mod, the things that constrain a merge, and a proposed merge process. Status of
each member stays in its own writeup; this file does not repeat build records.

Written for: the owner, as the working reference for the merge session.

**Revision note (second round, 2026-10-25):** sections 8 to 18 carry the latest owner decisions and **override** earlier text
where they differ: 3.1 rule 1 and 3.2 (what moves), 2.1 and Q2 (Ender's), 6.1 and 6.2 (flat jar, build, bake), and 0.3
(naming). Sections 0 to 2 are provisional until the overlap analysis fixes the member list (section 16).

**Revision note (third round, 2026-10-25): KubeJS scripts in the jar.** CD requires KubeJS through patcherMod, and KubeJS
1902.6.2 already loads server and client scripts from any mod jar (section 10.2, read from the bytecode). So CD's pack
integration is written as **KubeJS scripts shipped in the CD jar that call the pack helpers** (`global.NCH`,
`global.tags`, `global.LOOT`, the compost helper, the dicts). The Java rules engine of the second round (old 10.2 option
B, the `ce_rules` schema, the engine work list) is **dropped**; patcherMod only has to add a startup-script loader
(10.5). Sections 3.1, 4, 6, 8, 10, 11, 12, 13.3 and 17 are rewritten to match; older text that mentions "rule data" or "the
engine" now means these scripts.

## 0. At a glance

### 0.1 Ports (own projects under `development/`)

| Port | Project | Mod id | Upstream, ported version | Writeup | Test prefix | State (2026-10-25) |
|---|---|---|---|---|---|---|
| Veggies Delight | `backportedVeggies` | `veggiesdelight` | mehdinoui 1.9.2 (Forge 1.20.1) | [veggies_delight.md](veggies_delight.md) | `VD-` | `-02` built, live since 1.1.02 |
| Vintage Delight | `backportedVintage` | `vintagedelight` | Ribs 0.1.6 (Forge 1.20.1) + KOTS 0.3.1 ids | [vintage_delight.md](vintage_delight.md) | `VTD-` | `-03` built, live since 1.1.02 |
| Fungi Delight | `backportedFungi` | `fungidelight` | Eggmain 1.3 (Forge 1.20.1) | [fungi_delight.md](fungi_delight.md) | `FGD-` | `-01` built, scratch test queued |
| Miner's Delight | `backportedMiners` | `miners_delight` | Sammy Semicolon 1.4.5 + the live 1.1.2 merged | [miners_delight.md](miners_delight.md) | `MND-` | `-01` built, not in `mods/` |

### 0.2 Third-party native 1.19.2 jars (no port; today only pack integration)

| Mod | Jar in `mods/` | Mod id | Licence (from `mods.toml`) | Writeup | Integration so far |
|---|---|---|---|---|---|
| End's Delight | `ends_delight-1.19.2-2.1.jar` | `ends_delight` | MIT (FoggyHillside) | [ends_delight.md](ends_delight.md) | none (boot check only) |
| Ender's Delight | `endersdelight-1.19-1.2.2.jar` | `endersdelight` | **All rights reserved** (Ax3dgaming, Furti Two) | [ends_delight.md](ends_delight.md) | dict, cooking, potions, `end.js`, shield and damage tags |
| Ocean's Delight | `oceansdelight-1.0.2-1.19.2.jar` | `oceansdelight` | MIT (Scouter456) | [ocean_delight.md](ocean_delight.md) | names (caviar) only |
| Oceanic Delight | `oceanic_delight-1.0.1-forge-1.19.2.jar` | `oceanic_delight` | MIT (DariusZeBaguette, MCreator build) | [ocean_delight.md](ocean_delight.md) | dict block, fishing net mixin and loot tables |

"The Ender and Ocean mods" is read as all four jars above (End's and Ender's, Ocean's and Oceanic). Corrections welcome.

### 0.3 Naming rule (owner)

Every member keeps its registry namespace and its mod id as its own `[[mods]]` entry in the jar. Only the **display name**
changes: `displayName = "<Name> (Creative Delight)"`, e.g. "Veggies Delight (Creative Delight)", "Fungi Delight (Creative
Delight)", "Miner's Delight (Creative Delight)", "End's Delight (Creative Delight)", "Oceanic Delight (Creative Delight)".
Consequences to check at build time: the Forge mod list, JEI's mod-name line and WTHIT's mod-name line read the display name,
so tooltips show the suffix; pack lang overrides that spell a mod name (`namechanges.js`, `jeed_providers`, tooltip
scripts) must be grepped for the old names; `config/jei/ingredient-list-mod-sort-order.ini` is keyed by mod id and is
unaffected; `mod_sides.md` keeps one row for the jar and lists the member ids in its notes. The jar's own top-level mod id
for the manifest is `creative_delight` (no registry content under it).

Not in this jar (native builds with no pack Java, or hooks that belong elsewhere): Hostile Delight ([hostile_delight.md](hostile_delight.md)),
Nether's Delight ([nethers_delight.md](nethers_delight.md); stays the installed third-party jar), the goose golden egg
(`incubationAdditions`). Decision for the pack Java that touches these: section 3.

## 1. What changed in each port

### 1.1 Veggies Delight (Backported Veggies)

- **Content:** 6 of 7 crops (bell pepper is Rustic Delight's), 8 wild plants, mature dandelion, 89 items, village depot
  houses (5 vanilla styles), mob food edits, trades. 215 recipe files; 52 for absent mods and the 10 advancements dropped.
- **Port work:** `Material` on block properties, creative tab subclass, `category` field dropped from recipes, loot and
  worldgen JSON to 1.19.2 form; two upstream data typos fixed in `port_resources.py`.
- **Pack integration:** own rules for wild crops (`wild_crops.json`), mature dandelion (`wild_flowers.json`), seasons,
  food types, cans (24 generated), jars and soft fluids, juices as fluids (carrot, dandelion, turnip water), cooking rules
  (12 grid meals moved to `_manual_only` plus heatless pot routes, tortilla wraps), baking (carrot cake, sweet potato
  pie), loot as `L.add` entries, compost rows, trades and wandering trades. Marked merges: Fish and Chips, Mashed
  Potatoes, Stuffed Bell Peppers; 10 overlaps remain.
- **Build tooling:** `port_resources.py`; 18 Java files; MIT, whole mod (code and assets), notice kept. `-02` ships the
  22 Vintage compat recipes.

### 1.2 Vintage Delight (Backported Vintage)

- **Content (about 134 items, 68 blocks):** oats, peanuts, cucumbers, ghost peppers, salt line, cheese mold and cheese,
  mason jars and jams, pickling jar, nut butter, burgers, pizzas, chef hats, magic peanut vine tree (restored for test),
  Lush Grass.
- **Owner decisions (2026-10-23):** gearo berries purged; Dehydrated effect removed everywhere; fermenting jar renamed
  Pickling Jar; vinegar is the pack's vinegar; every overlap ships marked `*overlaps w/*` for review after play; loot
  tables added but not wired; mob food edits accepted; chef hats replace Simple Hats'; Mixed Seeds postponed.
- **Port notes:** KOTS 0.3.1's jar ids and item handler for the pickling jar, cucumber dough as KOTS, wild ghost pepper
  on nylium or the soil tag, upstream id bugs fixed, 22 Vintage recipes shipped inside the Veggies jar.
- **Pack integration:** wild crops and a Nether rule for ghost peppers, soil tag, 54 food types, 16+4 cans, compost, 8 soft
  fluids, cooking rules (burgers, burrito, heatless cheese pasta), mob audit (no conflict with Veggies), goose golden
  egg in `incubationAdditions` 1.1.0 (1 in 1000, [nests.md](nests.md) 3.11).
- **Build tooling:** `port_resources.py`, README of deviations; 50 Java files; two Lush Grass mixins. MIT 0.1.6, GPL-3.0 KOTS.

### 1.3 Fungi Delight (Backported Fungi)

- **Content:** inky cap and morel (with colonies, huge forms, mushroom cows), truffles from truffle dirt, Weak Stomach and
  Burrowing effects and potions, 32 items, 17 blocks, 34 recipes, 10 dishes, 5 crates. No cheese.
- **Dropped from the port:** config, sniffer, advancements, Botany Pots, compost hook, biome modifiers and placed features
  (8 configured features ship), `has_*` biome tags.
- **Pack-owned systems it introduced:** the **mushroom spawn system** (`mushroom_spawns.json`, surface and cave modes,
  CEConfig `[worldgen.systems] mushroomSpawns`; later reused for Miner's cave carrots), a **mooshroom family** in
  patcherMod (breeding any two of red, brown, inky, morel; Brown spawn chance per biome; mixins
  `MushroomCowBreedingMixin`, `AnimalCanMateMooshroomMixin`), truffle dirt through `wild_crops.json` with a new `step`
  key, pig digging and wolf hunt, mob spawn rows for the four cows, the pack's first JEI info pages.
- **Build tooling:** `port_resources.py`, README; 35 Java files; Hoglin mixin; MIT, notice kept. **Hard dependency:** the
  pack's placed features in `kubejs/data` reference `fungidelight:*`, so a world without the jar fails to load.

### 1.4 Miner's Delight (Backported Miners)

- **A merge, not just a port:** the live 1.1.2 (Forge 1.19.2) and 1.4.5 in one jar, mod id `miners_delight`; 69 items,
  10 blocks, 4 potions, 18 loot modifiers; Union of ids, 1.4.5 wins; `hot_cocoa_cup` dropped (not in 1.4.5, no art).
- **Licence: all rights reserved, code too.** The strictest of the six: the jar holds only a fresh implementation (about
  3,100 lines written from behaviour, not translated) and reads art, data, recipes, loot and lang from the donor
  `miners_upstream/miners_delight-1.20.1-1.4.5-backport.jar` (sha1-pinned in `UpstreamJars`). Pack data changes go through
  a translator, never a file at an upstream path.
- **Mechanics kept intact on purpose:** class names `CopperPotBlockEntity` and `StickyBasketBlockEntity` and method shapes,
  because Create Central Kitchen mixes into them.
- **Pack integration:** cave carrot worldgen as a cave rule in `mushroom_spawns.json` (three Y bands), onion soup cup mapped to
  Brewin' Creamy Onion Soup, haste potions shipped beside Rustic Delight's (overlap, review `MND-40`), every overlap shipped
  marked. `replaces_jars=miners_delight-*.jar`.
- **Build tooling:** `check_donor.py`; 41 Java files; own GPLv3 code with no upstream file.

## 2. Third-party native jars as members

### 2.1 End's Delight 2.1 and Ender's Delight 1.2.2

- Two "End food" add-ons that overlap on chorus, shulker, enderman and endermite food and each ship a stove (`end_stove`,
  `endstone_stove`). Section-by-section comparison and the planned tag unification are in [ends_delight.md](ends_delight.md).
- **End's Delight (MIT, 64 items):** can be folded in as is: its classes and assets keep their package and namespace, its MIT
  notice goes in a `NOTICE-upstream-MIT.txt`. Dragon, shulker, enderman and endermite loot modifiers (`add_*` and `nd_add_*`
  sets) and the four knives (joined into the knife set the pack tunes) come with it.
- **Ender's Delight (all rights reserved, 28 items):** cannot be repacked. The only routes are (a) leave it as its own jar
  beside Creative Delight (it is not a member then, and keeps its own name), or (b) the Vinery/Miner's route: a fresh
  implementation that reads art and data from the unmodified donor jar. Recommended: (a) for the first release; revisit
  after the Delight-end tags (ends_delight.md) are decided, since a merged End food set could retire Ender's overlap.
- Pack Java touching Ender's today: `ShieldConfig`, `damage_tags/burn_tick.json`, `damage_tags/fire.json` (data keyed on
  `endersdelight` ids). These stay in patcherMod under (a).

### 2.2 Ocean's Delight 1.0.2 and Oceanic Delight 1.0.1

- Two seafood add-ons (22 and 47 items) overlapping on tentacles (three raw-tentacle items with Miner's Delight), sea pickle,
  guardian and pufferfish dishes ([ocean_delight.md](ocean_delight.md)). Both MIT, both foldable with notice.
- **Oceanic Delight is an MCreator build** (`net.mcreator.oceanicdelight.*`): its package names, procedures and the fishing
  net must stay byte-compatible for the existing mixin target `FishingNetExtractingProcedure` (it moves with the jar, so the
  `@Pseudo` becomes a normal mixin of a class in the same jar).
- Oceanic's `mods.toml` lists AppleSkin, JEED, JEI and "delight" as dependency entries; check which are mandatory before the
  merged `mods.toml` carries them (a merged jar must not make a member's soft dependency hard for the others).
- Worldgen: Oceanic's wild sea grape and the pack's fishing-net loot tables (`kubejs/data/creativeengineering/loot_tables/gameplay`).

## 3. Pack-side code touching Delight mods: what moves, what stays

Widened scope (owner): every Delight mod KubeJS or patcherMod touches is in this plan, and each piece is assigned either to
the Creative Delight jar or to patcherMod. Counts below are literal `namespace:` strings in `kubejs/**/*.js` outside
`generation_scripts` and `exported` (the dictionary file `item_dict.js` dominates every row), plus patcherMod references.

| Delight mod | KubeJS (files, besides `item_dict.js`) | patcherMod | Member of the jar? |
|---|---|---|---|
| Farmer's Delight | everywhere (base mod) | 23 files | No, the dependency |
| Veggies, Vintage, Fungi, Miner's | recipes, tags, cooking, loot, dicts (the ports' pack integration, section 1) | mooshroom family (Fungi), `MushroomTypeSetter`, `SpawnTuning` mushroom rows | Yes |
| End's, Ender's | `cooking.js`, `end.js`, `potions.js` (Ender's) | Ender's: `ShieldConfig`, damage tag JSON | End's yes; Ender's per 2.1 |
| Ocean's, Oceanic | `namechanges.js` (caviar) | `OceanicDelightFishingNetMixin`, `loot/BiomeTagCondition` | Yes |
| Fruits Delight | `blocktags.js`, `jeed_providers.js`, `cooking.js`, fluid dict | `FruitsDelightLeafCheckMixin` (mixin plugin), `crop_growth.json` | No |
| Delightful | `blocktags.js`, `dust.js`, `ash.js`, `item_merges.js`, `cooking.js`, `namechanges.js` | `crop_growth.json` | No |
| Rustic Delight | `blocktags.js`, `baking_ingredients.js`, `potions.js`, `cooking.js`, `09_crop_drops.js` | `state_lines.json` | No |
| Abnormals Delight | `bark.js`, `item_merges.js`, `cooking.js` | `CEConfig` | No |
| Nether's Delight | `fabric.js` | `StuckInBlock`, `FeastPlatingCategory` (Stuffed Hoglin), `crop_growth.json` | No (nethers_delight.md) |
| Corn, Alex's, More, Quark, Chef's, Display, Storage Delight | `cooking.js`, `jartags.js`, `bark.js`, `misc_mods.js`, `trades.js`, `dump_placeable.js` | `FeastPlatingCategory` (corn), `CounterToppers` (Display), `CEConfig` (Chef's) | No |

### 3.1 Proposed rule (revised third round; Java placement per section 9)

1. **KubeJS stays the language; CD's rows move into CD's own scripts** (third round, section 10). Recipes
   (`global.NCH`), tags (`global.tags`), loot (`global.LOOT`), cooking, compost and tooltip rows that concern a member
   move out of the pack's `kubejs/*_scripts/` into CD script files that ship in the jar and call the same helpers. No
   bake, no second schema. Rows that need a non-member mod (Create, IE, Display Delight, ...) move too, as files with a
   `// requires: <mod>` header (section 12). Rows that only concern non-members stay in the pack (superseded for food by section 29: all food code moved). Generated rows
   (`tools/*.py` output) are generated into CD's script tree instead of the pack's.
2. **Java moves only if its target is a member of the jar and it is specific to that member.** Then it is part of the member
   it patches, so the mixin no longer needs `@Pseudo` and the refmap lives with it. Java aimed at a non-member (Fruits,
   Nether's, Display, Corn) stays in patcherMod.
3. **Shared hooks stay.** Generic pack machinery that members merely call (`CEConfig`, `SpawnTuning`, the placement
   system, `CounterToppers`, `FeastPlatingCategory`, the goose egg in `incubationAdditions`, and the KubeJS helpers CD scripts call) stays in patcherMod or the pack; the jar then
   reads its switches through patcherMod's config the way the ports already do.

### 3.2 Candidates by that rule

| Piece | Today | Proposed home | Why / open point |
|---|---|---|---|
| `OceanicDelightFishingNetMixin` | patcherMod | **Creative Delight** (Oceanic member) | Targets only Oceanic's MCreator procedure; the tags it reads are KubeJS (`loottags.js`); under 3.1 rule 1 they move to the Oceanic folder of CD's scripts |
| `loot/BiomeTagCondition` | patcherMod | patcherMod unless only the fishing-net tables use it | It is a loot condition type under the pack namespace; check other users before moving |
| Mooshroom family (`MooshroomFamily`, `MushroomTypeSetter`, `MushroomCowBreedingMixin`, `AnimalCanMateMooshroomMixin`, `MushroomCowWalkTargetMixin`) | patcherMod | **Creative Delight** (Fungi member), or stay | Fungi-specific breeding, but it edits vanilla mooshrooms and uses `SpawnTuning` and `CEConfig`; moving needs those hooks exposed. Owner call |
| Fungi pig-digging and wolf goals, Hoglin mixin | the Fungi port | Creative Delight | Already part of the port |
| Ender's shield and damage tags | patcherMod | patcherMod, or Creative Equipment if it becomes a mod | Not food (section 9); Ender's is not a member under 2.1 (a) |
| `CounterToppers` Display Delight blocks, `FeastPlatingCategory` | patcherMod | patcherMod | Targets are not members |
| Mushroom spawn system (`mushroom_spawns.json`, `[worldgen.systems] mushroomSpawns`) | KubeJS data + patcherMod | patcherMod | Shared: Fungi built it, Miner's cave carrots use it |
| Wild crop system (`wild_crops.json`, its applier, the crop features, the `wildCrops` switch) | KubeJS data + patcherMod | **Creative Delight** (moved 2026-10-25, section 24) | Owner call: all 63 rules, vanilla included; the biome trait table and `_biome_rules.js` stay in the pack (shared) |
| Placed features in `kubejs/data` that name `fungidelight:*` | KubeJS data | stay, with the existing hard-dependency rule | See section 4 (Fungi dependency) |

## 4. What the members have in common (the shared method)

1. Own Forge 1.19.2 project under `development/`, root `settings.gradle`, plugin `ce.forge-mod` from `build-logic`, one
   mod id per project, `installToMods` with optional `replaces_jars`. (The native jars have no project yet; they would be
   imported as source or as a binary-repack step, section 6 step 2.)
2. Resources pulled from the upstream jar by a `port_resources.py` (Veggies, Vintage, Fungi) or read at runtime from a donor
   jar (Miner's).
3. Mojang-named mixins with a real refmap (Vintage, Fungi, Miner's); access transformers in Veggies, Vintage, Fungi, Miner's.
4. The same pack integration after the build: ids in `item_dict.js` / `block_dict.js`, tags through `global.tags`, recipes
   through `global.NCH`, wild placement in `wild_crops.json` and the nether/mushroom rule files, food types and generated
   cans, soft fluids for jars, compost rows, loot as `L.add`, trades, changelog bullets, `mod_sides.md` rows, tests. Under
   the third round the script part of this list moves into CD's script tree unchanged in form (section 10.3).
5. Advancements always kept in the jar and dropped at load; no cross-id conversions; every overlap kept and marked.

## 5. Cross-member interactions that a merge must not break

- Veggies `-02` carries 22 Vintage compat recipes; Vintage's pickling jar takes Veggies' fermented garlic honey.
- Fungi's Weak Stomach reacts to alcohol and potions (Brewin' and Vinery drinks are separate jars now, still installed).
- Miner's onion soup cup depends on `brewinandchewin:creamy_onion_soup`, a **non-member** (Brewin' is parked): keep it an
  optional lookup, never a hard dependency of the merged jar. Miner's cave carrot rule runs in the system Fungi built.
- Veggies and Vintage both edit `Chicken.FOOD_ITEMS`, `Pig.FOOD_ITEMS`, `Villager.WANTED_ITEMS` (combine with the current
  value, order-independent); Fungi adds pig and wolf goals.
- Miner's, Ocean's and Oceanic share the squid/tentacle drops (three raw tentacle items, three squid-drop loot modifiers;
  a shared tag is planned in ocean_delight.md); End's and Ender's share chorus, shulker, enderman and endermite drops.
- Third-party mods look these ids up: Create Central Kitchen (Miner's mixins), Delightful, Display Delight, Quark Delight,
  JEI/WTHIT plugins, KubeJS scripts and datapack JSON (hundreds of literal `namespace:id` strings). **Every registry
  namespace must stay as it is.**

## 6. Constraints on the merge and proposed process

### 6.1 Constraints

| Topic | Fact | Consequence |
|---|---|---|
| Namespaces | `veggiesdelight`, `vintagedelight`, `fungidelight`, `miners_delight`, `ends_delight`, `oceansdelight`, `oceanic_delight` are used by worlds, scripts and other mods | Each stays a mod id (or at least the registry namespace); no renames, no conversions. Only display names gain " (Creative Delight)" |
| Precedent | Vinery's jar already holds two `[[mods]]` entries (`vinery`, `foundation`) with own `@Mod` classes | One jar with several `[[mods]]` and `@Mod` classes is a pattern this pack already ships |
| Licences | Veggies, Fungi MIT; Vintage MIT 0.1.6 / GPL-3.0 KOTS; End's, Ocean's, Oceanic MIT; Miner's all rights reserved (code too); Ender's all rights reserved | Merged jar is GPLv3 with the MIT notices kept (per-source `NOTICE-upstream-MIT.txt`); it **may not contain** Miner's or Ender's upstream assets, data or code |
| Donors | `miners_upstream/` (one jar), outside `mods/`, sha1-pinned, own deploy category (Ender's would need one under route 2.1 b) | Unchanged by a merge; `UPSTREAM_DONORS` in `tools/build_deploy.py` keys donors to the port name `backportedMiners` |
| Build | `ce.forge-mod` assumes one `mod_id` per project; mixin configs, refmaps and ATs are per project | Flat shape: a multi-mod variant with merged refmaps and ATs. **Nested shape (14.1 to 14.3): not needed**, each member keeps its own; new work is the repack and bundle plugins |
| Scripts | CD's KubeJS scripts live in the jar (section 10.3) and are authored in the pack's `kubejs/data/creative_delight/kubejs/` and `kubejs/assets/creative_delight/kubejs/` (10.4) | The build copies those two trees into the jar at the same paths; a script error is a KubeJS error in `server.log` / `client.log`, never a crash |
| Mixins | `vintagedelight`, `fungidelight`, `miners_delight` each have a `*.mixins.json` (Veggies has one, 10 lines); Oceanic gains one if the fishing net mixin moves | One config per mod id keeps class paths and refmap names stable |
| Dependencies | All need `farmersdelight`; compile libs differ (JEI for Vintage, Miner's; Oceanic lists AppleSkin, JEED, JEI) | Union of compile deps; runtime requirements unchanged, soft stays soft |
| Deploy | `IN_TREE_MODS` lists the ports; `replaces_jars` differs per project; `mod_sides.md` has one row per jar | One project entry, one row, `replaces_jars` is the union of the members' globs, including `ends_delight-*`, `oceansdelight-*`, `oceanic_delight-*` once imported |
| Fungi dependency | Pack `kubejs/data` placed features need `fungidelight:*` | A merged jar must never ship without Fungi content |
| Not in mods/ | (2026-10-25) all four ports are in `mods/` now (Veggies `-02`, Vintage `-03`, Fungi `-01`, Miner's `-01`), plus the three third-party native jars | First release replaces up to seven live jars in one pack version; Miner's also replaces the third-party 1.1.2 jar |

### 6.2 Proposed merge process (draft; read with sections 10, 14 and 15: nested bundles, patcherMod dependency)

1. **Freeze.** Land or park each member's open work first (Fungi scratch test SQ-FGD, Miner's SQ-MDP/SQ-MD-01; the unwritten
   End's and Ocean's integration passes in the TODO rows). A merge on top of unfinished members makes regressions hard to
   attribute.
2. **New project `development/creativeDelight`** (jar and manifest mod id `creative_delight`; the member ids stay as
   `[[mods]]` entries, each with the " (Creative Delight)" display name). Source layout keeps each member's package intact
   (`net.mehdinoui.*`, `net.ribs.*`, `com.sammy.*`, the End's / Ocean's / Oceanic packages): `git mv` per port with history
   kept; the native jars (MIT) are brought in as decompiled-and-verified source or, simpler, as a repack of their classes
   and resources with a `NOTICE`. Decide between the two before step 2 (open question Q6).
3. **Build logic.** Extend `ce.forge-mod` (or add `ce.forge-multimod`) to take a list of mod ids; merge `mods.toml`,
   `accesstransformer.cfg`, mixin configs and refmaps; one `gradle.properties` with the union of `replaces_jars`.
4. **Resources.** Each member's `assets/` and `data/` already sit under its own namespace, so they merge by copy;
   `port_resources.py` per port moves to `creativeDelight/tools/`, with Miner's `UpstreamJars` and translators untouched.
   The CD script trees (10.4) are copied in at the same step.
5. **Move the Java chosen in section 3.2** (default: only the Oceanic mixin), update `creativeengineering.mixins.json`.
   **Move the scripts** chosen by rule 3.1.1 file by file from `kubejs/*_scripts/` into the CD trees (10.3), one member at a
   time, with a recipe/tag diff before and after each move (`/vtdtest`-style probes, the `exported/` dumps).
6. **Deploy.** `tools/build_deploy.py`: replace the port `IN_TREE_MODS` entries by one, repoint `UPSTREAM_DONORS`,
   `mod_sides.md` rows merged into one (both sides), changelog bullet for the jar swap and the display-name change.
7. **Verify.** Offline: each member's check scripts, the headless Rhino/javac harness, a data diff of registry names before
   and after (no id may disappear), and the same recipe ids and counts with the scripts in the jar as with them in the pack
   folder. Live (queue entries in [testing/scratch_queue.md](testing/scratch_queue.md), SQ-KS first): boot with the
   merged jar, load worlds that hold Veggies, Vintage and Fungi blocks, the in-game suites VD-, VTD-, FGD-, MND- unchanged,
   plus a check that the mod list, JEI and WTHIT show "(Creative Delight)".
8. **Cutover.** `installToMods` moves the old jars to `jar_archive/displaced/`; never while the game runs.

## 7. Parked: Brewin' and Chewin' and Let's Do Vinery

Removed from this plan on 2026-10-25 (owner). They stay separate jars (`backportedBrewin`, `backportedVinery`) with their own
projects, `IN_TREE_MODS` entries and writeups ([alcohol.md](alcohol.md) Parts A and B, tests `AL-` and `VB-`). Facts kept here so
a later plan can pick them up without re-reading everything:

- Vinery's second mod id `foundation`, and its licence split (code MIT, assets and data All Rights Reserved, runtime donors in
  `vinery_upstream/`), is the reason the merge process needs the donor path anyway.
- Brewin' is MIT with notice; `replaces_jars=BrewinAndChewin-1.19-*.jar`; Vinery replaces Vinery 1.3.10 and `doapi-*`.
- Brewin', Vinery and Vintage share the heat/cold and storage-temperature tags, and Vintage's pickling jar takes Brewin's
  pickled foods; both must still load beside Creative Delight unchanged.

### P.1 Brewin' and Chewin' (Backported Brewin)

- **Base:** full port of 4.5.0+1.21.1 over the pack's 1.19-2.0. Where the versions differ the port follows 4.5.0, except
  the owner's deviations: scrambling (chat, signs, name tags) client-side only, Dread Nog takes a poison potion, no jams,
  Steel Toe Stout gives Resistance II for 90 s, kombucha on green tea, honey and milk are the pack's fluids.
- **Keg is a fluid tank:** a batch is 1000 mB poured 250 mB per tankard (4 drinks, was 1), 2.0's fluid slot pair, pipe
  rule, leftover slot, any-face input, JEI "[+]" fill/empty, heat and cold from a block audit (furnaces, bonfires,
  running machines; snow, powder snow, IW ice).
- **Effects and drinks:** Tipsy rework, Intoxication / Raging, Tipsy and Intoxicated visuals; every alcoholic drink is half
  a rotten shank with Intoxication + Nausea ("Intoxicating Drink" in Quark's saturation word).
- **Pack side:** coasters as modelled items with Display Delight models and WTHIT, compost and BlockRunner rows, tooltips,
  all advancements dropped, recipe changes in the pack not the jar, village houses in vanilla villages (port `-04` per
  alcohol.md 13).
- **Build tooling:** `tools/check_data.py`, `gen_at.py`, `port_resources.py`; 139 Java files; MIT upstream, notice kept.
  `replaces_jars=BrewinAndChewin-1.19-*.jar`.

### P.2 Let's Do Vinery (Backported Vinery)

- **Base:** 1.5.5 (Fabric 1.21.1) ported to Forge 1.19.2 with Satisfy's Foundation 1.0.1 subset in the same jar (second
  mod id `foundation`); the 1.3.10 keepers stay. Cherry renamed `dark_cherry` (touches 65 items from other mods), 15
  effects replaced by 15 new ones, every wine has a new recipe and effect, about 25 decoration blocks removed upstream.
- **Mechanics:** Aging Barrel holds juice as a fluid level (6000 ticks per wine), two-stage Apple Press, wines age in
  storage blocks (faster in the dark) on the sun clock (months; sleep ages wine), cold/warm storage kinds, wine racks
  merged, sizes from the per-block limit, hopper jams on press and cask, village houses in vanilla villages.
- **Licence handling, the one that constrains the merge:** code MIT, assets and data **All Rights Reserved** (Satisfy
  refused redistribution). So the jar ships only our code and reads art and data from the **unmodified upstream jars in
  `vinery_upstream/`** at runtime (`UpstreamJars` sha1 pins, `UpstreamTranslator`, `checkNoUpstreamResources`). Deploy
  has a `vinery_upstream` category; the game stops with a message if the donors are missing.
- **Pack side:** Vinery banner recipe, WTHIT on grapevines and lattices, King Danis out and Praetorian in every rack,
  duplicate storage/furniture removed, wine box looks (Strad's shape), tooltips, drink values, effects.
- **Build tooling:** `port_java.py`, `port_fixups.py`, `gen_at.py`; 218 Java files; `replaces_jars` = Vinery 1.3.10 +
  `doapi-*`. No access transformer file, a `vinery_port/upstream_lang` resource.

## 8. QoL features: marked, then folded in (owner decisions, 2026-10-25)

Rule: any pack behavior that makes Delight-style cooking nicer (the Farmer's Delight output buffer for bottles is the model
case) is **marked `[QoL-n]`** in its own writeup and listed here. **Decision (second round): QoL-1 to QoL-6 are all part of
Creative Delight**, including the rules still in development (food types, the Quark saturation word). New QoL features are
added to this table in the session that builds them, with the status `accepted` unless the owner says otherwise.

**Built 2026-10-25: QoL-1, 2, 4, 5, 6 (section 22); QoL-3 stays pinned (food/types-and-tuning #13, framework #4).**

Each accepted feature gets a switch in Creative Delight's own config (default on) and, where it touches another mod's
classes, lives in a compartment that loads only when that mod is present (section 12).

| ID | Feature | Writeup | Today | Becomes |
|---|---|---|---|---|
| QoL-1 | Remainder buffer: bottles, bowls, buckets stay in the Farmer's Delight pot and kettle (take-only leftover slots, stall when full, automation) | [remainder_buffer.md](remainder_buffer.md) | patcherMod mixins on FD; the oven half is native pack code for the Cooking for Blockheads oven | CD compartment `farmersdelight`; the oven half is a compartment `cooking_for_blockheads` (loaded only when that mod is). Keg is parked with Brewin' |
| QoL-2 | Placeable-food tooltip lines | [placeable_food.md](placeable_food.md) | KubeJS + `/dumpplaceable` | CD client scripts (`assets/creative_delight/kubejs/`); lines for non-member mods are files with `// requires:` |
| QoL-3 | Food types and the Quark saturation word (rules in development) | [food_families.md](food_families.md) | KubeJS tables | CD scripts, tables keyed by the food dict (section 13); startup tables need the startup loader (10.5) |
| QoL-4 | Compost rows for member foods | compost framework (README index) | KubeJS | CD startup script calling the pack's compost helper (needs 10.5; until then the rows stay in `compost_rules.js`) |
| QoL-5 | Heatless pot routes and grid-meal moves (`_manual_only`) | member writeups | KubeJS cooking rules | CD server scripts through `global.NCH`, as today |
| QoL-6 | Crop-growth WTHIT line, JEI info pages | member writeups | patcherMod / KubeJS | WTHIT line: CD Java, soft dependency; JEI info pages: CD client scripts with `// requires: jei` |

### 8.1 Creative Delight config (Q21, proposal)

One common config file for the jar, read through patcherMod's `CEConfig`, all booleans unless noted, default on:

- `qol.<id>` one switch per accepted QoL feature (`remainderBuffer`, `placeableTooltips`, `foodTypes`, `compost`,
  `manualOnlyRoutes`, `wthitCropLine`, `jeiInfoPages`). A switch off skips that feature's script or compartment entirely.
- `members.<modid>` one switch per member whose recipes and loot rows CD adds; off leaves the member's own data untouched.
- `integrations.<modid>` one switch per optional integration of section 12 (only read when that mod is loaded).
- `swappers.*` later (Q19): `onWorldLoad`, `onPlayerDataLoad`, `onContainerOpen`, `onCommand`, `onceWhenUpdated`.
- Scripts read the values through a small `global.CD.config` object; startup scripts see the file as it was at boot, so a
  change needs a restart, server scripts re-read on `/reload`.

Consequence: QoL-1 (Java) moves **out of patcherMod** into a CD compartment; QoL-2 to QoL-5 move **out of the pack's
`kubejs/` folder** into CD's scripts (patcherMod and the pack keep no copy). patcherMod is then a dependency of CD for
KubeJS, `CEConfig`, the startup loader and (stage 2) the helper scripts (section 10), not for these features' code.

## 9. Non-food integrations (owner, 2026-10-25)

Creative Delight is **food only**. Non-food behavior (shield and damage tags, armor and tool effects) lives in patcherMod, or
in the mod that adds the behavior: Creative Equipment, if it becomes a mod (no work implied).

- Ender's Delight shield and damage tags (`ShieldConfig`, `damage_tags/burn_tick.json`, `damage_tags/fire.json`): patcherMod
  now, Creative Equipment later.
- **Decided:** the Oceanic fishing net mixin and its loot stay with CD (moved from patcherMod into the Oceanic member).
- **Decided:** the mooshroom family moves **into moreMooblooms**, behind a check that `fungidelight` is loaded. Facts for
  that move: moreMooblooms today has no dependency on patcherMod (its `mods.toml` lists forge, minecraft and soft Buzzier
  Bees, JEI, Blueprint, Alex's Mobs), and the family uses `SpawnTuning` and `CEConfig`, which stay in patcherMod. So
  moreMooblooms gains a hard patcherMod dependency for the spawning systems (decided, Q13). Files:
  `MooshroomFamily`, `MushroomTypeSetter`, `MushroomCowBreedingMixin`, `AnimalCanMateMooshroomMixin`,
  `MushroomCowWalkTargetMixin`. The mushroom spawn system data (`mushroom_spawns.json`) stays in patcherMod; Fungi's cow
  rows move with the family.

## 10. Runtime architecture: KubeJS scripts in the jar, calling the pack helpers (third round, 2026-10-25)

### 10.1 What changed

- Creative Delight **depends on patcherMod** (mod id `creativeengineering`, mandatory), and patcherMod **requires KubeJS**
  (`mods.toml`, `[1902.6.0,)`, loads after KubeJS; it already uses KubeJS classes in `KubeJSVirtualDataPackListMixin` and
  `KubeJSCanAction`). So KubeJS is always present where CD loads. CD's `mods.toml` does not list KubeJS separately, and its
  patcherMod range must not allow a patcherMod that requires an older KubeJS than CD's scripts need.
- CD is therefore **not a standalone Delight bundle**: it loads only in this pack (Q23, Q14).
- Owner goal: CD's recipes and changes are written by calling the same helpers the pack uses, not a second system.

### 10.2 Feasibility: can CD call the helpers? Yes, with one gap

Where the helpers are today: `global.NCH` ([neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js), 2,248
lines), `global.tags` ([tag_helper.js](../kubejs/startup_scripts/func/tag_helper.js)), the compost helper
(`startup_scripts/func/compost.js`), `global.LOOT`, and the dicts (`global.item`, `global.fluid`, ...) are **KubeJS
scripts in the pack's `kubejs/` folder, not Java in patcherMod**. patcherMod has no Java recipe API. "patcherMod's
helpers" in this plan means these scripts, which travel with patcherMod as CD's dependency (10.5 stage 2 makes that
literal).

How KubeJS 1902.6.2-build.73 finds scripts (read from `ScriptManager` bytecode, 2026-10-25; live check SQ-KS):

| Script type | Read from | From a mod jar? |
|---|---|---|
| Server | `kubejs/server_scripts/`, then every `data/<ns>/kubejs/**/*.js` in the server resources (`loadFromResources`: one script pack per namespace) | **Yes, native.** Also from world datapacks and `kubejs/data/` |
| Client | `kubejs/client_scripts/`, then every `assets/<ns>/kubejs/**/*.js` in the client resources | **Yes, native.** Also from resource packs and `kubejs/assets/` |
| Startup | `kubejs/startup_scripts/` only (`KubeJS` calls `reload(null)`, so `loadFromResources` is skipped) | **No.** Needs a patcherMod loader (10.5) |

Facts that make the server and client halves work:
- **`global` is one object for every script type.** The pack's server scripts already call `global.NCH`, defined in a
  startup script. A CD server script does the same: `ServerEvents.recipes(e => { global.NCH.<fn>(e, ...) })`.
- **Order:** script packs are kept in a `LinkedHashMap`, the pack folder first, the jar packs after it; files inside a
  pack are sorted by their `// priority:` header. So pack server-script globals (for example `global.LOOT`) exist when
  CD's scripts load, and CD's event callbacks run in the same passes as the pack's.
- **`// requires: <modid>`** at the top of a file makes KubeJS skip it when that mod is absent
  (`ScriptFileInfo.skipLoading`). That is the compartment mechanism for scripts (section 12), with no code of ours.
- `/reload` reloads server scripts from jars too; client scripts reload with the resource reload (F3+T).

Not planned: **calling the helpers from Java.** NCH is a JavaScript object in the Rhino scope; Java could reach it only
through KubeJS's script context, which is fragile. CD's Java (mixins, block entities, QoL-1, WTHIT) does not make
recipes; it reads switches through `CEConfig` as the ports do.

### 10.3 The approach (replaces the second round's options A, B and C)

| | Approach | Status |
|---|---|---|
| A | **CD ships KubeJS scripts that call the pack helpers** | **Chosen.** Server and client native (10.2); startup through the loader (10.5) |
| B | patcherMod Java rules engine, CD ships `ce_rules` JSON | **Dropped.** It would rewrite NCH and `tags` in Java and give the pack two schemas |
| C | Build-time bake of KubeJS output into plain data | Ruled out (owner, second round) |

What goes where in the CD jar:

| Kind | Path in the jar | Notes |
|---|---|---|
| A member's own recipes, loot, worldgen, tags under its namespace | `data/<member ns>/...` | Plain data, as the ports already ship it; unchanged |
| Pack integration for members: cooking rules, `_manual_only` moves, heatless routes, cans, tags, loot `L.add` rows, trades, item merges | `data/creative_delight/kubejs/<member>/*.js` (server) | Calls `global.NCH`, `global.tags`, `global.LOOT`; one folder per member so a member's rows move as a unit |
| Rows for non-member mods (Create, IE, Display Delight, Rustic Delight, ...) | same tree, files headed `// requires: <mod>` | Skipped when the mod is absent (section 12) |
| Tooltips, JEI info pages, name changes | `assets/creative_delight/kubejs/<member>/*.js` (client) | Same helpers the pack's client scripts use |
| Startup parts: dict keys of member items, food dict masks, compost rows, food types | `kubejs_startup/<member>/*.js` at the jar root (not a resource path, so no pack sees it) | Needs the loader (10.5); until it exists these stay in the pack's `startup_scripts` |

- **Recipe ids.** NCH names recipes `${global.NCH.modId}:<type>/<suffix>` with `modId` `creativeengineering`. CD
  recipes would then sit in the pack namespace. Extend the helper (CLAUDE.md: extend, do not bypass) with a namespace
  option, for example a per-call `ns` or a scoped `NCH.withNamespace('creative_delight', () => ...)`, so CD recipe ids are
  `creative_delight:<type>/<suffix>` (decided, Q24). Existing ids that players or JEI bookmarks know keep their old names until the
  move; a move changes the id, not the recipe.
- **Ids in scripts.** CD scripts use dict keys (`i.x`, `f.x`), never literal `ns:id`, the same rule as the pack.
- **Config values.** `NCH.cfg()` already reads `CEConfig` from patcherMod; CD scripts get pack-wide values the same way.
- **Generators.** `tools/*.py` that write rows for members get an output path in the CD tree (`tools/generators.json`
  entry per generator); `/cegen` unchanged.
- **Two front ends become one.** The second round's stage 2 (NCH writing a `ce_rules` schema) is gone: NCH is the only
  front end.

### 10.4 Development loop: one path in the pack folder and in the jar

- **Authoring location:** CD server scripts live in `kubejs/data/creative_delight/kubejs/`, client scripts in
  `kubejs/assets/creative_delight/kubejs/`. KubeJS serves `kubejs/data` and `kubejs/assets` as packs, so the scripts load
  from there with `/reload` or F3+T, without a jar rebuild. The CD build copies both trees into the jar at the **same
  resource paths**.
- **No double load:** the resource manager returns one file per path (the highest pack wins), so a script present in both
  the folder and the jar runs once. Which copy wins (expected: the `kubejs/` folder pack, above mod resources) is SQ-KS-03.
- **Deploy:** ship the jar copy only, and exclude `kubejs/data/creative_delight/kubejs/` and
  `kubejs/assets/creative_delight/kubejs/` from the pack's deploy, so a CD update is a jar update (decided, Q25).
- **Startup scripts** need a restart anyway; they are authored in the CD project (`src/main/resources/kubejs_startup/`)
  and loaded from the jar in dev too (`installToMods`, restart).
- **Offline tests:** the headless Rhino harness (memory `kubejs-headless-rhino-harness`) loads NCH, the dicts and a CD
  script file, as for pack scripts today.

### 10.5 What patcherMod must provide (replaces the engine work list)

1. **Startup script loader** (the one gap in 10.2). A mixin on `ScriptManager.loadFromDirectory` for
   `ScriptType.STARTUP`: at RETURN, for every loaded mod file with a `kubejs_startup/` folder, add a `ScriptPack` named
   after the mod id and load its files through a `ScriptSource` that reads from the mod file (an `@Invoker` for the
   private `loadFile`). Pack folder first, jars after, as for server scripts. Same file-header rules (`priority`,
   `requires`). patcherMod already mixes into KubeJS, so this is the existing pattern.
2. **Helper extensions** that CD needs: the NCH namespace option (Q24); any helper that today exists only inline in one
   pack script (move it into `func/` first).
3. **The helpers live in patcherMod's jar, written once (decided, Q26).** `neoCraftHelper.js`, `tag_helper.js`,
   `compost.js` and the other shared `func/` helpers move to `development/patcherMod/src/main/resources/kubejs_startup/`
   as the same JavaScript (no translation to Java; KubeJS runs them from the jar). The pack folder keeps no copy. The
   loader adds patcherMod's pack at HEAD (before the pack folder), every other mod's pack after it. Consequences:
   - NCH must read `global.item` / `global.fluid` lazily (at call time), since the dicts load after it; the dicts stay in the pack folder (decided, Q27).
   - patcherMod's Java cannot call the helpers; only scripts can (patcherMod's own, CD's, the pack's).
   - A helper edit needs a patcherMod rebuild and restart; a dev switch letting a pack-folder copy replace the jar
     copy is optional.
   - The helpers publish `global.NCH.apiVersion`; a caller checks it (see 10.6).
4. A headless check of the loader (javac harness) and SQ-KS live.

### 10.6 How another mod calls the helpers

Any mod (CD, or another in-tree mod) needs no Java for this: a `mods.toml` dependency and script files.

```
<mod>.jar
  META-INF/mods.toml                        mandatory dependency on creativeengineering (patcherMod)
  kubejs_startup/<mod>/*.js                 startup: dict keys, compost rows, food types (loaded after patcherMod's helpers)
  data/<ns>/kubejs/<mod>/*.js               server: recipes, tags, loot
  assets/<ns>/kubejs/<mod>/*.js             client: tooltips, JEI info, names
```

```js
// data/creative_delight/kubejs/veggies/cooking.js
// requires: veggiesdelight
const i = global.item

ServerEvents.recipes(e => {
  global.NCH.shapeless(e, i.veggie_dish, [i.broccoli, i.bowl], 'veggies/veggie_dish')
})
```

The function names above are placeholders for the real NCH signatures. Rules: call helpers inside event callbacks (all
startup scripts have run by then); use dict keys, not literal ids; a startup file that needs the helpers at load time
relies on patcherMod's pack loading first; a file checks `global.NCH.apiVersion` when it uses a helper added later.

## 11. Swappers: retained replacement lists (owner, 2026-10-25)

Unlike patcherMod, Creative Delight runs **swappers** on removed or replaced items. No notes yet on when they run.

- **Replacement list.** Every merge or swap records, for the surviving item, the old items it replaced as
  `namespace:path` strings (namespace kept, since the old mod may be gone): `new_id -> [old_id, ...]`. The list is retained
  in the jar as data and never pruned, so a world that still holds an old version can be fixed whenever the list is read.
- **Format (proposal):** `data/creative_delight/swaps/<name>.json` with `{ "replaces": { "creative_delight:thing": ["oldmod:thing", ...] } }`
  (one list per survivor), plus optional `block` and `entity` sections later.
- **Swapper (third round):** the lists stay JSON data; the recipe, loot and tag half is a CD server script that reads them
  and feeds the existing item-merge path (`global.itemMerges`, `server_scripts/other/item_merges.js`), so the pack's merge
  rows for CD members become list entries. The world half (inventories, containers, dropped items) needs events KubeJS
  covers only partly (player login and inventory yes, containers in unloaded chunks no); if it is needed it is Java in
  patcherMod, reading the same lists. Who reads the JSON from scripts: a small reader in patcherMod exposed to scripts,
  or the lists written as a CD startup script instead of JSON (decide with Q19).
- **Not decided here:** triggers, timing, which containers, block/entity swaps (owner: no notes for now).
- Tie-in: ids that currently appear as `removed_items.js` entries for a member also get a swap line, if a survivor exists.

## 12. Optional integrations and compartments

Recipes and tweaks that depend on jars not in CD (Create, IE automation, Display Delight, Cooking for Blockheads, Rustic
Delight, ...) are **applied if those mods are loaded and skipped if not**, never a crash and never a hard dependency.

- Static data: Forge `forge:mod_loaded` conditions or non-required tag entries.
- Scripts: a `// requires: <modid>` header per file (KubeJS skips the file when the mod is absent, 10.2); inside a file
  that must stay whole, `Platform.isLoaded('<modid>')`. One file per optional mod and member keeps it readable.
- Java: a compartment is a class set registered only after a loaded-check (the existing `@Pseudo` / mixin-plugin pattern in
  patcherMod, for example `FruitsDelightLeafCheckMixin`), never a static reference on the main path.
- Naming: a mod that is **shipped as a submod or backport** carries the " (Creative Delight)" display suffix; a compartment
  that only changes someone else's mod carries no name change and is simply inactive when that mod is absent.
- Pack-wide effects (config-driven values from `NCH.cfg`) stay in patcherMod's config and are read through it.

**Built 2026-10-25 (first Java compartment):** `creative_delight.mixins.json` with `compat/CDMixinPlugin`. A mixin in
`com.SaintAlfonzo.creativedelight.mixin.<modid>.` applies only when `<modid>` is loaded (`LoadingModList`), and only at
the version in `FOR_VERSION` when the compartment fixes one jar's bugs. First user: `mixin/fruitsdelight/` (Fruits Delight
0.5.11 fixes, [fruits_delight.md](fruits_delight.md) 5). Compile-only jars for compartments go in CD's own `libs/`
(git-ignored). A new compartment: a subpackage under `mixin/`, its entries in the config, a `FOR_VERSION` line if needed.

## 13. The food dict (owner pattern: category, form, state)

### 13.1 Mask rule (unchanged)

`global.food` (`f`) is a mask over the general dict `global.item` (`i`): the right-hand side is always `i.xxx` (never a
literal `ns:id`), so items are defined once in `item_dict.js` and `f` only gives a development name. A helper throws when
the general key is undefined; postInit validation checks every mask value equals a general value; a lint in `tools/`
rejects a literal id in a mask file. Values are the same strings, so `NCH`, `tags` and the rule generator read `f.x` like
`i.x`. A reverse index (`itemId -> food key`) is generated. Examples:

```
f.fruit_apple                = i.apple;
f.veggie_slice_potato        = i.potato_slices;
f.veggie_slice_cooked_potato = i.cooked_potato_slices;
```

### 13.2 Key grammar: `<category>[_<form>][_<state>]_<name>`

- **Categories (owner list):** `fruit`, `veggie`, `meat`, `fish`, `cake`, `pie`, `cookie`, `sandwich`, `salad`, `soup`,
  `ice_cream`, `candy`, "etc.". Sandwich has sub-kinds `square` and `burger`: `sandwich_square_ham`, `sandwich_burger_beef`.
  (The owner wrote "veggies"; the existing example key is `veggie_`, so the category is `veggie`, singular, like `fruit`.)
- **Additional categories (Q15: accepted as the build starting point, final list settled while building the food dict, TODO `creativedelight/framework #4`):** `grain` (bread, pasta, rice, oats), `dairy`, `egg`, `mushroom`, `nut`, `dish`
  (plated meals that fit nothing above), `drink`, `sauce`, `spice`, `dough`.
- **Form** (`slice`, `diced`, `ground`, `juice`, `jam`, `dough`, ...) and **state** (`raw` implicit, `cooked`, `dried`,
  `pickled`, `fermented`, `smoked`) come from fixed lists in one file; the lint resolves a key by longest-prefix match
  (needed because `ice_cream` and `veggie_slice_cooked_potato` contain underscores).
- Order is form, then state, then name, as in `veggie_slice_cooked_potato`.

### 13.3 Creative Delight's subdicts

| Dict | Alias | Holds | Examples |
|---|---|---|---|
| `food` | `f` | everything edible and drinkable | `f.fruit_apple`, `f.veggie_slice_cooked_potato` |
| `tool` | `t` | bowls, cookware, utensils, stations | `t.bowl_bamboo`, `t.pot_copper` |
| `misc` | `m` | anything else (seeds, crates, decor) | `m.seed_oat`, `m.crate_morel` |

All three are masks over the general dict. Q9 is decided: one pack-wide `f`, CD reads a member view. Third round: CD scripts read `global.food` and the general dict
directly (shared `global`), so no generator is needed between the dict and CD. Member item keys stay in `item_dict.js`
(define once) until the startup loader (10.5) exists; then a member's keys can move into its `kubejs_startup/` folder,
loaded after the pack's dicts, still defined once.

### 13.4 Work plan

1. **Done 2026-10-25 (food half):** `food_dict.js`, the mask check, the lint, the grammar file (13.5). `tool_dict.js` and
   `misc_dict.js` not started (same helper: `global.subdict.check(t, 'tool')`, no grammar argument until their key rules exist).
2. Pilot on Veggies Delight (about 89 items); recipes through `f`; diff outputs against today's. Not started: needs the
   move-diff harness (framework #6) for the "identical recipes" proof.
3. **Done:** coverage report (`tools/food_dict.py`), seeded from food families and the `/dumpfood` dump; 0 foods without a key.
4. **Done:** registered as `food_dict` in `tools/generators.json` (groups `reports`, `food`; dump `dumpfood`).

### 13.5 As built (2026-10-25)

| File | What |
|---|---|
| `kubejs/startup_scripts/_lib/food_grammar.js` (priority 990) | `global.foodGrammar` (categories, forms, states), `global.subdict.parse(key, grammar)` and `global.subdict.check(dict, label, grammar)` |
| `kubejs/startup_scripts/_lib/food_dict.js` (priority 980) | `global.food`: 1542 keys, one per food, grouped by category; ends by building `global.foodKey` (item id -> key) through the check |
| `tools/food_dict.py` | Lint (`--check`), reverse index `kubejs/generation_scripts/food/food_dict_index.json`, coverage with a proposed line per missing food (`--append` adds them at the end of the file), `--seed` (done once; refuses to overwrite) |
| `tools/test_food_dict.sh`, `tools/food_dict_test/RhinoRun.java` | Offline: lint, then the three files loaded in the pack's Rhino fork; a second run with three bad lines must throw (CDT-08) |

- **Placement:** pack folder, not the CD jar: `f` is pack-wide (Q9) and covers non-member mods (Farmer's Delight, vanilla,
  Neapolitan). CD's jar scripts load after the pack's startup folder, so they can read it; an edit needs a restart, no jar build.
- **Load check:** throws (KubeJS logs it as a startup script error) on a missing general key (`i.xxx` undefined), a value
  that is not a `global.item` value, an item named twice, or a key outside the grammar. The dict is built before the
  check runs, so a throw does not empty it.
- **Alias:** `f` was already the fluid dict alias in the pack's startup and server folders (`fluid_dict.js` and 30 server
  scripts use `var f = global.fluid`). The food dict body is wrapped in a function so its `f` stays local. Pack-folder
  scripts alias the food dict `fd`; CD's own scripts (their own scope) can use `f`.
- **What is a food:** every edible item and food block in the dump, every drinkable item (34 drinks have no food
  properties: juices, teas, milk), plus the kitchen ingredients the audit found (`INGREDIENTS` in the tool, about 100:
  grains and flours, doughs and bases, eggs, cheese wheels, milk, salt, sugar, cocoa, vanilla, fats, tea leaves, coffee
  beans, whole produce cut before eating, feasts missing from the block dump). Removed and merged items get no key.
  Potions, potion bundles, Botania brews and Fantasy Furniture's decoration platters are not food.
- **Audit (2026-10-25):** sources were the food recipes' non-edible inputs (370 items), each food mod's item list (crates,
  seeds, tools and decor dropped by name) and a name sweep of the general dict. 5 foods had no general key and were added
  to `item_dict.js` (End's dried chorus flower, ender pearl grain and non-hatchable dragon egg; Nether's raw stuffed
  hoglin; Splash Milk's milk bottle).
- **Naming:** category and form from `tools/food_families.py` `classify()` (table `FAMILY_KEY` in the tool); the name is the id path less
  the words the key already says. A cooked food named for its method (boiled corn, glazed porkchop) keeps the word and
  gets no `cooked` state. When two items want one key, the first namespace (vanilla, Farmer's Delight, the pack) keeps it
  and the other gets `_<namespace>` (`soup_tomato`, `salad_potato_rusticdelight`).

Final lists (the open part of Q15, settled while building; change them in `food_grammar.js`):

- **Categories (31):** owner list plus Q15 extras, and `ingredient` (other kitchen ingredients: mash, gelatin, vinegar),
  `pastry` (waffles, rolls, pancakes), `dessert` (jellos, puddings, popsicles), `snack`, `feast` (whole feast blocks and
  `feast_serving_` portions), `alcohol`, `golden`, `can`, `hazard` (rotten, poisonous). Bread is under `grain` (Q15 list:
  `grain_bread`, `grain_slice_toast`), raw pasta and noodles too; meals, skewers, rolls, stuffed vegetables and pizzas are `dish`.
- **Forms (21):** `slice diced ground cut chop fillet strip leaf patty wedge wheel flour juice jam` plus the sandwich
  sub-kinds `square burger roll wrap toast`, `serving` (feast portion) and `enchanted` (golden). `dough` is a category, not a form.
- **States (5):** `cooked dried smoked pickled fermented`, as planned.

Decisions for the owner (defaults taken; renaming is a find-and-replace while nothing reads `f`):

- Juices are `drink_juice_<name>`, so all drinks sit together for drink rules. Jams are `fruit_jam_<name>`. The other
  reading of the owner's form list would be `fruit_juice_apple`.
- `f` names items only. Tag values (`i.milk = '#forge:milk'`, `i.flour`) stay in `i` / `global.tags`.
- Exotic mob ingredients from End's and Hostile Delight stay under their meat or fish family names (`meat_ghasttentacle`);
  fused ids keep their spelling.

## 14. Native jars (Q6): detailed options

Members that ship as upstream Forge 1.19.2 jars: End's Delight 2.1 (MIT, 64 items), Ocean's Delight 1.0.2 (MIT), Oceanic
Delight 1.0.1 (MIT, MCreator build). Ender's Delight (all rights reserved) is outside this question (section 15).

| | Approach | What is in our tree | Edits to upstream behavior | Licence work | Risk |
|---|---|---|---|---|---|
| 1 | **Decompile to source** | Java we own, Mojmap names | Free | Source is a derivative of MIT code: keep notice, our changes GPLv3 | Decompiler errors; MCreator output is generated, ugly code and a pile of `Procedure` classes; large diff; must recompile and re-test every item, block and loot table |
| 2 | **Binary repack** (copy classes and resources unchanged into the bundle at build time from a sha1-pinned upstream jar) | Only the pin, build script and our overlay | Only by overlay (extra mixins, replaced resource files, new classes) | Keep upstream MIT notice per source; our overlay GPLv3 | Cannot fix an upstream bug in a class except by a mixin; refmaps and `mods.toml` need merging |
| 3 | **Hybrid (recommended)** | Repack by default; decompile only the specific class we must change, and keep the rest binary | Per class | As above | Mixed provenance, but each class has one clear origin |
| 4 | **Donor jar** (read at runtime from an unmodified upstream jar in a donor folder) | Only our code | Via translators | Needs no redistribution rights | Needed only for all-rights-reserved sources; unnecessary for the three MIT jars |

Per-jar notes:
- **End's Delight:** assets, data and classes keep their package and namespace; four knives and the End loot modifiers come with
  it. Repack plus overlay (tags and the Ender's overlap decisions are data, not class edits).
- **Ocean's Delight:** repack; the only class-level need is none today. Tentacle tags are data.
- **Oceanic Delight (MCreator):** `net.mcreator.oceanicdelight.*`. The fishing net lives in `FishingNetExtractingProcedure`;
  the existing patcherMod mixin becomes a normal (non-`@Pseudo`) mixin in the Oceanic bundle, a good test of the overlay
  approach. Check which of its listed dependencies (AppleSkin, JEED, JEI, "delight") are mandatory before merging
  `mods.toml`.

Bundling shape (decided, Q16: nested, see 14.1 to 14.3; Q6 hybrid). "Built in parts by submod" and the `xyzDelightBackport` bundles suggest:

- **Flat:** one jar, several `[[mods]]` entries (the Vinery precedent), as in the earlier draft. Simple loader behavior;
  one mixin config and refmap per mod id; one build with merged resources.
- **Nested (jar-in-jar):** each submod builds as its own jar (for example `VeggiesDelightBackport-<upstream>-ce-19.2-<NN>.jar`),
  and the Creative Delight jar embeds them with Forge's Jar-in-Jar. Each bundle can be built and tested on its own, keeps its
  mod id and namespace, and can be left out. Check before committing to it: `build-logic` has **no `jarJar` configuration
  today** (grep found none), and the loader must be tested to start nested jars as separate mods; donors (Vinery, Ender's)
  cannot be nested, they stay manual copies.
- Recommendation: **nested**, because it matches "in parts by submod", keeps the unit of testing, and leaves room to
  swap one bundle without re-releasing the rest. Fall back to flat if Jar-in-Jar breaks mixin/refmap loading.
  (Checked 2026-10-25, 14.1: it does not on Forge 43.3.9, so no fallback is expected.)

**Q6 decided 2026-10-25 (owner): hybrid.** Repack by default, decompile only a class that must change. Today no class needs
decompiling (End's and Ocean's: none; Oceanic's fishing net is a mixin, an overlay), so all three start as pure repacks.

### 14.1 Nested shape: findings (read from the loader and this pack's log, 2026-10-25)

1. **Nested mod jars load as full mods.** Forge 43.3.9 `JarInJarDependencyLocator` reads `META-INF/jarjar/metadata.json` and
   builds each listed jar through the same `createMod` path as a `mods/` jar. `latest.log`: "Found 41 dependencies", for
   example `flywheel-forge-1.19.2-0.6.11-22.jar of type MOD` from Create.
2. **Mixins and refmaps in nested jars work.** FML has no mixin code; Mixin 0.8.5 reads `MixinConfigs` from every resource
   FML hands ModLauncher (`MixinLaunchPlugin.addResources`), nested ones included. Proof in this pack: Flywheel, nested in
   Create, has `MixinConfigs: flywheel.mixins.json,flywheel.sodium.mixins.json` and its own refmap.
3. **Access transformers in nested jars work** (`ModFile.getAccessTransformer` per mod file). Proof: `l2library` (in Fruits
   Delight), `apexcore` (in Fantasy Furniture) and `TRender` ship a nested `META-INF/accesstransformer.cfg`.
4. **Duplicate mod ids are resolved silently, newest version wins** (`UniqueModListBuilder`: "selecting most recent based on
   version data"). Version trap, tested with maven-artifact 3.6.3 (the loader's comparator): `1.0.2-ce-19.2-01` is **older**
   than Ocean's upstream `1.0.2-1.19.2`, so a stale upstream jar left in `mods/` would beat the repack with no error. Rule:
   a repack's version is the upstream version string verbatim plus `-ce-19.2-NN`: `1.19.2-2.1-ce-19.2-01` (End's),
   `1.0.2-1.19.2-ce-19.2-01` (Ocean's), `1.0.1-ce-19.2-01` (Oceanic); all three tested newer. The ports' current versions
   already beat their upstreams (Miner's `1.4.5-ce-19.2-01` against `1.1.2` tested).
   **Live correction (SQ-CDJ-02, 2026-10-25): the version rule does not protect against a stale top-level jar.** With
   `oceansdelight-1.0.2-1.19.2.jar` in `mods/` beside CD, Forge's `JarSelector` logged "Attempted to select a dependency
   jar for JarJar which was passed in as source: oceansdelight. Using Mod File: mods/oceansdelight-1.0.2-1.19.2.jar" and
   loaded upstream (`1.0.2-1.19.2-ce-19.2-01 -> 1.0.2-1.19.2`): a jar in `mods/` always beats a nested one with the same
   id, whatever the versions. The version rule only orders nested copies against each other. The real guard is removing
   the upstream jars: `build_deploy.py`'s duplicate-id check (reads the nested ids, checked offline) stops a release
   with both in `mods/`, and the update payload's removals are by file name, so the upstream jars it no longer ships are
   deleted from players' `mods/`. A player who drops an upstream jar back in by hand gets upstream; since framework #10 (section 28.3) CD logs an
   error naming the jar.
5. **The native jars:** no mixins, no refmaps; Oceanic has a 2-line AT (`ScatteredOreFeature` constructor, `TreeFeature`
   place). **None ships a LICENSE or NOTICE file**, so each MIT notice must be taken from the upstream source (End's:
   FoggyHillside; Ocean's: github.com/Scouter456/Oceans_Delight; Oceanic: DariusZeBaguette, MCreator), with the source URL
   in the notice file.
6. **mods.toml facts for the rewrite:** Ocean's uses `version="${file.jarVersion}"` (from the manifest) and has a mis-keyed
   `[[dependencies.farmersdelight]]` block (range `[1.19-1.2.0)`, a no-op today). Oceanic: mandatory only minecraft
   `[1.19.2]` and farmersdelight; appleskin, jeed, `delight`, jei optional (answers the per-jar check above). End's:
   forge, minecraft, farmersdelight `[1.19-1.2.4,)` AFTER. No `updateJSONURL` in any of them.
7. **Miner's donor is unaffected:** `UpstreamJars` resolves `FMLPaths.GAMEDIR/miners_upstream`, not its own jar's path.
8. **Split packages are fatal** (module layer): two jars may not hold the same package. A decompiled replacement class must
   go **into that member's own nested jar**, never into the CD outer jar or patcherMod. CD's own code keeps its own package.
9. **ForgeGradle 6.0.54 has a `jarJar` task, not used here:** it nests a project's `jar` artifact, which this build reobfs
   in place in `reobfJar` (a finalizer, so a consumer may pick up the Mojmap jar), and the repacks are not Maven artifacts.
   A small own bundle task is simpler and deterministic.

### 14.2 The jar

```
creative_delight-<v>.jar                  mod id creative_delight: CD's code, scripts (10.3), swaps (11), QoL compartments (8)
  META-INF/mods.toml                      mandatory creativeengineering, farmersdelight
  META-INF/jarjar/metadata.json           one entry per member: group, artifact, version range [v], path, isObfuscated false
  META-INF/jarjar/backportedVeggies-1.9.2-ce-19.2-NN.jar    in-tree ports, built as today
  META-INF/jarjar/backportedVintage-... backportedFungi-... backportedMiners-...
  META-INF/jarjar/repackedEnds-1.19.2-2.1-ce-19.2-01.jar     repacks (14.3)
  META-INF/jarjar/repackedOceans-1.0.2-1.19.2-ce-19.2-01.jar
  META-INF/jarjar/repackedOceanic-1.0.1-ce-19.2-01.jar
```

Each member keeps its own `mods.toml`, AT, mixin config and refmap, so the flat shape's merge work (6.1 Build row, 6.2
step 3) is not needed. A port project stays a separate Gradle project (no `git mv` into one source tree).

### 14.3 What to build

| # | Piece | Where | Content |
|---|---|---|---|
| B1 | `ce.forge-repack` convention plugin | `build-logic` | For a native member. Reads `upstream_jar`, `upstream_sha1`, `upstream_version` from the member's `gradle.properties` and refuses a jar with another sha1. Upstream jar on `compileOnly` (as patcherMod's `libs/`) for overlay code. `repackJar`: upstream entries minus `META-INF/mods.toml`, `MANIFEST.MF` and the classes listed in `replaced` (with their `$` inner classes), plus the reobfed overlay jar, a `mods.toml` from the project's template (display name suffix, our version, dependencies cleaned), a manifest (`Implementation-Version`, `MixinConfigs` from the overlay) and the NOTICE. **Fails on any path collision not listed in `replaced`** |
| B2 | Repack projects | `development/creativeDelight/members/{ends,oceans,oceanic}`, in root `settings.gradle` | `gradle.properties` (version rule 14.1.4), `mods.toml` template, `NOTICE-upstream-MIT.txt`, `src/` for overlay only. Upstream jars in `development/creativeDelight/upstream/` (git-ignored, sha1-pinned; build-time only, so no deploy category, unlike donors) |
| B3 | Decompile per class | guard in B1 now; `tools/decompile_class.py` later | The guard: `replaced` lists each class with the sha1 of the upstream bytes it replaces, so an upstream bump fails the build. The tool (decompile the deobfed upstream with the decompiler FG already caches, write Mojmap source into the member's `src/`, add the `replaced` row) waits until a class needs it; none does today |
| B4 | `ce.forge-bundle` plugin | `build-logic` | `bundle_members` (project paths). `bundleJar`: the reobfed own jar plus each member's shipped jar into `META-INF/jarjar/`, `metadata.json` generated from each member's `mod_group_id`, `jar_name`, `mod_version`. Depends on the members' `reobfJar` / `repackJar`. Reproducible like every jar |
| B5 | `ce.forge-mod` changes | `build-logic` | A "shipped jar" indirection (default `jar`, `bundleJar` for CD) used by `archiveJar` and `installToMods`; `bundled_in=creativeDelight` makes a member's `installToMods` refuse and name CD's task. CD's `replaces_jars` is the union: the four `backported*-*.jar`, `ends_delight-*`, `oceansdelight-*`, `oceanic_delight-*`, `miners_delight-*` |
| B6 | `development/creativeDelight` | new project | `ce.forge-mod-refmap` + `ce.forge-bundle`; `@Mod` class, the script trees (10.4), swaps, QoL code. Member ports: `mod_name` gets " (Creative Delight)" instead of " (Backport)", version bump |
| B7 | Oceanic overlay | `members/oceanic/src` | `OceanicDelightFishingNetMixin` moves from patcherMod as a normal mixin (no `@Pseudo`; target on the compile classpath), own config `oceanic_delight.ce.mixins.json`; patcherMod's copy and its mixin-json entry removed in the same release. `loot/BiomeTagCondition` stays in patcherMod (CD depends on it) |
| B8 | `tools/build_deploy.py` | | `IN_TREE_MODS`: the member rows become one `('creativeDelight', 'creative_delight', True)` (note: `backportedMiners` is missing from the list today). Mod id reading looks into `metadata.json` too, so the duplicate-id check catches a stale upstream jar beside CD (the trap in 14.1.4) and the release diff shows member versions, not seven removed mods. Freshness check also covers the members' `src/` |
| B9 | `mod_sides.md` | | One `creative_delight` row (both) naming the members; the seven member rows removed; nested ids take the outer jar's side |
| B10 | Checks | `creativeDelight/tools/` + scratch queue | Offline: every `metadata.json` path exists, each nested jar's `mods.toml` has the expected id and version, each repack version newer than its upstream. Live: SQ-CDJ ([testing/scratch_queue.md](testing/scratch_queue.md)) |

Not planned: dev `runClient` for CD (would need every member as a `mods {}` source set and the repacks as runtime jars); the
pack verifies through `installToMods` and the scratch server.

Order: B1 + one repack (Ocean's, no overlay) + B4/B5 with that one member is the spike (SQ-CDJ-01 to 03); then Oceanic with
B7 (SQ-CDJ-04), End's, then the ports.

Decided (Q28 to Q30, section 17): repack jar names `repacked<Name>`; member projects under `development/creativeDelight/members/`;
the Oceanic overlay lives in its repack (keeps the member whole).

Licensing and naming (decided): all code the pack distributes is GPLv3; per-source MIT notices stay; display names of bundles
get " (Creative Delight)"; namespaces unchanged; version scheme unchanged (`<upstream>-ce-19.2-<NN>` for bundles; the
the Creative Delight jar itself follows the pack's own scheme: starts at 1.0.0, then 1.0.01, 1.0.02 (decided, Q17)).

## 15. Donors and Ender's Delight (owner, 2026-10-25)

- **Donor jars** (Vinery 1.3.10/1.5.5 sources, Ender's Delight, Miner's upstream): copied by hand into the donor folder today;
  later downloaded from their source by the build/launch step. The existing `UpstreamJars` sha1 pin and `UPSTREAM_DONORS` in
  `tools/build_deploy.py` stay the mechanism; a download step would verify the same sha1 and refuse otherwise.
- **Ender's Delight:** any needed code is **rebuilt licence-compliant** (fresh implementation, no upstream code), as a later
  submod. It is **left until later**; the earlier "leave it a separate jar for the first release" stands in the meantime.
  Found 2026-10-25 (devmods evaluation, [ends_delight.md](ends_delight.md) 3.2): the 1.20.1 build `endersdelight-1.1.4`
  declares MIT (author Axedgaming only), while the installed 1.2.2 is ARR. If upstream confirms the grant covers the shared
  content, a 1.19.2 build from the MIT sources could fold Ender's in like End's. Owner's call; nothing changes until then.

## 16. Scope gate: the backport and overlap analysis

Which mods become members depends on the result of the backport / overlap analysis (the marked `*overlaps w/*` items plus
the TODO overlap passes). Until it lands, sections 0 to 2 are provisional and the first merge session starts only after
the list is final. Q18 is dropped: the subdict masks (section 13) make a separate analysis document unnecessary; membership is whatever the masks select.

## 17. Decisions log (second round, 2026-10-25) and open questions

Answered:
- Q1 namespaces and mod ids kept as original, display name only (decided).
- Q2 Ender's Delight: rebuild compliant later, by submod (decided; section 15).
- Q3 Java moves: fishing net with CD; mooshroom family into moreMooblooms behind a `fungidelight` check (decided; section 9).
- Q4 GPLv3 for all distributed code, MIT notices kept (decided).
- Q5 version naming unchanged (decided; Q17 only for the CD jar itself).
- Q7 display name: every shipped submod or backport gets the suffix; compartmentalized changes to other mods do not
  and load only when that mod is loaded (decided).
- Q8 dict pattern category / form / state with the owner's category list (decided; section 13).
- Q23 CD depends on patcherMod and thus on KubeJS (decided 2026-10-25; section 10.1). Not a standalone jar.
- Q11 QoL-1 to QoL-6 all in CD; the baked-data approach is replaced by section 10 (decided).
- K1/K2 (can a mod jar host KubeJS scripts): server and client yes, natively; startup no, patcherMod loader needed (read
  from KubeJS bytecode 2026-10-25, section 10.2; live check SQ-KS).
- Q20 (third round): no Java rules engine; CD ships KubeJS scripts calling the pack helpers (section 10.3).
- Q26 The shared KubeJS helpers live in patcherMod's jar as JavaScript, written once, loaded first (decided 2026-10-25,
  section 10.5 item 3; 10.6 shows how another mod calls them).
- Q12 Creative Equipment: placeholder only.
- Q6 Native jars: hybrid, repack by default, decompile per class only when needed (owner, 2026-10-25; section 14).

Answered (third pass, 2026-10-25):
- Q9 one pack-wide `f`; CD reads a member view of it.
- Q13 moreMooblooms takes a hard patcherMod dependency for the spawning systems (`SpawnTuning`, `CEConfig`).
- Q14 CD does not load without KubeJS (already settled by Q23).
- Q15 extra food categories accepted as the starting list; building the food dict is a TODO row (`creativedelight/framework #4`).
- Q16 nested, already answered by 14.1 to 14.3 and Q6. Spikes SQ-CDJ-01 to 03 stay queued.
- Q17 CD jar version: follows the pack's own scheme, starts at 1.0.0, then 1.0.01, 1.0.02.
- Q18 not needed: subdict masks replace the separate overlap analysis.
- Q19 swappers still later. When built, every trigger is a config boolean: on world load, on player data load, on container
  open, on command, and once when the swap list was updated.
- Q21 yes, CD gets its own config; defined in 8.1.
- Q22 hard cut at the CD release; no deprecation step and no duplicate patcherMod copy.
- Q24 recipe id namespace is CD's own (`creative_delight:`), not each member's.
- Q25 CD scripts ship only in the jar.
- Q27 dicts stay in the pack folder; NCH reads them lazily.
- Q28 repack jar names `repacked<Name>`.
- Q29 members under `development/creativeDelight/members/`.
- Q30 Oceanic overlay inside its repack.

Open:
- Q24 detail: the NCH namespace option shape (per call or scoped).
- Q21 detail: confirm the key list in 8.1 when the first QoL feature moves.

## 18. History

- 2026-10-25: file written from the six writeups, the projects' `gradle.properties` and `mods.toml`, and `mod_sides.md`.
- 2026-10-25: scope revised (owner): Brewin' and Vinery parked (section 7); naming rule added (0.3); End's, Ender's, Ocean's
  and Oceanic added (section 2); scope widened to Delight mods touched by KubeJS, with a move-or-stay split (section 3).
- 2026-10-25: owner direction, round 1: QoL marking, food-only rule, baked suite and masked food dict. (Superseded in part.)
- 2026-10-25: owner direction, round 2 (this revision): swappers with retained replacement lists, optional integrations,
  patcherMod as a dependency (bake replaced by a rules engine, section 10), QoL-1 to QoL-6 accepted, mooshrooms to
  moreMooblooms, fishing net with CD, GPLv3, donors, Q6 detail, dict grammar and categories, open-question scan.
  Plan only, nothing built.
- 2026-10-25: owner decision: Creative Delight depends on patcherMod and thus on KubeJS (section 10.1, Q23); option table
  and Q14 adjusted to match.
- 2026-10-25: third round (owner: call the pack helpers from CD): KubeJS script loading checked in the 1902.6.2 bytecode;
  CD ships server and client scripts in the jar that call `global.NCH` / `global.tags` / `global.LOOT`; Java rules
  engine dropped; patcherMod adds a startup-script loader; sections 3.1, 6, 8, 10 to 13 and 17 rewritten; SQ-KS queued.
  Plan only, nothing built.
- 2026-10-25: Q6 decided (hybrid). Nested shape investigated from the Forge 43.3.9 loader, Mixin 0.8.5 and this pack's
  log (14.1): nested mods, mixins, refmaps and ATs load; repack version rule added (Ocean's trap); build list B1 to B10
  (14.3); 6.1 rows updated; SQ-CDJ queued. Plan only, nothing built.
- 2026-10-25: third pass: owner answered Q9, Q13 to Q19, Q21, Q22, Q24, Q25, Q27 to Q30 (section 17); 8.1 config added; food dict build
  added to TODO. Plan only, nothing built.
- 2026-10-25: TODO family `creativedelight` opened with briefs (section 20); no code changed.
- 2026-10-25: framework built (owner: "build the framework as planned, repack, apply the mixins, test suite, scratch tests not
  run, no ongoing backport/merge features"). Section 19.
- 2026-10-25: food dict built (owner: "build the new food dictionary ... fold in all the existing food items ... audit all
  food mods"): grammar file, mask check, `food_dict.js` with 1542 keys, audit of the food mods' non-edible ingredients,
  `tools/food_dict.py` lint/coverage/index, CDT-08 and CDT-10 passed offline. Section 13.4, 13.5.

## 19. Built 2026-10-25: the framework

### 19.1 What exists

| Piece (plan ref) | Where | Notes |
|---|---|---|
| Startup loader (10.5.1) | patcherMod `compat/kubejs/JarStartupScripts`, mixins `KubeJSStartupJarScriptsMixin` (RETURN of `ScriptManager.loadFromDirectory`, require = 1) and `KubeJSScriptManagerInvoker` (`loadFile`) | Every mod jar with a root `kubejs_startup/` folder becomes a startup script pack named after its first mod id; order patcherMod, pack folder, other jars in mod-list order. Headers (`priority`, `requires`, `packmode`) go through KubeJS's own `loadFile`. Each pack has its own scope: shared code goes through `global` |
| Helpers in patcherMod's jar (10.5.3, Q26) | `development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/` | `neoCraftHelper.js`, `tag_helper.js`, `compost.js` moved with `git mv` (backups in 19.4). The pack folder keeps no copy. NCH reads `global.item` at call time now (six lines). The other `func/` helpers stay in the pack (they are pack-specific or read the dicts at load) |
| NCH namespace (10.3, Q24) | NCH `ns()`, `withNamespace(ns, fn)`, `apiVersion: 1` | Scoped (answers the open Q24 detail): every recipe ID the helpers write uses `ns()`, recipe TYPES keep `modId` (`creativeengineering:seasoning`, ...). Restores after an error and when nested |
| Build (14.3 B1, B3, B4, B5) | `build-logic`: `ce.forge-repack`, `ce.forge-bundle`; `ce.forge-mod` gains `shipped_jar_task`, `bundled_in`, `mixin_configs=none` | A repack refuses another upstream sha1 and any path the overlay and upstream both hold; `replaced_classes` is the B3 guard (no class replaced today). A member's `installToMods` refuses and names CD's task |
| Repacks (B2) | `development/creativeDelight/members/{oceans,oceanic,ends}`, projects `repackedOceans`, `repackedOceanic`, `repackedEnds` | Versions `1.0.2-1.19.2-ce-19.2-01`, `1.0.1-ce-19.2-01`, `1.19.2-2.1-ce-19.2-01` (14.1.4). Upstream entries checked byte-identical (CRC) apart from `mods.toml` and the manifest. Upstream jars in `development/creativeDelight/upstream/` (git-ignored, copied from `mods/`). MIT notices written from the `mods.toml` data, since the jars ship none; years and sources checked against upstream 2026-10-25 (28.1) |
| Oceanic overlay (B7) | `members/oceanic/src`, package `com.SaintAlfonzo.creativedelight.oceanic.mixin`, config `oceanic_delight.ce.mixins.json` | Moved from patcherMod: normal mixin, `@Mixin(FishingNetExtractingProcedure.class)`, SRG names with `remap = false` (checked reobfed). Tag ids stay `creativeengineering:fishing_net/*` (filled by `loottags.js`, not moved). `loot/BiomeTagCondition` stays in patcherMod |
| CD jar (B6) | `development/creativeDelight`: `@Mod creative_delight`, `CDConfig`, `kubejs_startup/creative_delight/cd_core.js` (`global.CD`) | `creative_delight-1.0.0.jar` (Q17), reproducible. Dependencies: patcherMod `[1.1.02,)` and Farmer's Delight; at boot it logs an error when the installed patcherMod has no loader. `replaces_jars` = the three upstream jars and stray repacks |
| CD config (8.1, Q21) | `config/creative_delight-common.toml` | Only `[members]` (one switch per member, default on); `qol`, `integrations`, `swappers` keys come with the first feature that moves. Scripts: `global.CD.on(id)` inside callbacks (COMMON config loads after startup scripts) |
| CD script trees (10.4) | `kubejs/data/creative_delight/kubejs/`, `kubejs/assets/creative_delight/kubejs/` | Only `_core/` checks so far (warn when `global.CD` is missing). The jar build copies both trees and fails if a tree has no script. `build_deploy.py` classes both trees `dev` (Q25) |
| Deploy (B8) | `tools/build_deploy.py` | `('creativeDelight', 'creative_delight', True)` in `IN_TREE_MODS`; freshness covers the members and the script trees; nested mod ids join the duplicate-id check (a stale upstream jar beside CD is an error); a jar holding `_cdtest/` is refused. The port rows stay until the ports join |
| Test suite | `tools/creative_delight_test/` | See its README. The cooking mirror is generated (`gen_cdtest_mirror.py`): 49 single-line calls of 30 helpers from `server_scripts/cooking/`, run unchanged from the CD tree |

Not changed: `mod_sides.md` gets the `creative_delight` row now; the three member rows stay until the jar is installed (B9).

### 19.2 Tests (CDT-)

Offline, run 2026-10-25: every new or moved script compiles in KubeJS's Rhino; NCH loads with no dicts and `ns()` /
`withNamespace` scope, nest, restore after an error and reject a bad name (headless Rhino); patcherMod, the three repacks and
CD build; repack bytes match upstream; overlay reobfed; normal CD jar unchanged by a test-suite build; `build_deploy.py` reads
the nested ids. Two offline sims that load `tag_helper.js` from its new path ran.

| ID | Test | Where |
|---|---|---|
| CDT-01 | Startup loader: pack order, file counts, `requires` | SQ-CDT-01, -02 |
| CDT-02 | Pack's own scripts unchanged after the helper move | SQ-CDT-03 |
| CDT-03 | NCH namespace, compartments, tags, members config | SQ-CDT-04, -08 |
| CDT-04 | Existing cooking calls from the CD tree give identical recipes | SQ-CDT-05 |
| CDT-05 | `/reload`, client tree, folder dev path | SQ-CDT-06, -07, -09 |
| CDT-06 | CD with an old patcherMod reports it | SQ-CDT-10 |
| CDT-07 | Nested members load, names, stale jar, Oceanic overlay, End's | SQ-CDJ-01 to -04, -06; SQ-FN (setup changed) |
| CDT-08 | Food dict load check passes; three bad lines (missing general key, key outside the grammar, item named twice) make it throw | `tools/test_food_dict.sh` (PASSED offline 2026-10-25); SQ-FD-01 |
| CDT-09 | Veggies pilot recipes identical through `f` | not built (13.4 point 2) |
| CDT-10 | Coverage: no food without a key | `python3 tools/food_dict.py` (PASSED 2026-10-25: 1542 keys, 0 missing) |

Scratch run 2026-10-25 (scratch_queue.md run log): CDT-01 to CDT-04 and CDT-06 PASSED; CDT-05 PASSED apart from the
client tree (SQ-CDT-07, Round 2); CDT-07 server halves of SQ-CDJ-01, -06, SQ-FN-01, -04 PASSED, **SQ-CDJ-02 FAILED** (a
stale top-level upstream jar beats the nested repack, 14.1 point 4); client names, Oceanic net loot by biome and the
bundled ports still open. Fixed in the run: `cd_core.js` `members()` (`Array.from` on a Java List stopped boot). Boot
cost of the CD jar against the three upstream jars: none measurable (mean 108.0 s against 104.7 s, inside the spread).

### 19.3 Not done (owner: no ongoing backport or merge features)

The four ports stay separate jars (`bundle_members` and their `mod_name` change when they join, SQ-CDJ-05); member rows of Veggies, Vintage,
Fungi and Miner's moved into the CD trees 2026-10-25 (section 27; Ocean's, Oceanic and End's not yet); QoL-1, 2, 4, 5 and 6 moved 2026-10-25 (section 22; QoL-3 pinned); mooshroom family not moved
(section 9); swappers (11), Ender's rebuild (15) not started; food dict built (13.5), tool and misc dicts not. Generators can write into the CD trees (`outRoot`, 28.2); none does yet.

### 19.6 First member scripts (2026-10-25)

New changes for members are written in the CD trees, one folder per mod (owner, 2026-10-25): `vintagedelight/recipes.js`,
`vintagedelight/loot.js`, `veggiesdelight/loot.js` (`// requires: rusticdelight` too) and `create/slime.js` (a non-member
row, `// requires: create`, `create_compressed`) in `kubejs/data/creative_delight/kubejs/`, and the jar startup script
`kubejs_startup/vintagedelight/vintage_items.js`. Recipe files run inside `global.CD.recipes` (ids
`creative_delight:<type>/<suffix>`) and return early without `global.CD` or with the member switched off; the loot files
only need `global.lootTuning`. Until CD and its patcherMod are installed none of this runs (startup half) or the recipe
files return early, and the deploy ships the trees only in the jar (Q25). Contents: [vintage_delight.md](vintage_delight.md)
4.0b, [kitchen.md](kitchen.md) 3. The Java parts of the same rows went into the port projects (`backportedVintage -04`,
`backportedVeggies -03`), which join CD when the ports are bundled (19.3).

### 19.4 Backups of moved code

`development/creativeDelight/moved_code_backup/2026-10-25/` holds each moved file at its old path, as it was before the move:
`kubejs/startup_scripts/func/{neoCraftHelper,tag_helper,compost}.js`, patcherMod's `OceanicDelightFishingNetMixin.java` and
`creativeengineering.mixins.json`. Old to new: the three helpers to patcherMod `src/main/resources/kubejs_startup/creativeengineering/func/`;
the mixin to `members/oceanic/src/main/java/com/SaintAlfonzo/creativedelight/oceanic/mixin/`.

### 19.5 Install (not done; never while the game runs)

patcherMod and CD go in together: the patcherMod jar in `development/patcherMod/build/libs/` holds the helpers the pack folder no
longer has, and the fishing net mixin is only in CD now. `./gradlew :patcherMod:installToMods :creativeDelight:installToMods
--offline` (CD's moves the four upstream jars, Farmer's Delight included, to `jar_archive/displaced/`), then the
`mod_sides.md` member rows go (B9).

## 20. TODO family (2026-10-25)

Open work is the `creativedelight` family in [TODO.md](todo/TODO.md): `framework` (scratch tests, install, notices, food dict, config
keys, move-diff harness, generators, compartments, donor download), `members` (bundle the ports, per-member script moves, Ocean's / Oceanic
and End's passes, mooshroom family to moreMooblooms, Ender's rebuild, cutover), `qol` (QoL-1 to QoL-6), `swappers` (pinned). Briefs:
`briefs/creativedelight_*.md`. Rows moved in: former `ports/creativedelightport` #1 to #5 and `food/types-and-tuning #14`.

## 21. Farmer's Delight as a member, staged for the 1.3.4 backports (2026-10-25)

Owner: repack Farmer's Delight (MIT) into Creative Delight so the few 1.3.4 features worth having
([farmers_delight.md](farmers_delight.md) 3) can be backported into FD itself; **no feature backported yet**.

| Piece | Where | Notes |
|---|---|---|
| Member `repackedFarmers` | `development/creativeDelight/members/farmers`, root `settings.gradle` | Upstream `FarmersDelight-1.19.2-1.2.4.jar` (sha1 `7474aaf5...`, copied to `upstream/`), version `1.19.2-1.2.4-ce-19.2-01`, display name "Farmer's Delight (Creative Delight)". Every upstream entry byte-identical (CRC) apart from `mods.toml` and the manifest |
| FD's own mixins | member `gradle.properties` `mixin_configs=farmersdelight.mixins.json` | The repack replaces the upstream manifest, and FD's `MixinConfigs` line lives there: without this line FD's 9 mixins (7 common, 2 client) would silently not load. Overlay mixin configs are appended to the same key. AT (`META-INF/accesstransformer.cfg`) and refmap ride along as files |
| Bundle | CD `gradle.properties` | First in `bundle_members` (the base); `FarmersDelight-*.jar` and `repackedFarmers-*.jar` in `replaces_jars` |
| No member switch | `CDConfig.BASE_MEMBERS` | Every other member needs FD, so `[members]` gets no `farmersdelight` key and `global.CD.on('farmersdelight')` is true while it is loaded |
| Donor | `upstream/donors/FarmersDelight-1.20.1-1.3.4.jar` (git-ignored; from `devmods/checkNewerVersionForContent/`), `donor_jar` / `donor_sha1` in the member's `gradle.properties` (record only) | The newest Forge build (1.20.1, Forge 47); its `mods.toml` declares MIT like 1.2.4. Being MIT, it is a source to decompile from (Q6 hybrid), not a runtime donor like Miner's or Vinery |
| Edited upstream files | `ce.forge-repack`: new `replaced_resources` key | Same form and sha1 guard as `replaced_classes`, for any non-class file (`assets/farmersdelight/lang/en_us.json=<sha1>`): the overlay's copy replaces upstream's. Tested offline 2026-10-25: unlisted clash fails, wrong pin fails, right pin ships the overlay copy |
| Notice | `members/farmers/NOTICE-upstream-MIT.txt` | Written from `mods.toml` (the jar has no LICENSE); covers the 1.3.4 code too. Copyright line to check (framework #3) |

### 21.1 Checks run (offline, 2026-10-25)

- **Dependents accept the version.** 24 jars in `mods/` declare `farmersdelight` (scan of every `mods.toml`, nested ones too;
  none nests its own FD). Every range, tested with the loader's maven-artifact 3.8.5, holds both `1.19.2-1.2.4` and
  `1.19.2-1.2.4-ce-19.2-01`: `[0,)`, `*`, `[1.1.2,)`, `[1.19-1.2,)`, `[1.19,1.20)` (Farmer's Respite), `[1.19-1.2.0,]`
  (Supplementaries, optional), `[1.19-1.2.1,)`, `[1.19-1.2.4,)`, `[1.19.2-1.2.2,)`, `[1.19.2-1.2.4,)` (the five ports),
  `[1.2.0,)`, `[1.2.3,)`. The one unparsable range, Ocean's `[1.19-1.2.0)`, is the upstream jar the Ocean's repack replaces.
  The repack version is newer than the upstream one.
- **Builds unaffected.** The ports and patcherMod compile against their own `libs/` copies of FD, not `mods/`, so the
  install moving FD out of `mods/` breaks no build. `:repackedOceans`, `:repackedOceanic`, `:repackedEnds` and
  `:creativeDelight` build with the plugin change; `metadata.json` lists the four members, `creative_delight_members.txt`
  starts with `farmersdelight`.
- **Deploy.** `build_deploy.py` reads `mods/`, so the standalone FD jar drops out of the payload once CD is installed (removal by
  file name); a stale FD jar beside CD is caught by the nested duplicate-id check (and would win at runtime, 14.1 point 4).

Live checks: SQ-CDJ-07 (server: loads, FD and patcherMod mixins, dependents, old world, config) and SQ-CDJ-08 (client) in
[testing/scratch_queue.md](testing/scratch_queue.md), queued.

**First features in (2026-10-25):** repack `-02`, [farmers_delight.md](farmers_delight.md) 4. The overlay registers through
`FarmersDelightInitMixin` (end of FD's constructor), owns its own config `farmersdelight-ce-common.toml`, and replaces
FD's `lang/en_us.json` and `sounds.json` (`replaced_resources`). Member scripts: `kubejs/data/creative_delight/kubejs/farmersdelight/`.

### 21.2 How a 1.3.4 feature goes in (when picked up)

1. Java (new blocks, items, mixins) in `members/farmers/src/main/java/`, own package
   (`com.SaintAlfonzo.creativedelight.farmers`, never `vectorwing.*`: split packages are fatal unless the class replaces one,
   14.1 point 8); registered with FD's mod id through its own `DeferredRegister`s, so ids are `farmersdelight:<id>`.
   Mixins get their own config (`farmersdelight.ce.mixins.json`), appended to `mixin_configs`.
2. A changed FD class: decompile it from 1.2.4 into the overlay at its upstream path and list it in `replaced_classes`;
   use the 1.3.4 donor only as the reference for the change.
3. New assets and data at new paths need nothing; an edited FD file (lang, tags) goes in `replaced_resources`.
4. Recipe and loot rows keep going through `global.NCH` and the loot helpers in the pack (or the CD tree).
5. Version bump `-ce-19.2-02`, rebuild CD, changelog bullet, test IDs in this file and the queue.

## 22. Built 2026-10-25: the QoL moves (QoL-1, 2, 4, 5, 6)

Owner: "build all of the QoL changes as part of Creative Delight". QoL-3 stays pinned (owner, same day: the food type
rules are still in development and its tables are to be keyed by the food dict). Behaviour is meant to be unchanged with
every switch on; nothing here is installed (19.5: patcherMod and CD go in together).

### 22.1 Config (framework #5) and compartments (framework #8)

- `creative_delight-common.toml [qol]`: `remainderBuffer`, `placeableTooltips`, `compost`, `manualOnlyRoutes`,
  `wthitCropLine`, `jeiInfoPages`, all default on (`CDConfig.qol(id)`, constants `CDConfig.REMAINDER_BUFFER` ...).
  `foodTypes` comes with QoL-3. Scripts: `global.CD.qol(id)` (cd_core.js), read inside callbacks like `on()`.
- `global.CD.on(id)` fixed: a loaded mod that is not a nested member has no `[members]` switch and now counts as on.
  Before, `on('vintagedelight')` was always false, so `vintagedelight/recipes.js` would never have run until the port
  is bundled.
- Compartments: `compat/CDMixinPlugin` (written the same day for the Fruits Delight fixes) applies a mixin in
  `mixin.<modid>.` only when that mod is loaded. QoL-1 adds `mixin.farmersdelight` and `mixin.farmersrespite`.
  WTHIT needs no mixin: its plugin is in CD's `waila_plugins.json`, which WTHIT reads only when it is installed.
  CD's mixin json has `defaultRequire: 1` (patcherMod's has none): every pot and kettle injection target was checked
  with javap against Farmer's Delight 1.2.4 and Farmer's Respite 2.0.
- Build: CD compiles against `project(':patcherMod')` (buffer API, `CEConfig`, `CropGrowth`) and the FD, Farmer's Respite
  and WTHIT jars in `development/creativeDelight/libs/` (git-ignored, copied from `patcherMod/libs/`).

### 22.2 What moved

| QoL | From | To (CD) | Switch off |
|---|---|---|---|
| 1 Remainder buffer, pot + kettle | patcherMod `mixin/{CookingPot,Kettle}{,Menu,Screen}RemainderMixin` | `mixin/farmersdelight/`, `mixin/farmersrespite/` (git mv), gate `qol/RemainderBufferQol` | new leftovers are thrown as upstream; held ones still show, drop and can be pulled |
| 1 Remainder buffer, oven | stays | patcherMod (see below) | patcherMod `[cooking] remainderBuffer` (now oven only) |
| 2 Placeable tooltips | `kubejs/client_scripts/placeable_tooltip.js` | `kubejs/assets/creative_delight/kubejs/_qol/placeable_tooltip.js` | no lines; the mods' own lines are not stripped |
| 4 Compost rows | 101 Farmer's, Fungi, Veggies and Vintage Delight rows of `startup_scripts/compost/compost_values.js` | jar startup scripts `kubejs_startup/{farmersdelight,fungidelight,veggiesdelight,vintagedelight}/compost.js` (48 + 19 + 12 + 22) | rows skipped, the foods fall to the pack's rules |
| 5 Grid-meal moves, heatless routes, wraps | Veggies, Vintage, Fungi blocks of `server_scripts/cooking/cooking.js` | `kubejs/data/creative_delight/kubejs/{veggiesdelight,vintagedelight,fungidelight}/cooking.js` | file skipped: the mods' own recipes stay |
| 6 WTHIT crop line | patcherMod `crops/WthitCropGrowth`, `assets/creativeengineering/crop_growth.json`, lang key | `compat/wthit/WthitCropGrowth`, `assets/creative_delight/crop_growth.json`, `creative_delight.wthit.crop_harvestable` | no line |
| 6 JEI info pages | `kubejs/client_scripts/jei_info.js` (Fungi's) | `kubejs/assets/creative_delight/kubejs/fungidelight/jei_info.js` (`// requires: jei`, `fungidelight`) | no pages |

Notes:
- **Oven stays in patcherMod.** The plan had an oven compartment `cooking_for_blockheads`, but the oven is patcherMod's
  own block (`creativeengineering:oven`) and its buffer is native code in `OvenBlockEntity`, not a hook into Cooking for
  Blockheads. Moving it would mean moving the block and its registry ids. The buffer API (`cooking/Remainder*`) stays in
  patcherMod as shared API (the brief's recommendation), with `RemainderBufferStorage` and `WthitRemainderBuffer`.
- The kettle is Farmer's Respite's, so its compartment is `farmersrespite`, not `farmersdelight`.
- WTHIT dials `fallback` and `showHarvestable` stay in `creativeengineering-common.toml [crop_growth]` (pack-wide
  values, section 12); its `enabled` key is gone (the CD switch replaces it; Forge drops the stale line on load).
  `CropGrowth` (format, globs) stays in patcherMod: `StateLines` uses it. `tools/crop_growth_coverage.py` reads the new path.
- Compost: patcherMod's engine gained `values(category, rows, { when })`; `when` is checked at every table rebuild, so
  the CD rows follow the switch without the config being loaded at script time. `sandy_shrub` (a Farmer's Delight item)
  was filed under the Fungi rows; it is in the FD file now. The pack file's header said 745 items, it held 744; 643 now.
- QoL-5 recipe ids change namespace (`creativeengineering:` to `creative_delight:`, Q24); outputs and inputs unchanged.
  The rules they follow and the shared helper recipes (bone broth, cooked forms, the cauliflower bread oven route) stay
  in the pack's cooking and baking files; the rest of each member's rows still move with members #2 to #4.
- Backups of every moved or edited file at their old paths: `development/creativeDelight/moved_code_backup/2026-10-25/`
  (`*.before_qol` for files that stay but changed).
- `tools/remainder_buffer_test` now syncs the pot and kettle mixins (and `CDConfig`, `RemainderBufferQol`) from CD. The
  harness does not compile today for an older reason: patcherMod's `oven/BakingRecipe` imports `food.CanServings`, which
  the harness does not sync (already so at HEAD).

### 22.3 Install caveat

The rebuilt patcherMod (still 1.1.02) has no pot and kettle hooks; the installed CD jar does not yet have them either.
Install both together (19.5). An old patcherMod jar beside the new CD would apply the pot and kettle mixins twice (CD's
`[1.1.02,)` range does not catch it): bump patcherMod's version and CD's range at the cutover release.

### 22.4 Tests (CDQ-)

Offline 2026-10-25: patcherMod and CD build; CD's own jar holds the six mixins, `waila_plugins.json`, the rules file, the
lang file, the four compost scripts and the five moved scripts, manifest `MixinConfigs: creative_delight.mixins.json`;
patcherMod's jar has none of them; every moved or edited script compiles in KubeJS's Rhino; the compost `when` gate
tested in node (on, off, throwing). Live checks are queued (scratch_queue.md SQ-CDQ).

| ID | Test | Where |
|---|---|---|
| CDQ-01 | Pot and kettle buffer unchanged (remainder_buffer.md RB-01 to RB-11: bottle, bowl, bucket stay, stall when full, hoppers) | SQ-CDQ-01 (client) |
| CDQ-02 | `[qol] remainderBuffer = false`: pot and kettle throw leftovers; oven still buffers | SQ-CDQ-02 |
| CDQ-03 | CD boots without Farmer's Respite (kettle compartment skipped, no mixin error) | SQ-CDQ-03 |
| CDQ-04 | Placeable lines unchanged; off: none, mods' own lines back | SQ-CDQ-04 (client) |
| CDQ-05 | `/dumpcompost`: every moved food keeps its chance and source `value`; off: they fall to rules | SQ-CDQ-05 |
| CDQ-06 | QoL-5 recipes present under `creative_delight:`; off: the Veggies, Vintage and Fungi grid recipes are back | SQ-CDQ-06 |
| CDQ-07 | WTHIT growth line on hemp, a bush and a Delight crop; off: none | SQ-CDQ-07 (client) |
| CDQ-08 | JEI info pages for the Fungi items; off: none | SQ-CDQ-08 (client) |

## 23. Rustic Delight as a member (2026-10-25)

Owner: execute the Rustic Delight fold-in and backport. Rustic Delight is all rights reserved, so it cannot be repacked:
`backportedRustic` (`members/rustic`) is a fresh port of 1.7.0 that reads art and data from the unmodified donor in
`rustic_upstream/` (the Miner's route), built as a member from the start (`bundled_in=creativeDelight`, fifth in
`bundle_members`; `rusticdelight-*.jar` in `replaces_jars`). Member switch `rusticdelight` comes from the generated member
list. Its pack rows are in `kubejs/data/creative_delight/kubejs/rusticdelight/` (`loot.js`, `recipes.js`, `tags.js`,
`trades.js`). The village farm crop processor (`farm/VillageFarmCrops`, `creative_delight:village_farm_crops`) lives in the CD
jar itself, not in the member (2026-10-25): it adds Rustic's crops to the vanilla `farm_*` processor lists by block id and is
the village farm audit (`farming/crops #16`, section 25). Everything else, tests (SQ-RD) and the cutover: [rustic_delight.md](rustic_delight.md) 5.


## 24. Wild crops in Creative Delight (2026-10-25)

Owner: fold the wild crop spawning system into Creative Delight. Decisions (2026-10-25): **all 63 rules** move, vanilla,
Neapolitan, Vinery and rubber included (CD owns wild crops; without the CD jar no crop is re-placed); the switch moves
to **CDConfig `[worldgen] wildCrops`** (CEConfig `[worldgen.systems] wildCrops` and `WorldgenConfig.wildCropsEnabled()`
removed); ids **renamed to `creative_delight`**. Same rules and rates as before: no player-visible change, so no
changelog bullet. Player-side reference stays [crops.md](crops.md) 2.8.

### 24.1 What moved, what stays

| Piece | Now | Was |
|---|---|---|
| Crop rules | `kubejs/data/creative_delight/kubejs/_worldgen/wild_crops.json` (63 rules) | `kubejs/generation_scripts/worldgen/wild_crops.json` "crops" |
| Applier | `kubejs/data/creative_delight/kubejs/_worldgen/wild_crops.js` (returns without `global.CD`) | `kubejs/server_scripts/worldgen/wild_crops.js` |
| Biome tags, modifiers | `creative_delight:wild_crops/<key>`, `creative_delight:forge/biome_modifier/wild_crops/<key>` | `creativeengineering:...` |
| CD features (11 placed, 4 configured) | `kubejs/data/creative_delight/worldgen/{placed,configured}_feature/wild/`, ids `creative_delight:wild/*` | `creativeengineering:wild/*` |
| Switch | `creative_delight-common.toml [worldgen] wildCrops`, `global.CD.worldgen(id)`, `CDConfig.worldgen(id)` | CEConfig `[worldgen.systems] wildCrops` |
| `VillageFarmCrops` tags | `#creative_delight:wild_crops/<key>` | `#creativeengineering:wild_crops/<key>` |
| **Stays in the pack:** biome traits | `kubejs/generation_scripts/worldgen/biome_traits.json` (split out of the old file; also read by `mushroom_spawns`, `wild_flowers`, `bee_nests`, `mob_spawns` and their generators) | `wild_crops.json` "biomes" |
| **Stays:** machinery | `kubejs/server_scripts/worldgen/_biome_rules.js` (`global.biomeRules`, shared with mushroom spawns; 3.1 rule 3), new optional `readConfig` hook | |
| **Stays:** mushroom and cave carrot features | `creativeengineering:wild/{*_mushroom_colonies,cave_carrots/*,mushrooms/*}` (mushroom_spawns.json) | |

The hamimelon feature keeps reading the pack's block tag `#creativeengineering:worldgen/grass_like_soils` (CD needs the pack anyway).

### 24.2 How it loads

- Script and rules are authored in the pack folder and copied into the jar at the same paths; `kubejs/data/creative_delight/worldgen/`
  is a new `dataTrees` entry in CD's `build.gradle`, and `build_deploy.py` classes it `dev` like the script trees (Q25).
- KubeJS's `JsonIO` reads files only, so the script reads `kubejs/data/creative_delight/kubejs/_worldgen/wild_crops.json` while
  it exists (dev) and otherwise the jar copy through `CDResources.text(path)` (new class). Same precedence as the scripts (10.4).
- Older CD jar (no `global.CD.worldgen`): the switch reads on. Older jar without `CDResources`: the folder copy is used.
- Stale ids dropped on the way: Rustic's `add_wild_cotton`, `add_wild_bell_peppers`, `add_wild_coffee` and Miner's `cave_carrots`
  (mushroom_spawns.json) in `disable_modifiers`; neither port ships biome modifiers, so the `forge:none` files did nothing.
- `tools/gen_crop_worldgen_table.py` now also reads the members nested under `META-INF/jarjar/` of a bundle jar (it missed
  Farmer's Delight and Rustic since the CD install and reported their modifiers as unshipped).

### 24.3 Tests (WCD-)

Offline 2026-10-25: `tools/test_wild_crops.sh` passes (63 crop tags match the generator; 62 add modifiers, 49 `forge:none`,
1 remove modifier; switch off: nothing; Neapolitan missing: its rows skipped); `test_mushroom_spawns.sh`, `test_wild_flowers.sh`,
`test_bee_nests.sh` pass on `biome_traits.json`; patcherMod and CD build; the CD jar holds the script, the rules, the 15 features and
`CDResources`. Live checks are queued (scratch_queue.md SQ-WCD).

| ID | Test | Where |
|---|---|---|
| WCD-01 | Fresh level: `[wild-crops]` modifier counts as offline, no problems; datapack-tree script runs in the biome-tag pre-pass | SQ-WCD-01, -03 |
| WCD-02 | Cocoa and melons in a jungle, cabbages on a beach (CD features load) | SQ-WCD-02 |
| WCD-03 | `[worldgen] wildCrops = false`: off line, mods' own spawns back | SQ-WCD-04 |
| WCD-04 | Old CD jar + new folder trees: unchanged | SQ-WCD-05 |
| WCD-05 | Jar copy only (folder removed): unchanged | SQ-WCD-06 |
| WCD-06 | Savanna village farms grow cotton from the renamed tag | SQ-WCD-07 |

Install: patcherMod and CD together (19.5). Until then the folder trees run with the installed jars (WCD-04), except
`VillageFarmCrops` in the installed CD 1.0.0, which still reads the old `creativeengineering:` tags and finds them empty: no
Rustic crops in village farms until the new CD jar is in.


## 25. Village farm crops (2026-10-25)

Owner audit of village farms, built as the rewrite of `farm/VillageFarmCrops`. Reference, crop table, climate groups and
the planned ruins/pillager/remote structure rules: [crops.md](crops.md) 2.11.

- Data: `creative_delight/village_farms.json` in the jar (`share` 0.35, `groups`, `templates`); read through `CDResources`.
  No pack-folder override yet; edit, rebuild `:creativeDelight:`, restart.
- Hook: at `ServerAboutToStartEvent` the processor is appended to the processor list of every `SinglePoolElement` whose
  template id is listed (reflection on `SinglePoolElement.processors`, SRG `f_210412_`, template `f_210411_`). The old hook
  (the five vanilla `farm_*` lists) is gone: modded farms use `minecraft:empty`, which is shared.
- Roll: per field (all blocks of one vanilla crop in a placed piece), 35%; a rolled field is split 50/50 along its longer
  axis between the original crop and one crop of the village's group. 1.19.2 has no `finalizeProcessing`, so the roll is
  seeded by piece origin and crop, and the field extent comes from `StructureTemplate.filterBlocks` in the 7-argument `process`.
- Group `auto` (sky villages, Villager Farmer House, Campsite farmer camp, Terralith outpost) comes from the biome's base
  temperature and downfall. Switch: `[worldgen] wildCrops`.
- Log line at start: `village farm crops added to N pool elements of M listed farm templates`.

## 26. Delightful as a member (2026-10-25)

Owner: execute the Delightful backport as an in-tree mod under Creative Delight ([delightful.md](delightful.md) 3).
Delightful is MIT, so it takes the Farmer's Delight route (21), not Rustic's donor route (23): `repackedDelightful`
(`members/delightful`) repacks the upstream 3.4.1 jar unchanged (sha1-pinned in `upstream/`, version `3.4.1-ce-19.2-01`)
and its overlay registers the Delightful 3.8.1 items the pack takes in the `delightful` namespace, hooked by
`DelightfulInitMixin` at the end of Delightful's constructor (own mixin config, the upstream manifest has none). 3.8.1 is
the record-only donor (`upstream/donors/`), its art and data copied by `members/delightful/tools/port_resources.py`; the
edited `en_us.json` is in `replaced_resources`. Sixth in `bundle_members`; `Delightful-*.jar` and
`repackedDelightful-*.jar` in `replaces_jars`. Member switch `delightful` comes from the generated member list. Pack rows:
`kubejs/data/creative_delight/kubejs/delightful/` (`loot.js`, `recipes.js`, `tags.js`), startup values in
`src/main/resources/kubejs_startup/delightful/delightful_foods.js`. Neapolitan compartment (milkshake cauldrons) in the
member itself (`DelightfulMilkshakes`), loaded only with Neapolitan. Tests DF-01 to DF-11 and SQ-DF.

## 27. Member rows moved: Veggies, Vintage, Fungi, Miner's (members #2 to #5, 2026-10-25)

Rows that concern one member left the pack's scripts for the member's folder in the CD tree, unchanged (3.1 rule 1).
Recipe ids change namespace to `creative_delight:` (Q24, as QoL-5); inputs, outputs and values are the same, so no
changelog bullet. Every file returns early without `global.CD` or with the member switched off.

| Member | Moved rows | To (`kubejs/data/creative_delight/kubejs/` unless noted) | From |
|---|---|---|---|
| Veggies (#2) | 3 juices (pot, mixer, spout, drain, bottling) and the cauliflower bread oven route | `veggiesdelight/recipes.js` (new) | `cooking/cooking.js`, `cooking/baking.js` |
| Veggies (#2) | 6 crop crate packs, 9 chest table additions | `veggiesdelight/loot.js` | `loot/07_rules.js` |
| Veggies (#2) | 6 farmer trades, 7 Wandering Trader items | `veggiesdelight/trades.js` (new) | `economy/trades.js`, `economy/wandering_trades.js` |
| Vintage (#3) | furnace bread removals, the cheese and deluxe burgers (with Veggies' compat burger removals) | `vintagedelight/recipes.js` | `cooking/cooking.js` |
| Vintage (#3) | oat dough in `#forge:dough`, crop, seed and crate tags | `vintagedelight/tags.js` (new) | `tags/cookingtags.js` |
| Fungi (#4) | mushroom type tags, crate tags | `fungidelight/tags.js` (new) | `tags/cookingtags.js` |
| Miner's (#5) | Creamy Onion Soup cup conversion | `kubejs/data/creative_delight/cup_conversion/` (new `dataTrees` entry, `build_deploy.py` classes it `dev`) | `kubejs/data/creativeengineering/cup_conversion/` |

**Stays in the pack (decided while moving; the script rows of this list moved later, section 29):**
- Soft fluids (9 Veggies, 4 Vintage, `data/creativeengineering/moonlight/soft_fluids/`): Moonlight reads any namespace,
  but the soft fluid id is the file's id and Supplementaries jars store it in world NBT; moving would empty filled jars.
- Ghost pepper biome modifier: written by the pack's nether worldgen generator (`nether_worldgen_rules.json`), shared machinery.
- Fungi placed and configured features, `mushroom_spawns.json`, Miner's cave carrot feature (24.1, hard-dependency rule).
- Rows inside shared rule tables: pie and cookie rows in `baking.js`, the dough block loop (the blocks are pack items),
  the cooked-veggie soup tag list (`t_soup_veggie_cooked`), the garlic seasoning, dict keys, food types and generated cans (QoL-3 and framework #7).
- `vintagedelight/worldgen/configured_feature/patch_salt.json` overrides an upstream path; it stays where it is.
- Miner's Haste potions: nothing in the pack to move; the overlap with Rustic Delight waits for `MND-40` (in-game review).

**Order checks:** CD scripts run after the pack's. No pack script removes from or expands the moved tags; loot rules are
applied in the engine's phase order, not file order; the trade events add after the pack's own removals, as before.

**Move-diff harness (framework #6, offline half):** `tools/cd_move_diff.py snapshot` before a move, `diff` after it:
every code line of the scanned script trees (comments and whitespace stripped) and every data file's content must still
exist somewhere. Run 2026-10-25 around these moves: no line lost from the moved rows, one data file MOVED. Its two LOST
lines were an unrelated `neoCraftHelper.js` edit made by another session during the run, so take the snapshot just before
the move. The in-game half (recipe ids and counts, tags, loot from the dumps) is SQ-CDM.

Backups: `development/creativeDelight/moved_code_backup/2026-10-25/` (`*.before_members` for the pack files and the two CD
files that grew; the cup conversion file at its old path).

| ID | Test | Where |
|---|---|---|
| CDM-04 | Veggies: harness clean; juices, cauliflower bread, loot, trades present under the new ids | `cd_move_diff.py` (PASSED offline); SQ-CDM-00, -01, -03, -04 |
| CDM-05 | Vintage: harness clean; bread removals, burgers, tags | PASSED offline; SQ-CDM-01, -02 |
| CDM-06 | Fungi: harness clean; mushroom tags | PASSED offline; SQ-CDM-02 |
| CDM-07 | Miner's: cup conversion read from the CD tree and the jar | PASSED offline (in the jar); SQ-CDM-05 |

Also offline: all 12 changed scripts compile in KubeJS's Rhino; `:creativeDelight:build` holds the new files. Not installed.

## 28. Framework #3, #7, #10 (2026-10-25)

### 28.1 MIT notices checked (framework #3)

Each member's `NOTICE-upstream-MIT.txt` compared with the upstream repository and the Modrinth listing (CurseForge refuses
scripted reads). The repacks ship the notice as `<project>-NOTICE-upstream-MIT.txt` (checked in the built jar).

| Member | Upstream LICENSE | Change |
|---|---|---|
| Farmer's Delight | github.com/vectorwing/FarmersDelight, 1.19 and 1.20 branches: `Copyright (c) 2020 vectorwing` | year added |
| Delightful | github.com/brnbrd/Delightful: 1.19.2 branch `2023 brnbrd`, 1.20.1 branch (3.8.1 overlay) `2026 brnbrd` | both lines |
| End's Delight | github.com/FoggyHillside/End-s-Delight (Modrinth source link), 1.19.2 branch: `Copyright (c) 2022 FoggyHillside` | year and repo URL added |
| Ocean's Delight | No MIT file: master has the Forge MDK `LICENSE.txt` (LGPL text for Forge), the 1.19.2 Fabric branch the template CC0; the grant is `license="MIT"`, `authors="Scouter456"` in `mods.toml`; Modrinth lists MIT | no year exists; the notice says so |
| Oceanic Delight | No repository. Jar `mods.toml`: `license="MIT License"`. **Modrinth lists the project All Rights Reserved** (same author, and the same file: sha1 `91fd8842...` equals our upstream jar) | unchanged; **owner decision** |

Oceanic is the open point: the jar we repack says MIT, its own author's listing says ARR. The `mods.toml` field is the
only grant in the file we redistribute, but the conflict means the grant may be an MCreator default rather than a choice.
Options: ask DariusZeBaguette; or drop the repack and keep Oceanic as its own upstream jar (the fishing net overlay then
goes back to a `@Pseudo` mixin in patcherMod or CD). Recorded in TODO `creativedelight/framework #3`.

### 28.2 Generator output roots (framework #7)

`tools/gen_out.py`: a generator entry in `tools/generators.json` may carry `outRoot`, a folder inside one of the trees the
CD jar build copies (`kubejs/data/creative_delight/kubejs`, `kubejs/assets/creative_delight/kubejs`,
`kubejs/data/creative_delight/worldgen`, `.../cup_conversion`). The script calls `out_root(id, default)`; missing means the
default (pack folder, unchanged). `header(id)` is the first line of a generated file in a tree. `/cegen` is unchanged (the
runner ignores the field); `outputs` stay instance-relative and must lie under `outRoot` (`python3 tools/gen_out.py
--check`). A run after a jar build makes the jar stale, which `build_deploy.py`'s freshness check already refuses.
Test: `python3 tools/test_gen_out.py` (CDT-13, PASSED 2026-10-25).

**No generator uses it yet, on purpose.** Every candidate writes JSON that scripts read by instance path with
`JsonIO.readString` (`can_types.json` by CD's startup `cans.js` and server `canning.js`, `food_types.json`,
`placeable.json`, `mortar_catalog.json`). A file in the jar has no such path, and `can_types.json` is needed at startup,
whose CD scripts live in the jar project (rebuild per run). Moving one means emitting a script (a `.js` that sets a
`global` value) instead of JSON, and splitting the shared files per member. Which generators, and whether cans move at all,
is the owner's decision in the row.

### 28.3 Stale member jar warning (framework #10)

`CreativeDelight.warnIfNotNested`: at construction, for every bundled member, CD looks up its mod file. Forge loads a
nested jar from the root of its own `jij:` file system (`JarInJarDependencyLocator`, `FileSystems.newFileSystem`, checked
in fmlloader 43.3.9 bytecode) and a `mods/` jar from the default one, so a member on the default file system is a jar
beside CD that beat the nested copy (14.1 point 4). It logs one ERROR naming the jar and version. Loading is unchanged
(the top-level jar still wins). Built into `creative_delight-1.0.0.jar`, not installed; SQ-CDJ-02 rewritten to check it.


## 29. All pack food and cooking KubeJS moved into Creative Delight (2026-10-25)

Owner, 2026-10-25: "translate all kubejs cooking and food to the creative delight mod. merge all changes into the by mod
format already used". Scope chosen by the owner: every food script (server cooking, food tags, startup cooking, compost),
not only member rows, so section 27's "Stays in the pack" list and 3.1 rule 1's "rows that only concern non-members stay"
are superseded. Folders follow the mod each row touches (the food's mod; vanilla food `minecraft/`; machine-only routes
the machine's mod; pack-native systems `creativeengineering/`). Potion brewing, alcohol, Vinery and effect content stay
in the pack, marked for a future Creative Brewing mod (29.3). Rows are unchanged; recipe ids move from `creativeengineering:`
to `creative_delight:` (Q24), so no changelog bullet. TODO `creativedelight/members #13`; backups of every source and of
both CD trees before the move: `development/creativeDelight/moved_code_backup/2026-10-25b/`.

### 29.1 Where things went

Server scripts under `kubejs/data/creative_delight/kubejs/`, startup scripts under
`development/creativeDelight/src/main/resources/kubejs_startup/`.

| Old pack file | Now |
|---|---|
| `server_scripts/cooking/cooking.js` | split into `<mod>/recipes.js`: new `minecraft`, `create`, `creativeengineering` (mortar, dough-block loop, cornmeal grinding, burger table), `immersiveengineering`, `pneumaticcraft`, `corn_delight`, `endersdelight`, `farmersrespite`, `supplementaries`, `incubation`, `neapolitan`, `quarkdelight`, `alexsdelight`, `architects_palette`, `quark`, `buzzier_bees`; appended to `farmersdelight` (with the heatless meal rule), `delightful`, `rusticdelight`, `fruitsdelight`, `moredelight`, `abnormals_delight`. Optional-mod routes sit in `Platform.isLoaded` blocks. Left: the Brewin' food rows (29.3) |
| `baking.js`, `canning.js` | whole, `creativeengineering/` (every row shares the tables and the `n()` / `canSection()` helpers) |
| `salt.js`, `mortar.js`, `powdered_sugar.js`, `seasoning.js`, `cultivated_meat.js` | whole, `creativeengineering/` |
| `golden.js` | `minecraft/golden.js`, `create/golden.js`, `immersiveengineering/golden.js` |
| `chocolate.js`, `milk_jar.js` | `create_confectionery/chocolate.js`, `cookingforblockheads/milk_jar.js` |
| `tags/cookingtags.js` | split by the mod owning each entry into `<mod>/tags.js` (new `minecraft`, `creativeengineering`, `pneumaticcraft`, `create`, `incubation`, `alexsmobs`, `alexsdelight`, `quark`, `corn_delight`, `upgrade_aquatic`, `neapolitan`, `veggiesdelight`; merged into `farmersdelight`, `rusticdelight`, `delightful`, `fruitsdelight`). Left: Brewin' tags (29.3) |
| `tags/foodtypetags.js`, `cantags.js`, `jartags.js` | whole: `creativeengineering/food_type_tags.js`, `can_tags.js`, `jar_tags.js` |
| `startup_scripts/cooking/{bakeware,baking_ingredients,cornmeal,fluids,seasonings}.js`, `items/cans.js` | whole, `kubejs_startup/creativeengineering/` (ids unchanged: every `e.create` takes a full id from the dicts) |
| `startup_scripts/cooking/food_values.js` | value copies: applier `creativeengineering/food_value_copies.js`, rows in `minecraft/food_values.js` and `delightful/delightful_foods.js` (each `Object.assign`s into `global.foodValueCopies`). Left: the effect swaps and Farmer's Respite slice effects (29.3) |
| `startup_scripts/compost/compost_rules.js` | `kubejs_startup/creativeengineering/compost_rules.js` (`priority: 900`) |
| `startup_scripts/compost/compost_values.js` | 35 generated `kubejs_startup/<mod>/compost_values.js` (643 rows, `priority: 10`, so the hand-written QoL-4 `compost.js` files win); `tools/gen_compost_values.py` now writes them (`--check`, `--add-new` replaces `--merge`) and skips items in hand-written files |

Kept in the pack folder: the dicts, `kubejs/generation_scripts/food/*.json` (the CD scripts read them with `JsonIO` from
the pack folder; `build_deploy.py` ships them as runtime files).

### 29.2 Order: what the move changed and the fixes

CD server scripts run after every pack script, and jar startup scripts after the pack's `startup_scripts` (same load
pass; `// priority:` and `// requires:` work as in the folder).

- **Item merges.** `other/item_merges.js` swaps purged ids in script-added recipes; it ran after the cooking scripts and now
  runs before CD's (example: the generated canned coffee berries would ask for Farmer's Respite coffee berries again). The
  swap pass is now `global.itemMergeSwaps(e, where)`; `_core/item_merges.js` (`priority: -100`) runs it again last in the
  CD tree.
- **Removed-item tags.** `_lib/recipe_scan.js` strips every tag from removed items at the end of the pack's tag scripts,
  so the food-type tags (15 removed items in `food_types.json`) and the Delightful venison would have kept theirs. The strip
  is now `global.stripRemovedItemTags(e)`; `_core/removed_item_tags.js` (`priority: -10000`) runs it again last in CD.
- **Removals and swaps** (baking's every-cake/pie/cookie removal, the sugar, ice and flour swaps) only see data recipes, so
  running later does not change what they hit; no pack script swaps into sugar, ice or flour. Check in game (SQ-CDF-02).
- **Compost:** the engine builds its table on `TagsUpdatedEvent`, after all startup scripts; precedence (exclusion >
  value > rule) is in code. Headless run: the table from the new tree equals the old one (744 values, 10 categories, 5 tiers).
- **Startup registration** now happens after the pack folder's own KubeJS items: saved worlds keep their id map; the
  creative-tab order may shift.
- **Mortar catalog:** `tools/gen_mortar_catalog.py` skips `creative_delight:` recipes too (they get their mortar craft from
  `R.foodGrinding`), so a regenerate does not duplicate the moved grinding rows.
- **Test mirror** (`cdtest_cooking_mirror.js`) is frozen: its pack twins are gone (see the suite README).

**The pack now needs the CD jar for its food.** Without it the server rows return early and the startup half (bakeware,
dough blocks, cornmeal, the pack fluids, generated cans, seasonings, compost) is not registered at all: never boot a
world on a CD jar built before this move.

### 29.3 Left for Creative Brewing (TODO `creativebrewing/move #1`)

`startup_scripts/cooking/potions.js`, `alcohol_drinks.js`, `server_scripts/cooking/alcohol.js`; the effect swaps and
Farmer's Respite cake-slice effects (`startup_scripts/cooking/food_values.js`); the Brewin' and Chewin' food rows in
`server_scripts/cooking/cooking.js` (pierogi, pizza, ham and cheese sandwich, quiches with the egg-solution assembly,
horror lasagna, creamy onion soup, cheesy pasta, vegetable omelet: their egg solution now comes from CD's
`create/recipes.js`); the Brewin' tags (`tags/cookingtags.js`: lasagna meats, kimchi and pickled pickles as cooked soup
veggies, bell peppers as pizza toppings); effect text (`other/jeed_providers.js`, client effect lines). Borderline but
moved to CD: Vinery and Brewin' compost rows (grapes, kimchi, quiche) and the cider chain (drinks, not effects).

### 29.4 Checks and tests

Offline, 2026-10-25: all 160 CD and changed pack scripts parse in KubeJS's Rhino fork; tag callbacks run headless give
the same add / clear operations as before (1259 + 2 rows added since HEAD); the compost table is identical; the food value
copy table is identical in either load order; `cd_move_diff.py diff` loses only `console.log` boilerplate, the two
refactored global lines, the `foodValueCopies` opener and tag list lines split across mod files. In game: SQ-CDF-00 to
-08 ([scratch_queue.md](testing/scratch_queue.md)), plus SN-00 (seasonings), OF-00 (cans), CM-01 to CM-08 (compost).

## 30. Status snapshot and member table (2026-10-25)

Read from this file, `todo/TODO.md`, `mod_sides.md` and `mods/` on 2026-10-25. The game was not running.

### 30.1 Creative Delight

- **Installed, never booted.** `creative_delight-1.0.0.jar` is in `mods/` and is byte-identical to
  `development/creativeDelight/build/libs/creative_delight-1.0.0.jar`, beside `creativeengineering-1.1.03.jar`
  (patcherMod). The standalone Farmer's, Ocean's, Oceanic, End's, Rustic and Delightful jars are gone from `mods/`
  (nested). Older text in this file, `mod_sides.md` and TODO framework #2 still says "not installed": out of date.
- **Scratch server (2026-10-25):** server halves PASSED; SQ-CDJ-02 (stale jar) FAILED, rewritten, needs a re-run; client
  and biome rows open. Nothing checked in the real client.
- **Built:** startup loader and helpers in patcherMod, NCH namespace, repack and bundle build, Oceanic fishing-net
  overlay, config keys, compartment pattern, QoL-1, 2, 4, 5, 6, food dict (1542 keys), wild crops, village farm crops,
  all pack food and cooking scripts in the CD trees, offline move-diff harness, generator `outRoot`, stale member jar warning.
- **Open framework:** #1 scratch tests, #2 formal install (row and table disagree with the install above), #3 Oceanic
  licence (jar says MIT, Modrinth says All Rights Reserved), #4 and #6 (dict remainder, harness dump half), #9 donor download (pinned).
- **Open members:** #1 bundle the four ports, #6 Ocean's / Oceanic pass, #7 End's pass, #8 mooshroom family into
  moreMooblooms, #10 cutover release.

### 30.2 Delight and food mods

| Mod | Delivered as | Integration status |
|---|---|---|
| Farmer's Delight 1.2.4 | Nested in CD (repack `-02`, staged for 1.3.4 backports) | Member, installed, not run |
| Rustic Delight | Nested in CD (1.7.0 port) | Member, installed, not run |
| Delightful | Nested in CD (3.4.1 plus 3.8.1 items) | Member, installed, not run |
| Ocean's Delight | Nested in CD | Member; only caviar names done; pass planned (members #6) |
| Oceanic Delight | Nested in CD (fishing-net overlay) | Member; licence conflict open; fishing tags and loot still in the pack (members #6) |
| End's Delight | Nested in CD | Member; pass planned (members #7) |
| Veggies Delight | Own port, `backportedVeggies-...-03.jar` in `mods/` | Rows moved into CD trees; jar not bundled (members #1) |
| Vintage Delight | Own port, `backportedVintage-...-04.jar` in `mods/` | Rows moved into CD trees; jar not bundled (members #1) |
| Fungi Delight | Own port, `backportedFungi-...-01.jar` in `mods/` | Tags moved; jar not bundled; mooshroom Java still in patcherMod (members #8) |
| Miner's Delight | Own port, `backportedMiners-...-01.jar` in `mods/` | Cup conversion moved; jar not bundled; Haste overlap waits for MND-40 |
| Ender's Delight 1.2.2 | Third-party jar, stays out (All Rights Reserved) | Rows stay in the pack; some CD `endersdelight/` rows; 1.1.4 evaluation planned (TODO #28) |
| Fruits Delight 0.5.11 | Separate jar | Built (TODO #20): 1.1.3 changes in CD, active only beside Fruits Delight |
| Farmer's Respite 2.0 | Separate jar | Built (TODO #21): 2.1.2 changes, not run |
| Corn Delight 1.0.3 | Separate jar | Built (TODO #24): 1.2.11 changes |
| Display Delight | Own port, `backportedDisplay-...-01.jar` | Produce-family dish blocks built in patcherMod, not run |
| Brewin' and Chewin' | Own port, `backportedBrewin-...-03.jar` | Parked out of CD (section 7); future Creative Brewing |
| Let's Do Vinery | Own port, `backportedVinery-...-04.jar` | Parked, same |
| More Delight 25.07.28 | Separate jar | 26.05.20 changes built 2026-10-25 (TODO #25, more_delight.md), not run: knives retuned, every toast fast, Cheese Toast added; CD folder `moredelight/` (recipes) |
| Abnormals Delight 4.1.2 | Separate jar | 6.0.3 evaluated 2026-10-25, nothing built (abnormals_delight.md); CD folder `abnormals_delight/` (recipes) |
| Alex's Delight 1.4.1 | Separate jar | CD folder `alexsdelight/` (recipes, tags); no further status read |
| Quark Delight 1.0.0 | Separate jar | CD folder `quarkdelight/` (recipes); no further status read |
| Hostile Delight | Separate jar | CD script folder exists; `mod_sides.md` still says "not yet integrated"; stays out of the jar |
| Nether's Delight | Separate third-party jar | Stays out (no pack Java) |
| Chefs Delight 1.0.3 | Separate jar | No CD folder, no integration found |
| Engineers Delight 1.3 | Separate jar | No CD folder, no integration found |
| KubeJSDelight 1.1.2 | Separate jar | No CD folder, no integration found |
| Storage Delight 25.12.09 | Separate jar | 26.10.05 evaluated 2026-10-25, nothing built (storage_delight.md); no CD folder |

Rows for the smaller mods come from folder names, `mod_sides.md` and each mod's writeup header where one exists.

## 30. Food family crafts: `global.CD.food` (2026-10-25, written, not yet adopted)

`kubejs_startup/creative_delight/cd_food.js` (`// priority: 999`, `development/creativeDelight`) is the successor to
`global.foodFamily` ([food_families.md](food_families.md) 4.5, `kubejs_startup/creativeengineering/food_family_recipes.js`).
`var F = global.CD.food(e)` is called inside `global.CD.recipes`, so ids are `creative_delight:<type>/<suffix>`; the rule
values (slice counts, cook xp and ticks, bake xp and time, cookies 12, muffins 4 oven / 2 pot, tarts 2, gold 6 mixer / 5 arc)
are in `global.CD.FOOD` at the top of the file.

- **Calls take (input, output, ...): the input first.** `fruitSlice`, `veggieSlice`, `meatCut`, `fishSlice` (plus a bone meal),
  `pieSlice` (board cut and slices back to the whole by hand), `fruitCook` / `veggieCook` / `meatCook` / `fishCook` / `eggCook`
  (furnace, smoker, campfire), `cakeBake`, `pieBake` (with an optional filling can key), `cookieBake`, `muffinBake`, `tartBake`.
- **Authoritative per food.** A call removes the recipes of the same stations that make the output from that input (cooking
  trio, board), or every other recipe of the output (cakes, pies, cookies; muffins their pot recipes; tarts keep theirs),
  except recipes of `creativeengineering` and `creative_delight`. Calling it on another mod's food therefore replaces that
  mod's version. A second food with the same path gets its namespace in the id suffix.
- **New families beyond 4.5:** heatless grid meals (`mealCraft`, `soupCraft` with bone broth, `saladCraft` with a bowl;
  `_cooked_manual_only` / `_manual_only` keep Create's basin and crafter off them); hand food (`burgerAssemble`,
  `skewerAssemble`, `sandwichAssemble`, `wrapAssemble`: shaped by hand plus one sequenced assembly on the base; `assemble`
  alone takes deploy, `{ fill }` and `{ press }` steps); golden food (`goldenGild`: ring of 8 gold, heated mixer + 6, Arc
  Furnace + 5; `goldenEnchant`: gold block, 250 mB liquid XP, press, 4 loops). Oven parts for one-off recipes: `F.slot`,
  `F.ware`, `F.SWEET`.
- **State:** no script calls `global.CD.food` yet; every member file still calls `global.foodFamily`. Dev-only (no changelog
  bullet) until the callers move; the move must keep every recipe id (SQ-FFH-02).

