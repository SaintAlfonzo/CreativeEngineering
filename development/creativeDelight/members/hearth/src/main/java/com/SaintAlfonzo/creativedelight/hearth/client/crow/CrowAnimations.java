package com.SaintAlfonzo.creativedelight.hearth.client.crow;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.animation.AnimationChannel.Interpolations;
import net.minecraft.client.animation.AnimationChannel.Targets;
import net.minecraft.client.animation.AnimationDefinition.Builder;

/** Hearth and Harvest 1.4.1 crow keyframe animations, unchanged (the keyframe system is in 1.19.2). */
public class CrowAnimations {
    public static final AnimationDefinition walking = Builder.withLength(0.5F)
        .looping()
        .addAnimation(
            "rightleg",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.125F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.375F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "leftleg",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.125F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.375F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .build();
    public static final AnimationDefinition idle = Builder.withLength(2.005F)
        .looping()
        .addAnimation(
            "body",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "wings",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "head",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "lowerbeak",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "tail",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .build();
    public static final AnimationDefinition flying = Builder.withLength(0.401F)
        .looping()
        .addAnimation(
            "legs",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.4F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "head",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.1F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "tail",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.1F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(35.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.4F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "leftwing",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, -75.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.1F, KeyframeAnimations.degreeVec(-9.7724F, -11.4356F, -124.0192F), Interpolations.CATMULLROM),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, -75.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(-9.7724F, 11.4356F, -25.9808F), Interpolations.CATMULLROM),
                    new Keyframe(0.4F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, -75.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "rightwing",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 75.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.1F, KeyframeAnimations.degreeVec(-9.7724F, 11.4356F, 124.0192F), Interpolations.CATMULLROM),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 75.0F), Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(-9.7724F, -11.4356F, 25.9808F), Interpolations.CATMULLROM),
                    new Keyframe(0.4F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 75.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .build();
    public static final AnimationDefinition gliding = Builder.withLength(2.0F)
        .looping()
        .addAnimation(
            "legs",
            new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(45.0F, 0.0F, 0.0F), Interpolations.LINEAR)})
        )
        .addAnimation(
            "head",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "tail",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "leftwing",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, -85.0F), Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, -90.0F), Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, -85.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "rightwing",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 85.0F), Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 90.0F), Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 85.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .build();
    public static final AnimationDefinition sitting = Builder.withLength(2.005F)
        .looping()
        .addAnimation(
            "body",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "body",
            new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -2.5F, 0.0F), Interpolations.LINEAR)})
        )
        .addAnimation(
            "wings",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "wings",
            new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -2.5F, 0.0F), Interpolations.LINEAR)})
        )
        .addAnimation(
            "head",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "head",
            new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -2.5F, 0.0F), Interpolations.LINEAR)})
        )
        .addAnimation(
            "legs", new AnimationChannel(Targets.SCALE, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0, 0.0, 1.0), Interpolations.LINEAR)})
        )
        .addAnimation(
            "lowerbeak",
            new AnimationChannel(
                Targets.ROTATION,
                new Keyframe[]{
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
                }
            )
        )
        .addAnimation(
            "tail",
            new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(27.5F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
        )
        .build();
}
