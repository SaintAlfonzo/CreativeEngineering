package com.SaintAlfonzo.ceperfbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * {@code /pbot} (permission 2; the suite sends these over RCON):
 * <pre>
 *   spawn NAME POS [DIM] [survival|creative|adventure]   log a bot in (or move it); invulnerable
 *   remove NAME | remove-all | list
 *   path NAME TOX Y TOZ SPEED     straight line at height Y, SPEED blocks per tick; marks path_done:NAME
 *   path-stop NAME
 *   hold NAME ITEM [COUNT]        main hand
 *   use NAME POS [FACE]           right-click a block with the main hand item (as a client would)
 *   mark LABEL...                 marker line in the tick log
 *   census TAG                    &lt;out&gt;/census-TAG.json: entities by type per dimension, block entities by
 *                                 type in loaded chunks within the view distance of each bot
 *   status
 * </pre>
 */
final class BotCommands {

    private final TickLog log;
    private final Bots bots;
    private final Path out;

    BotCommands(TickLog log, Bots bots, Path out) {
        this.log = log;
        this.bots = bots;
        this.out = out;
        bots.setLog(log);
    }

    @SubscribeEvent
    public void onRegister(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("pbot").requires(s -> s.hasPermission(2))
                .then(Commands.literal("spawn").then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .executes(c -> spawn(c, c.getSource().getLevel(), "survival"))
                                .then(Commands.argument("dim", DimensionArgument.dimension())
                                        .executes(c -> spawn(c, DimensionArgument.getDimension(c, "dim"), "survival"))
                                        .then(Commands.argument("mode", StringArgumentType.word())
                                                .executes(c -> spawn(c, DimensionArgument.getDimension(c, "dim"),
                                                        StringArgumentType.getString(c, "mode"))))))))
                .then(Commands.literal("remove").then(Commands.argument("name", StringArgumentType.word())
                        .executes(c -> reply(c, bots.remove(StringArgumentType.getString(c, "name")) ? "removed" : "no such bot"))))
                .then(Commands.literal("remove-all").executes(c -> reply(c, "removed " + bots.removeAll())))
                .then(Commands.literal("list").executes(this::list))
                .then(Commands.literal("path").then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("tox", DoubleArgumentType.doubleArg())
                                .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("toz", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("speed", DoubleArgumentType.doubleArg(0.01, 64))
                                                        .executes(this::path)))))))
                .then(Commands.literal("path-stop").then(Commands.argument("name", StringArgumentType.word())
                        .executes(c -> {
                            BotPlayer bot = bot(c);
                            return bot == null ? 0 : reply(c, bots.stopPath(bot) ? "stopped" : "no path");
                        })))
                .then(Commands.literal("hold").then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                .executes(c -> hold(c, 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                        .executes(c -> hold(c, IntegerArgumentType.getInteger(c, "count")))))))
                .then(Commands.literal("use").then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(c -> use(c, "up"))
                                .then(Commands.argument("face", StringArgumentType.word())
                                        .executes(c -> use(c, StringArgumentType.getString(c, "face")))))))
                .then(Commands.literal("mark").then(Commands.argument("label", StringArgumentType.greedyString())
                        .executes(c -> {
                            log.mark(StringArgumentType.getString(c, "label"));
                            return reply(c, "marked");
                        })))
                .then(Commands.literal("census").then(Commands.argument("tag", StringArgumentType.word())
                        .executes(this::census)))
                .then(Commands.literal("status").executes(c -> reply(c, "ceperfbot ok tick "
                        + c.getSource().getServer().getTickCount() + " bots " + bots.all().size() + " out " + log.fileHint()))));
    }

    private static int reply(CommandContext<CommandSourceStack> c, String text) {
        c.getSource().sendSuccess(Component.literal(text), false);
        return 1;
    }

    private BotPlayer bot(CommandContext<CommandSourceStack> c) {
        String name = StringArgumentType.getString(c, "name");
        BotPlayer bot = bots.get(name);
        if (bot == null) c.getSource().sendFailure(Component.literal("no such bot: " + name));
        return bot;
    }

    private int spawn(CommandContext<CommandSourceStack> c, ServerLevel level, String mode) throws CommandSyntaxException {
        GameType type = GameType.byName(mode, null);
        if (type == null || type == GameType.SPECTATOR) {
            // spectators don't count for chunk ticking or mob spawning, which is the point of a bot
            c.getSource().sendFailure(Component.literal("mode must be survival, creative or adventure"));
            return 0;
        }
        String name = StringArgumentType.getString(c, "name");
        Vec3 pos = Vec3Argument.getVec3(c, "pos");
        BotPlayer bot = bots.spawn(c.getSource().getServer(), name, level, pos.x, pos.y, pos.z, type);
        log.mark("bot_spawn:" + name);
        return reply(c, "bot " + name + " at " + (int) bot.getX() + " " + (int) bot.getY() + " " + (int) bot.getZ()
                + " in " + level.dimension().location());
    }

    private int list(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder("bots:");
        bots.all().forEach((name, bot) -> sb.append(' ').append(name).append(bot.isRemoved() ? "(removed)" : "")
                .append('@').append(bot.getLevel().dimension().location()).append(',')
                .append((int) bot.getX()).append(',').append((int) bot.getY()).append(',').append((int) bot.getZ()));
        return reply(c, sb.toString());
    }

    private int path(CommandContext<CommandSourceStack> c) {
        BotPlayer bot = bot(c);
        if (bot == null) return 0;
        int ticks = bots.path(bot, DoubleArgumentType.getDouble(c, "tox"), DoubleArgumentType.getDouble(c, "y"),
                DoubleArgumentType.getDouble(c, "toz"), DoubleArgumentType.getDouble(c, "speed"));
        return reply(c, "path " + ticks + " ticks");
    }

    private int hold(CommandContext<CommandSourceStack> c, int count) {
        BotPlayer bot = bot(c);
        if (bot == null) return 0;
        ResourceLocation id = ResourceLocationArgument.getId(c, "item");
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null || !ForgeRegistries.ITEMS.containsKey(id)) {
            c.getSource().sendFailure(Component.literal("no such item: " + id));
            return 0;
        }
        bot.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item, count));
        return reply(c, "holding " + id);
    }

    private int use(CommandContext<CommandSourceStack> c, String face) throws CommandSyntaxException {
        BotPlayer bot = bot(c);
        if (bot == null) return 0;
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(c, "pos");
        Direction dir = Direction.byName(face);
        if (dir == null) dir = Direction.UP;
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos).relative(dir, 0.5), dir, pos, false);
        InteractionResult r = bot.gameMode.useItemOn(bot, bot.getLevel(), bot.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
        return reply(c, "use " + r);
    }

    private int census(CommandContext<CommandSourceStack> c) {
        MinecraftServer server = c.getSource().getServer();
        String tag = StringArgumentType.getString(c, "tag").replaceAll("[^A-Za-z0-9_.-]", "_");
        int radius = server.getPlayerList().getViewDistance();
        Map<String, Object> levels = new TreeMap<>();
        int entityTotal = 0, beTotal = 0;
        for (ServerLevel level : server.getAllLevels()) {
            Map<String, Integer> entities = new TreeMap<>();
            for (Entity e : level.getAllEntities()) {
                entities.merge(String.valueOf(EntityType.getKey(e.getType())), 1, Integer::sum);
            }
            Set<Long> seen = new HashSet<>();
            Map<String, Integer> blockEntities = new TreeMap<>();
            for (BotPlayer bot : bots.all().values()) {
                if (bot.isRemoved() || bot.getLevel() != level) continue;
                ChunkPos at = bot.chunkPosition();
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (!seen.add(ChunkPos.asLong(at.x + dx, at.z + dz))) continue;
                        LevelChunk chunk = level.getChunkSource().getChunkNow(at.x + dx, at.z + dz);
                        if (chunk == null) continue;
                        for (BlockEntity be : chunk.getBlockEntities().values()) {
                            blockEntities.merge(String.valueOf(BlockEntityType.getKey(be.getType())), 1, Integer::sum);
                        }
                    }
                }
            }
            int n = entities.values().stream().mapToInt(Integer::intValue).sum();
            int b = blockEntities.values().stream().mapToInt(Integer::intValue).sum();
            if (n == 0 && b == 0) continue;
            entityTotal += n;
            beTotal += b;
            Map<String, Object> row = new TreeMap<>();
            row.put("entities", entities);
            row.put("block_entities_near_bots", blockEntities);
            row.put("loaded_chunks", level.getChunkSource().getLoadedChunksCount());
            levels.put(level.dimension().location().toString(), row);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path file = out.resolve("census-" + tag + ".json");
        try {
            Files.writeString(file, gson.toJson(Map.of("tick", server.getTickCount(), "levels", levels)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            c.getSource().sendFailure(Component.literal("census write failed: " + e));
            return 0;
        }
        return reply(c, "census " + tag + " entities " + entityTotal + " block_entities " + beTotal);
    }
}
