package com.SaintAlfonzo.creativecommon.compat;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.SaintAlfonzo.creativecommon.campfire.CampfireRainInfo;
import com.SaintAlfonzo.creativecommon.campfire.CampfireRainTicks;

import cech12.unlitcampfire.mixinaccess.ICampfireBlockEntityMixin;
import cech12.unlitcampfire.mixinaccess.ICampfireBlockMixin;
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
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;

/**
 * WTHIT plugin: lit campfires. Two lines, both from the server:
 * <ul>
 *   <li>Unlit Campfire's own burn line, which it only wrote for Jade: "Burns forever" (the pack sets
 *       {@code campfireLitTime = 0}) or "Burn time: 42s", in that mod's words ({@code hud.unlitcampfire.*}).</li>
 *   <li>The pack's rain rule (Creative Compat's CampfireRain, read through {@link CampfireRainInfo}): "Rain: Puts it out", "Rain: Going out in 6s" while it rains on
 *       it, or "Rain: Safe (fuel block below)". Only campfires rain can douse (the plain campfire) get it.</li>
 * </ul>
 * Off with {@code [wthit] campfireBurn} and {@code campfireRain}. feature_writeup/wthit_lines.md 2.6.
 */
public final class WthitCampfire implements IWailaCommonPlugin, IWailaClientPlugin, IBlockComponentProvider,
        IDataProvider<CampfireBlockEntity> {

    private static final String INFINITE = "creativecommon.campfire.infinite";
    private static final String BURN_LEFT = "creativecommon.campfire.burn_left";
    private static final String RAIN = "creativecommon.campfire.rain";
    private static final String RAIN_LEFT = "creativecommon.campfire.rain_left";
    private static final int RAIN_DOUSES = 1;
    private static final int RAIN_PROOF = 2;
    private static final int RAIN_GOING = 3;
    private static final ResourceLocation BURN_LINE = new ResourceLocation("creativecommon", "campfire_burn");
    private static final ResourceLocation RAIN_LINE = new ResourceLocation("creativecommon", "campfire_rain");

    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.blockData(this, CampfireBlockEntity.class);
    }

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, CampfireBlock.class);
    }

    @Override
    public void appendData(IDataWriter data, IServerAccessor<CampfireBlockEntity> accessor, IPluginConfig config) {
        CampfireBlockEntity campfire = accessor.getTarget();
        BlockState state = campfire.getBlockState();
        if (!state.hasProperty(CampfireBlock.LIT) || !state.getValue(CampfireBlock.LIT)) {
            return;
        }
        CompoundTag raw = data.raw();
        if (ModList.get().isLoaded("unlitcampfire")) {
            Unlit.write(raw, campfire, state);
        }
        if (CampfireRainInfo.rainDouses() && state.is(CampfireRainInfo.RAIN_DOUSES)) {
            Level level = accessor.getLevel();
            BlockPos pos = campfire.getBlockPos();
            if (level.getBlockState(pos.below()).is(CampfireRainInfo.RAIN_PROOF_FUEL)) {
                raw.putInt(RAIN, RAIN_PROOF);
            } else if (level.isRainingAt(pos.above())) {
                int left = CampfireRainInfo.rainOutTicks() - ((CampfireRainTicks) campfire).ce$getRainTicks();
                raw.putInt(RAIN, RAIN_GOING);
                raw.putInt(RAIN_LEFT, Math.max(0, (left + 19) / 20));
            } else {
                raw.putInt(RAIN, RAIN_DOUSES);
            }
        }
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getData().raw();
        if (CommonConfig.WTHIT.campfireBurn.get()) {
            if (data.getBoolean(INFINITE)) {
                tooltip.setLine(BURN_LINE, Component.translatable("hud.unlitcampfire.infinite"));
            } else if (data.contains(BURN_LEFT)) {
                tooltip.setLine(BURN_LINE, Component.translatable("hud.unlitcampfire.n_seconds", data.getInt(BURN_LEFT)));
            }
        }
        Component rain = !CommonConfig.WTHIT.campfireRain.get() ? null : switch (data.getInt(RAIN)) {
            case RAIN_DOUSES -> Component.translatable("creativecommon.wthit.campfire.rain.douses");
            case RAIN_PROOF -> Component.translatable("creativecommon.wthit.campfire.rain.proof")
                    .withStyle(ChatFormatting.GREEN);
            case RAIN_GOING -> Component.translatable("creativecommon.wthit.campfire.rain.going", data.getInt(RAIN_LEFT))
                    .withStyle(ChatFormatting.RED);
            default -> null;
        };
        if (rain != null) {
            tooltip.setLine(RAIN_LINE, new PairComponent(Component.translatable("creativecommon.wthit.campfire.rain"), rain));
        }
    }

    /** Unlit Campfire's mixin interfaces; only loaded when the mod is. */
    private static final class Unlit {
        static void write(CompoundTag raw, CampfireBlockEntity campfire, BlockState state) {
            if (campfire instanceof ICampfireBlockEntityMixin be && state.getBlock() instanceof ICampfireBlockMixin block) {
                if (block.burnsInfinite(state)) {
                    raw.putBoolean(INFINITE, true);
                } else {
                    raw.putInt(BURN_LEFT, Math.max(0, (block.getMaxLitTime(state) - be.getLitTime()) / 20));
                }
            }
        }
    }
}
