package com.SaintAlfonzo.creativedelight.compat.farmersrespite;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import umpaz.farmersrespite.common.block.entity.KettleBlockEntity;

/**
 * Rules of the patched Kettle tank (farmers_respite.md 2.1): four units of {@link #UNIT} mB, one fluid at a time,
 * only fluids the tank holds ({@link KettleDrinks#tankHolds}, K3). Whole units only, from pipes (K6), right-click (K7),
 * the dispenser and the GUI boxes (K4).
 */
public final class KettleTank {

	public static final int UNIT = 250;
	public static final int MAX_LEVEL = 4;
	public static final int CAPACITY = UNIT * MAX_LEVEL;
	/** Ticks between two box transfers (one unit each). */
	private static final int BOX_INTERVAL = 4;

	private KettleTank() {}

	public enum Use { PASS, DONE, REFUSED }

	/** Units of {@code offer} the tank takes now (0 if the fluid is not allowed or another fluid is in). */
	public static int roomFor(BlockEntity be, FluidStack offer) {
		KettleTankHolder tank = (KettleTankHolder) be;
		if (offer.isEmpty() || !KettleDrinks.tankHolds(be.getLevel(), offer)) return 0;
		FluidStack in = tank.ce$tankFluid();
		if (!in.isEmpty() && !in.isFluidEqual(offer)) return 0;
		return MAX_LEVEL - tank.ce$tankLevel();
	}

	/** Adds {@code units} of {@code type}; the caller checked {@link #roomFor}. */
	public static void add(BlockEntity be, FluidStack type, int units) {
		KettleTankHolder tank = (KettleTankHolder) be;
		tank.ce$setTank(type, Math.min(MAX_LEVEL, tank.ce$tankLevel() + units));
	}

	public static void remove(BlockEntity be, int units) {
		KettleTankHolder tank = (KettleTankHolder) be;
		tank.ce$setTank(tank.ce$tankFluid(), Math.max(0, tank.ce$tankLevel() - units));
	}

	// ---------------------------------------------------------------------------------------------------------
	// Right-click (K7) and dispenser

	/**
	 * A held item used on the Kettle: a drink or water bottle pours one unit, a glass bottle takes one unit as its
	 * drink, a filled bucket fills the tank (the rest of the bucket is lost, as 2.0 did with water), an empty bucket
	 * takes a full tank of a fluid that has a bucket. REFUSED stops 2.0's own water handling (it would add water on
	 * top of another fluid); PASS leaves the click to 2.0 (serving, the GUI, the lid).
	 */
	public static Use use(KettleBlockEntity kettle, Player player, InteractionHand hand) {
		BlockEntity be = kettle;
		Level level = be.getLevel();
		ItemStack held = player.getItemInHand(hand);
		if (held.isEmpty() || level == null) return Use.PASS;
		KettleTankHolder tank = (KettleTankHolder) be;

		FluidStack poured = KettleDrinks.fluidOf(level, held);
		if (!poured.isEmpty()) {
			if (roomFor(be, poured) < 1) return isWaterBottle(held) ? Use.REFUSED : Use.PASS;
			add(be, poured, 1);
			give(player, hand, held, new ItemStack(Items.GLASS_BOTTLE));
			sound(level, be.getBlockPos(), SoundEvents.BOTTLE_EMPTY);
			return Use.DONE;
		}

		if (held.is(Items.GLASS_BOTTLE)) {
			if (!kettle.getMeal().isEmpty() && kettle.isContainerValid(held)) return Use.PASS;
			ItemStack drink = KettleDrinks.bottleOf(level, tank.ce$tankFluid());
			if (drink.isEmpty()) return Use.PASS;
			remove(be, 1);
			give(player, hand, held, drink);
			sound(level, be.getBlockPos(), SoundEvents.BOTTLE_FILL);
			return Use.DONE;
		}

		Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(held, 1)).resolve();
		if (cap.isEmpty()) return Use.PASS;
		IFluidHandlerItem item = cap.get();
		FluidStack contained = item.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
		if (!contained.isEmpty()) {
			int units = contained.getAmount() / UNIT;
			if (units < 1 || roomFor(be, contained) < 1) return held.is(Items.WATER_BUCKET) ? Use.REFUSED : Use.PASS;
			FluidStack drained = item.drain(contained, IFluidHandler.FluidAction.EXECUTE);
			if (drained.isEmpty()) return Use.PASS;
			add(be, drained, units);
			give(player, hand, held, item.getContainer());
			SoundEvent s = drained.getFluid().getFluidType().getSound(drained, net.minecraftforge.common.SoundActions.BUCKET_EMPTY);
			sound(level, be.getBlockPos(), s != null ? s : SoundEvents.BUCKET_EMPTY);
			return Use.DONE;
		}
		FluidStack in = tank.ce$tankFluid();
		if (tank.ce$tankLevel() < MAX_LEVEL || in.isEmpty()) return Use.PASS;
		FluidStack full = new FluidStack(in, CAPACITY);
		if (item.fill(full, IFluidHandler.FluidAction.SIMULATE) != CAPACITY) return Use.PASS;
		item.fill(full, IFluidHandler.FluidAction.EXECUTE);
		tank.ce$setTank(in, 0);
		give(player, hand, held, item.getContainer());
		SoundEvent s = in.getFluid().getFluidType().getSound(in, net.minecraftforge.common.SoundActions.BUCKET_FILL);
		sound(level, be.getBlockPos(), s != null ? s : SoundEvents.BUCKET_FILL);
		return Use.DONE;
	}

	/** Dispenser: what comes back out, or null when the item is not one the tank deals with. */
	public static ItemStack dispense(BlockEntity be, ItemStack stack) {
		Level level = be.getLevel();
		FluidStack poured = KettleDrinks.fluidOf(level, stack);
		if (!poured.isEmpty()) {
			if (roomFor(be, poured) < 1) return stack;
			add(be, poured, 1);
			sound(level, be.getBlockPos(), SoundEvents.BOTTLE_EMPTY);
			stack.shrink(1);
			return stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : leftover(be, stack, new ItemStack(Items.GLASS_BOTTLE));
		}
		if (stack.is(Items.WATER_BUCKET)) {
			FluidStack water = new FluidStack(net.minecraft.world.level.material.Fluids.WATER, UNIT);
			if (roomFor(be, water) < 1) return stack;
			add(be, water, MAX_LEVEL);
			sound(level, be.getBlockPos(), SoundEvents.BUCKET_EMPTY);
			return new ItemStack(Items.BUCKET);
		}
		return null;
	}

	private static ItemStack leftover(BlockEntity be, ItemStack rest, ItemStack out) {
		net.minecraft.world.level.block.Block.popResource(be.getLevel(), be.getBlockPos().above(), out);
		return rest;
	}

	private static boolean isWaterBottle(ItemStack stack) {
		return stack.is(Items.POTION);
	}

	private static void give(Player player, InteractionHand hand, ItemStack held, ItemStack result) {
		player.setItemInHand(hand, net.minecraft.world.item.ItemUtils.createFilledResult(held, player, result));
	}

	private static void sound(Level level, BlockPos pos, SoundEvent sound) {
		level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
		level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
	}

	// ---------------------------------------------------------------------------------------------------------
	// GUI boxes (K4)

	/** One unit through the boxes every {@link #BOX_INTERVAL} ticks, in the latched direction. Server side. */
	public static boolean tickBoxes(BlockEntity be) {
		Level level = be.getLevel();
		if (level == null || level.getGameTime() % BOX_INTERVAL != 0) return false;
		KettleTankHolder tank = (KettleTankHolder) be;
		ItemStackHandler boxes = tank.ce$boxes();
		if (tank.ce$pouring()) {
			ItemStack drink = boxes.getStackInSlot(KettleTankHolder.FILL_BOX);
			FluidStack poured = KettleDrinks.fluidOf(level, drink);
			if (poured.isEmpty() || roomFor(be, poured) < 1) return false;
			ItemStack empty = new ItemStack(Items.GLASS_BOTTLE);
			if (!boxes.insertItem(KettleTankHolder.EMPTY_BOX, empty, true).isEmpty()) return false;
			add(be, poured, 1);
			boxes.extractItem(KettleTankHolder.FILL_BOX, 1, false);
			boxes.insertItem(KettleTankHolder.EMPTY_BOX, empty, false);
			return true;
		}
		ItemStack bottles = boxes.getStackInSlot(KettleTankHolder.EMPTY_BOX);
		if (!bottles.is(Items.GLASS_BOTTLE) || tank.ce$tankLevel() < 1) return false;
		ItemStack drink = KettleDrinks.bottleOf(level, tank.ce$tankFluid());
		if (drink.isEmpty() || !boxes.insertItem(KettleTankHolder.FILL_BOX, drink, true).isEmpty()) return false;
		remove(be, 1);
		boxes.extractItem(KettleTankHolder.EMPTY_BOX, 1, false);
		boxes.insertItem(KettleTankHolder.FILL_BOX, drink, false);
		return true;
	}

	/** What a box slot accepts from a player: drink bottles the tank could hold (fill box), glass bottles (empty box). */
	public static boolean boxAccepts(BlockEntity be, int box, ItemStack stack) {
		if (box == KettleTankHolder.EMPTY_BOX) return stack.is(Items.GLASS_BOTTLE);
		return KettleDrinks.isPourable(be.getLevel(), stack);
	}

	// ---------------------------------------------------------------------------------------------------------
	// Pipes and pumps (K6)

	/** The tank on every side: valid fluids only, whole units in and out. */
	public static final class Handler implements IFluidHandler {

		private final BlockEntity be;

		public Handler(BlockEntity be) {
			this.be = be;
		}

		@Override
		public int getTanks() {
			return 1;
		}

		@Override
		public FluidStack getFluidInTank(int tank) {
			return ((KettleTankHolder) be).ce$tankFluid();
		}

		@Override
		public int getTankCapacity(int tank) {
			return CAPACITY;
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return KettleDrinks.tankHolds(be.getLevel(), stack);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			int units = Math.min(resource.getAmount() / UNIT, roomFor(be, resource));
			if (units < 1) return 0;
			if (action.execute()) add(be, resource, units);
			return units * UNIT;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			FluidStack in = ((KettleTankHolder) be).ce$tankFluid();
			if (in.isEmpty() || !in.isFluidEqual(resource)) return FluidStack.EMPTY;
			return drain(resource.getAmount(), action);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			KettleTankHolder tank = (KettleTankHolder) be;
			FluidStack in = tank.ce$tankFluid();
			int units = Math.min(maxDrain / UNIT, tank.ce$tankLevel());
			if (in.isEmpty() || units < 1) return FluidStack.EMPTY;
			if (action.execute()) remove(be, units);
			return new FluidStack(in, units * UNIT);
		}
	}
}
