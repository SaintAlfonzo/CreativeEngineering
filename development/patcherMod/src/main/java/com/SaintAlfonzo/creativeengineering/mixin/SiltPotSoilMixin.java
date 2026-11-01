package com.SaintAlfonzo.creativeengineering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;

/**
 * A Twigs silt pot filled with rooted dirt is soil for every plant, like a Supplementaries planter (which returns true
 * from {@code canSustainPlant} for any {@link IPlantable}): crops, saplings, cactus, sugar cane, nether wart, bushes.
 * Before, only {@code FlowerBlock}s went on it (Twigs' own mixin). An unfilled pot is unchanged. flower_pots.md 3.2.
 */
@Pseudo
@Mixin(targets = "com.ninni.twigs.block.enums.SiltPotBlock", remap = false)
public abstract class SiltPotSoilMixin extends Block {

    protected SiltPotSoilMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, IPlantable plantable) {
        if (facing == Direction.UP && filled(state)) return true;
        return super.canSustainPlant(state, level, pos, facing, plantable);
    }

    private static boolean filled(BlockState state) {
        for (var property : state.getProperties()) {
            if (property.getName().equals("filled") && property.getValueClass() == Boolean.class) {
                return (Boolean) state.getValue(property);
            }
        }
        return false;
    }
}
