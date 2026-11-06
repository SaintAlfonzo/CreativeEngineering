package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.perf.IwGrowthHolder;
import com.ordana.immersive_weathering.data.block_growths.growths.IBlockGrowth;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

/** Per-block Immersive Weathering growth table for {@code perf.IwGrowthFast} (perf.md 15, candidate B). */
@Mixin(value = Block.class, remap = false)
public class IwBlockGrowthHolderMixin implements IwGrowthHolder {
	@Unique
	private Set<IBlockGrowth>[] ce$iwGrowths;

	@Override
	public Set<IBlockGrowth>[] ce$iwGrowths() {
		return ce$iwGrowths;
	}

	@Override
	public void ce$setIwGrowths(Set<IBlockGrowth>[] growths) {
		ce$iwGrowths = growths;
	}
}
