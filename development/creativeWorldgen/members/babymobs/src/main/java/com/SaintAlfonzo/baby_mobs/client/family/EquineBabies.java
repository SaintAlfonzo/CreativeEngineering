package com.SaintAlfonzo.baby_mobs.client.family;

import com.SaintAlfonzo.baby_mobs.client.kit.BabyKit;
import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import net.minecraft.client.model.ChestedHorseModel;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Markings;

/**
 * 26.1 baby horse (also skeleton and zombie horse), donkey, mule, llama and trader llama.
 *
 * <p>1.19.2 differences handled here: the horse renderers scale the whole model (horse 1.1, donkey 0.87, mule 0.92);
 * 26.1 bakes those into the adult meshes only, so the baby pose undoes them. 1.19.2's {@code HorseModel} hides the
 * real legs of a baby and positions parts absolutely in {@code prepareMobModel}; the baby models below replace that
 * with the 26.1 {@code AbstractEquineModel.setupAnim} plus the baby overrides. Babies show no saddle, armour, chests
 * or carpets (26.1 draws none on a baby); the trader llama baby wears 26.1's baby trader decor (EquineLlamaDecorMixin).
 * Horse markings use the baby marking textures (EquineHorseMarkingMixin, 1.19.2's marking layer draws directly).
 */
public final class EquineBabies {
    private EquineBabies() {}

    /** 26.1 baby trader decor: the baby body geometry inflated by 0.2, drawn with the equipment texture. */
    public static BabyKit<Llama> traderKit;
    public static ModelLayerLocation traderDecor;
    public static final ResourceLocation TRADER_DECOR_TEXTURE = BabyKit.tex("equipment/llama_body/trader_llama_baby.png");

    private static final float HORSE_SCALE = 1.1F;
    private static final float DONKEY_SCALE = 0.87F;
    private static final float MULE_SCALE = 0.92F;

    public static void register() {
        BabyKit<Horse> horse = BabyKit.<Horse>of("horse", HorseModel.class, EquineBabies::horse, BabyHorseModel::new,
                        e -> BabyKit.tex("horse/horse_" + horseVariant(e) + "_baby.png"))
                .pose((e, pose) -> pose.scale(1.0F / HORSE_SCALE, 1.0F / HORSE_SCALE, 1.0F / HORSE_SCALE));
        for (Markings m : Markings.values()) {
            String name = markingName(m);
            if (name != null) {
                horse.layerTexture(new ResourceLocation("textures/entity/horse/horse_markings_" + name + ".png"),
                        BabyKit.tex("horse/horse_markings_" + name + "_baby.png"));
            }
        }
        BabyRender.register(EntityType.HORSE, horse);

        BabyRender.register(EntityType.SKELETON_HORSE, BabyKit.<AbstractHorse>of("skeleton_horse", HorseModel.class,
                EquineBabies::horse, BabyHorseModel::new, e -> BabyKit.tex("horse/horse_skeleton_baby.png")));
        BabyRender.register(EntityType.ZOMBIE_HORSE, BabyKit.<AbstractHorse>of("zombie_horse", HorseModel.class,
                EquineBabies::horse, BabyHorseModel::new, e -> BabyKit.tex("horse/horse_zombie_baby.png")));

        BabyRender.register(EntityType.DONKEY, BabyKit.<AbstractChestedHorse>of("donkey", ChestedHorseModel.class,
                        EquineBabies::donkey, BabyDonkeyModel::new, e -> BabyKit.tex("horse/donkey_baby.png"))
                .pose((e, pose) -> pose.scale(1.0F / DONKEY_SCALE, 1.0F / DONKEY_SCALE, 1.0F / DONKEY_SCALE)));
        BabyRender.register(EntityType.MULE, BabyKit.<AbstractChestedHorse>of("mule", ChestedHorseModel.class,
                        EquineBabies::donkey, BabyDonkeyModel::new, e -> BabyKit.tex("horse/mule_baby.png"))
                .pose((e, pose) -> pose.scale(1.0F / MULE_SCALE, 1.0F / MULE_SCALE, 1.0F / MULE_SCALE)));

        BabyRender.register(EntityType.LLAMA, BabyKit.<Llama>of("llama", LlamaModel.class,
                () -> llama(CubeDeformation.NONE), LlamaModel::new, EquineBabies::llamaTexture));
        traderKit = BabyKit.<Llama>of("trader_llama", LlamaModel.class,
                () -> llama(CubeDeformation.NONE), LlamaModel::new, EquineBabies::llamaTexture);
        traderDecor = traderKit.extraLayer("decor", () -> llama(new CubeDeformation(0.2F)));
        BabyRender.register(EntityType.TRADER_LLAMA, traderKit);
    }

    private static String horseVariant(Horse horse) {
        return switch (horse.getVariant()) {
            case WHITE -> "white";
            case CREAMY -> "creamy";
            case CHESTNUT -> "chestnut";
            case BROWN -> "brown";
            case BLACK -> "black";
            case GRAY -> "gray";
            case DARKBROWN -> "darkbrown";
        };
    }

    private static String markingName(Markings m) {
        return switch (m) {
            case NONE -> null;
            case WHITE -> "white";
            case WHITE_FIELD -> "whitefield";
            case WHITE_DOTS -> "whitedots";
            case BLACK_DOTS -> "blackdots";
        };
    }

    private static final String[] LLAMA_VARIANTS = {"creamy", "white", "brown", "gray"};

    private static ResourceLocation llamaTexture(Llama llama) {
        return BabyKit.tex("llama/llama_" + LLAMA_VARIANTS[Mth.clamp(llama.getVariant(), 0, 3)] + "_baby.png");
    }

    /** Empty parts 1.19.2's {@code HorseModel} constructor looks up under head_parts (bridle and reins). */
    private static void saddlePlaceholders(PartDefinition headParts) {
        for (String s : new String[]{"left_saddle_mouth", "right_saddle_mouth", "left_saddle_line", "right_saddle_line",
                "head_saddle", "mouth_saddle_wrap"}) {
            headParts.addOrReplaceChild(s, CubeListBuilder.create(), PartPose.ZERO);
        }
    }

    private static void babyLegPlaceholders(PartDefinition root) {
        for (String s : new String[]{"left_hind_baby_leg", "right_hind_baby_leg", "left_front_baby_leg", "right_front_baby_leg"}) {
            root.addOrReplaceChild(s, CubeListBuilder.create(), PartPose.ZERO);
        }
    }

    /**
     * 26.1 {@code BabyHorseModel.createBabyMesh} (horse, skeleton horse, zombie horse), plus empty parts the 1.19.2
     * constructor requires: the four "baby legs" of 1.19.2's stretched-leg foal, the saddle and the bridle.
     */
    static LayerDefinition horse() {
        CubeDeformation g = CubeDeformation.NONE;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 13).addBox(-4.0F, -3.5F, -7.0F, 8.0F, 7.0F, 14.0F, g),
                PartPose.offset(0.0F, 12.5F, 0.0F));
        body.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(24, 34).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 8.0F, g),
                PartPose.offsetAndRotation(0.0F, -1.0F, 7.0F, -0.7418F, 0.0F, 0.0F));
        root.addOrReplaceChild("left_hind_leg",
                CubeListBuilder.create().texOffs(12, 46).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(2.4F, 16.0F, 5.4F));
        root.addOrReplaceChild("right_hind_leg",
                CubeListBuilder.create().texOffs(0, 46).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(-2.4F, 16.0F, 5.4F));
        root.addOrReplaceChild("left_front_leg",
                CubeListBuilder.create().texOffs(12, 34).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(2.4F, 16.0F, -5.4F));
        root.addOrReplaceChild("right_front_leg",
                CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(-2.4F, 16.0F, -5.4F));
        PartDefinition neck = root.addOrReplaceChild("head_parts",
                CubeListBuilder.create().texOffs(30, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 8.0F, 4.0F, g),
                PartPose.offsetAndRotation(0.0F, 10.0F, -6.0F, 0.6109F, 0.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.9484F, -6.705F, 6.0F, 4.0F, 9.0F, g),
                PartPose.offset(0.0F, -6.0516F, -0.2951F));
        head.addOrReplaceChild("left_ear",
                CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -2.5F, -0.8F, 2.0F, 3.0F, 1.0F, g),
                PartPose.offsetAndRotation(2.0F, -4.2484F, 1.9451F, 0.0F, 0.0F, 0.2618F));
        head.addOrReplaceChild("right_ear",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 3.0F, 1.0F, g),
                PartPose.offsetAndRotation(-2.0F, -4.2484F, 1.645F, 0.0F, 0.0F, -0.2618F));
        body.addOrReplaceChild("saddle", CubeListBuilder.create(), PartPose.ZERO);
        saddlePlaceholders(neck);
        babyLegPlaceholders(root);
        return LayerDefinition.create(mesh, 64, 64);
    }

    /**
     * 26.1 {@code BabyDonkeyModel.createBabyLayer} (donkey and mule). The baby's legs, neck and tail hang off the body;
     * 1.19.2's constructor wants head_parts and the legs at the root, so those are added empty there (and the saddle,
     * bridle and baby-leg placeholders) and {@link BabyDonkeyModel} animates the real ones under the body.
     */
    static LayerDefinition donkey() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 13).addBox(-5.0F, -3.0F, -7.0F, 8.0F, 6.0F, 14.0F),
                PartPose.offset(1.0F, 14.0F, 0.0F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5F, 6.5F));
        tail.addOrReplaceChild("tail_r1",
                CubeListBuilder.create().texOffs(24, 33).addBox(-2.5F, -1.0F, -0.5F, 3.0F, 3.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7418F, 0.0F, 0.0F));
        body.addOrReplaceChild("left_hind_leg",
                CubeListBuilder.create().texOffs(12, 44).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.offset(2.25F, 3.5F, 5.25F));
        body.addOrReplaceChild("right_hind_leg",
                CubeListBuilder.create().texOffs(0, 44).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.offset(-2.4F, 3.5F, 5.4F));
        body.addOrReplaceChild("left_front_leg",
                CubeListBuilder.create().texOffs(12, 33).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.offset(2.4F, 3.5F, -5.3F));
        body.addOrReplaceChild("right_front_leg",
                CubeListBuilder.create().texOffs(0, 33).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.offset(-2.4F, 3.5F, -5.4F));
        PartDefinition neck = body.addOrReplaceChild("head_parts", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, -5.0F));
        neck.addOrReplaceChild("neck_r1",
                CubeListBuilder.create().texOffs(30, 9).addBox(-3.0F, -6.0F, -3.0F, 4.0F, 8.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, -3.0F));
        head.addOrReplaceChild("head_r1",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.6F, -8.4F, 6.0F, 4.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 1.0F, 0.3927F, 0.0F, 0.0F));
        head.addOrReplaceChild("left_ear",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.5F, -0.3F, 2.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, -3.5F, -1.0F, 0.48F, 0.0F, 0.48F));
        head.addOrReplaceChild("right_ear",
                CubeListBuilder.create().texOffs(22, 0).mirror().addBox(-2.0F, -6.5F, -0.3F, 2.0F, 7.0F, 1.0F).mirror(false),
                PartPose.offsetAndRotation(-2.0F, -3.5F, -1.0F, 0.48F, 0.0F, -0.48F));
        body.addOrReplaceChild("right_chest", CubeListBuilder.create(), PartPose.offset(-1.0F, 10.0F, 0.0F));
        body.addOrReplaceChild("left_chest", CubeListBuilder.create(), PartPose.offset(-1.0F, 10.0F, 0.0F));
        // 1.19.2 constructor placeholders
        body.addOrReplaceChild("saddle", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition rootHead = root.addOrReplaceChild("head_parts", CubeListBuilder.create(), PartPose.ZERO);
        saddlePlaceholders(rootHead);
        for (String s : new String[]{"left_hind_leg", "right_hind_leg", "left_front_leg", "right_front_leg"}) {
            root.addOrReplaceChild(s, CubeListBuilder.create(), PartPose.ZERO);
        }
        babyLegPlaceholders(root);
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** 26.1 {@code BabyLlamaModel.createBodyLayer} (llama, trader llama; inflated 0.2 = the trader decor). */
    static LayerDefinition llama(CubeDeformation g) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -9.0F, -4.0F, 6.0F, 11.0F, 4.0F, g)
                        .texOffs(0, 15).addBox(-1.5F, -7.0F, -7.0F, 3.0F, 3.0F, 3.0F, g)
                        .texOffs(20, 4).addBox(0.5F, -11.0F, -3.0F, 2.0F, 2.0F, 2.0F, g)
                        .texOffs(20, 0).addBox(-2.5F, -11.0F, -3.0F, 2.0F, 2.0F, 2.0F, g),
                PartPose.offset(0.0F, 12.0F, -4.0F));
        root.addOrReplaceChild("right_hind_leg",
                CubeListBuilder.create().texOffs(0, 45).addBox(-1.4F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(-2.5F, 16.5F, 4.5F));
        root.addOrReplaceChild("left_hind_leg",
                CubeListBuilder.create().texOffs(12, 45).addBox(-1.6F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(2.5F, 16.5F, 4.5F));
        root.addOrReplaceChild("right_front_leg",
                CubeListBuilder.create().texOffs(0, 34).addBox(-1.4F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(-2.5F, 16.5F, -3.5F));
        root.addOrReplaceChild("left_front_leg",
                CubeListBuilder.create().texOffs(12, 34).addBox(-1.6F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(2.5F, 16.5F, -3.5F));
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 15).addBox(-4.0F, -3.0F, -8.5F, 8.0F, 6.0F, 13.0F, g), PartPose.offset(0.0F, 14.0F, 2.5F));
        root.addOrReplaceChild("right_chest",
                CubeListBuilder.create().texOffs(45, 28).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, g),
                PartPose.offsetAndRotation(-8.5F, 4.0F, 3.0F, 0.0F, (float) (Math.PI / 2), 0.0F));
        root.addOrReplaceChild("left_chest",
                CubeListBuilder.create().texOffs(45, 41).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, g),
                PartPose.offsetAndRotation(5.5F, 4.0F, 3.0F, 0.0F, (float) (Math.PI / 2), 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** The equine baby animation inputs 26.1 reads from the render state, captured in prepareMobModel. */
    private static final class EquineAnim {
        float eating, standing, feeding;
        boolean animateTail, inWater;

        void capture(AbstractHorse e, float partialTicks) {
            eating = e.getEatAnim(partialTicks);
            standing = e.getStandAnim(partialTicks);
            feeding = e.getMouthAnim(partialTicks);
            animateTail = e.tailCounter > 0;
            inWater = e.isInWater();
        }
    }

    /** 26.1 baby ageScale (tail sway distance). */
    private static final float AGE_SCALE = 0.5F;

    /**
     * Ported 26.1 {@code AbstractEquineModel.setupAnim}, parameterised by the baby overrides. limbSwing / amount are
     * 26.1's walkAnimationPos / Speed (1.19.2 already triples a baby's limbSwing as 26.1's walk animation does).
     */
    private static void animate(ModelPart body, ModelPart headParts, ModelPart rightHindLeg, ModelPart leftHindLeg,
                                ModelPart rightFrontLeg, ModelPart leftFrontLeg, ModelPart tail, EquineAnim a,
                                float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
                                float legStandingYOffset, float legStandingZOffset, float legStandAngle,
                                float legStandingXRotOffset, float tailXRotOffset) {
        float clampedYRot = Mth.clamp(Mth.wrapDegrees(netHeadYaw), -20.0F, 20.0F);
        float headRotXRad = headPitch * Mth.DEG_TO_RAD;
        if (limbSwingAmount > 0.2F) headRotXRad += Mth.cos(limbSwing * 0.8F) * 0.15F * limbSwingAmount;
        float eating = a.eating;
        float standing = a.standing;
        float iStanding = 1.0F - standing;
        headParts.xRot = (float) (Math.PI / 6) + headRotXRad;
        headParts.yRot = clampedYRot * Mth.DEG_TO_RAD;
        float waterMultiplier = a.inWater ? 0.2F : 1.0F;
        float legAnim1 = Mth.cos(waterMultiplier * limbSwing * 0.6662F + (float) Math.PI);
        float legXRotAnim = legAnim1 * 0.8F * limbSwingAmount;
        float baseHeadAngle = (1.0F - Math.max(standing, eating))
                * ((float) (Math.PI / 6) + headRotXRad + a.feeding * Mth.sin(ageInTicks) * 0.05F);
        headParts.xRot = standing * ((float) (Math.PI / 12) + headRotXRad)
                + eating * (2.1816616F + Mth.sin(ageInTicks) * 0.05F) + baseHeadAngle;
        headParts.yRot = standing * clampedYRot * Mth.DEG_TO_RAD + (1.0F - Math.max(standing, eating)) * headParts.yRot;
        // animateHeadPartsPlacement is model-specific: the caller applies it right after this (see the models)
        body.xRot = standing * (float) (-Math.PI / 4) + iStanding * body.xRot;
        leftFrontLeg.y = leftFrontLeg.y - legStandingYOffset * standing;
        leftFrontLeg.z = leftFrontLeg.z + legStandingZOffset * standing;
        rightFrontLeg.y = leftFrontLeg.y;
        rightFrontLeg.z = leftFrontLeg.z;
        float standAngle = legStandAngle * standing;
        float bobValue = Mth.cos(ageInTicks * 0.6F + (float) Math.PI);
        float rlegRot = (legStandingXRotOffset + bobValue) * standing + legXRotAnim * iStanding;
        float llegRot = (legStandingXRotOffset - bobValue) * standing - legXRotAnim * iStanding;
        leftHindLeg.xRot = standAngle - legAnim1 * 0.5F * limbSwingAmount * iStanding;
        rightHindLeg.xRot = standAngle + legAnim1 * 0.5F * limbSwingAmount * iStanding;
        leftFrontLeg.xRot = rlegRot;
        rightFrontLeg.xRot = llegRot;
        tail.xRot = tailXRotOffset + (float) (Math.PI / 6) + limbSwingAmount * 0.75F;
        tail.y += limbSwingAmount * AGE_SCALE;
        tail.z += limbSwingAmount * 2.0F * AGE_SCALE;
        tail.yRot = a.animateTail ? Mth.cos(ageInTicks * 0.7F) : 0.0F;
    }

    /**
     * 26.1 {@code BabyHorseModel} as a 1.19.2 {@code HorseModel}: the adult prepareMobModel (absolute adult positions,
     * hidden real legs for a baby) is replaced by the 26.1 animation with the baby overrides.
     */
    public static class BabyHorseModel<T extends AbstractHorse> extends HorseModel<T> {
        private final ModelPart root;
        private final ModelPart rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, tail;
        private final EquineAnim anim = new EquineAnim();

        public BabyHorseModel(ModelPart root) {
            super(root);
            this.root = root;
            this.rightHindLeg = root.getChild("right_hind_leg");
            this.leftHindLeg = root.getChild("left_hind_leg");
            this.rightFrontLeg = root.getChild("right_front_leg");
            this.leftFrontLeg = root.getChild("left_front_leg");
            this.tail = body.getChild("tail");
        }

        @Override
        public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTicks) {
            anim.capture(entity, partialTicks);
        }

        @Override
        public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            root.getAllParts().forEach(ModelPart::resetPose);
            animate(body, headParts, rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, tail, anim,
                    limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                    4.0F, 0.0F, (float) (Math.PI / 12), (float) (-Math.PI / 3), (float) (-Math.PI / 2));
            // BabyHorseModel.animateHeadPartsPlacement
            headParts.y = headParts.y + Mth.lerp(anim.eating, Mth.lerp(anim.standing, 0.0F, -2.0F), 2.0F);
            headParts.z = Mth.lerp(anim.standing, headParts.z, -4.0F);
        }
    }

    /**
     * 26.1 {@code BabyDonkeyModel} as a 1.19.2 {@code ChestedHorseModel}. The real legs, neck and tail are children of
     * the body (they rear with it); the root-level parts the 1.19.2 constructor binds are empty placeholders.
     */
    public static class BabyDonkeyModel<T extends AbstractChestedHorse> extends ChestedHorseModel<T> {
        private final ModelPart root;
        private final ModelPart neck, rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, tail;
        private final EquineAnim anim = new EquineAnim();

        public BabyDonkeyModel(ModelPart root) {
            super(root);
            this.root = root;
            this.neck = body.getChild("head_parts");
            this.rightHindLeg = body.getChild("right_hind_leg");
            this.leftHindLeg = body.getChild("left_hind_leg");
            this.rightFrontLeg = body.getChild("right_front_leg");
            this.leftFrontLeg = body.getChild("left_front_leg");
            this.tail = body.getChild("tail");
        }

        @Override
        public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTicks) {
            anim.capture(entity, partialTicks);
        }

        @Override
        public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            root.getAllParts().forEach(ModelPart::resetPose);
            float eating = anim.eating;
            float standing = anim.standing;
            animate(body, neck, rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, tail, anim,
                    limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                    1.0F, 0.5F, (float) (Math.PI / 3), 0.0F, (float) (-Math.PI / 4));
            // BabyDonkeyModel.animateHeadPartsPlacement
            neck.y = Mth.lerp(eating, neck.y, -1.2F);
            neck.z = Mth.lerp(standing, neck.z, -3.6F);
            // BabyDonkeyModel.offsetLegPositionWhenStanding (26.1 lerps the right leg from the updated left one; kept)
            leftHindLeg.y = Mth.lerp(standing, leftHindLeg.y, -0.3F);
            rightHindLeg.y = Mth.lerp(standing, leftHindLeg.y, -0.3F);
            // BabyDonkeyModel.setupAnim: the head angle again with a fixed -30 degree pitch and a 90 degree graze
            float headRotXRad = -30.0F * Mth.DEG_TO_RAD;
            float baseHeadAngle = (1.0F - Math.max(standing, eating))
                    * ((float) (Math.PI / 6) + headRotXRad + anim.feeding * Mth.sin(ageInTicks) * 0.05F);
            neck.xRot = standing * ((float) (Math.PI / 12) + headRotXRad)
                    + eating * ((float) (Math.PI / 2) + Mth.sin(ageInTicks) * 0.05F) + baseHeadAngle;
        }
    }
}
