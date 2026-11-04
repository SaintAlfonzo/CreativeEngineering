package net.ribs.vintagedelight.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import net.ribs.vintagedelight.VintageDelight;
import net.ribs.vintagedelight.block.ModBlocks;
import net.ribs.vintagedelight.block.entity.ModBlockEntities;
import net.ribs.vintagedelight.block.entity.renderer.FermentingJarBlockEntityRenderer;
import net.ribs.vintagedelight.screen.FermentingJarScreen;
import net.ribs.vintagedelight.screen.ModMenuTypes;

// Upstream's client events (screen, render layers, jar renderer). The render layers are set in code, as upstream does.
@Mod.EventBusSubscriber(modid = VintageDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenuTypes.FERMENTING_MENU.get(), FermentingJarScreen::new));
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FERMENTING_JAR.get(), RenderType.translucent());
        for (RegistryObject<Block> cutout : java.util.List.of(ModBlocks.MAGIC_PEANUT, ModBlocks.OAT_CROP, ModBlocks.WILD_OATS,
            ModBlocks.PEANUT_CROP, ModBlocks.WILD_PEANUTS, ModBlocks.GHOST_PEPPER_CROP, ModBlocks.WILD_GHOST_PEPPERS,
            ModBlocks.CUCUMBER_CROP, ModBlocks.WILD_CUCUMBERS, ModBlocks.EMPTY_MASON_JAR, ModBlocks.PEPPER_JAM_JAR,
            ModBlocks.SWEET_BERRY_JAM_JAR, ModBlocks.GLOW_BERRY_JAM_JAR, ModBlocks.APPLE_SAUCE_JAR, ModBlocks.NUT_MASH_JAR,
            ModBlocks.RELISH_JAR, ModBlocks.VINEGAR_JAR, ModBlocks.HONEY_JAR,
            ModBlocks.GREEN_GRAPE_JAM_JAR, ModBlocks.RED_GRAPE_JAM_JAR, ModBlocks.CHERRY_JAM_JAR,
            ModBlocks.SALMONBERRY_JAM_JAR, ModBlocks.RASPBERRY_JAM_JAR)) {
            ItemBlockRenderTypes.setRenderLayer(cutout.get(), RenderType.cutout());
        }
        // Pack jam line: the new jars and the placed Fruits Delight jams (jam blocks set translucent in their models)
        for (Block jar : net.ribs.vintagedelight.jam.PackJams.cutoutBlocks()) {
            ItemBlockRenderTypes.setRenderLayer(jar, RenderType.cutout());
        }
    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FERMENTING_JAR_BE.get(), FermentingJarBlockEntityRenderer::new);
    }
}
