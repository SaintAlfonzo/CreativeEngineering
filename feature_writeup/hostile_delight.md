# Hostile Delight: inventory, overlaps and integration plan

Investigation written 2026-10-23; **the plan (section 4) is not built** (deep scan 2026-11-02). Pack-wide rules already touch Hostile items: soups and
stews in jars (1.1.03), the seven cookies in the Oven (1.1.04), JEED `TODO` placeholders for Dioxide and Collapsing
([effects.md](effects.md) F2); CD `hostiledelight/` scripts: sandwiches and burger on any bread (1.1.04), Grilled Ghast / Cooked Rotten Ham / Fried Turtle Egg in furnace, smoker and campfire (cooking trio, unreleased), B2 recipe removed, food types; since 2026-11-02 Prepared Sea Pickle is a removed item (TODO `dishes/pickles-and-ferments #12`, scratch PASSED r1102b SQ-FCB-01); the D-list below is not untouched ground. Hostile Delight 0.2.2 (`hostiledelight`, MCreator, author
"SAMSUNG") is a native 1.19.2 build and is already in `mods/` (added 2026-10-23, `mod_sides.md` row, TODO
`mod-additions #6`), so no port is needed: the work is integration only. "Hostile" in this file is Hostile Delight.
Test prefix `HTD-` (`HD` is too short to grep).

**Scope rule (as for the Fungi and Vintage ports):** integration adds no recipe and no mechanic that is not in
Hostile Delight or defined by a pack rule ([recipes_cooking.md](recipes_cooking.md), [oven.md](oven.md),
[food.md](food.md), [food_families.md](food_families.md)), unless the owner asks for it by name. Upstream bugs are
fixed (2.8).

**Overlap marker:** every Hostile item that duplicates or closely mirrors something in the pack carries
`*overlaps w/ <what>*` in the tables (section 2).

This file has three parts, as asked:

- **Part 1 (section 2):** everything in the mod, from the jar and its decompiled code.
- **Part 2 (section 3):** the overlaps with the pack and with the other four 2026-10-23 add-ons.
- **Part 3 (section 4):** what has to be folded into existing pack systems, in build order.

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Mod inventory (68 items + 1 block, 2 effects, 1 potion with splash / lingering / arrow, 74 recipe files, 3 replaced vanilla mob loot tables, 65 advancements, no tags, no worldgen, no entities) | section 2 | reference, from `Hostile_Delight_0.2.2_1.19.2.jar` |
| Upstream bugs | 2.8 | 12 found; 2 recipes already fail at load in `logs/latest.log` |
| Overlap list | section 3 | analysis; three mobs shared with End's, Ender's, Ocean's Delight and Upgrade Aquatic |
| Integration list in build order | section 4 | planned |
| Decisions for the owner | section 5 | D1 to D16, each with a recommendation and a default |
| Tests | section 8 | HTD-01 to HTD-31 drafted, none run |

- **Jar:** `mods/Hostile_Delight_0.2.2_1.19.2.jar` (`mods.toml` version string says 1.0.0). Decompile with
  `python3 tools/decompile_mod.py <jar> <out dir>` (123 classes; 45 are on-eat procedures).
- **Licence:** Academic Free License v3.0. AFL is not GPL-compatible, so the plan keeps Hostile as an untouched jar and
  does everything in KubeJS and patcherMod (our own code, GPLv3). No fork.
- **Dependencies:** Minecraft 1.19.2 and Farmer's Delight (mandatory; the other two FD entries in `mods.toml` are
  duplicate optional rows). Recipes use FD's `cooking` and `cutting` types, `farmersdelight:raw_pasta`,
  `cooked_rice`, `cabbage`.
- **Pack references (2026-11-02):** CD `kubejs_startup/hostiledelight/hostile_items.js`, `data/creativedelight/kubejs/hostiledelight/` (`recipes.js`, `food_types.js`), item and food dict keys. Earlier (2026-10-23): no KubeJS script, tag or dict named `hostiledelight`. No Display
  Delight compat (Display Delight has Ocean's Delight placeables, not Hostile's). The dumps in `kubejs/exported/`
  (2026-10-20 to 10-22) predate the jar, so `/dumpall` is step 0.
- **Already live in play:** the game has run with it (`logs/latest.log`). Its three loot table overrides are active
  now (2.6), and `SQ-AD-00` ("no `Failed to parse`") will see Hostile's two known roll recipe warnings (2.8 B1).

## 1. Overview

A food mod built around hostile and odd mobs: ghast tentacles, guardian fillets and endermites become meats, and slime,
blaze powder, magma cream, glow ink, rotten flesh and turtle eggs become dishes, cookies, sauces and drinks. Almost
every item gives effects on eating, all through MCreator procedures (code), not food properties. Two own effects:
Dioxide (you cannot hold your breath: air is set to 2 every tick) and Collapsing (unused: nothing applies it). One
potion, Dioxiding (awkward potion + raw guardian fillet), with splash, lingering and arrow forms. One block, the
Candied Slime Block (9 candied slime). Raw meat comes from three replaced vanilla mob loot tables.

## 2. Part 1: everything in the mod

### 2.1 Facts

| Fact | Value |
|---|---|
| Mod id / namespace | `hostiledelight` |
| Items | 68 own items + 1 block item = 69 in the creative tab "Hostile Delight" (`tabhostile_delight`) |
| Blocks | 1 (`candiedslimeblock`) |
| Effects | 2 (`dioxide`, `collapsing`) |
| Potions | 1 (`dioxiding`, 3:00 Dioxide), brewed through Forge `BrewingRecipeRegistry` (not `PotionBrewing`) |
| Recipes | 74: 30 shapeless, 12 shaped, 13 FD cooking pot, 4 FD cutting board, 6 smoking, 5 smelting, 2 campfire, 2 blasting |
| Loot | 3 vanilla tables replaced: `minecraft:entities/ghast`, `guardian`, `endermite` |
| Tags | none |
| Advancements | 65 (the pack drops all advancements at load) |
| Lang | `en_us`, `ko_kr` |
| Tooltips | every item adds one hard-coded English flavour line (`Component.literal`, no lang key) |
| Config | none |

Effect notation below: MCreator passes the amplifier, so "Levitation VI 3 s" is amplifier 5 for 60 ticks. Values are
nutrition / saturation modifier. Stack is the item's own max stack. "Returns" is the container handed back on eating
(hard-coded in `finishUsingItem`); no item sets a crafting remainder (2.8 B7).

### 2.2 Items by line

**Ghast** (drop: 3 rolls x 1-3 tentacles per ghast, 2.6)

| Item (id) | Values | Stack, returns | On eat | Recipe |
|---|---|---|---|---|
| Ghast Tentacle (`ghasttentacle`) | 0 / 1.0, always edible, drink animation | 64 | Levitation VI 3 s | ghast drop |
| Grilled Ghast (`grilledghast`) | 4 / 0.3, meat, always edible | 64 | | smelt / smoke tentacle |
| Ghast Blood (`ghastblood`) | 0 / 0.3, drink | 16, bottle | Levitation II 6 s, Regeneration II 12 s | 2 tentacles + bottle |
| Ghast Brew (`ghastbrew`) | 3 / 0.5, drink | 16, bottle | Instant Health II | blast Ghast Blood |
| Ghast Pudding (`ghastpudding`) | 5 / 1.0, drink | 16, bowl | Regeneration II 200 s | tentacle + ghast tear + egg + bowl. *overlaps w/ the `pudding` family (Neapolitan vanilla pudding, Fruits Delight fig pudding)* |
| Ghastly Ice cream (`ghastbloodicecream`) | 8 / 0.8, rare | 16, bowl | Regeneration II 300 s, Slow Falling II 300 s | 2 tears + snowball + tentacle + Ghast Blood + sugar + bowl. *overlaps w/ Neapolitan / Delightful ice creams (`frozen/ice_cream`)* |
| Ghast Roll (`ghastroll`) | 4 / 0.3 | 64 | | 2 Grilled Ghast + cooked rice -> 2: **recipe fails to load** (B1). *overlaps w/ FD salmon / cod roll (`roll`)* |
| Swelling cookie (`ghastcookie`) | 2 / 0.3 | 64 | Levitation V 3 s | 2 wheat + tentacle -> 8. *overlaps w/ the baking overhaul (cookies oven only)* |

**Guardian** (drop: 2 rolls x 1-3 fillets per guardian, 2.6)

| Item (id) | Values | Stack, returns | On eat | Recipe |
|---|---|---|---|---|
| Guardian Fillet (`guardianfillet`) | 3 / 0.3, meat | 64 | Dioxide 30 s | guardian drop; also the Dioxiding brewing ingredient. *overlaps w/ Ocean's Delight `guardian` (raw, drops on every kill)* |
| Cooked Guardian Fillet (`cookedguardianfillet`) | 8 / 0.7, meat | 64 | | smelt / smoke / campfire. *overlaps w/ Ocean's `cooked_guardian_tail`* |
| Guardian Ratty (`guardianpatty`) | 2 / 0.1, meat | 64 | Dioxide 3 s | cut fillet (knife) -> 2 |
| Guardian Burger (`guardianburger`) | 7 / 0.8 | 64 | | 2 bread + raw patty, shaped. *overlaps w/ the burger rule (shaped, toppings on a cooked patty)* |
| Guardian Sashimi (`guardiansashimi`) | 7 / 0.6, meat | 16, bowl | | bowl + 2 fillets + seagrass |
| Spiked Tartare (`guardiantartar`) | 5 / 0.5, meat, always edible | 16, bowl | Water Breathing II 30 s | bowl + 2 patties + cactus |
| Guardian Ross (`guardianross`) | 9 / 1.0, meat | 16, bowl | | pot: kelp + fillet + cabbage (tooltip promises rice, B8) |
| Guardian Soup (`guardiansoup`) | 4 / 0.6, drink | 16, bowl | | pot: kelp + fillet + carrot. *overlaps w/ Ocean's `guardian_soup` (same dish, same name)* |
| Guardian Sandwich (`guardiansandwich`) | 6 / 0.6 | 64 | | bread + cooked fillet + kelp + sea pickle |
| Guardian Roll (`guardianroll`) | 5 / 0.4 | 64 | | 2 fillets + cooked rice -> 2: **recipe fails to load** (B1). *overlaps w/ Ocean's `elder_guardian_roll`, FD rolls* |
| Gazing cookie (`guardiancookie`) | 2 / 0.3 | 64 | sets air to 20 of 300 (B5) | 2 wheat + fillet -> 8 |

**Endermite** (drop: 1 raw endermite per endermite, always, 2.6)

| Item (id) | Values | Stack, returns | On eat | Recipe |
|---|---|---|---|---|
| Raw Endermite (`rawendermite`) | 1 / 0.1, meat | 64 | Blindness II 3 s | endermite drop. *overlaps w/ End's `raw_ender_mite_meat` and Ender's `mite_crust` (both knife drops)* |
| Cooked Endermite (`cookendermite`) | 4 / 0.5, meat | 64 | | smelt / smoke / campfire. *overlaps w/ End's `dried_endermite_meat`* |
| Carrotmite (`carrotmite`) | 8 / 0.8 | 64, bowl | | 2 cooked endermite + carrot + bowl |
| Magical Butter (`magicalbutter`) | 5 / 1.0, always edible | 64 | **+5 XP levels** | 2 raw endermite + milk bucket + amethyst shard + popped chorus fruit |
| Magical Butter Cookie (`magicbuttercookie`) | 4 / 0.3, always edible | 64 | **+2 XP levels** | 2 wheat + Magical Butter -> 3 |
| Mitey Meal (`miteymeal`) | 10 / 1.3, always edible | 64, bowl | | pot: chorus fruit + raw endermite + Magical Butter |
| Aurora soup (`aurorasoup`) | 6 / 0.5, drink | 16, bowl | removes Bad Omen | pot: amethyst shard + raw endermite |
| Glimmering Chowder (`glimmeringchowder`) | 8 / 1.0, drink | 16, bowl | Regeneration IV 6 s | pot: popped chorus fruit + raw endermite + ender pearl |
| Crawling Cookie (`mitecookie`) | 2 / 0.3 | 64 | removes Levitation | 2 wheat + raw endermite -> 8 |
| Dragon Tonic (`dragontonic`) | 2 / 1.3, always edible, epic, foil | 16, bottle | Blindness II 3 s, Strength IV 30 s, Regeneration III 30 s | pot: chorus fruit + amethyst + Magical Butter, container dragon's breath |
| Ender Substance (`endersubstance`) | 3 / 0.3, always edible | 1, bowl | 8 void damage (ignores armour, can kill), Resistance IV 30 s | Dragon Tonic + ender pearl + Magical Butter + cooked endermite + 2 bowls -> 2 |
| Ender Pearlsicle (`enderpop`) | 4 / 0.3, always edible | 64 | | 2 sugar + 2 ender pearls + 2 ice + stick. *overlaps w/ the popsicle family and the ice cube rule* |
| Trembling cookie (`pearlcookie`) | 2 / 0.3 | 64 | **spawns an endermite** at the eater | 2 wheat + ender pearl -> 8 |

**Slime and gelatin**

| Item (id) | Values | Stack, returns | On eat | Recipe |
|---|---|---|---|---|
| Candied Slime (`candiedslime`) | 1 / 0.1, always edible | 64 | | 2 sugar + slime ball |
| Candied Slime Block (`candiedslimeblock`) | block | 64 | | 9 candied slime, and back. Glass material and sound, opaque, strength 1 / 10 |
| Silme Syrup (`silmesyrup`) | 1 / 0.5, drink | 16, bottle | Slowness III 30 s, Jump Boost V 30 s | 2 candied slime + honey bottle (B6: a bottle is duplicated) |
| Candied Salad (`candiedsalad`) | 6 / 0.5 | 16, bowl | | syrup + 2 ferns + kelp + bowl |
| Slime Stew (`slimestew`) | 4 / 0.3, always edible, drink | 16, bowl | Jump Boost II 50 s | candied slime + syrup + slime ball + bowl |
| Popsiclime (`popsiclime`) | 4 / 0.3 | 64 | Jump Boost IV 6 s | 2 slime balls + 2 candied slime + 2 ice + stick. *overlaps w/ the popsicle family and the ice cube rule* |
| Bone Gelatin (`bonegelatin`) | 2 / 0.3, drink | 64, bottle | | bone + slime ball + bottle. *overlaps w/ Quark Delight sweet gelatine, Nether's Delight magma gelatin* |
| Refined Gelatin (`refinedgelatin`) | 0 / 0.2, always edible | 64 | | smoke Bone Gelatin (bottle lost) |
| Slime Brownie (`slimebrownie`) | 6 / 0.5, always edible | 64 | removes Slowness and Mining Fatigue | candied slime + melon slice + 2 Refined Gelatin. *overlaps w/ the Stimulation status-immunity group (effects.md 2.3)* |
| Glow Gummy (`glowgummy`) | 2 / 0.1, always edible | 64 | Glowing II 10 min | 2 Glowing Sauce + candied slime + sugar -> 2. *overlaps w/ Delightful salmonberry gummy (`candy`)* |

**Blaze and magma**

| Item (id) | Values | Stack, returns | On eat | Recipe |
|---|---|---|---|---|
| Ardent Sauce (`ardentsauce`) | 4 / 0.3, drink | 64, bowl | | pot: 2 blaze powder + bowl |
| Blazing Apple (`blazingapple`) | 5 / 0.6, always edible | 64 | Fire Resistance II 30 s, Absorption III 30 s | 4 blaze powder + apple. *overlaps w/ the golden apple tier (`golden` overlay)* |
| Blazing Ale (`blazingale`) | 4 / 0.2, always edible, drink, fire-proof item | 16, bottle | Fire Resistance II 30 s | pot: 2 blaze powder + sugar + bottle. No alcohol effect despite the name |
| Emitting Cookie (`blazecookie`) | 3 / 0.3 | 64 | Strength III 18 s | 2 wheat + blaze rod -> 8 |
| Magma Brownie (`magmabrownie`) | 6 / 0.6 | 64 | Absorption III 30 s, Fire Resistance II 60 s | blaze powder + carrot + magma cream + Ardent Sauce |
| Vein cookie (`magmacookie`) | 2 / 0.3 | 64 | Fire Resistance II 36 s | 2 wheat + magma cream -> 8 |
| Magma Jelly (`magmajelly`) | 4 / 0.6, always edible | 64 | Fire Resistance II 60 s | 2 magma cream + ghast tear + sugar -> 2. *overlaps w/ Nether's Delight `magma_gelatin` (`jello`)* |
| Sunset Ade (`sunsetade`) | 4 / 0.5, always edible, drink | 64, bottle | Regeneration II 150 s, Luck II 150 s | pot: blaze powder + ghast tear + magma cream + bottle |

**Glow**

| Item (id) | Values | Stack, returns | On eat | Recipe |
|---|---|---|---|---|
| Glowing Sauce (`glowingsauce`) | 2 / 0.3, drink | 64, bowl | Glowing II 9 s | pot: 2 glow ink sacs + bowl |
| Glowing Apple (`glowapple`) | 4 / 0.4, always edible | 64 | Glowing II 15 s, Haste IV 15 s | 2 Glowing Sauce + apple |
| Glowing Bread (`glowbread`) | 7 / 0.6, always edible | 64 | Glowing II 3 s | Glowing Sauce + 3 wheat. *overlaps w/ the one-dough bread rule* |
| Glowing Ink Pasta (`glowpasta`) | 9 / 0.9 | 64, bowl | Glowing II 90 s, Night Vision II 90 s | pot: cod + Glowing Sauce + raw pasta, bowl. *overlaps w/ FD squid ink pasta* |
| Glow Wax Shuffle (`glowwaxshuffle`) | 8 / 0.8, always edible | 16, bowl | Glowing II 30 s, Haste III 30 s, Absorption IV 30 s | 2 honeycomb + 2 glow ink sacs + bowl |
| Cave Skewer (`caveskewer`) | 6 / 0.7, meat | 64, stick | Glowing II 3 s, Hunger VIII 3 s | glow berries + spider eye + rotten flesh + stick. *overlaps w/ the `skewer` family (Ender's crispy skewer, Nether's skewer)* |
| Dim Illuminant (`dimluminary`) | not food, uncommon | 64 | on hitting a mob with it: Glowing II 3 s | cut glow ink sac with shears -> slime ball + Dim Illuminant |

**Rotten flesh and turtle egg**

| Item (id) | Values | Stack, returns | On eat | Recipe |
|---|---|---|---|---|
| Rotten Ham (`rottenham`) | 2 / 0.1, meat | 64 | Hunger II 15 s | cut rotten flesh (knife) -> 2. *overlaps w/ the pack's rotten flesh uses (prepared flesh, dog food, mulch)* |
| Cooked Rotten Ham (`cookedrottenham`) | 4 / 0.3, meat | 64 | | smoke / smelt |
| Rotten Pie (`rottenpie`) | 4 / 0.3 | 64 | | 2 Cooked Rotten Ham + egg + wheat. *overlaps w/ the baking overhaul (pies oven only, on pie crust)* |
| Green Pie (`greenpie`) | 5 / 0.4 | 16 | Nausea II 30 s | moss block + turtle egg + Rotten Ham. Same overlap |
| Frozen Flesh (`frozen_flesh`) | 4 / 0.2, meat | 64 | Slowness IV 4 s | snowball + rotten flesh |
| Fried Turtle Egg (`friedturtleegg`) | 5 / 0.4, meat | 64 | | smelt / smoke a turtle egg. *overlaps w/ the fried egg rule (food.md 3.7)* |
| Turtle Egg Sandwich (`turtleeggsandwich`) | 5 / 0.5 | 64 | | 2 Fried Turtle Eggs + bread. *overlaps w/ FD egg sandwich (same shape)* |
| Suspicious Bacon And Eggs (`suspiciousbaconandaggs`) | 8 / 1.0, meat | 16, bowl | | 2 Fried Turtle Eggs + Cooked Rotten Ham + bowl. *overlaps w/ FD bacon and eggs* |

**Sea and drinks**

| Item (id) | Values | Stack, returns | On eat | Recipe |
|---|---|---|---|---|
| Prepared Sea Pickle (`preparedseapickle`) | 2 / 0.0 | 64 | | cut sea pickle (knife): **output id misspelled, no working recipe** (B2); used by nothing. *overlaps w/ Ocean's braised sea pickle, Oceanic sea pickle slices* |
| Sea Pickle Tea (`seacucumbertea`) | 2 / 0.5, always edible, drink | 16, bottle | Water Breathing II 90 s | pot: 2 sea pickles + sugar + bottle. *overlaps w/ the `drink/tea` family (Farmer's Respite kettle teas)* |
| Marine Cocktale (`marinecocktale`) | 4 / 0.3, always edible, drink | 16, bottle | Dolphin's Grace II 5 s, Water Breathing II 30 s | pot: prismarine shard + kelp + sea pickle + bottle |
| Soda (`soda`) | 2 / 0.3, drink | 64, bottle | | blast a dragon's breath. *overlaps w/ End's `dragon_breath_soda` (same input) and Canned Goods `soda` (same name)* |

### 2.3 Effects and potion

| Effect | Category, colour | What it does | Sources |
|---|---|---|---|
| Dioxide (`hostiledelight:dioxide`) | harmful | Sets air to 2 every tick. On land: the air bar shows empty, no damage. In water: drowning damage within about a second. Water Breathing stops the damage (air never drops below 2) | raw fillet 30 s, patty 3 s, Dioxiding potion 3:00 (splash, lingering, arrow) |
| Collapsing (`hostiledelight:collapsing`) | harmful, `#333333` | A global `LivingHurtEvent` handler: while it lasts, every hit also sets health to health - 3 (B4) | none: nothing applies it; only `/effect` |

Dioxiding: awkward potion (bottle, splash or lingering) + raw Guardian Fillet, through a Forge `IBrewingRecipe`.
Create's mixer, IE and the pack's potion fluid only read `PotionBrewing` mixes ([potions.md](potions.md) 2.3), so it
brews only in a stand today. Lang has the arrow name, so tipped arrows exist through vanilla's potion list.

### 2.4 Recipes (74 files)

All listed in 2.2. By station: grid 42 (cookies 9, two of which are broken roll recipes), FD cooking pot 13, FD cutting
board 4 (knife: patty, rotten ham, prepared sea pickle; shears: Dim Illuminant), furnace / smoker / campfire for the 5
meats and the turtle egg, smoker for Refined Gelatin, blast furnace for Ghast Brew and Soda. Inputs are literal ids
(`minecraft:cod`, `farmersdelight:cabbage`, `farmersdelight:raw_pasta`, `minecraft:egg`, `minecraft:milk_bucket`);
the only tags are `#forge:tools/knives` and `#forge:shears`.

Items with no source: Ghast Roll, Guardian Roll (B1), Prepared Sea Pickle (B2). Items that are only ingredients: Bone
Gelatin -> Refined Gelatin, Ardent / Glowing Sauce, Silme Syrup, Magical Butter, Candied Slime (all also edible).

### 2.5 Mechanics outside food

- Trembling cookie spawns a real endermite (`MOB_SUMMONED`) at the eater. 1 ender pearl -> 8 cookies -> 8 endermites
  -> 8 raw endermite with Hostile's always-drop table.
- Magical Butter and its cookie call `giveExperienceLevels` (5 and 2): one butter (2 raw endermite + milk + amethyst
  + popped chorus) is worth 5 + (3 cookies x 2) = 11 levels.
- Ender Substance deals 8 void damage on eating; it can kill a player below 8 health.
- Dim Illuminant applies Glowing to what it hits (an item `hurtEnemy` hook, no attack damage of its own).
- A server tick queue in the main class (MCreator boilerplate, empty).

### 2.6 Loot (whole-table replacements)

| Table | Vanilla (from the 2026-10-22 dump) | Hostile's replacement |
|---|---|---|
| `minecraft:entities/ghast` | tear 0-1, gunpowder 0-2, each + looting 0-1 | **3 rolls x 1-3 tentacles** (3 to 9), tear 1-2, gunpowder 1-2; no looting |
| `minecraft:entities/guardian` | shard 0-2; cod (cooked if on fire) / crystals / nothing 2:2:1; fish 2.5 % + looting when killed by a player; all + looting | **2 rolls x 1-3 fillets** (2 to 6), 2 rolls x 1-4 shards (2 to 8), 2 rolls x 0-2 crystals; no cod, no fish, no looting, no fire cooking |
| `minecraft:entities/endermite` | empty | 1 raw endermite, always, no knife |

Other drops on the same tables come from loot modifiers, which still apply on top of Hostile's tables: Upgrade
Aquatic's `guardian_spine` (Blueprint table modifier on the guardian table), Create Guardian Beam Defense's capacitor,
Ocean's Delight `guardian` (every kill), End's Delight `raw_ender_mite_meat` (knife, plus a machete set), Ender's
Delight `mite_crust` (knife). Upgrade Aquatic's modifier patches the table json, so its order against Hostile's
override is checked in game (HTD-05).

### 2.7 Tags, advancements, tooltips, tab

- No tags at all: no `#forge:` meat, egg, sauce or storage block tags, no FD tags, no `#forge:storage_blocks/*` for the
  slime block.
- 65 advancements (`data/hostiledelight/advancements`), dropped by the pack at load.
- One literal flavour line per item ("A body of well-roasted endermite. It's surprisingly crispy."). Without a lang
  key, `removeTooltipKey` cannot target them (D11).
- Own creative tab.

### 2.8 Upstream bugs found

| # | Bug | Effect | Fix in the plan |
|---|---|---|---|
| B1 | `grolllrecp` (Ghast Roll) and `grollrecp` (Guardian Roll) are `crafting_shapeless` with `pattern` / `key` | Fail at load (`latest.log` line 30515-30516); both rolls have no recipe | Remove, re-add through `global.NCH` by the roll rule (step 6) |
| B2 | `ppicklerecp` outputs `hostiledelight:preapredseapickle` (typo) | Prepared Sea Pickle has no working recipe | Remove, re-add with the right id. **Built 2026-10-25** (Creative Delight `hostiledelight/recipes.js`, with the campfire recipes the family rule wants; [food_families.md](food_families.md) 4.4, FF-02, FF-04) |
| B3 | Lang key `item.hostile_delight.blazingale` (wrong namespace) beside the right one | Harmless duplicate | none |
| B4 | Collapsing's handler calls `setHealth(health - 3)` on any hurt event | Bypasses armour, absorption and the death path (no damage source; `die()` is not called if it reaches 0) | patcherMod: cancel the handler, or route it through a real damage source (D9) |
| B5 | Gazing cookie sets air to 20 (of 300) | Under water it empties the air bar instead of filling it (the tooltip reads as a breathing food) | Set to max air (D9) |
| B6 | Silme Syrup: the honey bottle's crafting remainder (glass bottle) comes back and the syrup also returns a bottle when drunk | One glass bottle gained per syrup | Recipe takes a glass bottle too, or drop the remainder (step 6) |
| B7 | No item sets a crafting remainder | Bowls and bottles vanish when Glowing / Ardent Sauce (4 recipes), Silme Syrup (2), Ghast Blood (1), Dragon Tonic (1), Bone Gelatin (smoker) are used as ingredients | Crafting remainders in `ItemEvents.modification` (step 6) |
| B8 | Guardian Ross tooltip says "Served with cabbage and rice"; the recipe has no rice | Text only | Goes with D11 |
| B9 | Names: "Silme Syrup", "Guardian Ratty", "Marine Cocktale", "Guardian Ross", lower case "Aurora soup", "Ghastly Ice cream", "Trembling cookie", "Swelling cookie", "Gazing cookie", "Vein cookie"; id `suspiciousbaconandaggs` | Names audit | Step 14 |
| B10 | Ghast and guardian replacements drop looting, the guardian's cod and fish pool and fire cooking | Vanilla drops lost while the jar is in (live now) | Step 3 |
| B11 | Effects live in 42 on-eat procedures, not food properties | AppleSkin, JEED food lines, the food catalog's effect column, the food tuning and the can system cannot see or change them | Step 9 (D10) |
| B12 | Ghast Brew adds Instant Health II as a 3 s effect (`addEffect`); an instant effect with a duration applies every tick | 8 health per tick for 60 ticks: a full heal, not Instant Health II | Keep in code, apply once (step 9) |

## 3. Part 2: overlap with what the pack already has

Batch sibling (not installed): [carnivore.md](archive/carnivore.md) section 2 lists where Carnivore's mob meats overlap this mod's drops.

The pack's item names (2026-10-20 dump) were searched for every Hostile theme; the four other 2026-10-23 jars were read
from their item models (they are newer than the dump). Hostile's item ids have no underscores, so no id clashes
literally; every overlap is a concept.

### 3.1 Mob drops (the main overlap)

| Mob | Drops today with the five new jars | Problem | Proposal |
|---|---|---|---|
| Endermite | Hostile `rawendermite` (always), End's `raw_ender_mite_meat` (knife; machete set for Nether's Delight), Ender's `mite_crust` (knife) | Three raw items from one small mob; Hostile's ignores the FD knife convention. Ender's `endermite_stew` and the pack's heatless stew route (2 mite crusts) already use the crust | One raw endermite meat (D2); Ender's crust stays (it is the shell, used by its stew) |
| Guardian | Hostile 2-6 fillets (always), Ocean's whole `guardian` (always), Upgrade Aquatic spine, CGBD capacitor; vanilla drops replaced (2.6) | Two raw guardian meats on every kill, plus vanilla's cod and fish gone | Ocean's whole guardian stays the drop and is cut into Hostile fillets (D3); vanilla table restored |
| Ghast | Hostile 3-9 tentacles | No other ghast food; only the rate is out of line (a ghast gives more meat than a cow) | Fewer tentacles, looting kept (D4) |
| Elder guardian | Ocean's slices / eye (Oceanic), Armor Trims, CGBD, Upgrade Aquatic | Hostile does not touch it | none |

### 3.2 Dish overlaps

| Hostile item | Overlap | Proposal |
|---|---|---|
| Guardian Soup | Ocean's `guardian_soup` (same dish; Display Delight has `od_bowl_of_guardian_soup`) | Item merge, Ocean's survives (D3) |
| Guardian Roll, Ghast Roll | FD salmon / cod roll, Ocean's `elder_guardian_roll`, Oceanic `sea_pickle_roll` | Pack roll rule (step 6) |
| Guardian Burger | Every burger is shaped with an assembly, on a cooked patty | Burger rule, on a cooked patty (D13) |
| Turtle Egg Sandwich | FD egg sandwich (bread + 2 fried eggs) | Sandwich rule; keep both items |
| Suspicious Bacon And Eggs | FD bacon and eggs (shapeless + assembly) | Same rule; keep both |
| Fried Turtle Egg | Pack fried egg rule (smoking, cooking oil, NCH `smoking` xp / time); `#forge:eggs` migration TODO (food.md 3.8) | Same routes; not in `#forge:eggs` (turtle eggs are a breeding block) |
| 9 cookies | Baking overhaul: every cookie is oven only, cookie dough + flavour (oven.md) | Rebuild on cookie dough (D12) |
| Rotten Pie, Green Pie | Same overhaul: pies on pie crust, oven only | Rebuild on pie crust (D12) |
| Slime Brownie, Magma Brownie | No brownie in the pack; `bake/slice` or `cake` family | Oven like the other bakes (D12) |
| Glowing Bread | One dough rule (all bread from dough) | Dough + Glowing Sauce in the oven (D12) |
| Ender Pearlsicle, Popsiclime | Every ice food takes `#forge:ice_cubes` (2026-10-22) | Same tag in place of ice |
| Ghastly Ice cream | Neapolitan and Delightful ice creams (`frozen/ice_cream`, TODO `sweets #4`) | Join the family; snowball kept |
| Glowing Ink Pasta | FD squid ink pasta (pack heated mixing from ink fluid) | Heatless rule; no ink fluid route (glow ink has no fluid) |
| Magma Jelly | Nether's Delight `magma_gelatin` | Keep both; family `jello` |
| Bone Gelatin, Refined Gelatin | Quark Delight `bucket_of_sweet_gelatine`, Nether's magma gelatin | Keep; `ingredient` family |
| Soda | End's `dragon_breath_soda` (dragon's breath too), Canned Goods `soda` ("Soda") | Keep, rename to avoid a second "Soda" (D15) |
| Sea Pickle Tea, Marine Cocktale | Farmer's Respite kettle teas; `drink/tea`, `drink/other` | Families only; no kettle route (scope rule) |
| Prepared Sea Pickle | Ocean's `braised_sea_pickle`, Oceanic `sea_pickle_slices` | Fix B2; it has no use, so a candidate for removal (D14) |
| Blazing Ale | Name says alcohol; no Tipsy, no `#creativedelight:food_type/drink/alcohol` | `drink/other`, not alcohol (D15) |
| Blazing Apple, Glowing Apple | Golden apple tier (25 golden foods, `golden` overlay) | `special`, not golden (no gold) |
| Cave Skewer | Ender's crispy skewer, Nether's skewer (`skewer`) | Family only |
| Rotten Ham line, Frozen Flesh | Pack rotten flesh uses: creosote -> prepared flesh, dog food, mulch, cleric trade | Keep; `hazard` (raw) and `meat` (cooked) |
| Candied Slime Block | No candy storage block; storage audit ([storage_audit.md](storage_audit.md)) | `#forge:storage_blocks/candied_slime`, hoe or hand mining like the other soft storage blocks |

### 3.3 System overlaps

| System | Overlap | Proposal |
|---|---|---|
| Item definitions | Nothing in `item_dict.js` | 69 keys + tags (step 1) |
| Mob loot | 3 whole-table overrides against modifiers from 4 mods; the pack's loot framework edits chests, not mob drops (loot.md 4.1) | Restore vanilla tables in `kubejs/data`, add Hostile drops as loot modifiers (step 3) |
| Food values and code effects | 42 items apply effects or actions in code (B11); food.md decision D4 covers exactly this | Move the plain effects into food properties, keep the rest in code (D10) |
| Potions | Dioxiding only brews in a stand (2.3) | `PotionBrewing` mix, then mixers, bottling, IE, potion buckets, arrows (step 8) |
| Effects | 2 new effects; Collapsing has no source; Slime Brownie and Mite cookie are cures | effects.md rows, JEED text, overlap row (step 8) |
| XP economy | The pack rebuilt XP around Create's experience block (recipes_magic.md); 11 levels per butter is a shortcut | D6 |
| Stack sizes | 8 bowl foods at 64 (Carrotmite, Mitey Meal, Glowing Ink Pasta, the two sauces...), drinks at 64 | Policy rules (step 11) |
| Tooltips | 69 literal lines; pack lines for effects | D11 |
| Names | B9 typos | Step 14 |
| Mob food | 15 meat-flagged items (wolves eat them), Raw Endermite and Rotten Ham among them | Mob audit (step 15) |

### 3.4 Unusual Delight

Superseded 2026-10-30 by the full survey in [unusual_delight.md](unusual_delight.md) (35 items overlap Hostile).

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

Recommendation (D1): integrate Hostile now. A later Unusual
Delight port would merge onto Hostile's items, or skip the shared ones; both are cheaper than holding 69 live items
with broken loot in the pack meanwhile. TODO row: `mod-additions #8` (integrate or hold for Unusual Delight).

## 4. Part 3: what folds into existing systems (build order)

0. **Dumps.** `/dumpall` with the jar in (it is), then the generators in the usual order (memory
   `dumpall-refresh-workflow`). Gives `display_names.json`, `food/`, `recipes/`, `loot_tables/` rows for Hostile.
1. **Ids and tags in one place.** Every item in `item_dict.js` (keys `hd_*` or per line, as the Fungi keys),
   the block in the block dict. Tags through `global.tags`: `#forge:raw_endermite` / the survivor's tag (D2),
   `#forge:raw_guardian`, `#forge:cooked_guardian`, the pack's `#creativedelight:food_type/*` through
   `food_types.json` (step 9), `#forge:storage_blocks/candied_slime`, `#forge:slimeballs` users checked,
   `#forge:ice_cubes` users.
2. **Upstream recipe removals.** Remove B1 and B2 by id; every other Hostile recipe is either kept, rebuilt by a rule
   (step 6) or removed (D12, D13). Literal inputs move to tags (`#forge:raw_fishes/cod` or the pack's raw fish type
   tag, `#forge:vegetables/cabbage`, `#forge:pasta/raw` if present, `#forge:eggs`, `#forge:milk` per the honey and
   milk decision in alcohol.md 4.1).
3. **Mob loot.** Pack copies of vanilla `entities/ghast` and `entities/guardian` in `kubejs/data/minecraft/loot_tables`
   override Hostile's (kubejs data wins over mod jars; HTD-04 checks). `entities/endermite` back to empty. Hostile's
   drops as loot modifiers (FD `add_item` style, as End's and Ocean's ship) with D2 to D4: fillets from cutting Ocean's
   guardian (FD cutting board, D3), tentacles 0-2 + looting (D4), raw endermite by D2. Upgrade Aquatic's spine and the
   other modifiers then sit on the vanilla table again. Rows in [loot.md](loot.md) 2.6 (override) and 2.4.
4. **Item merges** ([item_merges.md](item_merges.md) section 1): Hostile `guardiansoup` -> Ocean's `guardian_soup`
   (D3); raw endermite per D2. Purge, survivor takes recipes, tags, loot, held copies.
5. **Potion.** Dioxiding as `PotionBrewing.addMix` (patcherMod, as Fungi's Burrowing) for bottle; splash
   and lingering follow by vanilla's container mixes; drop Hostile's `IBrewingRecipe` (patcherMod mixin on
   `DioxidingpotrecpBrewingRecipe.init`). Then [potions.md](potions.md) 2.3 row, `PotionMixDedup` check, Create / IE
   mixers, bottling and the potion fluid, [potion_buckets.md](potion_buckets.md), `tipped_arrows.js`.
6. **Recipes by the pack rules**, all through `global.NCH`:
   - **Rolls** (Ghast, Guardian): shapeless x2 + assembly x3, as FD salmon / cod roll.
   - **Burger** (D13): shaped on the cooked patty (bread top and bottom), plus an assembly; Hostile's raw-patty recipe
     removed.
   - **Sandwiches** (Turtle Egg, Guardian): shapeless + assembly as FD egg sandwich / grilled salmon.
   - **Bacon and eggs** (Suspicious): shapeless + assembly as FD bacon and eggs.
   - **Heatless meal rule** for the 6 pot meals (Guardian Soup if kept, Guardian Ross, Aurora soup, Glimmering Chowder,
     Mitey Meal, Glowing Ink Pasta): a cooked-input grid recipe, one more main ingredient, id `_manual_only`; soups
     with bone broth. Hostile's grid meals (Sashimi, Tartare, Carrotmite, Candied Salad, Slime Stew, Glow Wax Shuffle,
     Ghast Pudding, Ender Substance) re-id'd `_manual_only` so the unheated basin skips them.
   - **Baking** (D12): 9 cookies on cookie dough + flavour in the oven; Rotten Pie and Green Pie on pie crust; the two
     brownies and Glowing Bread in the oven on dough. Hostile's grid recipes added to `baking.js` `REMOVED_OUTPUTS`.
   - **Ice foods**: Ender Pearlsicle and Popsiclime take `#forge:ice_cubes`.
   - **Fried Turtle Egg**: the fried egg routes (smoking with NCH xp / time, cooking oil pan route as the fried egg).
   - **Meats**: smelting / smoking / campfire through NCH with the pack's meat times.
   - **Containers**: crafting remainders for Glowing Sauce, Ardent Sauce (bowl), Silme Syrup, Ghast Blood, Dragon Tonic,
     Bone Gelatin (bottle) in `ItemEvents.modification` (B7); Silme Syrup recipe without the honey bottle's extra
     bottle (B6).
   - **Machine mirrors**: Create Central Kitchen turns pot recipes into heated mixing by itself; cutting board recipes
     get Slice and Dice mirrors as every cutting recipe.
7. **Gelatin, sauces, syrup as ingredients**: tags `#forge:gelatin` (Bone / Refined), so a later recipe can take any
   gelatin; no new uses (scope rule).
8. **Effects.** Dioxide and Collapsing: rows in [effects.md](effects.md) 2.1, JEED text in the pack lang,
   `jeed_providers.js` source for Dioxide (fillet, patty, potion), Collapsing marked "no source" (D9). Slime Brownie and
   Crawling Cookie as cures in the 2.3 overlap table. Category audit: both harmful (correct).
9. **Food types, values, code effects.** `food_types.json` rows for 68 foods; `/dumpfood`, `food_catalog.py`, tune
   against the type bands (Mitey Meal 10 / 1.3 is above every `meal` median; Ghast Tentacle 0 / 1.0). Code effects
   (D10): the 33 items whose procedure only adds lasting effects get those effects as food properties and their
   procedure cancelled by a patcherMod mixin; the 9 others keep their procedure (XP, endermite spawn, void damage, air,
   effect removal, Ghast Brew's heal applied once, B12), each with a pack tooltip line. Values then match AppleSkin, JEED and cans.
10. **Food families.** `tools/food_families.py` rules so every Hostile food lands in a family (`--check` fails closed on
    unsorted): raw meats -> `meat/raw` or `hazard` (Ghast Tentacle, Raw Endermite, Rotten Ham, Frozen Flesh), cooked ->
    `meat/cooked`, Guardian line -> `fish` (it is a fish-like mob) or `meat` (owner call inside D1 of food_families.md),
    XP / spawn / void foods -> `special`, the rest by form (cookie, pie, roll, sandwich, soup, salad, drink, ...).
11. **Stack sizes.** `tools/gen_stack_sizes.py` rules: bowl meals 32, drinks 32, special bottles 16 (Dragon Tonic),
    ingredients and handheld 64; Ender Substance stays 1 or goes to 16 (D16).
12. **Cans.** `gen_can_types.py`: meats and the meals by type loops; Hostile in `AWAIT_DUMP` until step 0.
13. **Compost.** `compost_values.js` rows (`gen_compost_values.py --add-new` after the dump): raw meats and gelatin as the
    pack's meat and slime values.
14. **Names and lang.** Fix B9 in the pack lang (`kubejs/assets/hostiledelight/lang/en_us.json`) or
    `namechanges.js` as the pack does: Slime Syrup, Guardian Patty, Marine Cocktail, Guardian Rosti (or as the owner
    names it), title case for the lower-case names; Soda per D15; names audit ([names.md](names.md)).
15. **Mob and block audit.** Wolves and the pack's dog food rules for the 15 meat items
    ([mob_block_tweaks.md](mob_block_tweaks.md)); Candied Slime Block: BlockRunner tier, `#minecraft:mineable/hoe`
    (soft storage block rule in `storageblocktags.js`), WTHIT icon, Item Physic.
16. **Tooltips** (D11) and **JEI.** JEI info pages (`client_scripts/jei_info.js`, as Fungi) for Trembling cookie
    (spawns an endermite), Magical Butter (XP, if kept), Ender Substance (void damage), Dioxide; JEI coverage
    ([jei_coverage.md](jei_coverage.md)); Collapsing hidden from JEI effect lists if it stays unsourced.
17. **Placeables.** None expected (`/dumpplaceable`); no Display Delight compat to add (scope rule).
18. **Trades, chests, worldgen.** None (Hostile has none; scope rule).
19. **Release.** `mod_sides.md` note "integrated", changelog bullets under the food heading (new foods, mob drops, the
    loot fix), TODO `mod-additions #6`, `SQ-AD-00` note, `plan_*.md` row.

Order matters in three places: step 0 before 9 to 13 (they read dumps); step 3 before the loot dump check; step 4
before step 6 (recipes name the survivors).

## 5. Decisions for the owner

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | Integrate now, or hold for an Unusual Delight port? | Integrate now, KubeJS + patcherMod only (AFL licence, no fork). An Unusual port later merges onto these items | integrate now |
| D2 | Which raw endermite item does an endermite drop? | One: End's `raw_ender_mite_meat` survives (knife drop, machete set, own tag), Hostile `rawendermite` merged into it; Hostile Cooked Endermite stays as its cooked form; Ender's `mite_crust` kept (knife, separate part). Decide together with `mod-additions #4` | End's survives, knife only |
| D3 | Guardian meat | Ocean's whole `guardian` stays the drop; cutting board: guardian -> 3 Hostile fillets; Hostile's always-drop removed. Hostile Guardian Soup merged into Ocean's `guardian_soup` | as recommended |
| D4 | Ghast tentacle rate | 0-2 + looting 0-1, no knife (ghasts are shot); tear and gunpowder back to vanilla | 0-2 + looting |
| D5 | Hostile's three whole-table replacements | Undo all three (vanilla restored in `kubejs/data`); Hostile drops as modifiers | undo |
| D6 | XP foods (Magical Butter +5 levels, cookie +2) | Remove the XP (food only); or small XP points (for example 10 and 3 points, not levels) | remove XP |
| D7 | Trembling cookie spawns an endermite | Keep (it is the joke); it feeds the endermite line, 8 mites per pearl | keep |
| D8 | Ender Substance 8 void damage | Keep as the cost of Resistance IV; JEI page and tooltip warn | keep |
| D9 | Collapsing and the Gazing cookie | Collapsing: cancel the `setHealth` handler and leave the effect unsourced (hidden), or make it 3 magic damage per hit if a source is ever wanted. Gazing cookie: fill air (B5) | cancel handler; fill air |
| D10 | Code-side effects | Food properties for the 33 plain-effect foods (procedures cancelled), code kept for the 9 others (food.md D4) | as recommended |
| D11 | 69 literal flavour lines | Remove them (pack style, as Vinery's): needs a literal-text helper beside `removeTooltipKey`, or a patcherMod mixin cancelling Hostile's `appendHoverText` | remove |
| D12 | Grid cookies, pies, brownies, bread vs the baking overhaul | Rebuild on the pack bases, oven only | rebuild |
| D13 | Guardian Burger on a raw patty | The burger takes Cooked Guardian Fillet in place of the raw patty (a cooked patty would be a new item) | cooked fillet |
| D14 | Prepared Sea Pickle (no use anywhere) | Fix the recipe and give it no use (scope rule), or remove the item through `removed_items.js` | fix, keep |
| D15 | Names that collide: Soda, Blazing Ale | Soda -> "Dragon Soda" (Canned Goods has "Soda"; End's has "Dragon Breath Soda", so check that too); Blazing Ale stays a non-alcohol drink | rename Soda only |
| D16 | Ender Substance stack 1 | 16 by the special bottle policy, or keep 1 | keep 1 |

### 5.1 Owner notes (scratchpad 2026-11-04)

Translated into the `## Hostile Delight` table of [TODO](todo/TODO.md) (`largecontent/mod-additions #6`, `#43` to `#51`).
Nothing below is built yet (checked 2026-11-04: only `hostiledelight/recipes.js`, the generated food types, `hostile_items.js`
removing the prepared sea pickle, the cookie and pie rebuilds in CD `baking.js` and the Cave Skewer line in
`barbequesdelight/skewers.js` exist).

Settles these decisions:

| D | Owner note | Effect on the plan |
|---|---|---|
| D3 | Hostile's Guardian Soup removed | Ocean's Guardian Soup is the only one, on a new recipe (`dishes/ocean #12`) |
| D6 | Magical Butter removed | XP question left only for any XP cookie that survives (none on the list below) |
| D7 | Trembling Cookie removed | Reverses "keep"; the endermite-spawn joke goes |
| D9 | Gazing Cookie removed | B5 air fill not needed |
| D11 | Native tooltips removed | As recommended |
| D12 | Magma Brownie baking only, Slime Brownie oven only | Both marked for brownie unification (`baking/baked-goods #18`) |
| D13 | Guardian Burger / Sandwich removed | Moot |
| D15 | Soda and Blazing Ale removed | Rename moot |

Notes, as rows:

- **Tooltips and effects (`#43`):** remove the mod's own tooltips; every food shows its effects as the pack's eaten-effect line.
- **Removed (`#44`):** Magma Jelly, Frozen Flesh, every ghast food and drop (Ghast Blood, Ghast Brew included), Guardian Soup,
  Guardian Ratty, Guardian Burger and Sandwich, Gazing, Vein, Trembling, Swelling and Crawling Cookies, Magical Butter, Soda,
  Blazing Ale, Marine Cocktail, Dragon Tonic, Sunset Ade. CD `baking.js` lines 84-86 still rebuild several of these cookies
  on dough (`cooking/oven #6`), and `items/jars #3` lists Hostile cookies for jars: both shrink. Guardian Ratty is on this list
  and also has a recipe note (`#50`): open question.
- **Blazing line (`#45`):** Blazing Apple shows its effects and is marked for effect balancing (Fire Resistance). Ardent Sauce
  renamed Blazing Sauce: Apple Sauce jar, 2 Blaze Powder and a bowl, with the Blazing Apple's Fire Resistance effects, a more
  blaze-powder-efficient route than the apple. Emitting Cookie renamed Blazing Cookie, Blaze Rod swapped for Blaze Powder
  (today `baking.js:84` rebuilds it on dough with a rod).
- **Brownies (`#46`):** Magma Brownie baking only, carrot swapped for any nether fungus, effects shown and balanced. Slime
  Brownie oven only, recipe kept. Both join the brownie unification.
- **Rotten foods (`#47`, `#48`):** Rotten Ham and Cooked Rotten Ham leave the normal meat tags for new raw and cooked rotten
  meat tags; the raw one counts as rotten flesh, the cooked one as meat in dog food (`dishes/meals #8`). Every rotten food and
  the Green Pie get the hunger food type with a 20% chance of Poison II for 5 s. Rotten Pie, Green Pie and Suspicious Bacon and
  Eggs are not saltable (today they are in the salt tag, `food/seasoning #4`).
- **Assembly (`#49`):** Suspicious Bacon and Eggs follows the bacon and eggs sequenced assembly.
- **Guardian dishes (`#50`):** Guardian Roll gets a recipe in line with the roll family; Guardian Ratty takes raw guardian fillet
  for every meat input; Guardian Ross and Sashimi keep their recipes, a more complex and rewarding craft later.
- **Recipe tweaks (`#51`):** Spiked Tartare takes any cactus steak; Slime Syrup cooking gives 2x; Candied Salad swaps ferns for
  any cabbage-leaf-like green (dandelion included).
- **Elsewhere:** Refined Gelatin renamed Gelatin, new texture (`dishes/ingredients #30`); Cave Skewer in the skewer family (built
  2026-11-04, `dishes/meals #50`), merge with Miner's Delight (`dishes/meals #58`); guardian shell, soup and fugu notes in
  [ocean_delight.md](ocean_delight.md) 9.

## 6. File map (planned)

| File | Change |
|---|---|
| `kubejs/startup_scripts/_lib/item_dict.js` | 69 keys, tags |
| `kubejs/startup_scripts/_lib/block_dict` (as named) | slime block |
| `kubejs/startup_scripts/changes/` (stack sizes generated, remainders, food properties) | steps 6, 9, 11 |
| `kubejs/server_scripts/cooking/cooking.js`, `baking.js` | steps 2, 6 |
| `kubejs/server_scripts/tags/cookingtags.js`, `storageblocktags.js` | steps 1, 15 |
| `kubejs/data/minecraft/loot_tables/entities/{ghast,guardian,endermite}.json`, `kubejs/data/creativeengineering/loot_modifiers/*` + `forge/loot_modifiers/global_loot_modifiers.json` | step 3 |
| `kubejs/server_scripts/equipment/tipped_arrows.js`, `engineering/potion_buckets.js`, `potion_bottling.js`, `other/jeed_providers.js` | steps 5, 8 |
| `development/creativeCommon`: Dioxiding mix, `IBrewingRecipe` off, Collapsing handler, Gazing cookie air, procedure cancels (D10), tooltip cancel (D11) | steps 5, 8, 9, 16 |
| `kubejs/generation_scripts/food/food_types.json`, `tools/food_families.py`, `tools/gen_stack_sizes.py`, `gen_can_types.py`, `compost_values.js` | steps 9 to 13 |
| `kubejs/assets/hostiledelight/lang/en_us.json` or `client_scripts/namechanges.js`, `client_scripts/jei_info.js`, tooltips | steps 14, 16 |
| Docs: this file, effects.md, potions.md, loot.md, recipes_cooking.md, oven.md, food.md, item_merges.md, stack_sizes.md, TODO, README, changelog | each step |

## 7. Offline checks (once built)

- `/dumpall` and `tools/food_families.py --check` (no unsorted Hostile food).
- Recipe dump: no `hostiledelight:grollrecp`, `grolllrecp`, `ppicklerecp`; every Hostile output has at least one
  recipe or a loot source except the drops.
- Loot dump: `minecraft:entities/ghast` and `guardian` equal the 2026-10-22 vanilla rows; `endermite` empty.
- `python3 tools/changelog_lint.py`.
- patcherMod: headless harness for the Dioxiding mix (memory `patchermod-headless-smoke-test`).

## 8. In-game test suite (drafted)

| ID | Test | Pass |
|---|---|---|
| HTD-01 | Boot | No `Failed to parse` naming `hostiledelight`; no unknown item or tag |
| HTD-02 | JEI: Ghast Roll, Guardian Roll | roll recipes x2 and assembly x3 shown |
| HTD-03 | JEI: Prepared Sea Pickle | cutting board recipe gives it |
| HTD-04 | Kill 10 ghasts (no looting) | tears 0-1, gunpowder 0-2, tentacles 0-2 each |
| HTD-05 | Kill 10 guardians | vanilla drops (shards, cod / crystals) + Ocean's guardian + Upgrade Aquatic spine; no Hostile fillet |
| HTD-06 | Cut Ocean's guardian on a cutting board | 3 Guardian Fillets |
| HTD-07 | Kill endermites with and without a knife | per D2: survivor meat only with a knife; mite crust with a knife |
| HTD-08 | Brew awkward + fillet in a stand | Potion of Dioxiding; splash and lingering by gunpowder / dragon's breath |
| HTD-09 | Dioxiding in Create mixer and IE mixer | potion fluid of Dioxiding |
| HTD-10 | Arrow of Dioxiding | craftable with lingering Dioxiding, applies Dioxide |
| HTD-11 | Drink Dioxiding, stand on land, then swim | land: no damage; water: drowning; with Water Breathing: no damage |
| HTD-12 | `/effect give @s hostiledelight:collapsing`, take a hit | per D9 (no extra 3 health lost) |
| HTD-13 | Eat Gazing cookie under water with low air | air bar full |
| HTD-14 | Craft Silme Syrup | glass bottle count unchanged overall |
| HTD-15 | Use Glowing Sauce in Glowing Apple | bowl returned |
| HTD-16 | Eat Magical Butter | per D6 |
| HTD-17 | Eat Trembling cookie | an endermite appears |
| HTD-18 | Eat Ender Substance at 20 health | 8 damage, Resistance IV 30 s |
| HTD-19 | AppleSkin / JEED on Blazing Apple, Glow Wax Shuffle | the effects show (D10) |
| HTD-20 | Eat Blazing Apple | Fire Resistance and Absorption apply once each (no double from procedure and properties) |
| HTD-21 | Tooltips on 5 Hostile items | no flavour line (D11); effect lines present |
| HTD-22 | Cookies in JEI | oven only, on cookie dough; no grid recipe |
| HTD-23 | Rotten / Green Pie | oven on pie crust |
| HTD-24 | Pearlsicle, Popsiclime | accept ice cubes and ice |
| HTD-25 | Guardian Burger | shaped on cooked fillet + assembly |
| HTD-26 | Mitey Meal, Glowing Ink Pasta | pot + `_manual_only` grid; unheated basin does not make the grid version |
| HTD-27 | Stack sizes of Carrotmite, Sunset Ade, Dragon Tonic | 32 / 32 / 16 |
| HTD-28 | Candied Slime Block | hoe fastest, drops itself, 9 <-> 1 |
| HTD-29 | Names: Silme Syrup, Guardian Ratty, Marine Cocktale, Soda | fixed names |
| HTD-30 | Guardian Soup | Ocean's item only; Hostile's merged (held copies converted) |
| HTD-31 | Drink Ghast Brew at 4 health | heals 8 once, not to full (B12) |

## 9. History

- 2026-10-23: investigation and plan written (inventory from the jar and decompiled code, overlap search against the
  2026-10-20 name dump and the four other 2026-10-23 jars, 12 upstream bugs, D1 to D16; later the same day: counts corrected, B12 added). Nothing built.
- 2026-10-31: scratchpad notes translated into TODO rows (ghast foods removal added to `largecontent/mod-additions #6`). No code changed.
- 2026-11-04: owner scratchpad notes added as 5.1 (settles D3, D6, D7, D9, D11, D12, D13, D15) and TODO rows `largecontent/mod-additions #43` to `#51` in a new `## Hostile Delight` table. No code changed.
