package com.SaintAlfonzo.creativedelight.blockheads.mixin;

import com.SaintAlfonzo.creativedelight.blockheads.kitchen.CounterToppers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Counter toppers ({@link CounterToppers}, TODO cooking/kitchen #8, #20) on Cooking for Blockheads counters.
 * <ul>
 * <li>{@code canSurvive} (m_60710_, RETURN): FD's cutting board, Display Delight's plates and the Brewin' coaster want a
 * rigid or center support face below, which the 15 px counter lacks. A topper above a support stays. Placing
 * ({@code BlockItem.canPlace}) and the board's and coaster's {@code updateShape} all ask through the state, so this one
 * hook covers every topper. Only a {@code false} answer is looked at.</li>
 * <li>{@code updateShape} (m_60728_, RETURN): Display Delight's blocks check survival only when placed, so they stayed
 * in the air when the block below was broken. A change from below that leaves a topper or Display Delight block
 * unable to survive answers air, which vanilla's {@code updateOrDestroy} breaks with drops (as for a torch).</li>
 * <li>{@code getOffset} (m_60824_, HEAD): what the chunk mesher (vanilla and Rubidium) moves a block model by. None of the
 * toppers has an offset type, so without this it is always zero. Plated foods draw the food in the block model, so this
 * moves plate and food together.</li>
 * </ul>
 * Whether a state is a topper, and whether it pops without support, is cached per state for one tag generation
 * ({@code getOffset} runs for every block the mesher draws, so no tag lookup there): one int holding
 * {@code generation << 2 | pops << 1 | topper}, written in one store, so a chunk-builder thread never sees a
 * half-updated set.
 * SRG names with {@code remap = false}.
 */
@Mixin(BlockStateBase.class)
public abstract class BlockStateCounterTopperMixin {

    @Unique
    private int ce$topperKey;

    @Unique
    private int ce$topperFlags() {
        int gen = CounterToppers.generation();
        int key = ce$topperKey;
        if ((key >>> 2) != gen) {
            BlockState state = (BlockState) (Object) this;
            boolean topper = state.is(CounterToppers.TOPPERS);
            boolean pops = CounterToppers.popsUnsupported(state);
            key = (gen << 2) | (pops ? 2 : 0) | (topper ? 1 : 0);
            ce$topperKey = key;
        }
        return key;
    }

    @Unique
    private boolean ce$isTopper() {
        return (ce$topperFlags() & 1) != 0;
    }

    @Inject(method = "m_60710_", at = @At("RETURN"), cancellable = true, remap = false)
    private void ce$counterSupport(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && ce$isTopper() && CounterToppers.onSupport(level, pos)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "m_60728_", at = @At("RETURN"), cancellable = true, remap = false)
    private void ce$popUnsupported(Direction direction, BlockState neighbour, LevelAccessor level, BlockPos pos,
                                   BlockPos neighbourPos, CallbackInfoReturnable<BlockState> cir) {
        if (direction != Direction.DOWN || (ce$topperFlags() & 2) == 0) return;
        BlockState result = cir.getReturnValue();
        if (!result.isAir() && !result.canSurvive(level, pos)) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }

    @Inject(method = "m_60824_", at = @At("HEAD"), cancellable = true, remap = false)
    private void ce$counterTopperOffset(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Vec3> cir) {
        if (ce$isTopper()) {
            cir.setReturnValue(CounterToppers.offset(((BlockStateBase) (Object) this).getBlock(), level, pos));
        }
    }
}
