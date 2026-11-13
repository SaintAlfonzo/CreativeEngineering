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
// Create 6 PonderPlugin (ponder library) -> Create 0.5.1: no plugin interface. register() runs on the client setup
// (PowerGridClientImpl, FMLClientSetupEvent enqueueWork) and fills Create 0.5.1's PonderRegistry directly. Shared text and
// tag titles come from the lang file (powergrid.ponder.shared.* / powergrid.ponder.tag.*, shipped whole); they are also
// registered as default lang for Create's ponder editing mode. onPonderLevelRestore: 0.5.1 PonderWorld has no restore
// hook, so every Power Grid storyboard starts with an instruction that does the same (PowerGridPonderScenes.wrap).
package org.patryk3211.powergrid.ponder;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import com.simibubi.create.foundation.ponder.PonderScene;
import org.patryk3211.powergrid.PortPhase;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.base.ElectricBehaviour;

public class PowerGridPonderPlugin {
    /** Port phase that brought ponder (plan_power_grid.md P5). */
    public static final int PONDER_PHASE = 5;
    private static boolean registered = false;

    public static void register() {
        if(registered || PortPhase.CURRENT < PONDER_PHASE)
            return;
        registered = true;
        registerSharedText();
        PowerGridPonderTags.register();
        PowerGridPonderScenes.register();
    }

    private static void registerSharedText() {
        PonderLocalization.registerShared(PowerGrid.asResource("gauge_range"), "You can change the gauge's range by clicking on top of it");
        PonderLocalization.registerShared(PowerGrid.asResource("gauge_goggles"), "When wearing Engineers' Goggles, the player can get more detailed information from the Gauge");
        PonderLocalization.registerShared(PowerGrid.asResource("gauge_customize"), "You can change how the measured value is displayed by right-clicking the gauge with a name tag");
    }

    public static void onPonderLevelRestore(PonderScene scene) {
        for(var be : scene.getWorld().getBlockEntities()) {
            var electric = BlockEntityBehaviour.get(be, ElectricBehaviour.TYPE);
            if(electric != null)
                electric.unpause();
        }
    }
}
