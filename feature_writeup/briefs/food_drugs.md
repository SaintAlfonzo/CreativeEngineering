# Brief: food / drugs

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# food`, Sub `drugs`; verified 2026-10-31 (line refs refreshed,
`drugs #2` added). There is no feature writeup yet.
Write one when building (suggested `feature_writeup/smoking.md`, test prefix `SM-`) and list it in
[feature_writeup/README.md](../README.md). Source: the cooking plan tree, `drugs/smoking` (cooking.md, removed
2026-10-19, last in git at `c82dd31^`, lines 380-398).

## Scope and order

`drugs #1` is too big for one step; build it in phases. `drugs #2` (tobacco look and packing) is small and can go first
or with phase A.

| Phase | What | Work | Depends on |
|---|---|---|---|
| A | Take over The Dirty Stuff's smoking: one data table, lighter check, smoke limit with growing Nausea, tolerance | KubeJS (startup + server + client) | nothing |
| B | Pipe: reusable smoking item, loaded with tobacco and an optional herb additive | KubeJS item + recipes (NCH) | A |
| C | Rolling: rolling paper, roll recipes (hand + Create automation) | KubeJS + NCH | A |
| D | Smokeleaf: second smokable crop with its own effects | KubeJS crop or new jar, worldgen | A; decision |

| - | `drugs #2` Tobacco: retexture, packing with canvas (rename shipped 1.1.05) | resource pack + KubeJS | nothing |

The tolerance rule (row Decisions: "the tolerance mechanic") has to be settled before A; it is the first decision below.
Related rows:
- `farming/crops #2`: tobacco is the only crop at parity; a smokeleaf crop would need the same treatment
  ([crops.md](../crops.md) 2.6).
- `ui/*` tooltip conventions for the effect lines ([tooltips.md](../tooltips.md)).

---

## drugs #1: Smoking
Status **Planned** · Work: KubeJS / patcherMod · id `food/drugs #1`

### Goal
From the cooking plan:
- Smoking by **rolling** (cigarettes) and with a **pipe**.
- A **smoke limit**: smoking past it gives Nausea that grows longer each time, and **tolerance** builds up.
- **Herb additives** give effects.
- Tobacco and **smokeleaf** each have positive effects (Comfort?) and negative ones.
- Maybe a Spice of Life-style tolerance.

### Now
The pack already ships **The Dirty Stuff 2.1** (`the_dirty_stuff`, `the_dirty_stuff-2.1-forge-1.19.2.jar`, "both" in
[pack/MODLIST.md](../../pack/MODLIST.md)). Decompiled 2026-10-21 (MCreator mod, verified):
- **Items:** 15 cigarettes and 15 cigars, plus the inputs `tobacco_seeds` (block), `tobacco_leafs`, `dried_tobacco_leafs`,
  `tobacco` and three `*_package` items (8x storage). Every item stacks to 16 (`m_41487_(16)` in each item class).
  - Cigarettes: `cigarette`, `blue_`, `light_blue_`, `light_gray_`, `magenta_`, `orange_`, `red_`, `yellow_cigarette`,
    `black_light_`, `light_gray_light_`, `white_light_cigarette`, `light_gray_`, `orange_`, `pink_`, `red_ultralight_cigarette`.
  - Cigars: the same colours as `*_cigar`, `black_slim_` / `light_gray_slim_` / `white_slim_cigar`, `*_ultraslim_cigar`.
- **Recipes** (shapeless, `data/the_dirty_stuff/recipes/`): cigarette = `tobacco` + `paper` (+ one flower);
  cigar = 2 `tobacco` + `dried_tobacco_leafs` (+ the same flower). Flower per colour:
  - blue: cornflower; light_blue: blue orchid; light_gray: oxeye daisy; magenta: allium; orange: sunflower;
    red: poppy; yellow: dandelion.
  - black_light / black_slim: wither rose; light_gray_light / light_gray_slim: azure bluet; white_light / white_slim:
    lily of the valley.
  - ultralight / ultraslim: the matching tulip.

  **So flower "herb additives" already exist.** Workstation: `workstation.json` (iron ingots + nuggets).
- **Workstation** (`WorkstationOnTickUpdateProcedure`): 4 `tobacco_leafs` in slot 0 are processed over 200 ticks
  (`craftingTime`) into `dried_tobacco_leafs`, then `tobacco` (slot 1, up to 64). Check the exact slot flow in game.
- **Smoking** (`item/*Item.use` → `procedures/*RightclickedProcedure.execute(LevelAccessor, x, y, z, Entity, ItemStack)`):
  - It needs a flint and steel (damaged 1) or a fire charge (used up) in the **off hand**; otherwise it shows the
    action-bar message "You need to hold a flint and steel in your off hand". The SRG `Items.f_42409_` / `f_42613_`
    are read as flint and steel / fire charge from memory of the 1.19.2 tables, not checked.
  - Every smoke gives **Nausea V for 5 s**, an effect A at once and an effect B after A ends
    (`TheDirtyStuffMod.queueServerWork`), with a **20 s cooldown** on that item.
  - Cigarettes: A 15 s, B 5 s. Cigars: A 30 s, B 15 s. Light / slim / ultra variants: A 5 or 15 s, then B 20 or 35 s.
  - Effects by colour (SRG ids mapped from memory, unchecked):

| Variant | A (now) | B (after) |
|---|---|---|
| plain | Speed | Slowness |
| blue | Jump Boost | Slowness |
| light_blue, yellow | Saturation (4 / 8 ticks) | Hunger |
| light_gray | Regeneration | Poison |
| magenta | Fire Resistance | Glowing |
| orange, red | Night Vision | Blindness |
| light_gray_light / _slim | Blindness | Night Vision |
| black_light / _slim | Wither | Resistance |
| white_light / _slim | Poison | Regeneration |
| *_ultralight / *_ultraslim (4 colours, identical) | Weakness | Strength |

- **World:** tobacco seeds from the wandering trader (1 emerald); villagers buy 7 leaves for 1 emerald; a loot modifier
  adds `chests/tobacco_village_chest` to `chests/village/village_plains_house`.
- **Pack side so far (2026-10-31):** tobacco crop parity (`development/patcherMod/.../mixin/TobaccoPlacementMixin.java`,
  crops.md 2.5); WTHIT growth line now in Creative Delight
  (`development/creativeDelight/src/main/resources/assets/creativedelight/crop_growth.json:28`); compost values
  (`development/creativeDelight/src/main/resources/kubejs_startup/the_dirty_stuff/compost_values.js`); tobacco follows
  the seasons (1.1.05); the three packages renamed to bundles (namechanges.js:304-308, shipped 1.1.05, `drugs #2`);
  seed-pouch fit (1.1.05). item_dict has 7 keys (`item_dict.js:13416-13423`, `i.tobacco`, `i.tobacco_leafs`...).
  **No cigarette or cigar has a key**, and no pack script touches smoking. No config file (`config/` has none for it).
- **Not in the pack:** smokeleaf (no item anywhere), a pipe (`i.pipe` is `quark:pipe`, a pipe block), any Comfort
  link. `tobacconistmod` was removed in 1.0.4 (`tools/gen_stack_sizes.py:34` `STALE_NAMESPACES`).
- **Building blocks in the pack:**
  - `farmersdelight:comfort` (FD effect; patcherMod `campfire/CampfireComfort.java:24` applies it).
  - KubeJS effect registration ([magic_resistance.js](../../kubejs/startup_scripts/magic/magic_resistance.js) registers
    `creativeengineering:magic_resistance` with `StartupEvents.registry('mob_effect')`).
  - The Brewin' port's stacking Tipsy and the hidden Intoxication ([alcohol.md](../alcohol.md) 2.4, T1-T6, E1), a model
    for "effect level = how much you had".
  - Spice of Life: Apple Pie's queue ([food.md](../food.md) 2.3: last 32 foods, each with an age, penalty `1 - age/32`).
  - KubeJS 1902.6.2-build.73 (jar checked): `ItemEvents.rightClicked` `hasResult()` (cancellable);
    `ItemBuilder.useAnimation / useDuration / use / finishUsing / releaseUsing / maxDamage`.

### Build

**Phase A: one smoking system over The Dirty Stuff (KubeJS).**
1. **Data, both sides:** `kubejs/startup_scripts/cooking/smoking.js` defines `global.smoking`:
   - `items`: item_dict key → `{ kind: 'cigarette'|'cigar'|'pipe', base: [...effects], delayed: [...], herb: '<flower key>' }`.
   - `herbs`: herb key → effects.
   - Limits: `limit`, `windowTicks`, `nauseaStepTicks`, tolerance numbers.

   Effects are `[global.effect key, seconds, amplifier]`. Model the table on `global.alcohol` in
   [alcohol_drinks.js:15](../../kubejs/startup_scripts/cooking/alcohol_drinks.js): a startup global, so the client
   tooltip and the server logic read the same data. Start from the table above, then retune: tobacco's own line
   (plain cigarette) gets the positive / negative pair the plan names.
2. **Item ids:** add the 30 smokables to item_dict under the existing `// ===== the_dirty_stuff (7) =====` block
   (line 13416), plain path keys (`i.cigarette`, `i.blue_cigar` ...; no collisions found for `cigar*`), and
   `i.dirty_stuff_workstation = 'the_dirty_stuff:workstation'` (namespaced key, the way `i.farmersdelight_milk_bottle`
   avoids a generic name). Add `ef.comfort = 'farmersdelight:comfort'` and the new effect key (below) to
   [effect_dict.js](../../kubejs/startup_scripts/_lib/effect_dict.js) (it has a modded block since, e.g. `ef.caffeinated`
   line 51; no `ef.comfort` yet).
3. **New effect:** `creativeengineering:smoked` (KubeJS `mob_effect` registry, `'basic'`, harmful = false, grey colour),
   as in magic_resistance.js. Its **amplifier is the smoke count in the current window**, so the player sees the limit
   coming. The effect does nothing itself; the server script reads it.
4. **Take over the use:** `kubejs/server_scripts/cooking/smoking.js`:
   ```js
   ItemEvents.rightClicked(e => {
     var def = SMOKABLE[e.item.id]; if (!def) return
     e.cancel()                         // stops Item.use, so the mod's procedure never runs
     if (!hasLighter(e.player)) { e.player.setStatusMessage(Text.translate('creativeengineering.smoking.need_lighter')); return }
     useLighter(e.player)               // flint and steel: damage 1; fire charge: shrink 1 (as the mod does)
     smoke(e.player, def)               // effects, limit, tolerance, particles, sound, cooldown, shrink 1
   })
   ```
   Also cancel on the client (`client_scripts`, same id list, `e.cancel()` only). Otherwise the client runs the mod's
   `use()` locally: a ghost shrink, a client cooldown and sounds. KubeJS `event.cancel()` ends the handler chain, so
   keep the two handlers in different script types and check that both fire.
   - Cooldown: `player.cooldowns.addCooldown(item, ticks)` (Mojmap; check the KubeJS wrapper name in game).
   - Delayed effects: `e.server.scheduleInTicks(t, cb)`.
   - Particles `minecraft:campfire_cosy_smoke` at the mouth (the mod uses `f_123777_` with the same 0.4-block offset maths).
5. **Limit and tolerance** (see Decisions for the numbers). Per player in `player.persistentData.ce_smoking`:
   `{ recent: [gameTime...], tolerance: n, lastDecay: gameTime, history: [id...] }`.
   - Smoke count = entries in `recent` younger than `windowTicks`.
   - Over `limit`: Nausea for `nauseaStepTicks x (count - limit + 1)` and tolerance +1; heavy overuse adds Hunger or Weakness.
   - Tolerance scales positive durations by `1 / (1 + 0.15 x tolerance)` and loses 1 per in-game hour without a smoke.
   - Optional SoL-style part: `history` holds the last 8 smokables. Each repeat of the same item cuts its positive
     duration by 1/8, so variety (different herbs) keeps the effects.
6. **Tooltips:** one line per smokable listing its effects, the way the pack draws food effect lines
   ([tooltips.md](../tooltips.md); [client_scripts/tooltips.js](../../kubejs/client_scripts/tooltips.js)), read
   from `global.smoking` so the table stays the only source. Plus a JEED page:
   `global.NCH.jeedProvider(effect, providers, e)` (`neoCraftHelper.js:2065`) for each effect → the smokables that give it.
7. **Lang:** `creativeengineering.smoking.need_lighter`, effect name and description in
   `kubejs/assets/creativeengineering/lang/en_us.json`. Run `tools/gen_effect_descriptions.py` if it covers new effects
   (alcohol.md line 446 notes it prunes placeholders).

**Phase B: pipe.**
1. `StartupEvents.registry('item')`: `creativeengineering:smoking_pipe` (key `i.smoking_pipe`; `i.pipe` is taken).
   - `.maxStackSize(1)`, `.useAnimation('toot_horn')` (held to the mouth), `.useDuration(() => 32)`.
   - `.use(...)` returns true only when the pipe is loaded and a lighter is in the off hand.
   - `.finishUsing(...)` calls the same `smoke()` (move it into a `global.smoking.smoke` function, or keep the server
     logic in one server file and have `finishUsing` return early on the client).
   - Load state in NBT: `{Fill:'the_dirty_stuff:tobacco', Herb:'minecraft:dandelion', Puffs:4}`. Model: empty and
     loaded through an item model predicate (KubeJS `ItemEvents.modelProperties`), or two textures.
2. Recipes through NCH:
   - Pipe: `global.NCH.shaped` (for example a stick + bowl-shaped wood: decide the material).
   - Loading: `global.NCH.shapeless(Item.of(i.smoking_pipe, '{Fill:...,Puffs:4}'), [i.smoking_pipe, i.tobacco], e, id)`,
     one per herb. NCH's result JSON handles NBT (`_nbtItemJson`, `neoCraftHelper.js:550`). If the empty-pipe ingredient
     has to ignore NBT, or a loaded pipe has to be rejected, extend `shapeless` with an `opts.strictNbt` instead of
     hand-writing `e.custom`.
3. Herbs to start with (all in item_dict):
   - `neapolitan:mint_leaves`, `minecraft:dandelion` (Comfort?), `farmersrespite:rose_hips`, the Farmer's Respite tea
     leaves (green / yellow / black).
   - The Dirty Stuff flowers, kept with their current effects.
   - `immersive_weathering:weeds` as the smokeleaf stand-in if D is skipped.

**Phase C: rolling.**
1. Rolling paper: `creativeengineering:rolling_paper` (KubeJS item, key `i.rolling_paper`). Made from paper with
   `global.NCH.fdCutting` (`neoCraftHelper.js:1093`, paper + knife → 4 rolling paper; NCH has no Create cutting helper)
   or `stonecutting` (`:929`).
2. Swap paper for rolling paper in the 15 cigarette recipes:
   `global.NCH.replaceInput({ mod: 'the_dirty_stuff', output: '#...' }, 'minecraft:paper', i.rolling_paper, e)`
   (`neoCraftHelper.js:635`).
3. Automation: `global.NCH.createDeploying` (`:1694`, rolling paper onto tobacco) or `createSequencedAssembly`
   (`:1737`) for the coloured ones (deploy flower, deploy paper). Register the transitional item with
   `NCH.registerIncomplete` (`:1789`), as the pack does for other sequences. Note `food/drinks #12`: NCH fluid steps in
   sequenced assemblies fail to load today; these steps carry no fluid.

**Phase D: smokeleaf** (only if wanted, see Decisions). A KubeJS `crop` block builder (`CropBlockBuilder` exists in
this KubeJS), a seed source (wandering trader or loot, like tobacco), crop parity through
`#creativeengineering:crops/farmland_crops` and the WTHIT `crop_growth.json` line (crops.md 2.6, 2.9). It gets its own
effects table in `global.smoking`.

### Ids and data
- Verified in `the_dirty_stuff-2.1-forge-1.19.2.jar`: item ids, stack 16, recipes and ingredients, the off-hand check
  structure, durations, the 400-tick cooldown, loot modifier and trades.
- Unchecked: the SRG → vanilla effect and item names (from memory; confirm with `/dumpall` `mob_effects.json` or by
  smoking one in game). The KubeJS player-wrapper names for cooldowns and status messages.
- New ids: effect `creativeengineering:smoked`; items `creativeengineering:smoking_pipe`, `rolling_paper` (+ maybe
  `smokeleaf`, `smokeleaf_seeds`); keys `i.cigarette` ... `i.dirty_stuff_workstation`, `i.smoking_pipe`,
  `i.rolling_paper`, `ef.comfort`, `ef.smoked`.

### Decisions
- **Where the code lives** (routing since 2026-10-25, memory `creative-delight-routing`): `NCH` paths are in patcherMod's
  jar (`development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/neoCraftHelper.js`); the
  `kubejs/startup_scripts/cooking/smoking.js` / `server_scripts/cooking/smoking.js` paths above are from 2026-10-21.
  Options: Creative Delight's `the_dirty_stuff/` trees (the mod's compost already lives there) or Creative Brewing
  (intoxicants and effects, next to alcohol's Tipsy model). Recommendation: Creative Brewing for the smoking system and
  its effect, CD for crop / packing recipes; owner to confirm.
- **Tolerance mechanic (the row's open decision).** Recommend the two-layer model above: a short window limit
  (3 smokes per 5 in-game minutes; each smoke over it adds 5 s to the Nausea) plus a slowly decaying tolerance that
  shortens the positive effects. Add the SoL-style variety history only if repeats should matter beyond the limit. It
  costs one list in persistentData and gives herb additives a reason to exist.
- **Keep The Dirty Stuff's colour variants?** 30 smokables with heavy duplication (red = orange, yellow = light_blue,
  four identical ultralights). Recommend keeping the items (tobacco crop, trades and loot stay) and retuning them in
  `global.smoking`. Each flower then acts as one herb additive with one distinct effect. Hide nothing.
- **Lighter:** keep "flint and steel or fire charge in the off hand" (the mod's rule, players know it). Optionally add a
  `#creativeengineering:lighters` tag (`global.tags`) so a later lighter item or a lit torch counts.
- **Smokeleaf:** a new crop, or skip it and let herbs carry the effects. Recommend skipping it in the first build; D is
  the most work and the only phase that needs worldgen or loot.
- **KubeJS or patcherMod for the take-over:** recommend KubeJS (event cancel). Fallback: one patcherMod mixin with
  `@Mixin(value = {CigaretteRightclickedProcedure.class, ...}, remap = false)` on `execute` HEAD, calling a Java handler.
  All 30 procedures share the signature `execute(LevelAccessor, double, double, double, Entity, ItemStack)`, and
  `TobaccoPlacementMixin` already shows the pattern on this mod.
- **Comfort as tobacco's positive effect:** fits (FD's Comfort is the pack's "resting" buff, also from campfires). Recommend Comfort 30 s for a plain cigarette.

### Gotchas
- `ItemEvents.rightClicked` fires for the main and the off hand. With a smokable in the off hand and a lighter in the
  main hand the mod refuses; keep that, or swap the roles explicitly.
- Cancelling only on the server leaves the client running the mod's `use()` (sound, a ghost shrink, a client-side cooldown).
- `queueServerWork` effects from the mod no longer run once `use()` is cancelled. Nothing is left over, but check that
  the mod has no other path that calls the procedures (dispensers, for example; none seen).
- `player.persistentData` survives logout. Whether it survives death in this KubeJS build is unchecked; a reset on death
  is acceptable.
- Workstation output stacks to 64 in its slot but the items stack to 16 in inventories (`food/stack-sizes` has no rule
  for these). Consider `tobacco` 64 in `tools/gen_stack_sizes.py` `MISC_64`.

### Tests
Prefix `SM-` (new writeup):
- **SM-01 Lighter:** right click a Cigarette with an empty off hand → the message, nothing used. With flint and steel →
  one cigarette used, the flint and steel loses 1 durability, the effects apply, a 20 s cooldown.
- **SM-02 Limit:** four smokes inside the window → the fourth gives Nausea 5 s longer than the third; the
  `creativeengineering:smoked` level shows the count.
- **SM-03 Tolerance:** ten smokes over an in-game day → the positive effect is shorter; after a smoke-free stretch it recovers.
- **SM-04 Client:** no double sound, no ghost item count change on a dedicated server.
- **SM-05 Pipe (phase B):** load a pipe with tobacco + dandelion → 4 puffs with the dandelion effect, then empty.
- **SM-06 Rolling (phase C):** cigarette recipes take rolling paper; the Create deploying recipe makes one.

---

## drugs #2: Tobacco
Status **Planned** · Work: KubeJS / resource pack · id `food/drugs #2` · part of the smoking work (`drugs #1`)

### Goal
Retexture tobacco, rename the packages, and let tobacco be packed with canvas.

### Now (2026-10-31)
- **Rename: done and shipped in 1.1.05.** [namechanges.js:304-308](../../kubejs/client_scripts/namechanges.js#L304):
  `tobacco_leafs_package` -> "Bundle of Tobacco Leaves", `dried_tobacco_leafs_package` (and the mod's stray
  `dried_tobacco_leaf_package` key) -> "Bundle of Dried Tobacco Leaves", `tobacco_package` -> "Tobacco Bundle"
  (upstream both the leaf package and the tobacco package read "Tobacco Package"). Changelog 1.1.05 line 101.
- **Packing:** the mod's three recipes (`data/the_dirty_stuff/recipes/{tobacco_leafs,dried_tobacco_leafs,tobacco}_package.json`,
  verified in `mods/the_dirty_stuff-2.1-forge-1.19.2.jar`) are shapeless 8 of the item + 1 `minecraft:paper` -> 1
  package; the unpack recipes `tobacco.json`, `tobacco_leafs.json`, `dried_tobacco_leafs.json` give 8 back (verified, so
the paper is lost on unpacking). Canvas is
  `i.canvas` (`farmersdelight:canvas`, item_dict.js 158).
- **Textures:** the mod's `assets/the_dirty_stuff/textures/item/tobacco.png`, `tobacco_leafs.png`, `tobacco_package.png`,
  `tobacco_seeds.png` (plus the dried ones); no pack override exists (`kubejs/assets/the_dirty_stuff/` absent).
- Keys: `i.tobacco`, `i.tobacco_leafs`, `i.dried_tobacco_leafs`, `i.tobacco_package`, `i.tobacco_leafs_package`,
  `i.dried_tobacco_leafs_package`, `i.tobacco_seeds` (item_dict.js 13416-13423).

### Build
1. **Canvas packing:** in a CD `the_dirty_stuff/` server tree (`kubejs/data/creativedelight/kubejs/the_dirty_stuff/recipes.js`,
   new folder, `// requires: the_dirty_stuff`, wrapped in `global.CD.recipes`): per package
   `R.shapeless(i.tobacco_leafs_package, [Item.of(i.tobacco_leafs, 8), i.canvas], e, 'tobacco_leafs_package_canvas')`
   (keep the paper recipe or replace it, Decisions); the mod's unpack recipes stay (canvas lost on unpacking, like
   paper today; or return it, Decisions).
2. **Retexture:** sprites in `kubejs/assets/the_dirty_stuff/textures/item/` with the same file names (resource override;
   models untouched). Bundle look to match the new names (a canvas-wrapped bundle). Check what the resource pack bench
   needs (memory `resource-pack-bench`) only if sprites are large.
3. Changelog bullets (retexture; canvas recipe), crops.md 2.5 tobacco line.

### Ids and data
Verified: the three package recipes and their ingredients, lang keys, texture files. New recipe ids under the CD
namespace NCH writes (`creativedelight:...`).

### Decisions
- Paper or canvas: recommendation canvas replaces paper (`R.remove` the three mod ids, re-add with canvas), so one
  recipe per bundle and the name "bundle" fits.
- Canvas back on unpacking: recommendation no (paper is not returned today; canvas is cheap).
- Which items get new art: recommendation the three bundles and Tobacco (cut tobacco); leaves and seeds look fine.

### Gotchas
- Packing is 8 in, 8 out; if the canvas is returned on unpacking, use NCH's remainder / `replace` argument of
  `shapeless`, not a second output.
- Item stack size is 16 for every Dirty Stuff item (drugs #1 Now); a bundle of 8 still stacks to 16.

### Tests
- SM-new1 (proposed, numbered when the smoking writeup exists): JEI shows each bundle from 8 + canvas, and back to 8; the
  paper recipe is gone if replaced.
- SM-new2 (proposed): the new sprites show in inventory, JEI and dropped.

---

## Built, needs play

None (no Testing or Complete rows in this subfamily).

## Writeup corrections made

None (there is no drugs writeup; nothing read here contradicts the code).
