// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/transformData/TransformItemData.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.transformData;

import com.yuushya.modelling.utils.YuushyaUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;

public class TransformItemData implements ITransformDataProvider {
   public Vector3d pos = new Vector3d(0.0, 0.0, 0.0);
   public Vector3f rot = new Vector3f(0.0F, 0.0F, 0.0F);
   public Vector3f scales = new Vector3f(1.0F, 1.0F, 1.0F);
   public int color = ARGB32.color(255, 255, 255, 255);
   public ItemStack itemStack = Items.AIR.getDefaultInstance();
   public boolean isShown = false;
   public boolean enableBlock;

   public TransformItemData() {
   }

   public TransformItemData(Vector3d pos, Vector3f rot, Vector3f scales, ItemStack ItemStack, int color, boolean isShown, boolean enableBlock) {
      this();
      this.pos.set(pos);
      this.rot.set(rot.x(), rot.y(), rot.z());
      this.scales.set(scales.x(), scales.y(), scales.z());
      this.itemStack = ItemStack;
      this.color = color;
      this.isShown = isShown;
      this.enableBlock = enableBlock;
   }

   public void set(Vector3d pos, Vector3f rot, Vector3f scales, ItemStack ItemStack, int color, boolean isShown, boolean enableBlock) {
      this.pos.set(pos);
      this.rot.set(rot.x(), rot.y(), rot.z());
      this.scales.set(scales.x(), scales.y(), scales.z());
      this.itemStack = ItemStack;
      this.color = color;
      this.isShown = isShown;
      this.enableBlock = enableBlock;
   }

   public void set(TransformItemData old) {
      this.set(old.pos, old.rot, old.scales, old.itemStack, old.color, old.isShown, old.enableBlock);
   }

   public void set() {
      this.pos.set(0.0, 0.0, 0.0);
      this.rot.set(0.0F, 0.0F, 0.0F);
      this.scales.set(1.0F, 1.0F, 1.0F);
      this.itemStack = Items.AIR.getDefaultInstance();
      this.isShown = false;
      this.enableBlock = false;
   }

   public void load(CompoundTag compoundTag) {
      ListTag listTagPos = compoundTag.getList("ShowPos", 6);
      ListTag listTagRot = compoundTag.getList("ShowRotation", 6);
      ListTag listTagScales = compoundTag.getList("ShowScales", 6);
      this.pos.set(listTagPos.getDouble(0), listTagPos.getDouble(1), listTagPos.getDouble(2));
      this.rot.set((float)listTagRot.getDouble(0), (float)listTagRot.getDouble(1), (float)listTagRot.getDouble(2));
      this.scales.set((float)listTagScales.getDouble(0), (float)listTagScales.getDouble(1), (float)listTagScales.getDouble(2));
      this.itemStack = ItemStack.of(compoundTag.getCompound("ItemStack"));
      this.color = compoundTag.getInt("Color");
      this.isShown = compoundTag.getBoolean("isShown");
      this.enableBlock = compoundTag.getBoolean("enableBlock");
   }

   @Override
   public void saveAdditional(CompoundTag compoundTag) {
      compoundTag.put("ShowPos", YuushyaUtils.toListTag(this.pos.x, this.pos.y, this.pos.z));
      compoundTag.put("ShowRotation", YuushyaUtils.toDoubleListTag(this.rot));
      compoundTag.put("ShowScales", YuushyaUtils.toDoubleListTag(this.scales));
      compoundTag.put("ItemStack", YuushyaUtils.itemStackTag(this.itemStack));
      compoundTag.putInt("Color", this.color);
      compoundTag.putBoolean("isShown", this.isShown);
      compoundTag.putBoolean("enableBlock", this.enableBlock);
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
