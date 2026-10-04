package com.SaintAlfonzo.creativeengineering.compat;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Mixin config plugin for creativeengineering.mixins.json. It exists for one job: Mixin will not
 * inject into a method another mixin merged into a class, so changing Fruits Delight's own mixin
 * handler needs a direct bytecode edit.
 *
 * FRUITS DELIGHT LEAF CHECK:
 *   Fruits Delight's BlockStateBaseMixin merges {@code handler$<id>$fruitsdelight$getCollisionShape$passableLeave}
 *   into {@code BlockStateBase}, called at the head of every collision-shape query. It starts with
 *   {@code this.is(BlockTags.LEAVES)}, a tag lookup run for every query in the game, before it looks at
 *   who is asking. After {@code FruitsDelightLeafCheckMixin} (an empty mixin with a high priority, so it
 *   applies after Fruits Delight's) has been applied, {@link #postApply} turns that first test into
 *   {@link FruitsDelightHooks#isLeafForProjectile}, which checks for a projectile first.
 *   Instructions {@code aload_0; getstatic BlockTags.LEAVES; invokevirtual is} become
 *   {@code aload_0; aload_3; invokestatic isLeafForProjectile}: same stack effect and the same branch
 *   after it, so no stack-map frames change. If the handler is not found or looks different (Fruits
 *   Delight absent or updated) nothing is changed.
 */
public class CreativeEngineeringMixinPlugin implements IMixinConfigPlugin {

    private static final String ANCHOR_MIXIN = "FruitsDelightLeafCheckMixin";
    private static final String HANDLER_PART = "fruitsdelight$getCollisionShape$passableLeave";
    private static final String BLOCK_TAGS = "net/minecraft/tags/BlockTags";
    private static final String HOOKS = "com/SaintAlfonzo/creativeengineering/compat/FruitsDelightHooks";
    private static final String HOOK_DESC =
            "(Lnet/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase;"
                    + "Lnet/minecraft/world/phys/shapes/CollisionContext;)Z";

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
        if (mixinClassName.endsWith(ANCHOR_MIXIN)) {
            patchFruitsDelightHandler(targetClass);
        }
    }

    private static void patchFruitsDelightHandler(ClassNode targetClass) {
        for (MethodNode method : targetClass.methods) {
            if (!method.name.startsWith("handler$") || !method.name.contains(HANDLER_PART)) continue;
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode field) || field.getOpcode() != Opcodes.GETSTATIC
                        || !BLOCK_TAGS.equals(field.owner)) continue;
                AbstractInsnNode next = insn.getNext();
                if (!(next instanceof MethodInsnNode call) || call.getOpcode() != Opcodes.INVOKEVIRTUAL) return;
                method.instructions.remove(insn);
                method.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 3));
                method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "isLeafForProjectile", HOOK_DESC, false));
                return;
            }
            return;
        }
    }
}
