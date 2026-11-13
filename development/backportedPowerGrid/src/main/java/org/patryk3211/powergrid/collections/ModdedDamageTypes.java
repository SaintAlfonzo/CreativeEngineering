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
// Port: damage types are data-driven from 1.19.4 on (data/powergrid/damage_type, deleted in P0). On 1.19.2 each one is a
// DamageSource with the same message id, so the death messages use upstream's lang keys (death.attack.<id>[.player]).
// Upstream's types have exhaustion 0 and no damage type tags, so these sources keep armor and cause no exhaustion.
package org.patryk3211.powergrid.collections;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public enum ModdedDamageTypes {
    OVERLOADED_MACHINE("overloaded_machine"),
    ZAP("zap"),
    LIVE_WIRE_CUTTING("live_wire_cutting"),
    ELECTROCUTION("electrocution"),
    SPINNING_ROTOR("spinning_rotor"),
    ACID("acid");

    /** Message id of the damage type (upstream's {@code message_id}). */
    public final String messageId;
    private final DamageSource source;

    ModdedDamageTypes(String id) {
        this.messageId = id;
        this.source = new PowerGridDamageSource(id);
    }

    public DamageSource simpleDamageSource(Level level) {
        return source;
    }

    public DamageSource simpleDamageSource(MinecraftServer server) {
        return source;
    }

    public DamageSource simpleDamageSource() {
        return source;
    }

    /**
     * Phase P7: a source with a direct entity and an owner (upstream {@code new DamageSource(holder, direct, owner)}, the
     * Electro-Zapper's bolt). 1.19.2's {@link IndirectEntityDamageSource} gives the same death message keys
     * ({@code death.attack.<id>}, {@code .item}); upstream's zap type has {@code scaling: never}, {@code exhaustion: 0}
     * and is in no damage type tag (not a projectile for armor enchantments).
     */
    public DamageSource indirect(Entity direct, Entity owner) {
        return new IndirectEntityDamageSource(messageId, direct, owner) {
            @Override
            public float getFoodExhaustion() {
                return 0;
            }

            @Override
            public boolean scalesWithDifficulty() {
                return false;
            }
        };
    }

    /** Plain damage source with no exhaustion (upstream's damage types have {@code exhaustion: 0}). */
    public static class PowerGridDamageSource extends DamageSource {
        public PowerGridDamageSource(String messageId) {
            super(messageId);
        }

        @Override
        public float getFoodExhaustion() {
            return 0;
        }
    }
}
