package umpaz.brewinandchewin.client.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCJson;

/**
 * Which item stands for a fluid in the keg GUI, tooltips and JEI ({@code assets/<ns>/brewinandchewin/fluid_item_displays}).
 * Each key is a fluid id or {@code #tag}; each value is an item id, or {"id", "nbt"?, "copy_nbt"?, "optional"?}.
 * <p>
 * Port: upstream's data component remapper ("base" + "remaps") becomes {@code copy_nbt}: the listed keys (or every key,
 * for {@code true}) are copied from the fluid's NBT into the item's, e.g. Create's potion fluid "Potion" tag onto a
 * potion bottle.
 */
public class BnCFluidItemDisplays {
   private static final Map<Either<TagKey<Fluid>, Fluid>, FluidBasedItemStack> FLUID_TYPE_TO_ITEM_MAP = new HashMap<>();

   public static ItemStack getFluidItemDisplay(AbstractedFluidStack fluid) {
      FluidBasedItemStack exact = FLUID_TYPE_TO_ITEM_MAP.get(Either.right(fluid.fluid()));
      if (exact != null) {
         return exact.getStack(fluid);
      }
      Optional<Entry<Either<TagKey<Fluid>, Fluid>, FluidBasedItemStack>> validTagKey = FLUID_TYPE_TO_ITEM_MAP.entrySet()
         .stream()
         .filter(entry -> entry.getKey().map(fluidTagKey -> fluid.fluid().is(fluidTagKey), fluid1 -> false))
         .findFirst();
      if (validTagKey.isPresent()) {
         return validTagKey.get().getValue().getStack(fluid);
      }
      return fluid.fluid().getBucket() != Items.AIR ? fluid.fluid().getBucket().getDefaultInstance() : ItemStack.EMPTY;
   }

   public record FluidBasedItemStack(ItemStack base, @Nullable List<String> copyKeys, boolean copyAll) {
      private static final HashMap<Pair<Fluid, CompoundTag>, ItemStack> CACHE = new HashMap<>(32);

      private static FluidBasedItemStack fromJson(JsonElement json) {
         if (json.isJsonPrimitive()) {
            ResourceLocation id = new ResourceLocation(json.getAsString());
            if (!ForgeRegistries.ITEMS.containsKey(id)) {
               throw new JsonParseException("Unknown item '" + id + "'");
            }
            return new FluidBasedItemStack(new ItemStack(ForgeRegistries.ITEMS.getValue(id)), null, false);
         }
         JsonObject object = GsonHelper.convertToJsonObject(json, "fluid item display");
         ItemStack base = BnCJson.itemStack(object);
         List<String> keys = null;
         boolean all = false;
         if (object.has("copy_nbt")) {
            JsonElement copy = object.get("copy_nbt");
            if (copy.isJsonPrimitive()) {
               all = copy.getAsBoolean();
            } else {
               keys = new ArrayList<>();
               for (JsonElement key : (JsonArray)copy) {
                  keys.add(key.getAsString());
               }
            }
         }
         return new FluidBasedItemStack(base, keys, all);
      }

      private ItemStack getStack(AbstractedFluidStack fluid) {
         CompoundTag fluidTag = fluid.tag();
         Pair<Fluid, CompoundTag> key = Pair.of(fluid.fluid(), fluidTag);
         ItemStack cached = CACHE.get(key);
         if (cached != null) {
            return cached;
         }
         ItemStack item = this.base.copy();
         if (fluidTag != null && (this.copyAll || this.copyKeys != null)) {
            CompoundTag itemTag = item.getOrCreateTag();
            for (String name : fluidTag.getAllKeys()) {
               if (this.copyAll || this.copyKeys.contains(name)) {
                  itemTag.put(name, fluidTag.get(name).copy());
               }
            }
         }
         CACHE.put(key, item);
         return item;
      }
   }

   public static class Loader extends SimplePreparableReloadListener<Map<Either<TagKey<Fluid>, Fluid>, FluidBasedItemStack>> {
      public static final Loader INSTANCE = new Loader();
      private static final Gson GSON = new GsonBuilder().create();
      private static final String DIRECTORY = "brewinandchewin/fluid_item_displays";

      protected Loader() {
      }

      @Override
      protected Map<Either<TagKey<Fluid>, Fluid>, FluidBasedItemStack> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
         FluidBasedItemStack.CACHE.clear();
         Map<Either<TagKey<Fluid>, Fluid>, FluidBasedItemStack> map = new HashMap<>();

         for (Entry<ResourceLocation, List<Resource>> entry : resourceManager.listResourceStacks(DIRECTORY, path -> path.getPath().endsWith(".json")).entrySet()) {
            for (Resource resource : entry.getValue()) {
               try (Reader reader = resource.openAsReader()) {
                  JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);

                  for (Entry<String, JsonElement> e : jsonObject.entrySet()) {
                     boolean optional = e.getValue().isJsonObject()
                        && e.getValue().getAsJsonObject().has("optional")
                        && e.getValue().getAsJsonObject().get("optional").getAsBoolean();
                     Either<TagKey<Fluid>, Fluid> either;
                     if (e.getKey().startsWith("#")) {
                        either = Either.left(TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation(e.getKey().substring(1))));
                     } else {
                        ResourceLocation fluidLocation = new ResourceLocation(e.getKey());
                        if (!ForgeRegistries.FLUIDS.containsKey(fluidLocation)) {
                           if (!optional) {
                              BrewinAndChewin.LOG.error("Could not find fluid '{}' from fluid item display JSON at location '{}' from pack '{}'.",
                                 e.getKey(), entry.getKey(), resource.sourcePackId());
                           }
                           continue;
                        }

                        either = Either.right(ForgeRegistries.FLUIDS.getValue(fluidLocation));
                     }

                     try {
                        map.put(either, FluidBasedItemStack.fromJson(e.getValue()));
                     } catch (IllegalStateException | JsonParseException | ResourceLocationException | IllegalArgumentException ex) {
                        if (!optional) {
                           BrewinAndChewin.LOG.error("Couldn't parse fluid item display JSON at location '{}' from pack '{}'. ", entry.getKey(), resource.sourcePackId(), ex);
                        }
                     }
                  }
               } catch (Exception ex) {
                  BrewinAndChewin.LOG.error("Couldn't parse fluid item display JSON at location '{}' from pack '{}'. ", entry.getKey(), resource.sourcePackId(), ex);
               }
            }
         }

         return map;
      }

      @Override
      protected void apply(Map<Either<TagKey<Fluid>, Fluid>, FluidBasedItemStack> obj, ResourceManager resourceManager, ProfilerFiller profiler) {
         FLUID_TYPE_TO_ITEM_MAP.clear();
         FLUID_TYPE_TO_ITEM_MAP.putAll(obj);
      }
   }
}
