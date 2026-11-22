// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.compat;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecompat.CreativeCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Two flower pots in one block, like candles (feature_writeup/flower_pots.md 3.4, TODO blocks/flower-pots #1). A
 * flower pot item used on a placed vanilla-family flower pot (empty or planted; not sneaking) turns it into
 * {@code creativecompat:double_flower_pot}: the old pot and its plant become the first pot, the new pot stands
 * beside it {@link #TALL_RISE_PX} px taller, and each takes its own plant from the vanilla pot's map, picked by
 * which half of the block is clicked. The block has no item: it breaks into two flower pots and their plants.
 *
 * <p>Pot A (the first, vanilla height) sits at x/z 2-8, pot B (the taller one) at x/z 8-14 (model
 * {@code creativecompat:block/double_flower_pot}; its pot B elements stand on a {@link #TALL_RISE_PX} px base,
 * so a retune changes this constant and that model's pot B y values together).
 */
public final class DoublePot {

	/** How much taller the second pot is than the first (owner: 2 px). */
	public static final int TALL_RISE_PX = 2;
	/** Pot centers in block pixels (x = z). */
	static final float POT_A_CENTER_PX = 5f;
	static final float POT_B_CENTER_PX = 11f;
	static final String PLANT_A_TAG = "PlantA";
	static final String PLANT_B_TAG = "PlantB";

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CreativeCompat.NS);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
			DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CreativeCompat.NS);

	public static final RegistryObject<Block> BLOCK = BLOCKS.register("double_flower_pot",
			() -> new DoublePotBlock(BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)));
	public static final RegistryObject<BlockEntityType<DoublePotBlockEntity>> TYPE =
			BLOCK_ENTITY_TYPES.register("double_flower_pot", () -> BlockEntityType.Builder.of(DoublePotBlockEntity::new,
					BLOCK.get()).build(null));

	private DoublePot() {}

	public static void register(IEventBus modEventBus) {
		BLOCKS.register(modEventBus);
		BLOCK_ENTITY_TYPES.register(modEventBus);
	}

	/** True for pot B (the taller one), from the hit position: the block's diagonal splits the two pots. */
	static boolean hitsPotB(BlockPos pos, Vec3 hit) {
		double x = hit.x - pos.getX();
		double z = hit.z - pos.getZ();
		return x + z >= 1.0;
	}

	/** A placed pot of the vanilla family (vanilla or a mod's {@code potted_*} on the vanilla empty pot). */
	private static boolean doublable(BlockState state) {
		return state.getBlock() instanceof FlowerPotBlock pot && pot.getEmptyPot() == Blocks.FLOWER_POT;
	}

	@Nullable
	private static ResourceLocation plantOf(FlowerPotBlock pot) {
		Block content = pot.getContent();
		if (content == null || content == Blocks.AIR) return null;
		return ForgeRegistries.BLOCKS.getKey(content);
	}

	/** Flower pot item on a placed pot: a second pot joins it. Runs on both sides so the client swings and stops. */
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		ItemStack held = event.getItemStack();
		if (!held.is(Items.FLOWER_POT) || player.isSecondaryUseActive()) return;
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		BlockState state = level.getBlockState(pos);
		if (!doublable(state)) return;
		ResourceLocation plant = plantOf((FlowerPotBlock) state.getBlock());
		// A plant the pot holds but the vanilla map no longer lists (a broken mod entry) would be lost: leave it be.
		if (plant != null && BotanistPot.pottedFor(plant) == null) return;
		if (!level.isClientSide) {
			level.setBlock(pos, BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
			if (level.getBlockEntity(pos) instanceof DoublePotBlockEntity be) be.setPlants(plant, null);
			if (!player.getAbilities().instabuild) held.shrink(1);
			level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1f, 1.2f);
			level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
		}
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
	}
}
