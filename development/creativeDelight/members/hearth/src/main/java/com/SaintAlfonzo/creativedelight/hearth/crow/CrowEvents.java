package com.SaintAlfonzo.creativedelight.hearth.crow;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.nest.NestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

/** Event side of the crow systems: state, goals, alarm, hand feeding, the rules that keep stolen loot alive, and spawning. */
public final class CrowEvents {
    private static final ResourceLocation CAPABILITY_ID = new ResourceLocation(HearthAndHarvestMod.MODID, "crow_wariness");

    private CrowEvents() {
    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(CrowWariness.class);
    }

    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (CrowAccess.isCrow(event.getObject())) {
            event.addCapability(CAPABILITY_ID, new CrowWariness.Provider());
        }
    }

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && CrowAccess.isCrow(event.getEntity()) && event.getEntity() instanceof TamableAnimal crow) {
            crow.goalSelector.addGoal(1, new CrowFleeGoal(crow));
            crow.goalSelector.addGoal(1, new CrowAvoidRepellingBlocksGoal(crow));
            crow.goalSelector.addGoal(3, new CrowSeekShinyGoal(crow));
            crow.goalSelector.addGoal(3, new CrowEatDroppedFoodGoal(crow));
            crow.goalSelector.addGoal(3, new CrowWaryTemptGoal(crow));
            crow.goalSelector.addGoal(5, new CrowEatCropsGoal(crow));
        }
    }

    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof TamableAnimal crow) || !CrowAccess.isCrow(crow) || crow.level.isClientSide) {
            return;
        }
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null) {
            return;
        }
        wariness.tick();
        if (!wariness.isCarrying()) {
            return;
        }
        if (!wariness.getHeldBack().isEmpty()) {
            crow.setItemSlot(EquipmentSlot.MAINHAND, wariness.getHeldBack());
            wariness.setHeldBack(ItemStack.EMPTY);
        } else if (crow.getMainHandItem().isEmpty()) {
            wariness.stopCarrying();
        } else if (wariness.tickCarry()) {
            CrowLogic.dropLoot(crow, wariness);
        }
    }

    /**
     * Sets off the flock, and deals with the loot before AM does: AM drops a held item as a plain entity after any hit, so
     * the hand is emptied here first, either into a protected drop or, with {@code crow.dropOnHurt} off, into a one-tick hold.
     */
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof TamableAnimal crow) || !CrowAccess.isCrow(crow) || crow.level.isClientSide) {
            return;
        }
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null) {
            return;
        }
        if (CrowConfig.FLOCK_ALARM.get() && !crow.isTame() && event.getSource().getEntity() instanceof LivingEntity attacker && attacker != crow) {
            raiseAlarm(crow, wariness, attacker);
        }
        if (wariness.isCarrying() && !crow.getMainHandItem().isEmpty()) {
            if (CrowConfig.DROP_ON_HURT.get()) {
                CrowLogic.dropLoot(crow, wariness);
            } else {
                wariness.setHeldBack(crow.getMainHandItem().copy());
                crow.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
        }
    }

    private static void raiseAlarm(TamableAnimal crow, CrowWariness wariness, LivingEntity attacker) {
        wariness.alarm(attacker);
        if (!crow.isSilent()) {
            crow.playSound(CrowAccess.squawk(), 2.0F, 1.25F);
        }
        for (TamableAnimal other : crow.level.getEntitiesOfClass(TamableAnimal.class, crow.getBoundingBox().inflate(CrowLogic.ALARM_RANGE),
                entity -> entity != crow && CrowAccess.isCrow(entity) && !entity.isTame())) {
            CrowWariness state = CrowWariness.of(other);
            if (state != null) {
                state.alarm(attacker);
                if (!other.isSilent() && other.getRandom().nextInt(3) == 0) {
                    other.playSound(CrowAccess.squawk(), 1.5F, 1.15F + (other.getRandom().nextFloat() - other.getRandom().nextFloat()) * 0.2F);
                }
            }
        }
    }

    /** Carried or held-back loot leaves as a protected item. Runs before the equipment drop, so the guaranteed drop finds nothing left. */
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof TamableAnimal crow && CrowAccess.isCrow(crow) && !crow.level.isClientSide) {
            CrowWariness wariness = CrowWariness.of(crow);
            if (wariness != null && wariness.isCarrying()) {
                CrowLogic.dropLoot(crow, wariness);
            }
        }
    }

    public static void onAllowDespawn(LivingSpawnEvent.AllowDespawn event) {
        if (event.getEntity() instanceof TamableAnimal crow && CrowAccess.isCrow(crow)) {
            CrowWariness wariness = CrowWariness.of(crow);
            if (wariness != null && wariness.isCarrying()) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof TamableAnimal crow) || !CrowAccess.isCrow(crow)) {
            return;
        }
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack stack = event.getItemStack();
        if (crow.isTame()) {
            if (CrowConfig.CORN_BREEDING.get() && (stack.is(CrowTags.CORN) || stack.is(CrowTags.CORN_SEEDS)) && crow.isOwnedBy(player) && crow.getAge() == 0 && crow.canFallInLove()) {
                if (!crow.level.isClientSide) {
                    consume(player, stack);
                    crow.setInLove(player);
                }
                succeed(event, crow);
            }
            return;
        }
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null) {
            return;
        }
        if (wariness.isCarrying() && !crow.getMainHandItem().isEmpty()) {
            if (!crow.level.isClientSide) {
                CrowLogic.dropLoot(crow, wariness);
            }
            succeed(event, crow);
        } else if (CrowConfig.HAND_FEEDING.get() && stack.is(CrowTags.CROW_FOOD)) {
            if (!crow.level.isClientSide) {
                consume(player, stack);
                if (!crow.isSilent()) {
                    crow.playSound(SoundEvents.PARROT_EAT, 1.0F, 1.0F + (crow.getRandom().nextFloat() - crow.getRandom().nextFloat()) * 0.2F);
                }
                CrowLogic.tryTame(crow, player, 5);
            }
            succeed(event, crow);
        }
    }

    private static void consume(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private static void succeed(PlayerInteractEvent.EntityInteract event, TamableAnimal crow) {
        event.setCancellationResult(InteractionResult.sidedSuccess(crow.level.isClientSide));
        event.setCanceled(true);
    }

    /** ALLOW near generated nests or enough crops (C9). Additive; with the deny switch off, every other spawn is left alone. */
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (!CrowAccess.isCrow(event.getEntity()) || event.isSpawner()) {
            return;
        }
        LevelAccessor level = event.getLevel();
        BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
        int radius = CrowConfig.SPAWN_RADIUS.get();
        if (radius > 0 && (CrowConfig.SPAWN_NEAR_NESTS.get() && nearGeneratedNest(level, pos, radius) || cropsNearby(level, pos, radius))) {
            if (event.getEntity().checkSpawnObstruction(level)) {
                event.setResult(Event.Result.ALLOW);
            }
        } else if (CrowConfig.DENY_SPAWNS_ELSEWHERE.get()) {
            event.setResult(Event.Result.DENY);
        }
    }

    private static boolean nearGeneratedNest(LevelAccessor level, BlockPos pos, int radius) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -4; dy <= 8; dy++) {
                    cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.getBlock() instanceof NestBlock && state.getValue(NestBlock.GENERATED)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean cropsNearby(LevelAccessor level, BlockPos pos, int radius) {
        int needed = CrowConfig.CROP_REQUIREMENT.get();
        if (needed <= 0) {
            return false;
        }
        int count = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (level.getBlockState(cursor).is(BlockTags.CROPS) && ++count >= needed) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
