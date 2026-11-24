package com.SaintAlfonzo.creativecompat.melting;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Headless checks of Melting (feature_writeup/melting.md section 7, plan_post_scratch_1115 G7, G8): the [melting] spec
 * builds with the current defaults, snow melts by layer under heat and by saltMeltSnowLayers under salt, a snow
 * block salts into layers, products and the water rule, shared properties are copied, and the melt tick delay stays
 * within 75 to 125 percent of the interval.
 */
public class MeltingTest {
	static int fails = 0, checks = 0;

	static void check(boolean ok, String what) {
		checks++;
		if (!ok) fails++;
		System.out.println((ok ? "ok   " : "FAIL ") + what);
	}

	public static void main(String[] args) throws Exception {
		CommentedConfig cfg = CommentedConfig.inMemory();
		CompatConfig.SPEC.correct(cfg);
		CompatConfig.SPEC.setConfig(cfg);
		CompatConfig.MeltingConfig m = CompatConfig.MELTING;

		check(m.snowLayerMeltChance.get() == 0.8, "snowLayerMeltChance default 0.8");
		check(m.meltTickInterval.get() == 100, "meltTickInterval default 100 (G11)");
		check(m.slowLavaRange.get() == 2 && m.slowMagmaRange.get() == 2, "slow lava and magma ranges 2 (G11)");
		check(m.hotMeltTickInterval.get() == 20, "hotMeltTickInterval default 20 (G11)");
		check(m.endContactOnly.get() && m.endNoFire.get(), "endContactOnly and endNoFire default true (G11)");
		check(m.sluggishMeltFactor.get() == 0.25, "sluggishMeltFactor default 0.25 (E1)");
		check(m.meltProducts.get().size() == 22, "meltProducts: 22 frozen decor rows (E1)");
		check(m.magmaInstantRange.get() == 2, "magmaInstantRange default 2");
		check(m.magmaStopsFreezing.get(), "magmaStopsFreezing default true");
		check(m.saltMeltSnowLayers.get() == 3, "saltMeltSnowLayers default 3");
		check(m.extraRandomTickBlocks.get().containsAll(List.of("minecraft:snow_block", "minecraft:powder_snow",
						"architects_palette:coarse_snow", "architects_palette:molten_nether_bricks"))
				&& !m.extraRandomTickBlocks.get().contains("minecraft:packed_ice"),
				"extraRandomTickBlocks: snow block, powder snow, frozen decor, molten nether bricks; no packed ice");
		check(m.saltProducts.get().contains("immersive_weathering:cryosol=minecraft:dirt")
				&& m.saltProducts.get().contains("minecraft:packed_ice=minecraft:ice"), "saltProducts default rows");
		check(m.slowSteps.get().contains("immersive_weathering:permafrost=immersive_weathering:cryosol"),
				"slowSteps: permafrost -> cryosol");
		Melting.reloadConfig();

		RandomSource r = RandomSource.create(7);
		int lo = Integer.MAX_VALUE, hi = 0;
		for (int i = 0; i < 2000; i++) {
			int d = Melting.scheduleDelay(200, r);
			lo = Math.min(lo, d);
			hi = Math.max(hi, d);
		}
		check(lo >= 150 && hi <= 250 && hi > lo, "delay for 200 within 150..250 (" + lo + ".." + hi + ")");
		check(Melting.scheduleDelay(1, r) >= 1, "delay for 1 at least 1");

		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		BlockState snow5 = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 5);
		BlockState snow1 = Blocks.SNOW.defaultBlockState();
		BlockState snow2 = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 2);

		check(Melting.meltedState(snow5, false, false) == snow5.setValue(SnowLayerBlock.LAYERS, 4), "heat: 5 layers -> 4");
		check(Melting.meltedState(snow1, false, false).isAir(), "heat: 1 layer -> air");
		check(Melting.meltedState(Blocks.SNOW_BLOCK.defaultBlockState(), false, false).isAir(), "heat: snow block -> air (whole)");
		check(Melting.meltedState(Blocks.POWDER_SNOW.defaultBlockState(), false, false).isAir(), "heat: powder snow -> air");
		boolean tagsBound = Blocks.ICE.defaultBlockState().is(net.minecraft.tags.BlockTags.ICE); // no datapack headless
		if (tagsBound) check(Melting.meltedState(Blocks.ICE.defaultBlockState(), false, false).is(Blocks.WATER), "heat: ice -> water");
		else System.out.println("skip ice -> water: block tags are not bound headless (the water rule reads #minecraft:ice)");
		check(Melting.meltedState(Blocks.ICE.defaultBlockState(), true, false).isAir(), "heat: ice in ultra-warm -> air");

		check(Melting.saltedState(snow5, false, 3) == snow5.setValue(SnowLayerBlock.LAYERS, 2), "salt x3: 5 layers -> 2");
		check(Melting.saltedState(snow2, false, 3).isAir(), "salt x3: 2 layers -> air");
		check(Melting.saltedState(snow5, false, 1) == snow5.setValue(SnowLayerBlock.LAYERS, 4), "salt tick: 5 layers -> 4");
		BlockState fromBlock = Melting.saltedState(Blocks.SNOW_BLOCK.defaultBlockState(), false, 3);
		check(fromBlock.is(Blocks.SNOW) && fromBlock.getValue(SnowLayerBlock.LAYERS) == 5, "salt x3: snow block -> 5 layers");
		if (tagsBound) check(Melting.saltedState(Blocks.ICE.defaultBlockState(), false, 3).is(Blocks.WATER), "salt: ice -> water");
		check(Melting.saltedState(Blocks.POWDER_SNOW.defaultBlockState(), false, 3).isAir(), "salt: powder snow -> air");

		BlockState oak = Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
		BlockState spruce = Melting.withShared(oak, Blocks.SPRUCE_STAIRS.defaultBlockState());
		check(spruce.getValue(StairBlock.FACING) == Direction.EAST, "withShared keeps the stairs' facing");
		check(Melting.withShared(oak, Blocks.DIRT.defaultBlockState()) == Blocks.DIRT.defaultBlockState(),
				"withShared ignores properties the product lacks");
		// by name: a top slab carries its type onto another slab block and a wall's sides onto another wall
		BlockState topSlab = Blocks.OAK_SLAB.defaultBlockState().setValue(net.minecraft.world.level.block.SlabBlock.TYPE,
				net.minecraft.world.level.block.state.properties.SlabType.TOP);
		check(Melting.withShared(topSlab, Blocks.STONE_SLAB.defaultBlockState())
				.getValue(net.minecraft.world.level.block.SlabBlock.TYPE) == net.minecraft.world.level.block.state.properties.SlabType.TOP,
				"withShared keeps a slab's type");
		// a property of the same name but another class: copied by value name (two mods' vertical slab types)
		net.minecraft.world.level.block.state.properties.EnumProperty<Direction.Axis> axisA = net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;
		BlockState log = Blocks.OAK_LOG.defaultBlockState().setValue(axisA, Direction.Axis.X);
		check(Melting.withShared(log, Blocks.BASALT.defaultBlockState()).getValue(axisA) == Direction.Axis.X,
				"withShared keeps the axis");
		BlockState observer = Blocks.OBSERVER.defaultBlockState()
				.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, Direction.EAST);
		check(Melting.withShared(observer, Blocks.OAK_STAIRS.defaultBlockState()).getValue(StairBlock.FACING) == Direction.EAST,
				"withShared by name: facing (6 ways) -> stairs facing (4 ways)");
		check(Melting.withShared(observer.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,
						Direction.UP), Blocks.OAK_STAIRS.defaultBlockState()) == Blocks.OAK_STAIRS.defaultBlockState(),
				"withShared by name: a value the product lacks stays at its default");

		// Rows read through ForgeRegistries: only when the headless registry answers (else reported, not failed)
		boolean registry;
		try {
			registry = net.minecraftforge.registries.ForgeRegistries.BLOCKS.containsKey(new net.minecraft.resources.ResourceLocation("minecraft:ice"));
		} catch (Throwable t) {
			registry = false;
		}
		if (registry) {
			check(Melting.saltedState(Blocks.PACKED_ICE.defaultBlockState(), false, 3).is(Blocks.ICE), "salt: packed ice -> ice (row)");
			check(Melting.meltedState(Blocks.BLUE_ICE.defaultBlockState(), false, true).is(Blocks.PACKED_ICE), "heat: blue ice -> packed ice (slow step)");
			// frozen decor rows (E1): products of vanilla blocks only (no mod blocks headless)
			check(Melting.meltedState(Blocks.SNOW_BLOCK.defaultBlockState(), true, false).isAir(), "heat: snow block in ultra-warm -> air");
		} else {
			System.out.println("skip product rows: ForgeRegistries not available headless");
		}

		System.out.println(fails == 0 ? "ALL " + checks + " OK" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}
}
