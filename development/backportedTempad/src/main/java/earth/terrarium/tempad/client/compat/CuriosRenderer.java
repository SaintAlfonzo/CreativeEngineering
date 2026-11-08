// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/compat/CuriosRenderer.kt. Translated to Java for Forge 1.19.2
// against Curios 5.1: Axis -> Vector3f rotations, ItemDisplayContext.FIXED -> TransformType.FIXED. Upstream's
// PlayerModelAccessor mixin (reads PlayerModel.slim) is replaced by the player's skin model name, which is what picks the
// slim player renderer. Loaded only when Curios is (TempadClientExtras checks ModList first), TP-D8.
package earth.terrarium.tempad.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public final class CuriosRenderer implements ICurioRenderer {
    public static final CuriosRenderer INSTANCE = new CuriosRenderer();

    private CuriosRenderer() {
    }

    /** Upstream {@code initCuriosCompat()} (client). */
    public static void init() {
        CuriosRendererRegistry.register(ModItems.tempad.get(), () -> INSTANCE);
        CuriosRendererRegistry.register(ModItems.cardWallet.get(), () -> INSTANCE);

        CuriosRendererRegistry.register(ModItems.chrononBattery.get(), () -> INSTANCE);
        CuriosRendererRegistry.register(ModItems.chrononCell.get(), () -> INSTANCE);

        CuriosRendererRegistry.register(ModItems.chronometer.get(), () -> INSTANCE);
        CuriosRendererRegistry.register(ModItems.chrononGenerator.get(), () -> INSTANCE);
        CuriosRendererRegistry.register(ModItems.creativeChronometer.get(), () -> INSTANCE);

        CuriosRendererRegistry.register(ModItems.screeningDevice.get(), () -> INSTANCE);
        CuriosRendererRegistry.register(ModItems.locationBroadcaster.get(), () -> INSTANCE);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack,
                                                                          RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer,
                                                                          int light, float limbSwing, float limbSwingAmount, float partialTicks,
                                                                          float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(renderLayerParent instanceof PlayerRenderer)) return;
        LivingEntity entity = slotContext.entity();
        Object renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        if (!(renderer instanceof PlayerRenderer render)) return;
        PlayerModel<AbstractClientPlayer> model = render.getModel();
        matrixStack.pushPose();
        if (!slotContext.identifier().equals("bracelet")) {
            model.body.translateAndRotate(matrixStack);
            matrixStack.scale(0.35f, 0.35f, 0.35f);
        }
        switch (slotContext.identifier()) {
            case "belt" -> {
                if (slotContext.index() != 0) {
                    matrixStack.popPose();
                    return;
                }
                double offset = !entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty() ? 0.1 : 0.0;
                matrixStack.translate(-0.75 - offset, 2.0, 0.0);
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180f));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(90f));
            }
            case "charm" -> {
                if (slotContext.index() > 3) {
                    matrixStack.popPose();
                    return;
                }
                double zOffset;
                if (!entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
                    zOffset = 0.35;
                } else if (!entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty()) {
                    zOffset = 0.15;
                } else {
                    zOffset = 0.0;
                }
                matrixStack.scale(0.5f, 0.5f, 0.5f);
                matrixStack.translate(1 - slotContext.index() * 1.0, 3.65, 0.75 + zOffset);
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180f));
            }
            case "bracelet" -> {
                if (slotContext.index() != 0) {
                    matrixStack.popPose();
                    return;
                }
                boolean slim = entity instanceof AbstractClientPlayer player && "slim".equals(player.getModelName());
                double armOffset = slim ? 0.0 : 0.15;
                if (entity.getMainArm() == HumanoidArm.RIGHT) {
                    model.leftArm.translateAndRotate(matrixStack);
                    matrixStack.mulPose(Vector3f.XP.rotationDegrees(90f));
                    matrixStack.mulPose(Vector3f.YP.rotationDegrees(90f));
                    matrixStack.scale(0.4f, 0.4f, 0.4f);
                    matrixStack.translate(1.0, 0.0, 0.35 + armOffset);
                } else {
                    model.rightArm.translateAndRotate(matrixStack);
                    matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90f));
                    matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90f));
                    matrixStack.scale(0.4f, 0.4f, 0.4f);
                    matrixStack.translate(1.0, 0.0, 0.35 + armOffset);
                }
            }
            default -> {
            }
        }

        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED, light, OverlayTexture.NO_OVERLAY,
                matrixStack, renderTypeBuffer, 0);
        matrixStack.popPose();
    }
}
