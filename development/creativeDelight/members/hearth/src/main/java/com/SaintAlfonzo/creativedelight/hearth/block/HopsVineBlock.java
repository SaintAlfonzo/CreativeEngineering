package com.SaintAlfonzo.creativedelight.hearth.block;

import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;

/**
 * Hops vine (plan_bugfix_1114 3.1): the behaviour of Vinery's jungle grape vine (GrapeVineBlock), written here so Creative
 * Delight needs no Creative Brewing class at load. A vine on a wall face (any full face, as vanilla vines), age 0 to 3; it
 * grows 1 in 5 random ticks at light 9 or more (through Forge's crop events, so crop seasons apply) and spreads like a vine.
 * Right-click at age 2 or more picks 0 to 1 hops (+1 at age 3) and goes back to age 1. Shears toggle {@code sterilized}:
 * no growth and no spreading. Bone meal adds one age. Planted with the hops item. Exists only with Vinery loaded, whose
 * jungle grape models it draws (blockstate in this member, models by id from the donor jar).
 */
public class HopsVineBlock extends VineBlock implements BonemealableBlock {
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final BooleanProperty STERILIZED = BooleanProperty.create("sterilized");

    public HopsVineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0).setValue(STERILIZED, false).setValue(UP, false)
                .setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE, STERILIZED);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.SHEARS)) {
            level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, state.cycle(STERILIZED));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        int age = state.getValue(AGE);
        boolean ripe = age == MAX_AGE;
        if (!ripe && held.is(Items.BONE_MEAL)) {
            return InteractionResult.PASS;
        }
        if (age > 1) {
            if (!level.isClientSide) {
                int count = level.random.nextInt(2) + (ripe ? 1 : 0);
                if (count > 0) {
                    popResource(level, pos, new ItemStack(HHItems.HOPS.get(), count));
                }
                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
                level.setBlock(pos, state.setValue(AGE, 1), 2);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(STERILIZED);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE && random.nextInt(5) == 0 && level.getRawBrightness(pos.above(), 0) >= 9
                && ForgeHooks.onCropsGrowPre(level, pos, state, true)) {
            BlockState grown = state.setValue(AGE, age + 1);
            level.setBlock(pos, grown, 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(grown));
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
        super.randomTick(state, level, pos, random);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean client) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, Math.min(MAX_AGE, state.getValue(AGE) + 1)), 2);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(HHItems.HOPS.get());
    }
}
