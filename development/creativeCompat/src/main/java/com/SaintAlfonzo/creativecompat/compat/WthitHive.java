package com.SaintAlfonzo.creativecompat.compat;

import com.SaintAlfonzo.creativecompat.CreativeCompat;
import java.util.Collections;

import com.SaintAlfonzo.creativecommon.CommonConfig;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.IWailaCommonPlugin;
import mcp.mobius.waila.api.component.PairComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * WTHIT plugin: beehives and bee nests (every {@code BeehiveBlock}: vanilla and Upgrade Aquatic's driftwood and
 * river hives). "Bees: 2 / 3" on top, in place of WTHIT's occupant-names line ("2 Bee"), and WTHIT's own
 * "Honey Level: 3" becomes "Honey Level: 3 / 5".
 *
 * <p>The client never receives a hive's bees (vanilla doesn't sync them), so the server side of this plugin writes
 * the count into WTHIT's block data; a server without it just leaves the line out. The honey line reuses WTHIT's
 * tag and option ({@code minecraft:bee.hive_honey_level}): this body provider runs after WTHIT's (priority 1500
 * against 1000), so {@code setLine} replaces lines in place, and turning WTHIT's option off still hides it.
 * Knobs: {@code [wthit] hiveBees}, {@code hiveBeesOnTop}, {@code hiveHoneyMax}. feature_writeup/wthit_lines.md.
 */
public final class WthitHive implements IWailaCommonPlugin, IWailaClientPlugin, IBlockComponentProvider,
        IDataProvider<BeehiveBlockEntity> {

    private static final String BEES = "creativecommon.bees";
    private static final ResourceLocation BEES_LINE = new ResourceLocation(CreativeCompat.MOD_ID, "hive_bees");
    private static final ResourceLocation HONEY_LEVEL = new ResourceLocation("minecraft", "bee.hive_honey_level");
    private static final ResourceLocation OCCUPANTS = new ResourceLocation("minecraft", "bee.hive_occupants");
    private static final ResourceLocation HONEY_AFTER = new ResourceLocation(CreativeCompat.MOD_ID, "hive_honey");
    private static final int MAX_HONEY = Collections.max(BeehiveBlock.HONEY_LEVEL.getPossibleValues());

    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.blockData(this, BeehiveBlockEntity.class);
    }

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, BeehiveBlock.class, 1500);
    }

    @Override
    public void appendData(IDataWriter data, IServerAccessor<BeehiveBlockEntity> accessor, IPluginConfig config) {
        data.raw().putInt(BEES, accessor.getTarget().getOccupantCount());
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        CompoundTag data = accessor.getData().raw();
        boolean showHoney = config.getBoolean(HONEY_LEVEL) && state.hasProperty(BeehiveBlock.HONEY_LEVEL);
        ITooltipComponent honey = null;
        if (showHoney) {
            int level = state.getValue(BeehiveBlock.HONEY_LEVEL);
            honey = new PairComponent(Component.translatable("tooltip.waila.honey_level"),
                    Component.literal(com.SaintAlfonzo.creativecompat.CompatConfig.WTHIT.hiveHoneyMax.get() ? level + " / " + MAX_HONEY : String.valueOf(level)));
        }
        if (!com.SaintAlfonzo.creativecompat.CompatConfig.WTHIT.hiveBees.get() || !data.contains(BEES)) {
            if (honey != null && com.SaintAlfonzo.creativecompat.CompatConfig.WTHIT.hiveHoneyMax.get()) {
                tooltip.setLine(HONEY_LEVEL, honey);
            }
            return;
        }
        int count = data.getInt(BEES);
        PairComponent bees = new PairComponent(Component.translatable("creativecommon.wthit.hive_bees"),
                Component.literal(count + " / " + BeehiveBlockEntity.MAX_OCCUPANTS));
        // WTHIT writes its occupant-names line ("2 Bee") above the honey line when its option is on and bees are in.
        // The Bees line replaces it; with no names line it takes the honey line's slot and honey moves under it, so
        // the order is always Bees, Honey Level.
        if (!com.SaintAlfonzo.creativecompat.CompatConfig.WTHIT.hiveBeesOnTop.get()) {
            if (honey != null && com.SaintAlfonzo.creativecompat.CompatConfig.WTHIT.hiveHoneyMax.get()) {
                tooltip.setLine(HONEY_LEVEL, honey);
            }
            tooltip.setLine(BEES_LINE, bees);
        } else if (config.getBoolean(OCCUPANTS) && count > 0) {
            tooltip.setLine(OCCUPANTS, bees);
            if (honey != null) {
                tooltip.setLine(HONEY_LEVEL, honey);
            }
        } else if (honey != null) {
            tooltip.setLine(HONEY_LEVEL, bees);
            tooltip.setLine(HONEY_AFTER, honey);
        } else {
            tooltip.setLine(BEES_LINE, bees);
        }
    }
}
