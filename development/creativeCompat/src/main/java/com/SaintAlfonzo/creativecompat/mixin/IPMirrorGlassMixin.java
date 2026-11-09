package com.SaintAlfonzo.creativecompat.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.portal.BreakableMirror;

/**
 * More glass makes Immersive Portals mirrors (feature_writeup/plan_portal_changes.md 4, M4). IP 2.3.7 accepts only
 * GlassBlock, StainedGlassBlock and IronBarsBlock subclasses, so tinted glass (AbstractGlassBlock) and most modded glass
 * failed. Blocks in {@code creativecompat:mirror_glass} now count as glass and blocks in
 * {@code creativecompat:mirror_glass_panes} as glass panes (filled in kubejs/data/creativecompat/kubejs/tags/). Both
 * methods are used by flint and steel, the wall search in {@code createMirror} and the wall check every tick.
 * Frosty glass: Immersive Weathering melts it in the block's use, which vanilla skips while sneaking with an item, so
 * sneak + flint and steel reaches IP's mirror check and plain use still melts the frost.
 */
@Mixin(value = BreakableMirror.class, remap = false)
public abstract class IPMirrorGlassMixin {

    @Unique
    private static final TagKey<Block> creativecompat$GLASS =
            TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("creativecompat", "mirror_glass"));
    @Unique
    private static final TagKey<Block> creativecompat$PANES =
            TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("creativecompat", "mirror_glass_panes"));
    /** Never mirrors: camouflage blocks that Immersive Portals' own class check would accept (Framed Blocks' panes). */
    @Unique
    private static final TagKey<Block> creativecompat$NOT_GLASS =
            TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("creativecompat", "not_mirror_glass"));

    @Inject(method = "isGlass", at = @At("RETURN"), cancellable = true, remap = false, require = 1)
    private static void creativecompat$tagGlass(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(pos);
        if (state.is(creativecompat$NOT_GLASS)) {
            cir.setReturnValue(false);
            return;
        }
        if (cir.getReturnValueZ()) return;
        if (state.is(creativecompat$GLASS) || state.is(creativecompat$PANES)) cir.setReturnValue(true);
    }

    @Inject(method = "isGlassPane", at = @At("RETURN"), cancellable = true, remap = false, require = 1)
    private static void creativecompat$tagPane(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(pos);
        if (state.is(creativecompat$NOT_GLASS)) {
            cir.setReturnValue(false);
            return;
        }
        if (!cir.getReturnValueZ() && state.is(creativecompat$PANES)) cir.setReturnValue(true);
    }
}
