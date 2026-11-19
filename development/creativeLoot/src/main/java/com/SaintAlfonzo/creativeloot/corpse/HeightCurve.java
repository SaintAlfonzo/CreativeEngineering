package com.SaintAlfonzo.creativeloot.corpse;

import net.minecraft.util.RandomSource;

import java.util.List;

/** The cave corpse height density ({@code cave.y_curve}, ruling 12): linear between [y, weight] points, 0 outside. */
public final class HeightCurve {
	private HeightCurve() {}

	/**
	 * A height drawn from the piecewise-linear density {@code ys}/{@code ws} between {@code min} and {@code max}
	 * (inclusive); MIN_VALUE when the curve has no weight there.
	 */
	public static int sampleY(List<int[]> ys, List<Double> ws, RandomSource r, int min, int max) {
		if (max < min) return Integer.MIN_VALUE;
		double[] w = new double[max - min + 1];
		double total = 0;
		for (int y = min; y <= max; y++) {
			double d = density(ys, ws, y);
			w[y - min] = d;
			total += d;
		}
		if (total <= 0) return Integer.MIN_VALUE;
		double pick = r.nextDouble() * total;
		for (int i = 0; i < w.length; i++) {
			pick -= w[i];
			if (pick < 0) return min + i;
		}
		return max;
	}

	public static double density(List<int[]> ys, List<Double> ws, int y) {
		int n = ys.size();
		if (n == 0) return 0;
		int lowI = -1;
		for (int i = 0; i < n; i++) if (ys.get(i)[0] <= y && (lowI < 0 || ys.get(i)[0] > ys.get(lowI)[0])) lowI = i;
		int highI = -1;
		for (int i = 0; i < n; i++) if (ys.get(i)[0] >= y && (highI < 0 || ys.get(i)[0] < ys.get(highI)[0])) highI = i;
		if (lowI < 0 || highI < 0) return 0; // outside the curve
		int y0 = ys.get(lowI)[0], y1 = ys.get(highI)[0];
		double d0 = ws.get(lowI), d1 = ws.get(highI);
		if (y1 == y0) return Math.max(0, d0);
		return Math.max(0, d0 + (d1 - d0) * (y - y0) / (double) (y1 - y0));
	}

}
