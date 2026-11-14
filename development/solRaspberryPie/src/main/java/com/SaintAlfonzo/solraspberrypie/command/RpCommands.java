package com.SaintAlfonzo.solraspberrypie.command;

import com.SaintAlfonzo.solraspberrypie.data.RpData;
import com.SaintAlfonzo.solraspberrypie.logic.DietEngine;
import com.SaintAlfonzo.solraspberrypie.logic.PauseRegistry;
import com.SaintAlfonzo.solraspberrypie.net.RpNetwork;
import com.SaintAlfonzo.solraspberrypie.net.S2CMessage;
import com.tarinoita.solsweetpotato.tracking.CapabilityHandler;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * /solrp pause &lt;players&gt;, /solrp resume &lt;players&gt;, /solrp pause list (12.3), /solrp reset &lt;players&gt;
 * (12.4: clears the player's whole diet: Apple Pie's food memory and counter, the unique-food count, highest
 * diversity and so every one-time unlock, slot choices and the Liquid Diet Log; pause state and books are kept). Same
 * permission level as
 * vanilla /effect. A pause stops Raspberry Pie applying anything for that player; data keeps updating; slot actions are
 * refused. It removes nothing by itself (use /effect clear after it). Resume re-evaluates at once with the gain lines.
 */
public final class RpCommands {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("solrp").requires(s -> s.hasPermission(2))
                .then(Commands.literal("pause")
                        .then(Commands.literal("list").executes(RpCommands::list))
                        .then(Commands.argument("players", EntityArgument.players()).executes(c -> setPaused(c, true))))
                .then(Commands.literal("resume")
                        .then(Commands.argument("players", EntityArgument.players()).executes(c -> setPaused(c, false))))
                .then(Commands.literal("reset")
                        .then(Commands.argument("players", EntityArgument.players()).executes(RpCommands::reset))));
    }

    private static int setPaused(CommandContext<CommandSourceStack> c, boolean pause) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(c, "players");
        int n = 0;
        for (ServerPlayer p : players) {
            RpData data = RpData.get(p);
            if (data.paused == pause) continue;
            data.paused = pause;
            PauseRegistry.get(p.server).set(p, pause);
            if (!pause) {
                // Resume: everything active announces as gained again, applied from current state.
                data.activeKeys.clear();
                DietEngine.evaluate(p, true);
                DietEngine.reapplyAll(p);
            } else {
                DietEngine.sync(p);
            }
            n++;
        }
        int count = n;
        c.getSource().sendSuccess(Component.translatable(pause ? "solraspberrypie.cmd.paused" : "solraspberrypie.cmd.resumed", count), true);
        return count;
    }

    private static int reset(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(c, "players");
        for (ServerPlayer p : players) {
            FoodList fl = FoodList.get(p);
            fl.clearFood();
            fl.resetFoodsEaten();
            RpData data = RpData.get(p);
            data.maxDiversity = 0;
            data.slots.clear();
            data.drinks.clear();
            data.regenAnchor = -1;
            data.stamp++;
            CapabilityHandler.syncFoodList(p);
            RpNetwork.toPlayer(p, S2CMessage.clear());
            DietEngine.evaluate(p, false);
            p.sendSystemMessage(Component.translatable("solraspberrypie.msg.reset"));
        }
        c.getSource().sendSuccess(Component.translatable("solraspberrypie.cmd.reset", players.size()), true);
        return players.size();
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        Map<UUID, String> all = PauseRegistry.get(c.getSource().getServer()).all();
        if (all.isEmpty()) {
            c.getSource().sendSuccess(Component.translatable("solraspberrypie.cmd.none_paused"), false);
            return 0;
        }
        c.getSource().sendSuccess(Component.translatable("solraspberrypie.cmd.paused_list", all.size(), String.join(", ", all.values())), false);
        return all.size();
    }

    private RpCommands() {}
}
