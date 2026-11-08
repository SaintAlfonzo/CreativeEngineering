// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/WorkstationItem.kt. Translated to Java for Forge 1.19.2:
// the properties are passed in (ModItems adds the creative tab, as for every phase-1 item); the block entity's owner
// attachment -> WorkstationBE.owner.
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.common.block.WorkstationBE;
import earth.terrarium.tempad.common.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WorkstationItem extends BlockItem {
    public WorkstationItem(Properties properties) {
        super(ModBlocks.workstation.get(), properties);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        if (level.getBlockEntity(pos) instanceof WorkstationBE blockEntity && player != null) {
            blockEntity.owner = player.getGameProfile();
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }
}
