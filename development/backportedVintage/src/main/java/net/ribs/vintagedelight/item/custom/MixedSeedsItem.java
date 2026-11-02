package net.ribs.vintagedelight.item.custom;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.ribs.vintagedelight.item.ModTags;

/**
 * Mixed Seeds (pack addition; upstream ships only the texture). Used on a block, it tries the seed kinds of
 * #vintagedelight:mixed_seeds_plants in a random order and plants the first one that fits, as if that seed had been
 * used there; one mixed seed is spent. The pick is made again on every use. The tag is filled by the pack
 * (Creative Delight vintagedelight/tags.js); the jar ships it empty, and an empty tag plants nothing.
 */
public class MixedSeedsItem extends Item {
    public MixedSeedsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        List<Item> kinds = new ArrayList<>();
        for (Holder<Item> holder : Registry.ITEM.getTagOrEmpty(ModTags.MIXED_SEEDS_PLANTS)) {
            if (holder.value() != this) kinds.add(holder.value());
        }
        if (kinds.isEmpty()) return InteractionResult.PASS;
        RandomSource random = level.getRandom();
        for (int i = kinds.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Item swap = kinds.get(i);
            kinds.set(i, kinds.get(j));
            kinds.set(j, swap);
        }
        Player player = context.getPlayer();
        BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
                context.getClickedPos(), context.isInside());
        for (Item kind : kinds) {
            ItemStack seed = new ItemStack(kind);
            InteractionResult result = kind.useOn(new BlockPlaceContext(level, player, context.getHand(), seed, hit));
            if (result.consumesAction()) {
                if (player == null || !player.getAbilities().instabuild) context.getItemInHand().shrink(1);
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.vintagedelight.mixed_seeds.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
