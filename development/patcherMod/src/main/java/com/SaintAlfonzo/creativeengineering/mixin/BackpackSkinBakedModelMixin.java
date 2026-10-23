package com.SaintAlfonzo.creativeengineering.mixin;

import java.util.List;

import com.SaintAlfonzo.creativeengineering.backpackskin.BackpackSkinsClient;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Placed look of a skinned backpack. The block entity hands the skin over as model data; with it present the
 * baked model answers with the skin block's quads, render layers and particle, so no tank, battery or pouch is drawn.
 */
@Mixin(targets = "net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackDynamicModel$BackpackBakedModel", remap = false)
public abstract class BackpackSkinBakedModelMixin {

    @Inject(method = "getQuads", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void creativeengineering$skinQuads(BlockState state, Direction side, RandomSource rand, ModelData data,
            RenderType renderType, CallbackInfoReturnable<List<BakedQuad>> cir) {
        Block skin = data.get(BackpackSkinsClient.SKIN);
        if (skin != null) cir.setReturnValue(BackpackSkinsClient.quads(skin, state, side, rand, renderType));
    }

    @Inject(method = "getRenderTypes", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void creativeengineering$skinRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
            @NotNull ModelData data, CallbackInfoReturnable<ChunkRenderTypeSet> cir) {
        Block skin = data.get(BackpackSkinsClient.SKIN);
        if (skin != null) cir.setReturnValue(BackpackSkinsClient.renderTypes(skin, state, rand));
    }

    /** Overrides Forge's default {@code getParticleIcon(ModelData)}. */
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        Block skin = data.get(BackpackSkinsClient.SKIN);
        return skin != null ? BackpackSkinsClient.particle(skin) : ((net.minecraft.client.resources.model.BakedModel) this).getParticleIcon();
    }
}
