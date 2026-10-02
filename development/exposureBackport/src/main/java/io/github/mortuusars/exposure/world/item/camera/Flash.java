package io.github.mortuusars.exposure.world.item.camera;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.block.FlashBlock;
import io.github.mortuusars.exposure.world.camera.component.FlashMode;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.sound.Sound;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Flash {
   public int getCooldown() {
      return 10;
   }

   public boolean isAvailable(ItemStack camera) {
      return Attachment.FLASH.isPresent(camera);
   }

   public boolean shouldFire(ItemStack camera, int lightLevel) {
      return switch ((FlashMode)CameraSettings.FLASH_MODE.getOrDefault(camera)) {
         case OFF -> false;
         case ON -> true;
         case AUTO -> lightLevel < 8;
      };
   }

   public boolean fire(CameraHolder holder, ServerLevel level, ItemStack stack) {
      Entity entity = holder.asHolderEntity();
      BlockPos flashPosition = this.findPosition(holder);
      if (flashPosition == null) {
         return false;
      } else {
         level.setBlock(
            flashPosition,
            (BlockState)Exposure.Blocks.FLASH
               .get()
               .defaultBlockState()
               .setValue(FlashBlock.WATERLOGGED, level.getFluidState(flashPosition).isSourceOfType(Fluids.WATER)),
            11
         );
         Sound.play(entity, this.getSound(), entity.getSoundSource());
         entity.gameEvent(GameEvent.PRIME_FUSE);
         holder.getServerPlayerAwardedForExposure().ifPresent(player -> player.awardStat(Exposure.Stats.FLASHES_TRIGGERED));
         this.sendParticles(holder, level);
         return true;
      }
   }

   @Nullable
   protected BlockPos findPosition(CameraHolder holder) {
      Entity entity = holder.asHolderEntity();
      BlockPos headPos = entity.blockPosition().above();
      if (this.isPositionSuitable(entity.level, headPos)) {
         return headPos;
      } else {
         for (Direction direction : Direction.values()) {
            BlockPos relative = headPos.relative(direction);
            if (this.isPositionSuitable(entity.level, relative)) {
               return relative;
            }
         }

         return null;
      }
   }

   protected boolean isPositionSuitable(Level level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      return state.isAir() || state.is(Blocks.WATER) && level.getFluidState(pos).isSourceOfType(Fluids.WATER);
   }

   protected void sendParticles(CameraHolder holder, ServerLevel level) {
      Vec3 pos = this.getFlashEffectsPosition(holder);
      ServerPlayer executingPlayer = holder.getServerPlayerExecutingExposure().orElse(null);
      level.players().stream().filter(player -> !player.equals(executingPlayer) && player.distanceTo(holder.asHolderEntity()) < 128.0F).forEach(player -> {
         level.sendParticles(player, ParticleTypes.FLASH, false, pos.x, pos.y, pos.z, 0, 0.0, 0.0, 0.0, 0.0);
         level.sendParticles(player, ParticleTypes.END_ROD, false, pos.x, pos.y, pos.z, 4, 0.2, 0.2, 0.2, 0.1);
      });
   }

   protected Vec3 getFlashEffectsPosition(CameraHolder holder) {
      return holder.asHolderEntity().position().add(0.0, 1.1, 0.0).add(holder.asHolderEntity().getLookAngle().scale(0.8));
   }

   public SoundEvent getSound() {
      return Exposure.SoundEvents.FLASH.get();
   }
}
