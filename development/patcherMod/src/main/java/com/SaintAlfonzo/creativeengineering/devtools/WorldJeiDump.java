package com.SaintAlfonzo.creativeengineering.devtools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * {@code /dumpworldjei}: the live-game facts the world-interaction JEI tabs need that no data file holds (which block
 * an axe strips to, which mob a bucket picks up...). Each {@link Part} writes
 * {@code kubejs/exported/world_jei/<name>.json}; tools/gen_jei_world.py turns them into tab rows. Runs on the server
 * (integrated or dedicated), so the overworld is there to build contexts in. A failing part is reported and the
 * rest still run.
 */
@Mod.EventBusSubscriber(modid = "creativeengineering", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WorldJeiDump {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /** One output file. {@code dump} gets the overworld and returns the file's JSON. */
    public record Part(String name, Function<ServerLevel, JsonElement> dump) {}

    private static final List<Part> PARTS = List.of(
            new Part("tool_pairs", WorldJeiToolPairs::dump),
            new Part("mob_buckets", WorldJeiMobBuckets::dump));

    private WorldJeiDump() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dumpworldjei")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    ServerLevel level = ctx.getSource().getServer().overworld();
                    int ok = 0;
                    for (Part part : PARTS) {
                        try {
                            write(part.name(), part.dump().apply(level));
                            ok++;
                        } catch (Exception e) {
                            LOGGER.error("[dumpworldjei] {} failed", part.name(), e);
                            ctx.getSource().sendFailure(Component.literal("[dumpworldjei] " + part.name() + " failed: " + e));
                        }
                    }
                    ctx.getSource().sendSuccess(Component.literal("[dumpworldjei] " + ok + "/" + PARTS.size()
                            + " parts -> kubejs/exported/world_jei/").withStyle(ChatFormatting.GREEN), false);
                    return ok;
                }));
    }

    private static void write(String name, JsonElement json) throws IOException {
        Path dir = FMLPaths.GAMEDIR.get().resolve("kubejs/exported/world_jei");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(name + ".json"), GSON.toJson(json) + "\n", StandardCharsets.UTF_8);
    }
}
