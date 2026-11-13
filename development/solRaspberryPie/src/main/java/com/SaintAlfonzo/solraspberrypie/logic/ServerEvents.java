package com.SaintAlfonzo.solraspberrypie.logic;

import com.SaintAlfonzo.solraspberrypie.RpConfig;
import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.SaintAlfonzo.solraspberrypie.command.RpCommands;
import com.SaintAlfonzo.solraspberrypie.data.RpData;
import com.SaintAlfonzo.solraspberrypie.ladder.Ladder;
import com.SaintAlfonzo.solraspberrypie.ladder.LadderLoader;
import com.SaintAlfonzo.solraspberrypie.net.RpNetwork;
import com.SaintAlfonzo.solraspberrypie.net.S2CLadder;
import com.SaintAlfonzo.solraspberrypie.net.S2CMessage;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.tracking.CapabilityHandler;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Forge event wiring: capability, ladder load, login/logout, death and respawn, ticks, cures, drinks, commands. */
public final class ServerEvents {
    /** Apple Pie 2.2.0's attribute modifier names: "<attribute>_modifier_<value>" (3.1). */
    private static final Pattern APPLE_PIE_MODIFIER = Pattern.compile("^[a-z0-9_.:/-]+_modifier_-?[0-9.]+(E-?[0-9]+)?$");

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Player) e.addCapability(RpData.KEY, new RpData());
    }

    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent e) {
        Ladder.current = LadderLoader.load(e.getServer());
        if (SOLSweetPotatoConfig.minContribution() > 0) {
            SolRaspberryPie.LOG.warn("Apple Pie minContribution is {} (above 0): foods aged out by respecs and death still count a little, so every respec costs less (6.1)",
                    SOLSweetPotatoConfig.minContribution());
        }
        if (SOLSweetPotatoConfig.shouldResetOnDeath()) {
            SolRaspberryPie.LOG.warn("Apple Pie resetOnDeath is true: it clears the whole food memory on death; Raspberry Pie expects false and ages the window out instead");
        }
        if (!SOLSweetPotatoConfig.getBenefitsUnparsed().isEmpty() || !SOLSweetPotatoConfig.getThresholds().isEmpty()) {
            SolRaspberryPie.LOG.warn("Apple Pie still has its own benefits configured (benefitsUnparsed / thresholds): they apply on top of the Raspberry Pie ladder. Empty both lists.");
        }
    }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e) {
        RpCommands.register(e.getDispatcher());
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer player)) return;
        RpData d = RpData.get(player);
        stripApplePieLeftovers(player);
        if (d.paused && RpConfig.SERVER.pauseEndsOnRelog.get()) {
            d.paused = false;
            PauseRegistry.get(player.server).set(player, false);
        }
        RpNetwork.toPlayer(player, new S2CLadder(Ladder.current));
        DietEngine.forgetApplied(d);
        DietEngine.evaluate(player, false);
        if (d.savedHealth > player.getHealth()) player.setHealth(Math.min(d.savedHealth, player.getMaxHealth()));
        d.savedHealth = -1;
        giveBooks(player, d);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer player) RpData.get(player).savedHealth = player.getHealth();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void clone(PlayerEvent.Clone e) {
        Player original = e.getOriginal();
        original.reviveCaps();
        try {
            RpData old = RpData.get(original);
            RpData neu = RpData.get(e.getEntity());
            neu.copyFrom(old);
            if (e.isWasDeath()) {
                neu.pendingDeathMessage = !old.activeKeys.isEmpty();
            } else {
                neu.cloneHealth = original.getHealth();
            }
        } finally {
            original.invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer player)) return;
        RpData d = RpData.get(player);
        DietEngine.forgetApplied(d);
        if (!e.isEndConquered()) {
            // 8: death ages the whole window out and resets the foods-eaten counter; slots grey, choices and unlocks stay.
            FoodList fl = FoodList.get(player);
            Map<com.tarinoita.solsweetpotato.tracking.FoodInstance, Integer> memory = FoodWindow.memory(fl);
            FoodWindow.moveOut(memory, FoodWindow.windowOldestFirst(memory));
            fl.resetFoodsEaten();
            d.stamp++;
            CapabilityHandler.syncFoodList(player);
            RpNetwork.toPlayer(player, S2CMessage.clear());
            DietEngine.evaluate(player, false);
            if (d.pendingDeathMessage && !d.paused) {
                RpNetwork.toPlayer(player, S2CMessage.line(S2CMessage.RED, Component.translatable("solraspberrypie.msg.death")));
            }
            d.pendingDeathMessage = false;
        } else {
            DietEngine.evaluate(player, false);
            if (d.cloneHealth > player.getHealth()) player.setHealth(Math.min(d.cloneHealth, player.getMaxHealth()));
        }
        d.cloneHealth = -1;
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer player) DietEngine.sync(player);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer player)) return;
        RpData d = RpData.get(player);
        if (d.paused || d.applied.isEmpty()) {
            d.pendingReapply = false;
            return;
        }
        int interval = RpConfig.SERVER.refreshInterval.get();
        long time = player.server.getTickCount() + (player.getUUID().hashCode() & 0x7fffffff);
        if (d.pendingReapply || time % interval == 0) {
            d.pendingReapply = false;
            DietEngine.reapplyAll(player);
        }
    }

    /** 9.3: nothing is cancelled; a removed diet instance comes back on the next tick. */
    @SubscribeEvent
    public static void effectRemoved(MobEffectEvent.Remove e) {
        if (!(e.getEntity() instanceof ServerPlayer player)) return;
        RpData d = RpData.get(player);
        if (d.suppressRemoveEvent || d.paused) return;
        if (EffectApplier.chainHasMarker(e.getEffectInstance())) d.pendingReapply = true;
    }

    @SubscribeEvent
    public static void itemUsed(LivingEntityUseItemEvent.Finish e) {
        if (e.getEntity() instanceof ServerPlayer player) Drinks.onDrink(player, e.getItem());
    }

    private static void stripApplePieLeftovers(ServerPlayer player) {
        int removed = 0;
        for (Attribute attr : ForgeRegistries.ATTRIBUTES) {
            AttributeInstance inst = player.getAttribute(attr);
            if (inst == null) continue;
            List<AttributeModifier> doomed = new ArrayList<>();
            for (AttributeModifier m : inst.getModifiers()) if (APPLE_PIE_MODIFIER.matcher(m.getName()).matches()) doomed.add(m);
            for (AttributeModifier m : doomed) {
                inst.removeModifier(m.getId());
                removed++;
            }
        }
        if (removed > 0) {
            SolRaspberryPie.LOG.info("Removed {} leftover Apple Pie attribute modifiers from {}", removed, player.getGameProfile().getName());
            if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
        }
    }

    private static void giveBooks(ServerPlayer player, RpData d) {
        if (d.booksGiven) return;
        d.booksGiven = true;
        if (!RpConfig.SERVER.giveBooksOnFirstJoin.get()) return;
        if (player.getStats().getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) > 0) return; // not a new player
        Item dietLog = ForgeRegistries.ITEMS.getValue(new ResourceLocation("solapplepie", "food_book"));
        for (Item item : new Item[]{dietLog, SolRaspberryPie.LIQUID_DIET_LOG.get()}) {
            if (item == null) continue;
            ItemStack stack = new ItemStack(item);
            if (!player.getInventory().add(stack)) player.drop(stack, false);
        }
    }

    private ServerEvents() {}
}
