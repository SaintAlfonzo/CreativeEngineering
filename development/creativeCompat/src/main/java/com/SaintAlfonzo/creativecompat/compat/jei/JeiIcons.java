package com.SaintAlfonzo.creativecompat.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/** JEI icon helpers shared by the Common JEI plugins and the content jars' categories (creative_split.md 7.2, Common API). */
public final class JeiIcons {
    private JeiIcons() {}

    /** A big item with a small one on top (Create's DoubleItemIcon), or the big one alone without Create. */
    public static IDrawable doubleIcon(IGuiHelper gui, ItemStack big, ItemStack small) {
        if (ModList.get().isLoaded("create")) {
            return CreateIcon.of(big, small);
        }
        return gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, big);
    }

    /** Kept in its own class so Create's class is only loaded when Create is installed. */
    private static final class CreateIcon {
        static IDrawable of(ItemStack big, ItemStack small) {
            return new com.simibubi.create.compat.jei.DoubleItemIcon(() -> big, () -> small);
        }
    }
}
