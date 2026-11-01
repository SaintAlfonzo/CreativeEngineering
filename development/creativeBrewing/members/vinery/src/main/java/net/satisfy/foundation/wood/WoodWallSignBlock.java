package net.satisfy.foundation.wood;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class WoodWallSignBlock extends WallSignBlock {
    private final Supplier<? extends BlockEntityType<?>> blockEntityType;

    public WoodWallSignBlock(WoodType woodType, Properties properties, Supplier<? extends BlockEntityType<?>> blockEntityType) {
        super(properties, woodType);
        this.blockEntityType = blockEntityType;
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new WoodSignBlockEntity(blockEntityType.get(), pos, state);
    }
}
