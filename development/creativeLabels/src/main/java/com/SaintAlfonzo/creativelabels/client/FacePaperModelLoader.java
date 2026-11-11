package com.SaintAlfonzo.creativelabels.client;

import com.SaintAlfonzo.creativelabels.CreativeLabels;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Geometry loader {@code creativelabels:face_paper}, referenced by {@code models/block/face_paper.json}. The JSON
 * carries only a {@code particle} texture (the plain note sprite); the geometry bakes to a {@link FacePaperModel}
 * with every note sprite resolved and Labels' {@code labels:block/label} model baked for the label paper.
 */
public class FacePaperModelLoader implements IGeometryLoader<FacePaperModelLoader.Geometry> {

    public static final String NAME = "face_paper";
    public static final FacePaperModelLoader INSTANCE = new FacePaperModelLoader();
    /** Labels' label paper model, registered as an additional model so it is loaded and its texture stitched. */
    public static final ResourceLocation LABEL_MODEL = new ResourceLocation(DonorArt.DONOR_LABELS, "block/label");

    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        return new Geometry();
    }

    public static class Geometry implements IUnbakedGeometry<Geometry> {

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter,
                               ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
            TextureAtlasSprite particle = spriteGetter.apply(context.getMaterial("particle"));
            Map<ResourceLocation, TextureAtlasSprite> sprites = new HashMap<>();
            for (ResourceLocation id : DonorArt.noteSprites()) {
                sprites.put(id, spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, id)));
            }
            return new FacePaperModel(particle, sprites, labelQuads(bakery, spriteGetter));
        }

        /** Labels' model quads (all sides) in its own model space; a hand-built square when the model is missing. */
        private static List<BakedQuad> labelQuads(ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter) {
            List<BakedQuad> quads = new ArrayList<>();
            try {
                BakedModel label = bakery.bake(LABEL_MODEL, BlockModelRotation.X0_Y0, spriteGetter);
                BakedModel missing = bakery.bake(ModelBakery.MISSING_MODEL_LOCATION, BlockModelRotation.X0_Y0, spriteGetter);
                if (label != null && label != missing) {
                    RandomSource rand = RandomSource.create(42);
                    quads.addAll(label.getQuads(null, null, rand));
                    for (Direction d : Direction.values()) quads.addAll(label.getQuads(null, d, rand));
                }
            } catch (RuntimeException e) {
                CreativeLabels.LOGGER.warn("Creative Labels: could not bake {}", LABEL_MODEL, e);
            }
            if (quads.isEmpty()) {
                CreativeLabels.LOGGER.warn("Creative Labels: {} unavailable, drawing the label paper as a plain square", LABEL_MODEL);
                quads.addAll(fallbackLabel(spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, DonorArt.LABEL_PAPER))));
            }
            return quads;
        }

        /** The same square as labels:block/label: x 3..13, y 3..13 at z 15.825 px, facing north, uv 3..13. */
        private static List<BakedQuad> fallbackLabel(TextureAtlasSprite sprite) {
            List<BakedQuad> out = new ArrayList<>();
            QuadBakingVertexConsumer baker = new QuadBakingVertexConsumer(out::add);
            double z = 15.825 / 16.0;
            // Seen from the north (viewer at -z, right = -x): u runs from x 13 to x 3.
            NoteShape.PlacedVertex[] v = {
                    new NoteShape.PlacedVertex(13 / 16.0, 13 / 16.0, z, 3 / 16f, 3 / 16f),
                    new NoteShape.PlacedVertex(13 / 16.0, 3 / 16.0, z, 3 / 16f, 13 / 16f),
                    new NoteShape.PlacedVertex(3 / 16.0, 3 / 16.0, z, 13 / 16f, 13 / 16f),
                    new NoteShape.PlacedVertex(3 / 16.0, 13 / 16.0, z, 13 / 16f, 3 / 16f)};
            LabelQuads.bake(baker, NoteShape.orient(v, new Vec3(0, 0, -1)), sprite, -1, true);
            return out;
        }

        @Override
        public Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter,
                                                 Set<Pair<String, String>> missingTextureErrors) {
            List<Material> materials = new ArrayList<>();
            materials.add(context.getMaterial("particle"));
            for (ResourceLocation id : DonorArt.noteSprites()) materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, id));
            materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, DonorArt.LABEL_PAPER));
            return materials;
        }
    }
}
