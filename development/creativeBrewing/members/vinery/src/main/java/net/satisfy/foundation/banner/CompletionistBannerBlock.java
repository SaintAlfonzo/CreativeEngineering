package net.satisfy.foundation.banner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The standing completionist banner. Gives nearby players the banner's effect
 * as a little thank you. Each mod passes its own {@link BannerSettings}.
 */
public class CompletionistBannerBlock extends BaseEntityBlock {
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    private final BannerSettings settings;

    public CompletionistBannerBlock(Properties properties, BannerSettings settings) {
        super(properties);
        this.settings = settings;
        makeDefaultState();
    }

    public BannerSettings getSettings() {
        return this.settings;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new CompletionistBannerEntity(blockPos, blockState);
    }

    protected void makeDefaultState() {
        registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
    }

    @Override
    public boolean canSurvive(@NotNull BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return levelReader.getBlockState(blockPos.below()).getMaterial().isSolid();
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace == Direction.UP || clickedFace == Direction.DOWN) {
            return this.defaultBlockState().setValue(ROTATION, Mth.floor((double) ((180.0f + context.getRotation()) * 16.0f / 360.0f) + 0.5) & 0xF);
        }
        return this.settings.wallBanner().get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, clickedFace.getOpposite());
    }

    @Override
    public @NotNull BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(ROTATION, rotation.rotate(blockState.getValue(ROTATION), 16));
    }

    @Override
    public @NotNull BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.setValue(ROTATION, mirror.mirror(blockState.getValue(ROTATION), 16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, this.settings.type().get(), (level1, pos, state1, entity) -> CompletionistBannerEntity.tick(level1, pos, state1));
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState blockState, @NotNull Direction direction, @NotNull BlockState blockState2, @NotNull LevelAccessor levelAccessor, @NotNull BlockPos blockPos, @NotNull BlockPos blockPos2) {
        if (direction == Direction.DOWN && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    public ResourceLocation getRenderTexture() {
        return this.settings.texture();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        String prefix = this.settings.tooltipPrefix();
        tooltip.add(Component.translatable(prefix + ".thankyou_1").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        int radius = this.settings.radius().getAsInt();
        if (radius > 0 && this.settings.showEffect().getAsBoolean()) {
            // Port (alcohol.md B7.7): from the settings, not upstream's fixed "Regeneration II within 8 blocks"
            MutableComponent effect = this.settings.effect().get().getDisplayName().copy();
            int amplifier = this.settings.amplifier().getAsInt();
            if (amplifier > 0) {
                effect.append(" ").append(Component.translatable("enchantment.level." + (amplifier + 1)));
            }
            tooltip.add(Component.translatable(prefix + ".thankyou_2").withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.translatable("tooltip.foundation.banner.effect", effect, radius, this.settings.durationTicks().getAsInt() / 20).withStyle(ChatFormatting.BLUE));
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(prefix + ".thankyou_3").withStyle(ChatFormatting.GOLD));
    }
}
