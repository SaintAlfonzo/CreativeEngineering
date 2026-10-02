package io.github.mortuusars.exposure.util;

import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;

public record PointOfView(Vec3 pos, Vec3 dir) {
   public static PointOfView of(CameraHolder holder) {
      return of(holder.asHolderEntity());
   }

   public static PointOfView of(Entity entity) {
      return new PointOfView(
         entity.position().add(0.0, (double)entity.getEyeHeight(), 0.0),
         Vec3.directionFromRotation(Mth.clamp(entity.getXRot(), -89.0F, 89.0F), entity.getYRot())
      );
   }

   public PointOfView rotateX(double degrees) {
      Vec3 upVector = new Vec3(0.0, 1.0, 0.0);
      Vec3 rightVector = this.dir.cross(upVector).normalize();
      if (Mth.equal(rightVector.length(), 0.0)) {
         rightVector = new Vec3(1.0, 0.0, 0.0).cross(this.dir).normalize();
      }

      Vec3 rotated = Vec3Util.rotateVector(this.dir(), rightVector, Math.toRadians(degrees));
      return new PointOfView(this.pos, rotated);
   }

   public PointOfView rotateY(double degrees) {
      return new PointOfView(this.pos, this.dir.yRot((float)Math.toRadians(degrees)));
   }

   public PointOfView move(double x, double y, double z) {
      return new PointOfView(this.pos.add(x, y, z), this.dir);
   }

   public PointOfView reverseDirection() {
      return new PointOfView(this.pos, this.dir.reverse());
   }

   public PointOfView limitMaxDistance(CameraHolder holder, double distance) {
      return this.limitMaxDistance(holder.asHolderEntity(), distance);
   }

   public PointOfView limitMaxDistance(Entity entity, double distance) {
      float maxDistance = this.getMaxCameraDistance(entity, this.pos, this.dir, (float)distance);
      return new PointOfView(this.pos.add(this.dir.normalize().scale((double)(-maxDistance))), this.dir);
   }

   private float getMaxCameraDistance(Entity entity, Vec3 position, Vec3 direction, float maxDistance) {
      for (int i = 0; i < 8; i++) {
         float xOff = (float)((i & 1) * 2 - 1);
         float yOff = (float)((i >> 1 & 1) * 2 - 1);
         float zOff = (float)((i >> 2 & 1) * 2 - 1);
         Vec3 pos = position.add((double)(xOff * 0.1F), (double)(yOff * 0.1F), (double)(zOff * 0.1F));
         Vec3 endPos = pos.add(direction.scale((double)(-maxDistance)));
         HitResult hitResult = entity.level.clip(new ClipContext(pos, endPos, Block.VISUAL, Fluid.NONE, entity));
         if (hitResult.getType() != Type.MISS) {
            float distanceSqr = (float)hitResult.getLocation().distanceToSqr(position);
            if (distanceSqr < Mth.square(maxDistance)) {
               maxDistance = Mth.sqrt(distanceSqr);
            }
         }
      }

      return maxDistance;
   }
}
