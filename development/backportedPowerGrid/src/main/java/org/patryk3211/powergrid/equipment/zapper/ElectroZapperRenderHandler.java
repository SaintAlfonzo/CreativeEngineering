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
package org.patryk3211.powergrid.equipment.zapper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.equipment.zapper.ShootableGadgetRenderHandler;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.collections.ModdedItems;

// Port: no @OnlyIn on the class (upstream @Environment(CLIENT)): ElectroZapperS2CPacket.getHandler returns it, so the
// common packet class verifies against it on a dedicated server, as Create's PotatoCannonPacket does with its handler.
public class ElectroZapperRenderHandler extends ShootableGadgetRenderHandler {
    @Override
    protected void playSound(InteractionHand hand, Vec3 position) {
        ZapProjectileEntity.playLaunchSound(Minecraft.getInstance().level, position, 1);
    }

    @Override
    protected boolean appliesTo(ItemStack stack) {
        return ModdedItems.ELECTROZAPPER.get().isZapper(stack);
    }

    @Override
    protected void transformTool(PoseStack ms, float flip, float equipProgress, float recoil, float pt) {
        ms.translate(flip * -.1f, 0, .14f);
        ms.scale(.75f, .75f, .75f);
        ms.mulPose(Vector3f.XP.rotationDegrees(recoil * 80));
    }

    @Override
    protected void transformHand(PoseStack ms, float flip, float equipProgress, float recoil, float pt) {
        ms.translate(flip * -.09, -.275, -.25);
        ms.mulPose(Vector3f.ZP.rotationDegrees(flip * -10));
    }
}
