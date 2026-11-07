package com.SaintAlfonzo.creativeengineering;

import com.SaintAlfonzo.creativeengineering.compat.IECompat;
import com.SaintAlfonzo.creativeengineering.fan.DragonBreathingFanProcessingType;
import com.SaintAlfonzo.creativeengineering.fan.DragonBreathingRecipeType;
import com.SaintAlfonzo.creativeengineering.items.CreativeEngineeringItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Creative Engineering (feature_writeup/creative_engineering.md): the pack's machines and engineering content, carved
 * out of patcherMod in the Creative Split wave 8 (2026-11-04, creative_split.md 7.6 and 7.13). Create's dragon
 * breathing fan type, the IE sawmill blade routing and sawdust, the IE and Immersive Posts tweaks, and the engineering
 * scripts (components, metals, wood, machines, the former Creative Computers rows). Requires Creative Common (the
 * shared helpers, the startup loader, the Common APIs) and Creative Compat.
 *
 * <p>On 2026-11-06 the non-engineering features moved out with their ids unchanged: TNT shrapnel, Ore Excavation's
 * undo rules and BlockRunner surfaces to Creative Compat (with [shrapnel], [surface] and [vein_undo], so this jar has
 * no config file any more), the liquid XP bridge to Creative Magic, the wooden bucket to Creative Equipment.
 *
 * <p>This jar took over the mod id {@code creativeengineering} at the swap (creative_split.md 6 Q1); what was left of
 * patcherMod became Creative Common ({@code creativecommon}). Every id keeps the {@code creativeengineering}
 * namespace, which is now this jar's own.
 */
@Mod(CreativeEngineering.MODID)
public class CreativeEngineering {

    public static final String MODID = "creativeengineering";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CreativeEngineering() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        // Precision sawblade and creosote-soaked sawdust (item/), the dragon breathing recipe type and serializer.
        CreativeEngineeringItems.register(modEventBus);
        DragonBreathingRecipeType.register(modEventBus);

        // IE: arc recycling of springs, prefixToIngotMap override (onServerAboutToStart).
        MinecraftForge.EVENT_BUS.register(IECompat.class);

        LOGGER.info("[creativeengineering] loaded");
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
}
