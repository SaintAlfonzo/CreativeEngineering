package com.SaintAlfonzo.creativedelight.hearth.entity;

import net.minecraft.world.entity.animal.Fox;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

/** Gives every fox the berry-bush goal on joining a server level (1.4.1: priority 10, vanilla's berry goal slot). */
public final class FoxBerryGoals {
    private FoxBerryGoals() {
    }

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Fox fox) {
            fox.goalSelector.addGoal(10, new FoxEatBushBerriesGoal(fox, 1.2D, 12, 1));
        }
    }
}
