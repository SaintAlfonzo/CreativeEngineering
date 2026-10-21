package net.satisfy.foundation.banner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/** Block entity of the banner, only does the effect ticking. */
public class CompletionistBannerEntity extends BlockEntity {
    private static final int EFFECT_INTERVAL = 20;

    public CompletionistBannerEntity(BlockPos blockPos, BlockState state) {
        super(((CompletionistBannerBlock) state.getBlock()).getSettings().type().get(), blockPos, state);
    }

    /**
     * Every second: the banner's effect for all players within its radius. Server only. Port (alcohol.md B7.7): a
     * player whose effect still has {@code refreshBelowTicks} or more left is skipped; below that the running effect is
     * extended in place. Re-adding a running effect in 1.19.2 calls onEffectUpdated, which removes and re-adds its
     * attribute modifiers, and removing Health Boost clamps health to the base max: every refresh would empty the
     * extra hearts.
     */
    public static void tick(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof CompletionistBannerBlock banner)) {
            return;
        }
        BannerSettings settings = banner.getSettings();
        if (!level.isClientSide && level.getGameTime() % EFFECT_INTERVAL == 0) {
            int radius = settings.radius().getAsInt();
            if (radius <= 0) {
                return;
            }
            AABB effectRadius = new AABB(pos).inflate(radius);
            List<Player> players = level.getEntitiesOfClass(Player.class, effectRadius);
            MobEffect effect = settings.effect().get();
            int amplifier = settings.amplifier().getAsInt();
            int duration = settings.durationTicks().getAsInt();
            int refreshBelow = Math.min(duration, settings.refreshBelowTicks().getAsInt());
            for (Player player : players) {
                MobEffectInstance running = player.getEffect(effect);
                if (running != null && running.getAmplifier() == amplifier) {
                    if (running.getDuration() < refreshBelow && running.update(new MobEffectInstance(effect, duration, amplifier, true, false))
                            && player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), running));
                    }
                } else if (running == null || running.getAmplifier() < amplifier || running.getDuration() < refreshBelow) {
                    player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false));
                }
            }
        }
    }
}
