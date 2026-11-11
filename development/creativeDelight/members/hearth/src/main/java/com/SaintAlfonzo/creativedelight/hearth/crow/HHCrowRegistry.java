package com.SaintAlfonzo.creativedelight.hearth.crow;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.entity.HHEntities;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.registry.ModItems;

/** The crow ({@code hearthandharvest:crow}), its spawn egg and the scarecrow (plan_hearth_crow_salt.md, plan_hearth_crow_swap.md). */
public final class HHCrowRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HearthAndHarvestMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HearthAndHarvestMod.MODID);

    public static final RegistryObject<Block> SCARECROW = BLOCKS.register("scarecrow", () -> new ScarecrowBlock(Properties.copy(Blocks.HAY_BLOCK).noOcclusion()));
    public static final RegistryObject<Item> SCARECROW_ITEM = ITEMS.register("scarecrow", () -> new BlockItem(SCARECROW.get(), ModItems.basicItem()));
    public static final RegistryObject<Item> CROW_SPAWN_EGG = ITEMS.register("crow_spawn_egg", () -> new ForgeSpawnEggItem(HHEntities.CROW, 1843248, 856348, ModItems.basicItem()));

    private HHCrowRegistry() {
    }

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener((EntityAttributeCreationEvent event) -> event.put(HHEntities.CROW.get(), CrowEntity.createAttributes().build()));
        // No biome spawn entries; the placement rule only matters to commands and other mods' spawners.
        bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> SpawnPlacements.register(HHEntities.CROW.get(),
                SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, level, reason, pos, random) -> level.getRawBrightness(pos, 0) > 8)));
        MinecraftForge.EVENT_BUS.addListener(CrowEvents::onLivingTick);
        MinecraftForge.EVENT_BUS.addListener(CrowEvents::onLivingHurt);
        MinecraftForge.EVENT_BUS.addListener(CrowEvents::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(CrowEvents::onAllowDespawn);
        MinecraftForge.EVENT_BUS.addListener(CrowEvents::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(CrowShoulder::onPlayerTick);
    }
}
