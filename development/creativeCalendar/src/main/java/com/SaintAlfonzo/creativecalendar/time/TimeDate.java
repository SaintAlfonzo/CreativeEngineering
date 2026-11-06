package com.SaintAlfonzo.creativecalendar.time;

import com.SaintAlfonzo.creativecalendar.CalendarConfig;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.ServerConfig;
import sereneseasons.handler.season.SeasonHandler;
import sereneseasons.season.SeasonSavedData;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code /cetime date ...} (op 2, feature_writeup/time.md 3.11): moves the calendar to a date and keeps every clock on
 * it. A date is one position, day time units since the first sunrise ({@link TimeSettings#firstSunrise()}): year 1,
 * Early Spring, day 1, 6:00 am is 0. Applying it sets
 * <ul>
 * <li>the sun: dayTime = first sunrise + position, in every level, as /time set does;</li>
 * <li>Serene Seasons: seasonCycleTicks = position within the year, in every level that has seasons, and the
 *     {@link SeasonClock} step tracker, so the jump is not added again;</li>
 * <li>the year ({@link CalendarDate}): follows from the two above, it is not stored;</li>
 * <li>the Vinery vintage clock ({@code [time.wine] clock = SUN}): moved by the same amount as the sun, back as well as
 *     forward, so wines age or un-age with the calendar. It adds forward steps itself; backward ones go through its
 *     {@code add}.</li>
 * </ul>
 * Needs {@code [time.seasons] clock = SUN}: on TICKS Serene Seasons' counter is not tied to the sun.
 */
public final class TimeDate {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String VINTAGE_CLOCK = "net.satisfy.vinery.core.wine.VintageClock";
    private static final Pattern TIME = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?");
    private static final String[] UNITS = {"day", "month", "season", "year"};

    private static final SimpleCommandExceptionType NOT_SUN = new SimpleCommandExceptionType(
            Component.literal("Date commands need Serene Seasons and [time.seasons] clock = SUN."));
    private static final DynamicCommandExceptionType BAD_MONTH = new DynamicCommandExceptionType(
            name -> Component.literal("Unknown month " + name + "; use early_spring ... late_winter."));
    private static final DynamicCommandExceptionType BAD_DAY = new DynamicCommandExceptionType(
            days -> Component.literal("A month has " + days + " days."));
    private static final DynamicCommandExceptionType BAD_TIME = new DynamicCommandExceptionType(
            text -> Component.literal("Unknown time " + text + "; use 6:00 am, 7:30 pm or 19:30."));
    private static final DynamicCommandExceptionType BAD_UNIT = new DynamicCommandExceptionType(
            text -> Component.literal("Unknown unit " + text + "; use day, month, season or year."));

    private TimeDate() {}

    public static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("date").requires(s -> s.hasPermission(2))
                .executes(ctx -> show(ctx.getSource()))
                .then(Commands.literal("reset").executes(ctx -> apply(ctx.getSource(), start(), "the world's start")))
                .then(Commands.literal("set")
                        .then(Commands.argument("year", IntegerArgumentType.integer(1, 100_000))
                                .then(month()
                                        .executes(ctx -> set(ctx, 1, null))
                                        .then(Commands.argument("day", IntegerArgumentType.integer(1, 1000))
                                                .executes(ctx -> set(ctx, IntegerArgumentType.getInteger(ctx, "day"), null))
                                                .then(Commands.argument("time", StringArgumentType.greedyString())
                                                        .suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"6:00 am", "12:00 pm", "8:42 pm"}, b))
                                                        .executes(ctx -> set(ctx, IntegerArgumentType.getInteger(ctx, "day"),
                                                                StringArgumentType.getString(ctx, "time"))))))))
                .then(step("next", 1))
                .then(step("previous", -1))
                .then(step("restart", 0))
                .then(Commands.literal("add")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(-100_000, 100_000))
                                .then(unit().executes(ctx -> {
                                    long length = unitUnits(StringArgumentType.getString(ctx, "unit"));
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    return apply(ctx.getSource(), now(ctx.getSource().getServer()) + amount * length,
                                            amount + " x " + StringArgumentType.getString(ctx, "unit"));
                                }))));
    }

    /** next / previous / restart: to the start of the next, previous or current day, month, season or year. */
    private static ArgumentBuilder<CommandSourceStack, ?> step(String name, int offset) {
        return Commands.literal(name).then(unit().executes(ctx -> {
            String unit = StringArgumentType.getString(ctx, "unit");
            long length = unitUnits(unit);
            long target = (Math.floorDiv(now(ctx.getSource().getServer()), length) + offset) * length;
            return apply(ctx.getSource(), target, "start of the " + name + " " + unit);
        }));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> unit() {
        return Commands.argument("unit", StringArgumentType.word()).suggests((ctx, b) -> SharedSuggestionProvider.suggest(UNITS, b));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> month() {
        return Commands.argument("month", StringArgumentType.word()).suggests((ctx, b) -> SharedSuggestionProvider.suggest(
                Arrays.stream(Season.SubSeason.VALUES).map(s -> s.name().toLowerCase(Locale.ROOT)), b));
    }

    private static int set(CommandContext<CommandSourceStack> ctx, int day, String time) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "month");
        Season.SubSeason month = Arrays.stream(Season.SubSeason.VALUES).filter(s -> s.name().equalsIgnoreCase(name))
                .findFirst().orElseThrow(() -> BAD_MONTH.create(name));
        int sub = TimeSettings.daysPerSubSeason();
        if (day > sub) {
            throw BAD_DAY.create(sub);
        }
        long days = (IntegerArgumentType.getInteger(ctx, "year") - 1L) * TimeSettings.yearDays() + (long) month.ordinal() * sub + day - 1;
        return apply(ctx.getSource(), days * TimeSettings.DAY_UNITS + (time == null ? 0 : timeUnits(time)), "the date given");
    }

    /** Day time units since sunrise for "7:30 pm", "7pm", "19:30" or "6" (the pack clock: sunrise is 6:00 am). */
    private static long timeUnits(String text) throws CommandSyntaxException {
        Matcher m = TIME.matcher(text.trim().toLowerCase(Locale.ROOT));
        if (!m.matches()) {
            throw BAD_TIME.create(text);
        }
        int hour = Integer.parseInt(m.group(1));
        int minute = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
        if (m.group(3) != null) {
            if (hour < 1 || hour > 12) {
                throw BAD_TIME.create(text);
            }
            hour = hour % 12 + (m.group(3).equals("pm") ? 12 : 0);
        }
        if (hour > 23 || minute > 59) {
            throw BAD_TIME.create(text);
        }
        return Math.floorMod(hour * 60 + minute - 6 * 60, 24 * 60) * 1000L / 60;
    }

    private static long unitUnits(String unit) throws CommandSyntaxException {
        int days = switch (unit) {
            case "day" -> 1;
            case "month" -> TimeSettings.daysPerSubSeason();
            case "season" -> 3 * TimeSettings.daysPerSubSeason();
            case "year" -> TimeSettings.yearDays();
            default -> throw BAD_UNIT.create(unit);
        };
        return (long) days * TimeSettings.DAY_UNITS;
    }

    /** A fresh world: year 1, day 1 of Serene Seasons' starting sub-season, at dayTime's time of day 0 (7:42 am). */
    private static long start() {
        long month = Math.max(0, Math.min(11, ServerConfig.startingSubSeason.get() - 1));
        return month * TimeSettings.daysPerSubSeason() * TimeSettings.DAY_UNITS
                + Math.floorMod(-TimeSettings.firstSunrise(), (long) TimeSettings.DAY_UNITS);
    }

    /** The date now, read from the overworld's season state and year. */
    private static long now(MinecraftServer server) throws CommandSyntaxException {
        if (!SeasonClock.enabled()) {
            throw NOT_SUN.create();
        }
        ServerLevel overworld = server.overworld();
        SeasonSavedData data = SeasonHandler.getSeasonSavedData(overworld);
        int year = CalendarDate.year(overworld, SeasonHelper.getSeasonState(overworld));
        return (year - 1L) * yearUnits() + (data == null ? 0 : data.seasonCycleTicks);
    }

    private static long yearUnits() {
        return (long) TimeSettings.yearDays() * TimeSettings.DAY_UNITS;
    }

    private static int apply(CommandSourceStack source, long position, String what) throws CommandSyntaxException {
        MinecraftServer server = source.getServer();
        long before = now(server);
        long dayTime = Math.max(0, TimeSettings.firstSunrise() + position); // vanilla and mods expect dayTime >= 0
        position = dayTime - TimeSettings.firstSunrise();
        long delta = dayTime - server.overworld().getDayTime();
        int cycle = (int) Math.floorMod(position, yearUnits());

        for (ServerLevel level : server.getAllLevels()) {
            level.setDayTime(dayTime);
        }
        for (ServerLevel level : server.getAllLevels()) {
            server.getPlayerList().broadcastAll(new ClientboundSetTimePacket(level.getGameTime(), level.getDayTime(),
                    level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)), level.dimension());
            SeasonSavedData data = SeasonHandler.getSeasonSavedData(level);
            if (data != null) {
                data.seasonCycleTicks = cycle;
                data.setDirty();
                SeasonHandler.sendSeasonUpdate(level); // after the time packet, so the client's sun step is overwritten
            }
            SeasonClock.jumped(level);
        }
        String wine = moveWine(server, delta);

        String date = describe(server);
        source.sendSuccess(Component.literal(String.format(Locale.ROOT, "[time] Moved to %s: %s (%s%.1f days; %s)",
                what, date, delta >= 0 ? "+" : "", delta / (double) TimeSettings.DAY_UNITS, wine)), true);
        LOGGER.info("[time] /cetime date: {} -> {} ({}), dayTime {}, seasonCycleTicks {}, sun moved {} units; {}",
                before, position, date, dayTime, cycle, delta, wine);
        return 1;
    }

    /** Forward steps reach the vintage clock on its next tick; backward ones (it ignores them) are added here. */
    private static String moveWine(MinecraftServer server, long delta) {
        if (CalendarConfig.TIME.wineClock.get() != CalendarConfig.Time.WineClock.SUN) {
            return "wine clock on playtime, not moved";
        }
        if (delta >= 0) {
            return "wine clock follows";
        }
        try {
            Class.forName(VINTAGE_CLOCK).getMethod("add", MinecraftServer.class, long.class).invoke(null, server, delta);
            return "wine clock moved back";
        } catch (ClassNotFoundException e) {
            return "no Vinery port";
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("[time] could not move the vintage clock back", e);
            return "wine clock NOT moved back (see log)";
        }
    }

    private static int show(CommandSourceStack source) throws CommandSyntaxException {
        now(source.getServer());
        source.sendSuccess(Component.literal("[time] " + describe(source.getServer())), false);
        return 1;
    }

    /** "Early Spring": a dedicated server has no Serene Seasons lang. */
    private static String monthName(Season.SubSeason month) {
        String[] words = month.name().toLowerCase(Locale.ROOT).split("_");
        return Character.toUpperCase(words[0].charAt(0)) + words[0].substring(1) + " "
                + Character.toUpperCase(words[1].charAt(0)) + words[1].substring(1);
    }

    /** "Year 1, Early Spring, day 1 of 8, 7:42 am (world day 1)". */
    private static String describe(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        var state = SeasonHelper.getSeasonState(overworld);
        int sub = TimeSettings.daysPerSubSeason();
        long dayTime = overworld.getDayTime();
        return String.format(Locale.ROOT, "Year %d, %s, day %d of %d, %s (world day %d)", CalendarDate.year(overworld, state),
                monthName(state.getSubSeason()),
                state.getDay() % sub + 1, sub, TimeSettings.clock(dayTime), TimeSettings.dayNumber(dayTime));
    }
}
