package umpaz.brewinandchewin.common.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.registries.RegisterEvent.RegisterHelper;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.CheeseWheelBlock;
import umpaz.brewinandchewin.common.block.CoasterBlock;
import umpaz.brewinandchewin.common.block.FieryFonduePotBlock;
import umpaz.brewinandchewin.common.block.HeatingCaskBlock;
import umpaz.brewinandchewin.common.block.IceCrateBlock;
import umpaz.brewinandchewin.common.block.KegBlock;
import umpaz.brewinandchewin.common.block.PizzaBlock;
import umpaz.brewinandchewin.common.block.UnripeCheeseWheelBlock;
import vectorwing.farmersdelight.common.block.PieBlock;

// Port: the fields are built when Forge fires the block RegisterEvent (the first touch of this class), as upstream's
// registerAll does on Fabric / NeoForge. Every registry class here follows the same pattern.
public class BnCBlocks {
   public static final Block KEG = new KegBlock(Properties.copy(Blocks.OAK_PLANKS));
   public static final Block HEATING_CASK = new HeatingCaskBlock(Properties.copy(Blocks.OAK_PLANKS));
   public static final Block ICE_CRATE = new IceCrateBlock(Properties.copy(Blocks.OAK_PLANKS));
   public static final Block COASTER = new CoasterBlock();
   public static final Block UNRIPE_FLAXEN_CHEESE_WHEEL = new UnripeCheeseWheelBlock(() -> BnCBlocks.FLAXEN_CHEESE_WHEEL, Properties.copy(Blocks.CAKE));
   public static final Block FLAXEN_CHEESE_WHEEL = new CheeseWheelBlock(() -> BnCItems.FLAXEN_CHEESE_WEDGE, Properties.copy(Blocks.CAKE));
   public static final Block UNRIPE_SCARLET_CHEESE_WHEEL = new UnripeCheeseWheelBlock(() -> BnCBlocks.SCARLET_CHEESE_WHEEL, Properties.copy(Blocks.CAKE));
   public static final Block SCARLET_CHEESE_WHEEL = new CheeseWheelBlock(() -> BnCItems.SCARLET_CHEESE_WEDGE, Properties.copy(Blocks.CAKE));
   public static final Block FIERY_FONDUE_POT = new FieryFonduePotBlock(Properties.copy(Blocks.CAULDRON));
   public static final Block PIZZA = new PizzaBlock(Properties.copy(Blocks.CAKE));
   public static final Block QUICHE = new PieBlock(Properties.copy(Blocks.CAKE), () -> BnCItems.QUICHE_SLICE);

   public static void registerAll(RegisterHelper<Block> helper) {
      helper.register(BrewinAndChewin.asResource("keg"), KEG);
      helper.register(BrewinAndChewin.asResource("heating_cask"), HEATING_CASK);
      helper.register(BrewinAndChewin.asResource("ice_crate"), ICE_CRATE);
      helper.register(BrewinAndChewin.asResource("coaster"), COASTER);
      helper.register(BrewinAndChewin.asResource("unripe_flaxen_cheese_wheel"), UNRIPE_FLAXEN_CHEESE_WHEEL);
      helper.register(BrewinAndChewin.asResource("flaxen_cheese_wheel"), FLAXEN_CHEESE_WHEEL);
      helper.register(BrewinAndChewin.asResource("unripe_scarlet_cheese_wheel"), UNRIPE_SCARLET_CHEESE_WHEEL);
      helper.register(BrewinAndChewin.asResource("scarlet_cheese_wheel"), SCARLET_CHEESE_WHEEL);
      helper.register(BrewinAndChewin.asResource("fiery_fondue_pot"), FIERY_FONDUE_POT);
      helper.register(BrewinAndChewin.asResource("pizza"), PIZZA);
      helper.register(BrewinAndChewin.asResource("quiche"), QUICHE);
   }
}
