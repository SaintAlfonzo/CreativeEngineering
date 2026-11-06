package com.SaintAlfonzo.creativecompat.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sweet berries and glow berries back in the creative tabs and JEI. A plain {@code ItemNameBlockItem} fills a tab
 * through its block ({@code BlockItem.fillItemCategory} -> {@code Block.fillItemCategory} -> {@code new
 * ItemStack(block)}), and {@code block.asItem()} is the last item registered for that block. Berry Good registers its
 * pips ({@code BerryPipsBlockItem}) for the sweet berry bush and cave vines after vanilla, so the berries' own fill
 * added a second pips stack and the berries were in no tab; JEI builds its item list from the tabs.
 *
 * Only the exact {@code ItemNameBlockItem} class, and only when the block's item is a different item: then the item
 * adds itself instead. Every other case keeps vanilla's fill. SRG names, no refmap (m_6787_ fillItemCategory,
 * m_220152_ allowedIn, m_40614_ getBlock, m_5456_ asItem).
 */
@Mixin(BlockItem.class)
public abstract class BlockItemOwnStackFillMixin extends Item {

	private BlockItemOwnStackFillMixin(Properties properties) {
		super(properties);
	}

	@Inject(method = "m_6787_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeengineering$fillOwnStack(CreativeModeTab tab, NonNullList<ItemStack> items, CallbackInfo ci) {
		BlockItem self = (BlockItem) (Object) this;
		if (self.getClass() != ItemNameBlockItem.class || self.getBlock().asItem() == self) return;
		if (this.allowedIn(tab)) items.add(new ItemStack(self));
		ci.cancel();
	}
}
