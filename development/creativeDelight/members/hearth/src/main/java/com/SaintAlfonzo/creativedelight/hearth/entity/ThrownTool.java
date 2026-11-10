package com.SaintAlfonzo.creativedelight.hearth.entity;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Collection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Shared body of the thrown cleaver and pitchfork, on the trident's rules. The stack is carried whole, so Loyalty and damage survive. */
public abstract class ThrownTool extends AbstractArrow {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ThrownTool.class, EntityDataSerializers.ITEM_STACK);
    private final IntOpenHashSet hit = new IntOpenHashSet();
    private boolean dealtDamage;

    protected ThrownTool(EntityType<? extends ThrownTool> type, Level level) {
        super(type, level);
    }

    protected ThrownTool(EntityType<? extends ThrownTool> type, Level level, LivingEntity shooter, ItemStack stack, double damageDivisor) {
        super(type, shooter, level);
        ItemStack one = stack.copy();
        one.setCount(1);
        entityData.set(DATA_ITEM, one);
        setBaseDamage(meleeDamage(stack) / damageDivisor);
        pickup = shooter instanceof Player player && player.getAbilities().instabuild ? Pickup.CREATIVE_ONLY : Pickup.ALLOWED;
    }

    private static double meleeDamage(ItemStack stack) {
        double damage = 2.0D;
        Collection<AttributeModifier> modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE);
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) damage += modifier.getAmount();
        }
        return damage;
    }

    /** How many entities one throw can hit. */
    protected abstract int maxTargets();

    protected void onHurtTarget(LivingEntity target) {
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_ITEM, ItemStack.EMPTY);
    }

    public ItemStack getTool() {
        return entityData.get(DATA_ITEM);
    }

    public boolean isStuck() {
        return inGround;
    }

    @Override
    protected ItemStack getPickupItem() {
        return getTool().copy();
    }

    @Override
    public byte getPierceLevel() {
        return (byte) (maxTargets() - 1);
    }

    @Override
    public void tick() {
        int loyalty = EnchantmentHelper.getLoyalty(getTool());
        if (loyalty > 0 && dealtDamage && !level.isClientSide && getOwner() instanceof Player player && player.isAlive()) {
            inGround = false;
            setNoPhysics(true);
            Vec3 toPlayer = player.getEyePosition().subtract(position());
            setDeltaMovement(getDeltaMovement().add(toPlayer.normalize().scale(0.05D * loyalty)).scale(0.92D));
        }
        super.tick();
    }

    /** Without this a piercing throw would find the same entity forever: AbstractArrow's own ignore list is only filled by its onHitEntity. */
    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !hit.contains(entity.getId());
    }

    @Override
    protected EntityHitResult findHitEntity(Vec3 from, Vec3 to) {
        return isNoPhysics() || hit.size() >= maxTargets() ? null : super.findHitEntity(from, to);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        dealtDamage = true;
    }

    /** DamageSource.thrown keeps this entity as the direct source, which is how a kill is told to be the thrown cleaver's. */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        hit.add(entity.getId());
        Entity owner = getOwner();
        DamageSource source = DamageSource.thrown(this, owner == null ? this : owner);
        float damage = (float) (getDeltaMovement().length() * getBaseDamage());
        if (entity instanceof LivingEntity living) damage += EnchantmentHelper.getDamageBonus(getTool(), living.getMobType());
        dealtDamage = true;
        if (entity.hurt(source, damage) && entity instanceof LivingEntity living) {
            if (owner instanceof LivingEntity attacker) {
                EnchantmentHelper.doPostHurtEffects(living, attacker);
                EnchantmentHelper.doPostDamageEffects(attacker, living);
            }
            doPostHurtEffects(living);
            onHurtTarget(living);
        }
        playSound(getDefaultHitGroundSoundEvent(), 1.0F, 1.0F);
        if (hit.size() >= maxTargets()) setDeltaMovement(getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
    }

    @Override
    protected void tickDespawn() {
        if (pickup != Pickup.ALLOWED || EnchantmentHelper.getLoyalty(getTool()) <= 0) super.tickDespawn();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Tool", getTool().save(new CompoundTag()));
        tag.putBoolean("DealtDamage", dealtDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_ITEM, ItemStack.of(tag.getCompound("Tool")));
        dealtDamage = tag.getBoolean("DealtDamage");
    }
}
