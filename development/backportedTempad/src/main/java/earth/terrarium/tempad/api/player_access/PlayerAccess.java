// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/player_access/PlayerAccess.kt. Translated to Java for Forge 1.19.2:
// ItemCapability -> Forge Capability. (Phase 1 attaches it to no item: the broadcaster and screening device come in
// phase 2, so the player handler lists nobody yet, as upstream without those items.)
package earth.terrarium.tempad.api.player_access;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.jetbrains.annotations.Nullable;

public interface PlayerAccess {
    /** Upstream {@code tempad:player_access}. */
    Capability<PlayerAccess> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() { });

    boolean canAccess(Level level, GameProfile owner, GameProfile accessor);

    /** Upstream {@code ItemStack.playerAccess}. */
    @Nullable
    static PlayerAccess of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return stack.getCapability(CAPABILITY).resolve().orElse(null);
    }
}
