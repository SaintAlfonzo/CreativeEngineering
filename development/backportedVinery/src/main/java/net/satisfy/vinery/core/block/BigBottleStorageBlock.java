package net.satisfy.vinery.core.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.satisfy.vinery.core.wine.WineYears;
import org.jetbrains.annotations.Nullable;
import java.util.List;

import net.satisfy.vinery.core.registry.EntityTypeRegistry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.satisfy.foundation.storage.StorageBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.vinery.core.registry.StorageTypeRegistry;
import net.satisfy.vinery.core.item.DrinkBlockItem;
import org.jetbrains.annotations.NotNull;

public class BigBottleStorageBlock extends StorageBlock {
    public BigBottleStorageBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(OPEN, false));
    }

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    /** The largest wine this block takes (alcohol.md 10.12). */
    private static final DrinkBlockItem.BottleSize LARGEST = DrinkBlockItem.BottleSize.LARGE;
    private static final SoundEvent OPEN_SOUND = SoundEvents.WOODEN_DOOR_OPEN;

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(player.getUsedItemHand());
        if (player.isShiftKeyDown() && stack.isEmpty()) {
            if (!level.isClientSide()) {
                level.playSound(null, pos, OPEN_SOUND, SoundSource.BLOCKS, 0.4f, 0.4f);
                level.setBlock(pos, state.setValue(OPEN, !state.getValue(OPEN)), UPDATE_ALL);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        } else if (state.getValue(OPEN)) {
            return super.useWithoutItem(state, level, pos, player, hit);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN);
    }

    /** alcohol.md 10.12: every size; 1.5.5 used the small / large_bottle tags. */
    @Override
    public boolean canInsertStack(ItemStack stack) {
        return DrinkBlockItem.BottleSize.fits(stack, LARGEST);
    }

    @Override
    public BlockEntityType<?> blockEntityType() {
        return EntityTypeRegistry.STORAGE_ENTITY.get();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ResourceLocation type() {
        return StorageTypeRegistry.BIG_BOTTLE;
    }

    @Override
    public Direction[] unAllowedDirections() {
        return new Direction[]{Direction.DOWN, Direction.UP};
    }

    @Override
    public int getSection(Float x, Float y) {
        return 0;
    }

    /** Port: says what speeds up wine aging here (WineYears.storageKind). */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        DrinkBlockItem.BottleSize.addHoldsLine(tooltip, size(), LARGEST);
        WineYears.addStorageHint(tooltip);
    }
}
