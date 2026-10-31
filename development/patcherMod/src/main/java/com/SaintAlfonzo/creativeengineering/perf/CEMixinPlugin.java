package com.SaintAlfonzo.creativeengineering.perf;

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
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin config plugin for creativeengineering.mixins.json. Applies every mixin as listed; its one job is perf.md 15
 * candidate C: after {@code IwServerLevelTickMixin} (priority 1500, so after Immersive Weathering's own
 * {@code ServerLevelMixin} at 1000) it rewrites IW's merged {@code callTick} handler in ServerLevel to call
 * {@link IwGrowthFast#blockTick}. Mixin will not inject into another mixin's merged method, hence the ASM.
 * Must not load game classes: it runs while ServerLevel is being transformed.
 */
public class CEMixinPlugin implements IMixinConfigPlugin {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final String IW_MIXIN = "com.ordana.immersive_weathering.mixins.ServerLevelMixin";
	private static final String TICK_MIXIN = "com.SaintAlfonzo.creativeengineering.mixin.IwServerLevelTickMixin";
	private static final String MERGED = "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;";
	/** IW 1.2.10's callTick(LevelChunk, int, CallbackInfo, captured locals...). */
	private static final String HANDLER_DESC = "(Lnet/minecraft/world/level/chunk/LevelChunk;ILorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;"
			+ "Lnet/minecraft/world/level/ChunkPos;ZIILnet/minecraft/util/profiling/ProfilerFiller;[Lnet/minecraft/world/level/chunk/LevelChunkSection;"
			+ "IILnet/minecraft/world/level/chunk/LevelChunkSection;IILnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V";
	private static final String FAST = "com/SaintAlfonzo/creativeengineering/perf/IwGrowthFast";
	private static final String FAST_DESC = "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunkSection;"
			+ "Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;III)V";

	@Override
	public void onLoad(String mixinPackage) {}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		if (!TICK_MIXIN.equals(mixinClassName)) return;
		if ("false".equals(System.getProperty("creativeengineering.iwFastTick"))) {
			LOGGER.info("[perf] Immersive Weathering random-tick fast path off (-Dcreativeengineering.iwFastTick=false)");
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
