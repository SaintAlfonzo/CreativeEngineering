# Brief: loot / trader

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# loot`, Sub `trader`. Reference writeups:
[wandering_trader.md](../wandering_trader.md) (2.2 pools, 4.1 decisions, 7 tests WT-),
[wandering_trader_catalog.md](../wandering_trader_catalog.md), [loot.md](../loot.md) 3.8 (bans). Code:
[wandering_trades.js](../../kubejs/server_scripts/economy/wandering_trades.js),
[trade_helper.js](../../kubejs/startup_scripts/func/trade_helper.js) (`global.trades`), patcherMod
`trader/TradeListings.java`, [07_rules.js](../../kubejs/server_scripts/loot/07_rules.js),
[config/wares-common.toml](../../config/wares-common.toml),
[config/lightmanscurrency-common.lcconfig](../../config/lightmanscurrency-common.lcconfig),
`config/ftbquests/quests/` (chapters `food_purchasing.snbt`, reward tables `basiccoinreward.snbt`, `tinycoinreward.snbt`).

## Scope and order

Open rows: #2, #3 (Planned). #1 is Complete (table at the end).

**Facts that shape the rows** (verified against `wares-1.19.2-1.2.9.jar` unless marked):

- A Sealed Delivery Agreement is plain item NBT (`SealedDeliveryAgreement.CODEC`): `requested` and `payment` are each
  a loot table id **or** an inline list (`requested`: `[{id:"ns:item" | "#ns:tag", Count:n | {min,max,step}}]`,
  `payment`: a list of ItemStacks), plus optional `ordered`, `experience`, `deliveryTime`, `expiresInSeconds`
  (int or `{min,max,step}`), `title`, `message`, `buyerName`, `buyerAddress`, `seal`, `id`. So agreements that pay in
  Lightman's coins need no Java: an item with the right NBT.
- Wares rolls a `payment` / `requested` loot table with `LootContextParamSets.EMPTY` when the agreement is opened, so
  LootJS sees it as type UNKNOWN: the pack's post-roll coin ban (07_rules.js line 38, chest rolls only) does not
  strip coins from it.
- Wares' own village warehouse tables (`wares:chests/village/<biome>_warehouse`) already put sealed agreements in
  loot (weight 6 of their pool) with emerald payments.
- `item_dict.js` has only `i.sealed_delivery_agreement` (line 13314). `wares:delivery_agreement`,
  `wares:cardboard_box` (the dict's `cardboard_box` is `design_decor:cardboard_box`), `wares:delivery_table`,
  `wares:package` have no keys.

**Build groups and order:** #2 first (one number, `/reload`), then #3 after its D1 (what "more trades" means). #3's
loot half waits for `loot/lootjs-layer #3` (fresh index) so `L.add` reaches every village table.

---

## trader #2: Fewer Wares trades
Status Planned / Work KubeJS / `loot/trader #2`

### Goal
Fewer Sealed Delivery Agreement offers per Wandering Trader than today's ~4.9.

### Now
- [wandering_trades.js](../../kubejs/server_scripts/economy/wandering_trades.js) line 20:
  `t.move(e, [t.COMMON, t.RARE], [i.sealed_delivery_agreement], t.COMMON, 20)`: both Wares listings (regular, 6
  emeralds; former rare, 12) into COMMON, 20 copies each. Common pool 205 entries (165 others + 40), 25 drawn
  (distinct entries) per trader.
- WT-13 expects "usually 3 to 7" agreements; wandering_trader.md 2.2 and 4.1 describe x20.

### Build
1. Change the weight (D1) on line 20; keep both listings in COMMON or put the 12-emerald one back in RARE:
   ```js
   t.move(e, [t.COMMON, t.RARE], [i.sealed_delivery_agreement], t.COMMON, 3)
   ```
   (`t.move` moves every matching listing with the same weight; to split them would need `t.move` per listing, and
   `take` matches by result item, which both share. Keeping both in COMMON is the simple path.)
2. `/reload`; `logs/kubejs/server.log` `[wandering trades] common N` -> 165 + 2 x weight.
3. Writeups: wandering_trader.md 0 (Stock row), 2.2 (pool size, agreements per trader), 4.1 (Wares bullet), WT-13
   expected count; regenerate the catalog (`/dumptrades`, `python3 tools/wandering_trader_report.py`).

### Ids and data
Expected agreements per trader (25 drawn without repeats from 165 + 2w entries; hypergeometric):

| weight w | pool | agreements per trader | traders with at least one |
|---:|---:|---:|---:|
| 1 | 167 | 0.30 | 28% |
| 2 | 169 | 0.59 | 48% |
| 3 | 171 | 0.88 | 62% |
| 5 | 175 | 1.43 | 80% |
| 10 | 185 | 2.70 | 95% |
| 20 (now) | 205 | 4.88 | 99.7% |

### Decisions
- **D1 how many:** recommend **w = 3** (about one agreement on most traders; Wares' warehouses and #3's loot also
  supply them). w = 2 if #3 adds agreements as loot and quest rewards.

### Gotchas
- `wares-common.toml` `WanderingTraderSellsAgreements = true` must stay on, or the listings to move disappear.
- Already-spawned traders keep their rolled offers.

### Tests
- Update **WT-13**: across 8 summoned traders -> 0 to 3 agreements each, about 1 on average (w = 3).

---

## trader #3: Lightman's Currency with Wares
Status Planned / Work KubeJS / data / `loot/trader #3`

### Goal
Tie Wares' delivery agreements into the coin economy: agreements that pay Lightman's coins, found as loot and given
as quest rewards; plus more coin trades (D1).

### Now
- Coins: `i.coin_copper` .. `i.coin_netherite` (`lightmanscurrency:coin_*`, item_dict lines 9619-9624). Coin chest
  loot is off (`[loot.chests]`), and 07_rules.js line 38 bans coin items from chest rolls (loud banner).
- Lightman's villagers: `addBanker = true`, `addCashier = true`, `addCustomWanderingTrades = true` (but
  wandering_trades.js line 15 removes every `@lightmanscurrency` listing from the trader); emerald-to-coin
  replacement in villager trades is off (`changeVanillaTrades = false`, `changeModdedTrades = false`).
- FTB Quests already uses coins as rewards: `food_purchasing.snbt` (repeatable "Sell x8 Wheat for 2 Copper Coins"
  style quests, 54 item tasks / rewards), reward tables `basiccoinreward` (5 / 10 / 15 iron coins) and
  `tinycoinreward`.
- The row's source note (scratchpad 2026-10-19) could not be found in git history; the TODO text is the only spec.

### Build
1. **Agreement helper** (new, `kubejs/startup_scripts/func/agreement_helper.js`, `global.agreements`), so loot, trades
   and quests share one definition:
   ```js
   global.agreements = {
     // spec: { id, title, buyer, requested: [{ item: i.x | tag, count | {min,max,step} }],
     //         payment: [{ item: i.coin_iron, count }], ordered: {min,max,step}, experience, expires }
     snbt(spec) { /* -> '{requested:[{id:"...",Count:{min:16,max:32,step:4}}],payment:[{id:"lightmanscurrency:coin_iron",Count:5b}],...}' */ },
     list: {},   // named specs: list.bakery_bread = {...}
   }
   ```
   Item ids come from `global.item` keys (CLAUDE.md); tags from the `i.t_*` keys.
2. **Agreements as loot** (pre-roll, 07_rules.js): one `L.add` per family, e.g.
   ```js
   L.add(/village|warehouse/, i.sealed_delivery_agreement,
         { share: 0.02, nbt: global.agreements.snbt(global.agreements.list.bakery_bread) })
   ```
   `L.add` supports `nbt` (05_tuning.js lines 481-507); several specs = several adds at smaller shares.
3. **Agreements as quest rewards:** add `{ type: "item", item: { id: "wares:sealed_delivery_agreement", Count: 1b,
   tag: {...} } }` rewards in `config/ftbquests/quests/chapters/*.snbt` (in-game editor or by hand; the SNBT from
   step 1 can be printed with a `/kubejs` dev command to paste). Candidate: the `food_purchasing` chain (a sell quest
   unlocks a bulk delivery contract that pays more coins).
4. **"More trades" (D1):** if it means coin trades on the Wandering Trader, extend `TradeListings` with
   `itemFor(id, count, priceId, priceCount, maxUses)` (coin price instead of emeralds) and `global.trades.sell` with
   `opts.price` (default emeralds); if it means agreements on the trader, extend `TradeListings.item` with an NBT
   argument and `global.trades.sell` with `opts.nbt`. Both are small Java changes in patcherMod (1.1.x).
5. Dict keys to add (item_dict.js, same style as line 13314): `i.delivery_agreement = 'wares:delivery_agreement'`,
   `i.wares_cardboard_box = 'wares:cardboard_box'`, `i.delivery_table = 'wares:delivery_table'`,
   `i.wares_package = 'wares:package'` (only those used).
6. Writeups: wandering_trader.md (new section on coin agreements) or a short section in loot.md 3.8; quests in the
   FTB writeup if one exists.

### Ids and data
- Verified: `wares:sealed_delivery_agreement`, `wares:delivery_agreement`, `wares:completed_delivery_agreement`,
  `wares:expired_delivery_agreement`, `wares:sealed_delivery_agreement_damaged`, `wares:cardboard_box`,
  `wares:delivery_table`, `wares:package`; item tags `wares:agreements`, `wares:delivery_boxes`,
  `wares:cardboard_box_blacklisted`.
- Wares' own agreement tables: `wares:agreement/village/<biome>_{payment,requested}_{buy,sell}`,
  `wares:agreement/wandering_trader/{regular,rare}_{price,ware}`, `.../dyes` (examples of the table form).
- Coin values between tiers: unverified here (Lightman's default 10x per tier; check the `lcconfig` coin data).

### Decisions
- **D1 what "more trades" means:** (a) coin-priced offers on the Wandering Trader, (b) more Lightman's villager trades
  (banker / cashier are on already), (c) more agreement variety. Ask the user; recommend (c) + coin payments, since
  that is what the rest of the row describes.
- **D2 inline payment vs payment tables:** inline lists (recommended: one place, readable in the helper). If tables
  are wanted, put them under `creativeengineering:agreement/...`, not under `chests/`, so the pre-roll scope and the
  coin ban never touch them.
- **D3 where agreements drop:** villages and warehouses (recommended), plus camps and ships at a lower share.

### Gotchas
- Never use the coin items themselves as a loot entry: the ban strips them and raises the banner. The agreement item
  is safe (it is `wares:sealed_delivery_agreement`).
- A delivery needs a Delivery Table with a Packager villager and cardboard boxes (`DeliveriesRequireBoxes`,
  `PackagerRequiredForDelivery`); quest text should say so.
- `ordered` counts above a few stacks take many deliveries at Packager level 1 (batch size 1).

### Tests (WT- prefix, continuing at WT-17)
- **WT-17 coin agreement loot:** `/loot give @s loot <village table>` until an agreement drops -> opening it shows
  coin payment; completing one pays the coins.
- **WT-18 quest reward:** claim the new quest -> a sealed agreement with the listed buyer, request and coin payment.
- **WT-19 no ban alarm:** after WT-17, `logs/kubejs/server.log` has no `!!!` coin banner.

---

## Built, needs play

| Row | State | Notes |
|---|---|---|
| trader #1 Wandering Trader and Winemaker | Complete | Deployed with 1.0.4 (2026-10-04, `wandering_trades.js` and `trade_helper.js` in that deploy); WT-01 to WT-16 are still unchecked boxes in wandering_trader.md 7. The writeup said "BUILT ... in `mods/`"; corrected to DEPLOYED. Row #2 changes WT-13's expected count. |

## Writeup corrections made

- [wandering_trader.md](../wandering_trader.md) 0: spawn-timer and stock rows "BUILT 2026-10-03 ... in `mods/`" ->
  "DEPLOYED with 1.0.4 (2026-10-04); built 2026-10-03".
