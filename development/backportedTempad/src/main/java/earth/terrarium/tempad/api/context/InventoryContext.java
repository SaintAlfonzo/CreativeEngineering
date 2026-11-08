// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/context/InventoryContext.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.api.context;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.utils.BufCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InventoryContext implements SyncableContext<Integer> {
    public static final ContextType<Integer> TYPE = new ContextType<>(Tempad.id("inventory"), BufCodec.INT);

    public final Player player;
    private final int data;

    public InventoryContext(Player player, int data) {
        this.player = player;
        this.data = data;
    }

    @Override
    public ContextType<Integer> getType() {
        return TYPE;
    }

    @Override
    public Integer getData() {
        return data;
    }

    @Override
    public ItemStack getStack() {
        return player.getInventory().getItem(data);
    }

    @Override
    public void setStack(ItemStack stack) {
        player.getInventory().setItem(data, stack);
    }

    @Override
    public void addStack(ItemStack stack) {
        player.getInventory().placeItemBackInInventory(stack);
    }
}
