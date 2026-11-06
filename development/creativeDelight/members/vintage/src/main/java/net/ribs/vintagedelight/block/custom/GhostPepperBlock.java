package net.ribs.vintagedelight.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.ribs.vintagedelight.item.ModItems;
import net.ribs.vintagedelight.item.ModTags;

public class GhostPepperBlock extends CropBlock {
   public static final int MAX_AGE = 7;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

   public GhostPepperBlock(Properties properties) {
      super(properties);
   }

   protected ItemLike getBaseSeedId() {
      return (ItemLike)ModItems.GHOST_PEPPER_SEEDS.get();
   }

   public IntegerProperty getAgeProperty() {
      return AGE;
   }

   public int getMaxAge() {
      return 7;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      pBuilder.add(new Property[]{AGE});
   }

   // Port: the hard-coded nylium check is the block tag #vintagedelight:ghost_pepper_soil (the pack fills it)
   protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return state.is(ModTags.GHOST_PEPPER_SOIL);
   }

   // Port: Forge's BushBlock.canSurvive asks the soil's canSustainPlant, and farmland sustains every crop, so the seeds
   // planted on farmland past mayPlaceOn. The soil tag is now the only soil rule (vintageport #7); the light check stays.
   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockPos below = pos.below();
      return this.mayPlaceOn(level.getBlockState(below), level, below) && super.canSurvive(state, level, pos);
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      int currentAge = (Integer)state.getValue(AGE);
      if (currentAge == 7) {
         int quantity = 1;
         popResource(level, pos, new ItemStack((ItemLike)ModItems.GHOST_PEPPER.get(), quantity));
         level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
         level.setBlock(pos, (BlockState)state.setValue(AGE, 5), 2);
         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return super.use(state, level, pos, player, hand, hit);
      }
   }
}
