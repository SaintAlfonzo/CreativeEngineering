package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.JarMixing;
import java.util.List;
import net.mehvahdjukaar.supplementaries.common.items.JarItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixed jars ({@code SupplementariesJarTileMixin}): a jar item's tooltip lists one line per kind with its total
 * ("Cookie x3", "Lemon Cookie x2") instead of one "x1" line per stored layer. Supplementaries' tooltip loads the
 * stored stacks into a 27-slot list and writes a line for the first five non-empty ones; the load here groups them
 * first ({@link JarMixing#kinds}).
 */
@Mixin(value = JarItem.class, remap = false)
public abstract class SupplementariesJarItemTooltipMixin {

    @Redirect(method = "m_7373_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/ContainerHelper;m_18980_(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/NonNullList;)V"),
            remap = false)
    private void creativeengineering$loadByKind(CompoundTag tag, NonNullList<ItemStack> list) {
        ContainerHelper.loadAllItems(tag, list);
        List<ItemStack> kinds = JarMixing.kinds(list);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, i < kinds.size() ? kinds.get(i) : ItemStack.EMPTY);
        }
    }
}
