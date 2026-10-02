package com.SaintAlfonzo.creativeengineering.surface;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.logging.LogUtils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Blocks that slow you while you're inside them: their {@code entityInside} calls
 * {@code Entity#makeStuckInBlock} (cobwebs, berry bushes, leaf piles...). That multiplies the move and
 * also zeroes your momentum every tick, so the speed you really get is
 * {@code multiplier * (1 - 0.91 * friction)} of normal ({@link #walkRatio}), about 0.45x the
 * multiplier on a normal floor.
 *
 * <p>The table is keyed by block class (superclasses count, so a modded {@code WebBlock} subclass is a
 * cobweb too). Multipliers and conditions were read from each class's {@code entityInside} bytecode
 * (2026-09-27, the pack's current jars); only the horizontal part is kept. Display only: nothing here
 * changes movement.
 */
final class StuckInBlock {

    private StuckInBlock() {}

    /** Horizontal multiplier for this state, or {@link Double#NaN} when it doesn't slow you. */
    interface Rule {
        double horizontal(BlockState state, @Nullable Player player, boolean standingOnIt);
    }

    private static final double NONE = Double.NaN;

    /** Class name -> rule. Vanilla by class, modded by name (not on the compile classpath). */
    private static final Map<String, Rule> RULES = new HashMap<>();

    static {
        Rule cobweb = (s, p, on) -> 0.25;
        RULES.put(WebBlock.class.getName(), cobweb);
        RULES.put("net.geforcemods.securitycraft.blocks.reinforced.ReinforcedCobwebBlock", cobweb); // owner passes freely
        RULES.put("xyz.apex.forge.fantasyfurniture.common.block.decorations.SpiderWebBlock", cobweb);
        RULES.put("xyz.apex.forge.fantasyfurniture.common.block.decorations.SpiderWebMultiBlock", cobweb);
        RULES.put("com.mrh0.createaddition.blocks.barbed_wire.BarbedWireBlock", cobweb);

        Rule bush = (s, p, on) -> 0.8;
        RULES.put(SweetBerryBushBlock.class.getName(), bush);
        RULES.put("umpaz.nethersdelight.common.block.util.PropelplantBlock", bush);
        RULES.put("satisfyu.vinery.block.grape.TaigaGrapeBush", bush);

        // only while you're in it; standing on top (leather boots) is the snow tier instead
        RULES.put(PowderSnowBlock.class.getName(), (s, p, on) -> on ? NONE : 0.9);
        RULES.put("net.mehvahdjukaar.supplementaries.common.block.blocks.BambooSpikesBlock",
                (s, p, on) -> p != null && p.isCreative() ? NONE : 0.95);
        RULES.put("com.ninni.twigs.block.BambooLeavesBlock", (s, p, on) -> layers(s) > 2 ? 0.75 : NONE);
        RULES.put("com.ordana.immersive_weathering.blocks.LeafPileBlock", StuckInBlock::leafPile);
    }

    /** Block -> its rule (empty = none), resolved once per block. */
    private static final Map<Block, Optional<Rule>> RULE_OF = new ConcurrentHashMap<>();

    /** Horizontal stuck multiplier for this state, or NaN when the block doesn't slow you. */
    static double horizontal(BlockState state, @Nullable Player player, boolean standingOnIt) {
        return RULE_OF.computeIfAbsent(state.getBlock(), StuckInBlock::findRule)
                .map(r -> r.horizontal(state, player, standingOnIt))
                .orElse(NONE);
    }

    /**
     * Walk speed through the block relative to normal, for a stuck multiplier over a floor with this
     * friction. Normal steady speed is {@code a / (1 - 0.91f)}; stuck, each tick is just {@code a * m}.
     */
    static double walkRatio(double multiplier, float friction) {
        return multiplier * (1.0 - 0.91 * friction);
    }

    private static Optional<Rule> findRule(Block block) {
        for (Class<?> c = block.getClass(); c != null && c != Block.class; c = c.getSuperclass()) {
            Rule r = RULES.get(c.getName());
            if (r != null) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    private static int layers(BlockState state) {
        for (Property<?> p : state.getProperties()) {
            if (p instanceof IntegerProperty ip && "layers".equals(p.getName())) {
                return state.getValue(ip);
            }
        }
        return 0;
    }

    // Immersive Weathering LeafPileBlock: over 3 layers, COLLISIONS[layers - 1], unless its
    // leaf_piles_slow config is off or you wear Depth Strider.
    private static final float[] LEAF_PILE = {1.0f, 0.999f, 0.998f, 0.997f, 0.996f, 0.994f, 0.993f, 0.992f};

    private static double leafPile(BlockState state, @Nullable Player player, boolean standingOnIt) {
        int layers = layers(state);
        if (layers <= 3 || !LeafPileConfig.slow()) {
            return NONE;
        }
        if (player != null && EnchantmentHelper.getEnchantmentLevel(Enchantments.DEPTH_STRIDER, player) > 0) {
            return NONE;
        }
        return LEAF_PILE[Math.min(layers, LEAF_PILE.length) - 1];
    }

    /** IW's {@code CommonConfigs.LEAF_PILES_SLOW}, looked up once; true if it can't be read. */
    private static final class LeafPileConfig {
        @Nullable
        static final Supplier<?> SUPPLIER = find();

        static boolean slow() {
            try {
                return SUPPLIER == null || !Boolean.FALSE.equals(SUPPLIER.get());
            } catch (RuntimeException e) {
                return true;
            }
        }

        @Nullable
        private static Supplier<?> find() {
            try {
                Object value = Class.forName("com.ordana.immersive_weathering.configs.CommonConfigs")
                        .getField("LEAF_PILES_SLOW").get(null);
                return value instanceof Supplier<?> s ? s : null;
            } catch (ReflectiveOperationException | LinkageError e) {
                LogUtils.getLogger().debug("Immersive Weathering leaf_piles_slow not readable, assuming true", e);
                return null;
            }
        }
    }
}
