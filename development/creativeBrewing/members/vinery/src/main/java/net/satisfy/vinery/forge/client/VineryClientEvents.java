package net.satisfy.vinery.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.satisfy.vinery.client.event.DoubleJumpHandler;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.registry.MobEffectRegistry;
import net.satisfy.vinery.core.wine.VintageClock;
import net.satisfy.vinery.forge.upstream.BottleLooks;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;

/** Forge-bus client events: double jump, the trippy shader (D3), the vintage clock on logout, /dumpwinetextures. */
@Mod.EventBusSubscriber(modid = Vinery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class VineryClientEvents {
    /** 1.3.10's trippy shader list (vanilla post shaders). */
    private static final String[] TRIPPY_SHADERS = {"notch", "fxaa", "art", "bumpy", "blobs2", "pencil", "color_convolve",
            "deconverge", "flip", "invert", "ntsc", "outline", "phosphor", "scan_pincushion", "sobel", "bits", "desaturate",
            "green", "blur", "wobble", "blobs", "antialias", "creeper", "spider"};
    private static boolean trippyActive;
    private static Method loadEffect;

    private VineryClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        DoubleJumpHandler.tick(minecraft);
        trippyTick(minecraft);
    }

    /** Port of 1.3.10's trippy effect: a random vanilla post shader while the effect lasts, in the game renderer's own slot. */
    private static void trippyTick(Minecraft minecraft) {
        boolean has = minecraft.player != null && minecraft.player.hasEffect(MobEffectRegistry.get(MobEffectRegistry.TRIPPY));
        if (has == trippyActive) {
            return;
        }
        trippyActive = has;
        if (has) {
            try {
                if (loadEffect == null) {
                    loadEffect = ObfuscationReflectionHelper.findMethod(net.minecraft.client.renderer.GameRenderer.class, "m_109128_", ResourceLocation.class);
                }
                String shader = TRIPPY_SHADERS[RandomSource.create().nextInt(TRIPPY_SHADERS.length)];
                loadEffect.invoke(minecraft.gameRenderer, new ResourceLocation("shaders/post/" + shader + ".json"));
            } catch (ReflectiveOperationException | RuntimeException e) {
                Vinery.LOGGER.warn("Couldn't load the trippy shader", e);
            }
        } else {
            minecraft.gameRenderer.shutdownEffect();
        }
    }

    /** alcohol.md 10.17: every wine texture (upstream and the port's recolours) to kubejs/exported/wine_textures/. */
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dumpwinetextures").executes(context -> {
            Path dir = FMLPaths.GAMEDIR.get().resolve("kubejs/exported/wine_textures");
            try {
                int count = BottleLooks.dump(dir);
                context.getSource().sendSuccess(Component.literal(count + " wine textures written to " + dir), false);
                return count;
            } catch (IOException e) {
                Vinery.LOGGER.warn("Couldn't dump the wine textures", e);
                context.getSource().sendFailure(Component.literal("Couldn't write " + dir + ": " + e.getMessage()));
                return 0;
            }
        }));
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        VintageClock.clearClient();
        trippyActive = false;
    }
}
