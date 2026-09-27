package com.SaintAlfonzo.creativeengineering.surface;

import java.util.Locale;

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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;

/**
 * WTHIT plugin: a "Run Speed" line in the look-at overlay, the walk speed that block gives
 * ({@link RunSpeed}), tuned by CEConfig {@code [surface.wthit]}. Client-only, declared in
 * {@code waila_plugins.json} (loaded only when BlockRunner is present). No server data: frames
 * sync their camo to clients already.
 */
public final class WthitRunSpeed implements IWailaClientPlugin, IBlockComponentProvider {

    private static final ResourceLocation LINE = new ResourceLocation(CreativeEngineering.MODID, "run_speed");

    private static boolean listening;

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, Block.class);
        if (!listening) {
            listening = true;
            // tier names are cached per block; a tag reload (join, /reload) can move blocks between tiers
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, TagsUpdatedEvent.class,
                    event -> RunSpeed.clearTierCache());
        }
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        CEConfig.Surface.Wthit cfg = CEConfig.SURFACE.wthit;
        if (!cfg.enabled.get()) {
            return;
        }
        RunSpeed.Info info = RunSpeed.at(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState());
        if (cfg.hideNormal.get() && RunSpeed.isNormal(info.factor())) {
            return;
        }
        MutableComponent line = Component.translatable("creativeengineering.wthit.run_speed",
                RunSpeed.format(info.factor(), cfg.format.get()));
        if (cfg.showTier.get()) {
            MutableComponent detail = null;
            if (info.tier() != null) {
                detail = Component.translatable("creativeengineering.surface.tier." + info.tier().key());
            }
            if (info.note() != RunSpeed.Note.NONE) {
                Component note = Component.translatable("creativeengineering.wthit.run_speed."
                        + info.note().name().toLowerCase(Locale.ROOT));
                detail = detail == null ? note.copy() : detail.append(", ").append(note);
            }
            if (detail != null) {
                line.append(Component.literal(" (").append(detail).append(")").withStyle(ChatFormatting.GRAY));
            }
        }
        tooltip.setLine(LINE, line);
    }
}
