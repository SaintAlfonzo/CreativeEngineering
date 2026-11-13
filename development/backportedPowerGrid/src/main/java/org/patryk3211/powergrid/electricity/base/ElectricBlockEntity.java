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
package org.patryk3211.powergrid.electricity.base;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.electricity.sim.AbstractElectricWire;
import org.patryk3211.powergrid.utility.sound.SoundScapes;

import java.util.List;

public abstract class ElectricBlockEntity extends SmartBlockEntity implements IElectricEntity {
    protected ElectricBehaviour electricBehaviour;
    @Nullable
    protected ThermalBehaviour thermalBehaviour;

    private float power;

    public ElectricBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setUnsaved() {
        if(level != null && !level.isClientSide) {
            level.blockEntityChanged(worldPosition);
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        electricBehaviour = specifyElectricBehaviour();
        if(electricBehaviour != null) {
            behaviours.add(electricBehaviour);
        }

        thermalBehaviour = specifyThermalBehaviour();
        if(thermalBehaviour != null) {
            behaviours.add(thermalBehaviour);
        }
    }

    public ElectricBehaviour specifyElectricBehaviour() {
        return new ElectricBehaviour(this);
    }

    @Nullable
    public ThermalBehaviour specifyThermalBehaviour() {
        return null;
    }

    protected void applyPower(@Nullable AbstractElectricWire wire) {
        if(wire != null)
            power += wire.power();
        if(thermalBehaviour != null)
            thermalBehaviour.applyWirePower(wire);
    }

    @Override
    public void tick() {
        if(!level.isClientSide || isVirtual())
            electricalTick();
        super.tick();
        if(level.isClientSide) {
            tickAudio();
        }
        power = 0;
    }

    public void electricalTick() {

    }

    public boolean isNoisy() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        if(!isNoisy() || thermalBehaviour == null)
            return;
        var percent = power / thermalBehaviour.maxPower();
        SoundScapes.play(SoundScapes.AmbienceGroup.HUM, worldPosition, 1, percent);
    }

    public ElectricBehaviour getElectricBehaviour() {
        return electricBehaviour;
    }

    @Override
    public void remove() {
        super.remove();
        if(electricBehaviour != null) {
            electricBehaviour.remove();
        }
    }

    // Port: registerAwardables / award / awardIfNear (advancement behaviour) removed; the pack drops advancements.
}
