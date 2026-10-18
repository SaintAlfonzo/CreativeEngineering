package net.satisfy.foundation.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;

public final class Wearing {
    private static final List<BiPredicate<LivingEntity, Item>> SLOT_PROVIDERS = new CopyOnWriteArrayList<>();

    private Wearing() {
    }

    public static void registerSlotProvider(BiPredicate<LivingEntity, Item> provider) {
        SLOT_PROVIDERS.add(provider);
    }

    public static boolean isWearing(LivingEntity entity, Item item) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR && entity.getItemBySlot(slot).is(item)) return true;
        }
        for (BiPredicate<LivingEntity, Item> provider : SLOT_PROVIDERS) {
            if (provider.test(entity, item)) return true;
        }
        return false;
    }

    public static boolean isWearingAny(LivingEntity entity, List<Item> items) {
        for (Item item : items) {
            if (isWearing(entity, item)) return true;
        }
        return false;
    }
}
