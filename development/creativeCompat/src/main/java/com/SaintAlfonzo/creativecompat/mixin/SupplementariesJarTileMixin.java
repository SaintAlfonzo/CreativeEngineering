package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.JarMixing;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs.Functional;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixed Supplementaries jars (rules in {@link JarMixing}). The jar gets one slot per capacity point instead of one
 * slot holding a stack, each slot holds one item, filled bottom to top:
 * <ul>
 *   <li>{@code canPlaceItem} / {@code canPlaceItemThroughFace}: any {@code #supplementaries:cookies} item that
 *       {@link JarMixing#canMix} allows, while the jar has room and no fluid or mob (the old test wanted the same
 *       item). An empty slot other than the lowest one refuses, so item handlers (hoppers, funnels: Forge's unsided
 *       {@code InvWrapper}, which only asks {@code canPlaceItem}) fill in order. An occupied slot answers yes for the
 *       player path, which always asks slot 0; inserting into a full one-item slot is a no-op anyway.</li>
 *   <li>{@code addItem} fills the lowest empty slot; {@code extractItem} takes the top layer.</li>
 *   <li>{@code getMaxStackSize} is 1 (per slot), so the comparator reads total / capacity; {@code isFull} counts
 *       every slot.</li>
 *   <li>Load and save put the contents back in order ({@link JarMixing#normalize}): old one-stack jars split into
 *       layers, and a gap left by an extraction from the middle closes.</li>
 * </ul>
 * Rendering: {@code SupplementariesJarRendererMixin} and {@code SupplementariesJarItemRendererMixin}; tooltip
 * {@code SupplementariesJarItemTooltipMixin}. feature_writeup/mixed_jars.md.
 */
@Mixin(value = JarBlockTile.class, remap = false)
public abstract class SupplementariesJarTileMixin {

    @Shadow(remap = false)
    @Final
    private int capacity;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void creativeengineering$oneSlotPerItem(BlockPos pos, BlockState state, CallbackInfo ci) {
        ((ItemDisplayTileAccessor) this).creativeengineering$setStacks(
                NonNullList.withSize(Math.max(1, capacity), ItemStack.EMPTY));
    }

    @Inject(method = "m_7013_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$canPlaceMixed(int index, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(creativeengineering$accepts(index, stack));
    }

    @Inject(method = "m_7155_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$canPlaceThroughFaceMixed(int index, ItemStack stack, Direction direction,
                                                              CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(creativeengineering$accepts(index, stack));
    }

    @Inject(method = "addItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$addLayer(ItemStack stack, CallbackInfo ci) {
        Container jar = (Container) this;
        int slot = JarMixing.firstEmpty(jar);
        if (slot >= 0 && !stack.isEmpty()) {
            ItemStack one = stack.copy();
            one.setCount(1);
            jar.setItem(slot, one);
        }
        ci.cancel();
    }

    @Inject(method = "extractItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$takeTopLayer(CallbackInfoReturnable<ItemStack> cir) {
        Container jar = (Container) this;
        int slot = JarMixing.top(jar);
        cir.setReturnValue(slot < 0 ? ItemStack.EMPTY : jar.removeItem(slot, 1));
    }

    @Inject(method = "isFull", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$isFull(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(JarMixing.total((Container) this) >= capacity);
    }

    @Inject(method = "m_6893_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$oneItemPerSlot(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(1);
    }

    @Inject(method = "resetHolders", at = @At("RETURN"), remap = false)
    private void creativeengineering$clearAllLayers(CallbackInfo ci) {
        NonNullList<ItemStack> stacks = ((ItemDisplayTileAccessor) this).creativeengineering$getStacks();
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
    }

    @Inject(method = "m_142466_", at = @At("RETURN"), remap = false)
    private void creativeengineering$normalizeOnLoad(CompoundTag tag, CallbackInfo ci) {
        creativeengineering$normalize();
    }

    @Inject(method = "m_183515_", at = @At("HEAD"), remap = false)
    private void creativeengineering$normalizeOnSave(CompoundTag tag, CallbackInfo ci) {
        creativeengineering$normalize();
    }

    private void creativeengineering$normalize() {
        ItemDisplayTileAccessor self = (ItemDisplayTileAccessor) this;
        NonNullList<ItemStack> stacks = self.creativeengineering$getStacks();
        if (stacks.size() < capacity) { // a load sized the list from an older, smaller save
            NonNullList<ItemStack> grown = NonNullList.withSize(capacity, ItemStack.EMPTY);
            for (int i = 0; i < stacks.size(); i++) {
                grown.set(i, stacks.get(i));
            }
            self.creativeengineering$setStacks(grown);
            stacks = grown;
        }
        JarMixing.normalize(stacks);
    }

    private boolean creativeengineering$accepts(int index, ItemStack stack) {
        JarBlockTile tile = (JarBlockTile) (Object) this;
        Container jar = (Container) this;
        if (stack.isEmpty() || !Functional.JAR_COOKIES.get() || !tile.fluidHolder.isEmpty()
                || !tile.mobContainer.isEmpty() || !stack.is(ModTags.COOKIES)) {
            return false;
        }
        if (JarMixing.total(jar) >= capacity || !JarMixing.canMix(jar, stack)) {
            return false;
        }
        return index < 0 || index >= jar.getContainerSize() || !jar.getItem(index).isEmpty()
                || index == JarMixing.firstEmpty(jar);
    }
}
