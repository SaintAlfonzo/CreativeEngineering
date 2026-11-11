package com.SaintAlfonzo.creativedelight.hearth.crow;

import com.SaintAlfonzo.creativedelight.hearth.entity.HHEntities;
import java.lang.reflect.Method;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * Hearth and Harvest 1.4.1 {@code CrowShoulderEvents}: once a second a crow on a shoulder hops off when an item worth fetching
 * lies within 8 blocks. The shoulder setters are protected in 1.19.2, so they are reached by reflection (SRG names).
 */
public final class CrowShoulder {
    private static final Method SET_LEFT = ObfuscationReflectionHelper.findMethod(Player.class, "m_36362_", CompoundTag.class);
    private static final Method SET_RIGHT = ObfuscationReflectionHelper.findMethod(Player.class, "m_36364_", CompoundTag.class);

    private CrowShoulder() {
    }

    public static boolean isCrow(CompoundTag tag) {
        return !tag.isEmpty() && tag.getString("id").equals(EntityType.getKey(HHEntities.CROW.get()).toString());
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        if (!player.isAlive() || player.isSpectator() || player.isSleeping() || !CrowConfig.FETCH_ITEMS.get() || !CrowConfig.LEAVE_SHOULDER_TO_FETCH.get()) {
            return;
        }
        boolean left = isCrow(player.getShoulderEntityLeft());
        boolean right = isCrow(player.getShoulderEntityRight());
        if ((left || right) && hasItemToFetch(player)) {
            if (left) {
                release(player, player.getShoulderEntityLeft(), SET_LEFT, 0.4D);
            } else {
                release(player, player.getShoulderEntityRight(), SET_RIGHT, -0.4D);
            }
        }
    }

    private static boolean hasItemToFetch(ServerPlayer player) {
        return !player.level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(8.0D, 4.0D, 8.0D),
                item -> CrowRetrieveItemsGoal.isWorthFetching(item, player)).isEmpty();
    }

    private static void release(ServerPlayer player, CompoundTag tag, Method clearShoulder, double sideOffset) {
        ServerLevel level = player.getLevel();
        EntityType.create(tag, level).ifPresent(entity -> {
            if (entity instanceof CrowEntity crow) {
                double yaw = Math.toRadians(player.getYRot());
                crow.setOwnerUUID(player.getUUID());
                crow.moveTo(player.getX() + Math.cos(yaw) * sideOffset, player.getY() + 1.3D, player.getZ() + Math.sin(yaw) * sideOffset, player.getYRot(), 0.0F);
                if (level.addWithUUID(crow)) {
                    try {
                        clearShoulder.invoke(player, new CompoundTag());
                    } catch (ReflectiveOperationException e) {
                        crow.discard();
                    }
                }
            }
        });
    }
}
