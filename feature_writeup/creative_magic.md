# Creative Magic: enchanting, magic recipes and Botania tweaks in one mod (reserved)

Written 2026-11-01 from the Creative Split audit ([creative_split.md](creative_split.md) 2.3 and [split/](split/README.md)).
**Status: carved in this split (owner, 2026-11-01; creative_split.md 7.6).** Magic also takes Dark Fire, Flint and
Terrasteel, the Nether portal-failure rule, `[botania]` and `TrinketCaseRules`; the full sweep is
[split/H_magic.md](split/H_magic.md). Creative Common never holds these rows. No source header marks them (owner, 2026-11-01); this list is the marker.

Mod id `creativemagic`, package `com.SaintAlfonzo.creativemagic`, config `creativemagic-common.toml`, script packs
`kubejs_startup/creativemagic/`, `kubejs/data/creativemagic/kubejs/`. Potions, effects, alcohol and gear effects are
Creative Brewing's ([creative_brewing.md](creative_brewing.md) 4.2) and are not reserved here.

## 1. Reserved rows (interim jar: Equipment)

| Kind | Row | Source |
|---|---|---|
| Mixins | `CycleEnchantmentMixin`, `RecoveryEnchantmentMixin`, `StockpileEnchantmentMixin` | split/B_mixins.md |
| Java | `item/FlintAndTerraSteelItem` (registered in `items/CreativeEngineeringItems`, headed TEMPORARY); its Dark Fire link is Worldgen's portal rule, see creative_split.md 7.4 | split/A_java.md, D |
| Server scripts | `magic/dragon_breathing.js`, `enchants.js` (six raw event calls, convert to NCH), `progression.js`, `sculk.js`, `storage.js`, `xp.js` (146 lines) | split/F_pack_scripts.md |
| Script rows | `tags/fluidtags.js` 86-90 (XP fluid merge); `client_scripts/tooltips.js` 808-814 (Botania) | F section 13 |
| Docs | [enchant_power.md](enchant_power.md), [recipes_magic.md](recipes_magic.md) minus CB rows | |
| Open | `[botania]` config and `compat/TrinketCaseRules`: Compat or here (owner row in creative_split.md 7.4) | split/C_config.md |

Not reserved: `magic/armor_effects.js`, `tool_effects.js`, `lava_vision.js`, `startup_scripts/magic/magic_resistance.js`
and `[magic_resistance]` (all CB).

## 2. History

- 2026-11-01: reserved from the split audit. Nothing moved.
