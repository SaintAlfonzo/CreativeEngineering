package com.SaintAlfonzo.solraspberrypie.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** A slot's stored choice: effect and level (1-based). Kept when the slot is greyed (5.1). */
public record SlotChoice(ResourceLocation effect, int level) {
    public static void write(FriendlyByteBuf buf, SlotChoice c) {
        buf.writeBoolean(c != null);
        if (c != null) {
            buf.writeResourceLocation(c.effect);
            buf.writeVarInt(c.level);
        }
    }

    public static SlotChoice read(FriendlyByteBuf buf) {
        return buf.readBoolean() ? new SlotChoice(buf.readResourceLocation(), buf.readVarInt()) : null;
    }
}
