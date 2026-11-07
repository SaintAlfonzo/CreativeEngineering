package com.SaintAlfonzo.creativeworldgen.mixin;

import forge.net.mca.entity.ai.pathfinder.ExtendedPathNodeType;
import forge.net.mca.entity.ai.pathfinder.VillagerLandPathNodeMaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

/**
 * MCA villagers path with MCA's own node maker (VillagerNavigation, on whatever {@code useSmarterDoorAI} says).
 * It marks every {@code #minecraft:fence_gates} block WALKABLE_DOOR in findNearbyNodeTypes, so villagers route through
 * gates, open or closed, and press against the closed ones (with useSmarterDoorAI off nothing opens them). It also
 * knows only vanilla DoorBlock, so a closed Dramatic Doors door is BLOCKED and villagers never use one, though Dramatic
 * Doors' own InteractWithDoor mixin would open it. Here: any gate is FENCE (walled off like a fence, either state),
 * and Dramatic Doors' tall and short doors get the vanilla door types the way Dramatic Doors does for WalkNodeEvaluator
 * (wooden closed = DOOR_WOOD_CLOSED, which villagers treat as walkable; other closed = DOOR_IRON_CLOSED; open = DOOR_OPEN).
 * Non-MCA villagers: {@code WalkNodeEvaluatorVillagerGateMixin}. feature_writeup/mob_block_tweaks.md section 2.7.
 */
@Mixin(value = VillagerLandPathNodeMaker.class, remap = false)
public abstract class McaVillagerPathGatesMixin {

    @Unique
    private static final TagKey<Block> CWG_TALL_DOORS = creativeengineering$tag("tall_doors");
    @Unique
    private static final TagKey<Block> CWG_SHORT_DOORS = creativeengineering$tag("short_doors");
    @Unique
    private static final TagKey<Block> CWG_TALL_WOODEN_DOORS = creativeengineering$tag("tall_wooden_doors");
    @Unique
    private static final TagKey<Block> CWG_SHORT_WOODEN_DOORS = creativeengineering$tag("short_wooden_doors");

    @Unique
    private static TagKey<Block> creativeengineering$tag(String path) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("dramaticdoors", path));
    }

    @Inject(method = "getCommonNodeType", at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativeengineering$gatesAndDramaticDoors(BlockGetter world, BlockPos pos,
                                                                  CallbackInfoReturnable<ExtendedPathNodeType> cir) {
        BlockState state = world.getBlockState(pos);
        if (state.is(BlockTags.FENCE_GATES) || state.getBlock() instanceof FenceGateBlock) {
            cir.setReturnValue(ExtendedPathNodeType.FENCE);
            return;
        }
        if (!(state.is(CWG_TALL_DOORS) || state.is(CWG_SHORT_DOORS)) || !state.hasProperty(BlockStateProperties.OPEN)) return;
        if (state.getValue(BlockStateProperties.OPEN)) {
            cir.setReturnValue(ExtendedPathNodeType.DOOR_OPEN);
        } else if (state.is(CWG_TALL_WOODEN_DOORS) || state.is(CWG_SHORT_WOODEN_DOORS)) {
            cir.setReturnValue(ExtendedPathNodeType.DOOR_WOOD_CLOSED);
        } else {
            cir.setReturnValue(ExtendedPathNodeType.DOOR_IRON_CLOSED);
        }
    }

    @Redirect(method = "findNearbyNodeTypes", remap = false, at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraft/world/level/block/state/BlockState;m_204338_(Lnet/minecraft/tags/TagKey;Ljava/util/function/Predicate;)Z"))
    private boolean creativeengineering$noWalkableGates(BlockState state, TagKey<Block> tag,
                                                        Predicate<BlockBehaviour.BlockStateBase> predicate) {
        return tag != BlockTags.FENCE_GATES && state.is(tag, predicate);
    }
}
