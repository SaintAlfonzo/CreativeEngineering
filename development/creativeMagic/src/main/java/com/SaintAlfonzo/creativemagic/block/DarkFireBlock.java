package com.SaintAlfonzo.creativemagic.block;

import com.SaintAlfonzo.creativemagic.dimension.IPNetherPortalBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.ModList;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraftforge.event.ForgeEventFactory;
import java.util.Optional;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;

public class DarkFireBlock extends BaseFireBlock {

    public DarkFireBlock(Properties properties) {
        super(properties, 1.0F);
    }

    @Override
    protected boolean canBurn(BlockState state) {
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // Checked against every neighbor rather than just below, so Dark
        // Fire lit against the side of a tall frame (a completely normal
        // way to ignite a portal) doesn't get ripped out by updateShape
        // immediately after placement.
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (level.getBlockState(neighborPos)
                    .isFaceSturdy(level, neighborPos, direction.getOpposite())) {
                return true;
            }
        }

        return false;
    }

    // No isAir() override. Forge's own docs on this method are explicit:
    // "primarily useful for creating pure logic-blocks that will be
    // invisible to the player." Overriding it to true is exactly what was
    // making the flame render as nothing - the client skips rendering
    // any block that reports isAir()==true, independent of blockstate/
    // model/texture setup. Portal-interior recognition is now handled by
    // adding this block to the minecraft:fire block tag instead (see the
    // dark_fire tag KubeJS script), which is what PortalShape's interior
    // check and PortalFailureHandler.isInterior both key off.

    @Override
    public void onPlace(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean movedByPiston
    ) {
        // Intentionally NOT calling super.onPlace(...) here. BaseFireBlock's
        // own onPlace doesn't do portal detection at all (that's FireBlock's
        // job, which we don't extend), so there's nothing useful in it for
        // us to inherit - and calling it was previously causing a second,
        // competing portal-detection pass to run before this method's own.

        if (level.isClientSide || oldState.is(this)) {
            return;
        }

        // Dark Fire is in the minecraft:fire block tag (see the comment
        // above), so PortalShape's interior check already accepts it
        // directly - no need to swap out for vanilla Blocks.FIRE first. This also means PortalSpawnEvent will see the
        // real Dark Fire state, not a temporarily-swapped one.
        Optional<PortalShape> optional =
                PortalShape.findPortalShape(
                        level,
                        pos,
                        PortalShape::isValid,
                        Direction.Axis.X
                );

        // With Immersive Portals loaded, a valid rectangle (the vanilla check above, so no new shapes) becomes an IP
        // seamless portal instead of vanilla portal blocks (plan_portal_changes.md 1, N1). The bridge returns false
        // when IP's mode is vanilla/disabled, outside the Overworld and the Nether, or when IP finds no frame; the
        // vanilla path below then runs as before. Loaded only behind the ModList check, as it touches IP classes.
        if (optional.isPresent() && level instanceof ServerLevel serverLevel
                && ModList.get().isLoaded("imm_ptl_core")
                && IPNetherPortalBridge.light(serverLevel, pos)) {
            return;
        }

        // This is just shape-detection + event-firing, not portal creation.
        // Whether (and how) the resulting shape actually gets turned into
        // portal blocks is entirely PortalFailureHandler's call from here -
        // this method doesn't touch createPortalBlocks() itself, so there's
        // only ever one place in the codebase that can spawn a portal.
        ForgeEventFactory.onTrySpawnPortal(level, pos, optional);
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (!this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(
                state,
                direction,
                neighborState,
                level,
                pos,
                neighborPos
        );
    }
}
