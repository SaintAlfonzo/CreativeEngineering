package com.SaintAlfonzo.creativedelight.hearth.machine;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;

/** Fluid Jug in hand: picks up world fluid sources, exchanges with fluid tanks, keeps the tank in the stack's tag. */
public class JugBlockItem extends BlockItem {
    private static final String TAG_FLUID = "HHJugFluid";

    public JugBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static FluidStack getFluid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(TAG_FLUID) ? FluidStack.EMPTY : FluidStack.loadFluidStackFromNBT(tag.getCompound(TAG_FLUID));
    }

    public static void setFluid(ItemStack stack, FluidStack fluid) {
        if (fluid.isEmpty()) {
            if (stack.getTag() != null) {
                stack.getTag().remove(TAG_FLUID);
                if (stack.getTag().isEmpty()) stack.setTag(null);
            }
        } else {
            stack.getOrCreateTag().put(TAG_FLUID, fluid.writeToNBT(new CompoundTag()));
        }
    }

    private static FluidTank readTank(ItemStack stack) {
        FluidTank tank = new FluidTank(JugBlockEntity.CAPACITY);
        tank.setFluid(getFluid(stack));
        return tank;
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        FluidStack carried = getFluid(context.getItemInHand());
        InteractionResult result = super.place(context);
        if (result.consumesAction() && !level.isClientSide && !carried.isEmpty() && level.getBlockEntity(context.getClickedPos()) instanceof JugBlockEntity jug) {
            jug.tank().setFluid(carried);
            jug.sync();
        }
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return super.use(level, player, hand);
        BlockPos pos = hit.getBlockPos();
        FluidState fluidState = level.getFluidState(pos);
        if (!fluidState.isSource()) return super.use(level, player, hand);
        if (!player.mayUseItemAt(pos, hit.getDirection(), stack)) return InteractionResultHolder.fail(stack);
        ItemStack single = ItemHandlerHelper.copyStackWithSize(stack, 1);
        FluidTank tank = readTank(single);
        FluidStack incoming = new FluidStack(fluidState.getType(), 1000);
        if (tank.fill(incoming, IFluidHandler.FluidAction.SIMULATE) != 1000) {
            if (level.isClientSide) player.displayClientMessage(Component.translatable("tooltip.hearthandharvest.jug.full").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BucketPickup pickup) || pickup.pickupBlock(level, pos, state).isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }
        tank.fill(incoming, IFluidHandler.FluidAction.EXECUTE);
        setFluid(single, tank.getFluid());
        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (stack.getCount() == 1) return InteractionResultHolder.sidedSuccess(single, false);
        stack.shrink(1);
        if (!player.addItem(single)) player.drop(single, false);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null) return InteractionResult.PASS;
        IFluidHandler handler = FluidUtil.getFluidHandler(level, pos, context.getClickedFace()).orElse(null);
        if (handler == null) return super.useOn(context);
        if (!player.mayUseItemAt(pos, context.getClickedFace(), stack)) return InteractionResult.FAIL;
        ItemStack single = ItemHandlerHelper.copyStackWithSize(stack, 1);
        FluidTank jug = readTank(single);
        FluidStack jugFluid = jug.getFluid().copy();
        if (!jugFluid.isEmpty()) {
            int simFill = handler.fill(jugFluid, IFluidHandler.FluidAction.SIMULATE);
            if (simFill > 0) {
                if (!level.isClientSide) {
                    FluidStack moving = jugFluid.copy();
                    moving.setAmount(simFill);
                    handler.fill(moving, IFluidHandler.FluidAction.EXECUTE);
                    jug.drain(simFill, IFluidHandler.FluidAction.EXECUTE);
                    setFluid(single, jug.getFluid());
                    giveBack(player, context.getHand(), stack, single);
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        int space = JugBlockEntity.CAPACITY - jug.getFluidAmount();
        if (space <= 0) {
            if (level.isClientSide) player.displayClientMessage(Component.translatable("tooltip.hearthandharvest.jug.full").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        if (handler.getTanks() == 0) return InteractionResult.PASS;
        FluidStack source = handler.getFluidInTank(0);
        if (source.isEmpty()) return InteractionResult.PASS;
        FluidStack request = source.copy();
        request.setAmount(space);
        FluidStack simDrain = handler.drain(request, IFluidHandler.FluidAction.SIMULATE);
        if (simDrain.isEmpty()) return InteractionResult.PASS;
        int simFill = jug.fill(simDrain, IFluidHandler.FluidAction.SIMULATE);
        if (simFill <= 0) {
            if (level.isClientSide) player.displayClientMessage(Component.translatable("tooltip.hearthandharvest.jug.wrong_fluid").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            FluidStack exact = source.copy();
            exact.setAmount(simFill);
            jug.fill(handler.drain(exact, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
            setFluid(single, jug.getFluid());
            giveBack(player, context.getHand(), stack, single);
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void giveBack(Player player, InteractionHand hand, ItemStack held, ItemStack modified) {
        held.shrink(1);
        if (held.isEmpty()) {
            player.setItemInHand(hand, modified);
        } else if (!player.addItem(modified)) {
            player.drop(modified, false);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        FluidStack fluid = getFluid(stack);
        if (fluid.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.hearthandharvest.jug.empty").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(fluid.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.translatable("tooltip.hearthandharvest.jug.amount", fluid.getAmount()).withStyle(ChatFormatting.GRAY));
        }
    }
}
