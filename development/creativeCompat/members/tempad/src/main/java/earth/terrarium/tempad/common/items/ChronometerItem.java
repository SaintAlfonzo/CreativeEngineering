// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/ChronometerItem.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.api.ActionType;
import earth.terrarium.tempad.api.context.ContextRegistry;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.common.registries.ModTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.IntSupplier;

public class ChronometerItem extends CapacitorItem {
    public final IntSupplier rate;
    public final IntSupplier amount;

    public ChronometerItem(Properties properties, IntSupplier rate, IntSupplier amount) {
        super(properties);
        this.rate = rate;
        this.amount = amount;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof Player player) || entity.tickCount % rate.getAsInt() != 0 || isClashing(player, stack)) return; // 1 mb every 1.2 seconds, 1 bucket per day
        ChrononHandler own = ChrononHandler.of(stack);
        if (own != null) own.insert(amount.getAsInt(), ActionType.Execute);
        if (ChrononHandler.of(stack) != null) {
            distribute(player, stack);
        } else {
            SyncableContext<?> target = ContextRegistry.locate(player, it -> isChargable(stack, it));
            if (target != null) {
                ChrononHandler chronons = ChrononHandler.of(target.getStack());
                if (chronons != null) chronons.insert(amount.getAsInt(), ActionType.Execute);
            }
        }
    }

    @Override
    public boolean isChargable(ItemStack source, ItemStack target) {
        ChrononHandler chronons = ChrononHandler.of(target);
        return chronons != null && chronons.hasRoom() && target != source && !target.is(ModTags.chargeBlacklist);
    }

    public boolean isClashing(Player player, ItemStack stack) {
        return ContextRegistry.locate(player, it -> it.is(ModTags.chrononGens) && it != stack) != null;
    }
}
