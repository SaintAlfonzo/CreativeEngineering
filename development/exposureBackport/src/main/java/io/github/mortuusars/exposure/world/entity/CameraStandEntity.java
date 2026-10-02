package io.github.mortuusars.exposure.world.entity;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.CameraStandSetRotationsS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.CameraStandStopControllingS2CP;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.inventory.CameraOnStandAttachmentsMenu;
import io.github.mortuusars.exposure.world.item.InterplanarProjectorItem;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.exposure.world.sound.Sound;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import io.github.mortuusars.exposure.backport.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.AbstractMinecart.Type;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EntityDimensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.mortuusars.exposure.backport.util.Backport;

public class CameraStandEntity extends Entity implements CameraHolder {
   protected static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<ItemStack> DATA_ID_CAMERA = SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.ITEM_STACK);
   protected static final EntityDataAccessor<Integer> DATA_ID_COOLDOWN_TIME = SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> DATA_ID_COOLDOWN = SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> DATA_ID_OPERATOR_ID = SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Boolean> DATA_ID_MALFUNCTIONED = SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.BOOLEAN);
   protected static final Predicate<Entity> RIDABLE_MINECARTS = entity -> entity instanceof AbstractMinecart
         && ((AbstractMinecart)entity).getMinecartType() == Type.RIDEABLE;
   protected CameraStandRedstoneControl redstoneControl = new CameraStandRedstoneControl(this);
   protected UUID ownerPlayerId = Util.NIL_UUID;

   public CameraStandEntity(EntityType<? extends CameraStandEntity> entityType, Level level) {
      super(entityType, level);
   }

   @NotNull
   public Packet<?> getAddEntityPacket() {
      return new ClientboundAddEntityPacket(this);
   }

   protected float getEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions dimensions) {
      return 1.40625F;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_ID_HURT, 0);
      this.entityData.define(DATA_ID_HURTDIR, 1);
      this.entityData.define(DATA_ID_DAMAGE, 0.0F);
      this.entityData.define(DATA_ID_CAMERA, ItemStack.EMPTY);
      this.entityData.define(DATA_ID_OPERATOR_ID, -1);
      this.entityData.define(DATA_ID_COOLDOWN_TIME, 0);
      this.entityData.define(DATA_ID_COOLDOWN, 0);
      this.entityData.define(DATA_ID_MALFUNCTIONED, false);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
      if (dataAccessor.equals(DATA_ID_CAMERA)) {
         ItemStack camera = this.getCamera();
         if (!camera.isEmpty()) {
            camera.setEntityRepresentation(this);
         }
      }
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      tag.putInt("CooldownTime", this.getCooldownTime());
      tag.putInt("Cooldown", this.getCooldown());
      tag.putBoolean("Malfunctioned", this.isMalfunctioned());
      if (!this.getCamera().isEmpty()) {
         tag.put("Camera", this.getCamera().save(new CompoundTag()));
      }

      this.redstoneControl.save(tag);
      if (!this.ownerPlayerId.equals(Util.NIL_UUID)) {
         tag.putUUID("Owner", this.ownerPlayerId);
      }
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.setCooldownTime(tag.getInt("CooldownTime"));
      this.setCooldown(tag.getInt("Cooldown"));
      this.setMalfunctioned(tag.getBoolean("Malfunctioned"));
      this.setCamera(Backport.parseItemStackOptional(tag.getCompound("Camera")));
      this.redstoneControl.load(tag);
      if (tag.contains("Owner", 11)) {
         this.ownerPlayerId = tag.getUUID("Owner");
      }
   }

   public ItemStack getCamera() {
      return (ItemStack)this.getEntityData().get(DATA_ID_CAMERA);
   }

   public void setCamera(ItemStack cameraStack) {
      if (!this.isClientSide() && (cameraStack.isEmpty() || !CameraId.ofStack(this.getCamera()).matches(cameraStack))) {
         this.level
            .players()
            .stream()
            .filter(pl -> pl instanceof ServerPlayer && pl.containerMenu instanceof CameraOnStandAttachmentsMenu)
            .forEach(pl -> ((ServerPlayer)pl).closeContainer());
      }

      this.getEntityData().set(DATA_ID_CAMERA, cameraStack);
   }

   public boolean isCameraActive() {
      if (this.getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.isActive(this.getCamera())) {
         return true;
      }

      return false;
   }

   public Optional<Player> getOwnerPlayer() {
      return Optional.ofNullable(this.level.getPlayerByUUID(this.ownerPlayerId));
   }

   public UUID getOwnerPlayerUuid() {
      return this.ownerPlayerId;
   }

   public void setOwnerPlayer(Player player) {
      this.ownerPlayerId = player.getUUID();
   }

   public Optional<? extends Player> getClosestPlayerInRange() {
      return this.level.players().stream().min(Comparator.comparingDouble(player -> (double)player.distanceTo(this))).filter(this::isInRangeForPhoto);
   }

   public int getCooldownTime() {
      return (Integer)this.getEntityData().get(DATA_ID_COOLDOWN_TIME);
   }

   public void setCooldownTime(int cooldown) {
      this.getEntityData().set(DATA_ID_COOLDOWN_TIME, cooldown);
   }

   public int getCooldown() {
      return (Integer)this.getEntityData().get(DATA_ID_COOLDOWN);
   }

   public void setCooldown(int cooldown) {
      this.getEntityData().set(DATA_ID_COOLDOWN, cooldown);
   }

   public void startCooldown(int cooldown) {
      this.setCooldownTime(cooldown);
      this.setCooldown(cooldown);
   }

   public boolean isOnCooldown() {
      return this.getCooldown() > 0;
   }

   public float getCooldownPercent() {
      float cooldownTime = (float)this.getCooldownTime();
      float cooldown = (float)this.getCooldown();
      return Mth.clamp(cooldown / cooldownTime, 0.0F, 1.0F);
   }

   public boolean isMalfunctioned() {
      return (Boolean)this.getEntityData().get(DATA_ID_MALFUNCTIONED);
   }

   public void setMalfunctioned(boolean malfunctioned) {
      this.getEntityData().set(DATA_ID_MALFUNCTIONED, malfunctioned);
   }

   public int getOperatorId() {
      return (Integer)this.getEntityData().get(DATA_ID_OPERATOR_ID);
   }

   @Nullable
   public CameraOperator operator() {
      int id = this.getOperatorId();
      return id >= 0 && this.level.getEntity(id) instanceof CameraOperator operator ? operator : null;
   }

   public void setOperator(@Nullable CameraOperator operator) {
      this.getEntityData().set(DATA_ID_OPERATOR_ID, operator != null ? operator.asOperatorEntity().getId() : -1);
   }

   public boolean isInRangeForPhoto(Player player) {
      return player.distanceTo(this) < (float)((Integer)Config.Server.CAMERA_STAND_WORKING_RANGE.get()).intValue();
   }

   @Override
   public Optional<Player> getPlayerExecutingExposure() {
      return Config.Server.CAMERA_STAND_FALLBACK_TO_OTHER_PLAYERS.get()
         ? this.getOwnerPlayer().filter(this::isInRangeForPhoto).or(this::getClosestPlayerInRange)
         : this.getOwnerPlayer().filter(this::isInRangeForPhoto);
   }

   @Override
   public Optional<Player> getPlayerAwardedForExposure() {
      return this.getOwnerPlayer().or(this::getClosestPlayerInRange);
   }

   @NotNull
   @Override
   public Entity getExposureAuthorEntity() {
      return this.getOwnerPlayer().or(this::getClosestPlayerInRange).map(pl -> (Entity)pl).orElse(this);
   }

   @Override
   public Optional<CameraOperator> getExposureCameraOperator() {
      return Optional.ofNullable(this.operator());
   }

   public boolean isInInteractionRange(LivingEntity entity) {
      // 1.20.5 entity_interaction_range: Forge 1.19.2's attack range (3, +3 in creative) for players
      double range = entity instanceof Player player ? player.getAttackRange() : 3.0;
      return Backport.distanceToSqr(this.getBoundingBox(), entity.getEyePosition()) < range * range;
   }

   @NotNull
   public InteractionResult interact(Player player, InteractionHand hand) {
      if (!this.canUse(player)) {
         player.displayClientMessage(Component.translatable("gui.exposure.camera_stand.error.in_use").withStyle(ChatFormatting.RED), true);
         return InteractionResult.FAIL;
      } else if (this.isMalfunctioned()) {
         if (!this.isClientSide()) {
            this.setMalfunctioned(false);
            player.displayClientMessage(Component.translatable("gui.exposure.camera_stand.malfunction_fixed"), true);
            this.playSound(SoundEvents.SMITHING_TABLE_USE, 0.9F, 1.3F);
            this.showRepairingParticles();
         }

         return InteractionResult.SUCCESS;
      } else {
         ItemStack handStack = player.getItemInHand(hand);
         ItemStack cameraStack = this.getCamera();
         if (handStack.is(Items.DEBUG_STICK) && !cameraStack.isEmpty()) {
            this.malfunction();
            return InteractionResult.SUCCESS;
         } else if (cameraStack.isEmpty() && handStack.getItem() instanceof CameraItem) {
            this.setCamera(handStack);
            player.setItemInHand(hand, ItemStack.EMPTY);
            if (!this.isClientSide()) {
               this.playCameraSetSound();
            }

            if (this.getYRot() == 0.0F && this.getXRot() == 0.0F) {
               this.setYRot(Mth.wrapDegrees(player.getYRot()));
               if (!this.isClientSide()) {
                  this.syncRotationToClients();
               }
            }

            return InteractionResult.SUCCESS;
         } else if (cameraStack.isEmpty()) {
            return InteractionResult.PASS;
         } else if (cameraStack.getItem() instanceof CameraItem cameraItem) {
            if (PlatformHelper.isCreateDeployer(player, hand)) {
               return Config.Server.CREATE_DEPLOYER_STAND_HOTSWAP.get()
                  ? this.handleSneakInteraction(player, hand, cameraItem, cameraStack, handStack)
                  : InteractionResult.CONSUME;
            } else if (player.isSecondaryUseActive()) {
               return this.handleSneakInteraction(player, hand, cameraItem, cameraStack, handStack);
            } else {
               this.startControlling(player, cameraItem);
               return InteractionResult.CONSUME;
            }
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   protected InteractionResult handleSneakInteraction(Player player, InteractionHand hand, CameraItem cameraItem, ItemStack cameraStack, ItemStack handStack) {
      if (this.isOnCooldown()) {
         return InteractionResult.FAIL;
      } else if (cameraItem.getShutter().isOpen(cameraStack)) {
         return InteractionResult.FAIL;
      } else if (handStack.isEmpty() && cameraItem.hasAttachmentsMenu()) {
         return this.openAttachmentsMenu(player, hand);
      } else {
         InteractionResult result = cameraItem.handleStandSneakInteraction(this, player, hand, cameraStack);
         if (result == InteractionResult.FAIL) {
            return InteractionResult.FAIL;
         } else if (result == InteractionResult.PASS) {
            return cameraItem.hasAttachmentsMenu() ? this.openAttachmentsMenu(player, hand) : InteractionResult.PASS;
         } else {
            cameraItem.getTimer().stop(cameraStack);
            this.forceUpdate();
            return result;
         }
      }
   }

   public boolean canUse(Player player) {
      return (this.operator() == null || player.equals(this.operator()))
         && this.level.players().stream().filter(pl -> !pl.equals(player)).noneMatch(pl -> pl.containerMenu instanceof CameraOnStandAttachmentsMenu);
   }

   public InteractionResult openAttachmentsMenu(Player player, InteractionHand hand) {
      if (!this.canUse(player)) {
         player.displayClientMessage(Component.translatable("gui.exposure.camera_stand.error.in_use").withStyle(ChatFormatting.RED), true);
         return InteractionResult.FAIL;
      } else {
         final ItemStack cameraStack = this.getCamera();
         if (cameraStack.isEmpty() || !(cameraStack.getItem() instanceof CameraItem cameraItem)) {
            return InteractionResult.FAIL;
         } else if (cameraItem.getShutter().isOpen(cameraStack)) {
            player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
         } else {
            cameraItem.getOrCreateId(cameraStack);
            if (player instanceof ServerPlayer serverPlayer) {
               this.setOperator((CameraOperator)serverPlayer);
               cameraItem.getTimer().stop(cameraStack);
               MenuProvider menuProvider = new MenuProvider() {
                  @NotNull
                  public Component getDisplayName() {
                     return (Component)(DataComponents.CUSTOM_NAME.get(cameraStack) != null
                        ? cameraStack.getHoverName()
                        : Component.translatable("container.exposure.camera"));
                  }

                  @NotNull
                  public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                     return new CameraOnStandAttachmentsMenu(containerId, playerInventory, CameraStandEntity.this);
                  }
               };
               this.forceUpdate();
               PlatformHelper.openMenu(serverPlayer, menuProvider, buffer -> buffer.writeInt(this.getId()));
            }

            cameraItem.setDisassembled(cameraStack, true);
            Sound.play(player, Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), SoundSource.PLAYERS, 0.9F, 0.9F, 0.2F);
            return InteractionResult.SUCCESS;
         }
      }
   }

   public void startControlling(Player player, CameraItem cameraItem) {
      cameraItem.activateOnStand(player, this.getCamera(), this);
      this.setOperator((CameraOperator)player);
      if (this.getOwnerPlayerUuid().equals(Util.NIL_UUID)) {
         this.setOwnerPlayer(player);
      }

      if (this.isClientSide()) {
         CameraClient.setCameraEntity(this);
         Minecrft.stopPlayerMovement();
      } else {
         this.forceUpdate();
      }
   }

   public void stopControlling() {
      if (this.getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.isActive(this.getCamera())) {
         cameraItem.deactivate(this, this.getCamera());
      }

      if (this.operator() != null) {
         if (this.isClientSide()) {
            CameraClient.resetCameraEntity();
         } else if (this.operator() instanceof ServerPlayer serverPlayer) {
            Packets.sendToClient(new CameraStandStopControllingS2CP(this.getId()), serverPlayer);
         }
      }

      this.setOperator(null);
      if (!this.isClientSide()) {
         this.forceUpdate();
      }
   }

   public void release() {
      if (!this.isMalfunctioned() && !this.isOnCooldown() && this.getCamera().getItem() instanceof CameraItem cameraItem) {
         this.getPlayerExecutingExposure()
            .filter(player -> !this.shouldMalfunction(player))
            .ifPresentOrElse(player -> cameraItem.release(this, this.getCamera()), this::malfunction);
         this.forceUpdate();
      }
   }

   protected boolean shouldMalfunction(Player player) {
      return !(Boolean)Config.Server.CAMERA_STAND_FALLBACK_TO_OTHER_PLAYERS_PROJECTOR.get()
         && Attachment.FILTER.get(this.getCamera()).getItem() instanceof InterplanarProjectorItem
         && !player.getUUID().equals(this.getOwnerPlayerUuid());
   }

   protected void malfunction() {
      if (!this.getCamera().isEmpty()) {
         if (!!Config.Server.CAMERA_STAND_RANGE_MALFUNCTION.get()) {
            this.stopControlling();
            this.setMalfunctioned(true);
            this.playSound(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get());
         }
      }
   }

   public void tick() {
      super.tick();
      this.travel();
      this.checkForBoats();
      this.checkForMinecarts();
      this.pushEntities();
      if (this.getHurtTime() > 0) {
         this.setHurtTime(this.getHurtTime() - 1);
      }

      if (this.getDamage() > 0.0F) {
         this.setDamage(this.getDamage() - 1.0F);
      }

      int cooldown = this.getCooldown();
      if (cooldown > 0) {
         this.setCooldown(cooldown - 1);
      }

      if (!this.isClientSide()) {
         this.redstoneControl.tick();
         if (this.getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.tick(this, this.getCamera())) {
            this.forceUpdate();
         }

         CameraOperator operator = this.operator();
         if (operator == null) {
            if (this.getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.isActive(this.getCamera())) {
               cameraItem.deactivate(this, this.getCamera());
            }
         } else if (!this.isInInteractionRange(operator.asOperatorEntity())) {
            this.stopControlling();
         } else if (!this.isCameraActive() && (!(operator instanceof Player player) || !(player.containerMenu instanceof CameraOnStandAttachmentsMenu))) {
            this.stopControlling();
         }
      }
   }

   protected void travel() {
      this.xo = this.getX();
      this.yo = this.getY();
      this.zo = this.getZ();
      // 1.20.5 applyGravity()
      if (!this.isNoGravity()) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, -this.getDefaultGravity(), 0.0));
      }
      if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > 0.1F) {
         this.setUnderwaterMovement();
      } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > 0.1F) {
         this.setUnderLavaMovement();
      }

      if (this.level.isClientSide) {
         this.noPhysics = false;
      } else {
         this.noPhysics = !this.level.noCollision(this, this.getBoundingBox().deflate(1.0E-7));
         if (this.noPhysics) {
            this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
         }
      }

      if (!this.onGround || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
         this.move(MoverType.SELF, this.getDeltaMovement());
         float f = 0.98F;
         if (this.onGround) {
            f = this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98F;
         }

         this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 0.98, (double)f));
         if (this.onGround) {
            Vec3 vec32 = this.getDeltaMovement();
            if (vec32.y < 0.0) {
               this.setDeltaMovement(vec32.multiply(1.0, -0.5, 1.0));
            }
         }
      }
   }

   protected void setUnderwaterMovement() {
      Vec3 vec3 = this.getDeltaMovement();
      this.setDeltaMovement(vec3.x * 0.9F, vec3.y * 0.23F, vec3.z * 0.9F);
   }

   protected void setUnderLavaMovement() {
      Vec3 vec3 = this.getDeltaMovement();
      this.setDeltaMovement(vec3.x * 0.5, vec3.y * 0.1F, vec3.z * 0.5);
   }

   protected void checkForBoats() {
      if (!this.isClientSide() && !this.isPassenger()) {
         for (Entity entity : this.level.getEntities(this, this.getBoundingBox().inflate(0.4F, 0.2F, 0.4F), e -> e instanceof Boat)) {
            Boat boat = (Boat)entity;
            if (boat.getPassengers().size() < 2 && this.getBbWidth() < boat.getBbWidth()) {
               this.startRiding(boat);
            }
         }
      }
   }

   protected void checkForMinecarts() {
      if (!this.isClientSide() && !this.isPassenger()) {
         for (Entity entity : this.level.getEntities(this, this.getBoundingBox().inflate(0.4F, 0.2F, 0.4F), e -> {
            if (e instanceof AbstractMinecart cart && cart.getMinecartType() == Type.RIDEABLE) {
               return true;
            }

            return false;
         })) {
            AbstractMinecart minecart = (AbstractMinecart)entity;
            if (!minecart.isVehicle()) {
               this.startRiding(minecart);
            }
         }
      }
   }

   protected void pushEntities() {
      for (Entity entity : this.level.getEntities(this, this.getBoundingBox(), RIDABLE_MINECARTS)) {
         if (this.distanceToSqr(entity) <= 0.2) {
            entity.push(this);
         }
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.isRemoved()) {
         return true;
      } else if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         this.markHurt();
         if (!this.getCamera().isEmpty()) {
            if (!this.isClientSide()) {
               ItemEntity itemEntity = this.spawnAtLocation(this.getCamera(), this.getEyeHeight());
               if (itemEntity != null) {
                  itemEntity.setPickUpDelay(5);
               }

               this.playCameraRemoveSound();
            }

            this.setCamera(ItemStack.EMPTY);
            if (source.isCreativePlayer()) {
               return true;
            }

            amount = 1.0F;
         }

         if (!this.isClientSide()) {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.markHurt();
            this.setDamage(this.getDamage() + amount * 10.0F);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
            this.playHitSound();
            if (source.isCreativePlayer()) {
               this.discard();
               this.showBreakingParticles();
               this.playBreakSound();
            } else if (this.getDamage() > 10.0F) {
               this.destroy(source);
               this.showBreakingParticles();
               this.playBreakSound();
            }
         }

         return true;
      }
   }

   public void setHurtTime(int hurtTime) {
      this.entityData.set(DATA_ID_HURT, hurtTime);
   }

   public void setHurtDir(int hurtDir) {
      this.entityData.set(DATA_ID_HURTDIR, hurtDir);
   }

   public void setDamage(float damage) {
      this.entityData.set(DATA_ID_DAMAGE, damage);
   }

   public float getDamage() {
      return (Float)this.entityData.get(DATA_ID_DAMAGE);
   }

   public int getHurtTime() {
      return (Integer)this.entityData.get(DATA_ID_HURT);
   }

   public int getHurtDir() {
      return (Integer)this.entityData.get(DATA_ID_HURTDIR);
   }

   protected void destroy(DamageSource source) {
      this.destroy(this.getDropItem());
   }

   public void destroy(Item dropItem) {
      this.kill();
      if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
         ItemStack itemStack = new ItemStack(dropItem);
         DataComponents.CUSTOM_NAME.set(itemStack, this.getCustomName());
         this.spawnAtLocation(itemStack, 0.5F);
      }
   }

   protected void showBreakingParticles() {
      if (this.level instanceof ServerLevel) {
         ((ServerLevel)this.level)
            .sendParticles(
               new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()),
               this.getX(),
               this.getY(0.25),
               this.getZ(),
               6,
               (double)(this.getBbWidth() / 6.0F),
               (double)(this.getBbHeight() / 6.0F),
               (double)(this.getBbWidth() / 6.0F),
               0.05
            );
      }
   }

   protected void showRepairingParticles() {
      if (this.level instanceof ServerLevel) {
         ((ServerLevel)this.level)
            .sendParticles(
               new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HOPPER.defaultBlockState()),
               this.getX(),
               this.getY(0.75),
               this.getZ(),
               4,
               (double)(this.getBbWidth() / 8.0F),
               (double)(this.getBbHeight() / 8.0F),
               (double)(this.getBbWidth() / 8.0F),
               0.05
            );
      }
   }

   protected Item getDropItem() {
      return Exposure.Items.CAMERA_STAND.get();
   }

   public boolean isPickable() {
      return true;
   }

   @Nullable
   public ItemStack getPickResult() {
      return new ItemStack(this.getDropItem());
   }

   protected double getDefaultGravity() {
      return 0.08;
   }

   @NotNull
   public SoundSource getSoundSource() {
      return SoundSource.BLOCKS;
   }

   public void playPlaceSound() {
      this.level.playSound(null, this.getX(), this.getY(), this.getZ(), Exposure.SoundEvents.CAMERA_STAND_PLACE.get(), this.getSoundSource(), 0.8F, 1.0F);
   }

   public void playHitSound() {
      this.level.playSound(null, this.getX(), this.getY(), this.getZ(), Exposure.SoundEvents.CAMERA_STAND_HIT.get(), this.getSoundSource(), 0.8F, 1.0F);
   }

   public void playBreakSound() {
      this.level.playSound(null, this.getX(), this.getY(), this.getZ(), Exposure.SoundEvents.CAMERA_STAND_BREAK.get(), this.getSoundSource(), 1.0F, 1.0F);
   }

   public void playCameraSetSound() {
      this.level
         .playSound(null, this.getX(), this.getY(), this.getZ(), Exposure.SoundEvents.CAMERA_STAND_SET_CAMERA.get(), this.getSoundSource(), 0.8F, 1.0F);
   }

   public void playCameraRemoveSound() {
      this.level
         .playSound(null, this.getX(), this.getY(), this.getZ(), Exposure.SoundEvents.CAMERA_STAND_REMOVE_CAMERA.get(), this.getSoundSource(), 0.8F, 1.0F);
   }

   public void forceUpdate() {
      Backport.setEntityData(this.getEntityData(), DATA_ID_CAMERA, this.getCamera(), true);
   }

   public boolean isClientSide() {
      return this.level.isClientSide;
   }

   public boolean isControlledByLocalInstance() {
      return this.operator() instanceof Player player && player.isLocalPlayer() || super.isControlledByLocalInstance();
   }

   public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
      this.setPos(x, y, z);
   }

   public void syncRotationToClientsIfNeeded() {
      if (!this.isClientSide()) {
         if (this.operator() != null || this.tickCount % 10 == 0) {
            this.syncRotationToClients();
         }
      }
   }

   public void syncRotationToClients() {
      Packets.sendToClients(new CameraStandSetRotationsS2CP(this.getId(), this.getYRot(), this.getXRot()), pl -> !pl.equals(this.operator()));
   }
}
