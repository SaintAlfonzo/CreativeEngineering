package com.SaintAlfonzo.baby_mobs.client.kit;

import com.SaintAlfonzo.baby_mobs.BMConfig;
import com.SaintAlfonzo.baby_mobs.BabyMobs;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * The kit registry and the per-render context the renderer and layer mixins read. Render thread only. A stack, not a
 * single slot, so an entity rendered inside another's render (a passenger in an inventory preview, say) keeps its own.
 */
public final class BabyRender {

    /** What is rendering right now: the kit (null = not a remodelled baby) and the entity. */
    public record Frame(@Nullable BabyKit<?> kit, LivingEntity entity, @Nullable ResourceLocation babyTexture) {}

    private static final Map<EntityType<?>, BabyKit<?>> KITS = new HashMap<>();
    /** Kits for other mods' entity types, by id (their types don't exist yet when the families register). */
    private static final Map<ResourceLocation, BabyKit<?>> KITS_BY_ID = new HashMap<>();
    /** Per type: the kit in effect (null = none), resolved once per config list. */
    private static final Map<EntityType<?>, java.util.Optional<BabyKit<?>>> RESOLVED = new HashMap<>();
    private static Object resolvedFor;
    private static final Deque<Frame> STACK = new ArrayDeque<>();
    private static final Set<String> WARNED = new HashSet<>();

    private BabyRender() {}

    public static <T extends LivingEntity> void register(EntityType<? extends T> type, BabyKit<T> kit) {
        KITS.put(type, kit);
    }

    /** A kit for another mod's entity type (e.g. Cozy's Improved Cats), matched by registry id at render time. */
    public static void register(ResourceLocation typeId, BabyKit<?> kit) {
        KITS_BY_ID.put(typeId, kit);
    }

    static boolean newModelsForVariants() {
        return BMConfig.VARIANT_BABIES.get() == BMConfig.VariantBabies.NEW_MODELS;
    }

    /** The kit for this type, or null: no kit, or (OLD_MODELS) the type is in {@code keepVanillaBabies}. */
    @Nullable
    private static BabyKit<?> kitFor(EntityType<?> type) {
        boolean all = newModelsForVariants();
        java.util.List<? extends String> keep = all ? java.util.List.of() : BMConfig.KEEP_VANILLA_BABIES.get();
        if (resolvedFor != keep) {
            RESOLVED.clear();
            resolvedFor = keep;
        }
        return RESOLVED.computeIfAbsent(type, t -> {
            ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(t);
            if (id != null && keep.contains(id.toString())) return java.util.Optional.empty();
            BabyKit<?> kit = KITS.get(t);
            if (kit == null && id != null) kit = KITS_BY_ID.get(id);
            return java.util.Optional.ofNullable(kit);
        }).orElse(null);
    }

    /** Every layer definition the kits need, for {@code RegisterLayerDefinitions}. */
    public static void definitions(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> sink) {
        Set<BabyKit<?>> seen = new HashSet<>(KITS.values());
        seen.addAll(KITS_BY_ID.values());
        for (BabyKit<?> kit : seen) {
            sink.accept(kit.layer, kit.definition);
            kit.extraDefinitions.forEach(sink);
        }
    }

    /**
     * Render HEAD: pushes a frame and returns the baby model to swap in, or null to leave the renderer alone (not a
     * baby, no kit, switched off, the renderer uses a model the kit wasn't made for, or a variant coat that keeps the
     * 1.19.2 baby). {@code adultTexture} supplies the renderer's own texture (with other mods' variant hooks applied).
     */
    @Nullable
    public static EntityModel<?> begin(LivingEntity entity, EntityModel<?> current, Supplier<ResourceLocation> adultTexture) {
        BabyKit<?> kit = kitFor(entity.getType());
        if (kit == null || !entity.isBaby() || !BMConfig.BABY_MODELS.get() || !kit.adultModel.isInstance(current)) {
            if (kit != null && entity.isBaby() && !kit.adultModel.isInstance(current) && WARNED.add(kit.name + current.getClass().getName())) {
                BabyMobs.LOGGER.info("Baby Mobs: {} renders with {}, not {}; keeping its own baby", kit, current.getClass().getName(), kit.adultModel.getName());
            }
            STACK.push(new Frame(null, entity, null));
            return null;
        }
        ResourceLocation baby = kit.babyTextureFor(entity, adultTexture.get(), newModelsForVariants());
        if (baby == null) { // a variant coat that keeps the 1.19.2 baby
            STACK.push(new Frame(null, entity, null));
            return null;
        }
        STACK.push(new Frame(kit, entity, baby));
        try {
            return kit.babyModel();
        } catch (RuntimeException e) {
            if (WARNED.add(kit.name + "#bake")) BabyMobs.LOGGER.error("Baby Mobs: {} model failed, using vanilla's baby", kit, e);
            STACK.pop();
            STACK.push(new Frame(null, entity, null));
            return null;
        }
    }

    public static void end() {
        if (!STACK.isEmpty()) STACK.pop();
    }

    @Nullable
    public static Frame current() {
        return STACK.peek();
    }

    /** True while a remodelled baby renders (its model must not get the young flag). */
    public static boolean active() {
        Frame f = STACK.peek();
        return f != null && f.kit() != null;
    }

    public static ResourceLocation texture(ResourceLocation adult) {
        Frame f = STACK.peek();
        return f == null || f.kit() == null || f.babyTexture() == null ? adult : f.babyTexture();
    }

    public static void pose(PoseStack pose) {
        Frame f = STACK.peek();
        if (f != null && f.kit() != null) f.kit().poseFor(f.entity(), pose);
    }

    @Nullable
    public static BabyKit.LayerSwap layerSwap(ResourceLocation adultTexture) {
        Frame f = STACK.peek();
        return f == null || f.kit() == null ? null : f.kit().layerSwap(adultTexture);
    }

    @Nullable
    public static EntityModel<?> layerModel(BabyKit.LayerSwap swap) {
        Frame f = STACK.peek();
        return f == null || f.kit() == null ? null : f.kit().layerModel(swap);
    }

    public static ResourceLocation layerTexture(ResourceLocation adult) {
        BabyKit.LayerSwap swap = layerSwap(adult);
        return swap != null ? swap.texture() : adult;
    }
}
