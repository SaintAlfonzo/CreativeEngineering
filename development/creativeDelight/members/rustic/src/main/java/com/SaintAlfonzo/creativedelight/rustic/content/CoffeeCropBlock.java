package com.SaintAlfonzo.creativedelight.rustic.content;

import com.SaintAlfonzo.creativedelight.rustic.registry.RDItems;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** The coffee crop: 6 growth stages on farmland. */
public class CoffeeCropBlock extends CropBlock {
    public static final int MAX_AGE = 5;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);

    public CoffeeCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return RDItems.COFFEE_BEANS.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}
