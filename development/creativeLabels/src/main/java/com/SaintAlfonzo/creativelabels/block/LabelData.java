package com.SaintAlfonzo.creativelabels.block;

import com.SaintAlfonzo.creativelabels.CLRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * One placed label (sticky_notes.md 3.10, BRIEF 3). The picture is a stripped one-count copy of the shown item
 * (C2 / Q5): id + count 1 + only {@code Enchantments} (presence = glint), {@code Potion} (+ custom potion colour /
 * effects, which decide the potion colour), {@code display.color} and
 * {@code CustomModelData}. A stripped tag over {@link #MAX_PICTURE_TAG_BYTES} is dropped: id only.
 * The dye colours the icon, not the paper. Labels always drop blank ({@link #toItem()}).
 */
public final class LabelData {

    public static final String KEY_PICTURE = "Picture";
    public static final String KEY_DYE = "Dye";
    public static final String KEY_GLOW = "Glow";
    public static final String KEY_SHOW_NAME = "ShowName";

    /** C2: the stripped picture tag may not be bigger than this (serialized NBT bytes), else only the id is kept. */
    public static final int MAX_PICTURE_TAG_BYTES = 1024;

    /** CustomPotionColor / CustomPotionEffects: PotionUtils.getColor reads them before the Potion id. */
    private static final String[] KEPT_KEYS = {"Enchantments", "Potion", "CustomPotionColor", "CustomPotionEffects", "CustomModelData"};

    /** Stripped (see {@link #strip}); EMPTY = no picture. */
    public ItemStack picture = ItemStack.EMPTY;
    @Nullable
    public DyeColor dye;
    public boolean glow;
    public boolean showName;

    public LabelData() {}

    public boolean hasPicture() {
        return !picture.isEmpty();
    }

    /** Sets the picture from any stack (stripped copy); EMPTY clears it. */
    public void setPicture(ItemStack shown) {
        this.picture = shown.isEmpty() ? ItemStack.EMPTY : strip(shown);
    }

    public LabelData copy() {
        LabelData l = new LabelData();
        l.picture = picture.copy();
        l.dye = dye;
        l.glow = glow;
        l.showName = showName;
        return l;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (!picture.isEmpty()) tag.put(KEY_PICTURE, picture.save(new CompoundTag()));
        if (dye != null) tag.putString(KEY_DYE, dye.getName());
        if (glow) tag.putBoolean(KEY_GLOW, true);
        if (showName) tag.putBoolean(KEY_SHOW_NAME, true);
        return tag;
    }

    public static LabelData load(CompoundTag tag) {
        LabelData l = new LabelData();
        if (tag.contains(KEY_PICTURE, Tag.TAG_COMPOUND)) {
            ItemStack s = ItemStack.of(tag.getCompound(KEY_PICTURE));
            l.picture = s.isEmpty() ? ItemStack.EMPTY : strip(s);
        }
        if (tag.contains(KEY_DYE, Tag.TAG_STRING)) l.dye = DyeColor.byName(tag.getString(KEY_DYE), null);
        l.glow = tag.getBoolean(KEY_GLOW);
        l.showName = tag.getBoolean(KEY_SHOW_NAME);
        return l;
    }

    /** A blank label item (labels drop without their data, spec 3.2). */
    public ItemStack toItem() {
        return new ItemStack(CLRegistry.LABEL.get());
    }

    /**
     * C2 / Q5: a one-count copy keeping only what rendering needs. Everything else (BlockEntityTag, inventories,
     * book pages, backpack data, damage) is dropped; a result over {@link #MAX_PICTURE_TAG_BYTES} keeps the id only.
     */
    public static ItemStack strip(ItemStack source) {
        ItemStack out = new ItemStack(source.getItem(), 1);
        CompoundTag src = source.getTag();
        if (src == null) return out;
        CompoundTag kept = new CompoundTag();
        for (String key : KEPT_KEYS) {
            if (src.contains(key)) kept.put(key, src.get(key).copy());
        }
        if (src.contains(NoteData.TAG_DISPLAY, Tag.TAG_COMPOUND)) {
            CompoundTag display = src.getCompound(NoteData.TAG_DISPLAY);
            if (display.contains(NoteData.KEY_COLOR, Tag.TAG_ANY_NUMERIC)) {
                CompoundTag d = new CompoundTag();
                d.putInt(NoteData.KEY_COLOR, display.getInt(NoteData.KEY_COLOR));
                kept.put(NoteData.TAG_DISPLAY, d);
            }
        }
        if (!kept.isEmpty() && nbtBytes(kept) <= MAX_PICTURE_TAG_BYTES) out.setTag(kept);
        return out;
    }

    /** Serialized size of a compound (uncompressed NBT bytes); Integer.MAX_VALUE if it can't be written. */
    public static int nbtBytes(CompoundTag tag) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            NbtIo.write(tag, new DataOutputStream(bytes));
            return bytes.size();
        } catch (IOException e) {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabelData l)) return false;
        return glow == l.glow && showName == l.showName && dye == l.dye
                && ItemStack.matches(picture, l.picture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(picture.getItem(), picture.getTag(), dye, glow, showName);
    }

    @Override
    public String toString() {
        return "LabelData{picture=" + picture + ", dye=" + dye + ", glow=" + glow + ", showName=" + showName + "}";
    }
}
