package com.SaintAlfonzo.creativecalendar.seasons;

import com.SaintAlfonzo.creativecalendar.CalendarConfig;
import com.SaintAlfonzo.creativecalendar.CreativeCalendar;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The crop type's seasons on its planting item (and, through {@link #summary}, on WTHIT): "Fertile: Spring" and
 * "Offseason: Winter, Dry" from the type's table (warm crops too: "Fertile: Summer, Dry"), Nether and End crops
 * "Season: Nether" / "Season: End", cave crops their heights, grapes their biome families ("Fertile: Cold biomes",
 * "Offseason: Warm biomes"). Neutral seasons are not shown.
 * feature_writeup/crops.md 2.12.1.
 */
@Mod.EventBusSubscriber(modid = CreativeCalendar.MOD_ID, value = Dist.CLIENT)
public final class CropSeasonTooltip {

    private static final String KEY = "creativeengineering.crop_season.";
    private static final String[] SEASONS = {"spring", "summer", "autumn", "winter"};
    private static final String[] WET_DRY = {"wet", "dry"};

    private CropSeasonTooltip() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!CropSeasons.enabled()) return;
        CropSeasons.Type type = CropSeasons.displayTypeOf(event.getItemStack());
        if (type == null) return;
        List<Component> lines = summary(type);
        List<Component> tooltip = event.getToolTip();
        int at = Math.min(1, tooltip.size());
        tooltip.addAll(at, lines);
    }

    /** The lines for one type, empty for {@code none}. */
    public static List<Component> summary(CropSeasons.Type type) {
        List<Component> out = new ArrayList<>(2);
        switch (type) {
            case NONE, GRAPE -> {}
            case GRAPE_TEMPERATE, GRAPE_COLD, GRAPE_WARM_DRY, GRAPE_WARM_WET -> {
                out.add(line("fertile", Component.translatable(KEY + "family." + type.family.name().toLowerCase(Locale.ROOT)),
                        ChatFormatting.GREEN));
                String opposite = switch (type.family) {
                    case COLD -> "warm";
                    case WARM_DRY, WARM_WET -> "cold";
                    default -> null;
                };
                if (opposite != null) {
                    out.add(line("off", Component.translatable(KEY + "family." + opposite), ChatFormatting.RED));
                }
            }
            case NETHER, END -> out.add(Component.translatable(KEY + "label." + type.id)
                    .withStyle(ChatFormatting.GOLD));
            case CAVE -> {
                out.add(line("fertile", Component.translatable(KEY + "cave_fertile",
                        CalendarConfig.CROP_SEASONS.caveFertileBelowY.get()), ChatFormatting.GREEN));
                out.add(line("off", Component.translatable(KEY + "cave_off",
                        CalendarConfig.CROP_SEASONS.caveOffAboveY.get()), ChatFormatting.RED));
            }
            case MUSHROOM -> {
                out.add(line("fertile", join(Component.translatable(KEY + "mushroom_fields"),
                        CropSeasons.Type.TEMPERATE, CropSeasons.Fertility.FERTILE), ChatFormatting.GREEN));
                out.add(line("off", join(null, CropSeasons.Type.TEMPERATE, CropSeasons.Fertility.OFF), ChatFormatting.RED));
            }
            default -> {
                MutableComponent fertile = join(null, type, CropSeasons.Fertility.FERTILE);
                MutableComponent off = join(null, type, CropSeasons.Fertility.OFF);
                if (fertile != null) out.add(line("fertile", fertile, ChatFormatting.GREEN));
                if (off != null) out.add(line("off", off, ChatFormatting.RED));
            }
        }
        return out;
    }

    private static Component line(String which, Component list, ChatFormatting colour) {
        return Component.translatable(KEY + which, list.copy().withStyle(colour)).withStyle(ChatFormatting.GRAY);
    }

    /** "Spring, Dry": the type's seasons with this fertility, after {@code first}; null when there are none. */
    private static MutableComponent join(Component first, CropSeasons.Type type, CropSeasons.Fertility f) {
        List<Component> parts = new ArrayList<>();
        if (first != null) parts.add(first);
        for (int i = 0; i < 4; i++) {
            if (type.fourSeasons[i] == f) parts.add(Component.translatable("desc.sereneseasons." + SEASONS[i]));
        }
        for (int i = 0; i < 2; i++) {
            if (type.wetDry[i] == f) parts.add(Component.translatable(KEY + WET_DRY[i]));
        }
        if (parts.isEmpty()) return null;
        MutableComponent out = Component.empty();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) out.append(", ");
            out.append(parts.get(i));
        }
        return out;
    }
}
