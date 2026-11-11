package com.SaintAlfonzo.creativedevtools.dump;

import com.SaintAlfonzo.creativedevtools.CreativeDevtools;

import com.SaintAlfonzo.creativecompat.surface.RunSpeed;
import com.SaintAlfonzo.creativecompat.surface.SurfaceTier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * BlockRunner surface audit (feature_writeup/blockrunner.md 6): for every block's default state, the collision top,
 * its surface tier, BlockRunner's factor, and the vanilla friction / speed / jump factors. Writes
 * {@code kubejs/exported/surface_audit.json}; {@code tools/blockrunner_scan.py --audit} reads it and lists walkable
 * blocks (collision top at or above {@link RunSpeed#REGISTER_HEIGHT}, with an item) that have no tier, plus native
 * factor outliers.
 *
 * <p>Shapes are asked of an empty world at the origin with an empty collision context, so blocks whose shape depends on
 * neighbours, a block entity or the entity may differ in game; a block that throws is listed with {@code error}.
 */
@Mod.EventBusSubscriber(modid = CreativeDevtools.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SurfaceAudit {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private SurfaceAudit() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dumpsurface")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    try {
                        String summary = dump();
                        ctx.getSource().sendSuccess(Component.literal("[dumpsurface] " + summary
                                + " -> kubejs/exported/surface_audit.json").withStyle(ChatFormatting.GREEN), false);
                        return 1;
                    } catch (IOException e) {
                        LOGGER.error("[dumpsurface] write failed", e);
                        ctx.getSource().sendFailure(Component.literal("[dumpsurface] write failed: " + e));
                        return 0;
                    }
                }));
    }

    public static String dump() throws IOException {
        RunSpeed.clearTierCache(); // read the tags as they are now
        JsonArray blocks = new JsonArray();
        int errors = 0, tiered = 0;
        List<ResourceLocation> keys = new ArrayList<>(ForgeRegistries.BLOCKS.getKeys());
        keys.sort(ResourceLocation::compareTo);
        for (ResourceLocation id : keys) {
            Block block = ForgeRegistries.BLOCKS.getValue(id);
            if (block == null) continue;
            JsonObject row = new JsonObject();
            row.addProperty("id", id.toString());
            row.addProperty("class", block.getClass().getName());
            try {
                BlockState state = block.defaultBlockState();
                VoxelShape shape = state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
                row.addProperty("collisionTop", shape.isEmpty() ? -1.0 : round(shape.max(Direction.Axis.Y)));
                SurfaceTier tier = RunSpeed.tierOf(block);
                double runner = RunSpeed.runnerFactor(block);
                if (tier != null) {
                    row.addProperty("tier", tier.key());
                    tiered++;
                }
                if (!Double.isNaN(runner)) row.addProperty("runnerFactor", round(runner));
                row.addProperty("override", tier == null && !Double.isNaN(runner));
                row.addProperty("friction", round(block.getFriction()));
                row.addProperty("speedFactor", round(block.getSpeedFactor()));
                row.addProperty("jumpFactor", round(block.getJumpFactor()));
                row.addProperty("hasItem", block.asItem() != Items.AIR);
            } catch (RuntimeException e) {
                row.addProperty("error", e.toString());
                errors++;
            }
            blocks.add(row);
        }
        JsonObject root = new JsonObject();
        root.addProperty("generated", Instant.now().toString());
        root.addProperty("registerHeight", round(RunSpeed.REGISTER_HEIGHT));
        root.addProperty("count", blocks.size());
        root.addProperty("errors", errors);
        root.add("blocks", blocks);
        Path out = FMLPaths.GAMEDIR.get().resolve("kubejs/exported/surface_audit.json");
        Files.createDirectories(out.getParent());
        Files.writeString(out, GSON.toJson(root) + "\n", StandardCharsets.UTF_8);
        String summary = blocks.size() + " blocks, " + tiered + " tiered, " + errors + " errors";
        LOGGER.info("[dumpsurface] {}", summary);
        return summary;
    }

    private static double round(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
