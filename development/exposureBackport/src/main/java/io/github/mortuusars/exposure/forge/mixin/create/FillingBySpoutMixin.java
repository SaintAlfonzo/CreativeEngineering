package io.github.mortuusars.exposure.forge.mixin.create;

import com.simibubi.create.content.fluids.spout.FillingBySpout;
import io.github.mortuusars.exposure.world.item.FilmItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Film developed by a spout (Create sequenced assembly) keeps its data: each step's result gets the input film's
 * NBT, except the {@code SequencedAssembly} progress tag Create just wrote. Upstream does the same with Create 6's
 * data components. Only applied when Create is installed (forge.ExposureForgeMixinPlugin).
 */
@Mixin(value = FillingBySpout.class, remap = false)
public class FillingBySpoutMixin {
   @ModifyVariable(
      method = "fillItem(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/item/ItemStack;Lnet/minecraftforge/fluids/FluidStack;)Lnet/minecraft/world/item/ItemStack;",
      at = @At(value = "INVOKE_ASSIGN", target = "Lcom/simibubi/create/content/fluids/transfer/FillingRecipe;rollResults()Ljava/util/List;"),
      ordinal = 0
   )
   private static List<ItemStack> onFillItem(List<ItemStack> results, Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
      if (!(stack.getItem() instanceof FilmItem) || stack.getTag() == null) {
         return results;
      }

      List<ItemStack> modified = new ArrayList<>(results);
      for (int i = 0; i < modified.size(); i++) {
         ItemStack result = modified.get(i);
         if (result.getItem() instanceof FilmItem) {
            CompoundTag sequencedAssembly = result.getTagElement("SequencedAssembly");
            CompoundTag tag = stack.getTag().copy();
            tag.remove("SequencedAssembly");
            if (sequencedAssembly != null) {
               tag.put("SequencedAssembly", sequencedAssembly);
            }
            result.setTag(tag);
            modified.set(i, result);
            break;
         }
      }

      return modified;
   }
}
