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
package org.patryk3211.powergrid.collections;

import com.simibubi.create.foundation.config.ConfigBase;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.config.*;
import org.patryk3211.powergrid.forge.PowerGridImpl;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @see com.simibubi.create.infrastructure.config.AllConfigs
 */
public class ModdedConfigs {
    public static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

    private static CServer server;
    private static CCommon common;
    private static CClient client;

    public static CServer server() {
        return server;
    }

    public static CCommon common() {
        return common;
    }

    public static CClient client() {
        return client;
    }

    public static ConfigBase byType(ModConfig.Type type) {
        return CONFIGS.get(type);
    }

    private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
        Pair<T, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(builder -> {
            T config = factory.get();
            config.registerAll(builder);
            return config;
        });

        T config = specPair.getLeft();
        config.specification = specPair.getRight();
        CONFIGS.put(side, config);
        return config;
    }

    // @ExpectPlatform inlined (D2): body of the forge ModdedConfigsImpl
    public static void registerPlatform() {
        for(Map.Entry<ModConfig.Type, ConfigBase> pair : CONFIGS.entrySet())
            PowerGridImpl.context.registerConfig(pair.getKey(), pair.getValue().specification);
    }

    public static void register() {
        server = register(CServer::new, ModConfig.Type.SERVER);
        common = register(CCommon::new, ModConfig.Type.COMMON);
        client = register(CClient::new, ModConfig.Type.CLIENT);

        registerPlatform();

        // Port: Create 6 registers two providers (BlockStressValues.IMPACTS / CAPACITIES). Create 0.5.1 takes one
        // provider per mod namespace (since P3: the HV switch and HV breaker have a stress impact). Generator speeds
        // (BlockStressValues.setGeneratorSpeed upstream, the motors and the servo) come from CStress.setGeneratorSpeed (P4).
        var stress = server.kinetics.stressValues;
        com.simibubi.create.content.kinetics.BlockStressValues.registerProvider(PowerGrid.MOD_ID,
                new com.simibubi.create.content.kinetics.BlockStressValues.IStressValueProvider() {
                    @Override
                    public double getImpact(net.minecraft.world.level.block.Block block) {
                        var value = stress.getImpact(block);
                        return value == null ? 0 : value.getAsDouble();
                    }

                    @Override
                    public double getCapacity(net.minecraft.world.level.block.Block block) {
                        var value = stress.getCapacity(block);
                        return value == null ? 0 : value.getAsDouble();
                    }

                    @Override
                    public boolean hasImpact(net.minecraft.world.level.block.Block block) {
                        return stress.getImpact(block) != null;
                    }

                    @Override
                    public boolean hasCapacity(net.minecraft.world.level.block.Block block) {
                        return stress.getCapacity(block) != null;
                    }

                    @Override
                    public com.simibubi.create.foundation.utility.Couple<Integer> getGeneratedRPM(net.minecraft.world.level.block.Block block) {
                        return org.patryk3211.powergrid.config.CStress.getGeneratedRPM(block);
                    }
                });

        ResistanceValues.register(server.electricity.resistance);
        ThermalValues.register(server.electricity.thermal);
    }

    public static void onLoad(ModConfig modConfig) {
        for(ConfigBase config : CONFIGS.values())
            if(config.specification == modConfig.getSpec())
                config.onLoad();
    }

    public static void onReload(ModConfig modConfig) {
        for(ConfigBase config : CONFIGS.values())
            if(config.specification == modConfig.getSpec())
                config.onReload();
    }

    public static boolean logsEnabled() {
        return common.lotsOfLogs.get();
    }
}
