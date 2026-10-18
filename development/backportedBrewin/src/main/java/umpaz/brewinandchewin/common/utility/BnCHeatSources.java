package umpaz.brewinandchewin.common.utility;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

/** Port-only helpers for KegBlockEntity.updateTemperature (alcohol.md 2.7). */
public final class BnCHeatSources {
   private BnCHeatSources() {
   }

   /**
    * A block with a `lit` property counts only while lit; one without it always counts (upstream rule, for both heat
    * and freeze sources).
    */
   public static boolean isActive(BlockState state) {
      return !state.hasProperty(BlockStateProperties.LIT) || state.getValue(BlockStateProperties.LIT);
   }

   /**
    * A Create Blaze Burner with no blaze fuel (heat level NONE) isn't a heat source: it has no `lit` property, so it
    * would otherwise always count. Matches Create's `blaze` property by name, so it needs no Create classes and also
    * covers add-on burners that reuse the property. Replaces Create Central Kitchen's 2.0 keg mixin.
    */
   public static BlockState withoutIdleBurner(BlockState state) {
      for (Property<?> property : state.getProperties()) {
         if (property.getName().equals("blaze") && "none".equals(valueName(state, property))) {
            return Blocks.AIR.defaultBlockState();
         }
      }
      return state;
   }

   private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
      return property.getName(state.getValue(property));
   }
}
