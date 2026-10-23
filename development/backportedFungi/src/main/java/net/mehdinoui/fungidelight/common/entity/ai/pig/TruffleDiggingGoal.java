package net.mehdinoui.fungidelight.common.entity.ai.pig;

import java.util.EnumSet;
import net.mehdinoui.fungidelight.common.registry.ModItems;
import net.mehdinoui.fungidelight.common.registry.ModSoundEvents;
import net.mehdinoui.fungidelight.common.tag.FungiDelightTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

// Port changes: digging asks the Forge mob griefing event first (Friendly Griefing in the pack decides for pigs); when it
// is denied the pig neither digs nor changes the block. The chance is upstream's default, hard-coded. 1.19.2 loot call,
// with the chest parameter set the table declares (upstream passed ORIGIN to the barter set, which does not allow it).
public class TruffleDiggingGoal extends Goal {
   private final Pig pig;
   private final Level level;
   private BlockPos targetBlock = null;
   public static final ResourceLocation DIGGING_LOOT = new ResourceLocation("fungidelight", "gameplay/pig_digging");
   private int digTime = 0;
   private int offBlockTimer = 0;
   private int cooldown = 0;
   private boolean hasFinishedDigging = false;
   // Upstream's config default (chancePigDigging), hard-coded: about one attempt per 4000 ticks.
   private static final float CHANCE_PIG_DIGGING = 2.5E-4F;
   private static final int SEARCH_RADIUS = 6;
   private static final int DIG_DURATION = 60;
   private static final int MAX_GIVE_UP_TICKS = 120;

   public TruffleDiggingGoal(Pig pig) {
      this.pig = pig;
      this.level = pig.getLevel();
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   private void lookAtTarget() {
      if (this.targetBlock != null) {
         this.pig
            .getLookControl()
            .setLookAt(
               (double)this.targetBlock.getX() + 0.5,
               (double)this.targetBlock.getY() + 0.5,
               (double)this.targetBlock.getZ() + 0.5,
               10.0F,
               (float)this.pig.getMaxHeadXRot()
            );
      }
   }

   private void navigateToTarget() {
      if (this.targetBlock != null) {
         this.pig
            .getNavigation()
            .moveTo((double)this.targetBlock.getX() + 0.5, (double)this.targetBlock.getY() + 1.0, (double)this.targetBlock.getZ() + 0.5, 1.0);
      }
   }

   private BlockPos findDiggableBlock() {
      BlockPos origin = this.pig.blockPosition();
      BlockPos best = null;
      double bestDist = Double.MAX_VALUE;

      for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-6, -1, -6), origin.offset(6, 1, 6))) {
         if (this.level.getBlockState(pos).is(FungiDelightTags.PIG_CAN_DIG_UP) && this.level.isEmptyBlock(pos.above())) {
            double dist = pos.distSqr(origin);
            if (dist < bestDist) {
               bestDist = dist;
               best = pos.immutable();
            }
         }
      }

      return best;
   }

   private void dropLoot() {
      if (this.level instanceof ServerLevel serverLevel) {
         BlockState state = this.level.getBlockState(this.targetBlock);
         if (state.is(FungiDelightTags.TRUFFLE_ORE)) {
            int count = 1 + this.level.random.nextInt(3);
            ItemStack truffles = new ItemStack((ItemLike)ModItems.TRUFFLE.get(), count);
            ItemEntity itemEntity = new ItemEntity(
               this.level,
               (double)this.targetBlock.getX() + 0.5,
               (double)this.targetBlock.getY() + 1.0,
               (double)this.targetBlock.getZ() + 0.5,
               truffles
            );
            itemEntity.setDeltaMovement(0.0, 0.25, 0.0);
            this.level.addFreshEntity(itemEntity);
         } else {
            LootTable lootTable = serverLevel.getServer().getLootTables().get(DIGGING_LOOT);

            for (ItemStack item : lootTable.getRandomItems(
               new LootContext.Builder(serverLevel)
                  .withParameter(LootContextParams.ORIGIN, this.pig.position())
                  .withParameter(LootContextParams.THIS_ENTITY, this.pig)
                  .withRandom(this.pig.getRandom())
                  .create(LootContextParamSets.CHEST)
            )) {
               ItemEntity itemEntity = new ItemEntity(
                  this.level,
                  (double)this.targetBlock.getX() + 0.5,
                  (double)this.targetBlock.getY() + 1.0,
                  (double)this.targetBlock.getZ() + 0.5,
                  item
               );
               itemEntity.setDeltaMovement(0.0, 0.25, 0.0);
               this.level.addFreshEntity(itemEntity);
            }
         }
      }
   }

   public boolean canUse() {
      if (this.cooldown > 0) {
         this.cooldown--;
         return false;
      } else if (!this.level.shouldTickBlocksAt(this.pig.blockPosition())) {
         return false;
      } else if (this.pig.isOnGround() && !this.pig.isBaby()) {
         if (this.level.random.nextFloat() >= CHANCE_PIG_DIGGING) {
            return false;
         } else if (!ForgeEventFactory.getMobGriefingEvent(this.level, this.pig)) {
            return false;
         } else {
            this.targetBlock = this.findDiggableBlock();
            return this.targetBlock != null;
         }
      } else {
         return false;
      }
   }

   public boolean canContinueToUse() {
      boolean targetBlockStillValid = this.targetBlock != null && this.level.getBlockState(this.targetBlock).is(FungiDelightTags.PIG_CAN_DIG_UP);
      return this.digTime > 0 && targetBlockStillValid && this.pig.hurtTime <= 0 && this.pig.isOnGround() && this.offBlockTimer < 120 && !this.hasFinishedDigging;
   }

   public void start() {
      this.digTime = 60;
      this.offBlockTimer = 0;
      this.hasFinishedDigging = false;
      this.pig.getNavigation().stop();
      this.navigateToTarget();
      this.level.playSound(null, this.pig, (SoundEvent)ModSoundEvents.PIG_HAPPY.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
      this.lookAtTarget();
   }

   public void tick() {
      if (this.targetBlock != null) {
         Vec3 targetCenter = new Vec3(
            (double)this.targetBlock.getX() + 0.5, (double)this.targetBlock.getY() + 1.0, (double)this.targetBlock.getZ() + 0.5
         );
         double dx = this.pig.getX() - targetCenter.x;
         double dz = this.pig.getZ() - targetCenter.z;
         double distSqrHorizontal = dx * dx + dz * dz;
         if (!(distSqrHorizontal > 2.25)) {
            this.offBlockTimer = 0;
            this.pig.getNavigation().stop();
            this.lookAtTarget();
            if (this.targetBlock != null && this.level.getBlockState(this.targetBlock).is(FungiDelightTags.PIG_CAN_DIG_UP)) {
               this.digTime = Math.max(0, this.digTime - 1);
               if (this.digTime > 0) {
                  if (this.digTime % 5 == 0) {
                     this.level.broadcastEntityEvent(this.pig, (byte)10);
                  }

                  if (this.digTime % 5 == 0 && this.level instanceof ServerLevel serverLevel) {
                     BlockState blockState = this.level.getBlockState(this.targetBlock);
                     serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                        (double)this.targetBlock.getX() + 0.5,
                        (double)this.targetBlock.getY() + 1.0,
                        (double)this.targetBlock.getZ() + 0.5,
                        6,
                        0.3,
                        0.1,
                        0.3,
                        0.15
                     );
                     this.level.playSound(null, this.targetBlock, (SoundEvent)ModSoundEvents.PIG_DIG.get(), SoundSource.NEUTRAL, 0.5F, 0.8F);
                  }
               } else {
                  this.hasFinishedDigging = true;
               }
            } else {
               this.digTime = 0;
            }
         } else {
            this.offBlockTimer++;
            if (this.pig.getNavigation().isDone() || this.offBlockTimer % 10 == 0) {
               this.navigateToTarget();
            }
         }
      }
   }

   public void stop() {
      if (this.hasFinishedDigging
         && this.targetBlock != null
         && this.level.getBlockState(this.targetBlock).is(FungiDelightTags.PIG_CAN_DIG_UP)
         && ForgeEventFactory.getMobGriefingEvent(this.level, this.pig)) {
         this.level.playSound(null, this.targetBlock, SoundEvents.MUD_BREAK, SoundSource.NEUTRAL, 1.0F, 1.0F);
         this.level.playSound(null, this.targetBlock, (SoundEvent)ModSoundEvents.PIG_DROP.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
         this.dropLoot();
         this.level.setBlockAndUpdate(this.targetBlock, Blocks.COARSE_DIRT.defaultBlockState());
         this.cooldown = 2400;
      } else {
         this.cooldown = 200;
      }

      this.targetBlock = null;
      this.hasFinishedDigging = false;
      this.pig.getNavigation().stop();
   }
}
