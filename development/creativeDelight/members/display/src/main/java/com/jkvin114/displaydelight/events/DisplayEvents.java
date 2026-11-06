package com.jkvin114.displaydelight.events;

import com.jkvin114.displaydelight.block.AbstractStackablePlatedFoodBlock;
import com.jkvin114.displaydelight.block.SmallPlatedFoodBlock;
import com.jkvin114.displaydelight.init.BlockAssociations;
import com.jkvin114.displaydelight.init.DisplayBlocks;
import com.jkvin114.displaydelight.init.DisplayConfig;
import com.jkvin114.displaydelight.init.DisplayItems;
import com.jkvin114.displaydelight.init.DisplayTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Start;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.LevelEvent.Load;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "displaydelight"
)
public class DisplayEvents {
   private static boolean shouldCancelUseEventClient = false;
   private static boolean shouldCancelUseEventServer = false;

   @SubscribeEvent
   public static void on(Start event) {
      if (!(Boolean)DisplayConfig.DISABLE_EATING_ANIMATION_FIX.get()) {
         if (event.getEntity() instanceof Player && isPlateDisplayable(event.getItem())) {
            Level lvl = event.getEntity().getLevel();
            if (shouldCancelUseEventServer && !lvl.isClientSide) {
               shouldCancelUseEventServer = false;
               event.setCanceled(true);
            }

            if (shouldCancelUseEventClient && lvl.isClientSide) {
               shouldCancelUseEventClient = false;
               event.setCanceled(true);
            }
         }
      }
   }

   private static boolean isPlateDisplayable(ItemStack item) {
      return item.is(DisplayTags.SMALL_PLATE_DISPLAYABLE) || item.is(DisplayTags.PLATE_DISPLAYABLE);
   }

   @SubscribeEvent
   public static void onCheck(RightClickBlock event) {
      if (event.getLevel().isClientSide && InterationManager.testInsertPlate(event.getEntity(), event.getLevel(), event.getHitVec(), event.getHand())) {
         event.setCanceled(true);
         event.setCancellationResult(InteractionResult.PASS);
      }

      if (event.getLevel() instanceof ServerLevel slevel && event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().isEmpty()) {
         InterationManager.tryTakeItemWithBareHand(event.getEntity(), slevel, event.getHitVec());
         return;
      }

      if (event.getLevel() instanceof ServerLevel level) {
         boolean handled = false;
         if (event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().isEmpty()) {
            handled = InterationManager.tryTakeItemWithBareHand(event.getEntity(), level, event.getHitVec());
         }

         if (!handled && event.getItemStack().canPerformAction(ToolActions.AXE_DIG) && !(Boolean)DisplayConfig.DISABLE_HIDE_PLATE_INTERACTION.get()) {
            handled = InterationManager.tryTakePlateWithAxe(event.getEntity(), level, event.getHitVec());
            if (handled) {
               event.getEntity().swing(event.getHand(), true);
            }
         }

         if (!handled && event.getItemStack().getItem() == DisplayItems.PLATE.get() && !(Boolean)DisplayConfig.DISABLE_PUT_PLATE_INTERACTION.get()) {
            handled = InterationManager.tryInsertPlate(event.getEntity(), level, event.getHitVec(), event.getHand());
            if (handled) {
               event.getEntity().swing(event.getHand(), true);
            }
         }

         if (!handled && event.getItemStack().getItem() == DisplayItems.SMALL_PLATE.get() && !(Boolean)DisplayConfig.DISABLE_PUT_PLATE_INTERACTION.get()) {
            handled = InterationManager.tryInsertSmallPlate(event.getEntity(), level, event.getHitVec(), event.getHand());
            if (handled) {
               event.getEntity().swing(event.getHand(), true);
            }
         }

         if (!handled && event.getItemStack().is(DisplayTags.SMALL_PLATE_DISPLAYABLE)) {
            handled = InterationManager.tryPlaceItemOnSmallPlate(event.getEntity(), level, event.getHitVec(), event.getHand() == InteractionHand.MAIN_HAND);
         }

         if (!handled && event.getItemStack().is(DisplayTags.PLATE_DISPLAYABLE)) {
            handled = InterationManager.tryPlaceItemOnPlate(event.getEntity(), level, event.getHitVec(), event.getHand() == InteractionHand.MAIN_HAND);
         }

         if (!handled && event.getItemStack().is(DisplayTags.DISPLAYABLE)) {
            handled = InterationManager.tryPlaceItem(event.getEntity(), level, event.getHitVec(), event.getHand() == InteractionHand.MAIN_HAND);
         }

         event.setResult(Result.ALLOW);
         if (handled) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
         }
      } else {
         BlockPos pos = event.getHitVec().getBlockPos();
         BlockState state = event.getLevel().getBlockState(pos);
         if (isPlateDisplayable(event.getItemStack())) {
            if (state.is((Block)DisplayBlocks.SMALL_PLATE.get())
               || state.is((Block)DisplayBlocks.PLATE.get())
               || state.getBlock() instanceof AbstractStackablePlatedFoodBlock
               || state.getBlock() instanceof SmallPlatedFoodBlock) {
               shouldCancelUseEventClient = true;
               shouldCancelUseEventServer = true;
            }
         }
      }
   }

   @SubscribeEvent
   public static void onWorldLoad(Load event) {
      if (event.getLevel() instanceof ServerLevel lvl) {
         BlockAssociations.initialize(DisplayItems.GetAll());
      }
   }
}
