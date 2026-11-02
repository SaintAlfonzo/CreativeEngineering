package com.SaintAlfonzo.creativeengineering.compat;

import com.SaintAlfonzo.creativeengineering.CEConfig;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.IWailaCommonPlugin;
import mcp.mobius.waila.api.component.PairComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.ModList;
import umpaz.farmersrespite.common.block.entity.KettleBlockEntity;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

/**
 * WTHIT plugin: what the leftover slots of the FD cooking pot, the Farmer's Respite kettle and the oven hold
 * ("Holding: 3 Glass Bottle, 1 Bucket"), and "Waiting: leftover slots full" while a craft is held back for room
 * ({@code [cooking] stallWhenFull}). The buffer is read on the server, so the line doesn't depend on the client copy.
 * Off with {@code [wthit] remainderBuffer}. feature_writeup/remainder_buffer.md 3.3, TODO cooking/kitchen #3.
 * The buffer itself is Creative Delight's; this line reads it through {@link RemainderView} (the oven is added to
 * {@link RemainderView#STATIONS} by CD, the pot and kettle carry the {@link RemainderView.Holder} mixin).
 */
public final class WthitRemainderBuffer implements IWailaCommonPlugin, IWailaClientPlugin, IBlockComponentProvider,
        IDataProvider<BlockEntity> {

    private static final String ITEMS = "creativeengineering.remainders";
    private static final String STALLED = "creativeengineering.remainders.stalled";
    private static final ResourceLocation HOLDING = new ResourceLocation("creativeengineering", "remainders");
    private static final ResourceLocation WAITING = new ResourceLocation("creativeengineering", "remainders_stalled");

    @Override
    public void register(ICommonRegistrar registrar) {
        RemainderView.STATIONS.forEach(station -> registrar.blockData(this, station));
        if (ModList.get().isLoaded("farmersdelight")) {
            registrar.blockData(this, CookingPotBlockEntity.class);
        }
        if (ModList.get().isLoaded("farmersrespite")) {
            registrar.blockData(this, KettleBlockEntity.class);
        }
    }

    @Override
    public void register(IClientRegistrar registrar) {
        RemainderView.STATIONS.forEach(station -> registrar.body(this, station));
        if (ModList.get().isLoaded("farmersdelight")) {
            registrar.body(this, CookingPotBlockEntity.class);
        }
        if (ModList.get().isLoaded("farmersrespite")) {
            registrar.body(this, KettleBlockEntity.class);
        }
    }

    @Override
    public void appendData(IDataWriter data, IServerAccessor<BlockEntity> accessor, IPluginConfig config) {
        BlockEntity be = accessor.getTarget();
        RemainderView buffer = be instanceof RemainderView.Holder holder ? holder.ce$remainderView() : null;
        if (buffer == null) {
            return;
        }
        ListTag items = new ListTag();
        for (int i = 0; i < buffer.getSlots(); ++i) {
            ItemStack stack = buffer.getStackInSlot(i);
            if (!stack.isEmpty()) {
                items.add(stack.save(new CompoundTag()));
            }
        }
        CompoundTag raw = data.raw();
        raw.put(ITEMS, items);
        if (buffer.isStalled(accessor.getLevel().getGameTime())) {
            raw.putBoolean(STALLED, true);
        }
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getData().raw();
        if (!CEConfig.WTHIT.remainderBuffer.get() || !data.contains(ITEMS)) {
            return;
        }
        ListTag items = data.getList(ITEMS, Tag.TAG_COMPOUND);
        if (!items.isEmpty()) {
            MutableComponent held = Component.empty();
            for (int i = 0; i < items.size(); ++i) {
                ItemStack stack = ItemStack.of(items.getCompound(i));
                if (i > 0) {
                    held.append(", ");
                }
                held.append(Component.translatable("creativeengineering.wthit.remainders.entry", stack.getCount(),
                        stack.getHoverName()));
            }
            tooltip.setLine(HOLDING, new PairComponent(
                    Component.translatable("creativeengineering.wthit.remainders.holding"), held));
        }
        if (data.getBoolean(STALLED)) {
            tooltip.setLine(WAITING, Component.translatable("creativeengineering.wthit.remainders.stalled")
                    .withStyle(ChatFormatting.RED));
        }
    }
}
