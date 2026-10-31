package com.SaintAlfonzo.creativeengineering.fan;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;

import net.minecraft.world.item.ItemStack;

/**
 * Mutually exclusive chance outputs for Create processing recipes (any type: fans, D&D sanding,
 * milling, ...). A recipe JSON with {@code "exclusive_results": true} rolls once per processed item
 * and gives exactly one of its outputs, picked by weight: output chances are slices of [0, 1) in
 * listed order. If the chances add up to less than 1 the remainder gives nothing.
 *
 * <p>Create's own rule rolls every output independently, so 80 % A + 20 % B there can give A and B
 * together or neither. Wired in by {@code ProcessingRecipeSerializerMixin} (reads the flag) and
 * {@code ProcessingRecipeMixin} (replaces {@code rollResults}).
 */
public final class ExclusiveResults {

    public static final String JSON_KEY = "exclusive_results";

    private static final Random RANDOM = new Random();

    private ExclusiveResults() {}

    public static List<ItemStack> roll(List<ProcessingOutput> outputs) {
        List<ItemStack> results = new ArrayList<>(1);
        float r = RANDOM.nextFloat();
        for (ProcessingOutput output : outputs) {
            r -= output.getChance();
            if (r < 0) {
                ItemStack stack = output.getStack().copy();
                if (!stack.isEmpty()) results.add(stack);
                break;
            }
        }
        return results;
    }

    public static float chanceSum(List<ProcessingOutput> outputs) {
        float sum = 0;
        for (ProcessingOutput output : outputs) sum += output.getChance();
        return sum;
    }
}
