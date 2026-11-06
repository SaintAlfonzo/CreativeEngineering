package com.SaintAlfonzo.creativedelight.lunchbox;

import com.tarinoita.solsweetpotato.item.foodcontainer.FoodContainerItem;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The Netherite Lunchbox, a fourth Spice of Life tier (18 slots, two rows of 9). Registered under
 * {@code solapplepie:} so Spice of Life's own screen and lang keys ({@code item.solapplepie.container.*})
 * treat it like the other three. Only registered when Spice of Life is loaded.
 */
public final class LunchboxItems {

    public static final int NETHERITE_SLOTS = 18;

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Lunchboxes.SOL);

    public static final RegistryObject<Item> NETHERITE_LUNCHBOX =
            ITEMS.register(Lunchboxes.NETHERITE_LUNCHBOX.getPath(), NetheriteLunchboxItem::new);

    private LunchboxItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    /** Spice of Life builds the item properties itself, so fire resistance is an override. */
    public static final class NetheriteLunchboxItem extends FoodContainerItem {
        public NetheriteLunchboxItem() {
            super(NETHERITE_SLOTS, Lunchboxes.NETHERITE_LUNCHBOX.getPath());
        }

        @Override
        public boolean isFireResistant() {
            return true;
        }
    }
}
