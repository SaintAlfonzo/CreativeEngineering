package com.SaintAlfonzo.creativecompat.cats;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.animal.Cat;

/**
 * A cat claws the mob its owner's Feather on a Stick holds ({@link CatFeather}). Added at priority 1 to every cat on
 * the server, so it outranks Cozy's home goal (2), sitting (2), lying on beds and following (5, 6) and vanilla's ocelot
 * attack (9) while it runs; it only runs for a feathered target and stops the moment the feather lets go.
 */
final class FeatherAttackGoal extends MeleeAttackGoal {

    private final Cat cat;

    FeatherAttackGoal(Cat cat) {
        super(cat, 1.3, true);
        this.cat = cat;
    }

    private boolean feathered() {
        LivingEntity target = cat.getTarget();
        return target != null && !cat.isOrderedToSit() && CatFeather.isFeatheredFor(cat, target);
    }

    @Override
    public boolean canUse() {
        if (!feathered()) {
            CatFeather.dropStaleTarget(cat);
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return feathered() && super.canContinueToUse();
    }

    @Override
    public void stop() {
        super.stop();
        CatFeather.dropStaleTarget(cat);
    }
}
