/*
 * Copyright 2025 patryk3211
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
package org.patryk3211.powergrid.equipment.drill;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

public class DrillItemRenderer extends CustomRenderedItemModelRenderer {
    // Port: the head partial model is ModdedPartialModels.DRILL_HEAD (Flywheel 0.6 registers partials at init)

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemTransforms.TransformType transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        renderer.render(model.getOriginalModel(), light);
        LocalPlayer player = mc.player;

        float offset = -3f / 16;
        float angle = 0;
        if(player instanceof PlayerDrillExtensions ext) {
            angle = -ext.powerGrid$animation(AnimationTickHolder.getPartialTicks());
        }

        ms.pushPose();
        ms.translate(0, offset, 0);
        ms.mulPose(Vector3f.ZP.rotationDegrees(angle));
        ms.translate(0, -offset, -0.5f / 16f);
        renderer.render(org.patryk3211.powergrid.collections.ModdedPartialModels.DRILL_HEAD.get(), light);
        ms.popPose();
    }

    public static boolean renderPlayerHand(ItemStack heldItem, InteractionHand hand, PoseStack ms, MultiBufferSource buffer, int light, float pt, float swing, float equip) {
        if(heldItem.getItem() instanceof DrillItem) {
            Minecraft mc = Minecraft.getInstance();
            ItemInHandRenderer firstPersonRenderer = mc.getEntityRenderDispatcher().getItemInHandRenderer();
            boolean rightHand = hand == InteractionHand.MAIN_HAND ^ mc.player.getMainArm() == HumanoidArm.LEFT;

            float flip = rightHand ? 1.0F : -1.0F;
            ms.pushPose();
            ms.translate(flip * (0.64f - 0.1f), -0.6f + equip * -0.6f, -0.72f - 0.1f);

            var rand = mc.level.random;
            ms.mulPose(Vector3f.YP.rotationDegrees(flip * (rand.nextFloat() * 2 - 1) * 5.0f * swing));
            ms.mulPose(Vector3f.ZP.rotationDegrees(flip * (rand.nextFloat() * 2 - 1) * -2.0f * swing));
            firstPersonRenderer.renderItem(mc.player, heldItem, rightHand ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !rightHand, ms, buffer, light);
            ms.popPose();
            return true;
        }
        return false;
    }
}
