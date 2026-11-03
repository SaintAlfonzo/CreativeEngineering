package com.SaintAlfonzo.more_mooblooms.mixin;

import com.SaintAlfonzo.more_mooblooms.spawn.MoobloomSpawns;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The moobloom spawn swap (feature_writeup/mooblooms.md 4.9): the entity the natural spawner hands to
 * {@code addFreshEntityWithPassengers} goes through {@link MoobloomSpawns#swap}, which may return a moobloom in
 * place of a cow. The cow has already passed every spawn check and was finalized, and is simply never added.
 *
 * {@code @ModifyArg}, not {@code @Redirect}: Botania already has a ModifyArg on the same call in
 * spawnCategoryForPosition, and a Redirect there would clash with it.
 *
 * - spawnCategoryForPosition (m_47038_, ongoing spawns, server thread): the cow was created in the ServerLevel,
 *   so {@code entity.level} is the level.
 * - spawnMobsForChunkGeneration (m_220450_, worldgen threads): the level is the WorldGenRegion parameter, which
 *   a ModifyArg can't see, so a HEAD inject keeps it in a ThreadLocal for the duration of the call.
 */
@Mixin(value = NaturalSpawner.class, remap = false)
public abstract class NaturalSpawnerMixin {

    @Unique
    private static final ThreadLocal<ServerLevelAccessor> more_mooblooms$chunkGenLevel = new ThreadLocal<>();

    @ModifyArg(method = "m_47038_",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;m_47205_(Lnet/minecraft/world/entity/Entity;)V",
                    remap = false),
            remap = false)
    private static Entity more_mooblooms$swapNatural(Entity entity) {
        return entity.level instanceof ServerLevel level ? MoobloomSpawns.swap(level, entity, MobSpawnType.NATURAL) : entity;
    }

    @Inject(method = "m_220450_", at = @At("HEAD"), remap = false)
    private static void more_mooblooms$enterChunkGen(ServerLevelAccessor level, Holder<Biome> biome, ChunkPos chunkPos,
                                                     RandomSource random, CallbackInfo ci) {
        more_mooblooms$chunkGenLevel.set(level);
    }

    @Inject(method = "m_220450_", at = @At("RETURN"), remap = false)
    private static void more_mooblooms$exitChunkGen(ServerLevelAccessor level, Holder<Biome> biome, ChunkPos chunkPos,
                                                    RandomSource random, CallbackInfo ci) {
        more_mooblooms$chunkGenLevel.remove();
    }

    @ModifyArg(method = "m_220450_",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/ServerLevelAccessor;m_47205_(Lnet/minecraft/world/entity/Entity;)V",
                    remap = false),
            remap = false)
    private static Entity more_mooblooms$swapChunkGen(Entity entity) {
        ServerLevelAccessor level = more_mooblooms$chunkGenLevel.get();
        return level == null ? entity : MoobloomSpawns.swap(level, entity, MobSpawnType.CHUNK_GENERATION);
    }
}
