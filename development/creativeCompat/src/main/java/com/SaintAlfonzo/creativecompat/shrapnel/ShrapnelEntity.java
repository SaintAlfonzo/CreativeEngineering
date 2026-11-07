package com.SaintAlfonzo.creativecompat.shrapnel;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * One flying piece of TNT shrapnel - a plain, no-gravity {@link ThrowableItemProjectile} that
 * deals a flat hit of damage to whatever it touches and despawns once it has travelled far
 * enough from where it was fired. Spawned in bursts by {@link ShrapnelSpawner}; never placed
 * directly.
 *
 * <p>No gravity is applied ({@link #setNoGravity} in {@link ShrapnelSpawner}) so the burst stays
 * roughly on the plane it was launched at instead of arcing to the ground like a thrown item
 * would. Distance-based despawning is tracked here rather than relying on a tick-count timeout,
 * since {@link CompatConfig.Shrapnel#speed} and {@link CompatConfig.Shrapnel#maxDistance} are both
 * independently configurable.
 */
public class ShrapnelEntity extends ThrowableItemProjectile {

    private static final String TAG_MAX_DISTANCE = "creativeengineering_max_distance";
    private static final String TAG_DAMAGE = "creativeengineering_damage";

    private Vec3 origin = Vec3.ZERO;
    private double maxDistanceSqr = Double.MAX_VALUE;
    private float damage = 3.0F;

    public ShrapnelEntity(EntityType<? extends ShrapnelEntity> type, Level level) {
        super(type, level);
    }

    public ShrapnelEntity(EntityType<? extends ShrapnelEntity> type, double x, double y, double z, Level level) {
        super(type, x, y, z, level);
    }

    public ShrapnelEntity(EntityType<? extends ShrapnelEntity> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    /** Called once by {@link ShrapnelSpawner} right after construction, before it's added to the level. */
    void init(Vec3 origin, double maxDistance, float damage) {
        this.origin = origin;
        this.maxDistanceSqr = maxDistance * maxDistance;
        this.damage = damage;
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(CompatConfig.SHRAPNEL.item.get()));
        return item == null ? Items.IRON_NUGGET : item;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide && this.position().distanceToSqr(this.origin) >= this.maxDistanceSqr) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level.isClientSide) return;

        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        DamageSource source = DamageSource.thrown(this, owner instanceof LivingEntity ? owner : null);
        target.hurt(source, this.damage);
        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level.isClientSide && result.getType() != HitResult.Type.ENTITY) {
            // Block hits (or a miss) just end the flight - shrapnel doesn't break blocks or stick.
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target != this.getOwner();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("OriginX", this.origin.x);
        tag.putDouble("OriginY", this.origin.y);
        tag.putDouble("OriginZ", this.origin.z);
        tag.putDouble(TAG_MAX_DISTANCE, Math.sqrt(this.maxDistanceSqr));
        tag.putFloat(TAG_DAMAGE, this.damage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.origin = new Vec3(tag.getDouble("OriginX"), tag.getDouble("OriginY"), tag.getDouble("OriginZ"));
        double maxDistance = tag.getDouble(TAG_MAX_DISTANCE);
        this.maxDistanceSqr = maxDistance <= 0 ? Double.MAX_VALUE : maxDistance * maxDistance;
        this.damage = tag.getFloat(TAG_DAMAGE);
        this.setNoGravity(true);
    }
}
