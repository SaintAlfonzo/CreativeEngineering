import com.SaintAlfonzo.creative_labels.block.Spots;
import com.SaintAlfonzo.creative_labels.client.IconDye;
import com.SaintAlfonzo.creative_labels.client.LabelLayout;
import com.SaintAlfonzo.creative_labels.item.NoteColours;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Headless checks for the phase-3 label rendering maths (no GL, no Minecraft instance): the IconDye ramp and
 * pixel packing, and LabelLayout's view frame on every face (icon inside the label box, in front of the paper,
 * counter-clockwise toward the viewer, upright for the viewer; the text rotation's axes).
 */
public class LabelRenderTest {
    static int ok = 0, failed = 0;
    static final double EPS = 1e-4;

    static void check(String name, boolean cond) {
        if (cond) { ok++; System.out.println("ok   " + name); } else { failed++; System.out.println("FAIL " + name); }
    }

    static void checkNear(String name, double a, double b, double eps) {
        check(name + " (" + a + " ~ " + b + ")", Math.abs(a - b) <= eps);
    }

    static boolean near(Vec3 a, Vec3 b) {
        return a.distanceTo(b) < 1e-3;
    }

    static boolean inBox(AABB box, Vec3 v, double eps) {
        return v.x >= box.minX - eps && v.x <= box.maxX + eps && v.y >= box.minY - eps && v.y <= box.maxY + eps
                && v.z >= box.minZ - eps && v.z <= box.maxZ + eps;
    }

    public static void main(String[] args) throws Exception {
        net.minecraft.SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        iconDye();
        layout();
        quaternions();
        System.out.println(failed == 0 ? "ALL " + ok + " checks passed" : failed + " FAILED, " + ok + " ok");
        System.exit(failed == 0 ? 0 : 1);
    }

    static int abgr(int a, int r, int g, int b) {
        return a << 24 | b << 16 | g << 8 | r;
    }

    static void iconDye() {
        System.out.println("== icon dye");
        for (DyeColor dye : DyeColor.values()) {
            int light = IconDye.light(dye), dark = IconDye.dark(dye);
            check(dye.getName() + ": dark is darker than light (" + NoteColours.hex(dark) + " < " + NoteColours.hex(light) + ")",
                    NoteColours.luminance(dark) < NoteColours.luminance(light));
            check(dye.getName() + ": black pixel -> dark", IconDye.ramp(0x000000, dark, light) == dark);
            check(dye.getName() + ": white pixel -> light", IconDye.ramp(0xFFFFFF, dark, light) == light);
        }
        int dark = IconDye.dark(DyeColor.RED), light = IconDye.light(DyeColor.RED);
        check("white dye falls back to the scaled light tone", IconDye.dark(DyeColor.WHITE) == IconDye.scale(IconDye.light(DyeColor.WHITE), IconDye.DARKEN));
        check("scale 0.6 of ff8040", IconDye.scale(0xFF8040, 0.6f) == (153 << 16 | 77 << 8 | 38));
        // mid grey lands between the ends, per channel
        int mid = IconDye.ramp(0x808080, dark, light);
        for (int shift = 16; shift >= 0; shift -= 8) {
            int d = dark >> shift & 0xFF, l = light >> shift & 0xFF, m = mid >> shift & 0xFF;
            check("mid grey channel between ends (" + d + " <= " + m + " <= " + l + ")", m >= Math.min(d, l) && m <= Math.max(d, l));
        }
        // packed pixels: transparent untouched, alpha kept, channel order R low byte
        check("transparent pixel untouched", IconDye.dyePixel(abgr(0, 10, 20, 30), dark, light) == abgr(0, 10, 20, 30));
        int p = IconDye.dyePixel(abgr(200, 255, 255, 255), dark, light);
        check("white pixel keeps alpha 200", (p >>> 24) == 200);
        check("white pixel -> light, R in the low byte", (p & 0xFF) == (light >> 16 & 0xFF) && (p >> 8 & 0xFF) == (light >> 8 & 0xFF)
                && (p >> 16 & 0xFF) == (light & 0xFF));
        int q = IconDye.dyePixel(abgr(255, 0, 0, 0), dark, light);
        check("black pixel -> dark", (q & 0xFF) == (dark >> 16 & 0xFF) && (q >> 16 & 0xFF) == (dark & 0xFF) && (q >>> 24) == 255);
        // a pure red pixel is darker than a pure green one in luminance, so it lands nearer the dark end
        int red = IconDye.ramp(0xFF0000, 0x000000, 0xFFFFFF), green = IconDye.ramp(0x00FF00, 0x000000, 0xFFFFFF);
        check("luminance ordering red < green", (red & 0xFF) < (green & 0xFF));
    }

    static void layout() {
        System.out.println("== layout");
        check("mirrored only on UP", LabelLayout.mirrored(Direction.UP, Direction.NORTH) && !LabelLayout.mirrored(Direction.DOWN, Direction.NORTH)
                && !LabelLayout.mirrored(Direction.NORTH, null) && !LabelLayout.mirrored(Direction.EAST, null));
        float[] full = LabelLayout.iconRect(false), small = LabelLayout.iconRect(true);
        check("full icon 8 px centred in the 10 px paper", full[0] == 4f && full[1] == 4f && full[2] == 12f && full[3] == 12f);
        check("small icon 6 px inside the paper, above the text", small[0] == 5f && small[2] == 11f && small[1] > LabelLayout.TEXT_TOP
                && small[3] <= Spots.LABEL_RECT[3]);
        check("text box inside the paper", LabelLayout.TEXT_TOP - LabelLayout.TEXT_HEIGHT >= Spots.LABEL_RECT[1]);
        checkNear("text scale fits the height for a short name", LabelLayout.textScale(10), LabelLayout.TEXT_HEIGHT / LabelLayout.LINE_HEIGHT, 1e-6);
        checkNear("text scale fits the width for a long name", LabelLayout.textScale(80), LabelLayout.TEXT_WIDTH / 80f, 1e-6);

        Direction[] rotations = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (Direction face : Direction.values()) {
            for (Direction rot : face.getAxis().isVertical() ? rotations : new Direction[]{null}) {
                String tag = face + (rot == null ? "" : "/" + rot);
                for (float inset : new float[]{0f, 1f / 16f}) {
                    for (boolean name : new boolean[]{false, true}) {
                        Vec3[] p = LabelLayout.iconCorners(face, rot, inset, name);
                        AABB box = Spots.labelBox(face, rot, inset);
                        boolean inside = true;
                        for (Vec3 v : p) inside &= inBox(box, v, 0.02);
                        check(tag + " inset " + inset + (name ? " small" : " full") + ": icon corners within the label box (+lift)", inside);
                        // in front of the paper plane by ICON_LIFT
                        double depth = Spots.frame(face, rot, p[0])[2];
                        checkNear(tag + ": icon depth = inset - (paper + lift)", depth, inset - (Spots.LABEL_DEPTH + LabelLayout.ICON_LIFT) / 16.0, 1e-4);
                        // counter-clockwise toward the viewer: (p1-p0) x (p3-p0) points out of the host
                        Vec3 n = p[1].subtract(p[0]).cross(p[3].subtract(p[0]));
                        check(tag + ": icon winding faces the viewer", n.dot(LabelLayout.toward(face)) > 0);
                        // bottom-left -> bottom-right runs along the viewer's right; bottom-left -> top-left along the viewer's up
                        check(tag + ": u runs along the viewer's right", p[1].subtract(p[0]).normalize().dot(LabelLayout.right(face, rot)) > 0.999);
                        check(tag + ": v runs along the viewer's up", p[3].subtract(p[0]).normalize().dot(LabelLayout.up(face, rot)) > 0.999);
                    }
                }
                // view frame axes are orthonormal and right x up = toward the viewer
                Vec3 r = LabelLayout.right(face, rot), u = LabelLayout.up(face, rot), t = LabelLayout.toward(face);
                check(tag + ": right x up = toward the viewer", near(r.cross(u), t));
                // text anchor is the top centre of the text box, in front of the paper
                Vec3 anchor = LabelLayout.textAnchor(face, rot, 0f);
                double[] f = Spots.frame(face, rot, anchor);
                checkNear(tag + ": text anchor x centred", f[0], 8.0, 1e-4);
                checkNear(tag + ": text anchor view y = TEXT_TOP", LabelLayout.viewY(face, rot, f[1]), LabelLayout.TEXT_TOP, 1e-4);
                check(tag + ": text in front of the icon", f[2] < LabelLayout.iconDepth(0f));
            }
        }
        // the viewer's up on a ceiling is the opposite of the placing player's facing (what is at the top of their view)
        check("ceiling, placed facing north: viewer's up is south", near(LabelLayout.up(Direction.UP, Direction.NORTH), new Vec3(0, 0, 1)));
        check("ceiling, placed facing north: viewer's right is east", near(LabelLayout.right(Direction.UP, Direction.NORTH), new Vec3(1, 0, 0)));
        check("floor, placed facing north: viewer's up is north", near(LabelLayout.up(Direction.DOWN, Direction.NORTH), new Vec3(0, 0, -1)));
        check("wall north face: right is east, up is up", near(LabelLayout.right(Direction.NORTH, null), new Vec3(1, 0, 0))
                && near(LabelLayout.up(Direction.NORTH, null), new Vec3(0, 1, 0)));
        check("wall east face (viewer west): right is south", near(LabelLayout.right(Direction.EAST, null), new Vec3(0, 0, 1)));
    }

    static Vec3 rotate(Quaternion q, Vec3 v) {
        Vector3f f = new Vector3f((float) v.x, (float) v.y, (float) v.z);
        f.transform(q);
        return new Vec3(f.x(), f.y(), f.z());
    }

    static void quaternions() {
        System.out.println("== quaternions");
        Vec3 ex = new Vec3(1, 0, 0), ey = new Vec3(0, 1, 0), ez = new Vec3(0, 0, 1);
        // every right-handed axis-aligned frame round-trips through fromAxes
        Direction[] all = Direction.values();
        int frames = 0;
        for (Direction a : all) {
            for (Direction b : all) {
                if (a.getAxis() == b.getAxis()) continue;
                Vec3 x = Vec3.atLowerCornerOf(a.getNormal()), y = Vec3.atLowerCornerOf(b.getNormal()), z = x.cross(y);
                Quaternion q = LabelLayout.fromAxes(x, y, z);
                boolean good = near(rotate(q, ex), x) && near(rotate(q, ey), y) && near(rotate(q, ez), z);
                if (!good) check("fromAxes " + a + " " + b, false);
                frames++;
            }
        }
        check("fromAxes maps the unit axes for all " + frames + " axis-aligned frames", frames == 24 && failed == 0);
        // the text rotation: font x -> viewer's right, font y -> viewer's down, font z -> into the host
        Direction[] rotations = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (Direction face : all) {
            for (Direction rot : face.getAxis().isVertical() ? rotations : new Direction[]{null}) {
                String tag = face + (rot == null ? "" : "/" + rot);
                Quaternion q = LabelLayout.textRotation(face, rot);
                check(tag + ": text x -> right", near(rotate(q, ex), LabelLayout.right(face, rot)));
                check(tag + ": text y -> down", near(rotate(q, ey), LabelLayout.up(face, rot).reverse()));
                check(tag + ": text z -> into the host", near(rotate(q, ez), Vec3.atLowerCornerOf(face.getNormal())));
            }
        }
    }
}
