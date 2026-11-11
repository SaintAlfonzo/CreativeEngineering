import com.SaintAlfonzo.creativelabels.block.FaceContent;
import com.SaintAlfonzo.creativelabels.block.FacePaperInteractions;
import com.SaintAlfonzo.creativelabels.block.LabelData;
import com.SaintAlfonzo.creativelabels.block.NoteData;
import com.SaintAlfonzo.creativelabels.block.PieceHit;
import com.SaintAlfonzo.creativelabels.block.Spots;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Headless checks of Spots and PieceHit (BRIEF 6): the host table of sticky_notes.md 3.3 on real vanilla blocks,
 * floor rotation, the covered-face rule, piece boxes and hit resolution. Needs Bootstrap (block shapes).
 */
public class SpotsTest {
    static int fails = 0, checks = 0;

    static void check(boolean ok, String what) {
        checks++;
        if (!ok) fails++;
        System.out.println((ok ? "ok   " : "FAIL ") + what);
    }

    static final BlockGetter LEVEL = new BlockGetter() {
        @Override public BlockEntity getBlockEntity(BlockPos pos) { return null; }
        @Override public BlockState getBlockState(BlockPos pos) { return Blocks.AIR.defaultBlockState(); }
        @Override public FluidState getFluidState(BlockPos pos) { return Fluids.EMPTY.defaultFluidState(); }
        @Override public int getHeight() { return 384; }
        @Override public int getMinBuildHeight() { return -64; }
    };
    static final BlockPos POS = BlockPos.ZERO;

    static final Spots.HostRules NO_TAGS = new Spots.HostRules() {
        @Override public boolean deny(BlockState host) { return false; }
        @Override public boolean post(BlockState host) { return false; }
    };

    static int bits(int... spots) {
        int m = 0;
        for (int s : spots) m |= 1 << s;
        return m;
    }

    static String mask(int m) {
        StringBuilder sb = new StringBuilder();
        String[] names = {"TL", "TR", "C", "BL", "BR"};
        for (int i = 0; i < 5; i++) if ((m & (1 << i)) != 0) sb.append(names[i]).append(' ');
        return sb.length() == 0 ? "-" : sb.toString().trim();
    }

    /** fit of the cell face {@code face} whose host is {@code host}. */
    static Spots.Fit fit(BlockState host, Direction face, Direction rotation) {
        return Spots.fit(host, LEVEL, POS, face, rotation, NO_TAGS);
    }

    static void expect(BlockState host, Direction face, Direction rotation, int mask, float inset, String what) {
        Spots.Fit f = fit(host, face, rotation);
        boolean ok = f.mask() == mask && Math.abs(f.inset() - inset) < 1e-4;
        check(ok, what + " (face " + face + ", rot " + rotation + "): got " + mask(f.mask()) + " @ " + f.inset()
                + (ok ? "" : ", want " + mask(mask) + " @ " + inset));
    }

    static BlockState stairs(Direction facing, Half half) {
        return Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half)
                .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
    }

    static BlockState slab(SlabType type) {
        return Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, type);
    }

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState chest = Blocks.CHEST.defaultBlockState();
        BlockState barrel = Blocks.BARREL.defaultBlockState();
        BlockState carpet = Blocks.RED_CARPET.defaultBlockState();
        BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
        BlockState wall = Blocks.COBBLESTONE_WALL.defaultBlockState();
        BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        BlockState pane = Blocks.GLASS_PANE.defaultBlockState();

        System.out.println("== full faces");
        for (Direction d : Direction.values()) expect(stone, d, Direction.NORTH, Spots.ALL, 0f, "stone");
        check(fit(stone, Direction.NORTH, null).labelFits(), "stone: a label fits");
        for (Direction d : Direction.values()) expect(barrel, d, Direction.NORTH, Spots.ALL, 0f, "barrel");
        for (Direction d : Direction.Plane.HORIZONTAL) expect(chest, d, null, Spots.ALL, 1f / 16f, "chest side");
        expect(chest, Direction.DOWN, Direction.NORTH, Spots.ALL, 2f / 16f, "chest lid (host below the cell)");
        expect(chest, Direction.UP, Direction.NORTH, Spots.ALL, 0f, "chest bottom (host above the cell)");
        check(fit(chest, Direction.NORTH, null).labelFits(), "chest side: a label fits (inset 1/16)");
        check(fit(chest, Direction.DOWN, Direction.NORTH).labelFits(), "chest lid: a label fits (inset 2/16)");
        expect(Blocks.AIR.defaultBlockState(), Direction.NORTH, null, 0, 0f, "air");

        System.out.println("== deny tag");
        Spots.HostRules denyStone = new Spots.HostRules() {
            @Override public boolean deny(BlockState host) { return host.is(Blocks.STONE); }
            @Override public boolean post(BlockState host) { return false; }
        };
        check(!Spots.fit(stone, LEVEL, POS, Direction.NORTH, null, denyStone).any(), "deny predicate: stone gives nothing");
        check(Spots.fit(barrel, LEVEL, POS, Direction.NORTH, null, denyStone).fiveSpot(), "deny predicate: barrel untouched");
        Spots.HostRules postStone = new Spots.HostRules() {
            @Override public boolean deny(BlockState host) { return false; }
            @Override public boolean post(BlockState host) { return host.is(Blocks.STONE); }
        };
        check(Spots.fit(stone, LEVEL, POS, Direction.DOWN, Direction.NORTH, postStone).mask() == Spots.CENTRE_ONLY,
                "posts predicate: stone top = centre only");
        check(!Spots.fit(stone, LEVEL, POS, Direction.NORTH, null, postStone).any(), "posts predicate: stone side = nothing");

        System.out.println("== thin shapes");
        expect(carpet, Direction.NORTH, null, 0, 0f, "carpet side");
        expect(carpet, Direction.DOWN, Direction.NORTH, 0, 0f, "carpet top (1 px, 15/16 deep)");
        expect(Blocks.OAK_BUTTON.defaultBlockState(), Direction.NORTH, null, 0, 0f, "button (2 px thick)");
        expect(Blocks.LADDER.defaultBlockState(), Direction.NORTH, null, 0, 0f, "ladder (3 px thick)");
        expect(Blocks.OAK_TRAPDOOR.defaultBlockState(), Direction.UP, Direction.NORTH, 0, 0f, "closed bottom trapdoor underside (3 px)");
        expect(Blocks.OAK_DOOR.defaultBlockState(), Direction.NORTH, null, 0, 0f, "door (3 px)");
        expect(Blocks.STONE_PRESSURE_PLATE.defaultBlockState(), Direction.UP, Direction.NORTH, 0, 0f, "pressure plate underside");
        expect(Blocks.SNOW.defaultBlockState().setValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS, 8), Direction.DOWN, Direction.NORTH, Spots.ALL, 0f, "8 snow layers (full)");
        expect(Blocks.SNOW.defaultBlockState().setValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS, 2), Direction.DOWN, Direction.NORTH, 0, 0f, "2 snow layers (4 px, 12/16 deep)");

        System.out.println("== posts");
        for (BlockState post : new BlockState[]{fence, wall, bars, pane}) {
            String n = Registry.BLOCK.getKey(post.getBlock()).getPath();
            expect(post, Direction.DOWN, Direction.NORTH, Spots.CENTRE_ONLY, 0f, n + " top");
            expect(post, Direction.UP, Direction.NORTH, Spots.CENTRE_ONLY, 0f, n + " bottom");
            expect(post, Direction.NORTH, null, 0, 0f, n + " side");
        }
        BlockState rodUp = Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP);
        BlockState rodEast = Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.EAST);
        expect(rodUp, Direction.DOWN, Direction.NORTH, Spots.CENTRE_ONLY, 0f, "vertical end rod top");
        expect(rodUp, Direction.EAST, null, 0, 0f, "vertical end rod side");
        expect(rodEast, Direction.EAST, null, Spots.CENTRE_ONLY, 0f, "horizontal end rod end");
        expect(rodEast, Direction.WEST, null, Spots.CENTRE_ONLY, 0f, "horizontal end rod other end");
        expect(rodEast, Direction.DOWN, Direction.NORTH, 0, 0f, "horizontal end rod top");
        BlockState chainZ = Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.Z);
        expect(chainZ, Direction.NORTH, null, Spots.CENTRE_ONLY, 0f, "horizontal chain end");
        expect(chainZ, Direction.UP, Direction.NORTH, 0, 0f, "horizontal chain bottom");
        expect(Blocks.LIGHTNING_ROD.defaultBlockState(), Direction.DOWN, Direction.NORTH, Spots.CENTRE_ONLY, 0f, "lightning rod top");
        expect(Blocks.OAK_FENCE_GATE.defaultBlockState(), Direction.DOWN, Direction.NORTH, 0, 0f, "fence gate top (not a post)");
        for (Direction d : Direction.values()) {
            expect(Blocks.PLAYER_WALL_HEAD.defaultBlockState(), d, Direction.NORTH, 0, 0f, "wall head " + d + " (heads: none)");
            expect(Blocks.ZOMBIE_HEAD.defaultBlockState(), d, Direction.NORTH, 0, 0f, "floor head " + d + " (heads: none)");
        }

        System.out.println("== slabs");
        expect(slab(SlabType.BOTTOM), Direction.DOWN, Direction.NORTH, Spots.ALL, 0.5f, "bottom slab top (half face)");
        check(!fit(slab(SlabType.BOTTOM), Direction.DOWN, Direction.NORTH).labelFits(), "bottom slab top: no label");
        expect(slab(SlabType.BOTTOM), Direction.UP, Direction.NORTH, Spots.ALL, 0f, "bottom slab bottom (full)");
        expect(slab(SlabType.TOP), Direction.UP, Direction.NORTH, Spots.ALL, 0.5f, "top slab bottom (half face)");
        expect(slab(SlabType.TOP), Direction.DOWN, Direction.NORTH, Spots.ALL, 0f, "top slab top (full)");
        for (Direction d : Direction.Plane.HORIZONTAL) {
            expect(slab(SlabType.BOTTOM), d, null, bits(Spots.BL, Spots.BR), 0f, "bottom slab side");
            expect(slab(SlabType.TOP), d, null, bits(Spots.TL, Spots.TR), 0f, "top slab side");
        }
        for (Direction d : Direction.values()) expect(slab(SlabType.DOUBLE), d, Direction.NORTH, Spots.ALL, 0f, "double slab");

        System.out.println("== stairs (bottom half)");
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            BlockState s = stairs(facing, Half.BOTTOM);
            // The host's back face is in direction `facing`; the cell on that side has face facing.getOpposite().
            expect(s, facing.getOpposite(), null, Spots.ALL, 0f, "stairs " + facing + " back");
            check(fit(s, facing.getOpposite(), null).labelFits(), "stairs " + facing + " back: a label fits");
            expect(s, facing, null, bits(Spots.BL, Spots.BR), 0f, "stairs " + facing + " front (riser)");
            expect(s, Direction.UP, Direction.NORTH, Spots.ALL, 0f, "stairs " + facing + " bottom");
            // Top: the back half; which two corners depends on the face rotation.
            for (Direction rot : Direction.Plane.HORIZONTAL) {
                Spots.Fit top = fit(s, Direction.DOWN, rot);
                int want;
                if (rot == facing) want = bits(Spots.TL, Spots.TR);
                else if (rot == facing.getOpposite()) want = bits(Spots.BL, Spots.BR);
                else if (rot.getClockWise() == facing) want = bits(Spots.TR, Spots.BR);
                else want = bits(Spots.TL, Spots.BL);
                check(top.mask() == want && top.inset() == 0f, "stairs " + facing + " top, rotation " + rot + ": "
                        + mask(top.mask()) + (top.mask() == want ? "" : " want " + mask(want)));
            }
            // Sides: the two bottom corners and the top corner on the back half.
            for (Direction side : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) {
                Spots.Fit f = fit(s, side.getOpposite(), null);
                // Seen from the cell on `side`, the frame's x runs along side.getOpposite().getClockWise().
                Direction xAxis = Spots.xAxis(side.getOpposite(), null);
                int topCorner = xAxis == facing ? Spots.TR : Spots.TL;
                int want = bits(Spots.BL, Spots.BR, topCorner);
                check(f.mask() == want && f.inset() == 0f, "stairs " + facing + " side " + side + ": " + mask(f.mask())
                        + (f.mask() == want ? "" : " want " + mask(want)));
            }
        }

        System.out.println("== stairs (top half)");
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            BlockState s = stairs(facing, Half.TOP);
            expect(s, facing.getOpposite(), null, Spots.ALL, 0f, "top stairs " + facing + " back");
            expect(s, facing, null, bits(Spots.TL, Spots.TR), 0f, "top stairs " + facing + " front (upper riser)");
            expect(s, Direction.DOWN, Direction.NORTH, Spots.ALL, 0f, "top stairs " + facing + " top");
            Spots.Fit bottom = fit(s, Direction.UP, facing);
            check(bottom.mask() == bits(Spots.TL, Spots.TR) && bottom.inset() == 0f,
                    "top stairs " + facing + " bottom, rotation " + facing + ": " + mask(bottom.mask()));
            for (Direction side : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) {
                Spots.Fit f = fit(s, side.getOpposite(), null);
                Direction xAxis = Spots.xAxis(side.getOpposite(), null);
                int bottomCorner = xAxis == facing ? Spots.BR : Spots.BL;
                int want = bits(Spots.TL, Spots.TR, bottomCorner);
                check(f.mask() == want, "top stairs " + facing + " side " + side + ": " + mask(f.mask())
                        + (f.mask() == want ? "" : " want " + mask(want)));
            }
        }
        BlockState inner = stairs(Direction.NORTH, Half.BOTTOM).setValue(StairBlock.SHAPE, StairsShape.INNER_LEFT);
        check(fit(inner, Direction.UP, Direction.NORTH).fiveSpot(), "inner-corner stairs bottom: five spots (shape test)");
        check(fit(inner, Direction.DOWN, Direction.NORTH).count() >= 2, "inner-corner stairs top: at least two spots (shape test)");

        System.out.println("== frame");
        // Wall NORTH (host north of the cell): x = EAST, y = UP, origin (0,0,0); fx 16 -> x 1; depth 1 -> z -1.
        Vec3 p = Spots.local(Direction.NORTH, null, 16, 16, 0);
        check(near(p, 1, 1, 0), "north wall frame corner: " + p);
        p = Spots.local(Direction.NORTH, null, 0, 0, 0.5);
        check(near(p, 0, 0, -0.5), "north wall frame depth: " + p);
        p = Spots.local(Direction.SOUTH, null, 16, 0, 0);
        check(near(p, 0, 0, 1), "south wall frame: x runs west: " + p);
        p = Spots.local(Direction.EAST, null, 16, 0, 0);
        check(near(p, 1, 0, 1), "east wall frame: x runs south: " + p);
        p = Spots.local(Direction.WEST, null, 16, 0, 0);
        check(near(p, 0, 0, 0), "west wall frame: x runs north: " + p);
        // Floor (host below), rotation NORTH: x = EAST, y = NORTH, origin (0,0,1).
        p = Spots.local(Direction.DOWN, Direction.NORTH, 16, 16, 0);
        check(near(p, 1, 0, 0), "floor frame rot north: " + p);
        p = Spots.local(Direction.DOWN, Direction.EAST, 16, 16, 0);
        check(near(p, 1, 0, 1), "floor frame rot east (x = SOUTH): " + p);
        p = Spots.local(Direction.UP, Direction.NORTH, 0, 0, 0);
        check(near(p, 0, 1, 1), "ceiling frame rot north origin: " + p);
        for (Direction face : Direction.values()) {
            for (Direction rot : Direction.Plane.HORIZONTAL) {
                Vec3 q = Spots.local(face, rot, 3.5, 12.25, 0.125);
                double[] f = Spots.frame(face, rot, q);
                check(Math.abs(f[0] - 3.5) < 1e-9 && Math.abs(f[1] - 12.25) < 1e-9 && Math.abs(f[2] - 0.125) < 1e-9,
                        "frame round trip " + face + "/" + rot);
            }
        }

        System.out.println("== boxes and shapes");
        AABB note = Spots.noteBox(Direction.NORTH, null, Spots.TL, 0f);
        check(near(note.minX, 0.5 / 16) && near(note.maxX, 6.5 / 16) && near(note.minY, 10.65 / 16) && near(note.maxY, 1)
                && near(note.minZ, 0) && near(note.maxZ, 2.76 / 16), "north TL note box: " + note);
        AABB slabNote = Spots.noteBox(Direction.DOWN, Direction.NORTH, Spots.C, 0.5f);
        check(near(slabNote.minY, -0.5) && near(slabNote.maxY, -0.5 + 2.76 / 16), "slab-top note box reaches into the host cell: " + slabNote);
        AABB label = Spots.labelBox(Direction.EAST, null, 1f / 16);
        check(near(label.minX, 1 + 1.0 / 16 - 0.175 / 16) && near(label.maxX, 1 + 1.0 / 16) && near(label.minZ, 3.0 / 16)
                && near(label.maxZ, 13.0 / 16) && near(label.minY, 3.0 / 16), "east label box at inset 1/16: " + label);

        FaceContent[] faces = new FaceContent[6];
        for (int i = 0; i < 6; i++) faces[i] = new FaceContent();
        FaceContent north = faces[Direction.NORTH.ordinal()];
        north.putNote(Spots.TL, new NoteData());
        north.putNote(Spots.BR, new NoteData());
        VoxelShape shape = Spots.shape(faces);
        check(shape.toAabbs().size() == 2, "two notes -> two boxes: " + shape.toAabbs().size());
        check(!north.isCovered(), "two notes: not covered");

        System.out.println("== hits");
        PieceHit h = PieceHit.resolve(faces, new Vec3(3.0 / 16, 13.0 / 16, 0.05));
        check(h != null && h.face() == Direction.NORTH && h.isNote() && h.spot() == Spots.TL, "hit inside TL: " + h);
        h = PieceHit.resolve(faces, new Vec3(12.0 / 16, 1.0 / 16, 0.0));
        check(h != null && h.isNote() && h.spot() == Spots.BR, "hit inside BR: " + h);
        h = PieceHit.resolve(faces, new Vec3(8.0 / 16, 8.0 / 16, 0.0));
        check(h != null && h.isNote(), "hit between notes resolves to the nearest: " + h);
        check(Spots.nearestSpot(Direction.NORTH, null, new Vec3(8.0 / 16, 8.0 / 16, 0), Spots.ALL) == Spots.C, "nearest spot at the centre = C");
        check(Spots.nearestSpot(Direction.NORTH, null, new Vec3(8.0 / 16, 8.0 / 16, 0), bits(Spots.TL, Spots.BR)) == Spots.TL
                || Spots.nearestSpot(Direction.NORTH, null, new Vec3(8.0 / 16, 8.0 / 16, 0), bits(Spots.TL, Spots.BR)) == Spots.BR,
                "nearest spot restricted to allowed spots");
        check(Spots.nearestSpot(Direction.NORTH, null, new Vec3(0.1, 0.1, 0), Spots.ALL) == Spots.BL, "nearest spot bottom-left = BL");
        check(Spots.spotAt(Direction.NORTH, null, new Vec3(8.0 / 16, 8.0 / 16, 0)) == Spots.C, "spotAt centre = C");
        check(Spots.spotAt(Direction.NORTH, null, new Vec3(8.0 / 16, 15.0 / 16, 0)) == -1, "spotAt between TL and TR = none");

        System.out.println("== covered face");
        for (int s = 0; s < Spots.COUNT; s++) north.putNote(s, new NoteData());
        check(north.isCovered(), "five notes: covered");
        shape = Spots.shape(faces);
        check(shape.toAabbs().size() == 1 && near(shape.bounds().getXsize(), 1) && near(shape.bounds().getYsize(), 1),
                "covered face: one 16x16 panel box: " + shape.bounds());
        h = PieceHit.resolve(faces, new Vec3(8.0 / 16, 15.0 / 16, 0.0));
        check(h != null && h.isPanel(), "hit between notes on a covered face = panel: " + h);
        h = PieceHit.resolve(faces, new Vec3(3.0 / 16, 13.0 / 16, 0.0));
        check(h != null && h.isNote() && h.spot() == Spots.TL, "hit on a note of a covered face = that note: " + h);
        north.removeNote(Spots.C);
        check(!north.isCovered() && north.noteCount() == 4, "four notes: not covered");
        FaceContent side = faces[Direction.EAST.ordinal()];
        side.inset = 0;
        side.putNote(Spots.BL, new NoteData());
        side.putNote(Spots.BR, new NoteData());
        check(!side.isCovered(), "a two-spot face never counts as covered");

        System.out.println("== floor rotation of spots");
        // On a floor the TL spot moves with the rotation: its centre sits at the rotation's "top-left".
        FaceContent[] floor = new FaceContent[6];
        for (int i = 0; i < 6; i++) floor[i] = new FaceContent();
        FaceContent down = floor[Direction.DOWN.ordinal()];
        down.rotation = Direction.NORTH;
        down.putNote(Spots.TL, new NoteData());
        AABB tlNorth = PieceHit.resolve(floor, new Vec3(0.5, 0, 0.5)).box(floor);
        down.rotation = Direction.SOUTH;
        AABB tlSouth = PieceHit.resolve(floor, new Vec3(0.5, 0, 0.5)).box(floor);
        check(tlNorth.minZ < 0.5 && tlNorth.minX < 0.5 && tlSouth.minZ > 0.5 && tlSouth.minX > 0.5,
                "floor TL note: north-west for rotation NORTH " + tlNorth + ", south-east for SOUTH " + tlSouth);
        down.rotation = Direction.EAST;
        AABB tlEast = PieceHit.resolve(floor, new Vec3(0.5, 0, 0.5)).box(floor);
        check(tlEast.minX > 0.5 && tlEast.minZ < 0.5, "floor TL note: north-east for rotation EAST " + tlEast);
        check(near(tlNorth.maxY, 0) && near(tlNorth.minY, 2.76 / 16 * -1 + 0) || near(tlNorth.minY, 0),
                "floor note depth is along y: " + tlNorth);

        System.out.println("== interactions: spot choice");
        Vec3 inTL = new Vec3(3.0 / 16, 13.0 / 16, 0);
        check(FacePaperInteractions.chooseSpot(Direction.NORTH, null, inTL, Spots.ALL) == Spots.TL, "spot under the hit when free");
        check(FacePaperInteractions.chooseSpot(Direction.NORTH, null, inTL, Spots.ALL & ~bits(Spots.TL)) == Spots.C
                || FacePaperInteractions.chooseSpot(Direction.NORTH, null, inTL, Spots.ALL & ~bits(Spots.TL)) == Spots.TR,
                "spot under the hit taken: a nearest free spot instead");
        check(FacePaperInteractions.chooseSpot(Direction.NORTH, null, inTL, bits(Spots.BR)) == Spots.BR, "only one free spot: that one");
        check(FacePaperInteractions.chooseSpot(Direction.NORTH, null, inTL, 0) == -1, "nothing free: -1");
        check(FacePaperInteractions.chooseSpot(Direction.NORTH, null, new Vec3(8.0 / 16, 15.0 / 16, 0), Spots.ALL) != -1
                && FacePaperInteractions.chooseSpot(Direction.NORTH, null, new Vec3(8.0 / 16, 15.0 / 16, 0), bits(Spots.BL, Spots.BR)) == Spots.BL
                || FacePaperInteractions.chooseSpot(Direction.NORTH, null, new Vec3(8.0 / 16, 15.0 / 16, 0), bits(Spots.BL, Spots.BR)) == Spots.BR,
                "between spots: the nearest free one");

        System.out.println("== interactions: host-gone re-fit");
        FaceContent wallFace = new FaceContent();
        for (int s = 0; s < Spots.COUNT; s++) wallFace.putNote(s, new NoteData(0x10 * s, null, "n" + s, 1, "Steve"));
        wallFace.inset = 0;
        FaceContent gone = FacePaperInteractions.refit(wallFace, fit(Blocks.STONE.defaultBlockState(), Direction.NORTH, null));
        check(gone.isEmpty() && wallFace.noteCount() == 5, "full block still: nothing removed");
        gone = FacePaperInteractions.refit(wallFace, fit(Blocks.CHEST.defaultBlockState(), Direction.NORTH, null));
        check(gone.isEmpty() && wallFace.noteCount() == 5 && near(wallFace.inset, 1.0 / 16), "stone -> chest: nothing removed, inset follows: " + wallFace.inset);
        gone = FacePaperInteractions.refit(wallFace, fit(slab(SlabType.BOTTOM), Direction.NORTH, null));
        check(gone.kind == FaceContent.Kind.NOTES && gone.noteMask() == bits(Spots.TL, Spots.TR, Spots.C), "chest -> bottom slab side: TL, TR, C removed: " + mask(gone.noteMask()));
        check(wallFace.noteMask() == bits(Spots.BL, Spots.BR) && wallFace.kind == FaceContent.Kind.NOTES && near(wallFace.inset, 0), "bottom corners stay: " + mask(wallFace.noteMask()));
        check(gone.note(Spots.C) != null && gone.note(Spots.C).text.equals("n2") && gone.note(Spots.C).color == 0x20, "removed notes keep their data");
        gone = FacePaperInteractions.refit(wallFace, fit(Blocks.AIR.defaultBlockState(), Direction.NORTH, null));
        check(gone.noteMask() == bits(Spots.BL, Spots.BR) && wallFace.isEmpty() && wallFace.rotation == null && wallFace.inset == 0, "host gone: the rest removed, face cleared");
        check(FacePaperInteractions.refit(wallFace, Spots.Fit.NONE).isEmpty(), "re-fit of an empty face: nothing");
        FaceContent floorLabel = new FaceContent();
        floorLabel.setLabel(new LabelData());
        floorLabel.rotation = Direction.EAST;
        gone = FacePaperInteractions.refit(floorLabel, fit(Blocks.STONE.defaultBlockState(), Direction.DOWN, Direction.EAST));
        check(gone.isEmpty() && floorLabel.hasLabel(), "label on a full floor: kept");
        gone = FacePaperInteractions.refit(floorLabel, fit(Blocks.CHEST.defaultBlockState(), Direction.DOWN, Direction.EAST));
        check(gone.isEmpty() && floorLabel.hasLabel() && near(floorLabel.inset, 2.0 / 16), "label over a chest lid: kept at inset 2/16: " + floorLabel.inset);
        gone = FacePaperInteractions.refit(floorLabel, fit(slab(SlabType.BOTTOM), Direction.DOWN, Direction.EAST));
        check(gone.hasLabel() && floorLabel.isEmpty() && floorLabel.rotation == null, "label over a slab top (inset 0.5): removed, face cleared");

        System.out.println("== interactions: removing one piece");
        FaceContent[] cell = new FaceContent[6];
        for (int i = 0; i < 6; i++) cell[i] = new FaceContent();
        FaceContent n = cell[Direction.NORTH.ordinal()];
        for (int s = 0; s < Spots.COUNT; s++) n.putNote(s, new NoteData(NoteData.NO_COLOR, null, "n" + s, 1, "Steve"));
        PieceHit onTL = PieceHit.resolve(cell, new Vec3(3.0 / 16, 13.0 / 16, 0.0));
        FaceContent taken = FacePaperInteractions.removePiece(n, onTL, new Vec3(3.0 / 16, 13.0 / 16, 0.0));
        check(taken.noteMask() == bits(Spots.TL) && taken.note(Spots.TL).text.equals("n0") && n.noteCount() == 4 && !n.isCovered(),
                "hit note removed with its data; four left, no longer covered");
        n.putNote(Spots.TL, taken.note(Spots.TL));
        Vec3 betweenTop = new Vec3(8.0 / 16, 15.0 / 16, 0.0);
        PieceHit panel = PieceHit.resolve(cell, betweenTop);
        check(panel != null && panel.isPanel(), "covered face, between notes: panel");
        taken = FacePaperInteractions.removePiece(n, panel, betweenTop);
        check(taken.noteCount() == 1 && (taken.hasNote(Spots.TL) || taken.hasNote(Spots.TR)) && n.noteCount() == 4,
                "panel hit removes the nearest note: " + mask(taken.noteMask()));
        FaceContent lf = cell[Direction.EAST.ordinal()];
        LabelData ld = new LabelData();
        ld.glow = true;
        lf.setLabel(ld);
        PieceHit onLabel = PieceHit.resolve(cell, new Vec3(1.0, 0.5, 0.5));
        check(onLabel != null && onLabel.isLabel() && onLabel.face() == Direction.EAST, "hit on the label face resolves to the label: " + onLabel);
        taken = FacePaperInteractions.removePiece(lf, onLabel, new Vec3(1.0, 0.5, 0.5));
        check(taken.hasLabel() && taken.label.glow && lf.isEmpty(), "label removed, face cleared");
        check(FacePaperInteractions.removePiece(lf, onLabel, Vec3.ZERO).isEmpty(), "removing from an empty face: nothing");

        System.out.println("== interactions: pick-up and eraser");
        NoteData gold = new NoteData(NoteData.NO_COLOR, "gold", "hi", 3, "Alex");
        NoteData kept = FacePaperInteractions.pickedUp(gold, true);
        check(kept.isSpecial() && kept.text.equals("hi") && kept != gold, "keepSpecialColor on: a copy that stays gold");
        NoteData plainBack = FacePaperInteractions.pickedUp(gold, false);
        check(plainBack.isPlain() && plainBack.text.equals("hi") && gold.isSpecial(), "keepSpecialColor off: comes back plain, text kept, source untouched");
        NoteData dyedBack = FacePaperInteractions.pickedUp(new NoteData(0xABCDEF, null, "", 0, ""), false);
        check(dyedBack.color == 0xABCDEF, "keepSpecialColor off only strips specials, a dyed note keeps its colour");
        NoteData e = new NoteData(0xABCDEF, null, "buy milk", 0, "");
        check(FacePaperInteractions.erase(e) && e.text.isEmpty() && e.color == 0xABCDEF, "eraser: text first, colour stays");
        check(FacePaperInteractions.erase(e) && e.isPlain(), "eraser again: colour removed");
        check(!FacePaperInteractions.erase(e), "eraser on a plain blank note: nothing");
        NoteData sp = new NoteData(NoteData.NO_COLOR, "creeper", "", 0, "");
        check(FacePaperInteractions.erase(sp) && sp.isPlain(), "eraser on a blank special: plain");

        surfaceTests(stone, chest, carpet);

        System.out.println(fails == 0 ? "ALL " + checks + " CHECKS PASSED" : fails + " FAILED of " + checks);
        System.exit(fails == 0 ? 0 : 1);
    }

    static boolean near(double a, double b) {
        return Math.abs(a - b) < 1e-6;
    }

    static boolean placeIs(Spots.Place p, float inset, float offX, float offY, float size) {
        return p != null && Math.abs(p.inset() - inset) < 1e-4 && Math.abs(p.offX() - offX) < 1e-4
                && Math.abs(p.offY() - offY) < 1e-4 && Math.abs(p.size() - size) < 1e-4;
    }

    static void expectLabel(Spots.Place p, float inset, float offX, float offY, float size, String what) {
        check(placeIs(p, inset, offX, offY, size), what + ": got " + p + ", want " + inset + " / " + offX + "," + offY + " / " + size);
    }

    static void expectCentreNote(Spots.Fit f, float inset, float offY, String what) {
        boolean ok = f.mask() == Spots.CENTRE_ONLY && Math.abs(f.inset() - inset) < 1e-4 && Math.abs(f.noteOffX()) < 1e-4
                && Math.abs(f.noteOffY() - offY) < 1e-4;
        check(ok, what + ": got " + mask(f.mask()) + " @ " + f.inset() + " off " + f.noteOffX() + "," + f.noteOffY()
                + (ok ? "" : ", want C @ " + inset + " off 0," + offY));
    }

    /**
     * Label surface test and centred notes (sticky_notes.md 3.3a), on the real shapes of the modded hosts (read from
     * their jars 2026-11-02) and on vanilla blocks.
     */
    static void surfaceTests(BlockState stone, BlockState chest, BlockState carpet) {
        VoxelShape jar = Shapes.or(Block.box(3, 0, 3, 13, 14, 13), Block.box(5, 14, 5, 11, 16, 11));
        VoxelShape sack = Shapes.or(Block.box(2, 0, 2, 14, 12, 14), Block.box(6, 12, 6, 10, 13, 10), Block.box(5, 13, 5, 11, 16, 11));
        VoxelShape trashCan = Shapes.or(Shapes.box(0.1875, 0, 0.1875, 0.8125, 0.75, 0.8125),
                Shapes.box(0.125, 0.75, 0.125, 0.875, 0.8125, 0.875), Shapes.box(0.1875, 0.78125, 0.1875, 0.8125, 0.84375, 0.8125));
        VoxelShape fermentingJar = Block.box(3.25, 0, 3.25, 12.75, 13.5, 12.75);
        VoxelShape botanyPot = Block.box(2, 0, 2, 14, 8, 14);
        Direction N = Direction.NORTH;

        System.out.println("== labels: surface test, modded shapes");
        for (Direction side : Direction.Plane.HORIZONTAL) {
            expectLabel(Spots.labelSurface(jar, side, null), 3f / 16, 0, 0, 10, "Supplementaries jar side " + side);
            expectLabel(Spots.labelSurface(sack, side, null), 2f / 16, 0, -1, 10, "sack side " + side + " (sits low, below the neck)");
            expectLabel(Spots.labelSurface(trashCan, side, null), 2f / 16, 0, 0, 10, "trash can side " + side + " (rim and body together)");
            expectLabel(Spots.labelSurface(fermentingJar, side, null), 3.25f / 16, 0, 0, 10, "fermenting jar side " + side);
            expectLabel(Spots.labelSurface(botanyPot, side, null), 2f / 16, 0, -4, 8, "botany pot side " + side + " (8 px, shrunk)");
        }
        expectLabel(Spots.labelSurface(jar, Direction.DOWN, N), 0, 0, 0, 6, "jar top: 6 px label on the lid (body 2 px lower is left out)");
        expectLabel(Spots.labelSurface(botanyPot, Direction.DOWN, N), 0.5f, 0, 0, 10, "botany pot top (8 px in): label on the soil");

        System.out.println("== labels: surface test, vanilla");
        expectLabel(fit(stone, N, null).label(), 0, 0, 0, 10, "stone: the standard label");
        expectLabel(fit(chest, N, null).label(), 1f / 16, 0, 0, 10, "chest side: standard label at 1/16");
        expectLabel(fit(chest, Direction.DOWN, N).label(), 2f / 16, 0, 0, 10, "chest lid: standard label at 2/16");
        BlockState cauldron = Blocks.CAULDRON.defaultBlockState();
        for (Direction side : Direction.Plane.HORIZONTAL)
            expectLabel(fit(cauldron, side, null).label(), 0, 0, 0, 10, "cauldron side " + side + " (2 px walls count)");
        check(fit(cauldron, Direction.DOWN, N).label() == null, "cauldron top (rim only): no label");
        expectLabel(fit(Blocks.COMPOSTER.defaultBlockState(), N, null).label(), 0, 0, 0, 10, "composter side");
        expectLabel(fit(Blocks.FLOWER_POT.defaultBlockState(), N, null).label(), 5f / 16, 0, -5, 6, "flower pot side: 6 px label");
        check(fit(carpet, Direction.DOWN, N).label() == null, "carpet top (15 px in): no label");
        check(fit(Blocks.OAK_PRESSURE_PLATE.defaultBlockState(), Direction.DOWN, N).label() == null, "pressure plate: no label");
        check(fit(Blocks.STONE_BUTTON.defaultBlockState(), N, null).label() == null, "button: no label");
        check(fit(Blocks.GRASS.defaultBlockState(), N, null).label() == null, "grass (no collision): no label");
        check(fit(Blocks.LADDER.defaultBlockState(), Direction.SOUTH, null).label() == null, "ladder front (13 px in): no label");
        for (Direction d : Direction.values()) {
            check(fit(Blocks.OAK_DOOR.defaultBlockState(), d, Direction.NORTH).label() == null, "door face " + d + ": no label");
            check(fit(Blocks.OAK_TRAPDOOR.defaultBlockState(), d, Direction.NORTH).label() == null, "trapdoor face " + d + ": no label");
        }
        check(fit(slab(SlabType.BOTTOM), Direction.DOWN, N).label() == null, "bottom slab top (inset face): no label");
        check(fit(slab(SlabType.TOP), Direction.UP, N).label() == null, "top slab bottom (inset face): no label");
        check(fit(slab(SlabType.BOTTOM), N, null).label() == null, "bottom slab side: no label");
        expectLabel(fit(slab(SlabType.DOUBLE), N, null).label(), 0, 0, 0, 10, "double slab: standard label");
        BlockState st = stairs(Direction.NORTH, Half.BOTTOM);
        check(fit(st, Direction.EAST, null).label() == null && fit(st, Direction.WEST, null).label() == null, "stair sides: no label");
        check(fit(st, Direction.NORTH, null).label() == null, "stair front: no label");
        check(fit(st, Direction.DOWN, N).label() == null, "stair top: no label");
        expectLabel(fit(st, Direction.SOUTH, null).label(), 0, 0, 0, 10, "stair back: standard label");

        System.out.println("== centred notes");
        for (Direction side : Direction.Plane.HORIZONTAL) {
            expectCentreNote(Spots.centreNoteFit(jar, side, null), 3f / 16, -1, "jar side " + side + " (body, not the lid)");
            expectCentreNote(Spots.centreNoteFit(sack, side, null), 2f / 16, -2, "sack side " + side);
            expectCentreNote(Spots.centreNoteFit(trashCan, side, null), 2f / 16, -1.25f, "trash can side " + side);
            expectCentreNote(Spots.centreNoteFit(fermentingJar, side, null), 3.25f / 16, -1.25f, "fermenting jar side " + side);
            expectCentreNote(Spots.centreNoteFit(botanyPot, side, null), 2f / 16, -4, "botany pot side " + side);
        }
        check(!Spots.shapeFit(jar, N, null).any(), "jar side without the tag: no notes (shape test)");
        Spots.HostRules centre = new Spots.HostRules() {
            @Override public boolean deny(BlockState host) { return false; }
            @Override public boolean post(BlockState host) { return false; }
            @Override public boolean centreNote(BlockState host) { return host.is(Blocks.CAULDRON); }
        };
        BlockState cauldronState = Blocks.CAULDRON.defaultBlockState();
        Spots.Fit tagged = Spots.fit(cauldronState, LEVEL, POS, N, null, centre);
        check(tagged.mask() == Spots.CENTRE_ONLY && tagged.label() != null, "centre-note rule via fit: side = C only, label still fits: " + tagged);
        check(!Spots.fit(cauldronState, LEVEL, POS, Direction.DOWN, N, centre).any(), "centre-note rule: top uses the shape test (nothing)");
        check(!Spots.fit(cauldronState, LEVEL, POS, N, null, NO_TAGS).any(), "cauldron side untagged: no notes");

        System.out.println("== placement: boxes, hits, re-fit, NBT");
        Spots.Fit potFit = Spots.centreNoteFit(botanyPot, N, null);
        AABB nb = Spots.noteBox(N, null, potFit.notePlace(), Spots.C);
        check(near(nb.minY, (5.33 - 4) / 16) && near(nb.maxY, (10.67 - 4) / 16) && near(nb.minX, 5.0 / 16) && near(nb.maxX, 11.0 / 16),
                "centred note box on a botany pot moved down 4 px: " + nb);
        FaceContent[] faces = new FaceContent[6];
        for (int i = 0; i < 6; i++) faces[i] = new FaceContent();
        FaceContent potFace = faces[N.ordinal()];
        potFace.putNote(Spots.C, new NoteData(NoteData.NO_COLOR, null, "seeds", 0, ""));
        potFace.setPlace(potFit.notePlace());
        PieceHit h = PieceHit.resolve(faces, nb.getCenter());
        check(h != null && h.isNote() && h.spot() == Spots.C && near(h.box(faces).minY, nb.minY), "hit on the moved note: " + h);
        FaceContent loaded = FaceContent.load(potFace.save());
        check(placeIs(loaded.place(), 2f / 16, 0, -4, 10), "shift survives save and load: " + loaded.place());
        check(!new FaceContent().save().contains(FaceContent.KEY_OFF_Y) && FaceContent.load(stoneLabelFace().save()).labelSize == 10f,
                "default layout writes no new keys and loads at 10 px");
        FaceContent gone = FacePaperInteractions.refit(potFace, Spots.centreNoteFit(sack, N, null));
        check(gone.isEmpty() && placeIs(potFace.place(), 2f / 16, 0, -2, 10), "pot -> sack: the note stays, re-centred: " + potFace.place());
        gone = FacePaperInteractions.refit(potFace, fit(stone, N, null));
        check(gone.isEmpty() && placeIs(potFace.place(), 0, 0, 0, 10), "-> stone (C allowed): back to the standard spot");
        FaceContent lf = new FaceContent();
        lf.setLabel(new LabelData());
        lf.setPlace(Spots.labelSurface(botanyPot, N, null));
        AABB lb = Spots.labelBox(N, null, lf.place());
        check(near(lb.minY, 0) && near(lb.maxY, 8.0 / 16) && near(lb.minX, 4.0 / 16) && near(lb.maxX, 12.0 / 16), "8 px label box on a botany pot: " + lb);
        gone = FacePaperInteractions.refit(lf, fit(stone, N, null));
        check(gone.isEmpty() && lf.hasLabel() && placeIs(lf.place(), 0, 0, 0, 10), "pot -> stone: label grows back to 10 px");
        gone = FacePaperInteractions.refit(lf, fit(Blocks.OAK_DOOR.defaultBlockState(), N, null));
        check(gone.hasLabel() && lf.isEmpty(), "stone -> door: label dropped");
    }

    static FaceContent stoneLabelFace() {
        FaceContent c = new FaceContent();
        c.setLabel(new LabelData());
        return c;
    }

    static boolean near(Vec3 p, double x, double y, double z) {
        return near(p.x, x) && near(p.y, y) && near(p.z, z);
    }
}
