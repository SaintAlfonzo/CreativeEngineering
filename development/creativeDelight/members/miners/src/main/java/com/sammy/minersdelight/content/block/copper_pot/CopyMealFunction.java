package com.sammy.minersdelight.content.block.copper_pot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.sammy.minersdelight.registry.MDLoot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/** Loot function {@code miners_delight:copy_meal}: a broken pot keeps its meal (the upstream block loot table uses it). */
public class CopyMealFunction extends LootItemConditionalFunction {
    private CopyMealFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (context.getParamOrNull(LootContextParams.BLOCK_ENTITY) instanceof CopperPotBlockEntity pot) {
            CompoundTag meal = pot.writeMeal(new CompoundTag());
            if (!meal.isEmpty()) {
                stack.addTagElement("BlockEntityTag", meal);
            }
        }
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return MDLoot.COPY_MEAL.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyMealFunction> {
        @Override
        public CopyMealFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new CopyMealFunction(conditions);
        }
    }
}
