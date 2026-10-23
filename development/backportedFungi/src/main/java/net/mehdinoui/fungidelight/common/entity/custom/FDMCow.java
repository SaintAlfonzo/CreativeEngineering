package net.mehdinoui.fungidelight.common.entity.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

// Port change: upstream's canMate and getBreedOffspring overrides are gone. Mooshroom breeding (every mooshroom type with
// every other, calf type 50 % each parent) is patcherMod's mixin on MushroomCow, which this class inherits. Kept: Fungi
// cows ignore lightning, shear into a cow and 5 of their mushroom, render their own mushroom.
public class FDMCow extends MushroomCow {
   private final Supplier<Block> mushroom;

   public FDMCow(EntityType<? extends MushroomCow> type, Level level, Supplier<Block> mushroom) {
      super(type, level);
      this.mushroom = mushroom;
   }

   public BlockState getMushroomBlockState() {
      return this.mushroom.get().defaultBlockState();
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));
      this.goalSelector.addGoal(2, new BreedGoal(this, 1.0, MushroomCow.class));
      this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(new ItemLike[]{Items.WHEAT}), false));
      this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
   }

   public void thunderHit(ServerLevel level, LightningBolt bolt) {
   }

   public List<ItemStack> onSheared(@Nullable Player player, @NotNull ItemStack item, Level world, BlockPos pos, int fortune) {
      this.gameEvent(GameEvent.SHEAR, player);
      this.getLevel().playSound(null, this, SoundEvents.MOOSHROOM_SHEAR, player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, 1.0F, 1.0F);
      if (!this.getLevel().isClientSide()) {
         Cow cow = (Cow)EntityType.COW.create(this.getLevel());
         if (cow != null) {
            ((ServerLevel)this.getLevel()).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            cow.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            cow.setHealth(this.getHealth());
            cow.yBodyRot = this.yBodyRot;
            if (this.hasCustomName()) {
               cow.setCustomName(this.getCustomName());
               cow.setCustomNameVisible(this.isCustomNameVisible());
            }

            if (this.isPersistenceRequired()) {
               cow.setPersistenceRequired();
            }

            cow.setInvulnerable(this.isInvulnerable());
            this.discard();
            this.getLevel().addFreshEntity(cow);
            List<ItemStack> items = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
               items.add(new ItemStack((ItemLike)this.mushroom.get()));
            }

            return items;
         }
      }

      return Collections.emptyList();
   }
}
