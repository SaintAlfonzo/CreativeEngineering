package com.SaintAlfonzo.creativedelight.rustic.content;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

import java.util.function.Supplier;

/** A farmland crop that grows over 8 stages and drops the given seed on pick-block (the bell pepper crops). */
public class RusticCropBlock extends CropBlock {
    private final Supplier<? extends ItemLike> seed;

    public RusticCropBlock(Properties properties, Supplier<? extends ItemLike> seed) {
        super(properties);
        this.seed = seed;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seed.get();
    }
}
