// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativeoxidation;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

/**
 * Headless checks of Creative Oxidation (plan_creative_oxidation.md P1, P2): the 45 target ids, the B1 decision that
 * StateDefinitionBuilderMixin asks (properties only while Registrate builds a target id; vanilla iron bars and doors,
 * built outside Registrate, and create:andesite_casing get none), the B2 random tick decisions, the aging chances, the
 * item NBT round trip and the creativeoxidation:staged matching rule (R10).
 *
 * The mixins themselves are not applied in a plain JVM, so the state definition check runs the decision with the real
 * owners (vanilla blocks after Bootstrap) in the same begin/take/end order the two mixins use; the live check is
 * SQ-OXD-07.
 */
public class CreativeOxidationTest {
	static int fails = 0, checks = 0;

	static void check(boolean ok, String what) {
		checks++;
		if (!ok) fails++;
		System.out.println((ok ? "ok   " : "FAIL ") + what);
	}

	/** Registrate builds {@code id} (null: the block is not built through Registrate) whose constructor makes {@code owner}'s state definition. */
	static boolean build(String id, Object owner) {
		if (id == null) return OxidationTargets.takeForStateDefinition(owner instanceof net.minecraft.world.level.block.Block);
		OxidationTargets.beginBuild(id);
		try {
			return OxidationTargets.takeForStateDefinition(owner instanceof net.minecraft.world.level.block.Block);
		} finally {
			OxidationTargets.endBuild();
		}
	}

	public static void main(String[] args) throws Exception {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();

		// target list (plan 2.1 + 2.2)
		List<String> ids = OxidationTargets.ORDERED_BLOCK_IDS;
		check(ids.size() == 45, "45 target ids (" + ids.size() + ")");
		check(new HashSet<>(ids).size() == ids.size(), "no duplicate target id");
		check(ids.stream().filter(i -> i.startsWith("create:")).count() == 18, "18 Create ids");
		check(ids.stream().filter(i -> i.startsWith("createdeco:")).count() == 14, "14 Create Deco ids");
		check(ids.stream().filter(i -> i.startsWith("design_decor:")).count() == 13, "13 Design n' Decor ids");
		String tag = java.nio.file.Files.readString(java.nio.file.Path.of(
				"development/creativeEngineering/members/creativeoxidation/src/main/resources/data/creativeoxidation/tags/blocks/oxidizable_blocks.json"));
		check(ids.stream().allMatch(i -> tag.contains("\"" + i + "\"")) && tag.split("\"(create|createdeco|design_decor):").length - 1 == ids.size(),
				"oxidizable_blocks tag lists exactly the target ids");
		check(!OxidationTargets.isTarget("create:copper_table_cloth") && !OxidationTargets.isTarget("dndecor:copper_boiler"),
				"absent upstream ids dropped");

		// B1: properties only while Registrate builds a target id
		check(!build(null, Blocks.IRON_BARS), "vanilla IronBarsBlock (not built by Registrate): no properties");
		check(!build(null, Blocks.OAK_DOOR), "vanilla DoorBlock: no properties");
		check(!build(null, Blocks.GLASS_PANE), "vanilla glass pane: no properties");
		check(!build("create:andesite_casing", Blocks.IRON_BARS), "create:andesite_casing (a CasingBlock, not a target): no properties");
		check(!build("create:framed_glass_pane", Blocks.GLASS_PANE), "create:framed_glass_pane: no properties");
		check(!build("everythingcopper:copper_door", Blocks.OAK_DOOR), "another mod's door: no properties");
		check(build("create:fluid_pipe", Blocks.IRON_BARS), "create:fluid_pipe: properties");
		check(build("createdeco:copper_bars", Blocks.IRON_BARS), "createdeco:copper_bars: properties");
		check(build("create:copper_door", Blocks.OAK_DOOR), "create:copper_door: properties");
		check(build("design_decor:copper_screw", Blocks.IRON_BARS), "design_decor:copper_screw: properties");

		OxidationTargets.beginBuild("create:fluid_pipe");
		check(!OxidationTargets.takeForStateDefinition(((Object) Fluids.WATER) instanceof net.minecraft.world.level.block.Block),
				"a non-block owner inside the window: no properties");
		check(OxidationTargets.takeForStateDefinition(true), "the block after it still gets them");
		check(!OxidationTargets.takeForStateDefinition(true), "a second block in the same factory call: none (id used up)");
		OxidationTargets.endBuild();
		check(!build(null, Blocks.IRON_BARS), "nothing pending after the factory returns");

		OxidationTargets.beginBuild("create:spout");
		AtomicReference<Boolean> other = new AtomicReference<>();
		Thread t = new Thread(() -> other.set(OxidationTargets.takeForStateDefinition(true)));
		t.start();
		t.join();
		check(Boolean.FALSE.equals(other.get()), "pending id is per thread");
		OxidationTargets.endBuild();

		// B2: random ticks
		check(OxidationRules.isAging(true, false, OxidationStage.UNAFFECTED), "unaffected unwaxed target ages");
		check(!OxidationRules.isAging(true, true, OxidationStage.EXPOSED), "waxed does not age");
		check(!OxidationRules.isAging(true, false, OxidationStage.OXIDIZED), "oxidized does not age");
		check(!OxidationRules.isAging(false, false, OxidationStage.EXPOSED), "not in the tag: does not age");
		check(Boolean.TRUE.equals(OxidationRules.forceRandomTicking(true)), "aging state: isRandomlyTicking forced true");
		check(OxidationRules.forceRandomTicking(false) == null, "other states fall through to the block (B2)");
		check(OxidationRules.runOwnRandomTick(true, false), "a block that ticks by itself keeps its own randomTick");
		check(!OxidationRules.runOwnRandomTick(false, false), "a block that ticks only to age: own randomTick (tick()) skipped");
		check(!OxidationRules.runOwnRandomTick(true, true), "state just aged: own randomTick skipped");

		// chances
		check(OxidationRules.attemptChance(0.0) == 0.0, "speedFactor 0: no attempts");
		check(Math.abs(OxidationRules.attemptChance(1.0) - 0.05688889) < 1e-7, "speedFactor 1: vanilla 0.0569");
		check(Math.abs(OxidationRules.attemptChance(2.0) - (1 - 0.94311111 * 0.94311111)) < 1e-9, "speedFactor 2: two vanilla tries");
		check(OxidationRules.attemptChance(100.0) > 0.99, "speedFactor 100: near certain");
		check(OxidationRules.stepChance(OxidationStage.UNAFFECTED, 0, 0) == 0.75F, "alone, unaffected: 0.75");
		check(OxidationRules.stepChance(OxidationStage.EXPOSED, 0, 0) == 1.0F, "alone, exposed: 1");
		check(Math.abs(OxidationRules.stepChance(OxidationStage.EXPOSED, 3, 0) - 1F / 16F) < 1e-6, "3 same-age neighbors: (1/4)^2");

		// stages
		check(OxidationStage.UNAFFECTED.next() == OxidationStage.EXPOSED && OxidationStage.OXIDIZED.next() == OxidationStage.OXIDIZED, "next");
		check(OxidationStage.UNAFFECTED.previous() == OxidationStage.UNAFFECTED && OxidationStage.OXIDIZED.previous() == OxidationStage.WEATHERED, "previous");
		check(OxidationStage.byName("bogus") == null && OxidationStage.fromSerializedName("bogus") == OxidationStage.UNAFFECTED, "unknown stage name");

		// item NBT
		ItemStack plain = new ItemStack(Items.IRON_BARS);
		ItemStack weatheredWaxed = OxidationItemData.staged(plain, OxidationStage.WEATHERED, true);
		check(plain.getTag() == null, "staged() leaves the base stack alone");
		check(OxidationItemData.read(weatheredWaxed).equals(Optional.of(new OxidationItemData.StoredState(OxidationStage.WEATHERED, true))), "round trip weathered waxed");
		check(!weatheredWaxed.hasCustomHoverName(), "no stored custom name (R3)");
		ItemStack back = weatheredWaxed.copy();
		OxidationItemData.write(back, OxidationStage.UNAFFECTED, false);
		check(back.getTag() == null && ItemStack.isSameItemSameTags(back, plain), "back to plain: no empty tag, stacks with a fresh item");
		check(OxidationItemData.read(OxidationItemData.staged(plain, OxidationStage.UNAFFECTED, true)).isPresent(), "waxed unaffected is a variant");
		check("item.creativeoxidation.variant.waxed.weathered".equals(OxidationItemData.variantKey(new OxidationItemData.StoredState(OxidationStage.WEATHERED, true))), "variant lang key");

		// R10 staged ingredient
		StagedIngredient unaffected = new StagedIngredient(Items.IRON_BARS, OxidationStage.UNAFFECTED, false);
		StagedIngredient exposed = new StagedIngredient(Items.IRON_BARS, OxidationStage.EXPOSED, false);
		ItemStack exposedStack = OxidationItemData.staged(plain, OxidationStage.EXPOSED, false);
		check(unaffected.test(plain), "unaffected ingredient takes a plain item");
		check(!unaffected.test(exposedStack), "unaffected ingredient refuses a staged item");
		check(!unaffected.test(OxidationItemData.staged(plain, OxidationStage.UNAFFECTED, true)), "unaffected ingredient refuses waxed");
		check(exposed.test(exposedStack) && !exposed.test(plain), "exposed ingredient takes exposed only");
		check(!exposed.test(OxidationItemData.staged(plain, OxidationStage.EXPOSED, true)), "exposed ingredient refuses waxed exposed");
		check(!exposed.test(OxidationItemData.staged(new ItemStack(Items.OAK_DOOR), OxidationStage.EXPOSED, false)), "other item refused");
		check(!exposed.isSimple(), "not a simple ingredient (NBT matters)");
		check(exposed.getItems().length == 1 && OxidationItemData.read(exposed.getItems()[0]).isPresent(), "display stack carries the stage");
		StagedIngredient parsed = StagedIngredient.Serializer.INSTANCE.parse(exposed.toJson().getAsJsonObject());
		check(parsed.item() == Items.IRON_BARS && parsed.stage() == OxidationStage.EXPOSED && !parsed.waxed(), "JSON round trip");
		check(net.minecraft.world.item.crafting.Ingredient.of(Items.IRON_BARS).test(exposedStack), "a plain item ingredient still takes a staged item (R15)");

		// P3 drops, placer items, display name
		check(OxidationRules.dropKeepsStage(true, false, OxidationStage.WEATHERED) && OxidationRules.dropKeepsStage(true, true, OxidationStage.UNAFFECTED),
				"keepStageOnBreak on: staged or waxed drops keep the stage");
		check(!OxidationRules.dropKeepsStage(false, true, OxidationStage.OXIDIZED), "keepStageOnBreak off: drops plain");
		check(!OxidationRules.dropKeepsStage(true, false, OxidationStage.UNAFFECTED), "plain block: nothing written");
		check(OxidationTargets.isTargetItem("create:fluid_pipe", "create:fluid_pipe"), "fluid pipe item takes the stage");
		check(OxidationTargets.isTargetItem("createdeco:copper_coinstack", null), "coin stack item (not a BlockItem) takes the stage");
		check(!OxidationTargets.isTargetItem("minecraft:copper_ingot", null) && !OxidationTargets.isTargetItem("create:andesite_casing", "create:andesite_casing"),
				"other drops stay plain");
		check("createdeco:copper_coinstack_block".equals(OxidationTargets.placedBlockOf("createdeco:copper_coinstack"))
				&& OxidationTargets.placedBlockOf("create:fluid_pipe") == null, "placer item map");
		check(OxidationTargets.isTarget(OxidationTargets.PLACER_ITEMS.get("createdeco:copper_coinstack")), "placer item places a target");
		net.minecraft.network.chat.Component base = net.minecraft.network.chat.Component.literal("Fluid Pipe");
		net.minecraft.network.chat.Component named = OxidationItemData.displayName(weatheredWaxed, base);
		check(named.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc
				&& "item.creativeoxidation.variant.waxed.weathered".equals(tc.getKey()) && tc.getArgs().length == 1 && tc.getArgs()[0] == base,
				"display name: Waxed Weathered %s around the item name");
		check(OxidationItemData.displayName(plain, base) == base, "plain item keeps its name");
		java.util.Map<String, String> lang = new com.google.gson.Gson().fromJson(java.nio.file.Files.readString(java.nio.file.Path.of(
				"development/creativeEngineering/members/creativeoxidation/src/main/resources/assets/creativeoxidation/lang/en_us.json")),
				new com.google.gson.reflect.TypeToken<java.util.Map<String, String>>() {}.getType());
		boolean allKeys = true;
		for (OxidationStage st : OxidationStage.values()) for (boolean w : new boolean[]{false, true}) {
			OxidationItemData.StoredState ss = new OxidationItemData.StoredState(st, w);
			if (!ss.isPlain() && !lang.containsKey(OxidationItemData.variantKey(ss))) allKeys = false;
		}
		check(allKeys, "every variant name key is in en_us.json");

		// P4 honeycomb, scrapers, soap
		check(OxidationRules.toolApplies(OxidationRules.Tool.HONEYCOMB, false, OxidationStage.OXIDIZED), "honeycomb waxes an unwaxed block");
		check(!OxidationRules.toolApplies(OxidationRules.Tool.HONEYCOMB, true, OxidationStage.EXPOSED), "honeycomb on a waxed block: no use");
		check(OxidationRules.toolApplies(OxidationRules.Tool.SCRAPER, true, OxidationStage.UNAFFECTED), "scraper takes wax off a waxed plain block");
		check(OxidationRules.toolApplies(OxidationRules.Tool.SCRAPER, false, OxidationStage.EXPOSED), "scraper scrapes an aged block");
		check(!OxidationRules.toolApplies(OxidationRules.Tool.SCRAPER, false, OxidationStage.UNAFFECTED), "scraper on a plain block: no use");
		check(OxidationRules.soapWashes(true, OxidationStage.OXIDIZED) && OxidationRules.soapWashes(false, OxidationStage.EXPOSED)
				&& OxidationRules.soapWashes(true, OxidationStage.UNAFFECTED), "soap washes waxed or aged");
		check(!OxidationRules.soapWashes(false, OxidationStage.UNAFFECTED), "soap on a plain block: no use");

		System.out.println(fails == 0 ? "ALL " + checks + " OK" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}
}
