package com.SaintAlfonzo.creativecalendar.time;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Listeners for a {@code /ccaltime date} move (plan_rules_fixes.md section 12f, D24). A jar that keeps its own clock
 * on the sun (the Vinery port's vintage clock) registers at setup; the command calls every listener with the server and
 * the move in sun units, so Calendar names no other jar's classes.
 * <pre>
 *     DateMoves.register((server, delta) -> { VintageClock.add(server, delta); return "wine clock moved back"; });
 * </pre>
 */
public final class DateMoves {
	/** One clock that follows a date move. Returns a short note for the command's output, or null for none. */
	@FunctionalInterface
	public interface Listener {
		String moved(MinecraftServer server, long delta);
	}

	private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

	private DateMoves() {}

	public static void register(Listener listener) {
		LISTENERS.add(listener);
	}

	public static boolean any() {
		return !LISTENERS.isEmpty();
	}

	/** Calls every listener; the notes they return, in order. A listener that throws gives a note and does not stop the rest. */
	static List<String> fire(MinecraftServer server, long delta) {
		List<String> notes = new ArrayList<>();
		for (Listener l : LISTENERS) {
			try {
				String note = l.moved(server, delta);
				if (note != null) notes.add(note);
			} catch (RuntimeException e) {
				notes.add("a clock was NOT moved (see log)");
				org.slf4j.LoggerFactory.getLogger(DateMoves.class).warn("[time] date move listener failed", e);
			}
		}
		return notes;
	}
}
