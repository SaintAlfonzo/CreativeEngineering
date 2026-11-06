# Trade sync: server trades for JER on dedicated-server clients

Status: BUILT 2026-11-03 (patcherMod `trader/TradeSync`, `trader/client/TradeSyncClient`), installed in this instance's
`mods/`, not booted. Scratch rows SQ-TSY-01, -02 and client rows CQ-TSY-01 ([scratch_queue.md](testing/scratch_queue.md)).
Needs the new patcherMod on **both** the server and the client.

## 1. Problem

Forge builds the modded villager and wandering trader lists (`VillagerTradesEvent`, `WandererTradesEvent`, so every
KubeJS / MoreJS trade in `economy/trades.js` and the Creative Delight `*/trades.js`) only in
`VillagerTradingManager.loadTrades`, on `ServerAboutToStartEvent`. A client joined to a dedicated server never runs it,
so its `VillagerTrades.TRADES` stays vanilla. Just Enough Resources reads that table for its villager pages, so on a
server JER showed only vanilla trades: no Barbeque's Delight seasonings, no salt, no Mechanical Engineer or Chef
trades, the removed Cleric trades still listed. Singleplayer was correct (the integrated server shares the table).

## 2. How it works

| Step | Where | What |
|---|---|---|
| Build | server, first `OnDatapackSyncEvent` after start | For every profession in `TRADES` and the wandering trader: each listing that is the same object as in Forge's vanilla snapshot (`VANILLA_TRADES`, `WANDERER_TRADES`, by reflection) is sent as its index; any other listing is sampled 8 times with `getOffer(null, random)` (every Throwable caught) and up to 4 distinct offers are kept. Listing classes whose name contains `map` or `treasure` are skipped (a server-side locate can hang). Cached until `ServerStoppedEvent` |
| Send | server, `OnDatapackSyncEvent` (login and `/reload`) | One packet per profession, wandering trader last, channel `creativeengineering:trade_sync` (protocol 1, optional on both sides). Memory connections (singleplayer) are skipped. Forge posts the event before the recipe and tag packets, so the lists arrive before JEI loads |
| Apply | client, main thread | First packet snapshots the client's own tables. Vanilla indexes take the client's own listing (JER samples it with its villager as before); sampled entries become a listing that hands the offers out in turn. The wandering trader packet logs `[trade sync] applied ...` |
| Restore | client, `ClientPlayerNetworkEvent.LoggingOut` | The snapshot goes back, so a later singleplayer world or another server starts clean |

Nothing here changes what a villager offers in game; it only fills the client's copy that JEI addons read.

Known limits: listings that need the villager entity on the server (none of the pack's KubeJS trades do) and map
listings are left out of the sync; a sampled random listing (enchanted items) shows up to 4 of its variants.

## 3. Logs

- server: `[trade sync] built N trade list(s), M listing(s) sampled` (once per server run), `[trade sync] sent N trade
  list(s) to P player(s)` (each login and `/reload`).
- client: `[trade sync] applied the server's villager and wandering trader trades`; it must come before
  `[jei] Seasoning page` in the same log.

## 4. Tests

- TS-01: on the server, JER's Butcher page lists the six Barbeque's Delight seasonings and 64 salt at level 1 (CQ-TSY-01).
- TS-02: Mechanical Engineer, Hydraulic Engineer, Chef and Cleric pages match the pack trades; Wandering Trader page
  shows the pack list (CQ-TSY-01).
- TS-03: after leaving the server, a singleplayer world shows the same pages without duplicates (CQ-TSY-01).
