package com.SaintAlfonzo.creativeengineering.crops;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * WTHIT plugin: the "Growth: 60%" / "Growth: Mature" line for plants WTHIT doesn't cover, worded and
 * toggled exactly like WTHIT's own (it reuses the "Crop progress" option and its lang keys), so a
 * hemp field reads like a wheat field. Plants that can be picked before they are fully grown add
 * "(Harvestable)". Client-side only, display only: the blockstate already reaches the client.
 *
 * <p>Which blocks and how to read them is {@code assets/creativeengineering/crop_growth.json}
 * ({@link CropGrowth}); copy it to {@code kubejs/assets/creativeengineering/} to change it without
 * rebuilding. {@code [crops.wthit] fallback} adds any other {@code BushBlock} with an integer
 * {@code age}, so a plant from a new mod isn't silently missing.
 * {@code /dumpcropgrowth} lists every growing block and which ones WTHIT leaves out.
 *
 * <p>Priority: a block WTHIT already covers is skipped here, so no block gets two lines.
 */
public final class WthitCropGrowth implements IWailaClientPlugin, IBlockComponentProvider {

    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation RULES = new ResourceLocation(CreativeEngineering.MODID, "crop_growth.json");
    /** WTHIT's own "Crop progress" option and line id, so one toggle and one line slot serve both. */
    private static final ResourceLocation LINE = new ResourceLocation("plant.crop_progress");

    private static final Resolved NONE = new Resolved(null, List.of(), 0, 0, -1, null);
    private static final Map<Block, Resolved> CACHE = new ConcurrentHashMap<>();
    private static volatile List<CropGrowth.Rule> rules;
    private static boolean listening;
    private static boolean hooked;

    /** A rule bound to one block's properties. */
    private record Resolved(CropGrowth.Rule rule, List<Property<?>> props, int max, int harvest, int empty,
                            List<String> stages) {

        /** Summed growth value, or -1 when a property holds a value the rule doesn't list. */
        int read(BlockState state) {
            int sum = 0;
            for (Property<?> p : props) {
                int v = valueOf(p, state);
                if (v < 0) {
                    return -1;
                }
                sum += v;
            }
            return sum;
        }

        private int valueOf(Property<?> p, BlockState state) {
            Comparable<?> value = state.getValue(p);
            if (stages != null) {
                return stages.indexOf(nameOf(p, value));
            }
            return value instanceof Integer i ? i : -1;
        }

        boolean passes(BlockState state) {
            if (rule == null || rule.requires().isEmpty()) {
                return true;
            }
            for (Map.Entry<String, String> need : rule.requires().entrySet()) {
                Property<?> p = state.getBlock().getStateDefinition().getProperty(need.getKey());
                if (p == null || !need.getValue().equals(nameOf(p, state.getValue(p)))) {
                    return false;
                }
            }
            return true;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String nameOf(Property p, Comparable<?> value) {
        return p.getName(value);
    }

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, Block.class);
        if (!listening) {
            listening = true;
            // block tags can move a block between rules; a join or /reload refreshes them
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, TagsUpdatedEvent.class,
                    event -> reset());
        }
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (!CEConfig.CROP_GROWTH.enabled.get() || !config.getBoolean(LINE)) {
            return;
        }
        BlockState state = accessor.getBlockState();
        Resolved r = CACHE.computeIfAbsent(state.getBlock(), WthitCropGrowth::resolve);
        if (r == NONE || !r.passes(state)) {
            return;
        }
        int value = r.read(state);
        if (value < 0) {
            return;
        }
        CropGrowth.Display d = CropGrowth.evaluate(value, r.max(), r.harvest(), r.empty());
        if (d == null) {
            return;
        }
        MutableComponent text = d.stage() == CropGrowth.Stage.MATURE
                ? Component.translatable("tooltip.waila.crop_mature")
                : Component.literal(d.percent() + "%");
        if (d.stage() == CropGrowth.Stage.HARVESTABLE && CEConfig.CROP_GROWTH.showHarvestable.get()) {
            text.append(" ").append(Component.translatable("creativeengineering.wthit.crop_harvestable"));
        }
        tooltip.setLine(LINE, new PairComponent(Component.translatable("tooltip.waila.crop_growth"), text));
    }

    /** WTHIT's PlantProvider covers these; a second line would only repeat it. */
    private static boolean coveredByWthit(Block block) {
        return block instanceof CropBlock
                || block == Blocks.MELON_STEM || block == Blocks.PUMPKIN_STEM || block == Blocks.NETHER_WART
                || block == Blocks.COCOA || block == Blocks.SWEET_BERRY_BUSH;
    }

    private static Resolved resolve(Block block) {
        if (coveredByWthit(block)) {
            return NONE;
        }
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        if (key == null) {
            return NONE;
        }
        BlockState base = block.defaultBlockState();
        String id = key.toString();
        for (CropGrowth.Rule rule : rules()) {
            if (rule.matchesId(id) || matchesTag(base, rule.tags())) {
                Resolved r = bind(block, rule);
                if (r != null) {
                    return r;
                }
                LOGGER.warn("[crop-growth] {} matches a rule whose properties it doesn't have: {}", id, rule.properties());
                return NONE;
            }
        }
        if (CEConfig.CROP_GROWTH.fallback.get() && block instanceof BushBlock && !(block instanceof SaplingBlock)
                && block.getStateDefinition().getProperty(CropGrowth.FALLBACK_PROPERTY) instanceof IntegerProperty age) {
            return new Resolved(null, List.of(age), Collections.max(age.getPossibleValues()),
                    Collections.max(age.getPossibleValues()), -1, null);
        }
        return NONE;
    }

    private static boolean matchesTag(BlockState base, List<String> tags) {
        for (String t : tags) {
            if (base.is(TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(t)))) {
                return true;
            }
        }
        return false;
    }

    private static Resolved bind(Block block, CropGrowth.Rule rule) {
        List<Property<?>> props = new ArrayList<>();
        int top = 0;
        for (String name : rule.properties()) {
            Property<?> p = block.getStateDefinition().getProperty(name);
            if (p == null || rule.stages() == null && !(p instanceof IntegerProperty)) {
                return null;
            }
            props.add(p);
            if (p instanceof IntegerProperty ip) {
                top += Collections.max(ip.getPossibleValues());
            }
        }
        int max = rule.max() >= 0 ? rule.max() : rule.stages() != null ? rule.stages().size() - 1 : top;
        int harvest = rule.harvest() >= 0 ? rule.harvest() : max;
        return new Resolved(rule, props, max, harvest, rule.empty(), rule.stages());
    }

    private static List<CropGrowth.Rule> rules() {
        List<CropGrowth.Rule> current = rules;
        if (current != null) {
            return current;
        }
        hook();
        ResourceManager rm = Minecraft.getInstance().getResourceManager();
        try {
            Resource res = rm.getResource(RULES).orElseThrow();
            try (Reader in = new InputStreamReader(res.open(), StandardCharsets.UTF_8)) {
                current = CropGrowth.parse(JsonParser.parseReader(in).getAsJsonObject());
            }
        } catch (Exception e) {
            LOGGER.error("[crop-growth] can't read {}: {}", RULES, e.toString());
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
