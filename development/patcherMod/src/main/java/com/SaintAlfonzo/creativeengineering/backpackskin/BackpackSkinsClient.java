package com.SaintAlfonzo.creativeengineering.backpackskin;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

/** Client drawing for backpack skins: quads for the placed block, the worn block. Used by the mixins in mixin/BackpackSkin*. */
public final class BackpackSkinsClient {

    /** Carries the skin block from the backpack block entity to the baked model. */
    public static final ModelProperty<Block> SKIN = new ModelProperty<>();

    /** Worn skin: size relative to the block, and where its centre sits in SB's back-pack model space (y points down). Tune in game. */
    private static final float WORN_SCALE = 0.6F;
    private static final double WORN_CENTRE_Y = 0.4;
    private static final double WORN_CENTRE_Z = 0.0;

    private BackpackSkinsClient() {}

    public static ModelData modelData(@Nullable Block skin) {
        return skin == null ? ModelData.EMPTY : ModelData.builder().with(SKIN, skin).build();
    }

    private static BakedModel model(BlockState skinState) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(skinState);
    }

    public static List<BakedQuad> quads(Block skin, @Nullable BlockState backpackState, @Nullable Direction side,
            RandomSource rand, @Nullable RenderType renderType) {
        BlockState ss = BackpackSkins.skinState(skin, backpackState);
        return model(ss).getQuads(ss, side, rand, ModelData.EMPTY, renderType);
    }

    public static ChunkRenderTypeSet renderTypes(Block skin, BlockState backpackState, RandomSource rand) {
        BlockState ss = BackpackSkins.skinState(skin, backpackState);
        return model(ss).getRenderTypes(ss, rand, ModelData.EMPTY);
    }

    public static TextureAtlasSprite particle(Block skin) {
        return model(skin.defaultBlockState()).getParticleIcon(ModelData.EMPTY);
    }

    /** Draws the skin block in place of the backpack model; the pose is already at SB's worn position. */
    public static void renderWorn(Block skin, PoseStack ps, MultiBufferSource buffer, int light) {
        ps.pushPose();
        ps.translate(0.0, WORN_CENTRE_Y, WORN_CENTRE_Z);
        ps.scale(WORN_SCALE, WORN_SCALE, WORN_SCALE);
        ps.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        ps.translate(-0.5, -0.5, -0.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                BackpackSkins.skinState(skin, null), ps, buffer, light, OverlayTexture.NO_OVERLAY);
        ps.popPose();
    }
}
