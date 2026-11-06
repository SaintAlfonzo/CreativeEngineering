// Pack addition (CreativeEngineering2, TODO dishes/jams #8). Own work, GPLv3.
package net.ribs.vintagedelight.jam;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A block of jam, as Fruits Delight's jam block: honey's slide and stickiness, sticks to the jello block of the same
 * jam, does not join other sticky blocks.
 */
public class JamStorageBlock extends HoneyBlock {
    public final String jam;

    public JamStorageBlock(Properties properties, String jam) {
        super(properties);
        this.jam = jam;
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    @Override
    public boolean canStickTo(BlockState state, BlockState other) {
        if (other.getBlock() instanceof JelloStorageBlock jello) {
            return jello.jam.equals(this.jam);
        }
        return other.isStickyBlock() && other.getBlock() != this ? false : super.canStickTo(state, other);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable("vintagedelight.tooltip.jam_block").withStyle(ChatFormatting.GRAY));
    }
}
