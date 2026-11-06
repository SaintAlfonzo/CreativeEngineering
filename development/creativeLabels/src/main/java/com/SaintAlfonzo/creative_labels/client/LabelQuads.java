package com.SaintAlfonzo.creative_labels.client;

import com.SaintAlfonzo.creative_labels.block.Spots;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Labels' {@code labels:block/label} model moved onto a face (BRIEF 4 "Sprites"). That model is one north-facing
 * 10 x 10 px square at z = 15.825 px: paper on a host to the SOUTH of its cell, 0.175 px in front of the cell
 * boundary, seen by a viewer to the north. In our terms that is face SOUTH at inset 0 (frame x = west, y = up).
 *
 * <p>{@link #map} takes a point of that model (cell-local blocks) to the frame of the target face: fx = (1 - x) * 16,
 * fy = y * 16, depth = z - 1 (+ the face's inset); x is mirrored within the label square on a right-handed frame
 * (UP faces) so the mapping stays a proper rotation. Normals go through the same linear map and the winding is
 * checked ({@link NoteShape#orient}).
 *
 * <p>Vertex data layout (DefaultVertexFormat.BLOCK): 8 ints per vertex: x y z, colour, u v, light, normal.
 */
public final class LabelQuads {

    public static final int STRIDE = 8;

    private LabelQuads() {}

    /** A point of the label model (cell-local blocks, SOUTH-face frame) on the target face. */
    public static Vec3 map(Vec3 p, Direction face, @Nullable Direction rotation, float inset) {
        return map(p, face, rotation, Spots.Place.at(inset));
    }

    /** As above, then moved and scaled onto the label's square ({@link Spots.Place#labelX}); depth is not scaled. */
    public static Vec3 map(Vec3 p, Direction face, @Nullable Direction rotation, Spots.Place place) {
        boolean mirror = NoteShape.mirrored(face, rotation);
        double fx = (1.0 - p.x) * 16.0;
        if (mirror) fx = 16.0 - fx;
        return Spots.local(face, rotation, place.labelX(fx), place.labelY(p.y * 16.0), p.z - 1.0 + place.inset());
    }

    /** A direction of the label model on the target face (same linear map, no origin). */
    public static Vec3 mapNormal(Vec3 n, Direction face, @Nullable Direction rotation) {
        boolean mirror = NoteShape.mirrored(face, rotation);
        Direction xa = Spots.xAxis(face, rotation), ya = Spots.yAxis(face, rotation);
        double fx = mirror ? n.x : -n.x, fy = n.y, fz = n.z;
        return new Vec3(fx * xa.getStepX() + fy * ya.getStepX() + fz * face.getStepX(),
                fx * xa.getStepY() + fy * ya.getStepY() + fz * face.getStepY(),
                fx * xa.getStepZ() + fy * ya.getStepZ() + fz * face.getStepZ());
    }

    /** The model's quads moved onto a face. */
    public static List<BakedQuad> place(List<BakedQuad> model, Direction face, @Nullable Direction rotation, float inset) {
        return place(model, face, rotation, Spots.Place.at(inset));
    }

    public static List<BakedQuad> place(List<BakedQuad> model, Direction face, @Nullable Direction rotation, Spots.Place place) {
        List<BakedQuad> out = new ArrayList<>(model.size());
        QuadBakingVertexConsumer baker = new QuadBakingVertexConsumer(out::add);
        for (BakedQuad quad : model) {
            int[] data = quad.getVertices();
            if (data.length < STRIDE * 4) continue;
            NoteShape.PlacedVertex[] v = new NoteShape.PlacedVertex[4];
            for (int i = 0; i < 4; i++) {
                int o = i * STRIDE;
                Vec3 p = new Vec3(Float.intBitsToFloat(data[o]), Float.intBitsToFloat(data[o + 1]), Float.intBitsToFloat(data[o + 2]));
                Vec3 q = map(p, face, rotation, place);
                v[i] = new NoteShape.PlacedVertex(q.x, q.y, q.z, Float.intBitsToFloat(data[o + 4]), Float.intBitsToFloat(data[o + 5]));
            }
            Vec3 modelNormal = unpackNormal(data[7]);
            if (modelNormal.lengthSqr() < 1e-6) modelNormal = Vec3.atLowerCornerOf(quad.getDirection().getNormal());
            NoteShape.PlacedQuad placed = NoteShape.orient(v, mapNormal(modelNormal, face, rotation));
            bake(baker, placed, quad.getSprite(), -1, false); // the baked model's UVs are atlas coordinates already
        }
        return out;
    }

    /** The packed normal of a BLOCK-format vertex (signed bytes / 127). */
    public static Vec3 unpackNormal(int packed) {
        return new Vec3(((byte) (packed & 0xFF)) / 127.0, ((byte) (packed >> 8 & 0xFF)) / 127.0, ((byte) (packed >> 16 & 0xFF)) / 127.0);
    }

    /**
     * Emits a placed quad (already oriented) as a baked quad: white vertex colour, the given sprite's UVs (texture
     * coordinates in 0..1 of the whole sprite image), the quad's normal, shade by its nearest direction.
     */
    public static void bake(QuadBakingVertexConsumer baker, NoteShape.PlacedQuad quad, TextureAtlasSprite sprite, int tintIndex,
                            boolean spriteUv) {
        Vec3 n = quad.normal();
        baker.setSprite(sprite);
        baker.setTintIndex(tintIndex);
        baker.setDirection(Direction.getNearest(n.x, n.y, n.z));
        baker.setShade(true);
        baker.setHasAmbientOcclusion(false);
        for (NoteShape.PlacedVertex v : quad.v()) {
            float u = spriteUv ? sprite.getU(v.u() * 16.0) : v.u();
            float vv = spriteUv ? sprite.getV(v.v() * 16.0) : v.v();
            baker.vertex(v.x(), v.y(), v.z()).color(255, 255, 255, 255).uv(u, vv).uv2(0, 0)
                    .normal((float) n.x, (float) n.y, (float) n.z).endVertex();
        }
    }
}
