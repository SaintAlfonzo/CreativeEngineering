package net.satisfy.vinery.core.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.satisfy.vinery.core.block.GrapevinePotBlock;
import net.satisfy.vinery.core.block.entity.ApplePressBlockEntity;
import net.satisfy.vinery.core.block.entity.FermentationBarrelBlockEntity;
import net.satisfy.vinery.core.block.entity.GrapevinePotBlockEntity;
import net.satisfy.vinery.core.entity.TraderMuleEntity;
import net.satisfy.vinery.core.entity.WanderingWinemakerEntity;
import net.satisfy.vinery.core.registry.EntityTypeRegistry;
import net.satisfy.vinery.core.registry.GrapeTypeRegistry;
import net.satisfy.vinery.core.wine.GrapeType;
import net.satisfy.vinery.core.wine.JuiceUtil;
import net.satisfy.vinery.core.wine.WineEffects;
import net.satisfy.vinery.platform.PlatformHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class VineryCommands {
    private static final int MAX_GROW_STEPS = 64;

    @FunctionalInterface
    private interface BlockAction {
        int run(CommandContext<CommandSourceStack> ctx, BlockPos pos) throws CommandSyntaxException;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("vinery")
                        .then(Commands.literal("dump")
                                .then(Commands.literal("grapes").executes(ctx -> dumpGrapes(ctx.getSource())))
                                .then(Commands.literal("juices").executes(ctx -> dumpJuices(ctx.getSource())))
                                .then(Commands.literal("effects").executes(ctx -> dumpEffects(ctx.getSource()))))
                        .then(Commands.literal("grow").requires(source -> source.hasPermission(2))
                                .executes(ctx -> grow(ctx.getSource(), lookedAt(ctx.getSource()), 1))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> grow(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"), 1))
                                        .then(Commands.argument("steps", IntegerArgumentType.integer(1, MAX_GROW_STEPS))
                                                .executes(ctx -> grow(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"), IntegerArgumentType.getInteger(ctx, "steps"))))))
                        .then(Commands.literal("barrel").requires(source -> source.hasPermission(2))
                                .then(atBlock(Commands.literal("finish"), (ctx, pos) -> finishBarrel(ctx.getSource(), pos))))
                        .then(Commands.literal("press").requires(source -> source.hasPermission(2))
                                .then(atBlock(Commands.literal("finish"), (ctx, pos) -> finishPress(ctx.getSource(), pos))))
                        .then(Commands.literal("pot").requires(source -> source.hasPermission(2))
                                .then(Commands.literal("fill")
                                        .then(atBlock(Commands.argument("grape", StringArgumentType.word()).suggests((ctx, builder) -> SharedSuggestionProvider.suggest(grapeIds(), builder)),
                                                (ctx, pos) -> fillPot(ctx.getSource(), pos, StringArgumentType.getString(ctx, "grape"))))))
                        .then(Commands.literal("winemaker").requires(source -> source.hasPermission(2))
                                .then(Commands.literal("spawn")
                                        .executes(ctx -> spawnWinemaker(ctx.getSource(), new BlockPos(ctx.getSource().getPosition())))
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(ctx -> spawnWinemaker(ctx.getSource(), BlockPosArgument.getSpawnablePos(ctx, "pos")))))
                                .then(Commands.literal("locate").executes(ctx -> locateWinemakers(ctx.getSource()))))
                        .then(Commands.literal("effect").requires(source -> source.hasPermission(2))
                                .then(Commands.argument("wine", ResourceLocationArgument.id()).suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(wineIds(), builder))
                                        .executes(ctx -> giveEffect(ctx, List.of(ctx.getSource().getPlayerOrException()), -1))
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(ctx -> giveEffect(ctx, EntityArgument.getPlayers(ctx, "targets"), -1))
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 256))
                                                        .executes(ctx -> giveEffect(ctx, EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "level") - 1))))))
                        .then(Commands.literal("reload").requires(source -> source.hasPermission(2))
                                .executes(ctx -> reload(ctx.getSource())))
        );
    }

    private static <T extends ArgumentBuilder<CommandSourceStack, T>> T atBlock(T node, BlockAction action) {
        return node
                .executes(ctx -> withPos(ctx, lookedAt(ctx.getSource()), action))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(ctx -> withPos(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos"), action)));
    }

    private static int withPos(CommandContext<CommandSourceStack> ctx, BlockPos pos, BlockAction action) throws CommandSyntaxException {
        if (pos == null) {
            ctx.getSource().sendFailure(Component.translatable("command.vinery.no_block"));
            return 0;
        }
        return action.run(ctx, pos);
    }

    private static int grow(CommandSourceStack source, BlockPos pos, int steps) {
        if (pos == null) {
            source.sendFailure(Component.translatable("command.vinery.no_block"));
            return 0;
        }
        ServerLevel level = source.getLevel();
        int grown = 0;
        for (int i = 0; i < steps; i++) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof BonemealableBlock block) || !block.isValidBonemealTarget(level, pos, state, false)) {
                break;
            }
            block.performBonemeal(level, level.random, pos, state);
            grown++;
        }
        if (grown == 0) {
            source.sendFailure(Component.translatable("command.vinery.grow.cannot"));
            return 0;
        }
        int result = grown;
        source.sendSuccess(Component.translatable("command.vinery.grow.done", result), true);
        return grown;
    }

    private static int finishBarrel(CommandSourceStack source, BlockPos pos) {
        if (!(source.getLevel().getBlockEntity(pos) instanceof FermentationBarrelBlockEntity barrel)) {
            source.sendFailure(Component.translatable("command.vinery.barrel.not_barrel"));
            return 0;
        }
        if (!barrel.finishFermentation()) {
            source.sendFailure(Component.translatable("command.vinery.barrel.idle"));
            return 0;
        }
        source.sendSuccess(Component.translatable("command.vinery.barrel.finished"), true);
        return 1;
    }

    private static int finishPress(CommandSourceStack source, BlockPos pos) {
        if (!(source.getLevel().getBlockEntity(pos) instanceof ApplePressBlockEntity press)) {
            source.sendFailure(Component.translatable("command.vinery.press.not_press"));
            return 0;
        }
        if (!press.finishProcessing()) {
            source.sendFailure(Component.translatable("command.vinery.press.idle"));
            return 0;
        }
        source.sendSuccess(Component.translatable("command.vinery.press.finished"), true);
        return 1;
    }

    private static int fillPot(CommandSourceStack source, BlockPos pos, String grapeId) {
        ServerLevel level = source.getLevel();
        BlockState state = level.getBlockState(pos);
        GrapeType type = GrapeType.fromString(grapeId);
        if (type == null || type.equals(GrapeTypeRegistry.NONE)) {
            source.sendFailure(Component.translatable("command.vinery.pot.unknown_grape", grapeId));
            return 0;
        }
        if (!(state.getBlock() instanceof GrapevinePotBlock) || !(level.getBlockEntity(pos) instanceof GrapevinePotBlockEntity pot)) {
            source.sendFailure(Component.translatable("command.vinery.pot.not_pot"));
            return 0;
        }
        int progress = GrapevinePotBlockEntity.pointsNeeded() - GrapevinePotBlockEntity.POINTS_PER_STOMP;
        int stage = GrapevinePotBlock.FULL_STAGE + progress * (GrapevinePotBlock.MAX_STAGE - GrapevinePotBlock.FULL_STAGE) / GrapevinePotBlockEntity.pointsNeeded();
        level.setBlockAndUpdate(pos, state
                .setValue(GrapevinePotBlock.GRAPEVINE_TYPE, type)
                .setValue(GrapevinePotBlock.STORAGE, GrapevinePotBlock.MAX_GRAPES)
                .setValue(GrapevinePotBlock.STAGE, stage));
        pot.setProgress(progress);
        source.sendSuccess(Component.translatable("command.vinery.pot.filled", grapeId), true);
        return 1;
    }

    private static int spawnWinemaker(CommandSourceStack source, BlockPos pos) {
        ServerLevel level = source.getLevel();
        WanderingWinemakerEntity winemaker = EntityTypeRegistry.WANDERING_WINEMAKER.get().spawn(level, null, null, null, pos, MobSpawnType.COMMAND, false, false);
        if (winemaker == null) {
            source.sendFailure(Component.translatable("command.vinery.winemaker.failed"));
            return 0;
        }
        winemaker.setDespawnDelay(PlatformHelper.getTraderSpawnDelay());
        if (PlatformHelper.shouldSpawnWithMules()) {
            for (int i = 0; i < 2; i++) {
                TraderMuleEntity mule = EntityTypeRegistry.MULE.get().spawn(level, null, null, null, pos, MobSpawnType.COMMAND, false, false);
                if (mule != null) {
                    mule.setLeashedTo(winemaker, true);
                }
            }
        }
        source.sendSuccess(Component.translatable("command.vinery.winemaker.spawned", formatPos(pos)), true);
        return 1;
    }

    private static int locateWinemakers(CommandSourceStack source) {
        int found = 0;
        for (ServerLevel level : source.getServer().getAllLevels()) {
            for (WanderingWinemakerEntity winemaker : level.getEntities(EntityTypeRegistry.WANDERING_WINEMAKER.get(), entity -> entity.isAlive())) {
                BlockPos pos = winemaker.blockPosition();
                String dimension = level.dimension().location().toString();
                MutableComponent where = ComponentUtils.wrapInSquareBrackets(Component.literal(formatPos(pos) + " " + dimension)).withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/execute in " + dimension + " run tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, trades(winemaker))));
                int minutes = winemaker.getDespawnDelay() / 1200;
                source.sendSuccess(Component.translatable("command.vinery.winemaker.found", where, minutes), false);
                found++;
            }
        }
        if (found == 0) {
            source.sendFailure(Component.translatable("command.vinery.winemaker.none"));
        }
        return found;
    }

    private static Component trades(WanderingWinemakerEntity winemaker) {
        MutableComponent text = Component.translatable("command.vinery.winemaker.trades");
        for (MerchantOffer offer : winemaker.getOffers()) {
            text.append("\n").append(offer.getResult().getCount() + "x ").append(offer.getResult().getHoverName())
                    .append(Component.literal(" ← " + offer.getCostA().getCount() + "x ").append(offer.getCostA().getHoverName()).withStyle(ChatFormatting.GRAY));
        }
        return text;
    }

    private static int giveEffect(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players, int amplifier) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "wine");
        Item wine = Registry.ITEM.getOptional(id).orElse(null);
        WineEffects.WineEffect effect = wine == null ? null : WineEffects.get(wine);
        if (effect == null) {
            ctx.getSource().sendFailure(Component.translatable("command.vinery.effect.unknown", id.toString()));
            return 0;
        }
        int resolved = amplifier >= 0 ? amplifier : effect.amplifier();
        for (ServerPlayer player : players) {
            player.addEffect(new MobEffectInstance(effect.effect(), effect.duration(), resolved), ctx.getSource().getEntity());
        }
        int count = players.size();
        ctx.getSource().sendSuccess(Component.translatable("command.vinery.effect.given", wine.getDefaultInstance().getHoverName(), count), true);
        return count;
    }

    private static int reload(CommandSourceStack source) {
        if (!PlatformHelper.reloadConfig()) {
            source.sendSuccess(Component.translatable("command.vinery.reload.automatic"), false);
            return 0;
        }
        source.sendSuccess(Component.translatable("command.vinery.reload.done"), true);
        return 1;
    }

    private static int dumpGrapes(CommandSourceStack source) {
        List<GrapeType> types = new ArrayList<>(GrapeTypeRegistry.GRAPE_TYPE_TYPES);
        types.remove(GrapeTypeRegistry.NONE);
        types.sort(Comparator.comparing(GrapeType::getId));
        header(source, "command.vinery.dump.grapes", "%s grape types", types.size());
        for (GrapeType type : types) {
            String line = type.getId() + " (" + (type.isRed() ? "red" : "white") + (type.isLattice() ? ", lattice" : "") + ") fruit=" + key(type.getFruit()) + " seeds=" + key(type.getSeeds()) + " bottle=" + key(type.getBottle());
            source.sendSuccess(Component.literal(line), false);
        }
        return types.size();
    }

    private static int dumpJuices(CommandSourceStack source) {
        List<String> juices = JuiceUtil.types();
        header(source, "command.vinery.dump.juices", "%s juice types", juices.size());
        for (String juice : juices) {
            String line = juice + " → " + key(JuiceUtil.juiceItem(juice));
            source.sendSuccess(Component.literal(line), false);
        }
        return juices.size();
    }

    private static int dumpEffects(CommandSourceStack source) {
        List<ResourceLocation> wines = wineIds();
        header(source, "command.vinery.dump.effects", "%s wines with an effect", wines.size());
        for (ResourceLocation id : wines) {
            WineEffects.WineEffect effect = WineEffects.get(Registry.ITEM.get(id));
            String line = id + " → " + String.valueOf(net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getKey(effect.effect())) + " " + (effect.amplifier() + 1) + ", " + effect.duration() + " ticks";
            source.sendSuccess(Component.literal(line), false);
        }
        return wines.size();
    }

    private static void header(CommandSourceStack source, String key, String fallback, int count) {
        source.sendSuccess(Component.translatable(key, count).withStyle(ChatFormatting.GOLD), false);
    }

    private static List<String> grapeIds() {
        return GrapeTypeRegistry.GRAPE_TYPE_TYPES.stream().filter(type -> !type.equals(GrapeTypeRegistry.NONE)).map(GrapeType::getId).sorted().toList();
    }

    private static List<ResourceLocation> wineIds() {
        return Registry.ITEM.stream().filter(item -> WineEffects.get(item) != null).map(Registry.ITEM::getKey).sorted().toList();
    }

    private static String key(Item item) {
        return item == null ? "-" : Registry.ITEM.getKey(item).toString();
    }

    private static String formatPos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static BlockPos lookedAt(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        HitResult hit = player.pick(player.getReachDistance(), 0.0F, false);
        return hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK ? blockHit.getBlockPos() : null;
    }
}
