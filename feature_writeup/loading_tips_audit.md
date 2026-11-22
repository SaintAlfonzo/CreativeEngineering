# Loading tips audit 2 (2026-11-14)

Owner request 2026-11-14: find the out of date loading screen tips, decide which to remove and which to rewrite, make
every tip follow the style rules, and add to the rules that tips are concise and informative and give the full picture
of one feature (counter cases and sister cases included, several short facts in one tip). Rules:
[plan_creative_tips.md](plan_creative_tips.md) section 2. Mechanism and file format: [loading_tips.md](loading_tips.md)
and [plan_loading_tips_screens.md](plan_loading_tips_screens.md). First audit (2026-11-06): loading_tips.md appendix B.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=f61f45c8ea25 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 5 open TODO rows (Planned 1, In Progress 1, Testing 3), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `ui/loadingtips #2` | Planned | S C | Tip announcements in chat |
| `ui/loadingtips #3` | In Progress | C | Creative Tips: the pack's own tips |
| `ui/loadingtips #1` | Testing | S C | Loading screen tip bar and tip packs |
| `ui/loadingtips #4` | Testing | C | Audit Loading Tips against pack changes |
| `ui/loadingtips #5` | Testing | C | Tip ids, override packs, tips on every loading screen |
<!-- generated:status_index end open_work -->

| Part | Status |
|---|---|
| Audit of the 335 effective tips (Loading Tips 321 + Creative Tips 14) against the pack | DONE 2026-11-14 |
| Changes written to Creative Tips (override file, `kubejs/assets/creative_tips/loading_tips/tips.json`) | BUILT 2026-11-14: 213 effective tips, 319 ids in `remove`, 210 replacements; F3+T, no rebuild |
| Loading Tips (`creativecommon` jar) | unchanged, still upstream verbatim |
| Fact checks (section 3) | Open |
| In game: LTP-15 | Open |

## 1. Method and result

- Effective list built from the two files with the loader's rules (archive below), each tip read against the pack's
  mods and configs. Pack changes that made vanilla tips wrong or partial: Quark game nerfs (sheep drop no wool when killed, Iron Golems drop no iron, boats do not glide
  on ice, Endermen and Creepers do not grief, cure discounts reset on re-infection), Quark tweaks (Gold Tools Have
  Fortune, Pig Litters, Sponges Better 256, Armed Armor Stands, Map Washing, Campfires Boost Elytra, Glass Item
  Frame, Wool Shuts Up Minecarts), Allurement (no 'Too Expensive', cheap renaming, Feather Falling stops trampling,
  Riptide in cauldrons, Bane of Arthropods on cobwebs), Better Beacons (base amplifier, payment range), Unlit Campfire
  and the pack's campfire rain, the golden food values, Glow Ink Plus, Chimes, and the pack's Illusioner spawns.
- 335 tips became 213: 69 groups merged into one tip each (107 tips merged away), 15 removed with no replacement
  (trivia, or wrong with no confirmed true version), 43 rewritten in place, 101 unchanged.
- Checks after the write: no unknown or unmatched ids, longest tip 129 characters, no trailing period, no
  "now / new / no longer / changed".
- Archive of the state before this audit (both files from the working tree and HEAD, and the generated list):
  `/tmp/claude-1000/-home-[REDACTED]--local-share-atlauncher-instances-CreativeEngineering2/2bb72711-5d06-4617-8f48-c2173d7e8f5d/scratchpad/tips_archive_2026-11-14/`
  (session scratchpad, not kept). The repo copy is `tools/loading_tips_audit_1114/effective_before.tsv` (index, source,
  id, text) plus the decision table and apply script (`audit_decisions.py`, `audit_apply.py`), and git holds the
  files themselves.
- Quark's Nerf Mending and Dimension Locked Elytra were turned off on 2026-11-14 (owner), after this audit first
  applied them; the Mending and elytra tips (54, 98) describe vanilla behavior again.
- **A later pass will change more tips** (owner, 2026-11-14). This audit fixed tips the pack makes wrong; a second
  pass, describing the pack itself, will override more of them with the pack's own facts, for example golden and
  gilded foods (tip 68) and ice melting near light (tip 87). Those tips are true but generic until then. That pass
  joins the Creative Tips content plan ([plan_creative_tips.md](plan_creative_tips.md)) and edits entries by id.
- Do not rerun `tools/loading_tips_migrate.py`: it writes Creative Tips from scratch and would drop this audit.

## 2. Changes

`#` is the line in `effective_before.tsv` (the list order before the audit).

### 2.1 Merged (one tip per feature)

The first tip keeps its id and takes the new text; the others go to `remove`.

| Kept id | Merged in (#) | New text | Why |
|---|---|---|---|
| 1 `you_can_use_an_axe_to_scrape_oxidation_off` | 40, 291 | Copper turns green over time; honeycomb wax stops it, and an axe scrapes off the wax or one stage of rust | three halves of one feature (oxidize, wax, scrape) |
| 5 `endermen_hate_water` | 120 | Endermen are hurt by water, anger when looked at, ignore you behind a carved pumpkin and never pick up blocks | Quark Non Griefing Entities lists enderman; pumpkin tip is the counter case |
| 6 `use_a_silk_touch_pickaxe_for_glass` | 11 | Smelt sand into glass; glass breaks into shards unless mined with Silk Touch, and shards craft back into glass | make and break of one block |
| 7 `axes_disable_shields` | 220 | Shields cut damage from the front, Creeper blasts included, and better shields cut more; an axe hit disables one for 5 seconds | pack shield tiers; both shield tips are one feature |
| 9 `torches_prevent_monster_spawns` | 127 | Monsters cannot spawn on lit blocks, or on bottom slabs, glass, carpet or leaves | spawn-proofing as one rule, sister cases added |
| 17 `wolves_love_bones_to_tame_and_meat_to_heal` | 157 | Tame wolves with bones and heal them with any meat, rotten flesh included | sister tip |
| 24 `soul_sand_slows_you_down` | 316 | Soul sand slows you down unless your boots have Soul Speed, which makes you faster on soul sand and soul soil | counter case |
| 28 `smokers_cook_food_twice_as_fast` | 29, 298 | Smokers cook food and blast furnaces smelt ores and raw metal twice as fast as a furnace, but take only their own kind | sister blocks and the limit |
| 32 `lecterns_hold_books_for_reading` | 240 | A lectern holds a book anyone can read and sends a redstone pulse on each page turn | same block |
| 34 `hoppers_move_items_between_containers` | 242 | Hoppers pull items from above, push into the container they face and stop while powered | same block |
| 37 `axolotls_help_fight_guardians_and_grant` | 285 | Axolotls play dead to heal, and killing a mob one is fighting gives you Regeneration | same mob |
| 42 `spyglasses_zoom_in_on_the_horizon` | 293 | Spyglasses, made from amethyst and copper, can be used to zoom in on distant things | recipe and use |
| 43 `goats_can_ram_you_off_cliffs` | 214 | Goats can ram you off cliffs, and a Goat that rams a hard block drops a goat horn | same mob |
| 44 `frogs_eat_small_slimes_and_magma_cubes` | 210, 211, 212, 213 | Frogs eat small Slimes for slimeballs and small Magma Cubes for froglight in the Frog's color, set by its biome | five fragments of one feature |
| 48 `pumpkins_need_space_to_grow` | 49 | Pumpkins and melons need a free dirt, grass or farmland block beside the stem to grow | sister crops |
| 51 `bells_alert_villagers_to_raids` | 171 | Ring a bell, by hand or with a projectile, to send villagers indoors and make nearby raiders glow | same block |
| 60 `blast_protection_reduces_explosion_damage_and` | 61, 62 | Protection, Fire, Blast and Projectile Protection exclude each other; each specific one guards best against its own damage | exclusive family |
| 64 `respiration_iii_lets_you_breathe_longer` | 57, 65, 274 | Underwater, Respiration and turtle shells add breath, Aqua Affinity restores mining speed and Depth Strider speeds you up | underwater gear family |
| 66 `ender_pearls_teleport_you_instantly` | 105 | A thrown ender pearl teleports you where it lands, costs a little health and sometimes spawns an Endermite | cost and side effect |
| 68 `gold_apples_give_extra_health_hearts` | 69 | Golden foods give Regeneration and Absorption; gilded foods give more of both plus Resistance and Fire Resistance | pack golden food values cover every golden food; 'massive buffs' vague |
| 80 `weakness_potions_help_cure_zombie_villagers` | 81 | Cure a Zombie Villager with a splash Potion of Weakness, then a golden apple | two halves of one recipe |
| 85 `magma_blocks_deal_damage_when_stepped_on` | 86 | Magma blocks hurt anything on them unless it crouches or wears Frost Walker boots | counter cases |
| 87 `ice_blocks_melt_near_light_sources` | 326 | Ice melts near bright light, but packed and blue ice never do | counter case |
| 90 `water_cannot_be_placed_in_the_nether` | 91 | Water cannot be placed in the Nether, except in a cauldron | counter case |
| 92 `sponge_blocks_soak_up_nearby_water` | 93 | A sponge soaks up to 256 water blocks; dry a wet one in a furnace or by placing it in the Nether | Quark Sponges Better (256, vanilla 64) |
| 94 `conduits_require_a_prismarine_frame` | 273 | A conduit in a prismarine frame gives Conduit Power underwater, and a full frame makes it attack hostile mobs | same block |
| 95 `beacons_require_a_pyramid_base` | 192, 304 | A beacon's pyramid size unlocks effects, a richer base block strengthens them, payment sets the range, glass colors the beam | Better Beacons base amplifier and payment range; three beacon fragments |
| 98 `elytra_let_you_glide_through_the_sky` | 270 | Elytra let you glide from a height; repair them with phantom membrane | same item (Quark Dimension Locked Elytra turned off 2026-11-14) |
| 100 `shulker_boxes_are_portable_chests` | 101 | Shulker boxes keep their items when broken; craft them from shulker shells, dropped by Shulkers in End Cities | 101 was wrong (shells drop from Shulkers, not cities) |
| 104 `silverfish_spawn_from_infested_blocks_and` | 186 | Silverfish come from spawners and from infested blocks, which look like stone but break fast | dup |
| 107 `lead_animals_to_move_them_easily` | 328 | Leads let you walk animals and tie them to fences | dup |
| 109 `saddles_let_you_ride_horses_and_pigs` | 113, 308, 309 | Saddle a horse, pig or lava-walking Strider to ride it; steer pigs with a carrot on a stick, Striders with a warped fungus on one | riding family |
| 119 `snow_golems_leave_a_trail_of_snow` | 327 | Snow Golems leave snow trails and throw snowballs; shear off the pumpkin to see their face | same mob |
| 123 `wooden_buttons_have_a_longer_signal` | 124 | Wooden buttons and pressure plates react to arrows and items, stone ones only to players and mobs | sister blocks |
| 129 `fences_connect_to_most_solid_blocks` | 130 | Fences connect to solid blocks and are too high to jump, and fence gates in them open either way | same block family |
| 135 `leather_boots_let_you_walk_on_snow` | 134 | Powder snow freezes anyone who sinks into it; leather boots let you walk on top | counter case |
| 139 `hoglins_are_afraid_of_warped_fungus_nether` | 140, 177 | Hoglins breed with crimson fungus and flee warped fungus, portals and respawn anchors; out of the Nether they become Zoglins | same mob |
| 141 `bastions_are_full_of_gold_blocks` | 318 | Bastions hold gold blocks and rich loot, Pigstep disc included | same structure |
| 144 `daylight_detectors_activate_when_exposed_to` | 238 | Daylight detectors give a signal in sunlight, or at night once clicked to invert them | same block |
| 147 `sand_falls_if_there_is_no_block_below` | 148, 131, 132, 288 | Sand, gravel and concrete powder fall with nothing below; a falling anvil or pointed dripstone hurts what it hits | falling blocks family |
| 174 `nether_gold_ore_drops_nuggets` | 175, 324 | Nether gold ore and gilded blackstone drop gold nuggets | dup and sister |
| 178 `twisting_vines_grow_upward` | 179 | Twisting vines grow up and weeping vines grow down; both can be climbed | sister blocks |
| 194 `pistons_can_push_up_to_12_blocks` | 195, 249 | Pistons push up to 12 blocks; sticky pistons also pull one back, even another piston | same block |
| 200 `the_recovery_compass_points_to_where_you_last` | 201 | The recovery compass, crafted with echo shards, points to where you last died | dup |
| 202 `ancient_cities_are_the_only_place_to_find_swift` | 203 | Swift Sneak, found only in Ancient City chests, lets you walk faster while crouching | dup |
| 204 `the_warden_is_completely_blind_but_has` | 163, 164, 169, 205 | The blind Warden, summoned by sculk shriekers, hunts by sound; sneak to stay quiet and throw items to lure it away | five fragments of one mob |
| 206 `wool_blocks_dampen_vibrations_making_you` | 150 | Wool and carpet hide vibrations from sculk, steps on them included, and a minecart over wool runs silently | Quark Wool Shuts Up Minecarts; 150 was unclear |
| 207 `allay_mobs_will_collect_items_for_you_if_you` | 208, 209 | Allays fetch items matching the one you give them, and one dancing to a jukebox duplicates when given an amethyst shard | same mob |
| 215 `glow_ink_sacs_make_sign_text_glow_in_the_dark` | 152, 153, 286 | Glow ink sacs from Glow Squids make sign text glow and craft glowing wool and carpet; an ink sac clears sign glow | dup x2, Glow Ink Plus |
| 218 `multishot_crossbows_fire_three_arrows_for_the` | 217, 219 | Multishot fires three arrows and Piercing goes through mobs, but not both; Quick Charge reloads faster | crossbow family |
| 223 `smite_deals_massive_damage_to_undead_mobs_like` | 224 | Smite hits undead harder, Bane of Arthropods hits spiders harder and cuts cobwebs fast; neither combines with Sharpness | exclusive family |
| 227 `loyalty_tridents_fly_back_to_your_hand_after` | 228, 229 | Tridents: Loyalty returns them, Channeling calls lightning, Riptide launches you in rain, water or a cauldron but excludes both | trident family |
| 232 `infinity_bows_never_run_out_of_arrows_as_long_as` | 53, 230, 231, 233 | On bows, Power adds damage, Punch knockback, Flame fire, and Infinity spares arrows; Infinity and Mending exclude each other | bow family, Infinity was a dup |
| 245 `powered_rails_can_be_used_to_launch_minecarts_up` | 246, 247 | Powered rails speed carts up slopes, detector rails signal when a cart passes and activator rails eject riders | rail family |
| 250 `slime_blocks_bounce_entities_high_into_the_air` | 251, 196 | Slime blocks bounce you, honey blocks stop jumps and let you slide down walls, and the two never stick together | sister blocks |
| 252 `sweet_berry_bushes_slow_you_down_and_deal_damage` | 145 | (text unchanged) | dup; keep 252 text (pack rewrite) |
| 253 `foxes_will_pick_up_and_use_any_item_you_drop` | 254 | Foxes pick up and use dropped items, so a Fox holding a Fire Aspect sword can set you alight | same mob |
| 256 `pandas_have_different_personalities_based_on` | 257 | Pandas have personalities set by their genes, and a sneezing baby Panda can drop slimeballs | same mob |
| 258 `parrots_mimic_the_sounds_of_nearby_hostile_mobs` | 259 | Parrots imitate nearby hostile mobs, and cookies poison them | same mob |
| 262 `curing_a_zombie_villager_gives_you_massive_trade` | 117, 261 | Hero of the Village, good gossip and curing a Zombie Villager lower prices; a cure's discount is lost if it turns again | Quark Nerf Villager Discount; price family |
| 263 `iron_golems_spawn_in_villages_to_protect_against` | 264 | Iron Golems guard villages and drop no iron when killed; heal one with iron ingots | Quark Disable Iron Farms |
| 278 `soul_sand_underwater_creates_bubble_columns_that` | 277 | Underwater, soul sand makes bubble columns that push up and magma blocks make ones that pull down | sister blocks |
| 295 `amethyst_geodes_are_found_deep_underground_in` | 41, 168, 296 | Budding amethyst in underground geodes grows clusters that drop four shards; it cannot be moved, even with Silk Touch | one feature |
| 302 `wither_skeletons_only_spawn_in_nether_fortresses` | 97 | Wither Skeletons spawn only in Nether Fortresses and sometimes drop their skull | same mob |
| 303 `wither_skulls_are_needed_to_summon_the_wither` | 52, 96 | Three Wither Skeleton skulls on a T of soul sand or soul soil summon the Wither, which drops a Nether star | boss in full |
| 306 `ancient_debris_is_most_common_at_y_level_15_in` | 22, 317 | Ancient debris is most common around y 15 in the Nether and smelts into netherite scrap | dup and the use |
| 321 `respawn_anchors_allow_you_to_set_your_spawn_in` | 173, 320, 322 | Respawn anchors, made from crying obsidian and charged with glowstone, set your spawn in the Nether and explode elsewhere | four fragments |
| 332 `crafting_coal_into_a_block_of_coal_gives_you_8` | 280, 330 | A coal block smelts 80 items, 8 more than its nine coal; a dried kelp block smelts 20 and bamboo only 0.25 | fuel values family |
| 333 `frost_walker_boots_let_you_walk_on_water_and` | 58 | Frost Walker boots freeze the water under you so you can walk on it | dup; magma part is in the magma tip |

### 2.2 Rewritten

| # | Before | After | Why |
|---|---|---|---|
| 3 | Gold tools mine faster than diamond, are easy to enchant, and naturally apply Fortune or Looting. Unfortunately they aren't very durable | Gold tools mine faster than diamond, enchant well and carry Fortune II or Looting II, but wear fast and mine only what iron can | Quark Gold Tools Have Fortune (level 2, harvest level 2); cut flavor 'Unfortunately'; adds the harvest limit |
| 4 | Beds explode when used outside the Overworld | Beds set your spawn in the Overworld and explode in any other dimension | adds the use, sister of the respawn anchor tip |
| 13 | Zombies burn in sunlight | Zombies, Skeletons and Phantoms burn in sunlight; a helmet, water or shade saves them, and Husks never burn | counter cases (helmet, water, Husk) and sister mobs |
| 14 | Creepers are afraid of cats | Creepers run from cats and Ocelots, and Phantoms keep away from cats | sister cases |
| 15 | Water buckets negate fall damage | Land in water, powder snow, cobwebs or on a slime block to take no fall damage; hay bales and honey blocks cut it by 80% | bucket trick is one case of the rule |
| 16 | Pigs love carrots | Pigs breed with carrots, potatoes, beetroot or golden carrots, and have litters of 2 to 3 piglets | Quark Pig Litters (2-3, golden carrots); vague 'love' |
| 18 | Shear sheep for more wool | Shear sheep for wool: a killed sheep drops none | Quark Disable Wool Drops |
| 21 | Fire Aspect cooks meat instantly | Animals that die on fire drop cooked meat, whether from Fire Aspect, Flame or lava | sister sources; 'instantly' wrong |
| 25 | Scaffolding is great for building | Scaffolding can be climbed, reaches 6 blocks out from a support, and breaks all at once from the bottom | was not informative |
| 27 | Barrels are cheaper than chests | Barrels cost less than chests and open with a block on top, but cannot join into a double chest | 'cheaper' alone is half the trade-off |
| 30 | Anvils can rename your items | Anvils rename items cheaply and repair or combine them; costs rise with each use but never become 'Too Expensive' | Allurement Cheap item renaming and Remove too expensive; rename was one of three uses |
| 31 | Grindstones remove enchantments | Grindstones remove every enchantment except curses and give back some XP | curse counter case |
| 33 | Stonecutters save stairs and slabs | A stonecutter turns one block into stairs, walls or two slabs with no waste | vague 'save' |
| 39 | Bees produce honey and wax | Bees fill hives with honey; take it with a bottle, or honeycomb with shears, and keep a campfire below to calm them | what the player does with it |
| 45 | Mud is made from dirt and water | Use a water bottle on dirt to make mud; mud above pointed dripstone dries into clay | how, and the follow-on use |
| 50 | Campfires can cook four items | Campfires place unlit and cook four items; rain puts one out unless a coal or charcoal block is under it | Unlit Campfire defaults and pack campfire rain; revisit when kindling (CT-098) is built |
| 54 | Mending repairs gear using XP orbs | Mending repairs held and worn gear with the XP orbs you collect, before the XP reaches your level | how it works (Quark Nerf Mending turned off 2026-11-14) |
| 59 | Feather Falling IV reduces fall damage | Feather Falling cuts fall damage and stops you trampling farmland | Allurement Feather Falling prevents trampling |
| 74 | Potions of Night Vision see in the dark | Potions of Night Vision let you see in the dark | grammar |
| 76 | Potions of Water Breathing are for diving | Potions of Water Breathing let you breathe underwater | not informative |
| 82 | Cartography tables zoom out your maps | Cartography tables zoom out, copy and lock maps; a cauldron of water wipes one clean | sister uses, Quark Map Washing |
| 83 | Loom tables make custom banner designs | Looms put patterns on banners; banner pattern items unlock the rare ones | name ('Loom tables') and the rest of the feature |
| 88 | Blue ice is the slipperiest block | Blue ice is the slipperiest block to walk on; boats get no extra speed on any ice | Quark Disable Ice Roads and iceboatnerf |
| 99 | Fireworks boost your elytra flight | Fireworks boost elytra flight; a campfire below lifts you and a soul campfire pulls you down | Quark Campfires Boost Elytra |
| 114 | Ravagers can be stunned by shields | Blocking a Ravager's bite with a shield stuns it, and it roars to knock you back when it recovers | what 'stunned' does |
| 118 | Villagers change jobs based on blocks | Villagers take the job of a free workstation nearby and keep it once you have traded with them | counter case |
| 121 | Suspicious stew gives random effects | Suspicious stew gives one effect, set by the flower it was made with | 'random' is wrong |
| 125 | Iron doors require a redstone signal | Iron doors and iron trapdoors open only with redstone | sister block |
| 126 | Trapdoors can be used as ladders | An open trapdoor above a ladder can be climbed like one | misleading |
| 128 | Stairs can be waterlogged | Stairs, slabs, fences, walls, panes and many other partial blocks can hold water | one case of a rule |
| 142 | Compasses point to spawn in the Overworld, to the portal you came through in the Nether, and to the exit portal in the End | Compasses point to spawn in the Overworld, to your entry portal in the Nether and to the exit portal in the End | 136 characters, over the length limit |
| 149 | Concrete is made by wetting powder | Concrete powder hardens into concrete when it touches water | clearer |
| 151 | Item frames can be placed on floors | Item frames go on any face, floors and ceilings included; a glass item frame hides its frame | Quark Glass Item Frame |
| 155 | Sugar speeds up baby horses | Feed baby horses sugar, wheat, apples or hay to make them grow faster | sister foods |
| 158 | Zombies can break wooden doors | Zombies break wooden doors on Hard difficulty | condition |
| 160 | Slimes only spawn in certain chunks | Slimes spawn in swamps at night and in certain chunks below y 40 | sister case |
| 170 | Sculk catalysts create sculk blocks | Sculk catalysts spread sculk where mobs die nearby | clearer |
| 172 | Soul torches give off low light | Soul torches give less light than torches but do not melt snow or ice | the useful half |
| 183 | Crying obsidian cannot be moved by pistons | Obsidian and crying obsidian cannot be pushed by pistons | sister block |
| 193 | Armor stands can hold any item | Armor stands have arms and can hold an item in each hand | Quark Armed Armor Stands |
| 198 | TNT can be lit with fire charges | TNT is lit by flint and steel, fire charges, fire, redstone or Flame arrows | sister sources |
| 269 | Phantoms spawn if you haven't slept for three days | Phantoms spawn after three nights without sleep; wind chimes keep them away | Chimes repel Phantoms |
| 283 | Tropical fish can be carried in buckets | Fish, axolotls and tadpoles can be carried in water buckets | sister mobs |

### 2.3 Removed with no replacement

| # | Tip | Why |
|---|---|---|
| 36 | Jukeboxes play music discs | not informative |
| 38 | Guardians and Elder Guardians hate being out of water! | trivia |
| 102 | Dragon heads look cool on walls | trivia |
| 137 | Strider babies can ride adults | trivia |
| 162 | Illusioners are hidden in the code | wrong here: the pack spawns them at large pillager towers and caravans (structure_tuning.md). Pending replacement once ST-322 passes: 'Illusioners, which blind you and make decoys, appear at large pillager towers and caravans' |
| 165 | Spore blossoms create green particles | trivia |
| 176 | Nether sprouts are just for looks | trivia |
| 184 | Bedrock is unbreakable in survival | trivia |
| 185 | End portal frames are unbreakable | trivia |
| 191 | Dragon eggs are unique trophies | trivia |
| 199 | Hardcore mode means one life only | trivia: chosen at world creation |
| 225 | Fire Aspect is great for combat, but careful with Endermen! | vague; cooked drops are in the fire tip |
| 260 | Ocelots no longer turn into Cats; they are separate mobs | 'no longer': history, not a tip (style rule) |
| 266 | Evokers turn blue sheep into red sheep with a 'Wololo' | trivia |
| 329 | Holding F3+C for 10 seconds intentionally crashes the game for debugging purposes! | not a play tip (crashes the game) |

### 2.4 Merged-away tips (for reference)

| # | Tip | Into |
|---|---|---|
| 11 | Smelt sand to get glass | 6 |
| 22 | Ancient debris is found deep in the Nether | 306 |
| 29 | Blast furnaces smelt ore faster | 28 |
| 40 | Honeycomb prevents copper oxidation | 1 |
| 41 | Amethyst grows on budding geodes | 295 |
| 49 | Melons need space to grow | 48 |
| 52 | The Wither is a powerful boss | 303 |
| 53 | Infinity bows don't need extra arrows, but can't have Mending! | 232 |
| 57 | Depth Strider makes you swim faster | 64 |
| 58 | Frost Walker turns water into ice | 333 |
| 61 | Projectile Protection reduces arrow damage | 60 |
| 62 | Fire Protection is essential for the Nether | 60 |
| 65 | Aqua Affinity speeds up underwater mining | 64 |
| 69 | Gilded apples give massive buffs | 68 |
| 81 | Golden apples help cure Zombie Villagers | 80 |
| 86 | Crouching prevents magma block damage | 85 |
| 91 | Cauldrons can hold water in the Nether | 90 |
| 93 | Wet sponges can be dried in a furnace or by placing them in the Nether! | 92 |
| 96 | Nether stars are dropped by the Wither | 303 |
| 97 | Wither Skeletons drop rare skulls | 302 |
| 101 | Shulker shells are dropped in End Cities | 100 |
| 105 | Endermites sometimes spawn from pearls | 66 |
| 113 | Carrot on a stick guides pigs | 109 |
| 117 | Hero of the Village gives discounts | 262 |
| 120 | Pumpkin masks hide you from Endermen | 5 |
| 124 | Pressure plates detect items or players | 123 |
| 127 | Slabs prevent mob spawning | 9 |
| 130 | Gates can be opened both ways | 129 |
| 131 | Anvils fall and deal damage | 147 |
| 132 | Dripstone can kill if it falls on you | 147 |
| 134 | Powder snow can freeze you | 135 |
| 140 | Zoglins are afraid of nothing | 139 |
| 145 | Sweet berry bushes damage mobs | 252 |
| 148 | Gravel falls if there is no block below | 147 |
| 150 | Wool muffles the sound of footsteps | 206 |
| 152 | Glow ink sacs make signs glow | 215 |
| 153 | Regular ink sacs clear sign glow | 215 |
| 157 | Rotten flesh heals injured wolves | 17 |
| 163 | The Warden reacts to vibrations | 204 |
| 164 | Sneaking hides you from the Warden | 204 |
| 168 | Amethyst clusters drop four shards | 295 |
| 169 | Sculk shriekers summon the Warden | 204 |
| 171 | Bells can be rung with arrows | 51 |
| 173 | Respawn anchors need glowstone | 321 |
| 175 | Gilded blackstone drops gold nuggets | 174 |
| 177 | Crimson fungus breeds Hoglins | 139 |
| 179 | Weeping vines grow downward | 178 |
| 186 | Silverfish hide inside stone bricks | 104 |
| 192 | Beacons can be colored with glass | 95 |
| 195 | Sticky pistons pull blocks back | 194 |
| 196 | Slime and honey don't stick together | 250 |
| 201 | Echo shards are needed to craft a recovery compass | 200 |
| 203 | Swift Sneak allows you to walk faster while crouching | 202 |
| 205 | Throw projectiles to distract the Warden away from you | 204 |
| 208 | Play a jukebox near an Allay to make it dance! | 207 |
| 209 | Give a dancing Allay an amethyst shard to duplicate it | 207 |
| 210 | Mangrove Swamps are home to mud blocks and Frogs | 44 |
| 211 | Frogs produce froglight when they eat small Magma Cubes | 44 |
| 212 | Different Frog colors produce different colors of froglight | 44 |
| 213 | Tadpoles grow into different Frogs depending on the biome | 44 |
| 214 | Goat horns are dropped when a Goat rams a hard block | 43 |
| 217 | Piercing crossbows allow one arrow to hit multiple enemies | 218 |
| 219 | Quick Charge reduces the reload time of your crossbow | 218 |
| 220 | Facing a Creeper blast with a shield cuts the damage; better shields block more | 7 |
| 224 | Bane of Arthropods makes Spiders much easier to deal with | 223 |
| 228 | Channeling tridents summon lightning during thunderstorms | 227 |
| 229 | Riptide tridents allow you to fly through rain and water | 227 |
| 230 | Punch bows increase the knockback of your arrows | 232 |
| 231 | Flame bows ignite enemies and can light TNT from afar | 232 |
| 233 | Power V bows can one-shot most common mobs | 232 |
| 238 | Daylight sensors can be inverted to turn on lights at night | 144 |
| 240 | Lecterns send a signal when a page is turned | 32 |
| 242 | Hoppers can be disabled by a redstone signal | 34 |
| 246 | Detector rails send a signal when a minecart passes over | 245 |
| 247 | Activator rails can eject players from minecarts | 245 |
| 249 | Sticky pistons can pull other pistons to create complex doors | 194 |
| 251 | Honey blocks prevent jumping but allow you to slide down walls | 250 |
| 254 | A Fox holding a Fire Aspect sword can set you on fire! | 253 |
| 257 | Sneezing baby Pandas can drop slimeballs | 256 |
| 259 | Don't feed chocolate cookies to Parrots; it's poisonous! | 258 |
| 261 | Villager gossip determines the price of their trades | 262 |
| 264 | You can repair an Iron Golem with iron ingots | 263 |
| 270 | Phantom membranes are used to repair elytra | 98 |
| 273 | Conduits protect you underwater and kill nearby mobs | 94 |
| 274 | Turtle shells give you extra breath time underwater | 64 |
| 277 | Magma blocks underwater create bubble columns that pull you down | 278 |
| 280 | Dried kelp blocks are a very efficient fuel for furnaces | 332 |
| 285 | Axolotls will play dead to regenerate health | 37 |
| 286 | Glow Squids drop glow ink sacs for glowing signs | 215 |
| 288 | Dripstone Caves contain sharp stalactites that can fall on you | 147 |
| 291 | Copper blocks turn green over time due to oxidation | 1 |
| 293 | Spyglasses are crafted from amethyst and copper | 42 |
| 296 | Budding amethyst blocks cannot be moved, even with Silk Touch | 295 |
| 298 | Raw iron, copper, and gold can be smelted faster in a blast furnace! | 28 |
| 304 | Beacons provide buffs like Haste and Regeneration | 95 |
| 308 | Striders can walk on lava and be ridden with a saddle | 109 |
| 309 | Warped fungus on a stick is used to control Striders | 109 |
| 316 | Soul Speed enchantment makes you run faster on soul sand | 24 |
| 317 | Netherite scrap is made by smelting ancient debris | 306 |
| 318 | Bastions are the best place to find Pigstep music discs | 141 |
| 320 | Crying obsidian can be used to craft respawn anchors | 321 |
| 322 | Don't use a respawn anchor in the Overworld; it will explode! | 321 |
| 324 | Gilded blackstone has a chance to drop gold nuggets | 174 |
| 326 | Packed ice doesn't melt, even near light sources | 87 |
| 327 | Snow Golems wear pumpkins that can be sheared off | 119 |
| 328 | Leads can be used to tie mobs to fences | 107 |
| 330 | Bamboo is the least efficient fuel, smelting only 0.25 items each! | 332 |

## 3. Fact checks still open

The tip is applied as written; check it in game or in code, and fix the tip if the check fails.

| # | Tip now | Check |
|---|---|---|
| 19 | Milk removes all potion effects | milk vs Tipsy (alcohol.md 2.4 says milk removes one Tipsy level; doc may be old) |
| 50 | Campfires place unlit and cook four items; rain puts one out unless a coal or charcoal block is under it | campfire kindling (CT-098) will change this tip |
| 60 | Protection, Fire, Blast and Projectile Protection exclude each other; each specific one guards best against its own damage | Allurement may relax protection exclusivity |
| 95 | A beacon's pyramid size unlocks effects, a richer base block strengthens them, payment sets the range, glass colors the beam | Better Beacons payment range and base amplifier wording |
| 106 | Wandering Traders offer exotic items | wandering_trader changes and MCA inn traders |
| 156 | Raw fish heals injured cats | Cozy's Improved Cats healing foods |
| 302 | Wither Skeletons spawn only in Nether Fortresses and sometimes drop their skull | Wither Skeleton spawns outside fortresses (YUNG's, worldgen) |
| 306 | Ancient debris is most common around y 15 in the Nether and smelts into netherite scrap | debris height with the pack's Nether worldgen |
| 332 | A coal block smelts 80 items, 8 more than its nine coal; a dried kelp block smelts 20 and bamboo only 0.25 | fuel values with fuelgoeshere and pack fuel edits |

## 4. Tests

| ID | Setup | Action | Expected |
|---|---|---|---|
| LTP-15 | client | F3+T or start the game, load a world | `latest.log` loading tips line: 213 tips, no unmatched id; tips read as section 2 |


Scratch run r1115 (2026-11-15, [scratch_run_2026-11-15.md](testing/scratch_run_2026-11-15.md) 8): SQ-QN-01 PASS (Mending repairs in full, Quark Nerf Mending off), SQ-QN-02 PASS (elytra glide in the Overworld, Dimension Locked Elytra off; hand feel left to the owner): tips 54 and 98 match the game.

## 5. History

- 2026-11-14: audit 2 written and applied to Creative Tips; style rules extended (concise and informative, full
  picture of one feature, one tip per feature, 120 / 130 characters, check against the pack).
