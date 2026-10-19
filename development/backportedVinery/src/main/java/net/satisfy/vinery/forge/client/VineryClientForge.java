package net.satisfy.vinery.forge.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.satisfy.foundation.banner.CompletionistBannerRenderer;
import net.satisfy.foundation.client.armor.ArmorModels;
import net.satisfy.foundation.client.wood.WoodBoatRenderer;
import net.satisfy.foundation.client.wood.WoodClient;
import net.satisfy.foundation.storage.StorageBlockEntityRenderer;
import net.satisfy.foundation.storage.WallShelfRenderer;
import net.satisfy.vinery.client.gui.ApplePressGui;
import net.satisfy.vinery.client.gui.FermentationBarrelGui;
import net.satisfy.vinery.client.model.MuleModel;
import net.satisfy.vinery.client.model.StrawHatModel;
import net.satisfy.vinery.client.model.WinemakerBootsModel;
import net.satisfy.vinery.client.model.WinemakerChestplateModel;
import net.satisfy.vinery.client.model.WinemakerLeggingsModel;
import net.satisfy.vinery.client.render.block.GrapevinePotRenderer;
import net.satisfy.vinery.client.render.block.LatticeRenderer;
import net.satisfy.vinery.client.render.block.StackableLogRenderer;
import net.satisfy.vinery.client.render.block.StoragePotRenderer;
import net.satisfy.vinery.client.render.block.storage.BigBottleRenderer;
import net.satisfy.vinery.client.render.block.storage.BottleRenderer;
import net.satisfy.vinery.client.render.block.storage.WineBottleRenderer;
import net.satisfy.vinery.client.render.block.storage.WineBoxRenderer;
import net.satisfy.vinery.client.render.entity.MuleRenderer;
import net.satisfy.vinery.client.render.entity.WanderingWinemakerRenderer;
import net.satisfy.vinery.client.util.ExtraModels;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.block.BottleStorageBlock;
import net.satisfy.vinery.core.block.state.properties.VineryWoodType;
import net.satisfy.vinery.core.registry.EntityTypeRegistry;
import net.satisfy.vinery.core.registry.MenuTypeRegistry;
import net.satisfy.vinery.core.registry.StorageTypeRegistry;
import net.satisfy.vinery.legacy.LegacyStorageTypes;
import net.satisfy.vinery.legacy.client.LegacyFlowerBoxRenderer;
import net.satisfy.vinery.legacy.client.LegacyFlowerPotBlockEntityRenderer;
import net.satisfy.vinery.legacy.client.LegacyFourBottleRenderer;
import net.satisfy.vinery.legacy.client.LegacyNineBottleRenderer;
import net.satisfy.vinery.legacy.client.LegacyShelfRenderer;

import static net.satisfy.vinery.core.registry.ObjectRegistry.*;

/** Port: upstream's VineryClient (Architectury registries) as Forge mod-bus client events, plus the 1.3.10 keepers. */
@Mod.EventBusSubscriber(modid = Vinery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class VineryClientForge {
    private VineryClientForge() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            cutout(RED_GRAPE_BUSH.get(), WHITE_GRAPE_BUSH.get(), DARK_CHERRY_DOOR.get(), DARK_CHERRY_TRAPDOOR.get(), FERMENTATION_BARREL.get(),
                    MELLOHI_WINE.get(), CLARK_WINE.get(), BOLVAR_WINE.get(), CHERRY_WINE.get(),
                    LILITU_WINE.get(), CHENET_WINE.get(), NOIR_WINE.get(), APPLE_CIDER.get(),
                    APPLE_WINE.get(), SOLARIS_WINE.get(), JELLIE_WINE.get(), AEGIS_WINE.get(), KELP_CIDER.get(),
                    SAVANNA_RED_GRAPE_BUSH.get(), SAVANNA_WHITE_GRAPE_BUSH.get(),
                    CHORUS_WINE.get(), STAL_WINE.get(), MAGNETIC_WINE.get(), STRAD_WINE.get(), JUNGLE_WHITE_GRAPE_BUSH.get(),
                    JUNGLE_RED_GRAPE_BUSH.get(), TAIGA_RED_GRAPE_BUSH.get(), TAIGA_WHITE_GRAPE_BUSH.get(),
                    GRAPEVINE_STEM.get(), WINE_BOX.get(), DARK_CHERRY_WINE_RACK_MID.get(), DARK_CHERRY_WINE_RACK_BIG.get(),
                    APPLE_PRESS.get(), GRASS_SLAB.get(), DARK_CHERRY_SAPLING.get(), APPLE_TREE_SAPLING.get(),
                    STACKABLE_LOG.get(), APPLE_LEAVES.get(), POTTED_APPLE_TREE_SAPLING.get(), DARK_CHERRY_WINE_RACK_SMALL.get(),
                    POTTED_DARK_CHERRY_TREE_SAPLING.get(), RED_WINE.get(), DARK_CHERRY_CHAIR.get(), CRISTEL_WINE.get(),
                    VILLAGERS_FRIGHT.get(), EISWEIN.get(), CREEPERS_CRUSH.get(),
                    GLOWING_WINE.get(), JO_SPECIAL_MIXTURE.get(), BOTTLE_MOJANG_NOIR.get(),
                    DARK_CHERRY_TABLE.get(), OAK_WINE_RACK_MID.get(), DARK_OAK_WINE_RACK_MID.get(), BIRCH_WINE_RACK_MID.get(),
                    SPRUCE_WINE_RACK_MID.get(), JUNGLE_WINE_RACK_MID.get(), MANGROVE_WINE_RACK_MID.get(), BAMBOO_WINE_RACK_MID.get(),
                    ACACIA_WINE_RACK_MID.get(), OAK_LATTICE.get(), SPRUCE_LATTICE.get(),
                    BIRCH_LATTICE.get(), DARK_OAK_LATTICE.get(), BAMBOO_LATTICE.get(), ACACIA_LATTICE.get(), JUNGLE_LATTICE.get(),
                    MANGROVE_LATTICE.get(), DARK_CHERRY_LATTICE.get(), DARK_CHERRY_LEAVES.get(),
                    // 1.3.10 keepers (1.3.10's render types)
                    PRAETORIAN_WINE.get(), TABLE.get(), GRAPEVINE_LATTICE.get(), FLOWER_POT.get(), CHAIR.get(),
                    KITCHEN_SINK.get());
            for (Block block : new Block[]{WINDOW.get(), WINDOW_BLOCK.get()}) {
                ItemBlockRenderTypes.setRenderLayer(block, RenderType.translucent());
            }

            StorageBlockEntityRenderer.registerStorageType(StorageTypeRegistry.BIG_BOTTLE, new BigBottleRenderer());
            StorageBlockEntityRenderer.registerStorageType(StorageTypeRegistry.FOUR_BOTTLE, new BottleRenderer(BottleStorageBlock.Layout.FOUR));
            StorageBlockEntityRenderer.registerStorageType(StorageTypeRegistry.NINE_BOTTLE, new BottleRenderer(BottleStorageBlock.Layout.NINE));
            StorageBlockEntityRenderer.registerStorageType(StorageTypeRegistry.SHELF, new WallShelfRenderer());
            StorageBlockEntityRenderer.registerStorageType(StorageTypeRegistry.WINE_BOX, new WineBoxRenderer());
            StorageBlockEntityRenderer.registerStorageType(StorageTypeRegistry.WINE_BOTTLE, new WineBottleRenderer());
            StorageBlockEntityRenderer.registerStorageType(LegacyStorageTypes.SHELF, new LegacyShelfRenderer());
            StorageBlockEntityRenderer.registerStorageType(LegacyStorageTypes.NINE_BOTTLE, new LegacyNineBottleRenderer());
            StorageBlockEntityRenderer.registerStorageType(LegacyStorageTypes.FOUR_BOTTLE, new LegacyFourBottleRenderer());
            StorageBlockEntityRenderer.registerStorageType(LegacyStorageTypes.FLOWER_BOX, new LegacyFlowerBoxRenderer());

            MenuScreens.register(MenuTypeRegistry.FERMENTATION_BARREL_MENU.get(), FermentationBarrelGui::new);
            MenuScreens.register(MenuTypeRegistry.APPLE_PRESS_MENU.get(), ApplePressGui::new);

            WoodClient.registerSignMaterials(VineryWoodType.DARK_CHERRY);
            ArmorModels.register(StrawHatModel.LAYER_LOCATION, StrawHatModel::new, STRAW_HAT.get());
            ArmorModels.register(WinemakerChestplateModel.LAYER_LOCATION, WinemakerChestplateModel::new, WINEMAKER_APRON.get());
            ArmorModels.register(WinemakerLeggingsModel.LAYER_LOCATION, WinemakerLeggingsModel::new, WINEMAKER_LEGGINGS.get());
            ArmorModels.register(WinemakerBootsModel.LAYER_LOCATION, WinemakerBootsModel::new, WINEMAKER_BOOTS.get());
        });
    }

    private static void cutout(Block... blocks) {
        for (Block block : blocks) {
            ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
        }
    }

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> level == null || pos == null ? -1 : BiomeColors.getAverageGrassColor(level, pos), GRASS_SLAB.get());
        event.register((state, level, pos, tintIndex) -> level == null || pos == null ? -1 : BiomeColors.getAverageFoliageColor(level, pos),
                JUNGLE_RED_GRAPE_BUSH.get(), JUNGLE_WHITE_GRAPE_BUSH.get(), GRAPEVINE_LATTICE.get());
    }

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> GrassColor.get(0.5, 1.0), GRASS_SLAB.get());
    }

    @SubscribeEvent
    public static void onLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MuleModel.LAYER_LOCATION, MuleModel::getTexturedModelData);
        event.registerLayerDefinition(StrawHatModel.LAYER_LOCATION, StrawHatModel::createBodyLayer);
        event.registerLayerDefinition(WinemakerChestplateModel.LAYER_LOCATION, WinemakerChestplateModel::createBodyLayer);
        event.registerLayerDefinition(WinemakerLeggingsModel.LAYER_LOCATION, WinemakerLeggingsModel::createBodyLayer);
        event.registerLayerDefinition(WinemakerBootsModel.LAYER_LOCATION, WinemakerBootsModel::createBodyLayer);
        event.registerLayerDefinition(LatticeRenderer.LAYER_LOCATION, LatticeRenderer::getTexturedModelData);
        WoodClient.registerBoatLayers(event, VineryWoodType.DARK_CHERRY_BOAT);
    }

    @SubscribeEvent
    public static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityTypeRegistry.MULE.get(), MuleRenderer::new);
        event.registerEntityRenderer(EntityTypeRegistry.WANDERING_WINEMAKER.get(), WanderingWinemakerRenderer::new);
        event.registerEntityRenderer(EntityTypeRegistry.DARK_CHERRY_BOAT.get(), context -> new WoodBoatRenderer(context, false));
        event.registerEntityRenderer(EntityTypeRegistry.DARK_CHERRY_CHEST_BOAT.get(), context -> new WoodBoatRenderer(context, true));

        event.registerBlockEntityRenderer(EntityTypeRegistry.VINERY_STANDARD.get(), CompletionistBannerRenderer::new);
        event.registerBlockEntityRenderer(EntityTypeRegistry.STORAGE_ENTITY.get(), context -> new StorageBlockEntityRenderer());
        event.registerBlockEntityRenderer(EntityTypeRegistry.LATTICE.get(), LatticeRenderer::new);
        event.registerBlockEntityRenderer(EntityTypeRegistry.GRAPEVINE_POT.get(), GrapevinePotRenderer::new);
        event.registerBlockEntityRenderer(EntityTypeRegistry.STORAGE_POT_ENTITY.get(), StoragePotRenderer::new);
        event.registerBlockEntityRenderer(EntityTypeRegistry.STACKABLE_LOG.get(), StackableLogRenderer::new);
        event.registerBlockEntityRenderer(EntityTypeRegistry.MOD_SIGN.get(), SignRenderer::new);
        event.registerBlockEntityRenderer(EntityTypeRegistry.FLOWER_POT.get(), LegacyFlowerPotBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onAdditionalModels(ModelEvent.RegisterAdditional event) {
        ExtraModels.all().forEach(event::register);
    }
}
