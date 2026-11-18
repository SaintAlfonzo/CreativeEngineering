package com.SaintAlfonzo.creativedelight.hearth.salt;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Player use of a salt lick, and the placement that makes a Rock Salt Block a lick (stage 0, {@link PlacedRockSaltBlock}). Lick with
 * both hands empty, honeycomb to wax, an axe to unwax, Rock Salt to restore one stage (by hand and by dispenser;
 * plan_post_scratch_1115 G2b).
 */
public final class SaltEvents {
    public static final TagKey<EntityType<?>> SALT_LICKERS = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation("creativedelight", "salt_lickers"));

    private SaltEvents() {
    }

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof PathfinderMob mob && mob.getType().is(SALT_LICKERS)) {
            mob.goalSelector.addGoal(4, new StayNearSaltGoal(mob));
        }
    }

    /**
     * A Rock Salt Block placed by a player (fake players too: Create's Deployer) becomes stage 0 of a salt lick, the
     * placed twin of The Salt's block. Generated Rock Salt Blocks never pass here, so they are never licks.
     */
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getLevel() instanceof Level level) || level.isClientSide()) {
            return;
        }
        Block rock = SaltLick.saltBlock();
        if (rock != null && rock != Blocks.AIR && event.getPlacedBlock().is(rock)) {
            level.setBlock(event.getPos(), HHSaltRegistry.PLACED_ROCK_SALT.get().defaultBlockState(), Block.UPDATE_ALL);
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
                SaltLick.refill(level, pos, state, 1);
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

    /**
     * Rock Salt in a dispenser facing a worn salt lick restores one stage (waxed or not, the same refill as by hand;
     * plan_post_scratch_1115 G2b). Any other target falls through to the behavior already registered (a plain drop).
     * Salt dust is no longer wrapped: it keeps The Salt's melting dispense (G6-10).
     */
    public static void registerDispenser() {
        registerDispenser(ForgeRegistries.ITEMS.getValue(SaltLick.ROCK_SALT_ID));
    }

    private static void registerDispenser(Item salt) {
        if (salt == null || salt == Items.AIR) {
            return;
        }
        // Wrap the behavior already there, so a target that is not a worn lick still gets it.
        java.util.Map<Item, DispenseItemBehavior> registry = ObfuscationReflectionHelper.getPrivateValue(DispenserBlock.class, null, "f_52661_");
        DispenseItemBehavior previous = registry == null ? null : registry.get(salt);
        DispenserBlock.registerBehavior(salt, new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos target = source.getPos().relative(facing);
                Level level = source.getLevel();
                BlockState state = level.getBlockState(target);
                Block block = state.getBlock();
                if (SaltLick.restored(block) != null && SaltLick.refill(level, target, state, 1) == 0) {
                    stack.shrink(1);
                    return stack;
                }
                return previous != null ? previous.dispense(source, stack) : super.execute(source, stack);
            }
        });
    }

    /** The hint line (S6): every unwaxed stage, and the Rock Salt Block that becomes stage 0. Fires on the client only. */
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof BlockItem item
                && (SaltLick.isLickable(item.getBlock()) || item.getBlock() == SaltLick.saltBlock())) {
            event.getToolTip().add(Component.translatable(HearthAndHarvestMod.MODID + ".salt_lick.hint").withStyle(ChatFormatting.GRAY));
        }
    }
}
