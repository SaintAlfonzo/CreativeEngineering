package com.SaintAlfonzo.creativedevtools.test;

import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.Container;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** World verbs of {@code /cdevtest}: read a block, its block entity, inventory, tanks and energy; scan a box; place;
 * act as a player (use, break, kill, loot); read item and entity lists and a player's state. */
final class TestWorld {

	/** Box scans refuse more than this many positions (a 64x64x64 box). */
	static final long MAX_BOX = 262_144;

	private TestWorld() {
	}

	static void register() {
		TestCommand.add("state", TestWorld::state);
		TestCommand.add("be", TestWorld::be);
		TestCommand.add("inv", TestWorld::inv);
		TestCommand.add("tank", TestWorld::tank);
		TestCommand.add("energy", TestWorld::energy);
		TestCommand.add("count", TestWorld::count);
		TestCommand.add("ages", TestWorld::ages);
		TestCommand.add("place", TestWorld::place);
		TestCommand.add("items", TestWorld::items, "keep");
		TestCommand.add("entities", TestWorld::entities, "nbt");
		TestCommand.add("use", TestWorld::use, "sneak");
		TestCommand.add("useair", TestWorld::useAir, "sneak");
		TestCommand.add("break", TestWorld::breakBlock, "keep", "sneak");
		TestCommand.add("kill", TestWorld::kill, "keep", "fire");
		TestCommand.add("loot", TestWorld::loot);
		TestCommand.add("player", TestWorld::player);
		TestCommand.add("chunk", TestWorld::chunk);
	}

	static String blockId(BlockState st) {
		return String.valueOf(ForgeRegistries.BLOCKS.getKey(st.getBlock()));
	}

	static String stateStr(BlockState st) {
		return BlockStateParser.serialize(st);
	}

	// ---- block reads

	/** state X Y Z: block state, fluid, block entity type, shape top, light, biome. */
	private static void state(TestCtx c) {
		ServerLevel level = c.level();
		BlockPos pos = c.pos("pos");
		c.args.end();
		c.args.checkOpts("dim");
		TestCtx.loaded(level, pos);
		BlockState st = level.getBlockState(pos);
		FluidState fl = st.getFluidState();
		BlockEntity be = level.getBlockEntity(pos);
		VoxelShape shape = st.getShape(level, pos);
		c.emit(c.line().kv("pos", pos.getX() + "," + pos.getY() + "," + pos.getZ())
				.kv("state", stateStr(st))
				.kv("fluid", fl.isEmpty() ? "none" : ForgeRegistries.FLUIDS.getKey(fl.getType()) + ":" + fl.getAmount())
				.kv("be", be == null ? "none" : String.valueOf(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(be.getType())))
				.kv("shape_top", shape.isEmpty() ? "none" : shape.max(Direction.Axis.Y))
				.kv("random_ticking", st.isRandomlyTicking())
				.kv("light_block", level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos))
				.kv("light_sky", level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos))
				.kv("biome", level.getBiome(pos).unwrapKey().map(k -> k.location().toString()).orElse("?"))
				.kv("gametime", level.getGameTime()));
	}

	/** be X Y Z: the block entity's full NBT. */
	private static void be(TestCtx c) {
		ServerLevel level = c.level();
		BlockPos pos = c.pos("pos");
		c.args.end();
		c.args.checkOpts("dim");
		TestCtx.loaded(level, pos);
		BlockEntity be = level.getBlockEntity(pos);
		if (be == null) {
			c.emit(c.line().kv("be", "none"));
			return;
		}
		CompoundTag tag = be.saveWithFullMetadata();
		c.emit(c.line().kv("be", ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(be.getType())).kv("nbt", tag.toString()));
	}

	private static Direction side(TestCtx c) {
		String f = c.args.opt("face", "null");
		return f.equals("null") ? null : TestCtx.face(f);
	}

	static BlockEntity beAt(TestCtx c, ServerLevel level, BlockPos pos) {
		TestCtx.loaded(level, pos);
		BlockEntity be = level.getBlockEntity(pos);
		if (be == null) throw new TestArgs.Bad("no block entity at " + pos.getX() + "," + pos.getY() + "," + pos.getZ());
		return be;
	}

	/** inv X Y Z [face=]: the item handler automation sees from that face (null = no side), else the Container. */
	private static void inv(TestCtx c) {
		ServerLevel level = c.level();
		BlockPos pos = c.pos("pos");
		c.args.end();
		c.args.checkOpts("dim", "face");
		invLines(c, level, pos, side(c), null);
	}

	static void invLines(TestCtx c, ServerLevel level, BlockPos pos, Direction side, Long t) {
		BlockEntity be = beAt(c, level, pos);
		IItemHandler h = be.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
		List<ItemStack> slots = new ArrayList<>();
		String via;
		if (h != null) {
			via = "item_handler";
			for (int i = 0; i < h.getSlots(); i++) slots.add(h.getStackInSlot(i));
		} else if (be instanceof Container ct) {
			via = "container";
			for (int i = 0; i < ct.getContainerSize(); i++) slots.add(ct.getItem(i));
		} else {
			c.emit(withT(c.line(), t).kv("via", "none").kv("slots", 0));
			return;
		}
		int filled = 0;
		StringBuilder all = new StringBuilder();
		for (int i = 0; i < slots.size(); i++) {
			ItemStack st = slots.get(i);
			if (st.isEmpty()) continue;
			filled++;
			if (t == null) c.emit(c.line().kv("slot", i).kv("item", TestCtx.describe(st)));
			else all.append(all.length() == 0 ? "" : ",").append(i).append(':').append(TestCtx.describe(st));
		}
		TestLine sum = withT(c.line(), t).kv("via", via).kv("slots", slots.size()).kv("filled", filled);
		if (t != null) sum.kv("items", all.length() == 0 ? "-" : all.toString());
		c.emit(sum);
	}

	static TestLine withT(TestLine l, Long t) {
		return t == null ? l : l.kv("t", t);
	}

	/** tank X Y Z [face=]: every tank of the fluid handler. */
	private static void tank(TestCtx c) {
		ServerLevel level = c.level();
		BlockPos pos = c.pos("pos");
		c.args.end();
		c.args.checkOpts("dim", "face");
		tankLines(c, level, pos, side(c), null);
	}

	static void tankLines(TestCtx c, ServerLevel level, BlockPos pos, Direction side, Long t) {
		BlockEntity be = beAt(c, level, pos);
		IFluidHandler h = be.getCapability(ForgeCapabilities.FLUID_HANDLER, side).orElse(null);
		if (h == null) {
			c.emit(withT(c.line(), t).kv("tanks", 0));
			return;
		}
		StringBuilder all = new StringBuilder();
		for (int i = 0; i < h.getTanks(); i++) {
			FluidStack fs = h.getFluidInTank(i);
			String f = fs.isEmpty() ? "empty" : ForgeRegistries.FLUIDS.getKey(fs.getFluid()) + "*" + fs.getAmount()
					+ (fs.hasTag() ? fs.getTag().toString() : "");
			all.append(all.length() == 0 ? "" : ",").append(i).append(':').append(f).append('/').append(h.getTankCapacity(i));
		}
		c.emit(withT(c.line(), t).kv("tanks", h.getTanks()).kv("fluids", all.length() == 0 ? "-" : all.toString()));
	}

	/** energy X Y Z [face=]: the energy storage (Forge Energy). */
	private static void energy(TestCtx c) {
		ServerLevel level = c.level();
		BlockPos pos = c.pos("pos");
		c.args.end();
		c.args.checkOpts("dim", "face");
		BlockEntity be = beAt(c, level, pos);
		IEnergyStorage e = be.getCapability(ForgeCapabilities.ENERGY, side(c)).orElse(null);
		if (e == null) c.emit(c.line().kv("energy", "none"));
		else c.emit(c.line().kv("energy", e.getEnergyStored()).kv("max", e.getMaxEnergyStored())
				.kv("extract", e.canExtract()).kv("receive", e.canReceive()));
	}

	// ---- box scans

	interface BoxVisitor {
		void visit(BlockPos.MutableBlockPos pos, BlockState st);
	}

	/** Visits every loaded position of the box; returns the number of columns skipped as unloaded. */
	static int scan(ServerLevel level, int[] a, int[] b, BoxVisitor v) {
		if (TestArgs.volume(a, b) > MAX_BOX) throw new TestArgs.Bad("box over " + MAX_BOX + " blocks");
		int skipped = 0;
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (int x = Math.min(a[0], b[0]); x <= Math.max(a[0], b[0]); x++) {
			for (int z = Math.min(a[2], b[2]); z <= Math.max(a[2], b[2]); z++) {
				if (!level.hasChunk(x >> 4, z >> 4)) {
					skipped++;
					continue;
				}
				for (int y = Math.min(a[1], b[1]); y <= Math.max(a[1], b[1]); y++) {
					m.set(x, y, z);
					v.visit(m, level.getBlockState(m));
				}
			}
		}
		return skipped;
	}

	/** A block id or #tag matcher. */
	static java.util.function.Predicate<BlockState> blockMatcher(String spec) {
		if (spec.startsWith("#")) {
			TagKey<Block> tag = TagKey.create(Registry.BLOCK_REGISTRY, TestCtx.id(spec.substring(1)));
			return st -> st.is(tag);
		}
		ResourceLocation id = TestCtx.id(spec);
		if (!ForgeRegistries.BLOCKS.containsKey(id)) throw new TestArgs.Bad("no such block " + spec);
		Block block = ForgeRegistries.BLOCKS.getValue(id);
		return st -> st.is(block);
	}

	/** count X1 Y1 Z1 X2 Y2 Z2 BLOCK|#TAG: matches, and what each match stands on. */
	private static void count(TestCtx c) {
		ServerLevel level = c.level();
		int[] a = c.args.xyz("from"), b = c.args.xyz("to");
		String spec = c.args.word("block");
		c.args.end();
		c.args.checkOpts("dim");
		var match = blockMatcher(spec);
		int[] n = {0};
		Map<String, Integer> on = new TreeMap<>();
		Map<String, Integer> ids = new TreeMap<>();
		int skipped = scan(level, a, b, (p, st) -> {
			if (!match.test(st)) return;
			n[0]++;
			ids.merge(blockId(st), 1, Integer::sum);
			BlockPos below = p.below();
			on.merge(level.hasChunkAt(below) ? blockId(level.getBlockState(below)) : "unloaded", 1, Integer::sum);
		});
		TestLine l = c.line().kv("block", spec).kv("n", n[0]).map("on", on);
		if (spec.startsWith("#")) l.map("ids", ids);
		c.emit(l.kv("skipped_columns", skipped));
	}

	/** ages X1 Y1 Z1 X2 Y2 Z2 BLOCK|#TAG [prop=age]: histogram and mean of an integer property. */
	private static void ages(TestCtx c) {
		ServerLevel level = c.level();
		int[] a = c.args.xyz("from"), b = c.args.xyz("to");
		String spec = c.args.word("block");
		c.args.end();
		c.args.checkOpts("dim", "prop");
		String prop = c.args.opt("prop", "age");
		var match = blockMatcher(spec);
		Map<String, Integer> hist = new TreeMap<>((x, y) -> {
			try {
				return Integer.compare(Integer.parseInt(x), Integer.parseInt(y));
			} catch (NumberFormatException e) {
				return x.compareTo(y);
			}
		});
		long[] sum = {0, 0, 0};
		int skipped = scan(level, a, b, (p, st) -> {
			if (!match.test(st)) return;
			String v = "none";
			for (Property<?> pr : st.getProperties()) {
				if (pr.getName().equals(prop)) v = String.valueOf(st.getValue(pr)).toLowerCase(java.util.Locale.ROOT);
			}
			hist.merge(v, 1, Integer::sum);
			sum[2]++;
			try {
				sum[0] += Integer.parseInt(v);
				sum[1]++;
			} catch (NumberFormatException ignored) {
			}
		});
		c.emit(c.line().kv("block", spec).kv("prop", prop).kv("n", sum[2])
				.kv("mean", sum[1] == 0 ? "-" : String.format(java.util.Locale.ROOT, "%.3f", (double) sum[0] / sum[1]))
				.map("hist", hist).kv("skipped_columns", skipped));
	}

	/** place X Y Z STATE [flags=3]: setBlock with those flags (18 = no neighbor or shape updates). */
	private static void place(TestCtx c) throws Exception {
		ServerLevel level = c.level();
		BlockPos pos = c.pos("pos");
		String spec = c.args.word("state");
		c.args.end();
		c.args.checkOpts("dim", "flags");
		TestCtx.loaded(level, pos);
		BlockState st = BlockStateParser.parseForBlock(Registry.BLOCK, spec, false).blockState();
		boolean ok = level.setBlock(pos, st, c.args.optInt("flags", 3, 0, 1023));
		c.emit(c.line().kv("placed", ok).kv("state", stateStr(level.getBlockState(pos))));
	}

	// ---- entities

	/** items X Y Z R [keep]: item entities within R summed by id, XP orb value, then discarded unless keep. */
	private static void items(TestCtx c) {
		ServerLevel level = c.level();
		BlockPos pos = c.pos("pos");
		double r = c.args.number("radius");
		c.args.end();
		c.args.checkOpts("dim", "keep");
		TestCtx.loaded(level, pos);
		sumDrops(c, level, pos, r, c.args.flag("keep"), "items");
	}

	static void sumDrops(TestCtx c, ServerLevel level, BlockPos pos, double r, boolean keep, String key) {
		AABB box = new AABB(pos).inflate(r);
		Map<String, Integer> by = new TreeMap<>();
		int stacks = 0;
		for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, box)) {
			by.merge(TestCtx.key(ie.getItem().getItem()), ie.getItem().getCount(), Integer::sum);
			stacks++;
			if (!keep) ie.discard();
		}
		int xp = 0;
		for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, box)) {
			xp += orb.getValue();
			if (!keep) orb.discard();
		}
		c.emit(c.line().map(key, by).kv("stacks", stacks).kv("xp", xp).kv("kept", keep));
	}

	/** entities X Y Z R [type=id|#tag] [nbt]: one line per entity (first 100), then the count. */
	private static void entities(TestCtx c) {
		ServerLevel level = c.level();
		BlockPos pos = c.pos("pos");
		double r = c.args.number("radius");
		c.args.end();
		c.args.checkOpts("dim", "type", "nbt");
		TestCtx.loaded(level, pos);
		String type = c.args.opt("type", null);
		java.util.function.Predicate<Entity> match = e -> true;
		if (type != null && type.startsWith("#")) {
			TagKey<EntityType<?>> tag = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, TestCtx.id(type.substring(1)));
			match = e -> e.getType().is(tag);
		} else if (type != null) {
			ResourceLocation id = TestCtx.id(type);
			match = e -> id.equals(EntityType.getKey(e.getType()));
		}
		int n = 0;
		Map<String, Integer> by = new TreeMap<>();
		for (Entity e : level.getEntities((Entity) null, new AABB(pos).inflate(r), match)) {
			n++;
			by.merge(String.valueOf(EntityType.getKey(e.getType())), 1, Integer::sum);
			if (n > 100) continue;
			TestLine l = c.line().kv("type", EntityType.getKey(e.getType())).kv("uuid", e.getStringUUID().substring(0, 8))
					.kv("x", e.getX()).kv("y", e.getY()).kv("z", e.getZ());
			if (e instanceof LivingEntity le) l.kv("health", le.getHealth()).kv("max", le.getMaxHealth());
			if (e.hasCustomName()) l.kv("name", e.getCustomName().getString());
			if (c.args.flag("nbt")) l.kv("nbt", e.saveWithoutId(new CompoundTag()).toString());
			c.emit(l);
		}
		c.emit(c.line().kv("n", n).map("by_type", by));
	}

	// ---- acting as a player

	private static InteractionHand[] hands(String h) {
		return switch (h) {
			case "main" -> new InteractionHand[]{InteractionHand.MAIN_HAND};
			case "off" -> new InteractionHand[]{InteractionHand.OFF_HAND};
			case "both", "client" -> new InteractionHand[]{InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND};
			default -> throw new TestArgs.Bad("hand must be main, off, both or client");
		};
	}

	/**
	 * use X Y Z [as=NAME] [held=ITEM] [face=up] [hit=dx,dy,dz] [hand=main|off|both|client] [sneak]: a right click on
	 * the block. both = the off hand when the main hand did not consume the click; client = the client's full order
	 * (per hand: block use; FAIL stops; then the item's air use when the hand is not empty).
	 */
	private static void use(TestCtx c) {
		ServerLevel level = c.level();
		BlockPos pos = c.pos("pos");
		c.args.end();
		c.args.checkOpts("dim", "as", "held", "face", "hit", "hand", "sneak");
		TestCtx.loaded(level, pos);
		Direction face = TestCtx.face(c.args.opt("face", "up"));
		double[] hit = c.args.optTriple("hit");
		Vec3 at = hit == null ? Vec3.atCenterOf(pos).relative(face, 0.5)
				: new Vec3(pos.getX() + hit[0], pos.getY() + hit[1], pos.getZ() + hit[2]);
		ServerPlayer p = c.actor(level, pos);
		String mode = c.args.opt("hand", "main");
		BlockHitResult bhr = new BlockHitResult(at, face, pos, false);
		for (InteractionHand hand : hands(mode)) {
			ItemStack held = p.getItemInHand(hand);
			InteractionResult r = p.gameMode.useItemOn(p, level, held, hand, bhr);
			c.emit(c.line().kv("who", TestCtx.who(p)).kv("hand", hand == InteractionHand.MAIN_HAND ? "main" : "off")
					.kv("action", "block").kv("result", r));
			if (r.consumesAction()) break;
			if (mode.equals("client")) {
				if (r == InteractionResult.FAIL) break;
				if (p.getItemInHand(hand).isEmpty()) continue;
				InteractionResult r2 = p.gameMode.useItem(p, level, p.getItemInHand(hand), hand);
				c.emit(c.line().kv("who", TestCtx.who(p)).kv("hand", hand == InteractionHand.MAIN_HAND ? "main" : "off")
						.kv("action", "air").kv("result", r2));
				if (r2.consumesAction()) break;
			}
		}
		afterAct(c, level, pos, p);
	}

	private static void afterAct(TestCtx c, ServerLevel level, BlockPos pos, ServerPlayer p) {
		TestLine l = c.line().kv("main", TestCtx.describe(p.getMainHandItem())).kv("off", TestCtx.describe(p.getOffhandItem()));
		if (pos != null) l.kv("state", stateStr(level.getBlockState(pos)));
		c.emit(l);
	}

	/** useair [as=NAME] [held=ITEM] [hand=main|off|both]: the item's air use (what the client sends after a block use
	 * passes; zq useitem). An item with a use duration only starts here: /pbot eat or useitem tick it. */
	private static void useAir(TestCtx c) {
		ServerLevel level = c.level();
		c.args.end();
		c.args.checkOpts("dim", "as", "held", "hand", "sneak");
		ServerPlayer p = c.actor(level, null);
		if (!(p instanceof net.minecraftforge.common.util.FakePlayer)) level = p.getLevel();
		for (InteractionHand hand : hands(c.args.opt("hand", "both"))) {
			ItemStack held = p.getItemInHand(hand);
			if (held.isEmpty()) continue;
			InteractionResult r = p.gameMode.useItem(p, level, held, hand);
			c.emit(c.line().kv("who", TestCtx.who(p)).kv("hand", hand == InteractionHand.MAIN_HAND ? "main" : "off")
					.kv("result", r).kv("using", p.isUsingItem()));
			if (r.consumesAction()) break;
		}
		afterAct(c, level, null, p);
	}

	/** break X Y Z [as=NAME] [held=ITEM] [keep]: a survival break through ServerPlayerGameMode.destroyBlock (BreakEvent,
	 * harvest check, tool damage), then the drops within 3 blocks summed (and discarded unless keep). */
	private static void breakBlock(TestCtx c) {
		ServerLevel level = c.level();
		BlockPos pos = c.pos("pos");
		c.args.end();
		c.args.checkOpts("dim", "as", "held", "keep", "sneak");
		TestCtx.loaded(level, pos);
		ServerPlayer p = c.actor(level, pos);
		String before = stateStr(level.getBlockState(pos));
		boolean ok = p.gameMode.destroyBlock(pos);
		c.emit(c.line().kv("who", TestCtx.who(p)).kv("broken", ok).kv("before", before)
				.kv("after", stateStr(level.getBlockState(pos))).kv("main", TestCtx.describe(p.getMainHandItem())));
		sumDrops(c, level, pos, 3, c.args.flag("keep"), "drops");
	}

	/** kill X Y Z TYPE [n=1] [held=ITEM] [as=NAME] [fire] [keep]: summons N mobs one at a time (as /summon does) and a
	 * player attack of 1000 kills each (fire: burning first); drops within 4 blocks summed. */
	private static void kill(TestCtx c) {
		ServerLevel level = c.level();
		BlockPos pos = c.pos("pos");
		ResourceLocation type = TestCtx.id(c.args.word("type"));
		c.args.end();
		c.args.checkOpts("dim", "n", "held", "as", "fire", "keep");
		TestCtx.loaded(level, pos);
		EntityType<?> et = ForgeRegistries.ENTITY_TYPES.getValue(type);
		if (et == null || !ForgeRegistries.ENTITY_TYPES.containsKey(type)) throw new TestArgs.Bad("no such entity " + type);
		int n = c.args.optInt("n", 1, 1, 500);
		ServerPlayer p = c.actor(level, pos);
		int killed = 0;
		for (int i = 0; i < n; i++) {
			Entity e = et.create(level);
			if (!(e instanceof LivingEntity le)) {
				if (e != null) e.discard();
				throw new TestArgs.Bad(type + " is not a living entity");
			}
			le.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0f, 0f);
			if (le instanceof Mob mob) mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.COMMAND, null, null);
			level.addFreshEntity(le);
			if (c.args.flag("fire")) le.setSecondsOnFire(10);
			le.hurt(DamageSource.playerAttack(p), 1000f);
			if (!le.isAlive()) killed++;
			else le.discard();
		}
		c.emit(c.line().kv("type", type).kv("n", n).kv("killed", killed).kv("who", TestCtx.who(p))
				.kv("main", TestCtx.describe(p.getMainHandItem())));
		sumDrops(c, level, pos, 4, c.args.flag("keep"), "drops");
	}

	/** loot X Y Z [tool=ITEM] [rolls=1]: the block's drops rolled through Block.getDrops, summed (nothing changes). */
	private static void loot(TestCtx c) {
		ServerLevel level = c.level();
		BlockPos pos = c.pos("pos");
		c.args.end();
		c.args.checkOpts("dim", "tool", "rolls");
		TestCtx.loaded(level, pos);
		int rolls = c.args.optInt("rolls", 1, 1, 100_000);
		ItemStack tool = TestCtx.stack(c.args.opt("tool", "none"));
		BlockState st = level.getBlockState(pos);
		BlockEntity be = level.getBlockEntity(pos);
		ServerPlayer p = c.actor(level, pos);
		p.setItemInHand(InteractionHand.MAIN_HAND, tool.copy());
		Map<String, Integer> by = new TreeMap<>();
		for (int i = 0; i < rolls; i++) {
			for (ItemStack d : Block.getDrops(st, level, pos, be, p, tool)) by.merge(TestCtx.key(d.getItem()), d.getCount(), Integer::sum);
		}
		c.emit(c.line().kv("state", stateStr(st)).kv("tool", TestCtx.describe(tool)).kv("rolls", rolls).map("drops", by));
	}

	// ---- players

	/** player NAME: position, health, food, air, XP, mode, pose, hands, effects of a player or bot. */
	private static void player(TestCtx c) {
		String name = c.args.word("name");
		c.args.end();
		c.args.checkOpts();
		ServerPlayer p = c.server.getPlayerList().getPlayerByName(name);
		if (p == null) throw new TestArgs.Bad("no player " + name);
		c.emit(c.line().kv("name", name).kv("dim", p.getLevel().dimension().location())
				.kv("x", p.getX()).kv("y", p.getY()).kv("z", p.getZ()).kv("yaw", p.getYRot()).kv("pitch", p.getXRot())
				.kv("health", p.getHealth()).kv("max", p.getMaxHealth()).kv("food", p.getFoodData().getFoodLevel())
				.kv("sat", p.getFoodData().getSaturationLevel()).kv("exhaustion", p.getFoodData().getExhaustionLevel())
				.kv("air", p.getAirSupply()).kv("xp_level", p.experienceLevel).kv("xp_total", p.totalExperience)
				.kv("mode", p.gameMode.getGameModeForPlayer().getName()).kv("invulnerable", p.isInvulnerable())
				.kv("shift", p.isShiftKeyDown()).kv("pose", p.getPose()).kv("sprint", p.isSprinting())
				.kv("on_ground", p.isOnGround()).kv("fall_flying", p.isFallFlying())
				.kv("using", p.isUsingItem() ? TestCtx.key(p.getUseItem().getItem()) + ":" + p.getUseItemRemainingTicks() : "no")
				.kv("selected", p.getInventory().selected).kv("main", TestCtx.describe(p.getMainHandItem()))
				.kv("off", TestCtx.describe(p.getOffhandItem())).kv("speed", p.getDeltaMovement().horizontalDistance()));
		StringBuilder sb = new StringBuilder();
		for (MobEffectInstance e : p.getActiveEffects()) {
			sb.append(sb.length() == 0 ? "" : ",").append(ForgeRegistries.MOB_EFFECTS.getKey(e.getEffect()))
					.append(':').append(e.getAmplifier()).append(':').append(e.getDuration());
		}
		c.emit(c.line().kv("name", name).kv("effects", sb.length() == 0 ? "-" : sb.toString()));
	}

	/** chunk X Z (block coordinates): ticket level, status and tickets (ServerChunkCache.getChunkDebugData). */
	private static void chunk(TestCtx c) {
		ServerLevel level = c.level();
		int x = c.args.integer("x"), z = c.args.integer("z");
		c.args.end();
		c.args.checkOpts("dim");
		ChunkPos cp = new ChunkPos(x >> 4, z >> 4);
		c.emit(c.line().kv("chunk", cp.x + "," + cp.z).kv("loaded", level.hasChunk(cp.x, cp.z))
				.kv("debug", level.getChunkSource().getChunkDebugData(cp)));
	}
}
