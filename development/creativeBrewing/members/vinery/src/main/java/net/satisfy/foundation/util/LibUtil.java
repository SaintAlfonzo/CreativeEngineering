package net.satisfy.foundation.util;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collection;
import java.util.Objects;

/** Grab bag of small helpers, stuff that did not fit anywhere else. */
public final class LibUtil {
    private LibUtil() {
    }

    /** Players that have this chunk loaded. */
    public static Collection<ServerPlayer> tracking(ServerLevel world, ChunkPos pos) {
        Objects.requireNonNull(world, "The world cannot be null");
        Objects.requireNonNull(pos, "The chunk pos cannot be null");
        return world.getChunkSource().chunkMap.getPlayers(pos, false);
    }

    public static Collection<ServerPlayer> tracking(ServerLevel world, BlockPos pos) {
        Objects.requireNonNull(pos, "BlockPos cannot be null");
        return tracking(world, new ChunkPos(pos));
    }

    /** 50 -> 0.5f */
    public static float getInPercent(int i) {
        return (float) i / 100.0F;
    }

    /** After eating/drinking: gives {@code returnItem} back (bowl, bottle...) and awards stats. */
    public static ItemStack convertStackAfterFinishUsing(LivingEntity entity, ItemStack used, Item returnItem, Item usedItem) {
        if (entity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, used);
            serverPlayer.awardStat(Stats.ITEM_USED.get(usedItem));
        }

        if (used.isEmpty()) {
            return new ItemStack(returnItem);
        }
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            ItemStack itemStack2 = new ItemStack(returnItem);
            if (!player.getInventory().add(itemStack2)) {
                player.drop(itemStack2, false);
            }
        }
        return used;
    }

    /** New log block with oak log properties. */
    public static RotatedPillarBlock logBlock() {
        return new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG));
    }

    public static boolean isIndexInRange(int index, int startInclusive, int endInclusive) {
        return index >= startInclusive && index <= endInclusive;
    }

    /** Drops the item out of the given face of the block, respecting the collision shape. */
    public static void popResourceFromFace(Level level, BlockPos blockPos, Direction side, ItemStack itemStack) {
        BlockState blockState = level.getBlockState(blockPos);
        double itemWidth = EntityType.ITEM.getWidth();
        double itemHeight = EntityType.ITEM.getHeight();
        VoxelShape shape = blockState.getCollisionShape(level, blockPos);
        double posX = blockPos.getX() + 0.5;
        double posY = blockPos.getY() + 0.5;
        double posZ = blockPos.getZ() + 0.5;
        double offsetX = 0.0;
        double offsetY = 0.0;
        double offsetZ = 0.0;
        if (!shape.isEmpty()) {
            switch (side) {
                case DOWN -> {
                    posY = blockPos.getY() - shape.min(Direction.Axis.Y);
                    offsetY = -itemHeight * 2.0;
                }
                case UP -> posY = blockPos.getY() + shape.max(Direction.Axis.Y);
                case NORTH -> {
                    posZ = blockPos.getZ() + shape.min(Direction.Axis.Z);
                    offsetZ = -itemWidth;
                }
                case SOUTH -> {
                    posZ = blockPos.getZ() + shape.max(Direction.Axis.Z);
                    offsetZ = itemWidth;
                }
                case WEST -> {
                    posX = blockPos.getX() + shape.min(Direction.Axis.X);
                    offsetX = -itemWidth;
                }
                case EAST -> {
                    posX = blockPos.getX() + shape.max(Direction.Axis.X);
                    offsetX = itemWidth;
                }
            }
        }

        int i = side.getStepX();
        int j = side.getStepY();
        int k = side.getStepZ();
        double deltaX = i == 0 ? Mth.nextDouble(level.random, -0.1, 0.1) : i * 0.1;
        double deltaY = j == 0 ? Mth.nextDouble(level.random, 0.0, 0.1) : j * 0.1 + 0.1;
        double deltaZ = k == 0 ? Mth.nextDouble(level.random, -0.1, 0.1) : k * 0.1;
        if (!level.isClientSide && !itemStack.isEmpty() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            ItemEntity itemEntity = new ItemEntity(level, posX + offsetX, posY + offsetY, posZ + offsetZ, itemStack, deltaX, deltaY, deltaZ);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }
}
