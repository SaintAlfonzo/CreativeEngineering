package com.SaintAlfonzo.creativedelight.compat.incubation.nest;

import com.SaintAlfonzo.creativedelight.compat.incubation.IncubationCompartment;
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
 * Incubation-style nests for Untitled Duck Mod's ducks and geese (both become Incubation
 * EggLayers in mixin/DuckEntityEggLayerMixin and mixin/GooseEntityEggLayerMixin). Same pieces as
 * {@link EmuNests}, once per bird:
 * - duck_nest / goose_nest: the empty nest (EmptyNestBlock), has an item, crafted from grass and
 *   tall grass (misc_mods.js).
 * - duck_egg_nest / goose_egg_nest: the 1-6 egg nest (BirdNestBlock), no item. Its constructor
 *   registers it in its empty nest's egg -> nest map, which is how the empty nest knows that bird's
 *   eggs turn it into this block.
 * Collection is Incubation's: BirdNestBlockEntity pushes eggs into a hopper directly below.
 * Each bird only uses its own nest: mixin/LayEggInNestGoalMixin keeps birds away from empty nests
 * with no variant for their egg (so geese skip duck nests, ducks skip hay nests, and so on).
 * Look: duck eggs on Incubation's duck-egg geometry, goose eggs on its turkey-egg geometry, nest
 * base/item textures copied from the hay nest (each bird has its own PNGs, so any of them can be
 * repainted or overridden from kubejs/assets/creativedelight/ without touching the others).
 */
public class WaterfowlNests {

    private static final String EGG_MOD = "untitledduckmod";

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, IncubationCompartment.ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IncubationCompartment.ID);

    // Incubation's own hay nest properties (hardness, sound, no occlusion).
    public static final RegistryObject<Block> DUCK_NEST = BLOCKS.register("duck_nest",
            () -> new EmptyNestBlock(IncubationBlocks.IncubationProperties.HAY_NEST));

    // Registered after DUCK_NEST in the same register, so DUCK_NEST.get() is already bound here.
    public static final RegistryObject<Block> DUCK_EGG_NEST = BLOCKS.register("duck_egg_nest",
            () -> new BirdNestBlock(new ResourceLocation(EGG_MOD, "duck_egg"),
                    (EmptyNestBlock) DUCK_NEST.get(), IncubationBlocks.IncubationProperties.HAY_NEST));

    public static final RegistryObject<Block> GOOSE_NEST = BLOCKS.register("goose_nest",
            () -> new EmptyNestBlock(IncubationBlocks.IncubationProperties.HAY_NEST));

    public static final RegistryObject<Block> GOOSE_EGG_NEST = BLOCKS.register("goose_egg_nest",
            () -> new BirdNestBlock(new ResourceLocation(EGG_MOD, "goose_egg"),
                    (EmptyNestBlock) GOOSE_NEST.get(), IncubationBlocks.IncubationProperties.HAY_NEST));

    public static final RegistryObject<Item> DUCK_NEST_ITEM = ITEMS.register("duck_nest",
            () -> new BlockItem(DUCK_NEST.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

    public static final RegistryObject<Item> GOOSE_NEST_ITEM = ITEMS.register("goose_nest",
            () -> new BlockItem(GOOSE_NEST.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    /** Same fire values Incubation gives its nests (IncubationCompat.registerFlammables). Run via enqueueWork. */
    public static void registerFlammables() {
        DataUtil.registerFlammable(DUCK_NEST.get(), 60, 20);
        DataUtil.registerFlammable(DUCK_EGG_NEST.get(), 60, 20);
        DataUtil.registerFlammable(GOOSE_NEST.get(), 60, 20);
        DataUtil.registerFlammable(GOOSE_EGG_NEST.get(), 60, 20);
    }
}
