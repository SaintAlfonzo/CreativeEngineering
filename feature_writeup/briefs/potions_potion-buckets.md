# Brief: potions / potion-buckets

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# potions`, Sub `potion-buckets` (rows 2 and 3 open, row 1 Testing).
Feature writeup: [potion_buckets.md](../potion_buckets.md) (test prefix `PB-`, last PB-20). Code:
[startup_scripts/items/potion_buckets.js](../../kubejs/startup_scripts/items/potion_buckets.js) (items, bottle
remainder), [startup_scripts/func/potion_area.js](../../kubejs/startup_scripts/func/potion_area.js) (clouds,
dispenser handler), [server_scripts/engineering/potion_buckets.js](../../kubejs/data/creativeengineering/kubejs/engineering/potion_buckets.js)
(right click), [server_scripts/engineering/potion_bottling.js](../../kubejs/data/creativeengineering/kubejs/engineering/potion_bottling.js)
(bucket and bottle recipes), [server_scripts/equipment/tipped_arrows.js](../../kubejs/server_scripts/equipment/tipped_arrows.js),
patcherMod `CE/potion/PotionBucketDispenser.java` (`CE/` = `development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/`).

Jar facts checked 2026-10-21: KubeJS 1902.6.2 build 73 (`RecipeJS.replaceIngredient`, `IngredientActionFilter`,
`ReplaceAction`); recipe scan of the 132 mod jars cached in the scratchpad (not all 407). Vanilla behaviour
(`ItemStack.getItem()` on an empty stack, dispensed potions' owner) is from memory and marked so.

## Scope and order

| Step | Rows | What | Work |
|---|---|---|---|
| 1 | #3 (F5 part) | Skip no-effect potions when generating bucket and arrow recipes, and stop no-effect buckets falling back to Night Vision | KubeJS |
| 2 | #2 | Decide keep / scope the global glass-bottle remainder; scoping is a recipe change in `potion_bottling.js` | KubeJS |
| 3 | #3 (F6 part) | Dispensed-cloud owner: recommend close as vanilla parity | none |

Two bugs found in row 1's code ride along with step 1 (same files): the survival cooldown that never applies, and the
awkward / thick / mundane bucket that gives Night Vision. See "Built, needs play".

Related rows:
- `tooling/conventions #1`: the bucket / arrow machine recipes are raw `e.custom` (F4). `NCH.createFilling` and
  `NCH.createEmptying` (`neoCraftHelper.js` lines 1429, 1449) cover the Create ones; `NCH.ieBottling` (line 1768)
  first needs NBT-aware results (`_nbtItemJson`, line 511) because `_itemJson` drops `nbt`. Do that row's move in the
  same edit as step 1 if both are scheduled, since step 1 rewrites the same loops.
- `potions/potions #6`: instant effects (Saturation, Healing, Harming) re-dose per ring and per reapplication in bucket
  clouds; a stronger Saturation potion makes a Saturation bucket a feeding station.

---

## potion-buckets #3: Small findings
Status **Planned** · Work: KubeJS · id `potions/potion-buckets #3`

### Goal
F5: stop generating recipes for potions no machine can hold or that do nothing. F6: decide about owner-less dispensed
clouds.

### Now
- `potion_bottling.js` lines 103-122 loop every key of `Registry.POTION` except `minecraft:empty`; per potion: 3 IE
  bottling recipes (lines 110-119) and, via `bucketRecipes()` (lines 38-101), per bucket type a grid fill, grid drain,
  Create filling, IE bottling and Create emptying (15). `tipped_arrows.js` lines 23-50 make an IE bottling and a Spout
  recipe per potion.
- `minecraft:water`: Create stores water bottles as plain water, so `create:potion{Potion:"minecraft:water"}` never
  exists; its machine recipes never run. Grid fill with 4 water bottles does work and makes a Water Potion Bucket.
- `minecraft:awkward`, `thick`, `mundane` exist as `create:potion` but carry no effects; their tipped arrows are
  vanilla's "Uncraftable Tipped Arrow" (`tipped_arrow.effect.awkward` etc. have no lang).
- **Bug (from code, not seen in game):** `global.potionArea.spawn` (`potion_area.js` lines 150-159) falls back to
  `DEFAULT_EFFECTS` (Night Vision 8 s, line 52) whenever `PotionUtils.getMobEffects(stack)` is empty. A bucket filled
  with awkward, thick, mundane or water potion has `Potion` NBT but no effects, so it bursts a Night Vision cloud.
  So 4 awkward potions (water + nether wart) + a bucket, through the PB-11 grid route, make a cheap Night Vision
  bucket.
- F6: the dispenser path calls `global.potionArea.spawn(..., null)` (`potion_area.js` line 124), so dispensed clouds
  have no owner. From memory, vanilla dispensed splash / lingering potions also have no owner
  (`ThrownPotion` built without one in the dispenser's projectile behaviour), so this matches vanilla.

### Build
1. One shared skip test, used by both loops: skip a potion when it has no effects.
   ```js
   // potion_bottling.js / tipped_arrows.js, inside the while loop
   var potionObj = POTIONS.get(new ResourceLocation(pid))      // load ResourceLocation once at top
   if (pid === 'minecraft:empty' || potionObj.getEffects().isEmpty()) continue
   ```
   Keep the plain-bottle IE bottling recipes (lines 110-119) for awkward / thick / mundane, since those fluids exist
   and players bottle brewing bases; skip only the bucket and arrow recipes for them, and skip water everywhere. So:
   in `potion_bottling.js` skip `bucketRecipes()` for no-effect potions and skip the whole iteration for
   `minecraft:water`; in `tipped_arrows.js` skip no-effect potions. Rhino note: `getEffects()` on `Potion` is a single
   no-arg method, so it should resolve; if Rhino objects, fall back to
   `potionObj.getEffects().size() === 0`.
2. `potion_area.js` `spawn()`: fall back to `DEFAULT_EFFECTS` only when the stack has no `Potion` and no
   `CustomPotionEffects` tag (a truly blank `/give` bucket). For a potion with no effects, return `[]` without
   spawning (or spawn a cosmetic cloud with no effects; recommend nothing, plus the empty-bucket sound so the click
   isn't silent). Read the tag with `stack.getOrCreateTag().contains('Potion')` (the file already uses
   `getOrCreateTag()` because `getTag()` is invisible to Rhino).
3. Right-click handler (`server_scripts/engineering/potion_buckets.js` lines 42-49): only consume the bucket if
   `spawn()` returned rings. Same fix point as the cooldown bug: take `const bucketItem = stack.getItem()` **before**
   `stack.shrink(1)` and pass it to `addCooldown` (line 49).
4. Log the new counts (`potion bottling done (N recipes)`, `tipped arrow tipping done`) and update potion_buckets.md
   2.3 ("15 per potion"), F5, section 6.
5. F6: no code. Write "matches vanilla dispensed potions" in potion_buckets.md F6 and close. If you want credit
   anyway: Forge `FakePlayerFactory.getMinecraft(level)` as owner in `PotionBucketDispenser.BucketBehavior.execute`
   (change `SpawnHandler.spawn` to pass an owner); not recommended (fake-player kills drop player loot / XP).

### Ids and data
- Potions to skip for buckets / arrows: `minecraft:water`, `minecraft:awkward`, `minecraft:thick`,
  `minecraft:mundane`, plus any modded potion with no effects (the `getEffects().isEmpty()` test catches all).
- Recipe id prefixes affected: `creativeengineering:shapeless/potion_bucket_fill/<potion>_<type>`,
  `.../potion_bucket_drain/...`, `creativeengineering:create_filling/potion_bucket/...`,
  `creativeengineering:bottling/potion_bucket/...`, `creativeengineering:create_emptying/potion_bucket/...`,
  `creativeengineering:bottling/tipped_arrow/<potion>`, `creativeengineering:createfilling/tipped_arrow/<potion>`.

### Decisions
- **Keep bottling recipes for brewing bases?** Recommend yes (awkward / thick / mundane bottles are useful); only
  water goes, since Create never holds it as `create:potion`.
- **F6 owner.** Recommend closing as vanilla parity.

### Gotchas
- Existing worlds may hold awkward / thick / mundane / water potion buckets; after step 2 they do nothing on click
  (and with step 3 aren't consumed). Their drain recipes also vanish, so they can't be emptied: keep the grid drain
  recipe for no-effect potions if that matters (cheap, 3 recipes per base).
- Server scripts share one top-level scope (`potion_buckets.js` lines 7-12): `POTIONS`, `n`, `it`, `BUCKETS` are
  already top-level `var`s in `potion_bottling.js` and `POTIONS`, `n`, `it` in `tipped_arrows.js`; they work because
  each file wraps them in its own `ServerEvents.recipes` callback. Keep new names inside the callbacks.

### Tests
- **PB-21 No noise:** JEI → Arrow of Awkward / Water Potion Bucket → no recipe; Potion of Swiftness bucket recipes
  still there; the load log counts drop.
- **PB-22 No-effect bucket:** `/give @s creativeengineering:potion_bucket{Potion:"minecraft:awkward"}`, right-click a
  block → nothing happens, bucket kept; a bucket with no NBT still bursts Night Vision.
- **PB-23 Survival cooldown:** survival, two Swiftness buckets in the hotbar, burst one and switch to the other within
  a second → the second is on cooldown (white sweep on the slot).

---

## potion-buckets #2: Global glass-bottle remainder
Status **Planned** · Work: KubeJS · id `potions/potion-buckets #2`

### Goal
Decide whether every crafting-grid recipe that uses up a potion bottle hands the glass bottle back (today), or only
the pack's own bucket-fill recipes do.

### Now
- `startup_scripts/items/potion_buckets.js` lines 77-86: `ItemEvents.modification` sets
  `setCraftingRemainder(glass_bottle)` on `minecraft:potion`, `splash_potion`, `lingering_potion`. Global: it changes
  `Item.getCraftingRemainingItem`, which every crafting grid, Create's basin auto-shapeless and Farmer's Delight's
  cooking pot (`ejectIngredientRemainder`) honour. The brewing stand only checks the ingredient slot, so potions
  brewed are unaffected.
- Who is affected (grid recipes consuming a potion item):
  - Vanilla `crafting_special_tippedarrow` (8 arrows + lingering potion): now returns the bottle.
  - The pack's bucket fill (`potion_bottling.js` lines 54-60), the reason it exists.
  - The pack's cinder-flour dust recipes (`server_scripts/engineering/dust.js` lines 24-84) already return the bottle
    per recipe through `NCH.shapeless`'s `replace` argument (`[i.potion, Item.of(i.glass_bottle)]`), so they don't
    need the global remainder.
  - The thunder detector (`server_scripts/engineering/redstone.js` lines 219-223, 3 mundane potions): returns 3 bottles.
    (That file also redefines `i.mundane_potion` at line 9, a duplicate of `item_dict.js` line 1363; CLAUDE.md
    one-definition rule.)
  - Archery dousing arrow (`archery.js` lines 255-257) uses a water bottle in a fletching table / IE blueprint;
    whether those honour remainders is mod-specific (unverified).
  - Mod jars (scan of the 132 cached jars, `data/*/recipes`, crafting types only): PneumaticCraft `jet_boots_upgrade_4`,
    `jumping_upgrade_3`, `jumping_upgrade_4`, `night_vision_upgrade`. The other ~275 jars are unscanned.
- **KubeJS fact (verified, decompiled):** `RecipeJS.replaceIngredient(IngredientActionFilter, ItemStack)` adds a
  `ReplaceAction` ingredient action: when the matching slot is used up, it leaves the given item. That **is** a
  per-recipe remainder. The comment in `NCH.shapeless` (`neoCraftHelper.js` lines 742-751) says the opposite; it is
  wrong for this KubeJS build (and `dust.js` relies on the real behaviour). Filter forms: a number (slot index), an
  item id / Ingredient, or `{item, index}`.

### Build
- **Keep (recommended):** no code. Update potion_buckets.md F3 to "kept", and (optional) drop the now-redundant
  `replace` arguments in `dust.js`. Fix the misleading NCH comment (lines 742-751) to describe ingredient actions
  correctly.
- **Scope:** 
  1. Delete the `ItemEvents.modification` block (`potion_buckets.js` lines 68-86) and its references in comments
     (`potion_bottling.js` lines 49-51 says "via the containerItem set on minecraft:potion ... below"; stale either way,
     it's `setCraftingRemainder` in a startup script).
  2. In `bucketRecipes()`, pass the `replace` argument to the fill recipe:
     ```js
     R.shapeless(Item.of(bucketItem, bucketNbt),
         ['minecraft:bucket', bottleIngredient, bottleIngredient, bottleIngredient, bottleIngredient],
         e, 'potion_bucket_fill/' + idSuffix, null, [bottleItem, Item.of(i.glass_bottle)])
     ```
     (`bottleItem` is the plain item id, so the filter matches any bottle of that tier.)
  3. Keep `dust.js` as is. If the thunder detector should still return its bottles: `NCH.shaped` (line 698) has no
     `replace` parameter, so extend it with the same handling as `shapeless` (lines 752-757) and pass
     `[i.potion, Item.of(i.glass_bottle)]`.
  4. Update potion_buckets.md 3 and F3, PB-17.

### Ids and data
`minecraft:potion` (`i.potion`), `minecraft:splash_potion` (`i.splash_potion`), `minecraft:lingering_potion`
(`i.lingering_potion`), `minecraft:glass_bottle` (`i.glass_bottle`).

### Decisions
- **Keep or scope.** Recommend **keep**: it makes every grid behave like Farmer's Delight's stations
  ([remainder_buffer.md](../remainder_buffer.md)), nothing found depends on the bottle being destroyed, and the bottle
  is worth less than the code to scope it. Scope only if a mod recipe turns up where the returned bottle breaks
  something (e.g. a recipe whose output is itself a bottle, giving a duplicate).

### Gotchas
- With the global remainder, a stack of potions in a Create basin auto-crafting leaves glass bottles in the basin
  output; fine, but basins can clog if nothing pulls them.
- In game check to finish the audit: JEI → glass bottle / Potion → U, crafting tab, for every recipe that consumes a
  potion; or `/kubejs dump` the recipe list and grep for `minecraft:potion` in `crafting_*` types.

### Tests
- PB-17 (vanilla tipped arrow returns the bottle) stays the test for "keep".
- **PB-24 PneumaticCraft upgrades:** craft a Night Vision Upgrade → a glass bottle is left in the grid (keep) / not
  (scope).

---

## Built, needs play

| Row | Status | Note |
|---|---|---|
| potion-buckets #1 Potion buckets and machine-tipped arrows | Testing | PB-01 to PB-20 never run. Two code findings to fold in before testing: (1) survival cooldown never applies: `server_scripts/engineering/potion_buckets.js` line 49 calls `addCooldown(stack.getItem(), 20)` after `stack.shrink(1)` emptied the unstackable bucket, and an empty stack's `getItem()` is air (vanilla, from memory), so PB-09 only passes in creative; (2) a bucket of a no-effect potion (awkward / thick / mundane / water) bursts Night Vision through `DEFAULT_EFFECTS`. Both fixed by #3 steps 2-3 above. potion_buckets.md file-map line for the dispenser registration was stale (265 → 290, fixed) |

## Writeup corrections made

- [potion_buckets.md](../potion_buckets.md) section 5: `CreativeEngineering.java` line 265 → 290 (dispenser
  registration).
- [potion_buckets.md](../potion_buckets.md) Known limits: the 1 s cooldown line now says it probably only applies in
  creative (code reading, links here).
