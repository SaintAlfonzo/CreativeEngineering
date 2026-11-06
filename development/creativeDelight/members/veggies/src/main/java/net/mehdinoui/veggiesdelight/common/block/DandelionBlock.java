package net.mehdinoui.veggiesdelight.common.block;

import net.mehdinoui.veggiesdelight.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.mehdinoui.veggiesdelight.common.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

// Mature Dandelion: a flower (Luck in suspicious stew) that pops another Mature Dandelion item on every bone meal.
// Shears or a knife used on it turn it back into a plain Dandelion (50% to drop a Dandelion, 2-3 Dandelion Leaves).
// The plain Dandelion matures on Rich Soil / Fertile Soil through patcherMod (crops/DandelionMaturing).
public class DandelionBlock extends FlowerBlock implements BonemealableBlock {
    private static final TagKey<Item> SHEARS = ItemTags.create(new ResourceLocation("forge", "shears"));
    private static final TagKey<Item> KNIVES = ItemTags.create(new ResourceLocation("forge", "tools/knives"));

    public DandelionBlock(Properties properties) {
        super(() -> MobEffects.LUCK, 5, properties);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        popResource(level, pos, new ItemStack(ModBlocks.MATURE_DANDELION.get()));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack tool = player.getItemInHand(hand);
        if (!tool.is(SHEARS) && !tool.is(KNIVES)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.setBlock(pos, Blocks.DANDELION.defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (level.random.nextBoolean()) {
                popResource(level, pos, new ItemStack(Items.DANDELION));
            }
            popResource(level, pos, new ItemStack(ModItems.DANDELION_LEAF.get(), 2 + level.random.nextInt(2)));
            tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
