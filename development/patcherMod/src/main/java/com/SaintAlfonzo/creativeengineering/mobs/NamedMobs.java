package com.SaintAlfonzo.creativeengineering.mobs;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Named mobs (CEConfig [named_mobs], feature_writeup/mob_block_tweaks.md section 2.6, TODO {@code mobs/tweaks #6}).
 * <ul>
 *   <li>Roll: a listed mob spawned for a listed reason ({@link LivingSpawnEvent.SpecialSpawn}) is marked
 *       {@link #PENDING} on {@code chance}. Chunk generation and structure spawns run on worldgen threads, so the
 *       name is not picked here.</li>
 *   <li>Name: when a {@link #PENDING} mob joins its level (server thread) it gets a name from Minecraft Comes Alive's
 *       citizen pools (its shared random is not thread safe, hence the split), else from {@code fallbackNames}, and is
 *       marked {@link #NAMED}. The mark is in the mob's persistent data, so it survives saves and chunk reloads.</li>
 *   <li>Death: a {@link #NAMED} mob's vanilla death message goes to every player, with "Bessie the Cow" as the
 *       victim. A tamed one's owner already gets vanilla's copy and is skipped.</li>
 * </ul>
 * Naming does not make a mob persistent: it despawns as its kind would (farm animals never do).
 */
public final class NamedMobs {

	private NamedMobs() {}

	private static final Logger LOG = LogUtils.getLogger();

	/** Persistent-data key; {@link #PENDING} or {@link #NAMED}. */
	public static final String KEY = "creativeengineering:named_mob";
	public static final byte PENDING = 1;
	public static final byte NAMED = 2;

	/** Lang key for the victim in the death message: "%s the %s" (name, mob type). */
	public static final String TITLE_KEY = "chat.creativeengineering.named_mob.title";

	// Parsed config lists, rebuilt when the config hands back a different list (a reload or a GUI edit). Volatile:
	// worldgen threads read them; the source is written last, so a thread that sees it also sees the parsed set.
	private static volatile List<?> mobsSource, reasonsSource;
	private static volatile Set<EntityType<?>> mobTypes = Set.of();
	private static volatile List<TagKey<EntityType<?>>> mobTags = List.of();
	private static volatile Set<MobSpawnType> reasons = EnumSet.noneOf(MobSpawnType.class);

	// MCA: forge.net.mca.resources.Names#pickCitizenName(Gender), looked up once. Null after a failed lookup.
	private static boolean mcaLooked;
	private static Method mcaPick;
	private static Object mcaMale, mcaFemale;

	@SubscribeEvent
	public static void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
		if (!CEConfig.NAMED_MOBS.enabled.get()) return;
		Mob mob = event.getEntity();
		if (mob.hasCustomName()) return;
		if (!reasons().contains(event.getSpawnReason()) || !listed(mob.getType())) return;
		if (mob.getRandom().nextDouble() >= CEConfig.NAMED_MOBS.chance.get()) return;
		mob.getPersistentData().putByte(KEY, PENDING);
	}

	@SubscribeEvent
	public static void onJoin(EntityJoinLevelEvent event) {
		if (!(event.getEntity() instanceof Mob mob) || !(event.getLevel() instanceof ServerLevel)) return;
		if (mob.getPersistentData().getByte(KEY) != PENDING) return;
		if (!mob.hasCustomName()) mob.setCustomName(Component.literal(pickName(mob.getRandom())));
		mob.getPersistentData().putByte(KEY, NAMED);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onDeath(LivingDeathEvent event) {
		LivingEntity dead = event.getEntity();
		if (!(dead.level instanceof ServerLevel level) || !dead.hasCustomName()) return;
		if (dead.getPersistentData().getByte(KEY) != NAMED) return;

		if (event.getSource().getEntity() instanceof ServerPlayer killer) {
			// Karma hook (largecontent/karma #1, not built): a large karma loss for the killer goes here.
		}

		if (!CEConfig.NAMED_MOBS.announceDeaths.get() || !level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)) return;
		Component message = withTitle(dead, dead.getCombatTracker().getDeathMessage());
		LivingEntity owner = dead instanceof TamableAnimal pet && pet.isTame() ? pet.getOwner() : null;
		for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
			if (player != owner) player.sendSystemMessage(message);
		}
	}

	/** The death message with its victim (vanilla's first argument) swapped for "Name the Type". */
	private static Component withTitle(LivingEntity dead, Component message) {
		if (!(message.getContents() instanceof TranslatableContents tc) || tc.getArgs().length == 0) return message;
		Object[] args = tc.getArgs().clone();
		args[0] = Component.translatable(TITLE_KEY, dead.getCustomName(), dead.getType().getDescription());
		return Component.translatable(tc.getKey(), args);
	}

	private static String pickName(RandomSource random) {
		String name = mcaName(random.nextBoolean());
		if (name != null) return name;
		List<? extends String> fallback = CEConfig.NAMED_MOBS.fallbackNames.get();
		return fallback.isEmpty() ? "Bessie" : fallback.get(random.nextInt(fallback.size()));
	}

	/** A citizen name from MCA, or null (MCA missing, its lists not loaded, or the call failed). */
	private static String mcaName(boolean male) {
		if (!mcaLooked) {
			mcaLooked = true;
			try {
				Class<?> gender = Class.forName("forge.net.mca.entity.ai.relationship.Gender");
				mcaPick = Class.forName("forge.net.mca.resources.Names").getMethod("pickCitizenName", gender);
				mcaMale = gender.getField("MALE").get(null);
				mcaFemale = gender.getField("FEMALE").get(null);
			} catch (ReflectiveOperationException | LinkageError e) {
				mcaPick = null;
			}
		}
		if (mcaPick == null) return null;
		try {
			Object name = mcaPick.invoke(null, male ? mcaMale : mcaFemale);
			// MCA answers "Unnamed" while its name lists are empty.
			return name instanceof String s && !s.isBlank() && !"Unnamed".equals(s) ? s : null;
		} catch (ReflectiveOperationException | RuntimeException e) {
			LOG.warn("[named_mobs] MCA name pick failed, using fallbackNames: {}", e.toString());
			mcaPick = null;
			return null;
		}
	}

	private static boolean listed(EntityType<?> type) {
		List<? extends String> list = CEConfig.NAMED_MOBS.mobs.get();
		if (list != mobsSource) {
			Set<EntityType<?>> types = new HashSet<>();
			List<TagKey<EntityType<?>>> tags = new ArrayList<>();
			for (String entry : list) {
				ResourceLocation id = ResourceLocation.tryParse(entry.startsWith("#") ? entry.substring(1) : entry);
				if (id == null) {
					LOG.warn("[named_mobs] bad mobs entry '{}'", entry);
				} else if (entry.startsWith("#")) {
					tags.add(TagKey.create(Registry.ENTITY_TYPE_REGISTRY, id));
				} else if (Registry.ENTITY_TYPE.containsKey(id)) {
					types.add(Registry.ENTITY_TYPE.get(id));
				} else {
					LOG.warn("[named_mobs] unknown entity '{}'", entry);
				}
			}
			mobTypes = types;
			mobTags = tags;
			mobsSource = list;
		}
		if (mobTypes.contains(type)) return true;
		for (TagKey<EntityType<?>> tag : mobTags) {
			if (type.is(tag)) return true;
		}
		return false;
	}

	private static Set<MobSpawnType> reasons() {
		List<? extends String> list = CEConfig.NAMED_MOBS.spawnReasons.get();
		if (list != reasonsSource) {
			Set<MobSpawnType> set = EnumSet.noneOf(MobSpawnType.class);
			for (String entry : list) {
				try {
					set.add(MobSpawnType.valueOf(entry.trim().toUpperCase(Locale.ROOT)));
				} catch (IllegalArgumentException e) {
					LOG.warn("[named_mobs] unknown spawn reason '{}'", entry);
				}
			}
			reasons = set;
			reasonsSource = list;
		}
		return reasons;
	}
}
