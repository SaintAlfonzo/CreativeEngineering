package com.SaintAlfonzo.baby_mobs.client.family;

import com.SaintAlfonzo.baby_mobs.client.kit.BabyKit;
import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import com.mojang.math.Vector3f;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.GoatModel;
import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.model.ModelUtils;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.model.PolarBearModel;
import net.minecraft.client.model.StriderModel;
import net.minecraft.client.model.TurtleModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 26.1 baby bee, axolotl, goat, panda, polar bear, turtle, strider, hoglin and zoglin.
 *
 * <p>26.1 models reset every part to its mesh pose before animating and then move parts relative to it; 1.19.2's adult
 * classes set several absolute adult positions (bee bob height, polar bear stand, strider body and legs, hoglin head,
 * baby-only branches keyed off {@code young} / {@code isBaby()}). So each baby here is a subclass of the 1.19.2 adult
 * class (the renderer's instanceof check) whose {@code setupAnim} resets the pose and runs the 26.1 animation. Parts the
 * 1.19.2 constructors look up but the 26.1 baby lacks are added empty.
 */
public final class MiscBabies {
    private MiscBabies() {}

    public static void register() {
        BabyRender.register(EntityType.BEE, BabyKit.<Bee>of("bee", BeeModel.class, MiscBabies::bee, BabyBeeModel::new,
                e -> BabyKit.tex(e.isAngry()
                        ? (e.hasNectar() ? "bee/bee_angry_nectar_baby.png" : "bee/bee_angry_baby.png")
                        : (e.hasNectar() ? "bee/bee_nectar_baby.png" : "bee/bee_baby.png"))));

        BabyRender.register(EntityType.AXOLOTL, BabyKit.<Axolotl>of("axolotl", AxolotlModel.class, MiscBabies::axolotl,
                BabyAxolotlModel::new, e -> BabyKit.tex("axolotl/axolotl_" + e.getVariant().getName() + "_baby.png")));

        BabyRender.register(EntityType.GOAT, BabyKit.<Goat>of("goat", GoatModel.class, MiscBabies::goat, BabyGoatModel::new,
                e -> BabyKit.tex("goat/goat_baby.png")));

        BabyRender.register(EntityType.PANDA, BabyKit.<Panda>of("panda", PandaModel.class, MiscBabies::panda, BabyPandaModel::new,
                e -> BabyKit.tex("panda/" + pandaTexture(e.getVariant()) + "_baby.png")));

        // 1.19.2 scales every polar bear by 1.2 in the renderer; 26.1 bakes that into the adult mesh only.
        BabyRender.register(EntityType.POLAR_BEAR, BabyKit.<PolarBear>of("polar_bear", PolarBearModel.class, MiscBabies::polarBear,
                        BabyPolarBearModel::new, e -> BabyKit.tex("bear/polarbear_baby.png"))
                .pose((e, pose) -> pose.scale(1.0F / 1.2F, 1.0F / 1.2F, 1.0F / 1.2F)));

        BabyRender.register(EntityType.TURTLE, BabyKit.<Turtle>of("turtle", TurtleModel.class, MiscBabies::turtle, BabyTurtleModel::new,
                e -> BabyKit.tex("turtle/turtle_baby.png")));

        // 1.19.2 halves a baby strider in the renderer; the 26.1 baby mesh is already baby-sized.
        BabyRender.register(EntityType.STRIDER, BabyKit.<Strider>of("strider", StriderModel.class, MiscBabies::strider,
                        BabyStriderModel::new,
                        e -> BabyKit.tex(e.isSuffocating() ? "strider/strider_cold_baby.png" : "strider/strider_baby.png"))
                .pose((e, pose) -> pose.scale(2.0F, 2.0F, 2.0F)));

        BabyRender.register(EntityType.HOGLIN, BabyKit.<Hoglin>of("hoglin", HoglinModel.class, MiscBabies::hoglin,
                p -> new BabyHoglinModel<Hoglin>(p), e -> BabyKit.tex("hoglin/hoglin_baby.png")));
        BabyRender.register(EntityType.ZOGLIN, BabyKit.<Zoglin>of("zoglin", HoglinModel.class, MiscBabies::hoglin,
                p -> new BabyHoglinModel<Zoglin>(p), e -> BabyKit.tex("hoglin/zoglin_baby.png")));
    }

    /** 26.1 panda baby texture names: {@code panda_baby}, {@code lazy_panda_baby}, ... */
    private static String pandaTexture(Panda.Gene gene) {
        return switch (gene) {
            case NORMAL -> "panda";
            case LAZY -> "lazy_panda";
            case WORRIED -> "worried_panda";
            case PLAYFUL -> "playful_panda";
            case BROWN -> "brown_panda";
            case WEAK -> "weak_panda";
            case AGGRESSIVE -> "aggressive_panda";
        };
    }

    private static final float DEG = (float) (Math.PI / 180.0);

    private static void resetPose(ModelPart root) {
        root.getAllParts().forEach(ModelPart::resetPose);
    }

    /** 26.1 {@code QuadrupedModel.setupAnim} (after the pose reset): head look and the leg swing. */
    private static void quadruped(ModelPart head, ModelPart rightHindLeg, ModelPart leftHindLeg, ModelPart rightFrontLeg,
                                  ModelPart leftFrontLeg, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch) {
        head.xRot = headPitch * DEG;
        head.yRot = netHeadYaw * DEG;
        rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
    }

    private static CubeListBuilder empty() {
        return CubeListBuilder.create();
    }

    // ---------------------------------------------------------------- bee

    /**
     * 26.1 {@code BabyBeeModel.createBodyLayer}. The baby's antennae are cubes on the bone; 1.19.2's {@code BeeModel}
     * still looks up body/left_antenna and body/right_antenna, so they are added empty.
     */
    static LayerDefinition bee() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bone = root.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(6, 12).addBox(1.0F, -1.6667F, -2.1633F, 1.0F, 2.0F, 2.0F)
                        .texOffs(0, 12).addBox(-2.0F, -1.6667F, -2.1933F, 1.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 19.6667F, -1.8567F));
        PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.5F, 4.0F, 4.0F, 5.0F),
                PartPose.offset(0.0F, 1.3333F, 2.3567F));
        body.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(13, 2).addBox(0.0F, -0.5F, 0.0F, 0.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.5F, 2.5F));
        body.addOrReplaceChild("left_antenna", empty(), PartPose.ZERO);
        body.addOrReplaceChild("right_antenna", empty(), PartPose.ZERO);
        bone.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(3, 9).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 0.0F, 3.0F),
                PartPose.offsetAndRotation(-1.0F, -0.6667F, 0.8567F, 0.2182F, 0.3491F, 0.0F));
        bone.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(-3, 9).mirror().addBox(0.0F, 0.0F, 0.0F, 3.0F, 0.0F, 3.0F).mirror(false),
                PartPose.offsetAndRotation(1.0F, -0.6667F, 0.8567F, 0.2182F, -0.3491F, 0.0F));
        bone.addOrReplaceChild("front_legs", CubeListBuilder.create().texOffs(13, 0).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 0.0F),
                PartPose.offset(0.0F, 3.3333F, 1.8567F));
        bone.addOrReplaceChild("middle_legs", CubeListBuilder.create().texOffs(13, 1).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 0.0F),
                PartPose.offset(0.0F, 3.3333F, 2.8567F));
        bone.addOrReplaceChild("back_legs", CubeListBuilder.create().texOffs(13, 2).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 0.0F),
                PartPose.offset(0.0F, 3.3333F, 3.8567F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** 26.1 {@code BeeModel.setupAnim}: bob relative to the baby's own bone height (1.19.2 sets the adult's 19). */
    public static class BabyBeeModel extends BeeModel<Bee> {
        private final ModelPart root;
        private final ModelPart bone;
        private final ModelPart stinger;
        private final ModelPart rightWing;
        private final ModelPart leftWing;
        private final ModelPart frontLeg;
        private final ModelPart midLeg;
        private final ModelPart backLeg;
        private float rollAmount;

        public BabyBeeModel(ModelPart root) {
            super(root);
            this.root = root;
            this.bone = root.getChild("bone");
            this.stinger = bone.getChild("body").getChild("stinger");
            this.rightWing = bone.getChild("right_wing");
            this.leftWing = bone.getChild("left_wing");
            this.frontLeg = bone.getChild("front_legs");
            this.midLeg = bone.getChild("middle_legs");
            this.backLeg = bone.getChild("back_legs");
        }

        @Override
        public void prepareMobModel(Bee bee, float limbSwing, float limbSwingAmount, float partialTicks) {
            super.prepareMobModel(bee, limbSwing, limbSwingAmount, partialTicks);
            this.rollAmount = bee.getRollAmount(partialTicks);
        }

        @Override
        public void setupAnim(Bee bee, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            resetPose(root);
            stinger.visible = !bee.hasStung();
            boolean grounded = bee.isOnGround() && bee.getDeltaMovement().lengthSqr() < 1.0E-7;
            if (!grounded) {
                float speed = ageInTicks * 120.32113F * DEG;
                rightWing.yRot = 0.0F;
                rightWing.zRot = Mth.cos(speed) * (float) Math.PI * 0.15F;
                leftWing.xRot = rightWing.xRot;
                leftWing.yRot = rightWing.yRot;
                leftWing.zRot = -rightWing.zRot;
                frontLeg.xRot = (float) (Math.PI / 4);
                midLeg.xRot = (float) (Math.PI / 4);
                backLeg.xRot = (float) (Math.PI / 4);
            }
            if (!bee.isAngry() && !grounded) {
                float bob = Mth.cos(ageInTicks * 0.18F);
                bone.xRot = 0.1F + bob * (float) Math.PI * 0.025F;
                bone.y = bone.y - Mth.cos(ageInTicks * 0.18F) * 0.9F;
                frontLeg.xRot = -bob * (float) Math.PI * 0.1F + (float) (Math.PI / 8);
                backLeg.xRot = -bob * (float) Math.PI * 0.05F + (float) (Math.PI / 4);
            }
            if (rollAmount > 0.0F) {
                bone.xRot = ModelUtils.rotlerpRad(bone.xRot, 3.0915928F, rollAmount);
            }
        }
    }

    // ---------------------------------------------------------------- axolotl

    /**
     * 26.1 {@code BabyAxolotlModel.createBodyLayer}, without its empty "root" wrapper (1.19.2's {@code AxolotlModel}
     * wants "body" directly under the model root; the 26.1 animations only key "root" to identity values).
     */
    static LayerDefinition axolotl() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -0.75F, -2.75F, 4.0F, 2.0F, 6.0F)
                        .texOffs(0, 12).addBox(0.0F, -1.75F, -2.75F, 0.0F, 3.0F, 5.0F),
                PartPose.offset(0.0F, 22.75F, 1.75F));
        body.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(20, 16).addBox(-3.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F),
                PartPose.offset(-2.0F, 0.25F, -1.25F));
        PartDefinition rightHind = body.addOrReplaceChild("right_hind_leg", empty(),
                PartPose.offsetAndRotation(-2.0F, 0.25F, 1.75F, 0.0F, 1.5708F, 1.5708F));
        rightHind.addOrReplaceChild("right_leg_r1", CubeListBuilder.create().texOffs(20, 14).addBox(0.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));
        body.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(20, 13).addBox(0.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F),
                PartPose.offset(2.0F, 0.25F, -1.25F));
        body.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(20, 14).addBox(0.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F),
                PartPose.offset(2.0F, 0.25F, 1.75F));
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(10, 9).addBox(0.0F, -1.5F, -1.0F, 0.0F, 3.0F, 8.0F),
                PartPose.offset(0.0F, -0.25F, 3.25F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 8).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, 0.25F, -2.75F));
        head.addOrReplaceChild("left_gills", CubeListBuilder.create().texOffs(20, 8).addBox(0.0F, -3.5F, 0.0F, 3.0F, 5.0F, 0.0F),
                PartPose.offset(3.0F, -0.5F, -2.0F));
        head.addOrReplaceChild("right_gills", CubeListBuilder.create().texOffs(20, 3).addBox(-3.0F, -3.5F, 0.0F, 3.0F, 5.0F, 0.0F),
                PartPose.offset(-3.0F, -0.5F, -2.0F));
        head.addOrReplaceChild("top_gills", CubeListBuilder.create().texOffs(20, 0).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 3.0F, 0.0F),
                PartPose.offset(0.0F, -2.0F, -2.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** 26.1 {@code AnimationState}: started at an entity tick, time = (ageInTicks - start) * 50 ms. */
    private static final class Anim {
        private int start = Integer.MIN_VALUE;

        boolean started() {
            return start != Integer.MIN_VALUE;
        }

        void startIfStopped(int tick) {
            if (!started()) start = tick;
        }

        void stop() {
            start = Integer.MIN_VALUE;
        }

        long millis(float ageInTicks) {
            return (long) ((ageInTicks - start) * 50.0F);
        }
    }

    /** The 26.1 baby axolotl's client-side animation states ({@code Axolotl.tickBabyAnimations}). */
    private static final class AxolotlAnims {
        final Anim swim = new Anim();
        final Anim walk = new Anim();
        final Anim walkUnderWater = new Anim();
        final Anim idleUnderWater = new Anim();
        final Anim idleUnderWaterOnGround = new Anim();
        final Anim idleOnGround = new Anim();
        final Anim playDead = new Anim();
        final Anim[] all = {swim, walk, walkUnderWater, idleUnderWater, idleUnderWaterOnGround, idleOnGround, playDead};

        void solo(Anim anim, int tick) {
            for (Anim a : all) {
                if (a == anim) a.startIfStopped(tick);
                else a.stop();
            }
        }

        void tick(Axolotl e) {
            boolean water = e.isInWater();
            boolean ground = e.isOnGround();
            boolean moving = e.animationSpeed > 1.0E-5F || e.getXRot() != e.xRotO || e.getYRot() != e.yRotO;
            if (e.isPlayingDead()) solo(playDead, e.tickCount);
            else if (moving) {
                if (water && !ground) solo(swim, e.tickCount);
                else if (!water && ground) solo(walk, e.tickCount);
                else solo(walkUnderWater, e.tickCount);
            } else if (water && !ground) solo(idleUnderWater, e.tickCount);
            else if (water) solo(idleUnderWaterOnGround, e.tickCount);
            else solo(idleOnGround, e.tickCount);
        }
    }

    private static final Vector3f ANIM_CACHE = new Vector3f();

    /** 1.19.2 {@code KeyframeAnimations.animate} for any model: parts looked up by name in {@code parts}. */
    static void animate(Map<String, ModelPart> parts, AnimationDefinition def, long millis, float scale) {
        float secs = millis / 1000.0F;
        float t = def.looping() ? secs % def.lengthInSeconds() : secs;
        for (Map.Entry<String, List<AnimationChannel>> entry : def.boneAnimations().entrySet()) {
            ModelPart part = parts.get(entry.getKey());
            if (part == null) continue;
            for (AnimationChannel channel : entry.getValue()) {
                Keyframe[] frames = channel.keyframes();
                int i = Math.max(0, Mth.binarySearch(0, frames.length, idx -> t <= frames[idx].timestamp()) - 1);
                int j = Math.min(frames.length - 1, i + 1);
                float alpha = j != i ? Mth.clamp((t - frames[i].timestamp()) / (frames[j].timestamp() - frames[i].timestamp()), 0.0F, 1.0F) : 0.0F;
                frames[j].interpolation().apply(ANIM_CACHE, alpha, frames, i, j, scale);
                channel.target().apply(part, ANIM_CACHE);
            }
        }
    }

    /**
     * 26.1 {@code BabyAxolotlModel}: keyframe animations instead of 1.19.2's procedural (and per-entity remembered)
     * axolotl poses. The animation states 26.1 keeps on the entity live here per entity, updated each frame.
     */
    public static class BabyAxolotlModel extends AxolotlModel<Axolotl> {
        private static final Map<Axolotl, AxolotlAnims> STATES = new WeakHashMap<>();
        private final ModelPart root;
        private final Map<String, ModelPart> parts = new HashMap<>();

        public BabyAxolotlModel(ModelPart root) {
            super(root);
            this.root = root;
            ModelPart body = root.getChild("body");
            ModelPart head = body.getChild("head");
            parts.put("body", body);
            parts.put("head", head);
            for (String leg : new String[]{"right_front_leg", "right_hind_leg", "left_front_leg", "left_hind_leg", "tail"}) {
                parts.put(leg, body.getChild(leg));
            }
            for (String gill : new String[]{"left_gills", "right_gills", "top_gills"}) {
                parts.put(gill, head.getChild(gill));
            }
        }

        private void apply(AnimationDefinition def, Anim anim, float ageInTicks) {
            if (anim.started()) animate(parts, def, anim.millis(ageInTicks), 1.0F);
        }

        @Override
        public void setupAnim(Axolotl e, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            resetPose(root);
            AxolotlAnims s = STATES.computeIfAbsent(e, k -> new AxolotlAnims());
            s.tick(e);
            if (s.walk.started()) {
                // KeyframeAnimation.applyWalk(pos, speed, 15, 30)
                animate(parts, MiscAxolotlAnimation.AXOLOTL_WALK_FLOOR, (long) (limbSwing * 50.0F * 15.0F),
                        Math.min(limbSwingAmount * 30.0F, 1.0F));
            }
            apply(MiscAxolotlAnimation.BABY_AXOLOTL_SWIM, s.swim, ageInTicks);
            // 26.1 drives the underwater walk with the land walk state (its walkUnderWater state is never read); kept as is
            apply(MiscAxolotlAnimation.WALK_FLOOR_UNDERWATER, s.walk, ageInTicks);
            apply(MiscAxolotlAnimation.BABY_AXOLOTL_IDLE_FLOOR, s.idleOnGround, ageInTicks);
            apply(MiscAxolotlAnimation.IDLE_UNDERWATER, s.idleUnderWater, ageInTicks);
            apply(MiscAxolotlAnimation.IDLE_FLOOR_UNDERWATER, s.idleUnderWaterOnGround, ageInTicks);
            apply(MiscAxolotlAnimation.BABY_AXOLOTL_PLAY_DEAD, s.playDead, ageInTicks);
        }
    }

    // ---------------------------------------------------------------- goat

    /** 26.1 {@code BabyGoatModel.createBodyLayer}. */
    static LayerDefinition goat() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(29, 12).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(1.5F, 19.5F, 3.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(21, 12).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(-1.5F, 19.5F, 3.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(21, 5).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(-1.5F, 19.5F, -2.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(29, 5).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(1.5F, 19.5F, -2.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 10).addBox(-3.0F, -2.3F, -4.5F, 6.0F, 5.0F, 9.0F)
                        .texOffs(0, 24).addBox(-2.5F, -2.2F, -4.0F, 5.0F, 4.0F, 8.0F),
                PartPose.offset(0.0F, 17.8F, 0.0F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -3.8126F, -5.1548F, 4.0F, 4.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 15.5F, -3.0F, 0.4363F, 0.0F, 0.0F));
        head.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(24, 0).mirror().addBox(0.0F, -4.5F, 0.0F, 1.0F, 2.0F, 1.0F).mirror(false),
                PartPose.offsetAndRotation(-1.5F, -1.5F, -1.0F, (float) (-Math.PI / 8), 0.0F, 0.0F));
        head.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(24, 0).mirror().addBox(2.0F, -4.5F, 0.0F, 1.0F, 2.0F, 1.0F).mirror(false),
                PartPose.offsetAndRotation(-1.5F, -1.5F, -1.0F, (float) (-Math.PI / 8), 0.0F, 0.0F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F).mirror(false),
                PartPose.offsetAndRotation(-1.7F, -2.3126F, 0.1452F, 0.0F, -0.5236F, 0.0F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(0, 12).addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(1.7F, -2.3126F, 0.1452F, 0.0F, 0.5236F, 0.0F));
        head.addOrReplaceChild("HeadMain", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.5F, -4.0F, 4.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, -1.3126F, -1.1548F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** 26.1 {@code BabyGoatModel.setupAnim}: the head stays tipped down (no look pitch) unless ramming. */
    public static class BabyGoatModel extends GoatModel<Goat> {
        public BabyGoatModel(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(Goat goat, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            super.setupAnim(goat, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            if (goat.getRammingXHeadRot() == 0.0F) head.xRot = (float) (Math.PI / 8);
        }
    }

    // ---------------------------------------------------------------- panda

    /** 26.1 {@code BabyPandaModel.createBodyLayer}. */
    static LayerDefinition panda() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 11).addBox(-4.5F, -3.5F, -5.5F, 9.0F, 7.0F, 11.0F),
                PartPose.offset(0.0F, 18.5F, 2.5F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -3.0F, -5.0F, 7.0F, 6.0F, 5.0F)
                        .texOffs(24, 6).addBox(-2.0F, 1.0F, -6.0F, 4.0F, 2.0F, 1.0F)
                        .texOffs(24, 0).addBox(-4.5F, -4.0F, -3.5F, 3.0F, 3.0F, 1.0F)
                        .texOffs(33, 0).addBox(1.5F, -4.0F, -3.5F, 3.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 19.0F, -3.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F),
                PartPose.offset(-3.0F, 22.0F, 6.5F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F),
                PartPose.offset(3.0F, 22.0F, 6.5F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 29).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F),
                PartPose.offset(-3.0F, 22.0F, -1.5F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(12, 29).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F),
                PartPose.offset(3.0F, 22.0F, -1.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** 26.1 {@code PandaModel.setupAnim} with the baby's own sitting pose ({@code BabyPandaModel.animateSitting}). */
    public static class BabyPandaModel extends PandaModel<Panda> {
        private final ModelPart root;
        private float partialTicks;

        public BabyPandaModel(ModelPart root) {
            super(root);
            this.root = root;
        }

        @Override
        public void prepareMobModel(Panda panda, float limbSwing, float limbSwingAmount, float partialTicks) {
            super.prepareMobModel(panda, limbSwing, limbSwingAmount, partialTicks);
            this.partialTicks = partialTicks;
        }

        @Override
        public void setupAnim(Panda panda, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            resetPose(root);
            quadruped(head, rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, limbSwing, limbSwingAmount, netHeadYaw, headPitch);
            float sit = panda.getSitAmount(partialTicks);
            float lieOnBack = panda.getLieOnBackAmount(partialTicks);
            if (panda.getUnhappyCounter() > 0) {
                head.yRot = 0.35F * Mth.sin(0.6F * ageInTicks);
                head.zRot = 0.35F * Mth.sin(0.6F * ageInTicks);
                rightFrontLeg.xRot = -0.75F * Mth.sin(0.3F * ageInTicks);
                leftFrontLeg.xRot = 0.75F * Mth.sin(0.3F * ageInTicks);
            } else {
                head.zRot = 0.0F;
            }
            if (panda.isSneezing()) {
                int sneeze = panda.getSneezeCounter();
                if (sneeze < 15) {
                    head.xRot = (float) (-Math.PI / 4) * (float) sneeze / 14.0F;
                } else if (sneeze < 20) {
                    float pos = (float) ((sneeze - 15) / 5);
                    head.xRot = (float) (-Math.PI / 4) + (float) (Math.PI / 4) * pos;
                }
            }
            if (sit > 0.0F) {
                body.xRot = ModelUtils.rotlerpRad(body.xRot, (float) (Math.PI / 18), sit);
                body.z = Mth.lerp(sit, body.z, -1.5F);
                head.z = Mth.lerp(sit, head.z, -11.5F);
                head.y = Mth.lerp(sit, head.y, 17.5F);
                rightFrontLeg.z = Mth.lerp(sit, rightFrontLeg.z, -5.0F);
                leftFrontLeg.z = Mth.lerp(sit, leftFrontLeg.z, -5.0F);
                rightHindLeg.z = Mth.lerp(sit, rightHindLeg.z, 3.0F);
                leftHindLeg.z = Mth.lerp(sit, leftHindLeg.z, 3.0F);
                rightFrontLeg.zRot = -0.27079642F;
                leftFrontLeg.zRot = 0.27079642F;
                rightHindLeg.zRot = 0.5707964F;
                leftHindLeg.zRot = -0.5707964F;
                if (panda.isEating()) {
                    head.xRot = (float) (Math.PI / 2) + 0.2F * Mth.sin(ageInTicks * 0.6F);
                    rightFrontLeg.xRot = -0.4F - 0.2F * Mth.sin(ageInTicks * 0.6F);
                    leftFrontLeg.xRot = -0.4F - 0.2F * Mth.sin(ageInTicks * 0.6F);
                }
                if (panda.isScared()) {
                    head.xRot = 2.1707964F;
                    rightFrontLeg.xRot = -0.9F;
                    leftFrontLeg.xRot = -0.9F;
                }
            } else {
                rightHindLeg.zRot = 0.0F;
                leftHindLeg.zRot = 0.0F;
                rightFrontLeg.zRot = 0.0F;
                leftFrontLeg.zRot = 0.0F;
            }
            if (lieOnBack > 0.0F) {
                rightHindLeg.xRot = -0.6F * Mth.sin(ageInTicks * 0.15F);
                leftHindLeg.xRot = 0.6F * Mth.sin(ageInTicks * 0.15F);
                rightFrontLeg.xRot = 0.3F * Mth.sin(ageInTicks * 0.25F);
                leftFrontLeg.xRot = -0.3F * Mth.sin(ageInTicks * 0.25F);
                head.xRot = ModelUtils.rotlerpRad(head.xRot, (float) (Math.PI / 2), lieOnBack);
            }
            // roll: 26.1 (and 1.19.2) give babies a roll amount of 0
        }
    }

    // ---------------------------------------------------------------- polar bear

    /** 26.1 {@code BabyPolarBearModel.createBodyLayer} (no 1.2 mesh scaling, unlike the 26.1 adult). */
    static LayerDefinition polarBear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 9).addBox(-4.0F, -3.5F, -6.0F, 8.0F, 7.0F, 12.0F),
                PartPose.offset(0.0F, 17.5F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -2.625F, -4.25F, 6.0F, 5.0F, 4.0F)
                        .texOffs(20, 3).addBox(-2.0F, 0.375F, -6.25F, 4.0F, 2.0F, 2.0F)
                        .texOffs(20, 0).addBox(-4.0F, -3.625F, -2.75F, 2.0F, 2.0F, 1.0F)
                        .texOffs(26, 0).addBox(2.0F, -3.625F, -2.75F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 18.625F, -5.75F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(-2.5F, 21.5F, 4.5F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(2.5F, 21.5F, 4.5F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 28).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(-2.5F, 21.5F, -4.5F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(12, 28).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(2.5F, 21.5F, -4.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** 26.1 {@code PolarBearModel.setupAnim}: standing moves parts relative to the baby pose (ageScale 0.5). */
    public static class BabyPolarBearModel extends PolarBearModel<PolarBear> {
        private static final float AGE_SCALE = 0.5F;
        private final ModelPart root;

        public BabyPolarBearModel(ModelPart root) {
            super(root);
            this.root = root;
        }

        @Override
        public void setupAnim(PolarBear bear, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            resetPose(root);
            quadruped(head, rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, limbSwing, limbSwingAmount, netHeadYaw, headPitch);
            float stand = bear.getStandingAnimationScale(ageInTicks - (float) bear.tickCount);
            stand *= stand;
            body.xRot -= stand * (float) Math.PI * 0.35F;
            body.y += stand * AGE_SCALE * 2.0F;
            rightFrontLeg.y -= stand * AGE_SCALE * 20.0F;
            rightFrontLeg.z += stand * AGE_SCALE * 4.0F;
            rightFrontLeg.xRot -= stand * (float) Math.PI * 0.45F;
            leftFrontLeg.y = rightFrontLeg.y;
            leftFrontLeg.z = rightFrontLeg.z;
            leftFrontLeg.xRot -= stand * (float) Math.PI * 0.45F;
            head.y -= stand * 24.0F;
            head.z += stand * 13.0F;
            head.xRot += stand * (float) Math.PI * 0.15F;
        }
    }

    // ---------------------------------------------------------------- turtle

    /** 26.1 {@code BabyTurtleModel.createBodyLayer}; 1.19.2's {@code TurtleModel} also wants an egg_belly (empty). */
    static LayerDefinition turtle() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, 22.9F, 1.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, -2.0F, -3.0F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 22.9F, -1.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(-1, 0).addBox(-2.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F),
                PartPose.offset(-2.0F, 23.9F, 2.5F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(-1, 1).addBox(0.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F),
                PartPose.offset(2.0F, 23.9F, 2.5F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(8, 6).addBox(-2.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F),
                PartPose.offset(-2.0F, 23.9F, -0.5F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(8, 7).addBox(0.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F),
                PartPose.offset(2.0F, 23.9F, -0.5F));
        root.addOrReplaceChild("egg_belly", empty(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 16);
    }

    /** 26.1 {@code TurtleModel.setupAnim} on top of the quadruped walk (1.19.2 zeroes some leg angles 26.1 keeps). */
    public static class BabyTurtleModel extends TurtleModel<Turtle> {
        private final ModelPart root;
        private final ModelPart eggBelly;

        public BabyTurtleModel(ModelPart root) {
            super(root);
            this.root = root;
            this.eggBelly = root.getChild("egg_belly");
        }

        @Override
        public void setupAnim(Turtle turtle, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            resetPose(root);
            quadruped(head, rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, limbSwing, limbSwingAmount, netHeadYaw, headPitch);
            if (!turtle.isInWater() && turtle.isOnGround()) {
                float layEgg = turtle.isLayingEgg() ? 4.0F : 1.0F;
                float layEggAmplitude = turtle.isLayingEgg() ? 2.0F : 1.0F;
                float swingPos = limbSwing * 5.0F;
                float frontSwing = Mth.cos(layEgg * swingPos);
                float hindSwing = Mth.cos(swingPos);
                rightFrontLeg.yRot = -frontSwing * 8.0F * limbSwingAmount * layEggAmplitude;
                leftFrontLeg.yRot = frontSwing * 8.0F * limbSwingAmount * layEggAmplitude;
                rightHindLeg.yRot = -hindSwing * 3.0F * limbSwingAmount;
                leftHindLeg.yRot = hindSwing * 3.0F * limbSwingAmount;
            } else {
                float swing = Mth.cos(limbSwing * 0.6662F * 0.6F) * 0.5F * limbSwingAmount;
                rightHindLeg.xRot = swing;
                leftHindLeg.xRot = -swing;
                rightFrontLeg.zRot = -swing;
                leftFrontLeg.zRot = swing;
            }
            // babies never carry an egg; also keeps 1.19.2's renderToBuffer egg offset off
            eggBelly.visible = false;
        }
    }

    // ---------------------------------------------------------------- strider

    /**
     * 26.1 {@code BabyStriderModel.createBodyLayer}: three crosswise bristles (bristle0..2). 1.19.2's {@code StriderModel}
     * looks up six side bristles by name, added empty.
     */
    static LayerDefinition strider() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.75F, -4.0F, 7.0F, 7.0F, 8.0F),
                PartPose.offset(0.0F, 16.75F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(-1.5F, 20.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(8, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(1.5F, 20.0F, 0.0F));
        body.addOrReplaceChild("bristle0", CubeListBuilder.create().texOffs(0, 21).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F),
                PartPose.offset(0.0F, -4.25F, 2.0F));
        body.addOrReplaceChild("bristle1", CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F),
                PartPose.offset(0.0F, -4.25F, 0.0F));
        body.addOrReplaceChild("bristle2", CubeListBuilder.create().texOffs(0, 15).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F),
                PartPose.offset(0.0F, -4.25F, -2.0F));
        for (String bristle : new String[]{"right_bottom_bristle", "right_middle_bristle", "right_top_bristle",
                "left_top_bristle", "left_middle_bristle", "left_bottom_bristle"}) {
            body.addOrReplaceChild(bristle, empty(), PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** 26.1 {@code StriderModel.setupAnim} + {@code BabyStriderModel.customAnimations}. */
    public static class BabyStriderModel extends StriderModel<Strider> {
        private final ModelPart body;
        private final ModelPart rightLeg;
        private final ModelPart leftLeg;
        private final ModelPart frontBristle;
        private final ModelPart middleBristle;
        private final ModelPart bottomBristle;

        public BabyStriderModel(ModelPart root) {
            super(root);
            this.body = root.getChild("body");
            this.rightLeg = root.getChild("right_leg");
            this.leftLeg = root.getChild("left_leg");
            this.frontBristle = body.getChild("bristle2");
            this.middleBristle = body.getChild("bristle1");
            this.bottomBristle = body.getChild("bristle0");
        }

        @Override
        public void setupAnim(Strider strider, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            resetPose(root());
            float pos = limbSwing;
            float speed = Math.min(limbSwingAmount, 0.25F);
            if (!strider.isVehicle()) {
                body.xRot = headPitch * DEG;
                body.yRot = netHeadYaw * DEG;
            } else {
                body.xRot = 0.0F;
                body.yRot = 0.0F;
            }
            body.zRot = 0.1F * Mth.sin(pos * 1.5F) * 4.0F * speed;
            leftLeg.xRot = Mth.sin(pos * 1.5F * 0.5F) * 2.0F * speed;
            rightLeg.xRot = Mth.sin(pos * 1.5F * 0.5F + (float) Math.PI) * 2.0F * speed;
            leftLeg.zRot = (float) (Math.PI / 18) * Mth.cos(pos * 1.5F * 0.5F) * speed;
            rightLeg.zRot = (float) (Math.PI / 18) * Mth.cos(pos * 1.5F * 0.5F + (float) Math.PI) * speed;
            // BabyStriderModel.customAnimations (26.1 bobs around 17.25, half a pixel under the mesh's 16.75)
            body.y = 17.25F - 1.0F * Mth.cos(pos * 1.5F) * 2.0F * speed;
            leftLeg.y = 20.0F + 2.0F * Mth.sin(pos * 1.5F * 0.5F + (float) Math.PI) * 2.0F * speed;
            rightLeg.y = 20.0F + 2.0F * Mth.sin(pos * 1.5F * 0.5F) * 2.0F * speed;
            float flow = Mth.cos(pos * 1.5F + (float) Math.PI) * speed;
            frontBristle.xRot += flow * 0.6F;
            middleBristle.xRot += flow * 1.2F;
            bottomBristle.xRot += flow * 1.3F;
            frontBristle.xRot += 0.1F * Mth.sin(ageInTicks * 0.4F);
            middleBristle.xRot += 0.1F * Mth.sin(ageInTicks * 0.2F);
            bottomBristle.xRot += 0.05F * Mth.sin(ageInTicks * -0.4F);
        }
    }

    // ---------------------------------------------------------------- hoglin / zoglin

    /** 26.1 {@code BabyHoglinModel.createBodyLayer} (hoglin and zoglin); 1.19.2's {@code HoglinModel} wants body/mane (empty). */
    static LayerDefinition hoglin() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, -2.2605F, -10.547F, 10.0F, 4.0F, 12.0F)
                        .texOffs(44, 29).addBox(-7.0F, -4.0981F, -8.4879F, 2.0F, 5.0F, 2.0F)
                        .texOffs(52, 29).addBox(5.0F, -4.0981F, -8.4879F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 13.0F, -7.0F, 0.8727F, 0.0F, 0.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-4.0F, -14.0F, -7.0F, 8.0F, 8.0F, 14.0F, new CubeDeformation(0.02F))
                        .texOffs(24, 39).addBox(0.0F, -18.0F, -8.0F, 0.0F, 6.0F, 11.0F, new CubeDeformation(0.02F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        body.addOrReplaceChild("mane", empty(), PartPose.ZERO);
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(32, 5).addBox(-5.1F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(-5.0F, -1.0F, -1.5F, 0.0F, 0.0F, -0.8727F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-0.9F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F).mirror(false),
                PartPose.offsetAndRotation(5.0F, -1.0F, -1.5F, 0.0F, 0.0F, 0.8727F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 47).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(-2.5F, 18.0F, 4.5F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(12, 47).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(2.5F, 18.0F, 4.5F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(-2.5F, 18.0F, -4.5F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(12, 38).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(2.5F, 18.0F, -4.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /**
     * 26.1 {@code HoglinModel.setupAnim} with {@code BabyHoglinModel.animateHeadbutt} (head drops 2.5 px into the
     * headbutt); 1.19.2's baby branch sets an absolute adult head height and mane offset instead.
     */
    public static class BabyHoglinModel<T extends Mob & HoglinBase> extends HoglinModel<T> {
        private final ModelPart root;
        private final ModelPart head;
        private final ModelPart rightEar;
        private final ModelPart leftEar;
        private final ModelPart rightFrontLeg;
        private final ModelPart leftFrontLeg;
        private final ModelPart rightHindLeg;
        private final ModelPart leftHindLeg;

        public BabyHoglinModel(ModelPart root) {
            super(root);
            this.root = root;
            this.head = root.getChild("head");
            this.rightEar = head.getChild("right_ear");
            this.leftEar = head.getChild("left_ear");
            this.rightFrontLeg = root.getChild("right_front_leg");
            this.leftFrontLeg = root.getChild("left_front_leg");
            this.rightHindLeg = root.getChild("right_hind_leg");
            this.leftHindLeg = root.getChild("left_hind_leg");
        }

        @Override
        public void setupAnim(T mob, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            resetPose(root);
            rightEar.zRot = (float) (-Math.PI * 2.0 / 9.0) - limbSwingAmount * Mth.sin(limbSwing);
            leftEar.zRot = (float) (Math.PI * 2.0 / 9.0) + limbSwingAmount * Mth.sin(limbSwing);
            head.yRot = netHeadYaw * DEG;
            float headbutt = 1.0F - (float) Mth.abs(10 - 2 * mob.getAttackAnimationRemainingTicks()) / 10.0F;
            head.xRot = Mth.lerp(headbutt, 0.87266463F, (float) (-Math.PI / 9));
            head.y += headbutt * 2.5F;
            rightFrontLeg.xRot = Mth.cos(limbSwing) * 1.2F * limbSwingAmount;
            leftFrontLeg.xRot = Mth.cos(limbSwing + (float) Math.PI) * 1.2F * limbSwingAmount;
            rightHindLeg.xRot = leftFrontLeg.xRot;
            leftHindLeg.xRot = rightFrontLeg.xRot;
        }
    }
}
