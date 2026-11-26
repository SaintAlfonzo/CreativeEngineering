// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 utils/YuushyaUtils.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.utils;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yuushya.modelling.blockentity.showblock.ShowBlock;
import com.yuushya.modelling.blockentity.showblock.ShowBlockEntity;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;

public class YuushyaUtils {
   public static final Function<Entry<Property<?>, Comparable<?>>, String> PROPERTY_ENTRY_TO_STRING_FUNCTION = new Function<Entry<Property<?>, Comparable<?>>, String>() {
      public String apply(@Nullable Entry<Property<?>, Comparable<?>> propertyValueMap) {
         if (propertyValueMap == null) {
            return "<NULL>";
         } else {
            Property<?> property = propertyValueMap.getKey();
            return property.getName() + "=" + this.getName(property, propertyValueMap.getValue());
         }
      }

      private <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> value) {
         return property.getName((T)value);
      }
   };

   public static int vertexSize() {
      return DefaultVertexFormat.BLOCK.getVertexSize() / 4;
   }

   public static void scale(PoseStack arg, Vector3f scales) {
      if (scales.x() != 1.0F || scales.y() != 1.0F || scales.z() != 1.0F) {
         arg.translate(0.5, 0.5, 0.5);
         arg.scale(scales.x(), scales.y(), scales.z());
         arg.translate(-0.5, -0.5, -0.5);
      }
   }

   public static void translateAfterScale(PoseStack arg, Vector3d pos, Vector3f scales) {
      if (pos.x != 0.0 || pos.y != 0.0 || pos.z != 0.0) {
         arg.translate(pos.x * (double)scales.x() / 16.0, pos.y * (double)scales.y() / 16.0, pos.z * (double)scales.z() / 16.0);
      }
   }

   public static void translate(PoseStack arg, Vector3d pos) {
      if (pos.x != 0.0 || pos.y != 0.0 || pos.z != 0.0) {
         arg.translate(pos.x / 16.0, pos.y / 16.0, pos.z / 16.0);
      }
   }

   public static void rotate(PoseStack arg, Vector3f rot) {
      float roll = rot.z();
      float yaw = rot.y();
      float pitch = rot.x();
      if (roll != 0.0F || yaw != 0.0F || pitch != 0.0F) {
         arg.translate(0.5, 0.5, 0.5);
         if (roll != 0.0F) {
            arg.mulPose(Vector3f.ZP.rotationDegrees(roll));
         }

         if (yaw != 0.0F) {
            arg.mulPose(Vector3f.YP.rotationDegrees(yaw));
         }

         if (pitch != 0.0F) {
            arg.mulPose(Vector3f.XP.rotationDegrees(pitch));
         }

         arg.translate(-0.5, -0.5, -0.5);
      }
   }

   // 1.19.2: com.mojang.math.Quaternion has no public fields (JOML Quaternionf x/y/z/w = i/j/k/r here).
   public static void mirror(Vector3d pos, Quaternion rot, YuushyaUtils.MirrorFace face) {
      switch (face) {
         case X:
            pos.x = -pos.x;
            rot.set(-rot.i(), rot.j(), rot.k(), -rot.r());
            break;
         case Y:
            pos.y = -pos.y;
            rot.set(rot.i(), -rot.j(), rot.k(), -rot.r());
            break;
         case Z:
            pos.z = -pos.z;
            rot.set(rot.i(), rot.j(), -rot.k(), -rot.r());
      }
   }

   // 1.19.2 stand-in for JOML Quaternionf.getEulerAnglesXYZ (JOML 1.10, radians; the inverse of Quaternion.fromXYZ,
   // which matches JOML's rotateXYZ). Mojang's own Quaternion.toXYZ uses a different (and wrong) formula.
   public static Vector3f eulerAnglesXYZ(Quaternion q) {
      float x = q.i();
      float y = q.j();
      float z = q.k();
      float w = q.r();
      float ex = (float)Math.atan2((double)(x * w - y * z), (double)(0.5F - x * x - y * y));
      float sy = 2.0F * (x * z + y * w);
      float ey = (float)(sy <= -1.0F ? -Math.PI / 2 : (sy >= 1.0F ? Math.PI / 2 : Math.asin((double)sy)));
      float ez = (float)Math.atan2((double)(z * w - x * y), (double)(0.5F - y * y - z * z));
      return new Vector3f(ex, ey, ez);
   }

   public static double normalizeAngle(double degrees) {
      degrees %= 360.0;
      if (degrees < 0.0) {
         degrees += 360.0;
      }

      return degrees;
   }

   public static int encodeTintWithState(int tint, BlockState state) {
      return Block.getId(state) << 8 | tint;
   }

   public static BlockState getBlockState(BlockState blockState, LevelAccessor world, BlockPos blockPos) {
      if (blockState.getBlock() instanceof ShowBlock) {
         ShowBlockEntity blockEntity = (ShowBlockEntity)world.getBlockEntity(blockPos);
         return blockEntity.getTransFormDataNow().blockState;
      } else {
         return blockState;
      }
   }

   public static String getBlockStateProperties(BlockState blockState) {
      StringBuilder stringBuilder = new StringBuilder();
      if (!blockState.getValues().isEmpty()) {
         stringBuilder.append('[');
         stringBuilder.append(blockState.getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
         stringBuilder.append(']');
      }

      return stringBuilder.toString();
   }

   public static ListTag toListTag(Number... values) {
      ListTag listTag = new ListTag();
      Arrays.stream(values).toList().forEach(e -> {
         if (e instanceof Float e1) {
            listTag.add(FloatTag.valueOf(e1));
         } else if (e instanceof Double e1) {
            listTag.add(DoubleTag.valueOf(e1));
         }
      });
      return listTag;
   }

   public static ListTag toDoubleListTag(Vector3f values) {
      ListTag listTag = new ListTag();
      listTag.add(DoubleTag.valueOf((double)values.x()));
      listTag.add(DoubleTag.valueOf((double)values.y()));
      listTag.add(DoubleTag.valueOf((double)values.z()));
      return listTag;
   }

   public static CompoundTag itemStackTag(ItemStack stack) {
      if (stack.isEmpty()) {
         return new CompoundTag();
      } else {
         CompoundTag compoundTag = new CompoundTag();
         return stack.save(compoundTag);
      }
   }

   public static BlockState readBlockState(CompoundTag tag) {
      if (!tag.contains("Name", 8)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         Block block = (Block)Registry.BLOCK.get(new ResourceLocation(tag.getString("Name")));
         BlockState blockState = block.defaultBlockState();
         if (tag.contains("Properties", 10)) {
            CompoundTag compoundTag = tag.getCompound("Properties");
            StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();

            for (String string : compoundTag.getAllKeys()) {
               Property<?> property = stateDefinition.getProperty(string);
               if (property != null) {
                  blockState = setValueHelper(blockState, property, string, compoundTag, tag);
               }
            }
         }

         return blockState;
      }
   }

   private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(
      S stateHolder, Property<T> property, String propertyName, CompoundTag propertiesTag, CompoundTag blockStateTag
   ) {
      Optional<T> optional = property.getValue(propertiesTag.getString(propertyName));
      return optional.<S>map(t -> (S)stateHolder.setValue(property, t)).orElse(stateHolder);
   }

   public static enum MirrorFace {
      X,
      Y,
      Z;
   }
}
