package net.ribs.vintagedelight.worldgen.tree;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.ribs.vintagedelight.VintageDelight;

// From KOTS 0.3.1: the magic vine tree's switchable ground decorator.
public final class ModTreeDecoratorTypes {
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATOR_TYPES = DeferredRegister.create(ForgeRegistries.TREE_DECORATOR_TYPES, VintageDelight.MODID);
    public static final RegistryObject<TreeDecoratorType<ConfigurableAlterGroundDecorator>> CONFIGURABLE_ALTER_GROUND = TREE_DECORATOR_TYPES.register(
        "configurable_alter_ground", () -> new TreeDecoratorType<>(ConfigurableAlterGroundDecorator.CODEC));

    private ModTreeDecoratorTypes() {
    }

    public static void register(IEventBus eventBus) {
        TREE_DECORATOR_TYPES.register(eventBus);
    }
}
