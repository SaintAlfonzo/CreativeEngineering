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
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.SaintAlfonzo.creativecommon.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

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
public class CEMixinPlugin extends GatedMixinPlugin {
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
			Map.entry("FluidTagInputMixin", List.of("immersiveengineering")),
			Map.entry("WorldEditSkipStateIdInitMixin", List.of("worldedit")),
			Map.entry("WorldEditBlockStateIdAccessMixin", List.of("worldedit")),
			Map.entry("QuarkSpiralSpiresTeleportMixin", List.of("quark")),
			Map.entry("CreateGravityDimensionMixin", List.of("create_gravity")),
			Map.entry("NeaWhistleGoalSkipMixin", List.of("nock_enough_arrows")),
			Map.entry("SequencedAssemblyRecipeMixin", List.of("create")),
			Map.entry("SubMenuConfigScreenMixin", List.of("create")),
			Map.entry("ArmorBarRendererAccessor", List.of("detailab")),
			Map.entry("ArmorBarRendererOverflowTickMixin", List.of("detailab")),
			Map.entry("JeiRecipeGuiLogicMixin", List.of("jei")),
			Map.entry("JeiMissingSlotsErrorAccessor", List.of("jei")),
			Map.entry("JeiRecipeLayoutTooltipMixin", List.of("jei")),
			Map.entry("JeiFluidHelperTooltipMixin", List.of("jei")),
			Map.entry("JeiFluidTankScaleMixin", List.of("jei")),
			Map.entry("JeiRecipesGuiTooltipMixin", List.of("jei")),
			Map.entry("JeiIngredientFilterAccessor", List.of("jei")),
			Map.entry("JeiIngredientFilterMixin", List.of("jei")),
			Map.entry("JeiStarterMixin", List.of("jei")),
			Map.entry("JeiPluginCallerMixin", List.of("jei")),
			Map.entry("JeiIngredientManagerMixin", List.of("jei")),
			Map.entry("LCEasyScreenHelperMixin", List.of("lightmanscurrency")),
			Map.entry("LCEasyGuiGraphicsMixin", List.of("lightmanscurrency")),
			Map.entry("IcebergTitleBreakMixin", List.of("legendarytooltips")), // targets Iceberg, reads a Legendary Tooltips class; LT requires Iceberg
			Map.entry("PrettyPipesTerminalTooltipMixin", List.of("prettypipes")),
			Map.entry("FtbTooltipListAccessor", List.of("ftblibrary")),
			Map.entry("FtbGuiHelperMixin", List.of("ftblibrary")),
			Map.entry("FtbScreenWrapperTooltipMixin", List.of("ftblibrary")),
			Map.entry("FtbMenuScreenWrapperTooltipMixin", List.of("ftblibrary")),
			Map.entry("FtbItemTaskTooltipMixin", List.of("ftbquests")),
			Map.entry("FtbQuestDescriptionTooltipMixin", List.of("ftbquests")),
			Map.entry("WaresAgreementTooltipMixin", List.of("wares")),
			Map.entry("PatchouliBookTooltipMixin", List.of("patchouli")),
			Map.entry("AeBaseScreenTooltipMixin", List.of("ae2")),
			Map.entry("AeTableRendererTooltipMixin", List.of("ae2")),
			Map.entry("AePatternEncodingTooltipMixin", List.of("ae2")),
			Map.entry("AeGuideItemTooltipMixin", List.of("ae2")));

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
			Map.entry("jei", "[11.8.1.1034,11.9)"),
			Map.entry("immersive_weathering", "[1.19.2-1.2.10,1.19.2-1.3)"),
			Map.entry("armor_trims", "[1.3.3,1.4)"),
			Map.entry("immersiveengineering", "[1.19.2-9.2.4-170,1.19.2-9.3)"),
			Map.entry("worldedit", "[7.2.12,7.3)"),
			Map.entry("quark", "[3.4-418,3.5)"),
			Map.entry("create_gravity", "[1.2.0,1.3)"),
			Map.entry("nock_enough_arrows", "[1.2.0,1.3)"),
			Map.entry("create", "[0.5.1.i,0.5.2)"),
			Map.entry("detailab", "[2.6.4,2.7)"),
			Map.entry("lightmanscurrency", "[1.19.2-2.2.1.3g,1.19.2-2.3)"),
			Map.entry("legendarytooltips", "[1.4.0,1.5)"),
			Map.entry("prettypipes", "[1.13.6,1.14)"),
			Map.entry("ftblibrary", "[1902.4.1-build.236,1902.5)"),
			Map.entry("ftbquests", "[1902.5.10-build.497,1902.6)"),
			Map.entry("wares", "[1.2.9,1.3)"),
			Map.entry("patchouli", "[1.19.2-77,1.19.3)"),
			Map.entry("ae2", "[12.9.12,12.10)"));

	public CEMixinPlugin() {
		super("[creativecommon]", NEEDS_MOD, MOD_RANGE);
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
