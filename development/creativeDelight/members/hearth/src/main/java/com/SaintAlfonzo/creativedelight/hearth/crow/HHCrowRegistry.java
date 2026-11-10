package com.SaintAlfonzo.creativedelight.hearth.crow;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.registry.ModItems;

/** The crow mechanics on {@code alexsmobs:crow} and the scarecrow (plan_hearth_crow_salt.md sections 3, 4 and 8). */
public final class HHCrowRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HearthAndHarvestMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HearthAndHarvestMod.MODID);

    public static final RegistryObject<Block> SCARECROW = BLOCKS.register("scarecrow", () -> new ScarecrowBlock(Properties.copy(Blocks.HAY_BLOCK).noOcclusion()));
    public static final RegistryObject<Item> SCARECROW_ITEM = ITEMS.register("scarecrow", () -> new BlockItem(SCARECROW.get(), ModItems.basicItem()));

    private HHCrowRegistry() {
    }

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener(CrowEvents::onRegisterCapabilities);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, CrowEvents::onAttachCapabilities);
        MinecraftForge.EVENT_BUS.addListener(CrowEvents::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(CrowEvents::onLivingTick);
        MinecraftForge.EVENT_BUS.addListener(CrowEvents::onLivingHurt);
        MinecraftForge.EVENT_BUS.addListener(CrowEvents::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(CrowEvents::onAllowDespawn);
        MinecraftForge.EVENT_BUS.addListener(CrowEvents::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(CrowEvents::onCheckSpawn);
    }
}
