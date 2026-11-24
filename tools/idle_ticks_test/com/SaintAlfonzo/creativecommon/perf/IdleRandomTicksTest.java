// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.perf;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.List;
import java.util.Map;

/**
 * Headless checks of idle random ticks (feature_writeup/random_tick_audit.md, plan_farming_cooking_batch.md F2): rule
 * line parsing and matching, the [perf] defaults (rows 1-7, 9, 11, 12 of the audit, 34 lines; Creative Delight adds its treated-stick tomatoes with addModRule), and the rule table
 * against real vanilla block states (the pack's own rule blocks are mod blocks, absent here).
 */
public class IdleRandomTicksTest {
	static int fails = 0, checks = 0;

	static void check(boolean ok, String what) {
		checks++;
		if (!ok) fails++;
		System.out.println((ok ? "ok   " : "FAIL ") + what);
	}

	static IdleTickRules.StateView view(Map<String, String> v, Map<String, int[]> ints) {
		return new IdleTickRules.StateView() {
			public String value(String p) {
				return v.get(p);
			}

			public String bound(String p, boolean max) {
				int[] r = ints.get(p);
				return r == null ? null : Integer.toString(max ? r[1] : r[0]);
			}
		};
	}

	static boolean parses(String line) {
		try {
			IdleTickRules.parse(line);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
		// rule lines, no game classes
		IdleTickRules.Rule tomato = IdleTickRules.parse("farmersdelight:tomatoes age=max +wake_above");
		Map<String, int[]> age3 = Map.of("age", new int[] {0, 3});
		check(tomato.wakeAbove() && tomato.conds().size() == 1, "tomato line: one condition and +wake_above");
		check(IdleTickRules.matches(tomato, view(Map.of("age", "3", "ropelogged", "false"), age3)), "tomato age 3 stops");
		check(!IdleTickRules.matches(tomato, view(Map.of("age", "2", "ropelogged", "false"), age3)), "tomato age 2 ticks");
		IdleTickRules.Rule rice = IdleTickRules.parse("farmersdelight:rice age=max supporting=true");
		check(IdleTickRules.matches(rice, view(Map.of("age", "3", "supporting", "true"), age3)), "rice with panicles stops");
		check(!IdleTickRules.matches(rice, view(Map.of("age", "3", "supporting", "false"), age3)), "rice with panicles cut ticks");
		IdleTickRules.Rule waxed = IdleTickRules.parse("redeco:stone_lantern waxed!=0");
		Map<String, int[]> lantern = Map.of("waxed", new int[] {0, 1}, "mossing", new int[] {0, 3});
		check(IdleTickRules.matches(waxed, view(Map.of("waxed", "1", "mossing", "0"), lantern)), "waxed lantern stops");
		check(!IdleTickRules.matches(waxed, view(Map.of("waxed", "0", "mossing", "0"), lantern)), "unwaxed lantern ticks");
		IdleTickRules.Rule hemp = IdleTickRules.parse("immersiveengineering:hemp growth=TOP0");
		check(IdleTickRules.matches(hemp, view(Map.of("growth", "top0"), Map.of())), "hemp top0 stops (value case-folded)");
		check(!IdleTickRules.matches(hemp, view(Map.of("growth", "bottom4"), Map.of())), "hemp bottom4 ticks (regrows a cut top)");
		check(IdleTickRules.check(IdleTickRules.parse("x:y growth=max"), view(Map.of("growth", "top0"), Map.of())) != null,
				"max on a non-integer property is reported");
		check(IdleTickRules.check(IdleTickRules.parse("x:y agee=1"), view(Map.of("age", "1"), age3)) != null,
				"unknown property is reported");
		check(IdleTickRules.matches(IdleTickRules.parse("floralench:poppy_bush"), view(Map.of("age", "0"), age3)),
				"line without conditions stops every state");
		for (String bad : new String[] {"", "noid", "x: age=1", "x:y age", "x:y =1", "x:y age="}) {
			check(!parses(bad), "malformed line refused: '" + bad + "'");
		}
		List<String> d = IdleTickRules.defaults();
		check(d.stream().allMatch(IdleRandomTicksTest::parses), "every default line parses");
		check(d.size() == 34, "34 default lines (" + d.size() + ")");
		check(d.stream().noneMatch(s -> s.startsWith("creativedelight:")), "no Creative Delight line (it adds its own with addModRule)");
		check(d.stream().filter(s -> s.startsWith("floralench:")).count() == 17, "17 Floral Enchantment bushes");
		check(d.contains("floralench:rose_bush_bush"), "rose bush bush id");
		check(d.stream().noneMatch(s -> s.startsWith("neapolitan:mint")), "mint not stopped (audit row 8: keep)");

		// the [perf] spec and the table against real states
		CommentedConfig cfg = CommentedConfig.inMemory();
		CommonConfig.COMMON_SPEC.correct(cfg);
		CommonConfig.COMMON_SPEC.setConfig(cfg);
		check(CommonConfig.PERF.idleRandomTicks.get(), "idleRandomTicks default on");
		check(CommonConfig.PERF.idleRandomTickRules.get().equals(d), "idleRandomTickRules default = IdleTickRules.defaults()");
		CommonConfig.PERF.idleRandomTickRules.set(List.of(
				"minecraft:wheat age=max",
				"minecraft:sweet_berry_bush age=2",
				"minecraft:sweet_berry_bush age=1",
				"minecraft:stone_slab type!=double",
				"minecraft:dead_bush",
				"minecraft:wheat agee=1",
				"minecraft:wheat age=max nonsense",
				"absentmod:thing age=max"));

		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		check(IdleRandomTicks.classify(Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7)) == IdleRandomTicks.STOP,
				"wheat age 7 (max) stops");
		check(IdleRandomTicks.classify(Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 6)) == IdleRandomTicks.KEEP,
				"wheat age 6 keeps");
		check(IdleRandomTicks.classify(Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 2))
				== IdleRandomTicks.STOP, "two lines for one block are alternatives (berry age 2)");
		check(IdleRandomTicks.classify(Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 1))
				== IdleRandomTicks.STOP, "two lines for one block are alternatives (berry age 1)");
		check(IdleRandomTicks.classify(Blocks.SWEET_BERRY_BUSH.defaultBlockState()) == IdleRandomTicks.KEEP,
				"berry age 0 keeps");
		check(IdleRandomTicks.classify(Blocks.STONE_SLAB.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE,
				SlabType.TOP)) == IdleRandomTicks.STOP, "enum != : top slab stops");
		check(IdleRandomTicks.classify(Blocks.STONE_SLAB.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE,
				SlabType.DOUBLE)) == IdleRandomTicks.KEEP, "enum != : double slab keeps");
		check(IdleRandomTicks.classify(Blocks.DEAD_BUSH.defaultBlockState()) == IdleRandomTicks.STOP, "no condition stops");
		check(IdleRandomTicks.classify(Blocks.STONE.defaultBlockState()) == IdleRandomTicks.KEEP, "unlisted block keeps");
		CommonConfig.PERF.idleRandomTickRules.set(List.of("minecraft:stone"));
		check(IdleRandomTicks.classify(Blocks.STONE.defaultBlockState()) == IdleRandomTicks.KEEP,
				"rules are read once (a later config change waits for a restart)");

		System.out.println(fails == 0 ? "ALL " + checks + " OK" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}
}
