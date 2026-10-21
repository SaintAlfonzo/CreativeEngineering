package com.SaintAlfonzo.creativeengineering.compat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.SaintAlfonzo.creativeengineering.CEConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Which blocks get their own model as the WTHIT icon ({@link WthitBlockModelIcon}), kept free of WTHIT types so
 * devtools/WthitIconDump can ask the same question for every block.
 *
 * <p>The rule: WTHIT's core icon is the block's vanilla clone stack. When that stack is empty (the block has no
 * item) or its item draws as the missing model: a block listed in {@code [wthit] iconExclude} keeps WTHIT's icon; one
 * in {@code [wthit] iconItems} shows that item; otherwise the plugin tries Forge's hit-aware pick block (which this
 * check can't, having no hit), and failing that draws the block's own baked model. An invisible or
 * block-entity-rendered block, or one whose own model is missing, keeps WTHIT's icon.
 */
public final class BlockModelIcons {

    /** Why a block does or doesn't get the model icon; the dump writes these names. */
    public enum Verdict {
        /** the clone stack has a real item model: WTHIT's own icon */
        ITEM,
        /** drawn by the plugin */
        MODEL,
        /** listed in {@code [wthit] iconExclude} */
        EXCLUDED,
        /** {@code [wthit] iconItems} names an item for it */
        SUBSTITUTE,
        /** no item icon, and the block isn't drawn as a baked model (invisible, or a block-entity renderer) */
        NOT_MODEL,
        /** no item icon, and the block's own model is missing too */
        MISSING_MODEL
    }

    private static volatile List<? extends String> excludeSource;
    private static volatile Set<String> exclude = Set.of();
    private static volatile List<? extends String> itemsSource;
    private static volatile Map<ResourceLocation, ResourceLocation> items = Map.of();

    private BlockModelIcons() {}

    public static Verdict verdict(BlockState state, ItemStack clone) {
        if (hasItemIcon(clone)) {
            return Verdict.ITEM;
        }
        if (isExcluded(ForgeRegistries.BLOCKS.getKey(state.getBlock()))) {
            return Verdict.EXCLUDED;
        }
        if (!substitute(state).isEmpty()) {
            return Verdict.SUBSTITUTE;
        }
        if (state.getRenderShape() != RenderShape.MODEL) {
            return Verdict.NOT_MODEL;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.getBlockRenderer().getBlockModel(state) == mc.getModelManager().getMissingModel()) {
            return Verdict.MISSING_MODEL;
        }
        return Verdict.MODEL;
    }

    /** The state the icon draws: facing pinned to north, as block items usually show it. */
    public static BlockState iconState(BlockState state) {
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        } else if (state.hasProperty(BlockStateProperties.FACING)) {
            state = state.setValue(BlockStateProperties.FACING, Direction.NORTH);
        }
        return state;
    }

    public static boolean hasItemIcon(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        return mc.getItemRenderer().getModel(stack, null, null, 0) != mc.getModelManager().getMissingModel();
    }

    /** The {@code [wthit] iconItems} item for this block, or empty. */
    public static ItemStack substitute(BlockState state) {
        if (!CEConfig.WTHIT.iconSubstitutes.get()) {
            return ItemStack.EMPTY;
        }
        List<? extends String> list = CEConfig.WTHIT.iconItems.get();
        if (list != itemsSource) {
            Map<ResourceLocation, ResourceLocation> m = new HashMap<>();
            for (String e : list) {
                String[] kv = e.split("=", 2);
                ResourceLocation block = kv.length == 2 ? ResourceLocation.tryParse(kv[0].trim()) : null;
                ResourceLocation item = kv.length == 2 ? ResourceLocation.tryParse(kv[1].trim()) : null;
                if (block != null && item != null) {
                    m.put(block, item);
                }
            }
            items = m;
            itemsSource = list;
        }
        ResourceLocation item = items.get(ForgeRegistries.BLOCKS.getKey(state.getBlock()));
        Item it = item == null ? null : ForgeRegistries.ITEMS.getValue(item);
        return it == null || it == Items.AIR ? ItemStack.EMPTY : new ItemStack(it);
    }

    private static boolean isExcluded(ResourceLocation id) {
        if (id == null) {
            return true;
        }
        List<? extends String> list = CEConfig.WTHIT.iconExclude.get();
        if (list != excludeSource) { // re-read only when the config list changes
            Set<String> s = new HashSet<>();
            for (String e : list) {
                s.add(e.trim());
            }
            exclude = s;
            excludeSource = list;
        }
        Set<String> s = exclude;
        return s.contains(id.getNamespace()) || s.contains(id.toString());
    }
}
