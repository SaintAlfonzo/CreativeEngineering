package io.github.mortuusars.exposure.world.item.camera;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Filters;
import io.github.mortuusars.exposure.data.Lenses;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.CaptureStartS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ShutterOpenedS2CP;
import io.github.mortuusars.exposure.network.packet.serverbound.OpenCameraAttachmentsInCreativePacketC2SP;
import io.github.mortuusars.exposure.server.CameraInstance;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.util.PointOfView;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.exposure.util.Vec3Util;
import io.github.mortuusars.exposure.util.color.Color;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.camera.capture.Projection;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import io.github.mortuusars.exposure.world.camera.component.SelfTimer;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmProperties;
import io.github.mortuusars.exposure.world.camera.frame.EntitiesInFrame;
import io.github.mortuusars.exposure.world.camera.frame.EntityInFrame;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.camera.frame.Photographer;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import io.github.mortuusars.exposure.world.inventory.CameraInHandAttachmentsMenu;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.item.InterplanarProjectorItem;
import io.github.mortuusars.exposure.world.item.SensitiveFilmItem;
import io.github.mortuusars.exposure.world.item.component.StoredItemStack;
import io.github.mortuusars.exposure.world.level.LevelUtil;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.world.sound.Sound;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import io.github.mortuusars.exposure.backport.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.mortuusars.exposure.backport.util.Backport;
import io.github.mortuusars.exposure.world.entity.CameraOperator;

public class CameraItem extends Item {
   public static final int BASE_COOLDOWN = 2;
   public static final int PROJECT_COOLDOWN = 20;
   protected final Shutter shutter = this.createShutter();
   protected final Timer timer = this.createTimer();
   protected final Flash flash = this.createFlash();
   protected final List<Attachment<?>> attachments = this.defineAttachments();
   protected final List<ShutterSpeed> availableShutterSpeeds = this.defineShutterSpeeds();

   public CameraItem(Properties properties) {
      super(properties);
      this.shutter.onOpen(this::onShutterOpen);
      this.shutter.onClosed(this::onShutterClosed);
   }

   protected Shutter createShutter() {
      return new Shutter();
   }

   protected Timer createTimer() {
      return new Timer();
   }

   protected Flash createFlash() {
      return new Flash();
   }

   @NotNull
   protected List<Attachment<?>> defineAttachments() {
      return List.of(Attachment.FILM, Attachment.FLASH, Attachment.LENS, Attachment.FILTER);
   }

   protected List<ShutterSpeed> defineShutterSpeeds() {
      return List.of(
         new ShutterSpeed("1/500"),
         new ShutterSpeed("1/250"),
         new ShutterSpeed("1/125"),
         new ShutterSpeed("1/60"),
         new ShutterSpeed("1/30"),
         new ShutterSpeed("1/15"),
         new ShutterSpeed("1/8"),
         new ShutterSpeed("1/4"),
         new ShutterSpeed("1/2"),
         new ShutterSpeed("1\"")
      );
   }

   public boolean hasAttachmentsMenu() {
      return true;
   }

   public Shutter getShutter() {
      return this.shutter;
   }

   public Timer getTimer() {
      return this.timer;
   }

   public Flash getFlash() {
      return this.flash;
   }

   public List<ShutterSpeed> getAvailableShutterSpeeds() {
      return this.availableShutterSpeeds;
   }

   public List<Attachment<?>> getAttachments() {
      return this.attachments;
   }

   public Attachment<?> getFilmAttachment() {
      return Attachment.FILM;
   }

   @NotNull
   public FilmProperties getFilmProperties(ItemStack stack) {
      ItemStack filmStack = this.getFilmAttachment().get(stack).getForReading();
      if (filmStack.getItem() instanceof SensitiveFilmItem filmItem) {
         return filmItem.getFilmProperties(filmStack);
      } else {
         throw new IllegalStateException("Cannot take a photo without SensitiveFilmItem in the camera. stack: " + stack);
      }
   }

   public SoundEvent getViewfinderOpenSound() {
      return Exposure.SoundEvents.VIEWFINDER_OPEN.get();
   }

   public SoundEvent getViewfinderCloseSound() {
      return Exposure.SoundEvents.VIEWFINDER_CLOSE.get();
   }

   public SoundEvent getReleaseButtonSound() {
      return Exposure.SoundEvents.CAMERA_RELEASE_BUTTON_CLICK.get();
   }

   public ResourceLocation getCaptureType(ItemStack stack) {
      return CaptureType.CAMERA;
   }

   public double getSelfieCameraDistance(ItemStack stack) {
      return (Double)Config.Server.SELFIE_CAMERA_DISTANCE.get();
   }

   public double getYPositionOffset(ItemStack stack) {
      return Config.Server.WAIST_LEVEL_VIEWFINDER.get() ? -0.35 : 0.0;
   }

   public float getScaleOnStand() {
      return 0.9F;
   }

   public float getCropFactor() {
      return 0.875F;
   }

   public FocalRange getFocalRange(RegistryAccess registryAccess, ItemStack stack) {
      return Attachment.LENS.<FocalRange>map(stack, lensStack -> Lenses.getFocalRangeOrDefault(registryAccess, lensStack)).orElse(FocalRange.getDefault());
   }

   public double getFov(Level level, ItemStack stack) {
      double zoom = (double)CameraSettings.ZOOM.getOrDefault(stack).floatValue();
      FocalRange focalRange = this.getFocalRange(level.registryAccess(), stack);
      return focalRange.fovFromZoom(zoom);
   }

   public double getViewfinderFov(Level level, ItemStack stack) {
      return this.getFov(level, stack) * (double)this.getCropFactor();
   }

   public PointOfView getPointOfView(CameraHolder holder, ItemStack stack) {
      return this.isInSelfieMode(stack)
         ? PointOfView.of(holder)
            .reverseDirection()
            .limitMaxDistance(holder, this.getSelfieCameraDistance(stack))
            .rotateX(-CameraSettings.SELFIE_ROTATION_X.getOrDefault(stack))
            .rotateY(-CameraSettings.SELFIE_ROTATION_Y.getOrDefault(stack))
         : PointOfView.of(holder).move(0.0, this.getYPositionOffset(stack), 0.0);
   }

   public Optional<Filter> getFilter(RegistryAccess registryAccess, ItemStack stack) {
      return Attachment.FILTER.<Optional<Filter>>map(stack, filter -> Filters.of(registryAccess, filter)).flatMap(Function.identity());
   }

   public Optional<ResourceLocation> getFilterShaderLocation(RegistryAccess registryAccess, ItemStack stack) {
      return this.getFilter(registryAccess, stack).map(Filter::shader);
   }

   protected Optional<ColorChannel> getChromaticChannel(ItemStack stack) {
      return Attachment.FILTER.map(stack, ColorChannel::fromFilterStack).orElse(Optional.empty());
   }

   protected Optional<Projection> getProjection(ItemStack stack) {
      return Attachment.FILTER
         .<Optional<Projection>>map(
            stack,
            (filterItem, filterStack) -> filterItem instanceof InterplanarProjectorItem projectorItem
                  ? projectorItem.getProjection(filterStack)
                  : Optional.empty()
         )
         .orElse(Optional.empty());
   }

   public CameraId getOrCreateId(ItemStack stack) {
      if (!Exposure.DataComponents.CAMERA_ID.has(stack)) {
         Exposure.DataComponents.CAMERA_ID.set(stack, CameraId.create());
      }

      return (CameraId)Exposure.DataComponents.CAMERA_ID.get(stack);
   }

   public boolean isInSelfieMode(ItemStack stack) {
      return CameraSettings.SELFIE_MODE.getOrDefault(stack);
   }

   public boolean isActive(ItemStack stack) {
      return (Boolean)Exposure.DataComponents.CAMERA_ACTIVE.getOrDefault(stack, false);
   }

   public void setActive(ItemStack stack, boolean active) {
      if (!active) {
         Exposure.DataComponents.CAMERA_ACTIVE.remove(stack);
      } else {
         Exposure.DataComponents.CAMERA_ACTIVE.set(stack, true);
      }
   }

   public boolean isDisassembled(ItemStack stack) {
      return (Boolean)Exposure.DataComponents.CAMERA_DISASSEMBLED.getOrDefault(stack, false);
   }

   public void setDisassembled(ItemStack stack, boolean disassembled) {
      if (!disassembled) {
         Exposure.DataComponents.CAMERA_DISASSEMBLED.remove(stack);
      } else {
         Exposure.DataComponents.CAMERA_DISASSEMBLED.set(stack, true);
      }
   }

   public long getLastActionTime(ItemStack stack) {
      return (Long)Exposure.DataComponents.CAMERA_LAST_ACTION_TIME.getOrDefault(stack, -1L);
   }

   public void setLastActionTime(ItemStack stack, long lastActionTime) {
      Exposure.DataComponents.CAMERA_LAST_ACTION_TIME.set(stack, lastActionTime);
   }

   public void actionPerformed(ItemStack stack, CameraHolder holder) {
      this.setLastActionTime(stack, holder.asHolderEntity().level.getGameTime());
      holder.asHolderEntity().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
   }

   @NotNull
   public InteractionResultHolder<ItemStack> activateInHand(Player player, ItemStack stack, @NotNull InteractionHand hand) {
      ((CameraOperator)player).setActiveExposureCamera(new CameraInHand((CameraHolder)player, this.getOrCreateId(stack), hand));
      if (player.level.isClientSide) {
         Minecrft.releaseUseButton();
      }

      return this.activate(player, stack);
   }

   @NotNull
   public InteractionResultHolder<ItemStack> activateOnStand(Player player, ItemStack stack, CameraStandEntity cameraStand) {
      ((CameraOperator)player).setActiveExposureCamera(new CameraOnStand((CameraOperator)player, cameraStand, this.getOrCreateId(stack)));
      if (player.level.isClientSide) {
         Minecrft.releaseUseButton();
      }

      return this.activate(player, stack);
   }

   @NotNull
   public InteractionResultHolder<ItemStack> activate(Entity entity, ItemStack stack) {
      this.setActive(stack, true);
      this.setDisassembled(stack, false);
      Sound.play(entity, this.getViewfinderOpenSound(), entity.getSoundSource(), 0.35F, 0.9F, 0.2F);
      entity.gameEvent(GameEvent.EQUIP);
      return InteractionResultHolder.consume(stack);
   }

   @NotNull
   public InteractionResultHolder<ItemStack> deactivate(Entity entity, ItemStack stack) {
      this.setActive(stack, false);
      CameraSettings.SELFIE_MODE.set(stack, false);
      Sound.play(entity, this.getViewfinderCloseSound(), entity.getSoundSource(), 0.35F, 0.9F, 0.2F);
      entity.gameEvent(GameEvent.EQUIP);
      return InteractionResultHolder.consume(stack);
   }

   public int calculateCooldownAfterShot(ItemStack stack, CaptureParameters captureParameters) {
      if (captureParameters.projection().isPresent()) {
         return 20;
      } else {
         return captureParameters.getFlash() ? this.getFlash().getCooldown() : 2;
      }
   }

   /** Upstream: neoforge.mixin.CameraItemNeoForgeMixin (IItemExtension). */
   @Override
   public boolean shouldCauseReequipAnimation(ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
      return !Backport.isSameItemSameComponents(newStack, oldStack);
   }

   public boolean isBarVisible(@NotNull ItemStack stack) {
      return (Boolean)Config.Client.CAMERA_SHOW_FILM_BAR_ON_ITEM.get() && Attachment.FILM.map(stack, FilmRollItem::isBarVisible).orElse(false);
   }

   public int getBarWidth(@NotNull ItemStack stack) {
      return Attachment.FILM.map(stack, FilmRollItem::getBarWidth).orElse(0);
   }

   public int getBarColor(@NotNull ItemStack stack) {
      return Attachment.FILM.map(stack, FilmRollItem::getBarColor).orElse(0);
   }

   public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> components, TooltipFlag tooltipFlag) {
      if ((Boolean)Config.Client.CAMERA_SHOW_FILM_FRAMES_IN_TOOLTIP.get()) {
         Attachment.FILM.ifPresent(stack, (filmItem, filmStack) -> {
            int exposed = filmItem.getStoredFramesCount(filmStack);
            int max = filmItem.getMaxFrameCount(filmStack);
            components.add(Component.translatable("item.exposure.camera.tooltip.film_roll_frames", new Object[]{exposed, max}));
         });
      }

      if ((Boolean)Config.Client.CAMERA_SHOW_TOOLTIP_DETAILS.get()) {
         if (stack.getEntityRepresentation() instanceof CameraStandEntity) {
            if (Screen.hasShiftDown()) {
               components.add(Component.translatable("item.exposure.camera.tooltip.details_attachments_screen_on_stand"));
               components.add(Component.translatable("item.exposure.camera.tooltip.details_hotswap_on_stand"));
            } else {
               components.add(Component.translatable("tooltip.exposure.hold_for_details"));
            }

            return;
         }

         boolean rClickAttachments = (Boolean)Config.Server.CAMERA_GUI_RIGHT_CLICK_OPEN_ATTACHMENTS.get();
         boolean rClickHotswap = (Boolean)Config.Server.CAMERA_GUI_RIGHT_CLICK_HOTSWAP.get();
         if (rClickAttachments || rClickHotswap) {
            if (Screen.hasShiftDown()) {
               if (rClickAttachments) {
                  components.add(Component.translatable("item.exposure.camera.tooltip.details_attachments_screen"));
               }

               if (rClickHotswap) {
                  components.add(Component.translatable("item.exposure.camera.tooltip.details_hotswap"));
               }
            } else {
               components.add(Component.translatable("tooltip.exposure.hold_for_details"));
            }
         }
      }
   }

   public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
      if (action != ClickAction.SECONDARY) {
         return false;
      } else if (this.getShutter().isOpen(stack)) {
         player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9F, 1.0F);
         player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open").withStyle(ChatFormatting.RED), true);
         return true;
      } else if (otherStack.isEmpty() && (Boolean)Config.Server.CAMERA_GUI_RIGHT_CLICK_OPEN_ATTACHMENTS.get()) {
         if (!(slot.container instanceof Inventory)) {
            return false;
         } else if (player.isCreative() && player.level.isClientSide()) {
            Packets.sendToServer(new OpenCameraAttachmentsInCreativePacketC2SP(slot.getContainerSlot()));
            return true;
         } else {
            this.openCameraAttachments(player, slot.getContainerSlot(), true);
            return true;
         }
      } else {
         return (Boolean)Config.Server.CAMERA_GUI_RIGHT_CLICK_HOTSWAP.get() && this.hotswap((CameraHolder)player, stack, otherStack, access) != InteractionResult.PASS;
      }
   }

   public InteractionResult handleStandSneakInteraction(CameraStandEntity stand, Player player, InteractionHand hand, ItemStack cameraStack) {
      ItemStack itemInHand = player.getItemInHand(hand);
      int slot = hand == InteractionHand.OFF_HAND ? 40 : player.getInventory().selected;
      SlotAccess access = SlotAccess.forContainer(player.getInventory(), slot);
      return this.hotswap(stand, cameraStack, itemInHand, access);
   }

   protected InteractionResult hotswap(CameraHolder holder, ItemStack stack, ItemStack otherStack, SlotAccess access) {
      for (Attachment<?> attachment : this.getAttachments()) {
         StoredItemStack storedStack = attachment.get(stack);
         int maxCount = attachment.maxCount().get();
         if (otherStack.isEmpty()) {
            if (storedStack.isEmpty()) {
               return InteractionResult.FAIL;
            }

            access.set(storedStack.getCopy());
            attachment.set(stack, ItemStack.EMPTY);
            attachment.playRemoveSoundSided(holder.asHolderEntity());
            return InteractionResult.SUCCESS;
         }

         if (attachment.matches(otherStack)) {
            if (!storedStack.isEmpty() && !Backport.isSameItemSameComponents(storedStack.getForReading(), otherStack)) {
               if (otherStack.getCount() > maxCount) {
                  holder.asHolderEntity().playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9F, 1.0F);
                  return InteractionResult.FAIL;
               }

               ItemStack returnedStack = storedStack.getCopy();
               attachment.set(stack, otherStack);
               access.set(returnedStack);
               attachment.playInsertSoundSided(holder.asHolderEntity());
               return InteractionResult.SUCCESS;
            }

            int availableCount = Math.max(0, maxCount - storedStack.getForReading().getCount());
            if (availableCount == 0) {
               holder.asHolderEntity().playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9F, 1.0F);
               return InteractionResult.FAIL;
            }

            ItemStack insertedStack = otherStack.split(availableCount);
            insertedStack.setCount(insertedStack.getCount() + storedStack.getForReading().getCount());
            attachment.set(stack, insertedStack);
            access.set(otherStack);
            attachment.playInsertSoundSided(holder.asHolderEntity());
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
   }

   public InteractionResultHolder<ItemStack> openCameraAttachments(@NotNull Player player, ItemStack stack, boolean openedFromGUI) {
      Preconditions.checkArgument(stack.getItem() instanceof CameraItem, "%s is not a CameraItem.", stack);
      int cameraSlot = this.getMatchingSlotInInventory(player.getInventory(), stack);
      if (cameraSlot < 0) {
         Exposure.LOGGER.error("Cannot open camera attachments: slot index is not found for item '{}'.", stack);
         return InteractionResultHolder.fail(stack);
      } else {
         return this.openCameraAttachments(player, cameraSlot, openedFromGUI);
      }
   }

   public InteractionResultHolder<ItemStack> openCameraAttachments(@NotNull Player player, int slotIndex, boolean openedFromGUI) {
      Preconditions.checkArgument(slotIndex >= 0, "slotIndex '%s' is invalid. Should be larger than 0", slotIndex);
      final ItemStack stack = player.getInventory().getItem(slotIndex);
      Preconditions.checkArgument(stack.getItem() instanceof CameraItem, "Item in slotIndex '%s' is not a CameraItem but '%s'.", slotIndex, stack);
      if (this.getShutter().isOpen(stack)) {
         player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open").withStyle(ChatFormatting.RED), true);
         return InteractionResultHolder.fail(stack);
      } else {
         this.getOrCreateId(stack);
         if (player instanceof ServerPlayer serverPlayer) {
            this.getTimer().stop(stack);
            MenuProvider menuProvider = new MenuProvider() {
               @NotNull
               public Component getDisplayName() {
                  return (Component)(DataComponents.CUSTOM_NAME.get(stack) != null ? stack.getHoverName() : Component.translatable("container.exposure.camera"));
               }

               @NotNull
               public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                  return new CameraInHandAttachmentsMenu(containerId, playerInventory, slotIndex, openedFromGUI);
               }
            };
            PlatformHelper.openMenu(serverPlayer, menuProvider, buffer -> {
               buffer.writeInt(slotIndex);
               buffer.writeBoolean(openedFromGUI);
            });
         }

         this.setDisassembled(stack, true);
         Sound.play(player, Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), SoundSource.PLAYERS, 0.9F, 0.9F, 0.2F);
         return InteractionResultHolder.success(stack);
      }
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
      if (entity instanceof CameraHolder holder) {
         this.tick(holder, stack);
         if (level.isClientSide && entity instanceof Player player) {
            boolean matchesActive = ((CameraOperator)player).getActiveExposureCameraOptional().map(camera -> camera.idMatches(this.getOrCreateId(stack))).orElse(false);
            if (this.isActive(stack) && !matchesActive) {
               this.setActive(stack, false);
            }
         }
      }
   }

   public boolean tick(CameraHolder holder, ItemStack stack) {
      Level level = holder.asHolderEntity().level;
      if (!(level instanceof ServerLevel serverLevel)) {
         return false;
      } else {
         boolean shutterStateChanged = this.getShutter().tick(holder, serverLevel, stack);
         boolean timerChanged = this.getTimer().tick(holder, serverLevel, stack);
         if ((Boolean)Config.Server.TIMER_ATTRACTS_MOB_ATTENTION.get() && this.getTimer().isTicking(holder, stack)
            || this.getTimer().getTicksSinceLastRelease(holder, stack) < 10L) {
            this.grabAttentionOfNearbyMobs(holder, stack);
         }

         boolean projectionChanged = CameraInstances.getOptional(stack).map(instance -> {
            CameraInstance.ProjectionState state = instance.getProjectionState(level);
            switch (state) {
               case SUCCESSFUL:
               case FAILED:
               case TIMED_OUT:
                  this.handleProjectionResult(serverLevel, holder, stack, state, instance.getProjectionError(level));
                  instance.stopWaitingForProjection();
                  return true;
               default:
                  return false;
            }
         }).orElse(false);
         if (ExposureServer.debugHighlightEntitiesInFrame && this.isActive(stack)) {
            this.testEntitiesInFrame(stack, level, holder);
         }

         return shutterStateChanged || timerChanged || projectionChanged;
      }
   }

   protected void grabAttentionOfNearbyMobs(CameraHolder holder, ItemStack stack) {
      Entity holderEntity = holder.asHolderEntity();
      Vec3 pos = this.isInSelfieMode(stack)
         ? holderEntity.getEyePosition().add(holderEntity.getLookAngle().scale((Double)Config.Server.SELFIE_CAMERA_DISTANCE.get()))
         : holderEntity.getEyePosition();
      holderEntity.level
         .getEntities(holderEntity, new AABB(holderEntity.blockPosition()).inflate((double)((Integer)Config.Server.TIMER_ATTENTION_RADIUS.get()).intValue()))
         .stream()
         .filter(entity -> entity instanceof Mob)
         .map(entity -> (Mob)entity)
         .filter(mob -> this.canGrabAttentionOf(holder, mob))
         .forEach(mob -> {
            long startLookingTick = this.getTimer().getStartTick(stack) + (long)(mob.getId() % 15);
            if (mob.level.getGameTime() > startLookingTick) {
               mob.lookAt(Anchor.EYES, pos);
            }
         });
   }

   protected boolean canGrabAttentionOf(CameraHolder holder, Mob mob) {
      return mob.isAlive()
         && !mob.isDeadOrDying()
         && !mob.isSleeping()
         && !mob.getType().is(Exposure.Tags.Entities.IGNORES_CAMERA)
         && (mob.getTarget() == null || mob.getTarget().equals(holder))
         && !mob.hasEffect(MobEffects.BLINDNESS)
         && mob.hasLineOfSight(holder.asHolderEntity());
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (hand == InteractionHand.MAIN_HAND
         && player.getOffhandItem().getItem() instanceof CameraItem offhandCameraItem
         && offhandCameraItem.isActive(player.getOffhandItem())) {
         return InteractionResultHolder.pass(stack);
      } else if (!this.isActive(stack)) {
         return player.isSecondaryUseActive() ? this.openCameraAttachments(player, stack, false) : this.activateInHand(player, stack, hand);
      } else {
         return this.release((CameraHolder)player, stack);
      }
   }

   public boolean canTakePhoto(CameraHolder holder, ItemStack stack) {
      return !this.isOnCooldown(holder, stack)
         && !this.getTimer().isTicking(holder, stack)
         && !this.getShutter().isOpen(stack)
         && Attachment.FILM.map(stack, FilmRollItem::canAddFrame).orElse(false)
         && CameraInstances.canReleaseShutter(CameraId.ofStack(stack));
   }

   public boolean isOnCooldown(CameraHolder holder, ItemStack stack) {
      if (holder.asHolderEntity() instanceof Player player) {
         return player.getCooldowns().isOnCooldown(this);
      } else {
         return holder instanceof CameraStandEntity stand ? stand.isOnCooldown() : false;
      }
   }

   public float getCooldownPercent(CameraHolder holder, ItemStack stack) {
      if (holder.asHolderEntity() instanceof Player player) {
         return player.getCooldowns().isOnCooldown(stack.getItem()) ? player.getCooldowns().getCooldownPercent(stack.getItem(), 0.0F) : 0.0F;
      } else if (holder instanceof CameraStandEntity stand) {
         return stand.isOnCooldown() ? stand.getCooldownPercent() : 0.0F;
      } else {
         return 0.0F;
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> release(CameraHolder holder, ItemStack stack) {
      Entity entity = holder.asHolderEntity();
      Level level = entity.level;
      Sound.playSided(entity, this.getReleaseButtonSound(), entity.getSoundSource(), 0.3F, 1.0F, 0.1F);
      if (!level.isClientSide && this.canTakePhoto(holder, stack)) {
         if (this.getTimer().getEndTick(stack) != level.getGameTime()) {
            SelfTimer selfTimer = CameraSettings.SELF_TIMER.getOrDefault(stack);
            if (selfTimer != SelfTimer.OFF) {
               this.getTimer().set(holder, stack, selfTimer.getTicks());
               return InteractionResultHolder.consume(stack);
            }
         }

         holder.getServerPlayerExecutingExposure()
            .ifPresentOrElse(
               player -> this.takePhoto(holder, player, stack),
               () -> Exposure.LOGGER.error("Cannot start capture: photographer '{}' does not have valid executing player.", holder)
            );
         return InteractionResultHolder.consume(stack);
      } else {
         return InteractionResultHolder.consume(stack);
      }
   }

   protected void takePhoto(CameraHolder holder, ServerPlayer executingPlayer, ItemStack stack) {
      ServerLevel level = executingPlayer.getLevel();
      Entity entity = holder.asHolderEntity();
      ShutterSpeed shutterSpeed = CameraSettings.SHUTTER_SPEED.getOrDefault(stack);
      this.getShutter().open(holder, level, stack, shutterSpeed);
      CameraId cameraId = this.getOrCreateId(stack);
      String exposureId = ExposureIdentifier.createId(executingPlayer);
      int lightLevel = LevelUtil.getLightLevelAt(level, entity.blockPosition());
      boolean flash = this.getFlash().isAvailable(stack) && this.getFlash().shouldFire(stack, lightLevel) && this.getFlash().fire(holder, level, stack);
      CaptureParameters captureParameters = new CaptureParameters.Builder(exposureId)
         .setCameraID(cameraId)
         .setCameraHolder(holder)
         .setFov(this.getFov(level, stack))
         .setCropFactor(this.getCropFactor())
         .setFilter(this.getFilterShaderLocation(level.registryAccess(), stack).orElse(null))
         .setProjection(this.getProjection(stack))
         .setChromaticChannel(this.getChromaticChannel(stack))
         .setFilmProperties(this.getFilmProperties(stack))
         .extraData(CaptureParameters.SHUTTER_SPEED, CameraSettings.SHUTTER_SPEED.getOrDefault(stack))
         .extraData(CaptureParameters.FLASH, flash)
         .extraData(CaptureParameters.LIGHT_LEVEL, lightLevel)
         .build();
      if (shutterSpeed.shouldCauseTickingSound() || captureParameters.projection().isPresent()) {
         int duration = Math.max(
            shutterSpeed.getDurationTicks(), captureParameters.projection().map(l -> (Integer)Config.Server.PROJECT_TIMEOUT_TICKS.get()).orElse(0)
         );
         Sound.playShutterTicking(entity, cameraId, duration);
      }

      CameraInstances.createOrUpdate(
         cameraId,
         instance -> {
            int cooldown = this.calculateCooldownAfterShot(stack, captureParameters);
            instance.setDeferredCooldown(cooldown);
            captureParameters.projection()
               .ifPresent(
                  fileLoading -> instance.waitForProjection(level.getGameTime() + (long)((Integer)Config.Server.PROJECT_TIMEOUT_TICKS.get()).intValue())
               );
         }
      );
      this.addNewFrame(level, holder, stack, captureParameters);
      ExposureServer.exposureRepository().expect(executingPlayer, exposureId);
      Packets.sendToClient(new CaptureStartS2CP(this.getCaptureType(stack), captureParameters), executingPlayer);
   }

   protected void onShutterOpen(CameraHolder holder, ServerLevel serverLevel, ItemStack stack) {
      holder.getExposureCameraOperator().ifPresent(operator -> {
         if (operator instanceof ServerPlayer player) {
            Packets.sendToClient(ShutterOpenedS2CP.INSTANCE, player);
         }
      });
   }

   protected void onShutterClosed(CameraHolder holder, ServerLevel serverLevel, ItemStack stack) {
      if (holder instanceof Player player) {
         int cooldown = CameraInstances.getOptional(stack).map(CameraInstance::getDeferredCooldown).orElse(2);
         player.getCooldowns().addCooldown(this, cooldown);
      } else if (holder instanceof CameraStandEntity stand) {
         int cooldown = CameraInstances.getOptional(stack).map(CameraInstance::getDeferredCooldown).orElse(2);
         stand.startCooldown(cooldown);
      }

      Attachment.FILM.ifPresent(stack, (filmItem, filmStack) -> {
         SoundEvent sound = filmItem.isFull(filmStack) ? Exposure.SoundEvents.FILM_ADVANCE_LAST.get() : Exposure.SoundEvents.FILM_ADVANCE.get();
         float fullness = filmItem.getFullness(filmStack);
         String id = holder.asHolderEntity().getId() + this.getOrCreateId(stack).uuid().toString();
         Sound.playUnique(id, holder.asHolderEntity(), sound, SoundSource.PLAYERS, 1.0F, 0.85F + 0.2F * fullness);
      });
   }

   public void addNewFrame(ServerLevel level, CameraHolder holder, ItemStack stack, CaptureParameters captureParameters) {
      boolean projecting = captureParameters.projection().isPresent();
      PointOfView pov = this.getPointOfView(holder, stack);
      double fov = this.getViewfinderFov(level, stack);
      List<BlockPos> positionsInFrame = !projecting ? this.getPositionsInFrame(holder, pov, fov) : Collections.emptyList();
      List<LivingEntity> entitiesInFrame = !projecting ? EntitiesInFrame.get(holder, pov, fov) : Collections.emptyList();
      Frame frame = this.createFrame(holder, level, stack, captureParameters, positionsInFrame, entitiesInFrame);
      this.addFrameToFilm(stack, frame);
      this.onFrameAdded(holder, level, stack, frame, positionsInFrame, entitiesInFrame);
   }

   public Frame createFrame(
      CameraHolder holder,
      ServerLevel level,
      ItemStack stack,
      CaptureParameters captureParameters,
      List<BlockPos> positionsInFrame,
      List<LivingEntity> entitiesInFrame
   ) {
      return Frame.create()
         .setIdentifier(ExposureIdentifier.id(captureParameters.exposureId()))
         .setType(captureParameters.filmProperties().type())
         .setPhotographer(new Photographer(holder))
         .setEntitiesInFrame(
            entitiesInFrame.stream()
               .limit(10L)
               .map(
                  entity -> EntityInFrame.of(
                        holder.asHolderEntity(), entity, data -> PlatformHelper.postModifyEntityInFrameExtraDataEvent(holder, stack, entity, data)
                     )
               )
               .toList()
         )
         .addExtraData(Frame.SHUTTER_SPEED, CameraSettings.SHUTTER_SPEED.getOrDefault(stack))
         .addExtraData(Frame.TIMESTAMP, UnixTimestamp.Seconds.now())
         .updateExtraData(data -> this.addFrameExtraData(holder, level, stack, captureParameters, positionsInFrame, entitiesInFrame, data))
         .toImmutable();
   }

   protected void addFrameExtraData(
      CameraHolder holder,
      ServerLevel level,
      ItemStack camera,
      CaptureParameters params,
      List<BlockPos> positionsInFrame,
      List<LivingEntity> entitiesInFrame,
      ExtraData data
   ) {
      Entity cameraHolder = holder.asHolderEntity();
      boolean projecting = params.projection().isPresent();
      if (projecting) {
         data.put(Frame.PROJECTED, true);
      } else {
         if (params.getFlash()) {
            data.put(Frame.FLASH, true);
         }

         if (this.isInSelfieMode(camera)) {
            data.put(Frame.SELFIE, true);
         }

         if (holder instanceof CameraStandEntity) {
            data.put(Frame.ON_STAND, true);
         }

         double zoom = (double)CameraSettings.ZOOM.getOrDefault(camera).floatValue();
         FocalRange focalRange = this.getFocalRange(level.registryAccess(), camera);
         int focalLength = (int)focalRange.focalLengthFromZoom(zoom);
         data.put(Frame.FOCAL_LENGTH, focalLength);
         params.extraData().get(CaptureParameters.LIGHT_LEVEL).ifPresent(lightLevel -> data.put(Frame.LIGHT_LEVEL, lightLevel));
         if (params.filmProperties().type() == ExposureType.BLACK_AND_WHITE) {
            params.singleChannel().ifPresent(channel -> data.put(Frame.COLOR_CHANNEL, channel));
         }

         data.put(Frame.POSITION, cameraHolder.position());
         data.put(Frame.PITCH, cameraHolder.getXRot());
         data.put(Frame.YAW, cameraHolder.getYRot());
         data.put(Frame.DAY_TIME, (int)level.getDayTime());
         data.put(Frame.DIMENSION, level.dimension().location());
         BlockPos blockPos = cameraHolder.blockPosition();
         int surfaceHeight = level.getHeight(Types.WORLD_SURFACE, cameraHolder.getBlockX(), cameraHolder.getBlockZ());
         level.updateSkyBrightness();
         int skyLight = level.getBrightness(LightLayer.SKY, blockPos);
         if (cameraHolder.isUnderWater()) {
            data.put(Frame.UNDERWATER, true);
         }

         if (cameraHolder.getBlockY() < Math.min(level.getSeaLevel(), surfaceHeight) && skyLight == 0) {
            data.put(Frame.IN_CAVE, true);
         } else if (!cameraHolder.isUnderWater()) {
            Precipitation precipitation = Backport.getPrecipitationAt(((Biome)level.getBiome(blockPos).value()), blockPos);
            if (level.isThundering() && precipitation != Precipitation.NONE) {
               data.put(Frame.WEATHER, precipitation == Precipitation.SNOW ? "Snowstorm" : "Thunder");
            } else if (level.isRaining() && precipitation != Precipitation.NONE) {
               data.put(Frame.WEATHER, precipitation == Precipitation.SNOW ? "Snow" : "Rain");
            } else {
               data.put(Frame.WEATHER, "Clear");
            }
         }

         positionsInFrame.stream()
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet()
            .stream()
            .max(Entry.comparingByValue())
            .map(Entry::getKey)
            .flatMap(pos -> level.getBiome(pos).unwrapKey().map(ResourceKey::location))
            .ifPresent(biome -> data.put(Frame.BIOME, biome));
         List<ResourceLocation> structures = positionsInFrame.stream()
            .map(pos -> LevelUtil.getStructuresAt(level, pos))
            .flatMap(Collection::stream)
            .collect(Collectors.toSet())
            .stream()
            .toList();
         if (!structures.isEmpty()) {
            data.put(Frame.STRUCTURES, structures);
         }

         PlatformHelper.postModifyFrameExtraDataEvent(holder, camera, params, positionsInFrame, entitiesInFrame, data);
      }
   }

   public List<BlockPos> getPositionsInFrame(CameraHolder cameraHolder, PointOfView pov, double fov) {
      float offsetDegrees = (float)(fov * (double)this.getCropFactor() / 4.3);
      return Vec3Util.getProbeVectors(pov.dir(), offsetDegrees).stream().map(direction -> {
         Vec3 endPos = pov.pos().add(direction.scale(100.0));
         return cameraHolder.asHolderEntity().level.clip(new ClipContext(pov.pos(), endPos, Block.OUTLINE, Fluid.ANY, cameraHolder.asHolderEntity()));
      }).filter(hit -> hit.getType() != Type.MISS).<BlockPos>map(BlockHitResult::getBlockPos).toList();
   }

   public void addFrameToFilm(ItemStack stack, Frame frame) {
      Attachment.FILM.ifPresentOrElse(stack, (filmItem, filmStack) -> {
         ItemStack updatedFilmStack = filmStack.copy();
         filmItem.addFrame(updatedFilmStack, frame);
         Attachment.FILM.set(stack, updatedFilmStack);
      }, () -> Exposure.LOGGER.error("Cannot add frame: no film attachment is present."));
   }

   public void onFrameAdded(
      CameraHolder holder, ServerLevel level, ItemStack stack, Frame frame, List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame
   ) {
      Entity executor = holder.getPlayerExecutingExposure().map(pl -> (Entity)pl).orElse(holder.asHolderEntity());
      ExposureServer.frameHistory().add(executor, frame);
      entitiesInFrame.forEach(entity -> this.entityCaptured(holder, stack, entity));
      holder.getPlayerAwardedForExposure().filter(player -> player instanceof ServerPlayer).ifPresent(player -> {
         ServerPlayer serverPlayer = (ServerPlayer)player;
         serverPlayer.awardStat(Exposure.Stats.FILM_FRAMES_EXPOSED);
         Exposure.CriteriaTriggers.FRAME_EXPOSED.get().trigger(serverPlayer, holder, stack, frame, positionsInFrame, entitiesInFrame);
      });
      PlatformHelper.postFrameAddedEvent(holder, stack, frame, positionsInFrame, entitiesInFrame);
   }

   protected void entityCaptured(CameraHolder cameraHolder, ItemStack stack, LivingEntity entity) {
      if (cameraHolder.asHolderEntity() instanceof ServerPlayer player && entity instanceof EnderMan enderMan) {
         boolean lookingAtAngryEnderMan = player.equals(enderMan.getTarget()) && enderMan.isLookingAtMe(player);
         if (lookingAtAngryEnderMan) {
            Exposure.CriteriaTriggers.PHOTOGRAPH_ENDERMAN_EYES.get().trigger(player);
         }
      }
   }

   public void handleProjectionResult(
      ServerLevel level, CameraHolder holder, ItemStack stack, CameraInstance.ProjectionState projectionState, Optional<TranslatableError> error
   ) {
      StoredItemStack filter = Attachment.FILTER.get(stack);
      if (!filter.isEmpty()) {
         if (filter.getItem() instanceof InterplanarProjectorItem interplanarProjector) {
            if (interplanarProjector.isConsumable(filter.getForReading())) {
               Entity entity = holder.asHolderEntity();
               if (projectionState != CameraInstance.ProjectionState.FAILED && projectionState != CameraInstance.ProjectionState.TIMED_OUT) {
                  ItemStack filterStack = filter.getCopy();
                  filterStack.shrink(1);
                  Attachment.FILTER.set(stack, filterStack);
                  if (projectionState == CameraInstance.ProjectionState.SUCCESSFUL) {
                     holder.getServerPlayerAwardedForExposure().ifPresent(player -> Exposure.CriteriaTriggers.SUCCESSFULLY_PROJECT_IMAGE.get().trigger(player));
                     Sound.play(entity, Exposure.SoundEvents.INTERPLANAR_PROJECT.get(), entity.getSoundSource(), 0.8F, 1.1F);

                     for (int i = 0; i < 16; i++) {
                        level.sendParticles(
                           ParticleTypes.PORTAL,
                           entity.getX(),
                           entity.getY() + 1.2,
                           entity.getZ(),
                           2,
                           level.getRandom().nextGaussian() * 0.3,
                           level.getRandom().nextGaussian() * 0.3,
                           level.getRandom().nextGaussian() * 0.3,
                           0.01
                        );
                     }
                  }
               } else {
                  ItemStack filterStack = Backport.transmuteCopy(filter.getCopy(), (ItemLike)Exposure.Items.BROKEN_INTERPLANAR_PROJECTOR.get());
                  error.ifPresent(err -> Exposure.DataComponents.INTERPLANAR_PROJECTOR_ERROR_CODE.set(filterStack, err.code()));
                  Attachment.FILTER.set(stack, filterStack);
                  Sound.play(entity, Exposure.SoundEvents.BSOD.get());
                  if (this.getShutter().isOpen(stack)) {
                     this.getShutter().close(holder, level, stack);
                  }
               }
            }
         }
      }
   }

   protected int getMatchingSlotInInventory(Inventory inventory, ItemStack stack) {
      for (int i = 0; i < inventory.getContainerSize(); i++) {
         if (inventory.getItem(i).equals(stack)) {
            return i;
         }
      }

      return -1;
   }

   protected void testEntitiesInFrame(ItemStack stack, Level level, CameraHolder holder) {
      PointOfView pov = this.getPointOfView(holder, stack);
      double fov = this.getViewfinderFov(level, stack);

      for (LivingEntity livingEntity : EntitiesInFrame.get(holder.asHolderEntity(), pov, fov)) {
         livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 2, 1, true, false, false));
      }
   }

   protected void testPositionsInFrame(ItemStack stack, Level level, Player player) {
      if (level.isClientSide && level.getGameTime() % 2L == 0L) {
         for (BlockPos pos : this.getPositionsInFrame((CameraHolder)player, this.getPointOfView((CameraHolder)player, stack), this.getViewfinderFov(level, stack))) {
            level.addAlwaysVisibleParticle(
               ParticleTypes.EXPLOSION, true, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 0.0, 0.0, 0.0
            );
         }
      }
   }

   public static int getGlassTintColor(ItemStack stack, int tintIndex) {
      if (tintIndex != 1) {
         return -1;
      } else {
         boolean var10000;
         label53: {
            if (stack.getItem() instanceof CameraItem cameraItem && cameraItem.getShutter().isOpen(stack)) {
               var10000 = true;
               break label53;
            }

            var10000 = false;
         }

         boolean shutterOpen = var10000;
         StoredItemStack filter = Attachment.FILTER.get(stack);
         if (filter.isEmpty()) {
            return shutterOpen ? -13421773 : -1;
         } else {
            if (filter.getForReading().getItem() instanceof BlockItem item && item.getBlock() instanceof StainedGlassPaneBlock pane) {
               return shutterOpen
                  ? Color.argb(Backport.getTextureDiffuseColor(pane.getColor())).multiply(0.2F).withAlpha(255).getARGB()
                  : Backport.getTextureDiffuseColor(pane.getColor());
            }

            if (filter.getForReading().is(Exposure.Items.INTERPLANAR_PROJECTOR.get())) {
               return shutterOpen ? -16442865 : -11488642;
            } else if (filter.getForReading().is(Exposure.Items.BROKEN_INTERPLANAR_PROJECTOR.get())) {
               return shutterOpen ? -16761482 : -11227649;
            } else {
               return -1;
            }
         }
      }
   }
}
