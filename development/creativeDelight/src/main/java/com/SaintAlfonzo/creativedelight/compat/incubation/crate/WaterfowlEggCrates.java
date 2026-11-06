package com.SaintAlfonzo.creativedelight.compat.incubation.crate;

import com.SaintAlfonzo.creativedelight.compat.incubation.IncubationCompartment;
import com.teamabnormals.blueprint.core.util.DataUtil;
import com.teamabnormals.incubation.core.registry.IncubationBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Duck and goose egg crates (Untitled Duck Mod eggs): plain storage blocks, the same as Incubation's
 * chicken egg crate. 9 eggs <-> 1 crate, recipes in packs/incubation/untitledduckmod/data.
 */
public class WaterfowlEggCrates {

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, IncubationCompartment.ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IncubationCompartment.ID);

    // Copies the chicken egg crate (wood, 1.5 strength, wood sound); Incubation's blocks register first
    // (mods.toml ordering AFTER incubation).
    public static final RegistryObject<Block> DUCK_EGG_CRATE = BLOCKS.register("duck_egg_crate",
            () -> new Block(BlockBehaviour.Properties.copy(IncubationBlocks.CHICKEN_EGG_CRATE.get())));
    public static final RegistryObject<Block> GOOSE_EGG_CRATE = BLOCKS.register("goose_egg_crate",
            () -> new Block(BlockBehaviour.Properties.copy(IncubationBlocks.CHICKEN_EGG_CRATE.get())));

    public static final RegistryObject<Item> DUCK_EGG_CRATE_ITEM = ITEMS.register("duck_egg_crate",
            () -> new BlockItem(DUCK_EGG_CRATE.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
    public static final RegistryObject<Item> GOOSE_EGG_CRATE_ITEM = ITEMS.register("goose_egg_crate",
            () -> new BlockItem(GOOSE_EGG_CRATE.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    /** Same fire values Incubation gives its egg crates. Run via enqueueWork. */
    public static void registerFlammables() {
        DataUtil.registerFlammable(DUCK_EGG_CRATE.get(), 5, 20);
        DataUtil.registerFlammable(GOOSE_EGG_CRATE.get(), 5, 20);
    }
}
