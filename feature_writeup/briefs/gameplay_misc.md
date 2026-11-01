# gameplay / misc: implementation brief

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# gameplay`, Sub `misc` (row 15); extended 2026-10-31 (brief
sweep) with sections for every open row (2, 9, 14, 16, 17, 18) and the Testing rows listed under "Built, needs play"
(#15's build section cut: [fixes_compat.md](../fixes_compat.md) 2.31 holds it). Layout per [README.md](README.md).
Feature writeups: [fixes_compat.md](../fixes_compat.md) (test prefix `FX-`, last id FX-49 on 2026-10-31) for the small
fixes; [gold_panning.md](../gold_panning.md) (no tests yet; proposed prefix `GP-`); campfire rain is written up in
[feature_porting.md](../feature_porting.md) 2.2 and [wthit_lines.md](../wthit_lines.md) (rain line), memory
`campfire-rain`. New test ids below are placeholders (`FX-newN`, `GP-newN`), numbered when queued.

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #2 | Campfire fuel blocks and rain | In Progress | small: tag entries in `blocktags.js`, one patcherMod mixin (signal smoke), a tooltip; rain audit is reading |
| #17 | Feather fluff | Planned | small: KubeJS recipes and one tag; firework star needs a mixin or is dropped |
| #18 | Dreamcatcher: tooltip, placeable, sleep rewards | Planned | tooltip tiny; placeable and rewards medium (KubeJS block + a wake-up handler) |
| #9 | Rename block | Planned | medium: patcherMod block + menu + screen, model and texture |
| #14 | Wardrobe items on armor stands | Planned | medium: patcherMod common mixin + client render layer |
| #16 | Gold panning | Planned | medium to large: new item, loot tables per biome, an interaction; build with `recipes/metal #6` |
| #1, #5, #6, #8, #10, #11, #12, #13, #15 | see "Built, needs play" | Testing | play only |

Order: #2 and #17 first (data, no new assets); #18 tooltip with #17 (the dreamcatcher recipe takes feathers); then the
patcherMod rows (#9, #14) in one build; #16 last, after its tool art is found (owner 2026-10-30 on `recipes/metal #6`).

---

## misc #2: Campfire fuel blocks and rain
Status **In Progress** / Work patcherMod (+ KubeJS tags, tooltip) / TODO `gameplay/misc #2`

### Goal
A campfire on a "fuel block" never goes out in rain and sends up tall smoke; the fuel tag holds every concentrated
fuel block in the pack; the fuel block says so in its tooltip; nothing else puts such a fire out in rain.

### Now
- **Built and shipped** (changelog 1.1.01 "Campfires"; the TODO's "patcherMod 1.0.9 in `mods/`, untested" is stale:
  `mods/creativeengineering-1.1.04.jar` today). Scratch PASSED 2026-10-22 (SQ-OM-05, SQ-OM-06).
  - [campfire/CampfireRain.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/campfire/CampfireRain.java)
    (56 lines): lit campfire in `#creativeengineering:campfire/rain_douses`, `isRainingAt(pos.above())`, block below
    not in `#creativeengineering:campfire/rain_proof_fuel` -> out after `[campfire] rainOutTicks` (160; CEConfig.java
    1001-1024, also `rainDouses`). Driven by `mixin/CampfireRainMixin` (RETURN of `cookTick`).
  - Tags filled in [blocktags.js](../../kubejs/server_scripts/tags/blocktags.js) 69-73: `rain_douses` =
    `b.campfire`; `rain_proof_fuel` = `b.coal_block`, `b.quark_charcoal_block`, `b.architects_palette_charcoal_block`
    (block_dict.js 44-47, 161-162). Live dump agrees (3 members).
  - WTHIT rain line "Rain: Safe (fuel block below)" ([compat/WthitCampfire.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/WthitCampfire.java),
    wthit_lines.md 2.6); JEI Campfire Rules tab reads the same tags (jei_world_tabs.md).
  - Unlit Campfire: `campfireLitTime 0` (burns forever, its own rain check off), `campfireRainUnlitTime = -1`
    (defaultconfigs/unlitcampfire-server.toml 8).
- **Rain audit (read 2026-10-31):** the only rain douses left are `CampfireRain` and Vinery's stackable log
  (`StackableLogBlockEntity` in the Vinery port calls `isRainingAt`; its tooltip, tooltips.js 647, says "rain can put
  it out"). Vanilla fire blocks: rain never removes fire on an infiniburn block (`#minecraft:infiniburn_overworld`:
  magma, netherrack, Quark blaze lantern and charcoal block, SecurityCraft netherrack). Soul campfires and Tinted
  Campfires are never doused.
- **Signal smoke** is vanilla's `CampfireBlock` `SIGNAL_FIRE` state, set when the block below is a hay bale
  (`isSmokeSource`, hard-coded `Blocks.HAY_BLOCK` in 1.19.2; SRG unverified). tooltips.js 437-438 already says a
  campfire on a hay bale boosts elytra users (source mod unverified).
- No fuel-block tooltip exists.

### Build
1. **Fuel tag** (blocktags.js 73, `global.tags` via the `t.add` already there): add `#forge:storage_blocks/coal_coke`
   (`immersiveengineering:coke`), `#forge:storage_blocks/petcoke` (`immersivepetroleum:petcoke_block`),
   `b.botania_blaze_block` (block_dict 76), `b.magma_block` (21), and the bales the row names: hay
   (`minecraft:hay_block`), straw (`farmersdelight:straw_bale`), rice (`farmersdelight:rice_bale`), oat
   (`vintagedelight:oat_bale`), stick bundle (`quark:stick_block`), plus `minecraft:dried_kelp_block` (a furnace fuel
   block). New block_dict keys for the ones without one (item_dict has `i.hay_bale` 1738, `i.straw_bale` 167,
   `i.rice_bale` 166, `i.oat_bale` 14462, `i.stick_block` 3544, `i.petcoke_block` 4266, `i.coke` 1595,
   `i.dried_kelp_block` 2330). Prefer the forge storage tags over single ids so new coal-like blocks join on their own.
2. **Signal smoke** (patcherMod, common): `mixin/CampfireSignalFuelMixin` on `CampfireBlock#isSmokeSource`
   (private, RETURN: `|| state.is(CampfireRain.RAIN_PROOF_FUEL)`; read the SRG name from the mapped Forge jar first).
   One tag drives both rules, as the row asks ("longer smoke over them"). `SIGNAL_FIRE` is set on placement and on a
   neighbour update below, so existing campfires update when their fuel block is replaced.
3. **Tooltip** ([client_scripts/tooltips.js](../../kubejs/client_scripts/tooltips.js) `addTooltips`, the Decor block
   near 437): one line on every fuel block item, e.g. "Keeps a campfire on top lit in rain", ids through `i.` keys.
   Tooltips are client side and cannot read the tag, so list the same ids as step 1 (or use a tag-driven tooltip if
   tooltips.js has one).
4. **Stackable log**: decide (below); if it should follow the campfire rule, a mixin in the Vinery port's
   `StackableLogBlockEntity` (Creative Brewing member `vinery`) that skips its rain check when the block below is in
   the fuel tag; fix the tooltip text either way.
5. Docs: a "Campfires" paragraph in fixes_compat.md (or extend feature_porting.md 2.2) with the tag list; wthit_lines.md
   rain line text stays.

### Ids and data
Verified (dict / live tag dump 2026-10-30): coal block, Quark and Architect's Palette charcoal blocks, IE `coke`,
IP `petcoke_block`, Botania `blaze_block`, magma, hay, FD straw and rice bales, Vintage `oat_bale`, Quark
`stick_block`, dried kelp block. Unverified: the `isSmokeSource` SRG name; which mod adds the hay-bale elytra boost.

### Decisions
- **Which blocks** (TODO decision). Recommendation: the list in Build 1. Hay, straw, rice and oat bales are weak fuel
  but the row names them (and hay is vanilla's signal block); magma and blaze block are heat sources.
- **Signal smoke on every fuel block or only bales.** Recommendation: every fuel block (one tag, one rule). Gotcha: if
  the elytra boost keys on signal smoke, coal blocks start boosting too; check the boost mod first.
- **Stackable log in rain.** Recommendation: same rule as the campfire (fuel block below = safe), so the fuel-block
  tooltip is true for every fire the pack has.

### Gotchas
- Changing a block's `SIGNAL_FIRE` source changes chunk-saved state only on the next neighbour update; old campfires
  on coal blocks keep short smoke until touched.
- Tags are server data (`/reload`); the mixin needs a patcherMod build and restart; the tooltip needs a client reload.

### Tests (FX-)
- SQ-OM-05 / SQ-OM-06 cover the built part. **FX-new1** fuel blocks: a campfire on each new fuel block in rain stays
  lit 30 s; one on stone goes out in about 8 s. **FX-new2** signal smoke: a campfire on a coal block and on a straw bale
  shows tall smoke. **FX-new3** tooltip: hover a straw bale and a Block of Coal Coke -> the fuel line; stone has none.

---

## misc #9: Rename block
Status **Planned** / Work new jar / patcherMod / TODO `gameplay/misc #9`

### Goal
A cheap block, available before the anvil, that only renames items: costs no levels and never wears out.

### Now
- Renaming is anvil-only today. No mod in `mods/` adds a renaming station (checked jar names 2026-10-31: no anvil,
  rename or name-tag mods). Source of the row: scratchpad 2026-10-21 (the text is only in the TODO now).
- patcherMod already registers blocks with menus: [oven/OvenRegistry.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/oven/OvenRegistry.java)
  and `oven/OvenMenu.java` are the pattern (block, block item, menu type, client screen).

### Build
1. Search for a 1.19.2 Forge "renaming table" mod first (licence, sides); if a GPL / MIT one is small, port it per
   the `port` skill; else own code (recommended below).
2. Own: `rename/RenameRegistry.java` (block `creativeengineering:renaming_table`, item, `MenuType`),
   `rename/RenameMenu extends AnvilMenu` overriding `createResult` to copy the input and set only the custom name
   (no repair, no enchant combine, second slot hidden), `cost` always 0, and `mayPickup` true at cost 0 (vanilla
   requires `cost > 0`). The block never changes state (no chipping like `AnvilBlock.damage`).
3. Client: `RenameScreen extends AnvilScreen` without the cost line, registered in the client setup.
4. Assets: blockstate, model, texture (a small lectern-like table), lang "Renaming Table". Recipe via `global.NCH.shaped`
   in a server script (planks, paper, a feather or ink; no iron so it comes before the anvil), ids through dict keys.
5. Pack data: BlockRunner tier `functional` is unfilled, so leave it untiered; WTHIT icon; stack size 64; JEI info.

### Decisions
- **Source (TODO decision).** Recommendation: own patcherMod block; the menu is about 80 lines on top of `AnvilMenu`.
- **"Unbreakable".** Read as "never wears out like an anvil" (normal hardness, mineable). The other reading
  (bedrock hardness) would make it unmovable; recommend not.
- **Name length / formatting.** Same 50-character limit as the anvil; no colour codes.

### Gotchas
- `AnvilMenu` also checks `player.getAbilities().instabuild` for "Too Expensive"; cost 0 sidesteps it.
- Creative Labels and Storage Labels read item names; no change needed.

### Tests (FX-)
- **FX-new4**: craft the table without iron; rename a sword with 0 levels; the level counter is unchanged; no second
  slot; the table never chips after 50 uses.

---

## misc #14: Wardrobe items on armor stands
Status **Planned** / Work patcherMod (common mixin + client layer) / TODO `gameplay/misc #14`

### Goal
Every Wardrobe item can be put on an armor stand and shows there as it does on a player, including the ones that only
fit Curios slots (a scarf shown on the head, an apron on the body).

### Now
- Wardrobe 1.0.3 (`mods/wardrobe-1.0.3-1.19.2.jar`, MCreator, package `net.mcreator.warbrode`, needs Curios). Its
  armour pieces (`*_helmet`, `*_chestplate`, `*_leggings`, `*_boots`, under armour) are `ArmorItem`s and already go on
  stands. The Curios-only items (verified from the jar's `data/curios/tags/items`):
  - head (21): 16 `wardrobe:<colour>_scarf`, `breathing_reed`, `book_hat`, `book_hat_2`, `book_hat_3`;
  - body (7): `leather_apron`, `hardened_leather_arpon` (sic), `chiton`, `brewing_apron`, `fishing_apron`,
    `lantern_on_a_stick`, `soul_lantern_on_a_stick`;
  - back (3): `backpack`, `large_backpack`, `travel_backpack`.
- Each is an `Item implements ICurioItem`; each has its own `ICurioRenderer` (`client/renderer/<Name>Renderer`).
  Curios 5.1.6.4 exposes `CuriosRendererRegistry.getRenderer(Item)` and `new SlotContext(String id, LivingEntity,
  int index, boolean cosmetic, boolean visible)` (verified with `javap`).
- Armor stands take items through `ArmorStand#interactAt`, which picks the slot with `Mob.getEquipmentSlotForItem`;
  a plain item goes to the main hand. Nothing in patcherMod or KubeJS touches armor stands or Wardrobe today
  (58 `wardrobe:` keys in item_dict.js, e.g. `i.black_scarf` 14899, `i.leather_apron` 14887, `i.wardrobe_backpack` 14893).

### Build
1. Tags (server, `global.tags`): `#creativeengineering:stand_wear/head` and `/chest` filled from the lists above via dict
   keys (head: scarves, reed, book hats; chest: aprons, chiton, lanterns, backpacks; see Decisions).
2. Common mixin `ArmorStandWearMixin` on `ArmorStand#interactAt`: redirect the `getEquipmentSlotForItem` call; items in
   the tags return HEAD / CHEST. Server and client both run it (the client predicts the swap).
3. Client layer `client/StandCurioLayer` added to the armor stand renderer (`EntityRenderersEvent.AddLayers`): for HEAD
   and CHEST, if `CuriosRendererRegistry.getRenderer(item)` is present, call `render(stack, new SlotContext(slotId,
   stand, 0, false, true), pose, parent, buffers, light, ...)`. Cancel the vanilla `CustomHeadLayer` for head items
   in the tag (a mixin or `RenderLivingEvent` check), or the flat item model draws on top.
4. WTHIT / tooltips: none needed. Docs: a fixes_compat.md section.

### Decisions
- **Which slot each Curios-only item shows in (TODO decision).** Recommendation: head for the head tag; chest for
  body and back (an armor stand has no back slot; one chest item at a time).
- **Other Curios wearables** (Simple Hats, Curios rings). Recommendation: Wardrobe only, as the row says; the tag
  makes adding more a data change.

### Gotchas
- MCreator renderers often call `ICurioRenderer.followBodyRotations` / cast the parent model to `HumanoidModel`;
  `ArmorStandArmorModel` is a `HumanoidModel`, but check one renderer's bytecode for an entity-type cast before relying on it.
- Backpacks: the stand only shows them; their inventory is not opened from a stand.

### Tests (FX-)
- **FX-new5**: right-click a stand with a red scarf -> it shows on the stand's head as on a player; with a fishing
  apron -> on the body; a backpack -> on the body; take each back with an empty hand. A diamond helmet still goes to
  the head as before.

---

## misc #16: Gold panning
Status **Planned** / Work KubeJS / data (+ assets) / TODO `gameplay/misc #16` (build with `recipes/metal #6`)

### Goal
A pan: fill it with dirt (or gravel, sand), work it in water, get gold nuggets, redstone, flint and a small diamond
chance, from tables that differ by biome and lean hard on Luck.

### Now
- [gold_panning.md](../gold_panning.md) holds only the owner idea (10 lines, "Not started"). `recipes/metal #6`
  ([recipes_metal.md](recipes_metal.md) "metal #6") recommends Create washing of gravel / sand with a gold chance and
  leaves a hand pan open; owner 2026-10-30 there: find a gold pan, plus a sluice model and texture.
- No pan, sluice or sieve item exists (no `*_pan` other than `creativeengineering:cake_pan`, no sifting mod).
- Existing gold-from-washing: `create:splashing/red_sand` (gold nuggets 12.5 %), `splashing/soul_sand` (2 %).
- Loot tables honour Luck through `bonus_rolls` and per-entry `quality`; the pack's loot scripts live in
  `kubejs/server_scripts/loot/` (LootJS layer).

### Build
1. **Art first** (owner's step): a pan item texture (empty and filled), later a sluice block model.
2. **Items** (startup, KubeJS item registry in the CE startup items with dict keys): `gold_pan` (empty, stack 1,
   durability ~128) and `gold_pan_<load>` filled variants for dirt / gravel / sand (or one item with an NBT load),
   with a use animation (`bow`) and `useDuration` ~40 ticks.
3. **Fill**: right-click a dirt / gravel / sand block with an empty pan -> the block is removed, the pan becomes
   filled (`BlockEvents.rightClicked`).
4. **Work**: use the filled pan while the player stands in water (`isInWater`, or the targeted block is a water
   source); on finish roll the table for the player's biome and give the results, return the empty pan, damage 1.
   Rolling a loot table from KubeJS needs `LootContext` (Java classes; check the KubeJS class filter). If blocked, a
   10-line patcherMod helper `loot/PanLoot.roll(player, tableId)` exposed to scripts.
5. **Tables**: `kubejs/data/creativeengineering/loot_tables/gameplay/panning/<group>.json` per biome group (river,
   badlands / mesa, desert, mountain stream, swamp, default) by the load: nuggets, redstone, flint, low diamond, with
   `quality` on the rare entries and `bonus_rolls` 0.5 per Luck. Generate them from one table in a `tools/` script if
   the groups multiply.
6. **Sluice** (later, after #6's decision): a Create-driven block or just Create washing (recipes_metal.md Build 1).
7. Docs: gold_panning.md becomes the writeup (tables, ratios, tests GP-).

### Ids and data
Dict keys exist: `i.dirt` 680, `i.gravel` 1280, `i.nugget_gold` 880, `i.redstone` 1368, `i.flint` 1332,
`i.diamond` 1339. New: pan items, the loot tables. Biome groups via existing biome tags (`#minecraft:is_river`,
`#minecraft:is_badlands`, `#forge:is_desert`; verify against the biome dump).

### Decisions
- **Interaction (TODO decision).** Recommendation: fill on a block, then hold use in water (above). Alternatives: pan in
  hand + dirt in the off hand on water (one step, less clear in JEI).
- **Tables (TODO decision).** Recommendation: 5-6 biome groups, badlands best for gold, river best for flint and
  redstone, diamond about 0.5 % with quality so Luck III roughly triples it.
- **Tool (TODO decision).** Recommendation: a new pack item with found art (owner's step); not a reused cake pan.
- **One row or two.** Recommendation: build `recipes/metal #6`'s Create washing first (one `/reload`), then this row;
  close #6 when both ship.

### Gotchas
- Gold already comes cheap from Create (red sand); keep panning yields below it per minute.
- Filled pans must not stack, or the fill step dupes blocks.

### Tests (GP-)
- **GP-new1**: fill a pan on dirt (the block is gone), use it in a river -> nuggets / flint / redstone; on land
  nothing happens. **GP-new2**: 50 pans in badlands vs plains -> more gold in badlands. **GP-new3**: with Luck (potion)
  -> visibly more rare drops.

---

## misc #17: Feather fluff
Status **Planned** / Work KubeJS / data / TODO `gameplay/misc #17`

### Goal
Feather fluff (Create Recycle's crushed feather) is the efficient cushion filling, and counts as a feather in the
pack's feather-using crafts (Wares delivery table, feather on a stick, Wardrobe, the string in the fishing apron and
in quilted wool, firework star). The dreamcatcher takes feathers too.

### Now
- `i.feather_fluff = 'create_crush_everything:feather_fluff'` (item_dict.js 5393), from Create Recycle 1.0.2
  (verified in the jar): crushing 1 feather -> **4 fluff**; scrap arrows -> 25 % fluff; `feather_from_fluff` 4 fluff ->
  1 feather (shapeless). Hearth and Harvest's chicken plucking (hearth_and_harvest.md, HNH-13) is a feather source.
- `#forge:feathers` = feather, duck feather (live tag dump); fluff is not in it. No script mentions fluff.
- The recipes (verified in the jars): Handcrafted cushions 6 wool + 3 **feather item** -> 8 (16 colours,
  `handcrafted:<colour>_cushion`); Wares `delivery_table` uses `#forge:feathers`; Domestication Innovation
  `feather_on_a_stick` uses `#forge:feathers`; Wardrobe `fletchers_hat_helmet` uses the feather item; Wardrobe
  `fishing_apron` uses 1 string; dbExtended `rec_<colour>_quilted_wool` uses 2 wool + 2 string; Dreamcatcher `dream`
  is sticks, string, lapis (no feather); the firework star is vanilla's special recipe with a hard-coded feather
  (burst shape). IE's `cushion` is hemp fabric only.

### Build
1. Cushions: for each Handcrafted colour, `global.NCH.shaped` 6 wool + 3 fluff -> 8 (same as feathers, so fluff is 4x
   as efficient per feather crushed); leave the feather recipe.
2. Feather crafts: `global.NCH.replaceInput` on `wares:delivery_table`, `domesticationinnovation:feather_on_a_stick`,
   `wardrobe:fletchers_hat_recipe` swapping the feather for a pack tag `#creativeengineering:feather_like` (feather,
   duck feather, fluff) built with `global.tags`; do **not** add fluff to `#forge:feathers` (arrows and every other tag
   user would accept it, see Decisions).
3. String uses: `replaceInput` string -> `#creativeengineering:fluff_or_string` in `wardrobe:fishing_apron_recipe` and
   the 16 `davebuildingmod:rec_<colour>_quilted_wool` recipes.
4. Dreamcatcher: replace `dreamcatcher:dream` with an `NCH.shaped` that adds feathers (`#creativeengineering:feather_like`)
   to the string-and-stick ring (shared with #18).
5. Firework star: vanilla's `FireworkStarRecipe` maps items to shapes in a static map; a patcherMod mixin adding fluff
   as a burst shape, or drop this part (see Decisions).
6. Recipe ids under the pack namespace through NCH; item ids through dict keys (`i.feather_on_a_stick` 1672 and
   `i.feather_fluff` exist; add Handcrafted cushion keys as needed).

### Decisions
- **How many fluff per feather (TODO decision).** Recommendation: 1 fluff stands in for 1 feather only in cosmetic
  crafts (cushions, quilted wool, apron, hat, delivery table, feather on a stick, dreamcatcher), never in arrows or
  other tag-wide uses; with crushing at 4 fluff per feather that is the "efficient" part. If that is too generous,
  require 2 fluff (shapeless variants) instead.
- **Firework star.** Recommendation: skip unless the user insists; it needs a mixin for one cosmetic shape.

### Gotchas
- `replaceInput` with a tag ingredient keeps the recipe id; check JEI after `/reload` that the old feather still works.
- Create Recycle's `feather_from_fluff` (4 -> 1) stays, so the loop feather -> 4 fluff -> 1 feather is neutral.

### Tests (FX-)
- **FX-new6**: crush a feather -> 4 fluff; 6 wool + 3 fluff -> 8 cushions; fluff crafts the fishing apron, quilted
  wool, a delivery table and a feather on a stick; an arrow cannot be made from fluff.

---

## misc #18: Dreamcatcher: tooltip, placeable, sleep rewards
Status **Planned** / Work KubeJS / patcherMod / TODO `gameplay/misc #18`

### Goal
The dreamcatcher explains itself, can hang on a wall, and sleeping near one (not only holding it) gives the reward.

### Now
- `mods/dreamcatcher mod.jar` (Dreamcatcher 1.0.0, MCreator, AFL 3.0, both sides, mod_sides.md 130): one item
  `dreamcatcher:dreamcatcher` (plain `Item`, stack 1 per stack_sizes.md 1486), recipe `dreamcatcher:dream` (sticks,
  string, lapis), an advancement (dropped by the pack). Its only effect (verified, `SleepProcedure`): on
  `PlayerWakeUpEvent`, if the player holds it in the **main hand**, `giveExperienceLevels(5)`. No tooltip, no dict
  key, no pack script mentions it.

### Build
1. Dict key `i.dreamcatcher` (item_dict.js). Tooltip in tooltips.js: "Hold it while you sleep: +5 levels on waking"
   (or the new rule after step 3).
2. Placeable: a KubeJS block `creativeengineering:dreamcatcher` (wall-hanging, no collision, thin model from the item
   texture), placed by right-clicking a wall with the item (`BlockEvents.rightClicked`, consume the item), dropping
   the item when broken. Or skip (Decisions).
3. Sleep rewards: a wake-up handler (KubeJS startup `ForgeEvents.onEvent('net.minecraftforge.event.entity.player.PlayerWakeUpEvent', ...)`
   or patcherMod) that checks a placed dreamcatcher within ~8 blocks of the bed and gives the reward; cancel the mod's
   own hand rule only if the reward changes (a patcherMod mixin on `SleepProcedure.execute`, `remap = false`).
4. The recipe with feathers goes in with #17 step 4.

### Decisions
- **Placeable or not (TODO decision).** Recommendation: yes, a wall block; "near" a bed only makes sense for a placed one.
- **Which rewards (TODO decision).** Recommendation: keep it modest: +5 levels per sleep is large; replace it with a
  short buff on waking (e.g. Regeneration I 30 s, or Luck 5 min) for a placed dreamcatcher near the bed, and drop the
  hand rule. No advancements (the pack drops them).

### Gotchas
- `PlayerWakeUpEvent` also fires when a player leaves the bed without sleeping (`wakeImmediately`); check the
  level's day changed (or `!updateLevel`) before rewarding.
- A KubeJS block is a startup change: restart.

### Tests (FX-)
- **FX-new7**: hover the dreamcatcher -> the tooltip; hang one on a wall next to a bed, sleep through the night -> the
  reward once; lie down and get up at once -> nothing.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| #1 | Advancements off | Testing | AV-01 to AV-05 in game; writeup [advancements.md](../advancements.md) |
| #5 | Immersive Paintings: every variant on the Datapacks page | Testing | 475 paintings at 32 px shipped in 1.0.6; PA-01 to PA-07 (painting screen) in game; [immersive_paintings.md](../immersive_paintings.md) |
| #6 | Grass shearing and spreading | Testing | Built 2026-10-28 (IW grass, mycelium, podzol, rooted grass; Vintage lush grass, Vinery grass slab; WTHIT "Sheared" line); client 1.1.04, server still 1.1.03. SQ-SG-01 to -03 queued, not run; WL-34 to WL-36 in game; [wthit_lines.md](../wthit_lines.md) 2.10 |
| #8 | Every chalk symbol unlocked | Testing | Config, shipped 1.1.01; scratch PASSED (SQ-GM-06); FX-27 in game; [fixes_compat.md](../fixes_compat.md) 2.27 |
| #10 | Remove the Upgrade Aquatic bedroll | Testing | Built 2026-10-23, shipped 1.1.03 (17 dict keys + `removedItems`, recipes in `misc_mods.js`, trade off in config); N-03 in [fixes_compat.md](../fixes_compat.md) is the check |
| #11 | Hot air balloon wind particle frequency | Testing | Breezy client config x3, shipped 1.1.01; FX-26 (client) |
| #12 | Undo vein mine command | Testing | Shipped 1.1.01; scratch PASSED (SQ-GM-05, SQ-GM-08); FX-29 to FX-33 in game; [fixes_compat.md](../fixes_compat.md) 2.26 |
| #13 | Biome-tinted moss | Testing | Shipped 1.1.03, client only; FX-43 to FX-45 in game; [fixes_compat.md](../fixes_compat.md) 2.30 |
| #15 | Immersive Weathering on Upgrade Aquatic coral | Testing | Built 2026-10-28 (`tools/gen_ua_coral_growths.py`, 11 growth files, warm ocean / every ocean for prismarine); in the 1.1.04 client payload, server still 1.1.03. SQ-UC-01 to -03 queued, not run; FX-48 in game; [fixes_compat.md](../fixes_compat.md) 2.31 |
