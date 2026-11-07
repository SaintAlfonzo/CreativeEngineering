# Creative Tips content plan (2026-11-06)

Owner request 2026-11-06: more loading screen tips about the pack's own behaviours, as a large multistep job
documented here. Step 1: scan item tooltips, WTHIT lines and JEI info pages and draft tips from what the UI already
documents. TODO row: `ui/loadingtips #3`. Mechanism and tip format: [loading_tips.md](loading_tips.md); current list:
[loading_tips_list.md](loading_tips_list.md).

Status: step 1 (harvest) DONE 2026-11-06, drafts in section 4. Step 2 (owner review) in progress: first pass of
owner notes applied 2026-11-07 (section 6). Nothing from section 4 written to the tip files yet.

## 1. Steps

| # | Step | Status | Output |
|---|---|---|---|
| 1 | Harvest: scan item tooltips, WTHIT providers, JEI info pages; one draft tip per behaviour | DONE 2026-11-06 | section 4 |
| 2 | Owner review: mark each draft keep / edit / drop, decide the open questions in section 3 | In progress (first pass 2026-11-07) | `Verdict` column in section 4 |
| 3 | Fact check every kept draft against code, config and dumps (tooltips can be stale since the 11-04 split) | Open | `Checked` column |
| 4 | Gap harvest: features in `feature_writeup/` and the changelogs with no UI text (seasons, worldgen, mob spawns, loot, structures, Create/IE changes) | Open | section 5 |
| 5 | Write the tips into the pack files (layout decided in section 3, Q1), run `tools/loading_tips_md.py` | Open | tip files, [loading_tips_list.md](loading_tips_list.md) |
| 6 | Build and install the owning jars; LT-03 count check, LT-01 read-through in game | Open | test IDs |
| 7 | Changelog bullet under `## Loading screen tips`; close `ui/loadingtips #3` | Open | `unreleased.md` |
| 8 | Hand the final list to the chat announcer (`ui/loadingtips #2`) | Open | shared list |

## 2. Style rules for tips

- No trailing period (owner 2026-11-07). A tip may hold two sentences; only the final period goes.
- Capitals (owner 2026-11-07): items, blocks and materials lower case (axe, copper, fishing net, salt dust);
  enchantments and effects Title Case (Silk Touch, Fortune, Looting, Bad Omen, Night Vision); mobs, places,
  structures, dimensions, mod names, control keys (Shift, Sprint) and quoted UI labels keep their capitals.
  Applied to every tip in Loading Tips and Creative Tips and to section 4.
- One sentence, at most about 110 characters (the bar wraps at 360 px; two lines read comfortably, three do not).
- Plain English, what the player can do or what is different. No ids, file names, config keys, emoji.
- No "now", "new" or "changed": the player may not know the old behaviour. Say what is true in this pack.
- Name items by their display name. Name a mod only when the player needs it to find the thing (JEI mod filter).
- A tip must stay true when the optional mod it talks about is absent, or ship in that mod's jar (section 3, Q1).

## 3. Open questions (owner)

- **Q1 Layout.** One Creative Tips file in Creative Common, or each Creative jar ships its own tip pack (own namespace,
  e.g. `creativedelight_tips`) so a tip only shows when the jar that makes it true is loaded? Proposed: per jar,
  because Creative Delight, Creative Brewing, Equipment and Calendar are published separately.
- **Q2 MOD rows.** Rows marked MOD explain an upstream mod feature (not a pack change). Keep a share of them as
  general help, or tips about pack changes only? Proposed: keep MOD tips for features players commonly miss.
- **Q3 Weighting.** The deck is a flat shuffle; 322 vanilla tips will drown about 100 pack tips. Add a weight per
  pack (e.g. Creative Tips drawn 1 in 2) or trim the vanilla list?

### 3.1 Changes to make to notes
  - add brackets around key or action inputs, like [shift] or [crouch] or [sprint + jump]
    - allow these to dynamically refer to bound keys?
  
  - conductive
    - add all armors
    - faraday armor note
  - expand broken kettle keeps tea when system is expanded to all containters
  - hoe harvest area tip
  - update kindling list in game and tip
  - CT-079: this is a configurable feature, tooltip disabled with feature in config?
    - identify other tooltips that rely on config state 
  - pickup sticky note and keep data: ensure its a feature, add tooltip
  - only show admin tips to admins
  - 97: flip to be hoe function, where to check in order

## 4. Draft tips from UI text

Harvested 2026-11-06 by three read-only scans (151 item tooltip rows, 38 WTHIT rows, 29 JEI info rows), merged
and deduplicated to the rows below. Section 4.1 is the pack's own changes (PACK), section 4.2 is upstream mod features that a
UI line explains (MOD, see Q2). `UI` = where the text lives: T item tooltip, W WTHIT line, J JEI info page.
Source paths abbreviate `development/` as `dev/` and `kubejs/assets/` as `ka/`. `?` in Note = origin or fact not
confirmed by the scan; check first in step 3. Verdict (step 2): keep / edit / drop. Checked (step 3): date.

Scan notes:
- Crop seasons do not stop growth: fertile seasons add growth ticks, offseason refuses about 25% of growth checks,
  bone meal is not limited ([crops.md](crops.md) 2.12.1). Drafts CT-041 to CT-043 are worded for that.
- Every KubeJS JEI info page and every Creative Delight member page shows only while the `jeiInfoPages` QoL switch
  is on.
- Left out: pure readouts (food values, material stats, sawblade value, slot and serving numbers alone), flavour
  text, renames, WTHIT icon and name fixes, the coaster "(put away)" suffix.

### 4.1 Pack changes (PACK)

| ID | Family | Draft tip | UI | Source | Note | Verdict | Checked |
|---|---|---|---|---|---|---|---|
| CT-001 | Armor sets | Full armor sets give bonuses: diamond gives Speed, gold gives Luck, netherite gives Fire Resistance | T | `dev/creativeEquipment/.../creativeequipment/gear_effects.js:49-62` |  |  |  |
| CT-002 | Armor sets | A full set of iron, copper or gold armor makes you conductive; wooden armor makes you flammable | T | `gear_effects.js:28-50` |  |  |  |
| CT-003 | Armor sets | A full set of felsic armor gives longer reach, higher jumps and resistance to magic damage | T | `gear_effects.js:64-69` |  |  |  |
| CT-004 | Dragon gear | Full dragon armor stacks resistances, removes fall damage and extends your reach | T | `gear_effects.js:78-90` |  |  |  |
| CT-005 | Dragon gear | Dragon horse armor gives your horse Regeneration, extra health and Resistance | T | `gear_effects.js:92-97` |  |  |  |
| CT-006 | Dragon gear | Felsic gear can be bound to dragon scale to upgrade it into dragon gear | T | `ka/creativeequipment/kubejs/tooltips.js:23-31` |  |  |  |
| CT-007 | Dragon gear | Each dragon tool has its own effect: the sword drains life from kills and the axe makes targets float | T | `gear_effects.js:130-153` |  |  |  |
| CT-008 | Dragon gear | A hit from a dragon shovel leaves the target disoriented and confused | T | `gear_effects.js:114-123` |  |  |  |
| CT-009 | Dragon gear | Holding a dragon bow turns you invisible; its arrows slow targets and make them glow | T | `gear_effects.js:147-153` |  |  |  |
| CT-010 | Dragon gear | A dragon hoe can refund the bone meal held in your off hand | T | `ka/creativeequipment/kubejs/tooltips.js:41` |  |  |  |
| CT-011 | Echo gear | A full set of echo armor gives Night Vision and keeps phantoms away | T | `dev/creativeEquipment/.../EchoArmorTooltipMixin.java:8-15` |  |  |  |
| CT-012 | Echo gear | Echo tools can be used for defense too: a hit gives Weakness and Darkness | T | `gear_effects.js:110-128` |  |  |  |
| CT-013 | Shields | Shields come in tiers from copper to dragon; better shields block more kinds of damage | T | `dev/creativeEquipment/.../ShieldTooltip.java:13-91` |  |  |  |
| CT-014 | Shields | Some shields buff you, or curse your attacker, each time you block a hit | T | `ShieldTooltip.java:113-137` |  |  |  |
| CT-015 | Shields | Hold [shift] over a shield to see its full stats, such as whether it blocks fire and magic | T | `ShieldTooltip.java:21-25` |  |  |  |
| CT-016 | Curios | The gold "Slot:" line on an item names the accessory slot it goes in | T | `dev/creativeEquipment/.../CuriosSlotTooltip.java:18-27` |  |  |  |
| CT-017 | Stepstools | Wear a stepstool, stand still and hold [Sprint + Jump] to step up onto it | T | `ka/creativeequipment/kubejs/tooltips.js:12-16` |  |  |  |
| CT-018 | Elytra | Use an air brake to slow down while gliding; go too slow and you stall | T | `ka/creativeequipment/kubejs/tooltips.js:35` |  |  |  |
| CT-019 | Elytra | Craft an elytra with a dragon scale to get a second elytra. The Ender Dragon drops the scales | T | `ka/creativecompat/kubejs/tooltips.js:144` |  |  |  |
| CT-020 | Placeable food | A "Placeable" line on a food means it can go on a table, a plate or a coaster | T | `ka/creativedelight/kubejs/_qol/placeable_tooltip.js` |  |  |  |
| CT-021 | Seasonings | Each seasoning on a dish adds something, from extra nutrition to longer effects | T | `dev/creativeDelight/.../SeasoningTooltip.java:19-104` |  |  |  |
| CT-022 | Cans | A can holds several servings; its tooltip shows how many are left | T | `dev/creativeDelight/.../CanServingsTooltip.java:20` |  |  |  |
| CT-023 | Lunchboxes | Lunchboxes carry food and choose a random contents when eaten; bigger ones have more slots | T | `dev/creativeDelight/.../LunchboxTooltip.java:21` |  |  |  |
| CT-024 | Cooking | Cooking pots, kettles and ovens keep leftover bottles and buckets; empty them or cooking pauses | W | `dev/creativeCommon/.../WthitRemainderBuffer.java:103-110` |  |  |  |
| CT-025 | Cooking | Punch a cooking pot or kettle with an empty hand to toggle its lid or handle instead of breaking it | W | `dev/creativeDelight/.../WthitCookwareToggle.java:33` |  |  |  |
| CT-026 | Cooking | A broken kettle keeps the tea in its tank | T W | `dev/creativeDelight/.../KettleItemTooltipMixin.java:20`, `WthitKettleTank.java:38` |  |  |  |
| CT-028 | Steel oven | The steel oven cooks like a furnace and a smoker, and burns any furnace fuel | T | `ka/creativedelight/kubejs/cookingforblockheads/client.js:4-17` | owner: steel oven; all fuels |  |  |
| CT-029 | Cooking | Hold chopsticks and eat a roll or fish dish from your off hand to make its effects last longer | T | `dev/creativeDelight/members/roll/.../RollTranslator.java:39-42` |  |  |  |
| CT-030 | Farming | Farmer villagers tend crops on rich farmland and fertile soil as well as plain farmland | T | `ka/creativedelight/kubejs/farmersdelight/client.js:25-26` |  |  |  |
| CT-031 | Fishing net | Use a fishing net on kelp or seagrass to find shrimp, coral, caviar, and rarely a heart of the sea | T J | `ka/creativedelight/kubejs/oceanic_delight/client.js:7-11` |  |  |  |
| CT-032 | Fishing net | Use a fishing net on kelp and seagrass in oceans that are not cold to catch shrimp | J | `ka/creativedelight/kubejs/creativeengineering/names.js:15` |  |  |  |
| CT-033 | Oceanic | Caviar drops from salmon killed with a knife, and can be caught in rivers with a fishing net | J | `names.js:13` |  |  |  |
| CT-034 | Jam and jello | Jam blocks stick to jello made from the same jam; jello is slippery and bouncy | T | `dev/creativeDelight/members/vintage/src/own/.../en_us.json:119-120` |  |  |  |
| CT-035 | Jars | One cookie jar can hold several kinds of treat; its tooltip counts each kind | T | `dev/creativeCompat/.../SupplementariesJarItemTooltipMixin.java:14-19` |  |  |  |
| CT-036 | Jars | Up to four mason jars of the same kind place in one block | J | `dev/creativeDelight/members/vintage/.../JEIVintagePlugin.java:49` |  |  |  |
| CT-037 | Salt | Salt builds up on an evaporator one layer at a time, up to 8; each layer breaks into one salt dust | J | `JEIVintagePlugin.java:40` |  |  |  |
| CT-039 | Frogspawn | Frogspawn can only be picked up by breaking it with shears | J | `ka/creativedelight/kubejs/roll_delight/jei_info.js:11-13` |  |  |  |
| CT-040 | Plants | Ghost peppers grow only on soul soil and the richer soul soils | T | `dev/creativeDelight/members/vintage/.../en_us.json:170` |  |  |  |
| CT-041 | Seasons | A seed's tooltip names its fertile season: crops grow faster in season and slower out of it | T W | `dev/creativeCalendar/.../CropSeasonTooltip.java:18-96`, `WthitCropSeasons.java:57-71` |  |  |  |
| CT-042 | Greenhouse | Glass anywhere in the 16 blocks above a crop makes a greenhouse: no offseason slowdown, but no fertile boost | W | `WthitCropSeasons.java:73-80` | glass block, pane, glass door or trapdoor; each crop column needs its own glass (crops.md greenhouse) |  |  |
| CT-043 | Seasons | Grapes follow climate, not seasons: each variety grows best in its own kind of biome | T W | `CropSeasonTooltip.java:50-58` |  |  |  |
| CT-044 | Seasons | Cave crops grow best below y 0; Nether and End crops have their own rules | T W | `CropSeasonTooltip.java`, `WthitCropSeasons.java:57-60` |  |  |  |
| CT-045 | Calendar | Hove over a calendar to see the date, the next season and how many days until it arrives | T | `dev/creativeCalendar/.../TimeClient.java:27-92` |  |  |  |
| CT-046 | Hoes | Right-click ripe crops with to harvest and replant them | T | `dev/creativeDelight/.../HoeHarvestTooltip.java:11-28` | split 2026-11-07: area is CT-097 |  |  |
| CT-047 | Plantable | A green line under an item's name means it can be planted, and says what kind of plant it is | T | `kubejs/client_scripts/plantable_tooltip.js` |  |  |  |
| CT-048 | Watering cans | Watering cans come in six tiers, copper to dragon; higher tiers hold more and cover a wider area | J | `dev/creativeDelight/members/hearth/.../HHJeiPlugin.java:34` |  |  |  |
| CT-049 | Watering cans | Hold fertilizer in your off hand and use a watering can to load it; the can then fertilizes a whole area | J T | `hearthandharvest/lang/en_us.json:16` |  |  |  |
| CT-050 | Watering cans | Refill a watering can from any water source, a well or a water cauldron | J | `hearthandharvest/lang/en_us.json:16` |  |  |  |
| CT-051 | Watering cans | Watering cans soak farmland in their area and put out fires and campfires | J | `hearthandharvest/lang/en_us.json:16` |  |  |  |
| CT-052 | Flower pots | Fill a silt pot with rooted dirt and any plant will grow in it | T W | `ka/creativecompat/kubejs/tooltips.js:242-243`, `WthitPotContents.java:82-86` |  |  |  |
| CT-053 | Flower pots | Planter boxes and flower boxes can hang on walls | T | `ka/creativecompat/kubejs/tooltips.js:238-241` |  |  |  |
| CT-055 | Plants | Shear a vine to stop it from growing | W T | `dev/creativeCommon/.../WthitSheared.java:60-63`, `kubejs/client_scripts/tooltips.js:315` |  |  |  |
| CT-056 | Kegs | Heating casks or ice crates within 2 blocks of a keg warm it up or cool it down | T | `ka/creativebrewing/kubejs/brewinandchewin/tooltips.js:13-21` |  |  |  |
| CT-057 | Kegs | Hot and cold blocks change both a keg's temperature and how fast wine ages | T | `brewinandchewin/lang/en_us.json:156-159` |  |  |  |
| CT-058 | Kegs | Pipes can't drain a keg while it ferments | T | `brewinandchewin/lang/en_us.json:155` |  |  |  |
| CT-059 | Kegs | Drinks marked Intoxicating can make you tipsy | T | `dev/creativeBrewing/.../creativebrewing/lang/en_us.json:6` |  |  |  |
| CT-060 | Wine | Wine ages faster in dark, cold storage and slower in warm spots | T W | `vinery_backport/lang/en_us.json:4-13`, `VineryWthitClient.java:196-201` |  |  |  |
| CT-061 | Wine | Wine bottles come in three sizes, and each size fits only certain racks | T | `vinery_backport/lang/en_us.json:75-87` |  |  |  |
| CT-062 | Vinery | Fill a grapevine pot with one kind of grape and jump on it to press the juice; heavy armor stomps harder | T W | `ka/creativebrewing/kubejs/vinery/tooltips.js:17`, `VineryWthitClient.java:118-130` |  |  |  |
| CT-063 | Vinery | Mash apples in the apple press, then add a wine bottle to make apple juice | T | `vinery/tooltips.js:18` |  |  |  |
| CT-064 | Vinery | A fermentation barrel takes one kind of juice at a time; sneak-click empty-handed to pour it out | T | `vinery/tooltips.js:19-20` |  |  |  |
| CT-065 | Vinery | Plant grapes on a two-high grapevine stem; jungle grapes need a lattice. Shears give the seed back | T | `vinery/tooltips.js:22-25` |  |  |  |
| CT-068 | Potion buckets | Potions also come by the bucket, as plain, splash and lingering | T | `ka/creativebrewing/kubejs/creativeengineering/potion_buckets.js` |  |  |  |
| CT-069 | Wrappist | Wrappist geodes grow in the outer End; the shard brews into a potion of Long Reach | T | `kubejs/client_scripts/tooltips.js:431-438` |  |  |  |
| CT-070 | Mooblooms | Feed a cow the right flower and it may turn into a moobloom | J | `dev/creativeWorldgen/members/mooblooms/.../MoreMoobloomsJeiPlugin.java:110` |  |  |  |
| CT-071 | Mooblooms | Shear a moobloom for its flowers; it grows them back by eating grass | J | `MoreMoobloomsJeiPlugin.java:111` |  |  |  |
| CT-072 | Mooblooms | Shear a bare moobloom again to turn it back into a cow, with a few more flowers | J | `MoreMoobloomsJeiPlugin.java:112` |  |  |  |
| CT-073 | Mooblooms | Mooblooms breed on their own flowers, not wheat | J | `MoreMoobloomsJeiPlugin.java:113-114` |  |  |  |
| CT-075 | Campfires | Campfires never burn out on their own, but rain puts out an unkindled campfire within seconds; kindle fires with haybales or other fuel blocks | W | `dev/creativeCommon/.../WthitCampfire.java:96-110` |  |  |  |
| CT-076 | Campfires | Soul campfires keep burning in the rain | W | `WthitCampfire.java:102-110` |  |  |  |
| CT-077 | Campfires | A campfire kindled on a haybale, coal or charcoal block is safe from rain | W | `WthitCampfire.java:103` |  |  |  |
| CT-078 | Run speed | Surfaces change how fast you walk: smooth, hard ground speeds you up; soft, dusty or sticky ground slows you down | T W | `dev/creativeCompat/.../WthitRunSpeed.java:61-66`, `kubejs/client_scripts/tooltips.js:359` |  |  |  |
| CT-079 | Run speed | Framed and copycat blocks walk like the block inside them | T W | `CamoSpeedTooltip.java:12-16` |  |  |  |
| CT-080 | Run speed | A sticky piston only slows you when its sticky face points up | W | `WthitRunSpeed.java:72-74` |  |  |  |
| CT-081 | Run speed | Carpets and thin snow walk like the block beneath them | W | `WthitRunSpeed.java:68-74` |  |  |  |
| CT-083 | Immersive Engineering | Program a precision sawblade with a blueprint to cut a shape in the sawmill | T | `kubejs/client_scripts/tooltips.js:330-339` |  |  |  |
| CT-084 | Immersive Engineering | Sawdust in a coke oven soaks up creosote oil | T | `kubejs/client_scripts/tooltips.js:325` | ? origin |  |  |
| CT-085 | Create | Create's filters, sails, pulleys and nozzles can be crafted with tough fabric | T | `kubejs/client_scripts/tooltips.js:344-350` |  |  |  |
| CT-086 | Create | Mechanical crafters need gold, not brass | T | `kubejs/client_scripts/tooltips.js:351` |  |  |  |
| CT-087 | Labels | Labels don't block the surface they are placed on from being interaced with; use them on chests with ease | W | `dev/creativeLabels/.../FacePaperWthit.java:72-77` |  |  |  |
| CT-088 | Labels | Sticky notes remember who placed them and on what day | W | `FacePaperWthit.java:119-126` |  |  |  |
| CT-089 | Labels | Write on a sticky note, then right-click an item in your inventory to stick the note on it | T | `creative_labels/lang/en_us.json:12-15` |  |  |  |
| CT-090 | Labels | Sneak right-click a placed label with an item to show it; glow ink makes the label glow | T | `creative_labels/lang/en_us.json:17-24` |  |  |  |
| CT-091 | Labels | An eraser takes the last label off an item or wipes a sticky note clean | T | `creative_labels/lang/en_us.json:26-29` |  |  |  |
| CT-093 | Netherite | Netherite shards can't be crafted by hand | T | `kubejs/client_scripts/tooltips.js:290` |  |  |  |
| CT-095 | Removed items | An item marked REMOVED ITEM in red is left over from an old version; don't use it! | T | `kubejs/client_scripts/jeichanges.js:15-23` |  |  |  |
| CT-096 | Admin | Operators can look at barriers, structure voids and light blocks; other players see through them | W | `dev/creativeCommon/.../WthitOperatorBlocks.java:22-30` | not for players? |  |  |
| CT-097 | Hoes | A hoe's tooltip shows its harvest area; better hoes harvest a wider area at once | T | `HoeHarvestTooltip.java:11-28` | split from CT-046 |  |  |
| CT-098 | Campfires | A kindled campfire sends up signal smoke and keeps burning in the rain | new | none yet | owner 2026-11-07. Kindling is not built: today a coal or charcoal block below gives rain immunity, a hay bale gives signal smoke. Needs a kindling JEI page (Common has a "Campfire Rules" page). May absorb CT-077 |  |  |
| CT-099 | Elytra | Elytra glide over a campfire on a hay bale for a lift; a soul campfire pulls you down | T | `kubejs/client_scripts/tooltips.js:316-317` | owner 2026-11-07; moved from CT-166. ? which mod or pack code does the lift |  |  |
| CT-100 | Breeding | Animals have no gender, so any two of the same kind can breed | new | `CozyCats.java` (cats) | owner 2026-11-07, replaces CT-074. ? check every modded animal; MCA villagers have genders |  |  |

### 4.2 Upstream mod features (MOD)

| ID | Family | Draft tip | UI | Source | Note | Verdict | Checked |
|---|---|---|---|---|---|---|---|
| CT-102 | Knife drops | Finish the Ender Dragon with a knife or machete for extra dragon meat and an extra tooth | J | `ends_delight/jei_info.js:10-12` |  |  |  |
| CT-103 | Knife drops | Kill sheep with a knife for a chance at a rack of mutton; Looting helps, fire cooks it | J T | `ka/creativedelight/kubejs/largemeals/jei_info.js:10-14` |  |  |  |
| CT-104 | Knife drops | Kill Elder Guardians and squid with a knife to collect eyes and tentacles for cooking | J | `dev/creativeDelight/members/oceanic/.../OceanicJei.java:27-29` |  |  |  |
| CT-105 | Oceanic | Wild sea grapes grow on the floor of warm and lukewarm oceans | J | `OceanicJei.java:30` |  |  |  |
| CT-106 | Fungi | Place a mushroom on rich soil and it slowly grows into a colony you can harvest | J T | `ka/creativedelight/kubejs/fungidelight/jei_info.js:12-15`, `farmersdelight/client.js:19-20` |  |  |  |
| CT-107 | Fungi | Pigs dig in podzol, mud, rooted dirt and truffle dirt; truffle dirt always gives a truffle | J | `fungidelight/jei_info.js:17` | other soils give a truffle, stick, bone meal or clay ball |  |  |
| CT-108 | Fungi | Show a truffle to your tamed wolf and it spends two minutes sniffing out dirt with truffles | J | `fungidelight/jei_info.js:18` |  |  |  |
| CT-109 | Fungi | Breed pigs with truffles for a chance at extra piglets | J | `fungidelight/jei_info.js:19` |  |  |  |
| CT-110 | Fungi | Inky cap dishes give you a weak stomach: skip alcohol and potions until it wears off | J T | `fungidelight/jei_info.js:21-23` |  |  |  |
| CT-111 | Farmer's Delight | Fertile soil needs no tilling; soak it with water and plants grow even faster | T | `farmersdelight/client.js:21-22` |  |  |  |
| CT-112 | Farmer's Delight | A basket picks up items dropped in front of its opening, even on its side | T | `farmersdelight/client.js:18` |  |  |  |
| CT-113 | Farmer's Delight | Some foods taste so good that you accidentally eat the bowl. Be sure to keep extras | T | `farmersdelight/client.js:17` | owner wording; fact still unchecked |  |  |
| CT-114 | Vintage Delight | Mixed seeds grow into a random crop | J T | `ka/creativedelight/kubejs/vintagedelight/jei_info.js:11-14` |  |  |  |
| CT-115 | Vintage Delight | Place a salt block, or a faster evaporator, next to water to grow salt | J | `JEIVintagePlugin.java:35-36` |  |  |  |
| CT-116 | Vintage Delight | Fermented organic mash is a stronger fertilizer, or compost | J | `JEIVintagePlugin.java:38` |  |  |  |
| CT-117 | Neapolitan | Strawberries grown above y 192 turn white | T | `ka/creativedelight/kubejs/neapolitan/client.js:11-12` |  |  |  |
| CT-118 | Plants | Feed a baby animal a poisonous potato and it will stay small | T | `ka/creativedelight/kubejs/minecraft/client.js:5` |  |  |  |
| CT-119 | Plants | Bone meal a spore blossom to get another one | T | `ka/creativecompat/kubejs/tooltips.js:143` |  |  |  |
| CT-120 | Plants | Shear a grass block to stop it spreading or turning to dirt; bone meal it to undo | W | `WthitSheared.java:52-60` | bone meal undo read from IW 1.2.10 (SoilBlock, ModGrassBlock, ModMyceliumBlock); Silk Touch not needed. Check the pack-added lush grass and grass slab |  |  |
| CT-121 | Farming | "(Harvestable)" on a crop means you can pick it before it is fully grown | W | `dev/creativeDelight/.../WthitCropGrowth.java:150-157` |  |  |  |
| CT-122 | Mining | Some blocks drop nothing, or something else, unless mined with Silk Touch; look at a block to check | W | `WthitSilkTouch.java:46-67` |  |  |  |
| CT-123 | Bees | Look at a beehive to see how many bees are inside and its honey level | W | `WthitHive.java:71-82` |  |  |  |
| CT-124 | Kegs | Kegs only ferment at the right temperature; look at a keg to see what the brew needs | W | `KegWthit.java:76-101` |  |  |  |
| CT-125 | Kegs | Dread nog raises your Bad Omen level, so expect a bigger raid | T | `brewinandchewin/lang/en_us.json:74` |  |  |  |
| CT-126 | Brewing | A coaster holds up to four drinks | W | `WthitCoaster.java:61-65` |  |  |  |
| CT-127 | Vinery | Chorus wine makes you wake up somewhere unexpected | T | `vinery/tooltips.js:34` |  |  |  |
| CT-128 | Vinery | Stand near a Vinery banner to keep getting its effect | T | `vinery_backport/lang/en_us.json:70` |  |  |  |
| CT-129 | Cooking | Skewers on a grill need flipping and burn if left too long | W | `BBQWthitClient.java:39-41` |  |  |  |
| CT-130 | Cooking | Barbecue sauce goes on skewers after they are cooked | T | `barbequesdelight/lang/en_us.json:78` | ? fact |  |  |
| CT-131 | Cooking | An ME kitchen station must be linked to an ME security terminal to work | W | `WthitKitchenStation.java:59-64` |  |  |  |
| CT-132 | Cooking | A kitchen sink is an endless water source | T | `cookingforblockheads/client.js:10` |  |  |  |
| CT-133 | Cooking | Fantasy Furniture ovens cook like a smoker using ordinary fuel | T | `kubejs/client_scripts/tooltips.js:375-376` |  |  |  |
| CT-134 | Pancakes | Stack up to 8 pancakes and top them with honey or chocolate | T | `ka/creativedelight/kubejs/supplementaries/client.js:9-11` |  |  |  |
| CT-135 | Supplementaries | Feed your horse a sugar cube, but keep it dry or it dissolves | T | `supplementaries/client.js:7` |  |  |  |
| CT-136 | Archery | Torch arrows light dark spots from afar; rope arrows hang a climbable rope where they land | T | `ka/creativecompat/kubejs/tooltips.js:82-120` |  |  |  |
| CT-137 | Archery | Seeker arrows home in, drill arrows break a block, and teleportation arrows take you where they land | T | `ka/creativecompat/kubejs/tooltips.js:63-90` |  |  |  |
| CT-138 | Archery | Wandering traders sometimes sell arrows you can't craft, like ethereal and cupid arrows | T | `ka/creativecompat/kubejs/tooltips.js:67-73` |  |  |  |
| CT-139 | Archery | Rename a message arrow and fire it to send that text to whoever it hits | T | `ka/creativecompat/kubejs/tooltips.js:77` |  |  |  |
| CT-140 | Archery | A quiver holds up to 7 kinds of arrow; hold its key and scroll to choose one | T | `ka/creativecompat/kubejs/tooltips.js:191-193` | key name? |  |  |
| CT-141 | Beacons | Corundum clusters bend and tint beacon beams; tinted glass dims them | T | `ka/creativecompat/kubejs/tooltips.js:99-100,140` |  |  |  |
| CT-142 | Botania | A trinket case holds any item, but only trinkets inside it are worn | T | `ka/creativemagic/kubejs/tooltips.js:14` |  |  |  |
| CT-143 | Botania | Throw a vine ball at a wall to cover it in climbable vines | T | `ka/creativemagic/kubejs/tooltips.js:13` |  |  |  |
| CT-144 | Building | Right-click a ladder with another ladder to extend it downward | T | `ka/creativecompat/kubejs/tooltips.js:108-114` |  |  |  |
| CT-145 | Building | Hollow logs make crawlable tunnels | T | `ka/creativecompat/kubejs/tooltips.js:98` |  |  |  |
| CT-146 | Building | Sponges can be placed on top of water, and banners can hang under blocks and rope | T | `kubejs/client_scripts/tooltips.js:313,318` |  |  |  |
| CT-147 | Create | In contraptions, chain ends stick to blocks without glue, but chain sides ignore slime blocks | T | `kubejs/client_scripts/tooltips.js:308` |  |  |  |
| CT-148 | Building | Mud brick lattices hold water without spilling it | T | `ka/creativecompat/kubejs/tooltips.js:122` |  |  |  |
| CT-149 | Building | Land on a hay bale to take less fall damage; a feather block cancels it | T | `kubejs/client_scripts/tooltips.js:306` |  |  |  |
| CT-150 | Building | Light crying obsidian for a soul fire that never goes out | T | `kubejs/client_scripts/tooltips.js:307` |  |  |  |
| CT-151 | Building | Put a mob head on a fence to make a skull pike that scares hostile mobs | T | `ka/creativecompat/kubejs/tooltips.js:204-213` |  |  |  |
| CT-152 | Building | Item frames can be dyed | T | `ka/creativecompat/kubejs/tooltips.js:141-142` |  |  |  |
| CT-153 | Framed blocks | Place a second framed slab or panel into the first to mix two materials in one block | T | `ka/creativecompat/kubejs/tooltips.js:228-233` |  |  |  |
| CT-154 | Create | Schematics keep the block inside a copycat block, but framed blocks come out empty | T | `ka/creativecompat/kubejs/tooltips.js:218-226` |  |  |  |
| CT-155 | Create | Speakers can post chat announcements, put a message on screen or read text aloud | T | `kubejs/client_scripts/tooltips.js:349` |  |  |  |
| CT-156 | Create | Cinder flour reacts with potions in a basin | T | `kubejs/client_scripts/tooltips.js:352-353` |  |  |  |
| CT-157 | Comforts | A sleeping bag skips the night without moving your spawn point | T | `ka/creativecompat/kubejs/tooltips.js:19-21` |  |  |  |
| CT-158 | Compasses | A nature's compass guides you to any biome | T | `ka/creativecompat/kubejs/tooltips.js:26` |  |  |  |
| CT-159 | Pets | Give your pet a bed: if it dies, it respawns there | T | `ka/creativecompat/kubejs/tooltips.js:31,37` |  |  |  |
| CT-160 | Pets | A deed of ownership hands a pet over to another player | T | `ka/creativecompat/kubejs/tooltips.js:33-35` |  |  |  |
| CT-161 | Pets | Stick a feather on a stick to a hostile mob and your cats attack it | T | `ka/creativecompat/kubejs/tooltips.js:32` |  |  |  |
| CT-162 | Pets | A rotten apple turns a horse into a zombie horse; a sinister carrot makes it a skeleton horse | T | `ka/creativedelight/kubejs/domesticationinnovation/client.js:7-8` |  |  |  |
| CT-163 | Pets | Bind a flute to your horse and play it to call the horse to you | T | `ka/creativecompat/kubejs/tooltips.js:169` |  |  |  |
| CT-164 | Dragon gear | A dragon anvil never breaks from use | T | `ka/creativeequipment/kubejs/tooltips.js:36` |  |  |  |
| CT-165 | Elytra | Craft an elytra with dyes or a banner to style it; the wings can be split and styled one at a time | T | `ka/creativeequipment/kubejs/tooltips.js:32-34` |  |  |  |
| CT-167 | Enchanting | Books on a shelf boost enchanting: four books, or three enchanted books, count as one bookshelf | T | `kubejs/client_scripts/tooltips.js:296-297` |  |  |  |
| CT-168 | Enchanting | An end crystal on a pedestal near an enchanting table adds 3 enchanting power | T | `ka/creativecompat/kubejs/tooltips.js:175` |  |  |  |
| CT-169 | Enchanting | Craft enchanted gear with a rune to change the colour of its glint | T | `ka/creativecompat/kubejs/tooltips.js:97,115` |  |  |  |
| CT-170 | Music | Make your own music discs at the etching table from blank discs and music labels | T | `kubejs/client_scripts/tooltips.js:397-405` |  |  |  |
| CT-171 | Music | An album cover holds nine discs, and an album jukebox plays them in order | T | `kubejs/client_scripts/tooltips.js:400-409` |  |  |  |
| CT-172 | Music | A boombox plays its disc while you carry it; hold it and people near you hear it too | T | `kubejs/client_scripts/tooltips.js:394-396` |  |  |  |
| CT-173 | Exposure | Right-click a camera with a lens, film or filter to swap it in | T | `exposure/lang/en_us.json:286-289` |  |  |  |
| CT-174 | Exposure | Print red, green and blue layers on a chromatic sheet for a full-colour photo | T | `exposure/lang/en_us.json:294-296` |  |  |  |
| CT-175 | Exposure | Retro films and camera filters give Game Boy, NES or pencil-sketch photos | T | `exposure_expanded/lang/en_us.json:3-77` |  |  |  |
| CT-176 | Exposure | Photograph frames come in three sizes, go on floors and ceilings, and glow with a glow ink sac | J | `ExposureJeiPlugin.java:68` |  |  |  |
| CT-177 | Gunpowder | Lay gunpowder as a fuse trail; it also makes TNT go off sooner | T | `kubejs/client_scripts/tooltips.js:295` |  |  |  |
| CT-178 | Immersive Engineering | Copper or lead wire works as solder in the engineer's circuit table | T | `kubejs/client_scripts/tooltips.js:326-327` | ? origin |  |  |
| CT-179 | Immersive Weathering | Steel wool scrubs rust and oxidation off metal blocks; tallow waxes them | T | `ka/creativecompat/kubejs/tooltips.js:53-54` |  |  |  |
| CT-180 | MCA | Give two villagers a matchmaker's ring to arrange their marriage | T | `ka/creativecompat/kubejs/tooltips.js:46` |  |  |  |
| CT-181 | Quark | Carry slime in a bucket: it jiggles when you stand in a slime chunk | T | `ka/creativecompat/kubejs/tooltips.js:116-121` |  |  |  |
| CT-182 | Quark | A crate holds 640 items of any kind, whatever their stack size | T | `ka/creativecompat/kubejs/tooltips.js:123` |  |  |  |
| CT-183 | Quark | Fill a feeding trough and nearby animals feed and breed on their own | T | `ka/creativecompat/kubejs/tooltips.js:136` |  |  |  |
| CT-184 | Redstone | Pistons can push chests and other storage blocks, and dispensers can place blocks | T | `ka/creativecompat/kubejs/tooltips.js:128,137-139` |  |  |  |
| CT-185 | Redstone | Obsidian pressure plates only trigger for players | T | `ka/creativecompat/kubejs/tooltips.js:125-129` |  |  |  |
| CT-186 | Redstone | Magnets push and pull metal blocks | T | `ka/creativecompat/kubejs/tooltips.js:130-131` |  |  |  |
| CT-187 | Redstone | An ender watcher gives a redstone signal when a player looks at it | T | `ka/creativecompat/kubejs/tooltips.js:134-135` |  |  |  |
| CT-188 | Redstone | Grates keep animals in but let items fall through | T | `ka/creativecompat/kubejs/tooltips.js:127,133` |  |  |  |
| CT-189 | Redstone | Cog blocks carry analog redstone signals, including straight up and down | T | `ka/creativecompat/kubejs/tooltips.js:160-170` |  |  |  |
| CT-190 | Redstone | A powered gold door can't be opened by hand | T | `ka/creativecompat/kubejs/tooltips.js:165-166` |  |  |  |
| CT-191 | Shulker shells | Hold shulker shells in both hands and sneak-right-click a chest to pack it up whole | T | `kubejs/client_scripts/tooltips.js:300` |  |  |  |
| CT-192 | Supplementaries | Wrap items in a present with a note; only the named player can open it | T | `ka/creativecompat/kubejs/tooltips.js:149-150` |  |  |  |
| CT-193 | Supplementaries | Sacks keep their contents when picked up, but carrying three or more slows you down | T | `ka/creativecompat/kubejs/tooltips.js:151` |  |  |  |
| CT-194 | Supplementaries | Rename a key to set its code, then lock safes and netherite doors with it | T | `ka/creativecompat/kubejs/tooltips.js:181-188` |  |  |  |
| CT-195 | Supplementaries | A faucet on a waterlogged block gives endless water | T | `ka/creativecompat/kubejs/tooltips.js:154` |  |  |  |
| CT-196 | Supplementaries | Point bellows at a furnace to make it smelt faster | T | `ka/creativecompat/kubejs/tooltips.js:155` |  |  |  |
| CT-197 | Supplementaries | Soap cleans the dye off items | T | `ka/creativecompat/kubejs/tooltips.js:157-158` |  |  |  |
| CT-198 | Supplementaries | Tip bamboo spikes with a lingering potion to poison anything that steps on them | T | `ka/creativecompat/kubejs/tooltips.js:168` |  |  |  |
| CT-199 | Supplementaries | A globe shows your coordinates, and an altimeter your height | T | `ka/creativecompat/kubejs/tooltips.js:171-173` |  |  |  |
| CT-200 | Supplementaries | Lay fodder down and animals eat from it on their own | T | `ka/creativecompat/kubejs/tooltips.js:174` |  |  |  |
| CT-201 | Supplementaries | Catch small animals in a cage to carry them around | T | `ka/creativecompat/kubejs/tooltips.js:179-180` |  |  |  |
| CT-202 | Supplementaries | Push a flint block past an iron block to strike fire | T | `ka/creativecompat/kubejs/tooltips.js:177-178` |  |  |  |
| CT-203 | Supplementaries | Bombs blast without breaking blocks; blue bombs set off nearby creepers | T | `ka/creativecompat/kubejs/tooltips.js:195-201` |  |  |  |
| CT-204 | Tools | A trowel places a random block from your hotbar, handy for natural-looking walls | T | `kubejs/client_scripts/tooltips.js:277-279` |  |  |  |
| CT-205 | Transport | Ghost rails send minecarts through solid blocks until they reach dreamwood | T | `kubejs/client_scripts/tooltips.js:366` |  |  |  |
| CT-206 | Waystones | A warpstone lets you teleport to your waystones from anywhere | T | `ka/creativecompat/kubejs/tooltips.js:59` |  |  |  |
| CT-207 | Knives | The fluix knife always has Looting I or better | T | `delightful/lang/en_us.json:179` |  |  |  |
| CT-208 | Storage | A jar holds up to 12 bottles of one fluid or potion | W | `WthitHolderContents.java:67-87` |  |  |  |
| CT-209 | Plants | "Stopped growing" on a vine tip means it has reached full length | W | `WthitSheared.java:66-70` |  |  |  |
| CT-210 | Vinery | Look at a grapevine stem or lattice to see which grape grows there and when it is ripe | W | `VineryWthitClient.java:96-108` |  |  |  |

## 5. Gap harvest

Not started (step 4).

## 6. Follow-ups from the review (not tips)

- Explorer's compass: make sure it is a creative-only item (owner 2026-11-07; was the note on draft CT-092, since
  removed).
- Kindling (CT-098): the mechanic is not built. Define it (today a coal or charcoal block under a campfire gives rain
  immunity, a hay bale gives signal smoke) and add a kindling JEI page next to Common's "Campfire Rules" page.
- Cheese mold (CT-038 dropped): the cheese mold method is under evaluation and probably going away.
- Grass un-shearing: bone meal makes a sheared Immersive Weathering grass block, mycelium or soil fertile again
  (read from IW 1.2.10 bytecode: `SoilBlock`, `ModGrassBlock`, `ModMyceliumBlock` bone meal sets `fertile=true`), so
  Silk Touch is not needed. The pack-added lush grass and grass slab still need checking.

## 7. Edits to the existing tip packs (2026-11-07)

Owner notes on the vanilla Loading Tips list (numbers from [loading_tips_list.md](loading_tips_list.md) before the edit):

| # | Before | After |
|---|---|---|
| 2 | Don't dig straight down! | removed |
| 4 | Gold tools are faster but break easily. | Gold tools mine faster than diamond, are easy to enchant, and naturally apply Fortune or Looting. Unfortunately they aren't very durable |
| 5 | Beds explode in the Nether. | Beds explode when used outside the Overworld |
| 8 | Fortune works on coal, diamonds, lapis, copper, and iron. | Fortune works on coal, copper, iron, gold, redstone, lapis, diamond, emerald and quartz ores, and on ancient debris |
| 12 | Villagers love emeralds. | Villagers trade for emeralds, and every trade gives them experience toward better trades |

- 8: the ore list is the vanilla Fortune ores; ancient debris from Fortune on Netherite (its loot table drops raw
  debris). 12: the owner's note said 9 and 13; confirmed 2026-11-07 they meant 8 and 12.
- Capitals and trailing periods normalized in both packs (section 2), with
  a one-off script: 217 vanilla and 9 Creative Tips lost only the final period; the other changed tips
  also changed case (item words lowered, mob and place names kept). Tips ending in `!` keep it.

## 6. History

- 2026-11-06: plan written; step 1 harvest (item tooltips, WTHIT, JEI info).
- 2026-11-07: owner review, first pass. Owner removed CT-054, 066, 067, 092, 094, 101. Dropped CT-027, CT-038,
  CT-074; CT-166 moved to CT-099. Rewritten: CT-008, 013, 015, 028 (steel oven), 032, 036, 042 (greenhouse), 046
  (split, area is new CT-097), 075, 078, 086, 107, 108, 113, 114, 120. New: CT-097 hoe area, CT-098 kindling,
  CT-099 elytra campfire lift, CT-100 no animal genders. Capitals and trailing periods normalized (section 2).
  Vanilla Loading Tips: one removed, four rewritten (section 7).
