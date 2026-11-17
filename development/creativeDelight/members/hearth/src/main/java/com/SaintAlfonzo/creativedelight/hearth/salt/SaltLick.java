package com.SaintAlfonzo.creativedelight.hearth.salt;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/** The chain of lick stages. Stage 0 is {@code salt:salt_block}; the rest are our {@link LickableSaltBlock}s. */
public final class SaltLick {
    public static final ResourceLocation SALT_BLOCK_ID = new ResourceLocation("salt", "salt_block");
    public static final TagKey<Item> SALT_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "salt"));
    private static final ResourceLocation SALT_ITEM_ID = new ResourceLocation("salt", "salt");
    /** Vintage Delight's salt layer (block and its block item): refills a worn stage (plan_bugfix_1114 3.7). */
    public static final ResourceLocation SALT_LAYER_ID = new ResourceLocation("vintagedelight", "salt");

    private static final Map<Block, Block> NEXT = new HashMap<>();
    private static final Map<Block, Block> PREV = new HashMap<>();
    private static final Map<Block, Block> WAX = new HashMap<>();
    private static final Map<Block, Block> UNWAX = new HashMap<>();
    private static volatile boolean built;

    private SaltLick() {
    }

    /** Built on first use, after registration; the client and integrated server threads can both get here. */
    private static void build() {
        if (built) {
            return;
        }
        synchronized (SaltLick.class) {
            if (built) {
                return;
            }
            Block[] plain = {saltBlock(), HHSaltRegistry.LIGHTLY_LICKED.get(), HHSaltRegistry.WELL_LICKED.get(), HHSaltRegistry.HEAVILY_LICKED.get()};
            Block[] waxed = {HHSaltRegistry.WAXED_SALT.get(), HHSaltRegistry.WAXED_LIGHTLY_LICKED.get(), HHSaltRegistry.WAXED_WELL_LICKED.get(), HHSaltRegistry.WAXED_HEAVILY_LICKED.get()};
            for (int i = 0; i < plain.length; i++) {
                WAX.put(plain[i], waxed[i]);
                UNWAX.put(waxed[i], plain[i]);
                if (i > 0) {
                    NEXT.put(plain[i - 1], plain[i]);
                    PREV.put(plain[i], plain[i - 1]);
                    NEXT.put(waxed[i - 1], waxed[i]);
                    PREV.put(waxed[i], waxed[i - 1]);
                }
            }
            built = true;
        }
    }

    public static Block saltBlock() {
        return ForgeRegistries.BLOCKS.getValue(SALT_BLOCK_ID);
    }

    /** An unwaxed stage: licked by players and animals, in the POI type. */
    public static boolean isLickable(Block block) {
        build();
        return WAX.containsKey(block);
    }

    public static boolean isWaxed(Block block) {
        build();
        return UNWAX.containsKey(block);
    }

    @Nullable
    public static Block waxedOf(Block block) {
        build();
        return WAX.get(block);
    }

    @Nullable
    public static Block unwaxedOf(Block block) {
        build();
        return UNWAX.get(block);
    }

    /** One stage less worn, or null for stage 0. */
    @Nullable
    public static Block restored(Block block) {
        build();
        return PREV.get(block);
    }

    /** Restores one stage by hand: #forge:salt (salt, salt dust) or a salt layer block item. */
    public static boolean isSalt(ItemStack stack) {
        return stack.is(SALT_TAG) || SALT_LAYER_ID.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    /** Vintage Delight's salt layer block, or null without it. */
    @Nullable
    public static Block saltLayer() {
        Block b = ForgeRegistries.BLOCKS.getValue(SALT_LAYER_ID);
        return b instanceof SnowLayerBlock ? b : null;
    }

    /**
     * The one refill (plan_post_scratch_1115 B7): restores the lick at pos by up to {@code layers} stages, one per layer of
     * salt (heavily, well, lightly, full); a waxed stage stays waxed. Returns the layers not used (all of them when the
     * block is not a worn stage). Entry points: salt by hand ({@link SaltEvents#onRightClickBlock}), by dispenser
     * ({@link SaltEvents#registerDispenser}) and a salt layer arriving on top ({@link LickableSaltBlock#neighborChanged}).
     */
    public static int refill(Level level, BlockPos pos, BlockState state, int layers) {
        build();
        Block stage = state.getBlock();
        while (layers > 0 && PREV.get(stage) != null) {
            stage = PREV.get(stage);
            layers--;
        }
        if (stage != state.getBlock()) {
            level.setBlockAndUpdate(pos, stage.defaultBlockState());
            level.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 1.0F, 1.4F);
        }
        return layers;
    }

    /**
     * Salt on a full lick (B7, owner): one salt layer on top of it, or one more on a layer already there (below 8).
     * False when nothing could be placed (no Vintage Delight, the spot is taken, the layer is full).
     */
    public static boolean addLayerOnTop(Level level, BlockPos lick) {
        Block layer = saltLayer();
        if (layer == null) {
            return false;
        }
        BlockPos above = lick.above();
        BlockState here = level.getBlockState(above);
        if (here.is(layer)) {
            int n = here.getValue(SnowLayerBlock.LAYERS);
            if (n >= SnowLayerBlock.MAX_HEIGHT) {
                return false;
            }
            level.setBlockAndUpdate(above, here.setValue(SnowLayerBlock.LAYERS, n + 1));
        } else if (here.isAir() && layer.defaultBlockState().canSurvive(level, above)) {
            level.setBlockAndUpdate(above, layer.defaultBlockState());
        } else {
            return false;
        }
        level.playSound(null, above, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    /**
     * B5: the topmost salt layer resting on the lick (a column of full 8-layer salt blocks counts as one stack, the top
     * block is the one worn), or null when no salt layer sits on it.
     */
    @Nullable
    static BlockPos topLayer(Level level, BlockPos lick) {
        Block layer = saltLayer();
        if (layer == null) {
            return null;
        }
        BlockPos.MutableBlockPos m = lick.above().mutable();
        BlockState s = level.getBlockState(m);
        if (!s.is(layer)) {
            return null;
        }
        while (s.getValue(SnowLayerBlock.LAYERS) >= SnowLayerBlock.MAX_HEIGHT && !level.isOutsideBuildHeight(m.above())) {
            BlockState up = level.getBlockState(m.above());
            if (!up.is(layer)) {
                break;
            }
            m.move(0, 1, 0);
            s = up;
        }
        return m.immutable();
    }

    /**
     * Wears the lick one step. B5 (plan_post_scratch_1115): salt layers on top go first, one layer per lick from the top
     * down, and the lick keeps its size; with none, the block wears one stage and the last stage pops 1 salt and is gone.
     */
    public static void wear(Level level, BlockPos pos, BlockState state) {
        build();
        BlockPos top = topLayer(level, pos);
        if (top != null) {
            BlockState layer = level.getBlockState(top);
            int n = layer.getValue(SnowLayerBlock.LAYERS);
            level.setBlockAndUpdate(top, n > 1 ? layer.setValue(SnowLayerBlock.LAYERS, n - 1) : Blocks.AIR.defaultBlockState());
            return;
        }
        Block next = NEXT.get(state.getBlock());
        if (next != null) {
            level.setBlockAndUpdate(pos, next.defaultBlockState());
        } else {
            level.removeBlock(pos, false);
            Item salt = ForgeRegistries.ITEMS.getValue(SALT_ITEM_ID);
            if (salt != null) {
                Block.popResource(level, pos, new ItemStack(salt));
            }
        }
    }
}
