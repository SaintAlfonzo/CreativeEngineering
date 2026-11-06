package com.SaintAlfonzo.baby_mobs.client.family;

import com.mojang.math.Vector3f;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.minecraft.client.animation.AnimationChannel.Interpolations.CATMULLROM;
import static net.minecraft.client.animation.AnimationChannel.Interpolations.LINEAR;
import static net.minecraft.client.animation.AnimationChannel.Targets.POSITION;
import static net.minecraft.client.animation.AnimationChannel.Targets.ROTATION;
import static net.minecraft.client.animation.AnimationChannel.Targets.SCALE;
import static net.minecraft.client.animation.KeyframeAnimations.degreeVec;
import static net.minecraft.client.animation.KeyframeAnimations.posVec;
import static net.minecraft.client.animation.KeyframeAnimations.scaleVec;

/**
 * The 26.1 keyframe animations the baby fox and baby rabbit use (26.1 {@code FoxBabyAnimation}, {@code BabyRabbitAnimation}),
 * on 1.19.2's own animation classes, and a player for them ({@link Baked}, 26.1 {@code KeyframeAnimation}) that works on
 * any model, not only a {@code HierarchicalModel}.
 */
final class PetAnimations {
    private PetAnimations() {}

    /** 26.1 {@code KeyframeAnimation}: the definition bound to the model's parts. */
    static final class Baked {
        private record Entry(ModelPart part, AnimationChannel.Target target, Keyframe[] keyframes) {}

        private final AnimationDefinition definition;
        private final List<Entry> entries = new ArrayList<>();
        private final Vector3f scratch = new Vector3f();

        Baked(AnimationDefinition definition, Function<String, ModelPart> lookup) {
            this.definition = definition;
            for (Map.Entry<String, List<AnimationChannel>> e : definition.boneAnimations().entrySet()) {
                ModelPart part = lookup.apply(e.getKey());
                if (part == null) throw new IllegalArgumentException("Cannot animate " + e.getKey() + ", which does not exist in model");
                for (AnimationChannel channel : e.getValue()) entries.add(new Entry(part, channel.target(), channel.keyframes()));
            }
        }

        /** 26.1 {@code applyWalk}. */
        void applyWalk(float animationPos, float animationSpeed, float speedFactor, float scaleFactor) {
            apply((long) (animationPos * 50.0F * speedFactor), Math.min(animationSpeed * scaleFactor, 1.0F));
        }

        void apply(long millisSinceStart, float targetScale) {
            float seconds = (float) millisSinceStart / 1000.0F;
            if (definition.looping()) seconds %= definition.lengthInSeconds();
            final float t = seconds;
            for (Entry entry : entries) {
                Keyframe[] k = entry.keyframes();
                int prev = Math.max(0, Mth.binarySearch(0, k.length, i -> t <= k[i].timestamp()) - 1);
                int next = Math.min(k.length - 1, prev + 1);
                float delta = t - k[prev].timestamp();
                float alpha = next != prev ? Mth.clamp(delta / (k[next].timestamp() - k[prev].timestamp()), 0.0F, 1.0F) : 0.0F;
                k[next].interpolation().apply(scratch, alpha, k, prev, next, targetScale);
                entry.target().apply(entry.part(), scratch);
            }
        }
    }

    /** First part named {@code name} below (or at) the given roots, depth-first like 1.19.2 {@code HierarchicalModel}. */
    static Function<String, ModelPart> lookup(ModelPart... roots) {
        return name -> {
            for (ModelPart r : roots) {
                ModelPart found = r.getAllParts().filter(p -> p.hasChild(name)).findFirst().map(p -> p.getChild(name)).orElse(null);
                if (found != null) return found;
            }
            return null;
        };
    }

    // ---- 26.1 FoxBabyAnimation ----

    static final AnimationDefinition FOX_BABY_WALK = AnimationDefinition.Builder.withLength(0.5F)
            .looping()
            .addAnimation("body", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("body", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("head", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("head", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, -1.025F, 0.0F), LINEAR)))
            .addAnimation("right_hind_leg", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(-35.0F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.25F, degreeVec(35.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.5F, degreeVec(-35.0F, 0.0F, 0.0F), CATMULLROM)))
            .addAnimation("right_hind_leg", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.05F, 0.6F, -0.02F), LINEAR),
                    new Keyframe(0.25F, posVec(0.05F, 0.6F, -0.02F), LINEAR),
                    new Keyframe(0.5F, posVec(0.05F, 0.6F, -0.02F), LINEAR)))
            .addAnimation("right_hind_leg", new AnimationChannel(SCALE,
                    new Keyframe(0.0F, scaleVec(1.0, 1.15F, 1.0), LINEAR)))
            .addAnimation("left_hind_leg", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(35.0F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.25F, degreeVec(-35.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.5F, degreeVec(35.0F, 0.0F, 0.0F), CATMULLROM)))
            .addAnimation("left_hind_leg", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(-0.05F, 0.6F, -0.02F), LINEAR),
                    new Keyframe(0.25F, posVec(-0.05F, 0.6F, -0.02F), LINEAR),
                    new Keyframe(0.5F, posVec(-0.05F, 0.6F, -0.02F), LINEAR)))
            .addAnimation("left_hind_leg", new AnimationChannel(SCALE,
                    new Keyframe(0.0F, scaleVec(1.0, 1.15F, 1.0), LINEAR)))
            .addAnimation("right_front_leg", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(35.0F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.25F, degreeVec(-35.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.5F, degreeVec(35.0F, 0.0F, 0.0F), CATMULLROM)))
            .addAnimation("right_front_leg", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.05F, 0.6F, -0.4F), LINEAR)))
            .addAnimation("right_front_leg", new AnimationChannel(SCALE,
                    new Keyframe(0.0F, scaleVec(1.0, 1.15F, 1.0), LINEAR)))
            .addAnimation("left_front_leg", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(-35.0F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.25F, degreeVec(35.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.5F, degreeVec(-35.0F, 0.0F, 0.0F), CATMULLROM)))
            .addAnimation("left_front_leg", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(-0.05F, 0.6F, -0.4F), LINEAR)))
            .addAnimation("left_front_leg", new AnimationChannel(SCALE,
                    new Keyframe(0.0F, scaleVec(1.0, 1.15F, 1.0), LINEAR)))
            .addAnimation("tail", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(-2.5F, 0.0F, 0.0F), CATMULLROM)))
            .addAnimation("tail", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, -0.05F, 0.0F), CATMULLROM)))
            .build();
 

    // ---- 26.1 BabyRabbitAnimation ----

    static final AnimationDefinition HOP = AnimationDefinition.Builder.withLength(0.75F)
            .looping()
            .addAnimation("body", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.01F), LINEAR),
                    new Keyframe(0.125F, degreeVec(0.0F, 0.0F, 0.01F), LINEAR),
                    new Keyframe(0.2083F, degreeVec(3.75F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.2917F, degreeVec(32.5F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.4167F, degreeVec(33.0F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.5833F, degreeVec(18.0F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.75F, degreeVec(0.0F, 0.0F, 0.01F), LINEAR)))
            .addAnimation("body", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.75F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("head", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.125F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2083F, degreeVec(-5.25F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.2917F, degreeVec(-32.17F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.375F, degreeVec(-34.58F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.5833F, degreeVec(-20.0F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.75F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("head", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.125F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.375F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.75F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("backlegs", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.125F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.25F, degreeVec(125.0F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.375F, degreeVec(125.5F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.4583F, degreeVec(95.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.5417F, degreeVec(42.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.6667F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.75F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("backlegs", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.6667F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.75F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("frontlegs", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(-0.17F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.125F, degreeVec(-0.17F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2083F, degreeVec(14.61F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.3333F, degreeVec(-74.37F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.5F, degreeVec(-78.19F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.5417F, degreeVec(-62.47F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.625F, degreeVec(-1.25F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.749F, degreeVec(-1.25F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.75F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("frontlegs", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.125F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2083F, posVec(0.0F, -0.16F, 0.16F), LINEAR),
                    new Keyframe(0.3333F, posVec(0.0F, 0.1F, -0.1F), CATMULLROM),
                    new Keyframe(0.75F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("right_front_leg", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.125F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.375F, degreeVec(0.0F, 0.0F, -8.45F), CATMULLROM),
                    new Keyframe(0.4583F, degreeVec(0.0F, 0.0F, -8.48F), CATMULLROM),
                    new Keyframe(0.5833F, degreeVec(0.0F, 0.0F, -2.0F), CATMULLROM),
                    new Keyframe(0.75F, degreeVec(0.0F, 0.0F, 0.0F), CATMULLROM)))
            .addAnimation("right_front_leg", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.125F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2083F, posVec(0.0F, 0.5F, -0.5F), CATMULLROM),
                    new Keyframe(0.75F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("left_front_leg", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.125F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.375F, degreeVec(0.0F, 0.0F, 10.44F), CATMULLROM),
                    new Keyframe(0.4583F, degreeVec(0.0F, 0.0F, 10.61F), CATMULLROM),
                    new Keyframe(0.5833F, degreeVec(0.0F, 0.0F, 2.0F), CATMULLROM),
                    new Keyframe(0.75F, degreeVec(0.0F, 0.0F, 0.0F), CATMULLROM)))
            .addAnimation("left_front_leg", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.125F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2083F, posVec(0.0F, 0.5F, -0.5F), CATMULLROM),
                    new Keyframe(0.75F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("left_ear", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.1667F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.375F, degreeVec(-48.5F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.5417F, degreeVec(-41.24F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.75F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("left_ear", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(-0.02F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.1667F, posVec(-0.02F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.375F, posVec(-0.025F, -0.5F, 0.0F), CATMULLROM),
                    new Keyframe(0.5417F, posVec(-0.02F, -0.38F, 0.0F), CATMULLROM),
                    new Keyframe(0.75F, posVec(-0.02F, 0.0F, 0.0F), CATMULLROM)))
            .addAnimation("right_ear", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.1667F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.375F, degreeVec(-44.95F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.75F, degreeVec(0.0F, 0.0F, 0.0F), CATMULLROM)))
            .addAnimation("right_ear", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.05F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.1667F, posVec(0.05F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.3333F, posVec(0.05F, -0.475F, 0.0F), LINEAR),
                    new Keyframe(0.5417F, posVec(0.04F, -0.385F, 0.0F), CATMULLROM),
                    new Keyframe(0.75F, posVec(0.05F, 0.0F, 0.0F), CATMULLROM)))
            .addAnimation("right_hind_leg", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, -2.5F, 0.0F), LINEAR),
                    new Keyframe(0.0833F, degreeVec(0.0F, -2.5F, 0.0F), LINEAR),
                    new Keyframe(0.25F, degreeVec(-25.0F, -22.5F, -17.5F), CATMULLROM),
                    new Keyframe(0.375F, degreeVec(-25.0F, -22.5F, -17.5F), CATMULLROM),
                    new Keyframe(0.5417F, degreeVec(0.0F, -2.5F, 0.0F), LINEAR),
                    new Keyframe(0.75F, degreeVec(0.0F, -2.5F, 0.0F), LINEAR)))
            .addAnimation("right_hind_leg", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.6667F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.75F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("left_hind_leg", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.0833F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.25F, degreeVec(-25.0F, 25.0F, 22.5F), CATMULLROM),
                    new Keyframe(0.375F, degreeVec(-25.0F, 25.0F, 22.5F), CATMULLROM),
                    new Keyframe(0.5417F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.75F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("left_hind_leg", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.6667F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.75F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("tail", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.125F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.3333F, degreeVec(15.0F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.375F, degreeVec(47.5F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.5F, degreeVec(43.33F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.75F, degreeVec(0.0F, 0.0F, 0.0F), CATMULLROM)))
            .addAnimation("tail", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.75F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .build();
 
    static final AnimationDefinition IDLE_HEAD_TILT = AnimationDefinition.Builder.withLength(4.0F)
            .addAnimation("body", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.25F, degreeVec(-62.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.3333F, degreeVec(-57.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.7083F, degreeVec(-57.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.875F, degreeVec(-57.0F, 12.0F, 2.0F), LINEAR),
                    new Keyframe(0.9583F, degreeVec(-57.0F, 5.0F, 1.0F), LINEAR),
                    new Keyframe(1.5417F, degreeVec(-57.0F, 5.0F, 1.0F), LINEAR),
                    new Keyframe(1.7083F, degreeVec(-56.5F, -16.0F, -3.0F), LINEAR),
                    new Keyframe(1.7917F, degreeVec(-57.0F, -7.5F, -1.5F), LINEAR),
                    new Keyframe(2.5417F, degreeVec(-57.0F, -7.5F, -1.5F), LINEAR),
                    new Keyframe(2.7083F, degreeVec(-57.0F, 11.0F, 2.0F), LINEAR),
                    new Keyframe(2.7917F, degreeVec(-57.0F, 5.0F, 1.0F), LINEAR),
                    new Keyframe(2.9583F, degreeVec(-57.0F, 5.0F, 1.0F), LINEAR),
                    new Keyframe(3.1667F, degreeVec(-57.0F, 5.0F, 1.0F), LINEAR),
                    new Keyframe(3.25F, degreeVec(-57.5F, 5.0F, 1.0F), LINEAR),
                    new Keyframe(3.4583F, degreeVec(-60.0F, 5.0F, 0.5F), LINEAR),
                    new Keyframe(3.5833F, degreeVec(-60.0F, 5.0F, 0.5F), LINEAR),
                    new Keyframe(3.6667F, degreeVec(-39.5F, 3.5F, 0.5F), LINEAR),
                    new Keyframe(3.8333F, degreeVec(10.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.9167F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("body", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.1667F, posVec(0.0F, 0.7F, 0.0F), LINEAR),
                    new Keyframe(0.25F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.8333F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.9167F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("body", new AnimationChannel(SCALE,
                    new Keyframe(0.0F, scaleVec(1.0, 1.0, 1.0), LINEAR),
                    new Keyframe(3.8333F, scaleVec(1.0, 1.0, 1.0), LINEAR),
                    new Keyframe(3.9167F, scaleVec(1.0, 1.0, 1.0), LINEAR)))
            .addAnimation("tail", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(55.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.0833F, degreeVec(55.0F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.2083F, degreeVec(37.5F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.25F, degreeVec(72.25F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(0.2917F, degreeVec(72.25F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.375F, degreeVec(72.25F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.7083F, degreeVec(72.25F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.8333F, degreeVec(72.25F, -3.5F, -19.5F), CATMULLROM),
                    new Keyframe(0.9167F, degreeVec(72.25F, 1.5F, 15.0F), CATMULLROM),
                    new Keyframe(1.0417F, degreeVec(72.25F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.5417F, degreeVec(72.25F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.625F, degreeVec(72.25F, 2.5F, 12.5F), CATMULLROM),
                    new Keyframe(1.7083F, degreeVec(72.25F, 4.5F, 25.5F), CATMULLROM),
                    new Keyframe(1.7917F, degreeVec(72.25F, -3.0F, -26.0F), CATMULLROM),
                    new Keyframe(1.875F, degreeVec(72.25F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.5417F, degreeVec(71.75F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.7083F, degreeVec(71.75F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.7917F, degreeVec(76.75F, 17.5F, 17.5F), LINEAR),
                    new Keyframe(2.875F, degreeVec(71.75F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.5833F, degreeVec(71.75F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.8333F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.9167F, degreeVec(32.5F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(4.0F, degreeVec(55.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("tail", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.25F, posVec(0.0F, 0.4294F, -0.6732F), LINEAR),
                    new Keyframe(0.7083F, posVec(0.0F, 0.4294F, -0.6732F), LINEAR),
                    new Keyframe(0.8333F, posVec(0.0F, 0.4294F, -0.6732F), LINEAR),
                    new Keyframe(0.9167F, posVec(0.0F, 0.4294F, -0.6732F), LINEAR),
                    new Keyframe(1.0417F, posVec(0.0F, 0.4294F, -0.6732F), LINEAR),
                    new Keyframe(1.5417F, posVec(0.0F, 0.4294F, -0.6732F), LINEAR),
                    new Keyframe(3.5833F, posVec(0.0F, 0.4294F, -0.6732F), LINEAR),
                    new Keyframe(3.8333F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.9167F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("head", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.25F, degreeVec(54.5F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.3333F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.75F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.9167F, degreeVec(40.0F, 20.5F, -11.0F), CATMULLROM),
                    new Keyframe(1.0F, degreeVec(39.5F, 11.5F, -6.0F), CATMULLROM),
                    new Keyframe(1.2917F, degreeVec(39.3F, 11.5F, -6.0F), LINEAR),
                    new Keyframe(1.75F, degreeVec(39.5F, 11.5F, -6.0F), LINEAR),
                    new Keyframe(1.9167F, degreeVec(36.0F, -28.5F, 16.5F), LINEAR),
                    new Keyframe(2.0F, degreeVec(38.0F, -18.5F, 10.0F), LINEAR),
                    new Keyframe(2.625F, degreeVec(38.0F, -18.5F, 10.0F), LINEAR),
                    new Keyframe(2.7083F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.7917F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.9583F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.1667F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.25F, degreeVec(42.5F, -0.25F, 0.0F), LINEAR),
                    new Keyframe(3.5833F, degreeVec(45.5F, -0.5F, 0.0F), LINEAR),
                    new Keyframe(3.8333F, degreeVec(-20.0F, 0.0F, 0.0F), CATMULLROM),
                    new Keyframe(3.9167F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("head", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.25F, posVec(0.0F, -0.025F, -1.975F), LINEAR),
                    new Keyframe(0.3333F, posVec(0.0F, 0.0F, -2.0F), LINEAR),
                    new Keyframe(0.5833F, posVec(0.0F, 0.0F, -2.0F), LINEAR),
                    new Keyframe(0.75F, posVec(0.0F, 0.0F, -2.0F), LINEAR),
                    new Keyframe(1.75F, posVec(0.0F, 0.0F, -2.0F), LINEAR),
                    new Keyframe(2.7083F, posVec(0.0F, 0.0F, -2.0F), LINEAR),
                    new Keyframe(3.5833F, posVec(0.0F, 0.0F, -2.0F), LINEAR),
                    new Keyframe(3.8333F, posVec(0.0F, -1.0F, 0.0F), CATMULLROM),
                    new Keyframe(3.9167F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("left_ear", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2083F, degreeVec(35.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2917F, degreeVec(19.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.3333F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.75F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.9167F, degreeVec(-15.0F, 0.0F, 7.5F), LINEAR),
                    new Keyframe(1.0F, degreeVec(-7.5F, 0.0F, -5.0F), LINEAR),
                    new Keyframe(1.0833F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.6667F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.75F, degreeVec(-3.0F, 1.5F, -17.0F), LINEAR),
                    new Keyframe(1.8333F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.9167F, degreeVec(-48.75F, 0.0F, 5.0F), LINEAR),
                    new Keyframe(2.0F, degreeVec(-65.0F, 2.5F, 0.5F), LINEAR),
                    new Keyframe(2.0833F, degreeVec(-45.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.1667F, degreeVec(-40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.5417F, degreeVec(-40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.625F, degreeVec(11.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.7083F, degreeVec(23.25F, 0.0F, 3.75F), LINEAR),
                    new Keyframe(2.75F, degreeVec(25.5F, 2.0F, 6.0F), LINEAR),
                    new Keyframe(2.8333F, degreeVec(10.0F, -0.5F, 0.75F), LINEAR),
                    new Keyframe(3.625F, degreeVec(10.0F, -0.5F, 0.75F), LINEAR),
                    new Keyframe(3.7083F, degreeVec(-25.0F, 0.25F, -1.0F), LINEAR),
                    new Keyframe(3.7917F, degreeVec(-19.0F, 0.0F, -1.0F), LINEAR),
                    new Keyframe(3.8333F, degreeVec(2.5F, 0.2F, -0.5F), LINEAR),
                    new Keyframe(3.9167F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("left_ear", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(-0.025F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2083F, posVec(-0.125F, -0.6553F, 0.4589F), LINEAR),
                    new Keyframe(0.3333F, posVec(-0.025F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.75F, posVec(-0.025F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.9167F, posVec(-0.0502F, -0.1915F, -0.0518F), LINEAR),
                    new Keyframe(1.0F, posVec(-0.2423F, -0.1025F, 0.0261F), LINEAR),
                    new Keyframe(1.0833F, posVec(-0.025F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.6667F, posVec(-0.025F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.75F, posVec(-0.125F, -0.325F, 0.0F), LINEAR),
                    new Keyframe(1.8333F, posVec(-0.025F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.0F, posVec(-0.025F, -0.75F, 0.0F), LINEAR),
                    new Keyframe(2.0833F, posVec(-0.025F, -0.75F, 0.0F), LINEAR),
                    new Keyframe(2.1667F, posVec(-0.025F, -0.75F, 0.0F), LINEAR),
                    new Keyframe(2.4167F, posVec(-0.025F, -0.75F, 0.0F), LINEAR),
                    new Keyframe(2.5417F, posVec(-0.025F, -0.75F, 0.0F), LINEAR),
                    new Keyframe(2.625F, posVec(-0.025F, -0.375F, 0.0F), LINEAR),
                    new Keyframe(2.75F, posVec(-0.025F, -0.375F, 0.25F), LINEAR),
                    new Keyframe(2.7917F, posVec(-0.025F, -0.375F, 0.0F), LINEAR),
                    new Keyframe(2.8333F, posVec(-0.025F, -0.125F, 0.0F), LINEAR),
                    new Keyframe(3.625F, posVec(-0.025F, -0.125F, 0.0F), LINEAR),
                    new Keyframe(3.9167F, posVec(-0.025F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("right_ear", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.1667F, degreeVec(35.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.3333F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.75F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.9167F, degreeVec(-22.3423F, -2.5587F, 5.4476F), LINEAR),
                    new Keyframe(1.0F, degreeVec(-11.17F, -1.28F, -21.03F), LINEAR),
                    new Keyframe(1.0833F, degreeVec(-4.1101F, 3.0703F, -18.1443F), LINEAR),
                    new Keyframe(1.125F, degreeVec(-0.0844F, 3.027F, -9.5338F), LINEAR),
                    new Keyframe(1.5417F, degreeVec(-0.0844F, 3.027F, -9.5338F), LINEAR),
                    new Keyframe(1.6667F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.75F, degreeVec(-1.5F, 1.0F, -14.5F), LINEAR),
                    new Keyframe(1.8333F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.9167F, degreeVec(-45.0F, 0.0F, 5.0F), LINEAR),
                    new Keyframe(2.0F, degreeVec(-60.0F, 1.0F, 5.0F), LINEAR),
                    new Keyframe(2.0833F, degreeVec(-50.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.5417F, degreeVec(-50.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.625F, degreeVec(16.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.7083F, degreeVec(25.5F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.7917F, degreeVec(5.25F, 0.5F, -2.0F), LINEAR),
                    new Keyframe(2.8333F, degreeVec(8.5F, 1.0F, -4.0F), LINEAR),
                    new Keyframe(3.625F, degreeVec(8.5F, 1.0F, -4.0F), LINEAR),
                    new Keyframe(3.7083F, degreeVec(-37.5F, 0.25F, -1.5F), LINEAR),
                    new Keyframe(3.7917F, degreeVec(-23.5F, 0.0F, -1.0F), LINEAR),
                    new Keyframe(3.8333F, degreeVec(10.0F, 0.25F, -0.5F), LINEAR),
                    new Keyframe(3.9167F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("right_ear", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.025F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2083F, posVec(0.075F, -0.3588F, 0.1769F), LINEAR),
                    new Keyframe(0.3333F, posVec(0.025F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.75F, posVec(0.025F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.875F, posVec(0.13F, -0.15F, 0.0F), LINEAR),
                    new Keyframe(0.9167F, posVec(0.125F, -0.2F, 0.0F), LINEAR),
                    new Keyframe(1.0F, posVec(0.0182F, -0.4258F, 0.0581F), LINEAR),
                    new Keyframe(1.0833F, posVec(0.02F, -0.34F, 0.02F), LINEAR),
                    new Keyframe(1.125F, posVec(0.025F, -0.175F, 0.0F), LINEAR),
                    new Keyframe(1.5417F, posVec(0.025F, -0.175F, 0.0F), LINEAR),
                    new Keyframe(1.6667F, posVec(0.025F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.75F, posVec(0.025F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.8333F, posVec(0.025F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.9167F, posVec(0.055F, -0.5F, 0.0F), LINEAR),
                    new Keyframe(2.0F, posVec(0.075F, -0.75F, 0.0F), LINEAR),
                    new Keyframe(2.0833F, posVec(0.025F, -0.75F, 0.0F), LINEAR),
                    new Keyframe(2.5417F, posVec(0.025F, -0.75F, 0.0F), LINEAR),
                    new Keyframe(2.5833F, posVec(0.025F, -0.5F, 0.0F), LINEAR),
                    new Keyframe(2.625F, posVec(0.025F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.7083F, posVec(0.025F, -0.25F, 0.0F), LINEAR),
                    new Keyframe(2.7917F, posVec(0.025F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.8333F, posVec(0.025F, -0.25F, 0.0F), LINEAR),
                    new Keyframe(3.625F, posVec(0.025F, -0.25F, 0.0F), LINEAR),
                    new Keyframe(3.9167F, posVec(0.025F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("right_front_leg", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.1667F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.3333F, degreeVec(60.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.4167F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.7083F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.8333F, degreeVec(39.0F, 12.5F, 6.16F), LINEAR),
                    new Keyframe(0.9167F, degreeVec(39.0F, 12.5F, -21.34F), LINEAR),
                    new Keyframe(1.0F, degreeVec(40.0F, 10.0F, -7.5F), LINEAR),
                    new Keyframe(1.0833F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.6667F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.75F, degreeVec(36.5F, 21.5F, 1.5F), LINEAR),
                    new Keyframe(1.8333F, degreeVec(38.5F, 20.0F, -0.5F), LINEAR),
                    new Keyframe(1.875F, degreeVec(41.0F, 20.0F, -0.5F), LINEAR),
                    new Keyframe(3.5F, degreeVec(41.0F, 20.0F, -0.5F), LINEAR),
                    new Keyframe(3.5833F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.75F, degreeVec(12.5F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.8333F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.9167F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("right_front_leg", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.1667F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.3333F, posVec(0.0F, 0.6F, 0.0F), LINEAR),
                    new Keyframe(0.4167F, posVec(0.0F, 0.6F, 0.0F), LINEAR),
                    new Keyframe(0.7083F, posVec(0.0F, 0.6F, 0.0F), LINEAR),
                    new Keyframe(1.0F, posVec(0.0F, 0.6F, 0.0F), LINEAR),
                    new Keyframe(1.0833F, posVec(0.0F, 0.6F, 0.0F), LINEAR),
                    new Keyframe(1.6667F, posVec(0.0F, 0.6F, 0.0F), LINEAR),
                    new Keyframe(3.5833F, posVec(0.0F, 0.6F, 0.0F), LINEAR),
                    new Keyframe(3.75F, posVec(0.0153F, 0.3728F, 0.0167F), LINEAR),
                    new Keyframe(3.8333F, posVec(0.0F, 1.0F, -0.5F), LINEAR),
                    new Keyframe(3.9167F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("left_front_leg", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2083F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.375F, degreeVec(52.5F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.4583F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.7083F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.8333F, degreeVec(40.0F, 9.0F, -18.5F), LINEAR),
                    new Keyframe(0.9167F, degreeVec(39.5F, 5.0F, -14.5F), LINEAR),
                    new Keyframe(1.0F, degreeVec(40.0F, 0.0F, -10.0F), LINEAR),
                    new Keyframe(1.0833F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.6667F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.75F, degreeVec(40.0F, -24.5F, -7.0F), LINEAR),
                    new Keyframe(1.8333F, degreeVec(39.5F, -15.5F, 0.5F), LINEAR),
                    new Keyframe(1.875F, degreeVec(42.0F, -15.5F, 0.5F), LINEAR),
                    new Keyframe(3.5F, degreeVec(42.0F, -15.5F, 0.5F), LINEAR),
                    new Keyframe(3.5833F, degreeVec(40.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.75F, degreeVec(15.5F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.8333F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(3.9167F, degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("left_front_leg", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2083F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.375F, posVec(0.0F, 0.8F, 0.0F), LINEAR),
                    new Keyframe(0.4583F, posVec(0.0F, 0.6F, 0.0F), LINEAR),
                    new Keyframe(0.7083F, posVec(0.0F, 0.6F, 0.0F), LINEAR),
                    new Keyframe(1.0F, posVec(0.0F, 0.6F, 0.0F), LINEAR),
                    new Keyframe(1.0833F, posVec(0.0F, 0.6F, 0.0F), LINEAR),
                    new Keyframe(1.6667F, posVec(0.0F, 0.6F, 0.0F), LINEAR),
                    new Keyframe(3.5833F, posVec(0.0F, 0.6F, 0.0F), LINEAR),
                    new Keyframe(3.75F, posVec(0.0F, 0.5F, 0.0F), LINEAR),
                    new Keyframe(3.8333F, posVec(0.0F, 1.0F, -0.5F), LINEAR),
                    new Keyframe(3.9167F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("frontlegs", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.3333F, posVec(0.0F, -0.4193F, -0.2723F), LINEAR),
                    new Keyframe(3.5833F, posVec(0.0F, -0.4193F, -0.2723F), LINEAR),
                    new Keyframe(4.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("right_hind_leg", new AnimationChannel(ROTATION,
                    new Keyframe(0.0F, degreeVec(0.0F, -2.5F, 0.0F), LINEAR)))
            .addAnimation("right_hind_leg", new AnimationChannel(POSITION,
                    new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .build();
 
}
