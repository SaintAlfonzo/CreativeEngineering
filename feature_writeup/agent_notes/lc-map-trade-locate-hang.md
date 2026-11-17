# lc-map-trade-locate-hang

> LC cashier + Cloud Storage balloon-salesman map trades searched structures on the server thread; FIXED 2026-10-02 via patcherMod AsyncMapTrades (Async Locator); MCA offer hand-off gotcha

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Found 2026-10-02 on a scratch server: `/dumptrades` -> LC `ItemsForMapTrade.getOffer` -> sync `findNearestMapStructure` -> 60 s watchdog kill. Cloud Storage `SellingMap` (big balloon / sky temple) had the same problem (~20 s). Async Locator 1.3.0 only patches vanilla's TreasureMapForEmeralds.

**Fixed (patcherMod 1.0.3, in mods/, staged in 1.0.3):** `mixin/LCItemsForMapTradeMixin` + `mixin/CSSellingMapMixin` (HEAD inject on m_213663_, remap=false) -> `compat/AsyncMapTrades`: returns AL's pending map (CommonLogic.createEmptyMap), and one server task later (TickTask) runs AsyncLocator.locate when the trader `isAddedToWorld() || isRemoved()`, then MerchantLogic.handleLocationFound + finish (LC lockMap AFTER the fill, since lockMap needs the map data; CS MapColor). libs/ needs the LC, CS and async-locator jars.

**Gotchas found while testing:** (1) mods fill offers from EntityJoinLevelEvent, before Forge's isAddedToWorld is true, so check one task later. (2) MCA SpawnQueue discards every vanilla villager, calls ve.getOffers() on the discarded one, and passes the SAME MerchantOffers object to the MCA villager, so "removed" traders must still be searched. Never-added traders (dump sample villagers) are skipped. Test summons: lost console commands right after "Done": wait ~20 s.

Verified: 24 summoned traders -> 23 maps filled, 0 pending, 19 locked map copies (LC), CS tinted; dumptrades 5 s. Related: [dead-structure-locate-hang](dead-structure-locate-hang.md), [deploy-pipeline](deploy-pipeline.md).
