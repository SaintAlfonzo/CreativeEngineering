package com.SaintAlfonzo.cdevperfbot;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.function.Function;

/**
 * The {@code /pbot} test verbs (plan_scratch_testing_expansion.md TX-22; list in BotCommands' class comment). Each
 * call prints {@code [cdevtest] verb=pbot.<verb> ...} lines to the log (and to a player or RCON caller); a call that
 * fails prints {@code error=...}. Every Throwable is caught (a probe Error crashed servers before).
 */
final class BotTestVerbs {

	private static final Logger LOGGER = LogUtils.getLogger();

	private BotTestVerbs() {
	}

	@FunctionalInterface
	interface Body {
		void run(CommandContext<CommandSourceStack> c, BotPlayer bot, Out out) throws Exception;
	}

	/** Lines of one call. */
	static final class Out {
		private final CommandContext<CommandSourceStack> c;
		private final String verb;

		Out(CommandContext<CommandSourceStack> c, String verb) {
			this.c = c;
			this.verb = verb;
		}

		BotLine line() {
			return new BotLine(verb);
		}

		void emit(BotLine l) {
			String s = l.toString();
			LOGGER.info(s);
			CommandSourceStack src = c.getSource();
			if (src.getEntity() != null || !"Server".equals(src.getTextName())) src.sendSuccess(Component.literal(s), false);
		}
	}

	private static int run(CommandContext<CommandSourceStack> c, Bots bots, String verb, Body body) {
		Out out = new Out(c, verb);
		String name = StringArgumentType.getString(c, "name");
		try {
			BotPlayer bot = bots.get(name);
			if (bot == null) {
				out.emit(out.line().kv("bot", name).kv("error", "no such bot"));
				return 0;
			}
			body.run(c, bot, out);
			return 1;
		} catch (VirtualMachineError e) {
			throw e;
		} catch (Throwable t) {
			out.emit(out.line().kv("bot", name).kv("error", t.getClass().getSimpleName() + ": " + t.getMessage()));
			return 0;
		}
	}

	private static RequiredArgumentBuilder<CommandSourceStack, String> name() {
		return Commands.argument("name", StringArgumentType.word());
	}

	private static String item(ItemStack st) {
		if (st.isEmpty()) return "empty";
		String s = ForgeRegistries.ITEMS.getKey(st.getItem()) + "*" + st.getCount();
		return st.hasTag() ? s + st.getTag() : s;
	}

	private static InteractionHand hand(String h) {
		return switch (h) {
			case "main" -> InteractionHand.MAIN_HAND;
			case "off" -> InteractionHand.OFF_HAND;
			default -> throw new IllegalArgumentException("hand must be main or off");
		};
	}

	private static boolean onOff(String s) {
		return switch (s) {
			case "on", "true", "1" -> true;
			case "off", "false", "0" -> false;
			default -> throw new IllegalArgumentException("on or off");
		};
	}

	static void register(LiteralArgumentBuilder<CommandSourceStack> root, Bots bots, CommandBuildContext build) {
		Function<String, Function<Body, com.mojang.brigadier.Command<CommandSourceStack>>> exec =
				verb -> body -> c -> run(c, bots, verb, body);

		root.then(Commands.literal("attack").then(name().then(Commands.argument("target", EntityArgument.entity())
				.executes(exec.apply("attack").apply((c, bot, out) -> attack(c, bot, out, true)))
				.then(Commands.literal("weak").executes(exec.apply("attack").apply((c, bot, out) -> attack(c, bot, out, false)))))));

		root.then(Commands.literal("interact").then(name().then(Commands.argument("target", EntityArgument.entity())
				.executes(exec.apply("interact").apply((c, bot, out) -> interact(c, bot, out, InteractionHand.MAIN_HAND)))
				.then(Commands.argument("hand", StringArgumentType.word())
						.executes(exec.apply("interact").apply((c, bot, out) ->
								interact(c, bot, out, hand(StringArgumentType.getString(c, "hand")))))))));

		root.then(Commands.literal("punch").then(name().then(Commands.argument("pos", BlockPosArgument.blockPos())
				.executes(exec.apply("punch").apply((c, bot, out) -> punch(c, bot, out, "up")))
				.then(Commands.argument("face", StringArgumentType.word())
						.executes(exec.apply("punch").apply((c, bot, out) -> punch(c, bot, out, StringArgumentType.getString(c, "face"))))))));

		root.then(Commands.literal("break").then(name().then(Commands.argument("pos", BlockPosArgument.blockPos())
				.executes(exec.apply("break").apply((c, bot, out) -> {
					BlockPos pos = BlockPosArgument.getLoadedBlockPos(c, "pos");
					String before = String.valueOf(bot.getLevel().getBlockState(pos));
					boolean ok = bot.gameMode.destroyBlock(pos);
					out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("broken", ok).kv("before", before)
							.kv("after", bot.getLevel().getBlockState(pos)).kv("main", item(bot.getMainHandItem())));
				})))));

		Body useitemMain = (c, bot, out) -> useItem(bots, bot, out, InteractionHand.MAIN_HAND, 0, "useitem");
		root.then(Commands.literal("useitem").then(name()
				.executes(exec.apply("useitem").apply(useitemMain))
				.then(Commands.argument("hand", StringArgumentType.word())
						.executes(exec.apply("useitem").apply((c, bot, out) ->
								useItem(bots, bot, out, hand(StringArgumentType.getString(c, "hand")), 0, "useitem")))
						.then(Commands.argument("ticks", IntegerArgumentType.integer(0, 72000))
								.executes(exec.apply("useitem").apply((c, bot, out) -> useItem(bots, bot, out,
										hand(StringArgumentType.getString(c, "hand")), IntegerArgumentType.getInteger(c, "ticks"), "useitem")))))));

		root.then(Commands.literal("eat").then(name()
				.executes(exec.apply("eat").apply((c, bot, out) -> useItem(bots, bot, out, InteractionHand.MAIN_HAND, 0, "eat")))
				.then(Commands.argument("hand", StringArgumentType.word())
						.executes(exec.apply("eat").apply((c, bot, out) ->
								useItem(bots, bot, out, hand(StringArgumentType.getString(c, "hand")), 0, "eat"))))));

		root.then(Commands.literal("hunger").then(name().then(Commands.argument("food", IntegerArgumentType.integer(0, 20))
				.executes(exec.apply("hunger").apply((c, bot, out) -> hunger(c, bot, out, 0f)))
				.then(Commands.argument("sat", FloatArgumentType.floatArg(0f, 20f))
						.executes(exec.apply("hunger").apply((c, bot, out) -> hunger(c, bot, out, FloatArgumentType.getFloat(c, "sat"))))))));

		root.then(Commands.literal("sneak").then(name().then(Commands.argument("state", StringArgumentType.word())
				.executes(exec.apply("sneak").apply((c, bot, out) -> {
					bot.setShiftKeyDown(onOff(StringArgumentType.getString(c, "state")));
					out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("shift", bot.isShiftKeyDown()));
				})))));

		root.then(Commands.literal("look").then(name().then(Commands.argument("yaw", FloatArgumentType.floatArg())
				.then(Commands.argument("pitch", FloatArgumentType.floatArg(-90f, 90f))
						.executes(exec.apply("look").apply((c, bot, out) -> {
							float yaw = FloatArgumentType.getFloat(c, "yaw"), pitch = FloatArgumentType.getFloat(c, "pitch");
							bot.setYRot(yaw);
							bot.setYHeadRot(yaw);
							bot.setYBodyRot(yaw);
							bot.setXRot(pitch);
							look(bot, out);
						}))))));

		root.then(Commands.literal("lookat").then(name().then(Commands.argument("pos", Vec3Argument.vec3())
				.executes(exec.apply("look").apply((c, bot, out) -> {
					bot.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3Argument.getVec3(c, "pos"));
					bot.setYBodyRot(bot.getYRot());
					look(bot, out);
				})))));

		root.then(Commands.literal("moveto").then(name().then(Commands.argument("x", DoubleArgumentType.doubleArg())
				.then(Commands.argument("z", DoubleArgumentType.doubleArg())
						.executes(exec.apply("moveto").apply((c, bot, out) -> moveTo(c, bots, bot, out, "walk", 1200)))
						.then(Commands.argument("gait", StringArgumentType.word())
								.executes(exec.apply("moveto").apply((c, bot, out) ->
										moveTo(c, bots, bot, out, StringArgumentType.getString(c, "gait"), 1200)))
								.then(Commands.argument("timeout", IntegerArgumentType.integer(1, 72000))
										.executes(exec.apply("moveto").apply((c, bot, out) -> moveTo(c, bots, bot, out,
												StringArgumentType.getString(c, "gait"), IntegerArgumentType.getInteger(c, "timeout"))))))))));

		root.then(Commands.literal("moveto-stop").then(name()
				.executes(exec.apply("moveto").apply((c, bot, out) -> {
					boolean was = bots.stopWalk(bot, "stopped");
					out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("stopped", was));
				}))));

		root.then(Commands.literal("inv").then(name()
				.then(Commands.literal("get").executes(exec.apply("inv").apply(BotTestVerbs::invGet)))
				.then(Commands.literal("clear").executes(exec.apply("inv").apply((c, bot, out) -> {
					bot.getInventory().clearContent();
					out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("cleared", true));
				})))
				.then(Commands.literal("select").then(Commands.argument("slot", IntegerArgumentType.integer(0, 8))
						.executes(exec.apply("inv").apply((c, bot, out) -> {
							bot.getInventory().selected = IntegerArgumentType.getInteger(c, "slot");
							out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("selected", bot.getInventory().selected)
									.kv("main", item(bot.getMainHandItem())));
						}))))
				.then(Commands.literal("set").then(Commands.argument("slot", IntegerArgumentType.integer(0, 40))
						.then(Commands.argument("item", ItemArgument.item(build))
								.executes(exec.apply("inv").apply((c, bot, out) -> invSet(c, bot, out, 1)))
								.then(Commands.argument("count", IntegerArgumentType.integer(0, 64))
										.executes(exec.apply("inv").apply((c, bot, out) ->
												invSet(c, bot, out, IntegerArgumentType.getInteger(c, "count"))))))))));

		root.then(Commands.literal("vulnerable").then(name().then(Commands.argument("state", StringArgumentType.word())
				.executes(exec.apply("vulnerable").apply((c, bot, out) -> {
					bots.setVulnerable(bot, onOff(StringArgumentType.getString(c, "state")));
					out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("invulnerable", bot.isInvulnerable()));
				})))));

		root.then(Commands.literal("pickup").then(name().then(Commands.argument("state", StringArgumentType.word())
				.executes(exec.apply("pickup").apply((c, bot, out) -> {
					bots.setPickup(bot, onOff(StringArgumentType.getString(c, "state")));
					out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("pickup", bots.pickup(bot.getGameProfile().getName())));
				})))));
	}

	private static void attack(CommandContext<CommandSourceStack> c, BotPlayer bot, Out out, boolean charged) throws Exception {
		Entity target = EntityArgument.getEntity(c, "target");
		if (charged) bot.chargeAttack();
		float before = target instanceof LivingEntity le ? le.getHealth() : -1f;
		float scale = bot.getAttackStrengthScale(0.5f);
		bot.attack(target);
		bot.swing(InteractionHand.MAIN_HAND);
		bot.resetAttackStrengthTicker();
		BotLine l = out.line().kv("bot", bot.getGameProfile().getName())
				.kv("target", ForgeRegistries.ENTITY_TYPES.getKey(target.getType())).kv("strength", scale)
				.kv("main", item(bot.getMainHandItem()));
		if (target instanceof LivingEntity le) l.kv("health_before", before).kv("health_after", le.getHealth());
		out.emit(l.kv("alive", target.isAlive()));
	}

	/** Right click on an entity as the client sends it (Player.interactOn: Forge's EntityInteract event, then the
	 * entity's own interaction, then the held item's interactLivingEntity); sneaking if the bot is. */
	private static void interact(CommandContext<CommandSourceStack> c, BotPlayer bot, Out out, InteractionHand hand) throws Exception {
		Entity target = EntityArgument.getEntity(c, "target");
		String held = item(bot.getItemInHand(hand));
		InteractionResult r = bot.interactOn(target, hand);
		out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("target", ForgeRegistries.ENTITY_TYPES.getKey(target.getType()))
				.kv("hand", hand == InteractionHand.MAIN_HAND ? "main" : "off").kv("held_before", held).kv("result", r)
				.kv("held_after", item(bot.getItemInHand(hand))).kv("shift", bot.isShiftKeyDown()));
	}

	private static void punch(CommandContext<CommandSourceStack> c, BotPlayer bot, Out out, String face) throws Exception {
		BlockPos pos = BlockPosArgument.getLoadedBlockPos(c, "pos");
		Direction dir = Direction.byName(face);
		if (dir == null) throw new IllegalArgumentException("bad face " + face);
		int top = bot.getLevel().getMaxBuildHeight();
		String before = String.valueOf(bot.getLevel().getBlockState(pos));
		bot.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, dir, top, 0);
		// a survival click on a block that does not break at once leaves a dig in progress: abort it like a release
		if (!bot.getLevel().getBlockState(pos).isAir()) {
			bot.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, dir, top, 0);
		}
		bot.swing(InteractionHand.MAIN_HAND);
		out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("before", before)
				.kv("after", bot.getLevel().getBlockState(pos)));
	}

	private static void useItem(Bots bots, BotPlayer bot, Out out, InteractionHand hand, int ticks, String verb) {
		ItemStack held = bot.getItemInHand(hand);
		String name = bot.getGameProfile().getName();
		if (held.isEmpty()) throw new IllegalStateException("empty hand");
		if (bot.isUsingItem()) bot.stopUsingItem();
		InteractionResult r = bot.gameMode.useItem(bot, bot.getLevel(), held, hand);
		boolean using = bot.isUsingItem();
		if (using) bots.holdUse(bot, ticks);
		out.emit(out.line().kv("bot", name).kv("item", item(held)).kv("result", r).kv("using", using)
				.kv("use_ticks", using ? bot.getUseItemRemainingTicks() : 0).kv("release_after", ticks)
				.kv("tick", bot.getServer().getTickCount()).kv("food", bot.getFoodData().getFoodLevel())
				.kv("sat", bot.getFoodData().getSaturationLevel()));
	}

	private static void hunger(CommandContext<CommandSourceStack> c, BotPlayer bot, Out out, float sat) {
		bot.getFoodData().setFoodLevel(IntegerArgumentType.getInteger(c, "food"));
		bot.getFoodData().setSaturation(sat);
		bot.getFoodData().setExhaustion(0f);
		out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("food", bot.getFoodData().getFoodLevel())
				.kv("sat", bot.getFoodData().getSaturationLevel()));
	}

	private static void look(BotPlayer bot, Out out) {
		out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("yaw", bot.getYRot()).kv("pitch", bot.getXRot()));
	}

	private static void moveTo(CommandContext<CommandSourceStack> c, Bots bots, BotPlayer bot, Out out, String gait, int timeout) {
		if (!gait.equals("walk") && !gait.equals("sprint")) throw new IllegalArgumentException("gait must be walk or sprint");
		double x = DoubleArgumentType.getDouble(c, "x"), z = DoubleArgumentType.getDouble(c, "z");
		bots.stopWalk(bot, "replaced");
		bots.walk(bot, x, z, gait.equals("sprint"), timeout);
		out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("to", x + "," + z).kv("gait", gait)
				.kv("timeout", timeout).kv("tick", bot.getServer().getTickCount()));
	}

	private static void invGet(CommandContext<CommandSourceStack> c, BotPlayer bot, Out out) {
		int n = 0;
		var inv = bot.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack st = inv.getItem(i);
			if (st.isEmpty()) continue;
			n++;
			out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("slot", i).kv("item", item(st)));
		}
		out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("filled", n).kv("selected", inv.selected));
	}

	private static void invSet(CommandContext<CommandSourceStack> c, BotPlayer bot, Out out, int count) throws Exception {
		int slot = IntegerArgumentType.getInteger(c, "slot");
		ItemStack st = count == 0 ? ItemStack.EMPTY : ItemArgument.getItem(c, "item").createItemStack(count, false);
		bot.getInventory().setItem(slot, st);
		out.emit(out.line().kv("bot", bot.getGameProfile().getName()).kv("slot", slot).kv("item", item(bot.getInventory().getItem(slot))));
	}
}
