/*
 * Canned Goods, by BlueIvey. Licensed under the Academic Free License version 3.0
 * (NOTICE-upstream-AFL.txt in this jar).
 *
 * MODIFIED FILE (Attribution Notice, AFL 3.0 section 6): SaintAlfonzo modified this class of Canned Goods 1.19.2-3
 * on 2026-11-02 for the Creative Delight repack. Decompiled from the release jar and mapped to Mojang names; added
 * the dye colour (synced data, NBT "Color", right-click with a dye), marked "CE:" below and implemented in
 * com.SaintAlfonzo.creativedelight.cannedgoods.CannedGolemDye. Everything else is the upstream code unchanged.
 */
package net.mcreator.cannedgoods.entity;

import com.SaintAlfonzo.creativedelight.cannedgoods.CannedGolemDye;
import net.mcreator.cannedgoods.init.CannedGoodsModEntities;
import net.mcreator.cannedgoods.procedures.CannedGolemEntityDiesProcedure;
import net.mcreator.cannedgoods.procedures.CannedGolemEntityIsHurtProcedure;
import net.mcreator.cannedgoods.procedures.CannedGolemItIsStruckByLightningProcedure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;

public class CannedGolemEntity extends PathfinderMob {
    // CE: the dye colour (DyeColor id), synced to clients for the renderer
    private static final EntityDataAccessor<Byte> DATA_COLOR =
            SynchedEntityData.defineId(CannedGolemEntity.class, EntityDataSerializers.BYTE);

    public CannedGolemEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(CannedGoodsModEntities.CANNED_GOLEM.get(), world);
    }

    public CannedGolemEntity(EntityType<CannedGolemEntity> type, Level world) {
        super(type, world);
        this.xpReward = 0;
        this.setNoAi(false);
        this.setPersistenceRequired();
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.5F));
        this.goalSelector.addGoal(5, new RemoveBlockGoal(Blocks.BROWN_MUSHROOM, this, 1.0, 3));
        this.goalSelector.addGoal(6, new RemoveBlockGoal(Blocks.RED_MUSHROOM, this, 1.0, 3));
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("canned_goods:golem_hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.creeper.death"));
    }

    @Override
    public void thunderHit(ServerLevel serverWorld, LightningBolt lightningBolt) {
        super.thunderHit(serverWorld, lightningBolt);
        CannedGolemItIsStruckByLightningProcedure.execute(this.level, this.getX(), this.getY(), this.getZ());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        CannedGolemEntityIsHurtProcedure.execute(this.level, this.getX(), this.getY(), this.getZ());
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        CannedGolemEntityDiesProcedure.execute(this.level, this.getX(), this.getY(), this.getZ());
    }

    // CE: begin dye colour
    public DyeColor getColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLOR));
    }

    public void setColor(DyeColor color) {
        this.entityData.set(DATA_COLOR, (byte) color.getId());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_COLOR, (byte) CannedGolemDye.DEFAULT_COLOR.getId());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        CannedGolemDye.save(this, tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CannedGolemDye.load(this, tag);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult dyed = CannedGolemDye.interact(this, player, hand);
        return dyed != null ? dyed : super.mobInteract(player, hand);
    }
    // CE: end dye colour

    public static void init() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.2);
        builder = builder.add(Attributes.MAX_HEALTH, 3.0);
        builder = builder.add(Attributes.ARMOR, 0.0);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 3.0);
        return builder.add(Attributes.FOLLOW_RANGE, 5.0);
    }
}
