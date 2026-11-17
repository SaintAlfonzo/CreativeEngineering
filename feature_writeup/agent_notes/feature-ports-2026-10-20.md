# feature-ports-2026-10-20

> Tier 1 (8 mods, KubeJS/data) + Glowing Illagers / Comfortable Campfires / Bottled Air ported 2026-10-20 (patcherMod 1.1.01); Book Fishing kept on purpose; Create Waystone Recipes was dead; Starter Kit marker = tracking.json, not the tag

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

2026-10-20 (feature_porting.md 1.1 + 2.4, FP-08..14; TODO largecontent/featureporting #2 Testing, #3 In Progress):
- Tier 1 in `kubejs/server_scripts/other/feature_ports.js` (377 recipes through NCH; AE2's 311 generated from 19
  templates x 16 colors + 7 fluix; 85 `ae2:*_cable` keys added to item_dict), `tags/feature_port_tags.js`
  (`ae2:can_remove_color`, `forge:buckets/water`, `creativeengineering:bottled_air/fire_sources`),
  `kubejs/data/minecraft/loot_tables/blocks/{copper_ore,deepslate_copper_ore}.json` (Copper Drops: 1 raw copper),
  `other/starter_kit.js`. Jars (11) in `disabledmods/`, rows out of mod_sides.md, configs deleted, server_mods/pack regenerated.
- **Book Fishing stays installed** (user: part of the enchanted-book loot rebalance). Create Waystone Recipes' only
  recipe needed `createcasing:chorium_ingot` (mod not in pack) = never loaded; just removed (D6 open).
- Starter Kit: the mod's real "already served" record is `<world>/data/starterkit/tracking.json` (multiplayer uuid map,
  singleplayer world-folder map; every playerdata uuid marked on each start), NOT the scoreboard tag. Port reads it,
  skips, writes it back + adds `collective.firstJoin.starterkit`. Kits = STARTER_KITS in the script (item_dict keys).
- Tier 2 in patcherMod 1.1.01: `mixin/RaidGlowingMixin` (Raid#addWaveMob = **m_37718_**, not m_37713_ = joinRaid;
  check the tsrg), `campfire/CampfireComfort` (called from CampfireRainMixin's cookTick RETURN, 20-tick gate by pos,
  CEConfig [campfire] comfort*), `item/BottledAir` ([bottled_air], fire sources tag). CEConfig [raids].
- Scratch 2026-10-20: FP-06, FP-08, FP-14 PASSED; player-driven tests (FP-09..13) need the user's game.
- User's version rule: this patcherMod release is **1.1.01** (not 1.0.11).

Related: [patchermod-coremod](patchermod-coremod.md), memory `scratch-server-verify`, [campfire-rain](campfire-rain.md), [backported-vinery](backported-vinery.md).
