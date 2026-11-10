package com.SaintAlfonzo.creativedelight.hearth.salt;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Player use of a salt lick. Goes through events rather than the block because stage 0 is The Salt's block: lick with
 * both hands empty, honeycomb to wax, an axe to unwax, salt to restore one stage (by hand and by dispenser).
 */
public final class SaltEvents {
    public static final TagKey<EntityType<?>> SALT_LICKERS = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation("creativedelight", "salt_lickers"));
    private static final ResourceLocation SALT_ITEM_ID = new ResourceLocation("salt", "salt");

    private SaltEvents() {
    }

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof PathfinderMob mob && mob.getType().is(SALT_LICKERS)) {
            mob.goalSelector.addGoal(4, new StayNearSaltGoal(mob));
        }
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        boolean lickable = SaltLick.isLickable(block);
        if (!lickable && !SaltLick.isWaxed(block)) {
            return;
        }
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack stack = event.getItemStack();

        if (lickable && stack.is(Items.HONEYCOMB)) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, SaltLick.waxedOf(block).defaultBlockState());
                level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
                consume(player, stack);
            }
            succeed(event, level);
        } else if (!lickable && stack.canPerformAction(ToolActions.AXE_WAX_OFF)) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, SaltLick.unwaxedOf(block).defaultBlockState());
                level.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            succeed(event, level);
        } else if (SaltLick.isSalt(stack) && SaltLick.restored(block) != null) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, SaltLick.restored(block).defaultBlockState());
                level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 1.4F);
                consume(player, stack);
            }
            succeed(event, level);
        } else if (lickable && hand == InteractionHand.MAIN_HAND && stack.isEmpty() && player.getOffhandItem().isEmpty() && SaltConfig.PLAYER_LICKING.get()) {
            if (!level.isClientSide()) {
                level.playSound(null, pos, HHSaltRegistry.LICK.get(), SoundSource.BLOCKS, 0.8F, 0.8F + level.random.nextFloat() * 0.7F);
                if (level.random.nextFloat() < SaltConfig.PLAYER_LICK_CHANCE.get()) {
                    SaltLick.wear(level, pos, state);
                }
            }
            succeed(event, level);
        }
    }

    private static void succeed(PlayerInteractEvent.RightClickBlock event, Level level) {
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
        event.setCanceled(true);
    }

    private static void consume(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    /** Salt in a dispenser facing a worn, unwaxed stage restores it; any other target falls through to a normal dispense. */
    public static void registerDispenser() {
        Item salt = ForgeRegistries.ITEMS.getValue(SALT_ITEM_ID);
        if (salt == null || salt == Items.AIR) {
            return;
        }
        DispenserBlock.registerBehavior(salt, new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos target = source.getPos().relative(facing);
                Level level = source.getLevel();
                Block block = level.getBlockState(target).getBlock();
                Block restored = SaltLick.restored(block);
                if (restored != null && SaltLick.isLickable(block)) {
                    level.setBlockAndUpdate(target, restored.defaultBlockState());
                    level.playSound(null, target, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 1.4F);
                    stack.shrink(1);
                    return stack;
                }
                return super.execute(source, stack);
            }
        });
    }

    /** The hint line (S6): every unwaxed stage, stage 0 included. Fires on the client only. */
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof BlockItem item && SaltLick.isLickable(item.getBlock())) {
            event.getToolTip().add(Component.translatable(HearthAndHarvestMod.MODID + ".salt_lick.hint").withStyle(ChatFormatting.GRAY));
        }
    }
}
