# wandering-trader

> Wandering Trader + Winemaker: independent tick spawners (96000), 25+5 offers, stock via global.trades in economy/wandering_trades.js; no Nether/End items; feature_writeup/wandering_trader.md WT-01..16

Moved from agent memory 2026-11-16 (last edited there 2026-10-03). Repo status docs win over any status line here.

Built 2026-10-03 (two rounds, second on request): CEConfig `[wandering_trader]` traderIntervalTicks / winemakerIntervalTicks 96000, commonTrades 25, rareTrades 5.
Creative Common `trader/`: WanderingTraderTweaks + 2 mixins, WinemakerSpawner (own spawner, SavedData), TradeListings / TagItemListing / WinemakerTrades (Java bridge).
Since 2026-11-24 (wave 2 carve, W2-2): WanderingTraderTweaks, both mixins and `[wandering_trader]` are Creative Loot's (`creativeloot-common.toml`, `LootConfig`); the Winemaker spawner is the Vinery port's; TradeListings, TagItemListing, TradeSync and the trade helper stay in Common.
KubeJS: `global.trades` (startup func/trade_helper.js) used in MoreJSEvents.wandererTrades (server economy/wandering_trades.js); tags in tags/tradetags.js.
Vinery `wine_trader_chance` set to 0 (backed up). Jar md5 37ce1023 in mods/; scratch server checked, WT tests pending.

**User rule:** no trader sells Nether/End (sequence-breaking) items. The report's EXCLUDE table holds the list.

Non-obvious findings:
- MoreJS fires the wanderer event on the finished pools and again on /reload from a backup, so edits never stack. Its removeTrades only matches vanilla/Forge listing classes, so removal samples getOffer(null) in Java.
- `VillagerTrades.ItemsForEmeralds` is package-private in 1.19.2; use Forge `BasicItemListing`.
- Better Days runs daytime at 0.125x, so never describe timers in in-game days; the spawners count server ticks.
- Quark forces a Pathfinder's Quill on every Wandering Trader, the Winemaker included; /dumptrades misses it.
- Rhino: `const` inside try{} in the dump script gave "redeclaration of var"; use var in a separate function.
- `#creativeengineering:loot/golden_food` contains golden chorus fruit (End), hence the separate trades tag.
- Forge config live reload misses `sed -i` (rename); write in place to test.

Related: [creative-common-coremod](creative-common-coremod.md), memory `feature-writeup-pattern`, [release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md), memory `scratch-server-verify`, memory `centralize-recipes-tags-items`, memory `rhino-fieldandmethods-gotcha`.
