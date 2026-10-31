package com.SaintAlfonzo.creativeengineering.potion;

import com.SaintAlfonzo.creativeengineering.CEConfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Copper poisoning from fully oxidized Everything Copper gear ({@code potions/effects #11}).
 *
 * <p>Each player has a next-roll time in its persistent data ({@value #NEXT_ROLL_KEY}, level game time),
 * {@code [copper_poisoning].intervalSeconds} +- {@code jitterSeconds} apart. At a roll the player's worn
 * armor, hotbar and offhand are scanned for Everything Copper items whose {@code state} tag is
 * {@code oxidized} (waxed or not: Everything Copper's own {@code ICopperItem#getAge}). Each such piece
 * poisons with {@code chancePerPiece}; on a hit the player gets Poison I for {@code baseSeconds} plus
 * {@code secondsPerExtraPiece} per piece after the first, capped at {@code maxSeconds}.
 *
 * <p>Cost: the scan runs only at a roll (14 stacks, one NBT string each, every few minutes per player);
 * between rolls the handler is one game-time compare per player per second.
 *
 * <p>The other half, Copper Poisoning's own tick giving Poison instead of damage, is
 * {@code mixin/CopperPoisoningEffectMixin} via {@link #poisonFromEffect}.
 */
public final class CopperPoisoningHandler {

    static final String NEXT_ROLL_KEY = "ce_copper_poison_roll";
    private static final String NAMESPACE = "everythingcopper";
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    private CopperPoisoningHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level.isClientSide || player.tickCount % 20 != 0) return;
        if (!CEConfig.COPPER_POISONING.enabled.get() || player.isCreative() || player.isSpectator()) return;

        long now = player.level.getGameTime();
        CompoundTag data = player.getPersistentData();
        if (!data.contains(NEXT_ROLL_KEY)) {
            data.putLong(NEXT_ROLL_KEY, now + nextIntervalTicks(player));
            return;
        }
        long next = data.getLong(NEXT_ROLL_KEY);
        // A level whose game time went backwards (another world's save) would never reach the stored time.
        if (now < next && next - now <= maxIntervalTicks()) return;
        data.putLong(NEXT_ROLL_KEY, now + nextIntervalTicks(player));
        if (now < next) return;

        int pieces = countOxidized(player);
        if (pieces == 0) return;
        double chance = CEConfig.COPPER_POISONING.chancePerPiece.get();
        boolean hit = false;
        for (int i = 0; i < pieces && !hit; i++) {
            hit = player.getRandom().nextDouble() < chance;
        }
        if (!hit) return;

        int seconds = CEConfig.COPPER_POISONING.baseSeconds.get()
                + CEConfig.COPPER_POISONING.secondsPerExtraPiece.get() * (pieces - 1);
        seconds = Math.min(seconds, CEConfig.COPPER_POISONING.maxSeconds.get());
        player.addEffect(new MobEffectInstance(MobEffects.POISON, seconds * 20, 0));
    }

    /** Copper Poisoning's tick: Poison I, {@code effectPoisonSeconds} per effect level, no direct damage. */
    public static void poisonFromEffect(LivingEntity entity, int amplifier) {
        if (entity.level.isClientSide) return;
        int seconds = CEConfig.COPPER_POISONING.effectPoisonSeconds.get() * (amplifier + 1);
        entity.addEffect(new MobEffectInstance(MobEffects.POISON, seconds * 20, 0));
    }

    /** Fully oxidized Everything Copper pieces in the armor slots, hotbar and offhand. */
    static int countOxidized(Player player) {
        int count = 0;
        for (EquipmentSlot slot : ARMOR) {
            if (isOxidized(player.getItemBySlot(slot))) count++;
        }
        Inventory inv = player.getInventory();
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            if (isOxidized(inv.getItem(i))) count++;
        }
        if (isOxidized(player.getOffhandItem())) count++;
        return count;
    }

    static boolean isOxidized(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return false;
        CompoundTag tag = stack.getTag();
        if (tag == null || !"oxidized".equals(tag.getString("state"))) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && NAMESPACE.equals(id.getNamespace());
    }

    private static long nextIntervalTicks(Player player) {
        int interval = CEConfig.COPPER_POISONING.intervalSeconds.get();
        int jitter = CEConfig.COPPER_POISONING.jitterSeconds.get();
        int seconds = interval + (jitter > 0 ? player.getRandom().nextInt(2 * jitter + 1) - jitter : 0);
        return Math.max(1, seconds) * 20L;
    }

    private static long maxIntervalTicks() {
        return (CEConfig.COPPER_POISONING.intervalSeconds.get() + CEConfig.COPPER_POISONING.jitterSeconds.get()) * 20L;
    }
}
