package com.SaintAlfonzo.creativeengineering.fan;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * Result pools for Create processing recipes (plan_flesh_recipes.md 3 and 5.3). A recipe JSON with
 * {@code "result_pools": [[0, 1, 2], [3, 4]]} groups its results by index. Per processed item each pool gives
 * {@code floor(sum of its chances)} picks plus one more at the fractional chance, and each pick is exactly one of the
 * pool's outputs, weighted by its chance. So a pool's chances are the overall chance per item of each output, as JEI
 * shows them. Results in no pool keep Create's independent roll.
 *
 * <p>Several pools per recipe, where {@link ExclusiveResults} has one. Wired in by
 * {@code ProcessingRecipeSerializerMixin} (JSON and network), {@code ProcessingRecipeMixin} (the roll) and the
 * Create JEI crushing and milling category mixins (one slot per pool).
 */
public final class ResultPools {

    public static final String JSON_KEY = "result_pools";

    private static final float EPSILON = 1.0e-4f;
    private static final Random RANDOM = new Random();

    private ResultPools() {}

    /** Parses and checks the key; returns null (and an error text in {@code problem[0]}) when it is malformed. */
    public static int[][] parse(JsonElement element, int resultCount, String[] problem) {
        if (!element.isJsonArray()) {
            problem[0] = "must be a list of lists of result indices";
            return null;
        }
        JsonArray outer = element.getAsJsonArray();
        int[][] pools = new int[outer.size()][];
        boolean[] used = new boolean[resultCount];
        for (int p = 0; p < outer.size(); p++) {
            if (!outer.get(p).isJsonArray() || outer.get(p).getAsJsonArray().size() == 0) {
                problem[0] = "pool " + p + " must be a non-empty list of result indices";
                return null;
            }
            JsonArray inner = outer.get(p).getAsJsonArray();
            pools[p] = new int[inner.size()];
            for (int n = 0; n < inner.size(); n++) {
                int index = inner.get(n).getAsInt();
                if (index < 0 || index >= resultCount) {
                    problem[0] = "pool " + p + " names result " + index + ", the recipe has " + resultCount;
                    return null;
                }
                if (used[index]) {
                    problem[0] = "result " + index + " is in more than one pool";
                    return null;
                }
                used[index] = true;
                pools[p][n] = index;
            }
        }
        return pools.length == 0 ? null : pools;
    }

    public static void write(FriendlyByteBuf buf, int[][] pools) {
        if (pools == null) {
            buf.writeVarInt(0);
            return;
        }
        buf.writeVarInt(pools.length);
        for (int[] pool : pools) buf.writeVarIntArray(pool);
    }

    public static int[][] read(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count == 0) return null;
        int[][] pools = new int[count][];
        for (int p = 0; p < count; p++) pools[p] = buf.readVarIntArray();
        return pools;
    }

    /** True when every index fits {@code outputs}; a filtered or foreign list falls back to Create's roll. */
    public static boolean fits(int[][] pools, List<ProcessingOutput> outputs) {
        for (int[] pool : pools)
            for (int index : pool)
                if (index >= outputs.size()) return false;
        return true;
    }

    public static float amount(int[] pool, List<ProcessingOutput> outputs) {
        float sum = 0;
        for (int index : pool) sum += outputs.get(index).getChance();
        return sum;
    }

    /** Whole picks of a pool amount, with float slack so 0.35 + 0.2 + ... = 0.99999 counts as 1. */
    public static int wholePicks(float amount) {
        return (int) Math.floor(amount + EPSILON);
    }

    /** The chance of one extra pick, 0 when the amount is whole. */
    public static float extraChance(float amount) {
        float extra = amount - wholePicks(amount);
        return extra < EPSILON ? 0 : extra;
    }

    public static List<ItemStack> roll(List<ProcessingOutput> outputs, int[][] pools) {
        List<ItemStack> results = new ArrayList<>();
        boolean[] pooled = new boolean[outputs.size()];
        for (int[] pool : pools) {
            for (int index : pool) pooled[index] = true;
            float amount = amount(pool, outputs);
            if (amount <= 0) continue;
            int picks = wholePicks(amount);
            float extra = extraChance(amount);
            if (extra > 0 && RANDOM.nextFloat() < extra) picks++;
            for (int pick = 0; pick < picks; pick++) {
                float r = RANDOM.nextFloat() * amount;
                int chosen = pool[pool.length - 1];
                for (int index : pool) {
                    r -= outputs.get(index).getChance();
                    if (r < 0) {
                        chosen = index;
                        break;
                    }
                }
                ItemStack stack = outputs.get(chosen).getStack().copy();
                if (!stack.isEmpty()) results.add(stack);
            }
        }
        for (int index = 0; index < outputs.size(); index++) {
            if (pooled[index]) continue;
            ItemStack stack = outputs.get(index).rollOutput();
            if (!stack.isEmpty()) results.add(stack);
        }
        return results;
    }
}
