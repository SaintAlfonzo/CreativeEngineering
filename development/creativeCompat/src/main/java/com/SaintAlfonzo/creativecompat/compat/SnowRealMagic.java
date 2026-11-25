// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
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

	/** SRM types; loaded only when SRM is. */
	private static final class Impl {
		static boolean isVariant(BlockState state) {
			return state.getBlock() instanceof snownee.snow.block.SnowVariant && state.hasBlockEntity();
		}

		static BlockState raw(BlockState state, BlockGetter level, BlockPos pos) {
			return ((snownee.snow.block.SnowVariant) state.getBlock()).getRaw(state, level, pos);
		}
	}
}
