# Wise Oak (Mystical Oak Tree): behaviour breakdown, replacement plan, web-call audit

Test prefix: `WO-`. TODO: `mobs/tweaks #19` (cooldown basis). Related: [time.md](time.md) (the pack's day length),
[enchant_power.md](enchant_power.md) (the Wise Oak gives 15 enchanting power, finding F1),
[creative_worldgen.md](creative_worldgen.md) (the bundle this lives in).

## 0. At a glance

| Part | Status |
|---|---|
| Behaviour of the stock mod (section 2) | DONE 2026-11-09, read from the bytecode of `mysticaloaktree-1.19.2-1.10.jar` |
| Web-call audit (section 3) | DONE 2026-11-09, every outbound call in the jar listed |
| New sleep and anger loop (section 4.1 to 4.3) | BUILT 2026-11-09, installed in `mods/`, not tested in game |
| Lightning growth (section 4.4) | defined from the stock code, no change made |
| Oak-biome-only generation (section 4.5) | BUILT 2026-11-09, not tested in game |
| Web block (section 4.6) | BUILT 2026-11-09, installed in `mods/`, not tested in game |
| High-trust gifts (section 4.7) | BUILT 2026-11-09, installed in `mods/`, not tested in game |
| Trust command (section 4.8) | BUILT 2026-11-09, installed in `mods/`, not tested in game |

- **Pending:** the test suite (section 8), and the scratch rows SQ-WO and CQ-WO.
- **Files to open:** section 9. The timers are in `config/creativeworldgen-common.toml` `[wise_oak]`. The mod's own config is `config/mysticaloaktree-common.toml` (`tree_rarity = 280`).
- **Home:** Creative Worldgen (owner decision 2026-11-09). It already has `creativeworldgen.mixins.json`, `CWGConfig` and a `data/` tree.
- **Licence:** the jar says "All rights reserved". Mixins that call into it are fine. Copying or editing its code or
  assets is not. This rules out forking the block or shipping an edited jar.

## 1. Overview

One block (`mysticaloaktree:wise_oak`) with a block entity. The block state `STATE` is the whole visible behaviour.
Timers and per-player trust live in the block entity, server side. The talking is client side: the server only decides
that a line should appear, the client picks and shows it.

## 2. Reference: the stock mod

### 2.1 States

| State | Meaning | Can blink | Can sleep |
|---|---|---|---|
| NONE | awake, idle | yes | yes |
| BLINKING | awake, eyes shut for 5 ticks | no | yes |
| SLEEPING | asleep | no | no |
| ANGRY, ANGRY_BLINKING | angry after blowing | yes (ANGRY) | no |
| BLOWING, THICC | blowing at a player | no | no |

THICC is the BLOWING variant with fat cheeks. Each blow has a 3 percent chance to be THICC. Both push the player the same way.

### 2.2 The tick loop

The block entity ticker runs every tick on both sides. On the server, every 23 game ticks it has a 1 in 21 chance to
call `randomTick` (about 24 seconds on average). `randomTick` does, in order:

1. ANGRY: 1 in 3 chance to calm to NONE, then stop.
2. `isDay = !level.isNight()`. This is Minecraft's sky-darkness test, so a thunderstorm counts as night.
3. If the state can sleep and (`isDay` or tired of the player): sleep.
4. If SLEEPING and not day: wake.
5. If it can blink: it blinks, then 5 ticks later a scheduled block tick ends the blink. The check is `nextFloat() < 1.0`, so it always passes (corrected 2026-11-09; this file first said 1 in 100).

So the stock tree sleeps **during the day** and wakes at night. The new loop is the reverse (section 4.1).

The ticker also keeps two client-only duties: running the current speech bubble, and turning to face a followed player.

### 2.3 Talking (right-click)

- Angry: the click does nothing (passes).
- Otherwise a per-player cooldown of 54 game ticks (about 2.7 seconds) gates a conversation. Sleeping trees skip the cooldown.
- Each conversation adds 5 trust (cap 100) and adds 1 to `dialoguesUntilSlept`.
- Awake: the client shows a `talked_to` line. With trust above 70 the tree also starts tracking the player for 60
  ticks; with trust above 40 it turns to face them.
- Asleep (**verified in the bytecode 2026-11-09**): the tree wakes, `dialoguesUntilSlept` resets, the player loses 20
  trust (floor 0), angry particles play, and the client shows a `woken_up` line. The block state it ends in is **NONE**,
  not ANGRY, so it only looks angry because of the particles. The new loop (4.1) turns this into a real ANGRY state.

### 2.4 Getting tired

Only while the state is NONE, each `randomTick` rolls 1 in N, where N = clamp(8 minus (talks minus 5), 1, 8). Up to 5
talks N is 8; the 6th talk gives 7; each further talk lowers N by 1; from the 12th talk N is 1 and the tree always sleeps.
The counter resets only when it wakes.

### 2.5 Trust (per player)

Stored per player UUID in the block entity (`relationship` list of `{id, trust}`), starting at 0.

| Rule | Value |
|---|---|
| Talk | +5 per conversation, only below 100 |
| Hit it, or wake it | minus 20, floor 0 |
| Trust above 40 | turns to face the talker |
| Trust above 70 | tracks the talker for 60 ticks |
| Trust below 0 ("angry at you") | never happens: the floor is 0 |
| Dialogue lines | each has `trust_required`; the client picks a random line the player qualifies for |

Silk touch keeps the whole trust table in the dropped item (the loot table copies the `relationship` NBT), so a moved
tree remembers everyone. Without silk touch it drops an oak log.

### 2.6 Hitting (left-click) and blowing

`onAttack` has no state check, so any hit (asleep, awake, angry) does all of this:
1. Angry particles, and the tree turns to face the attacker.
2. State becomes BLOWING (97 percent) or THICC (3 percent), a 34-tick counter starts, the attacker is tracked for 60 ticks.
   The client shows a `hurt` line.
3. Trust minus 20.

While BLOWING or THICC, each tick the attacker is pushed away from the tree if they are within 11 blocks and in line
of sight. The push falls off with distance, is reduced by Knockback Resistance, is horizontal only, and keeps their vertical
motion. Another hit during the blow restarts the 34 ticks. When the counter ends the state becomes **ANGRY**.

### 2.7 Everything else

- **Enchanting power:** `getEnchantPowerBonus` returns 15 (the cap). See [enchant_power.md](enchant_power.md) F1.
- **Breaking:** without silk touch it spawns up to 30 enchant particles around it. The client shows an `on_broken` line (dormant, see 2.8).
- **Particles:** idle enchant glyphs, angry particles, wind particles.
- **Piston:** it cannot be pushed (BLOCK reaction).
- **Lightning:** see 2.9.
- **Worldgen:** see 2.10.

### 2.8 Dialogue (client only)

Lines live in `assets/mysticaloaktree/tree_wisdom/`: 195 `talked_to`, 58 `hurt`, 33 `woken_up`, plus `countries.json`.
The code also has types for heard sounds, projectile hits, nearby entities and being broken, but the jar has no data
files for them, so they are dormant. Lines can use `$player_name`, `$ip`, `$random_pos`, `$random_date`, `$random_name`,
`$random_country` (placeholder counts: `$ip` 2 files, `$player_name` 4, `$random_pos` 3, `$random_date` 1, `$random_name` 2,
`$random_country` 3). On top of the files, a `talked_to` line can be replaced by web-sourced content (section 3).

### 2.9 Lightning growth (stock)

The mod listens through Moonlight for lightning strikes on a block. It takes the block **directly above the struck
block**. Lightning lands on the top solid block, and a sapling has no collision, so a bolt that hits the ground under an
**oak sapling** finds the sapling above it. In that case:
1. The sapling's position is cleared (replaced by the fluid state there, usually air; flag 4, no update).
2. The Wise Oak configured feature is placed at that position (`place`, so the tree needs room like any tree).
3. If the placement fails, the sapling is put back (flag 4). If it succeeded but that block is still the cleared state, the
   clients are notified (sendBlockUpdated).

It does not look at the biome, the rarity, the soil or the light, and it only matches the **oak** sapling. A struck sapling
that is not oak is ignored. The variant is picked by the biome (2.10). This is stock behaviour; nothing in the pack touches it.

### 2.10 Natural generation (stock)

- **Biome modifier** `mysticaloaktree:wise_oak`: `forge:add_features`, biomes `#minecraft:is_overworld`, feature `mysticaloaktree:wise_oak`,
  step `vegetal_decoration`.
- **Placement** (in code, not data): rarity filter 1 in `tree_rarity` (config, default 280, range 0 to 10000), random spot in the
  chunk, the tree-threshold and ocean-floor heightmap, the biome filter, and a block filter that needs soil an oak sapling
  survives on (a mangrove propagule in mangrove swamps). There is **no check that the biome grows oak trees**, so it appears
  anywhere with grass or dirt: taiga, snowy plains, desert edges and so on.
- **The tree** is picked by the biome: mangrove swamp gets a mangrove-rooted tree, swamp and jungle (the swamp-hut and jungle
  tags) get an oak with extra vines, everything else gets the plain oak. It is a big oak (7-high trunk base) with oak leaves and
  a custom trunk and foliage placer, plus a `WiseOakDecorator` that sets the Wise Oak block into the trunk. A flower
  feature is placed beside it afterwards.

## 3. Web-call audit (client and server, stock)

**Where it starts.** The mod's common init calls `TreeLoreManager.init()` without a side check, so the calls start **at
launch on both a client and a dedicated server**. Only one class touches the network: `client/TreeLoreManager`. `getRandomDialogue`
also calls the same start-up method, so the calls retry from the first conversation if they had not started. No other class in the
jar uses `java.net`. (Moonlight Library, the mod's dependency, is not covered by this audit.)

**What it does.** Four background tasks start on a cached thread pool, with no timeout set and failures swallowed:

| # | Task | Request | Sent | Used for |
|---|---|---|---|---|
| 1 | Wisdom quotes | GET `https://www.fantasynamegenerators.com/scripts/wisdomQuotes.js` | nothing | fills a quote list; a `talked_to` line becomes a random quote 7 percent of the time at trust 75 or more |
| 2 | Public IP | GET `http://checkip.amazonaws.com/` (plain http) | nothing | sets `$ip` (used by 2 dialogue files); fallback if it fails: a UDP "connect" to 8.8.8.8, which only reads the local network address and sends no packet |
| 3 | Location (follows 2) | GET `https://api.api-ninjas.com/v1/iplookup?address=<your public IP>` with an embedded API key header | **the player's public IP** to api-ninjas.com | gets the player's latitude and longitude |
| 4 | Weather (follows 3) | GET `http://api.weatherunlocked.com/api/forecast/<lat>,<lon>?app_id=...&app_key=...` (plain http, embedded credentials in the URL) | **the player's approximate latitude and longitude** | "tomorrow will be X" line, 4 percent of `talked_to` lines at trust 40 or more |
| 5 | Facts | GET `https://api.api-ninjas.com/v1/facts?limit=20` with the key | nothing | random-fact line, 5 percent at trust 100 or more; refetches whenever the 20 run out |
| 6 | Baby names | GET `https://api.api-ninjas.com/v1/babynames?limit=10` with the key | nothing | adds names to `$random_name` |

So **6 distinct HTTP requests at every launch** on every client, and again on a dedicated server. Two are plain http
and carry the API credentials or a public IP in the clear. The credentials are embedded in the jar and are not copied into this repo.

**What happens with the calls blocked (no network).** The mod's own fallbacks are safe:
- `$ip` stays `***` (so "Greetings ***").
- `$random_name` uses a built-in list (blorgle, splorgle, garvin, pepa, boris).
- Quotes, facts and weather lines are skipped, because each is null-checked or empty-checked before use.
- Countries come from the bundled `countries.json`, not the web.
- All 195 `talked_to`, 58 `hurt` and 33 `woken_up` lines still work.

**Worth knowing for the owner.** This leaks every player's public IP (to a third party) and rough location to a weather service,
at launch, with no opt-out, and the mod runs it on servers as well. Blocking it is a privacy fix as well as a tidy-up.

## 4. What the pack changes (planned)

### 4.1 The loop, per tree

| Loop state | Block state | Enters when | Leaves when |
|---|---|---|---|
| Awake | NONE / BLINKING | dawn, or a timed sleep ends | night starts; the tired roll succeeds |
| Night sleep | SLEEPING | night starts | the night ends and every sleep timer has expired |
| Tired sleep | SLEEPING | the tired roll succeeds | `sleepDuration` has passed, and it is not night |
| Angry | ANGRY | right-click on a sleeping tree, or the end of blowing | `angerDuration` has passed |
| Angry sleep | SLEEPING | angry time ends | `angrySleepDuration` has passed, and it is not night |

Rules:
- **Awake by day, asleep by night.** The reverse of the stock mod. Night is the stock `isNight()`, so **thunderstorms count
  as night** (owner decision 2026-11-09): a tree sleeps through a storm, and wakes when the storm ends if it is day.
- **Tired rolls unchanged:** same clamp-based 1 in N roll, same 23-tick and 1 in 21 cadence, same talk counter.
- **Waking resets** the talk counter and any anger, in every wake path.
- **Wake is "and", sleep is "or":** a tree sleeps if it is night or any timer is pending, and wakes only when it is day
  and all timers have expired. A timed sleep that ends mid-night stays asleep until dawn.
- **Right-click on a sleeping tree** mirrors the stock effects (owner decision 2026-11-09): trust minus 20 (floor 0),
  angry particles, the `woken_up` line, and the talk counter resets. The block state is now **ANGRY**, held for `angerDuration`,
  then SLEEPING for `angrySleepDuration`. The stock mod ends this click in NONE, so the angry state is the one addition.
- **Angry trees ignore right-clicks** (stock, kept).
- **Hitting** keeps the stock blow (BLOWING or THICC for 34 ticks, the push, trust minus 20) and ends in ANGRY for
  `angerDuration` instead of the 1 in 3 calm roll. A hit on a sleeping tree starts the blow as in stock.
- **Blinking is untouched** (every `randomTick` that reaches it, 5 ticks, ANGRY blinks as ANGRY_BLINKING).
- **Trust, blowing, THICC, dialogues, enchant power, loot and lightning growth are untouched.**

### 4.2 Clock

Timers store the absolute **game time** (`Level#getGameTime`) at which they expire. Game time advances one per real tick and is
not changed by `/time set`, by sleeping in a bed, or by Better Days' faster or slower sun ([time.md](time.md)), so "five minutes"
is five real minutes. Timers are saved in the block entity; a tree in an unloaded chunk wakes the next time it loads and ticks.
Night and day for the awake loop follow the sky, so they follow the pack's day length.

### 4.3 Config (Creative Worldgen's config, `CWGConfig`, table `[wise_oak]`)

| Key | Default | Meaning |
|---|---|---|
| `sleepDurationSeconds` | 300 | tired sleep length |
| `angerDurationSeconds` | 30 | how long it stays angry |
| `angrySleepDurationSeconds` | 600 | sleep after the anger |

Seconds are converted to game ticks (x20). The owner confirmed these defaults on 2026-11-09.

### 4.4 Lightning growth

No change. It is documented in 2.9 so that testing has a known target. It does **not** follow the oak-biome rule (owner decision
2026-11-09): a struck oak sapling can grow a Wise Oak in any biome, because the lightning path never checks the biome.

### 4.5 Generation only in biomes with oak trees

- **Rule:** a biome qualifies if its tree features (vanilla and mod biomes alike) include a tree with an **oak log trunk**.
  That covers plain, fancy and swamp oaks and mod trees built on the oak log.
- **Generator** `tools/gen_wise_oak_biomes.py` (listed in `tools/generators.json`, run with `/cdevgen`). It reuses the
  biome-feature walker the bee-nest generator already uses, so modded biomes are included. It writes
  `kubejs/data/creativeworldgen/tags/worldgen/biome/wise_oak_biomes.json` and prints the included and excluded list for the owner to
  read before it ships. A small allow and deny list in the generator handles exceptions.
- **Biome modifier (override, owner decision):** replace the jar's `biomes` entry `#minecraft:is_overworld` with `#creativeworldgen:wise_oak_biomes`. This is a datapack
  file in the pack folder at the jar's path (`kubejs/data/mysticaloaktree/forge/biome_modifier/wise_oak.json`) with our own content. It and the tag stay in `kubejs/data`, which outranks mod jars; a copy in our jar would not reliably win over the mod's. The
  default is safe: a new modded biome is excluded until the generator runs.
- **What changes in play:** the jar keeps placing the trees as in 2.10, in the chosen biomes only. The mangrove variant is
  unused (mangrove swamps have no oak trees). The swamp and jungle vine variants apply only to the biomes the generator keeps.
- **Existing worlds:** only newly generated chunks change. Existing trees stay.
- **Lightning:** not gated by the tag (4.4).
- **Result (generated 2026-11-09):** 42 included, 109 excluded of the 151 overworld biomes. `trees_meadow`, `trees_badlands`, `trees_savanna`, `trees_jungle` and `trees_water` are vanilla Java features and are modelled in the generator from vanilla.
- **Denied (owner decision 2026-11-09):** 16 biomes that pass the rule only on a technicality, held in `DENY` in the generator: 9 ocean biomes (island oaks through `trees_water`), `river`, `frozen_river` and Terralith `warm_river`, and four cave biomes (`dripstone_caves`, Terralith `desert_caves`, `frostfire_caves`, `mantle_caves`).

### 4.6 Blocking the web calls

- **Mixin `TreeLoreManagerMixin`** (Creative Worldgen, target `client/TreeLoreManager`). It cancels at head:
  - `initIfNeeded()`: nothing is queued, so tasks 1 to 6 never start (this also covers the retry from `getRandomDialogue`);
  - `readFromURL(String)` and `readFromURL(String, boolean)`: return an empty string, so even a call from elsewhere cannot open a connection.
  - The mixin applies only when the mod is loaded (`WorldgenMixinPlugin`), its injectors are `require = 0`, it applies on both sides (the call also happens on a dedicated server), and it logs one line
    "Wise Oak web lookups disabled".
- **No assets or code copied**: the mixin only cancels three methods.
- **What stays:** everything in 3 under "with the calls blocked".
- **Scope:** this mod only. Moonlight is not covered.

### 4.7 High-trust gifts

After a conversation with an awake tree (a right-click that passes the talk cooldown), a player at or above the trust threshold has a chance of a drop from the tree's face. Clicks on a sleeping, angry or blowing tree never count. The trust read already includes that conversation's +5.

One of the four lists is picked evenly, then one item in it. Config: `[wise_oak.gifts]` in `config/creativeworldgen-common.toml`.

| Key | Default | Meaning |
|---|---|---|
| `chancePercent` | 5.0 | chance per conversation; 0 turns gifts off |
| `trustThreshold` | 75 | trust needed, the same tier as the stock "tracks the talker" rule |
| `saplings`, `saplingCount` | 17 ids, 1 | saplings of Overworld trees, including Quark's ancient sapling |
| `fruitSaplings`, `fruitSaplingCount` | 14 ids, 1 | fruit tree saplings |
| `fruit`, `fruitCount` | 14 ids, 4 | fruit of the fruit trees |
| `leaves`, `leavesCount` | 30 ids, 16 | leaves of the trees in the two sapling lists, except ancient |

- An empty list is left out of the draw. An unknown id is skipped with one log warning, so a list can name a mod that is not installed.
- Quark's six blossom trees count as Overworld trees (Quark generates them in Java, so the feature walker does not list them).
- Left out on purpose (owner review 2026-11-09, checked against item_merges.md 2.11, the Wandering Trader and Winemaker lists, crops.md, and an id scan of every mod):
  - Upgrade Aquatic's river (Mulberry) sapling and leaves: owner cut.
  - Fruits Delight's apple sapling and leaves: the pack's only fruit tree overlap, merged into Vinery's.
  - Lemon and pineapple: crops.md has them as a bush and a plant, not among the 13 fruit trees.
  - Twisted and Magic Peanut saplings: not Overworld trees.
- `quark:ancient_sapling` is in the sapling list at the same odds as the others, and its leaves stay off the leaves list (owner 2026-11-09).

### 4.8 Trust command

`/wiseoak trust <pos> <player>` reads a player's trust on the tree at those coordinates; `/wiseoak trust <pos> <player> <value>` sets it (0 to 100). Op level 2. `<player>` is a username (online, or known to the server's profile cache) or a selector, and the result is returned for `/execute store`. Setting replaces the player's record, which also clears their talk cooldown, and sends a block update so a client sees the new trust. It uses the source's dimension; use `/execute in` for another.

## 5. Limits and decisions

Decided 2026-11-09: lives in Creative Worldgen; thunderstorms are night; right-click on a sleeper mirrors the stock effects (trust,
particles, line) and ends in ANGRY; only oak-tree biomes for natural generation; lightning growth ignores the oak-biome rule; the
biome modifier file is overridden (not a second modifier); all web calls blocked.

Out of implementation (owner, 2026-11-09):
- **Existing trees.** No migration or special handling. Missing timers load as 0 (expired), which falls out of the default.
- **Insults when another oak dies** (TODO `mobs/tweaks #19`, second half). Needs the dormant dialogue types plus new lines. Stays open in the TODO.

Nothing else is open.
- **Rejected:** replace the block with our own (licence). KubeJS only (the sleep logic is private Java). A per-world opt-out for the web
  calls (nobody wants them).

## 6. Where it lives and how (as built)

- Creative Worldgen, `development/creativeWorldgen`: package `mixin/wiseoak`, `CWGConfig.WiseOak`, two rows in `creativeworldgen.mixins.json`, two rows in `WorldgenMixinPlugin` (gated on the mod).
- `WiseOakTileMixin`:
  - cancels `randomTick` at head and runs the new loop (4.1), calling the stock `isTiredOfYou`, `goToSleep` and `wakeUp`;
  - injects before the ticker's `getGameTime` call, which only the server reaches and only with no blow running, and checks the timers every 23 game ticks. A timer change cancels the rest of that tick;
  - clears both timers at the head of `wakeUp`, sets ANGRY after the stock wake in `onInteract`, starts the anger timer at the tail of `stopBlowing`;
  - `@Unique` long fields `creativeworldgen$sleepUntil` and `creativeworldgen$angryUntil`, saved as NBT `creativeworldgen_sleep_until` and `creativeworldgen_angry_until` (only when set; missing loads as 0).
- `TreeLoreManagerMixin` (4.6).
- A dawn wake stays on the `randomTick` cadence, so trees wake staggered as in stock; a timed sleep that ends by day wakes at the next 23-tick check.
- Mixins target SRG names with `remap = false`. No code is copied from the jar. The stock jar is in `development/creativeCommon/libs/` (git-ignored) for compiling.

## 7. Offline checks

- Generator output: done 2026-11-09, see 4.5.
- Built jar: every vanilla call in the mixin is remapped to SRG (checked with `javap`, 2026-11-09).
- Mixin applies: a dedicated server boot, SQ-WO-01 in the scratch queue, not run yet.

## 8. In-game test suite (all open)

- [ ] **WO-01 awake by day:** place a Wise Oak in the day, wait 2 minutes -> it blinks and never sleeps.
- [ ] **WO-02 asleep by night:** `/time set night`, wait about a minute -> SLEEPING; `/time set day` -> wakes within about a minute.
- [ ] **WO-02b storm:** `/weather thunder` in the day -> it sleeps; `/weather clear` -> it wakes.
- [ ] **WO-03 tired roll:** talk to it 12 times, 3 seconds apart -> it sleeps by the 12th talk at the latest.
- [ ] **WO-04 timed sleep:** after WO-03 in daytime it stays asleep for `sleepDurationSeconds`, then wakes; the talk counter is 0.
- [ ] **WO-05 right-click on sleeper:** at night, right-click -> ANGRY for `angerDurationSeconds`, then SLEEPING for `angrySleepDurationSeconds`; trust minus 20, angry particles, `woken_up` line.
- [ ] **WO-06 angry ignores clicks:** right-click while ANGRY -> nothing.
- [ ] **WO-07 punch:** left-click -> BLOWING or THICC for 34 ticks and a push, then ANGRY for `angerDurationSeconds`.
- [ ] **WO-08 mid-night wake:** a tired sleep that ends at night -> stays asleep until dawn.
- [ ] **WO-09 persistence:** save and quit during a timed sleep; the remaining time is kept.
- [ ] **WO-11 time skip:** a bed or `/time add` does not shorten or extend a timed sleep.
- [ ] **WO-12 unchanged:** blinking, trust-based turning, enchant power 15, silk touch keeping trust.
- [ ] **WO-13 lightning (any biome):** `/summon lightning_bolt` at an oak sapling on grass with room -> a Wise Oak grows; with no room -> the sapling comes back; a birch sapling -> nothing; also works in a biome outside the oak tag.
- [ ] **WO-14 oak biomes only:** in a fresh world, no Wise Oak appears in a biome outside the tag (taiga, desert, snowy plains); find one in plains or forest with `/cdevplace` or by exploring.
- [ ] **WO-16 gifts:** with `chancePercent = 100` and `trustThreshold = 0`, talk to an awake tree -> a drop from one of the four lists, in the right count (1, 1, 4, 16); no drop when it is sleeping, angry or blowing.
- [ ] **WO-17 gift config:** a trust below `trustThreshold` gives nothing; `chancePercent = 0` gives nothing; an unknown id logs one warning and never drops.
- [ ] **WO-18 trust command:** `/wiseoak trust` reads and sets a username's trust on the tree at given coordinates, also through a selector; values outside 0 to 100, an unknown player, an unloaded position and a non-tree are refused.
- [ ] **WO-15 no web:** start a client and a dedicated server with `strace -f -e trace=connect` (or the network cut): no connection from this mod; the log has the one "web lookups disabled" line; talk to a tree at high trust -> lines still show, `$ip` lines read `***`.

## 9. File map

- Stock jar: `mods/mysticaloaktree-1.19.2-1.10.jar`; config `config/mysticaloaktree-common.toml`.
- Pack mentions: `mod_sides.md`, `pack/MODLIST.md`, `feature_writeup/enchant_power.md`, `feature_writeup/todo/TODO.md` (`mobs/tweaks #19`),
  `kubejs/generation_scripts/wthit/silk_touch_blocks.json`, `kubejs/data/creativeworldgen/kubejs/loot/loot_census.md`.
- Ours: `development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/mixin/wiseoak/`, `CWGConfig.java` (`WiseOak`), `development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/wiseoak/WiseOakGifts.java` (and `WiseOakCommand.java`, `WiseOakTrustAccess.java` beside it), `tools/gen_wise_oak_biomes.py`, `tools/scratch_query/wo_probe.js`, `kubejs/data/creativeworldgen/tags/worldgen/biome/wise_oak_biomes.json`, `kubejs/data/mysticaloaktree/forge/biome_modifier/wise_oak.json`.

## 10. History

- 2026-11-09: stock behaviour decompiled; plan written. Owner decisions added: Creative Worldgen, storms are night, mirror the
  right-click effects, oak-biome-only generation, web calls blocked, lightning growth defined, full web audit.
- 2026-11-09: built: sleep and anger loop, config, oak-biome tag and modifier, web block. Creative Worldgen installed. Corrected the stock blink chance (always, not 1 in 100).
- 2026-11-09: owner confirmed the timer defaults and denied 16 technicality biomes. High-trust gifts built.
- 2026-11-09: gift lists reviewed: river sapling and leaves and lemon cut, ancient sapling added.
- 2026-11-09: `/wiseoak trust` added. Scratch section SQ-WO expanded to 37 server and 7 client rows.
