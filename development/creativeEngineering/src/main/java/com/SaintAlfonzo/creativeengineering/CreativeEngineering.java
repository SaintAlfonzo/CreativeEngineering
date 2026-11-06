package com.SaintAlfonzo.creativeengineering;

import com.SaintAlfonzo.creativecommon.util.BurnBursts;
import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativeengineering.bucket.WoodenBucketItems;
import com.SaintAlfonzo.creativeengineering.compat.IECompat;
import com.SaintAlfonzo.creativeengineering.fan.DragonBreathingFanProcessingType;
import com.SaintAlfonzo.creativeengineering.fan.DragonBreathingRecipeType;
import com.SaintAlfonzo.creativeengineering.fluid.FluidBridge;
import com.SaintAlfonzo.creativeengineering.items.CreativeEngineeringItems;
import com.SaintAlfonzo.creativeengineering.shrapnel.CreativeEngineeringEntities;
import com.SaintAlfonzo.creativeengineering.shrapnel.ShrapnelSpawner;
import com.SaintAlfonzo.creativeengineering.shrapnel.TntShrapnelHandler;
import com.SaintAlfonzo.creativeengineering.surface.CamoSpeedTooltip;
import com.SaintAlfonzo.creativeengineering.surface.LeafPileSlow;
import com.SaintAlfonzo.creativeengineering.surface.SurfaceSpeeds;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Creative Engineering (feature_writeup/creative_engineering.md): the pack's machines and engineering content, carved
 * out of patcherMod in the Creative Split wave 8 (2026-11-04, creative_split.md 7.6 and 7.13). Create's dragon
 * breathing fan type, the IE sawmill blade routing and sawdust, the wooden bucket, TNT shrapnel, Ore Excavation's undo
 * rules, BlockRunner surfaces (tier speeds, sticky facing, camouflage, visual sinking), the liquid XP bridge, the IE and
 * Immersive Posts tweaks, and the engineering scripts (components, metals, wood, machines, the former Creative
 * Computers rows). Requires Creative Common (the shared helpers, the startup loader, the Common APIs) and Creative
 * Compat.
 *
 * <p>This jar took over the mod id {@code creativeengineering} and {@code config/creativeengineering-common.toml} at
 * the swap (creative_split.md 6 Q1); what was left of patcherMod became Creative Common ({@code creativecommon}). Every
 * id keeps the {@code creativeengineering} namespace, which is now this jar's own. The registrations below are the
 * ones patcherMod's constructor and setup made, in the same order.
 */
@Mod(CreativeEngineering.MODID)
public class CreativeEngineering {

    public static final String MODID = "creativeengineering";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CreativeEngineering() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        // Must be registered during construction (Forge reads COMMON configs right after every constructor).
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CEConfig.COMMON_SPEC, MODID + "-common.toml");

        // Precision sawblade and creosote-soaked sawdust (item/), the shrapnel entity, the dragon breathing recipe
        // type and serializer, the wooden bucket (water only).
        CreativeEngineeringItems.register(modEventBus);
        CreativeEngineeringEntities.register(modEventBus);
        DragonBreathingRecipeType.register(modEventBus);
        WoodenBucketItems.register(modEventBus);

        // IE: arc recycling of springs, prefixToIngotMap override (onServerAboutToStart).
        MinecraftForge.EVENT_BUS.register(IECompat.class);

        // TNT shrapnel on primed-TNT-entity detonation; the same burst for Creative Compat's burn-explosion rule
        // (itemphysic.ItemBurnTransform), through Common's util.BurnBursts. Tuning: CEConfig.Shrapnel.
        MinecraftForge.EVENT_BUS.register(TntShrapnelHandler.class);
        BurnBursts.register(ShrapnelSpawner::burst);

        // Framed / copycat blocks: say on BlockRunner's speed tooltip line whether they walk like their camo.
        MinecraftForge.EVENT_BUS.register(CamoSpeedTooltip.class);
        // BlockRunner tier speeds come from [surface.speed] (mixin/BlockRunnerSpeedConfigMixin): re-apply them when
        // the toml is saved, and log an error if the mixin never ran.
        modEventBus.addListener(SurfaceSpeeds::onConfigReload);
        MinecraftForge.EVENT_BUS.register(SurfaceSpeeds.class);

        // The WTHIT Run Speed line reads how many leaf pile layers slow walking from Creative Compat's
        // [leaf_piles] slowFromLayers (the rule itself is Compat's LeafPileSlowMixin). Compat set this hook while
        // surface/ was patcherMod's; Engineering requires Compat, so it reads the value downward now.
        LeafPileSlow.set(() -> CompatConfig.SPEC.isLoaded() ? CompatConfig.LEAF_PILES.slowFromLayers.get() : LeafPileSlow.DEFAULT);

        LOGGER.info("[creativeengineering] loaded (config/{}-common.toml)", MODID);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        IECompat.register();

        // SawmillProcessMixin applies lazily on the first sawmill tick (minutes into a session). ModLauncher's Mixin
        // bytecode provider cannot locate classes from this jar that have never been loaded when it does a late
        // transform, so the helper and duck classes the mixin references are defined up front here, or the apply
        // throws ClassMetadataNotFoundException and crashes the game.
        forceLoad("com.SaintAlfonzo.creativeengineering.sawmill.SawbladeRouting");
        forceLoad("com.SaintAlfonzo.creativeengineering.sawmill.duck.SawmillProcessExt");
        forceLoad("com.SaintAlfonzo.creativeengineering.sawmill.duck.SawmillRecipeExt");

        event.enqueueWork(FluidBridge::logStartupDiagnostic);
        event.enqueueWork(DragonBreathingFanProcessingType::register);
        if (ModList.get().isLoaded("immersiveengineering")) {
            event.enqueueWork(IECompat::registerHammerNoRotate);
        }
    }

    /** Define a class now (with linking) so it exists before any late Mixin transform needs it. */
    private static void forceLoad(String className) {
        try {
            Class.forName(className, true, CreativeEngineering.class.getClassLoader());
        } catch (Throwable t) {
            LOGGER.error("Failed to pre-load {} - sawmill blade routing may crash on use", className, t);
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(CreativeEngineeringEntities.SHRAPNEL.get(),
                    context -> new ThrownItemRenderer<>(context, 1.0F, false));
        }
    }
}
