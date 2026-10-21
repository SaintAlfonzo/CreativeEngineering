package net.satisfy.vinery.core.wine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.network.PacketDistributor;
import net.satisfy.vinery.forge.VineryConfig;
import net.satisfy.vinery.forge.network.VineryNetwork;
import net.satisfy.vinery.forge.network.VintageClockPacket;
import org.jetbrains.annotations.NotNull;

/**
 * The vintage clock wines age on (feature_writeup/alcohol.md B4.5). One counter per world, saved in the overworld
 * ({@code data/vinery_aging.dat}). Only {@link VineryConfig#CLOCK_MODE} moves it. The pack runs it on the sun
 * ({@code WORLD_TIME}, time.md 3.7): it moves with the overworld day time like the seasons, so sleeping ages wine, and
 * one day is 24000 clock units, sunrise to sunrise. {@code PLAYTIME} (B4.5) is still there.
 * <p>
 * Clients get the clock and the aging settings in {@link VintageClockPacket} (on login and every 5 s), so tooltips
 * show what the server will use.
 */
public final class VintageClock {
    private static final String NAME = "vinery_aging";
    private static final int SYNC_INTERVAL = 100;
    /** dayTime advances of more than this per tick are sleeps or commands, not the day passing. */
    private static final long MAX_NATURAL_STEP = 20;
    /** Running average of the world-day length over about three Better Days days. */
    private static final double MEASURE_WINDOW = 3 * 130_000;

    /** What the client knows, from the last packet. */
    private static Snapshot client;
    private static long clientReceivedAt;

    private VintageClock() {
    }

    /** The settings and clock value the aging math uses. */
    public record Snapshot(long ticks, double ticksPerDay, int daysPerYear, int yearsPerEffectLevel, int maxLevel,
                           int durationPerYear, int maxDuration, int cellarPercent, int coldPercent, int warmPercent, boolean ageOnlyInStorage,
                           boolean agingEnabled, double rate, double measuredDayTicks) {
        public double ticksPerYear() {
            return ticksPerDay * daysPerYear;
        }

        /** Clock ticks as real game ticks at the clock's pace: on the sun clock they are day time units. */
        public long realTicks(double clockTicks) {
            return (long) Math.ceil(clockTicks / (rate > 0 ? rate : 1.0));
        }
    }

    public static Snapshot of(Level level) {
        if (level != null && level.isClientSide && client != null) {
            long elapsed = Math.max(0, level.getGameTime() - clientReceivedAt);
            return new Snapshot(client.ticks + (long) (elapsed * client.rate), client.ticksPerDay, client.daysPerYear,
                    client.yearsPerEffectLevel, client.maxLevel, client.durationPerYear, client.maxDuration,
                    client.cellarPercent, client.coldPercent, client.warmPercent, client.ageOnlyInStorage, client.agingEnabled, client.rate, client.measuredDayTicks);
        }
        Data data = level instanceof ServerLevel server ? data(server.getServer()) : null;
        return fromConfig(data == null ? 0 : data.ticks, data == null ? -1 : data.measuredDayTicks(), rate(data));
    }

    private static Snapshot fromConfig(long ticks, double measuredDayTicks, double rate) {
        return new Snapshot(ticks, ticksPerDay(measuredDayTicks), Math.max(1, VineryConfig.DAYS_PER_YEAR.get()),
                Math.max(1, VineryConfig.YEARS_PER_EFFECT_LEVEL.get()), Math.max(0, VineryConfig.MAX_LEVEL.get()),
                Math.max(0, VineryConfig.DURATION_PER_YEAR.get()), Math.max(0, VineryConfig.MAX_DURATION.get()),
                (int) Math.round(VineryConfig.CELLAR_AGING_MULTIPLIER.get() * 100), (int) Math.round(VineryConfig.COLD_AGING_MULTIPLIER.get() * 100),
                (int) Math.round(VineryConfig.WARM_AGING_MULTIPLIER.get() * 100), VineryConfig.AGE_ONLY_IN_STORAGE.get(),
                VineryConfig.AGING_ENABLED.get(), rate, measuredDayTicks);
    }

    /** Clock ticks per aging day under the current settings. */
    private static double ticksPerDay(double measuredDayTicks) {
        if (VineryConfig.CLOCK_MODE.get() == VineryConfig.ClockMode.WORLD_TIME || VineryConfig.DAY_UNIT.get() == VineryConfig.DayUnit.DAYTIME) {
            return 24000; // the WORLD_TIME clock counts day time units (time.md 3.7)
        }
        if (VineryConfig.DAY_UNIT.get() == VineryConfig.DayUnit.WORLD) {
            double minutes = VineryConfig.WORLD_DAY_MINUTES.get();
            if (minutes > 0) {
                return minutes * 1200;
            }
            return measuredDayTicks > 0 ? measuredDayTicks : 24000;
        }
        double ticksPerYear = VineryConfig.PLAYTIME_HOURS_PER_YEAR.get() * 72000;
        return Math.max(1, ticksPerYear / Math.max(1, VineryConfig.DAYS_PER_YEAR.get()));
    }

    // ---------------------------------------------------------------- server

    public static Data data(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(Data::load, Data::new, NAME);
    }

    /** Server tick, end phase. */
    public static void tick(MinecraftServer server) {
        Data data = data(server);
        long dayTime = server.overworld().getDayTime();
        long step = data.lastDayTime < 0 ? 0 : dayTime - data.lastDayTime;
        boolean playersOnline = server.getPlayerCount() > 0;
        switch (VineryConfig.CLOCK_MODE.get()) {
            case PLAYTIME -> {
                if (playersOnline) data.ticks++;
            }
            case SERVER_TICKS -> data.ticks++;
            case WORLD_TIME -> {
                if (step > 0) data.ticks += step;
            }
        }
        if (playersOnline && step >= 0 && step <= MAX_NATURAL_STEP) {
            data.measuredSteps += step;
            data.measuredTicks++;
            if (data.measuredTicks > MEASURE_WINDOW) {
                data.measuredSteps /= 2;
                data.measuredTicks /= 2;
            }
        }
        data.lastDayTime = dayTime;
        if (server.getTickCount() % 600 == 0) {
            data.setDirty();
        }
        if (server.getTickCount() % SYNC_INTERVAL == 0) {
            VineryNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), packet(server));
        }
    }

    public static void sync(ServerPlayer player) {
        VineryNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet(player.getServer()));
    }

    private static VintageClockPacket packet(MinecraftServer server) {
        Data data = data(server);
        return new VintageClockPacket(fromConfig(data.ticks, data.measuredDayTicks(), rate(data)));
    }

    /**
     * Clock ticks per real tick: 1, or on the sun clock the measured day time pace (worldDayMinutes until a minute of
     * play has been measured).
     */
    private static double rate(Data data) {
        if (VineryConfig.CLOCK_MODE.get() != VineryConfig.ClockMode.WORLD_TIME) {
            return 1.0;
        }
        if (data != null && data.measuredDayTicks() > 0) {
            return (double) data.measuredSteps / data.measuredTicks;
        }
        double minutes = VineryConfig.WORLD_DAY_MINUTES.get();
        return minutes > 0 ? 24000 / (minutes * 1200) : 1.0;
    }

    /** Moves the clock (/wine clock). */
    public static void add(MinecraftServer server, long ticks) {
        Data data = data(server);
        data.ticks = Math.max(0, data.ticks + ticks);
        data.setDirty();
        VineryNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), packet(server));
    }

    public static void set(MinecraftServer server, long ticks) {
        Data data = data(server);
        data.ticks = Math.max(0, ticks);
        data.setDirty();
        VineryNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), packet(server));
    }

    // ---------------------------------------------------------------- client

    public static void receive(Snapshot snapshot, long clientGameTime) {
        client = snapshot;
        clientReceivedAt = clientGameTime;
    }

    public static void clearClient() {
        client = null;
    }

    public static final class Data extends SavedData {
        long ticks;
        long lastDayTime = -1;
        long measuredSteps;
        long measuredTicks;

        /** World-day length in ticks from the measured dayTime pace, -1 before a minute of play. */
        double measuredDayTicks() {
            if (measuredTicks < 1200 || measuredSteps <= 0) {
                return -1;
            }
            return 24000.0 * measuredTicks / measuredSteps;
        }

        public long ticks() {
            return ticks;
        }

        static Data load(CompoundTag tag) {
            Data data = new Data();
            data.ticks = tag.getLong("Ticks");
            data.measuredSteps = tag.getLong("MeasuredSteps");
            data.measuredTicks = tag.getLong("MeasuredTicks");
            return data;
        }

        @Override
        public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
            tag.putLong("Ticks", ticks);
            tag.putLong("MeasuredSteps", measuredSteps);
            tag.putLong("MeasuredTicks", measuredTicks);
            return tag;
        }
    }
}
