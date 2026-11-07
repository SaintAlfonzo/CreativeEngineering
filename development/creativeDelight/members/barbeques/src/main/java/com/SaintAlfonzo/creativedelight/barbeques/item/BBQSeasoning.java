package com.SaintAlfonzo.creativedelight.barbeques.item;

import com.SaintAlfonzo.creativedelight.barbeques.BBQLang;
import com.SaintAlfonzo.creativedelight.barbeques.BarbequesDelight;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * The six seasonings of 1.3.0: item, name prefix and colour. Since the seasoning fold (food/seasoning #11,
 * barbeques_delight.md 4.1) each is a pack seasoning {@code barbequesdelight:<name>} (Creative Delight's
 * {@code kubejs_startup/creativedelight/seasonings.js} holds the effects, values as 1.3.0), put on by a shapeless
 * craft that consumes one item; the click-and-durability use is retired. Powders go on raw and cooked skewers, sauces on
 * cooked ones only (O28), by their seasonable tags.
 */
public enum BBQSeasoning {
    CUMIN(BBQLang.CUMIN, BBQLang.CUMIN_INFO, ChatFormatting.YELLOW, true),
    PEPPER(BBQLang.PEPPER, BBQLang.PEPPER_INFO, ChatFormatting.GRAY, true),
    CHILI(BBQLang.CHILI, BBQLang.CHILI_INFO, ChatFormatting.RED, true),
    HONEY_MUSTARD(BBQLang.HONEY, BBQLang.HONEY_INFO, ChatFormatting.YELLOW, false),
    BUFFALO(BBQLang.BUFFALO, BBQLang.BUFFALO_INFO, ChatFormatting.GOLD, false),
    BARBEQUE(BBQLang.BBQ, BBQLang.BBQ_INFO, ChatFormatting.GOLD, false);

    private final BBQLang title;
    private final BBQLang info;
    public final ChatFormatting color;
    /** Powder (allowed on raw skewers) or sauce (cooked skewers only: O28). */
    public final boolean powder;

    BBQSeasoning(BBQLang title, BBQLang info, ChatFormatting color, boolean powder) {
        this.title = title;
        this.info = info;
        this.color = color;
        this.powder = powder;
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String itemId() {
        return getName() + (powder ? "_powder" : "_sauce");
    }

    /** The pack seasoning id, {@code barbequesdelight:<name>}. */
    public ResourceLocation seasoningId() {
        return BarbequesDelight.loc(getName());
    }

    @Nullable
    public static BBQSeasoning byId(ResourceLocation id) {
        if (!BarbequesDelight.MODID.equals(id.getNamespace())) {
            return null;
        }
        for (BBQSeasoning s : values()) {
            if (s.getName().equals(id.getPath())) {
                return s;
            }
        }
        return null;
    }

    public MutableComponent getTitle() {
        return title.get().withStyle(color);
    }

    public MutableComponent getInfo() {
        return info.get().withStyle(color);
    }
}
