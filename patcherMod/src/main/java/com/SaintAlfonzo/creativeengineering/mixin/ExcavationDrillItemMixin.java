package com.SaintAlfonzo.creativeengineering.mixin;

import com.simibubi.create.foundation.config.ConfigBase;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.simple.SimpleChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import uwu.lopyluna.create_dd.content.items.equipment.excavation_drill.ExcavationDrillItem;

/**
 * Create: Dreams &amp; Desires (1.2c Beta Mid-Dev) crashes dedicated servers from its Excavation
 * Drill. {@code onBlockDestroyed} is a common {@code BlockEvent.BreakEvent} listener, and for
 * <em>every</em> block any real player breaks (before it even checks the held item) it does:
 * <pre>
 *   DesiresPackets.getChannel().sendToServer(new InvertFunctionPacket());
 *   boolean inverted = DesiresConfigs.client().invertExcavationDrillFunction.get();
 * </pre>
 * {@code SimpleChannel#sendToServer} loads {@code net.minecraft.client.Minecraft} (RuntimeDistCleaner
 * throws on a dedicated server), and the CLIENT config spec is never loaded there either.
 * {@code destroyVein} repeats the same two lines. Singleplayer only works because the
 * integrated server shares the client's classes + config.
 *
 * <p>On a dedicated server both calls are neutralised: the packet send is dropped (its server
 * handler just reads the config and throws the value away anyway) and the invert flag reads as
 * the config's default {@code false}, so the drill bores/vein-mines unless sneaking. Singleplayer
 * / LAN hosts keep the original behaviour.
 *
 * <p>D&amp;D classes are unobfuscated; this project ships no Mixin refmap, so {@code remap = false}.
 * {@code require} makes a mod update that moves these call sites fail loudly instead of silently.
 */
@Mixin(value = ExcavationDrillItem.class, remap = false)
public class ExcavationDrillItemMixin {

    @Redirect(method = {"onBlockDestroyed", "destroyVein"}, remap = false, require = 2,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/minecraftforge/network/simple/SimpleChannel;sendToServer(Ljava/lang/Object;)V"))
    private static void creativeengineering$skipSendOnServer(SimpleChannel channel, Object message) {
        if (FMLEnvironment.dist.isClient()) {
            channel.sendToServer(message);
        }
    }

    @Redirect(method = {"onBlockDestroyed", "destroyVein"}, remap = false, require = 2,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lcom/simibubi/create/foundation/config/ConfigBase$ConfigBool;get()Ljava/lang/Object;"))
    private static Object creativeengineering$defaultInvertOnServer(ConfigBase.ConfigBool config) {
        return FMLEnvironment.dist.isClient() ? config.get() : Boolean.FALSE;
    }
}
