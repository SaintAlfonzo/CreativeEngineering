package umpaz.brewinandchewin.forge.platform;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;
import umpaz.brewinandchewin.common.attachment.TipsyHeartsAttachment;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.block.entity.container.KegStackedContents;
import umpaz.brewinandchewin.common.block.entity.container.SidedKegWrapper;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.network.BnCPacket;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCMenuConstructor;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;
import umpaz.brewinandchewin.forge.container.KegFluidItemStorageForge;
import umpaz.brewinandchewin.forge.container.KegFluidTankForge;
import umpaz.brewinandchewin.forge.container.KegItemHandlerForge;
import umpaz.brewinandchewin.forge.container.SidedKegWrapperForge;
import umpaz.brewinandchewin.forge.network.BnCNetwork;
import umpaz.brewinandchewin.forge.registry.BnCAttachments;
import umpaz.brewinandchewin.forge.registry.BnCFluidsImpl;
import umpaz.brewinandchewin.forge.utility.BnCCreateDelegate;
import umpaz.brewinandchewin.forge.utility.KegRecipeWrapperForge;
import umpaz.brewinandchewin.platform.BnCPlatform;
import umpaz.brewinandchewin.platform.BnCPlatformHelper;
import vectorwing.farmersdelight.common.Configuration;

/** Port of BnCPlatformHelperNeoForge onto Forge 1.19.2. */
public class BnCPlatformHelperForge implements BnCPlatformHelper {
   @Override
   public BnCPlatform getPlatform() {
      return BnCPlatform.NEOFORGE;
   }

   @Override
   public boolean isModLoaded(String modId) {
      ModList list = ModList.get();
      return list != null ? list.isLoaded(modId) : FMLLoader.getLoadingModList().getModFileById(modId) != null;
   }

   @Override
   public boolean isDevelopmentEnvironment() {
      return !FMLLoader.isProduction();
   }

   @Override
   public void sendClientbound(ServerPlayer player, BnCPacket packet) {
      if (!player.level.isClientSide) {
         BnCNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
      }
   }

   @Override
   public void sendClientboundTracking(Entity tracked, BnCPacket packet) {
      if (!tracked.level.isClientSide) {
         BnCNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> tracked), packet);
      }
   }

   @Override
   public void sendServerbound(BnCPacket packet) {
      BnCNetwork.CHANNEL.sendToServer(packet);
   }

   @Override
   public Component getFluidDisplayName(AbstractedFluidStack wrapper) {
      return wrapper.loaderSpecific() instanceof FluidStack fluidStack ? fluidStack.getDisplayName() : Component.translatable("");
   }

   @Override
   public void openKegMenu(Player player, KegBlockEntity blockEntity, BlockPos pos) {
      if (player instanceof ServerPlayer serverPlayer) {
         NetworkHooks.openScreen(serverPlayer, blockEntity, pos);
      }
   }

   @Override
   public BlockEntitySupplier<KegBlockEntity> supplyBlockEntity() {
      return KegBlockEntity::new;
   }

   @Override
   public MenuType<KegMenu> createMenuType(BnCMenuConstructor<KegMenu> constructor) {
      return IForgeMenuType.create((id, inv, data) -> constructor.apply(id, inv, data.readBlockPos()));
   }

   @Override
   public AbstractedItemHandler createKegInventory(int size, BiConsumer<AbstractedItemHandler, Integer> onContentsChanged) {
      return new KegItemHandlerForge(size) {
         @Override
         protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            onContentsChanged.accept(this, slot);
         }
      };
   }

   @Override
   public AbstractedFluidTank createKegTank(long capacity, Runnable onContentsChanged) {
      return new KegFluidTankForge((int)capacity) {
         @Override
         protected void onContentsChanged() {
            super.onContentsChanged();
            onContentsChanged.run();
         }
      };
   }

   @Override
   public Slot createKegSlot(AbstractedItemHandler inventory, int slot, int x, int y, boolean canInsert, @Nullable Pair<ResourceLocation, ResourceLocation> noItemIcon) {
      return new SlotItemHandler((KegItemHandlerForge)inventory, slot, x, y) {
         @Override
         public boolean mayPlace(ItemStack stack) {
            return canInsert && super.mayPlace(stack);
         }

         @Nullable
         @Override
         public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return noItemIcon;
         }
      };
   }

   @Override
   public Ingredient createStrictFillPickerIngredient(List<KegStackedContents.PouringEntry> entries) {
      return CompoundIngredient.of(
         entries.stream().map(p -> p.strict() ? StrictNBTIngredient.of(p.stack()) : Ingredient.of(p.stack().getItem())).toArray(Ingredient[]::new)
      );
   }

   @Override
   public KegRecipeWrapper createRecipeWrapper(AbstractedItemHandler itemHandler, AbstractedFluidTank fluidTank) {
      return new KegRecipeWrapperForge((IItemHandlerModifiable)itemHandler, fluidTank);
   }

   @Override
   public SidedKegWrapper createSidedKegWrapper(AbstractedItemHandler inventory, Direction direction) {
      return new SidedKegWrapperForge(inventory, direction);
   }

   /** The keg item's "FluidTank" tag, as FluidTank#writeToNBT wrote it. */
   @Override
   public AbstractedFluidStack deserializeTankFluidStack(CompoundTag tag) {
      return KegFluidTankForge.wrap(FluidStack.loadFluidStackFromNBT(tag));
   }

   @Override
   public ItemStack getCraftingRemainingItem(ItemStack stack) {
      return stack.getCraftingRemainingItem();
   }

   @Override
   public void initFluids() {
      BnCFluidsImpl.init();
   }

   @Override
   public boolean isEdible(ItemStack stack, LivingEntity entity) {
      return stack.getFoodProperties(entity) != null;
   }

   @Override
   public FoodProperties getFoodProperties(ItemStack stack, LivingEntity entity) {
      return stack.getFoodProperties(entity);
   }

   @Nullable
   @Override
   public MinecraftServer getServer() {
      return ServerLifecycleHooks.getCurrentServer();
   }

   @Nullable
   @Override
   public RagingAttachment getRagingAttachment(LivingEntity entity) {
      BnCAttachments.Data data = BnCAttachments.get(entity);
      return data == null ? null : data.raging;
   }

   @Override
   public void setRagingAttachment(LivingEntity entity, @Nullable RagingAttachment value) {
      BnCAttachments.Data data = BnCAttachments.get(entity);
      if (data != null) {
         data.raging = value;
      }
   }

   @Nullable
   @Override
   public TipsyHeartsAttachment getTipsyHeartsAttachment(LivingEntity entity) {
      BnCAttachments.Data data = BnCAttachments.get(entity);
      return data == null ? null : data.tipsyHearts;
   }

   @Override
   public void setTipsyHeartsAttachment(LivingEntity entity, @Nullable TipsyHeartsAttachment value) {
      BnCAttachments.Data data = BnCAttachments.get(entity);
      if (data != null) {
         data.tipsyHearts = value;
      }
   }

   @Override
   public Object createLoaderFluidStack(AbstractedFluidStack abstracted) {
      return new FluidStack(abstracted.fluid(), (int)abstracted.unit().convertToLoader(abstracted.amount()), abstracted.tag());
   }

   @Override
   public Object copyLoaderFluidStack(Object fluidStack) {
      return ((FluidStack)fluidStack).copy();
   }

   @Nullable
   @Override
   public AbstractedFluidTank getFluidContainerFromItem(ItemStack stack) {
      return KegFluidItemStorageForge.of(stack);
   }

   @Override
   public Fluid getMilkFluid() {
      return ForgeMod.MILK.get();
   }

   @Override
   public Fluid getFlowingMilkFluid() {
      return ForgeMod.FLOWING_MILK.get();
   }

   @Nullable
   @Override
   public Fluid getCreatePotionFluid() {
      return this.isModLoaded("create") ? BnCCreateDelegate.getPotionSource() : null;
   }

   @Override
   public boolean hasFoodEffectTooltip() {
      return Configuration.FOOD_EFFECT_TOOLTIP.get();
   }
}
