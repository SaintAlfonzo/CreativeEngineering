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
package org.patryk3211.powergrid.utility;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlock;
import org.patryk3211.powergrid.circuits.editor.CircuitDesignTableBlock;
import org.patryk3211.powergrid.electricity.electricswitch.HvSwitchBlock;
import org.patryk3211.powergrid.electricity.electricswitch.SwitchBlock;
import org.patryk3211.powergrid.electricity.fuse.FuseHolderBlock;
import org.patryk3211.powergrid.electricity.light.factorylight.FactoryLightBlock;
import org.patryk3211.powergrid.electricity.light.fixture.LightFixtureBlock;
import org.patryk3211.powergrid.electricity.transformer.NetherTransformerBlock;
import org.patryk3211.powergrid.electricity.transformer.TransformerMediumBlock;
import org.patryk3211.powergrid.electricity.transformer.TransformerSmallBlock;
import org.patryk3211.powergrid.general.ceilingtile.lamp.CeilingTileLampBlock;
import org.patryk3211.powergrid.kinetics.base.TunedBlock;
import org.patryk3211.powergrid.kinetics.generator.inductionrotor.VerticalCommutatorBlock;
import org.patryk3211.powergrid.kinetics.generator.rotor.AbstractRotorBlock;
import org.patryk3211.powergrid.kinetics.generator.winding.WindingBlock;

import java.util.function.Function;

/**
 * @see org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl
 * @see org.patryk3211.powergrid.utility.fabric.DataProviderUtilityImpl
 */
public class DataProviderUtility {
    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalBlock(String model) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.horizontalBlock(model);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalBlock(Function<BlockState, String> model) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.horizontalBlock(model);
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> alternateDirectionalBlock(String model) {
        return alternateDirectionalBlock($ -> model);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> alternateDirectionalBlock(Function<BlockState, String> modelProvider) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.alternateDirectionalBlock(modelProvider);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalAxisBlock(String model) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.horizontalAxisBlock(model);
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> surfaceSwitch(String baseName) {
        return surfaceBlock(state -> baseName + (state.getValue(SwitchBlock.OPEN) ? "_off" : "_on"));
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> surfaceBlock(String baseName) {
        return surfaceBlock(state -> baseName);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> surfaceBlock(Function<BlockState, String> baseName) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.surfaceBlock(baseName);
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> tunedBlock(String based, String baseless) {
        return horizontalBlock(state -> state.getValue(TunedBlock.BASE) ? based : baseless);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static NonNullBiConsumer<DataGenContext<Block, LightFixtureBlock>, RegistrateBlockstateProvider> lightFixture(String baseName) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.lightFixture(baseName);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static NonNullBiConsumer<DataGenContext<Block, FactoryLightBlock>, RegistrateBlockstateProvider> factoryLight(String baseFolder) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.factoryLight(baseFolder);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static NonNullBiConsumer<DataGenContext<Block, CeilingTileLampBlock>, RegistrateBlockstateProvider> ceilingTileLamp(String baseFolder) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.ceilingTileLamp(baseFolder);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> housing(String name) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.housing(name);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> basinHeater(String baseName) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.basinHeater(baseName);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends AbstractRotorBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> rotorModel(String name) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.rotorModel(name);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends VerticalCommutatorBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> verticalCommutator(String name) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.verticalCommutator(name);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends WindingBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> windingModel() {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.windingModel();
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends HvSwitchBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> hvSwitch(String baseName) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.hvSwitch(baseName);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static NonNullBiConsumer<DataGenContext<Block, TransformerSmallBlock>, RegistrateBlockstateProvider> transformerSmall() {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.transformerSmall();
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static NonNullBiConsumer<DataGenContext<Block, TransformerMediumBlock>, RegistrateBlockstateProvider> transformerMedium() {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.transformerMedium();
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static NonNullBiConsumer<DataGenContext<Block, NetherTransformerBlock>, RegistrateBlockstateProvider> transformerNether() {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.transformerNether();
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> downFacing(String name) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.downFacing(name);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> upFacing(String name) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.upFacing(name);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static NonNullBiConsumer<DataGenContext<Block, CircuitBoardBlock>, RegistrateBlockstateProvider> circuitBoard() {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.circuitBoard();
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static NonNullBiConsumer<DataGenContext<Block, CircuitDesignTableBlock>, RegistrateBlockstateProvider> circuitDesignTable() {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.circuitDesignTable();
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static NonNullBiConsumer<DataGenContext<Block, FuseHolderBlock>, RegistrateBlockstateProvider> fuseHolder() {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.fuseHolder();
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> cubeColumn(String side, String end) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.cubeColumn(side, end);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> cubeAllWithItem(String name) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.cubeAllWithItem(name);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> simple(String model) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.simple(model);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> carbonPile(String baseName) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.carbonPile(baseName);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> generated() {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.generated();
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> gauge(String model, String texture) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.gauge(model, texture);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> itemWithParent(String parent) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.itemWithParent(parent);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> blockItem() {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.blockItem();
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> barrier() {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.barrier();
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> northFacing(String name) {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.northFacing(name);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DataProviderUtilityImpl
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> air() {
        return org.patryk3211.powergrid.utility.forge.DataProviderUtilityImpl.air();
    }
}
