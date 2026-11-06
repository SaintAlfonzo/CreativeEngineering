package net.mehdinoui.fungidelight.common.entity.ai.wolf;

import java.util.EnumSet;
import net.mehdinoui.fungidelight.common.registry.ModSoundEvents;
import net.mehdinoui.fungidelight.common.tag.FungiDelightTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DogTruffleHuntGoal extends Goal {
   private final Wolf wolf;
   private final Level level;
   private BlockPos targetTrufflePos;
   private BlockPos targetSurfacePos;
   private int barkTimer = 9;
   private int offBlockTimer = 9;
   private static final int SEARCH_RADIUS = 12;
   private static final int SEARCH_DEPTH = 4;
   private static final int SUCCESS_TIME = 40;
   private static final int MAX_GIVE_UP_TICKS = 100;

   public DogTruffleHuntGoal(Wolf wolf) {
      this.wolf = wolf;
      this.level = wolf.getLevel();
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   private boolean isHunting() {
      if (!this.wolf.getPersistentData().contains("fungidelight.dog_hunting_timer")) {
         return false;
      } else {
         long endTime = this.wolf.getPersistentData().getLong("fungidelight.dog_hunting_timer");
         return this.level.getGameTime() < endTime;
      }
   }

   private void completeHunt() {
      this.wolf.getPersistentData().remove("fungidelight.dog_hunting_timer");
   }

   private BlockPos findNearestTruffle() {
      BlockPos origin = this.wolf.blockPosition();
      BlockPos best = null;
      double bestDist = Double.MAX_VALUE;

      for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-12, -4, -12), origin.offset(12, -1, 12))) {
         if (this.level.getBlockState(pos).is(FungiDelightTags.TRUFFLE_ORE)) {
            double dist = pos.distSqr(origin);
            if (dist < bestDist) {
               bestDist = dist;
               best = pos.immutable();
            }
         }
      }

      return best;
   }

   public boolean canUse() {
      if (this.wolf.isTame() && !this.wolf.isOrderedToSit() && this.wolf.getTarget() == null && this.isHunting()) {
         this.targetTrufflePos = this.findNearestTruffle();
         return this.targetTrufflePos != null;
      } else {
         return false;
      }
   }

   public boolean canContinueToUse() {
      boolean targetBlockStillValid = this.targetTrufflePos != null && this.level.getBlockState(this.targetTrufflePos).is(FungiDelightTags.TRUFFLE_ORE);
      return this.isHunting() && targetBlockStillValid && !this.wolf.isOrderedToSit() && this.offBlockTimer < 100 && this.wolf.getTarget() == null;
   }

   public void start() {
      this.barkTimer = 0;
      this.offBlockTimer = 0;
      this.targetSurfacePos = this.targetTrufflePos.above();

      while (this.level.getBlockState(this.targetSurfacePos).isSolidRender(this.level, this.targetSurfacePos)) {
         this.targetSurfacePos = this.targetSurfacePos.above();
         if (this.targetSurfacePos.getY() > this.targetTrufflePos.getY() + 5) {
            break;
         }
      }

      boolean pathSuccess = this.wolf
         .getNavigation()
         .moveTo(
            (double)this.targetSurfacePos.getX() + 0.5, (double)this.targetSurfacePos.getY(), (double)this.targetSurfacePos.getZ() + 0.5, 1.2
         );
      if (!pathSuccess) {
         this.offBlockTimer = 100;
         this.wolf.getNavigation().stop();
      }

      this.wolf.playSound((SoundEvent)ModSoundEvents.DOG_FOUND_TRUFFLE.get());
   }

   public void tick() {
      if (this.targetSurfacePos != null && this.level instanceof ServerLevel serverLevel) {
         double distSqr = this.wolf
            .distanceToSqr(
               (double)this.targetSurfacePos.getX() + 0.5, (double)this.targetSurfacePos.getY(), (double)this.targetSurfacePos.getZ() + 0.5
            );
         if (!(distSqr > 2.5)) {
            this.offBlockTimer = 0;
            this.wolf.getNavigation().stop();
            this.wolf
               .getLookControl()
               .setLookAt(
                  (double)this.targetSurfacePos.getX() + 0.5,
                  (double)this.targetSurfacePos.getY() - 0.5,
                  (double)this.targetSurfacePos.getZ() + 0.5,
                  10.0F,
                  (float)this.wolf.getMaxHeadXRot()
               );
            if (this.targetTrufflePos != null && this.level.getBlockState(this.targetTrufflePos).is(FungiDelightTags.TRUFFLE_ORE)) {
               if (this.barkTimer == 0) {
                  this.wolf.playSound((SoundEvent)ModSoundEvents.DOG_ALERT_TRUFFLE.get());
               }

               if (this.barkTimer % 5 == 0) {
                  BlockState blockState = this.level.getBlockState(this.targetSurfacePos.below());
                  serverLevel.sendParticles(
                     new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                     (double)this.targetSurfacePos.getX() + 0.5,
                     (double)this.targetSurfacePos.getY() + 0.2,
                     (double)this.targetSurfacePos.getZ() + 0.5,
                     6,
                     0.3,
                     0.1,
                     0.3,
                     0.15
                  );
               }

               this.barkTimer++;
               if (this.barkTimer > 40) {
                  this.completeHunt();
               }
            } else {
               this.barkTimer = 0;
            }
         } else {
            this.offBlockTimer++;
            if (this.wolf.getNavigation().isDone() || this.offBlockTimer % 20 == 0) {
               boolean pathSuccess = this.wolf
                  .getNavigation()
                  .moveTo(
                     (double)this.targetSurfacePos.getX() + 0.5,
                     (double)this.targetSurfacePos.getY(),
                     (double)this.targetSurfacePos.getZ() + 0.5,
                     1.2
                  );
               if (!pathSuccess) {
                  this.offBlockTimer += 10;
               }
            }
         }
      }
   }

   public void stop() {
      if (this.barkTimer <= 40) {
         this.completeHunt();
      }

      this.targetTrufflePos = null;
      this.targetSurfacePos = null;
      this.barkTimer = 0;
      this.offBlockTimer = 0;
      this.wolf.getNavigation().stop();
   }
}
