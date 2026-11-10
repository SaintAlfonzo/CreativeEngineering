package com.SaintAlfonzo.creativedelight.hearth.block;

import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

/** A bucket of sap boiled over fire: two steps of 24 s, then a glass bottle takes one Rustic Delight syrup (D4). */
public class SapCauldronBlock extends AbstractCauldronBlock {
    public static final IntegerProperty SAP_LEVEL = IntegerProperty.create("sap_level", 0, 3);
    private static final int STEP = 480;
    private static final ResourceLocation SYRUP = new ResourceLocation("rusticdelight", "syrup");

    public SapCauldronBlock(Properties properties) {
        super(properties, CauldronInteraction.EMPTY);
        registerDefaultState(stateDefinition.any().setValue(SAP_LEVEL, 3));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(Items.CAULDRON);
    }

    @Override
    public boolean isFull(BlockState state) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SAP_LEVEL);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.GLASS_BOTTLE) || state.getValue(SAP_LEVEL) != 1) return InteractionResult.PASS;
        if (!level.isClientSide) {
            Item syrup = ForgeRegistries.ITEMS.getValue(SYRUP);
            level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
            if (!player.getAbilities().instabuild) held.shrink(1);
            if (syrup != null) {
                ItemStack bottle = new ItemStack(syrup);
                if (!player.getInventory().add(bottle)) player.drop(bottle, false);
            }
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) level.scheduleTick(pos, this, STEP);
    }

    private static boolean heated(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(Blocks.FIRE) || (below.hasProperty(BlockStateProperties.LIT) && below.getValue(BlockStateProperties.LIT));
    }

    /** Fix B6: upstream stops for good if the first tick finds no fire; this keeps checking while there is sap to boil. */
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int sap = state.getValue(SAP_LEVEL);
        if (sap <= 1) return;
        if (!heated(level, pos)) {
            level.scheduleTick(pos, this, STEP);
            return;
        }
        level.setBlock(pos, state.setValue(SAP_LEVEL, sap - 1), 3);
        level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(SAP_LEVEL) > 0 && heated(level, pos) && random.nextFloat() < 0.2F) {
            level.addParticle(ParticleTypes.BUBBLE_POP, pos.getX() + 0.5D + (random.nextDouble() * 0.6D - 0.3D), pos.getY() + 1.0D, pos.getZ() + 0.5D + (random.nextDouble() * 0.6D - 0.3D), 0.0D, 0.1D, 0.0D);
        }
    }

    /** A sap bucket on an empty cauldron fills it. */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        if (!held.is(HHItems.SAP_BUCKET.get()) || !level.getBlockState(pos).is(Blocks.CAULDRON)) return;
        if (!level.isClientSide) {
            level.setBlock(pos, HHBlocks.SAP_CAULDRON.get().defaultBlockState(), 3);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
                ItemStack bucket = new ItemStack(Items.BUCKET);
                if (!player.getInventory().add(bucket)) player.drop(bucket, false);
            }
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        event.setCanceled(true);
    }
}
