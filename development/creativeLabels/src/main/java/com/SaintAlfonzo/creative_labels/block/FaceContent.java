package com.SaintAlfonzo.creative_labels.block;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * What one face of a {@code face_paper} cell holds: nothing, up to five notes (one per {@link Spots} spot) or one
 * label; exclusive (sticky_notes.md 3.2). Plus the face rotation (floors and ceilings only, 3.5) and the host
 * inset (how far into the host cell the paper surface sits, 0 to 0.5 blocks; BRIEF 3), the pieces' shift in the face
 * frame (centred notes, fitted labels) and the label's edge ({@link Spots.Place}).
 */
public final class FaceContent {

    public enum Kind { NONE, NOTES, LABEL }

    public static final String KEY_KIND = "Kind";
    public static final String KEY_NOTES = "Notes";
    public static final String KEY_SPOT = "Spot";
    public static final String KEY_LABEL = "Label";
    public static final String KEY_ROTATION = "Rotation";
    public static final String KEY_INSET = "Inset";
    public static final String KEY_OFF_X = "OffX";
    public static final String KEY_OFF_Y = "OffY";
    public static final String KEY_SIZE = "Size";

    public Kind kind = Kind.NONE;
    /** Index = spot ({@link Spots#TL} .. {@link Spots#BR}); null = empty. */
    public final NoteData[] notes = new NoteData[Spots.COUNT];
    @Nullable
    public LabelData label;
    /** Horizontal; only meaningful on UP and DOWN faces; set with the first piece, cleared when the face empties. */
    @Nullable
    public Direction rotation;
    /** Blocks into the host cell (0 = at the cell boundary, 1/16 chests, 0.5 slab halves). */
    public float inset;
    /** Shift of the pieces in the face frame, px (notes: every spot; label: its centre off (8, 8)). */
    public float offX, offY;
    /** The label's edge, px. */
    public float labelSize = Spots.LABEL_SIZE;

    /** Where this face's pieces sit. */
    public Spots.Place place() {
        return new Spots.Place(inset, offX, offY, labelSize);
    }

    public void setPlace(Spots.Place p) {
        inset = p.inset();
        offX = p.offX();
        offY = p.offY();
        labelSize = p.size();
    }

    public boolean isEmpty() {
        return kind == Kind.NONE;
    }

    public int noteCount() {
        int n = 0;
        for (NoteData note : notes) if (note != null) n++;
        return n;
    }

    /** Bit i set = spot i holds a note. */
    public int noteMask() {
        int m = 0;
        for (int i = 0; i < notes.length; i++) if (notes[i] != null) m |= 1 << i;
        return m;
    }

    public boolean hasNote(int spot) {
        return kind == Kind.NOTES && notes[spot] != null;
    }

    @Nullable
    public NoteData note(int spot) {
        return kind == Kind.NOTES ? notes[spot] : null;
    }

    /**
     * Covered face (3.3): all five spots hold notes. Notes only ever land on allowed spots, so five notes imply a
     * five-spot face; faces that can't hold five never count as covered.
     */
    public boolean isCovered() {
        return kind == Kind.NOTES && noteCount() == Spots.COUNT;
    }

    public boolean hasLabel() {
        return kind == Kind.LABEL && label != null;
    }

    /** Puts a note on a spot (replacing any there); the face becomes NOTES (a label there is discarded). */
    public void putNote(int spot, NoteData note) {
        if (kind != Kind.NOTES) {
            label = null;
            kind = Kind.NOTES;
        }
        notes[spot] = note;
    }

    /** Removes and returns the note on a spot (null if none); an emptied face is cleared. */
    @Nullable
    public NoteData removeNote(int spot) {
        if (kind != Kind.NOTES) return null;
        NoteData n = notes[spot];
        notes[spot] = null;
        if (noteCount() == 0) clear();
        return n;
    }

    public void setLabel(LabelData l) {
        for (int i = 0; i < notes.length; i++) notes[i] = null;
        kind = Kind.LABEL;
        label = l;
    }

    @Nullable
    public LabelData removeLabel() {
        if (kind != Kind.LABEL) return null;
        LabelData l = label;
        clear();
        return l;
    }

    /** Back to nothing: kind NONE, no pieces, rotation and inset reset. */
    public void clear() {
        kind = Kind.NONE;
        for (int i = 0; i < notes.length; i++) notes[i] = null;
        label = null;
        rotation = null;
        setPlace(Spots.Place.ZERO);
    }

    /** Every piece as an item: notes with colour, special and text; a label blank (spec 3.2). */
    public List<ItemStack> drops() {
        List<ItemStack> out = new ArrayList<>();
        if (kind == Kind.NOTES) {
            for (NoteData n : notes) if (n != null) out.add(n.toItem());
        } else if (kind == Kind.LABEL && label != null) {
            out.add(label.toItem());
        }
        return out;
    }

    public FaceContent copy() {
        FaceContent c = new FaceContent();
        c.kind = kind;
        for (int i = 0; i < notes.length; i++) c.notes[i] = notes[i] == null ? null : notes[i].copy();
        c.label = label == null ? null : label.copy();
        c.rotation = rotation;
        c.setPlace(place());
        return c;
    }

    /** Immutable view for the model (built on the main thread, read by chunk-build workers). */
    public FaceSnapshot snapshot() {
        return FaceSnapshot.of(this);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY_KIND, kind.name().toLowerCase());
        if (kind == Kind.NOTES) {
            ListTag list = new ListTag();
            for (int i = 0; i < notes.length; i++) {
                if (notes[i] == null) continue;
                CompoundTag n = notes[i].save();
                n.putByte(KEY_SPOT, (byte) i);
                list.add(n);
            }
            tag.put(KEY_NOTES, list);
        } else if (kind == Kind.LABEL && label != null) {
            tag.put(KEY_LABEL, label.save());
        }
        if (rotation != null) tag.putString(KEY_ROTATION, rotation.getName());
        if (inset != 0) tag.putFloat(KEY_INSET, inset);
        if (offX != 0) tag.putFloat(KEY_OFF_X, offX);
        if (offY != 0) tag.putFloat(KEY_OFF_Y, offY);
        if (labelSize != Spots.LABEL_SIZE) tag.putFloat(KEY_SIZE, labelSize);
        return tag;
    }

    public static FaceContent load(CompoundTag tag) {
        FaceContent c = new FaceContent();
        String kind = tag.getString(KEY_KIND);
        if (kind.equals("notes")) {
            ListTag list = tag.getList(KEY_NOTES, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag n = list.getCompound(i);
                int spot = n.getByte(KEY_SPOT);
                if (spot < 0 || spot >= Spots.COUNT) continue;
                c.notes[spot] = NoteData.load(n);
            }
            c.kind = c.noteCount() > 0 ? Kind.NOTES : Kind.NONE;
        } else if (kind.equals("label") && tag.contains(KEY_LABEL, Tag.TAG_COMPOUND)) {
            c.kind = Kind.LABEL;
            c.label = LabelData.load(tag.getCompound(KEY_LABEL));
        }
        if (c.kind == Kind.NONE) return c;
        if (tag.contains(KEY_ROTATION, Tag.TAG_STRING)) {
            Direction r = Direction.byName(tag.getString(KEY_ROTATION));
            if (r != null && r.getAxis().isHorizontal()) c.rotation = r;
        }
        c.inset = tag.getFloat(KEY_INSET);
        c.offX = tag.getFloat(KEY_OFF_X);
        c.offY = tag.getFloat(KEY_OFF_Y);
        c.labelSize = tag.contains(KEY_SIZE, Tag.TAG_FLOAT) ? tag.getFloat(KEY_SIZE) : Spots.LABEL_SIZE;
        return c;
    }

    @Override
    public String toString() {
        return "FaceContent{" + kind + ", mask=" + Integer.toBinaryString(noteMask()) + ", label=" + label
                + ", rotation=" + rotation + ", inset=" + inset + ", off=" + offX + "," + offY + ", size=" + labelSize + "}";
    }
}
