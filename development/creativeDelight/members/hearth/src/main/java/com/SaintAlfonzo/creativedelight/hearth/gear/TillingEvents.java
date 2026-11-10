package com.SaintAlfonzo.creativedelight.hearth.gear;

import com.SaintAlfonzo.creativedelight.hearth.block.HHBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * Sneak-use with a Tilling hoe. Goes through the hoe tool action, so modded soils work (B9 fixed), and end stone tills to
 * Tilled End Stone. One durability per block tilled. Even sizes reach one block toward the look direction, as Creative
 * Delight's hoe harvest area does.
 */
public final class TillingEvents {
    private TillingEvents() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack hoe = event.getItemStack();
        if (!player.isShiftKeyDown() || !(hoe.getItem() instanceof HoeItem)) {
            return;
        }
        int level = EnchantmentHelper.getItemEnchantmentLevel(HHGearRegistry.TILLING.get(), hoe);
        if (level <= 0 || event.getFace() == Direction.DOWN) {
            return;
        }
        Level world = event.getLevel();
        BlockPos center = event.getPos();
        if (!canTill(world, center)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(world.isClientSide));
        if (world.isClientSide) {
            return;
        }
        int[] xs = span(level + 2, player.getLookAngle().x);
        int[] zs = span(level + 2, player.getLookAngle().z);
        boolean tilled = false;
        for (int x = xs[0]; x <= xs[1]; x++) {
            for (int z = zs[0]; z <= zs[1]; z++) {
                BlockPos pos = center.offset(x, 0, z);
                if (till(world, pos, player, hoe, event)) {
                    tilled = true;
                    hoe.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
                    if (hoe.isEmpty()) {
                        break;
                    }
                }
            }
            if (hoe.isEmpty()) {
                break;
            }
        }
        if (tilled) {
            world.playSound(null, center, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private static int[] span(int size, double look) {
        int low = (size - 1) / 2;
        int high = size / 2;
        return look < 0 ? new int[]{-high, low} : new int[]{-low, high};
    }

    private static boolean canTill(Level world, BlockPos pos) {
        return world.getBlockState(pos.above()).isAir();
    }

    private static boolean till(Level world, BlockPos pos, Player player, ItemStack hoe, PlayerInteractEvent.RightClickBlock event) {
        if (!canTill(world, pos)) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        BlockState result;
        if (state.is(Blocks.END_STONE)) {
            result = HHBlocks.TILLED_END_STONE.get().defaultBlockState();
        } else {
            Vec3 hit = Vec3.atCenterOf(pos).add(0.0D, 0.5D, 0.0D);
            UseOnContext context = new UseOnContext(player, event.getHand(), new BlockHitResult(hit, Direction.UP, pos, false));
            result = state.getToolModifiedState(context, ToolActions.HOE_TILL, false);
        }
        if (result == null || result == state) {
            return false;
        }
        world.setBlock(pos, result, 11);
        return true;
    }

    static int clampLevel(int level) {
        return Mth.clamp(level, 1, 3);
    }
}
