package com.SaintAlfonzo.creativedelight.farm.shippingbin;

/**
 * The sale arithmetic of ruling D3a, free of game classes (headless test: tools/tests/shipping_bin_sale_math.sh). With stored
 * value V (credit plus every input item's value) and capacity C, a sale pays {@code floor(V / C)} loads and takes whole items
 * slot by slot ({@code ceil(needed / value)} from each, needed = loads x C - credit) until covered; the overshoot is the new
 * credit.
 */
public final class SaleMath {
	private SaleMath() {
	}

	/** Result: items taken per slot, loads paid, the new credit. */
	public record Sale(int[] taken, int loads, int credit) {
	}

	/**
	 * @param counts items per input slot (not changed)
	 * @param values points per item per slot, 0 = not sellable
	 */
	public static Sale sell(int[] counts, int[] values, int credit, int capacity) {
		long total = credit;
		for (int i = 0; i < counts.length; i++) total += (long) counts[i] * values[i];
		int loads = (int) Math.min(Integer.MAX_VALUE, total / Math.max(1, capacity));
		int[] taken = new int[counts.length];
		if (loads <= 0) return new Sale(taken, 0, credit);
		long shipment = (long) loads * capacity;
		long needed = shipment - credit;
		long got = 0;
		for (int i = 0; i < counts.length && got < needed; i++) {
			if (counts[i] <= 0 || values[i] <= 0) continue;
			long count = Math.min(counts[i], (needed - got + values[i] - 1) / values[i]);
			taken[i] = (int) count;
			got += count * values[i];
		}
		return new Sale(taken, loads, (int) Math.max(0, credit + got - shipment));
	}
}
