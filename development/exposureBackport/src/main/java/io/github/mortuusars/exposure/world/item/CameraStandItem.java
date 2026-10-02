package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CameraStandItem extends Item {
   public CameraStandItem(Properties properties) {
      super(properties);
   }

   @NotNull
   public InteractionResult useOn(UseOnContext context) {
      Direction direction = context.getClickedFace();
      if (direction == Direction.DOWN) {
         return InteractionResult.FAIL;
      } else {
         Level level = context.getLevel();
         BlockPlaceContext blockPlaceContext = new BlockPlaceContext(context);
         BlockPos blockPos = blockPlaceContext.getClickedPos();
         ItemStack itemStack = context.getItemInHand();
         Vec3 pos = Vec3.atBottomCenterOf(blockPos);
         AABB aabb = Exposure.EntityTypes.CAMERA_STAND.get().getDimensions().makeBoundingBox(pos.x(), pos.y(), pos.z());
         if (level.noCollision(null, aabb) && level.getEntities(null, aabb).isEmpty()) {
            if (level instanceof ServerLevel serverLevel) {
               CameraStandEntity cameraStand = Exposure.EntityTypes.CAMERA_STAND
                  .get()
                  .create(serverLevel, itemStack.getTag(), itemStack.hasCustomHoverName() ? itemStack.getHoverName() : null, context.getPlayer(), blockPos, MobSpawnType.SPAWN_EGG, true, true);
               if (cameraStand == null) {
                  return InteractionResult.FAIL;
               }

               if (context.getPlayer() != null) {
                  cameraStand.setOwnerPlayer(context.getPlayer());
               }

               cameraStand.moveTo(cameraStand.getX(), cameraStand.getY(), cameraStand.getZ(), 0.0F, 0.0F);
               serverLevel.addFreshEntityWithPassengers(cameraStand);
               cameraStand.playPlaceSound();
               cameraStand.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
            }

            itemStack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            return InteractionResult.FAIL;
         }
      }
   }

   public InteractionResult interactWithBoat(Player player, InteractionHand hand, Boat boat) {
      if (!player.isSecondaryUseActive()) {
         return InteractionResult.PASS;
      } else {
         ItemStack itemStack = player.getItemInHand(hand);
         if (!(itemStack.getItem() instanceof CameraStandItem)) {
            return InteractionResult.PASS;
         } else if (boat.getPassengers().size() >= 2) {
            return InteractionResult.PASS;
         } else if (player.level instanceof ServerLevel serverLevel) {
            CameraStandEntity cameraStand = Exposure.EntityTypes.CAMERA_STAND
               .get()
               .create(serverLevel, itemStack.getTag(), itemStack.hasCustomHoverName() ? itemStack.getHoverName() : null, player, boat.blockPosition(), MobSpawnType.SPAWN_EGG, true, true);
            if (cameraStand == null) {
               return InteractionResult.FAIL;
            } else {
               cameraStand.setOwnerPlayer(player);
               cameraStand.startRiding(boat);
               boat.positionRider(cameraStand);
               serverLevel.addFreshEntityWithPassengers(cameraStand);
               cameraStand.playPlaceSound();
               cameraStand.gameEvent(GameEvent.ENTITY_PLACE, player);
               itemStack.shrink(1);
               player.setItemInHand(hand, itemStack);
               return InteractionResult.SUCCESS;
            }
         } else {
            return InteractionResult.CONSUME;
         }
      }
   }
}
