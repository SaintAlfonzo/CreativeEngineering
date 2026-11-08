// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/WalletItem.kt. Translated to Java for Forge 1.19.2.
// The `ItemStack.items` extension is WalletInventory.of(stack). The wallet's IItemHandler capability (upstream
// `itemItems[cardWallet]`) is attached in forge/TempadCapabilities.
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.api.locations.DirectLocation;
import earth.terrarium.tempad.api.locations.IndirectLocation;
import earth.terrarium.tempad.api.locations.LocationGetter;
import earth.terrarium.tempad.common.menu.WalletMenu;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WalletItem extends Item {
    public WalletItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        player.openMenu(WalletInventory.of(stack));
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;
        WalletInventory items = WalletInventory.of(stack);
        ItemStack contents = slot.getItem();
        if (!items.isFull() && contents.getItem() == ModItems.locationCard.get() && ModComponents.getPortalTarget(contents) != null) {
            slot.set(items.insertItem(contents, false));
            return true;
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        WalletInventory items = WalletInventory.of(stack);
        if (!items.isFull() && other.getItem() == ModItems.locationCard.get() && ModComponents.getPortalTarget(other) != null
                && !(player.containerMenu instanceof WalletMenu)) {
            access.set(items.insertItem(other, false));
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        boolean empty = true;
        tooltip.add(Component.translatable("item.tempad.card_wallet.prefix").withStyle(ChatFormatting.GRAY));
        for (ItemStack card : ModComponents.getWalletContents(stack)) {
            // Upstream streams ItemContainerContents, which holds no empty stacks.
            if (card.isEmpty()) continue;
            LocationGetter portalTarget = ModComponents.getPortalTarget(card);
            if (portalTarget instanceof IndirectLocation indirect) {
                tooltip.add(indirect.info());
            } else if (portalTarget instanceof DirectLocation direct) {
                tooltip.add(MutableComponent.create(direct.location().name().getContents())
                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(direct.location().color().getValue() & 0xFFFFFF))));
            }
            empty = false;
        }
        if (empty) {
            tooltip.add(Component.translatable("item.tempad.card_wallet.empty").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        }
    }
}
