package com.SaintAlfonzo.creativedelight.hearth.salt;

import net.minecraft.world.level.block.Block;

/**
 * A worn or waxed stage of a salt lick. Every interaction lives in {@link SaltEvents}, because stage 0 is The Salt's
 * {@code salt:salt_block} and cannot carry code of ours. Waxed stages are separate blocks for the same reason: the foreign
 * block cannot take a {@code waxed} property, and a waxed block must stay out of the POI type.
 */
public class LickableSaltBlock extends Block {
    public LickableSaltBlock(Properties properties) {
        super(properties);
    }
}
