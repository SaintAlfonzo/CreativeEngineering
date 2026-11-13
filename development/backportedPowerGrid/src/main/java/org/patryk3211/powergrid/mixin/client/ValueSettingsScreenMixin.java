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
package org.patryk3211.powergrid.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsScreen;
import org.patryk3211.powergrid.utility.GuiGraphics;
import org.patryk3211.powergrid.electricity.transformer.TransformerWindingScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Port: Create 0.5.1 draws renderWindow on a PoseStack (Create 6: GuiGraphics); its row label is Font.draw (the third
// one in the method, as Create 6's third drawString) and the plain milestone is the second AllGuiTextures.render. Create
// members are not remapped (remap = false); the vanilla Font target is (remap = true). Local names x, y, milestoneX,
// milestone exist in the 0.5.1.i local variable table.
@Mixin(value = ValueSettingsScreen.class)
public abstract class ValueSettingsScreenMixin {
    @Shadow(remap = false) private int maxLabelWidth;
    @Shadow(remap = false) private int valueBarWidth;
    @Shadow(remap = false) private ValueSettingsBoard board;

    @Inject(method = "renderWindow(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", remap = false, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;draw(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/network/chat/Component;FFI)I",
            ordinal = 2,
            remap = true))
    private void powerGrid$renderBar(PoseStack ms, int mouseX, int mouseY, float partialTicks, CallbackInfo ci, @Local(name = "x") int x, @Local(name = "y") int y) {
        if(!((Object) this instanceof TransformerWindingScreen screen))
            return;
        int valueBarX = x + maxLabelWidth + 14 + 4;
        screen.renderBarCap(new GuiGraphics(ms), valueBarX, y, valueBarWidth, board);
    }

    @Inject(method = "renderWindow(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", remap = false, at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/foundation/gui/AllGuiTextures;render(Lcom/mojang/blaze3d/vertex/PoseStack;II)V",
            ordinal = 1,
            shift = At.Shift.AFTER))
    private void powerGrid$renderMilestone(PoseStack ms, int mouseX, int mouseY, float partialTicks, CallbackInfo ci, @Local(name = "milestoneX") int x, @Local(name = "y") int y, @Local(name = "milestone") int milestone) {
        if(!((Object) this instanceof TransformerWindingScreen screen))
            return;
        screen.renderBarCapMilestone(new GuiGraphics(ms), x, y, milestone, board);
    }
}
