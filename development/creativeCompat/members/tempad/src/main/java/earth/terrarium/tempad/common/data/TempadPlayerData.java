// Port glue (ours, GPLv3): the player attachments of Tempad 3.0.4 (MIT, Terrarium; common/registries/ModAttachments.kt
// pinned_location and travel_history, both copyOnDeath) as one Forge capability, TP-D5. Attached and cloned in
// forge/TempadCapabilities. NBT keys are the upstream attachment names. Not synced to the client: upstream sends the
// pinned location inside the Teleport app's menu data and the history inside the Time Twister packet.
package earth.terrarium.tempad.common.data;

import earth.terrarium.tempad.Tempad;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.jetbrains.annotations.Nullable;

public class TempadPlayerData {
    public static final ResourceLocation ID = Tempad.id("player_data");
    public static final Capability<TempadPlayerData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() { });

    /** Returned when the capability is missing (a fake player built before attach); writes to it are discarded. */
    private static final TempadPlayerData DETACHED = new TempadPlayerData();

    @Nullable
    private FavoriteLocationAttachment pinnedPosition;
    private final TravelHistoryAttachment travelHistory = new TravelHistoryAttachment();

    public static TempadPlayerData get(Player player) {
        return player.getCapability(CAPABILITY).resolve().orElse(DETACHED);
    }

    @Nullable
    public FavoriteLocationAttachment getPinnedPosition() {
        return pinnedPosition;
    }

    public void setPinnedPosition(@Nullable FavoriteLocationAttachment pinnedPosition) {
        this.pinnedPosition = pinnedPosition;
    }

    public TravelHistoryAttachment getTravelHistory() {
        return travelHistory;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (pinnedPosition != null) tag.put("pinned_location", pinnedPosition.save());
        tag.put("travel_history", travelHistory.save());
        return tag;
    }

    public void load(CompoundTag tag) {
        pinnedPosition = tag.contains("pinned_location") ? FavoriteLocationAttachment.load(tag.getCompound("pinned_location")) : null;
        travelHistory.load(tag.getCompound("travel_history"));
    }

    public void copyFrom(TempadPlayerData other) {
        pinnedPosition = other.pinnedPosition;
        travelHistory.copyFrom(other.travelHistory);
    }
}
