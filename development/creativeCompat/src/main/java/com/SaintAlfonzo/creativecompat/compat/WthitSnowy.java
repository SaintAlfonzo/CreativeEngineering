// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.compat;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.WailaConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * WTHIT plugin: Snow! Real Magic's blocks are named after the block they hold, "Snowy Oak Stairs", "Snowy Poppy"
 * (snow_real_magic.md ruling R12; lang key {@code creativecompat.wthit.snowy}). Its snowy plant holding nothing (a plain
 * snow layer whose snowy look is off) keeps the name "Snow". The held block is synced to the client by SRM. Client
 * only; registered only with SRM loaded ({@code waila_plugins.json}).
 */
public final class WthitSnowy implements IWailaClientPlugin, IBlockComponentProvider {

	@Override
	public void register(IClientRegistrar registrar) {
		registrar.head(this, Block.class, 2000);
	}

	@Override
	public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		if (!SnowRealMagic.isVariant(state)) return;
		BlockState held = SnowRealMagic.held(accessor.getWorld(), accessor.getPosition(), state);
		if (held == null) return;
		tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, Component.translatable("creativecompat.wthit.snowy",
				held.getBlock().getName()).withStyle(ChatFormatting.WHITE));
	}
}
