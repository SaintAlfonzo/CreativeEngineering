package io.github.mortuusars.exposure.world.entity;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.PhotographFrameItem;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import io.github.mortuusars.exposure.backport.util.Backport;

public class PhotographFrameEntity extends HangingEntity {
   public static final Logger LOGGER = Exposure.LOGGER;
   protected static final EntityDataAccessor<Integer> DATA_SIZE = SynchedEntityData.defineId(PhotographFrameEntity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<ItemStack> DATA_FRAME_ITEM = SynchedEntityData.defineId(
      PhotographFrameEntity.class, EntityDataSerializers.ITEM_STACK
   );
   protected static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(PhotographFrameEntity.class, EntityDataSerializers.ITEM_STACK);
   protected static final EntityDataAccessor<Integer> DATA_ITEM_ROTATION = SynchedEntityData.defineId(PhotographFrameEntity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Boolean> DATA_GLOWING = SynchedEntityData.defineId(PhotographFrameEntity.class, EntityDataSerializers.BOOLEAN);
   protected int size = 0;

   public PhotographFrameEntity(EntityType<? extends PhotographFrameEntity> entityType, Level level) {
      super(entityType, level);
   }

   public PhotographFrameEntity(Level level, BlockPos pos, Direction facingDirection) {
      this(Exposure.EntityTypes.PHOTOGRAPH_FRAME.get(), level, pos, facingDirection);
   }

   protected PhotographFrameEntity(EntityType<? extends PhotographFrameEntity> entityType, Level level, BlockPos pos, Direction facingDirection) {
      super(entityType, level, pos);
      this.setDirection(facingDirection);
      this.setItem(ItemStack.EMPTY);
   }

   public Component getDisplayName() {
      ItemStack item = this.getItem();
      return !item.isEmpty() ? item.getHoverName() : CommonComponents.EMPTY;
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      if (!this.level.isClientSide) {
         double d = 64.0 * getViewScale();
         return distance < d * d;
      } else {
         double d = (double)((Integer)Config.Client.PHOTOGRAPH_FRAME_CULLING_DISTANCE.get()).intValue() * getViewScale();
         return distance < d * d;
      }
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      if (key.equals(DATA_ITEM)) {
         this.onItemChanged(this.getItem());
      }

      if (key.equals(DATA_SIZE)) {
         this.size = (Integer)this.getEntityData().get(DATA_SIZE);
         this.recalculateBoundingBox();
      }
   }

   public void recreateFromPacket(@NotNull ClientboundAddEntityPacket packet) {
      super.recreateFromPacket(packet);
      int packedData = packet.getData();
      int size = packedData >> 8 & 0xFF;
      int direction = packedData & 0xFF;
      this.setSize(size);
      this.setDirection(Direction.from3DDataValue(direction));
   }

   @NotNull
   public Packet<?> getAddEntityPacket() {
      int packedData = this.size << 8 | this.direction.get3DDataValue();
      return new ClientboundAddEntityPacket(this, packedData, this.getPos());
   }

   public void addAdditionalSaveData(@NotNull CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      ItemStack item = this.getItem();
      if (!item.isEmpty()) {
         tag.put("Item", item.save(new CompoundTag()));
         tag.putBoolean("IsGlowing", this.isGlowing());
         tag.putByte("ItemRotation", (byte)this.getItemRotation());
      }

      ItemStack frameItem = this.getFrameItem();
      if (!frameItem.isEmpty()) {
         tag.put("FrameItem", frameItem.save(new CompoundTag()));
      }

      tag.putByte("Size", (byte)this.getSize());
      tag.putByte("Facing", (byte)this.direction.get3DDataValue());
      tag.putBoolean("Invisible", this.isInvisible());
   }

   public void readAdditionalSaveData(@NotNull CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      CompoundTag frameItemTag = tag.getCompound("FrameItem");
      if (!frameItemTag.isEmpty()) {
         ItemStack stack = Backport.parseItemStack(frameItemTag).orElse(new ItemStack(this.getBaseFrameItem()));
         this.setFrameItem(stack);
      }

      CompoundTag itemTag = tag.getCompound("Item");
      if (!itemTag.isEmpty()) {
         ItemStack itemstack = Backport.parseItemStack(itemTag).orElse(ItemStack.EMPTY);
         this.setItem(itemstack);
         this.setGlowing(tag.getBoolean("IsGlowing"));
         this.setItemRotation(tag.getByte("ItemRotation"));
      }

      this.setSize(tag.getByte("Size"));
      this.setDirection(Direction.from3DDataValue(tag.getByte("Facing")));
      this.setInvisible(tag.getBoolean("Invisible"));
   }

   @NotNull
   public Vec3 trackingPosition() {
      return Vec3.atLowerCornerOf(this.pos);
   }

   /** 1.19.2's HangingEntity computes the box itself; upstream (1.21) supplies it via calculateBoundingBox. */
   @Override
   protected void recalculateBoundingBox() {
      if (this.direction != null) {
         this.setBoundingBox(this.calculateBoundingBox(this.pos, this.direction));
      }
   }

   public int getWidth() {
      return this.getSize() * 16 + 16;
   }

   public int getHeight() {
      return this.getSize() * 16 + 16;
   }

   @Nullable
   public ItemStack getPickResult() {
      ItemStack item = this.getItem();
      return !item.isEmpty() ? item.copy() : this.getFrameItem().copy();
   }

   @NotNull
   protected AABB calculateBoundingBox(BlockPos pos, Direction direction) {
      double x = (double)pos.getX() + 0.5;
      double y = (double)pos.getY() + 0.5;
      double z = (double)pos.getZ() + 0.5;
      double widthOffset = this.getWidth() % 32 == 0 ? 0.5 : 0.0;
      double heightOffset = this.getHeight() % 32 == 0 ? 0.5 : 0.0;
      if (this.getSize() == 2) {
         widthOffset++;
         heightOffset++;
      }

      double hangOffset = 0.46875;
      if (direction.getAxis().isHorizontal()) {
         x -= (double)this.getDirection().getStepX() * hangOffset;
         z -= (double)this.getDirection().getStepZ() * hangOffset;
         Direction ccwDirection = direction.getCounterClockWise();
         double var24;
         double var26;
         double var29;
         this.setPosRaw(
            var24 = x + widthOffset * (double)ccwDirection.getStepX(), var26 = y + heightOffset, var29 = z + widthOffset * (double)ccwDirection.getStepZ()
         );
         double xSize = (double)this.getWidth();
         double ySize = (double)this.getHeight();
         double zSize = (double)this.getWidth();
         if (this.getDirection().getAxis() == Axis.Z) {
            zSize = 1.0;
         } else {
            xSize = 1.0;
         }

         double var32;
         double var34;
         double var35;
         return new AABB(
            var24 - (var32 = xSize / 32.0), var26 - (var34 = ySize / 32.0), var29 - (var35 = zSize / 32.0), var24 + var32, var26 + var34, var29 + var35
         );
      } else {
         y -= (double)this.getDirection().getStepY() * hangOffset;
         double var22;
         double var27;
         this.setPosRaw(var22 = x + widthOffset, y, var27 = z - heightOffset);
         double xSize = (double)this.getWidth();
         double zSize = (double)this.getHeight();
         double var30;
         double var33;
         return new AABB(var22 - (var30 = xSize / 32.0), y - 0.03125, var27 - (var33 = zSize / 32.0), var22 + var30, y + 0.03125, var27 + var33);
      }
   }

   public boolean survives() {
      if (!this.level.noCollision(this)) {
         return false;
      } else {
         int sizeX = Math.max(1, this.getWidth() / 16);
         int sizeY = Math.max(1, this.getHeight() / 16);
         BlockPos baseBlockPos = this.pos.relative(this.direction.getOpposite());
         if (this.getDirection().getAxis().isHorizontal()) {
            Direction direction = this.getDirection().getCounterClockWise();
            MutableBlockPos mPos = new MutableBlockPos();

            for (int pX = 0; pX < sizeX; pX++) {
               for (int pY = 0; pY < sizeY; pY++) {
                  mPos.set(baseBlockPos).move(direction, pX).move(Direction.UP, pY);
                  BlockState blockState = this.level.getBlockState(mPos);
                  if (!blockState.getMaterial().isSolid() && !DiodeBlock.isDiode(blockState)) {
                     return false;
                  }
               }
            }
         } else {
            MutableBlockPos mPos = new MutableBlockPos();

            for (int pX = 0; pX < sizeX; pX++) {
               for (int pYx = 0; pYx < sizeY; pYx++) {
                  mPos.set(baseBlockPos).move(Direction.NORTH, pX).move(Direction.EAST, pYx);
                  BlockState blockState = this.level.getBlockState(mPos);
                  if (!blockState.getMaterial().isSolid() && !DiodeBlock.isDiode(blockState)) {
                     return false;
                  }
               }
            }
         }

         return this.level.getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
      }
   }

   protected void setDirection(@NotNull Direction facingDirection) {
      Preconditions.checkNotNull(facingDirection);
      this.direction = facingDirection;
      if (facingDirection.getAxis().isHorizontal()) {
         this.setXRot(0.0F);
         this.setYRot((float)(this.direction.get2DDataValue() * 90));
      } else {
         this.setXRot((float)(-90 * facingDirection.getAxisDirection().getStep()));
         this.setYRot(0.0F);
      }

      this.xRotO = this.getXRot();
      this.yRotO = this.getYRot();
      this.recalculateBoundingBox();
   }

   public int getSize() {
      return this.size;
   }

   public void setSize(int size) {
      this.getEntityData().set(DATA_SIZE, Mth.clamp(size, 0, 2));
      this.size = size;
      this.recalculateBoundingBox();
   }

   public PhotographFrameItem getBaseFrameItem() {
      return Exposure.Items.PHOTOGRAPH_FRAME.get();
   }

   public ItemStack getFrameItem() {
      return (ItemStack)this.getEntityData().get(DATA_FRAME_ITEM);
   }

   public void setFrameItem(ItemStack stack) {
      this.getEntityData().set(DATA_FRAME_ITEM, stack);
   }

   public ItemStack getItem() {
      return (ItemStack)this.getEntityData().get(DATA_ITEM);
   }

   public void setItem(ItemStack stack) {
      this.getEntityData().set(DATA_ITEM, stack);
   }

   protected void onItemChanged(ItemStack itemStack) {
      if (!itemStack.isEmpty()) {
         itemStack.setEntityRepresentation(this);
      }
   }

   public int getItemRotation() {
      return (Integer)this.getEntityData().get(DATA_ITEM_ROTATION);
   }

   public void setItemRotation(int rotation) {
      this.getEntityData().set(DATA_ITEM_ROTATION, rotation % 4);
   }

   public boolean isGlowing() {
      return (Boolean)this.getEntityData().get(DATA_GLOWING);
   }

   public void setGlowing(boolean glowing) {
      this.getEntityData().set(DATA_GLOWING, glowing);
   }

   public boolean isFrameInvisible() {
      return this.isInvisible();
   }

   @NotNull
   public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack itemInHand = player.getItemInHand(hand);
      if (itemInHand.getItem() instanceof PhotographItem && this.getItem().isEmpty()) {
         this.setItem(itemInHand.copy());
         itemInHand.shrink(1);
         this.gameEvent(GameEvent.BLOCK_CHANGE, player);
         this.playSound(this.getAddItemSound(), 1.0F, 1.0F);
         return InteractionResult.SUCCESS;
      } else if (itemInHand.is(Items.GLOW_INK_SAC) && !this.isGlowing()) {
         this.setGlowing(true);
         itemInHand.shrink(1);
         if (!this.level.isClientSide) {
            this.playSound(SoundEvents.GLOW_INK_SAC_USE);
            this.gameEvent(GameEvent.BLOCK_CHANGE, player);
         }

         return InteractionResult.SUCCESS;
      } else if (!this.getItem().isEmpty()) {
         if (!this.level.isClientSide) {
            this.playSound(this.getRotateSound(), 1.0F, this.level.getRandom().nextFloat() * 0.2F + 0.9F);
            this.setItemRotation(this.getItemRotation() + 1);
            this.gameEvent(GameEvent.BLOCK_CHANGE, player);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public boolean hurt(@NotNull DamageSource damageSource, float amount) {
      if (this.isInvulnerableTo(damageSource)) {
         return false;
      } else if (!damageSource.isExplosion() && !this.getItem().isEmpty()) {
         if (!this.level.isClientSide) {
            this.dropItem(damageSource.getEntity(), false);
            this.gameEvent(GameEvent.BLOCK_CHANGE, damageSource.getEntity());
            this.playSound(this.getRemoveItemSound(), 1.0F, 1.0F);
         }

         return true;
      } else {
         return super.hurt(damageSource, amount);
      }
   }

   public void dropItem(@Nullable Entity brokenEntity) {
      this.playSound(this.getBreakSound(), 1.0F, 1.0F);
      this.dropItem(brokenEntity, true);
      this.gameEvent(GameEvent.BLOCK_CHANGE, brokenEntity);
   }

   protected void dropItem(@Nullable Entity entity, boolean dropSelf) {
      ItemStack itemStack = this.getItem();
      this.setItem(ItemStack.EMPTY);
      if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
         if (entity instanceof Player player && player.isCreative()) {
            return;
         }

         float yOffset = this.getDirection() == Direction.DOWN ? -0.3F : 0.0F;
         if (dropSelf) {
            this.spawnAtLocation(this.getFrameItem(), yOffset);
         }

         if (!itemStack.isEmpty()) {
            this.spawnAtLocation(itemStack.copy(), yOffset);
         }
      }
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_SIZE, 0);
      this.entityData.define(DATA_FRAME_ITEM, ItemStack.EMPTY);
      this.entityData.define(DATA_ITEM, ItemStack.EMPTY);
      this.entityData.define(DATA_ITEM_ROTATION, 0);
      this.entityData.define(DATA_GLOWING, false);
   }

   public void tick() {
      super.tick();
      if (this.level.isClientSide && this.isGlowing() && this.level.getRandom().nextFloat() < 0.003F) {
         AABB bb = this.getBoundingBox();
         Vec3i normal = this.getDirection().getNormal();
         this.level
            .addParticle(
               ParticleTypes.END_ROD,
               this.position().x + ((double)this.level.getRandom().nextFloat() * bb.getXsize() * 0.75 - bb.getXsize() * 0.75 / 2.0),
               this.position().y + ((double)this.level.getRandom().nextFloat() * bb.getYsize() * 0.75 - bb.getYsize() * 0.75 / 2.0),
               this.position().z + ((double)this.level.getRandom().nextFloat() * bb.getZsize() * 0.75 - bb.getZsize() * 0.75 / 2.0),
               (double)(this.level.getRandom().nextFloat() * 0.02F * (float)normal.getX()),
               (double)(this.level.getRandom().nextFloat() * 0.02F * (float)normal.getY()),
               (double)(this.level.getRandom().nextFloat() * 0.02F * (float)normal.getZ())
            );
      }
   }

   @NotNull
   public SlotAccess getSlot(int slot) {
      return slot == 0 ? new SlotAccess() {
         @NotNull
         public ItemStack get() {
            return PhotographFrameEntity.this.getItem();
         }

         public boolean set(ItemStack carried) {
            PhotographFrameEntity.this.setItem(carried);
            return true;
         }
      } : super.getSlot(slot);
   }

   @NotNull
   protected Component getTypeName() {
      return (Component)(this.isGlowing() ? Component.translatable("entity.exposure.glow_photograph_frame") : super.getTypeName());
   }

   public void playPlacementSound() {
      this.playSound(this.getPlaceSound(), 1.0F, this.level.getRandom().nextFloat() * 0.2F + 0.7F);
   }

   public SoundEvent getPlaceSound() {
      return Exposure.SoundEvents.PHOTOGRAPH_FRAME_PLACE.get();
   }

   public SoundEvent getBreakSound() {
      return Exposure.SoundEvents.PHOTOGRAPH_FRAME_BREAK.get();
   }

   public SoundEvent getAddItemSound() {
      return Exposure.SoundEvents.PHOTOGRAPH_FRAME_ADD_ITEM.get();
   }

   public SoundEvent getRemoveItemSound() {
      return Exposure.SoundEvents.PHOTOGRAPH_FRAME_REMOVE_ITEM.get();
   }

   public SoundEvent getRotateSound() {
      return Exposure.SoundEvents.PHOTOGRAPH_FRAME_ROTATE_ITEM.get();
   }
}
