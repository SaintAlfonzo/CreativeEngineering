package com.SaintAlfonzo.creativeengineering.backpackskin;

import com.SaintAlfonzo.creativeengineering.CEConfig;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.event.CurioEquipEvent;

/** Keeps a skinned backpack out of Curios slots while the equip switch is off. Registered only when Curios is loaded. */
public final class BackpackSkinCurios {

    private BackpackSkinCurios() {}

    @SubscribeEvent
    public static void onEquip(CurioEquipEvent event) {
        if (!CEConfig.BACKPACK_SKINS.allowEquip.get() && BackpackSkins.getSkin(event.getStack()) != null) {
            event.setResult(Event.Result.DENY);
        }
    }
}
