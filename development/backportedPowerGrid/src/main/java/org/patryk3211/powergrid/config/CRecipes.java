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

public class CRecipes extends ConfigBase {
    public final ConfigFloat lightningMagnetizationChance = f(0.05f, 0, 1, "lightningMagnetizationChance", Comments.lightningMagnetizationChance);

    @Override
    public String getName() {
        return "recipes";
    }

    private static class Comments {
        // Port: fix B7 (upstream said iron ingot; EntityMixin checks andesite alloy)
        public static final String lightningMagnetizationChance = "Chance for lightning strike to turn a single andesite alloy into a magnet";
    }
}
