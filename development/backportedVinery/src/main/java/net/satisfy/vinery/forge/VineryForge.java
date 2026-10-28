package net.satisfy.vinery.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.registry.EntityTypeRegistry;
import net.satisfy.vinery.core.registry.MenuTypeRegistry;
import net.satisfy.vinery.core.registry.MobEffectRegistry;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.registry.RecipeTypeRegistry;
import net.satisfy.vinery.core.registry.SoundEventRegistry;
import net.satisfy.vinery.core.registry.ArmorSetRegistry;
import net.satisfy.vinery.core.world.feature.VineryFeatures;
import net.satisfy.vinery.forge.network.VineryNetwork;
import net.satisfy.vinery.forge.upstream.UpstreamJars;
import net.satisfy.vinery.forge.shared.BuiltInPacks;
import net.satisfy.vinery.forge.upstream.UpstreamTranslator;
import net.minecraftforge.event.AddPackFindersEvent;

/** Forge entry of the Vinery port: config, registries, events. */
@Mod(Vinery.MOD_ID)
public class VineryForge {
    public VineryForge() {
        // D13 B: the upstream jars carry the art and data; without them nothing would render or craft
        UpstreamJars.verifyOrFail();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, VineryConfig.SPEC, "vinery-common.toml");
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        Vinery.init();
        ObjectRegistry.BLOCKS.register(bus);
        ObjectRegistry.ITEMS.register(bus);
        EntityTypeRegistry.BLOCK_ENTITY_TYPES.register(bus);
        EntityTypeRegistry.ENTITY_TYPES.register(bus);
        MenuTypeRegistry.MENU_TYPES.register(bus);
        MobEffectRegistry.EFFECTS.register(bus);
        RecipeTypeRegistry.RECIPE_TYPES.register(bus);
        RecipeTypeRegistry.RECIPE_SERIALIZERS.register(bus);
        SoundEventRegistry.SOUND_EVENTS.register(bus);
        VineryFeatures.FEATURES.register(bus);
        VineryVillagers.POI_TYPES.register(bus);
        VineryVillagers.PROFESSIONS.register(bus);
        ArmorSetRegistry.init();
        VineryNetwork.init();

        bus.addListener(EntityTypeRegistry::registerAttributes);
        // Upstream art and data as built-in packs, always on and at the bottom: resource packs, KubeJS and datapacks
        // still override them (alcohol.md B4.8).
        bus.addListener((AddPackFindersEvent event) -> BuiltInPacks.addUpstream(event, "vinery_upstream",
                "Let's Do Vinery: art and data read from the upstream jars", UpstreamTranslator::assets, UpstreamTranslator::data));
        bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(Vinery::commonSetup));

        VineryCommonEvents.register(MinecraftForge.EVENT_BUS);
    }
}
