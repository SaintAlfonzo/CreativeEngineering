package com.SaintAlfonzo.creativedelight.compat.cookingforblockheads;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.client.FridgeTopFill;
import com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.client.OvenRaise;
import com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.kitchen.CornerStorage;
import com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.kitchen.CounterToppers;
import com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.rack.BARacks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * The Cooking for Blockheads compartment of Creative Delight (feature_writeup/kitchen.md 9; plan_cb_publish.md 3b): the
 * pack's Cooking for Blockheads additions and changes, the blockheads_additions member until M8 (2026-11-04). Cooking for
 * Blockheads (All Rights Reserved) stays its own jar; this is our own code and art only (GPL-3.0-only).
 * <ul>
 * <li>{@code rack}: spice racks, tool racks and fruit baskets in 19 woods, {@code creativedelight:<wood>_spice_rack} and so
 * on (they were {@code cookingforblockheads:*} up to M8; data/creativedelight/swaps/compartments.json carries them over).</li>
 * <li>{@code kitchen}: corner storage, counter toppers (cutting board, plates, coaster on a counter); the oven fuel and
 * smoking mixins; the oven lit state (Farmer's Delight heat source).</li>
 * <li>{@code client}: the 2-tall fridge filled to a full block; the oven cook surface raised to the block top.</li>
 * </ul>
 * Load guard: {@link #init} is called by {@link CreativeDelight} only when Cooking for Blockheads and Balm are loaded and
 * the {@code [compartments] cookingforblockheads} switch is on ({@code compat.Compartments}). Only this package and
 * {@code mixin.cookingforblockheads} name CfB's types, so without CfB the JVM never loads one. The mixins
 * ({@code mixin.cookingforblockheads}, the cutting board and coaster offsets) are gated by {@code CDMixinPlugin}; the
 * assets and loot tables are the built-in pack {@code packs/cookingforblockheads/} ({@code compat.CompartmentPacks}).
 * The kitchen code lived in patcherMod until 2026-10-31; its saved data and tag ids keep their creativeengineering names.
 */
public final class CfbCompartment {
    /** Log tag of this compartment. */
    public static final String MODID = "creativedelight/cookingforblockheads";
    /** Registry namespace of the blocks and items here (Creative Delight's own since M8, plan D6). */
    public static final String NAMESPACE = CreativeDelight.MOD_ID;

    private CfbCompartment() {
    }

    public static void init(IEventBus bus) {
        BARacks.BLOCKS.register(bus);
        BARacks.ITEMS.register(bus);
        bus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(BARacks::allowOnBlockEntityTypes));
        MinecraftForge.EVENT_BUS.register(CornerStorage.class);
        // The per-state topper cache resets on tag reload.
        CounterToppers.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FridgeTopFill.register(bus));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> OvenRaise.register(bus));
    }
}
