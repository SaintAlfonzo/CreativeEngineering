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
package org.patryk3211.powergrid.ponder.base;

import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.instruction.TickingInstruction;
import org.patryk3211.powergrid.electricity.PonderElectricNetwork;

public class ElectricityTickInstruction extends TickingInstruction {
    public ElectricityTickInstruction(int ticks) {
        super(false, ticks);
    }

    // As upstream: tick() does not call TickingInstruction.tick, so the instruction never completes and keeps ticking the
    // ponder level's networks until the scene ends (every tickFor adds one more ticker). Same on Create 0.5.1, whose
    // TickingInstruction counts down in tick() as the Create 6 ponder library does. Kept (PORT_NOTES.md section 15).
    @Override
    public void tick(PonderScene scene) {
        PonderElectricNetwork.tickWorldNetworks(scene.getWorld());
    }
}
