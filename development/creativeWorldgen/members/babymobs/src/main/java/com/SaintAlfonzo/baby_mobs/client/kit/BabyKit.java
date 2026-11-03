package com.SaintAlfonzo.baby_mobs.client.kit;

import com.SaintAlfonzo.baby_mobs.BabyMobs;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * One remodelled baby (26.1 "Tiny Takeover") for one or more entity types: the baby body model, the texture choice and
 * the layer swaps. {@link BabyRender} applies it to whatever {@code LivingEntityRenderer} draws the mob (vanilla's or a
 * mod's subclass, e.g. Quark's variant cows) as long as that renderer's model is an instance of {@link #adultModel}.
 *
 * <p>While a baby renders: the renderer's model is the baked baby model, the young flag stays false (the baby mesh is
 * already baby-sized; vanilla 1.19.2 would shrink it again), {@link #texture} replaces the body texture, and layers
 * drawn through {@code RenderLayer.coloredCutoutModelCopyLayerRender} / {@code renderColoredCutoutModel} get their
 * texture (and, for the copy variant, their model) swapped by the adult layer texture key ({@link #layer}).
 */
public final class BabyKit<T extends LivingEntity> {

    /** A layer swap, keyed by the adult layer texture: the baby texture and, when the layer has its own model, the baby one. */
    public record LayerSwap(ResourceLocation texture, @Nullable ModelLayerLocation layer,
                            @Nullable Function<ModelPart, ? extends EntityModel<?>> factory) {}

    final String name;
    final Class<?> adultModel;
    final ModelLayerLocation layer;
    final Supplier<LayerDefinition> definition;
    final Function<ModelPart, ? extends EntityModel<?>> factory;
    BiFunction<T, ResourceLocation, ResourceLocation> texture;
    final Set<String> poolNamespaces = new HashSet<>();
    final Map<ResourceLocation, LayerSwap> layers = new HashMap<>();
    final Map<String, LayerSwap> layerPrefixes = new java.util.LinkedHashMap<>();
    final Map<ModelLayerLocation, Supplier<LayerDefinition>> extraDefinitions = new HashMap<>();
    @Nullable BiConsumer<T, PoseStack> pose;

    private final Map<ModelLayerLocation, EntityModel<?>> baked = new HashMap<>();

    private BabyKit(String name, Class<?> adultModel, Supplier<LayerDefinition> definition,
                    Function<ModelPart, ? extends EntityModel<?>> factory, Function<T, ResourceLocation> texture) {
        this.name = name;
        this.adultModel = adultModel;
        this.layer = location(name, "baby");
        this.definition = definition;
        this.factory = factory;
        this.texture = (e, adult) -> texture.apply(e);
    }

    /**
     * @param name       layer id under {@code baby_mobs:<name>}, e.g. "cow"
     * @param adultModel the 1.19.2 model class the renderer must use (instanceof check), e.g. {@code CowModel.class};
     *                   the baby model {@code factory} builds must be an instance of it too
     * @param definition the 26.1 baby {@code createBodyLayer}, ported
     * @param factory    builds the baby model from the baked root
     * @param texture    the baby body texture for this entity
     */
    public static <T extends LivingEntity> BabyKit<T> of(String name, Class<?> adultModel, Supplier<LayerDefinition> definition,
                                                         Function<ModelPart, ? extends EntityModel<?>> factory,
                                                         Function<T, ResourceLocation> texture) {
        return new BabyKit<>(name, adultModel, definition, factory, texture);
    }

    /** {@code baby_mobs:<name>#<layer>}. */
    public static ModelLayerLocation location(String name, String layer) {
        return new ModelLayerLocation(new ResourceLocation(BabyMobs.ID, name), layer);
    }

    /** {@code baby_mobs:textures/entity/<path>}. */
    public static ResourceLocation tex(String path) {
        return new ResourceLocation(BabyMobs.ID, "textures/entity/" + path);
    }

    /** A layer that draws the parent model with its own texture (collars, markings, villager clothes): texture only. */
    public BabyKit<T> layerTexture(ResourceLocation adult, ResourceLocation baby) {
        layers.put(adult, new LayerSwap(baby, null, null));
        return this;
    }

    /**
     * Fallback texture swap for any layer texture whose path (any namespace) starts with {@code pathPrefix} and has no
     * exact {@link #layerTexture} entry, e.g. villager biome types added by other mods.
     */
    public BabyKit<T> layerTexturePrefix(String pathPrefix, ResourceLocation baby) {
        layerPrefixes.put(pathPrefix, new LayerSwap(baby, null, null));
        return this;
    }

    /** Any layer texture whose path starts with {@code pathPrefix} gets the swap already set for {@code adultKey}. */
    public BabyKit<T> layerPrefixAs(String pathPrefix, ResourceLocation adultKey) {
        LayerSwap swap = layers.get(adultKey);
        if (swap == null) throw new IllegalStateException(name + ": no layer swap for " + adultKey);
        layerPrefixes.put(pathPrefix, swap);
        return this;
    }

    @Nullable
    LayerSwap layerSwap(ResourceLocation adult) {
        LayerSwap swap = layers.get(adult);
        if (swap != null || layerPrefixes.isEmpty()) return swap;
        for (Map.Entry<String, LayerSwap> e : layerPrefixes.entrySet()) {
            if (adult.getPath().startsWith(e.getKey())) return e.getValue();
        }
        return null;
    }

    public String name() {
        return name;
    }

    /**
     * A layer with its own model (sheep wool, cat collar, drowned outer layer): baby texture and baby model, the latter
     * registered under {@code baby_mobs:<name>#<layerName>}.
     */
    public BabyKit<T> layerModel(ResourceLocation adult, ResourceLocation baby, String layerName,
                                 Supplier<LayerDefinition> definition, Function<ModelPart, ? extends EntityModel<?>> factory) {
        ModelLayerLocation loc = location(name, layerName);
        extraDefinitions.put(loc, definition);
        layers.put(adult, new LayerSwap(baby, loc, factory));
        return this;
    }

    /** An extra layer definition the family's own code bakes through {@link #bake} (e.g. baby armour). */
    public ModelLayerLocation extraLayer(String layerName, Supplier<LayerDefinition> definition) {
        ModelLayerLocation loc = location(name, layerName);
        extraDefinitions.put(loc, definition);
        return loc;
    }

    /** Extra pose transform for the baby, applied after the renderer's own {@code scale()} (26.1 baby-only offsets). */
    public BabyKit<T> pose(BiConsumer<T, PoseStack> pose) {
        this.pose = pose;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <M extends EntityModel<?>> M bake(ModelLayerLocation loc, Function<ModelPart, M> f) {
        return (M) baked.computeIfAbsent(loc, l -> f.apply(Minecraft.getInstance().getEntityModels().bakeLayer(l)));
    }

    /**
     * Baby texture from the entity and the renderer's adult texture, for kits that map variant coats (More Mob Variants
     * wolves). Replaces the plain texture function given to {@link #of}.
     */
    public BabyKit<T> textureFrom(BiFunction<T, ResourceLocation, ResourceLocation> texture) {
        this.texture = texture;
        return this;
    }

    /**
     * Coats from these namespaces count as the mob's regular pool, not as variants, even with variantBabies =
     * OLD_MODELS (cats: More Mob Variants coats use the cat's vanilla coat baby).
     */
    public BabyKit<T> poolNamespace(String namespace) {
        poolNamespaces.add(namespace);
        return this;
    }

    /**
     * The baby texture, or null to keep the 1.19.2 baby: with OLD_MODELS a coat from another mod's namespace (not in
     * {@link #poolNamespaces}) is a variant and keeps the old baby; otherwise the kit picks (null = no baby texture).
     */
    @SuppressWarnings("unchecked")
    @Nullable
    ResourceLocation babyTextureFor(LivingEntity entity, @Nullable ResourceLocation adult, boolean newModelsForVariants) {
        if (!newModelsForVariants && adult != null && !"minecraft".equals(adult.getNamespace())
                && !poolNamespaces.contains(adult.getNamespace())) {
            return null;
        }
        return texture.apply((T) entity, adult);
    }

    @SuppressWarnings("unchecked")
    void poseFor(LivingEntity entity, PoseStack stack) {
        if (pose != null) pose.accept((T) entity, stack);
    }

    EntityModel<?> babyModel() {
        return bake(layer, factory::apply);
    }

    @Nullable
    EntityModel<?> layerModel(LayerSwap swap) {
        return swap.layer() == null ? null : bake(swap.layer(), p -> swap.factory().apply(p));
    }

    @Override
    public String toString() {
        return "BabyKit[" + name + "]";
    }
}
