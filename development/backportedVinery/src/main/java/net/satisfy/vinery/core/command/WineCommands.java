package net.satisfy.vinery.core.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.satisfy.foundation.storage.StorageBlockEntity;
import net.satisfy.vinery.core.block.entity.FermentationBarrelBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.satisfy.vinery.core.item.DrinkBlockItem;
import net.satisfy.vinery.core.wine.VintageClock;
import net.satisfy.vinery.forge.VineryConfig;
import net.satisfy.vinery.core.wine.WineEffects;
import net.satisfy.vinery.core.wine.WineYears;
import net.satisfy.vinery.platform.PlatformHelper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public final class WineCommands {
    private static final int MAX_AMOUNT = 100000;

    @FunctionalInterface
    private interface WineOperation {
        void apply(ItemStack wine, Level level);
    }

    @FunctionalInterface
    private interface OperationFactory {
        WineOperation create(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("wine")
                        .then(Commands.literal("info").executes(ctx -> info(ctx.getSource())))
                        .then(Commands.literal("time").executes(ctx -> time(ctx.getSource())))
                        .then(Commands.literal("storage")
                                .executes(ctx -> storage(ctx.getSource(), lookedAt(ctx.getSource())))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> storage(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos")))))
                        .then(Commands.literal("age")
                                .requires(source -> source.hasPermission(2))
                                .then(amountNode("set", 1))
                                .then(amountNode("add", 1))
                                .then(amountNode("remove", -1)))
                        .then(Commands.literal("level")
                                .requires(source -> source.hasPermission(2))
                                .then(targets(Commands.argument("level", IntegerArgumentType.integer(0, 255)), ctx -> {
                                    int target = IntegerArgumentType.getInteger(ctx, "level");
                                    return (wine, level) -> {
                                        VintageClock.Snapshot clock = VintageClock.of(level);
                                        int clamped = Math.min(target, clock.maxLevel());
                                        WineYears.setWineAgeDays(wine, level, clamped * clock.yearsPerEffectLevel() * clock.daysPerYear());
                                    };
                                })))
                        .then(targets(Commands.literal("mature").requires(source -> source.hasPermission(2)), ctx -> (wine, level) -> {
                            VintageClock.Snapshot clock = VintageClock.of(level);
                            WineYears.setWineAgeDays(wine, level, clock.maxLevel() * clock.yearsPerEffectLevel() * clock.daysPerYear());
                        }))
                        .then(targets(Commands.literal("fresh").requires(source -> source.hasPermission(2)), ctx -> (wine, level) -> WineYears.setWineAgeDays(wine, level, 0)))
                        .then(Commands.literal("clock")
                                .requires(source -> source.hasPermission(2))
                                .then(clockNode("add"))
                                .then(clockNode("set")))
        );
    }

    /** Port (B4.5): moves the vintage clock, for testing. */
    private static ArgumentBuilder<CommandSourceStack, ?> clockNode(String mode) {
        ArgumentBuilder<CommandSourceStack, ?> amount = Commands.argument("amount", IntegerArgumentType.integer(0, MAX_AMOUNT));
        for (String unit : new String[]{"ticks", "minutes", "hours", "days", "months"}) {
            amount = amount.then(Commands.literal(unit).executes(ctx -> clock(ctx.getSource(), mode, IntegerArgumentType.getInteger(ctx, "amount"), unit)));
        }
        return Commands.literal(mode).then(amount);
    }

    private static int clock(CommandSourceStack source, String mode, int amount, String unit) {
        VintageClock.Snapshot clock = VintageClock.of(source.getLevel());
        double perUnit = switch (unit) {
            case "minutes" -> 1200;
            case "hours" -> 72000;
            case "days" -> clock.ticksPerDay();
            case "months" -> clock.ticksPerYear();
            default -> 1;
        };
        long ticks = (long) (amount * perUnit);
        if (mode.equals("set")) {
            VintageClock.set(source.getServer(), ticks);
        } else {
            VintageClock.add(source.getServer(), ticks);
        }
        VintageClock.Snapshot after = VintageClock.of(source.getLevel());
        long now = after.ticks();
        source.sendSuccess(Component.translatable("command.vinery_backport.wine.clock", now, DrinkBlockItem.realTime(after.realTicks(now))), true);
        return (int) Math.min(Integer.MAX_VALUE, now);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> amountNode(String mode, int sign) {
        return Commands.literal(mode).then(Commands.argument("amount", IntegerArgumentType.integer(0, MAX_AMOUNT))
                .then(targets(Commands.literal("months"), ctx -> ageOperation(ctx, mode, sign, true)))
                .then(targets(Commands.literal("days"), ctx -> ageOperation(ctx, mode, sign, false))));
    }

    private static WineOperation ageOperation(CommandContext<CommandSourceStack> ctx, String mode, int sign, boolean months) {
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        return (wine, level) -> {
            int days = months ? amount * VintageClock.of(level).daysPerYear() : amount;
            int current = WineYears.getWineAgeDays(wine, level);
            WineYears.setWineAgeDays(wine, level, mode.equals("set") ? days : current + sign * days);
        };
    }

    private static <T extends ArgumentBuilder<CommandSourceStack, T>> T targets(T node, OperationFactory factory) {
        return node
                .executes(ctx -> applyToStacks(ctx.getSource(), factory.create(ctx), List.of(ctx.getSource().getPlayerOrException().getMainHandItem())))
                .then(Commands.literal("hand")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> applyToPlayers(ctx, factory, false))))
                .then(Commands.literal("inventory")
                        .executes(ctx -> applyToStacks(ctx.getSource(), factory.create(ctx), inventory(ctx.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> applyToPlayers(ctx, factory, true))))
                .then(Commands.literal("block")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> applyToBlock(ctx.getSource(), factory.create(ctx), BlockPosArgument.getLoadedBlockPos(ctx, "pos")))));
    }

    private static int applyToPlayers(CommandContext<CommandSourceStack> ctx, OperationFactory factory, boolean wholeInventory) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
        WineOperation operation = factory.create(ctx);
        int changed = 0;
        for (ServerPlayer player : players) {
            changed += apply(player.getLevel(), operation, wholeInventory ? inventory(player) : List.of(player.getMainHandItem()));
        }
        return report(ctx.getSource(), changed);
    }

    private static int applyToStacks(CommandSourceStack source, WineOperation operation, List<ItemStack> stacks) {
        return report(source, apply(source.getLevel(), operation, stacks));
    }

    private static int applyToBlock(CommandSourceStack source, WineOperation operation, BlockPos pos) {
        ServerLevel level = source.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        List<ItemStack> stacks;
        if (blockEntity instanceof StorageBlockEntity storage) {
            stacks = storage.getInventory();
        } else if (blockEntity instanceof FermentationBarrelBlockEntity barrel) {
            stacks = barrel.getItems();
        } else {
            source.sendFailure(Component.translatable("command.vinery.wine.no_container"));
            return 0;
        }
        int changed = apply(level, operation, stacks);
        if (changed > 0) {
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_CLIENTS);
        }
        return report(source, changed);
    }

    private static int apply(Level level, WineOperation operation, List<ItemStack> stacks) {
        if (!WineYears.isAgingEnabled()) {
            return 0;
        }
        int changed = 0;
        for (ItemStack stack : stacks) {
            if (stack.getItem() instanceof DrinkBlockItem) {
                operation.apply(stack, level);
                changed++;
            }
        }
        return changed;
    }

    private static int report(CommandSourceStack source, int changed) {
        if (!WineYears.isAgingEnabled()) {
            source.sendFailure(Component.translatable("command.vinery.wine.aging_disabled"));
            return 0;
        }
        if (changed == 0) {
            source.sendFailure(Component.translatable("command.vinery.wine.no_wine"));
            return 0;
        }
        source.sendSuccess(Component.translatable("command.vinery.wine.changed", changed), true);
        return changed;
    }

    private static List<ItemStack> inventory(ServerPlayer player) {
        return Stream.of(player.getInventory().items, player.getInventory().offhand).flatMap(List::stream).toList();
    }

    private static int info(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof DrinkBlockItem)) {
            source.sendFailure(Component.translatable("command.vinery.wine.not_wine"));
            return 0;
        }
        Level level = player.getLevel();
        WineYears.convertLegacy(stack, level);
        CompoundTag component = WineYears.data(stack);
        VintageClock.Snapshot clock = VintageClock.of(level);
        source.sendSuccess(stack.getHoverName().copy().withStyle(ChatFormatting.GOLD), false);
        if (component == null) {
            source.sendSuccess(line("command.vinery.wine.info.fresh", "Fresh, not aging yet"), false);
            return 1;
        }
        int days = WineYears.getWineAgeDays(stack, level);
        int levelNow = WineYears.getEffectLevel(stack, level);
        WineEffects.WineEffect effect = WineEffects.get(stack.getItem());
        int duration = WineYears.getEffectDuration(stack, level, effect != null ? effect.duration() : 0);
        int next = WineYears.getDaysUntilNextLevel(stack, level);
        boolean waitsForStorage = clock.ageOnlyInStorage() && component.getLong("storedSince") < 0;

        int months = WineYears.getWineAgeYears(stack, level);
        source.sendSuccess(line("command.vinery.wine.info.age", "Age: %s months, %s days", months, days - months * clock.daysPerYear()), false);
        source.sendSuccess(line("command.vinery.wine.info.level", "Effect level: %s / %s", levelNow, clock.maxLevel()), false);
        source.sendSuccess(line("command.vinery.wine.info.duration", "Duration: %ss", duration / 20), false);
        if (next < 0) {
            source.sendSuccess(line("command.vinery.wine.info.fully_aged", "Fully aged").withStyle(ChatFormatting.GOLD), false);
        } else if (waitsForStorage) {
            source.sendSuccess(line("command.vinery.wine.info.needs_storage", "Only ages while stored"), false);
        } else {
            long ticks = WineYears.getTicksUntilNextLevel(stack, level);
            source.sendSuccess(line("command.vinery.wine.info.next", "Next level in ~%s days", next), false);
            source.sendSuccess(Component.translatable("command.vinery_backport.wine.info.next_time", DrinkBlockItem.realTime(clock.realTicks(ticks)), DrinkBlockItem.realTime(clock.realTicks(Math.ceil(ticks * 100.0 / Math.max(100, clock.cellarPercent()))))), false);
        }
        String raw = component.toString();
        source.sendSuccess(line("command.vinery.wine.info.raw", "[Raw data]").withStyle(style -> style
                .withColor(ChatFormatting.DARK_GRAY)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(raw)))
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, raw))), false);
        return 1;
    }

    /** Port (B4.5): the vintage clock and what its units are in real play time. */
    private static int time(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        VintageClock.Snapshot clock = VintageClock.of(level);
        int daysPerYear = clock.daysPerYear();
        int day = WineYears.getDays(level);
        source.sendSuccess(line("command.vinery.wine.time", "Day %s, month %s (%s days per month, %s months per effect level)",
                day, WineYears.getYear(level, daysPerYear), daysPerYear, clock.yearsPerEffectLevel()), false);
        source.sendSuccess(Component.translatable("command.vinery_backport.wine.time.clock", clock.ticks(), DrinkBlockItem.realTime(clock.realTicks(clock.ticks())),
                VineryConfig.CLOCK_MODE.get().name(), VineryConfig.DAY_UNIT.get().name()), false);
        source.sendSuccess(Component.translatable("command.vinery_backport.wine.time.units", DrinkBlockItem.realTime(clock.realTicks(clock.ticksPerDay())),
                DrinkBlockItem.realTime(clock.realTicks(clock.ticksPerYear())), DrinkBlockItem.realTime(clock.realTicks(clock.ticksPerYear() * clock.yearsPerEffectLevel()))), false);
        String measured = clock.measuredDayTicks() > 0 ? DrinkBlockItem.realTime((long) clock.measuredDayTicks()) : "-";
        source.sendSuccess(Component.translatable("command.vinery_backport.wine.time.world_day", measured), false);
        return day;
    }

    private static int storage(CommandSourceStack source, BlockPos pos) {
        if (pos == null) {
            source.sendFailure(Component.translatable("command.vinery.wine.storage.no_block"));
            return 0;
        }
        int kind = WineYears.storageKind(source.getLevel(), pos);
        int rate = WineYears.storageRate(source.getLevel(), kind);
        MutableComponent message = Component.translatable("command.vinery_backport.wine.storage", WineYears.storageLabel(kind), rate)
                .withStyle(rate > 100 ? ChatFormatting.GREEN : rate < 100 ? ChatFormatting.GOLD : ChatFormatting.WHITE);
        source.sendSuccess(message, false);
        return rate;
    }

    private static BlockPos lookedAt(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        HitResult hit = player.pick(player.getReachDistance(), 0.0F, false);
        return hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK ? blockHit.getBlockPos() : null;
    }

    private static MutableComponent line(String key, String fallback, Object... args) {
        // Port: 1.19.2 has no translatable-with-fallback; upstream's lang has every key used here
        return Component.translatable(key, args);
    }
}
