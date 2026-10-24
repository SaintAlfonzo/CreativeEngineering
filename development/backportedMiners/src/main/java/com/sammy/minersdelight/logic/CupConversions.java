package com.sammy.minersdelight.logic;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sammy.minersdelight.MinersDelightMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Which bowl food the Copper Pot serves as which cup: every {@code data/<namespace>/cup_conversion/*.json}
 * ({@code {"entries": [{"bowl": id, "cup": id}]}}), read on reload. Rows naming an unknown item are skipped with a
 * warning. The server sends the table to clients (recipe book ghost and JEI show the cup).
 */
public final class CupConversions extends SimpleJsonResourceReloadListener {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(MinersDelightMod.path("main"), () -> PROTOCOL,
            PROTOCOL::equals, PROTOCOL::equals);
    private static Map<Item, Item> bowlToCup = Map.of();

    private CupConversions() {
        super(new Gson(), "cup_conversion");
    }

    public static void registerNetwork() {
        CHANNEL.registerMessage(0, Table.class, Table::write, Table::read, Table::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CupConversions());
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        Table table = new Table(bowlToCup);
        if (event.getPlayer() != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(event::getPlayer), table);
        } else {
            CHANNEL.send(PacketDistributor.ALL.noArg(), table);
        }
    }

    /** The cup a bowl food is served in, or null. */
    @Nullable
    public static Item cupFor(Item bowl) {
        return bowlToCup.get(bowl);
    }

    public static Map<Item, Item> table() {
        return bowlToCup;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<Item, Item> table = new HashMap<>();
        files.forEach((file, json) -> {
            for (JsonElement entry : json.getAsJsonObject().getAsJsonArray("entries")) {
                String bowlId = entry.getAsJsonObject().get("bowl").getAsString();
                String cupId = entry.getAsJsonObject().get("cup").getAsString();
                Item bowl = item(bowlId);
                Item cup = item(cupId);
                if (bowl == null || cup == null) {
                    MinersDelightMod.LOGGER.warn("Cup conversion {}: skipped {} -> {} (unknown item)", file, bowlId, cupId);
                } else {
                    table.put(bowl, cup);
                }
            }
        });
        bowlToCup = Map.copyOf(table);
        MinersDelightMod.LOGGER.info("Cup conversions: {} bowl foods", bowlToCup.size());
    }

    @Nullable
    private static Item item(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        Item item = location == null ? null : ForgeRegistries.ITEMS.getValue(location);
        return item == null || item == Items.AIR ? null : item;
    }

    public record Table(Map<Item, Item> rows) {
        void write(FriendlyByteBuf buf) {
            buf.writeMap(rows, (b, item) -> b.writeId(net.minecraft.core.Registry.ITEM, item), (b, item) -> b.writeId(net.minecraft.core.Registry.ITEM, item));
        }

        static Table read(FriendlyByteBuf buf) {
            return new Table(buf.readMap(b -> b.readById(net.minecraft.core.Registry.ITEM), b -> b.readById(net.minecraft.core.Registry.ITEM)));
        }

        void handle(Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> bowlToCup = Map.copyOf(rows));
            context.get().setPacketHandled(true);
        }
    }
}
