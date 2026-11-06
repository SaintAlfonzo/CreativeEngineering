package com.SaintAlfonzo.creativeengineering.mixin;

import com.simibubi.create.foundation.config.ConfigBase;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.simple.SimpleChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import uwu.lopyluna.create_dd.content.items.equipment.deforester_saw.DeforesterSawItem;

/**
 * Same dedicated-server crash as {@link ExcavationDrillItemMixin}, in D&amp;D's Deforester Saw:
 * {@code destroyTree} (run server-side when a player breaks a log with the saw) calls
 * {@code SimpleChannel#sendToServer} and reads the CLIENT-only {@code invertDeforesterSawFunction}
 * config. On a dedicated server the send is dropped and the flag reads as its default
 * {@code false} (fells whole trees unless sneaking); singleplayer / LAN hosts are untouched.
 */
@Mixin(value = DeforesterSawItem.class, remap = false)
public class DeforesterSawItemMixin {

    @Redirect(method = "destroyTree", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/minecraftforge/network/simple/SimpleChannel;sendToServer(Ljava/lang/Object;)V"))
    private static void creativeengineering$skipSendOnServer(SimpleChannel channel, Object message) {
        if (FMLEnvironment.dist.isClient()) {
            channel.sendToServer(message);
        }
    }

    @Redirect(method = "destroyTree", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lcom/simibubi/create/foundation/config/ConfigBase$ConfigBool;get()Ljava/lang/Object;"))
    private static Object creativeengineering$defaultInvertOnServer(ConfigBase.ConfigBool config) {
        return FMLEnvironment.dist.isClient() ? config.get() : Boolean.FALSE;
    }
}
