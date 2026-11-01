package com.SaintAlfonzo.creativeengineering.compat;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * WTHIT plugin: "Silk Touch Required: True / Drops Differently / False" in three colours (green, yellow, red).
 * True = the block tag {@code creativeengineering:wthit/needs_silk_touch} (nothing drops without Silk Touch),
 * Drops Differently = {@code wthit/silk_touch_differs} (something else drops, Silk Touch gives the block itself),
 * False = any other block, shown only with {@code [wthit] silkTouchShowFalse}. Both tags are generated from the loot
 * dump by tools/gen_silk_touch_blocks.py (loot tables never reach the client; block tags do). Hidden while the
 * main-hand tool has Silk Touch. Client-side only. Off with {@code [wthit] silkTouch}.
 * feature_writeup/wthit_lines.md 2.11.
 */
public final class WthitSilkTouch implements IWailaClientPlugin, IBlockComponentProvider {

    private static final ResourceLocation LINE = new ResourceLocation(CreativeEngineering.MODID, "silk_touch");
    private static final TagKey<Block> NEEDS_SILK_TOUCH =
            BlockTags.create(new ResourceLocation(CreativeEngineering.MODID, "wthit/needs_silk_touch"));
    private static final TagKey<Block> DIFFERS =
            BlockTags.create(new ResourceLocation(CreativeEngineering.MODID, "wthit/silk_touch_differs"));

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, Block.class);
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (!CEConfig.WTHIT.silkTouch.get()) {
            return;
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, accessor.getPlayer().getMainHandItem()) > 0) {
            return;
        }
        BlockState state = accessor.getBlockState();
        String key;
        ChatFormatting color;
        if (state.is(NEEDS_SILK_TOUCH)) {
            key = "true";
            color = ChatFormatting.GREEN;
        } else if (state.is(DIFFERS)) {
            key = "differs";
            color = ChatFormatting.YELLOW;
        } else if (CEConfig.WTHIT.silkTouchShowFalse.get()) {
            key = "false";
            color = ChatFormatting.RED;
        } else {
            return;
        }
        tooltip.setLine(LINE, Component.translatable("creativeengineering.wthit.silk_touch." + key).withStyle(color));
    }
}
