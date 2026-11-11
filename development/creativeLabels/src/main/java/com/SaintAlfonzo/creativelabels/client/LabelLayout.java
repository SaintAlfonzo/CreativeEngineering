package com.SaintAlfonzo.creativelabels.client;

import com.SaintAlfonzo.creativelabels.block.Spots;
import com.mojang.math.Quaternion;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Where a label's icon and name go on a face (sticky_notes.md 3.11, BRIEF 4 "Label icons"), in pure geometry so the
 * headless test can check it. Lengths in px of the face frame unless said otherwise.
 *
 * <p><b>View frame.</b> The icon must read upright to the viewer in front of the paper, so it is laid out in a
 * "view frame": x to the viewer's right, y up as the viewer sees it. On walls and floors that is the Spots frame.
 * On ceilings (UP faces) the Spots frame is mirrored ({@link NoteShape#mirrored}): its y axis is the facing of the
 * player who placed the first piece, which is the <i>bottom</i> of that player's view when looking up, while x is
 * still their right; the view frame therefore flips y there ({@link #viewY}). Notes and the label paper mirror x
 * instead (a square and a curl do not care); the icon and the name follow the viewer.
 *
 * <p>Layout (Labels' numbers): paper 3..13; without a name the icon is 8 px centred; with a name the icon is 6 px
 * moved up and the name sits under it, fitted into {@link #TEXT_WIDTH} x {@link #TEXT_HEIGHT}.
 */
public final class LabelLayout {

    /** Icon edge without / with the name shown (px). */
    public static final float ICON_FULL = 8f, ICON_SMALL = 6f;
    /** Centre of the small icon (view y, px) when the name is shown. */
    public static final float ICON_SMALL_CENTRE_Y = 9.2f;
    /** The icon sits this far (px) in front of the label paper; the name a little more (no z-fighting). */
    public static final float ICON_LIFT = 0.03f, TEXT_LIFT = 0.06f;
    /** The name's box: top edge (view y), width and height (px). */
    public static final float TEXT_TOP = 5.6f, TEXT_WIDTH = 8f, TEXT_HEIGHT = 2f;
    /** Minecraft's font line height in font units. */
    public static final float LINE_HEIGHT = 9f;
    /** Font units -> px: the largest scale that fits the name's width and the line height into the box. */
    public static final float CENTRE_X = 8f;

    private LabelLayout() {}

    public static boolean mirrored(Direction face, @Nullable Direction rotation) {
        return NoteShape.mirrored(face, rotation);
    }

    /** View y -> Spots frame y. */
    public static double frameY(Direction face, @Nullable Direction rotation, double viewY) {
        return mirrored(face, rotation) ? 16.0 - viewY : viewY;
    }

    /** Spots frame y -> view y (its own inverse). */
    public static double viewY(Direction face, @Nullable Direction rotation, double frameY) {
        return frameY(face, rotation, frameY);
    }

    /** A view-frame point (px, px, depth in blocks into the host) in cell-local coordinates. */
    public static Vec3 at(Direction face, @Nullable Direction rotation, double fx, double viewY, double depth) {
        return at(face, rotation, Spots.Place.ZERO, fx, viewY, depth);
    }

    /** As above, laid out for the standard label and moved and scaled onto {@code place}'s square. */
    public static Vec3 at(Direction face, @Nullable Direction rotation, Spots.Place place, double fx, double viewY, double depth) {
        return Spots.local(face, rotation, place.labelX(fx), place.labelY(frameY(face, rotation, viewY)), depth);
    }

    /** The viewer's right as a world direction. */
    public static Vec3 right(Direction face, @Nullable Direction rotation) {
        return Vec3.atLowerCornerOf(Spots.xAxis(face, rotation).getNormal());
    }

    /** The viewer's up as a world direction. */
    public static Vec3 up(Direction face, @Nullable Direction rotation) {
        Vec3 y = Vec3.atLowerCornerOf(Spots.yAxis(face, rotation).getNormal());
        return mirrored(face, rotation) ? y.reverse() : y;
    }

    /** Toward the viewer: out of the host, i.e. the opposite of the face direction. */
    public static Vec3 toward(Direction face) {
        return Vec3.atLowerCornerOf(face.getOpposite().getNormal());
    }

    /** The icon square in view px: {x0, y0, x1, y1}. */
    public static float[] iconRect(boolean showName) {
        if (showName) {
            float h = ICON_SMALL / 2f;
            return new float[]{CENTRE_X - h, ICON_SMALL_CENTRE_Y - h, CENTRE_X + h, ICON_SMALL_CENTRE_Y + h};
        }
        float h = ICON_FULL / 2f;
        return new float[]{CENTRE_X - h, 8f - h, CENTRE_X + h, 8f + h};
    }

    /** Depth (blocks into the host) of the icon quad: just in front of the label paper. */
    public static double iconDepth(float inset) {
        return inset - (Spots.LABEL_DEPTH + ICON_LIFT) / 16.0;
    }

    public static double textDepth(float inset) {
        return inset - (Spots.LABEL_DEPTH + TEXT_LIFT) / 16.0;
    }

    /**
     * The icon quad's corners in cell-local coordinates, counter-clockwise as seen by the viewer:
     * bottom-left, bottom-right, top-right, top-left (texture u 0..1 left to right, v 0..1 bottom to top).
     */
    public static Vec3[] iconCorners(Direction face, @Nullable Direction rotation, float inset, boolean showName) {
        return iconCorners(face, rotation, Spots.Place.at(inset), showName);
    }

    public static Vec3[] iconCorners(Direction face, @Nullable Direction rotation, Spots.Place place, boolean showName) {
        float[] r = iconRect(showName);
        double d = iconDepth(place.inset());
        return new Vec3[]{
                at(face, rotation, place, r[0], r[1], d), at(face, rotation, place, r[2], r[1], d),
                at(face, rotation, place, r[2], r[3], d), at(face, rotation, place, r[0], r[3], d)};
    }

    /** Where the name's box starts: its top centre, in cell-local coordinates. */
    public static Vec3 textAnchor(Direction face, @Nullable Direction rotation, float inset) {
        return textAnchor(face, rotation, Spots.Place.at(inset));
    }

    public static Vec3 textAnchor(Direction face, @Nullable Direction rotation, Spots.Place place) {
        return at(face, rotation, place, CENTRE_X, TEXT_TOP, textDepth(place.inset()));
    }

    /** Px per font unit so that a name of {@code widthUnits} fits the text box. */
    public static float textScale(int widthUnits) {
        float byWidth = widthUnits <= 0 ? Float.MAX_VALUE : TEXT_WIDTH / widthUnits;
        return Math.min(byWidth, TEXT_HEIGHT / LINE_HEIGHT);
    }

    /**
     * The rotation that takes the font's axes (x right, y down, z) onto the view frame: x -> the viewer's right,
     * y -> the viewer's down, z -> into the host (right x down), a proper rotation.
     */
    public static Quaternion textRotation(Direction face, @Nullable Direction rotation) {
        Vec3 r = right(face, rotation), d = up(face, rotation).reverse();
        return fromAxes(r, d, r.cross(d));
    }

    /**
     * The quaternion of the rotation matrix whose columns are the given orthonormal right-handed axes (the images of
     * the unit x, y and z vectors). Standard matrix-to-quaternion conversion.
     */
    public static Quaternion fromAxes(Vec3 x, Vec3 y, Vec3 z) {
        double m00 = x.x, m10 = x.y, m20 = x.z;
        double m01 = y.x, m11 = y.y, m21 = y.z;
        double m02 = z.x, m12 = z.y, m22 = z.z;
        double tr = m00 + m11 + m22;
        double qx, qy, qz, qw;
        if (tr > 0) {
            double s = Math.sqrt(tr + 1.0) * 2.0;
            qw = 0.25 * s;
            qx = (m21 - m12) / s;
            qy = (m02 - m20) / s;
            qz = (m10 - m01) / s;
        } else if (m00 > m11 && m00 > m22) {
            double s = Math.sqrt(1.0 + m00 - m11 - m22) * 2.0;
            qw = (m21 - m12) / s;
            qx = 0.25 * s;
            qy = (m01 + m10) / s;
            qz = (m02 + m20) / s;
        } else if (m11 > m22) {
            double s = Math.sqrt(1.0 + m11 - m00 - m22) * 2.0;
            qw = (m02 - m20) / s;
            qx = (m01 + m10) / s;
            qy = 0.25 * s;
            qz = (m12 + m21) / s;
        } else {
            double s = Math.sqrt(1.0 + m22 - m00 - m11) * 2.0;
            qw = (m10 - m01) / s;
            qx = (m02 + m20) / s;
            qy = (m12 + m21) / s;
            qz = 0.25 * s;
        }
        // Normalised here in double precision: Mojang's Quaternion.normalize() uses the fast inverse square root
        // and leaves the length about 0.3 % short, which would scale everything drawn through the rotation.
        double len = Math.sqrt(qx * qx + qy * qy + qz * qz + qw * qw);
        return new Quaternion((float) (qx / len), (float) (qy / len), (float) (qz / len), (float) (qw / len));
    }
}
