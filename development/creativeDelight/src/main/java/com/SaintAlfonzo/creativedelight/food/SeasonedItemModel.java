package com.SaintAlfonzo.creativedelight.food;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.SaintAlfonzo.creativedelight.CreativeDelight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Seasoned food icons: the food's own model with one overlay per seasoning on top, salt first (Salt's
 * {@code salt:item/salted_overlay} when the stack carries {@code Salted}), then each {@link Seasonings} id in NBT order,
 * overlay model {@code <ns>:item/seasoned/<path>} (textures from {@code textureswapper/seasoning/make_seasoning.py}). A
 * seasoning without an overlay model draws none. Hooked into {@code ItemRenderer.getModel} by
 * {@code mixin.SeasonedItemModelMixin}, so inventory, hand, dropped items and JEI all show it.
 *
 * <p>Salt's own overlay must be off ({@code salt-client.toml SaltingOverlayEnabled = false}): Salt caches its layered
 * model per item, so with it on, the first seasoned stack it saw would be drawn for every salted stack of that item.
 */
@Mod.EventBusSubscriber(modid = CreativeDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SeasonedItemModel implements BakedModel {

    private static final ResourceLocation SALT_OVERLAY = new ResourceLocation("salt", "item/salted_overlay");

    private record Key(BakedModel base, List<ResourceLocation> layers) {
    }

    private static final Map<Key, BakedModel> CACHE = new ConcurrentHashMap<>();

    private final BakedModel base;
    private final List<BakedModel> layers;

    private SeasonedItemModel(BakedModel base, List<BakedModel> overlays) {
        this.base = base;
        this.layers = new ArrayList<>(overlays.size() + 1);
        this.layers.add(base);
        this.layers.addAll(overlays);
    }

    public static ResourceLocation overlayModel(ResourceLocation seasoning) {
        return new ResourceLocation(seasoning.getNamespace(), "item/seasoned/" + seasoning.getPath());
    }

    /** {@code base} with the stack's seasoning overlays on top, or {@code base} itself when it has none. */
    public static BakedModel resolve(ItemStack stack, BakedModel base) {
        CompoundTag tag = stack.getTag();
        if (base == null || tag == null || base.isCustomRenderer()) {
            return base;
        }
        List<ResourceLocation> ids = new ArrayList<>();
        if (tag.contains("Salted")) {
            ids.add(SALT_OVERLAY);
        }
        for (ResourceLocation id : Seasonings.of(stack)) {
            ids.add(overlayModel(id));
        }
        if (ids.isEmpty()) {
            return base;
        }
        return CACHE.computeIfAbsent(new Key(base, List.copyOf(ids)), key -> {
            ModelManager manager = Minecraft.getInstance().getModelManager();
            BakedModel missing = manager.getMissingModel();
            List<BakedModel> overlays = new ArrayList<>();
            for (ResourceLocation id : key.layers()) {
                BakedModel overlay = manager.getModel(id);
                if (overlay != null && overlay != missing) {
                    overlays.add(overlay);
                }
            }
            return overlays.isEmpty() ? key.base() : new SeasonedItemModel(key.base(), overlays);
        });
    }

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        event.register(SALT_OVERLAY);
        for (ResourceLocation id : Seasonings.ids()) {
            event.register(overlayModel(id));
        }
    }

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        CACHE.clear();
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
        List<BakedQuad> quads = new ArrayList<>();
        for (BakedModel layer : layers) {
            quads.addAll(layer.getQuads(state, side, rand));
        }
        return quads;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>();
        for (BakedModel layer : layers) {
            quads.addAll(layer.getQuads(state, side, rand, data, renderType));
        }
        return quads;
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
        return base.getRenderTypes(stack, fabulous);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return base.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return base.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return base.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return base.isCustomRenderer();
    }

    @SuppressWarnings("deprecation")
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return base.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemTransforms getTransforms() {
        return base.getTransforms();
    }
}
