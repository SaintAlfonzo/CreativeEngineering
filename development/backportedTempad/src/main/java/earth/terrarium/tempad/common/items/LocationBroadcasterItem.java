// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/LocationBroadcasterItem.kt. Translated to Java for Forge
// 1.19.2. Its player access (DefaultAccess.Public while `enabled`, default true) is attached in forge/TempadCapabilities.
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.common.registries.ModComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LocationBroadcasterItem extends Item {
    public LocationBroadcasterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            ModComponents.setEnabled(stack, !ModComponents.isEnabled(stack));
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
