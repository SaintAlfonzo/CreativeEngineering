package net.ribs.vintagedelight.worldgen.tree;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.ribs.vintagedelight.VintageDelight;

// 1.19.2 has no Forge registry for trunk placers: a DeferredRegister on the vanilla registry key.
public class ModTrunkPlacerTypes {
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACER = DeferredRegister.create(Registry.TRUNK_PLACER_TYPE_REGISTRY, VintageDelight.MODID);
    public static final RegistryObject<TrunkPlacerType<VineTrunkPlacer>> VINE_TRUNK_PLACER = TRUNK_PLACER.register("vine_trunk_placer",
        () -> new TrunkPlacerType<>(VineTrunkPlacer.CODEC));

    public static void register(IEventBus eventBus) {
        TRUNK_PLACER.register(eventBus);
    }
}
