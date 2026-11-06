package com.SaintAlfonzo.creative_labels.client;

import com.SaintAlfonzo.creative_labels.CreativeLabels;
import com.SaintAlfonzo.creative_labels.block.Spots;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Sticky It's curled note polygons (BRIEF 4 "Note shape"), captured at runtime from its entity layer
 * {@code sticky_it:model_sticky} and placed into a spot rectangle of a face frame.
 *
 * <p><b>Model space</b> (what the capture yields, px): the entity model's frame with an identity pose: x = the
 * viewer's right when the viewer stands in front of the note (the entity faces -z), y DOWN, z away from the
 * viewer. The head part sits at (0, 23.34, -1.17): the flat 6 x 3 px face at y 18..21 on the plane z = 0.6 (max z,
 * the back of the entity: against the wall), then a 20 degree fold (0.94 px), a 45 degree fold (1.41 px) and a 1 px
 * lip at 90 degrees, ending at y = 23.35, z = -2.16. The texture (16 x 16) is sampled on u 0..6 px for the
 * viewer-facing polygons and u 6..12 (transparent in the donor files) for the back polygons.
 *
 * <p><b>Placement</b> ({@link #place}): model x -> frame x (the spot's left edge + x), model y -> frame y from the
 * spot's top edge downward (the lip hangs toward y = 0, i.e. down on walls and toward the placing player on floors
 * and ceilings), model z -> depth: the flat face on the paper surface ({@code inset}), the lip
 * {@link Spots#NOTE_DEPTH} px out toward the viewer. On a face whose frame is right-handed (UP: a viewer below sees
 * the frame's x axis mirrored) x is mirrored within the spot so the shape is a proper rotation of the wall one and
 * its texture reads the same way. Winding is checked against the transformed normal and reversed when a mapping
 * flips it (block render types cull back faces; entity ones did not).
 *
 * <p>Thread-safe: the capture is a synchronized lazy init (chunk builds run on worker threads), cleared on resource
 * reload. When the layer is missing or looks wrong the {@link #fallback()} constants (the same numbers) are used.
 */
public final class NoteShape {

    /** A captured vertex: model px (y down) and texture coordinates 0..1. */
    public record Vertex(float x, float y, float z, float u, float v) {}

    /** A captured polygon: four vertices and the model-space normal of its front. */
    public record Poly(Vertex[] v, float nx, float ny, float nz) {}

    /** The whole note: polygons and their bounds (px). */
    public record Shape(List<Poly> polys, float minX, float maxX, float minY, float maxY, float minZ, float maxZ,
                        boolean captured) {
        public float width() {
            return maxX - minX;
        }

        public float height() {
            return maxY - minY;
        }

        public float depth() {
            return maxZ - minZ;
        }
    }

    /** A placed vertex: cell-local blocks and texture coordinates 0..1 (of the whole 16 x 16 texture). */
    public record PlacedVertex(double x, double y, double z, float u, float v) {}

    /** A placed quad: counter-clockwise seen from its front, with the unit normal of that front (world axes). */
    public record PlacedQuad(PlacedVertex[] v, Vec3 normal) {}

    public static final String LAYER_NAMESPACE = "sticky_it", LAYER_PATH = "model_sticky", LAYER_LAYER = "main";
    public static final String PART = "head";

    /** Expected size (px); a capture off by more than {@link #TOLERANCE} falls back to the constants. */
    public static final float EXPECT_W = 6f, EXPECT_H = 5.354f, EXPECT_D = 2.756f, TOLERANCE = 1.5f;

    private static final float EPS = 1e-6f;

    @Nullable
    private static volatile Shape cached;

    private NoteShape() {}

    /** The note shape: captured once from the donor layer, else the fallback. Safe from any thread. */
    public static Shape get() {
        Shape s = cached;
        if (s == null) {
            synchronized (NoteShape.class) {
                s = cached;
                if (s == null) {
                    s = captureOrFallback();
                    cached = s;
                }
            }
        }
        return s;
    }

    /** Resource reload: the next {@link #get} captures again (the donor layer may have been re-registered). */
    public static void invalidate() {
        cached = null;
    }

    private static Shape captureOrFallback() {
        try {
            Shape s = capture();
            if (s != null) {
                CreativeLabels.LOGGER.info("Creative Labels: note shape captured from {}:{} ({} polygons, {} x {} x {} px)",
                        LAYER_NAMESPACE, LAYER_PATH, s.polys.size(), fmt(s.width()), fmt(s.height()), fmt(s.depth()));
                return s;
            }
        } catch (RuntimeException e) {
            CreativeLabels.LOGGER.warn("Creative Labels: note shape capture failed, using the fallback constants", e);
        }
        return fallback();
    }

    private static String fmt(float f) {
        return String.format("%.2f", f);
    }

    // ---------------------------------------------------------------------------------------------------- capture

    /** Renders the donor layer's head part into a capturing consumer. Client thread or chunk worker, no GL. */
    @Nullable
    static Shape capture() {
        ModelLayerLocation layer = new ModelLayerLocation(new ResourceLocation(LAYER_NAMESPACE, LAYER_PATH), LAYER_LAYER);
        ModelPart root;
        try {
            root = Minecraft.getInstance().getEntityModels().bakeLayer(layer);
        } catch (IllegalArgumentException missing) {
            CreativeLabels.LOGGER.warn("Creative Labels: entity layer {} not registered, using the fallback note shape", layer);
            return null;
        }
        ModelPart head = root.hasChild(PART) ? root.getChild(PART) : root;
        Capture capture = new Capture();
        head.render(new PoseStack(), capture, 0, 0);
        List<Poly> polys = capture.finish();
        if (polys.isEmpty()) {
            CreativeLabels.LOGGER.warn("Creative Labels: entity layer {} produced no polygons, using the fallback note shape", layer);
            return null;
        }
        Shape s = of(polys, true);
        if (Math.abs(s.width() - EXPECT_W) > TOLERANCE || Math.abs(s.height() - EXPECT_H) > TOLERANCE
                || Math.abs(s.depth() - EXPECT_D) > TOLERANCE) {
            CreativeLabels.LOGGER.warn("Creative Labels: captured note shape is {} x {} x {} px (expected {} x {} x {}), using the fallback",
                    fmt(s.width()), fmt(s.height()), fmt(s.depth()), EXPECT_W, EXPECT_H, EXPECT_D);
            return null;
        }
        return s;
    }

    /** Collects vertices from {@link ModelPart#render}; drops the zero-area edge polygons of flat cubes. */
    private static final class Capture implements VertexConsumer {
        private final List<Poly> polys = new ArrayList<>();
        private final Vertex[] current = new Vertex[4];
        private int index;
        private float x, y, z, u, v, nx, ny, nz;

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.x = (float) (x * 16.0);
            this.y = (float) (y * 16.0);
            this.z = (float) (z * 16.0);
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            this.u = u;
            this.v = v;
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            nx = x;
            ny = y;
            nz = z;
            return this;
        }

        @Override
        public void endVertex() {
            current[index++] = new Vertex(x, y, z, u, v);
            if (index == 4) {
                index = 0;
                Vertex[] quad = current.clone();
                if (area(quad) > EPS) polys.add(new Poly(quad, nx, ny, nz));
            }
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {}

        @Override
        public void unsetDefaultColor() {}

        List<Poly> finish() {
            return polys;
        }
    }

    /** Twice the area of the triangle (a, b, c) plus (a, c, d); zero for the edge faces of a flat cube. */
    static float area(Vertex[] q) {
        float ax = q[1].x - q[0].x, ay = q[1].y - q[0].y, az = q[1].z - q[0].z;
        float bx = q[2].x - q[0].x, by = q[2].y - q[0].y, bz = q[2].z - q[0].z;
        float cx = q[3].x - q[0].x, cy = q[3].y - q[0].y, cz = q[3].z - q[0].z;
        return cross(ax, ay, az, bx, by, bz) + cross(bx, by, bz, cx, cy, cz);
    }

    private static float cross(float ax, float ay, float az, float bx, float by, float bz) {
        float x = ay * bz - az * by, y = az * bx - ax * bz, z = ax * by - ay * bx;
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    static Shape of(List<Poly> polys, boolean captured) {
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
        for (Poly p : polys) {
            for (Vertex v : p.v) {
                minX = Math.min(minX, v.x);
                maxX = Math.max(maxX, v.x);
                minY = Math.min(minY, v.y);
                maxY = Math.max(maxY, v.y);
                minZ = Math.min(minZ, v.z);
                maxZ = Math.max(maxZ, v.z);
            }
        }
        return new Shape(List.copyOf(polys), minX, maxX, minY, maxY, minZ, maxZ, captured);
    }

    // --------------------------------------------------------------------------------------------------- fallback

    /**
     * The viewer-facing polygons of ModelSticky from its numbers (px, y down, z away from the viewer): the flat face
     * y 18..21 at z 0.6, the 20 degree fold to (21.94, 0.26), the 45 degree fold to (23.35, -1.16), the lip to
     * z -2.16. Texture rows: flat 0..3, 45 degree fold 3..5, 20 degree fold 5..6, lip 6..7; columns 0..6.
     */
    public static Shape fallback() {
        List<Poly> polys = new ArrayList<>();
        polys.add(strip(18f, 0.6f, 21f, 0.6f, 0f, 3f));
        polys.add(strip(21f, 0.6f, 21.9397f, 0.258f, 5f, 6f));
        polys.add(strip(21.9397f, 0.258f, 23.3539f, -1.1562f, 3f, 5f));
        polys.add(strip(23.3539f, -1.1562f, 23.3539f, -2.1562f, 6f, 7f));
        return of(polys, false);
    }

    /** A 6 px wide strip between two edges (top y0/z0, bottom y1/z1) with texture rows v0..v1 (px), front toward -z. */
    private static Poly strip(float y0, float z0, float y1, float z1, float v0, float v1) {
        Vertex a = new Vertex(3f, y0, z0, 6f / 16f, v0 / 16f);
        Vertex b = new Vertex(-3f, y0, z0, 0f, v0 / 16f);
        Vertex c = new Vertex(-3f, y1, z1, 0f, v1 / 16f);
        Vertex d = new Vertex(3f, y1, z1, 6f / 16f, v1 / 16f);
        float ax = b.x - a.x, ay = b.y - a.y, az = b.z - a.z;
        float bx = c.x - a.x, by = c.y - a.y, bz = c.z - a.z;
        float nx = ay * bz - az * by, ny = az * bx - ax * bz, nz = ax * by - ay * bx;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        return new Poly(new Vertex[]{a, b, c, d}, nx / len, ny / len, nz / len);
    }

    // -------------------------------------------------------------------------------------------------- placement

    /** Whether the face frame is right-handed (x axis cross y axis points along the face): true for UP faces. */
    public static boolean mirrored(Direction face, @Nullable Direction rotation) {
        Direction xa = Spots.xAxis(face, rotation), ya = Spots.yAxis(face, rotation);
        int cx = xa.getStepY() * ya.getStepZ() - xa.getStepZ() * ya.getStepY();
        int cy = xa.getStepZ() * ya.getStepX() - xa.getStepX() * ya.getStepZ();
        int cz = xa.getStepX() * ya.getStepY() - xa.getStepY() * ya.getStepX();
        return cx * face.getStepX() + cy * face.getStepY() + cz * face.getStepZ() > 0;
    }

    /** Frame vector (x px, y px, depth blocks) to a world-axis vector (no origin). */
    private static Vec3 frameVector(Direction face, @Nullable Direction rotation, double fx, double fy, double depth) {
        Direction xa = Spots.xAxis(face, rotation), ya = Spots.yAxis(face, rotation);
        return new Vec3(fx * xa.getStepX() + fy * ya.getStepX() + depth * face.getStepX(),
                fx * xa.getStepY() + fy * ya.getStepY() + depth * face.getStepY(),
                fx * xa.getStepZ() + fy * ya.getStepZ() + depth * face.getStepZ());
    }

    /**
     * Places the shape on a spot of a face: cell-local quads, the flat face on the paper surface at {@code inset},
     * the curl hanging toward the frame's y = 0.
     */
    public static List<PlacedQuad> place(Shape s, Direction face, @Nullable Direction rotation, int spot, float inset) {
        return place(s, face, rotation, Spots.RECTS[spot], inset);
    }

    /** As above on any note rectangle in the face frame (a shifted spot, {@link Spots.Place#noteRect}). */
    public static List<PlacedQuad> place(Shape s, Direction face, @Nullable Direction rotation, float[] rect, float inset) {
        // Scale into the spot's own rectangle (6 x 5.35 px, the centre spot 5.34): the note fills it exactly, so it
        // always sits inside Spots.noteBox; depth keeps the width's scale (1:1 for a true capture).
        float sx = (rect[2] - rect[0]) / Math.max(EPS, s.width());
        float sy = (rect[3] - rect[1]) / Math.max(EPS, s.height());
        float sz = sx;
        boolean mirror = mirrored(face, rotation);
        List<PlacedQuad> out = new ArrayList<>(s.polys.size());
        for (Poly p : s.polys) {
            PlacedVertex[] v = new PlacedVertex[4];
            for (int i = 0; i < 4; i++) {
                Vertex m = p.v[i];
                double fx = mirror ? rect[2] - (m.x - s.minX) * sx : rect[0] + (m.x - s.minX) * sx;
                double fy = rect[3] - (m.y - s.minY) * sy;
                double depth = inset - (s.maxZ - m.z) * sz / 16.0;
                Vec3 q = Spots.local(face, rotation, fx, fy, depth);
                v[i] = new PlacedVertex(q.x, q.y, q.z, m.u, m.v);
            }
            Vec3 expected = frameVector(face, rotation, mirror ? -p.nx : p.nx, -p.ny, p.nz);
            out.add(orient(v, expected));
        }
        return out;
    }

    /** Makes the quad counter-clockwise seen from {@code expected}'s side and returns it with its unit normal. */
    public static PlacedQuad orient(PlacedVertex[] v, Vec3 expected) {
        Vec3 g = geometricNormal(v);
        if (g.dot(expected) < 0) {
            v = new PlacedVertex[]{v[3], v[2], v[1], v[0]};
            g = geometricNormal(v);
        }
        return new PlacedQuad(v, g);
    }

    public static Vec3 geometricNormal(PlacedVertex[] v) {
        Vec3 a = new Vec3(v[1].x - v[0].x, v[1].y - v[0].y, v[1].z - v[0].z);
        Vec3 b = new Vec3(v[2].x - v[0].x, v[2].y - v[0].y, v[2].z - v[0].z);
        Vec3 c = new Vec3(v[3].x - v[0].x, v[3].y - v[0].y, v[3].z - v[0].z);
        Vec3 n = a.cross(b).add(b.cross(c));
        double len = n.length();
        return len < 1e-9 ? new Vec3(0, 1, 0) : n.scale(1 / len);
    }
}
