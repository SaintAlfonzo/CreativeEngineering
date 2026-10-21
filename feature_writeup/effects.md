# Effects: every mob effect in the pack, by mod, with its brewing, arrows, JEI and food coverage

## 0. At a glance

| Part | Status |
|---|---|
| Effect table (2.1): 140 effects from vanilla and 25 mods, with brewing, tipped arrow, superset, JEI potion fluid, JEI description, food sources, subset effects, overlaps, brief and detailed descriptions | reference, written 2026-10-19 from code (decompiled jars and the two ports), the `/dumppotions` and `/dumpfood` exports, and every lang table the client loads |
| `tools/effect_audit.py`: offline check of names, JEED descriptions, placeholders and potion / arrow names | BUILT 2026-10-19 |
| Findings F1 to F13 (section 4): 3 effects or arrows with no name, 11 effects with no real description, 11 wrong or incomplete descriptions, 12 total-overlap groups, a duplicate Luck brew | F3, F4, F7, F11, F13 done 2026-10-20; F2 down to 10 `TODO`s (kept, your call); F5 and F12 need your call |
| JEI item lists for every effect (3, [jeed_providers.js](../kubejs/server_scripts/other/jeed_providers.js)): mobs, blocks, gear, code-applied food, brews, wines, potion carriers | BUILT 2026-10-20, not seen in game (EF-15, EF-16) |
| Description fixes (3): 16 wrong, partial or misspelt JEED texts replaced, Travel Fatigue ported from Effect Insights, 7 stale placeholders gone | BUILT 2026-10-20, not seen in game (EF-17) |
| Category audit (2.4): all 140 effects checked for red / blue; 6 overridden in CEConfig `[effects]` | BUILT 2026-10-20 (patcherMod 1.1.01, rebuilt and installed), not seen in game (EF-18) |

- **Pending:** EF-01 to EF-19 have never been run. The 2026-10-20 work (providers, description fixes, category
  overrides) is built but not seen in game. The exports are from 2026-10-18 18:30, before the Vinery and Brewin ports
  went into `mods/` (2026-10-19 00:50), so rerun `/dumppotions` and `/dumpfood` on the next boot (F10).
- **Files to open:** the table below; [potions.md](potions.md) for how potions are brewed on every machine;
  [armor_effects.md](armor_effects.md) for effects given by gear; [alcohol.md](alcohol.md) for the Brewin and Vinery
  drinks.
- **Knobs:** CEConfig `[effects] categoryOverrides` (2.4). JEED's `config/jeed-client.toml` (`hidden_effects`), the
  Vinery wine table (`config/vinery-common.toml` `effects`), Fruits Delight aura ranges
  (`config/fruitsdelight-common.toml`).
- **How to look:** JEI → the effect list (JEED adds one entry per effect) → click for its description and the items
  that give it. `python3 tools/effect_audit.py` for the name, description and JEI-source gaps.

## 1. Overview

A mob effect is the timed status (Speed, Poison, Tipsy...) shown in the inventory and HUD. Effects reach the player
through:

- **Potions:** a potion is a registry entry carrying one or more effects. Every potion is drinkable, splash, lingering
  and a tipped arrow (vanilla's crafting recipe works for any potion), and in this pack also a `create:potion` fluid on
  every machine ([potions.md](potions.md)). Tipped arrows give 1/8 of the duration.
- **Food and drink:** `FoodProperties` effects (most foods), or code in the item (wines, Builder's Tea, chocolate).
- **Mobs, blocks, gear:** stings, auras, beacons, conduits, armor set bonuses.

JEI shows effects through Just Enough Effect Descriptions (JEED): one ingredient per effect, with a description from
the lang key `effect.<mod>.<id>.description`. JEED ships text for vanilla and the mods it knows. The pack fills the
rest with `tools/gen_effect_descriptions.py`, which writes `TODO` placeholders into
`kubejs/assets/creativeengineering/lang/en_us.json` until someone writes real text.

## 2. Reference

### 2.1 Effect table

Columns:

- **Brew:** a brewing-stand recipe makes a potion with this effect (pack, mod or vanilla). `Brewery` = only Botania's
  Botanical Brewery.
- **Arrow:** the potion's tipped arrow has its own name (not the raw `item.minecraft.tipped_arrow.effect.*` key). `-`
  when there is no potion.
- **Super:** the effect only works as other effects put together, or is a vanilla effect with something added. The
  parts are in **Subset**.
- **Fluid:** JEI's ingredient list has a `create:potion` fluid for it (one per potion, [fluid_jei.js](../kubejs/client_scripts/fluid_jei.js)).
- **Desc:** JEED description: `Y` complete and right, `N` missing or a `TODO` placeholder, `Partial` right but leaves
  out part of what it does, `Wrong` says something the code doesn't do. For anything but `Y`, **Detailed** gives
  replacement text, marked PROPOSED.
- **Food:** count of foods and drinks that give it, a few examples, canned versions counted. Blank if none.
- **Overlaps:** other effects that do the same job, `total` (one fully covers the other) or `slight` (part of it).
  Opposites and cures aren't listed.

Mods are in alphabetical order after vanilla. Numbers are per level unless stated.

| Effect | Mod | Brew | Arrow | Super | Fluid | Desc | Food | Subset | Overlaps | Brief | Detailed (JEI description) |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Speed `minecraft:speed` | Minecraft | Y | Y | N | Y | Y | 54: Bowl of Pineapple Fried Rice, Chocolate Coffee, Chorus Pie Slice, Cocoa Fudge, +50 more | | Sugar Rush: slight; Caffeinated: slight; Fleet-Footed: slight | +20% walk speed. | Increases walking speed; higher levels make the user faster and increase their field of view. |
| Slowness `minecraft:slowness` | Minecraft | Y | Y | N | Y | Y | 3: Matcha Ice Cream, Salmonberry Ice Cream, Withering Dross | | Sticky: total; Flashed: slight; Overencumbered: slight; Frosty Armor: slight | -15% walk speed. | Decreases walking speed; higher levels make the user slower and decrease their field of view. |
| Haste `minecraft:haste` | Minecraft | Y (Rustic Delight) | Y | N | Y | Y | 20: Berry Matcha Latte, Builder's Tea, Chocolate Coffee, Coffee, +16 more | | Conduit Power: total underwater; Caffeinated, Raging, Orca's Might: slight (attack speed) | +20% mining speed, +10% attack speed. | Increases mining and attack speed; higher levels increase both stats further. |
| Mining Fatigue `minecraft:mining_fatigue` | Minecraft | Y (pack) | Y | N | Y | Y | Pond Water | | | Mining speed x0.3 per level, -10% attack speed. | Decreases mining and attack speed; higher levels decrease both stats further. |
| Strength `minecraft:strength` | Minecraft | Y | Y | N | Y | Y | 17: Cheeseburger, Cider, Deluxe Cheeseburger, Endermite Stew, +13 more | | Berserking: slight; Frosty Armor: slight | +3 melee damage. | Increases melee damage; higher levels increase the damage boost power. |
| Instant Health `minecraft:instant_health` | Minecraft | Y | Y | N | Y | Y | Mellohi Wine | | Heal Aura: total | Heals 4 HP (doubling per level); hurts undead. | Heals living entities, damages undead; higher levels increase the effect potency. |
| Instant Damage `minecraft:instant_damage` | Minecraft | Y | Y | N | Y | Y | | | Copper Poisoning: total; Explosion (Vinery): slight | 6 damage (doubling per level); heals undead. | Damages living entities, heals undead; higher levels increase the effect potency. |
| Jump Boost `minecraft:jump_boost` | Minecraft | Y | Y | N | Y | Y | Noir Wine | | Double Jump: total | Jump higher, take less fall damage. | Increases jump height and reduces fall damage; higher levels increase both effects. |
| Nausea `minecraft:nausea` | Minecraft | Y (pack) | Y | N | Y | Y | 18: Bellini Cocktail, Cooking Oil, Crawling Sandwich, Maggot Salad, +14 more | | Tipsy, Trippy, Earthquake: slight | Screen wobble. | Wobbles and warps the screen. |
| Regeneration `minecraft:regeneration` | Minecraft | Y | Y | N | Y | Y | 120: Chicken Salad, Creamy Corn Drink, Enchanted Golden Apple, Enchanted Golden Baked Potato, +116 more | | Jellie: total; Comfort, Sweet Heart: slight | 1 HP every 2.5 s (halved per level). | Regenerates health over time; higher levels make health regenerate quicker. |
| Resistance `minecraft:resistance` | Minecraft | Y (pack) | Y | N | Y | Y | 62: Apple Wine, Bowl Of Stuffed Shulker, Cantaloupe Slice, Crawling Sandwich, +58 more | | Magic Resistance, Armor (Vinery), Berserking: slight | -20% damage taken. | Provides 20% damage reduction per level. |
| Fire Resistance `minecraft:fire_resistance` | Minecraft | Y | Y | N | Y | Y | 59: Blazing Chili, Enchanted Golden Apple, Enchanted Golden Baked Potato, Enchanted Golden Beetroot, +55 more | | Anti-Fire (IP): slight | Immune to fire and lava damage. | Grants immunity to fire damage as well as damage from lava. |
| Water Breathing `minecraft:water_breathing` | Minecraft | Y | Y | N | Y | Y | 10: Cristel Wine, Enchanted Golden Moss Clump, Pickerelweed Juice, Salmonberries, +6 more | | Conduit Power: total | No drowning. | Prevents drowning and allows to breathe underwater. |
| Invisibility `minecraft:invisibility` | Minecraft | Y | Y | N | Y | Y | Cherry Wine | | | Invisible; mobs see you from closer. | Grants invisibility, making the user invisible and reducing its detection range. Held or worn items will still be visible. Higher levels further decrease the detection range |
| Blindness `minecraft:blindness` | Minecraft | Y (pack) | Y | N | Y | Y | 4: Ender Nectar, Pond Water, Warped Moldy Meat, Withering Dross | | Darkness, Flashed: slight | Short view, no sprint or crits. | Impairs vision and disables the ability to sprint and critical hit. |
| Night Vision `minecraft:night_vision` | Minecraft | Y | Y | N | Y | Y | 10: Blueberry Custard, Blueberry Jam, Blueberry Jello, Blueberry Muffin, +6 more | | Conduit Power: total underwater; Lava Vision: slight | See in the dark and underwater. | Improves vision in dark areas and underwater. |
| Hunger `minecraft:hunger` | Minecraft | Y (pack) | Y | N | Y | Y | 15: Batter, Pond Water, Pufferfish, Raw Chicken, +11 more | | Intoxication: slight | Food drains faster. | Increases food exhaustion; higher levels cause to starve quicker. |
| Weakness `minecraft:weakness` | Minecraft | Y | Y | N | Y | Y | 5: Nether Wart Sourdough, Pond Water, Purulent Tea, Withering Dross, +1 more | | | -4 melee damage. | Decreases melee damage; higher levels decrease damage dealt further. |
| Poison `minecraft:poison` | Minecraft | Y | Y | N | Y | Y | 13: Blobfish, Chocolate Night Berries, Ground Strider, Poisonous Potato, +9 more | | Debilitating Sting, Exsanguination, Wither: slight | 1 damage every 1.25 s (halved per level), never kills. | Inflicts non lethal damage over time; higher levels do more damage per second. Does not affect undeads. |
| Wither `minecraft:wither` | Minecraft | Y (pack) | Y | N | Y | Y | 4: Coffee Berries, Gambler's Tea, Sinister Carrot, Withering Dross | | Exsanguination: slight | 1 damage every 2 s (halved per level), can kill. | Inflicts lethal damage over time; higher levels do more damage per second. |
| Health Boost `minecraft:health_boost` | Minecraft | Y (pack) | Y | N | Y | Y (pack) | 8: Hamimelon Jam, Hamimelon Jello, Hamimelon Juice, Hamimelon Popsicle, +4 more | | Absorption: slight | +4 max health. Also the Vinery banner (II). | Increases maximum health; higher levels give more additional hearths. | Increases maximum health; higher levels give more additional hearts. 
| Absorption `minecraft:absorption` | Minecraft | Y (pack) | Y | N | Y | Y (pack) | 103: Apple Cider, Apple Jam, Apple Jello, Chorus Juice, +99 more | | Jellie: total; Health Boost: slight | 4 absorption health that doesn't regenerate. | Adds some damaging absorbing hearths (which can't be regenerated); higher levels give more absorption. | Adds some damage-absorbing hearts (which can't be regenerated); higher levels give more absorption. 
| Saturation `minecraft:saturation` | Minecraft | Y (pack, 5 ticks) | Y | N | Y | Y | 2: Biscuit of Totality, Prickly Pear Juice | | Nourishment, Digesting: slight | Instant food and saturation. | Restores hunger and saturation. |
| Glowing `minecraft:glowing` | Minecraft | Y (pack) | Y | N | Y | Y | 10: Gambler's Tea, Glittering Grenadine, Glow Berries, Glow Berry Custard, +6 more | | | Outline visible through walls. | Marks affected entities with an outline glow, allowing them to be seen through blocks. |
| Levitation `minecraft:levitation` | Minecraft | Y (pack) | Y | N | Y | Y | 2: Bowl Of Stuffed Shulker, +1 more | | | Float upwards. | Make affected entities float upwards. |
| Luck `minecraft:luck` | Minecraft | Y (Buzzier Bees) | Y | N | Y | Y | Canned Pickled Pickles | | | +1 luck (better loot and fishing). | Can increase chances of high-quality and more loot; higher levels increase the chances of better loot. |
| Bad Luck `minecraft:unluck` | Minecraft | Y (Buzzier Bees, pack) | Y | N | Y | Y | | | | -1 luck. | Can reduce chances of high-quality and more loot; higher levels reduce the chance of good loot. |
| Slow Falling `minecraft:slow_falling` | Minecraft | Y | Y | N | Y | Y | 2: Cherry Cream Soda, Red Wine | | Sunbird's Blessing: total; Feather Feet: slight | Fall slowly, no fall damage. | Decreases falling speed and negates fall damage. |
| Conduit Power `minecraft:conduit_power` | Minecraft | N | - | Y | N | Y | | Water Breathing, Night Vision, Haste (underwater only) | Water Breathing: total; Night Vision, Haste: total underwater | From an active conduit. | Increases underwater visibility and mining speed, prevents drowning. |
| Dolphin's Grace `minecraft:dolphins_grace` | Minecraft | N | - | N | N | Y | | | Oiled: slight | Much faster swimming; from dolphins. | Drastically increases swimming speed. Effect given by dolphins |
| Bad Omen `minecraft:bad_omen` | Minecraft | N | - | N | N | Y | 2: A Bottle of 'Villagers Fright', Dread Nog | | Thief: slight | Starts a raid when you enter a village. | Causes an illager raid to start upon entering a village; higher levels increase the raid difficulty. |
| Hero of the Village `minecraft:hero_of_the_village` | Minecraft | N | - | N | N | Y | | | | Trade discounts and gifts after a raid. | Gives discounts on trades with villagers, and makes villagers throw items at the player depending on their profession. |
| Darkness `minecraft:darkness` | Minecraft | Y (pack) | Y | N | Y | Y | | | Blindness: slight | Pulsing darkness. | Causes vision to temporarily deteriorate. |
| Bug Pheromones `alexsmobs:bug_pheromones` | Alex's Mobs | Y | Y | N | Y | Y | | | Harmony, Intimidation: slight | Arthropods don't target you unless you hit them. | Make hostile arthropods neutral to the user. |
| Clinging `alexsmobs:clinging` | Alex's Mobs | Y | Y | N | Y | Y | | | Climbing (Vinery), Agility: slight | Walk upside down on ceilings; sneak to drop. | Allows walking upside-down on ceilings. |
| Debilitating Sting `alexsmobs:debilitating_sting` | Alex's Mobs | N | - | N | N | Y | | | Poison: slight; Scared Still: slight (arthropods) | Tarantula Hawk sting. Others take magic damage down to half health; arthropods are paralysed, then take 30 damage per level (level II hatches a tarantula hawk). | An Effect which poisons the victim but paralyzes arthropods. It will stop if the victim is at half health and is not an arthropod. Inflicted by a Tarantula Hawk. |
| Earthquake `alexsmobs:earthquake` | Alex's Mobs | N | - | N | N | Y | | | Nausea: slight | Camera shake; from Rocky Rollers. | Shakes the user violently. |
| Ender Flu `alexsmobs:ender_flu` | Alex's Mobs | N | - | N | N | Y | Cosmic Cod | | | When it runs out: 10 damage per level and one Enderiophage per level. | After the effect runs out naturally, the user takes massive damage and an Enderiophage will spawn. Inflicted by an Enderiophage. |
| Exsanguination `alexsmobs:exsanguination` | Alex's Mobs | N | - | N | N | Y | | | Wither, Poison: slight | 1 magic damage per second per level, can kill; Frilled Shark bite. | Gradually drains health. |
| Scared Still `alexsmobs:fear` | Alex's Mobs | N | - | N | N | Y | | | Power Outage: total; Shocked: total; Concrete Feet: slight | Can't move or jump; a Tiger revealing itself. | Makes the target unable to move. Effect applied when a Tiger reveals itself to the user. |
| Fleet-Footed `alexsmobs:fleet_footed` | Alex's Mobs | N | - | N | N | Y | | | Speed: slight | +0.2 speed (about 3x walk) while sprinting in the air; from Jerboas. | Increases player speed whilst jumping and sprinting. |
| Knockback Resistance `alexsmobs:knockback_resistance` | Alex's Mobs | Y | Y | N | Y | Y (pack) | | | Resilience (Quark): total | +50% knockback resistance; level II ignores knockback. | PROPOSED: Gives 50% knockback resistance per level; at level II and above you ignore knockback entirely. | Gives 50% knockback resistance per level; at level II and above you ignore knockback entirely. 
| Lava Vision `alexsmobs:lava_vision` | Alex's Mobs | Y | Y | N | Y | Y | | | Night Vision: slight | See through lava. | Allows seeing through lava. |
| Oiled `alexsmobs:oiled` | Alex's Mobs | N | - | N | N | Y | Fish Oil | | Water Walker: total; Dolphin's Grace: slight | Float up in water and rain; sneak to sink. | Oil floats on water. Cover yourself in oil to float on the surface of water and outside in the rain. |
| Orca's Might `alexsmobs:orcas_might` | Alex's Mobs | N | - | N | N | Y | | | Haste, Raging, Caffeinated: slight | +3 attack speed; from swimming with an orca. | Grants increased attack speed. Effect may be applied when swimming with an Orca. |
| Poison Resistance `alexsmobs:poison_resistance` | Alex's Mobs | Y | Y | N | Y | Y | | | Recovering: total | Clears Poison every tick. | Grants immunity to poison. |
| Power Outage `alexsmobs:power_down` | Alex's Mobs | N | - | N | N | Y (pack) | | | Scared Still: total | Can't move or jump, plays a power-outage sound; April Fools grizzly only. | PROPOSED: Freezes you in place (no walking or jumping) and plays a power-outage sound. Only from Alex's Mobs' April Fools grizzly bear. | Freezes you in place (no walking or jumping) and plays a power-outage sound. Only from Alex's Mobs' April Fools grizzly bear. 
| Soulsteal `alexsmobs:soulsteal` | Alex's Mobs | Y | Y | N | Y | Y | | | Soul Cross: slight | On hit: (25 + 25 x level)% chance to heal half the damage x level, at most 2 + 2 x level. | Grants the user attacks some life-steal. |
| Sunbird's Blessing `alexsmobs:sunbird_blessing` | Alex's Mobs | N | - | Y | N | Y | | Slow Falling (plus elytra lift) | Slow Falling: total; Feather Feet: slight | Slow fall, no fall damage, lift while gliding upward; near a Sunbird. | Decreases fall speed, prevents fall damage and makes it easier to fly with an Elytra or Tarantula Hawk Elytra. Effect is applied when near a Sunbird. |
| Sunbird's Curse `alexsmobs:sunbird_curse` | Alex's Mobs | N | - | N | N | Y | | | | Pulled down faster, elytra stops; from attacking a Sunbird. | Increases fall speed, causing the user to hit the ground quicker. This effectively impedes the use of an Elytra. Effect is applied when attacking a Sunbird. |
| Tiger's Blessing `alexsmobs:tigers_blessing` | Alex's Mobs | N | - | N | N | Y | | | | Tigers within 32 blocks attack whatever you attack. | Tigers near the user will be neutral and will protect them from hostile foes. The effect will go away early when attacking a Tiger. Applied by feeding a Tiger chicken or pork chops. |
| Long Reach `better_beacons:long_reach` | Better Beacons | Y (pack) | Y | N | Y | N | | | | +3 block reach (blocks only, not attacks). Also felsic and dragon armor. | PROPOSED: Extends your block reach by 3 blocks per level, for placing, breaking and using blocks. Attack range is unchanged. From a beacon, felsic and dragon armor, or a Potion of Long Reach. |
| Patrol Nullifier `better_beacons:patrol_nullifier` | Better Beacons | N | - | N | N | N | | | | No pillager patrols spawn around you; beacon only. | PROPOSED: Pillager patrols don't spawn around you while this is active. From a beacon. |
| Phantom Bane `better_beacons:phantom_bane` | Better Beacons | Y (pack) | Y | N | Y | N | | | Caffeinated: total (phantom spawns); Restfulness, Rest, Sunny: slight | No phantoms spawn for you. Also the echo armor set. | PROPOSED: Phantoms don't spawn for you while this is active, however long you've gone without sleep. From a beacon, the echo armor set or a Potion of Phantom Bane. |
| Allure `botania:allure` | Botania | Brewery | - | N | N | Y | | | | Fishing rod ticks twice as often (double fishing speed). | The affected player will have their Fishing Rod update twice as often, effectively doubling fishing speed |
| Bloodthirst `botania:bloodthirst` | Botania | Brewery | - | N | N | Y | | | | Monsters spawn within 64 blocks whatever the light. | Allows hostile monsters to spawn within a 64 block radius of the user, regardless of light level or other spawning conditions. This includes allowing Slimes to spawn outside of Slime Chunks |
| Absolution `botania:clear` | Botania | Brewery | - | N | N | Y | | | Vanilla Scent: slight | Instant: removes every effect (like milk). | Clears all status effects |
| Emptiness `botania:emptiness` | Botania | Brewery | - | N | N | Y (pack) | | | Phantom Bane, Patrol Nullifier: slight | No natural mob spawns within 128 blocks. | Prevents mob natural mob spawning in a 128 block radius around the affected entity | Prevents natural mob spawning in a 128 block radius around the affected entity. 
| Feather Feet `botania:feather_feet` | Botania | Brewery | - | N | N | Y (pack) | | | Slow Falling, Sunbird's Blessing: slight | No fall damage. Also the dragon armor set. | Negated damaged cause by falling. This effect doesn't prevent fall damage from other causes like Ender Pearls | Negates damage caused by falling. This effect doesn't prevent fall damage from other causes like Ender Pearls. 
| Soul Cross `botania:soul_cross` | Botania | Brewery | - | N | N | Y (pack) | | | Soulsteal: slight | Heal 5% of a killed creature's max health. | PROPOSED: When you kill a creature, heals you by 5% of that creature's maximum health. | When you kill a creature, heals you by 5% of that creature's maximum health. 
| Intoxication `brewinandchewin:intoxication` | Brewin' and Chewin' (port) | N | - | N | N | Y | 16: Beer, Bloody Mary, Dread Nog, Egg Grog, +12 more | | Hunger: slight | Food gives no saturation; hidden, comes with every alcoholic drink. | Makes the player unable to gain Saturation from food. |
| Raging `brewinandchewin:raging` | Brewin' and Chewin' (port) | N | - | N | N | Y | 2: Bloody Mary, Red Rum | | Haste, Orca's Might, Caffeinated: slight | +5% attack speed per melee hit, stacks 4 times. | Allows you to gain a 5% attack speed boost upon dealing melee damage to entities. This stacks up to 4 times. |
| Sweet Heart `brewinandchewin:sweet_heart` | Brewin' and Chewin' (port) | N | - | N | N | Y | 2: Mead, Saccharine Rum | | Regeneration, Comfort: slight | Spends saturation to heal 1 HP per second (faster per level) at any hunger. | Allows for extra saturation based healing at any hunger level on top of the existing saturation healing. |
| Tipsy `brewinandchewin:tipsy` | Brewin' and Chewin' (port) | N | - | N | N | Y | 16: Beer, Bloody Mary, Dread Nog, Egg Grog, +12 more | | Nausea, Trippy: slight | Screen wobble; mouse drag at III, scrambled chat at IV; delays damage. | Provides the screen with a wobble effect. At level III or higher, your mouse will have drag to it. At level IV or higher, chat sent by you, other player's chat on your end and signs will be scrambled. May delay damage, notated by orange Tipsy hearts. |
| Sunny `buzzier_bees:sunny` | Buzzier Bees | N | - | N | N | Y (hidden in JEI) | | | Phantom Bane: slight | Acts as sunlight: undead burn, bees stay awake, phantoms leave you alone. Buttercup stew (12 s), mooblooms. | Simulates sunlight. Phantoms won't attack mobs that have the effect, bees won't sleep if they have the effect, undead that have the effect will burn |
| Rest `create_confectionery:rest` | Create Confectionery | N | - | N | N | Y | 3: Bar of White Chocolate, Black Chocolate Candy, Ruby Chocolate Candy | | Phantom Bane: slight | Removes phantoms within 4 blocks every tick. Listed as harmful. | Makes phantoms disappear. |
| Stimulation `create_confectionery:stimulation` | Create Confectionery | N | - | Y | N | Y | 3: Bar of Black Chocolate, Black Chocolate Candy, Ruby Chocolate Candy | Refreshing, Sweetening (Fruits Delight) | Refreshing: total; Sweetening: total | Clears Slowness and Mining Fatigue every tick. | Removes mining fatigue and slowness |
| Shocked `createaddition:shocking` | Create Crafts & Additions | N | - | N | N | Y (pack) | | | Scared Still: total | Can't move at all; from a Tesla coil. | PROPOSED: Stops you moving entirely while it lasts. From standing near a Tesla coil. | Stops you moving entirely while it lasts. From standing near a Tesla coil. 
| Magic Resistance `creativeengineering:magic_resistance` | Creative Engineering (pack) | Y (pack) | Y | N | Y | N | | | Resistance: slight | -20% magic damage, immune at level V. Also felsic and dragon armor. | PROPOSED: Cuts damage from magic (Poison, Harming, evoker fangs, guardian beams, dragon's breath) by 20% per level; level V and above blocks it. From felsic and dragon armor or a Potion of Magic Resistance. |
| Ender Phasing `endersdelight:phasing` | Ender's Delight | N | - | N | N | Y | 3: Chorus Stew, Twisted Cereal, +1 more | | Chorus, Teleport (Vinery): slight | Rain and water hurt you; you teleport away when hit. | Imitates Endermen. (Water deals damage and teleports the user to a nearby location after taking damage) |
| Copper Poisoning `everythingcopper:copper_poisoning` (no name) | Everything Copper | N | - | N | N | N | Copper Nugget | | Instant Damage: total (weaker) | Instant 2 damage, doubling per level. | PROPOSED: Instant damage: 2 health at level I, doubling each level. From eating a copper nugget. |
| Comfort `farmersdelight:comfort` | Farmer's Delight | N | - | N | N | Y (pack) | 44: Baked Cod Stew, Beef Stew, Bell Pepper Soup, Bone Broth, +40 more | | Regeneration, Sweet Heart: slight | Heals 1 HP every 4 s while saturation is empty and you have no Regeneration. | PROPOSED: Heals half a heart every 4 seconds while your saturation is empty and you don't have Regeneration. | Heals half a heart every 4 seconds while your saturation is empty and you don't have Regeneration. 
| Nourishment `farmersdelight:nourishment` | Farmer's Delight | N | - | N | N | Y | 87: Acacia Blossom Soup, Azalea Tea, Bacon and Eggs, Bell Pepper Pasta, +83 more | | Saturation: slight | Hunger doesn't drain, except to heal from a nearly full bar. | Prevents players from losing hunger or saturation from any other source other than replenishing health points. |
| Caffeinated `farmersrespite:caffeinated` | Farmer's Respite | N | - | N | N | Y (pack) | 10: Azalea Tea, Berry Matcha Latte, Black Cod, Black Tea, +6 more | | Phantom Bane: total (phantom spawns); Restfulness: total; Speed, Haste: slight | +10% walk speed, +20% attack speed, keeps resetting time since rest. | PROPOSED: Increases walk speed by 10% and attack speed by 20%, and keeps resetting the time since you last slept, so phantoms don't come. | Increases walk speed by 10% and attack speed by 20%, and keeps resetting the time since you last slept, so phantoms don't come. 
| Alienating `fruitsdelight:alienating` | Fruits Delight | N | - | Y | N | Y | 6: Baked Durian, Durian Jam, Durian Jello, Durian Pie, +2 more | Weakness, Disgusted (on others) | | Every second, mobs within 12 blocks get Weakness and Disgusted. | Apply disgusted and weakness effect to surrounding entities without this effect |
| Appetizing `fruitsdelight:appetizing` | Fruits Delight | N | - | N | N | Y | 6: Hawberry Jam, Hawberry Jello, Hawberry Roll, Hawberry Sheet, +2 more | | | Eat when full. | Eat and drink even when you are full. |
| Astringent `fruitsdelight:astringent` | Fruits Delight | N | - | N | N | Y | 6: Dried Persimmon, Persimmon Cookie, Persimmon Jam, Persimmon Jello, +2 more | | | Ice and slime don't slide. | Grant more friction when walking on low-friction blocks, such as ice. |
| Brightening `fruitsdelight:brightening` | Fruits Delight | N | - | N | N | Y | 4: Blueberry Custard, Blueberry Jam, Blueberry Jello, Blueberry Muffin | | | Immune to Blindness and Darkness. | Make player immune to blindness and darkness |
| Chorus `fruitsdelight:chorus` | Fruits Delight | N | - | N | N | Y | 2: Chorus Jam, Chorus Jello | | Teleport (Vinery): total | One random chorus-fruit teleport. | Teleport player randomly |
| Cycling `fruitsdelight:cycling` | Fruits Delight | N | - | N | N | Y | 4: Kiwi Jam, Kiwi Jello, Kiwi Juice, Kiwi Popsicle | | | Drops 1 level as XP orbs every second (slower per level). | Player will drop 1 level of experience as orbs every second |
| Digesting `fruitsdelight:digesting` | Fruits Delight | N | - | N | N | Y | 6: Fig Chicken Stew, Fig Jam, Fig Jello, Fig Pudding Slice, +2 more | | Saturation: slight | Food over a full bar becomes saturation. | Overflowing nutrition becomes saturation. |
| Disgusted `fruitsdelight:disgusted` | Fruits Delight | N | - | N | N | Y | | | | Can't breed; from Alienating. | Entity with this effect will not breed |
| Heal Aura `fruitsdelight:heal_aura` | Fruits Delight | N | - | Y | N | Y | 4: Bellini Cocktail, Peach Jam, Peach Jello, Peach Tea | Instant Health (you and everything within 6 blocks) | Instant Health: total | Every 3 s, Instant Health to you and every mob within 6 blocks (hurts undead). | Apply instant health to mobs around you |
| Leaf Piercing `fruitsdelight:leaf_piercing` | Fruits Delight | N | - | N | N | Y | 6: Bayberry Cookie, Bayberry Jam, Bayberry Jello, Bayberry Soup, +2 more | | | Your projectiles pass through leaves. | Projectiles player shot will go through leaves |
| Lozenge `fruitsdelight:lozenge` | Fruits Delight | N | - | N | N | Y | 6: Baked Pear, Pear Jam, Pear Jello, Pear Juice, +2 more | | | Eat and drink twice as fast (more per level). | Eat and drink faster. |
| Rage Aura `fruitsdelight:rage_aura` | Fruits Delight | N | - | N | N | Y | 6: Mango Jam, Mango Jello, Mango Milkshake, Mango Salad, +2 more | | | Idle mobs within 24 blocks target you. | Mobs around you will be hostile to you |
| Recovering `fruitsdelight:recovering` | Fruits Delight | N | - | N | N | Y | 5: Orange Chicken, Orange Jam, Orange Jello, Orange Juice, +1 more | | Poison Resistance: total | Immune to Poison and Wither. | Make player immune to poison and wither |
| Refreshing `fruitsdelight:refreshing` | Fruits Delight | N | - | N | N | Y | 6: Lemon Cookie, Lemon Jam, Lemon Jello, Lemon Juice, +2 more | | Stimulation: total | Immune to Mining Fatigue. | Make player immune to mining fatigue |
| Shrinking `fruitsdelight:shrinking` | Fruits Delight | N | - | N | N | Y | 5: Cranberry Cookie, Cranberry Jam, Cranberry Jello, Cranberry Muffin, +1 more | | | Half size while sneaking; crawl through 1-block gaps. | You can crawl into 1-block space when sneaking. |
| Sliding `fruitsdelight:sliding` | Fruits Delight | N | - | N | N | Y | 4: Mangosteen Cake, Mangosteen Jam, Mangosteen Jello, Mangosteen Tea | | | Boats slide on land like on ice. | Reduce friction between your boat and blocks. |
| Suspicious Smell `fruitsdelight:suspicious_smell` | Fruits Delight | N | - | N | N | Y | 6: Baked Durian, Durian Jam, Durian Jello, Durian Pie, +2 more | | | See suspicious stew effects; spot suspicious sand. | You can see effects in suspicious stew and find suspicious sand easily |
| Sweetening `fruitsdelight:sweetening` | Fruits Delight | N | - | N | N | Y | 7: Bowl of Pineapple Fried Rice, Pineapple Jam, Pineapple Jello, Pineapple Marinated Pork, +3 more | | Stimulation: total | Immune to Slowness. | Make player immune to slowness |
| Concrete Feet `immersiveengineering:concrete_feet` | Immersive Engineering | N | - | N | N | Y | | | Scared Still: slight | Can't move while standing on IE concrete; ends when you leave it. | Makes the target unable to move or jump. The effect immediately ends when the user is no longer standing on Concrete |
| Conductive `immersiveengineering:conductive` | Immersive Engineering | N | - | N | N | Y | | | | Electric damage x(1.5 + 0.5 x amp^2). | Affected entities take extra damage from electric or 'flux' damage, such as Redstone Arsenal weapons or electrified Razor Wire |
| Flammable `immersiveengineering:flammable` | Immersive Engineering | N | - | N | N | Y | | | | Fire damage x(1.5 + 0.5 x amp^2). | Increases fire damage taken |
| Flashed `immersiveengineering:flashed` | Immersive Engineering | N | - | N | N | Y (pack) | | | Blindness, Slowness: slight | White screen, -15% walk speed. | PROPOSED: Players affected by it will have their screen flash white, rendering them temporarily blind, and walk 15% slower. | Players affected by it will have their screen flash white, rendering them temporarily blind, and walk 15% slower. 
| Slippery `immersiveengineering:slippery` | Immersive Engineering | N | - | N | N | Y | | | Slipping: slight | Slide forward on the ground, sometimes drop a held item. | Affected entities will slowly slide forward when on the ground, and each tick have a 1/300 chance (roughly 6.5% chance each second) of dropping either their held item or their offhand item |
| Sticky `immersiveengineering:sticky` | Immersive Engineering | N | - | N | N | Y (pack) | | | Slowness: total | -20% walk speed. | Heavily slows do the target and impedes jumping | Heavily slows down the target and impedes jumping. 
| Stunned `immersiveengineering:stunned` | Immersive Engineering | N | - | N | N | Y | | | | Endermen can't teleport. | Endermen affected by this effect are unable to teleport |
| Anti-Fire `immersivepetroleum:anti_fire` (no name) | Immersive Petroleum | N | - | N | N | N | | | Fire Resistance: slight | Hidden, 1 tick: puts your fire out once after you step off a fireproof motorboat onto lava. | PROPOSED: Hidden. Given for one tick when you leave a fireproof motorboat over lava, so the first burn is put out. |
| Thief `monobank:thief` | Monobank | N | - | N | N | Y | | | Bad Omen: slight | From robbing a Monobank: iron golems within 16 blocks (more per level) attack, no trading. | Prevents the user from trading with villagers and calls the police on them (Iron Golems will be aggressive). |
| Agility `neapolitan:agility` | Neapolitan | N | - | N | N | Y | 9: Adzuki Curry, Banana, Banana Bread, Banana Ice Cream, +5 more | | Climbing (Vinery): total; Clinging: slight | Climb any wall like a ladder. | Allows to climb over any block as if they're ladders. |
| Berserking `neapolitan:berserking` | Neapolitan | N | - | N | N | Y | 7: Cooked Mint Chops, Mint Candies, Mint Chocolate, Mint Ice Cream, +3 more | | Strength, Resistance, Armor (Vinery): slight | +1 armor and +0.5 attack damage; each kill adds a level. | Killing mobs raises the effect by 1 level each time, each level adds strength and defense. |
| Harmony `neapolitan:harmony` | Neapolitan | N | - | N | N | Y | 10: Adzuki Bun, Adzuki Curry, Adzuki Ice Cream, Adzuki Milkshake, +6 more | | Bug Pheromones, Intimidation: slight | Most hostile mobs within 12 blocks run from you. | Makes most hostile mobs run from the user. |
| Slipping `neapolitan:slipping` | Neapolitan | N | - | N | N | Y | | | Slippery: slight | Random sideways shoves on the ground. | Makes the user move across the ground in random directions. |
| Sugar Rush `neapolitan:sugar_rush` | Neapolitan | N | - | Y | N | Y | 9: Chocolate Bar, Chocolate Ice Cream, Chocolate Milkshake, Chocolate Night Berries, +5 more | Speed, then Slowness | Speed, Slowness: slight | +10% speed for the first two thirds, then -25%. | Gives speed for the first three quarters of the effect duration, slowness for the last one. |
| Vanilla Scent `neapolitan:vanilla_scent` | Neapolitan | N | - | N | N | Y | 7: Dried Vanilla Pods, Neapolitan Ice Cream, Vanilla Chocolate Fingers, Vanilla Fudge, +3 more | | Absolution: slight | No new effect can be applied. | Blocks incoming effects. |
| Resilience `quark:resilience` | Quark | Y (crab shell) | Y ("Arrow of Stability") | N | Y | Y (pack) | | | Knockback Resistance (AM): total | +50% knockback resistance; level II ignores knockback. Potions are named "Stability". | PROPOSED: Gives 50% knockback resistance per level; at level II and above you ignore knockback entirely. | Gives 50% knockback resistance per level; at level II and above you ignore knockback entirely. 
| Overencumbered `supplementaries:overencumbered` | Supplementaries | N | - | N | N | Y | | | Slowness: slight | No sprinting; lower jumps and walk speed at higher levels; from carrying too many sacks. | Prevents sprinting. Further levels reduce jump height hinder walking speed respectively |
| Intimidation `untitledduckmod:intimidation` | Untitled Duck Mod | Y (goose foot) | N (raw key) | N | Y | Y | | | Harmony: slight | Illagers flee from you. | Makes illagers afraid of the target. |
| Insomnia `upgrade_aquatic:insomnia` | Upgrade Aquatic | Y | Y | N | Y | Y | | | | Instant: +1 day since rest per level (phantoms sooner); phantoms become flares. | Increases the insomnia timer by 1 day worth per effect level, causing phantoms to spawn. If given to Phantoms will turn them into Flares, if given to Flares it will instantly kill them. |
| Repellence `upgrade_aquatic:repellence` | Upgrade Aquatic | Y | Y | Y | Y | Y | | Poison (on whatever touches you) | | Mobs touching you get Poison one level higher. | Inflicts poison on mobs that physically touch the user; higher levels increase the given poison effect level. |
| Restfulness `upgrade_aquatic:restfulness` | Upgrade Aquatic | Y | Y | N | Y | Y | | | Caffeinated: total; Phantom Bane: slight | Instant: -1 day since rest per level; kills phantoms. | Decreases the insomnia timer by 1 day worth per effect level, decreasing Phantoms spawn chance. If given to Flares will turn them into Phantoms, if given to Phantoms it will instantly kill them. |
| Vibing `upgrade_aquatic:vibing` | Upgrade Aquatic | Y | Y | Y | Y | Y | | Regeneration (on whatever touches you) | | Mobs touching you get Regeneration one level higher. | Inflicts regeneration on mobs that physically touch the user; higher levels increase the given regeneration effect level. |
| Travel Fatigue `via_romana:travellers_fatigue` | Via Romana | N | - | N | N | Y (ported) | | | | Cooldown after fast travel along a road. | PROPOSED: You've just travelled along a road. You can't fast-travel again until this wears off. | Prevents fast travel. (Effect Insights 26.3.1) 
| Armor `vinery:armor_effect` | Vinery (port) | N | - | N | N | Y | Aegis Wine | | Resistance, Frosty Armor, Berserking: slight | +2 armor, +3 armor toughness. | Increases Armor and Armor Toughness. |
| Climbing `vinery:climbing_effect` | Vinery (port) | N | - | N | N | Y | 2: Chenet Wine, Jo's Special Mixture | | Agility: total; Clinging: slight | Walk into a wall to climb it. | Allows climbing Walls. |
| Explosion `vinery:creeper_effect` | Vinery (port) | N | - | N | N | Y | A Bottle of 'Creepers Crush' | | Instant Damage: slight | You explode (power = level) and take 50 damage. | Causes the player to explode after a few seconds. Kaboom! |
| Double Jump `vinery:double_jump` | Vinery (port) | N | - | Y | N | Y (pack) | Clark Wine | Jump Boost (plus a mid-air jump) | Jump Boost: total | Jump Boost's height and less fall damage, plus a second jump in mid-air. | PROPOSED: Jump as high as Jump Boost of the same level, take a little less fall damage, and press jump again in mid-air to jump a second time. | Jump as high as Jump Boost of the same level, take a little less fall damage, and press jump again in mid-air to jump a second time. 
| Experience `vinery:experience_effect` | Vinery (port) | N | - | N | N | Y | A Bottle of 'Mojang Noir' | | | +50% XP from orbs. | Increases Experience gained. |
| Frosty Armor `vinery:frosty_armor` | Vinery (port) | N | - | N | N | Y (pack) | Eiswein | | Armor (Vinery), Strength, Slowness: slight | +4 armor, +2 attack damage, -5% speed; you freeze at level II. | PROPOSED: +4 armor and +2 attack damage, but 5% slower. At level II and above you also start to freeze. | Gives +4 armor and +2 attack damage, but you walk 5% slower. At level II and above you also start to freeze. 
| Better than Golden Apples! `vinery:jellie` | Vinery (port) | N | - | Y | N | Y | Jellie Wine | Regeneration, Absorption | Regeneration: total; Absorption: total | Regeneration's healing rate plus 4 absorption per level. | Provides the same but greater benefits than Golden Apples. |
| Lava Walker `vinery:lava_walker` | Vinery (port) | N | - | N | N | Y | Bolvar Wine | | Fire Resistance: slight | Walk on lava (no fire protection). | Allows walking on Lava. |
| Magnet `vinery:magnet` | Vinery (port) | N | - | N | N | Y | Magnetic Wine | | | Items within 5 blocks (+1 per level) picked up at once, or pulled to you when full; sneak to pause. | Attracts Items within a small radius to the Player. |
| Partystarter `vinery:party_effect` | Vinery (port) | N | - | N | N | Y | Miss Lilitus Wine | | | Each hit sets off a firework at the target. | Triggers Fireworks that deal Damage when hitting Entities. |
| Teleport `vinery:teleport` | Vinery (port) | N | - | N | N | Y (pack) | Chorus Wine | | Chorus: total | Random teleport to a safe spot up to 8 blocks away. | PROPOSED: Teleports you to a random safe spot up to 8 blocks away. | Teleports you to a random safe spot up to 8 blocks away. 
| Trippy `vinery:trippy` (no name) | Vinery (port) | N | - | N | N | Y | | | Nausea, Tipsy: slight | Random screen shader; from the echo tools' discombobulate. | Your view keeps switching to a strange screen filter (inverted, pixelated, blurred, wobbling...) until the effect wears off. |
| Water Walker `vinery:water_walker` | Vinery (port) | N | - | N | N | Y | Kelp Cider | | Oiled: total | Walk on water. | Allows Walking on Water. |
| No Cave Maps `xaerominimap:no_cave_maps` | Xaero's Minimap | N | - | N | N | Y | | | No Cave Maps (harmful): total | Server switch: minimap cave mode off. | Disables and hides cave maps on Xaero's Minimap. |
| No Cave Maps `xaerominimap:no_cave_maps_harmful` | Xaero's Minimap | N | - | N | N | Y | | | No Cave Maps: total | Same, harmful category. | Disables and hides cave maps on Xaero's Minimap. |
| No Entity Radar `xaerominimap:no_entity_radar` | Xaero's Minimap | N | - | N | N | Y | | | No Entity Radar (harmful): total | Server switch: radar off. | Disables and hides entities on Xaero's Minimap. |
| No Entity Radar `xaerominimap:no_entity_radar_harmful` | Xaero's Minimap | N | - | N | N | Y | | | No Entity Radar: total | Same, harmful category. | Disables and hides entities on Xaero's Minimap. |
| No Minimap `xaerominimap:no_minimap` | Xaero's Minimap | N | - | N | N | Y | | | No Minimap (harmful): total | Server switch: minimap off. | Disables and hides Xaero's Minimap. |
| No Minimap `xaerominimap:no_minimap_harmful` | Xaero's Minimap | N | - | N | N | Y | | | No Minimap: total | Same, harmful category. | Disables and hides Xaero's Minimap. |
| No Waypoints `xaerominimap:no_waypoints` | Xaero's Minimap | N | - | N | N | Y | | | No Waypoints (harmful): total | Server switch: waypoints off. | Disables and hides waypoints on Xaero's Minimap. |
| No Waypoints `xaerominimap:no_waypoints_harmful` | Xaero's Minimap | N | - | N | N | Y | | | No Waypoints: total | Same, harmful category. | Disables and hides waypoints on Xaero's Minimap. |
| No WM Cave Maps `xaeroworldmap:no_cave_maps` | Xaero's World Map | N | - | N | N | N | | | No WM Cave Maps (harmful): total | Server switch: world map cave mode off. | PROPOSED: Disables and hides cave maps on Xaero's World Map. |
| No WM Cave Maps `xaeroworldmap:no_cave_maps_harmful` | Xaero's World Map | N | - | N | N | N | | | No WM Cave Maps: total | Same, harmful category. | PROPOSED: Disables and hides cave maps on Xaero's World Map. |
| No World Map `xaeroworldmap:no_world_map` | Xaero's World Map | N | - | N | N | N | | | No World Map (harmful): total | Server switch: world map off. | PROPOSED: Disables and hides Xaero's World Map. |
| No World Map `xaeroworldmap:no_world_map_harmful` | Xaero's World Map | N | - | N | N | N | | | No World Map: total | Same, harmful category. | PROPOSED: Disables and hides Xaero's World Map. |

Not in the table: `vinery:health_effect`, `vinery:luck_effect` and `vinery:resistance_effect`. The Vinery port
dropped them (alcohol.md B7.3); its wines give vanilla Health Boost, Luck and Resistance instead. Botania's brews also
carry vanilla effects; only the six Botania effects are listed under Botania.

### 2.2 Counts

| Mod | Effects | Brewable | JEED description complete |
|---|---|---|---|
| Minecraft | 33 | 29 | 33 |
| Alex's Mobs | 18 | 6 | 16 (2 partial) |
| Better Beacons | 3 | 2 (pack) | 0 |
| Botania | 6 | 6 (Brewery only) | 5 (1 wrong) |
| Brewin' and Chewin' (port) | 4 | 0 | 4 |
| Buzzier Bees | 1 | 0 | 1 (hidden in JEI) |
| Create Confectionery | 2 | 0 | 2 |
| Create Crafts & Additions | 1 | 0 | 0 (partial) |
| Creative Engineering (pack) | 1 | 1 | 0 |
| Ender's Delight | 1 | 0 | 1 |
| Everything Copper | 1 | 0 | 0 |
| Farmer's Delight | 2 | 0 | 1 (1 wrong) |
| Farmer's Respite | 1 | 0 | 0 (partial) |
| Fruits Delight | 18 | 0 | 18 |
| Immersive Engineering | 7 | 0 | 6 (1 partial) |
| Immersive Petroleum | 1 | 0 | 0 |
| Monobank | 1 | 0 | 1 |
| Neapolitan | 6 | 0 | 6 |
| Quark | 1 | 1 | 0 (partial) |
| Supplementaries | 1 | 0 | 1 |
| Untitled Duck Mod | 1 | 1 | 1 |
| Upgrade Aquatic | 4 | 4 | 4 |
| Via Romana | 1 | 0 | 0 |
| Vinery (port) | 13 | 0 | 10 (2 partial, 1 wrong) |
| Xaero's Minimap / World Map | 12 | 0 | 8 |
| **Total** | **140** | **50** (6 of them Brewery only) | **118** |

### 2.3 Overlap groups

The `total` overlaps, grouped. Each is two ways to get the same result.

| Group | Effects | How they differ |
|---|---|---|
| Knockback resistance | Knockback Resistance (AM), Resilience (Quark) | Identical (+0.5 per level). Both brewable: two potion lines for one stat; Quark's is named "Stability" |
| No phantoms | Phantom Bane, Caffeinated, Restfulness | Phantom Bane blocks the spawn check. Caffeinated keeps resetting time since rest every tick, so the result is the same while it lasts. Restfulness resets it once |
| Wall climbing | Agility (Neapolitan), Climbing (Vinery) | Both make any wall a ladder |
| Random teleport | Chorus (Fruits Delight), Teleport (Vinery) | Both one random hop within 8 blocks |
| Immobile | Scared Still, Power Outage, Shocked | All three zero your speed; Power Outage only exists on April Fools |
| Status immunity | Stimulation = Refreshing + Sweetening | Stimulation clears both Slowness and Mining Fatigue |
| Poison immunity | Poison Resistance (AM) inside Recovering (Fruits Delight) | Recovering also covers Wither |
| Golden apple | Jellie = Regeneration + Absorption | Same healing rate and the same 4 absorption per level |
| Jump | Double Jump contains Jump Boost | Same jump height, plus the mid-air jump |
| Floating on water | Oiled (AM), Water Walker (Vinery) | Oiled also floats you in rain; Water Walker stands you on the surface |
| Underwater | Conduit Power contains Water Breathing, Night Vision, Haste | Vanilla |
| Xaero switches | each `*_harmful` and its neutral twin | Same switch, different category |

### 2.4 Category audit (harmful / helpful colouring)

Every effect has a category: BENEFICIAL, HARMFUL or NEUTRAL. Tooltips colour the effect's name by it: potion and
tipped-arrow tooltips, food tooltips (Farmer's Delight, Quark), the pack's gear lines. HARMFUL is red; BENEFICIAL
and NEUTRAL are both blue. AppleSkin, Quark's saturation word and `isBeneficial()` read the category too.

The audit (2026-10-20) asked one question per effect: does the colour match what the effect does to whoever has
it?

- Six didn't. CEConfig `[effects] categoryOverrides` changes them (patcherMod `MobEffectCategoryMixin`).
- Mixed effects keep their mod's category; the Note column says why.
- Vanilla keeps vanilla's choices.
- NEUTRAL and BENEFICIAL show the same blue, so nothing was moved between them.
- Own category is from `/dumppotions` 2026-10-18, and the ports' sources for Vinery and Brewin'.

| Effect | Mod | Own category | Colour | Pack | Note |
|---|---|---|---|---|---|
| Speed `minecraft:speed` | Minecraft | BENEFICIAL | blue | |  |
| Slowness `minecraft:slowness` | Minecraft | HARMFUL | red | |  |
| Haste `minecraft:haste` | Minecraft | BENEFICIAL | blue | |  |
| Mining Fatigue `minecraft:mining_fatigue` | Minecraft | HARMFUL | red | |  |
| Strength `minecraft:strength` | Minecraft | BENEFICIAL | blue | |  |
| Instant Health `minecraft:instant_health` | Minecraft | BENEFICIAL | blue | |  |
| Instant Damage `minecraft:instant_damage` | Minecraft | HARMFUL | red | |  |
| Jump Boost `minecraft:jump_boost` | Minecraft | BENEFICIAL | blue | |  |
| Nausea `minecraft:nausea` | Minecraft | HARMFUL | red | |  |
| Regeneration `minecraft:regeneration` | Minecraft | BENEFICIAL | blue | |  |
| Resistance `minecraft:resistance` | Minecraft | BENEFICIAL | blue | |  |
| Fire Resistance `minecraft:fire_resistance` | Minecraft | BENEFICIAL | blue | |  |
| Water Breathing `minecraft:water_breathing` | Minecraft | BENEFICIAL | blue | |  |
| Invisibility `minecraft:invisibility` | Minecraft | BENEFICIAL | blue | |  |
| Blindness `minecraft:blindness` | Minecraft | HARMFUL | red | |  |
| Night Vision `minecraft:night_vision` | Minecraft | BENEFICIAL | blue | |  |
| Hunger `minecraft:hunger` | Minecraft | HARMFUL | red | |  |
| Weakness `minecraft:weakness` | Minecraft | HARMFUL | red | |  |
| Poison `minecraft:poison` | Minecraft | HARMFUL | red | |  |
| Wither `minecraft:wither` | Minecraft | HARMFUL | red | |  |
| Health Boost `minecraft:health_boost` | Minecraft | BENEFICIAL | blue | |  |
| Absorption `minecraft:absorption` | Minecraft | BENEFICIAL | blue | |  |
| Saturation `minecraft:saturation` | Minecraft | BENEFICIAL | blue | |  |
| Glowing `minecraft:glowing` | Minecraft | NEUTRAL | blue | | Vanilla; kept as vanilla has it |
| Levitation `minecraft:levitation` | Minecraft | HARMFUL | red | | Vanilla; kept |
| Luck `minecraft:luck` | Minecraft | BENEFICIAL | blue | |  |
| Bad Luck `minecraft:unluck` | Minecraft | HARMFUL | red | |  |
| Slow Falling `minecraft:slow_falling` | Minecraft | BENEFICIAL | blue | |  |
| Conduit Power `minecraft:conduit_power` | Minecraft | BENEFICIAL | blue | |  |
| Dolphin's Grace `minecraft:dolphins_grace` | Minecraft | BENEFICIAL | blue | |  |
| Bad Omen `minecraft:bad_omen` | Minecraft | NEUTRAL | blue | | Vanilla; kept |
| Hero of the Village `minecraft:hero_of_the_village` | Minecraft | BENEFICIAL | blue | |  |
| Darkness `minecraft:darkness` | Minecraft | HARMFUL | red | |  |
| Bug Pheromones `alexsmobs:bug_pheromones` | Alex's Mobs | BENEFICIAL | blue | |  |
| Clinging `alexsmobs:clinging` | Alex's Mobs | BENEFICIAL | blue | |  |
| Debilitating Sting `alexsmobs:debilitating_sting` | Alex's Mobs | NEUTRAL | blue | **HARMFUL** (red) | Magic damage down to half health; paralyses arthropods |
| Earthquake `alexsmobs:earthquake` | Alex's Mobs | HARMFUL | red | |  |
| Ender Flu `alexsmobs:ender_flu` | Alex's Mobs | HARMFUL | red | |  |
| Exsanguination `alexsmobs:exsanguination` | Alex's Mobs | HARMFUL | red | |  |
| Scared Still `alexsmobs:fear` | Alex's Mobs | NEUTRAL | blue | **HARMFUL** (red) | You can't move or jump (a Tiger, the dragon shovel) |
| Fleet-Footed `alexsmobs:fleet_footed` | Alex's Mobs | BENEFICIAL | blue | |  |
| Knockback Resistance `alexsmobs:knockback_resistance` | Alex's Mobs | BENEFICIAL | blue | |  |
| Lava Vision `alexsmobs:lava_vision` | Alex's Mobs | BENEFICIAL | blue | |  |
| Oiled `alexsmobs:oiled` | Alex's Mobs | BENEFICIAL | blue | |  |
| Orca's Might `alexsmobs:orcas_might` | Alex's Mobs | BENEFICIAL | blue | |  |
| Poison Resistance `alexsmobs:poison_resistance` | Alex's Mobs | BENEFICIAL | blue | |  |
| Power Outage `alexsmobs:power_down` | Alex's Mobs | NEUTRAL | blue | **HARMFUL** (red) | You can't move or jump |
| Soulsteal `alexsmobs:soulsteal` | Alex's Mobs | BENEFICIAL | blue | |  |
| Sunbird's Blessing `alexsmobs:sunbird_blessing` | Alex's Mobs | BENEFICIAL | blue | |  |
| Sunbird's Curse `alexsmobs:sunbird_curse` | Alex's Mobs | HARMFUL | red | |  |
| Tiger's Blessing `alexsmobs:tigers_blessing` | Alex's Mobs | BENEFICIAL | blue | |  |
| Long Reach `better_beacons:long_reach` | Better Beacons | BENEFICIAL | blue | |  |
| Patrol Nullifier `better_beacons:patrol_nullifier` | Better Beacons | BENEFICIAL | blue | |  |
| Phantom Bane `better_beacons:phantom_bane` | Better Beacons | BENEFICIAL | blue | |  |
| Allure `botania:allure` | Botania | BENEFICIAL | blue | |  |
| Bloodthirst `botania:bloodthirst` | Botania | BENEFICIAL | blue | | Mixed: monsters spawn in any light; you brew it on purpose (mob farms). Kept blue |
| Absolution `botania:clear` | Botania | NEUTRAL | blue | | Removes every effect, good and bad |
| Emptiness `botania:emptiness` | Botania | BENEFICIAL | blue | |  |
| Feather Feet `botania:feather_feet` | Botania | BENEFICIAL | blue | |  |
| Soul Cross `botania:soul_cross` | Botania | BENEFICIAL | blue | |  |
| Intoxication `brewinandchewin:intoxication` | Brewin' and Chewin' (port) | HARMFUL | red | | Hidden; AppleSkin and the Intoxicating Drink line rely on it being harmful |
| Raging `brewinandchewin:raging` | Brewin' and Chewin' (port) | BENEFICIAL | blue | |  |
| Sweet Heart `brewinandchewin:sweet_heart` | Brewin' and Chewin' (port) | BENEFICIAL | blue | |  |
| Tipsy `brewinandchewin:tipsy` | Brewin' and Chewin' (port) | NEUTRAL | blue | | Mixed: impairs you but delays damage taken. Kept blue |
| Sunny `buzzier_bees:sunny` | Buzzier Bees | BENEFICIAL | blue | | Hidden in JEI |
| Rest `create_confectionery:rest` | Create Confectionery | HARMFUL | red | **BENEFICIAL** (blue) | Only clears phantoms near you; nothing bad about it |
| Stimulation `create_confectionery:stimulation` | Create Confectionery | BENEFICIAL | blue | |  |
| Shocked `createaddition:shocking` | Create Crafts & Additions | HARMFUL | red | |  |
| Magic Resistance `creativeengineering:magic_resistance` | Creative Engineering (pack) | BENEFICIAL | blue | |  |
| Ender Phasing `endersdelight:phasing` | Ender's Delight | BENEFICIAL | blue | | Mixed: water hurts you, you dodge hits by teleporting. Kept blue |
| Copper Poisoning `everythingcopper:copper_poisoning` (no name) | Everything Copper | HARMFUL | red | |  |
| Comfort `farmersdelight:comfort` | Farmer's Delight | BENEFICIAL | blue | |  |
| Nourishment `farmersdelight:nourishment` | Farmer's Delight | BENEFICIAL | blue | |  |
| Caffeinated `farmersrespite:caffeinated` | Farmer's Respite | BENEFICIAL | blue | |  |
| Alienating `fruitsdelight:alienating` | Fruits Delight | BENEFICIAL | blue | |  |
| Appetizing `fruitsdelight:appetizing` | Fruits Delight | BENEFICIAL | blue | |  |
| Astringent `fruitsdelight:astringent` | Fruits Delight | NEUTRAL | blue | | Mixed: no sliding on ice or slime |
| Brightening `fruitsdelight:brightening` | Fruits Delight | BENEFICIAL | blue | |  |
| Chorus `fruitsdelight:chorus` | Fruits Delight | NEUTRAL | blue | | Random hop |
| Cycling `fruitsdelight:cycling` | Fruits Delight | BENEFICIAL | blue | | Mixed: turns levels into orbs (Mending). Kept blue |
| Digesting `fruitsdelight:digesting` | Fruits Delight | BENEFICIAL | blue | |  |
| Disgusted `fruitsdelight:disgusted` | Fruits Delight | HARMFUL | red | | Put on mobs by Alienating |
| Heal Aura `fruitsdelight:heal_aura` | Fruits Delight | BENEFICIAL | blue | |  |
| Leaf Piercing `fruitsdelight:leaf_piercing` | Fruits Delight | BENEFICIAL | blue | |  |
| Lozenge `fruitsdelight:lozenge` | Fruits Delight | BENEFICIAL | blue | |  |
| Rage Aura `fruitsdelight:rage_aura` | Fruits Delight | NEUTRAL | blue | | Mixed: pulls idle mobs to you (taunt). Kept blue |
| Recovering `fruitsdelight:recovering` | Fruits Delight | BENEFICIAL | blue | |  |
| Refreshing `fruitsdelight:refreshing` | Fruits Delight | BENEFICIAL | blue | |  |
| Shrinking `fruitsdelight:shrinking` | Fruits Delight | BENEFICIAL | blue | |  |
| Sliding `fruitsdelight:sliding` | Fruits Delight | NEUTRAL | blue | | Boats slide on land |
| Suspicious Smell `fruitsdelight:suspicious_smell` | Fruits Delight | BENEFICIAL | blue | |  |
| Sweetening `fruitsdelight:sweetening` | Fruits Delight | BENEFICIAL | blue | |  |
| Concrete Feet `immersiveengineering:concrete_feet` | Immersive Engineering | HARMFUL | red | |  |
| Conductive `immersiveengineering:conductive` | Immersive Engineering | HARMFUL | red | | Drawback on copper / iron / gold armor sets |
| Flammable `immersiveengineering:flammable` | Immersive Engineering | HARMFUL | red | | Drawback on wooden armor |
| Flashed `immersiveengineering:flashed` | Immersive Engineering | HARMFUL | red | |  |
| Slippery `immersiveengineering:slippery` | Immersive Engineering | HARMFUL | red | |  |
| Sticky `immersiveengineering:sticky` | Immersive Engineering | HARMFUL | red | |  |
| Stunned `immersiveengineering:stunned` | Immersive Engineering | HARMFUL | red | | Put on endermen |
| Anti-Fire `immersivepetroleum:anti_fire` (no name) | Immersive Petroleum | BENEFICIAL | blue | |  |
| Thief `monobank:thief` | Monobank | HARMFUL | red | |  |
| Agility `neapolitan:agility` | Neapolitan | NEUTRAL | blue | | Climb walls (Climbing is blue too) |
| Berserking `neapolitan:berserking` | Neapolitan | BENEFICIAL | blue | |  |
| Harmony `neapolitan:harmony` | Neapolitan | BENEFICIAL | blue | |  |
| Slipping `neapolitan:slipping` | Neapolitan | NEUTRAL | blue | **HARMFUL** (red) | Random shoves; from banana peels, Bananarrows, Plantain Spiders |
| Sugar Rush `neapolitan:sugar_rush` | Neapolitan | NEUTRAL | blue | | Mixed: faster, then slower |
| Vanilla Scent `neapolitan:vanilla_scent` | Neapolitan | BENEFICIAL | blue | |  |
| Resilience `quark:resilience` | Quark | BENEFICIAL | blue | |  |
| Overencumbered `supplementaries:overencumbered` | Supplementaries | HARMFUL | red | |  |
| Intimidation `untitledduckmod:intimidation` | Untitled Duck Mod | BENEFICIAL | blue | |  |
| Insomnia `upgrade_aquatic:insomnia` | Upgrade Aquatic | HARMFUL | red | | Brings phantoms sooner |
| Repellence `upgrade_aquatic:repellence` | Upgrade Aquatic | NEUTRAL | blue | | Poisons mobs that touch you |
| Restfulness `upgrade_aquatic:restfulness` | Upgrade Aquatic | BENEFICIAL | blue | |  |
| Vibing `upgrade_aquatic:vibing` | Upgrade Aquatic | NEUTRAL | blue | | Heals mobs that touch you (for pets) |
| Travel Fatigue `via_romana:travellers_fatigue` | Via Romana | HARMFUL | red | | Fast-travel cooldown |
| Armor `vinery:armor_effect` | Vinery (port) | BENEFICIAL | blue | |  |
| Climbing `vinery:climbing_effect` | Vinery (port) | BENEFICIAL | blue | |  |
| Explosion `vinery:creeper_effect` | Vinery (port) | HARMFUL | red | | You explode |
| Double Jump `vinery:double_jump` | Vinery (port) | BENEFICIAL | blue | |  |
| Experience `vinery:experience_effect` | Vinery (port) | BENEFICIAL | blue | |  |
| Frosty Armor `vinery:frosty_armor` | Vinery (port) | NEUTRAL | blue | | Mixed: armor and damage, slower, freezing at II |
| Better than Golden Apples! `vinery:jellie` | Vinery (port) | BENEFICIAL | blue | |  |
| Lava Walker `vinery:lava_walker` | Vinery (port) | BENEFICIAL | blue | |  |
| Magnet `vinery:magnet` | Vinery (port) | BENEFICIAL | blue | |  |
| Partystarter `vinery:party_effect` | Vinery (port) | BENEFICIAL | blue | |  |
| Teleport `vinery:teleport` | Vinery (port) | BENEFICIAL | blue | | Random safe hop |
| Trippy `vinery:trippy` (no name) | Vinery (port) | BENEFICIAL | blue | **HARMFUL** (red) | Screen filter only, inflicted by the dragon shovel; Nausea is red too |
| Water Walker `vinery:water_walker` | Vinery (port) | BENEFICIAL | blue | |  |
| No Cave Maps `xaerominimap:no_cave_maps` | Xaero's Minimap | NEUTRAL | blue | | Server switch; Xaero ships a neutral and a harmful twin on purpose |
| No Cave Maps `xaerominimap:no_cave_maps_harmful` | Xaero's Minimap | HARMFUL | red | | Server switch; Xaero ships a neutral and a harmful twin on purpose |
| No Entity Radar `xaerominimap:no_entity_radar` | Xaero's Minimap | NEUTRAL | blue | | Server switch; Xaero ships a neutral and a harmful twin on purpose |
| No Entity Radar `xaerominimap:no_entity_radar_harmful` | Xaero's Minimap | HARMFUL | red | | Server switch; Xaero ships a neutral and a harmful twin on purpose |
| No Minimap `xaerominimap:no_minimap` | Xaero's Minimap | NEUTRAL | blue | | Server switch; Xaero ships a neutral and a harmful twin on purpose |
| No Minimap `xaerominimap:no_minimap_harmful` | Xaero's Minimap | HARMFUL | red | | Server switch; Xaero ships a neutral and a harmful twin on purpose |
| No Waypoints `xaerominimap:no_waypoints` | Xaero's Minimap | NEUTRAL | blue | | Server switch; Xaero ships a neutral and a harmful twin on purpose |
| No Waypoints `xaerominimap:no_waypoints_harmful` | Xaero's Minimap | HARMFUL | red | | Server switch; Xaero ships a neutral and a harmful twin on purpose |
| No WM Cave Maps `xaeroworldmap:no_cave_maps` | Xaero's World Map | NEUTRAL | blue | | Server switch; Xaero ships a neutral and a harmful twin on purpose |
| No WM Cave Maps `xaeroworldmap:no_cave_maps_harmful` | Xaero's World Map | HARMFUL | red | | Server switch; Xaero ships a neutral and a harmful twin on purpose |
| No World Map `xaeroworldmap:no_world_map` | Xaero's World Map | NEUTRAL | blue | | Server switch; Xaero ships a neutral and a harmful twin on purpose |
| No World Map `xaeroworldmap:no_world_map_harmful` | Xaero's World Map | HARMFUL | red | | Server switch; Xaero ships a neutral and a harmful twin on purpose |

## 3. What the pack changes

- **Potions for effects that had none** ([potions.md](potions.md) 2.1, 2.2): 12 vanilla effects (`kubejs:*`), Long Reach,
  Phantom Bane and Magic Resistance (`creativeengineering:*`), Haste from Rustic Delight, Luck and Bad Luck mixes. Those
  give the Brew, Arrow and Fluid columns their `Y (pack)` entries.
- **Effect potions on every machine** ([potions.md](potions.md)): any potion above is a `create:potion` fluid, bottled,
  splashed, lingered and tipped by machine.
- **Magic Resistance** ([armor_effects.md](armor_effects.md) 2.5): the pack's one own effect, registered in
  [magic_resistance.js](../kubejs/startup_scripts/magic/magic_resistance.js); the damage cut is patcherMod's
  `MagicResistanceEffect`.
- **Effect descriptions, one place:** every pack-side effect text is in
  [creativeengineering/lang/en_us.json](../kubejs/assets/creativeengineering/lang/en_us.json). The KubeJS pack sits
  above the mod jars, JEED's bundle and the Vinery donor jar, so a key there wins. It holds three kinds of line:
  - **Placeholders:** [gen_effect_descriptions.py](../tools/gen_effect_descriptions.py) writes
    `TODO: write a description for X` for a registered effect with no description anywhere. 10 are left (F2). The
    PROPOSED texts in 2.1 are there to replace them once you approve them.
  - **Ported:** before a placeholder is written, the script uses a `PORTED` text if there is one, taken from another
    effect-description mod. `--sources` lists what each mod says about the effects still on a placeholder. The
    sources checked on 2026-10-20 (jars in the git-ignored `tools/.cache/effect_desc_sources/`):
    - JEED 1.16.5-1.6, 1.19.2-1.18, 1.20-2.2.5 and 1.21-2.3.5;
    - MEED 8.1.0, Effect Insights 26.3.1, Effect Descriptions 8.0.2 and Potion Descriptions 1.8.
    Only Travel Fatigue was found ("Prevents fast travel.", Effect Insights).
  - **Fixes:** hand-written replacements for wrong, partial or misspelt texts (F3). The script leaves these alone.
  The ports' own effects (Vinery from the 1.5.5 donor jar, `trippy` from the port's lang, Brewin' from 4.5.0) already
  had right texts, except Teleport, Double Jump and Frosty Armor, which are fixed here too (TODO `ui/effects #3`).
- **JEED item lists** ([jeed_providers.js](../kubejs/server_scripts/other/jeed_providers.js)): one
  `jeed:effect_provider` recipe per effect, through `R.jeedProvider`.
  - **What JEED finds by itself** (`EffectInfo.buildStaticCache`): every food's `FoodProperties` effects (KubeJS food
    edits included), suspicious stew per flower, the beacon for vanilla beacon effects, every potion through its
    `jeed:potion_provider`, and its own 20 vanilla recipes (spawn eggs, conduit, turtle helmet, totem).
  - **What the file adds**, in the same kinds:
    - spawn eggs of mobs that inflict an effect (vanilla, Alex's Mobs, UA, Quark, Neapolitan);
    - blocks, fluids and gear: IE fluid buckets and the chemthrower, tesla coils, the beacon for Better Beacons, the
      Monobank, signs for Via Romana;
    - food and drink that applies its effect in code: Create Confectionery chocolate, Gambler's and Builder's Tea,
      chocolate coins, Abnormals Delight drinks, Fish Oil, Supplementaries candy and pancakes;
    - the pack's gear, read from `global.armorEffects` / `global.toolEffects`, plus the echo armor set;
    - Botania brews (vial, flask, incense stick, blood pendant, by `brewKey` NBT);
    - Vinery wines (moved here from `alcohol.js`) and the Vinery standard banner's configured effect;
    - the command block for the 12 Xaero's switches.
  - **Potion carriers:** a `jeed:potion_provider` for the potion buckets, potion bundles, the IE potion bullet and
    PneumaticCraft gun ammo, so each appears filled with every potion of the effect.
  - **Coverage:** every effect but hidden Sunny now has at least one item on its JEI page. 83 provider recipes; the
    rest are covered by JEED itself. `effect_audit.py` reports any that lose them.
  - **Helper:** `R.jeedProvider` now takes NBT items, drops ids that don't exist (one bad id makes JEED drop the whole
    recipe) and skips unregistered effects. `R.jeedPotionProvider` is new.
- **Category overrides** (2.4): CEConfig `[effects] categoryOverrides`, patcherMod `potion/EffectCategories` +
  `mixin/MobEffectCategoryMixin` (`getCategory()` and `isBeneficial()`). It is a common config, read live.
- **Effects on gear:** [armor_effects.md](armor_effects.md) (set bonuses, Night Vision + Phantom Bane on echo armor,
  dragon gear).

## 4. Limits and decisions

### Findings (2026-10-19)

- **F1. Three things show a raw lang key in game.**
  - `vinery:trippy`: the port kept the effect (D3) but not its name. "Sensory Perception" only existed in the 1.3.10
    jar, and neither the 1.5.5 donor jar nor the port's `vinery_backport` lang has `effect.vinery.trippy`. Add it to
    the port's lang.
  - `everythingcopper:copper_poisoning`: the mod ships no name. Add `effect.everythingcopper.copper_poisoning` to the
    KubeJS lang.
  - Untitled Duck Mod's Intimidation tipped arrow (base and long): no `item.minecraft.tipped_arrow.effect.intimidation`.
    The potion, splash and lingering names exist. Add "Arrow of Intimidation".
  - (`immersivepetroleum:anti_fire` has an empty name too, but it's hidden from the HUD and inventory, so nobody sees
    it.)
- **F2.** 2026-10-20: 10 left on `TODO`, kept as placeholders (your call). No other effect-description mod describes
  them, except Travel Fatigue, now ported (section 3). Copper Poisoning has a placeholder now (`EXTRA_EFFECTS`).
  **11 effects have no real JEI description:** Long Reach, Patrol Nullifier, Phantom Bane, Magic Resistance,
  Anti-Fire, Travel Fatigue, the 4 Xaero World Map switches (all `TODO` placeholders) and Copper Poisoning (nothing at
  all: with no name key the generator can't see it). PROPOSED text for each is in 2.1.
- **F3.** FIXED 2026-10-20: all 11 plus the 5 typo lines (Sticky too) replaced in the pack's lang overlay, with the
  2.1 texts. **11 descriptions are wrong or incomplete:**
  - Wrong: Comfort (JEED describes an older Farmer's Delight that cleared Slowness/Hunger/Weakness; 1.2.4 only heals
    slowly), Soul Cross (5% of the killed mob's health, not yours), Teleport (random direction, not "forward").
  - Partial: Knockback Resistance and Resilience (level II is full immunity), Power Outage (joke text), Shocked (full
    stop, not "reduced"), Flashed (leaves out the slow), Caffeinated (leaves out the phantom reset), Double Jump
    (leaves out the Jump Boost part), Frosty Armor (leaves out the freezing).
  - Typos in JEED's own text: "hearths" (Absorption, Health Boost), "Negated damaged cause by falling" (Feather Feet),
    "slows do" (Sticky), "mob natural mob spawning" (Emptiness). Overriding a JEED text is one lang key in the pack's
    overlay.
- **F4.** FIXED 2026-10-20: `gen_effect_descriptions.py` writes and keeps placeholders only for registered effects
  (`mob_effects.json`), so the 7 stale ones are gone. **Placeholder bookkeeping is stale.** The overlay has 17 `TODO` rows: 10 for live effects (F2) and 7 for effects
  that don't exist (`essence:wet`, `warbrode:wet`, four `xaerominimap:*_beneficial`,
  `xaeroworldmap:no_world_map_beneficial`). The generator keeps those because some jar still names them. The old TODO.md said
  47 left, todo.txt about 70; [TODO](todo/TODO.md) `ui/effects #1` now carries the 17.
- **F5. 12 total-overlap groups (2.3). Your call** which to keep. The two that cost something: Resilience and Knockback
  Resistance are two brewable potion lines for one stat (drop Quark's crab brew or AM's bear-fur brew), and Caffeinated
  gives the whole of Phantom Bane for free from tea and coffee, which undercuts the Phantom Bane potion and the echo
  armor set bonus.
- **F6. Luck is brewed twice.** Buzzier Bees 5.0.1 already registers awkward + four-leaf clover → Luck, Luck + redstone
  → long, + glowstone → strong, + fermented spider eye → Bad Luck, and Bad Luck + redstone → long, + glowstone → strong
  (`BBMobEffects.registerRecipes`, all real `PotionBrewing.Mix` entries via Blueprint `DataUtil.addMix`; checked
  against the 5.0.1 jar 2026-10-21). The pack's `LUCK_POTION_MIXES` rows 1, 2 and 4 in `potions.js` add three of
  those again, which probably shows twice in JEI's brewing and Automatic Brewing pages (EF-05). potions.md 2.3 says
  "Luck had no brew"; that's out of date. Only the silverfish bottle → Bad Luck row (row 3) is new.
- **F7.** DECIDED 2026-10-20: keep it hidden (your call: "hide sunny"). The setting is in both `config/` and
  `configs_edited/root_config/`. It still shows on the HUD from buttercup stew and mooblooms; removing it from the game
  is the separate "purge sunny" item in section 7. **Sunny is hidden from JEI. Your call.** `config/jeed-client.toml` `hidden_effects = ["buzzier_bees:sunny"]`. If
  that was deliberate (it's a mob-side effect), fine; otherwise remove it.
- **F8.** DECIDED 2026-10-20: remove it. Passion fruit isn't in the game, and the drink is already hidden in JEI
  (TODO `ui/effects #6`). **Passion Aloe Nectar does nothing.** It gives Atmospheric's Relief effect, and Atmospheric isn't installed. The
  drink still feeds you.
- **F9.** FIXED 2026-10-19 (header now points at `MagicResistancePotions` and `CE_POTION_MIXES`). **Stale comment:** [magic_resistance.js](../kubejs/startup_scripts/magic/magic_resistance.js) lines 19-23 say
  Magic Resistance has no potion; patcherMod's `MagicResistancePotions` added base, long and strong.
- **F10. Exports predate the ports.** `mob_effects.json`, `potions.json` and `foods.json` are from 2026-10-18 18:30; the
  Vinery and Brewin ports went into `mods/` at 2026-10-19 00:50. The table uses the port sources for those two mods
  and `effect_audit.py` drops the three removed Vinery effects (`EFFECT_OVERRIDES`). Rerun `/dumppotions` and
  `/dumpfood`, then empty `EFFECT_OVERRIDES`.
- **F11.** FIXED 2026-10-20, and the finding was partly wrong. JEED does list every `FoodProperties` food by itself
  (`EffectInfo.buildStaticCache`). What it missed were code-applied foods, mobs, blocks, gear and brews; section 3
  adds them all. **Food sources are partial in JEI.** JEED lists items that give an effect only for vanilla effects (its own 20
  provider recipes) and the Vinery wines. The Food column here has the rest (from `/dumpfood`); a generated
  `jeed:effect_provider` set from `foods.json` would put them in JEI too.
- **F12. Unbrewable beneficial effects. Your call** whether any should get a potion: Fleet-Footed, Orca's Might, Oiled,
  Agility/Climbing, Water Walker, Lava Walker, Magnet, Experience, Allure. The 18 Fruits Delight effects, Neapolitan and
  Vinery are food-only by design.
- **F13.** FIXED 2026-10-20: CEConfig `[effects]` makes it BENEFICIAL (blue), with 5 more recolours from the 2.4
  audit. **Rest is filed as harmful.** Create Confectionery registers it `HARMFUL`, so it shows red, although all it does
  is remove nearby phantoms. Cosmetic.

### Known limits

- **Provider sources were read from the effect-owning mods' code** (the 25 mods in 2.1), plus vanilla and the
  code-applied foods a bytecode scan of every jar found (`finishUsingItem` / `affectConsumer` calling `addEffect`).
  Other mods' mobs that inflict vanilla effects aren't listed (e.g. a mod's venomous spider giving Poison).
- **Botania's brew table is copied** from `BotaniaBrews.registerBrews` (440.2), not read at runtime; a Botania update
  that adds a brew needs a row.
- **Category overrides change the colour everywhere the category is read**, including AppleSkin and Quark's
  saturation word on foods that give them (none of the six comes from a food).

- **Values are read from code at these versions.** A mod update can change any of them; `effect_audit.py` only checks
  names and descriptions, not behaviour.
- **Suspicious stew effects** (flowers) and mob-applied effects aren't in the Food column.
- **Botania's Brewery** brews vanilla effects too; the Brew column only counts the brewing stand for those.
- **Code-applied drinks:** Gambler's Tea, Builder's Tea, the Abnormals Delight drinks and the Create Confectionery
  chocolate apply their effects in code, so `/dumpfood` misses them; they were added to the Food column by hand.

### Rejected options

- **One table per mod:** easier to read, but you asked for one table with the mod as a column, and one table sorts and
  searches as a whole.
- **Generating the table:** the behaviour, overlap and description columns are judgement, not data. The mechanical
  columns are checked by `effect_audit.py` instead.

## 5. File map

- [tools/effect_audit.py](../tools/effect_audit.py): names, descriptions, placeholders, potion and arrow names, JEI sources.
- [tools/gen_effect_descriptions.py](../tools/gen_effect_descriptions.py): writes the `TODO` placeholders and `PORTED` texts; `--sources` searches other description mods (`tools/.cache/effect_desc_sources/`, git-ignored).
- [kubejs/assets/creativeengineering/lang/en_us.json](../kubejs/assets/creativeengineering/lang/en_us.json): description overlay (placeholders, ported and fixed text).
- [kubejs/server_scripts/other/jeed_providers.js](../kubejs/server_scripts/other/jeed_providers.js): every `jeed:effect_provider` / `jeed:potion_provider` recipe.
- [neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js) `jeedProvider`, `jeedPotionProvider`: the recipe helpers.
- [EffectCategories.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/potion/EffectCategories.java), [MobEffectCategoryMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/MobEffectCategoryMixin.java), CEConfig `[effects]`: category overrides.
- [kubejs/assets/kubejs/lang/en_us.json](../kubejs/assets/kubejs/lang/en_us.json): potion and arrow names for the `kubejs:*` and `creativeengineering:*` potions.
- [kubejs/startup_scripts/cooking/potions.js](../kubejs/startup_scripts/cooking/potions.js): pack potions and brewing mixes.
- [kubejs/startup_scripts/magic/magic_resistance.js](../kubejs/startup_scripts/magic/magic_resistance.js): the pack's effect.
- [kubejs/server_scripts/_lib/dump_potions.js](../kubejs/server_scripts/_lib/dump_potions.js) (`/dumppotions`) → `kubejs/exported/mob_effects.json`, `potions.json`.
- [kubejs/server_scripts/_lib/dump_food.js](../kubejs/server_scripts/_lib/dump_food.js) (`/dumpfood`) → `kubejs/exported/food/foods.json`.
- [config/vinery-common.toml](../config/vinery-common.toml) `effects`: wine → effect table.
- `config/jeed-client.toml`: `hidden_effects`.
- Port sources: [MobEffectRegistry.java](../development/backportedVinery/src/main/java/net/satisfy/vinery/core/registry/MobEffectRegistry.java), [BnCEffects.java](../development/backportedBrewin/src/main/java/umpaz/brewinandchewin/common/registry/BnCEffects.java).

## 6. Offline checks

- `python3 tools/effect_audit.py`: 140 effects, 44 carried by a potion, 120 potions. 23 problems on 2026-10-19 (F1,
  F2, F4); 15 on 2026-10-20: the 10 kept placeholders and the F1 names and arrows. Each fix should take its line off
  the list.
  - **`no JEI source`** (new 2026-10-20) flags an effect with no item on its JEI page. It counts:
    - food (`foods.json`), potions and the vanilla beacon effects;
    - JEED's own recipes, the effect ids written in `jeed_providers.js`;
    - the gear registries and the Vinery wine config.
    Hidden effects are exempt. None on 2026-10-20.
  - `--json` prints the per-effect table: name, category, description and source, potions with their four item names,
    `jei_sources`, `hidden_in_jei`.
- `python3 tools/gen_effect_descriptions.py --sources`: what the other effect-description mods say about each effect
  still on a placeholder. Writes nothing.
- After a boot: `/dumppotions` then the audit; the effect count should be 140 with `EFFECT_OVERRIDES` emptied.

## 7. Changes

- add intimidation arrow name
- describe soul cross more
- examine 2 luck brews, overlap is ok if both ingredients unique/very similar
- fold resilience into kb resistance
- caffinated adds speed too?
  - if not, add. caffinated and phantom bane coexist, caffinated is superset of speed and phantom bane
    - implement phantom bane in caffinated same way as beacon/armor phantom bane, if that is better for performance 
    - purge sunny from game (w/o patching the mod itself, license)
    - include the unbrewable effects in the writeup, but keep the brewing limited to the wine until the effect shuffle

## 8. In-game test suite

Creative, `/effect give @s <id> 60 <amp>` for effects without an item. JEI: the effect list is under the JEED
ingredient type.

- [ ] **EF-01 Raw names (F1):** `/effect give @s vinery:trippy` and `everythingcopper:copper_poisoning` → the HUD shows
  the raw `effect.*` key. Search JEI for "Arrow of" → the Intimidation arrows show a raw key. All three show a name
  once F1 is fixed.
- [ ] **EF-02 JEED placeholders (F2):** JEI → Long Reach → the page reads "TODO: write a description for Long Reach."
- [ ] **EF-03 Comfort (F3):** survival, saturation empty (AppleSkin), hurt, no Regeneration, eat Beef Stew → half a
  heart every 4 s; no Slowness/Hunger/Weakness removal (`/effect give @s slowness` first, it stays).
- [ ] **EF-04 Soul Cross (F3):** with Soul Cross, kill a cow (10 HP) → heal 0.5 HP; kill an iron golem (100 HP) → heal 5.
- [ ] **EF-05 Luck duplicate (F6):** JEI → Potion of Luck → R: count the awkward + four-leaf clover recipes on the
  brewing page and on Automatic Brewing. Two of each confirms F6.
- [ ] **EF-06 Resilience = Knockback Resistance (F5):** drink Potion of Stability, get hit by a zombie → half knockback;
  add Knockback Resistance (AM) → none. Strong Stability alone → none.
- [ ] **EF-07 Caffeinated vs phantoms (F5):** stay awake 3+ nights drinking Black Tea before each night → no phantoms.
- [ ] **EF-08 Shocked:** stand next to an active Tesla coil → you can't walk while Shocked.
- [ ] **EF-09 Teleport direction (F3):** drink Chorus Wine five times on a flat field → you land in random directions,
  within 8 blocks.
- [ ] **EF-10 Double Jump:** drink Clark Wine → jump height as with Jump Boost I; press jump in mid-air → second jump.
- [ ] **EF-11 Sunny in JEI (F7):** search JEI for "Sunny" → no effect entry.
- [ ] **EF-12 Passion Aloe Nectar (F8):** after its removal, `/give @s abnormals_delight:passion_aloe_nectar` fails
  (or gives nothing) and no recipe in JEI uses it.
- [ ] **EF-13 Pack effect potions as fluid:** JEI fluid list → "Potion of Long Reach", "Potion of Magic Resistance" and
  "Potion of Intimidation" `create:potion` stacks exist.
- [ ] **EF-14 Heal Aura hits undead:** eat Peach Jam near a zombie → the zombie takes damage every 3 s.
- [ ] **EF-15 JEI item lists:** check `logs/latest.log` for `jeed_providers.js: 83 effect provider recipes` and no
  `jeedProvider` warnings. Then open these pages in JEI:
  - Rest: four Create Confectionery chocolates.
  - Allure: a brew vial, flask, incense stick and blood pendant, all of Allure.
  - Shocked: the Create Crafts & Additions tesla coil.
  - Flammable: the IE and IP fuel buckets, the chemthrower and the four wooden armor pieces.
  - Phantom Bane: the beacon and the four echo armor pieces.
  - Health Boost: the Vinery standard (banner), Solaris and Stal Wine.
  - No Minimap: a command block.
- [ ] **EF-16 Potion carriers:** JEI → Regeneration. The potion bucket (three kinds), potion bundles (three kinds),
  the IE potion bullet and PneumaticCraft gun ammo each show filled with a Regeneration potion.
- [ ] **EF-17 Fixed descriptions:** JEI → Comfort reads "Heals half a heart every 4 seconds..."; Absorption says
  "hearts", not "hearths"; Teleport says "random safe spot"; Travel Fatigue reads "Prevents fast travel."
- [ ] **EF-18 Category colours:** `/give @s create_confectionery:bar_of_white_chocolate`, hover it → Rest is blue.
  Hover the dragon shovel → Sensory Perception and Scared Still are red. In `config/creativeengineering-common.toml`,
  delete the `create_confectionery:rest` line under `[effects]` and save → Rest goes red again with no restart.
- [ ] **EF-19 Sunny stays hidden:** search JEI for "Sunny" → no effect entry (same as EF-11, after the config was
  confirmed).

## 9. History

- 2026-10-19: this writeup: table of all 140 effects (vanilla, 25 mods, the pack), counts, overlap groups, findings
  F1 to F13, tests EF-01 to EF-14; `tools/effect_audit.py`. Behaviour read from decompiled jars and the Vinery and Brewin
  port sources. No game code changed.
- 2026-10-19: F9 fixed (magic_resistance.js header).
- 2026-10-20: the ui/effects TODO rows.
  - JEI item lists for every effect (`jeed_providers.js`; the Vinery wine providers moved there from `alcohol.js`).
  - 16 description fixes, Travel Fatigue ported, stale placeholders gone (F2, F3, F4).
  - Sunny kept hidden (F7).
  - Category audit of all 140 effects with 6 overrides (2.4, F13), in patcherMod 1.1.01, rebuilt.
  - F11 corrected: JEED already lists `FoodProperties` foods.
  - Tests EF-15 to EF-19.
  - F8: Passion Aloe Nectar marked for removal (passion fruit isn't in the game).
