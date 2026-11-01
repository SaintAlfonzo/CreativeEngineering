package net.satisfy.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.satisfy.foundation.util.LibUtil.popResourceFromFace;
import net.minecraftforge.common.ForgeHooks;

/**
 * Leaves that grow fruit over time (apples, cherries ...). Only some leaves can carry fruit,
 * rolled on placement. Ripe fruit (age 3) is picked with an empty hand, bone meal speeds it up.
 * <p>
 * Subclasses name the two boolean properties themselves (keeps existing blockstates/worlds intact)
 * and say which item drops and how fast it grows. Leaf distance stays vanilla: neighbour updates
 * only schedule a tick, they never change the state right away.
 */
public abstract class FruitLeavesBlock extends LeavesBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);

    public FruitLeavesBlock(Properties properties) {
        this(properties, false);
    }

    public FruitLeavesBlock(Properties properties, boolean persistentByDefault) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(PERSISTENT, persistentByDefault)
                .setValue(DISTANCE, 7)
                .setValue(canGrowFruitProperty(), false)
                .setValue(hasFruitProperty(), false)
                .setValue(AGE, 0)
                .setValue(WATERLOGGED, false));
    }

    /** Whether these leaves can carry fruit at all. Must be a static constant of the subclass. */
    protected abstract BooleanProperty canGrowFruitProperty();

    /** Whether fruit is currently hanging. Must be a static constant of the subclass. */
    protected abstract BooleanProperty hasFruitProperty();

    protected abstract Item fruitItem();

    /** Chance per random tick to advance one growth step. */
    protected abstract double growthChance();

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        return useWithoutItem(state, level, pos, player, hit);
    }

    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(hasFruitProperty()) && state.getValue(AGE) == 3) {
            if (!level.isClientSide()) {
                int dropCount = level.getRandom().nextBoolean() ? level.getRandom().nextInt(1, 4) : 1;
                popResourceFromFace(level, pos, hit.getDirection(), new ItemStack(fruitItem(), dropCount));
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1F, 1F);
                level.setBlock(pos, state.setValue(hasFruitProperty(), false).setValue(AGE, 1), 2);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state) {
        boolean canGrow = state.getValue(canGrowFruitProperty());
        boolean has = state.getValue(hasFruitProperty());
        int age = state.getValue(AGE);
        return (canGrow && !has && age < 2) || (has && age < 3) || super.isRandomlyTicking(state);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean canGrow = context.getLevel().random.nextFloat() < 0.3f;
        return updateDistance(defaultBlockState()
                .setValue(PERSISTENT, true)
                .setValue(canGrowFruitProperty(), canGrow)
                .setValue(AGE, 0)
                .setValue(hasFruitProperty(), false)
                .setValue(WATERLOGGED, false), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(canGrowFruitProperty(), hasFruitProperty(), AGE);
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        // Port (W6): 1.3.10 leaves with fruit have no age (they load as age 0 with fruit); they become ripe fruit leaves
        if (state.getValue(hasFruitProperty()) && state.getValue(AGE) < 2) {
            level.setBlock(pos, state.setValue(AGE, 3).setValue(canGrowFruitProperty(), true), 2);
            return;
        }
        super.randomTick(state, level, pos, random);
        // Port: vanilla randomTick may have just removed the decayed leaves
        if (!level.getBlockState(pos).is(this)) {
            return;
        }
        // Port: fire Forge's crop-grow event so the pack's crop seasons apply (crops.md 2.12.1)
        if (random.nextDouble() < growthChance() && ForgeHooks.onCropsGrowPre(level, pos, level.getBlockState(pos), true)) {
            grow(level, pos, level.getBlockState(pos));
        }
    }

    /** Advances the fruit one step if possible. Returns false when nothing changed. */
    protected boolean grow(Level level, BlockPos pos, BlockState state) {
        if (!canGrowHere(level, pos)) return false;
        boolean has = state.getValue(hasFruitProperty());
        int age = state.getValue(AGE);
        BlockState newState;
        if (!has && age < 2 && state.getValue(canGrowFruitProperty())) {
            newState = age == 0 ? state.setValue(AGE, 1) : state.setValue(AGE, 2).setValue(hasFruitProperty(), true);
        } else if (has && age == 2) {
            newState = state.setValue(AGE, 3);
        } else {
            return false;
        }
        level.setBlock(pos, newState, 2);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
        return true;
    }

    protected boolean canGrowHere(LevelReader level, BlockPos pos) {
        return level.getRawBrightness(pos, 0) > 9;
    }

    @Override
    public boolean isValidBonemealTarget(@NotNull BlockGetter level, @NotNull BlockPos pos, BlockState state, boolean isClient) {
        int age = state.getValue(AGE);
        boolean has = state.getValue(hasFruitProperty());
        return (age < 2 && !has) || (age == 2 && has);
    }

    @Override
    public boolean isBonemealSuccess(@NotNull Level level, @NotNull RandomSource random, @NotNull BlockPos pos, @NotNull BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(@NotNull ServerLevel level, @NotNull RandomSource random, @NotNull BlockPos pos, BlockState state) {
        grow(level, pos, state.setValue(canGrowFruitProperty(), true));
    }

    @Override
    public int getLightBlock(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 1;
    }

    private static BlockState updateDistance(BlockState state, LevelAccessor level, BlockPos pos) {
        int minDistance = 7;
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            int distance = neighbor.getBlock() instanceof LeavesBlock ? neighbor.getValue(DISTANCE) : (neighbor.is(BlockTags.LOGS) ? 0 : 7);
            minDistance = Math.min(minDistance, distance + 1);
            if (minDistance == 1) break;
        }
        return state.setValue(DISTANCE, minDistance);
    }
}
