package com.SaintAlfonzo.creativedelight.hearth.crow;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Per-crow alarm, trust and theft state (plan_hearth_crow_salt.md 8.5, C1), kept on {@link CrowEntity} (a capability on Alex's
 * Mobs' crow before the swap, plan_hearth_crow_swap.md P3).
 * Trust changes only how close a wild crow lets a player come; it never gates taming.
 */
public class CrowWariness {
    public static final int ALARM_DURATION = 120;
    public static final int FULL_TRUST = 200;

    @Nullable
    private UUID alarmSource;
    private int alarmTicks;
    private boolean freshAlarm;
    private int trust;
    private boolean beingTempted;
    @Nullable
    private UUID snatchTarget;
    private boolean snatching;
    private boolean carrying;
    private int carryTicks;
    private ItemStack heldBack = ItemStack.EMPTY;

    public static CrowWariness of(CrowEntity crow) {
        return crow.getWariness();
    }

    public void tick() {
        if (alarmTicks > 0 && --alarmTicks == 0) {
            alarmSource = null;
        }
        if (!beingTempted && trust > 0) {
            trust--;
        }
    }

    public void alarm(LivingEntity source) {
        alarmSource = source.getUUID();
        alarmTicks = ALARM_DURATION;
        freshAlarm = true;
        trust = 0;
    }

    @Nullable
    public LivingEntity getAlarmSource(ServerLevel level) {
        if (alarmTicks <= 0 || alarmSource == null) {
            return null;
        }
        Entity entity = level.getEntity(alarmSource);
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    public boolean isAlarmed(ServerLevel level) {
        return getAlarmSource(level) != null;
    }

    public boolean consumeFreshAlarm() {
        boolean fresh = freshAlarm;
        freshAlarm = false;
        return fresh;
    }

    public void setBeingTempted(boolean tempted) {
        beingTempted = tempted;
    }

    public void addTrust(int amount) {
        trust = Mth.clamp(trust + amount, 0, FULL_TRUST);
    }

    public double comfortDistance() {
        return Mth.lerp(progress(), 4.0D, 1.5D);
    }

    public double waryDistance() {
        return Mth.lerp(progress(), 2.5D, 1.0D);
    }

    private double progress() {
        return trust / (double) FULL_TRUST;
    }

    public void setSnatching(boolean value, @Nullable UUID target) {
        snatching = value;
        snatchTarget = target;
    }

    public boolean isSnatching() {
        return snatching;
    }

    public boolean isSnatchTarget(Entity entity) {
        return snatching && entity.getUUID().equals(snatchTarget);
    }

    public void startCarrying(int ticks) {
        carrying = true;
        carryTicks = ticks;
    }

    public void stopCarrying() {
        carrying = false;
        carryTicks = 0;
        heldBack = ItemStack.EMPTY;
    }

    public boolean isCarrying() {
        return carrying;
    }

    /** One tick of the carry timer; true when it has run out. */
    public boolean tickCarry() {
        return --carryTicks <= 0;
    }

    public ItemStack getHeldBack() {
        return heldBack;
    }

    /** Loot kept out of the hand for a tick while {@code crow.dropOnHurt} is off. */
    public void setHeldBack(ItemStack stack) {
        heldBack = stack;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Trust", trust);
        tag.putInt("AlarmTicks", alarmTicks);
        if (alarmSource != null) {
            tag.putUUID("AlarmSource", alarmSource);
        }
        tag.putBoolean("Carrying", carrying);
        tag.putInt("CarryTicks", carryTicks);
        if (!heldBack.isEmpty()) {
            tag.put("HeldBack", heldBack.save(new CompoundTag()));
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        trust = Mth.clamp(tag.getInt("Trust"), 0, FULL_TRUST);
        alarmTicks = tag.getInt("AlarmTicks");
        alarmSource = tag.hasUUID("AlarmSource") ? tag.getUUID("AlarmSource") : null;
        carrying = tag.getBoolean("Carrying");
        carryTicks = tag.getInt("CarryTicks");
        heldBack = tag.contains("HeldBack") ? ItemStack.of(tag.getCompound("HeldBack")) : ItemStack.EMPTY;
    }
}
