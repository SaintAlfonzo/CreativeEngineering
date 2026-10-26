package com.SaintAlfonzo.creativedelight.compat;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mixin config plugin for creative_delight.mixins.json: the Java compartments of creative_delight.md 12. A mixin in
 * {@code mixin.<modid>.} applies only when that mod is loaded (and, for a mod in {@link #FOR_VERSION}, only at that
 * version: those mixins fix bugs of one jar and would undo the upstream fix on a newer one). Nothing outside the
 * compartment's package references the mod, so CD boots the same without it.
 *
 * FRUITS DELIGHT getFoodData NULL GUARD (fruits_delight.md 2.5, the 1.1.3 fix):
 *   Fruits Delight's PlayerMixin merges a {@code @ModifyReturnValue} handler {@code ...fruitsdelight$getFoodData} into
 *   {@code Player}; it calls {@code ((FoodDataAccessor) prev).fruitsdelight$setPlayer(this)} on the returned
 *   FoodData without a null check. Mixin cannot inject into another mixin's merged handler, so after
 *   {@code FruitsDelightFoodDataAnchorMixin} (empty, high priority, so it applies after Fruits Delight's) has been
 *   applied, {@link #postApply} swaps that interface call for {@link FruitsDelightHooks#setPlayer}, which skips a
 *   null FoodData. Same stack effect, no branch added, so no stack-map frames change. If the call is not found
 *   (Fruits Delight updated) nothing is changed.
 */
public class CDMixinPlugin implements IMixinConfigPlugin {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String COMPARTMENT_PREFIX = "com.SaintAlfonzo.creativedelight.mixin.";

	/** Mod id -> the only version its compartment applies to. */
	private static final Map<String, String> FOR_VERSION = Map.of("fruitsdelight", "0.5.11");

	private static final String FOOD_DATA_ANCHOR = "FruitsDelightFoodDataAnchorMixin";
	private static final String FD_HANDLER_PART = "fruitsdelight$getFoodData";
	private static final String FD_ACCESSOR = "dev/xkmc/fruitsdelight/events/FoodDataAccessor";
	private static final String FD_SET_PLAYER = "fruitsdelight$setPlayer";
	private static final String FD_SET_PLAYER_DESC = "(Lnet/minecraft/world/entity/player/Player;)V";
	private static final String HOOKS = "com/SaintAlfonzo/creativedelight/compat/FruitsDelightHooks";
	private static final String HOOK_DESC = "(Ljava/lang/Object;Lnet/minecraft/world/entity/player/Player;)V";

	private final Map<String, Boolean> compartmentOn = new HashMap<>();

	@Override
	public void onLoad(String mixinPackage) {}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (!mixinClassName.startsWith(COMPARTMENT_PREFIX)) return true;
		String rest = mixinClassName.substring(COMPARTMENT_PREFIX.length());
		int dot = rest.indexOf('.');
		if (dot < 0) return true;
		return compartmentOn.computeIfAbsent(rest.substring(0, dot), CDMixinPlugin::compartmentOn);
	}

	private static boolean compartmentOn(String modId) {
		ModFileInfo file = LoadingModList.get().getModFileById(modId);
		if (file == null) return false;
		String want = FOR_VERSION.get(modId);
		if (want == null) return true;
		String have = file.getMods().get(0).getVersion().toString();
		if (want.equals(have)) return true;
		LOGGER.warn("[creative_delight] {} {} is not {}: its fix mixins are off (check the fixes against the new jar)",
				modId, have, want);
		return false;
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
		if (mixinClassName.endsWith(FOOD_DATA_ANCHOR)) {
			patchFoodDataHandler(targetClass);
		}
	}

	private static void patchFoodDataHandler(ClassNode targetClass) {
		for (MethodNode method : targetClass.methods) {
			if (!method.name.contains(FD_HANDLER_PART)) continue;
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
				if (insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKEINTERFACE
						&& FD_ACCESSOR.equals(call.owner) && FD_SET_PLAYER.equals(call.name)
						&& FD_SET_PLAYER_DESC.equals(call.desc)) {
					method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "setPlayer", HOOK_DESC, false));
					return;
				}
			}
		}
		LOGGER.warn("[creative_delight] Fruits Delight's getFoodData handler not found in {}: null guard not applied",
				targetClass.name);
	}
}
