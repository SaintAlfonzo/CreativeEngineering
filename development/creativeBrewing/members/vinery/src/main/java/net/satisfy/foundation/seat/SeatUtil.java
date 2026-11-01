package net.satisfy.foundation.seat;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.foundation.registry.FoundationEntities;
import net.satisfy.foundation.util.ItemInteractionResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Everything around sitting on blocks. Keeps track of chair entities per
 * dimension and pos (server side only).
 */
public final class SeatUtil {
    private SeatUtil() {
    }

    private static final Map<ResourceLocation, Map<BlockPos, Pair<ChairEntity, BlockPos>>> CHAIRS = new HashMap<>();

    public static BlockPos getPreviousPlayerPosition(Player player, ChairEntity chairEntity) {
        if (!player.level.isClientSide()) {
            Map<BlockPos, Pair<ChairEntity, BlockPos>> chairs = CHAIRS.get(getDimensionTypeId(player.level));
            if (chairs != null) {
                for (Pair<ChairEntity, BlockPos> pair : chairs.values()) {
                    if (pair.getFirst() == chairEntity) {
                        return pair.getSecond();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Call from {@code useItemOn} of your seat block. Needs an empty hand, no sneaking.
     *
     * @param extraHeight added to the default seat height of 0.25
     */
    public static ItemInteractionResult onUse(Level world, Player player, InteractionHand hand, BlockHitResult hit, double extraHeight) {
        if (world.isClientSide) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (isPlayerSitting(player)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (hit.getDirection() == Direction.DOWN) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        BlockPos hitPos = hit.getBlockPos();
        if (!isOccupied(world, hitPos) && player.getItemInHand(hand).isEmpty()) {
            ChairEntity chair = FoundationEntities.CHAIR.get().create(world);
            if (chair == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            chair.moveTo(hitPos.getX() + 0.5D, hitPos.getY() + 0.25D + extraHeight, hitPos.getZ() + 0.5D, 0, 0);
            if (addChairEntity(world, hitPos, chair, player.blockPosition())) {
                world.addFreshEntity(chair);
                player.startRiding(chair);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static boolean isOccupied(Level world, BlockPos pos) {
        ResourceLocation id = getDimensionTypeId(world);
        return CHAIRS.containsKey(id) && CHAIRS.get(id).containsKey(pos);
    }

    public static boolean isPlayerSitting(Player player) {
        for (Map<BlockPos, Pair<ChairEntity, BlockPos>> chairs : CHAIRS.values()) {
            for (Pair<ChairEntity, BlockPos> pair : chairs.values()) {
                if (pair.getFirst().hasPassenger(player))
                    return true;
            }
        }
        return false;
    }

    private static ResourceLocation getDimensionTypeId(Level world) {
        return world.dimension().location();
    }

    /** Call from {@code onRemove}, kicks the player off when the block is gone. */
    public static void onStateReplaced(Level world, BlockPos pos) {
        if (!world.isClientSide) {
            ChairEntity entity = getChairEntity(world, pos);
            if (entity != null) {
                removeChairEntity(world, pos);
                entity.ejectPassengers();
            }
        }
    }

    public static boolean addChairEntity(Level world, BlockPos blockPos, ChairEntity entity, BlockPos playerPos) {
        if (!world.isClientSide) {
            CHAIRS.computeIfAbsent(getDimensionTypeId(world), id -> new HashMap<>()).put(blockPos, Pair.of(entity, playerPos));
            return true;
        }
        return false;
    }

    public static void removeChairEntity(Level world, BlockPos pos) {
        if (!world.isClientSide) {
            Map<BlockPos, Pair<ChairEntity, BlockPos>> chairs = CHAIRS.get(getDimensionTypeId(world));
            if (chairs != null) {
                chairs.remove(pos);
            }
        }
    }

    public static ChairEntity getChairEntity(Level world, BlockPos pos) {
        if (!world.isClientSide()) {
            Map<BlockPos, Pair<ChairEntity, BlockPos>> chairs = CHAIRS.get(getDimensionTypeId(world));
            if (chairs != null && chairs.containsKey(pos))
                return chairs.get(pos).getFirst();
        }
        return null;
    }
}
