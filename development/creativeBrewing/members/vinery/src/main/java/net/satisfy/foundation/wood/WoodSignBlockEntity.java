package net.satisfy.foundation.wood;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/** Port: 1.19.2's sign block entity takes no type, so the mod's own type comes back through {@link #getType()}. */
public class WoodSignBlockEntity extends SignBlockEntity {
    private final BlockEntityType<?> type;

    public WoodSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(pos, state);
        this.type = type;
    }

    @Override
    public @NotNull BlockEntityType<?> getType() {
        return type;
    }
}
