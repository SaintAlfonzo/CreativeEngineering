package com.SaintAlfonzo.creativeengineering.time;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.SeasonHelper;

import java.util.List;
import java.util.Locale;

/**
 * {@code /cetime} (everyone): every derived length in ticks, world days and real time, and where each value went
 * (feature_writeup/time.md 3.6). {@code /cetime set day|season|wine ...} and {@code /cetime push} (op 2) edit the
 * {@code [time]} values and push them, for testing.
 */
public final class TimeCommand {
    private TimeCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cetime")
                .executes(ctx -> show(ctx.getSource()))
                .then(Commands.literal("push").requires(s -> s.hasPermission(2))
                        .executes(ctx -> push(ctx.getSource())))
                .then(Commands.literal("set").requires(s -> s.hasPermission(2))
                        .then(Commands.literal("day")
                                .then(Commands.argument("dayMinutes", DoubleArgumentType.doubleArg(1, 10000))
                                        .then(Commands.argument("nightMinutes", DoubleArgumentType.doubleArg(1, 10000))
                                                .executes(ctx -> set(ctx.getSource(),
                                                        CEConfig.TIME.dayMinutes, DoubleArgumentType.getDouble(ctx, "dayMinutes"),
                                                        CEConfig.TIME.nightMinutes, DoubleArgumentType.getDouble(ctx, "nightMinutes"))))))
                        .then(Commands.literal("season")
                                .then(Commands.argument("daysPerSubSeason", IntegerArgumentType.integer(1, 1000))
                                        .executes(ctx -> set(ctx.getSource(),
                                                CEConfig.TIME.daysPerSubSeason, IntegerArgumentType.getInteger(ctx, "daysPerSubSeason"), null, null))))
                        .then(Commands.literal("wine")
                                .then(Commands.argument("monthsPerLevel", IntegerArgumentType.integer(1, 100))
                                        .executes(ctx -> set(ctx.getSource(),
                                                CEConfig.TIME.monthsPerLevel, IntegerArgumentType.getInteger(ctx, "monthsPerLevel"), null, null))))));
    }

    private static int show(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        if (!TimeSettings.active()) {
            source.sendSuccess(line("[time] is off: Better Days, Serene Seasons, SeasonHUD and Vinery use their own files.", ChatFormatting.YELLOW), false);
            return 0;
        }
        long dayTime = level.getDayTime();
        double world = TimeSettings.worldDayMinutes();

        source.sendSuccess(line("Preset: " + CEConfig.TIME.preset.get(), ChatFormatting.AQUA), false);
        source.sendSuccess(header("World day"), false);
        source.sendSuccess(line(String.format(Locale.ROOT, " %s day + %s night = %s; speeds %s by day, %s by night",
                TimeSettings.realTime(CEConfig.TIME.dayMinutes.get()), TimeSettings.realTime(CEConfig.TIME.nightMinutes.get()),
                TimeSettings.realTime(world), TimePush.fmt(TimeSettings.daySpeed()), TimePush.fmt(TimeSettings.nightSpeed())), null), false);
        source.sendSuccess(line(String.format(Locale.ROOT, " Now %s (dayTime %d); sunrise 6:00 am at %.0f, sunset %s at %.0f%s",
                TimeSettings.clock(dayTime), dayTime, TimeSettings.dayStart(), TimeSettings.clock((long) TimeSettings.nightStart()),
                TimeSettings.nightStart(), TimePause.paused(source.getServer()) ? "; PAUSED (nobody online)" : ""), null), false);

        int sub = TimeSettings.daysPerSubSeason();
        source.sendSuccess(header("Seasons (" + CEConfig.TIME.seasonClock.get() + " clock)"), false);
        if (ModList.get().isLoaded("sereneseasons")) {
            ISeasonState state = SeasonHelper.getSeasonState(level);
            int dayOfSub = state.getDay() % sub + 1;
            int ticksIntoSub = state.getSeasonCycleTicks() % state.getSubSeasonDuration();
            int ticksLeft = state.getSubSeasonDuration() - ticksIntoSub;
            double minutesLeft = TimeSettings.sunSeasons() ? TimeSettings.realMinutes(dayTime, ticksLeft) : ticksLeft / 1200.0;
            MutableComponent now = line(" ", null).append(Component.translatable("desc.sereneseasons." + state.getSubSeason().name().toLowerCase(Locale.ROOT)))
                    .append(String.format(Locale.ROOT, ", day %d of %d%s; next sub-season in %s", dayOfSub, sub,
                            TimeSettings.sunSeasons() ? ", year " + CalendarDate.year(level, state) : "", TimeSettings.realTime(minutesLeft)));
            source.sendSuccess(now, false);
        }
        source.sendSuccess(line(String.format(Locale.ROOT, " Sub-season %d days = %s; season %s; year %d days = %s",
                sub, TimeSettings.realTime(sub * world), TimeSettings.realTime(3 * sub * world),
                TimeSettings.yearDays(), TimeSettings.realTime(TimeSettings.yearDays() * world)), null), false);

        int months = CEConfig.TIME.monthsPerLevel.get();
        double cellar = CEConfig.TIME.cellarMultiplier.get();
        double level1 = months * sub * world;
        source.sendSuccess(header("Wine (" + CEConfig.TIME.wineClock.get() + " clock)"), false);
        source.sendSuccess(line(String.format(Locale.ROOT, " Month = one sub-season = %s; +1 level every %d months = %s (%s in a cellar, x%.1f)",
                TimeSettings.realTime(sub * world), months, TimeSettings.realTime(level1), TimeSettings.realTime(level1 / cellar), cellar), null), false);
        int maxLevel = CEConfig.TIME.maxLevel.get();
        source.sendSuccess(line(String.format(Locale.ROOT, " Max level %d at %s (%s in a cellar)",
                maxLevel, TimeSettings.realTime(maxLevel * level1), TimeSettings.realTime(maxLevel * level1 / cellar)), null), false);

        source.sendSuccess(header("Pushed"), false);
        source.sendSuccess(line(String.format(Locale.ROOT,
                " Better Days (getters): speeds above, RATIO. sereneseasons-server.toml: day_duration %d, sub_season_duration %d, progress_season_while_offline %s."
                        + " seasonhud-server.toml: the same lengths. Vinery port (reads [time.wine]): clock %s, daysPerMonth %d, monthsPerEffectLevel %d, cellar %.1f.",
                TimeSettings.seasonDayDuration(), sub, !CEConfig.TIME.pauseWhenEmpty.get(),
                CEConfig.TIME.wineClock.get() == CEConfig.Time.WineClock.SUN ? "WORLD_TIME" : "PLAYTIME", sub, months, cellar), ChatFormatting.GRAY), false);
        return 1;
    }

    private static <A, B> int set(CommandSourceStack source, ForgeConfigSpec.ConfigValue<A> first, A firstValue,
                                  ForgeConfigSpec.ConfigValue<B> second, B secondValue) {
        first.set(firstValue);
        if (second != null) {
            second.set(secondValue);
        }
        CEConfig.COMMON_SPEC.save();
        TimePresets.reconcile();
        return push(source);
    }

    private static int push(CommandSourceStack source) {
        List<String> changes = TimePush.push();
        if (changes.isEmpty()) {
            source.sendSuccess(line("[time] nothing to push: every value already matches.", ChatFormatting.GRAY), true);
        } else {
            changes.forEach(change -> source.sendSuccess(line("[time] " + change, ChatFormatting.GREEN), true));
            source.sendSuccess(line("Players already online see new Serene Seasons / SeasonHUD lengths after rejoining.", ChatFormatting.GRAY), true);
        }
        return changes.size();
    }

    private static MutableComponent header(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GOLD);
    }

    private static MutableComponent line(String text, ChatFormatting style) {
        MutableComponent component = Component.literal(text);
        return style == null ? component : component.withStyle(style);
    }
}
