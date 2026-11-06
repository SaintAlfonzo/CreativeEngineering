package com.SaintAlfonzo.creativeengineering.fluid;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

public final class FluidBridge {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation LIQUID_XP_ID =
            new ResourceLocation(
                    "create_enchantment_industry",
                    "experience"
            );

    private static boolean warnedMissing = false;
    private static boolean loggedSuccess = false;

    private FluidBridge() {
    }


    public static Fluid getTargetFluidNow() {

        Fluid fluid = ForgeRegistries.FLUIDS.getValue(LIQUID_XP_ID);

        if (fluid == null) {

            if (!warnedMissing) {
                LOGGER.warn(
                        "[XPFluidBridge] Missing fluid {}",
                        LIQUID_XP_ID
                );
                warnedMissing = true;
            }

            return null;
        }


        if (!loggedSuccess) {
            LOGGER.info(
                    "[XPFluidBridge] Found target fluid {}",
                    LIQUID_XP_ID
            );
            loggedSuccess = true;
        }


        return fluid;
    }


        private static RegistryObject<Fluid> cachedTarget;

    public static RegistryObject<Fluid> resolveOrFallback(
            RegistryObject<Fluid> fallback
    ) {
        if (getTargetFluidNow() == null) {
            return fallback;
        }

        if (cachedTarget == null) {
            cachedTarget = RegistryObject.create(LIQUID_XP_ID, ForgeRegistries.FLUIDS);
        }

        return cachedTarget;
    }


    
    public static void logStartupDiagnostic() {

        if (getTargetFluidNow() != null) {

            LOGGER.info(
                    "[XPFluidBridge] Startup OK: {}",
                    LIQUID_XP_ID
            );

        } else {

            LOGGER.warn(
                    "[XPFluidBridge] Startup FAILED: {}",
                    LIQUID_XP_ID
            );
        }
    }
}
