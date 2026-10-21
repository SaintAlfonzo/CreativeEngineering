# mobs / tweaks: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# mobs`, Sub `tweaks`. Feature writeup:
[mob_block_tweaks.md](../mob_block_tweaks.md) (test prefix `MT-`, used up to MT-25; the leaf decay tests MT-13 to
MT-25 belong to `farming/trees #1`). New tests below start at MT-26. Check the next free number before you add them.

Jars checked (decompiled with vineflower, under `scratchpad/decomp/`): `alexsmobs-1.21.1.jar`,
`solapplepie-1.19.2-2.2.0.jar`, `neapolitan-1.19.2-4.1.0.jar`, `nock_enough_arrows-1.19.2-1.2.0.jar`,
`Quark-3.4-418.jar` (+ `AutoRegLib-1.8.2-55.jar`), `minecraft-comes-alive-7.5.20+1.19.2-universal.jar`,
`notenoughanimations-forge-1.12.2-mc1.19.2.jar`. A fact is marked "verified" when it was read from one of them.

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #7 | Seagulls can't take lunchboxes | Planned | tiny: config (2 files); optional patcherMod mixin for dropped boxes |
| #4 | Entity dictionary | Planned | small: KubeJS, 1 new startup file + 2 edited |
| #9 | Nock Enough Arrows golem (the Sputter) | Planned | look done here; then config line or docs |
| #8 | Merge monkeys | Planned | small once decided: config / data, + JEI hide |
| #6 | Named mobs | Planned | medium: KubeJS (startup + server), optional patcherMod |
| #5 | Quark monster boxes | Planned | the question is answered here; vault rework is medium-large patcherMod |
| #2 | MCA villagers glitch while sleeping | Bug | unknown cause: in-game diagnosis first, then a client mixin |

Build order:

1. **#7 config now.** Two config lines, no code, no decision. Ships with the next deploy.
2. **#4 before #6 and #8.** Both add entity ids to scripts. Make the dictionary first so they use keys from the start.
3. **#9 and #8 are decisions.** This brief has the facts for both. Once decided, each is a config line plus a few
   script lines.
4. **#6 KubeJS part.** Startup + server scripts. Its optional despawn / karma hooks wait for `largecontent/karma #1`.
5. **patcherMod batch: #5 vault rework, #2 fix, optional #7 mixin.** One patcherMod build and one restart. #2 needs its
   in-game diagnosis (#2 Tests, MT-38) before any code.

Related rows: `largecontent/karma #1` (karma system, docs only so far; #6 adds the hook), `food/drinks #3` (banana
merge, which says "Alex's Mobs' banana goes"; it touches #8, see there), `mobs/spawns #2` (farm animals by biome; #8's
spawn change goes in the same `mob_spawns.json`), `worldgen/structures/contents #3` (remove pillager spawners; same
area as #5), `gear/archery` ([archery.md](../archery.md); #9's Sputter shoots NEA arrows), `tooling/dictionaries #1`/`#2`
(dictionary validation; #4 copies the block_dict pattern).

---

## tweaks #7: Seagulls can't take lunchboxes
Status Planned / Work KubeJS / patcherMod (really config) / TODO `mobs/tweaks #7`

### Goal
Alex's Mobs seagulls stop stealing Spice of Life lunchboxes from the hotbar. Today they also eat them, contents and
all.

### Now
- **Why a lunchbox is a target (verified).** `com.github.alexthe666.alexsmobs.entity.ai.SeagullAIStealFromPlayers`
  scans hotbar slots 0-8. `hasFoods` / `getFoodItemFrom` take any stack where `ItemStack#isEdible()` (`m_41614_`) is
  true and `isBlacklisted` is false. `isBlacklisted` compares the item id with each string in
  `AMConfig.seagullStealingBlacklist`. Spice of Life: Apple Pie's `com.tarinoita.solsweetpotato.item.foodcontainer.FoodContainerItem`
  overrides `isEdible()` (`m_41472_`) to `return true`, with an empty `FoodProperties`. All three containers count:
  `solapplepie:lunchbox` (9 slots), `solapplepie:lunchbag` (5) and `solapplepie:golden_lunchbox` (14), all stack size 1.
- **What happens to it (verified).** The gull copies the stack (NBT, so the contents too), shrinks the player's stack
  to 0, and holds the copy. `EntitySeagull#tick` eats any held item that passes `canTargetItem` (`getItem().isEdible()`,
  so true for a box) after 200 ticks: `shrink(1)`. The box and everything in it are gone 10 s after the theft unless
  the gull is killed first. Killing it drops the held stack (`EntitySeagull` 279-281, `dropEquipment`).
- **Config today** ([config/alexsmobs.toml](../../config/alexsmobs.toml) 94-97, identical copy in
  [configs_edited/root_config/alexsmobs.toml](../../configs_edited/root_config/alexsmobs.toml)):
  `seagullStealing = true`, `seagullStealingBlacklist = []`.
- Dict keys exist: `i.lunchbox`, `i.lunchbag`, `i.golden_lunchbox`
  ([item_dict.js](../../kubejs/startup_scripts/_lib/item_dict.js) 12489-12491).
- **Ground pickup isn't covered by the blacklist (verified).** `EntitySeagull$AITargetItems` (a
  `CreatureAITargetItems`) picks up dropped items that pass `canTargetItem`, which is `isEdible()` again. A dropped
  lunchbox gets picked up and eaten. A class scan of Alex's Mobs shows 11 more entities that use `isEdible` for item
  targeting or eating: blue jay, cockroach, crocodile, crow, kangaroo, mimicube, raccoon, rattlesnake, snow leopard,
  tasmanian devil, tiger. Per-entity behaviour not read.

### Build
1. **Config (the row's fix).** In both copies of `alexsmobs.toml`, edit with the game stopped (Forge rewrites a
   running config):
   ```toml
   	seagullStealingBlacklist = ["solapplepie:lunchbox", "solapplepie:lunchbag", "solapplepie:golden_lunchbox"]
   ```
   A config file can't read `global.item`. Note the three ids in the writeup instead.
2. **Optional, for dropped boxes (patcherMod).** Add an item tag `creativeengineering:not_mob_food` through
   `global.tags` in a server tag script (e.g. `kubejs/server_scripts/tags/itemtags.js`, or the file that holds other
   behaviour tags) with `i.lunchbox`, `i.lunchbag`, `i.golden_lunchbox`. Then a mixin:
   ```java
   @Mixin(value = EntitySeagull.class, remap = false)
   public class SeagullFoodTagMixin {
       @Inject(method = "canTargetItem", at = @At("HEAD"), cancellable = true, remap = false)
       private void ce$notMobFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
           if (stack.is(CETags.NOT_MOB_FOOD)) cir.setReturnValue(false);
       }
   }
   ```
   `canTargetItem(ItemStack)` is Alex's Mobs' own public method, so it needs no refmap. The same tag check could also
   go at the head of `SeagullAIStealFromPlayers#isBlacklisted` (private, `remap = false`), so one tag covers both
   paths and the config line becomes a backup. The other 11 mobs each have their own `canTargetItem`. Add them only if
   one is seen taking a box (each is a one-line mixin on the same pattern).
   Put `alexsmobs-1.21.1.jar` in `development/patcherMod/libs/` (compileOnly `fileTree`, build.gradle 17).

### Ids and data
- `solapplepie:lunchbox`, `solapplepie:lunchbag`, `solapplepie:golden_lunchbox` (verified, `SOLSweetPotatoItems`).
- Config keys `seagullStealing`, `seagullStealingBlacklist` (verified, `AMConfig` use; present in the toml).
- Mixin target `com.github.alexthe666.alexsmobs.entity.EntitySeagull#canTargetItem(Lnet/minecraft/world/item/ItemStack;)Z`
  (verified).

### Decisions
- **Config only, or config + mixin?** Recommend config now, and the mixin only with the next patcherMod build. The
  hotbar theft is the reported problem. A dropped box near gulls is rarer.
- **Turn stealing off altogether (`seagullStealing = false`)?** No. It's the seagull's flavour, and the row only asks
  to protect lunchboxes.

### Gotchas
- `FoodContainerItem#isEdible()` must stay true, because SoL eats from the box through the use-item path. So don't
  "fix" it on the item side.
- The blacklist matches exact ids. A new container item (a future SoL version) needs a new line, and the tag route
  avoids that.

### Tests
- **MT-26 Lunchbox safe in the hotbar:** stand on a beach with a lunchbox in hotbar slot 1 and bread in slot 2, and
  wait for a gull → it takes the bread, never the box. With only the box in the hotbar, no gull dives at you.
- **MT-27 Dropped box (only with the mixin):** drop a lunchbox near gulls → none picks it up.

---

## tweaks #4: Entity dictionary
Status Planned / Work KubeJS / TODO `mobs/tweaks #4` ([mob_block_tweaks.md](../mob_block_tweaks.md) F1)

### Goal
Give entity ids a home in `_lib/` like items, blocks, fluids and effects, and switch the scripts that name entity
types over to its keys.

### Now
- Entity ids written as literals in scripts:
  - [server_scripts/tags/entitytags.js](../../kubejs/server_scripts/tags/entitytags.js) 7-12: 11 ids in
    `creativeengineering:bubble_column_immune`, already through `global.tags.add`.
  - [startup_scripts/func/sample_syringe.js](../../kubejs/startup_scripts/func/sample_syringe.js) 17-24: the `SAMPLES`
    table is keyed by 8 entity ids (`minecraft:cow`, `more_mooblooms:moobloom`, pig, chicken, sheep, rabbit, cod,
    salmon). Its values are item ids (`creativeengineering:*_sample_syringe`) that aren't in `item_dict` either.
  - Dev dumps (`server_scripts/_lib/villager_trades_dump.js` 113 `vinery:wandering_winemaker`) can stay as they are.
  - JSON data (`generation_scripts/worldgen/mob_spawns.json`, `structure_config.json`) holds entity ids as data. JSON
    can't use the dict, so leave it.
- The dictionary pattern to copy: [block_dict.js](../../kubejs/startup_scripts/_lib/block_dict.js) (`// priority: 1000`,
  `global.blocks = {}`, `var b = global.blocks`, a `StartupEvents.postInit` check at 86-117 against
  `ForgeRegistries.BLOCKS` that logs `[block_dict] Missing/invalid block IDs`).
  [effect_dict.js](../../kubejs/startup_scripts/_lib/effect_dict.js) is the smallest example (`global.effect`, `ef.`).
- Rows #6, #7 (tag route), #8, `mobs/spawns` and `mobs/pillagers` (KubeJS spawn-with-gear for End pillagers,
  `mobs/pillagers #2`) will add more entity ids.

### Build
1. New `kubejs/startup_scripts/_lib/entity_dict.js`:
   ```js
   // priority: 1000
   // Entity type ids (global.entity), same role as item_dict.js for items: scripts reference
   // global.entity.<key> instead of repeating 'namespace:id' strings. Tags: 't_' prefix, '#' value.
   global.entity = {};
   var en = global.entity;

   // ############################## Vanilla ##############################
   en.squid = 'minecraft:squid';
   en.glow_squid = 'minecraft:glow_squid';
   en.dolphin = 'minecraft:dolphin';
   en.turtle = 'minecraft:turtle';
   en.cow = 'minecraft:cow'; // ... pig, chicken, sheep, rabbit, cod, salmon
   // ############################## Alex's Mobs ##############################
   en.orca = 'alexsmobs:orca'; // ... cachalot_whale, seal, giant_squid, giant_squid_part, hammerhead_shark, frilled_shark
   // ############################## More Mooblooms ##############################
   en.moobloom = 'more_mooblooms:moobloom';
   ```
   Key style follows item_dict: the bare path, plus a mod prefix only on a clash (item_dict's `i.neapolitan_banana`).
   Known clash: `untitledduckmod:goose` vs `upgrade_aquatic:goose`. Recommend `en.goose` = the duck mod's (it's the
   one spawning, [mob_spawns.md](../mob_spawns.md) 9) and `en.upgrade_aquatic_goose`. Tags: `en.t_<path>` = `'#ns:path'`,
   as `i.t_fruits_banana`.
2. Validation at the end of the file, copied from block_dict 86-117 with `ForgeRegistries.ENTITY_TYPES`, logging
   `[entity_dict] Missing/invalid entity IDs`. Skip `#` keys (the tag problem is `tooling/dictionaries #1`/`#2`).
3. `entitytags.js`: `var en = global.entity` and pass `[en.squid, en.glow_squid, ...]`.
4. `sample_syringe.js` (startup, so the dict loads first thanks to priority 1000): `SAMPLES[global.entity.cow] = ...`.
   While there, the filled-syringe item ids could move to `item_dict` (they're registered by
   `startup_scripts/items/sample_syringe.js`). That's optional, one step further than this row.
5. Add a line to [pack_tooling.md](../pack_tooling.md) section 2's dictionary table (row for `entity_dict.js`) and
   close F1 in mob_block_tweaks.md.

### Ids and data
Every id above is already used live today, so the validation at the end of the file should log nothing. Run it once
to confirm.

### Decisions
- **Make it now or "if more entity tags come"** (the TODO's wording)? Recommend now. #6, #7 and #8 each add entity
  ids, and CLAUDE.md asks for one definition per id.
- **Fill it with every entity in the pack (like item_dict's 12k keys) or only what scripts use?** Recommend only what's
  used, plus each new row's ids. A generated full list would need a `/dump` and adds nothing the scripts read.

### Gotchas
- Server scripts share one top-level scope: use `var en = global.entity` inside a function or IIFE if `en` clashes,
  as `entitytags.js` does with `var t`.
- `global.entity` is a startup global. Client scripts see it too (same `global`), which is fine.

### Tests
- **MT-28 Dict check:** restart, then `logs/latest.log` has no `[entity_dict] Missing/invalid` line, and MT-01 still
  passes (the tag is unchanged; `/tag` can't list entity tags, so use `/dumptags entity_type` and check
  `kubejs/exported/tags/`).

---

## tweaks #9: Nock Enough Arrows golem (the Sputter)
Status Planned / Work none / TODO `mobs/tweaks #9`

### Goal
Find out what NEA's golem is and whether it's used, then keep, integrate or remove it.

### Now (the look, verified against `nock_enough_arrows-1.19.2-1.2.0.jar`)
- **What it is.** `nock_enough_arrows:sputter` (`com.itayfeder.nock_enough_arrows.sputter.Sputter`, an
  `AbstractGolem` + `RangedAttackMob`). It has 40 HP, speed 0.2, and knockback resistance 1.0.
  - It's an arrow turret: `RangedAttackGoal(1.25, 20 ticks, 10 blocks)`. It targets any `Mob` that is an `Enemy`
    within 10 blocks, plus whoever hurts it.
  - It only shoots arrows you give it. Right-click with an `#minecraft:arrows` stack and it takes the whole stack.
    Each shot uses one arrow. The shot uses the arrow item's own entity, so NEA's special arrows and tipped arrows
    work, and its own potion effects get added to plain arrows. Shot arrows are `CREATIVE_ONLY` pickup, so they're
    not recoverable.
  - Right-click with an empty hand to take the arrows back. A dye sets its war-paint colour.
- **How it's made** (`events/GolemCreatureEvents`, on `BlockEvent.EntityPlaceEvent`): a carved pumpkin or jack
  o'lantern on top of any block in `#forge:barrels`. Both blocks are set to air (flag 2) and the Sputter appears.
  The advancement `nock_enough_arrows:fletching/build_sputter` ("Sput it out!") fires.
- **Switch.** [config/nock_enough_arrows-common.toml](../../config/nock_enough_arrows-common.toml) 2-4:
  `[sputter] enableSputter = true`.
- **Use in the pack.** Nothing references it: no quest, no writeup, no script, no JEI entry (a repo-wide grep for
  `sputter` only finds the config). Whether players have built one can only be known in game (MT-29).

### Build
Depends on the decision:
- **Keep and integrate (recommended):** document it in [archery.md](../archery.md) (a short "Sputter" section: build,
  arrows, targets), and add a JEI information page on the carved pumpkin and on a barrel through
  `JEIEvents.information` in a client script ([jeichanges.js](../../kubejs/client_scripts/jeichanges.js) is the JEI
  file; the pack has no information pages yet, so this is the first). Optionally add an FTB Quests task in the
  archery part of [progression.snbt](../../config/ftbquests/quests/chapters/progression.snbt) (~3095, where the NEA
  arrows are).
- **Remove:** `enableSputter = false` in the toml (and its `configs_edited/root_config/` copy if one is added). Sputters
  that already exist stay. Kill them with `/kill @e[type=nock_enough_arrows:sputter]` if wanted. The advancement stays
  but can't be earned.

### Decisions
- **Keep or remove?** Recommend keep: it's a cheap, arrow-fed base turret that fits the pack's archery and fletching
  work, and it costs nothing to leave on. Remove it if MT-29 shows the accidental build below happens in play.

### Gotchas
- **Accidental builds.** Any carved pumpkin or jack o'lantern placed on any barrel makes a Sputter. That's a common
  decoration (Halloween builds, storage rooms). The barrel is set to air and its contents spill: `BarrelBlock#onRemove`
  drops them on a server-side `setBlock`. Which barrels are in `#forge:barrels` isn't known offline (Quark or Every
  Compat variant barrels may be); check with `/dumptags block`. If this is a problem, a patcherMod mixin could narrow
  `PLANKS_PREDICATE` to vanilla `minecraft:barrel` or require sneaking. Not recommended unless it shows up.

### Tests
- **MT-29 Sputter:** place a carved pumpkin on a barrel → a Sputter appears and the advancement fires; give it 16
  arrows and lure a zombie in → it shoots, the count goes down; with an empty hand take the rest back. With
  `enableSputter = false` (game stopped) → nothing happens.

---

## tweaks #8: Merge monkeys
Status Planned / Work data / KubeJS / TODO `mobs/tweaks #8`

### Goal
One jungle monkey across Alex's Mobs and Neapolitan instead of two overlapping ones.

### Now
| | Alex's Mobs capuchin (`alexsmobs:capuchin_monkey`) | Neapolitan chimpanzee (`neapolitan:chimpanzee`) | Alex's Mobs gelada (`alexsmobs:gelada_monkey`) |
|---|---|---|---|
| Where | biome spawn, weight 28, groups 9-16, 9 jungle-type biomes ([spawns_by_biome.md](../spawns_by_biome.md) 2606) | no biome spawn. Spawns with Neapolitan banana plants that grow a Banana Bundle: 25 % chance, 12-24 attempts, up to 10 ([neapolitan-common.toml](../../config/neapolitan-common.toml) 28-36; [crops.md](../crops.md) 420). The pack places banana plants in the 4 dense jungles (common) and other warm wet forests (uncommon), `wild_crops.json` 348-361 | biome spawn, weight 5, 9-16, 7 highland / meadow / shrubland biomes |
| Config | [alexsmobs.toml](../../config/alexsmobs.toml) 230 `capuchinMonkeySpawnWeight = 28` | `Chimpanzee group chance` etc. | 506 `geladaMonkeySpawnWeight = 5` |
| Play | tameable: feed `#alexsmobs:bananas`, 1 in 5 tames it. Sits on your shoulder, attacks your target, throws an Ancient Dart. Heals on `#alexsmobs:insect_items` | not tameable. Ambient group behaviour: grooming, flips, crying, jumps on bouncy blocks, opens banana bunches, scared by explosions. A Bananarrow sends chimps after the mob it hits. Drops/head: `neapolitan:chimpanzee_head`, banner pattern | not tameable (herd animal that clears grass) |
| Foods (verified tags) | `#alexsmobs:bananas` = `#forge:crops/banana` + `#forge:fruits/banana`. Neapolitan's `forge:fruits/banana` holds `neapolitan:banana`, so capuchins tame on Neapolitan bananas too | `#neapolitan:chimpanzee_food` = banana bunch; `#neapolitan:chimpanzee_snacks` = Neapolitan banana, bunch, bread, dried banana, bananarrow, potion | |

The gelada lives in a different habitat and is a different animal (a grass-eating baboon relative). It isn't part of
the overlap. The overlap is capuchin vs chimpanzee: same jungles, same bananas.

### Build (either way)
- **Capuchin goes:** in both copies of `alexsmobs.toml` set `capuchinMonkeySpawnWeight = 0` (Alex's Mobs adds its
  spawns from these weights; 0 = disabled, per the toml comment). Or add a `remove` rule in
  [mob_spawns.json](../../kubejs/generation_scripts/worldgen/mob_spawns.json) (`{"id": "no_capuchin", "mob":
  "alexsmobs:capuchin_monkey", "action": "remove", "add": ["#minecraft:is_overworld"]}`; a rule with no selector is
  skipped, `mob_spawns.js` 189). The config is simpler and also covers the Skylands. Hide `i.spawn_egg_capuchin_monkey`
  ([item_dict.js](../../kubejs/startup_scripts/_lib/item_dict.js) 10209) by adding it to `global.removedItems`
  ([removed_items.js](../../kubejs/startup_scripts/_lib/removed_items.js)). The Ancient Dart (`alexsmobs:ancient_dart`)
  then has no use. Hide it too. Raise the chimp group chance to make up the numbers, e.g. `Chimpanzee group chance`
  0.25 → 0.5.
- **Chimpanzee goes:** `neapolitan-common.toml` `"Chimpanzee group chance" = 0.0`. Hide `neapolitan:chimpanzee_spawn_egg`,
  `neapolitan:bananarrow`, `neapolitan:chimpanzee_head` and `neapolitan:chimpanzee_banner_pattern` (add dict keys
  first: `i.chimpanzee_spawn_egg` etc.; none are in item_dict today). Remove the bananarrow recipe through `NCH`
  (`R.remove({ output: i.bananarrow }, e)`).
- **Existing mobs.** Spawned monkeys of the losing type stay in the world. To clear them, use a server-script
  `EntityEvents.spawned(<loser id from #4's dict>, e => e.cancel())`. KubeJS 6 fires it on level join, so chunk loads
  count too. That also stops spawn eggs and spawners. Optional: it's harsh on tamed capuchins, so skip tamed ones
  (`entity.isTame()`).
- Write the change into [mob_spawns.md](../mob_spawns.md) section 9 (where the row came from) and
  [spawns_by_biome.md](../spawns_by_biome.md) gets regenerated (`python3 tools/gen_spawn_table.py`) only if
  `mob_spawns.json` changes.

### Decisions
- **Which survives?** Recommend the **capuchin survives**:
  - it's the one with player-facing play (taming, shoulder pet, darts);
  - it already eats Neapolitan bananas through the forge tag, so it survives the `food/drinks #3` banana merge;
  - its spawn is a normal biome spawn the pack's spawn tooling already tunes.

  Turn the chimps off with the group-chance config. The alternative (keep chimps) suits a pack that wants ambient
  jungle life over pets. Then the capuchin's shoulder-pet role is lost.
- **What about the gelada?** Recommend leave it. It's a different habitat and isn't a "duplicate monkey". Say so in
  the row when it closes.
- **Capuchin weight 28 in groups of 9-16 is heavy** ([mob_spawns.md](../mob_spawns.md) 418 lists it among the big
  weights). While touching it, consider `mob_spawns.json` `scale` 0.5 for the capuchin (folds into `mobs/spawns #3`).

### Gotchas
- Neapolitan's chimp spawn is part of the banana plant feature (Java). The pack's `wild_crops.json` moved where banana
  plants grow but didn't touch the chimp chance. Turning chimps off doesn't touch the bananas.
- Alex's Mobs `config/alexsmobs/*_spawns.json` (per-mob biome lists) exist next to the toml. The weight in the toml is
  the switch. Check that a 0 weight really adds no spawn with `/dumpspawns` after the change (MT-30).

### Tests
- **MT-30 One monkey:** after the change, `/dumpspawns` + `python3 tools/gen_spawn_table.py` → the loser has no biome
  spawn. Fly through a fresh jungle → only the survivor appears. The hidden items are gone from JEI.

---

## tweaks #6: Named mobs
Status Planned / Work patcherMod / KubeJS / TODO `mobs/tweaks #6`

### Goal
A small chance that a mob spawns with a random name. Deaths of named mobs, and kills by them, are announced in chat.
Killing one costs a little karma later (`largecontent/karma #1`).

### Now
- Nothing exists. Vanilla behaviour to build on (from vanilla 1.19.2 knowledge, not decompiled here):
  - `LivingEntity#die` only **logs** "Named entity ... died: ..." to the server console for a custom-named mob. It
    doesn't send it to chat (tamed pets' deaths go to the owner only).
  - A player killed by a named mob already gets a death message with the mob's name ("Steve was slain by Grumbo").
    `showDeathMessages` gamerule.
  - `setCustomName` alone doesn't make a mob persistent in 1.19 (the name tag item calls `setPersistenceRequired()`
    itself). So a script-named mob still despawns. Unverified: test MT-33.
- **Name source (verified):** MCA's `forge.net.mca.resources.Names` (note the relocated `forge.` package in the
  universal jar) has `public static String pickCitizenName(Gender gender)` over its `data/mca/names/<region>/<gender>.json`
  lists (Arabia, Croatia, Great Britain, Vietnam, ... weighted pools). The `Gender` enum class wasn't read. Find it
  with `unzip -l` under `forge/net/mca/entity/ai/relationship/`.
- KubeJS `ForgeEvents.onEvent` is already used in startup scripts
  ([func/compost.js](../../kubejs/startup_scripts/func/compost.js) 368-380, with a `global.*Listening` guard against
  double registration).
- Disabled mod `Nametag Crafting` sits in `disabledmods/` (unrelated, but name tags have no recipe).

### Build
1. **Config values** in a startup global (e.g. `global.namedMobs = { chance: 0.005, categories: ['monster'], ... }`
   in a new `kubejs/startup_scripts/func/named_mobs.js`). Plus an entity-type tag
   `creativeengineering:no_random_name` (server tag script, through `global.tags`, ids from #4's dict) for exclusions:
   bosses, MCA villagers (they have names), `minecraft:ender_dragon`, `wither`, `elder_guardian`, `warden`, and
   multi-part entities.
2. **Naming on spawn** (startup script, needs a restart to change). Hook the Forge spawn event so only natural and
   structure spawns roll, not chunk reloads:
   ```js
   if (!global.namedMobsListening) {
   	global.namedMobsListening = true
   	ForgeEvents.onEvent('net.minecraftforge.event.entity.living.LivingSpawnEvent$SpecialSpawn', event => {
   		try { global.namedMobs.roll(event.getEntity(), event.getSpawnReason()) } catch (err) { console.error('[named-mobs] ' + err) }
   	})
   }
   ```
   `roll`: skip unless the reason is `NATURAL` / `STRUCTURE` / `CHUNK_GENERATION` (decision), the category is
   allowed, the type isn't in the tag, and there's no custom name already. Then on `random < chance`:
   `mob.setCustomName(Text.of(name))` and `mob.getPersistentData().putBoolean('creativeengineering:named', true)`.
   Name: `Names.pickCitizenName(Gender.MALE/FEMALE random)` via `Java.loadClass('forge.net.mca.resources.Names')`,
   inside try/catch with a pack fallback list.
   **Unverified:** the event class name and `getSpawnReason()` in Forge 43.3.9 (`LivingSpawnEvent.SpecialSpawn`;
   Forge's 1.19.3+ `MobSpawnEvent.FinalizeSpawn` doesn't exist yet). Check in the Forge jar or an IDE before
   writing, and fall back to `EntityEvents.spawned` + a "rolled" persistent flag if it's missing (that fires on every
   level join, so the flag is required).
3. **Announcements** (server script, reloadable), e.g. `kubejs/server_scripts/other/named_mobs.js`:
   - `EntityEvents.death(event => ...)`: if the dead entity has the `creativeengineering:named` flag →
     `server.tell(entity.getCombatTracker().getDeathMessage())` (the vanilla message, with killer). Prefix it with
     the type (`Text.translate(entity.getType().getDescriptionId())`) so "Grumbo" reads as "Grumbo the Zombie".
   - If the killer (`event.source.actual`) is a flagged named mob and the victim isn't a player (player deaths are
     already announced) → announce it too, for named victims or a hand-picked list only (decision), or chat fills up.
   - Karma hook: when the killer is a player, call `global.karma?.add(player, 'kill_named_mob', -1)` if karma exists.
     It doesn't today, so leave a guarded call and a comment pointing at `largecontent/karma #1`.
4. **Optional patcherMod:** `Mob#checkDespawn` keeps despawning named mobs (wanted). If they should stay once a player
   has hurt them, set `setPersistenceRequired()` in a `EntityEvents.hurt` handler instead. That's KubeJS too, so
   patcherMod is only needed if the announcement should use a custom chat component or a config file. Recommend
   KubeJS only.
5. Writeup: a new section in [mob_block_tweaks.md](../mob_block_tweaks.md) (Mob and block tweaks is the home for
   small mob changes).

### Ids and data
- MCA names: `forge.net.mca.resources.Names#pickCitizenName(Gender)` (verified signature); `data/mca/names/*` (verified
  files).
- Persistent-data key `creativeengineering:named` (new). Entity tag `creativeengineering:no_random_name` (new).

### Decisions
- **Chance:** recommend 1 in 200 (0.5 %) of eligible spawns. Hostiles spawn and despawn constantly, so even this
  gives a named mob every few nights. Tune after play.
- **Which mobs:** recommend MONSTER category only at first (named zombies and skeletons are the fun case, and their
  kills of players are already announced), with animals as a later toggle at a lower chance.
- **Spawn reasons:** recommend natural + structure spawns. Exclude spawners and Quark monster boxes (`SPAWNER`, see
  #5), or a mob farm floods chat.
- **Announce kills by named mobs of non-players?** Recommend no at first (noise). Announce deaths of named mobs, and
  rely on vanilla for player deaths.

### Gotchas
- Mob farms: a named mob that drops into a farm grinder spams chat. Excluding `SPAWNER` covers spawners, not natural
  farms. Rate-limit announcements (one per N seconds per dimension) or skip deaths with no player involvement.
- MCA overwrites villagers with its own named ones (`config/mca.json` `overwriteOriginalVillagers`). Exclude MCA
  entity types and `minecraft:villager`.
- Startup-script event listeners don't come back on `/reload`. Keep the logic in `global.namedMobs` (startup) and
  everything else in server scripts.

### Tests
- **MT-31 Name rolls:** set `chance = 1` (startup, restart), and new hostile spawns at night carry a name. Reloading
  the chunk doesn't rename them.
- **MT-32 Announce:** kill a named zombie → one chat line with its name and type. A named skeleton that kills you →
  the vanilla death message names it.
- **MT-33 Despawn:** walk 128+ blocks away from an untouched named mob → it despawns like any other.

---

## tweaks #5: Quark monster boxes
Status Planned / Work config / patcherMod / data / TODO `mobs/tweaks #5`

### Goal
Answer "do they break when they spawn, and is that configurable?" Then rework them into a vault-like box: when its
mobs are dead it spits out items from its own loot table, then waits a cooldown and re-arms.

### Now (verified against `Quark-3.4-418.jar`)
- **Answer: yes, always; no, not configurable.** `vazkii.quark.content.world.block.be.MonsterBoxBlockEntity#tick`
  (static, Quark's own name): outside Peaceful, once a non-spectator player is within `activationRange` (2.5 blocks)
  it growls, counts `breakProgress` to 40 ticks (2 s), then `spawnMobs()`, plays the break effect and calls
  `level.removeBlock(pos, false)`. No config skips the removal.
- **Config** ([config/quark-common.toml](../../config/quark-common.toml) 1267-1282, same in `configs_edited/root_config/`):
  - `Chance Per Chunk` 0.2, `Min Y` -50, `Max Y` 0, `Min Mob Count` 5, `Max Mob Count` 8, `Activation Range` 2.5,
    `Search Range` 15;
  - `Enable Extra Loot Table` true: each mob from a box carries persistent tag `quark:monster_box_spawned` and drops a
    roll of `quark:misc/monster_box` when a player kills it (`MonsterBoxModule#onDrops`);
  - dimensions: overworld only;
  - the module switch is `"Monster Box" = true` at line 795.
- **Spawns:** `quark:misc/monster_box_spawns` = one spawn egg per mob: zombie 7, cave spider 2, witch 1. Each mob is
  spawned `MobSpawnType.SPAWNER` with a small random push.
- **Rewards:** `quark:misc/monster_box` (1 roll per mob): raw iron, copper, gold, coal, bone blocks, cobweb, TNT,
  infested stone, heads, horse armour, saddle, name tag, enchanted book, 12 music discs, a damaged diamond pickaxe or
  sword, golden apple, and **`quark:monster_box` itself (weight 1)**. The block can also come from Quark's Ancient
  Tome table (`quark-common.toml` 502, `quark:misc/monster_box,5` is the tome's weight in that table, not a box drop).
- **Block:** `MonsterBoxBlock`, hardness 25, its block loot table has no pools (mining one drops nothing). The block
  entity extends AutoRegLib's `ARLBlockEntity` (`writeSharedNBT` / `readSharedNBT` hooks, both empty in the box).
- **Placement:** `MonsterBoxGenerator`, `UNDERGROUND_DECORATION` step, one try per chunk at 20 %, on a sturdy floor
  under air, Y -50 to 0, not in flat worlds. It's a Quark world generator, not a structure
  ([structure_tuning.md](../structure_tuning.md) 1662 says the same).
- The pack's loot layer doesn't touch either table (no `quark:misc` in `kubejs/server_scripts/loot/`). `loot.md` 413
  lists the box config knobs.

### Build: config-only option
`"Monster Box" = false` (module off), or keep as is. Nothing else in config changes the breaking.

### Build: vault rework (patcherMod)
1. **Config** `CEConfig.MonsterBox` (new section `[monster_box]`, next to `Raids` at CEConfig 679, built in the
   constructor around line 94):
   - `vaultMode` (true);
   - `cooldownTicks` (72000 = 1 hour real time, about 3 in-game days);
   - `rewardTable` (`creativeengineering:misc/monster_box_reward`);
   - `rewardRolls` (3);
   - `leashRange` (24: a wave mob farther than this from the box counts as gone);
   - `checkIntervalTicks` (20).
2. **Mixin** `MonsterBoxBlockEntityMixin` on `vazkii.quark.content.world.block.be.MonsterBoxBlockEntity`
   (`remap = false` on the class; `tick` and `spawnMobs` are Quark names):
   - `@Inject(method = "tick", at = @At("HEAD"), cancellable = true)`. When `vaultMode`, run the pack's state machine
     instead and `ci.cancel()`. Logic in a plain class `world/MonsterBoxVault.java`, so the mixin stays thin, as
     `LeafDecayRange` does for leaves:
     - **ARMED:** Quark's own countdown. Copy its player check and `breakProgress` logic, or call through. Simplest:
       keep Quark's tick but `@Redirect` the `Level#removeBlock` call in it (target
       `Lnet/minecraft/world/level/Level;m_7471_(Lnet/minecraft/core/BlockPos;Z)Z`, `remap = false` with the SRG
       name: patcherMod ships no refmap, build.gradle 20-26). That turns removal into "switch to ACTIVE", resets
       `breakProgress` through an `@Accessor`, and claims the wave.
     - **Claim the wave:** right after `spawnMobs()`, collect mobs within 3 blocks whose persistent data has
       `quark:monster_box_spawned` and no `ce:box` yet. Store their UUIDs and stamp each with `ce:box` = the box's
       `BlockPos.asLong()`. This avoids injecting into the private lambda in `spawnMobs`.
     - **ACTIVE:** every `checkIntervalTicks`, a wave mob counts as gone when `ServerLevel#getEntity(uuid)` is null,
       or it's dead, or it's farther than `leashRange`. When all are gone, eject `rewardRolls` rolls of `rewardTable`
       (chest context with origin; pop items up with a small spread like a dispenser; `ItemEntity` + pickup delay 10),
       play a sound, and go to COOLDOWN with `until = gameTime + cooldownTicks`.
     - **COOLDOWN:** do nothing (no growl, no countdown) until `until`, then ARMED.
   - **State storage:** use Forge's `BlockEntity#getPersistentData()` (saved with the block entity as `ForgeData`),
     so no NBT override of `ARLBlockEntity` is needed. Keys `ce:phase`, `ce:wave` (list of UUID int arrays),
     `ce:until`.
3. **Loot:** new table `kubejs/data/creativeengineering/loot_tables/misc/monster_box_reward.json`, e.g. Quark's
   `misc/monster_box` without the `quark:monster_box` entry and without TNT and infested stone. With vault mode on,
   set `Enable Extra Loot Table = false` in both `quark-common.toml` copies, or players get both the per-mob drops and
   the box reward. If the pack-side loot layer should own it, add the table there instead (see
   [loot.md](../loot.md); `loot/07_rules.js` holds per-table rules).
4. **No farming.** The `quark:monster_box` item in the old table plus vault mode = a placeable, re-arming zombie
   spawner. Dropping that entry (step 3) closes the main route. Decide whether player-placed boxes (creative,
   tomes) should be vaults at all. A cheap rule is "only boxes that generated": stamp `ce:natural` from the generator
   with a `MonsterBoxGenerator` mixin, or treat a box without any `ce:` data on first tick after placement by a
   player as plain Quark (`BlockEvent.EntityPlaceEvent` → stamp `ce:placed`).
5. Writeup: a section in [mob_block_tweaks.md](../mob_block_tweaks.md), and a line in [loot.md](../loot.md) 413
   about `Enable Extra Loot Table`.

### Ids and data
- Classes (verified): `vazkii.quark.content.world.module.MonsterBoxModule` (statics `activationRange`, `minMobCount`,
  `maxMobCount`, `enableExtraLootTable`, `MONSTER_BOX_LOOT_TABLE`, `MONSTER_BOX_SPAWNS_LOOT_TABLE`),
  `...world.block.MonsterBoxBlock`, `...world.block.be.MonsterBoxBlockEntity` (`private int breakProgress`,
  `public static void tick(Level, BlockPos, BlockState, MonsterBoxBlockEntity)`, `private void spawnMobs()`),
  `...world.gen.MonsterBoxGenerator#generateChunk`.
- Tables (verified): `quark:misc/monster_box`, `quark:misc/monster_box_spawns`, `quark:blocks/monster_box` (empty).
- Item: `i.monster_box` = `quark:monster_box` ([item_dict.js](../../kubejs/startup_scripts/_lib/item_dict.js) 3223).
- `Quark-3.4-418.jar` and `AutoRegLib-1.8.2-55.jar` into `development/patcherMod/libs/` (Quark is likely already
  there for `FeedingTroughModuleMixin`).

### Decisions
- **Rework or config only?** Recommend the rework: it turns a one-shot ambush into a repeatable small dungeon, which
  is what the row asks for. If the patcherMod time isn't there, leave the config as is (the boxes are harmless).
- **Cooldown length:** recommend 72000 ticks (1 hour of server time, gameTime-based so it advances only while the
  chunk's level ticks; the pack's time mods pause time but not `gameTime`. Check `time.md` if that matters).
- **Per-player rewards like the vanilla vault?** Recommend no: one reward per wave for whoever is there. A per-player
  "already looted" list is a later step.
- **Keep the per-mob extra drops?** Recommend off in vault mode (the box reward replaces it).

### Gotchas
- Client side: Quark's tick also runs on the client for particles (flame while idle, smoke while counting). The
  pack's phase lives in persistent data, which isn't in the update tag, so the client will keep showing idle flames
  during cooldown. Acceptable, or sync a boolean through the `ARLBlockEntity` update tag (needs overriding
  `writeSharedNBT`, so the mixin must extend `ARLBlockEntity` to call `super`).
- Mobs that wander into unloaded chunks read as gone (`getEntity` null). That's fine: it pays out a little early.
- Peaceful: Quark's tick returns early. Keep that.
- Named mobs (#6) should skip `SPAWNER` spawns, or every wave can name a zombie.

### Tests
- **MT-34 Box stays:** find or `/setblock` a `quark:monster_box`, walk up → mobs spawn, the box stays.
- **MT-35 Reward:** kill the wave → items pop out of the box once; walking up again within the cooldown does nothing.
- **MT-36 Re-arm:** `cooldownTicks = 200` (game stopped), repeat → after 10 s it growls again.
- **MT-37 Off switch:** `vaultMode = false` → Quark behaviour (the box breaks).

---

## tweaks #2: MCA villagers glitch while sleeping
Status Bug / Work patcherMod / TODO `mobs/tweaks #2`

### Goal
Sleeping MCA villagers lie still in bed instead of lying with legs and arms bent out and jittering.

### Now
- MCA Reborn 7.5.20 (`mca`, both sides; the jar ships `forge/`, `fabric/` and `quilt/` copies of every class, and Forge
  loads the `forge.net.mca.*` package, see `forge-mca.mixin.json`).
- **MCA's model (verified):** `forge.net.mca.client.model.VillagerEntityBaseModelMCA` extends vanilla `HumanoidModel`.
  `VillagerEntityModelMCA` extends it, and the renderer is `forge.net.mca.client.render.VillagerLikeEntityMCARenderer`
  (`HumanoidMobRenderer`). `setupAnim` (`m_6973_`) only changes baby wobble, limb-angle scale and the panic arm wave,
  then copies limbs to the wear layers. **Nothing in it handles the sleeping pose.** A sleeping MCA villager is drawn
  with the plain humanoid limb animation, rotated flat by `LivingEntityRenderer#setupRotations`. That fits the
  symptom: any leftover limb swing (`limbDistance`) or the `riding` / `crouching` flags would show as bent-out limbs.
  Vanilla's riding pose is exactly "legs splayed out" (legs xRot -1.41, yRot ±π/10).
- **Not Not Enough Animations (verified):** NEA 1.12.2's `PlayerEntityModelMixin` targets `PlayerModel` (MCA doesn't
  use it) and acts only on `AbstractClientPlayer`. MCA's own client mixins (`client.MixinLivingEntityRenderer` etc.)
  only touch players rendered with the villager model.
- Server side: `VillagerEntityMCA` returns fixed sleeping dimensions (`getDimensions(Pose.SLEEPING)`). The sleep task
  itself (brain) wasn't read.
- The remaining causes can only be told apart in game:
  - (a) the model's limb swing while asleep (client);
  - (b) the server flipping the villager between sleeping and awake every few ticks (jitter = pose flapping, e.g. a
    bed the POI system doesn't accept: [Bedspreads](../../mod_sides.md) beds, Comforts sleeping bags or hammocks);
  - (c) the villager riding something (a seat).

### Build
1. **Diagnose first** (MT-38): next to a sleeping villager at night, run
   `/data get entity @e[type=mca:male_villager,limit=1,sort=nearest] SleepingX` a few times. Also check `Pose` via
   F3 + looking at it (or `/data get ... Brain`), and whether it has a vehicle (`/data get ... RootVehicle`).
   - Steady sleeping data + bent limbs → (a).
   - The sleeping data appearing and vanishing → (b).
   - A vehicle → (c).

   Try a vanilla bed vs a Bedspreads bed, and note the bed block (F3) in the row.
2. **(a) fix, client mixin** `McaVillagerSleepPoseMixin` in the `client` list of
   [creativeengineering.mixins.json](../../development/patcherMod/src/main/resources/creativeengineering.mixins.json)
   (line 122):
   ```java
   @Mixin(targets = "forge.net.mca.client.model.VillagerEntityBaseModelMCA")
   public abstract class McaVillagerSleepPoseMixin<T extends LivingEntity> extends HumanoidModel<T> {
       // HEAD: zero the swing before vanilla poses the limbs; then riding/crouch flags off while asleep
       @Inject(method = "m_6973_", at = @At("HEAD"))   // setupAnim; MCA's override, vanilla SRG name
       private void ce$stillWhenAsleep(T entity, float limbAngle, float limbDistance, ..., CallbackInfo ci) { ... }
       @Inject(method = "m_6973_", at = @At("TAIL"))
       private void ce$flatLimbs(T entity, ..., CallbackInfo ci) {
           if (!entity.isSleeping()) return;
           // arms and legs straight: xRot/yRot/zRot = 0 on leftArm/rightArm/leftLeg/rightLeg; riding = crouching = false
       }
   }
   ```
   Changing a captured parameter at HEAD needs `@ModifyVariable(argsOnly)` on `limbDistance` → 0 when sleeping. The
   TAIL reset alone is enough if the wear layers are re-copied: `VillagerEntityModelMCA#setupAnim` copies limbs to
   wear after `super`, so target `VillagerEntityModelMCA` too, or reset the wear parts as well. Use
   `@Mixin(targets = ...)` and `require = 0`, so a future MCA rename doesn't crash the client. patcherMod ships no refmap
   ([build.gradle](../../development/patcherMod/build.gradle) 20-26): every mixin uses SRG names with `remap = false`,
   so write `method = "m_6973_"` with `remap = false`. Use SRG names for fields too: `ModelPart` xRot `f_104203_`
   and zRot `f_104205_`, `EntityModel` young `f_102609_` (all three seen in MCA's decompiled model). Look up yRot,
   `riding` and `crouching` in the 1.19.2 mappings.
3. **(b) fix:** find which bed or POI. If it's a modded bed, add it to `#minecraft:beds` /
   `minecraft:point_of_interest_type/home` or exclude it, which is data, no mixin. Read MCA's sleep task
   (`forge/net/mca/entity/ai/brain/VillagerTasksMCA` and its tasks) once the bed is known.
4. **(c) fix:** none needed in the model. Stop the cause.
5. Writeup: a section in [mob_block_tweaks.md](../mob_block_tweaks.md) with the diagnosis result.

### Ids and data
- Entity ids: `mca:male_villager`, `mca:female_villager` (MCA replaces vanilla villagers; `config/mca.json`
  `overwriteOriginalVillagers = true`). The ids are from MCA's naming, unverified here. Check with `/summon mca:` tab
  completion.
- Classes (verified): `forge.net.mca.client.model.VillagerEntityBaseModelMCA`, `...VillagerEntityModelMCA`,
  `...client.render.VillagerLikeEntityMCARenderer`, `forge.net.mca.entity.VillagerEntityMCA`.

### Decisions
- **Fix in the model even if the cause is (b)?** Recommend yes for (a) and (c). For (b) fix the cause: a model reset
  would hide a villager that keeps waking up.

### Gotchas
- A client mixin, so every client needs the new patcherMod jar (patcherMod is "both" in
  [mod_sides.md](../../mod_sides.md); fine).
- MCA updates often (this jar is a 2026-03 build). Keep the mixin `require = 0` and the target by name.

### Tests
- **MT-38 Diagnose:** the `/data` checks in Build 1. Record the result in the row.
- **MT-39 Still sleepers:** a village at night → sleeping villagers lie straight and still, in vanilla beds and
  in every modded bed type that villagers use.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| #1 | Mob and block tweaks (bubble columns, Bushier Flowers off, trough retry, asphalt) | Testing | Nothing wrong found. MT-01 to MT-12 open. The same writeup now also carries the leaf decay work (MT-13 to MT-25, `farming/trees #1`), built 2026-10-19/20 after the 1.0.9 deploy and correctly marked not deployed |

## Writeup corrections made

None. Nothing in [mob_block_tweaks.md](../mob_block_tweaks.md), [mob_spawns.md](../mob_spawns.md) 393 or the other
linked writeups contradicts the code. Proposed TODO correction (not edited): `mobs/tweaks #4` Details should also name
`startup_scripts/func/sample_syringe.js` (8 entity-id keys) next to `entitytags.js`.
