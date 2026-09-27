package me.desht.pneumaticcraft.common.item;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.capabilities.FluidItemWrapper;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.EnchantmentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class MemoryStickItem extends Item implements ColorHandlers.ITintableItem, ILeftClickableItem {
   private static final String TANK_NAME = "Tank";
   private static final String NBT_ABSORB_ORBS = "AbsorbXPOrbs";
   private static final int XP_FLUID_CAPACITY = 512000;
   private static final int[] TINT_COLORS = new int[]{16252863, 15925145, 15597427, 15269709, 14941990, 14614272};

   public MemoryStickItem() {
      super(ModItems.defaultProps().m_41487_(1));
   }

   public InteractionResultHolder<ItemStack> m_7203_(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.m_21120_(handIn);
      if (stack.m_41613_() != 1) {
         return InteractionResultHolder.m_19098_(stack);
      } else {
         if (!worldIn.f_46443_) {
            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent((handler) -> {
               int ratio = XPFluidManager.getInstance().getXPRatio((Fluid)ModFluids.MEMORY_ESSENCE.get());
               int playerXp = EnchantmentUtils.getPlayerXP(playerIn);
               if (playerIn.m_6144_()) {
                  int xpToGive = EnchantmentUtils.getExperienceForLevel(playerIn.f_36078_ + 1) - playerXp;
                  int fluidAmount = xpToGive * ratio;
                  FluidStack toDrain = handler.drain(fluidAmount, FluidAction.SIMULATE);
                  if (!toDrain.isEmpty()) {
                     EnchantmentUtils.addPlayerXP(playerIn, toDrain.getAmount() / ratio);
                     handler.drain(toDrain.getAmount(), FluidAction.EXECUTE);
                     playerIn.m_21008_(handIn, handler.getContainer());
                  }
               } else if (playerXp > 0) {
                  int xpToTake = playerXp - EnchantmentUtils.getExperienceForLevel(playerIn.f_36078_);
                  if (xpToTake == 0) {
                     xpToTake = playerXp - EnchantmentUtils.getExperienceForLevel(playerIn.f_36078_ - 1);
                  }

                  int fluidAmount = xpToTake * ratio;
                  FluidStack toFill = new FluidStack((Fluid)ModFluids.MEMORY_ESSENCE.get(), fluidAmount);
                  int filled = handler.fill(toFill, FluidAction.SIMULATE);
                  if (filled >= ratio) {
                     EnchantmentUtils.addPlayerXP(playerIn, -(filled / ratio));
                     handler.fill(new FluidStack((Fluid)ModFluids.MEMORY_ESSENCE.get(), filled), FluidAction.EXECUTE);
                     playerIn.m_21008_(handIn, handler.getContainer());
                  }
               }

            });
         } else {
            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent((handler) -> {
               int amount = handler.getFluidInTank(0).getAmount();
               if (EnchantmentUtils.getPlayerXP(playerIn) > 0 && amount < handler.getTankCapacity(0) && !playerIn.m_6144_() || handler.getFluidInTank(0).getAmount() > 0 && playerIn.m_6144_()) {
                  playerIn.m_5496_(SoundEvents.f_11871_, 0.1F, (worldIn.f_46441_.m_188501_() - worldIn.f_46441_.m_188501_()) * 0.35F + 0.9F);
               }

            });
         }

         return InteractionResultHolder.m_19090_(stack);
      }
   }

   public void m_7373_(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
      super.m_7373_(stack, worldIn, tooltip, flagIn);
      if (worldIn != null) {
         stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent((handler) -> {
            int ratio = XPFluidManager.getInstance().getXPRatio((Fluid)ModFluids.MEMORY_ESSENCE.get());
            if (ratio > 0) {
               FluidStack fluidStack = handler.getFluidInTank(0);
               int amount = fluidStack.getAmount();
               int levels = EnchantmentUtils.getLevelForExperience(amount / ratio);
               tooltip.add(Component.m_237110_("pneumaticcraft.gui.tooltip.memory_stick.xp_stored", new Object[]{amount / ratio, levels}).m_130940_(ChatFormatting.GREEN));
            }

         });
         boolean absorb = shouldAbsorbXPOrbs(stack);
         tooltip.add(Component.m_237115_("pneumaticcraft.message.memory_stick.absorb." + absorb).m_130940_(ChatFormatting.YELLOW));
      }

   }

   public int m_142158_(ItemStack pStack) {
      return (Integer)pStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map((handler) -> {
         FluidStack fluidStack = handler.getFluidInTank(0);
         return Math.round((float)fluidStack.getAmount() / (float)handler.getTankCapacity(0) * 13.0F);
      }).orElse(0);
   }

   public boolean m_142522_(ItemStack pStack) {
      return true;
   }

   @Nullable
   public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
      return new FluidItemWrapper(stack, "Tank", 512000, (fluid) -> fluid == ModFluids.MEMORY_ESSENCE.get());
   }

   public static boolean shouldAbsorbXPOrbs(ItemStack stack) {
      return stack.m_41720_() == ModItems.MEMORY_STICK.get() && stack.m_41613_() == 1 && stack.m_41782_() && ((CompoundTag)Objects.requireNonNull(stack.m_41783_())).m_128471_("AbsorbXPOrbs");
   }

   public static void setAbsorbXPOrbs(ItemStack stack, boolean absorb) {
      if (stack.m_41720_() == ModItems.MEMORY_STICK.get()) {
         stack.m_41784_().m_128379_("AbsorbXPOrbs", absorb);
      }

   }

   public int getTintColor(ItemStack stack, int tintIndex) {
      int var10000;
      switch (tintIndex) {
         case 1 -> var10000 = (Integer)stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map((handler) -> {
   FluidStack fluidStack = handler.getFluidInTank(0);
   if (fluidStack.isEmpty()) {
      return 16777215;
   } else {
      float f = (float)fluidStack.getAmount() / (float)handler.getTankCapacity(0);
      return TINT_COLORS[(int)(f * 5.0F)];
   }
}).orElse(-1);
         case 2 -> var10000 = shouldAbsorbXPOrbs(stack) ? -16711936 : -8355712;
         default -> var10000 = -1;
      }

      return var10000;
   }

   public void onLeftClickEmpty(ServerPlayer sender) {
      toggleXPAbsorption(sender, sender.m_21205_());
   }

   public void m_6883_(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
      if (shouldAbsorbXPOrbs(stack) && entityIn instanceof Player && itemSlot >= 0) {
         cacheMemoryStickLocation((Player)entityIn, me.desht.pneumaticcraft.common.item.MemoryStickItem.MemoryStickLocator.playerInv(itemSlot));
      }

   }

   public static boolean isRoomInStick(ItemStack stick) {
      return stick.m_41720_() instanceof MemoryStickItem && (Boolean)stick.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map((h) -> h.getFluidInTank(0).getAmount() < h.getTankCapacity(0)).orElseThrow(RuntimeException::new);
   }

   private static void toggleXPAbsorption(Player player, ItemStack stack) {
      if (stack.m_41720_() instanceof MemoryStickItem) {
         boolean absorb = shouldAbsorbXPOrbs(stack);
         setAbsorbXPOrbs(stack, !absorb);
         player.m_5661_(Component.m_237115_("pneumaticcraft.message.memory_stick.absorb." + !absorb).m_130940_(ChatFormatting.YELLOW), true);
         player.m_20193_().m_5594_((Player)null, player.m_20183_(), SoundEvents.f_12211_, SoundSource.PLAYERS, 1.0F, absorb ? 1.5F : 2.0F);
      }

   }

   public static void cacheMemoryStickLocation(Player entityIn, MemoryStickLocator locator) {
      ((Set)me.desht.pneumaticcraft.common.item.MemoryStickItem.Listener.memoryStickCache.computeIfAbsent(entityIn.m_20148_(), (k) -> new HashSet())).add(locator);
   }
}
