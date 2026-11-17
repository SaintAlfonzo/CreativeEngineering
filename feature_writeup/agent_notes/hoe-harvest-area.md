# hoe-harvest-area

> Per-hoe Quark harvest area 1x1-9x9 via creativeengineering:hoe_harvest/NxN item tags; even sizes biased toward look; generated tooltip; status in feature_writeup/crops.md

Moved from agent memory 2026-11-16 (last edited there 2026-10-02). Repo status docs win over any status line here.

2026-10-02: the user asked for hoe harvest areas per hoe (copper/gold 2x2, iron 3x3, diamond/netherite
4x4, end gear 5x5) with an auto tooltip. Quark only does odd centred squares (radius r -> 2r-1).

- Tags `creativeengineering:hoe_harvest/<N>x<N>` (N 1..9, largest wins) in kubejs equipmenttags.js
  "Hoe harvest area". I chose the unlisted ones: wood/stone 1x1, IE steel + manasteel 3x3, elementium
  4x4, echo/felsic/dragon 5x5. Untagged hoes fall back to Quark's config radius.
- patcherMod `crops/HoeHarvestArea` (size + area; even N reaches one further on the side of
  `player.getLookAngle()` per axis), `crops/HoeHarvestTooltip` (lang `creativeengineering.hoe_harvest.*`),
  `mixin/QuarkHoeAreaHarvestMixin` (SimpleHarvestModule.click) + `QuarkHoeAreaBreakMixin`
  (HoeHarvestingModule.onBlockBroken): inject at the `getRange` INVOKE, require = 1, replay Quark's loop.
- Status: two scratch-server boots clean (injections apply), tags verified via dumptags. Jar in
  patcherMod build/libs only. Tests CR-26..30 in crops.md §7.

**How to apply:** change sizes by editing the tags, not Java. See [crops-harvest-writeup](crops-harvest-writeup.md), [patchermod-coremod](patchermod-coremod.md).

2026-10-02: this ships as patcherMod **1.0.4** (pack 1.0.4), not 1.0.3; now in mods/. See [release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md).
