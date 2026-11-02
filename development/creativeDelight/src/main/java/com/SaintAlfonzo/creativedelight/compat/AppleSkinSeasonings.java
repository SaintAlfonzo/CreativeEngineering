package com.SaintAlfonzo.creativedelight.compat;

import java.util.List;

import com.SaintAlfonzo.creativedelight.food.Seasonings;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.food.FoodValues;

/**
 * Folds a seasoned food's {@link Seasonings} bonus into the values AppleSkin shows (tooltip bar and HUD overlay), so
 * they match what eating gives. Registered only when AppleSkin is loaded; the class is never touched otherwise.
 * Nutrition adds to the hunger points; saturation is absolute points, so the modifier is recomputed from the new
 * total ({@code hunger * modifier * 2} is AppleSkin's saturation increment).
 *
 * <p>Runs last ({@link EventPriority#LOWEST}): Salt's own handler adds its {@code Salted} bonus to hunger and to the
 * saturation modifier, as its eating mixin does, and the pack bonus goes on after the food's own (salted) values on
 * eating too ({@link Seasonings#onEaten}). Run before Salt's, the modifier recomputed here would be scaled again by
 * Salt's addition and the bar would show the wrong saturation for salted seasoned food.
 */
public final class AppleSkinSeasonings {

    private AppleSkinSeasonings() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFoodValues(FoodValuesEvent event) {
        List<ResourceLocation> ids = Seasonings.of(event.itemStack);
        if (ids.isEmpty()) {
            return;
        }
        int nutrition = 0;
        float saturation = 0f;
        for (ResourceLocation id : ids) {
            Seasonings.Definition def = Seasonings.get(id);
            if (def != null) {
                nutrition += def.nutrition();
                saturation += def.saturation();
            }
        }
        if (nutrition == 0 && saturation == 0f) {
            return;
        }
        FoodValues base = event.modifiedFoodValues;
        int hunger = base.hunger + nutrition;
        if (hunger <= 0) {
            return;
        }
        float increment = base.getSaturationIncrement() + saturation;
        event.modifiedFoodValues = new FoodValues(hunger, increment / (hunger * 2f));
    }
}
