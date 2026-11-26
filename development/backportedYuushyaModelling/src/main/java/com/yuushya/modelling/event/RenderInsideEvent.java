// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 event/RenderInsideEvent.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.event;

import com.yuushya.modelling.blockentity.AbstractTransformBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber({Dist.CLIENT})
public class RenderInsideEvent {
   @SubscribeEvent
   public static void renderInside(RenderBlockScreenEffectEvent event) {
      Block block = event.getBlockState().getBlock();
      if (block instanceof AbstractTransformBlock) {
         event.setCanceled(true);
      }
   }
}
