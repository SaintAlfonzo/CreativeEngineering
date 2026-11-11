package com.SaintAlfonzo.creativelabels.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Which piece of a face_paper cell a hit lands on (BRIEF 3): a note on a spot, a label, or the panel of a covered
 * face (a hit between the notes of a covered face). The nearest piece wins when the point is between pieces.
 *
 * @param face the cell face the piece is on
 * @param kind NOTE, LABEL or PANEL
 * @param spot the spot for a NOTE, else -1
 */
public record PieceHit(Direction face, Kind kind, int spot) {

    public enum Kind { NOTE, LABEL, PANEL }

    public boolean isNote() {
        return kind == Kind.NOTE;
    }

    public boolean isLabel() {
        return kind == Kind.LABEL;
    }

    public boolean isPanel() {
        return kind == Kind.PANEL;
    }

    /** The piece's note, or null. */
    @Nullable
    public NoteData note(FaceContent[] faces) {
        return kind == Kind.NOTE ? faces[face.ordinal()].note(spot) : null;
    }

    /** The piece's box in cell-local coordinates. */
    public AABB box(FaceContent[] faces) {
        FaceContent c = faces[face.ordinal()];
        return switch (kind) {
            case NOTE -> Spots.noteBox(face, c.rotation, c.place(), spot);
            case LABEL -> Spots.labelBox(face, c.rotation, c.place());
            case PANEL -> Spots.panelBox(face, c.rotation, c.inset);
        };
    }

    /** Resolves a world-space hit location against a block entity; null when the cell holds nothing. */
    @Nullable
    public static PieceHit resolve(BlockState state, FacePaperBlockEntity be, Vec3 hitLocation) {
        BlockPos pos = be.getBlockPos();
        return resolve(be.faces(), hitLocation.subtract(pos.getX(), pos.getY(), pos.getZ()));
    }

    /**
     * Resolves a cell-local point. A note inside its box wins over the panel of a covered face; otherwise the piece
     * nearest to the point. Null when no face holds anything.
     */
    @Nullable
    public static PieceHit resolve(FaceContent[] faces, Vec3 local) {
        PieceHit best = null;
        double bestDist = Double.MAX_VALUE;
        for (Direction d : Direction.values()) {
            FaceContent c = faces[d.ordinal()];
            if (c == null || c.isEmpty()) continue;
            if (c.kind == FaceContent.Kind.LABEL) {
                double dist = Spots.boxDistance(Spots.labelBox(d, c.rotation, c.place()), local);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = new PieceHit(d, Kind.LABEL, -1);
                }
                continue;
            }
            for (int spot = 0; spot < Spots.COUNT; spot++) {
                if (c.notes[spot] == null) continue;
                double dist = Spots.boxDistance(Spots.noteBox(d, c.rotation, c.place(), spot), local);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = new PieceHit(d, Kind.NOTE, spot);
                }
            }
            if (c.isCovered()) {
                // Strictly nearer only: a point on a note's box stays that note; between notes it is the panel.
                double dist = Spots.boxDistance(Spots.panelBox(d, c.rotation, c.inset), local);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = new PieceHit(d, Kind.PANEL, -1);
                }
            }
        }
        return best;
    }
}
