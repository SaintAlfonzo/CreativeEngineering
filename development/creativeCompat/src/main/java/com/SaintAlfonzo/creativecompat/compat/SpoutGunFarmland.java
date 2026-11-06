package com.SaintAlfonzo.creativecompat.compat;

import com.SaintAlfonzo.creativecompat.CreativeCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Create: Misc and Things' water Spout Gun ({@code create_things_and_misc:spout_gun_water}) soaks farmland it hits.
 * Its shot is an arrow-like entity ({@code projectile_spout_gun_water}) that sticks on a block hit and is discarded
 * the next tick; the mod's own block-hit procedure is never called. Plants have no collision box, so a shot at a crop
 * lands on its farmland. The hit block, or the block under it, with farmland moisture (farmland, Farmer's Delight rich
 * soil farmland and the like) goes to full moisture. Only the water gun: the lava, honey, chocolate and slime guns
 * have their own entity types. Matched by entity type id, so the mod stays optional.
 */
@Mod.EventBusSubscriber(modid = CreativeCompat.MOD_ID)
public final class SpoutGunFarmland {

    private static final ResourceLocation WATER_SHOT =
            new ResourceLocation("create_things_and_misc", "projectile_spout_gun_water");

    /** Only with Create: Misc and Things. */
    private static final boolean LOADED = net.minecraftforge.fml.ModList.get().isLoaded("create_things_and_misc");

    private SpoutGunFarmland() {}

    @SubscribeEvent
    public static void onImpact(ProjectileImpactEvent event) {
        if (!LOADED) return;
        HitResult hit = event.getRayTraceResult();
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) return;
        Projectile shot = event.getProjectile();
        if (!(shot.level instanceof ServerLevel level)) return;
        if (!WATER_SHOT.equals(ForgeRegistries.ENTITY_TYPES.getKey(shot.getType()))) return;

        BlockPos pos = blockHit.getBlockPos();
        if (!wet(level, pos)) wet(level, pos.below());
    }

    /** Moisture to full if the block has farmland moisture and is not soaked; true if it has the property. */
    private static boolean wet(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(FarmBlock.MOISTURE)) return false;
        if (state.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE) {
            level.setBlock(pos, state.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), 2);
            level.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 4, 0.3, 0.05, 0.3, 0.0);
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.3F, 1.6F);
        }
        return true;
    }
}
