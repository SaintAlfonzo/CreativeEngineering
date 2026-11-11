package com.SaintAlfonzo.creativedelight.hearth.crow;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class CrowMoveControl extends MoveControl {
    private static final double CRUISE_SCALE = 0.36;
    private static final double WALK_SCALE = 0.5;
    private static final double WALK_RANGE_SQR = 16.0;
    private static final double MIN_ARRIVE_SPEED = 0.05;
    private static final double HORIZONTAL_ACCEL = 0.2;
    private static final double VERTICAL_ACCEL = 0.22;
    private static final double MAX_CLIMB = 0.22;
    private static final double MAX_DIVE = 0.4;
    private static final double GLIDE_SINK = 0.1;
    private static final double GLIDE_MIN_SPEED = 0.12;
    private static final double LANDING_SINK = 0.15;
    private static final float TURN_FAST = 12.0F;
    private static final float TURN_SLOW = 45.0F;
    private static final float TURN_WALK = 30.0F;
    private static final double AIR_DRAG = 0.91;
    private static final double VERTICAL_DRAG = 0.98;
    private static final int MIN_GLIDE_TICKS = 12;
    private static final int MIN_FLAP_TICKS = 10;
    private final CrowEntity crow;
    private int wingLockTicks;
    private int cruiseTicks;
    private boolean cruiseGliding;
    private int takeoffSoundCooldown;

    public CrowMoveControl(CrowEntity crow) {
        super(crow);
        this.crow = crow;
    }

    public void tick() {
        if (this.takeoffSoundCooldown > 0) {
            this.takeoffSoundCooldown--;
        }

        if (this.wingLockTicks > 0) {
            this.wingLockTicks--;
        }

        if (this.crow.isPassenger()) {
            this.crow.setNoGravity(false);
            this.setWingState(false, true);
        } else if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            double dx = this.wantedX - this.crow.getX();
            double dy = this.wantedY - this.crow.getY();
            double dz = this.wantedZ - this.crow.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            if (horizontal * horizontal + dy * dy < 2.5E-7) {
                this.crow.setZza(0.0F);
                this.crow.setYya(0.0F);
            } else {
                if (this.shouldWalk(dy)) {
                    this.walk(dx, dy, dz);
                } else {
                    this.fly(dx, dy, dz, horizontal);
                }
            }
        } else if (this.operation == Operation.WAIT) {
            this.crow.setZza(0.0F);
            this.crow.setYya(0.0F);
            this.crow.setXxa(0.0F);
            this.drift();
        } else {
            super.tick();
        }
    }

    private boolean shouldWalk(double nodeDy) {
        if (!this.crow.isOnGround() || this.speedModifier >= 1.5) {
            return false;
        } else if (!(nodeDy > 1.2) && !(nodeDy < -1.5)) {
            BlockPos destination = this.crow.getNavigation().getTargetPos();
            if (destination == null) {
                return false;
            } else {
                double tx = (double)destination.getX() + 0.5 - this.crow.getX();
                double ty = (double)destination.getY() - this.crow.getY();
                double tz = (double)destination.getZ() + 0.5 - this.crow.getZ();
                return tx * tx + tz * tz < 16.0 && ty < 1.5 && ty > -2.0;
            }
        } else {
            return false;
        }
    }

    private void walk(double dx, double dy, double dz) {
        this.crow.setNoGravity(false);
        this.setWingState(false, true);
        float targetYaw = (float)(Mth.atan2(dz, dx) * 180.0F / (float)Math.PI) - 90.0F;
        this.crow.setYRot(this.rotlerp(this.crow.getYRot(), targetYaw, 30.0F));
        this.crow.setSpeed((float)(this.speedModifier * this.crow.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.5));
        this.crow.setYya(0.0F);
        if (dy > (double)this.crow.getStepHeight() + 0.01) {
            this.crow.getJumpControl().jump();
        }
    }

    private void fly(double dx, double dy, double dz, double horizontal) {
        this.crow.setNoGravity(true);
        this.crow.setZza(0.0F);
        this.crow.setYya(0.0F);
        this.crow.setXxa(0.0F);
        Vec3 current = this.crow.getDeltaMovement();
        double currentHorizontal = current.horizontalDistance();
        if (horizontal > 1.0E-4) {
            float targetYaw = (float)(Mth.atan2(dz, dx) * 180.0F / (float)Math.PI) - 90.0F;
            float maxTurn = (float)Mth.clampedLerp(45.0, 12.0, currentHorizontal / 0.3);
            this.crow.setYRot(this.rotlerp(this.crow.getYRot(), targetYaw, maxTurn));
        }

        float yawRad = this.crow.getYRot() * (float) (Math.PI / 180.0);
        double facingX = (double)(-Mth.sin(yawRad));
        double facingZ = (double)Mth.cos(yawRad);
        double dirX = facingX;
        double dirZ = facingZ;
        double alignmentFactor = 0.0;
        if (horizontal > 1.0E-4) {
            double precision = Mth.clamp(1.0 - (horizontal - 0.5) / 1.5, 0.0, 1.0);
            dirX = Mth.lerp(precision, facingX, dx / horizontal);
            dirZ = Mth.lerp(precision, facingZ, dz / horizontal);
            double alignment = (facingX * dx + facingZ * dz) / horizontal;
            alignmentFactor = Mth.clamp((alignment + 0.3) / 1.3, 0.15, 1.0);
        }

        double cruise = this.crow.getAttributeValue(Attributes.FLYING_SPEED) * this.speedModifier * 0.36;
        double targetHorizontal = Math.min(cruise, Math.max(0.05, horizontal * 0.25)) * alignmentFactor;
        double targetVertical = Mth.clamp(dy * 0.3, -0.4, 0.22);
        if (targetVertical > 0.0) {
            targetHorizontal *= 1.0 - targetVertical * 1.4;
        } else {
            targetHorizontal *= 1.0 - targetVertical * 0.6;
        }

        boolean takingOff = this.crow.isOnGround();
        if (takingOff) {
            targetVertical = Math.max(targetVertical, dy > 0.0 ? 0.32 : 0.18);
            this.playTakeoffSound();
        }

        double newX = current.x + (dirX * targetHorizontal - current.x) * 0.2;
        double newZ = current.z + (dirZ * targetHorizontal - current.z) * 0.2;
        double newY = takingOff ? targetVertical : current.y + (targetVertical - current.y) * 0.22;
        this.applyVelocity(newX, newY, newZ);
        if (takingOff) {
            this.setWingState(false, true);
        } else if (newY > 0.05 || targetHorizontal > currentHorizontal + 0.05 || currentHorizontal < 0.08) {
            this.setWingState(false, false);
        } else if (targetVertical < -0.05) {
            this.setWingState(true, false);
        } else {
            this.setWingState(this.nextCruisePhase(), false);
        }
    }

    private void drift() {
        if (!this.crow.isOnGround() && !this.crow.isInWater() && !this.crow.isInLava()) {
            this.crow.setNoGravity(true);
            Vec3 current = this.crow.getDeltaMovement();
            if (this.isNearGround()) {
                this.applyVelocity(current.x * 0.6, current.y + (-0.15 - current.y) * 0.3, current.z * 0.6);
                this.setWingState(false, false);
            } else {
                float yawRad = this.crow.getYRot() * (float) (Math.PI / 180.0);
                double speed = Math.max(0.12, current.horizontalDistance() * 0.97);
                double newX = current.x + ((double)(-Mth.sin(yawRad)) * speed - current.x) * 0.1;
                double newZ = current.z + ((double)Mth.cos(yawRad) * speed - current.z) * 0.1;
                double newY = current.y + (-0.1 - current.y) * 0.15;
                this.applyVelocity(newX, newY, newZ);
                this.setWingState(true, false);
            }
        } else {
            this.crow.setNoGravity(false);
            this.setWingState(false, true);
        }
    }

    private boolean nextCruisePhase() {
        if (--this.cruiseTicks <= 0) {
            this.cruiseGliding = !this.cruiseGliding;
            this.cruiseTicks = this.cruiseGliding ? 15 + this.crow.getRandom().nextInt(25) : 25 + this.crow.getRandom().nextInt(30);
        }

        return this.cruiseGliding;
    }

    private void setWingState(boolean gliding, boolean force) {
        if (this.crow.isGliding() != gliding) {
            if (force || this.wingLockTicks <= 0) {
                this.crow.setGliding(gliding);
                this.wingLockTicks = gliding ? 12 : 10;
            }
        }
    }

    private boolean isNearGround() {
        return !this.crow.level.noCollision(this.crow, this.crow.getBoundingBox().move(0.0, -0.75, 0.0));
    }

    private void applyVelocity(double x, double y, double z) {
        this.crow.setDeltaMovement(x / 0.91, y / 0.98, z / 0.91);
    }

    private void playTakeoffSound() {
        if (this.takeoffSoundCooldown <= 0 && !this.crow.isSilent()) {
            this.takeoffSoundCooldown = 20;
            this.crow.playSound(SoundEvents.PARROT_FLY, 0.5F, 0.7F + this.crow.getRandom().nextFloat() * 0.2F);
        }
    }
}
