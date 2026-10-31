package com.SaintAlfonzo.creativeengineering.compat;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.SaintAlfonzo.creativeengineering.seasons.CropSeasonTooltip;
import com.SaintAlfonzo.creativeengineering.seasons.CropSeasons;

import java.util.List;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * WTHIT plugin: the crop's growing seasons on the crop itself ({@code seasons/CropSeasons}, owner spec
 * feature_writeup/crops.md 2.12.1). The type's lines as on the planting item ("Fertile: Spring",
 * "Offseason: Winter, Dry", "Season: Nether", "Fertile: Cold biomes", ...), then where it stands: green "Fertile here: grows
 * faster" or red "Offseason here: grows slower" (neutral shows nothing). Under greenhouse glass
 * (which makes every season neutral): green "Greenhouse: preventing offseason debuff" or red "Greenhouse: preventing
 * fertile season buff", for what the season would be without the glass.
 * Client-side only: block tags, the season and the configs are all on the client.
 * Off with {@code [wthit] cropSeasons} / {@code cropOutOfSeason}. feature_writeup/wthit_lines.md 2.12.
 */
public final class WthitCropSeasons implements IWailaClientPlugin, IBlockComponentProvider {

    private static final ResourceLocation SEASONS_LINE = new ResourceLocation(CreativeEngineering.MODID, "crop_seasons");
    private static final ResourceLocation HERE_LINE = new ResourceLocation(CreativeEngineering.MODID, "crop_out_of_season");
    /** After the Growth line (default priority 1000). */
    private static final int PRIORITY = 1100;

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, Block.class, PRIORITY);
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (!CropSeasons.enabled()) {
            return;
        }
        BlockState state = accessor.getBlockState();
        CropSeasons.Type type = CropSeasons.typeOf(state);
        if (type == null || type == CropSeasons.Type.NONE) {
            return;
        }
        if (CEConfig.WTHIT.cropSeasons.get()) {
            List<Component> lines = CropSeasonTooltip.summary(type);
            for (int i = 0; i < lines.size(); i++) {
                tooltip.setLine(i == 0 ? SEASONS_LINE : new ResourceLocation(SEASONS_LINE + "_" + i), lines.get(i));
            }
        }
        if (CEConfig.WTHIT.cropOutOfSeason.get()) {
            Level level = accessor.getLevel();
            BlockPos pos = accessor.getPosition();
            CropSeasons.Fertility f = CropSeasons.fertility(level, pos, type);
            if (f == CropSeasons.Fertility.FERTILE) {
                tooltip.setLine(HERE_LINE, Component.translatable("creativeengineering.wthit.crop_season_here.fertile")
                        .withStyle(ChatFormatting.GREEN));
            } else if (f == CropSeasons.Fertility.OFF) {
                tooltip.setLine(HERE_LINE, Component.translatable("creativeengineering.wthit.crop_season_here.off")
                        .withStyle(ChatFormatting.RED));
            } else {
                // neutral only because of greenhouse glass above: say which way it cuts
                CropSeasons.Fertility open = CropSeasons.fertility(level, pos, type, false);
                if (open == CropSeasons.Fertility.OFF) {
                    tooltip.setLine(HERE_LINE, Component.translatable("creativeengineering.wthit.crop_season_here.greenhouse_off")
                            .withStyle(ChatFormatting.GREEN));
                } else if (open == CropSeasons.Fertility.FERTILE) {
                    tooltip.setLine(HERE_LINE, Component.translatable("creativeengineering.wthit.crop_season_here.greenhouse_fertile")
                            .withStyle(ChatFormatting.RED));
                }
            }
        }
    }
}
