package com.SaintAlfonzo.creativeworldgen.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Private vanilla members the locate code reads (SRG names; ObfuscationReflectionHelper maps them in dev, with a
 * Mojang-name fallback). Method handles, so a call costs about as much as a field read.
 */
final class LocateReflect {

	private static final MethodHandle CHECK_GENERATOR = getter(StructureCheck.class, "f_197242_", "chunkGenerator");
	private static final MethodHandle CHECK_RANDOM_STATE = getter(StructureCheck.class, "f_226710_", "randomState");
	private static final MethodHandle CHECK_SEED = getter(StructureCheck.class, "f_197245_", "seed");
	private static final MethodHandle CHECK_REGISTRY_ACCESS = getter(StructureCheck.class, "f_197238_", "registryAccess");
	private static final MethodHandle CACHE_TICKETS = getter(ServerChunkCache.class, "f_8327_", "distanceManager");
	private static final MethodHandle MANAGER_LEVEL = getter(StructureManager.class, "f_220460_", "level");
	private static final MethodHandle MULTI_NOISE_PARAMETERS = getter(MultiNoiseBiomeSource.class, "f_48435_", "parameters");
	private static final MethodHandle PLACEMENTS_FOR = method(ChunkGenerator.class, "m_223138_", "getPlacementsForStructure", Holder.class, RandomState.class);

	private LocateReflect() {}

	static ChunkGenerator generator(StructureCheck check) { return (ChunkGenerator) get(CHECK_GENERATOR, check); }
	static RandomState randomState(StructureCheck check) { return (RandomState) get(CHECK_RANDOM_STATE, check); }
	static long seed(StructureCheck check) { return (Long) get(CHECK_SEED, check); }
	static RegistryAccess registryAccess(StructureCheck check) { return (RegistryAccess) get(CHECK_REGISTRY_ACCESS, check); }
	static DistanceManager tickets(ServerChunkCache cache) { return (DistanceManager) get(CACHE_TICKETS, cache); }
	static LevelAccessor level(StructureManager manager) { return (LevelAccessor) get(MANAGER_LEVEL, manager); }
	@SuppressWarnings("unchecked")
	static Climate.ParameterList<Holder<Biome>> parameters(MultiNoiseBiomeSource source) {
		return (Climate.ParameterList<Holder<Biome>>) get(MULTI_NOISE_PARAMETERS, source);
	}

	/** ChunkGenerator#getPlacementsForStructure: every live placement (ChunkGeneratorLocateMixin drops dead ones). */
	@SuppressWarnings("unchecked")
	static List<StructurePlacement> placementsFor(ChunkGenerator generator, Holder<Structure> structure, RandomState randomState) {
		try {
			return (List<StructurePlacement>) PLACEMENTS_FOR.invoke(generator, structure, randomState);
		} catch (Throwable t) {
			throw new IllegalStateException("getPlacementsForStructure", t);
		}
	}

	private static Object get(MethodHandle getter, Object target) {
		try {
			return getter.invoke(target);
		} catch (Throwable t) {
			throw new IllegalStateException("locate reflection", t);
		}
	}

	private static MethodHandle getter(Class<?> c, String srg, String mojmap) {
		try {
			Field f;
			try {
				f = ObfuscationReflectionHelper.findField(c, srg);
			} catch (RuntimeException | LinkageError e) {
				f = c.getDeclaredField(mojmap);
				f.setAccessible(true);
			}
			return MethodHandles.lookup().unreflectGetter(f);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("no field " + srg + "/" + mojmap + " on " + c.getName(), e);
		}
	}

	private static MethodHandle method(Class<?> c, String srg, String mojmap, Class<?>... params) {
		try {
			Method m;
			try {
				m = ObfuscationReflectionHelper.findMethod(c, srg, params);
			} catch (RuntimeException | LinkageError e) {
				m = c.getDeclaredMethod(mojmap, params);
				m.setAccessible(true);
			}
			return MethodHandles.lookup().unreflect(m);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("no method " + srg + "/" + mojmap + " on " + c.getName(), e);
		}
	}
}
