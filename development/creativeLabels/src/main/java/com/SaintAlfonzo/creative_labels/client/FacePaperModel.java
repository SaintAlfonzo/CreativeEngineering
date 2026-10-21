package com.SaintAlfonzo.creative_labels.client;

import com.SaintAlfonzo.creative_labels.block.FaceContent;
import com.SaintAlfonzo.creative_labels.block.FaceSnapshot;
import com.SaintAlfonzo.creative_labels.block.Spots;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The face_paper block model (BRIEF 4): quads are built per chunk rebuild from the {@link #FACES} snapshot the block
 * entity exposes through {@code getModelData()}: Sticky It's curled note ({@link NoteShape}) on every occupied spot
 * and Labels' paper ({@link LabelQuads}) on every LABEL face. Chunk builds run on worker threads: everything read
 * here is immutable ({@link FaceSnapshot}, the sprite map, the label model's quads) or thread-safe (the quad cache,
 * {@link NoteShape#get()}).
 *
 * <p>Quads for one piece depend only on (face, rotation, spot, inset, sprite, tinted), so they are cached per such
 * key: a face full of notes costs five map lookups per rebuild. A dyed note's quads carry tint index
 * {@code face.ordinal() * 5 + spot} and the greyscale sprite; the block colour handler turns the index into the
 * note's RGB. Everything else is untinted (-1).
 */
public class FacePaperModel implements IDynamicBakedModel {

    /** Alias of {@link FaceSnapshot#FACES}: six snapshots, index = Direction ordinal. */
    public static final ModelProperty<FaceSnapshot[]> FACES = FaceSnapshot.FACES;

    private static final ChunkRenderTypeSet RENDER_TYPES = ChunkRenderTypeSet.of(RenderType.cutout());

    private final TextureAtlasSprite particle;
    private final Map<ResourceLocation, TextureAtlasSprite> sprites;
    private final List<BakedQuad> labelModel;
    private final Map<Key, List<BakedQuad>> cache = new ConcurrentHashMap<>();

    /** Cache key of one placed piece; {@code spot} -1 = the label. */
    private record Key(Direction face, @Nullable Direction rotation, int spot, int insetBits, @Nullable ResourceLocation sprite,
                       boolean tinted) {}

    /**
     * @param particle   the plain note sprite (particle icon)
     * @param sprites    every note sprite of {@link DonorArt#noteSprites()} resolved on the block atlas
     * @param labelModel Labels' label paper quads in their own model space (see {@link LabelQuads}); may be empty
     */
    public FacePaperModel(TextureAtlasSprite particle, Map<ResourceLocation, TextureAtlasSprite> sprites, List<BakedQuad> labelModel) {
        this.particle = particle;
        this.sprites = Map.copyOf(sprites);
        this.labelModel = List.copyOf(labelModel);
    }

    /** Tint index of a dyed note: face ordinal * 5 + spot; -1 for everything else. */
    public static int tintIndex(Direction face, int spot) {
        return face.ordinal() * Spots.COUNT + spot;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    ModelData data, @Nullable RenderType renderType) {
        if (side != null) return List.of();
        FaceSnapshot[] faces = data.get(FACES);
        if (faces == null) return List.of();
        List<BakedQuad> out = new ArrayList<>();
        for (Direction face : Direction.values()) {
            FaceSnapshot snap = faces[face.ordinal()];
            if (snap == null || snap.isEmpty()) continue;
            if (snap.kind() == FaceContent.Kind.LABEL) {
                out.addAll(labelQuads(face, snap.rotation(), snap.inset()));
                continue;
            }
            for (int spot = 0; spot < Spots.COUNT; spot++) {
                FaceSnapshot.NoteLook look = snap.note(spot);
                if (look == null) continue;
                out.addAll(noteQuads(face, snap.rotation(), spot, snap.inset(), look));
            }
        }
        return out;
    }

    private List<BakedQuad> noteQuads(Direction face, @Nullable Direction rotation, int spot, float inset, FaceSnapshot.NoteLook look) {
        ResourceLocation sprite = DonorArt.noteSprite(look.color(), look.special(), look.written());
        boolean tinted = DonorArt.tinted(look.color(), look.special());
        Key key = new Key(face, rotation, spot, Float.floatToIntBits(inset), sprite, tinted);
        return cache.computeIfAbsent(key, k -> buildNote(k, sprite, tinted));
    }

    private List<BakedQuad> buildNote(Key k, ResourceLocation spriteId, boolean tinted) {
        TextureAtlasSprite sprite = sprites.get(spriteId);
        if (sprite == null) sprite = particle;
        float inset = Float.intBitsToFloat(k.insetBits);
        List<BakedQuad> quads = new ArrayList<>();
        QuadBakingVertexConsumer baker = new QuadBakingVertexConsumer(quads::add);
        int tint = tinted ? tintIndex(k.face, k.spot) : -1;
        for (NoteShape.PlacedQuad q : NoteShape.place(NoteShape.get(), k.face, k.rotation, k.spot, inset)) {
            LabelQuads.bake(baker, q, sprite, tint, true);
        }
        return List.copyOf(quads);
    }

    private List<BakedQuad> labelQuads(Direction face, @Nullable Direction rotation, float inset) {
        Key key = new Key(face, rotation, -1, Float.floatToIntBits(inset), null, false);
        return cache.computeIfAbsent(key, k -> List.copyOf(LabelQuads.place(labelModel, k.face, k.rotation, Float.intBitsToFloat(k.insetBits))));
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return RENDER_TYPES;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
