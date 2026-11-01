package net.satisfy.vinery.core.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CreeperEffect extends InstantenousMobEffect {
    public CreeperEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF0000);
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        explode(source, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity source, int amplifier) {
        explode(source, amplifier);
        return;
    }

    private void explode(Entity source, int amplifier){
        if (source instanceof ServerPlayer serverPlayer && serverPlayer.gameMode.getGameModeForPlayer() != GameType.CREATIVE) {
            Level level = serverPlayer.getCommandSenderWorld();
            double x = serverPlayer.getX();
            double y = serverPlayer.getY();
            double z = serverPlayer.getZ();
            level.explode(null, x, y, z, (float) (amplifier + 1), net.minecraft.world.level.Explosion.BlockInteraction.BREAK);

            serverPlayer.hurt(net.minecraft.world.damagesource.DamageSource.explosion((net.minecraft.world.entity.LivingEntity) null), 50.0F);
        }
    }
}
