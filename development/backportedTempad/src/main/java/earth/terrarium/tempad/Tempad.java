// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/Tempad.kt. Translated to Java for
// Forge 1.19.2 (feature_writeup/immersive_portals.md 2.1.1): RegisterCapabilitiesEvent block -> forge/TempadCapabilities;
// TicketController -> ForgeChunkManager validation callback; the required_location_upgrade feature pack -> config
// require_location_upgrade + forge/RequiredLocationUpgradeCondition; NeoForge events -> their Forge 1.19.2 names.
// The upstream art and data come from tempad_upstream/ (forge/UpstreamJars, forge/UpstreamTranslator).
package earth.terrarium.tempad;

import earth.terrarium.tempad.client.TempadClient;
import earth.terrarium.tempad.client.TempadClientExtras;
import earth.terrarium.tempad.common.compat.CuriosCompat;
import earth.terrarium.tempad.common.config.ClientConfig;
import earth.terrarium.tempad.common.config.CommonConfig;
import earth.terrarium.tempad.common.config.CommonConfigCache;
import earth.terrarium.tempad.common.data.MetronomeData;
import earth.terrarium.tempad.common.data.TempadPlayerData;
import earth.terrarium.tempad.common.data.TravelHistoryAttachment;
import earth.terrarium.tempad.common.entity.TimedoorEntity;
import earth.terrarium.tempad.common.registries.ModApps;
import earth.terrarium.tempad.common.registries.ModBlocks;
import earth.terrarium.tempad.common.registries.ModContext;
import earth.terrarium.tempad.common.registries.ModEntities;
import earth.terrarium.tempad.common.registries.ModItems;
import earth.terrarium.tempad.common.registries.ModLocations;
import earth.terrarium.tempad.common.registries.ModMacros;
import earth.terrarium.tempad.common.registries.ModMenus;
import earth.terrarium.tempad.common.registries.ModNetworking;
import earth.terrarium.tempad.common.registries.ModRecipes;
import earth.terrarium.tempad.common.registries.ModSizing;
import earth.terrarium.tempad.common.registries.ModSounds;
import earth.terrarium.tempad.common.utils.Color;
import earth.terrarium.tempad.forge.RequiredLocationUpgradeCondition;
import earth.terrarium.tempad.forge.TempadCapabilities;
import earth.terrarium.tempad.forge.UpstreamJars;
import earth.terrarium.tempad.forge.UpstreamTranslator;
import earth.terrarium.tempad.shared.BuiltInPacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Tempad.MOD_ID)
public class Tempad {
    public static final String MOD_ID = "tempad";
    public static final Color DARK_ORANGE = new Color(0x91, 0x45, 0x0d, 255);
    public static final Color ORANGE = new Color(0xFF, 0x6a, 0, 255);
    public static final Color HIGHLIGHTED_ORANGE = new Color(0xFF, 0xcc, 0x54, 255);
    public static final Color GREEN = new Color(0xFF65fb01);

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Tempad() {
        UpstreamJars.verifyOrFail();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "tempad-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "tempad-client.toml");
        bus.addListener((ModConfigEvent.Loading event) -> onConfig(event.getConfig()));
        bus.addListener((ModConfigEvent.Reloading event) -> {
            onConfig(event.getConfig());
            // Upstream's Observable listeners push changed values to every player.
            if (event.getConfig().getSpec() == CommonConfig.SPEC && server() != null) ModNetworking.sendToAll(CommonConfigCache.createPacket());
        });

        ModApps.init();
        ModSizing.init();
        ModContext.init();
        ModEntities.SERIALIZERS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModBlocks.BLOCK_ENTITIES.register(bus);
        ModItems.REGISTRY.register(bus);
        ModMacros.init();
        ModMenus.REGISTRY.register(bus);
        ModNetworking.init();
        ModRecipes.SERIALIZERS.register(bus);
        ModRecipes.RECIPE_TYPES.register(bus);
        ModSounds.REGISTRY.register(bus);
        ModLocations.init();
        CraftingHelper.register(RequiredLocationUpgradeCondition.SERIALIZER);
        // Curios 5.1 takes slot types by IMC (upstream: data/tempad/curios/), only when Curios is installed (TP-D8).
        bus.addListener((InterModEnqueueEvent event) -> {
            if (ModList.get().isLoaded("curios")) CuriosCompat.enqueueSlots(event);
        });

        bus.addListener(TempadCapabilities::register);
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, (AttachCapabilitiesEvent<ItemStack> event) -> TempadCapabilities.attachItem(event));
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, (AttachCapabilitiesEvent<Entity> event) -> TempadCapabilities.attachEntity(event));
        MinecraftForge.EVENT_BUS.addListener(TempadCapabilities::clone);

        // Upstream registers TicketController("tempad:timedoor", no callback) and never issues tickets with it (the doors use
        // TicketType.PORTAL region tickets). Same here: the mod's forced-chunk slot, with no tickets kept across restarts.
        ForgeChunkManager.setForcedChunkLoadingCallback(MOD_ID, (level, helper) -> {
            helper.getBlockTickets().keySet().forEach(pos -> helper.removeAllTickets(pos));
            helper.getEntityTickets().keySet().forEach(id -> helper.removeAllTickets(id));
        });

        // Upstream art and data as built-in packs, always on and at the bottom (datapacks and KubeJS still override).
        bus.addListener((AddPackFindersEvent event) -> BuiltInPacks.addUpstream(event, "tempad_upstream",
                "Tempad: art and data read from the upstream jar", UpstreamTranslator::assets, UpstreamTranslator::data));

        MinecraftForge.EVENT_BUS.addListener((TickEvent.PlayerTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) return;
            if (event.player.tickCount % 1200 != 0) return;
            TempadPlayerData.get(event.player).getTravelHistory().logLocation(event.player);
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, (LivingDeathEvent event) -> {
            if (event.isCanceled() || !(event.getEntity() instanceof Player player) || player.level.isClientSide) return;
            TempadPlayerData.get(player).getTravelHistory().logLocation(player, TravelHistoryAttachment.DEATH_MARKER);
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) ->
                TempadPlayerData.get(event.getEntity()).getTravelHistory().logLocation(event.getEntity(), TravelHistoryAttachment.RESPAWN_MARKER));
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, (EntityTravelToDimensionEvent event) -> {
            if (event.isCanceled() || !(event.getEntity() instanceof Player player)) return;
            TempadPlayerData.get(player).getTravelHistory().logLocation(player, TravelHistoryAttachment.DIM_EXIT_MARKER);
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event) ->
                TempadPlayerData.get(event.getEntity()).getTravelHistory().logLocation(event.getEntity(), TravelHistoryAttachment.DIM_ENTER_MARKER));
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END) return;
            if (event.getServer().getTickCount() % CommonConfig.Metronome.generationRate() == 0) {
                MetronomeData energy = MetronomeData.get();
                if (energy != null) energy.tick();
            }
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) ModNetworking.sendToPlayer(CommonConfigCache.createPacket(), player);
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.StartTracking event) -> {
            if (event.getEntity().level.isClientSide) return;
            if (event.getTarget() instanceof TimedoorEntity door) door.setAnimationOffset(door.tickCount);
        });

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> TempadClient.init(bus));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> TempadClientExtras.init(bus));
    }

    private static void onConfig(ModConfig config) {
        if (config.getSpec() == CommonConfig.SPEC) CommonConfigCache.refreshFromConfig();
    }

    /** Upstream {@code String.tempadId}. */
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    /** Upstream {@code Tempad.server}. */
    public static MinecraftServer server() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
