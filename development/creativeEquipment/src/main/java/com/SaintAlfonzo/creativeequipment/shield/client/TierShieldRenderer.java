package com.SaintAlfonzo.creativeequipment.shield.client;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;

import com.SaintAlfonzo.creativeequipment.shield.ShieldTier;

import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Holder;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Draws a tier shield exactly like vanilla's own {@code BlockEntityWithoutLevelRenderer} draws
 * {@code Items.SHIELD} (same {@code ShieldModel}, same banner-pattern overlay via
 * {@code BannerRenderer.renderPatterns}, same foil handling) but with this tier's own base/
 * no-pattern sprite ({@link TierShieldMaterials}) swapped in for vanilla's
 * {@code ModelBakery.SHIELD_BASE} / {@code NO_PATTERN_SHIELD}. The sequence below is copied from
 * {@code BlockEntityWithoutLevelRenderer#renderByItem}'s shield branch (decompiled from the
 * 1.19.2 mappings to get the exact call order/arguments), not reverse-engineered by guesswork.
 */
@OnlyIn(Dist.CLIENT)
public final class TierShieldRenderer extends BlockEntityWithoutLevelRenderer {

    private final EntityModelSet models;
    private ShieldModel shieldModel;

    public TierShieldRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
        this.models = models;
    }

    // No onResourceManagerReload override: this instance is never registered as a reload
    // listener, and shieldModel only wraps a baked ModelPart (no textures of its own to go stale).
    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ShieldTier tier = ShieldTier.of(stack);
        TierShieldMaterials.Pair materials = tier == null ? null : TierShieldMaterials.of(tier);
        if (materials == null) return; // not a tier shield (or IRON, which never reaches this renderer)

        if (shieldModel == null) {
            shieldModel = new ShieldModel(models.bakeLayer(ModelLayers.SHIELD));
        }

        boolean hasPatterns = BlockItem.getBlockEntityData(stack) != null;
        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);

        Material material = hasPatterns ? materials.base() : materials.noPattern();
        TextureAtlasSprite sprite = material.sprite();
        RenderType renderType = shieldModel.renderType(material.atlasLocation());
        VertexConsumer vertexConsumer = sprite.wrap(
                ItemRenderer.getFoilBufferDirect(buffer, renderType, true, stack.hasFoil()));
        shieldModel.handle().render(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        if (hasPatterns) {
            DyeColor color = ShieldItem.getColor(stack);
            ListTag patternsNbt = BannerBlockEntity.getItemPatterns(stack);
            List<Pair<Holder<BannerPattern>, DyeColor>> patterns = BannerBlockEntity.createPatterns(color, patternsNbt);
            BannerRenderer.renderPatterns(poseStack, buffer, packedLight, packedOverlay,
                    shieldModel.plate(), material, false, patterns, stack.hasFoil());
        } else {
            shieldModel.plate().render(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        poseStack.popPose();
    }
}
