package com.SaintAlfonzo.creative_labels.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Pure geometry and host rules for face paper (sticky_notes.md 3.3, BRIEF 3). No registry access at class init
 * (the tag keys are built lazily), so headless tests can load it after {@code Bootstrap.bootStrap()}.
 *
 * <p><b>Face frame.</b> Each face of a cell has a 2D frame in pixels: x (0..16, left to right as seen by a viewer in
 * front of the paper), y (0..16, bottom to top), and a depth that is positive INTO the host cell (0 = the cell
 * boundary). On walls x runs along the wall ({@code face.getClockWise()}) and y is up. On floors (face DOWN, host
 * below) and ceilings (face UP, host above) y is the face rotation (the player's facing when the first piece went on)
 * and x is to that player's right ({@code rotation.getClockWise()}). The note's curl hangs toward y = 0.
 *
 * <p><b>Spots.</b> Five rectangles in that frame: TL, TR, C, BL, BR ({@link #RECTS}). A spot is allowed when the host
 * fills its footprint at the face: class rules first (stairs and slabs through the shape test, posts by class or
 * tag), then the deny tag, then the shape test ({@link #fit}). Hosts in the centre-note tag take one note centred on
 * their visible side instead ({@link #centreNoteFit}).
 *
 * <p><b>Labels</b> have their own test ({@link #labelSurface}): the host's outline seen from the face, the outermost
 * surface and everything within {@link #SURFACE_WINDOW} of it, and the largest square there (10 px, or shrunk down to
 * {@link #MIN_LABEL}). Stairs and slabs keep the five-spot rule (no label on an inset face); doors and trapdoors and
 * hosts without collision take no label.
 */
public final class Spots {

    public static final int TL = 0, TR = 1, C = 2, BL = 3, BR = 4;
    public static final int COUNT = 5;
    public static final int ALL = (1 << COUNT) - 1;
    public static final int CENTRE_ONLY = 1 << C;

    /** Spot rectangles in the face frame, px: x0, y0, x1, y1 (BRIEF 3). */
    public static final float[][] RECTS = {
            {0.5f, 10.65f, 6.5f, 16f},   // TL
            {9.5f, 10.65f, 15.5f, 16f},  // TR
            {5f, 5.33f, 11f, 10.67f},    // C
            {0.5f, 0f, 6.5f, 5.35f},     // BL
            {9.5f, 0f, 15.5f, 5.35f},    // BR
    };

    /** Note: 6 x 5.35 px, curl up to 2.76 px out of the paper surface. Label: 10 x 10 px, 0.175 px thick. */
    public static final float NOTE_W = 6f, NOTE_H = 5.35f, NOTE_DEPTH = 2.76f;
    public static final float LABEL_SIZE = 10f, LABEL_DEPTH = 0.175f;
    /** The label's square in the face frame. */
    public static final float[] LABEL_RECT = {3f, 3f, 13f, 13f};
    public static final float[] PANEL_RECT = {0f, 0f, 16f, 16f};

    /**
     * Shape test: a host box must reach within this of the face plane (blocks) to carry paper. 2/16 (not 1/16) so
     * chest and ender chest lids (14 px tall) take notes like their sides; labels use the same limit.
     */
    public static final float MAX_INSET = 2f / 16f;
    /**
     * Shape test: the 9 sample points of a spot are taken this many px inside its rectangle, so a note may overhang
     * a host edge by up to 2 px (chest sides are 14 px tall; the stair and slab table still holds, see the tests).
     */
    public static final float EDGE_TOLERANCE = 2f;
    /**
     * Shape test: a host box must be at least this thick (blocks, along the face axis) to carry paper, so thin
     * attachments (carpets 1 px, buttons 2 px, ladders, doors and trapdoors 3 px, pressure plates, wall signs) take
     * nothing (3.3: "anything else: none") while slabs, stairs, chests and full blocks pass.
     */
    public static final float MIN_THICKNESS = 4f / 16f;

    /** Label and centre-note test: half-pixel samples per face edge. */
    public static final int GRID = 32;
    /** Label and centre-note test: surfaces up to this far behind the outermost one still count (blocks). */
    public static final float SURFACE_WINDOW = 1f / 16f;
    /** Label and centre-note test: the outermost surface may sit at most this deep (blocks; FaceContent's range). */
    public static final float MAX_SURFACE_INSET = 0.5f;
    /** Smallest label edge (px); a label shrinks to fit faces smaller than {@link #LABEL_SIZE}. */
    public static final float MIN_LABEL = 6f;

    private static final double EPS = 1e-4;

    public static final ResourceLocation DENY_TAG_ID = new ResourceLocation("creative_labels", "paper/deny");
    public static final ResourceLocation POSTS_TAG_ID = new ResourceLocation("creative_labels", "paper/posts");
    public static final ResourceLocation CENTRE_NOTE_TAG_ID = new ResourceLocation("creative_labels", "paper/centre_note");
    /** Centre-note hosts whose top takes one note too (Supplementaries jar, Pickling Jar; owner 2026-11-03). */
    public static final ResourceLocation NOTE_ON_TOP_TAG_ID = new ResourceLocation("creative_labels", "paper/note_on_top");
    @Nullable
    private static volatile TagKey<Block> denyTag, postsTag, centreNoteTag, noteOnTopTag;

    private Spots() {}

    // ------------------------------------------------------------------------------------------------ host rules

    /** Which hosts are denied outright and which count as posts; the default reads the two block tags. */
    public interface HostRules {
        boolean deny(BlockState host);

        boolean post(BlockState host);

        /** One note centred on each side face (jars, sacks, trash cans, botany pots). */
        default boolean centreNote(BlockState host) {
            return false;
        }

        /** A centre-note host whose top takes one centred note (the jar lid); other centre-note tops take none. */
        default boolean noteOnTop(BlockState host) {
            return false;
        }
    }

    public static final HostRules TAG_RULES = new HostRules() {
        @Override
        public boolean deny(BlockState host) {
            return host.is(denyTag());
        }

        @Override
        public boolean post(BlockState host) {
            return host.is(postsTag());
        }

        @Override
        public boolean centreNote(BlockState host) {
            return host.is(centreNoteTag());
        }

        @Override
        public boolean noteOnTop(BlockState host) {
            return host.is(noteOnTopTag());
        }
    };

    public static TagKey<Block> denyTag() {
        TagKey<Block> t = denyTag;
        if (t == null) denyTag = t = TagKey.create(Registry.BLOCK_REGISTRY, DENY_TAG_ID);
        return t;
    }

    public static TagKey<Block> postsTag() {
        TagKey<Block> t = postsTag;
        if (t == null) postsTag = t = TagKey.create(Registry.BLOCK_REGISTRY, POSTS_TAG_ID);
        return t;
    }

    public static TagKey<Block> noteOnTopTag() {
        TagKey<Block> t = noteOnTopTag;
        if (t == null) noteOnTopTag = t = TagKey.create(Registry.BLOCK_REGISTRY, NOTE_ON_TOP_TAG_ID);
        return t;
    }

    public static TagKey<Block> centreNoteTag() {
        TagKey<Block> t = centreNoteTag;
        if (t == null) centreNoteTag = t = TagKey.create(Registry.BLOCK_REGISTRY, CENTRE_NOTE_TAG_ID);
        return t;
    }

    /**
     * Where a face's pieces sit: the paper surface depth into the host (blocks), a shift of the pieces in the face
     * frame (px; notes: every spot rectangle moves by it, labels: the label's centre moves off (8, 8)) and the label's
     * edge (px). The default ({@link #at}) is the unshifted layout every face had before centred notes and fitted
     * labels.
     */
    public record Place(float inset, float offX, float offY, float size) {
        public static final Place ZERO = new Place(0f, 0f, 0f, LABEL_SIZE);

        public static Place at(float inset) {
            return new Place(inset, 0f, 0f, LABEL_SIZE);
        }

        public boolean isDefaultLayout() {
            return offX == 0f && offY == 0f && size == LABEL_SIZE;
        }

        /** A spot's rectangle moved by the shift. */
        public float[] noteRect(int spot) {
            float[] r = RECTS[spot];
            return new float[]{r[0] + offX, r[1] + offY, r[2] + offX, r[3] + offY};
        }

        /** The label's square in the face frame. */
        public float[] labelRect() {
            float h = size / 2f, cx = 8f + offX, cy = 8f + offY;
            return new float[]{cx - h, cy - h, cx + h, cy + h};
        }

        /** Label size relative to the standard 10 px label. */
        public float labelScale() {
            return size / LABEL_SIZE;
        }

        /** A frame x laid out for the standard label (square 3..13) moved onto this label's square. */
        public double labelX(double fx) {
            return 8.0 + offX + (fx - 8.0) * labelScale();
        }

        public double labelY(double fy) {
            return 8.0 + offY + (fy - 8.0) * labelScale();
        }
    }

    /**
     * Result of {@link #fit}: allowed spots (bit i = spot i), the notes' surface depth into the host and their shift
     * (px, centred notes only), and where a label would go (null = no label on this face).
     */
    public record Fit(int mask, float inset, float noteOffX, float noteOffY, @Nullable Place label) {
        public static final Fit NONE = new Fit(0, 0f, 0f, 0f, null);

        /** Unshifted notes; a label by the five-spot rule (all five spots, inset within {@link #MAX_INSET}). */
        public Fit(int mask, float inset) {
            this(mask, inset, 0f, 0f, mask == ALL && inset <= MAX_INSET + EPS ? Place.at(inset) : null);
        }

        public Fit withLabel(@Nullable Place l) {
            return new Fit(mask, inset, noteOffX, noteOffY, l);
        }

        /** Where the notes sit. */
        public Place notePlace() {
            return new Place(inset, noteOffX, noteOffY, LABEL_SIZE);
        }

        public boolean allows(int spot) {
            return (mask & (1 << spot)) != 0;
        }

        public boolean any() {
            return mask != 0;
        }

        public int count() {
            return Integer.bitCount(mask);
        }

        /** All five spots fit (slab halves included). */
        public boolean fiveSpot() {
            return mask == ALL;
        }

        /** A label fits this face ({@link #label} says where). */
        public boolean labelFits() {
            return label != null;
        }

        /** Spots that fit and are not yet taken. */
        public int free(int takenMask) {
            return mask & ~takenMask;
        }
    }

    /**
     * Which spots of the cell face {@code face} the host at {@code hostPos} allows. {@code rotation} matters for UP
     * and DOWN faces only (null = NORTH).
     */
    public static Fit fit(BlockState host, BlockGetter level, BlockPos hostPos, Direction face, @Nullable Direction rotation) {
        return fit(host, level, hostPos, face, rotation, TAG_RULES);
    }

    public static Fit fit(BlockState host, BlockGetter level, BlockPos hostPos, Direction face, @Nullable Direction rotation,
                          HostRules rules) {
        if (host.isAir()) return Fit.NONE;
        if (rules.deny(host)) return Fit.NONE;
        Direction hostFace = face.getOpposite();
        Block block = host.getBlock();

        // Fence gates are not posts (4.1) and their 4 px top band would otherwise pass the centre spot: nothing.
        if (block instanceof FenceGateBlock) return Fit.NONE;

        // Heads take nothing (3.3): a wall head is an 8 px cube against the wall whose 8 x 8 band would otherwise
        // pass the centre spot through the shape test.
        if (block instanceof AbstractSkullBlock) return Fit.NONE;

        // Posts: fences, walls, iron bars, glass panes (CrossCollisionBlock, WallBlock), end rods and lightning rods
        // (RodBlock), chains, or the posts tag: the centre spot on the two end faces of the post's axis. No label.
        if (block instanceof CrossCollisionBlock || block instanceof WallBlock || block instanceof RodBlock
                || block instanceof ChainBlock || rules.post(host)) {
            Direction.Axis axis = postAxis(host);
            return hostFace.getAxis() == axis ? new Fit(CENTRE_ONLY, 0f) : Fit.NONE;
        }

        // Slab halves: the half-height face takes all five spots at inset 0.5 (C9); every other slab face and
        // double slabs go through the shape test (sides: the two corners next to the slab; double: full block).
        if (block instanceof SlabBlock && host.hasProperty(SlabBlock.TYPE)) {
            SlabType type = host.getValue(SlabBlock.TYPE);
            if (type == SlabType.BOTTOM && hostFace == Direction.UP) return new Fit(ALL, 0.5f);
            if (type == SlabType.TOP && hostFace == Direction.DOWN) return new Fit(ALL, 0.5f);
        }

        // Stairs (StairBlock, any shape) and everything else: the shape test. For straight stairs it yields the
        // 3.3 table exactly: bottom/back 5, sides 3, front 2, top 2 (verified by SpotsTest). Stairs and slabs keep
        // the five-spot label rule (a label only on a full face at the boundary, never on an inset face).
        VoxelShape shape = host.getShape(level, hostPos, CollisionContext.empty());
        if (block instanceof StairBlock || block instanceof SlabBlock) return shapeFit(shape, face, rotation);

        // Centre-note hosts (tag): one note centred on each side face. Tops: only hosts in the note-on-top tag (the
        // jar and the Pickling Jar, whose lid sits 2.5 px down, too deep for the shape test), one note centred on the
        // lid; every other centre-note top (sack neck, trash can rim, botany pot soil) and every bottom takes none
        // (owner 2026-11-03, SQ-LBS-03; the shape test let the jar lid and the sack neck carry spot C).
        Fit notes;
        if (!rules.centreNote(host)) notes = shapeFit(shape, face, rotation);
        else if (face.getAxis().isHorizontal()) notes = centreNoteFit(shape, face, rotation);
        else if (hostFace == Direction.UP && rules.noteOnTop(host)) notes = centreNoteFit(shape, face, rotation);
        else notes = Fit.NONE;

        // Labels: the surface test; doors and trapdoors (they swing) and hosts without collision (plants) take none.
        Place label = null;
        if (!(block instanceof DoorBlock) && !(block instanceof TrapDoorBlock)
                && !host.getCollisionShape(level, hostPos, CollisionContext.empty()).isEmpty()) {
            label = labelSurface(shape, face, rotation);
        }
        return notes.withLabel(label);
    }

    // ------------------------------------------------------------------------------------------ surface test

    /**
     * Depth (blocks behind the host face's plane) of the first host surface behind each half-pixel sample of the
     * face, index [x][y] in the face frame; +infinity = nothing behind. Uses the merged outline, so how a shape is
     * split into boxes (a cauldron's 2 px walls) does not matter, and no thickness limit applies.
     */
    public static float[][] surface(VoxelShape hostShape, Direction face, @Nullable Direction rotation) {
        Direction hostFace = face.getOpposite();
        List<AABB> boxes = hostShape.toAabbs();
        float[][] d = new float[GRID][GRID];
        for (int i = 0; i < GRID; i++) {
            for (int j = 0; j < GRID; j++) {
                Vec3 p = hostLocal(face, rotation, (i + 0.5) * 16.0 / GRID, (j + 0.5) * 16.0 / GRID, 0);
                float best = Float.POSITIVE_INFINITY;
                for (AABB box : boxes) {
                    double depth = depthOf(box, hostFace);
                    if (depth < -EPS || !coversInPlane(box, hostFace, p)) continue;
                    if (depth < best) best = (float) Math.max(0, depth);
                }
                d[i][j] = best;
            }
        }
        return d;
    }

    /** The outermost surface depth of a surface map, or +infinity when nothing is behind the face. */
    static float outermost(float[][] d) {
        float min = Float.POSITIVE_INFINITY;
        for (float[] col : d) for (float v : col) min = Math.min(min, v);
        return min;
    }

    /** Samples whose surface lies within {@link #SURFACE_WINDOW} of the outermost one ({@code d0}). */
    static boolean[][] usable(float[][] d, float d0) {
        boolean[][] u = new boolean[GRID][GRID];
        for (int i = 0; i < GRID; i++) for (int j = 0; j < GRID; j++) u[i][j] = d[i][j] <= d0 + SURFACE_WINDOW + EPS;
        return u;
    }

    private static int sample(double px) {
        return Math.max(0, Math.min(GRID - 1, (int) Math.floor(px * GRID / 16.0)));
    }

    /**
     * Where a label goes on this face, or null: the outermost surface (at most {@link #MAX_SURFACE_INSET} deep),
     * every sample within {@link #SURFACE_WINDOW} of it, and the largest square of such samples nearest the face
     * centre, capped at {@link #LABEL_SIZE}; under {@link #MIN_LABEL} -> null. Full faces give the standard label.
     */
    @Nullable
    public static Place labelSurface(VoxelShape hostShape, Direction face, @Nullable Direction rotation) {
        if (hostShape.isEmpty()) return null;
        float[][] d = surface(hostShape, face, rotation);
        float d0 = outermost(d);
        if (d0 > MAX_SURFACE_INSET + EPS) return null;
        boolean[][] u = usable(d, d0);
        // s[i][j]: edge (samples) of the largest usable square whose top-right sample is (i, j).
        int[][] s = new int[GRID][GRID];
        int maxEdge = 0;
        for (int i = 0; i < GRID; i++) {
            for (int j = 0; j < GRID; j++) {
                if (!u[i][j]) continue;
                s[i][j] = (i == 0 || j == 0) ? 1 : 1 + Math.min(s[i - 1][j], Math.min(s[i][j - 1], s[i - 1][j - 1]));
                maxEdge = Math.max(maxEdge, s[i][j]);
            }
        }
        int n = Math.min(maxEdge, Math.round(LABEL_SIZE * GRID / 16f));
        float size = n * 16f / GRID;
        if (size < MIN_LABEL - EPS) return null;
        float bestCx = 8f, bestCy = 8f;
        double bestDist = Double.MAX_VALUE;
        for (int i = n - 1; i < GRID; i++) {
            for (int j = n - 1; j < GRID; j++) {
                if (s[i][j] < n) continue;
                float cx = (2 * i - n + 2) * 8f / GRID, cy = (2 * j - n + 2) * 8f / GRID;
                double dist = (cx - 8.0) * (cx - 8.0) + (cy - 8.0) * (cy - 8.0);
                if (dist < bestDist - EPS) {
                    bestDist = dist;
                    bestCx = cx;
                    bestCy = cy;
                }
            }
        }
        return new Place(d0, bestCx - 8f, bestCy - 8f, size);
    }

    /**
     * Centre-note hosts (jars, sacks, trash cans, botany pots): only the centre spot, moved to the middle of the
     * host's visible side (the bounding box of the samples within {@link #SURFACE_WINDOW} of the outermost surface),
     * at that surface's depth (at most {@link #MAX_SURFACE_INSET}). The note's sample points (as in {@link #shapeFit})
     * must all lie on that surface.
     */
    public static Fit centreNoteFit(VoxelShape hostShape, Direction face, @Nullable Direction rotation) {
        if (hostShape.isEmpty()) return Fit.NONE;
        float[][] d = surface(hostShape, face, rotation);
        float d0 = outermost(d);
        if (d0 > MAX_SURFACE_INSET + EPS) return Fit.NONE;
        boolean[][] u = usable(d, d0);
        int minI = GRID, maxI = -1, minJ = GRID, maxJ = -1;
        for (int i = 0; i < GRID; i++) {
            for (int j = 0; j < GRID; j++) {
                if (!u[i][j]) continue;
                minI = Math.min(minI, i);
                maxI = Math.max(maxI, i);
                minJ = Math.min(minJ, j);
                maxJ = Math.max(maxJ, j);
            }
        }
        if (maxI < 0) return Fit.NONE;
        float[] c = RECTS[C];
        float cx = (minI + maxI + 1) * 8f / GRID, cy = (minJ + maxJ + 1) * 8f / GRID;
        float offX = clamp(cx - (c[0] + c[2]) / 2f, -c[0], 16f - c[2]);
        float offY = clamp(cy - (c[1] + c[3]) / 2f, -c[1], 16f - c[3]);
        for (int sy = 0; sy <= 2; sy++) {
            for (int sx = 0; sx <= 2; sx++) {
                float fx = c[0] + offX + EDGE_TOLERANCE + (c[2] - c[0] - 2 * EDGE_TOLERANCE) * sx / 2f;
                float fy = c[1] + offY + EDGE_TOLERANCE + (c[3] - c[1] - 2 * EDGE_TOLERANCE) * sy / 2f;
                if (!u[sample(fx)][sample(fy)]) return Fit.NONE;
            }
        }
        return new Fit(CENTRE_ONLY, d0, offX, offY, null);
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    /** Axis of a post-like host: its FACING or AXIS property when it has one, else vertical. */
    public static Direction.Axis postAxis(BlockState host) {
        if (host.hasProperty(RotatedPillarBlock.AXIS)) return host.getValue(RotatedPillarBlock.AXIS);
        if (host.hasProperty(DirectionalBlock.FACING)) return host.getValue(DirectionalBlock.FACING).getAxis();
        return Direction.Axis.Y;
    }

    /**
     * Shape test (BRIEF 3): host boxes at least {@link #MIN_THICKNESS} thick that reach within {@link #MAX_INSET} of
     * the face plane must together cover all 9 sample points of a spot (corners, edge midpoints, centre of the
     * rectangle shrunk by {@link #EDGE_TOLERANCE}); the face inset is the shallowest depth among the boxes that
     * cover any allowed sample point.
     */
    public static Fit shapeFit(VoxelShape hostShape, Direction face, @Nullable Direction rotation) {
        if (hostShape.isEmpty()) return Fit.NONE;
        Direction hostFace = face.getOpposite();
        List<AABB> boxes = hostShape.toAabbs();
        int mask = 0;
        float inset = Float.MAX_VALUE;
        for (int spot = 0; spot < COUNT; spot++) {
            float[] r = RECTS[spot];
            boolean ok = true;
            float spotInset = Float.MAX_VALUE;
            for (int sy = 0; sy <= 2 && ok; sy++) {
                for (int sx = 0; sx <= 2; sx++) {
                    float fx = r[0] + EDGE_TOLERANCE + (r[2] - r[0] - 2 * EDGE_TOLERANCE) * sx / 2f;
                    float fy = r[1] + EDGE_TOLERANCE + (r[3] - r[1] - 2 * EDGE_TOLERANCE) * sy / 2f;
                    Vec3 p = hostLocal(face, rotation, fx, fy, 0);
                    float best = Float.MAX_VALUE;
                    for (AABB box : boxes) {
                        float depth = (float) depthOf(box, hostFace);
                        if (depth > MAX_INSET + EPS || depth < -EPS) continue;
                        if (thicknessOf(box, hostFace) < MIN_THICKNESS - EPS) continue;
                        if (!coversInPlane(box, hostFace, p)) continue;
                        if (depth < best) best = depth;
                    }
                    if (best == Float.MAX_VALUE) {
                        ok = false;
                        break;
                    }
                    spotInset = Math.min(spotInset, best);
                }
            }
            if (ok) {
                mask |= 1 << spot;
                inset = Math.min(inset, spotInset);
            }
        }
        return mask == 0 ? Fit.NONE : new Fit(mask, Math.max(0f, inset));
    }

    /** How far a host box's outer surface sits behind the host face's plane (blocks; 0 = flush). */
    static double depthOf(AABB box, Direction hostFace) {
        return hostFace.getAxisDirection() == Direction.AxisDirection.POSITIVE
                ? 1.0 - box.max(hostFace.getAxis()) : box.min(hostFace.getAxis());
    }

    /** A host box's extent along the host face's axis (blocks). */
    static double thicknessOf(AABB box, Direction hostFace) {
        return box.max(hostFace.getAxis()) - box.min(hostFace.getAxis());
    }

    /** Whether a point on the host face's plane lies within the box's footprint on that plane. */
    static boolean coversInPlane(AABB box, Direction hostFace, Vec3 p) {
        for (Direction.Axis axis : Direction.Axis.VALUES) {
            if (axis == hostFace.getAxis()) continue;
            double v = p.get(axis);
            if (v < box.min(axis) - EPS || v > box.max(axis) + EPS) return false;
        }
        return true;
    }

    // ------------------------------------------------------------------------------------------------- face frame

    /** Rotation used for UP and DOWN faces (null = NORTH); walls ignore it. */
    public static Direction rotationOf(Direction face, @Nullable Direction rotation) {
        if (face.getAxis().isHorizontal()) return face;
        return rotation == null || !rotation.getAxis().isHorizontal() ? Direction.NORTH : rotation;
    }

    /** The frame's x axis (right, as seen by the viewer in front of the paper) as a world direction. */
    public static Direction xAxis(Direction face, @Nullable Direction rotation) {
        return face.getAxis().isHorizontal() ? face.getClockWise() : rotationOf(face, rotation).getClockWise();
    }

    /** The frame's y axis (up on walls, the face rotation on floors and ceilings). */
    public static Direction yAxis(Direction face, @Nullable Direction rotation) {
        return face.getAxis().isHorizontal() ? Direction.UP : rotationOf(face, rotation);
    }

    private static double originComponent(Direction xa, Direction ya, Direction face, Direction.Axis axis) {
        if (xa.getAxis() == axis) return xa.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0 : 1;
        if (ya.getAxis() == axis) return ya.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0 : 1;
        return face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0;
    }

    /** Frame origin (fx = fy = depth = 0) in cell-local coordinates. */
    public static Vec3 origin(Direction face, @Nullable Direction rotation) {
        Direction xa = xAxis(face, rotation), ya = yAxis(face, rotation);
        return new Vec3(originComponent(xa, ya, face, Direction.Axis.X), originComponent(xa, ya, face, Direction.Axis.Y),
                originComponent(xa, ya, face, Direction.Axis.Z));
    }

    /**
     * Frame point (px, px, blocks into the host) to cell-local coordinates (blocks; 0..1 inside the cell, beyond 1 or
     * below 0 along the face axis when the depth reaches into the host).
     */
    public static Vec3 local(Direction face, @Nullable Direction rotation, double fxPx, double fyPx, double depth) {
        Direction xa = xAxis(face, rotation), ya = yAxis(face, rotation);
        Vec3 o = origin(face, rotation);
        double fx = fxPx / 16.0, fy = fyPx / 16.0;
        return new Vec3(
                o.x + fx * xa.getStepX() + fy * ya.getStepX() + depth * face.getStepX(),
                o.y + fx * xa.getStepY() + fy * ya.getStepY() + depth * face.getStepY(),
                o.z + fx * xa.getStepZ() + fy * ya.getStepZ() + depth * face.getStepZ());
    }

    /** Frame point to the HOST cell's local coordinates (the host sits one cell along {@code face}). */
    public static Vec3 hostLocal(Direction face, @Nullable Direction rotation, double fxPx, double fyPx, double depth) {
        Vec3 p = local(face, rotation, fxPx, fyPx, depth);
        return new Vec3(p.x - face.getStepX(), p.y - face.getStepY(), p.z - face.getStepZ());
    }

    /** Cell-local point to frame coordinates: {x px, y px, depth blocks}. */
    public static double[] frame(Direction face, @Nullable Direction rotation, Vec3 local) {
        Direction xa = xAxis(face, rotation), ya = yAxis(face, rotation);
        Vec3 o = origin(face, rotation);
        double dx = local.x - o.x, dy = local.y - o.y, dz = local.z - o.z;
        return new double[]{
                (dx * xa.getStepX() + dy * xa.getStepY() + dz * xa.getStepZ()) * 16.0,
                (dx * ya.getStepX() + dy * ya.getStepY() + dz * ya.getStepZ()) * 16.0,
                dx * face.getStepX() + dy * face.getStepY() + dz * face.getStepZ()};
    }

    // ------------------------------------------------------------------------------------------------- piece boxes

    /** Cell-local box of a frame rectangle (px) between two depths (blocks into the host). */
    public static AABB box(Direction face, @Nullable Direction rotation, float[] rect, double depthNear, double depthFar) {
        Vec3 a = local(face, rotation, rect[0], rect[1], depthNear);
        Vec3 b = local(face, rotation, rect[2], rect[3], depthFar);
        return new AABB(a, b);
    }

    /** A note's box on a spot: the spot rectangle, from the paper surface out by {@link #NOTE_DEPTH}. */
    public static AABB noteBox(Direction face, @Nullable Direction rotation, int spot, float inset) {
        return noteBox(face, rotation, Place.at(inset), spot);
    }

    /** A note's box on a spot of a face laid out by {@code place} (shifted for centred notes). */
    public static AABB noteBox(Direction face, @Nullable Direction rotation, Place place, int spot) {
        return box(face, rotation, place.noteRect(spot), place.inset() - NOTE_DEPTH / 16.0, place.inset());
    }

    public static AABB labelBox(Direction face, @Nullable Direction rotation, float inset) {
        return labelBox(face, rotation, Place.at(inset));
    }

    /** A label's box: its square from {@code place}, label-thick. */
    public static AABB labelBox(Direction face, @Nullable Direction rotation, Place place) {
        return box(face, rotation, place.labelRect(), place.inset() - LABEL_DEPTH / 16.0, place.inset());
    }

    /** Covered face (3.3): the whole 16 x 16 panel, note-thick. */
    public static AABB panelBox(Direction face, @Nullable Direction rotation, float inset) {
        return box(face, rotation, PANEL_RECT, inset - NOTE_DEPTH / 16.0, inset);
    }

    /** Hit shape of one face: the panel when covered, else every piece's box. Empty for an empty face. */
    public static VoxelShape faceShape(Direction face, FaceContent c) {
        if (c.isEmpty()) return Shapes.empty();
        if (c.kind == FaceContent.Kind.LABEL) return Shapes.create(labelBox(face, c.rotation, c.place()));
        if (c.isCovered()) return Shapes.create(panelBox(face, c.rotation, c.inset));
        VoxelShape shape = Shapes.empty();
        for (int spot = 0; spot < COUNT; spot++) {
            if (c.notes[spot] != null) shape = Shapes.or(shape, Shapes.create(noteBox(face, c.rotation, c.place(), spot)));
        }
        return shape;
    }

    /** Hit shape of the whole cell: the union over its six faces. */
    public static VoxelShape shape(FaceContent[] faces) {
        VoxelShape shape = Shapes.empty();
        for (Direction d : Direction.values()) {
            FaceContent c = faces[d.ordinal()];
            if (c != null && !c.isEmpty()) shape = Shapes.or(shape, faceShape(d, c));
        }
        return shape;
    }

    // ------------------------------------------------------------------------------------------------- hit lookup

    /** Distance from a point to a rectangle in the frame (0 inside). */
    static double rectDistance(float[] r, double x, double y) {
        double dx = Math.max(Math.max(r[0] - x, 0), x - r[2]);
        double dy = Math.max(Math.max(r[1] - y, 0), y - r[3]);
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** Distance from a point to a box (0 inside). */
    public static double boxDistance(AABB b, Vec3 p) {
        double dx = Math.max(Math.max(b.minX - p.x, 0), p.x - b.maxX);
        double dy = Math.max(Math.max(b.minY - p.y, 0), p.y - b.maxY);
        double dz = Math.max(Math.max(b.minZ - p.z, 0), p.z - b.maxZ);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * The spot under a cell-local point (in the face frame), or the nearest one among {@code allowedMask}; -1 when
     * the mask is empty. Used for placement: the spot under the click if free and allowed, else the nearest.
     */
    public static int nearestSpot(Direction face, @Nullable Direction rotation, Vec3 local, int allowedMask) {
        if (allowedMask == 0) return -1;
        double[] f = frame(face, rotation, local);
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int spot = 0; spot < COUNT; spot++) {
            if ((allowedMask & (1 << spot)) == 0) continue;
            double d = rectDistance(RECTS[spot], f[0], f[1]);
            if (d < bestDist) {
                bestDist = d;
                best = spot;
            }
        }
        return best;
    }

    /** The spot whose rectangle contains the point, or -1. */
    public static int spotAt(Direction face, @Nullable Direction rotation, Vec3 local) {
        double[] f = frame(face, rotation, local);
        for (int spot = 0; spot < COUNT; spot++) {
            if (rectDistance(RECTS[spot], f[0], f[1]) <= EPS) return spot;
        }
        return -1;
    }
}
