package com.sammy.minersdelight.content.item;

import com.sammy.minersdelight.registry.MDItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

/**
 * The Copper Cup, empty or holding water: a one-serving bucket. Empty, it scoops a water source (or powder snow) and
 * milks cows and goats; the water cup pours a source back out.
 */
public class CopperCupItem extends Item implements DispensibleContainerItem {
    private final Fluid content;

    public CopperCupItem(Fluid content, Properties properties) {
        super(properties);
        this.content = content;
    }

    /** The cup that holds what a bucket holds, or EMPTY when there is none. */
    public static ItemStack cupFor(Item bucket) {
        if (bucket == Items.WATER_BUCKET) {
            return new ItemStack(MDItems.WATER_CUP.get());
        }
        if (bucket == Items.MILK_BUCKET) {
            return new ItemStack(MDItems.MILK_CUP.get());
        }
        if (bucket == Items.POWDER_SNOW_BUCKET) {
            return new ItemStack(MDItems.POWDER_SNOW_CUP.get());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        // Milking goes through the animal's own bucket interaction, with a bucket lent for the moment.
        if (content == Fluids.EMPTY && (target instanceof Cow || target instanceof Goat)) {
            player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            InteractionResult result = target.interact(player, hand);
            ItemStack cup = cupFor(player.getItemInHand(hand).getItem());
            player.setItemInHand(hand, cup.isEmpty() ? stack : ItemUtils.createFilledResult(stack, player, cup));
            if (!cup.isEmpty()) {
                return result;
            }
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, content == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        InteractionResultHolder<ItemStack> event = ForgeEventFactory.onBucketUse(player, level, stack, hit);
        if (event != null) {
            return event;
        }
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }
        BlockPos pos = hit.getBlockPos();
        Direction face = hit.getDirection();
        BlockPos beside = pos.relative(face);
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(beside, face, stack)) {
            return InteractionResultHolder.fail(stack);
        }
        if (content == Fluids.EMPTY) {
            return scoop(level, player, stack, pos);
        }
        BlockState state = level.getBlockState(pos);
        BlockPos target = canHold(level, pos, state) ? pos : beside;
        if (!emptyContents(player, level, target, hit)) {
            return InteractionResultHolder.fail(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, target, stack);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        ItemStack after = player.getAbilities().instabuild ? stack : ItemUtils.createFilledResult(stack, player, new ItemStack(MDItems.COPPER_CUP.get()));
        return InteractionResultHolder.sidedSuccess(after, level.isClientSide());
    }

    private InteractionResultHolder<ItemStack> scoop(Level level, Player player, ItemStack stack, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BucketPickup pickup)) {
            return InteractionResultHolder.fail(stack);
        }
        ItemStack bucket = pickup.pickupBlock(level, pos, state);
        if (bucket.isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }
        ItemStack cup = cupFor(bucket.getItem());
        if (cup.isEmpty()) {
            // Not something a cup holds (lava): put it back.
            level.setBlock(pos, state, Block.UPDATE_ALL);
            return InteractionResultHolder.fail(stack);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        pickup.getPickupSound(state).ifPresent(sound -> player.playSound(sound, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.FILLED_BUCKET.trigger(serverPlayer, cup);
        }
        return InteractionResultHolder.sidedSuccess(ItemUtils.createFilledResult(stack, player, cup), level.isClientSide());
    }

    private boolean canHold(Level level, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof LiquidBlockContainer container && container.canPlaceLiquid(level, pos, state, content);
    }

    @Override
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult hit) {
        if (!(content instanceof FlowingFluid flowing)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        boolean replaceable = state.canBeReplaced(content);
        if (!state.isAir() && !replaceable && !canHold(level, pos, state)) {
            return hit != null && emptyContents(player, level, hit.getBlockPos().relative(hit.getDirection()), null);
        }
        if (level.dimensionType().ultraWarm() && content.is(FluidTags.WATER)) {
            level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
            for (int i = 0; i < 8; i++) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0, 0, 0);
            }
            return true;
        }
        if (canHold(level, pos, state)) {
            ((LiquidBlockContainer) state.getBlock()).placeLiquid(level, pos, state, flowing.getSource(false));
            playEmptySound(player, level, pos);
            return true;
        }
        if (!level.isClientSide && replaceable && !state.getMaterial().isLiquid()) {
            level.destroyBlock(pos, true);
        }
        if (!level.setBlock(pos, content.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE) && !state.getFluidState().isSource()) {
            return false;
        }
        playEmptySound(player, level, pos);
        return true;
    }

    private void playEmptySound(@Nullable Player player, LevelAccessor level, BlockPos pos) {
        SoundEvent sound = content.getFluidType().getSound(player, level, pos, SoundActions.BUCKET_EMPTY);
        if (sound == null) {
            sound = content.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        }
        level.playSound(player, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
    }
}
