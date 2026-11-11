// Copyright (C) 2026 SaintAlfonzo. Part of Creative Delight; GNU GPL v3 or later (see the pack's LICENSE).
package com.SaintAlfonzo.creativedelight.cannedgoods;

import net.mcreator.cannedgoods.entity.CannedGolemEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/**
 * The dyeable canned golem (feature_writeup/cans.md 2.3). Right-click a canned golem with any dye (vanilla or
 * #forge:dyes/<color>) to recolour it; the dye is used up unless in creative. The colour is synced entity data on
 * the modified upstream CannedGolemEntity, saved as NBT "Color" (DyeColor id), default red. Red keeps the mod's own
 * big_canned_boy texture; the other 15 render from creativedelight:textures/entity/canned_golem/<color>.png
 * (the modified CannedGolemRenderer). Same behaviour, NBT and textures as the CD mixins it replaces.
 */
public final class CannedGolemDye {

    public static final DyeColor DEFAULT_COLOR = DyeColor.RED;
    private static final String NBT_COLOR = "Color";
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[DyeColor.values().length];

    static {
        for (DyeColor color : DyeColor.values()) {
            TEXTURES[color.getId()] = new ResourceLocation("creativedelight",
                    "textures/entity/canned_golem/" + color.getName() + ".png");
        }
    }

    private CannedGolemDye() {
    }

    /** Texture for a colour, or null for the default colour (use the mod's own texture). */
    public static ResourceLocation texture(DyeColor color) {
        return color == DEFAULT_COLOR ? null : TEXTURES[color.getId()];
    }

    public static void save(CannedGolemEntity golem, CompoundTag tag) {
        tag.putByte(NBT_COLOR, (byte) golem.getColor().getId());
    }

    public static void load(CannedGolemEntity golem, CompoundTag tag) {
        if (tag.contains(NBT_COLOR, Tag.TAG_ANY_NUMERIC)) {
            golem.setColor(DyeColor.byId(tag.getByte(NBT_COLOR)));
        }
    }

    /** The dye interaction, or null when the held item is no dye or the golem already has that colour. */
    public static InteractionResult interact(CannedGolemEntity golem, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        DyeColor dye = DyeColor.getColor(stack);
        if (dye == null || dye == golem.getColor()) {
            return null;
        }
        if (!golem.level.isClientSide) {
            golem.setColor(dye);
            golem.level.playSound(null, golem, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(golem.level.isClientSide);
    }
}
