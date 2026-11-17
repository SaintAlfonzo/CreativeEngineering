// Creative Delight overlay for the Salt repack (GNU GPL3, like the rest of the pack's code). Not upstream code.
package io.github.mortuusars.salt.ce;

import io.github.mortuusars.salt.Melting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Salt Melt (plan_post_scratch_1115 B6, feature_writeup/salt.md 7): 64 uses; a click on any melting target (Creative
 * Common's meltable and meltable_slow tags) melts it one stage (packed and blue ice step down a tier, the rest melts) for
 * 1 durability. In a dispenser it does the same to the block in front; facing anything else it does nothing and stays.
 */
public class SaltMeltItem extends Item {

    public SaltMeltItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!Melting.isMeltTarget(state)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            if (!Melting.meltOnce(state, pos, server)) return InteractionResult.PASS;
            Player player = context.getPlayer();
            ItemStack stack = context.getItemInHand();
            if (player != null) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            } else if (stack.hurt(1, server.getRandom(), null)) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Dispenser: melt the target in front for 1 durability; otherwise fail (the item stays in the dispenser). */
    public static final class Dispense extends OptionalDispenseItemBehavior {
        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos target = source.getPos().relative(facing);
            ServerLevel level = source.getLevel();
            boolean melted = Melting.meltOnce(level.getBlockState(target), target, level);
            setSuccess(melted);
            if (melted && stack.hurt(1, level.getRandom(), null)) {
                stack.shrink(1);
            }
            return stack;
        }
    }
}
