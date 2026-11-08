package com.SaintAlfonzo.creativedelight.compat;

import com.SaintAlfonzo.creativedelight.shared.GatedMixinPlugin;
import com.mojang.logging.LogUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Mixin config plugin for creativedelight.mixins.json: the Java compartments of creative_delight.md 12. A mixin in
 * {@code mixin.<modid>.} applies only when that mod is loaded (and, for a mod in {@link #FOR_VERSION}, only at that
 * version: those mixins fix bugs of one jar and would undo the upstream fix on a newer one). Nothing outside the
 * compartment's package references the mod, so CD boots the same without it. The gate is the shared
 * {@link GatedMixinPlugin} (development/shared/mixinplugin).
 *
 * Mixin cannot inject into a method another mixin merged into a class, so changing Fruits Delight's own mixin
 * handlers needs a direct bytecode edit: an empty, high-priority anchor mixin applies after Fruits Delight's, and
 * {@link #postApply} for that anchor edits the merged handler ({@link #PATCHES}, one row per anchor). Each edit has the
 * same stack effect and adds no branch, so no stack-map frames change; a handler that is not found or looks different
 * (Fruits Delight updated) is left unchanged.
 *
 * FRUITS DELIGHT getFoodData NULL GUARD (fruits_delight.md 2.5, the 1.1.3 fix):
 *   Fruits Delight's PlayerMixin merges a {@code @ModifyReturnValue} handler {@code ...fruitsdelight$getFoodData} into
 *   {@code Player}; it calls {@code ((FoodDataAccessor) prev).fruitsdelight$setPlayer(this)} on the returned
 *   FoodData without a null check. Mixin cannot inject into another mixin's merged handler, so after
 *   {@code FruitsDelightFoodDataAnchorMixin} (empty, high priority, so it applies after Fruits Delight's) has been
 *   applied, {@link #postApply} swaps that interface call for {@link FruitsDelightHooks#setPlayer}, which skips a
 *   null FoodData. If the call is not found a warning is logged.
 *
 * FRUITS DELIGHT LEAF CHECK (perf.md; moved from patcherMod 2026-10-27):
 *   Fruits Delight's BlockStateBaseMixin merges {@code handler$<id>$fruitsdelight$getCollisionShape$passableLeave}
 *   into {@code BlockStateBase}, called at the head of every collision-shape query. It starts with
 *   {@code this.is(BlockTags.LEAVES)}, a tag lookup run for every query in the game, before it looks at who is asking.
 *   After {@code FruitsDelightLeafCheckMixin} has been applied, {@link #postApply} turns that first test into
 *   {@link FruitsDelightHooks#isLeafForProjectile}, which checks for a projectile first. Instructions
 *   {@code aload_0; getstatic BlockTags.LEAVES; invokevirtual is} become
 *   {@code aload_0; aload_3; invokestatic isLeafForProjectile}: same stack effect and the same branch after it.
 */
public class CDMixinPlugin extends GatedMixinPlugin {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String COMPARTMENT_PREFIX = "com.SaintAlfonzo.creativedelight.mixin.";

	/** Mod id -> the only version its compartment applies to. */
	private static final Map<String, String> FOR_VERSION = Map.of("fruitsdelight", "0.5.11");

	private static final String HOOKS = "com/SaintAlfonzo/creativedelight/compat/FruitsDelightHooks";

	private static final String FD_HANDLER_PART = "fruitsdelight$getFoodData";
	private static final String FD_ACCESSOR = "dev/xkmc/fruitsdelight/events/FoodDataAccessor";
	private static final String FD_SET_PLAYER = "fruitsdelight$setPlayer";
	private static final String FD_SET_PLAYER_DESC = "(Lnet/minecraft/world/entity/player/Player;)V";
	private static final String SET_PLAYER_HOOK_DESC = "(Ljava/lang/Object;Lnet/minecraft/world/entity/player/Player;)V";

	private static final String LEAF_HANDLER_PART = "fruitsdelight$getCollisionShape$passableLeave";
	private static final String BLOCK_TAGS = "net/minecraft/tags/BlockTags";
	private static final String LEAF_HOOK_DESC = "(Lnet/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase;"
			+ "Lnet/minecraft/world/phys/shapes/CollisionContext;)Z";

	/** Anchor mixin simple name -> the edit run on its target after it has been applied. */
	private static final Map<String, Consumer<ClassNode>> PATCHES = Map.of(
			"FruitsDelightFoodDataAnchorMixin", CDMixinPlugin::patchFoodDataHandler,
			"FruitsDelightLeafCheckMixin", CDMixinPlugin::patchLeafCheckHandler);

	/**
	 * Table rows with a tested version range (the compartment gate has none): Farmer's Respite's Caffeinated tick fix,
	 * moved from Creative Common with its range (2026-11-07). Outside the range the mixin is skipped with one warning.
	 */
	private static final Map<String, java.util.List<String>> NEEDS_MOD = Map.of(
			"CaffeinatedEffectMixin", java.util.List.of("farmersrespite"));
	private static final Map<String, String> MOD_RANGE = Map.of("farmersrespite", "[1.19-2.0,1.19-2.1)");

	public CDMixinPlugin() {
		super("[creativedelight]", COMPARTMENT_PREFIX, FOR_VERSION, NEEDS_MOD, MOD_RANGE);
	}

	/**
	 * Mixins of the M8 compartments (plan_cb_publish.md 3b) that need more than their package's mod: the compartment
	 * ({@link Compartments}, mods and switch) as well. Package {@code cookingforblockheads.} and {@code incubation.} mixins
	 * need their compartment too; the incubation nest goal needs a bird mod with nests (emus or ducks and geese).
	 */
	private static final Map<String, String> NEEDS_COMPARTMENT = Map.of(
			"CuttingBoardRendererOffsetMixin", Compartments.CFB,
			"CoasterRendererOffsetMixin", Compartments.CFB,
			"EntityEmuEggLayerMixin", Compartments.INCUBATION,
			"DuckEntityEggLayerMixin", Compartments.INCUBATION,
			"GooseEntityEggLayerMixin", Compartments.INCUBATION);

	/**
	 * Mixins that reference a second mod's classes besides their package's mod: both must be loaded. The seagull
	 * lunchbox mixins test for Spice of Life's FoodContainerItem; with Alex's Mobs and no Spice of Life the apply failed
	 * with ClassMetadataNotFound and crashed the boot (scratch r1106cbb, cooking set).
	 */
	private static final Map<String, String> NEEDS_ALSO_MOD = Map.of(
			"AlexsMobsSeagullItemMixin", "solapplepie",
			"AlexsMobsSeagullStealMixin", "solapplepie",
			// Farmer's Respite coffee bushes break only while the coffee merge into Rustic Delight applies (item_merges.md 2.8)
			"CoffeeBushRemovedMixin", "rusticdelight");

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (!super.shouldApplyMixin(targetClassName, mixinClassName)) return false;
		String simple = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
		String alsoMod = NEEDS_ALSO_MOD.get(simple);
		if (alsoMod != null && !Compartments.loaded(alsoMod)) return false;
		String compartment = NEEDS_COMPARTMENT.get(simple);
		if (mixinClassName.startsWith(COMPARTMENT_PREFIX + Compartments.CFB + ".")) compartment = Compartments.CFB;
		if (mixinClassName.startsWith(COMPARTMENT_PREFIX + Compartments.INCUBATION + ".")) {
			if (!Compartments.loaded("alexsmobs") && !Compartments.loaded("untitledduckmod")) return false;
			compartment = Compartments.INCUBATION;
		}
		return compartment == null || Compartments.on(compartment);
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		Consumer<ClassNode> patch = PATCHES.get(mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1));
		if (patch != null) patch.accept(targetClass);
	}

	private static void patchFoodDataHandler(ClassNode targetClass) {
		for (MethodNode method : targetClass.methods) {
			if (!method.name.contains(FD_HANDLER_PART)) continue;
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
				if (insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKEINTERFACE
						&& FD_ACCESSOR.equals(call.owner) && FD_SET_PLAYER.equals(call.name)
						&& FD_SET_PLAYER_DESC.equals(call.desc)) {
					method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "setPlayer", SET_PLAYER_HOOK_DESC, false));
					return;
				}
			}
		}
		LOGGER.warn("[creativedelight] Fruits Delight's getFoodData handler not found in {}: null guard not applied",
				targetClass.name);
	}

	private static void patchLeafCheckHandler(ClassNode targetClass) {
		for (MethodNode method : targetClass.methods) {
			if (!method.name.startsWith("handler$") || !method.name.contains(LEAF_HANDLER_PART)) continue;
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
				if (!(insn instanceof FieldInsnNode field) || field.getOpcode() != Opcodes.GETSTATIC
						|| !BLOCK_TAGS.equals(field.owner)) continue;
				AbstractInsnNode next = insn.getNext();
				if (!(next instanceof MethodInsnNode call) || call.getOpcode() != Opcodes.INVOKEVIRTUAL) return;
				method.instructions.remove(insn);
				method.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 3));
				method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "isLeafForProjectile", LEAF_HOOK_DESC, false));
				return;
			}
			return;
		}
	}
}
