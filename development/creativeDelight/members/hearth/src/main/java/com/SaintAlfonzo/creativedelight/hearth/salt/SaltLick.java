package com.SaintAlfonzo.creativedelight.hearth.salt;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The chain of lick stages. Stage 0 is a placed (not generated) Rock Salt Block ({@link PlacedRockSaltBlock}, swapped in when The
 * Salt's {@code salt:raw_rock_salt_block} is placed after world generation, plan_post_scratch_1115 G2b, G11); generated Rock Salt Blocks are
 * no lick. The rest are our {@link LickableSaltBlock}s. 4 stages = 4 Rock Salt: one wear is one stage, one Rock Salt (by hand
 * or dispenser) refills one stage. Salt layers no longer refill a lick and are not eaten first (G6-10).
 */
public final class SaltLick {
    public static final ResourceLocation SALT_BLOCK_ID = new ResourceLocation("salt", "raw_rock_salt_block");
    /** Rock Salt, the item that refills a worn stage (G2b). */
    public static final ResourceLocation ROCK_SALT_ID = new ResourceLocation("salt", "raw_rock_salt");
    /** The pack's salt (Vintage Delight's salt dust): the last wear drops 1 (G6-10c). The Salt's own salt without it. */
    private static final ResourceLocation SALT_DUST_ID = new ResourceLocation("vintagedelight", "salt_dust");
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
            Block[] plain = {HHSaltRegistry.PLACED_ROCK_SALT.get(), HHSaltRegistry.LIGHTLY_LICKED.get(), HHSaltRegistry.WELL_LICKED.get(), HHSaltRegistry.HEAVILY_LICKED.get()};
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

    /** The Salt's Rock Salt Block: placed after world generation it becomes stage 0; generated ones are no lick. */
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

    /** Restores one stage by hand or dispenser: Rock Salt only (G2b). */
    public static boolean isSalt(ItemStack stack) {
        return !stack.isEmpty() && ROCK_SALT_ID.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    /**
     * The one refill: restores the lick at pos by up to {@code count} stages, one per Rock Salt (heavily, well, lightly,
     * full); a waxed stage stays waxed. Returns the count not used (all of it when the block is not a worn stage).
     * Entry points: Rock Salt by hand ({@link SaltEvents#onRightClickBlock}) and by dispenser
     * ({@link SaltEvents#registerDispenser}).
     */
    public static int refill(Level level, BlockPos pos, BlockState state, int count) {
        build();
        Block stage = state.getBlock();
        while (count > 0 && PREV.get(stage) != null) {
            stage = PREV.get(stage);
            count--;
        }
        if (stage != state.getBlock()) {
            level.setBlockAndUpdate(pos, stage.defaultBlockState());
            level.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 1.0F, 1.4F);
        }
        return count;
    }

    /** Wears the lick one stage; the last stage is gone and drops 1 Salt (G6-10c). */
    public static void wear(Level level, BlockPos pos, BlockState state) {
        build();
        Block next = NEXT.get(state.getBlock());
        if (next != null) {
            level.setBlockAndUpdate(pos, next.defaultBlockState());
        } else {
            level.removeBlock(pos, false);
            Item salt = ForgeRegistries.ITEMS.getValue(SALT_DUST_ID);
            if (salt == null || salt == Items.AIR) {
                salt = ForgeRegistries.ITEMS.getValue(SALT_ITEM_ID);
            }
            if (salt != null && salt != Items.AIR) {
                Block.popResource(level, pos, new ItemStack(salt));
            }
        }
    }
}
