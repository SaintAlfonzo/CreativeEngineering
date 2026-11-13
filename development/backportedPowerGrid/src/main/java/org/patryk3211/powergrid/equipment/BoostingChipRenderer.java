// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
// Port (P9d): ItemDisplayContext -> ItemTransforms.TransformType; joml Axis -> com.mojang.math.Vector3f rotations.
package org.patryk3211.powergrid.equipment;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

public class BoostingChipRenderer extends CustomRenderedItemModelRenderer {
    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemTransforms.TransformType transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        LocalPlayer player = Minecraft.getInstance().player;
        boolean leftHand = transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
        boolean firstPerson = leftHand || transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND;
        ms.pushPose();

        if (firstPerson && player != null) {
            int itemInUseCount = player.getUseItemRemainingTicks();
            if (itemInUseCount > 0) {
                int modifier = leftHand ? -1 : 1;
                ms.translate(modifier * -0.4f, 0.0f, -0.25f);
                ms.mulPose(Vector3f.ZP.rotationDegrees((float)(modifier * 40)));
                ms.mulPose(Vector3f.XP.rotationDegrees((float)(modifier * 10)));
                ms.mulPose(Vector3f.YP.rotationDegrees((float)(modifier * 90)));
            }
        }

        itemRenderer.render(stack, ItemTransforms.TransformType.NONE, false, ms, buffer, light, overlay, model.getOriginalModel());
        ms.popPose();
    }
}
