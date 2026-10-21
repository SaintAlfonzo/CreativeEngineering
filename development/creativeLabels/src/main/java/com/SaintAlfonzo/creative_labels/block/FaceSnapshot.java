package com.SaintAlfonzo.creative_labels.block;

import net.minecraft.core.Direction;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;

/**
 * Immutable copy of one face's content for the dynamic model (BRIEF 4): built by the block entity on the main thread
 * ({@link FacePaperBlockEntity#getModelData()}), read by chunk-build worker threads. Lives in the common block
 * package so the block entity never references a client class; {@code client.FacePaperModel.FACES} aliases
 * {@link #FACES}.
 *
 * @param kind     NONE, NOTES or LABEL
 * @param rotation face rotation (floors and ceilings), null on walls
 * @param inset    paper surface depth into the host, blocks
 * @param notes    one entry per spot (index = spot), null = empty; the array is private to the snapshot: never mutate
 * @param label    the label's look, null unless kind is LABEL
 */
public record FaceSnapshot(FaceContent.Kind kind, @Nullable Direction rotation, float inset, NoteLook[] notes,
                           @Nullable LabelLook label) {

    /** Six entries, index = Direction ordinal; a face with nothing on it is {@link #EMPTY}. */
    public static final ModelProperty<FaceSnapshot[]> FACES = new ModelProperty<>();

    public static final FaceSnapshot EMPTY = new FaceSnapshot(FaceContent.Kind.NONE, null, 0, new NoteLook[Spots.COUNT], null);

    /** What a note looks like: its colour ({@link NoteData#NO_COLOR} = plain yellow), special look, pen strokes. */
    public record NoteLook(int color, @Nullable String special, boolean written) {}

    /** What the label paper looks like in the chunk mesh (the icon is the block entity renderer's job). */
    public record LabelLook(boolean hasPicture, boolean glow) {}

    public static FaceSnapshot of(FaceContent c) {
        if (c.kind == FaceContent.Kind.NONE) return EMPTY;
        NoteLook[] looks = new NoteLook[Spots.COUNT];
        if (c.kind == FaceContent.Kind.NOTES) {
            for (int i = 0; i < Spots.COUNT; i++) {
                NoteData n = c.notes[i];
                if (n != null) looks[i] = new NoteLook(n.color, n.special, n.isWritten());
            }
        }
        LabelLook label = c.kind == FaceContent.Kind.LABEL && c.label != null
                ? new LabelLook(c.label.hasPicture(), c.label.glow) : null;
        return new FaceSnapshot(c.kind, c.rotation, c.inset, looks, label);
    }

    public boolean isEmpty() {
        return kind == FaceContent.Kind.NONE;
    }

    @Nullable
    public NoteLook note(int spot) {
        return notes[spot];
    }

    public boolean hasNote(int spot) {
        return kind == FaceContent.Kind.NOTES && notes[spot] != null;
    }

    public int noteCount() {
        int n = 0;
        for (NoteLook l : notes) if (l != null) n++;
        return n;
    }

    public boolean isCovered() {
        return kind == FaceContent.Kind.NOTES && noteCount() == Spots.COUNT;
    }
}
