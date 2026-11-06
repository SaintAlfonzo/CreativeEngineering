package com.SaintAlfonzo.creativecompat.compat;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecompat.CreativeCompat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;

/**
 * Plant models shared by the flower boxes (feature_writeup/flower_pots.md 3.2).
 *
 * <ul>
 * <li>Glow berries and glow lichen join Supplementaries' special flower box models ({@link SuppFlowerBox}), so the
 * Supplementaries box takes them and the other boxes can draw them.</li>
 * <li>{@link PlantBoxModelsClient#renderSpecial} draws a plant with its Supplementaries flower box model (crops, seeds, cactus, chorus,
 * tall flowers...) when it has one; Another Furniture's planter box renderer calls it
 * ({@code mixin/AnotherFurniturePlanterRenderMixin}).</li>
 * <li>{@link #NOT_IN_BOXES}: plants no flower box takes (azalea bushes), read by the Supplementaries box mixin and
 * listed in Another Furniture's {@code planter_box_banned} tag.</li>
 * </ul>
 */
public final class PlantBoxModels {

    public static final ResourceLocation GLOW_BERRIES = new ResourceLocation(CreativeCompat.NS, "block/flower_box/glow_berries");
    public static final ResourceLocation GLOW_LICHEN = new ResourceLocation(CreativeCompat.NS, "block/flower_box/glow_lichen");
    /** Glow lichen with its base at y4, the vanilla pot's soil height (botanist workbench). */
    public static final ResourceLocation GLOW_LICHEN_POT = new ResourceLocation(CreativeCompat.NS, "block/flower_box/glow_lichen_pot");

    public static final TagKey<Item> NOT_IN_BOXES = ItemTags.create(new ResourceLocation(CreativeCompat.NS, "not_in_flower_boxes"));

    private PlantBoxModels() {}

    static boolean supplementaries() {
        return ModList.get().isLoaded("supplementaries");
    }

    /** Common setup: adds glow berries and glow lichen to Supplementaries' special flower box models. */
    public static void commonSetup() {
        if (supplementaries()) SuppFlowerBox.registerExtras();
    }

    @Nullable
    public static ResourceLocation specialModel(Item item) {
        return supplementaries() ? SuppFlowerBox.specialModel(item) : null;
    }

}
