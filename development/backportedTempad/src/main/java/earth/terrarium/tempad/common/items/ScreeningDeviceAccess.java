// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/ScreeningDeviceItem.kt (ScreeningDeviceAccess). Translated
// to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.items;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.api.player_access.PlayerAccess;
import earth.terrarium.tempad.api.player_access.PlayerAccessApi;
import earth.terrarium.tempad.common.registries.ModComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ScreeningDeviceAccess implements PlayerAccess {
    public final ItemStack stack;

    public ScreeningDeviceAccess(ItemStack stack) {
        this.stack = stack;
    }

    @Nullable
    public static ScreeningDeviceAccess create(ItemStack stack) {
        return ModComponents.getAccessId(stack) != null ? new ScreeningDeviceAccess(stack) : null;
    }

    @Override
    public boolean canAccess(Level level, GameProfile owner, GameProfile accessor) {
        ResourceLocation accessId = ModComponents.getAccessId(stack);
        PlayerAccess access = accessId == null ? null : PlayerAccessApi.get(accessId);
        // Upstream passes (accessor, owner) to the chosen access, swapped; kept.
        return access != null && access.canAccess(level, accessor, owner);
    }
}
