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
// Create 6 PonderLevel (ponder library, common code) -> Create 0.5.1 PonderWorld (client class); only ponder code calls this.
package org.patryk3211.powergrid.electricity;

import com.simibubi.create.foundation.ponder.PonderWorld;

public class PonderElectricNetwork extends GlobalElectricNetworks {
    public static void tickWorldNetworks(PonderWorld world) {
        GlobalElectricNetworks.preTick(world);
    }

    public static void removeWorldEntry(PonderWorld world) {
        worldNetworks.remove(world);
    }
}
