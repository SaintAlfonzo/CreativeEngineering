package umpaz.brewinandchewin.common.block.entity;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.block.KegBlock;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.block.entity.container.SidedKegWrapper;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.item.BoozeItem;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.tag.BnCTags;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCHeatSources;
import umpaz.brewinandchewin.common.utility.BnCItemUtils;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;
import vectorwing.farmersdelight.common.tag.ModTags;
import vectorwing.farmersdelight.common.utility.ItemUtils;

public class KegBlockEntity extends SyncedBlockEntity implements MenuProvider, Nameable, RecipeHolder {
   public static final int CONTAINER_SLOT = 4;
   public static final int OUTPUT_SLOT = 5;
   public static final int INVENTORY_SIZE = 6;
   public static final int RANGE = 2;
   /** Port: a 2.0 keg's drink serving, in mB per drink, for the old-world conversion (alcohol.md 3.3). */
   public static final int LEGACY_SERVING = 250;
   private final AbstractedItemHandler inventory;
   private final SidedKegWrapper inputHandler;
   private final SidedKegWrapper outputHandler;
   private final AbstractedFluidTank fluidTank;
   private final KegRecipeWrapper recipeWrapper;
   private final LazyOptional<IItemHandler> inputCap;
   private final LazyOptional<IItemHandler> outputCap;
   private final LazyOptional<?> fluidCap;
   private int fermentTime;
   private int fermentTimeTotal;
   private Component customName;
   private boolean deferFluidExtraction = false;
   private boolean currentlyOperating = false;
   public int kegTemperature;
   private boolean initialisedTemperature = false;
   protected final ContainerData kegData;
   private final Object2IntOpenHashMap<ResourceLocation> usedRecipeTracker;
   private ResourceLocation lastRecipeID;
   private boolean checkNewRecipe;
   /** Port: items a converted 2.0 keg couldn't keep; dropped on the next tick (saved until then). */
   private final List<ItemStack> pendingDrops = new ArrayList<>();

   public KegBlockEntity(BlockPos pos, BlockState state) {
      super(BnCBlockEntityTypes.KEG, pos, state);
      this.inventory = this.createHandler();
      this.inputHandler = BrewinAndChewin.getHelper().createSidedKegWrapper(this.inventory, Direction.UP);
      this.outputHandler = BrewinAndChewin.getHelper().createSidedKegWrapper(this.inventory, Direction.DOWN);
      this.fluidTank = this.createFluidTank();
      this.kegData = this.createIntArray();
      this.usedRecipeTracker = new Object2IntOpenHashMap<>();
      this.checkNewRecipe = true;
      this.recipeWrapper = BrewinAndChewin.getHelper().createRecipeWrapper(this.inventory, this.fluidTank);
      this.inputCap = LazyOptional.of(() -> (IItemHandler)this.inputHandler);
      this.outputCap = LazyOptional.of(() -> (IItemHandler)this.outputHandler);
      this.fluidCap = LazyOptional.of(() -> this.fluidTank);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      CompoundTag inventoryTag = compound.getCompound("Inventory");
      if (inventoryTag.getInt("Size") == 9) {
         this.loadLegacyInventory(inventoryTag);
      } else {
         this.inventory.readFromNbt(inventoryTag);
         this.fluidTank.readFromNbt(compound.getCompound("FluidTank"));
      }
      this.fermentTime = compound.getInt("FermentTime");
      this.fermentTimeTotal = compound.getInt("FermentTimeTotal");
      if (compound.contains("CustomName", 8)) {
         this.customName = Serializer.fromJson(compound.getString("CustomName"));
      }

      CompoundTag compoundRecipes = compound.getCompound("RecipesUsed");

      for (String key : compoundRecipes.getAllKeys()) {
         ResourceLocation id = ResourceLocation.tryParse(key);
         if (id != null) {
            this.usedRecipeTracker.put(id, compoundRecipes.getInt(key));
         }
      }

      if (compound.contains("Temperature", 3)) {
         this.kegTemperature = compound.getInt("Temperature");
      }

      this.pendingDrops.clear();
      ListTag drops = compound.getList("BnCPendingDrops", Tag.TAG_COMPOUND);
      for (int i = 0; i < drops.size(); i++) {
         ItemStack stack = ItemStack.of(drops.getCompound(i));
         if (!stack.isEmpty()) {
            this.pendingDrops.add(stack);
         }
      }

      this.checkNewRecipe = true;
   }

   /**
    * Port: a 2.0 keg (9 slots: 0-3 ingredients, 4 base-fluid item, 5 finished drink, 6 container in, 7 output,
    * 8 container out). The drink stack becomes 250 mB per drink in the tank; the rest goes to the container and output
    * slots, and anything left over drops.
    */
   private void loadLegacyInventory(CompoundTag inventoryTag) {
      ItemStackHandler old = new ItemStackHandler(9);
      old.deserializeNBT(inventoryTag);
      for (int i = 0; i < INVENTORY_SIZE; i++) {
         this.inventory.setStackInSlot(i, i < 4 ? old.getStackInSlot(i).copy() : ItemStack.EMPTY);
      }
      this.fluidTank.setAbstractedFluid(AbstractedFluidStack.EMPTY);

      List<ItemStack> leftovers = new ArrayList<>();
      AbstractedFluidStack legacyFluid = legacyDrinkToFluid(old.getStackInSlot(5), this.fluidTank.getFluidCapacity(), leftovers);
      if (!legacyFluid.isEmpty()) {
         this.fluidTank.setAbstractedFluid(legacyFluid);
      }
      leftovers.add(old.getStackInSlot(7).copy());
      leftovers.add(old.getStackInSlot(6).copy());
      leftovers.add(old.getStackInSlot(4).copy());
      leftovers.add(old.getStackInSlot(8).copy());
      for (ItemStack stack : leftovers) {
         if (stack.isEmpty()) {
            continue;
         }
         ItemStack rest = this.inventory.insertItem(OUTPUT_SLOT, stack, false);
         if (!rest.isEmpty()) {
            rest = this.inventory.insertItem(CONTAINER_SLOT, rest, false);
         }
         if (!rest.isEmpty()) {
            this.pendingDrops.add(rest);
         }
      }
      BrewinAndChewin.LOG.info("Converted a Brewin' and Chewin' 2.0 keg at {}: tank {}", this.worldPosition, this.fluidTank.getAbstractedFluid());
   }

   /** The fluid a 2.0 keg's drink slot turns into; drinks that don't fit (or aren't drinks) go to `leftovers`. */
   private static AbstractedFluidStack legacyDrinkToFluid(ItemStack drinks, long capacity, List<ItemStack> leftovers) {
      if (drinks.isEmpty()) {
         return AbstractedFluidStack.EMPTY;
      }
      if (!(drinks.getItem() instanceof BoozeItem booze)) {
         leftovers.add(drinks.copy());
         return AbstractedFluidStack.EMPTY;
      }
      int fit = (int)Math.min(drinks.getCount(), capacity / LEGACY_SERVING);
      if (fit < drinks.getCount()) {
         leftovers.add(BnCItemUtils.copyWithCount(drinks, drinks.getCount() - fit));
      }
      return fit <= 0 ? AbstractedFluidStack.EMPTY : new AbstractedFluidStack(booze.getFluid(), (long)fit * LEGACY_SERVING);
   }

   public static AbstractedFluidStack getMealFromItem(ItemStack kegStack) {
      if (!kegStack.is(BnCItems.KEG)) {
         return AbstractedFluidStack.EMPTY;
      } else {
         CompoundTag tag = BlockItem.getBlockEntityData(kegStack);
         if (tag == null || tag.isEmpty()) {
            return AbstractedFluidStack.EMPTY;
         }
         // Port: a 2.0 keg item shows what its drinks will become once placed.
         CompoundTag inventoryTag = tag.getCompound("Inventory");
         if (inventoryTag.getInt("Size") == 9) {
            ItemStackHandler old = new ItemStackHandler(9);
            old.deserializeNBT(inventoryTag);
            return legacyDrinkToFluid(old.getStackInSlot(5), BnCConfiguration.COMMON_CONFIG.get().keg().localizedCapacity(), new ArrayList<>());
         }
         return tag.contains("FluidTank", 10)
            ? BrewinAndChewin.getHelper().deserializeTankFluidStack(tag.getCompound("FluidTank"))
            : AbstractedFluidStack.EMPTY;
      }
   }

   public AbstractedFluidStack getOutput() {
      return this.fluidTank.getAbstractedFluid();
   }

   public SidedKegWrapper getSidedHandler(Direction direction) {
      return direction == Direction.UP ? this.inputHandler : this.outputHandler;
   }

   public CompoundTag writeMeal(CompoundTag tag) {
      return this.writeDrink(tag);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.put("Inventory", this.inventory.writeToNbt());
      compound.put("FluidTank", this.fluidTank.writeToNbt());
      compound.putInt("FermentTime", this.fermentTime);
      compound.putInt("FermentTimeTotal", this.fermentTimeTotal);
      if (this.customName != null) {
         compound.putString("CustomName", Serializer.toJson(this.customName));
      }

      CompoundTag compoundRecipes = new CompoundTag();
      this.usedRecipeTracker.forEach((recipeId, craftedAmount) -> compoundRecipes.putInt(recipeId.toString(), craftedAmount));
      compound.put("RecipesUsed", compoundRecipes);
      if (!this.pendingDrops.isEmpty()) {
         ListTag drops = new ListTag();
         for (ItemStack stack : this.pendingDrops) {
            drops.add(stack.save(new CompoundTag()));
         }
         compound.put("BnCPendingDrops", drops);
      }
   }

   private CompoundTag writeUpdateTag(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.put("Inventory", this.inventory.writeToNbt());
      compound.put("FluidTank", this.fluidTank.writeToNbt());
      compound.putInt("FermentTime", this.fermentTime);
      compound.putInt("FermentTimeTotal", this.fermentTimeTotal);
      compound.putInt("Temperature", this.kegTemperature);
      return compound;
   }

   /** The keg item's BlockEntityTag: the tank (when not empty) and the custom name. */
   public CompoundTag writeDrink(CompoundTag compound) {
      compound.putString("id", ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(BnCBlockEntityTypes.KEG).toString());
      if (this.customName != null) {
         compound.putString("CustomName", Serializer.toJson(this.customName));
      }

      if (!this.fluidTank.isEmpty()) {
         compound.put("FluidTank", this.fluidTank.writeToNbt());
      }

      return compound;
   }

   public static boolean isValidTemp(int kegTemp, int want) {
      return switch (want) {
         case 1 -> kegTemp <= 1;
         case 2 -> kegTemp <= 2;
         case 3 -> kegTemp < 5 && kegTemp > 1;
         case 4 -> kegTemp >= 4;
         case 5 -> kegTemp >= 5;
         default -> false;
      };
   }

   protected boolean canFerment(KegFermentingRecipe recipe, KegBlockEntity keg) {
      if (!this.hasInput()) {
         return false;
      } else if (this.level == null) {
         return false;
      } else if (!isValidTemp(keg.getTemperature(), recipe.getTemperature())) {
         return false;
      } else if (recipe.getFluidIngredient().isEmpty()) {
         return keg.fluidTank.isEmpty();
      } else {
         return !recipe.getFluidIngredient().get().ingredient().matches(keg.fluidTank.getAbstractedFluid())
            ? false
            : keg.fluidTank.getAbstractedFluid().amount() % recipe.getFluidIngredient().get().amount() == 0L;
      }
   }

   public static void fermentingTick(Level level, BlockPos pos, BlockState state, KegBlockEntity keg) {
      boolean didInventoryChange = false;
      if (!keg.pendingDrops.isEmpty()) {
         for (ItemStack stack : keg.pendingDrops) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, stack);
         }
         keg.pendingDrops.clear();
         keg.setChanged();
      }

      if (level.getGameTime() % 80L == 0L) {
         keg.updateTemperature();
      }

      if (keg.deferFluidExtraction) {
         keg.deferFluidExtraction = false;
         List<ItemStack> out = keg.extractInGui(keg.inventory.getStackInSlot(4), keg.inventory.getSlotLimit(5));
         if (!out.isEmpty()) {
            keg.inventory.insertItem(5, out.get(0), false);
         }
      }

      if (keg.hasInput()) {
         Optional<KegFermentingRecipe> recipe = keg.getMatchingRecipe(keg.recipeWrapper);
         if (recipe.isPresent()) {
            if (keg.canFerment(recipe.get(), keg)) {
               didInventoryChange = keg.processFermenting(recipe.get(), keg);
            } else {
               keg.fermentTime = Math.max(0, keg.fermentTime - 20);
            }
         } else {
            keg.fermentTime = Math.max(0, keg.fermentTime - 20);
         }
      } else if (keg.fermentTime > 0) {
         keg.fermentTime = Math.max(0, keg.fermentTime - 20);
      }

      if (didInventoryChange) {
         keg.inventoryChanged();
      }
   }

   public Optional<KegFermentingRecipe> getRecipeWithoutTemperature() {
      if (!this.hasInput()) {
         return Optional.empty();
      } else {
         Optional<KegFermentingRecipe> recipe = this.getMatchingRecipe(this.recipeWrapper);
         if (recipe.isEmpty()) {
            return Optional.empty();
         } else {
            if (recipe.get().getFluidIngredient().isEmpty()) {
               if (!this.fluidTank.isEmpty()) {
                  return Optional.empty();
               }
            } else {
               if (!recipe.get().getFluidIngredient().get().ingredient().matches(this.fluidTank.getAbstractedFluid())) {
                  return Optional.empty();
               }

               if (this.fluidTank.getAbstractedFluid().amount() % recipe.get().getFluidIngredient().get().amount() != 0L) {
                  return Optional.empty();
               }
            }

            return recipe;
         }
      }
   }

   private Optional<KegFermentingRecipe> getMatchingRecipe(KegRecipeWrapper inventoryWrapper) {
      if (this.level == null) {
         return Optional.empty();
      } else {
         if (this.checkNewRecipe) {
            Optional<KegFermentingRecipe> recipe = this.level
               .getRecipeManager()
               .getAllRecipesFor(BnCRecipeTypes.FERMENTING)
               .stream()
               .filter(a -> a.matches(inventoryWrapper, this.level))
               .findFirst();
            if (recipe.isPresent()) {
               ResourceLocation newRecipeID = recipe.get().getId();
               if (this.lastRecipeID != null && !this.lastRecipeID.equals(newRecipeID)) {
                  this.fermentTime = 0;
               }

               this.lastRecipeID = newRecipeID;
               return recipe;
            }
         }

         this.checkNewRecipe = false;
         if (this.lastRecipeID != null) {
            Optional<KegFermentingRecipe> recipe = this.level
               .getRecipeManager()
               .getRecipeFor(BnCRecipeTypes.FERMENTING, inventoryWrapper, this.level, this.lastRecipeID)
               .map(com.mojang.datafixers.util.Pair::getSecond);
            if (recipe.isPresent() && recipe.get().matches(inventoryWrapper, this.level)) {
               return recipe;
            }
         }

         return Optional.empty();
      }
   }

   private boolean hasInput() {
      for (int i = 0; i < 5; i++) {
         if (!this.inventory.getStackInSlot(i).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   private boolean processFermenting(KegFermentingRecipe recipe, KegBlockEntity keg) {
      if (this.level == null) {
         return false;
      } else {
         this.fermentTime++;
         this.fermentTimeTotal = recipe.getFermentTime();
         if (this.fermentTime < this.fermentTimeTotal) {
            this.setChanged();
            return false;
         } else {
            this.fermentTime = 0;
            if (recipe.getResult().left().isPresent()) {
               this.deferFluidExtraction = true;
               keg.fluidTank.setAbstractedFluid(recipe.getResult().left().get());
               if (!keg.level.isClientSide()) {
                  Vec3 center = Vec3.atCenterOf(keg.getBlockPos());
                  keg.level.playSound(null, center.x(), center.y(), center.z(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.6F, 0.8F);
               }
            }

            if (recipe.getResult().right().isPresent()) {
               if (recipe.getFluidIngredient().isPresent()) {
                  keg.fluidTank.drain(recipe.getFluidIngredient().get().amount(), recipe.getUnit(), false);
               }

               keg.inventory.insertItem(5, recipe.getResult().right().get().copy(), false);
            }

            for (int i = 0; i < 4; i++) {
               ItemStack slotStack = this.inventory.getStackInSlot(i);
               ItemStack remainder = BnCItemUtils.getIngredientRemainder(slotStack);
               if (!remainder.isEmpty()) {
                  this.ejectIngredientRemainder(remainder);
               }

               this.inventory.extractItem(i, 1, false);
            }

            this.setRecipeUsed(recipe);
            return true;
         }
      }
   }

   public List<ItemStack> extractInGui(ItemStack slotIn, int maxTakeAmount) {
      return this.fluidExtract(slotIn, maxTakeAmount, true, false);
   }

   public List<ItemStack> extractInWorld(ItemStack slotIn, int maxTakeAmount, boolean isCreative) {
      return this.fluidExtract(slotIn, maxTakeAmount, false, isCreative);
   }

   private List<ItemStack> fluidExtract(ItemStack slotIn, int maxTakeAmount, boolean inGui, boolean isCreative) {
      if (!slotIn.isEmpty()
         && !this.deferFluidExtraction
         && this.inventory.getStackInSlot(5).getCount() < Math.min(this.inventory.getSlotLimit(5), this.inventory.getStackInSlot(5).getMaxStackSize())) {
         Optional<KegPouringRecipe> recipe = this.getPouringRecipe(slotIn);
         boolean changed = false;
         List<ItemStack> outputs = new ArrayList<>();
         this.currentlyOperating = true;
         if (recipe.isPresent() && (this.fluidTank.isEmpty() || this.fluidTank.getAbstractedFluid().fluid() == recipe.get().getRawFluid().fluid())) {
            ItemStack resultItem = recipe.get().assemble(this.recipeWrapper);
            if (!BnCItemUtils.isSameItem(slotIn, recipe.get().getContainer(resultItem))
               || recipe.get().getRawFluid().amount() > this.fluidTank.getAbstractedFluid().amount()
               || inGui && !this.inventory.getStackInSlot(5).isEmpty() && !ItemStack.isSameItemSameTags(resultItem, this.inventory.getStackInSlot(5))) {
               if (recipe.filter(KegPouringRecipe::canFill).isPresent()
                  && (
                     recipe.get().isStrict() && ItemStack.isSameItemSameTags(resultItem, slotIn)
                        || !recipe.get().isStrict() && BnCItemUtils.isSameItem(slotIn, resultItem)
                  )
                  && (
                     this.fluidTank.isEmpty()
                        || this.fluidTank.getAbstractedFluid().matches(recipe.get().getFluid(slotIn))
                           && this.fluidTank.getAbstractedFluid().amount() < this.fluidTank.getFluidCapacity()
                  )
                  && (
                     !inGui
                        || this.inventory.getStackInSlot(5).isEmpty()
                        || ItemStack.isSameItemSameTags(recipe.get().getContainer(slotIn), this.inventory.getStackInSlot(5))
                  )) {
                  // Port fix: upstream clamps to capacity / per-item amount, so a part-full keg could overfill (the
                  // extra fluid was voided by the tank). Clamp to the space that's left instead.
                  long space = this.fluidTank.getFluidCapacity() - this.fluidTank.getAbstractedFluid().amount();
                  int containerAmount = (int)Mth.clamp(
                     Math.min((long)Math.min(slotIn.getCount(), recipe.get().getContainer(slotIn).getMaxStackSize()), space / recipe.get().getLoaderAmount()),
                     1L,
                     (long)maxTakeAmount
                  );
                  if (space >= recipe.get().getLoaderAmount()) {
                     this.fluidTank
                        .fill(
                           new AbstractedFluidStack(
                              recipe.get().getFluid(slotIn).fluid(),
                              recipe.get().getRawFluid().amount() * (long)containerAmount,
                              recipe.get().getFluid(slotIn).tag(),
                              recipe.get().getUnit(),
                              null
                           ),
                           false
                        );
                     if (!isCreative) {
                        ItemStack recipeItem = recipe.get().getContainer(slotIn);
                        int overflow = containerAmount;

                        while (overflow > 0 && !slotIn.isEmpty()) {
                           ItemStack newResult = BnCItemUtils.copyWithCount(recipeItem, Math.min(Math.min(slotIn.getCount(), maxTakeAmount), overflow));
                           outputs.add(newResult);
                           overflow -= newResult.getCount();
                           slotIn.shrink(newResult.getCount());
                        }

                        if (!slotIn.isEmpty()) {
                           outputs.add(slotIn);
                        }
                     } else {
                        outputs.add(slotIn);
                     }

                     changed = true;
                  }
               }
            } else {
               int containerAmount = (int)Mth.clamp(
                  (long)Math.min(Math.min(slotIn.getCount(), resultItem.getMaxStackSize()), maxTakeAmount),
                  1L,
                  this.fluidTank.getAbstractedFluid().amount() / recipe.get().getLoaderAmount()
               );
               this.fluidTank.drain(recipe.get().getRawFluid().amount() * (long)containerAmount, recipe.get().getUnit(), false);
               if (!isCreative) {
                  long overflow = containerAmount;

                  while (overflow > 0L && (!inGui || outputs.isEmpty()) && !slotIn.isEmpty()) {
                     ItemStack newResult = BnCItemUtils.copyWithCount(resultItem, (int)Math.min((long)Math.min(slotIn.getCount(), maxTakeAmount), overflow));
                     outputs.add(newResult);
                     overflow -= newResult.getCount();
                     slotIn.shrink(newResult.getCount());
                  }

                  if (!slotIn.isEmpty()) {
                     outputs.add(slotIn);
                  }
               } else {
                  outputs.add(slotIn);
               }

               changed = true;
            }

            if (changed) {
               this.inventoryChanged();
            }
         }

         if (outputs.isEmpty() && recipe.isEmpty()) {
            outputs.addAll(this.fluidExtractFromContainer(slotIn, maxTakeAmount, inGui, isCreative));
         }

         this.currentlyOperating = false;
         return outputs;
      } else {
         return List.of();
      }
   }

   /**
    * Fluid containers without a pouring recipe (Create's honey bucket, other modded tanks): pour them into the keg, or
    * fill them from it. Port: upstream's version leans on 1.21 transfer-API return values (it could not start an empty
    * keg from such a container); this is the same idea written against Forge's IFluidHandlerItem, one item at a time.
    * As upstream, only fluids that some pouring recipe knows can go in.
    */
   private List<ItemStack> fluidExtractFromContainer(ItemStack slotIn, int maxTakeAmount, boolean inGui, boolean isCreative) {
      List<ItemStack> outputs = new ArrayList<>();
      ItemStack single = BnCItemUtils.copyWithCount(slotIn, 1);
      AbstractedFluidTank item = BrewinAndChewin.getHelper().getFluidContainerFromItem(single);
      if (item == null || slotIn.isEmpty() || maxTakeAmount < 1) {
         return outputs;
      }

      AbstractedFluidStack tankFluid = this.fluidTank.getAbstractedFluid();
      AbstractedFluidStack itemFluid = item.getAbstractedFluid();
      boolean outputFree = !inGui || this.inventory.getStackInSlot(5).isEmpty();
      if (!itemFluid.isEmpty() && (tankFluid.isEmpty() || tankFluid.matches(itemFluid))) {
         AbstractedFluidStack known = tankFluid.isEmpty() ? itemFluid : tankFluid;
         boolean pourable = this.level != null
            && this.level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().anyMatch(r -> r.getFluid(slotIn).matches(known));
         long space = this.fluidTank.getFluidCapacity() - tankFluid.amount();
         if (pourable && space > 0L) {
            AbstractedFluidStack drained = item.drain(space, FluidUnit.getLoaderUnit(), true);
            long accepted = this.fluidTank.fillAmount(drained, true);
            if (accepted > 0L) {
               AbstractedFluidStack moved = item.drain(accepted, FluidUnit.getLoaderUnit(), false);
               this.fluidTank.fillAmount(moved, false);
               ItemStack emptied = item.getContainer();
               if (outputFree || ItemStack.isSameItemSameTags(emptied, this.inventory.getStackInSlot(5))) {
                  if (!isCreative) {
                     slotIn.shrink(1);
                     outputs.add(emptied);
                  }
                  if (!slotIn.isEmpty() || isCreative) {
                     outputs.add(slotIn);
                  }
                  this.inventoryChanged();
               }
            }
         }
      } else if (!tankFluid.isEmpty() && item.isFluidValid(tankFluid)) {
         AbstractedFluidStack offered = this.fluidTank.drain(item.getFluidCapacity(), FluidUnit.getLoaderUnit(), true);
         long accepted = item.fillAmount(offered, true);
         if (accepted > 0L && (outputFree || this.inventory.getStackInSlot(5).is(single.getItem()))) {
            AbstractedFluidStack moved = this.fluidTank.drain(accepted, FluidUnit.getLoaderUnit(), false);
            item.fillAmount(moved, false);
            if (!isCreative) {
               slotIn.shrink(1);
               outputs.add(item.getContainer());
            }
            if (!slotIn.isEmpty() || isCreative) {
               outputs.add(slotIn);
            }
            this.inventoryChanged();
         }
      }

      return outputs;
   }

   public Optional<KegPouringRecipe> getPouringRecipe(ItemStack slot) {
      return this.level == null
         ? Optional.empty()
         : this.level
            .getRecipeManager()
            .getAllRecipesFor(BnCRecipeTypes.KEG_POURING)
            .stream()
            .sorted(Comparator.comparingInt(value -> value.isStrict() ? 0 : 1))
            .filter(
               r -> {
                  boolean containerCheck = false;
                  boolean resultCheck = false;
                  boolean fluidCheck = false;
                  if (r.isStrict() && ItemStack.isSameItemSameTags(r.getContainer(), slot) || !r.isStrict() && r.getContainer().getItem() == slot.getItem()) {
                     containerCheck = true;
                  }

                  if (!containerCheck
                     && r.canFill()
                     && (
                        r.isStrict() && ItemStack.isSameItemSameTags(r.assemble(this.recipeWrapper), slot)
                           || !r.isStrict() && r.assemble(this.recipeWrapper).getItem() == slot.getItem()
                     )) {
                     resultCheck = true;
                  }

                  if (this.recipeWrapper.getFluid().isEmpty()
                     || containerCheck && r.getRawFluid().fluid() == this.recipeWrapper.getFluid().fluid()
                     || r.getFluid(slot).matches(this.recipeWrapper.getFluid())) {
                     fluidCheck = true;
                  }

                  return (containerCheck || resultCheck) && fluidCheck;
               }
            )
            .findFirst();
   }

   /**
    * Counts heat and freeze sources in the 5x5x5 area around the keg, plus the biome and dimension. Port: one plain
    * loop, not upstream's streams, and the Blaze Burner rule (alcohol.md 2.6/2.7) built in. The shape matters:
    * Create Central Kitchen 1.3.11.c ships a required mixin into this method (written for 2.0) with
    * defaultRequire 1, so it needs a direct Level.getBlockState call and a second BlockState.is(TagKey) call with a
    * second BlockState local in scope, or the game crashes at boot. Here both its injectors land harmlessly: the first
    * swaps idle burners for air (as withoutIdleBurner does), the second only touches the freeze-source check.
    */
   public void updateTemperature() {
      int heat = 0;
      int cold = 0;

      for (BlockPos pos : BlockPos.betweenClosed(this.worldPosition.offset(-RANGE, -RANGE, -RANGE), this.worldPosition.offset(RANGE, RANGE, RANGE))) {
         BlockState state = this.level.getBlockState(pos);
         BlockState source = BnCHeatSources.withoutIdleBurner(state);
         if (source.is(ModTags.HEAT_SOURCES) && BnCHeatSources.isActive(source)) {
            heat++;
         }

         if (source.is(BnCTags.Blocks.FREEZE_SOURCES) && BnCHeatSources.isActive(source)) {
            cold++;
         }
      }

      if (BnCConfiguration.COMMON_CONFIG.get().keg().biomeTemp()) {
         Holder<Biome> biome = this.level.getBiome(this.worldPosition);
         if (biome.isBound()) {
            float biomeTemperature = biome.value().getBaseTemperature();
            if (biomeTemperature <= 0.0F) {
               cold++;
            } else if (biomeTemperature == 2.0F) {
               heat++;
            }
         }
      }

      int temp = heat - cold;
      if (BnCConfiguration.COMMON_CONFIG.get().keg().dimTemp() && this.level.dimensionType().ultraWarm()) {
         temp += 2;
      }

      if (!this.initialisedTemperature || temp != this.kegTemperature) {
         this.kegTemperature = temp;
         this.initialisedTemperature = true;
         this.inventoryChanged();
      }
   }

   public int getTemperature() {
      BnCConfiguration.Common.Keg keg = BnCConfiguration.COMMON_CONFIG.get().keg();
      if (this.kegTemperature <= -keg.cold()) {
         return 1;
      } else if (this.kegTemperature <= -keg.chilly()) {
         return 2;
      } else if (this.kegTemperature < keg.warm()) {
         return 3;
      } else {
         return this.kegTemperature < keg.hot() ? 4 : 5;
      }
   }

   protected void ejectIngredientRemainder(ItemStack remainderStack) {
      Direction direction = this.getBlockState().getValue(KegBlock.FACING).getCounterClockWise();
      double x = (double)this.worldPosition.getX() + 0.5 + (double)direction.getStepX() * 0.25;
      double y = (double)this.worldPosition.getY() + 0.7;
      double z = (double)this.worldPosition.getZ() + 0.5 + (double)direction.getStepZ() * 0.25;
      ItemUtils.spawnItemEntity(
         this.level, remainderStack, x, y, z, (double)((float)direction.getStepX() * 0.08F), 0.25, (double)((float)direction.getStepZ() * 0.08F)
      );
   }

   @Override
   public void setRecipeUsed(@Nullable Recipe<?> recipe) {
      if (recipe != null) {
         ResourceLocation recipeID = recipe.getId();
         this.usedRecipeTracker.addTo(recipeID, 1);
      }
   }

   @Nullable
   @Override
   public Recipe<?> getRecipeUsed() {
      return null;
   }

   @Override
   public void awardUsedRecipes(Player player) {
      List<Recipe<?>> usedRecipes = this.getUsedRecipesAndPopExperience(player.level, player.position());
      player.awardRecipes(usedRecipes);
      this.usedRecipeTracker.clear();
   }

   public List<Recipe<?>> getUsedRecipesAndPopExperience(Level level, Vec3 pos) {
      List<Recipe<?>> list = Lists.newArrayList();

      for (Entry<ResourceLocation> entry : this.usedRecipeTracker.object2IntEntrySet()) {
         level.getRecipeManager().byKey(entry.getKey()).ifPresent(recipe -> {
            list.add(recipe);
            if (recipe instanceof KegFermentingRecipe fermenting && level instanceof ServerLevel serverLevel) {
               splitAndSpawnExperience(serverLevel, pos, entry.getIntValue(), fermenting.getExperience());
            }
         });
      }

      return list;
   }

   private static void splitAndSpawnExperience(ServerLevel level, Vec3 pos, int craftedAmount, float experience) {
      int expTotal = Mth.floor((float)craftedAmount * experience);
      float expFraction = Mth.frac((float)craftedAmount * experience);
      if (expFraction > 0.0F && Math.random() < (double)expFraction) {
         expTotal++;
      }

      ExperienceOrb.award(level, pos, expTotal);
   }

   public AbstractedItemHandler getInventory() {
      return this.inventory;
   }

   public AbstractedFluidTank getFluidTank() {
      return this.fluidTank;
   }

   public NonNullList<ItemStack> getDroppableInventory() {
      NonNullList<ItemStack> drops = NonNullList.create();

      for (int i = 0; i < 6; i++) {
         drops.add(this.inventory.getStackInSlot(i));
      }

      drops.addAll(this.pendingDrops);
      return drops;
   }

   @Override
   public Component getName() {
      return this.customName != null ? this.customName : BnCTextUtils.getTranslation("container.keg");
   }

   @Override
   public Component getDisplayName() {
      return this.getName();
   }

   @Nullable
   @Override
   public Component getCustomName() {
      return this.customName;
   }

   public void setCustomName(Component name) {
      this.customName = name;
   }

   @Override
   public AbstractContainerMenu createMenu(int id, Inventory player, Player entity) {
      return new KegMenu(id, player, this, this.kegData);
   }

   @Override
   public CompoundTag getUpdateTag() {
      return this.writeUpdateTag(new CompoundTag());
   }

   @NotNull
   @Override
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
      if (cap == ForgeCapabilities.ITEM_HANDLER) {
         return side == Direction.UP ? this.inputCap.cast() : this.outputCap.cast();
      }
      if (cap == ForgeCapabilities.FLUID_HANDLER) {
         return this.fluidCap.cast();
      }
      return super.getCapability(cap, side);
   }

   @Override
   public void invalidateCaps() {
      super.invalidateCaps();
      this.inputCap.invalidate();
      this.outputCap.invalidate();
      this.fluidCap.invalidate();
   }

   private AbstractedItemHandler createHandler() {
      return BrewinAndChewin.getHelper().createKegInventory(6, (handler, slot) -> {
         if (this.level != null && !this.level.isClientSide() && (slot == 4 || slot == 5) && !this.currentlyOperating) {
            this.deferFluidExtraction = true;
         }

         if (slot >= 0 && slot < 5) {
            this.checkNewRecipe = true;
         }

         this.inventoryChanged();
      });
   }

   private AbstractedFluidTank createFluidTank() {
      return BrewinAndChewin.getHelper().createKegTank(BnCConfiguration.COMMON_CONFIG.get().keg().localizedCapacity(), () -> {
         AbstractedItemHandler handler = this.inventory;
         if (this.level != null && !this.level.isClientSide() && !this.currentlyOperating && !this.deferFluidExtraction) {
            List<ItemStack> out = this.extractInGui(handler.getStackInSlot(4), handler.getSlotLimit(5));
            if (!out.isEmpty()) {
               handler.insertItem(5, out.get(0), false);
            }
         }

         this.inventoryChanged();
         this.checkNewRecipe = true;
      });
   }

   @Override
   protected void inventoryChanged() {
      if (this.level == null) {
         return;
      }
      super.inventoryChanged();
   }

   private ContainerData createIntArray() {
      return new ContainerData() {
         @Override
         public int get(int index) {
            return switch (index) {
               case 0 -> KegBlockEntity.this.fermentTime;
               case 1 -> KegBlockEntity.this.fermentTimeTotal;
               default -> 0;
            };
         }

         @Override
         public void set(int index, int value) {
            switch (index) {
               case 0:
                  KegBlockEntity.this.fermentTime = value;
                  break;
               case 1:
                  KegBlockEntity.this.fermentTimeTotal = value;
            }
         }

         @Override
         public int getCount() {
            return 3;
         }
      };
   }
}
