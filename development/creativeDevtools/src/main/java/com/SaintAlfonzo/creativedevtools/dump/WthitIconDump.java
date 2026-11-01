package com.SaintAlfonzo.creativedevtools.dump;

import com.SaintAlfonzo.creativedevtools.CreativeDevtools;
import com.SaintAlfonzo.creativedevtools.DevtoolsConfig;

import com.SaintAlfonzo.creativeengineering.compat.BlockModelIcons;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side icon audit for the WTHIT block model icon (feature_writeup/wthit_lines.md): for every block's default
 * state, the clone stack WTHIT would show, whether its item model is missing, the block's render shape, and the
 * {@link BlockModelIcons.Verdict} the plugin reaches; plus every item whose own model is missing (the purple cube in
 * JEI and inventories). Writes {@code kubejs/exported/wthit_icons.json}; tools/wthit_icon_audit.py reads it.
 *
 * <p>The clone stack is asked of an empty world ({@code EmptyBlockGetter}, origin), so a block whose pick item
 * depends on its block entity or neighbours may differ in game; those that throw are listed with {@code error}.
 * Triggers: the client command {@code /cedumpicons}, or {@code [debug.wthit] dumpIconsAtTitle} (once, when the title
 * screen first opens).
 */
@Mod.EventBusSubscriber(modid = CreativeDevtools.MOD_ID, value = Dist.CLIENT)
public final class WthitIconDump {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean dumpedAtTitle;

    private WthitIconDump() {}

    @SubscribeEvent
    public static void onClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cedumpicons").executes(ctx -> {
            String summary = dump();
            ctx.getSource().sendSuccess(Component.literal("[cedumpicons] " + summary
                    + " -> kubejs/exported/wthit_icons.json").withStyle(ChatFormatting.GREEN), false);
            return 1;
        }));
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (dumpedAtTitle || !(event.getNewScreen() instanceof TitleScreen)) return;
        if (!DevtoolsConfig.DEBUG.dumpIconsAtTitle.get()) return;
        dumpedAtTitle = true;
        dump();
    }

    public static String dump() {
        long start = System.nanoTime();
        Minecraft mc = Minecraft.getInstance();
        BakedModel missing = mc.getModelManager().getMissingModel();
        Map<BlockModelIcons.Verdict, Integer> counts = new EnumMap<>(BlockModelIcons.Verdict.class);
        int errors = 0;

        JsonArray blocks = new JsonArray();
        List<ResourceLocation> keys = new ArrayList<>(ForgeRegistries.BLOCKS.getKeys());
        keys.sort(ResourceLocation::compareTo);
        for (ResourceLocation id : keys) {
            Block block = ForgeRegistries.BLOCKS.getValue(id);
            if (block == null) continue;
            BlockState state = block.defaultBlockState();
            JsonObject row = new JsonObject();
            row.addProperty("id", id.toString());
            row.addProperty("class", block.getClass().getName());
            row.addProperty("shape", state.getRenderShape().name());
            row.addProperty("blockModelMissing", mc.getBlockRenderer().getBlockModel(state) == missing);
            row.addProperty("hasBlockEntity", state.hasBlockEntity());
            ItemStack clone;
            try {
                clone = block.getCloneItemStack(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, state);
            } catch (RuntimeException e) {
                row.addProperty("error", e.toString());
                blocks.add(row);
                errors++;
                continue;
            }
            row.addProperty("clone", clone.isEmpty() ? "" : String.valueOf(ForgeRegistries.ITEMS.getKey(clone.getItem())));
            if (!clone.isEmpty()) {
                row.addProperty("cloneModelMissing", !BlockModelIcons.hasItemIcon(clone));
            }
            BlockModelIcons.Verdict v;
            try {
                v = BlockModelIcons.verdict(state, clone);
            } catch (RuntimeException e) {
                row.addProperty("error", e.toString());
                blocks.add(row);
                errors++;
                continue;
            }
            row.addProperty("verdict", v.name());
            counts.merge(v, 1, Integer::sum);
            blocks.add(row);
        }

        JsonArray missingItems = new JsonArray();
        List<ResourceLocation> itemKeys = new ArrayList<>(ForgeRegistries.ITEMS.getKeys());
        itemKeys.sort(ResourceLocation::compareTo);
        for (ResourceLocation id : itemKeys) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item == null || "minecraft:air".equals(id.toString())) continue;
            try {
                if (mc.getItemRenderer().getItemModelShaper().getItemModel(item) == missing) {
                    missingItems.add(id.toString());
                }
            } catch (RuntimeException e) {
                LOGGER.warn("[cedumpicons] item {}: {}", id, e.toString());
            }
        }

        JsonObject totals = new JsonObject();
        counts.forEach((k, n) -> totals.addProperty(k.name(), n));
        totals.addProperty("error", errors);
        totals.addProperty("missingItemModels", missingItems.size());

        JsonObject out = new JsonObject();
        out.addProperty("generated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        out.add("totals", totals);
        out.add("blocks", blocks);
        out.add("missingItemModels", missingItems);

        Path dir = FMLPaths.GAMEDIR.get().resolve("kubejs/exported");
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("wthit_icons.json"),
                    new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(out), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[cedumpicons] could not write {}", dir, e);
            return "write failed: " + e.getMessage();
        }
        String summary = totals.toString();
        LOGGER.info("[cedumpicons] {} ({} ms)", summary, (System.nanoTime() - start) / 1_000_000);
        return summary;
    }
}
