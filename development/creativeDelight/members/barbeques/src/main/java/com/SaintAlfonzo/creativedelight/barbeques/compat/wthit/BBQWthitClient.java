package com.SaintAlfonzo.creativedelight.barbeques.compat.wthit;

import com.SaintAlfonzo.creativedelight.barbeques.block.GrillBlockEntity;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * WTHIT plugin, client half: one line per filled grill slot, "skewer: Cooking: 12s", "Ready to Flip!", "Cooked!" or
 * "Burnt" (1.3.0's Jade text and keys; 1.3.0 said "Burnt!!"). Data from {@link BBQWthit}.
 */
public final class BBQWthitClient implements IWailaClientPlugin, IBlockComponentProvider {

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, GrillBlockEntity.class);
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (!config.getBoolean(BBQWthit.GRILL)) {
            return;
        }
        ListTag slots = accessor.getServerData().getList(BBQWthit.KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < slots.size(); i++) {
            CompoundTag slot = slots.getCompound(i);
            if (!slot.contains("Item")) {
                continue;
            }
            ItemStack stack = ItemStack.of(slot.getCompound("Item"));
            Component status = GrillBlockEntity.ItemEntry.status(slot.getInt("Time"), slot.getInt("Duration"),
                    slot.getBoolean("Flipped"), slot.getBoolean("Burnt"));
            tooltip.addLine(Component.empty().append(stack.getHoverName()).append(": ").append(status));
        }
    }
}
