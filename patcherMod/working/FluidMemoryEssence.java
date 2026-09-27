package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class FluidMemoryEssence {
   public static final PNCFluidRenderProps RENDER_PROPS = new PNCFluidRenderProps("memory_essence_still", "memory_essence_flow", -3080448);

   private static ForgeFlowingFluid.Properties props() {
      return (new ForgeFlowingFluid.Properties(ModFluids.MEMORY_ESSENCE_FLUID_TYPE, ModFluids.MEMORY_ESSENCE, ModFluids.MEMORY_ESSENCE_FLOWING)).block(ModBlocks.MEMORY_ESSENCE).bucket(ModItems.MEMORY_ESSENCE_BUCKET).tickRate(2);
   }
}
