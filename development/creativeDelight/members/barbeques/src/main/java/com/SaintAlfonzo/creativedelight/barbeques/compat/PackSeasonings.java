package com.SaintAlfonzo.creativedelight.barbeques.compat;

import com.SaintAlfonzo.creativedelight.barbeques.BarbequesDelight;
import com.SaintAlfonzo.creativedelight.food.SeasoningTooltip;
import com.SaintAlfonzo.creativedelight.food.Seasonings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The pack seasoning framework (Creative Delight's {@code food.Seasonings}, feature_writeup/food.md 3.12) seen from this
 * member. Since the seasoning fold (food/seasoning #11, barbeques_delight.md 4.1) the six BBQ seasonings and
 * {@code grilled} are pack seasonings: NBT {@code Seasonings:["barbequesdelight:chili", ...]}, put on by crafts (and the
 * Grill for {@code grilled}), bonuses applied by Creative Delight on eating. The member ships inside Creative Delight and
 * compiles against it, so it calls the framework directly.
 */
public final class PackSeasonings {
    public static final String TAG = "Seasonings";
    public static final ResourceLocation GRILLED = BarbequesDelight.loc("grilled");

    private PackSeasonings() {
    }

    /** The seasoning ids on a stack, NBT order. */
    public static List<ResourceLocation> of(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG, Tag.TAG_LIST)) {
            return List.of();
        }
        ListTag list = tag.getList(TAG, Tag.TAG_STRING);
        List<ResourceLocation> out = new ArrayList<>(list.size());
        for (int n = 0; n < list.size(); n++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(n));
            if (id != null) {
                out.add(id);
            }
        }
        return out;
    }

    /** The food with its seasonings' fast, always-edible and effect-transform kinds. */
    @Nullable
    public static FoodProperties foodProperties(ItemStack stack, @Nullable FoodProperties base) {
        if (base == null || of(stack).isEmpty()) {
            return base;
        }
        return Seasonings.foodProperties(stack, base);
    }

    /** The Grill puts {@code grilled} on a skewer it finished (within the cap rules). */
    public static void addGrilled(ItemStack stack) {
        Seasonings.addFromStation(stack, GRILLED);
    }

    /** {@code stack} (modified) without {@code grilled}: a skewer used as an ingredient drops it (O35). */
    public static ItemStack withoutGrilled(ItemStack stack) {
        return Seasonings.without(stack, GRILLED);
    }

    /** Each seasoning's on-eat bonus for a stack the player did not eat directly (a skewer inside a combo). */
    public static void applyEaten(Player player, ItemStack stack) {
        if (of(stack).isEmpty()) {
            return;
        }
        Seasonings.applyEaten(player, stack);
    }

    /**
     * The tooltip line a food seasoned with {@code id} shows (Creative Delight's {@code food.SeasoningTooltip.line}), for
     * the seasoning item's own tooltip (plan_meals_rolls_bbq.md C1). Client only (the line reads lang).
     */
    @Nullable
    public static Component tooltipLine(ResourceLocation id) {
        return SeasoningTooltip.line(id);
    }
}
