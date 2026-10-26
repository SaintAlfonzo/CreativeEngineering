package com.SaintAlfonzo.creativedelight.rustic.registry;

import com.SaintAlfonzo.creativedelight.rustic.RusticDelightMod;
import com.SaintAlfonzo.creativedelight.rustic.logic.ChanceFilter;
import com.SaintAlfonzo.creativedelight.rustic.logic.SquidCalamariModifier;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The data types: the squid loot modifier and and the patch placement filter upstream data names. */
public final class RDLoot {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, RusticDelightMod.MODID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENTS =
            DeferredRegister.create(Registry.PLACEMENT_MODIFIER_REGISTRY, RusticDelightMod.MODID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> SQUID_CALAMARI = MODIFIERS.register("squids_drop_calamari_modifier",
            SquidCalamariModifier.CODEC);
    public static final RegistryObject<PlacementModifierType<ChanceFilter>> CHANCE_FILTER = PLACEMENTS.register("configurable_rarity_filter",
            () -> () -> ChanceFilter.CODEC);

    private RDLoot() {
    }
}
