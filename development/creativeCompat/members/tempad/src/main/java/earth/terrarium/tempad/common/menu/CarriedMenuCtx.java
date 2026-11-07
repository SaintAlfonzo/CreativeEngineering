// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/menu/CarriedMenuCtx.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.menu;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.context.ContextType;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.common.utils.BufCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CarriedMenuCtx implements SyncableContext<Unit> {
    public static final ContextType<Unit> TYPE = new ContextType<>(Tempad.id("menu_carried_item"), BufCodec.unit(Unit.INSTANCE));

    public final Player player;

    public CarriedMenuCtx(Player player) {
        this.player = player;
    }

    @Override
    public ContextType<Unit> getType() {
        return TYPE;
    }

    @Override
    public Unit getData() {
        return Unit.INSTANCE;
    }

    @Override
    public ItemStack getStack() {
        return player.containerMenu == null ? ItemStack.EMPTY : player.containerMenu.getCarried();
    }

    @Override
    public void setStack(ItemStack stack) {
        if (player.containerMenu != null) player.containerMenu.setCarried(stack);
    }

    @Override
    public void addStack(ItemStack stack) {
        player.getInventory().placeItemBackInInventory(stack);
    }
}
