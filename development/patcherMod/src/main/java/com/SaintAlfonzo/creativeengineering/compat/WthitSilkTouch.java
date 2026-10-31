package com.SaintAlfonzo.creativeengineering.compat;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.component.ItemComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * WTHIT plugin: a Silk Touch book icon and "Needs Silk Touch" on blocks that only drop themselves with Silk Touch
 * (glass, ice, grass blocks, ores, stone). The list is the block tag {@code creativeengineering:wthit/needs_silk_touch},
 * generated from the loot dump by tools/gen_silk_touch_blocks.py (loot tables never reach the client; block tags do).
 * Hidden while the main-hand tool has Silk Touch. Client-side only. Off with {@code [wthit] silkTouch}.
 * feature_writeup/wthit_lines.md 2.11.
 */
public final class WthitSilkTouch implements IWailaClientPlugin, IBlockComponentProvider {

    private static final ResourceLocation LINE = new ResourceLocation(CreativeEngineering.MODID, "silk_touch");
    private static final TagKey<Block> NEEDS_SILK_TOUCH =
            BlockTags.create(new ResourceLocation(CreativeEngineering.MODID, "wthit/needs_silk_touch"));

    private ItemComponent icon;

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, Block.class);
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (!CEConfig.WTHIT.silkTouch.get()) {
            return;
        }
        BlockState state = accessor.getBlockState();
        if (!state.is(NEEDS_SILK_TOUCH)) {
            return;
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, accessor.getPlayer().getMainHandItem()) > 0) {
            return;
        }
        if (icon == null) {
            ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1));
            icon = new ItemComponent(book);
        }
        tooltip.setLine(LINE).with(icon).with(Component.translatable("creativeengineering.wthit.silk_touch"));
    }
}
