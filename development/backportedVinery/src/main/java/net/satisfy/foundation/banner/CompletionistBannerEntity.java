package net.satisfy.foundation.banner;

import net.minecraft.core.BlockPos;
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
    private static final int EFFECT_DURATION = 60;

    public CompletionistBannerEntity(BlockPos blockPos, BlockState state) {
        super(((CompletionistBannerBlock) state.getBlock()).getSettings().type().get(), blockPos, state);
    }

    /** Every second: the banner's effect for all players within its radius. Server only. */
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
            for (Player player : players) {
                player.addEffect(new MobEffectInstance(settings.effect().get(), EFFECT_DURATION, settings.amplifier().getAsInt(), true, false));
            }
        }
    }
}
