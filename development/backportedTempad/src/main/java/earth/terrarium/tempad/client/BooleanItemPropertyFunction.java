// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/BooleanItemPropertyFunction.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface BooleanItemPropertyFunction extends ItemPropertyFunction {
    @Override
    default float call(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        return invoke(stack, level, entity, seed) ? 1f : 0f;
    }

    boolean invoke(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed);
}
