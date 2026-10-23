package net.mehdinoui.fungidelight.common.event;

import net.mehdinoui.fungidelight.common.entity.ai.pig.TruffleDiggingGoal;
import net.mehdinoui.fungidelight.common.entity.ai.wolf.DogTruffleHuntGoal;
import net.mehdinoui.fungidelight.common.registry.ModSoundEvents;
import net.mehdinoui.fungidelight.common.tag.FungiDelightTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "fungidelight"
)
// Port change: no config switches (all upstream defaults were on).
public class ModEntitiesEvents {
   public static final String HUNTING_TAG = "fungidelight.dog_hunting_timer";
   public static final int HUNT_DURATION = 2400;

   @SubscribeEvent
   public static void onDogInteract(EntityInteract event) {
      if (event.getTarget() instanceof Wolf wolf && wolf.isTame()) {
         ItemStack itemStack = event.getItemStack();
         if (itemStack.is(FungiDelightTags.TRUFFLES)) {
            if (!event.getLevel().isClientSide) {
               long endTime = event.getLevel().getGameTime() + 2400L;
               wolf.getPersistentData().putLong("fungidelight.dog_hunting_timer", endTime);
               wolf.playSound((SoundEvent)ModSoundEvents.DOG_SNIFF_TRUFFLE.get());
            }

            if (event.getLevel().isClientSide) {
               for (int i = 0; i < 7; i++) {
                  double d0 = event.getLevel().random.nextGaussian() * 0.02;
                  double d1 = event.getLevel().random.nextGaussian() * 0.02;
                  double d2 = event.getLevel().random.nextGaussian() * 0.02;
                  event.getLevel().addParticle(ParticleTypes.HAPPY_VILLAGER, wolf.getRandomX(1.0), wolf.getRandomY() + 0.5, wolf.getRandomZ(1.0), d0, d1, d2);
               }
            }

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public static void onPigInteract(EntityInteract event) {
      if (event.getTarget() instanceof Pig pig
         && pig.isFood(event.getItemStack())
         && event.getItemStack().is(FungiDelightTags.TRUFFLES)
         && pig.getAge() == 0
         && !pig.isInLove()) {
         pig.getPersistentData().putBoolean("FD_TruffleBred", true);
         if (!event.getLevel().isClientSide) {
            pig.setInLove(event.getEntity());
            if (!event.getEntity().getAbilities().instabuild) {
               event.getItemStack().shrink(1);
            }
         }

         event.setCancellationResult(InteractionResult.SUCCESS);
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onBabySpawn(BabyEntitySpawnEvent event) {
      Level level = event.getParentA().getLevel();
      if (event.getParentA() instanceof Pig parentA && event.getParentB() instanceof Pig parentB) {
         CompoundTag var12 = parentA.getPersistentData();
         CompoundTag dataB = parentB.getPersistentData();
         if (var12.getBoolean("FD_TruffleBred") || dataB.getBoolean("FD_TruffleBred")) {
            if (level instanceof ServerLevel serverLevel) {
               float roll = parentA.getRandom().nextFloat();
               int extrasToSpawn = 0;
               if (roll < 0.1F) {
                  extrasToSpawn = 2;
               } else if (roll < 0.5F) {
                  extrasToSpawn = 1;
               }

               if (extrasToSpawn > 0) {
                  for (int i = 0; i < extrasToSpawn; i++) {
                     Pig extraBaby = parentA.getBreedOffspring(serverLevel, parentB);
                     if (extraBaby != null) {
                        extraBaby.setBaby(true);
                        extraBaby.moveTo(parentA.getX(), parentA.getY(), parentA.getZ(), 0.0F, 0.0F);
                        serverLevel.addFreshEntity(extraBaby);
                     }
                  }
               }
            }

            var12.remove("FD_TruffleBred");
            dataB.remove("FD_TruffleBred");
         }

         return;
      }
   }

   @SubscribeEvent
   public static void addCustomGoals(EntityJoinLevelEvent event) {
      if (event.getEntity() instanceof Pig pig) {
         if (pig.goalSelector.getAvailableGoals().stream().anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof TruffleDiggingGoal)) {
            return;
         }

         pig.goalSelector.addGoal(5, new TruffleDiggingGoal(pig));
      }

      if (event.getEntity() instanceof Wolf wolf) {
         if (wolf.goalSelector.getAvailableGoals().stream().anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof DogTruffleHuntGoal)) {
            return;
         }

         wolf.goalSelector.addGoal(6, new DogTruffleHuntGoal(wolf));
      }
   }

   @SubscribeEvent
   public static void onLivingTick(LivingTickEvent event) {
      if (!event.getEntity().getLevel().isClientSide
         && event.getEntity() instanceof Wolf wolf
         && wolf.getPersistentData().contains("fungidelight.dog_hunting_timer")) {
         long endTime = wolf.getPersistentData().getLong("fungidelight.dog_hunting_timer");
         if (wolf.getLevel().getGameTime() < endTime) {
            ServerLevel serverLevel = (ServerLevel)wolf.getLevel();
            if (wolf.tickCount % 4 == 0) {
               serverLevel.sendParticles(ParticleTypes.MYCELIUM, wolf.getX(), wolf.getY() + 0.5, wolf.getZ(), 3, 0.3, 0.3, 0.3, 0.05);
            }
         }
      }
   }
}
