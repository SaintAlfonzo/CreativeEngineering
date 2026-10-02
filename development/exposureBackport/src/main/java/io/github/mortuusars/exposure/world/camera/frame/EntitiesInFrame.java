package io.github.mortuusars.exposure.world.camera.frame;

import io.github.mortuusars.exposure.util.Fov;
import io.github.mortuusars.exposure.util.PointOfView;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class EntitiesInFrame {
   public static List<LivingEntity> get(CameraHolder cameraHolder, PointOfView pov, double fov) {
      return get(cameraHolder.asHolderEntity(), pov, fov);
   }

   public static List<LivingEntity> get(Entity cameraHolder, PointOfView pov, double fov) {
      fov *= 0.95;
      double focalLength = Fov.fovToFocalLength(fov);
      AABB area = new AABB(cameraHolder.blockPosition()).inflate(128.0);
      List<Entity> entities = cameraHolder.level.getEntities(null, area);
      EntitiesInFrame.FrustumCheck frustum = EntitiesInFrame.FrustumCheck.createFromCamera(pov.pos(), pov.dir(), (float)Math.toRadians(fov));
      entities.sort((entityx, entity2) -> {
         double dist1 = pov.pos().distanceTo(entityx.position());
         double dist2 = pov.pos().distanceTo(entity2.position());
         if (dist1 == dist2) {
            return 0;
         } else {
            return dist1 > dist2 ? 1 : -1;
         }
      });
      List<LivingEntity> entitiesInFrame = new ArrayList<>();

      for (Entity entity : entities) {
         if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            if (livingEntity.isAlive()
               && frustum.contains(entity.getEyePosition())
               && !(calculateVisibleDistance(pov.pos(), entity) > focalLength)
               && hasLineOfSight(pov.pos(), entity)) {
               entitiesInFrame.add(livingEntity);
            }
         }
      }

      return entitiesInFrame;
   }

   public static double calculateVisibleDistance(Vec3 cameraPos, Entity entity) {
      double distanceInBlocks = Math.sqrt(entity.distanceToSqr(cameraPos));
      AABB boundingBox = entity.getBoundingBoxForCulling();
      double size = boundingBox.getSize();
      if (Double.isNaN(size) || size == 0.0) {
         size = 0.1;
      }

      double sizeInfluence = (size - 1.0) * 0.6 + 1.0;
      double feelsRightInfluence = 1.15;
      return distanceInBlocks / sizeInfluence * feelsRightInfluence;
   }

   public static boolean hasLineOfSight(Vec3 cameraPos, Entity entity) {
      return entity.level.clip(new ClipContext(cameraPos, entity.getEyePosition(), Block.COLLIDER, Fluid.NONE, entity)).getType() == Type.MISS;
   }

   public static class FrustumCheck {
      private final Vec3 cameraPos;
      private final Vec3 forward;
      private final Vec3 right;
      private final Vec3 up;
      private final float fovRadians;

      public FrustumCheck(Vec3 cameraPos, Vec3 forward, Vec3 right, Vec3 up, float fovRadians) {
         this.cameraPos = cameraPos;
         this.forward = forward.normalize();
         this.right = right.normalize();
         this.up = up.normalize();
         this.fovRadians = fovRadians;
      }

      public boolean contains(Vec3 pos) {
         Vec3 toEntity = pos.subtract(this.cameraPos);
         double depth = toEntity.dot(this.forward);
         if (depth <= 0.0) {
            return false;
         } else {
            double horizontalOffset = toEntity.dot(this.right);
            double verticalOffset = toEntity.dot(this.up);
            double halfSize = depth * Math.tan((double)(this.fovRadians / 2.0F));
            return Math.abs(horizontalOffset) <= halfSize && Math.abs(verticalOffset) <= halfSize;
         }
      }

      public static EntitiesInFrame.FrustumCheck createFromCamera(Vec3 cameraPos, Vec3 lookVec, float fov) {
         Vec3 forward = lookVec.normalize();
         Vec3 worldUp = new Vec3(0.0, 1.0, 0.0);
         Vec3 right = forward.cross(worldUp).normalize();
         Vec3 up = right.cross(forward).normalize();
         return new EntitiesInFrame.FrustumCheck(cameraPos, forward, right, up, fov);
      }
   }
}
