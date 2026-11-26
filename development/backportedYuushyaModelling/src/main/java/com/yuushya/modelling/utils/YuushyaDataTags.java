// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 utils/YuushyaDataTags.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class YuushyaDataTags {
   public static final String TRANS_DIRECTION = "TransDirection";
   public static final String BLOCKSTATE = "BlockState";
   public static final String TRANSFORM_DATA = "TransformData";
   public static final String COLOR_DATA = "ColorData";
   public static final String TEXT_DATA = "TextData";
   public static final String LIT = "Lit";
   public static final String SHAPES = "Shapes";
   public static final String ENABLE_AO = "EnableAO";

   public static int getTransDirection(ItemStack itemStack) {
      return itemStack.getOrCreateTag().getInt("TransDirection");
   }

   public static void setTransDirection(ItemStack itemStack, int value) {
      itemStack.getOrCreateTag().putInt("TransDirection", value);
   }

   public static BlockState getBlockState(ItemStack itemStack) {
      CompoundTag tag = itemStack.getOrCreateTag();
      return tag.contains("BlockState")
         ? BlockState.CODEC.decode(NbtOps.INSTANCE, tag.get("BlockState")).result().<BlockState>map(Pair::getFirst).orElse(null)
         : null;
   }

   public static void setBlockState(ItemStack itemStack, BlockState blockState) {
      if (blockState == null) {
         itemStack.getOrCreateTag().remove("BlockState");
      } else {
         CompoundTag tag = itemStack.getOrCreateTag();
         BlockState.CODEC.encodeStart(NbtOps.INSTANCE, blockState).result().ifPresent(nbt -> tag.put("BlockState", nbt));
      }
   }

   public static CompoundTag getTransformData(ItemStack itemStack) {
      CompoundTag tag = itemStack.getOrCreateTag();
      if (tag.contains("TransformData")) {
         return tag.getCompound("TransformData");
      } else {
         return tag.contains("BlockEntityTag") ? tag.getCompound("BlockEntityTag") : new CompoundTag();
      }
   }

   public static void setTransformData(ItemStack itemStack, CompoundTag transformData) {
      itemStack.getOrCreateTag().put("TransformData", transformData);
   }

   public static int getColorData(ItemStack itemStack) {
      return itemStack.getOrCreateTag().getInt("ColorData");
   }

   public static void setColorData(ItemStack itemStack, int value) {
      itemStack.getOrCreateTag().putInt("ColorData", value);
   }

   public static boolean hasColorData(ItemStack itemStack) {
      return itemStack.getOrCreateTag().contains("ColorData");
   }

   public static int getLit(ItemStack itemStack) {
      return itemStack.getOrCreateTag().getInt("Lit");
   }

   public static void setLit(ItemStack itemStack, int value) {
      itemStack.getOrCreateTag().putInt("Lit", value);
   }

   public static String getShapes(ItemStack itemStack) {
      CompoundTag tag = itemStack.getOrCreateTag();
      return tag.contains("Shapes") ? tag.getString("Shapes") : null;
   }

   public static void setShapes(ItemStack itemStack, String shape) {
      itemStack.getOrCreateTag().putString("Shapes", shape);
   }

   public static boolean getEnableAO(ItemStack itemStack) {
      return itemStack.getOrCreateTag().getBoolean("EnableAO");
   }

   public static void setEnableAO(ItemStack itemStack, boolean value) {
      itemStack.getOrCreateTag().putBoolean("EnableAO", value);
   }

   public static CompoundTag getTextData(ItemStack itemStack) {
      CompoundTag tag = itemStack.getOrCreateTag();
      return tag.contains("TextData") ? tag.getCompound("TextData") : new CompoundTag();
   }

   public static void setTextData(ItemStack itemStack, CompoundTag textData) {
      itemStack.getOrCreateTag().put("TextData", textData);
   }
}
