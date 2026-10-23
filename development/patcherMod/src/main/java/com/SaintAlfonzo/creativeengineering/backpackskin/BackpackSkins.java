package com.SaintAlfonzo.creativeengineering.backpackskin;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;

/**
 * Backpack skins: the skin is one string on the stack ({@link #TAG}, the skin block's id). Every look
 * (held, placed, worn) and the placed hitbox read it. Feature doc: feature_writeup/backpack_skins.md.
 */
public final class BackpackSkins {

    /** Stack tag holding the skin block id. Rides along with the stack into the placed block entity. */
    public static final String TAG = "CESkin";

    /** Items allowed as a skin (block items only, checked again in the recipe). */
    public static final TagKey<Item> SKIN_TAG = TagKey.create(ForgeRegistries.Keys.ITEMS,
            new ResourceLocation(CreativeEngineering.MODID, "backpack_skins"));

    private BackpackSkins() {}

    public static boolean isBackpack(ItemStack stack) {
        return stack.getItem() instanceof BackpackItem;
    }

    /** The skin block of a backpack stack, or null when it has none (or the id no longer exists). */
    @Nullable
    public static Block getSkin(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG, 8)) return null;
        Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(tag.getString(TAG)));
        return block == null || block == Blocks.AIR ? null : block;
    }

    public static ItemStack withSkin(ItemStack backpack, Block skin) {
        ItemStack out = backpack.copy();
        out.getOrCreateTag().putString(TAG, ForgeRegistries.BLOCKS.getKey(skin).toString());
        return out;
    }

    public static ItemStack withoutSkin(ItemStack backpack) {
        ItemStack out = backpack.copy();
        CompoundTag tag = out.getTag();
        if (tag != null) {
            tag.remove(TAG);
            if (tag.isEmpty()) out.setTag(null);
        }
        return out;
    }

    /** The block a skin item stands for, or null if it is not a block item. */
    @Nullable
    public static Block skinBlockOf(ItemStack addition) {
        return addition.getItem() instanceof BlockItem bi ? bi.getBlock() : null;
    }

    /** The skin's default state turned to face the way the backpack does, when it has a facing. */
    public static BlockState skinState(Block skin, @Nullable BlockState backpackState) {
        BlockState state = skin.defaultBlockState();
        if (backpackState == null) return state;
        for (Property<?> p : state.getProperties()) {
            if (p instanceof DirectionProperty dp && p.getName().equals("facing")) {
                Direction facing = backpackState.getValue(net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock.FACING);
                if (dp.getPossibleValues().contains(facing)) state = state.setValue(dp, facing);
            }
        }
        return state;
    }
}
