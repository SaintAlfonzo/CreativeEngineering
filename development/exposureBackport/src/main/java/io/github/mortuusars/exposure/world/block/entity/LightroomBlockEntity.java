package io.github.mortuusars.exposure.world.block.entity;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.world.block.LightroomBlock;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.inventory.LightroomMenu;
import io.github.mortuusars.exposure.world.item.ChromaticSheetItem;
import io.github.mortuusars.exposure.world.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.world.level.LevelUtil;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.world.lightroom.PrintingMode;
import io.github.mortuusars.exposure.world.lightroom.PrintingProcess;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import io.github.mortuusars.exposure.backport.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import io.github.mortuusars.exposure.backport.component.CustomData;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.mortuusars.exposure.backport.util.Backport;

public class LightroomBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
   public static final int CONTAINER_DATA_SIZE = 3;
   public static final int CONTAINER_DATA_PROGRESS_ID = 0;
   public static final int CONTAINER_DATA_PRINT_TIME_ID = 1;
   public static final int CONTAINER_DATA_SELECTED_FRAME_ID = 2;
   protected final ContainerData containerData = new ContainerData() {
      public int get(int id) {
         return switch (id) {
            case 0 -> LightroomBlockEntity.this.progress;
            case 1 -> LightroomBlockEntity.this.printTime;
            case 2 -> LightroomBlockEntity.this.getSelectedFrameIndex();
            default -> 0;
         };
      }

      public void set(int id, int value) {
         if (id == 0) {
            LightroomBlockEntity.this.progress = value;
         } else if (id == 1) {
            LightroomBlockEntity.this.printTime = value;
         } else if (id == 2) {
            LightroomBlockEntity.this.setSelectedFrameIndex(value);
         }

         LightroomBlockEntity.this.setChanged();
      }

      public int getCount() {
         return 3;
      }
   };
   public static final Map<DyeColor, Integer> DYE_SLOTS = Map.of(DyeColor.CYAN, 2, DyeColor.MAGENTA, 3, DyeColor.YELLOW, 4, DyeColor.BLACK, 5);
   protected NonNullList<ItemStack> items = NonNullList.withSize(7, ItemStack.EMPTY);
   protected UUID lastPlayerId = Util.NIL_UUID;
   protected int selectedFrameIndex;
   protected int progress;
   protected int printTime;
   protected int storedExperience;
   protected boolean advanceFrame;
   protected PrintingMode printingMode = PrintingMode.REGULAR;
   @Nullable
   protected Frame selectedFrame;

   public LightroomBlockEntity(BlockPos pos, BlockState blockState) {
      super(Exposure.BlockEntityTypes.LIGHTROOM.get(), pos, blockState);
   }

   public static <T extends BlockEntity> void serverTick(Level ignoredLevel, BlockPos ignoredBlockPos, BlockState ignoredBlockState, T blockEntity) {
      if (blockEntity instanceof LightroomBlockEntity lightroomBlockEntity) {
         lightroomBlockEntity.tick();
      }
   }

   protected void tick() {
      if (this.printTime <= 0 || !this.canPrint()) {
         this.stopPrintingProcess();
      } else if (this.progress < this.printTime) {
         this.progress++;
         if (this.progress % 55 == 0 && this.printTime - this.progress > 12) {
            this.playPrintingSound();
         }
      } else {
         Frame frame = this.getSelectedFrame();
         Preconditions.checkState(frame != null, "Frame cannot be null here because of 'canPrint' check. If it is - something went wrong.");
         PrintingProcess process = this.getPrintingProcess(frame);
         this.printFrame(frame, process, true);
         this.stopPrintingProcess();
      }
   }

   public void setLastPlayer(Player player) {
      this.lastPlayerId = player.getUUID();
   }

   public float getProgressPercentage() {
      return this.progress >= 1 && this.printTime >= 1 ? (float)this.progress / (float)this.printTime : 0.0F;
   }

   public boolean isPrinting() {
      return this.level != null && (Boolean)this.level.getBlockState(this.getBlockPos()).getValue(LightroomBlock.PRINTING);
   }

   public boolean isAdvancingFrameOnPrint() {
      return this.advanceFrame;
   }

   protected boolean canEjectFilm() {
      if (this.level != null && !this.level.isClientSide && !this.getItem(0).isEmpty()) {
         BlockPos pos = this.getBlockPos();
         Direction facing = (Direction)this.level.getBlockState(pos).getValue(LightroomBlock.FACING);
         return !this.level.getBlockState(pos.relative(facing)).canOcclude();
      } else {
         return false;
      }
   }

   protected void ejectFilm() {
      if (this.level != null && !this.level.isClientSide && !this.getItem(0).isEmpty()) {
         BlockPos pos = this.getBlockPos();
         Direction facing = (Direction)this.level.getBlockState(pos).getValue(LightroomBlock.FACING);
         ItemStack filmStack = this.removeItem(0, 1);
         Vec3i normal = facing.getNormal();
         Vec3 point = Vec3.atCenterOf(pos)
            .add((double)((float)normal.getX() * 0.75F), (double)((float)normal.getY() * 0.75F), (double)((float)normal.getZ() * 0.75F));
         ItemEntity itemEntity = new ItemEntity(this.level, point.x, point.y, point.z, filmStack);
         itemEntity.setDeltaMovement(
            (double)((float)normal.getX() * 0.05F), (double)((float)normal.getY() * 0.05F + 0.15F), (double)((float)normal.getZ() * 0.05F)
         );
         itemEntity.setDefaultPickUpDelay();
         this.level.addFreshEntity(itemEntity);
         this.inventoryContentsChanged(0);
      }
   }

   public int getSelectedFrameIndex() {
      return this.selectedFrameIndex;
   }

   public void setSelectedFrameIndex(int index) {
      if (this.selectedFrameIndex != index) {
         this.selectedFrameIndex = index;
         this.stopPrintingProcess();
      }

      ItemStack filmStack = this.getItem(0);
      if (filmStack.getItem() instanceof DevelopedFilmItem developedFilm && developedFilm.hasFrameAt(filmStack, this.getSelectedFrameIndex())) {
         this.selectedFrame = developedFilm.getStoredFrames(filmStack).get(this.getSelectedFrameIndex());
         return;
      }

      this.selectedFrame = null;
   }

   public PrintingMode getActualPrintingMode() {
      return this.isRefracted() ? this.printingMode : PrintingMode.REGULAR;
   }

   public PrintingMode getPrintingMode() {
      return this.printingMode;
   }

   public void setPrintMode(PrintingMode printingMode) {
      this.printingMode = printingMode;
      this.stopPrintingProcess();
      this.setChanged();
   }

   @Nullable
   public Frame getSelectedFrame() {
      return this.selectedFrame;
   }

   public boolean isRefracted() {
      return this.level != null && (Boolean)this.level.getBlockState(this.getBlockPos()).getValue(LightroomBlock.REFRACTED);
   }

   public void startPrintingProcess(boolean advanceFrameOnFinish) {
      if (this.canPrint()) {
         Frame frame = this.getSelectedFrame();
         Preconditions.checkState(frame != null, "Frame cannot be null here because of 'canPrint' check. If it is - something went wrong.");
         PrintingProcess process = this.getPrintingProcess(frame);
         int time = process.getPrintTime();
         this.printTime = this.isRefracted() && process.isRegular() ? time * 3 : time;
         this.advanceFrame = advanceFrameOnFinish;
         if (this.level != null) {
            BlockState state = this.level.getBlockState(this.getBlockPos());
            if (state.getBlock() instanceof LightroomBlock && !(Boolean)state.getValue(LightroomBlock.PRINTING)) {
               this.level.setBlock(this.getBlockPos(), (BlockState)state.setValue(LightroomBlock.PRINTING, true), 2);
               this.playPrintingStartedSound();
            }
         }
      }
   }

   public void stopPrintingProcess() {
      this.progress = 0;
      this.printTime = 0;
      this.advanceFrame = false;
      if (this.level != null) {
         BlockState state = this.level.getBlockState(this.getBlockPos());
         if (state.getBlock() instanceof LightroomBlock && (Boolean)state.getValue(LightroomBlock.PRINTING)) {
            this.level.setBlock(this.getBlockPos(), (BlockState)state.setValue(LightroomBlock.PRINTING, false), 2);
         }
      }
   }

   public boolean canPrint() {
      Frame frame = this.getSelectedFrame();
      if (frame == null) {
         return false;
      } else {
         ItemStack paperStack = this.getItem(1);
         if (paperStack.isEmpty()) {
            return false;
         } else {
            PrintingProcess process = this.getPrintingProcess(frame);
            return this.isPaperValidForPrint(frame, process)
               && this.canOutputToResultSlot(frame, process)
               && this.hasDyesForPrint(frame, process)
               && this.hasSufficientLightLevel();
         }
      }
   }

   public boolean hasSufficientLightLevel() {
      if (this.level == null) {
         return false;
      } else {
         int requiredLightLevel = (Integer)Config.Server.LIGHTROOM_LIGHT_REQUIREMENT.get();
         BlockPos above = this.getBlockPos().above();
         return !this.isRefracted()
            ? LevelUtil.getLightLevelAt(this.level, above) >= requiredLightLevel
            : LevelUtil.getLightLevelAt(this.level, above.above()) >= requiredLightLevel
               || LevelUtil.getLightLevelAt(this.level, above.relative(Direction.NORTH)) >= requiredLightLevel
               || LevelUtil.getLightLevelAt(this.level, above.relative(Direction.EAST)) >= requiredLightLevel
               || LevelUtil.getLightLevelAt(this.level, above.relative(Direction.SOUTH)) >= requiredLightLevel
               || LevelUtil.getLightLevelAt(this.level, above.relative(Direction.WEST)) >= requiredLightLevel;
      }
   }

   public boolean canPrintInCreativeMode() {
      Frame frame = this.getSelectedFrame();
      if (frame == null) {
         return false;
      } else {
         PrintingProcess process = this.getPrintingProcess(frame);
         return this.canOutputToResultSlot(frame, process);
      }
   }

   protected PrintingProcess getPrintingProcess(@NotNull Frame frame) {
      return switch (this.getActualPrintingMode()) {
         case REGULAR -> PrintingProcess.fromExposureType(frame.type());
         case CHROMATIC -> PrintingProcess.fromChromaticStep(this.getChromaticStep(this.getItem(1)));
      };
   }

   protected boolean isPaperValidForPrint(Frame frame, PrintingProcess process) {
      ItemStack paperStack = this.getItem(1);
      if (paperStack.isEmpty()) {
         return false;
      } else if (!process.isChromatic()) {
         return paperStack.is(Exposure.Tags.Items.PHOTO_PAPERS);
      } else if (paperStack.is(Exposure.Tags.Items.PHOTO_PAPERS)) {
         return true;
      } else {
         if (paperStack.getItem() instanceof ChromaticSheetItem chromaticSheet && !chromaticSheet.canCombine(paperStack)) {
            return true;
         }

         return false;
      }
   }

   protected boolean hasDyesForPrint(Frame frame, PrintingProcess process) {
      for (DyeColor requiredDye : process.getRequiredDyes()) {
         int slotIndex = DYE_SLOTS.get(requiredDye);
         if (this.getItem(slotIndex).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public boolean canOutputToResultSlot(Frame frame, PrintingProcess process) {
      ItemStack resultStack = this.getItem(6);
      if (process.isChromatic()) {
         return resultStack.isEmpty();
      } else if (resultStack.isEmpty() || resultStack.getItem() instanceof PhotographItem) {
         return true;
      } else {
         if (resultStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem && stackedPhotographsItem.canAddPhotograph(resultStack)) {
            return true;
         }

         return false;
      }
   }

   protected int getChromaticStep(ItemStack paper) {
      return paper.getItem() instanceof ChromaticSheetItem chromaticSheet ? chromaticSheet.getLayers(paper).size() : 0;
   }

   protected void printFrame(Frame frame, PrintingProcess process, boolean consumeIngredients) {
      ItemStack printResult = this.createPrintResult(frame, process);
      this.putPrintResultInOutputSlot(printResult);
      if (consumeIngredients) {
         this.consumePrintIngredients(frame, process);
         this.awardExperienceForPrint(frame, process, printResult);
      }

      this.onFramePrinted(frame, process, printResult);
   }

   public void printFrameInCreative() {
      Frame frame = this.getSelectedFrame();
      if (frame == null) {
         Exposure.LOGGER.error("Cannot creatively print a frame: No frame is selected.");
      } else {
         PrintingProcess process = this.getPrintingProcess(frame);
         this.printFrame(frame, process, false);
         if (this.getItem(1).getItem() instanceof ChromaticSheetItem) {
            this.getItem(1).shrink(1);
            this.setChanged();
         }
      }
   }

   protected ItemStack createPrintResult(Frame frame, PrintingProcess process) {
      if (process.isChromatic()) {
         ItemStack paperStack = this.getItem(1);
         ItemStack chromaticStack = paperStack.getItem() instanceof ChromaticSheetItem
            ? paperStack.copy()
            : new ItemStack((ItemLike)Exposure.Items.CHROMATIC_SHEET.get());
         ChromaticSheetItem chromaticItem = (ChromaticSheetItem)chromaticStack.getItem();
         chromaticItem.addLayer(chromaticStack, frame);
         ServerPlayer player = this.getLastOrClosestPlayer();
         if (chromaticItem.canCombine(chromaticStack)) {
            if (player != null) {
               return chromaticItem.combineIntoPhotograph(player, chromaticStack, true);
            }

            CompoundTag tag = new CompoundTag();
            tag.putBoolean("printed", true);
            DataComponents.CUSTOM_DATA.set(chromaticStack, CustomData.of(tag));
         }

         return chromaticStack;
      } else {
         ItemStack photographStack = new ItemStack((ItemLike)Exposure.Items.PHOTOGRAPH.get());
         Exposure.DataComponents.PHOTOGRAPH_FRAME.set(photographStack, frame);
         Exposure.DataComponents.PHOTOGRAPH_TYPE.set(photographStack, frame.type());
         return photographStack;
      }
   }

   @Nullable
   protected ServerPlayer getLastOrClosestPlayer() {
      if (this.getLevel() instanceof ServerLevel serverLevel) {
         Player var3 = serverLevel.getPlayerByUUID(this.lastPlayerId);
         if (var3 instanceof ServerPlayer) {
            return (ServerPlayer)var3;
         } else {
            BlockPos pos = this.getBlockPos();
            return serverLevel.players()
               .stream()
               .min(Comparator.comparingDouble(pl -> pl.distanceToSqr((double)pos.getX(), (double)pos.getY(), (double)pos.getZ())))
               .orElse(null);
         }
      } else {
         return null;
      }
   }

   protected void putPrintResultInOutputSlot(ItemStack printResult) {
      ItemStack resultStack = this.getItem(6);
      if (resultStack.isEmpty()) {
         resultStack = printResult;
      } else if (resultStack.getItem() instanceof PhotographItem) {
         StackedPhotographsItem stackedPhotographsItem = Exposure.Items.STACKED_PHOTOGRAPHS.get();
         ItemStack newStackedPhotographs = new ItemStack(stackedPhotographsItem);
         stackedPhotographsItem.addPhotographOnTop(newStackedPhotographs, resultStack);
         stackedPhotographsItem.addPhotographOnTop(newStackedPhotographs, printResult);
         resultStack = newStackedPhotographs;
      } else {
         if (!(resultStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem)) {
            Exposure.LOGGER.error("Unexpected item in result slot: {}", resultStack);
            return;
         }

         stackedPhotographsItem.addPhotographOnTop(resultStack, printResult);
      }

      this.setItem(6, resultStack);
   }

   protected void consumePrintIngredients(Frame frame, PrintingProcess process) {
      this.getItem(1).shrink(1);
      process.getRequiredDyes().forEach(dye -> this.getItem(DYE_SLOTS.get(dye)).shrink(1));
      this.setChanged();
   }

   protected void awardExperienceForPrint(Frame frame, PrintingProcess process, ItemStack result) {
      int xp = process.getExperiencePerPrint();
      if (xp > 0) {
         float variability = ThreadLocalRandom.current().nextFloat() * 0.3F + 1.0F;
         int variableXp = (int)Math.max(1.0, Math.ceil((double)((float)xp * variability)));
         this.storedExperience += variableXp;
      }
   }

   protected void onFramePrinted(Frame frame, PrintingProcess process, ItemStack result) {
      if (process.isRegular()) {
         frame.identifier().ifId(id -> ExposureServer.exposureRepository().update(id, exposure -> exposure.withTag(ExposureData.Tag::setPrinted)));
      }

      ServerPlayer var5 = this.getLastOrClosestPlayer();
      if (var5 instanceof ServerPlayer) {
         Exposure.CriteriaTriggers.FRAME_PRINTED.get().trigger(var5, this.getBlockPos(), frame, result);
      }

      if (this.advanceFrame) {
         this.advanceFrame();
      }

      this.playPrintingFinishedSound();
   }

   protected void advanceFrame() {
      ItemAndStack<DevelopedFilmItem> film = new ItemAndStack<>(this.getItem(0));
      int frames = film.getItem().getStoredFramesCount(film.getItemStack());
      if (this.getSelectedFrameIndex() >= frames - 1) {
         if (this.canEjectFilm()) {
            this.ejectFilm();
         }
      } else {
         this.setSelectedFrameIndex(this.getSelectedFrameIndex() + 1);
         this.setChanged();
      }
   }

   public void dropStoredExperience(@Nullable Player player) {
      if (this.level instanceof ServerLevel serverLevel && this.storedExperience > 0) {
         ExperienceOrb.award(serverLevel, Vec3.atCenterOf(this.getBlockPos()), this.storedExperience);
         this.storedExperience = 0;
         this.setChanged();
      }
   }

   protected void playPrintingStartedSound() {
      if (this.level != null) {
         this.level
            .playSound(
               null, this.getBlockPos(), Exposure.SoundEvents.LIGHTROOM_PRINT.get(), SoundSource.BLOCKS, 1.0F, this.level.getRandom().nextFloat() * 0.3F + 1.0F
            );
      }
   }

   protected void playPrintingSound() {
      if (this.level != null) {
         this.level
            .playSound(
               null, this.getBlockPos(), Exposure.SoundEvents.LIGHTROOM_PRINT.get(), SoundSource.BLOCKS, 1.0F, this.level.getRandom().nextFloat() * 0.3F + 1.0F
            );
      }
   }

   protected void playPrintingFinishedSound() {
      if (this.level != null) {
         this.level.playSound(null, this.getBlockPos(), Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
      }
   }

   @NotNull
   protected Component getDefaultName() {
      return Component.translatable("container.exposure.lightroom");
   }

   @NotNull
   protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
      return new LightroomMenu(containerId, inventory, this, this.containerData);
   }

   public boolean isItemValidForSlot(int slot, ItemStack stack) {
      if (slot == 0) {
         return stack.getItem() instanceof DevelopedFilmItem;
      } else if (slot == 2) {
         return stack.is(Exposure.Tags.Items.CYAN_PRINTING_DYES);
      } else if (slot == 3) {
         return stack.is(Exposure.Tags.Items.MAGENTA_PRINTING_DYES);
      } else if (slot == 4) {
         return stack.is(Exposure.Tags.Items.YELLOW_PRINTING_DYES);
      } else if (slot == 5) {
         return stack.is(Exposure.Tags.Items.BLACK_PRINTING_DYES);
      } else if (slot == 1) {
         if (stack.is(Exposure.Tags.Items.PHOTO_PAPERS)) {
            return true;
         } else {
            if (stack.getItem() instanceof ChromaticSheetItem chromatic && chromatic.getLayers(stack).size() < 3) {
               return true;
            }

            return false;
         }
      } else {
         return slot != 6
            ? false
            : stack.getItem() instanceof PhotographItem || stack.getItem() instanceof StackedPhotographsItem || stack.getItem() instanceof ChromaticSheetItem;
      }
   }

   protected void inventoryContentsChanged(int slot) {
      if (slot == 0) {
         this.setSelectedFrameIndex(0);
      }

      this.setChanged();
   }

   public boolean stillValid(@NotNull Player player) {
      return this.level != null
         && this.level.getBlockEntity(this.worldPosition) == this
         && player.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5)
            <= 64.0;
   }

   @NotNull
   public int[] getSlotsForFace(@NotNull Direction face) {
      return Lightroom.ALL_SLOTS;
   }

   public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStack, @Nullable Direction direction) {
      return direction == Direction.DOWN ? false : this.canPlaceItem(index, itemStack);
   }

   public boolean canPlaceItem(int index, ItemStack stack) {
      return index != 6 && this.isItemValidForSlot(index, stack) && super.canPlaceItem(index, stack);
   }

   public boolean canTakeItemThroughFace(int index, @NotNull ItemStack pStack, @NotNull Direction direction) {
      for (int outputSlot : Lightroom.OUTPUT_SLOTS) {
         if (index == outputSlot) {
            return true;
         }
      }

      return false;
   }

   public void load(CompoundTag tag) {
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      ContainerHelper.loadAllItems(tag, this.items);
      if (tag.contains("Inventory", 10)) {
         CompoundTag inventory = tag.getCompound("Inventory");
         ListTag itemsList = inventory.getList("Items", 10);

         for (int i = 0; i < itemsList.size(); i++) {
            CompoundTag itemTag = itemsList.getCompound(i);
            int slot = itemTag.getInt("Slot");
            if (slot >= 0 && slot < this.items.size()) {
               Backport.parseItemStack(itemTag).ifPresent(stack -> this.items.set(slot, stack));
            }
         }
      }

      this.setSelectedFrameIndex(tag.getInt("SelectedFrame"));
      this.progress = tag.getInt("Progress");
      this.printTime = tag.getInt("PrintTime");
      this.storedExperience = tag.getInt("PrintedPhotographsCount");
      this.advanceFrame = tag.getBoolean("AdvanceFrame");
      this.printingMode = PrintingMode.fromStringOrDefault(tag.getString("PrintMode"), PrintingMode.REGULAR);
      if (tag.contains("LastPlayerId", 11)) {
         this.lastPlayerId = tag.getUUID("LastPlayerId");
      }
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      ContainerHelper.saveAllItems(tag, this.items);
      if (this.getSelectedFrameIndex() > 0) {
         tag.putInt("SelectedFrame", this.getSelectedFrameIndex());
      }

      if (this.progress > 0) {
         tag.putInt("Progress", this.progress);
      }

      if (this.printTime > 0) {
         tag.putInt("PrintTime", this.printTime);
      }

      if (this.storedExperience > 0) {
         tag.putInt("PrintedPhotographsCount", this.storedExperience);
      }

      if (this.advanceFrame) {
         tag.putBoolean("AdvanceFrame", true);
      }

      if (this.printingMode != PrintingMode.REGULAR) {
         tag.putString("PrintMode", this.printingMode.getSerializedName());
      }

      if (!this.lastPlayerId.equals(Util.NIL_UUID)) {
         tag.putUUID("LastPlayerId", this.lastPlayerId);
      }
   }

   @NotNull
   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   protected void setItems(NonNullList<ItemStack> items) {
      this.items = items;
   }

   public int getContainerSize() {
      return 7;
   }

   public boolean isEmpty() {
      return this.getItems().stream().allMatch(ItemStack::isEmpty);
   }

   @NotNull
   public ItemStack getItem(int slot) {
      return (ItemStack)this.getItems().get(slot);
   }

   @NotNull
   public ItemStack removeItem(int slot, int amount) {
      ItemStack itemStack = ContainerHelper.removeItem(this.getItems(), slot, amount);
      if (!itemStack.isEmpty()) {
         this.inventoryContentsChanged(slot);
      }

      return itemStack;
   }

   @NotNull
   public ItemStack removeItemNoUpdate(int slot) {
      return ContainerHelper.takeItem(this.getItems(), slot);
   }

   public void setItem(int slot, ItemStack stack) {
      this.getItems().set(slot, stack);
      if (stack.getCount() > this.getMaxStackSize()) {
         stack.setCount(this.getMaxStackSize());
      }

      this.inventoryContentsChanged(slot);
   }

   public void clearContent() {
      this.getItems().clear();
      this.inventoryContentsChanged(-1);
   }

   public void setChanged() {
      super.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
      }
   }

   @Nullable
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @NotNull
   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   // Item handler capability: upstream registers sided wrappers through NeoForge's RegisterCapabilitiesEvent
   // (unsided InvWrapper for a null side). Forge 1.19.2 asks the block entity; its BaseContainerBlockEntity default
   // is the unsided one, kept for null.
   private net.minecraftforge.common.util.LazyOptional<? extends net.minecraftforge.items.IItemHandler>[] sidedItemHandlers =
           net.minecraftforge.items.wrapper.SidedInvWrapper.create(this, Direction.values());

   @Override
   public <T> net.minecraftforge.common.util.@NotNull LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.@NotNull Capability<T> capability, @Nullable Direction side) {
      if (!this.remove && side != null && capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER) {
         return this.sidedItemHandlers[side.ordinal()].cast();
      }
      return super.getCapability(capability, side);
   }

   @Override
   public void invalidateCaps() {
      super.invalidateCaps();
      for (net.minecraftforge.common.util.LazyOptional<?> handler : this.sidedItemHandlers) {
         handler.invalidate();
      }
   }

   @Override
   public void reviveCaps() {
      super.reviveCaps();
      this.sidedItemHandlers = net.minecraftforge.items.wrapper.SidedInvWrapper.create(this, Direction.values());
   }
}
