package com.SaintAlfonzo.creativedelight.mixin.alexsmobs;

import com.SaintAlfonzo.creativedelight.compat.BananaMergeHooks;
import com.github.alexthe666.alexsmobs.entity.ai.GorillaAIForageLeaves;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * A gorilla foraging leaves finds Neapolitan's banana, not Alex's Mobs' purged one (item_merges.md 2.1).
 */
@Mixin(value = GorillaAIForageLeaves.class, remap = false)
public abstract class GorillaForageBananaMixin {

	@Redirect(method = "breakLeaves", remap = false, at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
			target = "Lcom/github/alexthe666/alexsmobs/item/AMItemRegistry;BANANA:Lnet/minecraftforge/registries/RegistryObject;"))
	private RegistryObject<Item> creative_delight$neapolitanBanana() {
		return BananaMergeHooks.banana(AMItemRegistry.BANANA);
	}
}
