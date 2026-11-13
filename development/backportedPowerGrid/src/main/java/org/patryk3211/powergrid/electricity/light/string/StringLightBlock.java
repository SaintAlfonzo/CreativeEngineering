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
package org.patryk3211.powergrid.electricity.light.string;

import net.minecraft.world.level.block.AirBlock;

// Port: 1.20 Properties.replaceable() / pushReaction(DESTROY) are Material properties on 1.19.2: ModdedBlocks gives this
// block FactoryLightLightBlock.MATERIAL (no collider, replaceable, destroyed when pushed), as for the factory light's
// light block.
public class StringLightBlock extends AirBlock {
    public StringLightBlock(Properties properties) {
        super(properties.lightLevel(state -> 5)
                .noCollission()
                .noLootTable()
                .noOcclusion()
                .air());
    }
}
