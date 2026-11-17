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
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
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
 * Right-click at age 3 picks 1 to 2 hops and goes back to age 1; younger vines are not picked (R5, plan_small_fixes_1116.md).
 * Shears toggle {@code sterilized}: no growth and no spreading. Bone meal and every bone-meal-like item adds one age. Planted with the hops item. Exists only with Vinery loaded, whose
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
        if (age < MAX_AGE) {
            // R5 (plan_small_fixes_1116.md): no harvest before age 3. A real BoneMealItem passes to its own useOn (vanilla
            // bone meal, BonemealEvent); every other bone-meal-like item grows the vine one age here; anything else passes.
            if (held.getItem() instanceof BoneMealItem || !isFertilizer(held)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                performBonemeal((ServerLevel) level, level.random, pos, state);
                level.levelEvent(1505, pos, 0); // bone meal particles and sound for every client
                if (!player.getAbilities().instabuild) {
                    if (held.isDamageableItem()) {
                        held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                    } else {
                        held.shrink(1);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            popResource(level, pos, new ItemStack(HHItems.HOPS.get(), level.random.nextInt(2) + 1));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            level.setBlock(pos, state.setValue(AGE, 1), 2);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** H&H's watering-can fertilisers and {@code #creativedelight:bonemeal_like} (tree fertilizer, IE and Botania fertilizer, fertilizer bag, organic mash). */
    public static final TagKey<Item> BONEMEAL_SUBSTITUTES = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("hearthandharvest", "bonemeal_substitutes"));
    public static final TagKey<Item> BONEMEAL_LIKE = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("creativedelight", "bonemeal_like"));

    private static boolean isFertilizer(ItemStack stack) {
        return stack.is(BONEMEAL_SUBSTITUTES) || stack.is(BONEMEAL_LIKE);
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
