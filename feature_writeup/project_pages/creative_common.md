# Creative Common

**The shared library behind the Creative mods.** Creative Common is the core that every Creative mod is built on. On its own it adds no blocks or items. It provides the shared code, scripting helpers and data the other Creative mods need, plus bug fixes and performance fixes that help any large Forge 1.19.2 pack.

***

## Mods that require Creative Common

*   **Creative Engineering**: machines and engineering
*   **Creative Delight**: food, cooking and Farmer's Delight addons
*   **Creative Brewing**: brewing, alcohol and potions
*   **Creative Equipment**: armor, tools and gear
*   **Creative Magic**: magic mod integration
*   **Creative Worldgen**: world generation and wildlife
*   **Creative Calendar**: seasons and time
*   **Creative Compat**: compatibility between third-party mods, and the JEI, WTHIT and tooltip improvements
*   **Creative Devtools**: pack development tools

***

## What it does on its own

Every feature below works without any other Creative mod. Features that touch another mod only switch on when that mod is installed.

### For mod and pack authors (KubeJS)

<div class="spoiler"><ul><li><strong>KubeJS scripts loaded from mod jars.</strong> Creative mods ship their KubeJS scripts inside their own jars, so a mod can be dropped into any pack without copying files. Creative Common's own scripts and any jar script headed <code>// phase: helpers</code> load before the pack's <code>startup_scripts</code>, so the pack can use them; every other jar script loads after.</li><li><strong>Shared helpers</strong> for recipes, tags, composting, villager and wandering trader trades, and JEI ordering.</li><li><strong>Item, block, fluid, food and effect dictionaries</strong> that skip any mod that is not installed, so scripts can name optional content safely. Recipe removals that name a missing mod are skipped, and one summary line lists the absent mods instead of an error per recipe.</li><li><strong>Armor and tool effect frameworks</strong>: scripts can give armor sets and held tools passive effects and on-hit effects, with matching tooltip lines.</li><li><strong>Swap lists</strong>: when a mod renames or removes an item, block, effect or potion, existing worlds keep working. Old ids are remapped on load, potions made before a rename keep their effect, and items that were merged into another are converted in inventories, containers and on the ground. Renamed ids stay mapped for good, so data saved under an old name still loads on every later start, not only the first one. Block entity, entity and item data that a mod stores under a renamed key is moved to the new key. For ten minutes after a server starts, any listed id that still fails to load is counted and logged by name. Operators can run <code>/ccswap items</code> to convert everything loaded right away.</li><li><strong>Plantable item tags</strong>: every item that plants something is sorted into seeds, plantable crops, saplings, fungi, water plants, vines or vertical crops (<code>#creativecommon:plantable/...</code>). The kind is read from the block the item places, so plants from any mod are covered without a list.</li><li><strong>Data-driven damage tags</strong> that group damage sources (fire, magic, and so on) for other mods to use. Minecraft 1.19.2 has no damage types of its own.</li><li><strong>Loot tables that name optional mods</strong> load without errors when those mods are missing.</li><li><strong>Server profiling events</strong>: when a Java Flight Recorder recording is running, Creative Common writes events for every server tick, each dimension's tick, chunk waits (with the requesting stack), KubeJS event handlers (by script and line), and entity and block entity time by type. Nothing is recorded or paid for unless a recording enables them.</li><li><strong>Trade sync</strong>: on a dedicated server, clients receive the real villager and wandering trader trades, so recipe viewers that show trades list the server's offers instead of vanilla's.</li></ul></div>

### Interface

<div class="spoiler"><ul><li><strong>In-game changelog viewer</strong> on the title screen and pause menu. It only appears when a pack ships a changelog.</li></ul><p><img src="https://media.forgecdn.net/attachments/2164/300/2026-11-07_17-21-27-png.png" alt="Changelog viewer"></p><ul><li><strong>Loading screen tip bar</strong>: tips roll across the bottom of the world loading screen. Tip packs use the Loading Screen Tips format; over 300 general Minecraft tips are built in.</li></ul><p><img src="https://media.forgecdn.net/attachments/2164/299/screenshot-from-2026-11-07-17-28-10-png.png" alt="Changelog viewer"> <img src="https://media.forgecdn.net/attachments/2164/298/screenshot-from-2026-11-07-17-28-55-png.png" alt="Changelog viewer"></p><ul><li><strong>Mod display names</strong>: rename how any mod is shown in JEI, WTHIT and the Mods screen. The list starts empty.</li></ul></div>

### Effects and potions

<div class="spoiler"><ul><li><strong>Magic Resistance</strong>: a new effect that reduces magic damage and shortens poison and wither, with potions (normal, long and strong).</li><li><strong>Effect rules in the config</strong>: change an effect's category (beneficial, harmful, neutral), swap one effect for another wherever it is applied, block effects from being applied at all, and choose which hidden effects keep their HUD icon. No effect is swapped or blocked by default. (Creative Compat draws the icons and effect descriptions.)</li></ul></div>

### Bug fixes

<div class="spoiler"><ul><li><strong>Ghost blocks on dedicated servers</strong>: vanilla 1.19.2 corrupts multi-block updates once a pack has more than about 524,000 block states. Fixed, along with the client-side crash from a missing block state in those packets.</li><li><strong>Creative pick block deleting the held item</strong> on servers (MC-197793).</li><li><strong>Chat key typed into the chat box</strong>: opening chat with <code>/</code> or <code>T</code> sometimes also typed that key into the box (MC-122477).</li><li><strong>Client never exiting after quit</strong>: on some NVIDIA drivers the game window closes but the process keeps running.</li><li><strong>"Accessing LegacyRandomSource from multiple threads" crashes</strong>: each world's shared random is made thread safe.</li><li><strong>Poison and Wither damage told apart from other magic damage</strong>: shields and resistance effects can tell an effect's periodic tick from an ordinary hit that uses the same vanilla damage source.</li><li><strong>Unbound holder crashes</strong> from mods that check tags on unregistered entries.</li></ul></div>

***

## Optional pack settings

<div class="spoiler"><p>Out of the box, Creative Common leaves vanilla behaviour alone (the Create: Gravity limit below is the one performance setting that is on). The *Creative Engineering* pack turns these on in its own config, and any pack can do the same in <code>config/creativecommon-common.toml</code>:</p><table><thead><tr><th>Setting</th><th>What it does</th></tr></thead><tbody><tr><td><code>[advancements] enabled = false</code></td><td>Loads no advancements at all (vanilla, mods and datapacks)</td></tr><tr><td><code>[wandering_trader] enabled = true</code></td><td>Sets the Wandering Trader's spawn interval and number of offers</td></tr><tr><td><code>[perf] gravityLimitDimensions = true</code></td><td>Create: Gravity's per-entity work and Creative Common's low gravity for items, blocks and projectiles only run in the listed dimensions (default the End)</td></tr><tr><td><code>[creative_menu] disabled = true</code></td><td>Creative players open the survival inventory, which avoids the long freeze of building the creative search in large packs; JEI's cheat mode covers item browsing</td></tr></tbody></table><p>Most other features have a switch in the same file. With Create and Creative Compat installed, the file can also be edited in game through Create's config screen.</p></div>

***

## Optional mods: what changes with each

<div class="spoiler"><p>None of these are required. Each change only switches on when that mod is installed. Changes that alter another mod's code are tied to the versions given; with any other version they are turned off and one warning is logged, so an update to that mod never crashes the game.</p><p> <b> Performance and stability </b> </p><ul><li><strong>Create: Gravity (1.2.x)</strong><ul><li>Its per-entity tick logic only runs in the dimensions that can have gravity (performance).</li><li>Low gravity also applies to dropped items, falling blocks, lit TNT, minecarts, experience orbs, fishing bobbers, arrows, tridents, thrown items, llama spit, Shulker bullets and boats out of water. Modded entities with their own gravity code are not affected.</li><li>Low gravity works from the moment a dedicated server starts; without this fix it only began after a <code>/reload</code>.</li><li>With ItemPhysic (1.6.7 or newer in 1.6.x), dropped items follow the same low gravity.</li></ul></li><li><strong>Immersive Weathering (1.2.x)</strong><ul><li>Faster random ticks.</li></ul></li><li><strong>WorldEdit (7.2.x)</strong><ul><li>Skips building its full block state table at server start (a long stall in large packs).</li></ul></li><li><strong>Quark (3.4)</strong><ul><li>Teleports into unexplored terrain no longer force chunk generation.</li></ul></li><li><strong>Nock Enough Arrows (1.2.x)</strong><ul><li>The whistle goal skips its search when no whistling arrow exists.</li></ul></li><li><strong>Armor Trims (1.3.x)</strong><ul><li>Item lists are cached.</li></ul></li><li><strong>Immersive Portals (2.3.x) with Serene Seasons</strong><ul><li>Crash at world creation and on joining a world fixed.</li></ul></li><li><strong>Immersive Portals (2.3.x) with Heartstone (1.1.x)</strong><ul><li>Client crash at launch fixed.</li></ul></li></ul><p><b>Effects</b></p><ul><li><strong>Better Beacons (1.0.x)</strong><ul><li>One player with Phantom Bane no longer stops phantom spawns for every player after them.</li></ul></li></ul></div>

***

## Requirements

|                        |Version                               |
| ---------------------- |------------------------------------- |
| Minecraft              |1.19.2                                |
| Forge                  |43 or newer                           |
| KubeJS                 |1902.6.2-build.73 or newer (1902.6.x) |
| Rhino and Architectury |required by KubeJS                    |

Everything else is optional; see "Optional mods: what changes with each" above.

Both client and server need it.

***

## Source, issues and licence

*   Source code and issue tracker: [github.com/SaintAlfonzo/CreativeEngineering](https://github.com/SaintAlfonzo/CreativeEngineering)
*   Licence: GNU GPL v3. Includes code adapted from Immersive Weathering by Ordana, MehVahdJukaar and Keybounce (LGPL v3), a shaded copy of commonmark-java (BSD 2-Clause), and loading screen tips from Loading Tips by Naw7k (MIT). The tip format follows Loading Screen Tips by UltrusBot (MIT). The full notices are in the jar.
*   Free to include in modpacks.