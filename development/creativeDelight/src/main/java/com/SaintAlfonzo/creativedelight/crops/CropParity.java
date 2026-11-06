package com.SaintAlfonzo.creativedelight.crops;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
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
 *   <li>{@link #OWN_PICK_CROPS}: {@code CropBlock}s with their own ripe right-click (pick the fruit, drop back a few
 *       stages). Quark's search would harvest and replant them to stage 0 first; instead an empty hand or a 1x1 hoe
 *       leaves them to the block, and a hoe area clicks them like berry bushes ({@code mixin/QuarkOwnPickMixin}).</li>
 * </ul>
 *
 * Right-click and hoe right-click replant aren't here: those are Quark's {@code Harvestable Blocks}
 * config list (config/quark-common.toml), one {@code "mod:block[age_prop=max]"} line per crop.
 * Rich Farmland's growth boost needs nothing, since it bone-meals any {@code BonemealableBlock}.
 */
public final class CropParity {

    private CropParity() {}

    public static final TagKey<Block> FARMLAND_SOILS = BlockTags.create(
            new ResourceLocation(CreativeDelight.NS, "crops/farmland_soils"));
    public static final TagKey<Block> FARMLAND_CROPS = BlockTags.create(
            new ResourceLocation(CreativeDelight.NS, "crops/farmland_crops"));
    public static final TagKey<Block> OWN_PICK_CROPS = BlockTags.create(
            new ResourceLocation(CreativeDelight.NS, "crops/own_pick"));

    public static boolean isFarmlandSoil(BlockState soil) {
        return soil.is(Blocks.FARMLAND) || soil.is(FARMLAND_SOILS);
    }

    public static boolean isFarmlandCrop(BlockState plant) {
        return plant.is(FARMLAND_CROPS);
    }

    public static boolean isOwnPickCrop(BlockState plant) {
        return plant.is(OWN_PICK_CROPS);
    }
}
