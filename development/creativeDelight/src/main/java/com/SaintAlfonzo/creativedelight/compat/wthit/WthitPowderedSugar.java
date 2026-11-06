package com.SaintAlfonzo.creativedelight.compat.wthit;

import com.SaintAlfonzo.creativedelight.food.PowderedSugarBlock;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.world.level.block.state.BlockState;

/**
 * WTHIT plugin: the powdered sugar block keeps its own name and mod. WTHIT's vanilla plugin overrides every
 * {@code PowderSnowBlock} with a snow block (option {@code override_powder_snow}), which made the powdered sugar block
 * read "Snow Block" / Minecraft (TODO food/seasoning #14). This override runs first (priority 0, the vanilla one is
 * 1000) and returns the block's own state, so the vanilla override is never reached for it.
 */
public final class WthitPowderedSugar implements IWailaClientPlugin, IBlockComponentProvider {

	@Override
	public void register(IClientRegistrar registrar) {
		registrar.override(this, PowderedSugarBlock.class, 0);
	}

	@Override
	public BlockState getOverride(IBlockAccessor accessor, IPluginConfig config) {
		return accessor.getBlockState();
	}
}
