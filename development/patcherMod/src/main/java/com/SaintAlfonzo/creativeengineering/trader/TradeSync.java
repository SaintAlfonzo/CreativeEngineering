package com.SaintAlfonzo.creativeengineering.trader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.common.VillagerTradingManager;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

import org.slf4j.Logger;

/**
 * Villager and wandering trader trades for clients on a dedicated server (feature_writeup/trade_sync.md).
 * <p>
 * Forge builds the modded trade lists ({@code VillagerTradesEvent}, {@code WandererTradesEvent}, so every KubeJS /
 * MoreJS trade) only on {@code ServerAboutToStartEvent}. A client connected to a dedicated server keeps the vanilla
 * {@link VillagerTrades#TRADES}, which is what Just Enough Resources reads for its villager pages. This sends the
 * server's lists to each client in {@link OnDatapackSyncEvent} (Forge posts it before the recipe and tag packets, so
 * the lists are in place before JEI loads its plugins) and puts them into the client's tables:
 * <ul>
 *   <li>a listing that is still the vanilla one is sent as its index in Forge's vanilla snapshot; the client takes its
 *       own copy, so JER samples it with its villager as before;</li>
 *   <li>any other listing is sampled on the server ({@link #SAMPLES} tries, null entity, every Throwable caught) and
 *       sent as up to {@link #KEEP} distinct offers; the client listing hands them out in turn. Map and treasure
 *       listings are skipped (a server-side locate can hang, memory lc-map-trade-locate-hang).</li>
 * </ul>
 * Singleplayer is skipped (the integrated server already filled the shared tables). The client restores its own
 * tables on logout ({@code trader.client.TradeSyncClient}). Nothing here changes what a villager offers in game.
 */
public final class TradeSync {

    private static final Logger LOG = LogUtils.getLogger();
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("creativeengineering", "trade_sync"),
            () -> PROTOCOL, NetworkRegistry.acceptMissingOr(PROTOCOL), NetworkRegistry.acceptMissingOr(PROTOCOL));

    /** getOffer calls per non-vanilla listing on the server, and the most distinct offers kept. */
    static final int SAMPLES = 8;
    static final int KEEP = 4;
    /** Profession id used for the wandering trader's lists (level 1 common, level 2 rare). */
    private static final ResourceLocation WANDERER = new ResourceLocation("creativeengineering", "wandering_trader");

    /** Server: the packets built once per server run (the lists don't change after server start). */
    private static List<Lists> built;

    /** Client: the tables before the first sync of this connection, restored on logout; null when nothing applied. */
    private static Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> clientOriginal;
    private static Int2ObjectMap<VillagerTrades.ItemListing[]> clientOriginalWanderer;

    private TradeSync() {
    }

    public static void register() {
        CHANNEL.messageBuilder(Lists.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Lists::encode).decoder(Lists::decode).consumerMainThread(Lists::handle).add();
    }

    // ---------------------------------------------------------------- server

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        List<ServerPlayer> players = event.getPlayer() != null ? List.of(event.getPlayer())
                : event.getPlayerList().getPlayers();
        List<Lists> packets = null;
        int sent = 0;
        for (ServerPlayer player : players) {
            if (player.connection == null || player.connection.connection.isMemoryConnection()
                    || !CHANNEL.isRemotePresent(player.connection.connection)) {
                continue;
            }
            if (packets == null) {
                packets = packets();
            }
            for (Lists packet : packets) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
            sent++;
        }
        if (sent > 0) {
            LOG.info("[trade sync] sent {} trade list(s) to {} player(s)", packets.size(), sent);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        built = null;
    }

    private static synchronized List<Lists> packets() {
        if (built != null) {
            return built;
        }
        Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> vanilla = vanillaTrades();
        Int2ObjectMap<VillagerTrades.ItemListing[]> vanillaWanderer = vanillaWanderer();
        RandomSource random = RandomSource.create(0x7AD35L);
        List<Lists> out = new ArrayList<>();
        int sampled = 0;
        for (Map.Entry<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> e : VillagerTrades.TRADES.entrySet()) {
            ResourceLocation id = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(e.getKey());
            if (id == null) {
                continue;
            }
            Lists lists = build(id, e.getValue(), vanilla.get(e.getKey()), random);
            sampled += lists.sampled();
            out.add(lists);
        }
        Lists wanderer = build(WANDERER, VillagerTrades.WANDERING_TRADER_TRADES, vanillaWanderer, random);
        sampled += wanderer.sampled();
        out.add(wanderer);
        LOG.info("[trade sync] built {} trade list(s), {} listing(s) sampled", out.size(), sampled);
        built = out;
        return out;
    }

    private static Lists build(ResourceLocation id, Int2ObjectMap<VillagerTrades.ItemListing[]> levels,
                               Int2ObjectMap<VillagerTrades.ItemListing[]> vanilla, RandomSource random) {
        Int2ObjectMap<List<Entry>> out = new Int2ObjectOpenHashMap<>();
        for (Int2ObjectMap.Entry<VillagerTrades.ItemListing[]> level : levels.int2ObjectEntrySet()) {
            VillagerTrades.ItemListing[] original = vanilla == null ? null : vanilla.get(level.getIntKey());
            List<Entry> entries = new ArrayList<>();
            for (VillagerTrades.ItemListing listing : level.getValue()) {
                int index = indexOf(original, listing);
                if (index >= 0) {
                    entries.add(new Entry(index, List.of()));
                    continue;
                }
                List<MerchantOffer> samples = sample(listing, random);
                if (!samples.isEmpty()) {
                    entries.add(new Entry(-1, samples));
                }
            }
            out.put(level.getIntKey(), entries);
        }
        return new Lists(id, out);
    }

    private static int indexOf(VillagerTrades.ItemListing[] listings, VillagerTrades.ItemListing listing) {
        if (listings != null) {
            for (int i = 0; i < listings.length; i++) {
                if (listings[i] == listing) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static List<MerchantOffer> sample(VillagerTrades.ItemListing listing, RandomSource random) {
        String name = listing.getClass().getName().toLowerCase(Locale.ROOT);
        if (name.contains("map") || name.contains("treasure")) {
            return List.of();
        }
        List<MerchantOffer> kept = new ArrayList<>();
        for (int i = 0; i < SAMPLES && kept.size() < KEEP; i++) {
            MerchantOffer offer;
            try {
                offer = listing.getOffer(null, random);
            } catch (Throwable t) {
                break;
            }
            if (offer == null || offer.getResult().isEmpty()) {
                continue;
            }
            if (kept.stream().noneMatch(k -> same(k, offer))) {
                kept.add(offer);
            }
        }
        return kept;
    }

    private static boolean same(MerchantOffer a, MerchantOffer b) {
        return ItemStack.matches(a.getBaseCostA(), b.getBaseCostA()) && ItemStack.matches(a.getCostB(), b.getCostB())
                && ItemStack.matches(a.getResult(), b.getResult());
    }

    @SuppressWarnings("unchecked")
    private static Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> vanillaTrades() {
        Object value = forgeField("VANILLA_TRADES");
        return value instanceof Map<?, ?> map ? (Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>>) map : Map.of();
    }

    @SuppressWarnings("unchecked")
    private static Int2ObjectMap<VillagerTrades.ItemListing[]> vanillaWanderer() {
        Object value = forgeField("WANDERER_TRADES");
        return value instanceof Int2ObjectMap<?> map ? (Int2ObjectMap<VillagerTrades.ItemListing[]>) map : null;
    }

    private static Object forgeField(String name) {
        try {
            Field field = VillagerTradingManager.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOG.warn("[trade sync] can't read Forge's {}; every listing is sampled", name, e);
            return null;
        }
    }

    // ---------------------------------------------------------------- client

    /** Puts one synced list into the client's tables (main thread). */
    static void apply(Lists lists) {
        if (clientOriginal == null) {
            clientOriginal = new HashMap<>(VillagerTrades.TRADES);
            clientOriginalWanderer = new Int2ObjectOpenHashMap<>(VillagerTrades.WANDERING_TRADER_TRADES);
        }
        boolean wanderer = WANDERER.equals(lists.profession());
        VillagerProfession profession = null;
        Int2ObjectMap<VillagerTrades.ItemListing[]> own;
        if (wanderer) {
            own = clientOriginalWanderer;
        } else {
            profession = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(lists.profession());
            if (profession == null || !lists.profession().equals(ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession))) {
                return;
            }
            own = clientOriginal.get(profession);
        }
        Int2ObjectMap<VillagerTrades.ItemListing[]> levels = new Int2ObjectOpenHashMap<>();
        for (Int2ObjectMap.Entry<List<Entry>> level : lists.levels().int2ObjectEntrySet()) {
            VillagerTrades.ItemListing[] original = own == null ? null : own.get(level.getIntKey());
            List<VillagerTrades.ItemListing> listings = new ArrayList<>();
            for (Entry entry : level.getValue()) {
                if (entry.vanillaIndex() >= 0) {
                    if (original != null && entry.vanillaIndex() < original.length) {
                        listings.add(original[entry.vanillaIndex()]);
                    }
                } else {
                    listings.add(new SampledListing(entry.samples()));
                }
            }
            levels.put(level.getIntKey(), listings.toArray(new VillagerTrades.ItemListing[0]));
        }
        if (wanderer) {
            // the server sends the wandering trader last
            VillagerTrades.WANDERING_TRADER_TRADES.clear();
            VillagerTrades.WANDERING_TRADER_TRADES.putAll(levels);
            LOG.info("[trade sync] applied the server's villager and wandering trader trades");
        } else {
            VillagerTrades.TRADES.put(profession, levels);
        }
    }

    /** Puts the client's own tables back (logout); no-op when nothing was synced. */
    public static void restoreClient() {
        if (clientOriginal == null) {
            return;
        }
        VillagerTrades.TRADES.clear();
        VillagerTrades.TRADES.putAll(clientOriginal);
        VillagerTrades.WANDERING_TRADER_TRADES.clear();
        VillagerTrades.WANDERING_TRADER_TRADES.putAll(clientOriginalWanderer);
        clientOriginal = null;
        clientOriginalWanderer = null;
    }

    // ---------------------------------------------------------------- data

    /** One listing: a vanilla index ({@code >= 0}) or sampled offers. */
    record Entry(int vanillaIndex, List<MerchantOffer> samples) {
    }

    /** One profession's (or the wandering trader's) lists, per level. */
    public record Lists(ResourceLocation profession, Int2ObjectMap<List<Entry>> levels) {

        int sampled() {
            int n = 0;
            for (List<Entry> entries : levels.values()) {
                for (Entry entry : entries) {
                    if (entry.vanillaIndex() < 0) {
                        n++;
                    }
                }
            }
            return n;
        }

        void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(profession);
            buf.writeVarInt(levels.size());
            for (Int2ObjectMap.Entry<List<Entry>> level : levels.int2ObjectEntrySet()) {
                buf.writeVarInt(level.getIntKey());
                buf.writeVarInt(level.getValue().size());
                for (Entry entry : level.getValue()) {
                    buf.writeVarInt(entry.vanillaIndex() + 1);
                    if (entry.vanillaIndex() < 0) {
                        buf.writeVarInt(entry.samples().size());
                        for (MerchantOffer offer : entry.samples()) {
                            buf.writeNbt(offer.createTag());
                        }
                    }
                }
            }
        }

        static Lists decode(FriendlyByteBuf buf) {
            ResourceLocation profession = buf.readResourceLocation();
            int levelCount = buf.readVarInt();
            Int2ObjectMap<List<Entry>> levels = new Int2ObjectOpenHashMap<>(levelCount);
            for (int l = 0; l < levelCount; l++) {
                int level = buf.readVarInt();
                int count = buf.readVarInt();
                List<Entry> entries = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    int index = buf.readVarInt() - 1;
                    List<MerchantOffer> samples = new ArrayList<>();
                    if (index < 0) {
                        int n = buf.readVarInt();
                        for (int s = 0; s < n; s++) {
                            CompoundTag tag = buf.readNbt();
                            if (tag != null) {
                                samples.add(new MerchantOffer(tag));
                            }
                        }
                    }
                    entries.add(new Entry(index, samples));
                }
                levels.put(level, entries);
            }
            return new Lists(profession, levels);
        }

        void handle(Supplier<NetworkEvent.Context> context) {
            if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                apply(this);
            }
        }
    }

    /** A client listing from the server's samples: hands them out in turn (JER calls getOffer many times). */
    static final class SampledListing implements VillagerTrades.ItemListing {
        private final List<MerchantOffer> samples;
        private final AtomicInteger next = new AtomicInteger();

        SampledListing(List<MerchantOffer> samples) {
            this.samples = samples;
        }

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            if (samples.isEmpty()) {
                return null;
            }
            return new MerchantOffer(samples.get(Math.floorMod(next.getAndIncrement(), samples.size())).createTag());
        }
    }
}
