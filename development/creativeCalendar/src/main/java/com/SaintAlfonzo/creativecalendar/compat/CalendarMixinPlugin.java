package com.SaintAlfonzo.creativecalendar.compat;

import com.SaintAlfonzo.creativecalendar.shared.GatedMixinPlugin;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;

/**
 * Mixin config plugin for creativecalendar.mixins.json (feature_writeup/creative_calendar.md 4.1): the table form of the
 * shared {@link GatedMixinPlugin} (development/shared/mixinplugin). A listed mixin applies only when one of its mods is
 * loaded, so Calendar boots the same without Serene Seasons, SeasonHUD, Better Days, Xaero's Minimap or Pretty Rain.
 * Mixins not listed (vanilla targets: ServerLevelTimePauseMixin) always apply. The mixins use SRG names, remap = false.
 *
 * postApply: CoolRainSeasonMixin is a marker; this edits Cool Rain's merged tickRain handler directly, sending its
 * getBiome and getPrecipitation calls to {@link CoolRainHooks} (Mixin 0.8.5 regex selectors skip merged methods).
 */
public class CalendarMixinPlugin extends GatedMixinPlugin {

	private static final List<String> SERENE = List.of("sereneseasons");

	private static final Map<String, List<String>> NEEDS_MOD = Map.ofEntries(
			Map.entry("CropSeasonGrowHookMixin", List.of("immersiveengineering", "the_dirty_stuff", "nethersdelight")),
			Map.entry("SereneSeasonsMeltThrottleMixin", SERENE),
			Map.entry("SereneSeasonsClockMixin", SERENE),
			Map.entry("SereneSeasonsTimeSkipMixin", SERENE),
			Map.entry("SereneSeasonsClientClockMixin", SERENE),
			Map.entry("SeasonBiomeConfigMixin", SERENE),
			Map.entry("SeasonServerConfigMixin", SERENE),
			Map.entry("SeasonHooksTypesMixin", SERENE),
			Map.entry("SeasonHooksClientMixin", SERENE),
			Map.entry("SeasonFertilityMixin", SERENE),
			// crop seasons for grass spread (CropSeasons reads Serene Seasons); the IW one also needs Immersive Weathering
			Map.entry("GrassSpreadSeasonMixin", SERENE),
			Map.entry("IWGrowthSeasonMixin", List.of("immersive_weathering")),
			// modded grass blocks that spread outside the vanilla random tick; Serene Seasons checked at runtime
			Map.entry("IWGrassSpreadSeasonMixin", List.of("immersive_weathering")),
			Map.entry("BotaniaGrassSeasonMixin", List.of("botania")),
			Map.entry("SeasonColorUtilMixin", SERENE),
			// vanilla target, but it reads SeasonTypes, which needs Serene Seasons' classes
			Map.entry("SeasonRainColumnMixin", SERENE),
			Map.entry("BetterDaysConfigMixin", List.of("betterdays")),
			Map.entry("BetterDaysTimePauseMixin", List.of("betterdays")),
			Map.entry("XaeroTimeInfoMixin", List.of("xaerominimap")),
			Map.entry("SeasonHudHelperMixin", List.of("seasonhud")),
			Map.entry("SeasonHudCurrentSeasonMixin", List.of("seasonhud")),
			Map.entry("PrettyRainSeasonMixin", List.of("particlerain")),
			Map.entry("CoolRainSeasonMixin", List.of("coolrain")),
			// Serene Seasons' melt kept under Snow! Real Magic (snow_real_magic.md R6); needs both, see shouldApplyMixin
			Map.entry("SereneSeasonsSrmMeltMixin", List.of("snowrealmagic")));

	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarMixinPlugin.class);

	private static final String HOOKS = "com/SaintAlfonzo/creativecalendar/compat/CoolRainHooks";
	private static final String COOL_RAIN_HANDLER_DESC =
			"(Lnet/minecraft/client/Camera;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V";
	private static final String LEVEL_READER = "net/minecraft/world/level/LevelReader";
	private static final String GET_BIOME = "m_204166_";
	private static final String GET_BIOME_DESC = "(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;";
	private static final String BIOME = "net/minecraft/world/level/biome/Biome";
	private static final String GET_PRECIPITATION = "m_47530_";
	private static final String GET_PRECIPITATION_DESC = "()Lnet/minecraft/world/level/biome/Biome$Precipitation;";

	public CalendarMixinPlugin() {
		super("[creativecalendar]", NEEDS_MOD);
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.endsWith(".SereneSeasonsSrmMeltMixin") && !modOn("sereneseasons")) return false;
		return super.shouldApplyMixin(targetClassName, mixinClassName);
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		if (mixinClassName.endsWith(".CoolRainSeasonMixin")) patchCoolRainHandler(targetClass);
		if (mixinClassName.endsWith(".SereneSeasonsSrmMeltMixin")) dropSrmMeltCancel(targetClass);
	}

	private static final String SS_WORLD_TICK_DESC = "(Lnet/minecraftforge/event/TickEvent$LevelTickEvent;)V";

	/**
	 * Snow! Real Magic's HEAD callback on Serene Seasons' {@code onWorldTick} (merged as {@code handler$...$srm_onWorldTick},
	 * static, taking the event and the CallbackInfo) cancels the whole melt. Its call becomes POP2 (the two arguments
	 * dropped), so the CallbackInfo is never cancelled and the handler runs on (snow_real_magic.md ruling R6). SRM's
	 * own work in that callback, the weather frequency, is what the handler does itself.
	 */
	private static void dropSrmMeltCancel(ClassNode targetClass) {
		for (MethodNode method : targetClass.methods) {
			if (!"onWorldTick".equals(method.name) || !SS_WORLD_TICK_DESC.equals(method.desc)) continue;
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
				if (!(insn instanceof MethodInsnNode call) || call.getOpcode() != Opcodes.INVOKESTATIC) continue;
				if (!targetClass.name.equals(call.owner) || !call.name.endsWith("srm_onWorldTick")) continue;
				method.instructions.set(call, new InsnNode(Opcodes.POP2));
				LOGGER.info("[creativecalendar] Snow! Real Magic loaded: Serene Seasons still melts snow and ice ({} skipped)", call.name);
				return;
			}
			LOGGER.warn("[creativecalendar] Snow! Real Magic's Serene Seasons callback not found: its snow and ice melt stays off");
			return;
		}
	}

	private static void patchCoolRainHandler(ClassNode targetClass) {
		for (MethodNode method : targetClass.methods) {
			if (!method.name.startsWith("handler$") || !method.name.endsWith("$tickRain")
					|| !COOL_RAIN_HANDLER_DESC.equals(method.desc)) continue;
			boolean biome = false, precipitation = false;
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
				if (!(insn instanceof MethodInsnNode call)) continue;
				if (!biome && call.getOpcode() == Opcodes.INVOKEINTERFACE && LEVEL_READER.equals(call.owner)
						&& GET_BIOME.equals(call.name) && GET_BIOME_DESC.equals(call.desc)) {
					MethodInsnNode hook = new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getBiome",
							"(L" + LEVEL_READER + ";Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", false);
					method.instructions.set(call, hook);
					insn = hook;
					biome = true;
				} else if (!precipitation && call.getOpcode() == Opcodes.INVOKEVIRTUAL && BIOME.equals(call.owner)
						&& GET_PRECIPITATION.equals(call.name) && GET_PRECIPITATION_DESC.equals(call.desc)) {
					MethodInsnNode hook = new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getPrecipitation",
							"(L" + BIOME + ";)Lnet/minecraft/world/level/biome/Biome$Precipitation;", false);
					method.instructions.set(call, hook);
					insn = hook;
					precipitation = true;
				}
			}
			if (biome && precipitation) {
				LOGGER.info("[creativecalendar] Cool Rain's rain sounds follow the season ({})", method.name);
				return;
			}
			LOGGER.warn("[creativecalendar] Cool Rain handler {} changed (getBiome {}, getPrecipitation {}): season rain sounds partly off",
					method.name, biome, precipitation);
			return;
		}
		LOGGER.warn("[creativecalendar] Cool Rain's tickRain handler not found in {}: its rain sounds ignore the season",
				targetClass.name);
	}
}
