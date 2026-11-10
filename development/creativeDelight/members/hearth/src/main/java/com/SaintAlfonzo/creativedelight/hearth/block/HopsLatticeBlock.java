package com.SaintAlfonzo.creativedelight.hearth.block;

import com.SaintAlfonzo.creativedelight.hearth.HHConfig;
import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Hops growing on Vinery's grapevine lattice (N21: no H&H trellis). Planting swaps the empty lattice for this block, which
 * keeps its facing and drops the lattice back. Age 0 to 4; at 4 it harvests for 2 to 4 hops and spreads up, to height 5,
 * and sideways along empty lattices. The column's base must stand on farmland (config).
 */
public class HopsLatticeBlock extends Block implements BonemealableBlock {
    public static final int MAX_AGE = 4;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final ResourceLocation LATTICE = new ResourceLocation("vinery", "grapevine_lattice");
    private static final int MAX_HEIGHT = 5;
    private static final VoxelShape SHAPE_N = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_E = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_S = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    private static final VoxelShape SHAPE_W = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public HopsLatticeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AGE, 0).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> SHAPE_E;
            case SOUTH -> SHAPE_S;
            case WEST -> SHAPE_W;
            default -> SHAPE_N;
        };
    }

    /** Right-click with hops on an empty lattice. */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(HHItems.HOPS.get())) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!isEmptyLattice(state)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        if (level.isClientSide) {
            return;
        }
        level.setBlock(pos, plantedFrom(state), 3);
        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!event.getEntity().getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private static boolean isEmptyLattice(BlockState state) {
        if (!LATTICE.equals(ForgeRegistries.BLOCKS.getKey(state.getBlock()))) {
            return false;
        }
        Property<?> age = state.getBlock().getStateDefinition().getProperty("age");
        return age instanceof IntegerProperty integer && state.getValue(integer) == 0;
    }

    private static BlockState plantedFrom(BlockState lattice) {
        BlockState hops = HHBlocks.HOPS.get().defaultBlockState();
        Property<?> facing = lattice.getBlock().getStateDefinition().getProperty("facing");
        return facing instanceof DirectionProperty direction ? hops.setValue(FACING, lattice.getValue(direction)) : hops;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos base = columnBase(level, pos);
        if (HHConfig.HOPS_REQUIRE_FARMLAND.get() && !(level.getBlockState(base.below()).getBlock() instanceof FarmBlock)) {
            return;
        }
        if (!ForgeHooks.onCropsGrowPre(level, pos, state, true)) {
            return;
        }
        int age = state.getValue(AGE);
        if (age < MAX_AGE) {
            if (random.nextInt(5) == 0) {
                level.setBlock(pos, state.setValue(AGE, age + 1), 2);
                ForgeHooks.onCropsGrowPost(level, pos, state);
            }
        } else if (random.nextInt(7) == 0) {
            spread(level, pos, pos.getY() - base.getY());
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }

    private BlockPos columnBase(Level level, BlockPos pos) {
        BlockPos current = pos;
        while (level.getBlockState(current.below()).getBlock() == this) {
            current = current.below();
        }
        return current;
    }

    private void spread(ServerLevel level, BlockPos pos, int height) {
        if (height < MAX_HEIGHT && plantIfEmpty(level, pos.above())) {
            return;
        }
        plantIfEmpty(level, pos.relative(Direction.Plane.HORIZONTAL.getRandomDirection(level.random)));
    }

    private boolean plantIfEmpty(ServerLevel level, BlockPos pos) {
        BlockState target = level.getBlockState(pos);
        if (!isEmptyLattice(target)) {
            return false;
        }
        level.setBlock(pos, plantedFrom(target), 3);
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.SHEARS)) {
            if (!level.isClientSide) {
                popResource(level, pos, new ItemStack(HHItems.HOPS.get()));
                level.setBlock(pos, emptyLattice(state), 3);
                level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (state.getValue(AGE) == MAX_AGE) {
            if (!level.isClientSide) {
                popResource(level, pos, new ItemStack(HHItems.HOPS.get(), 2 + level.random.nextInt(3)));
                level.setBlock(pos, state.setValue(AGE, 0), 3);
                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private BlockState emptyLattice(BlockState state) {
        Block lattice = ForgeRegistries.BLOCKS.getValue(LATTICE);
        BlockState out = lattice.defaultBlockState();
        Property<?> facing = lattice.getStateDefinition().getProperty("facing");
        return facing instanceof DirectionProperty direction ? out.setValue(direction, state.getValue(FACING)) : out;
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean client) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        } else {
            spread(level, pos, pos.getY() - columnBase(level, pos).getY());
        }
    }
}
