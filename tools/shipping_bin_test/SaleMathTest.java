import com.SaintAlfonzo.creativedelight.farm.shippingbin.SaleMath;

import java.util.Arrays;

/** Shipping Bin sale arithmetic (ruling D3a, writeup section 7): payouts at 127, 128, 255, 256 points; partial sales; credit. */
public class SaleMathTest {
	private static int failures;

	private static void check(String name, boolean ok, String detail) {
		System.out.println((ok ? "PASS " : "FAIL ") + name + (ok ? "" : ": " + detail));
		if (!ok) failures++;
	}

	private static void sale(String name, int[] counts, int[] values, int credit, int loads, int[] taken, int newCredit) {
		SaleMath.Sale s = SaleMath.sell(counts, values, credit, 128);
		check(name, s.loads() == loads && Arrays.equals(s.taken(), taken) && s.credit() == newCredit,
				"loads " + s.loads() + " taken " + Arrays.toString(s.taken()) + " credit " + s.credit());
	}

	public static void main(String[] args) {
		int[] wheat = {4, 0, 0, 0, 0, 0};
		// 31 wheat = 124 points: no sale (SHB-02); 32 = 128: one load, all taken (SHB-03)
		sale("below capacity", new int[]{31, 0, 0, 0, 0, 0}, wheat, 0, 0, new int[6], 0);
		sale("one load", new int[]{32, 0, 0, 0, 0, 0}, wheat, 0, 1, new int[]{32, 0, 0, 0, 0, 0}, 0);
		// 63 wheat = 252 points: 1 load, 32 taken, 31 stay (SHB-04)
		sale("partial wheat", new int[]{63, 0, 0, 0, 0, 0}, wheat, 0, 1, new int[]{32, 0, 0, 0, 0, 0}, 0);
		// 31 carrots = 155 points: 1 load, 26 taken (130), credit 2 (SHB-04, ruling D3a example)
		sale("partial carrots", new int[]{31, 0, 0, 0, 0, 0}, new int[]{5, 0, 0, 0, 0, 0}, 0, 1, new int[]{26, 0, 0, 0, 0, 0}, 2);
		// credit counts: 2 credit + 5 carrots (25) + 21 carrots (105) = 132 -> 1 load, needs 126 -> 26 carrots (130), credit 4 (SHB-20)
		sale("credit used", new int[]{26, 0, 0, 0, 0, 0}, new int[]{5, 0, 0, 0, 0, 0}, 2, 1, new int[]{26, 0, 0, 0, 0, 0}, 4);
		// 127, 128, 255, 256 points: 0, 1, 1, 2 loads (section 7)
		sale("127 points", new int[]{127, 0, 0, 0, 0, 0}, new int[]{1, 0, 0, 0, 0, 0}, 0, 0, new int[6], 0);
		sale("128 points", new int[]{128, 0, 0, 0, 0, 0}, new int[]{1, 0, 0, 0, 0, 0}, 0, 1, new int[]{128, 0, 0, 0, 0, 0}, 0);
		sale("255 points", new int[]{255, 0, 0, 0, 0, 0}, new int[]{1, 0, 0, 0, 0, 0}, 0, 1, new int[]{128, 0, 0, 0, 0, 0}, 0);
		sale("256 points", new int[]{256, 0, 0, 0, 0, 0}, new int[]{1, 0, 0, 0, 0, 0}, 0, 2, new int[]{256, 0, 0, 0, 0, 0}, 0);
		// unsellable slot (cobblestone) left alone, slots taken in order (SHB-05)
		sale("unsellable stays", new int[]{64, 32, 0, 0, 0, 0}, new int[]{0, 4, 0, 0, 0, 0}, 0, 1, new int[]{0, 32, 0, 0, 0, 0}, 0);
		// several slots: 20 wheat (80) + 10 pumpkin (80) = 160 -> 1 load: 20 wheat (80), then 6 pumpkins (48) -> overshoot 0
		sale("slot order", new int[]{20, 10, 0, 0, 0, 0}, new int[]{4, 8, 0, 0, 0, 0}, 0, 1, new int[]{20, 6, 0, 0, 0, 0}, 0);
		// emeralds through the payout table: floor(shipment * 1/128) = loads
		check("payout scale", Math.floor(256 * 0.0078125F) == 2 && Math.floor(128 * 0.0078125F) == 1, "scale");
		if (failures > 0) {
			System.out.println(failures + " failed");
			System.exit(1);
		}
		System.out.println("all passed");
	}
}
