package com.SaintAlfonzo.creativedelight.barbeques;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;

/** Translation keys, as 1.3.0's BBQLangData (the en_us text lives in lang/en_us.json, not here). */
public enum BBQLang {
    CHANCE_EFFECT("tooltip.chance", ChatFormatting.GRAY),
    INFO_FLIP("info.flip", ChatFormatting.WHITE),
    CHILI("seasoning.chili", ChatFormatting.YELLOW),
    CUMIN("seasoning.cumin", ChatFormatting.YELLOW),
    PEPPER("seasoning.pepper", ChatFormatting.YELLOW),
    HONEY("seasoning.honey_mustard", ChatFormatting.YELLOW),
    BUFFALO("seasoning.buffalo", ChatFormatting.YELLOW),
    BBQ("seasoning.barbeque", ChatFormatting.YELLOW),
    SALTED("seasoning.salted", ChatFormatting.YELLOW),
    GARLIC("seasoning.garlic", ChatFormatting.YELLOW),
    CHILI_INFO("tooltip.chili", ChatFormatting.GRAY),
    CUMIN_INFO("tooltip.cumin", ChatFormatting.GRAY),
    PEPPER_INFO("tooltip.pepper", ChatFormatting.GRAY),
    HONEY_INFO("tooltip.honey_mustard", ChatFormatting.GRAY),
    BUFFALO_INFO("tooltip.buffalo", ChatFormatting.GRAY),
    BBQ_INFO("tooltip.barbeque", ChatFormatting.GRAY),
    NO_SAUCE_RAW("tooltip.no_sauce_raw", ChatFormatting.GRAY),
    JEI_GRILLING("jei.grilling", null),
    JEI_SKEWERING("jei.skewering", null),
    JEI_TIME("jei.time", ChatFormatting.GRAY),
    JEI_MAINHAND("jei.mainhand", ChatFormatting.GRAY),
    JEI_OFFHAND("jei.offhand", ChatFormatting.GRAY),
    JEI_EMPTY("jei.offhand_empty", ChatFormatting.GRAY),
    JEI_BASIN("jei.item_basin", ChatFormatting.GRAY),
    COOK("jade.cook", ChatFormatting.GRAY),
    FLIP("jade.flip", ChatFormatting.YELLOW),
    COOKED("jade.cooked", ChatFormatting.YELLOW),
    BURNT("jade.burnt", ChatFormatting.RED);

    private final String key;
    @Nullable
    private final ChatFormatting format;

    BBQLang(String key, @Nullable ChatFormatting format) {
        this.key = BarbequesDelight.MODID + "." + key;
        this.format = format;
    }

    public MutableComponent get(Object... args) {
        MutableComponent ans = Component.translatable(key, args);
        return format != null ? ans.withStyle(format) : ans;
    }
}
