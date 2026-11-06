package com.sammy.minersdelight.registry;

import com.sammy.minersdelight.MinersDelightMod;
import com.sammy.minersdelight.content.block.copper_pot.CopyMealFunction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * The loot type the upstream data names: the pot's meal copy. Upstream's loot modifiers (add_items, replace_items) and
 * their block tag condition went with the modifier files (creativedelight members #51): the pack's loot rules give
 * those drops.
 */
public final class MDLoot {
    public static final DeferredRegister<LootItemFunctionType> FUNCTIONS = DeferredRegister.create(Registry.LOOT_FUNCTION_REGISTRY, MinersDelightMod.MODID);

    public static final RegistryObject<LootItemFunctionType> COPY_MEAL = FUNCTIONS.register("copy_meal",
            () -> new LootItemFunctionType(new CopyMealFunction.Serializer()));

    private MDLoot() {
    }
}
