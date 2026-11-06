package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.perf.IwGrowthFast;
import com.google.common.base.Suppliers;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.data.block_growths.BlockGrowthHandler;
import com.ordana.immersive_weathering.data.block_growths.IConditionalGrowingBlock;
import com.ordana.immersive_weathering.data.block_growths.TickSource;
import com.ordana.immersive_weathering.data.block_growths.growths.IBlockGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Immersive Weathering's growth dispatch goes through {@code perf.IwGrowthFast} (perf.md 15, candidates A and B).
 * An overwrite, not a cancellable inject, so the hot path allocates no CallbackInfo.
 *
 * <p>The fallback branch reproduces Immersive Weathering 1.2.10's own method body (Ordana, MehVahdJukaar,
 * Keybounce), GNU LGPL v3; see creativecommon-NOTICE-thirdparty.txt.
 */
@Mixin(value = BlockGrowthHandler.class, remap = false)
public class IwBlockGrowthHandlerMixin {
	@Shadow @Final private static Map<TickSource, Map<Block, Set<IBlockGrowth>>> GROWTH_FOR_BLOCK;
	@Shadow @Final private static Map<TickSource, Set<IBlockGrowth>> UNIVERSAL_GROWTHS;

	@Shadow
	public static Optional<Set<IBlockGrowth>> getBlockGrowth(TickSource source, Block block) {
		throw new AssertionError();
	}

	/**
	 * @author SaintAlfonzo
	 * @reason Skip the config read, supplier allocation and map lookups for blocks without growths (perf.md 15).
	 */
	@Overwrite
	public static void tickBlock(TickSource source, BlockState state, ServerLevel level, BlockPos pos) {
		if (IwGrowthFast.ENABLED) {
			IwGrowthFast.tickBlock(source, state, level, pos);
			return;
		}
		// IW 1.2.10's own body, for -Dcreativecommon.iwFastTick=false
		if (CommonConfigs.BLOCK_GROWTHS.get()) {
			if (state.getBlock() instanceof IConditionalGrowingBlock cb && !cb.canGrow(state)) return;
			Supplier<Holder<Biome>> biome = Suppliers.memoize(() -> level.getBiome(pos));
			Set<IBlockGrowth> universalGroup = UNIVERSAL_GROWTHS.get(source);
			if (universalGroup != null) {
				for (IBlockGrowth config : universalGroup) config.tryGrowing(pos, state, level, biome);
			}
			Optional<Set<IBlockGrowth>> growth = getBlockGrowth(source, state.getBlock());
			if (growth.isPresent()) {
				for (IBlockGrowth config : growth.get()) config.tryGrowing(pos, state, level, biome);
			}
		}
	}

	@Inject(method = "rebuild", remap = false, at = @At("RETURN"))
	private void creativecommon$iwGrowthTables(RegistryAccess registryAccess, CallbackInfo ci) {
		IwGrowthFast.rebuilt(GROWTH_FOR_BLOCK, UNIVERSAL_GROWTHS);
	}
}
