package com.SaintAlfonzo.creativeengineering.block;

import java.util.Map;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Biome-tinted moss block and moss carpet (CEConfig [moss]).
 *
 * <p>Vanilla moss has no tint index, so a colour handler alone does nothing. With {@code biomeTint} on, every
 * moss block / carpet state and item model is swapped at bake time for a copy with tint index 0 on a grayscale
 * texture ({@code moss_block_tinted.png}, the vanilla texture's luminance). With it off nothing is swapped and
 * moss is the untouched vanilla model.
 *
 * <p>The tint is {@link #MOSS_BASE} (the colour that turns the grayscale texture back into vanilla moss) shifted
 * per channel by the biome's grass colour relative to Lush Caves grass, so moss in Lush Caves stays vanilla green
 * and elsewhere moves as far as grass does. Items have no biome and get the base colour.
 */
@Mod.EventBusSubscriber(modid = CreativeEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MossTint {

    /** Least-squares fit of vanilla moss_block.png against the grayscale copy. */
    private static final int MOSS_BASE = 0x738E3A;

    private static final ResourceLocation BLOCK_MODEL = new ResourceLocation(CreativeEngineering.MODID, "block/moss_block_tinted");
    private static final ResourceLocation CARPET_MODEL = new ResourceLocation(CreativeEngineering.MODID, "block/moss_carpet_tinted");

    private MossTint() {}

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        event.register(BLOCK_MODEL);
        event.register(CARPET_MODEL);
    }

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        if (!CEConfig.MOSS.biomeTint.get()) return;
        Map<ResourceLocation, BakedModel> models = event.getModels();
        BakedModel missing = event.getModelManager().getMissingModel();
        swap(models, missing, Blocks.MOSS_BLOCK, models.get(BLOCK_MODEL));
        swap(models, missing, Blocks.MOSS_CARPET, models.get(CARPET_MODEL));
    }

    private static void swap(Map<ResourceLocation, BakedModel> models, BakedModel missing, Block block, BakedModel tinted) {
        if (tinted == null || tinted == missing) return;
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            models.put(BlockModelShaper.stateToModelLocation(state), tinted);
        }
        models.put(new ModelResourceLocation(ForgeRegistries.ITEMS.getKey(block.asItem()), "inventory"), tinted);
    }

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> {
            if (tint != 0) return -1;
            if (level == null || pos == null) return MOSS_BASE;
            return shift(BiomeColors.getAverageGrassColor(level, pos));
        }, Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET);
    }

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> tint == 0 ? MOSS_BASE : -1, Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET);
    }

    /** {@link #MOSS_BASE} times (grass / Lush Caves grass) per channel, scaled by tintStrength. */
    static int shift(int grass) {
        double strength = CEConfig.MOSS.tintStrength.get();
        int ref = GrassColor.get(0.5, 0.5);
        int out = 0;
        for (int shiftBits = 16; shiftBits >= 0; shiftBits -= 8) {
            int base = (MOSS_BASE >> shiftBits) & 0xFF;
            int g = (grass >> shiftBits) & 0xFF;
            int r = Math.max(1, (ref >> shiftBits) & 0xFF);
            double factor = 1.0 + strength * ((double) g / r - 1.0);
            int c = (int) Math.round(Math.min(255.0, Math.max(0.0, base * factor)));
            out |= c << shiftBits;
        }
        return out;
    }
}
