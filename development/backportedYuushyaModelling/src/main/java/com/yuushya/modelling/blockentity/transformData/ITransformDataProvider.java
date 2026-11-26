// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/transformData/ITransformDataProvider.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.transformData;

import net.minecraft.nbt.CompoundTag;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;

public interface ITransformDataProvider {
   Vector3d getPosition();

   Vector3f getRotation();

   Vector3f getScale();

   boolean isShown();

   void setShown(boolean var1);

   void saveAdditional(CompoundTag var1);
}
