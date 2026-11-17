package com.SaintAlfonzo.creativecompat.surface;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecompat.CreativeCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.level.ChunkEvent;

/**
 * Neighbour counts baked into the chunk (plan_post_scratch_1115b.md C2). A counted block's count is computed once on
 * first use ({@link #count}) and kept in its chunk's {@link PassCountData}; it is not recounted per tick.
 *
 * <ul>
 *   <li><b>Invalidation</b>: {@code mixin/LevelChunkPassMixin} at RETURN of {@code LevelChunk#setBlockState} calls
 *       {@link #blockChanged} with the old and new state; when their tiers differ (a block broken or planted / placed,
 *       an explosion, a piston, a fluid replacing a crop) the entries of that block and its 6 face neighbours are
 *       dropped (6 faces cover the 4 sides too). A state change of the same tier (harvest to age 0, replant, growth)
 *       keeps them: tier membership, not growth, is what counts. Both sides: the client's block update packets set
 *       blocks through the same method.</li>
 *   <li><b>Storage</b>: a chunk capability ({@code creativecompat:pass_counts}, attached with
 *       {@code AttachCapabilitiesEvent<LevelChunk>}, never a mixin on {@code CapabilityProvider}), saved in the chunk
 *       NBT on the server (entries for blocks that are gone left out), not saved on the client. The data also hangs
 *       on the chunk itself ({@link PassChunkHolder}) so a lookup is a field read.</li>
 *   <li><b>Chunk borders</b>: a chunk that loads drops its own edge entries and the facing edge entries of its loaded
 *       neighbours (a neighbour may have changed while it was away; the client fills chunks from packets without
 *       {@code setBlockState}); the client also drops the loaded chunk's whole set.</li>
 * </ul>
 */
public final class PassCounts {

    private PassCounts() {}

    /** The capability (its own holder: the token needs Forge's transformer, which the headless harness lacks). */
    public static final class Cap {
        private Cap() {}

        public static final Capability<PassCountData> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    }

    private static final ResourceLocation ID = new ResourceLocation(CreativeCompat.NS, "pass_counts");

    /** Mod bus. */
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(PassCountData.class);
    }

    /** Forge bus, generic on LevelChunk. */
    public static void attach(AttachCapabilitiesEvent<LevelChunk> event) {
        LevelChunk chunk = event.getObject();
        PassCountData data = new PassCountData();
        ((PassChunkHolder) chunk).creativecompat$setPassCounts(data);
        boolean client = chunk.getLevel() != null && chunk.getLevel().isClientSide;
        event.addCapability(ID, client ? new Provider(data) : new Saved(chunk, data));
    }

    /** The client copy: not saved. */
    private static class Provider implements ICapabilityProvider {
        final PassCountData data;
        final LazyOptional<PassCountData> lazy;

        Provider(PassCountData data) {
            this.data = data;
            this.lazy = LazyOptional.of(() -> data);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            return cap == Cap.CAP ? lazy.cast() : LazyOptional.empty();
        }
    }

    /** The server copy, saved in the chunk ({@code v}: tag version, {@code e}: entries). */
    private static final class Saved extends Provider implements ICapabilitySerializable<CompoundTag> {
        private final LevelChunk chunk;

        Saved(LevelChunk chunk, PassCountData data) {
            super(data);
            this.chunk = chunk;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            int minY = chunk.getMinBuildHeight();
            int bx = chunk.getPos().getMinBlockX(), bz = chunk.getPos().getMinBlockZ();
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            int[] entries = data.save(k -> {
                p.set(bx + PassCountData.keyX(k), PassCountData.keyY(k, minY), bz + PassCountData.keyZ(k));
                return counted(chunk.getBlockState(p));
            });
            if (entries.length == 0) return tag;
            tag.putInt("v", PassCountData.tagVersion());
            tag.putIntArray("e", entries);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("e")) data.load(tag.getInt("v"), tag.getIntArray("e"));
        }
    }

    /** The block is in a tier that counts neighbours (with this side's values). */
    private static boolean counted(BlockState state) {
        PassTier t = PassThrough.tierOf(state);
        return t != null && PassRule.needsCount(PassValues.local().tier(t));
    }

    /** The chunk's data, made on demand for a chunk the attach event missed (then never saved). */
    @Nullable
    private static PassCountData data(LevelChunk chunk) {
        if (chunk instanceof EmptyLevelChunk) return null;
        PassChunkHolder h = (PassChunkHolder) chunk;
        PassCountData d = h.creativecompat$passCounts();
        if (d == null) {
            d = new PassCountData();
            h.creativecompat$setPassCounts(d);
        }
        return d;
    }

    /**
     * The same-tier neighbour count of the block at {@code pos} with shape {@code mode}: the baked one, else counted
     * now ({@link PassThrough#neighbours}) and baked. A plain {@link BlockGetter} (the harness, a render region) is
     * always counted.
     */
    public static int count(BlockGetter level, BlockPos pos, PassTier tier, PassTier.Neighbours mode) {
        if (mode.canonical() == PassTier.Neighbours.NONE) return 0;
        if (!(level instanceof Level lv)) return PassThrough.neighbours(level, pos, tier, mode);
        PassCountData d = chunkData(lv, pos.getX(), pos.getZ());
        if (d == null) return PassThrough.neighbours(level, pos, tier, mode);
        int key = PassCountData.key(pos.getX(), pos.getY(), pos.getZ(), lv.getMinBuildHeight());
        boolean faces = mode.vertical();
        int k = d.get(key, faces);
        if (k >= 0) return k;
        k = PassThrough.neighbours(level, pos, tier, mode);
        d.put(key, faces, k);
        return k;
    }

    /** The baked count, or -1 (not baked, or no chunk data). */
    static int baked(Level level, long pos, PassTier.Neighbours mode) {
        int x = BlockPos.getX(pos), z = BlockPos.getZ(pos);
        PassCountData d = chunkData(level, x, z);
        if (d == null) return -1;
        return d.get(PassCountData.key(x, BlockPos.getY(pos), z, level.getMinBuildHeight()), mode.vertical());
    }

    /** Bakes a count found another way ({@link PassThrough#countRegion}). */
    static void bake(Level level, long pos, PassTier.Neighbours mode, int count) {
        int x = BlockPos.getX(pos), z = BlockPos.getZ(pos);
        PassCountData d = chunkData(level, x, z);
        if (d != null) d.put(PassCountData.key(x, BlockPos.getY(pos), z, level.getMinBuildHeight()), mode.vertical(), count);
    }

    /** The loaded chunk's data at block x / z, or null (not loaded). Never loads a chunk. */
    @Nullable
    private static PassCountData chunkData(Level level, int x, int z) {
        LevelChunk c = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
        return c == null ? null : data(c);
    }

    /**
     * RETURN of {@code LevelChunk#setBlockState} ({@code old} null: nothing changed). Drops the counts of the block
     * and its 6 face neighbours when the tier at {@code pos} changed.
     */
    public static void blockChanged(LevelChunk chunk, BlockPos pos, @Nullable BlockState old, BlockState now) {
        if (!changesCounts(old, now)) return;
        Level level = chunk.getLevel();
        int minY = level.getMinBuildHeight();
        PassCountData own = data(chunk);
        if (own != null) own.remove(PassCountData.key(pos.getX(), pos.getY(), pos.getZ(), minY));
        for (Direction d : Direction.values()) {
            int x = pos.getX() + d.getStepX(), y = pos.getY() + d.getStepY(), z = pos.getZ() + d.getStepZ();
            PassCountData nd = (x >> 4) == chunk.getPos().x && (z >> 4) == chunk.getPos().z ? own : chunkData(level, x, z);
            if (nd != null) nd.remove(PassCountData.key(x, y, z, minY));
        }
    }

    /**
     * The change from {@code old} to {@code now} at one position can change neighbour counts: the tier there changed
     * (broken, planted, placed, replaced). A state change of the same block (harvest to age 0, replant, growth) or a
     * change between blocks of the same tier does not.
     */
    static boolean changesCounts(@Nullable BlockState old, BlockState now) {
        if (old == null || old == now) return false;
        if (old.getBlock() == now.getBlock()) return false;
        return PassThrough.tierOf(old) != PassThrough.tierOf(now);
    }

    /** Forge bus: a chunk loaded (both sides). See the class comment, "Chunk borders". */
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getChunk() instanceof LevelChunk chunk) || chunk instanceof EmptyLevelChunk) return;
        Level level = chunk.getLevel();
        if (level == null) return;
        PassCountData own = data(chunk);
        if (own != null) {
            if (level.isClientSide) own.clear();
            else own.dropEdges();
        }
        int cx = chunk.getPos().x, cz = chunk.getPos().z;
        dropFacing(level, cx - 1, cz, 1);
        dropFacing(level, cx + 1, cz, 0);
        dropFacing(level, cx, cz - 1, 3);
        dropFacing(level, cx, cz + 1, 2);
    }

    private static void dropFacing(Level level, int cx, int cz, int side) {
        LevelChunk c = level.getChunkSource().getChunkNow(cx, cz);
        if (c == null) return;
        PassCountData d = data(c);
        if (d != null) d.dropEdge(side);
    }

    /**
     * Tag reload: hash of every block's tier, the data version saved with the counts. Changed tier tags drop the
     * counts saved under the old ones.
     */
    public static void onTagsUpdated() {
        int h = 1;
        for (Block b : Registry.BLOCK) {
            PassTier t = PassThrough.tierOf(b);
            if (t == null) continue;
            ResourceLocation id = Registry.BLOCK.getKey(b);
            h = h * 31 + (id == null ? 0 : id.hashCode()) * (t.ordinal() + 1);
        }
        PassCountData.setTagVersion(h == 0 ? 1 : h);
    }
}
