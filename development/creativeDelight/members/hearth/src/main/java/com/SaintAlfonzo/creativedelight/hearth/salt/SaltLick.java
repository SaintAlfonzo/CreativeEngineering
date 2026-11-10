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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/** The chain of lick stages. Stage 0 is {@code salt:salt_block}; the rest are our {@link LickableSaltBlock}s. */
public final class SaltLick {
    public static final ResourceLocation SALT_BLOCK_ID = new ResourceLocation("salt", "salt_block");
    public static final TagKey<Item> SALT_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "salt"));
    private static final ResourceLocation SALT_ITEM_ID = new ResourceLocation("salt", "salt");

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

    public static boolean isSalt(ItemStack stack) {
        return stack.is(SALT_TAG);
    }

    /** Wears the block one stage; the last stage pops 1 salt and is gone. */
    public static void wear(Level level, BlockPos pos, BlockState state) {
        build();
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
