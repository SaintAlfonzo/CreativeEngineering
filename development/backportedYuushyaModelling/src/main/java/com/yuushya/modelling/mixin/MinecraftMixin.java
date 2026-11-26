// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 mixin/MinecraftMixin.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2
// (same target: private Minecraft.updateLevelInEngines(ClientLevel)). Deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.mixin;

import com.yuushya.modelling.client.anvilcraft.rendering.CacheableBERenderingPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
   @Inject(method = "updateLevelInEngines", at = @At("HEAD"))
   private void yuushya$updateLevel(ClientLevel level, CallbackInfo ci) {
      CacheableBERenderingPipeline.updateLevel(level);
   }
}
