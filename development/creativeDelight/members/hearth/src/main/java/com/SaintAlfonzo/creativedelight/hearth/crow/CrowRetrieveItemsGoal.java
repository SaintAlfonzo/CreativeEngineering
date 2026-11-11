package com.SaintAlfonzo.creativedelight.hearth.crow;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** H&H 1.4.1: a tame crow fetches loose items near its owner and hands them over, perching on a free shoulder. */
public class CrowRetrieveItemsGoal extends CrowItemGoal {
    private static final int SCAN_INTERVAL = 10;
    private static final int REPATH_INTERVAL = 5;
    private static final double SCAN_RADIUS = 8.0;
    private static final double PICKUP_RANGE_SQR = 2.25;
    private static final double OWNER_PICKUP_RANGE_SQR = 9.0;
    private static final double MAX_OWNER_DISTANCE_SQR = 1024.0;
    private static final double HANDOFF_RANGE_SQR = 4.0;
    private static final double PERCH_RANGE_SQR = 1.44;
    private static final int PERCH_PATIENCE = 100;
    private static final String DELIVERED_TAG = "hearthandharvest:crow_delivered";
    private int waitingTicks;

    public CrowRetrieveItemsGoal(CrowEntity crow, double speed) {
        super(crow, speed, 10, 8.0, 5, EnumSet.of(Flag.MOVE));
    }

    @Nullable
    private Player getOwnerPlayer() {
        if (this.crow.getOwner() instanceof Player owner) {
            if (!owner.isAlive() || owner.isSpectator() || owner.level != this.crow.level) {
                return null;
            } else {
                return this.crow.distanceToSqr(owner) > 1024.0 ? null : owner;
            }
        } else {
            return null;
        }
    }

    private boolean isAvailable() {
        return this.crow.isTame() && !this.crow.isBusy();
    }

    public boolean canUse() {
        if (!this.isAvailable() || this.getOwnerPlayer() == null) {
            return false;
        } else if (!this.crow.getMainHandItem().isEmpty()) {
            return true;
        } else if (!CrowConfig.FETCH_ITEMS.get()) {
            return false;
        } else if (!this.scanReady()) {
            return false;
        } else {
            this.targetItem = this.findNearestTarget();
            return this.targetItem != null;
        }
    }

    public boolean canContinueToUse() {
        return this.isAvailable() && this.getOwnerPlayer() != null
            ? !this.crow.getMainHandItem().isEmpty() || this.targetItem != null && this.targetItem.isAlive() && !this.targetItem.getItem().isEmpty()
            : false;
    }

    public void start() {
        this.waitingTicks = 0;
        this.resetRepath();
    }

    public void tick() {
        Player owner = this.getOwnerPlayer();
        if (owner != null) {
            if (this.crow.getMainHandItem().isEmpty()) {
                this.collectTarget();
            } else {
                this.deliverToOwner(owner);
            }
        }
    }

    private void collectTarget() {
        if (this.targetItem != null && this.targetItem.isAlive()) {
            if (this.approach(this.targetItem, 2.25)) {
                ItemStack stack = this.targetItem.getItem().copy();
                this.crow.take(this.targetItem, stack.getCount());
                this.targetItem.discard();
                this.targetItem = null;
                this.crow.holdItem(stack);
                this.crow.playSound(SoundEvents.ITEM_PICKUP, 0.4F, 1.0F);
            }
        }
    }

    private void deliverToOwner(Player owner) {
        boolean var10000;
        label37: {
            if (owner instanceof ServerPlayer serverOwner && hasFreePerch(serverOwner)) {
                var10000 = true;
                break label37;
            }

            var10000 = false;
        }

        boolean canPerch = var10000;
        double distanceSqr = this.crow.distanceToSqr(owner);
        double arriveRangeSqr = canPerch ? 1.44 : 4.0;
        if (distanceSqr > arriveRangeSqr) {
            if (distanceSqr <= 4.0 && ++this.waitingTicks > 100) {
                this.handOff(owner, false);
            } else {
                this.repath(owner, this.speed);
            }
        } else if (canPerch && !owner.isOnGround()) {
            if (++this.waitingTicks > 100) {
                this.handOff(owner, false);
            }
        } else {
            this.handOff(owner, canPerch);
        }
    }

    private void handOff(Player owner, boolean perch) {
        this.waitingTicks = 0;
        ItemStack delivery = this.crow.getMainHandItem().copy();
        this.crow.holdItem(ItemStack.EMPTY);
        this.crow.getNavigation().stop();
        if (perch && owner instanceof ServerPlayer serverOwner) {
            this.crow.setEntityOnShoulder(serverOwner);
        }

        owner.getInventory().add(delivery);
        if (!delivery.isEmpty()) {
            ItemEntity overflow = owner.drop(delivery, false, false);
            if (overflow != null) {
                overflow.getPersistentData().putBoolean("hearthandharvest:crow_delivered", true);
            }
        }

        owner.level.playSound(null, owner, SoundEvents.ITEM_PICKUP, owner.getSoundSource(), 0.3F, 1.4F);
    }

    private static boolean hasFreePerch(ServerPlayer owner) {
        return !owner.getAbilities().flying
            && !owner.isInWater()
            && !owner.isInPowderSnow
            && !owner.isPassenger()
            && (owner.getShoulderEntityLeft().isEmpty() || owner.getShoulderEntityRight().isEmpty());
    }

    @Override
    protected boolean isValidTarget(@Nullable ItemEntity item) {
        Player owner = this.getOwnerPlayer();
        return owner != null && isValidTarget(item, owner);
    }

    private static boolean isValidTarget(@Nullable ItemEntity item, Player owner) {
        return item != null
            && item.isAlive()
            && !item.getItem().isEmpty()
            && !item.hasPickUpDelay()
            && !item.getPersistentData().getBoolean("hearthandharvest:crow_delivered")
            && !item.getPersistentData().getBoolean(CrowLogic.CROW_DROPPED_TAG)
            && (CrowConfig.FETCH_THROWN_ITEMS.get() || !owner.getUUID().equals(item.getThrower()))
            && item.distanceToSqr(owner) > 9.0;
    }

    public static boolean isWorthFetching(ItemEntity item, Player owner) {
        return isValidTarget(item, owner) && !item.isInWater() && !item.isInLava() && owner.hasLineOfSight(item);
    }
}
