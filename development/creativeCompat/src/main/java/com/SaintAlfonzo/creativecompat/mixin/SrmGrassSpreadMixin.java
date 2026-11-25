// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.HookFaults;
import com.SaintAlfonzo.creativecompat.compat.SnowRealMagic;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * One pack grass spread tick while Snow! Real Magic is loaded (snow_real_magic.md ruling R17, bug B6). SRM cancels
 * {@code SpreadingSnowyDirtBlock.randomTick} ({@code m_213898_}) at HEAD and runs its own copy of vanilla's spread,
 * which drops the hooks other mods put in vanilla's body (Vinery's dirt slab spread). This HEAD hook is applied before
 * SRM's ({@code priority = 900}; SRM 1000) and does the whole tick, then cancels:
 * <ul>
 *   <li>covered grass turns to dirt: {@code canBeGrass} ({@code m_56823_}), which SRM answers with its snow rule
 *       (grass stays grass under snow layers) and vanilla's light and water rule otherwise;</li>
 *   <li>Creative Calendar's crop-season check runs before this (its {@code GrassSpreadSeasonMixin} is at priority
 *       800);</li>
 *   <li>grass with any snow on top ({@code #minecraft:snow}, {@code #snowrealmagic:bottom_snow}) never spreads;</li>
 *   <li>otherwise vanilla's spread: light 9 above, 4 tries in a 3x5x3 box; Vinery's dirt slab spread at each try;
 *       {@code canPropagate} ({@code m_56827_}, where Neapolitan adds its rule); new grass is snowy under any
 *       {@code #snowrealmagic:bottom_snow} block.</li>
 * </ul>
 * Mycelium shares the class and the rules. Gated on {@code snowrealmagic}.
 */
@Mixin(value = SpreadingSnowyDirtBlock.class, remap = false, priority = 900)
public abstract class SrmGrassSpreadMixin {

	// m_56823_ = SpreadingSnowyDirtBlock.canBeGrass(BlockState, LevelReader, BlockPos)
	@Shadow(remap = false)
	private static boolean m_56823_(BlockState state, LevelReader level, BlockPos pos) {
		throw new AssertionError();
	}

	// m_56827_ = SpreadingSnowyDirtBlock.canPropagate(BlockState, LevelReader, BlockPos)
	@Shadow(remap = false)
	private static boolean m_56827_(BlockState state, LevelReader level, BlockPos pos) {
		throw new AssertionError();
	}

	@Inject(method = "m_213898_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativecompat$grassTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
										  CallbackInfo ci) {
		try {
			ci.cancel();
			if (!m_56823_(state, level, pos)) {
				if (!level.isAreaLoaded(pos, 1)) return;
				level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
				return;
			}
			if (SnowRealMagic.snowOnTop(level.getBlockState(pos.above()))) return;
			if (!level.isAreaLoaded(pos, 3) || level.getMaxLocalRawBrightness(pos.above()) < 9) return;
			BlockState grass = state.getBlock().defaultBlockState();
			for (int i = 0; i < 4; i++) {
				BlockPos to = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
				SnowRealMagic.vinerySlabSpread(level, to);
				if (level.getBlockState(to).is(Blocks.DIRT) && m_56827_(grass, level, to)) {
					BlockState placed = grass.hasProperty(SnowyDirtBlock.SNOWY)
							? grass.setValue(SnowyDirtBlock.SNOWY, level.getBlockState(to.above()).is(SnowRealMagic.BOTTOM_SNOW))
							: grass;
					level.setBlockAndUpdate(to, placed);
				}
			}
		} catch (Throwable t) {
			HookFaults.once("SrmGrassSpreadMixin", t);
			// the tick is skipped this once
		}
	}
}
