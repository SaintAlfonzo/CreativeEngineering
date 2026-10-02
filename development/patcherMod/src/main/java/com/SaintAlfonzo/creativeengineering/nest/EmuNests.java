package com.SaintAlfonzo.creativeengineering.nest;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.teamabnormals.blueprint.core.util.DataUtil;
import com.teamabnormals.incubation.common.block.BirdNestBlock;
import com.teamabnormals.incubation.common.block.EmptyNestBlock;
import com.teamabnormals.incubation.core.registry.IncubationBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Incubation-style nest for Alex's Mobs emus (the emu itself becomes an Incubation EggLayer in
 * mixin/EntityEmuEggLayerMixin). Same pieces as Incubation's own twig nest:
 * - emu_nest: empty nest (EmptyNestBlock), has an item, crafted from bark (misc_mods.js).
 * - emu_egg_nest: the 1-6 egg nest (BirdNestBlock), no item. Its constructor registers it in
 *   emu_nest's egg -> nest map, which is how emu_nest knows emu eggs turn it into this block.
 * Collection is Incubation's: BirdNestBlockEntity pushes eggs into a hopper directly below.
 * Only emus use it: mixin/LayEggInNestGoalMixin keeps birds away from empty nests with no
 * variant for their egg (chickens off emu_nest, emus off hay/twig nests).
 */
public class EmuNests {

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CreativeEngineering.MODID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreativeEngineering.MODID);

    // Incubation's own twig nest properties (hardness, sound, no occlusion), shared the way it
    // shares them across its twig nests.
    public static final RegistryObject<Block> EMU_NEST = BLOCKS.register("emu_nest",
            () -> new EmptyNestBlock(IncubationBlocks.IncubationProperties.TWIG_NEST));

    // Registered after EMU_NEST in the same register, so EMU_NEST.get() is already bound here.
    public static final RegistryObject<Block> EMU_EGG_NEST = BLOCKS.register("emu_egg_nest",
            () -> new BirdNestBlock(new ResourceLocation("alexsmobs", "emu_egg"),
                    (EmptyNestBlock) EMU_NEST.get(), IncubationBlocks.IncubationProperties.TWIG_NEST));

    public static final RegistryObject<Item> EMU_NEST_ITEM = ITEMS.register("emu_nest",
            () -> new BlockItem(EMU_NEST.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    /** Same fire values Incubation gives its nests (IncubationCompat.registerFlammables). Run via enqueueWork. */
    public static void registerFlammables() {
        DataUtil.registerFlammable(EMU_NEST.get(), 60, 20);
        DataUtil.registerFlammable(EMU_EGG_NEST.get(), 60, 20);
    }
}
