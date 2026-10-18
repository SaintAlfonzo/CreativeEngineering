package umpaz.brewinandchewin.common.utility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;

// Port: 1.21 fluid data components -> the fluid NBT tag a Forge 1.19.2 FluidStack carries. `tag()` replaces
// `components()`; a null or empty tag means "no components". The upstream Codec/StreamCodec are replaced by fromJson /
// write / read.
public class AbstractedFluidStack {
   public static final AbstractedFluidStack EMPTY = new AbstractedFluidStack(Fluids.EMPTY, 0L, null, FluidUnit.getLoaderUnit(), null);
   private final Fluid fluid;
   private final long amount;
   @Nullable
   private final CompoundTag tag;
   private final FluidUnit unit;
   private Object loaderSpecific;

   public AbstractedFluidStack(Fluid fluid, long amount, @Nullable CompoundTag tag, FluidUnit unit, @Nullable Object loaderSpecific) {
      this.fluid = fluid;
      this.amount = amount;
      this.tag = tag == null || tag.isEmpty() ? null : tag.copy();
      this.unit = unit;
      this.loaderSpecific = loaderSpecific;
   }

   public AbstractedFluidStack(Fluid fluid, long amount, @Nullable CompoundTag tag) {
      this(fluid, amount, tag, FluidUnit.getLoaderUnit(), null);
   }

   public AbstractedFluidStack(Fluid fluid, long amount, @Nullable CompoundTag tag, FluidUnit unit) {
      this(fluid, amount, tag, unit, null);
   }

   public AbstractedFluidStack(Fluid fluid, long amount) {
      this(fluid, amount, null, FluidUnit.getLoaderUnit(), null);
   }

   public boolean isEmpty() {
      return this == EMPTY || this.fluid == Fluids.EMPTY || this.amount <= 0L;
   }

   public boolean matches(AbstractedFluidStack other) {
      return this.fluid == other.fluid && Objects.equals(this.tag, other.tag);
   }

   public Fluid fluid() {
      return this.isEmpty() ? Fluids.EMPTY : this.fluid;
   }

   public long amount() {
      return this.isEmpty() ? 0L : this.amount;
   }

   /** A copy of the fluid's NBT, or null when it has none. */
   @Nullable
   public CompoundTag tag() {
      return this.tag == null ? null : this.tag.copy();
   }

   public boolean hasTag() {
      return this.tag != null;
   }

   public FluidUnit unit() {
      return this.unit;
   }

   public Object loaderSpecific() {
      if (this.loaderSpecific == null) {
         this.loaderSpecific = BrewinAndChewin.getHelper().createLoaderFluidStack(this);
      }

      return BrewinAndChewin.getHelper().copyLoaderFluidStack(this.loaderSpecific);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      return o instanceof AbstractedFluidStack other
         && this.fluid() == other.fluid()
         && this.amount() == other.amount()
         && this.unit == other.unit
         && Objects.equals(this.tag, other.tag);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.fluid(), this.amount(), this.unit, this.tag);
   }

   @Override
   public String toString() {
      return this.amount() + " " + ForgeRegistries.FLUIDS.getKey(this.fluid()) + (this.tag == null ? "" : this.tag.toString());
   }

   /**
    * Upstream codec shape: {"id": "<fluid>", "amount": N, "unit": "millibuckets", "nbt": {...}}. "nbt" (an object or an
    * SNBT string) replaces 1.21's "components".
    */
   public static AbstractedFluidStack fromJson(JsonObject json) {
      ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "id"));
      if (!ForgeRegistries.FLUIDS.containsKey(id)) {
         throw new JsonParseException("Unknown fluid '" + id + "'");
      }
      Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
      long amount = GsonHelper.getAsLong(json, "amount");
      if (amount < 1L) {
         throw new JsonParseException("Fluid amount must be positive");
      }
      FluidUnit unit = json.has("unit") ? BnCJson.unit(json.get("unit")) : FluidUnit.MILLIBUCKET;
      CompoundTag tag = BnCJson.optionalNbt(json, "nbt");
      return new AbstractedFluidStack(fluid, amount, tag, unit);
   }

   public static void write(FriendlyByteBuf buf, AbstractedFluidStack stack) {
      buf.writeBoolean(!stack.isEmpty());
      if (!stack.isEmpty()) {
         buf.writeResourceLocation(ForgeRegistries.FLUIDS.getKey(stack.fluid()));
         buf.writeVarLong(stack.amount());
         buf.writeNbt(stack.tag);
      }
      FluidUnit.write(buf, stack.unit());
   }

   public static AbstractedFluidStack read(FriendlyByteBuf buf) {
      if (!buf.readBoolean()) {
         FluidUnit.read(buf);
         return EMPTY;
      }
      Fluid fluid = ForgeRegistries.FLUIDS.getValue(buf.readResourceLocation());
      long amount = buf.readVarLong();
      CompoundTag tag = buf.readNbt();
      FluidUnit unit = FluidUnit.read(buf);
      return new AbstractedFluidStack(fluid == null ? Fluids.EMPTY : fluid, amount, tag, unit);
   }
}
