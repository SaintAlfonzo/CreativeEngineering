package com.sammy.minersdelight.registry;

import com.mojang.serialization.Codec;
import com.sammy.minersdelight.MinersDelightMod;
import com.sammy.minersdelight.content.block.copper_pot.CopyMealFunction;
import com.sammy.minersdelight.loot.AddItemsModifier;
import com.sammy.minersdelight.loot.BlockTagCondition;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The loot types the upstream data names: two loot modifiers, a block tag condition and the pot's meal copy. */
public final class MDLoot {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MinersDelightMod.MODID);
    public static final DeferredRegister<LootItemConditionType> CONDITIONS = DeferredRegister.create(Registry.LOOT_ITEM_REGISTRY, MinersDelightMod.MODID);
    public static final DeferredRegister<LootItemFunctionType> FUNCTIONS = DeferredRegister.create(Registry.LOOT_FUNCTION_REGISTRY, MinersDelightMod.MODID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_ITEMS = MODIFIERS.register("add_items", AddItemsModifier.ADD);
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> REPLACE_ITEMS = MODIFIERS.register("replace_items", AddItemsModifier.REPLACE);
    public static final RegistryObject<LootItemConditionType> BLOCK_TAG = CONDITIONS.register("block_tag",
            () -> new LootItemConditionType(new BlockTagCondition.JsonSerializer()));
    public static final RegistryObject<LootItemFunctionType> COPY_MEAL = FUNCTIONS.register("copy_meal",
            () -> new LootItemFunctionType(new CopyMealFunction.Serializer()));

    private MDLoot() {
    }
}
