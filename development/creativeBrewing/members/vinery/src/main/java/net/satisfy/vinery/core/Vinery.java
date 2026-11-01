package net.satisfy.vinery.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.satisfy.foundation.storage.StorageBlockEntity;
import net.satisfy.vinery.api.VineryPlugin;
import net.satisfy.vinery.core.registry.GrapeTypeRegistry;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.wine.WineYears;
import net.satisfy.vinery.platform.PlatformHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Port: upstream's common entry. The registries are Forge DeferredRegisters, hooked up in
 * {@code net.satisfy.vinery.forge.VineryForge}; what is left here runs in common setup.
 */
public class Vinery {
    public static final String MOD_ID = "vinery";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Before the registries fire: plugin grape types and juices, then the grape type list is frozen. */
    public static void init() {
        for (VineryPlugin plugin : PlatformHelper.getPlugins()) {
            plugin.register();
        }
        GrapeTypeRegistry.freeze();
        StorageBlockEntity.addListener(new StorageBlockEntity.Listener() {
            @Override
            public void onInsert(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
                if (stack.getItem() instanceof net.satisfy.vinery.core.item.DrinkBlockItem) {
                    WineYears.startStorage(stack, level, pos);
                }
            }

            @Override
            public void onRemove(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.Level level) {
                if (stack.getItem() instanceof net.satisfy.vinery.core.item.DrinkBlockItem) {
                    WineYears.stopStorage(stack, level);
                }
            }
        });
    }

    public static ResourceLocation identifier(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    /** Common setup (enqueueWork): vanilla tables that aren't registries. */
    public static void commonSetup() {
        CommonSetup.init();
        GrapeTypeRegistry.addGrapeAttributes();
        VineryToolActions.register(ObjectRegistry.DARK_CHERRY_LOG.get(), ObjectRegistry.STRIPPED_DARK_CHERRY_LOG.get());
        VineryToolActions.register(ObjectRegistry.DARK_CHERRY_WOOD.get(), ObjectRegistry.STRIPPED_DARK_CHERRY_WOOD.get());
        VineryToolActions.register(ObjectRegistry.APPLE_LOG.get(), Blocks.STRIPPED_OAK_LOG);
        VineryToolActions.register(ObjectRegistry.APPLE_WOOD.get(), Blocks.STRIPPED_OAK_WOOD);
    }
}
