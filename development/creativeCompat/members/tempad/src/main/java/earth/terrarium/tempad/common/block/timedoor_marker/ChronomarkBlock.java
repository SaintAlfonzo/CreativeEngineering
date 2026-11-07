// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/timedoor_marker/ChronomarkBlock.kt. Translated to
// Java for Forge 1.19.2 (no block codec on 1.19.2).
package earth.terrarium.tempad.common.block.timedoor_marker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ChronomarkBlock extends AbstractMarkerBlock {
    public ChronomarkBlock() {
        super();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChronomarkBE(pos, state);
    }
}
