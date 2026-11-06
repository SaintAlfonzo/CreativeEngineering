package com.SaintAlfonzo.creativedelight.nethers.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import umpaz.nethersdelight.common.block.FungusColonyBlock;
import umpaz.nethersdelight.common.tag.NDTags;

/**
 * Nether's Delight 4.0's Fungus Colony rules on the 3.1 block (nethers_delight.md 4.4):
 * <ul>
 *   <li>growth on {@code #nethersdelight:fungus_colony_growable_on} (rich soul soil): 1 in 4 per tick (3.1: 1 in 5);</li>
 *   <li>stays ({@code canSurvive}): on a growable-on floor always; elsewhere only while the raw light at the colony is
 *       below 13 and the floor sustains it (3.1: whenever the floor is one it may be placed on).</li>
 * </ul>
 * 4.0 also sends the growth roll through Forge's crop-grow event; in this pack Creative Calendar's
 * {@code CropSeasonGrowHookMixin} already fires that event at the head of the colony's tick, so this mixin does not fire
 * it a second time. The colonies are crop season type {@code nether} (no calendar effect).
 *
 * Creative Delight overlay of the Nether's Delight repack (GNU GPL3). SRG names, remap = false.
 */
@Mixin(value = FungusColonyBlock.class, remap = false)
public abstract class FungusColonyRulesMixin extends BushBlock {

    /** 4.0's {@code FungusColonyBlock.PLACING_LIGHT_LEVEL}: off rich soul soil a colony needs light below this. */
    private static final int PLACING_LIGHT_LEVEL = 13;

    private FungusColonyRulesMixin(Properties properties) {
        super(properties);
    }

    // m_213897_ = Block.tick(BlockState, ServerLevel, BlockPos, RandomSource), which the random tick calls;
    // its only int constant 5 is the growth roll's bound (MathUtils.RAND.nextInt(5)).
    @ModifyConstant(method = "m_213897_", constant = @Constant(intValue = 5), remap = false, require = 1)
    private int creativedelight$growthRoll(int bound) {
        return 4;
    }

    /** 4.0's own survive check; added to the 3.1 class, which inherits BushBlock's. */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos floorPos = pos.below();
        BlockState floor = level.getBlockState(floorPos);
        if (floor.is(NDTags.FUNGUS_COLONY_GROWABLE_ON)) return true;
        return level.getRawBrightness(pos, 0) < PLACING_LIGHT_LEVEL && floor.canSustainPlant(level, floorPos, Direction.UP, this);
    }
}
