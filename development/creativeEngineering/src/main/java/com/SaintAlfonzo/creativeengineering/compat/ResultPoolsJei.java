package com.SaintAlfonzo.creativeengineering.compat;

import java.util.ArrayList;
import java.util.List;

import com.SaintAlfonzo.creativeengineering.fan.ResultPools;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * JEI output slots for a Create recipe with result pools ({@link ResultPools}), used by the crushing and milling
 * category mixins (plan_flesh_recipes.md 5.3). Client only; loaded only from those mixins, which apply only with
 * JEI and Create.
 *
 * <p>Each pool is one slot holding all its items, which JEI cycles like a tag ingredient, so looking up any of
 * them still finds the recipe. The shown item's tooltip keeps Create's chance line (its overall chance per item
 * processed) and adds the pool's picks. Every output in no pool keeps its own slot, after the pools.
 */
public final class ResultPoolsJei {

    private ResultPoolsJei() {}

    /** One output slot: a pool's outputs, or a single output in no pool. */
    public record Slot(List<ProcessingOutput> outputs, boolean pool) {}

    /** The output slots in display order: the pools as listed, then the outputs in no pool. */
    public static List<Slot> slots(List<ProcessingOutput> outputs, int[][] pools) {
        List<Slot> slots = new ArrayList<>();
        boolean[] pooled = new boolean[outputs.size()];
        for (int[] pool : pools) {
            List<ProcessingOutput> members = new ArrayList<>(pool.length);
            for (int index : pool) {
                pooled[index] = true;
                members.add(outputs.get(index));
            }
            slots.add(new Slot(members, true));
        }
        for (int index = 0; index < outputs.size(); index++)
            if (!pooled[index]) slots.add(new Slot(List.of(outputs.get(index)), false));
        return slots;
    }

    /** Fills a slot from {@link #slots}: background, items, tooltip. */
    public static void fill(IRecipeSlotBuilder builder, Slot slot) {
        if (!slot.pool()) {
            ProcessingOutput output = slot.outputs().get(0);
            builder.setBackground(CreateRecipeCategory.getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addTooltipCallback(CreateRecipeCategory.addStochasticTooltip(output));
            return;
        }
        float amount = 0;
        List<ItemStack> stacks = new ArrayList<>(slot.outputs().size());
        for (ProcessingOutput output : slot.outputs()) {
            amount += output.getChance();
            stacks.add(output.getStack());
        }
        // Create's plain slot for a guaranteed pick, its chance slot otherwise
        float shownChance = ResultPools.wholePicks(amount) >= 1 ? 1f : amount;
        builder.setBackground(CreateRecipeCategory.getRenderedSlot(shownChance), -1, -1)
                .addItemStacks(stacks)
                .addTooltipCallback(poolTooltip(slot.outputs(), amount));
    }

    private static IRecipeSlotTooltipCallback poolTooltip(List<ProcessingOutput> slot, float amount) {
        return (view, tooltip) -> {
            ItemStack shown = view.getDisplayedIngredient(VanillaTypes.ITEM_STACK).orElse(ItemStack.EMPTY);
            int line = Math.min(1, tooltip.size());
            for (ProcessingOutput output : slot) {
                if (!ItemStack.isSameItemSameTags(output.getStack(), shown)) continue;
                float chance = output.getChance();
                tooltip.add(line++, Lang.translateDirect("recipe.processing.chance",
                        chance < 0.01 ? "<1" : (int) (chance * 100)).withStyle(ChatFormatting.GOLD));
                break;
            }
            tooltip.add(line, picksLine(amount).withStyle(ChatFormatting.GRAY));
        };
    }

    private static net.minecraft.network.chat.MutableComponent picksLine(float amount) {
        int whole = ResultPools.wholePicks(amount);
        int extra = Math.round(ResultPools.extraChance(amount) * 100);
        String base = "creativeengineering.jei.result_pool.";
        if (extra == 0) return Component.translatable(base + (whole == 1 ? "one" : "whole"), whole);
        if (whole == 0) return Component.translatable(base + "chance", extra);
        return Component.translatable(base + "whole_and_chance", whole, extra);
    }
}
