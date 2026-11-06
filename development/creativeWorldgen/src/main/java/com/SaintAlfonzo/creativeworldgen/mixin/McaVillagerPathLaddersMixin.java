package com.SaintAlfonzo.creativeworldgen.mixin;

import forge.net.mca.entity.ai.pathfinder.ExtendedPathNodeType;
import forge.net.mca.entity.ai.pathfinder.VillagerLandPathNodeMaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ladders in MCA's villager node maker, which has no vertical moves: a ladder block in mid-air was OPEN (the walker
 * dropped through it) and a path never went up one. A block in both {@code #minecraft:climbable} and
 * {@code #creativeengineering:villager_ladders} (KubeJS blocktags.js, every "ladder" block; no vines, ropes, chains or
 * scaffolding) is now:
 * <ul>
 *   <li>WALKABLE in getLandNodeType, so a villager can stand at any height of a ladder and a walk off a floor into a
 *       ladder shaft stops on its top rung;</li>
 *   <li>a vertical successor in getNeighbors (m_6065_): the ladder block straight above or below a node, when the
 *       villager's box fits there. Leaving at the top is MCA's normal one-block step up from the top rung, so the ladder
 *       has to reach the level of the floor it leads to;</li>
 *   <li>the start node (m_7171_) when the villager is on one; MCA's start search walks down through pathfindable blocks
 *       and would start a path at the foot of the ladder.</li>
 * </ul>
 * Climbing itself is vanilla (a mob jumping in a ladder climbs); the navigation and move control parts are
 * {@code GroundPathNavigationClimbMixin} and {@code MoveControlVillagerLadderMixin}. feature_writeup/mob_block_tweaks.md 2.8.
 */
@Mixin(value = VillagerLandPathNodeMaker.class, remap = false)
public abstract class McaVillagerPathLaddersMixin extends NodeEvaluator {

    @Unique
    private static final TagKey<Block> CE_VILLAGER_LADDERS =
            TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("creativeengineering", "villager_ladders"));

    @Shadow(remap = false)
    protected abstract ExtendedPathNodeType getNodeType(Mob entity, int x, int y, int z);

    @Shadow(remap = false)
    abstract float getPenalty(ExtendedPathNodeType type);

    @Unique
    private static boolean creativeengineering$isLadder(BlockState state) {
        return state.is(BlockTags.CLIMBABLE) && state.is(CE_VILLAGER_LADDERS);
    }

    @Inject(method = "getLandNodeType", at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativeengineering$standOnLadders(BlockGetter world, BlockPos.MutableBlockPos pos,
                                                           CallbackInfoReturnable<ExtendedPathNodeType> cir) {
        if (creativeengineering$isLadder(world.getBlockState(pos))) cir.setReturnValue(ExtendedPathNodeType.WALKABLE);
    }

    @Inject(method = "m_6065_", at = @At("RETURN"), cancellable = true, remap = false)
    private void creativeengineering$climbSuccessors(Node[] successors, Node node, CallbackInfoReturnable<Integer> cir) {
        int count = cir.getReturnValueI();
        for (int dy = -1; dy <= 1; dy += 2) {
            int y = node.y + dy;
            if (count >= successors.length) break;
            if (!creativeengineering$isLadder(this.level.getBlockState(new BlockPos(node.x, y, node.z)))) continue;
            ExtendedPathNodeType type = this.getNodeType(this.mob, node.x, y, node.z);
            float penalty = this.getPenalty(type);
            if (penalty < 0.0F) continue;
            Node next = this.getNode(node.x, y, node.z);
            if (next.closed) continue;
            next.type = type.toVanilla();
            next.costMalus = Math.max(next.costMalus, penalty);
            successors[count++] = next;
        }
        cir.setReturnValue(count);
    }

    @Inject(method = "m_7171_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$startOnLadder(CallbackInfoReturnable<Node> cir) {
        BlockPos feet = this.mob.blockPosition();
        if (this.mob.isOnGround() || !creativeengineering$isLadder(this.level.getBlockState(feet))) return;
        Node start = this.getNode(feet.getX(), feet.getY(), feet.getZ());
        ExtendedPathNodeType type = this.getNodeType(this.mob, feet.getX(), feet.getY(), feet.getZ());
        start.type = type.toVanilla();
        start.costMalus = this.getPenalty(type);
        cir.setReturnValue(start);
    }
}
