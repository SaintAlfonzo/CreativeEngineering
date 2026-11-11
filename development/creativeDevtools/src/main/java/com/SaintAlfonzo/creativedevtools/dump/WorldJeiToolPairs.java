package com.SaintAlfonzo.creativedevtools.dump;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.google.common.collect.BiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

/**
 * {@link WorldJeiDump} part {@code tool_pairs}: what an axe strips / scrapes / un-waxes, what honeycomb waxes, and
 * Supplementaries' in-world dye and soap colour changes. Read by tools/jei_world/tab_tools.py.
 *
 * <p>Axe actions go through {@code BlockState#getToolModifiedState(ctx, action, simulate=true)}, so vanilla tables,
 * block overrides (Immersive Weathering's Rustable mixin, Create, Quark...) and {@code BlockToolModificationEvent}
 * listeners all answer. The context is a fake player holding an iron axe, aimed at the world-bottom block under
 * spawn. Door halves skip the event: everythingcopper's listener writes the other half into the world even when
 * simulating, so doors ask the block (no event) and everythingcopper's own static helpers instead.
 *
 * <p>Output: {@code strip/scrape/wax_off/honeycomb: [{from, to, age?, bark?}]}, {@code color_sets: {key: {colors:
 * {dye: id}, base: id?}}}, {@code dye_blocked: [id]} (block entity or {@code #creativeengineering:no_world_dye}, see
 * SupplementariesDyeBlocksMixin), {@code soap: [{from, to, how}]}, {@code soap_washable: [id]} (blocks with their own
 * soap behaviour), {@code config: {...}} (live Supplementaries / Immersive Weathering switches).
 */
final class WorldJeiToolPairs {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final TagKey<Block> NO_WORLD_DYE = TagKey.create(Registry.BLOCK_REGISTRY,
            new ResourceLocation("creativecompat", "no_world_dye"));
    /** Above this many states only the default state and its axis variants are tried. */
    private static final int MAX_STATES = 48;

    private WorldJeiToolPairs() {}

    static JsonElement dump(ServerLevel level) {
        JsonObject out = new JsonObject();
        JsonObject config = new JsonObject();
        out.add("config", config);

        FakePlayer player = FakePlayerFactory.getMinecraft(level);
        ItemStack axe = new ItemStack(Items.IRON_AXE);
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        player.setItemInHand(InteractionHand.MAIN_HAND, axe);
        BlockPos pos = new BlockPos(level.getSharedSpawnPos().getX(), level.getMinBuildHeight(), level.getSharedSpawnPos().getZ());
        UseOnContext ctx = new UseOnContext(level, player, InteractionHand.MAIN_HAND, axe,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));

        Class<?> ecWeathering = classOrNull("cy.jdkdigital.everythingcopper.common.block.IWeatheringBlock");
        Method ecPrevious = method(ecWeathering, "getPrevious", BlockState.class);
        Method ecUnwaxed = method(ecWeathering, "getUnwaxed", BlockState.class);
        Method ecWaxed = method(ecWeathering, "getWaxed", BlockState.class);
        Method iwBark = method(classOrNull("com.ordana.immersive_weathering.util.WeatheringHelper"), "getBarkToStrip", BlockState.class);

        JsonArray strip = new JsonArray();
        JsonArray scrape = new JsonArray();
        JsonArray waxOff = new JsonArray();
        int errors = 0;
        try {
            for (Block block : Registry.BLOCK) {
                Map<ToolAction, Set<Block>> seen = new LinkedHashMap<>();
                for (BlockState state : candidates(block)) {
                    boolean door = state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF);
                    for (ToolAction action : List.of(ToolActions.AXE_STRIP, ToolActions.AXE_SCRAPE, ToolActions.AXE_WAX_OFF)) {
                        BlockState to;
                        try {
                            to = door ? block.getToolModifiedState(state, ctx, action, true)
                                      : state.getToolModifiedState(ctx, action, true);
                            if (to == null && door && ecWeathering != null && ecWeathering.isInstance(block)) {
                                Method m = action == ToolActions.AXE_SCRAPE ? ecPrevious
                                        : action == ToolActions.AXE_WAX_OFF ? ecUnwaxed : null;
                                to = m == null ? null : optionalState(m.invoke(null, state));
                            }
                        } catch (Throwable e) {
                            if (errors++ < 20) LOGGER.warn("[dumpworldjei] {} {} on {}: {}", action.name(), block, state, e.toString());
                            continue;
                        }
                        if (to == null || to.getBlock() == block) continue;
                        if (!seen.computeIfAbsent(action, a -> new LinkedHashSet<>()).add(to.getBlock())) continue;
                        JsonObject pair = pair(block, to.getBlock());
                        if (action == ToolActions.AXE_STRIP) {
                            Item bark = (Item) invokeQuiet(iwBark, state);
                            if (bark != null && bark != Items.AIR) pair.addProperty("bark", id(bark));
                            strip.add(pair);
                        } else {
                            if (block instanceof ChangeOverTimeBlock<?> aging) pair.addProperty("age", aging.getAge().name().toLowerCase());
                            (action == ToolActions.AXE_SCRAPE ? scrape : waxOff).add(pair);
                        }
                    }
                }
            }
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, held);
        }
        out.add("strip", strip);
        out.add("scrape", scrape);
        out.add("wax_off", waxOff);
        out.addProperty("errors", errors);

        // Honeycomb: vanilla's table (Create, Immersive Weathering and others merge into it) + everythingcopper's own.
        JsonArray honeycomb = new JsonArray();
        Set<String> waxSeen = new LinkedHashSet<>();
        for (Map.Entry<Block, Block> e : HoneycombItem.WAXABLES.get().entrySet()) {
            if (waxSeen.add(id(e.getKey()))) honeycomb.add(pair(e.getKey(), e.getValue()));
        }
        if (ecWeathering != null) {
            for (Block block : Registry.BLOCK) {
                if (!ecWeathering.isInstance(block) || waxSeen.contains(id(block))) continue;
                BlockState waxed = optionalState(invokeQuiet(ecWaxed, block.defaultBlockState()));
                if (waxed != null && waxed.getBlock() != block && waxSeen.add(id(block))) {
                    JsonObject pair = pair(block, waxed.getBlock());
                    pair.addProperty("source", "everythingcopper");
                    honeycomb.add(pair);
                }
            }
        }
        out.add("honeycomb", honeycomb);

        supplementaries(out, config);
        immersiveWeathering(config);
        return out;
    }

    // Supplementaries dye / soap -------------------------------------------------------------------------------

    private static void supplementaries(JsonObject out, JsonObject config) {
        Class<?> api = classOrNull("net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI");
        if (api == null) return;
        Method getKeys = method(api, "getBlockKeys");
        Method holderSet = method(api, "getBlockHolderSet", String.class);
        Method colored = method(api, "getColoredBlock", String.class, DyeColor.class);
        Method changeColor = method(api, "changeColor", Block.class, DyeColor.class);
        Method getKey = method(api, "getKey", Block.class);
        Class<?> tweaks = classOrNull("net.mehvahdjukaar.supplementaries.configs.CommonConfigs$Tweaks");
        Class<?> functional = classOrNull("net.mehvahdjukaar.supplementaries.configs.CommonConfigs$Functional");
        Object dyeOn = configValue(tweaks, "DYE_BLOCKS");
        Object soapOn = configValue(functional, "SOAP_ENABLED");
        Object blacklistObj = configValue(functional, "SOAP_DYE_CLEAN_BLACKLIST");
        if (dyeOn instanceof Boolean b) config.addProperty("dye_blocks", b);
        if (soapOn instanceof Boolean b) config.addProperty("soap", b);
        List<String> blacklist = new ArrayList<>();
        if (blacklistObj instanceof Collection<?> c) for (Object o : c) blacklist.add(String.valueOf(o));
        JsonArray bl = new JsonArray();
        blacklist.forEach(bl::add);
        config.add("soap_blacklist", bl);

        JsonObject sets = new JsonObject();
        JsonArray dyeBlocked = new JsonArray();
        Set<String> blockedSeen = new LinkedHashSet<>();
        Object keysObj = invokeQuiet(getKeys);
        if (keysObj instanceof Collection<?> keys) {
            for (Object keyObj : keys) {
                String key = String.valueOf(keyObj);
                JsonObject set = new JsonObject();
                JsonObject colors = new JsonObject();
                for (DyeColor dye : DyeColor.values()) {
                    Object b = invokeQuiet(colored, key, dye);
                    if (b instanceof Block block && block != Blocks.AIR) colors.addProperty(dye.getName(), id(block));
                }
                set.add("colors", colors);
                JsonArray members = new JsonArray();
                Block any = null;
                if (invokeQuiet(holderSet, key) instanceof Iterable<?> holders) {
                    for (Object h : holders) {
                        if (!(h instanceof Holder<?> holder) || !(holder.value() instanceof Block block)) continue;
                        members.add(id(block));
                        if (any == null) any = block;
                        if ((block.defaultBlockState().hasBlockEntity() || block.defaultBlockState().is(NO_WORLD_DYE))
                                && blockedSeen.add(id(block))) dyeBlocked.add(id(block));
                    }
                }
                set.add("members", members);
                if (any != null && invokeQuiet(changeColor, any, null) instanceof Block base && base != Blocks.AIR) {
                    set.addProperty("base", id(base));
                    if (base.defaultBlockState().hasBlockEntity() || base.defaultBlockState().is(NO_WORLD_DYE)) {
                        if (blockedSeen.add(id(base))) dyeBlocked.add(id(base));
                    }
                }
                sets.add(key, set);
            }
        }
        out.add("color_sets", sets);
        out.add("dye_blocked", dyeBlocked);

        // Soap: SoapWashableHelper.tryWash order is IWashable, then colour (blacklist, no beds), then un-oxidise.
        Class<?> washable = classOrNull("net.mehvahdjukaar.moonlight.api.block.IWashable");
        Method tryParse = method(classOrNull("net.mehvahdjukaar.supplementaries.common.misc.SoapWashableHelper"),
                "tryParse", BlockState.class);
        BiMap<Block, Block> unwax = HoneycombItem.WAXABLES.get().inverse();
        JsonArray soap = new JsonArray();
        JsonArray washables = new JsonArray();
        for (Block block : Registry.BLOCK) {
            if (washable != null && washable.isInstance(block)) {
                washables.add(id(block));
                continue;
            }
            BlockState state = block.defaultBlockState();
            Block to = null;
            String how = null;
            if (!(block instanceof BedBlock) && invokeQuiet(changeColor, block, null) instanceof Block clean
                    && clean != Blocks.AIR && !blacklist.contains(String.valueOf(invokeQuiet(getKey, block)))) {
                to = clean;
                how = "color";
            }
            if (to == null || to == block) {
                to = null;
                if (block == Blocks.STICKY_PISTON) {
                    to = Blocks.PISTON;
                    how = "piston";
                } else {
                    Block unwaxed = unwax.getOrDefault(block, block);
                    Block first = WeatheringCopper.getFirst(unwaxed);
                    if (first != block) {
                        to = first;
                        how = "unoxidise";
                    } else {
                        BlockState parsed = (BlockState) invokeQuiet(tryParse, state);
                        if (parsed != null && parsed.getBlock() != block) {
                            to = parsed.getBlock();
                            how = "name";
                        }
                    }
                }
            }
            if (to == null || to == block) continue;
            JsonObject pair = pair(block, to);
            pair.addProperty("how", how);
            soap.add(pair);
        }
        out.add("soap", soap);
        out.add("soap_washable", washables);
    }

    private static void immersiveWeathering(JsonObject config) {
        Class<?> cfg = classOrNull("com.ordana.immersive_weathering.configs.CommonConfigs");
        if (cfg == null) return;
        for (String name : List.of("AXE_STRIPPING", "AXE_SCRAPING", "SPONGE_RUSTING")) {
            if (configValue(cfg, name) instanceof Boolean b) config.addProperty("iw_" + name.toLowerCase(), b);
        }
    }

    // Helpers ----------------------------------------------------------------------------------------------------

    private static Collection<BlockState> candidates(Block block) {
        List<BlockState> all = block.getStateDefinition().getPossibleStates();
        if (all.size() <= MAX_STATES) return all;
        Set<BlockState> some = new LinkedHashSet<>();
        BlockState def = block.defaultBlockState();
        some.add(def);
        if (def.hasProperty(BlockStateProperties.AXIS)) {
            for (Axis axis : Axis.values()) some.add(def.setValue(BlockStateProperties.AXIS, axis));
        }
        return some;
    }

    private static JsonObject pair(Block from, Block to) {
        JsonObject o = new JsonObject();
        o.addProperty("from", id(from));
        o.addProperty("to", id(to));
        return o;
    }

    private static String id(Block block) {
        return Registry.BLOCK.getKey(block).toString();
    }

    private static String id(Item item) {
        return Registry.ITEM.getKey(item).toString();
    }

    private static BlockState optionalState(Object o) {
        return o instanceof Optional<?> opt && opt.isPresent() && opt.get() instanceof BlockState s ? s : null;
    }

    private static Class<?> classOrNull(String name) {
        try {
            return Class.forName(name);
        } catch (Throwable e) {
            return null;
        }
    }

    private static Method method(Class<?> owner, String name, Class<?>... args) {
        if (owner == null) return null;
        try {
            Method m = owner.getDeclaredMethod(name, args);
            m.setAccessible(true);
            return m;
        } catch (Throwable e) {
            LOGGER.warn("[dumpworldjei] no {}#{}: {}", owner.getName(), name, e.toString());
            return null;
        }
    }

    private static Object invokeQuiet(Method m, Object... args) {
        if (m == null) return null;
        try {
            return m.invoke(null, args);
        } catch (Throwable e) {
            return null;
        }
    }

    /** A mod config's static {@code Supplier} field, resolved. */
    private static Object configValue(Class<?> owner, String field) {
        if (owner == null) return null;
        try {
            Field f = owner.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(null) instanceof Supplier<?> s ? s.get() : null;
        } catch (Throwable e) {
            LOGGER.warn("[dumpworldjei] no {}.{}: {}", owner.getName(), field, e.toString());
            return null;
        }
    }
}
