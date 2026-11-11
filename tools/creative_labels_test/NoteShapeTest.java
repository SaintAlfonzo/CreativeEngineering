import com.SaintAlfonzo.creativelabels.block.Spots;
import com.SaintAlfonzo.creativelabels.client.DonorArt;
import com.SaintAlfonzo.creativelabels.client.LabelQuads;
import com.SaintAlfonzo.creativelabels.client.NoteShape;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Headless checks for the phase-1 client geometry (no GL, no Minecraft instance): the fallback note shape and its
 * placement on every face (bounds, paper plane, curl direction, winding), the label model mapping, and DonorArt's
 * image maths on the real donor PNGs (from DecompiledMods; nothing is written).
 */
public class NoteShapeTest {
    static int ok = 0, failed = 0;
    static final double EPS = 1e-4;

    static void check(String name, boolean cond) {
        if (cond) { ok++; System.out.println("ok   " + name); } else { failed++; System.out.println("FAIL " + name); }
    }

    static void checkNear(String name, double a, double b, double eps) {
        check(name + " (" + a + " ~ " + b + ")", Math.abs(a - b) <= eps);
    }

    static boolean inBox(AABB box, NoteShape.PlacedVertex v, double eps) {
        return v.x() >= box.minX - eps && v.x() <= box.maxX + eps && v.y() >= box.minY - eps && v.y() <= box.maxY + eps
                && v.z() >= box.minZ - eps && v.z() <= box.maxZ + eps;
    }

    public static void main(String[] args) throws Exception {
        net.minecraft.SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        fallback();
        placement();
        labels();
        donorArt();
        System.out.println(failed == 0 ? "ALL " + ok + " checks passed" : failed + " FAILED, " + ok + " ok");
        System.exit(failed == 0 ? 0 : 1);
    }

    static void fallback() {
        System.out.println("== fallback shape");
        NoteShape.Shape s = NoteShape.fallback();
        check("4 polygons", s.polys().size() == 4);
        checkNear("width 6", s.width(), 6.0, 0.001);
        checkNear("height 5.354", s.height(), 5.354, 0.01);
        checkNear("depth 2.756", s.depth(), 2.756, 0.01);
        check("not captured", !s.captured());
        NoteShape.Poly flat = s.polys().get(0);
        checkNear("flat face normal -z", flat.nz(), -1.0, 0.001);
        check("flat face at max z (the paper plane)", Math.abs(flat.v()[0].z() - s.maxZ()) < 0.001);
        check("flat face at min y (the top; y is down)", Math.abs(flat.v()[0].y() - s.minY()) < 0.001);
        NoteShape.Poly lip = s.polys().get(3);
        checkNear("lip normal -y (faces up)", lip.ny(), -1.0, 0.001);
        check("lip reaches min z (the tip toward the viewer)", Math.abs(Math.min(lip.v()[2].z(), lip.v()[0].z()) - s.minZ()) < 0.001);
        for (NoteShape.Poly p : s.polys()) {
            check("front faces toward the viewer or up: ny <= 0, nz <= 0", p.ny() <= 1e-6 && p.nz() <= 1e-6);
            check("uv in the 6 px strip", p.v()[0].u() <= 6f / 16f + 1e-6 && p.v()[1].u() >= -1e-6);
        }
    }

    static void placement() {
        System.out.println("== placement");
        NoteShape.Shape s = NoteShape.fallback();
        for (Direction face : Direction.values()) {
            Direction[] rots = face.getAxis().isVertical() ? new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST} : new Direction[]{null};
            for (Direction rot : rots) {
                check("mirrored only on UP: " + face + "/" + rot, NoteShape.mirrored(face, rot) == (face == Direction.UP));
                for (float inset : new float[]{0f, 1f / 16f, 0.5f}) {
                    for (int spot = 0; spot < Spots.COUNT; spot++) {
                        String tag = face + "/" + rot + "/" + spot + "/" + inset;
                        List<NoteShape.PlacedQuad> quads = NoteShape.place(s, face, rot, spot, inset);
                        AABB box = Spots.noteBox(face, rot, spot, inset);
                        boolean allIn = true, windingOk = true;
                        for (NoteShape.PlacedQuad q : quads) {
                            for (NoteShape.PlacedVertex v : q.v()) allIn &= inBox(box, v, 0.002);
                            Vec3 g = NoteShape.geometricNormal(q.v());
                            windingOk &= g.dot(q.normal()) > 0.999;
                        }
                        check("inside noteBox " + tag, allIn);
                        check("winding = normal " + tag, windingOk);
                        // The flat face (first polygon) lies on the paper plane and faces the viewer (out of the host).
                        NoteShape.PlacedQuad flat = quads.get(0);
                        Vec3 out = Vec3.atLowerCornerOf(face.getOpposite().getNormal());
                        check("flat face normal toward viewer " + tag, flat.normal().dot(out) > 0.999);
                        boolean onPlane = true, topEdge = false, bottomEdge = false, leftEdge = false, rightEdge = false;
                        float[] r = Spots.RECTS[spot];
                        for (NoteShape.PlacedVertex v : flat.v()) {
                            double[] f = Spots.frame(face, rot, new Vec3(v.x(), v.y(), v.z()));
                            onPlane &= Math.abs(f[2] - inset) < EPS;
                            topEdge |= Math.abs(f[1] - r[3]) < EPS;
                            leftEdge |= Math.abs(f[0] - r[0]) < EPS;
                            rightEdge |= Math.abs(f[0] - r[2]) < EPS;
                        }
                        check("flat face on the paper plane " + tag, onPlane);
                        check("flat face touches the spot's top edge " + tag, topEdge);
                        check("flat face spans the spot's width " + tag, leftEdge && rightEdge);
                        // The lip (last polygon) hangs at the spot's bottom edge and sticks out NOTE_DEPTH.
                        NoteShape.PlacedQuad lip = quads.get(quads.size() - 1);
                        double minDepth = Double.MAX_VALUE;
                        for (NoteShape.PlacedVertex v : lip.v()) {
                            double[] f = Spots.frame(face, rot, new Vec3(v.x(), v.y(), v.z()));
                            bottomEdge |= Math.abs(f[1] - r[1]) < EPS;
                            minDepth = Math.min(minDepth, f[2]);
                        }
                        check("lip at the spot's bottom edge (curl toward y = 0) " + tag, bottomEdge);
                        checkNear("lip tip " + Spots.NOTE_DEPTH + " px out " + tag, minDepth, inset - 2.756 / 16.0, 0.001);
                        Vec3 up = Vec3.atLowerCornerOf(Spots.yAxis(face, rot).getNormal());
                        check("lip front faces the frame's +y " + tag, lip.normal().dot(up) > 0.999);
                    }
                }
            }
        }
        // Texture orientation: on a wall, u increases toward the frame's +x (viewer's right), v toward -y (down).
        List<NoteShape.PlacedQuad> q = NoteShape.place(s, Direction.NORTH, null, Spots.C, 0f);
        NoteShape.PlacedVertex a = q.get(0).v()[0], b = q.get(0).v()[1];
        double[] fa = Spots.frame(Direction.NORTH, null, new Vec3(a.x(), a.y(), a.z()));
        double[] fb = Spots.frame(Direction.NORTH, null, new Vec3(b.x(), b.y(), b.z()));
        check("u grows with frame x on walls", (a.u() - b.u()) * (fa[0] - fb[0]) > 0);
        // And on the ceiling (mirrored frame) the same holds against the viewer's right, which is -x of the frame.
        List<NoteShape.PlacedQuad> qc = NoteShape.place(s, Direction.UP, Direction.NORTH, Spots.C, 0f);
        a = qc.get(0).v()[0]; b = qc.get(0).v()[1];
        fa = Spots.frame(Direction.UP, Direction.NORTH, new Vec3(a.x(), a.y(), a.z()));
        fb = Spots.frame(Direction.UP, Direction.NORTH, new Vec3(b.x(), b.y(), b.z()));
        check("u grows against frame x on ceilings (mirrored frame)", (a.u() - b.u()) * (fa[0] - fb[0]) < 0);
    }

    static void labels() {
        System.out.println("== label mapping");
        Vec3[] corners = {new Vec3(3 / 16.0, 3 / 16.0, 15.825 / 16.0), new Vec3(13 / 16.0, 3 / 16.0, 15.825 / 16.0),
                new Vec3(13 / 16.0, 13 / 16.0, 15.825 / 16.0), new Vec3(3 / 16.0, 13 / 16.0, 15.825 / 16.0)};
        // Identity: face SOUTH at inset 0 is the model's own frame.
        for (Vec3 c : corners) check("SOUTH/0 is identity " + c, LabelQuads.map(c, Direction.SOUTH, null, 0f).distanceTo(c) < EPS);
        for (Direction face : Direction.values()) {
            Direction[] rots = face.getAxis().isVertical() ? new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST} : new Direction[]{null};
            for (Direction rot : rots) {
                for (float inset : new float[]{0f, 1f / 16f}) {
                    AABB box = Spots.labelBox(face, rot, inset);
                    boolean in = true;
                    for (Vec3 c : corners) {
                        Vec3 m = LabelQuads.map(c, face, rot, inset);
                        in &= inBox(box, new NoteShape.PlacedVertex(m.x, m.y, m.z, 0, 0), EPS);
                        double[] f = Spots.frame(face, rot, m);
                        in &= Math.abs(f[2] - (inset - 0.175 / 16.0)) < EPS;
                    }
                    check("label corners on the label box " + face + "/" + rot + "/" + inset, in);
                    Vec3 n = LabelQuads.mapNormal(new Vec3(0, 0, -1), face, rot);
                    check("label normal toward the viewer " + face + "/" + rot, n.dot(Vec3.atLowerCornerOf(face.getOpposite().getNormal())) > 0.999);
                    Vec3 upM = LabelQuads.mapNormal(new Vec3(0, 1, 0), face, rot);
                    check("label up = frame y " + face + "/" + rot, upM.dot(Vec3.atLowerCornerOf(Spots.yAxis(face, rot).getNormal())) > 0.999);
                }
            }
        }
        check("packed normal (0,0,-1)", LabelQuads.unpackNormal((127 & 0xFF) | (0 << 8) | ((-127 & 0xFF) << 16)).distanceTo(new Vec3(1, 0, -1)) < EPS);
    }

    static void donorArt() throws Exception {
        System.out.println("== donor art");
        File base = new File("DecompiledMods/sticky_it-1.2.1/assets/sticky_it/textures");
        if (!base.isDirectory()) { System.out.println("skip: decompiled Sticky It not present"); return; }
        BufferedImage sticky = ImageIO.read(new File(base, "entities/sticky.png"));
        BufferedImage icon = ImageIO.read(new File(base, "item/stickyitem.png"));
        BufferedImage creeper = ImageIO.read(new File(base, "entities/sticky_creeper.png"));
        Method dominant = DonorArt.class.getDeclaredMethod("dominant", BufferedImage.class);
        Method greyscale = DonorArt.class.getDeclaredMethod("greyscale", BufferedImage.class);
        Method written = DonorArt.class.getDeclaredMethod("written", BufferedImage.class);
        Method recolour = DonorArt.class.getDeclaredMethod("recolour", BufferedImage.class, int.class);
        Method png = DonorArt.class.getDeclaredMethod("png", BufferedImage.class);
        for (Method m : new Method[]{dominant, greyscale, written, recolour, png}) m.setAccessible(true);
        int dom = (int) dominant.invoke(null, sticky);
        check("sticky dominant is yellow " + Integer.toHexString(dom), dom == 0xECE44C || dom == 0xE1D948);
        BufferedImage grey = (BufferedImage) greyscale.invoke(null, sticky);
        check("grey keeps size", grey.getWidth() == 16 && grey.getHeight() == 16);
        int whiteCount = 0, opaque = 0, transparentMatch = 0;
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
            int g = grey.getRGB(x, y), s = sticky.getRGB(x, y);
            if ((s >>> 24) == 0) { transparentMatch += (g >>> 24) == 0 ? 1 : 0; continue; }
            opaque++;
            int r = g >> 16 & 255;
            check("grey pixel is grey " + x + "," + y, r == (g >> 8 & 255) && r == (g & 255));
            if ((s & 0xFFFFFF) == dom) whiteCount += r == 255 ? 1 : 0;
        }
        check("transparent pixels stay transparent", transparentMatch == 256 - opaque);
        check("dominant colour maps to white", whiteCount > 0);
        BufferedImage w = (BufferedImage) written.invoke(null, sticky);
        check("stroke pixel darker (2,0)", (w.getRGB(2, 0) >> 8 & 255) < (sticky.getRGB(2, 0) >> 8 & 255));
        check("stroke pixel darker (2,2)", (w.getRGB(2, 2) >> 8 & 255) < (sticky.getRGB(2, 2) >> 8 & 255));
        check("non-stroke pixel unchanged (0,8)", w.getRGB(0, 8) == sticky.getRGB(0, 8));
        int creeperDom = (int) dominant.invoke(null, creeper);
        BufferedImage ic = (BufferedImage) recolour.invoke(null, icon, creeperDom);
        int p = ic.getRGB(6, 2);
        check("recoloured icon is greenish", (p >> 8 & 255) > (p >> 16 & 255) && (p >> 8 & 255) > (p & 255));
        check("recoloured icon keeps alpha", (ic.getRGB(0, 0) >>> 24) == 0 && (p >>> 24) == 255);
        byte[] bytes = (byte[]) png.invoke(null, grey);
        check("png bytes", bytes.length > 8 && (bytes[1] & 0xFF) == 'P' && (bytes[2] & 0xFF) == 'N' && (bytes[3] & 0xFF) == 'G');
        check("noteSprite plain", DonorArt.noteSprite(-1, null, false).equals(DonorArt.NOTE_PLAIN));
        check("noteSprite dyed written", DonorArt.noteSprite(0xFF0000, null, true).equals(DonorArt.NOTE_GREY_WRITTEN));
        check("noteSprite special ignores colour", DonorArt.noteSprite(0xFF0000, "gold", false).equals(DonorArt.special("gold")));
        check("tinted only when dyed and not special", DonorArt.tinted(0xFF0000, null) && !DonorArt.tinted(0xFF0000, "gold") && !DonorArt.tinted(-1, null));
        check("12 note sprites", DonorArt.noteSprites().size() == 12);
    }
}
