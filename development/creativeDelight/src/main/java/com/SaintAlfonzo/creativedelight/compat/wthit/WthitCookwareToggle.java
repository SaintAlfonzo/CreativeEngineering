package com.SaintAlfonzo.creativedelight.compat.wthit;

import com.SaintAlfonzo.creativedelight.cooking.CookwareHandling;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * WTHIT plugin: "Punch to toggle lid" / "Punch to toggle handle" on the cookware that {@link CookwareHandling} lets a
 * punch toggle (kitchen.md 2.9). Hidden when the {@code cookwarePickup} switch is off.
 */
public final class WthitCookwareToggle implements IWailaClientPlugin, IBlockComponentProvider {

	private static final ResourceLocation LINE = new ResourceLocation("creativedelight", "cookware_toggle");

	@Override
	public void register(IClientRegistrar registrar) {
		registrar.body(this, Block.class);
	}

	@Override
	public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		if (!CookwareHandling.enabled(state)) return;
		CookwareHandling.Toggle toggle = CookwareHandling.toggleOf(state);
		if (toggle != null) tooltip.setLine(LINE, toggle.hint());
	}
}
