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
// Port state: phases P2 (wires and cord), P7 (zap projectile) and P10 (string light cord): every upstream entity.
package org.patryk3211.powergrid.collections;

import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.world.entity.MobCategory;
import org.patryk3211.powergrid.PortPhase;
import org.patryk3211.powergrid.electricity.wire.BlockWireEntity;
import org.patryk3211.powergrid.electricity.wire.BlockWireRenderer;
import org.patryk3211.powergrid.electricity.wire.HangingWireEntity;
import org.patryk3211.powergrid.electricity.wire.HangingWireRenderer;
import org.patryk3211.powergrid.electricity.wire.powercord.CordEntity;
import org.patryk3211.powergrid.electricity.wire.powercord.CordRenderer;
import org.patryk3211.powergrid.equipment.zapper.ZapProjectileEntity;
import org.patryk3211.powergrid.equipment.zapper.ZapProjectileRenderer;
import org.patryk3211.powergrid.utility.EntityProperties;

import static org.patryk3211.powergrid.PowerGrid.REGISTRATE;

public class ModdedEntities {
    public static final EntityEntry<HangingWireEntity> HANGING_WIRE =
            REGISTRATE.entity(PortPhase.require("entity", "hanging_wire"), HangingWireEntity::new, MobCategory.MISC)
                    .renderer(() -> HangingWireRenderer::new)
                    .register();
    public static final EntityEntry<BlockWireEntity> BLOCK_WIRE =
            REGISTRATE.entity(PortPhase.require("entity", "block_wire"), BlockWireEntity::new, MobCategory.MISC)
                    .renderer(() -> BlockWireRenderer::new)
                    .register();
    public static final EntityEntry<CordEntity> CORD_ENTITY =
            REGISTRATE.entity(PortPhase.require("entity", "cord"), CordEntity::new, MobCategory.MISC)
                    .renderer(() -> CordRenderer::new)
                    .register();

    // Phase P10
    public static final EntityEntry<org.patryk3211.powergrid.electricity.light.string.StringLightCordEntity> STRING_LIGHT_CORD =
            REGISTRATE.entity(PortPhase.require("entity", "string_light_cord"), org.patryk3211.powergrid.electricity.light.string.StringLightCordEntity::new, MobCategory.MISC)
                    .renderer(() -> org.patryk3211.powergrid.electricity.light.string.StringLightCordRenderer::new)
                    .register();

    // Phase P7
    public static final EntityEntry<ZapProjectileEntity> ZAP_PROJECTILE =
            REGISTRATE.entity(PortPhase.require("entity", "zap_projectile"), ZapProjectileEntity::new, MobCategory.MISC)
                    .transform(EntityProperties.apply(b -> b
                            .dimensions(0.25f, 0.25f)
                            .trackRangeChunks(4)
                            .trackedUpdateRate(20)
                            .forceTrackedVelocityUpdates(true)
                    ))
                    .renderer(() -> ZapProjectileRenderer::new)
                    .register();

    @SuppressWarnings("EmptyMethod")
    public static void register() { /* Initialize static fields. */ }
}
