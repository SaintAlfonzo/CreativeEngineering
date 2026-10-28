package com.SaintAlfonzo.creativeengineering.itemphysic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Re-applies Allurement's Curse of Ascension / Curse of Fleeting to dropped items under
 * ItemPhysic. Allurement hooks them into {@code ItemEntity.tick} inside the
 * {@code !isNoGravity()} gravity branch, which ItemPhysic's {@code isNoGravity -> true}
 * redirect skips. Allurement is reached by reflection so it stays an optional dependency.
 */
public final class AllurementItemCurses {
    private static final ResourceLocation ASCENSION = new ResourceLocation("allurement", "ascension_curse");
    private static final ResourceLocation FLEETING = new ResourceLocation("allurement", "fleeting_curse");

    private static boolean resolved;
    private static MethodHandle ascend;
    private static MethodHandle repel;

    private AllurementItemCurses() {
    }

    private static synchronized void resolve() {
        if (resolved) return;
        resolved = true;
        if (!ModList.get().isLoaded("allurement")) return;
        try {
            Class<?> util = Class.forName("com.teamabnormals.allurement.core.other.AllurementUtil");
            MethodHandles.Lookup l = MethodHandles.publicLookup();
            MethodType t = MethodType.methodType(void.class, ItemEntity.class);
            ascend = l.findStatic(util, "ascendItem", t);
            repel = l.findStatic(util, "repelItem", t);
        } catch (Throwable ignored) {
            ascend = null;
            repel = null;
        }
    }

    public static void tick(ItemEntity item) {
        if (!resolved) resolve();
        if (ascend == null) return;
        try {
            if (level(item, ASCENSION) > 0) ascend.invoke(item);
            if (level(item, FLEETING) > 0) repel.invoke(item);
        } catch (Throwable ignored) {
        }
    }

    private static int level(ItemEntity item, ResourceLocation id) {
        Enchantment e = ForgeRegistries.ENCHANTMENTS.getValue(id);
        return e == null ? 0 : EnchantmentHelper.getTagEnchantmentLevel(e, item.getItem());
    }
}
