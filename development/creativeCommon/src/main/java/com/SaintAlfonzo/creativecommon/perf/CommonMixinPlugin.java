package com.SaintAlfonzo.creativecommon.perf;

import com.mojang.logging.LogUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.SaintAlfonzo.creativecommon.shared.GatedMixinPlugin;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mixin config plugin for creativecommon.mixins.json. A {@link GatedMixinPlugin} (creative_split.md 7.3 step 0):
 * a mixin named in {@link #NEEDS_MOD} applies only when one of its mods is loaded and inside its tested range in
 * {@link #MOD_RANGE}; every other mixin (vanilla and Forge targets) applies as listed.
 * Each jar carved out of patcherMod takes its mixins' rows with it into its own plugin. Its other job is perf.md 15
 * candidate C: after {@code IwServerLevelTickMixin} (priority 1500, so after Immersive Weathering's own
 * {@code ServerLevelMixin} at 1000) it rewrites IW's merged {@code callTick} handler in ServerLevel to call
 * {@link IwGrowthFast#blockTick}. Mixin will not inject into another mixin's merged method, hence the ASM.
 * Must not load game classes: it runs while ServerLevel is being transformed.
 */
public class CommonMixinPlugin extends GatedMixinPlugin {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final String IW_MIXIN = "com.ordana.immersive_weathering.mixins.ServerLevelMixin";
	private static final String TICK_MIXIN = "com.SaintAlfonzo.creativecommon.mixin.IwServerLevelTickMixin";
	private static final String MERGED = "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;";
	/** IW 1.2.10's callTick(LevelChunk, int, CallbackInfo, captured locals...). */
	private static final String HANDLER_DESC = "(Lnet/minecraft/world/level/chunk/LevelChunk;ILorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;"
			+ "Lnet/minecraft/world/level/ChunkPos;ZIILnet/minecraft/util/profiling/ProfilerFiller;[Lnet/minecraft/world/level/chunk/LevelChunkSection;"
			+ "IILnet/minecraft/world/level/chunk/LevelChunkSection;IILnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V";
	private static final String FAST = "com/SaintAlfonzo/creativecommon/perf/IwGrowthFast";
	private static final String FAST_DESC = "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunkSection;"
			+ "Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;III)V";

	/**
	 * Mixin simple name -> mod ids it serves (any one loaded is enough). Every mixin whose target is in another mod has
	 * a row (creative_split.md 7.17), so Common loads with any of these mods absent.
	 */
	private static final Map<String, List<String>> NEEDS_MOD = Map.ofEntries(
			Map.entry("IwServerLevelTickMixin", List.of("immersive_weathering")),
			Map.entry("IwBlockGrowthHandlerMixin", List.of("immersive_weathering")),
			Map.entry("IwBlockGrowthHolderMixin", List.of("immersive_weathering")),
			Map.entry("KubeJSClientResourceScriptsMixin", List.of("kubejs")),
			Map.entry("KubeJSEventHandlerContainerMixin", List.of("kubejs")),
			Map.entry("KubeJSEventHandlerListenMixin", List.of("kubejs")),
			Map.entry("KubeJSScriptManagerInvoker", List.of("kubejs")),
			Map.entry("KubeJSStartupJarScriptsMixin", List.of("kubejs")),
			Map.entry("KubeJSVirtualDataPackListMixin", List.of("kubejs")),
			Map.entry("ArmorTrimsLargeItemListsMixin", List.of("armor_trims")),
			Map.entry("WorldEditSkipStateIdInitMixin", List.of("worldedit")),
			Map.entry("WorldEditBlockStateIdAccessMixin", List.of("worldedit")),
			Map.entry("QuarkSpiralSpiresTeleportMixin", List.of("quark")),
			Map.entry("CreateGravityDimensionMixin", List.of("create_gravity")),
			Map.entry("CreateGravityConfigAccessor", List.of("create_gravity")),
			Map.entry("LowGravityPullMixin", List.of("create_gravity")),
			Map.entry("LowGravitySlowPullMixin", List.of("create_gravity")),
			Map.entry("LowGravityArrowMixin", List.of("create_gravity")),
			Map.entry("LowGravityLlamaSpitMixin", List.of("create_gravity")),
			Map.entry("LowGravityBoatMixin", List.of("create_gravity")),
			Map.entry("LowGravityThrowableMixin", List.of("create_gravity")),
			Map.entry("LowGravityItemPhysicMixin", List.of("itemphysic")),
			Map.entry("NeaWhistleGoalSkipMixin", List.of("nock_enough_arrows")),
			// effect mixin from Creative Brewing (plan_cb_polish.md 2, 2026-11-06); the Create tea pipe icon went to
			// Engineering and the Fruits Delight remap to Delight 2026-11-07 (creative_engineering.md 7.1)
			Map.entry("PhantomSpawnerBaneSkipMixin", List.of("better_beacons")));

	/**
	 * Tested version range of every mod in {@link #NEEDS_MOD} (Maven syntax, as in mods.toml): from the version the pack
	 * runs to the mod's next minor release, where internal classes most often change. Outside it the mod's mixins are
	 * skipped with one warning (GatedMixinPlugin), so a public install on other versions boots without those fixes and
	 * tooltips instead of crashing. Raise a bound only after checking the mixins against that jar; the same table is the
	 * dependency list in creative_engineering.md 7.1. KubeJS is also required in mods.toml over the same range, because
	 * Common's startup scripts cannot load without its KubeJS mixins.
	 */
	private static final Map<String, String> MOD_RANGE = Map.ofEntries(
			Map.entry("kubejs", "[1902.6.2-build.73,1902.7)"),
			Map.entry("immersive_weathering", "[1.19.2-1.2.10,1.19.2-1.3)"),
			Map.entry("armor_trims", "[1.3.3,1.4)"),
			Map.entry("worldedit", "[7.2.12,7.3)"),
			Map.entry("quark", "[3.4-418,3.5)"),
			Map.entry("create_gravity", "[1.2.0,1.3)"),
			Map.entry("itemphysic", "[1.6.7,1.7)"),
			Map.entry("nock_enough_arrows", "[1.2.0,1.3)"),
			Map.entry("better_beacons", "[1.0.2,1.1)"),
			Map.entry("immersive_portals", "[2.3.7,2.4)"),
			Map.entry("heartstone", "[1.19.2-1.1.2,1.19.2-1.2)"));

	public CommonMixinPlugin() {
		super("[creativecommon]", NEEDS_MOD, MOD_RANGE);
	}

	/** The ItemPhysic low gravity mixin calls LowGravity, which needs Create Gravity too; the table gate takes any one mod. */
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.endsWith(".LowGravityItemPhysicMixin") && !modOn("create_gravity")) return false;
		return super.shouldApplyMixin(targetClassName, mixinClassName);
	}

	/** Immersive Portals' mixin config; its MixinChunkMap_C overwrites ChunkMap.playerLoadedChunk (m_183760_) as private. */
	private static final String PORTALS_CONFIG = "imm_ptl.mixins.json";
	private boolean portalsConformed;

	private static final String HEARTSTONE_CONFIG = "heartstone.mixins.json";
	private static final String HEARTSTONE_MIXIN = "net.mehvahdjukaar.heartstone.mixins.forge.LevelRendererMixin";
	/** Below the default 1000 that Immersive Portals' MixinLevelRenderer uses, so Heartstone's mixin applies first. */
	private static final int HEARTSTONE_PRIORITY = 999;
	private static final String CONFIG_CLASS = "org.spongepowered.asm.mixin.transformer.Config";
	private boolean heartstoneOrdered;
	private boolean heartstoneMissLogged;

	/** Every config of this batch is prepared (its MixinInfos exist) before any plugin's acceptTargets runs. */
	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
		orderHeartstoneBeforePortals();
		conformPortalsOverwrites();
	}

	/**
	 * Serene Seasons' access transformer makes every ChunkMap method public ({@code ChunkMap *()}); Immersive Portals
	 * 2.3.7's {@code MixinChunkMap_C} overwrites {@code m_183760_} (playerLoadedChunk) as private, and its config does not
	 * set {@code overwrites.conformVisibility}, so Mixin throws "cannot reduce visibility" when ChunkMap loads (world join).
	 * Turning that option on for IP's config makes Mixin take its standard path instead: one "Static binding violation"
	 * warning and the overwrite upgraded to public. This has to happen before ChunkMap's target context is built (the
	 * visibility check runs there, before any plugin preApply), and acceptTargets runs once every config is prepared, long
	 * before any server class loads. Fields reached with Unsafe as in orderHeartstoneBeforePortals; any failure is logged
	 * and leaves Mixin as it was.
	 */
	private void conformPortalsOverwrites() {
		if (portalsConformed) return;
		portalsConformed = true;
		if (!modOn("immersive_portals")) return;
		try {
			sun.misc.Unsafe unsafe = unsafe();
			Field registry = Class.forName(CONFIG_CLASS, false, Mixins.class.getClassLoader()).getDeclaredField("allConfigs");
			Map<?, ?> configs = (Map<?, ?>) unsafe.getObject(unsafe.staticFieldBase(registry), unsafe.staticFieldOffset(registry));
			for (Object config : configs.values()) {
				IMixinConfig mixinConfig = (IMixinConfig) read(unsafe, config, "config");
				if (mixinConfig == null || !PORTALS_CONFIG.equals(mixinConfig.getName())) continue;
				Object options = read(unsafe, mixinConfig, "overwriteOptions");
				if (options == null) break;
				unsafe.putBoolean(options, unsafe.objectFieldOffset(options.getClass().getDeclaredField("conformAccessModifiers")), true);
				LOGGER.info("[compat] Immersive Portals overwrites conform to target visibility (Serene Seasons widens ChunkMap)");
				return;
			}
			LOGGER.warn("[compat] {} not found; Immersive Portals will crash on world load with Serene Seasons", PORTALS_CONFIG);
		} catch (ReflectiveOperationException | RuntimeException e) {
			LOGGER.error("[compat] could not set conformVisibility on {}; Immersive Portals will crash on world load with Serene Seasons", PORTALS_CONFIG, e);
		}
	}

	/**
	 * Heartstone 1.1.2's {@code LevelRendererMixin} @ModifyArg's the {@code renderEntity} call in
	 * {@code LevelRenderer.renderLevel}; Immersive Portals 2.3.7's {@code MixinLevelRenderer} @Redirect's the same call.
	 * Both sit at priority 1000 and IP's config registers first, so the redirect lands first, Heartstone's injector
	 * (require 1) no longer finds its call and the client crashes at launch. Applied the other way round both work: the
	 * ModifyArg rewrites the argument in front of the call and the redirect then takes that call over. Lowering the
	 * Heartstone mixin's priority before LevelRenderer loads (its target context sorts mixins by priority) gives that
	 * order. Mixin does not export or open its transformer package to mods, so the fields are reached with Unsafe and
	 * only the exported IMixinConfig / IMixinInfo interfaces are called. Runs only with both mods loaded and inside
	 * their tested ranges; any failure is logged and leaves Mixin as it was.
	 */
	private void orderHeartstoneBeforePortals() {
		if (heartstoneOrdered) return;
		// Heartstone's LevelRendererMixin is a client mixin: nothing to reorder on a dedicated server.
		if (!net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient() || !modOn("heartstone") || !modOn("immersive_portals")) {
			heartstoneOrdered = true;
			return;
		}
		try {
			sun.misc.Unsafe unsafe = unsafe();
			// Mixins.getConfigs() is only the queue of configs not yet selected (selectConfigs removes each one), so read
			// Config's static registry of every config instead.
			Field registry = Class.forName(CONFIG_CLASS, false, Mixins.class.getClassLoader()).getDeclaredField("allConfigs");
			Map<?, ?> configs = (Map<?, ?>) unsafe.getObject(unsafe.staticFieldBase(registry), unsafe.staticFieldOffset(registry));
			for (Object config : configs.values()) {
				IMixinConfig mixinConfig = (IMixinConfig) read(unsafe, config, "config");
				if (mixinConfig == null || !HEARTSTONE_CONFIG.equals(mixinConfig.getName())) continue;
				for (String list : List.of("pendingMixins", "mixins")) {
					for (Object mixin : (List<?>) read(unsafe, mixinConfig, list)) {
						IMixinInfo info = (IMixinInfo) mixin;
						if (!HEARTSTONE_MIXIN.equals(info.getClassName())) continue;
						int before = info.getPriority();
						unsafe.putInt(mixin, unsafe.objectFieldOffset(mixin.getClass().getDeclaredField("priority")), HEARTSTONE_PRIORITY);
						heartstoneOrdered = true;
						LOGGER.info("[compat] Heartstone LevelRendererMixin priority {} -> {} so it applies before Immersive Portals' renderEntity redirect",
								before, info.getPriority());
						return;
					}
				}
			}
			// Heartstone's config is not prepared yet; preApply tries again on each of our mixins until LevelRenderer.
			if (!heartstoneMissLogged) {
				heartstoneMissLogged = true;
				LOGGER.info("[compat] Heartstone LevelRendererMixin not prepared yet ({} configs registered), will retry", configs.size());
			}
		} catch (ReflectiveOperationException | RuntimeException e) {
			heartstoneOrdered = true;
			LOGGER.error("[compat] could not reorder Heartstone's LevelRendererMixin; the client will crash with Immersive Portals", e);
		}
	}

	private static Object read(sun.misc.Unsafe unsafe, Object owner, String field) throws ReflectiveOperationException {
		Field f = owner.getClass().getDeclaredField(field);
		return unsafe.getObject(owner, unsafe.objectFieldOffset(f));
	}

	private static sun.misc.Unsafe unsafe() throws ReflectiveOperationException {
		Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
		f.setAccessible(true);
		return (sun.misc.Unsafe) f.get(null);
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		orderHeartstoneBeforePortals();
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		if (!TICK_MIXIN.equals(mixinClassName)) return;
		if ("false".equals(System.getProperty("creativecommon.iwFastTick"))) {
			LOGGER.info("[perf] Immersive Weathering random-tick fast path off (-Dcreativecommon.iwFastTick=false)");
			return;
		}
		for (MethodNode m : targetClass.methods) {
			if (!m.name.endsWith("$callTick") || !isFrom(m, IW_MIXIN)) continue;
			if (!HANDLER_DESC.equals(m.desc)) {
				LOGGER.warn("[perf] Immersive Weathering callTick has an unknown signature {}, left as it is", m.desc);
				return;
			}
			// Parameter slots (0 = this): 6 = section min x, 7 = min z, 12 = section, 13 = min y, 15 = pos, 16 = state
			InsnList body = new InsnList();
			body.add(new VarInsnNode(Opcodes.ALOAD, 0));
			body.add(new VarInsnNode(Opcodes.ALOAD, 12));
			body.add(new VarInsnNode(Opcodes.ALOAD, 15));
			body.add(new VarInsnNode(Opcodes.ALOAD, 16));
			body.add(new VarInsnNode(Opcodes.ILOAD, 6));
			body.add(new VarInsnNode(Opcodes.ILOAD, 13));
			body.add(new VarInsnNode(Opcodes.ILOAD, 7));
			body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, FAST, "blockTick", FAST_DESC, false));
			body.add(new InsnNode(Opcodes.RETURN));
			m.instructions = body;
			m.tryCatchBlocks.clear();
			m.localVariables = null;
			m.visibleLocalVariableAnnotations = null;
			m.invisibleLocalVariableAnnotations = null;
			m.maxStack = Math.max(m.maxStack, 7);
			m.maxLocals = Math.max(m.maxLocals, Type.getArgumentsAndReturnSizes(m.desc) >> 2);
			LOGGER.info("[perf] Immersive Weathering callTick handler {} now calls IwGrowthFast.blockTick", m.name);
			return;
		}
		LOGGER.warn("[perf] Immersive Weathering callTick handler not found in {}, left as it is", targetClassName);
	}

	private static boolean isFrom(MethodNode m, String mixin) {
		return isFrom(m.visibleAnnotations, mixin) || isFrom(m.invisibleAnnotations, mixin);
	}

	private static boolean isFrom(List<AnnotationNode> annotations, String mixin) {
		if (annotations == null) return false;
		for (AnnotationNode a : annotations) {
			if (!MERGED.equals(a.desc) || a.values == null) continue;
			for (int i = 0; i + 1 < a.values.size(); i += 2) {
				if ("mixin".equals(a.values.get(i)) && mixin.equals(a.values.get(i + 1))) return true;
			}
		}
		return false;
	}
}
