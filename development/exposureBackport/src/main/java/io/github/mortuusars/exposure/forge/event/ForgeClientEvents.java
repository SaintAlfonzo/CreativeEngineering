package io.github.mortuusars.exposure.forge.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.client.ExposureClientReloadListener;
import io.github.mortuusars.exposure.client.gui.screen.ItemRenameScreen;
import io.github.mortuusars.exposure.client.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.AlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.LecternAlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraAttachmentsScreen;
import io.github.mortuusars.exposure.client.gui.tooltip.CameraStandTooltip;
import io.github.mortuusars.exposure.client.gui.tooltip.PhotographClientTooltip;
import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import io.github.mortuusars.exposure.client.render.CameraStandEntityRenderer;
import io.github.mortuusars.exposure.client.render.GlassPhotographFrameEntityRenderer;
import io.github.mortuusars.exposure.client.render.PhotographFrameEntityRenderer;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.gui.screens.MenuScreens;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.backport.client.DeltaTracker;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.input.MouseHandler;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.event.ClientEvents;
import io.github.mortuusars.exposure.forge.client.FirstPersonPhotographRenderer;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Upstream {@code neoforge.event.NeoForgeClientEvents}. */
public class ForgeClientEvents {
   @Mod.EventBusSubscriber(modid = Exposure.ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
   public static class GameBus {
      @SubscribeEvent
      public static void onRenderGuiPost(RenderGuiEvent.Post event) {
         CameraStandTooltip.render(new GuiGraphics(event.getPoseStack()), DeltaTracker.of(event.getPartialTick()));
      }

      // ---- port: upstream mixin.client.GuiMixin (ForgeGui overrides Gui.render, so it has to be events) ----

      @SubscribeEvent
      public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
         if (CameraClient.viewfinder() != null && CameraClient.viewfinder().isLookingThrough()) {
            CameraClient.viewfinder().overlay().render(new GuiGraphics(event.getPoseStack()), DeltaTracker.of(event.getPartialTick()));
            if (Config.Client.HIDE_HUD_WHILE_IN_VIEWFINDER.get()) {
               event.setCanceled(true);
            }
         }
      }

      @SubscribeEvent
      public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
         if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) {
            return;
         }
         LocalPlayer player = Minecrft.player();
         if (player != null && Config.Client.PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR.get()
                 && player.getXRot() > 25.0F
                 && (player.getMainHandItem().getItem() instanceof PhotographItem || player.getMainHandItem().getItem() instanceof StackedPhotographsItem)
                 && player.getOffhandItem().isEmpty()) {
            event.setCanceled(true);
         }
      }

      // ---- port: upstream mixin.client.ItemInHandRendererMixin ----

      @SubscribeEvent
      public static void onRenderHand(RenderHandEvent event) {
         FirstPersonPhotographRenderer.onRenderHand(event);
      }

      // ---- port: upstream mixin.client.ItemFrameRendererMixin ----

      @SubscribeEvent
      public static void onRenderItemInFrame(RenderItemInFrameEvent event) {
         PoseStack poseStack = event.getPoseStack();
         poseStack.pushPose();
         poseStack.scale(0.5F, 0.5F, 0.5F); // vanilla applies this right before renderStatic, where upstream hooks in
         boolean rendered = ClientEvents.renderItemFrameItem(event.getItemFrameEntity(), poseStack, event.getMultiBufferSource(), event.getPackedLight());
         poseStack.popPose();
         if (rendered) {
            event.setCanceled(true);
         }
      }

      // ---- port: upstream mixin.client.MouseHandlerMixin (scroll, button press) ----

      @SubscribeEvent
      public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
         if (event.getScrollDelta() != 0.0 && MouseHandler.scrolled(event.getScrollDelta())) {
            event.setCanceled(true);
         }
      }

      @SubscribeEvent
      public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
         CameraClient.removeViewfinder();
      }

      @SubscribeEvent
      public static void onMouseButton(InputEvent.MouseButton.Pre event) {
         if (MouseHandler.buttonPressed(event.getButton(), event.getAction(), event.getModifiers())) {
            event.setCanceled(true);
         }
      }
   }

   @Mod.EventBusSubscriber(modid = Exposure.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
   public static class ModBus {
      @SubscribeEvent
      public static void clientSetup(FMLClientSetupEvent event) {
         event.enqueueWork(() -> {
            ExposureClient.init();
            // 1.19.2 has no RegisterMenuScreensEvent: screens are bound here, on the main thread.
            MenuScreens.register(Exposure.MenuTypes.CAMERA_IN_HAND.get(), CameraAttachmentsScreen::new);
            MenuScreens.register(Exposure.MenuTypes.CAMERA_ON_STAND.get(), CameraAttachmentsScreen::new);
            MenuScreens.register(Exposure.MenuTypes.ALBUM.get(), AlbumScreen::new);
            MenuScreens.register(Exposure.MenuTypes.LECTERN_ALBUM.get(), LecternAlbumScreen::new);
            MenuScreens.register(Exposure.MenuTypes.LIGHTROOM.get(), LightroomScreen::new);
            MenuScreens.register(Exposure.MenuTypes.ITEM_RENAME.get(), ItemRenameScreen::new);
         });
      }

      @SubscribeEvent
      public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
         event.register(CameraItem::getGlassTintColor, Exposure.Items.CAMERA.get());
      }

      @SubscribeEvent
      public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
         event.registerEntityRenderer(Exposure.EntityTypes.PHOTOGRAPH_FRAME.get(), PhotographFrameEntityRenderer::new);
         event.registerEntityRenderer(Exposure.EntityTypes.CLEAR_PHOTOGRAPH_FRAME.get(), GlassPhotographFrameEntityRenderer::new);
         event.registerEntityRenderer(Exposure.EntityTypes.CAMERA_STAND.get(), CameraStandEntityRenderer::new);
      }

      @SubscribeEvent
      public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
         event.register(PhotographTooltip.class, PhotographClientTooltip::new);
      }

      @SubscribeEvent
      public static void registerResourceReloadListeners(RegisterClientReloadListenersEvent event) {
         event.registerReloadListener(new ExposureClientReloadListener());
      }

      @SubscribeEvent
      public static void registerModels(ModelEvent.RegisterAdditional event) {
         ExposureClient.Models.MODELS.forEach(event::register);
      }

      @SubscribeEvent
      public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
         KeyboardHandler.registerKeymappings((key) -> {
            event.register(key);
            return key;
         });
      }
   }
}
