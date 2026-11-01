package umpaz.brewinandchewin.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.block.CoasterBlock;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.tag.BnCTags;
import umpaz.brewinandchewin.common.utility.BnCItemUtils;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;

public class CoasterBlockEntity extends SyncedBlockEntity {
   public final NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);

   public CoasterBlockEntity(BlockPos pos, BlockState state) {
      super(BnCBlockEntityTypes.COASTER, pos, state);
   }

   /** Port: 1.21 ItemInteractionResult -> InteractionResult, PASS_TO_DEFAULT_BLOCK_INTERACTION -> PASS. */
   public InteractionResult useItemOn(ItemStack stack, Level level, BlockState state, BlockPos pos, Player player, InteractionHand hand) {
      if (!player.getAbilities().mayBuild) {
         return InteractionResult.PASS;
      } else if (state.getValue(CoasterBlock.INVISIBLE) && stack.is(BnCItems.COASTER)) {
         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }

         level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
         level.setBlockAndUpdate(pos, state.setValue(CoasterBlock.INVISIBLE, false));
         return InteractionResult.SUCCESS;
      } else {
         return state.getValue(CoasterBlock.SIZE) < 4 && this.addItem(level, pos, state, stack, player.getAbilities().instabuild, state.getValue(CoasterBlock.SIZE))
            ? InteractionResult.SUCCESS
            : InteractionResult.PASS;
      }
   }

   public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (!player.getAbilities().mayBuild) {
         return InteractionResult.PASS;
      } else if (state.getValue(CoasterBlock.SIZE) <= 0) {
         return InteractionResult.PASS;
      } else if (player.isShiftKeyDown() && !state.getValue(CoasterBlock.INVISIBLE)) {
         ItemStack coaster = new ItemStack(BnCItems.COASTER);
         if (!player.getAbilities().instabuild && !player.addItem(coaster)) {
            player.drop(coaster, false);
         }

         level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
         level.setBlockAndUpdate(pos, state.setValue(CoasterBlock.INVISIBLE, true));
         return InteractionResult.SUCCESS;
      } else {
         int count = state.getValue(CoasterBlock.SIZE);
         if (!player.getAbilities().instabuild && !player.addItem(this.inventory.get(count - 1))) {
            player.drop(this.inventory.get(count - 1), false);
         }

         BlockState replaceWith = Blocks.AIR.defaultBlockState();
         if (!state.getValue(CoasterBlock.INVISIBLE) || count > 1) {
            replaceWith = state.setValue(CoasterBlock.SIZE, state.getValue(CoasterBlock.SIZE) - 1);
         }

         level.setBlockAndUpdate(pos, replaceWith);
         this.inventory.set(count - 1, ItemStack.EMPTY);
         this.inventoryChanged();
         return InteractionResult.SUCCESS;
      }
   }

   /** Port: with [coaster] taggedItemsOnly, only items with a coaster model ({@link BnCTags.Items#PLACEABLE_ON_COASTER}). */
   public static boolean accepts(ItemStack stack) {
      return !stack.isEmpty() && (!BnCConfiguration.COMMON_CONFIG.get().coaster().taggedItemsOnly() || stack.is(BnCTags.Items.PLACEABLE_ON_COASTER));
   }

   private boolean addItem(Level level, BlockPos pos, BlockState state, ItemStack stack, boolean instabuild, int index) {
      if (!accepts(stack)) {
         return false;
      } else {
         level.setBlock(pos, state.setValue(CoasterBlock.SIZE, index + 1), 3);
         this.inventory.set(index, BnCItemUtils.copyWithCount(stack, 1));
         this.inventoryChanged();
         if (!instabuild) {
            stack.shrink(1);
         }

         return true;
      }
   }

   @Override
   public void load(CompoundTag nbt) {
      super.load(nbt);
      this.inventory.clear();
      ContainerHelper.loadAllItems(nbt, this.inventory);
   }

   @Override
   protected void saveAdditional(CompoundTag nbt) {
      super.saveAdditional(nbt);
      ContainerHelper.saveAllItems(nbt, this.inventory);
   }

   @Override
   public AABB getRenderBoundingBox() {
      BlockPos pos = this.getBlockPos();
      return new AABB(pos, pos.offset(1, 2, 1));
   }

   public NonNullList<ItemStack> getItems() {
      return this.inventory;
   }
}
