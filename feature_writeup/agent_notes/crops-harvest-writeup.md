# crops-harvest-writeup

> Crop soils / Quark replant / hoe harvesting reference in feature_writeup/crops.md (CR- tests); crop parity layer (2 tags + Quark config line + 4 mixins) first used for tobacco, is THE pattern for other crops

Moved from agent memory 2026-11-16 (last edited there 2026-10-02). Repo status docs win over any status line here.

feature_writeup/crops.md documents, for every crop, its type, soils and harvesting. Right-click replant
and the hoe area mechanics both come from Quark (SimpleHarvestModule + HoeHarvestingModule), and the pack
runs their default config. Quark auto-finds only non-vanilla `CropBlock` subclasses and matches exact states.

**Crop parity layer (2026-10-02, crops.md §2.6):** the user asked for tobacco to match the other crops and
said it sets the approach for the remaining crop parity work. Pieces:
- block tags `creativeengineering:crops/farmland_soils` (farmland, rich farmland, fertile soil) and
  `crops/farmland_crops` (tobacco), in blocktags.js "Crop parity" via global.tags + item_dict keys;
- Quark `Harvestable Blocks` line `the_dirty_stuff:tobacco_seeds[blockstate=7]` (backed up per memory `configs-edited-backup`);
- patcherMod `crops/CropParity` + mixins FarmBlockUnderCropsMixin (`m_53250_`), MulchBlockCropGrowthMixin
  (IW `m_213898_`), QuarkHoeHarvestMixin (`canHarvest`): generic, tag-driven. TobaccoPlacementMixin is
  per-crop (each hard-coded soil check needs its own mixin calling `CropParity.isFarmlandSoil`).
- Rich Farmland's boost needed nothing (FD bone-meals any BonemealableBlock).
Status and open gaps (G1-G11): feature_writeup/crops.md and feature_writeup/todo/TODO.md.

**Why:** the user wants every crop to behave like wheat on all soils and with both Quark mechanics.
**How to apply:** read crops.md §2.1 before any crop/soil/harvest question. For another crop, follow the
§2.6 checklist; extend the tags/config, don't add per-crop special cases beyond a placement mixin.
See memory `feature-writeup-pattern`, [patchermod-coremod](patchermod-coremod.md).

2026-10-02: this ships as patcherMod **1.0.4** (pack 1.0.4), not 1.0.3; now in mods/. See [release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md).
