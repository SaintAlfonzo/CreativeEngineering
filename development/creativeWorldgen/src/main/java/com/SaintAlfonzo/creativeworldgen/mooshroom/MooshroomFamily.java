package com.SaintAlfonzo.creativeworldgen.mooshroom;

import com.SaintAlfonzo.creativeworldgen.worldgen.SpawnTuning;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Locale;
import java.util.Set;

/**
 * The mooshroom family: vanilla red and brown mooshrooms plus Fungi Delight's Inky Cap Cow and Morel Cow
 * (feature_writeup/fungi_delight.md section 5.1.2).
 * <ul>
 *   <li>Natural brown mooshrooms: a {@code minecraft:mooshroom} spawned naturally or with the chunk
 *       ({@link MobSpawnType#NATURAL}, {@link MobSpawnType#CHUNK_GENERATION}) is brown with its biome's
 *       {@code brown_chance} from mob_spawns.json ({@link SpawnTuning#brownChance}). Eggs, breeding, spawners and
 *       commands are untouched.</li>
 *   <li>Breeding: any two family members breed ({@code AnimalCanMateMooshroomMixin}); the calf is 50 % each parent's
 *       kind ({@code MushroomCowBreedingMixin}, {@link #pickCalf}). Vanilla's 1 in 1024 red/brown mutation stays for
 *       two parents of the same vanilla type only.</li>
 * </ul>
 * The Fungi cows are known by entity type id only, so patcherMod does not compile against the Fungi port and
 * works without it. Lightning ({@code thunderHit}) is not touched here.
 */
public final class MooshroomFamily {

	private MooshroomFamily() {}

	public static final String VANILLA = "minecraft:mooshroom";
	/** Entity type ids that breed with each other. All of them extend MushroomCow. */
	public static final Set<String> MEMBERS = Set.of(VANILLA, "fungidelight:inky_cap_cow", "fungidelight:morel_cow");

	/** One parent or calf: its entity type id and, for the vanilla mooshroom only, its mushroom ("red"/"brown"). */
	public record Kind(String entity, String mushroom) {
		public boolean vanilla() {
			return VANILLA.equals(entity);
		}
	}

	public static String id(EntityType<?> type) {
		ResourceLocation key = Registry.ENTITY_TYPE.getKey(type);
		return key == null ? "" : key.toString();
	}

	public static boolean isMember(Entity e) {
		return e instanceof MushroomCow && MEMBERS.contains(id(e.getType()));
	}

	public static Kind kindOf(MushroomCow cow) {
		String entity = id(cow.getType());
		return new Kind(entity, VANILLA.equals(entity) ? cow.getMushroomType().name().toLowerCase(Locale.ROOT) : null);
	}

	/**
	 * The calf's kind: 50 % each parent's. Two vanilla parents of the same mushroom keep vanilla's 1 in 1024
	 * mutation to the other mushroom (rolled first, as vanilla does). No other mutation.
	 */
	public static Kind pickCalf(Kind a, Kind b, RandomSource random) {
		if (a.vanilla() && b.vanilla() && a.mushroom() != null && a.mushroom().equals(b.mushroom())
				&& random.nextInt(1024) == 0) {
			return new Kind(VANILLA, "brown".equals(a.mushroom()) ? "red" : "brown");
		}
		return random.nextBoolean() ? a : b;
	}

	/** Builds the calf of {@code self} and {@code partner}; null leaves it to vanilla (not two family members). */
	public static MushroomCow breed(MushroomCow self, MushroomCow partner, ServerLevel level) {
		if (!isMember(self) || !isMember(partner)) return null;
		Kind kind = pickCalf(kindOf(self), kindOf(partner), self.getRandom());
		EntityType<?> type = kind.entity().equals(id(self.getType())) ? self.getType() : partner.getType();
		Entity made = type.create(level);
		if (!(made instanceof MushroomCow calf)) {
			if (made != null) made.discard();
			return null;
		}
		if (kind.vanilla()) {
			((MushroomTypeSetter) calf).creativeengineering$setMushroomType(
					"brown".equals(kind.mushroom()) ? MushroomCow.MushroomType.BROWN : MushroomCow.MushroomType.RED);
		}
		return calf;
	}

	@SubscribeEvent
	public static void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
		MobSpawnType reason = event.getSpawnReason();
		if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) return;
		if (!(event.getEntity() instanceof MushroomCow cow) || cow.getType() != EntityType.MOOSHROOM) return;
		BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
		double chance = event.getLevel().getBiome(pos).unwrapKey()
				.map(k -> SpawnTuning.brownChance(k.location().toString())).orElse(0.0);
		if (chance > 0 && cow.getRandom().nextDouble() < chance) {
			((MushroomTypeSetter) cow).creativeengineering$setMushroomType(MushroomCow.MushroomType.BROWN);
		}
	}
}
