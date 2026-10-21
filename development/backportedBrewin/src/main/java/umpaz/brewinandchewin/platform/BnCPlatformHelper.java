package umpaz.brewinandchewin.platform;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
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
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.material.Fluid;
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

// Port: upstream's loader abstraction, kept so the common code reads like 4.5.0. The only implementation is
// forge/platform/BnCPlatformHelperForge. Codec / StreamCodec getters are gone: 1.19.2 recipes and packets use JSON and
// FriendlyByteBuf, see AbstractedFluidStack and KegCompatibleFluidIngredients.
public interface BnCPlatformHelper {
   BnCPlatform getPlatform();

   boolean isModLoaded(String modId);

   default boolean isModLoadedEarly(String modId) {
      return this.isModLoaded(modId);
   }

   boolean isDevelopmentEnvironment();

   void sendClientbound(ServerPlayer player, BnCPacket packet);

   void sendClientboundTracking(Entity tracked, BnCPacket packet);

   void sendServerbound(BnCPacket packet);

   Component getFluidDisplayName(AbstractedFluidStack stack);

   void openKegMenu(Player player, KegBlockEntity blockEntity, BlockPos pos);

   BlockEntitySupplier<KegBlockEntity> supplyBlockEntity();

   MenuType<KegMenu> createMenuType(BnCMenuConstructor<KegMenu> constructor);

   /** Port: `validator` (slot, stack) backs isItemValid, so menus and hoppers both follow the keg's slot rules. */
   AbstractedItemHandler createKegInventory(int size, BiConsumer<AbstractedItemHandler, Integer> onContentsChanged, BiPredicate<Integer, ItemStack> validator);

   AbstractedFluidTank createKegTank(long capacity, Runnable onContentsChanged);

   default Slot createKegSlot(AbstractedItemHandler inventory, int slot, int x, int y) {
      return this.createKegSlot(inventory, slot, x, y, true, null);
   }

   default Slot createKegContainerSlot(AbstractedItemHandler inventory, int slot, int x, int y) {
      return this.createKegSlot(inventory, slot, x, y, true, Pair.of(InventoryMenu.BLOCK_ATLAS, KegMenu.EMPTY_CONTAINER_SLOT_TANKARD));
   }

   default Slot createKegResultSlot(AbstractedItemHandler inventory, int slot, int x, int y) {
      return this.createKegSlot(inventory, slot, x, y, false, null);
   }

   Slot createKegSlot(AbstractedItemHandler inventory, int slot, int x, int y, boolean canInsert, @Nullable Pair<ResourceLocation, ResourceLocation> noItemIcon);

   Ingredient createStrictFillPickerIngredient(List<KegStackedContents.PouringEntry> entries);

   KegRecipeWrapper createRecipeWrapper(AbstractedItemHandler itemHandler, AbstractedFluidTank fluidTank);

   SidedKegWrapper createSidedKegWrapper(AbstractedItemHandler inventory, Direction direction);

   AbstractedFluidStack deserializeTankFluidStack(CompoundTag tag);

   ItemStack getCraftingRemainingItem(ItemStack stack);

   void initFluids();

   boolean isEdible(ItemStack stack, LivingEntity entity);

   FoodProperties getFoodProperties(ItemStack stack, LivingEntity entity);

   @Nullable
   MinecraftServer getServer();

   @Nullable
   RagingAttachment getRagingAttachment(LivingEntity entity);

   void setRagingAttachment(LivingEntity entity, @Nullable RagingAttachment value);

   @Nullable
   TipsyHeartsAttachment getTipsyHeartsAttachment(LivingEntity entity);

   void setTipsyHeartsAttachment(LivingEntity entity, @Nullable TipsyHeartsAttachment value);

   Object createLoaderFluidStack(AbstractedFluidStack abstracted);

   Object copyLoaderFluidStack(Object fluidStack);

   @Nullable
   AbstractedFluidTank getFluidContainerFromItem(ItemStack stack);

   Fluid getMilkFluid();

   Fluid getFlowingMilkFluid();

   @Nullable
   Fluid getCreatePotionFluid();

   boolean hasFoodEffectTooltip();
}
