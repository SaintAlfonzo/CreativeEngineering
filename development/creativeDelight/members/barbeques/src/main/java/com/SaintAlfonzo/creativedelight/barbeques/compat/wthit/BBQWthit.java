package com.SaintAlfonzo.creativedelight.barbeques.compat.wthit;

import com.SaintAlfonzo.creativedelight.barbeques.BarbequesDelight;
import com.SaintAlfonzo.creativedelight.barbeques.block.GrillBlockEntity;
import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.IWailaCommonPlugin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

/**
 * WTHIT plugin, common half (barbeques_delight.md 4 step 17, ui/wthit #13): 1.3.0's Jade grill tooltip ported. The
 * server writes each slot's skewer, time, duration and flip state; the client half {@link BBQWthitClient} draws the line.
 */
public final class BBQWthit implements IWailaCommonPlugin {
    public static final ResourceLocation GRILL = BarbequesDelight.loc("grill");
    static final String KEY = "bbqGrill";

    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.localConfig(GRILL, true);
        registrar.blockData(new Data(), GrillBlockEntity.class);
    }

    private static final class Data implements IServerDataProvider<GrillBlockEntity> {
        @Override
        public void appendServerData(CompoundTag data, IServerAccessor<GrillBlockEntity> accessor, IPluginConfig config) {
            ListTag slots = new ListTag();
            for (GrillBlockEntity.ItemEntry e : accessor.getTarget().entries) {
                CompoundTag slot = new CompoundTag();
                if (!e.stack.isEmpty()) {
                    slot.put("Item", e.stack.save(new CompoundTag()));
                    slot.putInt("Time", e.time);
                    slot.putInt("Duration", e.duration);
                    slot.putBoolean("Flipped", e.flipped);
                    slot.putBoolean("Burnt", e.burnt);
                }
                slots.add(slot);
            }
            data.put(KEY, slots);
        }
    }
}
