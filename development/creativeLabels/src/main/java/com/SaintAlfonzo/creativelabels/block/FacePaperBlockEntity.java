package com.SaintAlfonzo.creativelabels.block;

import com.SaintAlfonzo.creativelabels.CLRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * The six faces of a face_paper cell (BRIEF 3). No ticker. NBT: {@code Faces} (compound keyed by direction name,
 * only faces with content) and {@code Mask} (the state's six booleans at save time, C7: a structure that rotated or
 * mirrored the block state is detected on load and the faces are remapped the same way). Synced whole with the
 * update tag / {@link ClientboundBlockEntityDataPacket}; the client then refreshes model data and the chunk section.
 */
public class FacePaperBlockEntity extends BlockEntity {

    public static final String KEY_FACES = "Faces";
    public static final String KEY_MASK = "Mask";

    private final FaceContent[] faces = new FaceContent[6];
    @Nullable
    private VoxelShape cachedShape;

    public FacePaperBlockEntity(BlockPos pos, BlockState state) {
        super(CLRegistry.FACE_PAPER_BE.get(), pos, state);
        for (int i = 0; i < 6; i++) faces[i] = new FaceContent();
    }

    public FaceContent face(Direction d) {
        return faces[d.ordinal()];
    }

    /** The live array (index = Direction ordinal); callers that change it call {@link #sync()} after. */
    public FaceContent[] faces() {
        return faces;
    }

    public boolean isEmpty() {
        for (FaceContent c : faces) if (!c.isEmpty()) return false;
        return true;
    }

    /** Bit = Direction ordinal of every face holding something. */
    public int contentMask() {
        int m = 0;
        for (int i = 0; i < 6; i++) if (!faces[i].isEmpty()) m |= 1 << i;
        return m;
    }

    /** How many pieces the cell holds (every note, each label). */
    public int pieceCount() {
        int n = 0;
        for (FaceContent c : faces) {
            if (c.kind == FaceContent.Kind.NOTES) n += c.noteCount();
            else if (c.hasLabel()) n++;
        }
        return n;
    }

    /** Every piece as an item (notes with their data, labels blank). */
    public List<ItemStack> allDrops() {
        List<ItemStack> out = new ArrayList<>();
        for (FaceContent c : faces) out.addAll(c.drops());
        return out;
    }

    /** Hit shape of the cell, cached until the content changes. */
    public VoxelShape shape() {
        VoxelShape s = cachedShape;
        if (s == null) cachedShape = s = Spots.shape(faces);
        return s;
    }

    /** Forgets the cached shape (the content changed); {@link #sync()} does this too. */
    public void markShapeDirty() {
        cachedShape = null;
    }

    /**
     * After a content change on the server: save, resync the client and the shape. On the client (local edits
     * before the server answers): refresh model data and the section.
     */
    public void sync() {
        markShapeDirty();
        setChanged();
        if (level == null) return;
        BlockState state = getBlockState();
        if (level.isClientSide) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, state, state, 8);
        } else {
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    // ----- NBT -----

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag list = new CompoundTag();
        for (Direction d : Direction.values()) {
            FaceContent c = faces[d.ordinal()];
            if (!c.isEmpty()) list.put(d.getName(), c.save());
        }
        tag.put(KEY_FACES, list);
        tag.putByte(KEY_MASK, (byte) maskOf(getBlockState()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        FaceContent[] loaded = new FaceContent[6];
        CompoundTag list = tag.getCompound(KEY_FACES);
        for (Direction d : Direction.values()) {
            loaded[d.ordinal()] = list.contains(d.getName(), Tag.TAG_COMPOUND)
                    ? FaceContent.load(list.getCompound(d.getName())) : new FaceContent();
        }
        if (tag.contains(KEY_MASK, Tag.TAG_ANY_NUMERIC)) {
            int saved = tag.getByte(KEY_MASK) & 0x3F;
            int now = maskOf(getBlockState());
            if (saved != now) loaded = remap(loaded, saved, now);
        }
        System.arraycopy(loaded, 0, faces, 0, 6);
        markShapeDirty();
    }

    /** Bit = Direction ordinal of every face boolean set in the state (0 for a state without them). */
    public static int maskOf(BlockState state) {
        int m = 0;
        for (Direction d : Direction.values()) {
            if (state.hasProperty(FacePaperBlock.FACE.get(d)) && state.getValue(FacePaperBlock.FACE.get(d))) m |= 1 << d.ordinal();
        }
        return m;
    }

    /**
     * C7: finds the rotation / mirror (identity, the three rotations, the two mirrors, then mirror + rotation) that
     * maps {@code savedMask} onto {@code stateMask} and moves the faces (and their floor rotations) the same way.
     * The first match in that order wins: a mirrored horizontal mask is always also some rotated one (D4 orbit), so
     * the mask alone can't tell a mirror from a rotation and rotations, far more common in structures, win. No match
     * (an edited mask) keeps the faces as saved.
     */
    public static FaceContent[] remap(FaceContent[] faces, int savedMask, int stateMask) {
        if (savedMask == stateMask) return faces;
        for (UnaryOperator<Direction> t : transforms()) {
            if (apply(t, savedMask) != stateMask) continue;
            FaceContent[] out = new FaceContent[6];
            for (int i = 0; i < 6; i++) out[i] = new FaceContent();
            for (Direction d : Direction.values()) {
                FaceContent c = faces[d.ordinal()];
                if (c.isEmpty()) continue;
                FaceContent moved = c.copy();
                if (moved.rotation != null) moved.rotation = t.apply(moved.rotation);
                out[t.apply(d).ordinal()] = moved;
            }
            return out;
        }
        return faces;
    }

    private static int apply(UnaryOperator<Direction> t, int mask) {
        int out = 0;
        for (Direction d : Direction.values()) {
            if ((mask & (1 << d.ordinal())) != 0) out |= 1 << t.apply(d).ordinal();
        }
        return out;
    }

    private static List<UnaryOperator<Direction>> transforms() {
        List<UnaryOperator<Direction>> out = new ArrayList<>();
        for (Rotation r : Rotation.values()) out.add(r::rotate);
        for (Mirror m : new Mirror[]{Mirror.LEFT_RIGHT, Mirror.FRONT_BACK}) {
            for (Rotation r : Rotation.values()) out.add(d -> r.rotate(m.mirror(d)));
        }
        return out;
    }

    // ----- sync -----

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
        refreshClient();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) load(tag);
        refreshClient();
    }

    private void refreshClient() {
        if (level == null || !level.isClientSide) return;
        requestModelDataUpdate();
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, 8);
    }

    @Override
    public ModelData getModelData() {
        FaceSnapshot[] snap = new FaceSnapshot[6];
        for (int i = 0; i < 6; i++) snap[i] = faces[i].snapshot();
        return ModelData.builder().with(FaceSnapshot.FACES, snap).build();
    }
}
