package com.SaintAlfonzo.creativedelight.cooking;

import com.SaintAlfonzo.creativedelight.CDConfig;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Cookware handling (TODO cooking/kitchen #27, QoL-7 {@code cookwarePickup}): for the blocks in
 * {@code #creativedelight:cookware_pickup} (cooking pot, copper pot, kettle, skillet).
 * <ul>
 * <li>Sneak right-click with an empty main hand picks the block up into that hand, as if broken by hand: its loot
 * (the meal kept in the item, as the mods' loot tables do) goes to the hand, anything the block throws out when
 * removed (ingredients) drops as on breaking. Respects break protection (a {@link BlockEvent.BreakEvent} is posted).</li>
 * <li>Sneak left-click with an empty main hand toggles the handle on the first hit (no breaking): the block's own
 * toggle, which the mods run on sneak right-click, is called directly. Only blocks with a {@code support} property.</li>
 * </ul>
 * Upstream's sneak right-click toggle is never reached: the right-click event is cancelled before the block's use.
 */
public final class CookwareHandling {

	public static final TagKey<Block> COOKWARE = TagKey.create(Registry.BLOCK_REGISTRY,
			new ResourceLocation("creativedelight", "cookware_pickup"));

	private CookwareHandling() {}

	private static boolean applies(Player player, BlockState state) {
		return player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && !player.isSpectator()
				&& state.is(COOKWARE) && CDConfig.qol(CDConfig.COOKWARE_PICKUP);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		BlockState state = level.getBlockState(pos);
		if (!applies(player, state)) return;
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		if (event.getHand() != InteractionHand.MAIN_HAND || !(level instanceof ServerLevel server)) return;
		if (!player.mayBuild() || !level.mayInteract(player, pos)) return;
		if (MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player))) return;

		BlockEntity be = level.getBlockEntity(pos);
		List<ItemStack> drops = Block.getDrops(state, server, pos, be, player, ItemStack.EMPTY);
		if (drops.isEmpty()) drops = List.of(new ItemStack(state.getBlock()));
		// Removing runs the block's onRemove: ingredients drop as on breaking, the meal stays in the item
		if (!level.removeBlock(pos, false)) return;
		level.levelEvent(2001, pos, Block.getId(state));
		SoundType sound = state.getSoundType(level, pos, player);
		level.playSound(null, pos, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
		level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
		player.setItemInHand(InteractionHand.MAIN_HAND, drops.get(0).copy());
		for (int n = 1; n < drops.size(); n++) player.getInventory().placeItemBackInInventory(drops.get(n).copy());
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
		Player player = event.getEntity();
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		BlockState state = level.getBlockState(pos);
		if (!applies(player, state) || state.getBlock().getStateDefinition().getProperty("support") == null) return;
		// Cancelled on both sides: no break progress on the client, and the server ignores the start of the break
		event.setCanceled(true);
		if (level.isClientSide || !level.mayInteract(player, pos)) return;
		Direction face = event.getFace() == null ? Direction.UP : event.getFace();
		state.use(level, player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false));
	}
}
