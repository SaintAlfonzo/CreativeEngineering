package com.SaintAlfonzo.creativecompat.surface;

import static com.SaintAlfonzo.creativecompat.surface.PassThroughTest.check;
import static com.SaintAlfonzo.creativecompat.surface.PassThroughTest.near;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Pass-through phases 3 and 5 (plan_passthrough.md 3.1, 6): the [leaf_piles] slowFromLayers -> [surface.passthrough.
 * leaf_pile] minLayers move, the WTHIT line format, and StuckInBlock reduced to untiered blocks. Called from
 * {@link PassThroughTest#main}.
 */
final class PassPhase35Test {

	private PassPhase35Test() {}

	static void run(Path root) throws Exception {
		format();
		stuckInBlock();
	}

	static final String OLD = """

            #Immersive Weathering leaf piles (1-8 layers). Their slowdown is still IW's own (on/off: IW
            #leaf_piles_slow; Depth Strider, foxes and bees walk through); this sets the height it starts at.
            [leaf_piles]
            \t#Layers a leaf pile needs before it slows walking.
            \t#Range: 1 ~ 9
            \tslowFromLayers = %s

            #Moss block and moss carpet colour (client only; the server ignores this section).
            [moss]
            \tbiomeTint = true
            """;

	static final String NEW_TABLE = """

            [surface.passthrough]
            \tenabled = true

            \t\t#Immersive Weathering leaf piles, Twigs bamboo leaves.
            \t\t[surface.passthrough.leaf_pile]
            \t\t\t#Range: 1 ~ 9
            \t\t\tminLayers = %s
            \t\t\twalkOnlyMoving = false

            \t\t[surface.passthrough.grass]
            \t\t\tminLayers = 1
            """;

	static Object at(String toml, String... path) {
		CommentedConfig c = new TomlParser().parse(toml);
		return c.get(List.of(path));
	}

	static String text(PassThroughFormat.Line l) {
		return PassThroughFormat.english(l, false, "Farmland", "Tilled");
	}

	static List<String> texts(PassRule.Cell c, PassTier.Values v, double soil, boolean hideNormal) {
		return PassThroughFormat.lines(c, v, soil, hideNormal).stream().map(PassPhase35Test::text).toList();
	}

	static void format() {
		PassTier.Values dc = PassTier.DENSE_CROP.defaults(), crop = PassTier.CROP.defaults(), web = PassTier.COBWEB.defaults(),
				vine = PassTier.VINE.defaults(), leaf = PassTier.LAYERS.defaults();
		check("number 1.50x / 0.40x / -12% / +50% / 0%", PassThroughFormat.number(1.5, false).equals("1.50×")
				&& PassThroughFormat.number(0.4, false).equals("0.40×") && PassThroughFormat.number(0.88, true).equals("-12%")
				&& PassThroughFormat.number(1.5, true).equals("+50%") && PassThroughFormat.number(1.0, true).equals("0%"));
		List<String> t = texts(PassRule.cell(PassTier.DENSE_CROP, dc, 3), dc, 0.8, true);
		check("dense crop, 3 of 4 neighbors, on tilled 0.8: " + t, t.equals(List.of(
				"Walk Through: 0.40× (Dense neighbors) on Farmland (Tilled)", "Neighbors: 3 of 4")));
		t = texts(PassRule.cell(PassTier.DENSE_CROP, dc, 2), dc, 1.0, true);
		check("dense crop, 2 of 4 neighbors, on dirt: " + t, t.equals(List.of("Walk Through: 0.75×", "Neighbors: 2 of 4")));
		t = texts(PassRule.cell(PassTier.CROP, crop, 0), crop, 1.0, true);
		check("crop on dirt: one line, no soil: " + t, t.equals(List.of("Walk Through: 0.75×")));
		t = texts(PassRule.cell(PassTier.CROP, crop, 0), crop, 1.0, false);
		check("crop, hideNormal off: fall 1.00 shown: " + t, t.equals(List.of("Walk Through: 0.75×", "Fall Through: 1.00×")));
		t = texts(PassRule.cell(PassTier.COBWEB, web, 0), web, 1.0, true);
		check("lone web: " + t, t.equals(List.of("Walk Through: 0.50×", "Fall Through: 0.85×", "Neighbors: 0 of 6")));
		t = texts(PassRule.cell(PassTier.COBWEB, web, 3), web, 1.0, true);
		check("dense web (3 of 6): " + t, t.equals(List.of("Walk Through: 0.35× (Dense neighbors)",
				"Fall Through: 0.20× (Dense neighbors)", "Neighbors: 3 of 6")));
		t = texts(PassRule.cell(PassTier.COBWEB, web, 6), web, 1.0, true);
		check("surrounded web (6 of 6): " + t, t.equals(List.of("Walk Through: 0.35× (Dense neighbors)",
				"Fall Through: stopped", "Neighbors: 6 of 6")));
		t = texts(PassRule.cell(PassTier.VINE, vine, 0), vine, 1.0, true);
		check("vine: " + t, t.equals(List.of("Walk Through: 0.75× (while moving through)")));
		t = PassThroughFormat.lines(PassRule.layerCell(PassTier.LAYERS, leaf, 0.45), leaf, 1.6, true, 4, 8).stream()
				.map(PassPhase35Test::text).toList();
		check("4-layer pile (0.45) on a 1.6 path: 0.72x, layers, soil named: " + t,
				t.equals(List.of("Walk Through: 0.72× (4 of 8 layers) on Farmland (Tilled)")));
		PassTier.Values half = PassTier.CANE.defaults();
		check("walk 0.50 on a 2.00 soil = 1.00: hidden with hideNormal",
				PassThroughFormat.lines(PassRule.cell(PassTier.CANE, half, 0), half, 2.0, true).isEmpty());
		PassThroughFormat.Line w = PassThroughFormat.lines(PassRule.cell(PassTier.CROP, crop, 0), crop, 1.0, true).get(0);
		check("percent: -25%", PassThroughFormat.english(w, true, null, null).equals("Walk Through: -25%"));
		check("soil tier unknown: name only", PassThroughFormat.english(
				PassThroughFormat.lines(PassRule.cell(PassTier.CROP, crop, 0), crop, 0.6, true).get(0), false, "Mud", null)
				.equals("Walk Through: 0.45× on Mud"));

		// through cellAt (the WTHIT entry), a 3x3x3 web cube
		PassThroughTest.World cube = new PassThroughTest.World();
		BlockState webState = Blocks.COBWEB.defaultBlockState();
		for (int x = 0; x < 3; x++) for (int y = 0; y < 3; y++) for (int z = 0; z < 3; z++) cube.put(x, y, z, webState);
		PassRule.Cell centre = PassThrough.cellAt(cube, new BlockPos(1, 1, 1), webState, PassValues.defaults());
		check("cellAt cube center -> 'Fall Through: stopped'", centre != null && texts(centre, web, 1.0, true).contains("Fall Through: stopped"));
		PassRule.Cell corner = PassThrough.cellAt(cube, BlockPos.ZERO, webState, PassValues.defaults());
		check("cellAt cube corner (3 of 6 faces) -> dense 0.35 / 0.20", corner != null && near(corner.walk(), 0.35)
				&& texts(corner, web, 1.0, true).contains("Neighbors: 3 of 6"));
	}

	static void stuckInBlock() {
		check("StuckInBlock: cobweb no longer listed (tiered)", Double.isNaN(StuckInBlock.horizontal(Blocks.COBWEB.defaultBlockState(), null, false)));
		check("StuckInBlock: sweet berry bush no longer listed", Double.isNaN(StuckInBlock.horizontal(Blocks.SWEET_BERRY_BUSH.defaultBlockState(), null, false)));
		check("StuckInBlock: powder snow inside 0.9, standing on it none",
				near(StuckInBlock.horizontal(Blocks.POWDER_SNOW.defaultBlockState(), null, false), 0.9)
						&& Double.isNaN(StuckInBlock.horizontal(Blocks.POWDER_SNOW.defaultBlockState(), null, true)));
	}
}
