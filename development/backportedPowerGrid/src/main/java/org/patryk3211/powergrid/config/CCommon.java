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
package org.patryk3211.powergrid.config;

import com.simibubi.create.foundation.config.ConfigBase;

public class CCommon extends ConfigBase {
    public final ConfigBool lotsOfLogs = b(false, "lotsOfLogs", Comments.lotsOfLogs);
    public final ConfigInt stateSynchronization = i(100, 0, "fullStateSynchronizationInterval", Comments.stateSynchronization);
    public final ConfigBool syncWithDoubles = b(true, "syncWithDoubles", Comments.syncWithDoubles);
    // Port (power_grid.md H5, D14): switch for measuring the client mirror against server-sent values. Read from the
    // server's copy (WorldNetworks.postTick): false stops the per-tick state stream to clients, so client values come
    // only from the full state sync every fullStateSynchronizationInterval ticks.
    public final ConfigBool clientMirror = b(true, "clientMirror", Comments.clientMirror);
    public final ConfigBool allocateUnpooledBuffers = b(false, "allocateUnpooledBuffers");

    @Override
    public String getName() {
        return "common";
    }

    private static class Comments {
        public static final String lotsOfLogs = "Enables extensive logging in different segments of the mod (can cause larger log files and log spam)";
        public static final String stateSynchronization = "Periodic state synchronization sent by the server. This option makes sure that the clients are always close to the server simulation state, it will send the NBT data of all block entities in an electrical network to all clients (0 = disabled)";
        public static final String clientMirror = "Stream electrical state to clients every few ticks so their copy of each network stays live (upstream behaviour). If false (read on the server), clients get values only from the full state synchronization, every fullStateSynchronizationInterval ticks";
        public static final String syncWithDoubles = "Synchronize network with double precision numbers (this will double the amount of data that needs to be sent)";
    }
}
