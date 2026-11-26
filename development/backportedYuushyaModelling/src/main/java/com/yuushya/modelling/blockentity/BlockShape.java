// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/BlockShape.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public enum BlockShape implements StringRepresentable {
   NONE(Shapes.empty()),
   FENCE(Shapes.create(0.4375, 0.0, 0.4375, 0.5625, 1.5, 0.5625)),
   BOTTOM_HALF(Shapes.create(0.0, 0.0, 0.0, 1.0, 0.5, 1.0)),
   TOP_HALF(Shapes.create(0.0, 0.5, 0.0, 1.0, 1.0, 1.0)),
   BLOCK(Shapes.block());

   public final VoxelShape voxelShape;

   private BlockShape(VoxelShape voxelShape) {
      this.voxelShape = voxelShape;
   }

   @NotNull
   public String getSerializedName() {
      return this.name().toLowerCase();
   }

   public Component getSymbol() {
      return Component.translatable("gui.showBlockScreen.shape." + this.name().toLowerCase());
   }
}
