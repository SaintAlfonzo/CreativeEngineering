package com.SaintAlfonzo.baby_mobs;

import com.SaintAlfonzo.baby_mobs.age.BabyAges;
import com.SaintAlfonzo.baby_mobs.client.ClientSetup;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Baby Mobs: the 1.21.2 baby squid, glow squid and dolphin, with their 26.1 "Tiny Takeover" models, on 1.19.2
 * (feature_writeup/baby_mobs.md).
 * <ul>
 *   <li>{@link BabyAges}: the age state (synced baby flag + server age, saved as {@code Age}) that mixins give the
 *       three vanilla classes: a share of natural spawns are babies ({@link BMConfig}), babies grow up after
 *       {@code growTicks}, dolphins also age up when fed fish, babies have smaller hitboxes, higher-pitched sounds
 *       (vanilla keys those off {@code isBaby()}), drop nothing and give no experience, baby dolphins don't attack.</li>
 *   <li>client/: renderers that replace vanilla's for the three mobs and swap in the 26.1 baby models and textures
 *       while the mob is young.</li>
 * </ul>
 * Nothing new is registered (no entity types, items or blocks): a world without this mod keeps its squid and
 * dolphins, grown up.
 */
@Mod(BabyMobs.ID)
public class BabyMobs {

    public static final String ID = "baby_mobs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BabyMobs() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BMConfig.SPEC);
        BabyAges.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSetup.register(modBus));
    }
}
