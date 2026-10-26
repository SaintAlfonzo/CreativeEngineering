package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleBases;
import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleBoxes;
import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleTank;
import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleTankHolder;
import com.SaintAlfonzo.creativedelight.qol.RemainderBufferQol;
import com.SaintAlfonzo.creativeengineering.cooking.RemainderBufferHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.farmersrespite.common.block.KettleBlock;
import umpaz.farmersrespite.common.block.entity.KettleBlockEntity;
import umpaz.farmersrespite.common.crafting.KettleRecipe;
import umpaz.farmersrespite.common.registry.FRRecipeTypes;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;

/**
 * Farmer's Respite Kettle (1.19-2.0) as a four-unit fluid tank (farmers_respite.md 2.1). The level stays the block's
 * {@code water_level} (0-4 since KettleBlockMixin), so 2.0 models and the brew's level-1 step keep working; the fluid
 * type is NBT {@code CETankFluid} (none = water, how a 2.0 Kettle reads). A brew matches its items and its {@link
 * KettleBases base} against the tank (K3, K8), uses one unit and makes one item (K2). Pipes reach the tank from every
 * side in whole units (K6). Two GUI boxes move bottles in and out (K4, {@link KettleTank#tickBoxes}). Glass bottles
 * put into an ingredient slot go to the plating slot or the leftover slots (K13). The kettle item keeps the tank
 * ({@code writeMeal}, KettleBlockMixin.setPlacedBy).
 */
@Mixin(value = KettleBlockEntity.class, remap = false)
public abstract class KettleTankMixin extends SyncedBlockEntity implements KettleTankHolder {

	@Unique private static final String CE_FLUID = "CETankFluid";
	@Unique private static final String CE_LEVEL = "CETankLevel";
	@Unique private static final String CE_BOXES = "CETankBoxes";
	@Unique private static final String CE_POURING = "CETankPouring";

	@Shadow @Final private ItemStackHandler inventory;
	@Shadow private ResourceLocation lastRecipeID;
	@Shadow private boolean checkNewRecipe;

	@Shadow public abstract ItemStack getMeal();
	@Shadow public abstract boolean isContainerValid(ItemStack containerItem);

	@Unique private FluidStack ce$type = FluidStack.EMPTY;
	@Unique private boolean ce$pouring = true;
	@Unique private ItemStackHandler ce$boxes;
	@Unique private LazyOptional<IFluidHandler> ce$fluidCap;

	private KettleTankMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/** Minecraft calls go through BlockEntity so the jar remaps them without FD's class in the hierarchy. */
	@Unique
	private BlockEntity ce$be() {
		return (BlockEntity) (Object) this;
	}

	// ---------------------------------------------------------------------------------------------------------
	// KettleTankHolder

	@Override
	public int ce$tankLevel() {
		BlockState state = ce$be().getBlockState();
		return state.hasProperty(KettleBlock.WATER_LEVEL) ? state.getValue(KettleBlock.WATER_LEVEL) : 0;
	}

	@Override
	public FluidStack ce$tankFluid() {
		int units = ce$tankLevel();
		if (units <= 0) return FluidStack.EMPTY;
		return ce$type.isEmpty() ? new FluidStack(Fluids.WATER, units * KettleTank.UNIT) : new FluidStack(ce$type, units * KettleTank.UNIT);
	}

	@Override
	public void ce$setTank(FluidStack type, int level) {
		int clamped = Math.max(0, Math.min(KettleTank.MAX_LEVEL, level));
		FluidStack newType = clamped == 0 || type.isEmpty() ? FluidStack.EMPTY : new FluidStack(type, 1);
		boolean typeChanged = !(newType.isEmpty() ? ce$type.isEmpty() : newType.isFluidEqual(ce$type));
		ce$type = newType;
		checkNewRecipe = true;
		Level world = ce$be().getLevel();
		if (world != null && clamped != ce$tankLevel()) {
			world.setBlock(ce$be().getBlockPos(), ce$be().getBlockState().setValue(KettleBlock.WATER_LEVEL, clamped), 3);
		}
		if (typeChanged) inventoryChanged(); else ce$be().setChanged();
	}

	@Override
	public ItemStackHandler ce$boxes() {
		if (ce$boxes == null) {
			ce$boxes = new KettleBoxes(() -> ce$be().setChanged());
		}
		return ce$boxes;
	}

	@Override
	public boolean ce$pouring() {
		return ce$pouring;
	}

	@Override
	public void ce$latch(int box) {
		ce$pouring = box == KettleTankHolder.FILL_BOX;
		ce$be().setChanged();
	}

	// ---------------------------------------------------------------------------------------------------------
	// NBT

	@Inject(method = "m_142466_", at = @At("TAIL"))
	private void ce$loadTank(CompoundTag tag, CallbackInfo ci) {
		ce$type = tag.contains(CE_FLUID) ? FluidStack.loadFluidStackFromNBT(tag.getCompound(CE_FLUID)) : FluidStack.EMPTY;
		if (tag.contains(CE_BOXES)) ce$boxes().deserializeNBT(tag.getCompound(CE_BOXES));
		if (tag.contains(CE_POURING)) ce$pouring = tag.getBoolean(CE_POURING);
	}

	@Inject(method = "m_183515_", at = @At("TAIL"))
	private void ce$saveTank(CompoundTag tag, CallbackInfo ci) {
		ce$writeType(tag);
		tag.put(CE_BOXES, ce$boxes().serializeNBT());
		tag.putBoolean(CE_POURING, ce$pouring);
	}

	/** The client copy (update tag): the type, so the GUI, WTHIT and the bar show it. */
	@Inject(method = "writeItems", at = @At("RETURN"))
	private void ce$syncTank(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		ce$writeType(cir.getReturnValue());
	}

	/** The kettle item (loot copy_meal, pick block): type and level, restored by KettleBlockMixin.setPlacedBy. */
	@Inject(method = "writeMeal", at = @At("RETURN"))
	private void ce$tankToItem(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		int units = ce$tankLevel();
		if (units <= 0) return;
		CompoundTag out = cir.getReturnValue();
		FluidStack type = ce$type.isEmpty() ? new FluidStack(Fluids.WATER, 1) : ce$type;
		out.put(CE_FLUID, type.writeToNBT(new CompoundTag()));
		out.putInt(CE_LEVEL, units);
	}

	@Unique
	private void ce$writeType(CompoundTag tag) {
		if (!ce$type.isEmpty()) tag.put(CE_FLUID, ce$type.writeToNBT(new CompoundTag()));
	}

	// ---------------------------------------------------------------------------------------------------------
	// Brewing: recipe by items and base (K3, K8)

	@Inject(method = "getMatchingRecipe", at = @At("HEAD"), cancellable = true)
	private void ce$matchWithBase(RecipeWrapper inv, CallbackInfoReturnable<Optional<KettleRecipe>> cir) {
		Level world = ce$be().getLevel();
		if (world == null) {
			cir.setReturnValue(Optional.empty());
			return;
		}
		FluidStack tank = ce$tankFluid();
		if (lastRecipeID != null) {
			Optional<? extends Recipe<?>> last = world.getRecipeManager().byKey(lastRecipeID);
			if (last.isPresent() && last.get() instanceof KettleRecipe kr && kr.matches(inv, world) && ce$baseMatches(kr, tank)) {
				cir.setReturnValue(Optional.of(kr));
				return;
			}
		}
		if (checkNewRecipe) {
			List<KettleRecipe> all = world.getRecipeManager().getAllRecipesFor(FRRecipeTypes.BREWING.get());
			// a recipe whose base is in the tank before one that needs no fluid
			Optional<KettleRecipe> found = all.stream()
					.filter(r -> r.matches(inv, world) && ce$baseMatches(r, tank))
					.min(Comparator.comparingInt(r -> KettleBases.get(r).isEmpty() ? 1 : 0));
			if (found.isPresent()) {
				lastRecipeID = found.get().getId();
				cir.setReturnValue(found);
				return;
			}
		}
		checkNewRecipe = false;
		cir.setReturnValue(Optional.empty());
	}

	@Unique
	private static boolean ce$baseMatches(KettleRecipe recipe, FluidStack tank) {
		FluidStack base = KettleBases.get(recipe);
		return base.isEmpty() || (!tank.isEmpty() && tank.isFluidEqual(base));
	}

	// ---------------------------------------------------------------------------------------------------------
	// Tick: boxes (K4), bottles out of the ingredient slots (K13)

	@Inject(method = "brewingTick", at = @At("HEAD"))
	private static void ce$tankTick(Level level, BlockPos pos, BlockState state, KettleBlockEntity kettle, CallbackInfo ci) {
		((KettleTankMixin) (Object) kettle).ce$serverTick();
	}

	@Unique
	private void ce$serverTick() {
		KettleTank.tickBoxes(this);
		for (int i = 0; i < 2; ++i) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.is(Items.GLASS_BOTTLE)) continue;
			ItemStack rest = ce$stowBottles(stack.copy());
			if (rest.getCount() != stack.getCount()) inventory.setStackInSlot(i, rest);
		}
	}

	/**
	 * Glass bottles from an ingredient slot go to the leftover slots (K13: a brew plates from the plating slot only);
	 * returns what stays. With the buffer off or full they stay where they are.
	 */
	@Unique
	private ItemStack ce$stowBottles(ItemStack bottles) {
		if (!RemainderBufferQol.enabled() || !((Object) this instanceof RemainderBufferHolder holder)) return bottles;
		ItemStack left = bottles;
		for (int s = 0; s < holder.ce$getRemainderBuffer().getSlots() && !left.isEmpty(); ++s) {
			left = holder.ce$getRemainderBuffer().insertItem(s, left, false);
		}
		return left;
	}

	// ---------------------------------------------------------------------------------------------------------
	// Drops, pipes

	@Inject(method = "getDroppableInventory", at = @At("RETURN"))
	private void ce$dropBoxes(CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
		for (int i = 0; i < ce$boxes().getSlots(); ++i) {
			cir.getReturnValue().add(ce$boxes().getStackInSlot(i));
		}
	}

	@Inject(method = "getCapability", at = @At("HEAD"), cancellable = true)
	private <T> void ce$fluidCapability(Capability<T> cap, @Nullable Direction side, CallbackInfoReturnable<LazyOptional<T>> cir) {
		if (cap != ForgeCapabilities.FLUID_HANDLER) return;
		if (ce$fluidCap == null || !ce$fluidCap.isPresent()) {
			KettleTank.Handler handler = new KettleTank.Handler(this);
			ce$fluidCap = LazyOptional.of(() -> handler);
		}
		cir.setReturnValue(ce$fluidCap.cast());
	}

	@Inject(method = "m_7651_", at = @At("TAIL"))
	private void ce$invalidateFluidCap(CallbackInfo ci) {
		if (ce$fluidCap != null) ce$fluidCap.invalidate();
	}
}
