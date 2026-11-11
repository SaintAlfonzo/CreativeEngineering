package com.SaintAlfonzo.creativedelight.hearth.crow;

import net.minecraft.util.Mth;

public class CrowFlightPose {
    private static final float SMOOTHING = 0.2F;
    private static final float FLAP_SMOOTHING = 0.1F;
    private static final float CLIMB_FLAP_SPEED = 1.35F;
    private static final int AIRBORNE_GRACE = 3;
    private float pitch;
    private float pitchO;
    private float roll;
    private float rollO;
    private float flapSpeed = 1.0F;
    private int airborneTicks;

    public void tick(CrowEntity crow) {
        this.airborneTicks = !crow.isOnGround() && !crow.isPassenger() ? this.airborneTicks + 1 : 0;
        this.pitchO = this.pitch;
        this.rollO = this.roll;
        float targetPitch = 0.0F;
        float targetRoll = 0.0F;
        float targetFlapSpeed = 1.0F;
        if (this.isVisuallyFlying(crow) && !crow.isInWater()) {
            double vy = crow.getY() - crow.yo;
            double vh = Math.sqrt(Mth.square(crow.getX() - crow.xo) + Mth.square(crow.getZ() - crow.zo));
            if (vh + Math.abs(vy) > 0.02) {
                targetPitch = (float)Mth.clamp(Mth.atan2(vy, vh) * 180.0F / (float)Math.PI * 0.7, -35.0, 30.0);
            }

            targetRoll = Mth.clamp(Mth.wrapDegrees(crow.getYRot() - crow.yRotO) * 3.0F, -45.0F, 45.0F);
            if (vy > 0.05) {
                targetFlapSpeed = 1.35F;
            }
        }

        this.pitch = this.pitch + (targetPitch - this.pitch) * 0.2F;
        this.roll = this.roll + (targetRoll - this.roll) * 0.2F;
        this.flapSpeed = this.flapSpeed + (targetFlapSpeed - this.flapSpeed) * 0.1F;
    }

    public boolean isVisuallyFlying(CrowEntity crow) {
        return !crow.isInSittingPose() && !crow.isPassenger() ? this.airborneTicks > 3 || this.airborneTicks > 0 && crow.isGliding() : false;
    }

    public float pitch(float partialTick) {
        return Mth.lerp(partialTick, this.pitchO, this.pitch);
    }

    public float roll(float partialTick) {
        return Mth.lerp(partialTick, this.rollO, this.roll);
    }

    public float flapSpeed() {
        return this.flapSpeed;
    }
}
