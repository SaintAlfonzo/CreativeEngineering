package com.SaintAlfonzo.creativecompat.compat;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Drops known harmless log lines that repeat by the hundred thousand (perf.md 19). Each message passes once per
 * session, with a note that the rest are hidden, so a real problem still shows up in the log.
 *
 * Better Fortresses and Better Dungeons (NoBasaltColumnsInStructuresMixin) warn when a basalt columns feature asks a
 * WorldGenRegion for a chunk outside it. Distant Horizons' FEATURES generator hands features smaller regions than
 * vanilla's 3x3, so every basalt column it generates in the Nether warns: 440,000 lines in 15 minutes on 2026-11-07.
 * YUNG's check still runs (it skips the column); only the line is dropped.
 *
 * A config-level filter on the running log4j context: it sees every logger's events, before the message is formatted.
 */
public final class LogSpamFilter extends AbstractFilter {

    private record Rule(String needle, AtomicBoolean seen) {}

    private static final List<Rule> RULES = List.of(
            new Rule("Better Fortresses: Detected a mod with a broken basalt columns configuredfeature", new AtomicBoolean()),
            new Rule("Better Dungeons: Detected a mod with a broken basalt columns configuredfeature", new AtomicBoolean()));

    private LogSpamFilter() {
        super(Result.NEUTRAL, Result.NEUTRAL);
    }

    public static void install() {
        if (!(LogManager.getContext(false) instanceof LoggerContext context)) return;
        LogSpamFilter filter = new LogSpamFilter();
        filter.start();
        context.getConfiguration().addFilter(filter);
        context.updateLoggers();
    }

    private static Result check(Level level, String text) {
        if (text == null) return Result.NEUTRAL;
        for (Rule rule : RULES) {
            if (text.startsWith(rule.needle())) {
                if (rule.seen().compareAndSet(false, true)) {
                    LogManager.getLogger("creativecompat").info("[creativecompat] repeats of the next line are hidden "
                            + "this session (harmless with Distant Horizons generation; LogSpamFilter)");
                    return Result.NEUTRAL;
                }
                return Result.DENY;
            }
        }
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(LogEvent event) {
        Message m = event.getMessage();
        return check(event.getLevel(), m == null ? null : m.getFormat());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return check(level, msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return check(level, msg == null ? null : msg.toString());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return check(level, msg == null ? null : msg.getFormat());
    }
}
