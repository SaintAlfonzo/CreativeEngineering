package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.crops.StemExtraFruit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Extra stem fruit ({@link StemExtraFruit}). {@code AttachedStemBlock} overrides none of the three methods
 * added here, so they are new methods carrying the SRG names of {@code Block.isRandomlyTicking} (m_6724_),
 * {@code BlockBehaviour.randomTick} (m_213898_) and {@code BlockBehaviour.onRemove} (m_6810_) and override
 * them at runtime (no refmap in this build). isRandomlyTicking is always true, not tied to the config:
 * chunk sections keep a count of ticking blocks, and a value that changes on a config reload would
 * throw that count off. {@code updateShape} (m_7417_) keeps the stem attached to another of its fruits
 * when the one it faces goes.
 */
@Mixin(value = AttachedStemBlock.class, remap = false)
public abstract class AttachedStemExtraFruitMixin {

    public boolean m_6724_(BlockState state) {
        return true;
    }

    public void m_213898_(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        StemExtraFruit.randomTick(state, level, pos, random);
    }

    public void m_6810_(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // BlockBehaviour.onRemove's body, since super can't be called from here
        if (state.hasBlockEntity() && !state.is(newState.getBlock())) level.removeBlockEntity(pos);
        StemExtraFruit.onRemove(state, level, pos, newState);
    }

    @Inject(method = "m_7417_", at = @At("RETURN"), cancellable = true, remap = false)
    private void creativeengineering$reattach(BlockState state, Direction direction, BlockState neighbor,
                                              LevelAccessor level, BlockPos pos, BlockPos neighborPos,
                                              CallbackInfoReturnable<BlockState> cir) {
        BlockState other = StemExtraFruit.reattach(state, cir.getReturnValue(), level, pos);
        if (other != null) cir.setReturnValue(other);
    }
}
