package com.SaintAlfonzo.solraspberrypie.command;

import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.SaintAlfonzo.solraspberrypie.data.DietAdjustment;
import com.SaintAlfonzo.solraspberrypie.data.GateFoods;
import com.SaintAlfonzo.solraspberrypie.data.MealLog;
import com.SaintAlfonzo.solraspberrypie.data.RpData;
import com.SaintAlfonzo.solraspberrypie.data.SlotChoice;
import com.SaintAlfonzo.solraspberrypie.ladder.Ladder;
import com.SaintAlfonzo.solraspberrypie.ladder.LadderLoader;
import com.SaintAlfonzo.solraspberrypie.mixin.FoodListAccessor;
import com.SaintAlfonzo.solraspberrypie.net.S2CLadder;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.mojang.brigadier.arguments.DoubleArgumentType;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * /solrp pause &lt;players&gt;, /solrp resume &lt;players&gt;, /solrp pause list (12.3), /solrp reset &lt;players&gt;
 * (12.4: clears the player's whole diet: Apple Pie's food memory and counter, the unique-food count, highest
 * diversity and so every one-time unlock, slot choices and the Liquid Diet Log; pause state and books are kept; with no
 * player it resets the player running it). Same
 * permission level as
 * vanilla /effect. /solrp diversity set &lt;players&gt; &lt;value&gt; clears the player's food window and puts one
 * "Adjustment" entry in it worth that much diversity, so current and highest diversity both become the value (highest
 * can go down: unlocks above it are lost, slot choices drop to what is still unlocked); the entry ages out like a meal.
 * /solrp diversity get &lt;player&gt;. /solrp ladder reset moves the world's ladder copy aside and seeds a fresh one
 * from defaultconfigs (or the jar default), then re-sends it and re-evaluates every player online. A pause stops Raspberry Pie applying anything for that player; data keeps updating; slot actions are
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
						.executes(c -> reset(c, java.util.List.of(c.getSource().getPlayerOrException())))
						.then(Commands.argument("players", EntityArgument.players()).executes(c -> reset(c, EntityArgument.getPlayers(c, "players")))))
				.then(Commands.literal("diversity")
						.then(Commands.literal("set")
								.then(Commands.argument("players", EntityArgument.players())
										.then(Commands.argument("value", DoubleArgumentType.doubleArg(0, 10000))
												.executes(c -> setDiversity(c, EntityArgument.getPlayers(c, "players"), DoubleArgumentType.getDouble(c, "value"))))))
						.then(Commands.literal("get")
								.then(Commands.argument("player", EntityArgument.player()).executes(RpCommands::getDiversity))))
				.then(Commands.literal("ladder")
						.then(Commands.literal("reset").executes(RpCommands::resetLadder))));
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

	/** {@code /solrp reset} with no player resets the player running it. */
	private static int reset(CommandContext<CommandSourceStack> c, Collection<ServerPlayer> players) {
		for (ServerPlayer p : players) {
			FoodList fl = FoodList.get(p);
			fl.clearFood();
			fl.resetFoodsEaten();
			RpData data = RpData.get(p);
			data.maxDiversity = 0;
			data.gateOpened = false;
			data.slots.clear();
			data.clearDrinks();
			data.toggles.clear();
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

	/** {@code /solrp diversity set}: the window holds only the Adjustment entry, worth {@code value} now. */
	private static int setDiversity(CommandContext<CommandSourceStack> c, Collection<ServerPlayer> players, double value) {
		Ladder ladder = Ladder.current;
		for (ServerPlayer p : players) {
			FoodList fl = FoodList.get(p);
			fl.clearFood();
			if (value > 0) {
				FoodInstance entry = new FoodInstance(SolRaspberryPie.ADJUSTMENT.get());
				DietAdjustment adj = (DietAdjustment) (Object) entry;
				adj.solrp$setValue(1.0);
				double unit = FoodList.calculateDiversityContribution(entry, 0); // Apple Pie's newest-meal weight
				adj.solrp$setValue(unit > 0 ? value / unit : value);
				((FoodListAccessor) (Object) fl).solrp$uniqueFoods().put(entry, 0);
				// Rounding must not leave the sum a hair under the value (a milestone at exactly the value would be missed).
				for (int i = 0; i < 16 && fl.foodDiversity() < value; i++) adj.solrp$setValue(Math.nextUp(adj.solrp$value()));
				((MealLog) (Object) fl).solrp$meals().add(0, SolRaspberryPie.ADJUSTMENT_ID);
			}
			RpData data = RpData.get(p);
			data.maxDiversity = Math.min(data.maxDiversity, value);
			for (int i = 0; i < data.slots.size(); i++) {
				SlotChoice choice = data.slots.get(i);
				if (choice == null) continue;
				int unlocked = ladder.unlockedLevel(choice.effect(), data.maxDiversity);
				if (unlocked < 1) data.slots.set(i, null);
				else if (choice.level() > unlocked) data.slots.set(i, new SlotChoice(choice.effect(), unlocked));
			}
			data.stamp++;
			CapabilityHandler.syncFoodList(p);
			DietEngine.evaluate(p, true);
			p.sendSystemMessage(Component.translatable("solraspberrypie.msg.diversity_set", fmt(value)));
			int missing = SOLSweetPotatoConfig.minFoodsToActivate() - GateFoods.count(fl);
			if (missing > 0) {
				c.getSource().sendSuccess(Component.translatable("solraspberrypie.cmd.diversity_gate", p.getDisplayName(), fmt(value), missing), false);
			}
		}
		c.getSource().sendSuccess(Component.translatable("solraspberrypie.cmd.diversity_set", players.size(), fmt(value)), true);
		return players.size();
	}

	private static int getDiversity(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
		ServerPlayer p = EntityArgument.getPlayer(c, "player");
		double now = FoodList.get(p).foodDiversity();
		c.getSource().sendSuccess(Component.translatable("solraspberrypie.cmd.diversity_get", p.getDisplayName(), fmt(now), fmt(RpData.get(p).maxDiversity)), false);
		return (int) Math.floor(now);
	}

	/** Two decimals, rounded down, as the Diet Log prints diversity (food-tuning #28). */
	private static String fmt(double v) {
		return String.format(java.util.Locale.ROOT, "%.2f", Math.floor(v * 100 + 1e-9) / 100);
	}

	/** {@code /solrp ladder reset}: the world's copy is moved aside, a fresh one seeded and loaded, everyone re-synced. */
	private static int resetLadder(CommandContext<CommandSourceStack> c) {
		MinecraftServer server = c.getSource().getServer();
		LadderLoader.Reset r;
		try {
			r = LadderLoader.reset(server);
		} catch (Exception e) {
			c.getSource().sendFailure(Component.translatable("solraspberrypie.cmd.ladder_reset_failed", e.toString()));
			return 0;
		}
		Ladder.current = r.ladder();
		for (ServerPlayer p : server.getPlayerList().getPlayers()) {
			RpNetwork.toPlayer(p, new S2CLadder(Ladder.current));
			DietEngine.forgetApplied(RpData.get(p));
			DietEngine.evaluate(p, false);
		}
		c.getSource().sendSuccess(Component.translatable("solraspberrypie.cmd.ladder_reset", r.source(), r.backup(),
				r.ladder().rungs.size(), r.ladder().slotCount(), r.ladder().pool.size()), true);
		return r.ladder().rungs.size();
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
