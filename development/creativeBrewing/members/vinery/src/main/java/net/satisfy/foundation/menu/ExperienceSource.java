package net.satisfy.foundation.menu;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public interface ExperienceSource {
    void dropExperience(ServerLevel level, Vec3 pos);
}
