// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/CapacitorItem.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.common.registries.ModTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class CapacitorItem extends ChrononItem {
    public CapacitorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || entity.tickCount % 10 != 0 || cannotDistribute(entity, stack)) return; // 1 mb every 0.5 seconds
        distribute((Player) entity, stack);
    }

    @Override
    public boolean isChargable(ItemStack source, ItemStack target) {
        return super.isChargable(source, target) && !target.is(ModTags.batteries);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action == ClickAction.SECONDARY && slot.hasItem() && ChrononHandler.of(slot.getItem()) != null) {
            ChrononHandler from = ChrononHandler.of(stack);
            ChrononHandler to = ChrononHandler.of(slot.getItem());
            if (from != null && to != null) ChrononHandler.move(from, to, 1000);
            return true;
        }
        return super.overrideStackedOnOther(stack, slot, action, player);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ChrononHandler block = ChrononHandler.of(context.getLevel().getBlockEntity(context.getClickedPos()));
        if (block == null) return super.useOn(context);
        if (!context.getLevel().isClientSide) {
            ChrononHandler it = ChrononHandler.of(context.getItemInHand());
            if (it != null) {
                if (it.getPower() == 0 && block.canExtract()) {
                    ChrononHandler.move(block, it, it.getMaxPower());
                } else if (block.canInsert()) {
                    ChrononHandler.move(it, block, block.getMaxPower());
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
