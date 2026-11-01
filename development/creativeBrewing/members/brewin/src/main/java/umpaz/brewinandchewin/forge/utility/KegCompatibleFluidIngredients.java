package umpaz.brewinandchewin.forge.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.utility.AbstractedFluidIngredient;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCJson;
import umpaz.brewinandchewin.common.utility.FluidUnit;

/**
 * The fluid ingredients a keg recipe can name. JSON, as in 4.5.0:
 * <ul>
 * <li>{"id": "brewinandchewin:vodka"} (optional "nbt"): exactly that fluid</li>
 * <li>{"tag": "#forge:water"} (the "#" is optional, plus optional "nbt"): any fluid in the tag</li>
 * </ul>
 * Port: 1.19.2 Forge has no FluidIngredient, so upstream's third (NeoForge) variant is gone.
 */
public class KegCompatibleFluidIngredients {
   public static AbstractedFluidIngredient fromJson(JsonElement element) {
      JsonObject json = GsonHelper.convertToJsonObject(element, "fluid ingredient");
      CompoundTag tag = BnCJson.optionalNbt(json, "nbt");
      if (json.has("tag")) {
         String raw = GsonHelper.getAsString(json, "tag");
         ResourceLocation id = new ResourceLocation(raw.startsWith("#") ? raw.substring(1) : raw);
         return new Tag(TagKey.create(Registry.FLUID_REGISTRY, id), tag);
      } else if (json.has("id")) {
         ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "id"));
         if (!ForgeRegistries.FLUIDS.containsKey(id)) {
            throw new JsonParseException("Unknown fluid '" + id + "'");
         }
         return new Exact(ForgeRegistries.FLUIDS.getValue(id), tag);
      }
      throw new JsonParseException("A fluid ingredient needs an 'id' or a 'tag'");
   }

   public static void write(FriendlyByteBuf buf, AbstractedFluidIngredient ingredient) {
      if (ingredient instanceof Exact exact) {
         buf.writeByte(0);
         buf.writeResourceLocation(ForgeRegistries.FLUIDS.getKey(exact.displayStack.fluid()));
         buf.writeNbt(exact.displayStack.tag());
      } else if (ingredient instanceof Tag tag) {
         buf.writeByte(1);
         buf.writeResourceLocation(tag.fluidTag.location());
         buf.writeNbt(tag.tag);
      } else {
         throw new UnsupportedOperationException("Unsupported wrapped fluid ingredient class.");
      }
   }

   public static AbstractedFluidIngredient read(FriendlyByteBuf buf) {
      byte kind = buf.readByte();
      ResourceLocation id = buf.readResourceLocation();
      CompoundTag tag = buf.readNbt();
      if (kind == 0) {
         return new Exact(ForgeRegistries.FLUIDS.getValue(id), tag);
      }
      return new Tag(TagKey.create(Registry.FLUID_REGISTRY, id), tag);
   }

   public static class Exact implements AbstractedFluidIngredient {
      private final AbstractedFluidStack displayStack;

      public Exact(Fluid fluid, @Nullable CompoundTag tag) {
         this.displayStack = new AbstractedFluidStack(fluid, 1000L, tag, FluidUnit.MILLIBUCKET, new FluidStack(fluid, 1000, tag));
      }

      public Exact(Fluid fluid) {
         this(fluid, null);
      }

      public Fluid getFluid() {
         return this.displayStack.fluid();
      }

      @Override
      public List<AbstractedFluidStack> displayStacks() {
         return List.of(this.displayStack);
      }

      @Override
      public boolean matches(AbstractedFluidStack stack) {
         return !this.displayStack.hasTag() ? this.displayStack.fluid().isSame(stack.fluid()) : this.displayStack.matches(stack);
      }
   }

   public static class Tag implements AbstractedFluidIngredient {
      private final TagKey<Fluid> fluidTag;
      @Nullable
      private final CompoundTag tag;

      public Tag(TagKey<Fluid> fluidTag, @Nullable CompoundTag tag) {
         this.fluidTag = fluidTag;
         this.tag = tag == null || tag.isEmpty() ? null : tag;
      }

      public Tag(TagKey<Fluid> fluidTag) {
         this(fluidTag, null);
      }

      public TagKey<Fluid> getTagKey() {
         return this.fluidTag;
      }

      // Port: upstream caches these; tags can change on /reload, so they're rebuilt each time (a handful of fluids).
      @Override
      public List<AbstractedFluidStack> displayStacks() {
         List<AbstractedFluidStack> stacks = new ArrayList<>();
         for (Holder<Fluid> holder : Registry.FLUID.getTagOrEmpty(this.fluidTag)) {
            Fluid fluid = holder.value();
            if (fluid.isSource(fluid.defaultFluidState())) {
               stacks.add(new AbstractedFluidStack(fluid, 1000L, this.tag, FluidUnit.MILLIBUCKET, new FluidStack(fluid, 1000, this.tag)));
            }
         }
         return stacks;
      }

      @Override
      public boolean matches(AbstractedFluidStack stack) {
         if (stack.isEmpty() || !stack.fluid().is(this.fluidTag)) {
            return false;
         }
         return this.tag == null || Objects.equals(this.tag, stack.tag());
      }
   }
}
