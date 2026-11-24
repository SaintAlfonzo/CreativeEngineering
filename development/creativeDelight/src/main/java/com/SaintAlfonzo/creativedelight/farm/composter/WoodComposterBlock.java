// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.farm.composter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A vanilla composter in another wood. Everything but fire is {@link ComposterBlock}'s: {@code use},
 * {@code getContainer} (hoppers), the comparator output, the scheduled level-7 -> 8 tick and the shared
 * {@code COMPOSTABLES} table. Vanilla gives the composter its fire odds in {@code FireBlock.bootStrap}, which only
 * names {@code Blocks.COMPOSTER}, so they are repeated here (5 / 20); the nether woods do not burn.
 */
public class WoodComposterBlock extends ComposterBlock {

	private final boolean flammable;

	public WoodComposterBlock(Properties props, boolean flammable) {
		super(props);
		this.flammable = flammable;
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return flammable ? 5 : 0;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return flammable ? 20 : 0;
	}
}
