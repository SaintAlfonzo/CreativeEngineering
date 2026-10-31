package com.SaintAlfonzo.creativeengineering.bucket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

/**
 * Water-only bucket. The empty one scoops a water source (also from waterlogged blocks) and nothing else; the
 * full one places water through vanilla's {@link BucketItem#use} and hands back the empty wooden bucket instead of
 * an iron one. Fluid-handler interactions (Create, tanks) go through {@link Wrapper}, which swaps between the two
 * wooden items and refuses every other fluid.
 */
public class WoodenBucketItem extends BucketItem {

    private final boolean filled;

    public WoodenBucketItem(boolean filled, Item.Properties properties) {
        super(filled ? () -> Fluids.WATER : () -> Fluids.EMPTY, properties);
        this.filled = filled;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (filled) {
            InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
            if (result.getResult().consumesAction() && result.getObject().is(Items.BUCKET)) {
                return new InteractionResultHolder<>(result.getResult(), new ItemStack(WoodenBucketItems.EMPTY.get()));
            }
            return result;
        }

        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }
        BlockPos pos = hit.getBlockPos();
        Direction face = hit.getDirection();
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos.relative(face), face, stack)) {
            return InteractionResultHolder.fail(stack);
        }
        FluidState fluid = level.getFluidState(pos);
        BlockState state = level.getBlockState(pos);
        if (!fluid.isSource() || fluid.getType() != Fluids.WATER || !(state.getBlock() instanceof BucketPickup pickup)) {
            return InteractionResultHolder.fail(stack);
        }
        ItemStack picked = pickup.pickupBlock(level, pos, state);
        if (picked.isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }
        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        pickup.getPickupSound(state).ifPresent(sound -> player.playSound(sound, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
        ItemStack result = ItemUtils.createFilledResult(stack, player, new ItemStack(WoodenBucketItems.WATER.get()));
        return InteractionResultHolder.sidedSuccess(result, level.isClientSide());
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new Wrapper(stack);
    }

    /** Forge's wrapper would fill to {@code fluid.getBucket()} (the iron buckets); this one stays wooden. */
    private static final class Wrapper extends FluidBucketWrapper {

        Wrapper(ItemStack container) {
            super(container);
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return fluid.getFluid() == Fluids.WATER;
        }

        @Override
        protected void setFluid(FluidStack fluid) {
            if (fluid.isEmpty()) {
                container = new ItemStack(WoodenBucketItems.EMPTY.get());
            } else if (fluid.getFluid() == Fluids.WATER) {
                container = new ItemStack(WoodenBucketItems.WATER.get());
            }
        }
    }
}
