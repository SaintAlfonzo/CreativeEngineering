import java.util.ArrayList;
import java.util.List;

import com.SaintAlfonzo.creativedelight.shared.LeftoverPlan;
import com.SaintAlfonzo.creativedelight.shared.OverflowPull;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Headless checks of the remainder buffer routing every station shares (feature_writeup/remainder_buffer.md 3.8):
 * LeftoverPlan (container slot merge, overflow same-stack then empty, servedIn into an empty container slot, what is
 * returned, slot limits and stack sizes, dry-run copies) and OverflowPull (moves only what the waiting meals need,
 * after the delay, restarts the wait on a new candidate). Runs against Creative Delight's build/classes
 * (tools/test_remainder_buffer.sh). The pot / kettle / oven GameTests in tools/remainder_buffer_test/src need a server.
 */
public class RemainderLogicTest {
    static int fails = 0, checks = 0;

    static void check(boolean ok, String what) {
        checks++;
        if (!ok) fails++;
        System.out.println((ok ? "ok   " : "FAIL ") + what);
    }

    static ItemStack st(net.minecraft.world.item.Item i, int n) {
        return new ItemStack(i, n);
    }

    static boolean is(ItemStack s, net.minecraft.world.item.Item i, int n) {
        return s != null && (n == 0 ? s.isEmpty() : s.is(i) && s.getCount() == n);
    }

    static int[] lim(int n, int v) {
        int[] l = new int[n];
        java.util.Arrays.fill(l, v);
        return l;
    }

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        ItemStack E = ItemStack.EMPTY;

        // 1. container slot holding the same item takes it first
        ItemStack[] s = {st(Items.BOWL, 3), E, E};
        List<ItemStack> rest = LeftoverPlan.place(List.of(st(Items.BOWL, 2)), s, lim(3, 64), x -> true);
        check(rest.isEmpty() && is(s[0], Items.BOWL, 5) && s[1].isEmpty(), "same item in container slot: merged there");

        // 2. container slot holds another item: overflow, a matching stack before an empty slot
        s = new ItemStack[] {st(Items.BOWL, 1), E, st(Items.GLASS_BOTTLE, 2)};
        rest = LeftoverPlan.place(List.of(st(Items.GLASS_BOTTLE, 3)), s, lim(3, 64), x -> false);
        check(rest.isEmpty() && is(s[0], Items.BOWL, 1) && s[1].isEmpty() && is(s[2], Items.GLASS_BOTTLE, 5),
                "other item in container slot: overflow, matching stack before the empty slot");

        // 3. overflow full, empty container slot: servedIn takes it, anything else is returned
        s = new ItemStack[] {E, st(Items.STICK, 64)};
        rest = LeftoverPlan.place(List.of(st(Items.BOWL, 1)), s, lim(2, 64), x -> x.is(Items.BOWL));
        check(rest.isEmpty() && is(s[0], Items.BOWL, 1), "overflow full: servedIn leftover goes to the empty container slot");
        s = new ItemStack[] {E, st(Items.STICK, 64)};
        rest = LeftoverPlan.place(List.of(st(Items.GLASS_BOTTLE, 1)), s, lim(2, 64), x -> x.is(Items.BOWL));
        check(rest.size() == 1 && is(rest.get(0), Items.GLASS_BOTTLE, 1) && s[0].isEmpty(),
                "overflow full: leftover that is not servedIn is returned (thrown or stalls)");

        // 4. empty container slot, free overflow: overflow first even when servedIn
        s = new ItemStack[] {E, E};
        rest = LeftoverPlan.place(List.of(st(Items.BOWL, 1)), s, lim(2, 64), x -> true);
        check(rest.isEmpty() && s[0].isEmpty() && is(s[1], Items.BOWL, 1), "empty container slot is the last resort");

        // 5. limits: slot limit and the item's own stack size
        s = new ItemStack[] {st(Items.BOWL, 7), E};
        rest = LeftoverPlan.place(List.of(st(Items.BOWL, 5)), s, new int[] {8, 64}, x -> true);
        check(rest.isEmpty() && is(s[0], Items.BOWL, 8) && is(s[1], Items.BOWL, 4), "container slot limit 8, rest to overflow");
        s = new ItemStack[] {null, st(Items.BUCKET, 15)};
        rest = LeftoverPlan.place(List.of(st(Items.BUCKET, 3)), s, lim(2, 64), x -> true);
        check(rest.size() == 1 && is(rest.get(0), Items.BUCKET, 2) && is(s[1], Items.BUCKET, 16),
                "buckets stack to 16; no container slot (null): the rest is returned");

        // 6. several leftovers in order, empties skipped
        s = new ItemStack[] {E, E, E};
        rest = LeftoverPlan.place(List.of(st(Items.BOWL, 1), E, st(Items.GLASS_BOTTLE, 1), st(Items.BOWL, 1)), s, lim(3, 64), x -> false);
        check(rest.isEmpty() && is(s[1], Items.BOWL, 2) && is(s[2], Items.GLASS_BOTTLE, 1), "several leftovers in order");

        // 7. dry run on copies leaves the originals alone
        ItemStack[] real = {st(Items.BOWL, 1), E};
        ItemStack[] dry = LeftoverPlan.copies(real);
        LeftoverPlan.place(List.of(st(Items.BOWL, 4)), dry, lim(2, 64), x -> true);
        check(is(real[0], Items.BOWL, 1) && is(dry[0], Items.BOWL, 5), "copies(): dry run does not touch the station");
        check(LeftoverPlan.copies(null, E)[0] == null, "copies(): null stays null");

        // OverflowPull: container slot 0, overflow slots 1 and 2
        ItemStack[] slots = {E, st(Items.BOWL, 5), E};
        OverflowPull.Slots view = new OverflowPull.Slots() {
            public ItemStack get(int i) { return slots[i]; }
            public void set(int i, ItemStack v) { slots[i] = v; }
            public int limit(int i) { return 64; }
        };
        OverflowPull pull = new OverflowPull();
        List<Boolean> moves = new ArrayList<>();
        for (int t = 0; t < 4; t++) moves.add(pull.tick(view, 0, new int[] {1, 2}, 3, x -> x.is(Items.BOWL), 2));
        check(moves.equals(List.of(false, false, true, false)) && is(slots[0], Items.BOWL, 3) && is(slots[1], Items.BOWL, 2),
                "pull: waits 2 ticks, then moves only the 3 the meals need " + moves);
        check(!pull.tick(view, 0, new int[] {1, 2}, 0, x -> true, 0), "pull: no meal waiting, no move");
        slots[0] = E;
        slots[1] = st(Items.GLASS_BOTTLE, 4);
        check(!pull.tick(view, 0, new int[] {1, 2}, 2, x -> x.is(Items.BOWL), 0) && slots[0].isEmpty(),
                "pull: an item that does not plate the meal stays");
        slots[1] = st(Items.BOWL, 4);
        slots[0] = st(Items.BOWL, 1);
        check(pull.tick(view, 0, new int[] {1, 2}, 2, x -> x.is(Items.BOWL), 0) && is(slots[0], Items.BOWL, 2)
                && is(slots[1], Items.BOWL, 3), "pull: counts containers already in the slot (2 meals, 1 bowl there: moves 1)");
        slots[0] = E;
        slots[1] = st(Items.BOWL, 1);
        slots[2] = st(Items.BOWL, 1);
        OverflowPull p2 = new OverflowPull();
        p2.tick(view, 0, new int[] {1, 2}, 1, x -> x.is(Items.BOWL), 1);
        slots[1] = E; // candidate changes to slot 2: the wait starts over
        boolean first = p2.tick(view, 0, new int[] {1, 2}, 1, x -> x.is(Items.BOWL), 1);
        boolean second = p2.tick(view, 0, new int[] {1, 2}, 1, x -> x.is(Items.BOWL), 1);
        check(!first && second && is(slots[0], Items.BOWL, 1) && slots[2].isEmpty(), "pull: a new candidate restarts the wait");

        System.out.println(fails == 0 ? "ALL " + checks + " OK" : fails + " FAILED of " + checks);
        System.exit(fails == 0 ? 0 : 1);
    }
}
