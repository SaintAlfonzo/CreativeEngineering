package com.SaintAlfonzo.creativedelight.compat.wthit;

import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleTank;
import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleTankHolder;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.component.PairComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import umpaz.farmersrespite.common.block.entity.KettleBlockEntity;

/**
 * WTHIT plugin: the Farmer's Respite Kettle's tank, "Tank: Black Tea 3 / 4" (farmers_respite.md K11), beside the brew
 * progress and patcherMod's "Holding" leftover line. Client side: the tank's fluid and level reach the client with
 * the block (block entity sync and {@code water_level}). Declared in CD's {@code waila_plugins.json} with Farmer's
 * Respite required.
 */
public final class WthitKettleTank implements IWailaClientPlugin, IBlockComponentProvider {

	private static final ResourceLocation LINE = new ResourceLocation("creativedelight", "kettle_tank");

	@Override
	public void register(IClientRegistrar registrar) {
		registrar.body(this, KettleBlockEntity.class);
	}

	@Override
	public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
		if (!(accessor.getBlockEntity() instanceof KettleTankHolder tank)) return;
		FluidStack fluid = tank.ce$tankFluid();
		Component value = fluid.isEmpty()
				? Component.translatable("creativedelight.kettle.tank.empty")
				: Component.translatable("creativedelight.wthit.kettle_tank.value", fluid.getDisplayName(), tank.ce$tankLevel(),
						KettleTank.MAX_LEVEL);
		tooltip.setLine(LINE, new PairComponent(Component.translatable("creativedelight.wthit.kettle_tank"), value));
	}
}
