package com.SaintAlfonzo.creative_labels.client;

import com.SaintAlfonzo.creative_labels.CLConfig;
import com.SaintAlfonzo.creative_labels.CreativeLabels;
import com.SaintAlfonzo.creative_labels.block.FaceContent;
import com.SaintAlfonzo.creative_labels.block.FacePaperBlockEntity;
import com.SaintAlfonzo.creative_labels.block.LabelData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.FrameBufferBackedDynamicTexture;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Draws label pictures and names (sticky_notes.md 3.11, BRIEF 4 "Label icons"). The paper itself and every note
 * are in the chunk mesh ({@link FacePaperModel}); this renderer only adds, per LABEL face with a picture, one quad
 * textured with the picture and, with the feather on, the item's name under it. NOTES faces draw nothing here.
 *
 * <p>Pictures are Moonlight render-to-texture textures: an undyed label uses
 * {@link RenderedTexturesManager#requestFlatItemStackTexture} (the stack is rendered as in a GUI, so NBT tints and
 * the glint show; Moonlight redraws it each tick); a dyed one uses {@link RenderedTexturesManager#requestTexture}
 * with a one-off draw, download, {@link IconDye#apply} and upload. Texture ids are
 * {@code creative_labels:labels/<item>/<dye>/<tag hash>}, so the cache key is the stripped stack plus the dye (C3);
 * Moonlight frees them two minutes after their last use and on resource reload.
 *
 * <p>Distances: {@link #shouldRender} and {@link #getViewDistance} use {@code pictureDistance}; names need the
 * camera within {@code textDistance}. Glow = full-bright light on the quad and the text.
 */
public class FacePaperRenderer implements BlockEntityRenderer<FacePaperBlockEntity> {

    public static final int TEXTURE_SIZE = 16;
    public static final String TEXTURE_PREFIX = "labels/";
    public static final int TEXT_COLOUR = 0xFF1D1D21;
    private static final int ID_CACHE_LIMIT = 1024;

    private final Font font;
    private final Map<TextureKey, ResourceLocation> ids = new HashMap<>();

    public FacePaperRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    public static double pictureDistance() {
        return CLConfig.num(CLConfig.PICTURE_DISTANCE);
    }

    public static double textDistance() {
        return CLConfig.num(CLConfig.TEXT_DISTANCE);
    }

    @Override
    public int getViewDistance() {
        return (int) Math.ceil(pictureDistance());
    }

    @Override
    public boolean shouldRender(FacePaperBlockEntity be, Vec3 cameraPos) {
        double distance = pictureDistance();
        return distance > 0 && Vec3.atCenterOf(be.getBlockPos()).closerThan(cameraPos, distance);
    }

    @Override
    public boolean shouldRenderOffScreen(FacePaperBlockEntity be) {
        return false;
    }

    @Override
    public void render(FacePaperBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        FaceContent[] faces = be.faces();
        boolean anyName = false;
        for (FaceContent c : faces) {
            if (c.kind == FaceContent.Kind.LABEL && c.label != null && c.label.showName && c.label.hasPicture()) anyName = true;
        }
        boolean nameNear = anyName && Vec3.atCenterOf(be.getBlockPos())
                .closerThan(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), textDistance());
        for (Direction face : Direction.values()) {
            FaceContent c = faces[face.ordinal()];
            if (c.kind != FaceContent.Kind.LABEL || c.label == null) continue;
            LabelData label = c.label;
            if (!label.hasPicture()) continue;
            FrameBufferBackedDynamicTexture tex = texture(label);
            if (!tex.isInitialized()) continue; // drawn on the render thread shortly; nothing to show yet
            int packedLight = label.glow ? LightTexture.FULL_BRIGHT : light;
            drawIcon(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(tex.getTextureLocation())),
                    face, c.rotation, c.inset, label.showName, packedLight, overlay);
            if (label.showName && nameNear) {
                drawName(poseStack, buffer, face, c.rotation, c.inset, label.picture.getHoverName(), packedLight);
            }
        }
    }

    /** The picture texture for a label, requested from Moonlight (created once per id, then cached there). */
    private FrameBufferBackedDynamicTexture texture(LabelData label) {
        ResourceLocation id = textureId(label.picture, label.dye);
        ItemStack stack = label.picture;
        DyeColor dye = label.dye;
        if (dye == null) {
            return RenderedTexturesManager.requestFlatItemStackTexture(id, stack, TEXTURE_SIZE);
        }
        return RenderedTexturesManager.requestTexture(id, TEXTURE_SIZE, t -> {
            RenderedTexturesManager.drawItem(t, stack);
            t.download();
            IconDye.apply(t.getPixels(), dye);
            t.upload();
        }, false);
    }

    /** {@code creative_labels:labels/<namespace>/<path>/<dye|plain>/<tag hash>} for a stripped stack and a dye (C3). */
    public ResourceLocation textureId(ItemStack stripped, @Nullable DyeColor dye) {
        CompoundTag tag = stripped.getTag();
        TextureKey key = new TextureKey(stripped.getItem(), tag == null ? null : tag.copy(), dye);
        ResourceLocation id = ids.get(key);
        if (id == null) {
            if (ids.size() >= ID_CACHE_LIMIT) ids.clear();
            id = buildTextureId(stripped, dye);
            ids.put(key, id);
        }
        return id;
    }

    public static ResourceLocation buildTextureId(ItemStack stripped, @Nullable DyeColor dye) {
        ResourceLocation item = ForgeRegistries.ITEMS.getKey(stripped.getItem());
        String itemPart = item == null ? "unknown/unknown" : item.getNamespace() + "/" + item.getPath();
        CompoundTag tag = stripped.getTag();
        String hash = tag == null ? "0" : Integer.toHexString(tag.hashCode());
        return new ResourceLocation(CreativeLabels.ID,
                TEXTURE_PREFIX + itemPart + "/" + (dye == null ? "plain" : dye.getName()) + "/" + hash);
    }

    /** One quad with the picture, in front of the label paper, counter-clockwise toward the viewer. */
    static void drawIcon(PoseStack poseStack, VertexConsumer consumer, Direction face, @Nullable Direction rotation, float inset,
                         boolean showName, int light, int overlay) {
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        Vec3 n = LabelLayout.toward(face);
        Vec3[] p = LabelLayout.iconCorners(face, rotation, inset, showName);
        float[] u = {0f, 1f, 1f, 0f}, v = {0f, 0f, 1f, 1f}; // bottom-left, bottom-right, top-right, top-left
        for (int i = 0; i < 4; i++) {
            consumer.vertex(pose, (float) p[i].x, (float) p[i].y, (float) p[i].z)
                    .color(255, 255, 255, 255)
                    .uv(u[i], v[i])
                    .overlayCoords(overlay)
                    .uv2(light)
                    .normal(normal, (float) n.x, (float) n.y, (float) n.z)
                    .endVertex();
        }
    }

    /** The item's name under the picture, scaled into the text box, centred. */
    private void drawName(PoseStack poseStack, MultiBufferSource buffer, Direction face, @Nullable Direction rotation, float inset,
                          Component name, int light) {
        int width = font.width(name);
        if (width <= 0) return;
        float scale = LabelLayout.textScale(width) / 16f; // blocks per font unit
        Vec3 anchor = LabelLayout.textAnchor(face, rotation, inset);
        poseStack.pushPose();
        poseStack.translate(anchor.x, anchor.y, anchor.z);
        poseStack.mulPose(LabelLayout.textRotation(face, rotation));
        poseStack.scale(scale, scale, scale);
        font.drawInBatch(name, -width / 2f, 0f, TEXT_COLOUR, false, poseStack.last().pose(), buffer, false, 0, light);
        poseStack.popPose();
    }

    /** Cache key for texture ids: the stripped stack's item and tag plus the dye (C3). */
    private record TextureKey(Item item, @Nullable CompoundTag tag, @Nullable DyeColor dye) {}
}
