package net.ribs.vintagedelight.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.BlockHitResult;
import vectorwing.farmersdelight.common.registry.ModBlocks;

public class LushGrassBlock extends GrassBlock {
   // Pack addition: shears clear it, as on Immersive Weathering's grass block; sheared grass stops ticking (no spreading)
   public static final BooleanProperty FERTILE = BooleanProperty.create("fertile");

   public LushGrassBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(this.defaultBlockState().setValue(FERTILE, true));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(FERTILE);
   }

   public boolean isRandomlyTicking(BlockState state) {
      return state.getValue(FERTILE) && super.isRandomlyTicking(state);
   }

   public boolean isValidBonemealTarget(BlockGetter world, BlockPos pos, BlockState state, boolean isClient) {
      return world.getBlockState(pos.above()).isAir();
   }

   public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
      return true;
   }

   public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      BlockPos blockpos = pPos.above();
      ChunkGenerator chunkGenerator = pLevel.getChunkSource().getGenerator();
      // 1.19.2: the vanilla feature constants are holders already
      VegetationFeatures.FLOWER_FLOWER_FOREST.value().place(pLevel, chunkGenerator, pRandom, blockpos);
      VegetationFeatures.FLOWER_MEADOW.value().place(pLevel, chunkGenerator, pRandom, blockpos);
      VegetationFeatures.PATCH_GRASS.value().place(pLevel, chunkGenerator, pRandom, blockpos);
      VegetationFeatures.PATCH_TALL_GRASS.value().place(pLevel, chunkGenerator, pRandom, blockpos);
      VegetationFeatures.FLOWER_DEFAULT.value().place(pLevel, chunkGenerator, pRandom, blockpos);
   }

   public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      ItemStack itemInHand = player.getItemInHand(hand);
      Item usedItem = itemInHand.getItem();
      if (usedItem instanceof ShearsItem && state.getValue(FERTILE)) {
         if (world.isClientSide) {
            world.addDestroyBlockEffect(pos, Blocks.GRASS.defaultBlockState());
         } else {
            world.playSound(null, pos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.setBlock(pos, state.setValue(FERTILE, false), 11);
            itemInHand.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            world.gameEvent(player, GameEvent.SHEAR, pos);
         }
         return InteractionResult.sidedSuccess(world.isClientSide);
      }
      if (usedItem instanceof HoeItem && !world.isClientSide && world.getBlockState(pos.above()).isAir()) {
         world.setBlock(pos, ((Block)ModBlocks.RICH_SOIL_FARMLAND.get()).defaultBlockState(), 11);
         world.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
         itemInHand.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
         player.swing(hand, true);
         return InteractionResult.sidedSuccess(false);
      } else {
         return super.use(state, world, pos, player, hand, hit);
      }
   }

   public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
      if (world.isAreaLoaded(pos, 3)) {
         if (world.getMaxLocalRawBrightness(pos.above()) >= 9) {
            for (int i = 0; i < 4; i++) {
               BlockPos targetPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
               if (world.getBlockState(targetPos).is((Block)ModBlocks.RICH_SOIL.get()) && canPropagate(state, world, targetPos)) {
                  world.setBlockAndUpdate(targetPos, this.defaultBlockState());
               }
            }
         }
      }
   }

   private static boolean canPropagate(BlockState state, LevelReader world, BlockPos pos) {
      BlockPos blockpos = pos.above();
      return canBeGrass(state, world, pos) && !world.getFluidState(blockpos).is(FluidTags.WATER);
   }

   private static boolean canBeGrass(BlockState state, LevelReader world, BlockPos pos) {
      BlockPos blockpos = pos.above();
      BlockState blockstate = world.getBlockState(blockpos);
      if (blockstate.is(Blocks.SNOW) && (Integer)blockstate.getValue(SnowLayerBlock.LAYERS) == 1) {
         return true;
      } else if (blockstate.getFluidState().getAmount() == 8) {
         return false;
      } else {
         int lightLevel = LayerLightEngine.getLightBlockInto(world, state, pos, blockstate, blockpos, Direction.UP, blockstate.getLightBlock(world, blockpos));
         return lightLevel < world.getMaxLightLevel();
      }
   }
}
