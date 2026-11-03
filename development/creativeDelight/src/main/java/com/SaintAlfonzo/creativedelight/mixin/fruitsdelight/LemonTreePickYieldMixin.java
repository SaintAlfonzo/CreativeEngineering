package com.SaintAlfonzo.creativedelight.mixin.fruitsdelight;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * TODO farming/trees #3: a ripe lemon tree gives 2 to 5 lemons. Breaking it runs the pack's loot table
 * ({@code kubejs/data/fruitsdelight/loot_tables/blocks/lemon_tree.json}), but Fruits Delight 0.5.11's right-click pick
 * ({@code DoubleFruitBushBlock.use}, SRG {@code m_6227_}) drops {@code new ItemStack(item, 1 + random.nextInt(2))}
 * itself, so a pick gave 1 to 2. This sets that count to 2 to 5 for the lemon tree only; the other double fruit bushes
 * keep theirs. Keep the range in step with the loot table's set_count.
 */
@Pseudo
@Mixin(targets = "dev.xkmc.fruitsdelight.content.block.DoubleFruitBushBlock", remap = false)
public abstract class LemonTreePickYieldMixin {

	private static final ResourceLocation creativedelight$LEMON_TREE = new ResourceLocation("fruitsdelight", "lemon_tree");
	private static final int creativedelight$MIN = 2;
	private static final int creativedelight$MAX = 5;

	@ModifyArg(method = "m_6227_", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/item/ItemStack;<init>(Lnet/minecraft/world/level/ItemLike;I)V"),
			index = 1)
	// @ModifyArg handlers get the invoked constructor's arguments only (ItemLike, int), not the enclosing use() params:
	// a handler that also took (BlockState, Level, ...) failed to apply and stopped the game loading (r1103 Boot 0).
	private int creativedelight$lemonPickCount(ItemLike item, int count) {
		Block self = (Block) (Object) this;
		if (!creativedelight$LEMON_TREE.equals(ForgeRegistries.BLOCKS.getKey(self))) return count;
		return creativedelight$MIN + ThreadLocalRandom.current().nextInt(creativedelight$MAX - creativedelight$MIN + 1);
	}
}
