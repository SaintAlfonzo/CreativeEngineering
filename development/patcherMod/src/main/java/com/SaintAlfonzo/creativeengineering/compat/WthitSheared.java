package com.SaintAlfonzo.creativeengineering.compat;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * WTHIT plugin: "Sheared, prevents spreading and converting to dirt" on a grass-like block cut with shears. Shears clear the block's
 * {@code fertile} state (Immersive Weathering's grass, mycelium and soils; the pack's Vintage lush grass and Vinery
 * grass slab), and a block with {@code fertile=false} never random-ticks, so it neither spreads nor grows plants.
 * Vines (Creative Compat {@code block.VineShearing}): "Sheared, stops growing" on a sheared modded vine ({@code fertile=false}) and
 * on Vinery's jungle grape vine with {@code sterilized=true}; "Stopped growing" on a weeping, twisting or cave vine
 * or kelp tip at its last age and on Immersive Weathering's ivy at its last age (10), sheared or grown that far.
 * Client-side only: block state is always on the client. Off with {@code [wthit] sheared}.
 * feature_writeup/wthit_lines.md 2.10, feature_writeup/vine_shearing.md.
 */
public final class WthitSheared implements IWailaClientPlugin, IBlockComponentProvider {

    private static final ResourceLocation LINE = new ResourceLocation(CreativeEngineering.MODID, "sheared");
    private static final ResourceLocation IW_IVY = new ResourceLocation("immersive_weathering", "ivy");
    private static final int IW_IVY_MAX_AGE = 10;

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, Block.class);
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (!CEConfig.WTHIT.sheared.get()) {
            return;
        }
        BlockState state = accessor.getBlockState();
        String key = lineKey(state);
        if (key != null) {
            tooltip.setLine(LINE, Component.translatable(key));
        }
    }

    private static String lineKey(BlockState state) {
        Block block = state.getBlock();
        boolean vine = block instanceof VineBlock;
        if (hasValue(block.getStateDefinition().getProperty("fertile"), state, false)) {
            return vine ? "creativeengineering.wthit.sheared_vine" : "creativeengineering.wthit.sheared";
        }
        if (vine && hasValue(block.getStateDefinition().getProperty("sterilized"), state, true)) {
            return "creativeengineering.wthit.sheared_vine";
        }
        if (block instanceof GrowingPlantHeadBlock head && head.isMaxAge(state)) {
            return "creativeengineering.wthit.not_growing";
        }
        if (block.getStateDefinition().getProperty("age") instanceof IntegerProperty age
                && state.getValue(age) >= IW_IVY_MAX_AGE && IW_IVY.equals(ForgeRegistries.BLOCKS.getKey(block))) {
            return "creativeengineering.wthit.not_growing";
        }
        return null;
    }

    private static boolean hasValue(Property<?> property, BlockState state, boolean wanted) {
        return property instanceof BooleanProperty bp && state.getValue(bp) == wanted;
    }
}
