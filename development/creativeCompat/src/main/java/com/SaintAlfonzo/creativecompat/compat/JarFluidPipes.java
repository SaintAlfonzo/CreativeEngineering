package com.SaintAlfonzo.creativecompat.compat;

import com.SaintAlfonzo.creativecompat.CreativeCompat;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Pipes into and out of Supplementaries jars (and goblets): every Moonlight soft-fluid tank block entity gets a Forge
 * {@link IFluidHandler}, so Create pumps and pipes (and any other Forge fluid pipe) fill and drain it. Supplementaries
 * 2.4.20 gives the jar no fluid capability at all, and Moonlight's own Forge bridge methods pass mB as a bottle count.
 * <ul>
 *   <li>Units: one bottle = {@value #MB_PER_BOTTLE} mB (Moonlight: bottle 1, bowl 2, bucket 4).</li>
 *   <li>Which fluid: {@link SoftFluidRegistry#fromVanillaFluid} (a soft fluid's {@code equivalent_fluids}, so
 *       {@code create:potion} is the potion soft fluid). Soft fluids with no Forge fluid (most soups) stay jar-only:
 *       nothing pipes in, and the jar reports empty to pipes.</li>
 *   <li>NBT: only the soft fluid's {@code preserved_tags_from_item} keys travel (Potion, Bottle, CustomPotionEffects
 *       for potions), the same keys a hand-filled jar keeps; a missing {@code Bottle} on one side still matches, as in
 *       Moonlight.</li>
 *   <li>Part bottles: pipes move a few mB a tick, the tank counts whole bottles, so up to 249 mB waits in a buffer
 *       saved with the block (ForgeCaps). Pipes see tank + buffer. A buffer whose fluid no longer matches the tank (a
 *       player poured something else into the emptied jar) is dropped.</li>
 *   <li>Filling needs {@link ISoftFluidTankProvider#canInteractWithSoftFluidTank}: no items, no mob, liquids enabled.</li>
 * </ul>
 * feature_writeup/mixed_jars.md section 10.
 */
public final class JarFluidPipes {

    public static final int MB_PER_BOTTLE = 250;
    private static final ResourceLocation ID = new ResourceLocation(CreativeCompat.NS, "jar_fluid_pipes");

    private JarFluidPipes() {}

    /** Only with Supplementaries loaded (this class names Moonlight types). */
    public static void register() {
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, JarFluidPipes::attach);
    }

    private static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof ISoftFluidTankProvider) {
            Provider provider = new Provider(event.getObject());
            event.addCapability(ID, provider);
            event.addListener(provider.optional::invalidate);
        }
    }

    private static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final Handler handler;
        final LazyOptional<IFluidHandler> optional;

        Provider(BlockEntity be) {
            this.handler = new Handler(be);
            this.optional = LazyOptional.of(() -> handler);
        }

        @Override
        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            return handler.save();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            handler.load(tag);
        }
    }

    static final class Handler implements IFluidHandler {
        // The block entity, not its tank: the attach event fires inside the constructor, before the tank field is set.
        private final BlockEntity be;
        private int pendingMb;
        @Nullable private ResourceLocation pendingId;
        @Nullable private CompoundTag pendingTag;

        Handler(BlockEntity be) {
            this.be = be;
        }

        private ISoftFluidTankProvider provider() {
            return (ISoftFluidTankProvider) be;
        }

        @Nullable
        private SoftFluidTank tank() {
            return provider().getSoftFluidTank();
        }

        // ---- IFluidHandler ----

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        @Nonnull
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? contents() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            SoftFluidTank t = tank();
            return tank == 0 && t != null ? bottleCapacity(t) * MB_PER_BOTTLE : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return tank == 0 && !SoftFluidRegistry.fromVanillaFluid(stack.getFluid()).isEmpty();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            SoftFluidTank t = tank();
            if (t == null || resource.isEmpty() || !provider().canInteractWithSoftFluidTank()) return 0;
            SoftFluid sf = SoftFluidRegistry.fromVanillaFluid(resource.getFluid());
            if (sf.isEmpty()) return 0;
            ResourceLocation id = SoftFluidRegistry.getID(sf);
            CompoundTag tag = preserved(sf, resource.getTag());
            settle(t);
            if (!t.isEmpty()) {
                if (!t.isSameFluidAs(sf, tag)) return 0;
            } else if (pendingMb > 0 && (!id.equals(pendingId) || !nbtMatches(pendingTag, tag))) {
                return 0;
            }
            int stored = t.getCount() * MB_PER_BOTTLE + pendingMb;
            int accept = Math.min(resource.getAmount(), bottleCapacity(t) * MB_PER_BOTTLE - stored);
            if (accept <= 0) return 0;
            if (action.execute()) {
                int total = pendingMb + accept;
                int bottles = total / MB_PER_BOTTLE;
                pendingMb = total % MB_PER_BOTTLE;
                if (bottles > 0) t.tryAddingFluid(sf, bottles, tag);
                if (pendingMb > 0) {
                    pendingId = id;
                    pendingTag = tag;
                } else {
                    clearPending();
                }
                be.setChanged();
            }
            return accept;
        }

        @Override
        @Nonnull
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return FluidStack.EMPTY;
            FluidStack in = contents();
            if (!in.isFluidEqual(resource)) return FluidStack.EMPTY;
            return drain(resource.getAmount(), action);
        }

        @Override
        @Nonnull
        public FluidStack drain(int maxDrain, FluidAction action) {
            SoftFluidTank t = tank();
            if (t == null || maxDrain <= 0) return FluidStack.EMPTY;
            FluidStack in = contents();
            if (in.isEmpty()) return FluidStack.EMPTY;
            int take = Math.min(maxDrain, in.getAmount());
            if (action.execute()) {
                if (take <= pendingMb) {
                    pendingMb -= take;
                } else {
                    // open whole bottles into the buffer, then take from it
                    int bottles = (take - pendingMb + MB_PER_BOTTLE - 1) / MB_PER_BOTTLE;
                    pendingId = SoftFluidRegistry.getID(t.getFluid());
                    pendingTag = preserved(t.getFluid(), t.getNbt());
                    t.shrink(bottles);
                    pendingMb = pendingMb + bottles * MB_PER_BOTTLE - take;
                }
                if (pendingMb == 0) clearPending();
                be.setChanged();
            }
            FluidStack out = in.copy();
            out.setAmount(take);
            return out;
        }

        // ---- contents ----

        /** Tank + buffer as one Forge stack; empty when the soft fluid has no Forge fluid. */
        private FluidStack contents() {
            SoftFluidTank t = tank();
            if (t == null) return FluidStack.EMPTY;
            settle(t);
            SoftFluid sf;
            CompoundTag tag;
            if (!t.isEmpty()) {
                sf = t.getFluid();
                tag = preserved(sf, t.getNbt());
            } else if (pendingMb > 0 && pendingId != null) {
                sf = SoftFluidRegistry.getOptional(pendingId).orElse(null);
                if (sf == null) {
                    clearPending();
                    return FluidStack.EMPTY;
                }
                tag = pendingTag;
            } else {
                return FluidStack.EMPTY;
            }
            Fluid forge = sf.getForgeFluid();
            if (forge == null || forge == Fluids.EMPTY) return FluidStack.EMPTY;
            int amount = t.getCount() * MB_PER_BOTTLE + pendingMb;
            FluidStack out = new FluidStack(forge, amount);
            if (tag != null && !tag.isEmpty()) {
                CompoundTag copy = tag.copy();
                // as Moonlight's applyNBTtoFluidStack: IE potion fluid has no bottle type
                if ("immersiveengineering".equals(net.minecraftforge.registries.ForgeRegistries.FLUIDS.getKey(forge).getNamespace())) {
                    copy.remove(SoftFluidTank.POTION_TYPE_KEY);
                }
                if (!copy.isEmpty()) out.setTag(copy);
            }
            return out;
        }

        /** Drop a buffer the tank no longer agrees with (a player changed the jar's fluid by hand). */
        private void settle(SoftFluidTank t) {
            if (pendingMb <= 0) {
                clearPending();
                return;
            }
            if (t.isEmpty()) return;
            ResourceLocation tankId = SoftFluidRegistry.getID(t.getFluid());
            if (!tankId.equals(pendingId) || !nbtMatches(preserved(t.getFluid(), t.getNbt()), pendingTag)) {
                clearPending();
            }
        }

        private void clearPending() {
            pendingMb = 0;
            pendingId = null;
            pendingTag = null;
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            if (pendingMb > 0 && pendingId != null) {
                tag.putInt("PendingMb", pendingMb);
                tag.putString("Fluid", pendingId.toString());
                if (pendingTag != null && !pendingTag.isEmpty()) tag.put("Tag", pendingTag.copy());
            }
            return tag;
        }

        void load(CompoundTag tag) {
            clearPending();
            int mb = tag.getInt("PendingMb");
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("Fluid"));
            if (mb > 0 && mb < MB_PER_BOTTLE && id != null) {
                pendingMb = mb;
                pendingId = id;
                pendingTag = tag.contains("Tag") ? tag.getCompound("Tag") : null;
            }
        }
    }

    // ---- helpers ----

    /** The tank's capacity in bottles (SoftFluidTank keeps it protected; canAdd tests count + n against it). */
    private static int bottleCapacity(SoftFluidTank t) {
        int free = 0;
        while (free < 4096 && t.canAdd(free + 1)) free++;
        return t.getCount() + free;
    }

    /** The keys of {@code tag} this soft fluid keeps from its items; null when none. */
    @Nullable
    private static CompoundTag preserved(SoftFluid sf, @Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty() || sf.getNbtKeyFromItem() == null) return null;
        CompoundTag out = new CompoundTag();
        for (String key : sf.getNbtKeyFromItem()) {
            if (tag.contains(key)) out.put(key, tag.get(key).copy());
        }
        return out.isEmpty() ? null : out;
    }

    /** Moonlight's rule: null equals empty, and a {@code Bottle} key on only one side is ignored. */
    private static boolean nbtMatches(@Nullable CompoundTag a, @Nullable CompoundTag b) {
        boolean ae = a == null || a.isEmpty();
        boolean be = b == null || b.isEmpty();
        if (ae || be) return ae == be;
        String bottle = SoftFluidTank.POTION_TYPE_KEY;
        if (a.contains(bottle) != b.contains(bottle)) {
            a = a.copy();
            b = b.copy();
            a.remove(bottle);
            b.remove(bottle);
        }
        return a.equals(b);
    }
}
