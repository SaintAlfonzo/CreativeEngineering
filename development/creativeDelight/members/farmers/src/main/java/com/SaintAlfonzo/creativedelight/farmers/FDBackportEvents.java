package com.SaintAlfonzo.creativedelight.farmers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import vectorwing.farmersdelight.common.block.PieBlock;
import vectorwing.farmersdelight.common.block.RichSoilFarmlandBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;

/** Forge-bus halves of the backports: rich soil trampling and vanilla Pumpkin Pie placement. */
public final class FDBackportEvents {

	private FDBackportEvents() {}

	/**
	 * Rich Soil Farmland is never trampled (1.3.4 KeepRichSoilUntrampledMixin). In 1.2.4 it inherits vanilla
	 * fallOn, whose trample turns it into plain Dirt; Forge's trample event is the hook in front of that call.
	 */
	@SubscribeEvent
	public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
		if (event.getState().getBlock() instanceof RichSoilFarmlandBlock) event.setCanceled(true);
	}

	/**
	 * A vanilla Pumpkin Pie used on a block places the pie block chosen in {@code [pumpkin_pie] placeAs} (1.3.4
	 * PlacePumpkinPieMixin, here an event so it also decides between Delightful's and Central Kitchen's pies: HIGHEST
	 * runs before Delightful's NORMAL handler). Same rules as Delightful's: never on a Cutting Board, the block below
	 * must be solid, and the pie needs room.
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onPlacePumpkinPie(PlayerInteractEvent.RightClickBlock event) {
		ItemStack stack = event.getItemStack();
		if (!stack.is(Items.PUMPKIN_PIE) || event.isCanceled()) return;
		ResourceLocation id = FDBackportConfig.pieBlock();
		if (id == null || !ForgeRegistries.BLOCKS.containsKey(id)) return;
		if (!(ForgeRegistries.BLOCKS.getValue(id) instanceof PieBlock pie)) return;
		Player player = event.getEntity();
		if (FDBackportConfig.pieSneakToPlace() && !player.isShiftKeyDown()) return;
		if (event.getLevel().getBlockState(event.getPos()).is(ModBlocks.CUTTING_BOARD.get())) return;

		event.setUseItem(Event.Result.DENY);
		event.setUseBlock(Event.Result.DENY);
		InteractionResult result = place(pie, new BlockPlaceContext(player, event.getHand(), stack, event.getHitVec()));
		event.setCancellationResult(result);
		event.setCanceled(result.consumesAction());
	}

	private static InteractionResult place(Block pie, BlockPlaceContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		if (!context.canPlace() || !level.getBlockState(pos.below()).getMaterial().isSolid()) return InteractionResult.FAIL;
		BlockState state = pie.getStateForPlacement(context);
		CollisionContext collision = player == null ? CollisionContext.empty() : CollisionContext.of(player);
		if (state == null || !state.canSurvive(level, pos) || !level.isUnobstructed(state, pos, collision)) return InteractionResult.FAIL;
		if (!level.setBlock(pos, state, 11)) return InteractionResult.FAIL;

		BlockState placed = level.getBlockState(pos);
		if (placed.is(pie)) pie.setPlacedBy(level, pos, placed, player, context.getItemInHand());
		level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, state));
		SoundType sound = placed.getSoundType(level, pos, player);
		level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
		if (player == null || !player.getAbilities().instabuild) context.getItemInHand().shrink(1);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}
}
