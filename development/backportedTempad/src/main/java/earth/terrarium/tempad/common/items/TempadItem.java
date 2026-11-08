// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/TempadItem.kt. Translated to Java for Forge 1.19.2.
// Time twister stacking: twister_data (upstream: the twister's DataComponentPatch) holds the twister stack's NBT tag.
// Kept upstream quirk: detaching into an empty slot leaves twister_data on the Tempad (overwritten by the next attach).
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.api.app.AppRegistry;
import earth.terrarium.tempad.api.app.TempadApp;
import earth.terrarium.tempad.api.context.InventoryContext;
import earth.terrarium.tempad.api.macro.TempadMacro;
import earth.terrarium.tempad.api.macro.MacroRegistry;
import earth.terrarium.tempad.common.registries.ModApps;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TempadItem extends ChrononItem {
    public TempadItem(Properties properties) {
        super(properties);
    }

    /** Upstream {@code InteractionHand.getSlot(player)}: the selected hotbar slot, or 40 for the offhand. */
    public static int getSlot(InteractionHand hand, Player player) {
        return hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        InventoryContext ctx = new InventoryContext(player, getSlot(hand, player));
        if (player.isShiftKeyDown()) {
            TempadMacro macro = MacroRegistry.get(ModComponents.getDefaultMacro(stack));
            if (macro != null) macro.run(player, ctx);
        } else {
            TempadApp<?> app = AppRegistry.get(ModComponents.getDefaultApp(stack), ctx, false);
            if (app == null) app = AppRegistry.get(ModApps.TELEPORT, ctx, false);
            if (app != null) app.openMenu((ServerPlayer) player);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action == ClickAction.SECONDARY) {
            if (ModComponents.isTwisterEquipped(stack)) {
                ItemStack other = slot.getItem();
                if (!slot.hasItem()) {
                    ModComponents.setTwisterEquipped(stack, false);
                    ItemStack twister = new ItemStack(ModItems.timeTwister.get());
                    CompoundTag data = ModComponents.getTwisterData(stack);
                    if (data != null) twister.setTag(data.copy());
                    ModComponents.setChrononContent(twister, ModComponents.getChrononContentTimeTwister(stack));
                    ModComponents.setChrononContentTimeTwister(stack, 0);
                    slot.set(twister);
                    return true;
                } else if (other.is(ModItems.tempad.get()) && !ModComponents.isTwisterEquipped(other)) {
                    ModComponents.setTwisterEquipped(stack, false);
                    ModComponents.setTwisterEquipped(other, true);
                    ModComponents.setChrononContentTimeTwister(other, ModComponents.getChrononContentTimeTwister(stack));
                    ModComponents.setTwisterData(other, ModComponents.getTwisterData(stack));
                    ModComponents.setChrononContentTimeTwister(stack, 0);
                    ModComponents.setTwisterData(stack, null);
                    return true;
                }
            } else if (slot.getItem().is(ModItems.timeTwister.get())) {
                ItemStack twister = slot.getItem();
                ModComponents.setTwisterEquipped(stack, true);
                ModComponents.setChrononContentTimeTwister(stack, ModComponents.getChrononContentTimeTwister(stack) + ModComponents.getChrononContent(twister));
                ModComponents.setTwisterData(stack, twister.getTag());
                slot.set(ItemStack.EMPTY);
                return true;
            }
        }
        return super.overrideStackedOnOther(stack, slot, action, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action == ClickAction.SECONDARY && other.is(ModItems.timeTwister.get()) && !ModComponents.isTwisterEquipped(stack)) {
            ModComponents.setTwisterEquipped(stack, true);
            ModComponents.setChrononContentTimeTwister(stack, ModComponents.getChrononContentTimeTwister(stack) + ModComponents.getChrononContent(other));
            ModComponents.setTwisterData(stack, other.getTag());
            access.set(ItemStack.EMPTY);
            return true;
        }
        return super.overrideOtherStackedOnMe(stack, other, slot, action, player, access);
    }
}
