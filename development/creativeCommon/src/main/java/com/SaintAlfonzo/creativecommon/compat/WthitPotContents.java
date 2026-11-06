package com.SaintAlfonzo.creativecommon.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecommon.CommonConfig;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.WailaConstants;
import mcp.mobius.waila.api.component.PairComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.registries.ForgeRegistries;
import xfacthd.framedblocks.common.blockentity.FramedFlowerPotBlockEntity;

/**
 * WTHIT plugin: "Contains: Poppy" on every block that holds plants (feature_writeup/flower_pots.md 3.3): vanilla and
 * modded {@code potted_*} pots, the framed pot, Supplementaries' hanging pot and flower box, Another Furniture's
 * planter boxes, Beautify's hanging pot and botanist workbench, Floral Enchantment's large pots, Vinery's pot and box.
 * Several plants in slot order; the same plant twice as "2 Poppy". A Twigs silt pot shows "Rooted Dirt: Yes / No"
 * instead (plants grow on it only when filled); planters show nothing (the plant is its own block above them). Also names a Beautify hanging pot standing on the ground "Rich Pot".
 *
 * <p>Client-side only: every one of these syncs its plants to the client for its renderer. Off with
 * {@code [wthit] potContents}.
 */
public final class WthitPotContents implements IWailaClientPlugin, IBlockComponentProvider {

    private static final ResourceLocation LINE = new ResourceLocation("creativecommon", "pot_contents");
    private static final ResourceLocation BEAUTIFY_POT = new ResourceLocation("beautify", "hanging_pot");
    /** Another Furniture's planter box block entity (also used by Every Compat's wood variants, or subclassed). */
    private static final String AF_PLANTER_BE = "com.starfish_studios.another_furniture.block.entity.PlanterBoxBlockEntity";

    private static volatile List<?> beautifyFlowers;
    private static volatile boolean beautifyLookedUp;
    private static volatile Method afSlot;
    private static volatile Field vineryInventory;

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.head(this, Block.class, 2000);
        registrar.body(this, Block.class);
    }

    @Override
    public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (!CommonConfig.WTHIT.potContents.get()) return;
        BlockState state = accessor.getBlockState();
        if (!BEAUTIFY_POT.equals(ForgeRegistries.BLOCKS.getKey(state.getBlock()))) return;
        if (state.hasProperty(BlockStateProperties.HANGING) && !state.getValue(BlockStateProperties.HANGING)) {
            tooltip.setLine(WailaConstants.OBJECT_NAME_TAG,
                    Component.translatable("creativecommon.wthit.rich_pot").withStyle(ChatFormatting.WHITE));
        }
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (!CommonConfig.WTHIT.potContents.get()) return;
        Boolean filled = siltPotFilled(accessor.getBlockState());
        if (filled != null) {
            tooltip.setLine(LINE, new PairComponent(Component.translatable("creativecommon.wthit.rooted_dirt"),
                    Component.translatable(filled ? "creativecommon.wthit.yes" : "creativecommon.wthit.no")));
            return;
        }
        List<Item> plants = plants(accessor.getBlockState(), accessor.getBlockEntity());
        if (plants.isEmpty()) return;
        Map<Item, Integer> counted = new LinkedHashMap<>();
        for (Item item : plants) counted.merge(item, 1, Integer::sum);
        MutableComponent text = Component.empty();
        boolean first = true;
        for (Map.Entry<Item, Integer> e : counted.entrySet()) {
            if (!first) text.append(", ");
            first = false;
            Component name = new ItemStack(e.getKey()).getHoverName();
            text.append(e.getValue() > 1
                    ? Component.translatable("creativecommon.wthit.contents.kind", e.getValue(), name) : name);
        }
        tooltip.setLine(LINE, new PairComponent(Component.translatable("creativecommon.wthit.pot_contains"), text));
    }

    /** A Twigs silt pot: whether it holds rooted dirt (then plants grow on it); null for any other block. */
    @Nullable
    static Boolean siltPotFilled(BlockState state) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (id == null || !"twigs".equals(id.getNamespace()) || !id.getPath().endsWith("silt_pot")) return null;
        for (var property : state.getProperties()) {
            if (property.getName().equals("filled") && property.getValueClass() == Boolean.class) {
                return (Boolean) state.getValue(property);
            }
        }
        return null;
    }

    /** The plants in a holder, empty for anything else. */
    static List<Item> plants(BlockState state, @Nullable BlockEntity be) {
        List<Item> out = new ArrayList<>();
        Block block = state.getBlock();
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        if (block instanceof FlowerPotBlock pot) {
            addBlock(out, pot.getContent());
        } else if (be instanceof FramedFlowerPotBlockEntity framed) {
            if (framed.hasFlowerBlock()) addBlock(out, framed.getFlowerBlock());
        } else if (be instanceof PottedPlantHolder bench) {
            if (bench.plant() != null) addBlock(out, ForgeRegistries.BLOCKS.getValue(bench.plant()));
        } else if (id != null && "supplementaries".equals(id.getNamespace()) && be != null) {
            SuppPots.plants(be, out);
        } else if (BEAUTIFY_POT.equals(id)) {
            beautify(state, out);
        } else if (id != null && "floralench".equals(id.getNamespace())) {
            floral(id.getPath(), out);
        } else if (be != null && isAfPlanter(be.getClass())) {
            anotherFurniture(be, out);
        } else if (id != null && "vinery".equals(id.getNamespace())
                && (id.getPath().equals("flower_box") || id.getPath().equals("flower_pot")) && be != null) {
            vinery(be, out);
        }
        return out;
    }

    static void addBlock(List<Item> out, @Nullable Block block) {
        if (block == null || block == Blocks.AIR) return;
        Item item = block.asItem();
        if (item != Items.AIR) out.add(item);
    }

    static void addStack(List<Item> out, ItemStack stack) {
        if (!stack.isEmpty()) out.add(stack.getItem());
    }

    /** potflower is an index into HangingPot.validFlowers (0 = air). */
    private static void beautify(BlockState state, List<Item> out) {
        for (var property : state.getProperties()) {
            if (!(property instanceof IntegerProperty p) || !p.getName().equals("potflower")) continue;
            int index = state.getValue(p);
            List<?> flowers = beautifyFlowers();
            if (flowers != null && index > 0 && index < flowers.size() && flowers.get(index) instanceof Item item) {
                if (item != Items.AIR) out.add(item);
            }
        }
    }

    @Nullable
    private static List<?> beautifyFlowers() {
        if (!beautifyLookedUp) {
            beautifyLookedUp = true;
            try {
                Field f = Class.forName("com.github.Pandarix.beautify.common.block.HangingPot").getDeclaredField("validFlowers");
                f.setAccessible(true);
                beautifyFlowers = (List<?>) f.get(null);
            } catch (ReflectiveOperationException | RuntimeException e) {
                beautifyFlowers = null;
            }
        }
        return beautifyFlowers;
    }

    /** large_poppy_pot = Poppy, large_double_poppy_pot = 2 Poppy, large_pot = empty. */
    private static void floral(String path, List<Item> out) {
        if (!path.startsWith("large_") || !path.endsWith("_pot") || path.equals("large_pot")) return;
        String plant = path.substring("large_".length(), path.length() - "_pot".length());
        int n = 1;
        if (plant.startsWith("double_")) {
            plant = plant.substring("double_".length());
            n = 2;
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", plant));
        if (item == null || item == Items.AIR) return;
        for (int i = 0; i < n; i++) out.add(item);
    }

    private static boolean isAfPlanter(Class<?> c) {
        for (; c != null; c = c.getSuperclass()) if (c.getName().equals(AF_PLANTER_BE)) return true;
        return false;
    }

    private static void anotherFurniture(BlockEntity be, List<Item> out) {
        try {
            Method m = afSlot;
            if (m == null) {
                m = be.getClass().getMethod("getItemFromSlot", int.class);
                afSlot = m;
            }
            for (int i = 0; i < 2; i++) {
                if (m.invoke(be, i) instanceof Item item && item != Items.AIR) out.add(item);
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            // not the expected class: no line
        }
    }

    private static void vinery(BlockEntity be, List<Item> out) {
        try {
            Field f = vineryInventory;
            if (f == null) {
                Class<?> c = be.getClass();
                while (c != null && f == null) {
                    try {
                        f = c.getDeclaredField("inventory");
                    } catch (NoSuchFieldException e) {
                        c = c.getSuperclass();
                    }
                }
                if (f == null) return;
                f.setAccessible(true);
                vineryInventory = f;
            }
            if (f.get(be) instanceof List<?> stacks) {
                for (Object o : stacks) if (o instanceof ItemStack s) addStack(out, s);
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            // no line
        }
    }

    /** Supplementaries classes, only touched for blocks in its namespace. */
    private static final class SuppPots {
        static void plants(BlockEntity be, List<Item> out) {
            if (be instanceof net.mehvahdjukaar.supplementaries.common.block.tiles.HangingFlowerPotBlockTile hanging) {
                if (hanging.getHeldBlock(0).getBlock() instanceof FlowerPotBlock pot) addBlock(out, pot.getContent());
            } else if (be instanceof net.mehvahdjukaar.supplementaries.common.block.tiles.FlowerBoxBlockTile box) {
                Container items = (Container) box;
                for (int i = 0; i < items.getContainerSize(); i++) addStack(out, items.getItem(i));
            }
        }
    }
}
