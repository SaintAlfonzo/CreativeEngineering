package com.SaintAlfonzo.creativeengineering.crops;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Brings crops that aren't {@code CropBlock}s (tobacco first) in line with the normal farmland
 * crops. Two block tags drive it, filled in kubejs blocktags.js ("Crop parity"):
 *
 * <ul>
 *   <li>{@link #FARMLAND_SOILS}: soils that count as farmland for crops whose placement check is
 *       hard-coded to vanilla farmland. Vanilla farmland always counts, so an unloaded or empty tag
 *       only falls back to the mod's own rule. Each hard-coded crop needs a small mixin that calls
 *       {@link #isFarmlandSoil} ({@code mixin/TobaccoPlacementMixin}).</li>
 *   <li>{@link #FARMLAND_CROPS}: plants that the soils and Quark treat like a farmland crop. Dry
 *       vanilla farmland under them doesn't revert to dirt ({@code mixin/FarmBlockUnderCropsMixin}),
 *       soaked Fertile Soil bone-meals them like {@code CropBlock}s ({@code mixin/MulchBlockCropGrowthMixin}),
 *       and Quark's hoe area break includes them ({@code mixin/QuarkHoeHarvestMixin}).</li>
 * </ul>
 *
 * Right-click and hoe right-click replant aren't here: those are Quark's {@code Harvestable Blocks}
 * config list (config/quark-common.toml), one {@code "mod:block[age_prop=max]"} line per crop.
 * Rich Farmland's growth boost needs nothing, since it bone-meals any {@code BonemealableBlock}.
 */
public final class CropParity {

    private CropParity() {}

    public static final TagKey<Block> FARMLAND_SOILS = BlockTags.create(
            new ResourceLocation(CreativeEngineering.MODID, "crops/farmland_soils"));
    public static final TagKey<Block> FARMLAND_CROPS = BlockTags.create(
            new ResourceLocation(CreativeEngineering.MODID, "crops/farmland_crops"));

    public static boolean isFarmlandSoil(BlockState soil) {
        return soil.is(Blocks.FARMLAND) || soil.is(FARMLAND_SOILS);
    }

    public static boolean isFarmlandCrop(BlockState plant) {
        return plant.is(FARMLAND_CROPS);
    }
}
