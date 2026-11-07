// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/CreativeChronometerItem.kt. Translated to Java for Forge
// 1.19.2.
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.api.ActionType;
import earth.terrarium.tempad.api.context.ContextRegistry;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.tva_device.ChrononData;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class CreativeChronometerItem extends Item {
    public CreativeChronometerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(ChrononData.INFINITE);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || entity.tickCount % 10 != 0) return;
        if (!(entity instanceof Player player)) return;
        SyncableContext<?> target = ContextRegistry.locate(player, it -> {
            ChrononHandler chronons = ChrononHandler.of(it);
            return chronons != null && chronons.hasRoom() && it != stack;
        });
        if (target != null) {
            ChrononHandler chronons = ChrononHandler.of(target.getStack());
            if (chronons != null) chronons.insert(Integer.MAX_VALUE, ActionType.Execute);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ChrononHandler to = ChrononHandler.of(context.getLevel().getBlockEntity(context.getClickedPos()));
        if (to == null) return super.useOn(context);
        if (!context.getLevel().isClientSide) {
            ChrononHandler it = ChrononHandler.of(context.getItemInHand());
            if (it != null) ChrononHandler.move(it, to, Integer.MAX_VALUE);
        }
        return InteractionResult.SUCCESS;
    }
}
