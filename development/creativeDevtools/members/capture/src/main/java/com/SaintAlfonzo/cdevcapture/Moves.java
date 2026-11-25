package com.SaintAlfonzo.cdevcapture;

import com.google.gson.JsonObject;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.player.LocalPlayer;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Tick-exact movement (TX-04, client_capture.md 2.2). Keys are held through the game's own key mappings, so the
 * player moves by the normal input path (KeyboardInput, sprint, sneak, jump), counted in client ticks, never in
 * wall-clock time. Every tick of the hold (and of {@code after}) logs
 * {@code [cdevcapture] READ move <n> pos x y z moved dx dy dz hspeed h ground b}; at the end
 * {@code READ walk ticks N dist D bpt S from x y z to x y z}.
 *
 * <pre>
 * {"walk": 40, "keys": "forward sprint", "after": 0}  hold the keys 40 ticks (forward back left right jump sneak
 *                                                    sprint use attack), then log "after" more ticks released
 * {"sneak": 20}                                      = walk 20 with keys "sneak"
 * {"jump": 20}                                       jump held 1 tick, 20 ticks logged in all
 * {"fly": true | false}                              creative flight on or off (needs mayfly)
 * </pre>
 * The keys are pressed at the end of the tick the step starts in, so the first logged tick is the first tick of
 * input. With other keys held, {@code use} and {@code attack} go down {@code ACTION_DELAY} (2) ticks later, so a
 * sneak-use reaches the server as sneak first. Toggle-mode sprint/sneak mappings are pressed once at the start and once at the end.
 */
final class Moves {

	private Moves() {}

	private static final Map<String, Function<Minecraft, KeyMapping>> KEYS = Map.of(
			"forward", m -> m.options.keyUp, "back", m -> m.options.keyDown,
			"left", m -> m.options.keyLeft, "right", m -> m.options.keyRight,
			"jump", m -> m.options.keyJump, "sneak", m -> m.options.keyShift,
			"sprint", m -> m.options.keySprint, "use", m -> m.options.keyUse,
			"attack", m -> m.options.keyAttack);

	static Steps.Step parse(JsonObject o) {
		if (o.has("walk")) {
			return new Hold("walk", o.get("walk").getAsInt(), keys(o.has("keys") ? o.get("keys").getAsString() : "forward"),
					o.has("after") ? o.get("after").getAsInt() : 0);
		}
		if (o.has("sneak")) return new Hold("sneak", o.get("sneak").getAsInt(), keys("sneak"), o.has("after") ? o.get("after").getAsInt() : 0);
		if (o.has("jump")) return new Hold("jump", 1, keys("jump"), Math.max(0, o.get("jump").getAsInt() - 1));
		if (o.has("fly")) {
			boolean on = o.get("fly").getAsBoolean();
			return new Steps.Instant("fly", mc -> {
				if (on && !mc.player.getAbilities().mayfly) throw new Steps.StepFailed("fly: player may not fly");
				mc.player.getAbilities().flying = on;
				mc.player.onUpdateAbilities();
			});
		}
		return null;
	}

	static Set<String> keys(String s) {
		Set<String> out = new LinkedHashSet<>();
		for (String k : s.trim().split("[\\s,+]+")) {
			if (k.isEmpty()) continue;
			if (!KEYS.containsKey(k)) throw new IllegalArgumentException("unknown movement key " + k + " (" + KEYS.keySet() + ")");
			out.add(k);
		}
		return out;
	}

	/** Lets go of every movement key the driver may hold (end of a channel request, test reset). */
	static void releaseKeys(Minecraft mc) {
		for (var f : KEYS.values()) {
			KeyMapping km = f.apply(mc);
			if (!(km instanceof ToggleKeyMapping)) km.setDown(false);
		}
	}

	static final class Hold extends Steps.Step {
		final int hold, after;
		final Set<String> keys;
		double sx, sy, sz;

		Hold(String kind, int hold, Set<String> keys, int after) {
			super(kind);
			if (hold < 1) throw new IllegalArgumentException(kind + ": needs at least 1 tick");
			this.hold = hold;
			this.keys = keys;
			this.after = after;
		}

		@Override
		int maxTicks(CaptureDriver d) {
			return hold + after + 20;
		}

		/** Ticks use / attack wait after the other keys, so a sneak is on the server before the click. */
		static final int ACTION_DELAY = 2;

		private boolean deferAction() {
			return hold > ACTION_DELAY && keys.stream().anyMatch(k -> !k.equals("use") && !k.equals("attack"));
		}

		private void press(Minecraft mc, boolean down, boolean edge) {
			boolean defer = down && deferAction() && ticks < ACTION_DELAY;
			for (String k : keys) {
				if (defer && (k.equals("use") || k.equals("attack"))) continue;
				KeyMapping km = KEYS.get(k).apply(mc);
				if (km instanceof ToggleKeyMapping) {
					// Toggle mode flips on each press: only touch it on the edges.
					if (edge) km.setDown(down);
				} else {
					km.setDown(down);
				}
			}
		}

		@Override
		boolean tick(CaptureDriver d) {
			Minecraft mc = d.mc;
			LocalPlayer p = mc.player;
			if (ticks == 0) {
				sx = p.getX();
				sy = p.getY();
				sz = p.getZ();
				d.read("move", "0 pos " + Reads.f(sx) + " " + Reads.f(sy) + " " + Reads.f(sz) + " start keys " + String.join("+", keys));
				press(mc, true, true);
				return false;
			}
			d.read("move", ticks + " pos " + Reads.f(p.getX()) + " " + Reads.f(p.getY()) + " " + Reads.f(p.getZ()) + " "
					+ Reads.motion(p).replaceFirst("^v \\S+ \\S+ \\S+ ", "") + " ground " + p.isOnGround());
			if (ticks < hold) {
				press(mc, true, false);
				return false;
			}
			if (ticks == hold) {
				press(mc, false, true);
				// Toggle mappings: a second edge clears the toggle.
				for (String k : keys) {
					KeyMapping km = KEYS.get(k).apply(mc);
					if (km instanceof ToggleKeyMapping && km.isDown()) {
						km.setDown(true);
						km.setDown(false);
					}
				}
			}
			if (ticks < hold + after) return false;
			double dx = p.getX() - sx, dz = p.getZ() - sz;
			double dist = Math.sqrt(dx * dx + dz * dz);
			d.read(kind, "ticks " + hold + " dist " + Reads.f(dist) + " bpt " + Reads.f(dist / hold) + " dy "
					+ Reads.f(p.getY() - sy) + " from " + Reads.f(sx) + " " + Reads.f(sy) + " " + Reads.f(sz)
					+ " to " + Reads.f(p.getX()) + " " + Reads.f(p.getY()) + " " + Reads.f(p.getZ()));
			note = "dist " + Reads.f(dist) + " in " + hold + " ticks";
			return true;
		}
	}
}
