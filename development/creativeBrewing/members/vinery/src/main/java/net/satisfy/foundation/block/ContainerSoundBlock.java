package net.satisfy.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ContainerSoundBlock {
    void playSound(Level level, BlockPos pos, boolean open);
}
