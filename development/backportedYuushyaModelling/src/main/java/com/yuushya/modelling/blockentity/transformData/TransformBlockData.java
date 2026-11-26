// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/transformData/TransformBlockData.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.transformData;

import com.yuushya.modelling.utils.YuushyaUtils;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;

public class TransformBlockData implements ITransformDataProvider {
   public Vector3d pos = new Vector3d(0.0, 0.0, 0.0);
   public Vector3f rot = new Vector3f(0.0F, 0.0F, 0.0F);
   public Vector3f scales = new Vector3f(1.0F, 1.0F, 1.0F);
   public BlockState blockState = Blocks.AIR.defaultBlockState();
   public boolean isShown = false;

   public TransformBlockData() {
   }

   public TransformBlockData(Vector3d pos, Vector3f rot, Vector3f scales, BlockState blockState, boolean isShown) {
      this();
      this.pos.set(pos);
      this.rot.set(rot.x(), rot.y(), rot.z());
      this.scales.set(scales.x(), scales.y(), scales.z());
      this.blockState = blockState;
      this.isShown = isShown;
   }

   public void set(Vector3d pos, Vector3f rot, Vector3f scales, BlockState blockState, boolean isShown) {
      this.pos.set(pos);
      this.rot.set(rot.x(), rot.y(), rot.z());
      this.scales.set(scales.x(), scales.y(), scales.z());
      this.blockState = blockState;
      this.isShown = isShown;
   }

   public void set(TransformBlockData old) {
      this.set(old.pos, old.rot, old.scales, old.blockState, old.isShown);
   }

   public void set() {
      this.pos.set(0.0, 0.0, 0.0);
      this.rot.set(0.0F, 0.0F, 0.0F);
      this.scales.set(1.0F, 1.0F, 1.0F);
      this.blockState = Blocks.AIR.defaultBlockState();
      this.isShown = false;
   }

   public void load(CompoundTag compoundTag) {
      ListTag listTagPos = compoundTag.getList("ShowPos", 6);
      ListTag listTagRot = compoundTag.getList("ShowRotation", 5);
      ListTag listTagScales = compoundTag.getList("ShowScales", 5);
      this.pos.set(listTagPos.getDouble(0), listTagPos.getDouble(1), listTagPos.getDouble(2));
      this.rot.set(listTagRot.getFloat(0), listTagRot.getFloat(1), listTagRot.getFloat(2));
      this.scales.set(listTagScales.getFloat(0), listTagScales.getFloat(1), listTagScales.getFloat(2));
      this.isShown = compoundTag.getBoolean("isShown");
      this.blockState = YuushyaUtils.readBlockState(compoundTag.getCompound("BlockState"));
   }

   @Override
   public void saveAdditional(CompoundTag compoundTag) {
      compoundTag.put("ShowPos", YuushyaUtils.toListTag(this.pos.x, this.pos.y, this.pos.z));
      compoundTag.put("ShowRotation", YuushyaUtils.toListTag(this.rot.x(), this.rot.y(), this.rot.z()));
      compoundTag.put("ShowScales", YuushyaUtils.toListTag(this.scales.x(), this.scales.y(), this.scales.z()));
      compoundTag.put("BlockState", NbtUtils.writeBlockState(this.blockState));
      compoundTag.put("isShown", ByteTag.valueOf(this.isShown));
   }

   @Override
   public Vector3d getPosition() {
      return this.pos;
   }

   @Override
   public Vector3f getRotation() {
      return this.rot;
   }

   @Override
   public Vector3f getScale() {
      return this.scales;
   }

   @Override
   public boolean isShown() {
      return this.isShown;
   }

   @Override
   public void setShown(boolean shown) {
      this.isShown = shown;
   }
}
