package com.SaintAlfonzo.creativeengineering.devtools;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.IdMap;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Debug switch ({@code [debug.block_state_sync]} in creativeengineering-common.toml) for ghost blocks
 * that only show up on a dedicated server.
 *
 * <p>Symptom it chases: the client logs {@code Error executing task on Client} / NullPointerException
 * from {@code ClientboundSectionBlocksUpdatePacket.runUpdates}. The packet carries block-state ids and
 * the client resolves them with {@code Block.BLOCK_STATE_REGISTRY.byId}; an id it does not have comes
 * back null, the NPE aborts the loop, and every later change in that section is lost until a relog
 * resends the chunk. Forge syncs block ids on join but never checks that a block has the same NUMBER
 * of states on both sides, so a block whose properties differ between client and server shifts every
 * id after it.
 *
 * <p>Two halves: the client mixins ({@code BlockUpdateNullStateMixin}, {@code SectionUpdateNullStateMixin})
 * record the raw id of every null lookup while the packet is decoded and report it once the packet is
 * built; {@link #dumpRanges} writes the id range of every block, on the client and on the dedicated
 * server, so {@code tools/diff_block_state_ranges.py} can find the first block where the two disagree
 * and say which block an unresolvable id belongs to.
 *
 * <p>Everything is gated on the config switch; with it off the only cost is one extra null check per
 * decoded state.
 */
@Mod.EventBusSubscriber(modid = "creativeengineering", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BlockStateSyncDebug {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG = "[CE block-state sync] ";

    /** Ids that came back null while the packet on this (network) thread was being decoded. */
    private static final ThreadLocal<IntArrayList> PENDING = ThreadLocal.withInitial(IntArrayList::new);
    private static final Set<Integer> SEEN_IDS = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger BAD_PACKETS = new AtomicInteger();
    private static final AtomicBoolean CLIENT_DUMPED = new AtomicBoolean();

    private BlockStateSyncDebug() {}

    public static boolean enabled() {
        try {
            return CEConfig.DEBUG.stateSyncEnabled.get();
        } catch (Throwable t) {
            return false; // config not loaded yet
        }
    }

    // -------------------------------------------------------------------------
    // Client: called from the packet mixins
    // -------------------------------------------------------------------------

    /** Stand-in for {@code IdMap#byId} inside the packet constructors: same result, remembers null ids. */
    public static Object lookup(IdMap<?> map, int id) {
        Object state = map.byId(id);
        if (state == null && enabled()) PENDING.get().add(id);
        return state;
    }

    public static void finishSection(ClientboundSectionBlocksUpdatePacket packet) {
        IntArrayList ids = PENDING.get();
        if (ids.isEmpty()) return;
        List<String> where = new ArrayList<>();
        int[] entries = {0};
        try {
            packet.runUpdates((pos, state) -> {
                entries[0]++;
                if (state == null) where.add(pos.getX() + "," + pos.getY() + "," + pos.getZ());
            });
        } catch (Throwable t) {
            where.add("(positions unavailable: " + t + ")");
        }
        report("ClientboundSectionBlocksUpdatePacket (" + entries[0] + " entries)", ids, where);
    }

    public static void finishSingle(ClientboundBlockUpdatePacket packet) {
        IntArrayList ids = PENDING.get();
        if (ids.isEmpty()) return;
        var pos = packet.getPos();
        report("ClientboundBlockUpdatePacket", ids, List.of(pos.getX() + "," + pos.getY() + "," + pos.getZ()));
    }

    private static void report(String packet, IntArrayList ids, List<String> where) {
        int stateCount = Block.BLOCK_STATE_REGISTRY.size();
        int packetNo = BAD_PACKETS.incrementAndGet();
        int maxDetail = CEConfig.DEBUG.stateSyncMaxDetailLines.get();
        for (int i = 0; i < ids.size(); i++) {
            int id = ids.getInt(i);
            if (SEEN_IDS.add(id)) {
                LOGGER.warn(TAG + "new unresolvable block-state id {} (client registry holds {} states, so the id is {}) in {} at {}",
                        id, stateCount, id >= stateCount ? "beyond the end by " + (id - stateCount + 1) : "inside the range, an id gap",
                        packet, i < where.size() ? where.get(i) : "?");
            }
        }
        if (packetNo <= maxDetail) {
            LOGGER.warn(TAG + "bad packet #{}: {} nulls={} ids={} positions={} clientStates={}",
                    packetNo, packet, ids.size(), ids, where, stateCount);
        } else if (packetNo % 500 == 0) {
            LOGGER.warn(TAG + "{} bad packets so far, {} distinct unresolvable ids {}", packetNo, SEEN_IDS.size(), SEEN_IDS);
        }
        ids.clear();
        if (CLIENT_DUMPED.compareAndSet(false, true)) dumpRanges("client");
    }

    // -------------------------------------------------------------------------
    // Both sides: id range dump
    // -------------------------------------------------------------------------

    /** Dedicated server: write its half of the comparison at startup. */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (!enabled() || !event.getServer().isDedicatedServer()) return;
        dumpRanges("server");
    }

    /** Client: write at login too, so a session with no bad packet still has a file to compare. */
    @Mod.EventBusSubscriber(modid = "creativeengineering", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ClientLogin {
        private ClientLogin() {}

        @SubscribeEvent
        public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
            if (enabled() && CLIENT_DUMPED.compareAndSet(false, true)) dumpRanges("client");
        }
    }

    /**
     * One line per block, in state-id order: first id, number of states, registry name. Because ids are
     * handed out block by block, two such files disagree exactly where a block's state count differs.
     */
    public static void dumpRanges(String side) {
        // ~760k states: several seconds, so never on the server thread or a network thread
        Thread t = new Thread(() -> writeRanges(side), "ce-block-state-dump");
        t.setDaemon(true);
        t.start();
    }

    private static void writeRanges(String side) {
        Path file = FMLPaths.GAMEDIR.get().resolve("logs").resolve("ce_block_state_ranges_" + side + ".txt");
        try {
            StringBuilder out = new StringBuilder();
            StringBuilder body = new StringBuilder();
            Block current = null;
            int start = 0, count = 0, blocks = 0;
            for (BlockState state : Block.BLOCK_STATE_REGISTRY) {
                Block block = state.getBlock();
                if (block != current) {
                    if (current != null) appendRange(body, start, count, current);
                    current = block;
                    start = Block.BLOCK_STATE_REGISTRY.getId(state);
                    count = 0;
                    blocks++;
                }
                count++;
            }
            if (current != null) appendRange(body, start, count, current);
            out.append("# side=").append(side)
                    .append(" states=").append(Block.BLOCK_STATE_REGISTRY.size())
                    .append(" blocks=").append(blocks).append('\n')
                    .append("# firstId\tstateCount\tblock\n").append(body);
            Files.createDirectories(file.getParent());
            Files.writeString(file, out.toString(), StandardCharsets.UTF_8);
            LOGGER.info(TAG + "wrote {} ({} states in {} blocks)", file, Block.BLOCK_STATE_REGISTRY.size(), blocks);
        } catch (IOException | RuntimeException e) {
            LOGGER.warn(TAG + "could not write " + file, e);
        }
    }

    private static void appendRange(StringBuilder sb, int start, int count, Block block) {
        sb.append(start).append('\t').append(count).append('\t').append(ForgeRegistries.BLOCKS.getKey(block)).append('\n');
    }
}
