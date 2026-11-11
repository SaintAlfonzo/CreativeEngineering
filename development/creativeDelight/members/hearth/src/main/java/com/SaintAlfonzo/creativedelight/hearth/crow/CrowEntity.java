package com.SaintAlfonzo.creativedelight.hearth.crow;

import com.SaintAlfonzo.creativedelight.hearth.HHSounds;
import com.SaintAlfonzo.creativedelight.hearth.entity.HHEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

/**
 * Hearth and Harvest 1.4.1's crow ({@code hearthandharvest:crow}), ported to 1.19.2 (plan_hearth_crow_swap.md 6.1). The wild
 * behaviour is the member's own goal set (flee, scarecrows, theft, dropped food, tempting, crops); wild hand feeding, corn
 * breeding, the flock alarm and loot handling stay in {@link CrowEvents}. Wariness was a capability on Alex's Mobs' crow and is a
 * field here, saved under "Wariness" with the old key names.
 */
public class CrowEntity extends ShoulderRidingEntity implements FlyingAnimal {
    private static final EntityDataAccessor<Boolean> DATA_GLIDING = SynchedEntityData.defineId(CrowEntity.class, EntityDataSerializers.BOOLEAN);

    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    private float flapping = 1.0F;
    private float nextFlap = 1.0F;
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState flyingAnimationState = new AnimationState();
    public final AnimationState sittingAnimationState = new AnimationState();
    public final AnimationState glidingAnimationState = new AnimationState();
    private final CrowFlightPose flightPose = new CrowFlightPose();
    private final CrowWariness wariness = new CrowWariness();

    public CrowEntity(EntityType<? extends ShoulderRidingEntity> type, Level level) {
        super(type, level);
        this.moveControl = new CrowMoveControl(this);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.FLYING_SPEED, 0.8F)
                .add(Attributes.MOVEMENT_SPEED, 0.4F)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    public CrowWariness getWariness() {
        return wariness;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new PanicGoal(this, 1.25D));
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new CrowFleeGoal(this));
        goalSelector.addGoal(1, new CrowAvoidRepellingBlocksGoal(this));
        goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(3, new CrowSeekShinyGoal(this));
        goalSelector.addGoal(3, new CrowRetrieveItemsGoal(this, 1.2D));
        goalSelector.addGoal(3, new CrowEatDroppedFoodGoal(this));
        goalSelector.addGoal(4, new CrowWaryTemptGoal(this));
        goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.0D, 5.0F, 1.0F, true));
        goalSelector.addGoal(5, new CrowEatCropsGoal(this));
        goalSelector.addGoal(5, new LandOnOwnersShoulderGoal(this));
        goalSelector.addGoal(5, new CrowWanderGoal(this, 1.0D));
        goalSelector.addGoal(6, new FollowMobGoal(this, 1.0D, 3.0F, 7.0F));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_GLIDING, false);
    }

    public boolean isGliding() {
        return entityData.get(DATA_GLIDING);
    }

    public void setGliding(boolean gliding) {
        entityData.set(DATA_GLIDING, gliding);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public boolean isFlying() {
        return !isOnGround() && !isInSittingPose();
    }

    public CrowFlightPose getFlightPose() {
        return flightPose;
    }

    public boolean isVisuallyFlying() {
        return flightPose.isVisuallyFlying(this);
    }

    public float getFlapAnimationSpeed() {
        return flightPose.flapSpeed();
    }

    /** Sitting or riding: no wandering, fetching or fleeing. */
    public boolean isBusy() {
        return isOrderedToSit() || isPassenger();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level.isClientSide) {
            flightPose.tick(this);
        } else {
            // wariness ticks in CrowEvents.onLivingTick, with the carry timer
            if (isTame() && isOrderedToSit() && isOnGround() && !isInSittingPose()) {
                setInSittingPose(true);
            }
        }
        calculateFlapping();
    }

    private void calculateFlapping() {
        if (level.isClientSide) {
            if (isInSittingPose()) {
                startAnimation(sittingAnimationState);
            } else if (isVisuallyFlying()) {
                startAnimation(isGliding() ? glidingAnimationState : flyingAnimationState);
            } else {
                startAnimation(idleAnimationState);
            }
        }
        oFlap = flap;
        oFlapSpeed = flapSpeed;
        flapSpeed = Mth.clamp(flapSpeed + (!isOnGround() && !isPassenger() ? 4 : -1) * 0.3F, 0.0F, 1.0F);
        if (!isOnGround() && flapping < 1.0F) {
            flapping = 1.0F;
        }
        flapping *= 0.9F;
        flap += flapping * 2.0F;
    }

    private void startAnimation(AnimationState active) {
        if (active.isStarted()) {
            return;
        }
        for (AnimationState state : new AnimationState[]{idleAnimationState, flyingAnimationState, sittingAnimationState, glidingAnimationState}) {
            if (state != active) {
                state.stop();
            }
        }
        active.start(tickCount);
    }

    /** Wild hand feeding is {@link CrowEvents#onEntityInteract}; this keeps upstream's sit toggle and cookie poisoning. */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.COOKIE)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            addEffect(new MobEffectInstance(MobEffects.POISON, 900));
            if (player.isCreative() || !isInvulnerable()) {
                hurt(DamageSource.playerAttack(player), Float.MAX_VALUE);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!isTame() || !isOwnedBy(player) || isFood(stack)) {
            return super.mobInteract(player, hand);
        }
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            boolean sit = !isOrderedToSit();
            setOrderedToSit(sit);
            setInSittingPose(sit && isOnGround());
            getNavigation().stop();
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean canSitOnShoulder() {
        return super.canSitOnShoulder() && getMainHandItem().isEmpty();
    }

    public void holdItem(ItemStack stack) {
        setItemSlot(EquipmentSlot.MAINHAND, stack);
        if (!stack.isEmpty()) {
            setGuaranteedDrop(EquipmentSlot.MAINHAND);
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(CrowTags.CROW_FOOD);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return HHEntities.CROW.get().create(level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return HHSounds.CROW_SQUAWK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return HHSounds.CROW_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return HHSounds.CROW_HURT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(HHSounds.CROW_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    protected boolean isFlapping() {
        return flyDist > nextFlap;
    }

    @Override
    protected void onFlap() {
        nextFlap = flyDist + flapSpeed / 2.0F;
    }

    @Override
    public float getVoicePitch() {
        return getPitch(random);
    }

    public static float getPitch(RandomSource random) {
        return (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void doPush(Entity entity) {
        if (!(entity instanceof Player)) {
            super.doPush(entity);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source == DamageSource.SWEET_BERRY_BUSH || isInvulnerableTo(source)) {
            return false;
        }
        if (!level.isClientSide) {
            setOrderedToSit(false);
        }
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Wariness", wariness.save());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        wariness.load(tag.getCompound("Wariness"));
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0D, 0.5F * getEyeHeight(), getBbWidth() * 0.4F);
    }
}
