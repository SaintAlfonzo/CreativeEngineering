package com.SaintAlfonzo.creativedelight.barbeques.compat;

import com.SaintAlfonzo.creativedelight.barbeques.BarbequesDelight;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

/**
 * The pack seasoning framework (Creative Delight's {@code food.Seasonings}, feature_writeup/food.md 3.12) seen from this
 * member. Since the seasoning fold (food/seasoning #11, barbeques_delight.md 4.1) the six BBQ seasonings and
 * {@code grilled} are pack seasonings: NBT {@code Seasonings:["barbequesdelight:chili", ...]}, put on by crafts (and the
 * Grill for {@code grilled}), bonuses applied by Creative Delight on eating. Members do not compile against Creative
 * Delight, so its methods are found by reflection once; without it (never in the pack: the member ships inside it) the
 * food is left as it is.
 */
public final class PackSeasonings {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String TAG = "Seasonings";
    public static final ResourceLocation GRILLED = BarbequesDelight.loc("grilled");

    private static boolean looked;
    @Nullable
    private static MethodHandle foodProperties;
    @Nullable
    private static MethodHandle addFromStation;
    @Nullable
    private static MethodHandle applyEaten;
    @Nullable
    private static MethodHandle without;

    private PackSeasonings() {
    }

    private static void look() {
        if (looked) {
            return;
        }
        looked = true;
        try {
            Class<?> c = Class.forName("com.SaintAlfonzo.creativedelight.food.Seasonings");
            MethodHandles.Lookup l = MethodHandles.publicLookup();
            foodProperties = l.findStatic(c, "foodProperties", MethodType.methodType(FoodProperties.class, ItemStack.class, FoodProperties.class));
            addFromStation = l.findStatic(c, "addFromStation", MethodType.methodType(boolean.class, ItemStack.class, ResourceLocation.class));
            applyEaten = l.findStatic(c, "applyEaten", MethodType.methodType(void.class, Player.class, ItemStack.class));
            without = l.findStatic(c, "without", MethodType.methodType(ItemStack.class, ItemStack.class, ResourceLocation.class));
        } catch (Throwable e) {
            LOGGER.warn("[barbequesdelight] Creative Delight seasonings not found; skewers ignore seasonings", e);
        }
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
        look();
        if (foodProperties == null) {
            return base;
        }
        try {
            return (FoodProperties) foodProperties.invokeExact(stack, base);
        } catch (Throwable e) {
            return base;
        }
    }

    /** The Grill puts {@code grilled} on a skewer it finished (within the cap rules). */
    public static void addGrilled(ItemStack stack) {
        look();
        if (addFromStation == null) {
            return;
        }
        try {
            boolean ignored = (boolean) addFromStation.invokeExact(stack, GRILLED);
        } catch (Throwable e) {
            LOGGER.warn("[barbequesdelight] could not add grilled", e);
        }
    }

    /** {@code stack} (modified) without {@code grilled}: a skewer used as an ingredient drops it (O35). */
    public static ItemStack withoutGrilled(ItemStack stack) {
        look();
        if (without == null) {
            return stack;
        }
        try {
            return (ItemStack) without.invokeExact(stack, GRILLED);
        } catch (Throwable e) {
            return stack;
        }
    }

    /** Each seasoning's on-eat bonus for a stack the player did not eat directly (a skewer inside a combo). */
    public static void applyEaten(Player player, ItemStack stack) {
        if (of(stack).isEmpty()) {
            return;
        }
        look();
        if (applyEaten == null) {
            return;
        }
        try {
            applyEaten.invokeExact(player, stack);
        } catch (Throwable e) {
            LOGGER.warn("[barbequesdelight] could not apply seasonings", e);
        }
    }
}
