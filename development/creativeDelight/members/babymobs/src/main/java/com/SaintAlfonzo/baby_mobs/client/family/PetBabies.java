package com.SaintAlfonzo.baby_mobs.client.family;

import com.SaintAlfonzo.baby_mobs.client.kit.BabyKit;
import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Wolf;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 26.1 baby cat (and its collar), ocelot, wolf (and its collar), fox and rabbit. 1.19.2 has one wolf: it gets 26.1's
 * pale baby. Every baby here has its own 26.1 animation, so each is a subclass of the 1.19.2 adult model that keeps the
 * part names the 1.19.2 constructor needs and replaces {@code prepareMobModel} / {@code setupAnim} with the 26.1 baby
 * animation (26.1 resets every part to its rest pose before animating; the subclasses do the same).
 * The fox's held item offset is fixed in {@code PetFoxHeldItemMixin}.
 */
public final class PetBabies {
    private PetBabies() {}

    private static final float DEG = (float) (Math.PI / 180.0);
    /** 26.1 {@code ageScale} of a baby. */
    private static final float AGE_SCALE = 0.5F;

    /** 1.19.2 cat variants (the adult texture file name) that have a 26.1 baby texture. */
    private static final Set<String> CAT_VARIANTS = Set.of("tabby", "black", "red", "siamese", "british_shorthair", "calico",
            "persian", "ragdoll", "white", "jellie", "all_black");

    public static void register() {
        BabyKit<Cat> cat = BabyKit.<Cat>of("cat", CatModel.class, PetBabies::feline, r -> new BabyCatModel(r, 1.0F), PetBabies::catTexture)
                // 26.1 CAT_BABY_COLLAR: the baby body scaled 1.01 at the root (BabyCatModel.COLLAR_TRANSFORMER)
                .layerModel(new ResourceLocation("textures/entity/cat/cat_collar.png"), BabyKit.tex("cat/cat_collar_baby.png"),
                        "collar", PetBabies::feline, r -> new BabyCatModel(r, 1.01F))
                // 1.19.2 CatRenderer.scale shrinks every cat to 0.8; 26.1 builds that into the adult mesh only
                .pose((e, pose) -> pose.scale(1.25F, 1.25F, 1.25F))
                // More Mob Variants cat coats: the cat's own vanilla coat baby, also with variantBabies = OLD_MODELS
                // (owner, 2026-11-01); Cozy's end / nether coats stay variants (old baby unless NEW_MODELS)
                .poolNamespace("moremobvariants");
        BabyRender.register(EntityType.CAT, cat);
        // Cozy's Improved Cats: its own cat type (extends Cat, renderer extends CatRenderer, vanilla CatModel). Its end and
        // nether coats have no baby texture; catTexture uses the baby of the cat's own vanilla coat (owner, 2026-11-01).
        BabyRender.register(new ResourceLocation("cozys-improved-cats", "improved_cat"), cat);

        BabyRender.register(EntityType.OCELOT, BabyKit.<Ocelot>of("ocelot", OcelotModel.class, PetBabies::feline, BabyOcelotModel::new,
                e -> BabyKit.tex("cat/ocelot_baby.png")));

        BabyRender.register(EntityType.WOLF, BabyKit.<Wolf>of("wolf", WolfModel.class, PetBabies::wolf, BabyWolfModel::new,
                        e -> BabyKit.tex(e.isTame() ? "wolf/wolf_tame_baby.png" : e.isAngry() ? "wolf/wolf_angry_baby.png" : "wolf/wolf_baby.png"))
                .textureFrom(PetBabies::wolfTexture)
                .layerTexture(new ResourceLocation("textures/entity/wolf/wolf_collar.png"), BabyKit.tex("wolf/wolf_collar_baby.png"))
                // More Mob Variants ships its own collar texture
                .layerPrefixAs("textures/entity/wolf/wolf_collar", new ResourceLocation("textures/entity/wolf/wolf_collar.png")));

        BabyRender.register(EntityType.FOX, BabyKit.<Fox>of("fox", FoxModel.class, PetBabies::fox, BabyFoxModel::new,
                e -> BabyKit.tex("fox/" + (e.getFoxType() == Fox.Type.SNOW ? "fox_snow" : "fox") + (e.isSleeping() ? "_sleep" : "") + "_baby.png")));

        BabyRender.register(EntityType.RABBIT, BabyKit.<Rabbit>of("rabbit", RabbitModel.class, PetBabies::rabbit, BabyRabbitModel::new,
                PetBabies::rabbitTexture));
    }

    /** 26.1 wolf variants other than pale; More Mob Variants uses the same names. */
    private static final Set<String> WOLF_VARIANTS = Set.of("ashen", "black", "chestnut", "rusty", "snowy", "spotted", "striped", "woods");
    private static final java.util.regex.Pattern MMV_WOLF = java.util.regex.Pattern.compile("textures/entity/wolf/([a-z_]+)_(wild|tame|angry)\\.png");

    /**
     * Pale baby by state; a More Mob Variants coat with a 26.1 baby (ashen, black, ...) gets that variant's baby
     * (reached with variantBabies = NEW_MODELS; OLD_MODELS keeps wolves on the 1.19.2 baby). Other coats: pale.
     */
    private static ResourceLocation wolfTexture(Wolf wolf, ResourceLocation adult) {
        String suffix = wolf.isTame() ? "_tame" : wolf.isAngry() ? "_angry" : "";
        if (adult != null && "moremobvariants".equals(adult.getNamespace())) {
            java.util.regex.Matcher m = MMV_WOLF.matcher(adult.getPath());
            if (m.matches() && WOLF_VARIANTS.contains(m.group(1))) return BabyKit.tex("wolf/wolf_" + m.group(1) + suffix + "_baby.png");
        }
        return BabyKit.tex("wolf/wolf" + suffix + "_baby.png");
    }

    private static ResourceLocation catTexture(Cat cat) {
        String path = cat.getResourceLocation().getPath();
        String name = path.substring(path.lastIndexOf('/') + 1).replace(".png", "");
        // a variant some mod adds has no baby texture; the adult one would not fit the baby UVs
        return BabyKit.tex("cat/cat_" + (CAT_VARIANTS.contains(name) ? name : "tabby") + "_baby.png");
    }

    private static ResourceLocation rabbitTexture(Rabbit rabbit) {
        if ("Toast".equals(ChatFormatting.stripFormatting(rabbit.getName().getString()))) return BabyKit.tex("rabbit/rabbit_toast_baby.png");
        String name = switch (rabbit.getRabbitType()) {
            case 1 -> "white";
            case 2 -> "black";
            case 3 -> "white_splotched";
            case 4 -> "gold";
            case 5 -> "salt";
            case 99 -> "caerbannog";
            default -> "brown";
        };
        return BabyKit.tex("rabbit/rabbit_" + name + "_baby.png");
    }

    /** 26.1 resets the whole model to its rest pose before every {@code setupAnim}. */
    private static void reset(ModelPart root) {
        root.getAllParts().forEach(p -> {
            p.resetPose();
            p.xScale = 1.0F;
            p.yScale = 1.0F;
            p.zScale = 1.0F;
        });
    }

    // ---------------------------------------------------------------- cat, ocelot

    /** 26.1 {@code BabyFelineModel.createBabyLayer} (cat, ocelot and, scaled, the cat collar). */
    static LayerDefinition feline() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5F, -3.0F, -2.875F, 5.0F, 4.0F, 4.0F)
                        .texOffs(18, 0).addBox(-2.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F)
                        .texOffs(24, 0).addBox(1.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F)
                        .texOffs(18, 3).addBox(-1.5F, -1.0F, -3.875F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 20.0F, -3.125F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(18, 18).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F),
                PartPose.offset(1.0F, 22.0F, -1.5F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(12, 18).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F),
                PartPose.offset(-1.0F, 22.0F, -1.5F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(18, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F),
                PartPose.offset(1.0F, 22.0F, 2.5F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -1.5F, -3.5F, 4.0F, 3.0F, 7.0F),
                PartPose.offset(0.0F, 20.5F, 0.5F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(12, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F),
                PartPose.offset(-1.0F, 22.0F, 2.5F));
        root.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 18).addBox(-0.5F, -0.107F, 0.0849F, 1.0F, 1.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 19.107F, 3.9151F, -0.567232F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail2", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** The 26.1 baby feline parts and inputs, shared by the cat and ocelot subclasses. */
    private static final class Feline {
        final ModelPart root, head, body, tail1, tail2, leftHindLeg, rightHindLeg, leftFrontLeg, rightFrontLeg;
        boolean crouching, sprinting, sitting;
        float lieDown, lieDownTail, relaxOne;

        Feline(ModelPart root) {
            this.root = root;
            head = root.getChild("head");
            body = root.getChild("body");
            tail1 = root.getChild("tail1");
            tail2 = root.getChild("tail2");
            leftHindLeg = root.getChild("left_hind_leg");
            rightHindLeg = root.getChild("right_hind_leg");
            leftFrontLeg = root.getChild("left_front_leg");
            rightFrontLeg = root.getChild("right_front_leg");
        }

        /** 26.1 {@code BabyFelineModel.setupAnim}. */
        void setupAnim(float pos, float speed, float yaw, float pitch) {
            reset(root);
            if (crouching) {
                body.y += 1.0F * AGE_SCALE;
                head.y += 2.0F * AGE_SCALE;
                tail1.y += 1.0F * AGE_SCALE;
                tail2.y += -4.0F * AGE_SCALE;
                tail2.z += 2.0F * AGE_SCALE;
                tail1.xRot = (float) (Math.PI / 2);
                tail2.xRot = (float) (Math.PI / 2);
            } else if (sprinting) {
                tail2.y = tail1.y;
                tail2.z += 2.0F * AGE_SCALE;
                tail1.xRot = (float) (Math.PI / 2);
                tail2.xRot = (float) (Math.PI / 2);
            }

            head.xRot = pitch * DEG;
            head.yRot = yaw * DEG;
            if (!sitting) {
                if (sprinting) {
                    leftHindLeg.xRot = Mth.cos(pos * 0.6662F) * speed;
                    rightHindLeg.xRot = Mth.cos(pos * 0.6662F + 0.3F) * speed;
                    leftFrontLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI + 0.3F) * speed;
                    rightFrontLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * speed;
                    tail2.xRot = 1.7278761F + (float) (Math.PI / 10) * Mth.cos(pos) * speed;
                } else {
                    leftHindLeg.xRot = Mth.cos(pos * 0.6662F) * speed;
                    rightHindLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * speed;
                    leftFrontLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * speed;
                    rightFrontLeg.xRot = Mth.cos(pos * 0.6662F) * speed;
                    tail2.xRot = 1.7278761F + (crouching ? 0.47123894F : (float) (Math.PI / 4)) * Mth.cos(pos) * speed;
                }
            } else {
                body.xRot += -0.43633232F;
                body.y++;
                head.z += 0.75F;
                tail1.xRot += 0.5454154F;
                tail1.y += 4.0F;
                tail1.z -= 0.9F;
                leftHindLeg.z -= 0.9F;
                rightHindLeg.z -= 0.9F;
            }

            if (lieDown > 0.0F) {
                body.x++;
                head.xRot = Mth.rotLerp(lieDown, head.xRot, (float) (Math.PI / 18));
                head.zRot = Mth.rotLerp(lieDown, head.zRot, (float) (-Math.PI * 5.0 / 12.0));
                head.x++;
                head.y += 0.75F;
                head.z -= 0.5F;
                rightFrontLeg.xRot = (float) (-Math.PI / 4);
                rightFrontLeg.x += 3.5F;
                rightFrontLeg.y -= 0.5F;
                leftFrontLeg.xRot = (float) (-Math.PI / 2);
                leftFrontLeg.x++;
                leftFrontLeg.y--;
                leftFrontLeg.z -= 2.0F;
                rightHindLeg.xRot = (float) (Math.PI * 2.0 / 9.0);
                rightHindLeg.yRot = (float) (Math.PI / 9);
                rightHindLeg.zRot = (float) (-Math.PI / 9);
                rightHindLeg.x += 2.5F;
                rightHindLeg.y -= 0.25F;
                rightHindLeg.z += 0.5F;
                leftHindLeg.x++;
                leftHindLeg.z--;
                tail1.xRot = tail1.xRot + Mth.rotLerp(lieDownTail, tail1.xRot, (float) (-Math.PI / 6));
                tail1.yRot = tail1.yRot + Mth.rotLerp(lieDownTail, tail1.yRot, 0.0F);
                tail1.zRot = tail1.zRot + Mth.rotLerp(lieDownTail, tail1.zRot, (float) (-Math.PI / 18));
                tail1.x++;
                tail1.y += 0.5F;
                tail1.z -= 0.25F;
            }

            if (relaxOne > 0.0F) {
                head.xRot = Mth.rotLerp(relaxOne, head.xRot, -0.58177644F);
            }
        }
    }

    /**
     * 26.1 {@code BabyCatModel}. {@code rootScale} 1.01 is the collar layer (26.1 {@code MeshTransformer.scaling(1.01)}:
     * root scaled by 1.01 and moved down 24.016 * (1 - 1.01) pixels).
     */
    public static class BabyCatModel extends CatModel<Cat> {
        private final Feline f;
        private final float rootScale;

        public BabyCatModel(ModelPart root, float rootScale) {
            super(root);
            this.f = new Feline(root);
            this.rootScale = rootScale;
        }

        @Override
        public void prepareMobModel(Cat cat, float limbSwing, float limbSwingAmount, float partialTicks) {
            f.crouching = cat.isCrouching();
            f.sprinting = cat.isSprinting();
            f.sitting = cat.isInSittingPose();
            f.lieDown = cat.getLieDownAmount(partialTicks);
            f.lieDownTail = cat.getLieDownAmountTail(partialTicks);
            f.relaxOne = cat.getRelaxStateOneAmount(partialTicks);
        }

        @Override
        public void setupAnim(Cat cat, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            f.setupAnim(limbSwing, limbSwingAmount, netHeadYaw, headPitch);
        }

        @Override
        public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
            if (rootScale == 1.0F) {
                super.renderToBuffer(pose, buffer, light, overlay, r, g, b, a);
                return;
            }
            pose.pushPose();
            pose.translate(0.0, 24.016F * (1.0F - rootScale) / 16.0F, 0.0);
            pose.scale(rootScale, rootScale, rootScale);
            super.renderToBuffer(pose, buffer, light, overlay, r, g, b, a);
            pose.popPose();
        }
    }

    /** 26.1 {@code BabyOcelotModel} (the ocelot never sits or lies down). */
    public static class BabyOcelotModel extends OcelotModel<Ocelot> {
        private final Feline f;

        public BabyOcelotModel(ModelPart root) {
            super(root);
            this.f = new Feline(root);
        }

        @Override
        public void prepareMobModel(Ocelot ocelot, float limbSwing, float limbSwingAmount, float partialTicks) {
            f.crouching = ocelot.isCrouching();
            f.sprinting = ocelot.isSprinting();
        }

        @Override
        public void setupAnim(Ocelot ocelot, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            f.setupAnim(limbSwing, limbSwingAmount, netHeadYaw, headPitch);
        }
    }

    // ---------------------------------------------------------------- wolf

    /**
     * 26.1 {@code BabyWolfModel.createBodyLayer}. 1.19.2's {@code WolfModel} also looks up head/real_head,
     * upper_body and tail/real_tail: real_head, upper_body and real_tail are added empty, the 26.1 cubes stay on head
     * and tail (so head roll keeps 26.1's rotation order).
     */
    static LayerDefinition wolf() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-2.99F, -3.25F, -3.0F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.025F))
                        .texOffs(17, 12).addBox(-1.5F, -0.24F, -5.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 18.25F, -4.0F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(0, 5).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(-2.0F, -4.25F, -0.5F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(20, 5).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(2.0F, -4.25F, -0.5F));
        head.addOrReplaceChild("real_head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 4.0F, 8.0F),
                PartPose.offset(0.0F, 19.0F, 0.0F));
        root.addOrReplaceChild("upper_body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(-1.5F, 21.0F, 3.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(8, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(1.5F, 21.0F, 3.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(-1.5F, 21.0F, -3.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(20, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(1.5F, 21.0F, -3.0F));
        PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 19.0F, 3.0F, -0.5236F, 0.0F, 0.0F));
        tail.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(22, 16).addBox(-1.0F, -5.7F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -0.6F, 0.2F, -3.1F, 0.0F, 0.0F));
        tail.addOrReplaceChild("real_tail", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** 26.1 {@code BabyWolfModel} (+ {@code WolfModel.setupAnim}); also carries the wet shade, see prepareMobModel. */
    public static class BabyWolfModel extends WolfModel<Wolf> {
        private final ModelPart root, head, body, tail, rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg;
        private float headRoll, bodyRoll0, bodyRollBody, bodyRollTail;

        public BabyWolfModel(ModelPart root) {
            super(root);
            this.root = root;
            head = root.getChild("head");
            body = root.getChild("body");
            tail = root.getChild("tail");
            rightHindLeg = root.getChild("right_hind_leg");
            leftHindLeg = root.getChild("left_hind_leg");
            rightFrontLeg = root.getChild("right_front_leg");
            leftFrontLeg = root.getChild("left_front_leg");
        }

        @Override
        public void prepareMobModel(Wolf wolf, float limbSwing, float limbSwingAmount, float partialTicks) {
            headRoll = wolf.getHeadRollAngle(partialTicks);
            bodyRoll0 = wolf.getBodyRollAngle(partialTicks, 0.0F);
            bodyRollBody = wolf.getBodyRollAngle(partialTicks, -0.16F);
            bodyRollTail = wolf.getBodyRollAngle(partialTicks, -0.2F);
            // 1.19.2 WolfRenderer shades the renderer's model before the baby model is swapped in
            float shade = wolf.isWet() ? wolf.getWetShade(partialTicks) : 1.0F;
            setColor(shade, shade, shade);
        }

        @Override
        public void setupAnim(Wolf wolf, float pos, float speed, float ageInTicks, float netHeadYaw, float headPitch) {
            reset(root);
            tail.yRot = wolf.isAngry() ? 0.0F : Mth.cos(pos * 0.6662F) * 1.4F * speed;
            if (wolf.isInSittingPose()) {
                body.y += 4.0F * AGE_SCALE;
                body.z -= 2.0F * AGE_SCALE;
                body.xRot = (float) (Math.PI / 4);
                tail.y += 9.0F * AGE_SCALE;
                tail.z -= 2.0F * AGE_SCALE;
                rightHindLeg.y += 6.7F * AGE_SCALE;
                rightHindLeg.z -= 5.0F * AGE_SCALE;
                rightHindLeg.xRot = (float) (Math.PI * 3.0 / 2.0);
                leftHindLeg.y += 6.7F * AGE_SCALE;
                leftHindLeg.z -= 5.0F * AGE_SCALE;
                leftHindLeg.xRot = (float) (Math.PI * 3.0 / 2.0);
                rightFrontLeg.xRot = 5.811947F;
                rightFrontLeg.x += 0.01F * AGE_SCALE;
                rightFrontLeg.y += 1.0F * AGE_SCALE;
                leftFrontLeg.xRot = 5.811947F;
                leftFrontLeg.x -= 0.01F * AGE_SCALE;
                leftFrontLeg.y += 1.0F * AGE_SCALE;
                body.xRot--; // BabyWolfModel.setSittingPose
            } else {
                rightHindLeg.xRot = Mth.cos(pos * 0.6662F) * 1.4F * speed;
                leftHindLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * 1.4F * speed;
                rightFrontLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * 1.4F * speed;
                leftFrontLeg.xRot = Mth.cos(pos * 0.6662F) * 1.4F * speed;
            }
            body.zRot = bodyRollBody;
            head.zRot = headRoll + bodyRoll0;
            tail.zRot = bodyRollTail;
            head.xRot = headPitch * DEG;
            head.yRot = netHeadYaw * DEG;
            tail.xRot = wolf.getTailAngle();
        }
    }

    // ---------------------------------------------------------------- fox

    /** 26.1 {@code BabyFoxModel.createBodyLayer}. */
    static LayerDefinition fox() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -2.125F, -5.125F, 6.0F, 5.0F, 5.0F)
                        .texOffs(18, 20).addBox(-1.0F, 0.875F, -7.125F, 2.0F, 2.0F, 2.0F)
                        .texOffs(22, 8).addBox(-3.0F, -4.125F, -4.125F, 2.0F, 2.0F, 1.0F)
                        .texOffs(22, 11).addBox(1.0F, -4.125F, -4.125F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 18.125F, 0.125F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(22, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(-1.5F, 22.0F, 4.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(1.5F, 22.0F, 4.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(22, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(-1.5F, 22.0F, 0.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(1.5F, 22.0F, 0.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, 20.0F, 2.0F));
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 20).addBox(-1.5F, -1.48F, -1.0F, 3.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -0.5F, 3.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** 26.1 {@code BabyFoxModel} (+ {@code FoxModel.setupAnim}). */
    public static class BabyFoxModel extends FoxModel<Fox> {
        private final ModelPart root, body, tail, rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg;
        private final PetAnimations.Baked walk;
        private float headRoll, crouchAmount, legMotionPos;

        public BabyFoxModel(ModelPart root) {
            super(root);
            this.root = root;
            body = root.getChild("body");
            tail = body.getChild("tail");
            rightHindLeg = root.getChild("right_hind_leg");
            leftHindLeg = root.getChild("left_hind_leg");
            rightFrontLeg = root.getChild("right_front_leg");
            leftFrontLeg = root.getChild("left_front_leg");
            walk = new PetAnimations.Baked(PetAnimations.FOX_BABY_WALK, PetAnimations.lookup(root));
        }

        @Override
        public void prepareMobModel(Fox fox, float limbSwing, float limbSwingAmount, float partialTicks) {
            headRoll = fox.getHeadRollAngle(partialTicks);
            crouchAmount = fox.getCrouchAmount(partialTicks);
        }

        @Override
        public void setupAnim(Fox fox, float pos, float speed, float ageInTicks, float netHeadYaw, float headPitch) {
            reset(root);
            boolean sleeping = fox.isSleeping(), crouching = fox.isCrouching(), faceplanted = fox.isFaceplanted();
            // setWalkingPose
            head.zRot = headRoll;
            setLegsVisible(true);
            walk.applyWalk(pos, speed, 1.0F, 2.5F);
            if (crouching) {
                body.xRot += 0.10471976F;
                head.y += crouchAmount * AGE_SCALE;
                float wiggle = Mth.cos(ageInTicks) * 0.05F;
                body.yRot = wiggle;
                rightHindLeg.zRot = wiggle;
                leftHindLeg.zRot = wiggle;
                rightFrontLeg.zRot = wiggle / 2.0F;
                leftFrontLeg.zRot = wiggle / 2.0F;
                body.y += crouchAmount / 6.0F;
            } else if (sleeping) {
                setLegsVisible(false);
                body.zRot = (float) (-Math.PI / 2);
                body.xRot = (float) (-Math.PI / 18);
                body.y++;
                body.z--;
                body.x--;
                tail.xRot = -2.1816616F;
                tail.x -= 0.7F;
                tail.z += 0.6F;
                tail.y += 0.9F;
                head.x -= 2.0F;
                head.y += 2.8F;
                head.z -= 4.0F;
                head.yRot = (float) (-Math.PI * 2.0 / 3.0);
                head.zRot = 0.0F;
            } else if (fox.isSitting()) {
                head.xRot = 0.0F;
                head.yRot = 0.0F;
                body.xRot = -0.959931F;
                body.z -= 4.5F * AGE_SCALE;
                body.y += 3.0F * AGE_SCALE;
                tail.y -= 0.6F;
                tail.z -= 2.0F * AGE_SCALE;
                tail.xRot = 0.95993114F;
                head.y -= 0.75F;
                rightFrontLeg.xRot = (float) (-Math.PI / 12);
                leftFrontLeg.xRot = (float) (-Math.PI / 12);
                rightFrontLeg.z--;
                leftFrontLeg.z--;
                rightFrontLeg.x += 0.01F;
                leftFrontLeg.x -= 0.01F;
                rightHindLeg.z -= 3.75F;
                leftHindLeg.z -= 3.75F;
                rightHindLeg.x += 0.01F;
                leftHindLeg.x -= 0.01F;
            }

            if (!sleeping && !faceplanted && !crouching) {
                head.xRot = headPitch * DEG;
                head.yRot = netHeadYaw * DEG;
            }
            if (sleeping) {
                head.xRot = 0.0F;
                head.yRot = (float) (-Math.PI * 2.0 / 3.0);
                head.zRot = Mth.cos(ageInTicks * 0.027F) / 22.0F;
            }
            if (faceplanted) {
                legMotionPos += 0.67F;
                rightHindLeg.xRot = Mth.cos(legMotionPos * 0.4662F) * 0.1F;
                leftHindLeg.xRot = Mth.cos(legMotionPos * 0.4662F + (float) Math.PI) * 0.1F;
                rightFrontLeg.xRot = Mth.cos(legMotionPos * 0.4662F + (float) Math.PI) * 0.1F;
                leftFrontLeg.xRot = Mth.cos(legMotionPos * 0.4662F) * 0.1F;
            }
        }

        private void setLegsVisible(boolean visible) {
            rightHindLeg.visible = visible;
            leftHindLeg.visible = visible;
            rightFrontLeg.visible = visible;
            leftFrontLeg.visible = visible;
        }
    }

    // ---------------------------------------------------------------- rabbit

    /** Parts 1.19.2's {@code RabbitModel} looks up directly under the root; the 26.1 baby nests or lacks them. */
    private static final String[] RABBIT_PLACEHOLDERS = {"left_hind_foot", "right_hind_foot", "left_haunch", "right_haunch",
            "left_front_leg", "right_front_leg", "head", "right_ear", "left_ear", "tail", "nose"};

    /**
     * 26.1 {@code BabyRabbitModel.createBodyLayer}: body (with head, ears, tail and front legs) and backlegs. The parts
     * 1.19.2 requires at the root get empty placeholders there; the 26.1 parts of the same names stay nested.
     */
    static LayerDefinition rabbit() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 1.6F));
        body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, -1.6F, -0.5236F, 0.0F, 0.0F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -2.2F, 2.0F));
        tail.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(0, 21).addBox(-1.4F, -2.0268F, -1.0177F, 3.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(-0.1F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -3.0F, -3.0F, 5.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, -5.0F, -2.6F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(18, 0).addBox(-1.0F, -3.5F, -0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(-1.5F, -3.5F, -0.5F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -3.5F, -0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(1.5F, -3.5F, -0.5F));
        PartDefinition frontLegs = body.addOrReplaceChild("frontlegs", CubeListBuilder.create(), PartPose.offset(0.0F, -2.5F, -2.6F));
        PartDefinition leftFrontLeg = frontLegs.addOrReplaceChild("left_front_leg", CubeListBuilder.create(),
                PartPose.offsetAndRotation(1.0F, 1.0F, -0.5F, 0.3927F, 0.0F, 0.0F));
        leftFrontLeg.addOrReplaceChild("left_front_leg_r1", CubeListBuilder.create().texOffs(18, 8).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
        PartDefinition rightFrontLeg = frontLegs.addOrReplaceChild("right_front_leg", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-1.0F, 1.0F, -0.5F, 0.3927F, 0.0F, 0.0F));
        rightFrontLeg.addOrReplaceChild("right_front_leg_r1", CubeListBuilder.create().texOffs(14, 8).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
        PartDefinition backLegs = root.addOrReplaceChild("backlegs", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 2.0F));
        PartDefinition leftBackLeg = backLegs.addOrReplaceChild("left_hind_leg", CubeListBuilder.create(),
                PartPose.offsetAndRotation(1.5F, 0.5F, 0.5F, 0.0F, 3.1416F, 0.0F));
        leftBackLeg.addOrReplaceChild("left_haunch", CubeListBuilder.create().texOffs(10, 17).addBox(-2.0F, -0.5F, 0.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(1.0F, 0.0F, 0.5F, 0.0F, -0.7854F, 0.0F));
        PartDefinition rightBackLeg = backLegs.addOrReplaceChild("right_hind_leg", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-1.5F, 0.5F, 0.5F, 0.0F, 3.1416F, 0.0F));
        rightBackLeg.addOrReplaceChild("right_haunch", CubeListBuilder.create().texOffs(0, 17).addBox(-2.0F, -0.5F, 0.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.5F, 0.0F, -0.9F, 0.0F, 0.7854F, 0.0F));
        for (String name : RABBIT_PLACEHOLDERS) root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    /**
     * 26.1 {@code Rabbit.setupAnimationStates}, client side: 1.19.2 rabbits have no hop / idle head tilt animation
     * states, so each rendered baby keeps them here, stepped once per entity tick.
     */
    private static final class RabbitAnimState {
        private static final int STOPPED = Integer.MIN_VALUE;
        private final RandomSource random = RandomSource.create();
        int hopStart = STOPPED, idleStart = STOPPED, idleTimeout = random.nextInt(40) + 180, lastTick;

        RabbitAnimState(Rabbit rabbit) {
            lastTick = rabbit.tickCount - 1;
        }

        void catchUp(Rabbit rabbit) {
            int now = rabbit.tickCount;
            if (now < lastTick) lastTick = now - 1;
            for (int t = Math.max(lastTick + 1, now - 20); t <= now; t++) step(rabbit, t);
            lastTick = now;
        }

        private void step(Rabbit rabbit, int tick) {
            if (rabbit.isLeashed()) idleStart = STOPPED; // 26.1 setLeashData stops the head tilt
            if (idleTimeout <= 0 && !rabbit.isLeashed() && !rabbit.isNoAi()) {
                idleTimeout = random.nextInt(40) + 180;
                idleStart = tick;
            } else if (rabbit.getJumpCompletion(0.0F) > 0.0F) { // 26.1 jumpTicks > 0
                if (hopStart == STOPPED) hopStart = tick;
                idleStart = STOPPED;
            } else {
                idleTimeout--;
                hopStart = STOPPED;
            }
        }
    }

    /** 26.1 {@code BabyRabbitModel} (+ {@code RabbitModel.setupAnim}). */
    public static class BabyRabbitModel extends RabbitModel<Rabbit> {
        private static final Map<Rabbit, RabbitAnimState> STATES = new WeakHashMap<>();
        private final ModelPart root, head;
        private final PetAnimations.Baked hop, idleHeadTilt;

        public BabyRabbitModel(ModelPart root) {
            super(root);
            this.root = root;
            ModelPart body = root.getChild("body");
            ModelPart backLegs = root.getChild("backlegs");
            this.head = body.getChild("head");
            // the 26.1 names, looked up in the real tree (the root's same-named children are the empty placeholders)
            java.util.function.Function<String, ModelPart> nested = PetAnimations.lookup(body, backLegs);
            java.util.function.Function<String, ModelPart> lookup = name -> switch (name) {
                case "body" -> body;
                case "backlegs" -> backLegs;
                default -> nested.apply(name);
            };
            this.hop = new PetAnimations.Baked(PetAnimations.HOP, lookup);
            this.idleHeadTilt = new PetAnimations.Baked(PetAnimations.IDLE_HEAD_TILT, lookup);
        }

        @Override
        public void prepareMobModel(Rabbit rabbit, float limbSwing, float limbSwingAmount, float partialTicks) {
        }

        @Override
        public void setupAnim(Rabbit rabbit, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            RabbitAnimState s = STATES.computeIfAbsent(rabbit, RabbitAnimState::new);
            s.catchUp(rabbit);
            reset(root);
            if (s.idleStart == RabbitAnimState.STOPPED) {
                head.yRot = netHeadYaw * DEG;
                head.xRot = headPitch * DEG;
            }
            if (s.hopStart != RabbitAnimState.STOPPED) hop.apply((long) ((ageInTicks - s.hopStart) * 50.0F), 1.0F);
            if (s.idleStart != RabbitAnimState.STOPPED) idleHeadTilt.apply((long) ((ageInTicks - s.idleStart) * 50.0F), 1.0F);
        }

        /** The whole 26.1 tree, unscaled (1.19.2 draws the adult at 0.6 and its own baby in two scaled halves). */
        @Override
        public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
            root.render(pose, buffer, light, overlay, r, g, b, a);
        }
    }
}
