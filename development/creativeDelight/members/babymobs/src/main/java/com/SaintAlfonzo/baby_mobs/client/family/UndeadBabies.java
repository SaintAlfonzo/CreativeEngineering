package com.SaintAlfonzo.baby_mobs.client.family;

import com.SaintAlfonzo.baby_mobs.BabyMobs;
import com.SaintAlfonzo.baby_mobs.client.kit.BabyKit;
import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import com.google.common.collect.Iterables;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * 26.1 baby zombie, husk, drowned (and its outer layer), zombie villager, villager, piglin and zombified piglin, with
 * 26.1's baby armour (own mesh and textures, mixin/client/UndeadArmorLayerMixin), baby held-item offset
 * (UndeadItemInHandMixin) and full-size head items (UndeadCustomHeadMixin).
 *
 * <p>1.19.2's {@code HumanoidModel.setupAnim} writes absolute adult part positions (arms at x 5, legs at y 12, crouch
 * heights); 26.1 animates relative to each model's default pose. The baby models here run the 1.19.2 animation in the
 * adult frame and then move every part by (baby default - adult default) ({@link Rebase}), which gives 26.1's result.
 */
public final class UndeadBabies {
    private UndeadBabies() {}

    private static final String[] VILLAGER_TYPES = {"desert", "jungle", "plains", "savanna", "snow", "swamp", "taiga"};

    /** Kits of this family (item-in-hand offset) and their armour sets. */
    private static final Map<BabyKit<?>, ArmorSet> ARMOR = new IdentityHashMap<>();

    public static void register() {
        BabyKit<Zombie> zombie = BabyKit.<Zombie>of("zombie", ZombieModel.class, () -> zombie(CubeDeformation.NONE),
                BabyZombieModel::new, e -> BabyKit.tex("zombie/zombie_baby.png"));
        armor(zombie, false);
        BabyRender.register(EntityType.ZOMBIE, zombie);

        // 1.19.2 HuskRenderer scales by 1.0625; 26.1 scales only the adult mesh, the baby is the plain baby zombie.
        BabyKit<Husk> husk = BabyKit.<Husk>of("husk", ZombieModel.class, () -> zombie(CubeDeformation.NONE),
                        BabyZombieModel::new, e -> BabyKit.tex("zombie/husk_baby.png"))
                .pose((e, pose) -> pose.scale(1.0F / 1.0625F, 1.0F / 1.0625F, 1.0F / 1.0625F));
        armor(husk, false);
        BabyRender.register(EntityType.HUSK, husk);

        // 26.1 BabyDrownedModel = the baby zombie mesh; its outer layer is the same mesh grown by 0.25.
        BabyKit<Drowned> drowned = BabyKit.<Drowned>of("drowned", DrownedModel.class, () -> zombie(CubeDeformation.NONE),
                        BabyDrownedModel::new, e -> BabyKit.tex("zombie/drowned_baby.png"))
                .layerModel(new ResourceLocation("textures/entity/zombie/drowned_outer_layer.png"),
                        BabyKit.tex("zombie/drowned_outer_layer_baby.png"), "outer",
                        () -> zombie(new CubeDeformation(0.25F)), BabyDrownedModel::new);
        armor(drowned, false);
        BabyRender.register(EntityType.DROWNED, drowned);

        BabyKit<ZombieVillager> zombieVillager = BabyKit.<ZombieVillager>of("zombie_villager", ZombieVillagerModel.class,
                UndeadBabies::zombieVillager, BabyZombieVillagerModel::new,
                e -> BabyKit.tex("zombie_villager/zombie_villager_baby.png"));
        // 26.1 VillagerProfessionLayer: babies draw <path>/baby/<type>.png instead of type/<type>.png, no profession.
        for (String type : VILLAGER_TYPES) {
            zombieVillager.layerTexture(new ResourceLocation("textures/entity/zombie_villager/type/" + type + ".png"),
                    BabyKit.tex("zombie_villager/baby/" + type + ".png"));
        }
        // villager types added by other mods have no baby texture: plains clothes
        zombieVillager.layerTexturePrefix("textures/entity/zombie_villager/type/", BabyKit.tex("zombie_villager/baby/plains.png"));
        armor(zombieVillager, false);
        BabyRender.register(EntityType.ZOMBIE_VILLAGER, zombieVillager);

        // 1.19.2 VillagerRenderer draws babies at 0.9375 * 0.5; 26.1 draws the baby mesh unscaled.
        BabyKit<Villager> villager = BabyKit.<Villager>of("villager", VillagerModel.class, UndeadBabies::villager,
                        VillagerModel::new, e -> BabyKit.tex("villager/villager_baby.png"))
                .pose((e, pose) -> pose.scale(1.0F / 0.46875F, 1.0F / 0.46875F, 1.0F / 0.46875F));
        for (String type : VILLAGER_TYPES) {
            villager.layerTexture(new ResourceLocation("textures/entity/villager/type/" + type + ".png"),
                    BabyKit.tex("villager/baby/" + type + ".png"));
        }
        villager.layerTexturePrefix("textures/entity/villager/type/", BabyKit.tex("villager/baby/plains.png"));
        BabyRender.register(EntityType.VILLAGER, villager);

        BabyKit<Piglin> piglin = BabyKit.<Piglin>of("piglin", PiglinModel.class, UndeadBabies::piglin,
                BabyPiglinModel::new, e -> BabyKit.tex("piglin/piglin_baby.png"));
        armor(piglin, true);
        BabyRender.register(EntityType.PIGLIN, piglin);

        BabyKit<ZombifiedPiglin> zombifiedPiglin = BabyKit.<ZombifiedPiglin>of("zombified_piglin", PiglinModel.class,
                UndeadBabies::piglin, BabyPiglinModel::new, e -> BabyKit.tex("piglin/zombified_piglin_baby.png"));
        armor(zombifiedPiglin, true);
        BabyRender.register(EntityType.ZOMBIFIED_PIGLIN, zombifiedPiglin);
    }

    // ------------------------------------------------------------------------------------------------ meshes

    /** 26.1 {@code BabyZombieModel.createBodyLayer} (zombie, husk, drowned, drowned outer layer). Hat moved to root (1.19.2). */
    static LayerDefinition zombie(CubeDeformation g) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-2.0F, -2.5F, -1.0F, 4.0F, 5.0F, 2.0F, g),
                PartPose.offset(0.0F, 17.5F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(3, 3).addBox(-3.0F, -6.25F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(35, 3).addBox(-3.0F, -6.15F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 15.25F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 15.25F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(36, 16).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, g),
                PartPose.offset(-3.0F, 15.5F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(28, 16).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, g),
                PartPose.offset(3.0F, 15.5F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(8, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, g),
                PartPose.offset(-1.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, g),
                PartPose.offset(1.0F, 20.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /**
     * 26.1 {@code BabyZombieVillagerModel.createBodyLayer}. 1.19.2 wants hat at the root with hat_rim under it: the hat
     * cube sits at the head pivot (same box, shifted by its 26.1 offset) and hat_rim hangs under it, same geometry.
     */
    static LayerDefinition zombieVillager() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 15).addBox(-2.0F, -2.75F, -1.5F, 4.0F, 5.0F, 3.0F)
                        .texOffs(16, 22).addBox(-2.0F, -2.75F, -1.5F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 18.75F, 0.0F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -3.5F, 8.0F, 8.0F, 7.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(23, 0).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -1.0F, -4.0F));
        PartDefinition hat = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 31)
                        .addBox(-4.0F, -8.0F, -3.5F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.3F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        hat.addOrReplaceChild("hat_rim", CubeListBuilder.create().texOffs(0, 46).addBox(-7.0F, -0.5F, -6.0F, 14.0F, 1.0F, 12.0F),
                PartPose.offset(0.0F, -4.5F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 15).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(-3.0F, 15.5F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 15).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(3.0F, 15.5F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(8, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(-1.0F, 21.5F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(1.0F, 21.5F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /**
     * 26.1 {@code BabyVillagerModel.createBodyModel}. 1.19.2 {@code VillagerModel} looks up head/hat/hat_rim, so hat_rim
     * moves under hat (offset reduced by hat's own -4, same geometry). bb_main is 26.1's baby jacket.
     */
    static LayerDefinition villager() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition arms = root.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offset(0.0F, 17.5F, 0.0F));
        arms.addOrReplaceChild("right_hand", CubeListBuilder.create()
                        .texOffs(36, 15).addBox(-1.0F, -2.4925F, -1.8401F, 2.0F, 4.0F, 2.0F)
                        .texOffs(16, 15).addBox(5.0F, -2.4925F, -1.8401F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 1.4025F, -0.9599F, -1.0472F, 0.0F, 0.0F));
        arms.addOrReplaceChild("middlearm_r1", CubeListBuilder.create().texOffs(24, 17).addBox(-2.0F, -0.9924F, -0.9825F, 4.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.9024F, -1.8175F, -1.0472F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(8, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(-1.0F, 21.5F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(1.0F, 21.5F, 0.0F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -3.5F, 8.0F, 8.0F, 7.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 30)
                        .addBox(-4.0F, -4.0F, -3.5F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.3F)),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        hat.addOrReplaceChild("hat_rim", CubeListBuilder.create().texOffs(0, 45).addBox(-7.0F, -0.5F, -6.0F, 14.0F, 1.0F, 12.0F),
                PartPose.offset(0.0F, -0.5F, 0.0F));
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(23, 0).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -2.0F, -4.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 15).addBox(-2.0F, -2.75F, -1.5F, 4.0F, 5.0F, 3.0F),
                PartPose.offset(0.0F, 18.75F, 0.0F));
        root.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(16, 21)
                        .addBox(-2.5F, -8.0F, -1.5F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.5F, 24.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /**
     * 26.1 {@code BabyPiglinModel.createBodyLayer} (piglin and zombified piglin). 1.19.2's PiglinModel is a PlayerModel:
     * hat, ear, cloak, sleeves, pants and jacket are required, added empty at the root.
     */
    static LayerDefinition piglin() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 5.0F, 3.0F),
                PartPose.offset(0.0F, 18.0F, -0.5F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(21, 30).addBox(-1.5F, -3.0F, -4.5F, 3.0F, 3.0F, 1.0F)
                        .texOffs(0, 0).addBox(-4.5F, -6.0F, -3.5F, 9.0F, 6.0F, 7.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F));
        PartDefinition leftEar = head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(4.2F, -4.0F, 0.0F));
        leftEar.addOrReplaceChild("left_ear_r1", CubeListBuilder.create().texOffs(0, 21).addBox(-0.5F, -3.0F, -2.0F, 1.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(1.0F, 1.75F, 0.0F, 0.0F, 0.0F, -0.6109F));
        PartDefinition rightEar = head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-4.2F, -4.0F, 0.0F));
        rightEar.addOrReplaceChild("right_ear_r1", CubeListBuilder.create().texOffs(18, 13).addBox(-0.5F, -3.0F, -2.0F, 1.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(-1.0F, 1.75F, 0.0F, 0.0F, 0.0F, 0.6109F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(28, 13).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F),
                PartPose.offset(4.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(10, 30).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F),
                PartPose.offset(-4.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(22, 23).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offset(-1.5F, 20.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(10, 23).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offset(1.5F, 20.0F, 0.0F));
        for (String empty : List.of("hat", "ear", "cloak", "left_sleeve", "right_sleeve", "left_pants", "right_pants", "jacket")) {
            root.addOrReplaceChild(empty, CubeListBuilder.create(), PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    // ------------------------------------------------------------------------------------------------ armour

    /** 26.1 LayerDefinitions: humanoid baby armour deformations; piglins use 0.7 all round and arms 0.5 out, 0.5 up. */
    private static final CubeDeformation BABY_OUTER = new CubeDeformation(-0.1F, 0.5F, 0.3F);
    private static final CubeDeformation BABY_INNER = new CubeDeformation(-0.1F, 0.3F, 0.3F);
    private static final CubeDeformation BABY_PIGLIN = new CubeDeformation(0.7F);

    private static void armor(BabyKit<?> kit, boolean piglin) {
        ArmorSet set = new ArmorSet(kit);
        for (EquipmentSlot slot : ArmorSet.SLOTS) {
            CubeDeformation g = piglin ? BABY_PIGLIN : slot == EquipmentSlot.LEGS ? BABY_INNER : BABY_OUTER;
            float armX = piglin ? 0.5F : 0.0F;
            float armY = piglin ? -0.5F : 0.0F;
            set.layers.put(slot, kit.extraLayer("armor_" + slot.getName(), () -> babyArmor(slot, g, armX, armY)));
        }
        ARMOR.put(kit, set);
    }

    /**
     * 26.1 {@code HumanoidModel.createBabyArmorMesh} cut to one slot the way {@code createArmorMeshSet} does it (only that
     * slot's parts keep their cubes): head = head; chest = body and arms; legs = legs and waist; feet = the two feet.
     * hat moves to the root (1.19.2), inner_body (no slot) is dropped.
     */
    static LayerDefinition babyArmor(EquipmentSlot slot, CubeDeformation g, float armX, float armY) {
        boolean head = slot == EquipmentSlot.HEAD, chest = slot == EquipmentSlot.CHEST;
        boolean legs = slot == EquipmentSlot.LEGS, feet = slot == EquipmentSlot.FEET;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", keep(head, CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -7.0F, -4.5F, 9.0F, 8.0F, 8.0F, g)),
                PartPose.offset(0.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("body", keep(chest, CubeListBuilder.create().texOffs(0, 17).addBox(-3.0F, -3.0F, -1.5F, 6.0F, 5.0F, 3.0F, g)),
                PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("waist", keep(legs, CubeListBuilder.create().texOffs(0, 36).addBox(-3.0F, -1.2F, -1.49F, 5.9F, 2.0F, 2.9F, g.extend(-0.1F))),
                PartPose.offset(0.0F, 19.0F, 0.0F));
        root.addOrReplaceChild("right_arm", keep(chest, CubeListBuilder.create().texOffs(30, 25).addBox(-1.0F, 0.0F, -1.53F, 2.0F, 5.0F, 3.0F, g)),
                PartPose.offset(-3.5F - armX, 15.5F + armY, 0.0F));
        root.addOrReplaceChild("left_arm", keep(chest, CubeListBuilder.create().texOffs(30, 17).addBox(-1.0F, 0.0F, -1.53F, 2.0F, 5.0F, 3.0F, g)),
                PartPose.offset(3.5F + armX, 15.5F + armY, 0.0F));
        // 26.1 names: the foot under left_leg is "right_foot" and vice versa; kept.
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg",
                keep(legs, CubeListBuilder.create().texOffs(18, 24).addBox(-2.0F, -0.2F, -2.0F, 3.0F, 4.0F, 3.0F, g.extend(-0.1F))),
                PartPose.offset(1.5F, 20.0F, 0.5F));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg",
                keep(legs, CubeListBuilder.create().texOffs(18, 17).addBox(-1.0F, -0.2F, -2.0F, 3.0F, 4.0F, 3.0F, g.extend(-0.1F))),
                PartPose.offset(-1.5F, 20.0F, 0.5F));
        leftLeg.addOrReplaceChild("right_foot", keep(feet, CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, 2.9F, -2.0F, 3.0F, 1.0F, 3.0F, g)),
                PartPose.ZERO);
        rightLeg.addOrReplaceChild("left_foot", keep(feet, CubeListBuilder.create().texOffs(0, 29).mirror().addBox(-1.0F, 2.9F, -2.0F, 3.0F, 1.0F, 3.0F, g).mirror(false)),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** The slot's own cubes, or an empty part (26.1 {@code retainExactParts}). */
    private static CubeListBuilder keep(boolean keep, CubeListBuilder cubes) {
        return keep ? cubes : CubeListBuilder.create();
    }

    /** The baby armour models of one kit, baked on first use. */
    public static final class ArmorSet {
        static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        final BabyKit<?> kit;
        final Map<EquipmentSlot, ModelLayerLocation> layers = new java.util.EnumMap<>(EquipmentSlot.class);

        ArmorSet(BabyKit<?> kit) {
            this.kit = kit;
        }

        public BabyArmorModel model(EquipmentSlot slot) {
            return kit.bake(layers.get(slot), BabyArmorModel::new);
        }
    }

    /** The armour set of the baby rendering right now, or null (not one of this family, or not a remodelled baby). */
    @Nullable
    public static ArmorSet activeArmor() {
        BabyRender.Frame f = BabyRender.current();
        return f == null || f.kit() == null ? null : ARMOR.get(f.kit());
    }

    /** True while a remodelled armed baby of this family renders (held-item offset; the villager holds nothing in hand). */
    public static boolean active() {
        BabyRender.Frame f = BabyRender.current();
        return f != null && f.kit() != null && ARMOR.containsKey(f.kit());
    }

    /**
     * 26.1 baby armour texture ({@code equipment/humanoid_baby/<material>.png}) for a vanilla armour piece, or null for
     * modded armour (no baby texture: it keeps 1.19.2's old half-size baby armour, see UndeadArmorLayerMixin).
     */
    @Nullable
    public static ResourceLocation babyArmorTexture(ItemStack stack, boolean overlay) {
        if (!(stack.getItem() instanceof ArmorItem armor) || !(armor.getMaterial() instanceof ArmorMaterials material)) return null;
        if (!"minecraft".equals(Registry.ITEM.getKey(stack.getItem()).getNamespace())) return null;
        String name = material == ArmorMaterials.TURTLE ? "turtle_scute" : material.getName();
        return BabyKit.tex("equipment/humanoid_baby/" + name + (overlay ? "_overlay" : "") + ".png");
    }

    /**
     * 26.1 baby armour piece: a HumanoidModel whose parts carry only one slot's cubes, plus the 26.1 waist (leggings).
     * Posed from the baby body model by {@link #follow}.
     */
    public static class BabyArmorModel extends HumanoidModel<LivingEntity> {
        private final ModelPart waist;
        private final PartPose[] defaults;

        public BabyArmorModel(ModelPart root) {
            super(root);
            this.waist = root.getChild("waist");
            this.defaults = Rebase.store(this);
        }

        @Override
        protected Iterable<ModelPart> bodyParts() {
            return Iterables.concat(super.bodyParts(), List.of(waist));
        }

        /** 26.1 runs the same animation on the armour: same rotations, positions moved by the same amount from defaults. */
        public void follow(HumanoidModel<?> body) {
            PartPose[] bodyDefaults = body instanceof Rebased r ? r.rebase().baby : Rebase.store(body);
            ModelPart[] from = Rebase.parts(body), to = Rebase.parts(this);
            for (int i = 0; i < to.length; i++) {
                ModelPart t = to[i], s = from[i];
                t.x = defaults[i].x + (s.x - bodyDefaults[i].x);
                t.y = defaults[i].y + (s.y - bodyDefaults[i].y);
                t.z = defaults[i].z + (s.z - bodyDefaults[i].z);
                t.xRot = s.xRot;
                t.yRot = s.yRot;
                t.zRot = s.zRot;
            }
            hat.copyFrom(head);
            young = false;
            setAllVisible(true);
        }
    }

    // ------------------------------------------------------------------------------------------------ animation

    /** A baby model that animates through {@link Rebase}. */
    public interface Rebased {
        Rebase rebase();
    }

    /**
     * Runs 1.19.2's absolute-position humanoid animation in the adult frame and moves the result into the baby frame:
     * part position = baby default + (animated adult position - adult default). Parts: head, body, right arm, left arm,
     * right leg, left leg. Adult defaults are 1.19.2's standing values (legs z 0.1 as setupAnim writes it).
     */
    public static final class Rebase {
        static final PartPose[] ADULT = {
                PartPose.ZERO, PartPose.ZERO,
                PartPose.offset(-5.0F, 2.0F, 0.0F), PartPose.offset(5.0F, 2.0F, 0.0F),
                PartPose.offset(-1.9F, 12.0F, 0.1F), PartPose.offset(1.9F, 12.0F, 0.1F)};

        final PartPose[] baby;

        Rebase(HumanoidModel<?> model) {
            this.baby = store(model);
        }

        static ModelPart[] parts(HumanoidModel<?> m) {
            return new ModelPart[]{m.head, m.body, m.rightArm, m.leftArm, m.rightLeg, m.leftLeg};
        }

        static PartPose[] store(HumanoidModel<?> m) {
            ModelPart[] parts = parts(m);
            PartPose[] out = new PartPose[parts.length];
            for (int i = 0; i < parts.length; i++) out[i] = parts[i].storePose();
            return out;
        }

        /** Before the 1.19.2 animation: positions to the adult defaults, rotations to the baby defaults. */
        void toAdult(HumanoidModel<?> m) {
            ModelPart[] parts = parts(m);
            for (int i = 0; i < parts.length; i++) {
                parts[i].x = ADULT[i].x;
                parts[i].y = ADULT[i].y;
                parts[i].z = ADULT[i].z;
                parts[i].xRot = baby[i].xRot;
                parts[i].yRot = baby[i].yRot;
                parts[i].zRot = baby[i].zRot;
            }
        }

        /** After it: into the baby frame; hat follows the head again. */
        void toBaby(HumanoidModel<?> m) {
            ModelPart[] parts = parts(m);
            for (int i = 0; i < parts.length; i++) {
                parts[i].x = baby[i].x + (parts[i].x - ADULT[i].x);
                parts[i].y = baby[i].y + (parts[i].y - ADULT[i].y);
                parts[i].z = baby[i].z + (parts[i].z - ADULT[i].z);
            }
            m.hat.copyFrom(m.head);
        }

        /** The other way, for modded armour drawn with 1.19.2's adult armour model at young = true. */
        public void copyAsAdult(HumanoidModel<?> from, HumanoidModel<?> to) {
            ModelPart[] s = parts(from), t = parts(to);
            for (int i = 0; i < t.length; i++) {
                t[i].copyFrom(s[i]);
                t[i].x = ADULT[i].x + (s[i].x - baby[i].x);
                t[i].y = ADULT[i].y + (s[i].y - baby[i].y);
                t[i].z = ADULT[i].z + (s[i].z - baby[i].z);
            }
            to.hat.copyFrom(to.head);
        }
    }

    /** 26.1 BabyZombieModel (zombie, husk). */
    public static class BabyZombieModel extends ZombieModel<Zombie> implements Rebased {
        private final Rebase rebase;

        public BabyZombieModel(ModelPart root) {
            super(root);
            this.rebase = new Rebase(this);
        }

        @Override
        public Rebase rebase() {
            return rebase;
        }

        @Override
        public void setupAnim(Zombie e, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            rebase.toAdult(this);
            super.setupAnim(e, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            rebase.toBaby(this);
        }
    }

    /** 26.1 BabyDrownedModel (body and outer layer). */
    public static class BabyDrownedModel extends DrownedModel<Drowned> implements Rebased {
        private final Rebase rebase;

        public BabyDrownedModel(ModelPart root) {
            super(root);
            this.rebase = new Rebase(this);
        }

        @Override
        public Rebase rebase() {
            return rebase;
        }

        @Override
        public void setupAnim(Drowned e, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            rebase.toAdult(this);
            super.setupAnim(e, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            rebase.toBaby(this);
        }
    }

    /** 26.1 BabyZombieVillagerModel. */
    public static class BabyZombieVillagerModel extends ZombieVillagerModel<ZombieVillager> implements Rebased {
        private final Rebase rebase;

        public BabyZombieVillagerModel(ModelPart root) {
            super(root);
            this.rebase = new Rebase(this);
        }

        @Override
        public Rebase rebase() {
            return rebase;
        }

        @Override
        public void setupAnim(ZombieVillager e, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            rebase.toAdult(this);
            super.setupAnim(e, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            rebase.toBaby(this);
        }
    }

    /**
     * 26.1 BabyPiglinModel / BabyZombifiedPiglinModel. 1.19.2's PiglinModel reloads its stored default poses at the start
     * of setupAnim; those are set to the adult defaults so its whole animation runs in the adult frame. Ears rest at
     * 26.1's baby 5 degrees (adult 30) except while dancing (26.1 dances with the adult angle); 26.1 bobs the left arm
     * up while dancing where 1.19.2 bobs both down.
     */
    public static class BabyPiglinModel extends PiglinModel<Mob> implements Rebased {
        private static final float EAR_SHIFT = (30.0F - 5.0F) * Mth.DEG_TO_RAD;
        private static boolean warned;
        private final Rebase rebase;
        private final ModelPart babyLeftEar;

        public BabyPiglinModel(ModelPart root) {
            super(root);
            this.rebase = new Rebase(this);
            this.babyLeftEar = head.getChild("left_ear");
            try {
                ObfuscationReflectionHelper.setPrivateValue(PiglinModel.class, this, PartPose.ZERO, "f_103337_");
                ObfuscationReflectionHelper.setPrivateValue(PiglinModel.class, this, PartPose.ZERO, "f_103338_");
                ObfuscationReflectionHelper.setPrivateValue(PiglinModel.class, this, Rebase.ADULT[3], "f_103333_");
                ObfuscationReflectionHelper.setPrivateValue(PiglinModel.class, this, Rebase.ADULT[2], "f_103334_");
            } catch (RuntimeException ex) {
                if (!warned) BabyMobs.LOGGER.warn("Baby Mobs: piglin default poses not reachable, baby piglin poses may be off", ex);
                warned = true;
            }
        }

        @Override
        public Rebase rebase() {
            return rebase;
        }

        @Override
        public void setupAnim(Mob e, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            rebase.toAdult(this);
            super.setupAnim(e, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            boolean dancing = e instanceof AbstractPiglin p && p.getArmPose() == PiglinArmPose.DANCING;
            if (dancing) {
                leftArm.y += 1.0F;
            } else {
                babyLeftEar.zRot += EAR_SHIFT;
                rightEar.zRot -= EAR_SHIFT;
            }
            rebase.toBaby(this);
        }
    }

}
