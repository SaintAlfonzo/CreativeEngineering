package dev.titanite.sparkwave.exposureexpanded;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.Exposure.DataComponents;
import io.github.mortuusars.exposure.backport.component.DataComponentType;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.film.properties.ColorBalance;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmStyle;
import io.github.mortuusars.exposure.world.camera.film.properties.HSB;
import io.github.mortuusars.exposure.world.camera.film.properties.Levels;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

/**
 * Upstream Exposure: Expanded 1.0.1 {@code ExposureExpanded}, hand-ported:
 * <ul>
 *   <li>Architectury {@code DeferredRegister}/{@code RegistrySupplier} -> Forge {@code DeferredRegister}/{@code RegistryObject},
 *   registered on the mod bus by {@link #init(IEventBus)}.</li>
 *   <li>{@code Properties.component(TYPE, value)} -> {@link DataComponentType#withDefault}; {@code arch$tab(exposure)} ->
 *   {@code tab(Exposure.CreativeTabs.EXPOSURE)} (items listed after Exposure's own, in registry order).</li>
 *   <li>Polaroid ships in this jar, so the instant slides always register as {@code InstantSlideItem}. Upstream's
 *   {@code is_polaroid_loaded} flag is set to {@code false} when Polaroid IS present, before the suppliers run, which
 *   would register them as plain items; it is dropped here.</li>
 *   <li>Loot table keys: {@code ResourceKey<LootTable>} -> {@code ResourceLocation} (1.19.2 has no loot table registry).
 *   They're unused; the loot comes from {@code data/exposure_expanded/loot_modifiers} via Exposure's {@code add_table}.</li>
 * </ul>
 */
public final class ExposureExpanded {
   public static final String MOD_ID = "exposure_expanded";
   public static final Logger LOGGER = LogUtils.getLogger();
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
   public static final DeferredRegister<Item> POLAROID_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
   public static final RegistryObject<Item> TELESCOPIC_LENS = ITEMS.register(
      "telescopic_lens", () -> new ItemWithShiftTooltip(new Properties().tab(tab()), "item.exposure_expanded.telescopic_lens.tooltip")
   );
   public static final RegistryObject<Item> PANORAMIC_LENS = ITEMS.register(
      "panoramic_lens", () -> new ItemWithShiftTooltip(new Properties().tab(tab()), "item.exposure_expanded.panoramic_lens.tooltip")
   );
   public static final RegistryObject<Item> GAMEBOY_FILM = ITEMS.register("gameboy_film", () -> paletteFilm(8962160, "gameboy"));
   public static final RegistryObject<Item> NES_FILM = ITEMS.register("nes_film", () -> paletteFilm(65280, "nes"));
   public static final RegistryObject<Item> C64_FILM = ITEMS.register("c64_film", () -> paletteFilm(10508195, "c64"));
   public static final RegistryObject<Item> CGA_FILM = ITEMS.register("cga_film", () -> paletteFilm(16711935, "cga"));
   public static final RegistryObject<Item> INSTANT_GAMEBOY_SLIDE = POLAROID_ITEMS.register(
      "instant_gameboy_slide", () -> DataComponentType.withDefault(PolaroidHandler.create_item(ExposureType.COLOR, new Properties().tab(tab())), DataComponents.FILM_COLOR_PALETTE, resource("gameboy"))
   );
   public static final RegistryObject<Item> INSTANT_NES_SLIDE = POLAROID_ITEMS.register(
      "instant_nes_slide", () -> DataComponentType.withDefault(PolaroidHandler.create_item(ExposureType.COLOR, new Properties().tab(tab())), DataComponents.FILM_COLOR_PALETTE, resource("nes"))
   );
   public static final RegistryObject<Item> INSTANT_C64_SLIDE = POLAROID_ITEMS.register(
      "instant_c64_slide", () -> DataComponentType.withDefault(PolaroidHandler.create_item(ExposureType.COLOR, new Properties().tab(tab())), DataComponents.FILM_COLOR_PALETTE, resource("c64"))
   );
   public static final RegistryObject<Item> INSTANT_CGA_SLIDE = POLAROID_ITEMS.register(
      "instant_cga_slide", () -> DataComponentType.withDefault(PolaroidHandler.create_item(ExposureType.COLOR, new Properties().tab(tab())), DataComponents.FILM_COLOR_PALETTE, resource("cga"))
   );
   public static final RegistryObject<Item> HICAP_BLACK_AND_WHITE_FILM = ITEMS.register(
      "hicap_black_and_white_film", () -> DataComponentType.withDefault(
            new FilmRollItem(ExposureType.BLACK_AND_WHITE, FilmRollItem.BAR_BLACK_AND_WHITE, new Properties().tab(tab()).stacksTo(16)), DataComponents.FILM_FRAME_COUNT, 32
         )
   );
   public static final RegistryObject<Item> HICAP_COLOR_FILM = ITEMS.register(
      "hicap_color_film", () -> DataComponentType.withDefault(
            new FilmRollItem(ExposureType.COLOR, FilmRollItem.BAR_COLOR, new Properties().tab(tab()).stacksTo(16)), DataComponents.FILM_FRAME_COUNT, 32
         )
   );
   public static final RegistryObject<Item> HIRES_BLACK_AND_WHITE_FILM = ITEMS.register(
      "hires_black_and_white_film", () -> DataComponentType.withDefault(
            new FilmRollItem(ExposureType.BLACK_AND_WHITE, FilmRollItem.BAR_BLACK_AND_WHITE, new Properties().tab(tab()).stacksTo(16)), DataComponents.FILM_FRAME_SIZE, 640
         )
   );
   public static final RegistryObject<Item> HIRES_COLOR_FILM = ITEMS.register(
      "hires_color_film", () -> DataComponentType.withDefault(
            new FilmRollItem(ExposureType.COLOR, FilmRollItem.BAR_COLOR, new Properties().tab(tab()).stacksTo(16)), DataComponents.FILM_FRAME_SIZE, 640
         )
   );
   public static final RegistryObject<Item> INSTANT_HIRES_BLACK_AND_WHITE_SLIDE = POLAROID_ITEMS.register(
      "instant_hires_black_and_white_slide", () -> hiresSlide(ExposureType.BLACK_AND_WHITE)
   );
   public static final RegistryObject<Item> INSTANT_HIRES_COLOR_SLIDE = POLAROID_ITEMS.register(
      "instant_hires_color_slide", () -> hiresSlide(ExposureType.COLOR)
   );
   public static final RegistryObject<Item> ANTIALIAS_FILTER = ITEMS.register(
      "antialias_filter",
      () -> new ItemWith2ShiftTooltips(
            new Properties().tab(tab()), "item.exposure_expanded.antialias_filter.tooltip1", "item.exposure_expanded.antialias_filter.tooltip2"
         )
   );
   public static final RegistryObject<Item> ART_FILTER = ITEMS.register("art_filter", () -> filter("art_filter"));
   public static final RegistryObject<Item> BITS_FILTER = ITEMS.register("bits_filter", () -> filter("bits_filter"));
   public static final RegistryObject<Item> BLOBS_FILTER = ITEMS.register("blobs_filter", () -> filter("blobs_filter"));
   public static final RegistryObject<Item> BLUR_FILTER = ITEMS.register("blur_filter", () -> filter("blur_filter"));
   public static final RegistryObject<Item> BUMPY_FILTER = ITEMS.register("bumpy_filter", () -> filter("bumpy_filter"));
   public static final RegistryObject<Item> COLOR_CONVOLVE_FILTER = ITEMS.register("color_convolve_filter", () -> filter("color_convolve_filter"));
   public static final RegistryObject<Item> DECONVERGE_FILTER = ITEMS.register("deconverge_filter", () -> filter("deconverge_filter"));
   public static final RegistryObject<Item> DESATURATE_FILTER = ITEMS.register("desaturate_filter", () -> filter("desaturate_filter"));
   public static final RegistryObject<Item> FLIP_FILTER = ITEMS.register("flip_filter", () -> filter("flip_filter"));
   public static final RegistryObject<Item> NTSC_FILTER = ITEMS.register("ntsc_filter", () -> filter("ntsc_filter"));
   public static final RegistryObject<Item> PENCIL_FILTER = ITEMS.register(
      "pencil_filter",
      () -> new ItemWith2ShiftTooltips(
            new Properties().tab(tab()), "item.exposure_expanded.pencil_filter.tooltip1", "item.exposure_expanded.pencil_filter.tooltip2"
         )
   );
   public static final RegistryObject<Item> SCAN_PINCUSHION_FILTER = ITEMS.register("scan_pincushion_filter", () -> filter("scan_pincushion_filter"));
   public static final RegistryObject<Item> SOBEL_FILTER = ITEMS.register("sobel_filter", () -> filter("sobel_filter"));
   public static final RegistryObject<Item> SPIDER_FILTER = ITEMS.register("spider_filter", () -> filter("spider_filter"));
   public static final RegistryObject<Item> WOBBLE_FILTER = ITEMS.register(
      "wobble_filter",
      () -> new ItemWith2ShiftTooltips(
            new Properties().tab(tab()), "item.exposure_expanded.wobble_filter.tooltip1", "item.exposure_expanded.wobble_filter.tooltip2"
         )
   );

   private static CreativeModeTab tab() {
      return Exposure.CreativeTabs.EXPOSURE.get();
   }

   private static Item paletteFilm(int barColor, String palette) {
      return DataComponentType.withDefault(
         new FilmRollItem(ExposureType.COLOR, barColor, new Properties().tab(tab()).stacksTo(16)), DataComponents.FILM_COLOR_PALETTE, resource(palette)
      );
   }

   private static Item hiresSlide(ExposureType type) {
      Item slide = PolaroidHandler.create_item(type, new Properties().tab(tab()));
      DataComponents.FILM_STYLE.setDefault(slide, FilmStyle.create()
         .withContrast(0.2F)
         .withLevels(new Levels(0, 135, 255, 25, 255))
         .withHSB(new HSB(0.0F, 0.05F, 0.05F))
         .withColorBalance(new ColorBalance(0.03F, 0.01F, -0.01F)));
      DataComponents.FILM_FRAME_SIZE.setDefault(slide, 480);
      return slide;
   }

   private static Item filter(String name) {
      return new ItemWithShiftTooltip(new Properties().tab(tab()), "item.exposure_expanded." + name + ".tooltip");
   }

   public static void init(IEventBus modEventBus) {
      POLAROID_ITEMS.register(modEventBus);
      LOGGER.info("Exposure Polaroid hook successful.");
      ITEMS.register(modEventBus);
   }

   public static ResourceLocation resource(String path) {
      return new ResourceLocation(MOD_ID, path);
   }

   public static class LootTables {
      public static final ResourceLocation ABANDONED_MINESHAFT_INJECT = ExposureExpanded.resource("chests/abandoned_mineshaft");
      public static final ResourceLocation ANCIENT_CITY_INJECT = ExposureExpanded.resource("chests/ancient_city");
      public static final ResourceLocation BASTION_OTHER_INJECT = ExposureExpanded.resource("chests/bastion_other");
      public static final ResourceLocation BURIED_TREASURE_INJECT = ExposureExpanded.resource("chests/buried_treasure");
      public static final ResourceLocation DESERT_PYRAMID_INJECT = ExposureExpanded.resource("chests/desert_pyramid");
      public static final ResourceLocation END_CITY_TREASURE_INJECT = ExposureExpanded.resource("chests/end_city_treasure");
      public static final ResourceLocation IGLOO_CHEST_INJECT = ExposureExpanded.resource("chests/igloo_chest");
      public static final ResourceLocation JUNGLE_TEMPLE_INJECT = ExposureExpanded.resource("chests/jungle_temple");
      public static final ResourceLocation NETHER_BRIDGE_INJECT = ExposureExpanded.resource("chests/nether_bridge");
      public static final ResourceLocation PILLAGER_OUTPOST_INJECT = ExposureExpanded.resource("chests/pillager_outpost");
      public static final ResourceLocation RUINED_PORTAL_INJECT = ExposureExpanded.resource("chests/ruined_portal");
      public static final ResourceLocation SHIPWRECK_SUPPLY_INJECT = ExposureExpanded.resource("chests/shipwreck_supply");
      public static final ResourceLocation SIMPLE_DUNGEON_INJECT = ExposureExpanded.resource("chests/simple_dungeon");
      public static final ResourceLocation TRIAL_CHAMBER_CORRIDOR_INJECT = ExposureExpanded.resource("chests/trial_chamber_corridor");
      public static final ResourceLocation UNDERWATER_RUIN_SMALL_INJECT = ExposureExpanded.resource("chests/underwater_ruin_small");
      public static final ResourceLocation VILLAGE_DESERT_HOUSE_INJECT = ExposureExpanded.resource("chests/village_desert_house");
      public static final ResourceLocation VILLAGE_PLAINS_HOUSE_INJECT = ExposureExpanded.resource("chests/village_plains_house");
      public static final ResourceLocation VILLAGE_SAVANNA_HOUSE_INJECT = ExposureExpanded.resource("chests/village_savanna_house");
      public static final ResourceLocation VILLAGE_SNOWY_HOUSE_INJECT = ExposureExpanded.resource("chests/village_snowy_house");
      public static final ResourceLocation VILLAGE_TEMPLE_INJECT = ExposureExpanded.resource("chests/village_temple");
      // upstream points this at abandoned_mineshaft too (unused either way)
      public static final ResourceLocation WOODLAND_MANSION_INJECT = ExposureExpanded.resource("chests/abandoned_mineshaft");
   }
}
