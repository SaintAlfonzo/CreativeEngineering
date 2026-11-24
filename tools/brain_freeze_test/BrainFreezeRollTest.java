// SPDX-License-Identifier: GPL-3.0-only
// Headless check of Creative Common's BrainFreezeRoll (TODO sweets #9, feature_writeup/smoothies.md 3 and 6):
// the roll window, the chance ladder, the length table and the reset on a hit. Run by tools/test_brain_freeze.sh.
import com.SaintAlfonzo.creativecommon.effect.BrainFreezeRoll;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.DoubleSupplier;

public class BrainFreezeRollTest {
	static final List<Integer> LENGTHS = List.of(5, 8, 11, 14, 20);
	static final long WINDOW = 60 * 20;
	static final double STEP = 0.2;
	static int failures;

	static void check(String what, Object got, Object want) {
		if (!got.equals(want)) {
			failures++;
			System.out.println("FAIL " + what + ": got " + got + ", want " + want);
		}
	}

	/** Random numbers in order; the test fails when one more is asked for than given. */
	static DoubleSupplier seq(double... values) {
		Deque<Double> q = new ArrayDeque<>();
		for (double v : values) q.add(v);
		return () -> {
			if (q.isEmpty()) throw new AssertionError("unexpected roll");
			return q.poll();
		};
	}

	static DoubleSupplier none() {
		return seq();
	}

	public static void main(String[] args) {
		// first cold food: no roll
		BrainFreezeRoll r = new BrainFreezeRoll();
		check("first food no roll", r.eat(0, WINDOW, STEP, LENGTHS, none()), 0);
		check("first food count", r.rolls(), 0);

		// misses at 20, 40, 60, 80 % (random just above the chance), then 100 % hits for the 5th length
		check("roll 1 miss", r.eat(100, WINDOW, STEP, LENGTHS, seq(0.21)), 0);
		check("roll 2 miss", r.eat(200, WINDOW, STEP, LENGTHS, seq(0.41)), 0);
		check("roll 3 miss", r.eat(300, WINDOW, STEP, LENGTHS, seq(0.61)), 0);
		check("roll 4 miss", r.eat(400, WINDOW, STEP, LENGTHS, seq(0.81)), 0);
		check("count before 5th", r.rolls(), 4);
		check("roll 5 hit (100 %)", r.eat(500, WINDOW, STEP, LENGTHS, seq(0.999)), 20);
		check("hit resets count", r.rolls(), 0);
		// the food that hit opens a new window: the next one rolls again at 20 %
		check("after hit roll 1 hit", r.eat(600, WINDOW, STEP, LENGTHS, seq(0.19)), 5);

		// lengths by roll number: hit on roll n gives lengths[n-1]
		int[] want = {5, 8, 11, 14, 20, 20};
		for (int n = 1; n <= 6; n++) {
			BrainFreezeRoll x = new BrainFreezeRoll();
			x.eat(0, WINDOW, STEP, LENGTHS, none());
			int got = 0;
			for (int k = 1; k <= n; k++) {
				// miss every roll before the n-th, hit the n-th (random 0 always hits)
				got = x.eat(k * 10L, WINDOW, k == n ? STEP : 0.0, LENGTHS, seq(k == n ? 0.0 : 0.5));
			}
			check("hit on roll " + n, got, want[n - 1]);
		}

		// window: exactly 60 s still rolls, 60 s + 1 tick starts over without a roll
		BrainFreezeRoll w = new BrainFreezeRoll();
		w.eat(0, WINDOW, STEP, LENGTHS, none());
		check("at window edge rolls", w.eat(WINDOW, WINDOW, STEP, LENGTHS, seq(0.9)), 0);
		check("edge count", w.rolls(), 1);
		check("past window no roll", w.eat(2 * WINDOW + 1, WINDOW, STEP, LENGTHS, none()), 0);
		check("past window count reset", w.rolls(), 0);
		// the window slides with each food: three foods 50 s apart all roll
		w.eat(2 * WINDOW + 1 + 1000, WINDOW, STEP, LENGTHS, seq(0.9));
		w.eat(2 * WINDOW + 1 + 2000, WINDOW, STEP, LENGTHS, seq(0.9));
		check("sliding window count", w.rolls(), 2);

		// time going back (a new world, /time) starts over
		check("time back no roll", w.eat(5, WINDOW, STEP, LENGTHS, none()), 0);

		// length table edge cases and a custom config
		check("empty lengths", BrainFreezeRoll.length(List.of(), 3), 0);
		check("short list uses last", BrainFreezeRoll.length(List.of(7), 4), 7);
		BrainFreezeRoll c = new BrainFreezeRoll();
		c.eat(0, 200, 0.5, List.of(3, 9), none());
		check("custom step miss", c.eat(100, 200, 0.5, List.of(3, 9), seq(0.5)), 0);
		check("custom step hit 2nd", c.eat(200, 200, 0.5, List.of(3, 9), seq(0.99)), 9);
		// chance step 0: never hits
		BrainFreezeRoll z = new BrainFreezeRoll();
		z.eat(0, WINDOW, 0.0, LENGTHS, none());
		for (int k = 1; k <= 10; k++) check("step 0 roll " + k, z.eat(k, WINDOW, 0.0, LENGTHS, seq(0.0)), 0);

		if (failures > 0) {
			System.out.println(failures + " failure(s)");
			System.exit(1);
		}
		System.out.println("BrainFreezeRoll: all checks passed");
	}
}
