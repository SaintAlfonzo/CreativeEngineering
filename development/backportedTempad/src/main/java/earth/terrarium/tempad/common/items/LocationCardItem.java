// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/LocationCardItem.kt. Translated to Java for Forge 1.19.2.
// The card's portal target lines come from the phase-1 ItemTooltipEvent hook (client/TempadClient), as upstream's
// TooltipProvider hook.
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.context.ContextRegistry;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class LocationCardItem extends Item {
    public LocationCardItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack cardStack = player.getItemInHand(usedHand);
        if (!level.isClientSide && ModComponents.getPortalTarget(cardStack) != null) {
            SyncableContext<?> ctx = ContextRegistry.locate(player, it -> it.getItem() == ModItems.cardWallet.get()
                    && WalletInventory.of(it).insertItem(cardStack, true).getCount() < cardStack.getCount());
            if (ctx != null) {
                ctx.modify(it -> {
                    ItemStack leftOver = WalletInventory.of(it).insertItem(cardStack, false);
                    player.setItemInHand(usedHand, leftOver);
                    player.displayClientMessage(Component.translatable("item.tempad.card_wallet.inserted").withStyle(color(Tempad.ORANGE.getValue())), true);
                });
            } else {
                player.displayClientMessage(Component.translatable("item.tempad.card_wallet.insert_fail")
                        .withStyle(color(Tempad.HIGHLIGHTED_ORANGE.getValue())), true);
            }
        }
        return InteractionResultHolder.success(cardStack);
    }

    /**
     * Upstream returns {@code portalTarget as? TooltipComponent}; neither DirectLocation nor IndirectLocation is a
     * TooltipComponent, so it is always empty. Kept as empty.
     */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.empty();
    }

    private static Style color(int argb) {
        return Style.EMPTY.withColor(TextColor.fromRgb(argb & 0xFFFFFF));
    }
}
