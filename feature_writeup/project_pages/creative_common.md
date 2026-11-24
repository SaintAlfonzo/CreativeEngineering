# Creative Common

**The shared library behind the Creative mods.** Creative Common is the core that every Creative mod is built on. On its own it adds three effects (Magic Resistance, Wither Immunity and Brain Freeze) and potions of 12 vanilla effects (plus Magic Resistance). It provides the shared code, scripting helpers and data the other Creative mods need, pack switches (advancements, the recipe book, the creative menu, chat signing), an in-game changelog viewer, loading screen tips, effect rules, and bug fixes and performance fixes that help any large Forge 1.19.2 pack.

***

***

## Mods that require Creative Common

*   **Creative Engineering**: machines and engineering
*   **Creative Delight**: food, cooking and Farmer's Delight addons
*   **Creative Brewing**: brewing, alcohol and potions
*   **Creative Equipment**: armor, tools and gear
*   **Creative Magic**: magic mod integration
*   **Creative Loot**: loot, drops and trades
*   **Creative Labels**: sticky notes and labels
*   **Creative Worldgen**: world generation and wildlife
*   **Creative Calendar**: seasons and time
*   **Creative Compat**: compatibility between third-party mods, and the JEI, WTHIT and tooltip improvements
*   **Creative Devtools**: pack development tools

***

## What it does on its own

Every feature below works without any other Creative mod. Each feature has a bold name and a dropdown with its description. Features that touch another mod only switch on when that mod is installed.

### For mod and pack authors (KubeJS)

**KubeJS scripts loaded from mod jars**

<div class="spoiler"><p>Creative mods ship their KubeJS scripts inside their own jars, so a mod can be dropped into any pack without copying files. Creative Common's own scripts and any jar script headed <code>// phase: helpers</code> load before the pack's <code>startup_scripts</code>, so the pack can use them; every other jar script loads after.</p></div>

**Shared helpers**

<div class="spoiler"><p>Helpers for recipes, tags, composting, villager and wandering trader trades, and JEI ordering.</p></div>

**Item, block, fluid, food and effect dictionaries**

<div class="spoiler"><p>Dictionaries that skip any mod that is not installed, so scripts can name optional content safely. Recipe removals that name a missing mod are skipped, and one summary line lists the absent mods instead of an error per recipe.</p></div>

**Armor and tool effect frameworks**

<div class="spoiler"><p>Scripts can give armor sets and held tools passive effects and on-hit effects, with matching tooltip lines.</p></div>

**Swap lists and config carry-over**

<div class="spoiler"><p>When a mod renames or removes an item, block, effect or potion, existing worlds keep working. Old ids are remapped on load, potions made before a rename keep their effect, and items that were merged into another are converted in inventories, containers and on the ground. Renamed ids stay mapped for good, so data saved under an old name still loads on every later start, not only the first one. Block entity, entity and item data that a mod stores under a renamed key is moved to the new key. For ten minutes after a server starts, any listed id that still fails to load is counted and logged by name. Operators can run <code>/ccswap items</code> to convert everything loaded right away.</p><p>The same mechanism copies config sections that move from one Creative jar to another, so edited values survive an update.</p></div>

**Plantable item tags**

<div class="spoiler"><p>Every item that plants something is sorted into seeds, plantable crops, saplings, fungi, water plants, vines or vertical crops (<code>#creativecommon:plantable/...</code>). The kind is read from the block the item places, so plants from any mod are covered without a list.</p></div>

**Data-driven damage tags**

<div class="spoiler"><p>Tags that group damage sources (fire, magic, and so on) for other mods to use. Minecraft 1.19.2 has no damage types of its own. Setting: <code>[damage_tags] logSeenSources</code> logs each damage source once, for finding sources no tag covers (off by default).</p></div>

**Loot tables that name optional mods**

<div class="spoiler"><p>Loot tables that name optional mods load without errors when those mods are missing.</p></div>

**Server profiling events**

<div class="spoiler"><p>When a Java Flight Recorder recording is running, Creative Common writes events for every server tick, each dimension's tick, chunk waits (with the requesting stack), KubeJS event handlers (by script and line), and entity and block entity time by type. Nothing is recorded or paid for unless a recording enables them.</p></div>

**Trade sync**

<div class="spoiler"><p>On a dedicated server, clients receive the real villager and wandering trader trades, so recipe viewers that show trades list the server's offers instead of vanilla's.</p></div>

**Config reload**

<div class="spoiler"><p>Changes saved to the pack's own config files while the game runs are applied after about a second, without a restart. (Creative Compat draws the effect icons and effect descriptions.)</p></div>

### Pack switches

These change vanilla behavior only when switched on in <code>config/creativecommon-common.toml</code> (or <code>creativecommon-client.toml</code> for the client keys). The Creative Engineering pack turns them on.

**No advancements**

<div class="spoiler"><p><code>[advancements] enabled = false</code> loads no advancements at all (vanilla, mods and datapacks), so none show, toast or give rewards. Default: advancements on. Applies on the next <code>/reload</code> or world start.</p></div>

**No recipe unlocks**

<div class="spoiler"><p><code>[recipe_book] unlocks = false</code>: no recipe is ever unlocked (crafting, furnaces, the knowledge book, <code>/recipe give</code>, other mods), so the recipe book gains no entry and no new-recipe toast shows. Entries already in a save stay. Default: unlocks on.</p></div>

**Survival inventory for creative players**

<div class="spoiler"><p>Client side. <code>[creative_menu] disabled = true</code> makes creative players open the survival inventory instead of the creative tabs, which avoids a multi-second freeze when the creative search is built in a large pack; JEI's cheat mode covers item browsing. Default: off (vanilla).</p></div>

**Items without a creative tab**

<div class="spoiler"><p>Client side. Items that have no creative tab are hidden from creative mode, its search and JEI. <code>[creative_menu] orphanTabItems</code> lists the ones to show; each goes into the tab most other items of its mod use. Only listed items change. The default list holds the Abnormals Delight compat foods that have pack recipes while their parent mods are absent. Applies on the next game start.</p></div>

### Chat and privacy

**Chat reporting off**

<div class="spoiler"><p>Client side, on by default. The game uses Mojang's offline user API and never gets a chat signing key, so nothing typed in chat is signed or can be reported to Mojang, no abuse reports or telemetry are sent, and the Microsoft account block list is not loaded. The "Not Secure" chat bar and the "Chat messages can't be verified" toast are hidden. Servers that enforce secure chat refuse players without a key; set <code>[privacy] blockMojangReporting = false</code> in <code>creativecommon-client.toml</code> to play on those. Login and skins are unaffected. Applies on the next game start.</p></div>

**Unsigned chat on dedicated servers**

<div class="spoiler"><p>On by default. <code>[privacy] acceptUnsignedChat</code> lets players join a dedicated server without a chat signing key, whatever <code>enforce-secure-profile</code> says in <code>server.properties</code>. Set it to false and <code>server.properties</code> decides.</p></div>

### Interface

**In-game changelog viewer**

<div class="spoiler"><p>A book button on the title screen and the pause menu opens the changelog, and an update popup can show it after an update. It only appears when a pack ships a changelog. Settings: <code>[changelog]</code> (client): master switch, each button and the popup.</p><p><img src="https://media.forgecdn.net/attachments/2164/300/2026-11-07_17-21-27-png.png" alt="Changelog viewer"></p></div>

**Loading screen tip bar**

<div class="spoiler"><p>Tips roll across the bottom of the world loading screen. Tip packs use the Loading Screen Tips format. 532 general Minecraft tips are built in; 106 that describe content newer than Minecraft 1.19.2 are filtered out, so 426 show on Creative Common alone (a tip pack can remove or replace tips by id). Settings: <code>[loading_tips] enabled</code> and <code>cycleSeconds</code> (10) in <code>creativecommon-client.toml</code>.</p><p><img src="https://media.forgecdn.net/attachments/2164/299/screenshot-from-2026-11-07-17-28-10-png.png" alt="Loading screen tips"> <img src="https://media.forgecdn.net/attachments/2164/298/screenshot-from-2026-11-07-17-28-55-png.png" alt="Loading screen tips"></p></div>

**Mod display names**

<div class="spoiler"><p>Rename how any mod is shown in JEI, WTHIT and the Mods screen with <code>[mod_names] overrides</code>. The list starts empty.</p></div>

**Effect list in two columns**

<div class="spoiler"><p>Client side. The inventory effect list shows timed effects in the first column and permanent ones in a second column to its right. It stays a single column when the screen is narrow, there is no room for two columns, or every effect is of one kind.</p></div>

### World and performance

**Idle random ticks**

<div class="spoiler"><p>Blocks that can no longer change on a random tick (full-grown colonies, waxed lanterns, flower bushes) stop asking for them, so chunk sections holding only such blocks are skipped. Settings: <code>[perf] idleRandomTicks</code> and <code>idleRandomTickRules</code> (one line per block and state); restart to apply.</p></div>

**Worn horse armor effects**

<div class="spoiler"><p><code>[perf] mountEffectIntervalTicks</code> (100, 1 to 1200) sets how often the armor effect framework applies the effects of worn horse armor, instead of every tick. Effects last the interval plus 40 ticks, so they stay on.</p></div>

### Effects and potions

**Magic Resistance**

<div class="spoiler"><p>A new effect with potions (normal, long and strong). It reduces magic damage (potions, poison, evoker fangs, thorns, sonic booms, curses, life drain) by 20% per level, with full immunity from level V. It also stops the damage ticks of Poison and Wither up to its own level; a higher poison or wither level takes the per-level cut instead. Settings: <code>[magic_resistance] reductionPerLevel</code> (0.2) and <code>negatedEffectTicks</code>.</p></div>

**Wither Immunity**

<div class="spoiler"><p>A new effect; while it lasts, Wither cannot be applied and a Wither already present deals no damage. No potion; packs and other mods give it.</p></div>

**Brain Freeze**

<div class="spoiler"><p>A new harmful effect. Cold foods (smoothies, ice cream, popsicles, shaved ice, cold milkshakes and ice cubes, from the item tag <code>#creativecommon:brain_freeze</code>; Creative Delight fills it) eaten within 60 seconds of each other can freeze the player: the powdered snow overlay shows and freeze damage applies while it lasts, with a blue line above the hotbar. The chance is 20% on the first food in a row and rises 20% with each further one; it lasts 5, 8, 11, 14 or 20 seconds by how many foods it took. Settings: <code>[brain_freeze]</code> (<code>enabled</code>, <code>windowSeconds</code>, <code>chanceStep</code>, <code>lengths</code>). With no mod filling the tag it never triggers.</p></div>

**Potions of 12 vanilla effects**

<div class="spoiler"><p>Resistance, Health Boost, Absorption, Glowing, Saturation, Wither, Levitation, Mining Fatigue, Hunger, Nausea, Blindness and Darkness, with long and strong forms where they make sense. Creative Common only registers them; without a mod or pack that adds brewing recipes (Creative Brewing does) they are only in the creative menu and commands.</p></div>

**Effect rules in the config**

<div class="spoiler"><p>In <code>[effects]</code>: change an effect's category (<code>categoryOverrides</code>: beneficial, harmful, neutral), swap one effect for another wherever it is applied (<code>remap</code>), block effects from being applied at all (<code>removed</code>), and choose which hidden effects keep their HUD icon (<code>forceHudIcon</code>, and <code>forceHudIconItems</code> for effects an item gives). No effect is swapped, blocked or recategorized by default.</p></div>

**Level caps**

<div class="spoiler"><p><code>[effects] levelCaps</code> holds effects that do the same at every level at level I from every source, tooltips included. The default list holds 87 effects that do the same at every level (Fire Resistance, Water Breathing, Night Vision and so on); empty the list to turn it off.</p></div>

### Bug fixes

**Ghost blocks on dedicated servers**

<div class="spoiler"><p>Vanilla 1.19.2 corrupts multi-block updates once a pack has more than about 524,000 block states. Fixed, along with the client-side crash from a missing block state in those packets.</p></div>

**Creative pick block deleting the held item**

<div class="spoiler"><p>Fixed on servers (MC-197793).</p></div>

**Chat key typed into the chat box**

<div class="spoiler"><p>Opening chat with <code>/</code> or <code>T</code> sometimes also typed that key into the box (MC-122477). Fixed.</p></div>

**Client never exiting after quit**

<div class="spoiler"><p>On some NVIDIA drivers the game window closes but the process keeps running. Fixed.</p></div>

**Crash at launch with about 420 or more mods**

<div class="spoiler"><p>Forge leaves a file handle open for each mod jar, which made the window fail to open on a client with about 420 or more jars. The handles are freed just before the window is created.</p></div>

**"Accessing LegacyRandomSource from multiple threads" crashes**

<div class="spoiler"><p>Each world's shared random is made thread safe.</p></div>

**Poison and Wither damage told apart from other magic damage**

<div class="spoiler"><p>Shields and resistance effects can tell an effect's periodic tick from an ordinary hit that uses the same vanilla damage source.</p></div>

**Unbound holder crashes**

<div class="spoiler"><p>Fixed crashes from mods that check tags on unregistered entries.</p></div>

**KubeJS in-memory data listing**

<div class="spoiler"><p>KubeJS's in-memory datapack listed files from a sibling folder whose name only starts with the requested one. It now lists only the folder asked for.</p></div>

***

## Optional pack settings

<div class="spoiler"><p>Out of the box, Creative Common leaves vanilla behavior alone except for the settings marked on below (level caps, idle random ticks, Create: Gravity's dimension limit, Brain Freeze, the tip bar, the changelog viewer, and chat signing off), each of which has a switch or list in the config. The Creative Engineering pack turns the pack switches on in its own config, and any pack can do the same in <code>config/creativecommon-common.toml</code> (client keys in <code>creativecommon-client.toml</code>):</p><table><thead><tr><th>Setting</th><th>What it does</th></tr></thead><tbody><tr><td><code>[advancements] enabled = false</code></td><td>Loads no advancements at all (vanilla, mods and datapacks)</td></tr><tr><td><code>[recipe_book] unlocks = false</code></td><td>No recipe is ever unlocked, so the recipe book stays empty and no new-recipe toasts show</td></tr><tr><td><code>[creative_menu] disabled = true</code></td><td>Creative players open the survival inventory, which avoids the long freeze of building the creative search in large packs; JEI's cheat mode covers item browsing</td></tr><tr><td><code>[creative_menu] orphanTabItems</code></td><td>Items with no creative tab that should be listed</td></tr><tr><td><code>[privacy] acceptUnsignedChat = true</code></td><td>Dedicated servers accept players without a chat signing key (on by default)</td></tr><tr><td><code>[privacy] blockMojangReporting = true</code></td><td>Client file: chat is never signed or reportable (on by default)</td></tr><tr><td><code>[effects] remap</code>, <code>categoryOverrides</code>, <code>removed</code>, <code>forceHudIcon</code>, <code>forceHudIconItems</code></td><td>Swap, recategorize or block effects and choose HUD icons (all empty by default)</td></tr><tr><td><code>[effects] levelCaps</code></td><td>Highest level per effect (87 effects at level I by default)</td></tr><tr><td><code>[magic_resistance]</code>, <code>[brain_freeze]</code></td><td>Tuning of those two effects</td></tr><tr><td><code>[perf] gravityLimitDimensions = true</code></td><td>Create: Gravity's per-entity work only runs in the dimensions listed in <code>gravityDimensions</code> (default the End); Creative Compat's low gravity asks the same list</td></tr><tr><td><code>[perf] idleRandomTicks</code>, <code>idleRandomTickRules</code>, <code>mountEffectIntervalTicks</code></td><td>Random tick and horse armor effect throttles</td></tr><tr><td><code>[changelog]</code>, <code>[loading_tips]</code>, <code>[mod_names] overrides</code></td><td>Changelog viewer buttons and popup, tip bar and its timing, mod display names</td></tr><tr><td><code>[damage_tags] logSeenSources</code></td><td>Log each damage source once (off by default)</td></tr></tbody></table><p>Most other features have a switch in the same file. With Create and Creative Compat installed, the file can also be edited in game through Create's config screen.</p></div>

***

## Optional mods: what changes with each

None of these are required. Each change only switches on when that mod is installed. Changes that alter another mod's code are tied to the versions given; with any other version they are turned off and one warning is logged, so an update to that mod never crashes the game.

### Performance and stability (optional mods)

**Create: Gravity dimension limit (Create: Gravity 1.2.x)**

<div class="spoiler"><p>Its per-entity tick logic (a biome lookup and gravity update for every living entity) only runs in the dimensions listed in <code>[perf] gravityDimensions</code> (default the End). Low gravity for items, blocks and projectiles is Creative Compat's.</p></div>

**Faster random ticks (Immersive Weathering 1.2.x)**

<div class="spoiler"><p>Immersive Weathering's random tick handling is faster.</p></div>

**WorldEdit start-up stall (WorldEdit 7.2.x)**

<div class="spoiler"><p>Skips building its full block state table at server start (a long stall in large packs).</p></div>

**Teleport chunk generation (Quark 3.4)**

<div class="spoiler"><p>Teleports into unexplored terrain no longer force chunk generation.</p></div>

**Whistle goal (Nock Enough Arrows 1.2.x)**

<div class="spoiler"><p>The whistle goal skips its search when no whistling arrow exists.</p></div>

**Item list caching (Armor Trims 1.3.x)**

<div class="spoiler"><p>Item lists are cached.</p></div>

**World creation crash (Immersive Portals 2.3.x with Serene Seasons)**

<div class="spoiler"><p>Crash at world creation and on joining a world fixed.</p></div>

**Launch crash (Immersive Portals 2.3.x with Heartstone 1.1.x)**

<div class="spoiler"><p>Client crash at launch fixed.</p></div>

### Effects (optional mods)

**Phantom Bane (Better Beacons 1.0.x)**

<div class="spoiler"><p>One player with Phantom Bane no longer stops phantom spawns for every player after them in the list. When Phantom Bane ends on a player, Time Since Rest goes back to 0, as after sleeping.</p></div>

**Steady and Leaf Piercing (Fruits Delight)**

<div class="spoiler"><p>Steady (Fruits Delight's Astringent) also makes its holder immune to Immersive Engineering's Slippery and Neapolitan's Slipping, and pressure plates, tripwires and big dripleaf ignore the holder. Leaf Piercing arrows that pass through leaves leave a poof of that leaf's particles where they enter and a thin trail while inside, for up to 5 seconds.</p></div>

***

## Requirements

|                        |Version                               |
| ---------------------- |------------------------------------- |
| Minecraft              |1.19.2                                |
| Forge                  |43 or newer                           |
| KubeJS                 |1902.6.2-build.73 or newer (1902.6.x) |
| Rhino and Architectury |required by KubeJS                    |

Creative mods that need Creative Common: version 1.3.0 or later of Creative Common is required by every Creative jar listed above.

Everything else is optional; see "Optional mods: what changes with each" above.

Both client and server need it.

***

## Source, issues and license

*   Source code and issue tracker: [github.com/SaintAlfonzo/CreativeEngineering](https://github.com/SaintAlfonzo/CreativeEngineering)
*   License: GNU GPL v3. Includes code adapted from Immersive Weathering by Ordana, MehVahdJukaar and Keybounce (LGPL v3), a shaded copy of commonmark-java (BSD 2-Clause), and loading screen tips from Loading Tips by Naw7k (MIT). The tip format follows Loading Screen Tips by UltrusBot (MIT). The full notices are in the jar.
*   Free to include in modpacks.