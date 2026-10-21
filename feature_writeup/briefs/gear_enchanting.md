# Brief: gear / enchanting

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# gear`, Sub `enchanting`. All four rows come from the
2026-10-19 scratchpad and have **no feature writeup yet**; the closest are [recipes_magic.md](../recipes_magic.md)
(CEI machines, `xp.js`), [archery.md](../archery.md) (the NEA enchantments kept off tables and trades,
`enchants.js`), [fixes_compat.md](../fixes_compat.md) (the liquid-XP merge onto CEI's fluid) and
[tooltips.md](../tooltips.md). Recommend starting `feature_writeup/enchanting.md` (test prefix `EN-`) with the first
row built; the test ids below use that prefix.

What the pack has today (all verified against jars or the repo on 2026-10-21 unless marked):

| Piece | Mod / file | Relevant state |
|---|---|---|
| Enchanting table menu | Easy Magic 4.3.3 (`easymagic`), [config/easymagic-server.toml](../../config/easymagic-server.toml) | Replaces the menu (`ModEnchantmentMenu`, overrides `slotsChanged` without calling super). `max_enchanting_power = 15`, re-roll on (3 lapis + 5 XP points), `enchantment_hint = SINGLE`, `lenient_bookshelves = true` |
| Quark Matrix Enchanting | `config/quark-common.toml:1788` | `"Matrix Enchanting" = false` (off) |
| Librarians | Dynamic Villager Trades 1.2.3 (`dynamicvillagertrades`), `config/dynamicvillagertrades.properties` | DVT generates librarian book offers itself (`no_book_duplicates=true`), see #1 |
| Villager trade edits | MoreJS 0.3.0, [trades.js](../../kubejs/server_scripts/economy/trades.js) | `MoreJSEvents.villagerTrades` (line 12); no librarian section; cleric sells Bottle o' Enchanting at master (line 94) |
| Enchantment filters | [enchants.js](../../kubejs/server_scripts/magic/enchants.js) | MoreJS `filterEnchantedBookTrade` / `filterAvailableEnchantments` drop NEA Stockpile, Recovery, Cycle |
| XP storage item | XP Tome 2.1.7 (`xpbook`), item `xpbook:xp_tome` | In the pack since the first commit (2026-09-28); not in item_dict; see #2 |
| Liquid XP | CEI `create_enchantment_industry:experience` (`f.experience` / `f.xp`, fluid_dict.js:79-80), 1 mB = 1 XP point | patcherMod `XpHelperMixin`, `XPFluidManagerMixin`, `fluid/FluidBridge.java` |
| Extra enchanting power | Supplementaries 2.4.20, Botania, Quark, Blueprint, Create | see #4 |

## Scope and order

| Row | Status | Size |
|---|---|---|
| `gear/enchanting #1` Enchanted books out of villager trades | Planned | small: ~6 lines in `trades.js`, plus a decision on already-spawned librarians |
| `gear/enchanting #3` Enchanting overhaul | Planned | medium: one patcherMod mixin + CEConfig section, Easy Magic config, tooltips, a dump command |
| `gear/enchanting #4` Enchanting power item audit | Planned | medium: a power table (patcherMod mixin + CEConfig list), one tag fix, two Supplementaries config values |
| `gear/enchanting #2` XP tomes | Planned | medium-large: recipe, stack size, tooltip, config, fluid capability, book-pile power |

Build order:
1. **#1** first: independent, `/reload`-only, and it also closes the NEA-books-through-DVT hole (see #1 Now).
2. **#3 + #4 together** in one patcherMod build: both are "how much power does a table see, and what does it buy".
   Decide the top cost (#3 Decisions) before the power values (#4), because the values only mean something against
   the cap. Add `/dumpenchants` (#3 step 1) before deciding.
3. **#2** last: its storage cap follows #3's top cost, and its "enchanting power by stored XP" is a #4 entry. The
   recipe / stack / tooltip / filling parts of #2 don't depend on anything and can go earlier if wanted.

Cross-family: `gear/tuning #4` (assembly NBT carry, unrelated mixin but same "enchantments survive" concern),
`loot/*` rows touching enchanted-book loot (the "adventure" source #1 leans on, [loot.md](../loot.md)).

---

## enchanting #1: Enchanted books out of villager trades
Status **Planned** · Work: KubeJS / data · id `gear/enchanting #1`

### Goal
Villagers stop selling enchanted books, so enchantments come from adventure (loot), the enchanting table and CEI
printing (the Printer copies a book for liquid XP, recipe in [xp.js:19-20](../../kubejs/server_scripts/magic/xp.js)).

### Now
- Vanilla source: librarian `VillagerTrades$EnchantBookForEmeralds` listings, levels 1 to 5.
- **Dynamic Villager Trades rebuilds librarians from those listings** (decompiled 2026-10-21):
  `trade_offers/generators/LibrarianGenerator.tradeGroupAtLevel` scans each level's `ItemListing[]` from
  `VillagerTrades.TRADES` (`VanillaLikeGenerator`, `f_35627_`); for every `EnchantBookForEmeralds` it finds, it adds a
  subgroup of `EnchantSpecificBookFactory` offers, **one per enchantment and level for every registered enchantment
  whose `isTradeable()` (`m_6594_`) is true**. Its offer bypasses vanilla's `EnchantBookForEmeralds.getOffer`, which
  is where MoreJS's `filterEnchantedBookTrade` hooks in. Consequences:
  - `enchants.js`'s trade filter doesn't reach DVT librarians. NEA's three enchantment classes don't override
    `isTradeable` (checked in `nock_enough_arrows-1.19.2-1.2.0.jar`), and patcherMod's
    `StockpileEnchantmentMixin` / `RecoveryEnchantmentMixin` / `CycleEnchantmentMixin` only override
    `isDiscoverable` (`m_6592_`), `canApplyAtEnchantingTable` and `isAllowedOnBooks`. **So DVT librarians can
    probably sell Stockpile, Recovery and Cycle books today** (unverified in game). Same for any other tradeable
    enchantment the pack hides, e.g. Golden Food's `goldenfood:gf_enchantment` ([loot.md](../loot.md) 508).
  - Treasure enchantments (Mending, Frost Walker...) are tradeable, so DVT offers them at every listing level.
  - If the `EnchantBookForEmeralds` listings are gone from `VillagerTrades.TRADES`, DVT generates no book
    subgroups at all: removing the vanilla listings is enough for both paths.
- MoreJS writes its `villagerTrades` edits straight back into `VillagerTrades.TRADES` (`TradingManager.updateVanillaTrades`,
  decompiled), and reruns from a backup on `/reload`, so DVT (which reads that map lazily) sees the edited lists.
- Quark's `ExchangeAncientTomesTrade` (master librarian, `"Librarians Exchange Ancient Tomes" = true`,
  quark-common.toml:512) takes an Ancient Tome + a max-level enchanted book and gives a specific Ancient Tome. It's a
  converter, not a source of new books; it isn't an `EnchantBookForEmeralds`, so the removal below leaves it.
- Wandering trader and Winemaker pools have no enchanted books ([wandering_trader_catalog.md](../wandering_trader_catalog.md)).
- MCA Reborn villagers use the vanilla profession tables (unverified whether DVT's `VillagerEntityMixin` applies to
  them; the removal covers both).
- Enchanted tools / armour (`EnchantedItemForEmeralds`: armorer, weaponsmith, toolsmith, fletcher) are not books
  and are out of this row's scope (see Decisions).

### Build
1. In [trades.js](../../kubejs/server_scripts/economy/trades.js), inside `MoreJSEvents.villagerTrades` (line 12), add
   near the top (before the per-profession sections; all professions, so a mod villager that reuses the vanilla
   class is covered too):
   ```js
   // Enchanted books off every villager (gear/enchanting #1): enchantments come from loot, the table and the
   // CEI printer. Removing the vanilla listing also stops Dynamic Villager Trades, which builds its librarian
   // book offers from it (and ignores filterEnchantedBookTrade in magic/enchants.js).
   const $EnchantBook = Java.loadClass('net.minecraft.world.entity.npc.VillagerTrades$EnchantBookForEmeralds')
   e.forEachTrades((listings, level, profession) => {
   	listings.removeIf(l => l instanceof $EnchantBook)
   })
   ```
   `forEachTrades(ForEachCallback)` is `(List<ItemListing>, int, VillagerProfession)` (MoreJS
   `VillagerTradingEventJS`, decompiled). The script already loads `net.minecraft.world.entity.npc.VillagerTrades`
   in `_lib/villager_trades_dump.js`, so the class filter allows the package. MoreJS's own filter form also works
   but is fiddlier (`e.removeTrades({firstItem: i.emerald, secondItem: <empty>, outputItem: i.enchanted_book,
   types: ['EnchantBookForEmeralds']})`: its `EnchantBookForEmeralds` matcher tests an empty second item, so the
   second matcher must accept an empty stack; unverified how KubeJS builds that).
2. Leave a short line in [enchants.js](../../kubejs/server_scripts/magic/enchants.js) that `filterEnchantedBookTrade`
   is now belt-and-braces (no book trades left).
3. Already-spawned villagers keep the offers saved in their NBT. Decide (see Decisions); if cleaning them, add a
   `MoreJSEvents.playerStartTrading` handler (`StartTradingEventJS.forEachOffers((offer, index) => ...)`,
   MoreJS 0.3.0) in the same file that disables (`offer.setDisabled(true)` or sets max uses 0; check the
   `OfferExtension` API in game with `console.log(Object.keys(offer))`) any offer whose result is
   `i.enchanted_book`. Unverified API details; the event exists (decompiled `core/Events.java`).
4. Restart (MoreJS fires at server start; `/reload` reruns it too), then test.

### Ids and data
- `i.enchanted_book` (item_dict.js:1407), `i.emerald`. No profession dict exists; `trades.js` keeps its own `v`
  object (lines 5-10), add `v.librarian = 'minecraft:librarian'` there if a librarian-only form is preferred.
- Mod facts verified by decompiling DVT 1.2.3, MoreJS 0.3.0, Quark 3.4-418 and NEA 1.2.0.

### Decisions
- **Whether** (the row's open question). Recommend yes: CEI printing makes any book you find reproducible, so
  villager books would otherwise be the cheapest route to every enchantment including Mending.
- **Existing librarians on the live world.** Recommend cleaning them with the `playerStartTrading` handler (step 3),
  otherwise the change only affects villagers that level up or get a profession after the update.
- **Enchanted tools / armour trades.** Recommend leaving them for now (one random enchant at modest levels); revisit
  with #3 if they undercut the table.
- **Quark tome exchange.** Recommend keep (needs a max-level book you already have).

### Gotchas
- `/dumptrades` ([villager_trades_dump.js](../../kubejs/server_scripts/_lib/villager_trades_dump.js)) samples
  `VillagerTrades.TRADES` with a throwaway villager; it shows the vanilla listing table, **not** what DVT villagers
  actually offer. Test with a real librarian.
- DVT caches generated offers (`cachedOffer`) and resets them on its own data reload; restart rather than trust a
  `/reload` for the first check.
- Don't use `removeVanillaTrades` for librarians: it would also drop paper, bookshelves, lanterns, glass, etc.

### Tests
- **EN-01 No books:** spawn a librarian, give it a lectern, trade it up to master (`/data merge entity` its
  `VillagerData.level`, or trade) → no enchanted book at any level; paper, bookshelf, compass, clock etc. still there.
- **EN-02 Old villager:** a librarian that had a book offer before the update → the offer is gone or disabled (if
  step 3 is built).
- **EN-03 Other paths intact:** the CEI Printer still copies an enchanted book; Quark's master-librarian tome exchange
  still shows when holding an Ancient Tome.

---

## enchanting #3: Enchanting overhaul
Status **Planned** · Work: config / KubeJS (+ patcherMod) · id `gear/enchanting #3`

### Goal
From the row: "Easy Magic's max-enchantment limit makes max-level enchants need a higher level (documented in
tooltips); enchanting power tuned to the enchantments available." Read as: the table's top cost goes above vanilla's
30 levels so the best rolls need more power and a higher player level, the numbers are in tooltips, and the cost
curve is chosen against the enchantments actually in the pack.

### Now
- **Easy Magic's knob is `max_enchanting_power`** (easymagic-server.toml:3, now 15): "Amount of bookshelves required
  to perform enchantments at the highest level". `ModEnchantmentMenu.slotsChanged` (decompiled) computes
  `power = getEnchantingPower(level, pos) * 15 / max_enchanting_power` (0 means always 15), where
  `getEnchantingPower` sums `BlockState.getEnchantPowerBonus` over `EnchantmentTableBlock.BOOKSHELF_OFFSETS`
  (`f_207902_`, the 32 ring positions) that pass `isValidBookShelf` (`m_207909_`), then casts to int. It then calls
  vanilla `EnchantmentHelper.getEnchantmentCost(random, slot, power, stack)` (`m_220287_`) for the three slots.
- **The 30-level ceiling is vanilla's**, not Easy Magic's: `getEnchantmentCost` clamps power to 15 and returns
  `max(j, 2*power)` for the top slot with `j = rand(8) + 1 + power/2 + rand(power+1)` (so 30 at power 15; slot 0 is
  `max(j/3, 1)`, slot 1 `j*2/3 + 1`). Raising `max_enchanting_power` alone only makes 30 need more shelves; it can't
  push the cost past 30.
- With `max_enchanting_power = 15` the normalisation is the identity (power = summed bonus).
- MoreJS's enchanting-table events (`enchantmentTableChanged` etc.) hook `EnchantmentMenu.slotsChanged`, which Easy
  Magic overrides without calling super: **don't plan on them**. `filterAvailableEnchantments` hooks
  `EnchantmentHelper.getAvailableEnchantmentResults` and still works (Easy Magic calls the vanilla list method).
- No enchanting tooltips exist on the table or bookshelf ([tooltips.js](../../kubejs/client_scripts/tooltips.js) has
  only book / enchanted book lines at 411-412 and the pedestal at 729).
- No dump of enchantments exists (`/dumpall` list in [dump_all.js:14-33](../../kubejs/server_scripts/_lib/dump_all.js)).

### Build
1. **`/dumpenchants`** in `kubejs/server_scripts/_lib/dump_enchants.js` (pattern: `dump_gear.js`), registered in the
   `dump_all.js` list. Per enchantment: id, min / max level, rarity, category, `isTreasureOnly`, `isTradeable`,
   `isDiscoverable`, `isAllowedOnBooks`, `getMinCost(l)` / `getMaxCost(l)` for each level. Output
   `kubejs/exported/enchantments.json`. Use it to pick the cap (step 3): for each item class's enchantability
   (`/dumpgear` already exports it), which max levels are reachable at a given top cost (modified level
   `m = cost + 1 + rand(e/4+1) + rand(e/4+1)`, times 1 ± 0.15; level `l` qualifies when
   `minCost(l) <= m <= maxCost(l)`). A small table in the new writeup is enough.
2. **patcherMod mixin `EnchantmentCostCapMixin`** on `net.minecraft.world.item.enchantment.EnchantmentHelper`,
   method `m_220287_` (`getEnchantmentCost(RandomSource, int, int, ItemStack)`), `remap = false` (the
   `SkullBlockEntityMixin` / `ArmorMaterialEnchantabilityMixin` idiom: SRG names, no refmap). `@ModifyConstant(
   constant = @Constant(intValue = 15))` on both clamp constants (`if (power > 15) power = 15`) returning
   `CEConfig.ENCHANTING.powerCap`. Register in `creativeengineering.mixins.json` (common, not client). Every table
   path goes through it: vanilla menu, Easy Magic's menu, re-rolls.
   Verify the two `15` constants are the only ones in that method (decompile the 1.19.2 client jar method, or
   check with `-Dmixin.debug.export=true`).
3. **CEConfig section `[enchanting]`** in
   `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/CEConfig.java` (pattern:
   `ArmorEnchant`, line 384): `powerCap` (int, default 15 = vanilla, range 15-50) and #4's `powerOverrides` list.
   Top cost = `2 * powerCap`.
4. **Easy Magic:** keep `max_enchanting_power = 15` so power stays the plain sum and `powerCap` is the only scale.
   (Alternative: set `max_enchanting_power` = shelves wanted for power 15 and leave the cap; that only re-prices 30
   and doesn't meet the row.) Keep the file in both `config/` and `configs_edited/root_config/` in step (both have
   it today).
5. **Tooltips** in [tooltips.js](../../kubejs/client_scripts/tooltips.js) "Basic materials" / blocks section:
   `i.enchanting_table`: "Top cost 40 levels at 20 enchanting power"; "Power: bookshelf 1, 4 placed books 1, end
   crystal on a pedestal 3, …" (from #4's table). `i.bookshelf`: "1 enchanting power". Keep the numbers in one
   comment block next to the config defaults so they get updated together.
6. Re-tune the re-roll cost if the top cost rises (Easy Magic `reroll_catalyst_cost`, `reroll_experience_points_cost`).

### Ids and data
- Vanilla targets (SRG, from the decompiles' references): `EnchantmentHelper.m_220287_`,
  `EnchantmentTableBlock.f_207902_`, `EnchantmentTableBlock.m_207909_`. The clamp-to-15 body is from vanilla 1.19.2
  knowledge, not decompiled here (no client jar in scratchpad): verify before writing the `@ModifyConstant`.
- `i.enchanting_table` (item_dict.js:2251), `i.bookshelf` (2071).

### Decisions
- **Top cost ("levels").** Recommend `powerCap = 20` → top cost 40: max-level Protection IV / Sharpness V on diamond
  become regular instead of lucky, the jump is visible, and the 32-position ring can still reach it with plain
  bookshelves (20 of 32 slots), so #4's extras are a convenience, not a gate. Confirm with the step-1 table.
- **What the player pays.** Vanilla consumes only 1 / 2 / 3 levels (slot + 1) even at cost 40; the cost is a
  requirement. Recommend keeping that (the requirement is the "needs a higher level"); a "pay the full cost" variant
  is a second mixin on `EnchantmentMenu.clickMenuButton`, and Easy Magic overrides that too.
- **CEI Blaze Enchanter** ignores the table cap (its own XP-fluid costs, hyper XP for +1 level). Recommend leaving it
  as the automation route and saying so in the writeup.

### Gotchas
- Botania pylons give 8 (mana) and 15 (natura, gaia) power each (`PylonBlock.Variant`, decompiled): with the cap at
  15 one natura pylon in the ring is already max. Retune them in #4 if the cap goes up.
- Easy Magic casts the summed float power to int: fractional sources (book piles 0.25 per book) only count in whole
  steps.
- `lenient_bookshelves = true`: non-full blocks (candles, carpets, torches) between table and ring don't block; but
  only the ring positions themselves are summed.
- The client draws whatever costs the server sends; no client change needed.

### Tests
- **EN-10 Cap:** 15 bookshelves → top cost 30 (as now); 20 → 40 with `powerCap = 20`; 25 → still 40.
- **EN-11 Easy Magic:** re-roll still works and its costs are as configured.
- **EN-12 Tooltip:** table and bookshelf tooltips show the configured numbers.
- **EN-13 Dump:** `/dumpenchants` writes the file; spot-check Sharpness's min/max costs.

---

## enchanting #4: Enchanting power item audit
Status **Planned** · Work: data / KubeJS (+ patcherMod) · id `gear/enchanting #4`

### Goal
Decide and set an enchanting-power value for each candidate the row names (ender crystals, books and enchanted
books, skulls and heads, candles and fancy candles, filled XP tomes by stored XP, filled bottles, amethyst and other
crystal clusters, others), plus the existing modded sources.

### Now
Power only comes from **blocks** in the ring (`getEnchantPowerBonus`). What already gives power (classes that
implement it, found by scanning the cached jars; ~130 of 407 jars are cached, so a few may be missing):

| Source | Power | Where set | Verified |
|---|---|---|---|
| `minecraft:bookshelf` | 1 | Forge default | known |
| Quark variant bookshelves | 1 each | `VariantBookshelfBlock` | jar |
| Blueprint (Abnormals) bookshelves | `BookshelfBlock` (value not read; expect 1) | jar class found |
| Supplementaries book pile (placed books) | `book_power` 1.0 per 4 books (0.25 each); Quark Ancient Tome counts double (0.5) | `[tweaks.placeable_books]` supplementaries-common.toml:552-562 | jar |
| Supplementaries book pile, enchanted books | **0.25 each, not 1.334/4**: see Gotchas | same | jar |
| Supplementaries pedestal holding an End Crystal | `crystal_enchanting = 3` | supplementaries-common.toml:272 | jar |
| Supplementaries frames, Create copycats | the held / copied block's value | `SelfFrameMixin`, `CopycatBlock` | jar |
| Botania mana / natura / gaia pylon | 8 / 15 / 15 | `PylonBlock.Variant` (no config) | jar |
| Mystical Oak Tree wise oak | `WiseOakBlock` (value not read) | jar class found |

Candidates from the row with no power today: skulls and heads (`i.skeleton_head` = `minecraft:skeleton_skull`,
`i.wither_head`, `i.zombie_head`, `i.creeper_head`, `i.dragon_head`, `i.player_head`, `i.enderman_head` =
`supplementaries:enderman_head`, `i.gaia_head` = `botania:gaia_head`), candles (`#minecraft:candles`), "fancy
candles" (read as Supplementaries candle holders `i.candle_holder*` and skull candles, `SkullCandlesBehavior`),
amethyst clusters (`i.amethyst_cluster`, buds), Quark corundum clusters (`i.*_corundum_cluster`), Geode clusters
(`i.celestite_cluster`, `i.lapis_cluster`, ...), filled bottles (no placeable bottle block; Supplementaries jars hold
XP as a soft fluid but `JarBlock` has no power hook), XP tomes (items; only placeable through a book pile, see #2).
The tooltips in [tooltips.js:411-412](../../kubejs/client_scripts/tooltips.js) and 729 already advertise the book
pile and pedestal values.

### Build
1. **Power table mixin** (same patcherMod build as #3). Add `getEnchantPowerBonus(LevelReader, BlockPos)` to
   `net.minecraft.world.level.block.state.BlockState` through a mixin class (`EnchantPowerOverrideMixin`, a plain
   added method with the Forge name, no remap): look the state's block up in a table built from
   `CEConfig.ENCHANTING.powerOverrides`; if found return that value, else return
   `state.getBlock().getEnchantPowerBonus(state, level, pos)` (what Forge's `IForgeBlockState` default does).
   The added method overrides the interface default for every caller: vanilla's `isValidBookShelf`, Easy Magic's
   `ForgeAbstractions.getEnchantPowerBonus` (decompiled: calls `state.getEnchantPowerBonus(level, pos)`), Create's
   enchant-power Display Link source. The Forge default-method bodies above are from Forge 1.19.2 knowledge, not a
   decompile: check them, and check Mixin accepts the added method (boot log) before relying on it. Fallback if it
   doesn't: two targeted mixins, `EnchantmentTableBlock.m_207909_` and Easy Magic's `ForgeAbstractions`.
2. **Config format** (`[enchanting] powerOverrides`): `"minecraft:amethyst_cluster=0.5"`, `"#minecraft:candles=0.25"`
   (block id or `#block_tag`, `=` because ids contain `:`). Resolve tags on `TagsUpdatedEvent` into a
   `Map<Block, Float>`; exact ids win over tags. Optional per-entry `@lit` suffix meaning "only when the `lit`
   property is true" for candles / candle holders. Pattern for the bridge class: `ArmorEnchantabilityBridge`.
3. **Book-pile fix** (KubeJS, [tags](../../kubejs/server_scripts/tags/)): remove `i.enchanted_book` from
   `#supplementaries:placeable_books` with `global.tags.remove(e, '#supplementaries:placeable_books',
   [i.enchanted_book])` (add a dict key `i.t_supplementaries_placeable_books = '#supplementaries:placeable_books'`
   next to the other tag keys). Then enchanted books hit the `isEnchantedBook` branch and give
   `enchanted_book_power / 4` (1.334 / 4) as the config and the tooltip at line 412 already claim. Check in game that
   enchanted books are still placeable (`isAcceptedItem` = `isEnchantedBook || (mixed_books && isNormalBook)`, so
   the pile accepts them; whether the right-click placement still triggers is not traced).
4. **Values** (recommendations, against #3's cap 20; one CEConfig list):
   - Skulls / heads: mob heads 0.5, wither skeleton skull 1, dragon head 2, player head 0.
   - Candles: 0.25 per lit candle block (`@lit`); candle holders 0.5 lit; skull candles 1 lit.
   - Amethyst cluster 0.5 (buds 0); corundum clusters 0.5; Geode clusters 0.5.
   - Book piles: keep `book_power = 1.0`; `enchanted_book_power = 1.334` becomes live with step 3.
   - Pedestal crystal: keep 3.
   - Botania pylons: retune to mana 3, natura 5, gaia 6 if the cap rises (else one pylon maxes the table).
   - Filled bottles: skip (no block; revisit only if a placeable bottle block arrives).
   - XP tomes: through #2's book-pile mixin, up to 1 per full tome.
5. **Tooltips:** add the values to the items in tooltips.js (skull, candle, cluster entries), and the summary line on
   the enchanting table (#3 step 5).

### Ids and data
- All block ids above are in item_dict.js (lines 1411-1414, 1422, 1531, 2103, 2240, 2252, 2555, 2881-3533,
  3571-3573, 5776, 6389-6452); block ids equal the item ids except where noted. Block tag `#minecraft:candles`
  (vanilla). Skull candle block ids not checked (Supplementaries, `skull_candle*`; look up with F3 in game).
- Supplementaries tag `supplementaries:placeable_books` (contents: book, enchanted_book, 16 Inspirations books;
  verified in the jar).

### Decisions
- **Values** (the row's open question): the table in step 4. Main call: whether the extras should exceed a
  bookshelf per block (they're decorative and fiddly to place), recommend at most 1 except the dragon head and the
  pedestal crystal.
- **Pylons:** retune or leave Botania's numbers. Recommend retune with the cap raise.

### Gotchas
- **Supplementaries enchanted-book bug (verified in the jar):** `BookPileBlockTile.updateTileOnInventoryChanged`
  checks `isNormalBook` (tag `placeable_books`, which contains `minecraft:enchanted_book`) before
  `isEnchantedBook`, so enchanted books give 0.25 like plain books and `enchanted_book_power` is dead. The tooltip at
  tooltips.js:412 ("Three placed enchanted books provide the same enchantment power as a bookshelf") is wrong until
  step 3.
- Power counts only at the 32 ring positions two blocks out, with the in-between block empty or non-full; a head on
  top of a bookshelf doesn't count.
- Easy Magic floors the summed power (int cast).

### Tests
- **EN-20 Values:** for each source, a table with only that source in the ring; read the power with a Create Display
  Link "enchant power" source on the table (Create ships `EnchantPowerDisplaySource`) and compare with the config.
- **EN-21 Book pile:** 3 enchanted books in one pile → power 1 (after step 3); 4 plain books → 1.
- **EN-22 Lit candles:** unlit candle → 0, lit → its value.

---

## enchanting #2: XP tomes
Status **Planned** · Work: KubeJS / patcherMod · id `gear/enchanting #2`

### Goal
From the row: new recipe; stack like books and enchanted books; enchanting power by stored XP; maybe a crafting
ingredient for other magic; tooltip with the levels stored (current / max, counted from level 0); a filling
automation recipe; storage cap tuned for enchanting, crafting and player-to-player trading.

### Now
- The item exists: **XP Tome 2.1.7 (`xpbook`), `xpbook:xp_tome`** (plus the deprecated `xpbook:xp_book` "OLD -
  RIGHTCLICK TO CONVERT"). Verified by decompiling `bl4ckscor3.mod.xptome`:
  - Registered `stacksTo(1)`. Stored XP is the int NBT `xp` (points, not levels). `use`: sneak + right-click stores
    as much of the player's XP as fits, right-click returns all (config switches below); returns early if the stack
    count is > 1. Glint when `xp > 0`; durability bar shows fill; anvils refuse it (`AnvilUpdateEvent` cancelled);
    not enchantable or repairable.
  - Tooltip: `xpbook.tooltip.1/2` (use lines) and `xpbook.tooltip.3` = `"%1$s/%2$s XP stored"` (points).
  - Recipe `data/xpbook/recipes/xp_tome.json` (id `xpbook:xp_tome`): shaped `_E_ / EBE / _E_`, E = ender pearl,
    B = book, result `forge:nbt` `xpbook:xp_tome` with `{xp:0}`.
  - Config: **server** config `xptome-server.toml` (per-world `serverconfig/`; the pack ships none, so defaults):
    `max_xp = 1395` (30 levels from 0), `retrieval_percentage = 1.0`, `retrieve_until_next_level = false`,
    `retrieve_xp_orbs = false`, `store_until_previous_level = false`.
- Not in item_dict, not in `stack_sizes.json`, no recipe edits, no tooltips in the pack.
- Liquid XP is CEI's fluid at 1 mB per point (patcherMod `XpHelperMixin`, `XPFluidManagerMixin`); `FluidBridge`
  (`fluid/FluidBridge.java:14, 27`) has `LIQUID_XP_ID` and `getTargetFluidNow()`.
- Create's spout fills any item with a `FLUID_HANDLER_ITEM` capability: `GenericItemFilling.canItemBeFilled` /
  `fillItem` (decompiled Create 0.5.1.i) split one item off the stack, fill it up to what the spout holds, and
  return the container. The item drain uses the same capability (`GenericItemEmptying`, not read).
- A commented-out item capability example sits in `CreativeEngineering.java:587-610` (`attachCaps` for IE sawdust).

### Build
1. **Dict:** `i.xp_tome = 'xpbook:xp_tome'` in item_dict.js next to `i.xp_nugget` (line 1695), with a comment
   pointing at this brief / the new writeup. Optional `i.xp_tome_old = 'xpbook:xp_book'` to hide it in JEI.
2. **Recipe** (new file `kubejs/server_scripts/magic/xp_tome.js`, or a section in `xp.js`):
   ```js
   R.remove({id: 'xpbook:xp_tome'}, e)
   R.shaped(Item.of(i.xp_tome, '{xp:0}'), [' N ', 'NBN', ' L '], {N: i.xp_nugget, B: i.book, L: i.lapis}, e)
   ```
   Keep the `{xp:0}` result so crafted tomes match emptied ones (`setStoredXP(0)` writes `xp:0`). Ingredients are a
   decision; Create experience nuggets tie it to the XP economy without the ender-pearl cost.
3. **Stack size** ("like books and enchanted books": empty ones stack, filled ones don't):
   - Base size: add `'xpbook:xp_tome'` to a new `MISC_16` list (or `BOTTLES_16`) in
     [tools/gen_stack_sizes.py](../../tools/gen_stack_sizes.py) (lists at lines 85-97), rerun, restart
     (`stack_sizes.js` applies it).
   - Filled = 1: patcherMod mixin `XpTomeStackMixin` on `bl4ckscor3.mod.xptome.XPTomeItem` (`remap = false`) adding
     Forge's `getMaxStackSize(ItemStack)` (an `IForgeItem` default that `XPTomeItem` doesn't override): return 1
     when `stack.getTag()` has `xp > 0`, else the item's base size. Without this mixin, two tomes with identical
     `xp` would also stack (harmless: the mod refuses to use a stack, so no dupe, but confusing).
4. **Tooltip in levels** (client, [tooltips.js](../../kubejs/client_scripts/tooltips.js), dynamic like the
   `precision_sawblade` entry at line 446): `e.addAdvanced(i.xp_tome, (item, adv, text) => { ... })` reading
   `item.nbt?.xp`, converting points → level from 0 (total points to reach level L: `L²+6L` up to 16,
   `2.5L²-40.5L+360` up to 31, `4.5L²-162.5L+2220` above), and replacing the mod's points line (find the line whose
   string ends with "XP stored", or blank `xpbook.tooltip.3` via a `kubejs/assets/xpbook/lang/en_us.json` override)
   with "Stored: 12.4 / 30 levels (from 0)". The max comes from the server config; the client can't read it, so
   hard-code it next to the config default with a comment (or derive it from `getBarWidth` if wanted).
5. **Cap:** ship `defaultconfigs/xptome-server.toml` with `max_xp` = points for #3's top cost from level 0
   (30 → 1395, 40 → 2920, 50 → 5345). Existing worlds already have `serverconfig/xptome-server.toml` generated:
   edit the live server's copy too (deploy note).
6. **Filling automation:** patcherMod `AttachCapabilitiesEvent<ItemStack>` for `xp_tome` providing an
   `IFluidHandlerItem` (one tank, capacity `Configuration.CONFIG.maxXP`, contents `FluidStack(FluidBridge
   target, xp)`, fill → `XPTomeItem.addXP`, drain → `removeXP`, only when the stack count is 1). Spouts fill partial
   tomes with liquid XP, the item drain empties them, CEI / Create pipes work as is. No recipe JSON. The static
   alternative (`R.createFilling(Item.of(i.xp_tome, '{xp:1395}'), [Item.of(i.xp_tome, '{xp:0}'),
   Fluid.of(f.experience, 1395)], e, 'xp_tome_fill')`) only does empty → full and breaks when `max_xp` changes;
   recommend the capability.
7. **Enchanting power by stored XP:** add `i.xp_tome` to `#supplementaries:placeable_books` (same tag call as #4
   step 3) so book piles accept it, and a patcherMod mixin on
   `net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile.updateTileOnInventoryChanged`
   (`@Inject` at `TAIL`, `remap = false`, `@Shadow private float enchantPower`) that, for each tome in the pile's 4
   slots, subtracts the plain-book share the tag gave it (`book_power / 4`) and adds
   `tomePower * xp / max_xp` (`tomePower` a CEConfig value, recommend 1.0 per full tome). Placement itself:
   Supplementaries attaches its book-pile placement per item at startup (Quark tomes get
   `SimplePlacement(BOOK_PILE)` in `InteractEventOverrideHandler`); the tag alone may not make tomes placeable.
   If it doesn't, register the same `SimplePlacement` for `xp_tome` from patcherMod (Moonlight's `IExtendedItem`).
   Unverified; check in game first.
8. **Ingredient use** (optional): full tomes only, as a `forge:partial_nbt` ingredient
   `{type: 'forge:partial_nbt', item: i.xp_tome, nbt: '{xp:1395}'}` (the form `engineering/dust.js:24` uses with
   `R.shapeless`). There's no ingredient for "any filled tome"; partial ones can't be used.
9. Writeup: start `enchanting.md` with the tome section.

### Ids and data
- `xpbook:xp_tome`, `xpbook:xp_book`, recipe `xpbook:xp_tome`, NBT `xp` (int), lang keys `item.xpbook.xp_tome`,
  `xpbook.tooltip.1-3`, config `xptome-server.toml` keys above: all verified in the jar.
- `i.xp_nugget` (`create:experience_nugget`), `i.book`, `i.lapis`, `f.experience`.

### Decisions
- **Cap** (row's question): recommend points for the table's top cost from 0 (1395 if #3 keeps 30; 2920 for 40), so
  one full tome = one top-tier enchant from scratch, a clean unit for trading and for the ingredient form.
- **Uses as an ingredient** (row's "crafting ingredient for other magic?"): recommend none until a recipe wants it;
  if so, full tomes only (step 8).
- **Recipe ingredients:** recommend book + 4 experience nuggets + lapis (step 2) over the mod's 4 ender pearls.
- **`retrieval_percentage`:** recommend keep 1.0 (lossless); a loss makes the tome worse than a CEI tank.
- **Stack size for empties:** recommend 16 (like the pack's bottles), or 64 to match books.

### Gotchas
- Tags vs. no tag: a tome from JEI / creative has no NBT, crafted ones have `{xp:0}`; they won't stack together
  until the tagless one is used once. The mod's tooltip calls `getOrCreateTag` client-side, which also leaves an
  empty `{}` on client copies.
- The spout holds 1000 mB: a tome needing more than that takes two passes (route it back to the spout).
- The mod also fires `PlayerXpEvent` on store / retrieve; MoreJS `playerXpChange` handlers (none in the pack today)
  would see them.
- Mending: `retrieve_xp_orbs = false` puts XP straight into the bar; Mending never sees it. Fine; say it in the
  tooltip if players ask.

### Tests
- **EN-30 Recipe:** the new recipe makes an empty tome; the old pearl recipe is gone (JEI).
- **EN-31 Stack:** 16 empty tomes stack; a filled one doesn't stack with anything; a stack can't be used.
- **EN-32 Tooltip:** a tome holding 315 points reads "15.0 / 30 levels"; full reads "30 / 30".
- **EN-33 Spout / drain:** a spout with liquid XP fills an empty tome (two passes for 1395); an item drain empties it
  into a tank; amounts match 1 mB per point.
- **EN-34 Book pile:** a full tome in a book pile in the ring adds 1 power (EN-20 Display Link method); an empty tome
  adds 0.

---

## Built, needs play

No enchanting row is at Testing or Complete.

## Writeup corrections made

- [archery.md](../archery.md) 2.5 (NEA enchantments): added that the MoreJS book-trade filter doesn't reach Dynamic
  Villager Trades librarians, which can probably still sell Stockpile / Recovery / Cycle (DVT builds its offers from
  `isTradeable`, which the mixins don't override).
