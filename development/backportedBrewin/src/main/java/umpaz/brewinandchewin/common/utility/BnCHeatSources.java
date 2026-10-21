package umpaz.brewinandchewin.common.utility;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.tag.BnCTags;

/** Port-only helpers for KegBlockEntity.updateTemperature (alcohol.md 2.7). */
public final class BnCHeatSources {
   /**
    * Boolean "running" properties: vanilla/modded furnaces and braziers ({@code lit}), Immersive Engineering machines
    * and the AE2 vibration chamber ({@code active}, IE sets it on the multiblock's master block only), PneumaticCraft
    * compressors ({@code on}).
    */
   private static final Set<String> RUNNING_PROPERTIES = Set.of("lit", "active", "on");
   /** Blaze heat levels that mean "no fuel": Create's NONE/SMOULDERING, Enchantment Industry's SMOULDERING. */
   private static final Set<String> IDLE_BLAZE = Set.of("none", "smouldering");

   private BnCHeatSources() {
   }

   /**
    * A block with a running property (see RUNNING_PROPERTIES) counts only while it's true; one without it always counts
    * (upstream's `lit` rule, widened to machines; for both heat and freeze sources).
    */
   public static boolean isActive(BlockState state) {
      for (Property<?> property : state.getProperties()) {
         if (property instanceof BooleanProperty running && RUNNING_PROPERTIES.contains(property.getName()) && !state.getValue(running)) {
            return false;
         }
      }
      return true;
   }

   /**
    * A blaze heater with no fuel isn't a heat source: Create's Blaze Burner and Create Crafts & Additions' Liquid Blaze
    * Burner idle at NONE/SMOULDERING, Create Enchantment Industry's Blaze Enchanter at SMOULDERING (KINDLED and up once
    * its tank holds experience). None of them has a `lit` property, so they'd otherwise always count. Matches the
    * `blaze` property by name, so it needs no Create classes. Replaces Create Central Kitchen's 2.0 keg mixin.
    */
   public static BlockState withoutIdleBurner(BlockState state) {
      for (Property<?> property : state.getProperties()) {
         if (property.getName().equals("blaze") && IDLE_BLAZE.contains(valueName(state, property))) {
            return Blocks.AIR.defaultBlockState();
         }
      }
      return state;
   }

   /**
    * Heat minus cold around any position, counted exactly like KegBlockEntity.updateTemperature (5x5x5 sources, biome,
    * dimension). The keg keeps its own copy of the loop because Create Central Kitchen's mixin pins its shape.
    */
   public static int environmentTemperature(Level level, BlockPos center) {
      int range = KegBlockEntity.RANGE;
      int heat = 0;
      int cold = 0;
      for (BlockPos pos : BlockPos.betweenClosed(center.offset(-range, -range, -range), center.offset(range, range, range))) {
         BlockState source = withoutIdleBurner(level.getBlockState(pos));
         if (source.is(BnCTags.Blocks.HEAT_SOURCES) && isActive(source)) {
            heat++;
         }
         if (source.is(BnCTags.Blocks.FREEZE_SOURCES) && isActive(source)) {
            cold++;
         }
      }

      BnCConfiguration.Common.Keg keg = BnCConfiguration.COMMON_CONFIG.get().keg();
      if (keg.biomeTemp()) {
         Holder<Biome> biome = level.getBiome(center);
         if (biome.isBound()) {
            float biomeTemperature = biome.value().getBaseTemperature();
            if (biomeTemperature <= 0.0F) {
               cold++;
            } else if (biomeTemperature == 2.0F) {
               heat++;
            }
         }
      }

      int temp = heat - cold;
      if (keg.dimTemp() && level.dimensionType().ultraWarm()) {
         temp += 2;
      }
      return temp;
   }

   /**
    * -1 when chilly or colder, 1 when warm or hotter (the keg's thresholds), else 0. Vinery's wine storage calls this
    * by reflection; keep the signature.
    */
   public static int climate(Level level, BlockPos pos) {
      BnCConfiguration.Common.Keg keg = BnCConfiguration.COMMON_CONFIG.get().keg();
      int temp = environmentTemperature(level, pos);
      if (temp <= -keg.chilly()) {
         return -1;
      }
      return temp >= keg.warm() ? 1 : 0;
   }

   private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
      return property.getName(state.getValue(property));
   }
}
