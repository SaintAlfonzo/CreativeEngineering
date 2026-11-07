package com.SaintAlfonzo.creativecompat.compat;

import com.SaintAlfonzo.creativecompat.CreativeCompat;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.SaintAlfonzo.creativecommon.CommonConfig;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.component.PairComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * WTHIT plugin: what stands on a Brewin' and Chewin' coaster (the 4.5.0 port). WTHIT's header already names the
 * top item (the coaster's clone stack), so the body adds "Coaster: 3 / 4", with "(put away)" once a sneak-use has
 * taken the coaster itself back, and one line per item in the order they went on ("2x Beer" for repeats).
 *
 * <p>Client-side only: the port's coaster syncs its four slots to the client. They are read through the block
 * entity's own {@code getItems()} by reflection, so patcherMod needn't compile against the port. Off with
 * {@code [coaster] wthit}. feature_writeup/alcohol.md 12.
 */
public final class WthitCoaster implements IWailaClientPlugin, IBlockComponentProvider {

    private static final ResourceLocation COASTER = new ResourceLocation("brewinandchewin", "coaster");
    private static final ResourceLocation LINE = new ResourceLocation(CreativeCompat.MOD_ID, "coaster");
    private static final int SLOTS = 4;
    private static final Map<Class<?>, Method> GET_ITEMS = new ConcurrentHashMap<>();
    private static volatile Block coaster;

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, Block.class);
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        if (state.getBlock() != coaster() || !com.SaintAlfonzo.creativecompat.CompatConfig.COASTER.wthit.get()) {
            return;
        }
        List<ItemStack> items = items(accessor.getBlockEntity());
        if (items == null) {
            return;
        }
        MutableComponent count = Component.literal(items.size() + " / " + SLOTS);
        if (isPutAway(state)) {
            count.append(" ").append(Component.translatable("creativecommon.wthit.coaster.put_away"));
        }
        tooltip.setLine(LINE, new PairComponent(Component.translatable("creativecommon.wthit.coaster"), count));

        // Repeats of the same item (and name) fold into one line, kept at the place the first one went on.
        List<ItemStack> kinds = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        outer:
        for (ItemStack stack : items) {
            for (int k = 0; k < kinds.size(); k++) {
                if (ItemStack.isSame(kinds.get(k), stack) && kinds.get(k).getHoverName().equals(stack.getHoverName())) {
                    counts.set(k, counts.get(k) + 1);
                    continue outer;
                }
            }
            kinds.add(stack);
            counts.add(1);
        }
        for (int k = 0; k < kinds.size(); k++) {
            Component name = kinds.get(k).getHoverName();
            tooltip.addLine(counts.get(k) > 1
                    ? Component.translatable("creativecommon.wthit.coaster.stack", counts.get(k), name)
                    : name.copy());
        }
    }

    private static Block coaster() {
        Block b = coaster;
        if (b == null) {
            b = ForgeRegistries.BLOCKS.getValue(COASTER);
            coaster = b;
        }
        return b;
    }

    private static boolean isPutAway(BlockState state) {
        Property<?> p = state.getBlock().getStateDefinition().getProperty("invisible");
        return p instanceof BooleanProperty b && state.getValue(b);
    }

    /** The non-empty slots, bottom first; null when the block entity isn't readable. */
    private static List<ItemStack> items(BlockEntity be) {
        if (be == null) {
            return null;
        }
        Method m = GET_ITEMS.computeIfAbsent(be.getClass(), c -> {
            try {
                return c.getMethod("getItems");
            } catch (NoSuchMethodException e) {
                return null;
            }
        });
        if (m == null) {
            return null;
        }
        try {
            List<ItemStack> out = new ArrayList<>();
            for (Object o : (List<?>) m.invoke(be)) {
                if (o instanceof ItemStack s && !s.isEmpty()) {
                    out.add(s);
                }
            }
            return out;
        } catch (ReflectiveOperationException | ClassCastException e) {
            return null;
        }
    }
}
