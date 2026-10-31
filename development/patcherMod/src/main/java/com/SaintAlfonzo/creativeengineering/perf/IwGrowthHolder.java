package com.SaintAlfonzo.creativeengineering.perf;

import com.ordana.immersive_weathering.data.block_growths.growths.IBlockGrowth;

import java.util.Set;

/** Added to every Block by {@code IwBlockGrowthHolderMixin}: its Immersive Weathering growths per tick source ordinal. */
public interface IwGrowthHolder {
	Set<IBlockGrowth>[] ce$iwGrowths();

	void ce$setIwGrowths(Set<IBlockGrowth>[] growths);
}
