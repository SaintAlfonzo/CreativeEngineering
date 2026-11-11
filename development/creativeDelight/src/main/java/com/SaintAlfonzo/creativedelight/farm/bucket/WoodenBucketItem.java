package com.SaintAlfonzo.creativedelight.farm.bucket;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * One wooden bucket item per fluid ({@code fluidId} null = empty). The empty one scoops only a water source (also from
 * waterlogged blocks); the water one places water and hands back the empty wooden bucket. Sap and honey only move
 * through tanks, pipes and the tree tapper (right-click does nothing, so nothing is lost into the air). The milk one
 * is drunk like a milk bucket. Fluid-handler interactions go through {@link Wrapper}, which stays wooden and refuses
 * every fluid outside {@link WoodenBuckets#BY_FLUID}.
 */
public class WoodenBucketItem extends BucketItem {

    private final ResourceLocation fluidId;

    public WoodenBucketItem(ResourceLocation fluidId, Properties properties) {
        super(() -> fluidId == null ? Fluids.EMPTY : ForgeRegistries.FLUIDS.getValue(fluidId), properties);
        this.fluidId = fluidId;
    }

    private boolean is(ResourceLocation id) {
        return id.equals(fluidId);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (fluidId == null) {
            return scoopWater(level, player, stack);
        }
        if (is(WoodenBuckets.WATER_ID)) {
            InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
            if (result.getResult().consumesAction() && result.getObject().is(Items.BUCKET)) {
                return new InteractionResultHolder<>(result.getResult(), new ItemStack(WoodenBuckets.EMPTY.get()));
            }
            return result;
        }
        if (is(WoodenBuckets.MILK_ID)) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }
        return InteractionResultHolder.pass(stack);
    }

    private InteractionResultHolder<ItemStack> scoopWater(Level level, Player player, ItemStack stack) {
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
        player.awardStat(Stats.ITEM_USED.get(this));
        pickup.getPickupSound(state).ifPresent(sound -> player.playSound(sound, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
        ItemStack result = ItemUtils.createFilledResult(stack, player, new ItemStack(WoodenBuckets.WATER.get()));
        return InteractionResultHolder.sidedSuccess(result, level.isClientSide());
    }

    // Milk: drunk like vanilla's MilkBucketItem.

    @Override
    public int getUseDuration(ItemStack stack) {
        return is(WoodenBuckets.MILK_ID) ? 32 : 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return is(WoodenBuckets.MILK_ID) ? UseAnim.DRINK : UseAnim.NONE;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!is(WoodenBuckets.MILK_ID)) {
            return stack;
        }
        if (!level.isClientSide) {
            entity.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
        }
        if (entity instanceof ServerPlayer player) {
            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return stack;
        }
        stack.shrink(1);
        return stack.isEmpty() ? new ItemStack(WoodenBuckets.EMPTY.get()) : stack;
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
            return WoodenBuckets.BY_FLUID.containsKey(ForgeRegistries.FLUIDS.getKey(fluid.getFluid()));
        }

        @Override
        protected void setFluid(FluidStack fluid) {
            if (fluid.isEmpty()) {
                container = new ItemStack(WoodenBuckets.EMPTY.get());
                return;
            }
            var bucket = WoodenBuckets.BY_FLUID.get(ForgeRegistries.FLUIDS.getKey(fluid.getFluid()));
            if (bucket != null) {
                container = new ItemStack(bucket.get());
            }
        }
    }
}
