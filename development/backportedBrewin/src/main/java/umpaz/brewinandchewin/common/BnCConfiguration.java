package umpaz.brewinandchewin.common;

import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.jetbrains.annotations.NotNull;
import umpaz.brewinandchewin.common.utility.FluidUnit;

/**
 * Port: upstream's Greenhouse Config (codec-backed toml) -> two ForgeConfigSpec files with the same key names:
 * {@code brewinandchewin-common.toml} ([keg], [recipe_book]) and {@code brewinandchewin-client.toml}. The records keep
 * upstream's accessors, so callers still read {@code COMMON_CONFIG.get().keg().cold()}.
 * <p>
 * Deviation (alcohol.md 4.3): the three scramble thresholds ({@code levelChatScramble} and so on) are client settings,
 * next to the scramble switches. Upstream keeps them in the common file's root and syncs them from the server.
 */
public class BnCConfiguration {
   private static final ForgeConfigSpec COMMON_SPEC;
   private static final ForgeConfigSpec CLIENT_SPEC;
   public static final Holder<Common> COMMON_CONFIG;
   public static final Holder<Client> CLIENT_CONFIG;

   private static ForgeConfigSpec.EnumValue<FluidUnit> kegCapacityUnit;
   private static ForgeConfigSpec.LongValue kegCapacity;
   private static ForgeConfigSpec.IntValue kegCold;
   private static ForgeConfigSpec.IntValue kegChilly;
   private static ForgeConfigSpec.IntValue kegWarm;
   private static ForgeConfigSpec.IntValue kegHot;
   private static ForgeConfigSpec.BooleanValue kegBiomeTemp;
   private static ForgeConfigSpec.BooleanValue kegDimTemp;
   private static ForgeConfigSpec.BooleanValue enableRecipeBookKeg;

   private static ForgeConfigSpec.EnumValue<FluidUnit> fluidDisplayUnit;
   private static ForgeConfigSpec.EnumValue<Client.DisplaySettings> oppositeFluidDisplay;
   private static ForgeConfigSpec.BooleanValue numbedHeartFlickering;
   private static ForgeConfigSpec.BooleanValue intoxicationFoodOverlay;
   private static ForgeConfigSpec.BooleanValue scrambleChat;
   private static ForgeConfigSpec.BooleanValue scrambleName;
   private static ForgeConfigSpec.BooleanValue scrambleSign;
   private static ForgeConfigSpec.IntValue levelChatScramble;
   private static ForgeConfigSpec.IntValue levelSignScramble;
   private static ForgeConfigSpec.IntValue levelNameScramble;
   private static ForgeConfigSpec.BooleanValue renderFluidInKeg;

   static {
      Common.Keg kd = Common.Keg.DEFAULT;
      ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
      common.push("keg");
      kegCapacityUnit = common.comment("Which unit the capacity field should use.", "Should be 'LITER', 'MILLIBUCKET' or 'DROPLET'",
            "1 L = 1 mB = 81 d", "Default: " + kd.capacityUnit().name())
         .defineEnum("kegCapacityUnit", kd.capacityUnit());
      kegCapacity = common.comment("How much fluid (unit specified by kegCapacityUnit) can the Keg hold?",
            "Default: " + kd.capacity() + " (" + kd.capacityUnit().getSerializedName() + ")")
         .defineInRange("kegCapacity", kd.capacity(), 1L, FluidUnit.convert(10000L, FluidUnit.MILLIBUCKET, FluidUnit.DROPLET));
      kegCold = common.comment("How many cold blocks are required for a cold temperature in the Keg?", "Default: " + kd.cold())
         .defineInRange("kegCold", kd.cold(), 1, Integer.MAX_VALUE);
      kegChilly = common.comment("How many cold blocks are required for a chilly temperature in the Keg?", "Default: " + kd.chilly())
         .defineInRange("kegChilly", kd.chilly(), 1, Integer.MAX_VALUE);
      kegWarm = common.comment("How many hot blocks are required for a warm temperature in the Keg?", "Default: " + kd.warm())
         .defineInRange("kegWarm", kd.warm(), 1, Integer.MAX_VALUE);
      kegHot = common.comment("How many hot blocks are required for a hot temperature in the Keg?", "Default: " + kd.hot())
         .defineInRange("kegHot", kd.hot(), 1, Integer.MAX_VALUE);
      kegBiomeTemp = common.comment("Should the biome temperature influence the temperature in the Keg?", "Default: " + kd.biomeTemp())
         .define("kegBiomeTemp", kd.biomeTemp());
      kegDimTemp = common.comment("Should the dimension temperature influence the temperature in the Keg?", "Default: " + kd.dimTemp())
         .define("kegDimTemp", kd.dimTemp());
      common.pop();
      common.push("recipe_book");
      enableRecipeBookKeg = common.comment("Should the Keg have a Recipe Book available on its interface?", "Default: " + Common.RecipeBook.DEFAULT.enabled())
         .define("enableRecipeBookKeg", Common.RecipeBook.DEFAULT.enabled());
      common.pop();
      COMMON_SPEC = common.build();

      Client cd = Client.DEFAULT;
      ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
      fluidDisplayUnit = client.comment("Which unit the fluid display in the keg should use.", "Should be 'LITER', 'MILLIBUCKET' or 'DROPLET'",
            "1 L = 1 mB = 81 d", "Default: " + cd.displayUnit().name())
         .defineEnum("fluidDisplayUnit", cd.displayUnit());
      oppositeFluidDisplay = client.comment("When the opposite fluid display unit should be shown.", "Should be one of 'NEVER', 'ADVANCED_TOOLTIPS' or 'ALWAYS'",
            "Default: " + cd.oppositeFluidDisplay().name())
         .defineEnum("oppositeFluidDisplay", cd.oppositeFluidDisplay());
      numbedHeartFlickering = client.comment("Should the numbed hearts obtained from being damaged when Tipsy flicker when you are about to take damage?",
            "Default: " + cd.numbedHeartFlickering())
         .define("numbedHeartFlickering", cd.numbedHeartFlickering());
      intoxicationFoodOverlay = client.comment("Should the food bar have a yellow overlay when the player has the Intoxication effect?",
            "Default: " + cd.intoxicationFoodOverlay())
         .define("intoxicationFoodOverlay", cd.intoxicationFoodOverlay());
      scrambleChat = client.comment("Should the chat scramble when the player has the Tipsy effect?", "Default: " + cd.scrambleChat())
         .define("scrambleChat", cd.scrambleChat());
      scrambleName = client.comment("Should other player's nametags scramble when the player has the Tipsy effect?", "Default: " + cd.scrambleName())
         .define("scrambleName", cd.scrambleName());
      scrambleSign = client.comment("Should signs scramble when the player has the Tipsy effect?", "Default: " + cd.scrambleSign())
         .define("scrambleSign", cd.scrambleSign());
      levelChatScramble = client.comment("At what amplifier of Tipsy should the chat scramble? (the sender's Tipsy)", "Default: " + cd.levelChatScramble())
         .defineInRange("levelChatScramble", cd.levelChatScramble(), 1, 10);
      levelSignScramble = client.comment("At what amplifier of Tipsy should signs scramble? (your own Tipsy)", "Default: " + cd.levelSignScramble())
         .defineInRange("levelSignScramble", cd.levelSignScramble(), 1, 10);
      levelNameScramble = client.comment("At what amplifier of Tipsy should nametags scramble? (your own Tipsy)", "Default: " + cd.levelNameScramble())
         .defineInRange("levelNameScramble", cd.levelNameScramble(), 1, 10);
      renderFluidInKeg = client.comment("Should kegs render the fluid texture in the background of the fluid slot?", "Default: " + cd.renderFluidInKeg())
         .define("renderFluidInKeg", cd.renderFluidInKeg());
      CLIENT_SPEC = client.build();

      COMMON_CONFIG = new Holder<>(COMMON_SPEC, Common.DEFAULT, () -> new Common(
         new Common.Keg(kegCapacityUnit.get(), kegCapacity.get(), kegCold.get(), kegChilly.get(), kegWarm.get(), kegHot.get(), kegBiomeTemp.get(), kegDimTemp.get()),
         new Common.RecipeBook(enableRecipeBookKeg.get())
      ));
      CLIENT_CONFIG = new Holder<>(CLIENT_SPEC, Client.DEFAULT, () -> new Client(
         fluidDisplayUnit.get(), oppositeFluidDisplay.get(), numbedHeartFlickering.get(), intoxicationFoodOverlay.get(), scrambleChat.get(),
         scrambleName.get(), scrambleSign.get(), levelChatScramble.get(), levelSignScramble.get(), levelNameScramble.get(), renderFluidInKeg.get()
      ));
   }

   /** Registers both files; called from the mod constructor. Forge reads them after registration, before common setup. */
   public static void init() {
      ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC, "brewinandchewin-common.toml");
      ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC, "brewinandchewin-client.toml");
   }

   /** Mod bus listener: drop the cached records when a file loads or changes. */
   public static void onConfigChanged(ModConfigEvent event) {
      if (event.getConfig().getSpec() == COMMON_SPEC) {
         COMMON_CONFIG.invalidate();
      } else if (event.getConfig().getSpec() == CLIENT_SPEC) {
         CLIENT_CONFIG.invalidate();
      }
   }

   /** The settings as one immutable record, rebuilt after each (re)load; the defaults until the file is read. */
   public static final class Holder<T> {
      private final ForgeConfigSpec spec;
      private final T defaults;
      private final java.util.function.Supplier<T> reader;
      private volatile T cached;

      private Holder(ForgeConfigSpec spec, T defaults, java.util.function.Supplier<T> reader) {
         this.spec = spec;
         this.defaults = defaults;
         this.reader = reader;
      }

      public T get() {
         T value = this.cached;
         if (value == null) {
            if (!this.spec.isLoaded()) {
               return this.defaults;
            }
            value = this.reader.get();
            this.cached = value;
         }
         return value;
      }

      private void invalidate() {
         this.cached = null;
      }
   }

   public record Client(
      FluidUnit displayUnit,
      BnCConfiguration.Client.DisplaySettings oppositeFluidDisplay,
      boolean numbedHeartFlickering,
      boolean intoxicationFoodOverlay,
      boolean scrambleChat,
      boolean scrambleName,
      boolean scrambleSign,
      int levelChatScramble,
      int levelSignScramble,
      int levelNameScramble,
      boolean renderFluidInKeg
   ) {
      // NeoForge defaults (the Forge build is closest to it): millibuckets, never show the opposite unit.
      public static final BnCConfiguration.Client DEFAULT = new BnCConfiguration.Client(
         FluidUnit.MILLIBUCKET, DisplaySettings.NEVER, true, true, true, true, true, 3, 3, 3, true
      );

      public enum DisplaySettings implements StringRepresentable {
         NEVER("never"),
         ADVANCED_TOOLTIPS("advanced_tooltips"),
         ALWAYS("always");

         final String name;

         DisplaySettings(String name) {
            this.name = name;
         }

         @NotNull
         @Override
         public String getSerializedName() {
            return this.name;
         }
      }
   }

   public record Common(BnCConfiguration.Common.Keg keg, BnCConfiguration.Common.RecipeBook recipeBook) {
      public static final BnCConfiguration.Common DEFAULT = new BnCConfiguration.Common(Keg.DEFAULT, RecipeBook.DEFAULT);

      public record Keg(FluidUnit capacityUnit, long capacity, int cold, int chilly, int warm, int hot, boolean biomeTemp, boolean dimTemp) {
         public static final BnCConfiguration.Common.Keg DEFAULT = new BnCConfiguration.Common.Keg(FluidUnit.MILLIBUCKET, 1000L, 2, 1, 1, 2, true, true);

         public long localizedCapacity() {
            return this.capacityUnit.convertToLoader(this.capacity);
         }
      }

      public record RecipeBook(boolean enabled) {
         public static final BnCConfiguration.Common.RecipeBook DEFAULT = new BnCConfiguration.Common.RecipeBook(true);
      }
   }
}
