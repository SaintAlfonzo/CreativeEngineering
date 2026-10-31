package com.SaintAlfonzo.creativeengineering.compat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Draws the botanist workbench's plant ({@link BotanistPot}). Every workbench state model is wrapped at bake time;
 * the wrapper adds the quads of the stored plant's {@code potted_*} block model, minus the quads textured like the
 * empty vanilla flower pot (pot and dirt), scaled to 2/3 and moved onto the workbench's own pot. The models come from
 * the current bake, so another mod's pot entry or a resource pack's potted model shows as it does on a vanilla pot;
 * the per-plant quad cache lives in the wrapper and goes with it on the next resource reload.
 *
 * <p>Plant tint indices are moved up by {@link #TINT_OFFSET} and the colour handler hands them to the potted block's
 * own colour handler (potted fern, grass).
 */
@Mod.EventBusSubscriber(modid = CreativeEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BotanistPotClient {

    static final int TINT_OFFSET = 1000;

    /** Plant base in the vanilla potted model (pot centre, dirt surface), in block units. */
    private static final Vector3f POT_BASE = new Vector3f(8 / 16f, 4 / 16f, 8 / 16f);
    /** The workbench pot's centre and dirt surface in its north-facing model. */
    private static final Vector3f BENCH_BASE = new Vector3f(11.5f / 16f, 15 / 16f, 10.5f / 16f);
    /** Workbench pot width over vanilla pot width (4 px over 6 px). */
    private static final float SCALE = 2 / 3f;

    private BotanistPotClient() {}

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        Block bench = BotanistPot.workbench();
        if (bench == null) return;
        Map<ResourceLocation, BakedModel> models = event.getModels();
        BakedModel emptyPot = models.get(BlockModelShaper.stateToModelLocation(Blocks.FLOWER_POT.defaultBlockState()));
        Set<ResourceLocation> potSprites = new HashSet<>();
        if (emptyPot != null) {
            RandomSource random = RandomSource.create();
            for (Direction side : sidesAndNull()) {
                for (BakedQuad quad : emptyPot.getQuads(Blocks.FLOWER_POT.defaultBlockState(), side, random, ModelData.EMPTY, null)) {
                    potSprites.add(quad.getSprite().getName());
                }
            }
        }
        for (BlockState state : bench.getStateDefinition().getPossibleStates()) {
            ResourceLocation location = BlockModelShaper.stateToModelLocation(state);
            BakedModel base = models.get(location);
            if (base == null || base instanceof Wrapped) continue;
            models.put(location, new Wrapped(base, placement(state), potSprites));
        }
    }

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        Block bench = BotanistPot.workbench();
        if (bench == null) return;
        event.register((state, level, pos, tint) -> {
            if (tint < TINT_OFFSET || level == null || pos == null) return -1;
            if (!(level.getBlockEntity(pos) instanceof BotanistPotBlockEntity pot)) return -1;
            Block potted = BotanistPot.pottedFor(pot.plant());
            if (potted == null) return -1;
            return Minecraft.getInstance().getBlockColors().getColor(potted.defaultBlockState(), level, pos, tint - TINT_OFFSET);
        }, bench);
    }

    /** Vanilla pot space to workbench space: scale about the plant base, move onto the bench pot, turn with facing. */
    private static Transformation placement(BlockState state) {
        Direction facing = state.hasProperty(HorizontalDirectionalBlock.FACING)
                ? state.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        // The blockstate file turns the north model by y = 0/90/180/270 for north/east/south/west.
        int yRot = switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
        Quaternion rotation = BlockModelRotation.by(0, yRot).getRotation().getLeftRotation();
        // p' = C + R(BENCH_BASE - C + s * (p - POT_BASE)) = R(s * p) + [C + R(BENCH_BASE - C - s * POT_BASE)]
        Vector3f offset = BENCH_BASE.copy();
        offset.sub(new Vector3f(0.5f, 0.5f, 0.5f));
        Vector3f scaledBase = POT_BASE.copy();
        scaledBase.mul(SCALE);
        offset.sub(scaledBase);
        offset.transform(rotation);
        offset.add(0.5f, 0.5f, 0.5f);
        return new Transformation(offset, rotation, new Vector3f(SCALE, SCALE, SCALE), null);
    }

    private static Direction[] sidesAndNull() {
        Direction[] sides = new Direction[7];
        System.arraycopy(Direction.values(), 0, sides, 0, 6);
        return sides;
    }

    private static final class Wrapped extends BakedModelWrapper<BakedModel> {

        private final IQuadTransformer transformer;
        private final Matrix4f matrix;
        private final Set<ResourceLocation> potSprites;
        private final Map<Block, Map<RenderType, List<BakedQuad>>> byType = new ConcurrentHashMap<>();
        private final Map<Block, List<BakedQuad>> allTypes = new ConcurrentHashMap<>();

        Wrapped(BakedModel base, Transformation placement, Set<ResourceLocation> potSprites) {
            super(base);
            this.transformer = QuadTransformers.applying(placement);
            this.matrix = placement.getMatrix();
            this.potSprites = potSprites;
        }

        @Override
        public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
            ChunkRenderTypeSet own = super.getRenderTypes(state, rand, data);
            Block potted = data.get(BotanistPotBlockEntity.POTTED);
            if (potted == null) return own;
            return ChunkRenderTypeSet.union(own, plantModel(potted).getRenderTypes(potted.defaultBlockState(), rand, ModelData.EMPTY));
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                ModelData data, @Nullable RenderType renderType) {
            List<BakedQuad> own = super.getQuads(state, side, rand, data, renderType);
            Block potted = side == null ? data.get(BotanistPotBlockEntity.POTTED) : null;
            if (potted == null) return own;
            List<BakedQuad> plant = plantQuads(potted, renderType);
            if (plant.isEmpty()) return own;
            List<BakedQuad> out = new ArrayList<>(own.size() + plant.size());
            out.addAll(own);
            out.addAll(plant);
            return out;
        }

        private static BakedModel plantModel(Block potted) {
            return Minecraft.getInstance().getBlockRenderer().getBlockModel(potted.defaultBlockState());
        }

        private List<BakedQuad> plantQuads(Block potted, @Nullable RenderType renderType) {
            // ConcurrentHashMap takes no null keys: getQuads(..., null) (every render type) has its own cache.
            if (renderType == null) return allTypes.computeIfAbsent(potted, k -> buildQuads(k, null));
            return byType.computeIfAbsent(potted, k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(renderType, type -> buildQuads(potted, type));
        }

        private List<BakedQuad> buildQuads(Block potted, @Nullable RenderType renderType) {
            BlockState pottedState = potted.defaultBlockState();
            BakedModel model = plantModel(potted);
            RandomSource random = RandomSource.create(42L);
            if (renderType != null && !model.getRenderTypes(pottedState, random, ModelData.EMPTY).contains(renderType)) {
                return List.of();
            }
            List<BakedQuad> out = new ArrayList<>();
            for (Direction face : sidesAndNull()) {
                random.setSeed(42L);
                for (BakedQuad quad : model.getQuads(pottedState, face, random, ModelData.EMPTY, renderType)) {
                    if (potSprites.contains(quad.getSprite().getName())) continue;
                    BakedQuad moved = transformer.process(quad);
                    int tint = moved.getTintIndex() >= 0 ? moved.getTintIndex() + TINT_OFFSET : -1;
                    out.add(new BakedQuad(moved.getVertices(), tint, Direction.rotate(matrix, quad.getDirection()),
                            moved.getSprite(), moved.isShade()));
                }
            }
            return List.copyOf(out);
        }
    }
}
