package com.SaintAlfonzo.creativecompat.compat;

import com.SaintAlfonzo.creativecompat.CreativeCompat;
import com.SaintAlfonzo.creativecommon.CommonConfig;

import dev.smolinacadena.appliedcooking.blockentity.KitchenStationBlockEntity;
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
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * WTHIT plugin: Applied Cooking's ME Kitchen Station, the line the mod only wrote for Jade, in its own words
 * ({@code jade.appliedcooking:*}): "Device Online" + "Connected to ME Security Terminal at x, y, z", or "Device
 * Offline". The security terminal's position lives on the server. Off with {@code [wthit] kitchenStation}.
 * feature_writeup/wthit_lines.md 2.7.
 */
public final class WthitKitchenStation implements IWailaCommonPlugin, IWailaClientPlugin, IBlockComponentProvider,
        IDataProvider<KitchenStationBlockEntity> {

    private static final String POS = "creativecommon.kitchen_station.security";
    private static final ResourceLocation STATUS = new ResourceLocation(CreativeCompat.MOD_ID, "kitchen_station");
    private static final ResourceLocation LINK = new ResourceLocation(CreativeCompat.MOD_ID, "kitchen_station_link");

    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.blockData(this, KitchenStationBlockEntity.class);
    }

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, KitchenStationBlockEntity.class);
    }

    @Override
    public void appendData(IDataWriter data, IServerAccessor<KitchenStationBlockEntity> accessor, IPluginConfig config) {
        String pos = accessor.getTarget().getSecurityStationPos();
        data.raw().putString(POS, pos == null ? "" : pos);
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getData().raw();
        if (!com.SaintAlfonzo.creativecompat.CompatConfig.WTHIT.kitchenStation.get() || !data.contains(POS)) {
            return;
        }
        String pos = data.getString(POS);
        if (pos.isEmpty()) {
            tooltip.setLine(STATUS, Component.translatable("jade.appliedcooking:offline").withStyle(ChatFormatting.RED));
            return;
        }
        tooltip.setLine(STATUS, Component.translatable("jade.appliedcooking:online").withStyle(ChatFormatting.GREEN));
        tooltip.setLine(LINK, Component.translatable("jade.appliedcooking:kitchen_station",
                Component.translatable("block.ae2.security_station"), pos));
    }
}
