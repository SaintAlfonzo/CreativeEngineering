package com.SaintAlfonzo.creativedelight.hearth.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The placed Four-Leaf Clover (N23-8). The item stays Buzzier Bees' {@code buzzier_bees:four_leaf_clover}: using it on
 * grass or dirt places this block, breaking it drops that item (loot table, no tool needed).
 */
public class FourLeafCloverBlock extends FlowerBlock {
    private static final ResourceLocation ITEM_ID = new ResourceLocation("buzzier_bees", "four_leaf_clover");

    public FourLeafCloverBlock() {
        super(MobEffects.LUCK, 10, BlockBehaviour.Properties.copy(Blocks.POPPY));
    }

    private static Item cloverItem() {
        return ForgeRegistries.ITEMS.getValue(ITEM_ID);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        Item item = cloverItem();
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    /** Buzzier Bees' item has no use of its own, so the placement is an event, not a replaced item class. */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !ITEM_ID.equals(ForgeRegistries.ITEMS.getKey(stack.getItem())) || event.getEntity() == null) {
            return;
        }
        Level level = event.getLevel();
        BlockPos clicked = event.getPos();
        Direction face = event.getHitVec().getDirection();
        BlockState clickedState = level.getBlockState(clicked);
        BlockPos target = clickedState.getMaterial().isReplaceable() ? clicked : clicked.relative(face);
        if (target.equals(clicked) ? false : face != Direction.UP) {
            return;
        }
        BlockState clover = HHWorldgen.FOUR_LEAF_CLOVER.get().defaultBlockState();
        Player player = event.getEntity();
        BlockState existing = level.getBlockState(target);
        if ((!existing.isAir() && !existing.getMaterial().isReplaceable()) || !existing.getFluidState().isEmpty()
                || !clover.canSurvive(level, target) || !player.mayUseItemAt(target, face, stack)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        if (level.isClientSide) {
            return;
        }
        level.setBlock(target, clover, 3);
        SoundType sound = clover.getSoundType(level, target, player);
        level.playSound(null, target, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}
