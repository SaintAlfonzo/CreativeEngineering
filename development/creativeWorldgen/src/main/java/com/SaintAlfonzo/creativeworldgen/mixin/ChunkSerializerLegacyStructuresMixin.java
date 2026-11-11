package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.LegacyIds;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Structure ids moved to creativeworldgen: on 2026-11-10 (worldgen/LegacyIds). ChunkSerializer reads a chunk's
 * structure starts (unpackStructureStart, m_188254_) and references (unpackStructureReferences, m_208154_) with
 * Registry#get(ResourceLocation) and drops unknown ids, so the old ids in the chunk's {@code structures} compound are
 * renamed in place first: start keys, start ids, piece type ids and reference keys.
 */
@Mixin(value = ChunkSerializer.class, remap = false)
public class ChunkSerializerLegacyStructuresMixin {

	@Inject(method = "m_188254_(Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePieceSerializationContext;Lnet/minecraft/nbt/CompoundTag;J)Ljava/util/Map;",
			at = @At("HEAD"), require = 1, remap = false)
	private static void creativeworldgen$legacyStarts(StructurePieceSerializationContext context, CompoundTag structures, long seed,
			CallbackInfoReturnable<Map<Structure, StructureStart>> cir) {
		LegacyIds.upgradeStructures(structures, context.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY));
	}

	@Inject(method = "m_208154_(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)Ljava/util/Map;",
			at = @At("HEAD"), require = 1, remap = false)
	private static void creativeworldgen$legacyReferences(RegistryAccess access, ChunkPos pos, CompoundTag structures,
			CallbackInfoReturnable<Map<Structure, LongSet>> cir) {
		LegacyIds.upgradeStructures(structures, access.registryOrThrow(Registry.STRUCTURE_REGISTRY));
	}
}
