package com.SaintAlfonzo.baby_mobs.client.family;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationChannel.Interpolations;
import net.minecraft.client.animation.AnimationChannel.Targets;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.AnimationDefinition.Builder;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * 26.1 {@code BabyAxolotlAnimation} keyframes, unchanged (the 1.19.2 animation classes take the same data). The unused
 * dash animation is left out. Applied by {@code MiscBabies.BabyAxolotlModel}.
 */
final class MiscAxolotlAnimation {
    private MiscAxolotlAnimation() {}

   public static final AnimationDefinition BABY_AXOLOTL_IDLE_FLOOR = Builder.withLength(2.88F)
      .looping()
      .addAnimation(
         "body",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_front_leg",
         new AnimationChannel(
            Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(160.0F, -10.0F, -37.5F), Interpolations.CATMULLROM)}
         )
      )
      .addAnimation(
         "right_front_leg",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.2F, 0.0F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "right_hind_leg",
         new AnimationChannel(
            Targets.ROTATION, new Keyframe[]{new Keyframe(1.72F, KeyframeAnimations.degreeVec(360.0F, 0.0F, -45.0F), Interpolations.CATMULLROM)}
         )
      )
      .addAnimation(
         "left_front_leg",
         new AnimationChannel(
            Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(160.0F, 10.0F, 37.5F), Interpolations.CATMULLROM)}
         )
      )
      .addAnimation(
         "left_front_leg",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(-0.2F, 0.0F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "left_hind_leg",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 37.5F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "tail",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.72F, KeyframeAnimations.degreeVec(0.0F, -14.0F, 0.0F), Interpolations.LINEAR),
               new Keyframe(0.96F, KeyframeAnimations.degreeVec(0.0F, -21.0F, 0.0F), Interpolations.LINEAR),
               new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, -25.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.degreeVec(0.0F, -16.75F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.84F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "head",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.04F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -6.0F), Interpolations.CATMULLROM),
               new Keyframe(1.32F, KeyframeAnimations.degreeVec(-6.45F, -1.45F, -6.5F), Interpolations.LINEAR),
               new Keyframe(1.48F, KeyframeAnimations.degreeVec(-6.45F, -1.45F, -6.5F), Interpolations.LINEAR),
               new Keyframe(2.84F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -6.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 38.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.28F, KeyframeAnimations.degreeVec(0.0F, 45.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.64F, KeyframeAnimations.degreeVec(0.0F, 45.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.8F, KeyframeAnimations.degreeVec(0.0F, 38.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.04F, KeyframeAnimations.degreeVec(0.0F, -50.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.32F, KeyframeAnimations.degreeVec(0.0F, -59.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.6F, KeyframeAnimations.degreeVec(0.0F, -59.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.84F, KeyframeAnimations.degreeVec(0.0F, -50.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "top_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(33.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.28F, KeyframeAnimations.degreeVec(47.4F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.64F, KeyframeAnimations.degreeVec(47.4F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.8F, KeyframeAnimations.degreeVec(33.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .build();
   public static final AnimationDefinition IDLE_FLOOR_UNDERWATER = Builder.withLength(5.12F)
      .looping()
      .addAnimation(
         "body",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(5.12F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "body",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 1.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(5.12F, KeyframeAnimations.posVec(0.0F, 1.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_front_leg",
         new AnimationChannel(
            Targets.ROTATION, new Keyframe[]{new Keyframe(5.04F, KeyframeAnimations.degreeVec(190.0F, -15.0F, -30.0F), Interpolations.CATMULLROM)}
         )
      )
      .addAnimation(
         "right_front_leg",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(0.3F, 0.0F, 0.5F), Interpolations.CATMULLROM),
               new Keyframe(5.12F, KeyframeAnimations.posVec(0.3F, 0.0F, 0.5F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_hind_leg",
         new AnimationChannel(
            Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(360.0F, 0.0F, -30.0F), Interpolations.CATMULLROM)}
         )
      )
      .addAnimation(
         "right_hind_leg",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(0.18F, 0.0F, 0.0F), Interpolations.LINEAR),
               new Keyframe(5.12F, KeyframeAnimations.posVec(0.18F, 0.0F, 0.0F), Interpolations.LINEAR)
            }
         )
      )
      .addAnimation(
         "left_front_leg",
         new AnimationChannel(
            Targets.ROTATION, new Keyframe[]{new Keyframe(5.04F, KeyframeAnimations.degreeVec(190.0F, 18.0F, 30.0F), Interpolations.CATMULLROM)}
         )
      )
      .addAnimation(
         "left_front_leg",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(-0.1F, 0.0F, 0.5F), Interpolations.CATMULLROM),
               new Keyframe(5.12F, KeyframeAnimations.posVec(-0.1F, 0.0F, 0.5F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_hind_leg",
         new AnimationChannel(
            Targets.ROTATION, new Keyframe[]{new Keyframe(5.04F, KeyframeAnimations.degreeVec(180.0F, 0.0F, 30.0F), Interpolations.CATMULLROM)}
         )
      )
      .addAnimation(
         "left_hind_leg",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(-0.2F, 0.0F, 0.0F), Interpolations.LINEAR),
               new Keyframe(5.12F, KeyframeAnimations.posVec(-0.2F, 0.0F, 0.0F), Interpolations.LINEAR)
            }
         )
      )
      .addAnimation(
         "tail",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.04F, KeyframeAnimations.degreeVec(0.0F, -12.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.56F, KeyframeAnimations.degreeVec(0.0F, 12.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(5.12F, KeyframeAnimations.degreeVec(0.0F, -12.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(3.12F, KeyframeAnimations.degreeVec(0.0F, -16.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(5.12F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -16.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.08F, KeyframeAnimations.degreeVec(0.0F, 12.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(5.12F, KeyframeAnimations.degreeVec(0.0F, -16.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "top_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.56F, KeyframeAnimations.degreeVec(-16.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(5.12F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .build();
   public static final AnimationDefinition IDLE_UNDERWATER = Builder.withLength(5.2F)
      .looping()
      .addAnimation(
         "body",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.9F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.6F, KeyframeAnimations.degreeVec(-2.6F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(5.2F, KeyframeAnimations.degreeVec(-8.9F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "body",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.6F, KeyframeAnimations.posVec(0.0F, -0.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(5.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_front_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, -20.0F, -55.0F), Interpolations.CATMULLROM),
               new Keyframe(2.6F, KeyframeAnimations.degreeVec(1.0F, -20.0F, -65.0F), Interpolations.CATMULLROM),
               new Keyframe(5.2F, KeyframeAnimations.degreeVec(-5.0F, -20.0F, -55.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_front_leg",
         new AnimationChannel(
            Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.15F, -0.3391F, 0.0876F), Interpolations.CATMULLROM)}
         )
      )
      .addAnimation(
         "right_hind_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(60.0F, -60.0F, 15.0F), Interpolations.CATMULLROM),
               new Keyframe(2.6F, KeyframeAnimations.degreeVec(75.0F, -60.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(5.2F, KeyframeAnimations.degreeVec(60.0F, -60.0F, 15.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_hind_leg",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(5.2F, KeyframeAnimations.posVec(0.0F, -0.4F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "left_front_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 20.0F, 50.0F), Interpolations.CATMULLROM),
               new Keyframe(2.6F, KeyframeAnimations.degreeVec(0.0F, 20.0F, 60.0F), Interpolations.CATMULLROM),
               new Keyframe(5.2F, KeyframeAnimations.degreeVec(0.0F, 20.0F, 50.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_front_leg",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(2.68F, KeyframeAnimations.posVec(-0.15F, -0.4F, 0.3F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "left_hind_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-135.0F, -50.0F, -330.0F), Interpolations.CATMULLROM),
               new Keyframe(2.6F, KeyframeAnimations.degreeVec(-160.0F, -60.0F, -295.0F), Interpolations.CATMULLROM),
               new Keyframe(5.2F, KeyframeAnimations.degreeVec(-135.0F, -50.0F, -330.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_hind_leg",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(5.2F, KeyframeAnimations.posVec(0.0F, -0.5F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "tail",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.6F, KeyframeAnimations.degreeVec(0.0F, -32.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(5.2F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "head",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(11.7F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.6F, KeyframeAnimations.degreeVec(2.4F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(5.2F, KeyframeAnimations.degreeVec(11.7F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -25.6F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(3.12F, KeyframeAnimations.degreeVec(0.0F, 16.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(5.2F, KeyframeAnimations.degreeVec(0.0F, -25.6F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 23.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.08F, KeyframeAnimations.degreeVec(0.0F, -26.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(5.2F, KeyframeAnimations.degreeVec(0.0F, 23.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "top_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-19.2F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.6F, KeyframeAnimations.degreeVec(12.3F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(5.2F, KeyframeAnimations.degreeVec(-19.2F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .build();
   public static final AnimationDefinition BABY_AXOLOTL_SWIM = Builder.withLength(1.0F)
      .looping()
      .addAnimation(
         "body",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.24F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.68F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), Interpolations.LINEAR)
            }
         )
      )
      .addAnimation(
         "body",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.52F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.96F, KeyframeAnimations.posVec(0.0F, -0.05F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_front_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(310.0F, 70.0F, 400.0F), Interpolations.CATMULLROM),
               new Keyframe(0.24F, KeyframeAnimations.degreeVec(330.0F, 70.0F, 420.0F), Interpolations.CATMULLROM),
               new Keyframe(0.52F, KeyframeAnimations.degreeVec(290.0F, 65.0F, 384.0F), Interpolations.CATMULLROM),
               new Keyframe(0.76F, KeyframeAnimations.degreeVec(290.0F, 70.0F, 380.0F), Interpolations.CATMULLROM),
               new Keyframe(0.96F, KeyframeAnimations.degreeVec(310.0F, 70.0F, 400.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_hind_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(105.0F, -95.0F, 180.0F), Interpolations.CATMULLROM),
               new Keyframe(0.28F, KeyframeAnimations.degreeVec(105.0F, -85.0F, 180.0F), Interpolations.CATMULLROM),
               new Keyframe(0.52F, KeyframeAnimations.degreeVec(105.0F, -80.0F, 180.0F), Interpolations.CATMULLROM),
               new Keyframe(0.76F, KeyframeAnimations.degreeVec(105.0F, -95.0F, 180.0F), Interpolations.CATMULLROM),
               new Keyframe(0.96F, KeyframeAnimations.degreeVec(105.0F, -95.0F, 180.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_front_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-50.0F, -70.0F, -40.0F), Interpolations.CATMULLROM),
               new Keyframe(0.52F, KeyframeAnimations.degreeVec(-60.0F, -68.0F, -30.0F), Interpolations.CATMULLROM),
               new Keyframe(0.76F, KeyframeAnimations.degreeVec(-55.0F, -70.0F, -30.0F), Interpolations.CATMULLROM),
               new Keyframe(0.96F, KeyframeAnimations.degreeVec(-50.0F, -70.0F, -40.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_hind_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(70.0F, -80.0F, 15.0F), Interpolations.CATMULLROM),
               new Keyframe(0.28F, KeyframeAnimations.degreeVec(130.0F, -80.0F, -45.0F), Interpolations.CATMULLROM),
               new Keyframe(0.52F, KeyframeAnimations.degreeVec(120.0F, -70.0F, -35.0F), Interpolations.CATMULLROM),
               new Keyframe(0.76F, KeyframeAnimations.degreeVec(80.0F, -70.0F, 5.0F), Interpolations.CATMULLROM),
               new Keyframe(0.96F, KeyframeAnimations.degreeVec(70.0F, -80.0F, 15.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "tail",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.52F, KeyframeAnimations.degreeVec(0.0F, -15.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.96F, KeyframeAnimations.degreeVec(0.0F, 13.36F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "head",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.28F, KeyframeAnimations.degreeVec(-13.9F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.72F, KeyframeAnimations.degreeVec(14.23F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.0F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -72.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.2F, KeyframeAnimations.degreeVec(0.0F, -79.9F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.64F, KeyframeAnimations.degreeVec(0.0F, -38.1F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.96F, KeyframeAnimations.degreeVec(0.0F, -72.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 72.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.2F, KeyframeAnimations.degreeVec(0.0F, 86.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.64F, KeyframeAnimations.degreeVec(0.0F, 26.7F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.96F, KeyframeAnimations.degreeVec(0.0F, 72.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "top_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-57.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.2F, KeyframeAnimations.degreeVec(-68.7F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.64F, KeyframeAnimations.degreeVec(-24.2F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.96F, KeyframeAnimations.degreeVec(-57.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .build();
   public static final AnimationDefinition AXOLOTL_WALK_FLOOR = Builder.withLength(2.72F)
      .looping()
      .addAnimation(
         "body",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR),
               new Keyframe(2.72F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR)
            }
         )
      )
      .addAnimation(
         "right_front_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(2.5F, 10.0F, -30.0F), Interpolations.CATMULLROM),
               new Keyframe(1.32F, KeyframeAnimations.degreeVec(2.5F, -22.5F, -30.0F), Interpolations.CATMULLROM),
               new Keyframe(2.72F, KeyframeAnimations.degreeVec(2.5F, 10.0F, -30.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_front_leg",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(1.92F, KeyframeAnimations.posVec(0.5F, 0.0F, 0.8F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "right_hind_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(260.0F, -10.0F, 230.0F), Interpolations.CATMULLROM),
               new Keyframe(1.32F, KeyframeAnimations.degreeVec(90.0F, -20.0F, 50.0F), Interpolations.CATMULLROM),
               new Keyframe(2.72F, KeyframeAnimations.degreeVec(260.0F, -10.0F, 230.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_hind_leg",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(0.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.72F, KeyframeAnimations.posVec(0.5F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_front_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-2.5F, 32.5F, 30.0F), Interpolations.CATMULLROM),
               new Keyframe(1.32F, KeyframeAnimations.degreeVec(-2.5F, -12.0F, 30.0F), Interpolations.CATMULLROM),
               new Keyframe(2.72F, KeyframeAnimations.degreeVec(-2.5F, 32.5F, 30.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_front_leg",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(-0.5F, 0.0F, 0.7F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "left_hind_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-2.5F, -30.0F, 30.0F), Interpolations.CATMULLROM),
               new Keyframe(1.32F, KeyframeAnimations.degreeVec(2.5F, 12.0F, 30.0F), Interpolations.CATMULLROM),
               new Keyframe(2.72F, KeyframeAnimations.degreeVec(-2.5F, -30.0F, 30.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_hind_leg",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(-0.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.72F, KeyframeAnimations.posVec(-0.5F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "tail",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -10.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.32F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.72F, KeyframeAnimations.degreeVec(0.0F, -10.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "head",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -7.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.32F, KeyframeAnimations.degreeVec(0.0F, 6.7F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.72F, KeyframeAnimations.degreeVec(0.0F, -7.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 38.1F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.08F, KeyframeAnimations.degreeVec(0.0F, 45.6F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.44F, KeyframeAnimations.degreeVec(0.0F, 45.6F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.72F, KeyframeAnimations.degreeVec(0.0F, 38.1F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -50.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.08F, KeyframeAnimations.degreeVec(0.0F, -59.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.36F, KeyframeAnimations.degreeVec(0.0F, -59.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.72F, KeyframeAnimations.degreeVec(0.0F, -50.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "top_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(33.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.08F, KeyframeAnimations.degreeVec(47.4F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.44F, KeyframeAnimations.degreeVec(47.4F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.72F, KeyframeAnimations.degreeVec(33.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .build();
   public static final AnimationDefinition WALK_FLOOR_UNDERWATER = Builder.withLength(2.04F)
      .looping()
      .addAnimation(
         "body",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -5.0F, 6.0F), Interpolations.CATMULLROM),
               new Keyframe(1.04F, KeyframeAnimations.degreeVec(0.0F, 5.0F, -4.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.degreeVec(0.0F, -5.0F, 6.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "body",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 1.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.posVec(0.0F, 1.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_front_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -30.0F), Interpolations.CATMULLROM),
               new Keyframe(0.72F, KeyframeAnimations.degreeVec(0.0F, 9.0F, -30.0F), Interpolations.CATMULLROM),
               new Keyframe(1.12F, KeyframeAnimations.degreeVec(0.0F, 13.0F, -24.0F), Interpolations.CATMULLROM),
               new Keyframe(1.44F, KeyframeAnimations.degreeVec(0.0F, 9.0F, -30.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -30.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_front_leg",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.96F, KeyframeAnimations.posVec(0.4F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_hind_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(95.0F, -20.0F, 70.0F), Interpolations.CATMULLROM),
               new Keyframe(0.68F, KeyframeAnimations.degreeVec(255.0F, -20.0F, 230.0F), Interpolations.CATMULLROM),
               new Keyframe(1.12F, KeyframeAnimations.degreeVec(280.0F, -25.0F, 255.0F), Interpolations.CATMULLROM),
               new Keyframe(1.52F, KeyframeAnimations.degreeVec(255.0F, -20.0F, 230.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.degreeVec(95.0F, -20.0F, 70.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_hind_leg",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(0.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.96F, KeyframeAnimations.posVec(0.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.posVec(0.5F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_front_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, -20.0F, 30.0F), Interpolations.CATMULLROM),
               new Keyframe(0.72F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 25.0F), Interpolations.CATMULLROM),
               new Keyframe(1.44F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 25.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.degreeVec(-5.0F, -20.0F, 30.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_front_leg",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(-0.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.96F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.posVec(-0.5F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_hind_leg",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 15.0F, 25.0F), Interpolations.CATMULLROM),
               new Keyframe(0.72F, KeyframeAnimations.degreeVec(-10.0F, -20.0F, 35.0F), Interpolations.CATMULLROM),
               new Keyframe(1.44F, KeyframeAnimations.degreeVec(-10.0F, -20.0F, 35.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.degreeVec(5.0F, 15.0F, 25.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_hind_leg",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(-0.7F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.96F, KeyframeAnimations.posVec(-0.4F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.posVec(-0.7F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "tail",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 16.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.56F, KeyframeAnimations.degreeVec(0.0F, 1.12F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.44F, KeyframeAnimations.degreeVec(0.0F, 17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.degreeVec(0.0F, 16.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "head",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 4.0F, -5.8F), Interpolations.CATMULLROM),
               new Keyframe(1.04F, KeyframeAnimations.degreeVec(0.0F, -4.0F, 5.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.degreeVec(0.0F, 4.0F, -5.8F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "left_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -60.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.24F, KeyframeAnimations.degreeVec(0.0F, -45.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.degreeVec(0.0F, -60.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "right_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 38.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(0.64F, KeyframeAnimations.degreeVec(0.0F, 53.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.degreeVec(0.0F, 38.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "top_gills",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-34.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.04F, KeyframeAnimations.degreeVec(-41.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.04F, KeyframeAnimations.degreeVec(-34.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .build();
   public static final AnimationDefinition BABY_AXOLOTL_PLAY_DEAD = Builder.withLength(0.04F)
      .addAnimation(
         "body",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 30.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "body", new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "body", new AnimationChannel(Targets.SCALE, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.LINEAR)})
      )
      .addAnimation(
         "right_front_leg",
         new AnimationChannel(
            Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(202.35F, -30.33F, -40.82F), Interpolations.CATMULLROM)}
         )
      )
      .addAnimation(
         "right_front_leg",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.2F, 0.0F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "right_front_leg",
         new AnimationChannel(Targets.SCALE, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.LINEAR)})
      )
      .addAnimation(
         "right_hind_leg",
         new AnimationChannel(
            Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(418.0F, -50.0F, 35.8F), Interpolations.CATMULLROM)}
         )
      )
      .addAnimation(
         "right_hind_leg",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "right_hind_leg",
         new AnimationChannel(Targets.SCALE, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.LINEAR)})
      )
      .addAnimation(
         "left_front_leg",
         new AnimationChannel(
            Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(177.0F, 22.76F, 15.9F), Interpolations.CATMULLROM)}
         )
      )
      .addAnimation(
         "left_front_leg",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(-0.2F, 0.0F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "left_front_leg",
         new AnimationChannel(Targets.SCALE, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.LINEAR)})
      )
      .addAnimation(
         "left_hind_leg",
         new AnimationChannel(
            Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(16.4287F, -37.6467F, 16.9822F), Interpolations.CATMULLROM)}
         )
      )
      .addAnimation(
         "left_hind_leg",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "left_hind_leg",
         new AnimationChannel(Targets.SCALE, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.LINEAR)})
      )
      .addAnimation(
         "tail",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -18.67F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "tail", new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "tail", new AnimationChannel(Targets.SCALE, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.LINEAR)})
      )
      .addAnimation(
         "head",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.21F, -0.95F, -6.33F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "head", new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "head", new AnimationChannel(Targets.SCALE, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.LINEAR)})
      )
      .addAnimation(
         "left_gills",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 43.21F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "left_gills",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "left_gills",
         new AnimationChannel(Targets.SCALE, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.LINEAR)})
      )
      .addAnimation(
         "right_gills",
         new AnimationChannel(
            Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -55.87F, 0.0F), Interpolations.CATMULLROM)}
         )
      )
      .addAnimation(
         "right_gills",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "right_gills",
         new AnimationChannel(Targets.SCALE, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.LINEAR)})
      )
      .addAnimation(
         "top_gills",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(43.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "top_gills",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "top_gills",
         new AnimationChannel(Targets.SCALE, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.LINEAR)})
      )
      .addAnimation(
         "root",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "root", new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR)})
      )
      .addAnimation(
         "root", new AnimationChannel(Targets.SCALE, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.LINEAR)})
      )
      .build();
}
