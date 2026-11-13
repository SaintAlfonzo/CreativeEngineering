/*
 * Copyright 2025 patryk3211
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
// Port: the forge implementation (collections/forge/ModdedFluidsImpl) is folded in (D2). Phase P9c (9.3): the acid fluid,
// its block and bucket. Registrate MC1.19-1.1.5 has the same FluidType builder as 1.20; the data-generation calls (tag,
// lang) are left out, the jar ships upstream's generated tag, lang, blockstate and models.
package org.patryk3211.powergrid.collections;

import com.simibubi.create.AllFluids;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.patryk3211.powergrid.PortPhase;
import org.patryk3211.powergrid.PowerGrid;

import static org.patryk3211.powergrid.PowerGrid.REGISTRATE;

public class ModdedFluids {
    // Registers fluid "acid" (source), "flowing_acid", block "acid" and item "acid_bucket" (Registrate's names).
    public static final FluidEntry<ForgeFlowingFluid.Flowing> ACID =
            REGISTRATE.fluid(PortPhase.require("fluid", "acid"),
                            PowerGrid.asResource("block/acid_still"),
                            PowerGrid.asResource("block/acid_flow"),
                            AcidFluidType::new)
                    .source(ForgeFlowingFluid.Source::new)
                        .bucket()
                        .build()
                    .transform(translucent())
                    .register();

    private static <T extends ForgeFlowingFluid, P> NonNullUnaryOperator<FluidBuilder<T, P>> translucent() {
        return b -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                b.renderType(() -> RenderType.translucent());
            });
            return b;
        };
    }

    public static Fluid acid() {
        return ACID.getSource();
    }

    public static Fluid acidFlowing() {
        return ACID.get();
    }

    @SuppressWarnings("EmptyMethod")
    public static void register() { /* Initialize static fields. */ }

    public static class AcidFluidType extends AllFluids.TintedFluidType {
        public AcidFluidType(Properties properties, net.minecraft.resources.ResourceLocation still, net.minecraft.resources.ResourceLocation flowing) {
            super(properties, still, flowing);
        }

        @Override
        protected int getTintColor(FluidStack stack) {
            return 0xFFFFFFFF;
        }

        @Override
        protected int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
            return 0xFFFFFFFF;
        }

        @Override
        public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
            // Port: entity.level() -> the 1.19.2 field.
            if(entity.level.random.nextInt(15) >= 10) {
                entity.hurt(ModdedDamageTypes.ACID.simpleDamageSource(entity.level), 2);
            }
            return false;
        }
    }
}
