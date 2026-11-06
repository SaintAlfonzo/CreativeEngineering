// Pack addition (CreativeEngineering2, TODO dishes/jams #8). Own work, GPLv3.
package net.ribs.vintagedelight.jam;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A block of jello, as Fruits Delight's jello block: slime's bounce and slipperiness, no slowdown when walked on,
 * sticks to sticky blocks, to the same jello and to the jam block of the same jam.
 */
public class JelloStorageBlock extends SlimeBlock {
    public final String jam;

    public JelloStorageBlock(Properties properties, String jam) {
        super(properties);
        this.jam = jam;
    }

    @Override
    public boolean isSlimeBlock(BlockState state) {
        return true;
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    @Override
    public boolean canStickTo(BlockState state, BlockState other) {
        if (other.getBlock() instanceof JamStorageBlock jamBlock) {
            return jamBlock.jam.equals(this.jam);
        }
        return other.getBlock() == this;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable("vintagedelight.tooltip.jello_block").withStyle(ChatFormatting.GRAY));
    }
}
