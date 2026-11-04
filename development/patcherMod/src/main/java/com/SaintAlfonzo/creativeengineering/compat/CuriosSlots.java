package com.SaintAlfonzo.creativeengineering.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

/**
 * The pack's Curios slots: hands, face, feet (feature_writeup/curios_slots.md). Curios 5.1.x (1.19.2) only takes slots by
 * IMC; the 1.20 datapack format (data/<ns>/curios/slots, curios/entities) is ignored. Priorities slot between the presets
 * (head 40, necklace 60, hands 140, belt 180, charm 200). Icons: Curios stitches every textures/slot/*.png. Only loaded
 * when Curios is.
 */
public final class CuriosSlots {

    private CuriosSlots() {
    }

    public static void enqueue(InterModEnqueueEvent event) {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.HANDS.getMessageBuilder().build());
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("face")
                .priority(50).size(1).icon(new ResourceLocation("minecraft", "item/empty_armor_slot_helmet")).build());
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("feet")
                .priority(190).size(1).icon(new ResourceLocation("minecraft", "item/empty_armor_slot_boots")).build());
    }
}
