package com.SaintAlfonzo.creativeworldgen.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.quark.content.world.gen.SpiralSpireGenerator;

import java.util.Random;

/**
 * Quark's Spiral Spires (End outer islands) are placed on the top end stone of a column and never look at the ground
 * around it, so a spire near an island edge hangs its base over the void. Any air under the spire's footprint now
 * cancels that spire (structures/contents #5).
 *
 * The footprint is the base layer Quark builds at the surface (its y 0 layer): a disc of radius 7 (i * i + j * j <= 49,
 * i and j from -6 to 6). Its radius is ceil((pi / 2 - atan(0.5 / (height / composition))) * 4), which is 7 for every
 * height (50 to 69) and composition (5 to 6) Quark rolls, so the check needs no random draw and the roll sequence is
 * unchanged. The column under each footprint cell (y - 1) must not be air. Only this spire is dropped; the other
 * spires of the same cluster roll on their own.
 */
@Mixin(value = SpiralSpireGenerator.class, remap = false)
public abstract class QuarkSpiralSpireFootprintMixin {

    private static final int CWG_FOOTPRINT_RADIUS = 7;

    @Inject(method = "makeSpike", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$cancelOverAir(WorldGenRegion world, ChunkGenerator generator, Random rand,
                                                   BlockPos pos, CallbackInfo ci) {
        int r = CWG_FOOTPRINT_RADIUS;
        BlockPos.MutableBlockPos test = new BlockPos.MutableBlockPos();
        for (int i = -r + 1; i < r; i++) {
            for (int j = -r + 1; j < r; j++) {
                if (i * i + j * j > r * r) continue;
                test.set(pos.getX() + i, pos.getY() - 1, pos.getZ() + j);
                if (world.getBlockState(test).isAir()) {
                    ci.cancel();
                    return;
                }
            }
        }
    }
}
