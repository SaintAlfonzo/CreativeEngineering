package umpaz.brewinandchewin.common.registry;

import com.google.common.collect.Sets;
import java.util.LinkedHashSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.registries.RegisterEvent.RegisterHelper;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.item.BoozeItem;
import umpaz.brewinandchewin.common.item.DreadNogItem;
import umpaz.brewinandchewin.common.item.JamJarItem;
import umpaz.brewinandchewin.common.item.KegItem;
import vectorwing.farmersdelight.common.item.ConsumableItem;

public class BnCItems {
   public static LinkedHashSet<Item> CREATIVE_TAB_ITEMS = Sets.newLinkedHashSet();
   public static final Item KEG = new KegItem(BnCBlocks.KEG, props().stacksTo(1));
   public static final Item HEATING_CASK = new BlockItem(BnCBlocks.HEATING_CASK, props());
   public static final Item ICE_CRATE = new BlockItem(BnCBlocks.ICE_CRATE, props());
   public static final Item COASTER = new BlockItem(BnCBlocks.COASTER, props());
   public static final Item TANKARD = new Item(props());
   public static final Item BEER = new BoozeItem(() -> BnCFluids.BEER, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.BEER));
   public static final Item VODKA = new BoozeItem(() -> BnCFluids.VODKA, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.VODKA));
   public static final Item MEAD = new BoozeItem(() -> BnCFluids.MEAD, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.MEAD));
   public static final Item RICE_WINE = new BoozeItem(() -> BnCFluids.RICE_WINE, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.RICE_WINE));
   public static final Item PALE_JANE = new BoozeItem(() -> BnCFluids.PALE_JANE, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.PALE_JANE));
   public static final Item EGG_GROG = new BoozeItem(() -> BnCFluids.EGG_GROG, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.EGG_GROG));
   public static final Item GLITTERING_GRENADINE = new BoozeItem(
      () -> BnCFluids.GLITTERING_GRENADINE, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.GLITTERING_GRENADINE)
   );
   public static final Item SACCHARINE_RUM = new BoozeItem(
      () -> BnCFluids.SACCHARINE_RUM, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.SACCHARINE_RUM)
   );
   public static final Item SALTY_FOLLY = new BoozeItem(
      () -> BnCFluids.SALTY_FOLLY, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.SALTY_FOLLY)
   );
   public static final Item BLOODY_MARY = new BoozeItem(
      () -> BnCFluids.BLOODY_MARY, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.BLOODY_MARY)
   );
   public static final Item RED_RUM = new BoozeItem(() -> BnCFluids.RED_RUM, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.RED_RUM));
   public static final Item STRONGROOT_ALE = new BoozeItem(
      () -> BnCFluids.STRONGROOT_ALE, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.STRONGROOT_ALE)
   );
   public static final Item STEEL_TOE_STOUT = new BoozeItem(
      () -> BnCFluids.STEEL_TOE_STOUT, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.STEEL_TOE_STOUT)
   );
   public static final Item DREAD_NOG = new DreadNogItem(
      () -> BnCFluids.DREAD_NOG, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.DREAD_NOG)
   );
   public static final Item WITHERING_DROSS = new BoozeItem(
      () -> BnCFluids.WITHERING_DROSS, props().stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.WITHERING_DROSS)
   );
   public static final Item KOMBUCHA = new BoozeItem(() -> BnCFluids.KOMBUCHA, props("farmersrespite").stacksTo(16).craftRemainder(TANKARD).food(BnCFoods.KOMBUCHA));
   public static final Item UNRIPE_FLAXEN_CHEESE_WHEEL = new BlockItem(BnCBlocks.UNRIPE_FLAXEN_CHEESE_WHEEL, props().stacksTo(16));
   public static final Item FLAXEN_CHEESE_WHEEL = new BlockItem(BnCBlocks.FLAXEN_CHEESE_WHEEL, props().stacksTo(16));
   public static final Item FLAXEN_CHEESE_WEDGE = new Item(props().food(BnCFoods.FLAXEN_CHEESE));
   public static final Item UNRIPE_SCARLET_CHEESE_WHEEL = new BlockItem(BnCBlocks.UNRIPE_SCARLET_CHEESE_WHEEL, props().stacksTo(16));
   public static final Item SCARLET_CHEESE_WHEEL = new BlockItem(BnCBlocks.SCARLET_CHEESE_WHEEL, props().stacksTo(16));
   public static final Item SCARLET_CHEESE_WEDGE = new Item(props().food(BnCFoods.SCARLET_CHEESE));
   public static final Item VEGETABLE_OMELET = new ConsumableItem(
      props().stacksTo(16).food(BnCFoods.VEGETABLE_OMELET).craftRemainder(Items.BOWL), true
   );
   public static final Item CREAMY_ONION_SOUP = new ConsumableItem(
      props().stacksTo(16).food(BnCFoods.CREAMY_ONION_SOUP).craftRemainder(Items.BOWL), true
   );
   public static final Item CHEESY_PASTA = new ConsumableItem(props().stacksTo(16).food(BnCFoods.CHEESY_PASTA).craftRemainder(Items.BOWL), true);
   public static final Item HORROR_LASAGNA = new ConsumableItem(props().stacksTo(16).food(BnCFoods.HORROR_LASAGNA).craftRemainder(Items.BOWL), true);
   public static final Item SCARLET_PIEROGI = new ConsumableItem(props().stacksTo(16).food(BnCFoods.SCARLET_PIEROGI).craftRemainder(Items.BOWL), true);
   public static final Item FIERY_FONDUE_POT = new BlockItem(BnCBlocks.FIERY_FONDUE_POT, props().stacksTo(1));
   public static final Item FIERY_FONDUE = new ConsumableItem(props().stacksTo(16).food(BnCFoods.FIERY_FONDUE).craftRemainder(Items.BOWL), true);
   public static final Item PIZZA = new BlockItem(BnCBlocks.PIZZA, props().stacksTo(1));
   public static final Item QUICHE = new BlockItem(BnCBlocks.QUICHE, props());
   public static final Item PIZZA_SLICE = new Item(props().food(BnCFoods.PIZZA_SLICE));
   public static final Item QUICHE_SLICE = new Item(props().food(BnCFoods.QUICHE_SLICE));
   public static final Item HAM_AND_CHEESE_SANDWICH = new Item(props().food(BnCFoods.HAM_AND_CHEESE_SANDWICH));
   public static final Item KIMCHI = new ConsumableItem(props().food(BnCFoods.KIMCHI));
   public static final Item JERKY = new ConsumableItem(props().food(BnCFoods.JERKY));
   public static final Item PICKLED_PICKLES = new ConsumableItem(props().food(BnCFoods.PICKLED_PICKLES));
   public static final Item KIPPERS = new ConsumableItem(props().food(BnCFoods.KIPPERS));
   public static final Item COCOA_FUDGE = new ConsumableItem(props().food(BnCFoods.COCOA_FUDGE));
   public static final Item SWEET_BERRY_JAM = new JamJarItem(props().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).food(BnCFoods.SWEET_BERRY_JAM));
   public static final Item GLOW_BERRY_MARMALADE = new JamJarItem(
      props().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).food(BnCFoods.GLOW_BERRY_MARMALADE)
   );
   public static final Item APPLE_JELLY = new JamJarItem(props().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).food(BnCFoods.APPLE_JELLY));

   /** Port: 1.19.2 items name their creative tab in Properties; an item whose mod is missing gets none. */
   private static Properties props() {
      return props(null);
   }

   private static Properties props(@Nullable String requiredMod) {
      Properties properties = new Properties();
      if (requiredMod == null || BrewinAndChewin.getHelper().isModLoaded(requiredMod)) {
         properties.tab(BnCCreativeTabs.TAB_BREWIN_AND_CHEWIN);
      }
      return properties;
   }

   private static RegisterHelper<Item> helper;

   public static void registerWithTab(String name, Item item) {
      registerWithTab(name, item, null);
   }

   public static void registerWithTab(String name, Item item, @Nullable String requiredMod) {
      helper.register(BrewinAndChewin.asResource(name), item);
      if (requiredMod == null || BrewinAndChewin.getHelper().isModLoaded(requiredMod)) {
         CREATIVE_TAB_ITEMS.add(item);
      }
   }

   public static void registerAll(RegisterHelper<Item> registerHelper) {
      helper = registerHelper;
      registerWithTab("keg", KEG);
      registerWithTab("heating_cask", HEATING_CASK);
      registerWithTab("ice_crate", ICE_CRATE);
      registerWithTab("coaster", COASTER);
      registerWithTab("tankard", TANKARD);
      registerWithTab("beer", BEER);
      registerWithTab("vodka", VODKA);
      registerWithTab("mead", MEAD);
      registerWithTab("rice_wine", RICE_WINE);
      registerWithTab("pale_jane", PALE_JANE);
      registerWithTab("egg_grog", EGG_GROG);
      registerWithTab("glittering_grenadine", GLITTERING_GRENADINE);
      registerWithTab("saccharine_rum", SACCHARINE_RUM);
      registerWithTab("salty_folly", SALTY_FOLLY);
      registerWithTab("bloody_mary", BLOODY_MARY);
      registerWithTab("red_rum", RED_RUM);
      registerWithTab("strongroot_ale", STRONGROOT_ALE);
      registerWithTab("steel_toe_stout", STEEL_TOE_STOUT);
      registerWithTab("dread_nog", DREAD_NOG);
      registerWithTab("withering_dross", WITHERING_DROSS);
      registerWithTab("kombucha", KOMBUCHA, "farmersrespite");
      registerWithTab("unripe_flaxen_cheese_wheel", UNRIPE_FLAXEN_CHEESE_WHEEL);
      registerWithTab("flaxen_cheese_wheel", FLAXEN_CHEESE_WHEEL);
      registerWithTab("flaxen_cheese_wedge", FLAXEN_CHEESE_WEDGE);
      registerWithTab("unripe_scarlet_cheese_wheel", UNRIPE_SCARLET_CHEESE_WHEEL);
      registerWithTab("scarlet_cheese_wheel", SCARLET_CHEESE_WHEEL);
      registerWithTab("scarlet_cheese_wedge", SCARLET_CHEESE_WEDGE);
      registerWithTab("vegetable_omelet", VEGETABLE_OMELET);
      registerWithTab("creamy_onion_soup", CREAMY_ONION_SOUP);
      registerWithTab("cheesy_pasta", CHEESY_PASTA);
      registerWithTab("horror_lasagna", HORROR_LASAGNA);
      registerWithTab("scarlet_pierogi", SCARLET_PIEROGI);
      registerWithTab("fiery_fondue_pot", FIERY_FONDUE_POT);
      registerWithTab("fiery_fondue", FIERY_FONDUE);
      registerWithTab("pizza", PIZZA);
      registerWithTab("quiche", QUICHE);
      registerWithTab("pizza_slice", PIZZA_SLICE);
      registerWithTab("quiche_slice", QUICHE_SLICE);
      registerWithTab("ham_and_cheese_sandwich", HAM_AND_CHEESE_SANDWICH);
      registerWithTab("kimchi", KIMCHI);
      registerWithTab("jerky", JERKY);
      registerWithTab("pickled_pickles", PICKLED_PICKLES);
      registerWithTab("kippers", KIPPERS);
      registerWithTab("cocoa_fudge", COCOA_FUDGE);
      registerWithTab("sweet_berry_jam", SWEET_BERRY_JAM);
      registerWithTab("glow_berry_marmalade", GLOW_BERRY_MARMALADE);
      registerWithTab("apple_jelly", APPLE_JELLY);
      helper = null;
   }
}
