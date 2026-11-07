package com.SaintAlfonzo.creativecompat.surface;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.CreativeCompat;

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
 * ({@link RunSpeed}), tuned by CompatConfig {@code [surface.wthit]}. Client-only, declared in
 * {@code waila_plugins.json} (loaded only when BlockRunner is present). No server data: frames
 * sync their camo to clients already.
 */
public final class WthitRunSpeed implements IWailaClientPlugin, IBlockComponentProvider {

    private static final ResourceLocation LINE = new ResourceLocation(CreativeCompat.MOD_ID, "run_speed");

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
        CompatConfig.Surface.Wthit cfg = CompatConfig.SURFACE.wthit;
        if (!cfg.enabled.get()) {
            return;
        }
        RunSpeed.Info info = RunSpeed.at(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState(),
                accessor.getPlayer());
        if (info == null
                || cfg.hideUntyped.get() && !info.typed()
                || cfg.hideNormal.get() && RunSpeed.isNormal(info.factor())) {
            return;
        }
        MutableComponent line = Component.translatable("creativeengineering.wthit.run_speed",
                RunSpeed.format(info.factor(), cfg.format.get()));
        if (cfg.showTier.get()) {
            List<Component> parts = new ArrayList<>();
            if (info.tier() != null) {
                parts.add(Component.translatable("creativeengineering.surface.tier." + info.tier().key()));
            }
            if (info.source() == RunSpeed.Source.NATIVE || info.source() == RunSpeed.Source.IN_BLOCK) {
                parts.add(Component.translatable("creativeengineering.wthit.run_speed.source."
                        + info.source().name().toLowerCase(Locale.ROOT)));
            }
            for (RunSpeed.Note note : info.notes()) {
                parts.add(Component.translatable("creativeengineering.wthit.run_speed."
                        + note.name().toLowerCase(Locale.ROOT)));
            }
            if (!parts.isEmpty()) {
                MutableComponent detail = Component.literal(" (");
                for (int i = 0; i < parts.size(); i++) {
                    if (i > 0) {
                        detail.append(", ");
                    }
                    detail.append(parts.get(i));
                }
                line.append(detail.append(")").withStyle(ChatFormatting.GRAY));
            }
        }
        tooltip.setLine(LINE, line);
    }
}
