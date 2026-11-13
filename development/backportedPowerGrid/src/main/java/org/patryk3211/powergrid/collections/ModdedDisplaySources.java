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
// Port: Create 0.5.1 has no display source registry; sources register in AllDisplayBehaviours and blocks are assigned
// with AllDisplayBehaviours.assignBlock in ModdedBlocks. Only the sources of ported phases are here (battery P3,
// generator clutch P4).
package org.patryk3211.powergrid.collections;

import com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours;
import com.simibubi.create.content.redstone.displayLink.source.DisplaySource;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.battery.BatteryDisplaySource;
import org.patryk3211.powergrid.electricity.gauge.ElectricGaugeDisplaySource;
import org.patryk3211.powergrid.equipment.thermometer.ThermometerDisplaySource;
import org.patryk3211.powergrid.kinetics.generator.clutch.ClutchDisplaySource;

public class ModdedDisplaySources {
    public static final ElectricGaugeDisplaySource ELECTRIC_GAUGE = register("electric_gauge", new ElectricGaugeDisplaySource());
    public static final ThermometerDisplaySource THERMOMETER = register("thermometer", new ThermometerDisplaySource());
    public static final BatteryDisplaySource BATTERY = register("battery", new BatteryDisplaySource());
    // Phase P4
    public static final ClutchDisplaySource CLUTCH = register("clutch", new ClutchDisplaySource());

    private static <T extends DisplaySource> T register(String name, T source) {
        AllDisplayBehaviours.register(PowerGrid.asResource(name), source);
        return source;
    }

    public static void register() { }
}
