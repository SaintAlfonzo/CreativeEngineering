package umpaz.brewinandchewin.forge.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;
import umpaz.brewinandchewin.client.gui.KegScreen;
import umpaz.brewinandchewin.client.gui.KegTooltip;
import umpaz.brewinandchewin.client.particle.DrunkBubbleParticle;
import umpaz.brewinandchewin.client.particle.RagingParticle;
import umpaz.brewinandchewin.client.recipebook.BnCRecipeBookCategories;
import umpaz.brewinandchewin.client.renderer.CoasterBlockEntityRenderer;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.BnCRecipeBookTypes;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.item.KegTooltipComponent;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.forge.client.gui.BnCHUDOverlays;
import umpaz.brewinandchewin.forge.client.platform.BnCClientPlatformHelperForge;
import vectorwing.farmersdelight.client.particle.SteamParticle;

/** Client mod-bus setup (port of BrewinAndChewinNeoForgeClient + BnCRecipeBookNeoForge + BnCClientSetup). */
public class BrewinAndChewinForgeClient {
   public static void init(IEventBus modBus) {
      BrewinAndChewinClient.init(new BnCClientPlatformHelperForge());
      BrewinAndChewin.isClient = true;
      BnCHUDOverlays.init(modBus);
      modBus.addListener(BrewinAndChewinForgeClient::clientSetup);
      modBus.addListener(BrewinAndChewinForgeClient::registerRenderers);
      modBus.addListener(BrewinAndChewinForgeClient::registerParticles);
      modBus.addListener(BrewinAndChewinForgeClient::registerTooltipComponents);
      modBus.addListener(BrewinAndChewinForgeClient::registerReloadListeners);
      modBus.addListener(BrewinAndChewinForgeClient::registerModels);
      modBus.addListener(BrewinAndChewinForgeClient::registerRecipeBooks);
      modBus.addListener(BrewinAndChewinForgeClient::stitchTextures);
      BnCClientForgeEvents.register();
   }

   private static void clientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> MenuScreens.register(BnCMenuTypes.KEG, KegScreen::new));
   }

   private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerBlockEntityRenderer(BnCBlockEntityTypes.COASTER, CoasterBlockEntityRenderer::new);
   }

   private static void registerParticles(RegisterParticleProvidersEvent event) {
      event.register(BnCParticleTypes.FOG, SteamParticle.Factory::new);
      event.register(BnCParticleTypes.DRUNK_BUBBLE, DrunkBubbleParticle.Factory::new);
      event.register(BnCParticleTypes.RAGING_STAGE_1, RagingParticle.Factory::new);
      event.register(BnCParticleTypes.RAGING_STAGE_2, RagingParticle.Factory::new);
      event.register(BnCParticleTypes.RAGING_STAGE_3, RagingParticle.Factory::new);
      event.register(BnCParticleTypes.RAGING_STAGE_4, RagingParticle.Factory::new);
   }

   private static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
      event.register(KegTooltipComponent.class, KegTooltip::new);
   }

   private static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
      event.registerReloadListener(BnCFluidItemDisplays.Loader.INSTANCE);
   }

   private static void registerModels(ModelEvent.RegisterAdditional event) {
      event.register(CoasterBlockEntityRenderer.COASTER_MODEL);
      event.register(CoasterBlockEntityRenderer.COASTER_TRAY_MODEL);
      CoasterBlockEntityRenderer.loadModelEntries(Minecraft.getInstance().getResourceManager()).forEach(event::register);
   }

   // 1.19.2 has no atlases/*.json: slot icons must be stitched into the block atlas by hand.
   private static void stitchTextures(TextureStitchEvent.Pre event) {
      if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
         event.addSprite(KegMenu.EMPTY_CONTAINER_SLOT_TANKARD);
      }
   }

   private static void registerRecipeBooks(RegisterRecipeBookCategoriesEvent event) {
      event.registerBookCategories(
         BnCRecipeBookTypes.FERMENTING,
         ImmutableList.of(
            BnCRecipeBookCategories.FERMENTING_SEARCH,
            BnCRecipeBookCategories.FERMENTING_DRINKS,
            BnCRecipeBookCategories.FERMENTING_MEALS,
            BnCRecipeBookCategories.FERMENTING_MISC
         )
      );
      event.registerAggregateCategory(
         BnCRecipeBookCategories.FERMENTING_SEARCH,
         ImmutableList.of(BnCRecipeBookCategories.FERMENTING_DRINKS, BnCRecipeBookCategories.FERMENTING_MEALS, BnCRecipeBookCategories.FERMENTING_MISC)
      );
      event.registerRecipeCategoryFinder(BnCRecipeTypes.FERMENTING, recipe -> {
         if (recipe instanceof KegFermentingRecipe fermentingRecipe && fermentingRecipe.getRecipeBookCategory() != null) {
            return switch (fermentingRecipe.getRecipeBookCategory()) {
               case MEALS -> BnCRecipeBookCategories.FERMENTING_MEALS;
               case DRINKS -> BnCRecipeBookCategories.FERMENTING_DRINKS;
               case MISC -> BnCRecipeBookCategories.FERMENTING_MISC;
            };
         }
         return null;
      });
      event.registerRecipeCategoryFinder(BnCRecipeTypes.KEG_POURING, recipe -> RecipeBookCategories.UNKNOWN);
   }
}
