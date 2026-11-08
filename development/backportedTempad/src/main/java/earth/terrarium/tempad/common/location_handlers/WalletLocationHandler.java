// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/location_handlers/WalletLocationHandler.kt. Translated to Java
// for Forge 1.19.2. Location ids are the upstream fixed UUIDs, one per wallet slot.
package earth.terrarium.tempad.common.location_handlers;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.context.ContextRegistry;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.locations.LocationGetter;
import earth.terrarium.tempad.api.locations.LocationHandler;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WalletLocationHandler implements LocationHandler {
    public static final ResourceLocation ID = Tempad.id("wallet");

    private static final UUID[] IDS = {
            UUID.fromString("d15655c9-782d-4a21-9f7e-024522cc5c52"),
            UUID.fromString("fefeb329-cd83-492b-a279-96305f85e07d"),
            UUID.fromString("b8de7ee0-b175-4785-8cef-02f107fac7b8"),
            UUID.fromString("f92bbcbe-5da5-462e-bdd5-d2cdb73f2f91"),
            UUID.fromString("d611b84e-a2ed-46b4-9b43-f61a7121b506"),
            UUID.fromString("407ed827-e56c-453e-8f55-7e5c7b49b9bb"),
            UUID.fromString("e8516d35-9943-41c5-bd9c-23678aa27e1e"),
            UUID.fromString("fd9cb781-9cd4-4975-bfc1-5c388d442c26"),
            UUID.fromString("af1796b9-ab87-4799-9e62-c804f9898cf8"),
            UUID.fromString("dec5a3b2-bd0e-4fe8-9222-01a486b329ad"),
            UUID.fromString("fa5c3767-3624-4ab2-9530-360adc73949f"),
            UUID.fromString("ff0cd8d6-b1f0-4a26-8900-ed70dbd97dc9"),
            UUID.fromString("381dd934-33e9-4b45-968b-39329bc2358c"),
            UUID.fromString("7ad6a4f9-b9f5-4e0d-a6dd-78e8a7bd67e7"),
            UUID.fromString("b37db112-ac1b-4f73-b2a2-99b47769467d"),
            UUID.fromString("36352a51-eb4e-4639-b7aa-8848a84a516f"),
            UUID.fromString("874dc1fb-b4f4-4828-a2a0-fc2f2cbabedf"),
            UUID.fromString("d9885d96-0aed-47ad-9ec0-9483b0d25a90"),
    };

    public final GameProfile playerProfile;
    @Nullable
    public final UpgradeHandler upgrades;
    @Nullable
    public final ChrononHandler chronons;

    public WalletLocationHandler(GameProfile playerProfile, @Nullable UpgradeHandler upgrades, @Nullable ChrononHandler chronons) {
        this.playerProfile = playerProfile;
        this.upgrades = upgrades;
        this.chronons = chronons;
    }

    @Nullable
    public ServerPlayer getPlayer() {
        MinecraftServer server = Tempad.server();
        return server == null ? null : server.getPlayerList().getPlayer(playerProfile.getId());
    }

    @Override
    public Map<UUID, NamedGlobalVec3> getLocations() {
        ServerPlayer player = getPlayer();
        if (player == null) return Map.of();
        SyncableContext<?> wallet = ContextRegistry.locate(player, it -> it.getItem() == ModItems.cardWallet.get()
                && ModComponents.getWalletContents(it).stream().anyMatch(card -> !card.isEmpty()));
        if (wallet == null) return Map.of();

        Map<UUID, NamedGlobalVec3> values = new LinkedHashMap<>();
        List<ItemStack> walletContents = ModComponents.getWalletContents(wallet.getStack());
        for (int i = 0; i < walletContents.size() && i < IDS.length; i++) {
            LocationGetter target = ModComponents.getPortalTarget(walletContents.get(i));
            NamedGlobalVec3 pos = target == null ? null : target.get(upgrades, chronons);
            if (pos != null) values.put(IDS[i], pos);
        }
        return values;
    }

    @Override
    public void remove(UUID locationId) {
    }

    @Nullable
    @Override
    public LocationGetter getSerializable(UUID locationId) {
        return null;
    }
}
