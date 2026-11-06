package com.SaintAlfonzo.creativecalendar.time;

import com.SaintAlfonzo.creativecalendar.CreativeCalendar;
import com.SaintAlfonzo.creativecalendar.seasons.SeasonTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.ServerConfig;

import java.util.List;
import java.util.Locale;

/**
 * The Serene Seasons calendar takes over the removed Vinery calendar's job (feature_writeup/time.md 3.7): its tooltip
 * adds the year, the time of day and the next sub-season, and while it is held the action bar shows the date, as the
 * Vinery calendar did. Season types (arid, frozen, permanent) get a line naming theirs.
 */
@Mod.EventBusSubscriber(modid = CreativeCalendar.MOD_ID, value = Dist.CLIENT)
public final class TimeClient {
    private static final ResourceLocation CALENDAR = new ResourceLocation("sereneseasons", "calendar");
    private static final int ACTION_BAR_INTERVAL = 10;

    private TimeClient() {}

    private static boolean isCalendar(ItemStack stack) {
        return !stack.isEmpty() && CALENDAR.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    private static boolean shows(Level level) {
        return level != null && TimeSettings.active() && ServerConfig.isDimensionWhitelisted(level.dimension());
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (!isCalendar(event.getItemStack()) || player == null || !shows(player.level)) {
            return;
        }
        Level level = player.level;
        ISeasonState state = SeasonHelper.getSeasonState(level);
        List<Component> tooltip = event.getToolTip();
        tooltip.add(yearAndTime(level, state).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.creativeengineering.calendar.next",
                        Component.translatable(subSeasonKey(CalendarDate.next(state))), CalendarDate.daysLeft(state),
                        TimeSettings.realTime(CalendarDate.minutesLeft(level, state)))
                .withStyle(ChatFormatting.DARK_GRAY));
        SeasonTypes.Type type = SeasonTypes.hudTypeAt(level, player.blockPosition());
        if (type != null) {
            tooltip.add(Component.translatable("tooltip.creativeengineering.calendar.here",
                    Component.translatable("tooltip.creativeengineering.calendar.type." + type.name().toLowerCase(Locale.ROOT)))
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || mc.player == null || mc.isPaused()
                || mc.player.tickCount % ACTION_BAR_INTERVAL != 0 || !shows(mc.level)) {
            return;
        }
        if (!isCalendar(mc.player.getMainHandItem()) && !isCalendar(mc.player.getOffhandItem())) {
            return;
        }
        ISeasonState state = SeasonHelper.getSeasonState(mc.level);
        int sub = ServerConfig.subSeasonDuration.get();
        MutableComponent date = Component.translatable(subSeasonKey(state.getSubSeason()))
                .append(Component.translatable("tooltip.creativeengineering.calendar.day", state.getDay() % sub + 1, sub))
                .append(Component.literal(", "))
                .append(yearAndTime(mc.level, state));
        mc.gui.setOverlayMessage(date.withStyle(ChatFormatting.GOLD), false);
    }

    /** "Year 2, 7:30 pm" on the sun clock; "7:30 pm" when the seasons run on ticks (no year count to go by). */
    private static MutableComponent yearAndTime(Level level, ISeasonState state) {
        String time = TimeSettings.clock(level.getDayTime());
        return TimeSettings.sunSeasons()
                ? Component.translatable("tooltip.creativeengineering.calendar.year", CalendarDate.year(level, state), time)
                : Component.literal(time);
    }

    private static String subSeasonKey(Enum<?> subSeason) {
        return "desc.sereneseasons." + subSeason.name().toLowerCase(Locale.ROOT);
    }
}
