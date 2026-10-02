package io.github.mortuusars.exposure.util;

public class Fov {
   public static double fovToFocalLength(double fov) {
      return fovToFocalLength(fov, 36.0);
   }

   public static double focalLengthToFov(double focalLength) {
      return focalLengthToFov(focalLength, 36.0F);
   }

   public static double fovToFocalLength(double fov, double filmWidth) {
      return filmWidth / (2.0 * Math.tan(Math.toRadians(fov / 2.0)));
   }

   public static double focalLengthToFov(double focalLength, float filmWidth) {
      return (double)((float)(2.0 * Math.toDegrees(Math.atan((double)filmWidth / (2.0 * focalLength)))));
   }
}
