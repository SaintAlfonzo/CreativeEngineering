package com.SaintAlfonzo.creativedelight.oceanic.content;

import com.SaintAlfonzo.creativedelight.oceanic.registry.OceanicItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Sponge Cake (ocean_delight.md 5.6), four slices. A knife cuts one slice off (dropped on the cake); otherwise a
 * hungry player (or one in creative) eats a slice off the block: 3 hunger, 1 saturation and Water Breathing for 30 s.
 * The fourth slice takes the cake. Stands where a cake could; breaks without drops when it no longer can.
 */
public class SpongeCakeBlock extends Block {
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 3);
    private static final TagKey<Item> KNIVES = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("farmersdelight", "tools/knives"));
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 5, 14);

    public SpongeCakeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BITES, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BITES);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).getMaterial().isSolid();
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos from, boolean moving) {
        super.neighborChanged(state, level, pos, neighbor, from, moving);
        if (!state.canSurvive(level, pos)) level.destroyBlock(pos, false);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getMainHandItem().is(KNIVES)) {
            if (!level.isClientSide) {
                level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                ItemEntity slice = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5,
                        new ItemStack(OceanicItems.SPONGE_CAKE_SLICE.get()));
                slice.setPickUpDelay(5);
                level.addFreshEntity(slice);
                takeSlice(state, level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!player.canEat(false) && !player.getAbilities().instabuild) return InteractionResult.PASS;
        if (!level.isClientSide) {
            level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 1.0F);
            takeSlice(state, level, pos);
            player.getFoodData().eat(3, 1.0F / 6.0F);
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 600, 0));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void takeSlice(BlockState state, Level level, BlockPos pos) {
        int bites = state.getValue(BITES);
        if (bites < 3) {
            level.setBlock(pos, state.setValue(BITES, bites + 1), 3);
        } else {
            level.removeBlock(pos, false);
        }
    }
}
