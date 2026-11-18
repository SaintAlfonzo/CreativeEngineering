package com.SaintAlfonzo.creativedelight.campfire;

import java.util.List;

import com.SaintAlfonzo.creativedelight.CDConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Torches light campfires, and the "placed unlit" tooltip (plan_post_scratch_1115 G3a, G3b, G7.8, G7.13, G7.14; switches
 * in {@code [campfire]}).
 * <ul>
 * <li>A torch used on an unlit, dry campfire lights it the way flint and steel does (lit state, game event; Unlit
 * Campfire keeps no burn timer while its lit time is 0, the pack's setting) and is used up ({@code torchConsumed}).
 * Soul torches light only soul campfires, plain torches the others ({@code soulflameRule}). Where rain would put it out
 * again ({@link CampfireRain}'s rule), the extinguish sound plays and nothing is lit or used ({@code torchRainRefuses}).</li>
 * <li>Tooltip on every campfire Unlit Campfire places unlit (its mixin covers every {@link CampfireBlock}; Tinted
 * Campfires are not one): "Requires a spark or flame to light", "soulflame" on soul campfires, and a torch line while
 * torch lighting is on. Only while Unlit Campfire is loaded and {@code unlitTooltip} is on.</li>
 * </ul>
 * A torch is a {@link TorchBlock} item that is not a redstone torch; "soul" is read from the registry path (soul torch,
 * soul campfire, and modded ones named the same way).
 */
public final class CampfireTorch {

	private CampfireTorch() {}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (!CDConfig.CAMPFIRE.torchLights.get()) return;
		ItemStack stack = event.getItemStack();
		Boolean soulTorch = torchKind(stack);
		if (soulTorch == null) return;
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof CampfireBlock) || !CampfireBlock.canLight(state)) return;
		if (CDConfig.CAMPFIRE.soulflameRule.get() && soulTorch != isSoul(state.getBlock())) return;

		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
		if (level.isClientSide()) return;
		Player player = event.getEntity();
		if (CampfireEnd.refuses(level, state)) {
			level.levelEvent(null, 1009, pos, 0); // the End refuses, as rain does (CampfireEnd)
			return;
		}
		if (CDConfig.CAMPFIRE.torchRainRefuses.get() && rainWouldDouse(level, pos, state)) {
			level.levelEvent(null, 1009, pos, 0); // the extinguish sound, as CampfireRain's rain-out
			return;
		}
		level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.6F, 1.2F + level.random.nextFloat() * 0.2F);
		level.setBlock(pos, state.setValue(CampfireBlock.LIT, true), Block.UPDATE_ALL_IMMEDIATE);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		if (CDConfig.CAMPFIRE.torchConsumed.get() && !player.getAbilities().instabuild) {
			stack.shrink(1);
		}
	}

	/** {@link CampfireRain}'s rule: in the rain-douses tag, rain on it, not on a rain-proof fuel block, rain-out on. */
	private static boolean rainWouldDouse(Level level, BlockPos pos, BlockState state) {
		return CDConfig.CAMPFIRE.rainDouses.get()
				&& state.is(CampfireRain.RAIN_DOUSES)
				&& !level.getBlockState(pos.below()).is(CampfireRain.RAIN_PROOF_FUEL)
				&& level.isRainingAt(pos.above());
	}

	/** null: not a torch; true: a soul torch; false: a plain torch. */
	private static Boolean torchKind(ItemStack stack) {
		if (!(stack.getItem() instanceof BlockItem item)) return null;
		Block block = item.getBlock();
		if (!(block instanceof TorchBlock) || block instanceof RedstoneTorchBlock) return null;
		return isSoul(block);
	}

	static boolean isSoul(Block block) {
		ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
		return id != null && id.getPath().contains("soul");
	}

	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent event) {
		if (!(event.getItemStack().getItem() instanceof BlockItem item) || !(item.getBlock() instanceof CampfireBlock)) return;
		if (!CDConfig.CAMPFIRE.unlitTooltip.get() || !ModList.get().isLoaded("unlitcampfire")) return;
		boolean soul = isSoul(item.getBlock());
		List<Component> tip = event.getToolTip();
		tip.add(Component.translatable(soul ? "creativedelight.campfire.unlit_soul" : "creativedelight.campfire.unlit").withStyle(ChatFormatting.GRAY));
		if (CDConfig.CAMPFIRE.torchLights.get()) {
			tip.add(Component.translatable(soul && CDConfig.CAMPFIRE.soulflameRule.get() ? "creativedelight.campfire.torch_soul" : "creativedelight.campfire.torch")
					.withStyle(ChatFormatting.GRAY));
		}
	}
}
