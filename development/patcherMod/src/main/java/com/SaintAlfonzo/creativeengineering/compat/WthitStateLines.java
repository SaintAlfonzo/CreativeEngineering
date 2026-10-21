package com.SaintAlfonzo.creativeengineering.compat;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.component.PairComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * WTHIT plugin: numbers blocks keep in their state and WTHIT never prints. "Servings: 3 / 4" on feasts, "Slices: 5 / 7"
 * on cakes and pies, "Eggs: 3 / 6" and "Hatching: 1 / 2" on nests and eggs, "Level: 2 / 3" on cauldrons WTHIT has no
 * fluid bar for, respawn anchor charges, sea pickles. Which blocks and how to read them is
 * {@code assets/creativeengineering/state_lines.json} ({@link StateLines}; copy it to
 * {@code kubejs/assets/creativeengineering/} to override). Client-side only: block state is always on the client.
 * Off with {@code [wthit] stateLines}, one line at a time in {@code [wthit.state_lines]}. feature_writeup/wthit_lines.md 2.5.
 */
public final class WthitStateLines implements IWailaClientPlugin, IBlockComponentProvider {

    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation RULES = new ResourceLocation(CreativeEngineering.MODID, "state_lines.json");
    private static final Map<Block, List<Bound>> CACHE = new ConcurrentHashMap<>();
    private static volatile List<StateLines.Rule> rules;
    private static boolean listening;
    private static boolean hooked;

    /** A rule bound to one block's property. */
    private record Bound(IntegerProperty property, StateLines.Mode mode, int min, int top, ResourceLocation line,
                         String label) {}

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, Block.class);
        if (!listening) {
            listening = true;
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, TagsUpdatedEvent.class, event -> reset());
        }
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (!CEConfig.WTHIT.stateLines.get()) {
            return;
        }
        BlockState state = accessor.getBlockState();
        for (Bound b : CACHE.computeIfAbsent(state.getBlock(), WthitStateLines::bind)) {
            ForgeConfigSpec.BooleanValue on = CEConfig.WTHIT.stateLine.get(b.label());
            if (on != null && !on.get()) {
                continue;
            }
            StateLines.Shown shown = StateLines.evaluate(b.mode(), state.getValue(b.property()), b.min(), b.top());
            tooltip.setLine(b.line(), new PairComponent(
                    Component.translatable("creativeengineering.wthit.state." + b.label()),
                    Component.literal(shown.value() + " / " + shown.max())));
        }
    }

    private static List<Bound> bind(Block block) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        if (key == null) {
            return List.of();
        }
        String id = key.toString();
        BlockState base = block.defaultBlockState();
        List<Bound> out = new ArrayList<>();
        Set<String> taken = new HashSet<>(); // labels and properties already shown: one line each
        for (StateLines.Rule rule : rules()) {
            if (taken.contains("label:" + rule.label()) || taken.contains("property:" + rule.property())
                    || rule.excludes(id) || !(rule.matchesId(id) || matchesTag(base, rule.tags()))) {
                continue;
            }
            Property<?> p = block.getStateDefinition().getProperty(rule.property());
            if (!(p instanceof IntegerProperty ip)) {
                continue;
            }
            taken.add("label:" + rule.label());
            taken.add("property:" + rule.property());
            out.add(new Bound(ip, rule.mode(), Collections.min(ip.getPossibleValues()),
                    Collections.max(ip.getPossibleValues()),
                    new ResourceLocation(CreativeEngineering.MODID, "state_" + rule.label()), rule.label()));
        }
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    private static boolean matchesTag(BlockState base, List<String> tags) {
        for (String t : tags) {
            if (base.is(TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(t)))) {
                return true;
            }
        }
        return false;
    }

    private static List<StateLines.Rule> rules() {
        List<StateLines.Rule> current = rules;
        if (current != null) {
            return current;
        }
        hook();
        try {
            Resource res = Minecraft.getInstance().getResourceManager().getResource(RULES).orElseThrow();
            try (Reader in = new InputStreamReader(res.open(), StandardCharsets.UTF_8)) {
                current = StateLines.parse(JsonParser.parseReader(in).getAsJsonObject());
            }
        } catch (Exception e) {
            LOGGER.error("[state-lines] can't read {}: {}", RULES, e.toString());
            current = List.of();
        }
        rules = current;
        return current;
    }

    /** Re-read the rules after a resource reload (F3+T, a resource pack change). */
    private static void hook() {
        if (!hooked && Minecraft.getInstance().getResourceManager() instanceof ReloadableResourceManager rm) {
            hooked = true;
            rm.registerReloadListener((ResourceManagerReloadListener) manager -> reset());
        }
    }

    private static void reset() {
        rules = null;
        CACHE.clear();
    }
}
