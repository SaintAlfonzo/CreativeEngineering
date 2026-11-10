package com.SaintAlfonzo.creativedelight.hearth.event;

import com.SaintAlfonzo.creativedelight.hearth.HHTags;
import com.SaintAlfonzo.creativedelight.hearth.entity.ThrownCleaver;
import com.SaintAlfonzo.creativedelight.hearth.item.CleaverItem;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

/** Butchering: a cleaver kill on a tagged animal drops only meat, plus half as much again (rounded up). */
public final class CleaverEvents {
    private static final Set<LivingEntity> KILLED = Collections.newSetFromMap(new WeakHashMap<>());

    private CleaverEvents() {
    }

    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        Entity direct = event.getSource().getDirectEntity();
        if (direct instanceof ThrownCleaver || (direct == player && player.getMainHandItem().getItem() instanceof CleaverItem)) {
            KILLED.add(event.getEntity());
        }
    }

    public static void onDrops(LivingDropsEvent event) {
        LivingEntity target = event.getEntity();
        if (!KILLED.remove(target) || !target.getType().is(HHTags.CAN_BE_BUTCHERED)) return;
        event.getDrops().removeIf(drop -> !drop.getItem().is(HHTags.RAW_MEAT));
        if (event.getDrops().isEmpty()) return;
        int meat = event.getDrops().stream().mapToInt(drop -> drop.getItem().getCount()).sum();
        ItemStack bonus = event.getDrops().iterator().next().getItem().copy();
        bonus.setCount((int) Math.ceil(meat * 0.5D));
        event.getDrops().add(new ItemEntity(target.level, target.getX(), target.getY(), target.getZ(), bonus));
    }
}
