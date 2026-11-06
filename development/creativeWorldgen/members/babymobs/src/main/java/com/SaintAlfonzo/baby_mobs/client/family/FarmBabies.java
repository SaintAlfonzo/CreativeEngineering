package com.SaintAlfonzo.baby_mobs.client.family;

import com.SaintAlfonzo.baby_mobs.client.kit.BabyKit;
import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;

/**
 * 26.1 baby cow, mooshroom, pig, sheep (and its wool) and chicken. 1.19.2 has one cow, pig and chicken: they get 26.1's
 * temperate baby. The 1.19.2 model classes take the 26.1 geometry unchanged except where noted.
 */
public final class FarmBabies {
    private FarmBabies() {}

    public static void register() {
        BabyKit<Cow> cow = BabyKit.of("cow", CowModel.class, FarmBabies::cow, CowModel::new,
                e -> BabyKit.tex("cow/cow_temperate_baby.png"));
        BabyRender.register(EntityType.COW, cow);

        BabyKit<MushroomCow> mooshroom = BabyKit.of("mooshroom", CowModel.class, FarmBabies::cow, CowModel::new,
                e -> BabyKit.tex(e.getMushroomType() == MushroomCow.MushroomType.BROWN
                        ? "cow/mooshroom_brown_baby.png" : "cow/mooshroom_red_baby.png"));
        BabyRender.register(EntityType.MOOSHROOM, mooshroom);

        BabyRender.register(EntityType.PIG, BabyKit.<Pig>of("pig", PigModel.class, FarmBabies::pig, PigModel::new,
                e -> BabyKit.tex("pig/pig_temperate_baby.png")));

        BabyKit<Sheep> sheep = BabyKit.<Sheep>of("sheep", SheepModel.class, FarmBabies::sheep, BabySheepModel::new,
                        e -> BabyKit.tex("sheep/sheep_baby.png"))
                // 26.1 SHEEP_BABY_WOOL is the baby body geometry itself, drawn with the wool texture
                .layerModel(new ResourceLocation("textures/entity/sheep/sheep_fur.png"), BabyKit.tex("sheep/sheep_wool_baby.png"),
                        "wool", FarmBabies::sheep, BabySheepFurModel::new)
                // More Mob Variants swaps the wool texture per coat (its SheepWoolFeatureMixin); no baby wool for those
                .layerPrefixAs("textures/entity/sheep/wool/", new ResourceLocation("textures/entity/sheep/sheep_fur.png"));
        BabyRender.register(EntityType.SHEEP, sheep);

        BabyRender.register(EntityType.CHICKEN, BabyKit.<Chicken>of("chicken", ChickenModel.class, FarmBabies::chicken, ChickenModel::new,
                e -> BabyKit.tex("chicken/chicken_temperate_baby.png")));
    }

    /** 26.1 {@code BabyCowModel.createBodyLayer} (cow and mooshroom). */
    static LayerDefinition cow() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-3.0F, -4.569F, -4.8333F, 6.0F, 6.0F, 5.0F)
                        .texOffs(8, 29).addBox(3.0F, -5.569F, -3.8333F, 1.0F, 2.0F, 1.0F)
                        .texOffs(4, 29).mirror().addBox(-4.0F, -5.569F, -3.8333F, 1.0F, 2.0F, 1.0F).mirror(false)
                        .texOffs(12, 29).addBox(-2.0F, -1.569F, -5.8333F, 4.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 13.569F, -5.1667F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -7.0F, -1.0F, 8.0F, 6.0F, 12.0F),
                PartPose.offset(3.0F, 19.0F, -5.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(22, 18).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(-2.5F, 18.0F, -3.5F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(34, 18).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(2.5F, 18.0F, -3.5F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(22, 27).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(-2.5F, 18.0F, 3.5F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(34, 27).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(2.5F, 18.0F, 3.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** 26.1 {@code BabyPigModel.createBodyLayer}. */
    static LayerDefinition pig() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.0F, -4.5F, 7.0F, 6.0F, 9.0F),
                PartPose.offset(0.0F, 19.0F, 0.5F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 15).addBox(-3.5F, -5.0F, -5.0F, 7.0F, 6.0F, 6.0F, new CubeDeformation(0.025F))
                        .texOffs(6, 27).addBox(-1.5F, -1.975F, -6.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.015F)),
                PartPose.offset(0.0F, 19.0F, -2.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(2.5F, 22.0F, -3.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(23, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(-2.5F, 22.0F, -3.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(2.5F, 22.0F, 4.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(23, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(-2.5F, 22.0F, 4.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** 26.1 {@code BabySheepModel.createBodyLayer} (body and, with the wool texture, the wool layer). */
    static LayerDefinition sheep() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-3.0F, -2.0F, -4.5F, 6.0F, 4.0F, 9.0F),
                PartPose.offset(0.0F, 17.0F, 0.5F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -4.5F, -3.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 15.5F, -2.5F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(-2.0F, 19.0F, 3.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(24, 12).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(2.0F, 19.0F, 3.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(8, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(-2.0F, 19.0F, -2.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(24, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(2.0F, 19.0F, -2.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /**
     * 26.1 {@code BabyChickenModel.createBodyLayer}. The baby has no separate head (it is part of the body cube);
     * 1.19.2's {@code ChickenModel} still looks up head, beak and red_thing, so they are added empty.
     */
    static LayerDefinition chicken() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -2.25F, -0.75F, 4.0F, 4.0F, 4.0F)
                        .texOffs(10, 8).addBox(-1.0F, -0.25F, -1.75F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 20.25F, -1.25F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(2, 2).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0F)
                        .texOffs(0, 1).addBox(-0.5F, 2.0F, -1.0F, 1.0F, 0.0F, 1.0F),
                PartPose.offset(1.0F, 22.0F, 0.5F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 2).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0F)
                        .texOffs(0, 0).addBox(-0.5F, 2.0F, -1.0F, 1.0F, 0.0F, 1.0F),
                PartPose.offset(-1.0F, 22.0F, 0.5F));
        root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(6, 8).addBox(0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F),
                PartPose.offset(2.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(4, 8).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F),
                PartPose.offset(-2.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("beak", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("red_thing", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 16);
    }

    /** Baby head height while grazing: 26.1 {@code head.y + eatPosition * 9 * ageScale}, ageScale 0.5 for a baby. */
    private static final float SHEEP_HEAD_Y = 15.5F;

    /** 1.19.2 {@code SheepModel} sets an absolute adult head height while grazing; the baby's own height instead. */
    public static class BabySheepModel extends SheepModel<Sheep> {
        private final ModelPart babyHead;

        public BabySheepModel(ModelPart root) {
            super(root);
            this.babyHead = root.getChild("head");
        }

        @Override
        public void prepareMobModel(Sheep sheep, float limbSwing, float limbSwingAmount, float partialTicks) {
            super.prepareMobModel(sheep, limbSwing, limbSwingAmount, partialTicks);
            babyHead.y = SHEEP_HEAD_Y + sheep.getHeadEatPositionScale(partialTicks) * 9.0F * 0.5F;
        }
    }

    /** Same for the wool layer. */
    public static class BabySheepFurModel extends SheepFurModel<Sheep> {
        private final ModelPart babyHead;

        public BabySheepFurModel(ModelPart root) {
            super(root);
            this.babyHead = root.getChild("head");
        }

        @Override
        public void prepareMobModel(Sheep sheep, float limbSwing, float limbSwingAmount, float partialTicks) {
            super.prepareMobModel(sheep, limbSwing, limbSwingAmount, partialTicks);
            babyHead.y = SHEEP_HEAD_Y + sheep.getHeadEatPositionScale(partialTicks) * 9.0F * 0.5F;
        }
    }
}
