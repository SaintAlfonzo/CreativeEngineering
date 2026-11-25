package com.SaintAlfonzo.creativedelight.compat.wthit;

import com.SaintAlfonzo.creativedelight.farm.shippingbin.ShippingBinBlockEntity;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.component.PairComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** WTHIT: the Shipping Bin's stored value against its capacity (the client copy the bin syncs for its display). */
public final class WthitShippingBin implements IWailaClientPlugin, IBlockComponentProvider {
	private static final ResourceLocation LINE = new ResourceLocation("creativedelight", "shipping_bin_value");

	@Override
	public void register(IClientRegistrar registrar) {
		registrar.body(this, ShippingBinBlockEntity.class);
	}

	@Override
	public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
		if (!(accessor.getBlockEntity() instanceof ShippingBinBlockEntity bin)) return;
		tooltip.setLine(LINE, new PairComponent(Component.translatable("creativedelight.wthit.shipping_bin"),
				Component.translatable("creativedelight.wthit.shipping_bin.value", bin.storedValue(), bin.clientCapacity())));
	}
}
