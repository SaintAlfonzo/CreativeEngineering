package com.SaintAlfonzo.more_mooblooms.entity;

import com.SaintAlfonzo.more_mooblooms.MMConfig;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariant;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariants;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

/**
 * O2: a plain cow ({@code minecraft:cow} exactly, so not a mooshroom or moobloom, D7) fed any variant's flower
 * may turn into that moobloom. Every try uses one flower (except in creative); {@code feedChance} decides.
 * Babies convert too and stay babies (D6). A flower several variants share picks one of them by each variant's
 * {@code feed_weight} for it (default 1 each, so an even pick; moobloom's dandelion is 9 to sunbloom's 1).
 */
public final class CowFeeding {

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget().getType() != EntityType.COW || !(event.getTarget() instanceof Cow cow)) return;
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        boolean client = event.getLevel().isClientSide;
        List<MoobloomVariant> matches = MoobloomVariants.byFlower(client, stack);
        if (matches.isEmpty() || !cow.isAlive()) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(client));
        if (client) return;

        ItemStack fed = stack.copy(); // the pick below needs the item after the last one is used up
        if (!player.getAbilities().instabuild) stack.shrink(1);
        ServerLevel level = (ServerLevel) event.getLevel();
        level.playSound(null, cow, SoundEvents.MOOSHROOM_EAT, SoundSource.NEUTRAL, 1.0F, 1.0F);
        if (cow.getRandom().nextDouble() >= MMConfig.num(MMConfig.FEED_CHANCE)) {
            level.sendParticles(ParticleTypes.SMOKE, cow.getX(), cow.getY(0.6), cow.getZ(), 4, 0.3, 0.2, 0.3, 0.01);
            return;
        }
        MoobloomVariant variant = pickByFeedWeight(matches, fed, cow);
        Moobloom moobloom = Moobloom.fromCow(cow, variant);
        if (moobloom == null) return;
        level.playSound(null, moobloom, SoundEvents.MOOSHROOM_CONVERT, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, moobloom.getX(), moobloom.getY(0.8), moobloom.getZ(), 12, 0.5, 0.4, 0.5, 0.0);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, moobloom.getX(), moobloom.getY(1.0), moobloom.getZ(), 20, 0.5, 0.3, 0.5, 0.0);
    }

    private static MoobloomVariant pickByFeedWeight(List<MoobloomVariant> matches, ItemStack stack, Cow cow) {
        int total = 0;
        for (MoobloomVariant v : matches) total += v.feedWeight(stack);
        int roll = cow.getRandom().nextInt(Math.max(1, total));
        for (MoobloomVariant v : matches) {
            roll -= v.feedWeight(stack);
            if (roll < 0) return v;
        }
        return matches.get(0);
    }

    private CowFeeding() {}
}
