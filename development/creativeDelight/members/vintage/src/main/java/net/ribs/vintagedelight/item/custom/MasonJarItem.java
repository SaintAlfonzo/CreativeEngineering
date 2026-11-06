package net.ribs.vintagedelight.item.custom;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.ribs.vintagedelight.block.custom.MasonJarBlock;
import vectorwing.farmersdelight.common.utility.TextUtils;

/**
 * One item per jar (KOTS 0.3.1's merge of upstream's per-jar item classes and the separate mason_jar item). A jar is
 * placeable, 1 to 4 per block: clicking a block of the same jar adds one, sneaking places a new block. A filled jar is
 * also a pot meal: a plain click eats it and leaves the empty jar (its crafting remainder), like FD's ConsumableItem.
 */
public class MasonJarItem extends BlockItem {
    public MasonJarItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);
        if (player != null && state.is(this.getBlock()) && state.getValue(MasonJarBlock.JARS) < MasonJarBlock.MAX_JARS) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(MasonJarBlock.JARS, state.getValue(MasonJarBlock.JARS) + 1), 3);
                level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (player != null && !player.isShiftKeyDown() && this.isEdible()) {
            InteractionResult result = this.use(level, player, context.getHand()).getResult();
            return result == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : result;
        }
        return this.place(new BlockPlaceContext(context));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        ItemStack container = stack.getCraftingRemainingItem();
        if (consumer instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        }
        ItemStack result = super.finishUsingItem(stack, level, consumer);
        if (container.isEmpty()) {
            return result;
        }
        if (result.isEmpty()) {
            return container;
        }
        if (consumer instanceof Player player && !player.getAbilities().instabuild && !player.getInventory().add(container)) {
            player.drop(container, false);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.vintagedelight.mason_jar.tooltip").withStyle(ChatFormatting.GRAY).withStyle(style -> style.withItalic(true)));
        if (this.isEdible()) {
            TextUtils.addFoodEffectTooltip(stack, tooltip, 1.0F);
        }
    }
}
