# Spice of Life: Raspberry Pie Addition

**Choosable diet bonuses on top of Spice of Life: Apple Pie Edition.** Raspberry Pie turns food variety into a bonus ladder: extra hearts, armor toughness, knockback resistance, always-on effects, effect slots you fill yourself, and one-time effect unlocks. Eating many different and high value foods climbs the ladder; repeating the same and low value food does not. Drinks are kept out of the food diet and have their own book, the Liquid Diet Log. It works with only Minecraft Forge and its own jar: Apple Pie and Spice of Life: Carrot Edition come bundled inside it.

<!-- screenshot: the Diet Log open on the Diet Ladder page, with the progress bar and the key -->

***

Every feature below works in a standalone install. The ladder, the settings and the effect pool are plain files you can edit. A line that names a mod in brackets only applies when that mod is installed.

***

## <u><strong>The diet ladder and effect slots</strong></u>

How diversity give bonuses, the default ladder, and the effect slots you fill yourself.

### Diversity

<div class="spoiler"><ul><li><strong>Diversity drives everything:</strong> Apple Pie scores how varied your recent meals are. Raspberry Pie reads that score and applies every bonus at its threshold. The score is shown rounded down, so 4.97 does not count as 5.</li><li><strong>Unique-food gate:</strong> Below Apple Pie's <code>minFoodsToActivate</code> no bonus is active. By default (<code>uniqueFoodsToActivate = true</code>) the count is of different foods eaten since the counter last reset, so eating one food over and over never opens the gate. Apple Pie's own default for <code>minFoodsToActivate</code> is 0, which opens the gate at once.</li><li><strong>Unlocks follow your best diet:</strong> One-time unlocks are driven by the highest diversity you have ever reached, so a bad stretch of eating never takes them away, and editing the ladder updates existing players.</li></ul></div>

### Bonuses

<div class="spoiler"><ul><li><strong>Five kinds of bonus:</strong> Health tiers (extra hearts), attribute tiers (armor toughness, knockback resistance), permanent buffs (an effect that is always on while the threshold is reached, and is lost again if your diversity drops), effect slots, and one-time unlocks (effects you may put in a slot; never lost once reached).</li><li><strong>Apple Pie's own ladder, rescaled:</strong> The built-in default ladder is Apple Pie's default benefits (hearts, armor toughness, knockback resistance and always-on Speed, Haste, Strength, Regeneration and Luck) scaled to a top of 50, the most a diet of vanilla foods reaches. On top of that it adds four effect slots (at 5, 15, 27.5 and 40) and ten one-time unlocks: Regeneration I, Jump Boost I to III, Luck I, Night Vision, Water Breathing, Fire Resistance, Slow Falling and Glowing. 44 bonuses in all.</li><li><strong>Fully editable:</strong> Every threshold, type and amount is in one JSON file per world, <code>serverconfig/solraspberrypie/ladder.json</code>, copied from the mod's built-in default (or from <code>defaultconfigs/solraspberrypie/ladder.json</code> when a pack ships one). It is read at server start and sent to each player on login. The file also sets the pool of effects slots may hold and each one's highest level.</li><li><strong>Ladder updates reach existing worlds:</strong> A world's ladder copy that you never edited is replaced when the mod ships a new default (the old copy is kept as a dated backup). An edited copy is kept, with one warning in the log.</li></ul></div>

### Effect slots

<div class="spoiler"><ul><li><strong>You choose the effect:</strong> Each slot holds one effect at one level from the ladder's pool and applies it for as long as the slot is active. Open the effect screen with the P key or by clicking the bar on the survival inventory.</li><li><strong>Free changes:</strong> Filling an empty slot and changing a slot's level are free. Filled slots pack from slot 1 upward.</li><li><strong>Grayed slots:</strong> A slot whose threshold is no longer reached is grayed and cannot be used, but remembers its choice.</li><li><strong>No double dipping:</strong> An effect that is already a permanent buff is hidden from the picker, and a slot holding it is shown as covered and can be changed for free. A ladder may also mark one unlock as replacing another.</li><li><strong>Toggle effects:</strong> A ladder entry can be marked as a toggle: the player switches it on or off instead of choosing a level.</li></ul></div>

### Costs

<div class="spoiler"><ul><li><strong>Changing the effect costs foods:</strong> Swapping a slot to a different effect ages out the 5 oldest foods in your diet window (<code>respecFoods</code>); dropping a slot ages foods out until the highest active slot is lost. Aged-out foods stay in memory but count for nothing until eaten again, so every choice has a real cost in diversity.</li><li><strong>Dying costs the window:</strong> Death ages your whole diet window out (Apple Pie's own reset on death is not used, because it would wipe the memory first).</li><li><strong>Effects behave well:</strong> Diet effects are refreshed on a timer, and the effect icons in the inventory and the HUD can be replaced by the diet bar. Server operators can pause one player's diet effects with <code>/solrp pause</code> and undo it with <code>/solrp resume</code>.</li></ul></div>

***

## <u><strong>Books and tooltips</strong></u>

The Diet Log, the Liquid Diet Log for drinks, the Food Dictionary and food tooltips.

### Diet Log

<div class="spoiler"><ul><li><strong>Diet Log:</strong> Apple Pie's Food Book is renamed Diet Log and gains pages: an overview with the diversity value, the last 32 meals as a grid and how many more different foods the gate needs; the Diet Ladder with a progress bar and a key; and Milestones. Locked and unreached bonuses alternate between a placeholder icon and the effect; reached ones show the effect.</li><li><strong>Crafting:</strong> A book and any food. In a standalone install "food" is any item that can be eaten except drinks, which have their own book, and carrots. Packs can add to the exclusion tag <code>#solraspberrypie:diet_log_excluded</code>. Apple Pie's own recipe for the book is removed.</li><li><strong>Starting books:</strong> A new player gets both books once on first join (<code>giveBooksOnFirstJoin</code>).</li></ul></div>

<!-- screenshot: Diet Log overview page with the meal grid -->

### Drinks and the Liquid Diet Log

<div class="spoiler"><ul><li><strong>Liquid Diet Log:</strong> A second book that records unique drinks (the last 256) as a history only; it gives nothing. Crafted from a book and any drink in <code>#solraspberrypie:drinks</code> (potions, milk, and any drink tag that other mods provide).</li><li><strong>Drinks stay out of the diet:</strong> A drink has no diversity, does not age your other foods, does not count toward the gate and does not appear in the Diet Log.</li></ul></div>

### Food Dictionary and tooltips

<div class="spoiler"><ul><li><strong>Food Dictionary (bundled Spice of Life: Carrot Edition):</strong> Its Food Dictionary lists every food, eaten and uneaten, with sorting (mod and name, name, easiest, hunger, saturation, diversity, or your own order; eaten foods also by newest unlock or latest eaten).</li><li><strong>Food tooltips:</strong> Tooltips show "Diversity: X (contributes Y)", rounded down.</li></ul></div>

***

## <u><strong>Packs, servers and other mods</strong></u>

Food values from any mod or pack, operator commands, and what changes with optional mods.

### Food diversity tables

<div class="spoiler"><ul><li><strong>Any mod or pack can set food values:</strong> Raspberry Pie loads every <code>solraspberrypie/diversity/*.json</code> found in any loaded mod or datapack-style jar, so a food mod can say how much each of its foods is worth. Files merge by mod id then file name; a later file wins for an id both list. Ids of mods that are not installed are skipped with one summary line.</li><li><strong>Safe on bad files:</strong> A file of the wrong shape is skipped with one error line, and a value that is not a finite number of 0 or more is skipped.</li><li><strong>Nothing shipped:</strong> Raspberry Pie includes no table of its own: without one, Apple Pie's own values stand.</li><li><strong>Synced:</strong> A dedicated server sends its table to each client on login and on <code>/reload</code>, so tooltips and the Diet Log match the server.</li></ul></div>

### Commands

<div class="spoiler"><p>All need permission level 2.</p><ul><li><code>/solrp reset [players]</code>: clears a player's food memory.</li><li><code>/solrp diversity set [players] [value]</code>: sets a player's diversity to exactly that value (current and highest).</li><li><code>/solrp diversity get [player]</code>: shows current and highest diversity.</li><li><code>/solrp ladder reset</code>: replaces the world's ladder file with the current default (the old file is kept as a dated backup) and re-applies it to everyone online.</li><li><code>/solrp pause [player]</code>, <code>/solrp pause list</code> and <code>/solrp resume [player]</code>: stop and restore a player's diet effects.</li></ul></div>

### Optional mods

<div class="spoiler"><p>None of these are required. Changes that alter another mod's code are tied to the versions given; with any other version they are turned off and one warning is logged, so an update never crashes the game.</p><ul><li><strong>Just Enough Items (11 or newer):</strong> The overlay stays clear of the inventory diet bar; the internal marker item is hidden.</li><li><strong>AppleSkin (2.4.2):</strong> The food tooltip rows show diversity, with the saturation layout set by <code>saturationOwnLine</code>.</li><li><strong>Spice of Life: Carrot Edition (1.14.0 or newer):</strong> Already bundled; replacing or removing it never stops the game from starting. Its Food Dictionary gets the sorting above and is renamed.</li><li><strong>Creative Common:</strong> Diet effects count as permanent in its two-column inventory effect list. Not required.</li></ul></div>

***

## <u><strong>Settings and defaults</strong></u>

<div class="spoiler"><p>Raspberry Pie writes no Apple Pie settings. It runs on whatever the world's <code>solapplepie-server.toml</code> says, Apple Pie's defaults included. It logs one boxed warning at server start for a setting it cannot work with, and an info line for settings that only change how it behaves. Its own files:</p><table><thead><tr><th>Setting</th><th>Default</th><th>What it does</th></tr></thead><tbody><tr><td><code>[diet] uniqueFoodsToActivate</code> (per world)</td><td>true</td><td>The gate counts different foods, not every meal</td></tr><tr><td><code>[diet] respecFoods</code> (per world)</td><td>5</td><td>Foods aged out when a slot changes to a different effect</td></tr><tr><td><code>[diet] effectDuration</code> (per world)</td><td>800 ticks</td><td>Duration of every diet effect instance</td></tr><tr><td><code>[diet] refreshInterval</code> (per world)</td><td>400 ticks</td><td>How often diet effects are re-applied; keep it well below the duration</td></tr><tr><td><code>[diet] pauseEndsOnRelog</code> (per world)</td><td>false</td><td>Whether <code>/solrp pause</code> ends when the player logs out</td></tr><tr><td><code>[diet] giveBooksOnFirstJoin</code> (per world)</td><td>true</td><td>Both books on a player's first join</td></tr><tr><td><code>[dietLog] bookScale</code> (client)</td><td>1.4</td><td>Diet Log size; shrinks to fit a small window</td></tr><tr><td><code>[dietLog] extraWidth</code> (client)</td><td>48 pixels</td><td>Widens the Diet Log background; 0 is Apple Pie's own width</td></tr><tr><td><code>[dietLog.pages.*]</code> (client)</td><td>per page</td><td>Position and scale of each page's content</td></tr><tr><td><code>[dietLog] diversityIconFallback</code> (client)</td><td>vanilla seeds, rotten flesh, spider eye</td><td>Icon beside the diversity value while nothing has been eaten</td></tr><tr><td><code>[foodDictionary] uneatenSort</code>, <code>eatenSort</code>, <code>customOrder</code> (client)</td><td>mod and name; newest unlock; none</td><td>Food Dictionary order</td></tr><tr><td><code>[icons] unlock</code>, <code>permanentBuff</code>, <code>slot</code> (client)</td><td>awkward potion, diamond, item frame</td><td>Diet Log icons per bonus kind; first loaded item wins</td></tr><tr><td><code>[tooltips] saturationOwnLine</code> (client)</td><td>false</td><td>AppleSkin saturation icons on their own tooltip line</td></tr><tr><td><code>[widget] enabled</code>, <code>locked</code>, <code>x</code>, <code>y</code> (client)</td><td>true, true, 178, 0</td><td>The diet bar on the survival inventory and its position</td></tr></tbody></table><p>Settings Raspberry Pie cannot work with, in Apple Pie's file: <code>minContribution</code> above 0 (aged-out foods would still count a little), <code>resetOnDeath = true</code> (the memory is wiped before it can be aged out) and <code>startDecay</code> above <code>endDecay</code>. Apple Pie's own benefit lists are kept off, because the ladder replaces them. Keys: P opens the effect screen, Shift+T locks or unlocks the bar; both can be rebound.</p></div>

***

## <u><strong>Requirements</strong></u>

|                                   |Version                           |
| --------------------------------- |--------------------------------- |
| Minecraft                         |1.19.2                            |
| Forge                             |43 or newer                       |
| Spice of Life: Apple Pie Edition  |2.2.0 exactly, bundled in the jar |

Spice of Life: Carrot Edition 1.14.0 is bundled too and optional. Both come nested unmodified inside the Raspberry Pie jar; do not install loose copies unless you want to replace the bundled ones. Raspberry Pie does not need Creative Common or KubeJS.

Both client and server need it, and their versions must match.

***

## <u><strong>Source, issues and license</strong></u>

*   Source code and issue tracker: [github.com/SaintAlfonzo/CreativeEngineering](https://github.com/SaintAlfonzo/CreativeEngineering)
*   License: GNU GPL v3 for Raspberry Pie's own code. It is an add-on that mixes into Apple Pie at runtime and copies none of its code. The bundled Spice of Life: Apple Pie Edition by tarinoita (forked from Spice of Life: Potato Edition by Kevun1 and the Carrot Edition team) and Spice of Life: Carrot Edition by Cazsius, Talonos and juliand665 are unmodified and under LGPL v2.1; their source is attached to every release. The Liquid Diet Log texture is an unmodified copy of Apple Pie's Food Book texture. The full notices are in the jar.
*   Logo: the graph is adapted from the logo of Spice of Life: Apple Pie Edition by tarinoita (LGPL v2.1; pie removed, colors changed). The pie is the Raspberry Pie texture from [Hearth and Harvest](https://www.curseforge.com/minecraft/mc-mods/hearth-and-harvest) by AlabasterLeking (MIT License, copyright AlabasterLeking), with an edited crust.
*   Free to include in modpacks.
