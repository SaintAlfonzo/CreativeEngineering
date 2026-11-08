// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/ScreeningDeviceItem.kt (ScreeningDeviceItem). Translated
// to Java for Forge 1.19.2. ScreeningDeviceAccess (same Kotlin file) is its own class; it is attached as the player
// access capability in forge/TempadCapabilities.
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.api.player_access.PlayerAccessApi;
import earth.terrarium.tempad.common.registries.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScreeningDeviceItem extends Item {
    public ScreeningDeviceItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            List<ResourceLocation> ids = PlayerAccessApi.ids();
            int currentIndex = ids.indexOf(ModComponents.getAccessId(stack));
            if (currentIndex == -1 || currentIndex != ids.size() - 1) {
                // Upstream precedence kept: `currentIndex + 1 % ids.size` is currentIndex + (1 % size).
                ModComponents.setAccessId(stack, currentIndex == -1 ? ids.get(0) : ids.get(currentIndex + 1 % ids.size()));
            } else {
                ModComponents.setAccessId(stack, null);
            }

            player.displayClientMessage(Component.translatable("item.tempad.screening_device.screening", display(stack)), true);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.tempad.screening_device.screening", display(stack)).withStyle(ChatFormatting.GRAY));
    }

    private static Component display(ItemStack stack) {
        ResourceLocation accessId = ModComponents.getAccessId(stack);
        if (accessId == null) return Component.translatable("item.tempad.screening_device.off").withStyle(ChatFormatting.RED);
        return Component.translatable(accessLanguageKey(accessId)).withStyle(ChatFormatting.GREEN);
    }

    /** 1.21 {@code ResourceLocation.toLanguageKey("access")}: {@code access.<namespace>.<path>}. */
    public static String accessLanguageKey(ResourceLocation id) {
        return "access." + id.getNamespace() + "." + id.getPath();
    }
}
