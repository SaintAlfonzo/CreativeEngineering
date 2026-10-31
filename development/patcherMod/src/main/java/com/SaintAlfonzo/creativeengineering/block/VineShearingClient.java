package com.SaintAlfonzo.creativeengineering.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Draws a sheared modded vine ({@link VineShearing}) like Quark's cut vine: Quark's texture is the vanilla vine with
 * every pixel below row 11 cleared. Here the vine's own model is used and its wall quads are cut short instead, so
 * the bottom {@link #CUT} of each wall face shows nothing; ceiling faces stay whole, as the cut is where the vine
 * hangs. No texture is copied or derived, so it works for any vine, coloured or tinted (Floral Enchantment's art is
 * All Rights Reserved). The blockstate files need no change: their variant keys do not name {@code fertile}, so both
 * values already get the vine's model, and the sheared ones are wrapped here. Off with
 * {@code [vine_shearing] trimmedLook}.
 */
@Mod.EventBusSubscriber(modid = CreativeEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class VineShearingClient {

    /** Share of a wall face cut off at the bottom: Quark's cut vine clears texture rows 11 to 15. */
    static final float CUT = 5 / 16f;

    private VineShearingClient() {}

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        if (!CEConfig.VINE_SHEARING.trimmedLook.get()) return;
        Map<ResourceLocation, BakedModel> models = event.getModels();
        for (Block block : ForgeRegistries.BLOCKS) {
            if (!(block instanceof VineBlock) || !block.getStateDefinition().getProperties().contains(VineShearing.FERTILE)) {
                continue;
            }
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                if (!VineShearing.isSheared(state)) continue;
                ResourceLocation location = BlockModelShaper.stateToModelLocation(state);
                BakedModel base = models.get(location);
                if (base == null || base instanceof Trimmed) continue;
                models.put(location, new Trimmed(base));
            }
        }
    }

    /** Copy of a wall quad with its lower vertices moved up by {@link #CUT} of its height, UVs following. */
    static BakedQuad trim(BakedQuad quad) {
        if (quad.getDirection().getAxis() == Direction.Axis.Y) return quad;
        int[] v = quad.getVertices().clone();
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            float y = y(v, i);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        if (maxY - minY < 1e-4f) return quad;
        float cutY = minY + (maxY - minY) * CUT;
        int[] src = quad.getVertices();
        for (int i = 0; i < 4; i++) {
            if (y(src, i) > cutY) continue;
            // The other end of this vertex's vertical edge is the neighbour (in quad order) that sits higher.
            int prev = (i + 3) % 4, next = (i + 1) % 4;
            int top = y(src, prev) > y(src, next) ? prev : next;
            float t = (cutY - y(src, i)) / (y(src, top) - y(src, i));
            setFloat(v, i, IQuadTransformer.POSITION + 1, cutY);
            for (int k = 0; k < 2; k++) {
                int at = IQuadTransformer.UV0 + k;
                setFloat(v, i, at, lerp(getFloat(src, i, at), getFloat(src, top, at), t));
            }
        }
        return new BakedQuad(v, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
    }

    private static float y(int[] v, int vertex) {
        return getFloat(v, vertex, IQuadTransformer.POSITION + 1);
    }

    private static float getFloat(int[] v, int vertex, int offset) {
        return Float.intBitsToFloat(v[vertex * IQuadTransformer.STRIDE + offset]);
    }

    private static void setFloat(int[] v, int vertex, int offset, float value) {
        v[vertex * IQuadTransformer.STRIDE + offset] = Float.floatToRawIntBits(value);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static final class Trimmed extends BakedModelWrapper<BakedModel> {

        /** Keyed by side (null as 6) and render type (null as all): a vine's quads do not vary with position. */
        private final Map<Integer, Map<Object, List<BakedQuad>>> cache = new ConcurrentHashMap<>();
        private static final Object ALL_TYPES = new Object();

        Trimmed(BakedModel base) {
            super(base);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                ModelData data, @Nullable RenderType renderType) {
            int sideKey = side == null ? 6 : side.ordinal();
            Object typeKey = renderType == null ? ALL_TYPES : renderType;
            return cache.computeIfAbsent(sideKey, k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(typeKey, k -> {
                        List<BakedQuad> quads = super.getQuads(state, side, rand, data, renderType);
                        List<BakedQuad> out = new ArrayList<>(quads.size());
                        for (BakedQuad quad : quads) out.add(trim(quad));
                        return List.copyOf(out);
                    });
        }
    }
}
