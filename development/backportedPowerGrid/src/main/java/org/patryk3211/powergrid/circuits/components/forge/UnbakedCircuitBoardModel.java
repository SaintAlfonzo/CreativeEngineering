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
package org.patryk3211.powergrid.circuits.components.forge;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class UnbakedCircuitBoardModel implements IUnbakedGeometry<UnbakedCircuitBoardModel> {
    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBakery bakery, Function<Material, TextureAtlasSprite> textureProvider, ModelState state, ItemOverrides overrides, ResourceLocation modelLocation) {
        return new CircuitBoardModel(
                bakery.bake(CircuitBoardModel.BASE_MODEL, state),
                textureProvider.apply(CircuitBoardModel.PAD_SPRITE_ID),
                textureProvider.apply(CircuitBoardModel.COPPER_SPRITE_ID)
        );
    }

    // Port: 1.19.2 stitches the sprites a geometry lists here (upstream: 1.19.3+ atlases/blocks.json, which does not
    // ship). The base model is a RegisterAdditional model, so its own textures are collected with it.
    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return List.of(CircuitBoardModel.PAD_SPRITE_ID, CircuitBoardModel.COPPER_SPRITE_ID);
    }
}
