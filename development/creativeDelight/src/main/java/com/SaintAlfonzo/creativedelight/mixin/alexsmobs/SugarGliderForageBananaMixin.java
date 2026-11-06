package com.SaintAlfonzo.creativedelight.mixin.alexsmobs;

import com.SaintAlfonzo.creativedelight.compat.BananaMergeHooks;
import com.github.alexthe666.alexsmobs.entity.EntitySugarGlider;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * A sugar glider foraging jungle leaves finds Neapolitan's banana, not Alex's Mobs' purged one (item_merges.md 2.1).
 * The target is the lambda that fills the glider's leaf -> drops map in its static initializer.
 */
@Mixin(value = EntitySugarGlider.class, remap = false)
public abstract class SugarGliderForageBananaMixin {

	@Redirect(method = "lambda$static$1", remap = false, at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
			target = "Lcom/github/alexthe666/alexsmobs/item/AMItemRegistry;BANANA:Lnet/minecraftforge/registries/RegistryObject;"))
	private static RegistryObject<Item> creativedelight$neapolitanBanana() {
		return BananaMergeHooks.banana(AMItemRegistry.BANANA);
	}
}
