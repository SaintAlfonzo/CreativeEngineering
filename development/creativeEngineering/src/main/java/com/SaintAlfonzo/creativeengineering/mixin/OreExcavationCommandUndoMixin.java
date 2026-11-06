package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.excavation.VeinUndo;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import oreexcavation.core.CommandUndo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Puts {@code excavation/VeinUndo}'s cooldown and denied-block rules in front of Ore Excavation's
 * {@code /undo_excavation}. Ore Excavation's command throws on every failure, so a normal return is a success
 * and starts the cooldown. Unobfuscated target, no refmap: {@code remap = false}.
 */
@Mixin(value = CommandUndo.class, remap = false)
public class OreExcavationCommandUndoMixin {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativeengineering$veinUndoRules(CommandContext<CommandSourceStack> context,
                                                          CallbackInfoReturnable<Integer> cir) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return;
        Component refusal = VeinUndo.refusal(player);
        if (refusal != null) {
            context.getSource().sendFailure(refusal);
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "execute", at = @At("RETURN"), remap = false)
    private static void creativeengineering$startCooldown(CommandContext<CommandSourceStack> context,
                                                          CallbackInfoReturnable<Integer> cir) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) VeinUndo.startCooldown(player);
    }
}
