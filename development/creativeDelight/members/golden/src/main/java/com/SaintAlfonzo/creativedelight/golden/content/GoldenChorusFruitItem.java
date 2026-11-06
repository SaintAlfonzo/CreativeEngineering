package com.SaintAlfonzo.creativedelight.golden.content;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ChorusFruitItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityTeleportEvent;

/**
 * Golden chorus fruit (feature_writeup/golden_food.md 3.3). Eating it does what a chorus fruit does (the vanilla
 * teleport within 16 blocks), then teleports again within {@code reach} blocks ({@code reach} tries, each up to
 * {@code reach} blocks sideways and from 8 down to {@code reach - 9} up), and the item cooldown becomes
 * {@code cooldown} ticks instead of a chorus fruit's 20 (unless another mod cancels the teleport). Golden: 32 blocks, 10 ticks; enchanted: 64 blocks, 5 ticks.
 */
public class GoldenChorusFruitItem extends ChorusFruitItem {
    private final int reach;
    private final int cooldown;
    private final boolean shiny;

    public GoldenChorusFruitItem(Properties properties, int reach, int cooldown, boolean shiny) {
        super(properties);
        this.reach = reach;
        this.cooldown = cooldown;
        this.shiny = shiny;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity eater) {
        ItemStack left = super.finishUsingItem(stack, level, eater);
        // a cancelled teleport event ends it there, keeping the chorus fruit's own cooldown
        if (level instanceof ServerLevel server && farTeleport(server, eater)) {
            if (eater instanceof Player player) {
                player.getCooldowns().addCooldown(this, cooldown);
            }
        }
        return left;
    }

    /** False when a teleport event was cancelled. */
    private boolean farTeleport(ServerLevel level, LivingEntity eater) {
        double fromX = eater.getX();
        double fromY = eater.getY();
        double fromZ = eater.getZ();
        RandomSource random = eater.getRandom();
        int floor = level.getMinBuildHeight();
        int ceiling = floor + level.getLogicalHeight() - 1;
        for (int attempt = 0; attempt < reach; attempt++) {
            double x = fromX + (random.nextDouble() - 0.5D) * reach;
            double y = Mth.clamp(fromY + (random.nextInt(reach) - 8), floor, ceiling);
            double z = fromZ + (random.nextDouble() - 0.5D) * reach;
            if (eater.isPassenger()) {
                eater.stopRiding();
            }
            var start = eater.position();
            level.gameEvent(GameEvent.TELEPORT, start, GameEvent.Context.of(eater));
            EntityTeleportEvent.ChorusFruit event = ForgeEventFactory.onChorusFruitTeleport(eater, x, y, z);
            if (event.isCanceled()) {
                return false;
            }
            if (eater.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
                SoundEvent sound = eater instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                level.playSound(null, fromX, fromY, fromZ, sound, SoundSource.PLAYERS, 1.0F, 1.0F);
                eater.playSound(sound, 1.0F, 1.0F);
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return shiny || super.isFoil(stack);
    }
}
