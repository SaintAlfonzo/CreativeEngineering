package com.SaintAlfonzo.creativeworldgen.mixin.visceralheap;

import biomesoplenty.common.worldgen.feature.misc.FleshTendonFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Flesh tendons (feature_writeup/visceral_heap.md): reads the removed rose quartz buds safely, and keeps every tendon whole
 * and inside the Heap. BOP silently drops the part of a tendon outside the chunk write window and lets it cross biomes;
 * here the end point is pulled back along the tendon's line to whichever limit comes first. The names target
 * Biomes O' Plenty 17.1.2.544 only (WorldgenMixinPlugin pins that version).
 */
@Mixin(value = FleshTendonFeature.class, remap = false)
public abstract class HeapFleshTendonFeatureMixin {

    @Unique private static final ResourceKey<Biome> creativeworldgen$HEAP =
            ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation("biomesoplenty", "visceral_heap"));
    /** BOP's minimum sideways reach. */
    @Unique private static final int creativeworldgen$MIN_REACH = 8;
    /** Set by the end point redirect, read by the inject just after it, on the same thread and in the same call. */
    @Unique private static final ThreadLocal<Boolean> creativeworldgen$TOO_SHORT = ThreadLocal.withInitial(() -> false);

    @Shadow private boolean respectsCutoff(WorldGenRegion region, BlockPos pos) { throw new AssertionError(); }

    @Redirect(method = "lambda$new$0", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/registries/RegistryObject;get()Ljava/lang/Object;"))
    private static Object creativeworldgen$removedAsAir(RegistryObject<?> object) {
        return object.isPresent() ? object.get() : Blocks.AIR;
    }

    /** {@code dy} is BOP's own value (the start's height added to itself) and passes through unchanged. */
    @Redirect(method = "m_142674_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;m_7918_(III)Lnet/minecraft/core/BlockPos;"))
    private BlockPos creativeworldgen$pullEnd(BlockPos start, int dx, int dy, int dz, FeaturePlaceContext<NoneFeatureConfiguration> context) {
        creativeworldgen$TOO_SHORT.set(false);
        if (!(context.level() instanceof WorldGenRegion region)) return start.offset(dx, dy, dz);

        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        int reached = 0;
        for (int step = 1; step <= steps; step++) {
            int x = start.getX() + creativeworldgen$along(dx, step, steps);
            int z = start.getZ() + creativeworldgen$along(dz, step, steps);
            if (!creativeworldgen$fits(region, x, start.getY(), z)) break;
            reached = step;
        }
        if (reached == steps) return start.offset(dx, dy, dz);

        int pulledX = creativeworldgen$along(dx, reached, steps);
        int pulledZ = creativeworldgen$along(dz, reached, steps);
        if (Math.abs(pulledX) < creativeworldgen$MIN_REACH && Math.abs(pulledZ) < creativeworldgen$MIN_REACH) {
            creativeworldgen$TOO_SHORT.set(true);
            return start;
        }
        return start.offset(pulledX, dy, pulledZ);
    }

    @Inject(method = "m_142674_", cancellable = true, at = @At(value = "INVOKE", ordinal = 0,
            target = "Lbiomesoplenty/common/worldgen/feature/misc/FleshTendonFeature;quadratic(FLnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"))
    private void creativeworldgen$cancelTooShort(FeaturePlaceContext<NoneFeatureConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        if (creativeworldgen$TOO_SHORT.get()) cir.setReturnValue(false);
    }

    @Unique
    private static int creativeworldgen$along(int offset, int step, int steps) {
        return Math.round(offset * (float) step / steps);
    }

    /** Flesh balls reach one block sideways, so the write window has to hold the 3x3 around the column. */
    @Unique
    private boolean creativeworldgen$fits(WorldGenRegion region, int x, int y, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                if (!respectsCutoff(region, pos.set(x + ox, y, z + oz))) return false;
            }
        }
        return region.getBiome(pos.set(x, y, z)).is(creativeworldgen$HEAP);
    }
}
