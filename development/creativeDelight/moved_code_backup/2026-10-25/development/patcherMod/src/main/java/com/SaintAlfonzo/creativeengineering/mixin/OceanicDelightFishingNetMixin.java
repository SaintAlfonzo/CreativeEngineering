package com.SaintAlfonzo.creativeengineering.mixin;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Oceanic Delight 1.0.1's fishing net only works on {@code minecraft:kelp_plant} (with kelp above it) and
 * {@code minecraft:tall_seagrass}: its MCreator procedure compares {@code getBlock()} against those two blocks.
 * Every one of those reads answers through two block tags instead, so Upgrade Aquatic's kelps, short seagrass,
 * sea grape and pickerelweed use the same two loot tables:
 * {@code #creativeengineering:fishing_net/kelp} reads as kelp plant (table {@code fishing_net_on_kelp}),
 * {@code #creativeengineering:fishing_net/sea_grass} as tall seagrass ({@code fishing_net_on_sea_grass}).
 * The "kelp above" check goes through the same mapping, so a kelp stalk still needs more kelp above it.
 * The tags are KubeJS (server_scripts/tags/loottags.js); the biome rules are in the tables
 * (loot/BiomeTagCondition). Pseudo: Oceanic Delight may be removed.
 */
@Pseudo
@Mixin(targets = "net.mcreator.oceanicdelight.procedures.FishingNetExtractingProcedure", remap = false)
public class OceanicDelightFishingNetMixin {

	@Unique
	private static final TagKey<Block> creativeengineering$KELP =
			TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("creativeengineering", "fishing_net/kelp"));
	@Unique
	private static final TagKey<Block> creativeengineering$SEA_GRASS =
			TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("creativeengineering", "fishing_net/sea_grass"));

	@Redirect(method = "execute", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/block/state/BlockState;m_60734_()Lnet/minecraft/world/level/block/Block;"))
	private static Block creativeengineering$netBlock(BlockState state) {
		if (state.is(creativeengineering$KELP)) return Blocks.KELP_PLANT;
		if (state.is(creativeengineering$SEA_GRASS)) return Blocks.TALL_SEAGRASS;
		return state.getBlock();
	}
}
