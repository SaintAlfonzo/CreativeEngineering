// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Snow! Real Magic (mod id {@code snowrealmagic}, feature_writeup/snow_real_magic.md section 4, rulings 5b): the seam
 * between the pack's systems and SRM's seven snow blocks. {@code snowrealmagic:snow} is a snow layer holding a plant;
 * {@code fence}, {@code fence2}, {@code fence_gate}, {@code wall}, {@code slab} and {@code stairs} are snow-topped
 * copies holding the original block. Both kinds keep the held block in a block entity ({@code SnowVariant.getRaw}).
 * Every SRM type is touched only inside {@link Impl}, which loads only when SRM is loaded.
 */
public final class SnowRealMagic {

	private SnowRealMagic() {}

	private static final Logger LOGGER = LogUtils.getLogger();

	public static final boolean LOADED = ModList.get() != null && ModList.get().isLoaded("snowrealmagic");

	/** Falling snow landing in water turns it to ice this often (ruling R13). */
	public static final double ICE_CHANCE = 0.2;
	/** Pack defaults of SRM's blizzard gamerules (R13): layers per falling chunk 1 to 2, a chance of 3500 in 10000. */
	public static final int BLIZZARD_STRENGTH = 2, BLIZZARD_FREQUENCY = 3500;
	/** Snow layers the shovel scoop takes per click (R10); the sneak hand scoop stays SRM's 1. */
	public static final int SHOVEL_SCOOP_LAYERS = 2;
	/** Save and packet key of the "snowy look off" flag (R19). */
	private static final String LOOK_OFF = "CreativeCompatLookOff";

	/** SRM's snow that keeps grass beneath it snowy ({@code snowrealmagic:bottom_snow}). */
	public static final TagKey<Block> BOTTOM_SNOW = BlockTags.create(new ResourceLocation("snowrealmagic", "bottom_snow"));

	/** One of SRM's seven blocks (a block that holds another block). */
	public static boolean isVariant(BlockState state) {
		return LOADED && Impl.isVariant(state);
	}

	/** SRM's snow layer that holds a plant ({@code snowrealmagic:snow}). */
	public static boolean isSnowLayer(BlockState state) {
		return LOADED && state.getBlock() instanceof SnowLayerBlock && Impl.isVariant(state);
	}

	/**
	 * The block an SRM block holds (the plant in the snow, the fence under its snow cap), or null when {@code state} is
	 * not an SRM block or holds nothing (air).
	 */
	@Nullable
	public static BlockState held(BlockGetter level, BlockPos pos, BlockState state) {
		if (!LOADED || !Impl.isVariant(state)) return null;
		BlockState raw = Impl.raw(state, level, pos);
		return raw == null || raw.isAir() ? null : raw;
	}

	/**
	 * Grass with snow on it never spreads (ruling R17): any {@code #minecraft:snow} block (snow layers, SRM's blocks)
	 * or {@code #snowrealmagic:bottom_snow} block above it.
	 */
	public static boolean snowOnTop(BlockState above) {
		return above.is(BlockTags.SNOW) || above.is(BOTTOM_SNOW);
	}

	/** A snow-layer block that is neither vanilla snow nor SRM's snowy plant (a salt layer): SRM's snow rules skip it. */
	public static boolean notSnowLayer(BlockState state) {
		return LOADED && state.getBlock() instanceof SnowLayerBlock && state.getBlock() != Blocks.SNOW && !Impl.isVariant(state);
	}

	/** A blizzard column whose top is already a full stack of snow layers (8) gets no more falling snow (R5, R13). */
	public static boolean fullSnowAbove(Level level, BlockPos pos) {
		BlockState above = level.getBlockState(pos.above());
		return above.getBlock() instanceof SnowLayerBlock && above.hasProperty(SnowLayerBlock.LAYERS)
				&& above.getValue(SnowLayerBlock.LAYERS) >= SnowLayerBlock.MAX_HEIGHT;
	}

	/**
	 * SRM's right-click on snow (snow_real_magic.md 4c). Returns the result, or null to leave the click to SRM.
	 * <ul>
	 *   <li>Any shovel: 2 layers per click (R10), a snowball per layer, 1 durability; a 1-layer pile or a snow cap gives 1.</li>
	 *   <li>A snow-layer block that is not snow (salt): PASS, never scooped (step 11).</li>
	 *   <li>Sneak: SRM's hand scoop (1 layer, a snowball in hand).</li>
	 *   <li>Plain click with both hands empty: the snowy look turns off (R19); Immersive Weathering's snowy stone,
	 *       cobblestone and bricks turn back into the plain block (no snowball, the look only).</li>
	 * </ul>
	 */
	@org.jetbrains.annotations.Nullable
	public static InteractionResult onUse(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		if (!LOADED || hand != InteractionHand.MAIN_HAND) return null;
		BlockPos pos = hit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		boolean emptyHands = player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty();
		if (!Impl.isSnowVariant(state)) {
			if (!emptyHands || player.isShiftKeyDown()) return null;
			BlockState plain = IwSnowy.plain(state);
			if (plain == null) return null;
			if (!level.isClientSide) level.setBlock(pos, plain, Block.UPDATE_ALL);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		if (notSnowLayer(state)) return InteractionResult.PASS;
		if (player.hasCorrectToolForDrops(Blocks.SNOW.defaultBlockState())) {
			Impl.shovelScoop(player, level, pos, state);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		if (player.isShiftKeyDown() || !emptyHands) return null;
		if (!level.isClientSide) lookOff(level, pos, state);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	/**
	 * R19: the snowy look of the block at {@code pos} turns off. SRM's blocks keep a flag in their block entity; a plain
	 * snow layer becomes SRM's snowy plant holding nothing (what SRM's own toggle click does) with the flag set. Plain
	 * snow over a block that shows no drip sides, or a full stack, has no look to turn off.
	 */
	static void lookOff(Level level, BlockPos pos, BlockState state) {
		if (state.getBlock() == Blocks.SNOW) {
			if (state.getValue(SnowLayerBlock.LAYERS) >= SnowLayerBlock.MAX_HEIGHT || !showsDrip(level, pos)) return;
			level.setBlock(pos, Impl.emptySnowyPlant(state), Block.UPDATE_CLIENTS);
		}
		BlockEntity be = level.getBlockEntity(pos);
		SrmLookFlag flag = Impl.flag(be);
		if (flag == null || flag.creativecompat$lookOff()) return;
		flag.creativecompat$setLookOff(true);
		be.setChanged();
		BlockState now = level.getBlockState(pos);
		level.sendBlockUpdated(pos, now, now, Block.UPDATE_ALL);
	}

	/**
	 * R19: new snow at {@code pos} turns the look back on. SRM's snowy plant holding nothing goes back to a plain snow
	 * layer. Server side.
	 */
	public static void lookOn(Level level, BlockPos pos) {
		if (!LOADED || level.isClientSide) return;
		BlockEntity be = level.getBlockEntity(pos);
		SrmLookFlag flag = Impl.flag(be);
		if (flag == null || !flag.creativecompat$lookOff()) return;
		BlockState state = level.getBlockState(pos);
		if (Impl.holdsNothing(state, level, pos)) {
			level.setBlock(pos, Impl.plainSnow(state), Block.UPDATE_CLIENTS);
			return;
		}
		flag.creativecompat$setLookOff(false);
		be.setChanged();
		level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
	}

	/**
	 * A snow layer placed by hand at {@code pos} ({@code placed} is the state about to be set): the look comes back on;
	 * returns the state to place instead (plain snow for SRM's snowy plant holding nothing).
	 */
	public static BlockState placedLookOn(Level level, BlockPos pos, BlockState placed) {
		if (!LOADED) return placed;
		SrmLookFlag flag = Impl.flag(level.getBlockEntity(pos));
		if (flag == null || !flag.creativecompat$lookOff()) return placed;
		BlockState current = level.getBlockState(pos);
		if (placed.getBlock() == current.getBlock() && Impl.holdsNothing(current, level, pos)) return Impl.plainSnow(placed);
		lookOn(level, pos);
		return placed;
	}

	/** Snow at {@code pos} would show drip sides on the block under it (R11): the client's rule, on the server. */
	static boolean showsDrip(BlockGetter level, BlockPos pos) {
		BlockState below = level.getBlockState(pos.below());
		return !below.isAir() && !(below.getBlock() instanceof SnowLayerBlock)
				&& !below.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.SNOWY);
	}

	/** {@code SnowBlockEntity.saveState} tail: the look flag (save file and update packet). */
	public static void saveLook(Object be, CompoundTag data) {
		SrmLookFlag flag = Impl.flag(be);
		if (flag != null && flag.creativecompat$lookOff()) data.putBoolean(LOOK_OFF, true);
	}

	/** {@code SnowBlockEntity.loadState} tail: the look flag; a client that sees it change redraws the block. */
	public static void loadLook(Object be, CompoundTag data) {
		SrmLookFlag flag = Impl.flag(be);
		if (flag == null) return;
		boolean off = data.getBoolean(LOOK_OFF);
		if (off == flag.creativecompat$lookOff()) return;
		flag.creativecompat$setLookOff(off);
		if (be instanceof BlockEntity entity && entity.getLevel() != null && entity.getLevel().isClientSide) {
			entity.requestModelDataUpdate();
			BlockState state = entity.getBlockState();
			entity.getLevel().sendBlockUpdated(entity.getBlockPos(), state, state, Block.UPDATE_IMMEDIATE);
		}
	}

	// ---- Vinery's dirt slab spread (its own mixin sits in the vanilla grass tick body, which SRM cancels) ----

	private static final MethodHandle VINERY_SPREAD = vinerySpread();

	private static MethodHandle vinerySpread() {
		if (ModList.get() == null || !ModList.get().isLoaded("vinery")) return null;
		try {
			Class<?> c = Class.forName("net.satisfy.vinery.core.block.SpreadableGrassSlabBlock");
			return MethodHandles.publicLookup().findStatic(c, "trySpread",
					MethodType.methodType(void.class, ServerLevel.class, BlockPos.class));
		} catch (Throwable t) {
			LOGGER.warn("[creativecompat] Vinery's grass slab spread not found ({}): grass does not spread onto dirt slabs", t.toString());
			return null;
		}
	}

	/** Vinery: grass at a spread target may turn a dirt slab there into a grass slab. */
	public static void vinerySlabSpread(ServerLevel level, BlockPos target) {
		if (VINERY_SPREAD == null) return;
		try {
			VINERY_SPREAD.invokeExact(level, target);
		} catch (Throwable t) {
			HookFaults.once("SnowRealMagic#vinerySlabSpread", t);
		}
	}

	/** Immersive Weathering's snowy stone shapes back to the plain block (R19); null when IW is absent or not snowy. */
	private static final class IwSnowy {
		private static final boolean IW = ModList.get() != null && ModList.get().isLoaded("immersive_weathering");

		@Nullable
		static BlockState plain(BlockState state) {
			if (!IW) return null;
			return Impl.iwPlain(state);
		}
	}

	/** SRM types; loaded only when SRM is. */
	private static final class Impl {
		static boolean isVariant(BlockState state) {
			return state.getBlock() instanceof snownee.snow.block.SnowVariant && state.hasBlockEntity();
		}

		/** SRM's own test in its click: any block it treats as snow (snow layers by its mixin, its seven blocks). */
		static boolean isSnowVariant(BlockState state) {
			return state.getBlock() instanceof snownee.snow.block.SnowVariant;
		}

		@Nullable
		static BlockState iwPlain(BlockState state) {
			return com.ordana.immersive_weathering.blocks.snowy.Snowy.getUnSnowy(state).orElse(null);
		}

		@Nullable
		static SrmLookFlag flag(@Nullable Object be) {
			if (be instanceof snownee.snow.block.entity.SnowBlockEntity snow && snow.options instanceof SrmLookFlag flag) {
				return flag;
			}
			return null;
		}

		/** SRM's snowy plant with nothing in it (a plain snow layer whose look was turned off). */
		static boolean holdsNothing(BlockState state, BlockGetter level, BlockPos pos) {
			return snownee.snow.CoreModule.TILE_BLOCK.is(state) && raw(state, level, pos).isAir();
		}

		static BlockState emptySnowyPlant(BlockState snow) {
			return snownee.snow.Hooks.copyProperties(snow, snownee.snow.CoreModule.TILE_BLOCK.defaultBlockState());
		}

		static BlockState plainSnow(BlockState snowyPlant) {
			return snownee.snow.Hooks.copyProperties(snowyPlant, Blocks.SNOW.defaultBlockState());
		}

		/** R10: the shovel takes up to {@link #SHOVEL_SCOOP_LAYERS} layers, SRM's drop and durability rules. */
		static void shovelScoop(Player player, Level level, BlockPos pos, BlockState state) {
			snownee.snow.block.SnowVariant variant = (snownee.snow.block.SnowVariant) state.getBlock();
			BlockState next = variant.onShovel(state, level, pos);
			int taken = 1;
			while (taken < SHOVEL_SCOOP_LAYERS && next.getBlock() == state.getBlock()
					&& next.hasProperty(SnowLayerBlock.LAYERS)) {
				next = variant.onShovel(next, level, pos);
				taken++;
			}
			level.setBlockAndUpdate(pos, next);
			if (!player.isCreative() && player instanceof ServerPlayer) {
				BlockPos drop = next.canOcclude() ? pos.above() : pos;
				Block.popResource(level, drop, new ItemStack(Items.SNOWBALL, taken));
				player.getMainHandItem().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
			}
		}

		static BlockState raw(BlockState state, BlockGetter level, BlockPos pos) {
			return ((snownee.snow.block.SnowVariant) state.getBlock()).getRaw(state, level, pos);
		}
	}
}
