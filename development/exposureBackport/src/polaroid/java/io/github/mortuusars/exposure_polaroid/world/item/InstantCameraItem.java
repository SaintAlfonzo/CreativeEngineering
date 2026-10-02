package io.github.mortuusars.exposure_polaroid.world.item;

import io.github.mortuusars.exposure.Exposure.CriteriaTriggers;
import io.github.mortuusars.exposure.Exposure.DataComponents;
import io.github.mortuusars.exposure.Exposure.Items;
import io.github.mortuusars.exposure.Exposure.SoundEvents;
import io.github.mortuusars.exposure.advancements.trigger.FramePrintedTrigger;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.Lenses;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.exposure.world.item.camera.Flash;
import io.github.mortuusars.exposure.world.item.camera.Shutter;
import io.github.mortuusars.exposure_polaroid.Config;
import io.github.mortuusars.exposure_polaroid.ExposurePolaroid;
import io.github.mortuusars.exposure_polaroid.world.camera.PolaroidFrameExtraData;
import io.github.mortuusars.exposure_polaroid.world.item.camera.InstantCameraAttachment;
import io.github.mortuusars.exposure_polaroid.world.item.camera.InstantCameraShutter;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InstantCameraItem extends CameraItem {
   public static final ResourceLocation CAPTURE_TYPE = ExposurePolaroid.resource("instant_camera");

   public InstantCameraItem(Properties properties) {
      super(properties);
   }

   protected Shutter createShutter() {
      return new InstantCameraShutter();
   }

   protected Flash createFlash() {
      return new Flash() {
         public boolean isAvailable(ItemStack camera) {
            return true;
         }
      };
   }

   protected List<ShutterSpeed> defineShutterSpeeds() {
      return List.of(
         new ShutterSpeed("1/200"),
         new ShutterSpeed("1/111"),
         new ShutterSpeed("1/78"),
         new ShutterSpeed("1/60"),
         new ShutterSpeed("1/30"),
         new ShutterSpeed("1/20"),
         new ShutterSpeed("1/15")
      );
   }

   @NotNull
   protected List<Attachment<?>> defineAttachments() {
      return List.of(InstantCameraAttachment.INSTANT_SLIDE);
   }

   public Attachment<?> getFilmAttachment() {
      return InstantCameraAttachment.INSTANT_SLIDE;
   }

   public boolean hasAttachmentsMenu() {
      return false;
   }

   public ResourceLocation getCaptureType(ItemStack stack) {
      return CAPTURE_TYPE;
   }

   public float getScaleOnStand() {
      return 0.85F;
   }

   public SoundEvent getViewfinderOpenSound() {
      return ExposurePolaroid.SoundEvents.INSTANT_CAMERA_VIEWFINDER_OPEN.get();
   }

   public SoundEvent getViewfinderCloseSound() {
      return ExposurePolaroid.SoundEvents.INSTANT_CAMERA_VIEWFINDER_CLOSE.get();
   }

   public FocalRange getFocalRange(RegistryAccess registryAccess, ItemStack stack) {
      return !Attachment.LENS.isEmpty(stack)
         ? Attachment.LENS
            .map(
               stack,
               lensStack -> Lenses.getFocalRange(registryAccess, lensStack).orElse(FocalRange.parse((String)Config.Server.INSTANT_CAMERA_FOCAL_RANGE.get()))
            )
            .orElse(FocalRange.parse((String)Config.Server.INSTANT_CAMERA_FOCAL_RANGE.get()))
         : FocalRange.parse((String)Config.Server.INSTANT_CAMERA_FOCAL_RANGE.get());
   }

   public double getYPositionOffset(ItemStack stack) {
      return 0.0;
   }

   public float getCropFactor() {
      return 0.75F;
   }

   public int getMaxSlideCount() {
      return (Integer)InstantCameraAttachment.INSTANT_SLIDE.maxCount().get();
   }

   public int getRemainingSlides(ItemStack stack) {
      return InstantCameraAttachment.INSTANT_SLIDE.get(stack).getForReading().getCount();
   }

   public boolean isBarVisible(@NotNull ItemStack stack) {
      return (Boolean)Config.Client.INSTANT_CAMERA_SHOW_FULLNESS_BAR_ON_ITEM.get() && !InstantCameraAttachment.INSTANT_SLIDE.isEmpty(stack);
   }

   public int getBarWidth(@NotNull ItemStack stack) {
      return InstantCameraAttachment.INSTANT_SLIDE.map(stack, this::getSlideFullness).orElse(0);
   }

   protected int getSlideFullness(ItemStack stack) {
      return Math.min(1 + 12 * stack.getCount() / this.getMaxSlideCount(), 13);
   }

   public int getBarColor(@NotNull ItemStack stack) {
      ItemStack slide = InstantCameraAttachment.INSTANT_SLIDE.get(stack).getForReading();
      int max = this.getMaxSlideCount();
      float f = Math.max(0.0F, (float)slide.getCount() / (float)max);
      return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
   }

   public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> components, TooltipFlag tooltipFlag) {
      if ((Boolean)Config.Client.INSTANT_CAMERA_SHOW_SLIDES_COUNT_IN_TOOLTIP.get()) {
         InstantCameraAttachment.INSTANT_SLIDE.ifPresent(stack, (slideItem, slideStack) -> {
            int exposed = slideStack.getCount();
            int max = this.getMaxSlideCount();
            components.add(Component.translatable("item.exposure_polaroid.instant_camera.tooltip.slides", new Object[]{exposed, max}));
         });
      }

      if ((Boolean)Config.Client.INSTANT_CAMERA_SHOW_TOOLTIP_DETAILS.get()) {
         if (stack.getEntityRepresentation() instanceof CameraStandEntity) {
            if (Screen.hasShiftDown()) {
               components.add(Component.translatable("item.exposure_polaroid.instant_camera.tooltip.details_insert_on_stand"));
               components.add(Component.translatable("item.exposure_polaroid.instant_camera.tooltip.details_remove_on_stand"));
            } else {
               components.add(Component.translatable("tooltip.exposure.hold_for_details"));
            }

            return;
         }

         if (Screen.hasShiftDown()) {
            components.add(Component.translatable("item.exposure_polaroid.instant_camera.tooltip.details_insert"));
            components.add(Component.translatable("item.exposure_polaroid.instant_camera.tooltip.details_remove"));
         } else {
            components.add(Component.translatable("tooltip.exposure.hold_for_details"));
         }
      }
   }

   public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
      if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
         if (!this.getShutter().isOpen(stack) && !this.isOnCooldown((CameraHolder)player, stack)) {
            InteractionResult hotswap = this.hotswap((CameraHolder)player, stack, otherStack, access);
            if (hotswap == InteractionResult.FAIL) {
               return true;
            } else if (hotswap.consumesAction()) {
               // port: client side only (upstream also runs this on a server, where Minecrft can't load)
               if (player.isCreative() && player.level.isClientSide()) {
                  Minecrft.gameMode().handleCreativeModeItemAdd(stack, slot.index);
               }

               return true;
            } else {
               return false;
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (hand == InteractionHand.MAIN_HAND
         && player.getOffhandItem().getItem() instanceof CameraItem offhandCameraItem
         && offhandCameraItem.isActive(player.getOffhandItem())) {
         return InteractionResultHolder.pass(stack);
      } else {
         return !this.isActive(stack) ? this.activateInHand(player, stack, hand) : this.release((CameraHolder)player, stack);
      }
   }

   public InteractionResult handleStandSneakInteraction(CameraStandEntity stand, Player player, InteractionHand hand, ItemStack cameraStack) {
      return player.level.isClientSide() ? InteractionResult.CONSUME : super.handleStandSneakInteraction(stand, player, hand, cameraStack);
   }

   public boolean canTakePhoto(CameraHolder holder, ItemStack stack) {
      return !this.isOnCooldown(holder, stack)
         && !this.getTimer().isTicking(holder, stack)
         && !this.getShutter().isOpen(stack)
         && !InstantCameraAttachment.INSTANT_SLIDE.isEmpty(stack)
         && CameraInstances.canReleaseShutter(CameraId.ofStack(stack));
   }

   public int calculateCooldownAfterShot(ItemStack stack, CaptureParameters captureParameters) {
      return 40;
   }

   public boolean tick(CameraHolder holder, ItemStack stack) {
      boolean changed = super.tick(holder, stack);
      Frame frame = (Frame)DataComponents.PHOTOGRAPH_FRAME.get(stack);
      if (frame != null && !this.getShutter().isOpen(stack) && !this.isOnCooldown(holder, stack)) {
         DataComponents.PHOTOGRAPH_FRAME.remove(stack);
         this.printPhotograph(holder, stack, frame);
         return true;
      } else {
         return changed;
      }
   }

   protected void printPhotograph(CameraHolder holder, ItemStack stack, Frame frame) {
      Entity holderEntity = holder.asHolderEntity();
      Level level = holderEntity.level;
      ExposureType type = InstantCameraAttachment.INSTANT_SLIDE.map(stack, (i, s) -> i.getType()).orElse(ExposureType.COLOR);
      ItemStack photograph = new ItemStack((ItemLike)Items.PHOTOGRAPH.get());
      DataComponents.PHOTOGRAPH_FRAME.set(photograph, frame);
      DataComponents.PHOTOGRAPH_TYPE.set(photograph, type);
      if (holder instanceof Player player) {
         if (!player.isCreative()) {
            ItemStack slide = InstantCameraAttachment.INSTANT_SLIDE.get(stack).getCopy();
            slide.shrink(1);
            slide = slide.isEmpty() ? ItemStack.EMPTY : slide;
            InstantCameraAttachment.INSTANT_SLIDE.set(stack, slide);
         }

         boolean placedInInventory = false;

         for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item.isEmpty()) {
               player.getInventory().setItem(slot, photograph);
               photograph.setPopTime(5);
               placedInInventory = true;
            } else if (item.getItem() instanceof PhotographItem) {
               StackedPhotographsItem stackedPhotographsItem = (StackedPhotographsItem)Items.STACKED_PHOTOGRAPHS.get();
               ItemStack stackedPhotographsStack = new ItemStack(stackedPhotographsItem);
               stackedPhotographsItem.addPhotographOnTop(stackedPhotographsStack, item);
               stackedPhotographsItem.addPhotographOnTop(stackedPhotographsStack, photograph);
               player.getInventory().setItem(slot, stackedPhotographsStack);
               stackedPhotographsStack.setPopTime(5);
               placedInInventory = true;
            } else if (item.getItem() instanceof StackedPhotographsItem stackedPhotographs && stackedPhotographs.canAddPhotograph(item)) {
               stackedPhotographs.addPhotographOnTop(item, photograph);
               item.setPopTime(5);
               placedInInventory = true;
            }

            if (placedInInventory) {
               level.playSound(
                  player, player, (SoundEvent)SoundEvents.PHOTOGRAPH_RUSTLE.get(), SoundSource.PLAYERS, 0.6F, level.getRandom().nextFloat() * 0.2F + 0.9F
               );
               break;
            }
         }

         if (!level.isClientSide && !placedInInventory) {
            ItemEntity itemEntity = player.drop(photograph, true, false);
            if (itemEntity != null) {
               itemEntity.setPickUpDelay(5);
            }
         }
      } else {
         ItemStack slide = InstantCameraAttachment.INSTANT_SLIDE.get(stack).getCopy();
         slide.shrink(1);
         slide = slide.isEmpty() ? ItemStack.EMPTY : slide;
         InstantCameraAttachment.INSTANT_SLIDE.set(stack, slide);
         if (!photograph.isEmpty() && !level.isClientSide()) {
            Vec3 look = holderEntity.getLookAngle().scale(0.2F);
            Vec3 pos = holderEntity.getEyePosition();
            ItemEntity itemEntity = new ItemEntity(
               level, pos.x + look.x * 2.0, pos.y + look.y * 2.0 - 0.3F, pos.z + look.z * 2.0, photograph, look.x * 0.5, look.y * 0.5, look.z * 0.5
            );
            itemEntity.setPickUpDelay(5);
            level.addFreshEntity(itemEntity);
         }
      }

      holder.getServerPlayerAwardedForExposure()
         .ifPresent(
            serverPlayer -> ((FramePrintedTrigger)CriteriaTriggers.FRAME_PRINTED.get()).trigger(serverPlayer, holderEntity.blockPosition(), frame, photograph)
         );
      if (!level.isClientSide) {
         level.playSound(
            null, holderEntity, (SoundEvent)SoundEvents.PHOTOGRAPH_RUSTLE.get(), SoundSource.PLAYERS, 0.6F, level.getRandom().nextFloat() * 0.2F + 1.2F
         );
      }
   }

   protected void addFrameExtraData(
      CameraHolder holder,
      ServerLevel level,
      ItemStack camera,
      CaptureParameters captureParameters,
      List<BlockPos> positionsInFrame,
      List<LivingEntity> entitiesInFrame,
      ExtraData data
   ) {
      data.put(PolaroidFrameExtraData.INSTANT, true);
      super.addFrameExtraData(holder, level, camera, captureParameters, positionsInFrame, entitiesInFrame, data);
   }

   public void addFrameToFilm(ItemStack stack, Frame frame) {
      DataComponents.PHOTOGRAPH_FRAME.set(stack, frame);
   }
}
