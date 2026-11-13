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
// Port: Create 6 registers fan processing types through a registry (CreateRegistries.FAN_PROCESSING_TYPE); Create 0.5.1
// has the static FanProcessingTypeRegistry (a plain map and a sorted list). Called once from the common setup's
// enqueueWork (main thread, after Create's own types), as the pack's dragon_breathing type does.
package org.patryk3211.powergrid.electricity.heater;

import com.simibubi.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import org.patryk3211.powergrid.PowerGrid;

public class HeaterFanProcessingTypes {
    public static final HeaterBlastingType HEATER_BLASTING = new HeaterBlastingType();
    public static final HeaterSmokingType HEATER_SMOKING = new HeaterSmokingType();

    public static void register() {
        FanProcessingTypeRegistry.register(PowerGrid.asResource("heater_blasting"), HEATER_BLASTING);
        FanProcessingTypeRegistry.register(PowerGrid.asResource("heater_smoking"), HEATER_SMOKING);
    }
}
