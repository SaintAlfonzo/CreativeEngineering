// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/ChrononItem.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.api.context.ContextRegistry;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.common.registries.ModTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public abstract class ChrononItem extends Item {
    /** Upstream {@code Item(Properties().stacksTo(1))}; the caller's properties carry the creative tab. */
    protected ChrononItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        ChrononHandler chronons = ChrononHandler.of(stack);
        return Optional.ofNullable(chronons == null ? null : chronons.tooltip());
    }

    public boolean cannotDistribute(Entity entity, ItemStack stack) {
        return !(entity instanceof Player player) || ContextRegistry.locate(player, it -> isChargable(stack, it)) == null;
    }

    public void distribute(Player player, ItemStack stack) {
        SyncableContext<?> target = ContextRegistry.locate(player, it -> isChargable(stack, it));
        if (target == null) return;
        ChrononHandler from = ChrononHandler.of(stack);
        ChrononHandler to = ChrononHandler.of(target.getStack());
        if (from != null && to != null) ChrononHandler.move(from, to, Integer.MAX_VALUE);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    public boolean isChargable(ItemStack source, ItemStack target) {
        ChrononHandler chronons = ChrononHandler.of(target);
        return chronons != null && chronons.hasRoom() && target != source && !target.is(ModTags.chargeBlacklist);
    }
}
