// Creative Delight overlay for the Salt repack (GNU GPL3, like the rest of the pack's code). Not upstream code.
package io.github.mortuusars.salt.ce;

import io.github.mortuusars.salt.Melting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Salt Melt (plan_post_scratch_1115 B6, G7.6, feature_writeup/salt.md 7): 64 uses; a click on a block salt can melt
 * (Creative Common's saltable tag) melts it once for 1 durability. In a dispenser it does the same to the block in front;
 * facing anything else it does nothing and stays.
 * <p>
 * G7.6: undamaged Salt Melts stack to 16 ({@link #getMaxStackSize(ItemStack)}), a damaged one to 1 (vanilla already
 * refuses to stack damaged items). A use from a stack of more than one splits one off, damages it and hands it back
 * (inventory, else dropped; a dispenser keeps it, else drops it), so a stack never holds a damaged item.
 */
public class SaltMeltItem extends Item {
    public static final int STACK = 16;

    public SaltMeltItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return stack.isDamaged() ? 1 : STACK;
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
            if (player == null) {
                if (stack.getCount() > 1) {
                    ItemStack used = stack.split(1);
                    if (!damage(used, server.getRandom())) Block.popResource(server, pos.relative(context.getClickedFace()), used);
                } else {
                    damage(stack, server.getRandom());
                }
            } else if (!player.getAbilities().instabuild) {
                if (stack.getCount() > 1) {
                    ItemStack used = stack.split(1);
                    if (!damage(used, server.getRandom()) && !player.getInventory().add(used)) {
                        player.drop(used, false);
                    }
                } else {
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Damages a single item by 1; true when it broke (and is now empty). */
    private static boolean damage(ItemStack single, RandomSource random) {
        if (single.hurt(1, random, null)) {
            single.shrink(1);
            return true;
        }
        return false;
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
            if (!melted) return stack;
            if (stack.getCount() > 1) {
                ItemStack used = stack.split(1);
                if (!damage(used, level.getRandom())) {
                    // Back into the dispenser (a free slot or one it stacks with), else out in front of it.
                    if (!(source.getEntity() instanceof DispenserBlockEntity dispenser) || dispenser.addItem(used) < 0) {
                        DefaultDispenseItemBehavior.spawnItem(level, used, 6, facing, DispenserBlock.getDispensePosition(source));
                    }
                }
            } else {
                damage(stack, level.getRandom());
            }
            return stack;
        }
    }
}
