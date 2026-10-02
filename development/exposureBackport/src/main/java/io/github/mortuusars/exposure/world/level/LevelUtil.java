package io.github.mortuusars.exposure.world.level;

import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class LevelUtil {
   public static int getLightLevelAt(Level level, BlockPos pos) {
      level.updateSkyBrightness();
      int skyBrightness = level.getBrightness(LightLayer.SKY, pos);
      int blockBrightness = level.getBrightness(LightLayer.BLOCK, pos);
      return skyBrightness < 15
         ? Math.max(blockBrightness, (int)((float)skyBrightness * ((float)(15 - level.getSkyDarken()) / 15.0F)))
         : Math.max(blockBrightness, 15 - level.getSkyDarken());
   }

   public static List<ResourceLocation> getStructuresAt(ServerLevel level, BlockPos pos) {
      Registry<Structure> registry = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
      return level.structureManager().getAllStructuresAt(pos).keySet().stream().filter(structure -> {
         StructureStart structureStart = level.structureManager().getStructureAt(pos, structure);
         return structureStart.isValid();
      }).<ResourceLocation>map(registry::getKey).filter(Objects::nonNull).toList();
   }
}
