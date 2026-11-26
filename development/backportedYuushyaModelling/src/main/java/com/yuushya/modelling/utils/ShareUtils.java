// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 utils/ShareUtils.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.yuushya.modelling.blockentity.transformData.TransformBlockData;
import com.yuushya.modelling.blockentity.transformData.TransformItemData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;

public class ShareUtils {
   public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

   public static int getABGR(int rgb) {
      int a = rgb >> 24 & 0xFF;
      int r = rgb >> 16 & 0xFF;
      int g = rgb >> 8 & 0xFF;
      int b = rgb & 0xFF;
      return a << 24 | b << 16 | g << 8 | r;
   }

   public static String transfer(List<TransformBlockData> transformDataList) {
      ShareUtils.ShareBlockInformation shareBlockInformation = ShareUtils.ShareBlockInformation.from(transformDataList);
      return GSON.toJson(shareBlockInformation, ShareUtils.ShareBlockInformation.class);
   }

   public static String transferItems(List<TransformItemData> transformDataList) {
      ShareUtils.ShareItemInformation shareInformation = ShareUtils.ShareItemInformation.from(transformDataList);
      return GSON.toJson(shareInformation, ShareUtils.ShareItemInformation.class);
   }

   // PHASE-B: transferText(List<TransformTextData>) returns with the Text Block (plan section 4).

   public static ShareUtils.ShareBlockInformation from(String json) {
      return (ShareUtils.ShareBlockInformation)GSON.fromJson(json, ShareUtils.ShareBlockInformation.class);
   }

   public static ShareUtils.ShareItemInformation fromItems(String json) {
      return (ShareUtils.ShareItemInformation)GSON.fromJson(json, ShareUtils.ShareItemInformation.class);
   }

   // PHASE-B: fromText(String) returns with the Text Block.

   public static CompoundTag jsonToCompoundTag(JsonElement json) {
      DataResult<CompoundTag> result = CompoundTag.CODEC.parse(JsonOps.INSTANCE, json);
      return result.result().orElse(new CompoundTag());
   }

   // Upstream's share types are Java records. Gson 2.8.9 (Minecraft 1.19.2) cannot set a record's final fields (an
   // AssertionError around IllegalAccessException, which catch (Exception) misses), so they are plain classes here
   // with the same field names (the JSON keys, UPPER_CAMEL_CASE as upstream) and record-style accessors. Gson leaves a
   // missing key null (0 / false for primitives), as upstream's Gson 2.10 did.
   public static final class ShareBlockInformation {
      private Set<String> mods;
      private List<ShareUtils.ShareBlockInformation.ShareData> blocks;

      private ShareBlockInformation() {
      }

      public ShareBlockInformation(Set<String> mods, List<ShareUtils.ShareBlockInformation.ShareData> blocks) {
         this.mods = mods;
         this.blocks = blocks;
      }

      public Set<String> mods() {
         return this.mods;
      }

      public List<ShareUtils.ShareBlockInformation.ShareData> blocks() {
         return this.blocks;
      }

      public static ShareUtils.ShareBlockInformation from(List<TransformBlockData> transformDataList) {
         Set<String> modIds = new HashSet<>();
         List<ShareUtils.ShareBlockInformation.ShareData> shareDataList = new ArrayList<>();

         for (TransformBlockData data : transformDataList) {
            String namespace = Registry.BLOCK.getKey(data.blockState.getBlock()).getNamespace();
            if (!"minecraft".equals(namespace)) {
               modIds.add(namespace);
            }

            shareDataList.add(ShareUtils.ShareBlockInformation.ShareData.from(data));
         }

         return new ShareUtils.ShareBlockInformation(modIds, shareDataList);
      }

      public void transfer(List<TransformBlockData> transformDataList) {
         if (!transformDataList.isEmpty()) {
            transformDataList.clear();
         }

         for (ShareUtils.ShareBlockInformation.ShareData data : this.blocks) {
            transformDataList.add(data.transfer());
         }
      }

      public static final class ShareData {
         private List<Double> pos;
         private List<Float> rot;
         private List<Float> scales;
         private ShareUtils.ShareBlockInformation.ShareData.ShareBlockState blockState;
         private boolean isShown;

         private ShareData() {
         }

         public ShareData(
            List<Double> pos, List<Float> rot, List<Float> scales, ShareUtils.ShareBlockInformation.ShareData.ShareBlockState blockState, boolean isShown
         ) {
            this.pos = pos;
            this.rot = rot;
            this.scales = scales;
            this.blockState = blockState;
            this.isShown = isShown;
         }

         public List<Double> pos() {
            return this.pos;
         }

         public List<Float> rot() {
            return this.rot;
         }

         public List<Float> scales() {
            return this.scales;
         }

         public ShareUtils.ShareBlockInformation.ShareData.ShareBlockState blockState() {
            return this.blockState;
         }

         public boolean isShown() {
            return this.isShown;
         }

         public static ShareUtils.ShareBlockInformation.ShareData from(TransformBlockData data) {
            return new ShareUtils.ShareBlockInformation.ShareData(
               List.of(data.pos.x, data.pos.y, data.pos.z),
               List.of(data.rot.x(), data.rot.y(), data.rot.z()),
               List.of(data.scales.x(), data.scales.y(), data.scales.z()),
               ShareUtils.ShareBlockInformation.ShareData.ShareBlockState.from(data.blockState),
               data.isShown
            );
         }

         public TransformBlockData transfer() {
            // Copies, as upstream's item form does: upstream padded the share object's own lists, so a second transfer
            // of one object (History List, Chisel results) kept growing them, and a ShareData built by
            // from(TransformBlockData) (immutable List.of lists) threw.
            List<Double> posList = this.pos == null ? new ArrayList<>() : new ArrayList<>(this.pos);
            List<Float> rotList = this.rot == null ? new ArrayList<>() : new ArrayList<>(this.rot);
            List<Float> scalesList = this.scales == null ? new ArrayList<>() : new ArrayList<>(this.scales);
            posList.add(0.0);
            posList.add(0.0);
            posList.add(0.0);
            rotList.add(0.0F);
            rotList.add(0.0F);
            rotList.add(0.0F);
            scalesList.add(1.0F);
            scalesList.add(1.0F);
            scalesList.add(1.0F);
            return new TransformBlockData(
               new Vector3d(posList.get(0), posList.get(1), posList.get(2)),
               new Vector3f(rotList.get(0), rotList.get(1), rotList.get(2)),
               new Vector3f(scalesList.get(0), scalesList.get(1), scalesList.get(2)),
               this.blockState.transfer(),
               this.isShown
            );
         }

         public static final class ShareBlockState {
            private String name;
            private Map<String, String> properties;

            private ShareBlockState() {
            }

            public ShareBlockState(String name, Map<String, String> properties) {
               this.name = name;
               this.properties = properties;
            }

            public String name() {
               return this.name;
            }

            public Map<String, String> properties() {
               return this.properties;
            }

            public static ShareUtils.ShareBlockInformation.ShareData.ShareBlockState from(BlockState state) {
               String name = Registry.BLOCK.getKey(state.getBlock()).toString();
               Map<Property<?>, Comparable<?>> map = state.getValues();
               Map<String, String> properties = new HashMap<>();

               for (Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
                  Property<?> property = entry.getKey();
                  Comparable<?> value = entry.getValue();
                  properties.put(property.getName(), getName((Property)property, (Comparable)value));
               }

               return new ShareUtils.ShareBlockInformation.ShareData.ShareBlockState(name, properties);
            }

            private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S stateHolder, Property<T> property, String value) {
               return property.getValue(value).map(t -> (S)stateHolder.setValue(property, t)).orElse(stateHolder);
            }

            private static <T extends Comparable<T>> String getName(Property<T> property, Comparable<T> value) {
               return property.getName((T)value);
            }

            public BlockState transfer() {
               Block block = (Block)Registry.BLOCK.get(new ResourceLocation(this.name));
               BlockState blockState = block.defaultBlockState();
               StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();

               for (String string : this.properties.keySet()) {
                  Property<?> property = stateDefinition.getProperty(string);
                  if (property != null) {
                     blockState = setValueHelper(blockState, property, this.properties.get(string));
                  }
               }

               return blockState;
            }
         }
      }
   }

   public static final class ShareItemInformation {
      private Set<String> mods;
      private List<ShareUtils.ShareItemInformation.ItemShareData> items;

      private ShareItemInformation() {
      }

      public ShareItemInformation(Set<String> mods, List<ShareUtils.ShareItemInformation.ItemShareData> items) {
         this.mods = mods;
         this.items = items;
      }

      public Set<String> mods() {
         return this.mods;
      }

      public List<ShareUtils.ShareItemInformation.ItemShareData> items() {
         return this.items;
      }

      public static ShareUtils.ShareItemInformation from(List<TransformItemData> transformDataList) {
         Set<String> modIds = new HashSet<>();
         List<ShareUtils.ShareItemInformation.ItemShareData> shareDataList = new ArrayList<>();

         for (TransformItemData data : transformDataList) {
            String namespace = Registry.ITEM.getKey(data.itemStack.getItem()).getNamespace();
            if (!"minecraft".equals(namespace)) {
               modIds.add(namespace);
            }

            shareDataList.add(ShareUtils.ShareItemInformation.ItemShareData.from(data));
         }

         return new ShareUtils.ShareItemInformation(modIds, shareDataList);
      }

      public void transferItems(List<TransformItemData> transformDataList) {
         if (!transformDataList.isEmpty()) {
            transformDataList.clear();
         }

         for (ShareUtils.ShareItemInformation.ItemShareData data : this.items) {
            transformDataList.add(data.transfer());
         }
      }

      public static final class ItemShareData {
         private List<Double> pos;
         private List<Float> rot;
         private List<Float> scales;
         private ShareUtils.ShareItemInformation.ItemShareData.ShareItemStack itemStack;
         private int color;
         private boolean isShown;
         private boolean enableBlock;

         private ItemShareData() {
         }

         public ItemShareData(
            List<Double> pos,
            List<Float> rot,
            List<Float> scales,
            ShareUtils.ShareItemInformation.ItemShareData.ShareItemStack itemStack,
            int color,
            boolean isShown,
            boolean enableBlock
         ) {
            this.pos = pos;
            this.rot = rot;
            this.scales = scales;
            this.itemStack = itemStack;
            this.color = color;
            this.isShown = isShown;
            this.enableBlock = enableBlock;
         }

         public List<Double> pos() {
            return this.pos;
         }

         public List<Float> rot() {
            return this.rot;
         }

         public List<Float> scales() {
            return this.scales;
         }

         public ShareUtils.ShareItemInformation.ItemShareData.ShareItemStack itemStack() {
            return this.itemStack;
         }

         public int color() {
            return this.color;
         }

         public boolean isShown() {
            return this.isShown;
         }

         public boolean enableBlock() {
            return this.enableBlock;
         }

         public static ShareUtils.ShareItemInformation.ItemShareData from(TransformItemData data) {
            return new ShareUtils.ShareItemInformation.ItemShareData(
               List.of(data.pos.x, data.pos.y, data.pos.z),
               List.of(data.rot.x(), data.rot.y(), data.rot.z()),
               List.of(data.scales.x(), data.scales.y(), data.scales.z()),
               ShareUtils.ShareItemInformation.ItemShareData.ShareItemStack.from(data.itemStack),
               data.color,
               data.isShown,
               data.enableBlock
            );
         }

         public TransformItemData transfer() {
            List<Double> posList = new ArrayList<>(this.pos);
            List<Float> rotList = new ArrayList<>(this.rot);
            List<Float> scalesList = new ArrayList<>(this.scales);
            posList.add(0.0);
            posList.add(0.0);
            posList.add(0.0);
            rotList.add(0.0F);
            rotList.add(0.0F);
            rotList.add(0.0F);
            scalesList.add(1.0F);
            scalesList.add(1.0F);
            scalesList.add(1.0F);
            return new TransformItemData(
               new Vector3d(posList.get(0), posList.get(1), posList.get(2)),
               new Vector3f(rotList.get(0), rotList.get(1), rotList.get(2)),
               new Vector3f(scalesList.get(0), scalesList.get(1), scalesList.get(2)),
               this.itemStack.transfer(),
               this.color,
               this.isShown,
               this.enableBlock
            );
         }

         public static final class ShareItemStack {
            private String name;
            private JsonElement jsonElement;

            private ShareItemStack() {
            }

            public ShareItemStack(String name, JsonElement jsonElement) {
               this.name = name;
               this.jsonElement = jsonElement;
            }

            public String name() {
               return this.name;
            }

            public JsonElement jsonElement() {
               return this.jsonElement;
            }

            public static ShareUtils.ShareItemInformation.ItemShareData.ShareItemStack from(ItemStack stack) {
               ResourceLocation itemId = Registry.ITEM.getKey(stack.getItem());
               CompoundTag tag = stack.save(new CompoundTag());
               String asString = tag.getAsString();
               JsonPrimitive jsonPrimitive = new JsonPrimitive(asString);
               return new ShareUtils.ShareItemInformation.ItemShareData.ShareItemStack(itemId.toString(), jsonPrimitive);
            }

            public ItemStack transfer() {
               String snbtFromJson = this.jsonElement.getAsJsonPrimitive().getAsString();

               CompoundTag compoundTag;
               try {
                  compoundTag = TagParser.parseTag(snbtFromJson);
               } catch (Exception var4) {
                  compoundTag = new CompoundTag();
               }

               return ItemStack.of(compoundTag);
            }
         }
      }
   }

   // PHASE-B: SharedTextInformation (Text Block share format) returns with the Text Block, as a plain class (a record
   // upstream; see ShareBlockInformation).

   public static class StringSerialization {
      public static CompoundTag transfer(String string) throws CommandSyntaxException {
         return TagParser.parseTag(string);
      }

      public static String from(CompoundTag tag) {
         return tag.getAsString();
      }
   }
}
