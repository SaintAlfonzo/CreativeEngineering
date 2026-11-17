# campfire-rain

> Campfire rules 2026-10-19 - Unlit Campfire set to infinite burn (its rain off), Creative Common CampfireRain douses plain campfires in rain unless on a rain_proof_fuel block

Moved from agent memory 2026-11-16 (last edited there 2026-10-20). Repo status docs win over any status line here.

User's rules (2026-10-19): campfires (and soul campfires) place unlit (Unlit Campfire mod default, kept); burn forever; a lit plain campfire goes out in rain unless the block UNDER it is a fuel block (not an item-fuel mechanic); soul campfires and Tinted Campfires never go out from rain.

- unlitcampfire-server.toml (world serverconfig + defaultconfigs + configs_edited/server_config): campfireLitTime 0, soulCampfireLitTime 0, campfireRainUnlitTime -1, soulCampfireRainParticleFactor 1. LitTime 0 makes the mod's `burnsInfinite()` true, which also skips ITS rain check and its add-burnables/magma-cream paths, so rain had to move to Creative Common.
- Creative Common `campfire/CampfireRain` + `mixin/CampfireRainMixin` (RETURN of CampfireBlockEntity.cookTick m_155306_, duck `CampfireRainTicks`, count not saved). Tags `creativeengineering:campfire/rain_douses` (minecraft:campfire) and `campfire/rain_proof_fuel` (coal block, quark + architects_palette charcoal blocks), filled in blocktags.js. CEConfig [campfire] rainDouses, rainOutTicks 160.
- Tinted Campfires' ColorCampfire extends BaseEntityBlock, not CampfireBlock: Unlit Campfire's mixins never touch them.
- Lives in Creative Delight since 2026-11-04 (creative_compat.md history); status: [barbeques_delight.md](../barbeques_delight.md) weather row and the campfire writeups.

Related: [creative-common-coremod](creative-common-coremod.md), memory `configs-edited-backup`.
- 2026-10-22: scratch server PASSED (SQ-OM-06): plain campfire on stone out in rain, on a coal block / Quark charcoal block lit, soul campfire lit; Unlit Campfire campfireLitTime 0 from the world serverconfig (SQ-OM-18).
