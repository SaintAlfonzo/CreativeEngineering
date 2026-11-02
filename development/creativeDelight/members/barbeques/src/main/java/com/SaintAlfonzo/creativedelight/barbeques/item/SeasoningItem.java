package com.SaintAlfonzo.creativedelight.barbeques.item;

import com.SaintAlfonzo.creativedelight.barbeques.BBQLang;
import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQItems;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A seasoning (1.3.0 SeasoningItem): right-click a skewer on the grill or on a Farmer's Delight cutting board to season
 * it, 1 durability per use. Port rule O28: sauces only on cooked skewers, powders on raw ones too (a powder on a raw
 * skewer carries over when it is grilled).
 */
public class SeasoningItem extends Item {
    private final BBQSeasoning seasoning;

    public SeasoningItem(Properties properties, BBQSeasoning seasoning) {
        super(properties);
        this.seasoning = seasoning;
    }

    public BBQSeasoning getSeasoning() {
        return seasoning;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        list.add(seasoning.getInfo().withStyle(ChatFormatting.YELLOW));
        if (!seasoning.powder) {
            list.add(BBQLang.NO_SAUCE_RAW.get());
        }
        super.appendHoverText(stack, level, list, flag);
    }

    public static boolean isRawSkewer(ItemStack stack) {
        return stack.is(BBQItems.RAW_SKEWERS) || !stack.isEdible();
    }

    public boolean canSprinkle(ItemStack stored) {
        if (stored.isEmpty() || !(stored.getItem() instanceof BBQSkewerItem) || BBQSeasoning.get(stored) != null) {
            return false;
        }
        return seasoning.powder || !isRawSkewer(stored);
    }

    public void sprinkle(ItemStack self, Vec3 pos, ItemStack skewer, Player player, InteractionHand hand) {
        if (player.level instanceof ServerLevel sl) {
            seasoning.set(skewer);
            sl.playSound(null, pos.x, pos.y, pos.z, SoundEvents.SAND_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            Integer color = seasoning.color.getColor();
            Vector3f rgb = new Vector3f(Vec3.fromRGB24(color == null ? 0 : color));
            sl.sendParticles(new DustParticleOptions(rgb, 1.0F), pos.x, pos.y, pos.z, 8, 0.0, 0.0, 0.0, 1.0);
            self.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }
    }

    /** The FD cutting board takes seasonings (1.3.0 did this with a mixin into CuttingBoardBlock#useItemOn). */
    public static void onCuttingBoardUse(PlayerInteractEvent.RightClickBlock event) {
        ItemStack hand = event.getItemStack();
        if (!(hand.getItem() instanceof SeasoningItem item)) {
            return;
        }
        Level level = event.getLevel();
        if (!(level.getBlockEntity(event.getPos()) instanceof CuttingBoardBlockEntity board)) {
            return;
        }
        ItemStack stored = board.getStoredItem();
        if (!item.canSprinkle(stored)) {
            return;
        }
        item.sprinkle(hand, event.getHitVec().getLocation(), stored, event.getEntity(), event.getHand());
        if (!level.isClientSide) {
            board.setChanged();
            level.sendBlockUpdated(event.getPos(), board.getBlockState(), board.getBlockState(), Block.UPDATE_CLIENTS);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
    }
}
