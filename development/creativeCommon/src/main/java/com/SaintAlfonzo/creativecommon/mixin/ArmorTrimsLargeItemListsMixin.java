package com.SaintAlfonzo.creativecommon.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.SaintAlfonzo.creativecommon.compat.ArmorTrimsItemCache;

import gg.hipposgrumm.armor_trims.util.LargeItemLists;
import net.minecraft.world.item.Item;

/**
 * Caches Armor Trims 1.3.3's item lists once the item registry is locked; the why and the reset rules are on
 * {@link ArmorTrimsItemCache}. Armor Trims' own code still builds each list the first time.
 */
@Mixin(value = LargeItemLists.class, remap = false)
public abstract class ArmorTrimsLargeItemListsMixin {

    @Inject(method = "getAllItemsOfType", at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativecommon$cachedOfType(Class<? extends Item> type, CallbackInfoReturnable<List<Item>> cir) {
        List<Item> cached = ArmorTrimsItemCache.ofType(type);
        if (cached != null) {
            cir.setReturnValue(cached);
        }
    }

    @Inject(method = "getAllItemsOfType", at = @At("RETURN"), remap = false)
    private static void creativecommon$storeOfType(Class<? extends Item> type, CallbackInfoReturnable<List<Item>> cir) {
        ArmorTrimsItemCache.putOfType(type, cir.getReturnValue());
    }

    @Inject(method = "getAllTrimmable", at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativecommon$cachedTrimmable(CallbackInfoReturnable<List<Item>> cir) {
        List<Item> cached = ArmorTrimsItemCache.trimmable(LargeItemLists.allTrimmableClasses.size(), LargeItemLists.allTrimmableTags.size());
        if (cached != null) {
            cir.setReturnValue(cached);
        }
    }

    @Inject(method = "getAllTrimmable", at = @At("RETURN"), cancellable = true, remap = false)
    private static void creativecommon$storeTrimmable(CallbackInfoReturnable<List<Item>> cir) {
        cir.setReturnValue(ArmorTrimsItemCache.putTrimmable(LargeItemLists.allTrimmableClasses.size(),
                LargeItemLists.allTrimmableTags.size(), cir.getReturnValue()));
    }
}
