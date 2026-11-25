package com.SaintAlfonzo.creativedelight.farm.shippingbin;

import com.SaintAlfonzo.creativedelight.CDConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The bin's 12 slots (0 to 5 inputs, 6 to 11 outputs), its sale cycle and its carrier bookkeeping.
 * <p>Sale (section 3.1, ruling D3a): the stored value V is the credit plus every input stack with a
 * {@code farmingforblockheads:shipping_bin} row times its value; nothing happens below the capacity C. At V >= C a
 * {@value #SALE_TICKS}-tick bar fills, then the sale pays {@code floor(V / C)} loads: it takes whole items slot by slot
 * (slots 0 to 5, {@code ceil(needed / value)} from each) until the loads minus the credit are covered, keeps the
 * overshoot as credit, and rolls the payout table {@link ShippingBin#PAYOUT_TABLE} with the shipment value
 * ({@code loads x C}) and the sold stacks. The payout goes to the outputs; what does not fit waits in a buffer and blocks
 * selling until the outputs take it. {@value #COOLDOWN_TICKS} ticks of cooldown after a sale. Items without a row stay.
 * <p>Carriers (D5): a balloon under open sky; under a roof a crate on the bin and a courier ({@link CourierDispatch}), one
 * courier per bin at a time (a sale while one is on its way sends no other).
 */
public class ShippingBinBlockEntity extends BlockEntity implements WorldlyContainer, net.minecraft.world.MenuProvider {
	public static final int INPUTS = 6, SLOTS = 12, SALE_TICKS = 20, COOLDOWN_TICKS = 100;
	private static final int[] ALL_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

	private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
	private final List<ItemStack> buffer = new ArrayList<>();
	private int credit;
	private int saleProgress;
	private int cooldown;
	private int value; // stored value incl. credit, recomputed on change and every second (recipes can /reload)
	private boolean valueDirty = true;
	private boolean syncDirty;
	@Nullable
	private UUID courierId;
	@Nullable
	private UUID crateId;
	private int courierPhase;
	private long courierDeadline;

	private final ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> Math.min(value, Short.MAX_VALUE);
				case 1 -> Math.min(capacity(), Short.MAX_VALUE);
				case 2 -> saleProgress;
				case 3 -> fillPermille();
				default -> 0;
			};
		}

		@Override
		public void set(int index, int v) {
		}

		@Override
		public int getCount() {
			return 4;
		}
	};

	private final LazyOptional<?>[] sidedHandlers = SidedInvWrapper.create(this, Direction.values());

	public ShippingBinBlockEntity(BlockPos pos, BlockState state) {
		super(ShippingBin.SHIPPING_BIN_BLOCK_ENTITY.get(), pos, state);
	}

	static int capacity() {
		return CDConfig.SHIPPING_BIN.capacity();
	}

	public int storedValue() {
		return value;
	}

	public int credit() {
		return credit;
	}

	int fillPermille() {
		return (int) Math.min(1000L, value * 1000L / Math.max(1, capacity()));
	}

	ContainerData data() {
		return data;
	}

	/** Input stacks for the in-crate display (client copy). */
	public List<ItemStack> inputs() {
		return items.subList(0, INPUTS);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ShippingBinBlockEntity bin) {
		bin.tick((ServerLevel) level);
	}

	private void tick(ServerLevel level) {
		if (valueDirty || level.getGameTime() % 20L == 0L) recompute(level);
		if (courierId != null && level.getGameTime() % 20L == 0L) watchCourier(level);
		if (cooldown > 0) {
			cooldown--;
		} else if (drainBuffer() && value >= capacity()) {
			saleProgress++;
			if (saleProgress >= SALE_TICKS) {
				sell(level);
				saleProgress = 0;
				cooldown = COOLDOWN_TICKS;
			}
		} else {
			saleProgress = 0;
		}
		if (syncDirty) {
			syncDirty = false;
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
		}
	}

	private void recompute(Level level) {
		int total = credit;
		for (int slot = 0; slot < INPUTS; slot++) {
			ItemStack stack = items.get(slot);
			if (!stack.isEmpty()) total += ShippingBinRecipe.valueOf(level, stack) * stack.getCount();
		}
		if (total != value) {
			value = total;
			syncDirty = true;
		}
		valueDirty = false;
	}

	/** Moves buffered payout into free output space; true when the buffer is empty afterwards. */
	private boolean drainBuffer() {
		if (buffer.isEmpty()) return true;
		buffer.replaceAll(this::insertOutput);
		buffer.removeIf(ItemStack::isEmpty);
		return buffer.isEmpty();
	}

	/** Puts as much of {@code stack} into the outputs as fits; returns the rest. */
	private ItemStack insertOutput(ItemStack stack) {
		ItemStack rest = stack.copy();
		for (int slot = INPUTS; slot < SLOTS && !rest.isEmpty(); slot++) {
			ItemStack in = items.get(slot);
			if (in.isEmpty()) {
				items.set(slot, rest.copy());
				rest = ItemStack.EMPTY;
			} else if (ItemStack.isSameItemSameTags(in, rest)) {
				int move = Math.min(rest.getCount(), Math.min(in.getMaxStackSize(), getMaxStackSize()) - in.getCount());
				if (move > 0) {
					in.grow(move);
					rest.shrink(move);
				}
			}
		}
		if (rest.getCount() != stack.getCount()) setChanged();
		return rest;
	}

	private void sell(ServerLevel level) {
		int capacity = capacity();
		int[] counts = new int[INPUTS];
		int[] values = new int[INPUTS];
		for (int slot = 0; slot < INPUTS; slot++) {
			ItemStack stack = items.get(slot);
			counts[slot] = stack.getCount();
			values[slot] = stack.isEmpty() ? 0 : ShippingBinRecipe.valueOf(level, stack);
		}
		SaleMath.Sale sale = SaleMath.sell(counts, values, credit, capacity);
		if (sale.loads() <= 0) return;
		int shipment = sale.loads() * capacity;
		List<ItemStack> sold = new ArrayList<>();
		for (int slot = 0; slot < INPUTS; slot++) {
			int count = sale.taken()[slot];
			if (count <= 0) continue;
			ItemStack part = items.get(slot).copy();
			part.setCount(count);
			sold.add(part);
			items.get(slot).shrink(count);
		}
		credit = sale.credit();
		valueDirty = true;
		setChanged();

		LootTable table = level.getServer().getLootTables().get(ShippingBin.PAYOUT_TABLE);
		LootContext context = new LootContext.Builder(level).withRandom(level.random)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
				.withParameter(ShipmentLoot.SHIPMENT_VALUE, shipment)
				.withParameter(ShipmentLoot.SOLD_STACKS, List.copyOf(sold))
				.create(ShipmentLoot.PARAMS);
		for (ItemStack payout : table.getRandomItems(context)) {
			ItemStack rest = insertOutput(payout);
			if (!rest.isEmpty()) buffer.add(rest);
		}
		sendCarrier(level);
	}

	private void sendCarrier(ServerLevel level) {
		if (level.canSeeSky(worldPosition.above())) {
			ShippingBalloonEntity balloon = ShippingBin.SHIPPING_BALLOON.get().create(level);
			if (balloon != null) {
				balloon.moveTo(worldPosition.getX() + 0.5D, worldPosition.getY(), worldPosition.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
				level.addFreshEntity(balloon);
			}
			return;
		}
		if (courierId != null) return; // one courier at a time; it is already on its way
		ShippingCrateEntity crate = ShippingBin.SHIPPING_CRATE.get().create(level);
		if (crate != null) {
			crate.setBin(worldPosition);
			crate.moveTo(worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D,
					getBlockState().getValue(ShippingBinBlock.FACING).toYRot(), 0.0F);
			level.addFreshEntity(crate);
			crateId = crate.getUUID();
		}
		CourierEntity courier = CourierDispatch.dispatch(level, worldPosition);
		courierId = courier.getUUID();
		courierPhase = courier.phase();
		courierDeadline = level.getGameTime() + CDConfig.SHIPPING_BIN.courierWalkTimeoutTicks();
		setChanged();
	}

	/**
	 * The walk timer when the courier is not loaded (D9): past the deadline on the way in, a new courier appears beside
	 * the bin and takes over (the frozen one leaves on its own once loaded); on the way out the bin forgets it.
	 */
	private void watchCourier(ServerLevel level) {
		if (level.getEntity(courierId) != null) return;
		if (level.getGameTime() < courierDeadline + 20L) return;
		if (courierPhase == CourierEntity.PHASE_IN) {
			CourierEntity courier = ShippingBin.COURIER.get().create(level);
			if (courier == null) return;
			courier.setup(worldPosition, null, CourierEntity.PHASE_PICKUP, courierDeadline,
					CDConfig.SHIPPING_BIN.courierMode().equals("at_bin"));
			Vec3 at = CourierDispatch.besideBin(level, worldPosition);
			courier.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
			List<? extends String> names = CDConfig.SHIPPING_BIN.courierNames();
			if (!names.isEmpty()) courier.setCustomName(Component.literal(names.get(level.random.nextInt(names.size()))));
			level.addFreshEntity(courier);
			CourierEntity.teleportEffects(level, at);
			courierId = courier.getUUID();
			courierPhase = CourierEntity.PHASE_PICKUP;
		} else {
			courierGone();
		}
		setChanged();
	}

	@Nullable
	UUID courierId() {
		return courierId;
	}

	@Nullable
	UUID crateId() {
		return crateId;
	}

	void clearCrate() {
		crateId = null;
		setChanged();
	}

	void noteCourier(int phase, long deadline) {
		courierPhase = phase;
		courierDeadline = deadline;
		setChanged();
	}

	void courierGone() {
		courierId = null;
		courierPhase = 0;
		setChanged();
	}

	/** On break: the waiting crate goes (a courier on its way leaves on its own when it sees the bin gone). */
	void removeCarriers() {
		if (level instanceof ServerLevel server && crateId != null) {
			Entity crate = server.getEntity(crateId);
			if (crate != null) crate.discard();
		}
	}

	/** Everything to drop when the bin breaks: the 12 slots and the payout buffer. */
	SimpleContainer allDrops() {
		SimpleContainer drops = new SimpleContainer(SLOTS + buffer.size());
		for (int slot = 0; slot < SLOTS; slot++) drops.setItem(slot, items.get(slot));
		for (int i = 0; i < buffer.size(); i++) drops.setItem(SLOTS + i, buffer.get(i));
		return drops;
	}

	int comparatorSignal() {
		return value <= 0 ? 0 : 1 + Math.min(14, fillPermille() * 14 / 1000);
	}

	// ---- Container ----

	@Override
	public int getContainerSize() {
		return SLOTS;
	}

	@Override
	public boolean isEmpty() {
		return items.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getItem(int slot) {
		return items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack out = ContainerHelper.removeItem(items, slot, amount);
		if (!out.isEmpty()) setChanged();
		return out;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		items.set(slot, stack);
		if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
		setChanged();
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return slot < INPUTS;
	}

	@Override
	public boolean stillValid(Player player) {
		return level != null && level.getBlockEntity(worldPosition) == this
				&& player.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= 64.0D;
	}

	@Override
	public void clearContent() {
		items.clear();
		setChanged();
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return ALL_SLOTS;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
		return slot < INPUTS;
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return slot >= INPUTS;
	}

	@Override
	public void setChanged() {
		valueDirty = true;
		syncDirty = true;
		super.setChanged();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (cap == ForgeCapabilities.ITEM_HANDLER && !remove) {
			return sidedHandlers[side == null ? Direction.UP.ordinal() : side.ordinal()].cast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		for (LazyOptional<?> handler : sidedHandlers) handler.invalidate();
	}

	@Override
	public void reviveCaps() {
		super.reviveCaps();
		LazyOptional<?>[] fresh = SidedInvWrapper.create(this, Direction.values());
		System.arraycopy(fresh, 0, sidedHandlers, 0, fresh.length);
	}

	// ---- Menu ----

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.farmingforblockheads.shipping_bin");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new ShippingBinMenu(id, inventory, this, data);
	}

	// ---- Save and sync ----

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		items.clear();
		ContainerHelper.loadAllItems(tag, items);
		buffer.clear();
		ListTag list = tag.getList("Buffer", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) buffer.add(ItemStack.of(list.getCompound(i)));
		credit = tag.getInt("Credit");
		cooldown = tag.getInt("Cooldown");
		value = tag.getInt("Value");
		courierId = tag.hasUUID("Courier") ? tag.getUUID("Courier") : null;
		crateId = tag.hasUUID("Crate") ? tag.getUUID("Crate") : null;
		courierPhase = tag.getInt("CourierPhase");
		courierDeadline = tag.getLong("CourierDeadline");
		valueDirty = true;
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		ContainerHelper.saveAllItems(tag, items);
		ListTag list = new ListTag();
		for (ItemStack stack : buffer) list.add(stack.save(new CompoundTag()));
		tag.put("Buffer", list);
		tag.putInt("Credit", credit);
		tag.putInt("Cooldown", cooldown);
		tag.putInt("Value", value);
		if (courierId != null) tag.putUUID("Courier", courierId);
		if (crateId != null) tag.putUUID("Crate", crateId);
		tag.putInt("CourierPhase", courierPhase);
		tag.putLong("CourierDeadline", courierDeadline);
	}

	/** Client copy: the inputs (drawn in the crate) and the stored value (WTHIT, the display). */
	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = new CompoundTag();
		NonNullList<ItemStack> inputs = NonNullList.withSize(INPUTS, ItemStack.EMPTY);
		for (int slot = 0; slot < INPUTS; slot++) inputs.set(slot, items.get(slot));
		ContainerHelper.saveAllItems(tag, inputs, true);
		tag.putInt("Value", value);
		tag.putInt("Credit", credit);
		tag.putInt("Capacity", capacity());
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		for (int slot = 0; slot < INPUTS; slot++) items.set(slot, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, items);
		value = tag.getInt("Value");
		credit = tag.getInt("Credit");
		clientCapacity = tag.getInt("Capacity");
	}

	private int clientCapacity = 128;

	/** The capacity the server sent (client side). */
	public int clientCapacity() {
		return Math.max(1, clientCapacity);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket packet) {
		CompoundTag tag = packet.getTag();
		if (tag != null) handleUpdateTag(tag);
	}
}
