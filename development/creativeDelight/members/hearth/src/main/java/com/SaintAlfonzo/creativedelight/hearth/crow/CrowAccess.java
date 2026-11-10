package com.SaintAlfonzo.creativedelight.hearth.crow;

import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;

/**
 * The one class that names Alex's Mobs. The rest of the package works on {@link TamableAnimal}, which keeps vanilla calls
 * off AM's SRG-named class when the jar is reobfuscated.
 */
public final class CrowAccess {
    private CrowAccess() {
    }

    public static boolean isCrow(Entity entity) {
        return entity instanceof EntityCrow;
    }

    public static boolean isFlying(TamableAnimal crow) {
        return ((EntityCrow) crow).isFlying();
    }

    public static void setFlying(TamableAnimal crow, boolean flying) {
        ((EntityCrow) crow).setFlying(flying);
    }

    public static boolean isFetchingItem(TamableAnimal crow) {
        return ((EntityCrow) crow).aiItemFlag;
    }

    public static boolean isSitting(TamableAnimal crow) {
        return ((EntityCrow) crow).isSitting();
    }

    public static void peck(TamableAnimal crow) {
        ((EntityCrow) crow).peck();
    }

    /** AM's "follow" command, what its own taming sets. */
    public static void setFollowing(TamableAnimal crow) {
        ((EntityCrow) crow).setCommand(1);
    }

    public static SoundEvent squawk() {
        return AMSoundRegistry.CROW_IDLE.get();
    }
}
