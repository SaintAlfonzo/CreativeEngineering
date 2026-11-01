package umpaz.brewinandchewin.common.utility;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.platform.BnCPlatform;

public enum FluidUnit implements StringRepresentable {
   LITER("liters", l -> l + " L", l -> l + " liters", 1L),
   MILLIBUCKET("millibuckets", l -> l + " mB", l -> l + " millibuckets", 1L),
   DROPLET("droplets", l -> l + " d", l -> l + " droplets", 81L);

   private final String name;
   private final Function<String, String> shortFormFormatFunc;
   private final Function<String, String> longFormFormatFunc;
   private final long oneL;
   public static final Codec<FluidUnit> CODEC = StringRepresentable.fromEnum(FluidUnit::values);

   FluidUnit(String name, Function<String, String> shortFormFormatFunc, Function<String, String> longFormFormatFunc, long oneL) {
      this.name = name;
      this.shortFormFormatFunc = shortFormFormatFunc;
      this.longFormFormatFunc = longFormFormatFunc;
      this.oneL = oneL;
   }

   public long convert(long value, FluidUnit unit) {
      return convert(value, this, unit);
   }

   public long convertToLoader(long value) {
      return convertToLoader(value, this);
   }

   public static FluidUnit getOpposite(FluidUnit unit) {
      if (unit == DROPLET) {
         return BrewinAndChewin.getHelper().getPlatform() == BnCPlatform.NEOFORGE ? MILLIBUCKET : LITER;
      } else {
         return DROPLET;
      }
   }

   public static FluidUnit getLoaderUnit() {
      return MILLIBUCKET;
   }

   public static long convertToLoader(long value, FluidUnit unit) {
      return convert(value, unit, getLoaderUnit());
   }

   public static long convert(long value, FluidUnit originalUnit, FluidUnit newUnit) {
      return originalUnit.oneL == newUnit.oneL ? value : value / originalUnit.oneL * newUnit.oneL;
   }

   public String shortFormat(String value) {
      return this.shortFormFormatFunc.apply(value);
   }

   public String longFormat(String value) {
      return this.longFormFormatFunc.apply(value);
   }

   @Override
   public String getSerializedName() {
      return this.name;
   }

   @Nullable
   public static FluidUnit byName(String name) {
      for (FluidUnit unit : values()) {
         if (unit.name.equals(name)) {
            return unit;
         }
      }
      return null;
   }

   // Port: StreamCodec -> FriendlyByteBuf.
   public static void write(FriendlyByteBuf buf, FluidUnit unit) {
      buf.writeVarInt(unit.ordinal());
   }

   public static FluidUnit read(FriendlyByteBuf buf) {
      int id = buf.readVarInt();
      return id >= 0 && id < values().length ? values()[id] : LITER;
   }
}
