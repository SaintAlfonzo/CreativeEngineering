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
// Port state: complete since P10 (component builders since P9a). Not ported: display source/target
// builder hooks (P8; Create 0.5.1 has no display source registry, see ModdedDisplaySources), Flywheel 1.0 visuals (not
// ported, plan section 0), and the datagen providers for component items and wire types (the jar ships upstream's
// generated output).
package org.patryk3211.powergrid;

import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractPowerGridRegistrate extends AbstractRegistrate<AbstractPowerGridRegistrate> {
    protected Function<Item, TooltipModifier> tooltipModifierFactory;

    protected AbstractPowerGridRegistrate(String modid) {
        super(modid);
    }

    // Phase P9a: circuit board components (registry powergrid:components, created in PowerGridImpl.newRegistry)
    public <T extends org.patryk3211.powergrid.circuits.components.Component> org.patryk3211.powergrid.circuits.components.ComponentBuilder<T, AbstractPowerGridRegistrate> component(String name, com.tterrag.registrate.util.nullness.NonNullFunction<org.patryk3211.powergrid.circuits.schematic.ComponentFootprint, T> factory) {
        return component(this.self(), name, factory);
    }

    public <T extends org.patryk3211.powergrid.circuits.components.Component, P> org.patryk3211.powergrid.circuits.components.ComponentBuilder<T, P> component(P parent, String name, com.tterrag.registrate.util.nullness.NonNullFunction<org.patryk3211.powergrid.circuits.schematic.ComponentFootprint, T> factory) {
        return this.entry(name, callback -> org.patryk3211.powergrid.circuits.components.ComponentBuilder.create(this, parent, name, callback, factory));
    }

    public AbstractPowerGridRegistrate setTooltipModifierFactory(Function<Item, TooltipModifier> factory) {
        this.tooltipModifierFactory = factory;
        return this.self();
    }

    @NotNull
    @Override
    public <T extends BlockEntity> PowerGridBlockEntityBuilder<T, AbstractPowerGridRegistrate> blockEntity(String name, BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return this.blockEntity(this.self(), name, factory);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity, P> PowerGridBlockEntityBuilder<T, P> blockEntity(P parent, String name, BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return (PowerGridBlockEntityBuilder<T, P>) this.entry(name, (callback) -> PowerGridBlockEntityBuilder.create(this, parent, name, callback, factory));
    }

    public static class PowerGridBlockEntityBuilder<T extends BlockEntity, P> extends BlockEntityBuilder<T, P> {
        private final Collection<NonNullSupplier<? extends Collection<NonNullSupplier<? extends Block>>>> deferredValidBlocks = new ArrayList<>();

        public static <T extends BlockEntity, P> BlockEntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
            return new PowerGridBlockEntityBuilder<>(owner, parent, name, callback, factory);
        }

        protected PowerGridBlockEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
            super(owner, parent, name, callback, factory);
        }

        public PowerGridBlockEntityBuilder<T, P> validBlocksDeferred(NonNullSupplier<? extends Collection<NonNullSupplier<? extends Block>>> blocks) {
            deferredValidBlocks.add(blocks);
            return this;
        }

        @Override
        protected @NotNull BlockEntityType<T> createEntry() {
            deferredValidBlocks.stream()
                    .map(Supplier::get)
                    .flatMap(Collection::stream)
                    .forEach(this::validBlock);
            return super.createEntry();
        }
    }
}
